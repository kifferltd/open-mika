/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004, 2005 by Chris Gray, /k/ Embedded Java *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/


/*
** $Id: GarbageCollector.java,v 1.8 2005/09/03 12:11:33 cvs Exp $
*/

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
      String debugProperty = System.getProperty("mika.verbose","");
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
  private synchronized void init() {
    create();
    theFinalizer = Finalizer.getInstance();
    notifyAll();
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
    // [CG 20050620] WAS: thread.interrupt();
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

