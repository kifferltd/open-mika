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

import java.awt.*;
import java.awt.image.*;
import com.acunia.wonka.test.awt.*;

public class DrawImageClipped2 extends VisualTestImpl {

  public class FadeFilter extends java.awt.image.RGBImageFilter {
    public int filterRGB(int x, int y, int rgb) {
      return (rgb & 0x00ffffff) | 0xA0000000;
    }
  }
  
  private Image original;
  private Image faded;
  private int w;
  private int h;
    
  public DrawImageClipped2() {
    super();
    String path = System.getProperty("vte.image.path", "{}/test/image");
    original = Toolkit.getDefaultToolkit().getImage(path + "/lena1.png");
    faded = createImage(new FilteredImageSource(original.getSource(), new FadeFilter()));

    w = original.getWidth(null);
    h = original.getHeight(null);
    
    this.setBackground(Color.black);
    this.repaint();
  }
    
  public void paint(Graphics g) {
    g.setColor(Color.white);
   
    g.drawImage(original, 10, 10, null);
    g.drawRect(20, 20, w - 20, h - 20);
    g.drawRect(30, 30, w - 40, h - 40);

    int xspace = 10 * (w * 2) / (w - 20);
    int yspace = 10 * (h * 2) / (h - 20);

    g.drawImage(faded, 120, 80, 120 + w * 2, 80 + h * 2, 10, 10, w - 10, h - 10, null);
   
    g.setClip(120 + xspace, 80 + yspace, (w - xspace) * 2, (h - yspace) * 2);
    g.drawImage(original, 120, 80, 120 + w * 2, 80 + h * 2, 10, 10, w - 10, h - 10, null);
    g.setClip(0, 0, -1, -1);
    
    g.setColor(Color.white);
    g.drawRect(120, 80, w * 2, h * 2);
    g.drawRect(120 + xspace, 80 + yspace, (w - xspace) * 2, (h - yspace) * 2);

  }
    
  public String getHelpText(){
    return "";
  }

}

