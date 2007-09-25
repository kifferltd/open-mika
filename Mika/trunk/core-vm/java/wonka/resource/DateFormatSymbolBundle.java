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
  private static final String[][] ZONESTRINGS = {
    {"GMT", "Greenwich Mean Time", "GMT", "Greenwich Mean Time", "GMT"},
    {"MIT", "Midway Islands Time", "MIT", "Midway Islands Time", "MIT"},
    {"HST", "Hawaii Standard Time","HST","Hawaii Daylight Time", "HDT"},
    {"AST", "Alaska Standard Time","AST","Alaska Daylight Time", "ADT"},
    {"PST", "Pacific Standard Time","PST","Pacific Daylight Time", "PDT"},
    {"PNT", "Central Standard Time","MST","Central Standard Time", "MST"},
    {"MST", "Mountain Standard Time","MST","Mountain Daylight Time", "MDT"},
    {"CST", "Central Standard Time","CST","Central Daylight Time", "CDT"},
    {"EST", "Eastern Standard Time","EST","Eastern Daylight Time", "EDT"},
    {"IET", "Eastern Standard Time","EST","Eastern Standard Time", "EST"},
    {"PRT", "Atlantic Standard Time","AST","Atlantic Daylight Time", "ADT"},
    {"CNT", "Newfoundland Standard Time","NST","Newfoundland Standard Time", "NST"},
    {"NST", "Newfoundland Standard Time","NST"," Newfoundland Daylight Time", "NDT"},
    {"ECT", "European Central Time","ECT","European Central Time", "ECT"},
    {"WET", "Western European Time","WET","Western European Time", "WET"},
    {"CTT", "China Standard Time","CTT","China Daylight Time", "CTT"},
    {"JST", "Japan Standard Time","JST","Japan Daylight Time", "JDT"},
  };

  private static final Object[][] contents = {
    {"ampms", AMPMS},{"eras", ERAS},{ "pattern", "GyMdkHmsSEDFwWahKz"},{"months",MONTHS},{"shortMonths",SHORTMONTHS},
    {"shortDays",SHORTWEEKDAYS},{"days",WEEKDAYS},{"zones",ZONESTRINGS}
  };

  public DateFormatSymbolBundle(){
    super();
  }

  public Object[][] getContents(){
    return contents;
  }
}














