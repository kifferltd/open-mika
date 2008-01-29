/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package java.awt;

import java.awt.event.*;
import java.awt.peer.*;

public class TextField extends TextComponent {

  private int  cols;
  private char echo;

  public TextField() {
    this("", 0);
  }

  public TextField(int cols) {
    this("", cols);
  }

  public TextField(String text) {
    this(text, 0);
  }

  public TextField(String text, int cols) {
    if(text == null){
      text = "";
    }
    this.text = text;
    this.cols = cols;

    setForeground(Color.black);
    setBackground(Color.white);

    ((TextFieldPeer)peer).setText(text);
  }

  public void addNotify() {
    if(peer == null) {
      peer = getToolkit().createTextField(this);
    }

    if (notified == false) {
      super.addNotify();
    }
  }

  /*
  ** @status As the action event of a textfield is triggered by the <return> key, not every functions is operational already
  */
   
  public synchronized void addActionListener(java.awt.event.ActionListener newlistener) {
    actionListener = AWTEventMulticaster.add(actionListener, newlistener);
  }

  public synchronized void removeActionListener(java.awt.event.ActionListener oldlistener) {
    actionListener = AWTEventMulticaster.remove(actionListener, oldlistener);
  }

  protected void processEvent(AWTEvent e) {
    if(e instanceof  ActionEvent) {
      processActionEvent((ActionEvent)e);
    }
    else {
      super.processEvent(e);
    }
  }

  protected void processActionEvent(java.awt.event.ActionEvent event) {
    if(actionListener != null) {
       actionListener.actionPerformed(event);
    }
  }

  /*
  ** The echo character.
  */
  
  public boolean echoCharIsSet() {
    return (echo != 0) ? true : false;
  }
  
  public char getEchoChar() {
    return echo;
  }
  
  public void setEchoChar(char ch) {
    ((TextFieldPeer)peer).setEchoChar(ch);
    echo = ch;   
  }  

  /*
  ** Columns.
  */
  
  public int getColumns() {
    return cols;
  }
  
  public void setColumns(int cols) {
    this.cols = cols;
  }

  /*
  ** Component Size.
  */
  
  public Dimension getMinimumSize() {
    return getMinimumSize(cols);
  }
  
  public Dimension getPreferredSize() {
    return getPreferredSize(cols);
  }
  
  public Dimension getMinimumSize(int cols) {
    return ((TextFieldPeer)peer).getMinimumSize(cols);
  }
  
  public Dimension getPreferredSize(int cols) {
    return ((TextFieldPeer)peer).getPreferredSize(cols);
  }

  /*
  ** toString & paramString.
  */
  
  public String toString() {
    String display = getClass().getName() +" length = "+ cols +" cols text = ["+ text;
    display += (editable) ? "] editable selection <" : "] not editable selection <";
    return display+selectionStart+", "+selectionStop+">";
  }

  protected String paramString() {
    return (getClass().getName()+" cols="+ cols +" text["+ text +"], caret("+ position +") selection<"+ selectionStart +", "+ selectionStop +">");
  }

}

