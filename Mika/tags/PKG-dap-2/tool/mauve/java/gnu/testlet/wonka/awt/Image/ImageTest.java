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

package gnu.testlet.wonka.awt.Image;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.Properties;
import java.awt.*;


public class ImageTest implements Testlet
  {

  TestHarness harness;

  /**
  * tests the dimensions's of an Image created by Component.createImage().
  */
  void testDimension()
    {
    harness.checkPoint("getWidth(java.awt.image.ImageObserver)int");
    int w = 400;
    int h = 234;
    Panel pa = new Panel();

    try         // fails on sun jdk1.3
      {
      Image im = pa.createImage(w,h);
      harness.check(im.getWidth(null), w, "getWidth(java.awt.image.ImageObserver)int");
      harness.checkPoint("getHeight(java.awt.image.ImageObserver)int");
      harness.check(im.getHeight(null), h, "getHeight(java.awt.image.ImageObserver)int");
      }
    catch (Exception e)
      {
      e.printStackTrace();
      }
    }

  /**
  * tests the graphics object retrieved from an Image object.
  */
  void testGraphics()
    {
    harness.checkPoint("getGraphics()java.awt.Graphics");
    try
      {
      int w = 400;
      int h = 234;
      Panel pa = new Panel();

      Image    im = pa.createImage(w,h);
      harness.check(im!=null, "createImage(int, int)java.awt.Image");
      Graphics g1  = null;

      try   // to be elaborated later when class ImageProducer is implemented.
        {
        g1 = im.getGraphics();
        harness.check(g1 != null, "getGraphics()java.awt.Graphics");
        harness.check(("test OK").equals("test OK"), "equals(java.lang.String)boolean");
        }
      catch (ClassCastException e)
        {
        harness.check(("test OK").equals(null), "equals(java.lang.String)boolean");
        }

      harness.check(g1.getFont(), Component.DEFAULT_FONT, "getFont()java.awt.Font");
      harness.check(g1.getColor(), Color.black, "getColor()java.awt.Color");
//      harness.debug(g1.getFont().toString());
//      harness.debug(g1.getColor().toString());


      pa.setFont(new Font("courR20", 0, 20));
      pa.setForeground(Color.orange);

      Graphics g2 = im.getGraphics();
      harness.check(g2.getFont(), new Font("courR20", 0, 20), "getFont()java.awt.Font");
      harness.check(g2.getColor(), Color.orange, "createColor()java.awt.Color");
//      harness.debug(g2.getFont().toString());
//      harness.debug(g2.getColor().toString());

      Graphics g3 = im.getGraphics();
      g3.setFont(new Font("helvB25", 0, 25));
      g3.setColor(Color.red);
      harness.check(g3.getFont(), new Font("helvB25", 0, 25), "getFont()java.awt.Font");
      harness.check(g3.getColor(), Color.red, "createColor()java.awt.Color");
//      harness.debug(g3.getFont().toString());
//      harness.debug(g3.getColor().toString());
      }

    catch (Exception e)
      {
      e.printStackTrace();
      }

    }

 /*
 ** make sure to set the system property 'com.acunia.wonka.awt' to 'false'
 ** before starting the test, and to reset it to 'true' afterwards. This is to
 ** prevent the creation of a window on the display by the test program; at the
 ** same time however the class SystemColor is prevented from being loaded (see
 ** dispatcher.c; as a consequence the 'testGraphics' function will generate a
 ** segmentation error in Component_getGraphics when trying to access a static
 ** field of that class. To avoid that, the testprogram must force the loading of
 ** class SystemColor by accessing one of its fields.
 */

  public void test (TestHarness newharness)
    {
    harness = newharness;
    harness.setclass("java.awt.Image");
      {
// Following lines used to avoid generation of an x-window without disrupting the
// tests

//      Properties p = System.getProperties();
//      p.put("com.acunia.wonka.awt", "false");    // avoid generation of an x-window

      testDimension();

// Following test is temporarily disabled : getting graphics from an image must be
// revised. who to make it rerurn a non-null graphics?

//      Color s = SystemColor.text;                // force loading of class SystemColor.
//      testGraphics();
//      p.put("com.acunia.wonka.awt", "true");
      }
    }
  }
