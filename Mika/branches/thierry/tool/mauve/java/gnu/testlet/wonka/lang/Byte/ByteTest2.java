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

package gnu.testlet.wonka.lang.Byte;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class ByteTest2 implements Testlet
{
  protected static TestHarness harness;

/**
* tests the Byte constructors Byte(byte) and Byte(String), also checks the initialisation of the types
* by calling on Byte.equals and Byte.byteValue();
* (by doing so, also tests the limits Byte.MIN_VALUE and Byte.MAX_VALUE
*/
  public void testConstructors()
  {
    harness.checkPoint("Byte(byte)");
    Byte a = new Byte((byte)0);
    Byte b = new Byte((byte)1);
    Byte c = new Byte((byte)0);
    Byte d = a;
    Byte e = new Byte((byte)0xe);
    byte fbyte = (byte)'f';
    Byte f = new Byte(fbyte);
    Byte g = new Byte((byte)'a');
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

    harness.checkPoint("byteValue()byte");
    harness.check( a.byteValue(), 0);
    harness.check( a.byteValue(), a.byteValue());
    harness.check( a.byteValue(), c.byteValue());
    harness.check( a.byteValue(), d.byteValue());
    harness.check( (int)a.byteValue(), i.intValue());
    harness.check( b.byteValue(), 1);
    harness.check( e.byteValue(), 14);
    harness.check( f.byteValue(), fbyte);
    harness.check( g.byteValue(), (byte)'a');
    harness.check( g.byteValue(), 0x61);
    harness.check( g.byteValue(), 97);

    harness.checkPoint("MAX_VALUE(public)byte");
    harness.check(Byte.MAX_VALUE, 0x7f);
    harness.checkPoint("MIN_VALUE(public)byte");
    harness.check(Byte.MIN_VALUE,-0x80);

    harness.checkPoint("Byte(java.lang.String)");
    Byte stringbyte;
    constructMustSucceed("1", 1);
    constructMustSucceed("127", 127);
//    constructMustSucceed("  1 ", 1);
    constructMustSucceed("-128", -128);

    constructMustFail("128");
    constructMustFail("-129");
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
      Byte constructed = new Byte(line);
      harness.check(constructed.byteValue(),(byte)expected);
    }
    catch(NumberFormatException e8)
    {
      harness.fail("Could not construct desired value <" + line + "> ");
    }

  }

  private void constructMustFail(String line)
  {
    try
    {
      new Byte(line);
      harness.fail("Attempt to construct out-of-range byte < " + line + " > ");
    }
    catch(Exception e) //(NumberFormatException e8)
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
		checkCompare(55, (int)Byte.MAX_VALUE);
		checkCompare((int)Byte.MIN_VALUE,-55);
		
		//harness.checkPoint("Byte.Compare(Class) : exceptions");
    try
    {
  		Byte cha1 = new Byte((byte)'a');
      harness.check (cha1.compareTo(new Byte((byte)'a') ) == 0 );
    }
    catch(ClassCastException e)
    {
      harness.fail("Exception comparing two instances of class Byte ");
    }

    try
    {
  		Byte cha1 = new Byte((byte)'a');
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
  		Byte cha1 = new Byte((byte)97);
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
	    Byte smallbyte = new Byte((byte)smallvalue);
	    Byte   bigbyte = new Byte((byte)  bigvalue);
	
	    if(smallbyte.compareTo(bigbyte) > 0)
	      harness.fail("compareTo detected <"+smallvalue+"> bigger then <"+bigvalue+">");
	    else if(smallbyte.compareTo(bigbyte) == 0)
	      harness.fail("compareTo detected <"+smallvalue+"> equal to <"+bigvalue+">");
	    else
	      harness.check(true);
	
	    if(bigbyte.compareTo(smallbyte) < 0)
	      harness.fail("compareTo detected <"+bigvalue+"> smaller then <"+smallvalue+">");
	    else if(bigbyte.compareTo(smallbyte) == 0)
	      harness.fail("compareTo detected <"+bigvalue+"> equal to <"+smallvalue+">");
	    else
	      harness.check(true);
	
	    if(smallbyte.compareTo(smallbyte)!= 0)
	      harness.fail("compareTo detected <"+smallvalue+"> not equal to itselves");
	    else
	      harness.check(true);
	
	    if(bigbyte.compareTo(bigbyte)!= 0)
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
* tests the conversion Byte to and from String using toString() and decode() functions
*/
	public void testStringConversion()
	{
		harness.checkPoint("toString(byte)java.lang.String");
		byte abyte = (byte)'a';
		byte zerobyte = 0;
		byte negbyte = (byte)-97;
		Byte a = new Byte(abyte);
		Byte zero = new Byte((byte)0);
		Byte negative = new Byte(negbyte);
		
		String astring = "97";
		String negstring = "-97";
		
		harness.check(zero.toString(), "0");
		harness.check(Byte.toString(zerobyte), "0");
		harness.check(a.toString(), astring);
		harness.check(a.toString(), Byte.toString(abyte));
		harness.check(negative.toString(), "-97");
		harness.check(negative.toString(), Byte.toString(negbyte) );
		
		harness.checkPoint("decode(java.lang.String)java.lang.Byte");
		decodeMustPass(   "11", 11 );
		decodeMustPass(  "011",  9);
		decodeMustPass(  "#11", 17);
		decodeMustPass( "0x11", 17);
		decodeMustPass( "0x1F", 0x1f);
		decodeMustPass( "0x1f", 0x1F);
		
		/** NOTE: for some reason, the Java SDK seems to demand 0-7 for -07, #-f for -#f and 0x-f for -0xf. allthough the java compiler protests
		when trying to compile a string 0x-f...  */
		decodeMustPass(  "-11",-11);
		decodeMustPass( "-011", -9);
		decodeMustFail( "0-11");  //f
		decodeMustPass( "-#11",-17);
		decodeMustFail( "#-11");     //f
		decodeMustPass("-0x11",-17);
		decodeMustFail("0x-11");     //ff
		
		decodeMustPass( "0x7f", (int)Byte.MAX_VALUE);
		decodeMustPass( "0x7F", (int)Byte.MAX_VALUE);
		
		decodeMustFail("  11");
		decodeMustFail( "128");
		decodeMustFail("-129");
		decodeMustFail( "019");
		decodeMustFail("122.5");
		decodeMustFail(  "4F");
		decodeMustFail( "#4G"); // f
		decodeMustFail("0x4G");  //f
		decodeMustFail(" ");
		decodeMustFail("");      // f
		decodeMustFail(null);
 		
	}
	
	private void decodeMustPass(String line, int checkvalue)
	{
	  try
	  {
	    Byte decoded = Byte.decode(line);
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
	    Byte.decode(line);
	    harness.fail("Attempt to decode illegal string format <" + line + "> got: "+Byte.decode(line));
	  }
	  catch(Exception e)
	  {
	    harness.check(true);
	  }
	}
	
/**
* tests the conversion from String to Byte class/byte primitive with different radix
*/
	public void testStringValueParsing()
	{	
		String astring = new String("100");;
		Byte target = new Byte((byte)100);
		
		harness.checkPoint("valueOf(java.lang.String)java.lang.Byte");
		harness.check(Byte.parseByte(astring), 100);
		harness.check( (Byte.valueOf(astring)).equals(target) );
		harness.check(Byte.parseByte("-34"), -34);
		harness.check( (Byte.valueOf("-56")).equals(new Byte((byte)-56)) );
		harness.check(Byte.parseByte("127"), 127);
		harness.check( (Byte.valueOf("127")).equals(new Byte(Byte.MAX_VALUE)) );
		harness.check(Byte.parseByte("-128"), -128);
		harness.check( (Byte.valueOf("-128")).equals(new Byte(Byte.MIN_VALUE)) );
		
		harness.checkPoint("parseByte(java.lang.String)byte");
 	  parseCheckMustFail(" 87  ", 10);
 	  valueCheckMustFail("  87",  10);
 	  parseCheckMustFail("128", 10);
 	  valueCheckMustFail("128", 10);		
 	  parseCheckMustFail("0x60", 10);
 	  valueCheckMustFail("0x60", 10);
		
	
		//harness.checkPoint("byte parseByte(String, radix)  / Byte valueOf(String, radix)");
		harness.check(Byte.parseByte( "12", 10), Byte.parseByte( "12") );
		harness.check(Byte.parseByte("-34", 10), Byte.parseByte("-34") );
		harness.check( (Byte.valueOf( "56", 10)),  Byte.valueOf( "56") );
		harness.check( (Byte.valueOf("-78", 10)),  Byte.valueOf("-78") );
		
		harness.check(Byte.parseByte( "11", 2), Byte.parseByte( "3"), "parseByte binary " );
		harness.check(Byte.parseByte("-11", 2), Byte.parseByte("-3"), "parseByte binary negative" );
		harness.check(  Byte.valueOf( "11", 2),   Byte.valueOf( "3"),   "valueOf binary " );
		harness.check(  Byte.valueOf("-11", 2),   Byte.valueOf("-3"),   "valueOf binary negative" );

		harness.check(Byte.parseByte( "11", 3), Byte.parseByte( "4"), "parseByte 3-based " );
		harness.check(Byte.parseByte("-11", 3), Byte.parseByte("-4"), "parseByte 3-based negative" );
		harness.check(  Byte.valueOf( "11", 3),   Byte.valueOf( "4"),   "valueOf 3-based " );
		harness.check(  Byte.valueOf("-11", 3),   Byte.valueOf("-4"),   "valueOf 3-based negative" );

		harness.check(Byte.parseByte( "11", 8), Byte.parseByte( "9"), "parseByte octal " );
		harness.check(Byte.parseByte("-11", 8), Byte.parseByte("-9"), "parseByte octal negative" );
		harness.check(  Byte.valueOf( "11", 8),   Byte.valueOf( "9"),   "valueOf octal " );
		harness.check(  Byte.valueOf("-11", 8),   Byte.valueOf("-9"),   "valueOf octal negative" );
		
		harness.check(Byte.parseByte( "11", 16), Byte.parseByte( "17"), "parseByte hex " );
		harness.check(Byte.parseByte("-11", 16), Byte.parseByte("-17"), "parseByte hex negative" );
		harness.check(  Byte.valueOf( "11", 16),   Byte.valueOf( "17"),   "valueOf hex " );
		harness.check(  Byte.valueOf("-11", 16),   Byte.valueOf("-17"),   "valueOf hex negative" );
		harness.check(Byte.parseByte( "f" , 16), Byte.parseByte( "15"), "parseByte hex " );
		harness.check(Byte.parseByte("-f" , 16), Byte.parseByte("-15"), "parseByte hex negative" );
		harness.check(Byte.parseByte( "F" , 16), Byte.parseByte( "15"), "parseByte hex capital" );
		harness.check(  Byte.valueOf( "f" , 16),   Byte.valueOf( "15"),   "valueOf hex " );
		harness.check(  Byte.valueOf("-f" , 16),   Byte.valueOf("-15"),   "valueOf hex negative" );
		harness.check(  Byte.valueOf( "F" , 16),   Byte.valueOf( "15"),   "valueOf hex capital" );
		
		harness.check(Byte.parseByte( "11", 25), Byte.parseByte( "26"), "parseByte 25-based " );
		harness.check(Byte.parseByte("-11", 25), Byte.parseByte("-26"), "parseByte 25-based negative" );
		harness.check(  Byte.valueOf( "11", 25),   Byte.valueOf( "26"),   "valueOf 25-based " );
		harness.check(  Byte.valueOf("-11", 25),   Byte.valueOf("-26"),   "valueOf 25-based negative" );
		harness.check(Byte.parseByte( "o" , 25), Byte.parseByte( "24"), "parseByte 25-based " );
		harness.check(Byte.parseByte( "O" , 25), Byte.parseByte( "24"), "parseByte 25-based capital" );
		harness.check(Byte.parseByte("-o" , 25), Byte.parseByte("-24"), "parseByte 25-based negative" );
		harness.check(  Byte.valueOf( "o" , 25),   Byte.valueOf( "24"),   "valueOf 25-based " );
		harness.check(  Byte.valueOf( "O" , 25),   Byte.valueOf( "24"),   "valueOf 25-based capital" );
		harness.check(  Byte.valueOf("-o" , 25),   Byte.valueOf("-24"),   "valueOf 25-based negative" );
		
		harness.check(Byte.parseByte( "11", 36), Byte.parseByte( "37"), "parseByte 36-based " );
		harness.check(Byte.parseByte("-11", 36), Byte.parseByte("-37"), "parseByte 36-based negative" );
		harness.check(  Byte.valueOf( "11", 36),   Byte.valueOf( "37"),   "valueOf 36-based " );
		harness.check(  Byte.valueOf("-11", 36),   Byte.valueOf("-37"),   "valueOf 36-based negative" );
		harness.check(Byte.parseByte( "z" , 36), Byte.parseByte( "35"), "parseByte 36-based " );
		harness.check(Byte.parseByte( "Z" , 36), Byte.parseByte( "35"), "parseByte 36-based capital" );
		harness.check(Byte.parseByte("-z" , 36), Byte.parseByte("-35"), "parseByte 36-based negative" );
		harness.check(  Byte.valueOf( "z" , 36),   Byte.valueOf( "35"),   "valueOf 36-based " );
		harness.check(  Byte.valueOf( "Z" , 36),   Byte.valueOf( "35"),   "valueOf 36-based capital" );
		harness.check(  Byte.valueOf("-z" , 36),   Byte.valueOf("-35"),   "valueOf 36-based negative" );
		
		
		//harness.checkPoint("parseByte(String, int)/ valueOf(String, int) : exceptions");
		
		//harness.checkPoint("parseByte(String, int)/ valueOf(String, int) : radix exceptions");
		  parseCheckMustFail("11",1);
		  valueCheckMustFail("11", 1);
		  parseCheckMustFail("11",37);
		  valueCheckMustFail("11", 37);
		
		//harness.checkPoint("parseByte(String, int)/ valueOf(String, int) : out-of-bound exceptions");
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
		
		//harness.checkPoint("parseByte(String, int)/ valueOf(String, int) : byte boundaries exceptions");
		  parseCheckMustFail("10000000",2);
		  valueCheckMustFail("10000000", 2);
		  parseCheckMustFail("200",8);
		  valueCheckMustFail("200", 8);
		  parseCheckMustFail("80",16);
		  valueCheckMustFail("80", 16);
		  parseCheckMustFail("40",32);
		  valueCheckMustFail("40", 32);
		
		  parseCheckMustFail("-10000001",2);
		  valueCheckMustFail("-10000001", 2);
		  parseCheckMustFail("-201",8);
		  valueCheckMustFail("-201", 8);
		  parseCheckMustFail("-81",16);
		  valueCheckMustFail("-81", 16);
		  parseCheckMustFail("-41",32);
		  valueCheckMustFail("-41", 32);
	
	}

  private void parseCheckMustFail(String line, int radix)
  {
		try
		{
		  Byte.parseByte(line, radix);
		  harness.fail("Attempt to parse illegal byte string <" + line + ">");
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
		  Byte.valueOf(line, radix);
		  harness.fail("Attempt to get value from illegal byte string <" + line + ">");
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
		Byte zero  = new Byte((byte) 0 );
		Byte abyte = new Byte((byte)'a');
		Byte min   = new Byte(Byte.MIN_VALUE);
		Byte max   = new Byte(Byte.MAX_VALUE);
		
		
		// as the check comparisons by itself convert to integer, float or string, it is better to do a direct (value == expected)??
		// instead of Boolean check(int target, int expected)		
		harness.checkPoint("Value conversins");
		harness.check( zero.byteValue() == 0);
		harness.check(abyte.byteValue() == (byte)'a');
		harness.check(max.byteValue() == 127);
		harness.check(min.byteValue() ==-128);
		
		//harness.checkPoint("Value conversins: Byte => shortValue");
		harness.check( zero.shortValue() == 0);
		harness.check(abyte.shortValue() == (short)'a');
		harness.check(abyte.shortValue() == (short)(abyte.byteValue()) );		
		harness.check(max.shortValue() == 127);
		harness.check(min.shortValue() ==-128);
		
		//harness.checkPoint("Value conversins: Byte => intValue");
		harness.check( zero.intValue() == 0);
		harness.check(abyte.intValue() == (int)'a');
		harness.check(abyte.intValue() == (int)(abyte.byteValue()) );		
		harness.check(max.intValue() == 127);
		harness.check(min.intValue() ==-128);
		
		//harness.checkPoint("Value conversins: Byte => longValue");
		harness.check( zero.longValue() == 0l);
		harness.check(abyte.longValue() == (long)'a');
		harness.check(abyte.longValue() == (long)(abyte.byteValue()) );		
		harness.check(max.longValue() == 127l);
		harness.check(min.longValue() ==-128l);
		
		//harness.checkPoint("Value conversins: Byte => floatValue");
		harness.check( zero.floatValue() == 0.0f);
		harness.check(abyte.floatValue() == (float)'a');
		harness.check(abyte.floatValue() == (float)(abyte.byteValue()) );		
		harness.check(max.floatValue() == 127.0f);
		harness.check(min.floatValue() ==-128.0f);
		
		//harness.checkPoint("Value conversins: Byte => doubleValue");
		harness.check( zero.doubleValue() == 0.0);
		harness.check(abyte.doubleValue() == (double)'a');
		harness.check(abyte.doubleValue() == (double)(abyte.byteValue()) );		
		harness.check(max.doubleValue() == 127.0);
		harness.check(min.doubleValue() ==-128.0);
	}

/*
* tests the properties put() method
* no system properties defined for Byte class
  void testProperties()
  {
      // Augment the System properties with the following.
      // Overwriting is bad because println needs the
      // platform-dependent line.separator property.
      harness.checkPoint("Properties.put()");
      Properties p = System.getProperties();
      p.put("abyte", "97");
      p.put("zero" , "0");
      p put("newa" , "97");

      harness.check (Byte.getByte("abyte") ==(byte)'a');
      harness.check (Byte.getByte("zero")  == (byte)0);
      harness.check (Byte.getByte("abyte") == Byte.getByte("newa") );
      harness.check (Byte.getByte("abyte") != Byte.getByte("zero") );
  }

/**
* tests the Boolean object overwrites hashCode()
*/
  public void testHashCode()
  {
    Byte a = new Byte((byte)'a');
    Byte b = new Byte((byte)'b');
    Byte zero = new Byte((byte) 0);
    Byte newa = new Byte((byte)'a');

    harness.checkPoint("hashCode()int");
    harness.check (a.hashCode(), newa.hashCode());
    harness.check (a.hashCode(), (int)'a');
    harness.check (zero.hashCode(),(int)0);
    harness.check (a.hashCode() != b.hashCode());
  }

/**
* tests the Boolean object overwrites getClass()
*/
  public void testGetClass()
  {
    Byte a = new Byte((byte)'a');
    Byte b = new Byte((byte) 0);
    Integer i = new Integer(0);


    harness.checkPoint("TYPE(public)java.lang.Class");

    try
    {
      harness.check (a instanceof Byte );
      harness.check (b instanceof Byte );
      harness.check (a.getClass().getName(), "java.lang.Byte");
      harness.check (b.getClass().getName(), "java.lang.Byte");
      harness.check (a.getClass(), Class.forName("java.lang.Byte") );
      harness.check (b.getClass(), Class.forName("java.lang.Byte") );
      harness.check (i.getClass() != Class.forName("java.lang.Byte") );
      harness.check (a.getClass(), b.getClass());
      harness.check (a.getClass() != i.getClass());
      harness.check ((Byte.TYPE).getName(), "byte");
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
		harness.setclass("java.lang.Byte");
		testConstructors();
	//	testCompare();
		testStringConversion();
		testStringValueParsing();
		testValueConversion();
//	 	testProperties(); // not defined
	 	testHashCode();
	 	testGetClass();
	}

}
