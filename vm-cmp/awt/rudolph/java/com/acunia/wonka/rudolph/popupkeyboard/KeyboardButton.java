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

