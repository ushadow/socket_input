package edu.mit.yingyin.websocket;

import java.io.IOException;

import org.OpenNI.ActiveHandEventArgs;
import org.OpenNI.GestureRecognizedEventArgs;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.Point3D;
import org.OpenNI.StatusException;
import org.eclipse.jetty.websocket.WebSocket.Connection;

public class HandInputListener implements IInputListener {
  private Connection connection;
  private GestureObserver gestureRecogObserver = new GestureObserver();
  private HandCreateObserver handCreateObserver = new HandCreateObserver();
  private HandUpdateObserver handUpdateObserer = new HandUpdateObserver();
  private HandTrackerController controller;
  
  public HandInputListener(HandTrackerController controller) {
    this.controller = controller;
  }
  
  public void startListening(Connection connection) {
    this.connection = connection;
    try {
      controller.addGestureRecognizedEventObserver(gestureRecogObserver);
      controller.addHandCreateEventObserver(handCreateObserver);
      controller.addHandUpdateEventObserver(handUpdateObserer);
    } catch (StatusException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public void stopListening() {
    controller.deleteGestureRecognizedEventObserver(gestureRecogObserver);
    controller.deleteHandCreateEventObserver(handCreateObserver);
  }

  private class GestureObserver implements
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
  
  private class HandCreateObserver implements 
      IObserver<ActiveHandEventArgs> {

    @Override
    public void update(IObservable<ActiveHandEventArgs> observable,
        ActiveHandEventArgs args) {
      Point3D pos = args.getPosition();
      try {
        pos = controller.convertRealWorldToProjective(pos);
        String message = String.format("hand_created,%d,%d,%d", 
            (int)pos.getX(), (int)pos.getY(), (int)pos.getZ());
        connection.sendMessage(message);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      } catch (StatusException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  private class HandUpdateObserver implements 
  IObserver<ActiveHandEventArgs> {
    
    @Override
    public void update(IObservable<ActiveHandEventArgs> observable,
        ActiveHandEventArgs args) {
      
      Point3D pos = args.getPosition();
      try {  
        pos = controller.convertRealWorldToProjective(pos);
        String message = String.format("hand_created,%d,%d,%d", 
            (int)pos.getX(), (int)pos.getY(), (int)pos.getZ());
        connection.sendMessage(message);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      } catch (StatusException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}