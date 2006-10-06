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

package com.acunia.wonka.test.awt.Font;

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;

public class SetFont extends VisualTestImpl implements ActionListener  {

  Font f1 = new Font("courP14", 0, 14);
  Font f2 = new Font("helvP14", 0, 18);
  Font f3 = new Font("helvP17", 0, 21);
  Font f4 = new Font("helvB17", 1, 21);
  Font f5 = new Font("courP24", 0, 24);
  Font f6 = new Font("helvP24", 0, 30);

  public class SubPanel extends Panel {

    Font flocal;
    String slocal;

    public SubPanel (String s, Font f) {
      super();
      slocal = s;
      flocal = f;
    }

    public void setFont(Font f) {
      flocal = f;
      super.repaint();
    }

    public void paint(Graphics g) {
      g.setFont(flocal);
      FontMetrics fm = g.getFontMetrics();
      g.drawString(slocal, 0, fm.getLeading()+fm.getAscent());
    }
  }


  Panel flowtop = new Panel();
  Panel card = new Panel();
  Panel flowbot = new Panel();

  String story = "This is the story of Rudolph and the seven ... ";

  SubPanel  spanel = new SubPanel(story, f1);
  Label     lbl    = new Label(story, Label.LEFT);
  Button    but    = new Button(story);
  Checkbox  chkb   = new Checkbox(story);
  List      lst    = new List();
  TextField tf     = new TextField(story);
  TextArea  ta     = new TextArea(story);

  public SetFont() {
    super();

    setLayout(new BorderLayout());
    flowtop.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    flowtop.setBackground(new Color(255, 200, 0));

    card.setLayout(new CardLayout());
    card.setForeground(Color.white); // temporarily needed: default for List and TextArea is 'white'
    card.setBackground(new Color(255,150,0));
    card.setFont(f1);

    flowbot.setLayout(new FlowLayout());
    flowbot.setBackground(new Color(255,200,0));

    lst.add("Batida de Coco");
    lst.add("Pisang");
    lst.add("Cuba Libre");
    lst.add("Tropical Heat");
    lst.add("... ");
    lst.add(story);


    addComp("Panel", spanel);
    addComp("Label", lbl);
    addComp("Button", but);
    addComp("Checkbox", chkb);
    addComp("List", lst);
    addComp("TextField",tf);
    addComp("TextArea", ta);

    addComp2("Font1");
    addComp2("Font2");
    addComp2("Font3");
    addComp2("Font4");
    addComp2("Font5");
    addComp2("Font6");

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
    if (arg.startsWith("Font")) {
      if (arg.equals("Font1")) {
        this.setFont(f1);
      }
      else if (arg.equals("Font2")) {
        this.setFont(f2);
      }
      else if (arg.equals("Font3")) {
        this.setFont(f3);
      }
      else if (arg.equals("Font4")) {
        this.setFont(f4);
      }
      else if (arg.equals("Font5")) {
        this.setFont(f5);
      }
      else if (arg.equals("Font6")) {
        this.setFont(f6);
      }
    }
    else {
      CardLayout l = (CardLayout)card.getLayout();
      l.show(card, arg);
    }
  }


  public void setFont(Font f) {

    spanel.setFont(f);
    lbl.setFont(f);
    but.setFont(f);
    chkb.setFont(f);
    lst.setFont(f);
    tf.setFont(f);
    ta.setFont(f);
    this.validate();  // needed under sun jvm to visually update the CardLayout on screen.
  }

  public String getHelpText(){
    return ("This test tests the setFont method for drawing on all kind of components. On top of " +
            "the screen a row of buttons, drawn in light-orange, allows you to select a type of " +
            "component to draw to. The center of the screen, drawn in orange, shows the component " +
            "you selected with some text on it. At the bottom of the screen, a row of buttons, " +
            "drawn in light-orange, allows you to change the font to be used for drawing " +
            "on the component. One can choose between six fonts, with increasing size from \"Font1\" " +
            "to \"Font6\". Note that the font of \"List\" and \"TextArea\" can not be changed." );
  }

	
  public void start(java.awt.Panel p, boolean b) {
  }

  public void stop(java.awt.Panel p) {
  }
}
