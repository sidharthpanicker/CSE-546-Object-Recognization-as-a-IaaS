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
            putFileInS3(fileName);
            deleteTempFile(fileName);
            String queueUrl = getQueueUrl();
            sendMessageToSQSQueue(queueUrl,fileName);

        } catch (IOException e) {
            e.printStackTrace();
            return("Failed");
        }
        return("Successfully Queued");
    }
}
