/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/


/*
** $Id: TimerThread.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.util;

import java.lang.ref.WeakReference;
import wonka.vm.Heartbeat;

class TimerThread extends Thread implements Comparator {

  private TreeSet tasks;

  boolean cancelled;
  boolean waiting;
  long waitTime;

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
    long time;
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
              this.wait(1000);//we wait for 10 seconds ...
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

            time = System.currentTimeMillis() - Heartbeat.getTimeOffset();
            // System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis() + ", Heartbeat.getTimeOffset() = " + Heartbeat.getTimeOffset() + " => time = " + time);
            if(time < task.startTime){
              //it is not yet time todo the task
              //System.out.println("TIMERTHREAD " + this +": task is not ready to run waiting "+(task.startTime - time));
              waiting = true;
              this.wait(task.startTime - time);
              waiting = false;
              //System.out.println("TIMERTHREAD " + this +":  waited "+(System.currentTimeMillis() - Heartbeat.getTimeOffset() - time));
              continue;
            }
            //System.out.println("TIMERTHREAD " + this +": task is ready to run "+(task.startTime - time));
            tasks.remove(task);
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
          task.startTime = task.period + (task.fixed ? task.startTime : time);
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
    // System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis() + ", Heartbeat.getTimeOffset() = " + Heartbeat.getTimeOffset() + ", delay = " + delay + " => schedule at " + (delay + System.currentTimeMillis() - Heartbeat.getTimeOffset()));
    scheduleAtTime(task, delay + System.currentTimeMillis() - Heartbeat.getTimeOffset(), period, fixedRate);
  }

  synchronized void scheduleAtTime(TimerTask task, long time, long period, boolean fixedRate){
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
    task.startTime = time;
    task.period = period;
    task.fixed = fixedRate;
    tasks.add(task);
    if(waiting){
      //System.out.println("Notifying timer thread");
      this.notifyAll();
    }
  }
}

