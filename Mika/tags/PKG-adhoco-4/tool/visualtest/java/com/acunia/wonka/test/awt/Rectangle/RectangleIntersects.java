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


public class RectangleIntersects extends VisualTestImpl implements MouseListener, MouseMotionListener {
  /** Variables */
  Rectangle target1;
  Rectangle target2;
  Rectangle union;
  Rectangle intersection;
  Rectangle bounds;
  Rectangle current;
  Point lastClick;

  /*****************************************************************/
  /** Constructor
  */
  public RectangleIntersects() {
    super();
    addMouseListener(this);
    addMouseMotionListener(this);

    target1 = new Rectangle();
    target2 = new Rectangle();
    union = new Rectangle();
    intersection = new Rectangle();
    bounds = new Rectangle();
    lastClick = new Point();
    current = null;
  }

  /*****************************************************************/
  /** visualTest implementations
  */
  public String getHelpText(){
    return "Tests the functions Rectangle.union(Rectangle), Rectangle.intersects(Rectangle) and Rectangle.intersection(Rectangle)\n"+
           "\nOn startup, the screen shows a blue field and two green rectangles: a smaller one in the topleft corner"+
           " and a bigger one in the center A white rectangular frame surround them"+
           " Issues to test : \n"+
           " => Rectangle.union(Rectangle):click on one of the rectangles and then drag the mouse to move it. The white frame should change"+
           " in size such that it forms the 'union' of the two rectangles: the smallest rectangle that contains both rectangles\n"+
           " => Rectangle.intersection(dx,dy): when one rectangle moves over the other, the test will calculate the intersecting area"+
           " of the two rectangles and paint it in yellow"+
           " => Rectangle.intersects(): when <intersects()> detects that the two rectangles overlap, (regardless of the intersection shown)."+
           " the frame will change its color from white to cyan";
  }
     	
  public java.awt.Panel getPanel(VisualTester vt){
    return this;
  }
     	
  public String getLogInfo(java.awt.Panel p, boolean b) {
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
    if(target2.contains(event.getPoint())){
      current = target2;
      lastClick.setLocation(event.getPoint());
      repaint();
    }
    else if(target1.contains(event.getPoint())){
      current = target1;
      lastClick.setLocation(event.getPoint());
      repaint();
    }
  }

  public void mouseReleased(MouseEvent event) {
    if(current != null) {
      current = null;
      repaint();
    }
  }

  public void mouseDragged(MouseEvent event) {
    int dx = event.getX()-lastClick.x;
    int dy = event.getY()-lastClick.y;
    if(bounds.contains(current.x+dx, current.y+dy) && bounds.contains(current.x+current.width+dx, current.y+current.height+dy)) {
      current.translate(dx,dy);
      union = target1.union(target2);
      intersection = target1.intersection(target2);
      repaint();
    }
    lastClick.setLocation(event.getPoint());
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
      target1.setBounds( 5+bounds.width/3, 5+bounds.height/3, bounds.width/3, bounds.height/3);
      target2.setBounds( 5,5, bounds.width/5, bounds.height/5);
      union = target1.union(target2);
      // intersection = target1.intersection(target2);
    }
    g.setColor(Color.blue);
    g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    g.setColor((current == target1)? Color.red: Color.green);
    g.fillRect(target1.x, target1.y, target1.width, target1.height);
    g.setColor((current == target2)? Color.red: Color.green);
    g.fillRect(target2.x, target2.y, target2.width, target2.height);
    g.setColor((target1.intersects(target2))?Color.cyan:Color.white);
    g.drawRect(union.x, union.y, union.width, union.height);
    if(!intersection.isEmpty()){
      g.setColor(Color.yellow);
      g.fillRect(intersection.x, intersection.y, intersection.width, intersection.height);
    }
  }

  /** paint */
	public void paint(Graphics g) {
	  update(g);
	}


  static public void main (String[] args) {
    new RectangleIntersects();
  }
}
