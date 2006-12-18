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


package gnu.testlet.wonka.io.CharArrayReader; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.CharArrayReader    <br>
*
*/
public class SMCharArrayReaderTest implements Testlet
{
  protected TestHarness th;
  protected int accesed=0;
  protected char []ca = "abcdefg\nhijklmnop\nqrstuvwxyz\n".toCharArray();

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.CharArrayReader");
       test_CharArrayReader();
       test_mark();
       test_markSupported();
       test_reset();
       test_read();
       test_skip();
       test_close();
       test_ready();
       test_lock();
     }

     public void inc() {
        accesed++;
     }

/**
* implemented. <br>
*
*/
  public void test_CharArrayReader(){
    th.checkPoint("CharArrayReader(char[])");
    SMExCharArrayReader xar = new SMExCharArrayReader(ca);
    th.check( xar.getcount() == ca.length , "all chars are used -- 1" );
    th.check( xar.getbuf() == ca , "reference is used, not a copy -- 1");
    th.check( xar.getpos() == 0 , "start reading at pos 0 -- 1");
    th.check( xar.getmpos() == 0 , "markedPos is 0 -- 1");
    try { new SMExCharArrayReader(null);
    	  th.fail("should throw a NullPointerException");
        }
    catch (NullPointerException ne){th.check(true);}	
    char [] ca1 = new char[0] ;
    xar = new SMExCharArrayReader(ca1);
    th.check( xar.getcount() == ca1.length , "all chars are used -- 2" );
    th.check( xar.getbuf() == ca1 , "reference is used, not a copy -- 2");
    th.check( xar.getpos() == 0 , "start reading at pos 0 -- 2");
    th.check( xar.getmpos() == 0 , "markedPos is 0 -- 2");
    	
    th.checkPoint("CharArrayReader(char[],int,int)");
    xar = new SMExCharArrayReader(ca,10,10);
    th.check( xar.getcount() == 20 , "this.count = offset+count -- 1" );
    th.check( xar.getbuf() == ca , "reference is used, not a copy -- 1");
    th.check( xar.getpos() == 10 , "start reading at pos 10 -- 1");
    th.check( xar.getmpos() == 10 , "markedPos is 10 -- 1");
    xar = new SMExCharArrayReader(ca,10,100);
    th.check( xar.getcount() == ca.length , "this.count = offset+count -- 2" );
    th.check( xar.getbuf() == ca , "reference is used, not a copy -- 2");
    th.check( xar.getpos() == 10 , "start reading at pos 10 -- 2");
    th.check( xar.getmpos() == 10 , "markedPos is 10 -- 2");
    try {    xar = new SMExCharArrayReader(ca,100,100);
             th.fail("should throw an ?????-Exception");	
  	}
    catch(IllegalArgumentException e)
    { 	th.check(true);
    }
    try {    xar = new SMExCharArrayReader(ca,-1,100);
             th.fail("should throw an ?????-Exception");	
  	}
    catch(IllegalArgumentException e)
    { 	th.check(true);
//th.debug("constructor threw:"+e+" -- 2");			
    }
    try {    xar = new SMExCharArrayReader(ca,1,-10);
             th.fail("should throw an ?????-Exception");	
  	}
    catch(IllegalArgumentException e)
    { 	th.check(true);
//th.debug("constructor threw:"+e+" -- 3");			
    }
    try { new SMExCharArrayReader(null,1,2);
    	  th.fail("should throw a NullPointerException");
        }
    catch (NullPointerException ne){th.check(true);}	

   }

/**
* implemented. <br>
* mark ignores the argument readaheadLimit(int-value)
*/
  public void test_mark(){
    th.checkPoint("mark(int)void");
    SMExCharArrayReader xar = new SMExCharArrayReader(ca);
    try {
    	xar.skip(5L);
    	xar.mark(123456);
    	th.check( xar.getpos() == xar.getmpos(), "pos == markedPos after a mark");
        th.check( xar.getmpos() == 5 , "makerdPos should be 5");
        xar.read();
    	xar.mark(-123456);
        th.check( xar.getmpos() == 6 , "makerdPos should be 6");
        }
     catch(Exception e) { th.fail("got unwanted exception: "+e); }

  }

/**
* implemented. <br>
* this method should always return true !!!
*/
  public void test_markSupported(){
    th.checkPoint("markSupported()boolean");
    SMExCharArrayReader xar = new SMExCharArrayReader(ca);
    th.check( xar.markSupported() , "always returns true");
  }

