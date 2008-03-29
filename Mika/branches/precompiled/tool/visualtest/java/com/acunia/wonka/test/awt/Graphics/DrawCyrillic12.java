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
* Cyrillic test derived from Nippon test 2008 by Chris Gray.              *
*                                                                         *
**************************************************************************/


// Author: J. Vandeneede
// Created: 2002/03/24

package com.acunia.wonka.test.awt.Graphics;

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;
import java.io.UnsupportedEncodingException;

public class DrawCyrillic12 extends VisualTestImpl {

  public DrawCyrillic12() {
  }

  public FontMetrics writeString(Graphics g, String s, Font f, int x, int y, Color rectColor) {
    g.setFont(f);
    FontMetrics fm = g.getFontMetrics();
    g.setColor(rectColor);
    g.drawString(s, x, y+fm.getMaxAscent()+1);
    return fm;
  }

  public void paint(Graphics g) {
    Font f = new Font("helvR12", 0, 12);
    int y = 0;
    int x = 12;

    try {
      for (int i = 0; i < 5; ++i) {
        String s = new String(DrawCyrillicText.nepoj[i], "UTF8");
        FontMetrics fm = writeString(g, s, f, x , y, Color.blue);
        y += fm.getHeight();
      }
      f = new Font("helvB12", 0, 12);
      for (int i = 5; i < 10; ++i) {
        String s = new String(DrawCyrillicText.nepoj[i], "UTF8");
        FontMetrics fm = writeString(g, s, f, x , y, Color.blue);
        y += fm.getHeight();
      }
    } catch (UnsupportedEncodingException uee) {
    }
  }


  static public void main (String[] args) {
    new DrawCyrillic12();
  }

  public String getHelpText(){
    return ("Drawing of Cyrillic characters is visually tested. " +
            "On a grey background, the user should see a poem by Pushkin " +
            "in twelve-point blue cyrillic type.\n" +
            "\n" +
            "Do not sing, my beauty, to me\n" +
            "your sad songs of Georgia;\n" +
            "they remind me\n" +
            "of that other life and distant shore.\n" +
            "\n" +
            "Alas, They remind me,\n" +
            "your cruel melodies,\n" +
            "of the steppe, the night and moonlit\n" +
            "features of a poor, distant maiden!\n" +
            "\nAt the time of writing this poem was available online at:\n" +
            "http://www.stihi-rus.ru/Pushkin/stihi/153.htm\n" +
            "http://www.zhurnal.ru/magister/library/pushkin/poetry/pu0549.htm\n"
           );
  }

	
  public void start(java.awt.Panel p, boolean b) {
  }

  public void stop(java.awt.Panel p) {
  }
}



