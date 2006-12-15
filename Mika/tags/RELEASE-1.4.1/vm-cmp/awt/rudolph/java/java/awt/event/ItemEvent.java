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



package java.awt.event;

import java.awt.*;

public class ItemEvent extends java.awt.AWTEvent {

  public static final int ITEM_LAST = 701;
  public static final int ITEM_FIRST = 701;
  public static final int ITEM_STATE_CHANGED = 701;

  public static final int SELECTED = 1;
  public static final int DESELECTED = 2;

  Object item;
  int stateChange;

  public ItemEvent(ItemSelectable source, int id, Object item, int newState) {
    super(source, id);

    this.item = item;
    this.stateChange = newState;
  }
  
  public Object getItem() {
    return item;
  }
  
  public ItemSelectable getItemSelectable() {
    return (ItemSelectable)source;
  }
  
  public int getStateChange() {
    return stateChange;
  }

  public String toString() {
    String display= getClass().getName() +" [ItemStateChanged, item=";
    display+=(source instanceof Checkbox)?((Checkbox)source).getLabel():item.toString();
    display+=(stateChange==SELECTED)?", SELECTED] on":", DESELECTED] on ";
    display+=source.getClass().getName();
    return display;
  }
  public String paramString() {
    return getClass().getName() +" [source="+source+", Item="+item+", State="+stateChange+"]";
  }
}
