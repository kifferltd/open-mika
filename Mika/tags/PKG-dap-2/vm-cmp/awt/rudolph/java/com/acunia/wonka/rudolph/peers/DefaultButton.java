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

public class DefaultButton extends DefaultComponent implements ButtonPeer, MouseListener, KeyListener {
 
  private static final int ACTION_KEY = KeyEvent.VK_SPACE;
 
  private String label;
  private boolean pressed;
  private boolean drawpressed;

  public DefaultButton(Button button) {
    super(button);
    button.addMouseListener(this);
	button.addKeyListener(this);
  }

  private static final int MIN_BTN_X = getMinBtnWidth();
  private static final int MIN_BTN_Y = getMinBtnHeight();
  
  private static final float WIDTH_FACTOR = getBtnWidthFactor();
  private static final float HEIGHT_FACTOR = getBtnHeightFactor();  

  private static float getBtnWidthFactor()
  {
      final float defaultValue = 4/3;
      
      try
      {
        return Float.parseFloat(System.getProperty("java.awt.Button.widthFactor", defaultValue+""));
      }
      catch(NumberFormatException ex)
      {
          System.err.println("-Djava.awt.Button.widthFactor must be a float");
          ex.printStackTrace();
          
          return defaultValue;
      }
  }
  
  private static float getBtnHeightFactor()
  {
      final float defaultValue = 2.0f;
      
      try
      {
        return Float.parseFloat(System.getProperty("java.awt.Button.heightFactor", defaultValue+""));
      }
      catch(NumberFormatException ex)
      {
          System.err.println("-Djava.awt.Button.heightFactor must be a float");
          ex.printStackTrace();
          
          return defaultValue;
      }
  }    
  
  private static int getMinBtnWidth()
  {
      final int defaultValue = 30;
      
      try
      {
        return Integer.parseInt(System.getProperty("java.awt.Button.minWidth", defaultValue+""));
      }
      catch(NumberFormatException ex)
      {
          System.err.println("-Djava.awt.Button.minWidth must be an integer");
          ex.printStackTrace();
          
          return defaultValue;
      }
  }
  
  private static int getMinBtnHeight()
  {
      final int defaultValue = 20;
      
      try
      {
        return Integer.parseInt(System.getProperty("java.awt.Button.minHeight", defaultValue+""));
      }
      catch(NumberFormatException ex)
      {
          System.err.println("-Djava.awt.Button.minHeight must be an integer");
          ex.printStackTrace();
          
          return defaultValue;
      }
  }  
  
  public Dimension getMinimumSize() {
    Button button = (Button)component;
    FontMetrics fm = button.getFontMetrics(button.getFont());
    int cx = Math.max(MIN_BTN_X, (int) (WIDTH_FACTOR * fm.stringWidth(button.getLabel())));
    int cy = Math.max(MIN_BTN_Y, (int) (HEIGHT_FACTOR * fm.getHeight()));

    return new Dimension(cx, cy);
  }
  
  public Dimension getPreferredSize () {
    Button button = (Button)component;
    FontMetrics fm = button.getFontMetrics(button.getFont());
    int cx = Math.max(MIN_BTN_X, (int) (WIDTH_FACTOR * fm.stringWidth(button.getLabel())));
    int cy = Math.max(MIN_BTN_Y, (int) (HEIGHT_FACTOR * fm.getHeight()));

    return new Dimension(cx, cy);
  }

  public void setLabel(String label) {
    this.label = label;
    paint(getGraphics());
  }

  public void paint(Graphics g) {
    if(g == null) return;

    int w;
    int h;

    Dimension dim = component.getSize();
    w = dim.width;
    h = dim.height;

    if(label == null) label = ((Button)component).getLabel();
  
    g.clearRect(0, 0, w, h);
    
    if(drawpressed) {
      g.setColor(SystemColor.controlShadow);
    }
    else {
      g.setColor(SystemColor.controlHighlight);
    }

    g.drawLine(0, 0, w - 1, 0);
    g.drawLine(0, 0, 0, h - 1);
    g.drawLine(1, 1, w - 2, 1);
    g.drawLine(1, 1, 1, h - 2);

    if(drawpressed) {
      g.setColor(SystemColor.controlHighlight);
    }
    else {
      g.setColor(SystemColor.controlShadow);
    }

    g.drawLine(0, h - 1, w - 1, h - 1);
    g.drawLine(w - 1, 0, w - 1, h - 1);
    g.drawLine(1, h - 2, w - 2, h - 2);
    g.drawLine(w - 2, 1, w - 2, h - 2);

    if(component.isEnabled()) {
      g.setColor(component.getForeground());
    }
    else {
      g.setColor(SystemColor.textInactiveText);
    }

    FontMetrics fm = g.getFontMetrics();
    g.drawString(label, (w - fm.stringWidth(label)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
    
    //if(!pressed) super.paint(g);
	super.paint(g);
  }
  
  public void mouseClicked(MouseEvent me) {}
  
  public void mousePressed(MouseEvent me) {
    pressed = true;
    drawpressed = true;
    paint(getGraphics());
  }
  
  public void mouseReleased(MouseEvent me) {
    boolean action = drawpressed;
    pressed = false;
    drawpressed = false;
    paint(getGraphics());
    if(action) component.dispatchEvent(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, ""));
  }
  
  public void mouseEntered(MouseEvent me) {
    if(pressed) drawpressed = true;
    paint(getGraphics());
  }
  
  public void mouseExited(MouseEvent me) {
    if(pressed) drawpressed = false;
    paint(getGraphics());
  }

  public void setEnabled(boolean enable) {
    drawpressed = false;
    pressed = false;
  }
  
  public boolean isFocusTraversable() {
    return true;
  }
  
  public void keyPressed(KeyEvent event)
  {
	  if(event.getKeyCode()==ACTION_KEY)
	  {
		pressed = true;
		drawpressed = true;
		paint(getGraphics());		  
	  }
  }
  
  public void keyReleased(KeyEvent event)
  {
	  if(event.getKeyCode()==ACTION_KEY)
	  {
		boolean action = drawpressed;
		pressed = false;
		drawpressed = false;
		paint(getGraphics());
		if(action) component.dispatchEvent(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, ""));		  
	  }
  }
  
  public void keyTyped(KeyEvent event)
  {
  }
  
}

