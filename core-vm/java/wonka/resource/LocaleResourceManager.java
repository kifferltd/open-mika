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

import java.util.Properties;
import java.util.Locale;

public class LocaleResourceManager {

  private static final char underscore = '_';
  private static final Locale loc = Locale.getDefault();

  private Properties languages;
  private Properties countries;
  private Properties variants;
  private Locale[] availableLocales;
  private String[] ISOCountries;
  private String[] ISOLanguages;

  /**
   * * the constructor build a Properties with languages and their translation
   * <br> * the keys are the iso 639 codes and (Simple)TimeZone Objects *
   */
  public LocaleResourceManager() {
    languages = new Properties();
    countries = new Properties();
    variants = new Properties();
    languages.setProperty("he", "iw");
    languages.setProperty("id", "in");
    languages.setProperty("yi", "ji");
  }

  public String[] checkValues(String l, String c, String v) {
    String s = l.toLowerCase(loc);
    String[] res = new String[3];
    res[0] = languages.getProperty(s, s);
    s = c.toUpperCase(loc);
    res[1] = countries.getProperty(s, s);
    s = v.toUpperCase(loc);
    res[2] = variants.getProperty(s, s);
    return res;
  }

  public static Locale getLocale(String key) {
    // this method can be called by getDefault Locale if the Default Locale is
    // null
    // every method that is called by this one CANNOT use Locale.getDefault
    // that is why checkValues uses toLowerCase(Locale loc)
    int i = key.indexOf(underscore);
    if (i < 1) {
      return loc;
    }
    String l = key.substring(0, i++);
    String c = "";
    String v = "";
    int j = key.indexOf(underscore, i);
    if (i < j) {
      c = key.substring(i, j++);
      v = key.substring(j);
    }
    System.out.println("ready to build " + l + "_" + c + "_" + v);
    return new Locale(l, c, v);
  }

  public Locale[] getAvailableLocales() {
    if (availableLocales == null) {
      createLocaleList();
    }
    return availableLocales;
  }

  public String[] getISOCountries() {
    if (ISOCountries == null) {
      createISOCountriesList();
    }
    return ISOCountries;

  }

  public String[] getISOLanguages() {
    if (ISOLanguages == null) {
      createISOLanguagesList();
    }
    return ISOLanguages;

  }

  private void createISOLanguagesList() {
    ISOLanguages = new String[10];
    ISOLanguages[0] = "nl";
    ISOLanguages[1] = "de";
    ISOLanguages[2] = "en";
    ISOLanguages[3] = "fr";
    ISOLanguages[4] = "ja";
    ISOLanguages[5] = "zh";
    ISOLanguages[6] = "es";
    ISOLanguages[7] = "pt";
    ISOLanguages[8] = "ar";
    ISOLanguages[9] = "ru";

  }

  private void createISOCountriesList() {
    ISOCountries = new String[10];
    ISOCountries[0] = "BE";
    ISOCountries[1] = "DE";
    ISOCountries[2] = "FR";
    ISOCountries[3] = "GB";
    ISOCountries[4] = "US";
    ISOCountries[5] = "JP";
    ISOCountries[6] = "TW";
    ISOCountries[7] = "CN";
    ISOCountries[8] = "NL";
    ISOCountries[9] = "LU";

  }

  private void createLocaleList() {
    availableLocales = new Locale[] {
      Locale.CANADA, Locale.CHINA, Locale.FRANCE, Locale.GERMANY,
      Locale.ITALY, Locale.JAPAN, Locale.KOREA, Locale.PRC, 
      Locale.UK, Locale.US, Locale.TAIWAN, Locale.CANADA_FRENCH,
      Locale.CHINESE, Locale.ENGLISH, Locale.GERMAN, 
      Locale.FRENCH, Locale.ITALIAN, Locale.JAPANESE, Locale.KOREAN,
      Locale.SIMPLIFIED_CHINESE, Locale.TRADITIONAL_CHINESE};
  }
}
