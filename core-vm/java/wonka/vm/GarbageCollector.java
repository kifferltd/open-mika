/**************************************************************************
* Copyright (c) 2007, 2009, 2015, 2016, 2022 by Chris Gray, KIFFER Ltd.   *
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

package wonka.vm;

public final class GarbageCollector implements Runnable {

  /**
   ** The unique instance of this class (it's a singleton).
   */
  private static GarbageCollector theGarbageCollector;

  /**
   ** The associated Finalizer singleton.
   */
  private static  Finalizer theFinalizer;

  /**
   ** Be verbose if this flag is true (set from wonka.properties).
   */
  private static boolean verbose = false;

  /**
   ** The priority at which the garbage collection thread is started.
   */
  private static final int INITIAL_PRIORITY = 5;

  /**
   ** The minimum priority at which the GC thread will run.
   */
  private static final int MIN_PRIORITY = 5; // CG 20040428 : WAS : 1

  /**
   ** The maximum priority at which the GC thread will run.
   */
  private static final int MAX_PRIORITY = 10;

  /**
   ** HIGHER_PRIORITY_THRESHOLD: when the occupancy of the heap rises above this
   ** percentage, we start to increase the priority of the gc thread.
   */
  private static final float HIGHER_PRIORITY_THRESHOLD = 0.60f;

  /**
   ** LOWER_PRIORITY_THRESHOLD: when the occupancy of the heap falls below this
   ** percentage, we start to gradually reduce the priority of the gc thread.
   */
  private static final float LOWER_PRIORITY_THRESHOLD = 0.40f;

  /**
   ** Priority of the GC thread.  Updated after each cycle, depending
   ** on the occupancy of the cache.  Boosted to MAX_PRIORITY each time
   ** we are `kicked'.  (N.B. a `kick' can also come from native code).
   */
  private int priority = INITIAL_PRIORITY;

  /**
   ** Numver of GC cycles completed so far.
   */
  private int passes;

  /**
   ** Number of times we have been `kicked'.
   */
  private int kicks;


  /*
  ** The timestamp at the start of the most recent sleep.
  */
  private long timestamp_previous;

  /**
   ** The amount of memory available at the start of the most recent sleep.
   */
  private long available_previous;

  /**
   ** Time to sleep between GC cycles.  Updated after each cycle, depending
   ** on whether our collection rate is rising or falling.
   */
  private int sleep_millis = 10000;

  /**
   ** The minimum time for which GC will sleep between cycles.
   ** Can be adjusted using setMinSleep().
   */
   private int min_sleep_millis = 1000;

  /**
   ** The maximum time for which GC will sleep between cycles.
   ** Can be adjusted using setMaxSleep().
   */
   private int max_sleep_millis = 60000;


  /**
   ** The identity of the thread which performs GC.
   */
  private Thread thread;

  /**
   ** Create the native code structures used by GC.
   */
  private native void create();

  /**
   ** Implement verbosity
   */
  private static void debug(String s) {
    if (verbose) {
      System.err.println(s);
    }
  }

  /**
   ** Return the solitary instance of GarbageCollector.
   */
  public static synchronized GarbageCollector getInstance() {
    if (theGarbageCollector == null) {
      String debugProperty = GetSystemProperty.MIKA_VERBOSE;
      if (debugProperty.indexOf("gc") >= 0) {
        verbose = true;
      }

      theGarbageCollector = new GarbageCollector();

      synchronized (theGarbageCollector) {
        while (theFinalizer == null) {
          try {
            theGarbageCollector.wait(1000);
          }
          catch (InterruptedException ie) {}
        }
      }
    }

    return theGarbageCollector;
  }

  /**
   ** Initialise variables, launch finalisation thread. This method is called
   ** from run(), and getInstance() will not return until this method has
   ** completed (so we don't proceed with a half-initialied GC system).
   */
  private void init() {
    create();
    theFinalizer = Finalizer.getInstance();
    Thread thread = new Thread(theFinalizer, "Confessor");
    thread.setPriority(10);
    thread.setDaemon(true);
    thread.start();
  }

  /**
   ** Our very private constructor.
   */
  private GarbageCollector() {
    debug("GC: Starting Undertaker thread at priority " + priority + ", sleep time = " + sleep_millis + " milliseconds");
    thread = new Thread(this, "Undertaker");
    thread.setPriority(priority);
    thread.setDaemon(true);
    thread.start();
  }

  /**
   ** Run the native code which performs a GC cycle.
   */
  private native void collect();

  /**
   ** Kick the GarbageCollector, make it do some work instead of sleeping.
   ** Will throw NullPointerException if GC thread is not yet started.
   */
  public synchronized void kick() {
    debug("GC: kicked into action by thread " + Thread.currentThread());
    if (kicks < 3) {
      kicks += 3;
    }
    this.notifyAll();
  }

  /**
   ** Wait for the GarbageCollector to do a certain number of passes.
   ** Will throw NullPointerException if GC thread is not yet started.
  public synchronized void work(int numPasses) {
    debug("GC: thread " + Thread.currentThread() + " is waiting until " + numPasses + " have been performed");
    int wait_until = passes + numPasses;
    while (passes - wait_until > 0) {
      try {
        wait();
      }
      catch (InterruptedException ie) {
        debug("GC: thread " + Thread.currentThread() + " was interrupted while waiting");
      }
    }
    debug("GC: thread " + Thread.currentThread() + " resuming after " + (passes -wait_until) + " were performed");
  }
   */

  /**
   ** Ask GC to free up a certain number of bytes of memory.
   ** Returns the number of bytes which were really freed 
   ** (can be more or less).
   */
  public native synchronized int request(int bytes);

  public void setMinSleep(int millis) {
    min_sleep_millis = millis;
  }

  public void setMaxSleep(int millis) {
    max_sleep_millis = millis;
  }

  public void run() {
    long slept_for_millis;
    long memory_consumed;
    long bytes_per_second = 1000;

    init();

    timestamp_previous = System.currentTimeMillis();
    available_previous = memAvail();
    slept_for_millis = sleep_millis;
    memory_consumed = available_previous - memAvail();
    bytes_per_second = memory_consumed * 1000 / slept_for_millis;

    while (true) {
      try {
        priority = thread.getPriority();

        float heap_total = memTotal();
        float heap_avail = memAvail();
        float heap_occupancy = (heap_total - heap_avail) / heap_total;
        debug("GC: before collection: " + (int)heap_avail + " bytes available out of " + (int)heap_total + ", occupancy = " + (int)(heap_occupancy * 100.0) + "%");
        synchronized(this) {
          ++passes;
          collect();
          this.notifyAll();
        }
        synchronized(theFinalizer) {
          theFinalizer.notifyAll();
        }
        heap_total = memTotal();
        heap_avail = memAvail();
        heap_occupancy = (heap_total - heap_avail) / heap_total;
        debug("GC: after collection: " + (int)heap_avail + " bytes available out of " + (int)heap_total + ", occupancy = " + (int)(heap_occupancy * 100.0) + "%");

        // Calculate the duration of the next sleep based on the predicted
        // time to exhaust memory, multiplied by (1-occupancy)^2. Apply some 
        // smoothing against sudden lengthening of the interval.
        int new_sleep_millis;
	if (bytes_per_second * slept_for_millis > 0 ) {
	  float time_to_exhaustion = (available_previous - memory_consumed) / bytes_per_second;
          float one_minus_rho_squared = (1.0F - heap_occupancy) * (1.0F - heap_occupancy);
	  new_sleep_millis = (int)(1000.0F * time_to_exhaustion * one_minus_rho_squared);
	  debug("GC: available memory was " + available_previous + " bytes, less " + memory_consumed + " leaves " + (available_previous - memory_consumed));
	  debug("GC: predicted time to exhaustion = " + time_to_exhaustion + " seconds, multiplier = " + one_minus_rho_squared);
	}
	else {
          new_sleep_millis = max_sleep_millis;
	}
	sleep_millis = new_sleep_millis;
        if (sleep_millis < min_sleep_millis) {
          sleep_millis = min_sleep_millis;
        }
        if (sleep_millis > max_sleep_millis) {
          sleep_millis = max_sleep_millis;
        }
  
        priority = (int)(heap_occupancy * 4.0F) + 5; // CG 20040428 : WAS : 9.0F, + 1
        thread.setPriority(priority);

        debug("GC: Will now sleep for " + sleep_millis + " milliseconds, priority is " + priority);

        timestamp_previous = System.currentTimeMillis();
        available_previous = memAvail();
	slept_for_millis = 0;
	while (slept_for_millis < sleep_millis && kicks == 0) {
          try {
            synchronized(this) {
              this.wait(sleep_millis);
            }
          }
          catch (InterruptedException ie) {
          }
          slept_for_millis = System.currentTimeMillis() - timestamp_previous;
        }
        if (slept_for_millis > 0) {
          memory_consumed = available_previous - memAvail();
          debug("GC: Slept for " + slept_for_millis + " milliseconds");
          debug("GC: During this time " + memory_consumed + " bytes of memory were consumed");
          bytes_per_second = memory_consumed * 1000 / sleep_millis;
          // bytes_per_second = memory_consumed * 1000 / slept_for_millis;
          debug("GC: That makes " + bytes_per_second + " bytes per second");
          if (bytes_per_second > 1000 && memory_consumed * 1000 > slept_for_millis) {
            debug("GC: At this rate we will run out of memory in " + ((available_previous - memory_consumed) / (memory_consumed * 1000 / slept_for_millis)) + " seconds");
          }
          else {
            debug("GC: At this rate we will (almost) never run out of memory");
            bytes_per_second = 1000;
            try {
              synchronized(this) {
                this.wait(max_sleep_millis);
              }
            }
            catch (InterruptedException ie) {
            }
          }
        }
 
        if (kicks > 0) {
          debug("GC: responding to kick");
//          sleep_millis = sleep_millis / kicks;
          sleep_millis = min_sleep_millis;
          --kicks;
          synchronized(this) {
            ++passes;
            collect();
            this.notifyAll();
          }
          synchronized(theFinalizer) {
            theFinalizer.notifyAll();
          }
        }
 
      }
      catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  static native long memTotal();
  static native long memAvail();

}

