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

import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;


public class BorderLayout3 extends VisualTestImpl implements ActionListener {

  private boolean vn = true;
  private boolean vw = true;
  private boolean vs = true;
  private boolean ve = true;
  
  private Button bn;
  private Button bw;
  private Button be;
  private Button bs;
  private Button bv;

  private Panel ct;

  private Label north;
  private Label south;
  private Label west;
  private Label east;

  public BorderLayout3() {
    setLayout(new java.awt.BorderLayout());
    setBackground(Color.orange);

    ct = new Panel();
    ct.setFont(new Font("courier",Font.BOLD,10));
    ct.setLayout(new java.awt.GridLayout(5, 1));
    ct.setBackground(Color.white);

    bn = new Button("Hide North");
    bw = new Button("Hide West");
    be = new Button("Hide East");
    bs = new Button("Hide South");
    bv = new Button("Validate Container");
    bv.setForeground(Color.red);

    bn.addActionListener(this);
    bw.addActionListener(this);
    be.addActionListener(this);
    bs.addActionListener(this);
    bv.addActionListener(this);

    north = new Label("NORTH", Label.CENTER);
    north.setBackground(Color.red);
    south = new Label("SOUTH", Label.CENTER);
    south.setBackground(Color.blue);
    west = new Label("WEST", Label.CENTER);
    west.setBackground(Color.green);
    east = new Label("EAST", Label.CENTER);
    east.setBackground(Color.yellow);

    add(north, java.awt.BorderLayout.NORTH);
    add(south, java.awt.BorderLayout.SOUTH);
    add(west, java.awt.BorderLayout.WEST);
    add(east, java.awt.BorderLayout.EAST);

    ct.add(bn);
    ct.add(be);
    ct.add(bs);
    ct.add(bw);
    ct.add(bv);

    add(ct, java.awt.BorderLayout.CENTER);
  }

  public void actionPerformed(ActionEvent e) {
    Button b = (Button)e.getSource();

    if (b == bn) {
      vn = !vn;
      north.setVisible(vn);
      if (vn) {
        bn.setLabel("Hide North");
      }
      else {
        bn.setLabel("Show North");
      }
    }
    else if (b == be) {
      ve = !ve;
      east.setVisible(ve);
      if (ve) {
        be.setLabel("Hide East");
      }
      else {
        be.setLabel("Show East");
      }
    }
    else if (b == bs){
      vs = !vs;
      south.setVisible(vs);
      if (vs) {
        bs.setLabel("Hide South");
      }
      else {
        bs.setLabel("Show South");
      }
    }
    else if (b == bw){
      vw = !vw;
      west.setVisible(vw);
      if (vw) {
        bw.setLabel("Hide West");
      }
      else {
        bw.setLabel("Show West");
      }
    }
    else if (b == bv){
      this.validate();
    }

  }

  public String getHelpText(){
    return "Test to verify Rudolph's BorderLayout implementation.\n " +
           "  click the north-south-east-west buttons in the center panel of the border-layout to show and hide the "+
           "labels in the borders; clicking a button calls setVisible(true/false) on the corresponding label. \n" +
           "  click the 'validate' button to execute a 'validate' instruction on the main container; as long as "+
           "the validate button is not pressed the extra free space of a hidden border is not reclaimed by the center; " +
           "instead the corresonding border is drawn in the container's orange background color. \n" +
           "if the validate button is pressed when a label is hidden, the center is resized to fill the extra free space.\n\n" +
           "Difference with execution under sun java 2 : if a labed was hidden and the 'validate' key pressed, the " +
           "corresponding border is filled by the center panel; if the button of that label is hit again to make it visible, "+
           "Rudolph will not show it until the validate key is pressed again; with sun's awt on the other hand there is no " +
           "need to press the validate key, but the center panel is not resized first, and the label hides part of it. "+
           "this different behaviour is caused by the fact that sun's awt and Rudolph draw components of a container in a " +
           "different order.";
  }
}
