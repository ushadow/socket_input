package edu.mit.yingyin.websocket;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

/**
 * A WebSocket server that listens to input events and sends them to clients.
 * @author yingyin
 *
 */
public class InputServer extends Server {
  
  private static Logger logger = Logger.getLogger(InputServer.class.getName());
  
  private class InputWebSocket implements WebSocket {

    @Override
    public void onClose(int code, String message) {
      logger.info(String.format("Connection closed with code: %d, message: %s\n", 
          code, message));
      if (inputListener != null)
        inputListener.stopListening();
    }

    /**
     * A new connection is opened.
     */
    @Override
    public void onOpen(Connection connection) {
      logger.info(String.format("Connection opened: %s\n", connection));
      inputListener.startListening(connection);
    }
  }
  
  /**
   * This connector uses efficient NIO buffers with a non blocking threading 
   * model.
   */
  private SelectChannelConnector connector;
  private WebSocketHandler wsHandler;
  private IInputListener inputListener;
  
  /**
   * 
   * @param port port this sever listens to.
   * @param tracker
   */
  public InputServer(int port, IInputListener inputListener) {
    connector = new SelectChannelConnector();
    connector.setPort(port);
    this.inputListener = inputListener;
    
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
      IInputListener inputListener = new HandInputListener(controller);
      InputServer server = new InputServer(port, inputListener);
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
 