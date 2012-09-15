/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

package com.acunia.wonka.rudolph.taskbar.applets;

import java.awt.*;
import com.acunia.wonka.rudolph.taskbar.*;

public class MemoryApplet extends TaskBarApplet implements Runnable {

  private int width;
  private int height;
  private int interval;
  private long total = 0;
  private boolean horizontal;

  public MemoryApplet(TaskBar taskbar) {
    super(taskbar);

    width = Integer.parseInt(taskbar.getProperties().getProperty("applet.memory.width", "100"));
    height = Integer.parseInt(taskbar.getProperties().getProperty("applet.memory.height", "15"));
    interval = Integer.parseInt(taskbar.getProperties().getProperty("applet.memory.interval", "500"));

    total = Runtime.getRuntime().totalMemory();
    horizontal = (TaskBar.getBarOrientation() == TaskBar.HORIZONTAL);

    Thread thread = new Thread(this, "MemoryApplet");
    thread.setDaemon(true);
    thread.start();
  }

  public void run() {
    while(true) {
      try {
        Thread.sleep(interval);
        repaint();
      }
      catch(Exception e) {
      }
    }
  }

  public void paint(Graphics g) {
    long free = Runtime.getRuntime().freeMemory();
    if(horizontal) {
      g.setColor(Color.white);
      g.fillRect(0, 0, width, height);
      g.setColor(Color.red);
      g.fillRect(0, 0, (int)(width * (total - free) / total), height);
      g.setColor(Color.black);
      g.drawRect(0, 0, width, height);
    }
    else {
      g.setColor(Color.white);
      g.fillRect(0, 0, height, width);
      g.setColor(Color.red);
      g.fillRect(0, 0, height, (int)(width * (total - free) / total));
      g.setColor(Color.black);
      g.drawRect(0, 0, height, width);
    }
  }

  public Dimension getPreferredSize() {
    return new Dimension(width, height);
  }

  public Dimension getMinimumSize() {
    return new Dimension(width, height);
  }
  
  public Dimension getMaximumSize() {
    return new Dimension(width, height);
  }

}

