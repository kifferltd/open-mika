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

// V.1.01.00 2001/08/29 : first release
// V.1.01.01 2001/08/29 : excluded CVS directories from files list
// V.1.01.02 2001/08/29 : Option to only load checked files
// V.1.02.01 2001/08/31 : Double list and quick scan possibilities
// V.1.02.02 2001/08/31 : bugfixes and selective file-load for subdirs
// V.1.02.03 2001/08/31 : added buttons for showing scripts and c/h/java files
// V.1.02.03 2001/09/03 : logging file
// V.1.02.04 2001/09/03 : edit screen allows to place header on current text position / button for header files only
// V.1.02.05 2001/09/03 : added buttons for same directory and last directory


// Author: N.Oberfeld
// Version 1.01.01
// Created: 2001/08/29

package com.acunia.wonka.test.awt.event;

//import
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class WindowMulticastTest extends VisualTestImpl implements ActionListener {
	
  /****************************************************************/
  /** variables
  */
  private Window testWindow;

  private Button build;
  private Button kill;
  private Button show;
  private Button hide;
  private WindowEventDisplay anchor;

  private Button[] add;
  private Button[] remove;
  private WindowEventDisplay[] display;
  final static int LISTENERS = 5;

  /****************************************************************/
  /** constructor
  */
  public WindowMulticastTest() {
    super();

    // build screen
    setLayout(new BorderLayout());
    setBackground(new Color(128,32,128));
    setForeground(Color.white);
    int lighter = 0x60;
    int darker = 0x40;
    // button row
    Panel buttons = new Panel(new GridLayout(1,4));
      build = new Button("[*]build window");
      build.setBackground(new Color(lighter+=0x10,32,darker+=0x10));
      build.addActionListener(this);
      buttons.add(build);
      // show
      show = new Button("[ ] show window");
      show.setBackground(new Color(lighter+=0x10,32,darker+=0x10));
      show.addActionListener(this);
      buttons.add(show);
      // hide
      hide = new Button("[.] hide window");
      hide.setBackground(new Color(lighter+=0x10,32,darker+=0x10));
      hide.addActionListener(this);
      buttons.add(hide);
      // dispose & delete
      kill = new Button("[x] dispose window");
      kill.setBackground(new Color(lighter+=0x10,32,darker+=0x10));
      kill.addActionListener(this);
      buttons.add(kill);
    add(buttons, BorderLayout.NORTH);

    // window anchor
    anchor = new WindowEventDisplay("Your window displayed here",new Color(darker,32,darker+=0x10),Color.white);
    add(anchor , BorderLayout.CENTER);

    // window listeners
    Panel listeners = new Panel(new GridLayout(LISTENERS,1));
      Panel[] row = new Panel[LISTENERS];
      add = new Button[LISTENERS];
      display = new WindowEventDisplay[LISTENERS];
      remove = new Button[LISTENERS];
      for(int i=0; i<LISTENERS; i++) {
        row[i] = new Panel(new BorderLayout() );
          display[i] = new WindowEventDisplay("press <build> to create a new window", new Color(darker-=0x10,48,lighter-=0x10), Color.white);
          row[i].add(display[i],BorderLayout.CENTER);
          add[i] = new Button("Add");
          add[i].setBackground(new Color(darker,32,lighter));
          add[i].addActionListener(this);
          row[i].add(add[i],BorderLayout.WEST);
          remove[i] = new Button("Remove");
          remove[i].setBackground(new Color(darker,64,lighter));
          remove[i].addActionListener(this);
          row[i].add(remove[i], BorderLayout.EAST);
        listeners.add(row[i]);

      }
    add(listeners, BorderLayout.SOUTH);
  }

  /* on startup,do layout
  */
  //public void start(java.awt.Panel p, boolean b){} already done in constructor

  /**
  ** on stop, close all windows that aren't closed yet
  */
  public void stop(java.awt.Panel p){
    if(testWindow != null) {
      testWindow.dispose();
      testWindow = null;
    }
  }

  /****************************************************************/
  /**button pressed : Display desired dialog window */
  public void actionPerformed(ActionEvent evt) {
    Component source = (Component)evt.getSource();
    if(source == build && testWindow == null){
      //displayText("... building new  window");
      testWindow = buildWindow("Event Test Window", Color.red, anchor);
      for(int i=0; i<LISTENERS; i++) {
        display[i].displayMessage("Window built, press <add> to add this listener");
      }
    }
    else if(source == kill && testWindow != null) {
      //displayText("... initiating -window.dispose()");
      for(int i=0; i<LISTENERS; i++) {
        display[i].displayMessage("Window and listeners deleted, press <build> for a new window");
      }
      testWindow.dispose();

      for(int i=0; i<LISTENERS; i++) {
        testWindow.removeWindowListener(display[i]);
      }
      testWindow = null;
    }
    else if(source == show && testWindow != null) {
      //displayText("... initiating -window.setVisible(true)");
      testWindow.setVisible(true); //setVisible(true);
    }
    else if(source == hide && testWindow != null) {
      //displayText("... initiating -window.setVisible(false)");
      testWindow.setVisible(false);
    }
    else {
      // button pressed = either an <add> or a <remove> command
      boolean found = false;
      for(int i=0; i<LISTENERS && !found; i++) {
        if(source == add[i] && testWindow!= null) {
          testWindow.addWindowListener(display[i]);
          display[i].displayMessage("Window listener added. press <remove> to remove it again");
          found = true;
        }
        else if(source == add[i]) {
          display[i].displayMessage("No window to add a listener to. Please build a one first");
          found = true;
        }
        else if(source == remove[i] && testWindow!= null) {
          testWindow.removeWindowListener(display[i]);
          display[i].displayMessage("Window listener removed. press <add> to add it again");
          found = true;
        }
        else if(source == remove[i]) {
          display[i].displayMessage("No window to remove a listener from. Please build a one first");
          found = true;
        }
    }

    }
  }

  /***************************/
  /** build desired Window */
  private Window buildWindow(String text, Color textcolor, Component displayframe ) {
    Window target = null;
    Frame vtframe = vt.getFrame();

    if(vtframe == null) {
      // unable to access the main wonka frame yet
      //displayText("Skipped command for vt.getFrame() not available yet");
    }
    else{
      WindowEventDisplay contents = new WindowEventDisplay(text, new Color(198,198,128), textcolor, 150, 40);
      target = new Window(vtframe);
      target.setName(text);
      //target.add WindowListener(this);
      target.setLayout(new FlowLayout());
      target.add(contents);

      target.pack();
      Point p = displayframe.getLocationOnScreen();
      Rectangle b = displayframe.getBounds();
      target.setLocation(p.x+(b.width-target.getWidth())/2, p.y+(b.height-target.getHeight())/2);
      //displayText("Build new window "+target);
    }
    return target;
  }


  /***************************/
  /** VirtualTestEngine help */
  public String getHelpText(){
    return "The aim: test the throwing of WindowEvents through the AWTEventMulticaster functions:\n\n"+
           "The screen consists out a row of buttons, an anchor area for the test window and "+LISTENERS+" ItemDisplay panels."+
           " Each of this panels is flanked by an <add> and a <remove> button.\n"+
           "Using the <build window>, <show>, <hide> and <dispose> buttons in the top row, you can build a new test window, show and hide it"+
           " and dispose/delete it again. Building a new window will do nothing, but showing, hiding or disposing it will fire a WindowEvent.\n"+
           "Using the <Add> button next to an event panel you can add a WindowListener to that panel in order to get all WindowEvents"+
           " fired by the test window casted to that panel (and subsequently displayed there)."+
           " Using the <remove> button you can remove that listener again\n."+
           "(Adding and removing of TextListeners is done by calls to the static AWTEventMulticaster.Add()and -remove() functions)\n"+
           "\nThe top row buttons:\n-------------------\n"+
           " <Build window> builds a new window and locates it in the center of the middle panel. (As this window is just freshly"+
           " constructed, no listeners are added to it yet. They have to be added anew after every call to <build>"+
           " by pressing that listener's <add> button.)\n"+
           " <show> displays the testwindow on the screen. This triggers a WindowOpened() or WindowDeiconified() event on the test window"+
           " that is casted to all subscribed listeners\n"+
           " <hide> hides the testwindow again. This triggers a WindowIconified() event on the test window,"+
           " again casted to all subscribed listeners\n"+
           " <dispose> disposes the testwindow and subsequently deletes it. Disposing triggers a WindowClosed() event on the test window,"+
           " again casted to all subscribed listeners. As the window is no longer available, this also removes all its listeners."+
           " they have to be added anew when the next test window is built\n"+
           "\n Items to test : \n -------------\n"+
           " => Pressing <add> for a panel and subsequently clicking the buttons to check if a WindowEvent is thrown and displayed"+
           " on the newly selected panel, as well as on all other previously selected panels\n"+
           " => Pressing <remove> for a panel and subsequently clicking a button to check if the WindowEvent thrown is no longer"+
           " displayed on that panel, nor on the panels previously deselected, yet remains displayed on all other panels still selected\n"+
           " => Pressing <add> for the same panel over and over again to see that the panel is not added twice\n"+
           " => Pressing <remove> for the same panel over and over again, or pressing <remove> on a panel to which no listener is added yet"+
           " to check that a panel is not removed twice\n"+
           " \n ps. as the Add and remove routines have a slightly different algorithm for the first and second listener then for all"+
           " subsequent listeners, specially check the behavior when \n"+
           "    - adding the first panel, adding the second panel, adding the third panel\n"+
           "    - removing the third-last panel, removing the second-last panel, removing the last panel\n"+
           "    - giving a remove-command when no panels are selected";
  }

  /********************/
  /** test main */
  static public void main (String[] args) {
	  new WindowMulticastTest();
  }

  // (end of class CopyWriter
}
