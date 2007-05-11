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

package gnu.testlet.wonka.lang.Long;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.Properties;

public class LongTest implements Testlet
{
  protected static TestHarness harness;

/**
* tests the Long constructors Long(long) and Long(String), also checks the initialisation of the types
* by calling on Long.equals and Long.longValue();
* (by doing so, also tests the limits Long.MIN_VALUE and Long.MAX_VALUE
*/
  public void testConstructors()
  {
    harness.checkPoint("Long(long)/ Long");
    Long a = new Long(0L);
    Long b = new Long(1L);
    Long c = new Long(0L);
    Long d = a;
    Long e = new Long(0xe);
    long flong = 'f';
    Long f = new Long(flong);
    Long g = new Long((long)'a');
    Integer i = new Integer(0);

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
    harness.check(!a.equals(i));

    harness.checkPoint("longValue()long");
    harness.check( a.longValue(), 0l);
    harness.check( a.longValue(), a.longValue());
    harness.check( a.longValue(), c.longValue());
    harness.check( a.longValue(), d.longValue());
    harness.check( a.longValue(), i.longValue());
    harness.check( b.longValue(), 1l);
    harness.check( e.longValue(), 14l);
    harness.check( f.longValue(), flong);
    harness.check( g.longValue(), (long)'a');
    harness.check( g.longValue(), 0x61l);
    harness.check( g.longValue(), 97l);

    harness.checkPoint("MAX_VALUE(public)long");
    harness.check (Long.MAX_VALUE,  0x7fffffffffffffffL);
    harness.checkPoint("MIN_VALUE(public)long");
    harness.check (Long.MIN_VALUE, -0x8000000000000000L);

    harness.checkPoint("Long(java.lang.String)");
    constructMustSucceed("1", 1);
    constructMustSucceed("9223372036854775807", 0x7fffffffffffffffL);
    constructMustSucceed("-9223372036854775808", -0x8000000000000000L);
    constructMustFail("9223372036854775808");
    constructMustFail("-9223372036854775809");
    constructMustFail("0x77");
    constructMustFail("#77");
    constructMustFail("4f");
    constructMustFail("0x4f");
    constructMustFail(" ");
    constructMustFail("");
    constructMustFail(null);
    constructMustFail("  1 ");
  }

  private void constructMustSucceed(String line, long expected)
  {
    try
    {
      Long constructed = new Long(line);
      harness.check(constructed.longValue(), expected);
    }
    catch(NumberFormatException e8)
    {
      harness.fail("Could not construct desired value <" + line + ">\n"+e8);
    }

  }

