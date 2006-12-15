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


package gnu.testlet.wonka.lang.Math;
import gnu.testlet.*;

/**
* the class java.lang.Math is tested by 3 classes:<br>
* - MathTest  <br>
* - TESTMath  <br>
* - SMMathTest<br>
*     <br>
* these 3 classes cover the whole Math class <br>
*   <br>
* two new functions since JDK 1.2: <br>
* - toDegrees <br>
* - toRadians <br>
*
*/
public class SMMathTest implements Testlet
{
	protected TestHarness th;
	
	public void test(TestHarness testharness)
	{
		th = testharness;
		th.setclass("java.lang.Math");
		cygnustest();
		test_remainder();
		test_toDegrees();
		test_toRadians();
	}

/**
* new function in JDK 1.2. <br>
*
* tests for NaN POSITIVE and NEGATIVE_INFINRTY are not based on spec's, but what we <br>
* mathematical expect --> JDK 1.2 complies to those tests
*/
	public void test_toDegrees()
	{
		th.checkPoint("toDegrees(double)double)");
		th.check(Math.toDegrees(0.0) , 0.0 , "0.0 --> 0.0");
		th.check(Math.toDegrees(-0.0) , -0.0 , "-0.0 --> -0.0");
		th.check(Math.toDegrees(Math.PI) , 180.0 , "PI --> 180.0");
		th.check(Math.toDegrees(Math.PI/2) , 90.0 , "PI/2 --> 90.0");
		th.check(Math.toDegrees(Math.PI/4) , 45.0 , "PI --> 45.0");
		th.check(Math.toDegrees(Math.PI/3) , 59.99999999999999 , "PI/3 -->60.0");
		th.check(Math.toDegrees(-Math.PI) , -180.0 , "-PI --> -180.0");
		th.check(Math.toDegrees(-Math.PI/2) , -90.0 , "-PI/2 --> -90.0");
		th.check(Math.toDegrees(-Math.PI/4) , -45.0 , "-PI --> -45.0");
		th.check(Math.toDegrees(-Math.PI/3) , -59.99999999999999 , "-PI/3 -->-60.0");
		th.check(Math.toDegrees(3.5) , (3.5 * 180.0 / Math.PI) , "test a number");
		th.check(new Double(Math.toDegrees(Double.NaN)).isNaN() , "NaN --> NaN");
		th.check(new Double(Math.toDegrees(Double.POSITIVE_INFINITY)).isInfinite() , "+ inf --> + inf");
		th.check(new Double(Math.toDegrees(Double.NEGATIVE_INFINITY)).isInfinite() , "- inf --> - inf");
	}

/**
* new function in JDK 1.2 <br>
*
* tests for NaN POSITIVE and NEGATIVE_INFINRTY are not based on spec's, but what we <br>
* mathematical expect --> JDK 1.2 complies to those tests
*/
	public void test_toRadians()
	{
		th.checkPoint("toRadians(double)double)");
		th.check( Math.toRadians(30.0) , Math.PI / 6 , "30.0 --> PI/6");
		th.check( Math.toRadians(60.0) , Math.PI / 3 , "60.0 --> PI/3");
		th.check( Math.toRadians(90.0) , Math.PI / 2 , "90.0 --> PI/2");
		th.check( Math.toRadians(-180.0) ,- Math.PI  , "180.0 --> PI");
		th.check( Math.toRadians(-30.0) ,- Math.PI / 6 , "-30.0 --> -PI/6");
		th.check( Math.toRadians(-60.0) ,- Math.PI / 3 , "-60.0 --> -PI/3");
		th.check( Math.toRadians(-90.0) ,- Math.PI / 2 , "-90.0 --> -PI/2");
		th.check( Math.toRadians(-180.0) ,- Math.PI  , "-180.0 --> -PI");
		th.check( Math.toRadians(0.0) , 0.0  , "0.0 --> 0.0");
		th.check( Math.toRadians(-0.0) , -0.0  , "- 0.0 --> - 0.0");
		th.check( Math.toRadians(123.0) , (123.0 * Math.PI /180) , "test a number");
		th.check(new Double(Math.toRadians(Double.NaN)).isNaN() , "NaN --> NaN");
		th.check(new Double(Math.toRadians(Double.POSITIVE_INFINITY)).isInfinite() , "+ inf --> + inf");
		th.check(new Double(Math.toRadians(Double.NEGATIVE_INFINITY)).isInfinite() , "- inf --> - inf");
	}

