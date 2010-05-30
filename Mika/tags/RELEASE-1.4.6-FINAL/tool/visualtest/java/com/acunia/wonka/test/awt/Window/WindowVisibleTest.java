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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class WindowVisibleTest extends VisualTestImpl implements ActionListener{
	
  /****************************************************************/
  /** variables
  */
  private Window leftWindow;
  private Window rightWindow;

  private Button buildLeft;
  private Button killLeft;
  private Button showLeft;
  private Button hideLeft;
  private Button frontLeft;
  private Button backLeft;
  private Button focusLeft;
  private Label leftAnchor;

  private Label rightAnchor;

  private Button buildRight;
  private Button killRight;
  private Button showRight;
  private Button hideRight;
  private Button frontRight;
  private Button backRight;
  private Button focusRight;

  private List display;

  /****************************************************************/
  /** constructor
  */
  public WindowVisibleTest() {
    super();

    leftWindow = null;
//    rightWindow = null;

    // build screen
    setLayout(new BorderLayout());
    setBackground(new Color(128,32,128));
    setForeground(Color.white);
    // list
    display = new List(3,false);
    display.add("Dialog outcome dispayed here");
    add(display, BorderLayout.SOUTH);
    // image anchors and button rows
    Panel anchors = new Panel(new GridLayout(1,4));
      // left buttons
      Panel leftbuttons = new Panel(new GridLayout(8,1) );
      leftbuttons.setBackground(new Color(80,32,32));
        leftbuttons.add(new Label("Left Window:"));
        // build
        buildLeft = new Button("[ ] build window");//("simple ok-dialog");
        buildLeft.setBackground(new Color(96,32,48));
        buildLeft.addActionListener(this);
        leftbuttons.add(buildLeft);
        // dispose & delete
        killLeft = new Button("[x] dispose window");//("simple ok-dialog");
        killLeft.setBackground(new Color(112,32,64));
        killLeft.addActionListener(this);
        leftbuttons.add(killLeft);
        // show
        showLeft = new Button("<= show window =>");//("simple ok-dialog");
        showLeft.setBackground(new Color(128,32,80));
        showLeft.addActionListener(this);
        leftbuttons.add(showLeft);
        // hide
        hideLeft = new Button("=> hide window <=");//("simple ok-dialog");
        hideLeft.setBackground(new Color(144,32,96));
        hideLeft.addActionListener(this);
        leftbuttons.add(hideLeft);
        // to front
        frontLeft = new Button("=> to front =>");//("simple ok-dialog");
        frontLeft.setBackground(new Color(160,32,112));
        frontLeft.addActionListener(this);
        leftbuttons.add(frontLeft);
        // to back
        backLeft = new Button("<= to back <=");//("simple ok-dialog");
        backLeft.setBackground(new Color(176,32,128));
        backLeft.addActionListener(this);
        leftbuttons.add(backLeft);
        // request focus
        focusLeft = new Button("request focus");//("simple ok-dialog");
        focusLeft.setBackground(new Color(192,32,144));
        focusLeft.addActionListener(this);
        leftbuttons.add(focusLeft);
      anchors.add(leftbuttons);

      // left anchor label
      leftAnchor = new Label("Left window area");
      leftAnchor.setBackground(new Color(224,0,144));
      anchors.add(leftAnchor);

      // right anchor label
      rightAnchor = new Label("Right window area");
      rightAnchor.setBackground(new Color(144,0,224));
      anchors.add(rightAnchor);

      // right buttons
      Panel rightbuttons = new Panel(new GridLayout(8,1) );
      rightbuttons.setBackground(new Color(32,32,80));
        rightbuttons.add(new Label("Right Window:"));
        // build
        buildRight = new Button("[ ] build window");//("simple ok-dialog");
        buildRight.setBackground(new Color(48,32,96));
        buildRight.addActionListener(this);
        rightbuttons.add(buildRight);
        // dispose & delete
        killRight = new Button("[X] dispose window");//("simple ok-dialog");
        killRight.setBackground(new Color(64,32,112));
        killRight.addActionListener(this);
        rightbuttons.add(killRight);
        // show
        showRight = new Button("<= show window =>");//("simple ok-dialog");
        showRight.setBackground(new Color(80,32,128));
        showRight.addActionListener(this);
        rightbuttons.add(showRight);
        // hide
        hideRight = new Button("=> hide window <=");//("simple ok-dialog");
        hideRight.setBackground(new Color(96,32,144));
        hideRight.addActionListener(this);
        rightbuttons.add(hideRight);
        // to front
        frontRight = new Button("=> to front =>");//("simple ok-dialog");
        frontRight.setBackground(new Color(112,32,160));
        frontRight.addActionListener(this);
        rightbuttons.add(frontRight);
        // to back
        backRight = new Button("<= to back <=");//("simple ok-dialog");
        backRight.setBackground(new Color(128,32,176));
        backRight.addActionListener(this);
        rightbuttons.add(backRight);
        // request focus
        focusRight = new Button("request focus");//("simple ok-dialog");
        focusRight.setBackground(new Color(144,32,192));
        focusRight.addActionListener(this);
        rightbuttons.add(focusRight);
      anchors.add(rightbuttons);
    add(anchors , BorderLayout.CENTER);
  }

  /**
  ** on stop, close all windows that aren't closed yet
  */
  public void stop(java.awt.Panel p){
    if(leftWindow != null) {
      leftWindow.dispose();
      leftWindow = null;
    }
    if(rightWindow != null) {
      rightWindow.dispose();
      rightWindow = null;
    }
  }
  /****************************************************************/
  /**button pressed : Display desired dialog window */
  public void actionPerformed(ActionEvent evt) {
    Component source = (Component)evt.getSource();
    if(source == buildLeft && leftWindow == null){
      leftWindow = buildWindow(Color.red, "R E D", leftAnchor.getLocationOnScreen());
      displayText("... Left-window constructed");
    }
    else if(source == buildLeft) {
      displayText("!! skipped build of left window !!","!! Left window was already constructed !!");
    }
    else if(source == killLeft && leftWindow != null) {
      leftWindow.dispose();
      leftWindow = null;
      displayText("... Left-window.dispose() called");
    }
    else if(source == killLeft) {
      displayText("!! skipped Left-window.dispose() !!","!! Left window is not built yet !!");
    }
    else if(source == showLeft && leftWindow != null) {
      leftWindow.setVisible(true); //setVisible(true);
      displayText("... Left-window.setVisible(true) called");
    }
    else if(source == showLeft) {
      displayText("!! skipped Left-window.setVisible() !!","!! Left window is not built yet !!");
    }
    else if(source == hideLeft && leftWindow != null) {
      leftWindow.setVisible(false);
      displayText("Left-window.setVisible(false) called");
    }
    else if(source == hideLeft) {
      displayText("!! skipped Left-window.setVisible() !!","!! Left window is not built yet !!");
    }
    else if(source == frontLeft && leftWindow != null) {
      leftWindow.toFront(); //setVisible(true);
      displayText("... Left-window.toFront() called");
    }
    else if(source == frontLeft) {
      displayText("!! skipped Left-window.toFront() !!","!! Left window is not built yet !!");
    }
    else if(source == backLeft && leftWindow != null) {
      leftWindow.toBack();
      displayText("Left-window.toBack() called");
    }
    else if(source == backLeft) {
      displayText("!! skipped Left-window.toBack() !!","!! Left window is not built yet !!");
    }
    else if(source == focusLeft && leftWindow != null) {
      leftWindow.requestFocus();
      displayText("Left-window.requestFocus called");
    }
    else if(source == focusLeft) {
      displayText("!! skipped Left-window.requestFocus() !!","!! Left window is not built yet !!");
    }

    else if(source == buildRight && rightWindow == null){
      rightWindow = buildWindow(Color.blue, "B L U E", rightAnchor.getLocationOnScreen());
      displayText("... Right-window constructed");
    }
    else if(source == buildRight) {
      displayText("!! skipped build of right window !!","!! Right window was already constructed !!");
    }
    else if(source == killRight && rightWindow != null) {
      rightWindow.dispose();
      rightWindow = null;
      displayText("... Right-window.dispose() called");
    }
    else if(source == killRight) {
      displayText("!! skipped Right-window.dispose() !!","!! Right window is not built yet !!");
    }
    else if(source == showRight && rightWindow != null) {
      rightWindow.setVisible(true); //setVisible(true);
      displayText("... Right-window.setVisible(true) called");
    }
    else if(source == showRight) {
      displayText("!! skipped Right-window.show() !!","!! Right window is not built yet !!");
    }
    else if(source == hideRight && rightWindow != null) {
      rightWindow.setVisible(false);
      displayText("Right-window.setVisible(false) called");
    }
    else if(source == hideRight) {
      displayText("!! skipped Right-window.setVisible(false) !!","!! Right window is not built yet !!");
    }
    else if(source == frontRight && rightWindow != null) {
      rightWindow.toFront(); //setVisible(true);
      displayText("... Right-window.toFront() called");
    }
    else if(source == frontRight) {
      displayText("!! skipped Right-window.toFront() !!","!! Right window is not built yet !!");
    }
    else if(source == backRight && rightWindow != null) {
      rightWindow.toBack();
      displayText("Right-window.toBack() called");
    }
    else if(source == backRight) {
      displayText("!! skipped Right-window.toBack() !!","!! Right window is not built yet !!");
    }
    else if(source == focusRight && rightWindow != null) {
      rightWindow.requestFocus();
      displayText("Right-window.requestFocus called");
    }
    else if(source == focusRight) {
      displayText("!! skipped Right-window.requestFocus() !!","!! Right window is not built yet !!");
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

  private void displayText(String line1, String line2){
    // clean list if needed
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    //message to list
    display.add(line2,0);
    display.add(line1,0);
    //message to screen
    System.out.println(line1);
    System.out.println(line2);
  }


  /***************************/
  /** build desired Window */
  private Window buildWindow(Color backcolor, String albumcolor, Point anchor ) {
    Window target = null;
    Frame vtframe = vt.getFrame();

    if(vtframe == null) {
      // unable to access the main wonka frame yet
      displayText("Skipped command for vt.getFrame() not available yet");
    }
    else{
      target = new Window(vtframe);
      target.setLayout(new GridLayout(6,3));
      target.setBackground(backcolor);
      target.setForeground(Color.white);
      target.add(new Label("The Beatles",Label.RIGHT));
      target.add(new Label());
      target.add(new Label());

      target.add(new Label());
      target.add(new Label());
      target.add(new Label());

      target.add(new Label());
      target.add(new Label("the",Label.CENTER));
      target.add(new Label());

      target.add(new Label());
      target.add(new Label(albumcolor,Label.CENTER));
      target.add(new Label());

      target.add(new Label());
      target.add(new Label("album",Label.CENTER));
      target.add(new Label());

      target.pack();
      target.setLocation(anchor.x, anchor.y);
      displayText("Build new window "+target);
    }
    return target;
  }


  /***************************/
  /** VirtualTestEngine help */
  public String getHelpText(){
    return "Test on the visibility of Windows\n"+
           "Using the buttons on the screen, this tests builds, shows and hides one of two possible Windows, brings it to back and to front"+
           "All events done will be logged in the event list on the bottom of the screen\n"+
           "\nthe  buttons:\n"+
           "-> <build> constructs a new window. the window isn't shown yet. (Use <show> to do so )\n"+
           "-> <dispose> disposes and deletes the window\n"+
           "-> <show> shows the window  calling setVisible(true)\n"+
           "-> <hide> calls setVisible(false) to hide the window again\n"+
           "-> <to front> calls Window.toFront() to bring this window on top of all others\n"+
           "   (in this case, over the other window, if present)\n"+
           "-> <to back> calls Window.toBack(), to place this window below all others\n"+
           "   (in this case, below the other window, if present)\n"+
           "-> <request focus> to request focus for the window\n"+
           "\n There are two rows of buttons, one for each of the two windows. The left buttons relate to the left, red window,"+
           " the right buttons to the right, blue one.\n"+
           "The left, red window is shown with its upper left corner aligned to the upper left corner of the left inner anchor panel,"+
           " the right, blue window is shown with its upper left corner aligned to that of the inner right anchor panel";
  }

  /********************/
  /** test main */
  static public void main (String[] args) {
	  new WindowVisibleTest();
  }

  // (end of class CopyWriter
}
