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


package gnu.testlet.wonka.io.Reader; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.Reader   <br>
*
*/
public class SMReaderTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.Reader");
       test_Reader();
       test_read();
       test_skip();
       test_mark();
       test_markSupported();
       test_reset();
       test_close();
       test_ready();
//       test_lock();

     }


/**
* implemented. <br>
*
*/
  public void test_Reader(){
    th.checkPoint("Reader()");
    SMExReader xr = new SMExReader();
    th.check( xr.getLock() == xr , "if lock is not specified Reader uses itself");

    th.checkPoint("Reader(java.lang.Object)");
    xr = new SMExReader(this);
    th.check( xr.getLock() == this , "if lock is specified Reader uses it");
    try {
    	new SMExReader(null);
    	th.fail("should throw a NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }
  }

/**
* implemented. <br>
*
*/
  public void test_read(){
   th.checkPoint("read()int");
    SMExReader xr = new SMExReader();
    try {
    	int i= xr.read();	
    	th.check(i == 97 ,"SMExReader always returns 'a' -- 1, got:"+i);	
        i= xr.read();	
    	th.check(i == 97 ,"SMExReader always returns 'a' -- 2, got:"+i);	
    	i= xr.read();	
    	th.check(i == 97 ,"SMExReader always returns 'a' -- 3, got:"+i);	
    	i= xr.read();	
    	th.check(i == -1 ,"SMExReader always returns -1 at EOF, got:"+i);	
        }
    catch (Exception e) { th.fail("should not throw any Exeption, got:"+e);}

   th.checkPoint("read(char[])int");
    char [] buf = new char[6] ;
    for (int j=0 ; j < 6; j++) { buf[j]= 'c';}
    try {
    	int i= xr.read(buf);	
    	th.check(i == -1 ,"SMExReader returns -1 if no chars read, got:"+i);	
	xr = new SMExReader(10);
        i= xr.read(buf);	
    	th.check(i == 6 ,"SMExReader returns nr chars read, got:"+i);	
    	th.check("aaaaaa".equals(new String(buf)) , "make sure all chars are placed");
	for (int k=0 ; k < 6; k++) { buf[k]= 'c';}
        i= xr.read(buf);	
    	th.check(i == 4 ,"SMExReader returns nr chars read, got:"+i);	
    	th.check("aaaacc".equals(new String(buf)) , "make no chars are overwritten");
    	
    	}
    catch (Exception e) { th.fail("should not throw any Exeption, got:"+e);}

   th.checkPoint("read(char[],int,int)int");
    //abstract method
  }

/**
* implemented. <br>
*
*/
  public void test_skip(){
    th.checkPoint("skip(long)long");
    SMExReader xr = new SMExReader(30);
    long l;
    try {
    	l = xr.skip(-5L);
    	th.fail("should throw an illegalArgumentException, but got:"+l);
    	}
    catch (IllegalArgumentException e) { th.check(true);} 	
    catch (Exception e) { th.fail("should not throw any Exeption, got:"+e);}
    try {
        l = xr.skip(20);
        th.check(l == 20 , "skipped 20 chars");
        l = xr.skip(0);
        th.check(l == 0 , "skipped no chars");
        l = xr.skip(20);
        th.check(l == 10 , "skipped 10 chars");
        l = xr.skip(10);
        th.check(l == 0 , "skipped no chars, EOF reached");
    	}    	
    catch (Exception e) { th.fail("should not throw any Exeption, got:"+e);}
  }

/**
* implemented. <br>
*
*/
  public void test_mark(){
    th.checkPoint("mark(int)void");
    SMExReader xr = new SMExReader();
    try { xr.mark(-3);
          th.fail("should throw an IOException -- 1");
    	}
    catch (IOException ioe) { th.check(true); }
    catch (Exception e) { th.fail("should not throw any Exeption -- 1, got:"+e);}

    try { xr.mark(3);
          th.fail("should throw an IOException -- 2");
    	}
    catch (IOException ioe) { th.check(true); }
    catch (Exception e) { th.fail("should not throw any Exeption -- 2, got:"+e);}
  }

/**
* implemented. <br>
*
*/
  public void test_markSupported(){
    th.checkPoint("markSupported()boolean");
    SMExReader xr = new SMExReader();
    try { th.check(!xr.markSupported() , "always returns false -- 1");
    	  th.check(!xr.markSupported() , "always returns false -- 2");
    	}
    catch (Exception e) { th.fail("should not throw any Exeption -- 1, got:"+e);}

  }

/**
* implemented. <br>
*
*/
  public void test_reset(){
    th.checkPoint("reset()void");
    SMExReader xr = new SMExReader();
    try { xr.reset();
          th.fail("should throw an IOException -- 1");
    	}
    catch (IOException ioe) { th.check(true); }
    catch (Exception e) { th.fail("should not throw any Exeption -- 1, got:"+e);}

    try { xr.reset();
          th.fail("should throw an IOException -- 2");
    	}
    catch (IOException ioe) { th.check(true); }
    catch (Exception e) { th.fail("should not throw any Exeption -- 2, got:"+e);}

  }

/**
*   not implemented. <br>
*   abstract method
*/
  public void test_close(){
    th.checkPoint("close()void");

  }

/**
* implemented. <br>
*
*/
  public void test_ready(){
    th.checkPoint("ready()boolean");
    SMExReader xr = new SMExReader();
    try { th.check(!xr.ready() , "always returns false -- 1");
    	  th.check(!xr.ready() , "always returns false -- 2");
    	}
    catch (Exception e) { th.fail("should not throw any Exeption -- 1, got:"+e);}

  }

/**
* implemented. <br>
* we test if the lock is used to synchronize all methods
*/
  public void test_lock(){
    th.checkPoint("behauvior_of_lock()member");
    SMExReader xr = new SMExReader(new Object());
    SMlockTC lt = new SMlockTC();
    int i = setupLockThread(lt,xr);
    try { xr.mark(2); }
    catch(Exception e) {
//th.debug("marked Reader");
    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- mark");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    lt = new SMlockTC();
    i = setupLockThread(lt,xr);
    try { xr.reset(); }
    catch(Exception e) {
//th.debug("reseted Reader");
    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- reset");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    lt = new SMlockTC();
    i = setupLockThread(lt,xr);
    try { xr.read();
//th.debug("read from Reader");
	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- read()");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    lt = new SMlockTC();
    i = setupLockThread(lt,xr);
    try { xr.read(new char[2]);
//th.debug("read from Reader");
	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- read(char[])");    	
    try { t.join(1000L); }
    catch(Exception e) {}
//th.debug("next test ...");

    lt = new SMlockTC();
    i = setupLockThread(lt,xr);
    try { xr.skip(3);
//th.debug("skipped chars Reader");
	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- skip");    	
    try { t.join(1000L);}
    catch(Exception e) {}
  }    	

  protected int accesed=0;

  public int setupLockThread(SMlockTC lt,SMExReader xr) {
    f1=false;
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
  public void inc() {
  	accesed++;		
  }

  private Thread t = null;
  private volatile boolean f1=false;

  public void set1(){
  	f1 = true;
  }



}
