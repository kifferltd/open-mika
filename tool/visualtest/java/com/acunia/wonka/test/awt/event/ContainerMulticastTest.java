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


public class ContainerMulticastTest extends VisualTestImpl implements ItemListener, ActionListener {

  /** Variables*/
  private Checkbox displayLeft;
  private FillComponent left;
  private Checkbox displayCenter;
  private FillComponent center;
  private Checkbox displayRight;
  private FillComponent right;
  private NamedPanel display;

  private Button[] add;
  private Button[] remove;
  private ListenerLabel[] listener;
  int step;

  final static int LISTENERS = 6;

  /*********************************************************************/
  /** constructor */
  public ContainerMulticastTest() {
    step = 100/LISTENERS;
    int clear = 155;
    setBackground(new Color(64,clear,clear));
      setForeground(Color.white);
    setLayout(new BorderLayout());

    display = new NamedPanel("<display panel>", new BorderLayout());
      clear+=step;
        Label displaylabel = new Label("display panel",Label.CENTER);
        displaylabel.setBackground(new Color(64, 64, clear));
      display.add(displaylabel, BorderLayout.NORTH);
      Panel boxes = new Panel(new GridLayout(1,3));
        clear+=step;
        boxes.setBackground(new Color(64, 64, clear));
        displayLeft = new Checkbox("component left", false);
        displayLeft.addItemListener(this);
        boxes.add(displayLeft);
        displayCenter = new Checkbox("component center", false);
        displayCenter.addItemListener(this);
        boxes.add(displayCenter);
        displayRight = new Checkbox("component right", false);
        displayRight.addItemListener(this);
        boxes.add(displayRight);
      display.add(boxes, BorderLayout.SOUTH);
    add(display, BorderLayout.CENTER);

    clear+=step;
    left = new FillComponent("<Left>", new Color(128,64,clear), new Color(128,clear,128), Color.white);
    clear+=step;
    center = new FillComponent("<Center>", new Color(96,96,clear), new Color(128,clear,128), Color.white);
    clear+=step;
    right = new FillComponent("<Right>", new Color(64,128,clear), new Color(128,clear,128), Color.white);

    clear = 155;
    Panel listeners = new Panel(new GridLayout(LISTENERS,1));
      Panel[] row = new Panel[LISTENERS];
      add = new Button[LISTENERS];
      listener = new ListenerLabel[LISTENERS];
      remove = new Button[LISTENERS];
      for(int i=0; i<LISTENERS; i++) {
        row[i] = new Panel(new BorderLayout() );
          listener[i] = new ListenerLabel("Press <add> to add a listener to this panel", new Color(128,128,clear));
          row[i].add(listener[i],BorderLayout.CENTER);
          add[i] = new Button("Add");
          add[i].setBackground(new Color(100,100,clear));
          add[i].addActionListener(this);
          row[i].add(add[i],BorderLayout.WEST);
          remove[i] = new Button("Remove");
          remove[i].setBackground(new Color(155,155,clear));
          remove[i].addActionListener(this);
          row[i].add(remove[i], BorderLayout.EAST);
          clear+= step;
        listeners.add(row[i]);

      }
    add(listeners, BorderLayout.SOUTH);
  }

  /*********************************************************************/
  /**ItemListener event command: add or remove the specified component */
  public void itemStateChanged(ItemEvent evt) {
    Checkbox source = (Checkbox)(evt.getSource());
    if(source == displayLeft && source.getState()){
      display.add(left, BorderLayout.WEST);
      validate();
    }
    else if(source == displayLeft) {
      display.remove(left);
      validate();
    }
    else if(source == displayCenter && source.getState()) {
      display.add(center, BorderLayout.CENTER);
      validate();
    }
    else if(source == displayCenter) {
      display.remove(center);
      validate();
    }
    else if(source == displayRight && source.getState()) {
      display.add(right, BorderLayout.EAST);
      validate();
    }
    else if(source == displayRight) {
      display.remove(right);
      validate();
    }
  }

  /************************************************************************************************************/
  /** ActionListener interface actionPerformed:
  * with the <add> or <remove> button clicked, add or remove the Item listener to its panel
  */
  public void actionPerformed(ActionEvent evt) {
    boolean found = false;
    Object source = evt.getSource();
    for(int i=0; i<LISTENERS && !found; i++) {
      if(source == add[i]) {
        display.addContainerListener(listener[i]);
        listener[i].setText("Listener added. press <remove> to remove it again");
        found = true;
      }
      else if(source == remove[i]) {
        display.removeContainerListener(listener[i]);
        listener[i].setText("Listener removed. press <add> to add it again");
        found = true;
      }
    }

  }

  /*********************************************************************/
  /** visualTestEngine help text */
  public String getHelpText(){
    return "The aim: test the throwing of ContainerEvents through the AWTEventMulticaster functions:\n\n"+
           "The top of the screen consists out a panel with title and a row of checkboxes.\n"+
           " The lower part out of "+LISTENERS+" display labels. Each of this panels is flanked by an <add> and a <remove> button.\n"+
           "Clicking the buttons, you can add a component to the left, center or right of the display panel, and remove them again."+
           " Every time a component is added or removed, the panel throws a ContainerEvent. Using the Add/Delete buttons next to the labels"+
           " you can add a ComponentListener to that label in order to get this events displayed on that label, or you can remove this listener again\n."+
           "(Adding and removing of containerListeners is done by calls to the static AWTEventMulticaster.Add()and -remove() functions)\n"+
           "\n Items to test : \n -------------\n"+
           " => Pressing <add> for a label and subsequently adding or removing a component to check if a ComponentEvent is thrown and displayed"+
           " on the newly selected label, as well as on all other previously selected labels\n"+
           " => Pressing <remove> for a label and subsequently adding or removing a component to check if the ComponentEvent thrown is no longer"+
           " displayed on that label, nor on the labels previously deselected, yet remains displayed on all other labels still selected\n"+
           " => Pressing <add> for the same label over and over again to see that the listener for that label is not added twice\n"+
           " => Pressing <remove> for the same label over and over again, or pressing <remove> on a label to which no listener is added yet"+
           " to check that a listener is not removed twice\n"+
           " \n ps. as the Add and remove routines have a slightly different algorithm for the first and second listener then for all"+
           " subsequent listeners, specially check the behavior when \n"+
           "    - adding the first label, adding the second label, adding the third label\n"+
           "    - removing the third-last label, removing the second-last label, removing the last label\n"+
           "    - giving a remove-command when no labels are selected";
  }

  /*********************************************************************/
  /** stand-alone main */
  static public void main (String[] args) {
    ContainerMulticastTest ce = new ContainerMulticastTest();
    ce.show();
  }


  /*********************************************************************/
  /*********************************************************************/
  /*********************************************************************/
  /**
  * inner class display component: a component of given minimum size
  */
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

  /*********************************************************************/
  /**
  * inner class Panel with toString == panel name
  */
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
  /*********************************************************************/
  /**
  * inner class label with Component listener
  */
  class ListenerLabel extends Label implements ContainerListener{
    /**constructor*/
    public ListenerLabel(String text, Color background) {
      super(text, Label.CENTER);
      this.setBackground(background);
    }

    /**container listener*/
    public void componentAdded(ContainerEvent evt) {
      this.setText("ComponentAdded "+evt.getChild()+" to "+evt.getComponent());
    }

    public void componentRemoved(ContainerEvent evt) {
      this.setText("ComponentRemoved "+evt.getChild()+" from "+evt.getComponent());
    }
  }



}
