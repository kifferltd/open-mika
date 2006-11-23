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


package gnu.testlet.wonka.io.PrintWriter; //complete the package name ...

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.OutputStream;
import java.io.Writer;

/**
*  this file contains test for java.io.PrintWriter   <br>
*
*/
public class SMPrintWriterTest implements Testlet
{
  protected TestHarness th;
  protected String sep=System.getProperty("line.separator","\n");
  protected String xsep="#new separator#"+System.getProperty("line.separator","\n");

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.PrintWriter");
       test_PrintWriter();
       test_print();
       test_println();
       test_write();
       test_checkError();
       test_setError();
       test_close();
       test_flush();
       test_lock();
     }


/**
* implemented. <br>
*
*/
  public void test_PrintWriter(){
    th.checkPoint("PrintWriter(java.io.Writer)");
    CharArrayWriter caw = new CharArrayWriter();
    SMExPrintWriter xpw = new SMExPrintWriter(caw);
    th.check( xpw.getOut() == caw );
    th.checkPoint("PrintWriter(java.io.Writer,boolean)");
    xpw = new SMExPrintWriter(caw,true);
    th.check( xpw.getOut() == caw );
    try {
        new SMExPrintWriter((Writer)null);
        th.fail("should throw a NullpointerException");
        }
    catch (NullPointerException ne) { th.check(true); }
    try {
        new SMExPrintWriter((Writer)null,false);
        th.fail("should throw a NullpointerException");
        }
    catch (NullPointerException ne) { th.check(true); }

    th.checkPoint("PrintWriter(java.io.OutputStream)");
    xpw = new SMExPrintWriter(new ByteArrayOutputStream());
    try {
        new SMExPrintWriter((OutputStream)null);
        th.fail("should throw a NullpointerException");
        }
    catch (NullPointerException ne) { th.check(true); }


    th.checkPoint("PrintWriter(java.io.OutputStream,boolean)");
    xpw = new SMExPrintWriter(new ByteArrayOutputStream(),true);
    try {
        new SMExPrintWriter((OutputStream)null,false);
        th.fail("should throw a NullpointerException");
        }
    catch (NullPointerException ne) { th.check(true); }
  }


