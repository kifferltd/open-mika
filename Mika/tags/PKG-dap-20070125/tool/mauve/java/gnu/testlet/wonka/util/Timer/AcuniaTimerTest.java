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


package gnu.testlet.wonka.util.Timer;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.*;


public class AcuniaTimerTest extends TimerTask implements Testlet {

  protected TestHarness th;

  public AcuniaTimerTest(){}

  private AcuniaTimerTest(int loop, TestHarness harness){
    th = harness;
    count = loop;
  }

  private AcuniaTimerTest(int loop, TestHarness harness, long prev){
    th = harness;
    count = loop;
    previous = prev;
  }

  private AcuniaTimerTest(TestHarness harness){
    th = harness;
  }

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.util.Timer");
    test_Timer();
    test_cancel();
    test_schedule();
    test_scheduleAtFixedRate();
    test_basic();
    test_behaviour();
    theTimer = null;
  }

/**
*  not implemented. <br>
*
*/
  public void test_basic(){
    th.checkPoint("basic test");
    Timer timer = new Timer();
    synchronized(this){
      timer.schedule(this,200,100);
      timer = null;
      try {
        this.wait();
        System.gc();
        timerthread.join(15000);
        th.check(!timerthread.isAlive(),"thread should be gone now");
      }
      catch(InterruptedException ie){}
    }
  }

/**
* implemented. <br>
*
*/
  public void test_Timer(){
    th.checkPoint("Timer()");
    Timer timer = new Timer();
    timerthread = null;
    timer.schedule(new AcuniaTimerTest(SET_THREAD, th),0);
    while(timerthread == null){ Thread.yield(); }
    th.check(!timerthread.isDaemon(), "timer has by default a non-daemon thread");

    th.checkPoint("Timer(boolean)");
    timer = new Timer(false);
    timerthread = null;
    timer.schedule(new AcuniaTimerTest(SET_THREAD, th),0);
    while(timerthread == null){ Thread.yield(); }
    th.check(!timerthread.isDaemon(), "timer was set non-daemon");

    timer = new Timer(true);
    timerthread = null;
    timer.schedule(new AcuniaTimerTest(SET_THREAD, th),0);
    while(timerthread == null){ Thread.yield(); }
    th.check(timerthread.isDaemon(), "timer was set daemon");
    th.check(timerthread.getPriority(), 5, "checking priority of the timer");
  }

/**
*  implemented. <br>
*
*/
  public void test_cancel(){
    th.checkPoint("cancel()void");
    theTimer = new Timer();
    timerthread = null;
    theTimer.schedule(new AcuniaTimerTest(CANCEL_TIMER, th),0,50);
    while(timerthread == null){ Thread.yield(); }

    Timer timer = new Timer();
    timer.schedule(new AcuniaTimerTest(FAIL, th),250,50);
    timer.cancel();
  }

