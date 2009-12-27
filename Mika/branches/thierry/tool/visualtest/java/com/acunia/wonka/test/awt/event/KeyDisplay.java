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

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyDisplay extends AWTEventDisplay implements KeyListener {


  /************************************************************************************************************/
  /** constructor
  */
  public KeyDisplay(String firstmessage, Color back, Color text) {
    super(firstmessage, back, text);
  }

  public KeyDisplay(Color back, Color text) {
    super("Your KeyEvents displayed HERE", back, text);
  }

  /************************************************************************************************************/
  /** CollectsEvents help text
  */
  public String getHelpText() {
    return "Displays a panel with a short text about the KeyEvent received.";
  }

  /************************************************************************************************************/
  /** THE KeyListener events : get the event shortcut and display it
  */
  public void keyPressed(KeyEvent evt) {
    message = displayKeyShortcut(evt);
    repaint();
  }

  public void keyReleased(KeyEvent evt) {
    message = displayKeyShortcut(evt);
    repaint();
  }

  public void keyTyped(KeyEvent evt) {
    message = displayKeyShortcut(evt);
    repaint();
  }



  /****************************************************************************************************************************************/
  /**     display event diagnostics
  * Following functions will be tested :
  * (java.util)EventObject.getSource()
  * (java awt)AWTEvent.getID() (KEY_PRESSED, KEY_RELEASED or KEY_TYPED)
  * (java awt.event)InputEvent.getWhen()
  * (java awt.event)InputEvent.getModifiers() and static (java awt.event)KeyEvent.getKeyModifiersText(int keymodifiers)
  * (java awt.event)KeyEvent.getKeyCode(), isActionKey() and static getKeyText(int keycode),
  * (java awt.event)KeyEvent.getKeyChar()
  */

  public static String[] displayKeyEvent(KeyEvent evt) {
    String[] lines = new String[3];
    // line 1: Object <getSource()>, action <getID()> received at <getwhen()>
    Object source = evt.getSource();
    if(source==null){
      lines[0] = "evt.getSource() == NULL";
    }
    else {
      lines[0] = "getSource()= "+source;
    }
    // event type
    int id = evt.getID();
    if(id==KeyEvent.KEY_PRESSED){
      lines[0]+=" KEY_PRESSED(";
    }
    else if(id==KeyEvent.KEY_RELEASED){
      lines[0]+=" KEY_RELEASED(";
    }
    else if(id==KeyEvent.KEY_TYPED){
      lines[0]+=" KEY_TYPED(";
    }
    else {
      lines[0]+=" UNKNOWN COMMAND ID(";
    }
    // received at...
    lines[0]+= id+") at time "+evt.getWhen();

    //line2:Character<getKeyChar>, modifiers <getModifiers()> : getModifiersText
    char key = evt.getKeyChar();
    id = evt.getModifiers();
    if(key == KeyEvent.CHAR_UNDEFINED){
      lines[1] = "undefined <CHAR_UNDEFINED> Actionkeys : "+id;
    }
    else {
      lines[1] = "Character <"+key+"> Actionkeys : "+id;
    }
    if(id==0){
      lines[1] += " (No modifiers)";
    }
    else {
      lines[1] += " => "+KeyEvent.getKeyModifiersText(id);
    }

    // line 3:Key code <getKeycode>:name <getKeyText> is action key:<isActionKey>
    id = evt.getKeyCode();
    lines[2] = "Key code: "+id+" <"+KeyEvent.getKeyText(id);
    lines[2] += (evt.isActionKey())?"> is action key":">";
    return lines;
  }

  /****************************************************************************************************************************************/
  /**     display event diagnostics in a short line
  *  <Key COMMAND> from [source] : Character <char> modifiers [modifiers....]
  * for non-action key or
  *  <Key COMMAND> from [source] : undefined char [name], modifiers [modifiers....]
  * for non-action non-character key or
  *  <Key COMMAND> from [source] : action key [name], modifiers [modifiers....]
  * for action key
  */
  public static String displayKeyShortcut(KeyEvent evt) {
    String line;
    Object source = evt.getSource();
    int id = evt.getID();
    if(id==KeyEvent.KEY_PRESSED){
      line = "<KEY_PRESSED> from "+source;
    }
    else if(id==KeyEvent.KEY_RELEASED){
      line = "<KEY_RELEASED> from "+source;
    }
    else if(id==KeyEvent.KEY_TYPED){
      line = "<KEY_TYPED> from "+source;
    }
    else {
      line = "<UNKNOWN COMMAND> from "+source;
    }

    char c = evt.getKeyChar();
    if(evt.isActionKey()){
      line += " Action key ["+KeyEvent.getKeyText(evt.getKeyCode())+"] modifiers :";
    }
    else if(c==KeyEvent.CHAR_UNDEFINED){
      line += " undefined ["+KeyEvent.getKeyText(evt.getKeyCode())+"] modifiers :";
    }
    else {
      line += " character <"+c+"> modifiers :";
    }

    line+= KeyEvent.getKeyModifiersText(evt.getModifiers());
    return line;
  }

  //end test
}
