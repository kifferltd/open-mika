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


// Author: D. Buytaert
// Created: 2001/04/20

package com.acunia.wonka.test.awt.Color;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Label;

import com.acunia.wonka.test.awt.VisualTestImpl;


public class StandardColors extends VisualTestImpl {

  public StandardColors() {
    setLayout(new GridLayout(4, 3));

    Label l1 = new Label("white");
    l1.setBackground(Color.white);
    add(l1);

    Label l2 = new Label("lightGray");
    l2.setBackground(Color.lightGray);
    add(l2);

    Label l3 = new Label("gray");
    l3.setBackground(Color.gray);
    l3.setForeground(Color.white);
    add(l3);

    Label l4 = new Label("black");
    l4.setBackground(Color.black);
    l4.setForeground(Color.white);
    add(l4);

    Label l5 = new Label("red");
    l5.setBackground(Color.red);
    add(l5);

    Label l6 = new Label("pink");
    l6.setBackground(Color.pink);
    add(l6);

    Label l7 = new Label("orange");
    l7.setBackground(Color.orange);
    add(l7);

    Label l8 = new Label("yellow");
    l8.setBackground(Color.yellow);
    add(l8);

    Label l9 = new Label("green");
    l9.setBackground(Color.green);
    add(l9);

    Label l10 = new Label("magenta");
    l10.setBackground(Color.magenta);
    add(l10);

    Label l11 = new Label("cyan");
    l11.setBackground(Color.cyan);
    add(l11);

    Label l12 = new Label("blue");
    l12.setBackground(Color.blue);
    add(l12);
  }

  static public void main (String[] args) {
    StandardColors tf = new StandardColors();
    tf.show();
  }

  public String getHelpText(){
    return "A test to (a) verify wether rudolph's internal color model is nicely coupled to java.awt.SystemColor and (b) to inspect the default colors in java.awt.Color.";
  }
}
