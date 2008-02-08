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
