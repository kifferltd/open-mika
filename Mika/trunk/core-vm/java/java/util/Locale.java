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


/*
** $Id: Locale.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
*/

package java.util;

import wonka.resource.LocaleResourceManager;

import java.io.*;

public final class Locale implements Cloneable, java.io.Serializable {

  private static final long serialVersionUID = 9149081749638150636L;

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    language = (String)in.readObject();
    country = (String)in.readObject();
    variant = (String)in.readObject();
    in.readInt();
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeObject(language);
    out.writeObject(country);
    out.writeObject(variant);
    out.writeInt(-1);
  }

  private String language;
  private String country;
  private String variant;

  private static ResourceBundle ISO3Languages;
  private static ResourceBundle ISO3Countries;

  private static Locale defaultLoc = new Locale("vl","VL","WONKA",0);
  private static LocaleResourceManager locResManager = new LocaleResourceManager();
  

  public static final Locale CANADA = new Locale("en","CA","",0);
  public static final Locale CHINA = new Locale("zh","CN","",0);
  public static final Locale FRANCE = new Locale("fr","FR","",0);
  public static final Locale GERMANY = new Locale("de","DE","",0);
  public static final Locale ITALY  = new Locale("it","IT","",0);
  public static final Locale JAPAN = new Locale("ja","JP","",0);
  public static final Locale KOREA = new Locale("ko","KR","",0);
  public static final Locale PRC = new Locale("zh","CN","",0);
  public static final Locale UK = new Locale("en","GB","",0);
  public static final Locale US = new Locale("en","US","",0);
  public static final Locale TAIWAN = new Locale("zh","TW","",0);

  public static final Locale CANADA_FRENCH = new Locale("fr","CA","",0);
  public static final Locale CHINESE = new Locale("zh","","",0);
  public static final Locale ENGLISH = new Locale("en","","",0);
  public static final Locale GERMAN  = new Locale("de","","",0);
  public static final Locale FRENCH  = new Locale("fr","","",0);
  public static final Locale ITALIAN   = new Locale("it","","",0);
  public static final Locale JAPANESE  = new Locale("ja","","",0);
  public static final Locale KOREAN   = new Locale("ko","","",0);
  public static final Locale SIMPLIFIED_CHINESE   = new Locale("zh","CN","",0);
  public static final Locale TRADITIONAL_CHINESE   = new Locale("zh","TW","",0);


  public Locale (String l, String c) {
    this(l,c,"");
  }

  public Locale (String language) {
    this(language, "", "");
  }

/**
** use this constructor to make the default locales ...
** no checks needed if we are really sure what arguments we get !
*/
  private Locale (String l, String c, String v, int i){
    language = l;
    country  = c;
    variant  = v;
  }

  public Locale (String l, String c, String v) {

    String [] values = locResManager.checkValues(l,c,v);

    language = values[0];
    country  = values[1];
    variant  = values[2];
  }

// instance methods ...
  public String getCountry() {
        return country;
  }	

  public String getLanguage() {
    return language;
  }

  public String getVariant() {
   	return variant;
  }
  public String toString() {
	StringBuffer result = new StringBuffer(language);
	if (!"".equals(country)) {
	 	result.append('_');
	 	result.append(country);
		if (!"".equals(variant)) {
	 		result.append('_');
	 		result.append(variant);
	        }

	}
    return new String(result);
  }

  public Object clone(){
  	return new Locale(language, country, variant);
  }
  public boolean equals(Object o){
  	if (!(o instanceof Locale)) {
  	 	return false;
  	}
  	Locale l = (Locale) o;
  	return (language.equals(l.getLanguage()) && country.equals(l.getCountry()) &&
  			variant.equals(l.getVariant()));
  }



  public int hashCode() {
  	return (language.hashCode() ^ country.hashCode() ^ variant.hashCode());

  }

  public String getISO3Language() throws MissingResourceException {
  	if ( ISO3Languages == null) {
  		ISO3Languages = ResourceBundle.getBundle("com.acunia.resource.ISO3166languageResourceBundle");
  	}
	return ISO3Languages.getString(language);
  }

  public String getISO3Country() throws MissingResourceException {
  	if ( ISO3Countries == null) {
  		ISO3Countries = ResourceBundle.getBundle("com.acunia.resource.ISO3166countryResourceBundle");
  	}
	return ISO3Countries.getString(country);
  }
  	
  public final String getDisplayCountry(){
        return getDisplayCountry(Locale.getDefault());
  }

  public String getDisplayCountry(Locale inLocale){
        ResourceBundle displayProps = ResourceBundle.getBundle("com.acunia.resource.LocaleDisplayCountryResourceBundle",inLocale);
        String s=country;
        try {
        	s = displayProps.getString(country);
        }
        catch(MissingResourceException mre) {
        // if no diplayCountry found we take country ...
        }
        return s;
  }

  public final String getDisplayLanguage(){
        return getDisplayLanguage(Locale.getDefault());
  }

  public String getDisplayLanguage(Locale inLocale){
        ResourceBundle displayProps = ResourceBundle.getBundle("com.acunia.resource.LocaleDisplayLanguageResourceBundle",inLocale);
        String s=language;
        try {
        	s = displayProps.getString(s);
        }
        catch(MissingResourceException mre) {
        // if no diplayLanguage found we take language ...
        }
        return s;
  }

  public final String getDisplayVariant(){
        return getDisplayVariant(Locale.getDefault());
  }
  public String getDisplayVariant(Locale inLocale){
        ResourceBundle displayProps = ResourceBundle.getBundle("com.acunia.resource.LocaleDisplayVariantResourceBundle",inLocale);
        String s=variant;
        try {
        	s = displayProps.getString(s);
        }
        catch(MissingResourceException mre) {
        // if no diplayVariant found we take variant ...
        }
        return s;
  }
  public final String getDisplayName(){
        return getDisplayName(Locale.getDefault());
  }

  public String getDisplayName(Locale inLocale){
  	if ( country.equals("")) {
  		return getDisplayLanguage(inLocale);
  	}
  	StringBuffer buf = new StringBuffer(getDisplayLanguage(inLocale));
        buf.append('(');
        buf.append(getDisplayCountry(inLocale));
        if (!variant.equals("")) {
        	buf.append(',');
        	buf.append(getDisplayVariant(inLocale));
        }
        buf.append(')');
        return new String(buf);
  }



// static Methods ...
// most of the static methods are handled by the LocaleResourceMangager ...


  public synchronized static Locale getDefault() {
    return defaultLoc;
  }

  public synchronized static void setDefault(Locale defLoc) {
     if (defLoc == null) throw new NullPointerException();
     defaultLoc = defLoc;
  }

  public static Locale[] getAvailableLocales(){
  	return locResManager.getAvailableLocales();
  }
  public static String[] getISOCountries(){
  	return locResManager.getISOCountries();

  }

  public static String[] getISOLanguages(){
  	return locResManager.getISOLanguages();
  }

}
