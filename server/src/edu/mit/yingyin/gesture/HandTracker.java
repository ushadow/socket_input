/****************************************************************************
 * * OpenNI 1.x Alpha * Copyright (C) 2011 PrimeSense Ltd. * * This file is part
 * of OpenNI. * * OpenNI is free software: you can redistribute it and/or modify
 * * it under the terms of the GNU Lesser General Public License as published *
 * by the Free Software Foundation, either version 3 of the License, or * (at
 * your option) any later version. * * OpenNI is distributed in the hope that it
 * will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the * GNU
 * Lesser General Public License for more details. * * You should have received
 * a copy of the GNU Lesser General Public License * along with OpenNI. If not,
 * see <http://www.gnu.org/licenses/>. * *
 ****************************************************************************/
package edu.mit.yingyin.gesture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.OpenNI.ActiveHandEventArgs;
import org.OpenNI.CalibrationProgressEventArgs;
import org.OpenNI.CalibrationProgressStatus;
import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMetaData;
import org.OpenNI.GeneralException;
import org.OpenNI.GestureGenerator;
import org.OpenNI.GestureRecognizedEventArgs;
import org.OpenNI.HandsGenerator;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.InactiveHandEventArgs;
import org.OpenNI.OutArg;
import org.OpenNI.Point3D;
import org.OpenNI.PoseDetectionCapability;
import org.OpenNI.PoseDetectionEventArgs;
import org.OpenNI.SceneMetaData;
import org.OpenNI.ScriptNode;
import org.OpenNI.SkeletonCapability;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.SkeletonProfile;
import org.OpenNI.StatusException;
import org.OpenNI.UserEventArgs;
import org.OpenNI.UserGenerator;

public class HandTracker {

  private class GestureRecognizedObserver implements
      IObserver<GestureRecognizedEventArgs> {

    @Override
    public void update(IObservable<GestureRecognizedEventArgs> observable,
        GestureRecognizedEventArgs args) {
      try {
        handsGen.StartTracking(args.getEndPosition());
        gestureGen.removeGesture("Wave");
      } catch (StatusException se) {
        se.printStackTrace();
      }
    }
  }

  private class HandCreateEventObserver implements
      IObserver<ActiveHandEventArgs> {

    @Override
    public void update(IObservable<ActiveHandEventArgs> observable,
        ActiveHandEventArgs args) {
      Point3D pos = args.getPosition();
      if (!history.containsKey(args.getId())) {
        ArrayList<Point3D> newList = new ArrayList<Point3D>();
        newList.add(pos);
        history.put(args.getId(), newList);
      }
    }
  }

  private class HandUpdateEventObserver implements
      IObserver<ActiveHandEventArgs> {

    @Override
    public void update(IObservable<ActiveHandEventArgs> observable,
        ActiveHandEventArgs args) {
      ArrayList<Point3D> historyList = history.get(args.getId());
      if (historyList != null) {
        if (historyList.size() >= HISTORY_SIZE)
          historyList.remove(0);
        historyList.add(args.getPosition());
      }
    }
  }

