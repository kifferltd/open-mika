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


package gnu.testlet.wonka.math.BigDecimal;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.math.*;


public class AcuniaBigDecimalTest implements Testlet {
  protected TestHarness th;

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.math.BigDecimal");
    test_BigDecimal();
    test_abs();
    test_add();
    test_divide();
    test_max();
    test_min();
    test_movePointLeft();
    test_movePointRight();
    test_multiply();
    test_negate();
    test_signum();
    test_subtract();
    test_equals();
    test_compareTo();
    test_doubleValue();
    test_floatValue();
    test_intValue();
    test_longValue();
    test_toBigInteger();
    test_valueOf();
    test_scale();
    test_setScale();
    test_hashCode();
    test_toString();
  }


/**
** implemented. <br>
** All these test should pass. If not they could cause fails and unexpected behaviour in other tests ...
*/
  public void test_BigDecimal(){

    th.check(BigDecimal.ROUND_UP          ,0, "checking class constant ROUND_UP");
    th.check(BigDecimal.ROUND_DOWN        ,1, "checking class constant ROUND_DOWN");
    th.check(BigDecimal.ROUND_CEILING     ,2, "checking class constant ROUND_CEILING");
    th.check(BigDecimal.ROUND_FLOOR       ,3, "checking class constant ROUND_FLOOR");
    th.check(BigDecimal.ROUND_HALF_UP     ,4, "checking class constant ROUND_HALF_UP");
    th.check(BigDecimal.ROUND_HALF_DOWN   ,5, "checking class constant ROUND_HALF_DOWN");
    th.check(BigDecimal.ROUND_HALF_EVEN   ,6, "checking class constant ROUND_HALF_EVEN");
    th.check(BigDecimal.ROUND_UNNECESSARY ,7, "checking class constant ROUND_UNNECESSARY");

    th.checkPoint("BigDecimal(java.math.BigInteger)");
    checkCreateBigDecimal("1",-1);
    checkCreateBigDecimal("123456",-1);
    checkCreateBigDecimal("-1",-1);
    checkCreateBigDecimal("123345565434",-1);
    checkCreateBigDecimal("198574562452467548659846522481940205802587206725082759257234078507408715308141071047814017401",-1);

    th.checkPoint("BigDecimal(java.math.BigInteger,int)");
    checkCreateBigDecimal(0,"1");
    checkCreateBigDecimal(3,"123456");
    checkCreateBigDecimal(3,"-1234");
    checkCreateBigDecimal(8,"11355666788789879865");
    checkCreateBigDecimal(0,"123456");
    checkCreateBigDecimal(3,"-12388568576546764764785858578547857854");

    th.checkPoint("BigDecimal(java.lang.String)");
    th.check(new BigDecimal(".123").toString(), "0.123", "constructing '.123'");
    th.check(new BigDecimal(".0").toString(), "0.0", "constructing '.0'");
    checkCreateBigDecimal("1",0);
    checkCreateBigDecimal("1234.56",2);
    checkCreateBigDecimal("-1.23",2);
    checkCreateBigDecimal("123345565434",0);
    checkCreateBigDecimal("-123345565434",0);
    checkCreateBigDecimal("198574562452467548659846522481940205.802587206725082759257234078507",30);
    try {
      new BigDecimal(" 123.456");
      th.fail("should throw a NumberFormatException -- 1 "+new BigDecimal(" 123.456"));

    }
    catch(NumberFormatException nfe){
      th.check(true);
    }
    try {
      new BigDecimal("123.456 ");
      th.fail("should throw a NumberFormatException -- 2 "+new BigDecimal("123.456 "));

    }
    catch(NumberFormatException nfe){
      th.check(true);
    }

    try {
      new BigDecimal(".");
      th.fail("should throw a NumberFormatException -- 3");

    }
    catch(NumberFormatException nfe){
      th.check(true);
    }

    th.checkPoint("BigDecimal(double)");
    checkCreateBigDecimal(1.0,false);
    checkCreateBigDecimal(1.25E308,false);
    checkCreateBigDecimal(1.23456E123,false);
    checkCreateBigDecimal(1.234567891123456,true);
    checkCreateBigDecimal(999999.5, true);
    checkCreateBigDecimal(12345.0,false);
    checkCreateBigDecimal(1.2334545E234,false);
    checkCreateBigDecimal(4.5E-122,false);
    checkCreateBigDecimal(5.0E-287,false);
    checkCreateBigDecimal(3.0E-308,false);
    try {
      new BigDecimal(Double.NaN);
      th.fail("should throw a NumberFormatException -- 1");

    }
    catch(NumberFormatException nfe){
      th.check(true);
    }
    try {
      new BigDecimal(Double.POSITIVE_INFINITY);
      th.fail("should throw a NumberFormatException -- 2");

    }
    catch(NumberFormatException nfe){
      th.check(true);
    }
    try {
      new BigDecimal(Double.NEGATIVE_INFINITY);
      th.fail("should throw a NumberFormatException -- 3");
    }
    catch(NumberFormatException nfe){
      th.check(true);
    }

  }

  private void checkCreateBigDecimal(String value, int scale){
    if(scale < 0){
      BigDecimal bd = new BigDecimal(new BigInteger(value));
      th.check(bd.toString(), value, "constructing BigDecimal with a BigInteger '"+value+"'");
      th.check(bd.scale(), 0, "checking scale BigDecimal('"+value+"')");
    }
    else {
      BigDecimal bd = new BigDecimal(value);
      th.check(bd.toString(), value, "constructing BigDecimal with a String '"+value+"'");
      th.check(bd.scale(), scale, "checking scale BigDecimal('"+value+"')");
    }
  }

  private void checkCreateBigDecimal(double value, boolean checkString){
    BigDecimal bd = new BigDecimal(value);
    if(checkString){
      th.check(bd.toString(), String.valueOf(value), "checking constructor BigDecimal(double "+value+") -- toString");
    }
    th.check(bd.doubleValue(), value, "checking constructor BigDecimal(double "+value+") -- doubleValue");

    value = -value;
    bd = new BigDecimal(value);
    if(checkString){
      th.check(bd.toString(), String.valueOf(value), "checking constructor BigDecimal(double "+value+") -- toString");
    }
    th.check(bd.doubleValue(), value, "checking constructor BigDecimal(double "+value+") -- doubleValue");
  }

  private void checkCreateBigDecimal(int scale, String value){
    BigDecimal bd = new BigDecimal(new BigInteger(value), scale);
    if(scale != 0){
      int dot = value.length() - scale;
      value = value.substring(0,dot)+"."+value.substring(dot);
    }
    th.check(bd.toString(), value, "constructing BigDecimal with a BigInteger '"+value+"' and scale "+scale);
    th.check(bd.scale(), scale, "checking scale BigDecimal('"+value+"')");
  }

