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


// Author: J. Vandeneede
// Created: 2001/11/21

package com.acunia.wonka.test.awt.GridBagLayout;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class LunaticLayout extends VisualTestImpl implements ActionListener  {
  private List display;

  public LunaticLayout() {
    super();
    setLayout(new BorderLayout());
    setBackground(Color.yellow);
    //header
    add(new Label("GridBagLayout demo...",Label.CENTER), BorderLayout.NORTH);
    // center test panel with gridbag layout
    Panel testpanel = new Panel(new GridBagLayout());                                          // colweights  0   1   2   3   4   5   6     rowweights   0   1   2   3   4
    testpanel.setBackground(Color.black );
      makeButton(testpanel, "1", 0, 0, 3, 2, 1.0, 0.0);   //  .0  .0  .0                                 .0  .0
      makeButton(testpanel, "2", 3, 0, 1, 3, 0.5, 0.0);   //              .5                             .0  .0  .0
      makeButton(testpanel, "3", 4, 0, 3, 1, 1.0, 0.5);   //                  .0  .0  .0                 .5
      makeButton(testpanel, "4", 5, 1, 1, 1, 1.0, 1.0);   //                      1                          1
      makeButton(testpanel, "5", 4, 1, 1, 4, 1.0, 1.0);   //                  1                              .0  .0  .0  .0
      makeButton(testpanel, "6", 0, 2, 1, 2, 0.5, 1.0);   //  .5                                                 1
      makeButton(testpanel, "7", 2, 2, 1, 1, 2.0, 1.0);   //          2                                          1
      makeButton(testpanel, "8", 6, 1, 1, 3, 1.0, 1.0);   //                          1                      .0  .0  .0
      makeButton(testpanel, "9", 1, 3, 3, 2, 2.0, 0.0);   //      .0  .0  .0                                         .0  .0
      makeButton(testpanel, "10", 5, 3, 1, 1, 1.0, 0.0);  //                      1                                  .0
      makeButton(testpanel, "11", 5, 4, 2, 1, 1.0, 1.0);  //                      .5  .5                                 1
    add(testpanel, BorderLayout.CENTER);

    // list
    display = new List(3,false);
    display.add("Click on a GridbagLayout button to see its constraints HERE");
    add(display, BorderLayout.SOUTH);
  }                                    // resultweight .5  .0  2   .5  1   1   1                  .5  1   1   .0  1

  public void makeButton(Container cont, String label,
                         int x, int y, int w, int h, double wx, double wy) {
    GridBagConstraints c = new GridBagConstraints();
    Button comp;
    c.fill = GridBagConstraints.BOTH;
    c.gridx = x;
    c.gridy = y;
    c.gridwidth = w;
    c.gridheight = h;
    c.weightx = wx;
    c.weighty = wy;
    comp = new Button(x+","+y+";"+w+","+h);
    comp.setActionCommand("Button <"+label+"> Position=("+x+","+y+") size=("+w+","+h+") weight=("+wx+", "+wy+")");
    comp.addActionListener(this);
    comp.setBackground(new Color(x*40, 100, y*40));
    cont.add(comp, c);
  }

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

  public String getHelpText(){
    return ("You should see a gridbag of 7 columns by 5 rows. The coordinates x and y and " +
            "dimension w and h should be visible as a label on 10 buttons " +
            "(disgard the right column of vte buttons). The buttons should carry the " +
            "following labels: \n    0,0;3,2 " +
                              "\n    3,0;1,3 " +
                              "\n    4,0;3,1 " +
                              "\n    5,1;1,1 " +
                              "\n    4,1;1,4 " +
                              "\n    2,2;1,1 " +
                              "\n    6,1;1,3 " +
                              "\n    1,3;3,2 " +
                              "\n    5,3;1,1 " +
                              "\n    5,4;2,1 \n" +
            "and should be positioned and sized accordingly. Some Buttons may have a width " +
            "or height that looks thinner than expected. This is because weights are assigned " +
            "to each row and each column. These are the column weights: \n " +
            "    0(.5) 1(0) 2(2) 3(.5) 4(1) 5(1) 6(1) \n " +
            "and the row weights: \n" +
            "    0(.5) 1(1) 2(1) 3(.0) 4(1)" );
  }
	
  class SizedPanel extends Panel {
    private int preferredWidth;
    private int preferredHeight;

    public SizedPanel(int w, int h, Color back, LayoutManager manager) {
      super(manager);
      preferredWidth = w;
      preferredHeight = h;
      this.setBackground(back);
    }


    public Dimension getPreferredSize() {
      return new Dimension(preferredWidth, preferredHeight);
    }

    public Dimension getMinimumSize() {
      return new Dimension(preferredWidth, preferredHeight);
    }

  }

  public void start(java.awt.Panel p, boolean b) {
  }

  public void stop(java.awt.Panel p) {
  }
}
