package com.acunia.wonka.rudolph.popupkeyboard;

import java.awt.*;

public class KeyboardButtonText extends KeyboardButton {

  protected String key;

  public KeyboardButtonText(String key, int[] xPoints, int[] yPoints, int event, char keychar) {
    super(xPoints, yPoints, event, keychar);
    this.key = key;
  }

  public void paint_img(Graphics g) {
    super.paint_img(g);
    
    FontMetrics fm = g.getFontMetrics();
    
    g.setColor(Color.black);
    g.drawString(key, xPoints[0] + ((xPoints[1] - xPoints[0]) - fm.stringWidth(key)) / 2 + 1, 
                      yPoints[0] + ((yPoints[2] - yPoints[0]) - (fm.getAscent())) / 2 + fm.getAscent());
  }

  public void paint(Graphics g) {
    super.paint(g);
    
    FontMetrics fm = g.getFontMetrics();
    
    if(pressed) {
      g.setColor(Color.white);
    }
    else {
      g.setColor(Color.black);
    }
    g.drawString(key, xPoints[0] + ((xPoints[1] - xPoints[0]) - fm.stringWidth(key)) / 2 + 1, 
                      yPoints[0] + ((yPoints[2] - yPoints[0]) - (fm.getAscent())) / 2 + fm.getAscent());
  }

}

