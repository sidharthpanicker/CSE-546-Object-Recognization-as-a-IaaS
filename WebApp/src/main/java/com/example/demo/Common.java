package com.example.demo;

import org.apache.commons.codec.binary.Base64;

import java.io.*;

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
}
