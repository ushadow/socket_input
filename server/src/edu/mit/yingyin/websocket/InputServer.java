package edu.mit.yingyin.websocket;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.OpenNI.GestureRecognizedEventArgs;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.StatusException;
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

    private InputListener inputListener;
    
    @Override
    public void onClose(int code, String message) {
      System.out.printf("%s#onClose %d %s\n", 
                         this.getClass().getSimpleName(), code, message);
      if (inputListener != null)
        inputListener.deleteObservers();
    }

    /**
     * A new connection is opened.
     */
    @Override
    public void onOpen(Connection connection) {
      System.out.printf("%s#onOpen %s\n",this.getClass().getSimpleName(),
                        connection);
      inputListener = new InputListener(connection);
    }
  }
  
  private class InputListener {
    private Connection connection;
    private GestureRecognizedObserver gestureRecogObserver = 
        new GestureRecognizedObserver();
    
    public InputListener(Connection connection) {
      this.connection = connection;
      try {
        trackerController.addObserver(gestureRecogObserver);
      } catch (StatusException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    public void deleteObservers() {
      trackerController.deleteObserver(gestureRecogObserver);
    }

    private class GestureRecognizedObserver implements
    IObserver<GestureRecognizedEventArgs> {

      @Override
      public void update(IObservable<GestureRecognizedEventArgs> observable,
          GestureRecognizedEventArgs args) {
        try {
          connection.sendMessage("Gesture recognized:" + args.getGesture());
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }
  
  /**
   * This connector uses efficient NIO buffers with a non blocking threading 
   * model.
   */
  private SelectChannelConnector connector;
  private WebSocketHandler wsHandler;
  private HandTrackerController trackerController;
  
  public InputServer(int port, HandTrackerController trackerController) {
    this.trackerController = trackerController;
    
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
      HandTrackerController controller = new HandTrackerController();
      InputServer server = new InputServer(port, controller);
      controller.start();
      server.start();
      server.join();
      controller.join();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
 