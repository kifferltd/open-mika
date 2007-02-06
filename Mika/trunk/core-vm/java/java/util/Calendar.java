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
** $Id: Calendar.java,v 1.8 2006/04/18 11:35:28 cvs Exp $
*/

package java.util;

import java.util.Date;

public abstract class Calendar implements java.io.Serializable, Cloneable {

  private static final long serialVersionUID = -1807547505821590642L;
  // Calendar fields
  public final static int MILLISECOND          = 14;
  public final static int SECOND               = 13;
  public final static int MINUTE               = 12;
  public final static int HOUR                 = 10;
  public final static int HOUR_OF_DAY          = 11;
  public final static int AM_PM                = 9;
  public final static int DAY_OF_WEEK          = 7;
  public final static int DAY_OF_MONTH         = 5;
  public final static int DATE                 = 5;
  public final static int DAY_OF_WEEK_IN_MONTH = 8;
  public final static int DAY_OF_YEAR          = 6;
  public final static int WEEK_OF_MONTH        = 4;
  public final static int WEEK_OF_YEAR         = 3;
  public final static int MONTH                = 2;
  public final static int YEAR                 = 1;
  public final static int ERA                  = 0;
  public final static int ZONE_OFFSET          = 15;
  public final static int DST_OFFSET           = 16;
  public final static int FIELD_COUNT          = 17;
  
  // MONTH Constants
  public final static int JANUARY   = 0;
  public final static int FEBRUARY	= 1;
  public final static int MARCH			= 2;
  public final static int APRIL			= 3;
  public final static int MAY 			= 4;
  public final static int JUNE			= 5;
  public final static int JULY			= 6;
  public final static int AUGUST		= 7;
  public final static int SEPTEMBER	= 8;
  public final static int OCTOBER		= 9;
  public final static int NOVEMBER	=10;
  public final static int DECEMBER	=11;
  public final static int UNDECIMBER=12;

  // Day-of-Week Constants
  public final static int SUNDAY		= 1;
  public final static int MONDAY		= 2;
  public final static int TUESDAY		= 3;
  public final static int WEDNESDAY		= 4;
  public final static int THURSDAY		= 5;
  public final static int FRIDAY		= 6;
  public final static int SATURDAY		= 7;


  public final static int AM = 0;
  public final static int PM = 1;


  protected long time;
  protected int[] fields = new int[FIELD_COUNT];;

  protected boolean isTimeSet;
  protected boolean areFieldsSet;
  protected boolean isSet[] = new boolean[FIELD_COUNT];

  private int minimalDaysInFirstWeek = 1;
  private int firstDayOfWeek = 2;

  private boolean lenient=true;
  private TimeZone zone;

  abstract public void add(int fld, int amount);


/*
* --> after was declared abstract but this changed in version 1.2
* NOTE: every Class which extended Calender will overwrite this method
* if the class was defined before these changes
*/
  public boolean after(Object cal){
  	complete();
  	if (!(cal instanceof Calendar)) return false;
  	return  (time > ((Calendar)cal).getTime().getTime());

  }

/*
* --> before was declared abstract but this changed in version 1.2
* NOTE: every Class which extended Calender will overwrite this method
* if the class was defined before these changes
*/
  public boolean before(Object cal){
  	complete();
  	if (!(cal instanceof Calendar)) return false;
  	return  (time < ((Calendar)cal).getTime().getTime());
  }

  protected Calendar() {
    this(TimeZone.getDefault(),Locale.getDefault());
  }

  protected Calendar(TimeZone tz, Locale loc) {
    if (tz == null || loc == null) {
    	throw new NullPointerException();
    }
    this.zone = tz;
  }

  public static synchronized Calendar getInstance() {
    return new GregorianCalendar();
  }
  public static synchronized Calendar getInstance(TimeZone tz) {
    return new GregorianCalendar(tz);
  }
  public static synchronized Calendar getInstance(Locale loc) {
    return new GregorianCalendar(loc);
  }
  public static synchronized Calendar getInstance(TimeZone tz, Locale loc) {
    return new GregorianCalendar(tz, loc);
  }

  // CG 20041112 - public in 1.4.2
  public long getTimeInMillis() {
    if (!isTimeSet) computeTime();
    return time;
  }

  public final void setTime(Date d) {
    time = d.getTime();
    isTimeSet = true;
    areFieldsSet = false;
    computeFields();
  }

