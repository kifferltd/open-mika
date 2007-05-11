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
* Solutions. Permission is hereby granted to reproduce, modify, and       *
* distribute these modifications under the terms of the Wonka Public      *
* Licence.                                                                *
**************************************************************************/

/*
** $Id: Dispatcher.java,v 1.1 2005/06/14 08:48:24 cvs Exp $
*/

package com.acunia.wonka.rudolph;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.util.Stack;

/**
 ** Rudolph's internal event queue dispatcher.
 */
public final class Dispatcher implements Runnable {

  /**
   ** If wonka.rudolph.queue.events is true (the default), keyboard/pointer
   ** events are enqueued by the scanner thread (Haigha) and dequeued by the
   ** dispatching thread (Hatta). If wonka.rudolph.queue.events=false,
   ** the scanner thread dispatches events directly.
   */
  static boolean queueing_enabled;

  /**
   ** Thread priority when dispatching events.
   */
  private static final int dispatch_priority;

  /**
   ** Reference to the master instance of this class.
   */
  private static Dispatcher mainDispatcher;

  /**
   ** Reference to our colleague the (current) event queue.
   */
  private static EventQueue currentEventQueue;

  /**
   ** Sequence number so we can distinguish instances of Hatta.
   */
  private static int seq;

  /**
   ** Stack of Thread objects - the top element is the ancestor of the current dispatch thread.
   */
  private static Stack threadStack;

  /**
   ** The current dispatch thread. Used (together with <code>threadStack</code>)
   ** to implement <code>isDispatchThread()</code>. Note that if queueing is
   ** disabled this will actually be a Scanner thread, not one of "ours".
   */
  private static Thread currentDispatchThread;

  /**
   ** The Java thread corresponding to this instance.
   */
  private Thread ourThread;

  /**
   ** Reference to our colleague the Scanner.
   */
  private Scanner ourScanner;

  /**
   ** The name of this Dispatcher.
   */
  private String name;

  /**
   ** State of the dispatching thread - takes one of the THREAD_xxx values below.
   */
  private int threadState;

  private static final int THREAD_UNSTARTED = 0;

  private static final int THREAD_STARTING = 1;

  private static final int THREAD_RUNNING = 2;

  private static final int THREAD_STOPPING = 3;

  private static final int THREAD_STOPPED = 4;

  /**
   ** Number of milliseconds to wait for a thread state change.
   */
  private static final long WAIT_MILLIS = 250;

  /**
   ** Has the corresponding Scanner already been started?
   */
  private boolean scannerRunning;

  /**
   ** Current time in millis at the time this instance was created.
   */
  private long timeCreated;

  /**
   ** The static initialiser creates the master instance of this class and
   ** the EventQueue and Scanner singletons.
   */
  static {
    queueing_enabled = !"false".equalsIgnoreCase(System.getProperty("wonka.rudolph.queue.events", "true").trim());
    dispatch_priority = Integer.getInteger("wonka.rudolph.dispatch.priority", 8).intValue();
    mainDispatcher = new Dispatcher(null);
    currentEventQueue = new EventQueue();
    init(currentEventQueue);
  }

  /**
   ** Native part of the static initialiser.
   */
  private native static void init(EventQueue queue);

  /**
   ** Compute name of this instance. 
   */
  private static synchronized void prepare(Dispatcher instance, Window root) {
    instance.name = "Hatta";
    if (seq > 0) {
      instance.name += "-" + seq + "(" + root + ")";
    }
    seq += 1;
  }

  public static Dispatcher getMainDispatcher() {
    return mainDispatcher;
  }

  public Dispatcher(Window root) {
    timeCreated = System.currentTimeMillis();
    ourScanner = new Scanner(this, root);
    prepare(this, root);
  }

  /**
   ** Dispatcher main loop. Only runs if queueing is enabled.
   */
  public void run() {
    synchronized(this) {
      threadState = THREAD_RUNNING;
      notifyAll();
    }

    boolean synched = false;
    AWTEvent event;

    try {
      while(threadState == THREAD_RUNNING) {
        long timestamp = 0;
        try {
          if (ourThread == currentDispatchThread) {
            event = currentEventQueue.getNextEvent();
            if (!synched) {
              if (event instanceof InputEvent) {
                timestamp = ((InputEvent)event).getWhen();
                if (timestamp >= timeCreated) {
                  synched = true;
                }
              }
            }
            if (synched || !(event instanceof InputEvent)) {
              currentEventQueue.dispatchEvent(event);
            }
          }
          else {
            try {
              Thread.sleep(WAIT_MILLIS);
            }
            catch (InterruptedException ie) {
            }
          }
        }
        catch (InterruptedException ie) {
        }
        catch (Exception exc) {
          try {
            exc.printStackTrace();
          }
          catch (Exception exc2) {
          }
        }
        catch (Error err) {
          try {
            err.printStackTrace();
          }
          finally {
            throw err; // this will kill thread
          }
        }
      }
    }
    finally {
      synchronized(this) {
        threadState = THREAD_STOPPED;
        notifyAll();
      }
    }
  }

  static synchronized void pushThread(Thread t) {
    if (threadStack == null) {
      threadStack = new Stack();
    }
    else {
      threadStack.push(currentDispatchThread);
    }
    currentDispatchThread = t;
  }

  static synchronized void popThread() {
    currentDispatchThread = (Thread)threadStack.pop();
  }

  /**
   ** Start the main loop.
   */
  public void start() {
    if(!scannerRunning) {
      scannerRunning = true;
      ourScanner.push();
      if (queueing_enabled && threadState < THREAD_STARTING) {
        threadState = THREAD_STARTING;
        ourThread = new Thread(this, name);
        ourThread.setPriority(dispatch_priority);
        ourThread.setDaemon(false);
        ourThread.start();
        pushThread(ourThread);
        synchronized(this) {
          while (threadState < THREAD_RUNNING) {
            try {
              wait(WAIT_MILLIS);
            }
            catch (InterruptedException ie) {
            }
          }
        }
      }
    }
  }
  
  /**
   ** Stop the main loop.
   */
  public void stop() {
    if (scannerRunning) {
      ourScanner.pop();
      scannerRunning = false;
    }
    if (queueing_enabled && threadState == THREAD_RUNNING) {
      threadState = THREAD_STOPPING;
      ourThread.interrupt();
      synchronized(this) {
        while (threadState < THREAD_STOPPED) {
          try {
            wait(WAIT_MILLIS);
          }
          catch (InterruptedException ie) {
          }
        }
      }
      popThread();
    }
  }

  /**
   ** Empty the queue (and any type-ahead buffers etc. as well).
   */
  public static void drain() {
    synchronized (mainDispatcher) {
      while (currentEventQueue.peekEvent() != null) {
        try {
          currentEventQueue.getNextEvent();
        }
        catch (InterruptedException ie) {
        }
      }
      Scanner.drain();
    }
  }

  /**
   ** Is Thread t our dispatching thread?
   */
  public static boolean isDispatchThread(Thread t) {
    return t == currentDispatchThread;
  }
}

