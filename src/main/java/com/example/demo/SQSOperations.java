package com.example.demo;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.demo.AWSClientGenerator.getSQSClient;
import static com.example.demo.Configuration.QUEUE_NAME;


public class SQSOperations {
    private static final String QUEUELENGTHATTR = "ApproximateNumberOfMessages";

    public static void sendMessageToSQSQueue(String queueUrl,String videoName){
        AmazonSQS sqs = getSQSClient();
        System.out.println("Sending a message to fifo.\n");
        final SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, videoName);

        // When you send messages to a FIFO queue, you must provide a non-empty MessageGroupId.
        sendMessageRequest.setMessageGroupId("messageGroup1");
        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("VideoName", new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(videoName));

        sendMessageRequest.withMessageAttributes(messageAttributes);

        // Uncomment the following to provide the MessageDeduplicationId
        //sendMessageRequest.se
        final SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);
        final String sequenceNumber = sendMessageResult.getSequenceNumber();
        final String messageId = sendMessageResult.getMessageId();
        System.out.println("SendMessage succeed with messageId " + messageId + ", sequence number " + sequenceNumber
                + "\n");

    }

    public static String getQueueUrl() {
        try {
            AmazonSQS sqs = getSQSClient();
            String queue_url = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
            //System.out.println("Queue Url is" + queue_url);
            return queue_url;
        } catch (AmazonSQSException e) {
            return createSQSQueue();
        }
    }

    public static String createSQSQueue(){
        // Create a FIFO queue
        AmazonSQS sqs = getSQSClient();
        System.out.println("Creating a new Amazon SQS FIFO queue called " + QUEUE_NAME+"\n");
        final Map<String, String> attributes = new HashMap<String, String>();

        // A FIFO queue must have the FifoQueue attribute set to True
        attributes.put("FifoQueue", "true");

        // If the user doesn't provide a MessageDeduplicationId, generate a MessageDeduplicationId based on the content.
        attributes.put("ContentBasedDeduplication", "true");

        // The FIFO queue name must end with the .fifo suffix
        final CreateQueueRequest createQueueRequest = new CreateQueueRequest(QUEUE_NAME)
                .withAttributes(attributes);
        final String myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
        return myQueueUrl;
    }

    public static void getMessagesFromSQSQueue(){
        AmazonSQS sqs = getSQSClient();
        System.out.println("Receiving messages from MyQueue.\n");
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(getQueueUrl());
        final List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        for (final Message message : messages) {
            System.out.println("Message");
            System.out.println("  MessageId:     " + message.getMessageId());
            System.out.println("  ReceiptHandle: " + message.getReceiptHandle());
            System.out.println("  MD5OfBody:     " + message.getMD5OfBody());
            System.out.println("  Body:          " + message.getBody());
            for (final Map.Entry<String, String> entry : message.getAttributes().entrySet()) {
                System.out.println("Attribute");
                System.out.println("  Name:  " + entry.getKey());
                System.out.println("  Value: " + entry.getValue());
            }
            //sqs.deleteMessage(getQueueUrl(), message.getReceiptHandle());
        }

        System.out.println();
    }

    public static int getSQSQueueSize(){
        int messages = 0;
        AmazonSQS sqs = getSQSClient();
        GetQueueAttributesResult result = sqs.getQueueAttributes(getQueueUrl(), Arrays.asList(QUEUELENGTHATTR));
        Map<String, String> attrs = result.getAttributes();
        if (attrs.containsKey(QUEUELENGTHATTR)) {
            messages =Integer.parseInt(attrs.get(QUEUELENGTHATTR));
        }
        return messages;
    }
}
