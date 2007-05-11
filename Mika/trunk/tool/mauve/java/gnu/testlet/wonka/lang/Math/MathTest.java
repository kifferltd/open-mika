/* Copyright (C) 1999 Hewlett-Packard Company

   This file is part of Mauve.

   Mauve is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.

   Mauve is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Mauve; see the file COPYING.  If not, write to
   the Free Software Foundation, 59 Temple Place - Suite 330,
   Boston, MA 02111-1307, USA.
*/

// Tags: JLS1.0
// edited by smartmove

package gnu.testlet.wonka.lang.Math;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class MathTest implements Testlet
{

  protected static TestHarness harness;
	public void test_Basics()
	{
		harness.checkPoint("E(public)double");
	 	harness.check(!( Math.E != 2.7182818284590452354 ), 
			"test: check value of E");
		harness.checkPoint("PI(public)double");

		harness.check(!( Math.PI != 3.14159265358979323846 ), 
			"test: check value of PI");
	}

	public void test_sincostan()
	{
	     harness.checkPoint("sin(double)double");

		harness.check(!(  !(new Double(Math.sin( Double.NaN ))).isNaN() ), 
			"test:value of sin(NaN)= NaN");
		harness.check(!(  !(new Double(Math.sin( Double.POSITIVE_INFINITY ))).isNaN() ), 
			"test:value of sin(POSITIVE_INFINITY)=NaN");
		harness.check(!(  !(new Double(Math.sin( Double.NEGATIVE_INFINITY ))).isNaN() ), 
			"test:value of sin(NEGATIVE_INFINITY)= NaN");
		harness.check(!(  Math.sin( -0.0 ) != -0.0 ), 
			"test:value of sin(-0.0)= -0.0");
		harness.check(!(  Math.sin( 0.0 ) != 0.0 ), 
			"test:value of sin(0.0)= 0.0");
		harness.check(!( Math.sin( Math.PI / 2.0 + Math.PI /6.0 ) <= 0.0),
			"test:value of sin(4*PI/6)>= 0.0");
		harness.check((Math.cos(Math.PI/4.0)-Math.sin(Math.PI/4.0)<=0.000000001),
		"test:value of sin(PI/4)= cos(PI/4): " + Math.sin(Math.PI/4.0) + " != " + Math.cos(Math.PI/4.0));

	    harness.checkPoint("cos(double)double");

		harness.check(!(  !(new Double(Math.cos( Double.NaN ))).isNaN() ), 
			"test:value of cos(NaN)= NaN");
		harness.check(!(  !(new Double(Math.cos( Double.POSITIVE_INFINITY ))).isNaN() ), 
			"test:value of cos(POSITIVE_INFINITY)= NaN");
		harness.check(!(  !(new Double(Math.cos( Double.NEGATIVE_INFINITY ))).isNaN() ), 
			"test:value of cos(NEGATIVE_INFINITY)= NaN");
		harness.check(!( Math.cos( Math.PI / 2.0 + Math.PI /6.0 ) >= 0.0 ), 
			"test:value of cos(4*PI/6)<= 0.0");
		
	    harness.checkPoint("tan(double)double");
	
		harness.check(!(  !(new Double(Math.tan( Double.NaN ))).isNaN() ), 
			"test:value of tan(NaN)= NaN");
		harness.check(!(  !(new Double(Math.tan( Double.POSITIVE_INFINITY ))).isNaN() ), 
			"test:value of tan(POSITIVE_INFINITY)= NaN");
		harness.check(!(  !(new Double(Math.tan( Double.NEGATIVE_INFINITY ))).isNaN()), 
			"test:value of tan(NEGATIVE_INFINITY)= NaN");
		harness.check(!(  Math.tan( -0.0 ) != -0.0 ), 
			"test:value of tan(-0.0)= -0.0");
		harness.check(!(  Math.tan( 0.0 ) != 0.0 ), 
			"test:value of tan(0.0)= 0.0");

		harness.check(!( Math.tan( Math.PI / 2.0 + Math.PI /6.0 ) >= 0.0 ), 
			"test:value of tan(4*PI/6)>= 0.0");
		
	}

  public void test_asinacosatan()
  {
    int i;
    double d1, d2, d3;

	     harness.checkPoint("asin(double)double");
		harness.check(!(  !(new Double(Math.asin( Double.NaN ))).isNaN() ), 
			"test:value of asin(NaN)=NaN");
		harness.check(!(  Math.asin( -0.0 ) != -0.0 ), 
			"test:value of asin(-0.0)=-0.0");
		harness.check(!(  Math.asin( 0.0 ) != 0.0 ), 
			"test:value of asin(0.0)=0.0");

		harness.check(!(  !(new Double(Math.asin( 10.0 ))).isNaN() ), 
			"test:value of asin(10.0)= NaN");
		harness.check(!(  !(new Double(Math.asin( Double.POSITIVE_INFINITY ))).isNaN()),
			"test:value of asin(POSITIVE_INFINITY)= NaN");
		harness.check(!(  !(new Double(Math.asin( Double.NEGATIVE_INFINITY ))).isNaN() ), 
		"test:value of asin(NEGATIVE_INFINITY)= NaN");
		harness.check(!(  !(new Double(Math.asin( -10.0 ))).isNaN() ), 
			"test:value of asin(-10.0)= NaN");

  // Tests added by CG 20070331
    for (i = 0; i < 100; ++i) {
      d1 = (i - 50) * 0.02d;
      d2 = Math.asin(d1);
      d3 = Math.sin(d2);
      harness.check(d1/d3 < 1.0000001 && d1/d3 > 0.9999999,
        "asin(" + d1 + ") = " + d2 + ", sin(" + d2 + ") = " + d3);
    }

    for (i = 0; i < 100; ++i) {
      d1 = (i - 50) * 0.01d * Math.PI;
      d2 = Math.sin(d1);
      d3 = Math.asin(d2);
      harness.check(d1/d3 < 1.0000001 && d1/d3 > 0.9999999,
        "sin(" + d1 + ") = " + d2 + ", asin(" + d2 + ") = " + d3);
    }


	     harness.checkPoint("acos(double)double");
		harness.check(!(  !(new Double(Math.acos( Double.NaN ))).isNaN() ), 
			"test:value of acos(NaN)= NaN");
		harness.check(!(  !(new Double(Math.acos( 10.0 ))).isNaN() ), 
			"test:value of acos(10.0)= NaN");
		harness.check(!(  !(new Double(Math.asin( Double.POSITIVE_INFINITY ))).isNaN() ), 
			"test:value of acos(POSITIVE_INFINITY)= NaN");
		harness.check(!(  !(new Double(Math.asin( Double.NEGATIVE_INFINITY ))).isNaN() ), 
			"test:value of acos(NEGATIVE_INFINITY)= NaN");
		harness.check(!(  !(new Double(Math.acos( -1.01 ))).isNaN() ), 
			"test:value of acos(-1.01)= NaN");

  // Tests added by CG 20070331
    for (i = 0; i < 100; ++i) {
      d1 = (i - 50) * 0.02d;
      d2 = Math.acos(d1);
      d3 = Math.cos(d2);
      harness.check(d1/d3 < 1.0000001 && d1/d3 > 0.9999999,
        "acos(" + d1 + ") = " + d2 + ", cos(" + d2 + ") = " + d3);
    }

    for (i = 0; i < 100; ++i) {
      d1 = i * 0.01d * Math.PI;
      d2 = Math.cos(d1);
      d3 = Math.acos(d2);
      harness.check(d1 == 0.0d && d3 == 0.0d || d1/d3 < 1.0000001 && d1/d3 > 0.9999999,
        "cos(" + d1 + ") = " + d2 + ", acos(" + d2 + ") = " + d3);
    }

	     harness.checkPoint("atan(double)double");
		harness.check(!(  !(new Double(Math.atan( Double.NaN ))).isNaN() ),
			"test:value of atan(NaN)= NaN");
		harness.check(!(  Math.atan( -0.0 ) != -0.0 ), 
			"test:value of atan(-0.0)= -0.0");
		harness.check(!(  Math.atan( 0.0 ) != 0.0 ), 
			"test:value of atan(0.0)= 0.0.");
		// atan of infinity is not defined --> this makes sense
		// because tan (PI/2) != POSITIVE_INFINITY
		harness.check(!(  !(new Double(Math.atan( Double.POSITIVE_INFINITY ))).isNaN() ), 
			"test:value of atan(POSITIVE_INFINITY)= NaN");
		harness.check(!(  !(new Double(Math.atan( Double.NEGATIVE_INFINITY ))).isNaN() ), 
		"test:value of atan(NEGATIVE_INFINITY)= NaN");
		
  // Tests added by CG 20070331
    for (i = 0; i < 100; ++i) {
      d1 = 7.0d / (i - 50);
      d2 = Math.atan(d1);
      d3 = Math.tan(d2);
      harness.check(d1 == 0.0d && d3 == 0.0d || d1/d3 < 1.0000001 && d1/d3 > 0.9999999,
        "atan(" + d1 + ") = " + d2 + ", tan(" + d2 + ") = " + d3);
    }

    for (i = 0; i < 100; ++i) {
      d1 = (i - 50) * 0.01d * Math.PI;
      d2 = Math.tan(d1);
      d3 = Math.atan(d2);
      harness.check(d1 == 0.0d && d3 == 0.0d || d1/d3 < 1.0000001 && d1/d3 > 0.9999999,
        "tan(" + d1 + ") = " + d2 + ", atan(" + d2 + ") = " + d3);
    }

  }

	public void test_atan2()
	{
		harness.checkPoint("atan2(double,double)double");
		harness.check(!(!(new Double( Math.atan2 (1.0 , Double.NaN ))).isNaN()), 
			"Error : test_atan2 failed - 1");
		harness.check(!(!(new Double( Math.atan2 (Double.NaN,1.0 ))).isNaN()), 
			"Error : test_atan2 failed - 2");

		harness.check((( Math.atan2(0.0, 10.0 ) == 0.0 ) &&
			( Math.atan2(2.0 , Double.POSITIVE_INFINITY ) == 0.0 )), 
			"Error : test_atan2 failed - 3");

		harness.check(!(( Math.atan2(-0.0, 10.0 ) != -0.0 ) ||
			( Math.atan2(-2.0 , Double.POSITIVE_INFINITY ) != -0.0 )), 
			"Error : test_atan2 failed - 4");

		harness.check(!(( Math.atan2(0.0, -10.0 ) != Math.PI) ||
			( Math.atan2(2.0 , Double.NEGATIVE_INFINITY ) != Math.PI )), 
			"Error : test_atan2 failed - 5");

		harness.check(Math.atan2(-0.0, -10.0), -Math.PI,
		  "Error : test_atan2 failed - 6a - got "+Math.atan2(-0.0, -10.0)+", but exp "+(-Math.PI));
		harness.check(Math.atan2(-2.0 , Double.NEGATIVE_INFINITY), -Math.PI ,
		  "Error : test_atan2 failed - 6b - got "+Math.atan2(-2.0, Double.NEGATIVE_INFINITY)+", but exp "+(-Math.PI));

		harness.check(!(( Math.atan2(10.0, 0.0 ) != Math.PI/2.0) ||
			( Math.atan2(Double.POSITIVE_INFINITY , 3.0) != Math.PI /2.0)), 
			"Error : test_atan2 failed - 7");
		harness.check(!(( Math.atan2(10.0,- 0.0 ) != Math.PI/2.0) ||
			( Math.atan2(Double.POSITIVE_INFINITY ,- 3.0) != Math.PI /2.0)), 
			"Error : test_atan2 failed - 8");

		harness.check(!(( Math.atan2(-10.0, 0.0 ) != -Math.PI/2.0) ||
			( Math.atan2(Double.NEGATIVE_INFINITY , 3.0) != -Math.PI /2.0)), 
			"Error : test_atan2 failed - 9");
		harness.check(!(( Math.atan2(-10.0, -0.0 ) != -Math.PI/2.0) ||
			( Math.atan2(Double.NEGATIVE_INFINITY ,- 3.0) != -Math.PI /2.0)), 
			"Error : test_atan2 failed - 10");

		harness.check(Math.abs( Math.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY ) - Math.PI/4.0) < 1.0E-15,
			"Error : test_atan2 failed - 11 got: "+Math.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY ));

		harness.check(Math.abs( Math.atan2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY ) - Math.PI*3.0/4.0)<1.0E-15,
			"Error : test_atan2 failed - 12 got: "+Math.atan2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY ));

		harness.check(Math.abs( Math.atan2(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY )+Math.PI/4.0)< 1.0E-15,
			"Error : test_atan2 failed - 13 got: "+Math.atan2(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY ));

		harness.check(Math.abs( Math.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY )+Math.PI*3.0/4.0)<1.0E-15,
			"Error : test_atan2 failed - 14 got: "+Math.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY ));

		harness.check(!(!(new Double( Math.atan2 (0.0 , Double.NaN ))).isNaN()), 
			"Error : test_atan2 failed - 15");
		harness.check(!(!(new Double( Math.atan2 (Double.NaN,0.0 ))).isNaN()), 
			"Error : test_atan2 failed - 16");
		harness.check(!(!(new Double( Math.atan2 (-0.0 , Double.NaN ))).isNaN()), 
			"Error : test_atan2 failed - 17");
		harness.check(!(!(new Double( Math.atan2 (Double.NaN,-0.0 ))).isNaN()), 
			"Error : test_atan2 failed - 18");
		harness.check(!(!(new Double( Math.atan2 (Double.POSITIVE_INFINITY , Double.NaN ))).isNaN()), 
			"Error : test_atan2 failed - 19");
		harness.check(!(!(new Double( Math.atan2 (Double.NaN,Double.POSITIVE_INFINITY ))).isNaN()), 
			"Error : test_atan2 failed - 20");
		harness.check(!(!(new Double( Math.atan2 (Double.NEGATIVE_INFINITY , Double.NaN ))).isNaN()), 
			"Error : test_atan2 failed - 21");
		harness.check(!(!(new Double( Math.atan2 (Double.NaN,Double.NEGATIVE_INFINITY ))).isNaN()), 
			"Error : test_atan2 failed - 22");

	}

	public void test_exp()
	{
		harness.check(!( !(new Double(Math.exp( Double.NaN ))).isNaN() ), 
			"Error : test_exp failed - 1");

		harness.check(!( !(new Double(Math.exp( Double.POSITIVE_INFINITY))).isInfinite() ), 
			"Error : test_exp failed - 2");

		harness.check(!( Math.exp( Double.NEGATIVE_INFINITY) != 0.0 ), 
			"Error : test_exp failed - 3");

		harness.check(( Math.exp( 0)==1.0), 
			"Error : test_exp failed - 4");

		harness.check(( Math.exp( 1.0)-(Math.E))<=0.0000000001, 
			"Error : test_exp failed - 5");

	


	}

	public void test_log()
	{														
		harness.check(!( !(new Double(Math.log( Double.NaN ))).isNaN() ), 
			"Error : test_log failed - 1");
		harness.check(!( !(new Double(Math.log( -1.0 ))).isNaN() ), 
			"Error : test_log failed - 2");
		harness.check(!( !(new Double(Math.log( Double.NEGATIVE_INFINITY))).isNaN() ), 
			"Error : test_log failed - 3");

		harness.check(!( !(new Double(Math.log( Double.POSITIVE_INFINITY ))).isInfinite() ), 
			"Error : test_log failed - 4");
		harness.check((Math.log( 1.0 ))==( 0.0 ), 
			"Error : test_log failed - 5");

		harness.check(	Math.log(0.0) == Double.NEGATIVE_INFINITY, 
			"Error : test_log failed - 6 got: "+Math.log(0.0));
		harness.check(	Math.log(-0.0) == Double.NEGATIVE_INFINITY, 
			"Error : test_log failed - 7 got: "+Math.log(-0.0));
  // Added CG 20070331
		System.out.println("log(E) = " + Math.log(Math.E)); 
		harness.check(( Math.log(Math.E)-(1.0d))<=0.0000000001, 
			"Error : test_log failed - 8");

    int i;
    double d1, d2, d3;

    for (i = 0; i < 100; ++i) {
      d1 = i * 0.1d;
      d1 = d1 * d1 * d1;
      d2 = Math.log(d1);
      d3 = Math.exp(d2);
      harness.check(d1 == 0.0d && d3 == 0.0d || d1/d3 < 1.0000001 && d1/d3 > 0.9999999,
        "log(" + d1 + ") = " + d2 + ", exp(" + d2 + ") = " + d3);
    }

    for (i = 0; i < 100; ++i) {
      d1 = (i - 50) * 0.01d;
      d2 = Math.exp(d1);
      d3 = Math.log(d2);
      harness.check(d1 == 0.0d && d3 == 0.0d || d1/d3 < 1.0000001 && d1/d3 > 0.9999999,
        "exp(" + d1 + ") = " + d2 + ", log(" + d2 + ") = " + d3);
    }

			
	}

	public void test_sqrt()
	{
		harness.check(!( !(new Double(Math.sqrt( Double.NaN ))).isNaN() ||
			 !(new Double(Math.sqrt( -10.0 ))).isNaN()), 
			"Error : test_sqrt failed - 1");

		harness.check(!( !(new Double(Math.sqrt( Double.NaN ))).isNaN() ||
			 !(new Double(Math.sqrt( -10.0 ))).isNaN()), 
			"Error : test_sqrt failed - 2");

		harness.check(!( !(new Double(Math.sqrt( Double.POSITIVE_INFINITY))).isInfinite()), 
			"Error : test_sqrt failed - 3");

		harness.check(!( Math.sqrt( -0.0) != -0.0), 
			"Error : test_sqrt failed - 4: " + Math.sqrt(-0.0));

		harness.check(!(Math.sqrt( 0.0) != 0.0), 
			"Error : test_sqrt failed - 5: " + Math.sqrt(0.0));


		double sq = Math.sqrt(4.0);
		harness.check(!(!( sq >= 1.9999 &&  sq <= 2.0001 )), 
			"Error : test_sqrt failed - 6: " + sq);

		harness.check(!( !(new Double(Math.sqrt(Double.NEGATIVE_INFINITY))).isNaN()), 
			"Error : test_sqrt failed - 7: expected NaN, got " + Math.sqrt(Double.NEGATIVE_INFINITY));

	}

	public void test_pow()
	{
		harness.check(!( Math.pow(1.0 , 0.0 ) != 1.0 ), 
			"Error : test_pow failed - 1");

		harness.check(!( Math.pow(2.0 , -0.0 ) != 1.0 ), 
			"Error : test_pow failed - 2");
		
		harness.check((Math.abs(Math.pow(123.0 , 1.0 ) - 123.0) < 0.001),
			"Error : test_pow failed - 3, got :"+Math.pow(123.0 , 1.0 ));

		harness.check(!( !(new Double(Math.pow( 10.0, Double.NaN ))).isNaN()), 
			"Error : test_pow failed - 4");

		harness.check(!( !(new Double(Math.pow( Double.NaN, 1.0 ))).isNaN()), 
			"Error : test_pow failed - 5");

		harness.check(!( !(new Double(Math.pow( 2.0, Double.POSITIVE_INFINITY ))).isInfinite()), 
			"Error : test_pow failed - 6");

		harness.check(!( !(new Double(Math.pow( 0.5, Double.NEGATIVE_INFINITY ))).isInfinite()), 
			"Error : test_pow failed - 7");

		harness.check(!( Math.pow( 1.5, Double.NEGATIVE_INFINITY ) != 0.0 ||
			 Math.pow( 0.5, Double.POSITIVE_INFINITY ) != 0.0), 
			"Error : test_pow failed - 8");

		harness.check(!( !(new Double(Math.pow( 1.0, Double.POSITIVE_INFINITY ))).isNaN()), 
			"Error : test_pow failed - 9");

		harness.check(!( Math.pow( 0.0, 1.0) != 0.0 ||
			 Math.pow( Double.POSITIVE_INFINITY , -1.0 ) != 0.0), 
			"Error : test_pow failed - 10");

		harness.check(!( !(new Double(Math.pow( 0.0, -1.0 ))).isInfinite() ||
			 !(new Double(Math.pow( Double.POSITIVE_INFINITY, 1.0 ))).isInfinite() ), 
			"Error : test_pow failed - 11");

		harness.check(!( Math.pow( -0.0, 5.0) != -0.0 ||
			 Math.pow( Double.NEGATIVE_INFINITY , -7.0 ) != -0.0), 
			"Error : test_pow failed - 12");

		harness.check(!( Math.pow( -2.0, 6.0) != Math.pow(2.0,6.0)), 
			"Error : test_pow failed - 13");

		harness.check(!( Math.pow( -2.0, 5.0) != -Math.pow(2.0,5.0)), 
			"Error : test_pow failed - 14");

		harness.check(!( !(new Double(Math.pow(-2.0 ,-3.4534))).isNaN()), 
			"Error : test_pow failed - 15");
		
	}

	public void test_IEEEremainder()
	{
		harness.check(!( !(new Double(Math.IEEEremainder( Double.NaN, 1.0 ))).isNaN()), 
			"Error :  test_IEEEremainder failed - 1");
		harness.check(!( !(new Double(Math.IEEEremainder( 1.0,Double.NaN))).isNaN()),  
			"Error :  test_IEEEremainder failed - 2");
		harness.check(!( !(new Double(Math.IEEEremainder( Double.POSITIVE_INFINITY , 2.0))).isNaN()), 
			"Error :  test_IEEEremainder failed - 3");
		harness.check(!( !(new Double(Math.IEEEremainder( 2.0,0.0))).isNaN() ), 
			"Error :  test_IEEEremainder failed - 4");
		harness.check(!( Math.IEEEremainder( 3.0, Double.POSITIVE_INFINITY ) != 3.0 ), 
			"Error :  test_IEEEremainder failed - 5");
		harness.check(!( Math.IEEEremainder( 3.0, Double.NEGATIVE_INFINITY ) != 3.0 ), 
			"Error :  test_IEEEremainder failed - 6");
		harness.check(new Double( Math.IEEEremainder( Double.NaN, Double.POSITIVE_INFINITY)).isNaN() , 
			"Error :  test_IEEEremainder failed - 7");
		harness.check(new Double( Math.IEEEremainder( Double.POSITIVE_INFINITY, Double.NaN)).isNaN() , 
			"Error :  test_IEEEremainder failed - 8");
		harness.check(new Double( Math.IEEEremainder( Double.NaN, Double.NEGATIVE_INFINITY)).isNaN() , 
			"Error :  test_IEEEremainder failed - 9");
		harness.check(new Double( Math.IEEEremainder( Double.NEGATIVE_INFINITY,Double.NaN)).isNaN() , 
			"Error :  test_IEEEremainder failed - 10");
		harness.check(new Double( Math.IEEEremainder( 0.0, Double.NaN )).isNaN(), 
			"Error :  test_IEEEremainder failed - 11");
		harness.check(new Double( Math.IEEEremainder( Double.NaN, 0.0)).isNaN() , 
			"Error :  test_IEEEremainder failed - 12");
		harness.check(new Double( Math.IEEEremainder(- 0.0, Double.NaN )).isNaN(), 
			"Error :  test_IEEEremainder failed - 13");
		harness.check(new Double( Math.IEEEremainder( Double.NaN,- 0.0)).isNaN() , 
			"Error :  test_IEEEremainder failed - 14");
		harness.check(new Double( Math.IEEEremainder( Double.NaN, Double.NaN)).isNaN() , 
			"Error :  test_IEEEremainder failed - 15");
	
	}
	
	public void test_ceil()
	{
		harness.check(!( Math.ceil(5.0) != 5.0 ), 
			"Error :  test_ceil failed - 1");

		harness.check(!( Math.ceil(0.0) != 0.0 || Math.ceil(-0.0) != -0.0 ), 
			"Error :  test_ceil failed - 2");

		harness.check(!( !(new Double(Math.ceil(Double.POSITIVE_INFINITY))).isInfinite() ||
			 !(new Double(Math.ceil(Double.NaN))).isNaN()), 
			"Error :  test_ceil failed - 3");
		harness.check(new Double(Math.ceil(Double.NEGATIVE_INFINITY)).isInfinite(), 
			"Error :  test_ceil failed - 4");

		harness.check(!( Math.ceil(-0.5) != -0.0 ), 
			"Error :  test_ceil failed - 5");

		harness.check(!( Math.ceil( 2.5 ) != 3.0 ), 
			"Error :  test_ceil failed - 6");
		harness.check(!( Math.ceil(0.5) != 1.0 ),
			"Error :  test_ceil failed - 7");


	}

	public void test_floor()
	{
		harness.check(!( Math.floor(5.0) != 5.0 ), 
			"Error :  test_floor failed - 1");

		harness.check(!( Math.floor(2.5) != 2.0 ), 
			"Error :  test_floor failed - 2");

		harness.check(!( !(new Double(Math.floor(Double.POSITIVE_INFINITY))).isInfinite() ||
			 !(new Double(Math.floor(Double.NaN))).isNaN()), 
			"Error :  test_floor failed - 3");
		harness.check(new Double(Math.floor(Double.POSITIVE_INFINITY)).isInfinite(), 
			"Error :  test_floor failed - 4");

		harness.check(!( Math.floor(0.0) != 0.0 || Math.floor(-0.0) != -0.0 ),
			"Error :  test_floor failed - 5");

		harness.check(!( Math.floor(-0.5) != -1.0 ),
			"Error :  test_floor failed - 6, got: "+Math.floor(-0.5));
		harness.check( Math.floor(-6.56566) == -7.0 ,
			"Error :  test_floor failed - 7, got: "+Math.floor(-6.56566)+" but exp.: -7.0");
		

	}

	public void test_rint()
	{	
		harness.check(!( Math.rint( 2.3 ) != 2.0 ), 
			"Error :  test_rint failed - 1");

		harness.check(!( Math.rint( 2.7 ) != 3.0 ), 
			"Error :  test_rint failed - 2");


		harness.check(!(Math.rint( 2.5) != 2.0 ), 
			"Error :  test_rint failed - 3");

		harness.check(!( Math.rint( 2.0) != 2.0 ), 
			"Error :  test_rint failed - 4");

		harness.check(!( Math.rint( 2.0) != 2.0 ), 
			"Error :  test_rint failed - 5");

		harness.check(!( !(new Double(Math.rint(Double.POSITIVE_INFINITY))).isInfinite() ||
			 !(new Double(Math.rint(Double.NaN))).isNaN()), 
			"Error :  test_rint failed - 6");
		harness.check(new Double(Math.rint(Double.POSITIVE_INFINITY)).isInfinite(), 
			"Error :  test_rint failed - 7");

		harness.check(!( Math.rint(0.0) != 0.0 || Math.rint(-0.0) != -0.0 ), 
			"Error :  test_rint failed - 8");
		float f1 = Integer.MIN_VALUE;
		f1 -= 5;
		harness.check(!( Math.rint(f1) != Integer.MIN_VALUE), 			  
			"Error :  test_round failed - 9");

	}

	public void test_round()
	{
	     harness.checkPoint("round(float)int");

		harness.check(!( Math.round( 3.4f ) != 3 ), 
			"Error :  test_round failed - 1");

		harness.check(!( Math.round( 9.55f ) != 10 ), 
			"Error :  test_round failed - 2");

		harness.check(!( Math.round(Float.NaN) != 0 ), 
			"Error :  test_round failed - 3");

		float f1 = Integer.MIN_VALUE;
		f1 -= 5;
		harness.check(!( Math.round(f1) != Integer.MIN_VALUE ||
			 Math.round(Float.NEGATIVE_INFINITY) != Integer.MIN_VALUE ), 
			"Error :  test_round failed - 4");

		f1 = Integer.MAX_VALUE;
		f1 += 5;
		harness.check(!( Math.round(f1) != Integer.MAX_VALUE ||
			 Math.round(Float.POSITIVE_INFINITY) != Integer.MAX_VALUE ), 
			"Error :  test_round failed - 5");

	     harness.checkPoint("round(double)long");	

		double d1 = Long.MIN_VALUE;
		d1 -= 5;
		harness.check(!( Math.round(d1) != Long.MIN_VALUE ||
			 Math.round(Double.NEGATIVE_INFINITY) != Long.MIN_VALUE ), 
			"Error :  test_round failed - 6");

		d1 = Long.MAX_VALUE;
		d1 += 5;
		harness.check(!( Math.round(d1) != Long.MAX_VALUE ||
			 Math.round(Double.POSITIVE_INFINITY) != Long.MAX_VALUE ), 
			"Error :  test_round failed - 7");


		harness.check(!( Math.round( 3.4 ) != 3 ), 
			"Error :  test_round failed - 8");

		harness.check(!( Math.round( 9.55 ) != 10 ), 
			"Error :  test_round failed - 9");

		harness.check(!( Math.round(Double.NaN) != 0 ), 
			"Error :  test_round failed - 10");

	}														  
