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



package java.util;

import java.io.Serializable;

import com.acunia.resource.TimeZoneResourceBundle;

abstract public class TimeZone implements Serializable, Cloneable {

  private static final long serialVersionUID = 3581463369166924961L;

  public static final int SHORT=0;
  public static final int LONG=1;

  private static TimeZone defaultTZ = new SimpleTimeZone(0, "GMT");
  private static TimeZoneResourceBundle tzResBundle;
  private String ID; //name specified by serialization ...

  /**
   ** Try to load the TimeZoneResourceBundle "com.acunia.resource.TimeZoneResourceBundle".
   ** If successful, the TimeZoneResourceBundle returned is also stored in tzResBundle.
   ** If unsuccessful, returns null.
   */
  private synchronized static TimeZoneResourceBundle getTimeZoneResourceBundle() {
    if (tzResBundle == null) {
      try {
       tzResBundle = (TimeZoneResourceBundle) ResourceBundle.getBundle("com.acunia.resource.TimeZoneResourceBundle");
       }
       catch (Exception ohoh){
      }
    }

    return tzResBundle;
  }

  /**
   ** Set the default TimeZone.  If tz is null then we try to get 
   ** "com.acunia.resource.TimeZoneResourceBundle", and if that fails 
   ** we default to GMT.
   ** @param	tz	The new default TimeZone, or null to reset the default.
   */
  public static synchronized void setDefault(TimeZone tz){
    defaultTZ = tz;
    if (tz == null) {
      if (getTimeZoneResourceBundle() != null) {
        defaultTZ = tzResBundle.getTimeZone(GetSystemProperty.DEFAULT_TIMEZONE);
      }
      else {
       defaultTZ = new SimpleTimeZone(0, "GMT");
      }
    }
  }

  /**
   ** Set this TimeZone's ID.
   ** @param	newID	The new ID.
   */
  public void setID(String newID) {
     if (newID == null) {
       throw new NullPointerException();
     }
     ID = newID;
  }

  public String getID() {
     return ID;
  }

  public boolean hasSameRules(TimeZone tz){
    return ((this.getRawOffset() == tz.getRawOffset()) && (this.useDaylightTime() == tz.useDaylightTime()) );
  }

  public String toString() {
     return super.toString()+"ID="+ID;

  }


  public final String getDisplayName(){
    return getDisplayName(false, LONG, Locale.getDefault());
  }

  public final String getDisplayName(Locale loc){
    return getDisplayName(false, LONG, loc);
  }

  public final String getDisplayName(boolean daylight, int style){
    return getDisplayName(daylight, style, Locale.getDefault());
  }
  public String getDisplayName(boolean daylight, int style, Locale loc){
       // the Locale is used to load the correct ResourceBundle ...
       // ID is the key within the ResourceBundle which contains arrays of Strings
       // daylight, style will be used to determine which String is needed ...
       // 0 ==> short name, 1 ==> long name, 2 ==> short name dst, 3 ==> long name dst
       String [] sa=null;
       int i =  1 + style + (daylight ? 2 : 0);
       String s=null;
       try {
            ResourceBundle tzNames = ResourceBundle.getBundle("com.acunia.resource.TimeZoneDisplayNameResourceBundle" , loc);
            sa = tzNames.getStringArray(ID);
            s = sa[i];
       } catch (Exception e) {}
       // if something went wrong or no value has been found make a default value
       if (s == null) {
           StringBuffer buf = new StringBuffer("GMT");
           int offset = getRawOffset();
           if ( offset >= 0 ) {
             buf.append('+');
           }
           else {
             buf.append('-');
                offset *= -1;
           }
           int h = offset / 3600000;
           buf.append((char)('0'+h/10));
           buf.append((char)('0'+h%10));
           buf.append(':');
           h = (offset % 3600000)/60000;
           buf.append((char)('0'+h/10));
           buf.append((char)('0'+h%10));
           s = new String(buf);
       }
       return s;  
  }

  public int getDSTSavings() {
    return useDaylightTime() ? 3600000 : 0;
  }
  
  public int getOffset(long date) {
    return (this.inDaylightTime(new Date(date)) ?
      getDSTSavings() : 0) + getRawOffset();    
  }

  public Object clone() {
    try {
      return super.clone();
    }
    catch(CloneNotSupportedException cnse){
      return null;
    }
  }

  //Declaration of abstract Methods ...

  public abstract int  getOffset(int era, int year, int month, int day, int dayOffWeek, int milliseconds);
  public abstract int  getRawOffset();
  public abstract void setRawOffset(int offsetMillis);
  public abstract boolean inDaylightTime(Date date);
  public abstract boolean useDaylightTime();


  //Implementation of static Methods ...

  public static synchronized TimeZone getDefault() {
    return defaultTZ;
  }

  public static synchronized TimeZone getTimeZone(String rID) {
     if (getTimeZoneResourceBundle() == null) {
        return null;
     }

     TimeZone result = null;
     if (tzResBundle != null) {
       result = tzResBundle.getTimeZone(rID);
     }
     if (result == null) {
       result = new SimpleTimeZone(0, "GMT");
     }

     return result;
  }

  public static synchronized String[] getAvailableIDs() {
     if (getTimeZoneResourceBundle() == null) {
        return new String[0];
     }
     String[] result = tzResBundle.getKeysArray();

     return result;
  }

  public static synchronized String[] getAvailableIDs(int rawOffset) {
     if (getTimeZoneResourceBundle() == null) {
        return new String[0];
     }
     // TODO - add synonyms
     return tzResBundle.getKeysArray(rawOffset);
  }


}

