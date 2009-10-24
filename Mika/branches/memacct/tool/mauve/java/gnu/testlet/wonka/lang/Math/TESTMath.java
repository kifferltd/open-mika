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

public class TESTMath extends Mv_Assert
{
        public final static String id = "$Id: TESTMath.java,v 1.2 2005/10/20 13:11:10 cvs Exp $";

	public TESTMath ( ) {}

	private final static double DEVIATION = 0.000001D;

	protected void runTest()
	{
		th.setclass("java.lang.Math");
		testBasics();
		th.checkPoint("sqrt(double)double");
		testSqrt();
		th.checkPoint("pow(double,double)double");
		testPow();
		testRound();
		testAbs();
		testExtra();
	}

	public static boolean approx ( double value, double expectedValue, double deviation)
	{
		double test;

		test = value;
		if ( expectedValue != 0.0D)
			test = ( expectedValue - value) / expectedValue;
		
		test = test < 0 ? -test : test; // abs

		return test < deviation;
	}

	
	protected void setUp ()
	{
	}

	public void testBasics ()
	{
		th.checkPoint("E(public)double");
	 	assertTrue( "Math.E == 2.7182818284590452354", Math.E == 2.7182818284590452354);
		th.checkPoint("PI(public)double");
		assertTrue( "Math.PI == 3.14159265358979323846", Math.PI == 3.14159265358979323846);
	}

	public void testSqrt ()
	{
		assertTrue( "Double.isNaN ( Math.sqrt ( Double.NaN))", Double.isNaN ( Math.sqrt ( Double.NaN)));

		assertTrue( "Double.isNaN ( Math.sqrt ( -10.0D))", Double.isNaN ( Math.sqrt ( -10.0D))); 

		assertTrue( "Double.isInfinite ( Math.sqrt ( Double.POSITIVE_INFINITY)) ==>"+Math.sqrt(Double.POSITIVE_INFINITY), Double.isInfinite(Math.sqrt ( Double.POSITIVE_INFINITY)));

		assertTrue( "Double.isNaN ( Math.sqrt ( Double.NEGATIVE_INFINITY))", Double.isNaN ( Math.sqrt ( Double.NEGATIVE_INFINITY))); 

		assertTrue( "Math.sqrt ( 0.0) == 0.0", Math.sqrt ( 0.0) == 0.0);

		assertTrue( "Math.sqrt ( -0.0) == -0.0", Math.sqrt ( -0.0) == -0.0);

//System.out.println ( "Math.sqrt ( 2.0D) = " + Math.sqrt ( 2.0D) + ", " + ( int)Math.sqrt ( 2.0D));
		assertTrue( "Math.sqrt ( 2.0D)", approx ( Math.sqrt ( 2.0D), 1.4142136D, DEVIATION));
	}

