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


package gnu.testlet.wonka.lang.Thread; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

//import java.*; // at least the class you are testing ...

/**
*  Written by ACUNIA. <br>
*                        <br>
*  this file contains test for Thread   <br>
*
*/
public class AcuniaThreadTest implements Testlet, Runnable {

  protected final static int SLEEPY = 0;
  protected final static int NOOP   = 1;
  protected final static int POL1   = 2;
  protected final static int POL2   = 3;
  protected final static int ALIVE  = 4;
  protected final static int PRESLP = 5;

  private boolean interrupted;
  private int time;

  protected TestHarness th;

  protected void setInterruptRcv(){
  	interrupted = true;
  }
  protected boolean getInterruptRcv(){
  	return interrupted;
  }

  public AcuniaThreadTest(){}

  private AcuniaThreadTest(int time, TestHarness th){
    this.time = time;
    this.th = th;
  }

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.lang.Thread");
    test_interrupt();
    test_join();
    test_isAlive();
    test_start();
    test_holdsLock();
  }

  public void run(){
    if(time > 0){
      Thread t = new Thread(new AcuniaThreadTest(-1, th), "SLEEPER");
      t.start();
      try {
        Thread.sleep(time);
        t.interrupt();
        t.join();
        th.check(true, "thread.join succeeded");
      }
      catch(InterruptedException ie){
        th.fail("Thread shouldn't have been interrupted");
      }
    }
    else {
      try {
        Thread.sleep(5000);
        th.fail("Thread should have been interrupted");
      }
      catch(InterruptedException ie){
        th.check(true, "thread was correctly interrupted");
      }
    }
  }




/**
* implemented. <br>
*
*/
  public void test_interrupt(){
    th.checkPoint("interrupt()void");
    Thread t = new Thread(new Runner(SLEEPY, this), "Sleepy");
    interrupted=false;
    t.interrupt();
    t.start();
    Thread.yield();
    t.interrupt();
    while(!interrupted){
     	Thread.yield();
    }
    t = new Thread(new Runner(POL1, this), "Pol1");
    interrupted=false;
    t.start();
    Thread.yield();
    t.interrupt();
    while(!interrupted){
     	Thread.yield();
    }
    t = new Thread(new Runner(POL2, this), "Pol2");
    interrupted=false;
    t.start();
    Thread.yield();
    t.interrupt();
    while(!interrupted){
     	Thread.yield();
    }

    t = new Thread(new Runner(PRESLP, this), "Pre-sleep");
    interrupted=false;
    t.start();
    while(!interrupted){
    	Thread.yield();
    }
    t.interrupt();
    interrupted=false;
    while(!interrupted){
     	Thread.yield();
    }

  }

/**
* implemented. <br>
*
*/
  public void test_join(){
    th.checkPoint("join()void");
    Thread t = new Thread(new Runner(NOOP, this), "No-op");
    interrupted=false;
    th.debug("joining thread which is not started");
    try {
     	long time = System.currentTimeMillis();
     	t.join(3000);
     	th.check((System.currentTimeMillis() - time) < 400);
    }catch (Exception e){
           th.fail("should not throw an Exception");
           e.printStackTrace();
    }
    t.start();
    try {
     	t.join(3000);
     	long time = System.currentTimeMillis();
     	t.join(3000);
     	th.check((System.currentTimeMillis() - time) < 400);
    }catch(InterruptedException ie){}

    t = new Thread(new AcuniaThreadTest(500,th), "KILLER");
    t.start();
    long stime = System.currentTimeMillis();
    try {
      t.join(5000);
      th.check(System.currentTimeMillis() - stime < 4000 , "checking join");
    }
    catch(InterruptedException ie){
      th.fail("no InterruptedException expected");
    }
  }

/**
* implemented. <br>
*
*/
  public void test_isAlive(){
    th.checkPoint("isAlive()boolean");
    Thread t = new Thread(new Runner(ALIVE, this), "Lively");
    th.check(!t.isAlive(), "thread is not started");
    interrupted=false;
    t.start();
    th.check(t.isAlive(), "thread is started -- 1");
    Thread.yield();
    while(!interrupted){
     	Thread.yield();
    }
    th.check(t.isAlive(), "thread is started -- 2");
    t.interrupt();
    try {
     	t.join(1500);
    }catch(InterruptedException ie){}
    th.check(!t.isAlive(), "thread has died");

  }

