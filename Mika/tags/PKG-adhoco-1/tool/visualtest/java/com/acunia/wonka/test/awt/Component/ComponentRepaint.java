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

public class ComponentRepaint extends VisualTestImpl implements ItemListener {
    /** variables */

  private InnerMouseComponent mouseComponent;
  private InnerMouseContainer mouseContainer;
  private Button update;
  private Button noupdate;
  private Checkbox showOnButton;
  final static Color[] colors = {new Color(128,0,128), new Color(0,0,128), new Color(96,96,255), Color.green, Color.yellow, Color.orange,Color.red};
  final static Color COPPER = new Color(200,200,120);
  final static Color DARKSAND=new Color(90,90,60);


    /** constructor */
  public ComponentRepaint() {
    setLayout(new BorderLayout());
    Panel p1 = new Panel(new GridLayout(3,1) );
      mouseComponent = new InnerMouseComponent("<Event listener, Component based>");
      p1.add(mouseComponent);
      update = new Button("Clicking this button also redraws the Component field above");
      p1.add(update, BorderLayout.CENTER);
      mouseContainer = new InnerMouseContainer("<Event listener, Container based>");
      p1.add(mouseContainer);
    add(p1,BorderLayout.CENTER);
    Panel p2 = new Panel(new GridLayout(2,1) );
      showOnButton=new Checkbox("Show messages on buttons",false);
      showOnButton.addItemListener(this);
      p2.add(showOnButton);
      noupdate = new Button("Clicking on this button doesn't help");
      p2.add(noupdate);
    add(p2,BorderLayout.SOUTH);
  }


  /****************************************************************************************************************************************/
  /**
  * checkbox item listener
  */
  public void itemStateChanged(ItemEvent evt) {
    if(showOnButton.getState()) {
      update.setLabel("Displaying the messages here triggers a repaint");
      noupdate.setLabel("messages will be displayed here just as well");
    }
    else {
      update.setLabel("Clicking this button also redraws the Component field above");
      noupdate.setLabel("Clicking on this button doesn't help");
    }
  }

  /****************************************************************************************************************************************/
  /**
  * Shows the data of the latest event in the TextArea
  */
  void displayEvent(String eventstring) {
    mouseComponent.setText(eventstring);
    mouseContainer.setText(eventstring);

    if(showOnButton.getState()) {
      update.setLabel(eventstring);
      noupdate.setLabel(eventstring);
    }
   System.out.println(eventstring);
  }

  /** VisualTestEngine help test */
  public String getHelpText(){
    return "A test on painting and repainting component members of a frame. The test shows\n"+
    "1) a mouse event field based on a Component,\n2) a <redraw> button,\n3)a mouse event field based on a Container, all this three components added to the same GridLayout Container"+
    "4) a <mirror> checkbox where you can ask to mirror the mouse events on the <redraw> button(2)\n 5) another button but not in the same Container as the mouse fields\n"+
    "\n What the test should do...\n"+
    "=> the component based and Container based mouse fields both have a MouseListener and FocusListener interface. Whenever one of this listeners throws an event"+
    " an event description message is displayed on both Containers.\n"+
    "=> If the mirror checkbox is active, the text is also displayed on the middle and lower button\n"+
    "\n What the test actually does \n(list of bugs this test was build to demonstrate / Wonka7.1, 7-10-2001)...\n"+
    "=> All mouse events(from both mouse fields) are correctly displayed on the upper Frame-based field. The lower component-based one does throw the events"+
    " and appearantly does react to them, but is not redrawn\n"+
    "=> When pressing the middle <redraw> button, the component-based field is suddenly redrawn in the state it should be in."+
    " this is because the <redraw> button and the component field are assigned to the same Gridlayout Container."+
    " Pressing the lower button(that is not in the Container) doesn't do anything\n"+
    "=> When the checkbox is pressed and all messages are mirrored to the <message>button, the button is redrawn on every event and so is the component field."+
    " as a consequence this test suddenly starts to work\n"+
    "\n(The mouse event fields are variations on the MouseEvent classes from the event-tests. Their expected behavior is explainrd below)\n"+
    "=> the test shows two squares: a <from> square resting on its side and a <to> square standing on its corner\n. The squares and the field boundaries"+
    " are drawn in one of the seven colors of the rainbow. At startup: from and to squares are drawn at the same position in the upper-left corner\n"+
    "=> Mouse pressed: the to-square is drawn at mouse-position and a line is drawn between from and to-square\n"+
    "=> Mouse released: the from-square is set to the same position as the to-square\n"+
    "=> Mouse clicked: the drawing color shifts to the next color of the rainbow\n"+
    "=> mouse entered: the from-square is moved to the point where the mouse has entered (it can't be drawn however, since half of it is outside the screen)\n"+
    "=> mouse exited: the to-square is moved to the point where the mouse has exited(can't be drawn however) and a line is drawn between the two points\n";
  }


