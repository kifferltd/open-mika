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

package com.acunia.wonka.test.awt.Frame;

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class FrameShowTest extends VisualTestImpl implements ActionListener{
	
  /****************************************************************/
  /** variables
  */
  private final static int STEP=20;
  private final static int DEPTH=50;
  private int maxAnchor;

  private Frame redFrame;
  private Frame blueFrame;

  private Button showRed;
  private Button hideRed;
  private Button frontRed;
  private Button backRed;
  private Button leftRed;
  private Button rightRed;

  private Button showBlue;
  private Button hideBlue;
  private Button frontBlue;
  private Button backBlue;
  private Button leftBlue;
  private Button rightBlue;

  private List display;

  /****************************************************************/
  /** constructor
  */
  public FrameShowTest() {
    super();

    redFrame = null;
    blueFrame = null;
    maxAnchor = -1;

    // build screen
    setLayout(new BorderLayout());
    setBackground(new Color(48,128,48));
    setForeground(Color.white);
    //header
    add(new Label("Show, hide, move and bring to front any of the two frames",Label.CENTER),BorderLayout.NORTH);
    // list
    display = new List(3,false);
    display.add("Dialog outcome dispayed here");
    add(display, BorderLayout.SOUTH);
    // buttons
    Panel buttons = new Panel(new GridLayout(1,3));
      Panel leftbuttons = new Panel(new GridLayout(7,1) );
      leftbuttons.setBackground(new Color(128,48,48));
      Label midlabel = new Label();
      midlabel.setBackground(new Color(192,192,128));
      Panel rightbuttons = new Panel(new GridLayout(7,1) );
      rightbuttons.setBackground(new Color(48,48,128));

        leftbuttons.add(new Label("Red frame:"));
        rightbuttons.add(new Label("blue frame:"));

        showRed = new Button("SHOW frame");//("simple ok-dialog");
        showRed.addActionListener(this);
        leftbuttons.add(showRed);
        showBlue = new Button("SHOW frame");//("simple ok-dialog");
        showBlue.addActionListener(this);
        rightbuttons.add(showBlue);

        hideRed = new Button("HIDE frame");//("simple ok-dialog");
        hideRed.addActionListener(this);
        leftbuttons.add(hideRed);
        hideBlue = new Button("HIDE frame");//("simple ok-dialog");
        hideBlue.addActionListener(this);
        rightbuttons.add(hideBlue);

        frontRed = new Button("frame to FRONT");//("simple ok-dialog");
        frontRed.addActionListener(this);
        leftbuttons.add(frontRed);
        frontBlue = new Button("frame to FRONT");//("simple ok-dialog");
        frontBlue.addActionListener(this);
        rightbuttons.add(frontBlue);

        backRed = new Button("frame to BACK");//("simple ok-dialog");
        backRed.addActionListener(this);
        leftbuttons.add(backRed);
        backBlue = new Button("frame to BACK");//("simple ok-dialog");
        backBlue.addActionListener(this);
        rightbuttons.add(backBlue);

        leftRed = new Button("move frame LEFT");//("simple ok-dialog");
        leftRed.addActionListener(this);
        leftbuttons.add(leftRed);
        leftBlue = new Button("move frame LEFT");//("simple ok-dialog");
        leftBlue.addActionListener(this);
        rightbuttons.add(leftBlue);

        rightRed = new Button("move frame RIGHT");//("simple ok-dialog");
        rightRed.addActionListener(this);
        leftbuttons.add(rightRed);
        rightBlue = new Button("move frame RIGHT");//("simple ok-dialog");
        rightBlue.addActionListener(this);
        rightbuttons.add(rightBlue);
      buttons.add(leftbuttons);
      buttons.add(midlabel);
      buttons.add(rightbuttons);
    add(buttons , BorderLayout.CENTER);
  }

  /****************************************************************/
  /**button pressed : Display desired dialog window */
  public void actionPerformed(ActionEvent evt) {
    Component source = (Component)evt.getSource();
    if(source == showRed) {
      if(checkRed()) {
        redFrame.show(); //setVisible(true);
        displayText("Red frame shown");
      }
    }
    else if(source == showBlue) {
      if(checkblue()) {
        blueFrame.show(); //.setVisible(true);
        displayText("blue frame shown");
      }
    }
    else if(source == hideRed) {
      if(checkRed()) {
        redFrame.setVisible(false);
        displayText("Red frame hidden");
      }
    }
    else if(source == hideBlue) {
      if(checkblue()) {
        blueFrame.setVisible(false);
        displayText("blue frame hidden");
      }
    }
    else if(source == frontRed) {
      if(checkRed()) {
        redFrame.toFront();
        displayText("Red frame to front");
      }
    }
    else if(source == frontBlue) {
      if(checkblue()) {
        blueFrame.toFront();
        displayText("blue frame to front");
      }
    }
    else if(source == backRed) {
      if(checkRed()) {
        redFrame.toBack();
        displayText("Red frame to back");
      }
    }
    else if(source == backBlue) {
      if(checkblue()) {
        blueFrame.toBack();
        displayText("blue frame to back");
      }
    }
    else if(source == leftRed) {
      int anchor = redFrame.getX();
      if(checkRed() && anchor>STEP) {
        anchor-=STEP;
        redFrame.setLocation(anchor,DEPTH);
        displayText("Red frame moved left to ("+anchor+", "+DEPTH+")");
      }
    }
    else if(source == leftBlue) {
      int anchor = blueFrame.getX();
      if(checkblue() && anchor>STEP) {
        anchor-=STEP;
        blueFrame.setLocation(anchor,DEPTH);
        displayText("blue frame moved left to ("+anchor+", "+DEPTH+")");
      }
    }
    else if(source == rightRed ) {
      int anchor = redFrame.getX();
      if(checkRed() && anchor<(maxAnchor-STEP)) {
        anchor+=STEP;
        redFrame.setLocation(anchor,DEPTH);
        displayText("Red frame moved right to ("+anchor+", "+DEPTH+")");
      }
    }
    else if(source == rightBlue) {
      int anchor = blueFrame.getX();
      if(checkblue() && anchor<(maxAnchor-STEP)) {
        anchor+=STEP;
        blueFrame.setLocation(anchor,DEPTH);
        displayText("blue frame moved right to ("+anchor+", "+DEPTH+")");
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


  /*******************************/
  /** build red frame if needed */
  private boolean checkRed() {
    displayText("Checking red frame: "+redFrame);

    Frame vtframe = vt.getFrame();
    if(vtframe == null) {
      // unable to access the main wonka frame yet
      displayText("Skipped command for vt.getFrame() not available yet");
      return false;
    }
    else if(redFrame == null) {
      // the anchor frame isn't built yet => build it now
      redFrame = new Frame("Red Frame"); //(vtframe, "Anchor frame");
      redFrame.setLayout(new GridLayout(5,3));
      redFrame.setBackground(Color.red);
      redFrame.setForeground(Color.white);
      redFrame.add(new Label("The Beatles",Label.RIGHT));
      redFrame.add(new Label());
      redFrame.add(new Label());

      redFrame.add(new Label());
      redFrame.add(new Label("the",Label.CENTER));
      redFrame.add(new Label());

      redFrame.add(new Label());
      redFrame.add(new Label("R E D",Label.CENTER));
      redFrame.add(new Label());

      redFrame.add(new Label());
      redFrame.add(new Label("album",Label.CENTER));
      redFrame.add(new Label());

      redFrame.pack();
      int max = this.getSize().width - redFrame.getSize().width;
      if(maxAnchor<0 || max < maxAnchor) {
        maxAnchor = max;
      }
      redFrame.setLocation(max/2,DEPTH);

      displayText("Build Red frame "+redFrame);
      return true;
    }
    else {
      // the anchor frame is up and running => just return 'the clock striketh ten and all is well'
      return true;
    }
  }

  /********************************/
  /** build blue frame if needed */
  private boolean checkblue() {
    displayText("Checking blue frame: "+blueFrame);

    Frame vtframe = vt.getFrame();
    if(vtframe == null) {
      // unable to access the main wonka frame yet
      displayText("Skipped command for vt.getFrame() not available yet");
      return false;
    }
    else if(blueFrame == null) {
      // the anchor frame isn't built yet => build it now
      blueFrame = new Frame("blue frame"); //(vtframe, "Anchor frame");
      blueFrame.setLayout(new GridLayout(5,3));
      blueFrame.setBackground(Color.blue);
      blueFrame.setForeground(Color.white);
      blueFrame.add(new Label("The Beatles",Label.RIGHT));
      blueFrame.add(new Label());
      blueFrame.add(new Label());

      blueFrame.add(new Label());
      blueFrame.add(new Label("the",Label.CENTER));
      blueFrame.add(new Label());

      blueFrame.add(new Label());
      blueFrame.add(new Label("B L U E",Label.CENTER));
      blueFrame.add(new Label());

      blueFrame.add(new Label());
      blueFrame.add(new Label("album",Label.CENTER));
      blueFrame.add(new Label());

      blueFrame.pack();
      int max = this.getSize().width - blueFrame.getSize().width;
      if(maxAnchor<0 || max < maxAnchor) {
        maxAnchor = max;
      }
      blueFrame.setLocation(max/2,DEPTH);

      displayText("Build Blue frame "+blueFrame);
      return true;
    }
    else {
      // the anchor frame is up and running => just return 'the clock striketh ten and all is well'
      return true;
    }
  }


  /***************************/
  /** VirtualTestEngine help */
  public String getHelpText(){
    return "Test on the construction and behavior of the java.awt.Frame class:\n"+
           "The two rows of buttons each\n"+
           "-> show a frame\n"+
           "-> hide that frame again\n"+
           "-> bring that frame to front (on overlapping frames)\n"+
           "-> bring that frame to back (as the test itself is a frame already, this will hide the window)\n"+
           "-> move that frame to the left \n"+
           "-> move that frame to the right \n"+
           "In particularly check the correct behavior of the toFront()/toBack() functions (triggered by the buttons of that name)\n"+
           "and the moving of the frames either using the buttons or by dragging";
  }

  /***************************/
  /** on stop, close all windows that aren't closed yet  */
  public void stop(java.awt.Panel p){
    if(redFrame != null) {
      redFrame.dispose();
      redFrame = null;
    }
    if(blueFrame != null) {
      blueFrame.dispose();
      blueFrame = null;
    }
  }

  public void hideTest(){
    if(redFrame != null) {
      redFrame.setVisible(false);
    }
    if(blueFrame != null) {
      blueFrame.setVisible(false);
    }
  }

  public void showTest(){
    if(redFrame != null) {
      redFrame.setVisible(true);
    }
    if(blueFrame != null) {
      blueFrame.setVisible(true);
    }
  }


  /********************/
  /** test main */
  static public void main (String[] args) {
	  new FrameShowTest();
  }
}
