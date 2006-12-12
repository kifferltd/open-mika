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

public class ReplicateScaleFilter extends VisualTestImpl {

  private Image original;
  private Image pic1;
  private Image pic2;
  private Image pic3;
    
  public ReplicateScaleFilter() {
    super();
    String path = System.getProperty("vte.image.path", "{}/test/image");
    original = Toolkit.getDefaultToolkit().getImage(path + "/lena1.png");

    int w = original.getWidth(null);
    int h = original.getHeight(null);
    
    pic1 = this.createImage(new FilteredImageSource(original.getSource(), new java.awt.image.ReplicateScaleFilter(w * 2, h * 2)));
    pic2 = this.createImage(new FilteredImageSource(original.getSource(), new java.awt.image.ReplicateScaleFilter(w / 2, h * 2)));
    pic3 = this.createImage(new FilteredImageSource(original.getSource(), new java.awt.image.ReplicateScaleFilter(w * 2, h / 2)));
    
    this.setBackground(Color.black);
    this.repaint();
  }
    
  public void paint(Graphics g) {
    g.setColor(Color.white);
     
    g.drawImage(original, 2, 5, null);
    g.drawImage(pic1, 127, 75, null);
    g.drawImage(pic2, 62, 75, null);
    g.drawImage(pic3, 127, 38, null);
  }
    
  public String getHelpText(){
    return "";
  }

}

