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


package gnu.testlet.wonka.text.ChoiceFormat;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*;
import java.text.*;
import java.util.Arrays;

public class AcuniaChoiceFormatTest implements Testlet {
  protected TestHarness th;

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.text.ChoiceFormat");
    test_ChoiceFormat();
    test_format();
    test_parse();
    test_applyPattern();
    test_getFormats();
    test_getLimits();
    test_setChoices();
    test_toPattern();
    test_nextDouble();
    test_previousDouble();
    test_clone();
    test_equals();
    test_hashCode();
    //test_serialization();
  }

/**
* implemented. <br>
*
*/
  public void test_ChoiceFormat(){
    th.checkPoint("test_ChoiceFormat(java.lang.String)");
    try {
      new ChoiceFormat(null);
      th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException np){
      th.check(true);
    }
    try {
      new ChoiceFormat("bad < worse");
      th.fail("should throw a IllegalArgumentException");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }
    catch(RuntimeException rte){
      th.fail("should throw a IllegalArgumentException, but threw "+rte);
    }

    th.checkPoint("test_ChoiceFormat(double[],java.lang.String[])");
    try {
      new ChoiceFormat(null,new String[0]);
      th.fail("should throw a NullPointerException -- 1");
    }
    catch(NullPointerException np){
      th.check(true);
    }
    try {
      new ChoiceFormat(new double[10],null);
      th.fail("should throw a NullPointerException -- 2");
    }
    catch(NullPointerException np){
      th.check(true);
    }
    try {
      String[] strings = new String[1];
      strings[0] = "bad news";
      new ChoiceFormat(new double[2],strings);
      th.fail("should throw a IllegalArgumentException");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }
  }

/**
* implemented. <br>
* @see alse the file format.java for more tests.
*/
  public void test_format(){
    th.checkPoint("format(long,java.lang.StringBuffer,java.text.FieldPosition)java.lang.StringBuffer");
    StringBuffer buf = new StringBuffer(128);
    double[] limits = new double[]{ 1, 5 };
    String[] formats = new String[]{"bad", "ok"};
    ChoiceFormat cf = new ChoiceFormat(limits,formats);
    StringBuffer result = cf.format(-1, buf, null);
    th.check(buf == result , "should return the 'same' StringBuffer");
    th.check(buf.toString() , "bad");
    cf.format(1, buf, null);
    th.check(buf.toString() , "badbad");
    cf.format(3, buf, null);
    th.check(buf.toString() , "badbadbad");
    cf.format(5, buf, null);
    th.check(buf.toString() , "badbadbadok");
    cf.format(6, buf, null);
    th.check(buf.toString() , "badbadbadokok");

    th.checkPoint("format(double,java.lang.StringBuffer,java.text.FieldPosition)java.lang.StringBuffer");
    buf.setLength(0);
    result = cf.format(-1, buf, null);
    th.check(buf == result , "should return the 'same' StringBuffer");
    th.check(buf.toString() , "bad");
    cf.format(3.0, buf, null);
    th.check(buf.toString() , "badbad");
    cf.format(5.0, buf, null);
    th.check(buf.toString() , "badbadok");
    cf.format(ChoiceFormat.previousDouble(5.0), buf, null);
    th.check(buf.toString() , "badbadokbad");
  }

/**
* implemented. <br>
* @see also in the file parse.java
*/
  public void test_parse(){
    th.checkPoint("parse(java.lang.String,java.text.ParsePosition)java.lang.Number");
    double[] limits = new double[]{ 1.0, 5.0 };
    String[] formats = new String[]{"bad", "ok"};
    ChoiceFormat cf = new ChoiceFormat(limits,formats);
    Number num = cf.parse("badok", new ParsePosition(1));
    th.check(num instanceof Double, "checking class type");
    th.check(Double.isNaN(num.doubleValue()) , "no valid pattern --> should return NaN");
  }

