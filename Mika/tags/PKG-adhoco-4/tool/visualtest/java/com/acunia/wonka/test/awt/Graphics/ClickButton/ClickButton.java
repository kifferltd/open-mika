/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

package com.acunia.wonka.test.awt.Graphics.ClickButton;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

public class ClickButton extends Panel implements MouseListener {

  protected String value;
  protected int status;
  private Color shadowUp;
  private Color shadowDown;
  private int indent=0;
  private Image imageUp = null;
  private Image imageDown = null;

  //private FontTypeInterface fontArray = new Macintosh_12_0();

  private Vector listeners;
  public static final int STATUS_DISABLED = 0;
  public static final int STATUS_DOWN = 1;
  public static final int STATUS_UP = 2;
  public static final int ALIGN_CENTER = 1;
  public static final int ALIGN_LEFT = 2;
  public static final int ALIGN_RIGHT = 3;

  public ClickButton(String value) {
     this(value, Color.green, Color.orange);
  }

  public ClickButton(String value, Color b, Color f) {
    this.setVisible(false);
    this.value = value;

    status = STATUS_UP;

    this.setBackground(b);
    this.setForeground(f);

    shadowUp = new Color(200,200,200);
    shadowDown = new Color(100,100,100);

    listeners = new Vector();

    this.addMouseListener(this);
    this.setBounds(0, 0, getMinimumSize().width, getMinimumSize().height);
    this.setVisible(true);
  }

  public ClickButton(String value, int indent) {
    this(value);
    this.indent = indent;
  }

  public Dimension getMinimumSize() {

    if(imageUp!=null) {
      return new Dimension(imageUp.getWidth(null)+20, imageUp.getHeight(null)+10);
    }
    else {
      return new Dimension(value.length()*12,40+indent*4);
    }
  }

  public Dimension getPreferredSize() {
    return new Dimension(getMinimumSize().width, getMinimumSize().height);
  }

  protected void processClickButtonEvent() {
    for(int i=0; i<listeners.size(); i++) {
      ClickButtonListener cbl = (ClickButtonListener)listeners.elementAt(i);
      cbl.buttonPressed(new ClickButtonEvent(this, this.value));
    }
  }

  public void addListener(ClickButtonListener cbl) {
    listeners.addElement(cbl);
  }

  public void removeListener(ClickButtonListener cbl) {
    listeners.removeElement(cbl);
  }

  public void paint(Graphics g) {
    switch(status) {
      case STATUS_DISABLED:
        paintDisabled(g);
        break;
      case STATUS_DOWN:
        paintDown(g);
        break;
      default:
        paintUp(g);
    }
  }

  public void paintDisabled(Graphics g) {
    paintUp(g);
  }

  public void paintUp(Graphics g) {
    if (imageUp == null) {
      g.setColor(shadowUp);
      g.drawLine(indent, indent, (int)super.getSize().getWidth()-1-indent*2, indent);
      g.drawLine(indent, indent, indent, (int)super.getSize().getHeight()-1-indent*2);
      g.setColor(shadowDown);
      g.drawLine((int)super.getSize().getWidth()-1-indent*2, (int)super.getSize().getHeight()-1-indent*2, (int)super.getSize().getWidth()-1-indent*2, indent);
      g.drawLine((int)super.getSize().getWidth()-1-indent*2, (int)super.getSize().getHeight()-1-indent*2, indent, (int)super.getSize().getHeight()-1-indent*2);
      g.setColor(getForeground());
//      g.setFont(new Font("Arial", Font.PLAIN, 12));
      g.setFont(new Font("helvR20", Font.PLAIN, 20));
      g.drawString(value, (int)(super.getSize().getWidth()-/*g.getFontMetrics().getStringBounds(value,g).getWidth()*/value.length()*6)/2, (int)(super.getSize().getHeight()/2+/*g.getFontMetrics().getStringBounds(value,g).getHeight()*/12/4));
    }
    else {
      g.drawImage(imageUp, (this.getWidth()-imageUp.getWidth(null))/2,(this.getHeight()-imageUp.getHeight(null))/2,null);
    }
  }

  public void paintDown(Graphics g) {
    if (imageDown == null) {
      g.setColor(shadowDown);
      g.drawLine(indent, indent, (int)super.getSize().getWidth()-1-indent*2, indent);
      g.drawLine(indent, indent, indent, (int)super.getSize().getHeight()-1-indent*2);
      g.setColor(shadowUp);
      g.drawLine((int)super.getSize().getWidth()-1-indent*2, (int)super.getSize().getHeight()-1-indent*2, (int)super.getSize().getWidth()-1-indent*2, indent);
      g.drawLine((int)super.getSize().getWidth()-1-indent*2, (int)super.getSize().getHeight()-1-indent*2, indent, (int)super.getSize().getHeight()-1-indent*2);
      g.setColor(getForeground());
//      g.setFont(new Font("Arial", Font.BOLD, 12));
      g.setFont(new Font("helvB20", Font.BOLD, 20));
      g.drawString(value, (int)(super.getSize().getWidth()-/*g.getFontMetrics().getStringBounds(value,g).getWidth()*/value.length()*7)/2, (int)(super.getSize().getHeight()/2+/*g.getFontMetrics().getStringBounds(value,g).getHeight()*/12/4)-2);
    }
    else {
      g.drawImage(imageDown, (this.getWidth()-imageDown.getWidth(null))/2,(this.getHeight()-imageDown.getHeight(null))/2,null);
    }
  }

  public void mousePressed(MouseEvent e) {
    buttonPressed();
    repaint();
  }

  public void mouseReleased(MouseEvent e) {
    buttonReleased();
    repaint();
  }

  protected void buttonPressed() {
    if(status==STATUS_UP) {
      processClickButtonEvent();
      status=STATUS_DOWN;
    }
  }

  protected void buttonReleased() {
    if(status==STATUS_DOWN) {
      status=STATUS_UP;
    }
  }

  public void mouseEntered(MouseEvent e){}

  public void mouseExited(MouseEvent e){}

  public void mouseClicked(MouseEvent e){}

  public void setStatus(int status) {
    this.status = status;
    repaint();
  }

  public void setValue(String value) {
    this.value = value;
    repaint();
  }

  public String getValue() {
    return value;
  }

  public void setImageUp(Image image) {
    this.setBounds(0, 0, getMinimumSize().width, getMinimumSize().height);
    this.imageUp = image;
    repaint();
  }

  public void setImageDown(Image image) {
    this.setBounds(0, 0, getMinimumSize().width, getMinimumSize().height);
    this.imageDown = image;
    repaint();
  }

  public int getStatus() {
    return status;
  }

}

