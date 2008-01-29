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