  /****************************************************************************************************************************************/
  /**                              Inner classes                   **/
  /****************************************************************************************************************************************/
  interface canSetText {
    public void setText(String text);
  }
  /*********************************************************************************/
  /**  inner class with painting and mouse movements , based on panel*/
  class InnerMouseComponent extends Component implements canSetText, MouseListener, FocusListener{
    /** variables */
    private String message;
    private String name;

    private Point toPoint;
    private Point fromPoint;
    private boolean connected;
    private int currentColor;

    private Rectangle bounds;
    private Rectangle inside;

    /** constructor */
    public InnerMouseComponent(String panelname) {
      super();
      name = panelname;
      message = "No events to display yet";

      toPoint = new Point(8,8);
      fromPoint = new Point(8,8);
      connected=false;
      currentColor = colors.length-1;

      bounds = new Rectangle(0,0,0,0);
      inside = new Rectangle(0,0,0,0);

      this.addMouseListener(this);
      this.addFocusListener(this);
    }

    /** Sizes */
    public Dimension getMinimumSize() {
      return new Dimension(70,50);
    }

    public Dimension getPreferredSize() {
      return new Dimension(70,50);
    }


    /** mouse events */
    public void mouseClicked(MouseEvent event) {
      displayEvent("Called function "+name + ".mouseClicked()");
      if(currentColor>0) {
        currentColor--;
      }
      else{
        currentColor=colors.length-1;
      }
      this.repaint();
    }

    public void mouseEntered(MouseEvent event) {
      displayEvent("Called function  "+name + ".mouseEntered()");
      fromPoint.setLocation(event.getX(), event.getY());
      connected=false;
      this.repaint();
    }

    public void mouseExited(MouseEvent event) {
      displayEvent("Called function  "+name + ".mouseExited()");
      toPoint.setLocation(event.getX(), event.getY());
      connected=true;
      this.repaint();
    }

    public void mousePressed(MouseEvent event) {
      displayEvent("Called function  "+name + ".mousePressed()");
      toPoint.setLocation(event.getX(), event.getY());
      connected=true;
      this.repaint();
    }

    public void mouseReleased(MouseEvent event) {
      displayEvent("Called function  "+name + ".mouseReleased()");
      fromPoint.setLocation(toPoint);
      connected=false;
      this.repaint();
    }
        /** focus events */
    public void focusGained(FocusEvent event) {
      displayEvent("Called function "+name + ".focusGained(FocusEvent.isTemporary()="+event.isTemporary()+")");
    }

    public void focusLost(FocusEvent event) {
      displayEvent("Called function "+name + ".focusLost(FocusEvent.isTemporary()="+event.isTemporary()+")");
    }

    /** display text */
    public void setText(String text) {
      message=text;
      this.repaint();
    }

    /** paint */
    public void paint(Graphics g) {
			update(g);
  	}
  	
  	public void update(Graphics g) {
      // first time initialiser
      if(bounds.width==0 ){
        bounds.setBounds(1,1, this.getSize().width-2, this.getSize().height-2);
        inside.setBounds(7,7, this.getSize().width-14, this.getSize().height-14);
      }
      g.setColor(COPPER);
      g.fillRect(1,1, bounds.width, bounds.height);
      // name and message
      g.setColor(DARKSAND);
      g.drawString(name,10,15);
      g.drawString(message,10,30);
      //frame & points
      g.setColor(colors[currentColor]);
      g.drawRect(inside.x, inside.y, inside.width, inside.height);
      // starting point
      if(inside.contains(fromPoint)) {
        g.drawLine(fromPoint.x-7, fromPoint.y, fromPoint.x+7, fromPoint.y);
        g.drawLine(fromPoint.x, fromPoint.y-7, fromPoint.x, fromPoint.y+7);
        g.drawLine(fromPoint.x-7, fromPoint.y, fromPoint.x, fromPoint.y+7);
        g.drawLine(fromPoint.x-7, fromPoint.y, fromPoint.x, fromPoint.y-7);
        g.drawLine(fromPoint.x+7, fromPoint.y, fromPoint.x, fromPoint.y+7);
        g.drawLine(fromPoint.x+7, fromPoint.y, fromPoint.x, fromPoint.y-7);
      }
      // stopping point
      if(inside.contains(toPoint)) {
        g.drawLine(toPoint.x-5, toPoint.y-5, toPoint.x+5, toPoint.y+5);
        g.drawLine(toPoint.x-5, toPoint.y+5, toPoint.x+5, toPoint.y-5);
        g.drawLine(toPoint.x-5, toPoint.y-5, toPoint.x+5, toPoint.y-5);
        g.drawLine(toPoint.x-5, toPoint.y-5, toPoint.x-5, toPoint.y+5);
        g.drawLine(toPoint.x+5, toPoint.y+5, toPoint.x+5, toPoint.y-5);
        g.drawLine(toPoint.x+5, toPoint.y+5, toPoint.x-5, toPoint.y+5);
      }
      // if needed, connect the two points
      if(connected) {
        g.drawLine(toPoint.x, toPoint.y, fromPoint.x, fromPoint.y);
      }
    }

    public String toString() {
      return "InnerMouseComponent "+name;
    }
    //(end inner class)
  }


