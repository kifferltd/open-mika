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



package gnu.testlet.wonka.util.SimpleTimeZone;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.*;

/**
*  this file contains test for java.util.SimpleTimeZone    <br>
*/
public class SMSimpleTimeZoneTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.SimpleTimeZone");
       test_SimpleTimeZone();
       test_getOffset();
       test_getRawOffset();
       test_setRawOffset();
       test_hasSameRules();
       test_inDayligthTime();
       test_getDSTSavings();
       test_setDSTSavings();
       test_setEndRule();
       test_setStartRule();
       test_setStartYear();
       test_useDaylightTime();
       test_clone();
       test_equals();
       test_hashCode();
       //test_toString();
     }


/**
*   not implemented. <br>
*
*/
  public void test_SimpleTimeZone(){
    th.checkPoint("SimpleTimeZone(int,java.lang.String)");
    SimpleTimeZone stz = new SimpleTimeZone(-123456 , "MyTimeZone");
    th.check( stz.getID() , "MyTimeZone" , "checking ID");
    th.check( stz.getRawOffset() , -123456 , "checking Offset" );
    th.check(!stz.useDaylightTime() , "no daylight savings by default");
    try { new SimpleTimeZone(-123456 , null);
    	th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe) { th.check(true); }
    	

    th.checkPoint("SimpleTimeZone(int,java.lang.String,int,int,int,int,int,int,int,int)");



    th.checkPoint("SimpleTimeZone(int,java.lang.String,int,int,int,int,int,int,int,int,int)");
  }

/**
*  implemented. <br>
*
*/
  public void test_getOffset(){
    th.checkPoint("getOffset(int,int,int,int,int,int)int");
    SimpleTimeZone ect = (SimpleTimeZone)TimeZone.getTimeZone("ECT");
    int ad = GregorianCalendar.AD;
    int bc = GregorianCalendar.BC;
    th.check(ect.getOffset(ad,2001,0,1,1,0) , 3600000 , "check normal offset -- 1");
    th.check(ect.getOffset(ad,2001,2,28,1,0) , 3600000 , "check normal offset -- 2");
    th.check(ect.getOffset(ad,2001,2,28,1,2*3600000) , 2*3600000 , "check dst offset -- 3");
    th.check(ect.getOffset(ad,2001,2,29,2,2*3600000) , 2*3600000 , "check dst offset -- 4");
    th.check(ect.getOffset(bc,1,2,28,1,2*3600000) , 3600000 , "check nrm offset -- 5");
    th.check(ect.getOffset(ad,2001,4,28,1,2*3600000) , 2*3600000 , "check dst offset -- 6");
    th.check(ect.getOffset(ad,2001,8,28,1,2*3600000) , 2*3600000 , "check dst offset -- 7");
    th.check(ect.getOffset(ad,2001,9,28,1,2*3600000) , 3600000 , "check nrm offset -- 8");
    th.check(ect.getOffset(ad,2001,2,27,7,3600000) , 3600000 , "check nrm offset -- 9");
    th.check(ect.getOffset(ad,2001,9,29,2,3600000) , 3600000 , "check nrm offset -- 10");
    ect = new SimpleTimeZone(3600000, "MyTZ", 3 , -2 , 7 , 300, 9,  2, 2, 300, 2*3600000);
    th.check(ect.getOffset(ad,2001,2,21,7,299) , 3600000 , "check nrm offset -- 11");
    th.check(ect.getOffset(ad,2001,9,12,2,300) , 3600000 , "check dst offset -- 12");
    th.check(ect.getOffset(ad,2001,2,14,1,2*3600000) , 3600000 , "check nrm offset -- 13");
    th.check(ect.getOffset(ad,2001,9,21,1,2*3600000-1) , 3600000 , "check nrm offset -- 14");
    ect = new SimpleTimeZone(3600000, "MyTZ", 6 , -2 , 7 , 300, 2,  -2, 7, 300, 2*3600000);
    th.check(ect.getOffset(ad,2001,2,21,7,400),   3600000, "check nrm offset -- 15");
    th.check(ect.getOffset(ad,2001,2,1,7,299), 3*3600000, "check dst offset -- 16");


  }

