/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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

