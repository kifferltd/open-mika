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


public class BorderLayout2 extends VisualTestImpl implements ActionListener {

  private FillComponent north;
  private FillComponent south;
  private FillComponent east;
  private FillComponent west;
  private FillComponent center;

  private Button addNorth;
  private Button removeNorth;
  private Button addSouth;
  private Button removeSouth;
  private Button addCenter;
  private Button removeCenter;
  private Button addEast;
  private Button removeEast;
  private Button addWest;
  private Button removeWest;

  private Panel screen;

  public BorderLayout2() {
    setLayout(new java.awt.BorderLayout());
    setBackground(new Color(96,96,48));
    north = new FillComponent("north", new Color(96,32,32), Color.red, Color.white);
    west = new FillComponent("west", new Color(128,128,32), Color.yellow, Color.black);
    center = new FillComponent("center", new Color(32,96,32), Color.green, Color.white);
    east = new FillComponent("east", new Color(32,32,96), Color.blue, Color.white);
    south = new FillComponent("south", new Color(96,32,96), Color.magenta, Color.white);

    Panel buttons = new Panel(new java.awt.GridLayout(10,0));
      addNorth = new Button("add North component");
      addNorth.addActionListener(this);
      addNorth.setBackground(new Color(128,64,64));
      buttons.add(addNorth);

      removeNorth = new Button("remove North component");
      removeNorth.addActionListener(this);
      removeNorth.setBackground(new Color(192,128,128));
      buttons.add(removeNorth);

      addWest = new Button("add West component");
      addWest.addActionListener(this);
      addWest.setBackground(new Color(192,192,64));
      buttons.add(addWest);

      removeWest = new Button("remove West component");
      removeWest.addActionListener(this);
      removeWest.setBackground(new Color(255,255,128));
      buttons.add(removeWest);

      addCenter = new Button("add Center component");
      addCenter.addActionListener(this);
      addCenter.setBackground(new Color(64,128,64));
      buttons.add(addCenter);

      removeCenter = new Button("remove Center component");
      removeCenter.addActionListener(this);
      removeCenter.setBackground(new Color(128,192,128));
      buttons.add(removeCenter);

      addEast = new Button("add East component");
      addEast.addActionListener(this);
      addEast.setBackground(new Color(64,64,128));
      buttons.add(addEast);

      removeEast = new Button("remove East component");
      removeEast.addActionListener(this);
      removeEast.setBackground(new Color(128,128,192));
      buttons.add(removeEast);

      addSouth = new Button("add South component");
      addSouth.addActionListener(this);
      addSouth.setBackground(new Color(128,64,128));
      buttons.add(addSouth);

      removeSouth = new Button("remove South component");
      removeSouth.addActionListener(this);
      removeSouth.setBackground(new Color(192,128,192));
      buttons.add(removeSouth);
    add(buttons, java.awt.BorderLayout.EAST);

    screen = new Panel(new java.awt.BorderLayout());
    add(screen,java.awt.BorderLayout.CENTER);
  }

  public void actionPerformed(ActionEvent e) {
    Button b = (Button)e.getSource();
    if(b == addNorth) {
      screen.add(north,java.awt.BorderLayout.NORTH);
      screen.validate();
    }
    else if(b == removeNorth) {
      screen.remove(north);
      screen.validate();
    }
    else if(b == addSouth) {
      screen.add(south,java.awt.BorderLayout.SOUTH);
      screen.validate();
    }
    else if(b == removeSouth) {
      screen.remove(south);
      screen.validate();
    }
    else if(b == addEast) {
      screen.add(east,java.awt.BorderLayout.EAST);
      screen.validate();
    }
    else if(b == removeEast) {
      screen.remove(east);
      screen.validate();
    }
    else if(b == addWest) {
      screen.add(west,java.awt.BorderLayout.WEST);
      screen.validate();
    }
    else if(b == removeWest) {
      screen.remove(west);
      screen.validate();
    }
    else if(b == addCenter) {
      screen.add(center,java.awt.BorderLayout.CENTER);
      screen.validate();
    }
    else if(b == removeCenter) {
      screen.remove(center);
      screen.validate();
    }
  }

  static public void main (String[] args) {
    BorderLayout tf = new BorderLayout();
    tf.show();
  }

  public String getHelpText(){
    return "A test to verify Rudolph's BorderLayout implementation\n"+
           "Use the add- and remove- buttons on the right of the screen to add or remove a component to the panel on the left"+
           " respectingly to the north, south, east, west or center of the panel using a BorderLayout layout manager\n"+
           "\nitems to test\n\n"+
           " => Correct location of the components added and a correct recalculation of all other components already present when adding"+
           " or removing a component\n"+
           " => correct size: Every one of the components displays in its center a rectangle that shows the minimum size of the component."+
           " FOR ALL OF THE BORDER LOCATIONS THE FRAME AROUND THIS RECTANGLE MUST BE COMPLETELY VISIBLE\n"+
           " => pushing the add-button for a component several times in a row to make sure the application doesn't hang or crash when the"+
           " same component is added twice\n\n WONKA 0.7.2: ADDING THE SAME COMPONENT TWICE CAUSES THE APPLICATION TO HANG\n"+
           " =>pushing the remove-button for a component several times in a row to make sure the application doesn't hang or crash when the"+
           " same component is removed twice";
  }

  /** inner class display component */
  class FillComponent extends Container {  //extends Component {
    private Dimension bounds;
    private Rectangle innerRect;
    private Color innerColor;
    private Color textColor;
    private int middle;

    public FillComponent(String name, Color background, Color rectangle, Color text) {
      super();
      this.setBackground(background);
      this.setName(name);
      bounds = new Dimension();
      innerRect = new Rectangle(0,0,60,50);
      innerColor = rectangle;
      textColor = text;
    }

    public Dimension getMinimumSize() {
      return new Dimension(innerRect.width, innerRect.height);
    }

    public Dimension getPreferredSize() {
      return new Dimension(innerRect.width, innerRect.height);
    }

/*
    public void doLayout() {
      innerRect.setLocation((this.getSize().width-innerRect.width)/2, (this.getSize().height-innerRect.height)/2);
      middle = this.getSize().height/2;
      repaint();
    }

*/
    /** paint */
    public void paint(Graphics g) {
    	update(g);
    }
      	
    public void update(Graphics g) {
      if(! bounds.equals(this.getSize()) ) {
        bounds.setSize(this.getSize());
        innerRect.setLocation((bounds.width-innerRect.width)/2, (bounds.height-innerRect.height)/2);
        middle = this.getSize().height/2;
      }
      g.setColor(this.getBackground());
      g.fillRect(0,0,bounds.width-1, bounds.height-1);
      g.setColor(innerColor);
      g.drawRect(innerRect.x, innerRect.y, innerRect.width-1, innerRect.height-1);
      g.fillRect(innerRect.x+2, innerRect.y+2, innerRect.width-4, innerRect.height-4);
      g.setColor(textColor);
      g.drawString(this.getName(),innerRect.x+5, middle);

    }
  }
}
