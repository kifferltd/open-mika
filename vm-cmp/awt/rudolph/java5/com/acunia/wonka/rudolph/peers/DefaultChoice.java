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

public class DefaultChoice extends DefaultComponent implements ChoicePeer, ItemListener, MouseListener, FocusListener, KeyListener {

  private static final int ACTION_KEY = KeyEvent.VK_SPACE;

  private List list;
  private Window dropwindow;
  private boolean dropped = false;
  private FontMetrics fm;
  private boolean transferFocus = false;

  public DefaultChoice(Choice choice) {
    super(choice);

    choice.addMouseListener(this);
	choice.addKeyListener(this);

    list = new List();
    list.addItemListener(this);
    
    dropwindow = new Window();
    dropwindow.setLayout(new BorderLayout());
    dropwindow.add(list, BorderLayout.CENTER);
    dropwindow.pack();

    list.addFocusListener(this);
  }

  public void add(String item, int index) {
    list.add(item, index);
    if(list.getItemCount() == 1) paint(getGraphics());
  }
  
  public void addItem(String item, int index) {
    list.add(item, index);
    if(list.getItemCount() == 1) paint(getGraphics());
  }
  
  public void remove(int index) {
    list.remove(index);
  }
  
  public void select(int index) {
    if(index > 0) {
      list.select(index);
    }
    else {
      
    }
    paint(getGraphics());
  }
  
  public void itemStateChanged(ItemEvent e) {
    dropwindow.setVisible(false);
    dropped = false;
   
	Choice choice = (Choice) component;
	choice.requestFocus();
		
   paint(getGraphics());
    
    if(e.getStateChange() == ItemEvent.DESELECTED) {
      String items[] = list.getItems();
      for(int i=0; i < items.length; i++) {
        if(items[i].equals(e.getItem())) {
          list.select(i);
          break;
        }
      }
    }
    component.dispatchEvent(new ItemEvent((ItemSelectable)component, ItemEvent.ITEM_STATE_CHANGED, e.getItem(), ItemEvent.SELECTED));
  }

  public void mouseClicked(MouseEvent event) {
    if(dropped) {
      dropwindow.setVisible(false);
      dropped = false;
      paint(getGraphics());
    }
    else {
      fm = component.getFontMetrics(component.getFont());

      Point p = component.getLocationOnScreen();
      dropped = true;

      paint(getGraphics());
      
      int y = component.getSize().height / 2 - fm.getHeight();
      int w = component.getSize().width - 5;
      int h = fm.getHeight() + 9;
      
      dropwindow.pack();
      dropwindow.setBounds(p.x, p.y + y + h + 1, w, dropwindow.getSize().height);
      dropwindow.show();

      transferFocus = true;
      list.requestFocus();
      transferFocus = false;

    }
  }
  
  public void mouseEntered(MouseEvent event) {
  }
  
  public void mouseExited(MouseEvent event) {
  }
  
  public void mousePressed(MouseEvent event) {
  }
  
  public void mouseReleased(MouseEvent event) {
  }

  public void focusGained(FocusEvent event) {
  }
  
  public void focusLost(FocusEvent event) {
    if(!transferFocus && dropped) {
      dropwindow.setVisible(false);  
      dropped = false;
      paint(getGraphics());
    }
  }
  
  /*
  ** From DefaultComponent :
  */
  
  public Dimension getPreferredSize() {
    Choice choice = (Choice)component;
    Font f = choice.getFont();
    FontMetrics fm = getFontMetrics((f != null) ? f : Component.DEFAULT_FONT);
    int cy = fm.getHeight() + 4;
    int cx = Math.max(dropwindow.getPreferredSize().width + cy, 40);
    /*
    int temp;
    String items[] = list.getItems();
    for(int i=0; i < items.length; i++) {
      temp = fm.stringWidth(items[i]);
      if(temp > cx) cx = temp;
    }
    */
    return new Dimension(cx, cy);
  }

  public void paint(Graphics g) {
    if(g == null) return;

    int x = 0;
    int y = component.getSize().height / 2 - ((g.getFontMetrics().getHeight() + 4 ) / 2);
    int w = component.getSize().width - 5;
    int h = g.getFontMetrics().getHeight() + 4;
    
    if(component.getSize().height - 1 < h) h = component.getSize().height - 1;
    
    int ystr = (h - (g.getFontMetrics().getHeight() + 4)) / 2 + g.getFontMetrics().getAscent() + 2;
    
    String text = list.getSelectedItem();
    
    if(text == null && list.getItemCount() > 0) {
      list.select(0);
      text = list.getItem(0);
    }
   
    if(text == null) text = "<none>";

    /*
    ** Draw the bounding box.
    */
      
    g.setColor(component.getBackground());
    g.fillRect(x, y, w, h);
      
    g.setColor(SystemColor.controlHighlight);
    g.drawLine(x, y + h, x + w - h, y + h);
    g.drawLine(x + w - h, y, x + w - h, y + h);
    g.setColor(SystemColor.controlShadow);
    g.drawLine(x, y, x + w - h, y);
    g.drawLine(x, y, x, y + h);
     
    /*
    ** Draw the text.
    */
      
    g.setClip(x, y, w - h, h);
    
    if(component.isEnabled()) {
      g.setColor(component.getForeground());
    }
    else {
      g.setColor(SystemColor.textInactiveText);
    }

    g.drawString(text, x + 3, y + ystr);

    /*
    ** Draw the button.
    */

    int bx = x + w - h + 1;
    int bw = h;

    if(dropped) {
      g.setColor(SystemColor.control.darker());
    }
    else {
      g.setColor(SystemColor.control);
    }

    g.fillRect(bx, y, bw, h);

    if(dropped) {
      g.setColor(SystemColor.controlHighlight);
    }
    else {
      g.setColor(SystemColor.controlShadow);
    }

    g.drawLine(bx, y + h, bx + bw, y + h);
    g.drawLine(bx + 1, y + h - 1, bx + bw - 1, y + h - 1);
    g.drawLine(bx + bw, y, bx + bw, y + h);
    g.drawLine(bx + bw - 1, y + 1, bx + bw - 1, y + h - 1);
    
    if(dropped) {
      g.setColor(SystemColor.controlShadow);
    }
    else {
      g.setColor(SystemColor.controlHighlight);
    }
    
    g.drawLine(bx, y, bx + bw, y);
    g.drawLine(bx + 1, y + 1, bx + bw - 1, y + 1);
    g.drawLine(bx, y, bx, y + h);
    g.drawLine(bx + 1, y + 1, bx + 1, y + h - 1);

    if(component.isEnabled()) {
      g.setColor(component.getForeground());
    }
    else {
      g.setColor(SystemColor.textInactiveText);
    }

    g.fillRect(bx + bw/4 + 1, y + h/4 + 1, bw/2, h/2);

	super.paint(g);
  }

  public boolean isFocusTraversable() {
    return true;
  }  
  
  public void keyPressed(KeyEvent event)
  {
  }
  
  public void keyReleased(KeyEvent event)
  {
  }
  
  public void keyTyped(KeyEvent event)
  {
	  wonka.vm.Etc.woempa(9, "keyTyped on choice. "+event.paramString());
	  
	if(event.getKeyCode()==ACTION_KEY)
	{
		mouseClicked(null);
	}	  
  }

}