/**
* implemented. <br>
*
*/
  public void test_abs(){
    th.checkPoint("abs()java.math.BigDecimal");
    BigDecimal bd = new BigDecimal("0");
    th.check(bd.abs(), bd);
    bd = new BigDecimal("0.00008");
    th.check(bd.abs(), bd);
    th.check(bd.negate().abs(), bd);
    bd = new BigDecimal("676465534.5578678967345");
    th.check(bd.abs(), bd);
    th.check(bd.negate().abs(), bd);
    bd = new BigDecimal("676465534989900788768767687678676678768756865754");
    th.check(bd.abs(), bd);
    th.check(bd.negate().abs(), bd);
  }

/**
* implemented. <br>
*
*/
  public void test_add(){
    th.checkPoint("add(java.math.BigDecimal)java.math.BigDecimal");
    checkAdd("4.5",  "3.0");
    checkAdd("1234.5",  "3.5");
    checkAdd("0.005",  "123.5");
    checkAdd("1234567.0",  "3.5");
    checkAdd("1234.5",  "35.125");
    BigDecimal bda = new BigDecimal("987654321.123456789");
    BigDecimal bdb = new BigDecimal("-54321.12345");
    th.check(bda.add(bdb).toString(), "987600000.000006789","check add large -- 1");
    bda = new BigDecimal("987654321123456789987654321");
    bdb = new BigDecimal("0.00000123456789");
    th.check(bda.add(bdb).toString(), "987654321123456789987654321.00000123456789","check add large -- 2");
    bda = new BigDecimal("-987654321.123456789");
    bdb = new BigDecimal("54321.12345");
    th.check(bda.add(bdb).toString(), "-987600000.000006789","check add large -- 3");
  }

  public void checkAdd(String sa, String sb){
    BigDecimal bda = new BigDecimal(sa);
    BigDecimal bdb = new BigDecimal(sb);
    double a = Double.parseDouble(sa);
    double b = Double.parseDouble(sb);
    th.check(Double.parseDouble(bda.add(bdb).toString()), (a+b),"check add "+a+" '+' "+b);
    th.check(Double.parseDouble(bda.negate().add(bdb).toString()), (-a+b),"check add "+(-a)+" '+' "+b);
    th.check(Double.parseDouble(bda.negate().add(bdb.negate()).toString()), (-a-b),"check add "+(-a)+" '+' "+(-b));
    th.check(Double.parseDouble(bda.add(bdb.negate()).toString()), (a-b),"check add "+a+" '+' "+(-b));
    th.check(Double.parseDouble(bdb.add(bda).toString()), (b+a),"check add "+b+" '+' "+a);
    th.check(Double.parseDouble(bdb.negate().add(bda).toString()), (-b+a),"check add "+(-b)+" '+' "+a);
    th.check(Double.parseDouble(bdb.negate().add(bda.negate()).toString()), (-b-a),"check add "+(-b)+" '+' "+(-a));
    th.check(Double.parseDouble(bdb.add(bda.negate()).toString()), (b-a),"check add "+b+" '+' "+(-a));
  }

