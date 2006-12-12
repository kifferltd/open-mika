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


package gnu.testlet.wonka.io.PrintStream; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...
import java.lang.reflect.Field;
/**
*  this file contains test for java.io.PrintStream   <br>
*
*/
public class SMPrintStreamTest implements Testlet
{
  protected TestHarness th;

  protected Field fautof=null;
  protected String sep=System.getProperty("line.separator","\n");

  private boolean JDK = false;	

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.PrintStream");
       setupFields();
       test_PrintStream();
       test_print();
       test_println();
       test_write();
       test_checkError();
       test_setError();
       test_close();
       test_flush();
    }

   public void setupFields() {
    th.checkPoint("setting up reflection()...");
    Field [] fa = PrintStream.class.getDeclaredFields();
    int i;

//    for (i=0; i < fa.length ; i++) th.debug(fa[i].toString());

    for (i=0; i < fa.length ; i++)
    { if (fa[i].getName().equals(JDK ? "autoFlush":"autoFlush")) break; }
    if ( i == fa.length)  th.fail("field auto_flush not found");
    else {
    	 fa[i].setAccessible(true);
    	 fautof = fa[i];
    }
  }

/**
* implemented. <br>
*
*/
  public void test_PrintStream(){
    th.checkPoint("PrintStream(java.io.OutputStream)");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    SMExPrintStream xpw = new SMExPrintStream(bos);
    try {
     	th.check(! fautof.getBoolean(xpw)  , "checking auto_flush value");
     	th.check(xpw.getOut() == bos , "checking value field out");
    	}
    catch(Exception e){ th.fail("got unwanted exception -- 1, got:"+e);}	
    try {
        new SMExPrintStream(null);
        th.fail("should throw a NullpointerException");
        }
    catch (NullPointerException ne) { th.check(true); }


    th.checkPoint("PrintStream(java.io.OutputStream,boolean)");
    xpw = new SMExPrintStream(bos,true);
    try {
     	th.check( fautof.getBoolean(xpw)  , "checking auto_flush value");
     	th.check(xpw.getOut() == bos , "checking value field out");
   	}
    catch(Exception e){ th.fail("got unwanted exception -- 1, got:"+e);}	
    try {
        new SMExPrintStream(null,false);
        th.fail("should throw a NullpointerException");
        }
    catch (NullPointerException ne) { th.check(true); }

  }


