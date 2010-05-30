/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2009 by /k/ Embedded Java Solutions.                *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package java.util;

import java.lang.ref.WeakReference;
import wonka.vm.Heartbeat;

class TimerThread extends Thread implements Comparator {

  private TreeSet tasks;

  boolean cancelled;
  boolean waiting;
  long waitTime;

  private long savedtime;

  /**
  ** we use a WeakReference to reference the Timer.  When all references to the timer are gone, the thread can terminate.
  */
  private WeakReference timerReference;

  TimerThread(Timer timer, boolean daemon){
    super("Thread for timer:"+timer);
    timerReference = new WeakReference(timer);
    tasks = new TreeSet(this);
    this.setDaemon(daemon);
    this.start();
  }

  public int compare(Object o, Object t){
    if(o == t){
      return 0;
    }
    return (((TimerTask)o).startTime < ((TimerTask)t).startTime) ? -1 : 1 ;
  }

  public void run(){
    //System.out.println("STARTING TIMERTHREAD " + this);
    TimerTask task;
    long time0; // System.currentTimeMillis()
    long time1; // earliest estimate of corrected time
    long time2; // latest estimate of corrected time
    long offset;

    while(true){
      synchronized(this){
        try {
          if(cancelled){
            //System.out.println("TIMERTHREAD "+ this + ": Thread is cancelled TIMERTHREAD");
            break;
          }
          if(tasks.isEmpty()){
            //STATE 1: no tasks scheduled.
            //System.out.println("TIMERTHREAD "+ this + ": queue is empty");
            if(timerReference.get() != null){
              //we use this boolean to indicate when the timer The is waiting
              //at that point it is ok for the timer to call a notifyAll without
              //disturbing any timer tasks ...
              //System.out.println("TIMERTHREAD " + this +": timer is alive");
              waiting = true;
              this.wait(1000);
              waiting = false;
            }
            else {
              //no task scheduled and the 'Timer' is collected. We stop the tread.
              //System.out.println("TIMERTHREAD " + this +": timer is gone");
              cancelled = true;
            }
            continue;
          }
          else {
            //STATE 2: tasks are pending ...
            //System.out.println("TIMERTHREAD " + this +": queue is not empty");
            task = (TimerTask)tasks.first();
            if(task.cancelled){
              tasks.remove(task);
              continue;
            }

            offset = task.absolute ? 0 : Heartbeat.getTimeOffset();
            if (offset > task.savedoffset) {
              //System.out.println("TIMERTHREAD " + this + ": offset has increased, " + task.savedoffset + " -> " + offset);
            }
            else if (offset < task.savedoffset) {
              //System.out.println("TIMERTHREAD " + this + ": offset has decreased, " + task.savedoffset + " -> " + offset);
            }
            time0 = System.currentTimeMillis();
            if (offset > task.savedoffset) {
              time1 = time0 - offset;
              time2 = time0 - task.savedoffset;
            }
            else {
              time1 = time0 - task.savedoffset;
              time2 = time0 - offset;
            }
            if (time0 < savedtime) {
              //System.out.println("TIMERTHREAD " + this + ": time went backwards by " + (savedtime - time0));
              time1 -= savedtime - time0;
            }
            if (time2 < task.startTime) {
              long nap = task.startTime - time2;
              if (nap > 10000) {
                nap = 10000;
              }
              //it is not yet time todo the task
              //System.out.println("TIMERTHREAD " + this +": task is not ready to run waiting " + nap);
              waiting = true;
              this.wait(nap);
              waiting = false;
              continue;
            }
            //System.out.println("TIMERTHREAD " + this +": task is ready to run " + (task.startTime - time2));
            tasks.remove(task);
            // if task is periodic, subsequent schedulings are considered to be relative
            task.absolute = false;
            savedtime = time0;
          }
        }
        catch(InterruptedException ie){
          //System.out.println("TIMERTHREAD " + this +": thread is interrupted");
          waiting = false;
          continue;
        }
      }
      /**
      ** The task cannot be run in a synchronized block ...
      ** We leave the chance for other to add tasks to timer.
      */
      //System.out.println("TIMERTHREAD " + this +": running task "+task);
      task.run();

      synchronized(this){
        //System.out.println("TIMERTHREAD " + this +": cleaning up task "+task);
        if(task.period != -1 && !task.cancelled){
          //System.out.println("TIMERTHREAD " + this +": putting task back in "+task);
          //System.out.println("TIMERTHREAD " + this + (task.fixed ? " fixed rate scheduling, next = " + (task.period + task.startTime) : " free scheduling, next = " + (task.period + time1)));
          task.startTime = task.period + (task.fixed ? task.startTime : time1);
          task.savedoffset = offset;
          tasks.add(task);
        }
        else {
          //System.out.println("TIMERTHREAD " + this +": cancelling task "+task);
          task.cancelled = true;
        }
      }
    }
  }

  void schedule(TimerTask task, long delay, long period, boolean fixedRate){
    if(delay < 0){
      throw new IllegalArgumentException("negative delay is not allowed");
    }
    // System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis() + ", delay = " + delay + " => schedule at " + (delay + System.currentTimeMillis()));
    scheduleAtTime(task, delay + System.currentTimeMillis(), period, fixedRate, false);
  }

  synchronized void scheduleAtTime(TimerTask task, long time, long period, boolean fixedRate, boolean absolute){
    if(time < 0){
      throw new IllegalArgumentException("negative time is not allowed");
    }
    if(task.startTime != -1 || task.cancelled){
      throw new IllegalStateException("task cannot be scheduled");
    }
    if(cancelled || !this.isAlive()){
      throw new IllegalStateException("timer cannot schedule task "+task);
    }
    //System.out.println("Adding task to timer "+task);
    //System.out.println("Setting " + task + ".startTime to " + time);
    task.savedoffset = absolute ? 0 : Heartbeat.getTimeOffset();
    task.startTime = time - task.savedoffset;
    task.period = period;
    task.fixed = fixedRate;
    tasks.add(task);
    if(waiting){
      //System.out.println("Notifying timer thread");
      this.notifyAll();
    }
  }
}

