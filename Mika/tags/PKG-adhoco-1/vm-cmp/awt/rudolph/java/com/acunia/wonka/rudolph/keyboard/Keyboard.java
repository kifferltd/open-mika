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

public class Keyboard extends Panel implements MouseListener, FocusListener {

  protected int kbdWidth = 240;
  protected int kbdHeight = 81;

  public Keyboard() {
    buildKbd();
    addMouseListener(this);
    addFocusListener(this);
  }

  protected void buildKbd() {
  }

  public Dimension getPreferredSize() {
    return new Dimension(kbdWidth, kbdHeight);
  }

  public Dimension getMinimumSize() {
    return new Dimension(kbdWidth, kbdHeight);
  }

  public Dimension getMaximumSize() {
    return new Dimension(kbdWidth, kbdHeight);
  }

  public void open() {
  }

  public void close() {
  }

  /*
  ** Mouse events.
  */
  
  public void mouseClicked(MouseEvent event) {
  }
  
  public void mouseEntered(MouseEvent event) {
  }
  
  public void mouseExited(MouseEvent event) {
  }
  
  public void mousePressed(MouseEvent event) {
  }
  
  public void mouseReleased(MouseEvent event) {
  }
 
  public void focusGained(FocusEvent evt) {
    Component.revertFocus();
  }

  public void focusLost(FocusEvent evt) {
  }
  
}

