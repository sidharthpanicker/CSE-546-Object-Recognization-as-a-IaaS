package com.example.demo;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import static com.example.demo.Common.getValueFromS3Object;
import static com.example.demo.Common.printToTheRequiredStream;
import static com.example.demo.Configuration.BUCKET_NAME;
import static com.example.demo.AWSClientGenerator.getS3Client;

import java.io.File;
import java.io.IOException;

public class S3Operations {
    public static int putFileInS3(String fileName){
        int result = 0;
        printToTheRequiredStream("putFileInS3 started with file Name:" + fileName);
        AmazonS3 s3client = AWSClientGenerator.getS3Client();
        try{
            PutObjectResult tmp = s3client.putObject(
                    Configuration.BUCKET_NAME,
                    fileName,
                    new File("."+File.separator + fileName)
            );
            printToTheRequiredStream("Completed Uploading");
            result = 1;
        }catch(AmazonS3Exception e){
            String CONTENT_MD5 = "Content-MD5";
            printToTheRequiredStream("Error in:" + fileName);
            if(e.getMessage().contains(CONTENT_MD5)){
                printToTheRequiredStream("Duplicate File Found");
                result = 2;
            }else{
                result = 0;
            }
            e.printStackTrace();

        }
        catch(Exception e){
            printToTheRequiredStream("Error in:" + fileName);
            e.printStackTrace();
            result = 0;
        }
        return result;
    }
    public static boolean existsInS3(String instanceId)
    {
        boolean result = false;
        try{
            AmazonS3 s3client = getS3Client();
            S3Object fullObject = null;
            fullObject = s3client.getObject(new GetObjectRequest(BUCKET_NAME, instanceId));
            result = true;
        }catch (AmazonServiceException e) {
            String errorCode = e.getErrorCode();
            if (!errorCode.equals("NoSuchKey")) {
                result = false;
            }
        }
        return result;
    }

    public static String getValueFromKey(String key) throws IOException {
        String result = "Not Found";
            AmazonS3 s3client = getS3Client();
            if(existsInS3(key)){
                S3Object fullObject = null;
                fullObject = s3client.getObject(new GetObjectRequest(BUCKET_NAME, key));
                if(fullObject != null){
                    result =  getValueFromS3Object(fullObject.getObjectContent());
                }
            }
        return result;
    }
}
