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

package com.acunia.wonka.test.awt.Window;

//import
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class PackSizeTest extends VisualTestImpl implements ActionListener{
	
  /****************************************************************/
  /** variables
  */
  private Window firstWindow;
  private Button showFirst;
  private Button hideFirst;

  private Window secondWindow;
  private Button showSecond;
  private Button hideSecond;

  private Window thirdWindow;
  private Button showThird;
  private Button hideThird;

  private Window fourthWindow;
  private Button showFourth;
  private Button hideFourth;

  private List display;

  /****************************************************************/
  /** constructor
  */
  public PackSizeTest() {
    super();

    firstWindow = null;
    secondWindow = null;
    thirdWindow = null;
    fourthWindow = null;

    // build screen
    setLayout(new BorderLayout());
    // list
    display = new List(3,false);
    display.add("Dialog outcome dispayed here");
    add(display, BorderLayout.SOUTH);
    // buttons
    Panel buttons = new Panel(new GridLayout(6,2) );
      buttons.add(new Label("GridLayout Window:"));
      buttons.add(new Label("FlowLayout Window:"));

      showFirst = new Button("build Window");
      showFirst.addActionListener(this);
      buttons.add(showFirst);
      showSecond = new Button("build Window");
      showSecond.addActionListener(this);
      buttons.add(showSecond);

      hideFirst = new Button("dispose Window");
      hideFirst.addActionListener(this);
      buttons.add(hideFirst);
      hideSecond = new Button("dispose Window");
      hideSecond.addActionListener(this);
      buttons.add(hideSecond);

      buttons.add(new Label("BorderLayout Window:"));
      buttons.add(new Label("default layout Window:"));

      showThird = new Button("build Window");
      showThird.addActionListener(this);
      buttons.add(showThird);
      showFourth = new Button("build Window");
      showFourth.addActionListener(this);
      buttons.add(showFourth);

      hideThird = new Button("dispose Window");
      hideThird.addActionListener(this);
      buttons.add(hideThird);
      hideFourth = new Button("dispose Window");
      hideFourth.addActionListener(this);
      buttons.add(hideFourth);
    add(buttons , BorderLayout.CENTER);
  }


  /**
  ** on stop, close all windows that aren't closed yet
  */
  public void stop(java.awt.Panel p){
    if(firstWindow != null) {
      firstWindow.dispose();
      firstWindow = null;
    }
    if(secondWindow != null) {
      secondWindow.dispose();
      secondWindow = null;
    }
    if(thirdWindow != null) {
      thirdWindow.dispose();
      thirdWindow = null;
    }
    if(fourthWindow != null) {
      fourthWindow.dispose();
      fourthWindow = null;
    }
  }

  /****************************************************************/
  /**button pressed : Display desired dialog window */
  public void actionPerformed(ActionEvent evt) {
    Component source = (Component)evt.getSource();
    if(source == showFirst) {
      if(checkFirst()) {
        firstWindow.show(); //setVisible(true);
        displayText("first (GridLayout) Window shown");
      }
    }
    else if(source == hideFirst) {
      if(firstWindow!= null) {
        firstWindow.dispose();
        firstWindow = null;
        displayText("first (GridLayout) Window disposed");
      }
    }
    else if(source == showSecond) {
      if(checkSecond()) {
        secondWindow.show(); //setVisible(true);
        displayText("second (FlowLayout) Window shown");
      }
    }
    else if(source == hideSecond) {
      if(secondWindow!= null) {
        secondWindow.dispose();
        secondWindow = null;
        displayText("second (FlowLayout) Window disposed");
      }
    }
    else if(source == showThird) {
      if(checkThird()) {
        thirdWindow.show(); //setVisible(true);
        displayText("third (BorderLayout) Window shown");
      }
    }
    else if(source == hideThird) {
      if(thirdWindow!= null) {
        thirdWindow.dispose();
        thirdWindow = null;
        displayText("third (BorderLayout) Window disposed");
      }
    }
    else if(source == showFourth) {
      if(checkFourth()) {
        fourthWindow.show(); //setVisible(true);
        displayText("fourth (default layout) Window shown");
      }
    }
    else if(source == hideFourth) {
      if(fourthWindow!= null) {
        fourthWindow.dispose();
        fourthWindow = null;
        displayText("fourth (default layout) Window disposed");
      }
    }
  }

  /**************************/
  /** Display event on list */
  private void displayText(String text){
    // clean list if needed
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    //message to list
    display.add(text,0);
    //message to screen
    System.out.println(text);
  }


  /********************************/
  /** build first Window if needed */
  private boolean checkFirst() {
    displayText("Checking first Window: "+firstWindow);

    Frame vtframe = vt.getFrame();
    if(vtframe == null) {
      // unable to access the main wonka frame yet
      displayText("Skipped command for vt.getFrame() not available yet");
      return false;
    }
    else if(firstWindow == null) {
      // the anchor Window isn't built yet => build it now
      firstWindow = new Window(vtframe);
      firstWindow.setLayout(new GridLayout(1,1));
      //firstWindow.setLayout(new FlowLayout());
      //firstWindow.setLayout(new BorderLayout());
      firstWindow.setBackground(Color.red);
      firstWindow.setForeground(Color.white);
      firstWindow.add(new Label("The first (GridLayout) Window",Label.CENTER));

      firstWindow.pack();
      displayText("Build first Window "+firstWindow);
      return true;
    }
    else {
      // the anchor Window is up and running => just return 'the clock striketh ten and all is well'
      return true;
    }
  }

  /*********************************/
  /** build second Window if needed */
  private boolean checkSecond() {
    displayText("Checking second Window: "+secondWindow);

    Frame vtframe = vt.getFrame();
    if(vtframe == null) {
      // unable to access the main wonka frame yet
      displayText("Skipped command for vt.getFrame() not available yet");
      return false;
    }
    else if(secondWindow == null) {
      // the anchor Window isn't built yet => build it now
      secondWindow = new Window(vtframe);
      //.setLayout(new GridLayout(1,1));
      secondWindow.setLayout(new FlowLayout());
      //.setLayout(new BorderLayout());
      secondWindow.setBackground(Color.blue);
      secondWindow.setForeground(Color.white);
      secondWindow.add(new Label("The second (FlowLayout) Window",Label.CENTER));

      secondWindow.pack();
      displayText("Build second Window "+secondWindow);
      return true;
    }
    else {
      // the anchor Window is up and running => just return 'the clock striketh ten and all is well'
      return true;
    }
  }

  /********************************/
  /** build third Window if needed */
  private boolean checkThird() {
    displayText("Checking third Window: "+thirdWindow);

    Frame vtframe = vt.getFrame();
    if(vtframe == null) {
      // unable to access the main wonka frame yet
      displayText("Skipped command for vt.getFrame() not available yet");
      return false;
    }
    else if(thirdWindow == null) {
      // the anchor Window isn't built yet => build it now
      thirdWindow = new Window(vtframe);
      //thirdWindow.setLayout(new GridLayout(1,1));
      //thirdWindow.setLayout(new FlowLayout());
      thirdWindow.setLayout(new BorderLayout());
      thirdWindow.setBackground(Color.green);
      thirdWindow.setForeground(Color.white);
      thirdWindow.add(new Label("The third (BorderLayout) Window",Label.CENTER),BorderLayout.CENTER);

      thirdWindow.pack();
      displayText("Build third Window "+thirdWindow);
      return true;
    }
    else {
      // the anchor Window is up and running => just return 'the clock striketh ten and all is well'
      return true;
    }
  }

  /*********************************/
  /** build fourth Window if needed */
  private boolean checkFourth() {
    displayText("Checking fourth Window: "+fourthWindow);

    Frame vtframe = vt.getFrame();
    if(vtframe == null) {
      // unable to access the main wonka frame yet
      displayText("Skipped command for vt.getFrame() not available yet");
      return false;
    }
    else if(fourthWindow == null) {
      // the anchor Window isn't built yet => build it now
      fourthWindow = new Window(vtframe);
      //fourthWindow.setLayout(new GridLayout(1,1));
      //fourthWindow.setLayout(new FlowLayout());
      //fourthWindow.setLayout(new BorderLayout());
      fourthWindow.setBackground(Color.black);
      fourthWindow.setForeground(Color.white);
      fourthWindow.add(new Label("The fourth (default layout) Window",Label.CENTER));

      fourthWindow.pack();
      displayText("Build fourth Window "+fourthWindow);
      return true;
    }
    else {
      // the anchor Window is up and running => just return 'the clock striketh ten and all is well'
      return true;
    }
  }

  /***************************/
  /** VirtualTestEngine help */
  public String getHelpText(){
    return "Test on the construction and behavior of the java.awt.Window class:\n"+
           "Each of the four groups of buttons will construct a Window, add a label to it and calculate its size (using Window.pack() )\n"+
           "the four Windows are aligned using four different layouts"+
           "-> a GridLayout \n"+
           "-> a FlowLayout\n"+
           "-> a BorderLayout (using the long form add(component, BorderLayout.CENTER) )\n"+
           "-> the default layout (using a simple add(component) )\n"+
           "Check the correct size calculation and the correct display in all of the cases"+
           "\n\nCURRENT ISSUE:\n constructing the fourth Window causes the application to hang."+
           " This is because the label is added to the default BorderLayout using the simple form Container.add(Component)"+
           " instead of the propper Container.add(Component, BorderLayout.alignment)\n"+
           "(see also PackBorderLayoutTest on this subject)";
  }

  /********************/
  /** test main */
  static public void main (String[] args) {
	  new PackSizeTest();
  }

  // (end of class CopyWriter
}
