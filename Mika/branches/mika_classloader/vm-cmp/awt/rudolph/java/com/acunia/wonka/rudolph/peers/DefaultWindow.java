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

import com.acunia.wonka.rudolph.WindowManager;
import java.awt.peer.*;
import java.awt.*;

public class DefaultWindow extends DefaultContainer implements WindowPeer {

  protected static WindowManager wm = WindowManager.getInstance();
  private boolean settingBounds = false;

  native protected void relocatePeer();
  native protected void disposePeer();
  native public void toBack0();    
  native public void toFront0();

  public DefaultWindow(Window window) {
    super(window);
    wm.addWindow(window);
  }

  public void setBounds(int x, int y, int w, int h) {
    if(settingBounds) return;
    
    settingBounds = true;
    Rectangle dim = wm.checkBounds((Window)component, x, y, w, h);
    
    ((Window)component).setBounds(dim.x, dim.y, dim.width, dim.height);
    super.setBounds(dim.x, dim.y, dim.width, dim.height);
    
    relocatePeer();
    settingBounds = false;
  } 

  public void setVisible(boolean visible) {
    if (wm != null) {
      wm.setVisible((Window)component, visible);
    }

    if (visible == false) {
      relocatePeer();
    }
  }

  public void dispose() {
    wm.removeWindow((Window)component);
  }

  public void finalize() {
    disposePeer();
    super.finalize();
  }

  public void toBack() {
    toBack0();
    wm.toBack((Window)component);
  }
  
  public void toFront() {
    toFront0();
    wm.toFront((Window)component);
  }
}

