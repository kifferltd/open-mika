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
// Created: 2001/12/09

package com.acunia.wonka.test.awt.GridBagLayout;

import java.awt.Button;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class GridWidth extends VisualTestImpl implements ActionListener {

  class MyConstraints extends GridBagConstraints {
    int id = 0;

    public Object clone() {
      MyConstraints c = (MyConstraints)super.clone();
      c.id      = this.id;
      return c;
    }

  }

  GridBagLayout gbl = new GridBagLayout();

  public GridWidth() {
    super();
    setLayout(gbl);

    int n = 1;

//1
    makeButton("1", 1, 1, n++);
    makeButton("1", 0, 1, 1, 1, n++);
    makeButton("1", 1, 1, n++);
    makeButton("1", 1, 2, 1, 1, n++);
    makeButton("RELATIVE", GridBagConstraints.RELATIVE, 1, n++);
    makeButton("REMAINDER", GridBagConstraints.REMAINDER, 1, n++);

//7
    makeButton("1", 1, 2, n++);
    makeButton("1", 1, 1, n++);
    makeButton("1", 1, 2, n++);  // causes sun jdk bug: button 9 overlap
                                 // buttons 12; and even more buttons if widthened
//10
    makeButton("REMAINDER", GridBagConstraints.REMAINDER, 1, n++);
    makeButton("1", 1, 1, n++);
    makeButton("1", 1, 1, n++);
//13
    makeButton("REMAINDER", -1, 5, GridBagConstraints.REMAINDER, 2, n++);
    makeButton("1", 1, 1, n++);
    makeButton("1", 1, 1, n++);
//16
    makeButton("1", 1, 2, n++);
    makeButton("1", 1, 1, n++);
//18
    makeButton("RELATIVE", GridBagConstraints.RELATIVE, 1, n++);
    makeButton("REMAINDER", GridBagConstraints.REMAINDER, 1, n++);

  }

  void makeButton(String label, int w, int h, int n) {
    Button b = new Button(label+", "+n);
    MyConstraints c = new MyConstraints();

    c.id = n;
    c.gridwidth = w;
    c.gridheight = h;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 1;
    gbl.setConstraints(b, c);
    if (w == GridBagConstraints.REMAINDER || h == GridBagConstraints.REMAINDER)
      b.setBackground(Color.red);
    else
    if (w == GridBagConstraints.RELATIVE || h == GridBagConstraints.RELATIVE)
      b.setBackground(new Color(255,0,255));
    else
      b.setBackground(new Color(0, (h+4)*40, 100));
    add(b);
    b.addActionListener(this);
  }

  void makeButton(String label, int x, int y, int w, int h, int n) {
    Button b;
    MyConstraints c = new MyConstraints();

    c.id = n;
    c.gridx = x;
    c.gridy = y;
    c.gridwidth = w;
    c.gridheight = h;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 1;

    b = new Button(label+", "+n);
    if (w == GridBagConstraints.REMAINDER || h == GridBagConstraints.REMAINDER)
      b.setBackground(Color.yellow);
    else
      b.setBackground(new Color(0, 100,(h/2+3)*50));
    add(b);
    b.addActionListener(this);
    gbl.setConstraints(b, c);
  }

  public void actionPerformed(ActionEvent evt) {
    Button b = (Button)evt.getSource();
    MyConstraints gbc = (MyConstraints)(gbl.getConstraints(b));
    if (gbc.gridwidth != GridBagConstraints.REMAINDER
             && gbc.gridwidth != GridBagConstraints.RELATIVE) {
      if (++gbc.gridwidth > 4) {
          gbc.gridwidth = 1;
      }
      gbl.setConstraints(b, gbc);
      b.setLabel("" + gbc.gridwidth + ", " + gbc.id);
      invalidate();
      validate();
    }
  }

  public String getHelpText(){
    return ("The test shows a panel of buttons, laid out with a gridbag layout" +
            "manager. The panel is divided in cells by a grid of 7 columns " +
            "by 8 rows. Rows 3, 4 and 7 are invisible since their weights are 0.0. " +
            "(row and column numbers start with 0). The width of a column is " +
            "determined by the button having the widest label in that column. \n" +
            "The label of a button is composed " +
            "of its 'width' - multiples of 1 cell width - and the number of the button " +
            "in the sequence of adding buttons to the panel. \n" +
            "You should see 19 buttons arranged as in the following table: \n\n"+
            "_1__3__5__5__5__5__6 \n" +
            "_2_____7__8__9_10_10 \n" +
            "____4__7_____9_11_12 \n" +
            "13_13_13_13_13_13_13 \n" +
            "14_15_16_17_18_18_19 \n\n" +
            "Blue buttons have absolute coordinates, as defined by the values of " +
            "their 'gridx' " +
            "and 'gridy' GridBagConstraints. All blue buttons are one row high. \n" +
            "Green buttons have no absolute coordinates and are positioned " +
            "relative to previously added components (of lower sequence number). Most " +
            "Buttons are 1 row high. Buttons 7, 9 and 16 are 2 rows high (row 7 has " +
            "weight 0.0)\n" +
            "Remark that the relative position of buttons is not influenced by the " +
            "presence of buttons on rows that are higher than the current row (buttons " +
            "3 and 5).\n " +
            "Red buttons have relative position and terminate a row of buttons " +
            "(their constraint 'gridwidth' has value GridBagConstratnts.REMAINDER). \n" +
            "The yellow button also is a row terminator but it has an absolute " +
            "y coordinate: it is positioned on row 5 though it terminates row 2  " +
            "(row 3 and 4 have zero weight)\n" +
            "Purple buttons have a 'gridwidth' constraint of value " +
            "GridBagConstraints.RELATIVE. \n" +
            "An additional feature of this test is that a blue or green button's width " +
            "is increased by " +
            "one grid cell when it is pressed. A button can attain a maximum width of 4. " +
            "The panel's layout is recalculated and repainted each time a button is pressed." +
            "Remark that, on the column next to the button you are " +
            "trying to widthen, and on a row different from the row of that button, " +
            "another button must start. " +
            "Otherwise the new column has weight 0.0 and the button will not become 'visibly' wider. "
            );
  }
	
  public void start(java.awt.Panel p, boolean b) {
  }

  public void stop(java.awt.Panel p) {
  }

}
