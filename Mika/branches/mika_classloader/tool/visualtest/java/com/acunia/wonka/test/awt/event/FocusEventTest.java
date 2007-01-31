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

import com.acunia.wonka.test.awt.*;
import java.awt.event.*;
import java.awt.*;

public class FocusEventTest extends VisualTestImpl implements ActionListener, FocusListener{

  private Button[] request;
  private Checkbox[] transversable;
  private FocusDisplay[] display;
  private List eventList;
  final static int ELEMENTS=4;

  /****************************************************************/
  /** constructor
  */
  public FocusEventTest() {
    setForeground(new Color(32, 80,32));
    setBackground(new Color(64,128,64));
    int step = 100/ELEMENTS;

    request = new Button[ELEMENTS];
    transversable = new Checkbox[ELEMENTS];
    display = new FocusDisplay[ELEMENTS];

    setLayout(new BorderLayout());
    Panel buttons = new Panel(new GridLayout(ELEMENTS+1,1));
      buttons.add(new Label("Display Focus", Label.CENTER));
      int green = 155;
      int rcomp = 100;
      int bcomp = 100;
      for(int i=0; i<ELEMENTS; i++){
        request[i] = new NoTransferButton("request","request_"+i);
        request[i].setBackground(new Color(rcomp,green,bcomp));
        request[i].addActionListener(this);
        buttons.add(request[i]);
        green+=step;
        bcomp+=step;
        rcomp+=step;
      }
    add(buttons, BorderLayout.WEST);

    Panel boxes = new Panel(new GridLayout(ELEMENTS+1,1));
      boxes.add(new Label("is Traversable?", Label.CENTER));
      rcomp=100;
      green=155+step;
      bcomp=100+step;
      for(int i=0; i<ELEMENTS; i++){
        transversable[i] = new NoTransferCheckbox("traversable","checkbox_"+i);
        transversable[i].setBackground(new Color(rcomp,green,bcomp));
        boxes.add(transversable[i]);
        rcomp+=step;
        green+=step;
        bcomp+=step;
      }
    add(boxes, BorderLayout.EAST);

    Panel displays = new Panel(new GridLayout(ELEMENTS+1,1));
      displays.add(new Label("Focus displays", Label.CENTER));
      rcomp=100+step;
      green=155;
      bcomp=100;
      for(int i=0; i<ELEMENTS; i++){
        display[i] = new FocusDisplay("Display_"+i, transversable[i], new Color(rcomp,green,bcomp),getForeground());
        display[i].addFocusListener(this);
        displays.add(display[i]);
        rcomp+=step;
        green+=step;
        bcomp+=step;
      }
    add(displays, BorderLayout.CENTER);


    eventList=new NoTransferList(5);
    eventList.setForeground(Color.white);
    eventList.add("Your ActionEvents displayed HERE");
    eventList.add(".");
    eventList.add(".");
    eventList.add(".");
    eventList.add(".");
    eventList.add(".");
    add(eventList, BorderLayout.SOUTH);
  }

  /****************************************************************/
  /** action performed : request or transfer focus
  */
  public void actionPerformed(ActionEvent evt){
    Object source = evt.getSource();
    boolean found=false;
    for(int i=0; i<ELEMENTS && !found; i++) {
      if(source == request[i]){
        display[i].requestFocus();
        displayEventMessage("requested focus for "+display[i]);
        found=true;
      }
    }
  }
  /****************************************************************/
  /** focus events focus gained/lost from focuslistener
  */
  public void focusGained(FocusEvent evt) {
    if(evt.getSource() instanceof FocusDisplay) {
      ((FocusDisplay)(evt.getSource())).setFocus(true);
    }
    displayEventMessage(displayEventShortcut(evt));
  }

  public void focusLost(FocusEvent evt) {
    if(evt.getSource() instanceof FocusDisplay) {
      ((FocusDisplay)(evt.getSource())).setFocus(false);
    }
    displayEventMessage(displayEventShortcut(evt));
  }

  /****************************************************************/
  /** Display a message on the event list
  */
  void displayEventMessage(String message) {
    if(eventList.getItemCount()>60) {
      eventList.removeAll();
      eventList.add(".");
      eventList.add(".");
      eventList.add(".");
      eventList.add(".");
      eventList.add(".");
    }
    eventList.add(message,0);
  }