/**
* implemented. <br>
*
*/
  public void test_print(){
    CharArrayWriter caw;
    SMExPrintWriter xpw;

    th.checkPoint("print(boolean)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.print(true);
    th.check( caw.toString().equals(String.valueOf(true)), "checking true");
    xpw.print(false);
    th.check( caw.toString().equals("truefalse"), "checking true+false");

    th.checkPoint("print(int)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.print(43);
    th.check( caw.toString().equals(String.valueOf(43)), "checking intValue");

    th.checkPoint("print(long)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.print(43L);
    th.check( caw.toString().equals(String.valueOf(43L)), "checking longValue");

    th.checkPoint("print(float)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.print(43.5f);
    th.check( caw.toString().equals(String.valueOf(43.5f)), "checking floatValue");

    th.checkPoint("print(double)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.print(43.25);
    th.check( caw.toString().equals(String.valueOf(43.25)), "checking doubleValue");

    th.checkPoint("print(java.lang.Object)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    Object o = new Object();
    xpw.print(o);
    th.check( caw.toString().equals(String.valueOf(o)), "checking doubleValue");
    try {
        xpw.print((Object)null);
    	th.check( caw.toString().equals(String.valueOf(o)+"null"), "checking string");
        }
    catch (NullPointerException ne) { th.fail("should not throw a NullpointerException"); }

    th.checkPoint("print(java.lang.String)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    String s = "smartmove rules!";
    xpw.print(s);
    th.check( caw.toString().equals(s), "checking string");
    try {
        xpw.print((String)null);
    	th.check( caw.toString().equals(s+"null"), "checking string");
        }
    catch (NullPointerException ne) { th.fail("should not throw a NullpointerException"); }

    th.checkPoint("print(char)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.print('a');
    th.check( caw.toString().equals(String.valueOf('a')), "checking char");

    th.checkPoint("print(char[])void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.print(s.toCharArray());
    th.check( caw.toString().equals(String.valueOf(s.toCharArray())), "checking charArray");
    try {
        xpw.print((char[])null);
        th.fail("should throw a NullpointerException");
        }
    catch (NullPointerException ne) { th.check(true); }
  }

/**
* implemented. <br>
*
*/
  public void test_println(){
    CharArrayWriter caw;
    SMExPrintWriter xpw;
    th.checkPoint("println()void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.println();
    th.check( caw.toString().equals(sep), "checking line separator");

    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw,1);
    xpw.println();
    th.check( caw.toString().equals(xsep), "checking line separator");

    SMErrorWriter ew = new SMErrorWriter();
    xpw  = new SMExPrintWriter(ew,false);
    xpw.println();
    th.check(!ew.isFlushed(), "not flushed --> auto_flush is off");
    xpw  = new SMExPrintWriter(ew,true);
    xpw.println("abcdef");
    //th.check(ew.isFlushed(), "flushed --> auto_flush is on");

    th.checkPoint("println(boolean)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.println(true);
    th.check( caw.toString().equals(String.valueOf(true)+sep), "checking true");
    xpw.println(false);
    th.check( caw.toString().equals("true"+sep+"false"+sep), "checking true+false");

    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw,1);
    xpw.println(true);
    th.check( caw.toString().equals(String.valueOf(true)+xsep), "checking use of println()");

    th.checkPoint("println(int)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.println(43);
    th.check( caw.toString().equals(String.valueOf(43)+sep), "checking intValue");

    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw,1);
    xpw.println(43);
    th.check( caw.toString().equals(String.valueOf(43)+xsep), "checking use of println()");

    th.checkPoint("println(long)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.println(43L);
    th.check( caw.toString().equals(String.valueOf(43L)+sep), "checking longValue");

    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw,1);
    xpw.println(43L);
    th.check( caw.toString().equals(String.valueOf(43L)+xsep), "checking use of println()");

    th.checkPoint("println(float)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.println(43.5f);
    th.check( caw.toString().equals(String.valueOf(43.5f)+sep), "checking floatValue");

    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw,1);
    xpw.println(43.5f);
    th.check( caw.toString().equals(String.valueOf(43.5f)+xsep), "checking use of println()");

    th.checkPoint("println(double)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.println(43.25);
    th.check( caw.toString().equals(String.valueOf(43.25)+sep), "checking doubleValue");

    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw,1);
    xpw.println(43.25);
    th.check( caw.toString().equals(String.valueOf(43.25)+xsep), "checking use of println()");

    th.checkPoint("println(java.lang.Object)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    Object o = new Object();
    xpw.println(o);
    th.check( caw.toString().equals(String.valueOf(o)+sep), "checking ObjectValue");
    try {
        xpw.println((Object)null);
    	th.check( caw.toString().equals(String.valueOf(o)+sep+"null"+sep), "checking string");
        }
    catch (NullPointerException ne) { th.fail("should not throw a NullpointerException"); }

    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw,1);
    xpw.println(o);
    th.check( caw.toString().equals(String.valueOf(o)+xsep), "checking use of println()");

    th.checkPoint("println(java.lang.String)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    String s = "smartmove rules!";
    xpw.println(s);
    th.check( caw.toString().equals(s+sep), "checking string");
    try {
        xpw.println((String)null);
    	th.check( caw.toString().equals(s+sep+"null"+sep), "checking string");
        }
    catch (NullPointerException ne) { th.fail("should not throw a NullpointerException"); }

    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw,1);
    xpw.println(s);
    th.check( caw.toString().equals(s+xsep), "checking use of println()");

    th.checkPoint("println(char)void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.println('a');
    th.check( caw.toString().equals(String.valueOf('a')+sep), "checking char");

    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw,1);
    xpw.println('a');
    th.check( caw.toString().equals(String.valueOf('a')+xsep), "checking use of println()");

    th.checkPoint("println(char[])void");
    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw);
    xpw.println(s.toCharArray());
    th.check( caw.toString().equals(String.valueOf(s.toCharArray())+sep), "checking charArray");
    try {
        xpw.print((char[])null);
        th.fail("should throw a NullpointerException");
        }
    catch (NullPointerException ne) { th.check(true); }

    caw  = new CharArrayWriter();
    xpw  = new SMExPrintWriter(caw,1);
    xpw.println(s.toCharArray());
    th.check( caw.toString().equals(String.valueOf(s.toCharArray())+xsep), "checking use of println()");
  }

/**
* implemented. <br>
*
*/
  public void test_write(){
   th.checkPoint("write(int)void");
    	CharArrayWriter caw = new CharArrayWriter();
    	SMExPrintWriter xw = new SMExPrintWriter(caw);
    	xw.write(97);
    	xw.write(98);
    	xw.write(99);
    	xw.write(98);
    	xw.write(97);
    	th.check(caw.toString().equals("abcba"), "check if chars are added");

   th.checkPoint("write(char[])void");
	caw = new CharArrayWriter();
    	xw = new SMExPrintWriter(caw);
 	xw.write("".toCharArray());
    	th.check("".equals(caw.toString()),"checking write -- 1, got:"+caw.toString());
	xw.write("bcdefg".toCharArray());
    	th.check( "bcdefg".equals(caw.toString()),"checking write -- 2, got:"+caw.toString());
	xw.write("abcd".toCharArray());
    	th.check( "bcdefgabcd".equals(caw.toString()),"checking write -- 3, got:"+caw.toString());
	xw.write("a".toCharArray());
    	th.check( "bcdefgabcda".equals(caw.toString()),"checking write -- 4, got:"+caw.toString());
    try { char []ca=null;
    	xw.write(ca);
    	th.fail("should throw NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 1, got:"+e); }

   th.checkPoint("write(char[],int,int)void");
	caw = new CharArrayWriter();
    	xw = new SMExPrintWriter(caw);
 	xw.write("".toCharArray(),0,0);
    	th.check("".equals(caw.toString()),"checking write -- 1, got:"+caw.toString());
	xw.write("abcdefgh".toCharArray(),1,6);
    	th.check( "bcdefg".equals(caw.toString()),"checking write -- 2, got:"+caw.toString());
	xw.write("abcd".toCharArray(),4,0);
    	th.check( "bcdefg".equals(caw.toString()),"checking write -- 3, got:"+caw.toString());
	xw.write("a".toCharArray(),0,0);
    	th.check( "bcdefg".equals(caw.toString()),"checking write -- 4, got:"+caw.toString());
	xw.write("abcd".toCharArray(),2,2);
    	th.check( "bcdefgcd".equals(caw.toString()),"checking write -- 5, got:"+caw.toString());
	xw.write("abcd".toCharArray(),0,4);
    	th.check( "bcdefgcdabcd".equals(caw.toString()),"checking write -- 6, got:"+caw.toString());
    try { char []ca=null;
    	xw.write(ca,4,5);
    	th.fail("should throw NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 1, got:"+e); }
    try {
    	xw.write("abcde".toCharArray(),4,2);
    	th.fail("should throw an IndexOutOfBoundsException -- 1");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 2, got:"+e); }
    try {
    	xw.write("abcde".toCharArray(),-4,2);
    	th.fail("should throw an IndexOutOfBoundsException -- 2");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 3, got:"+e); }
    try {
    	xw.write("abcde".toCharArray(),4,-2);
    	th.fail("should throw an IndexOutOfBoundsException -- 3");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 4, got:"+e); }
    try {
    	xw.write("abcde".toCharArray(),6,0);
    	th.fail("should throw an IndexOutOfBoundsException -- 4");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 5, got:"+e); }

   th.checkPoint("write(java.lang.String)void");
    	caw = new CharArrayWriter();
    	xw = new SMExPrintWriter(caw);
 	xw.write("",0,0);
    	th.check("".equals(caw.toString()),"checking write -- 1, got:"+caw.toString());
	xw.write("bcdefg");
    	th.check("bcdefg".equals(caw.toString()),"checking write -- 2, got:"+caw.toString());
	xw.write("abcd");
    	th.check( "bcdefgabcd".equals(caw.toString()),"checking write -- 3, got:"+caw.toString());
	xw.write("a");
    	th.check( "bcdefgabcda".equals(caw.toString()),"checking write -- 4, got:"+caw.toString());
    try { String s = null ;
    	xw.write(s);
    	th.fail("should throw NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 1, got:"+e); }

   th.checkPoint("write(java.lang.String,int,int)void");
    	caw = new CharArrayWriter();
    	xw = new SMExPrintWriter(caw);
 	xw.write("",0,0);
    	th.check("".equals(caw.toString()),"checking write -- 1, got:"+caw.toString());
	xw.write("abcdefgh",1,6);
    	th.check( "bcdefg".equals(caw.toString()),"checking write -- 2, got:"+caw.toString());
	xw.write("abcd",4,0);
    	th.check( "bcdefg".equals(caw.toString()),"checking write -- 3, got:"+caw.toString());
	xw.write("a",0,0);
    	th.check( "bcdefg".equals(caw.toString()),"checking write -- 4, got:"+caw.toString());
	xw.write("abcd",2,2);
    	th.check( "bcdefgcd".equals(caw.toString()),"checking write -- 5, got:"+caw.toString());
	xw.write("abcd",0,4);
    	th.check( "bcdefgcdabcd".equals(caw.toString()),"checking write -- 6, got:"+caw.toString());
    try { String s = null ;
    	xw.write(s,4,5);
    	th.fail("should throw NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 1, got:"+e); }
    try {
    	xw.write("abcde",4,2);
    	th.fail("should throw an IndexOutOfBoundsException -- 1");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 2, got:"+e); }
    try {
    	xw.write("abcde",-4,2);
    	th.fail("should throw an IndexOutOfBoundsException -- 2");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 3, got:"+e); }
    try {
    	xw.write("abcde",4,-2);
    	th.fail("should throw an IndexOutOfBoundsException -- 3");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 4, got:"+e); }
    try {
    	xw.write("abcde",6,0);
    	th.fail("should throw an IndexOutOfBoundsException -- 4");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 5, got:"+e); }

  }

/**
* implemented. <br>
*
*/
  public void test_checkError(){
    th.checkPoint("checkError()boolean");
    	SMErrorWriter ew = new SMErrorWriter();
    	SMExPrintWriter xw = new SMExPrintWriter(ew);
    	xw.close();
	th.check(xw.checkError() , "checking Error flag set -- close");
    	xw = new SMExPrintWriter(ew);
    	xw.flush();
	th.check(xw.checkError() , "checking Error flag set -- flush");
    	xw = new SMExPrintWriter(ew);
    	xw.write(1);
	th.check(xw.checkError() , "checking Error flag set -- write 1");
    	xw = new SMExPrintWriter(ew);
    	xw.write("abcd",1,3);
	th.check(xw.checkError() , "checking Error flag set -- write 2");
    	xw = new SMExPrintWriter(ew);
    	xw.write("as");
	th.check(xw.checkError() , "checking Error flag set -- write 3");
    	xw = new SMExPrintWriter(ew);
    	char [] ca = "abcde".toCharArray();
    	xw.write(ca,1,3);
	th.check(xw.checkError() , "checking Error flag set -- write 4");
    	xw = new SMExPrintWriter(ew);
    	xw.write(ca);
	th.check(xw.checkError() , "checking Error flag set -- write 5");

  }

/**
* implemented. <br>
*
*/
  public void test_setError(){
    th.checkPoint("setError()void");
    	CharArrayWriter caw = new CharArrayWriter();
    	SMExPrintWriter xw = new SMExPrintWriter(caw);
	xw.setError();
	th.check(xw.checkError() , "checking setError");
  }

/**
* implemented. <br>
*
*/
  public void test_close(){
    th.checkPoint("close()void");
    	SMErrorWriter ew = new SMErrorWriter(1);
    	SMExPrintWriter xw = new SMExPrintWriter(ew);
        xw.close();
        th.check( ew.timesClosed()==1 );
        xw.close();
        xw.close();
        xw.close();
        th.check( ew.timesClosed()==1 ,"should only sent one close");
  }

/**
* implemented. <br>
*
*/
  public void test_flush(){
    th.checkPoint("flush()void");
    	SMErrorWriter ew = new SMErrorWriter();
    	SMExPrintWriter xw = new SMExPrintWriter(ew);
        xw.flush();
        th.check( ew.isFlushed());

  }

/**
*   not implemented. <br>
*
*/
  public void test_lock(){
    th.checkPoint("lock(protected)java.lang.Object");
    	SMErrorWriter ew = new SMErrorWriter();
    	SMExPrintWriter wr = new SMExPrintWriter(ew);
    	SMlockPrintWriter lt = new SMlockPrintWriter();
    	
    	int i = setupLockThread(lt,wr);
    	wr.write(97);
      //th.debug("accesed Writer");
	    th.check( i+1 == accesed , "accesed xr before lock was released -- write 1");    	
    	try { t.join(1000L); }
    	catch(Exception e) {}
      //th.debug("next test ...");

	lt = new SMlockPrintWriter();
    	i = setupLockThread(lt,wr);
    	wr.write("abcd",1,2);
//th.debug("accesed Writer");
	th.check( i+1 == accesed , "accesed xr before lock was released -- write 2");    	
    	try { t.join(1000L); }
    	catch(Exception e) {}
//th.debug("next test ...");

	lt = new SMlockPrintWriter();
    	i = setupLockThread(lt,wr);
    	wr.write("abcd");
//th.debug("accesed Writer");
	th.check( i+1 == accesed , "accesed xr before lock was released -- write 3");    	
    	try { t.join(1000L); }
    	catch(Exception e) {}
//th.debug("next test ...");

	lt = new SMlockPrintWriter();
    	i = setupLockThread(lt,wr);
    	wr.write("abcd".toCharArray(),1,2);
//th.debug("accesed Writer");
	th.check( i+1 == accesed , "accesed xr before lock was released -- write 4");    	
    	try { t.join(1000L); }
    	catch(Exception e) {}
//th.debug("next test ...");

	lt = new SMlockPrintWriter();
    	i = setupLockThread(lt,wr);
    	wr.write("abcd".toCharArray());
//th.debug("accesed Writer");
	th.check( i+1 == accesed , "accesed xr before lock was released -- write 5");    	
    	try { t.join(1000L); }
    	catch(Exception e) {}
//th.debug("next test ...");

	lt = new SMlockPrintWriter();
    	i = setupLockThread(lt,wr);
    	wr.print("abcd");
//th.debug("accesed Writer");
	th.check( i+1 == accesed , "accesed xr before lock was released -- print");    	
    	try { t.join(1000L); }
    	catch(Exception e) {}
//th.debug("next test ...");

	lt = new SMlockPrintWriter();
    	i = setupLockThread(lt,wr);
    	wr.println();
//th.debug("accesed Writer");
	th.check( i+1 == accesed , "accesed xr before lock was released -- println");    	
    	try { t.join(1000L); }
    	catch(Exception e) {}
//th.debug("next test ...");
/*
	lt = new SMlockPrintWriter();
    	i = setupLockThread(lt,wr);
    	wr.setError();
//th.debug("accesed Writer");
	th.check( i+1 == accesed , "accesed xr before lock was released -- setError");    	
    	try { wr.wait(300L); }
    	catch(Exception e) {}
//th.debug("next test ...");

	lt = new SMlockPrintWriter();
    	i = setupLockThread(lt,wr);
    	wr.checkError();
//th.debug("accesed Writer");
	th.check( i+1 == accesed , "accesed xr before lock was released -- checkError");    	
    	try { wr.wait(300L); }
    	catch(Exception e) {}
//th.debug("next test ...");
*/
	lt = new SMlockPrintWriter();
    	i = setupLockThread(lt,wr);
    	wr.flush();
//th.debug("accesed Writer");
	th.check( i+1 == accesed , "accesed xr before lock was released -- flush");    	
    	try { t.join(1000L); }
    	catch(Exception e) {}
//th.debug("next test ...");

	lt = new SMlockPrintWriter();
    	i = setupLockThread(lt,wr);
    	wr.close();
//th.debug("accesed Writer");
	th.check( i+1 == accesed , "accesed xr before lock was released -- close");    	
    	try { t.join(1000L); }
    	catch(Exception e) {}
//th.debug("next test ...");
  }

  protected int accesed=0;

  public void inc() {
  	accesed++;
  }	

  public int setupLockThread(SMlockPrintWriter lt,SMExPrintWriter pw) {
    f1=false;
    lt.setWriter(pw);
    lt.setWT(this);
    t = new Thread(lt);
    int ret = accesed;
    t.start();
    while (!f1) {
    	Thread.yield();
    }
    return ret;
  }

  private Thread t = null;
  private volatile boolean f1=false;

  public void set1(){
  	f1 = true;
  }


}
