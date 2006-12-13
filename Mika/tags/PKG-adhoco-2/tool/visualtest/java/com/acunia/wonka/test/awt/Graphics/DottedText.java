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
import com.acunia.wonka.test.awt.*;
import com.acunia.wonka.test.awt.Graphics.dottedtext.*;

public class DottedText extends VisualTestImpl
{

  public PixelDisplay pd1;
  public PixelDisplay pd2;

  public DottedText()
  {
    super();

    DotArray da = new DotArray(80, 50);
    da.drawString("RUDOLPH", 10, 5, new Macintosh_12_0());
    pd1 = new PixelDisplay(da);
    pd1.setDotSize(4);
    pd1.setShowBorder(false);
    pd1.setBackground(Color.magenta);

    da = new DotArray(130, 40);
    da.drawString("scanning ...", 30, 0, new Macintosh_12_0());
    pd2 = new PixelDisplay(da);
    pd2.setDotSize(2);
    pd2.setShowBorder(false);
    pd2.setDelay(100);
    pd2.setShiftDirection(PixelDisplay.DIRECTION_LEFT_RIGHT);
    pd2.setActiveColor(Color.red);
//    pd2.setBackground(Color.red);

    this.setLayout(new GridLayout(2, 1));
    this.add(pd1);
    this.add(pd2);
  }

  public String getHelpText(){
    return ("This program tests painting in an off-screen image created by an object of type " +
            "\"Canvas\". To be successful this test should show : a frame vertically divided " +
            "in two \"Canvases\" of equal size. The image on the upper Canvas should have " +
            "inherited the background of the Canvas which is magenta. It displays the yellow text " +
            "\"RUDOLPH\" using a font of characters composed of individually visible dots of " +
            "size 4. The text scrolls from left to right over the screen. The image on the lower " +
            "Canvas should have a grey default background, since the background of that Canvas is " +
            "not set. It displays the red text \"scanning\", using the same dotted " +
            "font, this time with dotsize 2. The text scrolls from right to left over the screen." );
  }

  public void start(java.awt.Panel p, boolean autorun){
    pd1.start();
    pd2.start();
//    System.out.println("DottedText.start() called");
  }

  public void stop(java.awt.Panel p){
    pd1.stop();
    pd2.stop();
//    System.out.println("DottedText.stop() called");
  }

  public static void main(String args[])  {
    new DottedText();
  }
}
