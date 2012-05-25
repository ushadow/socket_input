Experiment with browser input from Kinect using websocket.

# Development Environment
The code is developed and tested on Ubuntu 11.10.

# Dependencies

## Server

### Java libraries

* Put the following [jetty](http://download.eclipse.org/jetty/stable-8/dist/) 
jar files to server/lib
    
    * jetty-all-8.0.4.v20111024.jar
    * servlet-api-3.0.jar
    
* Install [OpenNI unstable branch](https://github.com/OpenNI/OpenNI/tree/unstable)

* Install [avin2/SensorKinect unstable branch](https://github.com/avin2/SensorKinect)

* Install NITE Dev Linux x64 v1.5.2.21

# Setup

## Start up the web server.

For reloading the application every time a new request comes in during 
development:

		bundle exec shotgun config.ru

		nohup bundle exec unicorn config.ru > web.log &