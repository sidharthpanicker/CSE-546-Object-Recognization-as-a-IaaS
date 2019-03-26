package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

import static com.example.demo.AutoScaler.ScaleInOut;
import static com.example.demo.Common.getActualFileName;
import static com.example.demo.Common.getEC2USerData;
import static com.example.demo.Configuration.*;
import static com.example.demo.FileOperations.deleteTempFile;
import static com.example.demo.FileOperations.downloadFile;
import static com.example.demo.S3Operations.*;
import static com.example.demo.SQSOperations.*;
import static com.example.demo.SQSOperations.sendMessageToSQSQueue;

@SpringBootApplication
@RestController
public class WebAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebAppApplication.class, args);
        runLoadBalancer();
        //getEC2USerData();

    }

    @RequestMapping("/runmodel")
    public String runModel() {
        try {
            String fileName = downloadFile(RASBERRY_PIE_URL,".");
            int m = putFileInS3(fileName);
            if(m == 1){
                deleteTempFile(fileName);
                String queueUrl = getQueueUrl();
                sendMessageToSQSQueue(queueUrl,fileName);
                fileName = getActualFileName(fileName);
                while(!existsInS3(fileName)){
                    try {
                        TimeUnit.SECONDS.sleep(WEB_APP_TIMEOUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                String result = getValueFromKey(fileName);
                return "("+fileName+","+result+")";

            }else{
                System.out.println("New type of error:"+m);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return("Failed");
        }
        return("Successfully Queued \n");
    }
    public static void runLoadBalancer(){
        int i = 1;
        while(i>0){
            try {
                ScaleInOut();
                TimeUnit.SECONDS.sleep(LOAD_BALANCER_TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
