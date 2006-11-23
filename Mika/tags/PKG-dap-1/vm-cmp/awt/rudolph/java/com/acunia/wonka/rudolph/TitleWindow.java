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

package com.acunia.wonka.rudolph;

import java.awt.*;
import java.awt.event.*;

public class TitleWindow extends DecorationWindow implements MouseListener, MouseMotionListener {
  
  private Label titlebar;
  private Window win;
  private Point lastClick = new Point();
  private boolean pressed = false;
  
  public TitleWindow(Window win) {
    titlebar = new Label();
    titlebar.setForeground(SystemColor.activeCaptionText);
    titlebar.setBackground(SystemColor.activeCaption);
    add(titlebar, BorderLayout.CENTER);
    this.win = win;
    
    titlebar.addMouseListener(this);
    titlebar.addMouseMotionListener(this);
  }
  
  public void setTitle(String title) {
    titlebar.setText(title != null ? "  " + title : "");
  }

  public Insets getInsets() {
    return new Insets(1, 1, 1, 1);
  }

  public void mouseClicked(MouseEvent event) {
  }
  
  public void mouseEntered(MouseEvent event) {
  }
  
  public void mouseExited(MouseEvent event) {
  }
  
  public void mouseMoved(MouseEvent event) {
  }
  
  public void mousePressed(MouseEvent event) {
    pressed = true;
    lastClick.setLocation(event.getX(), event.getY());
  }

  public void mouseReleased(MouseEvent event) {
    pressed = false;
  }

  public void mouseDragged(MouseEvent event) {
    if(pressed) {
      int x1 = lastClick.x;
      int y1 = lastClick.y;
      int x2 = event.getX();
      int y2 = event.getY();
      int w = win.getSize().width;
      int h = win.getSize().height;
      int ox = win.getLocationOnScreen().x;
      int oy = win.getLocationOnScreen().y;
      win.setBounds(ox + x2 - x1, oy + y2 - y1, w, h);
    }
  }
}