/**
* implemented. <br>
*
*/
  public void test_getRawOffset(){
    th.checkPoint("getRawOffset()int");
    SimpleTimeZone ect = new SimpleTimeZone(3600000, "MyTZ");
    th.check(ect.getRawOffset() , 3600000 , "checking returnvalue");

  }

/**
* implemented. <br>
*
*/
  public void test_setRawOffset(){
    th.checkPoint("setRawOffset(int)void");
    SimpleTimeZone ect = new SimpleTimeZone(3600000, "MyTZ");
    ect.setRawOffset(12345);
    th.check(ect.getRawOffset() , 12345 , "checking set value -- 1");
    ect.setRawOffset(-12345);
    th.check(ect.getRawOffset() , -12345 , "checking set value -- 2");
    ect.setRawOffset(0);
    th.check(ect.getRawOffset() , 0 , "checking set value -- 3");

  }

/**
* not implemented. <br>
*
*/
  public void test_hasSameRules(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_inDayligthTime(){
    th.checkPoint("()");

  }

/**
* implemented. <br>
*
*/
  public void test_getDSTSavings(){
    th.checkPoint("getDSTSavings()int");
    SimpleTimeZone ect = new SimpleTimeZone(3600000, "MyTZ", 3 , -2 , 7 , 300, 9,  2, 2, 300, 2*3600000);
    th.check(ect.getDSTSavings() , 2*3600000 , "checking returnvalue -- 1");
    ect = new SimpleTimeZone(3600000, "MyTZ", 3 , -2 , 7 , 300, 9,  2, 2, 300);
    th.check(ect.getDSTSavings() , 3600000 , "checking returnvalue -- 2");

  }

/**
* implemented. <br>
*
*/
  public void test_setDSTSavings(){
    th.checkPoint("setDSTSavings(int)void");
    SimpleTimeZone ect = new SimpleTimeZone(3600000, "MyTZ", 3 , -2 , 7 , 300, 9,  2, 2, 300, 2*3600000);
    ect.setDSTSavings(122);
    th.check(ect.getDSTSavings() , 122 , "checking returnvalue -- 1");
    try {
    	ect.setDSTSavings(-122);
    	th.fail("should throw an IllegalArgumentException");
    }	
    catch(IllegalArgumentException iae) { th.check(true , "caught exception -- 1"); }
  }

/**
*   not implemented. <br>
*
*/
  public void test_setEndRule(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_setStartRule(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_setStartYear(){
    th.checkPoint("setStartYear(int)void");
    SimpleTimeZone ect = (SimpleTimeZone)TimeZone.getTimeZone("ECT");
    int ad = GregorianCalendar.AD;
    int bc = GregorianCalendar.BC;
    ect.setStartYear(1000);
    th.check(ect.getOffset(ad,1,0,1,1,0) , 3600000 , "check normal offset -- 1");
    th.check(ect.getOffset(ad,1,2,28,1,0) , 3600000 , "check normal offset -- 2");
    th.check(ect.getOffset(ad,1,2,28,1,2*3600000) , 3600000 , "check nrm offset -- 3");
    th.check(ect.getOffset(ad,1,2,29,2,2*3600000) , 3600000 , "check nrm offset -- 4");

  }

/**
*   not implemented. <br>
*
*/
  public void test_useDaylightTime(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_clone(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_hashCode(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_toString(){
    th.checkPoint("()");
    try {
    	Vector v = null;
    	try {
        	v.add("a");	
     	        th.debug("should throw NullPointerException");
    	}
    	catch(Exception e) {
    		th.debug("got exception "+e+" after try");
        	e.printStackTrace();
     	}
    	finally {
     		try {
     		 	th.debug("clearing vector");
     		 	v.clear();
//     		 	th.debug("done clearing vector");
     		}
     		catch(Exception e) {
		    	th.check(true, "got exception "+e+" in finally blok");
        		e.printStackTrace();
     		}
	}
     }
     catch(Exception e) { th.fail("got exception "+e+" in bottom catch"); }


  }

}
