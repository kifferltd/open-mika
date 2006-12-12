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
** $Id: DateFormatSymbols.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

import java.util.Locale;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.io.Serializable;

public class DateFormatSymbols implements Cloneable,Serializable {

  private static final long serialVersionUID = -5987973545549424702L;
/*
  private static final String[] AMPMS = {"AM","PM"};
  private static final String[] ERAS = {"BC","AD"};
  private static final String LOCALPATTERNCHARS = "GyMdkHmsSEDFwWahKz";
  private static final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August",
                                          "September", "October", "November", "December", ""};
  private static final String[] SHORTMONTHS = { "Jan",  "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                                                "Aug", "Sep", "Oct", "Nov", "Dec", ""};
  private static final String[] SHORTWEEKDAYS = {"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
  private static final String[] WEEKDAYS = {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
  private static final String[][] ZONESTRINGS = {{"GMT", "Greenwich Mean Time", "GMT", "Greenwich Mean Time", "GMT"}};
*/


  //dictated by serial form ...
  private String[] ampms;
  private String[] eras;
  private String localPatternChars;
  private String[] months;
  private String[] shortMonths;
  private String[] shortWeekdays;
  private String[] weekdays;
  private String[][] zoneStrings;

  public DateFormatSymbols(){
    this(Locale.getDefault());
  }

  public DateFormatSymbols(Locale loc){
    ResourceBundle resource = ResourceBundle.getBundle("com.acunia.resource.DateFormatSymbolBundle",loc);

    ampms = (String[])resource.getObject("ampms");
    eras = (String[])resource.getObject("eras");
    localPatternChars = (String)resource.getObject("pattern");
    months = (String[])resource.getObject("months");
    shortMonths = (String[])resource.getObject("shortMonths");
    shortWeekdays = (String[])resource.getObject("shortDays");
    weekdays = (String[])resource.getObject("days");
    zoneStrings = (String[][])resource.getObject("zones");
  }

  public Object clone(){
    try {
      DateFormatSymbols clone = (DateFormatSymbols) super.clone();
      clone.ampms = (String[]) this.ampms.clone();
      clone.eras = (String[]) this.eras.clone();
      clone.months = (String[]) this.months.clone();
      clone.shortMonths = (String[]) this.shortMonths.clone();
      clone.shortWeekdays = (String[]) this.shortWeekdays.clone();
      clone.weekdays = (String[]) this.weekdays.clone();
      clone.zoneStrings = (String[][]) this.zoneStrings.clone();
      return clone;
    }
    catch(CloneNotSupportedException cnse){
      return null;
    }
  }

  public boolean equals(Object o){
    if(!(o instanceof DateFormatSymbols)){
      return false;
    }
    DateFormatSymbols dfs = (DateFormatSymbols) o;
    return Arrays.equals(this.ampms , dfs.ampms)
        && Arrays.equals(this.eras , dfs.eras)
        && Arrays.equals(this.months , dfs.months)
        && Arrays.equals(this.shortMonths , dfs.shortMonths)
        && Arrays.equals(this.shortWeekdays , dfs.shortWeekdays)
        && Arrays.equals(this.weekdays , dfs.weekdays)
        && Arrays.equals(this.zoneStrings , dfs.zoneStrings)
        && this.localPatternChars.equals(dfs.localPatternChars);
  }

  /**
  ** the hashCode should be calculated based on all properties to determine
  ** equality @see equals. However since this involves a lot of arrays we simplify the
  ** algorithm ...
  */
  public int hashCode(){
    return localPatternChars.hashCode() ^ zoneStrings.length;
  }

  public String[] getAmPmStrings(){
    return ampms;
  }

  public String[] getEras(){
    return eras;
  }

  public String getLocalPatternChars(){
    return localPatternChars;
  }

  public String[] getMonths(){
    return months;
  }

  public String[] getShortMonths(){
    return shortMonths;
  }

  public String[] getShortWeekdays(){
    return shortWeekdays;
  }

  public String[] getWeekdays(){
    return weekdays;
  }

  public String[][] getZoneStrings(){
    return zoneStrings;
  }


  public void setAmPmStrings(String[] val){
    ampms = val;
  }

  public void setEras(String[] val){
    eras = val;
  }

  public void setLocalPatternChars(String val){
    localPatternChars = val;
  }

  public void setMonths(String[] val){
    months = val;
  }

  public void setShortMonths(String[] val){
    shortMonths = val;
  }

  public void setShortWeekdays(String[] val){
    shortWeekdays = val;
  }

  public void setWeekdays(String[] val){
    weekdays = val;
  }

  public void setZoneStrings(String[][] val){
    zoneStrings = val;
  }

  String getTimeZoneString(Calendar cal,boolean longString){
    TimeZone zone = cal.getTimeZone();
    String ID = zone.getID();
    for(int i = 0 ; i < zoneStrings.length ; i++){
      if (ID.equals(zoneStrings[i][0])){
        int val = 1;
        val += longString ? 0 : 1;
        val += zone.inDaylightTime(cal.getTime()) ? 2 : 0;
        return zoneStrings[i][val];
      }
    }
    return zone.getDisplayName(zone.inDaylightTime(cal.getTime()),(longString ? TimeZone.LONG : TimeZone.SHORT));
  }

  int parseTimeZoneString(Calendar cal, boolean longString, String dest, ParsePosition pos){
    int val = 1;
    val += longString ? 0 : 1;
    int val2 = val + 2;
    int start = pos.getIndex();
    for(int i = 0 ; i < zoneStrings.length ; i++){
      if(dest.regionMatches(start,zoneStrings[i][val],0,zoneStrings[i][val].length())){
        cal.setTimeZone(TimeZone.getTimeZone(zoneStrings[i][0]));
        pos.setIndex(start+zoneStrings[i][val].length());
        return 1;
      }
      if(dest.regionMatches(start,zoneStrings[i][val2],0,zoneStrings[i][val2].length())){
        cal.setTimeZone(TimeZone.getTimeZone(zoneStrings[i][0]));
        pos.setIndex(start+zoneStrings[i][val2].length());
        return 0;
      }
      if(dest.regionMatches(start,zoneStrings[i][0],0,zoneStrings[i][0].length())){
        cal.setTimeZone(TimeZone.getTimeZone(zoneStrings[i][0]));
        pos.setIndex(start+zoneStrings[i][0].length());
        return 0;
      }
    }
    return -1;
  }
}

