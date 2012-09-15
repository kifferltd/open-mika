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

public class KeyboardButtonPoly extends KeyboardButton {

  private int[] xs;
  private int[] ys;
  private boolean translated = false;

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
    if(!translated) {
      translated = true;
      super.setTranslate(x, y);
      for(int i=0; i < xs.length; i++) {
        xs[i] += x;
        ys[i] += y;
      }
    }
  }

  public void paint_img(Graphics g) {
    super.paint(g);
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