/**
* implemented. <br>
*
*/
  public void test_schedule(){
    th.checkPoint("schedule(java.util.TimerTask,long)void");
    timerthread = null;
    Timer killedTimer = new Timer();
    killedTimer.schedule(new AcuniaTimerTest(KILL_THREAD,th),0);
    while(timerthread == null){ Thread.yield(); }
    Thread.yield();
    Timer timer = new Timer(true);
    Timer cancelledTimer = new Timer();
    TimerTask cancelledTask = new AcuniaTimerTest(FAIL,th);
    cancelledTask.cancel();
    TimerTask addedTask = new AcuniaTimerTest(FAIL,th);
    timer.schedule(addedTask,10000000000L);

    try {
      cancelledTimer.schedule(addedTask, 50);
      th.fail("should throw an IllegalStateException -- 0");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    cancelledTimer.cancel();

    try {
      cancelledTimer.schedule(new AcuniaTimerTest(FAIL,th), 50);
      th.fail("should throw an IllegalStateException -- 1");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      killedTimer.schedule(new AcuniaTimerTest(FAIL,th), 50);
      th.fail("should throw an IllegalStateException -- 2");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.schedule(addedTask, 50);
      th.fail("should throw an IllegalStateException -- 3");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.schedule(cancelledTask, 50);
      th.fail("should throw an IllegalStateException -- 4");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.schedule(new AcuniaTimerTest(FAIL, th), -1);
      th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }

    try {
      timer.schedule(new AcuniaTimerTest(FAIL, th), Long.MAX_VALUE);
      th.fail("should throw an IllegalArgumentException -- 2");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }

    Timer runner = new Timer(true);
    timerthread = null;
    runner.schedule(new AcuniaTimerTest(ONE_RUN,th), 25);
    while(timerthread == null){ Thread.yield(); }


    th.checkPoint("schedule(java.util.TimerTask,java.util.Date)void");
    try {
      cancelledTimer.schedule(new AcuniaTimerTest(FAIL,th), new Date());
      th.fail("should throw an IllegalStateException -- 1");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      killedTimer.schedule(new AcuniaTimerTest(FAIL,th), new Date());
      th.fail("should throw an IllegalStateException -- 2");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.schedule(addedTask, new Date());
      th.fail("should throw an IllegalStateException -- 3");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.schedule(cancelledTask, new Date());
      th.fail("should throw an IllegalStateException -- 4");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.schedule(new AcuniaTimerTest(FAIL, th), new Date(-1));
      th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }

    runner = new Timer(true);
    timerthread = null;
    runner.schedule(new AcuniaTimerTest(ONE_RUN,th), new Date(1));
    while(timerthread == null){ Thread.yield(); }

    th.checkPoint("schedule(java.util.TimerTask,long,long)void");
    try {
      cancelledTimer.schedule(new AcuniaTimerTest(FAIL,th), 50, 50);
      th.fail("should throw an IllegalStateException -- 1");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      killedTimer.schedule(new AcuniaTimerTest(FAIL,th), 50, 50);
      th.fail("should throw an IllegalStateException -- 2");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.schedule(addedTask, 50, 50);
      th.fail("should throw an IllegalStateException -- 3");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.schedule(cancelledTask, 50, 50);
      th.fail("should throw an IllegalStateException -- 4");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.schedule(new AcuniaTimerTest(FAIL, th), -1, 50);
      th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }

    try {
      timer.schedule(new AcuniaTimerTest(FAIL, th), Long.MAX_VALUE, 50);
      th.fail("should throw an IllegalArgumentException -- 2");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }

    try {
      timer.schedule(new AcuniaTimerTest(FAIL, th), 10, -50);
      th.fail("should throw an IllegalArgumentException -- 3");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }

    runner = new Timer(true);
    timerthread = null;
    runner.schedule(new AcuniaTimerTest(FIXED_INTERVAL,th,System.currentTimeMillis()), 0,50);
    while(timerthread == null){ Thread.yield(); }

    th.checkPoint("schedule(java.util.TimerTask,java.util.Date,long)void");
    try {
      cancelledTimer.schedule(new AcuniaTimerTest(FAIL,th), new Date(),50);
      th.fail("should throw an IllegalStateException -- 1");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      killedTimer.schedule(new AcuniaTimerTest(FAIL,th), new Date(),50);
      th.fail("should throw an IllegalStateException -- 2");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.schedule(addedTask, new Date(),50);
      th.fail("should throw an IllegalStateException -- 3");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.schedule(cancelledTask, new Date(),50);
      th.fail("should throw an IllegalStateException -- 4");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.schedule(new AcuniaTimerTest(FAIL, th), new Date(), -50);
      th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }
    try {
      timer.schedule(new AcuniaTimerTest(FAIL, th), new Date(-1), 50);
      th.fail("should throw an IllegalArgumentException -- 2");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }

    runner = new Timer(true);
    timerthread = null;
    runner.schedule(new AcuniaTimerTest(FIXED_INTERVAL,th,0), new Date(0), 50);
    while(timerthread == null){ Thread.yield(); }
    timer.cancel();
  }

/**
* implemented. <br>
*
*/
  public void test_scheduleAtFixedRate(){
    th.checkPoint("scheduleAtFixedRate(java.util.TimerTask,long,long)void");
    timerthread = null;
    Timer killedTimer = new Timer();
    killedTimer.scheduleAtFixedRate(new AcuniaTimerTest(KILL_THREAD,th),0,1);
    while(timerthread == null){ Thread.yield(); }
    Thread.yield();
    Timer timer = new Timer(true);
    Timer cancelledTimer = new Timer();
    TimerTask cancelledTask = new AcuniaTimerTest(FAIL,th);
    cancelledTask.cancel();
    TimerTask addedTask = new AcuniaTimerTest(FAIL,th);
    timer.scheduleAtFixedRate(addedTask,10000000000L,100000000L);

    try {
      cancelledTimer.scheduleAtFixedRate(addedTask, 50, 50);
      th.fail("should throw an IllegalStateException -- 0");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    cancelledTimer.cancel();

    try {
      cancelledTimer.scheduleAtFixedRate(new AcuniaTimerTest(FAIL,th), 50, 50);
      th.fail("should throw an IllegalStateException -- 1");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      killedTimer.scheduleAtFixedRate(new AcuniaTimerTest(FAIL,th), 50, 50);
      th.fail("should throw an IllegalStateException -- 2");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.scheduleAtFixedRate(addedTask, 50, 50);
      th.fail("should throw an IllegalStateException -- 3");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.scheduleAtFixedRate(cancelledTask, 50, 50);
      th.fail("should throw an IllegalStateException -- 4");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.scheduleAtFixedRate(new AcuniaTimerTest(FAIL, th), -1, 50);
      th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }

    try {
      timer.scheduleAtFixedRate(new AcuniaTimerTest(FAIL, th), Long.MAX_VALUE, 50);
      th.fail("should throw an IllegalArgumentException -- 2");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }

    try {
      timer.scheduleAtFixedRate(new AcuniaTimerTest(FAIL, th), 50, -1);
      th.fail("should throw an IllegalArgumentException -- 3");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }

    th.checkPoint("scheduleAtFixedRateAtFixedRate(java.util.TimerTask,java.util.Date,long)void");

    try {
      cancelledTimer.scheduleAtFixedRate(new AcuniaTimerTest(FAIL,th), new Date(), 50);
      th.fail("should throw an IllegalStateException -- 1");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      killedTimer.scheduleAtFixedRate(new AcuniaTimerTest(FAIL,th), new Date(), 50);
      th.fail("should throw an IllegalStateException -- 2");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.scheduleAtFixedRate(addedTask, new Date(), 50);
      th.fail("should throw an IllegalStateException -- 3");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.scheduleAtFixedRate(cancelledTask, new Date(), 50);
      th.fail("should throw an IllegalStateException -- 4");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    try {
      timer.scheduleAtFixedRate(new AcuniaTimerTest(FAIL, th), new Date(-1), 50);
      th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }

    try {
      timer.scheduleAtFixedRate(new AcuniaTimerTest(FAIL, th), new Date(1), -1);
      th.fail("should throw an IllegalArgumentException -- 2");
    }
    catch(IllegalArgumentException iae){
      th.check(true);
    }

    Timer runner = new Timer(true);
    timerthread = null;
    runner.scheduleAtFixedRate(new AcuniaTimerTest(FIXED_RATE,th,1), new Date(1),50);
    while(timerthread == null){ Thread.yield(); }
    timer.cancel();

  }

/**
*   not implemented. <br>
*
*/
  public void test_behaviour(){
    th.checkPoint("()");

  }

  private static int SET_THREAD = 20;
  private static int KILL_THREAD = 21;
  private static int CANCEL_TIMER = 30;
  private static int FIXED_RATE = 40;
  private static int RATE_STOP = 49;
  private static int FIXED_INTERVAL = 50;
  private static int INTERVAL_STOP = 59;
  private static int ONE_RUN = 60;
  private static int FAIL = -1;

  private static Thread timerthread;
  private static Timer theTimer;

  private long previous;
  private int count;

  //TimerTask API ...
  public void run(){
    if(count == FAIL){
      th.fail("TimerTask FAILED");
      throw new Error();
    }

    if(count == ONE_RUN){
      th.check(this.scheduledExecutionTime() <= System.currentTimeMillis(), "ONE_RUN check");
      timerthread = Thread.currentThread();
      count = FAIL;
      return;
    }

    if(count == SET_THREAD){
      timerthread = Thread.currentThread();
      return;
    }

    if(count == INTERVAL_STOP){
      th.check((this.scheduledExecutionTime() >= previous + 1500), "checking fixed interval");
      timerthread = Thread.currentThread();
      cancel();
      count = FAIL;
      return;
    }

    if(count >= FIXED_INTERVAL){
      try {
        Thread.sleep(200);
      }
      catch(InterruptedException ie){}
      count++;
      return;
    }

    if(count == RATE_STOP){
      th.check(this.scheduledExecutionTime() ,previous, "checking fixed rate");
      timerthread = Thread.currentThread();
      cancel();
      count = FAIL;
      return;
    }

    if(count >= FIXED_RATE){
      th.check(this.scheduledExecutionTime() ,previous, "checking fixed rate");
      previous += 50;
      try {
        Thread.sleep(100);
      }
      catch(InterruptedException ie){}
      count++;
      return;
    }

    if(count == KILL_THREAD){
      timerthread = Thread.currentThread();
      throw new Error("this should kill the timerThread -- This is done intentionally");
    }

    if(count == CANCEL_TIMER){
      timerthread = Thread.currentThread();
      theTimer.cancel();
      count = FAIL;
      return;
    }

    th.check(this.scheduledExecutionTime() <= System.currentTimeMillis());
    if(count++ >= 5){
      th.check(cancel(), "stop the loop");
      th.check(!cancel(),"already cancelled the loop");
      count = FAIL;
      synchronized(this){
        timerthread = Thread.currentThread();
        this.notifyAll();
      }
    }
  }
}
