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
package edu.mit.yingyin.websocket;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.OpenNI.GestureRecognizedEventArgs;
import org.OpenNI.IObserver;
import org.OpenNI.StatusException;

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
  
  public void addObserver(IObserver<GestureRecognizedEventArgs> observer) 
      throws StatusException {
    tracker.addObserver(observer);
  }
  
  public void deleteObserver(IObserver<GestureRecognizedEventArgs> observer) {
    tracker.deleteObserver(observer);
  }

  public static void main(String s[]) {
    
    HandTrackerController app = new HandTrackerController();
    app.run();
  }
}
