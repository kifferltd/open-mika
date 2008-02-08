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


public class DrawRect extends VisualTestImpl {

    public class MyPanel extends Panel {

      public MyPanel(int w, int h, Color b, Color f) {
        super();
        this.setSize(w,h);
        this.setBackground(b);
        this.setForeground(f);
      }
      public void paint(Graphics g) {
        // according to the sun specs drawRect(x,y,w,h) should draw a rectangle
        // with corners (x,y), (x,y+h), (x+w,y), (x+w,y+h),
        // whereas one would expect the corner coordinates to be
        // (x,y), (x,y+h-1), (x+w-1,y), (x+w-1,y+h-1),
        // on the other hand clearRect and fillRect are filling rectangles with corners at
        // (x,y), (x,y+h-1), (x+w-1,y), (x+w-1,y+h-1) as expected. Beware of this difference!

        g.drawRect(1,1,this.getWidth()-3,this.getHeight()-3);
        g.drawRect(3,3,this.getWidth()-7,this.getHeight()-7);
        g.drawRect(5,5,this.getWidth()-11,this.getHeight()-11);

        g.setColor(Color.blue);
        g.drawRect(10,30,50,20);
        g.setColor(Color.white);
        g.drawRect(10,50,50,20);
        g.setColor(Color.blue);
        g.drawRect(10,70,50,20);

        g.setColor(Color.white);
        g.drawRect(60,30,50,20);
        g.setColor(Color.blue);
        g.drawRect(60,50,50,20);
        g.setColor(Color.white);
        g.drawRect(60,70,50,20);

        g.setColor(Color.blue);
        g.drawRect(125,30,50,20);
        g.clearRect(125,30,50,20);

        g.setColor(Color.white);
        g.drawRect(125,60,50,20);
        g.setColor(Color.blue);
        g.fillRect(125,60,50,20);

        g.setColor(Color.green);
        g.drawRect(150,100,0,0);

      }

    }

    MyPanel mp;

    public DrawRect() {

        super();
        setLayout(null);
        setBackground(Color.green);
        mp = new MyPanel(200,150, Color.red, Color.yellow);
        mp.setLocation(75, 15);
        add(mp);

    }

    public String getTitle(){
     	 	return "DrawRect";
    }
    public String getHelpText(){
      return ("According to sun specs drawRect(x,y,w,h) should draw a rectangle with corners " +
	      "at (x,y), (x,y+h), (x+w,y), (x+w,y+h), " +
              "whereas one would expect the corner coordinates to be " +
              "(x,y), (x,y+h-1), (x+w-1,y), (x+w-1,y+h-1); " +
              "on the other hand clearRect and fillRect are filling rectangles with corners at " +
              "(x,y), (x,y+h-1), (x+w-1,y), (x+w-1,y+h-1) as expected. Beware of this difference!\n " +
              "Concerning the test now. You should see a green main panel. In its center " +
	      "a red plane of width 200 and height 150 must be visible. Inside this plane three " +
	      "yellow concentric rectangles are drawn of sizes 188x148, 184x144, and " +
	      "180x140 respectively, and of thickness 1. Between the yellow rectangles, " +
	      "the background should be visible as red rectangles of thickness 1. The red " +
	      "visible outer border of the red plane should be 1 pixel thick aswell.\n " +
	      "Inside the concentric yellow rectangles, on the left side you should see 6 " +
	      "blue and white rectangles overlapping one another with their left or top sides. " +
	      "To the right of these 6 rectangles a blue and a white rectangle are drawn; the blue " +
	      "one is cleared using clearRect, the white one is painted blue using fillRect, " +
	      "each time the same x,y,w and h were specified as those used to draw the rectangles; " +
	      "you should see that the right and bottom lines of the rectangles are unaffected by " +
	      "either clearRect or fillRect. Finally one should see a single white point below the " +
	      "last mentioned white rectangle; it results from a call to drawRect with w=h=0. ");
    }
     	
    public java.awt.Panel getPanel(VisualTester vt){
     	 	return this;
    }
     	
    public String getLogInfo(java.awt.Panel p, boolean b){
     		return "no logging info !";
    }
    public void start(java.awt.Panel p, boolean b){
      // the following call to repaint should not be needed:
      // mp.repaint();

    }
    public void stop(java.awt.Panel p){}



    static public void main (String[] args) {
           new DrawRect();
    }
}
