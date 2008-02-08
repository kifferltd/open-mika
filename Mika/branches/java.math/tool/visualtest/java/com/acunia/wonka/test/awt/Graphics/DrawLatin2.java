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
*                                                                         *
* Latin2 test derived from Nippon test 2006 by Chris Gray.                *
*                                                                         *
**************************************************************************/


// Author: J. Vandeneede
// Created: 2002/03/24

package com.acunia.wonka.test.awt.Graphics;

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;

public class DrawLatin2 extends VisualTestImpl {

  public DrawLatin2() {
  }

  public FontMetrics writeString(Graphics g, String s, Font f, int x, int y, Color rectColor) {
    g.setFont(f);
    FontMetrics fm = g.getFontMetrics();
    g.setColor(rectColor);
    g.drawString(s, x, y+fm.getMaxAscent()+1);
    return fm;
  }

  public void paint(Graphics g) {
    String s1a = new String("Czech/Slovak:");
    String s1b = new String("\u0161\u0165\u017e\u013e\u010d\u011b\u010f\u0148\u0159\u016f\u013a");
    String s1c = new String("\u0160\u0164\u017d\u013d\u010c\u011a\u010e\u0147\u0158\u016e\u0139");
    String s2a = new String("Polish:");
    String s2b = new String("\u0142\u0105\u017c\u0119\u0107\u0144\u015b\u017a");
    String s2c = new String("\u0141\u0104\u017b\u0118\u0106\u0143\u015a\u0179");
    String s3a = new String("Romanian:");
    String s3b = new String("\u0102\u0103\u015e\u015f\u0162\u0163");
    String s4a = new String("Croatian/Slovenian:");
    String s4b = new String("\u0161\u010d\u017e\u0107\u0111");
    String s4c = new String("\u0160\u010c\u017d\u0106\u0110");
    String s5a = new String("Hungarian:");
    String s5b = new String("\u0150\u0151\u0170\u0171");
    String s6 = new String("Tv\u00e1\u0159\u00ed v tv\u00e1\u0159 vyhl\u00e1\u0161ce mi literatura p\u0159ipad\u00e1 stra\u0161n\u011b zbyte\u010dn\u00e1.");
    String s7a = new String("Dzi\u0119ki niemu Polska po odzyskaniu niepodleg\u0142o\u015bci mia\u0142a dost\u0119p do");
    String s7b = new String("Morza Ba\u0142tyckiego.");
    String s8a = new String("Primele apari\u0163ii ale lui Ionescu sunt \u00cen limba rom\u00e2n\u0103, cu poezii publicate");
    String s8b = new String("\u00cen revista Bilete de papagal (1928-1931) a lui Tudor Arghezi, articole de");
    String s8c = new String("critic\u0103 literar\u0103 \u015fi o \u00cencercare de epic\u0103 umoristic\u0103, Hugoliada: Via\u0163a grotesc\u0103");
    String s8d = new String("\u015fi tragic\u0103 a lui Victor Hugo.");

    Font f1 = new Font("helvB12", 0, 12);
    Font f2 = new Font("helvR12", 0, 12);
    Font f3 = new Font("helvR08", 0, 8);
    FontMetrics fm;
    int y = 0;
    int x = 10;

    fm = writeString(g, s1a, f1, x , y, Color.red);
    x += fm.stringWidth(s1a) + 12;
    fm = writeString(g, s1b, f2, x, y, Color.red);
    x += fm.stringWidth(s1b) + 12;
    fm = writeString(g, s1c, f2, x , y, Color.red);
    y += fm.getHeight();

    x = 10;
    fm = writeString(g, s2a, f1, x, y, Color.white);
    x += fm.stringWidth(s2a) + 12;
    fm = writeString(g, s2b, f2, x, y, Color.white);
    x += fm.stringWidth(s2b) + 12;
    fm = writeString(g, s2c, f2, x, y, Color.white);
    y += fm.getHeight();

    x = 10;
    fm = writeString(g, s3a, f1, x, y, Color.blue);
    x += fm.stringWidth(s3a) + 12;
    fm = writeString(g, s3b, f2, x, y, Color.blue);
    x += fm.stringWidth(s3b) + 24;
    fm = writeString(g, s5a, f1, x, y, Color.green);
    x += fm.stringWidth(s5a) + 12;
    fm = writeString(g, s5b, f2,x, y, Color.green);
    y += fm.getHeight();


    x = 10;
    fm = writeString(g, s4a, f1, x, y, Color.yellow);
    x += fm.stringWidth(s4a) + 12;
    fm = writeString(g, s4b, f2, x, y, Color.yellow);
    x += fm.stringWidth(s4b) + 12;
    fm = writeString(g, s4c, f2, x, y, Color.yellow);
    y += fm.getHeight();

    x = 10;
    fm = writeString(g, s6, f3, 10, y, Color.red);
    y += fm.getHeight();
    fm = writeString(g, s7a, f3, 10, y, Color.white);
    y += fm.getHeight();
    fm = writeString(g, s7b, f3, 10, y, Color.white);
    y += fm.getHeight();
    fm = writeString(g, s8a, f3, 10, y, Color.blue);
    y += fm.getHeight();
    fm = writeString(g, s8b, f3, 10, y, Color.blue);
    y += fm.getHeight();
    fm = writeString(g, s8c, f3, 10, y, Color.blue);
    y += fm.getHeight();
    fm = writeString(g, s8d, f3, 10, y, Color.blue);
    y += fm.getHeight();

  }


  static public void main (String[] args) {
    new DrawLatin2();
  }

  public String getHelpText(){
    return ("Drawing of Latin-2 characters is visually tested. On a grey " +
            "background, the user should see test strings resembling the " +
            " illustrations at <http://www.slovo.info/testlat2.htm>, " +
            "followed by three sentences in Czech, Polish, and Romanian.");
  }

	
  public void start(java.awt.Panel p, boolean b) {
  }

  public void stop(java.awt.Panel p) {
  }
}

