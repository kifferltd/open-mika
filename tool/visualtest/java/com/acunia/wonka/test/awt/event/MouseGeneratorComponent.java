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


// Author: N. Oberfeld
// Created: 2001/09/26

package com.acunia.wonka.test.awt.event;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/****************************************************************************************/
/**
* Mouse event tracking and generating component used by various mouse listener classes
*/
/****************************************************************************************/
public class MouseGeneratorComponent extends Component implements MouseListener, MouseMotionListener, CollectsEvents {

  /** variables */
  final static String[] colornames = {"Purple","Indigo", "Lightblue", "Green", "Yellow",  "Orange", "Red"};
  final static Color[] colors = {new Color(128,0,128), new Color(0,0,128), new Color(96,96,255),
                                 Color.green, Color.yellow, Color.orange,Color.red};

  final static int GRIDWIDTH  = 15; // pixels per grid horizontally
  final static int GRIDHEIGHT = 15; // pixels per grid vertically

  final static Color COPPER = new Color(200,200,120);
  final static Color DARKSAND=new Color(96,96,48);
  final static Color DUSTGREEN=new Color(128,192,128);
  final static Color DARKGREEN=new Color(64,96,64);

  protected int currentColor;
  protected Color background;
  protected Color foreground;
  protected  String name;

  protected Point screenPoint;
  protected Point mousePoint;
  protected Point gridPoint;
  protected Dimension frame;
  protected Point offset;
  protected Point bounds;
  protected Rectangle inside;
  protected boolean connected;

  protected CollectsEvents parent;

  /************************************************************************************************************/
  /** constructor
  */
  public MouseGeneratorComponent(String componentname, Color back, Color front, CollectsEvents parentinstance) {
    super();

    //layout
    name = componentname;
    background = back;
    foreground = front;

    //points & rectangles
    screenPoint = new Point();
    mousePoint = new Point();
    gridPoint = new Point();
    offset = new Point();
    bounds = new Point();
    frame = new Dimension();
    inside = new Rectangle();
    connected=false;

    currentColor=colors.length-1;

    //parent
    parent = parentinstance;

  }

  /************************************************************************************************************/
  /** constructor short form
  */
  public MouseGeneratorComponent(String componentname, CollectsEvents parentinstance) {
    this(componentname, Color.white, Color.black, parentinstance);
  }

  /************************************************************************************************************/
  /** Sizes
  */
  public Dimension getMinimumSize() {
    return new Dimension(70,50);
  }

  public Dimension getPreferredSize() {
    return new Dimension(70,50);
  }

  /************************************************************************************************************/
  /** screen variables
  */
  private void calculateGrid() {
    //frame.setSize(getSize());
    offset.setLocation(frame.width/2, frame.height/2);
    frame.width-=2;
    frame.height-=2;
    bounds.setLocation((offset.x-5)/GRIDWIDTH, (offset.y-5)/GRIDHEIGHT);
    inside.setBounds(offset.x-bounds.x*GRIDWIDTH, offset.y-bounds.y*GRIDHEIGHT, bounds.x*GRIDWIDTH*2, bounds.y*GRIDHEIGHT*2);
    screenPoint.setLocation(offset);
    gridPoint.setLocation(0,0);
  }
  /************************************************************************************************************/
  /** mouse position to grid position (the mouse listeners themselces are added from the main test class
  * in a line ourMouseGenerator.addMouseListener(ourMouseGenerator). Like this, we can select on the spot to
  * which events the listener wil react, and who will catch it.
  */
  protected Point screenToGrid(Point screen) {
    Point grid = new Point();
    if(screen.x<inside.x){
      grid.x=-bounds.x;
    }
    else if(screen.x<offset.x) {
      grid.x = (screen.x-offset.x-GRIDWIDTH/2)/GRIDWIDTH;
    }
    else if(screen.x <(inside.x+inside.width)) {
      grid.x=(screen.x-offset.x+GRIDWIDTH/2)/GRIDWIDTH;
    }
    else {
      grid.x=bounds.x;
    }

    if(screen.y<inside.y){
      grid.y=-bounds.y;
    }
    else if(screen.y<offset.y) {
      grid.y = (screen.y-offset.y-GRIDHEIGHT/2)/GRIDHEIGHT;
    }
    else if(screen.y <(inside.y+inside.height)) {
      grid.y=(screen.y-offset.y+GRIDHEIGHT/2)/GRIDHEIGHT;
    }
    else {
      grid.y=bounds.y;
    }

    return grid;
  }

