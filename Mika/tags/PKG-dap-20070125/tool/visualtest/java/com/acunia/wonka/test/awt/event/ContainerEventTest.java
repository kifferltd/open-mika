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


package com.acunia.wonka.test.awt.event;

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;


public class ContainerEventTest extends VisualTestImpl implements ItemListener, ContainerListener {

  /** Variables*/
  private Checkbox displayTopLeft;
  private FillComponent topLeft;
  private Checkbox displayTopCenter;
  private FillComponent topCenter;
  private Checkbox displayTopRight;
  private FillComponent topRight;
  private NamedPanel top;

  private Checkbox displayLowerLeft;
  private FillComponent lowerLeft;
  private Checkbox displayLowerCenter;
  private FillComponent lowerCenter;
  private Checkbox displayLowerRight;
  private FillComponent lowerRight;
  private NamedPanel lower;

  List display;
  int step;

  final static int ELEMENTS = 6;

  /*********************************************************************/
  /** constructor */
  public ContainerEventTest() {
    step = 100/ELEMENTS;
    int clear = 155;
    setBackground(new Color(128,clear,clear));
    setLayout(new BorderLayout());

      display = new List(4,false);
      display.add("Your ContainerEvents displayed HERE");
    add(display, BorderLayout.SOUTH);

    top = new NamedPanel("Top panel", new BorderLayout());
      top.setForeground(Color.white);
      top.setBackground(new Color(128, clear, 128));
      clear+= step;
        Label toplabel = new Label("Top panel",Label.CENTER);
        toplabel.setBackground(new Color(128,clear,128));
      top.add(toplabel, BorderLayout.NORTH);
      Panel topboxes = new Panel(new GridLayout(1,3));
        clear+=step;
        topboxes.setBackground(new Color(128,clear,128));
        displayTopLeft = new Checkbox("display left", false);
        displayTopLeft.addItemListener(this);
        topboxes.add(displayTopLeft);
        displayTopCenter = new Checkbox("display center", false);
        displayTopCenter.addItemListener(this);
        topboxes.add(displayTopCenter);
        displayTopRight = new Checkbox("display right", false);
        displayTopRight.addItemListener(this);
        topboxes.add(displayTopRight);
      top.add(topboxes, BorderLayout.SOUTH);
      top.addContainerListener(this);
    add(top, BorderLayout.NORTH);

    clear+=step;
    topLeft = new FillComponent("<top Left>", new Color(128,clear,128), new Color(128,128,clear), Color.white);
    clear+=step;
    topCenter = new FillComponent("<top Center>", new Color(128,clear,128), new Color(128,128,clear), Color.white);
    clear+=step;
    topRight = new FillComponent("<top Right>", new Color(128,clear,128), new Color(128,128,clear), Color.white);

    clear = 155;
    lower = new NamedPanel("Lower panel", new BorderLayout());
      lower.setForeground(Color.white);
      lower.setBackground(new Color(128, 128, clear));
      clear+=step;
        Label lowerlabel = new Label("Lower panel",Label.CENTER);
        lowerlabel.setBackground(new Color(128, 128, clear));
      lower.add(lowerlabel, BorderLayout.NORTH);
      Panel lowerboxes = new Panel(new GridLayout(1,3));
        clear+=step;
        lowerboxes.setBackground(new Color(128, 128, clear));
        displayLowerLeft = new Checkbox("display left", false);
        displayLowerLeft.addItemListener(this);
        lowerboxes.add(displayLowerLeft);
        displayLowerCenter = new Checkbox("display center", false);
        displayLowerCenter.addItemListener(this);
        lowerboxes.add(displayLowerCenter);
        displayLowerRight = new Checkbox("display right", false);
        displayLowerRight.addItemListener(this);
        lowerboxes.add(displayLowerRight);
      lower.add(lowerboxes, BorderLayout.SOUTH);
      lower.addContainerListener(this);
    add(lower, BorderLayout.CENTER);

    clear+=step;
    lowerLeft = new FillComponent("<lower Left>", new Color(128,128,clear), new Color(128,clear,128), Color.white);
    clear+=step;
    lowerCenter = new FillComponent("<lower Center>", new Color(128,128,clear), new Color(128,clear,128), Color.white);
    clear+=step;
    lowerRight = new FillComponent("<lower Right>", new Color(128,128,clear), new Color(128,clear,128), Color.white);

  }

  /*********************************************************************/
  /**ItemListener event command: add or remove the specified component */
  public void itemStateChanged(ItemEvent evt) {
    Checkbox source = (Checkbox)(evt.getSource());
    if(source == displayTopLeft && source.getState()){
      top.add(topLeft, BorderLayout.WEST);
      validate();
    }
    else if(source == displayTopLeft) {
      top.remove(topLeft);
      validate();
    }
    else if(source == displayTopCenter && source.getState()) {
      top.add(topCenter, BorderLayout.CENTER);
      validate();
    }
    else if(source == displayTopCenter) {
      top.remove(topCenter);
      validate();
    }
    else if(source == displayTopRight && source.getState()) {
      top.add(topRight, BorderLayout.EAST);
      validate();
    }
    else if(source == displayTopRight) {
      top.remove(topRight);
      validate();
    }
    else if(source == displayLowerLeft && source.getState()) {
      lower.add(lowerLeft, BorderLayout.WEST);
      validate();
    }
    else if(source == displayLowerLeft) {
      lower.remove(lowerLeft);
      validate();
    }
    else if(source == displayLowerCenter && source.getState()) {
      lower.add(lowerCenter, BorderLayout.CENTER);
      validate();
    }
    else if(source == displayLowerCenter) {
      lower.remove(lowerCenter);
      validate();
    }
    else if(source == displayLowerRight && source.getState()) {
      lower.add(lowerRight, BorderLayout.EAST);
      validate();
    }
    else if(source == displayLowerRight) {
      lower.remove(lowerRight);
      validate();
    }
  }

