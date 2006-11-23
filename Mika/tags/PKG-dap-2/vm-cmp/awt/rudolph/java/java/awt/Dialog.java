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

