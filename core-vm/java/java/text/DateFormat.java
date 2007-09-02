/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

/*
** $Id: DateFormat.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

import java.util.*;

public abstract class DateFormat extends Format {

  private static final long serialVersionUID = 7218322306649953788L;

  public static final int FULL = 0;
  public static final int LONG = 1;
  public static final int MEDIUM = 2;
  public static final int SHORT = 3;
  public static final int DEFAULT = MEDIUM;

  public static final int AM_PM_FIELD = 14;
  public static final int DATE_FIELD = 3;
  public static final int DAY_OF_WEEK_FIELD = 9;
  public static final int DAY_OF_WEEK_IN_MONTH_FIELD = 11;
  public static final int DAY_OF_YEAR_FIELD = 10;
  public static final int ERA_FIELD = 0;
  public static final int HOUR0_FIELD = 16;
  public static final int HOUR1_FIELD = 15;
  public static final int HOUR_OF_DAY0_FIELD = 5;
  public static final int HOUR_OF_DAY1_FIELD = 4;
  public static final int MILLISECOND_FIELD = 8;
  public static final int MINUTE_FIELD = 6;
  public static final int MONTH_FIELD = 2;
  public static final int SECOND_FIELD = 7;
  public static final int TIMEZONE_FIELD = 17;
  public static final int WEEK_OF_MONTH_FIELD = 13;
  public static final int WEEK_OF_YEAR_FIELD = 12;
  public static final int YEAR_FIELD = 1;

  public static Locale[] getAvailableLocales(){
    return new Locale[0];
  }

  public final static DateFormat getDateInstance(){
    return getDateInstance(DEFAULT, Locale.getDefault());
  }

  public final static DateFormat getDateInstance(int style){
    return getDateInstance(style, Locale.getDefault());
  }

  private static final String[] DATESTYLES = {"EEEE, MMMM d, yyyy G", "MMMM d, yyyy", "d-MMM-yy", "M/d/yy"};
  private static final String[] TIMESTYLES = {"K:mm:ss 'o''clock' a z", "K:mm:ss a z", "K:mm:ss a", "K:mm a"};

  public final static DateFormat getDateInstance(int style, Locale loc){
    return new SimpleDateFormat(DATESTYLES[style], loc);
  }

  public final static DateFormat getDateTimeInstance(){
    return getDateTimeInstance(DEFAULT, DEFAULT, Locale.getDefault());
  }

  public final static DateFormat getDateTimeInstance(int date, int time){
    return getDateTimeInstance(date,time, Locale.getDefault());
  }

  public final static DateFormat getDateTimeInstance(int date, int time , Locale loc){
    String pattern = DATESTYLES[date]+' '+TIMESTYLES[time];
    return new SimpleDateFormat(pattern, loc);
  }

  public final static DateFormat getInstance(){
    return getDateTimeInstance(SHORT,SHORT,Locale.getDefault());
  }

  public final static DateFormat getTimeInstance(){
    return getTimeInstance(DEFAULT, Locale.getDefault());
  }

  public final static DateFormat getTimeInstance(int style){
    return getTimeInstance(style, Locale.getDefault());
  }

  public final static DateFormat getTimeInstance(int style, Locale loc){
    return new SimpleDateFormat(TIMESTYLES[style], loc);
  }

  protected Calendar calendar;
  protected NumberFormat numberFormat;

  protected DateFormat() { }

  public Object clone() {
    DateFormat df = (DateFormat) super.clone();
    df.calendar = (Calendar) this.calendar.clone();
    df.numberFormat = (NumberFormat) this.numberFormat.clone();
    return df;
  }

  public boolean equals(Object o){
    if(!(o instanceof DateFormat)){
      return false;
    }
    DateFormat df = (DateFormat) o;
    return this.calendar.equals(df.calendar)
        && this.numberFormat.equals(df.numberFormat);
  }

  public final StringBuffer format(Object o, StringBuffer buffer, FieldPosition pos){
    Date date = ((o instanceof Number) ? new Date(((Number)o).longValue()) : (Date)o);
    return format(date, buffer, pos);
  }

  public abstract StringBuffer format(Date d, StringBuffer buffer, FieldPosition pos);

  public final String format(Date date){
    return format(date, new StringBuffer(64), new FieldPosition(0)).toString();
  }

  public Calendar getCalendar(){
    return calendar;
  }

  public NumberFormat getNumberFormat(){
    return numberFormat;
  }

  public TimeZone getTimeZone(){
    return calendar.getTimeZone();
  }

  public int hashCode(){
    return calendar.hashCode() ^ numberFormat.hashCode();
  }

  public boolean isLenient(){
    return calendar.isLenient();
  }

  public Date parse(String date) throws ParseException {
    ParsePosition pp = new ParsePosition(0);
    Date d = parse(date, pp);
    if(d == null){
      throw new ParseException("parsing failed!", pp.getErrorIndex());
    }
    return d;
  }

  public abstract Date parse(String date, ParsePosition pp);

  public Object parseObject(String date, ParsePosition pp){
    return parse(date, pp);
  }

  public void setCalendar(Calendar cal){
    calendar = cal;
  }

  public void setLenient(boolean lenient){
    calendar.setLenient(lenient);
  }

  public void setNumberFormat(NumberFormat nf){
    numberFormat = nf;
  }

  public void setTimeZone(TimeZone tz){
    calendar.setTimeZone(tz);
  }
}