/**
* implemented. <br>
*
*/
  public void test_reset(){
    th.checkPoint("reset()void");
    SMExCharArrayReader xar = new SMExCharArrayReader(ca);
    try {
    	xar.skip(5L);
        xar.reset();
        th.check( xar.getpos() == 0 , "pos should be 0");
    	xar.skip(5L);
    	xar.mark(123456);
    	xar.skip(15L);
        xar.reset();
        th.check( xar.getpos() == 5 , "pos should be 5");
        }
     catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 1"); }

    xar = new SMExCharArrayReader(ca,13,33);
    try {
    	xar.skip(5L);
        xar.reset();
        th.check( xar.getpos() == 13 , "pos should be 13");
    	xar.skip(5L);
    	xar.mark(123456);
    	xar.skip(15L);
        xar.reset();
        th.check( xar.getpos() == 18 , "pos should be 18");
        }
     catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 1"); }
  }

/**
* implemented. <br>
*
*/
  public void test_read(){
    th.checkPoint("read()int");
    SMExCharArrayReader xar = new SMExCharArrayReader(ca);
    try {
    	char c = (char) xar.read();
        th.check( xar.getpos() == 1 , "pos should be 1");
        th.check( c == 'a', "check char value got:"+c);
     	c = (char) xar.read();
        th.check( xar.getpos() == 2 , "pos should be 1");
        th.check( c == 'b', "check char value got:"+c);
     	xar.skip(5);
     	c = (char) xar.read();
        th.check( c == '\n', "check char value got:"+c);
     	xar.skip(50);
     	int i = xar.read();
        th.check( i == -1, "check char value got:"+i);     	
     }
     catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 1"); }

    th.checkPoint("read(char[],int,int)int");
    xar = new SMExCharArrayReader(ca);
    char[] ca1 = new char[15];
    try {
    	int i = xar.read(ca1, 0,15);
    	th.check( i == 15 , "15 chars should be read, got:"+i );
        th.check( xar.getpos() == 15 , "pos should be 15, got:"+xar.getpos());
        th.check( "abcdefg\nhijklmn".equals(new String(ca1)), "checking contents of char[] -- 1 :"+new String(ca1));
    	i = xar.read(ca1, 5,5);
        th.check( xar.getpos() == 20 , "pos should be 20, got:"+xar.getpos());
        th.check( "abcdeop\nqrjklmn".equals(new String(ca1)), "checking contents of char[] -- 2 :"+new String(ca1));
    	th.check( i == 5 , "5 chars should be read, got:"+i );
    	i = xar.read(ca1, 2,13);
    	th.check( xar.getpos() == ca.length , "pos should be 29, got:"+xar.getpos());
        th.check( "abstuvwxyz\nklmn".equals(new String(ca1)), "checking contents of char[] -- 3 :"+new String(ca1));
    	th.check( i == 9 , "9 chars should be read, got:"+i );
    	i = xar.read(ca1, 2,5);
    	th.check( xar.getpos() == ca.length , "pos should be 29, got:"+xar.getpos());
        th.check( "abstuvwxyz\nklmn".equals(new String(ca1)), "checking contents of char[] -- 4 :"+new String(ca1));
    	th.check( i == -1 , "no chars should be read, got:"+i );
        xar.reset();
        }
    catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 1"); }
    try {	xar.read(null, 0,15);
                th.fail("should throw a NullPointerException");
        }
    catch(NullPointerException ne) { th.check(true); }
    catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 2"); }
    try {	xar.read(ca1, 16,0);
                th.fail("should throw a IndexOutOfBoundsException -- 1");
        }
    catch(IndexOutOfBoundsException ne) { th.check(true); }
    catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 3"); }
    try {	xar.read(ca1, -1,5);
                th.fail("should throw a IndexOutOfBoundsException -- 2");
        }
    catch(IndexOutOfBoundsException ne) { th.check(true); }
    catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 4"); }
    try {	xar.read(ca1, 1,-5);
                th.fail("should throw a IndexOutOfBoundsException -- 3");
        }
    catch(IndexOutOfBoundsException ne) { th.check(true); }
    catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 5"); }
    try {	xar.read(ca1, 10,6);
                th.fail("should throw a IndexOutOfBoundsException -- 4");
        }
    catch(IndexOutOfBoundsException ne) { th.check(true); }
    catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 6"); }

  }

