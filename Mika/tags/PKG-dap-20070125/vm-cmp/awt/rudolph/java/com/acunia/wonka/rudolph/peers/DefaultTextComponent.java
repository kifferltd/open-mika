/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

package com.acunia.wonka.rudolph.peers;

import java.awt.peer.*;
import java.awt.event.*;
import java.awt.*;

public class DefaultTextComponent extends DefaultComponent implements TextComponentPeer, KeyListener {

  protected String text = "";
  protected int position;
  protected int selectionStart;
  protected int selectionStop;

  public DefaultTextComponent(TextComponent textComponent) {
    super(textComponent);
    textComponent.addKeyListener(this);
    if(com.acunia.wonka.rudolph.popupkeyboard.Keyboard.POPUPKEYBOARD){
      new com.acunia.wonka.rudolph.popupkeyboard.KeyboardListener(textComponent);
    }
  }

  public int getCaretPosition() {
    return position;
  }
  
  public void setCaretPosition(int newposition) {
    if(newposition < 0) {
      position = 0;
    }
    else if(newposition > text.length()) {
      position = text.length();
    }
    else {
      position = newposition;
    }
  }
  
  public int getSelectionStart() {
    return selectionStart;
  }
 
  public int getSelectionEnd() {
    return selectionStop;
  }
  
  public void select(int start, int end) {
    selectionStart = (start > 0 ? start : 0);
    selectionStop = (end > 0 ? end : 0);
    int length = text.length();
    if(selectionStart > length) selectionStart = length;
    if(selectionStop > length) selectionStop = length;
    
    paint(getGraphics());
  }
  
  public String getText() {
    return text;
  }
  
  public void setText(String newtext) {
    text = newtext;
    int length = text.length();
    if(position > length) position = length;
    if(selectionStart > length) selectionStart = length;
    if(selectionStop > selectionStart) selectionStop = selectionStart;
  }
  
  public void setEditable(boolean editable) {
  }
  
  /*
  ** Handle keyboard events.
  */

  public void keyPressed(KeyEvent evt) {
	  boolean doPaint = true;
	  
	  switch(evt.getKeyCode()) {
		  // key left: move carret left and set selection to carer
		  case KeyEvent.VK_LEFT:
			if(evt.isShiftDown() && moveSelection(position-1)) {
			}
			else if(moveCaret(position-1)) {
			}
			break;

		  // key right: move carret right and set selection to caret
		  case KeyEvent.VK_RIGHT:
			if(evt.isShiftDown() && moveSelection(position+1)) {
			}
			else if(moveCaret(position+1)) {
			}
			break;	
			
		  // key right: move carret completely left
		  case KeyEvent.VK_HOME:
			if(evt.isShiftDown() && moveSelection(0)) {
			}
			else if(moveCaret(0)) {
			}
			break;

		  case KeyEvent.VK_END:
			if(evt.isShiftDown() && moveSelection(text.length())) {
			  setCaretPosition(position); // triggers a repaint
			}
			else if(moveCaret(text.length())) {
			}
			break;			
			
		  default:
			doPaint = false;
	  }
	  
	  if(doPaint)
	  {
		paint(getGraphics());		  
	  }
  }
  
  public void keyReleased(KeyEvent evt) {
  }
  