  /************************************************************************************************************/
  /** grid position to mouse position
  */
  protected Point gridToScreen(Point grid) {
    return new Point(offset.x + grid.x*GRIDWIDTH, offset.y + grid.y*GRIDHEIGHT);
  }
  /************************************************************************************************************/
  /** mouse events
  */
  protected void mouseAction(MouseEvent event) {
    parent.displayMessage(displayMouseEvent(event,name));
    gridPoint = screenToGrid(event.getPoint());
    screenPoint = gridToScreen(gridPoint);
    mousePoint = event.getPoint();
  }

  public void mouseClicked(MouseEvent event) {
    parent.displayMessage(displayMouseEvent(event,name));
    if(currentColor>0) {
      currentColor--;
    }
    else{
      currentColor=colors.length-1;
    }
    repaint();
  }

  public void mouseEntered(MouseEvent event) {
    mouseAction(event);
    connected=false;
    repaint();
  }

  public void mouseExited(MouseEvent event) {
    mouseAction(event);
    connected=true;
    repaint();
  }

  public void mousePressed(MouseEvent event) {
    mouseAction(event);
    connected=true;
    repaint();
  }

  public void mouseReleased(MouseEvent event) {
    mouseAction(event);
    connected=false;
    repaint();
  }

  public void mouseDragged(MouseEvent event) {
    mouseAction(event);
    connected=true;
    repaint();
  }

  public void mouseMoved(MouseEvent event) {
    mouseAction(event);
    connected=false;
    repaint();
  }

  /** paint */
  public void paint(Graphics g) {

  	update(g);
  }
    	
  public void update(Graphics g) {
    int x0,x1,y0,y1;
    if(frame.width==0 && frame.height==0) {
      frame.setSize(getSize());
      calculateGrid();
    }
    //clear background
    g.setColor(background);
    g.fillRect(1,1, frame.width, frame.height);

    g.setColor(foreground);
    //frame
    g.drawRect(inside.x, inside.y, inside.width, inside.height);
    g.drawString(name,5,20);
    //axis
    g.drawLine(inside.x,offset.y, (inside.x+inside.width), offset.y);
    g.drawLine(offset.x,inside.y, offset.x, (inside.y+inside.height));
    if(connected) {
      g.drawLine(screenPoint.x, screenPoint.y,  mousePoint.x, mousePoint.y);
    }

    g.setColor(colors[currentColor]);
    //horizontal grid tags
    x0=offset.x;
    x1=offset.x;
    y0=offset.y-5;
    y1=offset.y+5;
    for(int i=1; i<bounds.x; i++) {
      x0+=GRIDWIDTH;
      g.drawLine(x0,y0, x0,y1);
      x1-=GRIDWIDTH;
      g.drawLine(x1,y0, x1,y1);
    }
    //vertical grid tags
    x0=offset.x-5;
    x1=offset.x+5;
    y0=offset.y;
    y1=offset.y;
    for(int i=1; i<bounds.y; i++) {
      y0+=GRIDHEIGHT;
      g.drawLine(x0,y0, x1,y0);
      y1-=GRIDHEIGHT;
      g.drawLine(x0,y1, x1,y1);
    }
    // screen point
    g.drawLine(screenPoint.x+3, screenPoint.y, screenPoint.x, screenPoint.y-3);
    g.drawLine(screenPoint.x+3, screenPoint.y, screenPoint.x, screenPoint.y+3);
    g.drawLine(screenPoint.x-3, screenPoint.y, screenPoint.x, screenPoint.y-3);
    g.drawLine(screenPoint.x-3, screenPoint.y, screenPoint.x, screenPoint.y+3);
    // grid
    if(connected) {
      g.drawLine(screenPoint.x, offset.y,  screenPoint.x, screenPoint.y);
      g.drawLine(offset.x, screenPoint.y,  screenPoint.x, screenPoint.y);
    }
  }
  /************************************************************************************************************/
  /** CollectsEvent interface display messages : do nothing, we only display our own messages
  */
  public void displayMessage(String[] messagestrings) {
  }

  /************************************************************************************************************/
  /** CollectsEvent interface help : describe the behavior to all calling CollectsEvent  classes
  */
  public String getHelpText() {
    return getHelpText(true,true);
  }

