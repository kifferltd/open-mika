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


package com.acunia.wonka.test.awt.Button;

import com.acunia.wonka.test.awt.*;       // Visualtestengine
import com.acunia.wonka.test.awt.event.ActionDisplay; // access to ActionDisplay.displayActionEvent(event)
import java.awt.event.*;
import java.awt.*;

public class ActionCommandTest extends VisualTestImpl implements ActionListener, ItemListener {

  final static Color DARKRED = new Color(128,32,32);
  final static Color DARKGREEN = new Color(32,128,32);
  final static Color DARKBLUE = new Color(32,32,128);
  final static String RED = "<RED>";
  final static String BLUE = "<BLUE>";
  final static String GREEN = "<GREEN>";

  private Button clickme;
  private Checkbox setRed;
  private Checkbox setGreen;
  private Checkbox setBlue;
  private Checkbox setNULL;

  private TextField commandDisplay;
  private List eventDisplay;

  public ActionCommandTest() {
    setBackground(new Color(64,64,64));
    setForeground(Color.white);
    setLayout(new BorderLayout());

    clickme = new Button("Click Me");
    clickme.addActionListener(this);
    add(clickme, BorderLayout.CENTER);

    Panel radio = new Panel(new GridLayout(6,1));
      CheckboxGroup group  = new CheckboxGroup();
      setRed = new Checkbox("setActionCommand("+RED+")", false, group);
      setRed.addItemListener(this);
      radio.add(setRed);
      setGreen = new Checkbox("setActionCommand("+GREEN+")", false, group);
      setGreen.addItemListener(this);
      radio.add(setGreen);
      setBlue = new Checkbox("setActionCommand("+BLUE+")", false, group);
      setBlue.addItemListener(this);
      radio.add(setBlue);
      setNULL = new Checkbox("setActionCommand(NULL)", true, group);
      setNULL.addItemListener(this);
      radio.add(setNULL);
      radio.add(new Label("getActionCommand returns :", Label.CENTER));
      commandDisplay = new TextField(clickme.getActionCommand());
      radio.add(commandDisplay);
    add(radio, BorderLayout.EAST);

    eventDisplay=new List(3,false);
    eventDisplay.setBackground(new Color(180,180,112));
    eventDisplay.add("Your ActionEvents displayed HERE");
    add(eventDisplay, BorderLayout.SOUTH);
  }

  /************************************************************************************************************/
  /** Actionlistener : display ActionEvent from button in list
  */
  public void actionPerformed(ActionEvent evt) {
    String[] messagestrings = ActionDisplay.displayActionEvent(evt);
    eventDisplay.removeAll();
    for(int i=messagestrings.length-1; i>=0; i--) {
      eventDisplay.add(messagestrings[i],0);
    }
  }

  /************************************************************************************************************/
  /** ItemListener: set the buttons action command to red, green, blue or null respectingly
  */
  public void itemStateChanged(ItemEvent evt) {
    Object source = evt.getSource();
    if(source == setRed) {
      clickme.setActionCommand(RED);
      clickme.setBackground(DARKRED);
    }
    else if(source == setGreen) {
      clickme.setActionCommand(GREEN);
      clickme.setBackground(DARKGREEN);
    }
    else if(source == setBlue) {
      clickme.setActionCommand(BLUE);
      clickme.setBackground(DARKBLUE);
    }
    else { //if (source == setNull)
      clickme.setActionCommand(null);
      clickme.setBackground(getBackground());
    }
    commandDisplay.setText(clickme.getActionCommand());
  }

  public String getHelpText() {
    return "The aim: test the setting and retrieving of a button's Action command:\n\n"+
           "The test:\n => Use the radio buttons on the right to set the button's action command to <red>, <green> and <blue> respectingly"+
           " using the function < Button.setActionCommand() >:\n"+
           " Directly afterwards the test makes a call to < Button.getActionCommand() > to display the new action command on the text area\n"+
           " => click the button to throw an ActionPerformed() event and see the contents of that call's ActionEvent in the list below.\n"+
           "\nItems to test:\n"+
           " => the action command in the text box should match the strings <RED>, <GREEN> or <BLUE> set by the setActionCommand() function"+
           " or should match the button's label 'Click me' if setActioncommand(null) was called\n"+
           " => Also the contents of the text box should be returned in line two of the list as a result of ActionEvent.getActioncommand()"+
           " every time the button is clicked (and a new ActionEvent is thrown)";


  }
}
