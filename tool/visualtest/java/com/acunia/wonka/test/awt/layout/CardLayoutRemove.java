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


public class CardLayoutRemove extends VisualTestImpl implements ActionListener {

  private java.awt.CardLayout l;

  private Button b1;
  private Button b2;
  private Button b3;
  private Button b4;
  private Button b5;
  private Button b6;
  
  private Panel m;
  private Panel c;

  private Panel p1;
  private Panel p2;
  private Panel p3;
  private Panel p4;
  private Panel p5;

  public CardLayoutRemove() {
    setLayout(new java.awt.BorderLayout());

    l = new java.awt.CardLayout();

    m = new Panel(l);
    c = new Panel(new java.awt.GridLayout(3, 2));

    add(m, java.awt.BorderLayout.CENTER);
    add(c, java.awt.BorderLayout.SOUTH);
    
    p1 = new Panel();
    p2 = new Panel();
    p3 = new Panel();
    p4 = new Panel();
    p5 = new Panel();

    p1.add(new Label("card 1"));
    p2.add(new Label("card 2"));
    p3.add(new Label("card 3"));
    p4.add(new Label("card 4"));
    p5.add(new Label("card 5"));

    m.add(p1, "card 1");
    m.add(p2, "card 2");
    m.add(p3, "card 3");
    m.add(p4, "card 4");
    m.add(p5, "card 5");
    
    b1 = new Button("Delete card 1");
    b2 = new Button("Delete card 2");
    b3 = new Button("Delete card 3");
    b4 = new Button("Delete card 4");
    b5 = new Button("Delete card 5");
    b6 = new Button("Show next card");

    b1.addActionListener(this);
    b2.addActionListener(this);
    b3.addActionListener(this);
    b4.addActionListener(this);
    b5.addActionListener(this);
    b6.addActionListener(this);
    
    c.add(b1);
    c.add(b2);
    c.add(b3);
    c.add(b4);
    c.add(b5);
    c.add(b6);
  }

  public void actionPerformed(ActionEvent e) {
    Button b = (Button)e.getSource();

    if (b == b1) {
      m.remove(p1);
      l.first(m);
    }

    if (b == b2) {
      m.remove(p2);
      l.first(m);
    } 

    if (b == b3) {
      m.remove(p3);
      l.first(m);
    }

    if (b == b4) {
      m.remove(p4);
      l.first(m);
    }

    if (b == b5) {
      m.remove(p5);
      l.first(m);
    }

    if (b == b6) {
      l.next(m);
    }
  }

  static public void main (String[] args) {
    CardLayoutRemove tf = new CardLayoutRemove();
    tf.show();
  }

  public String getHelpText(){
    return "";
  }
}
