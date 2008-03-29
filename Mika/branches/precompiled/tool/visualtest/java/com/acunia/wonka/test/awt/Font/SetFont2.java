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
// Created: 2001/03/13


/**
 *  Test program for changing fonts in buttons.
 */

package com.acunia.wonka.test.awt.Font;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;
import com.acunia.wonka.test.awt.VisualTester;


public class SetFont2 extends VisualTestImpl implements ActionListener {

  Panel flow1 = new Panel();
  Panel flow2 = new Panel();
  Panel flow3 = new Panel();
  Font  f1 = new Font("courP14",0,14);
  Font  f2 = new Font("helvP08",0,8);
  Font  f3 = new Font("courP17",0,17);

  public SetFont2() {
    super();

    setLayout(new BorderLayout());
    flow1.setLayout(new FlowLayout());
    flow2.setLayout(new FlowLayout());
    flow3.setLayout(new FlowLayout());

    flow1.setFont(f1);
    flow2.setFont(f2);
    flow3.setFont(f3);

    addComp("Button", new Button("Button"));
    addComp("TextArea", new TextArea("TextArea"));
    addComp("List", new java.awt.List());

    add(flow1, BorderLayout.NORTH);
    add(flow2, BorderLayout.CENTER);
    add(flow3, BorderLayout.SOUTH);
    flow1.setBackground(Color.yellow);
    flow2.setBackground(Color.green);
    flow3.setBackground(Color.orange);
  }

  void addComp(String label, Component c) {
    Button b1 = new Button(label);
    Button b2 = new Button(label);
    Button b3 = new Button(label);
    flow1.add(b1);
    flow2.add(b2);
    flow3.add(b3);
    b1.addActionListener(this);
    b2.addActionListener(this);
    b3.addActionListener(this);
  }


  public void actionPerformed(ActionEvent evt) {
    evt.getActionCommand();
  }


  public String getTitle(){
    return "SetFont2";
  }
  public String getHelpText(){
    return ("The test shows three flow layouts on the top, the center and the " +
            "bottom of the screen. In each flow layout three buttons with " +
            "labels are drawn. For each layout a font of a different size is used" +
            "One should observe that the awt system has adapted the size of the " +
            "buttons to the text size.");
  }
     	
  public java.awt.Panel getPanel(VisualTester vt){
    return this;
  }
     	
  public String getLogInfo(java.awt.Panel p, boolean b){
    return "no logging info !";
  }
  public void start(java.awt.Panel p, boolean b){}
  public void stop(java.awt.Panel p){}


  static public void main (String[] args) {
    new SetFont2();
  }
}
