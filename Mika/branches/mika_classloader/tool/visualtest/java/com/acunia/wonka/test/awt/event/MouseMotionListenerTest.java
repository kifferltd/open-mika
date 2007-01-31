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


// Author: N. Oberfeld
// Created: 2001/09/26


package com.acunia.wonka.test.awt.event;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Panel;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class MouseMotionListenerTest extends VisualTestImpl implements CollectsEvents {

  /***************************************************************************************************/
  /** variables
  */
  private List display;
  private MouseGeneratorComponent trigger1;
  private MouseGeneratorComponent trigger2;
  private MouseGeneratorComponent trigger3;

  /***************************************************************************************************/
  /** Constructor
  */
  public MouseMotionListenerTest() {
    trigger1= new MouseGeneratorComponent("<MouseListener>",MouseGeneratorComponent.COPPER,MouseGeneratorComponent.DARKGREEN,this);
    trigger1.addMouseListener(trigger1);
    trigger2= new MouseGeneratorComponent("<MouseMotionListener>",MouseGeneratorComponent.DARKSAND,MouseGeneratorComponent.DUSTGREEN,this);
    trigger2.addMouseMotionListener(trigger2);
    trigger3= new MouseGeneratorComponent("<both listeners>",MouseGeneratorComponent.DARKGREEN,MouseGeneratorComponent.COPPER,this);
    trigger3.addMouseListener(trigger3);
    trigger3.addMouseMotionListener(trigger3);
    setLayout(new BorderLayout());
    Panel p = new Panel(new GridLayout(1,3));
      p.add(trigger1);
      p.add(trigger2);
      p.add(trigger3);
    add(p,BorderLayout.CENTER);
    display = new List(6,false);
    display.add("See your mouse events here");
    add(display,BorderLayout.SOUTH);
  }


  /***************************************************************************************************/
  /** CollectsEvents interface handling of a message sent by one of our event generators
  * 'No big deal, simply add it to our event display list
  */
  public void displayMessage(String[] message) {
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    if(message.length>1) {
      display.add("..... "+message[1], 0);
    }
    if(message.length>0) {
      display.add(message[0], 0);
    }
  }

  /***************************************************************************************************/
  /** test description for the VisualTestEngine help
  */
  public String getHelpText(){
    return "The test displays three mouse event fields and a list. Click or drag on any of the fields and see a display"+
           " of the MouseEvent thrown appear in the list below.\n"+
           " The left field only reacts to MouseListener events, the middle field only to MouseMotionListener events (moving and dragging)"+
           " the right field reacts to both.\n"+
           "\n Items to test:\n --------------\n"+
           "=> The left panel must react to all MouseListener events (mouse entered, exited, pressed, released and clicked) and only to them\n"+
           "=> The middle panel must react to all MouseMotionListener events (mouse moved and dragged) and only to them\n"+
           "=> The right panel must react to both MouseListener as well as MouseMotionListener events\n"+
           "=> The events displayed in the list must match the correct source as well as the correct mouse action\n"+
           "\n current issues:\n --------------\n"+
           "=> when moving the mouse, no MOUSE_MOVED MouseEvent is thrown \n"+
           "=> No the MouseEvent's time is always 0 \n   (SEE THE OTHER ERRORS ON MOUSE-EVENTS IN THE MOUSE-EVENT TEST)\n"+
           "\n\nPs: the behavior of the event fields to the panels is:" + trigger3.getHelpText();
  }

  /***************************************************************************************************/
  /** toString function to display in mouseEvent.getSource().toString()
  */
  public String toString() {
    return "MouseEventTest";
  }


}