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

import java.awt.peer.*;
import java.awt.event.*;
import java.io.*;
  
public class CheckboxMenuItem extends MenuItem implements ItemSelectable {

  boolean state;
  int checkboxMenuItemSerializedDataVersion;
  
  transient ItemListener itemListener;

  public CheckboxMenuItem() {
    this(null);
  }

  
  public CheckboxMenuItem(String label) {
    this(label, false);
  }

  public CheckboxMenuItem(String label, boolean state) {
    super(label);
    this.state = state;
  }

  public boolean getState() {
    return state;
  }
  
  public synchronized void setState(boolean state) {
    this.state = state;
    ((CheckboxMenuItemPeer)peer).setState(state);
  }
  
  public Object[] getSelectedObjects() {
    if(state) {
      return new Object[]{ getLabel() };
    }
    else {
      return null;
    }
  }
  
  public synchronized void addItemListener(java.awt.event.ItemListener listener) {
    itemListener = AWTEventMulticaster.add(itemListener, listener);
  }

  protected void processEvent(AWTEvent event) {
    if (event instanceof ItemEvent) {
      processItemEvent((ItemEvent)event);
    }
    else {
      super.processEvent(event);
    }
  }
  
  protected void processItemEvent(java.awt.event.ItemEvent event) {
    if (itemListener != null) {
      setState(event.getStateChange() == ItemEvent.SELECTED);
      itemListener.itemStateChanged(event);
    }
  }
  
  public synchronized void removeItemListener(java.awt.event.ItemListener listener) {
    itemListener = AWTEventMulticaster.remove(itemListener, listener);
  }
  
  public void addNotify() {
    if(peer == null) {
      peer = Toolkit.getDefaultToolkit().createCheckboxMenuItem(this);
    }
  }

  public String paramString() {
    return "java.awt.CheckboxMenuItem";
  }
  
  private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
  }
  
  private void writeObject(ObjectOutputStream s) throws ClassNotFoundException, IOException {
  }

}
