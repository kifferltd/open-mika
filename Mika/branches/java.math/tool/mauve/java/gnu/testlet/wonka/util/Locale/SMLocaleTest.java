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


package gnu.testlet.wonka.util.Locale;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.*;

/**
*  this file contains test for java.util.Locale   <br>
*  methods asking for resources are not tested since the availability <br>
*  of those resources is rather limited at this point !!!
*/
public class SMLocaleTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.Locale");
       test_Locale();
       test_PredefinedLocales();
       test_setDefault();
       test_getAvailableLocales();
       test_getCountry();
       test_getDefault();
       test_getDisplayCountry();
       test_getDisplayLanguage();
       test_getDisplayName();
       test_getDisplayVariant();
       test_getISOCountries();
       test_getISO3Country();
       test_getISOLanguages();
       test_getISO3Language();
       test_getLanguage();
       test_getVariant();
       test_clone();
       test_equals();
       test_hashCode();
       test_toString();
     }

/**
* implemented. <br>
*
*/
  public void test_Locale(){
    th.checkPoint("Locale(java.lang.String,java.lang.String)");
    try {
    	new Locale(null , "abc");
    	th.fail("should throw NullPointerExeption -- 1");
    }
    catch(NullPointerException npe) { th.check(true); }

    try {
    	new Locale("null" , null);
    	th.fail("should throw NullPointerExeption -- 2");
    }
    catch(NullPointerException npe) { th.check(true); }

    Locale loc = new Locale("be", "AC");
    th.check(loc.getLanguage() , "be" , "checking language -- 1");
    th.check(loc.getCountry() , "AC" , "checking country -- 1");
    th.check(loc.getVariant() , "" , "checking variant -- 1");
    loc = new Locale("", "ok");
    th.check(loc.getLanguage() , "" , "checking language -- 2");
    th.check(loc.getCountry() , "OK" , "checking country -- 2");
    loc = new Locale("ABC", "def");
    th.check(loc.getLanguage() , "abc" , "checking language -- 3");
    th.check(loc.getCountry() , "DEF" , "checking country -- 3");
    loc = new Locale("A_c", "d_e_f");
    th.check(loc.getLanguage() , "a_c" , "checking language -- 4");
    th.check(loc.getCountry() , "D_E_F" , "checking country -- 4");
    th.check(loc.getVariant() , "" , "checking variant -- 4");
    loc = new Locale("HE", "@9f");
    th.check(loc.getLanguage() , "iw" , "checking language -- 5");
    th.check(loc.getCountry() , "@9F" , "checking country -- 5");
    loc = new Locale("yI", "_f");
    th.check(loc.getLanguage() , "ji" , "checking language -- 6");
    th.check(loc.getCountry() , "_F" , "checking country -- 6");
    loc = new Locale("iD", "be");
    th.check(loc.getLanguage() , "in" , "checking language -- 7");
    th.check(loc.getCountry() , "BE" , "checking country -- 7");
    th.check(loc.getVariant() , "" , "checking variant -- 7");

    th.checkPoint("Locale(java.lang.String,java.lang.String,java.lang.String)");

    try {
    	new Locale(null , "abc" , "def");
    	th.fail("should throw NullPointerExeption -- 1");
    }
    catch(NullPointerException npe) { th.check(true); }

    try {
    	new Locale("null" , null ,"def");
    	th.fail("should throw NullPointerExeption -- 2");
    }
    catch(NullPointerException npe) { th.check(true); }

    try {
    	new Locale("null" , "null" ,null);
    	th.fail("should throw NullPointerExeption -- 3");
    }
    catch(NullPointerException npe) { th.check(true); }
    loc = new Locale("be", "AC", "tof");
    th.check(loc.getLanguage() , "be" , "checking language -- 1");
    th.check(loc.getCountry() , "AC" , "checking country -- 1");
    th.check(loc.getVariant() , "TOF" , "checking variant -- 1");
    loc = new Locale("", "ok", "tOf");
    th.check(loc.getLanguage() , "" , "checking language -- 2");
    th.check(loc.getCountry() , "OK" , "checking country -- 2");
    th.check(loc.getVariant() , "TOF" , "checking variant -- 2");
    loc = new Locale("ABC", "def", "T_O_F");
    th.check(loc.getLanguage() , "abc" , "checking language -- 3");
    th.check(loc.getCountry() , "DEF" , "checking country -- 3");
    th.check(loc.getVariant() , "T_O_F" , "checking variant -- 3");
    loc = new Locale("A_c", "", "tof");
    th.check(loc.getLanguage() , "a_c" , "checking language -- 4");
    th.check(loc.getCountry() , "" , "checking country -- 4");
    th.check(loc.getVariant() , "TOF" , "checking variant -- 4");
    th.debug(loc.toString());
    loc = new Locale("HE", "@9f","tof");
    th.check(loc.getLanguage() , "iw" , "checking language -- 5");
    th.check(loc.getCountry() , "@9F" , "checking country -- 5");
    th.check(loc.getVariant() , "TOF" , "checking variant -- 5");
    loc = new Locale("yI", "_f","tof");
    th.check(loc.getLanguage() , "ji" , "checking language -- 6");
    th.check(loc.getCountry() , "_F" , "checking country -- 6");
    th.check(loc.getVariant() , "TOF" , "checking variant -- 6");
    loc = new Locale("iD", "be","");
    th.check(loc.getLanguage() , "in" , "checking language -- 7");
    th.check(loc.getCountry() , "BE" , "checking country -- 7");
    th.check(loc.getVariant() , "" , "checking variant -- 7");

  }


