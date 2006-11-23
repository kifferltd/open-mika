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
package gnu.testlet.wonka.lang.String;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import gnu.testlet.UnicodeSubsets;

public class StringTest implements Testlet
{

  protected static TestHarness harness;
	public void test_Basics()
	{

	     harness.checkPoint("String()");
		String str1 = new String();
		harness.check(!( str1.length() != 0 ),  
			"Error : test_Basics failed - 1");
		harness.check(!( !str1.toString().equals("")), 
			"Error : test_Basics failed - 2");

	     harness.checkPoint("String(java.lang.String)");
		String str2 = new String("testing" );
		harness.check(!( str2.length() != 7 ),  
			"Error : test_Basics failed - 3");
		harness.check(!( !str2.toString().equals("testing")), 
			"Error : test_Basics failed - 4");		
		try {
			String str = null;
			String str3 = new String(str);
			harness.fail("Error : test_Basics failed - 5");
		}
		catch ( NullPointerException e ){harness.check(true);}	

	     harness.checkPoint("String(java.lang.StringBuffer)");
		String str4 = new String( new StringBuffer("hi there"));
		harness.check(!( str4.length () != 8 ),  
			"Error : test_Basics failed - 6");
		harness.check(!( !str4.toString().equals("hi there")), 
			"Error : test_Basics failed - 7");
		try {
			StringBuffer strb = null;
			String str3 = new String(strb);
			harness.fail("Error : test_Basics failed - 5");
		}
		catch ( NullPointerException e ){harness.check(true);}	
		
	     harness.checkPoint("String(char[])");
		char cdata[] = { 'h' , 'e' , 'l' , 'l' , 'o' };
		String str5 = new String( cdata );
		harness.check(!( str5.length () != 5 ),  
			"Error : test_Basics failed - 8");
		harness.check(!( !str5.toString().equals("hello")), 
			"Error : test_Basics failed - 9");
		try {
			char[] chra = null;
			String str3 = new String(chra);
			harness.fail("Error : test_Basics failed - 5");
		}
		catch ( NullPointerException e ){harness.check(true);}	


	     harness.checkPoint("String(char[],int,int)");
		try 	{
			String str6 = new String( cdata , 0 , 10 );
			harness.fail("Error : test_Basics failed - 10");

			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
		try 	{
			String str6 = new String( cdata , 0 , -10 );
			harness.fail("Error : test_Basics failed - 10");

			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
		try 	{
			String str6 = new String( cdata , -1 , 4 );
			harness.fail("Error : test_Basics failed - 10");

			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}


		String str8 = new String( cdata , 0 , 4 );
		harness.check(!( !str8.equals("hell")), 
			"Error : test_Basics failed - 12");
		try {
			char[] chra = null;
			String str3 = new String(chra, 0 ,0 );
			harness.fail("Error : test_Basics failed - 5");
		}
		catch ( NullPointerException e ){harness.check(true);}	

	     harness.checkPoint("String(byte[])");
		byte bdata[] = { (byte)'d',(byte)'a',(byte)'n',(byte)'c',(byte)'i',(byte)'n',(byte)'g' };
		String str14 = new String( bdata);
		harness.check(!( !str14.equals("dancing")), 
			"Error : test_Basics failed - 18");
		try {
			byte[] bta = null;
			String str3 = new String(bta);
			harness.fail("Error : test_Basics failed - 5");
		}
		catch ( NullPointerException e ){harness.check(true);}	

	     harness.checkPoint("String(byte[],int,int)");
		try 	{
			byte [] barr = null;
			String str7 = new String( barr , 0 , 10 );
			harness.fail("Error : test_Basics failed - 11");
			}
		catch ( NullPointerException e ){harness.check(true);}
		try 	{
			String str6 = new String( bdata , 0 , 10 );
			harness.fail("Error : test_Basics failed - 10");

			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
		try 	{
			String str6 = new String( bdata , 0 , -10 );
			harness.fail("Error : test_Basics failed - 10");

			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}
		try 	{
			String str6 = new String( bdata , -1 , 4 );
			harness.fail("Error : test_Basics failed - 10");

			}
		catch ( IndexOutOfBoundsException e ){harness.check(true);}

	}

	public void test_toString()
	{

	     harness.checkPoint("toString()java.lang.String");		String str1 = "218943289";

		harness.check(!( !str1.toString().equals("218943289")), 
			"Error : test_toString failed - 1");

		harness.check(!( str1 != "218943289" ), 
			"Error : test_toString failed - 2");

		harness.check(!( !str1.equals(str1.toString())), 
			"Error : test_toString failed - 3");		
		harness.check( str1.toString()=="218943289", 
			"Error : test_toString failed - 4");
	}

	public void test_equals()
	{

	     harness.checkPoint("equals(java.lang.Object)boolean");
		String str2 = new String("Nectar");

		harness.check(!( str2.equals( null )), 
			"Error : test_equals failed - 1");		

		harness.check(!( !str2.equals("Nectar")), 
			"Error : test_equals failed - 2");		

		harness.check(!( str2.equals("")), 
			"Error : test_equals failed - 3");		

		harness.check(!( str2.equals("nectar")), 
			"Error : test_equals failed - 4");		

		harness.check(!( !"".equals("")), 
			"Error : test_equals failed - 5");		
		str2 = "";
		harness.check(!str2.equals(null));

	}

	public void test_hashCode()
	{

	     harness.checkPoint("equals(java.lang.Object)boolean");
		String str1 = "hp";
		String str2 = "Hewlett Packard Company";
		String str3 = "Hewlett Packard Company";
                String str4 = "SmartMove";

                int hashSm= (int) str4.charAt(0);
                for (int i = 1 ; i <9 ; i++) { hashSm = hashSm * 31 + (int)str4.charAt(i); }
                harness.check(str4.hashCode() == hashSm , "testing hashcode algorithm");

		int hash1 = 'h' * 31 + 'p';
		int acthash1 = str1.hashCode(); 

		harness.check(!( hash1 != acthash1 ), 
			"Error : test_hashCode failed - 1");		
		harness.check(str2.hashCode() == str3.hashCode(),
			"equal string should have equal hashcodes");
	}

	public void test_length()
	{
	     harness.checkPoint("length()int");
		harness.check(!( "".length() != 0 ),  
			"Error : test_length failed - 1");
		
		harness.check(!( "pentium".length() != 7 ),  
			"Error : test_length failed - 2");
	}

	public void test_charAt()
	{

	     harness.checkPoint("charAt(int)char");
		harness.check(!( "abcd".charAt(0) != 'a' || "abcd".charAt(1) != 'b' ||
			 "abcd".charAt(2) != 'c' || "abcd".charAt(3) != 'd'	), 
			"Error : test_charAt failed - 1");

		try {
			char ch = "abcd".charAt(4);
			harness.fail("Error : test_charAt failed - 2");
		}
		catch ( IndexOutOfBoundsException e ){}

		try {
			char ch = "abcd".charAt(-1);
			harness.fail("Error : test_charAt failed - 3");
		}
		catch ( IndexOutOfBoundsException e ){}
	}

	public void test_getChars()
	{
	     harness.checkPoint("getChars(int,int,char[],int)void");
		String str = "abcdefghijklmn";

		try {
			str.getChars(0 , 3 , null , 1 );
			harness.fail("Error : test_getChars failed - 1");
		}catch ( NullPointerException e ){}

		char dst[] = new char[5];
		
		try {
			str.getChars(-1 , 3 , dst , 1 );
			harness.fail("Error : test_getChars failed - 2");
		}catch ( IndexOutOfBoundsException e ){}

		try {
			str.getChars(4 , 3 , dst , 1 );
			harness.fail("Error : test_getChars failed - 3");
		}catch ( IndexOutOfBoundsException e ){}

		try {
			str.getChars(1 , 15 , dst , 1 );
			harness.fail("Error : test_getChars failed - 4");
		}catch ( IndexOutOfBoundsException e ){}

		try {
			str.getChars(1 , 5 , dst , -1 );
			harness.fail("Error : test_getChars failed - 5");
		}catch ( IndexOutOfBoundsException e ){}

		try {
			str.getChars(1 , 10 , dst , 1 );
			harness.fail("Error : test_getChars failed - 6");
		}catch ( IndexOutOfBoundsException e ){}

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
		dst[0] = dst[1] = dst[2] = dst[3] = dst[4] = ' ';
		str.getChars(3,7,dst, 0 );
		harness.check(!( dst[0] != 'd' || dst[1] != 'e' || dst[2] != 'f' ||
			 				  dst[3] != 'g' || dst[4] != ' ' ),
			"Error : test_getChars failed - 11");
		str.getChars(10,14,dst, 1 );
		harness.check("dklmn".equals(new String(dst)),"Error : test_getChars failed - 12" );
	}


	public void test_getBytes()
	{

	     harness.checkPoint("getBytes()byte[]");
		String str = "abcdefghijklmn";



		byte [] dst1 = new byte[40];
		dst1 = str.getBytes();
		harness.check(!( dst1[0] != 'a' || dst1[1] != 'b' || dst1[2] != 'c' ||
			 				  dst1[3] != 'd' || dst1[4] != 'e' ), 
			"Error : test_getBytes failed - 8");
	}

	public void test_toCharArray()
	{

	     harness.checkPoint("toCharArray()char[]");
		char[] charr = "abcde".toCharArray();

		harness.check(!( charr[0] != 'a' || charr[1] != 'b' ||
			charr[2] != 'c' || charr[3] != 'd' ||
			charr[4] != 'e' ), 
			"Error : test_toCharArray failed - 1");
		try	{
			charr[5] = 'l';
			harness.fail("An IndexOutOfBoundsException should have been thrown");
			}
		catch 	(IndexOutOfBoundsException e)	{ harness.check(true); }
		char [] charr1 = "".toCharArray();

		harness.check(!( charr1.length  > 0 ), 
			"Error : test_toCharArray failed - 2");
	}

	public void test_equalsIgnoreCase()
	{

	     harness.checkPoint("equalsIgnoreCase(java.lang.String)boolean");
		harness.check(!( "hi".equalsIgnoreCase(null)), 
			"Error : test_equalsIgnoreCase failed - 1");

		harness.check(!( !"hi".equalsIgnoreCase("HI")), 
			"Error : test_equalsIgnoreCase failed - 2");

		harness.check(!( "hi".equalsIgnoreCase("pq")), 
			"Error : test_equalsIgnoreCase failed - 3");

		harness.check(!( "hi".equalsIgnoreCase("HI ")), 
			"Error : test_equalsIgnoreCase failed - 4");

	}

	public void test_compareTo()
	{

	     harness.checkPoint("compareTo(java.lang.String)int");
		try {
			int res = "abc".compareTo(null);
			harness.fail("Error : test_compareTo failed - 1");
		}
		catch ( NullPointerException e ){}

		harness.check(!( "abc".compareTo("bcdef") >= 0  ), 
			"Error : test_compareTo failed - 2");

		harness.check(!( "abc".compareTo("abc") != 0 ), 
			"Error : test_compareTo failed - 3");

		harness.check(!( "abc".compareTo("aabc") <= 0 ), 
			"Error : test_compareTo failed - 4");

		harness.check(!( "abcd".compareTo("abc") <= 0 ), 
			"Error : test_compareTo failed - 5");

		harness.check(!( "".compareTo("abc") >= 0 ), 
			"Error : test_compareTo failed - 6");
		harness.check( "abc".compareTo("efg") == -4  , 
			"Error : test_compareTo failed - 7");
		harness.check( "abcdefgh".compareTo("abc") == 5 , 
			"Error : test_compareTo failed - 8");
		harness.check( "ebc".compareTo("afg") == 4  , 
			"Error : test_compareTo failed - 7");
		harness.check( "abc".compareTo("abcdefgh") == -5 , 
			"Error : test_compareTo failed - 8");

	}

	public void test_regionMatches()
	{

	     harness.checkPoint("regionMatches(int,java.lang.String,int,int)boolean");
		try {
			boolean res = "abc".regionMatches(0 , null , 0 , 2);
			harness.fail("Error : test_regionMatches failed - 1");
		}
		catch ( NullPointerException e ){ harness.check(true); }

		harness.check(!( "abcd".regionMatches(-1 , "abcd" , 0 , 2 )), 
			"Error : test_regionMatches failed - 2");
		harness.check(!( "abcd".regionMatches(0 , "abcd" , - 1 , 2 )), 
			"Error : test_regionMatches failed - 3");
		harness.check(!( "abcd".regionMatches(0 , "abcd" , 0 , 10 )), 
			"Error : test_regionMatches failed - 4");
		harness.check(!( "abcd".regionMatches(0 , "ab" , 0 , 3 )), 
			"Error : test_regionMatches failed - 5");

		harness.check(!( !"abcd".regionMatches(1 , "abc" , 1 , 2 )), 
			"Error : test_regionMatches failed - 6");

		harness.check(!( !"abcd".regionMatches(1 , "abc" , 1 , 0 )), 
			"Error : test_regionMatches failed - 7");

		harness.check(!( "abcd".regionMatches(1 , "ABC" , 1 , 2 )), 
			"Error : test_regionMatches failed - 8");
		

	     harness.checkPoint("regionMatches(boolean,int,java.lang.String,int,int)boolean");

		try {
			boolean res = "abc".regionMatches(true , 0 , null , 0 , 2);
			harness.fail("Error : test_regionMatches failed - 11");
		}
		catch ( NullPointerException e ){}

		harness.check(!( "abcd".regionMatches(true , -1 , "abcd" , 0 , 2 )), 
			"Error : test_regionMatches failed - 12");
		harness.check(!( "abcd".regionMatches(true , 0 , "abcd" , - 1 , 2 )), 
			"Error : test_regionMatches failed - 13");
		harness.check(!( "abcd".regionMatches(true , 0 , "abcd" , 0 , 10 )), 
			"Error : test_regionMatches failed - 14");
		harness.check(!( "abcd".regionMatches(true , 0 , "ab" , 0 , 3 )), 
			"Error : test_regionMatches failed - 15");

		harness.check(!( !"abcd".regionMatches(true , 1 , "abc" , 1 , 2 )), 
			"Error : test_regionMatches failed - 16");

		harness.check(!( !"abcd".regionMatches(true , 1 , "abc" , 1 , 0 )), 
			"Error : test_regionMatches failed - 17");

		harness.check(!( !"abcd".regionMatches(true , 1 , "ABC" , 1 , 2 )), 
			"Error : test_regionMatches failed - 18");
		harness.check(!( "abcd".regionMatches(false , 1 , "ABC" , 1 , 2 )), 
			"Error : test_regionMatches failed - 19");
	}

	public void test_startsWith()
	{

	     harness.checkPoint("startsWith(java.lang.String)boolean");
		harness.check(!( !"abcdef".startsWith( "abc")), 
			"Error : test_startsWith failed - 1");

		try {
			boolean b = "abcdef".startsWith( null );
			harness.fail("Error : test_startsWith failed - 2");
		} catch ( NullPointerException e ){ harness.check(true);}

		harness.check(!( "abcdef".startsWith( "ABC")), 
			"Error : test_startsWith failed - 3");

		harness.check(!( !"abcdef".startsWith( "")), 
			"Error : test_startsWith failed - 4");

		harness.check(!( "abc".startsWith( "abcd")), 
			"Error : test_startsWith failed - 5");

	     harness.checkPoint("startsWith(java.lang.String,int)boolean");

		harness.check(!( !"abcdef".startsWith( "abc" , 0 )), 
			"Error : test_startsWith failed - 6");

		try {
			boolean b = "abcdef".startsWith( null ,0);
			harness.fail("Error : test_startsWith failed - 7");
		} catch ( NullPointerException e ){ harness.check(true);}

		harness.check(!( "abcdef".startsWith( "ABC", 2)), 
			"Error : test_startsWith failed - 8");

		harness.check(!( !"abcdef".startsWith( "", 0 )), 
			"Error : test_startsWith failed - 9");

		harness.check(!( "abc".startsWith( "abcd" , 3)), 
			"Error : test_startsWith failed - 10");

		harness.check(!( "abc".startsWith( "abc" , 10)), 
			"Error : test_startsWith failed - 11");
		harness.check(!( !"defabcdef".startsWith( "abc" , 3 )), 
			"Error : test_startsWith failed - 12");
	}

	public void test_endsWith()
	{

	     harness.checkPoint("endsWith(java.lang.String)boolean");
		harness.check(!( !"abcdef".endsWith( "def")), 
			"Error : test_endsWith failed - 1");

		try {
			boolean b = "abcdef".endsWith( null );
			harness.fail("Error : test_endsWith failed - 2");
		} catch ( NullPointerException e ){ harness.check(true);}

		harness.check(!( "abcdef".endsWith( "DEF")), 
			"Error : test_endsWith failed - 3");

		harness.check(!( !"abcdef".endsWith( "")), 
			"Error : test_endsWith failed - 4");

		harness.check(!( "bcde".endsWith( "abcd")), 
			"Error : test_endsWith failed - 5");
		harness.check( "abcd".endsWith( "abcd"), 
			"Error : test_endsWith failed - 5");

	}

	public void test_indexOf()
	{

	     harness.checkPoint("indexOf(int)int");
		harness.check(!( "a".indexOf('a') != 0 ), 
			"Error : test_indexOf failed - 1");
		harness.check(!( "aabc".indexOf('c') != 3 ), 
			"Error : test_indexOf failed - 2");
		harness.check(!( "a".indexOf('c') != -1 ), 
			"Error : test_indexOf failed - 3");
		harness.check(!( "".indexOf('a') != -1 ), 
			"Error : test_indexOf failed - 4a");
		harness.check(!( "ab\t\nsd".indexOf('\n') != 3 ), 
			"Error : test_indexOf failed - 4b");
		harness.check(!( "ab\t\nsd".indexOf('\t') != 2 ), 
			"Error : test_indexOf failed - 4c");


	     harness.checkPoint("indexOf(int,int)int");
		harness.check(!( "abcde".indexOf('b', 3) != -1 ), 
			"Error : test_indexOf failed - 5");
		harness.check(!( "abcde".indexOf('b', 0) != 1 ), 
			"Error : test_indexOf failed - 6");
		harness.check(!( "abcdee".indexOf('e', 3) != 4 ), 
			"Error : test_indexOf failed - 7");
		harness.check(!( "abcdee".indexOf('e', 5) != 5 ), 
			"Error : test_indexOf failed - 8");
		harness.check(!( "abcdee".indexOf('e', -5) != 4 ), 
			"Error : test_indexOf failed - 9");
		harness.check(!( "abcdee".indexOf('e', 15) != -1 ), 
			"Error : test_indexOf failed - 10");


	     harness.checkPoint("indexOf(java.lang.String)int");
		harness.check(!( "abcdee".indexOf("babu") != -1 ), 
			"Error : test_indexOf failed - 11");
		try {
			int x = "abcdee".indexOf(null);
		   	harness.fail("Error : test_indexOf failed - 12");
		}
		catch ( NullPointerException e ){ harness.check(true);} 
		harness.check(!( "abcdee".indexOf("") != 0 ), 
			"Error : test_indexOf failed - 13");
		harness.check(!( "abcdee".indexOf("ee") != 4 ), 
			"Error : test_indexOf failed - 14");
		harness.check(!( "abcbcbc".indexOf("cbc") != 2 ), 
			"Error : test_indexOf failed - 15");
	// EJWcr00463
		if ( "hello \u5236 world".indexOf('\u5236') != 6 ) {
			harness.fail("Error : test_indexOf failed - 21");
		}
		if ( "hello \u0645 world".indexOf('\u0645') != 6 ) {
			harness.fail("Error : test_indexOf failed - 22");
		}
		if ( "hello \u07ff world".indexOf('\u07ff') != 6 ) {
			harness.fail("Error : test_indexOf failed - 23");
		}


	     harness.checkPoint("indexOf(java.lang.String,int)int");
		harness.check(!( "abcdee".indexOf("babu", 3) != -1 ), 
			"Error : test_indexOf failed - 16");
		try {
			int x = "abcdee".indexOf(null,0);
		   	harness.fail("Error : test_indexOf failed - 17");
		}
		catch ( NullPointerException e ){ harness.check(true);} 
		harness.check(!( "abcdee".indexOf("", 0) != 0 ), 
			"Error : test_indexOf failed - 18");
		harness.check(!( "abcdee".indexOf("ee", 4) != 4 ), 
			"Error : test_indexOf failed - 19");
		harness.check(!( "abcbcbc".indexOf("cbc",4 ) != 4 ), 
			"Error : test_indexOf failed - 20");
	}

	public void test_lastIndexOf()
	{

	     harness.checkPoint("lastIndexOf(int)int");
		harness.check(!( "a".lastIndexOf('a') != 0 ), 
			"Error : test_lastIndexOf failed - 1");

		harness.check(!( "acbc".lastIndexOf('c') != 3 ), 
			"Error : test_lastIndexOf failed - 2");

		harness.check(!( "a".lastIndexOf('c') != -1 ), 
			"Error : test_lastIndexOf failed - 3");

		harness.check(!( "".lastIndexOf('a') != -1 ), 
			"Error : test_lastIndexOf failed - 4");


	     harness.checkPoint("lastIndexOf(int,int)int");
		harness.check(!( "abcde".lastIndexOf('b', 0) != -1 ), 
			"Error : test_lastIndexOf failed - 5");
		harness.check(!( "abcde".lastIndexOf('b', 4) != 1 ), 
			"Error : test_lastIndexOf failed - 6");
		harness.check(!( "abcdee".lastIndexOf('e', 7) != 5 ), 
			"Error : test_lastIndexOf failed - 7");
		harness.check(!( "abcdee".lastIndexOf('e', 4) != 4 ), 
			"Error : test_lastIndexOf failed - 8");

		harness.check(!( "abcdee".lastIndexOf('e', -5) != -1 ), 
			"Error : test_lastIndexOf failed - 9");
		harness.check(!( "abcdee".lastIndexOf('e', 15) != 5 ), 
			"Error : test_lastIndexOf failed - 10");


	     harness.checkPoint("lastIndexOf(java.lang.String)int");
		harness.check(!( "abcdee".lastIndexOf("babu") != -1 ), 
			"Error : test_lastIndexOf failed - 11");
		try {
			int x = "abcdee".lastIndexOf(null);
		   	harness.fail("Error : test_lastIndexOf failed - 12");
		}
		catch ( NullPointerException e ){ harness.check(true);} 
		harness.check(!( "abcdee".lastIndexOf("") != 6 ), 
			"Error : test_lastIndexOf failed - 13");
		harness.check(!( "abcdee".lastIndexOf("ee") != 4 ), 
			"Error : test_lastIndexOf failed - 14a");
		harness.check(!( "abcdeef".lastIndexOf("ee") != 4 ),
			"Error : test_lastIndexOf failed - 14b");
		harness.check(!( "abcbcbc".lastIndexOf("cbc") != 4 ),
			"Error : test_lastIndexOf failed - 15a");
		harness.check(!( "abcbcbcd".lastIndexOf("cbc") != 4 ),
			"Error : test_lastIndexOf failed - 15b");
		harness.check(!( "abcdee".lastIndexOf("babu", 3) != -1 ),
			"Error : test_lastIndexOf failed - 16");

	     harness.checkPoint("lastIndexOf(java.lang.String,int)int");
		try {
			int x = "abcdee".lastIndexOf(null,0);
		   	harness.fail("Error : test_lastIndexOf failed - 17");
		}
		catch ( NullPointerException e ){ harness.check(true);} 
	
		harness.check(!( "abcdee".lastIndexOf("", 0) != 0 ),
			"Error : test_lastIndexOf failed - 18a");
		harness.check(!( "abcdee".lastIndexOf("", 5) != 5 ),
			"Error : test_lastIndexOf failed - 18b");
		harness.check(!( "abcdee".lastIndexOf("", 4) != 4 ),
			"Error : test_lastIndexOf failed - 18c");
		harness.check(!( "abcdee".lastIndexOf("ee", 4) != 4 ),
			"Error : test_lastIndexOf failed - 19a");
		harness.check(!( "abcdeef".lastIndexOf("ee", 4) != 4 ),
			"Error : test_lastIndexOf failed - 19b");
		harness.check(!( "abcbcbc".lastIndexOf("cbc",3 ) != 2 ),
			"Error : test_lastIndexOf failed - 20");
		harness.check(!( "abcdee".lastIndexOf("ee", -5) != -1 ), 
			"Error : test_lastIndexOf failed - 21");
		harness.check(!( "abcdeeg".lastIndexOf("ee", 55) != 4 ),
			"Error : test_lastIndexOf failed - 22");
	}

	public void test_substring()
	{
	     harness.checkPoint("substring(int)java.lang.String");
		harness.check(!( !"unhappy".substring(2).equals("happy")), 
			"Error : test_substring failed - 1");
		harness.check(!( !"Harbison".substring(3).equals("bison")), 
			"Error : test_substring failed - 2");
		harness.check(!( !"emptiness".substring(9).equals("")), 
			"Error : test_substring failed - 3");

		try {
			String str = "hi there".substring(-1);
			harness.fail("Error : test_substring failed - 4");
		}catch( IndexOutOfBoundsException e ){ harness.check(true);}

		try {
			String str = "hi there".substring(10);
			harness.fail("Error : test_substring failed - 5");
		}catch( IndexOutOfBoundsException e ){ harness.check(true);}


	     harness.checkPoint("substring(int,int)java.lang.String");
		harness.check(!( !"hamburger".substring(4,8).equals("urge")), 
			"Error : test_substring failed - 6");
		harness.check(!( !"smiles".substring(1,5).equals("mile")), 
			"Error : test_substring failed - 7");
		harness.check(!( !"emptiness".substring(2,2).equals("")), 
			"Error : test_substring failed - 8");

		try {
			String str = "hi there".substring(-1, 3);
			harness.fail("Error : test_substring failed - 9");
		}catch( IndexOutOfBoundsException e ){ harness.check(true);}

		try {
			String str = "hi there".substring(0, 10);
			harness.fail("Error : test_substring failed - 10");
		}catch( IndexOutOfBoundsException e ){ harness.check(true);}

		try {
			String str = "hi there".substring(7, 6);
			harness.fail("Error : test_substring failed - 11");
		}catch( IndexOutOfBoundsException e ){ harness.check(true);}
		harness.check(!( !"emptiness".substring(9,9).equals("")),
			"Error : test_substring failed - 12");
		try {
			String str = "emptiness".substring(10,10);
			harness.fail("Error : test_substring failed - 13");
		}catch( IndexOutOfBoundsException e ){ harness.check(true);}
		try {
			String str = "emptiness".substring(1110,1110);
			harness.fail("Error : test_substring failed - 14");
		}catch( IndexOutOfBoundsException e ){ harness.check(true);}


	}

	public void test_concat( )
	{

	     harness.checkPoint("concat(java.lang.String)java.lang.String");
		try {
			String str = "help".concat(null);
			harness.fail("Error : test_concat failed - 1");
		}catch ( NullPointerException e){}

		harness.check(!( !"help".concat("me").equals("helpme")), 
			"Error : test_concat failed - 2");

		harness.check(!( ! "to".concat("get").concat("her").equals("together")), 
			"Error : test_concat failed - 3");

		harness.check(!( "hi".concat("") != "hi"), 
			"Error : test_concat failed - 4");

		String str1 = "".concat("there");
		harness.check(!( !str1.equals("there")), 
			"Error : test_concat failed - 5");

	// EJWcr00467
		String str2 = new String();
		try {
		    str2 = str2.concat("hello");
		    if (!str2.equals("hello")) {
			harness.fail("Error : test_concat failed - 7");
		    }
		} catch (Exception e) {
			harness.fail("Error : test_concat failed - 6");
		}
	}


	public void test_replace()
	{

	     harness.checkPoint("replace(char,char)java.lang.String");
		harness.check(!( !"mesquite in your cellar".replace('e' , 'o' ).equals(
			          "mosquito in your collar" )), 
			"Error : test_replace failed - 1");

		harness.check(!( !"the war of baronets".replace('r' , 'y' ).equals(
			          "the way of bayonets" )), 
			"Error : test_replace failed - 2");

		harness.check(!( !"sparring with a purple porpoise".replace('p' , 't' ).equals(
			          "starring with a turtle tortoise" )), 
			"Error : test_replace failed - 3");

		harness.check(!( !"JonL".replace('q' , 'x' ).equals("JonL" )), 
			"Error : test_replace failed - 4");

		harness.check(!( !"ppppppppppppp".replace('p' , 'p' ).equals("ppppppppppppp")), 
			"Error : test_replace failed - 5");

		harness.check(!( !"ppppppppppppp".replace('p' , '1' ).equals("1111111111111")), 
			"Error : test_replace failed - 6");
		harness.check(!( !"hp".replace('c' , 'd' ).equals("hp")), 
			"Error : test_replace failed - 7");
		harness.check(!( !"vmhere".replace('a' , 'd' ).equals("vmhere")), 
			"Error : test_replace failed - 8");
		harness.check(!( !"hp\nnf\t".replace('\n' , 'd').equals("hpdnf\t")), 
			"Error : test_replace failed - 9");
		harness.check(!( !"vmhere".replace('e' , '\t').equals("vmh\tr\t")), 
			"Error : test_replace failed - 10");
		

	}

	public void test_toLowerCase()
	{

	     harness.checkPoint("toLowerCase()java.lang.String");
		harness.check(!( !"".toLowerCase().equals("")), 
			"Error : test_toLowerCase failed - 1");

		harness.check(!( !"French Fries".toLowerCase().equals("french fries")), 
			"Error : test_toLowerCase failed - 2");


		harness.check(!( !"SMALL-VM".toLowerCase().equals("small-vm")), 
			"Error : test_toLowerCase failed - 3");
		if (UnicodeSubsets.isSupported("8")) {
//Greek Letters
	harness.check(!(!"\u0391\u0392\u0393".toLowerCase().equals("\u03B1\u03B2\u03B3")), 
			"Error : test_toLowerCase failed - 4");
		}
	if (UnicodeSubsets.isSupported("10")) {
//Cyrillic letters
	harness.check(!(!"\u0401\u0402".toLowerCase().equals("\u0451\u0452")), 
			"Error : test_toLowerCase failed - 5");
	}
	if (UnicodeSubsets.isSupported("11")) {
//Armenian letters
		harness.check(!(!"\u0531\u0532".toLowerCase().equals("\u0561\u0562")), 
			"Error : test_toLowerCase failed - 6");
	}
//e accent grave + e accent circum
	harness.check(!(!"\u00C8\u00CA".toLowerCase().equals("\u00E8\u00EA")), 
			"Error : test_toLowerCase failed - 7");
	}

	public void test_toUpperCase()
	{

	     harness.checkPoint("toUpperCase()java.lang.String");
		harness.check(!( !"".toUpperCase().equals("")), 
			"Error : test_toUpperCase failed - 1");

		harness.check(!( !"French Fries".toUpperCase().equals("FRENCH FRIES")), 
			"Error : test_toUpperCase failed - 2");


		harness.check(!( !"SMALL-VM".toUpperCase().equals("SMALL-VM")), 
			"Error : test_toUpperCase failed - 3");

		harness.check(!( !"small-jvm".toUpperCase().equals("SMALL-JVM")), 
			"Error : test_toUpperCase failed - 4");
		if (UnicodeSubsets.isSupported("8")) {
//Greek Letters
		harness.check(!(!"\u03B1\u03B2\u03B3".toUpperCase().equals("\u0391\u0392\u0393")), 
			"Error : test_toUpperCase failed - 5");
		}
	if (UnicodeSubsets.isSupported("10")) {
//Cyrillic letters
	harness.check(!(!"\u0451\u0452".toUpperCase().equals("\u0401\u0402")), 
			"Error : test_toUpperCase failed - 6");
	}
	if (UnicodeSubsets.isSupported("11")) {
//Armenian letters
		harness.check(!(!"\u0561\u0562".toUpperCase().equals("\u0531\u0532")), 
			"Error : test_toUpperCase failed - 7");
	}
//e accent grave + e accent circum
		harness.check(!(!"\u00E8\u00EA".toUpperCase().equals("\u00C8\u00CA")), 
			"Error : test_toUpperCase failed - 8");



	}


	public void test_valueOf()
	{

	     harness.checkPoint("valueOf(java.lang.Object)java.lang.String");
		harness.check(!( !String.valueOf((Object)null).equals("null")), 
			"Error : test_valueOf failed - 1");

		Object obj = new Object();
		harness.check(!( !String.valueOf(obj).equals(obj.toString())), 
			"Error : test_valueOf failed - 2");

	     harness.checkPoint("valueOf(char[])java.lang.String");
		try {
			char [] data = null;
			String str = String.valueOf( data );
		}catch ( NullPointerException e ){ harness.check(true);}

		char [] data = { 'h' , 'e' , 'l' , 'l' , 'o' };
		harness.check(!( !String.valueOf( data ).equals("hello")), 
			"Error : test_valueOf failed - 3");


	     harness.checkPoint("valueOf(char[],int,int)java.lang.String");
		try {
			String str = String.valueOf(data , -1 , 4 );
			harness.fail("Error : test_valueOf failed - 4");
		}catch ( IndexOutOfBoundsException e ){ harness.check(true);}

		try {
			String str = String.valueOf(data , 1 , 5 );
			harness.fail("Error : test_valueOf failed - 5");
		}catch ( IndexOutOfBoundsException e ){ harness.check(true);}

		try {
			String str = String.valueOf(data , 1 , -5 );
			harness.fail("Error : test_valueOf failed - 6");
		}catch ( IndexOutOfBoundsException e ){ harness.check(true);}

		try {
			String str = String.valueOf(null , 1 , 3 );
			harness.fail("Error : test_valueOf failed - 7");
		}catch ( NullPointerException e ){ harness.check(true);}

		harness.check(!( !String.valueOf(data , 2 , 2 ).equals("ll")), 
			"Error : test_valueOf failed - 8");


	     harness.checkPoint("valueOf(boolean)java.lang.String");
		harness.check(!( !String.valueOf(true).equals("true")), 
			"Error : test_valueOf failed - 9");

		harness.check(!( !String.valueOf(false).equals("false")), 
			"Error : test_valueOf failed - 10");

	     harness.checkPoint("valueOf(char)java.lang.String");
		harness.check(!( !String.valueOf('c').equals("c")), 
			"Error : test_valueOf failed - 11");

		harness.check(!( !String.valueOf(' ').equals(" ")), 
			"Error : test_valueOf failed - 12");

	     harness.checkPoint("valueOf(int)java.lang.String");
		harness.check(!( !String.valueOf(234).equals("234")), 
			"Error : test_valueOf failed - 13a");
		harness.check(!( !String.valueOf(234).equals(new Integer(234).toString())), 
			"Error : test_valueOf failed - 13b");

	     harness.checkPoint("valueOf(long)java.lang.String");
		harness.check(!( !String.valueOf(234L).equals("234")), 
			"Error : test_valueOf failed - 14a");
		harness.check(!( !String.valueOf(234L).equals(new Long(234L).toString())), 
			"Error : test_valueOf failed - 14b");

	     harness.checkPoint("valueOf(float)java.lang.String");
		harness.check(!( !String.valueOf(23.45f).equals("23.45")), 
			"Error : test_valueOf failed - 15a");
		harness.check(!( !String.valueOf(234.4f).equals(new Float(234.4f).toString())), 
			"Error : test_valueOf failed - 15b");

	     harness.checkPoint("valueOf(double)java.lang.String");
		harness.check("23.5".equals(String.valueOf(23.5)),
			"Error : test_valueOf failed - 16a, got: "+String.valueOf(23.5));
		harness.check(String.valueOf(23.4),"23.4",
			"Error : test_valueOf failed - 16b");
		harness.check(!( !String.valueOf(234.4).equals(new Double(234.4).toString())),
			"Error : test_valueOf failed - 16c");


	     harness.checkPoint("copyValueOf(char[])java.lang.String");
		harness.check(!( !String.copyValueOf( data ).equals("hello")), 
			"Error : test_valueOf failed - 3a");
		try	{
			String.copyValueOf(null);
			harness.fail("test_copyValueOf failed");
			}
		catch	(NullPointerException ne) { harness.check(true); }

	     harness.checkPoint("copyValueOf(char[],int,int)java.lang.String");
		harness.check(!( !String.copyValueOf(data , 2 , 2 ).equals("ll")), 
			"Error : test_valueOf failed - 8a");
		try	{
			String.copyValueOf(data, -1 , 2);
			harness.fail("test_copyValueOf failed");
			}
		catch	(IndexOutOfBoundsException ie) { harness.check(true); }
		try	{
			String.copyValueOf(data, 1 ,-2);
			harness.fail("test_copyValueOf failed");
			}
		catch	(IndexOutOfBoundsException ie) { harness.check(true); }
		try	{
			String.copyValueOf(data, 1 , 20);
			harness.fail("test_copyValueOf failed");
			}
		catch	(IndexOutOfBoundsException ie) { harness.check(true); }
		try	{
			String.copyValueOf(data, 6 , 2);
			harness.fail("test_copyValueOf failed");
			}
		catch	(IndexOutOfBoundsException ie) { harness.check(true); }

	}
        
        public void test_intern()
	{

	     harness.checkPoint("intern()java.lang.String");
 	 	String hp = "hp";
		String nullstr = "";
		harness.check(!( "hp".intern() != hp.intern()), 
			"Error : test_intern failed - 1");
		harness.check(!( "pqr".intern() == hp.intern()), 
			"Error : test_intern failed - 2");
		harness.check(!( "".intern() != nullstr.intern()), 
			"Error : test_intern failed - 3");
		harness.check(!( "".intern() == hp.intern()), 
			"Error : test_intern failed - 4");
		hp = "";
		harness.check(!( "".intern() != hp.intern()), 
			"Error : test_intern failed - 5");
		StringBuffer buff= new StringBuffer();
		buff.append('a');
		buff.append('b');
		harness.check(!( "ab".intern() != buff.toString().intern()), 
			"Error : test_intern failed - 6");
		StringBuffer buff1 = new StringBuffer();
		harness.check(!( "".intern() != buff1.toString().intern()), 
			"Error : test_intern failed - 7");

	}
	public void test_trim()
	{

         harness.checkPoint("trim()java.lang.String");
	    	String source = "   laura";
	    	String dest;

	    	dest = source.trim();
	    	if (!dest.equals("laura")) {
		harness.fail("Error - test_trim - 1");
		//System.out.println("expected 'laura', got '" + dest + "'");
	    	}

	    source = "			laura";
	    dest = source.trim();
	    if (!dest.equals("laura")) {
		harness.fail("Error - test_trim - 2");
		//System.out.println("expected 'laura', got '" + dest + "'");
	    }

	    source = "              ";
	    dest = source.trim();
	    if (!dest.equals("")) {
		harness.fail("Error - test_trim - 3");
		//System.out.println("expected '', got '" + dest + "'");
	    }
	    source = "laura";
	    dest = source.trim();
	    if (dest != source) {
		harness.fail("Error - test_trim - 4");
		//System.out.println("Expected strings to be equal");
	    }
	    source = "l        ";
	    dest = source.trim();
	    if (!dest.equals("l")) {
		harness.fail("Error - test_trim - 5");
		//System.out.println("expected 'l', got '" + dest + "'");
	    }
	    source = "           l";
	    dest = source.trim();
	    if (!dest.equals("l")) {
		harness.fail("Error - test_trim - 6");
		//System.out.println("expected 'l', got '" + dest + "'");
	    }
	    source = "           l            ";
	    dest = source.trim();
	    if (!dest.equals("l")) {
		harness.fail("Error - test_trim - 7");
		//System.out.println("expected 'l', got '" + dest + "'");
	    }
	    source = "           l a u r a             ";
	    dest = source.trim();
	    if (!dest.equals("l a u r a")) {
		harness.fail("Error - test_trim - 8");
		//System.out.println("expected 'l a u r a', got '" + dest + "'");
	    }
	}

	public void testall()
	{
		harness.setclass("java.lang.String");
		test_Basics();
		test_toString();
		test_equals();
		test_hashCode();
		test_length();
		test_charAt();
		test_getChars();
		test_getBytes();	
		test_toCharArray();
		test_equalsIgnoreCase();
		test_compareTo();
		test_regionMatches();
		test_startsWith();
		test_endsWith();
		test_indexOf();
		test_lastIndexOf();
		test_substring();
		test_concat();
		test_replace();
		test_toLowerCase();
		test_toUpperCase();
		test_valueOf();
		test_intern();
		test_trim();
	}


  public void test (TestHarness the_harness)
  {
    harness = the_harness;
    testall ();
  }

}
