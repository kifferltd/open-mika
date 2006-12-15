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
// Created: 2001/05/14

package com.acunia.wonka.test.awt.Graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class DrawString extends VisualTestImpl {

  public DrawString() {
  }

  public int test(Graphics g, String s, Font f, int x, int y, Color rectColor) {
    g.setFont(f);
    FontMetrics fm = g.getFontMetrics();
    g.setColor(rectColor);
    g.drawRect(x, y, fm.stringWidth(s), fm.getHeight());
    g.setColor(Color.black);
    g.drawLine(x, y+fm.getMaxAscent()+1, x+fm.stringWidth(s), y+fm.getMaxAscent()+1);
    g.setColor(Color.blue);
    g.drawString(s, x, y+fm.getMaxAscent()+1);
    return fm.getHeight()+1;   // +1 to account for the bottom side of the rectangle at y+fm.getHeight()
  }

  public void paint(Graphics g) {
    String s = "abc-def-ghi-jkl-mno-pqr";
    int fh=0;
    int y=0;

    // Helvetica:
    fh=test(g, s, new Font("helvR08", 0, 8), 10 , y, Color.red);
    fh=test(g, s, new Font("helvR14", 0, 14), 10, y+=fh, Color.white);
    fh=test(g, s, new Font("helvR20", 0, 20), 10, y+=fh, Color.red);

    g.setColor(Color.white);
    g.drawRect(10, y+=fh, 200, 15);

    // Courier:
    fh=test(g, s, new Font("courR10", 0, 10), 10, y+=16, Color.red);
    fh=test(g, s, new Font("courR14", 0, 14), 10, y+=fh, Color.white);
    fh=test(g, s, new Font("courR17", 0, 17), 10, y+=fh, Color.red);
    fh=test(g, s, new Font("courR24", 0, 24), 10, y+=fh, Color.white);

    g.setColor(Color.red);
    g.drawRect(10, y+=fh, 200, 15);

    // Helvetica bold:
    fh=test(g, s, new Font("helvB20", 1, 20), 10, y+=16, Color.white);

  }


  static public void main (String[] args) {
    new DrawString();
  }

  public String getHelpText(){
    return ("This test tests Graphics.drawString() in combination with Graphics.setFont() " +
            "and Graphics.getFontMetrics(). A string is shown in different fonts, surrounded " +
            "by alternating red and white rectangles of heights equal to the fonts' heights. " +
            "The top three 'Helvetica' fonts are separated by a white rectangle from next four " +
            "'Courier' fonts. These four fonts are separated from the last 'bold' 'Helvetica' " +
            "font by a red rectangle. The fonts' baselines are drawn in black. " +
            "The test is successful 1) if and only if the characters' ascents and descents do not span " +
            "beyond and not even overlap the bounding box, 2) if the baseline occupies pixel locations " +
            "exactly below bottom pixels of letters 'a', 'b', 'c', 'd', 'e' and 'f', 3) if the baseline " +
            "does not span beyond the bounding box and 4) if the string to be drawn in the bottom white box " +
            "is visible (is considered inside its bounding box).");
  }

	
  public void start(java.awt.Panel p, boolean b) {
  }

  public void stop(java.awt.Panel p) {
  }
}
