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

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;

public class GridBagSizes extends VisualTestImpl implements ActionListener  {
  /****************************************************************/
  /** variables
  */
//  private List display;
  private Panel testPanel;
  FillComponent currentElement;

  private BorderedLabel number;
  private BorderedLabel preferedX;
  private BorderedLabel preferedY;
  private BorderedLabel sizeX;
  private BorderedLabel sizeY;
  private BorderedLabel spareX;
  private BorderedLabel spareY;
  private BorderedLabel colLeft;
  private BorderedLabel colMid;
  private BorderedLabel colRight;
  private BorderedLabel rowLeft;
  private BorderedLabel rowMid;
  private BorderedLabel rowRight;
  private Button increaseX;
  private Button decreaseX;
  private Button increaseY;
  private Button decreaseY;
  /*
  private Checkbox none;
  private Checkbox horizontal;
  private Checkbox vertical;
  private Checkbox both;
  */
  /****************************************************************/
  /** constructor
  */
  public GridBagSizes() {
    super();
    setLayout(new BorderLayout());
    setBackground(Color.yellow);

    // left panel with command buttons
    Panel buttons = new Panel(new GridLayout(12,1));
      buttons.add(new Label("Preferred width  ",Label.CENTER));
      Panel split2 = new Panel(new GridLayout(1,3));
          decreaseX = new Button("-");
          decreaseX.addActionListener(this);
        split2.add(decreaseX);
          preferedX = new BorderedLabel(Color.white);
        split2.add(preferedX);
          increaseX = new Button("+");
          increaseX.addActionListener(this);
        split2.add(increaseX);
      buttons.add(split2);

      Panel split3 = new Panel(new GridLayout(1,2));
        split3.add(new Label("Actual ",Label.RIGHT));
          sizeX = new BorderedLabel(Color.lightGray);
        split3.add(sizeX);
      buttons.add(split3);

      Panel split4 = new Panel(new GridLayout(1,2));
        split4.add(new Label("Leftover ",Label.RIGHT));
          spareX = new BorderedLabel(Color.lightGray);
        split4.add(spareX);
      buttons.add(split4);

      buttons.add(new Label("Total colunm widths",Label.CENTER));
      Panel split8 = new Panel(new GridLayout(1,3));
        colLeft = new BorderedLabel(Color.lightGray);
        split8.add(colLeft);
        colMid = new BorderedLabel(Color.lightGray);
        split8.add(colMid);
        colRight = new BorderedLabel(Color.lightGray);
        split8.add(colRight);
      buttons.add(split8);

      buttons.add(new Label("Preferred height  ",Label.CENTER));
      Panel split5 = new Panel(new GridLayout(1,3));
          decreaseY = new Button("-");
          decreaseY.addActionListener(this);
        split5.add(decreaseY);
          preferedY = new BorderedLabel(Color.white);
        split5.add(preferedY);
          increaseY = new Button("+");
          increaseY.addActionListener(this);
        split5.add(increaseY);
      buttons.add(split5);

      Panel split6 = new Panel(new GridLayout(1,2));
        split6.add(new Label("Actual ",Label.RIGHT));
          sizeY = new BorderedLabel(Color.lightGray);
        split6.add(sizeY);
      buttons.add(split6);

      Panel split7 = new Panel(new GridLayout(1,2));
        split7.add(new Label("Leftover ",Label.RIGHT));
          spareY = new BorderedLabel(Color.lightGray);
        split7.add(spareY);
      buttons.add(split7);

      buttons.add(new Label("Total row heights",Label.CENTER));
      Panel split9 = new Panel(new GridLayout(1,3));
        rowLeft = new BorderedLabel(Color.lightGray);
        split9.add(rowLeft);
        rowMid = new BorderedLabel(Color.lightGray);
        split9.add(rowMid);
        rowRight = new BorderedLabel(Color.lightGray);
        split9.add(rowRight);
      buttons.add(split9);
    add(buttons, BorderLayout.EAST);
    //right side
    Panel right = new Panel(new BorderLayout());
      //header
      Panel split1 = new Panel(new GridLayout(1,5));
        split1.add(new Label("GridBag",Label.RIGHT));
        split1.add(new Label("Layout demo",Label.LEFT));
        split1.add(new Label("Element : ",Label.RIGHT));
        number = new BorderedLabel(Color.lightGray);
        split1.add(number);
        split1.add(new Label());
      right.add(split1, BorderLayout.NORTH);

      // center test panel with gridbag layout
      GridBagLayout gbl = new GridBagLayout();
      GridBagConstraints gbc = new GridBagConstraints();
      testPanel = new Panel(gbl);
      testPanel.setBackground(new Color(200,200,120) );
        gbc.weightx=1.0;
        gbc.weighty=1.0;
        gbc.fill = GridBagConstraints.BOTH;
        for(int i=0; i<3; i++) {
          gbc.gridy=i;
          for(int j=0; j<3; j++) {
            gbc.gridx=j;
            currentElement = new FillComponent("No."+(3*i+j)+"  ("+j+", "+i+")", Color.white, new Color(i*48,100,j*48));
            testPanel.add(currentElement);
            gbl.setConstraints(currentElement, gbc);
          }
        }
      right.add(testPanel, BorderLayout.CENTER);

      right.add(new Label("All elements have equal weights : weightx = weighty = 1.0", Label.CENTER), BorderLayout.SOUTH);
    add(right, BorderLayout.CENTER);

    displayData(currentElement);
    currentElement.inverse(true);
  }

