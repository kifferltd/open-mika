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


package gnu.testlet.wonka.util.Calender;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.*;

/**
*  this file contains test for java.util.Calendar  <br>
*                                                  <br>
*  Calendar is an abstract class so we SMExCalender.   <br>
*  This class extends Calender and has some extra methods  <br>
*  to get the values of the protected fields  <br>
*                                             <br>
*  Calendar has the following abstract methods:<br>
*                                              <br>
*    add --> implemented in wonka              <br>
*    computeFields --> implemented in wonka   <br>
*    computeTime --> implemented in wonka      <br>
*    getGreatestMinimum --> not implemented in wonka <br>
*    getLeastMaximum --> not implemented in wonka   <br>
*    getMaximum --> not implemented in wonka   <br>
*    getMinimum --> not implemented in wonka  <br>
*    public abstract void roll(int, boolean) --> not implemented in wonka <br>
*/
public class SMCalenderTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.Calendar");
       test_Calendar();
       test_MonthConstants();
       test_Day_of_WeekConstants();
       test_FieldConstants();
       test_TimeConstants();
       test_protectedFields();
       test_getInstance();
       test_after();
       test_before();
       test_clear();
       test_get();
       test_getTime();
       test_isSet();
       test_roll();
       test_set();
       test_setTime();
       test_getFirstDayOfWeek();
       test_getMinimalDaysInFirstWeek();
       test_isLenient();
       test_setFirstDayOfWeek();
       test_setLenient();
       test_setMinimalDaysInFirstWeek();
       test_getActualMaximum();
       test_getActualMinimum();
       test_getAvailableLocales();
       test_getTimeZone();
       test_setTimeZone();
       test_complete();
       test_getTimeInMillis();
       test_internalGet();
       test_setTimeInMillis();
       test_clone();
       test_equals();
       test_hashCode();
       test_toString();
     }


/**
*   not implemented   <br>
*   test on the Constructors
*   lack of functionalty
*/
  public void test_Calendar(){
    th.checkPoint("Calendar()");
    SMExCalender xcal = new SMExCalender();
//    th.debug("Calendar is lenient = "+xcal.isLenient());

    th.checkPoint("Calendar(java.util.TimeZone,java.util.Locale)");
  }

/**
*   implemented  <br>
*   test the values of the various constants
*/
  public void test_MonthConstants(){
    th.checkPoint("JANUARY(public)int");
    th.check(Calendar.JANUARY == 0);
    th.checkPoint("FEBRUARY(public)int");
    th.check(Calendar.FEBRUARY == 1);
    th.checkPoint("MARCH(public)int");
    th.check(Calendar.MARCH == 2);
    th.checkPoint("APRIL(public)int");
    th.check(Calendar.APRIL == 3);
    th.checkPoint("MAY(public)int");
    th.check(Calendar.MAY == 4);
    th.checkPoint("JUNE(public)int");
    th.check(Calendar.JUNE == 5);
    th.checkPoint("JULY(public)int");
    th.check(Calendar.JULY == 6);
    th.checkPoint("AUGUST(public)int");
    th.check(Calendar.AUGUST == 7);
    th.checkPoint("SEPTEMBER(public)int");
    th.check(Calendar.SEPTEMBER == 8);
    th.checkPoint("OCTOBER(public)int");
    th.check(Calendar.OCTOBER == 9);
    th.checkPoint("NOVEMBER(public)int");
    th.check(Calendar.NOVEMBER == 10);
    th.checkPoint("DECEMBER(public)int");
    th.check(Calendar.DECEMBER == 11);
    th.checkPoint("UNDECIMBER(public)int");
    th.check(Calendar.UNDECIMBER == 12);

  }

/**
*   implemented <br>
*   test the values of the various constants
*/
  public void test_Day_of_WeekConstants(){
    th.checkPoint("SUNDAY(public)int");
    th.check(Calendar.SUNDAY == 1);
    th.checkPoint("MONDAY(public)int");
    th.check(Calendar.MONDAY == 2);
    th.checkPoint("TUESDAY(public)int");
    th.check(Calendar.TUESDAY == 3);
    th.checkPoint("WEDNESDAY(public)int");
    th.check(Calendar.WEDNESDAY == 4);
    th.checkPoint("THURSDAY(public)int");
    th.check(Calendar.THURSDAY == 5);
    th.checkPoint("FRIDAY(public)int");
    th.check(Calendar.FRIDAY == 6);
    th.checkPoint("SATURDAY(public)int");
    th.check(Calendar.SATURDAY == 7);

  }

/**
*   not implemented  <br>
*   test the values of the various constants
*/
  public void test_FieldConstants(){
    th.checkPoint("FIELD_COUNT(public)int");
    th.check(Calendar.FIELD_COUNT == 17);
  }

