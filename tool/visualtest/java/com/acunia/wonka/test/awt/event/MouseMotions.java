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


// Author: D. Buytaert
// Created: 2001/05/03

package com.acunia.wonka.test.awt.event;

import com.acunia.wonka.test.awt.*;
import java.awt.*;
import java.awt.event.*;

public class MouseMotions extends VisualTestImpl implements MouseListener, MouseMotionListener {

  private Label motion;
  private int count;  

  public MouseMotions() {
    setLayout(new BorderLayout());

    motion = new Label("");
    motion.setBackground(Color.yellow);
    add(motion, BorderLayout.NORTH);

    addMouseListener(this);
    addMouseMotionListener(this);
  }

  public void mouseClicked(MouseEvent event) {
    motion.setText("Mouse cliked ("+ count++ +").");
  }
  
  public void mouseEntered(MouseEvent event) {
    motion.setText("Mouse entered ("+ count++ +").");
  }
  
  public void mouseExited(MouseEvent event) {
    motion.setText("Mouse exited ("+ count++ +").");
  }
  
  public void mousePressed(MouseEvent event) {
    motion.setText("Mouse pressed ("+ count++ +").");
  }
  
  public void mouseReleased(MouseEvent event) {
    motion.setText("Mouse released ("+ count++ +").");
  }

  public void mouseMoved(MouseEvent event) {
    motion.setText("Mouse moved ("+ count++ +").");
  }

  public void mouseDragged(MouseEvent event) {
    motion.setText("Mouse dragged ("+ count++ +").");
  }

  public String getHelpText(){
    return "Click and move around and see what the mouse event can tell you.";
  }
}
