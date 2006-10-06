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

public class MouseListenerOverride extends VisualTestImpl implements  MouseListener, CollectsEvents {

  /***************************************************************************************************/
  /** variables
  */
  private List display;
  private GridMouseGenerator trigger;

  /***************************************************************************************************/
  /** Constructor
  */
  public MouseListenerOverride() {
    setLayout(new BorderLayout());

    trigger= new GridMouseGenerator("<MouseGenerator>",MouseGeneratorComponent.DARKGREEN,MouseGeneratorComponent.DUSTGREEN,this);
    trigger.addMouseListener(this);
    add(trigger, BorderLayout.CENTER);

    display = new List(6,false);
    display.add("See your mouse events here");
    add(display,BorderLayout.SOUTH);
  }


  /***************************************************************************************************/
  /** Mouse events received from second generator */
  public void mouseClicked(MouseEvent event) {
    displayMessage(MouseGeneratorComponent.displayMouseEvent(event, "<MouseListenerOverride>"));
  }

  public void mouseEntered(MouseEvent event) {
    displayMessage(MouseGeneratorComponent.displayMouseEvent(event, "<MouseListenerOverride>"));
  }

  public void mouseExited(MouseEvent event) {
    displayMessage(MouseGeneratorComponent.displayMouseEvent(event, "<MouseListenerOverride>"));
  }

  public void mousePressed(MouseEvent event) {
    displayMessage(MouseGeneratorComponent.displayMouseEvent(event, "<MouseListenerOverride>"));
  }

  public void mouseReleased(MouseEvent event) {
    displayMessage(MouseGeneratorComponent.displayMouseEvent(event, "<MouseListenerOverride>"));
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
  /** Own version of  MouseGeneratorComponent that overwrites its mouse listener to display the mouse position in grid coordinates
  */
  class GridMouseGenerator extends MouseGeneratorComponent {

    MouseListener ml;

    public GridMouseGenerator(String componentname, Color back, Color front, CollectsEvents parentinstance){
      super(componentname, back, front, parentinstance);
      ml=null;
    }

    /****************************************************************************************************************************************/
    /**
    * overwrite addListener to still call our mouse events when locked to another listener
    */
    public void addMouseListener(MouseListener newlistener) {
       ml=newlistener;
      super.addMouseListener(this);
    }

    /************************************************************************************************************/
    /** mouse events
    */
    protected MouseEvent eventToGrid(MouseEvent event) {
      return new MouseEvent(this, event.getID(),System.currentTimeMillis(),InputEvent.BUTTON1_MASK,gridPoint.x, -gridPoint.y,1,false);
    }

    public void mouseClicked(MouseEvent event) {
      if(ml==this) {
        parent.displayMessage(displayMouseEvent(eventToGrid(event),name));
      }
      else {
        ml.mouseClicked(eventToGrid(event));
      }
      if(currentColor>0) {
        currentColor--;
      }
      else{
        currentColor=colors.length-1;
      }
      this.repaint();
    }

    public void mouseEntered(MouseEvent event) {
      gridPoint = screenToGrid(event.getPoint());
      screenPoint = gridToScreen(gridPoint);
      mousePoint = event.getPoint();
      if(ml==this) {
        parent.displayMessage(displayMouseEvent(eventToGrid(event),name));
      }
      else {
        ml.mouseEntered(eventToGrid(event));
      }
      connected=false;
      this.repaint();
    }

    public void mouseExited(MouseEvent event) {
      gridPoint = screenToGrid(event.getPoint());
      screenPoint = gridToScreen(gridPoint);
      mousePoint = event.getPoint();
      if(ml==this) {
        parent.displayMessage(displayMouseEvent(eventToGrid(event),name));
      }
      else {
        ml.mouseExited(eventToGrid(event));
      }
      connected=true;
      this.repaint();
    }

    public void mousePressed(MouseEvent event) {
      gridPoint = screenToGrid(event.getPoint());
      screenPoint = gridToScreen(gridPoint);
      mousePoint = event.getPoint();
      if(ml==this) {
        parent.displayMessage(displayMouseEvent(eventToGrid(event),name));
      }
      else {
        ml.mousePressed(eventToGrid(event));
      }
      connected=true;
      this.repaint();
    }

    public void mouseReleased(MouseEvent event) {
      gridPoint = screenToGrid(event.getPoint());
      screenPoint = gridToScreen(gridPoint);
      mousePoint = event.getPoint();
      if(ml==this) {
        parent.displayMessage(displayMouseEvent(eventToGrid(event),name));
      }
      else {
        ml.mouseReleased(eventToGrid(event));
      }
      connected=false;
      this.repaint();
    }
  }
  /***************************************************************************************************/
  /** test description for the VisualTestEngine help
  */
  public String getHelpText(){
    return " A test on overriding the mouse listener interface: \n"+
           " The panel shows a mouse event fields and a list. Just as in the previous tests, the list wil display"+
           " the mouse events thrown. However, this time the event field's mouse listener is overridden such that it shows the"+
           " mouse movements on the screen, but rather then disposing them there, calculates the mouse position into grid coordinates"+
           " and then forwards the event to the assigned listener. (in this case the main test class). \n"+
           " CONSEQUENTLY ANY MOUSE ACTION IN THE LEFT PANEL WILL RESULT IN THE RIGHT PANEL DISPLAYING THE RESULT OF THIS ACTION"+
           "\n Items to test:\n --------------\n"+
           "=> reaction to mouse events, display of the right event ID, multiple displays of events as in the MouseEvent class\n"+
           "=> Source and catcher object: the event must be originated by the mouse event field and catched by the test application\n"+
           "=> Mouse coordinates: the mouse coordinates must be displained in grid coordinates ranging from -12 to +12 horizontally"+
           " and -3 to +3 vertically .\n"+
           "\n The mouse reacts to mouse events (no mouse-motion events) in the following way:\n"+
           "=> Mouse released: the point is set to the nearest grid intersection, no lines are drawn\n"+
           "=> Mouse clicked: the drawing color shifts to the next color of the rainbow\n"+
           "=> mouse entered: the point is moved to the nearest intersection\n"+
           "=> mouse exited: the point is moved to the nearest intersection, grid lines are shown"+
           " and a line is drawn from the point to the actual mouse position";
  }

  /***************************************************************************************************/
  /** toString function to display in mouseEvent.getSource().toString()
  */
  public String toString() {
    return "MouseEventTest";
  }


}