/**
* implemented. <br>
* this test is very important since the default locales are constructed with a special constructor <br>
* this means string passed are not checked or converted to upper or lower case if needed !
*/
  public void test_PredefinedLocales(){
    th.checkPoint("US(public)java.util.Locale");
    th.check(Locale.CANADA.toString() , "en_CA" ,"Locale.CANADA");
    th.check(Locale.CHINA.toString() , "zh_CN" ,"Locale.CHINA");
    th.check(Locale.FRANCE.toString() , "fr_FR" ,"Locale.FRANCE");
    th.check(Locale.GERMANY.toString() , "de_DE" ,"Locale.GERMANY");
    th.check(Locale.ITALY.toString() , "it_IT" ,"Locale.ITALY");
    th.check(Locale.JAPAN.toString() , "ja_JP" ,"Locale.JAPAN");
    th.check(Locale.KOREA.toString() , "ko_KR" ,"Locale.KOREA");
    th.check(Locale.PRC.toString() , "zh_CN" ,"Locale.PRC");
    th.check(Locale.TAIWAN.toString() , "zh_TW" ,"Locale.TAIWAN");
    th.check(Locale.UK.toString() , "en_GB" ,"Locale.UK");
    th.check(Locale.US.toString() , "en_US" ,"Locale.US");

    th.check(Locale.CANADA_FRENCH.toString() , "fr_CA" ,"Locale.CANADA_FRENCH");
    th.check(Locale.CHINESE.toString() , "zh" ,"Locale.CHINESE");
    th.check(Locale.ENGLISH.toString() , "en" ,"Locale.ENGLISH");
    th.check(Locale.FRENCH.toString() , "fr" ,"Locale.FRENCH");
    th.check(Locale.GERMAN.toString() , "de" ,"Locale.GERMAN");
    th.check(Locale.ITALIAN.toString() , "it" ,"Locale.ITALIAN");
    th.check(Locale.JAPANESE.toString() , "ja" ,"Locale.JAPANESE");
    th.check(Locale.KOREAN.toString() , "ko" ,"Locale.KOREAN");
    th.check(Locale.SIMPLIFIED_CHINESE.toString() , "zh_CN" ,"Locale.SIMPLIFIED_CHINESE");
    th.check(Locale.TRADITIONAL_CHINESE.toString() , "zh_TW" ,"Locale.TRADITIONAL_CHINESE");
  }

