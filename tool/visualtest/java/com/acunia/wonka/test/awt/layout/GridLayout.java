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


package com.acunia.wonka.test.awt.layout;

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;


public class GridLayout extends VisualTestImpl implements ActionListener {

  /** Variables*/
  private Button addLeft;
  private Button removeAllLeft;
  private Panel left;
  private int leftCount;

  private Button addRight;
  private Button removeAllRight;
  private Panel right;
  private int rightCount;

  //List display;
  private int step;

  private final static int ELEMENTS = 6;

  /** constructor */
  public GridLayout() {
    step = 100/ELEMENTS;
    int clear = 155;
    int darker = 155-step;
    setBackground(new Color(128,clear,clear));
    setLayout(new java.awt.BorderLayout());

    Panel addcommands = new Panel(new java.awt.GridLayout(1,2));
      addLeft = new Button("add one button");
      addLeft.setBackground(new Color(128,darker,clear));
      addLeft.addActionListener(this);
      addcommands.add(addLeft);

      addRight = new Button("add one button");
      addRight.setBackground(new Color(128,clear,darker));
      addRight.addActionListener(this);
      addcommands.add(addRight);
      darker+=step;
      clear+=step;
    add(addcommands, java.awt.BorderLayout.NORTH);

    Panel screen = new Panel(new java.awt.GridLayout(1,2));
      left = new Panel(new java.awt.GridLayout(ELEMENTS,1));
      left.setBackground(new Color(128,128,clear));
        buildLeftButton();
      screen.add(left);

      right = new Panel(new java.awt.GridLayout(ELEMENTS,1));
      right.setBackground(new Color(128,clear,128));
        buildRightButton();
      screen.add(right);
      darker+=step;
      clear+=step;
    add(screen, java.awt.BorderLayout.CENTER);

    Panel removecommands = new Panel(new java.awt.GridLayout(1,2));
      removeAllLeft = new Button("remove all buttons");
      removeAllLeft.setBackground(new Color(128,darker,clear));
      removeAllLeft.addActionListener(this);
      removecommands.add(removeAllLeft);

      removeAllRight = new Button("remove all buttons");
      removeAllRight.setBackground(new Color(128,clear,darker));
      removeAllRight.addActionListener(this);
      removecommands.add(removeAllRight);
    add(removecommands, java.awt.BorderLayout.SOUTH);
  }

  public void actionPerformed(ActionEvent evt) {
    Button source = (Button)(evt.getSource());
    if(source == addLeft) {
      buildLeftButton();
      left.validate();
    }
    else if(source == addRight) {
      buildRightButton();
      right.validate();
    }
    else if(source == removeAllLeft) {
      left.removeAll();
      left.validate();
    }
    else if(source == removeAllRight){
      right.removeAll();
      right.validate();
    }
    else if(source.getActionCommand() == "LEFT"){
      left.remove(source);
      source = null;
      left.validate();
    }
    else if(source.getActionCommand() == "RIGHT"){
      right.remove(source);
      right.validate();
    }

  }

  private void buildLeftButton() {
    leftCount= left.getComponentCount();
    Button b = new Button("LEFT_"+leftCount+" (Click to remove)");
    b.setBackground(new Color(128, 128, 128+step*(leftCount%ELEMENTS)));
    b.setActionCommand("LEFT");
    b.setName("LEFT_"+leftCount);
    b.addActionListener(this);
    left.add(b);
  }

  private void buildRightButton() {
    rightCount= right.getComponentCount();
    Button b = new Button("Right_"+rightCount+" (Click to remove)");
    b.setBackground(new Color(128, 128+step*(rightCount%ELEMENTS), 128));
    b.setActionCommand("RIGHT");
    b.setName("Right_"+rightCount);
    b.addActionListener(this);
    right.add(b);
  }

  public String getHelpText(){
    return "A test to verify Rudolph's GridLayour implementation\n"+
           "The test shows a blue and a green Panel. above each Panel a button <add> allows you to add a button to that panel,"+
           " below each panel a button <remove all> allows you to remove all buttons from that panel. On startup, every panel has one button"+
           "\n\nitems to test:\n"+
           "=> adding a button to a panel using the Container.add(component) function called by the <add> button\n"+
           "=> removing a button from a panel using Container.remove(component) called by clicking that button\n"+
           "=> removing all buttons from a panel using Container.removeAll() called by clicking <remove all> \n"+
           "=> Each panel can contain "+ELEMENTS+" elements vertically. Adding a "+(ELEMENTS+1)+"th element should force the display"+
           " to appear in two rows while removing this element again should display the remaining elements in one row again";
  }
}
