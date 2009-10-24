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

package gnu.testlet.wonka.lang.StringBuffer;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class StringBufferTest implements Testlet
{
  protected static TestHarness harness;
	public void test_Basics()
	{

	     harness.checkPoint("StringBuffer()");

		
		StringBuffer str1 = new StringBuffer();
		harness.check(!( str1.length() != 0 || str1.capacity() != 16 ),  
			"Error : test_Basics failed - 1");
		harness.check(!( !str1.toString().equals("")), 
			"Error : test_Basics failed - 2");
	     harness.checkPoint("StringBuffer(int)");
		try 	{
			StringBuffer str0 = new StringBuffer(-10);
			harness.fail("Error : test_Basics failed - 3");
			}
		catch ( NegativeArraySizeException e ){harness.check(true);}

		StringBuffer str3 = new StringBuffer(10);
		harness.check(str3.capacity() == 10 & str3.length() == 0
			 ,"Error : test_Basics failed - 4");


	     harness.checkPoint("StringBuffer(java.lang.String)");
		StringBuffer str2 = new StringBuffer( "testing" );
		harness.check(!( str2.length() != 7 ),  
			"Error : test_Basics failed - 5");
		harness.check(!( !str2.toString().equals("testing")), 
			"Error : test_Basics failed - 6");

		
//		try {
//			String str = null;
//			StringBuffer str9 = new StringBuffer(str);
			/*
			  CYGNUS: nowhere does it say that we should
			  handle a null argument in StringBuffer(str).
			  In fact, the JCL implies that we should not.
			  But this leads to an asymmetry: `null + ""'
			  will fail, while `"" + null' will work.  For
			  thaht reason, this test is commented out
			  here: it's not a failure.
			*/
//			harness.fail("Error : test_Basics failed - 7");
//		}
//		catch ( NullPointerException e ){harness.check(true);}	

		StringBuffer str4 =  new StringBuffer("hi there");
		harness.check(!( str4.length () != 8 || str4.capacity () != 24 ),  
			"Error : test_Basics failed - 8");
		harness.check(!( !str4.toString().equals("hi there")), 
			"Error : test_Basics failed - 9");

		StringBuffer strbuf = new StringBuffer(0);
		harness.check(!( ! strbuf.append("hiii").toString().equals("hiii")), 
			"Error : test_Basics failed - 10");

		strbuf = new StringBuffer(10);
		harness.check(!( strbuf.capacity() != 10 ), 
			"Error : test_Basics failed - 11");


	}

	public void test_toString()
	{

	     harness.checkPoint("toString()java.lang.String");
		StringBuffer str1 = new StringBuffer("218943289");

		harness.check(!( !str1.toString().equals("218943289")), 
			"Error : test_toString failed - 1");

		str1 = new StringBuffer();
		harness.check(!( !str1.toString().equals("")), 
			"Error : test_toString failed - 2");
	}

	public void test_length()
	{
	     harness.checkPoint("length()int");
		StringBuffer buf1 = new StringBuffer("");
		StringBuffer buf2 = new StringBuffer("pentium");

		harness.check(!( buf1.length() != 0 ),  
			"Error : test_length failed - 1");
		
		harness.check(!( buf2.length() != 7 ),  
			"Error : test_length failed - 2");
	}

	public void test_capacity()
	{
	     harness.checkPoint("capacity()int");
		StringBuffer buf1 = new StringBuffer("");
		StringBuffer buf2 = new StringBuffer("pentiumpentiumpentium");
		
		harness.check(!( buf1.capacity() != 16 ),  
			"Error : test_capacity failed - 1");

		int cap = buf2.capacity();
		harness.check(!( cap != 37 ), 
			"Error : test_capacity failed - 2");


		buf1.ensureCapacity( 17);
		
		// CYGNUS: This is a test for JDK 1.2 conformance
		harness.check(!( buf1.capacity() != 34 ), 
			"Error : test_capacity failed - 3");
	}

	public void test_setLength()
	{
	     harness.checkPoint("setLength(int)void");
		StringBuffer strbuf = new StringBuffer("ba");

		try {
			strbuf.setLength( -10);
			harness.fail("Error : test_setLength failed - 1");
		}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		strbuf.setLength( 4 );
		harness.check(!(strbuf.length() != 4 ), 
			"Error : test_setLength failed - 2");

		harness.check(!( strbuf.charAt(0 ) != 'b' || strbuf.charAt(1 ) != 'a' ||
			  strbuf.charAt(2 ) != '\u0000' || strbuf.charAt(3 ) != '\u0000' ), 
			"Error : test_setLength failed - 3");
	
// adding code
		StringBuffer sb = new StringBuffer("abcdefghijklm");
		sb.setLength( 5 );
		harness.check(sb.toString() ,"abcde", "test: truncating" );
	}

	public void test_charAt()
	{

	     harness.checkPoint("charAt(int)char");
		harness.check(!( (new StringBuffer("abcd")).charAt(0) != 'a' || 
			 (new StringBuffer("abcd")).charAt(1) != 'b' ||
			 (new StringBuffer("abcd")).charAt(2) != 'c' || 
			 (new StringBuffer("abcd")).charAt(3) != 'd'	), 
			
			"Error : test_charAt failed - 1");

		try 	{
			char ch = (new StringBuffer("abcd")).charAt(4);
			harness.fail("Error : test_charAt failed - 2");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		try 	{
			char ch = (new StringBuffer("abcd")).charAt(-1);
			harness.fail("Error : test_charAt failed - 3");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
	}

	public void test_getChars()
	{

	     harness.checkPoint("getChars(int,int,char[],int)void");
		StringBuffer str = new StringBuffer("abcdefghijklmn");

		try 	{
			str.getChars(0 , 3 , null , 1 );
			harness.fail("Error : test_getChars failed - 1");
			}
		catch ( NullPointerException e ){harness.check(true);}

		char dst[] = new char[5];
		
		try 	{
			str.getChars(-1 , 3 , dst , 1 );
			harness.fail("Error : test_getChars failed - 2");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		try {
			str.getChars(4 , 3 , dst , 3 );
			// CYGNUS: This is a test for JDK 1.2 conformance
			harness.fail("Error : test_getChars failed - 3");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		try 	{
			str.getChars(1 , 15 , dst , 1 );
			harness.fail("Error : test_getChars failed - 4a");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
		try 	{
			str.getChars(15 , 5 , dst , 1 );
			harness.fail("Error : test_getChars failed - 4b");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		try 	{
			str.getChars(1 , 5 , dst , -1 );
			harness.fail("Error : test_getChars failed - 5");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		try 	{
			str.getChars(1 , 10 , dst , 1 );
			harness.fail("Error : test_getChars failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		str.getChars(0,5,dst, 0 );
		harness.check(!( dst[0] != 'a' || dst[1] != 'b' || dst[2] != 'c' ||
			 				  dst[3] != 'd' || dst[4] != 'e' ), 
			"Error : test_getChars failed - 7");

		dst[0] = dst[1] = dst[2] = dst[3] = dst[4] = ' ';
		str.getChars(0,0,dst, 0 );
		harness.check(!( dst[0] != ' ' || dst[1] != ' ' || dst[2] != ' ' ||
			 				  dst[3] != ' ' || dst[4] != ' ' ), 
			"Error : test_getChars failed - 9");

		dst[0] = dst[1] = dst[2] = dst[3] = dst[4] = ' ';
		str.getChars(0,1,dst, 0 );
		harness.check(!( dst[0] != 'a' || dst[1] != ' ' || dst[2] != ' ' ||
			 				  dst[3] != ' ' || dst[4] != ' ' ), 
			"Error : test_getChars failed - 10");
	}

	public void test_append( )
	{

	     harness.checkPoint("append(java.lang.Object)java.lang.StringBuffer");
		StringBuffer str = new StringBuffer();
		Object nullobj = null;
		harness.check(!( !str.append( nullobj ).toString().equals("null")), 
			"Error : test_append failed - 1");

		harness.check(!( !str.append( new Integer(100) ).toString().equals("null100")), 
			"Error : test_append failed - 2");
		Object obj = new Object();
		StringBuffer strh = new StringBuffer();
		harness.check(strh.append(obj).toString().equals(String.valueOf(obj)));

	     harness.checkPoint("append(java.lang.String)java.lang.StringBuffer");
		StringBuffer str1 = new StringBuffer("hi");
		str1 = str1.append( " there" );
		str1 = str1.append( " buddy");

		harness.check(!( !str1.toString().equals("hi there buddy")), 
			"Error : test_append failed - 2");

		StringBuffer str2 = new StringBuffer();
		str2 = str2.append("sdljfksdjfklsdjflksdjflkjsdlkfjlsdkjflksdjfklsd");
		harness.check(!( !str2.toString().equals("sdljfksdjfklsdjflksdjflkjsdlkfjlsdkjflksdjfklsd")), 
			"Error : test_app9end failed - 3");
		harness.check(str2.length() == "sdljfksdjfklsdjflksdjflkjsdlkfjlsdkjflksdjfklsd".length());

		String s = null;
		strh = new StringBuffer("help");
		harness.check(strh.append(s).toString().equals("helpnull"));


	     harness.checkPoint("append(char[])java.lang.StringBuffer");
		char carr[] = null;
		StringBuffer str3 = new StringBuffer();

		try 	{
			str3 = str3.append( carr );
			harness.fail("Error : test_append failed - 4");
			}
		catch ( NullPointerException e ){harness.check(true);}

		char carr1[] = {'h','i','t','h','e','r'};
		StringBuffer  str4 = new StringBuffer("!");
		harness.check(!( !str4.append( carr1 ).toString().equals("!hither")), 
			"Error : test_append failed - 5");

	     harness.checkPoint("append(char[],int,int)java.lang.StringBuffer");
		try 	{
			str3 = str3.append( carr , 0 , 3);
			harness.fail("Error : test_append failed - 6");
			}
		catch ( NullPointerException e ){harness.check(true);}
		str3 = new StringBuffer();
		try 	{
			str3 = str3.append( carr1 , -1 , 3);
			harness.fail("Error : test_append failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
		try 	{
			str3 = str3.append ( carr1 , 0 , -3);
			harness.fail("Error : test_append failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
		try 	{
			str3 = str3.append( carr1 , 1 , 17);
			harness.fail("Error : test_append failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
		try 	{
			str3 = str3.append ( carr1 ,10 , 3);
			harness.fail("Error : test_append failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
		try 	{
			str3 = str3.append( carr1 , 4 , 4);
			harness.fail("Error : test_append failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		StringBuffer str5 = new StringBuffer("!");
		str5 = str5.append(carr1 , 2 , 3 );
		harness.check(!( !str5.toString().equals("!the")), 
			"Error : test_append failed - 7");


	     harness.checkPoint("append(boolean)java.lang.StringBuffer");
		str5 = new StringBuffer();
		str5 = str5.append( true );
		harness.check(!( !str5.toString().equals("true")), 
			"Error : test_append failed - 8");

		str5 = str5.append( false );
		harness.check(!( !str5.toString().equals("truefalse")), 
			"Error : test_append failed - 9");

	     harness.checkPoint("append(int)java.lang.StringBuffer");
		str5 = str5.append( 20);
		harness.check(!( !str5.toString().equals("truefalse20")), 
			"Error : test_append failed - 10");

	     harness.checkPoint("append(long)java.lang.StringBuffer");
		str5 = new StringBuffer();
		str5 = str5.append( 2034L );
		harness.check(!( !str5.toString().equals("2034")), 
			"Error : test_append failed - 11");

	     harness.checkPoint("append(float)java.lang.StringBuffer");
		str5 = new StringBuffer();
		str5 = str5.append( 34.45f );
		harness.check(!( !str5.toString().equals("34.45")), 
			"Error : test_append failed - 12");

	     harness.checkPoint("append(double)java.lang.StringBuffer");
		str5 = new StringBuffer();
		str5 = str5.append( 34.46 );
		harness.check(!( !str5.toString().equals("34.46")), 
			"Error : test_append failed - 13");
	}

	public void test_insert()
	{


	     harness.checkPoint("insert(int,java.lang.Object)java.lang.StringBuffer");
		StringBuffer buf = new StringBuffer("1234567");
		Object obj = null;
		buf = buf.insert(5 , obj);
		harness.check(!( !buf.toString().equals("12345null67")), 
			"Error : test_insert failed - 1");

		try 	{
			buf = buf.insert(-1 , new Object());
			harness.fail("Error : test_insert failed - 2");		
			}
		catch ( StringIndexOutOfBoundsException e ){harness.check(true);}

		buf = new StringBuffer("1234567");
		try 	{
			buf = buf.insert(8 , new Object() );
			harness.fail("Error : test_insert failed - 3");
			}
		catch ( StringIndexOutOfBoundsException e ){harness.check(true);}
		
	     harness.checkPoint("insert(int,java.lang.String)java.lang.StringBuffer");
		buf = new StringBuffer("1234567");
		buf = buf.insert(4 , "inserted" );
		harness.check(!( !buf.toString().equals("1234inserted567")), 
			"Error : test_insert failed - 4");
		String s = null;		
		buf = new StringBuffer("1234567");
		buf = buf.insert(5 , s);
		harness.check(!( !buf.toString().equals("12345null67")), 
			"Error : test_insert failed - 1");

		try 	{
			buf = buf.insert(-1 , new String("21"));
			harness.fail("Error : test_insert failed - 2");		
			}
		catch ( StringIndexOutOfBoundsException e ){harness.check(true);}

		buf = new StringBuffer("1234567");
		try 	{
			buf = buf.insert(8 , new String("21") );
			harness.fail("Error : test_insert failed - 3");
			}
		catch ( StringIndexOutOfBoundsException e ){harness.check(true);}

	     harness.checkPoint("insert(int,char[])java.lang.StringBuffer");
		buf = new StringBuffer("1234567");
		char cdata[] = null;
		try 	{
			buf = buf.insert(4 , cdata );
			harness.fail("Error : test_insert failed - 5");
			}
		catch ( NullPointerException e ) {harness.check(true);}

		cdata = new char[1];
		cdata[0] = '1';
		try 	{
			buf = buf.insert(-1 , cdata );
			harness.fail("Error : test_insert failed - 6");
			}
		catch ( StringIndexOutOfBoundsException e ){harness.check(true);}

		try 	{
			buf = buf.insert(8 , cdata );
			harness.fail("Error : test_insert failed - 7");
			}
		catch ( StringIndexOutOfBoundsException e ){harness.check(true);}

		buf = new StringBuffer("1234567");
		char cdata1[] = {'h','e','l','l','o'};
		buf = buf.insert(4 , cdata1 );
		harness.check(!( !buf.toString().equals("1234hello567")), 
			"Error : test_insert failed - 8");

	     harness.checkPoint("insert(int,char[],int,int)java.lang.StringBuffer");
		buf = new StringBuffer("1234567");
		cdata = null;
		try 	{
			buf = buf.insert(4 , cdata, 1, 3 );
			harness.fail("Error : test_insert failed - 5");
			}
		catch ( NullPointerException e ) {harness.check(true);}

		cdata = new char[4];
		cdata[0] = cdata[1] = cdata[2] = cdata[3] = '1';
		try 	{
			buf = buf.insert(-1 , cdata, 3, 0 );
			harness.fail("Error : test_insert failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		try 	{
			buf = buf.insert(8 , cdata, 3, 1 );
			harness.fail("Error : test_insert failed - 7");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
		try 	{
			buf = buf.insert(1 , cdata,-3, 0 );
			harness.fail("Error : test_insert failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		try 	{
			buf = buf.insert(2 , cdata, 6, 1 );
			harness.fail("Error : test_insert failed - 7");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
		try 	{
			buf = buf.insert(1 , cdata,3, 10 );
			harness.fail("Error : test_insert failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
		try 	{
			buf = buf.insert(1 , cdata,3, 3 );
			harness.fail("Error : test_insert failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
		try 	{
			buf = buf.insert(2 , cdata, 6, -1 );
			harness.fail("Error : test_insert failed - 7");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
		buf = new StringBuffer("1234567");
		cdata1 = new char[] {'h','e','l','l','o'};
		buf = buf.insert(4 , cdata1 );
		harness.check(!( !buf.toString().equals("1234hello567")), 
			"Error : test_insert failed - 8");


	     harness.checkPoint("insert(int,boolean)java.lang.StringBuffer");
		buf = new StringBuffer("1234567");
		buf = buf.insert(0 , true );
		harness.check(!( !buf.toString().equals("true1234567")), 
			"Error : test_insert failed - 9");

		buf = new StringBuffer("1234567");
		buf = buf.insert(7 , false );
		harness.check(!( !buf.toString().equals("1234567false")), 
			"Error : test_insert failed - 10");
		buf = new StringBuffer("1234567");
		try 	{
			buf = buf.insert(-1 , true );
			harness.fail("Error : test_insert failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		try 	{
			buf = buf.insert(8 , false );
			harness.fail("Error : test_insert failed - 7");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}


	     harness.checkPoint("insert(int,char)java.lang.StringBuffer");
		buf = new StringBuffer("1234567");
		buf = buf.insert(0 , 'c' );
		harness.check(!( !buf.toString().equals("c1234567")), 
			"Error : test_insert failed - 11");

		buf = new StringBuffer("1234567");
		buf = buf.insert(7 , 'b' );
		harness.check(!( !buf.toString().equals("1234567b")), 
			"Error : test_insert failed - 12");
			
		buf = new StringBuffer("1234567");
		try 	{
			buf = buf.insert(-1 , true );
			harness.fail("Error : test_insert failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		try 	{
			buf = buf.insert(8 , false );
			harness.fail("Error : test_insert failed - 7");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}


	     harness.checkPoint("insert(int,int)java.lang.StringBuffer");
		buf = new StringBuffer("1234567");
		buf = buf.insert(7 , 999 );
		harness.check(!( !buf.toString().equals("1234567999")), 
			"Error : test_insert failed - 13");
		buf = new StringBuffer("1234567");
		try 	{
			buf = buf.insert(-1 , 54 );
			harness.fail("Error : test_insert failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		try 	{
			buf = buf.insert(8 , 3 );
			harness.fail("Error : test_insert failed - 7");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}


	     harness.checkPoint("insert(int,float)java.lang.StringBuffer");
		buf = new StringBuffer("1234567");
		buf = buf.insert(0, 999.0f );
		harness.check(!( !buf.toString().equals("999.01234567")),
			"Error : test_insert failed - 14");
		buf = new StringBuffer("1234567");
		try 	{
			buf = buf.insert(-1 , 34435.34f );
			harness.fail("Error : test_insert failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		try 	{
			buf = buf.insert(8 , 234.6f  );
			harness.fail("Error : test_insert failed - 7");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}


	     harness.checkPoint("insert(int,double)java.lang.StringBuffer");
		buf = new StringBuffer("1234567");
		buf = buf.insert(3, 34.46 );
		harness.check(!( !buf.toString().equals("12334.464567")), 
			"Error : test_insert failed - 15 "
			   + buf.toString());
		buf = new StringBuffer("1234567");
		try 	{
			buf = buf.insert(-1 ,445.7 );
			harness.fail("Error : test_insert failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		try 	{
			buf = buf.insert(8 , 133.3 );
			harness.fail("Error : test_insert failed - 7");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		
	     harness.checkPoint("insert(int,long)java.lang.StringBuffer");
		buf = new StringBuffer("1234567");
		buf = buf.insert(3, (long)1230 );
		harness.check(!( !buf.toString().equals("12312304567")), 
			"Error : test_insert failed - 16 "
			   + buf.toString());
		buf = new StringBuffer("1234567");
		try 	{
			buf = buf.insert(-1 , 1123L );
			harness.fail("Error : test_insert failed - 6");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

		try 	{
			buf = buf.insert(8 , 1234L );
			harness.fail("Error : test_insert failed - 7");
			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
	}

	public void test_reverse()
	{
		StringBuffer buff = new StringBuffer();
		harness.check(!( !buff.reverse().toString().equals("")), 
			"Error : test_reverse failed - 1");

		buff = new StringBuffer("babu");
		harness.check(!( !buff.reverse().toString().equals("ubab")), 
			"Error : test_reverse failed - 2");

		buff = new StringBuffer("malayalam");
		harness.check(!( !buff.reverse().toString().equals("malayalam")), 
			"Error : test_reverse failed - 3");

		buff = new StringBuffer("cnbcbnc");
		harness.check(!( !buff.reverse().toString().equals("cnbcbnc")), 
			"Error : test_reverse failed - 4");

		buff = new StringBuffer("vinod");
		harness.check(!( !buff.reverse().toString().equals("doniv")), 
			"Error : test_reverse failed - 5");
		buff = null;
		try 	{
			buff.reverse();
			harness.fail("should throw NullPointerExcpetion");
			}
		catch	(NullPointerException ne)	{ harness.check(true); }
	}

	public void testall()
	{
		harness.setclass("java.lang.StringBuffer");
		test_Basics();
		test_toString();
		test_length();
		test_capacity();
		test_setLength();
		test_charAt();
		test_getChars();
		test_append();
		test_insert();
		test_reverse();
	}


  public void test (TestHarness the_harness)
  {
    harness = the_harness;
    testall ();
  }

}
