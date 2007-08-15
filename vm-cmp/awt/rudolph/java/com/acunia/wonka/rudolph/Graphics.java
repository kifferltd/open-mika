/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
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

package com.acunia.wonka.rudolph;

import java.awt.image.*;
import java.awt.*;

public class Graphics extends java.awt.Graphics {

  private Component component;
  private Color foreground = null;
  private Color background = null;
  private Font  font = null;
  
  private int ox = 0;   // offset x
  private int oy = 0;   // offset y
  
  private int cx = 0;   // clip x
  private int cy = 0;   // clip y
  private int cw = -1;  // clip width
  private int ch = -1;  // clip height
  private boolean clip = false;

  public Graphics() {
    cw = -1;
    ch = -1; 
    clip = false;
  }

  public void clearRect(int x, int y, int w, int h) {
    Color temp = foreground;
    foreground = background;
    fillRect(x, y, w, h);
    foreground = temp;
  }
  
  public void clipRect(int x, int y, int w, int h) {
    if(cw > 0 && ch > 0) {
      if(x < cx) {
        w -= cx - x;
        x = cx;
      }
      if(y < cy) {
        h -= cy - y;
        y = cy;
      }
      if(x + w > cx + cw) w = cx + cw - x;
      if(y + h > cy + ch) h = cy + ch - y;
    }
    setClip(x, y, w, h);
  }
  
  native public void copyArea(int x, int y, int w, int h, int deltax, int deltay);

  public java.awt.Graphics create() {
    return this;
  }
  
  public java.awt.Graphics create(int x, int y, int w, int h) {
    return this;
  }
  
  public void dispose() {
  }

  native private void fillArc2(int x, int y, int w, int h, int xb, int yb, int xe, int ye);
  public void fillArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
    fillArc2(x + w/2, y + h/2, w/2, h/2, 
        (int)Math.floor(Math.cos(-startAngle * Math.PI / 180) * w/2),
        (int)Math.floor(Math.sin(-startAngle * Math.PI / 180) * h/2),
        (int)Math.floor(Math.cos((-startAngle - arcAngle) * Math.PI / 180) * w/2),
        (int)Math.floor(Math.sin((-startAngle - arcAngle) * Math.PI / 180) * h/2));
  }
  
  native private void drawArc2(int x, int y, int w, int h, int xb, int yb, int xe, int ye);
  public void drawArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
    drawArc2(x + w/2, y + h/2, w/2, h/2, 
        (int)Math.floor(Math.cos(-startAngle * Math.PI / 180) * w/2),
        (int)Math.floor(Math.sin(-startAngle * Math.PI / 180) * h/2),
        (int)Math.floor(Math.cos((-startAngle - arcAngle) * Math.PI / 180) * w/2),
        (int)Math.floor(Math.sin((-startAngle - arcAngle) * Math.PI / 180) * h/2));
  }
  
  native public boolean drawImage(java.awt.Image image, int dx1, int dy1, ImageObserver observer);
  native public boolean drawImage(java.awt.Image image, int dx1, int dy1, Color bg, ImageObserver observer);
  native public boolean drawImage(java.awt.Image image, int dx1, int dy1, int w, int h, ImageObserver observer);
  native public boolean drawImage(java.awt.Image image, int dx1, int dy1, int w, int h, Color bg, ImageObserver observer);
  native public boolean drawImage(java.awt.Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer);
  native public boolean drawImage(java.awt.Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bg, ImageObserver observer);

  native private int getBufferWidth();
  native private int getBufferHeight();
  
  native public void drawLine(int x1, int y1, int x2, int y2);
  native public void drawOval(int x, int y, int width, int height);
  native public void drawPolygon(int[] x, int[] y, int n);
  

  public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
    for(int i=0; i < nPoints - 1; i++) {
      drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
    }
  }
  
  native public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight);
  native public void drawString(String string, int x, int y);
  native public void fillOval(int x, int y, int w, int h);
  native public void fillPolygon(int[] x, int[] y, int n);
  native public void fillRect(int x, int y, int w, int h);
  native public void fillRoundRect(int x, int y, int w, int h, int arcWidth, int arcHeight);
  
  public Shape getClip() {
    return (Shape) getClipRect(); // all the same for now
  }
  
  public Rectangle getClipBounds() {

    /*
    ** MB: I believe there to be a bug in the JDK specification: this 
    ** function is meant to return 'null' if no clip rectangle is set
    ** but Netclue (a Java web browser) dies during painting if this
    ** happens.  When the same call is traced in Sun's Java during the
    ** same painting process, the clip window is set to the size of the
    ** whole window if none is set; null never seems to be returned. Hence
    ** I'm trying to replicate that behaviour.
    */

    return (clip ? new Rectangle(cx, cy, cw, ch) : new Rectangle(0, 0, getBufferWidth(), getBufferHeight()));
  }

  public Color getColor() {
    return foreground;
  }
  
  public Font getFont() {
     return font;
  }

  public FontMetrics getFontMetrics() {
    return new FontMetrics((font == null) ? Component.DEFAULT_FONT : font);
  }
  
  public FontMetrics getFontMetrics(Font font) {
    return new FontMetrics((font == null) ? Component.DEFAULT_FONT : font);
  }
  
  public void setClip(int x, int y, int w, int h) {
    cx = x;
    cy = y;
    cw = w;
    ch = h;
    clip = true;
  }
  
  public void setClip(Shape shape) {
    if(shape == null) {
      cx = 0;
      cy = 0;
      cw = -1;
      ch = -1;
      clip = false;
      return;
    }

    Rectangle r = shape.getBounds();
    setClip(r.x, r.y, r.width, r.height);
  }
  
  public void setColor(Color color) {
    if (color != null) {
      foreground = color;
    }
  }
  
  public void setFont(Font font) {
    if (font != null) {
      this.font = font;
    }
  }
  
  public void setPaintMode() {
    System.out.println("[Graphics] setPaintMode not implemented");
  }
  
  public void setXORMode(Color color) {
    System.out.println("[Graphics] setXORMode not implemented");
  }
  
  public void translate(int x, int y) {
    ox += x;
    oy += y;
    cx -= x;
    cy -= y;
  }

}

