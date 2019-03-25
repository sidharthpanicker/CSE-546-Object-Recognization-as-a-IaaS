package com.amazonaws.samples;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.BufferedReader;
import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;//


public class AppInstanceDriver {
	public static BasicAWSCredentials creds = new BasicAWSCredentials("AKIAI7LTFYPHDGK7NSUA", "KJTIIlli57/FfxqDEupe4Y9BMp0q72TbT0EX9+14");
    public static AmazonS3 s3Client = AmazonS3Client.builder()
    	    .withRegion(AppInstanceConstants.BUCKET_REGION)
    	    .withCredentials(new AWSStaticCredentialsProvider(creds))
    	    .build();
    
	public static void main(String args[]) throws IOException, InterruptedException {
		
		
	    String instanceId = retrieveInstanceId();
	    System.out.println("INSTANCE ID:"+instanceId);
		String bucket_name = AppInstanceConstants.BUCKET_NAME;
		
		while(true) {
			String videoName = null;
			if(checkIfKeyExists(bucket_name,instanceId)) {
				videoName=getValueFromKey(instanceId);
				System.out.println("VIDEO NAME FOR INSTANCE ID: "+videoName);
				File file = getVideo(bucket_name, videoName);
				System.out.println("VIDEO FILE NAME FOR INSTANCE ID: "+file.getName());
				runDarknetModel(bucket_name, file);
				deleteFromS3(bucket_name, instanceId);
			}
		}
	}
		public static String getValueFromKey(String key) throws IOException {
        String result = "Not Found";
            
            if(existsInS3(key)){
                S3Object fullObject = null;
                fullObject = s3Client.getObject(new GetObjectRequest(AppInstanceConstants.BUCKET_NAME, key));
                if(fullObject != null){
                    result =  getValueFromS3Object(fullObject.getObjectContent());
                }
            }
        return result;
    }
	public static boolean existsInS3(String instanceId)
    {
        boolean result = false;
        try{
            
            S3Object fullObject = null;
            fullObject = s3Client.getObject(new GetObjectRequest(AppInstanceConstants.BUCKET_NAME, instanceId));
            result = true;
        }catch (AmazonServiceException e) {
            String errorCode = e.getErrorCode();
            if (!errorCode.equals("NoSuchKey")) {
                result = false;
            }
        }
        return result;
    }
	
	public static String getValueFromS3Object(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = reader.readLine();
        return line;
    }
	
	public static File getVideo(String bucket_name,String videoFileName) {
		String filePath = AppInstanceConstants.VIDEOS_HOME+videoFileName;
		File localFile = new File(filePath);

		ObjectMetadata object = s3Client.getObject(new GetObjectRequest(bucket_name, videoFileName), localFile);
		return localFile;
	}	
		public static boolean checkIfKeyExists(String bucketName, String key) {

		    try {
		    	s3Client.getObjectMetadata(bucketName, key); 
		    } catch(AmazonServiceException e) {
		        return false;
		    }
		    return true;
		}
		
		public static void deleteFromS3(String bucketName, String objectKey) {
			try {
				s3Client.deleteObject(bucketName, objectKey);
	        } catch (AmazonServiceException e) {
	            System.err.println(e.getErrorMessage());
	            System.exit(1);
	        }
			
		}
		
		public static void runDarknetModel(String bucket_name, File video) throws InterruptedException, IOException {
			
			String videoPath = AppInstanceConstants.VIDEOS_HOME+video.getName();
		
			String command = "./darknet detector demo cfg/coco.data cfg/yolov3-tiny.cfg "+AppInstanceConstants.WEIGHTS_PATH+" "+ videoPath+" -dont_show > result";
			String response = LinuxInteractor.executeShellCommand(command, true);
			System.out.println("Darknet model output:\n"+response);
			LinuxInteractor.executeCommand(AppInstanceConstants.PARSE_DARKNET_RESULTS);
			String result = new String ( Files.readAllBytes( Paths.get(AppInstanceConstants.OBJECT_DETECTION_RESULTS_LOCATION) ) );
			String key = video.getName().split("_")[0];
			s3Client.putObject(bucket_name, key, result);
			LinuxInteractor.executeCommand("rm "+videoPath);

		}
		public static String retrieveInstanceId() throws IOException {
		    String EC2Id = null;
		    String inputLine;
		    URL EC2MetaData = new URL(AppInstanceConstants.GET_INSTANCE_ID_URL);
		    URLConnection EC2MD = EC2MetaData.openConnection();
		    BufferedReader in = new BufferedReader(new InputStreamReader(EC2MD.getInputStream()));
		    while ((inputLine = in.readLine()) != null) {
		        EC2Id = inputLine;
		    }
		    in.close();
		    return EC2Id;
		}

}
