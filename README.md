# CSE-546-Object-Recognization-as-a-IaaS
Contributors:
Chandana Manjunath Ningappa,
Namitha Sudheendra,
Sidharth Panicker

Running Instructions:
Build the WebApp project, copy the jar to the Web Instance, and run it using the command java -jar <JAR NAME> . While building the jar make sure that the main class should be selected as WebApplication.
The functionality of an AppInstance is captured in the AMI ami-0ddb68049885e82e7 which has been made public. As per the request traffic, the load balancer creates App Instances using this image and launches the instance. The functionality of the AppInstance is invoked during the launch of the instance.

Credentials for Running the application:
Request URL: http://13.52.144.193:8080/runmodel

ACCESS_KEY = AKIAI7LTFYPHDGK7NSUA

SECRET_KEY = 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

S3 Bucket Name = cloud-iaas-project
