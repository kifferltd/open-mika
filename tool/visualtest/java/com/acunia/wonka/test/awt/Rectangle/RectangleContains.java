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
// Created: 2001/05/02

package com.acunia.wonka.test.awt.Rectangle;

import com.acunia.wonka.test.awt.*;
import java.awt.*;
import java.awt.event.*;


public class RectangleContains extends VisualTestImpl implements MouseListener, MouseMotionListener {
  /** Variables */
  Rectangle target;
  Rectangle surround;
  Rectangle bounds;
  Point lastClick;
  boolean inside;
  /*****************************************************************/
  /** Constructor
  */
  public RectangleContains() {
    super();
    addMouseListener(this);
    addMouseMotionListener(this);

    target = new Rectangle();
    surround = new Rectangle();
    bounds = new Rectangle();
    lastClick = new Point();
    inside = false;
  }

  /*****************************************************************/
  /** visualTest implementations
  */
  public String getHelpText(){
    return "Tests the functions Rectangle.Contains(Point), Rectangle.contains(Rectangle), Rectangle.translate(dx,dy) and Rectangle.add(Point)\n"+
           "\nOn startup, the screen shows a blue field and a green rectangle with a white border. Click and drag the mouse and compare"+
           " the responses on the screen to the expected behavior : \n"+
           " => Rectangle.contains(Point): when pressing the mouse button inside the rectangle, the rectangle should change color (to red)\n"+
           " => Rectangle.translate(dx,dy): when pressing the mouse button inside the rectangle and then dragging it, the rectangle should follow the movements of the mouse.\n"+
           " => Rectangle.contains(Rectangle): On correct behavior of this function, it should be impossible to drag the rectangle out of the blue field\n"+
           " => Rectangle.add(Point): when pressing the mouse button outside of the rectangle, the white frame will be enlarged so that it contains"+
           " the mouse position. Dragging the mouse, the frame will grow continuously to keep the mouse inside. (Note that it will only grow, not schrink)";
  }
     	
  public java.awt.Panel getPanel(VisualTester vt){
    return this;
  }
     	
  public String getLogInfo(java.awt.Panel p, boolean b){
    return "no logging info !";
  }
  public void start(java.awt.Panel p, boolean b){
  }

  public void stop(java.awt.Panel p){}


  public void showTest(){
  }

  public void hideTest(){
  }

  /*****************************************************************/
  /** Mouse functions
  */
  public void mouseClicked(MouseEvent event) {
  }

  public void mouseEntered(MouseEvent event) {
  }

  public void mouseExited(MouseEvent event) {
  }

  public void mousePressed(MouseEvent event) {
    if(target.contains(event.getPoint())){
      inside = true;
      lastClick.setLocation(event.getPoint());
    }
    else {
      surround.add(event.getPoint());
    }
    repaint();
  }

  public void mouseReleased(MouseEvent event) {
    if(bounds.contains(target)) {
      surround.setBounds(target);
    }
    else {
      target.setBounds(surround);
    }
    inside = false;
    repaint();
  }

  public void mouseDragged(MouseEvent event) {
    if(inside) {
      target.translate(event.getX()-lastClick.x, event.getY() - lastClick.y);
      if(bounds.contains(target)) {
        surround.setBounds(target);
        lastClick.setLocation(event.getPoint());
      }
      else {
        target.setBounds(surround);
      }
      repaint();
    }
    else {
      surround.add(event.getPoint());
    }
    repaint();
  }

  public void mouseMoved(MouseEvent event) {
  }
  /*****************************************************************/
  /** Painting function
  */
	public void update(Graphics g) {
    // first time initialiser
    if(bounds.width==0 ){
      bounds.setBounds(5,5, getSize().width-10, getSize().height-10);
      target.setBounds( 5+2*bounds.width/5, 5+2*bounds.height/5, bounds.width/5, bounds.height/5);
      surround.setBounds(target);
    }
    g.setColor(Color.blue);
    g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    g.setColor((inside)? Color.red: Color.green);
    g.fillRect(target.x, target.y, target.width, target.height);
    g.setColor(Color.white);
    g.drawRect(surround.x, surround.y, surround.width, surround.height);
  }

  /** paint */
	public void paint(Graphics g) {
	  update(g);
	}


  static public void main (String[] args) {
    new RectangleContains();
  }
}
