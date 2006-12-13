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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;

import com.acunia.wonka.test.awt.VisualTestImpl;
import com.acunia.wonka.test.awt.VisualTester;

public class DrawImage extends VisualTestImpl {

  VisualTester getVt() {
    return vt;
  }
  
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
    }

    public void update(Graphics g) {
      this.paint(g);
    }

    public void paint(Graphics g) {
      try {
        int W = this.getBounds().width;
        int H = this.getBounds().height;
        int w4 = (W/4);
        int h2 = (H/2);
        int w = 4*w4;
        int h = 2*h2;
        g.setFont(new Font("HelvB24",1,30));
        FontMetrics fm = g.getFontMetrics();

        // create and initialise buffered image
        if (backBuffer == null
                  || backBuffer.getWidth(null) != w
                  || backBuffer.getHeight(null) != h) {

          int  x = w/(2*nbrRect);
          int  y = h/(2*nbrRect);
 
          backBuffer = this.createImage(w, h);
          if (backBuffer != null) {
            backG = backBuffer.getGraphics();
            for  (int i=0;i<nbrRect;i++) {
              backG.setColor(new Color(255-R*i, 255-G*i, B*i));
              backG.fillRect(x*i, y*i, w-x*2*i, h-y*2*i);
            }
            backG.setColor(Color.red);
            for (int j=0;j<h-2;j+=h2) {
              for (int i=0;i<w-4;i+=w4) {
                backG.drawRect(i, j, w4-1, h2-1);
              }
            }
          }
        }

        // display image, on different locations
        if (backBuffer != null) {
          switch (step) {
            case 0:
              g.clearRect(0, 0, W, H);
    //          g.drawImage(backBuffer, 0, 0, w-1, h-1, 0, 0, w-1, h-1, null); // works wrong on sun jvm
              g.drawImage(backBuffer, 0, 0, w, h, null);           // works correct on both sun jvm and wonka
              g.setColor(Color.white);
              g.drawString("SOURCE IMAGE", (w-fm.stringWidth("SOURCE IMAGE"))/2, h2+fm.getAscent()/2);
              break;
            case 1:
              g.clearRect(0, 0, W, H);
              for (int j=0;j<h-2;j+=h2)
                for (int i=0;i<w-4;i+=w4) {
                  g.drawImage(backBuffer, i, j, i+w4-1, j+h2-1, i, j, i+w4-1, j+h2-1, null);
                  Thread.sleep(100);
                  g.clearRect(i, j, i+w4, j+h2);
                }
              break;
            case 2:
              g.clearRect(0, 0, W, H);
              for (int j=0;j<h-2;j+=h2)
                for (int i=0;i<w-4;i+=w4) {
                  g.drawImage(backBuffer, i, j, i+w4-1, j+h2-1, i, j, i+w4-1, j+h2-1, null);
                  Thread.sleep(50);
                }
              break;
            case 3:
              g.clearRect(0, 0, W, H);
              for (int j=0;j<h-2;j+=h2)
                for (int i=0;i<w-4;i+=w4) {
                  g.drawImage(backBuffer, i-w/8, j-h/4, i-w/8+w4-1, j+h2-h/4-1, i, j, i+w4-1, j+h2-1, null);
                  Thread.sleep(50);
                }
              break;
            case 4:
              g.clearRect(0, 0, W, H);
              for (int j=0;j<h-2;j+=h2)
                for (int i=0;i<w-4;i+=w4) {
                  g.drawImage(backBuffer, i+w/8, j-h/4, i+w/8+w4-1, j+h2-h/4-1, i, j, i+w4-1, j+h2-1, null);
                  Thread.sleep(50);
                }
              break;
            case 5:
              g.clearRect(0, 0, W, H);
              for (int j=0;j<h-2;j+=h2)
                for (int i=0;i<w-4;i+=w4) {
                  g.drawImage(backBuffer, i+w/8, j+h/4, i+w/8+w4-1, j+h2+h/4-1, i, j, i+w4-1, j+h2-1, null);
                  Thread.sleep(50);
                }
              break;
            case 6:
              g.clearRect(0, 0, W, H);
              for (int j=0;j<h-2;j+=h2)
                for (int i=0;i<w-4;i+=w4) {
                  g.drawImage(backBuffer, i-w/8, j+h/4, i-w/8+w4-1, j+h2+h/4-1, i, j, i+w4-1, j+h2-1, null);
                  Thread.sleep(50);
                }
              break;
            default:
              break;
          }
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

        while (!stop) {
          this.repaint();
          Thread.sleep(1000);
          step = (step + 1) % 7;
        }
      }
      catch (InterruptedException e) {
        getVt().logException("caught Interrupted Exception ",DrawImage.this,e);
        e.printStackTrace();
      }
      catch (Throwable t) {
        getVt().logException("caught Throwable Exception",DrawImage.this,t);
        t.printStackTrace();
      }
    }

  }
  
  public DrawImage() {
  }

  public Panel getPanel(VisualTester vte){
    vt = vte;
    return new MCanvas();
  }


  public String getHelpText(){
    return ("Basis for this test is an image, composed of concentric rectangular planes, each " +
            "filled with different colors. Furthermore the image is divided in 8 rectangle of " +
            "equal size. The borders of these rectangles are drawn in red. These borders are " +
            "used to show a violation of the sun specs by the sun java awt: a 1 pixel wide " +
            "right and bottom border, is never drawn by some versions of the method " +
            "\"Graphics.drawImage()\". The image is first displayed as a whole, with the " +
            "inscription \"SOURCE IMAGE\". After that, the image is displayed 6 times with " +
            "different origins, as a sequence of 8 sub-images of equal size. The first time " +
            "of this series of 6, the " +
            "origin is (0,0) and each subimage is, in turn, displayed and erased. The second, third, forth, " +
            "fifth and sixt time, subimages are erased only after all " +
            "subimages have become visible. The second time the origin is still (0,0). The third time the " +
            "origin is shifted to about (-50,-50). The forth time the origin is shifted to " +
            "about (50,-50). The fifth time the origin is (50,50) and the sixth time it is (-50, 50)." );
  }

  public void start(java.awt.Panel p, boolean autorun){
    try {
      (new Thread((MCanvas)p,"DrawImage Thread")).start();
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
    new DrawImage();
  }
}

