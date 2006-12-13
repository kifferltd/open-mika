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

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;


public class MinimumSizeBorder extends VisualTestImpl{
  private final static int LAMPWIDTH=20;
  private final static int LAMPHEIGHT=20;

  private List display;

  public MinimumSizeBorder() {
    int gray=32;
    setLayout(new BorderLayout());
    setForeground(Color.white);
    setBackground(new Color(gray,gray,gray));
    gray+=16;
    //display
    display = new List(2,false);
    display.add("Observer ImageUpdate() dispayed here");
    add(display, BorderLayout.SOUTH);
    // two panels, one for horizontal, onr for vertical
    Panel panels = new Panel(new GridLayout(1,4));
      Panel none = new Panel(new BorderLayout());
        gray+=16;
        none.add(new TrafficLightComponent("Vertical layout, No size definitions, EAST",new Color(gray,gray,gray)), BorderLayout.NORTH);
        gray+=16;
        none.add(new TrafficLightComponent("Vertical layout, No size definitions, WEST",new Color(gray,gray,gray)), BorderLayout.WEST);
        gray+=16;
        none.add(new RainbowComponent("horizontal layout, No size definitions, SOUTH",new Color(gray,gray,gray)), BorderLayout.SOUTH);
        gray+=16;
        none.add(new RainbowComponent("horizontal layout, No size definitions, EAST",new Color(gray,gray,gray)), BorderLayout.EAST);
        gray+=16;
      panels.add(none);

      Panel min = new Panel(new BorderLayout());
        gray+=16;
        min.add(new MinTrafficLight("Vertical layout, Minimum size definition, EAST",new Color(gray,gray,gray)), BorderLayout.NORTH);
        gray+=16;
        min.add(new MinTrafficLight("Vertical layout, Minimum size definition, WEST",new Color(gray,gray,gray)), BorderLayout.WEST);
        gray+=16;
        min.add(new MinRainbow("horizontal layout, Minimum size definition, SOUTH",new Color(gray,gray,gray)), BorderLayout.SOUTH);
        gray+=16;
        min.add(new MinRainbow("horizontal layout, Minimum size definition, EAST",new Color(gray,gray,gray)), BorderLayout.EAST);
        gray+=16;
      panels.add(min);

      Panel pref = new Panel(new BorderLayout());
        gray-=16;
        pref.add(new PrefTrafficLight("Vertical layout, Preferred size definition, EAST",new Color(gray,gray,gray)), BorderLayout.NORTH);
        gray-=16;
        pref.add(new PrefTrafficLight("Vertical layout, Preferred size definition, WEST",new Color(gray,gray,gray)), BorderLayout.WEST);
        gray-=16;
        pref.add(new PrefRainbow("horizontal layout, Preferred size definition, SOUTH",new Color(gray,gray,gray)), BorderLayout.SOUTH);
        gray-=16;
        pref.add(new PrefRainbow("horizontal layout, Preferred size definition, EAST",new Color(gray,gray,gray)), BorderLayout.EAST);
        gray-=16;
      panels.add(pref);

      Panel max = new Panel(new BorderLayout());
        gray-=16;
        max.add(new MaxTrafficLight("Vertical layout, Maximum size definition, EAST",new Color(gray,gray,gray)), BorderLayout.NORTH);
        gray-=16;
        max.add(new MaxTrafficLight("Vertical layout, Maximum size definition, WEST",new Color(gray,gray,gray)), BorderLayout.WEST);
        gray-=16;
        max.add(new MaxRainbow("horizontal layout,Maximum  size definition, SOUTH",new Color(gray,gray,gray)), BorderLayout.SOUTH);
        gray-=16;
        max.add(new MaxRainbow("horizontal layout,Maximum  size definition, EAST",new Color(gray,gray,gray)), BorderLayout.EAST);
        gray-=16;
      panels.add(max);
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
    return "A test to verify Rudolph's GridbagLayout implementation\n"+
           "The main screen is devided into four parts, each having a borderlayout to which are added two horizontal and two vertical elements\n"+
           "The horizontal and vertical elements again consist out of three colored rectangles (graphic Components), added to the element"+
           " in a gridbagLayout similar to the layout uded by the IAA demo scrollbar\n"+
           "The four parts of the main screen each have..\n"+
           "-> a vertical element added NORTH (forces the element to be layed out to its minimum height)\n"+
           "-> a vertical element added WEST (allows the element to be layed out using the available spare height)\n"+
           "-> a horizontal element added SOUTH (allows the element to be layed out using the available panel width)\n"+
           "-> a horizontal element added EAST (forces the element to be layed out to its minimum width)\n"+
           "For all four panels, a different subclass of the elements is used, each defining a minimum, maximum or preferred size."+
           " The defined size is the smallest size showing all three rectangles, ergo rectangle width and three times rectangle height"+
           " for the vertical element and three times rectangle width/one time rectangle height for the horizontal one.\n"+
           "-> for the most left panel, the elements have NO minimum, maximum or preferred size at all\n"+
           "-> for the inner left panel, the elements have a minimum size defined\n"+
           "-> for the inner right panel, the elements have a preferred size defined\n"+
           "-> for the outer right panel, the elements have a maximum size defined\n"+
           "Clicking on one of the element, shows the type, layout and size definition for that element in the list below";
  }

  /****************************************************************/
  /** standalone main */
  static public void main (String[] args) {
    MinimumSizeBorder tf = new MinimumSizeBorder();
    tf.show();
  }
}
