package com.example.demo;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.File;

public class S3Operations {
    public static void putFileInS3(String fileName){
        System.out.println("putFileInS3 started with file Name:" + fileName);
        AmazonS3 s3client = AWSClientGenerator.getS3Client();

        PutObjectResult tmp = s3client.putObject(
                Configuration.BUCKET_NAME,
                fileName,
                new File("."+File.separator + fileName)
        );
        System.out.println("Completed Uploading");
    }
}
