package com.example.demo;

import com.amazonaws.regions.Regions;

public  class Configuration {
    public static final String BUCKET_NAME = "cloud-sprng-19";
    public static final String RASBERRY_PIE_URL = "http://206.207.50.7/getvideo";
    public static final String ACCESS_KEY = "AKIAIMDJVS6GVNISDHRA";
    public static final String SECRET_KEY = "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";
    public static final Regions REGION = Regions.US_WEST_1;
    public static final String QUEUE_NAME = "cloud-computing.fifo";
    public static final int LOAD_BALANCER_TIMEOUT = 30;
    public static final int WEB_APP_TIMEOUT = 5;
    public static final int MINIMUM_NO_OF_INSTANCES = 2;
    public static final int MAXIMUM_NO_OF_INSTANCES = 19;
    public static final String IMAGE_ID = "ami-0e355297545de2f82";
}