	public void test_remainder(){
	}
	
	private void cygnustest()	
	{

//the following code are tests taken from Cygnus files

//SIN
     th.checkPoint("sin(double)double"); 	
   	th.check (Math.abs(Math.sin (1e50)) <= 1.0, "sin of large Number");

//COS
     th.checkPoint("cos(double)double"); 	

    	th.check (Math.cos (0), 1.0);
      	th.check (Math.cos (Math.PI), -1.0);
      	th.check (Math.cos (Math.PI*1E18), 1.0,"PI E18");
      	th.check (Math.abs(Math.cos (1e50)) <= 1.0, "got: "+Math.cos(1e50));
      	th.check (Math.abs (Math.cos (Math.PI/2))
		     <= 1.0E-10,"expected 0.0,but got: "+Math.cos(Math.PI/2));
      // It's unreasonable to expect the result of this to be eactly
      // zero, but 2^-53, the value of the constant used here, is 1ulp
      // in the range of cos.

//MAX
     th.checkPoint("max(double,double)double"); 	

      	th.check (Double.toString (Math.max (0.0, -0.0)), "0.0");
      	th.check (Double.toString (Math.max (-0.0, -0.0)), "-0.0");
      	th.check (Double.toString (Math.max (0.0, -0.0)), "0.0");
      	th.check (Double.toString (Math.max (0.0, 0.0)), "0.0");
      	th.check (Double.toString (Math.max (1.0, 2.0)), "2.0");
      	th.check (Double.toString (Math.max (2.0, 1.0)), "2.0");
      	th.check (Double.toString (Math.max (-1.0, -2.0)), "-1.0");
      	th.check (Double.toString (Math.max (-2.0, 1.0)), "1.0");
      	th.check (Double.toString (Math.max (1.0, -2.0)), "1.0");
      	th.check (Double.toString (Math.max (2.0, Double.NaN)), "NaN");
      	th.check (Double.toString (Math.max (Double.NaN, 2.0)), "NaN");
      	th.check (Double.toString (Math.max (Double.NEGATIVE_INFINITY, 
			       Double.POSITIVE_INFINITY)), 
		     "Infinity");
      	th.check (Double.toString (Math.max (Double.POSITIVE_INFINITY, 
			       Double.POSITIVE_INFINITY)), 
		     "Infinity");
      	th.check (Double.toString (Math.max (Double.NEGATIVE_INFINITY, 0.0)),
		     "0.0");
      	th.check (Double.toString (Math.max (Double.POSITIVE_INFINITY, 0.0)),
		     "Infinity");
      	th.check (Double.toString (Math.max (Math.PI, 0.0)),
		     Double.toString(Math.PI));
     th.checkPoint("max(float,float)float"); 	

      	th.check ((Math.max (0.0f, -0.0f)), 0.0);
      	th.check ((Math.max (-0.0f, -0.0f)), -0.0);
      	th.check ((Math.max (0.0f, -0.0f)), 0.0);
      	th.check ((Math.max (0.0f, 0.0f)), 0.0);
      	th.check ((Math.max (1.0f, 2.0f)), 2.0);
      	th.check ((Math.max (2.0f, 1.0f)), 2.0);
      	th.check ((Math.max (-1.0f, -2.0f)), -1.0);
      	th.check ((Math.max (-2.0f, 1.0f))== 1.0,"got: "+(Math.max (-2.0f, 1.0f)));
      	th.check ((Math.max (1.0f, -2.0f))== 1.0,"got: "+(Math.max (1.0f, -2.0f)));
      	th.check ((Math.max (2.0f, Float.NaN)), Float.NaN);
      	th.check ((Math.max (Float.NaN, 2.0f)), Float.NaN);
      	th.check ((Math.max (Float.NEGATIVE_INFINITY, 
			       Float.POSITIVE_INFINITY)), 
		     Float.POSITIVE_INFINITY);
      	th.check ((Math.max (Float.POSITIVE_INFINITY, 
			       Float.POSITIVE_INFINITY)), 
		     Float.POSITIVE_INFINITY);
      	th.check ((Math.max (Float.NEGATIVE_INFINITY, 0.0f)),
		     0.0);
      	th.check ((Math.max (Float.POSITIVE_INFINITY, 0.0f)),
		     Float.POSITIVE_INFINITY);
      	th.check ((Math.max ((float)Math.PI, 0.0f)),
		     ((float)Math.PI));

//MIN
     th.checkPoint("min(double,double)double"); 	

      	th.check ((Math.min (0.0, -0.0)), -0.0);
      	th.check ((Math.min (-0.0, -0.0)), -0.0);
      	th.check ((Math.min (0.0, -0.0)), -0.0);
      	th.check ((Math.min (0.0, 0.0)), 0.0);
      	th.check ((Math.min (1.0, 2.0)), 1.0);
      	th.check ((Math.min (2.0, 1.0)), 1.0);
      	th.check ((Math.min (-1.0, -2.0)), -2.0);
      	th.check ((Math.min (-2.0, 1.0)), -2.0);
      	th.check ((Math.min (1.0, -2.0)), -2.0);
      	th.check ((Math.min (2.0, Double.NaN)), Double.NaN);
      	th.check ((Math.min (Double.NaN, 2.0)), Double.NaN);
      	th.check ((Math.min (Double.NEGATIVE_INFINITY, 
			       Double.POSITIVE_INFINITY)), 
		     Double.NEGATIVE_INFINITY);
      	th.check ((Math.min (Double.POSITIVE_INFINITY, 
			       Double.POSITIVE_INFINITY)), 
		     Double.POSITIVE_INFINITY);
      	th.check ((Math.min (Double.NEGATIVE_INFINITY, 0.0)),
		     Double.NEGATIVE_INFINITY);
      	th.check ((Math.min (Double.POSITIVE_INFINITY, 0.0)),
		     0.0);
      	th.check ((Math.max (Math.PI, 0.0)),
		    (Math.PI));

     th.checkPoint("min(float,float)float"); 	

      	th.check ((Math.min (0.0f, -0.0f)), -0.0);
      	th.check ((Math.min (-0.0f, -0.0f)),-0.0);
      	th.check ((Math.min (0.0f, -0.0f)), -0.0);
      	th.check ((Math.min (0.0f, 0.0f)), 0.0);
      	th.check ((Math.min (1.0f, 2.0f))== 1.0,"got: "+(Math.min (1.0f, 2.0f)));
      	th.check ((Math.min (2.0f, 1.0f))== 1.0,"got: "+(Math.min (2.0f, 1.0f)));
      	th.check ((Math.min (-1.0f, -2.0f)), -2.0);
      	th.check ((Math.min (-2.0f, 1.0f)), -2.0);
      	th.check ((Math.min (1.0f, -2.0f)), -2.0);
      	th.check ((Math.min (2.0f, Float.NaN)), Float.NaN);
      	th.check ((Math.min (Float.NaN, 2.0f)), Float.NaN);
      	th.check ((Math.min (Float.NEGATIVE_INFINITY, 
			       Float.POSITIVE_INFINITY)), 
			     Float.NEGATIVE_INFINITY);
      	th.check ((Math.min (Float.POSITIVE_INFINITY, 
			       Float.POSITIVE_INFINITY)), 
		     Float.POSITIVE_INFINITY);
      	th.check ((Math.min (Float.NEGATIVE_INFINITY, 0.0f)),
		     Float.NEGATIVE_INFINITY);
      	th.check ((Math.min (Float.POSITIVE_INFINITY, 0.0f)),
		     0.0);
      	th.check ((Math.max ((float)Math.PI, 0.0f)),
		     ((float)Math.PI));

//RINT
  	th.checkPoint("rint(double)double");
	// Check for a well known rounding problem.
    th.check (Math.rint (-3.5), -4.0,"got: "+ Math.rint(-3.5));
    th.check (Math.rint (4.5),   4.0,"got: "+ Math.rint( 4.5));
   	th.check (Math.rint (3.5),   4.0,"got: "+ Math.rint( 3.5));
    th.check (Math.rint (-0.5),  -0.0,"got: "+ Math.rint(-0.5));
    th.check (Math.rint (-1.5),   -2.0,"got: "+ Math.rint( -1.5));
   	th.check (Math.rint (5.5),   6.0,"got: "+ Math.rint( 5.5));
    th.check (Math.rint (-3.5001), -4.0,"got: "+ Math.rint(-3.5001));
    th.check (Math.rint (4.4999),   4.0,"got: "+ Math.rint( 4.4999));
   	th.check (Math.rint (3.5001),   4.0,"got: "+ Math.rint( 3.5001));
    th.check (Math.rint (-0.5001),  -1.0,"got: "+ Math.rint(-0.5001));
    th.check (Math.rint (-1.50001),   -2.0,"got: "+ Math.rint( -1.50001));
   	th.check (Math.rint (5.500001),   6.0,"got: "+ Math.rint( 5.500001));
    th.check (Math.rint (0.0),   0.0,"got: "+ Math.rint(0.0));
    th.check (Math.rint (-0.0),   -0.0,"got: "+ Math.rint(-0.0));
    th.check (Math.rint (Double.NaN),   Double.NaN,"got: "+ Math.rint(Double.NaN));
    th.check (Math.rint (Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY,"got: "+ Math.rint(Double.POSITIVE_INFINITY));
    th.check (Math.rint (Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY,"got: "+ Math.rint(Double.NEGATIVE_INFINITY));
  	th.checkPoint("floor(double)double");
	// Check for a well known rounding problem.
    th.check (Math.floor (-3.5),  -4.0,"got: "+ Math.floor(-3.5));
    th.check (Math.floor (4.5),   4.0,"got: "+ Math.floor( 4.5));
   	th.check (Math.floor (3.5),   3.0,"got: "+ Math.floor( 3.5));
    th.check (Math.floor (-0.5),  -1.0,"got: "+ Math.floor(-0.5));
    th.check (Math.floor (0.5),   0.0,"got: "+ Math.floor(-0.5));
    th.check (Math.floor (-1.5),  -2.0,"got: "+ Math.floor( -1.5));
   	th.check (Math.floor (5.5),   5.0,"got: "+ Math.floor( 5.5));
    th.check (Math.floor (-3.5001), -4.0,"got: "+ Math.floor(-3.5001));
    th.check (Math.floor (4.9999),   4.0,"got: "+ Math.floor( 4.9999));
   	th.check (Math.floor (3.0),   3.0,"got: "+ Math.floor( 3.0));
    th.check (Math.floor (-3.0),  -3.0,"got: "+ Math.floor(-3.0));
    th.check (Math.floor (-1.50001),   -2.0,"got: "+ Math.floor( -1.50001));
   	th.check (Math.floor (5.500001),   5.0,"got: "+ Math.floor( 5.500001));
    th.check (Math.floor (0.0),   0.0,"got: "+ Math.floor(0.0));
    th.check (Math.floor (-0.0),   -0.0,"got: "+ Math.floor(-0.0));
    th.check (Math.floor (Double.NaN),   Double.NaN,"got: "+ Math.floor(Double.NaN));
    th.check (Math.floor (Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY,"got: "+ Math.floor(Double.POSITIVE_INFINITY));
    th.check (Math.floor (Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY,"got: "+ Math.floor(Double.NEGATIVE_INFINITY));
  	
  	
  	th.checkPoint("ceil(double)double");
	// Check for a well known rounding problem.
    th.check (Math.ceil (-3.5), -3.0,"got: "+ Math.ceil(-3.5));
    th.check (Math.ceil (4.5),   5.0,"got: "+ Math.ceil( 4.5));
   	th.check (Math.ceil (3.5),   4.0,"got: "+ Math.ceil( 3.5));
    th.check (Math.ceil (-0.5),  -0.0,"got: "+ Math.ceil(-0.5));
    th.check (Math.ceil (-1.5),   -1.0,"got: "+ Math.ceil( -1.5));
   	th.check (Math.ceil (5.5),   6.0,"got: "+ Math.ceil( 5.5));
    th.check (Math.ceil (-3.999999), -3.0,"got: "+ Math.ceil(-3.999999));
    th.check (Math.ceil (4.0),   4.0,"got: "+ Math.ceil( 4.0));
   	th.check (Math.ceil (-3.0),   -3.0,"got: "+ Math.ceil(-3.));
    th.check (Math.ceil (0.0),   0.0,"got: "+ Math.ceil(0.0));
    th.check (Math.ceil (-0.0),   -0.0,"got: "+ Math.ceil(-0.0));
    th.check (Math.ceil (Double.NaN),   Double.NaN,"got: "+ Math.ceil(Double.NaN));
    th.check (Math.ceil (Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY,"got: "+ Math.ceil(Double.POSITIVE_INFINITY));
    th.check (Math.ceil (Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY,"got: "+ Math.ceil(Double.NEGATIVE_INFINITY));
	}



}      