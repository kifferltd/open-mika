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

package gnu.testlet.wonka.lang.Short;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class ShortTest implements Testlet
{
  protected static TestHarness harness;

/**
* tests the Short constructors Short(short) and Short(String), also checks the initialisation of the types
* by calling on Short.equals and Short.ShortValue();
* (by doing so, also tests the limits Short.MIN_VALUE and Short.MAX_VALUE
*/
  public void testConstructors()
  {
    harness.checkPoint("Short(short)");
    Short a = new Short((short)0);
    Short b = new Short((short)1);
    Short c = new Short((short)0);
    Short d = a;
    Short e = new Short((short)0xe);
    short fshort = (short)'f';
    Short f = new Short(fshort);
    Short g = new Short((short)'a');
    Integer i = new Integer(0);

    harness.check( a != null);
    harness.checkPoint("equals(java.lang.Object)boolean");
    harness.check(!a.equals(null));
    harness.check( a != b     );
    harness.check(!a.equals(b));
    harness.check( a != c     );
    harness.check( a.equals(c));
    harness.check( a == d     );
    harness.check( a.equals(d));
    harness.check( a == a     );
    harness.check(!a.equals(i));

    harness.checkPoint("shortValue()short");
    harness.check( a.shortValue(), 0);
    harness.check( a.shortValue(), a.shortValue());
    harness.check( a.shortValue(), c.shortValue());
    harness.check( a.shortValue(), d.shortValue());
    harness.check( (int)a.shortValue(), i.intValue());
    harness.check( b.shortValue(), 1);
    harness.check( e.shortValue(), 14);
    harness.check( f.shortValue(), fshort);
    harness.check( g.shortValue(), (short)'a');
    harness.check( g.shortValue(), 0x61);
    harness.check( g.shortValue(), 97);

    harness.checkPoint("MAX_VALUE(public)short");
    harness.check(Short.MAX_VALUE, 0x7FFF);
    harness.checkPoint("MIN_VALUE(public)short");
    harness.check(Short.MIN_VALUE,-0x8000);

    harness.checkPoint("Short(java.lang.String)");
    constructMustSucceed("1",1);
    constructMustSucceed("32767",32767);
    constructMustSucceed("-32768",-32768);
//    constructMustSucceed("  1 ",1);

    constructMustFail("32768");
    constructMustFail("0x77");
    constructMustFail("10a");
    constructMustFail(" ");
    constructMustFail("");
    constructMustFail(null);
  }

  private void constructMustSucceed(String line, int expected)
  {
    try
    {
      Short constructed = new Short(line);
      harness.check(constructed.shortValue(), expected);
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
      new Short(line);
      harness.fail("Attempt to construct out-of-range short < " + line + " > ");
    }
    catch(Exception e) //(NumberFormatException e)
    {
      harness.check(true);
    }

  }

/**
* tests the compareTo compare/orderring functions
*/
	public void testCompare()
	{
		harness.checkPoint("compareTo(java.lang.Short)int");
		checkCompare(100, 101);
		checkCompare(0, 101);
		checkCompare(-101, -100);
		checkCompare(-100, 0);
		checkCompare(-101, 100);
		checkCompare(55, (int)Byte.MAX_VALUE);
		checkCompare((int)Byte.MIN_VALUE,-55);
		
		harness.checkPoint("compareTo(java.lang.Object)int");
    try
    {
  		Short cha1 = new Short((short)'a');
      harness.check (cha1.compareTo(new Short((short)'a') ) == 0 );
    }
    catch(ClassCastException e)
    {
      harness.fail("Exception comparing two instances of class Byte ");
    }

    try
    {
  		Short cha1 = new Short((short)'a');
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
  		Short cha1 = new Short((short)497);
  		Integer cha2 = new Integer(497);
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
	    Short smallshort = new Short((short)smallvalue);
	    Short   bigshort = new Short((short)  bigvalue);
	
	    if(smallshort.compareTo(bigshort) > 0)
	      harness.fail("compareTo detected <"+smallvalue+"> bigger then <"+bigvalue+">");
	    else if(smallshort.compareTo(bigshort) == 0)
	      harness.fail("compareTo detected <"+smallvalue+"> equal to <"+bigvalue+">");
	    else
	      harness.check(true);
	
	    if(bigshort.compareTo(smallshort) < 0)
	      harness.fail("compareTo detected <"+bigvalue+"> smaller then <"+smallvalue+">");
	    else if(bigshort.compareTo(smallshort) == 0)
	      harness.fail("compareTo detected <"+bigvalue+"> equal to <"+smallvalue+">");
	    else
	      harness.check(true);
	
	    if(smallshort.compareTo(smallshort)!= 0)
	      harness.fail("compareTo detected <"+smallvalue+"> not equal to itselves");
	    else
	      harness.check(true);
	
	    if(bigshort.compareTo(bigshort)!= 0)
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
* tests the conversion Short to and from String using toString() and decode() functions
*/
	public void testStringConversion()
	{
		harness.checkPoint("toString(short)java.lang.String");
		short ashort = (short)'a';
		short zeroshort = 0;
		short negshort = (short)-397;
		Short a = new Short(ashort);
		Short zero = new Short((short)0);
		Short negative = new Short(negshort);
		
		String astring = "97";
		String negstring = "-397";
		
		harness.check(zero.toString(), "0");
		harness.check(Short.toString(zeroshort), "0");
		harness.check(a.toString(), astring);
		harness.check(a.toString(), Short.toString(ashort));
		harness.check(negative.toString(), "-397");
		harness.check(negative.toString(), Short.toString(negshort) );
				
		harness.checkPoint("decode(java.lang.String)java.lang.Short");
		decodeMustPass(   "11", 11 );
		decodeMustPass(  "011",  9);
		decodeMustPass(  "#11", 17);
		decodeMustPass( "0x11", 17);
		decodeMustPass( "0x1FAB", 0x1fab);
		decodeMustPass( "0x1fab", 0x1FAB);
		
		//harness.checkPoint("Byte.decode(String): negative syntax");
		/** NOTE: for some reason, the Java SDK seems to demand 0-7 for -07, #-f for -#f and 0x-f for -0xf. in the Byte-class
		but behaves normally in the Short*/
		decodeMustPass(  "-11",-11);
		decodeMustPass( "-011", -9);
		decodeMustFail( "0-11");
		decodeMustPass( "-#11",-17);
		decodeMustFail( "#-11");
		decodeMustPass("-0x11",-17);
		decodeMustFail("0x-11");
		
		decodeMustPass( "0x7fff", (int)Short.MAX_VALUE);
		decodeMustPass( "0x7FFF", (int)Short.MAX_VALUE);
		
		//harness.checkPoint("Short.decode(String) : exceptions");
		decodeMustFail( "0x8000");
		decodeMustFail("-32769");
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
	    Short decoded = Short.decode(line);
	    harness.check( decoded.intValue(),checkvalue );
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
	    Short.decode(line);
	    harness.fail("Attempt to decode illegal string format <" + line + ">");
	  }
	  catch(Exception e)
	  {
	    harness.check(true);
	  }
	}
	
/**
* tests the conversion from String to Short class/short primitive with different radix
*/
	public void testStringValueParsing()
	{	
		String astring = new String("100");;
		Short target = new Short((short)100);
		
		harness.checkPoint("parseShort(java.lang.String)short");
		harness.check(Short.parseShort(astring), 100);
		harness.check( (Short.valueOf(astring)).equals(target) );
		harness.check(Short.parseShort("-34"), -34);
		harness.check( (Short.valueOf("-56")).intValue(),-56 );
		harness.check(Short.parseShort("32767"), (int)Short.MAX_VALUE);
		harness.check( (Short.valueOf( "32767")).equals(new Short(Short.MAX_VALUE)) );
		harness.check(Short.parseShort("-32768"), (int)Short.MIN_VALUE);
		harness.check( (Short.valueOf("-32768")).equals(new Short(Short.MIN_VALUE)) );
		
		harness.checkPoint("valueOf(java.lang.String)java.lang.Short");
 	  parseCheckMustFail(" 60  ", 10);
 	  valueCheckMustFail(" 60  ", 10);
 	  parseCheckMustFail("32768", 10);
 	  valueCheckMustFail("32768", 10);
 	  parseCheckMustFail( "#60", 10);
 	  valueCheckMustFail( "#60", 10);
 	  parseCheckMustFail("0x60", 10);
 	  valueCheckMustFail("0x60", 10);		
	
		//harness.checkPoint("short parseShort(String, radix)  / Short valueOf(String, radix)");
		harness.check(Short.parseShort( "12", 10), Short.parseShort( "12") );
		harness.check(Short.parseShort("-34", 10), Short.parseShort("-34") );
		harness.check( (Short.valueOf( "56", 10)),  Short.valueOf( "56") );
		harness.check( (Short.valueOf("-78", 10)),  Short.valueOf("-78") );
		
		harness.check(Short.parseShort( "11", 2), Short.parseShort( "3"), "parseShort binary " );
		harness.check(Short.parseShort("-11", 2), Short.parseShort("-3"), "parseShort binary negative" );
		harness.check(  Short.valueOf( "11", 2),   Short.valueOf( "3"),   "valueOf binary " );
		harness.check(  Short.valueOf("-11", 2),   Short.valueOf("-3"),   "valueOf binary negative" );

		harness.check(Short.parseShort( "11", 3), Short.parseShort( "4"), "parseShort 3-based " );
		harness.check(Short.parseShort("-11", 3), Short.parseShort("-4"), "parseShort 3-based negative" );
		harness.check(  Short.valueOf( "11", 3),   Short.valueOf( "4"),   "valueOf 3-based " );
		harness.check(  Short.valueOf("-11", 3),   Short.valueOf("-4"),   "valueOf 3-based negative" );

		harness.check(Short.parseShort( "11", 8), Short.parseShort( "9"), "parseShort octal " );
		harness.check(Short.parseShort("-11", 8), Short.parseShort("-9"), "parseShort octal negative" );
		harness.check(  Short.valueOf( "11", 8),   Short.valueOf( "9"),   "valueOf octal " );
		harness.check(  Short.valueOf("-11", 8),   Short.valueOf("-9"),   "valueOf octal negative" );
		
		harness.check(Short.parseShort( "11", 16), Short.parseShort( "17"), "parseShort hex " );
		harness.check(Short.parseShort("-11", 16), Short.parseShort("-17"), "parseShort hex negative" );
		harness.check(  Short.valueOf( "11", 16),   Short.valueOf( "17"),   "valueOf hex " );
		harness.check(  Short.valueOf("-11", 16),   Short.valueOf("-17"),   "valueOf hex negative" );
		harness.check(Short.parseShort( "f" , 16), Short.parseShort( "15"), "parseShort hex " );
		harness.check(Short.parseShort("-f" , 16), Short.parseShort("-15"), "parseShort hex negative" );
		harness.check(Short.parseShort( "F" , 16), Short.parseShort( "15"), "parseShort hex capital" );
		harness.check(  Short.valueOf( "f" , 16),   Short.valueOf( "15"),   "valueOf hex " );
		harness.check(  Short.valueOf("-f" , 16),   Short.valueOf("-15"),   "valueOf hex negative" );
		harness.check(  Short.valueOf( "F" , 16),   Short.valueOf( "15"),   "valueOf hex capital" );
		
		harness.check(Short.parseShort( "11", 25), Short.parseShort( "26"), "parseShort 25-based " );
		harness.check(Short.parseShort("-11", 25), Short.parseShort("-26"), "parseShort 25-based negative" );
		harness.check(  Short.valueOf( "11", 25),   Short.valueOf( "26"),   "valueOf 25-based " );
		harness.check(  Short.valueOf("-11", 25),   Short.valueOf("-26"),   "valueOf 25-based negative" );
		harness.check(Short.parseShort( "o" , 25), Short.parseShort( "24"), "parseShort 25-based " );
		harness.check(Short.parseShort( "O" , 25), Short.parseShort( "24"), "parseShort 25-based capital" );
		harness.check(Short.parseShort("-o" , 25), Short.parseShort("-24"), "parseShort 25-based negative" );
		harness.check(  Short.valueOf( "o" , 25),   Short.valueOf( "24"),   "valueOf 25-based " );
		harness.check(  Short.valueOf( "O" , 25),   Short.valueOf( "24"),   "valueOf 25-based capital" );
		harness.check(  Short.valueOf("-o" , 25),   Short.valueOf("-24"),   "valueOf 25-based negative" );
		
		harness.check(Short.parseShort( "11", 36), Short.parseShort( "37"), "parseShort 36-based " );
		harness.check(Short.parseShort("-11", 36), Short.parseShort("-37"), "parseShort 36-based negative" );
		harness.check(  Short.valueOf( "11", 36),   Short.valueOf( "37"),   "valueOf 36-based " );
		harness.check(  Short.valueOf("-11", 36),   Short.valueOf("-37"),   "valueOf 36-based negative" );
		harness.check(Short.parseShort( "z" , 36), Short.parseShort( "35"), "parseShort 36-based " );
		harness.check(Short.parseShort( "Z" , 36), Short.parseShort( "35"), "parseShort 36-based capital" );
		harness.check(Short.parseShort("-z" , 36), Short.parseShort("-35"), "parseShort 36-based negative" );
		harness.check(  Short.valueOf( "z" , 36),   Short.valueOf( "35"),   "valueOf 36-based " );
		harness.check(  Short.valueOf( "Z" , 36),   Short.valueOf( "35"),   "valueOf 36-based capital" );
		harness.check(  Short.valueOf("-z" , 36),   Short.valueOf("-35"),   "valueOf 36-based negative" );
		
		
		//harness.checkPoint("parseShort(String, int)/ valueOf(String, int) : exceptions");
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
		
		//harness.checkPoint("parseShort(String, int)/ valueOf(String, int) : radix exceptions");
		  parseCheckMustFail("11",1);
		  valueCheckMustFail("11", 1);
		  parseCheckMustFail("11",37);
		  valueCheckMustFail("11", 37);
		
		//harness.checkPoint("parseShort(String, int)/ valueOf(String, int) : out-of-bound exceptions");
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
		
		//harness.checkPoint("parseShort(String, int)/ valueOf(String, int) : short boundaries exceptions");
		  parseCheckMustFail("1000000000000000",2);
		  valueCheckMustFail("1000000000000000", 2);
		  parseCheckMustFail("100000", 8);
		  valueCheckMustFail("100000", 8);
		  parseCheckMustFail("8000",16);
		  valueCheckMustFail("8000", 16);
		  parseCheckMustFail("1000",32);
		  valueCheckMustFail("1000", 32);
		
		  parseCheckMustFail("-1000000000000001",2);
		  valueCheckMustFail("-1000000000000001", 2);
		  parseCheckMustFail("-100001",8);
		  valueCheckMustFail("-100001", 8);
		  parseCheckMustFail("-8001",16);
		  valueCheckMustFail("-8001", 16);
		  parseCheckMustFail("-1001",32);
		  valueCheckMustFail("-1001", 32);
	
	}

  private void parseCheckMustFail(String line, int radix)
  {
		try
		{
		  Short.parseShort(line, radix);
		  harness.fail("Attempt to parse illegal short string <" + line + ">");
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
		  Short.valueOf(line, radix);
		  harness.fail("Attempt to get value from illegal short string <" + line + ">");
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
		Short zero  = new Short((short) 0 );
		Short ashort = new Short((short)'a');
		Short min   = new Short(Short.MIN_VALUE);
		Short max   = new Short(Short.MAX_VALUE);
		
		
		// as the check comparisons by itself convert to integer, float or string, it is better to do a direct (value == expected)??
		//Boolean check instead of a check(int value, int expected)?? one
		
		harness.checkPoint("Value conversions");
		harness.check( zero.byteValue() == 0);
		harness.check(ashort.byteValue() == (byte)'a');
		harness.check(max.byteValue() == (byte) 32767);
		harness.check(min.byteValue() == (byte)-32768);
		
		//harness.checkPoint("Value conversions: Short => shortValue");
		harness.check( zero.shortValue() == 0);
		harness.check(ashort.shortValue() == (short)'a');
		harness.check(ashort.shortValue() == (short)(ashort.shortValue()) );		
		harness.check(max.shortValue() == 32767);
		harness.check(min.shortValue() ==-32768);
		
		//harness.checkPoint("Value conversions: Short => intValue");
		harness.check( zero.intValue() == 0);
		harness.check(ashort.intValue() == (int)'a');
		harness.check(ashort.intValue() == (int)(ashort.shortValue()) );		
		harness.check(max.intValue() == 32767);
		harness.check(min.intValue() ==-32768);
		
		//harness.checkPoint("Value conversions: Short => longValue");
		harness.check( zero.longValue() == 0l);
		harness.check(ashort.longValue() == (long)'a');
		harness.check(ashort.longValue() == (long)(ashort.shortValue()) );		
		harness.check(max.longValue() == 32767l);
		harness.check(min.longValue() ==-32768l);
		
		//harness.checkPoint("Value conversions: Short => floatValue");
		harness.check( zero.floatValue() == 0.0f);
		harness.check(ashort.floatValue() == (float)'a');
		harness.check(ashort.floatValue() == (float)(ashort.shortValue()) );		
		harness.check(max.floatValue() == 32767.0f);
		harness.check(min.floatValue() ==-32768.0f);
		
		//harness.checkPoint("Value conversions: Short => doubleValue");
		harness.check( zero.doubleValue() == 0.0);
		harness.check(ashort.doubleValue() == (double)'a');
		harness.check(ashort.doubleValue() == (double)(ashort.shortValue()) );		
		harness.check(max.doubleValue() == 32767.0);
		harness.check(min.doubleValue() ==-32768.0);
	}

/*
* tests the properties put() method
* no system properties defined for Short class
  void testProperties()
  {
      // Augment the System properties with the following.
      // Overwriting is bad because println needs the
      // platform-dependent line.separator property.
      harness.checkPoint("Properties.put()");
      Properties p = System.getProperties();
      p.put("ashort", "97");
      p.put("zero" , "0");
      p put("newa" , "97");

      harness.check (Short.getShort("ashort") ==(short)'a');
      harness.check (Short.getShort("zero")  == (short)0);
      harness.check (Short.getShort("ashort") == Short.getShort("newa") );
      harness.check (Short.getShort("ashort") != Short.getShort("zero") );
  }

/**
* tests the Boolean object overwrites hashCode()
*/
  public void testHashCode()
  {
    Short a = new Short((short)'a');
    Short b = new Short((short)-439);
    Short zero = new Short((short) 0);
    Short newa = new Short((short)'a');

    harness.checkPoint("hashCode()int");
    harness.check (a.hashCode(), newa.hashCode());
    harness.check (a.hashCode(), (int)'a');
    harness.check (zero.hashCode(),(int)0);
    harness.check (b.hashCode(), -439);
  }

/**
* tests the Boolean object overwrites getClass()
*/
  public void testGetClass()
  {
    Short a = new Short((short)'a');
    Short b = new Short((short) 0);
    Integer i = new Integer(0);

    harness.checkPoint("TYPE(public)java.lang.Class");
    try
    {
      harness.check (a instanceof Short );
      harness.check (b instanceof Short );
      harness.check (a.getClass().getName(), "java.lang.Short");
      harness.check (b.getClass().getName(), "java.lang.Short");
      harness.check (a.getClass(), Class.forName("java.lang.Short") );
      harness.check (b.getClass(), Class.forName("java.lang.Short") );
      harness.check (i.getClass() != Class.forName("java.lang.Short") );
      harness.check (a.getClass(), b.getClass());
      harness.check (a.getClass() != i.getClass());
      harness.check ((Short.TYPE).getName(), "short");
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
		harness.setclass("java.lang.Short");
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
