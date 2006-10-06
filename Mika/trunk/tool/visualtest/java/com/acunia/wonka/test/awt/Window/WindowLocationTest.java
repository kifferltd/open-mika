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
import java.awt.Dimension;
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

public class WindowLocationTest extends VisualTestImpl implements ActionListener{
	
  /****************************************************************/
  /** variables
  */
  private Window leftWindow;
//  Window rightWindow;

  private Button buildLeft;
  private Button killLeft;
  private Button showLeft;
  private Button hideLeft;
  private Button setLeftLocation;
  private Button setRightLocation;

  private Label leftAnchor;

  private Label rightAnchor;

  private List display;

  /****************************************************************/
  /** constructor
  */
  public WindowLocationTest() {
    super();

    leftWindow = null;
//    rightWindow = null;

    // build screen
    setLayout(new BorderLayout());
    setBackground(new Color(220,255,185));
    // list
    display = new List(3,false);
    //display.setBackground(new Color(220,255,185));
    display.add("Dialog outcome dispayed here");
    add(display, BorderLayout.SOUTH);
    // add sides
    add(new PlaceholderComponent(40,50), BorderLayout.EAST);
    add(new PlaceholderComponent(40,50), BorderLayout.WEST);
    // image anchors and button rows
    Panel anchors = new Panel(new GridLayout(1,3));
    anchors.setBackground(new Color(245,205,64));
      // left anchor label
      leftAnchor = new Label("Left window area",Label.CENTER);
      anchors.add(leftAnchor);

      // buttons
      Panel leftbuttons = new Panel(new GridLayout(6,1) );
        // build
        buildLeft = new Button("build window");
        buildLeft.addActionListener(this);
        buildLeft.setBackground(new Color(255,205,64));
        leftbuttons.add(buildLeft);
        // dispose & delete
        killLeft = new Button("dispose window");
        killLeft.addActionListener(this);
        killLeft.setBackground(new Color(250,215,84));
        leftbuttons.add(killLeft);
        // show
        showLeft = new Button("show window");
        showLeft.addActionListener(this);
        showLeft.setBackground(new Color(245,225,105));
        leftbuttons.add(showLeft);
        // hide
        hideLeft = new Button("hide window");
        hideLeft.addActionListener(this);
        hideLeft.setBackground(new Color(240,235,125));
        leftbuttons.add(hideLeft);
        // move to left
        setLeftLocation = new Button("<-- move to left");
        setLeftLocation.addActionListener(this);
        setLeftLocation.setBackground(new Color(235,245,145));
        leftbuttons.add(setLeftLocation);
        // move to right
        setRightLocation = new Button("move to right -->");
        setRightLocation.addActionListener(this);
        setRightLocation.setBackground(new Color(230,255,165));
        leftbuttons.add(setRightLocation);
      anchors.add(leftbuttons);

      // right anchor label
      rightAnchor = new Label("right window area",Label.CENTER);
      anchors.add(rightAnchor);
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
    /*if(rightWindow != null) {
      rightWindow.dispose();
      rightWindow = null;
    } */
  }

  /****************************************************************/
  /**button pressed : Display desired dialog window */
  public void actionPerformed(ActionEvent evt) {
    Component source = (Component)evt.getSource();
    if(source == buildLeft && leftWindow == null){
      leftWindow = buildWindow(Color.red, "R E D");
      displayText("... new window constructed");
    }
    else if(source == buildLeft) {
      displayText("!! skipped build of new window !!","!! window was already constructed !!");
    }
    else if(source == killLeft && leftWindow != null) {
      leftWindow.dispose();
      leftWindow = null;
      displayText("... window.dispose() called");
    }
    else if(source == killLeft) {
      displayText("!! skipped window.dispose() !!","!! window is not built yet !!");
    }
    else if(source == showLeft && leftWindow != null) {
      leftWindow.show(); //setVisible(true);
      displayText("... window.show() called");
    }
    else if(source == showLeft) {
      displayText("!! skipped window.show() !!","!! window is not built yet !!");
    }
    else if(source == hideLeft && leftWindow != null) {
      leftWindow.setVisible(false);
      displayText("window.setVisible(false) called");
    }
    else if(source == hideLeft) {
      displayText("!! skipped window.setVisible(false) !!","!! window is not built yet !!");
    }
    else if(source == setLeftLocation && leftWindow != null) {
      Point p=leftAnchor.getLocationOnScreen();
      leftWindow.setLocation(p.x, p.y);
      displayText("... window.setLocation() to left panel top=("+p.x+", "+p.y+")");

    }
    else if(source == setLeftLocation) {
      displayText("!! skipped window.setLocation() !!","!! window is not built yet !!");
    }
    else if(source == setRightLocation && leftWindow != null) {
      Point p=rightAnchor.getLocationOnScreen();
      leftWindow.setLocation(p.x, p.y);
      displayText("... window.setLocation() to right panel top=("+p.x+", "+p.y+")");

    }
    else if(source == setRightLocation) {
      displayText("!! skipped window.setLocation() !!","!! window is not built yet !!");
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
  /** build left frane if needed */
  private Window buildWindow(Color backcolor, String albumcolor) {
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
      displayText("Build new window "+target);
    }
    return target;
  }


  /***************************/
  /** VirtualTestEngine help */
  public String getHelpText(){
    return "Test on the visibility and placement of windows\n"+
           "Using the buttons on the screen, this tests builds, shows and moves a Window. Therefore testing the Window classes' redraw"+
           " and update functions. All events done will be logged in the event list on the bottom of the screen\n"+
           "\nthe  buttons:\n"+
           "-> <build> constructs a new window. the window isn't shown yet. (Use <show> to do so )\n"+
           "-> <dispose> disposes and deletes the window\n"+
           "-> <show> shows the window \n"+
           "-> <hide> hides the window again (using setVisible(false) )\n"+
           "-> <move to left> moves the window to the top-left of the inner left yellow anchor panel\n"+
           "-> <move to right> moves the window to the top-left of the inner right yellow anchor panel\n"+
           "   The test uses Component.getLocationOnScreen() to retrieve"+
           "\n\n current issues: \n"+
           "-> getLocationOnScreen() still retrieves the relative location to the next higher component instead of the absolute location\n"+
           "-> setVisible(false) redraws the main frame; but not the subPanels: pressing <hide> makes the test delete the part of the"+
           "window that overlaps the upper memory bar of the test, but leaces the part that is inside the actual test panel. ";
  }

  /***************************/
  /** An inner placeholder class that does nothing but occupy a certain minimum space */
  class PlaceholderComponent extends Component {
    /** the width & height */
    int width;
    int height;

    public PlaceholderComponent(int w, int h) {
      super();
      width = w;
      height = h;
    }

    public PlaceholderComponent(int w, int h, Color c) {
      super();
      width = w;
      height = h;
      this.setBackground(c);
    }

    public Dimension getMinimumSize() {
      return new Dimension(width, height);
    }

    public Dimension getPreferredSize() {
      return new Dimension(width, height);
    }

  }

  /********************/
  /** test main */
  static public void main (String[] args) {
	  new WindowLocationTest();
  }

  // (end of class CopyWriter
}
