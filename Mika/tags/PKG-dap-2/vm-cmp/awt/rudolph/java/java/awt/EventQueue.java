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
*                                                                         *
* Modifications copyright (c) 2004, 2005 by Chris Gray, /k/ Embedded Java *
* Solutions. Permission is hereby granted to reproduce, modify, and       *
* distribute these modifications under the terms of the Wonka Public      *
* Licence.                                                                *
**************************************************************************/

package java.awt;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;
import java.awt.event.*;
import com.acunia.wonka.rudolph.peers.*;
import com.acunia.wonka.rudolph.*;

/**
 ** A device-independent structure for queueing AWT events.
 ** Note that this implementation is really only intended for internal use
 ** by Rudolph, and may not behave as expected if used by other code. <= FIXME
 */
public class EventQueue {

  /**
   ** If wonka.rudolph.queue.events is true (the default), keyboard/pointer
   ** events are enqueued by the scanner thread (Haigha) and dequeued by the
   ** dispatching thread (Hatta). If wonka.rudolph.queue.events=false,
   ** the scanner thread dispatches events directly.
   */
  protected static boolean queueing_enabled = !"false".equalsIgnoreCase(System.getProperty("wonka.rudolph.queue.events", "true").trim());

  /**
   ** The logic for coalesceEvents() is executed iff both 
   ** wonka.rudolph.queue.events and wonka.rudolph.coalesce.events are true.
   ** Defaults to true, can be turned off using system property
   ** wonka.rudolph.coalesce.events=false.
   ** (For historical reasons we also allow coalescing to be turned off using
   ** wonka.rudolph.coalesce.event=false).
   */
  private static boolean coalescing_enabled = queueing_enabled && !"false".equalsIgnoreCase(System.getProperty("wonka.rudolph.coalesce.events", "true").trim()) && !"false".equalsIgnoreCase(System.getProperty("wonka.rudolph.coalesce.event", "true").trim());

  /**
   ** A native AWTEvent which was detected by Event.c and which should be
   ** appended to the queue when postNativeEvent() is called.
   */
  private AWTEvent nativeAWTEvent;

  /**
   ** The ArrayList used to hold the queue of events.
   */
  private ArrayList queue;

  /**
   ** A stack used by the push() and pop() methods of this class.
   */
  private static Stack queueStack = new Stack();

  /**
   * Constructor
   * @status  Complient with java specs
   * @remark  Complient with java specs
   */
  public EventQueue() {
    // We create the queue even if queueing isn't enabled; makes life so much easier ...
    queue = new ArrayList();
/*
    if (!queueing_enabled) {
      System.out.println(this + ": event queueing is disabled");
    }
    if (!coalescing_enabled) {
      System.out.println(this + ": coalesceEvents() is disabled");
    }
*/
  }

  /**
   ** Calls the dispatchEvent method of the event's source, passing the
   ** event as parameter. Events whose source is not a subclass of
   ** Component are silently ignored.
   ** N.B. FIXME This method should be protected, but is made public so that
   ** it can be called by com.acunia.wonka.rudolph.Dispatcher. The clean
   ** solution is to move the dispatching code into the java.awt package ...
   */
  public void dispatchEvent(AWTEvent event) {
    Object source = event.getSource();
    if (source instanceof Component) {
      ((Component)source).dispatchEvent(event);
    } 
  }

  /**
   * Get the next event from the queue. If the queue is empty, this method
   * blocks until an event is available. If event queueing is disabled,
   * this method just blocks forever.
   *
   * @status  Complient with java specs
   * @remark  Complient with java specs
   */
  public synchronized AWTEvent getNextEvent() throws InterruptedException {
    while (queue.isEmpty()) {
      //try {
        wait(1000);
      //}
     //catch (InterruptedException ie) {}
    }

    AWTEvent event = (AWTEvent) queue.get(queue.size() - 1);
    queue.remove(queue.size() - 1);

    return event;
  }
  
