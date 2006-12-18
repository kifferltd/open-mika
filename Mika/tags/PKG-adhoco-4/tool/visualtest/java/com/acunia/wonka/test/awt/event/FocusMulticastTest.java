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

public class FocusMulticastTest extends VisualTestImpl implements ActionListener, FocusListener {

  private Button[] add;
  private Button[] remove;
  private FocusDisplay[] display;
  final static int LISTENERS = 7;

  public FocusMulticastTest() {
    setForeground(new Color(32,80,32));
    setBackground(new Color(64,128,64));
    int step = 100/LISTENERS;
    int green = 155;
    int shade1 = 100;
    int shade2 = 100;

    setLayout(new GridLayout(LISTENERS,1));
    Panel[] row = new Panel[LISTENERS];
    add = new Button[LISTENERS];
    display = new FocusDisplay[LISTENERS];
    remove = new Button[LISTENERS];
    for(int i=0; i<LISTENERS; i++) {
      row[i] = new Panel(new BorderLayout() );
        add[i] = new Button("Add");
        add[i].setBackground(new Color(shade1,green,shade2));
        add[i].addActionListener(this);
        row[i].add(add[i],BorderLayout.WEST);
        shade1+=step;

        display[i] = new FocusDisplay("Display_"+i, "<Display"+i+"> (Press <add> to add a listener)", new Color(shade1,green,shade2),getForeground());
        display[i].addFocusListener(this);
        row[i].add(display[i],BorderLayout.CENTER);
        green+= step;

        remove[i] = new Button("Remove");
        remove[i].setBackground(new Color(shade1,green,shade2));
        remove[i].addActionListener(this);
        row[i].add(remove[i], BorderLayout.EAST);
        shade2+= step;
      add(row[i]);
    }

  }


  /************************************************************************************************************/
  /** ActionListener interface actionPerformed:
  * with the <add> or <remove> button clicked, add or remove the Item listener to its panel
  */
  public void actionPerformed(ActionEvent evt) {
    boolean found = false;
    Object source = evt.getSource();
    for(int i=0; i<LISTENERS && !found; i++) {
      if(source == add[i]) {
        for(int j=0; j<LISTENERS; j++) {
          display[j].addFocusListener(display[i]);
        }
        display[i].displayMessage("<"+display[i]+"> Listener added. press <remove> to remove it again");
        found = true;
      }
      else if(source == remove[i]) {
        for(int j=0; j<LISTENERS; j++) {
          display[j].removeFocusListener(display[i]);
        }
        display[i].displayMessage("<"+display[i]+"> Listener removed. press <add> to add it again");
        found = true;
      }
    }
  }

  /************************************************************************************************************/
  /** Our own focus event: paint the focusDisplay mentioned
  */
    /**focus event*/
    public void focusGained(FocusEvent evt) {
      ((FocusDisplay)(evt.getSource())).setFocus(true);
    }

    public void focusLost(FocusEvent evt) {
      ((FocusDisplay)(evt.getSource())).setFocus(false);
    }
  /************************************************************************************************************/
  /** CollectsEvent interface display messages : do nothing, we only display our own messages
  */
  public void displayMessage(String[] messagestrings) {
  }

  public String getHelpText() {
    return "The aim: test the throwing of FocusEvents through the AWTEventMulticaster functions:\n\n"+
           "The screen consists out of "+LISTENERS+" FocusDisplay panels. Each of this panels is flanked by an <add> and a <remove> button.\n"+
           "Furthermore, each of this panels has a focuslistener that throws an event every time it gained or lost focus"+
           " Using the Add/Delete buttons next to the panels you can add to that panel the Focuslistener of all the panels on the screen,"+
           " including the selected panel itself. A panel displays its panel name and the two last FocusEvents it received\n."+
           "(Adding and removing is done by calls to the static AWTEventMulticaster.Add()and -remove() functions)\n"+
           "\n Items to test : \n -------------\n"+
           " => Pressing <add> for a panel and subsequently clicking on one of the panels. On the newly selected panel,"+
           " as well as on all other previously selected panels, you should see a focus-lost event for the panel previously clicked"+
           " and a focus-gained event for the panel just clicked\n"+
           " => Pressing one of the panels, you should see a focus-lost event for the panel previously clicked"+
           " and a focus-gained event for the panel just clicked. (this on all panels selected)"+
           " => Pressing one of the buttons either an add/remove button of the test, or a VisualTestEngine button, you should see"+
           " a new event a focus-lost event for the panel previously clicked (the previous event also displayed should be"+
           " the focus-gained event for that same panel)\n"+
           " => Pressing <remove> for a panel and subsequently clicking a button you should see the FocusEvent no longer"+
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

  /**
  * inner class AWTEventdisplay with text listener
  */
  class FocusDisplay extends AWTEventDisplay implements FocusListener {
    /**name variable*/
    private String name;
    private String lastEvent;
    private boolean hasFocus;

    /**Constructor*/
    public FocusDisplay(String title, String firstmessage, Color back, Color front){
      super(firstmessage, back, front);
      name = title;
      lastEvent = "2) Nothing yet";
      hasFocus=false;
    }

    /** set focus*/
    public void setFocus(boolean focus) {
      hasFocus=focus;
      this.repaint();
    }

    /**focus event*/
    public void focusGained(FocusEvent evt) {
      message = displayFocusEventShortcut(evt);
      this.repaint();
    }

    /**focus event*/
    public void focusLost(FocusEvent evt) {
      message = displayFocusEventShortcut(evt);
      this.repaint();
    }

    /**toString : return name*/
    public String toString() {
      return name;
    }

     /** Overrides AWTEventDisplay.update to change color when got focus
    */
  	public void update(Graphics g) {
      // first time initialiser
      if(frame.width==0 ){
        frame.setSize(this.getSize().width-2, this.getSize().height-2);
        inside.setBounds(5,5, this.getSize().width-10, this.getSize().height-10);
      }
      g.setColor((hasFocus)?foreground:background);
      g.fillRect(1,1, frame.width, frame.height);
      g.setColor((hasFocus)?background:foreground);
      g.drawRect(inside.x, inside.y, inside.width, inside.height);
      g.drawString(message,20,17);
    }

    /** event text
    */
    public String displayFocusEventShortcut(FocusEvent evt) {
      String line = "<"+name+"> :1)" + evt.getSource();
      line += (evt.getID()==FocusEvent.FOCUS_GAINED) ? ":FOCUS GAINED ": ":FOCUS LOST ";
      line += lastEvent;
      lastEvent = "2)"+evt.getSource();
      lastEvent+=(evt.getID()==FocusEvent.FOCUS_GAINED) ? ":FOCUS GAINED ": ":FOCUS LOST ";
      return line;
    }
  }




}
