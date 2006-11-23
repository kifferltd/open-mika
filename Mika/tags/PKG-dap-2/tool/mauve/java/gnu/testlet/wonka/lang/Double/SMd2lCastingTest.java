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


/*
*  this test was created to test casting difficulties we have
*
*
*/
package gnu.testlet.wonka.lang.Double;
import gnu.testlet.*;

 public class SMd2lCastingTest implements Testlet
{

  protected static TestHarness th;

  public void test (TestHarness the_harness)
  {
    th = the_harness;
    th.setclass("java.lang.Double");
    th.checkPoint("testing double.toString");
    testtoString();
    th.checkPoint("testing f2d");
    testf2d();
    th.checkPoint("Casting ...");
    testcasting();
    th.checkPoint("String Representation of double's");
    //test_doubleStrings();
  }


  public void testf2d() {
    Float f = new Float(1.0f);
    th.check(f.doubleValue() == 1.0,"got: " );
  }

  public void test_doubleStrings(){
    double current = Double.longBitsToDouble(Double.doubleToLongBits(1.0)-10);
    for(int k = 0 ; k < 20 ; k++){
      current = Double.longBitsToDouble(Double.doubleToLongBits(current)+1);
      verifyStrings(current);
    }
    current = -Double.MAX_VALUE;
    for(int k = 0 ; k < 20 ; k++){
      current = Double.longBitsToDouble(Double.doubleToLongBits(current)-1);
      verifyStrings(current);
    }
    current = 123456.789;
    for(int k = 0 ; k < 20 ; k++){
      current = Double.longBitsToDouble(Double.doubleToLongBits(current)+1);
      verifyStrings(current);
    }
    current = -0.0005678;
    for(int k = 0 ; k < 20 ; k++){
      current = Double.longBitsToDouble(Double.doubleToLongBits(current)+1);
      verifyStrings(current);
    }
    current = 56789.134E56;
    for(int k = 0 ; k < 20 ; k++){
      current = Double.longBitsToDouble(Double.doubleToLongBits(current)+1);
      verifyStrings(current);
    }
    current = 5.6789134E-256;
    for(int k = 0 ; k < 20 ; k++){
      current = Double.longBitsToDouble(Double.doubleToLongBits(current)+1);
      verifyStrings(current);
    }

  }

  public void verifyStrings(double d){
    double prev = Double.longBitsToDouble(Double.doubleToLongBits(d)-1);
    String cur = ""+d;
    String prv = ""+prev;
    th.check(!cur.equals(prv) , "verifying '"+cur+"' > '"+prv+"'");
    //th.debug(cur);
  }

