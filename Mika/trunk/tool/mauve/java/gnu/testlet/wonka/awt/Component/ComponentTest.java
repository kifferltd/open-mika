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
// Created: 2001/01/23

package gnu.testlet.wonka.awt.Component;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.Properties;
import java.awt.*;

public class ComponentTest implements Testlet
  {
  TestHarness harness;


  /*
  ** tests the method 'getGraphics', and the inherited 'font' and 'foreground' color
  ** of the resulting Graphics object.
  */
  void testGetGraphics(){

    harness.checkPoint("getGraphics()java.awt.Graphics");

    Panel f = new Panel();

    Graphics g = f.getGraphics();
    harness.check(g!=null, "getGraphics()java.awt.Graphics");

    harness.check(g.getFont() != null, "getFont()java.awt.Font");
    harness.check(g.getFont(), Component.DEFAULT_FONT, "getFont()java.awt.Font"); // equal to default
    //harness.checkPoint("getColor()java.awt.Color");

    harness.check(g.getColor() != null, "getColor()java.awt.Color");
    harness.check(g.getColor(), new Color(0,0,0), "getColor()java.awt.Color");

    Color color = new Color(255,0,0);
    f.setForeground(color);
    Font font = new Font("helvP14", Font.PLAIN, 18);
    f.setFont(font);

    // g must have remained unchanged
    harness.check(g.getColor(), new Color(0,0,0), "getColor()java.awt.Color");
    harness.check(!(g.getColor().equals( f.getForeground())), "getColor()java.awt.Color");

    g.setColor(new Color(0, 255, 0));
    harness.check(g.getColor(), new Color(0,255,0), "getColor()java.awt.Color");
    //harness.checkPoint("getFont()java.awt.Font");
    harness.check(g.getFont(), Component.DEFAULT_FONT, "getFont()java.awt.Font");  // equal to default font
    g.setFont(new Font("helvB21", Font.BOLD, 26));
    harness.check(g.getFont(), new Font("helvB21", Font.BOLD, 26), "getFont()java.awt.Font");

    g = f.getGraphics();
    // new g should have the new font and color.
    harness.check(g.getFont(), font, "getFont()java.awt.Font");
    //harness.checkPoint("getColor()java.awt.Color");
    harness.check(g.getColor(), color, "getColor()java.awt.Color");
    harness.check(g.getColor(), f.getForeground(), "getColor()java.awt.Color");
  }

  void testBounds() {
    int x = 10;
    int y = 20;
    int w = 30;
    int h = 40;

    harness.checkPoint("setBounds(int,int,int,int)void");

    Button b = new Button("button");
    b.setBounds(x, y, w, h);

    harness.check(x, b.getX(), "getX()int");
    harness.check(y, b.getY(), "getY()int");
    harness.check(w, b.getWidth(),  "getWidth()int");
    harness.check(h, b.getHeight(), "getHeight()int");
  }

  /**
   * make sure to set the system property 'com.acunia.wonka.awt' to 'false'
   * before starting the test, and to reset it to 'true' afterwards. This is to
   * prevent the creation of a window on the display by the test program.
   */

  public void test (TestHarness newharness)
    {
    harness = newharness;
    harness.setclass("java.awt.Component");
      {
// following linnes used to make sense without disrupting the test
//      Properties p = System.getProperties();
//      p.put("com.acunia.wonka.awt", "false");    // avoid generation of an x-window

// following test needs to be revised to find a way of getting graphics from a component
// temporarily disable test

//      testGetGraphics();

      testBounds();
//      p.put("com.acunia.wonka.awt", "true");
      }
    }
  }
