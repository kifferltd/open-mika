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


package gnu.testlet.wonka.lang.ref.SoftReference;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import gnu.testlet.wonka.lang.ref.Reference.*;
import java.util.*;

import java.lang.ref.*;


public class AcuniaSoftReferenceTest implements Testlet, GarbageListener {

  protected TestHarness th;

  private Runtime rt = Runtime.getRuntime();
  private SoftReference sr;

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.lang.ref.SoftReference");
    //test_SoftReference();
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
  public void test_SoftReference(){
    th.checkPoint("SoftReference(java.lang.Object)");
    ReferenceQueue rq = new ReferenceQueue();
    try {
       new SoftReference(null,rq);
       th.fail("should throw a NullPointerException -- 1");

    } catch (NullPointerException np){
      th.check(true , "correct exception was thrown -- 1");
    }
    th.checkPoint("SoftReference(java.lang.Object,java.lang.ref.ReferenceQueue)");
    try {
       new SoftReference(null,rq);
       th.fail("should throw a NullPointerException -- 1");

    } catch (NullPointerException np){
      th.check(true , "correct exception was thrown -- 1");
    }
    try {
       new SoftReference(new Object(),null);
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
    String msg = "check return value ";
    sr = new SoftReference(msg , new ReferenceQueue());
    th.check(sr.get(), msg, msg+1);
    sr.clear();
    th.check(sr.get(), null, msg+2);
    ReferenceQueue rq = new ReferenceQueue();
    sr = new SoftReference(msg , rq);
    sr.enqueue();
    rq.poll();
    th.check(sr.get(), msg, msg+3);
  }

/**
* implemented. <br>
*
* Stress Test
*
* Objects reachable by Soft references should not be collected until we run low on memory.
* However they may not cause OutOfMemoryError
*/
  public void test_clear(){
    th.checkPoint("clear()void");
    ReferenceQueue rq = new ReferenceQueue();
    HashSet hs = new HashSet(1000);
    // Assuming we have at least 1 MB free before SoftReferences start getting killed
    makeSoft(hs, rq, 1000000);
    // Yield CPU to allow queueing to happen
    try {
      Thread.sleep(1000);
    }
    catch (InterruptedException ie) {}

    th.check(rq.poll(), null , "no references should be collected");
    for(int i=0 ; i < 250 ; i++){
      makeSoft(hs, rq, 1000000);
    }
    // Yield CPU to allow queueing to happen
    try {
      Thread.sleep(1000);
    }
    catch (InterruptedException ie) {}

    Reference ref = rq.poll();
    //there must be some references in the queue ... !!!
    th.check(ref != null, "some of the objects should be collected");
    hs.remove(ref);
    int count=0;
    while(ref != null){
      count++;
      hs.remove(ref);
      //th.debug(ref+"-->"+ref.get());
      if(ref.get() != null){
        th.fail("value was not cleared ...");
      }
      ref = rq.poll();
    }
    th.check(count > 100 , "there should be at least be 100 reference collected (got "+count+")");
/*
    THESE TEST FAIL ON WONKA HOWEVER it is not really specified it has to be that way.

    Iterator it = hs.iterator();
    while(it.hasNext()){
      ref = (Reference)it.next();
      //th.debug(ref+"-->"+ref.get());
      if(ref.get() == null && rq.poll() == null){
        th.fail("value was cleared ...");
      }
    }
*/
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
    sr = new SoftReference(msg, rq);
    th.check(sr.enqueue(), msg+(i++));
    th.check(!sr.enqueue(), msg+(i++));
    Reference ref = rq.poll();
    th.check(ref, sr , "reference was put into the queue");
    if(ref != null){
      th.check(ref.get() , msg , "Object was not collected yet");
    }
    th.check(!sr.enqueue(), msg+(i++));
    sr.clear();
    msg = "special case -- ";
    i=1;
    sr = new SoftReference(msg , rq);
    sr.clear();
    th.check(!sr.isEnqueued(), msg+(i++));
    th.check(sr.enqueue(), msg+(i++));
    th.check(!sr.enqueue(), msg+(i++));
    th.check(rq.poll(), sr , "reference was put into the queue");
    th.check(!sr.enqueue(), msg+(i++));
    msg = "not registred to a queue -- ";
    i=1;
    sr = new SoftReference(msg);
    th.check(!sr.isEnqueued(), msg+(i++));
    th.check(!sr.enqueue(), msg+(i++));
    th.check(!sr.isEnqueued(), msg+(i++));

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
    sr = new SoftReference(msg , rq);
    th.check(!sr.isEnqueued(), msg+(i++));
    th.check(sr.enqueue(), msg+(i++));
    th.check(sr.isEnqueued(), msg+(i++));
    th.check(rq.poll(), sr , "reference was put into the queue");
    th.check(!sr.isEnqueued(), msg+(i++));
    sr.clear();
  }

/**
*  implemented. <br>
*
*/
  public void test_behaviour(){
    th.checkPoint("SoftReference(java.lang.Object,java.lang.ref.ReferenceQueue)");
    ReferenceQueue rq = new ReferenceQueue();
    Garbage gb = new Garbage(this, null, 1);
    gb = new Garbage(this, gb, 2);
    sr = new SoftReference(gb, rq);
    gb = null;
    Garbage gb3 = new Garbage(this, null, 3);
    sr = new SoftReference(gb3, rq);
    while(!got1 && !got2){
      System.gc();
      System.runFinalization();
      try {
        Thread.sleep(100);
      } catch(InterruptedException ie){}
    }
    th.check(rq.poll(), null, "SoftReference got collected --> nothing put in the queue");
    gb = new Garbage(this, null, 4);
    got2 = false;
    new SoftReference(new Garbage(this, gb, 2), rq);
    while(!got2){
      System.gc();
      System.runFinalization();
      try {
        Thread.sleep(100);
      } catch(InterruptedException ie){}
    }
    done = true;
    sr = null;
  }

  private boolean got1;
  private boolean got2;
  private boolean done;

  public void reportFinalize(int id){
    if (id == 2 && !got2){
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

  private void makeSoft(HashSet hs,ReferenceQueue rq, int size){
    hs.add(new SoftReference(new byte[size], rq));
    long mem = rt.totalMemory() - rt.freeMemory();
    //th.debug("constructed SoftReference Memory used is "+mem);
  }
}
