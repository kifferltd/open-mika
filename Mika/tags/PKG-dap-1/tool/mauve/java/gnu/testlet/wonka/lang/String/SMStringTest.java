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



package gnu.testlet.wonka.lang.String;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.io.UnsupportedEncodingException;

/**
* the Class java.lang.String is tested in:<br>
*	- StringTest  <br>
*	-SMStringTest  <br>
* all member functions are tested, but read comments in this file to see where <br>
* test should be added <br>
*    <br>
* the function intern() is not completely tested --> add code if needed<br>
*   <br>
* we should lookup how hashcodes of strings are calculated in wonka <br>
* --> same as jdk  <br>
*
* String has 2 new methods: CASE_INSENSITIVE_ORDER and compareToIgnoreCase()<br>
* compareTo has changed in jdk 1.2 <br>
*/
public class SMStringTest implements Testlet
{
  	public void test (TestHarness harness)
	{
		th = harness;
		th.setclass("java.lang.String");

		th.checkPoint("trim()java.lang.String");
		test_trim();

		test_encoding();
		
		th.checkPoint("compareToIgnoreCase(java.lang.String)int");
		test_compareToIgnoreCase();
		
		th.checkPoint("compareTo(java.lang.String)int");
		test_compareTo2ndMethod();
		
		test_CASE_INSENSITIVE_ORDER();

		cygnustests(); //these test are taken from different files from Cygnus	

		test_extra_lastIndexOf();	
	}

