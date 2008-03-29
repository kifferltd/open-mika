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

import java.awt.peer.*;
import java.awt.*;

public class DefaultScrollPane extends DefaultContainer implements ScrollPanePeer {

  private int vx;
  private int vy;
  private int vwidth;
  private int vheight;

  public DefaultScrollPane(ScrollPane scrollpane) {
    super(scrollpane);
  }

  private native void setViewport(Component viewport, int vx, int vy, int vwidth, int vheight);

  private void refreshViewport() {
    Component comp;
    try {
      comp = ((ScrollPane) component).getComponent(0);
    } 
    catch(Exception e) {
      return;
    }

    Container viewport;

    if (comp instanceof Container) {
      viewport = (Container)comp;
    }
    else {
      viewport = comp.getParent();
    }

    if (vx + vwidth > viewport.getWidth()) {
      vx = viewport.getWidth() - vwidth;
    }

    if (vy + vheight > viewport.getHeight()) {
      vy = viewport.getHeight() - vheight;
    }
    
    setViewport(viewport, vx, vy, vwidth, vheight);

    ((DefaultComponent)viewport.getPeer()).refresh(DefaultComponent.REFRESH_GLOBAL);
    refresh(DefaultComponent.REFRESH_GLOBAL);
    
    viewport.invalidate();
    viewport.validate();
  }

  public void childResized(int w, int h) {
    vx = 0;
    vy = 0;
    vwidth = w;
    vheight = h;

    refreshViewport();
  }
  
  public int getHScrollbarHeight() {
    return 0;
  }
  
  public int getVScrollbarWidth() {
    return 0;
  }
  
  public void setScrollPosition(int x, int y) {
    vx = x;
    vy = y;

    refreshViewport();
  }
  
  public void setUnitIncrement(int increment) {
  }
  
  public void setValue(Adjustable adj, int value) {
    if (adj.getOrientation() == Adjustable.HORIZONTAL) {
      vx = value;
    }
    else {
      vy = value;
    }

    refreshViewport();
  }

  public Dimension getMinimumSize() {
    return new Dimension(0, 0);
  }

  public Dimension getPreferredSize() {
    Component comp = ((ScrollPane) component).getComponent(0);

    if (comp != null) {
      return new Dimension(comp.getPreferredSize().width, comp.getPreferredSize().height);
    }
    else {
      return new Dimension(0, 0);
    }
  }
  
}

