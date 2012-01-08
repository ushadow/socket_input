package edu.mit.yingyin.websocket;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketHandler;

/**
 * A WebSocket server that sends input events to clients.
 * @author yingyin
 *
 */
public class InputServer extends Server {
  
  private class InputWebSocket implements WebSocket {

    private InputListener inputListener;
    
    @Override
    public void onClose(int code, String message) {
      System.out.printf("%s#onClose %d %s\n", 
                         this.getClass().getSimpleName(), code, message);
      try {
        if (inputListener != null) {
          inputListener.setStop();
          inputListener.join();
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onOpen(Connection connection) {
      System.out.printf("%s#onOpen %s\n",this.getClass().getSimpleName(),
                        connection);
      inputListener = new InputListener(connection);
      inputListener.start();
    }
  }
  
  /**
   * A thread that listens to input envets and sends them to clients.
   * @author yingyin
   *
   */
  private class InputListener extends Thread {
    private Connection connection;
    private int count = 0;
    private boolean run = true;
    
    public InputListener(Connection connection) {
      this.connection = connection;
    }
    
    @Override
    public void run() {
      while(run) {
        try {
          connection.sendMessage("input " + count++);
          Thread.sleep(1000);
        } catch (IOException e) {
          System.err.println(e.getMessage());
          return;
        } catch (InterruptedException e) {
          e.printStackTrace();
          return;
        }
      }
    }
    
    public void setStop() {
      run = false;
    }
  }
  
  SelectChannelConnector connector;
  WebSocketHandler wsHandler;
  WebSocket websocket;
  
  public InputServer(int port) {
    connector = new SelectChannelConnector();
    connector.setPort(port);
    
    addConnector(connector);
    wsHandler = new WebSocketHandler() {
      
      @Override
      public WebSocket doWebSocketConnect(HttpServletRequest request, 
                                          String protocol) {
        return new InputWebSocket();
      }
    };
    setHandler(wsHandler);
  }
  
  public static void main(String... args) {
    try {
      int port = 8081;
      InputServer server = new InputServer(port);
      server.start();
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
 