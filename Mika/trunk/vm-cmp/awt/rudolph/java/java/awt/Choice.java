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

