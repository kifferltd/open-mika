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


public class FlowLayoutHorizontal extends VisualTestImpl implements ActionListener {

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

  private int step;

  private final static int ELEMENTS = 6;

  /** constructor */
  public FlowLayoutHorizontal() {
    step = 100/ELEMENTS;
    setLayout(new java.awt.GridLayout(3,1));

    Panel screentop = new Panel(new java.awt.BorderLayout());
      screentop.setBackground(new Color(140,140,128));
      screentop.add(new Label("FlowLayout (small elements)", Label.CENTER), java.awt.BorderLayout.NORTH);
        addTop = new Button("add one button");
        addTop.setBackground(new Color(140,155,128));
        addTop.addActionListener(this);
      screentop.add(addTop, java.awt.BorderLayout.WEST);
        removeAllTop = new Button("remove all buttons");
        removeAllTop.setBackground(new Color(155,140,128));
        removeAllTop.addActionListener(this);
      screentop.add(removeAllTop, java.awt.BorderLayout.EAST);
        Top = new Panel(new java.awt.FlowLayout());
        //buildTopButton();
        Top.setBackground(new Color(155,155,128));
      screentop.add(Top,java.awt.BorderLayout.CENTER);
    add(screentop);

    Panel screenmid = new Panel(new java.awt.BorderLayout());
      screenmid.setBackground(new Color(140,128,140));
      screenmid.add(new Label("FlowLayout (bigger elements)", Label.CENTER), java.awt.BorderLayout.NORTH);
        addMid = new Button("add one button");
        addMid.setBackground(new Color(140,128,155));
        addMid.addActionListener(this);
      screenmid.add(addMid, java.awt.BorderLayout.WEST);
        removeAllMid = new Button("remove all buttons");
        removeAllMid.setBackground(new Color(155,128,140));
        removeAllMid.addActionListener(this);
      screenmid.add(removeAllMid, java.awt.BorderLayout.EAST);
        Mid = new Panel(new java.awt.FlowLayout());
        //buildMidButton();
        Mid.setBackground(new Color(155,128,155));
      screenmid.add(Mid,java.awt.BorderLayout.CENTER);
    add(screenmid);

    Panel screenbottom = new Panel(new java.awt.BorderLayout());
      screenbottom.setBackground(new Color(128,140,140));
      screenbottom.add(new Label("FlowLayout (biggest elements)", Label.CENTER), java.awt.BorderLayout.NORTH);
        addBottom = new Button("add one button");
        addBottom.setBackground(new Color(128,140,155));
        addBottom.addActionListener(this);
      screenbottom.add(addBottom, java.awt.BorderLayout.WEST);
        removeAllBottom = new Button("remove all buttons");
        removeAllBottom.setBackground(new Color(128,155,140));
        removeAllBottom.addActionListener(this);
      screenbottom.add(removeAllBottom, java.awt.BorderLayout.EAST);
        Bottom = new Panel(new java.awt.FlowLayout());
        //buildBottomButton();
        Bottom.setBackground(new Color(128,155,155));
      screenbottom.add(Bottom,java.awt.BorderLayout.CENTER);
    add(screenbottom);
  }

  public void actionPerformed(ActionEvent evt) {
    Button source = (Button)(evt.getSource());
    if(source == addTop) {
      buildTopButton();
      Top.validate();
    }
    else if(source == removeAllTop) {
      Top.removeAll();
      Top.validate();
      topCount=0;
    }
    else if(source.getActionCommand() == "Top"){
      Top.remove(source);
      Top.validate();
    }
    else if(source == addMid) {
      buildMidButton();
      Mid.validate();
    }
    else if(source == removeAllMid){
      Mid.removeAll();
      Mid.validate();
      midCount=0;
    }
    else if(source.getActionCommand() == "Mid"){
      Mid.remove(source);
      Mid.validate();
    }
    else if(source == addBottom) {
      buildBottomButton();
      Bottom.validate();
    }
    else if(source == removeAllBottom){
      Bottom.removeAll();
      Bottom.validate();
      bottomCount=0;
    }
    else if(source.getActionCommand() == "Bottom"){
      Bottom.remove(source);
      Bottom.validate();
    }
  }

  private void buildTopButton() {
    topCount++;
    Button b = new Button("<Top no."+topCount+">");
    b.setBackground(new Color(128,155+step*(topCount%ELEMENTS), 128));
    b.setActionCommand("Top");
    b.addActionListener(this);
    Top.add(b);
  }

  private void buildMidButton() {
    midCount++;
    Button b = new Button("<Mid element no."+midCount+">");
    b.setBackground(new Color(155+step*(midCount%ELEMENTS),128,128));
    b.setActionCommand("Mid");
    b.addActionListener(this);
    Mid.add(b);
  }

  private void buildBottomButton() {
    bottomCount++;
    Button b = new Button("<Bottom element number "+bottomCount+", a rally, really, really, really big element>");
    b.setBackground(new Color(128, 128, 155+step*(bottomCount%ELEMENTS)));
    b.setActionCommand("Bottom");
    b.addActionListener(this);
    Bottom.add(b);
  }



  public String getHelpText(){
    return "A test to verify the dynamic layout of Rudolph's FlowLayout implementation\n"+
           "A flowLayout displays as many elements in a row as can be added and then switches to the next row for the next elements\n"+
           "This tests shows three FlowLayouts. The buttons allow you to add new components"+
           " to this row, remove them again and clear the whole layout. Each component has place for two rows of layouts. The elements"+
           " are sized so that the top Container can display three of them in a row, the middle container two and the bottom container"+
           " only one (and even that one not completely)\n\n"+
           "Items to test:\n"+
           " -> the placement of the elements over several rows.\n"+
           " -> the moving up of the elements of the lower row when elements of the higher rows get deleted\n"+
           " -> Managing elements that do not fit on the screen anymore. These elements becomming visible when higher (visible) elements"+
           " get deleted and thus space for them becomes available.\n"+
           " -> In the bottom row, the element is bigger then the available width. At least as much of the center part of it as is visible"+
           " should be displayed.\n"+
           "FOLLOWING THE DEFINITIONS OF A GRIDLAYOUT, REGARDLESS OF THE NUMBER OF COLUMS SPECIFIED, A LAYOUT WITH <n> COMPONENTS"+
           " WILL SHOW ALL THESE <n> COMPONENTS IN A ROW AND EVERY COMPONENT WILL OCCUPY ONE <n>TH OF THE TOTAL SPACE AVAILABLE";
  }
}
