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
// Created: 2001/05/09

package com.acunia.wonka.test.awt.Graphics;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextField;

import com.acunia.wonka.test.awt.VisualTestImpl;
import com.acunia.wonka.test.awt.VisualTester;

public class CopyAreaObscured extends VisualTestImpl {

  class MCanvas extends Panel implements Runnable {
    Image backBuffer = null;
    Graphics backG = null;
    int nbrRect = 10;
    int R=256/nbrRect;
    int G=256/nbrRect;
    int B=255/nbrRect;
    int x;
    int y;
    boolean stop = false;
    int step = 0;

    public MCanvas() {
//      this.setLayout(new BorderLayout());
//      this.add(new Button("BUTTON"), BorderLayout.NORTH);
//      Panel p = new Panel();
//      this.add(p, BorderLayout.WEST);
//      this.add(new Scrollbar(), BorderLayout.EAST);

      this.add(new Button("BUTTON"), BorderLayout.NORTH);
      Panel p = new Panel();
      this.add(p);
      this.add(new Scrollbar());
      this.add(new Label("Ready"));
      p.setLayout(new BorderLayout());
      p.add(new TextField("Hello"), BorderLayout.WEST);
      p.add(new Button("Press"), BorderLayout.EAST);
      p.add(new Panel(), BorderLayout.CENTER);
    }

    public void update(Graphics g) {
      this.paint(g);
    }

    public void paint(Graphics g) {
      try {
        int W = this.getBounds().width;
        int H = this.getBounds().height;
        int w4 = (W/4);
        int h4 = (H/4);
        int w2 = w4+w4;
        int h2 = h4+h4;

        // display image, on different locations
        switch (step) {
          case 0:
            g.clearRect(0, 0, W, H);
            g.setColor(new Color(255, 200, 0));
            g.fillRect(w4, 0, w2, h4);
            g.setColor(new Color(255, 255, 0));
            g.fillRect(w4, h4, w2, h4);

            for (int j = 0; j < 3; j++)
              for (int i = -1; i < 2; i++) {
                Thread.sleep(50);
                g.copyArea(w4, 0, w2, h2, w2*i, h4*j);
              }

            break;
          case 1:
            g.clearRect(0, 0, W, H);
            g.setColor(new Color(255,200,0));
            g.fillRect(w4, h2, w2, h4);
            g.setColor(new Color(255,255,0));
            g.fillRect(w4, h2+h4, w2, h4);

            for (int j = 0; j > -3; j--)
              for (int i = 1; i > -2; i--) {
                Thread.sleep(50);
                g.copyArea(w4, h2, w2, h2, w2*i, h4*j);
              }

            break;
          case 2:
            g.clearRect(0, 0, W, H);
            g.setColor(new Color(255,0,255));
            g.fillRect(0, h4, w4, h2);
            g.setColor(new Color(200,0,255));
            g.fillRect(w4, h4, w4, h2);

            for (int i = 0; i < 3; i++)
              for (int j = -1; j < 2; j++) {
                Thread.sleep(50);
                g.copyArea(0, h4, w2, h2, w4*i, h2*j);
              }

            break;
          case 3:
            g.clearRect(0, 0, W, H);
            g.setColor(new Color(255, 0, 255));
            g.fillRect(w2, h4, w4, h2);
            g.setColor(new Color(200, 0, 255));
            g.fillRect(w2+w4, h4, w4, h2);

            for (int i = 0; i > -3; i--)
              for (int j = 1; j > -2; j--) {
                Thread.sleep(50);
                g.copyArea(w2, h4, w2, h2, w4*i, h2*j);
              }

            break;
          default:
            g.clearRect(0, 0, W, H);
            break;
        }

      }
      catch (InterruptedException e) {
        System.out.println("caught Interrupted Exception "+e);
        e.printStackTrace();
      }  
      catch (Exception e) {
        e.printStackTrace();
      }
  
    }

    public void run() {
      try {
        int w1=0;
        int h1=0;
        int w2=400;
        int h2=234;
        while (w1==0 || h1==0 || w1!=w2 || h1!=h2) {
          w2=w1;
          h2=h1;
          Thread.sleep(80);
          w1 = this.getBounds().width;
          h1 = this.getBounds().height;
        }
        step = 4;
        this.repaint();
        step=0;
        while (!stop) {
          Thread.sleep(2000);
          this.repaint();
          step = (step + 1) % 4;
        }
      }
      catch (InterruptedException e) {
        System.out.println("caught Interrupted Exception "+e);
        e.printStackTrace();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

  }
  
  public CopyAreaObscured() {
  }

  public Panel getPanel(VisualTester vte){
    vt = vte;
    return new MCanvas();
  }


  public String getHelpText(){
    return ("Test for method \"Graphics.copyArea()\". \n Four times a part of the screen is " +
            "replicated several times horizontally and vertically such that the screen is " +
            "completely filled. Each time, the screen part that is replicated is a rectangular " +
            "area consisting of two smaller rectangles, filled with different colors.\n " +
            "1) in the first run, the rectangular area is located in the top-center part of the " +
            "screen, with its upper half colored dark-yellow, and its lower half colored light-yellow." +
            "The screen is filled from left to right and from top to bottom with copies of that " +
            "rectangle. Vertical copies are half overlapping. The result should be " +
            "that the whole screen area is colored dark-yellow.\n " +
            "2) The 2nd run: the rectangular area is located in the bottom-center part of the " +
            "screen, with its upper half colored dark-yellow, and its lower half colored light-yellow," +
            "like in the first run. " +
            "The screen is filled from right to left and from bottom to top with copies of that " +
            "rectangle. Vertical copies are half overlapping. The result should " +
            "be that the whole screen area is colored light-yellow.\n " +
            "3) The 3rd run: the rectangular area is located in the center-left part of the " +
            "screen, with its left half colored light-purple, and its right half colored dark-purple. " +
            "The screen is filled from top to bottom and from left to right with copies of that " +
            "rectangle. Horizontal copies are half overlapping. The result should " +
            "be that the whole screen area is colored light-purple.\n " +
            "4) The 4th run: the rectangular area is located in the center-right part of the " +
            "screen, with its left half colored light-purple, and its right half colored dark-purple," +
            "like in the 3rd run. " +
            "The screen is filled from bottom to top and from right to left with copies of that " +
            "rectangle. Horizontal copies are half overlapping. The result should " +
            "be that the whole screen area is colored dark-purple.\n " +
            "Remark that the window that is being cleared occupies the whole VTE screen. In this " +
            "second test however, that window is filled with several components and even a panel " +
            "with components, to demonstrate that Rudolps updating of the screen, does not " +
            "work perfectly yet");
  }

  public void start(java.awt.Panel p, boolean autorun){
    try {
      (new Thread((MCanvas)p,"CopyAreaObscured Thread")).start();
    }
    catch(ClassCastException cce) {
    }
  }

  public void stop(java.awt.Panel p){
    try {
      ((MCanvas)p).stop = true;
    }
    catch(ClassCastException cce) {
    }
  }

  static public void main(String[] args) {
    new CopyAreaObscured();
  }
}

