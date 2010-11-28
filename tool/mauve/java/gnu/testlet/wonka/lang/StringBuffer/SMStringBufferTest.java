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



package gnu.testlet.wonka.lang.StringBuffer;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

/**
* this file contains testcode for java.lang.StringBuffer.	<br>
*/
public class SMStringBufferTest implements Testlet
{
  	protected static TestHarness th;

	public void test(TestHarness harness)
	{
		th = harness;
		th.setclass("java.lang.StringBuffer");
		
		th.checkPoint("setCharAt(int,char)void");
		test_setCharAt();

		th.checkPoint("ensureCapacity(int)void");
		test_ensureCapacity();

		th.checkPoint("delete(int,int)java.lang.StringBuffer");
		test_delete();

		th.checkPoint("deleteCharAt(int)java.lang.StringBuffer");
		test_deleteCharAt();

		th.checkPoint("replace(int,int,java.lang.String)java.lang.StringBuffer");
		test_replace();

            th.checkPoint("substring(int)java.lang.String");
		test_substring();


		th.checkPoint("indexOf(String)");
		test_indexOf_String();

		th.checkPoint("indexOf(String, int)");
		test_indexOf_String_int();

		th.checkPoint("lastIndexOf(String)");
		test_lastIndexOf_String();

		th.checkPoint("lastIndexOf(String, int)");
		test_lastIndexOf_String_int();


	}
/**
* implemented.	<br>
* --> needed to get 1.2 tag 
*/	
	public void test_delete()
	{ 
		StringBuffer sb = new StringBuffer("test delete");
		try 	{
			sb.delete(-1 ,2);
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException se)	{ th.check(true); }

		try 	{
			sb.delete(5 ,2);
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException se)	{ th.check(true); }

		try 	{
			sb.delete(20 ,25);
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException se)	{ th.check(true); }

		th.check( sb.delete(1,1) == sb , "sb.delete(1,1) == sb");
		th.check( "test delete".equals(new String(sb)), "check if no elements were removed");
		th.check( sb.length() == 11 , "size hasn't changed");
		th.check( sb.delete(4,100).toString().equals("test")," end may be greater then length" );
		th.check( sb.length() == 4 , "size should be updated -- 1");
		th.check( sb.delete(0, 4).toString().equals(""),"everything can be deleted" );
		th.check( sb.length() == 0 , "size should be updated -- 2");
                sb.delete(0,0);
                sb = new StringBuffer("test delete");
                th.check( sb.delete(2,3).toString().equals("tet delete"), "testing on delete ... -- 1");
                th.check( sb.delete(3,5).toString().equals("tetelete"), "testing on delete ... -- 2");
                th.check( sb.delete(3,4).toString().equals("tetlete"), "testing on delete ... -- 3");
                th.check( sb.delete(1,6).toString().equals("te"), "testing on delete ... -- 4");
                th.check( sb.delete(1,6).toString().equals("t"), "testing on delete ... -- 5");
                th.check( sb.delete(0,1).toString().equals(""), "testing on delete ... -- 6");
	}

/**
* implemented.	<br>
*--> needed to get tag 1.2
*/
	public void test_deleteCharAt()
	{ 
		StringBuffer sb = new StringBuffer("test delete");
		try 	{
			sb.deleteCharAt(-1);
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException se)	{ th.check(true); }

		try 	{
			sb.deleteCharAt(20);
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException se)	{ th.check(true); }

		th.check( sb.deleteCharAt(0).toString().equals("est delete"),"test deleteCharAt" );
		th.check( sb.deleteCharAt(3).toString().equals("estdelete"),"test deleteCharAt" );
		th.check( sb.deleteCharAt(3).toString().equals("estelete"),"test deleteCharAt" );
		th.check( sb.deleteCharAt(1).toString().equals("etelete"),"test deleteCharAt" );
		th.check( sb.deleteCharAt(6).toString().equals("etelet"),"test deleteCharAt" );
		th.check( sb.deleteCharAt(0).toString().equals("telet"),"test deleteCharAt" );
		th.check( sb.deleteCharAt(4).toString().equals("tele"),"test deleteCharAt" );

	}

/**
* implemented.	<br>
* --> needed to get 1.2 tag
*/
	public void test_replace()
	{ 
		StringBuffer sb = new StringBuffer("test replace");
		String s = new String("SmartMove ");
		try 	{	
			sb.replace( 1, 3, null);
			th.fail("NullPointerException should be thrown");
			}
		catch	(NullPointerException ne)	{ th.check(true); }

		try 	{	
			sb.replace( -1, 3, s );
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException ne)	{ th.check(true); }
		try 	{	
			sb.replace( 4, 3, s );
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException ne)	{ th.check(true); }
		try 	{	
			sb.replace( 19, 23, s );
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException ne)	{ th.check(true); }
		sb.replace(0, 0, s);
		th.check( sb.length() == 22 , "length is "+sb.length()+", should be 22");
		th.check( sb.toString().equals("SmartMove test replace"),
			"result is ``"+sb+"'', should be ``SmartMove test replace''");
		sb.replace(0, 35 , s);
		th.check( sb.toString().equals("SmartMove "), 
			"result is ``"+sb+"'', should be ``SmartMove''");
		th.check( sb.length() == 10 , "length is "+sb.length()+", should be 10");

		sb = new StringBuffer("test replace");
		sb.replace(1,11,s);
		th.check( sb.toString().equals("tSmartMove e"),
			"result is ``"+sb+"'', should be ``tSmartMove e''");
	}


/**
* implemented.	<br>
* --> needed for tag 1.2
*/
	public void test_substring()
	{

		StringBuffer sb = new StringBuffer("test substring");
		try 	{	
			sb.substring( -1 );
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException ne)	{ th.check(true); }
		try 	{	
			sb.substring( 19 );
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException ne)	{ th.check(true); }
		th.check(sb.substring(0).equals(sb.toString()),
			"test substring(0)	1");
		th.check(sb.substring(0).equals("test substring"),
			"test substring(0)	2");
		th.check(sb.substring(13).equals("g"),
			"test substring(length -1) == last char");
		th.check(sb.substring(5).equals("substring"),
			"test substring(5) ");
		
		
	    th.checkPoint("substring(int,int)java.lang.String");
		try 	{	
			sb.substring( -1, 1  );
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException ne)	{ th.check(true); }
		try 	{	
			sb.substring( 19 , 4 );
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException ne)	{ th.check(true); }
		try 	{	
			sb.substring( 4, 19 );
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException ne)	{ th.check(true); }
		try 	{	
			sb.substring( 5, 3 );
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException ne)	{ th.check(true); }
		try 	{	
			sb.substring( 1 , -2  );
			th.fail("StringIndexOutOfBoundsException should be thrown");
			}
		catch	(StringIndexOutOfBoundsException ne)	{ th.check(true); }

		th.check(sb.substring(0, 0).equals(""),
			"test substring(0,O)	1");
		th.check(sb.substring(0, 13).equals("test substrin"),
			"test substring(0)	2");
		th.check(sb.substring(0,14).equals("test substring"),
			"test substring(0, length ) == last char");
		th.check(sb.substring(5,6).equals("s"),
			"test substring(5,6) ");

	}
	

