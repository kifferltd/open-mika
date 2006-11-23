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

public class ComponentUpdateBounds extends VisualTestImpl implements ActionListener {
    /** variables */

  private InnerComponent innerComponent;
  private InnerPaintComponent paintComponent;
  private InnerBoundsComponent boundsComponent;
  private Button repaintAll;
  private Button repaintBounds;
  List display;

    /** constructor */
  public ComponentUpdateBounds() {
    setLayout(new BorderLayout());
    setBackground(new Color(64,64,96));
    setForeground(Color.white);

    Panel top = new Panel(new FlowLayout() );
        repaintAll = new Button("repaint() all");
        repaintAll.addActionListener(this);
      top.add(repaintAll);
      top.add(new Label("   ")); //placeholder
        repaintBounds = new Button("repaint(x,y,w,h) selection");
        repaintBounds.addActionListener(this);
      top.add(repaintBounds);

    add(top,BorderLayout.NORTH);

      display = new List(5,false);
      display.add("Use your mouse to select an update area in the center panel");
      display.add("Press <repaint all> to repaint the left and right screen completely");
      display.add("Press <repaint selection> to repaint the selected area");
      display.add("  on the left and right screen");
    add(display,BorderLayout.SOUTH);

    Panel components = new Panel(new GridLayout(1,3));
        innerComponent = new InnerComponent();
      components.add(innerComponent);
        boundsComponent = new InnerBoundsComponent(new Color(96,96,144));
      components.add(boundsComponent);
        paintComponent = new InnerPaintComponent();
      components.add(paintComponent);
    add(components, BorderLayout.CENTER);
  }

  /****************************************************************/
  /**
  * on click repaint
  */
  public void actionPerformed(ActionEvent evt) {
    display.removeAll();
    if(evt.getSource() == repaintAll){
      display.add("Sending repaint()-command without bounds for whole screen");
      innerComponent.repaint();
      paintComponent.repaint();
    }
    else {
      Rectangle selection = boundsComponent.getSelection();
      display.add("Sending repaint(bounds) for bounds("+selection.x+", "+selection.y+", "+selection.width+", "+selection.height+")");
      innerComponent.repaint(selection.x, selection.y, selection.width, selection.height);
      paintComponent.repaint(selection.x, selection.y, selection.width, selection.height);
    }
  }

  /****************************************************************/
  /**  inner class with painting and mouse movements , based on panel*/
  class InnerPaintComponent extends PaintComponent {
    private Rectangle bounds;

    /** constructor */
    public InnerPaintComponent() {
      super("extern PaintComponent",display);
    }

    /** common command for Paint() and update() */
    protected boolean paintArea(Color background, Color foreground, Graphics g){
      bounds = g.getClipBounds();
      boolean calculated = false;
      if(frameRect.width<=0){
        frameRect.setBounds(0,0,this.getSize().width, this.getSize().height);
        calculated=true;
      }
      if(bounds==null){
        bounds = frameRect;
        display.add("NULL bounds: Reverting to full screen (0, 0, "+bounds.width+", "+bounds.height+")");
      }
      else {
        display.add("bounds ("+bounds.x+", "+bounds.y+", "+bounds.width+", "+bounds.height+")");
      }

      g.setColor(background);
      g.fillRect(5,5, frameRect.width-10, frameRect.height-10);
      g.setColor(foreground);
      g.drawString("extern PaintComponent:", 10,20);
      g.drawString("updates "+updateCount, 10,35);
      g.drawString("paints = "+paintCount, 10,50);
      return calculated;
    }
    //(end inner class)
  }
  /****************************************************************/
  /**  inner class with painting and mouse movements , based on panel*/
  class InnerComponent extends Panel {
    protected Rectangle bounds;
    protected int updateCount;
    protected int paintCount;
    protected Rectangle frameRect;

     public InnerComponent() {
      super();
      updateCount=0;
      paintCount=0;
      frameRect = new Rectangle();
    }

    /** paint */
    public void paint(Graphics g) {
      display.add("Inner class: Received call to paint()");
      paintCount++;
      if(paintArea(PaintComponent.DARKCOLORS[(paintCount-1)%PaintComponent.COLORCOUNT], Color.white, g) ) {
        display.add("Inner class: on paint() calculated sizes");
      }
      display.add("Inner class: paint() executed, total paints = "+paintCount);
  	}
    	
    /** update */
  	public void update(Graphics g) {
      display.add("Inner class: Received call to update()");
      updateCount++;
      if(paintArea(PaintComponent.LIGHTCOLORS[(updateCount-1)%PaintComponent.COLORCOUNT], Color.black, g) ) {
        display.add("Inner class: on update() calculated sizes");
      }
      display.add("Inner class: update() executed, total updates = "+updateCount);
    }

