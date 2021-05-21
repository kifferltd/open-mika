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



package gnu.testlet.wonka.util.Date;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.*;

/**
*  this file contains test for java.util.Date   <br>
*  <br>
*  Date offers more methods than the listed ones in here <br>
*  they are not tested since they are all depricated ! <br>
*  unfortunatly most of the alternative methods are not in wonka (sorry !) <br>
*  <br>
*  if you construct a new Date() --> the time Date is pointing at is the current time <br>
*  We need tests to verify this !
*/
public class SMDateTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.Date");
       test_Date();
       test_getTime();
       test_setTime();
       test_after();
       test_before();
       test_compareTo();
       test_equals();
       test_clone();
       test_hashCode();
       test_toString();
     }

/**
*   implemented. <br>
*   wonka has 6 constructors ( 4 deprecatred ones) <br>
*   use : Date() or Date(long msSinceEpoch)   <br>
*   tests only the non-depricated Constructors  <br>
* <br>
*   --> no test for Date() yet !
*/
  public void test_Date(){
    th.checkPoint("Date()");
    Date d1 = new Date();
    th.check(d1.getTime() > 100000000L, "got:"+d1.getTime());

    th.checkPoint("Date(long)");
    Date d2 = new Date(0);
    th.check(d2.getTime()== 0);
    d2 = new Date((long)Integer.MAX_VALUE);
    th.check(d2.getTime()== (long)Integer.MAX_VALUE);
    d2 = new Date((long)Integer.MIN_VALUE);
    th.check(d2.getTime()== (long)Integer.MIN_VALUE);
    d2 = new Date(Long.MAX_VALUE);
    th.check(d2.getTime()== Long.MAX_VALUE);
    d2 = new Date(Long.MIN_VALUE);
    th.check(d2.getTime()== Long.MIN_VALUE);

  }


/**
*   implemented. <br>
*   is also heavily used when testing the constructor(long) and setTime
*/
  public void test_getTime(){
    th.checkPoint("getTime()long");
    Date d2 = new Date(0);
    th.check(d2.getTime()== 0);
    d2 = new Date((long)Integer.MAX_VALUE);
    th.check(d2.getTime()== (long)Integer.MAX_VALUE);
    d2 = new Date((long)Integer.MIN_VALUE);
    th.check(d2.getTime()== (long)Integer.MIN_VALUE);
    d2 = new Date(Long.MAX_VALUE);
    th.check(d2.getTime()== Long.MAX_VALUE);
    d2 = new Date(Long.MIN_VALUE);
    th.check(d2.getTime()== Long.MIN_VALUE);

  }

/**
*   implemented. <br>
*
*/
  public void test_setTime(){
    th.checkPoint("setTime(long)void");
    Date d = new Date(0);
    long l = -24556798768787867L;
    d.setTime(l);
    th.check( d.getTime() == l );
    d.setTime(-l);
    th.check( d.getTime() == -l );
    d.setTime(0L);
    th.check( d.getTime() == 0L );

  }

/**
*   implemented. <br>
*
*/
  public void test_after(){
    th.checkPoint("after(java.util.Date)boolean");
    Date d1,d2 = new Date(34396989L);
    d1 = new Date();
    th.check(d1.after(d2));
    th.check(!d2.after(d1));
    Date d3,d4 = new Date(-45469456L);
    d3 = new Date(- d1.getTime());
    th.check(d4.after(d3));
    th.check(!d3.after(d4));
    th.check(d1.after(d3));
    th.check(!d3.after(d1));
    th.check(d1.after(d4));
    th.check(!d4.after(d1));
    th.check(d2.after(d3));
    th.check(!d3.after(d2));
    th.check(!d3.after(d3));
    th.check(!d1.after(d1));
    try { d1.after(null);
          th.fail("should throw NullPointerException");
        }
    catch (NullPointerException ne) { th.check(true); }

   Date d5 = new Date(2011, 5, 21);
   Date d6 = new Date(2015, 1, 21);
   th.check(d6.after(d5));
   th.check(!d5.after(d6));
    	
  }

/**
*   implemented. <br>
*
*/
  public void test_before(){
    th.checkPoint("before(java.util.Date)boolean");
    Date d1,d2 = new Date(34396989L);
    d1 = new Date();
    th.check(!d1.before(d2));
    th.check(d2.before(d1));
    Date d3,d4 = new Date(-45469456L);
    d3 = new Date(- d1.getTime());
    th.check(!d4.before(d3));
    th.check(d3.before(d4));
    th.check(!d1.before(d3));
    th.check(d3.before(d1));
    th.check(!d1.before(d4));
    th.check(d4.before(d1));
    th.check(!d2.before(d3));
    th.check(d3.before(d2));
    th.check(!d3.before(d3));
    th.check(!d1.before(d1));
    try { d1.before(null);
          th.fail("should throw NullPointerException");
        }
    catch (NullPointerException ne) { th.check(true); }
    //th.debug(d1.toString());
    //th.debug(d2.toString());
    //th.debug(d3.toString());
    //th.debug(d4.toString());
  }

