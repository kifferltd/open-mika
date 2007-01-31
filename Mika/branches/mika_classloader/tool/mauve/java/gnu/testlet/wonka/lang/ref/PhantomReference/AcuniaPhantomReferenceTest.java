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


package gnu.testlet.wonka.lang.ref.PhantomReference;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.lang.ref.*;


public class AcuniaPhantomReferenceTest implements Testlet {

  protected TestHarness th;
  private ReferenceQueue queue;
  private boolean finalized;

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.lang.ref.PhantomReference");
    //test_PhantomReference();
    test_get();
    test_clear();
    test_enqueue();
    test_isEnqueued();
    test_behaviour();
  }


  public void test_behaviour() {
    th.checkPoint("PhantomReference: behaviour");
    try {
      queue = new ReferenceQueue();
      finalized = false;
      PhantomReference pr = new PhantomReference(new Mock(this), queue);
      System.gc();
      System.runFinalization();
      synchronized (this) {
        if (finalized == false) {
          wait(20000);
        }
      }
      System.gc();
      th.check(queue.remove(10000), pr);
    } catch (InterruptedException e) {
      th.fail(e.toString());
    }
  }


/**
* implemented. <br>
*
*/
  public void test_PhantomReference(){
    th.checkPoint("PhantomReference(java.lang.Object,java.lang.ref.ReferenceQueue)");
    ReferenceQueue rq = new ReferenceQueue();
    try {
       new PhantomReference(null,rq);
       th.fail("should throw a NullPointerException -- 1");

    } catch (NullPointerException np){
      th.check(true , "correct exception was thrown -- 1");
    }
    try {
       new PhantomReference(new Object(),null);
       th.fail("should throw a NullPointerException -- 2");

    } catch (NullPointerException np){
      th.check(true , "correct exception was thrown -- 2");
    }
  }

/**
*  implemented. <br>
*
*/
  public void test_get(){
    th.checkPoint("get()java.lang.Object");
    String msg = "always returns null";
    PhantomReference pr = new PhantomReference(msg , new ReferenceQueue());
    th.check(pr.get(), null, msg);
    pr.clear();
  }

/**
* implemented. <br>
*
* memory should not be reclaimed until we clear the PhantomReference.
* we try to verify this by creating a huge array to be reclaimed ...
*/
  public void test_clear(){
    th.checkPoint("clear()void");
    ReferenceQueue rq = new ReferenceQueue();
    Runtime rt = Runtime.getRuntime();
    // long mem = rt.totalMemory() - rt.freeMemory();
    // th.debug("Memory used is "+mem);
    // th.debug("constructing PhantomReference");
    PhantomReference pr = makePhantom(rq, 1000000);
    Reference ref = null;
    // th.debug("waiting for GC to put PhantomReference on the queue");
    while(ref == null){
      try {
        System.gc();
        System.runFinalization();
        ref = rq.remove(1000);
      } catch(Exception e){}
      // th.debug("nothing in the queue");
    }
    th.check(ref, pr, "reference should be the same");
    long memC = rt.totalMemory() - rt.freeMemory();
    //th.debug("Memory used is "+memC);
    System.gc();
    System.runFinalization();
    ref.clear();
    System.gc();
    System.runFinalization();
    System.gc();
    System.runFinalization();
//    System.gc();
//    System.runFinalization();
    long memNow = rt.totalMemory() - rt.freeMemory();
    th.debug("Memory used is now "+memNow+", was "+memC);
    th.check(memNow + 1000000 < memC);
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
    PhantomReference pr = new PhantomReference(msg , rq);
    th.check(pr.enqueue(), msg+(i++));
    th.check(!pr.enqueue(), msg+(i++));
    th.check(rq.poll(), pr , "reference was put into the queue");
    th.check(!pr.enqueue(), msg+(i++));
    pr.clear();
    msg = "special case -- ";
    i=1;
    pr = new PhantomReference(msg , rq);
    pr.clear();
    th.check(!pr.isEnqueued(), msg+(i++));
    th.check(pr.enqueue(), msg+(i++));
    th.check(!pr.enqueue(), msg+(i++));
    th.check(rq.poll(), pr , "reference was put into the queue");
    th.check(!pr.enqueue(), msg+(i++));
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
    PhantomReference pr = new PhantomReference(msg , rq);
    th.check(!pr.isEnqueued(), msg+(i++));
    th.check(pr.enqueue(), msg+(i++));
    th.check(pr.isEnqueued(), msg+(i++));
    th.check(rq.poll(), pr , "reference was put into the queue");
    th.check(!pr.isEnqueued(), msg+(i++));
    pr.clear();

  }

  synchronized void finalizingMock() {
    finalized = true;
    try {
      th.check(queue.remove(500), null);
    } catch (Exception e) {
      th.fail("query failed.");
    }
    
  }
  private PhantomReference makePhantom(ReferenceQueue rq, int size){
    return new PhantomReference(new byte[size], rq);
  }


}
