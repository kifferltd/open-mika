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



package gnu.testlet.wonka.util.TimeZone;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.*;

/**
*  this file contains test for java.util.TimeZone   <br>
*  TimeZone is an abstract class  <br>
*  following methods are abstract: <br>
*  - inDaylightTime <br>
*  - useDaylightTime <br>
*  - getOffset <br>
*  - getRawOffset <br>
*  - setRawOffset <br>
* <br>
*/
public class SMTimeZoneTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.TimeZone");
       test_setDefault();
       test_LONG_SHORT();
       test_getDefault();
       test_getDisplayName();
       test_getTimeZone();
       test_hasSameRules();
       test_getAvailableIDs();
       test_getID();
       test_setID();
       test_clone();
     }


/**
*  implemented. <br>
*
*/
  public void test_LONG_SHORT(){
    th.checkPoint("LONG_SHORT");
    th.check(TimeZone.LONG , 1 , "LONG value");
    th.check(TimeZone.SHORT , 0 , "SHORT value");

  }


/**
* implemented. <br>
*
*/
  public void test_getDefault(){
    th.checkPoint("getDefault()java.util.TimeZone");
    SimpleTimeZone stz = new SimpleTimeZone(1,"gmt");
    TimeZone.setDefault(stz);
    th.check(TimeZone.getDefault() , stz , "checking if value is set");

  }

/**
*   not implemented. <br>
*
*/
  public void test_getDisplayName(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_getTimeZone(){
    th.checkPoint("()");

  }

/**
*  not implemented. <br>
*
*/
  public void test_hasSameRules(){
    th.checkPoint("()");

  }

/**
* implemented. <br>
*
*/
  public void test_setDefault(){
    th.checkPoint("setDefault(java.util.TimeZone)void");
  	TimeZone.setDefault(null);
  	TimeZone defTZ = TimeZone.getDefault();
  	SimpleTimeZone mySTZ = new SimpleTimeZone(4,"MYT");
  	TimeZone.setDefault(mySTZ);
  	th.check(TimeZone.getDefault(), mySTZ, "checking if default timezone changed set");
  	TimeZone.setDefault(null);
  	th.check(TimeZone.getDefault(), defTZ, "checking if default timezone was restored");
  }

/**
*   not implemented. <br>
*
*/
  public void test_getAvailableIDs(){
    th.checkPoint("getAvailableIDs()");

  }

/**
* implemented. <br>
*
*/
  public void test_getID(){
    th.checkPoint("getID()java.lang.String");
    SimpleTimeZone stz = new SimpleTimeZone(1,"gmt");
    th.check(stz.getID() , "gmt" , "checking value of get");

  }

/**
*  implemented. <br>
*
*/
  public void test_setID(){
    th.checkPoint("setID(java.lang.String)void");
    SimpleTimeZone stz = new SimpleTimeZone(1,"gmt");
    try {
    	stz.setID(null);
        th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe){ th.check(true);}
    stz.setID("ECT");
    th.check(stz.getID() , "ECT" , "checking if value is set");
  }

/**
* not implemented. <br>
* it is quite unclear what clone should do exactly ...
* the clone should be equal to the TimeZone,  but not the same object.
* The problem is that TimeZone doesn't implement equals ...
*/
  public void test_clone(){
    th.checkPoint("()");

  }

}
