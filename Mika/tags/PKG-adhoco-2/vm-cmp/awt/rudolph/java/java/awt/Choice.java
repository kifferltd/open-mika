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

import java.util.*;
import java.awt.peer.*;
import java.awt.event.*;

public class Choice extends Component implements ItemSelectable {

  /*
  ** Variables
  */
  
  private Button dropButton;
  private Label display;
  
  private ItemListener iListener;
  private Vector strings;
  private int currentSelected;

  /*
  ** Constructor
  */
  
  public Choice() {
    // no listeners yet
    iListener = null;
    strings = new Vector();
    currentSelected = -1;
  }

  /*
  ** add item listener (either the listener itself or an AWTEventMulticaster ItemListener handling it
  */

  public void addItemListener(ItemListener newlistener) {
    iListener = AWTEventMulticaster.add(iListener,newlistener);
  }

  /*
  ** remove item listener. If the listener was in the  AWTEventMulticaster ItemListener list, return a new AWTEventMulticaster without it
  ** if the listener was the only listener remaining, let AWTEventMulticaster delete it and return a null
  */

  public void removeItemListener(ItemListener oldlistener) {
    iListener = AWTEventMulticaster.remove(iListener,oldlistener);
  }
  
  /*
  ** On a call to Component.processEvent, use our own listeners to handle all Item and action listeners
  ** ( not that the list itself never calls this function, yet we reservr the option to receive action and Item events
  ** from either the component superclass or from a custom-build derived list
  */

  protected void processEvent(AWTEvent e) {
    if(e instanceof  ItemEvent) {
      processItemEvent((ItemEvent)e);
    }
    else {
      super.processEvent(e);
    }
  }

  /*
  ** Process ItemEvent as described in Itemselectable interface
  */

  protected void processItemEvent(ItemEvent iet) {
    if(iet.getStateChange() == ItemEvent.SELECTED) {
      currentSelected = strings.indexOf(iet.getItem());
    }
    if(iListener != null) {
      iListener.itemStateChanged(iet);
    }
  }

  public void addNotify() {
    if(peer == null) {
      peer = getToolkit().createChoice(this);
    }

    if (notified == false) {
      super.addNotify();
    }
  }

  /*
  ** adding   
  */
  
  public synchronized void add(String item) {
    if(currentSelected < 0){
      currentSelected = 0;
    }
    strings.add(item);
    ((ChoicePeer)peer).add(item, strings.size());
  }
  
  public synchronized void addItem(String item) {
    if(currentSelected < 0){
      currentSelected = 0;
    }
    strings.add(item);
    ((ChoicePeer)peer).add(item, strings.size());
  }

  /*
  ** inserting 
  */

  public synchronized void insert(String item, int index) {
    strings.insertElementAt(item, index);
    ((ChoicePeer)peer).add(item, index);
  }

  /*
  ** removing  
  */

  public synchronized void remove(String item) {
    remove(strings.indexOf(item));
  }

  public synchronized void remove(int index) {
    strings.remove(index);
    ((ChoicePeer)peer).remove(index);
  }

  public synchronized void removeAll() {
    for(int i=0; i < strings.size(); i++) {
      ((ChoicePeer)peer).remove(0);
    }
    strings.removeAllElements();
  } 

  /*
  ** selecting 
  */

  public synchronized void select(int index) {
    ((ChoicePeer)peer).select(index);
    currentSelected = index;
  }

  public synchronized void select(String item) {
  ///  displaySelection(item);
  }

  /*
  ** get number of items  
  */
  
  public int getItemCount() {
    return strings.size();
  }
  
  /*
  ** get item by number   
  */

  public String getItem(int index) {
    return (String)strings.get(index);
  }

  /*
  ** return (list) selection   
  */

  public int getSelectedIndex() {
    return currentSelected;
  }
  
  public synchronized String getSelectedItem() {
    if(currentSelected < 0){
      return null;
    }
    return (String)strings.elementAt(currentSelected);
  }
  
  /*
  ** return all selected objects
  */

  public synchronized Object[] getSelectedObjects() {
    Object[] copy = null;
    if(currentSelected != -1) {
      copy = new Object[1];
      copy[0] = strings.get(currentSelected);
    }
      
    return copy;
  }

  /*
  ** debug
  */
  
  protected String paramString() {
    return "java.awt.Choice";
  }

}

