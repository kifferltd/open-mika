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

public class KeyMulticastTest extends VisualTestImpl implements ActionListener{
  /** Variables */
  private Button[] add;
  private Button[] remove;
  private EventKeyDisplay[] display;
  final static int LISTENERS = 7;

  /****************************************************************/
  /** constructor
  */
  public KeyMulticastTest() {
    int step=0x60/LISTENERS;
    int gray=0x60;
    int darkgray=0x30;

    setLayout(new GridLayout(LISTENERS,1));
    //setBackground(Color.gray);
    Panel[] row = new Panel[LISTENERS];
    add = new Button[LISTENERS];
    display = new EventKeyDisplay[LISTENERS];
    remove = new Button[LISTENERS];

    for(int i=0; i<LISTENERS; i++) {
      row[i] = new Panel(new BorderLayout());
          add[i] = new Button("Add");
          add[i].setBackground(new Color(gray,gray,gray));
          add[i].addActionListener(this);
        row[i].add(add[i],BorderLayout.WEST);
          display[i]=new EventKeyDisplay("Listener_"+i, new Color(darkgray,darkgray,darkgray), new Color(gray,gray,gray+0x10), Color.white);
        row[i].add(display[i],BorderLayout.CENTER);
          remove[i] = new Button("remove");
          remove[i].setBackground(new Color(gray,gray,gray+0x20));
          remove[i].addActionListener(this);
        row[i].add(remove[i], BorderLayout.EAST);
        gray+=step;
        darkgray+=step;
      add(row[i]);
    }
  }

  /****************************************************************/
  /** action listener: if listClear pressed, clear list*/
  public void actionPerformed(ActionEvent evt) {
    boolean found = false;
    Object source = evt.getSource();
    for(int i=0; i<LISTENERS && !found; i++) {
      if(source == add[i]) {
        for(int j=0; j<LISTENERS; j++) {
          display[j].addKeyListener(display[i]);
        }
        display[i].displayMessage("<"+display[i]+"> Listener added. press <remove> to remove it again");
        found = true;
      }
      else if(source == remove[i]) {
        for(int j=0; j<LISTENERS; j++) {
          display[j].removeKeyListener(display[i]);
        }
        display[i].displayMessage("<"+display[i]+"> Listener removed. press <add> to add it again");
        found = true;
      }
    }
  }

  /****************************************************************/
  /**
  * our special class of KeyDisplay featuring a different color on focus
  * a forward of the key events to the list
  * and a name for the toString method
  */
  private class EventKeyDisplay extends KeyDisplay  implements KeyListener, FocusListener {
    //variables
    //protected Color background;
    //protected Color foreground;
    protected Color active;
    protected Color passive;
    //protected String message;
    protected String name;

    //constructor
    public EventKeyDisplay(String name, Color active, Color passive, Color text) {
      super(name, passive, text);
      this.active = active;
      this.passive = passive;
      this.name = name;
      // listeners
      this.addFocusListener(this);
    }

    // overrides Object.toString to return the objects name
    public String toString() {
      return name;
    }

    // focus listener switches background color if becomes active or passive
    public void focusGained(FocusEvent evt) {
      background = active;
      message=name+" (active)";
      this.repaint();
    }

    public void focusLost(FocusEvent evt) {
      background = passive;
      message=name+" (lost focus)";
      this.repaint();
    }

/*    // key listener overrides: show listeners name next to event shortcut
    public void keyPressed(KeyEvent evt) {
      message = name+": "+displayKeyShortcut(evt);
      this.repaint();
    }

    public void keyReleased(KeyEvent evt) {
      message = name+": "+displayKeyShortcut(evt);
      this.repaint();
    }

    public void keyTyped(KeyEvent evt) {
      message = name+": "+displayKeyShortcut(evt);
      this.repaint();
    }
*/
  }


  /****************************************************************/
  /** Help text
  */
  public String getHelpText() {
    return "The aim: test the throwing of KeyEvents through the AWTEventMulticaster functions:\n\n"+
           "The screen consists out of "+LISTENERS+" KeyDisplay panels. Each of this panels is flanked by an <add> and a <remove> button.\n"+
           "Every time a key is pressed, released or typed, the panel that currently has the focus receives a key event. Every other panel"+
           " that has a key listener for this panel will receive the key event through the listener interface functionality\n"+
           "(A panel that has the focus is shown in a darker color)\n"+
           " Using the Add/Delete buttons next to the panels you can add to that panel a KeyListener to all the panels on the screen,"+
           " (including the selected panel itself)and delete it again. Upon receipt of a key event (through its listeners if added), that"+
           " panel will display a short description of the event received\n."+
           "(Adding and removing is done by calls to the static AWTEventMulticaster.Add()and -remove() functions)\n"+
           "\n Items to test : \n -------------\n"+
           " => Pressing <add> for a panel and subsequently clicking on one of the panels. On the newly selected panel,"+
           " as well as on all other previously selected panels, you should see a key event for the key typed and the panel currently active\n"+
           " => Pressing <remove> for a panel and subsequently clicking a button you should see the KeyEvents no longer"+
           " displayed on that panel, nor on the panels previously deselected, yet still displayed on the other panels still selected\n"+
           " => Pressing <add> for the same panel over and over again, nothing should happen as a panel can not be added twice\n"+
           " => Pressing <remove> for the same panel over and over again, or pressing <remove> on a panel to which no listener is added yet"+
           " nothing should happen as a panel can not be removed twice\n"+
           " \n ps. as the Add and remove routines have a slightly different algorithm for the first and second listener then for all"+
           " subsequent listeners, specially check the behavior when \n"+
           "    - adding the first panel, adding the second panel, adding the third panel\n"+
           "    - removing the third-last panel, removing the second-last panel, removing the last panel\n"+
           "    - giving a remove-command when no panels are selected"+
           "\n Disclaimer : No panels were harmed during the making of this test.";
  }

}
