package com.example.demo;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.File;

public class S3Operations {
    public static int putFileInS3(String fileName){
        int result = 0;
        System.out.println("putFileInS3 started with file Name:" + fileName);
        AmazonS3 s3client = AWSClientGenerator.getS3Client();
        try{
            PutObjectResult tmp = s3client.putObject(
                    Configuration.BUCKET_NAME,
                    fileName,
                    new File("."+File.separator + fileName)
            );
            System.out.println("Completed Uploading");
            result = 1;
        }catch(AmazonS3Exception e){
            String CONTENT_MD5 = "Content-MD5";
            System.out.println("Error in:" + fileName);
            if(e.getMessage().contains(CONTENT_MD5)){
                System.out.println("Duplicate File Found");
                result = 2;
            }else{
                result = 0;
            }
            e.printStackTrace();

        }
        catch(Exception e){
            System.out.println("Error in:" + fileName);
            e.printStackTrace();
            result = 0;
        }
        return result;
    }

    /*public static getResultForInputFromS3(String){

    }*/
}