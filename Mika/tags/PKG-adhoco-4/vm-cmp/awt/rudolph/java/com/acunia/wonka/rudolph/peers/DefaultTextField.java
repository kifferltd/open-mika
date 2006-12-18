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

public class DefaultTextField extends DefaultTextComponent implements TextFieldPeer, FocusListener, MouseListener, MouseMotionListener, KeyListener {

  private boolean focus;
  private String  str;

  private int      cols;
  private char     echo;
  private int      xoffset;
  private boolean  selecting;
  private int      selstart;
  private int      selstop;
  
  public DefaultTextField(TextField textField) {
    super(textField);
    textField.addFocusListener(this);
    textField.addMouseListener(this);
    textField.addMouseMotionListener(this);
    textField.addKeyListener(this);
  }

  public Dimension getMinimumSize(int cols) {
    return getPreferredSize(cols);
  }
  
  public Dimension getPreferredSize(int cols) {
    Dimension preferredSize;
    synchronized(component.getTreeLock()) {
      Font f = component.getFont();
      FontMetrics fm = getFontMetrics((f != null) ? f : Component.DEFAULT_FONT);

      int cx = Math.max(30, cols * 8);
      int cy = Math.max(10, fm.getHeight() + 4);

      preferredSize = new Dimension(cx, cy);
    }
    return preferredSize;
  }

  public void setEchoChar(char c) {
    echo = c;   
    setText(text);
  }

  public void setCaretPosition(int newposition) {
    super.setCaretPosition(newposition);
    paint(getGraphics());
  }
  
  /*
  ** Deprecated
  */

  public Dimension minimumSize(int cols) {
    return null;
  }
  
  public Dimension preferredSize(int cols) {
    return null;
  }
  
  public void setEchoCharacter(char c) {
  }
 
  /*
  ** From TextComponent
  */
  
  public void setText(String text) {
    str = text;
    if (echo != 0) {
      this.text = echo(str);
    }
    else {
      this.text = str;
    }
    super.setText(this.text);
    paint(getGraphics());
  }
  
  private String echo(String s) {
    int length = s.length();
    char[] sb = new char[length];
    for (int i = 0; i < length; i++) {
      sb[i] = echo;
    }
    return new String(sb);
  }

  public String getText() {
    return (echo != 0) ? str : text;
  }
  
  /*
  ** paint.
  */

  public void paint(Graphics g) {
    if(g == null) return;

    int y = component.getSize().height / 2 - ((g.getFontMetrics().getHeight() + 4) / 2);
    int w = component.getSize().width - 1;
    int h = g.getFontMetrics().getHeight() + 4;
    
    if(component.getSize().height - 1 < h) h = component.getSize().height - 1;
      
    int ystr = (h - (g.getFontMetrics().getHeight() + 4)) / 2 + g.getFontMetrics().getAscent() + 2;

    int carretx = g.getFontMetrics().stringWidth(text.substring(0, getCaretPosition()));
   
    /*
    ** Make sure the caret is visible.
    */
    
    if(xoffset > (w - 4) - carretx) {
      xoffset =  (w - 4) - carretx;
    }

    if(xoffset < - carretx) {
      xoffset = w - 4 - carretx;
      if(xoffset > 0) {
        xoffset = 0;
      }
    }

    /*
    ** Draw the bounding box.
    */
      
    g.setColor(component.getBackground());
    g.fillRect(0, y, w, h);
      
    g.setColor(SystemColor.controlHighlight);
    g.drawLine(0, y + h, w, y + h);
    g.drawLine(w, y, w, y + h);
    g.setColor(SystemColor.controlShadow);
    g.drawLine(0, y, w, y);
    g.drawLine(0, y, 0, y + h);
     
    /*
    ** Draw the text.
    */
      
    g.setClip(2, y, w - 4, h);
     
    if(component.isEnabled()) {
      g.setColor(component.getForeground());
    }
    else {
      g.setColor(SystemColor.textInactiveText);
    }

    g.drawString(text, xoffset + 2, y + ystr);

    /*
    ** Draw the selection (if there is any and we have focus)
    */
    if(getSelectionEnd() - getSelectionStart() > 0 && focus) {
      int sx = g.getFontMetrics().stringWidth(text.substring(0, getSelectionStart()));
      int ex = g.getFontMetrics().stringWidth(text.substring(0, getSelectionEnd()));

      g.setColor(new Color(80, 80, 80));
      g.fillRect(sx + xoffset + 2, y + 2, ex - sx, h - 4);
        
      g.setColor(Color.white);
      g.setClip(sx + xoffset + 2, y, ex - sx, h);
      g.drawString(text, xoffset + 2, y + ystr);
        
      g.setClip(0, y, w, h);
      g.setColor(Color.black);
    }

    /*
    ** Draw the cursor 
     
     
     (if we have focus).
    */

    if(focus && ((TextField)component).isEditable()) {
      g.drawLine(carretx + xoffset + 2, y + 2, carretx + xoffset + 2, y + h - 2);
      g.drawLine(carretx + xoffset + 3, y + 2, carretx + xoffset + 3, y + h - 2);
    }

    super.paint(g);
  }

