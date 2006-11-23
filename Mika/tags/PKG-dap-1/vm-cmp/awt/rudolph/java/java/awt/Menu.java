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

