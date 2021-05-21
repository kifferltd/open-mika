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
import java.awt.Graphics;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class MemoryUsage extends VisualTestImpl {

  static private int history = 100;
  static private int f1 = 1;
  static private int f2 = 50;

  static private LinkedList ll1;
  static private LinkedList ll2;
  static private LinkedList ll3;
  static private LinkedList ll4;

  static private long totalMem;
  static private long minMem;
  static private long maxMem;
  static private int count = 1;

  static private Color c1;
  static private Color c2;
  static private Color c3;
  static private Color c4;

  static {
    totalMem = Runtime.getRuntime().totalMemory();
    minMem = totalMem - Runtime.getRuntime().freeMemory();
    maxMem = minMem;

    ll1 = new LinkedList();
    ll2 = new LinkedList();
    ll3 = new LinkedList();
    ll4 = new LinkedList();

    c1 = (Color.red.darker());
    c2 = (Color.yellow.darker());
    c3 = (Color.green.darker());
    c4 = (Color.blue.darker());
  }

  public MemoryUsage() {
    long mem = totalMem - Runtime.getRuntime().freeMemory();

    minMem = (mem < minMem) ? mem : minMem;
    maxMem = (mem > maxMem) ? mem : maxMem;

    if (count % f1 == 0) {
      ll1.addLast(new Long(mem));
      if (ll1.size() > history) ll1.removeFirst();
    }

    if (count % f2 == 0) {
      ll2.addLast(new Long(mem));
      if (ll2.size() > history) ll2.removeFirst();
    }

    if (count % (f1 * history) == 0) {
      long min = ((Long)Collections.min(ll1)).longValue();
      long max = ((Long)Collections.max(ll1)).longValue();
      long avg = (max + min) / 2;
      ll3.addLast(new Long(avg));
      if (ll3.size() > history) ll3.removeFirst();
    }

    if (count % (f2 * history) == 0) {
      long min = ((Long)Collections.min(ll2)).longValue();
      long max = ((Long)Collections.max(ll2)).longValue();
      long avg = (max + min) / 2;
      ll4.addLast(new Long(avg));
      if (ll4.size() > history) ll4.removeFirst();
    }
  
    count++;
  }

  public void grid(Graphics g) {
    int sw1 = g.getFontMetrics().stringWidth("000000000000");
    int sw2 = g.getFontMetrics().stringWidth("Maximal memory usage: 000000000000 bytes");
    int sh = g.getFontMetrics().getHeight();
    int topmargin = sh * 4;
    int bottommargin = getHeight() - sh;
    int height = bottommargin - topmargin;
    int w = getWidth();
    int mega = 1024 * 1024;
    int y;

    g.setColor(Color.blue.brighter());
    y = topmargin;
    g.drawLine(0, y, w - 1, y);
    g.drawString((totalMem / mega)+"M", 5, y - 1);
    y = (3 * topmargin + bottommargin) / 4;
    g.drawLine(0, y, w - 1, y);
    g.drawString(((totalMem * 3 / 4) / mega)+"M", 5, y - 1);
    y = (topmargin + bottommargin) / 2;
    g.drawLine(0, y, w - 1, y);
    g.drawString(((totalMem / 2) / mega)+"M", 5, y - 1);
    y = (topmargin + 3 * bottommargin) / 4;
    g.drawLine(0, y, w - 1, y);
    g.drawString(((totalMem / 4) / mega)+"M", 5, y - 1);
    y = bottommargin;
    g.drawLine(0, y, w - 1, y);
    g.drawString("0", 5, y - 1);

    g.setColor(Color.red.brighter());
    y = (int)(bottommargin - (maxMem * height) / totalMem);
    g.drawLine(0, y, w - 1, y);
    g.drawString(""+maxMem, w - sw1, y - 1);
    g.setColor(Color.green.darker());
    y = (int)(bottommargin - (minMem * height) / totalMem);
    g.drawLine(0, y, w - 1, y);
    g.drawString(""+minMem, w - sw1, y + sh);

    g.setColor(Color.black);
    g.drawString("Ran "+ count +" times.", 5, sh);
    g.drawString("x-axis: time", 5, sh * 2);
    g.drawString("y-axis: memory in use", 5, sh * 3);
    g.drawString("Minimal memory usage: "+ minMem +" bytes.", w - sw2 - 5, sh * 2);
    g.drawString("Maximal memory usage: "+ maxMem +" bytes.", w - sw2 - 5, sh * 3);
  }

  public void plot(LinkedList ll, Graphics g, Color c, String s) {
    int i = 0;
    int x0 = 0;
    int x1;
    int y0 = 0;
    int y1;
    int w = getWidth();
    int sw = g.getFontMetrics().stringWidth(s + "  ");
    int sh = g.getFontMetrics().getHeight();
        
    if (ll.size() > 0) {
      int topmargin = sh * 4;
      int bottommargin = getHeight() - sh;
      int height = bottommargin - topmargin;

      g.setColor(c);

      Iterator iterator = ll.iterator();

      if (iterator.hasNext()) {
        long n = ((Long)iterator.next()).longValue();
        y0 = (int)(bottommargin - n * height / totalMem);
      }

      while (iterator.hasNext()) {
        x1 = ((w - 1) * i) / history; 
        long n = ((Long)iterator.next()).longValue();
        y1 = (int)(bottommargin - (n * height) / totalMem);

        g.drawLine(x0, y0, x1, y1);
  
        x0 = x1;
        y0 = y1;
        ++i;
      }
    }

    g.drawString(s, x0 > sw ? x0 - sw : 0, y0);
  }  
  
  public void paint(Graphics g) {
    grid(g);
    plot(ll1, g, c1, "last " + (history * f1));
    plot(ll2, g, c2, "last " + (history * f2));
    plot(ll3, g, c3, "average over " + (history * f1));
    plot(ll4, g, c4, "average over " + (history * f2));
  }

  public String getHelpText() {
    return "Plots the memory usage into a chart.";
  }
}
