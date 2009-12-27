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


public class BorderLayout extends VisualTestImpl implements ActionListener {

  private Label ln;
  private Label lw;
  private Label lc;
  private Label le;
  private Label ls;

  private Button bn;
  private Button bw;
  private Button bc;
  private Button be;
  private Button bs;

  private Panel bl;
  private Panel ct;

  public BorderLayout() {
    setLayout(new java.awt.BorderLayout());

    bl = new Panel();
    bl.setLayout(new java.awt.BorderLayout(2, 2));
 
    layout(0);
    layout(1);
    layout(2);
    layout(3);
    layout(4);

    add(bl, java.awt.BorderLayout.CENTER);

    ct = new Panel();
    ct.setLayout(new java.awt.GridLayout(1, 5));

    bn = new Button("North");
    bw = new Button("West");
    bc = new Button("Center");
    be = new Button("East");
    bs = new Button("South");

    bn.addActionListener(this);
    bw.addActionListener(this);
    bc.addActionListener(this);
    be.addActionListener(this);
    bs.addActionListener(this);
    
    ct.add(bn);
    ct.add(be);
    ct.add(bs);
    ct.add(bw);
    ct.add(bc);

    add(ct, java.awt.BorderLayout.SOUTH);
  }

  public void layout(int location) {
    switch (location) {
      case 0:
        if (ln != null) {
          bl.remove(ln);
          ln = null;
        }
        else {
          ln = new Label("North", Label.CENTER);
          ln.setBackground(Color.gray);
          bl.add(ln, java.awt.BorderLayout.NORTH);
        }
        break;
      case 1:
        if (le != null) {
          bl.remove(le);
          le = null;
        }
        else {
          le = new Label("East", Label.CENTER);
          le.setBackground(Color.gray);
          bl.add(le, java.awt.BorderLayout.EAST);
        }
        break;
      case 2:
        if (ls != null) {
          bl.remove(ls);
          ls = null;
        }
        else {
          ls = new Label("South", Label.CENTER);
          ls.setBackground(Color.gray);
          bl.add(ls, java.awt.BorderLayout.SOUTH);
        }
        break;
      case 3:
        if (lw != null) {
          bl.remove(lw);
          lw = null;
        }
        else {
          lw = new Label("West", Label.CENTER);
          lw.setBackground(Color.gray);
          bl.add(lw, java.awt.BorderLayout.WEST);
        }
        break;
      case 4:
        if (lc != null) {
          bl.remove(lc);
          lc = null;
        }
        else {
          lc = new Label("Center", Label.CENTER);
          lc.setBackground(Color.gray);
          bl.add(lc, java.awt.BorderLayout.CENTER);
        }
        break;
    }
    bl.validate();
  }

  public void actionPerformed(ActionEvent e) {
    Button b = (Button)e.getSource();

    if (b == bn) layout(0);
    if (b == be) layout(1);
    if (b == bs) layout(2);
    if (b == bw) layout(3);
    if (b == bc) layout(4);
  }

  static public void main (String[] args) {
    BorderLayout tf = new BorderLayout();
    tf.show();
  }

  public String getHelpText(){
    return "A test to verify Rudolph's BorderLayout implementation: click the buttons to remove them one by one and verify the intermediate results with those described in Sun's documentation.";
  }
}
