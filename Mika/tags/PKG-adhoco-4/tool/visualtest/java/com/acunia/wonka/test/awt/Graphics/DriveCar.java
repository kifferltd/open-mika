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


// Author: J. Vandeneede
// Created: 2001/05/18

package com.acunia.wonka.test.awt.Graphics;

import java.awt.*;
import com.acunia.wonka.test.awt.*;

public class DriveCar extends VisualTestImpl {

  class Place extends Panel implements Runnable {

    Image bgBuffer = null;
    Image carImage = null;
    Graphics g1 = null;
    int x=0;
    int y=10000;
    int xo=0;
    int yo=0;
    boolean stop = false;

    Place(int width, int height, Color background) {
      this.setSize(width, height);
      this.setBackground(background);
      carImage = Toolkit.getDefaultToolkit().createImage(this.getClass().getResource("/car-image.png"));
    }


    public void update(Graphics g) {
      paint(g);
      }

    public void paint(Graphics g) {
 
      int w = carImage.getWidth(null);
      int h = carImage.getHeight(null);

      if (bgBuffer == null
                  || bgBuffer.getWidth(null) != w
                  || bgBuffer.getHeight(null) != h) {
        bgBuffer = this.createImage(w,h);
        if (bgBuffer != null) {
          g1 = bgBuffer.getGraphics();
          g1.clearRect(0,0,w, h);
          g.clearRect(0,0,this.getBounds().width,this.getBounds().height);
        }
      }

      if (bgBuffer != null) {
        g.drawImage(bgBuffer, xo, yo, xo+w-1, yo+h-1, 0, 0, w-1, h-1, null);
        g.drawImage(carImage, x, y, null);
      }
    }

    public void run() {
      int xb = 0;
      int yb = 0;
      int xStep = -4;
      int yStep = 1;
      int xe = 0;
      int xtest = 400;
      try {
        while (xb == 0 || /*yb == 0 ||*/ xe == 0 || xtest != xb) {
          if (xb!=0) xtest=xb;            // this.getBounds() seems to need time to stabilize
          Thread.sleep(100);
          xb = this.getBounds().width-1;
          yb = 0;//-carImage.getHeight(null)/4;
          xe = -carImage.getWidth(null);
        }
        x = xb;
        y = yb;
  //    System.out.println("x1="+x1+" y1="+y1+" xStep="+xStep);
        while (!stop) {
  //      System.out.println(" x="+x+" y="+y);
          this.repaint();
          Thread.sleep(40);
          xo=x;
          yo=y;
          x += xStep;
          y += yStep;
          if (x <= xe)  {
            x = xb;
            y = yb;
          }
        }
      }
      catch (/*Interrupted*/Exception e) {
        System.out.println("caught unwanted Exception "+e);
        e.printStackTrace();
      }
    }

  }

  public DriveCar() {}

  public String getHelpText(){
    return ("To be successful, this test should show a white background, a red moving Corvette with a" +
            "white label \"ACUNIA\" on it, the Corvette should move diagonally from the upper right " +
            "corner of your screen, to its lower left corner. The car disappears bit by bit at the " +
            "left border of the screen, and then re-appears bit by bit at the right border.");
  }
     	
  public Panel getPanel(VisualTester vte) {
    vt = vte;
    return new Place(getBounds().width, getBounds().height, Color.white);
  }

  public void start(java.awt.Panel p, boolean autorun){
    try {
      (new Thread((Place)p,"DriveCar Thread")).start();
    }
    catch(ClassCastException cce) {}
  }

  public void stop(java.awt.Panel p){
    try {
      ((Place)p).stop = true;
    }
    catch(ClassCastException cce) {}
  }

  static public void main(String[] args) {
    new DriveCar();
  }
}