	public void testPow ()
	{
// see The Java Language Specification on page 522
		assertTrue( "Math.pow ( 456.7689798D, 0.0) == 1.0", Math.pow ( 456.7689798D, 0.0) == 1.0);
		assertTrue( "Math.pow ( 682.5464, -0.0) == 1.0", Math.pow ( 682.5464, -0.0) == 1.0);
		assertTrue( "Math.pow ( +Inf, 0.0) == 1.0", Math.pow ( Double.POSITIVE_INFINITY, 0.0) == 1.0);
		assertTrue( "Math.pow ( -Inf, -0.0) == 1.0", Math.pow ( Double.NEGATIVE_INFINITY, -0.0) == 1.0);
		assertTrue( "Math.pow( NaN, 0,0) got: "+Math.pow (Double.NaN, 0.0), Math.pow (Double.NaN, 0.0) == 1.0);
		
		assertTrue( "Math.pow ( 123.747, 1.0) == 123.747", approx ( Math.pow ( 123.747, 1.0), 123.747, DEVIATION));

		assertTrue( "Double.isNaN ( Math.pow ( 10.0, Double.NaN))", Double.isNaN ( Math.pow ( 10.0, Double.NaN)));

// second argument nonzero
		assertTrue( "Double.isNaN ( Math.pow ( Double.NaN, 89.23))", Double.isNaN ( Math.pow ( Double.NaN, 89.23)));
// second argument zero?
// TODO give a wrong value
//		assertTrue("pow( NaN, 0,0) got: "+Math.pow (Double.NaN, 0.0), Math.pow (Double.NaN, 0.0) == 1.0);
//System.out.println ( "Math.pow ( Double.NaN, -0.0) " + Long.toHexString ( Double.doubleToLongBits ( Math.pow ( Double.NaN, -0.0))) + ",  " + Math.pow ( Double.NaN, -0.0));
//		assertTrue( "!Double.isNaN ( Math.pow ( Double.NaN, -0.0))", !Double.isNaN ( Math.pow ( Double.NaN, -0.0)));

// first argument > 1 - absolute value
		assertTrue( "Double.isInfinite ( Math.pow ( 647.585, Double.POSITIVE_INFINITY))", Double.isInfinite ( Math.pow ( 647.585, Double.POSITIVE_INFINITY)));
		assertTrue( "Double.isInfinite ( Math.pow ( -647.585, Double.POSITIVE_INFINITY))", Double.isInfinite ( Math.pow ( -647.585, Double.POSITIVE_INFINITY)));
// first argument < 1 - absolute value
		assertTrue( "Double.isInfinite ( Math.pow ( 0.647, Double.NEGATIVE_INFINITY))", Double.isInfinite ( Math.pow ( 0.647, Double.NEGATIVE_INFINITY)));
		assertTrue( "Double.isInfinite ( Math.pow ( -0.647, Double.NEGATIVE_INFINITY))", Double.isInfinite ( Math.pow ( -0.647, Double.NEGATIVE_INFINITY)));

// first argument > 1 - absolute value
		assertEqual( "Math.pow ( 1.5, Double.NEGATIVE_INFINITY) == 0.0", Math.pow (1.5, Double.NEGATIVE_INFINITY) , 0.0);
		assertEqual( "Math.pow ( -1.5, Double.NEGATIVE_INFINITY) == 0.0", Math.pow ( -1.5, Double.NEGATIVE_INFINITY) , 0.0);
// first argument < 1 - absolute value
		assertEqual( "Math.pow ( 0.5, Double.POSITIVE_INFINITY) == 0.0", Math.pow ( 0.5, Double.POSITIVE_INFINITY) , 0.0);
		assertEqual( "Math.pow ( -0.5, Double.POSITIVE_INFINITY) == 0.0", Math.pow ( -0.5, Double.POSITIVE_INFINITY) , 0.0);

// first argument absolute == 1 
		assertTrue( "Double.isNaN ( Math.pow ( 1.0, Double.POSITIVE_INFINITY))", Double.isNaN ( Math.pow ( 1.0, Double.POSITIVE_INFINITY)));
// second argument is infinite
		assertTrue( "Double.isNaN ( Math.pow ( -1.0, Double.NEGATIVE_INFINITY))", Double.isNaN ( Math.pow ( -1.0, Double.NEGATIVE_INFINITY)));
		assertTrue( "Double.isNaN ( Math.pow ( 1.0, Double.POSITIVE_INFINITY))", Double.isNaN ( Math.pow ( 1.0, Double.POSITIVE_INFINITY)));
		assertTrue( "Double.isNaN ( Math.pow ( -1.0, Double.NEGATIVE_INFINITY))", Double.isNaN ( Math.pow ( -1.0, Double.NEGATIVE_INFINITY)));

		assertTrue( "Math.pow ( 0.0, 0.00005) == 0.0", Math.pow ( 0.0, 0.00005) == 0.0);
		assertTrue( "Math.pow ( Double.POSITIVE_INFINITY, -0.00005) == 0.0", Math.pow ( Double.POSITIVE_INFINITY, -0.00005) == 0.0);

		assertTrue( "Double.isInfinite ( Math.pow ( 0.0, -0.00005))", Double.isInfinite ( Math.pow ( 0.0, -0.00005)));
		assertTrue( "Double.isInfinite ( Math.pow ( Double.POSITIVE_INFINITY, 0.00005))", Double.isInfinite ( Math.pow ( Double.POSITIVE_INFINITY, 0.00005)));

		assertTrue( "Math.pow ( -0.0, 6.0) == 0.0", Math.pow ( -0.0, 6.0) == 0.0);
		assertTrue( "Math.pow ( Double.NEGATIVE_INFINITY, -8.0) == 0.0", Math.pow ( Double.NEGATIVE_INFINITY, -8.0) == 0.0);

		assertTrue( "Math.pow ( -0.0, 5.0) == -0.0", Math.pow ( -0.0, 5.0) == -0.0);
		assertTrue( "Math.pow ( Double.NEGATIVE_INFINITY, -7.0) == -0.0", Math.pow ( Double.NEGATIVE_INFINITY, -7.0) == -0.0);

		assertTrue( "Double.isInfinite ( Math.pow ( -0.0, -6.0))", Double.isInfinite ( Math.pow ( -0.0, -6.0)));
		assertTrue( "Double.isInfinite ( Math.pow ( Double.NEGATIVE_INFINITY, 6.0))", Double.isInfinite ( Math.pow ( Double.NEGATIVE_INFINITY, 6.0)));

		assertTrue( "Double.isInfinite ( Math.pow ( -0.0, -7.0))", Double.isInfinite ( Math.pow ( -0.0, -7.0)));
		assertTrue( "Double.isInfinite ( Math.pow ( Double.NEGATIVE_INFINITY, 7.0))", Double.isInfinite ( Math.pow ( Double.NEGATIVE_INFINITY, 7.0)));

// counting on Math.abs
		assertTrue( "Math.pow ( -0.00005, 6.0) == Math.pow ( Math.abs ( -0.00005), 6.0)", Math.pow ( -0.00005, 6.0) == Math.pow ( Math.abs ( -0.00005), 6.0));
		assertTrue( "Math.pow ( -0.00005, 3.0) == -Math.pow ( Math.abs ( -0.00005), 3.0)", Math.pow ( -0.00005, 3.0) == -Math.pow ( Math.abs ( -0.00005), 3.0));

		assertTrue( "Double.isNaN ( Math.pow ( -6.0, 464.44678))", Double.isNaN ( Math.pow ( -6.0, 464.44678)));
	}

