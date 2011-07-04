package gnu.testlet.wonka.util.Timer;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.Timer;
import java.util.TimerTask;


public class KTimerTest implements Testlet {

  private class TestTimerTask extends TimerTask  {
    public void run(){
      ++total;
    }
  }

  protected TestHarness th;

  private int total;

  public KTimerTest(){}

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.util.Timer");
    test_duplicate();
  }

/**
*  Test the case where two tasks will fall due at the same instant.
*  (We had a bug where one would overwrite the other in the scheduler).
*/
  public void test_duplicate(){
    th.checkPoint("two timers which expire together");
    Timer timer1 = new Timer();
    Timer timer2 = new Timer();
    TimerTask task1 = new TestTimerTask();
    TimerTask task2 = new TestTimerTask();
    synchronized(this){
      timer1.schedule(task1 ,200);
      timer2.schedule(task2 ,200);
      try {
        this.wait(500);
      }
      catch(InterruptedException ie){}
    }
    th.check(total,2);
  }

}
