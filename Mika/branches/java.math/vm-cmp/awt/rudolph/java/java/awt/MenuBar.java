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
