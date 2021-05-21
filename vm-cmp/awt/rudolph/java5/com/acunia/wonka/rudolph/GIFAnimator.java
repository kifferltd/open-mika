/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
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

