package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static com.example.demo.Configuration.RASBERRY_PIE_URL;
import static com.example.demo.FileOperations.deleteTempFile;
import static com.example.demo.FileOperations.downloadFile;
import static com.example.demo.S3Operations.putFileInS3;
import static com.example.demo.SQSOperations.*;
import static com.example.demo.SQSOperations.sendMessageToSQSQueue;

@SpringBootApplication
@RestController
public class WebAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebAppApplication.class, args);
    }

    @RequestMapping("/hello")
    public String sayHello() {
        try {
            String fileName = downloadFile(RASBERRY_PIE_URL,".");
            int m = putFileInS3(fileName);
            if(m == 1){
                deleteTempFile(fileName);
                String queueUrl = getQueueUrl();
                sendMessageToSQSQueue(queueUrl,fileName);
                System.out.println("Queue Size is"+getSQSQueueSize());
            }else{
                System.out.println("New type of error:"+m);
            }
            //TODO
            //Query S3 with filename to return value

           /* while(getSQSQueueSize()>0){
                System.out.println("Queue Size is"+getSQSQueueSize());
                getMessagesFromSQSQueue();
            }*/


        } catch (Exception e) {
            e.printStackTrace();
            return("Failed");
        }
        return("Successfully Queued \n");
    }
}
