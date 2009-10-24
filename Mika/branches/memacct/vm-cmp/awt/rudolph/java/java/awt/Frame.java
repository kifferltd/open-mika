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
import java.util.*;

public class Frame extends Window implements MenuContainer {

  private static final long serialVersionUID = 2673458971256075116L;

  /*
  ** Deprecated constants... These moved to java.awt.Cursor
  */

  public static final int CROSSHAIR_CURSOR = 1;
  public static final int DEFAULT_CURSOR = 0;
  public static final int E_RESIZE_CURSOR = 11;
  public static final int HAND_CURSOR = 12;
  public static final int MOVE_CURSOR = 13;
  public static final int N_RESIZE_CURSOR = 8;
  public static final int NE_RESIZE_CURSOR = 7;
  public static final int NW_RESIZE_CURSOR = 6;
  public static final int S_RESIZE_CURSOR = 9;
  public static final int SE_RESIZE_CURSOR = 5;
  public static final int SW_RESIZE_CURSOR = 4;
  public static final int TEXT_CURSOR = 2;
  public static final int W_RESIZE_CURSOR = 10;
  public static final int WAIT_CURSOR = 3;
  
  /*
  ** Fields needed for Serialization.
  */
  
  String title;
  Image icon;
  MenuBar menuBar;
  boolean resizable;
  boolean mbManagement;
  int state;
  Vector ownedWindows;
  int frameSerializedDataVersion;
 
  public Frame() {
    this("");
  }
  
  public Frame(String title) {
    super();
    setTitle(title);
  }
  
  public Frame(Frame owner, String title) {
    super(owner);
    setTitle(title);
  }
  
  public void addNotify() {
    if(peer == null) {
      peer = getToolkit().createFrame(this);
    }

    if(notified == false) {
      super.addNotify();
    }
  }

  public Image getIconImage() {
    return icon;
  }
  
  public MenuBar getMenuBar() {
    return menuBar;
  }
  
  public String getTitle() {
    return title;
  }
  
  /**
  * @remark  resizable frames are not supported yet
  */

  public boolean isResizable() {
    return resizable;
  }
  
  protected String paramString() {
    return "java.awt.Frame";
  }
  
  public synchronized void remove(MenuComponent component) {
    if(component == menuBar) {
      menuBar = null;
    }
  }
  
  public synchronized void setIconImage(Image image) {
    icon = image;
    ((FramePeer)peer).setIconImage(image);
  }
  
  public synchronized void setMenuBar(MenuBar menubar) {
    menuBar = menubar;
    ((FramePeer)peer).setMenuBar(menubar);
  }

  public synchronized void setResizable(boolean resizable) {
    this.resizable = resizable;
    ((FramePeer)peer).setResizable(resizable);
  }
  
  public synchronized void setTitle(String title) {
    this.title = title;
    ((FramePeer)peer).setTitle(title);
  }

  /*
  ** Deprecated 
  */

  public int getCursorType() {
    return getCursor().getType();
  }

  public void setCursor(int cursorType) {
    super.setCursor(new Cursor(cursorType));
  }
  
}

