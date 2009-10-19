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

import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;
import java.awt.peer.*;

public class List extends Component implements ItemSelectable, Serializable {
  
  /*
  ** Variables :
  */

  private int rows;
  private boolean multipleMode;
  private Vector items;
  private int visibleIndex = 0;
	private int[] selected;
	private static final long serialVersionUID = -3304312411574666869L;
	private int listSerializedDataVersion = 0;
  
  /*
  ** listeners
  */
  
  public transient ActionListener aMultiListener;
  public transient ItemListener iMultiListener;

  /*
  ** Constructor  
  */

  public List(int rows, boolean allowmultipleselections) {
    this.rows = rows;
    multipleMode = allowmultipleselections;
    items = new Vector();
		selected = null;
    setMultipleMode(multipleMode);

    //listeners
    iMultiListener = null;
    aMultiListener = null;
  }

  public List(int rows) {
    this(rows, false);
  }
  
  public List() {
    this(4, false);
  }

  public int getRows() {
    return rows;
  }
  
  public void addNotify() {
    if(peer == null) {
      peer = getToolkit().createList(this);
    }

    if (notified == false) {
      super.addNotify();
    }
  }

  /*
  ** Listeners
  */

  public void addActionListener(ActionListener newlistener) {
    aMultiListener = AWTEventMulticaster.add(aMultiListener, newlistener);
  }

  public void removeActionListener(ActionListener oldlistener) {
    aMultiListener = AWTEventMulticaster.remove(aMultiListener, oldlistener);
  }

  public void addItemListener(ItemListener newlistener) {
    iMultiListener = AWTEventMulticaster.add(iMultiListener, newlistener);
  }

  public void removeItemListener(ItemListener oldlistener) {
    iMultiListener = AWTEventMulticaster.remove(iMultiListener, oldlistener);
  }

  /*
  ** Events
  */
  
  protected void processEvent(AWTEvent e) {
    if(e instanceof ActionEvent) {
      if(aMultiListener != null) {
        aMultiListener.actionPerformed((ActionEvent)e);
      }
    }
    else if(e instanceof ItemEvent) {
      if(iMultiListener != null) {
        iMultiListener.itemStateChanged((ItemEvent)e);
      }
    }
    else {
      super.processEvent(e);
    }
  }

  protected void processActionEvent(ActionEvent e) {
    if(aMultiListener != null) {
      aMultiListener.actionPerformed((ActionEvent)e);
    }
  }

  protected void processItemEvent(ItemEvent e){
    if(iMultiListener != null) {
      iMultiListener.itemStateChanged((ItemEvent)e);
    }
  }
  
  /*
  ** Selection and multiple mode functions
  */
  
  public void select(int index) {
    ((ListPeer)peer).select(index);
  }

  public void deselect(int index) {
    ((ListPeer)peer).deselect(index);
  }
    
  /*
  ** return true if current item is selected
  */

  public boolean isIndexSelected(int index) throws ArrayIndexOutOfBoundsException {
    int selected[] = getSelectedIndexes();
    for(int i = 0; i < selected.length; i++) {
      if(selected[i] == index) {
        return true;
      }
    }
    return false;
  }

  /*
  ** return last selected index
  */

  public synchronized int getSelectedIndex() {
    int a[] = ((ListPeer)peer).getSelectedIndexes();
    if(a.length == 1) return a[0]; else return -1;
  }
  
  /*
  ** return the index of the last object which was made visible with makeVisible.
  */

  public int getVisibleIndex() {
    return visibleIndex;
  }
      
  public synchronized int[] getSelectedIndexes() {
    return ((ListPeer)peer).getSelectedIndexes();
  }  

  /*
  ** Check if multiple mode
  */

  public boolean isMultipleMode() {
    return multipleMode;
  }
  
  /*
  ** deprecated
  */

  public boolean AllowsMultipleSelections() {
    return multipleMode;
  }

  /*
  ** Switch multiple selection mode on/off
  */

  public void setMultipleMode(boolean newmode) {
    multipleMode = newmode;
    ((ListPeer)peer).setMultipleMode(newmode);
  }

  /*
  ** deprecated
  */

  public void setMultipleSelections(boolean mode) {
    setMultipleMode(mode);
  }

  /*
  ** number of items in list
  */

  public int getItemCount() {
    return items == null ? 0 : items.size();
  }

  /*
  ** Gets (A COPY OF) the item at the selected index, or throws an ArrayIndexOutOfBoundException
  */

  public String getItem(int index) {
    return (String)items.elementAt(index);
  }

