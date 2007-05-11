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

public class DefaultCheckboxMenuItem extends DefaultMenuItem implements CheckboxMenuItemPeer {

  public DefaultCheckboxMenuItem(CheckboxMenuItem cbmi) {
    super(cbmi);
  }

  public void setState(boolean state) {
    menuitemcomponent.paint(menuitemcomponent.getGraphics());
  }

  public void mouseClicked(MouseEvent event) {
    boolean state = !((CheckboxMenuItem)component).getState(); 
    component.dispatchEvent(new ItemEvent((ItemSelectable)component, ItemEvent.ITEM_STATE_CHANGED, component, state ? ItemEvent.SELECTED : ItemEvent.DESELECTED));
    super.mouseClicked(event);
  }
}

