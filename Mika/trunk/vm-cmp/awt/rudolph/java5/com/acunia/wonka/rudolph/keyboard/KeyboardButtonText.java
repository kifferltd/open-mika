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

