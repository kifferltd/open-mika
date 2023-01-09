/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
* Small modifications by Chris Gray 2011.                                 *
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


package gnu.testlet.wonka.io.BufferedWriter; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.BufferedWriter   <br>
* <br>
*  This class has a private flag which allows test to be used on JDK <br>
*  so we can debug our tests !
*/
public class SMBufferedWriterTest implements Testlet {
  protected TestHarness th;

  public void test (TestHarness harness) {
       th = harness;
       th.setclass("java.io.BufferedWriter");
       test_BufferedWriter();
       test_newLine();
       test_write();
       test_close();
       test_flush();
// [CG 20221230 scrap this, it's totally misconceived
//       test_lock();
     }

/**
* implemented. <br>
*
*/
  public void test_BufferedWriter(){
    th.checkPoint("BufferedWriter(java.io.Writer)");
    CharArrayWriter caw = new CharArrayWriter();
    try {
        new BufferedWriter(null);
        th.fail("should throw NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }
    catch(Exception e){ th.fail("got unwanted exception -- 2, got:"+e);}	

    th.checkPoint("BufferedWriter(java.io.Writer,int)");
    try {
        new BufferedWriter(null,8);
        th.fail("should throw NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }
    catch(Exception e){ th.fail("got unwanted exception -- 2, got:"+e);}	
    try {
        new BufferedWriter(caw,-1);
        th.fail("should throw IllegalArgumentException -- 1");
    	}
    catch(IllegalArgumentException ie) { th.check(true); }
    catch(Exception e){ th.fail("got unwanted exception -- 2, got:"+e);}	
    try {
        new BufferedWriter(caw,0);
        th.fail("should throw IllegalArgumentException -- 2");
    	}
    catch(IllegalArgumentException ie) { th.check(true); }
    catch(Exception e){ th.fail("got unwanted exception -- 3, got:"+e);}	
  }

/**
* implemented. <br>
* add test when we can change the system.props easely
*/
  public void test_newLine(){
    th.checkPoint("newLine()void");
    CharArrayWriter caw = new CharArrayWriter();
    BufferedWriter bw = new BufferedWriter(caw,10);
    String s = System.getProperty("line.separator");
    try {
    	bw.newLine();
    	bw.newLine();
    	bw.write('a');
    	bw.flush();
    	th.check(caw.toString() ,s+s+'a' ,"checking lineseparator");
    }
    catch(Exception e){ th.fail("got unwanted exception -- 1, got:"+e);}	


  }

/**
* implemented. <br>
*
*/
  public void test_write(){
   th.checkPoint("write(int)void");
    CharArrayWriter caw = new CharArrayWriter();
    BufferedWriter bw = new BufferedWriter(caw,4);
    try {
    	bw.write(97);
    	bw.write(98);
    	bw.write(99);
    	bw.write(100);
    	th.check(caw.toString(),"","nothing written to underlying writer");
    	bw.write(101);
    	th.check(caw.toString().equals("abcd"),"there should be written to underlying writer -- 1, got:"+caw);
    	bw.write(102);
    	th.check(caw.toString().equals("abcd"),"there should be written to underlying writer -- 2, got:"+caw); 	
  	}
    catch(Exception e){ th.fail("got unwanted exception -- 1, got:"+e);}	

   th.checkPoint("write(char[],int,int)void");
    CharArrayWriter xw = new CharArrayWriter();
    bw = new BufferedWriter(xw,12);
    try {
   	bw.write(new char[0],0,0);
  	bw.write("abcdefgh".toCharArray(),1,6);
  	bw.write("abcd".toCharArray(),4,0);
  	bw.write("a".toCharArray(),0,0);
  	bw.write("abcd".toCharArray(),2,2);
    th.check( "".equals(xw.toString()) , "nothing is written to xw yet, got:"+xw);
  	bw.write("abcd".toCharArray(),0,4);
        th.check( "bcdefgcdabcd".equals(xw.toString()) , "first time something is written to xw, got:"+xw);
	bw.write("abcdefgh".toCharArray(),1,7);
	bw.write("abcdefgh".toCharArray(),1,7);
        th.check( "bcdefgcdabcdbcdefghbcdef".equals(xw.toString()) , "first time something is written to xw, got:"+xw);
	bw.write("smartmovetests".toCharArray());
        th.check( "bcdefgcdabcdbcdefghbcdefghsmartmovetests".equals(xw.toString()) , "first time something is written to xw, got:"+xw);
	bw.write("smartmovetests".toCharArray());
        th.check( "bcdefgcdabcdbcdefghbcdefghsmartmovetestssmartmovetests".equals(xw.toString()) , "first time something is written to xw, got:"+xw);
 	bw.write("abcdefghij".toCharArray());

        }
    catch(Exception e) { th.fail("shouldn't throw any Exception, got:"+e); }
    try { char []ca=null;
    	bw.write(ca,4,5);
    	th.fail("should throw NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 1, got:"+e); }
    try {
    	bw.write("abcde".toCharArray(),4,2);
    	th.fail("should throw an IndexOutOfBoundsException -- 1");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 2, got:"+e); }
    try {
    	bw.write("abcde".toCharArray(),-4,2);
    	th.fail("should throw an IndexOutOfBoundsException -- 2");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 3, got:"+e); }
    try {
    	bw.write("abcde".toCharArray(),4,-2);
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 4, got:"+e); }
    try {
    	bw.write("abcde".toCharArray(),6,0);
    	th.fail("should throw an IndexOutOfBoundsException -- 4");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 5, got:"+e); }
    try {
        th.check( "bcdefgcdabcdbcdefghbcdefghsmartmovetestssmartmovetests".equals(xw.toString()) , "first time something is written to xw, got:"+xw);
	}
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 6, got:"+e); }

   th.checkPoint("write(java.lang.String,int,int)void");
    xw = new CharArrayWriter();
    bw = new BufferedWriter(xw,12);
    try {
 	bw.write("",0,0);
	bw.write("abcdefgh",1,6);
	bw.write("abcd",4,0);
	bw.write("a",0,0);
	bw.write("abcd",2,2);
        th.check( "".equals(xw.toString()) , "nothing is written to xw yet, got:"+xw);
	bw.write("abcd",0,4);
        th.check( "bcdefgcdabcd".equals(xw.toString()) , "first time something is written to xw, got:"+xw);
	bw.write("abcdefgh",1,7);
	bw.write("abcdefgh",1,7);
        th.check( "bcdefgcdabcdbcdefghbcdef".equals(xw.toString()) , "checking contents of xw -- 1, got:"+xw);
	bw.write("smartmovetests");
        th.check( "bcdefgcdabcdbcdefghbcdefghsmartmovet".equals(xw.toString()) , "checking contents of xw -- 2, got:"+xw);
	bw.write("smartmovetests");
        th.check( "bcdefgcdabcdbcdefghbcdefghsmartmovetestssmartmov".equals(xw.toString()) , "first time something is written to xw, got:"+xw);
	bw.write("abcd");
        }
    catch(Exception e) { th.fail("shouldn't throw any Exception, got:"+e); }
    try { String s = null ;
    	bw.write(s,4,5);
    	th.fail("should throw NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 1, got:"+e); }
    try {
    	bw.write("abcde",4,2);
    	th.fail("should throw an IndexOutOfBoundsException -- 1");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 2, got:"+e); }
    try {
    	bw.write("abcde",-4,2);
    	th.fail("should throw an IndexOutOfBoundsException -- 2");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 3, got:"+e); }
    try {
    	bw.write("abcde",4,-2);
    	}
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 4, got:"+e); }
    try {
    	bw.write("abcde",5,1);
    	th.fail("should throw an IndexOutOfBoundsException -- 4");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 5, got:"+e); }
    try {
        th.check( "bcdefgcdabcdbcdefghbcdefghsmartmovetestssmartmov".equals(xw.toString()) ,
        "first time something is written to xw, got:"+xw);
	}
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 6, got:"+e); }
  }

/**
* implemented. <br>
*
*/
  public void test_close(){
    th.checkPoint("close()void");
    SMExWriter xw = new SMExWriter();
    BufferedWriter bw = new BufferedWriter(xw,40);
    try {
    	bw.close();
    	th.check(xw.isClosed() , "close must close underlying writers");
    	th.check(!xw.isFlushed() , "close must close underlying writers--flushed");
    	bw.close();
    	bw.close();
	}
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 1, got:"+e); }
    try {
    	bw.flush();
    	th.fail("should throw an IOException after writer is closed -- flush");
	}
    catch(IOException ioe){ th.check(true); }
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 2, got:"+e); }
    try {
    	bw.newLine();
    	th.fail("should throw an IOException after writer is closed -- newLine");
	}
    catch(IOException ioe){ th.check(true); }
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 2, got:"+e); }
    try {
    	bw.write(97);
    	th.fail("should throw an IOException after writer is closed -- write 1");
	}
    catch(IOException ioe){ th.check(true); }
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 2, got:"+e); }
    try {
    	bw.write("sdsd".toCharArray(),1,2);
    	th.fail("should throw an IOException after writer is closed -- write 2");
	}
    catch(IOException ioe){ th.check(true); }
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 2, got:"+e); }
    try {
    	bw.write("agfh",1,2);
    	th.fail("should throw an IOException after writer is closed -- write 3");
	}
    catch(IOException ioe){ th.check(true); }
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 2, got:"+e); }
    	
  }

/**
* implemented. <br>
*
*/
  public void test_flush(){
    th.checkPoint("flush()void");
    SMExWriter xw = new SMExWriter();
    BufferedWriter bw = new BufferedWriter(xw,40);
    try {
    	bw.write(97);
    	bw.flush();
    	th.check(xw.isFlushed() , "flush must flush underlying writers");
    	th.check(xw.toString().equals("a") ,"buf must be written before flush");
	}
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 1, got:"+e); }

  }

/**
* implemented. <br>
*
*/
  public void test_lock(){
    th.checkPoint("lock(protected)java.lang.Object");
    SMlockBufferedWriter lt = new SMlockBufferedWriter();
    CharArrayWriter wr = new CharArrayWriter();
    BufferedWriter bw = new BufferedWriter(wr,40);

    int i = setupLockThread(lt,wr);
    try { bw.write(97); }
    catch(Exception e) {th.fail("should not throw an exception -- 1, got:"+e);}
//th.debug("accesed Writer");
    th.check( i+1 == accesed , "accesed xr before lock was released -- write 1");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    lt = new SMlockBufferedWriter();
    i = setupLockThread(lt,wr);
    try { bw.write("abcd".toCharArray(),1,2); }
    catch(Exception e) {th.fail("should not throw an exception -- 2, got:"+e);}
//th.debug("accesed Writer");
    th.check( i+1 == accesed , "accesed xr before lock was released -- write 2");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    lt = new SMlockBufferedWriter();
    i = setupLockThread(lt,wr);
    try { bw.write("abcd",1,2); }
    catch(Exception e) {th.fail("should not throw an exception -- 3, got:"+e);}
//th.debug("accesed Writer");
    th.check( i+1 == accesed , "accesed xr before lock was released -- write 3");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    lt = new SMlockBufferedWriter();
    i = setupLockThread(lt,wr);
    try { bw.newLine(); }
    catch(Exception e) {th.fail("should not throw an exception -- 4, got:"+e);}
//th.debug("accesed Writer");
    th.check( i+1 == accesed , "accesed xr before lock was released -- newLine");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    lt = new SMlockBufferedWriter();
    i = setupLockThread(lt,wr);
    try { bw.flush(); }
    catch(Exception e) {th.fail("should not throw an exception -- 5, got:"+e);}
//th.debug("accesed Writer");
    th.check( i+1 == accesed , "accesed xr before lock was released -- flush");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    lt = new SMlockBufferedWriter();
    i = setupLockThread(lt,wr);
    try { bw.close(); }
    catch(Exception e) {th.fail("should not throw an exception -- 6, got:"+e);}
//th.debug("accesed Writer");
    th.check( i+1 == accesed , "accesed xr before lock was released -- close");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");
  }

  protected int accesed=0;
  private Thread t = null;
  private volatile boolean f1=false;

  public void set1(){
  	f1 = true;
  }

  public void inc() {
  	accesed++;
  }	

  public int setupLockThread(SMlockBufferedWriter lt,CharArrayWriter xr) {
    f1 = false;
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

}
