/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

package com.acunia.wonka.test.awt.Component;

import java.awt.*;
import java.util.*;
import com.acunia.wonka.test.awt.*;

public class ComponentOverlap extends VisualTestImpl {

  public class RandomRectangles extends Panel {

    private Random ran = new Random();

    public void paint(Graphics g) {
      int w = this.getWidth();
      int h = this.getHeight();
      
      for(int i=0; i < 100; i++) {
        g.setColor(new Color(ran.nextInt(0x00FFFFFF)));
        g.fillRect(ran.nextInt(w), ran.nextInt(h), ran.nextInt(w), ran.nextInt(h));
      }
    }
  }
  
  private Thread repaintThread;
  boolean keepRunning = true;
  RandomRectangles rr;
  private Panel p;

  public ComponentOverlap() {
    setLayout(null);
  
    rr = new RandomRectangles();
    rr.setSize(this.getSize());
    rr.setLocation(0, 0);

    p = new Panel();
    p.setSize(this.getSize());
    p.setLocation(0, 0);
    p.setLayout(new BorderLayout());
    
    Label l = new Label("This panel should always remain on top !!!");
    p.add(l, BorderLayout.CENTER);
    
    add(p);
    add(rr);
  }

  public void doLayout() {
    if(rr != null) rr.setSize(this.getSize());
    if(rr != null) p.setSize(this.getSize());
  }

  public String getHelpText() {
    return "";
  }
     	
  public void start(java.awt.Panel p, boolean autorun) {
    keepRunning = true;
    repaintThread = new Thread() {
      public void run() {
        while(keepRunning) {
          try {
            rr.repaint();
            System.out.println("repaint");
            Thread.sleep(2000);
            rr.paint(rr.getGraphics());
            System.out.println("getGraphics");
            Thread.sleep(2000);
          }
          catch(Exception e) {
          }
        }
      }
    };
    repaintThread.start(); 
  }

  public void stop(java.awt.Panel p) {
    keepRunning = false;
    repaintThread = null;
  }

}

