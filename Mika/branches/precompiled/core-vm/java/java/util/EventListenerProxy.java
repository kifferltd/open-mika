/**
 * Copyright  (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * $Id: EventListenerProxy.java,v 1.1 2006/04/18 11:35:28 cvs Exp $
 */
package java.util;

/**
 * EventListenerProxy:
 *
 * @author ruelens
 *
 * created: Mar 29, 2006
 */
public abstract class EventListenerProxy implements EventListener {

  private EventListener listener;

  public EventListenerProxy(EventListener listener) {
    this.listener = listener;
  }

  public EventListener getListener() {
    return this.listener;
  }

  
}
