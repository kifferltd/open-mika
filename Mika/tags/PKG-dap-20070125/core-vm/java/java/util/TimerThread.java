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


/*
** $Id: TimerThread.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.util;

import java.lang.ref.WeakReference;

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

            time = System.currentTimeMillis();
            if(time < task.startTime){
              //it is not yet time todo the task
              //System.out.println("TIMERTHREAD " + this +": task is not ready to run waiting "+(task.startTime - time));
              waiting = true;
              this.wait(task.startTime - time);
              waiting = false;
              //System.out.println("TIMERTHREAD " + this +":  waited "+(System.currentTimeMillis() - time));
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
    scheduleAtTime(task, delay + System.currentTimeMillis(), period, fixedRate);
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

