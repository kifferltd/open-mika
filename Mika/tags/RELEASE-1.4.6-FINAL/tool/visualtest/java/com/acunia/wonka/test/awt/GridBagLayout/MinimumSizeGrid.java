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


package com.acunia.wonka.test.awt.GridBagLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.acunia.wonka.test.awt.VisualTestImpl;


public class MinimumSizeGrid extends VisualTestImpl{
  private final static int LAMPWIDTH=30;
  private final static int LAMPHEIGHT=30;

  private List display;

  public MinimumSizeGrid() {
    int gray=96;
    setLayout(new BorderLayout());
    setForeground(Color.white);
    setBackground(new Color(gray,gray,gray));
    gray+=32;
    //display
    display = new List(2,false);
    display.add("Observer ImageUpdate() dispayed here");
    add(display, BorderLayout.SOUTH);
    // two panels, one for horizontal, onr for vertical
    Panel panels = new Panel(new GridLayout(1,2));
      Panel left = new Panel(new GridLayout(1,4));
      left.setBackground(Color.black);
      Panel right = new Panel(new GridLayout(4,1));
      right.setBackground(Color.white);

        TrafficLightComponent tnone=new TrafficLightComponent("Vertical GridBagLayout, No size definitions",new Color(gray,gray,gray));
        RainbowComponent rnone = new RainbowComponent("Horizontal GridBagLayout, No size definitions",new Color(gray,gray,gray));
        left.add(tnone);
        right.add(rnone);
        gray+=32;

        TrafficLightComponent tmin=new MinTrafficLight("Vertical GridBagLayout with defined minimum size",new Color(gray,gray,gray));
        RainbowComponent rmin = new MinRainbow("Horizontal GridBagLayout with defined minimum size",new Color(gray,gray,gray));
        left.add(tmin);
        right.add(rmin);
        gray+=32;

        TrafficLightComponent tpref=new PrefTrafficLight("Vertical GridBagLayout with defined preferred size",new Color(gray,gray,gray));
        RainbowComponent rpref = new PrefRainbow("Horizontal GridBagLayout with defined preferred size",new Color(gray,gray,gray));
        left.add(tpref);
        right.add(rpref);
        gray+=32;

        TrafficLightComponent tmax=new MaxTrafficLight("Vertical GridBagLayout with defined maximum size",new Color(gray,gray,gray));
        RainbowComponent rmax = new MaxRainbow("Horizontal GridBagLayout with defined maximum size",new Color(gray,gray,gray));
        left.add(tmax);
        right.add(rmax);
        gray+=32;

      panels.add(left);
      panels.add(right);
    add(panels, BorderLayout.CENTER);
  }

  /****************************************************************/
  /**
  * inner class gridbag component :
  * simulates the gridbag layout as used in the IAA demo ScrollBar showing a red,yellow,green component field
  */
  class TrafficLightComponent extends Panel {
    protected FillComponent red;
    protected FillComponent yellow;
    protected FillComponent green;

    public TrafficLightComponent(String name, Color background) {
      super();
      this.setBackground(background);
      // assign components
      red = new FillComponent(name, LAMPWIDTH, LAMPHEIGHT, Color.red);
      yellow = new FillComponent(name, LAMPWIDTH, LAMPHEIGHT, Color.yellow);
      green = new FillComponent(name, LAMPWIDTH, LAMPHEIGHT, Color.green);
      // lay out components
      this.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      this.add(red, gbc);
      this.add(yellow, gbc);
      this.add(green, gbc);
    }

  }

  class MinTrafficLight extends TrafficLightComponent {
    public MinTrafficLight(String name, Color background) {
      super(name, background);
    }

    public Dimension getMinimumSize() {
      return new Dimension(LAMPWIDTH, LAMPHEIGHT*3);
    }
  }

  class MaxTrafficLight extends TrafficLightComponent {
    public MaxTrafficLight(String name, Color background) {
      super(name, background);
    }

    public Dimension getMaximumSize() {
      return new Dimension(LAMPWIDTH, LAMPHEIGHT*3);
    }
  }

  class PrefTrafficLight extends TrafficLightComponent {
    public PrefTrafficLight(String name, Color background) {
      super(name, background);
    }

    public Dimension getPreferredSize() {
      return new Dimension(LAMPWIDTH, LAMPHEIGHT*3);
    }
  }
  /****************************************************************/
  /**
  * inner class gridbag component :
  * simulates a horizontal gridbag layout as used in the IAA demo ScrollBar showing a red,green, blue component field
  */
  class RainbowComponent extends Panel {
    protected FillComponent red;
    protected FillComponent green;
    protected FillComponent blue;