/**
* implemented. <br>
*
*/
  public void test_divide(){
    th.checkPoint("divide(java.math.BigDecimal,int)java.math.BigDecimal");
    checkdivide(123.50, 1.0, -1, BigDecimal.ROUND_UNNECESSARY);
    checkdivide(123.50, 10.0, -1, BigDecimal.ROUND_DOWN);
    checkdivide(12345.5, 12345.5, -1, BigDecimal.ROUND_DOWN);
    checkdivide(12.5, 1.5, -1, BigDecimal.ROUND_DOWN);
    checkdivide(12345.0, 123456.0, -1, BigDecimal.ROUND_DOWN);
    checkdivide(12.0, 36.0, -1, BigDecimal.ROUND_UP);
    checkdivide(12345.5, 150000000.0, -1, BigDecimal.ROUND_UP);

    BigDecimal bd = new BigDecimal("123456.789");
    try {
      bd.divide(bd,-1);
      th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }
    try {
      bd.divide(bd,8);
      th.fail("should throw an IllegalArgumentException -- 2");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }

    try {
      bd.divide(new BigDecimal(0.0),1);
      th.fail("should throw an ArithmeticException -- 1");
    }
    catch(ArithmeticException ae){
      th.check(true);
    }
    try {
      bd.divide(new BigDecimal(123E10),BigDecimal.ROUND_UNNECESSARY);
      th.fail("should throw an ArithmeticException -- 2");
    }
    catch(ArithmeticException ae){
      th.check(true);
    }

    th.checkPoint("divide(java.math.BigDecimal,int,int)java.math.BigDecimal");

    try {
      bd.divide(bd,0,-1);
      th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }
    try {
      bd.divide(bd,1,8);
      th.fail("should throw an IllegalArgumentException -- 2");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }

    try {
      bd.divide(new BigDecimal(0.0),2,1);
      th.fail("should throw an ArithmeticException -- 1");
    }
    catch(ArithmeticException ae){
      th.check(true);
    }
    try {
      bd.divide(new BigDecimal(123E10),3,BigDecimal.ROUND_UNNECESSARY);
      th.fail("should throw an ArithmeticException -- 2");
    }
    catch(ArithmeticException ae){
      th.check(true);
    }
    try {
      bd.divide(bd,-1,4);
      th.fail("should throw an ArithmeticException -- 3");
    }
    catch(ArithmeticException iae){
      th.check(true);
    }
    checkdivide(123.50, 1.0, 7, BigDecimal.ROUND_UNNECESSARY);
    checkdivide(123.50, 10.0, 1, BigDecimal.ROUND_DOWN);
    checkdivide(12345.5, 12345.5, 5, BigDecimal.ROUND_DOWN);
    checkdivide(12.5, 1.5, -1, BigDecimal.ROUND_DOWN);
    checkdivide(12345.0, 123456.0, 0, BigDecimal.ROUND_DOWN);
    checkdivide(12.0, 36.0, 10, BigDecimal.ROUND_UP);

    checkdivide(4.5, 1.0, 0, BigDecimal.ROUND_HALF_UP);
    checkdivide(4.5, 1.0, 0, BigDecimal.ROUND_HALF_DOWN);
    checkdivide(4.5, 1.0, 0, BigDecimal.ROUND_HALF_EVEN);
    checkdivide(5.5, 1.0, 0, BigDecimal.ROUND_HALF_EVEN);

    checkdivide(123.1, 123.0, 0, BigDecimal.ROUND_FLOOR);
    checkdivide(123.1, 123.0, 0, BigDecimal.ROUND_CEILING);
    checkdivide(0.5, 1.0, 1, BigDecimal.ROUND_FLOOR);
    checkdivide(0.5, 1.0, 1, BigDecimal.ROUND_CEILING);
  }

  public void checkdivide(double a, double b, int scale, int mode){
    BigDecimal bdA = new BigDecimal(a);
    BigDecimal bdB = new BigDecimal(b);
    BigDecimal expected = new BigDecimal(a/b);
    if(scale < 0){
      scale = bdA.scale();
      th.check(bdA.divide(bdB,mode), expected.setScale(scale, mode), "dividing "+a+" / "+b);
      th.check(bdA.divide(bdB.negate(),mode), expected.negate().setScale(scale, mode),  "dividing "+a+" / -"+b);
      th.check(bdA.negate().divide(bdB.negate(),mode), expected.setScale(scale, mode), "dividing "+a+" / "+b);
      th.check(bdA.negate().divide(bdB,mode), expected.negate().setScale(scale, mode),  "dividing "+a+" / -"+b);
    }
    else {
      th.check(bdA.divide(bdB, scale, mode), expected.setScale(scale, mode), "dividing "+a+" / "+b);
      th.check(bdA.divide(bdB.negate(), scale, mode), expected.negate().setScale(scale, mode),  "dividing "+a+" / -"+b);
      th.check(bdA.negate().divide(bdB.negate(), scale, mode), expected.setScale(scale, mode), "dividing "+a+" / "+b);
      th.check(bdA.negate().divide(bdB, scale, mode), expected.negate().setScale(scale, mode),  "dividing "+a+" / -"+b);
    }
  }

/**
* implemented. <br>
*
*/
  public void test_max(){
    th.checkPoint("max(java.math.BigDecimal)java.math.BigDecimal");
    BigDecimal bd = new BigDecimal("12345");
    BigDecimal val = new BigDecimal("12345");
    th.check(bd.max(val) == val,"checking max -- 1");
    val = new BigDecimal("12345.5");
    th.check(bd.max(val) == val,"checking max -- 2");
    val = new BigDecimal("12344.5");
    th.check(bd.max(val) == bd,"checking max -- 3");

  }

