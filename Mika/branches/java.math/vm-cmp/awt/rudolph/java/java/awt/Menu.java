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

public class Menu extends MenuItem implements MenuContainer {

  /*
  ** Fields needed for Serialization.
  */
  
  private Vector items;
  private boolean tearOff = false;
  private boolean isHelpMenu;
  private int menuSerializedDataVersion;
  
  public Menu() {
    this(null);
  }
  
  public Menu(String label) {
    this(label, false);
  }
  
  public Menu(String label, boolean tearOff) {
    super(label);
    setName(label);
    this.tearOff = tearOff;
    items = new Vector();
  }

  public synchronized MenuItem add(MenuItem item) {
    item.parent = this;
    items.add(item);
    ((MenuPeer)peer).addItem(item);
    return item;
  }
  
  public void add(String label) {
    add(new MenuItem(label));
  }
  
  public void addSeparator() {
    MenuItem item = new MenuItem("-");
    item.parent = this;
    items.add(item);
    ((MenuPeer)peer).addSeparator();
  }
  
  private void resetPeer() {

    /*
    ** Delete all items.
    */
    
    try {
      while(true) {
        ((MenuPeer)peer).delItem(0);
      }
    }
    catch(Exception e) {
    }

    /*
    ** Add all items.
    */

    for(int i = 0; i < items.size(); i++) {
      ((MenuPeer)peer).addItem((MenuItem)items.elementAt(i));
    }
    
  }
  
  public synchronized void insert(MenuItem item, int position) {
    item.parent = this;
    items.insertElementAt(item, position);
    resetPeer();
  }
 
  public void insert(String label, int position) {
    insert(new MenuItem(label), position);
    resetPeer();
  }
  
  public void insertSeparator(int position) {
    insert(new MenuItem("-"), position);
    resetPeer();
  }
  
  public MenuItem getItem(int position) {
    return (MenuItem)items.get(position);
  }
  
  public int getItemCount() {
    return items.size();
  }
  
  public synchronized void remove(int position) {
    items.remove(position);
    ((MenuPeer)peer).delItem(position);
  }
  
  public synchronized void remove(MenuComponent item) {
    int pos = items.indexOf(item);
    if(pos != -1) {
      remove(pos);
    }
  }
  
  public synchronized void removeAll() {
    for(int i=0; i < items.size(); i++) {
      ((MenuPeer)peer).delItem(0);
    }
    items.removeAllElements();
  }
  
  public boolean isTearOff() {
    return tearOff;
  }
  
  public void addNotify() {
    if(peer == null) {
      peer = Toolkit.getDefaultToolkit().createMenu(this);
    }
  }
  
  public void removeNotify() {
    super.removeNotify();
  }
  
  public String paramString() {
    return "java.awt.Menu";
  }

  // Deprecated:
  // public int countItems();
 
  private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
  }
  
  private void writeObject(ObjectOutputStream s) throws ClassNotFoundException, IOException {
  }

}

