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


public class BorderSizeDefs extends VisualTestImpl {
  /** variables */
  private final static int LAMPWIDTH=20;
  private final static int LAMPHEIGHT=20;

  private List display;

  /****************************************************************/
  /** Constructor */
  public BorderSizeDefs() {
    int gray=48;
    setLayout(new java.awt.BorderLayout());
    setForeground(Color.white);
    setBackground(new Color(gray,gray,gray));
    gray+=32;
    //display
    display = new List(2,false);
    display.add("Observer ImageUpdate() dispayed here");
    add(display, java.awt.BorderLayout.SOUTH);
    // border layouts:
    Panel layouts = new Panel(new java.awt.GridLayout(1,4) );
      Panel none = new Panel(new java.awt.BorderLayout() );
      none.setBackground(Color.white);
        none.add(new FillComponent("No Size definitions, NORTH", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.NORTH);
        gray+=16;
        none.add(new FillComponent("No Size definitions, SOUTH", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.SOUTH);
        gray+=16;
        none.add(new FillComponent("No Size definitions, EAST", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.EAST);
        gray+=16;
        none.add(new FillComponent("No Size definitions, WEST", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.WEST);
        gray+=16;
        none.add(new FillComponent("No Size definitions, CENTER", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.CENTER);
        gray+=16;
      layouts.add(none);

      Panel min = new Panel(new java.awt.BorderLayout() );
      min.setBackground(Color.black);
        min.add(new MinFillComponent("Minimum size defined, NORTH", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.NORTH);
        gray+=16;
        min.add(new MinFillComponent("Minimum size defined, SOUTH", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.SOUTH);
        gray+=16;
        min.add(new MinFillComponent("Minimum size defined, EAST", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.EAST);
        gray+=16;
        min.add(new MinFillComponent("Minimum size defined, WEST", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.WEST);
        gray+=16;
        min.add(new MinFillComponent("Minimum size defined, CENTER", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.CENTER);
        gray+=16;
      layouts.add(min);

      Panel pref = new Panel(new java.awt.BorderLayout() );
      pref.setBackground(Color.black);
        pref.add(new PrefFillComponent("Preferred size defined, NORTH", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.NORTH);
        gray-=16;
        pref.add(new PrefFillComponent("Preferred size defined, SOUTH", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.SOUTH);
        gray-=16;
        pref.add(new PrefFillComponent("Preferred size defined, EAST", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.EAST);
        gray-=16;
        pref.add(new PrefFillComponent("Preferred size defined, WEST", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.WEST);
        gray-=16;
        pref.add(new PrefFillComponent("Preferred size defined, CENTER", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.CENTER);
        gray-=16;
      layouts.add(pref);

      Panel max = new Panel(new java.awt.BorderLayout() );
      max.setBackground(Color.white);
        max.add(new MaxFillComponent("Maximum size defined, NORTH", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.NORTH);
        gray-=16;
        max.add(new MaxFillComponent("Maximum size defined,SOUTH ", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.SOUTH);
        gray-=16;
        max.add(new MaxFillComponent("Maximum size defined, EAST", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.EAST);
        gray-=16;
        max.add(new MaxFillComponent("Maximum size defined, WEST", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.WEST);
        gray-=16;
        max.add(new MaxFillComponent("Maximum size defined, CENTER", LAMPWIDTH, LAMPHEIGHT, new Color(gray,gray,gray)), java.awt.BorderLayout.CENTER);
        gray-=16;
      layouts.add(max);
    add(layouts, java.awt.BorderLayout.CENTER);

  }



  /****************************************************************/
  /** inner class display component: a colored box that throws a mouse event when clicked*/
  class FillComponent extends Component  {
    private Dimension bounds;
    protected Rectangle innerRect;
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
      innerRect = new Rectangle(0,0,width,height);
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
  /** With minimum size function*/
  class MinFillComponent extends FillComponent  {
    public MinFillComponent(String parent, int width, int height, Color back){
      super(parent, width, height, back);
    }

    /** discrete minimum size */
    public Dimension getMinimumSize() {
      return new Dimension(innerRect.width, innerRect.height);
    }

  }

  /****************************************************************/
  /** With preferred size function*/
  class PrefFillComponent extends FillComponent  {
    public PrefFillComponent(String parent, int width, int height, Color back){
      super(parent, width, height, back);
    }

    /** discrete preferred size */
    public Dimension getPreferredSize() {
      return new Dimension(innerRect.width, innerRect.height);
    }

  }

  /****************************************************************/
  /** With maximum size function*/
  class MaxFillComponent extends FillComponent  {
    public MaxFillComponent(String parent, int width, int height, Color back){
      super(parent, width, height, back);
    }

    /** discrete Maximum size */
    public Dimension getMaximumSize() {
      return new Dimension(innerRect.width, innerRect.height);
    }

  }
  /****************************************************************/
  /** button pressed : Display event */
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
    return "WHAT IT DOES: \n"+
           " The screen is devided into four panels, onto each panel are added five components respectingly on the locations"+
           " BorderLayout.NORTH, BorderLayout.SOUTH, BorderLayout.EAST, BorderLayout.WEST and BorderLayout.CENTER\n"+
           "In the first panel, the components are defines as-is without defiinitions of their minimum, maximum or preferred size"+
           "In the second panel, each component has a minimum size : Component.getMinimumSize() for that panel"+
           " returns the size of the inner rectangle drawn in that component.\n"+
           "In the third panel, for each of the components, Component.getPreferredSize() returns the size of the inner rectangle for that component\n"+
           "In the fourth panel, each component returns the size of the inner rectangle as Component.getMaximumSize()\n"+
           "\nWHAT YOU SHOULD SEE:\n"+
           "In the first , second and fourth panel, as no preferred sizes are defined, the north, south east and west components are considered"+
           " of having a size zero, and therefore are not drawn. The center component occupies all remaining space: in this case the complete panel\n"+
           "The third panel should show a complete BorderLayout figure with the north and south component exactly as high as their inner rectangle"+
           " and the east and west conponents just as wide as theirs";
  }
}
