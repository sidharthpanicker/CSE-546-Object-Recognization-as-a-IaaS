package com.example.demo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import static com.example.demo.Configuration.*;

public  class AWSClientGenerator {


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
    public static AmazonEC2 getEC2Client(){
        AWSCredentials credentials = new BasicAWSCredentials(
                ACCESS_KEY,
                SECRET_KEY
        );
        AmazonEC2 ec2Client = AmazonEC2ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(REGION)
                .build();
        return ec2Client;
    }
}
