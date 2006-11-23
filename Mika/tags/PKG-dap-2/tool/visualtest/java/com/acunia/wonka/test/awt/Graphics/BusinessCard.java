/**************************************************************************
* Copyright  (c) 2002 by Acunia N.V. All rights reserved.                 *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
* distribution or disclosure of this software is expressly forbidden.     *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Philips-site 5 box 3        info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


// Author: J. Vandeneede
// Created: 2002/10/07

package com.acunia.wonka.test.awt.Graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class BusinessCard extends VisualTestImpl  {

//    Font f1 = new Font("helvR12",1,12);
    Font f2 = new Font("nippon13",1,13);
    Font f1 = f2;
    String s1 = new String("\u30ef\u30a6\u30bf\u30fc\u3000\u30d4\u30fc\u30da\u30eb\u30b9");
    String s2 = new String("\u65e5\u672c\u30fb\u30a2\u30b8\u30a2\u3000\u30d3\u30B8\u30CD\u30B9\u958B\u767A\u30C7\u30A3\u30EC\u30AF\u30BF\u30FC");
    String s3 = new String("\u30D6\u30E9\u30F3\u30C9\u30FB\u901A\u4FE1\u30B0\u30ED\u30FC\u30D0\u30EB\u30C7\u30A3\u30EC\u30AF\u30BF\u30FC");
    String s4 = new String("Philips-site 5 box 3");
    String s5 = new String("3001 Leuven - Belgium");
    String s61 = new String("\u96FB\u8A71\u3000\u3000\u3000\u3000\u3000");
    String s62 = new String("+32-16-31 00 20");
    String s71 = new String("\u643A\u5E2F\u96FB\u8A71\u3000\u3000\u3000");
    String s72 = new String("+32-478-33 56 32");
    String s81 = new String("Fax");
    String s82 = new String("\u3000\u3000\u3000");
    String s83 = new String("+32-16-58 26 56");
    String s9 = new String("e-mail: wouter.piepers@acunia.com");
    String s10 = new String("www.acunia.com");


  public BusinessCard() {
    setBackground(Color.white);
  }

  public int drawCardString(Graphics g, String s, Font f, int x, int y, Color c) {
    g.setColor(c);
    g.setFont(f);
    FontMetrics fm = g.getFontMetrics();
    g.drawString(s, x, y+fm.getMaxAscent()+1);
    return fm.getHeight()+1;   // +1 to account for the bottom side of the rectangle at y+fm.getHeight()
  }
  public void drawCardBorder(Graphics g, int x, int y, int w, int h) {
    g.setColor(Color.black);
    g.drawRect(x, y, w, h);
    g.drawLine(x+w+1, y+1, x+w+1, y+h+1);
    g.drawLine(x+w+2, y+2, x+w+2, y+h+2);
    g.drawLine(x+1, y+h+1, x+w+1, y+h+1);
    g.drawLine(x+2, y+h+2, x+w+2, y+h+2);
  }

  public void paint(Graphics g) {

    int fh=0;
    int y=0;
    Color c = new Color(0xCC, 0x00, 0x33);

    fh=drawCardString(g, "Japanese Business card", f1, 10 , y, Color.black);

    fh=drawCardString(g, s1, f2, 20 , y+=fh+20, c);
    fh=drawCardString(g, s2, f2, 20 , y+=fh, c);
    fh=drawCardString(g, s3, f2, 20 , y+=fh, c);
    fh=drawCardString(g, s4, f1, 20 , y+=fh, c);
    fh=drawCardString(g, s5, f1, 20 , y+=fh, c);
    fh=drawCardString(g, s61, f2, 20 , y+=fh, c);
    fh=drawCardString(g, s62, f1, 10+getWidth()/4 , y, c);
    fh=drawCardString(g, s71, f2, 20 , y+=fh, c);
    fh=drawCardString(g, s72, f1, 10+getWidth()/4 , y, c);
    fh=drawCardString(g, s81, f1, 20 , y+=fh, c);
    fh=drawCardString(g, s83, f1, 10+getWidth()/4 , y, c);
    fh=drawCardString(g, s9, f1, 20 , y+=fh, c);
    fh=drawCardString(g, s10, f1, 20 , y+=fh, c);

    drawCardBorder(g, 10, 26, getWidth()-20, getHeight()-40);

  }


  static public void main (String[] args) {
    new BusinessCard();
  }

  public String getHelpText(){
    return ("The test demonstrates the use of wonka's font nippon13 for drawing a japanese business card");
  }

	
  public void start(java.awt.Panel p, boolean b) {
  }

  public void stop(java.awt.Panel p) {
  }
}
