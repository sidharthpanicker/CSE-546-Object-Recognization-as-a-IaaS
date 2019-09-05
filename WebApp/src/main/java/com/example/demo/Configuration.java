package com.example.demo;

import com.amazonaws.regions.Regions;

public  class Configuration {



    //Chandana
    public static final String BUCKET_NAME = "cloud-iaas-project";
    public static final String ACCESS_KEY = "";
    public static final String SECRET_KEY = "";

    public static final String RASBERRY_PIE_URL = "http://206.207.50.7/getvideo";

    public static final Regions REGION = Regions.US_WEST_1;
    public static final String QUEUE_NAME = "cloud-computing.fifo";
    public static final int LOAD_BALANCER_TIMEOUT = 20;
    public static final int WEB_APP_TIMEOUT = 5;
    public static final int MINIMUM_NO_OF_INSTANCES = 2;
    public static final int MAXIMUM_NO_OF_INSTANCES = 19;
    public static final String IMAGE_ID = "ami-0ddb68049885e82e7";
    public static final Boolean WRITE_TO_CONSOLE = true;
    //public static final String IMAGE_ID = "ami-0a8da0772a5f43939";
}
