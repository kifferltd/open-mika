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

public class MouseListenerTest extends VisualTestImpl implements  MouseListener, MouseMotionListener, CollectsEvents {

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
  public MouseListenerTest() {
    trigger1= new MouseGeneratorComponent("<COPPER>",MouseGeneratorComponent.COPPER,MouseGeneratorComponent.DARKGREEN,this);
    trigger1.addMouseListener(trigger1);
    trigger1.addMouseMotionListener(trigger1);
    trigger2= new MouseGeneratorComponent("<DARK>",MouseGeneratorComponent.DARKSAND,MouseGeneratorComponent.DUSTGREEN,this);
    trigger2.addMouseListener(trigger2);
    trigger2.addMouseListener(this);
    trigger2.addMouseMotionListener(this);
    trigger2.addMouseMotionListener(trigger2);
    trigger3= new MouseGeneratorComponent("<GREEN>",MouseGeneratorComponent.DARKGREEN,MouseGeneratorComponent.COPPER,this);
    trigger3.addMouseListener(this);
    trigger3.addMouseMotionListener(this);

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
  /** Mouse events received from second generator */
  public void mouseClicked(MouseEvent event) {
    displayMessage(MouseGeneratorComponent.displayMouseEvent(event, "Test impl"));
  }

  public void mouseEntered(MouseEvent event) {
    displayMessage(MouseGeneratorComponent.displayMouseEvent(event, "Test impl"));
  }

  public void mouseExited(MouseEvent event) {
    displayMessage(MouseGeneratorComponent.displayMouseEvent(event, "Test impl"));
  }

  public void mousePressed(MouseEvent event) {
    displayMessage(MouseGeneratorComponent.displayMouseEvent(event, "Test impl"));
  }

  public void mouseReleased(MouseEvent event) {
    displayMessage(MouseGeneratorComponent.displayMouseEvent(event, "Test impl"));
  }

  public void mouseDragged(MouseEvent event) {
    displayMessage(MouseGeneratorComponent.displayMouseEvent(event, "Test impl"));
  }

  public void mouseMoved(MouseEvent event) {
    displayMessage(MouseGeneratorComponent.displayMouseEvent(event, "Test impl"));
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
    return "The test displays three mouse event fields and a list. Click on any of the fields and see a display"+
           " of the MouseEvent thrown appear in the list below.\n"+
           " All mouse event fields have a MouseListener and therefor react to the mouse-pressed, mouse-released, mouse-clicked"+
           " mouse-entered and mouse-exited. (remark that this do not include mouse-moved and mouse-dragged events"+
           " that are catched by a MouseMotionListener.)\n"+
           " The mouse events of the left field are catched and handledby the field itself.\n"+
           " The events of the right field are directly dispatched to the main panel \n"+
           " The events of the middle field are dispatched both to the main as well as to the field itself.\n"+
           "\n Items to test:\n --------------\n"+
           "=> The left field must react to all MouseListener events and show this events on the list as originating from "+
           " and catched by the left field\n"+
           "=> The right field does not react to the mouse events, yet, the events will be displayed in the list"+
           " as originating from that field and catched by the main testing class\n"+
           "=> The middle field reacts to the mouse events, and displays eveny event twice: once as thrown and catched by the panel"+
           " itself and once thrown by the panel and catched by the testing class\n"+
           "\n current issues:\n --------------\n"+
           "=> The middle panel does NOT react to mouse events and does NOT throw the <catched by the panel> event."+
           " appearantly sending the messages to the main disposes them before they reach the other classes\n"+
           "=> No the MouseEvent's time is always 0 \n   (SEE THE OTHER ERRORS ON MOUSE-EVENTS IN THE MOUSE-EVENT TEST)\n"+
           "\n\nPs: the behavior of the event fields to the panels is:" + trigger1.getHelpText();
  }

  /***************************************************************************************************/
  /** toString function to display in mouseEvent.getSource().toString()
  */
  public String toString() {
    return "MouseEventTest";
  }


}