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
import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class DefaultMenu extends DefaultMenuItem implements MenuPeer, FocusListener {

  public static boolean menuOpened = false;
  public static Menu lastMenuOpened = null;

  MenuWindow menuwindow;
  Vector items;
  
  public DefaultMenu(Menu menu) {
    super(menu);

    menuwindow = new MenuWindow();
    menuwindow.setLayout(new MenuLayout());
    menuwindow.addFocusListener(this);
    items = new Vector();
  }

  public void addItem(MenuItem menuItem) {
    items.add(menuItem);
    menuwindow.add(((DefaultMenuItem)menuItem.getPeer()).getMenuItemComponent());
  }
  
  public void addSeparator() {
    MenuItem item = new MenuItem("-");
    items.add(item);
    menuwindow.add(((DefaultMenuItem)item.getPeer()).getMenuItemComponent());
  }
  
  public void delItem(int pos) {
    MenuItem item = (MenuItem)items.get(pos);
    items.remove(pos);
    menuwindow.remove(((DefaultMenuItem)item.getPeer()).getMenuItemComponent());
  }

  public void show() {
    menuOpened = true;
    lastMenuOpened = (Menu)component;
    closeChildren();
    menuwindow.pack();
    menuwindow.show();
    menuwindow.requestFocus();
  }

  public void focusGained(FocusEvent e) {
  }

  public void focusLost(FocusEvent e) {
  }

  public void closeParents() {
    MenuContainer container = ((MenuItem)component).getParent();
    if(container instanceof Menu) {
      DefaultMenu peer = (DefaultMenu)((Menu)container).getPeer();
      peer.closeParents();
      peer.close();
    }
  }

  public void closeChildren() {
    for(int i=0; i < items.size(); i++) {
      MenuItem item = (MenuItem)items.get(i);
      if(item instanceof Menu) {
        DefaultMenu peer = (DefaultMenu)((Menu)item).getPeer();
        peer.close();
        peer.closeChildren();
      }
    }
  }

  public void close() {
    menuwindow.setVisible(false);
    closeChildren();
  }

  public void mouseClicked(MouseEvent event) {

    /*
    ** Close possible other children.
    */

    MenuContainer container = ((MenuItem)component).getParent();
    if(container instanceof Menu) {
      DefaultMenu peer = (DefaultMenu)((Menu)container).getPeer();
      peer.closeChildren();
    }
    else if(container instanceof MenuBar) {
      DefaultMenuBar peer = (DefaultMenuBar)((MenuBar)container).getPeer();
      peer.closeChildren();
    }
    
    /* 
    ** Open window 
    */

    Point pos = menuitemcomponent.getLocationOnScreen();
  
    if(container instanceof Menu) {
      menuwindow.setLocation(pos.x + menuitemcomponent.getSize().width + 2, pos.y);
    }
    else if(container instanceof MenuBar) {
      menuwindow.setLocation(pos.x, pos.y + menuitemcomponent.getSize().height + 2);
    }
    
    show();
  }
  
}