  public String getHelpText(boolean tracksmouse, boolean tracksmotions) {
    String display= "=> the test shows an x/y grid with ruler marks on it. Next to this it shows a 'point', currently at the center of the axis  \n";
    if(tracksmouse) {
      display += "\nReaction to the mouse events:\n"+
      "=> Mouse pressed: the point is set to the nearest grid intersection. the point is drawn in the current drawing color. Also drawn are grid lines"+
      " from the point to the horizontal and vertical axis.\n"+
      "    Furthermore, a line is drawn between the 'grid' position of the point and the actual mouse position\n"+
      "=> Mouse released: the point is set to the nearest grid intersection, no lines are drawn\n"+
      "=> Mouse clicked: the drawing color shifts to the next color of the rainbow\n"+
      "=> mouse entered: the point is moved to the nearest intersection\n"+
      "=> mouse exited: the point is moved to the nearest intersection, grid lines are shown and a line is drawn from the point to the actual mouse position";
    }
    if(tracksmotions) {
      display += "\nReaction to the mouse motion events:\n"+
      "=> Mouse moved: the point should swap to the next grid intersection as soon as the mouse position is nearer to that position as to the current\n"+
      "=> Mouse dragged: a line is drawn to the mouse position, as soon as the position is nearer to the next grid, the point is moved to that grid"+
      " furthermore, grid lines are drawn to the axes ";
    }

    return display;
  }
  /************************************************************************************************************/
  /** toString() for display of MouseEvent.getSource().getString();
  */
  public String toString() {
    return name;
  }

  /***************************************************************************************************/
  /**
  * MouseEvent analysis
  * This is a static function, so all classes throwing mouse events can access this function
  * The analysis is returned to the caller as a 4-string array of form:
  * String 0: source (can be overwritten)
  * String 1: stamp time and event ID
  * String 2: position and clicks
  * String 3: modifiers and popup trigger
  *
  */
  static String[] displayMouseEvent(MouseEvent evt) {
    String[] data = new String[4];
    data[0] = "Generated by "+evt.getSource(); //evt.getSource().toString();
    int id = evt.getID();

    data[1] = "at "+evt.getWhen()+" action "+id;
    if(id == MouseEvent.MOUSE_ENTERED) {
      data[1]+=" : <Mouse entered>";
    }
    else if(id == MouseEvent.MOUSE_EXITED) {
      data[1]+=" : <Mouse exited>";
    }
    else if(id == MouseEvent.MOUSE_PRESSED) {
      data[1]+=" : <Mouse pressed>";
    }
    else if(id == MouseEvent.MOUSE_RELEASED) {
      data[1]+=" : <Mouse released>";
    }
    else if(id == MouseEvent.MOUSE_CLICKED) {
      data[1]+=" : <Mouse clicked>";
    }
    else if(id == MouseEvent.MOUSE_MOVED) {
      data[1]+=" : <Mouse moved>";
    }
    else if(id == MouseEvent.MOUSE_DRAGGED) {
      data[1]+=" : <Mouse dragged>";
    }
    else {
      data[1]+=" : UNKNOWN ID";
    }

    data[2] = "At position ("+evt.getX()+","+evt.getY()+") clicked "+evt.getClickCount()+" times";

    id = evt.getModifiers();
    data[3] = "Mouse modifiers = ("+id+") :" ;
    if((id & (InputEvent.BUTTON1_MASK + InputEvent.BUTTON2_MASK + InputEvent.BUTTON3_MASK))==0){
      data[3]+=" No Button detected";
    }
    if((id & InputEvent.BUTTON1_MASK)!=0){
      data[3]+=" Button1(left)";
    }
    if((id & InputEvent.BUTTON2_MASK)!=0){
      data[3]+=" Button2(right)";
    }
    if((id & InputEvent.BUTTON3_MASK)!=0){
      data[3]+=" Button3(middle)";
    }

    if(evt.isPopupTrigger()) {
      data[3]+=" ,Event is popup trigger";
    }
    else {
      data[3]+=" ,Event is no popup trigger";
    }
    return data;
  }

  /************************************************************************************************************/
  /**
  * extended MouseEvent analysis: on the first line, also adds a reference to the application that catches the event
  */
  static String[] displayMouseEvent(MouseEvent evt, String catcher) {
    String[] data = displayMouseEvent(evt);
    data[0]+=" catched by "+catcher;
    return data;
  }
}