	protected TestHarness th;

/**
* implemented.	<br>
* --> needed for tag 1.2
*/
	public void test_compareToIgnoreCase()
	{
		String s = new String("abc");
		th.check( s.compareToIgnoreCase("ABC")== 0,
			"test check small <--> capital	1");	
		th.check( s.compareToIgnoreCase("ABCa")== -1,
			"test check small <--> capital	2");	
		th.check( s.compareToIgnoreCase("AB")== 1,
			"test check small <--> capital	3");	
		th.check( s.compareToIgnoreCase("ABH")==-5,
			"test check small <--> capital	4");	

		s = new String("AbCdEfG");
		th.check( s.compareToIgnoreCase("ABC")==4,
			"test check small <--> capital	5");	
		th.check( s.compareToIgnoreCase("abcdefg")== 0,
			"test check small <--> capital	6");	
		th.check( s.compareToIgnoreCase("aBcDeFg")== 0,
			"test check small <--> capital	7");	
		th.check( s.compareToIgnoreCase("ABH")==-5,
			"test check small <--> capital	8");	

		if (gnu.testlet.UnicodeSubsets.isSupported("8")) {
		s = new String("\u0393a\u03B2");
		th.check( s.compareToIgnoreCase("\u03B3A\u0392")==0,
			"test check small <--> capital	9");
		}

	}

/**
*  implemented.	<br>	
*  since JDK 1.2 compareTo can be passed an Object<br>
* --> needed for tag 1.2
*/
	public void test_compareTo2ndMethod()
	{
		String s = new String("SmartMove");	
		try	{
			s.compareTo(new Object());
			th.fail("ClassCastException should be thrown");
			}
		catch 	(ClassCastException ce)	{ th.check(true); }
	}


/**
* not implemented.	<br>
* this is an public static final comparator <br>
* the comparator will use compareToIngnoreCase to determin the <br>
* ordering of strings<br>
* <br>
* --> needed for tag 1.2
*/
	public void test_CASE_INSENSITIVE_ORDER()
	{

	}

/**
* this function will test trim()<br>
* <br>
* especially on the definition of white space <br>
* - white space in trim UNICODE less than \u0020 	( p 1653 ) <br>
* - white space in Character.isWhitespace() 		( p 300 )  <br>
*	It is a Unicode space separator (category "Zs"), but is not a no-break space (\u00A0 or \uFEFF).<br>
*       It is a Unicode line separator (category "Zl"). <br>
*       It is a Unicode paragraph separator (category "Zp"). <br>
*       It is \u0009, HORIZONTAL TABULATION. <br>
*       It is \u000A, LINE FEED.   <br>
*       It is \u000B, VERTICAL TABULATION. <br>
*       It is \u000C, FORM FEED. <br>
*       It is \u000D, CARRIAGE RETURN. <br>
*       It is \u001C, FILE SEPARATOR.<br>
*       It is \u001D, GROUP SEPARATOR. <br>
*       It is \u001E, RECORD SEPARATOR.<br>
*       It is \u001F, UNIT SEPARATOR. <br>
*   --> inconsistant definition  <br>
* we will follow specs for the trim()  <br>
*/
protected void test_trim()
{
	String s = new String("\thelp\t");
	th.check(s.trim().equals("help"),
		"\\t is a white space !!");
	s = new String("\nhelp\n");
	th.check(s.trim().equals("help"),
		"\\n is a white space !!");
	s = new String("\u0019\u0018help\u0019\u0018");
	th.check(s.trim().equals("help"),
		"\\u0018 and \\u0019 are white space !!");
	s = new String("\u0017\u0016help\u0017\u0016");
	th.check(s.trim().equals("help"),
		"\\u0016 and \\u0017 are white space !!");
	s = new String("\u0015\u0014help\u0015\u0014");
	th.check(s.trim().equals("help"),
		"\\u0014 and \\u0015 are white space !!");
	s = new String("\u0013\u0012help\u0013\u0012");
	th.check(s.trim().equals("help"),
		"\\u0012 and \\u0013 are white space !!");
	s = new String("\u0011\u0010help\u0010\u0011");
	th.check(s.trim().equals("help"),
		"\\u0010 and \\u0011 are white space !!");
	s = new String("\u001a\u001bhelp\u001a\u001b");
	th.check(s.trim().equals("help"),
		"\\u001a and \\u001b are white space !!");
	s = new String("\u001c\u001dhelp\u001d\u001c");
	th.check(s.trim().equals("help"),
		"\\u001c and \\u001d are white space !!");
	s = new String("\u0020\u001e\u001fhelp\u001f\u001e\u0020");
	th.check(s.trim().equals("help"),
		"\\u001e, \\u001f and \\u0020 are  white space !!");
	s = new String("\u0000\u0001help\u0001\u0000");
	th.check(s.trim().equals("help"),
		"\\u0000 and \\u0001 are white space !!");
	s = new String("\u0002\u0003help\u0002\u0003");
	th.check(s.trim().equals("help"),
		"\\u0002 and \\u0003 are white space !!");
	s = new String("\u0004\u0005help\u0005\u0004");
	th.check(s.trim().equals("help"),
		"\\u0004 and \\u0005 are white space !!");
	s = new String("\u0006\u0007help\u0006\u0007");
	th.check(s.trim().equals("help"),
		"\\u0006 and \\u0007 are white space !!");
	s = new String("\u0008\u0009help\u0009\u0008");
	th.check(s.trim().equals("help"),
		"\\u0008 and \\u0009 are  white space !!");
	s = new String("\u2008\u2009help\u2009\u2008");
	th.check(!s.trim().equals("help"),
		"\\u2008 and \\u2009 are not white space !!");
	s = new String("\u2028\u2029\u3000help\u3000\u2029\u2028");
	th.check(!s.trim().equals("help"),
		"\\u2028, \\u2029 and \\u3000 are not white space !!");
}



/**
* this functions tests the constructors who make use of encoding Identifiers<br>
* <br>
* THIS FUNCTIONS NEEDS EXTRA TESTS  <br>
* --> getBytes(enc) Same Case  <br>
*/
protected void test_encoding()
  {	
      th.checkPoint("String(byte[],java.lang.String)");
      char[] cstr = { 'a', 'b', 'c', '\t', 'A', 'B', 'C', ' ', '1', '2', '3' };
      byte[] bstr = new byte [cstr.length];
      for (int i = 0; i < cstr.length; ++i)
	bstr[i] = (byte) cstr[i];

      String a = new String(bstr);
      String b = new String(bstr, 3, 3);
      String c = "";
      String d = "";

      try
	{
	  c = new String(bstr, "8859_1");
	}
      catch (UnsupportedEncodingException ex)
	{
	}

      try
	{
	  d = new String(bstr, 3, 3, "8859_1");
	}
      catch (UnsupportedEncodingException ex)
	{
	}

      th.check (a, "abc	ABC 123");
      th.check (b, "	AB");
      th.check (c, "abc	ABC 123");
      th.check (d, "	AB");

      boolean ok = false;
      try
	{
	  c = new String(bstr, "foobar8859_1");
	}
      catch (UnsupportedEncodingException ex)
	{
	  ok = true;
	}
      th.check (ok);

      ok = false;
      try
	{
	  d = new String(bstr, 3, 3, "foobar8859_1");
	}
      catch (UnsupportedEncodingException ex)
	{
	  ok = true;
	}
      th.check (ok);

      th.check (String.copyValueOf(cstr), "abc	ABC 123");
      th.check (String.copyValueOf(cstr, 3, 3), "	AB");
   }	

protected void cygnustests()	
  {

  th.checkPoint("charAt(int)char");

      String b = new String(" abc\tABC 123\t");

      boolean ok;

      ok = false;
      try
	{
	  b.charAt(b.length());
	}
      catch (StringIndexOutOfBoundsException ex)
	{
	  ok = true;
	}
      th.check (ok);

      ok = false;
      try
	{
	  b.charAt(-1);
	}
      catch (StringIndexOutOfBoundsException ex)
	{
	  ok = true;
	}
      th.check (ok);
      th.check( b.charAt(1) == 'a',"Error wrong return element !!");

 th.checkPoint("compareTo(java.lang.String)int");
 char[] cstr = { 'a', 'b', 'c', '\t', 'A', 'B', 'C', ' ', '1', '2', '3' };

      String a = new String();
      String d = new String(cstr);
      String e = new String(cstr, 3, 3);

      th.check (d.compareTo(d),0);	
      th.check (d.compareTo(b.trim()), 0);
      th.check (d.compareTo(a), 11);
      th.check (d.compareTo(b), 65);
      th.check (d.compareTo(e), 88);
      th.check (d.toLowerCase().compareTo(d), 32);
      th.check (d.compareTo(d.substring(0, d.length() - 2)), 2);

      th.check (a.compareTo(d), -11);
      th.check (b.compareTo(d), -65);
      th.check (e.compareTo(d), -88);
      th.check (d.compareTo(d.toLowerCase()), -32);
      th.check (d.substring(0, d.length() - 2).compareTo(d), -2);

      th.check (b.charAt(7), 'C');

  th.checkPoint("getBytes(java.lang.String)byte[]");
  String s = new String ("test me");
    try
      {
	byte[] ba = s.getBytes("8859_1");
	th.check (ba.length, s.length());
      }
    catch (UnsupportedEncodingException _)
      {
	th.check (false);
      }
   th.checkPoint("hashCode()int");  

      String c = new String(new StringBuffer("abc\tABC 123"));

      /* These results are for JDK 1.2; the hashCode algorithm changed
	 from JDK 1.1.  */
      th.check (a.hashCode(), 0);
      th.check (b.hashCode(), -524164548);
      th.check (c.hashCode(), -822419571);  


  
  th.checkPoint("indexOf(int)int");


      th.check (b.indexOf(' '), 0);
      
      th.checkPoint("indexOf(int,int)int");
      th.check (b.indexOf(' ', 1), 8);
      th.check (b.indexOf(' ', 10), -1);
      th.check (b.indexOf(' ', -1), 0);
      th.check (b.indexOf(' ', b.length()), -1);
      th.check (b.indexOf("abc"), 1);
      th.check (b.indexOf("abc", 1), 1);
      th.check (b.indexOf("abc", 10), -1);
    
      th.checkPoint("lastIndexOf(int)int");
      th.check (b.lastIndexOf(' '), 8);
    
      th.checkPoint("lastIndexOf(int,int)int");
      th.check (b.lastIndexOf(' ', 1), 0);
      th.check (b.lastIndexOf(' ', 10), 8);
      th.check (b.lastIndexOf(' ', -1), -1);
      th.check (b.lastIndexOf(' ', b.length()), 8);
      th.check (b.lastIndexOf("abc"), 1);
      th.check (b.lastIndexOf("abc", 1), 1);
      th.check (b.lastIndexOf("abc", 10), 1);  
 

     }