  /*
  ** Focus Events.
  */

  public void focusGained(FocusEvent fe) {
    focus = true;
    paint(getGraphics());
  }
  
  public void focusLost(FocusEvent fe) {
    focus = false;
    paint(getGraphics());
  }

  /*
  ** Mouse Events.
  */

  private int getNearestPos(int x) {
    int i = 0;
    Font f = component.getFont();
    FontMetrics fm = getFontMetrics((f != null) ? f : Component.DEFAULT_FONT);
    int length = text.length();
    while(i <= length) {
      if(x - 2 - xoffset < fm.stringWidth(text.substring(0, i))) {
        return i - 1;
      }
      i++;
    }
    return length;
  }

  private void doSelection() {
    if(selstart <= selstop) {
      select(selstart, selstop);
    }
    else {
      select(selstop, selstart);
    }
  }

  public void mouseEntered(MouseEvent me) {}
  public void mouseExited(MouseEvent me) {}
  public void mouseMoved(MouseEvent me) {}
  public void mouseClicked(MouseEvent me) {}
  
  public void mousePressed(MouseEvent me) {
    selstart = getNearestPos(me.getX());
    selstop = getNearestPos(me.getX());
    doSelection();
    setCaretPosition(selstart);
    paint(getGraphics());
  }
  
  public void mouseReleased(MouseEvent me) {
    selstop = getNearestPos(me.getX());
    doSelection();
    setCaretPosition(selstop);
    paint(getGraphics());
  }
  
  public void mouseDragged(MouseEvent me) {
    selstop = getNearestPos(me.getX());
    doSelection();
    setCaretPosition(selstop);
    paint(getGraphics());
  }
  
  public void keyTyped(KeyEvent evt) {
    switch(evt.getKeyCode()) {
      case KeyEvent.VK_ENTER:
        component.dispatchEvent(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, ""));
        break;

      default:
        super.keyTyped(evt);
    }
  }
  
  /*
  ** Overrides the protected function <delete the character after caret> of TextComponent
  ** reason: if echo character set, we should also delete the according character from the according echo string
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
    if (echo != 0) {
      str = new String(new StringBuffer(str).deleteCharAt(newpos));
    }
    return true;
  }

  /*
  ** Overrides the protected function <delete the characters between selection start and selection stop> of TextComponent
  ** reason: if echo character set, we should also delete the according characters from the according echo string
  */
  
  protected void deleteSelection(){
    text = new String(new StringBuffer(text).delete(selectionStart, selectionStop));
    if (echo != 0) {
      str = new String(new StringBuffer(str).delete(selectionStart, selectionStop));
    }
    position = selectionStart;
    //selectionStart = selectionStart;
    selectionStop = selectionStart;
  }

  /*
  ** Overrides the protected function <insert this character after caret> of TextComponent
  ** reason: if echo character set, we chould NOT type the given chaqracter but the textfield echo char
  */

  protected boolean insertCaret(int newpos, char c){
    if(newpos<0 || newpos>text.length()) {
      return false ;
    }
    //else {
    position = newpos+1;
    selectionStart = position;
    selectionStop = position;
    if (echo != 0) {
      str = new String(new StringBuffer(str).insert(newpos,c));
      text = new String(new StringBuffer(text).insert(newpos,echo));
    }
    else {
      text = new String(new StringBuffer(text).insert(newpos,c));
    }
    return true;
  }

  /*
  ** Overrides the protected function <replace this selection by the one given character> of TextComponent
  ** reason: if echo character set, we chould NOT display the given chaqracter but the Textfield echo char
  */

  protected void insertSelection(char c){
    StringBuffer buf =new StringBuffer(text);
    buf.delete(selectionStart, selectionStop);
    if (echo != 0) {
      buf.insert(selectionStart,echo);
      StringBuffer plain =new StringBuffer(str);
      plain.delete(selectionStart, selectionStop);
      plain.insert(selectionStart,c);
      str=new String(plain);
    }
    else {
      buf.insert(selectionStart,c);
    }
    text = new String(buf);

    position = selectionStart;
    selectionStop = selectionStart;
  }

  public boolean inRange(MouseEvent e) {
    return (new Rectangle(
      0, // x
      component.getSize().height / 2 - ((component.getGraphics().getFontMetrics().getHeight() + 4) / 2), // y
      component.getSize().width - 1, // width
      component.getGraphics().getFontMetrics().getHeight() + 4  // height
    )).contains(e.getX(), e.getY());
  }
}

