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

package com.acunia.wonka.test.awt.ScrollPane;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class ScrollbarDisplay extends VisualTestImpl implements ActionListener{
  private PaneButton higher;
  private PaneButton lower;
  private int upperHeight;
  private PaneButton largerLeft;
  private PaneButton largerRight;
  private PaneButton leanerLeft;
  private PaneButton leanerRight;
  private int sideWidth;
  private List display;
  final static int NUMBEROFBUTTONS = 12;

  public ScrollbarDisplay() {
    // Layout
    setLayout(new BorderLayout() );
    setBackground(Color.yellow);
    // header
    upperHeight = 30;
    Panel header = new Panel(new GridLayout(1,4));
      header.add(new Label());
      higher = new PaneButton("Scrollpane HIGHER", 50, upperHeight );
      higher.addActionListener(this);
      header.add(higher);

      lower = new PaneButton("Scrollpane LOWER", 50, upperHeight );
      lower.addActionListener(this);
      header.add(lower);
      header.add(new Label());
    add(header, BorderLayout.NORTH);

    // left and right
    sideWidth = 30;
    Panel left = new Panel(new GridLayout(2,1));
    Panel right = new Panel(new GridLayout(2,1));
      largerLeft = new PaneButton("<==",sideWidth,50);
      largerLeft.addActionListener(this);
      left.add(largerLeft);
      largerRight = new PaneButton("==>",sideWidth,50);
      largerRight.addActionListener(this);
      right.add(largerRight);
      leanerLeft = new PaneButton("==>",sideWidth,50);
      leanerLeft.addActionListener(this);
      left.add(leanerLeft);
      leanerRight = new PaneButton("<==",sideWidth,50);
      leanerRight.addActionListener(this);
      right.add(leanerRight);
    add(left, BorderLayout.WEST);
    add(right, BorderLayout.EAST);

    // scrollpane
    ScrollPane asneededpane = new ScrollPane(); //(ScrollPane.SCROLLBARS_AS_NEEDED);
    asneededpane.setBackground(Color.blue);
    int saturation= 128;
      Panel asneeded = new Panel(new GridLayout(3,4));
      PaneButton[] asneededbuttons = new PaneButton[NUMBEROFBUTTONS];
      for(int i=0; i<NUMBEROFBUTTONS; i++) {
        asneededbuttons[i]= new PaneButton(" Button no."+i, 60, 30, 64, 64, saturation+=8);
        asneededbuttons[i].addActionListener(this);
        asneeded.add(asneededbuttons[i]);
      }
    asneededpane.add(asneeded);
    add(asneededpane, BorderLayout.CENTER);

    // list
    display = new List(3,false);
    display.add("your button events displayed here");
    add(display, BorderLayout.SOUTH);
  }

  /****************************************************************/
  /** Button with preferred size
  */
  class PaneButton extends Button {
    private int preferredWidth;
    private int preferredHeight;

    public PaneButton(String text, int width, int height) {
      super(text);
      this.setActionCommand(text);
      preferredWidth = width;
      preferredHeight = height;
      this.setSize(width, height);
    }

    public PaneButton(String text, int width, int height, int red, int green, int blue) {
      super(text);
      this.setBackground(new Color(red, green, blue));
      this.setActionCommand(text);
      preferredWidth = width;
      preferredHeight = height;
      this.setSize(width, height);
    }

    public Dimension getPreferredSize() {
      return new Dimension(preferredWidth, preferredHeight);
    }

    public Dimension getMinimumSize() {
      return new Dimension(preferredWidth, preferredHeight);
    }

    public void setWidth(int width) {
      preferredWidth = width;
      this.setSize(width, preferredHeight);
    }

    public void setHeight(int height) {
      preferredHeight = height;
      this.setSize(preferredWidth, height);
    }
  }
  /****************************************************************/
  /** ItemListener event (there one and only) : Display it in the list
  */
  public void actionPerformed(ActionEvent evt) {
    Button source = (Button)evt.getSource();
    if(source==lower) {
      if( upperHeight<100) {
        upperHeight+=10;
        higher.setEnabled(true);
        higher.setHeight(upperHeight);
        lower.setHeight(upperHeight);
        validate();
        repaint();
      }
      else{
        lower.setEnabled(false);
      }
    }
    else if (source == higher) {
      if(upperHeight>20) {
        upperHeight-=10;
        higher.setHeight(upperHeight);
        lower.setHeight(upperHeight);
        lower.setEnabled(true);
        validate();
        repaint();
      }
      else{
        higher.setEnabled(false);
      }
    }
    else if(source == leanerLeft || source == leanerRight) {
      if(sideWidth<100) {
        sideWidth+=10;
        largerLeft.setWidth(sideWidth);
        leanerLeft.setWidth(sideWidth);
        largerRight.setWidth(sideWidth);
        leanerRight.setWidth(sideWidth);
        largerLeft.setEnabled(true);
        largerRight.setEnabled(true);
        validate();
        repaint();
      }
      else {
        leanerLeft.setEnabled(false);
        leanerRight.setEnabled(false);
      }
    }
    else if (source == largerLeft || source == largerRight) {
      if(sideWidth>30) {
        sideWidth-=10;
        largerLeft.setWidth(sideWidth);
        leanerLeft.setWidth(sideWidth);
        largerRight.setWidth(sideWidth);
        leanerRight.setWidth(sideWidth);
        leanerLeft.setEnabled(true);
        leanerRight.setEnabled(true);
        validate();
        repaint();
      }
      else {
        largerLeft.setEnabled(false);
        largerRight.setEnabled(false);
      }
    }
    else {
      displayMessage("Pressed : "+source.getActionCommand());
    }
  }
  /****************************************************************/
  /** CollectsEvent interface display messages : do nothing, we only display our own messages
  */
  public void displayMessage(String message) {
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    display.add(message,0);
  }

  public String getHelpText() {
    return "Tests the layout of the ScrollPane screen and the display of the Scrollbars in a default ScrollPane with default lauout policy"+
    " ScrollPane.SCROLLBARS_AS_NEEDED\n"+
    "The scrollbar is located in the center of the screen. Using the higher/lower buttons and the <== ==> buttons, you can resize the"+
    " surrounding buttons, leaving more or less space for the display. Like this, it is possible to obtain all of the four cases:"+
    " no scrollbars, only horizontal scrollbar, only vertical scrollbar and both bars.\n"+
    " in all of the cases, make sure that when selecting a button from the ScrollPane, the event for that button is displayed"+
    " in the list below, regardless of the scrollbars and scrollbar offset shown";
  }

}
