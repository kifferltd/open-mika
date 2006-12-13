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
** $Id: Timer.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.util;

public class Timer {

  private TimerThread thread;

  public Timer(){
    this(false);
  }

  public Timer(boolean daemon){
    thread = new TimerThread(this, daemon);
  }

  public void cancel(){
    if(!thread.cancelled){
      synchronized(thread){
        thread.cancelled = true;
        if(thread.waiting){
          thread.notifyAll();
        }
      }
    }
  }

  public void schedule(TimerTask task, Date date){
    thread.scheduleAtTime(task, date.getTime(), -1, false);
  }

  public void schedule(TimerTask task, Date date, long period){
    if(period <= 0){
      throw new IllegalArgumentException();
    }
    thread.scheduleAtTime(task, date.getTime(), period, false);
  }

  public void schedule(TimerTask task, long delay){
    thread.schedule(task, delay, -1, false);
  }

  public void schedule(TimerTask task, long delay, long period){
    if(period <= 0){
      throw new IllegalArgumentException();
    }
    thread.schedule(task, delay, period, false);
  }

  public void scheduleAtFixedRate(TimerTask task, Date date, long period){
    if(period <= 0){
      throw new IllegalArgumentException();
    }
    thread.scheduleAtTime(task, date.getTime(), period, true);
  }

  public void scheduleAtFixedRate(TimerTask task, long delay, long period){
    if(period <= 0){
      throw new IllegalArgumentException();
    }
    thread.schedule(task, delay, period, true);
  }
}
