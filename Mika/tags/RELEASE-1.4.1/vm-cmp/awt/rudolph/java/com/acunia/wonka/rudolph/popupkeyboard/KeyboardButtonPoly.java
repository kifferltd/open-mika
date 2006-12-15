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

