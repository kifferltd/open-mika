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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


package java.awt;

public class EventDispatchThread implements Runnable {

  private EventQueue eventQueue;
  private Thread dispatcher;

  EventDispatchThread(EventQueue queue) {
    this.eventQueue = queue;
    dispatcher = new Thread(this, "EventDispatchThread");
    dispatcher.start();
    dispatcher.setPriority(Thread.NORM_PRIORITY + 3);
  }

  public synchronized void setEventQueue(EventQueue queue) {
    this.eventQueue = queue;
  }

  public void doCheck() {
    if (dispatcher != null) {
      synchronized (this.dispatcher) {
        dispatcher.notify();
      }
    }
  }

  public void run() {
    dispatcher.setPriority(Thread.NORM_PRIORITY + 3);
    synchronized (this.dispatcher) {
      while (true) {
        try {
          dispatcher.wait();
          if ((eventQueue != null) && (eventQueue.peekEvent() != null)) eventQueue.dispatchEvent(eventQueue.getNextEvent());
          else System.err.println("java.awt.EventDispatchThread: warning: something whicked happend");
        }
        catch (Exception e) {
          System.err.println("ERROR: catched InterruptedException in java.awt.EventDispatchThread.");
        }
      }
    }
  }
}