/**
*   implemented. <br>
*   JLS specifies 2 methods compareTo <br>
*   - int compareTo(Date d) <br>
*   - int compareTo(Object d) <br>
*
*/
  public void test_compareTo(){
    th.checkPoint("compareTo(java.lang.Object)int");
    Date d = new Date(0);
    try {  d.compareTo(null);
           th.fail("should throw NullPointerException");
        }
    catch (NullPointerException ne) { th.check(true); }
    try {  d.compareTo("a");
           th.fail("should throw ClassCastException");
        }
    catch (ClassCastException ne) { th.check(true); }
    Object o = new Date();
    try { th.check(d.compareTo(o) < 0);
          o = new Date(-1);
          th.check(d.compareTo(o) > 0);
          o = new Date(0);
          th.check(d.compareTo(o) == 0);
        }
    catch (Exception e) { th.fail("did not Expect Exception -- got:"+e); }

    th.checkPoint("compareTo(java.util.Date)int");
    d = new Date();
    th.check(d.compareTo(d) == 0);
    Date d1 = new Date(d.getTime()-1);
    th.check(d.compareTo(d1) > 0);
    d1 = new Date(d.getTime()+1);
    th.check(d.compareTo(d1) < 0);
    try {  d.compareTo(null);
           th.fail("should throw NullPointerException");
        }
    catch (NullPointerException ne) { th.check(true); }

  }

/**
*   implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
    Date d1,d2 = new Date(34396989L);
    d1 = new Date();
    th.check(!d1.equals(d2));
    th.check(!d2.equals(d1));
    Date d3,d4 = new Date(-45469456L);
    d3 = new Date(-45469456L);
    th.check(d4.equals(d3));
    th.check(!d3.equals("a"));
    th.check(!d1.equals(new Object()));
    th.check(!d1.equals(d4));
    th.check(!d2.equals(d3));
    th.check(!d3.equals(d2));
    th.check(d3.equals(d3));
    th.check(d1.equals(d1));
    th.check(d3.equals(d4));
    try { th.check(!d1.equals(null));}
    catch (NullPointerException ne) {th.fail("should throw NullPointerException");}

  }

/**
*   not implemented. <br>
*   --> not in Wonka
*   ----> but inherited from Object
*/
  public void test_clone(){
    th.checkPoint("()");
  }

/**
*   implemented. <br>
*
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    Date d = new Date(0);
    th.check(d.hashCode() == 0, "got:"+d.hashCode() );
    d = new Date(1000);
    th.check(d.hashCode() == 1000, "got:"+d.hashCode() );
    d = new Date(-14587);
    long l = d.getTime();
    th.check(d.hashCode() == (int)(((int)l)^(l>>>32)), "got:"+d.hashCode()+", exp:"+(int)(((int)l)^(l>>>32)) );
    d = new Date();
    l = d.getTime();
    th.check(d.hashCode() == (int)(((int)l)^(l>>>32)), "got:"+d.hashCode()+", exp:"+(int)(((int)l)^(l>>>32)) );
    l = -l; d = new Date(l);
    th.check(d.hashCode() == (int)(((int)l)^(l>>>32)), "got:"+d.hashCode()+", exp:"+(int)(((int)l)^(l>>>32)));
    //th.debug("casting l = "+l+" to int ="+((int)l)+"but expected "+(int)(l & 0x00000000ffffffffL));
  }

/**
*   not implemented. <br>
*   at this point the toString method always returns the same date
*   --> in a correct format !!!
*/
  public void test_toString(){
    th.checkPoint("toString()java.lang.String");
    Date d = new Date();
    //th.debug("Debuging -- printing current Date\n"+d.toString());
    d = new Date(0);
    //th.debug("Debuging -- printing reference Date\n"+d.toString());
    d = new Date(31536000000555L);
    //th.debug("Debuging -- printing futere Date\n"+d.toString());
    d = new Date(-31536000000000L);
    //th.debug("Debuging -- printing past Date\n"+d.toString());
    d = new Date(31876576555L);
    //th.debug("Debuging -- printing a random Date\n"+d.toString());
    d = new Date(31876576555L+86400000L);
    //th.debug("Debuging -- printing a random Date\n"+d.toString());
//    try {Thread.sleep(2000);}
//    catch(Exception e){}
//    d = new Date();
//    th.debug("Debuging -- printing current Date\n"+d.toString());
  }
}