	public void test_ensureCapacity()
	{
		StringBuffer buf = new StringBuffer("abcdefgh");
		buf.ensureCapacity(-2);
		th.check(buf.capacity() == 24, "capacity shouldn't change");
		buf.ensureCapacity(20);
		th.check(buf.capacity() == 24, "capacity shouldn't change");
		buf.ensureCapacity(25);
		th.check(buf.capacity() == 50, "capacity should change to max (Cnew,Cold * 2)");
		buf.ensureCapacity(125);
		th.check(buf.capacity() == 125, "capacity should be 125");
	}

	
	public void test_setCharAt()
	{
		StringBuffer buf = new StringBuffer("abcdefgh");
		buf.setCharAt(4,'a');
		th.check( buf.charAt(4) == 'a', "setCharAt failed");
		for (int i=0; i < 8; i++){ buf.setCharAt(i,'a'); }
		th.check(buf.toString().equals("aaaaaaaa"), "setCharAt failed");
		for (int i=0; i < 8; i++){ buf.setCharAt(i,'\u0001'); }
		th.check(buf.toString().equals("\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"), "setCharAt failed");
		
		try 	{
			buf.setCharAt( -1 , 'b');
			th.fail("should throw IndexOutOfBoundsException");
			}
		catch	(IndexOutOfBoundsException ie)	{ th.check(true); }
		try 	{
			buf.setCharAt( 8 , 'b');
			th.fail("should throw IndexOutOfBoundsException");
			}
		catch	(IndexOutOfBoundsException ie)	{ th.check(true); }

		}
	public void test_indexOf_String() {
		StringBuffer testBuf = new StringBuffer("SELECT x, y, z");
		th.check(testBuf.indexOf(" "), 6);
		th.check(testBuf.indexOf(","), 8);
	}

	public void test_indexOf_String_int() {
		StringBuffer testBuf = new StringBuffer("SELECT x, y, z");
		th.check(testBuf.indexOf(" ", 3), 6);
		th.check(testBuf.indexOf(" ", 7), 9);
		th.check(testBuf.indexOf(",", 0), 8);
		th.check(testBuf.indexOf(",", 10), 11);
		th.check(testBuf.indexOf(",", 14), -1);
	}

	public void test_lastIndexOf_String() {
		StringBuffer testBuf = new StringBuffer("SELECT x, y, z");
		th.check(testBuf.lastIndexOf(" "), 12);
		th.check(testBuf.lastIndexOf(","), 11);
		th.check(testBuf.lastIndexOf("/"), -1);
	}

	public void test_lastIndexOf_String_int() {
		StringBuffer testBuf = new StringBuffer("SELECT x, y, z");
		th.check(testBuf.lastIndexOf(" ", 13), 12);
		th.check(testBuf.lastIndexOf(" ", 7), 6);
		th.check(testBuf.lastIndexOf(",", 0), -1);
		th.check(testBuf.lastIndexOf(",", 10), 8);
	}

}  