    private boolean paintArea(Color background, Color foreground, Graphics g){
      bounds = g.getClipBounds();
      boolean calculated = false;
      if(frameRect.width<=0){
        frameRect.setBounds(0,0,this.getSize().width, this.getSize().height);
        calculated=true;
      }
      if(bounds==null){
        bounds = frameRect;
        display.add("NULL bounds: Reverting to full screen (0, 0, "+bounds.width+", "+bounds.height+")");
      }
      else {
        display.add("bounds ("+bounds.x+", "+bounds.y+", "+bounds.width+", "+bounds.height+")");
      }

      g.setColor(background);
      g.fillRect(5,5, frameRect.width-10, frameRect.height-10);
      g.setColor(foreground);
      g.drawString("inner Component:", 10,20);
      g.drawString("updates "+updateCount, 10,35);
      g.drawString("paints = "+paintCount, 10,50);
      return calculated;
    }
    //(end inner class)
  }

  /****************************************************************/
  /**  inner class to get a series of bounds*/
  class InnerBoundsComponent extends Component implements MouseListener, MouseMotionListener {
    Rectangle frame;
    Rectangle bounds;
    Color background;
    int startX;
    int startY;
    int mouseX;
    int mouseY;

    /** constructor: */
    public InnerBoundsComponent(Color background){
      this.background = background;
      this.frame = new Rectangle();
      this.bounds = new Rectangle();
      this.addMouseListener(this);
      this.addMouseMotionListener(this);
    }

    /** Mouse events :
    * => on mouse pressed: new boundaries starting point
    * => on mouse dragged: new boundaries ending point
    */
    public void mouseEntered(MouseEvent evt) { }
    public void mouseExited(MouseEvent evt) { }
    public void mousePressed(MouseEvent evt) {
      startX = evt.getX();
      startY = evt.getY();
      mouseX = startX;
      mouseY = startY;
      bounds.setBounds(mouseX, mouseY, 0, 0);
      this.repaint();
    }

    public void mouseReleased(MouseEvent evt) { }
    public void mouseClicked(MouseEvent evt) { }
    public void mouseMoved(MouseEvent evt) { }
    public void mouseDragged(MouseEvent evt) {
      mouseX = evt.getX();
      mouseY = evt.getY();
      if(mouseX>startX) {
        bounds.x = startX;
        bounds.width = mouseX-startX;
      }
      else {
        bounds.x = mouseX;
        bounds.width = startX - mouseX;
      }
      if(mouseY>startY) {
        bounds.y = startY;
        bounds.height = mouseY-startY;
      }
      else {
        bounds.y = mouseY;
        bounds.height = startY - mouseY;
      }
      this.repaint();
    }

    /** return selection bounds: */
    public Rectangle getSelection() {
      return bounds;
    }

    /** screen paint: */
    public void paint(Graphics g) {
      this.update(g);
    }

    public void update(Graphics g) {
      // on first time paint calculate sizes
      if(frame.width <=0) {
        frame.setBounds(1, 1, this.getSize().width-2, this.getSize().height-2);
        bounds.setBounds(frame.width/3, frame.height/3, frame.width/3, frame.height/3);
      }
      // clear screen
      g.setColor(background);
      g.fillRect(1,1, frame.width, frame.height);
      g.setColor(Color.white);
      g.drawRect(1,1, frame.width, frame.height);
      // paint bounds
      g.setColor(Color.red);
      g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

  }

  /****************************************************************************************************************************************/
  public String getHelpText() {
    return "A test on passing the boundaries in a Component.repaint(x,y,w,h) command\n"+
           " The main part of this test are the two central Components. To refresh the content of the components, a paint() and an update()"+
           " function are implemented, both painting the current clipping bound in a background color and also displaying the number"+
           " of paints and updates made:\n"+
           "With each paint() or update() the background color changes along the colors of the rainbow (red, orange, yellow....)"+
           " Paint commands display white text on dark colors, update-commands black text on light colors. Like this it is possible to see"+
           " wether the clipping area is being paint()-ed or update()-ed \n"+
           "(If the clipping bound is <NULL>, it is being replaced by the complete screen)\n\n"+
           "The <repaint()> button sends an empty repaint() command to both components. the <repaint(x,y,w,h)> sends the special form to"+
           " repaint only a selected area. You can specify the area that has to be overpainted in the center panel using your mouse.\n"+
           "There are two display component. One derived from Component, the other from an extern class PaintComponent. For some reason the Sun"+
           "sdk always sends a call tp paint() to the external PaintComponent, even on a call to repaint() when the inner component receives"+
           " a call to update()";
  }

  public void main(String[] args) {
    Frame main=new Frame("Component paint test");
    main.setSize(400,234);
    main.add(new ComponentUpdate());
    main.show();
  }


}