  /*********************************************************************/
  /**ContainerListener event commands: display event in list          */
  public void componentAdded(ContainerEvent evt){
    displayEvent(evt,"componentAdded(evt)");
  }

  public void componentRemoved(ContainerEvent evt){
    displayEvent(evt,"componentRemoved(evt)");
  }

  /** event text */
  private void displayEvent(ContainerEvent evt, String action) {
    // clean display if needed
    if(display.getItemCount()>20) {
      display.removeAll();
    }
    //last line
    String line = " On component "+evt.getChild()+" From source "+evt.getContainer();
    display.add(line, 0);

    //first line
    int id = evt.getID();
    line = "Got "+action;
    if(id==ContainerEvent.COMPONENT_ADDED) {
      line += " for event <COMPONENT_ADDED>";
    }
    else if(id==ContainerEvent.COMPONENT_REMOVED) {
      line += " for event <COMPONENT_REMOVED>";
    }
    else {
      line += " for unknown event ("+id+")";
    }
    display.add(line, 0);
  }
  /*********************************************************************/
  /** visualTestEngine help text */
  public String getHelpText(){
    return "A on throwing and displaying ContainerEvents:\n"+
           " The test shows a list and two panels (a green and a blue one) each with a heading and three checkboxes."+
           " The checkboxes allow you to add or remove a component on the left, right or center of the panel. The list displays the"+
           " ContainerEvents fetched by a call to ContainerListener.componentAdded() or ContainerListener.componentRemoved()"+
           " every time a component is added or removed\n"+
           "\nItems to test:\n"+
           " => Component adding/removing: Clicking a checkbox should result in a component being added or removed. This must be directly"+
           " visible in the panel to which this component was added.\n"+
           " => Component events: Also adding a component (through its checkbox) should throw a ContainerListener.componentAdded() event"+
           " the ContainerEvent of which is analysed and displayed in the list below. Likewise, removing a component should throw a"+
           " ContainerListener.componentRemoved() event displayrd likewise.\n"+
           " => ComponentEvent data: the ComponentEvent display should list in a correct way the container that threw the event,"+
           " the component added or removed and the event ID ContainerEvent.COMPONENT_ADDED or ContainerEvent.COMPONENT_REMOVED.\n"+
           " => Component layout: The top panel is added as BorderLayout.NORTH through a BorderLayout manager. If it doesn't contain components,"+
           " it should display only the title and the checkboxes. If one component is added, it should grow in height to host that component,"+
           " while the lower panel should shrink an equal amount. Likewise removing the last component of the top panel should force the lower"+
           " panel to grow until the top panel only contains title and checkboxes.\n"+
           " => Component size: The components added are a special test component, that display their minimum size as a colored rectangle"+
           " in the middle of their screen. the BorderLayout used takes the minimum sizes of the components for his layout calculations so that:\n"+
           " ... the left and right conponents of the lower panel should not be wider then their inner rectangle, yet should display this rectangle completely\n"+
           " ... the center component of the top panel should not be higher then the inner rectangle, yet should display this rectangle completely\n"+
           " ... the left and right components of the top panel should consist out of the rectangle, and nothing but the rectangle,"+
           " yet this rectangle should be completely visible";
  }

  /*********************************************************************/
  /** stand-alone main */
  static public void main (String[] args) {
    ContainerEventTest ce = new ContainerEventTest();
    ce.show();
  }


  /*********************************************************************/
  /*********************************************************************/
  /*********************************************************************/
  /** inner class display component: a component of given minimum size */
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
      innerRect = new Rectangle(0,0,60,25);
      innerColor = rectangle;
      textColor = text;
    }

    public Dimension getMinimumSize() {
      return new Dimension(innerRect.width, innerRect.height);
    }

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
        middle = this.getSize().height/2;
      }
      g.setColor(this.getBackground());
      g.fillRect(0,0,bounds.width-1, bounds.height-1);
      g.setColor(innerColor);
      g.drawRect(innerRect.x, innerRect.y, innerRect.width-1, innerRect.height-1);
      g.fillRect(innerRect.x+2, innerRect.y+2, innerRect.width-4, innerRect.height-4);
      g.setColor(textColor);
      g.drawString(this.getName(),innerRect.x, middle+3);

    }

    /** toString returns the components name*/
    public String toString(){
      return this.getName();
    }
  }

  class NamedPanel extends Panel {
    /** constructor adds the panels name*/
    public NamedPanel(String name, LayoutManager layout) {
      super(layout);
      this.setName(name);
    }
    /** toString returns the panels name*/
    public String toString(){
      return this.getName();
    }
  }

}
