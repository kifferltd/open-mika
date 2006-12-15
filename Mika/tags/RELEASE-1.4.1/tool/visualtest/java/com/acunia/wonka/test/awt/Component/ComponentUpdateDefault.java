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


// Author: D. Buytaert
// Created: 2001/05/03

package com.acunia.wonka.test.awt.Component;

import com.acunia.wonka.test.awt.*;
import java.awt.*;
import java.awt.event.*;

public class ComponentUpdateDefault extends VisualTestImpl implements ActionListener {
    /** variables */

  private PaintUpdateComponent paintUpdateComponent;
  private DefaultPaintComponent defaultPaintComponent;
  private DefaultUpdateComponent defaultUpdateComponent;
  private Button repaint;
  List display;

    /** constructor */
  public ComponentUpdateDefault() {
    setLayout(new BorderLayout());
    setBackground(new Color(48,16,16));
    setForeground(Color.white);

    Panel top = new Panel(new FlowLayout() );
        repaint = new Button("repaint()...");
        repaint.addActionListener(this);
      top.add(repaint);
    add(top,BorderLayout.NORTH);

      display = new List(5,false);
    add(display,BorderLayout.SOUTH);

    Panel components = new Panel(new GridLayout(1,3));
        paintUpdateComponent = new PaintUpdateComponent();
      components.add(paintUpdateComponent);
        defaultPaintComponent = new DefaultPaintComponent();
      components.add(defaultPaintComponent);
        defaultUpdateComponent = new DefaultUpdateComponent();
      components.add(defaultUpdateComponent);
    add(components, BorderLayout.CENTER);
  }

  /****************************************************************************************************************************************/
  /**
  * on click repaint
  */
  public void actionPerformed(ActionEvent evt) {
      display.removeAll();
      display.add("Sending repaint()-command without bounds for whole screen");
      paintUpdateComponent.repaint();
      defaultPaintComponent.repaint();
      defaultUpdateComponent.repaint();
  }

  /*********************************************************************************/
  /**  inner class with differenet paint() and update() , based on panel*/
  class PaintUpdateComponent extends Panel {
    protected Rectangle frameRect;
    protected Rectangle bounds;
    protected int updateCount;
    protected int paintCount;

     public PaintUpdateComponent() {
      super();
      updateCount=0;
      paintCount=0;
      frameRect = new Rectangle();
    }

    /** paint */
    public void paint(Graphics g) {
      display.add("Paint & update: Received call to paint()");
      paintCount++;
      paintArea(this.getSize(), "Paint & update", "(painting)", paintCount, updateCount,
                                PaintComponent.DARKCOLORS[(paintCount-1)%PaintComponent.COLORCOUNT], Color.white, g);
      display.add("Paint & update: paint() executed, total paints = "+paintCount);
  	}
    	
    /** update */
  	public void update(Graphics g) {
      display.add("Paint & update: Received call to update()");
      updateCount++;
      paintArea(this.getSize(), "Paint & update", "(updating)", paintCount, updateCount,
                                PaintComponent.LIGHTCOLORS[(updateCount-1)%PaintComponent.COLORCOUNT], Color.black, g);
      display.add("Paint & update: update() executed, total updates = "+updateCount);
    }
    //(end inner class)
  }

  /*********************************************************************************/
  /**  inner class with default implementation of paint()*/
  class DefaultUpdateComponent extends Panel {
    protected Rectangle frameRect;
    protected Rectangle bounds;
    protected int updateCount;
    protected int paintCount;
    private String action;

     public DefaultUpdateComponent() {
      super();
      updateCount=0;
      paintCount=0;
      frameRect = new Rectangle();
      action = "(Painting)";
    }

    /** paint */
    public void paint(Graphics g) {
      display.add("default update: Received call to paint()");
      paintCount++;
      paintArea(this.getSize(), "default update", action, paintCount, updateCount,
                                PaintComponent.DARKCOLORS[(paintCount-1)%PaintComponent.COLORCOUNT], Color.white, g);
      display.add("default update: paint() executed, total paints = "+paintCount);
  	}
    	
    /** update */
  	public void update(Graphics g) {
      display.add("default update: Received call to (super.)update()");
      updateCount++;
      action = "(Updating)";
      super.update(g);
      display.add("default update: update() executed, total updates = "+updateCount);
      action = "(Painting)";
    }
    //(end inner class)
  }

  /*********************************************************************************/
  /**  inner class with default implementation of paint()*/
  class DefaultPaintComponent extends Panel {
    protected Rectangle frameRect;
    protected Rectangle bounds;
    protected int updateCount;
    protected int paintCount;
    private String action;

     public DefaultPaintComponent() {
      super();
      updateCount=0;
      paintCount=0;
      frameRect = new Rectangle();
      action = "(updating)";
    }

    /** paint */
    public void paint(Graphics g) {
      display.add("default paint: Received call to (super.)paint()");
      paintCount++;
      action = "(painting)";
      super.paint(g);
      display.add("default paint: paint() executed, total paints = "+paintCount);
      action = "(updating)";
  	}
    	
    /** update */
  	public void update(Graphics g) {
      display.add("default paint: Received call to update()");
      updateCount++;
      paintArea(this.getSize(), "default Paint", action, paintCount, updateCount,
                                PaintComponent.LIGHTCOLORS[(updateCount-1)%PaintComponent.COLORCOUNT], Color.black, g);
      display.add("default paint: update() executed, total updates = "+updateCount);
    }
    //(end inner class)
  }
  /*********************************************************************************/
  /**  Same for all 3 components */
  void paintArea(Dimension size, String name, String action, int paints, int updates, Color background, Color foreground, Graphics g){
    Rectangle bounds = g.getClipBounds();
    if(bounds==null){
      bounds=new Rectangle(1,1,size.width-3, size.height-3);
      display.add(name+" NULL bounds");
    }
    else {
      display.add(name+" bounds ("+bounds.x+", "+bounds.y+", "+bounds.width+", "+bounds.height+")");
      bounds.grow(-1,-1);
    }

    g.setColor(background);
    g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    g.setColor(foreground);
    g.drawString(name, 10,20);
    g.drawString(action, 10,35);
    g.drawString("paints = "+paints, 10,50);
    g.drawString("updates "+updates, 10,65);
  }
  /****************************************************************************************************************************************/

  public String getHelpText() {
    return "A test on the default implementation of update() and paint() commands\n"+
           " The main part of this test are the three central Components. All components have their paint() and an update() commands"+
           " implemented differently: \n"+
           " => By default, both update() and paint() paint the current clipping bound in a given background color and also display "+
           " the number of paints and updates made. With each paint() or update() the background color changes along the colors"+
           " of the rainbow (red, orange, yellow....). Paint commands display white text on dark colors, update-commands black text on light colors."+
           " Like this it is possible to see wether the clipping area is being paint()-ed or update()-ed \n"+
           "(If the clipping area is <NULL>, it is being replaced by the complete screen)\n\n"+
           " => The left 'paint & update' Component has its paint() and update() functions fully implemented as described above\n"+
           " => The center 'default paint' Component has its update() functin fully implemented, but its paint() function maps to the default Component.paint().\n"+
           " => Likewise the right 'default update' Component has its paint() function implemented but maps its update() function to the default Component.update()";
  }

  public void main(String[] args) {
    Frame main=new Frame("Component paint test");
    main.setSize(400,234);
    main.add(new ComponentUpdate());
    main.show();
  }


}
