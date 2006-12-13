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
import java.io.*;
import java.awt.event.*;

public class MenuItem extends MenuComponent {

  /*
  ** Fields needed for Serialization.
  */

  private boolean enabled;
  private String label;
  private String actionCommand;
  private long eventMask;
  private MenuShortcut shortcut;
  private int menuItemSerializedDataVersion;
  
  transient ActionListener actionListener;

  public MenuItem() {
    this(null);
  }
  
  public MenuItem(String label) {
    this(label, null);
  }
  
  public MenuItem(String label, MenuShortcut shortcut) {
    super();
    this.label = label;
    this.shortcut = shortcut;
    
  }

  public boolean isEnabled() {
    return enabled;
  }
  
  public synchronized void setEnabled(boolean condition) {
    enabled = condition;
    if(enabled) {
      ((MenuItemPeer)peer).setEnabled();
    }
    else {
      ((MenuItemPeer)peer).setDisabled();
    } 
  }
  
  public String getLabel() {
    return label;
  }
  
  public synchronized void setLabel(String label) {
    this.label = label;
  }
  
  public void deleteShortcut() {
    shortcut = null;
  }
  
  public MenuShortcut getShortcut() {
    return shortcut;
  }
  
  public void setShortcut(MenuShortcut shortcut) {
    this.shortcut = shortcut;
  }

  public synchronized void addActionListener(java.awt.event.ActionListener listener) {
    actionListener = AWTEventMulticaster.add(actionListener, listener);
  }

  protected final void enableEvents(long eventTypes) {
  }
  
  protected final void disableEvents(long eventTypes) {
  }
  
  public String getActionCommand() {
    return actionCommand;
  }
  
  protected void processActionEvent(java.awt.event.ActionEvent event) {
    if (actionListener != null) {
      actionListener.actionPerformed(event);
    }
  }
  
  protected void processEvent(AWTEvent event) {
    if (event instanceof ActionEvent) {
      processActionEvent(new ActionEvent(event.getSource(), event.getID(), (actionCommand == null) ? label : actionCommand));
    }
    else {
      super.processEvent(event);
    }
  }
  
  public synchronized void removeActionListener(java.awt.event.ActionListener listener) {
    actionListener = AWTEventMulticaster.remove(actionListener, listener);
  }
  
  public void setActionCommand(String command) {
    actionCommand = command;
  }
  
  public void addNotify() {
    if(peer == null) {
      peer = Toolkit.getDefaultToolkit().createMenuItem(this);
    }
  }
  
  public String paramString() {
    return "java.awt.MenuItem";
  }

  // Deprecated:
  public void enable() {
    setEnabled(true);
  }
  
  // Deprecated:
  public void disable() {
    setEnabled(false);
  }
  
  private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
  }
  
  private void writeObject(ObjectOutputStream s) throws ClassNotFoundException, IOException {
  }

}

