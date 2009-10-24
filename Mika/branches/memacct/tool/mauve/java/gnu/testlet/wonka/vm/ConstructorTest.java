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



package gnu.testlet.wonka.vm;

import java.lang.reflect.*;
import gnu.testlet.*;

public class ConstructorTest implements Testlet {

  protected TestHarness th;

  public void test(TestHarness harness) {

    th = harness;
    th.setclass("java.lang.reflect.Constructor");
    th.checkPoint("basic testing ...");
    try {
      Constructor con = Constructor_C.class.getDeclaredConstructor(new Class[] {});

      try {
        Constructor_C c = (Constructor_C)con.newInstance(new Object[] {});
      	th.check(true);
      }
      catch (Exception ex) {
       th.fail("should not throw an Exception -- 1");
      }

      con = Constructor_D.class.getDeclaredConstructor(new Class[] {int.class});

      // Demonstrate primitive wrapping
      
      Constructor_D d;
      try {
        d = (Constructor_D)con.newInstance(new Object[] {new Integer(0)});
      	th.check(true);
      }
      catch (Exception ex) {
       th.fail("should not throw an Exception -- 2");
      }

      // Demonstrate widening
  
      try {    
        d = (Constructor_D)con.newInstance(new Object[] {new Byte((byte)0)});
      	th.check(true);
      }
      catch (Exception ex) {
       th.fail("should not throw an Exception -- 3");
      }

      // Demonstrate exception handling

      d = (Constructor_D)con.newInstance(new Object[] {new Integer(-1)});
       th.fail("should throw an Exception -- 1");

    }
    catch (InvocationTargetException ex) {
      	th.check(true);
    }
    catch (Exception ex) {
       th.fail("should not throw an Exception -- 4");
    }
  }

}