  // CG 20041112 - public in 1.4.2
  public void setTimeInMillis(long Millis) {
    time = Millis;
    isTimeSet = true;
    areFieldsSet=false;
    computeFields();
  }

  /**
  **  - invalidate fields that are incompatible with the one we are setting.
  ** Package-protected so accessible to GregorianCalendar ...
  */
  void clearIncompatibleFields(int fld) {
   areFieldsSet = false;
    switch (fld) {
      case MONTH:
        isSet[DAY_OF_YEAR] = false;
        isSet[DAY_OF_WEEK] = false;
        isSet[WEEK_OF_YEAR] = false;
        break;

      case DAY_OF_MONTH:
        isSet[WEEK_OF_MONTH] = false;
        isSet[DAY_OF_WEEK] = false;
        isSet[DAY_OF_WEEK_IN_MONTH] = false;
        break;

      case DAY_OF_WEEK:
        isSet[DAY_OF_MONTH] = false;
        isSet[DAY_OF_YEAR] = false;
        isSet[DAY_OF_MONTH] = false;
        break;

      case WEEK_OF_MONTH:
        isSet[DAY_OF_WEEK_IN_MONTH] = false;
        break;

      case DAY_OF_WEEK_IN_MONTH:
        isSet[WEEK_OF_MONTH] = false;
        break;

      case DAY_OF_YEAR:
        isSet[DAY_OF_WEEK] = false;
        isSet[WEEK_OF_YEAR] = false;
        isSet[MONTH] = false;
        isSet[DAY_OF_MONTH] = false;
        isSet[WEEK_OF_MONTH] = false;
        isSet[DAY_OF_WEEK_IN_MONTH] = false;
        break;

      case WEEK_OF_YEAR:
        isSet[DAY_OF_YEAR] = false;
        isSet[MONTH] = false;
        isSet[DAY_OF_MONTH] = false;
        isSet[WEEK_OF_MONTH] = false;
        isSet[DAY_OF_WEEK] = false;
        isSet[DAY_OF_WEEK_IN_MONTH] = false;
        break;

      case HOUR_OF_DAY:
        isSet[AM_PM] = false;
        isSet[HOUR] = false;
        break;

      case HOUR:
      case AM_PM:
        isSet[HOUR_OF_DAY] = false;
        break;

      default:
        updateAreFieldsSet();
    }
  }

  /**
  **	basic implementation --> no checks ...
  */
  // CG 20041112 - non-final in 1.4.2
  public void set(int fld , int nv) {
      fields[fld] = nv;
      isSet[fld] = true;
      clearIncompatibleFields(fld);
      isTimeSet = false;
      areFieldsSet = false;
  }

  public final void set(int year, int month, int date) {
	fields[YEAR] = year;
        isSet[YEAR] = true;
	fields[MONTH] = month;
        isSet[MONTH] = true;
	fields[DATE] = date;
        isSet[DATE] = true;
        updateAreFieldsSet();
        isTimeSet = false;
  }

  public final void set(int year, int month, int date, int hourOfDay, int minute) {
	fields[YEAR] = year;
        isSet[YEAR] = true;
	fields[MONTH] = month;
        isSet[MONTH] = true;
	fields[DATE] = date;
        isSet[DATE] = true;
	fields[HOUR_OF_DAY] = hourOfDay;
        isSet[HOUR_OF_DAY] = true;
	fields[MINUTE] = minute;
        isSet[MINUTE] = true;
        updateAreFieldsSet();
        isTimeSet = false;
  }

  public final void set(int year, int month, int date, int hourOfDay, int minute, int second) {
	fields[YEAR] = year;
        isSet[YEAR] = true;
	fields[MONTH] = month;
        isSet[MONTH] = true;
	fields[DATE] = date;
        isSet[DATE] = true;
	fields[HOUR_OF_DAY] = hourOfDay;
        isSet[HOUR_OF_DAY] = true;
	fields[MINUTE] = minute;
        isSet[MINUTE] = true;
	fields[SECOND] = second;
        isSet[SECOND] = true;
        updateAreFieldsSet();
     	isTimeSet = false;
  }
  // CG 20041112 - non-final in 1.4.2
  public int get (int fld) {
    complete();
    return fields[fld];
  }

