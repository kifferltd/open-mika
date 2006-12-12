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
// Created: 2003/01/23

package gnu.testlet.wonka.awt.Color;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.awt.*;

public class ColorTest implements Testlet
  {
  TestHarness harness;

  /**
   * tests the decode() method.
   */
  void testDecode()
    {
    harness.checkPoint("decode(java.lang.String)java.awt.Color");

    String[] colorStrings = {
    "#7f7f7f",
    "0x7f7f7f",
    "0X7f7f7f",
    "8355711",              // 7f7f7f in decimal
    "037677577",            // 7f7f7f in octal
    "0x7f,0x7f,0x7f",       // such strings are not accpted by sun's implementation
    "127,127,127",          // and should not be supported according to the sun specs.
    "0177,0177,0177"
    };

    Color conv;

    for (int c = 0; c < colorStrings.length; c++) {
      conv = Color.decode(colorStrings[c]);

//    harness.debug(colorStrings[c] + " : " + conv.getRed() + " " + conv.getGreen() + " " + conv.getBlue());
      harness.check(conv.getRed() == 127 && conv.getGreen() == 127 && conv.getBlue() == 127, "boolean int.operator == (int)");
    }

    conv = Color.decode("0177177177");   // remark : three times octal value 127 is not the number that transforms to rgb = (127,127,127) |
    harness.check(conv.getRed() == 252 && conv.getGreen() == 254 && conv.getBlue() == 127, "boolean int.operator == (int)");
    }


  public void test (TestHarness newharness)
    {
    harness = newharness;
    harness.setclass("java.awt.Color");
    testDecode();
    }
  }
