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

package java.awt;

import java.awt.image.*;

public abstract class Graphics {

  public abstract void clearRect(int x, int y, int w, int h);
  public abstract void clipRect(int x, int y, int w, int h);
  public abstract void copyArea(int x, int y, int w, int h, int deltax, int deltay);

  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public Graphics create() {
    return null;
  }
  
  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public Graphics create(int x, int y, int w, int h) { 
    return null;
  }
  
  public abstract void dispose();

  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public void draw3DRect(int x, int y, int w, int h, boolean raised) {
    return;
  }
  
  public abstract void drawArc(int x, int y, int w, int h, int startAngle, int arcAngle);
  
  /**
   * @status  temporary implementation : works, but very ineficient : there must be a better way
   * @remark  temporary implementation : works, but ineficient, replace by better algorithm asap
   */
  public void drawBytes(byte[] buffer, int offset, int count, int x, int y) { 
    drawString(new String(buffer,offset,count),x,y);
  }

  /**
   * @status  temporary implementation : works, but very ineficient : there must be a better way
   * @remark  temporary implementation : works, but ineficient, replace by better algorithm asap
   */
  public void drawChars(char[] buffer, int offset, int count, int x, int y) { 
    drawString(new String(buffer,offset,count),x,y);
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
   * @status  implemented
   * @remark  not compliant with specs: Polygon segments intersecting or outside the borders of the
   * drawing surface are not drawn.
   */
  public void drawPolygon(Polygon polygon) {
    drawPolygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
  }
  
  public abstract void drawPolygon(int[] x, int[] y, int n);
  public abstract void drawPolyline(int[] xPoints, int[] yPoints, int nPoints);

  /**
   * @status  implemented
   * @remark  not compliant with specs: rectangle sides intersecting or outside the borders of
   * the drawing surface are not drawn.
   */
  public void drawRect(int x, int y, int w, int h) {
    // remarks:
    // the cases w < 0 or h < 0 are ignored
    // the right border line is drawn at x+w, not at x+w-1, and the bottom border line
    // is drawn at y+h, not y+h-1;
    if (w==0 && h==0){
      drawLine(x,y,x,y);  // draw a single point.
    }
    else if (w >=0 && h >= 0) {
      drawLine(x, y, x + w, y);
      drawLine(x, y, x, y + h);
      drawLine(x + w, y, x + w, y + h);
      drawLine(x, y + h, x + w, y + h);
    }
  }
  
  public abstract void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight);

  public abstract void drawString(String string, int x, int y);

  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public void fill3DRect(int x, int y, int w, int h, boolean raised) {
  }
  
  public abstract void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle);
  public abstract void fillOval(int x, int y, int w, int h);
  
  /**
   * @status  implemented
   * @remark  compliant with the specification
   */
  public void fillPolygon(Polygon polygon) {
    fillPolygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
  }
   
  public abstract void fillPolygon(int[] x, int[] y, int n);
  public abstract void fillRect(int x, int y, int w, int h);
  public abstract void fillRoundRect(int x, int y, int w, int h, int arcWidth, int arcHeight);
  public abstract Shape getClip();
  public abstract Rectangle getClipBounds();
  public abstract Color getColor();
  public abstract Font getFont();

  public Rectangle getClipRect() {
    return getClipBounds();
  }
  
  public FontMetrics getFontMetrics() {
    return null;
  }
  
  public abstract FontMetrics getFontMetrics(Font font);
  public abstract void setClip(int x, int y, int w, int h);
  public abstract void setClip(Shape shape);
  public abstract void setColor(Color color);
  public abstract void setFont(Font font);
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
    return getClipRect().intersects(new Rectangle(x, y, width, height));
  }

  public void finalize() {
  }

}

