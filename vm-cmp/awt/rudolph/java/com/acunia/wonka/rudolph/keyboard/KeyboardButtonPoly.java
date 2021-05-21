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

public class KeyboardButtonPoly extends KeyboardButton {

  private int[] xs;
  private int[] ys;

  public KeyboardButtonPoly(int[] xs, int[] ys, int[] xPoints, int[] yPoints, int event, char keychar) {
    super(xPoints, yPoints, event, keychar);
    this.xs = xs;
    this.ys = ys;

    for(int i = 0; i < xs.length; i++) {
      xs[i] += xPoints[0];
      ys[i] += yPoints[0];
    }
  }

  public void setScale(int width, int height, int oldwidth, int oldheight) {
    if(this.width != width || this.height != height) {
      super.setScale(width, height, oldwidth, oldheight);
      for(int i=0; i < xs.length; i++) {
        xs[i] = xs[i] * width / oldwidth;
        ys[i] = ys[i] * height / oldheight;
      }
    }
  }
  
  public void setTranslate(int x, int y) {
    super.setTranslate(x, y);
    for(int i=0; i < xs.length; i++) {
      xs[i] += x;
      ys[i] += y;
    }
  }

  public void paint_img(Graphics g) {
    super.paint_img(g);
    g.setColor(Color.black);
    g.fillPolygon(xs, ys, xs.length);
    g.drawPolygon(xs, ys, xs.length);
  }

  public void paint(Graphics g) {
    super.paint(g);
    if(pressed) {
      g.setColor(Color.white);
    }
    else {
      g.setColor(Color.black);
    }
    g.fillPolygon(xs, ys, xs.length);
    g.drawPolygon(xs, ys, xs.length);
  }

}

