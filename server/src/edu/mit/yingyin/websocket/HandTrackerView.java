package edu.mit.yingyin.websocket;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.StatusException;

public class HandTrackerView extends JFrame {
  private class ViewComponent extends JComponent {
    private static final long serialVersionUID = -8345829781181378852L;

    public Dimension getPreferredSize() {
      return new Dimension(width, height);
    }
   
    public void paint(Graphics g) {
      if (drawPixels) {
        DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width * height
            * 3);
  
        WritableRaster raster = Raster.createInterleavedRaster(dataBuffer, 
            width, height, width * 3, 3, new int[]{0, 1, 2}, null);
  
        ColorModel colorModel = new ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8},
            false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
  
        bimg = new BufferedImage(colorModel, raster, false, null);
  
        g.drawImage(bimg, 0, 0, null);
      }
      try {
        int[] users = tracker.userGen.getUsers();
        for (int i = 0; i < users.length; ++i) {
          Color c = colors[users[i] % colors.length];
          c = new Color(255 - c.getRed(), 255 - c.getGreen(), 
                        255 - c.getBlue());
  
          g.setColor(c);
  
          if (drawSkeleton && tracker.skeletonCap.isSkeletonTracking(users[i]))
            drawSkeleton(g, users[i]);
  
          if (printID) {
            Point3D com = tracker.convertRealWorldToProjective(
                tracker.userGen.getUserCoM(users[i]));
            String label = null;
            if (!printState) {
              label = new String("" + users[i]);
            } else if (tracker.skeletonCap.isSkeletonTracking(users[i])) {
              // Tracking
              label = new String(users[i] + " - Tracking");
            } else if (tracker.skeletonCap.isSkeletonCalibrating(users[i])) {
              // Calibrating
              label = new String(users[i] + " - Calibrating");
            } else {
              // Nothing
              label = new String(users[i] + " - Looking for pose (" + 
                  tracker.calibPose + ")");
            }
  
            g.drawString(label, (int) com.getX(), (int) com.getY());
          }
        }
        for (Integer id: tracker.history.keySet()) {
          ArrayList<Point3D> points = tracker.history.get(id);
          g.setColor(colors[id % colors.length]);
          int[] xPoints = new int[points.size()];
          int[] yPoints = new int[points.size()];
          for (int i = 0; i < points.size(); i++) {
            Point3D proj = tracker.convertRealWorldToProjective(
                points.get(i));
            xPoints[i] = (int)proj.getX();
            yPoints[i] = (int)proj.getY();
          }
          g.drawPolyline(xPoints, yPoints, points.size());
          Point3D proj = tracker.convertRealWorldToProjective(
              points.get(points.size() - 1));
          g.drawOval((int)proj.getX(), (int)proj.getY(), 5, 5);
        }
      } catch (StatusException e) {
        e.printStackTrace();
      }
    }
    
    private void drawLine(Graphics g,
        HashMap<SkeletonJoint, SkeletonJointPosition> jointHash,
        SkeletonJoint joint1, SkeletonJoint joint2) {
      Point3D pos1 = jointHash.get(joint1).getPosition();
      Point3D pos2 = jointHash.get(joint2).getPosition();
  
      if (jointHash.get(joint1).getConfidence() == 0
          || jointHash.get(joint2).getConfidence() == 0)
        return;
  
      g.drawLine((int) pos1.getX(), (int) pos1.getY(), (int) pos2.getX(),
          (int) pos2.getY());
    }
  
    private void drawSkeleton(Graphics g, int user) throws StatusException {
      HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints = 
          tracker.getJoints(user);
      HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints.get(
          new Integer(user));
  
      drawLine(g, dict, SkeletonJoint.HEAD, SkeletonJoint.NECK);
  
      drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.TORSO);
      drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.TORSO);
  
      drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.LEFT_SHOULDER);
      drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.LEFT_ELBOW);
      drawLine(g, dict, SkeletonJoint.LEFT_ELBOW, SkeletonJoint.LEFT_HAND);
  
      drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.RIGHT_SHOULDER);
      drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, 
               SkeletonJoint.RIGHT_ELBOW);
      drawLine(g, dict, SkeletonJoint.RIGHT_ELBOW, SkeletonJoint.RIGHT_HAND);
  
      drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.TORSO);
      drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.TORSO);
      drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.RIGHT_HIP);
  
      drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.LEFT_KNEE);
      drawLine(g, dict, SkeletonJoint.LEFT_KNEE, SkeletonJoint.LEFT_FOOT);
  
      drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_KNEE);
      drawLine(g, dict, SkeletonJoint.RIGHT_KNEE, SkeletonJoint.RIGHT_FOOT);
    }
  }
  
  private static final long serialVersionUID = 896308420949738738L;

  private HandTracker tracker;
  private Color colors[] = {
      Color.RED, Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA,
      Color.PINK, Color.YELLOW, Color.WHITE};
  private boolean drawPixels = true;
  private boolean drawSkeleton = true;
  private boolean printID = true;
  private boolean printState = true;
  private boolean drawBackground = true;
  
  private byte[] imgbytes;
  private int width, height;
  private BufferedImage bimg;
  
  public HandTrackerView(HandTracker tracker) {
    super("OpenNI Hand Tracker");
    addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    
    this.tracker = tracker;
    width = tracker.depthWidth();
    height = tracker.depthHeight();
    imgbytes = new byte[ width * height * 3];
    
    getContentPane().add(new ViewComponent());
  }
  
  public void updateDepthImage() {
    ShortBuffer depth = tracker.depthBuffer();
    ShortBuffer scene = tracker.sceneBuffer();
    float[] histogram = tracker.histogram();
    depth.rewind();

    while (depth.remaining() > 0) {
      int pos = depth.position();
      short pixel = depth.get();
      short user = scene.get();

      imgbytes[3 * pos] = 0;
      imgbytes[3 * pos + 1] = 0;
      imgbytes[3 * pos + 2] = 0;

      if (drawBackground || pixel != 0) {
        int colorID = user % (colors.length - 1);
        if (user == 0) {
          colorID = colors.length - 1;
        }
        if (pixel != 0) {
          float histValue = histogram[pixel];
          imgbytes[3 * pos] = (byte)(histValue * colors[colorID].getRed());
          imgbytes[3 * pos + 1] = (byte)(histValue * colors[colorID].
                                         getGreen());
          imgbytes[3 * pos + 2] = (byte)(histValue * colors[colorID].
                                         getBlue());
        }
      }
    }
  }
}