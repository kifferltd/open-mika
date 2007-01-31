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

package java.awt;

import java.awt.event.*;
import java.awt.peer.*;

public class TextComponent extends Component {

  /*
  ** common Variables for all derived classes
  */

  protected String text;
  protected int position;
  protected int selectionStart;
  protected int selectionStop;
  protected boolean editable = true;

  public transient TextListener textListener;

  public TextComponent() {
    super();
  }

  /*
  ** set carret position
  */

  public void setCaretPosition(int newposition) {
    ((TextComponentPeer)peer).setCaretPosition(newposition);
    position = ((TextComponentPeer)peer).getCaretPosition();
  }

  /*
  ** get carret position
  */
  
  public int getCaretPosition() {
    position = ((TextComponentPeer)peer).getCaretPosition();
    return position;
  }

  /*
  ** set selection start
  */

  public synchronized void setSelectionStart(int newstart) {
    selectionStop = ((TextComponentPeer)peer).getSelectionEnd();
    ((TextComponentPeer)peer).select(newstart, selectionStop);
    selectionStart = ((TextComponentPeer)peer).getSelectionStart();
  }

  /*
  ** get selection start
  */
  
  public synchronized int getSelectionStart() {
    selectionStart = ((TextComponentPeer)peer).getSelectionStart();
    return selectionStart;
  }

  /*
  ** set selection end
  */

  public synchronized void setSelectionEnd(int newstop) {
    selectionStart = ((TextComponentPeer)peer).getSelectionStart();
    ((TextComponentPeer)peer).select(selectionStart, newstop);
    selectionStop = ((TextComponentPeer)peer).getSelectionEnd();
  }

  /*
  ** get selection end
  */
  
  public synchronized int getSelectionEnd() {
    selectionStop = ((TextComponentPeer)peer).getSelectionEnd();
    return selectionStop;
  }

  /*
  ** set selection start and end
  */
  
  public synchronized void select(int newstart, int newstop) {
      paint(getGraphics());
    
    ((TextComponentPeer)peer).select(newstart, newstop);
  }

  /*
  ** set selection start and end to select the whole text
  */
  
  public synchronized void selectAll() {
    selectionStart = 0;
    text = ((TextComponentPeer)peer).getText();
    selectionStop = text.length();
    
    setCaretPosition(selectionStop);
    
    ((TextComponentPeer)peer).select(selectionStart, selectionStop);
    
    paint(getGraphics());
  }

  /*
  ** Text, selected text
  * override setText in TextArea and TextField to show the desired text
  */
  
  public synchronized void setText(String newtext) {
    if(newtext == null){
      newtext = "";
    }
    ((TextComponentPeer)peer).setText(newtext);
    text = newtext;
    if(position > text.length()) position = text.length();
  }

  public synchronized String getText() {
    text = ((TextComponentPeer)peer).getText();
    return text;
  }

  public synchronized String getSelectedText()  {
    selectionStart = ((TextComponentPeer)peer).getSelectionStart();
    selectionStop = ((TextComponentPeer)peer).getSelectionEnd();
    text = ((TextComponentPeer)peer).getText();
    if(selectionStart >= selectionStop) {
      return "";
    }
    else {
      return text.substring(selectionStart, selectionStop);
    }
  }

  /*
  ** Editable
  */
  
  public synchronized void setEditable(boolean condition) {
    ((TextComponentPeer)peer).setEditable(condition);
    this.editable = condition;
  }

  public boolean isEditable() {
    return editable;
  }
  
  public String toString() {
    String display = getClass().getName()+" text["+text;
    display += (editable)?"] editable selection <":"] not editable selection <";
    return display+selectionStart+", "+selectionStop+">";
  }

  protected String paramString() {
    return(getClass().getName()+" text["+text+"], caret("+position+") selection<"+selectionStart+", "+selectionStop+">");
  }

  /*
  ** Handling and throwing TextEvents
  ** @status implemented

  /*
  ** Call on AWTEventMulticaster to return either the new listener or a Multicaster TextListener that contains it 
  */
  
  public synchronized void addTextListener(TextListener newlistener) {
    textListener = AWTEventMulticaster.add(textListener, newlistener);
  }

  /*
  ** Call on AWTEventMulticaster to remove the listener from the Multicaster TextListener that contains it 
  */
  
  public void removeTextListener(TextListener oldlistener) {
    textListener = AWTEventMulticaster.remove(textListener, oldlistener);
  }

  /*
  ** overrides Component.processEvent to forward TextEvents to our own listener or Multicaster  
  */
  
  protected void processEvent(AWTEvent e) {
    if(e instanceof TextEvent) {
      if(textListener != null) {
        textListener.textValueChanged((TextEvent)e);
      }
    }
    else {
      super.processEvent(e);
    }
  }

  /*
  ** throw a textevent using the current textListener. 
  */
  
  protected void processTextEvent(TextEvent e) {
    if(textListener != null) {
      textListener.textValueChanged(e);
    }
  }
}

