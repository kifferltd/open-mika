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

package com.acunia.resource;

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