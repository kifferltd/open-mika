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

import gnu.testlet.*;

public class Tests implements Testlet {

  protected TestHarness th;

  public void test(TestHarness harness) {

    th = harness;
    th.checkPoint("doing Steven's tests");
    int result;

    th.setclass("java.lang.String");
    StringTest st = new StringTest();
    result = st.test();
    th.check( result == 0 ,
	"Testing String failed at point >>" + result + "<<");

    th.setclass("java.lang.StringBuffer");
    StringBufferTest sbt = new StringBufferTest();
    result = sbt.test(th);
    th.check( result == 0 ,
          "Testing StringBuffer failed at point >>" + result + "<<");

    th.setclass("java.lang.System");
    SystemTest syst = new SystemTest();
    result = syst.test();
    th.check( result == 0 ,
      "Testing System failed at point >>" + result + "<<");

    th.setclass("java.lang.Math");
    MathTest mt = new MathTest();
    result = mt.test();
    th.check( result == 0 ,
      "Testing Math failed at point >>" + result + "<<");

/* Suppressed because test buggy (assumes getFields() returns fields
**   in order they were declared)
**  FieldTest ft = new FieldTest();
**  result = ft.test();
**  th.check( result == 0,
**    "Testing Field failed at point >>" + result + "<<");
*/
 }


}