  /**
   ** Returns the first event on the event queue, or null if there is none.
   */
  public synchronized AWTEvent peekEvent() {
    if (queue.isEmpty()) {
      return null;
    }
    return (AWTEvent) queue.get(queue.size() - 1);
  }
  
  public synchronized AWTEvent peekEvent(int id) {
    System.out.println("EventQueue.peekEvent(id): TODO!");
    return null;
  }

  /**
   * Post a native event. The event to be posted is in nativeAWTEvent.
   * @status  Complient with java specs
   * @remark  Complient with java specs
   */
  public void postNativeEvent() {
    try {
      if (nativeAWTEvent != null) {
        postEvent(nativeAWTEvent);
        nativeAWTEvent = null;
      }
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
  }
  
  /**
   ** Post an event to the queue, or dispatch it directly if event queueing
   ** is disabled.
   ** If menu is open and the new event does not come from a MenuItemComponent 
   ** or MenuWindow then the open menu is first closed. If event queueing is
   ** disabled the event is then dispatched immediately. Otherwise, if event
   ** coalescing is enabled the new event is compared with each item currently
   ** in the queue (most recent first) to see if there is another event with
   ** the same source and ID; if so then the source Component's coalesceEvents()
   ** method is called with the old and new events as parameters. If 
   ** coalesceEvents() returns non-null, then the AWTEvent returned overwrites
   ** the event in the queue; otherwise the search continues. If no matching
   ** event is found, or all calls to coalesceEvents() return null, or event
   ** coalescing is disabled, the new event is appended to the queue.
   */ 
  public void postEvent(AWTEvent newEvent) {
    int newEventID = newEvent.getID();
    Component newEventSource = (Component)newEvent.getSource();

    if(newEventID == MouseEvent.MOUSE_PRESSED) {
      if(DefaultMenu.menuOpened) {
        if(!((newEventSource instanceof MenuItemComponent) ||
              (newEventSource instanceof MenuWindow)) && DefaultMenu.lastMenuOpened != null) {
          ((DefaultMenu)DefaultMenu.lastMenuOpened.getPeer()).closeChildren();
          ((DefaultMenu)DefaultMenu.lastMenuOpened.getPeer()).closeParents();
          ((DefaultMenu)DefaultMenu.lastMenuOpened.getPeer()).close();
          DefaultMenu.menuOpened = false;
        }
      }
    }

    if (!queueing_enabled) {
      dispatchEvent(newEvent);

      return;
    }

    synchronized (this) {
      if (coalescing_enabled) {
        for (int i = queue.size() - 1; i >= 0; --i) {
          AWTEvent oldEvent = (AWTEvent)queue.get(i);
          if (oldEvent.getID() == newEventID && oldEvent.getSource() == newEventSource) {
            AWTEvent mergedEvent = newEventSource.coalesceEvents(oldEvent, newEvent);
            if (mergedEvent != null) {
              queue.set(i, mergedEvent);
              notifyAll();

              return;

            }
          }
        }
      }

      queue.add(0, newEvent);
      notifyAll();
    }
  }

  /**
   ** Returns true iff the current thread is an AWT event dispatch thread.
   ** Works for Rudolph, but may not work for user-created event handlers.
   */
  public static boolean isDispatchThread() {
    return com.acunia.wonka.rudolph.Dispatcher.isDispatchThread(Thread.currentThread());
  }

  /**
   ** Push an event queue onto the stack. The new queue inherits all
   ** existing events. Untested?
   */
  public void push(EventQueue newEventQueue) {
    Dispatcher dispatcher = new com.acunia.wonka.rudolph.Dispatcher(null);
    dispatcher.start();
    queueStack.push(dispatcher);
  }

  /**
   ** Pop an event queue onto the stack. The previous queue inherits all
   ** existing events. Untested?
   */
  protected void pop() throws EmptyStackException {
    Dispatcher dispatcher = (Dispatcher)queueStack.pop();
    dispatcher.stop();
  }
}

