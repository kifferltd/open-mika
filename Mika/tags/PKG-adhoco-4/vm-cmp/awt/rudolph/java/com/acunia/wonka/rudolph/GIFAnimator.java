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

package com.acunia.wonka.rudolph;

import java.awt.image.*;
import java.util.*;

class GIFAnimator extends Thread {

  private WeakHashMap producers;
  private int         tick = 0;
  
  private static GIFAnimator ga = null;

  public static GIFAnimator getInstance() {
    if(ga == null) ga = new GIFAnimator();
    return ga;
  }
  
  private GIFAnimator() {
    super("GIF Animator");

    producers = new WeakHashMap();

    setPriority(7);
    start();
  }

  public void registerGIF(GIFImageSource producer, ImageConsumer consumer) {
    WeakHashMap consumers = (WeakHashMap)producers.get(producer);
    if(consumers == null) {
      consumers = new WeakHashMap();
      producers.put(producer, consumers);
    }

    if(!consumers.containsKey(consumer)) {
      consumers.put(consumer, null);
    }
  }

  public void startAnimator() {
    synchronized(this) {
      this.notify();
    }
  }

  private boolean checkProducers() { 
    boolean result = false;
    
    Iterator p = producers.keySet().iterator();
    GIFImageSource producer;
    while(p.hasNext()) {
      producer = (GIFImageSource)p.next();
      if(producer != null) {
        if(tick >= producer.next_event) {
          WeakHashMap consumers = (WeakHashMap)producers.get(producer);
          if(consumers != null) {
            result |= producer.produceNextFrame(consumers, tick);
          }
        }
        else {
          result = true;
        }
      }
    }
    
    return result;
  }

  public void run() {
    boolean keepOnGoing = false;
    
    while(true) {
      try {
        synchronized(this) {
          if(!keepOnGoing) {
            wait();
          }
        }

        sleep(50);
        
        keepOnGoing = checkProducers();

        if(!keepOnGoing) {
          if(producers.size() == 0) {
            ga = null;
            return;
          }
        }

        tick++;
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
  
}