    /** constructor */
    public RainbowComponent(String name, Color background) {
      super();
      this.setBackground(background);
      // assign components
      red = new FillComponent(name, LAMPWIDTH, LAMPHEIGHT, Color.red);
      green = new FillComponent(name, LAMPWIDTH, LAMPHEIGHT, Color.green);
      blue = new FillComponent(name, LAMPWIDTH, LAMPHEIGHT, Color.blue);
      // lay out components
      this.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      this.add(red, gbc);
      this.add(green, gbc);
      this.add(blue, gbc);
    }

  }

  class MinRainbow extends RainbowComponent {
    public MinRainbow(String name, Color background) {
      super(name, background);
    }

    public Dimension getMinimumSize() {
      return new Dimension(LAMPWIDTH*3, LAMPHEIGHT);
    }
  }

  class MaxRainbow extends RainbowComponent {
    public MaxRainbow(String name, Color background) {
      super(name, background);
    }

    public Dimension getMaximumSize() {
      return new Dimension(LAMPWIDTH*3, LAMPHEIGHT);
    }
  }

  class PrefRainbow extends RainbowComponent {
    public PrefRainbow(String name, Color background) {
      super(name, background);
    }

    public Dimension getPreferredSize() {
      return new Dimension(LAMPWIDTH*3, LAMPHEIGHT);
    }
  }
  /****************************************************************/
  /** inner class display component: a colored box of fixed size that throws a mouse event when clicked*/
  class FillComponent extends Component  {
    private Dimension bounds;
    private Rectangle innerRect;
    String name;
    private Color background;
    /** constructor */
    public FillComponent(String parent, int width, int height, Color back){
      super();
      background =back;
      name = parent;
      this.addMouseListener(new MouseAdapter() {
                                                 public void mousePressed(MouseEvent evt) {
                                                   displayMessage(name);
                                                 }
                                               }
                                             );
      bounds = new Dimension();
      innerRect = new Rectangle(0,0,width-4,height-4);
    }

    /** discrete minimum size */
    public Dimension getMinimumSize() {
      return new Dimension(innerRect.width, innerRect.height);
    }

    /** discrete preferred size */
    public Dimension getPreferredSize() {
      return new Dimension(innerRect.width, innerRect.height);
    }

    /** paint */
    public void paint(Graphics g) {
    	update(g);
    }
      	
    public void update(Graphics g) {
      if(! bounds.equals(this.getSize()) ) {
        bounds.setSize(this.getSize());
        innerRect.setLocation((bounds.width-innerRect.width)/2, (bounds.height-innerRect.height)/2);
      }
      g.setColor(background);
      g.fillRect(1, 1, bounds.width-1, bounds.height-1);
      g.setColor(this.getBackground());
      g.drawRect(innerRect.x+2, innerRect.y+2, innerRect.width-4, innerRect.height-4);
      g.drawLine(innerRect.x, innerRect.y, innerRect.x+innerRect.width, innerRect.y+innerRect.height);
      g.drawLine(innerRect.x, innerRect.y+innerRect.height, innerRect.x+innerRect.width, innerRect.y);
    }
  }

  /****************************************************************/
  /**button pressed : Display event */
  void displayMessage(String text) {
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    System.out.println(text);
    display.add(text,0);
  }

  /****************************************************************/
  /** VisualtestEngine help */
  public String getHelpText(){
    return "A test to verify Rudolph's GridBagLayout implementation\n"+
           "The main screen is devided into a left and a right part. In each part a gridlayout shows four Component elements\n"+
           "These elements consist out of three colored rectangles (graphic Components) either in a vertical or a horizontal row."+
           " The left part of the screen shows a four-in-a-row grid displaying four vertical elements, side by side, the right panel"+
           " shows a four vertical layer grid, every layer showing a horizontal element."+
           "For all four elements of a panel, a different subclass of the elements is used, each defining a minimum, maximum or preferred size."+
           " The defined size is the smallest size showing all three rectangles, ergo rectangle width and three times rectangle height"+
           " for the vertical element and three times rectangle width/one time rectangle height for the horizontal one.\n"+
           "-> for the first element, the elements has NO minimum, maximum or preferred size at all\n"+
           "-> for the second element, the elements has a minimum size defined\n"+
           "-> for the third element, the element has a preferred size defined\n"+
           "-> for the fourth element, the element has a maximum size defined\n"+
           "Clicking on one of the element, shows the type, layout and size definition for that element in the list below";
  }

  /****************************************************************/
  /** standalone main */
  static public void main (String[] args) {
    MinimumSizeGrid tf = new MinimumSizeGrid();
    tf.show();
  }
}
