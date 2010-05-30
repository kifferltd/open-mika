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

public class ScrollActionEvent extends VisualTestImpl implements ActionListener{
  private List display;
  final static int NUMBEROFBUTTONS = 15;

  public ScrollActionEvent() {
    // Layout
    setLayout(new BorderLayout() );
    setBackground(Color.yellow);
    // header
    Panel header = new Panel(new GridLayout(1,3));
      Label lleft=new Label("SCROLLBARS_ALWAYS");
      lleft.setBackground(Color.red);
      header.add(lleft);
      Label lmid=new Label("SCROLLBARS_AS_NEEDED");
      lmid.setBackground(Color.green);
      header.add(lmid);
      Label lright=new Label("SCROLLBARS_NEVER");
      lright.setBackground(Color.blue);
      header.add(lright);
    add(header, BorderLayout.NORTH);

    // scrollpane
    Panel mid = new Panel(new GridLayout(1,3));
      //left scrollPane: always show scrollbars
      ScrollPane alwayspane = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
      alwayspane.setBackground(Color.red);
      int saturation = 128;
        Panel always = new Panel(new GridLayout(5,3));
        PaneButton[] alwaysbuttons = new PaneButton[NUMBEROFBUTTONS];
        for(int i=0; i<NUMBEROFBUTTONS; i++) {
          alwaysbuttons[i]= new PaneButton("SCROLLBARS_ALWAYS", "Button no."+i, 70, 40, saturation+=8,64,64);
          alwaysbuttons[i].addActionListener(this);
          always.add(alwaysbuttons[i]);
        }
      alwayspane.add(always);
      mid.add(alwayspane);

      //mid scrollpane: bars as needed
      ScrollPane asneededpane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
      asneededpane.setBackground(Color.green);
      saturation= 128;
        Panel asneeded = new Panel(new GridLayout(5,3));
        PaneButton[] asneededbuttons = new PaneButton[NUMBEROFBUTTONS];
        for(int i=0; i<NUMBEROFBUTTONS; i++) {
          asneededbuttons[i]= new PaneButton("SCROLLBARS_AS_NEEDED", " Button no."+i, 70, 40, 64,saturation+=8,64);
          asneededbuttons[i].addActionListener(this);
          asneeded.add(asneededbuttons[i]);
        }
      asneededpane.add(asneeded);
      mid.add(asneededpane);

      //right scrollpane: never show scrollbars
      ScrollPane neverpane = new ScrollPane(ScrollPane.SCROLLBARS_NEVER);
      neverpane.setBackground(Color.blue);
      saturation=128;
        Panel never = new Panel(new GridLayout(5,3));
        PaneButton[] neverbuttons = new PaneButton[NUMBEROFBUTTONS];
        for(int i=0; i<NUMBEROFBUTTONS; i++) {
          neverbuttons[i]= new PaneButton("SCROLLBARS_NEVER", "Button no."+i, 70, 40, 64,64,saturation+=8);
          neverbuttons[i].addActionListener(this);
          never.add(neverbuttons[i]);
        }
      neverpane.add(never);
      mid.add(neverpane);

    add(mid, BorderLayout.CENTER);
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

    public PaneButton(String title, String text, int width, int height) {
      super(text);
      this.setActionCommand("<"+title+">"+text);
      preferredWidth = width;
      preferredHeight = height;
      this.setSize(width, height);
    }

    public PaneButton(String title, String text, int width, int height, int red, int green, int blue) {
      super(text);
      this.setBackground(new Color(red, green, blue));
      this.setActionCommand("<"+title+">"+text);
      preferredWidth = width;
      preferredHeight = height;
      this.setSize(width, height);
    }

    public Dimension getPreferredSize() {
      return new Dimension(preferredWidth, preferredHeight);
    }
  }
  /****************************************************************/
  /** ItemListener event (there one and only) : Display it in the list
  */
  public void actionPerformed(ActionEvent evt) {
    Button source = (Button)evt.getSource();
    displayMessage("Pressed : "+source.getActionCommand());
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
    return "Shows three ScrollPanes, each showing a table of 5 x 3 buttons. The left ScrollPane is defined with display policy"+
    " ScrollPane.SCROLLBARS_ALWAYS, the middle one with ScrollPane.SCROLLBARS_AS_NEEDED and the right one with ScrollPane.SCROLLBARS_NEVER\n"+
    "\nAll of the buttons of all of the ScrollPanes have an actionlistener added to them and pressing one of the buttons should display"+
    " the button's name and its ScrollPane in the list below.\n\n"+
    "TO TEST: make sure that the pressed button displayed in the events list is the actual button pressed, regardless of the offset of the scrollpanes";
  }

}