/**
*  implemented. <br>
*
*/
  public void test_setDefault(){
    th.checkPoint("setDefault(java.util.Locale)void");
    Locale def = Locale.getDefault();
    try {
    	Locale.setDefault(null);
    	th.fail("should throw NullPointerExeption -- 1");
    }
    catch(NullPointerException npe) { th.check(true); }

    Locale loc = new Locale("nl","be","linux");
    Locale.setDefault(loc);
    th.check( loc == Locale.getDefault(), "checking set/getDefault -- 1");

    //restore default locale
    Locale.setDefault(def);
    th.check( def == Locale.getDefault(), "checking set/getDefault -- 2");
  }

/**
*  implemented. <br>
*
*/
  public void test_getAvailableLocales(){
    th.checkPoint("getAvailableLocales()java.util.Locale[]");

    th.check(Locale.getAvailableLocales() != null);
  }

/**
*   implemented. <br>
*   done in constructor-tests
*/
  public void test_getCountry(){
    th.checkPoint("()");

  }

/**
*   implemented. <br>
*   done in setDefault-test
*/
  public void test_getDefault(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_getDisplayCountry(){
    th.checkPoint("getDisplayCountry()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_getDisplayLanguage(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_getDisplayName(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_getDisplayVariant(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_getISOCountries(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_getISO3Country(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_getISOLanguages(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_getISO3Language(){
    th.checkPoint("()");

  }

/**
*   implemented. <br>
*   done in constructor-tests
*/
  public void test_getLanguage(){
    th.checkPoint("()");

  }

/**
*   implemented. <br>
*   done in constructor-tests
*/
  public void test_getVariant(){
    th.checkPoint("()");

  }

/**
* implemented. <br>
*
*/
  public void test_clone(){
    th.checkPoint("clone()java.lang.Object");
    Locale lo = new Locale("be", "AC", "tof");
    Locale loc = (Locale) lo.clone();
    th.check(loc != lo , "the clone is not the same object");
    th.check(loc , lo , "both object are equal");
    th.check(loc.getLanguage() , "be" , "checking language -- 1");
    th.check(loc.getCountry() , "AC" , "checking country -- 1");
    th.check(loc.getVariant() , "TOF" , "checking variant -- 1");

  }

/**
* implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
      Locale loc = new Locale("acd","BEFG","adfg");
      th.check(! loc.equals(null) , "null is allowed");
      th.check(! loc.equals("acd_BEFG_ADFG") , "not equals to its string representation");

  }

/**
*   not implemented. <br>
*   NEED TO FIND THE CORRECT hashCode algorithm
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    Locale loc = new Locale("acd","BEFG","adfg");
    th.check(loc.hashCode(), loc.clone().hashCode() ,"checking hashcode");

  }

/**
*  implemented. <br>
*
*/
  public void test_toString(){
    th.checkPoint("toString()java.lang.String");
      Locale loc = new Locale("acd","BEFG","adfg");
      th.check(loc.toString(),"acd_BEFG_ADFG");
      loc = new Locale("be","BEF_G","a_f_g");
      th.check(loc.toString(),"be_BEF_G_A_F_G");
      loc = new Locale("ac_d","BEFG","adfg");
      th.check(loc.toString(),"ac_d_BEFG_ADFG");
      loc = new Locale("","BEFG","adfg");
      th.check(loc.toString(),"_BEFG_ADFG");
      loc = new Locale("a","","adfg");
      th.check(loc.toString(),"a");
      loc = new Locale("yi","BE_FG","adfg");
      th.check(loc.toString(),"ji_BE_FG_ADFG");
      loc = new Locale("id","BE_FG","ad_fg");
      th.check(loc.toString(), "in_BE_FG_AD_FG");
      loc = new Locale("zh","tw","a_d_f_g");
      th.check(loc.toString(),"zh_TW_A_D_F_G");
      loc = new Locale("zoth","cn","");
      th.check(loc.toString(),"zoth_CN");

  }
}