/**
* implemented. <br>
*
*/
  public void test_skip(){
    th.checkPoint("skip(long)long");
        SMExCharArrayReader xar = new SMExCharArrayReader(ca);
    try {
    	long l = xar.skip(5L);
        th.check( xar.getpos() == 5 , "pos should be 5");
    	th.check(l == 5L , "l should be 5 -- 1");
    	l = xar.skip(5L);
        th.check( xar.getpos() == 10 , "pos should be 10");
    	th.check(l == 5L , "l should be 5 -- 2");
    	l = xar.skip(15L);
        th.check( xar.getpos() == 25 , "pos should be 25");
    	th.check(l == 15L , "l should be 15");
    	l = xar.skip(15L);
        th.check( xar.getpos() == 29 , "pos should be 29 -- 1");
    	th.check(l == 4L , "l should be 4");
    	l = xar.skip(15L);
        th.check( xar.getpos() == 29 , "pos should be 29 -- 2");
    	th.check(l == 0L , "l should be 0 -- 1");
    	xar.reset();
    	l = xar.skip(-15L);
    	th.check(l == 0L , "l should be 0 -- 2");
        }
     catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 1"); }

  }

/**
* implemented. <br>
* once closed everything should throw an IOException !!! <br>
* except another close
*/
  public void test_close(){
    th.checkPoint("close()void");
    SMExCharArrayReader xar = new SMExCharArrayReader(ca);
    try {
        xar.close();
        xar.close();
        xar.close();
        xar.close();
        th.check(true);
        th.check( xar.getbuf() == null, "make sure resources are released");
        }
    catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 1"); }
    try { xar.read();
    	  th.fail("should throw IOException after close -- read() 1");
        }
    catch(IOException ioe){th.check(true);}
    try { xar.read(new char[6],1,3);
    	  th.fail("should throw IOException after close -- read() 2");
        }
    catch(IOException ioe){th.check(true);}
    try { xar.skip(3L);
    	  th.fail("should throw IOException after close -- skip()");
        }
    catch(IOException ioe){th.check(true);}
    try { xar.mark(23);
    	  th.fail("should throw IOException after close -- mark()");
        }
    catch(IOException ioe){th.check(true);}
    try { xar.reset();
    	  th.fail("should throw IOException after close -- reset()");
        }
    catch(IOException ioe){th.check(true);}
    try { xar.ready();
    	  th.fail("should throw IOException after close -- ready()");
        }
    catch(IOException ioe){th.check(true);}




  }

/**
* implemented. <br>
* always returns true !
*/
  public void test_ready(){
    th.checkPoint("ready()boolean");
    SMExCharArrayReader xar = new SMExCharArrayReader(ca);
    try {
        th.check(xar.ready(), "returns true -- 1");
        xar.skip(45L);
        th.check(!xar.ready(), "returns false EOF reached -- 2");
        }
     catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 1"); }
  }

/**
* implemented. <br>
* --> methods are synchronized !!!
*/
  public void test_lock(){
    th.checkPoint("lock(protected)java.lang.Object");
    SMlockCharArrayReader lt = new SMlockCharArrayReader();
    SMExCharArrayReader xr = new SMExCharArrayReader(ca);
    int i = setupLockThread(lt,xr);
    try { xr.mark(2);
//th.debug("marked Reader");
     	}
    catch(Exception e) {     }
    th.check( i+1 == accesed , "accesed xr before lock was released -- mark");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    lt = new SMlockCharArrayReader();
    i = setupLockThread(lt,xr);
    try { xr.reset();
//th.debug("reseted Reader");
    	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- reset");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    lt = new SMlockCharArrayReader();
    i = setupLockThread(lt,xr);
    try { xr.read();
//th.debug("read from Reader");
	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- read()");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    lt = new SMlockCharArrayReader();
    i = setupLockThread(lt,xr);
    try { xr.read(new char[2]);
//th.debug("read from Reader");
	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- read(char[])");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");
    lt = new SMlockCharArrayReader();
    i = setupLockThread(lt,xr);
    try { xr.read(new char[2],0,1);
//th.debug("read from Reader");
	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- read(char[],int,int)");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    lt = new SMlockCharArrayReader();
    i = setupLockThread(lt,xr);
    try { xr.ready();
//th.debug("got ready boolean Reader");
	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- ready");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    lt = new SMlockCharArrayReader();
    i = setupLockThread(lt,xr);
    try { xr.skip(30L);
//th.debug("skipped chars Reader");
	}
    catch(Exception e) {   }
    th.check( i+1 == accesed , "accesed xr before lock was released -- skip");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

      lt = new SMlockCharArrayReader();
    i = setupLockThread(lt,xr);
    try { xr.close();
//th.debug("closed the Reader");
	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- close");    	
    try { t.join(1000L); }
    catch(Exception e) {}
  }    	


  public int setupLockThread(SMlockCharArrayReader lt,SMExCharArrayReader xr) {

    f1 = false;
    lt.setXReader(xr);
    lt.setTestHarness(th);
    lt.setRT(this);
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