  /*********************************************************************************/
  /**  inner class with painting and mouse movements , based on panel*/
  class InnerMouseContainer extends Container implements canSetText, MouseListener, FocusListener{
    /** variables */
    private String message;
    private String name;

    private Point toPoint;
    private Point fromPoint;
    private boolean connected;
    private int currentColor;

    private Rectangle bounds;
    private Rectangle inside;

    /** constructor */
    public InnerMouseContainer(String panelname) {
      super();
      name = panelname;
      message = "No events to display yet";

      toPoint = new Point(8,8);
      fromPoint = new Point(8,8);
      connected=false;
      currentColor = colors.length-1;

      bounds = new Rectangle(0,0,0,0);
      inside = new Rectangle(0,0,0,0);

      this.addMouseListener(this);
      this.addFocusListener(this);
    }

    /** Sizes */
    public Dimension getMinimumSize() {
      return new Dimension(70,50);
    }

    public Dimension getPreferredSize() {
      return new Dimension(70,50);
    }


    /** mouse events */
    public void mouseClicked(MouseEvent event) {
      displayEvent("Called function "+name + ".mouseClicked()");
      if(currentColor>0) {
        currentColor--;
      }
      else{
        currentColor=colors.length-1;
      }
      this.repaint();
    }

    public void mouseEntered(MouseEvent event) {
      displayEvent("Called function  "+name + ".mouseEntered()");
      fromPoint.setLocation(event.getX(), event.getY());
      connected=false;
      this.repaint();
    }

    public void mouseExited(MouseEvent event) {
      displayEvent("Called function  "+name + ".mouseExited()");
      toPoint.setLocation(event.getX(), event.getY());
      connected=true;
      this.repaint();
    }

    public void mousePressed(MouseEvent event) {
      displayEvent("Called function  "+name + ".mousePressed()");
      toPoint.setLocation(event.getX(), event.getY());
      connected=true;
      this.repaint();
    }

    public void mouseReleased(MouseEvent event) {
      displayEvent("Called function  "+name + ".mouseReleased()");
      fromPoint.setLocation(toPoint);
      connected=false;
      this.repaint();
    }

    /** focus events */
    public void focusGained(FocusEvent event) {
      displayEvent("Called function "+name + ".focusGained(FocusEvent.isTemporary()="+event.isTemporary()+")");
    }

    public void focusLost(FocusEvent event) {
      displayEvent("Called function "+name + ".focusLost(FocusEvent.isTemporary()="+event.isTemporary()+")");
    }

    /** display text */
    public void setText(String text) {
      message=text;
      this.repaint();
    }
    /** paint */
    public void paint(Graphics g) {
			update(g);
  	}
  	
  	public void update(Graphics g) {
      // first time initialiser
      if(bounds.width==0 ){
        bounds.setBounds(1,1, this.getSize().width-2, this.getSize().height-2);
        inside.setBounds(7,7, this.getSize().width-14, this.getSize().height-14);
      }
      g.setColor(DARKSAND);
      g.fillRect(1,1, bounds.width, bounds.height);
      // message
      g.setColor(COPPER);
      g.drawString(name,10,15);
      g.drawString(message,10,30);
      //frame & points
      g.setColor(colors[currentColor]);
      g.drawRect(inside.x, inside.y, inside.width, inside.height);
      // starting point
      if(inside.contains(fromPoint)) {
        g.drawLine(fromPoint.x-7, fromPoint.y, fromPoint.x+7, fromPoint.y);
        g.drawLine(fromPoint.x, fromPoint.y-7, fromPoint.x, fromPoint.y+7);
        g.drawLine(fromPoint.x-7, fromPoint.y, fromPoint.x, fromPoint.y+7);
        g.drawLine(fromPoint.x-7, fromPoint.y, fromPoint.x, fromPoint.y-7);
        g.drawLine(fromPoint.x+7, fromPoint.y, fromPoint.x, fromPoint.y+7);
        g.drawLine(fromPoint.x+7, fromPoint.y, fromPoint.x, fromPoint.y-7);
      }
      // stopping point
      if(inside.contains(toPoint)) {
        g.drawLine(toPoint.x-5, toPoint.y-5, toPoint.x+5, toPoint.y+5);
        g.drawLine(toPoint.x-5, toPoint.y+5, toPoint.x+5, toPoint.y-5);
        g.drawLine(toPoint.x-5, toPoint.y-5, toPoint.x+5, toPoint.y-5);
        g.drawLine(toPoint.x-5, toPoint.y-5, toPoint.x-5, toPoint.y+5);
        g.drawLine(toPoint.x+5, toPoint.y+5, toPoint.x+5, toPoint.y-5);
        g.drawLine(toPoint.x+5, toPoint.y+5, toPoint.x-5, toPoint.y+5);
      }
      // if needed, connect the two points
      if(connected) {
        g.drawLine(toPoint.x, toPoint.y, fromPoint.x, fromPoint.y);
      }
    }

    public String toString() {
      return "InnerMouseContainer "+name;
    }
    //(end inner class)
  }


  /****************************************************************************************************************************************/
}
