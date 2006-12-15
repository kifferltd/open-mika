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


// Author: J. Vandeneede
// Created: 2002/03/24

package com.acunia.wonka.test.awt.Graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class DrawNippon18 extends VisualTestImpl {

  public DrawNippon18() {
  }

  public int test(Graphics g, String s, Font f, int x, int y, Color rectColor) {
    g.setFont(f);
    FontMetrics fm = g.getFontMetrics();
    g.setColor(rectColor);
    g.drawString(s, x, y+fm.getMaxAscent()+1);
    return fm.getHeight()+1;   // +1 to account for the bottom side of the rectangle at y+fm.getHeight()
  }

  public void paint(Graphics g) {
    //hiragana
    String s1 = new String("\u3040\u3041\u3042\u3043\u3044\u3045\u3046\u3047\u3048\u3049\u304A\u304B\u304C\u304D\u304E\u304F\u3050\u3051\u3052\u3053\u3054\u3055\u3056\u3057\u3058\u3059\u305A\u305B");
    String s2 = new String("\u305C\u305D\u305E\u305F\u3060\u3061\u3062\u3063\u3064\u3065\u3066\u3067\u3068\u3069\u306A\u306B\u306C\u306D\u306E\u306F\u3070\u3071\u3072\u3073\u3074\u3075\u3076\u3077");
    String s3 = new String("\u3078\u3079\u307A\u307B\u307C\u307D\u307E\u307F\u3080\u3081\u3082\u3083\u3084\u3085\u3086\u3087\u3088\u3089\u308A\u308B\u308C\u308D\u308E\u308F\u3090\u3091\u3092\u3093");
    String s4 = new String("\u3094\u3095\u3096\u3097\u3098\u3099\u309A\u309B\u309C\u309D\u309E\u309F");
    //katakana
    String s5 = new String("\u30A0\u30A1\u30A2\u30A3\u30A4\u30A5\u30A6\u30A7\u30A8\u30A9\u30AA\u30AB\u30AC\u30AD\u30AE\u30AF\u30B0\u30B1\u30B2\u30B3\u30B4\u30B5\u30B6\u30B7\u30B8\u30B9\u30BA\u30BB");
    String s6 = new String("\u30BC\u30BD\u30BE\u30BF\u30C0\u30C1\u30C2\u30C3\u30C4\u30C5\u30C6\u30C7\u30C8\u30C9\u30CA\u30CB\u30CC\u30CD\u30CE\u30CF\u30D0\u30D1\u30D2\u30D3\u30D4\u30D5\u30D6\u30D7");
    String s7 = new String("\u30D8\u30D9\u30DA\u30DB\u30DC\u30DD\u30DE\u30DF\u30E0\u30E1\u30E2\u30E3\u30E4\u30E5\u30E6\u30E7\u30E8\u30E9\u30EA\u30EB\u30EC\u30ED\u30EE\u30EF\u30F0\u30F1\u30F2\u30F3");
    String s8 = new String("\u30F4\u30F5\u30F6\u30F7\u30F8\u30F9\u30FA\u30FB\u30FC\u30FD\u30FE\u30FF");

    int fh=0;
    int y=0;

    Font f = new Font("nippon18", 0, 18);

    fh=test(g, "Font is 'nippon18'", f, 10 , y, Color.yellow);

    // hiragana
    fh=test(g, "hiragana U+3040-U+309f:", f, 10 , y+=fh+10, Color.blue);
    fh=test(g, s1, f, 10 , y+=fh+10, Color.red);
    fh=test(g, s2, f, 10, y+=fh, Color.white);
    fh=test(g, s3, f, 10, y+=fh, Color.red);
    fh=test(g, s4, f, 10, y+=fh, Color.white);

    // katakana:
    fh=test(g, "katakana U+30A0-U+30ff:", f, 10, y+=fh+10, Color.green);
    fh=test(g, s5, f, 10, y+=fh+10, Color.white);
    fh=test(g, s6, f, 10, y+=fh, Color.red);
    fh=test(g, s7, f, 10, y+=fh, Color.white);
    fh=test(g, s8, f, 10, y+=fh, Color.red);

  }


  static public void main (String[] args) {
    new DrawNippon18();
  }

  public String getHelpText(){
    return ("Drawing of japanese unicode characters is visually tested. On a grey " +
            "background, the user should see the following text, all drawn in characters " +
            "of width 18 and height 18, from font 'nippon18' : \n " +
            "  on a first line, in yellow, the string 'Font is nippon18' \n" +
            "  on a second line, in blue, the string 'hiragana U+3040-U+309f' \n" +
            "  on lines 2 to 5: japanese hiragana characters with unicode in the range U+3040-U+309f \n" +
            "  on line 6, in green, the string 'katakana U+30A0-U+30ff' \n" +
            "  on lines 7 to 10: japanese katakana characters with unicode in the range U+30A0-U+30ff \n" +
            "Characters missing in the font are replaced by a solid rectangle.");
  }

	
  public void start(java.awt.Panel p, boolean b) {
  }

  public void stop(java.awt.Panel p) {
  }
}
