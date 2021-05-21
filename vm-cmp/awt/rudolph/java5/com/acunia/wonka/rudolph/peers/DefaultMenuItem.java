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
import java.awt.peer.*;
import java.awt.event.*;

public class DefaultMenuItem extends DefaultMenuComponent implements MenuItemPeer, MouseListener {

  MenuItemComponent menuitemcomponent;

  public DefaultMenuItem(MenuItem menuItem) {
    super(menuItem);
  }

  public void setEnabled() {
  }
  
  public void setDisabled() {
  }

  /*
  ** Deprecated.
  */
  
  public void enable() {
  }
  
  public void disable() {
  }

  /*
  ** Other
  */

  public Component getMenuItemComponent() {
    if(menuitemcomponent == null) {
      menuitemcomponent = new MenuItemComponent((MenuItem)component);
      menuitemcomponent.addMouseListener(this);
    }
    return menuitemcomponent;
  }

  public void mouseClicked(MouseEvent event) {
    MenuContainer container = ((MenuItem)component).getParent();
    if(container instanceof Menu) {
      DefaultMenu peer = (DefaultMenu)((Menu)container).getPeer();
      peer.closeChildren();
      peer.close();
      peer.closeParents();
    }
    DefaultMenu.menuOpened = false;
    component.dispatchEvent(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, ((MenuItem)component).getActionCommand()));
  }
  
  public void mouseEntered(MouseEvent event) {
  }
  
  public void mouseExited(MouseEvent event) {
  }
  
  public void mousePressed(MouseEvent event) {
  }
  
  public void mouseReleased(MouseEvent event) {
  }
  
}

