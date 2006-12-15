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

public class DrawImageClipped extends VisualTestImpl {

  public class FadeFilter extends java.awt.image.RGBImageFilter {
    public int filterRGB(int x, int y, int rgb) {
      return (rgb & 0x00ffffff) | 0x60000000;
    }
  }
  
  private Image original;
  private Image faded;
  private int w;
  private int h;
    
  public DrawImageClipped() {
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
    
    g.drawImage(faded, 2, 5, null);
    g.drawImage(faded, 127, 75, w * 2, h * 2, null);
    g.drawImage(faded, 62, 75, w / 2, h * 2, null);
    g.drawImage(faded, 127, 38, w * 2, h / 2, null);
    
    g.setClip(2, 5, w / 2, h / 2);
    g.drawImage(original, 2, 5, null);
    g.setClip(2 + w / 2, 5 + h / 2, w / 2, h / 2);
    g.drawImage(original, 2, 5, null);

    g.setClip(127, 75, w, h);
    g.drawImage(original, 127, 75, w * 2, h * 2, null);
    g.setClip(127 + w, 75 + h, w, h);
    g.drawImage(original, 127, 75, w * 2, h * 2, null);
    
    g.setClip(62, 75, w / 4, h);
    g.drawImage(original, 62, 75, w / 2, h * 2, null);
    g.setClip(62 + w / 4, 75 + h, w / 4, h);
    g.drawImage(original, 62, 75, w / 2, h * 2, null);
    
    g.setClip(127, 38, w, h / 4);
    g.drawImage(original, 127, 38, w * 2, h / 2, null);
    g.setClip(127 + w, 38 + h / 4, w, h / 4);
    g.drawImage(original, 127, 38, w * 2, h / 2, null);
  }
    
  public String getHelpText(){
    return "";
  }

}

