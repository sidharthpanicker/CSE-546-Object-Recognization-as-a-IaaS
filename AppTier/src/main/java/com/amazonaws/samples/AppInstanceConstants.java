package com.amazonaws.samples;

public class AppInstanceConstants {
	public static final String GET_INSTANCE_ID_URL = "http://169.254.169.254/latest/meta-data/instance-id";
	public static final String VIDEOS_HOME = "/home/ubuntu/videos/";
	public static final String PARSE_DARKNET_RESULTS = "python /home/ubuntu/darknet/darknet_test.py";
	public static final String OBJECT_DETECTION_RESULTS_LOCATION = "/home/ubuntu/darknet/result_label";
	public static final String BUCKET_NAME = "cloud-iaas-project";
	public static final String BUCKET_REGION = "us-west-1";
	public static final String DARKNET_MODEL_HOME = "/home/ubuntu/darknet";
	public static final String WEIGHTS_PATH = "/home/ubuntu/yolov3-tiny.weights";
}
