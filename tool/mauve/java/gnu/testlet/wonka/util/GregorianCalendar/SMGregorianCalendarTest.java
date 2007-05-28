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


package gnu.testlet.wonka.util.GregorianCalendar; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.*; // at least the class you are testing ...

/**
*  this file contains test for java.util.GregorianCalendar  <br>
*
*/
public class SMGregorianCalendarTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.GregorianCalendar");
       test_();



       test_behaviour();
     }

/**
*   not implemented. <br>
*
*/
  public void test_(){
    th.checkPoint("()");

  }


/**
*   not implemented. <br>
*
*/
  public void test_behaviour(){
    th.checkPoint("GregorianCalendar()");
      GregorianCalendar gc = new GregorianCalendar();
//      gc.setTime(new Date(12345678910000L));
//    th.debug(""+new Date(12345678910000L));
/*      GregorianCalendar cp = cloneGC(gc);
      th.debug("gc = "+gc.getTime());
      th.debug("gc = "+cp.getTime());
      th.debug("jan 1 1970 :"+new Date(0L));
      th.check( gc.getTime().getTime() , cp.getTime().getTime() ,"checking new long values -- 1");
      gc.setTime(new Date(0L));
      cp = cloneGC(gc);
      th.check( cp.getTime().getTime() == 0L ,"test time is set");
      for (int i=3 ; i <250 ; i++) {
      	gc.clear();
      	gc.setTime(new Date((long)i*235456L+5234567891000L));
      	cp = cloneGC(gc);
      	th.check( gc.getTime().getTime() , cp.getTime().getTime() ,"checking new long values -- "+i);
      }
      th.debug(gc.toString()+"\n"+gc.getTime());
      th.debug(gc.getTime().toString()+" --> year = "+gc.get(Calendar.YEAR));
      th.debug(cp.toString()+"\n"+cp.getTime());
      gc = new GregrorianCalendar();
*/    gc.clear();
      gc.setTime(new Date(0L));
      th.check(gc.get(Calendar.YEAR),1970, "check year");
      th.check(gc.get(Calendar.MONTH),Calendar.JANUARY, "check month");
      th.check(gc.get(Calendar.DATE),1, "check day");
      gc.add(Calendar.YEAR, 1);
      th.check(gc.get(Calendar.YEAR),1971, "check year");
      th.check(gc.get(Calendar.MONTH),Calendar.JANUARY, "check month");
      th.check(gc.get(Calendar.DATE),1, "check day");
      gc.add(Calendar.MONTH, 13);
      th.check(gc.get(Calendar.YEAR),1972, "check year");
      th.check(gc.get(Calendar.MONTH),Calendar.FEBRUARY, "check month");
      th.check(gc.get(Calendar.DATE),1, "check day");
      gc.add(Calendar.DATE, 42);
      th.check(gc.get(Calendar.YEAR),1972, "check year");
      th.check(gc.get(Calendar.MONTH),Calendar.MARCH, "check month");
      th.check(gc.get(Calendar.DATE),14, "check day");
      gc.add(Calendar.DATE, 366 + 365 + 30 + 7);
      th.check(gc.get(Calendar.YEAR),1974, "check year");
      th.check(gc.get(Calendar.MONTH),Calendar.APRIL, "check month");
      th.check(gc.get(Calendar.DATE),21, "check day");
      gc.add(Calendar.DATE, -(366 + 365 + 30 + 7));
      th.check(gc.get(Calendar.YEAR),1972, "check year");
      th.check(gc.get(Calendar.MONTH),Calendar.MARCH, "check month");
      th.check(gc.get(Calendar.DATE),14, "check day");
      gc.add(Calendar.DATE, -42);
      th.check(gc.get(Calendar.YEAR),1972, "check year");
      th.check(gc.get(Calendar.MONTH),Calendar.FEBRUARY, "check month");
      th.check(gc.get(Calendar.DATE),1, "check day");
      gc.add(Calendar.MONTH, -13);
      th.check(gc.get(Calendar.YEAR),1971, "check year");
      th.check(gc.get(Calendar.MONTH),Calendar.JANUARY, "check month");
      th.check(gc.get(Calendar.DATE),1, "check day");
      
      th.checkPoint("roll");
      gc.roll(Calendar.MONTH, 13);
      //System.out.println("SMGregorianCalendarTest.test_behaviour()"+gc.getTime());;
      th.check(gc.get(Calendar.YEAR),1971, "check year");
      th.check(gc.get(Calendar.MONTH),Calendar.FEBRUARY, "check month");
      th.check(gc.get(Calendar.DATE),1, "check day");
      gc.roll(Calendar.DATE, 42);
      th.check(gc.get(Calendar.YEAR),1971, "check year");
      th.check(gc.get(Calendar.MONTH),Calendar.FEBRUARY, "check month");
      th.check(gc.get(Calendar.DATE),15, "check day");
      gc.roll(Calendar.YEAR, 21);
      th.check(gc.get(Calendar.YEAR),1992, "check year");
      th.check(gc.get(Calendar.MONTH),Calendar.FEBRUARY, "check month");
      th.check(gc.get(Calendar.DATE),15, "check day");
      gc.roll(Calendar.DATE, 14 - 10 * 29);
      th.check(gc.get(Calendar.YEAR),1992, "check year");
      th.check(gc.get(Calendar.MONTH),Calendar.FEBRUARY, "check month");
      th.check(gc.get(Calendar.DATE),29, "check day");
      //System.out.println("SMGregorianCalendarTest.test_behaviour()DEBUG "+gc.getTime());
      gc.roll(Calendar.YEAR, 21);
      //System.out.println("SMGregorianCalendarTest.test_behaviour()DEBUG "+gc.getTime());
      th.checkPoint("roll -- 2");
      //System.out.println("SMGregorianCalendarTest.test_behaviour()"+gc.getTime());;
      th.check(gc.get(Calendar.YEAR),2013, "check year");
      th.check(gc.get(Calendar.MONTH),Calendar.MARCH, "check month");
      th.check(gc.get(Calendar.DATE),1, "check day");
      gc.clear();
      try {
        gc.set(Calendar.DAY_OF_YEAR , 35);
        //th.debug(gc.getTime().toString());
      } catch (Exception e) {
        th.fail(e.toString());
        e.printStackTrace();
      }
      gc = new GregorianCalendar(1970,0,1,0,0);
      gc.clear(Calendar.MONTH);
      gc.set(Calendar.DAY_OF_YEAR , 35);
      gc.clear(Calendar.MONTH);
      th.check(gc.get(Calendar.MONTH) , 1 ,"update Month");
      th.check(gc.get(Calendar.DAY_OF_YEAR) , 35 ,"update Month");
      //th.debug(gc.getTime().toString());
      //th.debug(gc.toString());
      th.check(gc.get(Calendar.YEAR),1970, "check year");
      th.check(gc.get(Calendar.SECOND),0, "check seconds");
      gc.setTimeZone(TimeZone.getTimeZone("GMT"));
      //th.debug(gc.getTime().toString());
      //th.debug(new Date(0L).toString());
      TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
      //th.debug(new Date(0L).toString());
      //th.debug(gc.getTime().toString()+" "+gc.getTimeZone().toString());
      gc = new GregorianCalendar(1970,0,1,0,0);
      gc.setLenient(true);
      gc.set(Calendar.MINUTE , 12345678);	
      //th.debug(gc.getTime().toString());
      gc.add(Calendar.MINUTE , 1200);	
      //th.debug(gc.getTime().toString());
      gc.add(Calendar.MONTH , 16);	
      //th.debug(gc.getTime().toString());
      gc = new GregorianCalendar(1970,0,1,0,0);
      //th.debug("G.C is lenient "+gc.isLenient());	
      //th.debug(gc.getTime().toString());
      gc.setLenient(false);
      //th.debug("G.C is lenient "+gc.isLenient());	
//      gc.clear(Calendar.YEAR);
      gc.add(Calendar.YEAR , 12349);
      //th.debug(gc.getTime().toString());
//      gc.clear(Calendar.MONTH);
      gc.setLenient(true);
//      gc.add(Calendar.WEEK_OF_YEAR , 12345);

      //th.debug(gc.toString());
      gc = new GregorianCalendar(1970,0,1,0,0);
      //th.debug(gc.getTime().toString());
      //th.debug(""+gc.getTime().getTime());
      gc.set(Calendar.MONTH, 6);
      gc.setTime(new Date(Long.MAX_VALUE));
      //th.debug(gc.getTime().toString());
      gc.setTime(new Date(Long.MIN_VALUE));
      //th.debug(gc.getTime().toString());
      //th.debug(gc.toString());
      //th.debug(TimeZone.getDefault().getDisplayName(true,1));
      //th.debug(TimeZone.getTimeZone("PST").getDisplayName(true,1));
      //th.debug(TimeZone.getTimeZone("PRT").getDisplayName(true,1));
      //SimpleTimeZone stz = (SimpleTimeZone) TimeZone.getTimeZone("PST");
      //stz.setEndRule(1 , 28 , 342113);
      SimpleTimeZone stz = new SimpleTimeZone(3600000*(-9) ,"MySTZ");
      //th.debug ("my TimeZone uses DST --> "+stz.useDaylightTime());
      stz.setEndRule(10 , 28 , 342113);
      //th.debug ("my TimeZone uses DST --> "+stz.useDaylightTime());
      stz.setStartRule(1 , 28 , 342113);
      //th.debug ("my TimeZone uses DST --> "+stz.useDaylightTime());
      //th.debug(stz.toString());
      
  }

  public GregorianCalendar cloneGC(GregorianCalendar gc) {
   	GregorianCalendar cp = new GregorianCalendar(
   	gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), gc.get(Calendar.DATE),
   	gc.get(Calendar.HOUR_OF_DAY), gc.get(Calendar.MINUTE), gc.get(Calendar.SECOND));
   	cp.set(Calendar.MILLISECOND , gc.get(Calendar.MILLISECOND));
    return cp;
  }

}
/*
      Locale loc = new Locale("acd","BEFG","adfg");
      th.debug(loc.toString());
      loc = new Locale("acd","BEFG","adfg");
      th.debug(loc.toString());
      loc = new Locale("aDd","BFG","adg");
      th.debug(loc.toString());
      loc = new Locale("be","BEF_G","a_f_g");
      th.debug(loc.toString());
      loc = new Locale("ac_d","BEFG","adfg");
      th.debug(loc.toString());
      loc = new Locale("","BEFG","adfg");
      th.debug(loc.toString());
      loc = new Locale("a","BEFG","adfg");
      th.debug(loc.toString());
      loc = new Locale("yi","BEFG","adfg");
      th.debug(loc.toString());
      loc = new Locale("id","BE_FG","ad_fg");
      th.debug(loc.toString());
      th.debug(loc.getDisplayCountry());
      loc = new Locale("zh","tw","a_d_f_g");
      th.debug(loc.toString());
//      Locale.setDefault(loc);
      th.debug(Locale.getDefault().toString());
      th.debug(loc.getISO3Language());
      loc = new Locale("zoth","cn","a_d_f_g");
      th.debug(loc.toString());
//      th.debug(loc.getISO3Language());
      th.debug(loc.getDisplayCountry());
      th.debug(Locale.US.getDisplayCountry());
      th.debug(Locale.US.getDisplayLanguage());
      th.debug(Locale.getDefault().getDisplayName());
      th.debug(Locale.US.getDisplayName());
*/
