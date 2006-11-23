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

package gnu.testlet.wonka.lang.Float;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class FloatTest implements Testlet
{
  protected static TestHarness harness;

/**
* tests the Float constructors Float(float) and Float(String), also checks the initialisation of the types
* by calling on Float.equals and Float.FloatValue();
* (by doing so, also tests the limits Float.MIN_VALUE and Float.MAX_VALUE
*/
  public void testConstructors()
  {
    harness.checkPoint("Float(float)");
    String msg = "Float == /Float.equals() ";
    Float a = new Float(0.0f);
    Float b = new Float(110.0f);
    Float c = new Float(0.0f);
    Float d = a;
    Float e = new Float(1.1e2f);
    Float f = new Float(-2.2e3f);
    Float g = new Float(-0.0f);
    Integer i = new Integer(0);

    harness.check( a != null , msg);
    harness.check(!a.equals(null), msg);
    harness.check( a != b  , msg   );
    harness.check(!a.equals(b), msg);
    harness.check( a != c  , msg   );
    harness.check( a.equals(c), msg);
    harness.check( a == d   , msg  );
    harness.check( a.equals(d), msg);
    harness.check( a == a  , msg   );
    harness.check(!a.equals(i), msg);
    harness.check( b.equals(e), msg);

    harness.checkPoint("floatValue()float");
    harness.check( a.floatValue(), 0.0f);
    harness.check( a.floatValue(), a.floatValue());
    harness.check( a.floatValue(), c.floatValue());
    harness.check( a.floatValue(), d.floatValue());
    harness.check( (int)a.floatValue(), i.intValue());

    harness.check( b.floatValue(), 110.0f);
    harness.check( b.floatValue(), 1.1e2f);
    harness.check( f.floatValue(),-2200.0f);
    //   we have to use the direct comparison here as check(float, float) realy compares Double.toString(float) to Double.tostring(Float)
    harness.check(Float.MAX_VALUE == 3.4028235E38f);
    harness.check(Float.MIN_VALUE == 1.4E-45f);

    harness.checkPoint("Float(double)");
    Float fmax = new Float((double)(Float.MAX_VALUE *2));
    Float fmin = new Float((double)(Float.MIN_VALUE /2));
    Float hmax = new Float(Double.MAX_VALUE);
    Float hmin = new Float(Double.MIN_VALUE);
    harness.check( fmax.floatValue(), (float)(Float.MAX_VALUE *2));
    harness.check( fmin.floatValue(), (float)(Float.MIN_VALUE /2));
    harness.check( hmax.floatValue(), (float)(Double.MAX_VALUE));
    harness.check( hmin.floatValue(), (float)(Double.MIN_VALUE));
    Float h1 = new Float(110.0f);
    Float h2 = new Float(110.0F);
    Float h3 = new Float(110.0d);
    Float h4 = new Float(110.0D);
    Float h5 = new Float(110.0);
    harness.check( h1.floatValue(), 110.0f);
    harness.check( h2.floatValue(), 110.0f);
    harness.check( h3.floatValue(), 110.0f);
    harness.check( h4.floatValue(), 110.0f);
    harness.check( h5.floatValue(), 110.0f);
  //  harness.check( hmin.floatValue(), Float.MIN_VALUE); => to max values...

    harness.checkPoint("Float(float)");
    msg = "Minimum and maximum values";
//    harness.check(Float.MAX_VALUE, 3.408235e38f);
  //  harness.check(Float.MIN_VALUE, 1.4e-45f);
    harness.check( 0.0f/0.0f, Float.NaN, msg);
    harness.check( 1.0f/0.0f, Float.POSITIVE_INFINITY, msg);
    harness.check(-1.0f/0.0f, Float.NEGATIVE_INFINITY, msg);

    harness.checkPoint("Float(java.lang.String)");
    constructMustSucceed("1",1.0f);
    constructMustSucceed("1.0",1.0f);
    constructMustSucceed("1e2",100.0f);
    constructMustSucceed("1.02e2",102.0f);
    constructMustSucceed("1.02e2f",102.0f);
    constructMustSucceed("-10.2 ",-10.2f);
    constructMustSucceed("10.2e3",10200.0f);
    constructMustSucceed("-10.2e3 ",-10200.0f);
    constructMustSucceed("10.2e-4",0.00102f);
    constructMustSucceed("-10.2e-4 ",-0.00102f);
    constructMustSucceed("-10.2e-4 ",-0.00102f);
    constructMustSucceed(".2", 0.2f);
    constructMustSucceed(".2e3", 200.0f);
    constructMustSucceed(".2e-4",0.00002f);
    constructMustSucceed("-.2", -0.2f);
    constructMustSucceed("-.2e3", -200.0f);
    constructMustSucceed("-.2e-4",-0.00002f);

    msg = "float/double endings";
//    constructMustSucceed("1.02e2",102.0f);
    constructMustSucceed("1.02e2f",102.0f);
    constructMustSucceed("1.02e2F",102.0f);
    constructMustSucceed("1.02e2d",102.0f);
    constructMustSucceed("1.02e2D",102.0f);

 //   harness.checkPoint("Constructor Float(String): leading/training blanks");
    constructMustSucceed(" 110.0  ", 110.0f);

//    harness.checkPoint("Constructor Float(String): max values");
//    constructMustSucceed("3.408235e38",Float.MAX_VALUE);
//    constructMustSucceed("1.401298464324817E-45",Float.MIN_VALUE);
//(in float format, max and min value strings are decoded as infinity)

//    harness.checkPoint("Constructor Float(String): hex string errors");
    constructMustFail("0x77");
    constructMustFail("#77");
    constructMustFail("4a");
    constructMustFail("0x4a");
    constructMustFail("#4a");
    harness.checkPoint("Float(java.lang.String)");
    constructMustFail(" ");
    constructMustFail("");
    constructMustFail(null);
  }

  private void constructMustSucceed(String line, float expected)
  {
    try
    {
      Float constructed = new Float(line);
      harness.check(constructed.floatValue(), expected);
    }
    catch(NumberFormatException e)
    {
      harness.fail("Could not construct desired value <" + line + ">");
    }

  }

  private void constructMustFail(String line)
  {
    try
    {
      new Float(line);
      harness.fail("Attempt to construct out-of-range float < " + line + " > ");
    }
    catch(Exception e)
    {
      harness.check(true);
    }

  }
public void testf2d(){

    harness.checkPoint("test float to double");
    Float fg = new Float(1.0f);
    harness.check( Math.abs(fg.doubleValue() - 1.0)<0.0001, "got: "+fg.doubleValue()+", but exp.: 1.0"); 		
    fg = new Float(-1.0f);
    harness.check( fg.doubleValue() == -1.0, "got: "+fg.doubleValue()+", but exp.: -1.0"); 		
    fg = new Float(10.0f);
    harness.check( fg.doubleValue() == 10.0, "got: "+fg.doubleValue()+", but exp.: 10.0"); 		
    fg = new Float(-10.0f);
    harness.check( fg.doubleValue() == -10.0, "got: "+fg.doubleValue()+", but exp.: -10.0"); 		
    fg = new Float(0.5f);
    harness.check( fg.doubleValue() == 0.5, "got: "+fg.doubleValue()+", but exp.: 0.5"); 		
    fg = new Float(-0.5f);
    harness.check( fg.doubleValue() == -0.5, "got: "+fg.doubleValue()+", but exp.: -0.5"); 		
    fg = new Float(1.01f);
    harness.check( Math.abs(fg.doubleValue()- 1.01) <0.0001, "got: "+fg.doubleValue()+", but exp.: 1.01"); 		
    fg = new Float(-1.01f);
    harness.check(  Math.abs(fg.doubleValue()+ 1.01) <0.0001, "got: "+fg.doubleValue()+", but exp.: -1.01"); 		
    fg = new Float(0.05f);
    harness.check( Math.abs(fg.doubleValue()- 0.05)< 0.0001, "got: "+fg.doubleValue()+", but exp.: 0.05"); 		
    fg = new Float(-0.05f);
    harness.check( Math.abs(fg.doubleValue()+ 0.05)< 0.0001, "got: "+fg.doubleValue()+", but exp.: -0.05"); 		
    fg = new Float(0.01f);
    harness.check( Math.abs(fg.doubleValue()- 0.01)< 0.0001, "got: "+fg.doubleValue()+", but exp.: 0.01"); 		
    fg = new Float(-0.01f);
    harness.check( Math.abs(fg.doubleValue()+ 0.01)< 0.0001, "got: "+fg.doubleValue()+", but exp.: -0.01"); 		
    fg = new Float(0.005f);
    harness.check( Math.abs(fg.doubleValue() - 0.005) <0.00001, "got: "+fg.doubleValue()+", but exp.: 0.005"); 		
    fg = new Float(-0.005f);
    harness.check( Math.abs(fg.doubleValue() + 0.005) <0.00001, "got: "+fg.doubleValue()+", but exp.: -0.005"); 		
    fg = new Float(0.25f);
    harness.check( Math.abs(fg.doubleValue() - 0.25)<0.00001, "got: "+fg.doubleValue()+", but exp.: 0.25"); 		
    fg = new Float(-0.25f);
    harness.check( Math.abs(fg.doubleValue() + 0.25)<0.00001, "got: "+fg.doubleValue()+", but exp.: -0.25"); 		
    fg = new Float(Float.NaN);
    harness.check(new Double(fg.doubleValue()).isNaN(), "got: "+fg.doubleValue()+", but exp.: NaN"); 		
    fg = new Float(Float.POSITIVE_INFINITY);
    harness.check( fg.doubleValue()== Double.POSITIVE_INFINITY, "got: "+fg.doubleValue()+", but exp.: Infinity"); 		
    fg = new Float(Float.NEGATIVE_INFINITY);
    harness.check( fg.doubleValue()==Double.NEGATIVE_INFINITY, "got: "+fg.doubleValue()+", but exp.: -Infinity"); 		
    fg = new Float(0.0f);
    harness.check( fg.doubleValue() == 0.0, "got: "+fg.doubleValue()+", but exp.: 0.0"); 		
   }
/**
* tests the isInfinite() and isNaN() functions
*/
	public void testInfiniteValues()
	{
	  //harness.checkPoint("Float.isInfinite, isInfinite(float), Float.isNan(), isNan(float)");
	  Float positive = new Float( 1.0f / 0.0f);
	  Float negative = new Float(-1.0f / 0.0f);
	  Float undefined= new Float( 0.0f / 0.0f);
	  Float thirds   = new Float(-1.0f / 3.0f);
	
	  harness.checkPoint("isInfinite()boolean");
	  harness.check(  positive.isInfinite());
	  harness.check(  negative.isInfinite());
	  harness.check(!undefined.isInfinite(),"got: "+undefined.floatValue());
	  harness.check(   !thirds.isInfinite());
	  harness.checkPoint("isNaN()boolean");
	  harness.check( !positive.isNaN());
	  harness.check( !negative.isNaN());
	  harness.check( undefined.isNaN());
	  harness.check(   !thirds.isNaN());
	  harness.checkPoint("isInfinite(float)boolean");
	  harness.check( Float.isInfinite( 1.0f / 0.0f));
	  harness.check( Float.isInfinite(-1.0f / 0.0f));
	  harness.check(!Float.isInfinite( 0.0f / 0.0f),"got: "+undefined.floatValue());
	  harness.check(!Float.isInfinite( 1.0f / 3.0f));	
	  harness.checkPoint("isNaN(float)boolean");
	  harness.check(!Float.isNaN( 1.0f / 0.0f));
	  harness.check(!Float.isNaN(-1.0f / 0.0f));
	  harness.check(!Float.isNaN( 1.0f / 3.0f));
	  harness.check( Float.isNaN( 0.0f / 0.0f));
	
  }
/**
* tests the compareTo compare/ordering functions
*/
	public void testCompare()
	{
		harness.checkPoint("compareTo(java.lang.Object)int");
		checkCompare(10.0f, 10.1f);
		checkCompare(0.0f, 10.1f);
		checkCompare(-10.1f, -10.0f);
		checkCompare(-10.0f, 0.0f);
		checkCompare(-10.1f, 10.0f);
		
		//harness.checkPoint("Float.Compare(Class) : exceptions");
    try
    {
  		Integer cha1 = new Integer(-9359);
      harness.check (cha1.compareTo(new Integer(-9359 )) == 0 );
    }
    catch(ClassCastException e)
    {
      harness.fail("Exception comparing two instances of class Byte ");
    }

    try
    {
  		Integer i = new Integer(50);
  		Float   f = new Float  (50.0f);
      f.compareTo(i);
      harness.fail("Attempt to compare two different objects ");
    }
    catch(ClassCastException e)
    {
      harness.check(true);
    }

    try
    {
  		Double d = new Double(50.0d);
  		Float  f = new Float (50.0f);
      f.compareTo(d);
      harness.fail("Attempt to compare two different objects ");
    }
    catch(ClassCastException e)
    {
      harness.check(true);
    }

  }

	private void checkCompare(float smallvalue, float bigvalue)
	{
	  try
	  {
	    Float smallfloat = new Float(smallvalue);
	    Float   bigfloat = new Float(  bigvalue);
	
	    if(smallfloat.compareTo(bigfloat) > 0)
	      harness.fail("compareTo detected <"+smallvalue+"> bigger then <"+bigvalue+">");
	    else if(smallfloat.compareTo(bigfloat) == 0)
	      harness.fail("compareTo detected <"+smallvalue+"> equal to <"+bigvalue+">");
	    else
	      harness.check(true);
	
	    if(bigfloat.compareTo(smallfloat) < 0)
	      harness.fail("compareTo detected <"+bigvalue+"> smaller then <"+smallvalue+">");
	    else if(bigfloat.compareTo(smallfloat) == 0)
	      harness.fail("compareTo detected <"+bigvalue+"> equal to <"+smallvalue+">");
	    else
	      harness.check(true);
	
	    if(smallfloat.compareTo(smallfloat)!= 0)
	      harness.fail("compareTo detected <"+smallvalue+"> not equal to itselves");
	    else
	      harness.check(true);
	
	    if(bigfloat.compareTo(bigfloat)!= 0)
	      harness.fail("compareTo detected <"+bigvalue+"> not equal to itselves");
	    else
	      harness.check(true);
	  }
	  catch(Exception e)
	  {
	    harness.fail("Exception while comparing <"+ smallvalue+ "> and <" + bigvalue + ">");
	  }
	}

/**
* tests the conversion Float to and from String using toString() and decode() functions
*/
	public void testStringConversion()
	{
		harness.checkPoint("toString(float)java.lang.String");
		float zerofloat = 0.0f;
		float negzerofloat = -0.0f;
		Float zero = new Float(0.0f);
		Float negzero     = new Float(negzerofloat);
		Float posinfinite = new Float( 1.0/0.0f);
		Float neginfinite = new Float(-1.0/0.0f);
		Float undefined   = new Float( 0.0/0.0f);
		Float biggermax   = new Float(Float.MAX_VALUE *2);
		Float smallermin  = new Float(Float.MIN_VALUE /2);
		Float nbiggermax  = new Float(Float.MAX_VALUE *-2);
		Float nsmallermin = new Float(Float.MIN_VALUE /-2);
		
		harness.checkPoint("toString()java.lang.String");
		/** zero is represented as <0.0>, infinite values as <Infinity> and undefined ones as <NaN>*/		
		harness.check(zero.toString(), "0.0");
		harness.check(Float.toString(zerofloat), "0.0");
		harness.check(negzero.toString(), "-0.0");
		harness.check(Float.toString(negzerofloat), "-0.0");
		harness.check(posinfinite.toString(), "Infinity");
		harness.check(Float.toString( 1.0f / 0.0f), "Infinity");
		harness.check(neginfinite.toString(), "-Infinity");
		harness.check(Float.toString(-1.0f / 0.0f), "-Infinity");
		harness.check(undefined.toString(), "NaN");
		harness.check(Float.toString( 0.0f / 0.0f), "NaN");
		
		/** zero is represented as <0.0>, infinite values as <Infinity> and undefined ones as <NaN>*/		
		harness.check(biggermax.toString()  ,  "Infinity");
		harness.check(nbiggermax.toString() , "-Infinity");
		harness.check(smallermin.toString() ,  "0.0");
		harness.check(nsmallermin.toString(), "-0.0");
		harness.check(Float.toString(Float.MAX_VALUE *2),   "Infinity");
		harness.check(Float.toString(-Float.MAX_VALUE *2), "-Infinity");
		harness.check(Float.toString(Float.MIN_VALUE /2),   "0.0");
		harness.check(Float.toString(Float.MIN_VALUE /-2), "-0.0");
		
		Float f;
		/** for f < 10000000.. and f >= 0.001 , normal decimal representation */		
		f = new Float(6e6f);
		harness.check(f.toString(), "6000000.0");
		harness.check(Float.toString(6e6f), "6000000.0");
		f = new Float(-4.5e6f);
		harness.check(f.toString(), "-4500000.0");
		harness.check(Float.toString(-4.5e6f), "-4500000.0");
		f = new Float(9999999.0f);
		harness.check(f.toString(), "9999999.0");
		harness.check(Float.toString(9999999.0f), "9999999.0");
		f = new Float(-9999999.0f);
		harness.check(f.toString(), "-9999999.0");
		harness.check(Float.toString(-9999999.0f), "-9999999.0");
		
		f = new Float(1.01e-3f);
		harness.check(f.toString(), "0.00101");
		harness.check(Float.toString(1.01e-3f), "0.00101");
		f = new Float(-1.01e-3f);
		harness.check(f.toString(), "-0.00101");
		harness.check(Float.toString(-1.01e-3f), "-0.00101");
		f = new Float(0.001f);
		harness.check(f.toString(), "0.001");
		harness.check(Float.toString(1e-3f), "0.001");
		
		/** for f > 10000000.. and f <= 0.001 , 'scientific' representation 1.001e-5... */		
	
		f = new Float(800000.1f);
		harness.check(f.toString().startsWith("800000."));
		harness.check(Float.toString(800000.1f).startsWith("800000."));
		f = new Float(-100000.2f);
		harness.check(f.toString(), "-100000.2");
		harness.check(Float.toString(-100000.2f), "-100000.2");
		f = new Float(1e7f);
		harness.check(f.toString(), "1.0E7");
		harness.check(Float.toString(1e7f), "1.0E7");
		f = new Float(-1e7f);
		harness.check(f.toString(), "-1.0E7");
		harness.check(Float.toString(-1e7f), "-1.0E7");
		
		f = new Float(0.00099f);
		harness.check(f.toString().endsWith("E-4")&&f.toString().startsWith("9."), "9.9E-4");
//		harness.check(Float.toString(0.00099f), "9.9E-4");
		f = new Float(-0.00099f);
		harness.check(f.toString().endsWith("E-4")&&f.toString().startsWith("-9."), "-9.9E-4");
//		harness.check(Float.toString(-0.00099f), "-9.9E-4");
		f = new Float(0.001f);
		harness.check(!f.toString().equals("1E-3"));
		harness.check(!Float.toString(1e-3f).equals("1E-3"));
	}
	
/**
* tests the floating point representation rules for 32-bit numbers by giving integer and float 'values' for the same 4 byte number
*/
	public void testIntbitsRepresentation()
	{
/**
  NOTE, at present, I haven't had the time to check into the complete float storage specification, therefore, no
  <32-bit value of float value x should be....> checks are included. We will concentrate on the 32-bit
  representations of 'special' float values and just check if intBitsToFloat(floatToRawIntBits ) return the original value
*/
		harness.checkPoint("intBitsToFloat(int)float");
		int i = 1234;	
		float f = 123.45f;
		harness.check(Float.floatToIntBits(Float.intBitsToFloat(i)), i);
		harness.check(Float.intBitsToFloat(Float.floatToRawIntBits(f)), f);
		harness.check(Float.floatToRawIntBits(Float.intBitsToFloat(Integer.MAX_VALUE)), Integer.MAX_VALUE);
		harness.check(Float.floatToRawIntBits(Float.intBitsToFloat(Integer.MIN_VALUE)), Integer.MIN_VALUE);
//		harness.check(Float.intBitsToFloat(Float.floatToIntBits(  Float.MAX_VALUE)),   Float.MAX_VALUE);
	//	harness.check(Float.intBitsToFloat(Float.floatToIntBits(  Float.MIN_VALUE)),   Float.MIN_VALUE); //done below
		
		//harness.checkPoint("float intbitsToFloat(int) / int floatToIntBits(float) : infinite and NaN values");
		harness.check(Float.intBitsToFloat(0x80000000),-0.0f );
		harness.check(Float.intBitsToFloat(0x00000000), 0.0f );
		f = Float.POSITIVE_INFINITY;
		i = 0x7f800000;
		harness.check(Float.intBitsToFloat(i), f);
		harness.check(Float.floatToIntBits(f), i);
		f = Float.NEGATIVE_INFINITY;
		i = 0xff800000;
		harness.check(Float.intBitsToFloat(i), f);
		harness.check(Float.floatToIntBits(f), i);
		f = Float.MAX_VALUE;
		i = 0x7f7fffff;
		harness.check(Float.intBitsToFloat(i), f);
		harness.check(Float.floatToIntBits(f), i);
		f = Float.MIN_VALUE;
		i = 1;
		harness.check(Float.intBitsToFloat(i), f);
		harness.check(Float.floatToIntBits(f), i);
		f = Float.NaN;
		i = 0x7fc00000;
		harness.check(Float.intBitsToFloat(i), f);
		harness.check(Float.floatToIntBits(f), i);
		harness.debug("NaN = "+Integer.toHexString(Float.floatToIntBits(f))+", but should be 0x7fc00000");
		//harness.checkPoint("float intbitsToFloat(int) = NaN for integers between 0x7f800001 and 0x7fffffff");
		harness.check(Float.intBitsToFloat(0x7f800001),Float.NaN );
		harness.check(Float.intBitsToFloat(0x7f9abcde),Float.NaN );
		harness.check(Float.intBitsToFloat(0x7fffffff),Float.NaN );
		//harness.checkPoint("float intbitsToFloat(int) = NaN for integers between 0xff800001 and 0xffffffff");
		harness.check(Float.intBitsToFloat(0xff800001),Float.NaN );
		harness.check(Float.intBitsToFloat(0xff9abcde),Float.NaN );
		harness.check(Float.intBitsToFloat(0xffffffff),Float.NaN );
		
	}
	
/**
* tests the conversion from String to Float class/float primitive
*/
	public void testStringValueParsing()
	{	
		
		harness.checkPoint("valueOf(java.lang.String)java.lang.Float");
		parseCheckMustSucceed("100.0", 100.0f);
		valueCheckMustSucceed("100.0", 100.0f);
		parseCheckMustSucceed("100", 100.0f);
		valueCheckMustSucceed("100", 100.0f);
		parseCheckMustSucceed("1e2", 100.0f);
		valueCheckMustSucceed("1e2", 100.0f);
		
		parseCheckMustSucceed("-100.0", -100.0f);
		valueCheckMustSucceed("-100.0", -100.0f);
		parseCheckMustSucceed("-100", -100.0f);
		valueCheckMustSucceed("-100", -100.0f);
		parseCheckMustSucceed("-1e2", -100.0f);
		valueCheckMustSucceed("-1e2", -100.0f);
		
		
		parseCheckMustSucceed("1100.51", 1100.51f);
		valueCheckMustSucceed("1100.51", 1100.51f);
		parseCheckMustSucceed("1100.510", 1100.51f);
		valueCheckMustSucceed("1100.510", 1100.51f);
		parseCheckMustSucceed("110.051e1", 1100.51f);
		valueCheckMustSucceed("110.051e1", 1100.51f);
		parseCheckMustSucceed("11.0051e2", 1100.51f);
		valueCheckMustSucceed("11.0051e2", 1100.51f);
		parseCheckMustSucceed("1.10051e3", 1100.51f);
		valueCheckMustSucceed("1.10051e3", 1100.51f);
		parseCheckMustSucceed("0.110051e4", 1100.51f);
		valueCheckMustSucceed("0.110051e4", 1100.51f);
		
		parseCheckMustSucceed("-1100.51", -1100.51f);
		valueCheckMustSucceed("-1100.51", -1100.51f);
		parseCheckMustSucceed("-1100.510", -1100.51f);
		valueCheckMustSucceed("-1100.510", -1100.51f);
		parseCheckMustSucceed("-110.051e1", -1100.51f);
		valueCheckMustSucceed("-110.051e1", -1100.51f);
		parseCheckMustSucceed("-11.0051e2", -1100.51f);
		valueCheckMustSucceed("-11.0051e2", -1100.51f);
		parseCheckMustSucceed("-1.10051e3", -1100.51f);
		valueCheckMustSucceed("-1.10051e3", -1100.51f);
		parseCheckMustSucceed("-0.110051e4", -1100.51f);
		valueCheckMustSucceed("-0.110051e4", -1100.51f);
		
		parseCheckMustSucceed("0.00512", 0.00512f);
		valueCheckMustSucceed("0.00512", 0.00512f);
		parseCheckMustSucceed("0.0051200", 0.00512f);
		valueCheckMustSucceed("0.0051200", 0.00512f);
		parseCheckMustSucceed("0.0512e-1", 0.00512f);
		valueCheckMustSucceed("0.0512e-1", 0.00512f);
		parseCheckMustSucceed("0.512e-2", 0.00512f);
		valueCheckMustSucceed("0.512e-2", 0.00512f);
		parseCheckMustSucceed("5.12e-3", 0.00512f);
		valueCheckMustSucceed("5.12e-3", 0.00512f);
		parseCheckMustSucceed("512e-5", 0.00512f);
		valueCheckMustSucceed("512e-5", 0.00512f);
		parseCheckMustSucceed("51200e-7", 0.00512f);
		valueCheckMustSucceed("51200e-7", 0.00512f);
		
		parseCheckMustSucceed("-0.00512", -0.00512f);
		valueCheckMustSucceed("-0.00512", -0.00512f);
		parseCheckMustSucceed("-0.0051200", -0.00512f);
		valueCheckMustSucceed("-0.0051200", -0.00512f);
		parseCheckMustSucceed("-0.0512e-1", -0.00512f);
		valueCheckMustSucceed("-0.0512e-1", -0.00512f);
		parseCheckMustSucceed("-0.512e-2", -0.00512f);
		valueCheckMustSucceed("-0.512e-2", -0.00512f);
		parseCheckMustSucceed("-5.12e-3", -0.00512f);
		valueCheckMustSucceed("-5.12e-3", -0.00512f);
		parseCheckMustSucceed("-512e-5", -0.00512f);
		valueCheckMustSucceed("-512e-5", -0.00512f);
		parseCheckMustSucceed("-51200e-7", -0.00512f);
		valueCheckMustSucceed("-51200e-7", -0.00512f);

		parseCheckMustSucceed("11.51", 11.51f);
		valueCheckMustSucceed("11.51", 11.51f);
		parseCheckMustSucceed("1151e-2", 11.51f);
		valueCheckMustSucceed("1151e-2", 11.51f);
		parseCheckMustSucceed("115100e-4", 11.51f);
		valueCheckMustSucceed("115100e-4", 11.51f);
		parseCheckMustSucceed("0.1151e2", 11.51f);
		valueCheckMustSucceed("0.1151e2", 11.51f);
		parseCheckMustSucceed("0.001151e4", 11.51f);
		valueCheckMustSucceed("0.001151e4", 11.51f);
		
		harness.checkPoint("parseFloat(java.lang.String)float");
		parseCheckMustSucceed("11.51f", 11.51f);
		valueCheckMustSucceed("11.51f", 11.51f);
		parseCheckMustSucceed("11.51F", 11.51f);
		valueCheckMustSucceed("11.51F", 11.51f);
		parseCheckMustSucceed("11.51d", 11.51f);
		valueCheckMustSucceed("11.51d", 11.51f);
		parseCheckMustSucceed("11.51D", 11.51f);
		valueCheckMustSucceed("11.51D", 11.51f);
		
		parseCheckMustSucceed("  11.51   ", 11.51f);
		valueCheckMustSucceed("  11.51   ", 11.51f);
		
		  parseCheckMustFail(" ");
		  valueCheckMustFail(" ");
		  parseCheckMustFail("");
		  valueCheckMustFail("");
		  parseCheckMustFail(null);
		  valueCheckMustFail(null);
		  parseCheckMustFail("abc");
		  valueCheckMustFail("abc");
		  parseCheckMustFail("100.0.1");
		  valueCheckMustFail("100.0.1");
		  parseCheckMustFail("100. 01");
		  valueCheckMustFail("100. 01");
		  parseCheckMustFail("10 0.01");
		  valueCheckMustFail("10 0.01");
		  parseCheckMustFail("4.0 e5");
		  valueCheckMustFail("4.0 e5");
  				
      parseCheckMustSucceed("3.401E38" , 3.401e38f);
      valueCheckMustSucceed("3.401E38" , 3.401e38f);
      parseCheckMustSucceed("3.4028235E38" , Float.MAX_VALUE);
      valueCheckMustSucceed("3.4028235E38" , Float.MAX_VALUE);
      parseCheckMustSucceed("3.403E38" , Float.POSITIVE_INFINITY);
      valueCheckMustSucceed("3.403E38" , Float.POSITIVE_INFINITY);
      parseCheckMustSucceed("-3.401E38" , -3.401e38f);
      valueCheckMustSucceed("-3.401E38" , -3.401e38f);
      parseCheckMustSucceed("-3.4028235E38" , -Float.MAX_VALUE);
      valueCheckMustSucceed("-3.4028235E38" , -Float.MAX_VALUE);
      parseCheckMustSucceed("-3.403E38" , Float.NEGATIVE_INFINITY);
      valueCheckMustSucceed("-3.403E38" , Float.NEGATIVE_INFINITY);

      parseCheckMustSucceed("1.7E-45" , 1.7e-45f);
      valueCheckMustSucceed("1.7E-45" , 1.7e-45f);
      parseCheckMustSucceed("1.4E-45" , Float.MIN_VALUE);
      valueCheckMustSucceed("1.4E-45" , Float.MIN_VALUE);
      parseCheckMustSucceed("1.0E-46" , 0.0f);
      valueCheckMustSucceed("1.0E-46" , 0.0f);
      parseCheckMustSucceed("-1.7E-45" , -1.7e-45f);
      valueCheckMustSucceed("-1.7E-45" , -1.7e-45f);
      parseCheckMustSucceed("-1.4E-45" , -Float.MIN_VALUE);
      valueCheckMustSucceed("-1.4E-45" , -Float.MIN_VALUE);
      parseCheckMustSucceed("-1.0E-46" , -0.0f);
      valueCheckMustSucceed("-1.0E-46" , -0.0f);
	}

  private void parseCheckMustSucceed(String line, float target)
  {
		try
		{
		  float decoded = Float.parseFloat(line);
		  if(decoded == target)
		    harness.check(true);
		  else {
		    harness.fail("Decoded <"+line+"> into "+decoded+" instead of " + target );
		    harness.debug("decoded "+Integer.toHexString(Float.floatToIntBits(decoded)));
		    harness.debug("target "+Integer.toHexString(Float.floatToIntBits(target)));
		  }
		}
		catch(Exception e)
		{
		  harness.fail("Exception while decoding float string <" + line + ">");
		}
  }

  private void valueCheckMustSucceed(String line, float target)
  {
		try
		{
		  Float decodedFloat = Float.valueOf(line);
		  float decoded = decodedFloat.floatValue();
		  if(decoded == target)
		    harness.check(true);
		  else {
		    harness.fail("ValueOf Decoded <"+line+"> into "+decoded+" instead of " + target );
		    harness.debug("decoded "+Integer.toHexString(Float.floatToIntBits(decoded)));
		    harness.debug("target "+Integer.toHexString(Float.floatToIntBits(target)));
		  }
		}
		catch(Exception e)
		{
		  harness.fail("Exception while decoding float string <" + line + ">");
		}
  }

  private void parseCheckMustFail(String line)
  {
		try
		{
		  Float.parseFloat(line);
		  harness.fail("Attempt to parse illegal float string <" + line + ">");
		}
		catch(Exception e)
		{
		  harness.check(true);
		}
  }

  private void valueCheckMustFail(String line)
  {
		try
		{
		  Float.valueOf(line);
		  harness.fail("Attempt to get value from illegal float string <" + line + ">");
		}
		catch(Exception e)
		{
		  harness.check(true);
		}
  }


/**
* tests the conversion between the Boolean object to the different primitives (float, integer , float...)
*/
	public void testValueConversion()
	{
		Float zero    = new Float(0.0 );
		Float afloat  = new Float((float)'a');
		Float fvalue1 = new Float(30.0f);
		Float fvalue2 = new Float( 123.45f );
		Float fvalue3 = new Float( 400.35f );
		Float fvalue4 = new Float(-23.45f);
		Float fvalue5 = new Float(3.3e38f);
		Float fvalue6 = new Float(3.4e100); //(Float from double value)
		
		
		// as the check comparisons by itself convert to integer, float or string, it is better to do a direct (value == expected)??
		//Boolean check instead of a check(int value, int expected)?? one
		
		harness.checkPoint("Value conversins");
		harness.check( zero.byteValue() == 0);
		harness.check(afloat.byteValue() == (byte)'a');
		harness.check(fvalue1.byteValue() == (byte) 30);
		harness.check(fvalue2.byteValue() == (byte) 123);
		harness.check(fvalue3.byteValue() == (byte) 400);
		harness.check(fvalue4.byteValue() == (byte)-23);
		harness.check(fvalue5.byteValue() == -1);
		harness.check(fvalue6.byteValue() == -1);
		
//		harness.checkPoint("Value conversins: Float => shortValue");
		harness.check(  zero.shortValue() == 0);
		harness.check( afloat.shortValue() == (short)'a');
		harness.check(fvalue1.shortValue() == (short) 30);
		harness.check(fvalue2.shortValue() == (short) 123);
		harness.check(fvalue3.shortValue() == (short) 400);
		harness.check(fvalue4.shortValue() == (short)-23);
		harness.check(fvalue5.shortValue() == -1 );
		harness.check(fvalue6.shortValue() == -1 );
		
//		harness.checkPoint("Value conversins: Float => intValue");
		harness.check(   zero.intValue() == 0);
		harness.check( afloat.intValue() == (int)'a');
		harness.check(fvalue1.intValue() == 30);
		harness.check(fvalue2.intValue() == 123);
		harness.check(fvalue3.intValue() == 400);
		harness.check(fvalue4.intValue() == -23);
		harness.check(fvalue5.intValue() == Integer.MAX_VALUE);
		harness.check(fvalue6.intValue() == Integer.MAX_VALUE);
		harness.check(new Float(3456.789f).intValue() == 3456, "got: "+new Float(3456.789f).intValue()+" but exp.: "+(int)3456.789f );
//		harness.checkPoint("Value conversins: Float => longValue");
		harness.check(   zero.longValue() == 0l);
		harness.check( afloat.longValue() == (long)'a');
		harness.check(fvalue1.longValue() ==  30l);
		harness.check(fvalue2.longValue() ==  123l);
		harness.check(fvalue3.longValue() ==  400l);
		harness.check(fvalue4.longValue() == -23l);
		harness.check(fvalue5.longValue() == (long) 3.3e38f);
		harness.check(fvalue6.longValue() == (long) 3.4e100);
		harness.check(new Float(123456.789f).longValue() == 123456, "got: "+new Float(123456.789f).longValue()+" but exp.: "+(long)123456.789f );
		
//		harness.checkPoint("Value conversions: Float => floatValue");
		harness.check(   zero.floatValue() == 0.0f);
		harness.check( afloat.floatValue() == (float)'a');
		harness.check(fvalue1.floatValue() == 30.0f);
		harness.check(fvalue2.floatValue() == 123.45f);
		harness.check(fvalue3.floatValue() == 400.35f);
		harness.check(fvalue4.floatValue() == -23.45f);
		harness.check(fvalue5.floatValue() == 3.3e38f);
		harness.check(fvalue6.floatValue() == (float) 3.4e100);
		
//		harness.checkPoint("Value conversions: Float => doubleValue");
		harness.check(   zero.doubleValue() == 0.0);
		harness.check( afloat.doubleValue() == (double)'a');
		harness.check(fvalue1.doubleValue() , 30.0);
		harness.check(fvalue2.doubleValue() , 123.45f);
		harness.check(fvalue3.doubleValue() , 400.35f);
		harness.check(fvalue4.doubleValue() , -23.45f);
		harness.check(fvalue5.doubleValue() , 3.3e38f);
		harness.check(fvalue6.doubleValue() == (double)((float)3.4e100));
	
/** to do: check conversions for positive infinite, negative infinite, nan...*/	
	}

/*
* tests the properties put() method
* no system properties defined for Float class
  void testProperties()
  {
      // Augment the System properties with the following.
      // Overwriting is bad because println needs the
      // platform-dependent line.separator property.
      harness.checkPoint("Properties.put()");
      Properties p = System.getProperties();
      p.put("afloat", "97");
      p.put("zero" , "0");
      p put("newa" , "97");

      harness.check (Float.getFloat("afloat") ==(float)'a');
      harness.check (Float.getFloat("zero")  == (float)0);
      harness.check (Float.getFloat("afloat") == Float.getFloat("newa") );
      harness.check (Float.getFloat("afloat") != Float.getFloat("zero") );
  }

/**
* tests the Boolean object overwrites hashCode()
*/
  public void testHashCode()
  {
    Float a = new Float(0.01f);
    Float newa = new Float(1e-2f);
    Float infinite = new Float(Float.POSITIVE_INFINITY);
    int binary32= 0x02e00aa1;
    Float b32 = new Float(Float.intBitsToFloat(binary32));
    harness.checkPoint("hashCode()int");
    harness.check (b32.hashCode(), binary32);
    harness.check (a.floatValue(), Float.intBitsToFloat(a.hashCode()) );
    harness.check (Float.floatToIntBits(1e-2f), newa.hashCode() );
    harness.check (a.hashCode(), newa.hashCode());
  }

/**
* tests the Boolean object overwrites getClass()
*/
  public void testGetClass()
  {
    Float a = new Float(0.0f);
    Float b = new Float(Float.POSITIVE_INFINITY);
    Float c = new Float(Float.NEGATIVE_INFINITY);
    Float d = new Float(Float.NaN);
    Integer i = new Integer(0);


    harness.checkPoint("TYPE(public)java.lang.Class");
    harness.check (a.getClass(), b.getClass());
    harness.check (a.getClass(), c.getClass());
    harness.check (a.getClass(), d.getClass());
    harness.check (a.getClass() != i.getClass());
    try
    {
      harness.check (a instanceof Float );
      harness.check (b instanceof Float );
      harness.check (a.getClass().getName(), "java.lang.Float");
      harness.check (b.getClass().getName(), "java.lang.Float");
      harness.check (c.getClass().getName(), "java.lang.Float");
      harness.check (d.getClass().getName(), "java.lang.Float");
      harness.check (a.getClass(), Class.forName("java.lang.Float") );
      harness.check (b.getClass(), Class.forName("java.lang.Float") );
      harness.check (c.getClass(), Class.forName("java.lang.Float") );
      harness.check (d.getClass(), Class.forName("java.lang.Float") );
      harness.check (i.getClass() != Class.forName("java.lang.Float") );
      harness.check ((Float.TYPE).getName(), "float");
//      harness.check ( Boolean.TYPE, Class.forName("boolean"));
    }
    catch (ClassNotFoundException e)
    {
      harness.fail("error finding class name");
      harness.debug(e);
    }
  }

/**
* calls the tests described
*/
  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("java.lang.Float");
		testConstructors();
		testInfiniteValues();
		testCompare();
		testStringConversion();
		testIntbitsRepresentation();
		testStringValueParsing();
		testValueConversion();
//	 	testProperties(); // not defined
	 	testHashCode();
	 	testGetClass();
	 	testf2d();
	}

}
