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
