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

package com.acunia.wonka.test.awt.Graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class DrawImageScaled extends VisualTestImpl {

  private Image original;
  private int w;
  private int h;
    
  public DrawImageScaled() {
    super();
    String path = System.getProperty("vte.image.path", "{}/test/image");
    original = Toolkit.getDefaultToolkit().getImage(path + "/lena1.png");

    w = original.getWidth(null);
    h = original.getHeight(null);
    
    this.setBackground(Color.black);
    this.repaint();
  }
    
  public void paint(Graphics g) {
    g.setColor(Color.white);
     
    g.drawImage(original, 2, 5, w, h, null);
    g.drawImage(original, 127, 75, w * 2, h * 2, null);
    g.drawImage(original, 62, 75, w / 2, h * 2, null);
    g.drawImage(original, 127, 38, w * 2, h / 2, null);
  }
    
  public String getHelpText(){
    return "";
  }

}

