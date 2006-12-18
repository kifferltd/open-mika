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
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: GregorianCalendar.java,v 1.7 2006/04/05 13:18:45 cvs Exp $
*/

package java.util;

/**
**  serialVersionUID is hardcoded to allow serialization to work
**  we will not support all depricated methods ...
**
*/

public class GregorianCalendar extends Calendar {

  private static final long serialVersionUID = -8125100834729963327L;

  private static final int[] DAYS_IN_MONTH = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
  private static final int[] DAYS_IN_LEAPMONTH = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

  public static final int AD = 1;
  public static final int BC = 0;

  private long gregorianCutover = -12219292800000L;

  public GregorianCalendar() {
    super();
    setTimeInMillis(System.currentTimeMillis());
  }

  public GregorianCalendar(TimeZone tz, Locale loc) {
    super(tz,loc);
    setTimeInMillis(System.currentTimeMillis());
  }

  public GregorianCalendar(TimeZone tz) {
    super(tz,Locale.getDefault());
    setTimeInMillis(System.currentTimeMillis());
  }
  public GregorianCalendar(Locale loc) {
    super(TimeZone.getDefault(),loc);
    setTimeInMillis(System.currentTimeMillis());
  }

  public GregorianCalendar(int year, int month, int date) {
    super();
    set(year, month, date);
  }

  public GregorianCalendar(int year, int month, int date, int hour, int minute) {
    super();
    set(year, month, date, hour, minute);
  }

  public GregorianCalendar(int year, int month, int date, int hour, int minute, int second) {
    super();
    set(year, month, date, hour, minute, second);
  }


  public void add (int fld, int amount)
    throws IllegalArgumentException
  {
    if (fld==DST_OFFSET || fld==ZONE_OFFSET) {
      throw new IllegalArgumentException();
    }
    set(fld, fields[fld] + amount);
  }

  public boolean after (Object cal) {
    if (!(cal instanceof GregorianCalendar)) {
      return false;
    }
    complete();
    return (this.getTimeInMillis() > ((GregorianCalendar)cal).getTimeInMillis());
  }

  public boolean before (Object cal) {
    if (!(cal instanceof GregorianCalendar)) {
      return false;
    }
    complete();
    return (this.getTimeInMillis() < ((GregorianCalendar)cal).getTimeInMillis());
  }
/**
* We calculate the time using the rules described on page 270 of the Java class libraries
* there are no specification of the use of the millisecond field so I guess we take whatever is available
*/
  protected void computeTime() {
    //System.out.println("computing time ... "+this);
    if (!isTimeSet) {
      //date ...
      //first: year, month, day_of_month
      if (!isSet[YEAR]) {
        throw new IllegalArgumentException("no year set, please set the year if you like a time setting !");
      }
      int datecase=1;
      if ((!isSet[MONTH]) || (!isSet[DAY_OF_MONTH])){
        //second: year, month, week_of_month, day_of_week
        if(isSet[DAY_OF_WEEK] && isSet[MONTH] && isSet[WEEK_OF_MONTH]) {
          datecase=2;
          if (fields[DAY_OF_WEEK] < 1 || fields[DAY_OF_WEEK] > 7 ||
              fields[WEEK_OF_MONTH] < 1 || fields[WEEK_OF_MONTH] > 6 ) {
            throw new IllegalArgumentException();
          }
        }
        else {
          //third: year, month, day_of_week_in_month, day_of_week
          if (isSet[DAY_OF_WEEK] && isSet[MONTH] && isSet[DAY_OF_WEEK_IN_MONTH]) {
            datecase=3;
          }
          else {
            //fourth: year, day_of_year
            if (isSet[DAY_OF_YEAR]) {
              datecase=4;
            }
            else {
              //fifth: year, day_of_week, week_of_year
              if (isSet[DAY_OF_WEEK] && isSet[WEEK_OF_YEAR]) {
                datecase=5;
              }
              else {
                throw new IllegalArgumentException("please set enough values to determine a date !");
              }
            }
          }
        }      
      }
      //time ...
      //hour_of_day
      if (!isSet[HOUR_OF_DAY]) {
        //am_pm, hour ...
        if (isSet[AM_PM] && isSet[HOUR]) {
          fields[HOUR_OF_DAY] = fields[AM_PM] * 12 + fields[HOUR];
        }
        else {
          throw new IllegalArgumentException("no time settings, sorry !");
        }
      }
      settime(getFirstDayOfWeek(),datecase);
      TimeZone tz = getTimeZone();      
      if (fields[YEAR] > 0 ) {
        int millis = fields[MILLISECOND]+ 1000* fields[SECOND] + 60000* fields[MINUTE] + 3600000* fields[HOUR_OF_DAY];
        time -= tz.getOffset(AD, fields[YEAR], fields[MONTH], fields[DATE], fields[DAY_OF_WEEK], millis);
      }
      else {
        time -= tz.getRawOffset();
      }
    }
    isTimeSet = true;
  }

