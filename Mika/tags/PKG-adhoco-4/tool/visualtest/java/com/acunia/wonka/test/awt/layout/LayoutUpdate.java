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
// Created: 2001/11/21

package com.acunia.wonka.test.awt.layout;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class LayoutUpdate extends VisualTestImpl implements ActionListener{
  /****************************************************************/
  /** variables
  */
  private final static int ELEMENTSIZE=20;

  private BorderedLabel line2Left;
  private BorderedLabel line2Right;

  private BorderedLabel line4Left;
  private BorderedLabel line4Right;

  private BorderedLabel line6Left;
  private BorderedLabel line6Right;

  private BorderedLabel line8Left;
  private BorderedLabel line8Right;

  private BorderedLabel line10;

  private BorderedLabel line12;

  private BorderedLabel line14Mid;

  private BorderedLabel line16Mid;

  private Button paint18Left;
  private Button paint18Right;
  private BorderedLabel line18Left;
  private BorderedLabel line18Right;

  private Button paint20Right;
  private BorderedLabel line20Left;
  private BorderedLabel line20Right;

  private Button paint22;
  private BorderedLabel line22;

  private Button paint24Mid;
  private BorderedLabel line24Mid;
  /****************************************************************/
  /** constructor
  */
  public LayoutUpdate() {
    super();
    setLayout(new java.awt.BorderLayout());
    setBackground(Color.yellow);


    // left panel with command buttons

    Panel buttonsleft = new Panel(new java.awt.GridLayout(12,1));
      buttonsleft.add(new Label("two components with Display text",Label.CENTER));
      Panel line2 = new Panel(new java.awt.GridLayout(1,2));
        line2Left = new BorderedLabel();
        line2.add(line2Left);
        line2Right = new BorderedLabel();
        line2.add(line2Right);
      buttonsleft.add(line2);

      buttonsleft.add(new Label("two components without Display text",Label.CENTER));
      Panel line4 = new Panel(new java.awt.GridLayout(1,2));
        line4Left = new BorderedLabel();
        line4.add(line4Left);
        line4Right = new BorderedLabel();
        line4.add(line4Right);
      buttonsleft.add(line4);

      buttonsleft.add(new Label("First component with Display text",Label.CENTER));
      buttonsleft.add(new Label("second without",Label.CENTER));
      Panel line6 = new Panel(new java.awt.GridLayout(1,2));
        line6Left = new BorderedLabel();
        line6.add(line6Left);
        line6Right = new BorderedLabel();
        line6.add(line6Right);
      buttonsleft.add(line6);

      buttonsleft.add(new Label("First component without, ",Label.CENTER));
      buttonsleft.add(new Label("second with Display text",Label.CENTER));
      Panel line8 = new Panel(new java.awt.GridLayout(1,2));
        line8Left = new BorderedLabel();
        line8.add(line8Left);
        line8Right = new BorderedLabel();
        line8.add(line8Right);
      buttonsleft.add(line8);
    add(buttonsleft, java.awt.BorderLayout.WEST);

    Panel buttonsright = new Panel(new java.awt.GridLayout(12,1));
      buttonsright.add(new Label("one component, directly added",Label.CENTER));
      buttonsright.add(new Label("with Display function",Label.CENTER));
        line10 = new BorderedLabel();
      buttonsright.add(line10);

      buttonsright.add(new Label("one component, directly added",Label.CENTER));
      buttonsright.add(new Label("without Display function",Label.CENTER));
        line12 = new BorderedLabel();
      buttonsright.add(line12);

      buttonsright.add(new Label("Two labels",Label.CENTER));
      buttonsright.add(new Label("a component, with Display function",Label.CENTER));
      Panel line14 = new Panel(new java.awt.GridLayout(1,3));
        line14.add(new Label("Left label", Label.LEFT));
        line14Mid = new  BorderedLabel();
        line14.add(line14Mid);
        line14.add(new Label("Right label", Label.RIGHT));
      buttonsright.add(line14);

      buttonsright.add(new Label("Two labels",Label.CENTER));
      buttonsright.add(new Label("a component, without Display function",Label.CENTER));
      Panel line16 = new Panel(new java.awt.GridLayout(1,3));
        line16.add(new Label("Left label", Label.LEFT));
        line16Mid = new  BorderedLabel();
        line16.add(line16Mid);
        line16.add(new Label("Right label", Label.RIGHT));
      buttonsright.add(line16);
    add(buttonsright, java.awt.BorderLayout.EAST);

    Panel buttonsmid = new Panel(new java.awt.GridLayout(12,1));
      Panel line17 = new Panel(new java.awt.GridLayout(1,2));
        paint18Left = new Button("Paint left");
        paint18Left.addActionListener(this);
        line17.add(paint18Left);
        paint18Right = new Button("Paint right");
        paint18Right.addActionListener(this);
        line17.add(paint18Right);
      buttonsmid.add(line17);
      Panel line18 = new Panel(new java.awt.GridLayout(1,2));
        line18Left = new BorderedLabel();
        line18.add(line18Left);
        line18Right = new BorderedLabel();
        line18.add(line18Right);
      buttonsmid.add(line18);

      paint20Right = new Button("Paint line 20 right");
      paint20Right.addActionListener(this);
      buttonsmid.add(paint20Right);
      Panel line20 = new Panel(new java.awt.GridLayout(1,2));
        line20Left = new BorderedLabel();
        line20.add(line20Left);
        line20Right = new BorderedLabel();
        line20.add(line20Right);
      buttonsmid.add(line20);

      paint22 = new Button("Paint line 22");
      paint22.addActionListener(this);
      buttonsmid.add(paint22);
      line22 = new BorderedLabel();
      buttonsmid.add(line22);

      paint24Mid = new Button("Paint line 24 mid");
      paint24Mid.addActionListener(this);
      buttonsmid.add(paint24Mid);
      Panel line24 = new Panel(new java.awt.GridLayout(1,3));
        line24.add(new Label("Left label", Label.LEFT));
        line24Mid = new  BorderedLabel();
        line24.add(line24Mid);
        line24.add(new Label("Right label", Label.RIGHT));
      buttonsmid.add(line24);

    add(buttonsmid, java.awt.BorderLayout.CENTER);


    displayData();
  }


  /****************************************************************/
  /** Default show all selected labels
  */
  public void displayData() {
      line2Left.setText("(line 2, left)");
      line2Right.setText("(line 2, right)");

      //line4Left.setText("(line 4, left)");
      //line4Right.setText("(line 4, right)");

      line6Left.setText("(line 6, left)");
      //line6Right.setText("(line 6, right)");

      //line8Left.setText("(line 8, left)");
      line8Right.setText("(line 8, right)");

      line10.setText("line10");

      //line12.setText("line12");

      line14Mid.setText("line14");

      //line16Mid.setText("line16");
      line20Left.setText("(line 20, left)");
  }


  /****************************************************************/
  /** On button clicked, show the button label
  */
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if(source == paint18Left) {
      line18Left.setText("18 left");
    }
    else if(source == paint18Right){
      line18Right.setText("18 right");
    }
    else if(source == paint20Right){
      line20Right.setText("20 right");
    }
    else if(source == paint22){
      line22.setText("line 22");
    }
    else if(source == paint24Mid) {
      line24Mid.setText("24 mid");
    }
  }
  /****************************************************************/
  /** inner class borderedlabel: a component with a border and a text*/
  class BorderedLabel extends Component  {
    private String text;

    public BorderedLabel() {
      super();
      this.setBackground(Color.lightGray);
      text = "";
    }

    void setText(String newtext) {
      text = newtext;
      this.repaint();
    }

    /** discrete minimum size */
    public Dimension getMinimumSize() {
      return new Dimension(25,12);
    }

    /** discrete preferred size */
    public Dimension getPreferredSize() {
      return new Dimension(25,12);
    }

    public void paint(Graphics g) {
      update(g);
    }

    public void update(Graphics g) {
      Dimension d=this.getSize();
      if(d.width>0 && d.height>0) {
        g.clearRect(1,1,d.width-2, d.height-2);
        g.setColor(this.getForeground());
        g.drawRect(1,1,d.width-2, d.height-2);
        g.drawString(text,5,d.height/2+3);
      }
    }
  }

 /****************************************************************/
  /** VTE help*/
  public String getHelpText(){
    return "A GridBagLayout test on the GridBag size calculation by weight:\n" +
            "The screen shows a central panel with 3x3 elements ranged using a GridBagLayout. Each element has a central square of"+
            ELEMENTSIZE+" x "+ELEMENTSIZE+" pixels that shows its preferred size. \n"+
            "You can click an element to select it. The selected element will change color and displayed in the right panel."+
            " will appear its name, and weightX, weightY settings\n"+
            "Using the (+) and (-) buttons on the left panel you can change the preferred weights of the selected element."+
            " This forces the layout to change size. \n\n"+
            "ITEMS TO TEST:\n\n"+
            "-> Overall grid weights: in the bottom area area watch the weightX and weightY of the selected element"+
            " Compare to the overall weightX and weightY for the rows and columns and check that the overall weight is equal"+
            " to the biggest value for the weights of all elements in that row/colomn.\n"+
            "-> Division of the free widths: also regard the total width of each gridbag column and the 'free' width after"+
            " subtracting the (uniform) element width and check that the total leftover width is devided amongst the columns"+
            " proportional to their column weightX.\n"+
            "-> Division of the free heights: just like above regard the total heights and free heights of each row and make sure"+
            " that the leftover height is devided amongst the rows proportional to their row weightY.";
  }

  public void start(java.awt.Panel p, boolean b) {
      //currentElement.repaint();
      displayData();//currentElement);
  }

  public void stop(java.awt.Panel p) {
  }
}
