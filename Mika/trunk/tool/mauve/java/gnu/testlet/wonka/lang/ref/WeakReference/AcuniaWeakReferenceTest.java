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


package gnu.testlet.wonka.lang.ref.WeakReference;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import gnu.testlet.wonka.lang.ref.Reference.*;

import java.lang.ref.*;


public class AcuniaWeakReferenceTest implements Testlet, GarbageListener {

  protected TestHarness th;

  private Runtime rt = Runtime.getRuntime();
  private long mem;


  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.lang.ref.WeakReference");
    test_WeakReference();
    test_get();
    test_clear();
    test_enqueue();
    test_isEnqueued();
    test_behaviour();
  }


/**
* implemented. <br>
*
*/
  public void test_WeakReference(){
    th.checkPoint("WeakReference(java.lang.Object)");
    ReferenceQueue rq = new ReferenceQueue();
    try {
       new WeakReference(null);
       th.fail("should throw a NullPointerException -- 1");

    } catch (NullPointerException np){
      th.check(true , "correct exception was thrown -- 1");
    }
    th.checkPoint("WeakReference(java.lang.Object,java.lang.ref.ReferenceQueue)");
    try {
       new WeakReference(null,rq);
       th.fail("should throw a NullPointerException -- 2");

    } catch (NullPointerException np){
      th.check(true , "correct exception was thrown -- 2");
    }
    try {
       new WeakReference(new Object(),null);
       th.fail("should throw a NullPointerException -- 3");

    } catch (NullPointerException np){
      th.check(true , "correct exception was thrown -- 3");
    }

  }

/**
*  implemented. <br>
*
*/
  public void test_get(){
    th.checkPoint("get()java.lang.Object");
    String msg = "always returns null";
    WeakReference wr = new WeakReference(msg , new ReferenceQueue());
    th.check(wr.get(), msg, msg);
    wr.clear();
    th.check(wr.get(), null, msg);
  }

/**
* implemented. <br>
*
* memory should not be reclaimed until we clear the WeakReference.
* we try to verify this by creating a huge array to be reclaimed ...
*/
  public void test_clear(){
    th.checkPoint("clear()void");
    ReferenceQueue rq = new ReferenceQueue();
    //th.debug("constructing WeakReference");
    WeakReference wr = makeWeak(rq, 2000000);
    Reference ref = null;
    //th.debug("waiting for GC to put the WeakReference on the queue");
    int i=0;
    while(ref == null && i < 10){
      try {
        System.gc();
        System.runFinalization();
        ref = rq.remove(1000);
      } catch(Exception e){}
      //th.debug("nothing in the queue");
      i++;
    }
    th.check(ref, wr, "reference should be the same");
    if(ref != null){
       th.check(ref.get(), null, "referent is collected");
    }
    else{
      th.fail("WeakReference was not put on the queue");
    }
    System.gc();
    long memNow = rt.totalMemory() - rt.freeMemory();
    th.debug("Memory used is now "+memNow+", was "+mem);
    th.check(memNow + (1500000) < mem, "MEMORY TEST");
  }

/**
*  implemented. <br>
*
*/
  public void test_enqueue(){
    th.checkPoint("enqueue()boolean");
    String msg = "checking return value -- ";
    int i=1;
    ReferenceQueue rq = new ReferenceQueue();
    WeakReference wr = new WeakReference(msg , rq);
    th.check(wr.enqueue(), msg+(i++));
    th.check(!wr.enqueue(), msg+(i++));
    Reference ref = rq.poll();
    th.check(ref, wr , "reference was put into the queue");
    if(ref != null){
      th.check(ref.get() , msg , "Object was not collected yet");
    }
    th.check(!wr.enqueue(), msg+(i++));
    wr.clear();
    msg = "special case -- ";
    i=1;
    wr = new WeakReference(msg , rq);
    wr.clear();
    th.check(!wr.isEnqueued(), msg+(i++));
    th.check(wr.enqueue(), msg+(i++));
    th.check(!wr.enqueue(), msg+(i++));
    th.check(rq.poll(), wr , "reference was put into the queue");
  }

/**
* implemented. <br>
*
*/
  public void test_isEnqueued(){
    th.checkPoint("isEnqueued()boolean");
    String msg = "checking return value -- ";
    int i=1;
    ReferenceQueue rq = new ReferenceQueue();
    WeakReference wr = new WeakReference(msg , rq);
    th.check(!wr.isEnqueued(), msg+(i++));
    th.check(wr.enqueue(), msg+(i++));
    th.check(wr.isEnqueued(), msg+(i++));
    th.check(rq.poll(), wr , "reference was put into the queue");
    th.check(!wr.isEnqueued(), msg+(i++));
    wr.clear();

  }

/**
*  implemented. <br>
*
*/
  public void test_behaviour(){
    th.checkPoint("WeakReference(java.lang.Object,java.lang.ref.ReferenceQueue)");
    ReferenceQueue rq = new ReferenceQueue();
    Garbage gb = new Garbage(this, null, 1);
    gb = new Garbage(this, gb, 2);
    WeakReference wr = new WeakReference(gb, rq);
    gb = new Garbage(this, null, 3);
    wr = new WeakReference(gb, rq);
    gb = null; //cut object loose
    while(!got1 && !got2 && !got3){
      System.gc();
      System.runFinalization();
      try {
        Thread.sleep(100);
      } catch(InterruptedException ie){}
    }
    th.check(rq.poll(), wr, "WeakReference got collected --> reference put in the queue");
    th.check(wr.get() , null, "Object is collected");
    gb = new Garbage(this, null, 4);
    new WeakReference(new Garbage(this, gb, 2), rq);
    got2 = false;
    while(!got2){
      System.gc();
      System.runFinalization();
      try {
        Thread.sleep(100);
      } catch(InterruptedException ie){}
    }
    done = true;
    wr = null;
  }

  private boolean got1;
  private boolean got2;
  private boolean got3;
  private boolean done;

  public void reportFinalize(int id){
    if (id == 3 && !got3){
      got3 = true;
      th.check(true , "collecting Garbage object 3");
    }
    else if (id == 2 && !got2){
      got2 = true;
      th.check(true , "collecting Garbage object 2");
    }
    else if (id == 1 && !got1){
      got1 = true;
      th.check(true , "collecting Garbage object 1");
    }
    else if (!done) {
      th.fail("collected wrong object (id = "+id+")");
    }
  }
  private WeakReference makeWeak(ReferenceQueue rq, int size){
    Object obj = new byte[size];
    mem = rt.totalMemory() - rt.freeMemory();
    return new WeakReference(obj, rq);
  }
}