  public void testtoString(){
    Double  d;
    d = new Double(1.0);
    th.check( d.toString().equals("1.0"), "test 1, got: "+d+", but exp.: 1.0");
    d = new Double(0.5);
    th.check( d.toString().equals("0.5"), "test 2, got: "+d+", but exp.: 0.5");
    d = new Double(0.05);
    th.check(Double.toString(0.05).equals("0.05"), "test 3, got: "+d+", but exp.:0.05 ");
    d = new Double(0.005);
    th.check(Double.toString(0.005).equals("0.005"), "test 4, got: "+d+", but exp.:0.005 ");
    d = new Double(0.25);
    th.check(Double.toString(0.25).equals("0.25"), "test 5, got: "+d+", but exp.: 0.25");
    d = new Double(0.025);
    th.check(Double.toString(0.025).equals("0.025"), "test 6, got: "+d+", but exp.: 0.025");
    d = new Double(0.0025);
    th.check(Double.toString(0.0025).equals("0.0025"), "test 7, got: "+d+", but exp.: 0.0025");
    d = new Double(0.00025);
    th.check(Double.toString(0.00025).equals("2.5E-4"), "test 8, got: "+d+", but exp.: 2.5E-4");
    d = new Double(0.9);
    th.check( d.toString().equals("0.9"), "test 9, got: "+d+", but exp.: 0.9");
    d = new Double(99.99999);
    if ((100.0 -d.doubleValue() ) > 0.0000099 )
    th.check( d.toString().equals("99.99999"), "test 10, got: "+d+", but exp.: 99.99999");
    d = new Double(999999.9);
    if ((1.0E7 -d.doubleValue() ) > 0.0001 )
    th.check( d.toString().equals("999999.9"), "test 11, got: "+d+", but exp.: 999999.9");
    d = new Double(999999.0);
    if (Math.abs(d.doubleValue() -1.0E7) > 0.99 )
    th.check( d.toString().equals("999999.0"), "test 12, got: "+d+", but exp.: 999999.0");
    d = new Double(999999.99);
    if (Math.abs(d.doubleValue() -1.0E7) > 0.0099 )
    th.check( d.toString().startsWith("999999.9"), "test 13, got: "+d+", but exp.: 999999.99");
    d = new Double(888888.9);
    if (Math.abs(d.doubleValue() -1.0E7) > 0.099 )
    th.check( d.toString().equals("888888.9"), "test 14, got: "+d+", but exp.: 888888.9");
    d = new Double(999999.9999);
    if (Math.abs(d.doubleValue() -1.0E7) > 0.000099 )
    th.check( d.toString().equals("999999.9999"), "test 15, got: "+d+", but exp.: 999999.9999");
    d = new Double(99.0);
    if ((100.0 -d.doubleValue() ) > 0.99 )
    th.check( d.toString().equals("99.0"), "test 16 got: "+d+", but exp.: 99.0");
    d = new Double(9.999998E234);
    th.check( d.toString() ,"9.999998E234" , "test 17, got: "+d+", but exp.: 9.999998E234");
    d = new Double(9.99998E-154);
    th.check( d.toString().startsWith("9.9999") && d.toString().endsWith("E-154"), "test 18, got: "+d+", but exp.: 9.99998E-154");
    d = new Double(9.9999999E-232);
    th.check( d.toString().startsWith("9.999999") && d.toString().endsWith("E-232"), "test 19, got: "+d+", but exp.: 9.9999999E-232");
    d = new Double(8.888889E-54);
    th.check( d.toString().startsWith("8.88888")&& d.toString().endsWith("E-54"), "test 20, got: "+d+", but exp.: 8.888889E-54");
    d = new Double(9.999988E-234);
    th.check( d.toString().startsWith("9.99998")&& d.toString().endsWith("E-234"), "test 21, got: "+d+", but exp.: 9.999988E-234");
    d = new Double(9.999888E-234);
    th.check( d.toString().startsWith("9.99988")&& d.toString().endsWith("E-234"), "test 22, got: "+d+", but exp.: 9.999888E-234");

    d = new Double(-0.0101);
    th.check( d.toString().equals("-0.0101"), "test 23, got: "+d+", but exp.: -0.0101");
    d = new Double(-0.00101);
    th.check( d.toString().equals("-0.00101"), "test 24, got: "+d+", but exp.:-0.00101 ");
    d = new Double(-0.205);
    th.check( d.toString().equals("-0.205"), "test 25, got: "+d+", but exp.: -0.205");
    d = new Double(-0.0205);
    th.check( d.toString().equals("-0.0205"), "test 26, got: "+d+", but exp.: -0.0205");
    d = new Double(-0.00205);
    th.check( d.toString().equals("-0.00205"), "test 27, got: "+d+", but exp.: -0.00205");

    th.checkPoint("testing Float.toString");
    Float df;
    df = new Float(-0.0101f);
    th.check( df.toString().startsWith("-0.010"), "test 23, got: "+df+", but exp.: -0.0101");
    df = new Float(-0.00101f);
    th.check( df.toString().equals("-0.00101"), "test 24, got: "+df+", but exp.:-0.00101 ");
    df = new Float(-0.205f);
    th.check( df.toString().startsWith("-0.20"), "test 25, got: "+df+", but exp.: -0.205");
    df = new Float(-0.0205f);
    th.check( df.toString().equals("-0.0205"), "test 26, got: "+df+", but exp.: -0.0205");
    df = new Float(-0.00205f);
    th.check( df.toString().equals("-0.00205"), "test 27, got: "+df+", but exp.: -0.00205");
  }

  public void testcasting() {

    long l = Long.MIN_VALUE;
    th.check((int)l == 0 , "long to int -- 1");
    l = -1L;
    th.check((int)l == -1 , "long to int -- 2");
    l = Long.MAX_VALUE;
    th.check((int)l == -1 , "long to int -- 3");



  }

}
