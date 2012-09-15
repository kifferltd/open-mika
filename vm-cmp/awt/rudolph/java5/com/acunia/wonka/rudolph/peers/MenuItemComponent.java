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

public class MenuItemComponent extends Component {

  MenuItem menuitem;

  public MenuItemComponent(MenuItem menuitem) {
    this.menuitem = menuitem;
  }

  public Dimension getPreferredSize() {
    Dimension preferredSize;
    String label = menuitem.getLabel();
    if(label.equals("-")) {
      preferredSize = new Dimension(20, 4);
    }
    else {
      synchronized(getTreeLock()) {
        Font f = menuitem.getFont();
        if(f != null) setFont(f);
        FontMetrics fm = getFontMetrics((f != null) ? f : Component.DEFAULT_FONT);

        int cx = fm.stringWidth(label) + 10;
        int cy = (4 * fm.getHeight()) / 3;

        if(menuitem instanceof Menu && menuitem.getParent() instanceof Menu) {
          cx += 15;  // Extra space for the '>' 
        }
        
        if(menuitem instanceof CheckboxMenuItem) {
          cx += 15;  // Extra space for the toggle button. 
        }
        
        preferredSize = new Dimension(cx, cy);
      }
    }
    return preferredSize;
  }

  public void paint(Graphics g) {
    String text = menuitem.getLabel();
    
    if(text.equals("-")) {
      int w = getSize().width;
      int h = getSize().height;
     
      g.setColor(new Color(100, 100, 100));
      g.drawLine(0, h/2 - 1, w - 2, h/2 - 1);
      
      g.setColor(new Color(255, 255, 255));
      g.drawLine(1, h/2, w - 1, h/2);
    }
    else {
      Font f = menuitem.getFont();
      if(f != null) setFont(f);

      int x = 0;
      int y = (getSize().height - g.getFontMetrics().getHeight()) / 2;
      int w = getSize().width;
      int h = getSize().height;
      int ystr = g.getFontMetrics().getAscent();

      /*
      ** Check if this is a menu and draw an indication (>)
      */
      
      if(menuitem instanceof Menu && menuitem.getParent() instanceof Menu) {
        g.drawString(">", w - 10, y + ystr);
      }
      
      /*
      ** Check if this is a checkboxmenuitem and draw a button
      */

      if(menuitem instanceof CheckboxMenuItem) {
        int br = h / 2;
        int by = y + h / 4;
        int bx = x;
      
        if(((CheckboxMenuItem)menuitem).getState()) {
          g.setColor(Color.black);
          g.drawLine(bx, by, bx + br - 1, by);
          g.drawLine(bx, by, bx, by + br - 1);

          g.setColor(Color.white);
          g.drawLine(bx + br - 1, by, bx + br - 1, by + br - 1);
          g.drawLine(bx, by + br - 1, bx + br - 1, by + br - 1);

          g.setColor(new Color(100, 100, 100));
          g.fillRect(bx + 1, by + 1, br - 2, br - 2);
        }
        else {
          g.setColor(Color.white);
          g.drawLine(bx, by, bx + br - 1, by);
          g.drawLine(bx, by, bx, by + br - 1);

          g.setColor(Color.black);
          g.drawLine(bx + br - 1, by, bx + br - 1, by + br - 1);
          g.drawLine(bx, by + br - 1, bx + br - 1, by + br - 1);

          g.setColor(new Color(200, 200, 200));
          g.fillRect(bx + 1, by + 1, br - 2, br - 2);
        }

        /*
        ** Move the text to the right.
        */

        x += br + 2;
        
      }

      /*
      ** Draw the text.
      */
      
      g.setClip(x, y, w, h);

      g.setColor(Color.black);
      g.drawString(text, x , y + ystr);

    }
  }

}

