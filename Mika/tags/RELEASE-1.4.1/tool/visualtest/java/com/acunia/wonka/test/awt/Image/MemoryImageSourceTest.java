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

package com.acunia.wonka.test.awt.Image;

import java.awt.*;
import java.awt.image.*;
import com.acunia.wonka.test.awt.*;

public class MemoryImageSourceTest extends VisualTestImpl {

  Image img;
  
  public MemoryImageSourceTest() {
    super();
   
    int w = 100;
    int h = 100;
    int pix[] = new int[w * h];
    int index = 0;
    for (int y = 0; y < h; y++) {
      int red = (y * 255) / (h - 1);
      for (int x = 0; x < w; x++) {
        int blue = (x * 255) / (w - 1);
        pix[index++] = (255 << 24) | (red << 16) | blue;
      }
    }
    
    img = createImage(new MemoryImageSource(w, h, pix, 0, w));
    
    this.setBackground(Color.black);
    this.repaint();
  }
  
  public void paint(Graphics g) {
    g.drawImage(img, 10, 10, null);
  }
  
  public String getHelpText(){
    return "You should see a square with a gradient.\n" +
           "The top left should be black\n" +
           "The bottom left should be red\n" +
           "The top right should be blue\n" +
           "The bottom right should be purple\n";
  }

}

