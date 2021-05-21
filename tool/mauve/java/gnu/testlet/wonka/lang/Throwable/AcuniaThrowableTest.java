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


package gnu.testlet.wonka.lang.Throwable;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class AcuniaThrowableTest implements Testlet {

  protected TestHarness th;

  static class MyFirstException extends Exception {
    private static final long serialVersionUID = 312869088683582222L;}
  static class MySecondException extends Exception {
    private static final long serialVersionUID = -15918239120714014L;}


  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.lang.Throwable");
    test_Handling();
    test_fillInStackTrace();
  }


  private void check() throws MyFirstException,MySecondException {
    throw new MyFirstException();
  }

  private void check2() throws MyFirstException {
    try {
      check();
    }
    catch(MySecondException mse){
      th.fail("no MySecondException should be thrown got "+mse);
    }
  }

  private void check3() throws MyFirstException {
    try {
      check();
    }
    catch(MyFirstException mfe){
      throw (MyFirstException) mfe.fillInStackTrace();
    }
    catch(MySecondException mse){
      th.fail("no MySecondException should be thrown got "+mse);
    }
  }

/**
* implemented. <br>
*
*/
  public void test_Handling(){
    th.checkPoint("internal exception handling");
    try {
      check2();
    }
    catch(MyFirstException mfe){
      th.check(true, "Exception was caught in the right place");
    }
  }


/**
*   implemented. <br>
*
*/
  public void test_fillInStackTrace(){
    th.checkPoint("fillInStackTrace()");

    try {
      check3();
    }
   catch(MyFirstException mfe){
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);
      mfe.printStackTrace(ps);
      byte[] bytes = baos.toByteArray();
      BufferedReader r = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
      String line;
      try {
        line = r.readLine(); // Skip "gnu.testlet.wonka.lang.Throwable.AcuniaThrowableTest$MyFirstException"
        line = r.readLine(); 
        th.check(line.indexOf("check3(") >= 0, "This line of the stack trace must mention check3()");
      }
      catch (IOException ioe) {
        th.fail("no IOException should be thrown got "+ioe);
      }
    }
  }

/**
*   not implemented. <br>
*
*/
  public void test_(){
    th.checkPoint("()");

  }
}