/**
* implemented. <br>
*
*/
  public void test_applyPattern(){
    th.checkPoint("applyPattern(java.lang.String)void");
    ChoiceFormat cf = new ChoiceFormat("1<ok");
    try {
      cf.applyPattern ("-1.0#Less than one|1.0<One|1.0#One to two, exclusive|2.0#Two to three, inclusive|"+
                       "3.0<Over three, up to four|4.0<Four to five, exclusive|5.0#Five and above");
      th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }
    try {
      cf.applyPattern ("1.2 \u2264 token");
      th.check(true);
    }
    catch(IllegalArgumentException iae){
      th.fail("should NOT throw an IllegalArgumentException -- 1");
    }
    try {
      cf.applyPattern ("-\u221E# token|\u221E# token");
      double[] limits = cf.getLimits();
      th.check(limits[0], Double.NEGATIVE_INFINITY ,"checking '-\\u221E' code");
      th.check(limits[1], Double.POSITIVE_INFINITY ,"checking '\\u221E' code");
    }
    catch(IllegalArgumentException iae){
      th.fail("should NOT throw an IllegalArgumentException -- 2");
    }
    try {
      cf.applyPattern ("-Infinity #token|Infinity #token");
      th.fail("should throw an IllegalArgumentException -- 2");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }
  }

/**
* implemented. <br>
*
*/
  public void test_getFormats(){
    th.checkPoint("getFormats()java.lang.String[]");
    double[] limits = new double[]{ 1.0, 5.0 };
    String[] formats = new String[]{"bad", "ok"};
    ChoiceFormat cf = new ChoiceFormat(limits,formats);
    th.check(cf.getLimits() == limits);
    th.check(cf.getFormats() == formats);
    th.check(cf.format(6.0) , "ok");
    formats[1] = "ok ???";
    th.check(cf.format(6.0) , "ok ???");
    limits[0] = 7.0;
    th.check(cf.format(6.0) , "bad");
    cf.applyPattern("1.0 #cool| 2.0 #fun| 3.0 #done");
    limits = new double[]{1.0, 2.0, 3.0};
    formats = new String[]{"cool", "fun", "done"};
    th.check(Arrays.equals(cf.getLimits(),limits), "checking new limits");
    th.check(Arrays.equals(cf.getFormats(),formats), "checking new formats");
  }

/**
* implemented. <br>
* @see test_getFormats()
*/
  public void test_getLimits(){
    th.checkPoint("()");

  }

/**
* implemented. <br>
*
*/
  public void test_setChoices(){
    th.checkPoint("setChoices(double[],java.lang.String[])void");
    ChoiceFormat cf = new ChoiceFormat("1<a");
    try {
      cf.setChoices(null,new String[0]);
      th.fail("should throw a NullPointerException -- 1");
    }
    catch(NullPointerException np){
      th.check(true);
    }
    try {
      cf.setChoices(new double[10],null);
      th.fail("should throw a NullPointerException -- 2");
    }
    catch(NullPointerException np){
      th.check(true);
    }
    try {
      String[] strings = new String[1];
      strings[0] = "bad news";
      cf.setChoices(new double[2],strings);
      th.fail("should throw a IllegalArgumentException");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }
    th.check(cf.getLimits().length , 1, "feeding bad arguments should not mess up the ChoiceFormat -- 1");
    th.check(cf.getFormats().length , 1,"feeding bad arguments should not mess up the ChoiceFormat -- 2");

    String[] strings = new String[2];
    strings[0] = "bad news";
    cf.setChoices(new double[2],strings);
    th.check(cf.format(1.0) , "null", "null elements are harmless");
    th.check(cf.clone(), cf, "checking equals handles the 'null'");
    try {
      cf.hashCode();
    }
    catch(NullPointerException npe){
      th.debug("Oops the 'null' string in the array crashes hashCode yet equals works");
    }
  }

/**
* implemented. <br>
* - we check that cf1.applyPattern(cf2.toPattern()) resuls in an equal object
*/
  public void test_toPattern(){
    th.checkPoint("toPattern()java.lang.String");
    String pattern = "-1.0<Less than one|1.0<One to two, exclusive|2.0<Two to three, inclusive|"+
                     "3.0<Over three, up to four|4.0<Four to five, exclusive|5.0<Five and above";
    checkApplyToPattern(pattern);
    checkApplyToPattern("1.2345<a|2.34<b|3.45<c|4.56<d|5.67<e");
    checkApplyToPattern("1.2345<a|2.34#b|3.45<c|4.56#d|5.67<e");
    checkApplyToPattern("1.2345#a|2.34#b|3.45#c|4.56#d|5.67#e");
    checkApplyToPattern("1.2345#a|2.34<b|3.45#c|4.56<d|5.67#e");
  }

  private void checkApplyToPattern(String pattern){
    try {
      ChoiceFormat cf1 = new ChoiceFormat(pattern);
      ChoiceFormat cf2 = new ChoiceFormat("1<a");
      cf2.applyPattern(cf1.toPattern());
      th.check(cf2, cf1, "checking equality '"+pattern+"'");
      th.check(Arrays.equals(cf2.getLimits(),cf1.getLimits()),"checking arrays '"+pattern+"'");
      //th.debug("'"+cf1.toPattern()+"' <---> '"+cf2.toPattern()+"'");
    }
    catch(IllegalArgumentException iae){
      th.fail("caught an IllegalArgumentException for '"+pattern+"'");
    }
  }

