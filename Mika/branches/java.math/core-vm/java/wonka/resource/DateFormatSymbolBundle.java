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

package wonka.resource;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ListResourceBundle;

public class DateFormatSymbolBundle extends ListResourceBundle {

  private static final String[] AMPMS = {"AM","PM"};
  private static final String[] ERAS = {"BC","AD"};
  private static final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August",
                                          "September", "October", "November", "December", ""};
  private static final String[] SHORTMONTHS = { "Jan",  "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                                                "Aug", "Sep", "Oct", "Nov", "Dec", ""};
  private static final String[] SHORTWEEKDAYS = {"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
  private static final String[] WEEKDAYS = {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
  private static final String[][] ZONESTRINGS;

  static {
    // TODO: [CG 20080207] This makes me a little nervous. Is it not possible
    // that this static initialiser might execute before the timeZoneNames
    // have been filled in?
    ArrayList zonestrings = new ArrayList();
    Enumeration zonekeys = TimeZoneDisplayNameResourceBundle.timeZoneNames.keys();
    while (zonekeys.hasMoreElements()) {
      String key = (String)zonekeys.nextElement();
      String[] from = (String[])TimeZoneDisplayNameResourceBundle.timeZoneNames.get(key);
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














