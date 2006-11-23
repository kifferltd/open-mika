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

// Author: M. Bloch, 2002/04/12

package com.acunia.wonka.test.awt.layout;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;

public class OffLayout extends VisualTestImpl implements ComponentListener {

  public final int BUTTONSIZE = 50;

  private Vector buttons = new Vector();

  private void drawButton(String label, int cx, int cy) {
    Button b = new Button(label);
    b.setBounds(cx-(BUTTONSIZE/2), cy-(BUTTONSIZE/2), BUTTONSIZE, BUTTONSIZE);
    add(b);
    buttons.add(b);
  }

  private void changeButton(Component b, int cx, int cy) {
    b.setBounds(cx-(BUTTONSIZE/2), cy-(BUTTONSIZE/2), BUTTONSIZE, BUTTONSIZE);
  }

  private void showButtons() {
    int w = getSize().width;
    int h = getSize().height;
    
    Iterator iter = buttons.iterator();
    
    changeButton((Component)iter.next(), 0, 0);
    changeButton((Component)iter.next(), w/2, 0);
    changeButton((Component)iter.next(), w, 0);
    changeButton((Component)iter.next(), w, h/2);
    changeButton((Component)iter.next(), w, h);
    changeButton((Component)iter.next(), w/2, h);
    changeButton((Component)iter.next(), 0, h);
    changeButton((Component)iter.next(), 0, h/2);
  }

  public OffLayout() {
    super();
    int w = 350;
    int h = 210;
    
    setLayout(null);
    
    drawButton("NorthWest", 0, 0);
    drawButton("North",     w/2, 0);
    drawButton("NorthEast", w, 0);
    drawButton("East",      w, h/2);
    drawButton("SouthEast", w, h);
    drawButton("South",     w/2, h);
    drawButton("SouthWest", 0, h);
    drawButton("West",      0, h/2);

    addComponentListener(this);
  }
  
  public void componentHidden(ComponentEvent event) {
  }
  
  public void componentMoved(ComponentEvent event) {
  }
  
  public void componentResized(ComponentEvent event) {
    showButtons();
  }
  
  public void componentShown(ComponentEvent event) {
  }
  
  public String getHelpText() {
    return "Here you should be able to test that the AWT can correctly handle "+
           "components laid out without a layout manager, and also off the "+
           "side of their containing components.";
  }

}
