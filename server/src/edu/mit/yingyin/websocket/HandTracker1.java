package edu.mit.yingyin.websocket;

import java.util.ArrayList;
import java.util.List;

import org.OpenNI.ActiveHandEventArgs;
import org.OpenNI.Context;
import org.OpenNI.GeneralException;
import org.OpenNI.GestureGenerator;
import org.OpenNI.GestureRecognizedEventArgs;
import org.OpenNI.HandsGenerator;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.InactiveHandEventArgs;
import org.OpenNI.OutArg;
import org.OpenNI.ScriptNode;
import org.OpenNI.StatusException;

public class HandTracker1 {
  
  public static interface HandEventListener {
    public void gesturePerformed(GestureRecognizedEventArgs args);
  }
  
  private class GestureObserver implements IObserver<GestureRecognizedEventArgs>
  {
    @Override
    public void update(IObservable<GestureRecognizedEventArgs> observable,
                       GestureRecognizedEventArgs args) {
      try {
        handsGen.StartTracking(args.getEndPosition());
        System.out.println("Waved");
        for (HandEventListener listener : listeners)
          listener.gesturePerformed(args);
      } catch (StatusException e) {
        e.printStackTrace();
      }
    }
  }
  
  class HandCreateEventObserver implements IObserver<ActiveHandEventArgs> {
    public void update(IObservable<ActiveHandEventArgs> observable,
                       ActiveHandEventArgs args) {
      System.out.println("Waved");
    }
  }
  class HandUpdateEventObserver implements IObserver<ActiveHandEventArgs> {
    public void update(IObservable<ActiveHandEventArgs> observable,
                       ActiveHandEventArgs args) {
      System.out.println("Waved");
    }
  }
  class HandDestroyEventObserver implements IObserver<InactiveHandEventArgs> {
    public void update(IObservable<InactiveHandEventArgs> observable,
                InactiveHandEventArgs args) {
      System.out.println("Waved");
    }
  }

  private static final String CONFIG_FILE = "server/config/config.xml";
  private OutArg<ScriptNode> scriptNode;
  private Context context;
  private GestureGenerator gestureGen;
  private HandsGenerator handsGen;
  private List<HandEventListener> listeners;
  
  public HandTracker1() {
    try {
      scriptNode = new OutArg<ScriptNode>();
      context = Context.createFromXmlFile(CONFIG_FILE, scriptNode);
      gestureGen = GestureGenerator.create(context);
      gestureGen.addGesture("Wave");
      gestureGen.getGestureRecognizedEvent().addObserver(new GestureObserver());
      
      handsGen = HandsGenerator.create(context);
      handsGen.getHandCreateEvent().addObserver(new HandCreateEventObserver());
      handsGen.getHandUpdateEvent().addObserver(new HandUpdateEventObserver());
      handsGen.getHandDestroyEvent().addObserver(
          new HandDestroyEventObserver());
      
      listeners = new ArrayList<HandEventListener>();
      
    } catch (GeneralException ge) {
      ge.printStackTrace();
      System.exit(-1);
    }
  }
  
  public void addListener(HandEventListener listener) {
    listeners.add(listener);
  }
  
  public void removeListener(HandEventListener listener) {
    listeners.remove(listener);
  }
  
  public void start() {
    System.out.println("Tracker started...");
    try {
      context.startGeneratingAll();
    } catch (StatusException e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
  
  public static void main(String[] args) {
    HandTracker1 t = new HandTracker1();
    t.start();
    while (true){}
  }
}
