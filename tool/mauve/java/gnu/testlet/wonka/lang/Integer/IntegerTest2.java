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

package gnu.testlet.wonka.lang.Integer;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.Properties;

public class IntegerTest2 implements Testlet
{
  protected static TestHarness harness;

/**
* tests the Integer constructors Integer(int) and Integer(String), also checks the initialisation of the types
* by calling on Integer.equals and Integer.intValue();
* (by doing so, also tests the limits Integer.MIN_VALUE and Integer.MAX_VALUE
*/
  public void testConstructors()
  {
    harness.checkPoint("Integer(int)");
    Integer a = new Integer(0);
    Integer b = new Integer(1);
    Integer c = new Integer(0);
    Integer d = a;
    Integer e = new Integer(0xe);
    int fint = 'f';
    Integer f = new Integer(fint);
    Integer g = new Integer((int)'a');
    Long l = new Long(0l);

    harness.checkPoint("equals(java.lang.Object)boolean");
    harness.check( a != null);
    harness.check(!a.equals(null));
    harness.check( a != b     );
    harness.check(!a.equals(b));
    harness.check( a != c     );
    harness.check( a.equals(c));
    harness.check( a == d     );
    harness.check( a.equals(d));
    harness.check( a == a     );
    harness.check(!a.equals(l));

    harness.checkPoint("intValue()int");
    harness.check( a.intValue(), 0);
    harness.check( a.intValue(), a.intValue());
    harness.check( a.intValue(), c.intValue());
    harness.check( a.intValue(), d.intValue());
    harness.check( a.intValue(), l.intValue());
    harness.check( b.intValue(), 1);
    harness.check( e.intValue(), 14);
    harness.check( f.intValue(), fint);
    harness.check( g.intValue(), (int)'a');
    harness.check( g.intValue(), 0x61);
    harness.check( g.intValue(), 97);

    harness.checkPoint("MAX_VALUE(public)int");
    harness.check (Integer.MAX_VALUE,  0x7fffffff);
    harness.checkPoint("MIN_VALUE(public)int");
    harness.check (Integer.MIN_VALUE, -0x80000000);

    harness.checkPoint("Integer(java.lang.String)");
    constructMustSucceed("1", 1);
    constructMustSucceed("2147483647", 0x7fffffff);
//    constructMustSucceed("  1 ", 1);
    constructMustSucceed("-2147483648", -0x80000000);
    constructMustFail( "2147483648");
    constructMustFail("-2147483649");
    constructMustFail("0x77");
    constructMustFail("#77");
    constructMustFail("4f");
    constructMustFail("0x4f");
    constructMustFail(" ");
    constructMustFail("");
    constructMustFail(null);
  }

  private void constructMustSucceed(String line, int expected)
  {
    try
    {
      Integer constructed = new Integer(line);
      harness.check(constructed.intValue(), expected);
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
      new Integer(line);
      harness.fail("Attempt to construct out-of-range integer < " + line + " > ");
    }
    catch(Exception e)//(NumberFormatException e8)
    {
      harness.check(true);
    }

  }

/**
* tests the compareTo compare/orderring functions
*/
	public void testCompare()
	{
		harness.checkPoint("compareTo(java.lang.Object)int");
		checkCompare(100, 101);
		checkCompare(0, 101);
		checkCompare(-101, -100);
		checkCompare(-100, 0);
		checkCompare(-101, 100);
		checkCompare(0x7ffffff0, Integer.MAX_VALUE);
		checkCompare(Integer.MIN_VALUE,-0x7ffffff0);
		
		//harness.checkPoint("Byte.Compare(Class) : exceptions");
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
  		Integer cha1 = new Integer((int)'a');
  		Character cha2 = new Character('a');
      cha1.compareTo(cha2);
      harness.fail("Attempt to compare two different objects ");
    }
    catch(ClassCastException e)
    {
      harness.check(true);
    }

