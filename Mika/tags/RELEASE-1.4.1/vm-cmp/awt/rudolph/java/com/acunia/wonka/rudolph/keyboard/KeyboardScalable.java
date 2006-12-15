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

package com.acunia.wonka.rudolph.keyboard;

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.rudolph.taskbar.*;

public class KeyboardScalable extends KeyboardDefault {

  private TextField textfield;
  private TextComponent target;

  public KeyboardScalable() {
    super();
  }

  protected void buildKbd() {
    super.buildKbd(); 

    setLayout(null);
    textfield = new TextField();
    textfield.setFont(new Font("Helvetica", Font.PLAIN, 12));
    
    kbdWidth = Integer.parseInt(TaskBar.getTaskBar().getProperties().getProperty("applet.keyboard.width", "240"));
    kbdHeight = Integer.parseInt(TaskBar.getTaskBar().getProperties().getProperty("applet.keyboard.height", "81"));
    kbdHeight -= textfield.getPreferredSize().height;

    add(textfield);
    textfield.setBounds(2, 0, kbdWidth - 4, textfield.getPreferredSize().height);

    for(int i=0; i < keys_nrs.size(); i++) {
      ((KeyboardButton)keys_nrs.elementAt(i)).setScale(kbdWidth, kbdHeight, 240, 81);
      ((KeyboardButton)keys_nrs.elementAt(i)).setTranslate(0, textfield.getPreferredSize().height);
    }
    for(int i=0; i < keys_low.size(); i++) {
      ((KeyboardButton)keys_low.elementAt(i)).setScale(kbdWidth, kbdHeight, 240, 81);
      ((KeyboardButton)keys_low.elementAt(i)).setTranslate(0, textfield.getPreferredSize().height);
    }
    for(int i=0; i < keys_cap.size(); i++) {
      ((KeyboardButton)keys_cap.elementAt(i)).setScale(kbdWidth, kbdHeight, 240, 81);
      ((KeyboardButton)keys_cap.elementAt(i)).setTranslate(0, textfield.getPreferredSize().height);
    }
  }
/*
  public void paint(Graphics g) {
    Iterator iter = current_keys.iterator();
    while(iter.hasNext()) {
      KeyboardButton key = (KeyboardButton)iter.next();
      key.paint(g);
    }
  }
*/
  public void open() {
    super.open();
    lastSource = Component.getFocusComponent();
    textfield.requestFocus();
    if(lastSource instanceof TextComponent) {
      target = (TextComponent)lastSource;
      textfield.setText(target.getText());
      textfield.setCaretPosition(target.getCaretPosition());
      textfield.select(target.getSelectionStart(), target.getSelectionEnd());
    }
  }
 
  public void close() {
    super.close();
    if(target != null) {
      target.requestFocus();
    }
  }

  public void sendKeyEvent(Component source, int id, int modifiers, int keyCode, char keyChar) {
    if(target != null) {
      target.setCaretPosition(textfield.getCaretPosition());
      target.select(textfield.getSelectionStart(), textfield.getSelectionEnd());
      super.sendKeyEvent(target, id, modifiers, keyCode, keyChar);
      textfield.setText(target.getText());
      textfield.setCaretPosition(target.getCaretPosition());
      textfield.select(target.getSelectionStart(), target.getSelectionEnd());
    }
  }

  public void mousePressed(MouseEvent e) {
    textfield.requestFocus();
    super.mousePressed(e);
  }
  
}

