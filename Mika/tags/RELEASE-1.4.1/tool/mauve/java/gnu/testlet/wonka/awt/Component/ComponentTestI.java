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


package gnu.testlet.wonka.awt.Component;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.Properties;
import java.awt.*;


// ****** under development


public class ComponentTestI implements Testlet
  {
  TestHarness harness;

  void testCreateImage(){

    Frame f = new Frame("createImage test");
//    Panel f = new Panel();
    Font font = new Font("Courier", Font.PLAIN, 17);
    f.setFont(font);
    f.setForeground(new Color(255,0,0));

    int w = 300;
    int h = 200;

    f.setSize(w+40, h);

    Panel pa = new Panel();
    f.add(pa, BorderLayout.CENTER);
    f.show();
    Image im = pa.createImage(w,h);
    harness.check(im!=null);
    Graphics g = im.getGraphics();

    String str = "ACUNIA";
    g.drawString(str, 10, 100);
    g.drawRect(100,100,40,40);


    Point stringPt = new Point(50, 150);

    g.setColor(new Color(200, 40, 70));  // dark-red
    g.drawRect(100,100,40,40);
    g.fillRect(0, 0, w, h);

    Graphics g2 = pa.getGraphics();
    g2.drawImage(im, 0,0,null) ;

    f.show();
        try {Thread.sleep(3000);}
        catch (InterruptedException e) {}

  }

  public void test (TestHarness newharness)
    {
    harness = newharness;
    harness.setclass("java.awt.Component");
//    if (System.getProperty("com.acunia.wonka.awt", "false").equals("true"))
      testCreateImage();
    }
  }
