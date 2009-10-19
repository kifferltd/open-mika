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
