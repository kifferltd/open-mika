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
// Created: 2001/06/20

package com.acunia.wonka.test.awt.Graphics;

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;

public class DrawString2 extends VisualTestImpl implements ActionListener  {


  public class SubPanel extends Panel {

    /*
    ** Class needed for the 'setText' method of class DrawString2; among the
    ** components we want to test, Panel is the only one that has no 'setText'
    ** method.
    ** remark that this panel can inherit the font of the parent panel,
    ** but the drawstring method we use for implementing the setText method
    ** needs a font anyway.
    */

    String slocal;

    public SubPanel (String s) {
      super();
      slocal = s;
    }

    public void setText(String s) {
      slocal = s;
      super.repaint();
    }

    public int paintOne(Graphics g, String s, Font f, int x, int y) {
      g.setFont(f);
      FontMetrics fm = g.getFontMetrics();
      g.drawString(s, x, y+fm.getLeading()+fm.getAscent());
      return fm.getHeight();
    }

    public void paint(Graphics g) {
      int fh=0;
      int y=0;

  //    fh=paintOne(g, slocal, new Font("helvR08", 0, 8), 10 , y);
  //    fh=paintOne(g, slocal, new Font("helvR14", 0, 14), 10, y+=fh);
  //    fh=paintOne(g, slocal, new Font("helvR17", 0, 21), 10, y+=fh);
      fh=paintOne(g, slocal, new Font("helvR17", 0, 17), 10, y+=fh);
  //    fh=paintOne(g, slocal, new Font("courR10", 0, 10), 10, y+=16);
  //    fh=paintOne(g, slocal, new Font("courR14", 0, 14), 10, y+=fh);
  //    fh=paintOne(g, slocal, new Font("courR17", 0, 17), 10, y+=fh);
  //    fh=paintOne(g, slocal, new Font("courR25", 0, 25), 10, y+=fh);
  //    fh=paintOne(g, slocal, new Font("helvR24", 1, 30), 10, y+=fh);

    }
  }


  Panel flowtop = new Panel();
  Panel card = new Panel();
  Panel flowbot = new Panel();

  String s1 = "Short string";
  String s2 = "This medium size string should be partly invisible only ";
  String s3 = "This long string tells\tthe story\nof Snow-White and the seven\ndwarves. It contains\tsome tab\nand new line characters. ";
  String s4 = "This long string also tells the story of Snow-White and the seven dwarves, but it contains no special characters. ";
  String s5 = "String \001wit\005h \010ma\014ny \020unde\024fined \030char\033acters. ";

  SubPanel  spanel = new SubPanel(s1);
  Label     lblL   = new Label(s1, Label.LEFT);
  Label     lblC   = new Label(s1, Label.CENTER);
  Label     lblR   = new Label(s1, Label.RIGHT);
  Button    but    = new Button(s1);
  Checkbox  chkb   = new Checkbox(s1);
  List      lst    = new List();
  TextField tf     = new TextField(s1);
  TextArea  ta     = new TextArea(s1);

  public DrawString2() {
    super();

    setLayout(new BorderLayout());
    flowtop.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    flowtop.setBackground(new Color(0, 200, 255));

    card.setLayout(new CardLayout());
    card.setForeground(Color.white); // temporarily needed: default for List and TextArea is 'white'
    card.setBackground(new Color(0,150,255));
    card.setFont(new Font("helvR17", 0, 17));

    flowbot.setLayout(new FlowLayout());
    flowbot.setBackground(new Color(0,200,255));

    lst.add("Mercury");
    lst.add("Venus");
    lst.add("Earth");
    lst.add("Mars");
    lst.add("Planetoids");
    lst.add(s1);

    addComp("Panel", spanel);
    addComp("Label-L", lblL);
    addComp("Label-C", lblC);
    addComp("Label-R", lblR);
    addComp("Button", but);
    addComp("Checkbox", chkb);
    addComp("List", lst);
    addComp("TextField",tf);
    addComp("TextArea", ta);

    addComp2("String1");
    addComp2("String2");
    addComp2("String3");
    addComp2("String4");
    addComp2("String5");

    add(flowtop, BorderLayout.NORTH);
    add(card, BorderLayout.CENTER);
    add(flowbot, BorderLayout.SOUTH);
  }

  void addComp(String label, Component c) {
    Button b = new Button(label);
    card.add(c, label);
    flowtop.add(b);
    b.addActionListener(this);
  }

  void addComp2(String label) {
    Button b = new Button(label);
    flowbot.add(b);
    b.addActionListener(this);
  }


  public void actionPerformed(ActionEvent evt) {

    String arg = evt.getActionCommand();
    if (arg.startsWith("String")) {
      if (arg.equals("String1")) {
        this.setText(s1);
      }
      else if (arg.equals("String2")) {
        this.setText(s2);
      }
      else if (arg.equals("String3")) {
        this.setText(s3);
      }
      else if (arg.equals("String4")) {
        this.setText(s4);
      }
      else if (arg.equals("String5")) {
        this.setText(s5);
      }
    }
    else {
      CardLayout l = (CardLayout)card.getLayout();
      l.show(card, arg);
    }
  }

  public void setText(String s) {

    spanel.setText(s);
    lblL.setText(s);
    lblC.setText(s);
    lblR.setText(s);
    but.setLabel(s);
    chkb.setLabel(s);
    lst.remove(5);
    lst.add(s,5);
    tf.setText(s);
    ta.setText(s);
    this.validate();  // needed under sun jvm to visually update the CardLayout on screen.
  }

  public String getHelpText(){
    return ("This test tests Graphics.drawString() for drawing on all kind of components. On top of " +
            "the screen a row of buttons, drawn in light-blue, allows you to select a type of " +
            "component to draw to. The center of the screen, drawn in blue, shows the component " +
            "you selected with some text on it. At the bottom of the screen, a row of buttons, " +
            "drawn in light-blue, allows you to change the string to be " +
            "drawn on the component. You can choose between 1) a short string that fits on every " +
            "component, 2) a medium sized string that will not fit on some components. 3) a long " +
            "string that fits on no component, except on the TextArea which is able to interpret " +
            "the line feeds in the string and cut the string in lines accordingly. 4) a long " +
            "string like the former, but without line feeds, which consequently does not even fit " +
            "on the TextArea. 5) A medium sized string containing many undefined characters. " +
            "The test is successful if, 1) the Panel and TextArea show all strings in their top left " +
            "corner; 2) in Label-L, Label-C, Label-R, Button, Checkbox and TextField, the strings are " +
            "vertically centered, and left aligned, except in Label-C and Button, where strings are " +
            "horizontally centered, and in Label-R where the string is right aligned; 3) in List the " +
            "strings appear left aligned on the sixth line (lines 1 to 5 show planet names);" +
            "3) if string1 fits on all components, 4) if string2 is almost visible on all components, " +
            "5) if string3 is only partly visible and shows litle rectangles in some positions, " +
            "indicating unrecognized characters, except for the TextArea which recognizes these " +
            "characters as tabs and new-lines, 6) if string4 is only partly visible on all components, " +
            "7) if string5 is almost completely visible and shows many unrecognised characters on all " +
            "components\n\n" +
            "Remark buggy behaviour: all components initially should inherit the size 17 Helvetica font of the " +
            "central Panel with Card Layout. The TextArea cannot do that (yet)!" );
  }

	
  public void start(java.awt.Panel p, boolean b) {
  }

  public void stop(java.awt.Panel p) {
  }
}
