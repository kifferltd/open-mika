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
import java.util.*;

public class DefaultMenuBar extends DefaultMenuComponent implements MenuBarPeer {

  MenuWindow barwindow;
  Vector     items;
  Menu       helpmenu;

  public DefaultMenuBar(MenuBar menubar) {
    super(menubar);
    
    barwindow = new MenuWindow();
    barwindow.setLayout(new MenuBarLayout());
    items = new Vector();
  }

  public void addHelpMenu(Menu menu) {
    helpmenu = menu;
    items.add(menu);
    barwindow.add(((DefaultMenu)menu.getPeer()).getMenuItemComponent());
  }
  
  public void addMenu(Menu menu) {
    items.add(menu);
    barwindow.add(((DefaultMenu)menu.getPeer()).getMenuItemComponent());
  }
  
  public void delMenu(int pos) {
    Menu item = (Menu)items.get(pos);
    items.remove(pos);
    barwindow.remove(((DefaultMenu)item.getPeer()).getMenuItemComponent());
  }

  public Window getBarWindow() {
    return barwindow;
  }

  public void closeChildren() {
    for(int i=0; i < items.size(); i++) {
      Menu item = (Menu)items.get(i);
      DefaultMenu peer = (DefaultMenu)item.getPeer();
      peer.close();
      peer.closeChildren();
    }
  }
}

