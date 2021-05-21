/**************************************************************************
* Copyright (c) 2009, 2011, 2014 by Chris Gray, KIFFER Ltd.               *
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
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

package java.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.PhantomReference;

public class Timer {

  /**
   * We use phantom references to track instances of 
   * java.util.Timer which have become unreachable.
   * When the PhantomReference is enqueued we interrupt
   * the TimerThread, which will see that its associated
   * Timer is unreachable and so will terminate.  Then
   * GC can do its work and reclaim the whole caboodle.
   * <p>We store the phantom references in a HashSet so
   * that thsy themselves do not get garbage-collected.
   */
  private static Set phantomReferences = new HashSet();

  private static ReferenceQueue queue = new ReferenceQueue();

  private static class TimerPhantomReference extends PhantomReference {
    private TimerThread timerThread;

    private TimerPhantomReference(Timer timer, ReferenceQueue queue, TimerThread timerThread) {
      super(timer, queue);
      this.timerThread = timerThread;
    }

    private void cleanup() {
      timerThread.interrupt();
    }
  }
 
  static {
 
    /**
     * This is the daemon thread which collects phantom 
     * references from the queue and interrupts the 
     * corresponding TimerThread.
     */
    Runnable referenceGleaner = new Runnable() {
      public void run() {
        while (true) {
          try {
            TimerPhantomReference tpr = (TimerPhantomReference)queue.remove();
            tpr.cleanup();
            synchronized (phantomReferences) {
              phantomReferences.remove(tpr);
            }
          } catch (Exception ex) {
            // ignore
          }
        }
      }
    };

    Thread gleanerThread = new Thread(referenceGleaner, "java.util.Timer clean-up thread");
    gleanerThread.setDaemon(true);
    gleanerThread.start();
  }

  private final TimerThread thread;

  public Timer(){
    this(false);
  }

  public Timer(boolean daemon){
    thread = new TimerThread(this, daemon);
    synchronized (phantomReferences) {
      phantomReferences.add(new TimerPhantomReference(this, queue, thread));
    }
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
    thread.scheduleAtTime(task, date.getTime(), -1, false, true);
  }

  public void schedule(TimerTask task, Date date, long period){
    if(period <= 0){
      throw new IllegalArgumentException();
    }
    thread.scheduleAtTime(task, date.getTime(), period, false, true);
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
    thread.scheduleAtTime(task, date.getTime(), period, true, true);
  }

  public void scheduleAtFixedRate(TimerTask task, long delay, long period){
    if(period <= 0){
      throw new IllegalArgumentException();
    }
    thread.schedule(task, delay, period, true);
  }
}
