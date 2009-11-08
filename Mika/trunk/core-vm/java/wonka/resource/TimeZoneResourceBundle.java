/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2004, 2005, 2008, 2009 by Chris Gray, /k/ Embedded  *
* Java Solutions. All rights reserved.                                    *
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

  /**
   * Mapping of timezone names onto either a SimpleTimeZone object or
   * a String which is the name of another time zone for which the
   * current key is an alias. Thus if a given key returns a String value
   * the query should be repeated with the String value as key.
   */
  private static Hashtable timeZones = new Hashtable();	

  /**
   * The name of the default time zone (may be an alias). If no default
   * is specified in the mika.timezones file then GMT is used.
   */
  private static String defaultTimeZoneName = "GMT";

  /**
   * The static initializer reads in and processes the mika.timezones file.
   */
  static {
    InputStream tzis = ClassLoader.getSystemResourceAsStream(System.getProperty("mika.timezones", "mika.timezones"));
    if (tzis == null) {
      tzis = new ByteArrayInputStream("GMT=0\n".getBytes());
    }
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

  /**
   * Process one line of the mika.timezones file.
   * <dl><dt>Line starting with '#'
   *     <dd>Comment line, ignore.
   * </dl>
   * <p>Ohterwise the line must contain an equals sign; the string to the
   * left of this we call 'lhs' and the string to the right 'rhs'.
   * <dl><dt>lhs is "Default"
   *     <dd>Store rhs as <code>defaultTimeZoneName</code>.
   *     <dd>rhs starts with '+', '-', or a digit
   *     <dt>Parse the rhs as a SimpleTimeZone definition; for details see
   *         the <code>mika.timezones</code> file. The SimpleTimeZone
   *         definition may optionally be followed by up to four quotes
   *         strings which are the short and long names for the timezone
   *         without and with daylight saving time. Set a mapping from
   *         lhs to this SimpleTimeZone.
   *     <dt>Otherwise (rhs does not start with sign or digit)
   *     <dd>Treat as an alias: the rhs must correspond to the lhs of a
   *         line which was already processed. Set a mapping from
   *         lhs to this String.
   * </dl>
   */ 
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
    if ("Default".equalsIgnoreCase(lhs)) {
      defaultTimeZoneName = rhs;
      return;
    }
    char rhs1stchar = rhs.charAt(0);
    if (Character.isDigit(rhs1stchar) || rhs1stchar == '-' || rhs1stchar == '+') {
      // Strip off quoted strings from end
      String[] names = new String[4];
      while (names[3] == null && rhs.charAt(rhs.length() - 1) == '"') {
        rhs = rhs.substring(0, rhs.length() - 1);
        int openquote = rhs.lastIndexOf('"');
        if (openquote < 0) {
          System.err.println("Mismatched quotes in mika.timezones line: " + tzline);
          return;
        }
        names[3] = names[2];
        names[2] = names[1];
        names[1] = names[0];
        names[0] = rhs.substring(openquote + 1);
        rhs = rhs.substring(0, openquote).trim();
      }

      if (names[0] == null) {
        names[0] = lhs;
      }
      if (names[1] == null) {
        names[1] = names[0];
      }
      if (names[2] == null) {
        names[2] = names[0];
      }
      if (names[3] == null) {
        names[3] = names[2];
      }
      TimeZoneDisplayNameResourceBundle.timeZoneNames.put(lhs, names);
      if (TimeZoneDisplayNameResourceBundle.timeZoneNames.get(names[0]) == null) {
        TimeZoneDisplayNameResourceBundle.timeZoneNames.put(names[0], names);
      }

      int leftparen = rhs.indexOf('(');
      int rightparen = rhs.indexOf(')');
      float basicoffset;
        try {
        if (leftparen < 0 && rightparen < 0) {
          basicoffset = Float.parseFloat(rhs);
          SimpleTimeZone stz = new SimpleTimeZone((int)(basicoffset * 3600000F), lhs);
          timeZones.put(lhs, stz);
        }
        else if (leftparen < 0 || rightparen < 0 || rightparen < leftparen) {
          System.err.println("Mismatched parentheses in mika.timezones line: " + tzline);
          return;
        }
        else {
  	  basicoffset = Float.parseFloat(rhs.substring(0, leftparen));
	  String dststring = rhs.substring(leftparen + 1, rightparen);
	  float savings = Float.parseFloat(rhs.substring(rightparen + 1));
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
            SimpleTimeZone stz = new SimpleTimeZone((int)(basicoffset * 3600000F), lhs, startmonth, startday, startdayofweek, starttime, endmonth, endday, enddayofweek, endtime/*, savings*/);
            timeZones.put(lhs, stz);
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

  /**
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
  ** This implementation will create time zones of the form "GMT+hh:mm" or
  ** "GMT-hh:mm" on demand, adding them to its hashtable. Note that the
  ** result returned by getAvailableIDs() will only include these zones
  ** after they have been requested using getTimeZone(), even though in
  ** a sense they were always available ...
  */
  public TimeZone getTimeZone(String keyID) {
    Object o = null;
    String alias = null;
    String canonical = keyID;
    SimpleTimeZone z; 

    synchronized(timeZones) {
      o = timeZones.get(keyID);
      if ((o != null) && (o instanceof String)) {
        alias = keyID;
        while ((o != null) && (o instanceof String)) {
          canonical = (String)o;
          o = timeZones.get(o);
        }
      }
      z = (SimpleTimeZone)o;
      if (alias != null) {
        z = (SimpleTimeZone)z.clone();
        z.setID(alias);
        TimeZoneDisplayNameResourceBundle.timeZoneNames.put(alias, canonical);
      }

      int l = canonical.length();
      if (z == null && l >= 7 && canonical.charAt(l - 3) == ':' && (canonical.charAt(l - 6) == '+' || canonical.charAt(l - 6) == '-')) {
        String prefix = null;
        TimeZone base = null;
	Enumeration e = timeZones.keys();
	while (e.hasMoreElements()) {
          String candidate = (String)e.nextElement();
	  if (candidate.length() == l - 6 && canonical.startsWith(candidate)) {
            try {
              base = (SimpleTimeZone)timeZones.get(candidate);
	      prefix = candidate;
	      break;
	    }
	    catch (ClassCastException cce) {}
	  }
	}
        if (prefix != null) {
          char sign_char = canonical.charAt(l - 6);
          try {
            int hours = Integer.parseInt(canonical.substring(l - 5, l - 3));
            int mins = Integer.parseInt(canonical.substring(l - 2));
            if (hours < 0 || hours > 23 || mins < 0 || mins > 59) {
              throw new NumberFormatException();
            }
            int raw = base.getRawOffset();
            z = (SimpleTimeZone)base.clone(); 
            z.setID(canonical);
            if (sign_char == '+') {
              z.setRawOffset(base.getRawOffset() + 1000*60*(60 * hours + mins));
            }
            else {
              z.setRawOffset(base.getRawOffset() - 1000*60*(60 * hours + mins));
            }
          }
          catch (NumberFormatException nfe) {
            System.err.println("Malformed relative timezone: " + canonical);
          }
        }
      }
    }

    return z;
  }

  /**
   * Get the default time zone, i.e. the one specified in the mika.timezones
   * file as "Default=FOO", or GMT if no such line is present in the file.
   * @return The default time zone.
   */
  public TimeZone getDefaultTimeZone() {
    return getTimeZone(defaultTimeZoneName);
  }
}

