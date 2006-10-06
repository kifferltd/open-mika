/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


package com.acunia.wonka.test.awt.event;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ItemDisplay extends AWTEventDisplay implements ItemListener {

  /************************************************************************************************************/
  /** constructor
  */
  public ItemDisplay(String startmessage, Color back, Color fore) {
    super(startmessage, back, fore);
  }

  public ItemDisplay(Color back, Color fore) {
    super("Your ItemEvents displayed HERE", back, fore);
  }

  /************************************************************************************************************/
  /** CollectsEvents help text
  */
  public String getHelpText() {
    return "Displays a panel with a short text about the ItemEvent received.";
  }

  /************************************************************************************************************/
  /** THE ItemListener event (there is only one....) : get the event shortcut and display it
  */
  public void itemStateChanged(ItemEvent evt) {
    message = displayItemShortcut(evt);
//System.out.println(message);
    repaint();
  }

  /****************************************************************************************************************************************/
  /**     display event diagnostics
  * Following functions will be tested :
  * (java.util)EventObject.getSource()
  * (java awt.event)ItemEvent.getItem()
  * (java awt.event)ItemEvent.getItemSelectable()
  * (java awt.event)ItemEvent.getStateChange()
  */

  public static String[] displayItemEvent(ItemEvent evt) {
    String[] lines = new String[4];
    // line 1: EventObject.getSource
    Object source = evt.getSource();
    int id = evt.getID();
    if(source==null){
      lines[0] = "evt.getSource() == NULL";
    }
    else if (source instanceof Checkbox) {
      lines[0] = "getSource()="+((Checkbox)source).getLabel();
      lines[0]+= (id==ItemEvent.ITEM_STATE_CHANGED)?"ITEM_STATE_CHANGED "+id :"UNKNOWN "+id;
    }
    else {
      lines[0] = "getSource()= "+source;
      lines[0]+= (id==ItemEvent.ITEM_STATE_CHANGED)?"ITEM_STATE_CHANGED "+id :"UNKNOWN "+id;
    }

    //line2:ItemEvent.getItem()
    source = evt.getItem();
    if(source==null){
      lines[1] = "getItem() == NULL";
    }
    else if (source instanceof Checkbox) {
      lines[1] = "getItem() = "+((Checkbox)source).getLabel();

    }
    else {
      lines[1] = "getItem() = "+source.toString();
    }

    // line 3: ItemEvent.getStateChange
    if(evt.getStateChange()==ItemEvent.SELECTED) {
      lines[2] = "getStateChange() = SELECTED ("+evt.getStateChange()+")";
    }
    else if(evt.getStateChange()==ItemEvent.DESELECTED) {
      lines[2] = "getStateChange() = DESELECTED ("+evt.getStateChange()+")";
    }
    else {
      lines[2] = "(unknown state: evt.getStateChange() = "+evt.getStateChange()+")";
    }

    // line 4: ItemSelectable() and selections
    ItemSelectable selectable=evt.getItemSelectable();
    if(selectable==null){
      lines[3] = "getItemSelectable() == NULL";
    }
    else if (selectable.getSelectedObjects() == null) {
      lines[3] = "getItemSelectable():no selections";
    }
    else {
      Object[] selection = selectable.getSelectedObjects();
      lines[3] = "selections {";
      for(int i=0; i<selection.length; i++) {
        lines[3]+= " "+selection[i];
      }
      lines[3]+="}";
    }

    return lines;
  }

  /****************************************************************************************************************************************/
  /**     display event diagnostics in a short line
  */
  public static String displayItemShortcut(ItemEvent evt) {
    String line;
    Object source = evt.getSource();
    if(source==null){
      line = "From Source NULL ";
    }
    else if (source instanceof Checkbox) {
      line="From "+((Checkbox)source).getLabel();
    }
    else {
      line = "From "+source;
    }

    source = evt.getItem();
    if(source==null){
      line+= ": Item NULL";
    }
    else {
      line+= ": Item "+source.toString();
    }
    int state = evt.getStateChange();
    if(state==ItemEvent.SELECTED) {
      line+= " SELECTED";
    }
    else if(state==ItemEvent.DESELECTED) {
      line+= " DESELECTED";
    }
    else {
      line+= "(unknown state)";
    }
      return line;
  }

  //end test
}
