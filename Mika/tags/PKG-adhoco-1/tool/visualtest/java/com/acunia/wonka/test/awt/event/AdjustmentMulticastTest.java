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

public class AdjustmentMulticastTest extends VisualTestImpl implements ActionListener {

  private Button[] add;
  private Button[] remove;
  private AdjustmentDisplay[] display;
  private NamedScrollbar generator;
  final static int LISTENERS = 5;
  private Button ten;
  private Button fifty;
  private Button twofifty;

  public AdjustmentMulticastTest() {
    setForeground(new Color(108,56,32));
    setBackground(new Color(64,160,96));
    int step = 100/LISTENERS;
    int green = 155-step;
    int blue = 155-step;

    setLayout(new BorderLayout());
    Panel width = new Panel(new GridLayout(1,3));
      ten = new Button("width 10");
      ten.addActionListener(this);
      ten.setBackground(new Color(64,green,blue));
      width.add(ten);
      green+=step;
      fifty = new Button("width 50");
      fifty.addActionListener(this);
      fifty.setBackground(new Color(64,green,blue));
      width.add(fifty);
      blue+=step;
      twofifty = new Button("width 250");
      twofifty.addActionListener(this);
      twofifty.setBackground(new Color(64,green,blue));
      width.add(twofifty);
    add(width,BorderLayout.NORTH);
    generator = new NamedScrollbar("<main scrollbar>",10);
    add(generator, BorderLayout.CENTER);

    Panel listeners = new Panel(new GridLayout(LISTENERS,1));
      Panel[] row = new Panel[LISTENERS];
      add = new Button[LISTENERS];
      display = new AdjustmentDisplay[LISTENERS];
      remove = new Button[LISTENERS];
      for(int i=0; i<LISTENERS; i++) {
        row[i] = new Panel(new BorderLayout() );
          display[i] = new AdjustmentDisplay("Press <add> to add a listener to this panel", new Color(64,green,blue),new Color(128,50,80));
          row[i].add(display[i],BorderLayout.CENTER);
          green+= step;
          add[i] = new Button("Add");
          add[i].setBackground(new Color(64,green,blue));
          add[i].addActionListener(this);
          row[i].add(add[i],BorderLayout.WEST);
          blue+= step;
          remove[i] = new Button("Remove");
          remove[i].setBackground(new Color(64,green,blue));
          remove[i].addActionListener(this);
          row[i].add(remove[i], BorderLayout.EAST);
        listeners.add(row[i]);

      }
    add(listeners, BorderLayout.SOUTH);

  }


  /****************************************************************/
  /** ActionListener interface actionPerformed:
  * with the <add> or <remove> button clicked, add or remove the Item listener to its panel
  */
  public void actionPerformed(ActionEvent evt) {
    boolean found = false;
    Object source = evt.getSource();
    for(int i=0; i<LISTENERS && !found; i++) {
      if(source == add[i]) {
        generator.addAdjustmentListener(display[i]);
        display[i].displayMessage("Listener added. press <remove> to remove it again");
        found = true;
      }
      else if(source == remove[i]) {
        generator.removeAdjustmentListener(display[i]);
        display[i].displayMessage("Listener removed. press <add> to add it again");
        found = true;
      }
    }
    if(!found){
      if(source == ten) {
        generator.setNewMaximum(10);
      }
      else if (source == fifty) {
        generator.setNewMaximum(50);
      }
      else if (source == twofifty) {
        generator.setNewMaximum(250);
      }
    }
  }

  /****************************************************************/
  /** CollectsEvent interface display messages : do nothing, we only display our own messages
  */
  public void displayMessage(String[] messagestrings) {
  }

  public String getHelpText() {
    return "The aim: test the throwing of AdjustmentEvents through the AWTEventMulticaster functions:\n\n"+
           "The screen consists out a central scrollbar, three <width> buttons and "+LISTENERS+" AdjustmentDisplay panels. Each of this panels"+
           " is flanked by an <add> and a <remove> button.\n"+
           "The scrollbar throws an AdjustmentEvent every time the position of its scrollbox is changed, either by pressing the up and down"+
           " buttons, by clicking on the field before or after the scrollbox, or by dragging the box with the mouse.\n"+
           " Using the width buttons, you can set the scrollbox' width to either ten, fifty or two hundred and fifty units. Using the <add>"+
           " and <remove> buttons, you can add an ItemListener to a panel in order to get the ItemEvents displayed on that panel,"+
           " or remove the listener from that panel again\n."+
           "(Adding and removing is done by calls to the static AWTEventMulticaster.Add()and -remove() functions\n)"+
           "\n Items to test : \n -------------\n"+
           " => Pressing <add> for a panel and subsequently clicking in the event field to check if the AdjustmentEvent is displayed"+
           " on the newly selected panel, as well as on all other previously selected panels\n"+
           " => Pressing <remove> for a panel and subsequently clicking in the event field to check if the event is no longer"+
           " displayed on that panel, nor on the panels previously deselected, yet remains displayed on all other panels still selected\n"+
           " => Pressing <add> for the same panel over and over again to see that the panel is not added twice\n"+
           " => Pressing <remove> for the same panel over and over again, or pressing <remove> on a panel to which no listener is added yet"+
           " to check that a panel is not removed twice\n"+
           " => SPEED CHECK: set the scrollbar width to the maximum and drag the scrollbox with the mouse. Check if the response time to the"+
           " continuous stream of AdjustmentEvents thrown through the Multicaster is not slowing down other operations"+
           " \n ps. as the Add and remove routines have a slightly different algorithm for the first and second listener then for all"+
           " subsequent listeners, specially check the behavior when \n"+
           "    - adding the first panel, adding the second panel, adding the third panel\n"+
           "    - removing the third-last panel, removing the second-last panel, removing the last panel\n"+
           "    - giving a remove-command when no panels are selected";


  }

  /****************************************************************/
  /** Overrides the Scrollbar class to
  * 1) calculate position, width and block increment out of new maximum on changing scrollbar max
  * 2) provide a name to display in the toString() function  instead of the diagnostics
  */
  class NamedScrollbar extends Scrollbar {
    String name;

    public NamedScrollbar(String name, int maximum) {
      super(Adjustable.HORIZONTAL, 0, 5, 0, maximum);
      //setBlockIncrement(10);
      this.name = name;
    }

    public void setNewMaximum(int newmax) {
      int newval= getValue()*newmax/getMaximum();
//      setValues(newval,newmax/10,0,newmax);
//      setBlockIncrement((newmax>20)?newmax/10:2);
      setValues(newval,5,0,newmax);
      //setBlockIncrement((newmax>20)?10:newmax/2);
    }

    public String toString() {
      return name;
    }
  }


}