/**
* This test will check on the randomgenerator
*	
* this test will be run 10 times
*
* we check on : values <1 and >= 0
* the properties of the values
*    the average should be 0.5
*    the distribution should be uniform
*    --> we divide the interval in 20 sections
*    and each section should contain 50 values
*
* since this will not be the cause since we only take 1000 values
* we tested on jdk to see how good there generator is, so ours needs to be 
* at least as good as theirs 
*  	doesn't fail	--> with avg +/- 2.5 %
*			--> with deviation +/- 2.5) %  	
*			--> min value < 0.0012
*			--> max value > 0.9986
*/ 
	public void test_random()
	{
		int [] s = new int [20];
		for (int i = 0 ; i < 20 ; i ++) { s[i] = 0; }		 	

		double minv = 1.0, maxv= 0.0;
		double sumv = 0.0, devv= 0.0;
		double rndv = 0.0;
		//double [] arndv = new double[1000];
		try 	{
			Exception e =new Exception();
			for ( int i=0; i < 10000; i++ )
				{ 
				rndv = Math.random();
				if ( ( rndv < 0 )||( rndv >= 1.0 ))
					throw (e); 
				sumv += rndv;
				devv += Math.abs( rndv - 0.5 );
				minv = Math.min( minv, rndv );
				maxv = Math.max( maxv, rndv );
				if ( rndv < 0.05 ) s[0]++;
				else { if ( rndv < 0.10 ) s[1]++;
				else { if ( rndv < 0.15 ) s[2]++;
				else { if ( rndv < 0.20 ) s[3]++;
				else { if ( rndv < 0.25 ) s[4]++;
				else { if ( rndv < 0.30 ) s[5]++;
				else { if ( rndv < 0.35 ) s[6]++;
				else { if ( rndv < 0.40 ) s[7]++;
				else { if ( rndv < 0.45 ) s[8]++;
				else { if ( rndv < 0.50 ) s[9]++;
				else { if ( rndv < 0.55 ) s[10]++;
				else { if ( rndv < 0.60 ) s[11]++;
				else { if ( rndv < 0.65 ) s[12]++;
				else { if ( rndv < 0.70 ) s[13]++;
				else { if ( rndv < 0.75 ) s[14]++;
				else { if ( rndv < 0.80 ) s[15]++;
				else { if ( rndv < 0.85 ) s[16]++;
				else { if ( rndv < 0.90 ) s[17]++;
				else { if ( rndv < 0.95 ) s[18]++;
				else s[19]++;
				}}}}}}}}}}}}}}}}}} 
				}
			}
		catch (Exception e) 
			{
			harness.fail("RandomGenerator generated value: "+ rndv);
			}
		harness.check (maxv > 0.9986, "maximum value is to small : "+maxv);
		harness.check (minv < 0.0012, "minimum value is to large : "+minv);
		harness.check (Math.abs(sumv-5000) < 120, 
			"average value is wrong: "+(sumv/10000)+" != 0.5");
		harness.check (Math.abs(devv-2500) < 58, 
			"the deviation is wrong: "+(devv/10000)+" != 0.25");
		int mins = 550, maxs = 450;
		double sigma = 0.0;
		for (int i = 0 ; i < 20 ; i++ )
			{
			sigma += s[i]*(s[i] - 1000) + 250000;
			mins   = Math.min( mins , s[i] );
			maxs   = Math.max( maxs , s[i] );
			}
		harness.check( mins > 395 , 
			"distribution is not uniform "+mins+" smallest section" );
		harness.check( maxs < 610 , 
			"distribution is not uniform "+maxs+" largest section");			
		sigma = Math.sqrt(sigma/20);
		harness.check( sigma < 36.5 , "the standard deviation is to big -- "+sigma );

		harness.debug("\tmaxv = "+maxv+"\n\tminv = "+minv+"\n\tavgv = "+(sumv/10000)+"\n\tdevv = "+(devv/10000));
		harness.debug("maxs = "+maxs+"\n\tmins = "+mins+"\n\tsigma = "+(Math.sqrt(sigma/20)));		

		harness.debug("\t"+s[0]+"\t"+s[1]+"\t"+s[2]+"\t"+s[3]+"\t"+s[4]+"\n\t"+s[5]+"\t"+s[6]+"\t"+s[7]+"\t"+s[8]+"\t"+s[9]);
		harness.debug("\t"+s[10]+"\t"+s[11]+"\t"+s[12]+"\t"+s[13]+"\t"+s[14]+"\n\t"+s[15]+"\t"+s[16]+"\t"+s[17]+"\t"+s[18]+"\t"+s[19]);

	}
	public void test_abs()
	{
	     harness.checkPoint("abs(int)int");
		harness.check(!( Math.abs( 10 ) != 10 ),  
			"Error :  test_abs failed - 1");

		harness.check(!( Math.abs( -23 ) != 23 ), 
			"Error :  test_abs failed - 2");

		harness.check(!( Math.abs( Integer.MIN_VALUE ) != Integer.MIN_VALUE ), 
			"Error :  test_abs failed - 3" );
		
		harness.check(!( Math.abs(-0) != 0 ), 
			"Error :  test_abs failed - 4" );

	     harness.checkPoint("abs(long)long");
		harness.check(!( Math.abs( 1000L ) != 1000 ),  
			"Error :  test_abs failed - 5");

		harness.check(!( Math.abs( -2334242L ) != 2334242 ), 
			"Error :  test_abs failed - 6");

		harness.check( Math.abs( Long.MIN_VALUE ) ,Long.MIN_VALUE ,
			"Error :  test_abs failed - 7");
		

	     harness.checkPoint("abs(float)float");
		harness.check(!( Math.abs( 0.0f ) != 0.0f || Math.abs(-0.0f) != 0.0f ), 
			"Error :  test_abs failed - 8" );
		
		harness.check(!( !(new Float(Math.abs( Float.POSITIVE_INFINITY ))).isInfinite() ), 
			"Error :  test_abs failed - 9a" );
		harness.check(!( !(new Float(Math.abs( Float.NEGATIVE_INFINITY ))).isInfinite() ), 
			"Error :  test_abs failed - 9b" );

		harness.check(!( !(new Float(Math.abs( Float.NaN ))).isNaN() ), 
			"Error :  test_abs failed - 10" );

		harness.check(!( Math.abs( 23.34f ) != 23.34f ), 
			"Error :  test_abs failed - 11a" );
		harness.check(!( Math.abs(- 23.34f ) != 23.34f ), 
			"Error :  test_abs failed - 11b" );

	     harness.checkPoint("abs(double)double");
		harness.check(!( Math.abs( 0.0 ) != 0.0 || Math.abs(-0.0) != 0.0 ), 
			"Error :  test_abs failed - 12" );
		
		harness.check(!( !(new Double(Math.abs( Double.POSITIVE_INFINITY ))).isInfinite() ), 
			"Error :  test_abs failed - 13a" );
		harness.check(!( !(new Double(Math.abs( Double.NEGATIVE_INFINITY ))).isInfinite() ), 
			"Error :  test_abs failed - 13b" );

		harness.check(!( !(new Double(Math.abs( Double.NaN ))).isNaN() ), 
			"Error :  test_abs failed - 14" );

		harness.check(!( Math.abs( 23.34 ) != 23.34 ), 
			"Error :  test_abs failed - 15" );
		harness.check(!( Math.abs(-23.34 ) != 23.34 ), 
			"Error :  test_abs failed - 16" );

	}

	public void test_min()
	{

	     harness.checkPoint("min(int,int)int");
		harness.check(!( Math.min( 100 , 12 ) != 12 ),  
			"Error :  test_min failed - 1" );

		harness.check(!( Math.min( Integer.MIN_VALUE , Integer.MIN_VALUE + 1 ) != Integer.MIN_VALUE ), 
			"Error :  test_min failed - 2" );

		harness.check(!( Math.min( Integer.MAX_VALUE , Integer.MAX_VALUE -1 ) != Integer.MAX_VALUE -1 ), 
			"Error :  test_min failed - 3" );
			
		harness.check(!( Math.min( 10 , 10 ) != 10 ), 
			"Error :  test_min failed - 4" );

		harness.check(!( Math.min( 0 , -0 ) != -0 ), 
			"Error :  test_min failed - 5" );

	     harness.checkPoint("min(long,long)long");
	
		harness.check(!( Math.min( 100L , 12L ) != 12L ),  
			"Error :  test_min failed - 6" );

		harness.check(!( Math.min( Long.MIN_VALUE , Long.MIN_VALUE + 1 ) != Long.MIN_VALUE ), 
			"Error :  test_min failed - 7" );

		harness.check(!( Math.min( Long.MAX_VALUE , Long.MAX_VALUE -1 ) != Long.MAX_VALUE -1 ), 
			"Error :  test_min failed - 8" );
			
		harness.check(!( Math.min( 10L , 10L ) != 10L ), 
			"Error :  test_min failed - 9" );

		harness.check(!( Math.min( 0L , -0L ) != -0L ), 
			"Error :  test_min failed - 10" );

	     harness.checkPoint("min(float,float)float");
		harness.check(!( Math.min( 23.4f , 12.3f ) != 12.3f ),  
			"Error :  test_min failed - 11" );

		harness.check(!( !(new Float(Math.min( Float.NaN ,  1.0f ))).isNaN()  ), 
			"Error :  test_min failed - 12a" );
		harness.check(!( !(new Float(Math.min( 1.0f, Float.NaN  ))).isNaN()  ), 
			"Error :  test_min failed - 12b" );

		harness.check(!( Math.min( 10.0f , 10.0f ) != 10.0f ), 
			"Error :  test_min failed - 13" );

		harness.check(!( Math.min( 0.0f , -0.0f ) != -0.0f ), 
			"Error :  test_min failed - 14a" );
		harness.check(!( Math.min(- 0.0f , 0.0f ) != -0.0f ), 
			"Error :  test_min failed - 14b" );
		harness.check( Math.min(Float.NEGATIVE_INFINITY, 2.3f)== Float.NEGATIVE_INFINITY,
			"Error : test should return Neg. Inf.");
		harness.check( Math.min(Float.POSITIVE_INFINITY, 2.3f)==2.3f,
			"Error : Pos. Inf.should be treated as value");
		harness.check( Math.min(Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY )==Float.POSITIVE_INFINITY,
			"Error : Pos. Inf.should be treated as value");

	     harness.checkPoint("min(double,double)double");
		harness.check(!( Math.min( 23.4 , 12.3 ) != 12.3 ),  
			"Error :  test_min failed - 15" );

		harness.check(!( !(new Double(Math.min( Double.NaN ,  1.0 ))).isNaN()  ), 
			"Error :  test_min failed - 16a, got: "+(new Double(Math.min( Double.NaN ,  1.0 ))) );
		harness.check(!( !(new Double(Math.min( 1.0, Double.NaN ))).isNaN()  ), 
			"Error :  test_min failed - 16b, got: "+(new Double(Math.min( 1.0, Double.NaN ))) );

		harness.check(!( Math.min( 10.0 , 10.0 ) != 10.0 ), 
			"Error :  test_min failed - 17" );

		harness.check(!( Math.min( 0.0 , -0.0 ) != -0.0 ), 
			"Error :  test_min failed - 18" );
		harness.check(!( Math.min( -0.0 , 0.0 ) != -0.0 ), 
			"Error :  test_min failed - 18" );
		harness.check( Math.min(Double.NEGATIVE_INFINITY, 2.3)== Double.NEGATIVE_INFINITY,
			"Error : test should return Neg. Inf.");
		harness.check( Math.min(Double.POSITIVE_INFINITY, 2.3f)==2.3f,
			"Error : Pos. Inf.should be treated as value");
		harness.check( Math.min(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY )== Double.POSITIVE_INFINITY,
			"Error : Pos. Inf.should be treated as value");

	}

	public void test_max()
	{

	     harness.checkPoint("max(int,int)int");
		harness.check(!( Math.max( 100 , 12 ) != 100 ),  
			"Error :  test_max failed - 1" );

		harness.check(!( Math.max( Integer.MAX_VALUE , Integer.MAX_VALUE - 1 ) != Integer.MAX_VALUE ), 
			"Error :  test_max failed - 2" );

		harness.check(!( Math.max( Integer.MIN_VALUE , Integer.MIN_VALUE + 1 ) != Integer.MIN_VALUE +1 ), 
			"Error :  test_max failed - 3" );
			
		harness.check(!( Math.max( 10 , 10 ) != 10 ), 
			"Error :  test_max failed - 4" );

		harness.check(!( Math.max( 0 , -0 ) != 0 ), 
			"Error :  test_max failed - 5" );


	     harness.checkPoint("max(long,long)long");
		harness.check(!( Math.max( 100L , 12L ) != 100L ),  
			"Error :  test_max failed - 6" );

		harness.check(!( Math.max( Long.MAX_VALUE , Long.MAX_VALUE - 1 ) != Long.MAX_VALUE ), 
			"Error :  test_max failed - 7" );

		harness.check(!( Math.max( Long.MIN_VALUE , Long.MIN_VALUE +1 ) != Long.MIN_VALUE + 1 ), 
			"Error :  test_max failed - 8" );
			
		harness.check(!( Math.max( 10L , 10L ) != 10L ), 
			"Error :  test_max failed - 9" );

		harness.check(!( Math.max( 0L , -0L ) != 0L ), 
			"Error :  test_max failed - 10" );

	     harness.checkPoint("max(float,float)float");		
		harness.check(!( Math.max( 23.4f , 12.3f ) != 23.4f ),  
			"Error :  test_max failed - 11" );

		harness.check(!( !(new Float(Math.max( Float.NaN ,  1.0f ))).isNaN()  ), 
			"Error :  test_max failed - 12a" );
		harness.check(!( !(new Float(Math.max( 1.0f, Float.NaN ))).isNaN()  ), 
			"Error :  test_max failed - 12b" );

		harness.check(!( Math.max( 10.0f , 10.0f ) != 10.0f ), 
			"Error :  test_max failed - 13" );

		harness.check(!( Math.max( 0.0f , -0.0f ) != 0.0f ), 
			"Error :  test_max failed - 14a" );
		harness.check(!( Math.max( -0.0f , 0.0f ) != 0.0f ), 
			"Error :  test_max failed - 14b" );
		harness.check( Math.max(Float.NEGATIVE_INFINITY, 2.3f)==2.3f,
			"Error : test should treat Neg. Inf. as number");
		harness.check( Math.max(Float.POSITIVE_INFINITY,2.3f)==Float.POSITIVE_INFINITY,
			"Error : Pos. Inf.should be treated as value");
		harness.check( Math.max(Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY)==Float.NEGATIVE_INFINITY,
			"Error : Neg. Inf.should be treated as value");

	     harness.checkPoint("max(double,double)double");
		harness.check(!( Math.max( 23.4 , 12.3 ) != 23.4 ),  
			"Error :  test_max failed - 15" );

		harness.check(!( !(new Double(Math.max( Double.NaN ,  1.0 ))).isNaN()  ), 
			"Error :  test_max failed - 16a, got: "+(new Double(Math.max( Double.NaN ,  1.0 ))) );
		harness.check(!( !(new Double(Math.max( 1.0, Double.NaN ))).isNaN()  ), 
			"Error :  test_max failed - 16b, got: "+(new Double(Math.max( 1.0, Double.NaN ))) );

		harness.check(!( Math.max( 10.0 , 10.0 ) != 10.0 ), 
			"Error :  test_max failed - 17" );

		harness.check(!( Math.max( 0.0 , -0.0 ) != 0.0 ), 
			"Error :  test_max failed - 18" );
		harness.check(!( Math.max( -0.0 , 0.0 ) != 0.0 ), 
			"Error :  test_max failed - 18" );
		harness.check( Math.max(Double.NEGATIVE_INFINITY, 2.3)==2.3,
			"Error : test should treat Neg. Inf. as number");
		harness.check( Math.max(Double.POSITIVE_INFINITY,2.3)==Double.POSITIVE_INFINITY,
			"Error : Pos. Inf.should be treated as value");
		harness.check( Math.max(Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY)==Double.NEGATIVE_INFINITY,
			"Error : Neg. Inf.should be treated as value");
	}


	public void testall()
	{
		harness.setclass("java.lang.Math");
		test_Basics();
		test_sincostan();
		test_asinacosatan();
		test_atan2();
		harness.checkPoint("log(double)double");
		test_log();
		harness.checkPoint("exp(double)double");
		test_exp();
		harness.checkPoint("sqrt(double)double");
		test_sqrt();
		harness.checkPoint("pow(double,double)double");
		test_pow();
		harness.checkPoint("IEEEremainder(double,double)double");
		test_IEEEremainder();
		harness.checkPoint("ceil(double)double");
		test_ceil();
		harness.checkPoint("floor(double)double");
		test_floor();
		harness.checkPoint("rint(double)double");
		test_rint();
		test_round();
		harness.checkPoint("random()double");
		for ( int j = 0 ; j < 10 ; j ++ )
		{  test_random();  }
		test_abs();
		test_min();
		test_max();
	}

  public void test (TestHarness the_harness)
  {
    harness = the_harness;
    testall ();
  }

}
