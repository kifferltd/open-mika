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
// Created: 2001/11/05

package gnu.testlet.wonka.awt.Insets;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.awt.*;

public class InsetsTest implements Testlet
  {
  TestHarness harness;


  /*
   * tests the class constructor and the class' public fields.
   */
  void testConstructor()
    {
    harness.checkPoint("Insets(int,int,int,int)");

    Insets i = new Insets(1, 2, 3, 4);
    harness.check(i != null, "new Insets(1, 2, 3, 4) != null ? - Insets(int, int, int, int)");
    harness.check(i.top,  1, "Insets.top");
    harness.check(i.left, 2, "Insets.left");
    harness.check(i.bottom, 3, "Insets.bottom");
    harness.check(i.right,  4, "Insets.right");
    }

  /*
   * tests the equals() method.
   */
  void testEquals()
    {
    harness.checkPoint("equals(java.lang.Object)boolean");

    Insets i = new Insets(1, 2, 3, 4);
    harness.check( !i.equals(new Button()), "!new Insets(1, 2, 3, 4).equals(new Button())? - equals(Object)boolean");
    harness.check( !i.equals(new Insets(3, 4, 5, 6)), "!new Insets(1, 2, 3, 4).equals(new Insets(3, 4, 5, 6))? - equals(Object)boolean");
    harness.check(  i.equals(new Insets(1, 2, 3, 4)), "new Insets(1, 2, 3, 4).equals(new Insets(1, 2, 3, 4))? - equals(Object)boolean");
    }

  /*
   * tests the clone() method.
   */
  void testClone()
    {
    harness.checkPoint("clone()java.lang.Object");

    Insets i = new Insets(1, 2, 3, 4);
    harness.check( i.equals(i.clone()), "i.equals(i.clone())? - clone()Object");
    harness.check( i != (Insets)i.clone(), "i != (Insets)i.clone()? - clone()Object");
    }

  /*
   * tests the toString() method.
   */
  void testToString()
    {
    harness.checkPoint("toString()java.lang.String");

    Insets i1 = new Insets(1, 2, 3, 4);
//  harness.debug(i1.toString());
    harness.check( i1.toString().equals("java.awt.Insets[top = 1, left = 2, bottom = 3, right = 4]"), "toString()java.lang.String");
    }



  public void test (TestHarness newharness)
    {
    harness = newharness;
    harness.setclass("java.awt.Insets");
    testConstructor();
    testEquals();
    testClone();
    testToString();
    }
  }
