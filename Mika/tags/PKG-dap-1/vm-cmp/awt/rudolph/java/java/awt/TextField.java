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
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. Permission is hereby granted to reproduce, modify, and       *
* distribute these modifications under the terms of the Wonka Public      *
* Licence.                                                                *
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

