package com.acunia.wonka.rudolph.popupkeyboard;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class KeyboardListener extends MouseAdapter {

  private static Keyboard keyboard;

  private TextComponent textField;

  public KeyboardListener(TextComponent textField){
    this.textField = textField;
    textField.addMouseListener(this);
  }

  public void mousePressed(MouseEvent e) {
    if(textField.isEditable()){

      if(!((com.acunia.wonka.rudolph.peers.DefaultTextComponent)textField.getPeer()).inRange(e)) {
        return;
      }
      
      if(keyboard == null) {
        Component iter = textField;
        while(!(iter instanceof Frame)) iter = iter.getParent();
        keyboard = new Keyboard((Frame)iter);
      }
      keyboard.open(textField);
    }
  }
}
