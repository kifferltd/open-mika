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

package com.acunia.wonka.test.awt.layout;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import com.acunia.wonka.test.awt.VisualTestImpl;
import com.acunia.wonka.test.awt.Graphics.DrawString2;

public class OffLayout2 extends VisualTestImpl implements ComponentListener {

  private VisualTestImpl vt = new DrawString2();

  public OffLayout2() {
    addComponentListener(this);
    setLayout(null);
    add(vt);
  }

  public void componentHidden(ComponentEvent event) {
  }
  
  public void componentMoved(ComponentEvent event) {
  }
  
  public void componentResized(ComponentEvent event) {
    Dimension size = getSize();
    vt.setBounds(-15, -15, size.width + 30, size.height + 30);
  }
  
  public void componentShown(ComponentEvent event) {
  }

  public String getHelpText() {
    return "beu!";
  }

}