  private class HandDestroyEventObserver implements
      IObserver<InactiveHandEventArgs> {

    @Override
    public void update(IObservable<InactiveHandEventArgs> observable,
        InactiveHandEventArgs args) {
      System.out.printf("Lost hand: %d\n", args.getId());
      history.remove(args.getId());
      if (history.isEmpty()) {
        try {
          gestureGen.addGesture("Wave");
        } catch (StatusException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private class NewUserObserver implements IObserver<UserEventArgs> {
    @Override
    public void update(IObservable<UserEventArgs> observable, 
        UserEventArgs args) {
      System.out.println("New user " + args.getId());
      try {
        if (skeletonCap.needPoseForCalibration()) {
          poseDetectionCap.startPoseDetection(calibPose, args.getId());
        } else {
          skeletonCap.requestSkeletonCalibration(args.getId(), true);
        }
      } catch (StatusException e) {
        e.printStackTrace();
      }
    }
  }

  private class LostUserObserver implements IObserver<UserEventArgs> {
    @Override
    public void update(IObservable<UserEventArgs> observable, 
        UserEventArgs args) {
      System.out.println("Lost user " + args.getId());
      joints.remove(args.getId());
    }
  }

  private class CalibrationCompleteObserver implements
      IObserver<CalibrationProgressEventArgs> {
    @Override
    public void update(IObservable<CalibrationProgressEventArgs> observable,
        CalibrationProgressEventArgs args) {
      System.out.println("Calibraion complete: " + args.getStatus());
      try {
        if (args.getStatus() == CalibrationProgressStatus.OK) {
          System.out.println("starting tracking " + args.getUser());
          skeletonCap.startTracking(args.getUser());
          joints.put(new Integer(args.getUser()),
              new HashMap<SkeletonJoint, SkeletonJointPosition>());
        } else if (args.getStatus() != CalibrationProgressStatus.MANUAL_ABORT) {
          if (skeletonCap.needPoseForCalibration()) {
            poseDetectionCap.startPoseDetection(calibPose, args.getUser());
          } else {
            skeletonCap.requestSkeletonCalibration(args.getUser(), true);
          }
        }
      } catch (StatusException e) {
        e.printStackTrace();
      }
    }
  }

  private class PoseDetectedObserver implements
      IObserver<PoseDetectionEventArgs> {
    @Override
    public void update(IObservable<PoseDetectionEventArgs> observable,
        PoseDetectionEventArgs args) {
      System.out.println("Pose " + args.getPose() + " detected for "
          + args.getUser());
      try {
        poseDetectionCap.stopPoseDetection(args.getUser());
        skeletonCap.requestSkeletonCalibration(args.getUser(), true);
      } catch (StatusException e) {
        e.printStackTrace();
      }
    }
  }

  public DepthGenerator depthGen;
  public UserGenerator userGen;
  public SkeletonCapability skeletonCap;
  // TODO(ushadow): add synchronization.
  public HashMap<Integer, ArrayList<Point3D>> history;
  public String calibPose = null;

  private OutArg<ScriptNode> scriptNode;
  private Context context;
  private PoseDetectionCapability poseDetectionCap;
  private float[] histogram;
  private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints;

  private GestureGenerator gestureGen;
  private HandsGenerator handsGen;
  private ByteBuffer sceneBuffer, depthBuffer;
  private int depthByteBufferSize, sceneByteBufferSize;
  private DepthMetaData depthMD;
  private SceneMetaData sceneMD;
  private static final int HISTORY_SIZE = 10;
  private int width, height;
  private ShortBuffer depth, scene;

  private final String SAMPLE_XML_FILE = "server/config/config.xml";

  public HandTracker() {
    try {
      scriptNode = new OutArg<ScriptNode>();
      context = Context.createFromXmlFile(SAMPLE_XML_FILE, scriptNode);

      depthGen = DepthGenerator.create(context);
      depthMD = depthGen.getMetaData();

      histogram = new float[10000];
      width = depthMD.getFullXRes();
      height = depthMD.getFullYRes();
      depthByteBufferSize = width * height
          * depthMD.getData().getBytesPerPixel();
      depthBuffer = ByteBuffer.allocateDirect(depthByteBufferSize);
      depthBuffer.order(ByteOrder.LITTLE_ENDIAN);

      userGen = UserGenerator.create(context);
      skeletonCap = userGen.getSkeletonCapability();
      poseDetectionCap = userGen.getPoseDetectionCapability();

      userGen.getNewUserEvent().addObserver(new NewUserObserver());
      userGen.getLostUserEvent().addObserver(new LostUserObserver());
      skeletonCap.getCalibrationCompleteEvent().addObserver(
          new CalibrationCompleteObserver());
      poseDetectionCap.getPoseDetectedEvent().addObserver(
          new PoseDetectedObserver());

      calibPose = skeletonCap.getSkeletonCalibrationPose();
      joints = new HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>>();

      skeletonCap.setSkeletonProfile(SkeletonProfile.ALL);

      gestureGen = GestureGenerator.create(context);
      gestureGen.getGestureRecognizedEvent().addObserver(
          new GestureRecognizedObserver());

      handsGen = HandsGenerator.create(context);
      handsGen.getHandCreateEvent().addObserver(new HandCreateEventObserver());
      handsGen.getHandUpdateEvent().addObserver(new HandUpdateEventObserver());
      handsGen.getHandDestroyEvent().addObserver(new HandDestroyEventObserver());

      history = new HashMap<Integer, ArrayList<Point3D>>();

      sceneMD = userGen.getUserPixels(0);
      int sceneWidth = sceneMD.getFullXRes();
      int sceneHeight = sceneMD.getFullYRes();
      sceneByteBufferSize = sceneWidth * sceneHeight
          * sceneMD.getData().getBytesPerPixel();
      sceneBuffer = ByteBuffer.allocateDirect(sceneByteBufferSize);
      sceneBuffer.order(ByteOrder.LITTLE_ENDIAN);

      context.startGeneratingAll();
      gestureGen.addGesture("Wave");
    } catch (GeneralException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public int depthHeight() {
    return height;
  }

  public int depthWidth() {
    return width;
  }

  public void addGestureRecognizedEventObserver(
      IObserver<GestureRecognizedEventArgs> observer) throws StatusException {
    gestureGen.getGestureRecognizedEvent().addObserver(observer);
  }
  
  public void addHandCreateEventObserver(
      IObserver<ActiveHandEventArgs> observer) throws StatusException {
    handsGen.getHandCreateEvent().addObserver(observer);
  }
  
  public void addHandUpdateEventObserver(
      IObserver<ActiveHandEventArgs> observer) throws StatusException {
    handsGen.getHandUpdateEvent().addObserver(observer);
  }
  
  public void deleteGestureRecognizedEventObserver(
      IObserver<GestureRecognizedEventArgs> observer) {
    gestureGen.getGestureRecognizedEvent().deleteObserver(observer);
  }
  
  public void deleteHandCreateEventObserver(
      IObserver<ActiveHandEventArgs> observer) {
    handsGen.getHandCreateEvent().deleteObserver(observer);
  }


  public void updateDepth() {
    try {
      context.waitAnyUpdateAll();

      sceneMD.getData().copyToBuffer(sceneBuffer, sceneByteBufferSize);
      scene = sceneBuffer.asShortBuffer();

      depthMD.getData().copyToBuffer(depthBuffer, depthByteBufferSize);
      depth = depthBuffer.asShortBuffer();

      calcHist(depth);

    } catch (GeneralException e) {
      e.printStackTrace();
    }
  }

  public float[] histogram() {
    return histogram;
  }

  public ShortBuffer depthBuffer() {
    return depth;
  }

  public ShortBuffer sceneBuffer() {
    return scene;
  }

  public HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> getJoints(
      int user) throws StatusException {
    getJoint(user, SkeletonJoint.HEAD);
    getJoint(user, SkeletonJoint.NECK);

    getJoint(user, SkeletonJoint.LEFT_SHOULDER);
    getJoint(user, SkeletonJoint.LEFT_ELBOW);
    getJoint(user, SkeletonJoint.LEFT_HAND);

    getJoint(user, SkeletonJoint.RIGHT_SHOULDER);
    getJoint(user, SkeletonJoint.RIGHT_ELBOW);
    getJoint(user, SkeletonJoint.RIGHT_HAND);

    getJoint(user, SkeletonJoint.TORSO);

    getJoint(user, SkeletonJoint.LEFT_HIP);
    getJoint(user, SkeletonJoint.LEFT_KNEE);
    getJoint(user, SkeletonJoint.LEFT_FOOT);

    getJoint(user, SkeletonJoint.RIGHT_HIP);
    getJoint(user, SkeletonJoint.RIGHT_KNEE);
    getJoint(user, SkeletonJoint.RIGHT_FOOT);
    return joints;
  }

  public void release() {
    System.out.println("HandTracker released.");
    context.release();
  }
  
  public Point3D convertRealWorldToProjective(Point3D p) 
      throws StatusException {
    return depthGen.convertRealWorldToProjective(p);
  }
  
  private void calcHist(ShortBuffer depth) {
    // reset
    for (int i = 0; i < histogram.length; ++i)
      histogram[i] = 0;

    depth.rewind();

    int points = 0;
    while (depth.remaining() > 0) {
      short depthVal = depth.get();
      if (depthVal != 0) {
        histogram[depthVal]++;
        points++;
      }
    }

    for (int i = 1; i < histogram.length; i++) {
      histogram[i] += histogram[i - 1];
    }

    if (points > 0) {
      for (int i = 1; i < histogram.length; i++) {
        histogram[i] = 1.0f - (histogram[i] / (float) points);
      }
    }
  }

  private void getJoint(int user, SkeletonJoint joint) throws StatusException {
    SkeletonJointPosition pos = skeletonCap.getSkeletonJointPosition(user,
        joint);
    if (pos.getPosition().getZ() != 0) {
      joints.get(user).put(
          joint,
          new SkeletonJointPosition(
              depthGen.convertRealWorldToProjective(pos.getPosition()),
              pos.getConfidence()));
    } else {
      joints.get(user).put(joint, new SkeletonJointPosition(new Point3D(), 0));
    }
  }
}
