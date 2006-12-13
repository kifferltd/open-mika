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

package gnu.testlet.wonka.lang.Double;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class DoubleTest implements Testlet
{

  protected static TestHarness harness;
	public void test_Basics()
	{
		harness.checkPoint("Double(double)");
		Double i1 = new Double(100.5);
		harness.check(!( i1.doubleValue() != 100.5 ));
		
		try {
  		harness.check(!( (new Double("234.34")).doubleValue() != 234.34 ),"got: "+(new Double("234.34")).doubleValue() );
  		harness.check(new Double("1.4e-45").doubleValue() , 1.4e-45 ,"expected: 1.4e-45, got: "+(new Double("1.4e-45")) );
		harness.check(!( (new Double(3.4)).doubleValue() != 3.4 ) );
		}catch ( NumberFormatException e ){
			harness.check(false );
		}

		try {
			new Double("babu");
			harness.check(false,"should throw exception");
		}
		catch ( NumberFormatException e )
		{
			harness.check(true);
		}

		double min1 = 5e-324;
		double min2 = Double.MIN_VALUE;
		double max1 = 1.7976931348623157e+308;
		double max2 = Double.MAX_VALUE;
		double ninf1 = -1.0/0.0;
		double ninf2 = Double.NEGATIVE_INFINITY;
		double pinf1 = 1.0/0.0;
		double pinf2 = Double.POSITIVE_INFINITY;
		Double nan1 = new Double(0.0/0.0);
		Double nan2 = new Double(Double.NaN);

		//harness.checkPoint("Minimum & maximum values");
		harness.check(!( min1 != min2 ));
		harness.check(!( max1 != max2 ));
		harness.check(!(ninf1 != ninf2));
		harness.check(!(pinf1 != pinf2));
		
		harness.checkPoint("isNaN()boolean");
		harness.check(!( !nan1.isNaN()) );
		harness.check(!( (new Double(10.0)).isNaN()),"got: "+new Double(10.0).toString() );
		harness.check(!( !nan2.isNaN()) );
		harness.checkPoint("isNaN(double)boolean");
		harness.check(!( !Double.isNaN( Double.NaN )) );
//		harness.check(!(!nan2.equals(nan1)) );

		harness.checkPoint("isInfinite()boolean");
		harness.check(Double.isInfinite( pinf1));
		harness.check(Double.isInfinite(pinf2 ));
		harness.check(Double.isInfinite(ninf1));
		harness.check(Double.isInfinite( Double.POSITIVE_INFINITY));
		harness.check(Double.isInfinite(ninf2 ));
		harness.check(Double.isInfinite( Double.NEGATIVE_INFINITY));
		
		harness.checkPoint("isInfinite(double)boolean");
		harness.check((new Double(pinf1)).isInfinite());
		harness.check((new Double(pinf2)).isInfinite());
		harness.check((new Double(ninf1)).isInfinite());
		harness.check((new Double(Double.POSITIVE_INFINITY)).isInfinite());
		harness.check((new Double(ninf2)).isInfinite());
		harness.check((new Double(Double.NEGATIVE_INFINITY)).isInfinite());

		
		//harness.checkPoint("adding and subtracting zero");
		harness.check(!( 0.0 - 0.0 != 0.0) );
		harness.check(!( 0.0 + 0.0 != 0.0) );
		harness.check(!( 0.0 + -0.0 != 0.0));
		harness.check(!( 0.0 - -0.0 != 0.0));
		harness.check(!( -0.0 - 0.0 != -0.0));
		harness.check(!( -0.0 + 0.0 != 0.0));
		harness.check(!( -0.0 + -0.0 != -0.0));
		harness.check(!( -0.0 - -0.0 != 0.0) );

		harness.check(!( !"0.0".equals(0.0 - 0.0 +"" )));
	}

	public void test_toString()
	{
		Double D1 = new Double(23.04);
		Double D2 = new Double(-44.5343);
		harness.checkPoint("toString()java.lang.String");
		harness.check(!( !( new Double(123.0)).toString().equals("123.0")),"got: "+( new Double(123.0)).toString());
		harness.check(!( !( new Double(-44.5343)).toString().equals("-44.5343")),"got: "+( new Double(-44.5343)).toString());
		harness.check(!( !D1.toString().equals ("23.04" )),"got: "+D1.toString());
		harness.check(!( !D2.toString().equals ("-44.5343" )),"got: "+D2.toString());
		harness.checkPoint("toString(double)java.lang.String");	
		harness.check(!( !Double.toString( 1.0e-7 ).equals ("1.0E-7" )), "expected: 1.0E-7, got: "+Double.toString( 1.0e-7 ));		
		harness.check(!( !Double.toString( 1.0e7 ).equals ("1.0E7" )), "expected: 1.0E7, got: "+Double.toString( 1.0e7 ));		
		harness.check(!( !Double.toString( -9.999999e-4 ).equals ("-9.999999E-4" )), "expected: -9.999999E-4, got: "+Double.toString( -9.999999e-4 ));		
		harness.check(!( !Double.toString( -9.999999e14 ).equals ("-9.999999E14" )), "expected: -9.999999E14 got: "+Double.toString( -9.999999e14 ));		
		harness.check(!( !Double.toString( 9.999999e-4 ).equals ("9.999999E-4" )), "expected: 9.999999E-4, got: "+Double.toString( 9.999999e-4 ));		
		harness.check(!( !Double.toString( 9.999999e14 ).equals ("9.999999E14" )), "expected: 9.999999E14, got: "+Double.toString( 9.999999e14 ));		
		harness.check(!( !Double.toString( -1.0e-7 ).equals ("-1.0E-7" )), "expected: -1.0E-7, got: "+Double.toString( -1.0e-7 ));		
		harness.check(!( !Double.toString( -1.0e7 ).equals ("-1.0E7" )), "expected: -1.0E7, got: "+Double.toString( -1.0e7 ));		
		harness.check(!( !Double.toString( 9.343e27 ).equals ("9.343E27" )), "expected: 9.343E27, got: "+Double.toString( 9.343e27 ));		
		harness.check(!( !Double.toString( 1.65654e-19 ).equals ("1.65654E-19" )), "expected: 1.65654E-19, got: "+Double.toString( 1.65654e-19 ));				
		harness.check(!( !Double.toString( -9.343e27 ).equals ("-9.343E27" )), "expected: -9.343E27, got: "+Double.toString( -9.343e27 ));		
		harness.check(!( !Double.toString( -1.65654e-19 ).equals ("-1.65654E-19" )), "expected: -1.65654E-19, got: "+Double.toString( -1.65654e-19 ));				
		harness.check(!( !Double.toString( 23.04 ).equals ("23.04" )), "expected: 23.04, got: "+Double.toString(23.04));
		harness.check(!( !Double.toString(-44.5343).equals ("-44.5343" )), "expected: -44.5343, got: "+Double.toString(-44.5343));
		harness.check(!( !Double.toString( 123.0 ).equals ("123.0" )), "expected: 123.0, got: "+Double.toString(123.0));
		harness.check(!( !Double.toString( 0.0 ).equals ("0.0" )), "expected: 0.0, got: "+Double.toString(0.0));
		harness.check(!( !Double.toString( -0.0 ).equals ("-0.0" )), "expected: -0.0, got: "+Double.toString(-0.0));
		String str = Double.toString( -9412128.34 );
		harness.check(!( !str.startsWith("-9412128.3" )),
			"Error: test_toString failed - 9, got: "+str+"exp -9412128.34" );
/*    D1 = new Double(1.001);
		harness.check(D1.toString().equals("1.001" ));
		harness.check(Double.toString( 1.001 ).equals ("1.001" ));
		D1 = new Double(-1.001);
		harness.check(D1.toString().equals ("-1.001" ));
		harness.check(Double.toString( -1.001 ).equals ("-1.001" ));
		D1 = new Double(1e4d);
		harness.check(!( !(D1.toString()).equals ("10000" )));
		harness.check(!( !Double.toString( 1e4d ).equals ("10000" )));
		D1 = new Double(-1e4d);
		harness.check(!( !(D1.toString()).equals ("-10000" )));
		harness.check(!( !Double.toString( -1e4d ).equals ("-10000" )));
		D1 = new Double(1e4);
		harness.check(!( !(D1.toString()).equals ("10000" )));
		harness.check(!( !Double.toString( 1E4 ).equals ("10000" )));
		D1 = new Double(-1E4);
		harness.check(!( !(D1.toString()).equals ("-10000" )));
		harness.check(!( !Double.toString( -1E4 ).equals ("-10000" )));
		D1 = new Double(1e-4);
		harness.check(!( !(D1.toString()).equals ("0.0001" )));
		harness.check(!( !Double.toString( 1E-4 ).equals ("0.0001" )));
		D1 = new Double(-1E-4);
		harness.check(!( !(D1.toString()).equals ("-0.0001" )));
		harness.check(!( !Double.toString( -1E-4 ).equals ("-0.0001" )));
		D1 = new Double(1.2e4);
		harness.check(!( !(D1.toString()).equals ("12000" )));
		harness.check(!( !Double.toString( 1.2E4 ).equals ("12000" )));
		D1 = new Double(-1.2E-4);
		harness.check(!( !(D1.toString()).equals ("-0.00012" )));
		harness.check(!( !Double.toString( -1.2E-4 ).equals ("-0.00012" )));
	
		D1 = new Double(1.243E10);
		harness.check((D1.toString()).equals ("1.24E10" ));
*/		

		harness.check(Double.toString( Double.NaN ).equals ("NaN" ) );
		harness.check(Double.toString( Double.POSITIVE_INFINITY ).equals ("Infinity" ));
		harness.check(Double.toString( Double.NEGATIVE_INFINITY ).equals ("-Infinity" ));
		
	}

	public void test_equals()
	{
		harness.checkPoint("equals(java.lang.Object)boolean");
		Double i1 = new Double(1234.56E4);
		Double i2 = new Double(-1234.56E4);
    		Double i3 = new Double(12345600);
   	 	Double i4 = new Double(12345610.1);
    		Double i5 = new Double(-1200);
		harness.check(i1.equals( new Double(1234.56E4)));
		harness.check(i2.equals( new Double(-1234.56E4)));
		
		harness.check(!(i1.equals(null)) );
		harness.check(!(i1.equals(i2)) );
		harness.check(i1.equals(i3) );
		harness.check(i3.equals(i1) );		

		harness.checkPoint("compareTo(java.lang.Double)int");
		harness.check(i1.compareTo(i3)== 0.0);
		harness.check(i1.compareTo(i4) < 0.0);
		harness.check(i1.compareTo(i5) > 0.0);
		
		harness.checkPoint("equals(java.lang.Object)boolean");
		double n1 = Double.NaN;
		double n2 = Double.NaN;
		harness.check(!( n1 == n2 ) );

		Double flt1 = new Double( Double.NaN);
		Double flt2 = new Double( Double.NaN);
		harness.check(!( !flt1.equals(flt2)) );

		harness.check(!( 0.0 != -0.0 ) );
		Double pzero = new Double( 0.0 );
		Double nzero = new Double( -0.0 );
		harness.check(!( pzero.equals(nzero) ) );

	}


	public void test_hashCode( )
	{
		harness.checkPoint("hashCode()int");
		Double flt1 = new Double(3.4028235e+38);
		long lng1 = Double.doubleToLongBits( 3.4028235e+38);

		harness.check(!( flt1.hashCode() != (int) ( lng1^(lng1>>>32)) ));

		Double flt2 = new Double( -2343323354.0 );
		long lng2 = Double.doubleToLongBits( -2343323354.0 );

		harness.check(!( flt2.hashCode() != (int) ( lng2^(lng2>>>32)) ));
	}

	public void test_toValues( )
	{
		Double d1 = new Double(3.4E32);
		Double d2 = new Double(-23.45);
		Double d3 = new Double(3000.54);
		Double d4 = new Double(32735.3249);
		Double d5 = new Double(-32735.3249);
		Double d6 = new Double(0.0);
    		Double d7 = new Double(30.0);


		harness.checkPoint("byteValue()byte");
		harness.check(!( d2.byteValue() != -23 ) );
		//harness.check(!( d2.byteValue() != -24 ) );		
		harness.check(!( d3.byteValue() != -72 ),"got: "+d3.byteValue() );
		harness.check(!( d6.byteValue() != 0 ) );
		harness.check(!( d7.byteValue() != 30 ));
		
		harness.checkPoint("shortValue()short");
		harness.check(!( d2.shortValue() != -23 ) );
		//harness.check(!( d2.shortValue() != -24 ) );

		harness.check(!( d3.shortValue() != ((short)3000.54)),"got: "+d3.shortValue()  );
		harness.check(!( d6.shortValue() != 0 ) );


		harness.checkPoint("intValue()int");
		int i1 = d1.intValue();
		int i2 = d2.intValue();
    		harness.check(!( i1 != Integer.MAX_VALUE));
		harness.check(!( i2 != (int) -23.45 ) );
				
		harness.check(!( d3.intValue() != ((int)3000.54)  ),"got: "+d3.intValue()+",expected: "+((int)3000.54) );
		harness.check(!( d4.intValue() != 32735  ) );
		harness.check(!( d5.intValue() != -32735  ) );
		harness.check(!( d6.intValue() != 0  ) );
		
		harness.checkPoint("longValue()long");
		harness.check(!( d1.longValue() != (long) 3.4e32),"got: "+d1.longValue()+",expected: "+((long) 3.4e32) );
		harness.check(!( d2.longValue() != (long) -23.45 ),"got: "+d2.longValue()+",expected: "+((long) -23.45)  );
		harness.check(!( d3.longValue() != (long)3000.54 ),"got: "+d3.longValue()+",expected: "+((long)3000.54 )  );
		harness.check(!( d7.longValue() != 30L ),"got: "+d7.longValue()+",expected: "+(30L) );
		
		harness.checkPoint("floatValue()float");
		harness.check(!( d2.floatValue() != -23.45f ));
		harness.check(!( d3.floatValue() != 3000.54f ));
		harness.check(!( d6.floatValue() != 0.0f ));
		harness.check(!( d7.floatValue() != 30.0f ));
		
		harness.checkPoint ("doubleValue()double");
		harness.check(!( d4.doubleValue() != 32735.3249 ) );
		harness.check(!( d5.doubleValue() != -32735.3249 ) );
		harness.check(!( d6.doubleValue() != 0.0 ) );
		harness.check(!( d7.doubleValue() != 30.0 ) );
	}

	public void test_valueOf( )
	{
		harness.checkPoint("valueOf(java.lang.String)java.lang.Double");

		harness.check(!( Double.valueOf( "3.4e+32" ).doubleValue() != 3.4e32 ),"got: "+Double.valueOf( "3.4e+32" ).doubleValue());
		harness.check(!( Double.valueOf(" -23.45").doubleValue() != -23.45 ),"got: "+Double.valueOf(" -23.45").doubleValue());
		harness.check(!( Double.valueOf(" 23.45").doubleValue() != 23.45 ),"got: "+Double.valueOf(" 23.45").doubleValue());
		harness.check(!( Double.valueOf("-23.45  ").doubleValue() != -23.45 ),"got: "+Double.valueOf("-23.45  ").doubleValue());
		harness.check(!( Double.valueOf("23.45  ").doubleValue() != 23.45 ),"got: "+Double.valueOf("23.45  ").doubleValue());
		harness.check(!( Double.valueOf(" +23.45  ").doubleValue() != 23.45 ),"got: "+Double.valueOf(" +23.45  ").doubleValue());
		harness.check(!( Double.valueOf(" -23.45  ").doubleValue() != -23.45 ),"got: "+Double.valueOf(" -23.45  ").doubleValue());
		harness.check(!( Double.valueOf("  23.45  ").doubleValue() != 23.45 ),"got: "+Double.valueOf(" 23.45  ").doubleValue());
		
		try {
			Double.valueOf(null);
			harness.check(false);
		}catch ( NullPointerException e )
		{	harness.check(true);}

		try {
			Double.valueOf("Kona");
			harness.check(false);
		}catch( NumberFormatException e) {harness.check(true);}
	}

	public void test_doubleToLongBits()
	{
		harness.checkPoint("doubleToLongBits(double)long");

		long i1 = Double.doubleToLongBits(3.4e+32f);
		long i2 = Double.doubleToLongBits(-34.56f);

		//I3E standard: couble is repersented as a 32-bit long value:
		// 1st bit = sign bit
		long sign1 = i1 & 0x8000000000000000L ;
		long sign2 = i2 & 0x8000000000000000L ;
    //next 11 bits: exponent
		long exp1 = i1 & 0x7ff0000000000000L ;
		long exp2 = i2 & 0x7ff0000000000000L ;

		long man1 = i1 & 0x000fffffffffffffL ;
		long man2 = i2 & 0x000fffffffffffffL ;

		harness.check(!(sign1 != 0 ));
		harness.check(!( sign2 != 0x8000000000000000L ) );

		harness.check(!( exp1 != 5093571178556030976L ));
		harness.check(!( exp2 != 4629700416936869888L ));

		harness.check(!( man1 != 214848222789632L ));
		harness.check(!( man2 != 360288163463168L ));
		
		harness.check(!( Double.doubleToLongBits( Double.POSITIVE_INFINITY ) != 0x7ff0000000000000L ));
		harness.check(!( Double.doubleToLongBits( Double.NEGATIVE_INFINITY ) != 0xfff0000000000000L ));

		long nanval = Double.doubleToLongBits( Double.NaN );
		harness.check(nanval , 0x7ff8000000000000L ,"Value Double.NaN");
		harness.debug("NaN = "+Long.toHexString(nanval)+", but should be 0x7ff8000000000000");
		harness.checkPoint("longBitsToDouble(long)double");
		double fl1 = Double.longBitsToDouble( 0x34343f33 );
		harness.check ( !(Double.doubleToLongBits(fl1) != 0x34343f33 ) );

		harness.check(!( Double.doubleToLongBits( Double.longBitsToDouble(0x33439943)) != 0x33439943 ));
			
		
		harness.check(!( Double.longBitsToDouble( 0x7ff0000000000000L) != Double.POSITIVE_INFINITY ));
		harness.check(!( Double.longBitsToDouble( 0xfff0000000000000L) != Double.NEGATIVE_INFINITY ));

		harness.check(!( !Double.isNaN(Double.longBitsToDouble( 0xfff8000000000000L ))));
		harness.check(!( !Double.isNaN(Double.longBitsToDouble( 0x7ffffff000000000L ))));
		harness.check(!( !Double.isNaN(Double.longBitsToDouble( 0xfff8000020000001L ))));
		harness.check(!( !Double.isNaN(Double.longBitsToDouble( 0xfffffffffffffff1L ))));
	}

	public void check_remainder( double val, double val1 , double ret)
	{
		double res = val % val1;
		harness.check(!( res < ret - 0.001 || res > ret + 0.001 ),"did "+val+" % "+val1+" and got: "+res+", expected "+ret);
	}

	public void check_remainder_NaN( double val, double val1 )
	{
		double res = val % val1;
		harness.check (Double.isNaN(res),"got: "+res);
	}

	public void test_remainder( )
	{
		harness.checkPoint("remainder--> operator");
		check_remainder(15.2 , 1.0 , 0.2 );
		check_remainder(2345.2432 , 1.2 ,0.44319999999997  );
		check_remainder(20.56 , 1.87 ,1.86 );
		check_remainder(20.56 , -1.87 ,1.86 );
		check_remainder(-20.56 , 1.87 ,-1.86 );
		check_remainder(-20.56 , -1.87 ,-1.86 );
		check_remainder(0.0 , 1.2 , 0.00000000000000  );
		check_remainder(1000.0 , 10.0 , 0.00000000000000  );
		check_remainder(234.332 , 134.34 , 99.992 );
		check_remainder(1.0 , 1.0, 0.0  );
		check_remainder(1.0 , -1.0, 0.0  );
		check_remainder(45.0 , 5.0, 0.0 );
		check_remainder(1.25 , 0.50 , 0.25 );
		check_remainder(-1.25 , 0.50 , -0.25 );
		check_remainder(-1.25 , -0.50 , -0.25 );
		check_remainder(1.25 , -0.50 , 0.25 );
		check_remainder(12345.678, 1234.5678, 0.0 );
               
/*               if (!System.getProperty("os.name").equals("VxWorks")){
                  // bug EJWcr00686, has not been fixed yet.
                  // Test is disabled for smallvm 2.0.1 release.
                  check_remainder(Double.MAX_VALUE , Double.MIN_VALUE , 0.00000000000000 , 11 );
                }
*/
   		harness.checkPoint("%(double,double)double");
   		check_remainder(Double.MAX_VALUE , Double.MIN_VALUE , 0.0);
    		check_remainder(0.0 , 999.99, 0.0);
		check_remainder(123.0 , 25.0 , 23.0 );
		check_remainder(15.0 , 1.5 , 0.0 );
		

		harness.checkPoint("remainder NaN");
		check_remainder_NaN(Double.NaN, 1.5 );
		check_remainder_NaN(1.5, Double.NaN);
		check_remainder_NaN(Double.NaN, -0.0 );
		check_remainder_NaN(0.0, Double.NaN );
		check_remainder_NaN(Double.POSITIVE_INFINITY, 1.5 );
		check_remainder_NaN(Double.NEGATIVE_INFINITY, 1.5 );
		check_remainder_NaN(1.5, 0.0);
		check_remainder_NaN(1.5, -0.0);
		check_remainder_NaN(Double.POSITIVE_INFINITY, 0.0 );
		check_remainder_NaN(Double.NEGATIVE_INFINITY, 0.0 );
		check_remainder_NaN(Double.POSITIVE_INFINITY, -0.0 );
		check_remainder_NaN(Double.NEGATIVE_INFINITY, -0.0 );


		harness.checkPoint("remainder with infinity");
		check_remainder(15.0 , Double.POSITIVE_INFINITY, 15.0 );
    		check_remainder(-15.0 , Double.POSITIVE_INFINITY, -15.0 );
    		check_remainder(0.0 , Double.POSITIVE_INFINITY, 0.0   );
    		check_remainder(-0.0 , Double.POSITIVE_INFINITY, -0.0 );
    		check_remainder(0.1 , Double.POSITIVE_INFINITY, 0.1   );
    		check_remainder(-0.1 , Double.POSITIVE_INFINITY, -0.1 );

    		check_remainder(15.0 , Double.NEGATIVE_INFINITY, 15.0 );
    		check_remainder(-15.0 , Double.NEGATIVE_INFINITY, -15.0 );
    		check_remainder(0.0 , Double.NEGATIVE_INFINITY, 0.0  );
    		check_remainder(-0.0 , Double.NEGATIVE_INFINITY, -0.0);
    		check_remainder(0.1 , Double.NEGATIVE_INFINITY, 0.1  );
    		check_remainder(-0.1 , Double.NEGATIVE_INFINITY, -0.1);
	}


	public void test_negatives()
	{
	    harness.setclass("java.lang.String");
	    harness.checkPoint("valueOf(double)java.lang.String");
	    double zero = 0.0;
	    double nonzero = -21.23;
	    String zerostring = String.valueOf(zero);
	    String nonzerostring = String.valueOf(nonzero);
	
	    harness.check(zerostring.equals("0.0"));
	    zero = -zero;
	    zerostring = String.valueOf(zero);
	    harness.check(zerostring.equals("-0.0"));
	    zero = -zero;
	    zerostring = String.valueOf(zero);
	    harness.check(zerostring.equals("0.0"));
	
	    harness.check(nonzerostring.equals("-21.23"));
	    nonzero = -nonzero;
	    nonzerostring = String.valueOf(nonzero);
	    harness.check(nonzerostring.equals("21.23"));
	    nonzero = -nonzero;
	    nonzerostring = String.valueOf(nonzero);
	    harness.check(nonzerostring.equals("-21.23"));
	
	    harness.setclass("java.lang.Double");
	
	}
	
	
	
	public void testall()
	{
		harness.setclass("java.lang.Double");
		test_Basics();
		test_toString();
		test_negatives();
		test_toValues();
		test_remainder();
		test_equals();
		test_hashCode();
		test_valueOf();
		test_doubleToLongBits();
	}

  public void test (TestHarness the_harness)
  {
    harness = the_harness;
    testall ();
  }

}
