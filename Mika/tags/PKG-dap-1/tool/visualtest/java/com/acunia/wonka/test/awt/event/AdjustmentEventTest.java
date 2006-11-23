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

import com.acunia.wonka.test.awt.*;
import java.awt.event.*;
import java.awt.*;

public class AdjustmentEventTest extends VisualTestImpl implements AdjustmentListener ,CollectsEvents {

  private NamedScrollbar leftVertical;
  private NamedScrollbar rightVertical;
  private NamedScrollbar centerHorizontal;
  private AdjustmentDisplay display1;
  private List display2;

  public AdjustmentEventTest() {
    setForeground(new Color(108,56,32));
    setBackground(new Color(64,160,96));
    int step = 100/4;
    int green = 155-step;
    //int blue = 155-step;
    setLayout(new BorderLayout());

    display1 = new AdjustmentDisplay(new Color(64,255,255), getForeground());
    setLayout(new BorderLayout());

    Panel center = new Panel(new BorderLayout());
      leftVertical = new NamedScrollbar("<Left Vertical>",Adjustable.VERTICAL,0,10,0,100);
      leftVertical.setBackground(new Color(64,green,green));
      leftVertical.addAdjustmentListener(this);
      leftVertical.addAdjustmentListener(display1);
      center.add(leftVertical,BorderLayout.WEST);
      green+= step;

      centerHorizontal = new NamedScrollbar("<Center Horizontal>",Adjustable.HORIZONTAL,0,200,0,1000);
      centerHorizontal.setBackground(new Color(64,green,green));
      centerHorizontal.addAdjustmentListener(this);
      centerHorizontal.addAdjustmentListener(display1);
      center.add(centerHorizontal,BorderLayout.CENTER);
      green+= step;

      rightVertical = new NamedScrollbar("<Right Vertical>",Adjustable.VERTICAL,200,200,200,1000);
      rightVertical.setBackground(new Color(64,green,green));
      rightVertical.addAdjustmentListener(this);
      rightVertical.addAdjustmentListener(display1);
      center.add(rightVertical,BorderLayout.EAST);
      green+= step;

      center.add(display1,BorderLayout.SOUTH);
    add(center, BorderLayout.CENTER);

    display2=new List(3,false);
    display2.add("Your ItemEvents displayed HERE");
    add(display2, BorderLayout.SOUTH);
  }

  /****************************************************************/
  /** ItemListener event (there one and only) : Display it in the list
  */
  public void adjustmentValueChanged(AdjustmentEvent evt) {
    String[] messagestrings = AdjustmentDisplay.displayAdjustmentEvent(evt);
    if(display2.getItemCount()>40) {
      display2.removeAll();
    }
    for(int i=messagestrings.length-1; i>=0; i--) {
      display2.add(messagestrings[i],0);
    }
  }
  /****************************************************************/
  /** CollectsEvent interface display messages : do nothing, we only display our own messages
  */
  public void displayMessage(String[] messagestrings) {
    if(display2.getItemCount()>40) {
      display2.removeAll();
    }
    for(int i=messagestrings.length-1; i>=0; i--) {
      display2.add(messagestrings[i],0);
    }
  }

  public String getHelpText() {
    return "The aim: test the throwing and the correct layout of AdjustmentEvents:\n\n"+
           "The test: The upper part of the screen consists out of a series of horizontal and vertical scrollbars. The lower part"+
           " consists out of an event panel and a list. All AdjustmentEvents fired by  the scrollbars are caught and displayed in both"+
           " the list and the panel.\n"+
           "\nItems to test:\n"+
           "- Event generating and catching: Every movement of the scrollbox of one of the scrollboxes should fire an AdjustmentEvent"+
           " that is displayed BOTH in the event panel as in the lower list\n"+
           "- Correct event data: Every event is analysed and displayed in two lines in the lower list:\n"+
           "   => first line: the object generating the event: THis is the 'name' of the scrollbar\n"+
           "   Furthermore it should display the corrent value for AWTEvent.getID(). (" + AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED+
           " for <AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED > )\n"+
           "   => second line: the AdjustmentEvent's type flag being one of <UNIT_INCREMENT>, <BLOCK_INCREMENT>, <BLOCK_DECREMENT>,"+
           " <UNIT_DECREMENT> or <TRACK>according to the field the scrollbar was clicked\n"+
           "   Also in this line is the position value of the scrollbox\n"+
           " \n (The Scrollpane-class also throwing an ItemEvent is not covered here for reasons of it not being implemented yet in Wonka)\n";


  }

  /****************************************************************/
  /** Overrides the Scrollbar class to) provide a name to display in the toString() function  instead of the diagnostics
  */
  class NamedScrollbar extends Scrollbar {
    String name;

    public NamedScrollbar(String name, int alignment, int position, int span, int minimum, int maximum) {
      super(alignment, position, span, minimum, maximum);
      //setBlockIncrement(10);
      this.name = name;
    }

    public String toString() {
      return name;
    }
  }



}
