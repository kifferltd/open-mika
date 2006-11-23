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
import java.io.*;

public class MenuBar extends MenuComponent implements MenuContainer {

  /*
  ** Needed for Serialization.
  */
  
  Vector menus;
  Menu helpMenu;
  int menuBarSerializedDataVersion;

  public MenuBar() {
    menus = new Vector();
  }

  public synchronized Menu add(Menu menu) {
    menu.parent = this;
    menus.add(menu);
    ((MenuBarPeer)peer).addMenu(menu);
    return menu;
  }
  
  public Menu getMenu(int position) {
    return (Menu)menus.get(position);
  }
  
  public int getMenuCount() {
    return menus.size();
  }
  
  public synchronized void remove(int position) {
    ((MenuBarPeer)peer).delMenu(position);
    menus.remove(position);
  }

  public synchronized void remove(MenuComponent component) {
    remove(menus.indexOf(component));
  }

  public Menu getHelpMenu() {
    return helpMenu;
  }
  
  public synchronized void setHelpMenu(Menu menu) {
    menu.parent = this;
    ((MenuBarPeer)peer).addHelpMenu(menu);
    helpMenu = menu;
  }
  
  public void deleteShortcut(MenuShortcut shortcut) {
  }
  
  public MenuItem getShortcutMenuItem(MenuShortcut shortcut) {
    return null;
  }
  
  public synchronized java.util.Enumeration shortcuts() {
    return null;
  }
  
  public void addNotify() {
    if(peer == null) {
      peer = Toolkit.getDefaultToolkit().createMenuBar(this);
    }
  }
  
  public void removeNotify() {
  }
  
  private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
  }
  
  private void writeObject(ObjectOutputStream s) throws ClassNotFoundException, IOException {
  }
  
}