/**
*   not implemented. <br>
*
*/
  public void test_start(){
    th.checkPoint("start()void");
    Thread t = new Thread(new Runner(ALIVE, this), "Lively");
    interrupted=false;
    t.start();
    while(!interrupted){
    	Thread.yield();
    }
    try {
        t.start();
        th.fail("should throw an IllegalThreadStateException");
    }
    catch(IllegalThreadStateException bad){ th.check(true); }
    t.interrupt();
    while(t.isAlive()){
    	Thread.yield();
    }
    try {
        t.start();
        th.check(true);
    }
    catch(IllegalThreadStateException bad){
    	th.fail("should not throw an IllegalThreadStateException");
    }
  }

/**
*   Added by Chris Gray 20060325. New in 1.4.
*/
  public void test_holdsLock(){
    Object o = null;
    try {
        Thread.holdsLock(o);
        th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe){ th.check(true); }

    o = new Object();

    th.check(!Thread.holdsLock(o), "Thread.holdsLock() on a not-owned object must return false");
    synchronized(o) {
      th.check(Thread.holdsLock(o), "Thread.holdsLock() on an owned object must return true");
    }
    th.check(!Thread.holdsLock(o), "Thread.holdsLock() on a no-longer-owned object must return false");
  }

  public static class Runner implements Runnable{
   	
  	private AcuniaThreadTest att;
  	private int mode;
   	private Thread thd;
   		
   	public Runner(int m,AcuniaThreadTest t){
   		mode = m;
   		att = t;   	
   	}
   	public Runner(int m,AcuniaThreadTest t, Thread th){
   		mode = m;
   		att = t;   	
   		thd =th;
   	}
   	
   	public void run(){
   	  att.th.debug("running in mode "+mode); 	
   	  switch(mode){
   		case AcuniaThreadTest.SLEEPY:
   			att.th.debug("running thread in sleepy mode");
   			try { Thread.sleep(600000L); }
   			catch (InterruptedException ie){
   			   	att.th.check(!Thread.currentThread().isInterrupted());
   			   	att.th.check(!Thread.interrupted());
   			   	att.setInterruptRcv();
   			}
   			att.th.debug("ran thread in sleepy mode");
   			break;
   			
   		case AcuniaThreadTest.POL1:
                	while(!Thread.interrupted()){
                		Thread.yield();
                	}
		   	att.th.check(!Thread.currentThread().isInterrupted());
		   	att.th.check(!Thread.interrupted());
		   	att.setInterruptRcv();          	   		
   			break;   				
   			
   		case AcuniaThreadTest.POL2:
                	while(!Thread.currentThread().isInterrupted()){
                		Thread.yield();
                	}
		   	att.th.check(Thread.currentThread().isInterrupted());
		   	att.th.check(Thread.interrupted());
		   	att.setInterruptRcv();          	   		
   			break;   				
   			
   		case AcuniaThreadTest.ALIVE:
		   	att.setInterruptRcv();          	   		
   			try { Thread.sleep(600000L); }
   			catch (InterruptedException ie){}
   			break;
   		
   		case AcuniaThreadTest.PRESLP:
		   	att.setInterruptRcv();          	   		
		   	while (att.getInterruptRcv()){
		   		Thread.yield();          	   		
		   	}
                        long time = System.currentTimeMillis();
   			try {
   				Thread.sleep(6000L);
   				att.th.fail("should be interrupted");
   				att.setInterruptRcv();          	   		
   			}
   			catch (InterruptedException ie){
				att.th.check((System.currentTimeMillis() - time) < 1000, "took too long");
   				att.setInterruptRcv();          	   		
   			}
   			break;
   		
   		case AcuniaThreadTest.NOOP:
		default:   		
   		
   	  }   	
   	}
  }
}
