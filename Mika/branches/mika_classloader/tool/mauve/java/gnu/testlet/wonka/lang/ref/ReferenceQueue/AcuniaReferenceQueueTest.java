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


package gnu.testlet.wonka.lang.ref.ReferenceQueue;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.HashSet;
import java.lang.ref.*;


public class AcuniaReferenceQueueTest implements Testlet {

  protected TestHarness th;

  public void test (TestHarness harness) {
       th = harness;
       th.setclass("java.lang.ref.ReferenceQueue");
       test_ReferenceQueue();
       test_poll();
//       test_remove1();
//       test_remove2();
  }

/**
* implemented. <br>
*
*/
  public void test_ReferenceQueue(){
    th.checkPoint("ReferenceQueue()");
    //nothing to check ...
    th.check(new ReferenceQueue().poll(), null, "new ReferenceQueue is empty");
  }

/**
* implemented. <br>
*
*/
  public void test_poll(){
    th.checkPoint("poll()java.lang.ref.Reference");
    ReferenceQueue rq = new ReferenceQueue();
    SoftReference sr = new SoftReference("Hello", rq);
    sr.enqueue();
    th.check(rq.poll(), sr, "reference should be there -- 1");
    th.check(rq.poll(), null, "reference was removed from the queue -- 2");
    String s = "Hello";
    HashSet hs = new HashSet(7);
    Reference r1 = new PhantomReference(s, rq);
    r1.enqueue();
    hs.add(r1);
    Reference r2 = new WeakReference(s, rq);
    r2.enqueue();
    hs.add(r2);
    Reference r3 = new SoftReference(s, rq);
    r3.enqueue();
    hs.add(r3);
    //Queue order is not guaranteed
    th.check(hs.remove(rq.poll()), "a reference should be there -- 3");
    th.check(hs.remove(rq.poll()), "a reference should be there -- 4");
    th.check(hs.remove(rq.poll()), "a reference should be there -- 5");
    th.check(rq.poll(), null, "all references were removed from the queue -- 6");

  }

/**
* implemented. <br>
*
*/
  public void test_remove1(){
    th.checkPoint("remove()java.lang.ref.Reference");
    Thread t = new Thread (new RQRunner(null,0,this), "ReferenceQueueTest");
    t.start();

  }

/**
* implemented. <br>
*
*/
  public void test_remove2(){
    th.checkPoint("remove(long)java.lang.ref.Reference");

  }

  private boolean wantedException;
  private Reference onQueue;

  public void reportInterrupt(){
    th.check(wantedException ,"RQRunner got an InterruptedException while removing");
  }

  public void reportReference(Reference ref){
    th.check(ref, onQueue, "checking return value remove");
  }

  public static class RQRunner implements Runnable {

    private AcuniaReferenceQueueTest testlet;
    private ReferenceQueue rq;
    private long timeOut;


    public RQRunner(ReferenceQueue rq, long timeOut,AcuniaReferenceQueueTest testlet){
      if(rq != null){
        this.rq = rq;
      }
      else {
        this.rq = new ReferenceQueue();
      }
      this.timeOut = timeOut;
      this.testlet = testlet;
    }

    public void run(){
      try {
         Reference ref;
         if(timeOut == 0){
           ref = rq.remove();
         }
         else{
           ref = rq.remove(timeOut);
         }
         testlet.reportReference(ref);
      }
      catch(InterruptedException ie){
        testlet.reportInterrupt();
      }
    }

  }

}