 public void test_extra_lastIndexOf() {	
      th.checkPoint("lastIndexOf(java.lang.String,int)int");
      String s = "";
      th.check (s.lastIndexOf("abc", 10), -1 , "not there");
      th.check (s.lastIndexOf("", 10), 0 , "is there ?");
      th.check (s.lastIndexOf("", 0), 0 , "is there ?");
      th.check (s.lastIndexOf("", -1), -1 , "not there");
      try { s.lastIndexOf(null,1);
      	    th.fail("should throw NullPointerExcpetion");
      }
      catch (NullPointerException ne) { th.check(true); }
      s = "acunia";
      th.check (s.lastIndexOf("abc", 10), -1 , "not there");
      th.check (s.lastIndexOf("", 10), 6 , "is there ?");
      th.check (s.lastIndexOf("", 0), 0 , "is there ?");
      th.check (s.lastIndexOf("", -1), -1 , "not there");
      th.check (s.lastIndexOf("ac", 10), 0 , "not there");
      th.check (s.lastIndexOf("ia", 10), 4 , "is there ?");
      th.check (s.lastIndexOf("ia", 2), -1 , "is there ?");
      th.check (s.lastIndexOf("smartmove", 10), -1 , "not there");
      th.check (s.lastIndexOf("a", 0), 0 , "is there");
      th.check (s.lastIndexOf("a", 6), 5 , "is there");
      th.check (s.lastIndexOf("a", 1), 0 , "is there");
      	
 }
}  