/**
*   implemented  <br>
*   test the values of the various constants
*/
  public void test_TimeConstants(){
    th.checkPoint("AM(public)int");
    th.check(Calendar.AM == 0);
    th.checkPoint("PM(public)int");
    th.check(Calendar.PM == 1);

  }

/**
* not implemented  <br>
* test the values of the various constants <br>
* these test need to be altered whenever  <br>
* functionality is added to Calender !!!  <br>
* newly added functions might need to change these fields <br>
* test the fields with the other functions
*/
  public void test_protectedFields(){
    th.checkPoint("()");

  }

/**
*   not implemented  <br>
*
*/
  public void test_getInstance(){
    th.checkPoint("getInstance()");

  }

/**
*   not implemented <br>
*
*/
  public void test_after(){
    th.checkPoint("after()boolean");

  }

/**
*   not implemented <br>
*
*/
  public void test_before(){
    th.checkPoint("()");

  }

/**
*   not implemented <br>
*   --> not in WONKA
*/
  public void test_clear(){
    th.checkPoint("()");

  }

/**
*   not implemented <br>
*
*/
  public void test_get(){
    th.checkPoint("()");

  }

/**
*   not implemented <br>
*  --> not in WONKA
*/
  public void test_getTime(){
    th.checkPoint("()");

  }

/**
*   not implemented  <br>
*   --> not in WONKA
*/
  public void test_isSet(){
    th.checkPoint("()");

  }

/**
*   not implemented <br>
*   --> not in WONKA
*/
  public void test_roll(){
    th.checkPoint("()");

  }

/**
*   not implemented  <br>
*   --> not in WONKA
*/
  public void test_set(){
    th.checkPoint("()");

  }

/**
*   not implemented  <br>
*
*/
  public void test_setTime(){
    th.checkPoint("()");

  }

/**
*   not implemented <br>
*   --> not in WONKA
*/
  public void test_getFirstDayOfWeek(){
    th.checkPoint("()");

  }

/**
*   not implemented  <br>
*   --> not in WONKA
*/
  public void test_getMinimalDaysInFirstWeek(){
    th.checkPoint("()");

  }

/**
*   not implemented  <br>
*   --> not in WONKA
*/
  public void test_isLenient(){
    th.checkPoint("()");

  }

/**
*   not implemented <br>
*   --> not in WONKA
*/
  public void test_setFirstDayOfWeek(){
    th.checkPoint("()");

  }

/**
*   not implemented <br>
*   --> not in WONKA
*/
  public void test_setLenient(){
    th.checkPoint("()");

  }

/**
*   not implemented <br>
*   --> not in WONKA
*/
  public void test_setMinimalDaysInFirstWeek(){
    th.checkPoint("()");

  }

/**
*   not implemented  <br>
*   --> since jdk 1.2  <br>
*   --> not in WONKA
*/
  public void test_getActualMaximum(){
    th.checkPoint("()");

  }

/**
*   not implemented  <br>
*   --> since jdk 1.2 <br>
*   --> not in WONKA
*/
  public void test_getActualMinimum(){
    th.checkPoint("()");

  }

/**
*   not implemented <br>
*   --> not in WONKA
*/
  public void test_getAvailableLocales(){
    th.checkPoint("()");

  }

/**
*   not implemented <br>
*   --> not in WONKA
*/
  public void test_getTimeZone(){
    th.checkPoint("()");

  }

/**
*   not implemented <br>
*   --> not in WONKA
*/
  public void test_setTimeZone(){
    th.checkPoint("()");

  }

/**
*   not implemented <br>
*
*/
  public void test_complete(){
    th.checkPoint("()");

  }

/**
*   not implemented  <br>
*
*/
  public void test_getTimeInMillis(){
    th.checkPoint("()");

  }

/**
*   not implemented  <br>
*   --> not in WONKA
*/
  public void test_internalGet(){
    th.checkPoint("()");

  }

/**
*   not implemented <br>
*   --> not in WONKA
*/
  public void test_setTimeInMillis(){
    th.checkPoint("()");

  }

/**
*   not implemented  <br>
*   --> not in WONKA     <br>
*        --> inherited from Object
*/
  public void test_clone(){
    th.checkPoint("()");

  }

/**
*   not implemented   <br>
*    --> not in WONKA   <br>
*        --> inherited from Object
*/
  public void test_equals(){
    th.checkPoint("()");

  }

/**
*   not implemented  <br>
*    --> not in WONKA  <br>
*        --> inherited from Object
*/
  public void test_hashCode(){
    th.checkPoint("()");

  }

/**
*   not implemented  <br>
*    --> not in WONKA  <br>
*        --> inherited from Object
*/
  public void test_toString(){
    th.checkPoint("()");

  }

}
