package com.example.demo;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.sqs.model.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

@SpringBootApplication
@RestController


public class WebAppApplication {
    public static final String BUCKET_NAME = "cloud-spring-19";
    public static final String RASBERRY_PIE_URL = "http://206.207.50.7/getvideo";
    public static final String ACCESS_KEY = "AKIAJOSOG2IZ6HDA5ZXQ";
    public static final String SECRET_KEY = "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";
    public static final Regions REGION = Regions.US_WEST_2;
    private static final int BUFFER_SIZE = 4096;
    public static final String QUEUE_NAME = "cloud-computing.fifo";


    public static void main(String[] args) {
        SpringApplication.run(WebAppApplication.class, args);
    }

    @RequestMapping("/hello")
    public String sayHello() {
        try {
            String fileName = downloadFile(RASBERRY_PIE_URL,".");
            putFileInS3(fileName);
            deleteTempFile(fileName);
            String queueUrl = getQueueUrl();
            sendMessageToSQSQueue(queueUrl,fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return("Failed");
        }
        return("Successfully Queued");
    }

    private void deleteTempFile(String fileName) {
        File file = new File(fileName);

        if(file.delete())
        {
            System.out.println("File deleted successfully");
        }
        else
        {
            System.out.println("Failed to delete the file");
        }
    }


    public static String downloadFile(String fileURL, String saveDir)
            throws IOException {
        String fileName = "";
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }

            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();

            String saveFilePath = saveDir + File.separator + fileName;

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
        return fileName;
    }


    public static String createSQSQueue(){
        // Create a FIFO queue
        AmazonSQS sqs = getSQSClient();
        System.out.println("Creating a new Amazon SQS FIFO queue called " + QUEUE_NAME+"\n");
        final Map<String, String> attributes = new HashMap<String, String>();

        // A FIFO queue must have the FifoQueue attribute set to True
        attributes.put("FifoQueue", "true");

        // If the user doesn't provide a MessageDeduplicationId, generate a MessageDeduplicationId based on the content.
        //attributes.put("ContentBasedDeduplication", "true");

        // The FIFO queue name must end with the .fifo suffix
        final CreateQueueRequest createQueueRequest = new CreateQueueRequest(QUEUE_NAME)
                .withAttributes(attributes);
        final String myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
        return myQueueUrl;
    }

    public static String getQueueUrl() {
        try {
            AmazonSQS sqs = getSQSClient();
            String queue_url = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
            System.out.println("Queue Url is" + queue_url);
            return queue_url;
        } catch (AmazonSQSException e) {
                return createSQSQueue();
        }
    }


    public static void sendMessageToSQSQueue(String queueUrl,String videoName){
        AmazonSQS sqs = getSQSClient();
        System.out.println("Sending a message to fifo.\n");
        final SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, "This is my message text.");

        // When you send messages to a FIFO queue, you must provide a non-empty MessageGroupId.
        sendMessageRequest.setMessageGroupId("messageGroup1");
        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("VideoName", new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(videoName));

        sendMessageRequest.withMessageAttributes(messageAttributes);

        // Uncomment the following to provide the MessageDeduplicationId
        sendMessageRequest.setMessageDeduplicationId("1");
        final SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);
        final String sequenceNumber = sendMessageResult.getSequenceNumber();
        final String messageId = sendMessageResult.getMessageId();
        System.out.println("SendMessage succeed with messageId " + messageId + ", sequence number " + sequenceNumber
                + "\n");

    }

    public static void putFileInS3(String fileName){
        System.out.println("putFileInS3 started with file Name:" + fileName);
        AmazonS3 s3client = getS3Client();

        PutObjectResult tmp = s3client.putObject(
                BUCKET_NAME,
                 fileName,
                new File("."+File.separator + fileName)
        );
        System.out.println("Completed Uploading");
    }

    public static AmazonS3 getS3Client(){
        AWSCredentials credentials = new BasicAWSCredentials(
                ACCESS_KEY,
                SECRET_KEY
        );
        AmazonS3 s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(REGION)
                .build();
        return s3Client;
    }

    public static AmazonSQS getSQSClient(){
        AWSCredentials credentials = new BasicAWSCredentials(
                ACCESS_KEY,
                SECRET_KEY
        );
        AmazonSQS sqsClient = AmazonSQSClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(REGION)
                .build();
        return sqsClient;
    }

}
