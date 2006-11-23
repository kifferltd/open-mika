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

package com.acunia.wonka.test.awt.GridBagLayout;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class GridBagWeights extends VisualTestImpl implements ActionListener  {
  /****************************************************************/
  /** variables
  */
  protected final static int ELEMENTSIZE=20;

//  private List display;
  protected Panel testPanel;
  protected FillComponent currentElement;

  private BorderedLabel number;
  private BorderedLabel weightX;
  private BorderedLabel weightY;

  private Button increaseX;
  private Button decreaseX;
  private Button increaseY;
  private Button decreaseY;

  private BorderedLabel colWidthsLeft;
  private BorderedLabel colWidthsMid;
  private BorderedLabel colWidthsRight;
  private BorderedLabel totalColWidths;

  private BorderedLabel colWeightsLeft;
  private BorderedLabel colWeightsMid;
  private BorderedLabel colWeightsRight;

  private BorderedLabel rowHeightsLeft;
  private BorderedLabel rowHeightsMid;
  private BorderedLabel rowHeightsRight;
  private BorderedLabel totalRowHeights;

  private BorderedLabel rowWeightsLeft;
  private BorderedLabel rowWeightsMid;
  private BorderedLabel rowWeightsRight;
  /*
  private Checkbox none;
  private Checkbox horizontal;
  private Checkbox vertical;
  private Checkbox both;
  */
  /****************************************************************/
  /** constructor
  */
  public GridBagWeights() {
    super();
    setLayout(new BorderLayout());
    setBackground(Color.yellow);


    // left panel with command buttons
    add(buildButtonPanel(), BorderLayout.EAST);

    // center test panel with gridbag layout
    Panel mid = new Panel(new BorderLayout());
      mid.add(buildTopPanel(), BorderLayout.NORTH);

      GridBagLayout gbl = new GridBagLayout();
      GridBagConstraints gbc = new GridBagConstraints();
      testPanel = new Panel(gbl);
      testPanel.setBackground(new Color(200,200,120) );
        //gbc.weightx=1.0;
        //gbc.weighty=1.0;
        gbc.fill = GridBagConstraints.BOTH;
        for(int i=0; i<3; i++) {
          gbc.gridy=i;
          for(int j=0; j<3; j++) {
            gbc.gridx=j;
            currentElement = new FillComponent("no."+(3*i+j)+" ("+j+", "+i+")", Color.white, new Color(i*48,100,j*48));
            testPanel.add(currentElement);
            gbl.setConstraints(currentElement, gbc);
          }
        }
      mid.add(testPanel, BorderLayout.CENTER);
      mid.add(buildWeightsPanel(), BorderLayout.SOUTH);

    add(mid, BorderLayout.CENTER);

    displayData(currentElement);
    currentElement.inverse(true);
  }

  /****************************************************************/
  /** Auxilliary: build the button panel. With this function as protected,
  * we can call it from a subclass in order to save the code and the variable access */
  protected Panel buildButtonPanel() {
    Panel buttons = new Panel(new GridLayout(11,1));
      buttons.add(new Label("  columns  : weightX", Label.CENTER));
      Panel split3 = new Panel(new GridLayout(1,3));
        colWeightsLeft = new BorderedLabel(Color.lightGray);
        split3.add(colWeightsLeft);
         colWeightsMid = new BorderedLabel(Color.lightGray);
        split3.add(colWeightsMid);
         colWeightsRight = new BorderedLabel(Color.lightGray);
        split3.add(colWeightsRight);
      buttons.add(split3);

      buttons.add(new Label("widths / free widths  ", Label.CENTER));
      Panel split4 = new Panel(new GridLayout(1,3));
        colWidthsLeft = new BorderedLabel(Color.lightGray);
        split4.add(colWidthsLeft);
        colWidthsMid = new BorderedLabel(Color.lightGray);
        split4.add(colWidthsMid);
        colWidthsRight = new BorderedLabel(Color.lightGray);
        split4.add(colWidthsRight);
      buttons.add(split4);

      Panel split5 = new Panel(new GridLayout(1,2));
        split5.add(new Label("total ", Label.RIGHT));
          totalColWidths = new BorderedLabel(Color.lightGray);
        split5.add(totalColWidths);
      buttons.add(split5);

      buttons.add(new Label());

      buttons.add(new Label("  Rows  : weightY",Label.CENTER));
      Panel split6 = new Panel(new GridLayout(1,3));
        rowWeightsLeft = new BorderedLabel(Color.lightGray);
        split6.add(rowWeightsLeft);
        rowWeightsMid = new BorderedLabel(Color.lightGray);
        split6.add(rowWeightsMid);
        rowWeightsRight = new BorderedLabel(Color.lightGray);
        split6.add(rowWeightsRight);
      buttons.add(split6);

      buttons.add(new Label("heights / free heights  ",Label.CENTER));
      Panel split7 = new Panel(new GridLayout(1,3));
        rowHeightsLeft = new BorderedLabel(Color.lightGray);
        split7.add(rowHeightsLeft);
        rowHeightsMid = new BorderedLabel(Color.lightGray);
        split7.add(rowHeightsMid);
        rowHeightsRight = new BorderedLabel(Color.lightGray);
        split7.add(rowHeightsRight);
      buttons.add(split7);

      Panel split8 = new Panel(new GridLayout(1,2));
        split8.add(new Label("total ", Label.RIGHT));
          totalRowHeights = new BorderedLabel(Color.lightGray);
        split8.add(totalRowHeights);
      buttons.add(split8);
    return buttons;
  }

  /****************************************************************/
  /** Auxilliary: build the top panel. With this function as protected,
  * we can call it from a subclass in order to save the code and the variable access */
  protected Panel buildTopPanel() {
    Panel top = new Panel(new GridLayout(1,3));
      top.add(new Label("element : ",Label.RIGHT));
        number = new BorderedLabel(Color.lightGray);
      top.add(number);
      top.add(new Label());
    return top;
  }

  /****************************************************************/
  /** Auxilliary: build the weights display panel. With this function as protected,
  * we can call it from a subclass in order to save the code and the variable access */
  protected Panel buildWeightsPanel() {
    Panel weights = new Panel(new GridLayout(2,3));
      weights.add(new Label("Element: weightX : ",Label.RIGHT));
      weightX = new BorderedLabel(Color.white);
      weights.add(weightX);
      Panel split1 = new Panel(new GridLayout(1,2));
        increaseX = new Button("+");
        increaseX.addActionListener(this);
        split1.add(increaseX);
        decreaseX = new Button("-");
        decreaseX.addActionListener(this);
        split1.add(decreaseX);
      weights.add(split1);
      weights.add(new Label("weightY : ",Label.RIGHT));
      weightY = new BorderedLabel(Color.white);
      weights.add(weightY);
      Panel split2 = new Panel(new GridLayout(1,2));
        increaseY = new Button("+");
        increaseY.addActionListener(this);
        split2.add(increaseY);
        decreaseY = new Button("-");
        decreaseY.addActionListener(this);
        split2.add(decreaseY);
      weights.add(split2);
    return weights;
  }
  /****************************************************************/
  /** Button pressed
  */
  public void actionPerformed(ActionEvent evt) {
    Button source = (Button)evt.getSource();
    if(source == increaseX) {
      UpdateComponent(currentElement, 0.5, 0.0);
    }
    else if(source == decreaseX) {
      UpdateComponent(currentElement, -0.5, 0.0);
    }
    else if(source == increaseY) {
      UpdateComponent(currentElement, 0.0, 0.5);
    }
    else if(source == decreaseY) {
      UpdateComponent(currentElement, 0.0, -0.5);
    }
  }

  /****************************************************************/
  /** Display a message in the display list
  */
  public void displayData(FillComponent target) {
    // set name, width,height into textfields
    GridBagLayout gbl = (GridBagLayout)testPanel.getLayout();
    GridBagConstraints gbc = gbl.getConstraints(target);
    // set name, width,height into textfields
    number.setText(target.getName());
    weightX.setText(gbc.weightx);
    weightY.setText(gbc.weighty);

    // gridbagLayout weights
    GridBagLayout toscan = (GridBagLayout)testPanel.getLayout();
    double[][] weights = toscan.getLayoutWeights();
    if(weights[0].length>=3 && weights[1].length>=3){
      colWeightsLeft.setText(weights[0][0]);
      colWeightsMid.setText(weights[0][1]);
      colWeightsRight.setText(weights[0][2]);
      rowWeightsLeft.setText(weights[1][0]);
      rowWeightsMid.setText(weights[1][1]);
      rowWeightsRight.setText(weights[1][2]);
    }

    // GridBagLayout sizes
    int[][] dimensions = toscan.getLayoutDimensions();
    if(dimensions[0].length>=3 && dimensions[1].length>=3){
      colWidthsLeft.setText(dimensions[0][0], (dimensions[0][0]-ELEMENTSIZE));
      colWidthsMid.setText(dimensions[0][1], (dimensions[0][1]-ELEMENTSIZE));
      colWidthsRight.setText(dimensions[0][2], (dimensions[0][2]-ELEMENTSIZE));
      rowHeightsLeft.setText(dimensions[1][0], (dimensions[1][0]-ELEMENTSIZE));
      rowHeightsMid.setText(dimensions[1][1], (dimensions[1][1]-ELEMENTSIZE));
      rowHeightsRight.setText(dimensions[1][2], (dimensions[1][2]-ELEMENTSIZE));
    }
    // overall sizes
    Dimension panelsize = testPanel.getSize();
    if(panelsize.width>0 && panelsize.height>0) {
      totalColWidths.setText(panelsize.width, (panelsize.width-3*ELEMENTSIZE));
      totalRowHeights.setText(panelsize.height, (panelsize.height-3*ELEMENTSIZE));
    }

  }

  /****************************************************************/
  /** Display a message in the display list
  */
  public void UpdateComponent(FillComponent target, double dx, double dy) {
    // get layont and constraints
    GridBagLayout gbl = (GridBagLayout)testPanel.getLayout();
    GridBagConstraints gbc = gbl.getConstraints(target);
    // set new values
    gbc.weightx+=dx;
    gbc.weighty+=dy;
    // update constraints and layout
    if(gbc.weightx>=0.0 && gbc.weighty>=0.0) {
      //weightX.setText(""+gbc.weightx);
      //weightY.setText(""+gbc.weighty);
      gbl.setConstraints(target,gbc);
      testPanel.invalidate();
      testPanel.validate();
      displayData(target);
    }
  }


  /****************************************************************/
  /** inner class borderedlabel: a component with a border and a text*/
  class BorderedLabel extends Component  {
    private String text;
    public BorderedLabel() {
      super();
      text = "";
    }

    public BorderedLabel(Color backcolor) {
      super();
      this.setBackground(backcolor);
      text = "";
    }

    public BorderedLabel(Color textcolor, Color backcolor) {
      super();
      this.setBackground(backcolor);
      this.setForeground(textcolor);
      text = "";
    }

    void setText(String newtext) {
      text = newtext;
      this.repaint();
    }

    void setText(double newvalue) {
      text = Double.toString(newvalue);
      this.repaint();
    }

    void setText(int value1, int value2) {
      text = value1+" ("+value2+")";
      this.repaint();
    }

   /** discrete minimum size */
    public Dimension getMinimumSize() {
      return new Dimension(40,12);
    }

    /** discrete preferred size */
    public Dimension getPreferredSize() {
      return new Dimension(40,12);
    }

    public void paint(Graphics g) {
      update(g);
    }

    public void update(Graphics g) {
      Dimension d=this.getSize();
      g.clearRect(1,1,d.width-2, d.height-2);
      g.setColor(this.getForeground());
      g.drawRect(1,1,d.width-2, d.height-2);
      g.drawString(text,5,d.height/2+3);
    }
  }

  /****************************************************************/
  /** inner class display component: a colored box of fixed size that throws a mouse event when clicked*/
  class FillComponent extends Component  {
    private Dimension bounds;
    private Rectangle innerRect;
    private String name;
    private Color background;
    private Color foreground;
    private boolean inversed;

    /** default constructor */
    public FillComponent(String parent, Color fore, Color back){
      this(parent, fore,back, ELEMENTSIZE, ELEMENTSIZE);
    }

    /** full constructor */
    public FillComponent(String parent, Color fore, Color back, int w, int h){
      super();
      foreground = fore;
      background =back;
      inversed = false;
      name = parent;
      this.addMouseListener(new MouseAdapter() {
                                                 public void mousePressed(MouseEvent evt) {
                                                   currentElement.inverse(false);
                                                   currentElement=(FillComponent)evt.getSource();
                                                   displayData(currentElement);
                                                   inverse(true);
                                                 }
                                               }
                                             );
      bounds = new Dimension();
      innerRect = new Rectangle(0, 0, w, h);
    }

    /** get name */
    public String getName() {
      return name;
    }
    /*inverse colors/restore colors*/
    public void inverse(boolean newvalue) {
      inversed = newvalue;
      this.repaint();
    }

    /*change size from big to small & vice versa*/
    public void setSize(int w, int h) {
      innerRect = new Rectangle(0, 0, w, h);
      this.repaint();
    }

    /*change size from big to small & vice versa*/
    public boolean updateSize(int dx, int dy) {
      boolean allowed = true;
      if((innerRect.width+dx)>0) {
        innerRect.width+=dx;
      }
      else {
        allowed = false;
      }
      if((innerRect.height+dy)>0) {
        innerRect.height+=dy;
      }
      else {
        allowed = false;
      }
      this.repaint();
      return allowed;
    }


    /** discrete minimum size */
    public Dimension getMinimumSize() {
      return new Dimension(innerRect.width, innerRect.height);
    }

    /** discrete preferred size */
    public Dimension getPreferredSize() {
      return new Dimension(innerRect.width, innerRect.height);
    }

    /** actual size */
    public Dimension getActualSize() {
      return bounds; //new Dimension(bounds.width, bounds.height);
    }

    /** paint */
    public void paint(Graphics g) {
    	update(g);
    }
      	
    public void update(Graphics g) {
      bounds.setSize(this.getSize());
      innerRect.setLocation((bounds.width-innerRect.width)/2, (bounds.height-innerRect.height)/2);

      g.setColor((inversed)?foreground:background);
      g.fillRect(0, 0, bounds.width-1, bounds.height-1);
      g.setColor((inversed)?background:foreground);
      g.drawRect(innerRect.x+2, innerRect.y+2, innerRect.width-5, innerRect.height-5);
      g.drawRect(innerRect.x, innerRect.y, innerRect.width-1, innerRect.height-1);
      g.drawLine(innerRect.x, innerRect.y, innerRect.x+innerRect.width-1, innerRect.y+innerRect.height-1);
      g.drawLine(innerRect.x, innerRect.y+innerRect.height-1, innerRect.x+innerRect.width-1, innerRect.y);
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
      currentElement.repaint();
      displayData(currentElement);
  }

  public void stop(java.awt.Panel p) {
  }
}
