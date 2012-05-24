package edu.mit.yingyin.websocket;

import org.eclipse.jetty.websocket.WebSocket.Connection;

public interface IInputListener {
  public void startListening(Connection c);
  public void stopListening();
}
