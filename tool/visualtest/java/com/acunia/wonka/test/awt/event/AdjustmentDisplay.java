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

import java.awt.Adjustable;
import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class AdjustmentDisplay extends AWTEventDisplay implements AdjustmentListener {
  /************************************************************************************************************/
  /** constructor
  */
  public AdjustmentDisplay(String startmessage, Color back, Color fore) {
    super(startmessage, back, fore);
  }

  public AdjustmentDisplay(Color back, Color fore) {
    super("Your ItemEvents displayed HERE", back, fore);
  }

  /************************************************************************************************************/
  /** CollectsEvents help text
  */
  public String getHelpText() {
    return "Displays a panel with a short text about the AdjustmentEvent received.";
  }

  /************************************************************************************************************/
  /** THE AdjustmentListener event (there is only one....) : get the event shortcut and display it
  */
  public void adjustmentValueChanged(AdjustmentEvent evt) {
    message = displayAdjustmentShortcut(evt);
//System.out.println(message);
    repaint();
  }
  /****************************************************************************************************************************************/
  /**     display event diagnostics
  * Following functions will be tested :
  * (java awt.event)AdjustmentEventEvent.getAdjustable()  (for (java.util)EventObject.getSource() ) and (Java.awt)AWTEvent.getID()
  * (java awt.event)AdjustmentEventEvent.getAdjustmentType()  and (java awt.event)AdjustmentEvent.getValue()
  */

  public static String[] displayAdjustmentEvent(AdjustmentEvent evt) {
    String[] lines = new String[2];
    // line 1: EventObject.getSource  and AWTEvent.getID()
    Adjustable source = evt.getAdjustable();
    int id = evt.getID();
    if(source==null){
      lines[0] = "getAdjustable == NULL";
    }
    else {
      lines[0] = "getAdjustable()= "+source;
      lines[0]+= (id==AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED)?": ADJUSTMENT_VALUE_CHANGED "+id :": UNKNOWN "+id;
    }

    //line2:ItemEvent.getItem()
    id = evt.getAdjustmentType();
    if(source==null){
      lines[1] = "getAdjustmentType() == NULL";
    }
    else {
      lines[1] = "getAdjustmentType() = "+id;
      if(id==AdjustmentEvent.UNIT_INCREMENT) {
        lines[1]+= ": [UNIT_INCREMENT]";
      }
      else if(id==AdjustmentEvent.BLOCK_INCREMENT) {
        lines[1]+= ": [BLOCK_INCREMENT]";
      }
      else if(id==AdjustmentEvent.TRACK) {
        lines[1]+= ": [TRACK]";
      }
      else if(id==AdjustmentEvent.BLOCK_DECREMENT) {
        lines[1]+= ": [BLOCK_DECREMENT]";
      }
      else if(id==AdjustmentEvent.UNIT_DECREMENT) {
        lines[1]+= ": [UNIT_DECREMENT]";
      }
      else {
        lines[1]+= ": [unknown]";
      }
    }
    lines[1]+=" GetValue = "+evt.getValue();

    return lines;
  }

  /****************************************************************************************************************************************/
  /**     display event diagnostics in a short line
  */
  public static String displayAdjustmentShortcut(AdjustmentEvent evt) {
    String line;
    Adjustable source = evt.getAdjustable();
    int id = evt.getID();
    if(source==null){
      line = "from NULL";
    }
    else {
      line = "from "+source;
    }

    id= evt.getAdjustmentType();
    if(source==null){
      line += " action == NULL";
    }
    else {
      if(id==AdjustmentEvent.UNIT_INCREMENT) {
        line += " [UNIT_INCREMENT]";
      }
      else if(id==AdjustmentEvent.BLOCK_INCREMENT) {
        line += " [BLOCK_INCREMENT]";
      }
      else if(id==AdjustmentEvent.TRACK) {
        line += " [TRACK]";
      }
      else if(id==AdjustmentEvent.BLOCK_DECREMENT) {
        line += " [BLOCK_DECREMENT]";
      }
      else if(id==AdjustmentEvent.UNIT_DECREMENT) {
        line += " [UNIT_DECREMENT]";
      }
      else {
        line += " Unknown action";
      }
    }
    line += " value = "+evt.getValue();

    return line;
  }

  //end test
}
