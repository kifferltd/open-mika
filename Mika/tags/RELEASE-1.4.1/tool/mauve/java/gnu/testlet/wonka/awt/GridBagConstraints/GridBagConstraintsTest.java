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
// Created: 2001/11/06

package gnu.testlet.wonka.awt.GridBagConstraints;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.awt.*;

public class GridBagConstraintsTest implements Testlet
  {
  TestHarness harness;


  /*
   * tests the class constructor, public constants and default public fields values.
   */
  void testConstructor()
    {
    harness.checkPoint("GridBagConstraints()");

    GridBagConstraints c = new GridBagConstraints();

    harness.checkPoint("Size Type Constant Fields");
    harness.check(c.RELATIVE, -1, "GridBagConstraints.RELATIVE");
    harness.check(c.REMAINDER, 0, "GridBagConstraints.REMAINDER");

    harness.checkPoint("Fill Type Constant Fields");
    harness.check(c.NONE, 0, "GridBagConstraints.NONE");
    harness.check(c.BOTH, 1, "GridBagConstraints.BOTH");
    harness.check(c.HORIZONTAL, 2, "GridBagConstraints.HORIZONTAL");
    harness.check(c.VERTICAL,   3, "GridBagConstraints.VERTICAL");

    harness.checkPoint("Anchor Type Constant Fields");
    harness.check(c.CENTER,    10, "GridBagConstraints.CENTER");
    harness.check(c.NORTH,     11, "GridBagConstraints.NORTH");
    harness.check(c.NORTHEAST, 12, "GridBagConstraints.NORTHEAST");
    harness.check(c.EAST,      13, "GridBagConstraints.EAST");
    harness.check(c.SOUTHEAST, 14, "GridBagConstraints.SOUTHEAST");
    harness.check(c.SOUTH,     15, "GridBagConstraints.SOUTH");
    harness.check(c.SOUTHWEST, 16, "GridBagConstraints.SOUTHWEST");
    harness.check(c.WEST,      17, "GridBagConstraints.WEST");
    harness.check(c.NORTHWEST, 18, "GridBagConstraints.NORTHWEST");

    harness.checkPoint("Fields");
    harness.check(c.anchor, GridBagConstraints.CENTER, "GridBagConstraints.anchor");
    harness.check(c.fill,   GridBagConstraints.NONE,   "GridBagConstraints.fill");
    harness.check(c.gridwidth,   1, "GridBagConstraints.gridwidth");
    harness.check(c.gridheight,  1, "GridBagConstraints.gridheight");
    harness.check(c.gridx, GridBagConstraints.RELATIVE, "GridBagConstraints.gridx");
    harness.check(c.gridy, GridBagConstraints.RELATIVE, "GridBagConstraints.gridy");
    harness.check(c.insets, new Insets(0,0,0,0), "GridBagConstraints.insets");
    harness.check(c.ipadx,       0, "GridBagConstraints.ipadx");
    harness.check(c.ipady,       0, "GridBagConstraints.ipady");
    harness.check(c.weightx,   0.0, "GridBagConstraints.weightx");
    harness.check(c.weighty,   0.0, "GridBagConstraints.weighty");
    }

  /*
   * tests the clone() method.
   */
  void testClone()
    {
    harness.checkPoint("clone()java.lang.Object");

    GridBagConstraints c = new GridBagConstraints();
    GridBagConstraints cl = (GridBagConstraints)c.clone();
    harness.check( c != cl, "c != c.clone()? - clone()Object");
    harness.check( c.insets != cl.insets, "c.insets != c.clone().insets? - clone()Object");

    // check that fields hav equal values
    harness.check(c.anchor,     cl.anchor, "GridBagConstraints.anchor");
    harness.check(c.fill,       cl.fill, "GridBagConstraints.fill");
    harness.check(c.gridx,      cl.gridx, "GridBagConstraints.gridx");
    harness.check(c.weighty,    cl.weighty, "GridBagConstraints.weighty");
    harness.check(c.insets.top, cl.insets.top, "GridBagConstraints.insets.top");

    // check that c does not change with changes in cl
    cl.anchor = GridBagConstraints.NORTH;
    harness.check(c.anchor, c.CENTER, "GridBagConstraints.anchor");
    cl.fill = 1;
    harness.check(c.fill,        0, "GridBagConstraints.fill");
    cl.gridx = 0;
    harness.check(c.gridx,      -1, "GridBagConstraints.gridx");
    cl.weighty = 1.0;
    harness.check(c.weighty,   0.0, "GridBagConstraints.weighty");
    cl.insets.top = 1;
    harness.check(c.insets.top, 0, "GridBagConstraints.insets.top");

    }

  /*
   * tests the toString() method.
   */
  void testToString()
    {
    harness.checkPoint("toString()java.lang.String");

    GridBagConstraints c = new GridBagConstraints();
//    harness.check( c.toString().equals("java.awt.GridBagConstraints[]"), "toString()java.lang.String");
    }



  public void test (TestHarness newharness)
    {
    harness = newharness;
    harness.setclass("java.awt.GridBagConstraints");
    testConstructor();
    testClone();
//    testToString();
    }
  }
