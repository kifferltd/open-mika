/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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

