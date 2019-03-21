package com.example.demo;

import static com.example.demo.Configuration.*;
import static com.example.demo.SQSOperations.getApproximateNumberOfMsgs;

import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;

import static com.example.demo.EC2Operations.getIdsOfRunningInstances;
import static com.example.demo.EC2Operations.CreateOrStartInstance;
import static com.example.demo.EC2Operations.stopInstance;
import static com.example.demo.AWSClientGenerator.getSQSClient;
import static com.example.demo.AWSClientGenerator.getS3Client;
import static com.example.demo.SQSOperations.receiveMessage;
import static com.example.demo.SQSOperations.deleteMessage;

public class AutoScaler {
	 public static void main(String[] args) {
		 ScaleInOut();	
}
	 public static void ScaleInOut()
	 {
		// Query the queue for number of messages
		AmazonS3 s3client = getS3Client();
		int numberOfMsgs = getApproximateNumberOfMsgs();
		System.out.println("Number of messages "+ numberOfMsgs +"\n");
		List<String> runningEC2Ids = getIdsOfRunningInstances();
		int countRunningEC2 = runningEC2Ids.size();
		int numOfAppEC2 = countRunningEC2 - 1;
		System.out.println("Number of instances "+ numOfAppEC2 +"\n");
		// Scale Out
		if (numberOfMsgs > 0 && numberOfMsgs > numOfAppEC2 && numOfAppEC2 < MAXIMUM_NO_OF_INSTANCES )
		{
			int num = numberOfMsgs - numOfAppEC2;
			System.out.println("Scale out");
			CreateOrStartInstance(IMAGE_ID, num);
		}
		//Scale In
		if (numOfAppEC2 > MINIMUM_NO_OF_INSTANCES && numberOfMsgs < numOfAppEC2)
		{
			int num = numOfAppEC2 - numberOfMsgs;
			if (numberOfMsgs < MINIMUM_NO_OF_INSTANCES)
			{
				num = num - MINIMUM_NO_OF_INSTANCES;
			}
			System.out.println("Scale in");
			for (int i=0;i<num;i++)
			{
				stopInstance(runningEC2Ids.get(i));
			}
		}
		int i = 0;
		runningEC2Ids = getIdsOfRunningInstances();
		while(i< numberOfMsgs)
		{
			Message message = receiveMessage();
			s3client.putObject(BUCKET_NAME, runningEC2Ids.get(i),message.getBody());
			deleteMessage(message);
			i++;

		}
	 }
}