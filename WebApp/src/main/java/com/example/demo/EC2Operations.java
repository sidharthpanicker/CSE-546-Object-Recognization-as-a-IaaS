package com.example.demo;


import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.example.demo.AWSClientGenerator.getEC2Client;
public class EC2Operations{
	
    public static int CreateInstance(String imageId, Integer maxNumberOfInstances)
    {

    	AmazonEC2 ec2 = getEC2Client();
		int mincount = maxNumberOfInstances - 1; // create 1 instance
		int maxcount = maxNumberOfInstances;
		if(mincount == 0)
			mincount = 1;
		Collection<TagSpecification> tagSpecifications = new ArrayList<TagSpecification>();
		TagSpecification tagSpecification = new TagSpecification();
		Collection<Tag> tags = new ArrayList<Tag>();
		Tag t = new Tag();
		t.setKey("Name");
		t.setValue("App-Instance");
		tags.add(t);
		tagSpecification.setResourceType("instance");
		tagSpecification.setTags(tags);
		tagSpecifications.add(tagSpecification);
		RunInstancesRequest rir = new RunInstancesRequest(imageId, mincount, maxcount);
		rir.setInstanceType("t2.micro");
		//rir.setUserData(Common.getEC2USerData());
		rir.setTagSpecifications(tagSpecifications);
		RunInstancesResult result = null;
		try {
			result = ec2.runInstances(rir);
		} catch (AmazonEC2Exception amzEc2Exp) {
			amzEc2Exp.printStackTrace();
			return 0;
		} catch (Exception e) {
			return 0;
		}
		return 1;
    }
    public static void CreateOrStartInstance(String imageId, Integer maxNumberOfInstances)
    {
    	List<String> stoppedEC2Ids = getIdsOfStoppedInstances();
    	if (maxNumberOfInstances <= stoppedEC2Ids.size())
    	{
    		System.out.println("Starting instances");
    		for (int i=0;i<maxNumberOfInstances;i++)
    		{
    			startInstance(stoppedEC2Ids.get(i));
    		}

    	}
    	if (maxNumberOfInstances > stoppedEC2Ids.size())
    	{
    		System.out.println("Starting instances");
    		for (int i=0;i<stoppedEC2Ids.size();i++)
    		{
    			startInstance(stoppedEC2Ids.get(i));
    		}
    		int remainingCount = maxNumberOfInstances - stoppedEC2Ids.size();
    		int status = CreateInstance(imageId, remainingCount);
    	}
    	
    }
	public static void stopInstance(String instanceId) {
		AmazonEC2 ec2 = getEC2Client();
		StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instanceId);
		ec2.stopInstances(request);
	}

	public static void startInstance(String instanceId) {
		AmazonEC2 ec2 = getEC2Client();
		StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instanceId);
		ec2.startInstances(request);
	}

	public static void terminateInstance(String instanceId) {
		AmazonEC2 ec2 = getEC2Client();
		TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(instanceId);
		ec2.terminateInstances(request);
	}
	public static List<String> getIdsOfRunningInstances() {

		List<Instance> instances = getInstances();
		List<String> runningInstanceIds = new ArrayList<String>();
		for (Instance instance : instances) {
			if(instance.getState().getName().equals(InstanceStateName.Running.toString())){
				runningInstanceIds.add(instance.getInstanceId());
			}
		}
		
		return runningInstanceIds;
	}
	public static List<String> getIdsOfStoppedInstances() {

		List<Instance> instances = getInstances();
		List<String> stoppedInstanceIds = new ArrayList<String>();
		for (Instance instance : instances) {
			if(instance.getState().getName().equals(InstanceStateName.Stopped.toString())){
				stoppedInstanceIds.add(instance.getInstanceId());
			}
		}

		return stoppedInstanceIds;
	}

	private static List<Instance> getInstances() {
		AmazonEC2 ec2 = getEC2Client();
		DescribeInstancesRequest request = new DescribeInstancesRequest()
				.withFilters(new Filter("tag:Name").withValues("App-Instance"));

		DescribeInstancesResult result = ec2.describeInstances(request);
		List<Instance> resultInstances = new ArrayList<>();
		for (Reservation reservation : result.getReservations()) {
			resultInstances.addAll(reservation.getInstances());
		}
		return resultInstances;
	}

}