/**
* implemented. <br>
*
*/
  public void test_min(){
    th.checkPoint("min(java.math.BigDecimal)java.math.BigDecimal");
    BigDecimal bd = new BigDecimal("12345");
    BigDecimal val = new BigDecimal("12345");
    th.check(bd.min(val) == val,"checking min -- 1");
    val = new BigDecimal("12345.5");
    th.check(bd.min(val) == bd,"checking min -- 2");
    val = new BigDecimal("12344.5");
    th.check(bd.min(val) == val,"checking min -- 3");

  }

/**
* implemented. <br>
*
*/
  public void test_movePointLeft(){
    th.checkPoint("movePointLeft(int)java.math.BigDecimal");
    checkMoveLeft("12345", 2, "123.45");
    checkMoveLeft("12345", 0, "12345");
    checkMoveLeft("123.45", 2, "1.2345");
    checkMoveLeft("12345", 10, "0.0000012345");
    checkMoveLeft("1.2345", -2, "123.45");
    checkMoveLeft("123.45", 0, "123.45");
    checkMoveLeft("123.45", -3, "123450");
  }

  public void checkMoveLeft(String val, int n ,String moved){
    th.check(new BigDecimal(val).movePointLeft(n).toString(), moved, "moving in '"+val+"' point "+n+" places to the Left");
    th.check(new BigDecimal("-"+val).movePointLeft(n).toString(), "-"+moved, "moving in '-"+val+"' point "+n+" places to the Left");
  }

/**
* implemented. <br>
*
*/
  public void test_movePointRight(){
    th.checkPoint("movePointRight(int)java.math.BigDecimal");
    checkMoveRight("123.45", 2, "12345");
    checkMoveRight("12345", 0, "12345");
    checkMoveRight("1.2345", 2, "123.45");
    checkMoveRight("1.2345", 10, "12345000000");
    checkMoveRight("123.45", -2, "1.2345");
    checkMoveRight("123.45", 0, "123.45");
    checkMoveRight("123.45", -3, "0.12345");
  }

  public void checkMoveRight(String val, int n ,String moved){
    th.check(new BigDecimal(val).movePointRight(n).toString(), moved, "moving in '"+val+"' point "+n+" places to the Right");
    th.check(new BigDecimal("-"+val).movePointRight(n).toString(), "-"+moved, "moving in '-"+val+"' point "+n+" places to the Right");
  }


/**
* implemented. <br>
*
*/
  public void test_multiply(){
    th.checkPoint("multiply(java.math.BigInteger)java.math.BigInteger");
    checkMultiply(1,0,12,0);
    checkMultiply(0,0,12,0);
    checkMultiply(123456,0,129754323,0);
    checkMultiply(11233,0,1290882367,7);
    checkMultiply(114323343,6,1290882367,9);
    checkMultiply(51233,2,1290882231367L,30);
    checkMultiply(31233,9,1290882367433L,30);
    checkMultiply(11233,0,1290882367000L,3);

  }

  public void checkMultiply(long a, int scaleA, long b, int scaleB){
    BigDecimal bdA = BigDecimal.valueOf(a, scaleA);
    BigDecimal bdB = BigDecimal.valueOf(b, scaleB);
    BigDecimal result = BigDecimal.valueOf(a*b, scaleB+scaleA);
    th.check(bdA.multiply(bdB), result, "multiplying "+a+" * "+b);
    th.check(bdA.multiply(bdB.negate()), result.negate(),  "multiplying "+a+" * -"+b);
    th.check(bdB.multiply(bdA.negate()), result.negate(),  "multiplying "+b+" * -"+a);
    th.check(bdB.negate().multiply(bdA.negate()), result, "multiplying -"+b+" * -"+a);
  }

/**
* implemented. <br>
*
*/
  public void test_negate(){
    th.checkPoint("negate()java.math.BigDecimal");
    BigDecimal bd = new BigDecimal("0");
    bd = new BigDecimal("12234.454567");
    th.check(bd.negate().toString(), "-12234.454567", "negating '12234.454567'");
    bd = new BigDecimal("-0.000454567");
    th.check(bd.negate().toString(), "0.000454567", "negating '-0.000454567'");
  }

/**
* implemented. <br>
*
*/
  public void test_signum(){
    th.checkPoint("signum()int");
    BigDecimal bd = new BigDecimal("0");
    th.check(bd.signum(), 0);
    bd = new BigDecimal("0.00000");
    th.check(bd.signum(), 0);
    bd = new BigDecimal(".0");
    th.check(bd.signum(), 0);
    bd = new BigDecimal("122343");
    th.check(bd.signum(), 1);
    bd = new BigDecimal("-123.455");
    th.check(bd.signum(), -1);
  }

