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


package gnu.testlet.wonka.util.EventObject;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.*;

/**
*  this file contains test for EventObject <br>
*   <br>
*  this function uses SMTestEvent, SMTestListener and <br>
*  SMTestListenerObject <br>
*   <br>
*  we have those three classes as wrapper around the original class and interface <br>
*  and allow us to test them. <br>
*  <br>
*  since EventListener is an interface which no body, we know it exists and is <br>
*  defined empty if we can compile this package. <br>
*/
public class SMEventObjectTest implements Testlet
{
  protected TestHarness th;
  protected Object tlo;

  public int count=0;
  public int oldcount=0;
  public SMTestEvent SMevt;


  public String toString() {
  	return "SMEventObjectTest";
  }

  public void test (TestHarness harness)
    {
       th = harness;
       tlo = new SMTestListenerObject(this);
       th.setclass("java.util.EventObject");
       test_EventObject();
       test_source();
       test_getSource();
       test_toString();
     }

/**
* implemented.	<br>
* --> why don't we throw a NullPointerException  <br>
* jdk also throws an IllegalArgumentException, but WHY ???!!! <br>
*/
  public void test_EventObject(){
    th.checkPoint("EventObject(java.lang.Object)");
    String s = new String("I'm the source");
    SMevt = new SMTestEvent(s);
    th.check(SMevt.showSource() == s , "check field is set");
    try {
    	new SMTestEvent(null);
    	th.fail("should throw IllegalArgumentException");
    	}
    catch( IllegalArgumentException ne) { th.check(true); }
    catch( Exception e) { th.fail("got wrong Exeption:"+e); }
  }

/**
*   not implemented.	<br>
*   --> use reflection to get Modifiers
*/
  public void test_source(){
    th.checkPoint("()");

  }
/**
* implemented.
*
*/
  public void test_getSource(){
    th.checkPoint("getSource()java.lang.Object");
    oldcount = count;
    ((SMTestListener) tlo).fireTestEvent(new SMTestEvent(this));
    th.check(oldcount+1 == count , "check if event was fired");
    th.check(SMevt.getSource() == this , "check GetSource");
  }
/**
* implemented.
*
*/
  public void test_toString(){
    th.checkPoint("toString()java.lang.String");
    oldcount = count;
    ((SMTestListener) tlo).fireTestEvent(new SMTestEvent(this));
    th.check(oldcount+1 == count , "check if event was fired");
    th.check(SMevt.toString().indexOf(this.toString())!= -1 , "check toString -- got:"+SMevt.toString());

  }

}
