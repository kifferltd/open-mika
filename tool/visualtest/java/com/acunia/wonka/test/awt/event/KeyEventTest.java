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

public class KeyEventTest extends VisualTestImpl implements ActionListener{

  /** variables */
  private List listDisplay;
  private Button listClear;
  private final static int LISTENERS=3;
  /****************************************************************/
  /** constructor
  */
  public KeyEventTest() {
    //setBackground(Color.gray);
    int gray=0x60;
    int darkgray=0x30;
    setLayout(new BorderLayout());
    // key-receiving events
    Panel keydisplays = new Panel(new GridLayout(LISTENERS,1));
      for(int i=0; i<LISTENERS;i++) {
        keydisplays.add(new EventKeyDisplay("Listener_"+i,new Color(darkgray,darkgray,darkgray),new Color(gray,gray,gray+0x30),Color.white));
        gray+=0x10;
        darkgray+=0x10;
      }
    add(keydisplays, BorderLayout.NORTH);
    // display list
    listDisplay=new List(3,false);
    listDisplay.setBackground(new Color(gray,gray,gray+0x30));
    listDisplay.add("Your KeyEvents displayed HERE");
    add(listDisplay, BorderLayout.CENTER);
    gray+=0x10;
    // clear button
    listClear = new Button("Clear list");
    listClear.setBackground(new Color(gray,gray,gray+0x30));
    listClear.addActionListener(this);
    add(listClear, BorderLayout.SOUTH);
  }

  /****************************************************************/
  /** action listener: if listClear pressed, clear list*/
  public void actionPerformed(ActionEvent evt) {
    listDisplay.removeAll();
    listDisplay.add("Your KeyEvents displayed HERE");
  }

  /****************************************************************/
  /** display message on list
  */
  public void displayListMessage(String message) {
    listDisplay.add(message,0);
  }

  public void displayListMessage(String[] messagelist) {
    for(int i=0;i<messagelist.length; i++){
      listDisplay.add(messagelist[i],i);
    }
  }

  /****************************************************************/
  /**
  * our special class of KeyDisplay featuring a different color on focus
  * a forward of the key events to the list
  * and a name for the toString method
  */
  private class EventKeyDisplay extends KeyDisplay  implements KeyListener, FocusListener, MouseListener {
    //variables
    //protected Color background;
    //protected Color foreground;
    protected Color active;
    protected Color passive;
    //protected String message;
    protected String name;

    //constructor
    public EventKeyDisplay(String name, Color active, Color passive, Color text) {
      super(name, passive, text);
      this.active = active;
      this.passive = passive;
      this.name = name;
      // listeners
      this.addKeyListener(this);
      this.addFocusListener(this);
      this.addMouseListener(this);
    }

    // overrides Object.toString to return the objects name
    public String toString() {
      return name;
    }

    // focus listener switches background color if becomes active or passive
    public void focusGained(FocusEvent evt) {
      background = active;
      message=name+" (active)";
      displayListMessage(name+" gained focus");
      this.repaint();
    }

    public void focusLost(FocusEvent evt) {
      background = passive;
      message=name;
      displayListMessage(name+" lost focus");
      this.repaint();
    }

    // key listener overrides: show text and send complete description to list
    public void keyPressed(KeyEvent evt) {
      message = displayKeyShortcut(evt);
      displayListMessage(displayKeyEvent(evt));
      displayListMessage("   => KEY EVENT...............");
      this.repaint();
    }

    public void keyReleased(KeyEvent evt) {
      message = displayKeyShortcut(evt);
      displayListMessage(displayKeyEvent(evt));
      displayListMessage("   => KEY EVENT...............");
      this.repaint();
    }

    public void keyTyped(KeyEvent evt) {
      message = displayKeyShortcut(evt);
      displayListMessage(displayKeyEvent(evt));
      displayListMessage("   => KEY EVENT...............");
      this.repaint();
    }

    /**Special for Sun: gaining focus by clicking on it is NOT a standard feature: make it one*/
    public void mousePressed(MouseEvent evt) {
      if(background == passive) {
        // not active yet=> make it so
        this.requestFocus();
      }
    }
    public void mouseEntered(MouseEvent evt) {}
    public void mouseExited(MouseEvent evt) {}
    public void mouseReleased(MouseEvent evt) {}
    public void mouseClicked(MouseEvent evt) {}
    /**Special for Sun: the panel must be focus traversable to gain focus*/
    public boolean isFocusTraversable() {
      return true;
    }

  }


  /****************************************************************/
  /** Help text
  */
  public String getHelpText() {
    return "The aim: test the throwing and the correct layout of KeyEvents:\n\n"+
           "The test: The upper part of the screen consists out of three panels, each linked to a FocusListener and a KeyListener"+
           " The FocusListener will cause the panel currently having the focus to change color. This is important because actions"+
           " on the keyboard will send a KeyEvent to the component that currently has the focus and only to this one.\n\n"+
           " Typing a key will display a short description of the key event in the panel currently active and will send a detailed"+
           " description of the event to the message list below\n"+
           "\nItems to test:\n"+
           "- Key event chain: pressing a key should throw a KEY_PRESSED event. Releasing the key again should first throw a KEY_RELEASED"+
           " event and then a KEY_TYPED event\n"+
           "- Key modifiers: the four modifier keys <shift>, <ctrl>, <alt> and <meta>, when pressed together with a key character, should"+
           " appear in that characters key-event as character modifiers\n"+
           "- Key typed interpretation: when typing a character key together with the shift key, the KEY_RELEASED event should show this"+
           " character as small char + shift, whereas the KEY_PRESSED event should show the corresponding capital character\n"+
           "- Key focus: the key event will be thrown in that component that currently has the focus. If the focus is currently on the "+
           " display list, none of the event panels will receive the key events, so no events will be displayed\n"+
           "- Non-character keys: There are a number of non-action keys such as F1 or home. The key event should recognise them"+
           " through their KeyEvent.isActionKey() function which should return TRUE";
  }

}
