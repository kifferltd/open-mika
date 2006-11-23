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

public class KeyboardButtonTextJapanese extends KeyboardButton {

  private int accents = 0;
  private String key = null;
  static private Font font = null;
  static private Font dflt = null;

  public KeyboardButtonTextJapanese(String key, int[] xPoints, int[] yPoints, int event, char keychar, int accents) {
    super(xPoints, yPoints, event, keychar);
    this.accents = accents;
    this.key = key;
    if(font == null) {
      font = new Font("nippon13", 0, 13);
    }      
  }

  public void paint_img(Graphics g) {
    super.paint_img(g);
  
    if(dflt == null) {
      dflt = g.getFont();
    }

    g.setFont(font);
    FontMetrics fm = g.getFontMetrics();
    g.setColor(Color.black);
    g.drawString(key, xPoints[0] + ((xPoints[1] - xPoints[0]) - fm.stringWidth(key)) / 2 + 1, yPoints[0] + 13);
    g.setFont(dflt);
  }

  public void paint(Graphics g) {
    super.paint(g);
  
    if(dflt == null) {
      dflt = g.getFont();
    }

    g.setFont(font);
    FontMetrics fm = g.getFontMetrics();
    
    if(pressed) {
      g.setColor(Color.white);
    }
    else {
      g.setColor(Color.black);
    }
    g.drawString(key, xPoints[0] + ((xPoints[1] - xPoints[0]) - fm.stringWidth(key)) / 2 + 1, yPoints[0] + 13);

    g.setFont(dflt);
  }

  public int getAccent() {
    return accents;
  }

}