	public void testRound ()
	{
// test only doubles
		th.checkPoint("round(double)long");

		assertTrue( "Math.round ( 3.4D) == 3L", Math.round ( 3.4D) == 3L);

		assertTrue( "Math.round ( 9.55D) == 10L", Math.round ( 9.55D) == 10L);

		assertTrue( "Math.round ( Double.NaN) == 0L", Math.round ( Double.NaN) == 0L);

		double d1 = Long.MIN_VALUE;
		d1 -= 5;
		assertTrue( "Math.round ( Long.MIN_VALUE - 5) == Long.MIN_VALUE", Math.round ( d1) == Long.MIN_VALUE); 
		assertTrue( "Math.round ( Double.NEGATIVE_INFINITY) == Long.MIN_VALUE", Math.round ( Double.NEGATIVE_INFINITY) == Long.MIN_VALUE);

		d1 = Long.MAX_VALUE;
		d1 += 5;
		assertTrue( "Math.round ( Long.MAX_VALUE + 5) == Long.MAX_VALUE", Math.round ( d1) == Long.MAX_VALUE);
		assertTrue( "Math.round ( Double.POSITIVE_INFINITY) == Long.MAX_VALUE", Math.round ( Double.POSITIVE_INFINITY) == Long.MAX_VALUE);
	}														  

	public void testAbs ()
	{
// test only the doubles
		th.checkPoint("abs(double)double");
		assertTrue( "Math.abs ( 0.0D) == 0.0D", Math.abs ( 0.0D) == 0.0D);
		assertTrue( "Math.abs ( -0.0D) == 0.0D", Math.abs ( -0.0D) == 0.0D);

		assertTrue( "Double.isInfinite ( Math.abs ( Double.POSITIVE_INFINITY))", Double.isInfinite ( Math.abs ( Double.POSITIVE_INFINITY))); 

		assertTrue( "Double.isNaN ( Math.abs ( Double.NaN))", Double.isNaN ( Math.abs ( Double.NaN)));


		assertTrue( "Math.abs ( 10.0D) == 10.0D", Math.abs ( 10.0D) == 10.0D);

		assertTrue( "Math.abs( -23.0D) == 23.0D", Math.abs( -23.0D) == 23.0D);

		assertTrue( "Math.abs( 1000L ) == 1000", Math.abs( 1000L ) == 1000);  

		assertTrue( "Math.abs ( -2334242L) == 2334242", Math.abs ( -2334242L) == 2334242);

		assertTrue( "Math.abs ( Long.MIN_VALUE) == Long.MIN_VALUE", Math.abs ( Long.MIN_VALUE) == Long.MIN_VALUE);
		
		assertTrue( "Math.abs ( -23.34D) == 23.34D", Math.abs ( -23.34D) == 23.34D);

	}

	public void testExtra ()
	{
		double d;
		th.checkPoint("sqrt(double)double");

		for ( d = 0.0D; d < 10000.0D; d += 123.45D)
		{
//System.out.println ( "testExtra d == Math.pow ( Math.sqrt ( d), 2) " + d);
			assertTrue( approx ( Math.pow ( Math.sqrt ( d), 2), d, DEVIATION));
		}
	}
}
