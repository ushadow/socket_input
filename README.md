A self-contained demostration of using hand as browser input using Kinect and websocket.

# Development Environment
The code is developed and tested on Ubuntu 11.10.

# Dependencies

## Server

### Java libraries

* Put the following [jetty](http://download.eclipse.org/jetty/stable-8/dist/) 
jar files to server/lib
    
    * jetty-http-8.1.5.v20120716.jar
    * jetty-io-8.1.5.v20120716.jar
    * jetty-server-8.1.5.v20120716.jar
    * jetty-util-8.1.5.v20120716.jar
    * jetty-websocket-8.1.5.v20120716.jar
    * jetty-continuation-8.1.5.v20120716.jar
    * servlet-api-3.0.jar
    
* Install [OpenNI unstable branch](https://github.com/OpenNI/OpenNI/tree/unstable)

	This will install org.OpenNI.jar to /usr/share/java.

### Kinect driver
* Install [avin2/SensorKinect unstable branch](https://github.com/avin2/SensorKinect)

### OpenNI middleware
* Install NITE Dev Linux x64 v1.5.2.21 (unstable version)

	I removed the Features_1_5_2 folder so that libXnVFeatures_1_5_2.so is not installed. With is library installed,
	the sample program org.OpenNI.Samples.UserTracker crashes. The C++ version of the user tracking program runs
	fine though. I reported this bug in the [OpneNI Google group] (https://groups.google.com/d/msg/openni-dev/259RT7dVSy4/jK0YSikT5NEJ).

# Setup

## Start up the web server.

For reloading the application every time a new request comes in during 
development:

		bundle exec shotgun config.ru

		nohup bundle exec unicorn config.ru > web.log &