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

/*
** $Id: Date.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
*/

package java.util;

import java.io.NotActiveException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Date implements java.io.Serializable, Cloneable, Comparable {

  private static final long serialVersionUID = 7523967970034938905L; 

  private transient long millisSince1jan1970;

  private native void create_ymdhms(int year, int month, int date, int hours, int minutes, int seconds);

  public Date() {
    millisSince1jan1970 = System.currentTimeMillis();
  }

  public Date(long time) {
    millisSince1jan1970 = time;
  }
  
  public Date(int year, int month, int date, int hours, int minutes, int seconds) {
    create_ymdhms(year, month, date, hours, minutes, seconds);
  }

  public Date(int year, int month, int date, int hours, int minutes) {
    create_ymdhms(year, month, date, hours, minutes, 0);
  }

  public Date(int year, int month, int date) {
    create_ymdhms(year, month, date, 0, 0, 0);
  }

  public Date(String date){
    try {
      millisSince1jan1970 = java.text.DateFormat.getInstance().parse(date).millisSince1jan1970;
    }
    catch(java.text.ParseException pe){
      throw new IllegalArgumentException();
    }
  }

/**
* this method was declared native but now plain java code
*
*/
  public String toString(){
        StringBuffer buffer = new StringBuffer(32);
        GregorianCalendar cal = new GregorianCalendar(2000,0,1);
        cal.setTime(this);
        String s;
        int i = cal.get(Calendar.DAY_OF_WEEK);
        switch (i) {
        case Calendar.FRIDAY:
        	s="Fri ";
        	break;
        case Calendar.SATURDAY:
        	s="Sat ";
        	break;
        case Calendar.SUNDAY:
        	s="Sun ";
        	break;
        case Calendar.MONDAY:
        	s="Mon ";
        	break;
        case Calendar.TUESDAY:
        	s="Tue ";
        	break;
        case Calendar.WEDNESDAY:
        	s="Wed ";
        	break;
        case Calendar.THURSDAY:
        	s="Thu ";
        	break;
        default:
        	s="Foo ";
        }
        buffer.append(s);
        i = cal.get(Calendar.MONTH);
        switch (i) {
        case Calendar.JANUARY:
        	s="Jan ";
        	break;
        case Calendar.FEBRUARY:
        	s="Feb ";
        	break;
        case Calendar.MARCH:
        	s="Mar ";
        	break;
        case Calendar.APRIL:
        	s="Apr ";
        	break;
        case Calendar.MAY:
        	s="May ";
        	break;
        case Calendar.JUNE:
        	s="Jun ";
        	break;
        case Calendar.JULY:
        	s="Jul ";
        	break;
        case Calendar.AUGUST:
        	s="Aug ";
        	break;
        case Calendar.SEPTEMBER:
        	s="Sep ";
        	break;
        case Calendar.OCTOBER:
        	s="Oct ";
        	break;
        case Calendar.NOVEMBER:
        	s="Nov ";
        	break;
        case Calendar.DECEMBER:
        	s="Dec ";
        	break;
        case Calendar.UNDECIMBER:
        	s="Und ";
        	break;        	
        default:
        	s="Foo ";
        }
        buffer.append(s);
        i = cal.get(Calendar.DAY_OF_MONTH);
        char c[] = new char[3];
        c[0] = (char)('0' + i/10);
        c[1] = (char)('0' + i%10);
        c[2] = ' ';
        buffer.append(c);
        i = cal.get(Calendar.HOUR_OF_DAY);
        c[0] = (char)('0' + (i/10)%3);
        c[1] = (char)('0' + i%10);
        c[2] = ':';
        buffer.append(c);
        i = cal.get(Calendar.MINUTE);
        c[0] = (char)('0' + ((i/10)%6));
        c[1] = (char)('0' + i%10);
        buffer.append(c);
        i = cal.get(Calendar.SECOND);
        c[0] = (char)('0' + (i/10)%6);
        c[1] = (char)('0' + i%10);
        c[2] = ' ';
        buffer.append(c);
    	buffer.append(cal.getTimeZone().getID());
    	buffer.append(" ");
        buffer.append(cal.get(Calendar.YEAR));
        return buffer.toString();
  }

  public Object clone(){
    try {
      return super.clone();
    }
    catch(CloneNotSupportedException cnse){
      return null;
    }
  }

  public boolean equals(Object obj){
    if(!(obj instanceof Date)){
      return false;
    }
    Date date = (Date)obj;
    return this.millisSince1jan1970 == date.millisSince1jan1970;
  }

  public int hashCode(){
    return ((int)millisSince1jan1970) ^ ((int)(millisSince1jan1970>>32));
  }

/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public int getYear(){

  System.out.println("this is a deprecated method\n  calling it has no effect\n  it returns 2001");
  return 2001;
  }


/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public void setYear(int year){

  System.out.println("this is a deprecated method\n  calling it has no effect");
  }


/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public int getMonth(){

  System.out.println("this is a deprecated method\n  calling it has no effect\n  returning 1; ");
  return 1;
  }


/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* 	@deprecated
*/
  public void setMonth(int month){

  System.out.println("this is a deprecated method\n  calling it has no effect");
  }


/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public int getDate(){

  System.out.println("this is a deprecated method\n  calling it has no effect\n  returning 1; ");
  return 1;
  }


/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public void setDate(int date){

  System.out.println("this is a deprecated method\n  calling it has no effect");
  }


/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public int getDay(){

  System.out.println("this is a deprecated method\n  calling it has no effect\n  returning 0;");
  return 0;
  }


/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public  int getHours(){

  System.out.println("this is a deprecated method\n  calling it has no effect");
  return 0;
  }


/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public void setHours(int hours){

  System.out.println("this is a deprecated method\n  calling it has no effect");
  }


/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public int getMinutes(){

  System.out.println("this is a deprecated method\n  calling it has no effect\n  returning 0;");
  return 0;
  }


/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public void setMinutes(int minutes){

  System.out.println("this is a deprecated method\n  calling it has no effect");
  }


/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public int getSeconds(){

  System.out.println("this is a deprecated method\n  calling it has no effect\n  returning 0;");
  return 0;
  }


/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public void setSeconds(int seconds){

  System.out.println("this is a deprecated method\n  calling it has no effect");
  }

  public long getTime() {
    return millisSince1jan1970;
  }

  public void setTime(long time) {
    this.millisSince1jan1970 = time;
  }

  public boolean before(Date when) {
    return this.millisSince1jan1970 < when.millisSince1jan1970;
  }

  public boolean after(Date when) {
    return this.millisSince1jan1970 > when.millisSince1jan1970;
  }

/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public String toLocaleString(){
    System.out.println("this is a deprecated method\n  calling it has no effect\n  returning \"no time\";");
    return "no time";
 }



/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public String toGMTString(){
    System.out.println("this is a deprecated method\n  calling it has no effect\n  returning \"no time\";");
    return "no time";
  }


/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public int getTimezoneOffset(){

    System.out.println("this is a deprecated method\n  calling it has no effect\n  returning 0;");
    return 0;
  }


/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
* @deprecated
*/
  public static long UTC(int year, int month, int date, int hours, int minutes, int seconds){
    System.out.println("this is a deprecated method\n  calling it has no effect\n  returning 0;");
    return 0L;
  }



/**
* @status Empty body	
* @remarks DON'T use this method it is depricated!!!
*	@deprecated
*/
  public static long parse(String s) throws IllegalArgumentException{
    System.out.println("this is a deprecated method\n  calling it has no effect\n  returning 0;");
    return 0L;
  }

  public int compareTo(Object obj){
    return compareTo((Date)obj);
  }

  public int compareTo(Date date) {
    if (millisSince1jan1970 < date.millisSince1jan1970){
      return -1;
    }
    else if(millisSince1jan1970 > date.millisSince1jan1970){
      return 1;
    }
    return 0;
  }

  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    millisSince1jan1970 = s.readLong();
  }

  private void writeObject(ObjectOutputStream o) throws IOException, NotActiveException {
    o.defaultWriteObject();
    o.writeLong(millisSince1jan1970);
  }

}
