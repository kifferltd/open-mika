/**************************************************************************
* Copyright (c) 2012 by Chris Gray. All rights reserved.                  *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Chris Gray nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL CHRIS GRAY OR OTHER CONTRIBUTORS BE LIABLE FOR        *
* ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR                *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package java.awt;

import java.awt.image.ImageObserver;

/**
 ** Dummy implementation.
 */
public abstract class Graphics {

  public abstract void clearRect(int x, int y, int w, int h);
  public abstract void clipRect(int x, int y, int w, int h);
  public abstract void copyArea(int x, int y, int w, int h, int deltax, int deltay);

  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public Graphics create() {
    throw new HeadlessException();
  }
  
  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public Graphics create(int x, int y, int w, int h) { 
    throw new HeadlessException();
  }
  
  public abstract void dispose();

  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public void draw3DRect(int x, int y, int w, int h, boolean raised) {
    throw new HeadlessException();
  }
  
  public abstract void drawArc(int x, int y, int w, int h, int startAngle, int arcAngle);
  
  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public void drawBytes(byte[] buffer, int offset, int count, int x, int y) { 
    throw new HeadlessException();
  }

  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public void drawChars(char[] buffer, int offset, int count, int x, int y) { 
    throw new HeadlessException();
  }

  public abstract boolean drawImage(Image image, int dx1, int dy1, ImageObserver observer);
  public abstract boolean drawImage(Image image, int dx1, int dy1, Color bg, ImageObserver observer);
  public abstract boolean drawImage(Image image, int dx1, int dy1, int w, int h, ImageObserver observer);
  public abstract boolean drawImage(Image image, int dx1, int dy1, int w, int h, Color bg, ImageObserver observer);
  public abstract boolean drawImage(Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer);
  public abstract boolean drawImage(Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bg, ImageObserver observer);

  public abstract void drawLine(int x1, int y1, int x2, int y2);
  public abstract void drawOval(int x, int y, int width, int height);
  
  /**
   * @status  not implemented
   * @remark  not implemented
   * the drawing surface are not drawn.
   */
  public void drawRect(int x, int y, int w, int h) {
    throw new HeadlessException();
  }
  
  public abstract void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight);

  public abstract void drawString(String string, int x, int y);

  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public void fill3DRect(int x, int y, int w, int h, boolean raised) {
    throw new HeadlessException();
  }
  
  public abstract void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle);
  public abstract void fillOval(int x, int y, int w, int h);
  
  public abstract void fillPolygon(int[] x, int[] y, int n);
  public abstract void fillRect(int x, int y, int w, int h);
  public abstract void fillRoundRect(int x, int y, int w, int h, int arcWidth, int arcHeight);
  public abstract Shape getClip();
  public abstract Rectangle getClipBounds();
  public abstract Color getColor();

  public Rectangle getClipRect() {
    throw new HeadlessException();
  }
  
  public abstract void setClip(int x, int y, int w, int h);
  public abstract void setClip(Shape shape);
  public abstract void setColor(Color color);
  public abstract void setPaintMode();
  public abstract void setXORMode(Color color);

  /**
   * @status  implemented
   * @remark  implemented
   */
  public String toString() {
    return "Graphics";
  }
  
  public abstract void translate(int x, int y);

  public boolean hitClip(int x, int y, int width, int height) {
    throw new HeadlessException();
  }

  public void finalize() {
  }

}


