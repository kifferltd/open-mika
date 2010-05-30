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


public class GridLayoutHorizontal extends VisualTestImpl implements ActionListener {

  /** Variables*/
  private Button addTop;
  private Button removeAllTop;
  private Panel Top;
  private int topCount;

  private Button addMid;
  private Button removeAllMid;
  private Panel Mid;
  private int midCount;

  private Button addBottom;
  private Button removeAllBottom;
  private Panel Bottom;
  private int bottomCount;

  private java.awt.List display;
  private int step;

  private final static int ELEMENTS = 6;

  /** constructor */
  public GridLayoutHorizontal() {
    step = 100/ELEMENTS;
    setLayout(new java.awt.GridLayout(4,1));

    Panel screentop = new Panel(new java.awt.BorderLayout());
      screentop.setBackground(new Color(140,140,128));
      screentop.add(new Label("GridLayout(1,1)", Label.CENTER), java.awt.BorderLayout.NORTH);
        addTop = new Button("add one button");
        addTop.setBackground(new Color(140,155,128));
        addTop.addActionListener(this);
      screentop.add(addTop, java.awt.BorderLayout.WEST);
        removeAllTop = new Button("remove all buttons");
        removeAllTop.setBackground(new Color(155,140,128));
        removeAllTop.addActionListener(this);
      screentop.add(removeAllTop, java.awt.BorderLayout.EAST);
        Top = new Panel(new java.awt.GridLayout(1,1));
        //buildTopButton();
        Top.setBackground(new Color(155,155,128));
      screentop.add(Top,java.awt.BorderLayout.CENTER);
    add(screentop);

    Panel screenmid = new Panel(new java.awt.BorderLayout());
      screenmid.setBackground(new Color(140,128,140));
      screenmid.add(new Label("GridLayout(1,3)", Label.CENTER), java.awt.BorderLayout.NORTH);
        addMid = new Button("add one button");
        addMid.setBackground(new Color(140,128,155));
        addMid.addActionListener(this);
      screenmid.add(addMid, java.awt.BorderLayout.WEST);
        removeAllMid = new Button("remove all buttons");
        removeAllMid.setBackground(new Color(155,128,140));
        removeAllMid.addActionListener(this);
      screenmid.add(removeAllMid, java.awt.BorderLayout.EAST);
        Mid = new Panel(new java.awt.GridLayout(1,3));
        //buildMidButton();
        Mid.setBackground(new Color(155,128,155));
      screenmid.add(Mid,java.awt.BorderLayout.CENTER);
    add(screenmid);

    Panel screenbottom = new Panel(new java.awt.BorderLayout());
      screenbottom.setBackground(new Color(128,140,140));
      screenbottom.add(new Label("GridLayout(1,9)", Label.CENTER), java.awt.BorderLayout.NORTH);
        addBottom = new Button("add one button");
        addBottom.setBackground(new Color(128,140,155));
        addBottom.addActionListener(this);
      screenbottom.add(addBottom, java.awt.BorderLayout.WEST);
        removeAllBottom = new Button("remove all buttons");
        removeAllBottom.setBackground(new Color(128,155,140));
        removeAllBottom.addActionListener(this);
      screenbottom.add(removeAllBottom, java.awt.BorderLayout.EAST);
        Bottom = new Panel(new java.awt.GridLayout(1,9));
        //buildBottomButton();
        Bottom.setBackground(new Color(128,155,155));
      screenbottom.add(Bottom,java.awt.BorderLayout.CENTER);
    add(screenbottom);

      display = new java.awt.List();
      display.setBackground(new Color(155,155,155));
      display.add("Click on the add-button to add an element to the (1,x) layout");
    add(display);
  }

  public void actionPerformed(ActionEvent evt) {
    Button source = (Button)(evt.getSource());
    if(source == addTop) {
      buildTopButton();
      Top.validate();
      displayTop("Added new element no."+topCount);
    }
    else if(source == removeAllTop) {
      Top.removeAll();
      Top.validate();
      displayTop("removed all elements");
      topCount=0;
    }
    else if(source.getActionCommand() == "Top"){
      Top.remove(source);
      Top.validate();
      displayTop("removed element "+source.getName());
    }
    else if(source == addMid) {
      buildMidButton();
      Mid.validate();
      displayMid("Added new element no."+midCount);
    }
    else if(source == removeAllMid){
      Mid.removeAll();
      Mid.validate();
      displayMid("removed all elements");
      midCount=0;
    }
    else if(source.getActionCommand() == "Mid"){
      Mid.remove(source);
      Mid.validate();
      displayMid("removed element "+source.getName());
    }
    else if(source == addBottom) {
      buildBottomButton();
      Bottom.validate();
      displayBottom("Added new element no."+topCount);
    }
    else if(source == removeAllBottom){
      Bottom.removeAll();
      Bottom.validate();
      displayBottom("removed all elements");
      bottomCount=0;
    }
    else if(source.getActionCommand() == "Bottom"){
      Bottom.remove(source);
      Bottom.validate();
      displayBottom("removed element "+source.getName());
    }
  }

  private void buildTopButton() {
    topCount++;
    Button b = new Button("<"+topCount+">");
    b.setBackground(new Color(128,155+step*(topCount%ELEMENTS), 128));
    b.setActionCommand("Top");
    b.addActionListener(this);
    Top.add(b);
  }

  private void buildMidButton() {
    midCount++;
    Button b = new Button("<"+midCount+">");
    b.setBackground(new Color(155+step*(midCount%ELEMENTS),128,128));
    b.setActionCommand("Mid");
    b.addActionListener(this);
    Mid.add(b);
  }

  private void buildBottomButton() {
    bottomCount++;
    Button b = new Button("<"+bottomCount+">");
    b.setBackground(new Color(128, 128, 155+step*(bottomCount%ELEMENTS)));
    b.setActionCommand("Bottom");
    b.addActionListener(this);
    Bottom.add(b);
  }

  private void displayTop(String message) {
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    display.add("Top gridlayout(1,1): "+Top.getComponentCount()+" elements =>"+message, 0);
  }

  private void displayMid(String message) {
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    display.add("Mid gridlayout(1,3): "+Mid.getComponentCount()+" elements =>"+message, 0);
  }

  private void displayBottom(String message) {
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    display.add("Bottom gridlayout(1,9): "+Bottom.getComponentCount()+" elements =>"+message, 0);
  }

  public String getHelpText(){
    return "A test to verify the horizontal component of Rudolph's GridLayour implementation\n"+
           "As GridLayout primarily looks at the vertical component of the grid and then adds as many columns as are needed to display"+
           " all elements in the given number of lines: the 'c'- component of the GridLayout(r,c) constructor is pretty useless\n"+
           "this is all the more visible in an (1,c) Gridlayout with only one row\n"+
           "This tests shows three Gridlayouts, respectingly of form (1,1), (1,3) and (1,9). The buttons allow you to add new components"+
           " to this row, remove them again and clear the whole row\n"+
           "FOLLOWING THE DEFINITIONS OF A GRIDLAYOUT, REGARDLESS OF THE NUMBER OF COLUMS SPECIFIED, A LAYOUT WITH <n> COMPONENTS"+
           " WILL SHOW ALL THESE <n> COMPONENTS IN A ROW AND EVERY COMPONENT WILL OCCUPY ONE <n>TH OF THE TOTAL SPACE AVAILABLE";
  }
}
