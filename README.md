Experiment with browser input from Kinect using websocket.

# Dependencies

## Server

### Java libraries

* [jetty](http://download.eclipse.org/jetty/stable-8/dist/)
    
    * jetty-all-8.0.4.v20111024.jar
    * servlet-api-3.0.jar
    
# Setup

## Start up the web server.

For reloading the application every time a new request comes in during 
development:

		bundle exec shotgun config.ru

		nohup bundle exec unicorn config.ru > web.log &