  /*
  ** returns an array containing A COPY of all the items in the list
  */

  public String[] getItems() {
    String[] copy = new String[items.size()];
    for(int i = 0; i < items.size(); i++) {
      copy[i] = (String)items.elementAt(i);
    }

    return copy;
  }

  /*
  ** return last selected element
  ** By definition, in multiple mode, the function ALWAYS returns null
  */

  public synchronized String getSelectedItem() {
    int[] selected = getSelectedIndexes();

    if(selected.length != 1) {
      return null;
    }

    return (String)items.elementAt(selected[0]);
  }

  /*
  ** return String array of all selected elements
  */

  public synchronized String[] getSelectedItems() {
    int[] selected = getSelectedIndexes();
    String[] copy = new String[selected.length];

    for(int i = 0; i < selected.length; i++) {
      copy[i] = (String)items.elementAt(selected[i]);
    }

    return copy;
  }

  /*
  ** return array of all selected objects
  */

  public synchronized Object[] getSelectedObjects() {
    int[] selected = getSelectedIndexes();
    Object[] copy = new String[selected.length];

    for(int i = 0; i < selected.length; i++) {
      copy[i] = (String)items.elementAt(selected[i]);
    }

    return copy;
  }  

  /*
  ** add to the end of the list
  */

  public void add(String newitem) {
    add(newitem, items.size());
  }

  /*
  ** deprecated 
  */

  public void addItem(String newitem) {
    add(newitem);
  }

  /*
  ** add to the list at desired position
  */
  
  public void add(String newitem, int pos) {
    if(pos > items.size() || pos == -1) pos = items.size();
    items.insertElementAt(newitem, pos);
    ((ListPeer)peer).add(newitem, pos);
  }
    
  /*
  ** deprecated 
  */

  public void addItem(String newitem, int pos) {
    add(newitem, pos);
  }

  /*
  ** replace an item at a given position with another given element.
  */

  public void replaceItem(String newitem, int pos) {
    add(newitem, pos + 1);
    remove(pos);
  }

  /*
  ** remove an item at a given position of the list. 
  */

  public void remove(int index) {
    removeElement(index, index + 1);
  }

  /*
  ** deprecated 
  */

  public void delItem(int index) { 
    delItems(index, index + 1);
  }
    
  /*
  ** deprecated 
  */

  public void delItems(int start, int stop) {
    removeElement(start, stop);
  }
    
  /*
  ** remove a given item from the list. 
  */
  
  public void remove(String element) {
    remove(items.indexOf(element));
  }

  public void removeAll() {
    items.removeAllElements();
    ((ListPeer)peer).removeAll();
  }

  /*
  ** deprecated
  */

  public void clear() {
    removeAll();
  }

  public void removeElement(int start, int stop) {
    for(int i = start; i < stop; i++) {
      items.remove(start);
    }
    ((ListPeer)peer).delItems(start, stop);
  }

  /*
  ** replace an item in the list
  */

  public void replaceElement(String newitem, int pos) {
    remove(pos);
    add(newitem, pos);
  }
  
  /*
  ** 'manually' set the list to a certain position so that a certain item is visible
  */

  public void makeVisible(int target) {
    visibleIndex = target;
    ((ListPeer)peer).makeVisible(target);
  }
  
  /*
  ** Debug information
  */

  public String toString() {
    Rectangle b=getBounds();
    String text = "List " + (b == null ? "(no bounds)" : ("(" + b.x + ", " + b.y + ", " + b.width + ", " + b.height) + ")") + (items == null ? " no " : " " + items.size() + " ") + "items";
    if(multipleMode) {
      text = "Multiple " + text;
    }
    else {
      text += " selected: " + getSelectedIndex();
    }
    return text;
  }

  protected String paramString() {
    Rectangle b = getBounds();
    String text = "List " + getName() + " ( " + b.x + ", " + b.y + ", " + b.width + ", " + b.height + ") selected: " + items.size() + " items";
    if(multipleMode) {
      text = "Multiple " + text;
      String[] items = getSelectedItems();
      if(items.length > 0){
        text += "[" + items[0];
        for(int i = 1; i < items.length; i++) {
          text += "],[" + items[i];
        }
        text += "]";
      }
      else {
        text += "NONE";
      }
    }
    else {
      text += ") selected: " + getSelectedItem();
    }
    return text;
  }

	/* Serialization methods */

	private void readObject(ObjectInputStream s) {
		/* to be implemented */
	}

	private void writeObject(ObjectOutputStream s) {
		/* to be implemented */
	}
}

