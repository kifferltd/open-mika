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
import com.acunia.wonka.test.awt.*;
import java.awt.*;


public class CreateImage extends VisualTestImpl{

//  MainCanvas mcanvas;
//class MainCanvas extends Component implements Runnable {   // bug
  class MainCanvas extends Panel implements Runnable {

    public boolean stop = false;
    Image backBuffer = null;
    Graphics backG = null;
    String str1 = "/k/ Embedded";
    String str2 = "Java Solutions";
    int pointSize = 25;
    Point[] points = new Point[10];
    int[] pointRates = new int[points.length];
    Point string1Pt = new Point(0, 0);
    Point string2Pt = new Point(0, 25);
    int ascent = 0;

    MainCanvas() {
      for (int i=0; i<points.length; i++) {
        points[i] = new Point(0, 0);
        initPoint(i);
      }
  //    (new Thread(this)).start();
    }

    void initPoint(int i) {
      points[i].x = (int)(Math.floor(Math.random()*this.getSize().width));
      points[i].y = this.getSize().height;
      pointRates[i] = (int)(Math.floor(Math.random()*30)) + 5;
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

      try {
        if (backBuffer == null
                 || backBuffer.getWidth(null) != w
                 || backBuffer.getHeight(null) != h) {
          backBuffer = this.createImage(w, h);
          if (backBuffer != null) {
    //      if (backG != null) {
    //        backG.dispose();                      //jvde: method not implemented
    //      }
            backG = backBuffer.getGraphics();
            backG.setFont(new Font("helvB34", Font.BOLD, 34));
            FontMetrics fm = backG.getFontMetrics();
            string1Pt.x = (w-fm.stringWidth(str1))/2;
            string2Pt.x = (w-fm.stringWidth(str2))/2;
            ascent = fm.getAscent();
          }
        }

        if (backBuffer != null) {
  //      System.out.println("font height="+fh);
          backG.setColor(new Color(200, 40, 70));  // acunia-red
          backG.fillRect(0, 0, w, h);

          // Bubbles behind the string.
          backG.setColor(Color.cyan);
          for (int i=0; i<points.length/3; i++) {
            if (points[i].x > 0 && points[i].x < w-pointSize &&      //jvde
                points[i].y > 0 && points[i].y < h-pointSize    )    //jvde
              backG.drawOval(points[i].x, points[i].y, pointSize, pointSize);
    //        backG.fillOval(points[i].x, points[i].y, pointSize, pointSize);
          }

          // Paint the string
          backG.setColor(Color.white);
          if  (string1Pt.y % h + h >= 0)
            backG.drawString(str1, string1Pt.x, string1Pt.y % h + h);
          if  (string1Pt.y % h + 2*h < h+ascent)
            backG.drawString(str1, string1Pt.x, string1Pt.y % h + 2*h);
          if  (string2Pt.y % h + h >= 0)
            backG.drawString(str2, string2Pt.x, string2Pt.y % h + h);
          if  (string2Pt.y % h + 2*h < h+ascent)
            backG.drawString(str2, string2Pt.x, string2Pt.y % h + 2*h);

          // Bubbles in front of the string.
          backG.setColor(Color.blue);
          for (int i=points.length/3; i<points.length; i++) {
            if (points[i].x > 0 && points[i].x < w-pointSize &&         //jvde
                points[i].y > 0 && points[i].y < h-pointSize    )       //jvde
              backG.drawOval(points[i].x, points[i].y, pointSize, pointSize);
    //        backG.fillOval(points[i].x, points[i].y, pointSize, pointSize);
          }
          g.drawImage(backBuffer, 0, 0, null);
        }
      }
      catch (Throwable t) {
        System.out.println("caught unwanted Exception "+t.toString());
        t.printStackTrace();
      };
    }

    public void run() {
      int w = 0;
      int h = 0;
      int w0=400;
      try {
        while (w==0 || h==0 || w!=w0) {
          w0=w;
          Thread.sleep(80);
          w=this.getBounds().width;
          h=this.getBounds().height;
        }
        while (!stop) {
          for (int i=0; i<points.length; i++) {
            points[i].y -= pointRates[i];
            if (points[i].y < -pointSize) {
              initPoint(i);
            }
          }
          string1Pt.y--;
          string2Pt.y--;
  //        if (string1Pt.y == -h) string1Pt.y = 0; //jvde
  //        if (string2Pt.y == -h) string2Pt.y = 0; //jvde
          this.repaint();   // jvde : this will call 'update' from Component, unless we have our own 'update'.
          Thread.sleep(80);
        }
      }
      catch (Throwable t) {
        System.out.println("caught unwanted Exception "+t.toString());
        t.printStackTrace();
      };
    }
  }
    
  public CreateImage() {
//    mcanvas = new MainCanvas();
//    add(mcanvas, BorderLayout.CENTER);       // bug: does not support user defined subclasses of Component
  }

  public Panel getPanel(VisualTester vte){
    vt = vte;
    return new MainCanvas();
//    return mcanvas;
  }

  public String getHelpText(){
    return ("You should see a background coloured in 'acunia-red'. On the foreground, a string " +
            "\"/k/ Embedded Java Solutions\", in color white, in font Helvetica-bold-25, should be visible. The String " +
            "should be moved upward, disappear piece by piece at the top of the screen, meanwhile " +
            "reappearing at the bottom of the screen. At the same time light-blue and dark-blue " +
            "colored circles are moving from bottom to top aswell. They should be moving faster " +
            "than the string.");
  }
     	
  public void start(java.awt.Panel p, boolean autorun){
    try {
      (new Thread((MainCanvas)p, "CreateImage thread")).start();
    }
    catch(ClassCastException cce) {}
  }

  public void stop(java.awt.Panel p){
    try {
      ((MainCanvas)p).stop = true;
    }
    catch(ClassCastException cce) {}
  }

}

