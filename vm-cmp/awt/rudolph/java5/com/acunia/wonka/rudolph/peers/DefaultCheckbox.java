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

public class DefaultCheckbox extends DefaultComponent implements CheckboxPeer, MouseListener, KeyListener {
  private static final int ACTION_KEY = KeyEvent.VK_SPACE;

  private CheckboxGroup group;
  private String label;
  private boolean state;
  
  public DefaultCheckbox(Checkbox checkbox) {
    super(checkbox);
    checkbox.addMouseListener(this);
	checkbox.addKeyListener(this);
  }

  native private void createPeer();

  public void setCheckboxGroup(CheckboxGroup group) {
    this.group = group;
    paint(getGraphics());
  }
  
  public void setLabel(String label) {
    this.label = label;
    paint(getGraphics());
  }
  
  public void setState(boolean state) {
    this.state = state;
    paint(getGraphics());
  }

  /*
  ** From DefaultComponent :
  */
  
  public Dimension getPreferredSize () {
    Checkbox checkbox = (Checkbox)component;
    FontMetrics fm = getFontMetrics((checkbox.getFont() != null) ? checkbox.getFont() : Component.DEFAULT_FONT);
    int cx = Math.max(60, 30 + fm.stringWidth(checkbox.getLabel()));
    int cy = Math.max(20, 2 * fm.getHeight());
    return new Dimension(cx, cy);
  }
    
  public void paint(Graphics g) {
    if(g == null) return;
    
    int checkboxsize = 12;
    Dimension size = component.getSize();
    Font f = component.getFont();
    FontMetrics fm = getFontMetrics((f != null) ? f : Component.DEFAULT_FONT);

    if(!component.isEnabled()) {
      g.setColor(SystemColor.textInactiveText);
    }

    g.drawString(label, checkboxsize + 5, (size.height - fm.getHeight()) / 2 + fm.getAscent());

    int y = (size.height - checkboxsize) / 2;
    
    if(group != null) {

      /*
      ** It's part of a group, so we draw a nice little round thingie.
      */
      
      g.setColor(Color.white);
      g.fillOval(1, y + 1, checkboxsize - 2, checkboxsize - 2);
      g.setColor(Color.black);
      g.drawOval(1, y + 1, checkboxsize - 2, checkboxsize - 2);

      if(state) {
        g.fillOval(4, y + 4, checkboxsize - 8, checkboxsize - 8);
        g.drawOval(4, y + 4, checkboxsize - 8, checkboxsize - 8);
      }
    }
    else {

      /*
      ** It's not part of a group, then we draw this square thing.
      */
      
      if(state) {
        g.setColor(SystemColor.controlShadow);
      }
      else {
        g.setColor(SystemColor.controlHighlight);
      }

      g.drawLine(0, y, checkboxsize - 1, y);
      g.drawLine(0, y, 0, y + checkboxsize - 1);
      g.drawLine(1, y + 1, checkboxsize - 2, y + 1);
      g.drawLine(1, y + 1, 1, y + checkboxsize - 2);

      if(state) {
        g.setColor(SystemColor.controlHighlight);
      }
      else {
        g.setColor(SystemColor.controlShadow);
      }

      g.drawLine(0, y + checkboxsize - 1, checkboxsize - 1, y + checkboxsize - 1);
      g.drawLine(checkboxsize - 1, y, checkboxsize - 1, y + checkboxsize - 1);
      g.drawLine(1, y + checkboxsize - 2, checkboxsize - 2, y + checkboxsize - 2);
      g.drawLine(checkboxsize - 2, y + 1, checkboxsize - 2, y + checkboxsize - 2);

      if(state) {
        g.setColor(SystemColor.control.darker());
      }
      else {
        g.setColor(SystemColor.control);
      }

      g.fillRect(2, y + 2, checkboxsize - 4, checkboxsize - 4); 
    }

    super.paint(g);
  }
  
  public void mousePressed(MouseEvent me) {
    if(group != null) {
      group.setSelectedCheckbox((Checkbox)component);
    }
    else {
      ((Checkbox)component).setState(!state);
    }

    component.dispatchEvent(new ItemEvent((ItemSelectable)component, ItemEvent.ITEM_STATE_CHANGED, label, state ? ItemEvent.SELECTED : ItemEvent.DESELECTED));
  }

  public void mouseClicked(MouseEvent me) { }
  public void mouseReleased(MouseEvent me) { }
  public void mouseEntered(MouseEvent me) { }
  public void mouseExited(MouseEvent me) { }

  public boolean isFocusTraversable() {
    return true;
  }
  
  public void keyPressed(KeyEvent event)
  {
	  if(event.getKeyCode()==ACTION_KEY)
	  {	  
		if(group != null) {
		  group.setSelectedCheckbox((Checkbox)component);
		}
		else {
		  ((Checkbox)component).setState(!state);
		}

		component.dispatchEvent(new ItemEvent((ItemSelectable)component, ItemEvent.ITEM_STATE_CHANGED, label, state ? ItemEvent.SELECTED : ItemEvent.DESELECTED));
	  }	  
  }
  
  public void keyReleased(KeyEvent event)
  {
  }
  
  public void keyTyped(KeyEvent event)
  {
  }
  
}

