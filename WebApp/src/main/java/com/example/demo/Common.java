package com.example.demo;

import org.apache.commons.codec.binary.Base64;

import java.io.*;

import static com.example.demo.Configuration.WRITE_TO_CONSOLE;

public class Common {
    public static String getEC2USerData() {
        String userData = "";
        userData = userData + "#!/bin/bash" + "\n";
        userData = userData + "Xvfb :1 & export DISPLAY=:1" + "\n";
        userData = userData + "java -jar AppInstance.jar";
        System.out.println(userData);
        String base64UserData = null;
        try {
            base64UserData = new String( Base64.encodeBase64( userData.getBytes( "UTF-8" )), "UTF-8" );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return base64UserData;
    }
    public static String getActualFileName(String fileName){
        String[] split1 = fileName.split("\\.");
        String[] split2 = split1[0].split("_");
        //return split2[0]+"."+split1[1];
        return split2[0];
    }
    public static String getValueFromS3Object(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = reader.readLine();
        return line;
    }

    public static void printToTheRequiredStream(String input) {
        if(!WRITE_TO_CONSOLE){
            File file = new File("Output.txt");
            try {
                if (file.createNewFile()) {

                    System.out.println("File has been created.");
                }
                PrintStream o = new PrintStream(new File("Output.txt"));
                System.setOut(o);
                System.out.println(input);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else{
            System.out.println(input);
        }
    }
}
