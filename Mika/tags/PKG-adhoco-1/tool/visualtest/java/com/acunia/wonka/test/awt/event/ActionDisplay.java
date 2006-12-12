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

import java.awt.Button;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ActionDisplay extends AWTEventDisplay implements ActionListener {


  /************************************************************************************************************/
  /** constructor
  */
  public ActionDisplay(String firstmessage, Color back, Color text) {
    super(firstmessage, back, text);
  }

  public ActionDisplay(Color back, Color text) {
    super("Your ItemEvents displayed HERE", back, text);
  }

  /************************************************************************************************************/
  /** CollectsEvents help text
  */
  public String getHelpText() {
    return "Displays a panel with a short text about the ActionEvent received.";
  }

  /************************************************************************************************************/
  /** THE ItemListener event (there is only one....) : get the event shortcut and display it
  */
  public void actionPerformed(ActionEvent evt) {
    message = displayActionShortcut(evt);
System.out.println(message);
    repaint();
  }



  /****************************************************************************************************************************************/
  /**     display event diagnostics
  * Following functions will be tested :
  * (java.util)EventObject.getSource()
  * (java awt)AWTEvent.getID() (must be ACTION_PERFORMED)
  * (java awt.event)ActionEvent.getActionCommand()
  * (java awt.event)ActionEvent.getModifiers()
  */

  public static String[] displayActionEvent(ActionEvent evt) {
    String[] lines = new String[3];
    // line 1: EventObject.getSource() /AWTEvent.getID()
    Object source = evt.getSource();
    if(source==null){
      lines[0] = "evt.getSource() == NULL";
    }
    else if (source instanceof Button) {
      lines[0] = "getSource()="+((Button)source).getLabel();
    }
    else {
      lines[0] = "getSource()= "+source;
    }
    int id = evt.getID();
    lines[0]+= (id==ActionEvent.ACTION_PERFORMED)?" ACTION_PERFORMED(" :" UNKNOWN ID(";
    lines[0]+= id+")";

    //line2:ActionEvent.getActionCommand
    lines[1] = "getActionCommand() = "+evt.getActionCommand();

    // line 3: ActionEvent.getModifiers
    int modifiers = evt.getModifiers();
    lines[2] = "getModifiers() = "+modifiers;
    if(modifiers == 0) {
      lines[2]+=" : No special keys";
    }
    else {
      lines[2]+=" :";
    }
    if((modifiers & ActionEvent.SHIFT_MASK) > 0) {
      lines[2]+=" [SHIFT]";
    }
    if((modifiers & ActionEvent.CTRL_MASK) > 0) {
      lines[2]+=" [CTRL]";
    }
    if((modifiers & ActionEvent.ALT_MASK) > 0) {
      lines[2]+=" [ALT]";
    }
    if((modifiers & ActionEvent.META_MASK) > 0) {
      lines[2]+=" [META]";
    }
    if((modifiers & (ActionEvent.SHIFT_MASK+ActionEvent.CTRL_MASK+ActionEvent.ALT_MASK+ActionEvent.META_MASK))!= modifiers) {
      lines[2]+=" [other]";
    }
    return lines;
  }

  /****************************************************************************************************************************************/
  /**     display event diagnostics in a short line
  */
  public static String displayActionShortcut(ActionEvent evt) {
    String line;
    Object source = evt.getSource();
    if(source==null){
      line = "Source() == NULL ";
    }
    else if (source instanceof Button) {
      line="Source() = "+((Button)source).getLabel();
    }
    else {
      line = "Source() = "+source;
    }

    line+= " Command = "+evt.getActionCommand();
    line+= " Modifiers = "+evt.getModifiers();
    return line;
  }

  //end test
}
