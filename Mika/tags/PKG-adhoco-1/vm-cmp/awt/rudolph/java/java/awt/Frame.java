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

