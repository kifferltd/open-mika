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


/*
**  Test program for panel with layout manager.
*/

package com.acunia.wonka.test.awt.layout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Font;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;


public class NoLayout extends VisualTestImpl implements ActionListener {

    Panel flow1 = new Panel();
    Panel nolo1 = new Panel();
    Panel flow2 = new Panel();
    Panel nolo2 = new Panel();

    public NoLayout() {
      super();

      flow1.setLayout(new java.awt.FlowLayout());
      nolo1.setLayout(null);
      flow2.setLayout(new java.awt.FlowLayout());
      nolo2.setLayout(null);

      flow1.setFont(new Font("helvR12", Font.PLAIN, 12));
      nolo1.setFont(new Font("helvR12", Font.PLAIN, 12));
      flow2.setFont(new Font("helvB20", Font.BOLD, 20));
      nolo2.setFont(new Font("helvB20", Font.BOLD, 20));

      addComp1("Button", new Button("Button"));
      addComp1("TextArea", new Button("TextArea"));
      addComp1("List", new Button("List"));

      addComp2("Button", new Button("Button"), 1, 1, 60, 40);
      addComp2("TextArea", new Button("TextArea"), 65, 1, 60, 40);
      addComp2("List", new Button("List"), 129, 1, 60, 40);

      setLayout(new java.awt.GridLayout(4,1));
      add(flow1);
      add(flow2);
      add(nolo1);
      add(nolo2);

    }

    void addComp1(String label, Component c) {
      Button a = new Button(label);
      Button b = new Button(label);
      flow1.add(a);
      flow2.add(b);
      a.addActionListener(this);
      b.addActionListener(this);
    }

    void addComp2(String label, Component c, int x, int y, int w, int h) {
      Button a = new Button(label);
      Button b = new Button(label);
      a.setBounds(x,y,w,h);
      b.setBounds(x,y,w,h);
      nolo1.add(a);
      nolo2.add(b);
      a.addActionListener(this);
      b.addActionListener(this);
    }


    public void actionPerformed(ActionEvent evt) {
      evt.getActionCommand();
    }


    public String getHelpText(){
      return ("The program shows, in default gray colors, a grid flow of four rows, one column. " +
              "Each row containing three buttons, labeled \"Button\", \"TextArea\" and \"List\". " +
              "The two upper rows of buttons use a flow layout, for the two lower rows, a \"null\" " +
              "layout manager was set. One should see that the buttons of row 1 and 2 are arranged " +
              "symmetrically around the center of these rows, while the buttons of row 3 and 4 " +
              "are left aligned. The size of the buttons of row 1 and 2 was set automatically, " +
              "while the size of the buttons in row 3 and 4 had to be set manually. We forgot, on " +
              "purpose, to resize the buttons of row 4 for the bigger font. As a result the labels of " +
              "the first two buttons are partially visible.");
    }
     	
}