  /****************************************************************/
  /** Button pressed
  */
  public void actionPerformed(ActionEvent evt) {
    Button source = (Button)evt.getSource();
    boolean succeeded = false;
    if(source == increaseX) {
      succeeded = currentElement.updateSize(10,0);
    }
    else if(source == decreaseX) {
      succeeded = currentElement.updateSize(-10,0);
    }
    else if(source == increaseY) {
      succeeded = currentElement.updateSize(0,10);
    }
    else if(source == decreaseY) {
      succeeded = currentElement.updateSize(0,-10);
    }

    if(succeeded) {
      testPanel.invalidate();
      testPanel.validate();
      displayData(currentElement);
    }
  }

  /****************************************************************/
  /** Display a message in the display list
  */
  public void displayData(FillComponent target) {
    // set name, width,height into textfields
    number.setText(target.getName());
    preferedX.setText(target.getPreferredSize().width);
    preferedY.setText(target.getPreferredSize().height);
    // Actual sizes, spare sizes
    sizeX.setText(target.getActualSize().width);
    sizeY.setText(target.getActualSize().height);
    spareX.setText(target.getActualSize().width-target.getPreferredSize().width);
    spareY.setText(target.getActualSize().height-target.getPreferredSize().height);

    // GridBagLayout sizes
    GridBagLayout toscan = (GridBagLayout)testPanel.getLayout();
    int[][] dimensions = toscan.getLayoutDimensions();
    if(dimensions[0].length>=3 && dimensions[1].length>=3){
      colLeft.setText(dimensions[0][0]);
      colMid.setText(dimensions[0][1]);
      colRight.setText(dimensions[0][2]);
      rowLeft.setText(dimensions[1][0]);
      rowMid.setText(dimensions[1][1]);
      rowRight.setText(dimensions[1][2]);
    }
  }


  /****************************************************************/
  /** inner class display component: a colored box of fixed size that throws a mouse event when clicked*/
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

    void setText(int newvalue) {
      text = Integer.toString(newvalue);
      this.repaint();
    }

   /** discrete minimum size */
    public Dimension getMinimumSize() {
      return new Dimension(35,12);
    }

    /** discrete preferred size */
    public Dimension getPreferredSize() {
      return new Dimension(35,12);
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
      this(parent, fore,back, 20,20);
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
      g.fillRect(1, 1, bounds.width-2, bounds.height-2);
      g.setColor((inversed)?background:foreground);
      g.drawRect(innerRect.x+2, innerRect.y+2, innerRect.width-4, innerRect.height-4);
      g.drawRect(innerRect.x, innerRect.y, innerRect.width-1, innerRect.height-1);
      g.drawLine(innerRect.x, innerRect.y, innerRect.x+innerRect.width, innerRect.y+innerRect.height);
      g.drawLine(innerRect.x, innerRect.y+innerRect.height, innerRect.x+innerRect.width, innerRect.y);
    }
  }

  /****************************************************************/
  /** VTE help*/
  public String getHelpText(){
    return "A GridBagLayout test on the GridBag size calculation :\n" +
            "The screen shows a central panel with 3x3 elements ranged using a GridBagLayout. The central rectangle of each element"+
            " shows its preferred size.\n"+
            "You can click an element to select it. The selected element will change color and displayed in the right panel."+
            " will appear its name, actual size, preferred size, and the difference between these two sizes\n"+
            "Also, using the (+) and (-) buttons on the left panel you can change the preferred width and height of an element."+
            " This forces the layout to change size. The width of each colomn and the height of each row will be displayed, just as well \n\n"+
            "ITEMS TO TEST:\n\n"+
            "-> division of the free width: All elements are added with the same horizontal and vertical weights weightx = weighty = 1.0"+
            " This means that the 'left-over' space from the layout (the part of the elements between the inner rectangle and the border)"+
            " is devided equally between the rows.\n"+
            "clicking the different elements and regarding their size definitions,you can check this";
  }

  public void start(java.awt.Panel p, boolean b) {
      currentElement.repaint();
      displayData(currentElement);
  }

  public void stop(java.awt.Panel p) {
  }
}