/**
* implemented. <br>
*
*/
  public void test_subtract(){
    th.checkPoint("subtract(java.math.BigDecimal)java.math.BigDecimal");
    checkSubtract("4.5",  "3.0");
    checkSubtract("1234.5",  "3.5");
    checkSubtract("0.005",  "123.5");
    checkSubtract("1234567.0",  "3.5");
    checkSubtract("1234.5",  "35.125");
    BigDecimal bda = new BigDecimal("987654321.123456789");
    BigDecimal bdb = new BigDecimal("54321.12345");
    th.check(bda.subtract(bdb).toString(), "987600000.000006789","check subtract large -- 1");
    bda = new BigDecimal("987654321123456789987654321");
    bdb = new BigDecimal("-0.00000123456789");
    th.check(bda.subtract(bdb).toString(), "987654321123456789987654321.00000123456789","check subtract large -- 2");
    bda = new BigDecimal("-987654321.123456789");
    bdb = new BigDecimal("-54321.12345");
    th.check(bda.subtract(bdb).toString(), "-987600000.000006789","check subtract large -- 3");
  }

  public void checkSubtract(String sa, String sb){
    BigDecimal bda = new BigDecimal(sa);
    BigDecimal bdb = new BigDecimal(sb);
    double a = Double.parseDouble(sa);
    double b = Double.parseDouble(sb);
    th.check(Double.parseDouble(bda.subtract(bdb).toString()), (a-b),"check subtract "+a+" '-' "+b);
    th.check(Double.parseDouble(bda.negate().subtract(bdb).toString()), (-a-b),"check subtract "+(-a)+" '-' "+b);
    th.check(Double.parseDouble(bda.negate().subtract(bdb.negate()).toString()), (-a+b),"check subtract "+(-a)+" '-' "+(-b));
    th.check(Double.parseDouble(bda.subtract(bdb.negate()).toString()), (a+b),"check subtract "+a+" '-' "+(-b));
    th.check(Double.parseDouble(bdb.subtract(bda).toString()), (b-a),"check subtract "+b+" '-' "+a);
    th.check(Double.parseDouble(bdb.negate().subtract(bda).toString()), (-b-a),"check subtract "+(-b)+" '-' "+a);
    th.check(Double.parseDouble(bdb.negate().subtract(bda.negate()).toString()), (-b+a),"check subtract "+(-b)+" '-' "+(-a));
    th.check(Double.parseDouble(bdb.subtract(bda.negate()).toString()), (b+a),"check subtract "+b+" '-' "+(-a));
  }

/**
* implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
    BigDecimal bd = new BigDecimal("123456789.00");
    th.check(!bd.equals(null), "checking equals -- 'null'");
    th.check(!bd.equals("123456789.00"), "checking equals -- 'string'");
    th.check(bd.equals(new BigDecimal("123456789.00")), "checking equals -- 'true'");
    th.check(bd.equals(bd), "checking equals -- 'itself'");
    th.check(!bd.equals(new BigDecimal("123456789.0")), "checking equals -- 'same value diff precision 1'");
    th.check(!bd.equals(new BigDecimal("123456789.000")), "checking equals -- 'same value diff precision 2'");
  }

/**
* implemented. <br>
*
*/
  public void test_compareTo(){
    th.checkPoint("compareTo(java.lang.Object)int");
    BigDecimal bd = new BigDecimal("123456789.00");
    try {
      bd.compareTo(null);
      th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe){
      th.check(true);
    }
    try {
      bd.compareTo("abcd");
      th.fail("should throw a ClassCastException");
    }
    catch(ClassCastException npe){
      th.check(true);
    }
    Object o = bd;
    th.check(bd.compareTo(o), 0,"comparingTo itself");

    th.checkPoint("compareTo(java.math.BigDecimal)int");
    th.check(bd.compareTo(new BigDecimal("123456789.0")), 0, "same value -- 1");
    th.check(bd.compareTo(new BigDecimal("123456789.000")), 0, "same value -- 2");
    th.check(bd.compareTo(new BigDecimal("123456788.98")), 1, "value -- 1");
    th.check(bd.compareTo(new BigDecimal("123456789.001")), -1, "value -- 2");
    BigDecimal zero = new BigDecimal("0");
    th.check(bd.compareTo(zero), 1, "value -- 3");
    th.check(bd.negate().compareTo(zero), -1, "value -- 4");
    th.check(zero.compareTo(bd), -1, "value -- 5");
    th.check(zero.compareTo(bd.negate()), 1, "value -- 6");
    BigDecimal ten = new BigDecimal("10");
    bd = new BigDecimal("9.8765432211123456");
    th.check(bd.compareTo(ten), -1, "value -- 7");
    th.check(bd.negate().compareTo(ten), -1, "value -- 8");
    bd = new BigDecimal("10.8765432211123456");
    th.check(bd.compareTo(ten), 1, "value -- 9");
    th.check(bd.negate().compareTo(ten), -1, "value -- 10");
  }

