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

import java.io.*;
import java.awt.peer.*;

public abstract class MenuComponent implements java.io.Serializable {

  /*
  ** Fields which are needed for Serialization.
  */
  
  private Font font;
  private String name;
  private boolean nameExplicitlySet;
  private boolean newEventsOnly;

  // private AccessibleContext accessibleContext

  transient MenuComponentPeer peer;

  public MenuComponent() {
    addNotify();
  }

  public void addNotify() {
  }
  
  /*
  ** Other fields.
  */

  transient MenuContainer parent;
  
  public Font getFont() {
    return font;
  }
  
  public void setFont(Font font) {
    this.font = font;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MenuContainer getParent() {
    return parent;
  }
  
  public final void dispatchEvent(AWTEvent event) {
    processEvent(event);
  }

  protected void processEvent(AWTEvent event) {
  }
  
  public void removeNotify() {
    peer.dispose();
    peer = null;
  }

  protected String paramString() {
    return "java.awt.MenuComponent";
  }
  
  /**
  * @status  not implemented
  * @remark  not implemented
  */

  public String toString() {
    return "java.awt.MenuComponent";
  }
  
  // Deprecated:
  // public boolean postEvent(Event event);
  
  // Deprecated:
  public MenuComponentPeer getPeer() {
    return peer;
  }
  
  private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
  }

}

