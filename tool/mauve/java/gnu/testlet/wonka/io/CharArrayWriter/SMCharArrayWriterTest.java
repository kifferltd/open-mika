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


package gnu.testlet.wonka.io.CharArrayWriter; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.CharArrayWriter   <br>
*
*/
public class SMCharArrayWriterTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.CharArrayWriter");
       test_CharArrayWriter();
       test_write();
       test_close();
       test_flush();
       test_reset();
       test_size();
       test_toCharArray();
       test_toString();
       test_writeTo();
// [CG 20221230 scrap this, it's totally misconceived
//       test_lock();
     }


/**
* implemented. <br>
*
*/
  public void test_CharArrayWriter(){
    th.checkPoint("CharArrayWriter()");

    th.checkPoint("CharArrayWriter(int)");
    try {
    	CharArrayWriter cw = new CharArrayWriter(-1);
        th.fail("should throw an IllegalArgumentExeption");
        }
    catch(IllegalArgumentException ie) {th.check(true);}
  }

/**
* implemented. <br>
*
*/
  public void test_write(){
   th.checkPoint("write(int)void");
    CharArrayWriter cw = new CharArrayWriter();
    cw.write(97);
    cw.write(98);
    cw.write(99);
    cw.write(98);
    cw.write(97);
    th.check(cw.toString().equals("abcba"), "check if chars are added");
   th.checkPoint("write(char[],int,int)void");
    CharArrayWriter xw = new CharArrayWriter();
    try {
 	xw.write("".toCharArray(),0,0);
//th.debug("length buffer  ="+xw.buffer.length());
    	th.check("".equals(xw.toString()),"checking write -- 1, got:"+xw.toString());
	xw.write("abcdefgh".toCharArray(),1,6);
//th.debug("char 0 ="+xw.buffer.charAt(0));
    	th.check( "bcdefg".equals(xw.toString()),"checking write -- 2, got:"+xw.toString());
	xw.write("abcd".toCharArray(),4,0);
    	th.check( "bcdefg".equals(xw.toString()),"checking write -- 3, got:"+xw.toString());
	xw.write("a".toCharArray(),0,0);
    	th.check( "bcdefg".equals(xw.toString()),"checking write -- 4, got:"+xw.toString());
	xw.write("abcd".toCharArray(),2,2);
    	th.check( "bcdefgcd".equals(xw.toString()),"checking write -- 5, got:"+xw.toString());
	xw.write("abcd".toCharArray(),0,4);
    	th.check( "bcdefgcdabcd".equals(xw.toString()),"checking write -- 6, got:"+xw.toString());
        }
    catch(Exception e) { th.fail("shouldn't throw any Exception, got:"+e); }
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

   th.checkPoint("write(java.lang.String,int,int)void");
    xw = new CharArrayWriter();
    try {
 	xw.write("",0,0);
//th.debug("length buffer  ="+xw.buffer.length());
    	th.check("".equals(xw.toString()),"checking write -- 1, got:"+xw.toString());
	xw.write("abcdefgh",1,6);
//th.debug("char 0 ="+xw.buffer.charAt(0));
    	th.check( "bcdefg".equals(xw.toString()),"checking write -- 2, got:"+xw.toString());
	xw.write("abcd",4,0);
    	th.check( "bcdefg".equals(xw.toString()),"checking write -- 3, got:"+xw.toString());
	xw.write("a",0,0);
    	th.check( "bcdefg".equals(xw.toString()),"checking write -- 4, got:"+xw.toString());
	xw.write("abcd",2,2);
    	th.check( "bcdefgcd".equals(xw.toString()),"checking write -- 5, got:"+xw.toString());
	xw.write("abcd",0,4);
    	th.check( "bcdefgcdabcd".equals(xw.toString()),"checking write -- 6, got:"+xw.toString());
        }
    catch(Exception e) { th.fail("shouldn't throw any Exception, got:"+e); }
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
* --> this method does nothing, but  needs to be imlpemented <br>
*     because it is declared abstract in Writer
*/
  public void test_close(){
    th.checkPoint("close()void");
    CharArrayWriter cw = new CharArrayWriter();
    cw.close();
    try { cw.write(97);
    	  th.check("a".equals(cw.toString()));
        }
    catch(Exception e) { th.fail("it is allowed to write in closed CharArrayReader"); }     	
  }

/**
* implemented. <br>
* --> this method does nothing, but  needs to be imlpemented <br>
*     because it is declared abstract in Writer
*/
  public void test_flush(){
    th.checkPoint("flush()void");
    CharArrayWriter cw = new CharArrayWriter();
    try {  cw.write("abcde"); }
    catch(Exception e) { th.fail("should not throw an Exception");}
    cw.flush();
    th.check("abcde".equals(cw.toString()));

  }

/**
* implemented. <br>
*
*/
  public void test_reset(){
    th.checkPoint("reset()void");
    CharArrayWriter cw = new CharArrayWriter();
    try {  cw.write("abcde"); }
    catch(Exception e) { th.fail("should not throw an Exception");}
    cw.reset();
    th.check("".equals(cw.toString()));
    th.check( cw.size()==0 , "size is 0 after reset");
  }

/**
* implemented. <br>
*
*/
  public void test_size(){
    th.checkPoint("size()int");
    CharArrayWriter cw = new CharArrayWriter();
    try {
    	cw.write("abcde");
        th.check( cw.size() == 5 );
    	cw.write("abcde".toCharArray(),2,3);
        th.check( cw.size() == 8 );
    	cw.write(99);
        th.check( cw.size() == 9 );
    	cw.write("abcde",0,5);
        th.check( cw.size() == 14 );
    	cw.write("abcde",4,0);
        th.check( cw.size() == 14 );
    	}
    catch(Exception e) { th.fail("should not throw an Exception");}

  }

/**
* implemented. <br>
*
*/
  public void test_toCharArray(){
    th.checkPoint("toCharArray()char[]");
    CharArrayWriter cw = new CharArrayWriter();
    String s = "abcdefghij\nklm";
    try {
    	cw.write(s);
        th.check(s.equals(new String(cw.toCharArray())));
    	cw.write(s);
        th.check(new String(cw.toCharArray()).equals(s+s));
        cw.reset();
    	th.check("".equals(new String(cw.toCharArray())));
    	}
    catch(Exception e) { th.fail("should not throw an Exception");}
  }

/**
* implemented. <br>
*
*/
  public void test_toString(){
    th.checkPoint("toString()java.lang.String");
    CharArrayWriter cw = new CharArrayWriter();
    String s = "abcdefghij\nklm";
    try {
    	cw.write(s);
        th.check(s.equals(cw.toString()));
    	cw.write(s);
        th.check(cw.toString().equals(s+s));
        cw.reset();
    	th.check("".equals(cw.toString()));
    	}
    catch(Exception e) { th.fail("should not throw an Exception");}

  }

/**
* implemented. <br>
*
*/
  public void test_writeTo(){
    th.checkPoint("writeTo(java.io.Writer)void");
    CharArrayWriter cws = new CharArrayWriter();
    CharArrayWriter cwd = new CharArrayWriter();
    String s = "abcdefghij\nklm";
    try {
        cws.writeTo(cwd);
        th.check(cwd.toString().equals(""));
    	cws.write(s);
        cws.writeTo(cwd);
        th.check(cwd.toString().equals(s));
        cws.writeTo(cwd);
        th.check(cwd.toString().equals(s+s));
  	cws.reset();
        cws.writeTo(cwd);
        th.check(cwd.toString().equals(s+s));
    	}
    catch(Exception e) { th.fail("should not throw an Exception");}
  }

/**
* implemented. <br>
*
*/
  public void test_lock(){
    th.checkPoint("lock(protected)java.lang.Object");
    SMlockCharArrayWriter lt = new SMlockCharArrayWriter();
    CharArrayWriter wr = new CharArrayWriter();

    int i = setupLockThread(lt,wr);
    try { wr.writeTo(new CharArrayWriter()); }
    catch(Exception e) {}
//th.debug("accesed Writer");
    th.check( i+1 == accesed , "accesed xr before lock was released -- writeTo");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    wr = new CharArrayWriter();
    i = setupLockThread(lt,wr);
    wr.toString();
//th.debug("accesed Writer");
    th.check( i+1 == accesed , "accesed xr before lock was released -- toString");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    wr = new CharArrayWriter();
    i = setupLockThread(lt,wr);
    wr.toCharArray();
//th.debug("accesed Writer");
    th.check( i+1 == accesed , "accesed xr before lock was released -- toCharArray");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    wr = new CharArrayWriter();
    i = setupLockThread(lt,wr);
    wr.size();
//th.debug("accesed Writer");
    th.check( i , accesed , "accesed xr before lock was released -- size");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    wr = new CharArrayWriter();
    i = setupLockThread(lt,wr);
    wr.reset();
//th.debug("accesed Writer");
    th.check( i , accesed , "accesed xr before lock was released -- reset");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    wr = new CharArrayWriter();
    i = setupLockThread(lt,wr);
    wr.write(96);
//th.debug("accesed Writer");
    th.check( i+1 == accesed , "accesed xr before lock was released -- write(int)");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    wr = new CharArrayWriter();
    i = setupLockThread(lt,wr);
    wr.write("abcd",1,2);
//th.debug("accesed Writer");
    th.check( i+1 == accesed , "accesed xr before lock was released -- write(String,int,int)");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    wr = new CharArrayWriter();
    i = setupLockThread(lt,wr);
    wr.write(new char[4],1,2);
//th.debug("accesed Writer");
    th.check( i+1 == accesed , "accesed xr before lock was released -- write(char[],int,int)");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");
  }

  protected int accesed=0;

  public void inc() {
  	accesed++;
  }	

  public int setupLockThread(SMlockCharArrayWriter lt,CharArrayWriter xr) {
    f1=false;
    lt.setWriter(xr);
    lt.setTestHarness(th);
    lt.setWT(this);
    t = new Thread(lt);
    t.start();
    while (!f1) {
    	Thread.yield();
    }
    return accesed;
  }

  private Thread t = null;
  private volatile boolean f1=false;

  public void set1(){
  	f1 = true;
  }

}