  protected void computeFields() {
    if (!areFieldsSet) {
      if (!isTimeSet) {
        throw new IllegalArgumentException();
      }
      //System.out.println("computing fields ... "+this);
      TimeZone tz = getTimeZone();
      int rawOffset = tz.getRawOffset();
      setfields(rawOffset);
      int millis = fields[MILLISECOND]+ 1000* fields[SECOND] + 60000* fields[MINUTE] + 3600000* fields[HOUR_OF_DAY];
      int dst =0;
      if (fields[YEAR] > 0 ) {
        dst = tz.getOffset(AD, fields[YEAR], fields[MONTH], fields[DATE], fields[DAY_OF_WEEK], millis);
        if (dst != rawOffset) {
          setfields(dst);
          dst -= rawOffset;
        }
        fields[ERA] = AD;     
      }
      else {
        fields[ERA] =  BC;
      }
      fields[HOUR] = fields[HOUR_OF_DAY]%12;
      fields[AM_PM] = fields[HOUR_OF_DAY]/12;
      fields[DAY_OF_WEEK_IN_MONTH] = (fields[DATE]-1)/7+1;
      int fdow = getFirstDayOfWeek();
      int i = Math.abs(( fields[DAY_OF_WEEK] - fdow )%7);
      fields[WEEK_OF_MONTH] = 1;
      int j;
      for ( j = fields[DAY_OF_MONTH]; j > i ; j = j - 7){     
        fields[WEEK_OF_MONTH]+=1;
      }
      fields[WEEK_OF_YEAR] = 1;
      for ( j = fields[DAY_OF_YEAR]; j > i ; j =j - 7){
        fields[WEEK_OF_YEAR]++;
      }
      fields[ZONE_OFFSET] = rawOffset;
      fields[DST_OFFSET] = dst;      
      for ( j=0;j<FIELD_COUNT;j++) isSet[j] = true;
      areFieldsSet = true;
    }
  }

  public boolean isLeapYear(int year) {
    if (year % 4 != 0) {
      return false;
    }
    if ((year < 1582) || year % 100 != 0) {
      return true;
    }
    if (year % 400 != 0) {
      return false;
    }
    return true;
  }

  public void roll(int fld, boolean up){
    roll(fld, up ? 1 : -1);
  }

  public void roll(int fld, int amt){
    complete();
    switch(fld){
      case MILLISECOND:
        int millis = get(fld);
        int value = millis+amt;        
        set(fld, value % 1000);
        break;
       case SECOND:
        int seconds = get(SECOND);
        value = seconds + amt;
        set(SECOND, value % 60);
        break;
      case MINUTE:
        int minutes = get(MINUTE);
        value = minutes + amt;
        set(MINUTE, value % 60);
        break;
      case HOUR:
        int hours = get(HOUR);
        value = hours + amt;
        set(HOUR, value % 24);
        break;
      case DATE:
        int date = get(DATE);
        value = date +amt;
        int month = get(MONTH);
        boolean leap = isLeapYear(get(YEAR));
        int days = (leap ? DAYS_IN_LEAPMONTH[month] : DAYS_IN_MONTH[month]);
        value = value % days;
        if (value == 0) {
          value = days;
        }
        set(DATE, value);        
        break;
      case MONTH:
        month = get(MONTH);
        date = get(DATE);
        value = month + amt;
        month = value % 12;
        leap = isLeapYear(get(YEAR));
        days = (leap ? DAYS_IN_LEAPMONTH[month] : DAYS_IN_MONTH[month]);
        if(date > days){
          set(DATE,days);
        }
        set(MONTH, month);
        break;
      case YEAR:
        int year = get(fld);
        int newval = year + amt;
        if(get(MONTH) == FEBRUARY && get(DATE)==29 && isLeapYear(year) && !isLeapYear(newval)){
          set(DATE,1);
          set(MONTH, Calendar.MARCH);
        }
        set(fld,newval);
        break;
      case ZONE_OFFSET:
      case DST_OFFSET:
        set(fld, get(fld) + amt);
        break;
      case AM_PM:
      case DAY_OF_WEEK:
      case DAY_OF_WEEK_IN_MONTH:
      case DAY_OF_YEAR:
      case WEEK_OF_MONTH:
      case WEEK_OF_YEAR:
      case ERA:
      case HOUR_OF_DAY:
        //TODO
        System.out.println("\nGregorianCalendar: calling roll on field which is not yet implemented !!!\n");
      default:
        //do nothing
    }
  }

  public int getGreatestMinimum(int fld){
    //TODO ...
    return -1;
  }

  public int getLeastMaximum(int fld){
    //TODO...
    return -1;
  }

  public int getMaximum(int fld){
    //TODO ...
    return -1;
  }

  public int getMinimum(int fld){
    //TODO ...
    return -1;
  }

  public void setGregorianChange(Date date){
    gregorianCutover = date.getTime();
  }

  public final Date getGregorianChange(){
    return new Date(gregorianCutover);
  }

  private native void setfields(int offset);
  private native void settime(int First_day_of_week, int datecase);
}
