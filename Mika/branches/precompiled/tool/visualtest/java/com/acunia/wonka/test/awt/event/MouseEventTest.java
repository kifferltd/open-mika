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

import com.acunia.wonka.test.awt.*;
import java.awt.*;
import java.awt.event.*;

public class MouseEventTest extends VisualTestImpl implements CollectsEvents {

  /***************************************************************************************************/
  /** variables
  */
  private List display;
  private MouseGeneratorComponent trigger1;
  private MouseGeneratorComponent trigger2;
  private MouseGeneratorComponent trigger3;

  final static Color COPPER = new Color(200,200,120);
  final static Color DARKSAND=new Color(96,96,48);
  final static Color DUSTGREEN=new Color(128,192,128);
  final static Color DARKGREEN=new Color(64,96,64);
  /***************************************************************************************************/
  /** Constructor
  */
  public MouseEventTest() {
    trigger1= new MouseGeneratorComponent("<COPPER>",MouseGeneratorComponent.COPPER,MouseGeneratorComponent.DARKGREEN,this);
    trigger1.addMouseListener(trigger1);
    trigger1.addMouseMotionListener(trigger1);
    trigger2= new MouseGeneratorComponent("<DARK>",MouseGeneratorComponent.DARKSAND,MouseGeneratorComponent.DUSTGREEN,this);
    trigger2.addMouseListener(trigger2);
    trigger2.addMouseMotionListener(trigger2);
    trigger3= new MouseGeneratorComponent("<GREEN>",MouseGeneratorComponent.DARKGREEN,MouseGeneratorComponent.COPPER,this);
    trigger3.addMouseListener(trigger3);
    trigger3.addMouseMotionListener(trigger3);
    setLayout(new BorderLayout());
    add(trigger1,BorderLayout.NORTH);
    Panel p = new Panel(new GridLayout(1,2));
      p.add(trigger2);
      p.add(trigger3);
    add(p,BorderLayout.CENTER);
    display = new List();
    display.add("Click or drag the mouse");
    display.add("and see the events events");
    display.add("        HERE");
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
    for(int i=message.length-1; i>=0; i--) {
      display.add(message[i], 0);
    }
  }

  /***************************************************************************************************/
  /** test description for the VisualTestEngine help
  */
  public String getHelpText(){
    return "The test displays three mouse event fields and a list. Click or drag on any of the fields and see a display"+
           " of the MouseEvent thrown appear in the list below.\n"+
           " The mouse event is analysed and displayed in a four-line text. For the correct behavior of the MouseAdapter"+
           " and MouseEvent class, each event should be tested on the following items: \n"+
           "\n Items to test:\n --------------\n"+
           "=> Reaction: the events should be thrown in response to a mouse event thrown as the mouse experiences one of these cases :\n"+
           "  - the mouse entered one of the three fields\n"+
           "  - the mouse left one of the three fields\n"+
           "  - One of the mouse buttons was pressed\n"+
           "  - that mouse buttons was released again\n"+
           "  - one of the mouse buttons was clicked\n"+
           "=> Source: the mouse event should display the correct 'source' the event originated. This is the field the mouse action"+
           " was taken in.( either COPPER, DARK, or GREEN, depending on wether the event happened in the copper, dark grey or green field)"+
           " as originating from that field and catched by the main testing class\n"+
           "=> time: the second line should display the system time in milliseconds on which the event was thrown"+
           "=> action: the second line should also display the correct mouse action. Once as a number between"+
           MouseEvent.MOUSE_FIRST +" and   "+MouseEvent.MOUSE_LAST+" and once in text"+
           "=> Position: the third line should display the mouse position IN THE CURRENT FIELD. Move the mouse over the borders of several"+
           " fields and see the positions change"+
           "=> Number of clicks: when clicked, the third line shouls also detect the number of clicks as a means to distinguish"+
           " between single and double-clicks\n"+
           "=> Modifiers and buttons: The fourth line should show the mouse modifiers and detect from there which of the mouse buttons"+
           " was clicked, or of more were clicked at the same time\n"+
           "=> Popup menu: The fourth line should show also show wether the mouse event triggered a popup menu"+
           "=> multiple events: As Java defines a mouse click as a mouse-pressed event followed by a mouse-release event, clicking the mouse"+
           " will trigger three events at once: a mouse-pressed, mouse-clicked and mouse-released. Similar, releasing a pressed button"+
           " will trigger a mouse-clicked event as well as a mouse-released"+
           "\n current issues (version 0.7.2 / oct. 11 2001:\n -------------------------\n"+
           "=> event reaction: the mouse driver does not throw a mouse-moved item when the mouse is moved\n"+
           "=> time: the event time call <MouseEvent.getWhen()> always returns a zero\n"+
           "=> Number of clicks: <MouseEvent.getClickCount()> always returns a zero\n"+
           "=> Modifiers/buttons: <ItemEvent.getModifiers()> always returns a zero\n"+

           "\n\nPs: the behavior of the event fields to the mouse events is described below:\n\n" + trigger1.getHelpText();
  }

  /***************************************************************************************************/
  /** toString function to display in mouseEvent.getSource().toString()
  */
  public String toString() {
    return "MouseEventTest";
  }


}