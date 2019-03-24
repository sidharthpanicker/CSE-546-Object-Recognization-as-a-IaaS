package com.example.demo;

import static com.example.demo.Configuration.*;
import static com.example.demo.SQSOperations.getApproximateNumberOfMsgs;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.model.Message;

import static com.example.demo.EC2Operations.getIdsOfRunningInstances;
import static com.example.demo.EC2Operations.CreateInstance;
import static com.example.demo.EC2Operations.startInstance;
import static com.example.demo.EC2Operations.getIdsOfStoppedInstances;
import static com.example.demo.EC2Operations.stopInstance;
import static com.example.demo.AWSClientGenerator.getS3Client;
import static com.example.demo.SQSOperations.receiveMessage;
import static com.example.demo.SQSOperations.deleteMessage;
import static com.example.demo.S3Operations.existsInS3;
public class AutoScaler {
	 public static void main(String[] args) {
		 ScaleInOut();	
}
	 public static List<String> getIdsOfFreeRunningInstances()
	 {
		// Calculates free running instances
		 List<String> runningInstances = getIdsOfRunningInstances();
		 List<String> freeRunningEc2Ids = new ArrayList<String>();
		 int numOfAppEC2 = runningInstances.size() -1;
		 for(int i=0;i<numOfAppEC2;i++)
			{
				String instanceId = runningInstances.get(i);
				boolean existsInS3 = existsInS3(instanceId);
				if(existsInS3 != true)
				{
					freeRunningEc2Ids.add(instanceId);
				}
			}
		 return freeRunningEc2Ids;
	 }
	 
	 public static void ScaleInOut()
	 {
		
		List<String> runningEC2Ids = getIdsOfRunningInstances();
		List<String> stoppedEC2Ids = getIdsOfStoppedInstances();
		int numOfAppEC2 = runningEC2Ids.size() -1;
		List<String> freeRunningEc2Ids = getIdsOfFreeRunningInstances();
		int capacity = freeRunningEc2Ids.size() + stoppedEC2Ids.size();
		System.out.println("Number of free running ec2 "+ freeRunningEc2Ids.size() +"\n");
		
		// Query the queue for number of messages
		AmazonS3 s3client = getS3Client();
		int numberOfMsgs = getApproximateNumberOfMsgs();
		System.out.println("Number of messages "+ numberOfMsgs +"\n");
					
		
		// Scale Out
		if (numberOfMsgs > 0 && numberOfMsgs > capacity && numOfAppEC2 < MAXIMUM_NO_OF_INSTANCES )
		{
			int num = numberOfMsgs - capacity;
			if (num > MAXIMUM_NO_OF_INSTANCES )
			{
				num = MAXIMUM_NO_OF_INSTANCES;
			}
			System.out.println("Scale out");
    		int status = CreateInstance(IMAGE_ID, num);
    		for (int i=0;i<stoppedEC2Ids.size();i++)
    		{
    			startInstance(stoppedEC2Ids.get(i));
    		}
			
		}
		//Scale In
		if (numOfAppEC2 > MINIMUM_NO_OF_INSTANCES && numberOfMsgs < capacity)
		{
			int num = numOfAppEC2 - numberOfMsgs;
			if (numberOfMsgs < MINIMUM_NO_OF_INSTANCES)
			{
				num = num - MINIMUM_NO_OF_INSTANCES;
			}
			System.out.println("Scale in");
			for (int i=0;i<num;i++)
			{
				stopInstance(freeRunningEc2Ids.get(i));
			}
		}
		int i = 0;
		runningEC2Ids = getIdsOfFreeRunningInstances();
		while(i< runningEC2Ids.size())
		{
			Message message = receiveMessage();
			s3client.putObject(BUCKET_NAME, runningEC2Ids.get(i),message.getBody());
			deleteMessage(message);
			i++;

		}
	 }
}