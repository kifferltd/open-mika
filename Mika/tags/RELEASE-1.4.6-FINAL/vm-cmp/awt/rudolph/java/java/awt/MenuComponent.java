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

