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

public final class Painter implements Runnable {

  private static Painter thePainter;

  private int priority = Thread.MAX_PRIORITY;
  boolean keepOnGoing;
  private Thread thread;
  private Thread shutdown;

  private Painter() {
  }

  public static synchronized Painter getInstance() {
    if (thePainter == null) thePainter = new Painter();
    return thePainter;
  }

  private native void paint(); 

  public void run() {
    while (keepOnGoing) {
      try {
        paint();
        Thread.sleep(20);
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
          System.out.println("Painter thread threw " + err + "!");
          err.printStackTrace();
        }
        finally {
          System.exit(253);
        }
      }
    }
  }

  public void start() {
    if(!keepOnGoing) {
      keepOnGoing = true;
      shutdown = new Thread("Painter shutdown hook") {
        public void run() {
          keepOnGoing = false;
        }
      };
      Runtime.getRuntime().addShutdownHook(shutdown);
      thread = new Thread(this, "Picasso");
      thread.setPriority(priority);
      thread.setDaemon(false);
      thread.start();
    }
  }

  public void stop() {
    Runtime.getRuntime().removeShutdownHook(shutdown);
    keepOnGoing = false;
    thread = null;
  }

}

