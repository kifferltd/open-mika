/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2004, 2005 by Chris Gray, /k/ Embedded Java         *
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

package wonka.resource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class TimeZoneResourceBundle extends ResourceBundle {

  private static Hashtable timeZones;	

  static {
    InputStream tzis = ClassLoader.getSystemResourceAsStream(System.getProperty("mika.timezones", "mika.timezones"));
    if (tzis == null) {
      tzis = new ByteArrayInputStream("GMT=0\n".getBytes());
    }
    timeZones = new Hashtable();
    BufferedReader tzbr = new BufferedReader(new InputStreamReader(tzis));
    try {
      String tzline = tzbr.readLine();
      while (tzline != null) {
        processTimeZoneLine(tzline);
        tzline = tzbr.readLine();
      }
    } catch (IOException ioe) {
      System.err.println("Error reading mika.timezones file");
      ioe.printStackTrace();
    }
  }

  private static void processTimeZoneLine(String tzline) {
    if (tzline.length() == 0 || tzline.charAt(0) == '#') {

      return;

    }

    int equalssign = tzline.indexOf('=');
    if (equalssign < 0) {
      System.err.println("No '=' sign in mika.timezones line: " + tzline);
      return;
    }

    String lhs = tzline.substring(0, equalssign).trim();
    String rhs = tzline.substring(equalssign + 1).trim();
    if (rhs.length() == 0) {
      System.err.println("Nothing after '=' sign in mika.timezones line: " + tzline);
      return;
    }
    char rhs1stchar = rhs.charAt(0);
    if (Character.isDigit(rhs1stchar) || rhs1stchar == '-' || rhs1stchar == '+') {
      int leftparen = rhs.indexOf('(');
      int rightparen = rhs.indexOf(')');
      float basicoffset;
        try {
        if (leftparen < 0 && rightparen < 0) {
          basicoffset = Float.parseFloat(rhs);
          SimpleTimeZone stz = new SimpleTimeZone((int)(basicoffset * 3600000F), lhs);
          timeZones.put(lhs, stz);
	  // System.out.println(lhs + " => " + stz);
        }
        else if (leftparen < 0 || rightparen < 0 || rightparen < leftparen) {
          System.err.println("Mismatched parentheses in mika.timezones line: " + tzline);
          return;
        }
        else {
  	  basicoffset = Float.parseFloat(rhs.substring(0, leftparen));
	  String dststring = rhs.substring(leftparen + 1, rightparen);
	  //float savings = 
    Float.parseFloat(rhs.substring(rightparen + 1));
	  StringTokenizer st = new StringTokenizer(dststring, ",");
	  try {
            int startmonth = Integer.parseInt(st.nextToken()) - 1;
            int startday = Integer.parseInt(st.nextToken());
            int startdayofweek = (Integer.parseInt(st.nextToken()) % 7) + 1;
            int starttime = (int)(Float.parseFloat(st.nextToken()) * 3600000F);
            int endmonth = Integer.parseInt(st.nextToken()) - 1;
            int endday = Integer.parseInt(st.nextToken());
            int enddayofweek = (Integer.parseInt(st.nextToken()) % 7) + 1;
            int endtime = (int)(Float.parseFloat(st.nextToken()) * 3600000F);
            SimpleTimeZone stz = new SimpleTimeZone((int)(basicoffset * 3600000F), lhs, startmonth, startday, startdayofweek, starttime, endmonth, endday, enddayofweek, endtime);
            timeZones.put(lhs, stz);
	    // System.out.println(lhs + " => " + stz);
	  }
	  catch (NoSuchElementException nsee) {
            System.err.println("Too few DST elements in mika.timezone line: " + tzline);
	    return;
	  }
        }
      }
      catch (NumberFormatException nfe) {
        System.err.println("NumberFormatException in mika.timezone line: " + tzline);
	return;
      }
    }
    else {
      SimpleTimeZone stz = (SimpleTimeZone)timeZones.get(rhs);
      if (stz == null) {
        System.err.println("Unknown right-hand side in mika.timezone line: " + tzline);
	return;
      }
      timeZones.put(lhs, rhs);
      //  System.out.println(lhs + " => " + rhs);
    }
  }

  //required implementation of abstract methods of ResourceBundle
  protected Object handleGetObject(String key) throws MissingResourceException {
    Object o = timeZones.get(key);
    while ((o != null) && (o instanceof String)) {
      o = timeZones.get(o);
    }
    if (o != null) {
      return o;
    }
    throw new MissingResourceException("Oops, resource not found","TimeZoneResourceBundle","key");
  }

  public Enumeration getKeys() {
    return timeZones.keys();
  }


// methods for easy use ( TimeZone OBJECT )
  /**
  ** this method is called by the static method getAvailableIDs() of TimeZone <br>
  ** MAKE SURE THIS METHOD STAYS AVAILABLE !!!
  */
  public String[] getKeysArray() {
    int length = timeZones.size();
    String [] keys = new String[length];
    Enumeration e = timeZones.keys();
    int i=0;
    while (e.hasMoreElements()) {
      keys[i++] = (String) e.nextElement();
    }
    return keys;
  }	

  /**
  ** this method is called by the static method getAvailableIDs(int rawOffset) of TimeZone <br>
  ** MAKE SURE THIS METHOD STAYS AVAILABLE !!!
  */
  public String[] getKeysArray(int rOffset) {
    int length = timeZones.size();
    String [] keys = new String[length];
    Enumeration e = timeZones.keys();
    int i=0;
    while (e.hasMoreElements()) {
      String s = (String) e.nextElement();
      Object o = timeZones.get(s);
      while ((o != null) && (o instanceof String)) {
        o = timeZones.get(o);
      }
      if (rOffset == ((SimpleTimeZone)o).getRawOffset()){
        keys[i++] = s;
      }
    }
    String[] okeys = new String[i];
    System.arraycopy(keys , 0 , okeys , 0 , i);
    return okeys;
  }	

  /**
  ** this method is called by the static method getTimeZone(String ID) of TimeZone <br>
  ** MAKE SURE THIS METHOD STAYS AVAILABLE !!!
  **
  ** This implementation will create time zones of the form "GMT+hh:mm" or
  ** "GMT-hh:mm" on demand, adding them to its hashtable. Note that the
  ** result returned by getAvailableIDs() will only include these zones
  ** after they have been requested using getTimeZone(), even though in
  ** a sense they were always available ...
  */
  public TimeZone getTimeZone(String keyID) {
    Object o = null;
    String alias = null;
    SimpleTimeZone z; 

    synchronized(timeZones) {
      o = timeZones.get(keyID);
      if ((o != null) && (o instanceof String)) {
        alias = keyID;
        while ((o != null) && (o instanceof String)) {
          o = timeZones.get(o);
        }
      }
      z = (SimpleTimeZone)o;
      if (alias != null) {
        z = (SimpleTimeZone)z.clone();
        z.setID(alias);
      }

      if (z == null && keyID.length() == 9 && keyID.charAt(6) == ':' && (keyID.charAt(3) == '+' || keyID.charAt(3) == '-') && keyID.charAt(4) >= '0' && keyID.charAt(4) <= '2' && keyID.charAt(5) >= '0' && keyID.charAt(5) <= '9' && keyID.charAt(7) >= '0' && keyID.charAt(7) <= '5' && keyID.charAt(8) >= '0' && keyID.charAt(8) <= '9') {
        String prefix = null;
        TimeZone base = null;
	Enumeration e = timeZones.keys();
	while (e.hasMoreElements()) {
          String candidate = (String)e.nextElement();
	  if (keyID.startsWith(candidate)) {
            try {
              base = (SimpleTimeZone)timeZones.get(candidate);
	      prefix = candidate;
	      break;
	    }
	    catch (ClassCastException cce) {}
	  }
	}
        if (prefix != null) {
          char sign_char = keyID.charAt(3);
          int h10 = Character.digit(keyID.charAt(4), 3);
          int h1  = Character.digit(keyID.charAt(5), 10);
          int m10 = Character.digit(keyID.charAt(7), 7);
          int m1  = Character.digit(keyID.charAt(8), 10);
          if (h10 >= 0 && h1 >= 0 && m10 >= 0 && m1 >= 0) {
            int mins = m1 + 10 * (m10 + 6 * (h1 + 10 * h10)); 
            if (sign_char == '+') {
              z = (SimpleTimeZone)base.clone(); 
              z.setID(keyID);
              z.setRawOffset(z.getRawOffset() + 1000*60*mins);
              timeZones.put(keyID, z);
	      // System.out.println(keyID + " => " + z);
            }
            else if (sign_char == '-') {
              z = (SimpleTimeZone)base.clone(); 
              z.setID(keyID);
              z.setRawOffset(z.getRawOffset() - 1000*60*mins);
              timeZones.put(keyID, z);
	      // System.out.println(keyID + " => " + z);
            }
          }
        }
      }
    }

    return z;
  }

}

