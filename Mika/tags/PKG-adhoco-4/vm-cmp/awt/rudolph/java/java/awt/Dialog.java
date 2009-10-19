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

import com.acunia.wonka.rudolph.FocusCycle;

public class Dialog extends Window {

  private static final long serialVersionUID = 5920926903803293709L;
  
  boolean modal     = false;
  boolean resizable = false;
  String  title     = null;
  
  public Dialog(Frame owner) {
    this(owner, false);
  }
  
  public Dialog(Frame owner, String title) {
    this(owner, title, false);
  }
  
  public Dialog(Frame owner, boolean modal) {
    this(owner, null, modal);
  }
  
  public Dialog(Frame owner, String title, boolean modal) {
    super(owner);
    setTitle(title);
    this.modal = modal;

    setLayout(new BorderLayout());
  }

  public String getTitle() {
    return title;
  }

  public synchronized void setTitle(String title) {
    this.title = title;
    ((DialogPeer)peer).setTitle(title);
  }

  public boolean isModal() {
    return modal;
  }

  public void setModal(boolean modal) {
    this.modal = modal;
  }
  
  public boolean isResizable() {
    return resizable;
  }

  public synchronized void setResizable(boolean resizable) {
    this.resizable = resizable;
    ((DialogPeer)peer).setResizable(resizable);
  }

  public void show() {
	FocusCycle.focusFirstComponent(this);
		
	super.show();
    ((DialogPeer)peer).show();	
	
	FocusCycle.next(this);
  }
  
  protected String paramString() {
    return "" + getLocation() + "," + getSize();
  }

  public void addNotify() {
    if(peer == null) {
      peer = getToolkit().createDialog(this);
    }

    if (notified == false) {
      super.addNotify();
    }
  }
}