  /****************************************************************/
  /** build event text
  */
  private String displayEventShortcut(FocusEvent evt) {
    String line;
    Object source = evt.getSource();
    if(source==null){
      line = " From source() == NULL ";
    }
    else {
      line = "From "+source;
    }
    int id = evt.getID();
    if(id==FocusEvent.FOCUS_GAINED) {
      line+= " <FOCUS GAINED>, temporary: ";
    }
    else if(id==FocusEvent.FOCUS_LOST) {
      line+= " <FOCUS lost>, temporary: ";
    }
    else {
      line+= " unknown event ("+id+"), temporary: ";
    }
    return line+ evt.isTemporary();
  }
  /****************************************************************/
  /** Help text
  */
  public String getHelpText() {
    return "The aim: test the throwing and the correct layout of FocusEvents:\n\n"+
           "The test: The upper part of the screen consists out of "+ELEMENTS+" rows of each a focus-request button,"+
           " a focus listener display and a focus traversable checkbox.\n"+
           "The listener display is linked to a focus listener so thatwhen it gains or looses focus, it will change color and display"+
           " a FocusEvent message in the list below\n"+
           "The request buttons send a requestFocus() call on behalf of their listeners. The checkboxes set the focusTraversable property"+
           " of their listeners to true or false while clicking on <transfer> bos of a listener makes that listener throw a transferFocus()"+
           " request\n(The buttons, checkboxes and list are overridden so their focusTraversable property is always FALSE)\n"+
           "\nItems to test:\n"+
           "- focus gained event: When pressing a display, this display should become selected, the framework will receive and display"+
           " a FocusEvent on its behalf and the display itself should change color to indicate that it is selected.\n"+
           "- focus lost event: When pressing another item, pressing the list or pressing one of the VisualTestengine buttons,"+
           " you should loose focus to the original display: the display changes back to its original color and a  focusLost vent should be displayed in the list\n"+
           "- Requesting focus: pushing a <request> button makes the adjecent display call a requestFocus() command. Directly afterwards"+
           "the framework should give focus to this display and throw a focusGained() event to note this\n"+
           "   (the list displays this event and the display changes color)\n)"+
           "- Traversing focus: Clicking the <transfer> rectangle on a display should force a transferFocus() request: The display will loose"+
           " focus(focusLostEvent on the list) and the next traversable element should get the focus.\n"+
           "   (If this is a display just as well, its focusGained event will be shown just as well)"+
           "- Key traversing: the focus should equally be transferred to the next traversable element by pressing the <tab>-key\n"+
           "- <next traversable element>: A focus is transferred to the next Component that is transversable, eg. its Component.isTraversable()"+
           " function returns true. the buttons, checkboxes and list of this test have all been made untraversable, so the focus will wander"+
           " between the TestEngine buttons on the right and the display panels. Using the checkboxes you can make a display untraversable and see"+
           " it being <jumped over> in the transversable sequences\n"+
           "   (also note the traversable request messages in the event list for each component which traversability is checked)\n";
  }

  /****************************************************************/
  /**
  * A java.awt.Button that is NOT focus transversable
  */
  class NoTransferButton extends Button {
    private String name;

    public NoTransferButton(String label, String name){
      super(label);
      this.name=name;
    }

    public boolean isFocusTraversable() {
      displayEventMessage("isFocusTraversable Button <"+name+"> false by def.");
      return false;
    }
  }

  /****************************************************************/
  /**
  * A java.awt.Button that is NOT focus transversable
  */
  class NoTransferCheckbox extends Checkbox {
    private String name;

    public NoTransferCheckbox(String label, String name){
      super(label, true);
      this.name=name;
    }

    public boolean isFocusTraversable() {
      displayEventMessage("isFocusTraversable Checkbox <"+name+"> false by def.");
      return false;
    }
  }

  /****************************************************************/
  /**
  * A java.awt.Button that is NOT focus transversable
  */
  class NoTransferList extends List {
    public NoTransferList(int preferred){
      super(preferred, false);
    }

    public boolean isFocusTraversable() {
      displayEventMessage("isFocusTraversable event list false by def.");
      return false;
    }
  }

  /****************************************************************/
  /**
  * inner class AWTEventdisplay with focus listener
  */
  class FocusDisplay extends AWTEventDisplay implements MouseListener{
    /**name variable*/
    private boolean hasFocus;
    private Checkbox transferFlag;
    private Rectangle transferBox;
    /**Constructor*/
    public FocusDisplay(String title, Checkbox indicator, Color back, Color front){
      super(title, back, front);
      transferFlag = indicator;
      hasFocus=false;
      this.addMouseListener(this);
      transferBox = new Rectangle();
    }

    /** Component.isfocustransversable: return true if 'transversable' checkbox is clicked*/
    public boolean isFocusTraversable() {
      boolean flag = transferFlag.getState();
      displayEventMessage("checked focus traversable on <"+message+"> : "+flag);
      return flag;
    }

    /** on focus, repaint*/
    public void setFocus(boolean focus) {
      hasFocus=focus;
      this.repaint();
    }

    /**toString : return name*/
    public String toString() {
      return message;
    }

    /**Mouse listener: when clicked, transfer focus*/
    public void mousePressed(MouseEvent evt) {}
    public void mouseEntered(MouseEvent evt) {}
    public void mouseExited(MouseEvent evt) {}
    public void mouseReleased(MouseEvent evt) {}

    public void mouseClicked(MouseEvent evt) {
      if(transferBox.contains(evt.getX(), evt.getY())) {
        displayEventMessage("<"+message+"> : transferred focus");
        this.transferFocus();
      }
    }


     /** Overrides AWTEventDisplay.update to change color when got focus
    */
  	public void update(Graphics g) {
      // first time initialiser
      if(frame.width<=0 ){
        frame.setSize(this.getSize().width-2, this.getSize().height-2);
        inside.setBounds(3,3, frame.width-5, frame.height-5);
        transferBox.setBounds(frame.width-45,5,40,frame.height-10);
      }
      g.setColor((hasFocus)?foreground:background);
      g.fillRect(1,1, frame.width, frame.height);
      g.setColor((hasFocus)?background:foreground);
      g.drawRect(inside.x, inside.y, inside.width, inside.height);
      g.drawRect(transferBox.x, transferBox.y, transferBox.width, transferBox.height);
      g.drawString(message,20,inside.height);
      g.drawString("transfer",transferBox.x+3,inside.height-2);
    }

  }
}
