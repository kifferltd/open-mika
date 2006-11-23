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
// Created: 2001/05/02

package com.acunia.wonka.test.awt.Graphics;
import  com.acunia.wonka.test.awt.*;
import java.awt.*;


public class DrawOval extends VisualTestImpl {

  public class MyPanel extends Panel {

    public MyPanel(int w, int h, Color b, Color f) {
      super();
      this.setSize(w,h);
      this.setBackground(b);
      this.setForeground(f);
    }

    public void paint(Graphics g) {
      g.drawOval(1,1,this.getWidth()-3,this.getHeight()-3);
      g.drawOval(3,3,this.getWidth()-7,this.getHeight()-7);
      g.drawOval(5,5,this.getWidth()-11,this.getHeight()-11);
    }
  }

  MyPanel mp1;
  MyPanel mp2;

  public DrawOval() {
    super();
    setLayout(null);
    setBackground(Color.green);
    mp1 = new MyPanel(149,200, Color.red, Color.yellow);
    mp2 = new MyPanel(120,100, Color.blue, Color.yellow);
    mp1.setLocation(0,0);
    mp2.setLocation(149,0);
    add(mp1);
    add(mp2);
  }

  public String getTitle(){
    return "DrawOval";
  }

  public String getHelpText(){
    return ("You should see a green main panel. In its top-left corner a red plane of " +
            "width 149 and height 200 must be visible. Inside this plane three " +
            "yellow concentric circles are drawn of diameter 147, 143, and 139 " +
            "respectively, and of thickness 1. Between the yellow circles, " +
            "the background should be visible as red circles of thickness 1. The red " +
            "visible left, right and top border of the plane should be 1 pixel thick " +
            "aswell.\n" +
            "To the right of the first plane a blue plane of " +
            "width 120 and height 100 must be visible. Inside this plane three " +
            "yellow concentric circles are drawn of diameter 97, 93, and 89 " +
            "respectively, and of thickness 1. Between the yellow circles, " +
            "the background should be visible as blue circles of thickness 1. The blue " +
            "visible left and top border of the plane should be 1 pixel thick aswell, " +
            "the bottom border should be 2 pixels thick.");
  }
     	
  public java.awt.Panel getPanel(VisualTester vt){
    return this;
  }
     	
  public String getLogInfo(java.awt.Panel p, boolean b) {
    return "no logging info !";
  }
  public void start(java.awt.Panel p, boolean b){
    // the following call to repaint should not be needed:
    // mp1.repaint();
    // mp2.repaint();
  }

  public void stop(java.awt.Panel p){}



  static public void main (String[] args) {
    new DrawOval();
  }
}
