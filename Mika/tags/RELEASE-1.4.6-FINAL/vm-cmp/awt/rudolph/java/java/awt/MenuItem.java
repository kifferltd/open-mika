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

