package edu.mit.yingyin.websocket;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.OpenNI.GestureRecognizedEventArgs;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketHandler;

//import edu.mit.yingyin.websocket.HandTracker.HandEventListener;

/**
 * A WebSocket server that sends input events to clients.
 * @author yingyin
 *
 */
public class InputServer extends Server {
  
  private class InputWebSocket implements WebSocket {

//    private InputListener inputListener;
    
    @Override
    public void onClose(int code, String message) {
      System.out.printf("%s#onClose %d %s\n", 
                         this.getClass().getSimpleName(), code, message);
//      handTracker.removeListener(inputListener);
    }

    /**
     * A new connection is opened.
     */
    @Override
    public void onOpen(Connection connection) {
      System.out.printf("%s#onOpen %s\n",this.getClass().getSimpleName(),
                        connection);
//      inputListener = new InputListener(connection);
//      handTracker.addListener(inputListener);
    }
  }
  
//  /**
//   * A thread that listens to input envets and sends them to clients.
//   * @author yingyin
//   *
//   */
//  private class InputListener implements HandEventListener {
//    private Connection connection;
//    private int count = 0;
//    
//    public InputListener(Connection connection) {
//      this.connection = connection;
//    }
//    
//
//    @Override
//    public void gesturePerformed(GestureRecognizedEventArgs args) {
//      try {
//        connection.sendMessage("Click detected: " + count++);
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    }
//  }
  
  /**
   * This connector uses efficient NIO buffers with a non blocking threading 
   * model.
   */
  private SelectChannelConnector connector;
  private WebSocketHandler wsHandler;
//  private HandTracker1 handTracker = new HandTracker1();
  
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
//    handTracker.start();
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
 