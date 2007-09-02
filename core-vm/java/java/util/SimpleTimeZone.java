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



package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;

public class SimpleTimeZone extends TimeZone {
  private static final long serialVersionUID = -403250971215465050L;

  public static final int WALL_TIME     = 0;
  public static final int STANDARD_TIME = 1;
  public static final int UTC_TIME      = 2;

  private int dstSavings;
  private int endDay;
  private int endDayOfWeek;
  private int endMode;
  private int endMonth;
  private int endTime;
  private int rawOffset;
  private int serialVersionOnStream=2;
  private int startDay;
  private int startDayOfWeek;
  private int startMode;
  private int startMonth;
  private int startTime;
  private int startYear=0;

  private void readObject(ObjectInputStream stream)
     throws IOException, ClassNotFoundException  {
    stream.defaultReadObject();
    if(serialVersionOnStream == 0) {
      //TODO init extra fields
    }
  }
  
  private transient boolean hasEndRule;
  private transient boolean hasStartRule;

  private final static int DOM_MODE = 1; 	  //fixed day example 15 May
  private final static int DOW_IN_MONTH_MODE = 2; //Day of week in month: last monday of the month
  private final static int DOW_GE_DOM_MODE = 3;   //day of week after day in month : Sunday on or after 15 May
  private final static int DOW_LE_DOM_MODE = 4;   // day of week before day of month : Sunday on or before 15 May

  private boolean useDaylight;

  private final static byte[] monthLength = { 31, 28, 31, 30 , 31, 30, 31, 31, 30, 31, 30, 31 };

  public SimpleTimeZone(int rawOffset, String ID) {
	super();
	setID(ID);
	this.rawOffset = rawOffset;
	useDaylight = false;
	hasStartRule = false;
	hasEndRule = false;
  }