/**
* implemented. <br>
*
*/
  public void test_doubleValue(){
    th.checkPoint("doubleValue()double");
    check_doubleValue("4");
    check_doubleValue("40");
    check_doubleValue("400");
    check_doubleValue("412");
    check_doubleValue("4345");
    check_doubleValue("98765432198765433");
    check_doubleValue("9876543219876543000111000", 9.876543219876543E24);
    check_doubleValue("129876543219876543000111000", 1.2987654321987654E26);
    BigDecimal tobig = new BigDecimal(new BigInteger("10").pow(309));
    th.check(tobig.doubleValue(), Double.POSITIVE_INFINITY, "checking large numbers");
    th.check(tobig.negate().doubleValue(), Double.NEGATIVE_INFINITY, "checking large numbers");
    th.check(new BigDecimal(BigInteger.ONE, 340).doubleValue(), 0.0, "checking to small number");
    th.check(new BigDecimal(BigInteger.ONE, 250).doubleValue(), 1.0E-250, "checking small number");
  }

  private void check_doubleValue(String value){
    th.check(new BigDecimal(value).doubleValue(), Double.parseDouble(value), "checking parseDouble of '"+value+"'");
    value = "-"+value;
    th.check(new BigDecimal(value).doubleValue(), Double.parseDouble(value), "checking parseDouble of '"+value+"'");
  }

  private void check_doubleValue(String value, double result){
    th.check(new BigDecimal(value).doubleValue(), result, "checking parseDouble of '"+value+"'");
    value = "-"+value;
    th.check(new BigDecimal(value).doubleValue(), -result, "checking parseDouble of '"+value+"'");
  }

/**
* implemented. <br>
*
*/
  public void test_floatValue(){
    th.checkPoint("floatValue()float");
    check_floatValue("4");
    check_floatValue("40");
    check_floatValue("400");
    check_floatValue("412");
    check_floatValue("4345");
    check_floatValue("98765432198765433");
    check_floatValue("9876543219876543000111000", 9.876543E24F);
    check_floatValue("129876543219876543000111000", 1.2987654293035705E26F);
    BigDecimal tobig = new BigDecimal(new BigInteger("10").pow(309));
    th.check(tobig.floatValue(), Float.POSITIVE_INFINITY, "checking large numbers");
    th.check(tobig.negate().floatValue(), Float.NEGATIVE_INFINITY, "checking large numbers");
    th.check(new BigDecimal(BigInteger.ONE, 340).floatValue(), 0.0F, "checking to small number");
    th.check(new BigDecimal(BigInteger.ONE, 40).floatValue(), 1.0E-40F, "checking small number");
  }

  private void check_floatValue(String value){
    th.check(new BigDecimal(value).floatValue(), Float.parseFloat(value), "checking parseFloat of '"+value+"'");
    value = "-"+value;
    th.check(new BigDecimal(value).floatValue(), Float.parseFloat(value), "checking parseFloat of '"+value+"'");
  }

  private void check_floatValue(String value, float result){
    th.check(new BigDecimal(value).floatValue(), result, "checking parseFloat of '"+value+"'");
    value = "-"+value;
    th.check(new BigDecimal(value).floatValue(), -result, "checking parseFloat of '"+value+"'");
  }

/**
* implemented. <br>
*
*/
  public void test_intValue(){
    th.checkPoint("intValue()int");
    th.check(new BigDecimal("123.456789").intValue(), 123, "checking to intValue -- 1");
    th.check(new BigDecimal("-123.456789").intValue(), -123, "checking to intValue -- 2");
    th.check(new BigDecimal(".456789").intValue(), 0, "checking to intValue -- 3");
    th.check(new BigDecimal("12345678998765432.456789").intValue(), 1664843640, "checking to intValue -- 4");
    th.check(new BigDecimal("1234567899876543").intValue(), 1025477823, "checking to intValue -- 5");
    th.check(new BigDecimal("1.99999999999").intValue(), 1, "checking to intValue -- 6");
    th.check(new BigDecimal("-12345678998765432.456789").intValue(), -1664843640, "checking to intValue -- 7");
    th.check(new BigDecimal("-1.99999999999").intValue(), -1, "checking to intValue -- 8");
    th.check(new BigDecimal("456789").intValue(), 456789, "checking to intValue -- 9");
    th.check(new BigDecimal("-19999").intValue(), -19999, "checking to intValue -- 10");
    th.check(new BigDecimal("123456789987654324567899787892374823478234798").intValue(), -1556607314, "checking to intValue -- 11");
    th.check(new BigDecimal("-123456789987654324567899787892374823478234798").intValue(), 1556607314, "checking to intValue -- 12");
  }

/**
*  implemented. <br>
*
*/
  public void test_longValue(){
    th.checkPoint("longValue()long");
    th.check(new BigDecimal("12334.456789").longValue(), 12334, "checking to longValue -- 1");
    th.check(new BigDecimal("-12334.456789").longValue(), -12334, "checking to longValue -- 2");
    th.check(new BigDecimal(".456789").longValue(), 0, "checking to longValue -- 3");
    th.check(new BigDecimal("12345678998765432.456789").longValue(), 12345678998765432L, "checking to longValue -- 4");
    th.check(new BigDecimal("1234567899876543").longValue(), 1234567899876543L, "checking to longValue -- 5");
    th.check(new BigDecimal("1.99999999999").longValue(), 1, "checking to longValue -- 6");
    th.check(new BigDecimal("-12345678998765432.456789").longValue(), -12345678998765432L, "checking to longValue -- 7");
    th.check(new BigDecimal("-1.99999999999").longValue(), -1, "checking to longValue -- 8");
    th.check(new BigDecimal("456789").longValue(), 456789, "checking to longValue -- 9");
    th.check(new BigDecimal("-19999").longValue(), -19999, "checking to longValue -- 10");
    th.check(new BigDecimal("123456789987654324567899787892374823478234798").longValue(),
      -9020399958540939602L, "checking to longValue -- 11");
    th.check(new BigDecimal("-123456789987654324567899787892374823478234798").longValue(),
      9020399958540939602L, "checking to longValue -- 12");
  }