    try
    {
  		Long cha1 = new Long(97L);
  		Integer cha2 = new Integer(97);
      cha1.compareTo(cha2);
      harness.fail("Attempt to compare two different objects ");
    }
    catch(ClassCastException e)
    {
      harness.check(true);
    }
  }

	private void checkCompare(int smallvalue, int bigvalue)
	{
	  try
	  {
	    Integer smallint = new Integer(smallvalue);
	    Integer   bigint = new Integer(  bigvalue);
	
	    if(smallint.compareTo(bigint) > 0)
	      harness.fail("compareTo detected <"+smallvalue+"> bigger then <"+bigvalue+">");
	    else if(smallint.compareTo(bigint) == 0)
	      harness.fail("compareTo detected <"+smallvalue+"> equal to <"+bigvalue+">");
	    else
	      harness.check(true);
	
	    if(bigint.compareTo(smallint) < 0)
	      harness.fail("compareTo detected <"+bigvalue+"> smaller then <"+smallvalue+">");
	    else if(bigint.compareTo(smallint) == 0)
	      harness.fail("compareTo detected <"+bigvalue+"> equal to <"+smallvalue+">");
	    else
	      harness.check(true);
	
	    if(smallint.compareTo(smallint)!= 0)
	      harness.fail("compareTo detected <"+smallvalue+"> not equal to itselves");
	    else
	      harness.check(true);
	
	    if(bigint.compareTo(bigint)!= 0)
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
* tests the conversion Integer to and from String using toString() and decode() functions
*/
	public void testStringConversion()
	{
		harness.checkPoint("toString(int)java.lang.String");
		int aint = (int)'a';
		int zeroint = 0;
		int negint = -97;
		Integer a = new Integer(aint);
		Integer zero = new Integer(0);
		Integer negative = new Integer(negint);
		
		String astring = "97";
		String negstring = "-97";
		
		harness.check(zero.toString(), "0");
		harness.check(Integer.toString(zeroint), "0");
		harness.check(a.toString(), astring);
		harness.check(a.toString(), Integer.toString(aint));
		harness.check(negative.toString(), "-97");
		harness.check(negative.toString(), Integer.toString(negint) );
		
    harness.checkPoint ("toBinaryString(int)java.lang.String");
    harness.check (Integer.toBinaryString(0), "0");
    harness.check (Integer.toBinaryString(1),  "1");
    harness.check (Integer.toBinaryString(3), "11");
    harness.check (Integer.toBinaryString(-1),"11111111111111111111111111111111");
		
		harness.check(Integer.toBinaryString(358).equals("101100110"));
		harness.check(Integer.toBinaryString( -5478 ).equals("11111111111111111110101010011010"));

    harness.checkPoint ("toOctalString(int)java.lang.String");
    harness.check (Integer.toOctalString(0), "0");
    harness.check (Integer.toOctalString(1), "1");
    harness.check (Integer.toOctalString(9), "11");
    harness.check (Integer.toOctalString(-1),"37777777777");
	
		harness.check(Integer.toOctalString(5847).equals("13327"));
		harness.check(Integer.toOctalString(-9863 ).equals("37777754571"));

    harness.checkPoint ("toHexString(int)java.lang.String");
    harness.check (Integer.toHexString(0), "0");
    harness.check (Integer.toHexString(1), "1");
    harness.check (Integer.toHexString(17),"11");
    harness.check (Integer.toHexString(31),"1f");
    harness.check (Integer.toHexString(-1),"ffffffff");
		
		harness.checkPoint("toString(int,int)java.lang.String");
		harness.check(Integer.toString(aint,2) , Integer.toBinaryString(aint));
		harness.check(Integer.toString(aint,8) , Integer.toOctalString(aint));
    harness.check(Integer.toString(aint,10), Integer.toString(aint));
		harness.check(Integer.toString(aint,16), Integer.toHexString(aint));
		
		harness.check(Integer.toString(aint,1), Integer.toString(aint));
		harness.check(Integer.toString(aint,37), Integer.toString(aint));
				
		harness.check(Integer.toString(4,3),"11");
		harness.check(Integer.toString(11,11), "10");
		harness.check(Integer.toString(21,11), "1a");
		harness.check(Integer.toString(21, 20),"11");
		harness.check(Integer.toString(20,20), "10");
		harness.check(Integer.toString(39,20), "1j");
		harness.check(Integer.toString(37,36),"11");
		harness.check(Integer.toString(36,36), "10");
		harness.check(Integer.toString(71,36), "1z");
		

    harness.checkPoint("decode(java.lang.String)java.lang.Integer");
		decodeMustPass(   "11", 11 );
		decodeMustPass(  "011",  9);
		decodeMustPass(  "#11", 17);
		decodeMustPass( "0x11", 17);
		decodeMustPass( "0x1F", 0x1f);
		decodeMustPass( "0x1f", 0x1F);
		decodeMustPass( "0", 0);
		
		//harness.checkPoint("Integer.decode(String): negative syntax");
		/** NOTE: for some reason, the Java SDK seems to demand 0-7 for -07, #-f for -#f and 0x-f for -0xf
		in the Byte class, and passes the ninus-first values here*/
		decodeMustPass(  "-11",-11);
		decodeMustPass( "-011", -9);
		decodeMustFail( "0-11");
		decodeMustPass( "-#11",-17);
		decodeMustFail( "#-11");
		decodeMustPass("-0x11",-17);
		decodeMustFail("0x-11");
		
		decodeMustPass( "0x7fffffff", Integer.MAX_VALUE);
		decodeMustPass( "0x7FFFFFFF", Integer.MAX_VALUE);
		
		//harness.checkPoint("Integer.decode(String) : exceptions");
		decodeMustFail( "0x80000000");
		decodeMustFail("-0x80000001");
		decodeMustFail( "019");
		decodeMustFail("122.5");
		decodeMustFail(  "4F");
		decodeMustFail( "#4G");
		decodeMustFail("0x4G");
		decodeMustFail(" 11");
		decodeMustFail(" ");
		decodeMustFail("");
		decodeMustFail(null);
 		
	}
	
	private void decodeMustPass(String line, int checkvalue)
	{
	  try
	  {
	    Integer decoded = Integer.decode(line);
	    harness.check( decoded.intValue(),checkvalue );
	  }
	  catch(Exception e)
	  {
	    harness.fail("Exception while trying to decode string <" + line + ">\n"+e);
	  }
	}
	
	private void decodeMustFail(String line)
	{
	  try
	  {
	    Integer.decode(line);
	    harness.fail("Attempt to decode illegal string format <" + line + ">");
	  }
	  catch(Exception e)
	  {
	    harness.check(true);
	  }
	}
	
/**
* tests the conversion from String to Integer class/int primitive with different radix
*/
	public void testStringValueParsing()
	{	
		String astring = new String("100");;
		Integer target = new Integer(100);
		
		harness.checkPoint("parseInt(java.lang.String)int");
		harness.check(Integer.parseInt(astring), 100);
		harness.check( (Integer.valueOf(astring)).equals(target) );
		harness.check(Integer.parseInt("0"), 0);
		harness.check(Integer.parseInt("-34"), -34);
		harness.check( (Integer.valueOf("-56")).equals(new Integer(-56)) );
		harness.check(Integer.parseInt("2147483647"), 2147483647);
		harness.check( (Integer.valueOf("2147483647")).equals(new Integer(Integer.MAX_VALUE)) );
		harness.check(Integer.parseInt("-2147483648"), -2147483648);
		harness.check( (Integer.valueOf("-2147483648")).equals(new Integer(Integer.MIN_VALUE)) );
		
		harness.checkPoint("valueOf(java.lang.String)java.lang.Integer");
 	  parseCheckMustFail("2147483648", 10);
 	  valueCheckMustFail("-2147483649", 10);
 	  parseCheckMustFail("0x60", 10);
 	  valueCheckMustFail("0x60", 10);
 	  parseCheckMustFail(" 11  ", 10);
 	  valueCheckMustFail(" 11  ", 10);
 	  parseCheckMustFail(" ", 10);
 	  valueCheckMustFail(" ", 10);
 	  parseCheckMustFail("", 10);
 	  valueCheckMustFail("", 10);
	  parseCheckMustFail(null, 10);
 	  valueCheckMustFail(null, 10);
/*		try
		{
		  astring = " 78   ";
		  harness.check(Integer.parseInt(astring), 78);
		  harness.check( (Integer.valueOf(astring)).equals(new Integer(78)) );
		}
		catch(NumberFormatException e)
		{
		  harness.fail("unable to parse int/get value Of / string with blanks ");
		}
	*/	
	
		//harness.checkPoint("int parseInt(String, radix)  / Integer valueOf(String, radix)");
		harness.check(Integer.parseInt( "12", 10), Integer.parseInt( "12") );
		harness.check(Integer.parseInt("-34", 10), Integer.parseInt("-34") );
		harness.check( (Integer.valueOf( "56", 10)),  Integer.valueOf( "56") );
		harness.check( (Integer.valueOf("-78", 10)),  Integer.valueOf("-78") );
		
		harness.check(Integer.parseInt( "11", 2), Integer.parseInt( "3"), "parseInt binary " );
		harness.check(Integer.parseInt("-11", 2), Integer.parseInt("-3"), "parseInt binary negative" );
		harness.check(  Integer.valueOf( "11", 2),   Integer.valueOf( "3"),   "valueOf binary " );
		harness.check(  Integer.valueOf("-11", 2),   Integer.valueOf("-3"),   "valueOf binary negative" );

		harness.check(Integer.parseInt( "11", 3), Integer.parseInt( "4"), "parseInt 3-based " );
		harness.check(Integer.parseInt("-11", 3), Integer.parseInt("-4"), "parseInt 3-based negative" );
		harness.check(  Integer.valueOf( "11", 3),   Integer.valueOf( "4"),   "valueOf 3-based " );
		harness.check(  Integer.valueOf("-11", 3),   Integer.valueOf("-4"),   "valueOf 3-based negative" );

		harness.check(Integer.parseInt( "11", 8), Integer.parseInt( "9"), "parseInt octal " );
		harness.check(Integer.parseInt("-11", 8), Integer.parseInt("-9"), "parseInt octal negative" );
		harness.check(  Integer.valueOf( "11", 8),   Integer.valueOf( "9"),   "valueOf octal " );
		harness.check(  Integer.valueOf("-11", 8),   Integer.valueOf("-9"),   "valueOf octal negative" );
		
		harness.check(Integer.parseInt( "11", 16), Integer.parseInt( "17"), "parseInt hex " );
		harness.check(Integer.parseInt("-11", 16), Integer.parseInt("-17"), "parseInt hex negative" );
		harness.check(  Integer.valueOf( "11", 16),   Integer.valueOf( "17"),   "valueOf hex " );
		harness.check(  Integer.valueOf("-11", 16),   Integer.valueOf("-17"),   "valueOf hex negative" );
		harness.check(Integer.parseInt( "f" , 16), Integer.parseInt( "15"), "parseInt hex " );
		harness.check(Integer.parseInt("-f" , 16), Integer.parseInt("-15"), "parseInt hex negative" );
		harness.check(Integer.parseInt( "F" , 16), Integer.parseInt( "15"), "parseInt hex capital" );
		harness.check(  Integer.valueOf( "f" , 16),   Integer.valueOf( "15"),   "valueOf hex " );
		harness.check(  Integer.valueOf("-f" , 16),   Integer.valueOf("-15"),   "valueOf hex negative" );
		harness.check(  Integer.valueOf( "F" , 16),   Integer.valueOf( "15"),   "valueOf hex capital" );
		
		harness.check(Integer.parseInt( "11", 25), Integer.parseInt( "26"), "parseInt 25-based " );
		harness.check(Integer.parseInt("-11", 25), Integer.parseInt("-26"), "parseInt 25-based negative" );
		harness.check(  Integer.valueOf( "11", 25),   Integer.valueOf( "26"),   "valueOf 25-based " );
		harness.check(  Integer.valueOf("-11", 25),   Integer.valueOf("-26"),   "valueOf 25-based negative" );
		harness.check(Integer.parseInt( "o" , 25), Integer.parseInt( "24"), "parseInt 25-based " );
		harness.check(Integer.parseInt( "O" , 25), Integer.parseInt( "24"), "parseInt 25-based capital" );
		harness.check(Integer.parseInt("-o" , 25), Integer.parseInt("-24"), "parseInt 25-based negative" );
		harness.check(  Integer.valueOf( "o" , 25),   Integer.valueOf( "24"),   "valueOf 25-based " );
		harness.check(  Integer.valueOf( "O" , 25),   Integer.valueOf( "24"),   "valueOf 25-based capital" );
		harness.check(  Integer.valueOf("-o" , 25),   Integer.valueOf("-24"),   "valueOf 25-based negative" );
		
		harness.check(Integer.parseInt( "11", 36), Integer.parseInt( "37"), "parseInt 36-based " );
		harness.check(Integer.parseInt("-11", 36), Integer.parseInt("-37"), "parseInt 36-based negative" );
		harness.check(  Integer.valueOf( "11", 36),   Integer.valueOf( "37"),   "valueOf 36-based " );
		harness.check(  Integer.valueOf("-11", 36),   Integer.valueOf("-37"),   "valueOf 36-based negative" );
		harness.check(Integer.parseInt( "z" , 36), Integer.parseInt( "35"), "parseInt 36-based " );
		harness.check(Integer.parseInt( "Z" , 36), Integer.parseInt( "35"), "parseInt 36-based capital" );
		harness.check(Integer.parseInt("-z" , 36), Integer.parseInt("-35"), "parseInt 36-based negative" );
		harness.check(  Integer.valueOf( "z" , 36),   Integer.valueOf( "35"),   "valueOf 36-based " );
		harness.check(  Integer.valueOf( "Z" , 36),   Integer.valueOf( "35"),   "valueOf 36-based capital" );
		harness.check(  Integer.valueOf("-z" , 36),   Integer.valueOf("-35"),   "valueOf 36-based negative" );
		
		
		//harness.checkPoint("parseInt(String, int)/ valueOf(String, int) : exceptions");
  	  parseCheckMustFail(" 11  ", 2);
  	  valueCheckMustFail(" 11  ", 2);
  	  parseCheckMustFail(" 11  ", 3);
  	  valueCheckMustFail(" 11  ", 3);
  	  parseCheckMustFail(" 11  ", 8);
  	  valueCheckMustFail(" 11  ", 8);
  	  parseCheckMustFail(" 11  ", 16);
  	  valueCheckMustFail(" 11  ", 16);
  	  parseCheckMustFail(" 11  ", 25);
  	  valueCheckMustFail(" 11  ", 25);
  	  parseCheckMustFail(" 11  ", 36);
  	  valueCheckMustFail(" 11  ", 36);
		
		//harness.checkPoint("parseInt(String, int)/ valueOf(String, int) : radix exceptions");
		  parseCheckMustFail("11",1);
		  valueCheckMustFail("11", 1);
		  parseCheckMustFail("11",37);
		  valueCheckMustFail("11", 37);
		
		//harness.checkPoint("parseInt(String, int)/ valueOf(String, int) : out-of-bound exceptions");
		  parseCheckMustFail("3",2);
		  valueCheckMustFail("3", 2);
		  parseCheckMustFail("5",4);
		  valueCheckMustFail("5", 4);
		  parseCheckMustFail("9",8);
		  valueCheckMustFail("9", 8);
		  parseCheckMustFail("g",16);
		  valueCheckMustFail("g", 16);
		  parseCheckMustFail("z",35);
		  valueCheckMustFail("z", 35);
		
		//harness.checkPoint("parseInt(String, int)/ valueOf(String, int) : int boundaries exceptions");
		  parseCheckMustFail("10000000000000000000000000000000",2);
		  valueCheckMustFail("10000000000000000000000000000000", 2);
		  parseCheckMustFail("20000000000",8);
		  valueCheckMustFail("20000000000", 8);
		  parseCheckMustFail("80000000",16);
		  valueCheckMustFail("80000000", 16);
		  parseCheckMustFail("4000000",32);
		  valueCheckMustFail("4000000", 32);
		
		  parseCheckMustFail("-10000000000000000000000000000001",2);
		  valueCheckMustFail("-10000000000000000000000000000001", 2);
		  parseCheckMustFail("-20000000001",8);
		  valueCheckMustFail("-20000000001", 8);
		  parseCheckMustFail("-80000001",16);
		  valueCheckMustFail("-80000001", 16);
		  parseCheckMustFail("-4000001",32);
		  valueCheckMustFail("-4000001", 32);
	
	}

  private void parseCheckMustFail(String line, int radix)
  {
		try
		{
		  Integer.parseInt(line, radix);
		  harness.fail("Attempt to parse illegal int string <" + line + ">");
		}
		catch(NumberFormatException e)
		{
		  harness.check(true);
		}
  }

  private void valueCheckMustFail(String line, int radix)
  {
		try
		{
		  Integer.valueOf(line, radix);
		  harness.fail("Attempt to get value from illegal int string <" + line + ">");
		}
		catch(NumberFormatException e)
		{
		  harness.check(true);
		}
  }


/**
* tests the conversion between the Boolean object to the different primitives (short, integer , float...)
*/
	public void testValueConversion()
	{
		Integer zero  = new Integer( 0 );
		Integer aint = new Integer((int)'a');
		Integer min   = new Integer(Integer.MIN_VALUE);
		Integer max   = new Integer(Integer.MAX_VALUE);
		
		
		// as the check comparisons by itself convert to integer, float or string, it is better to do a direct (value == expected)??
		// instead of Boolean check(int target, int expected)		
		harness.checkPoint("Value conversions");
		harness.check( zero.byteValue() == 0);
		harness.check(aint.byteValue() == (byte)'a');
		harness.check(aint.byteValue() == (byte)(aint.intValue()) );		
		harness.check(max.byteValue() == (byte)2147483647);
		harness.check(min.byteValue() ==(byte)-2147483648);
		
		//harness.checkPoint("Value conversions: Integer => shortValue");
		harness.check( zero.shortValue() == 0);
		harness.check(aint.shortValue() == (short)'a');
		harness.check(aint.shortValue() == (short)(aint.intValue()) );		
		harness.check(max.shortValue() == (short)2147483647);
		harness.check(min.shortValue() == (short)-2147483648);
		
		//harness.checkPoint("Value conversions: Integer => intValue");
		harness.check( zero.intValue() == 0);
		harness.check(aint.intValue() == (int)'a');
		harness.check(max.intValue() == 2147483647);
		harness.check(min.intValue() ==-2147483648);
		
		//harness.checkPoint("Value conversions: Integer => longValue");
		harness.check( zero.longValue() == 0l);
		harness.check(aint.longValue() == (long)'a');
		harness.check(aint.longValue() == (long)(aint.intValue()) );		
		harness.check(max.longValue() == 2147483647l);
		harness.check(min.longValue() ==-2147483648l);
		
		//harness.checkPoint("Value conversions: Integer => floatValue");
		harness.check( zero.floatValue() == 0.0f);
		harness.check(aint.floatValue() == (float)'a');
		harness.check(aint.floatValue() == (float)(aint.intValue()) );		
		harness.check(max.floatValue() == 2147483647.0f);
		harness.check(min.floatValue() ==-2147483648.0f);
		
		//harness.checkPoint("Value conversions: Integer => doubleValue");
		harness.check( zero.doubleValue() == 0.0);
		harness.check(aint.doubleValue() == (double)'a');
		harness.check(aint.doubleValue() == (double)(aint.intValue()) );		
		harness.check(max.doubleValue() == 2147483647.0);
		harness.check(min.doubleValue() ==-2147483648.0);
	}

/**
* tests the properties put() and getInteger() methods
*/
  public void testProperties()
  {
      // Augment the System properties with the following.
      // Overwriting is bad because println needs the
      // platform-dependent line.separator property.
      harness.checkPoint("getInteger(java.lang.String)int");
      Properties p = System.getProperties();
      p.put("aint", "97");
      p.put("zero" , "0");
      p.put("newa" , "97");
      Integer aint = new Integer('a');

      harness.check (Integer.getInteger("aint"), aint);
      harness.check (Integer.getInteger("zero"), new Integer(0));
      harness.check (Integer.getInteger("aint") == Integer.getInteger("newa") );
      harness.check (Integer.getInteger("aint") != Integer.getInteger("zero") );
      harness.check (Integer.getInteger("bint") == null );
      harness.check (Integer.getInteger("cint",97), aint );
      harness.check (Integer.getInteger("dint", aint), aint);
  }

/**
* tests the Boolean object overwrites hashCode()
*/
  public void testHashCode()
  {
    Integer a = new Integer((int)'a');
    Integer b = new Integer(123456);
    Integer zero = new Integer( 0);
    Integer newa = new Integer((int)'a');

    harness.checkPoint("hashCode()int");
    harness.check (a.hashCode(), newa.hashCode());
    harness.check (a.hashCode(), (int)'a');
    harness.check (b.hashCode(), 123456);
    harness.check (zero.hashCode(),0);
    harness.check (a.hashCode() != b.hashCode());
  }

/**
* tests the Boolean object overwrites getClass()
*/
  public void testGetClass()
  {
    Integer a = new Integer((int)'a');
    Integer b = new Integer( 0);
    Long l = new Long(0L);

    harness.checkPoint("TYPE(public)java.lang.Class");
    try
    {
      harness.check (a instanceof Integer );
      harness.check (b instanceof Integer );
      harness.check (a.getClass().getName(), "java.lang.Integer");
      harness.check (b.getClass().getName(), "java.lang.Integer");
      harness.check (a.getClass(), Class.forName("java.lang.Integer") );
      harness.check (b.getClass(), Class.forName("java.lang.Integer") );
      harness.check (l.getClass() != Class.forName("java.lang.Integer") );
      harness.check (a.getClass(), b.getClass());
      harness.check (a.getClass() != l.getClass());
      harness.check ((Integer.TYPE).getName(), "int");
//      harness.check ( Short.TYPE, Class.forName("boolean"));
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
		harness.setclass("java.lang.Integer");
		testConstructors();
		testCompare();
		testStringConversion();
		testStringValueParsing();
		testValueConversion();
//	 	testProperties(); // not defined
	 	testHashCode();
	 	testGetClass();
	}

}
