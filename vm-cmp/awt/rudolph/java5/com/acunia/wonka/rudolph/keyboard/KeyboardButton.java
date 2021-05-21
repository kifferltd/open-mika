/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

package com.acunia.wonka.rudolph.keyboard;

import java.awt.*;

public class KeyboardButton {

  protected int[]  xPoints;
  protected int[]  yPoints;
  protected int    event;
  protected char   keychar;
  protected int    width;
  protected int    height;
  protected int    xtrans;
  protected int    ytrans;

  boolean  pressed = false;
  
  public KeyboardButton(int[] xPoints, int[] yPoints, int event, char keychar) {
    this.xPoints = xPoints;
    this.yPoints = yPoints;
    this.event = event;
    this.keychar = keychar;
  }

  public void paint_img(Graphics g) {
    g.setColor(Color.white);
    g.fillPolygon(xPoints, yPoints, xPoints.length);

    g.setColor(Color.black);
    g.drawPolygon(xPoints, yPoints, xPoints.length);
  }

  public void paint(Graphics g) {
    if(pressed) {
      g.setColor(Color.black);
      g.fillPolygon(xPoints, yPoints, xPoints.length);
      g.drawPolygon(xPoints, yPoints, xPoints.length);
    }
    else {
      g.setColor(Color.white);
      g.fillPolygon(xPoints, yPoints, xPoints.length);
   
      g.setColor(Color.black);
      g.drawPolygon(xPoints, yPoints, xPoints.length);
    }
  }

  public void setScale(int width, int height, int oldwidth, int oldheight) {
    if(this.width != width || this.height != height) {
      this.width = width;
      this.height = height;
      for(int i=0; i < xPoints.length; i++) {
        xPoints[i] = xPoints[i] * width / oldwidth;
        yPoints[i] = yPoints[i] * height / oldheight;
      }
    }
  }

  public void setTranslate(int x, int y) {
    for(int i=0; i < xPoints.length; i++) {
      xPoints[i] += x - xtrans;
      yPoints[i] += y - ytrans;
    }
    xtrans = x;
    ytrans = y;
  }

  public int[] getXPoints() { return xPoints; }
  public int[] getYPoints() { return yPoints; }

  public boolean contains(int x, int y) {
    return (new Polygon(xPoints, yPoints, xPoints.length)).contains(x, y);
  }

  public void setPressed(boolean pressed) {
    this.pressed = pressed;
  }

  public boolean getPressed() {
    return pressed;
  }

  public int getKeyEvent() {
    return event;
  }

  public char getKeyChar() {
    return keychar;
  }

}