  private void constructMustFail(String line)
  {
    try
    {
      new Long(line);
      harness.fail("Attempt to construct out-of-range long < " + line + " > ");
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
		harness.checkPoint("compareTo(java.lang.Long)int");
		checkCompare( 100L, 101L);
		checkCompare(   0L, 101L);
		checkCompare(-101L,-100L);
		checkCompare(-100L,   0L);
		checkCompare(-101L, 100L);
		checkCompare(0x7ffffffffffffff0L, Long.MAX_VALUE);
		checkCompare(Long.MIN_VALUE,-0x7ffffffffffffff0L);
		
		harness.checkPoint("compareTo(java.lang.Object)int");
    try
    {
  		Long cha1 = new Long((long)'a');
      harness.check (cha1.compareTo(new Long((long)'a') ) == 0 );
    }
    catch(ClassCastException e)
    {
      harness.fail("Exception comparing two instances of class Long ");
    }

    try
    {
  		Long cha1 = new Long((long)'a');
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
  		Long cha1 = new Long(0x7ffffffeL);
  		Integer cha2 = new Integer(0x7ffffffe);
      cha1.compareTo(cha2);
      harness.fail("Attempt to compare two different objects ");
    }
    catch(ClassCastException e)
    {
      harness.check(true);
    }
	}
	
	private void checkCompare(long smallvalue, long bigvalue)
	{
	  try
	  {
	    Long smalllong = new Long(smallvalue);
	    Long   biglong = new Long(  bigvalue);
	
	    if(smalllong.compareTo(biglong) > 0)
	      harness.fail("compareTo detected <"+smallvalue+"> bigger then <"+bigvalue+">");
	    else if(smalllong.compareTo(biglong) == 0)
	      harness.fail("compareTo detected <"+smallvalue+"> equal to <"+bigvalue+">");
	    else
	      harness.check(true);
	
	    if(biglong.compareTo(smalllong) < 0)
	      harness.fail("compareTo detected <"+bigvalue+"> smaller then <"+smallvalue+">");
	    else if(biglong.compareTo(smalllong) == 0)
	      harness.fail("compareTo detected <"+bigvalue+"> equal to <"+smallvalue+">");
	    else
	      harness.check(true);
	
	    if(smalllong.compareTo(smalllong)!= 0)
	      harness.fail("compareTo detected <"+smallvalue+"> not equal to itselves");
	    else
	      harness.check(true);
	
	    if(biglong.compareTo(biglong)!= 0)
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
* tests the conversion Long to and from String using toString() and decode() functions
*/
	public void testStringConversion()
	{
		harness.checkPoint("toString(long)java.lang.String");
		long along = (long)'a';
		long zerolong = 0;
		Long a = new Long(along);
		Long zero = new Long(0);
		String astring = "97";
		
		harness.check(zero.toString(), "0");
		harness.check(Long.toString(zerolong), "0");
		harness.check(a.toString(), astring);
		harness.check(a.toString(), Long.toString(along));
		
 		harness.checkPoint("toBinaryString(long)java.lang.String");
    harness.check (Long.toBinaryString(0L), "0");
    harness.check (Long.toBinaryString(1L), "1");
    harness.check (Long.toBinaryString(3L), "11");
    harness.check (Long.toBinaryString(-1L),
	     "1111111111111111111111111111111111111111111111111111111111111111");
		
    harness.checkPoint ("toOctalString(long)java.lang.String");
    harness.check (Long.toOctalString(0L), "0");
    harness.check (Long.toOctalString(1L), "1");
    harness.check (Long.toOctalString(9L), "11");
    harness.check (Long.toOctalString(-1L),"1777777777777777777777");

    harness.checkPoint ("toHexString(long)java.lang.String");
    harness.check (Long.toHexString(0L), "0");
    harness.check (Long.toHexString(1L), "1");
    harness.check (Long.toHexString(17L),"11");
    harness.check (Long.toHexString(31L),"1f");
    harness.check (Long.toHexString(-1L),"ffffffffffffffff");
		
		harness.checkPoint("toString(long,int)java.lang.String");
		harness.check(Long.toString(along,2) , Long.toBinaryString(along));
		harness.check(Long.toString(along,8) , Long.toOctalString(along));
    harness.check(Long.toString(along,10), Long.toString(along));
		harness.check(Long.toString(along,16), Long.toHexString(along));
		
		harness.check(Long.toString(along,1), Long.toString(along));
		harness.check(Long.toString(along,37), Long.toString(along));
				
		harness.check(Long.toString(4l,3),"11");
		harness.check(Long.toString(11l,11), "10");
		harness.check(Long.toString(21l,11), "1a");
		harness.check(Long.toString(21l, 20),"11");
		harness.check(Long.toString(20l,20), "10");
		harness.check(Long.toString(39l,20), "1j");
		harness.check(Long.toString(37l,36),"11");
		harness.check(Long.toString(36l,36), "10");
		harness.check(Long.toString(71l,36), "1z");
		

    harness.checkPoint("decode(java.lang.String)java.lang.Long");
		decodeMustPass(   "11", 11L );
		decodeMustPass(  "011",  9L);
		decodeMustPass(  "#11", 17L);
		decodeMustPass( "0x11", 17L);
		decodeMustPass( "0xCAFEBABBE", 0xcafebabbel);
		decodeMustPass( "0xd0edef0efe", 0xD0EDEf0EFEL);
		decodeMustPass( "0", 0);
		
		//harness.checkPoint("Byte.decode(String): negative syntax");
		/** NOTE: for some reason, the Java SDK seems to demand 0-7 for -07, #-f for -#f and 0x-f for -0xf. allthough the java compiler protests
		when trying to compile a string 0x-f...  */
		decodeMustPass(  "-11",-11L);
		decodeMustPass( "-011", -9L);
		decodeMustFail( "0-11");
		decodeMustPass( "-#11",-17L);
		decodeMustFail( "#-11");
		decodeMustPass("-0x11",-17L);
		decodeMustFail("0x-11");
		
		decodeMustPass( "0x7fffffffffffffff", Long.MAX_VALUE);
		decodeMustPass( "0x7FFFFFFFFFFFFFFF", Long.MAX_VALUE);
		
		harness.check(Long.decode( "0x7fffffffffffffff"), new Long(Long.MAX_VALUE));
		harness.check(Long.decode( "0x7FFFFFFFFFFFFFFF"), new Long(Long.MAX_VALUE));
		harness.check(Long.decode("-0x8000000000000000"), new Long(Long.MIN_VALUE));
		
		//harness.checkPoint("Long.decode(String) : exceptions");
		decodeMustFail( "0x8000000000000000");
		decodeMustFail("-0x8000000000000001");
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
	
	private void decodeMustPass(String line, long checkvalue)
	{
	  try
	  {
	    Long decoded = Long.decode(line);
	    harness.check( decoded.longValue(),checkvalue );
	  }
	  catch(Exception e)
	  {
	    harness.fail("Exception while trying to decode string <" + line + ">");
	  }
	}
		private void decodeMustFail(String line)
	{
	  try
	  {
	    Long.decode(line);
	    harness.fail("Attempt to decode illegal string format <" + line + ">");
	  }
	  catch(Exception e)
	  {
	    harness.check(true);
	  }
	}
	
/**
* tests the conversion from String to Long class/long primitive with different radix
*/
	public void testStringValueParsing()
	{	
		String astring = new String("100");;
		Long target = new Long(100);
		
		harness.checkPoint("parseLong(java.lang.String)long");
		harness.check(Long.parseLong(astring), 100);
		harness.check( (Long.valueOf(astring)).equals(target) );
		harness.check(Long.parseLong("0"), 0);
		harness.check(Long.parseLong("-34"), -34);
		harness.check( (Long.valueOf("-56")).equals(new Long(-56)) );
		harness.check(Long.parseLong("9223372036854775807"), 9223372036854775807l);
		harness.check( (Long.valueOf("9223372036854775807")).equals(new Long(Long.MAX_VALUE)) );
		harness.check(Long.parseLong("-9223372036854775808"), -9223372036854775808l);
		harness.check( (Long.valueOf("-9223372036854775808")).equals(new Long(Long.MIN_VALUE)) );
		
		harness.checkPoint("valueOf(java.lang.String)java.lang.Long");
 	  parseCheckMustFail( "9223372036854775808", 10);
 	  valueCheckMustFail( "9223372036854775808", 10);
 	  parseCheckMustFail("-9223372036854775809", 10);
 	  valueCheckMustFail("-9223372036854775809", 10);
 	  parseCheckMustFail("0x60", 10);
 	  valueCheckMustFail("0x60", 10);
 	  parseCheckMustFail(" ", 10);
 	  valueCheckMustFail(" ", 10);
 	  parseCheckMustFail("", 10);
 	  valueCheckMustFail("", 10);
 	  parseCheckMustFail(null, 10);
 	  valueCheckMustFail(null, 10);
 	  parseCheckMustFail(" 78  ", 10);
 	  valueCheckMustFail(" 78  ", 10);
		
	
		//harness.checkPoint("long parseLong(String, radix)  / Long valueOf(String, radix)");
		harness.check(Long.parseLong( "12", 10), Long.parseLong( "12") );
		harness.check(Long.parseLong("-34", 10), Long.parseLong("-34") );
		harness.check( (Long.valueOf( "56", 10)),  Long.valueOf( "56") );
		harness.check( (Long.valueOf("-78", 10)),  Long.valueOf("-78") );
		
		harness.check(Long.parseLong( "11", 2), Long.parseLong( "3"), "parseLong binary " );
		harness.check(Long.parseLong("-11", 2), Long.parseLong("-3"), "parseLong binary negative" );
		harness.check(  Long.valueOf( "11", 2),   Long.valueOf( "3"),   "valueOf binary " );
		harness.check(  Long.valueOf("-11", 2),   Long.valueOf("-3"),   "valueOf binary negative" );

		harness.check(Long.parseLong( "11", 3), Long.parseLong( "4"), "parseLong 3-based " );
		harness.check(Long.parseLong("-11", 3), Long.parseLong("-4"), "parseLong 3-based negative" );
		harness.check(  Long.valueOf( "11", 3),   Long.valueOf( "4"),   "valueOf 3-based " );
		harness.check(  Long.valueOf("-11", 3),   Long.valueOf("-4"),   "valueOf 3-based negative" );

		harness.check(Long.parseLong( "11", 8), Long.parseLong( "9"), "parseLong octal " );
		harness.check(Long.parseLong("-11", 8), Long.parseLong("-9"), "parseLong octal negative" );
		harness.check(  Long.valueOf( "11", 8),   Long.valueOf( "9"),   "valueOf octal " );
		harness.check(  Long.valueOf("-11", 8),   Long.valueOf("-9"),   "valueOf octal negative" );
		
		harness.check(Long.parseLong( "11", 16), Long.parseLong( "17"), "parseLong hex " );
		harness.check(Long.parseLong("-11", 16), Long.parseLong("-17"), "parseLong hex negative" );
		harness.check(  Long.valueOf( "11", 16),   Long.valueOf( "17"),   "valueOf hex " );
		harness.check(  Long.valueOf("-11", 16),   Long.valueOf("-17"),   "valueOf hex negative" );
		harness.check(Long.parseLong( "f" , 16), Long.parseLong( "15"), "parseLong hex " );
		harness.check(Long.parseLong("-f" , 16), Long.parseLong("-15"), "parseLong hex negative" );
		harness.check(Long.parseLong( "F" , 16), Long.parseLong( "15"), "parseLong hex capital" );
		harness.check(  Long.valueOf( "f" , 16),   Long.valueOf( "15"),   "valueOf hex " );
		harness.check(  Long.valueOf("-f" , 16),   Long.valueOf("-15"),   "valueOf hex negative" );
		harness.check(  Long.valueOf( "F" , 16),   Long.valueOf( "15"),   "valueOf hex capital" );
		
		harness.check(Long.parseLong( "11", 25), Long.parseLong( "26"), "parseLong 25-based " );
		harness.check(Long.parseLong("-11", 25), Long.parseLong("-26"), "parseLong 25-based negative" );
		harness.check(  Long.valueOf( "11", 25),   Long.valueOf( "26"),   "valueOf 25-based " );
		harness.check(  Long.valueOf("-11", 25),   Long.valueOf("-26"),   "valueOf 25-based negative" );
		harness.check(Long.parseLong( "o" , 25), Long.parseLong( "24"), "parseLong 25-based " );
		harness.check(Long.parseLong( "O" , 25), Long.parseLong( "24"), "parseLong 25-based capital" );
		harness.check(Long.parseLong("-o" , 25), Long.parseLong("-24"), "parseLong 25-based negative" );
		harness.check(  Long.valueOf( "o" , 25),   Long.valueOf( "24"),   "valueOf 25-based " );
		harness.check(  Long.valueOf( "O" , 25),   Long.valueOf( "24"),   "valueOf 25-based capital" );
		harness.check(  Long.valueOf("-o" , 25),   Long.valueOf("-24"),   "valueOf 25-based negative" );
		
		harness.check(Long.parseLong( "11", 36), Long.parseLong( "37"), "parseLong 36-based " );
		harness.check(Long.parseLong("-11", 36), Long.parseLong("-37"), "parseLong 36-based negative" );
		harness.check(  Long.valueOf( "11", 36),   Long.valueOf( "37"),   "valueOf 36-based " );
		harness.check(  Long.valueOf("-11", 36),   Long.valueOf("-37"),   "valueOf 36-based negative" );
		harness.check(Long.parseLong( "z" , 36), Long.parseLong( "35"), "parseLong 36-based " );
		harness.check(Long.parseLong( "Z" , 36), Long.parseLong( "35"), "parseLong 36-based capital" );
		harness.check(Long.parseLong("-z" , 36), Long.parseLong("-35"), "parseLong 36-based negative" );
		harness.check(  Long.valueOf( "z" , 36),   Long.valueOf( "35"),   "valueOf 36-based " );
		harness.check(  Long.valueOf( "Z" , 36),   Long.valueOf( "35"),   "valueOf 36-based capital" );
		harness.check(  Long.valueOf("-z" , 36),   Long.valueOf("-35"),   "valueOf 36-based negative" );
		
		
		//harness.checkPoint("parseLong(String, long)/ valueOf(String, int) : exceptions");
  	  parseCheckMustFail(" 78  ", 2);
  	  valueCheckMustFail(" 78  ", 2);
  	  parseCheckMustFail(" 78  ", 3);
  	  valueCheckMustFail(" 78  ", 3);
  	  parseCheckMustFail(" 78  ", 8);
  	  valueCheckMustFail(" 78  ", 8);
  	  parseCheckMustFail(" 78  ", 16);
  	  valueCheckMustFail(" 78  ", 16);
  	  parseCheckMustFail(" 78  ", 25);
  	  valueCheckMustFail(" 78  ", 25);
  	  parseCheckMustFail(" 78  ", 36);
  	  valueCheckMustFail(" 78  ", 36);
		
		//harness.checkPoint("parseLong(String, int)/ valueOf(String, int) : radix exceptions");
		  parseCheckMustFail("11",1);
		  valueCheckMustFail("11", 1);
		  parseCheckMustFail("11",37);
		  valueCheckMustFail("11", 37);
		
		//harness.checkPoint("parseLong(String, int)/ valueOf(String, int) : out-of-bound exceptions");
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
		
		//harness.checkPoint("parseLong(String, int)/ valueOf(String, int) : int boundaries exceptions");
		  parseCheckMustFail("1000000000000000000000000000000000000000000000000000000000000000",2);
		  valueCheckMustFail("1000000000000000000000000000000000000000000000000000000000000000", 2);
		  parseCheckMustFail("1000000000000000000000",8);
		  valueCheckMustFail("1000000000000000000000", 8);
		  parseCheckMustFail("8000000000000000",16);
		  valueCheckMustFail("8000000000000000", 16);
		  parseCheckMustFail("8000000000000",32);
		  valueCheckMustFail("8000000000000", 32);
		
		  parseCheckMustFail("-1000000000000000000000000000000000000000000000000000000000000001",2);
		  valueCheckMustFail("-1000000000000000000000000000000000000000000000000000000000000001", 2);
		  parseCheckMustFail("-1000000000000000000001",8);
		  valueCheckMustFail("-1000000000000000000001", 8);
		  parseCheckMustFail("-8000000000000001",16);
		  valueCheckMustFail("-8000000000000001", 16);
		  parseCheckMustFail("-8000000000001",32);
		  valueCheckMustFail("-8000000000001", 32);
	
	}

  private void parseCheckMustFail(String line, int radix)
  {
		try
		{
		  Long.parseLong(line, radix);
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
		  Long.valueOf(line, radix);
		  harness.fail("Attempt to get value from illegal long string <" + line + ">");
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
		Long zero  = new Long( 0 );
		Long along = new Long((long)'a');
		Long min   = new Long(Long.MIN_VALUE);
		Long max   = new Long(Long.MAX_VALUE);
		
		
		// as the check comparisons by itself convert to longeger, float or string, it is better to do a direct (value == expected)??
		// instead of Boolean check(long target, long expected)		
		harness.checkPoint("Value conversins");
		harness.check( zero.byteValue() == 0);
		harness.check(along.byteValue() == (byte)'a');
		harness.check(along.byteValue() == (byte)(along.longValue()) );		
		harness.check(max.byteValue() == (byte) 9223372036854775807l);
		harness.check(min.byteValue() == (byte)-9223372036854775808l);
		
		//harness.checkPoint("Value conversins: Long => shortValue");
		harness.check( zero.shortValue() == 0);
		harness.check(along.shortValue() == (short)'a');
		harness.check(along.shortValue() == (short)(along.longValue()) );		
		harness.check(max.shortValue() == (short) 9223372036854775807l);
		harness.check(min.shortValue() == (short)-9223372036854775808l);
		
		//harness.checkPoint("Value conversions: Long => intValue");
		harness.check( zero.intValue() == 0);
		harness.check(along.intValue() == (int)'a');
		harness.check(max.intValue() == (int) 9223372036854775807l);
		harness.check(min.intValue() == (int)-9223372036854775808l);
		
		//harness.checkPoint("Value conversins: Long => longValue");
		harness.check( zero.longValue() == 0l);
		harness.check(along.longValue() == (long)'a');
		harness.check(along.longValue() == (long)(along.longValue()) );		
		harness.check(max.longValue() == 9223372036854775807l);
		harness.check(min.longValue() ==-9223372036854775808l);
		
		//harness.checkPoint("Value conversins: Long => floatValue");
		harness.check( zero.floatValue() == 0.0f);
		harness.check(along.floatValue() == (float)'a');
		harness.check(along.floatValue() == (float)(along.longValue()) );		
		harness.check(max.floatValue() == 9223372036854775807.0f);
		harness.check(min.floatValue() ==-9223372036854775808.0f);
		
		//harness.checkPoint("Value conversins: Long => doubleValue");
		harness.check( zero.doubleValue() ,0.0);
		harness.check(along.doubleValue() ,(double)'a');
		harness.check(along.doubleValue() ,(double)(along.longValue()) );		
		harness.check(max.doubleValue() , 9223372036854775807.0);
		harness.check(min.doubleValue() ,-9223372036854775808.0);
	}

/**
* tests the properties put() and getLong() methods
*/
  public void testProperties()
  {
      // Augment the System properties with the following.
      // Overwriting is bad because println needs the
      // platform-dependent line.separator property.
      harness.checkPoint("getLong(java.lang.String)long");
      Properties p = System.getProperties();
      p.put("along", "97");
      p.put("zero" , "0");
      p.put("newa" , "97");
      Long along = new Long('a');

      harness.check (Long.getLong("along"), along);
      harness.check (Long.getLong("zero"), new Long(0));
      harness.check (Long.getLong("along") == Long.getLong("newa") );
      harness.check (Long.getLong("along") != Long.getLong("zero") );
      harness.check (Long.getLong("blong") == null );
      harness.check (Long.getLong("clong",97), along );
      harness.check (Long.getLong("dlong", along), along);
  }

/**
* tests the Boolean object overwrites hashCode()
*/
  public void testHashCode()
  {
    Long a = new Long((long)'a');
    long blong = 0x123456789abcdef0l;
    Long b = new Long(blong);
    Long zero = new Long( 0);
    Long newa = new Long((long)'a');

    harness.checkPoint("hashCode()int");
    harness.check (a.hashCode(), newa.hashCode());
    harness.check (a.hashCode(), (int)'a');
    harness.check (b.hashCode(), (int)(blong^blong>>>32));
    harness.check (zero.hashCode(),0);
  }

/**
* tests the Boolean object overwrites getClass()
*/
  public void testGetClass()
  {
    Long a = new Long((long)'a');
    Long b = new Long( 0l);
    Integer i = new Integer(0);
    Long c = new Long(0x123456789abcdef0l);

    harness.checkPoint("TYPE(public)java.lang.Class");
    try
    {
      harness.check (a instanceof Long );
      harness.check (b instanceof Long );
      harness.check (a.getClass().getName(), "java.lang.Long");
      harness.check (b.getClass().getName(), "java.lang.Long");
      harness.check (a.getClass(), Class.forName("java.lang.Long") );
      harness.check (b.getClass(), Class.forName("java.lang.Long") );
      harness.check (i.getClass() != Class.forName("java.lang.Long") );
      harness.check (a.getClass(), b.getClass());
      harness.check (a.getClass() != i.getClass());
      harness.check ((Long.TYPE).getName(), "long");
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
		harness.setclass("java.lang.Long");
		int i = 45;
		int j = -999;
		harness.check(j/i ,-22 , "-999 / 45 = -22");
		j = 999;
		harness.check(j/i , 22 , " 999 / 45 =  22");
		i = -45;
		harness.check(j/i ,-22 , " 999 /-45 = -22");
		j = -999;
		harness.check(j/i , 22 , "-999 /-45 =  22");
		
		
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
