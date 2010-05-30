/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2009 by Chris Gray, /k/ Embedded Java Solutions.    *
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

package wonka.resource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class DateFormatSymbolBundle extends ListResourceBundle {

  private static final String[] AMPMS = {"AM","PM"};
  private static final String[] ERAS = {"BC","AD"};
  private static final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August",
                                          "September", "October", "November", "December", ""};
  private static final String[] SHORTMONTHS = { "Jan",  "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                                                "Aug", "Sep", "Oct", "Nov", "Dec", ""};
  private static final String[] SHORTWEEKDAYS = {"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
  private static final String[] WEEKDAYS = {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
  private static final String[][] STRINGS = {AMPMS, ERAS, MONTHS, SHORTMONTHS, WEEKDAYS, SHORTWEEKDAYS};
  private static final HashMap lookup = new HashMap();
  private static final String[][] ZONESTRINGS;

  static {
    // Each entry in 'lookup' maps a key string to a pair of indices into
    // 'STRINGS': so {0, 0} is the first element of AMPMS, {2, 4} is the
    // fourth element of MONTHS, etc..
    // The key is always the lower-case English value of the item.
    lookup.put("am", new int[]{0, 0});
    lookup.put("pm", new int[]{0, 1});
    lookup.put("bc", new int[]{1, 0});
    lookup.put("ad", new int[]{1, 1});
    lookup.put("january", new int[]{2, 0});
    lookup.put("february", new int[]{2, 1});
    lookup.put("march", new int[]{2, 2});
    lookup.put("april", new int[]{2, 3});
    lookup.put("maylong", new int[]{2, 4});
    lookup.put("june", new int[]{2, 5});
    lookup.put("july", new int[]{2, 6});
    lookup.put("august", new int[]{2, 7});
    lookup.put("september", new int[]{2, 8});
    lookup.put("october", new int[]{2, 9});
    lookup.put("november", new int[]{2, 10});
    lookup.put("december", new int[]{2, 11});
    lookup.put("jan", new int[]{3, 0});
    lookup.put("feb", new int[]{3, 1});
    lookup.put("mar", new int[]{3, 2});
    lookup.put("apr", new int[]{3, 3});
    lookup.put("may", new int[]{3, 4});
    lookup.put("jun", new int[]{3, 5});
    lookup.put("jul", new int[]{3, 6});
    lookup.put("aug", new int[]{3, 7});
    lookup.put("sep", new int[]{3, 8});
    lookup.put("oct", new int[]{3, 9});
    lookup.put("nov", new int[]{3, 10});
    lookup.put("dec", new int[]{3, 11});
    lookup.put("sunday", new int[]{4, 1});
    lookup.put("monday", new int[]{4, 2});
    lookup.put("tuesday", new int[]{4, 3});
    lookup.put("wednesday", new int[]{4, 4});
    lookup.put("thursday", new int[]{4, 5});
    lookup.put("friday", new int[]{4, 6});
    lookup.put("saturday", new int[]{4, 7});
    lookup.put("sun", new int[]{5, 1});
    lookup.put("mon", new int[]{5, 2});
    lookup.put("tue", new int[]{5, 3});
    lookup.put("wed", new int[]{5, 4});
    lookup.put("thu", new int[]{5, 5});
    lookup.put("fri", new int[]{5, 6});
    lookup.put("sat", new int[]{5, 7});
    InputStream dsis = ClassLoader.getSystemResourceAsStream(System.getProperty("mika.datesymbols", "mika.datesymbols"));
    if (dsis != null) {
      Locale locale = Locale.getDefault();
      String language = locale.getLanguage();
      String country = locale.getCountry();
      if (language == null) {
        language = "";
      }
      if (country == null) {
        country = "";
      }
      String suffix1 = null;
      String suffix2 = null;
      if (language.length() == 0) {
        suffix1 = country.length() == 0 ? "" : ("." + country);
      }
      else {
        suffix1 = "." + language;
        suffix2 = country.length() == 0 ? null : (suffix1 + "." + country);
      }
      HashMap work = new HashMap();
      Properties dsprops = new Properties();
      try {
        dsprops.load(dsis);
        processDateSymbols(dsprops, suffix1, suffix2, work);
      } catch (IOException ioe) {
        System.err.println("Error reading mika.datesymbols file");
        ioe.printStackTrace();
      }
      updateStrings(suffix1, suffix2, work);
    }
    // TODO: [CG 20080207] This makes me a little nervous. Is it not possible
    // that this static initialiser might execute before the timeZoneNames
    // have been filled in?
    // NOTE: we don't localise the timezone displays because it's easier to
    // just translate the whole mika.timezones file into the user's language.
    ArrayList zonestrings = new ArrayList();
    Enumeration zonekeys = TimeZoneDisplayNameResourceBundle.timeZoneNames.keys();
    while (zonekeys.hasMoreElements()) {
      String key = (String)zonekeys.nextElement();
      Object value = TimeZoneDisplayNameResourceBundle.timeZoneNames.get(key);
      while (value instanceof String) {
        value = TimeZoneDisplayNameResourceBundle.timeZoneNames.get(value);
      }
      String[] from = (String[])value;
      String[] to = new String[5];
      to[0] = key;
      to[1] = from[1];
      to[2] = from[0];
      to[3] = from[3];
      to[4] = from[2];

      zonestrings.add(to);
    }
    ZONESTRINGS = new String[zonestrings.size()][];
    zonestrings.toArray(ZONESTRINGS);
  }

  /**
   * Process the mika.datesymbols file. The keys consist of a base
   * key such as "mon" either on its own or followed by suffix1 or suffix2.
   * If multiple keys with the same base are present then the one with the
   * longest suffix will be used (suffix2 > suffix1).
   */
  private static void processDateSymbols(Properties dsprops, String suffix1, String suffix2, HashMap work) {
    Enumeration keys = dsprops.propertyNames();
    while (keys.hasMoreElements()) {
       String lhs = (String)keys.nextElement();
       String rhs = dsprops.getProperty(lhs).trim();
      if (rhs.length() == 0) {
        System.err.println("mika.datesymbols key '" + lhs + "' has empty value");
        continue;
      }

      String base = lhs;
      String suffix = "";
      int firstdot = lhs.indexOf('.');
      if (firstdot >= 0) {
        base = lhs.substring(0, firstdot);
        suffix = lhs.substring(firstdot);
      }

      if (suffix.equals("") || suffix.equals(suffix1) || suffix.equals(suffix2)) {

        work.put(lhs, rhs);
      }
    }
  }

  private static void updateStrings(String suffix1, String suffix2, HashMap work) {
    Iterator iter = work.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry entry = (Map.Entry) iter.next();
      String lhs = (String) entry.getKey();
      String rhs = (String) entry.getValue();
      String base = lhs;
      String suffix = "";
      int firstdot = lhs.indexOf('.');
      if (firstdot >= 0) {
        base = lhs.substring(0, firstdot);
        suffix = lhs.substring(firstdot);
      }

      boolean accept = false;
      switch (suffix.length() / 3) {
        case 0:
          if (suffix1 != null && work.containsKey(base + suffix1)) break;
        case 1:
          if (suffix2 != null && work.containsKey(base + suffix2)) break;
        default:
          accept = true;
      }

      if (accept) {
      int[] indices = (int[]) lookup.get(base.toLowerCase());
      if (indices == null) {
        return;
      }
      else {
        int index0 = indices[0];
        int index1 = indices[1];
        STRINGS[index0][index1] = rhs;
      }
      }
    }
  }

  public DateFormatSymbolBundle(){
    super();
  }

  public Object[][] getContents(){
    return new Object[][] {
      {"ampms", AMPMS},
      {"eras", ERAS},
      { "pattern", "GyMdkHmsSEDFwWahKz"},
      {"months",MONTHS},
      {"shortMonths",SHORTMONTHS},
      {"shortDays",SHORTWEEKDAYS},
      {"days",WEEKDAYS},
      {"zones",ZONESTRINGS}
    };
  }
}














