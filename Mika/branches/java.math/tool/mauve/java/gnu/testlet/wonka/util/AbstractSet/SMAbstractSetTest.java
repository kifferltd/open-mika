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


package gnu.testlet.wonka.util.AbstractSet; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.*; // at least the class you are testing ...

/**
*  this file contains test for java.util.AbstractSet   <br>
*
*/
public class SMAbstractSetTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.AbstractSet");
       test_equals();
       test_hashCode();
     }


/**
* implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
    SMExAbstractSet xas1 = new SMExAbstractSet();
    SMExAbstractSet xas2 = new SMExAbstractSet();
    th.check( xas1.equals(xas2) , "checking equality -- 1");
    th.check(!xas1.equals(null) , "checking equality -- 2");
    th.check(!xas1.equals(this) , "checking equality -- 3");
    th.check( xas1.equals(xas1) , "checking equality -- 4");
    xas1.v.add(null);
    xas1.v.add("a");
    xas2.v.add("b");
    xas2.v.add(null);
    xas2.v.add("a");
    xas1.v.add("b");
    th.check( xas1.equals(xas2) , "checking equality -- 5");
    th.check( xas1.equals(xas1) , "checking equality -- 6");


  }
/**
* implemented. <br>
*
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    SMExAbstractSet xas = new SMExAbstractSet();
    th.check(xas.hashCode() == 0 ,"checking hc-algorithm -- 1");
    xas.v.add(null);
    th.check(xas.hashCode() == 0 ,"checking hc-algorithm -- 2");
    xas.v.add("a");
    int hash = "a".hashCode();
    th.check(xas.hashCode() == hash ,"checking hc-algorithm -- 3");
    hash += "b".hashCode();
    xas.v.add("b");
    th.check(xas.hashCode() == hash ,"checking hc-algorithm -- 4");
    hash += "c".hashCode();
    xas.v.add("c");
    th.check(xas.hashCode() == hash ,"checking hc-algorithm -- 5");
    hash += "d".hashCode();
    xas.v.add("d");
    th.check(xas.hashCode() == hash ,"checking hc-algorithm -- 6");




  }

}