  protected void complete() {
    if (!isTimeSet) {
    	computeTime();
    }
    if (!areFieldsSet) {
    	computeFields();
    }
  }

  private void updateAreFieldsSet() {
  	areFieldsSet = isSet[0];
  	for (int i=1 ; i < FIELD_COUNT ; i++){
  	 	areFieldsSet &= isSet[i];
  	}

  }

  protected abstract void computeFields();
  
  protected abstract void computeTime();
  
  public final Date getTime() {
     return new Date(getTimeInMillis());
  }

  public TimeZone getTimeZone() {
   	return zone;
  }

  public void setTimeZone(TimeZone tz) {
  	if (tz == null) {
  		throw new NullPointerException();
  	}
  	this.zone = tz;
  }
  public final void clear() {
    for (int i = 0; i < FIELD_COUNT; i++) {
      fields[i] = 0;
      areFieldsSet = false;
    }
    isTimeSet = false;
  }

  public final void clear(int fld) {
    	fields[fld] = 0;
    	areFieldsSet = false;
    	isTimeSet = false;
  }

  protected final int internalGet(int fld) {
   	return fields[fld];
  }

  public boolean isLenient() {
   	return lenient;
  }

  public final boolean isSet(int fld) {
   	return isSet[fld];
  }

  public void setLenient(boolean lenient) {
   	this.lenient = lenient;
  }


  public Object clone() {
    try {
      Calendar cal = (Calendar) super.clone();
      cal.fields = fields;
      cal.isSet =  isSet;
      cal.zone = (TimeZone) zone.clone();
      return cal;
    }
    catch(CloneNotSupportedException cnse){
      return null;
    }
  }

  public int getFirstDayOfWeek(){
  	return firstDayOfWeek;
  }

  public boolean equals(Object o) {
    if(!(o instanceof Calendar)){
      return false;
    }
    Calendar cal = (Calendar)o;
    complete();
    cal.complete();
    return this.time == cal.time
        && this.zone.equals(cal.zone);
  }

  public int hashCode() {
    return (int)time ^ ((int)(time>>32)) ^ zone.hashCode();
  }

  public String toString() {
    StringBuffer buf = new StringBuffer(getClass().getName()).append('@')
            .append(Integer.toHexString(System.identityHashCode(this)));
    for(int i = 0 ; i < (FIELD_COUNT - 1) ; i++) {
      buf.append(" Field[").append(i).append("] = ").append(get(i)).append(',');
    }
    return buf.append(" Field[").append(FIELD_COUNT-1).append("] = ").append(get(FIELD_COUNT-1)).toString();
  }

  public void setFirstDayOfWeek(int value){
    if(value < 1 || value > 7){
      throw new IllegalArgumentException();
    }
    firstDayOfWeek = value;
  }

  public int getMinimalDaysInFirstWeek(){
    return minimalDaysInFirstWeek;
  }

  public void setMinimalDaysInFirstWeek(int value){
    if(value < 1 || value > 7){
      throw new IllegalArgumentException();
    }
    minimalDaysInFirstWeek = value;
  }

  public abstract void roll(int fld, boolean up);

  public void roll(int fld, int amt) {
    boolean up = amt >= 0;
    if(!up){
      amt = -amt;
    }
    for(int i=0 ; i < amt ; i++){
      roll(fld,up);
    }
  }

  public int getActualMaximum(int fld) {
    complete();
    Calendar cal = (Calendar)clone();
    cal.setLenient(false);
    complete();
    int value = cal.fields[fld];
    do {
      cal.roll(fld,true);
      cal.complete();
      if(value > cal.fields[fld]){
        return value;
      }
      else {
        value = cal.fields[fld];
      }

    } while(true);
  }

  public int getActualMinimum(int fld) {
    complete();
    Calendar cal = (Calendar)clone();
    cal.setLenient(false);
    complete();
    int value = cal.fields[fld];
    do {
      cal.roll(fld,false);
      cal.complete();
      if(value < cal.fields[fld]){
        return value;
      }
      else {
        value = cal.fields[fld];
      }

    } while(true);
  }

  public abstract int getGreatestMinimum(int fld);
  public abstract int getLeastMaximum(int fld);
  public abstract int getMaximum(int fld);
  public abstract int getMinimum(int fld);

   public static Locale[] getAvailableLocales(){
     Locale[] l = new Locale[1];
     l[0] = Locale.getDefault();
     return l;
   }

}
