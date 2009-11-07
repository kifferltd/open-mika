/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2008 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
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
    ResourceBundle resource = ResourceBundle.getBundle("wonka.resource.DateFormatSymbolBundle",loc);

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

  int parseTimeZoneString(Calendar cal, String dest, ParsePosition pos) {
    int start = pos.getIndex();
    int matchlen = 0;
    String candidate;
    int candlen;
    TimeZone tz;
    int result = -1;

    //System.out.println("parsing TimeZone from " + dest.substring(start));
    for (int i = 0 ; i < zoneStrings.length ; i++) {
      // We try the longest strings first, so in the order: 1 3 2 4 0
      for (int j = 0; j < 2; ++j) {
        for (int k = 0; k < 2; ++k) {
          candidate = zoneStrings[i][j + 2 * k + 1];
          //System.out.println("candidate[" + i + "][" + (j + 2 * k + 1) + "]: " + candidate);
          candlen = candidate.length();
          if (candlen > matchlen && dest.regionMatches(start,candidate,0,candlen)) {
            matchlen = candlen;
            tz = TimeZone.getTimeZone(zoneStrings[i][0]);
            //System.out.println("Case 1: matched '" + candidate + "', canonical name = '" + zoneStrings[i][0] + ", tz = " + tz);
            cal.setTimeZone(tz);
            result = 1;
          }
        }
      }

      candidate = zoneStrings[i][0];
      candlen = candidate.length();
      if (candlen > matchlen && dest.regionMatches(start,candidate,0,candlen)) {
        matchlen = candlen;
        tz = TimeZone.getTimeZone(candidate);
        //System.out.println("Case 3: matched '" + candidate + "', canonical name = '" + zoneStrings[i][0] + ", tz = " + tz);
        cal.setTimeZone(tz);
        result = 0;
      }
    }

    //System.out.println("recognised " + dest.substring(start, start + matchlen) + " in " + dest);
    if (dest.substring(start, start + matchlen).equals("GMT") && dest.length() >= start + 9) {
      char sign = dest.charAt(start + 3);
      if (sign == '+' || sign == '-') {
        matchlen = 9;
        tz = TimeZone.getTimeZone(dest.substring(start, start + 9));
        cal.setTimeZone(tz);
        result = 0;
      }
    }
    pos.setIndex(start + matchlen);
    //System.out.println(" => timezone = " + cal.getTimeZone());

    return result;
  }
}

