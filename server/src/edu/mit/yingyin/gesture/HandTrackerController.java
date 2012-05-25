/****************************************************************************
*                                                                           *
*  OpenNI 1.x Alpha                                                         *
*  Copyright (C) 2011 PrimeSense Ltd.                                       *
*                                                                           *
*  This file is part of OpenNI.                                             *
*                                                                           *
*  OpenNI is free software: you can redistribute it and/or modify           *
*  it under the terms of the GNU Lesser General Public License as published *
*  by the Free Software Foundation, either version 3 of the License, or     *
*  (at your option) any later version.                                      *
*                                                                           *
*  OpenNI is distributed in the hope that it will be useful,                *
*  but WITHOUT ANY WARRANTY; without even the implied warranty of           *
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the             *
*  GNU Lesser General Public License for more details.                      *
*                                                                           *
*  You should have received a copy of the GNU Lesser General Public License *
*  along with OpenNI. If not, see <http://www.gnu.org/licenses/>.           *
*                                                                           *
****************************************************************************/
package edu.mit.yingyin.gesture;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.OpenNI.ActiveHandEventArgs;
import org.OpenNI.GestureRecognizedEventArgs;
import org.OpenNI.IObserver;
import org.OpenNI.Point3D;
import org.OpenNI.StatusException;

/**
 * A thread that controls the interaction with the <code>HandTracker</code>.
 * @author yingyin
 *
 */
public class HandTrackerController extends Thread {

	private HandTracker tracker = new HandTracker();
	private boolean shouldRun = true;
	private HandTrackerView view;

  public HandTrackerController () {
  	view = new HandTrackerView(tracker);
  	view.addKeyListener(new KeyListener() {
  		@Override
  		public void keyTyped(KeyEvent arg0) {}
  		@Override
  		public void keyReleased(KeyEvent arg0) {}
  		@Override
  		public void keyPressed(KeyEvent arg0) {
  			if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
  				shouldRun = false;
  				System.exit(0);
  			}
  		}
  	});
  	view.pack();
  	view.setVisible(true);
  }

  @Override
  public void run() {
    while(shouldRun) {
      tracker.updateDepth();
      view.updateDepthImage();
      view.repaint();
    }
    tracker.release();
    view.dispose();
  }
  
  public void addGestureRecognizedEventObserver(
      IObserver<GestureRecognizedEventArgs> observer) throws StatusException {
    tracker.addGestureRecognizedEventObserver(observer);
  }
  
  public void addHandCreateEventObserver(
      IObserver<ActiveHandEventArgs> observer) throws StatusException {
    tracker.addHandCreateEventObserver(observer);
  }
  
  public void addHandUpdateEventObserver(
      IObserver<ActiveHandEventArgs> observer) throws StatusException {
    tracker.addHandUpdateEventObserver(observer);
  }
  
  public void deleteGestureRecognizedEventObserver(
      IObserver<GestureRecognizedEventArgs> observer) {
    tracker.deleteGestureRecognizedEventObserver(observer);
  }
  
  public void deleteHandCreateEventObserver(
      IObserver<ActiveHandEventArgs> observer) {
    tracker.deleteHandCreateEventObserver(observer);
  }

  public Point3D convertRealWorldToProjective(Point3D p) 
      throws StatusException {
    return tracker.convertRealWorldToProjective(p);
  }
  
  public static void main(String s[]) {
    HandTrackerController app = new HandTrackerController();
    app.run();
  }
}
