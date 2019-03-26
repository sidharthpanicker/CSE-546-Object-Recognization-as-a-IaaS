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

	 public static List<String> getIdsOfFreeRunningInstances()
	 {
		// Calculates free running instances
		 List<String> runningInstances = getIdsOfRunningInstances();
		 List<String> freeRunningEc2Ids = new ArrayList<String>();
		 int numOfAppEC2 = runningInstances.size();
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
	 
	 public static void ScaleInOut() {

		 List<String> runningEC2Ids = getIdsOfRunningInstances();
		 List<String> stoppedEC2Ids = getIdsOfStoppedInstances();
		 int numOfAppEC2 = runningEC2Ids.size();
		 List<String> freeRunningEc2Ids = getIdsOfFreeRunningInstances();
		 int capacity = freeRunningEc2Ids.size() + stoppedEC2Ids.size();
		 int busyRunning = (runningEC2Ids.size()-freeRunningEc2Ids.size());

		 System.out.println("Number of running ec2 " + runningEC2Ids.size() + "\n");
		 System.out.println("Number of free running ec2 " + freeRunningEc2Ids.size() + "\n");
		 System.out.println("Number of busy running ec2 " + busyRunning + "\n");
		 System.out.println("Number of stopped ec2 " + stoppedEC2Ids.size() + "\n");


		 // Query the queue for number of messages
		 AmazonS3 s3client = getS3Client();
		 int numberOfMsgs = getApproximateNumberOfMsgs();
		 System.out.println("Number of messages " + numberOfMsgs + "\n");


/*		 // Scale Out
		 if (numberOfMsgs > 0 && numberOfMsgs > freeRunningEc2Ids.size() && numOfAppEC2 < MAXIMUM_NO_OF_INSTANCES) {
		 	if (numberOfMsgs < stoppedEC2Ids.size())
			 int num = numberOfMsgs - capacity;
			 if (num > MAXIMUM_NO_OF_INSTANCES) {
				 num = MAXIMUM_NO_OF_INSTANCES;
			 }
			 System.out.println("Scale out");
			 int status = CreateInstance(IMAGE_ID, num);
			 for (int i = 0; i < stoppedEC2Ids.size(); i++) {
				 startInstance(stoppedEC2Ids.get(i));
			 }

		 }
		 if( numberOfMsgs < capacity) {
			 if(numberOfMsgs > 0 && numberOfMsgs <= stoppedEC2Ids.size() && numberOfMsgs> freeRunningEc2Ids.size() ) {
				int num =  freeRunningEc2Ids.size() - numberOfMsgs;
			 	for (int i=0;i<num;i++)
				 {
					 startInstance(stoppedEC2Ids.get(i));
				 }
			 }
			 */

			// Scale Out
		 	if(numberOfMsgs> 0 && numberOfMsgs> capacity && numOfAppEC2 < MAXIMUM_NO_OF_INSTANCES)
			{
				if(numberOfMsgs > freeRunningEc2Ids.size())
				{
					int num = numberOfMsgs - capacity;
					if (num > MAXIMUM_NO_OF_INSTANCES - capacity - busyRunning) {
						num = MAXIMUM_NO_OF_INSTANCES- capacity - busyRunning;
					}
					System.out.println("Scale out");
					System.out.println("Creating new Instances " + num);
					if(num > 0){
						int status = CreateInstance(IMAGE_ID, num);
					}
					for (int i = 0; i < stoppedEC2Ids.size(); i++) {
						System.out.println("Start Stopped instances");
						startInstance(stoppedEC2Ids.get(i));
					}
				}

			}
		 	else if(numberOfMsgs< capacity)
			{
				if(numberOfMsgs > freeRunningEc2Ids.size() && (numberOfMsgs-freeRunningEc2Ids.size())<=stoppedEC2Ids.size())
				{
					int startinstancenum= (numberOfMsgs-freeRunningEc2Ids.size());
					for (int i=0;i<startinstancenum;i++)
					{
						System.out.println("Start Stopped instances");
						System.out.println("I value"+ i+"stoppedEC2Ids size"+stoppedEC2Ids.size());
						startInstance(stoppedEC2Ids.get(i));
					}
				}

			}
		 	// Scale In
		 	if(numOfAppEC2 > MINIMUM_NO_OF_INSTANCES && numberOfMsgs < freeRunningEc2Ids.size()){
			 int num = freeRunningEc2Ids.size() - numberOfMsgs;
			 if (numberOfMsgs < MINIMUM_NO_OF_INSTANCES) {
				 num = num - MINIMUM_NO_OF_INSTANCES;
			 }
			 System.out.println("Scale in");
			 for (int i = 0; i < num; i++) {
				 System.out.println("Stop instances");
				 stopInstance(freeRunningEc2Ids.get(i));
			 }
		 	}
		if(numberOfMsgs > 0 && numberOfMsgs < capacity && numberOfMsgs > freeRunningEc2Ids.size()){

			 for (int i=0;i<numberOfMsgs && i<stoppedEC2Ids.size();i++)
			 {
				 System.out.println("Starting instances");
				 System.out.println("I value"+ i+"numofMessages"+numberOfMsgs+"stoppedEC2Ids size"+stoppedEC2Ids.size());
				 startInstance(stoppedEC2Ids.get(i));
			 }
		 }
		//Add key value (instanceID, Video name) in S3
		if(numberOfMsgs > 0) {
			int i = 0;
			runningEC2Ids = getIdsOfFreeRunningInstances();
			while (i < runningEC2Ids.size() && i< numberOfMsgs) {
				Message message = receiveMessage();

				if(message != null) {
					String InstId = runningEC2Ids.get(i);
					System.out.println("Adding message and video name to S3");
					s3client.putObject(BUCKET_NAME, InstId , message.getBody());
					System.out.println("Deleting message from sqs");
					deleteMessage(message);
				}
				i++;

			}
		}
	 }
}