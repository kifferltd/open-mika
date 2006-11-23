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
// Created: 2001/05/08

package com.acunia.wonka.test.awt.Graphics;
import  com.acunia.wonka.test.awt.*;
import java.awt.*;

public class DrawPoly extends VisualTestImpl {

  class Poly  extends Panel implements Runnable {

    Image backBuffer = null;
    Graphics g1 = null;
  //  Graphics g2 = null;
  //  Graphics g3 = null;
    int n1points = 6;
    int n2points = 3;
    int n3points = 4;
    int[] x1points = new int[n1points];
    int[] y1points = new int[n1points];
    int[] x2points = new int[n2points];
    int[] y2points = new int[n2points];
    int[] x3points = new int[n3points];
    int[] y3points = new int[n3points];
    int w1;
    int w2;
    int w3;
    int h1;
    int h2;
    int h3;

    int x1;
    int y1;
    int x2;
    int y2;
    int x3;
    int y3;

    Color   bg;
  //  boolean clear;
    boolean stop = false;

    Poly(int width, int height, Color background) {
      this.setBackground(background);
      bg =  background;

  //    (new Thread(this)).start();
    }


    void initPoints(int xoffs, int yoffs) {
 
      int w = this.getBounds().width;
      int h = this.getBounds().height;

      w1 = w2 = w3 = w/2-1;
      h1 = h;
      h2 = h3 = h/2-1;
      x1 = xoffs;
      x2 = x3 = w1 + xoffs;
      y1 = y2 = yoffs;
      y3 = h2 + yoffs;
      if (y3 > h) y3-=h;
  //    System.out.println("w "+w+" h "+h+" w1 "+w1+" h1 "+h1+" w2 "+w2+" h2 "+h2+" w3 "+w3+" h3 "+h3);

      x1points[0] = x1 + 9*w1/10;
      y1points[0] = y1 + 3*h1/10;
      x1points[1] = x1 + 9*w1/10;
      y1points[1] = y1 + 7*h1/10;
      x1points[2] = x1 + 5*w1/10;
      y1points[2] = y1 + 9*h1/10;
      x1points[3] = x1 + 1*w1/10;
      y1points[3] = y1 + 7*h1/10;
      x1points[4] = x1 + 1*w1/10;
      y1points[4] = y1 + 3*h1/10;
      x1points[5] = x1 + 5*w1/10;
      y1points[5] = y1 + h1/10;

      x2points[0] = x2 + 9*w2/10;
      y2points[0] = y2;
      x2points[1] = x2 + w2/10;
      y2points[1] = y2 + 2*h2/10;
      x2points[2] = x2 + 5*w2/10;
      y2points[2] = y2 + 9*h2/10;


      x3points[0] = x3 + 4*w3/10;
      y3points[0] = y3;
      x3points[1] = x3 + 9*w3/10;
      y3points[1] = y3 + 2*h3/10;
      x3points[2] = x3 + 6*w3/10;
      y3points[2] = y3 + 9*h3/10;
      x3points[3] = x3 + w3/10;
      y3points[3] = y3 + 7*h3/10;
    }

  //jvde : under sun java vm, and rudolph, to avoid flikkering, this class needs a paint method that
  //       updates the screen, and an update method that just calls the paint method.
  //       otherwise repaint calls Component.update() which clears the screen before calling paint.

    public void update(Graphics g) {
      this.paint(g);
    }

    public void paint(Graphics g) {

      int w = this.getBounds().width;
      int h = this.getBounds().height;

      if (backBuffer == null
                  || backBuffer.getWidth(null) != w
                  || backBuffer.getHeight(null) != h) {
        backBuffer = this.createImage(w, h);
        if (backBuffer != null) {
          g1 = backBuffer.getGraphics();
        }
      }

      if (backBuffer != null && g1 != null) {
        try {
          g1.clearRect(0,0,w,h);
          g1.setColor(Color.yellow);
          g1.drawPolygon(x1points, y1points, n1points);
          g1.setColor(Color.green);
          g1.drawPolygon(x2points, y2points, n2points);
          g1.setColor(Color.white);
          g1.drawPolygon(x3points, y3points, n3points);

          g.drawImage(backBuffer, 0, 0, null);
        }
        catch (Exception e) {
          System.out.println("caught unwanted exception "+e);
          e.printStackTrace();
        }
      }
    }

    public void run() {
      int x = 0;
      int y = 0;
      while (!stop) {
        initPoints(x,y);
        this.repaint();
        try { Thread.sleep(80); } catch (Exception e) {};
  //      x--;
  //      if (x > getBounds().width)  x-=getBounds().width;
        y++;
        if (y > this.getBounds().height) y -= this.getBounds().height;
      }
    }

  }
  //Poly polygon;

  public DrawPoly() {
//    polygon = new Poly(getBounds().width, getBounds().height, new Color(200, 40, 70));
//    add(polygon, BorderLayout.CENTER); // only needed if you return 'this' in 'getPanel'
                                         // but that doesn't work under sun java vm
  }

  public Panel getPanel(VisualTester vte){
    vt = vte;
    return new Poly(getBounds().width, getBounds().height, new Color(200, 40, 70));
//    return polygon;
  }


  public String getHelpText(){
    return ("You should see an acunia-red frame with a yellow hexagon, a green triangle and a " +
            "white trapezium, slowly  moving downward. Edges of the polygons that cross the " +
            "lower border of the frame should be clipped: only parts of these edges inside " +
            "the frame remain visible");
  }
     	
  public void start(java.awt.Panel p, boolean autorun){
    try {
      (new Thread((Poly)p, "DrawPoly thread")).start();
    }
    catch(ClassCastException cce) {}
  }

  public void stop(java.awt.Panel p){
    try {
      ((Poly)p).stop = true;
    }
    catch(ClassCastException cce) {}
  }

  static public void main(String[] args) {
    new DrawPoly();
  }
}

