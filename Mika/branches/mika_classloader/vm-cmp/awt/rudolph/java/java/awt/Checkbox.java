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

package java.awt;

import java.awt.event.*;
import java.awt.peer.*;

public class Checkbox extends Component implements java.io.Serializable, ItemSelectable {

  String label;
  CheckboxGroup group;
  boolean state;
  ItemListener itemListener;
  
  private boolean isGroup;
  private CheckboxPeer peer;
 
  public Checkbox() {
    this("", false, null);
  }
  
  public Checkbox(String label) {
    this(label, false, null);
  }

  public Checkbox(String label, boolean state) {
    this(label, state, null);
  }

  public Checkbox(String label, CheckboxGroup group, boolean state) {
    this(label, state, group);
  }

  public Checkbox(String label, boolean state, CheckboxGroup group) {
    setLabel(label);
    setState(state);
    setCheckboxGroup(group);
    
    if (group != null) {
      if (state == true || group.getSelectedCheckbox() == null) {
        group.setSelectedCheckbox(this);
      }
    }

    addNotify();
  }

  public synchronized void addItemListener(ItemListener listener) {
    itemListener = AWTEventMulticaster.add(itemListener, listener);
  }

  public synchronized void removeItemListener(ItemListener listener) {
    itemListener = AWTEventMulticaster.remove(itemListener, listener);
  }

  protected void processEvent(AWTEvent event) {
    if (event instanceof ItemEvent) {
      processItemEvent((ItemEvent) event);
    }
    else {
      super.processEvent(event);
    }
  }

  protected void processItemEvent(ItemEvent event) {
    if (this.itemListener != null) {
      itemListener.itemStateChanged(event);
    }
  }

  public void addNotify() {
    if(peer == null) {
      peer = getToolkit().createCheckbox(this);
    }
    
    if (notified == false) {
      super.addNotify();
    }
  }

  public void setCheckboxGroup(CheckboxGroup group) {
    if (group != null) {
      this.group = group;
      if(state) {
        group.setSelectedCheckbox(this);
      }
    }
    else {
      this.group = null;
    }
    
    peer.setCheckboxGroup(group);
  }

  public CheckboxGroup getCheckboxGroup() {
    return group;
  }

  public void setLabel(String label) {

    if (this.label != label) {
      this.label = label;

      valid = false;

      peer.setLabel(label);
    }
  }

  public String getLabel() {
    return label;
  }

  public void setState(boolean state) {
  
    CheckboxGroup group = this.group;
  
    if (group != null) {
      if (state == false) {
        if (group.getSelectedCheckbox() == this) {
          state = true;
        }
      }
      else {
        if (group.getSelectedCheckbox() != this) {
          group.setSelectedCheckbox(this);
        }
      }
    }

    this.state = state;
  
    peer.setState(state);
  }

  public boolean getState() {
    return state;
  }

  public Object[] getSelectedObjects() {
    Object[] objects;

    if (state == true) {
      objects = new Object[1];
      objects[0] = label;
    }
    else {
      objects = null; 
    }

    return objects;
  }

  public String toString() {
    return getClass().getName() +" - label: "+ label +", bounds: x = "+ x +", y = "+ y +", w = "+ width +", h = "+ height;
  }

  protected String paramString(){
    return getClass().getName() +" label: "+ label +", bounds:("+ x +", "+ y +", "+ width +", "+ height+") group:"+group;
  }
}
