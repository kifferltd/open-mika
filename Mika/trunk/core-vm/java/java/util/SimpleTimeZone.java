/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2008, 2009 by Chris Gray, /k/ Embedded Java         *
* Solutions. All rights reserved.                                         *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
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
  private int endTimeMode;
  private int rawOffset;
  private int serialVersionOnStream=2;
  private int startDay;
  private int startDayOfWeek;
  private int startMode;
  private int startMonth;
  private int startTime;
  private int startTimeMode;
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
        endDay,endDayOfWeek,endTime,dstSavings);
    if (startTimeMode < 0 || startTimeMode > 2) {
      throw new IllegalArgumentException("bad startTimeMode");
    }
    if (endTimeMode < 0 || endTimeMode > 2) {
      throw new IllegalArgumentException("bad endTimeMode");
    }
    this.startTimeMode = startTimeMode;
    this.endTimeMode = endTimeMode;
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
  	if ( b && !useDaylight ) return b;
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
  	       		maxDay += (new GregorianCalendar(this).isLeapYear(year) ? 1 :0);
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

  public boolean inDaylightTime(Date date) {
    GregorianCalendar gc = new GregorianCalendar(this);
    gc.setTime(date);
    return gc.inDaylightTime();
  }

  public int hashCode(){
  	int hash = endDay * endDayOfWeek * endMode * endMonth + endTime;
  	hash ^= (startDay * startDayOfWeek * startMode * startMonth + startTime );
  	hash ^= getID().hashCode() ^ rawOffset;
  	return hash;
  }
    	
  /**
   * Gets the offset from GMT of this SimpleTimeZone for the specified date
   * and time. The offset includes daylight savings time if the specified date
   * and time are within the daylight savings time period.
   * The logic is shamelessly cribbed from ICU4J (icu-project.org). Therefore
   * here is their copyright and permission notice:
   * 
   * <p>COPYRIGHT AND PERMISSION NOTICE
   * <p>Copyright (c) 1995-2008 International Business Machines Corporation and others 
   * <p>All rights reserved. 
   * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, provided that the above copyright notice(s) and this permission notice appear in all copies of the Software and that both the above copyright notice(s) and this permission notice appear in supporting documentation. 
   * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF THIRD PARTY RIGHTS. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR HOLDERS INCLUDED IN THIS NOTICE BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL INDIRECT OR CONSEQUENTIAL DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. 
   * <p>Except as contained in this notice, the name of a copyright holder shall not be used in advertising or otherwise to promote the sale, use or other dealings in this Software without prior written authorization of the copyright holder.
   * 
   * @param era
   *            the GregorianCalendar era, either GregorianCalendar.BC or
   *            GregorianCalendar.AD
   * @param year
   *            the year
   * @param month
   *            the Calendar month
   * @param day
   *            the day of the month
   * @param dayOfWeek
   *            the Calendar day of the week
   * @param time
   *            the time of day in milliseconds
   * @return the offset from GMT in milliseconds
   */
  public int getOffset(int era, int year, int month, int day, int dayOfWeek, int time) {
    if (era != GregorianCalendar.BC && era != GregorianCalendar.AD) {
      throw new IllegalArgumentException("bad era: " + era);
    }
    if (month < Calendar.JANUARY || month > Calendar.DECEMBER) {
      throw new IllegalArgumentException("bad month: " + month);
    }
    if (dayOfWeek < Calendar.SUNDAY || dayOfWeek > Calendar.SATURDAY) {
      throw new IllegalArgumentException("bad dayOfWeek: " + dayOfWeek);
    }
    if (time < 0 || time >= 24 * 3600000) {
      throw new IllegalArgumentException("bad offset: " + time);
    }
//System.out.println("year = " + year + " month = " + month + " day = " + day + " dOW = " + dayOfWeek + " time = " + time);
    boolean leap = (year % 4 == 0);
    if (leap && year > 1582) {
      leap = (year % 100 != 0 || year % 400 == 0);
    }
//System.out.println(year + " is" + (leap ? " " : " not ") + "a leap year");

    if (month != Calendar.FEBRUARY || day != 29 || !leap) {
      if (day <= 0 || day > GregorianCalendar.DaysInMonth[month]) {
        throw new IllegalArgumentException("bad day: " + day);
      }
    }

    int monlen = monthLength[month];
    if (leap && month == Calendar.FEBRUARY) {
      ++monlen;
    }
    int prevmonlen = monthLength[(month + 11) % 12];
    if (leap && month == Calendar.MARCH) {
      ++prevmonlen;
    }
//System.out.println("month " + month + " length = " + monlen + ", previous = " + prevmonlen);
    int offset = rawOffset;

    if (useDaylight && year >= startYear && era == GregorianCalendar.AD) {
      boolean downUnder = startMonth > endMonth;
//System.out.println((downUnder ? "nor" : "sou") + "thern hemisphere");

      while (time > 24 * 3600000) {
        time -= 24 * 3600000;
        ++day;
        dayOfWeek = (dayOfWeek % 7) + 1;
        if (day > monlen) {
          ++month;
          day = 1;
        }
      }
      while (time < 0) {
        time += 24 * 3600000;
        --day;
        dayOfWeek = ((dayOfWeek + 5) % 7) + 1;
        if (day < 1) {
          --month;
          day = prevmonlen;
        }
      }
      // At this point 'month' could  be out of range but this does no harm.
//System.out.println("corrected year = " + year + " month = " + month + " day = " + day + " dOW = " + dayOfWeek + " time = " + time);

//System.out.println("startMonth = " + startMonth + " startDay = " + startDay);
      int startCompare = 0;
      if (month < startMonth) {
        startCompare = -1;
      }
      else if (month > startMonth) {
        startCompare = 1;
      }
      else {
        int startDayOfMonth = 0;
        if (startDay > monlen) {
          startDay = monlen;
        }

        switch (startMode) {
        case DOM_MODE:
          startDayOfMonth = startDay;
          break;
        case DOW_IN_MONTH_MODE:
          if (startDay > 0)
            startDayOfMonth = 1 + (startDay - 1) * 7 +
                (7 + startDayOfWeek - (dayOfWeek - day + 1)) % 7;
          else {
            startDayOfMonth = monlen + (startDay + 1) * 7 -
                (7 + (dayOfWeek + monlen - day) - startDayOfWeek) % 7;
          }
          break;
        case DOW_GE_DOM_MODE:
          startDayOfMonth = startDay +
            (49 + startDayOfWeek - startDay - dayOfWeek + day) % 7;
          break;
        case DOW_LE_DOM_MODE:
          startDayOfMonth = startDay -
            (49 - startDayOfWeek + startDay + dayOfWeek - day) % 7;
          break;
        }

//System.out.println("startDayOfMonth = " + startDayOfMonth + " startTime = " + startTime);
        int startWallTime = startTime;
        if (startTimeMode == UTC_TIME) {
          startWallTime = startTime + rawOffset;
        }

        if (day < startDayOfMonth) {
          startCompare = -1;
        }
        else if (day > startDayOfMonth) {
          startCompare = 1;
        }
        else {
          if (time < startWallTime){
            startCompare = -1;
          }
          else if (time > startWallTime){
            startCompare = 1;
          }
        }
      }
//System.out.println("startCompare = " + startCompare);

//System.out.println("endMonth = " + endMonth + " endDay = " + endDay);
      int endCompare = 0;
      if (downUnder != (startCompare >= 0)) {
        if (month < endMonth) {
          endCompare = -1;
        }
        else if (month > endMonth) {
          endCompare = 1;
        }
        else {
          int endDayOfMonth = 0;
          if (endDay > monlen) {
            endDay = monlen;
          }

          switch (endMode) {
          case DOM_MODE:
            endDayOfMonth = endDay;
            break;
          case DOW_IN_MONTH_MODE:
            if (endDay > 0)
              endDayOfMonth = 1 + (endDay - 1) * 7 +
                  (7 + endDayOfWeek - (dayOfWeek - day + 1)) % 7;
            else {
              endDayOfMonth = monlen + (endDay + 1) * 7 -
                  (7 + (dayOfWeek + monlen - day) - endDayOfWeek) % 7;
            }
            break;
          case DOW_GE_DOM_MODE:
            endDayOfMonth = endDay +
              (49 + endDayOfWeek - endDay - dayOfWeek + day) % 7;
            break;
          case DOW_LE_DOM_MODE:
            endDayOfMonth = endDay -
              (49 - endDayOfWeek + endDay + dayOfWeek - day) % 7;
            break;
          }

//System.out.println("endDayOfMonth = " + endDayOfMonth + " endTime = " + endTime);
        int endWallTime = endTime;
        if (endTimeMode == UTC_TIME) {
          endWallTime = endTime + rawOffset + dstSavings;
        }
        else if (endTimeMode == STANDARD_TIME) {
          endWallTime = endTime + dstSavings;
        }

          if (day < endDayOfMonth) {
            endCompare = -1;
          }
          else if (day > endDayOfMonth) {
            endCompare = 1;
          }
          else {
            time += dstSavings;
            // Following ICU4J we should re-normalize here in case adding
            // DST has pushed us into tomorrow. But that seems pretty far-
            // fetched so we don't bother.
            if (time < endWallTime){
              endCompare = -1;
            }
            else if (time > endWallTime){
              endCompare = 1;
            }
          }
        }
      }
//System.out.println("endCompare = " + endCompare);

      if ((!downUnder && (startCompare >= 0 && endCompare < 0)) ||
            (downUnder && (startCompare >= 0 || endCompare < 0))) {
//System.out.println("applying DST");
        offset += dstSavings;
      }
    }

    return offset;
  }

}

