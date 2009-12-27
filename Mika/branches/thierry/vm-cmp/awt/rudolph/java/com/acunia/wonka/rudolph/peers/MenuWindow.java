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

package com.acunia.wonka.rudolph.peers;

import java.awt.*;

public class MenuWindow extends Window {

  public MenuWindow() {
  }

  public Insets getInsets() {
    return new Insets(2, 2, 2, 2);
  }

  public void paint(Graphics g) {
    int w = getSize().width;
    int h = getSize().height;

    g.setColor(new Color(100, 100, 100));
    
    g.drawLine(w - 1, 0, w - 1, h - 1);
    g.drawLine(0, h - 1, w - 1, h - 1);

    g.setColor(new Color(255, 255, 255));
    
    g.drawLine(0, 0, w, 0);
    g.drawLine(0, 0, 0, h);

  }

}