/**
* implemented. <br>
*
*/
  public void test_nextDouble(){
    th.checkPoint("nextDouble(double)double");

    th.check(Double.isNaN(ChoiceFormat.nextDouble(Double.NaN)), "checking NaN");
    th.check(ChoiceFormat.nextDouble(Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY, "checking POSITIVE_INFINITY ");
    th.check(ChoiceFormat.nextDouble(Double.NEGATIVE_INFINITY), - Double.MAX_VALUE, "checking NEGATIVE_INFINITY");
    th.check(ChoiceFormat.nextDouble(Double.MAX_VALUE), Double.POSITIVE_INFINITY, "checking MAX_VALUE");
    th.check(ChoiceFormat.nextDouble(0.0), Double.MIN_VALUE, "checking 0.0");
    th.check(ChoiceFormat.nextDouble(-0.0), Double.MIN_VALUE, "checking -0.0");

    th.checkPoint("nextDouble(double,boolean)");

    th.check(Double.isNaN(ChoiceFormat.nextDouble(Double.NaN,true)), "checking NaN -- true");
    th.check(ChoiceFormat.nextDouble(Double.POSITIVE_INFINITY,true), Double.POSITIVE_INFINITY, "checking POSITIVE_INFINITY -- true");
    th.check(ChoiceFormat.nextDouble(Double.NEGATIVE_INFINITY,true), - Double.MAX_VALUE, "checking NEGATIVE_INFINITY -- true");
    th.check(ChoiceFormat.nextDouble(Double.MAX_VALUE,true), Double.POSITIVE_INFINITY, "checking MAX_VALUE -- true");
    th.check(ChoiceFormat.nextDouble( 0.0, true), Double.MIN_VALUE, "checking  0.0 -- true");
    th.check(ChoiceFormat.nextDouble(-0.0, true), Double.MIN_VALUE, "checking -0.0 -- true");

    th.check(Double.isNaN(ChoiceFormat.nextDouble(Double.NaN,false)), "checking NaN -- false");
    th.check(ChoiceFormat.nextDouble(Double.NEGATIVE_INFINITY,false), Double.NEGATIVE_INFINITY, "checking POSITIVE_INFINITY -- false");
    th.check(ChoiceFormat.nextDouble(Double.POSITIVE_INFINITY,false), Double.MAX_VALUE, "checking NEGATIVE_INFINITY -- false");
    th.check(ChoiceFormat.nextDouble(- Double.MAX_VALUE,false), Double.NEGATIVE_INFINITY, "checking - MAX_VALUE -- false");
    th.check(ChoiceFormat.nextDouble( 0.0, false),- Double.MIN_VALUE, "checking  0.0 -- false");
    th.check(ChoiceFormat.nextDouble(-0.0, false),- Double.MIN_VALUE, "checking -0.0 -- false");

    double d = 1234.345567E123;
    th.check(ChoiceFormat.nextDouble(ChoiceFormat.nextDouble(d, true),false), d, "checking -- 1");
    th.check(ChoiceFormat.nextDouble(ChoiceFormat.nextDouble(d,false), true), d, "checking -- 2");
    d = - 1234.345567E-123;
    th.check(ChoiceFormat.nextDouble(ChoiceFormat.nextDouble(d, true),false), d, "checking -- 1");
    th.check(ChoiceFormat.nextDouble(ChoiceFormat.nextDouble(d,false), true), d, "checking -- 2");
  }

/**
* implemented. <br>
*
*/
  public void test_previousDouble(){
    th.checkPoint("previousDouble(double)double");
    th.check(Double.isNaN(ChoiceFormat.previousDouble(Double.NaN)), "checking NaN");
    th.check(ChoiceFormat.previousDouble(Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY, "checking POSITIVE_INFINITY");
    th.check(ChoiceFormat.previousDouble(Double.POSITIVE_INFINITY), Double.MAX_VALUE, "checking NEGATIVE_INFINITY");
    th.check(ChoiceFormat.previousDouble(- Double.MAX_VALUE), Double.NEGATIVE_INFINITY, "checking - MAX_VALUE");
    th.check(ChoiceFormat.previousDouble( 0.0),- Double.MIN_VALUE, "checking  0.0");
    th.check(ChoiceFormat.previousDouble(-0.0),- Double.MIN_VALUE, "checking -0.0");

  }

/**
* implemented. <br>
*
*/
  public void test_clone(){
    th.checkPoint("clone()java.lang.Object");
    double[] limits = new double[]{ 1.0, 5.0 };
    String[] formats = new String[]{"bad", "ok"};
    ChoiceFormat cf = new ChoiceFormat(limits,formats);
    ChoiceFormat clone = (ChoiceFormat) cf.clone();
    th.check(cf, clone, "checking equality of the clone");
    th.check(cf != clone ,"equals but not the some");
    double[] cloneLimits = clone.getLimits();
    String[] cloneFormats = (String[])clone.getFormats();
    th.check(cloneLimits != limits, "internal object are not the same -- 1");
    th.check(cloneFormats != formats,"internal object are not the same -- 2");
    th.check(Arrays.equals(cloneLimits, limits),"equality of internal arrays -- 1");
    th.check(Arrays.equals(cloneFormats, formats),"equality of internal arrays -- 2");
  }

/**
* implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
    double[] limits = new double[]{ 1.0, 5.0 };
    String[] formats = new String[]{"bad", "ok"};
    ChoiceFormat cf = new ChoiceFormat(limits,formats);
    th.check(!cf.equals(null), "checking 'null'");
    th.check(!cf.equals(this), "checking 'this'");
    th.check(cf.equals(new ChoiceFormat(limits,formats)) ,"checking equal -- 1");
    th.check(cf.equals(new ChoiceFormat("1.0 #bad|5.0#ok")) ,"checking equal -- 2");
    th.check(!cf.equals(new ChoiceFormat("1.0 #bad|5.0#oke")) ,"checking non equal -- 1");
    th.check(!cf.equals(new ChoiceFormat("1.0 #bad|5.0<ok")) ,"checking non equal -- 2");
    th.check(!cf.equals(new ChoiceFormat("1.0 #bad|5.0#ok|6.0<Oops")) ,"checking non equal -- 2");
  }

/**
* implemented. <br>
*
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    double[] limits = new double[]{ 1.0, 5.0 };
    String[] formats = new String[]{"bad", "ok"};
    ChoiceFormat cf = new ChoiceFormat(limits,formats);
    th.check(cf.hashCode(), new ChoiceFormat(limits,formats).hashCode() ,"checking hashCode -- 1");
    th.check(cf.hashCode(), new ChoiceFormat("1.0 #bad|5.0#ok").hashCode() ,"checking hashCode -- 2");
    th.check(cf.hashCode() != new ChoiceFormat("1.0 #bad|5.0#ok|6.0<Oops").hashCode() ,"checking hashCode -- 3");
  }

/**
* not implemented. <br>
*
*/
  public void test_serialization(){
    th.checkPoint("()");
    String pattern = "-1.23456789<Less than one|1.23456789#One|1.23456789<One to two, exclusive|2.23456789#Two to three, inclusive|"+
                     "3.23456789<Over three, up to four|4.23456789<Four to five, exclusive|5.23456789#Five and above";

    try {
      ChoiceFormat cf = new ChoiceFormat(pattern);
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("ChoiceFormat.ser"));
      oos.writeObject(cf);
      oos.close();
    }
    catch(Exception e){
      e.printStackTrace();
    }

    try {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream("ChoiceFormat.ser"));
      ChoiceFormat cf = (ChoiceFormat)ois.readObject();
      Object[] formats = cf.getFormats();
      double[] limits = cf.getLimits();
      System.out.println("lengths "+ formats.length +" and "+ limits.length);
      for(int i = 0 ; i < formats.length ; i++){
        System.out.println("limit = "+limits[i]+" format = '"+formats[i]+"' <-- "+i);
      }
      cf  = new ChoiceFormat(limits, (String[])formats);
      formats = cf.getFormats();
      limits = cf.getLimits();
      System.out.println("lengths "+ formats.length +" and "+ limits.length);
      for(int i = 0 ; i < formats.length ; i++){
        System.out.println("limit = "+limits[i]+" format = '"+formats[i]+"' <-- "+i);
      }      th.check(cf.toPattern(), pattern);
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
}