  public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDayOfWeekInMonth, int startDayOfWeek,
  	int startTime, int endMonth, int endDayOfWeekInMonth, int endDayOfWeek, int endTime ) {
        this(rawOffset, ID, startMonth, startDayOfWeekInMonth, startDayOfWeek, startTime, endMonth,
        		endDayOfWeekInMonth, endDayOfWeek, endTime, 60*60*1000); 	

  }

  public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDayOfWeekInMonth, int startDayOfWeek,
  	int startTime, int endMonth, int endDayOfWeekInMonth, int endDayOfWeek, int endTime, int dstSavings ) {
	  super();
	  setID(ID);
   	this.rawOffset = rawOffset;
	//(check for IllegalArguments
	if (  dstSavings < 0 || startTime < 0 || startTime >= 86400000 || endTime < 0 || endTime >= 86400000
	      || startMonth < Calendar.JANUARY || startMonth > Calendar.DECEMBER
	      || endMonth < Calendar.JANUARY || endMonth > Calendar.DECEMBER	
	   )  throw new IllegalArgumentException("1");
	if (  startDayOfWeekInMonth == 0 || startDayOfWeek > Calendar.SATURDAY || startDayOfWeek < (-Calendar.SATURDAY)	
	      || startDayOfWeekInMonth > 31 || startDayOfWeekInMonth < -31
	      || ( (startDayOfWeekInMonth > 5 || startDayOfWeekInMonth < -5)  && startDayOfWeek > 0 )
	   )  throw new IllegalArgumentException("2 got stDoWiM"+startDayOfWeekInMonth+"and stDoW "+startDayOfWeek);
	if (  endDayOfWeekInMonth == 0 || endDayOfWeek > Calendar.SATURDAY || endDayOfWeek < (-Calendar.SATURDAY)	
	      || endDayOfWeekInMonth > 31 || endDayOfWeekInMonth < -31
	      || ( (endDayOfWeekInMonth > 5 || endDayOfWeekInMonth < -5)  && endDayOfWeek > 0 )
	   ) throw new IllegalArgumentException("3 got endDoWiM"+endDayOfWeekInMonth+"and endDoW "+endDayOfWeek);
	
        this.startMonth = startMonth;
        this.startTime = startTime;
        this.endMonth = endMonth;
        this.endTime = endTime;
        this.dstSavings = dstSavings;
        this.startMode = calculatedstSavMode(startDayOfWeek, startDayOfWeekInMonth);
        this.endMode = calculatedstSavMode(endDayOfWeek, endDayOfWeekInMonth);
        this.startDayOfWeek = Math.abs(startDayOfWeek);
        this.endDayOfWeek = Math.abs(endDayOfWeek);
        this.startDay = startDayOfWeekInMonth *(startMode == DOW_LE_DOM_MODE ? -1 : 1);
        this.endDay = endDayOfWeekInMonth *(endMode == DOW_LE_DOM_MODE ? -1 : 1);
        if (dstSavings != 0) {
        	useDaylight = true;
        }
	      hasStartRule = true;
	      hasEndRule = true;

  }

  public SimpleTimeZone(int rawOffset, String ID, int startMonth, int startDay,
      int startDayOfWeek, int startTime, int startTimeMode, int endMonth,
      int endDay, int endDayOfWeek, int endTime, int endTimeMode, int dstSavings) {
    
    this(rawOffset,ID,startMonth,startDay,startDayOfWeek,startTime,endMonth,
        endDay,endDayOfWeek,endDay,dstSavings);
  }
  
  private int calculatedstSavMode(int dow, int dowim) {
   	if (dow == 0) return DOM_MODE;
   	if (dow > 0) return DOW_IN_MONTH_MODE;
   	if (dowim > 0) return DOW_GE_DOM_MODE;
        return DOW_LE_DOM_MODE;
  }

  public int getRawOffset(){
    	return rawOffset;
  }
  public void setRawOffset(int offsetMillis){
  	rawOffset = offsetMillis;
  }
  public boolean useDaylightTime() {
   	return useDaylight;
  }

  public void setStartYear(int startYear) {
  	this.startYear = startYear;
  }

  public int getDSTSavings() {
   	return dstSavings;
  }

  public void setDSTSavings(int dst) {
  	if (dst < 0) {
  		throw new IllegalArgumentException();
  	}
  	dstSavings = dst;
  }


  public void setEndRule(int month, int dayOfWInM, int dayOfW, int time){
     	if ( time < 0 || time >= 86400000  || month < Calendar.JANUARY || month > Calendar.DECEMBER	
             || dayOfW > Calendar.SATURDAY || dayOfW < Calendar.SUNDAY || dayOfWInM > 5 || dayOfWInM < -5) {
          throw new IllegalArgumentException();
        }
        if (dayOfWInM==0) {
        	useDaylight = false;
		hasEndRule = false;
        }
        else {
		hasEndRule = true;
	        useDaylight |= hasEndRule && hasStartRule;
                endMonth = month;
                endMode = DOW_IN_MONTH_MODE;
                endDayOfWeek = dayOfW;
                endDay = dayOfWInM;
		endTime = time;
        }
  }

  public void setEndRule(int month, int dayOfM, int time){
     	if ( time < 0 || time >= 86400000  || month < Calendar.JANUARY || month > Calendar.DECEMBER )	
           throw new IllegalArgumentException();
        if ( dayOfM < 1 || dayOfM > monthLength[month] )
           throw new IllegalArgumentException("end Day Of Month is wrong, got "+dayOfM);
	hasEndRule = true;
        useDaylight |= hasEndRule && hasStartRule;
        endMonth = month;
        endMode = DOM_MODE;
        endDay = dayOfM;
	endTime = time;

  }

  public void setEndRule(int month, int dayOfM, int dayOfW, int time, boolean after){
     	if ( time < 0 || time >= 86400000  || month < Calendar.JANUARY || month > Calendar.DECEMBER
     	     || dayOfW > Calendar.SATURDAY || dayOfW < Calendar.SUNDAY ){	
           throw new IllegalArgumentException();
        }
        if ( dayOfM < 1 || dayOfM > monthLength[month] ) {
           throw new IllegalArgumentException("end Day Of Month is wrong, got "+dayOfM);
        }
	hasEndRule = true;
        useDaylight |= hasEndRule && hasStartRule;
        endMonth = month;
        endMode = (after ? DOW_GE_DOM_MODE : DOW_LE_DOM_MODE);
        endDay = dayOfM;
        endDayOfWeek = dayOfW;
	endTime = time;
  }

  public void setStartRule(int month, int dayOfWInM, int dayOfW, int time){
     	if ( time < 0 || time >= 86400000  || month < Calendar.JANUARY || month > Calendar.DECEMBER	
             || dayOfW > Calendar.SATURDAY || dayOfW < Calendar.SUNDAY || dayOfWInM > 5 || dayOfWInM < -5) {
          throw new IllegalArgumentException();
        }
        if (dayOfWInM==0) {
        	useDaylight = false;
		hasStartRule = false;
        }
        else {
        	hasStartRule = true;
	        useDaylight |= hasEndRule && hasStartRule;
                startMonth = month;
                startMode = DOW_IN_MONTH_MODE;
                startDayOfWeek = dayOfW;
                startDay = dayOfWInM;
		startTime = time;
        }
  }

  public void setStartRule(int month, int dayOfM, int time){
     	if ( time < 0 || time >= 86400000  || month < Calendar.JANUARY || month > Calendar.DECEMBER )	
           throw new IllegalArgumentException();
        if ( dayOfM < 1 || dayOfM > monthLength[month] )
           throw new IllegalArgumentException("start Day Of Month is wrong, got "+dayOfM);
	hasStartRule = true;
        useDaylight |= hasEndRule && hasStartRule;
        startMonth = month;
        startMode = DOM_MODE;
        startDay = dayOfM;
	startTime = time;

  }

  public void setStartRule(int month, int dayOfM, int dayOfW, int time, boolean after){
     	if ( time < 0 || time >= 86400000  || month < Calendar.JANUARY || month > Calendar.DECEMBER
     	     || dayOfW > Calendar.SATURDAY || dayOfW < Calendar.SUNDAY ){	
           throw new IllegalArgumentException();
        }
        if ( dayOfM < 1 || dayOfM > monthLength[month] ) {
           throw new IllegalArgumentException("start Day Of Month is wrong, got "+dayOfM);
        }
	hasStartRule = true;
        useDaylight |= hasEndRule && hasStartRule;
        startMonth = month;
        startMode = (after ? DOW_GE_DOM_MODE : DOW_LE_DOM_MODE);
        startDay = dayOfM;
        startDayOfWeek = dayOfW;
	startTime = time;
  }

  public String toString(){
    StringBuffer buf = new StringBuffer(250);
    //make sure the buffer is large enough so we don't have to resize
    buf.append("java.util.SimpleTimeZone[id=");
    buf.append(getID());
    buf.append(",offset=");
    buf.append(rawOffset);
    buf.append(",dstSavings=");
    buf.append(dstSavings);
    buf.append(",useDaylight=");
    buf.append(useDaylight);
    buf.append(",startYear=");
    buf.append(startYear);
    buf.append(",startMode=");
    buf.append(startMode);
    buf.append(",startMonth=");
    buf.append(startMonth);
    buf.append(",startDay=");
    buf.append(startDay);
    buf.append(",startDayOfWeek=");
    buf.append(startDayOfWeek);
    buf.append(",startTime=");
    buf.append(startTime);
    buf.append(",endMode=");
    buf.append(endMode);
    buf.append(",endMonth=");
    buf.append(endMonth);
    buf.append(",endDay=");
    buf.append(endDay);
    buf.append(",endDayOfWeek=");
    buf.append(endDayOfWeek);
    buf.append(",endTime=");
    buf.append(endTime);
    buf.append(']');
    return new String(buf);
  }

  public Object clone() {
 	 	return super.clone();
  }

  public boolean equals(Object o) {
    if (!(o instanceof SimpleTimeZone)) {
     	return false;
    }
    SimpleTimeZone other = (SimpleTimeZone) o;
    return ((other.getID() == getID()) && hasSameRules(other));
  }

  public boolean hasSameRules(TimeZone other) {
  	if (!(other instanceof SimpleTimeZone)) return false;
  	SimpleTimeZone ostz = (SimpleTimeZone)other;
  	boolean b = (rawOffset == ostz.rawOffset) && (useDaylight == ostz.useDaylight);
  	if ( b && useDaylight ) return b;
  	b &= (startMonth == ostz.startMonth);
  	b &= (endMonth == ostz.endMonth);
  	b &= (startMode == ostz.startMode);
  	b &= (endMode == ostz.endMode);	
  	b &= (startTime == ostz.startTime);
  	b &= (startDay == ostz.startDay);
  	b &= (endTime == ostz.endTime);
  	b &= (endDay == ostz.endDay);
  	if (startMode != DOM_MODE) {	
  		b &= (startDayOfWeek == ostz.startDayOfWeek);
  	}
  	if (endMode != DOM_MODE) {	
  		b &= (endDayOfWeek == ostz.endDayOfWeek);
  	}
  	return b;
  }

  private boolean inDaylightTime (int era, int year, int month, int day, int dayOffWeek, int millis) {
    boolean b = (startYear <= year && era == GregorianCalendar.AD);
    b &= !(((month < startMonth || month > endMonth ) && endMonth > startMonth)
         ||((month < startMonth && month > endMonth ) && endMonth < startMonth));
    int sDay;
    if (month == startMonth  && b) { 	
    	switch (startMode) {
    	  case DOM_MODE:	
    	    b = day > startDay || ( day == startDay && millis >= startTime);
    	    break;
    	  case DOW_IN_MONTH_MODE:
    	   	sDay = calculateChangeDay(month, day, dayOffWeek, startDayOfWeek, startDay, year);
    	   	b = day > sDay || ( day == sDay && millis >= startTime);
    	   	break;
    	  case DOW_GE_DOM_MODE:
    	   	sDay = calculateChangeDay(month, day, dayOffWeek, startDayOfWeek, startDay, true);
    	   	b = day > sDay || ( day == sDay && millis >= startTime);
    	   	break;
    	  case DOW_LE_DOM_MODE:
    	   	sDay = calculateChangeDay(month, day, dayOffWeek, startDayOfWeek, startDay, false);
    	   	b = day > sDay || ( day == sDay && millis >= startTime);
    	   	break;
    	}
    }
    if (month == endMonth  && b) { 	
    	switch (endMode) {
        case DOM_MODE:	
           b = day < endDay || ( day == endDay && millis < endTime);
           break;
        case DOW_IN_MONTH_MODE:
         	sDay = calculateChangeDay(month, day, dayOffWeek, endDayOfWeek, endDay, year);
         	b = day < sDay || ( day == sDay && millis < endTime);
        	break;
        case DOW_GE_DOM_MODE:
          sDay = calculateChangeDay(month, day, dayOffWeek, endDayOfWeek, endDay, true);
          b = day < sDay || ( day == sDay && millis < endTime);
          break;
        case DOW_LE_DOM_MODE:
        	sDay = calculateChangeDay(month, day, dayOffWeek, endDayOfWeek, endDay, false);
        	b = day < sDay || ( day == sDay && millis < endTime);
        	break;
    	}
    }
    return b;
  }

  private int calculateChangeDay(int month, int day, int dayOffweek, int DOWinM, int chDOW, int year) {
  	if (chDOW > 0) {
  	// find first DOWinM
  		day +=  ((7 - dayOffweek + DOWinM)% 7);
  		day -= (day/7)*7;
  		day += 7 * (chDOW-1);	
  	}
  	else {
  	      	day += ((DOWinM - dayOffweek - 7)% 7);
  	      	int maxDay = monthLength[month]; 	
  	      	if (month == Calendar.FEBRUARY){
  	       		maxDay += (new GregorianCalendar().isLeapYear(year) ? 1 :0);
  	        }
  	        day += (((maxDay - day)/ 7)* 7);
  	        day += 7 * (chDOW+1);
  	        if (day < 0) day = 35;
  	}
  	return day;
  }

  private int calculateChangeDay(int month, int day, int dayOffweek, int DOWinM, int chDOW, boolean after) {
        int help = day - chDOW;  // help holds the difference in day between the changeDay and given day
        help = (dayOffweek - help + 55)% 7+1;    //help now represents the DOW of the chDay
        //we add 55 ( = 56 - 1 ) to make %7 work if help is positive or negative
        help =  after ? (7 - help + DOWinM)% 7 : -((DOWinM - help - 7)% 7);
        return (help+day);
  }
/**
** this method expects an array of at least length 5
** IT DOES NOT CHECK THE LENGTH ... !!!!!
*/
  private native void getfields( int [] array, long time);

  public boolean inDaylightTime(Date date) {
   	int [] array = new int[5];
   	getfields(array, (date.getTime()+rawOffset));
   	int era = GregorianCalendar.AD;
   	if ( array[0] < 1 ) {
   	 	era = GregorianCalendar.BC;
   	 	array[0] = -array[0] -1;
   	}
   	return inDaylightTime(era, array[0], array[1], array[2], array[4], array[3]);

  }

  public int getOffset(int era, int year, int month, int day, int dayOffWeek, int milliseconds) {
   	return rawOffset + (inDaylightTime(era, year, month, day, dayOffWeek, milliseconds) ? dstSavings :0);
  }

/**
** BAD IMPLEMENTATION
** couldn't find algorithm to calculate hashCode ...
** so I made one up using all private fields !
*/
  public int hashCode(){
  	int hash = endDay * endDayOfWeek * endMode * endMonth + endTime;
  	hash ^= (startDay * startDayOfWeek * startMode * startMonth + startTime );
  	hash ^= getID().hashCode() ^ rawOffset;
  	return hash;
  }
    	


}

