package com.example.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import static com.example.demo.Common.printToTheRequiredStream;


public class FileOperations {
    private static final int BUFFER_SIZE = 4096;

    public static void deleteTempFile(String fileName) {
        File file = new File(fileName);

        if(file.delete())
        {
            printToTheRequiredStream("File deleted successfully");
        }
        else
        {
            printToTheRequiredStream("Failed to delete the file");
        }
    }


    public static String downloadFile(String fileURL, String saveDir)
            throws IOException {
        String fileName = "";
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String dpsn = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            if (dpsn != null) {
                // extracts file name from header field
                int index = dpsn.indexOf("filename=");
                if (index > 0) {
                    fileName = dpsn.replaceFirst("(?i)^.*filename=\"?([^\"]+)\"?.*$", "$1");
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }

            InputStream inputStream = httpConn.getInputStream();
            String[] split = fileName.split("\\.");

            fileName = split[0]+ "_"+ new Random().nextInt(1000)+ "."+split[1];
            String saveFilePath = saveDir + File.separator + fileName;

            // opens an output stream to save into file
            FileOutputStream outStrm = new FileOutputStream(saveFilePath);

            int bytesRead = -1;
            byte[] bf = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(bf)) != -1) {
                outStrm.write(bf, 0, bytesRead);
            }

            outStrm.close();
            inputStream.close();

            printToTheRequiredStream("File downloaded");
        } else {
            printToTheRequiredStream("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
        return fileName;
    }
}