/**
* implemented. <br>
*
*/
  public void test_toBigInteger(){
    th.checkPoint("toBigInteger()java.math.BigInteger");
    th.check(new BigDecimal("12334.456789").toBigInteger(), new BigInteger("12334"), "checking to toBigInteger -- 1");
    th.check(new BigDecimal("-12334.456789").toBigInteger(), new BigInteger("-12334"), "checking to toBigInteger -- 2");
    th.check(new BigDecimal(".456789").toBigInteger(), new BigInteger("0"), "checking to toBigInteger -- 3");
    th.check(new BigDecimal("12345678998765432.456789").toBigInteger(), new BigInteger("12345678998765432"),
      "checking to toBigInteger -- 4");
    th.check(new BigDecimal("1234567899876543").toBigInteger(), new BigInteger("1234567899876543"),
      "checking to toBigInteger -- 5");
    th.check(new BigDecimal("1.99999999999").toBigInteger(), new BigInteger("1"), "checking to toBigInteger -- 6");
    th.check(new BigDecimal("-12345678998765432.456789").toBigInteger(), new BigInteger("-12345678998765432"),
      "checking to toBigInteger -- 7");
    th.check(new BigDecimal("-1.99999999999").toBigInteger(), new BigInteger("-1"), "checking to toBigInteger -- 8");
    th.check(new BigDecimal("456789").toBigInteger(), new BigInteger("456789"), "checking to toBigInteger -- 9");
    th.check(new BigDecimal("-19999").toBigInteger(), new BigInteger("-19999"), "checking to toBigInteger -- 10");
    th.check(new BigDecimal("123456789987654324567899787892374823478234798").toBigInteger(),
      new BigInteger("123456789987654324567899787892374823478234798"), "checking to toBigInteger -- 11");
    th.check(new BigDecimal("-123456789987654324567899787892374823478234798").toBigInteger(),
      new BigInteger("-123456789987654324567899787892374823478234798"), "checking to toBigInteger -- 12");
  }

/**
* implemented. <br>
*
*/
  public void test_valueOf(){
    th.checkPoint("valueOf(long)java.math.BigDecimal");
    checkValueOf(12345677898753445L,-1);
    checkValueOf(-12345677898753445L,-1);
    checkValueOf(123456778,-1);
    checkValueOf(-12345677,-1);

    th.checkPoint("valueOf(long,int)java.math.BigDecimal");
    checkValueOf(812345677898753445L,10);
    checkValueOf(128345677898753445L,0);
    checkValueOf(8128345677898753445L,354);
    checkValueOf(-128345677898753445L,1111223);
    checkValueOf(128345677898753445L,11);
    try {
      BigDecimal.valueOf(12L,-1);
      th.fail("should throw a NumberFormatException");
    }
    catch(NumberFormatException nfe){
      th.check(true);
    }
  }

  private void checkValueOf(long value, int scale){
    if(scale < 0){
      BigDecimal bd = BigDecimal.valueOf(value);
      th.check(bd.toString(), ""+value, "valueOf '"+value+"'");
      th.check(bd.scale(), 0, "checking scale BigDecimal('"+value+"')");
    }
    else {
      BigDecimal bd = BigDecimal.valueOf(value,scale);
      th.check(bd, new BigDecimal(new BigInteger(""+value),scale), "valueOf '"+value+"','"+scale+"'");
      th.check(bd.scale(), scale, "checking scale BigDecimal('"+value+"')");
    }
  }



/**
* implemented. <br>
*
*/
  public void test_scale(){
    th.checkPoint("scale()int");
    BigDecimal bd = new BigDecimal("123456789.1234567890");
    th.check(bd.scale(),10, "checking scale -- 1");
    bd = new BigDecimal("1234567891234567890");
    th.check(bd.scale(),0, "checking scale -- 2");
    bd = new BigDecimal(12.5);
    th.check(bd.scale(),1, "checking scale -- 3");
    bd = new BigDecimal(12D);
    th.check(bd.scale(),0, "checking scale -- 4");
  }

