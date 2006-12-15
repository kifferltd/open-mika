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

public class Button extends Component {
  
  String label;
  String command;
 
  public Button() {
    this.label = "";
    this.command = null;
  }

  public Button(String label) {
    this.label = (label != null) ? label : "";
    this.command = null;
  }

  /**
   * Assign the new listener or return an AWTEventMulticaster 
   * listener that contains it
   */
  public synchronized void addActionListener(java.awt.event.ActionListener newlistener) {
    actionListener = AWTEventMulticaster.add(actionListener, newlistener);
  }

  /**
   * Remove the old listener or return an AWTEventMulticaster
   * listener where it is removed
   */
  public synchronized void removeActionListener(java.awt.event.ActionListener oldlistener) {
    actionListener = AWTEventMulticaster.remove(actionListener, oldlistener);
  }

  /**
   * We overwrite processEvent so that a super or subclass requesting
   * to throw an ActionEvent throws it through the multicaster to all
   * subscribed listeners
   */
  protected void processEvent(AWTEvent event) {
    if (event instanceof ActionEvent) {
      processActionEvent(new ActionEvent(event.getSource(), event.getID(), (command == null) ? label : command));
    }
    else {
      super.processEvent(event);
    }
  }

  protected void processActionEvent(ActionEvent event) {
    if (actionListener != null) {
      actionListener.actionPerformed(event);
    }
  }

  public void addNotify() {
    if(peer == null) {
      peer = getToolkit().createButton(this);
    }

    if (notified == false) {
      super.addNotify();
    }
  }

  public String getActionCommand() {
    return (command == null) ? label : command;
  }

  public void setActionCommand(String command) {
    this.command = command;
  }

  public synchronized void setLabel(String label) {
    this.label = (label != null) ? label : "";

    valid = false;
    ((ButtonPeer)peer).setLabel(label);
  }

  public String getLabel() {
    return label;
  }

  public String toString() {
    return getClass().getName() + " - label: " + label + ", bounds: x = " + x + ", y = " + y + ", w = " + width + ", h = " + height;
  }

  protected String paramString() {
    return getClass().getName() + " label: "+ label + " ActionCommand: " + command + ", bounds: (" + x + ", " + y + ", " + width + ", " + height + ")";
  }

}
