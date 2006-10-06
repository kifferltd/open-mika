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

public class MouseListenerCrossed extends VisualTestImpl implements CollectsEvents {

  /***************************************************************************************************/
  /** variables
  */
  private List display;
  private MouseGeneratorNoExit trigger1;
  private MouseGeneratorNoExit trigger2;

  /***************************************************************************************************/
  /** Constructor
  */
  public MouseListenerCrossed() {
    trigger1= new MouseGeneratorNoExit("<COPPER>",MouseGeneratorComponent.COPPER,MouseGeneratorComponent.DARKSAND,this);
    trigger2= new MouseGeneratorNoExit("<DARK>",MouseGeneratorComponent.DARKSAND,MouseGeneratorComponent.COPPER,this);
    trigger1.addMouseListener(trigger2);
    trigger2.addMouseListener(trigger1);
    trigger1.addMouseMotionListener(trigger2);
    trigger2.addMouseMotionListener(trigger1);

    setLayout(new BorderLayout());
    Panel p = new Panel(new GridLayout(1,2));
      p.add(trigger1);
      p.add(trigger2);
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
    if(message.length>2) {
      display.add("..... "+message[2], 0);
    }
    if(message.length>1) {
      display.add("..... "+message[1], 0);
    }
    if(message.length>0) {
      display.add(message[0], 0);
    }
  }

  /***************************************************************************************************/
  /** Own version of  MouseGeneratorComponent that does nothing but throw a message when entering or exiting the field
  */
  class MouseGeneratorNoExit extends MouseGeneratorComponent {
    public MouseGeneratorNoExit(String componentname, Color back, Color front, CollectsEvents parentinstance){
      super(componentname, back, front, parentinstance);
    }

    public void mouseEntered(MouseEvent event) {
      parent.displayMessage(MouseGeneratorComponent.displayMouseEvent(event,name));
    }

    public void mouseExited(MouseEvent event) {
      parent.displayMessage(MouseGeneratorComponent.displayMouseEvent(event,name));
    }
  }
  /***************************************************************************************************/
  /** test description for the VisualTestEngine help
  */
  public String getHelpText(){
    return " A test on remote controll: \n"+
           " The panel consists out of two mouse event fields and a list. Just as in the previous tests, the list will display"+
           " the mouse events thrown. However, this time the mouse listeners of the two panels are linked <crossed>"+
           " (The events of the left panel are sent to the right and vice versa). \n"+
           " CONSEQUENTLY ANY MOUSE ACTION IN THE LEFT PANEL WILL RESULT IN THE RIGHT PANEL DISPLAYING THE RESULT OF THIS ACTION"+
           " AND ANY MOUSE ACTION IN THE RIGHT PANEL WILL BE DISPLAYED IN THE LEFT"+
           " All mouse event fields have a MouseListener and therefor react to the mouse-pressed, mouse-released, mouse-clicked"+
           "\n Items to test:\n --------------\n"+
           "=> Clicking and dragging the mouse in the left field should make the point in the right field move just as if the mouse"+
           " would be positioned on this exact location in the right field and vice versa\n"+
           "=> In the list you should see that a mouse event thrown by the right field is catched by the left one and vice versa\n"+
           "=> Check the coordinates to see if the mouse listener really displays the coordinated according to the field the mouse is in "+
           "\n\nPs: the behavior of the event fields to the panels is:\n"+
           "=> Mouse pressed: the point is set to the nearest grid intersection. the point is drawn in the current drawing color. Also drawn are grid lines"+
           " from the point to the horizontal and vertical axis.\n"+
           "    Furthermore, a line is drawn between the 'grid' position of the point and the actual mouse position\n"+
           "=> Mouse released: the point is set to the nearest grid intersection, no lines are drawn\n"+
           "=> Mouse clicked: the drawing color shifts to the next color of the rainbow\n"+
           "=> Mouse moved: the point should swap to the next grid intersection as soon as the mouse position is nearer to that position as to the current\n"+
           "=> Mouse dragged: a line is drawn to the mouse position, as soon as the position is nearer to the next grid, the point is moved to that grid"+
           "=> mouse entered / mouse exited: A mouse-entered/mouse-exited event is thrown, but no painting is done"+
           " (this is different from the standard field behavior of the other tests)";
  }

  /***************************************************************************************************/
  /** toString function to display in mouseEvent.getSource().toString()
  */
  public String toString() {
    return "MouseEventTest";
  }


}