/**
* implemented. <br>
*
*/
  public void test_setScale(){
    th.checkPoint("setScale(int)java.math.BigDecimal");
    BigDecimal bd = new BigDecimal("12345");
    BigDecimal result = bd.setScale(3);
    th.check(result.toString(), "12345.000","checking - value -- 1");
    th.check(result.scale(),3, "checking - scale -- 1");
    result = result.setScale(0);
    th.check(result, bd, "checking - value -- 2");
    th.check(result.scale(), 0, "checking - scale -- 2");
    bd = new BigDecimal("-12345");
    result = bd.setScale(3);
    th.check(result.toString(), "-12345.000","checking - value -- 3");
    th.check(result.scale(), 3, "checking - scale -- 3");
    try {
      bd.setScale(-1);
      th.fail("should throw an ArethimeticException -- 1");
    }
    catch(ArithmeticException ae){
      th.check(true, "got exception :)");
    }
    bd = new BigDecimal("12.345");
    try {
      bd.setScale(1);
      th.fail("should throw an ArethimeticException -- 2");
    }
    catch(ArithmeticException ae){
      th.check(true, "got exception :)");
    }

    th.checkPoint("setScale(int,int)java.math.BigDecimal");
    try {
      bd.setScale(1,BigDecimal.ROUND_UNNECESSARY);
      th.fail("should throw an ArethimeticException -- 1");
    }
    catch(ArithmeticException ae){
      th.check(true, "got exception :)");
    }
    bd = new BigDecimal("12345");
    try {
      bd.setScale(-1,BigDecimal.ROUND_UNNECESSARY);
      th.fail("should throw an ArethimeticException -- 2");
    }
    catch(ArithmeticException ae){
      th.check(true, "got exception :)");
    }

    bd = new BigDecimal("123.45");
    try {
      bd.setScale(1,-1);
      th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException ae){
      th.check(true, "got exception :)");
    }
    try {
      bd.setScale(1,8);
      th.fail("should throw an IllegalArgumentException -- 2");
    }
    catch(IllegalArgumentException ae){
      th.check(true, "got exception :)");
    }
    checkScaling("1234567890.1234567890", 10, false, false, false);
    checkScaling("1234567890.89234567890", 10, true, true, true);
    checkScaling("1234567890.1234567881", 1, false, false, false);
    checkScaling("1234567890.12345000", 4, true, false, false);
    checkScaling("1234567890.12355000", 4, true, true, false);
  }

  private void checkScaling(String value, int diff, boolean rhUp, boolean rhEv, boolean rhDw){
    BigDecimal bd = new BigDecimal(value);
    int scale = bd.scale();
    String result = value;
    for(int k=0 ; k < diff ; k++){
      result = result + "0";
    }

    int newScale = scale+diff;

    for (int mode=0 ; mode <= BigDecimal.ROUND_UNNECESSARY ; mode++){
      BigDecimal scaled = bd.setScale(newScale, mode);
      th.check(scaled.toString(), result, "scaling '"+value+"' to scale "+newScale+" valueCheck");
      th.check(scaled.scale(), newScale, "scaling '"+value+"' to scale "+newScale+" scaleCheck");
    }

    newScale = scale-diff;
    result = value.substring(0,value.length()-diff);
    if(result.endsWith(".")){
      result = result.substring(0,result.length()-1);
    }

    int last = result.length()-1;
    char lastChar = (char)(result.charAt(last)+1);
    String upResult = result.substring(0,last) + lastChar;

    String[] results = new String[BigDecimal.ROUND_UNNECESSARY];
    results[BigDecimal.ROUND_CEILING] = bd.signum() == -1 ? result : upResult;
    results[BigDecimal.ROUND_DOWN] = result;
    results[BigDecimal.ROUND_FLOOR] = bd.signum() == 1 ? result : upResult;
    results[BigDecimal.ROUND_HALF_DOWN] = rhDw ? upResult : result;
    results[BigDecimal.ROUND_HALF_EVEN] = rhEv ? upResult : result;
    results[BigDecimal.ROUND_HALF_UP] = rhUp ? upResult : result;
    results[BigDecimal.ROUND_UP] = upResult;

    for (int loop=0 ; loop < BigDecimal.ROUND_UNNECESSARY ;loop++){
      BigDecimal scaled = bd.setScale(newScale, loop);
      th.check(scaled.toString(), results[loop], "scaling '"+value+"' to scale "+newScale+" valueCheck -- mode = "+loop);
      th.check(scaled.scale(), newScale, "scaling '"+value+"' to scale "+newScale+" scaleCheck -- mode = "+loop);
    }
  }

/**
* implemented. <br>
*
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    BigDecimal bd = new BigDecimal("12345.678900");
    th.check(bd.hashCode(), new BigDecimal("12345.678900").hashCode(), "checking equality of hashCode -- 1");
    th.check(bd.hashCode(), bd.hashCode(), "checking equality of hashCode -- 2");
    th.check(bd.hashCode() != new BigDecimal("12345.67890").hashCode(),"difference of hashCode -- 1");
    th.check(bd.hashCode() != new BigDecimal("12345.6789000").hashCode(),"difference of hashCode -- 2");
  }

/**
* implemented. <br>
*
*/
  public void test_toString(){
    th.checkPoint("toString()java.lang.String");
    checkToString("123456.789");
    checkToString("121213456.768678765454675453212234356789");
    checkToString("123976961878947824578456.7084094809481048194089");
    checkToString("112345678987654321234567890987623456");
    checkToString("123498799878978978378738232848956.7808134901294710471371648091749104801481904819414141241231241389");
    checkToString("1234508809184019481938101111380923819308103809136.8095829082342305234324234432270390252352345235789");
  }

  private void checkToString(String value){
    th.check(new BigDecimal(value).toString(), value, "checking toString of '"+value+"'");
  }

}
