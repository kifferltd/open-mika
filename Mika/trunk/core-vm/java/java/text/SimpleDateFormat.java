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

import java.util.*;
import java.io.IOException;
import java.io.ObjectInputStream;

public class SimpleDateFormat extends DateFormat {

  static final FieldPosition TRASHPOSITION = new FieldPosition(0);

  private static final long serialVersionUID = 4774881970558875024L;
  private static final DateFormatSymbols DEFAULTSYMBOLS = new DateFormatSymbols();
  private static final String PATTERNCHARS = "GyMdkHmsSEDFwWahKzZ";
  private static final int[] FIELDMAP = { 0, 1, 2, 5, 11, 11, 12, 13, 14, 7, 6, 8, 3, 4, 9, 10, 10, 10 };

  //dictated by the serialized form ...
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
    in.defaultReadObject();
    if(serialVersionOnStream < 1){
      serialVersionOnStream = 1;
      defaultCenturyStart = new Date();
    }
  }

  private Date defaultCenturyStart = new Date();
  private DateFormatSymbols formatData;
  private String pattern;
  private int serialVersionOnStream = 1;

  public SimpleDateFormat(){
    this("dd.MM.yy  hh:mm:ss",new DateFormatSymbols());
  }

  public SimpleDateFormat(String pattern){
    this(pattern, new DateFormatSymbols());
  }

  public SimpleDateFormat(String pattern, Locale loc){
    this(pattern, new DateFormatSymbols(loc));
  }

  public SimpleDateFormat(String pattern, DateFormatSymbols dfs){
    if(pattern == null || dfs == null){
      throw new NullPointerException();
    }
    calendar = Calendar.getInstance();
    numberFormat = NumberFormat.getInstance();
    numberFormat.setParseIntegerOnly(true);
    formatData = dfs;
    this.pattern = pattern;
  }

  public void applyLocalizedPattern(String pattern){
    String local = formatData.getLocalPatternChars();
    this.pattern = toPattern(pattern, local, PATTERNCHARS);
  }

  public void applyPattern(String pattern){
    if (pattern == null){
      throw new NullPointerException();
    }
    this.pattern = pattern;
  }

  public Object clone() {
    SimpleDateFormat sdf = (SimpleDateFormat) super.clone();
    sdf.formatData = (DateFormatSymbols) formatData.clone();
    sdf.defaultCenturyStart = (Date) defaultCenturyStart.clone();
    return sdf;
  }

  public boolean equals(Object o){
    if(!(o instanceof SimpleDateFormat)){
      return false;
    }
    SimpleDateFormat sdf = (SimpleDateFormat)o;
    return this.pattern.equals(sdf.pattern);
  }

  public StringBuffer format(Date date, StringBuffer dest, FieldPosition pos){
    //TODO: take min en max digits into account !!!

    calendar.setTime(date);
    //System.out.println("formatting date "+date+" to --> "+pattern);
    int size = pattern.length();

    boolean formatting = true;
    for(int i = 0 ; i < size ; i++){
      char ch = pattern.charAt(i);
      //System.out.println("pattern char = '"+ch+"' at ("+i+") formatting "+formatting);
      if(ch == '\''){ // lets find quotes ...
        if((++i) < size && pattern.charAt(i) == ch){
          dest.append(ch);
        }
        else {
          i--; formatting = !formatting;
        }
      }
      else {
        if(formatting){
          int field = PATTERNCHARS.indexOf(ch);
          if(field != -1){
            i = writeField(ch, i, field, dest, pos);
            //System.out.println("writeField stopped at "+i);
            continue;
          }
        }
        //System.out.println("appending "+ch);
        dest.append(ch);
      }
    }
    //System.out.println(dest);
    return dest;
  }

  public DateFormatSymbols getDateFormatSymbols(){
    return formatData;
  }

  public Date get2DigitYearStart(){
    return defaultCenturyStart;
  }

  public int hashCode(){
    return pattern.hashCode() ^ 0xaaaaaaaa;
  }

  public Date parse(String str, ParsePosition pos) {
    //System.out.println("parsing '"+str+"' ("+pos.getIndex()+") using "+pattern);
    calendar.clear();
    TimeZone current = calendar.getTimeZone();
    int size = pattern.length();
    boolean formatting = true;
    int p = pos.getIndex();
    boolean checkDST = false;
    try {
      for(int i = 0 ; i < size ; i++,p++){
        char ch = pattern.charAt(i);
        //System.out.println("pattern char = '"+ch+"' at ("+i+") formatting "+formatting);
        if(ch == '\''){ // lets find quotes ...
          if((++i) < size && pattern.charAt(i) == ch){
            if(str.charAt(p) != ch){
              pos.setIndex(p);
              pos.setErrorIndex(p);
              //System.out.println("PARSE ERROR -- MISSING QUOTE");
              return null;
            }
          }
          else {
            i--; formatting = !formatting; p--;
          }
        }
        else {
          if(formatting){
            int field = PATTERNCHARS.indexOf(ch);
            if(field != -1){
              pos.setIndex(p);
              if (field == 17 || field == 18) {
                // special case: z/Z
                int nr = 1;
                for (++i; i < size ; i++){
                  if(pattern.charAt(i) == ch){ nr++; }
                  else{ break; }
                }
                i--;

                int res = -1;
                if (str.charAt(p) == '+' || str.charAt(p) == '-') {
                  String syntheticTZ  = "GMT" + str.substring(p, p + 3) + ":" + str.substring(p + 3);
                  //System.out.println("using synthetic timezone " + syntheticTZ);
                  calendar.setTimeZone(TimeZone.getTimeZone(syntheticTZ));
                  p += 5;
                  res = 0;
                }
                else {
                  res = formatData.parseTimeZoneString(calendar, str, pos);
                  p = pos.getIndex()-1;
                }

                if(res == -1){
                  //System.out.println("time zone not found");
                  return null;
                }
                checkDST = (res == 1);
                continue;
              }
              else {
                i = readField(ch, i, field, str,pos);
                //System.out.println("readField stopped at "+i);
                if(i == -1){
                  //System.out.println("PARSE ERROR -- READFIELD FAILED");
                  return null;
                }
                p = pos.getIndex()-1;
                //System.out.println("readField stopped at "+i+"("+pattern.length()+") new position is "+(p+1)+" "+str.substring(0,p+1) + "^" + str.substring(p+1));
                continue;
              }
            }
          }
          if(str.charAt(p) != ch){
            pos.setIndex(p);
            pos.setErrorIndex(p);
            //System.out.println("PARSE ERROR -- WRONG CHAR ENCOUNTERED");
            return null;
          }
        }
      }
      pos.setIndex(p);
    }
    catch (IndexOutOfBoundsException ioobe) {
      pos.setErrorIndex(p);
      //System.out.println("PARSE ERROR -- premature end of input at position " + p);
            return null;
    }
    catch (NumberFormatException nfe) {
      pos.setErrorIndex(p);
      //System.out.println("PARSE ERROR -- number format error at position " + p);
      return null;
    }
    //System.out.println("parsed '"+str+"' to "+calendar.getTime());
    Date time = calendar.getTime();
    if(checkDST && calendar.getTimeZone().inDaylightTime(time)){
      time.setTime(time.getTime()+((SimpleTimeZone)calendar.getTimeZone()).getDSTSavings());
    }
    calendar.setTimeZone(current);
    return time;
  }

  public void setDateFormatSymbols(DateFormatSymbols dfs){
    formatData = dfs;
  }

  public void set2DigitYearStart(Date date){
    if(date == null){
      throw new NullPointerException();
    }
    defaultCenturyStart = date;
  }

  public String toLocalizedPattern(){
    return toPattern(pattern, PATTERNCHARS, formatData.getLocalPatternChars());
  }

  public String toPattern(){
    return pattern;
  }

  private String toPattern(String pattern, String from, String to){
    int size = pattern.length();
    char[] chars = new char[size];
    pattern.getChars(0, size, chars, 0);
    for(int i = 0 ; i < size ; i++){
      char ch = chars[i];
      if(ch == '\''){ // lets find quotes ...
        int q = pattern.indexOf(ch, i+1);
        if(i+1 == q){ //we found two consecutive quotes
          i++;
        }
        else{//we have a quoted string ...
          while(q != -1 && (++q) < size && ch == pattern.charAt(q)){
            q = pattern.indexOf(ch, q+1);
          }
          i = (q == -1 ? size : q-1);
        }
      }
      else {
        int p = from.indexOf(ch);
        if(p != -1){
          chars[i] = to.charAt(p);
        }
      }
    }
    return new String(chars,0,size);
  }

  private int readField(char ch, int idx, int field, String dest, ParsePosition pos){
    //System.out.println("reading field "+ch+" ("+field+")");
    int nr = 1;
    int i = idx+1;
    int pattern_length = pattern.length();
    int res;

    for (; i < pattern_length ; i++){
      if(pattern.charAt(i) == ch){
        nr++;
      }
      else{
        break;
      }
    }
    idx = i-1;
    switch(ch){
      case 'M': //MONTH
        if(nr < 3){
          numberFormat.minimumIntegerDigits = nr;
          numberFormat.maximumIntegerDigits = 2;
          Number num1 = numberFormat.parse(dest, pos);
          if(num1 != null){
            calendar.set(Calendar.MONTH,num1.intValue()-1);
          }
          else {
            return -1;
          }
        }
        else {
          res = arrayLookup(formatData.getMonths(), dest, pos,field, idx, true);
          if (res >= 0) {
            return res;
          }
          return arrayLookup(formatData.getShortMonths(), dest, pos,field, idx, true);
        }
        break;
      case 'y':
        numberFormat.minimumIntegerDigits = nr;
        numberFormat.maximumIntegerDigits = nr == 2 ? 2 : (nr > 4 ? nr : 4);
        Number num2 = numberFormat.parse(dest, pos);
        if(num2 != null){
          int year = num2.intValue();
          if(nr < 3){
            GregorianCalendar gc = new GregorianCalendar(0,0,0);
            gc.setTime(defaultCenturyStart);
            int cent = gc.get(Calendar.YEAR);
            year += cent - (cent % 100);
          }
          calendar.set(Calendar.YEAR, year);
        }
        else {
          return -1;
        }
        break;
      case 'd': //2
      case 'H':
      case 'K':
      case 'w':
      case 'W':
      case 'm':
      case 's':
        numberFormat.minimumIntegerDigits = nr;
        numberFormat.maximumIntegerDigits = nr > 2 ? nr : 2;
        Number num3 =  numberFormat.parse(dest, pos);
        if(num3 != null){
          //System.out.println("setting "+FIELDMAP[field]+" to "+num3.intValue());
          calendar.set(FIELDMAP[field],num3.intValue());
        }
        else {
          return -1;
        }
        break;
      case 'D': //3
        numberFormat.minimumIntegerDigits = nr;
        numberFormat.maximumIntegerDigits = nr > 3 ? nr : 3;
        Number num4 =  numberFormat.parse(dest, pos);
        if(num4 != null){
          //System.out.println("setting "+FIELDMAP[field]+" to "+num4.intValue());
          calendar.set(FIELDMAP[field],num4.intValue());
        }
        else {
          return -1;
        }
        break;
      case 'S': //always 3
        numberFormat.minimumIntegerDigits = 3;
        numberFormat.maximumIntegerDigits = 3;
        Number num5 =  numberFormat.parse(dest, pos);
        if(num5 != null){
          //System.out.println("setting "+FIELDMAP[field]+" to "+num5.intValue());
          calendar.set(FIELDMAP[field],num5.intValue());
        }
        else {
          return -1;
        }
        break;
      case 'F': //1
        numberFormat.minimumIntegerDigits = nr;
        numberFormat.maximumIntegerDigits = nr;
        Number num6 =  numberFormat.parse(dest, pos);
        if(num6 != null){
          //System.out.println("setting "+FIELDMAP[field]+" to "+num6.intValue());
          calendar.set(FIELDMAP[field],num6.intValue());
        }
        else {
          return -1;
        }
        break;
      case 'a':
        return arrayLookup(formatData.getAmPmStrings(), dest, pos, field, idx, true);
      case 'E':
          res = arrayLookup(formatData.getWeekdays(), dest, pos, field, idx, false);
          if(res != -1){
            return res;
          }
          return arrayLookup(formatData.getShortWeekdays(), dest, pos, field, idx, true);
      case 'h':
        numberFormat.minimumIntegerDigits = nr;
        numberFormat.maximumIntegerDigits = nr > 2 ? nr : 2;
        Number num7 =  numberFormat.parse(dest, pos);
        if(num7 != null){
          calendar.set(FIELDMAP[field],num7.intValue()%12);
        }
        else {
          return -1;
        }
        break;
      case 'k':
        Number num8 =  numberFormat.parse(dest, pos);
        if(num8 != null){
          calendar.set(FIELDMAP[field],num8.intValue()%24);
        }
        else {
          return -1;
        }
        break;
      case 'G': //ERA
        return arrayLookup(formatData.getEras(), dest, pos, field, idx, true);
      default:
          //System.out.println("got a bad character");
          return -1;
    }
    return idx;
  }

  private int arrayLookup(String[] strings, String source, ParsePosition pos, int field, int idx, boolean set){
    int start = pos.getIndex();
    for(int i = 0 ; i < strings.length ; i++){
      int len = strings[i].length();
      //System.out.println("checking '"+source+"'("+start+") for '"+strings[i]);

      // [CG 20081204] apparently we need to be case-insensitive here
      // WAS: if(len > 0 && source.regionMatches(start, strings[i], 0, len)){
      if(len > 0 && source.length() >= start + len && strings[i].length() >= len && source.substring(start, start + len).equalsIgnoreCase(strings[i].substring(0, len))) {
        calendar.set(FIELDMAP[field], i);
        pos.setIndex(start+len);
        return idx;
      }
    }
    if(set){
      pos.setErrorIndex(start);
    }
    return -1;
  }

  private int writeField(char ch, int idx, int field, StringBuffer dest, FieldPosition pos){
    //System.out.println("Writing field "+ch+" ("+field+")");
    int nr = 1;
    int start = dest.length();
    int i = idx+1;
    int pattern_length = pattern.length();
    for (; i < pattern_length ; i++){
      if(pattern.charAt(i) == ch){
        nr++;
      }
      else{
        break;
      }
    }
    idx = i-1;
    switch(ch){
      case 'M': //MONTH
        int m = calendar.get(Calendar.MONTH);
        if(nr < 3){
          numberFormat.setMinimumIntegerDigits(nr);
          numberFormat.format(m+1, dest, TRASHPOSITION);
        }
        else {
          String[] ms = (nr == 3 ? formatData.getShortMonths() : formatData.getMonths());
          dest.append(ms[m]);
        }
        break;
      case 'y':
        numberFormat.setMinimumIntegerDigits(nr);
        numberFormat.format(calendar.get(Calendar.YEAR), dest, TRASHPOSITION);
        if(nr < 4){
          dest.delete(start,dest.length()-2);
        }
        break;
      case 'd': //2
      case 'H':
      case 'K':
      case 'w':
      case 'W':
      case 'm':
      case 's':
        numberFormat.setMinimumIntegerDigits(2 < nr ? 2 : nr);
        numberFormat.format(calendar.get(FIELDMAP[field]), dest, TRASHPOSITION);
        break;
      case 'D': //3
        numberFormat.setMinimumIntegerDigits(3 < nr ? 3 : nr);
        numberFormat.format(calendar.get(FIELDMAP[field]), dest, TRASHPOSITION);
        break;
      case 'F': //1
        numberFormat.setMinimumIntegerDigits(1);
        numberFormat.format(calendar.get(FIELDMAP[field]), dest, TRASHPOSITION);
        break;
      case 'a':
        dest.append(formatData.getAmPmStrings()[calendar.get(Calendar.AM_PM)]);
        break;
      case 'E':
          String[] ms = (nr <= 3 ? formatData.getShortWeekdays() : formatData.getWeekdays());
          dest.append(ms[calendar.get(Calendar.DAY_OF_WEEK)]);
        break;
      case 'z':
        dest.append(formatData.getTimeZoneString(calendar, nr > 3));
        break;
      case 'S':
        numberFormat.setMinimumIntegerDigits(3);
        numberFormat.format(calendar.get(Calendar.MILLISECOND), dest, TRASHPOSITION);
        break;
      case 'h':
        numberFormat.setMinimumIntegerDigits(nr);
        int h = calendar.get(FIELDMAP[field]);
        numberFormat.format((h == 0 ? 12 : h) , dest, TRASHPOSITION);
        break;
      case 'k':
        numberFormat.setMinimumIntegerDigits(nr);
        int k = calendar.get(FIELDMAP[field]);
        numberFormat.format((k == 0 ? 24 : k) , dest, TRASHPOSITION);
        break;
      case 'G': //ERA
        dest.append(formatData.getEras()[calendar.get(Calendar.ERA)]);
        break;
    }
    if(field == pos.getField()){
      pos.setBeginIndex(start);
      pos.setEndIndex(dest.length());
    }
    return idx;
  }
}
