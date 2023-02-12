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
// Created: 2001/04/10

package gnu.testlet.wonka.awt.Image;

import java.util.Properties;
import java.awt.*;

public class ImageTestJDK
  {

  /**
  * tests the dimensions's of an Image created by Component.createImage().
  */
  void testDimension()
    {
    int w = 400;
    int h = 234;
    Panel pa = new Panel();

    try
      {
      Image im = pa.createImage(w,h);
      System.out.println(im.getWidth(null));       // fails on sun jdk1.3
      System.out.println(im.getHeight(null));
      }
    catch (Exception e)
      {
      e.printStackTrace();
      }
    }

  /**
  * tests the graphics object returned by the method Image.getGraphics().
  */
  void testGraphics()
    {
    int w = 400;
    int h = 234;
    Panel pa = new Panel();

    Image    im = pa.createImage(w,h);
    Graphics g1  = null;
    try {
      g1 = im.getGraphics();
      System.out.println("test OK");
      }
    catch (ClassCastException e) {
      System.out.println("test NOT OK");
      }
    catch (Exception e)
      {
      e.printStackTrace();
      }

//    harness.debug(g1.getFont().toString());
//    harness.debug(g1.getColor().toString());

/*
    pa.setFont(new Font("courP21", 0, 21));
    pa.setForeground(Color.orange);

    Graphics g2 = im.getGraphics();
    harness.debug(g2.getFont().toString());
    harness.debug(g2.getColor().toString());
*/
    Graphics g3 = im.getGraphics();
    g3.setFont(new Font("courP21", 0, 21));
    g3.setColor(Color.orange);
    System.out.println(g3.getFont().toString());
    System.out.println(g3.getColor().toString());

    }

 /**
  * tests the Font constructor Font(String, int, int), also checks the initialisation
  * by calling on Font.equals();
  * make sure to set the system property 'com.acunia.wonka.awt' to 'false'
  * before starting the test, and to reset it to 'true' aftrewards. This is to
  * prevent the creation of a window on the display by the test program.
  */

  ImageTestJDK ()
    {
//    Properties p = System.getProperties();
//    p.put("com.acunia.wonka.awt", "false");
    testDimension();  // fails under sun jdk
    testGraphics();
//    p.put("com.acunia.wonka.awt", "true");
    }

  static public void main (String[] args)
    {
    new ImageTestJDK();
  }
}