/**
* implemented. <br>
*
*/
  public void test_print(){
    th.checkPoint("print(boolean)void");
    ByteArrayOutputStream caw;
    SMExPrintStream xpw;

    th.checkPoint("print(boolean)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    xpw.print(true);
    th.check( caw.toString().equals(String.valueOf(true)), "checking true");
    xpw.print(false);
    th.check( caw.toString().equals("truefalse"), "checking true+false");

    th.checkPoint("print(int)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    xpw.print(43);
    th.check( caw.toString().equals(String.valueOf(43)), "checking intValue");

    th.checkPoint("print(long)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    xpw.print(43L);
    th.check( caw.toString().equals(String.valueOf(43L)), "checking longValue");

    th.checkPoint("print(float)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    xpw.print(43.5f);
    th.check( caw.toString().equals(String.valueOf(43.5f)), "checking floatValue");

    th.checkPoint("print(double)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    xpw.print(43.25);
    th.check( caw.toString().equals(String.valueOf(43.25)), "checking doubleValue");

    th.checkPoint("print(java.lang.Object)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    Object o = new Object();
    xpw.print(o);
    th.check( caw.toString().equals(String.valueOf(o)), "checking doubleValue");
    try {
        xpw.print((Object)null);
    	th.check( caw.toString().equals(String.valueOf(o)+"null"), "checking string");
        }
    catch (NullPointerException ne) { th.fail("should not throw a NullpointerException"); }

    th.checkPoint("print(java.lang.String)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    String s = "smartmove rules!";
    xpw.print(s);
    th.check( caw.toString().equals(s), "checking string");
    try {
        xpw.print((String)null);
    	th.check( caw.toString().equals(s+"null"), "checking string");
        }
    catch (NullPointerException ne) { th.fail("should not throw a NullpointerException"); }

    SMErrorStream es = new SMErrorStream();
    xpw  = new SMExPrintStream(es,true);
    xpw.print("hello,\nhow are you?");
    th.check(es.isFlushed() , "checking autoflush");
    es = new SMErrorStream();
    xpw  = new SMExPrintStream(es,false);
    xpw.print("hello,\nhow are you?");
    th.check(!es.isFlushed() , "checking autoflush");

    th.checkPoint("print(char)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    xpw.print('a');
    th.check( caw.toString().equals(String.valueOf('a')), "checking char");

    th.checkPoint("print(char[])void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw,true);
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
    ByteArrayOutputStream caw;
    SMExPrintStream xpw;
    th.checkPoint("println()void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    xpw.println();
    th.check( caw.toString().equals(sep), "checking line separator");

    SMErrorStream ew = new SMErrorStream();
    xpw  = new SMExPrintStream(ew,false);
    xpw.println();
    th.check(!ew.isFlushed(), "not flushed --> auto_flush is off");
    xpw  = new SMExPrintStream(ew,true);
    xpw.println();
    th.check(ew.isFlushed(), "flushed --> auto_flush is on");

    th.checkPoint("println(boolean)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    xpw.println(true);
    th.check( caw.toString().equals(String.valueOf(true)+sep), "checking true");
    xpw.println(false);
    th.check( caw.toString().equals("true"+sep+"false"+sep), "checking true+false");


    th.checkPoint("println(int)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    xpw.println(43);
    th.check( caw.toString().equals(String.valueOf(43)+sep), "checking intValue");


    th.checkPoint("println(long)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    xpw.println(43L);
    th.check( caw.toString().equals(String.valueOf(43L)+sep), "checking longValue");


    th.checkPoint("println(float)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    xpw.println(43.5f);
    th.check( caw.toString().equals(String.valueOf(43.5f)+sep), "checking floatValue");


    th.checkPoint("println(double)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    xpw.println(43.25);
    th.check( caw.toString().equals(String.valueOf(43.25)+sep), "checking doubleValue");


    th.checkPoint("println(java.lang.Object)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    Object o = new Object();
    xpw.println(o);
    th.check( caw.toString().equals(String.valueOf(o)+sep), "checking ObjectValue");
    try {
        xpw.println((Object)null);
    	th.check( caw.toString().equals(String.valueOf(o)+sep+"null"+sep), "checking string");
        }
    catch (NullPointerException ne) { th.fail("should not throw a NullpointerException"); }


    th.checkPoint("println(java.lang.String)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    String s = "smartmove rules!";
    xpw.println(s);
    th.check( caw.toString().equals(s+sep), "checking string");
    try {
        xpw.println((String)null);
    	th.check( caw.toString().equals(s+sep+"null"+sep), "checking string");
        }
    catch (NullPointerException ne) { th.fail("should not throw a NullpointerException"); }

    th.checkPoint("println(char)void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    xpw.println('a');
    th.check( caw.toString().equals(String.valueOf('a')+sep), "checking char");

    th.checkPoint("println(char[])void");
    caw  = new ByteArrayOutputStream();
    xpw  = new SMExPrintStream(caw);
    xpw.println(s.toCharArray());
    th.check( caw.toString().equals(String.valueOf(s.toCharArray())+sep), "checking charArray");

    try {
        xpw.println((char[])null);
        th.fail("should throw a NullpointerException");
        }
    catch (NullPointerException ne) { th.check(true); }
  }

/**
*   not implemented. <br>
*
*/
  public void test_write(){
   th.checkPoint("write(int)void");
    	ByteArrayOutputStream caw = new ByteArrayOutputStream();
    	SMExPrintStream xw = new SMExPrintStream(caw);
    	xw.write(97);
    	xw.write(98);
    	xw.write(99);
    	xw.write(98);
    	xw.write(97);
    	th.check(caw.toString().equals("abcba"), "check if chars are added, got:"+caw.toString());

   th.checkPoint("write(byte[],int,int)void");
	caw = new ByteArrayOutputStream();
    	xw = new SMExPrintStream(caw);
   try {
 	xw.write("".getBytes());
    	th.check("".equals(caw.toString()),"checking write -- 1, got:"+caw.toString());
	xw.write("bcdefg".getBytes());
    	th.check( "bcdefg".equals(caw.toString()),"checking write -- 2, got:"+caw.toString());
	xw.write("abcd".getBytes());
    	th.check( "bcdefgabcd".equals(caw.toString()),"checking write -- 3, got:"+caw.toString());
	xw.write("a".getBytes());
    	th.check( "bcdefgabcda".equals(caw.toString()),"checking write -- 4, got:"+caw.toString());
        }
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 1, got:"+e); }
    try {
    	xw.write(null);
    	th.fail("should throw NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 1, got:"+e); }

   th.checkPoint("write(byte[],int,int)void");
	caw = new ByteArrayOutputStream();
    	xw = new SMExPrintStream(caw);
 	xw.write("".getBytes(),0,0);
    	th.check("".equals(caw.toString()),"checking write -- 1, got:"+caw.toString());
	xw.write("abcdefgh".getBytes(),1,6);
    	th.check( "bcdefg".equals(caw.toString()),"checking write -- 2, got:"+caw.toString());
	xw.write("abcd".getBytes(),4,0);
    	th.check( "bcdefg".equals(caw.toString()),"checking write -- 3, got:"+caw.toString());
	xw.write("a".getBytes(),0,0);
    	th.check( "bcdefg".equals(caw.toString()),"checking write -- 4, got:"+caw.toString());
	xw.write("abcd".getBytes(),2,2);
    	th.check( "bcdefgcd".equals(caw.toString()),"checking write -- 5, got:"+caw.toString());
	xw.write("abcd".getBytes(),0,4);
    	th.check( "bcdefgcdabcd".equals(caw.toString()),"checking write -- 6, got:"+caw.toString());
    try {
    	xw.write(null,4,5);
    	th.fail("should throw NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 1, got:"+e); }
    try {
    	xw.write("abcde".getBytes(),4,2);
    	th.fail("should throw an IndexOutOfBoundsException -- 1");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 2, got:"+e); }
    try {
    	xw.write("abcde".getBytes(),-4,2);
    	th.fail("should throw an IndexOutOfBoundsException -- 2");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 3, got:"+e); }
    try {
    	xw.write("abcde".getBytes(),4,-2);
    	th.fail("should throw an IndexOutOfBoundsException -- 3");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 4, got:"+e); }
    try {
    	xw.write("abcde".getBytes(),6,0);
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
    	SMErrorStream ew = new SMErrorStream();
    	SMExPrintStream xw = new SMExPrintStream(ew);
    	xw.close();
	th.check(xw.checkError() , "checking Error flag set -- close");
    	xw = new SMExPrintStream(ew);
    	xw.flush();
	th.check(xw.checkError() , "checking Error flag set -- flush");
    	xw = new SMExPrintStream(ew);
    	xw.write(1);
	th.check(xw.checkError() , "checking Error flag set -- write 1");
    	xw = new SMExPrintStream(ew);
    	byte [] ca = "abcde".getBytes();
    	xw.write(ca,1,3);
	th.check(xw.checkError() , "checking Error flag set -- write 2");
    	xw = new SMExPrintStream(ew);
	
  }
/**
* implemented. <br>
*
*/
  public void test_setError(){
    th.checkPoint("setError()void");
    	ByteArrayOutputStream caw = new ByteArrayOutputStream();
    	SMExPrintStream xw = new SMExPrintStream(caw);
	xw.setError();
	th.check(xw.checkError() , "checking setError");
  }

/**
* implemented. <br>
*
*/
  public void test_close(){
    th.checkPoint("close()void");
    	SMErrorStream ew = new SMErrorStream(1);
    	SMExPrintStream xw = new SMExPrintStream(ew);
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
    	SMErrorStream ew = new SMErrorStream();
    	SMExPrintStream xw = new SMExPrintStream(ew);
        xw.flush();
        th.check( ew.isFlushed());

  }

}
