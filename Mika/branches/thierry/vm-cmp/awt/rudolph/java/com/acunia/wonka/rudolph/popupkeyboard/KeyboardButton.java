/**************************************************************************
* Copyright (c) 2003 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package com.acunia.wonka.rudolph.popupkeyboard;

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
  private Polygon  polygon;

  boolean  pressed = false;
  
  public KeyboardButton(int[] xPoints, int[] yPoints, int event, char keychar) {
    this.xPoints = xPoints;
    this.yPoints = yPoints;
    this.event = event;
    this.keychar = keychar;
    polygon = new Polygon(xPoints, yPoints, xPoints.length);
  }

  public void paint_img(Graphics g) {
    g.setColor(Color.white);
    g.fillPolygon(xPoints, yPoints, xPoints.length);

    g.setColor(Color.black);
    g.drawPolygon(polygon);
  }

  public void paint(Graphics g) {
    if(pressed) {
      g.setColor(Color.black);
      g.fillPolygon(polygon);
      g.drawPolygon(polygon);
    }
    else {
      g.setColor(Color.white);
      g.fillPolygon(polygon);
   
      g.setColor(Color.black);
      g.drawPolygon(polygon);
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
      polygon = new Polygon(xPoints, yPoints, xPoints.length);
    }
  }

  public void setTranslate(int x, int y) {
    for(int i=0; i < xPoints.length; i++) {
      xPoints[i] += x - xtrans;
      yPoints[i] += y - ytrans;
    }
    xtrans = x;
    ytrans = y;
    polygon = new Polygon(xPoints, yPoints, xPoints.length);
  }

  public int[] getXPoints() { return xPoints; }
  public int[] getYPoints() { return yPoints; }

  public boolean contains(int x, int y) {
    return polygon.contains(x, y);
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