  public void keyTyped(KeyEvent evt) {
    switch(evt.getKeyCode()) {
      case KeyEvent.VK_BACK_SPACE:
        if(selectionStart < selectionStop ){
          deleteSelection();
          component.dispatchEvent(new TextEvent(component, TextEvent.TEXT_VALUE_CHANGED));
        }
        else if(deleteCaret(position-1)) {
          component.dispatchEvent(new TextEvent(component, TextEvent.TEXT_VALUE_CHANGED));
        }
        break;

      case KeyEvent.VK_DELETE:
        if(selectionStart < selectionStop ){
          deleteSelection();
          component.dispatchEvent(new TextEvent(component, TextEvent.TEXT_VALUE_CHANGED));
        }
        else if(deleteCaret(position)) {
          component.dispatchEvent(new TextEvent(component, TextEvent.TEXT_VALUE_CHANGED));
        }
        break;

	  case KeyEvent.VK_TAB:
          case KeyEvent.VK_UNDEFINED:
        break;

      default:
		wonka.vm.Etc.woempa(9, "#####################"+evt.toString()+"\n");  
		  
        if(selectionStart < selectionStop ){
          insertSelection(evt.getKeyChar());
          
          setCaretPosition(selectionStart+1);
          
          component.dispatchEvent(new TextEvent(component, TextEvent.TEXT_VALUE_CHANGED));
        }
        else if(insertCaret(position, evt.getKeyChar()) ) {
          component.dispatchEvent(new TextEvent(component, TextEvent.TEXT_VALUE_CHANGED));
        }
    }
    paint(getGraphics());
  }

  /*
  ** Key to text helper methods
  */
  
  /*
  ** set caret to desired position, also collapse the selection 
  */
  
  private boolean moveCaret(int newpos){
    if(newpos<0 || newpos>text.length() || newpos==position) {
      return false ;
    }
    //else {
    position = newpos;
    selectionStart = newpos;
    selectionStop = newpos;
    return true;
  }

  /*
  ** set selection start or stop to desired position, update the carret 
  */

  private boolean moveSelection(int newpos){
    if(newpos<0 || newpos>text.length() || newpos==position) {
      return false ;
    }
    else if(position == selectionStart && newpos < selectionStop) {
      position = newpos;
      selectionStart = newpos;
      return true;
    }
    else if(position == selectionStart) { // && newpos > selectionStop) {
      position = newpos;
      selectionStart = selectionStop;
      selectionStop = newpos;
      return true;
    }
    else if(newpos > selectionStart) { //&& position == selectionStop)
      position = newpos;
      selectionStop = newpos;
      return true;
    }
    else { //if(newpos < selectionStart && position == selectionStop)
      position = newpos;
      selectionStop = selectionStart;
      selectionStart = newpos;
      return true;
    }
  }


  /*
  ** Delete the caracter after the given position, also set carret and zero selection to position
  ** This function is protected so it can be overridden by TextField in case we work with masked texts
  */
  
  protected boolean deleteCaret(int newpos){
    if(newpos<0 || newpos>=text.length()) {
      return false ;
    }
    //else {
    position = newpos;
    selectionStart = newpos;
    selectionStop = newpos;
    text = new String(new StringBuffer(text).deleteCharAt(newpos));

    return true;
  }

  /*
  ** Delete the caracter after the given position, also set carret and zero selection to position
  ** This function is protected so it can be overridden by TextField in case we work with masked texts
  */
  
  protected void deleteSelection(){
    text = new String(new StringBuffer(text).delete(selectionStart, selectionStop));
    position = selectionStart;
    //selectionStart = selectionStart;
    selectionStop = selectionStart;
  }

  /*
  ** Insert a new caracter after the given position, also set carret and zero selection just behind character added
  ** This function is protected so it can be overridden by TextField in case we work with masked texts
  */

  protected boolean insertCaret(int newpos, char c){
    if(newpos<0 || newpos>text.length()) {
      return false ;
    }
    //else {
    position = newpos+1;
    selectionStart = position;
    selectionStop = position;
    text = new String(new StringBuffer(text).insert(newpos,c));

    return true;
  }

  /*
  ** replace the current selection by ONE single typde character
  ** This function is protected so it can be overridden by TextField in case we work with masked texts
  */

  protected void insertSelection(char c){
    StringBuffer buf =new StringBuffer(text);
    buf.delete(selectionStart, selectionStop);
    buf.insert(selectionStart,c);
    text = new String(buf);

    position = selectionStart;
    //selectionStart = selectionStart;
    selectionStop = selectionStart;
  }

  public boolean inRange(MouseEvent e) {
    return true;
  }
  
  public boolean isFocusTraversable() {
    return true;
  }
}

