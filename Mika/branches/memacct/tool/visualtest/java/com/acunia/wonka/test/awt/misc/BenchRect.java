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


// Author: D. Buytaert
// Created: 2001/05/10

package com.acunia.wonka.test.awt.misc;

import java.awt.Color;
import java.awt.Panel;

import com.acunia.wonka.test.awt.VisualTestImpl;
import com.acunia.wonka.test.awt.VisualTester;

public class BenchRect extends VisualTestImpl {

  class BCanvas extends Panel implements Runnable {
    boolean stop = false;
    long rate, prev, next;
    int x;

    public BCanvas() {
    }

    public void paint(java.awt.Graphics g) {
      x = (x + 3) % this.getWidth();

      g.setColor(Color.red);
      g.fillRect(x, 20, 20, this.getHeight() - 20);

      // Calculate frame rate:
      next++;
      if (System.currentTimeMillis() > prev + 1000) {
        prev = System.currentTimeMillis();
        rate = next;
        next = 0;
      }

      g.drawString(rate +" frames/rectangles per second", 10, 10);
    }

    public void run() {
      System.out.println("started");
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
 //      Thread.yield();
        while (!stop) {
          this.repaint();
        }
        System.out.println("stopped");

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

  public BenchRect() {
  }

  public String getHelpText() {
    return "A microbenchmark to benchmark the performance of Rudolph's rendering engine, in particular by using java.awt.Graphics.DrawRect().";
  }

  public java.awt.Panel getPanel(VisualTester vt){
    this.vt = vt;
    return new BCanvas();
  }

  public void start(java.awt.Panel p, boolean autorun){
    try {
      new Thread((BCanvas)p, "BenchRect thread").start();
    }
    catch(ClassCastException cce) {
    }
  }

  public void stop(java.awt.Panel p){
    try {
      ((BCanvas)p).stop = true;
    }
    catch(ClassCastException cce) {
    }
  }

}

