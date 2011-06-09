/**************************************************************************
* Parts copyright (c) 2002 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2011 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                         *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/
 
package wonka.vm;

import java.lang.reflect.Method;

public final class Heartbeat implements Runnable {

  private static final boolean DEBUG = false;

  private static Heartbeat theHeartbeat;

  private static final long PERIOD = 60000 / 72;

  private static Runtime theRuntime;

  private Thread thread;

  private Method  shutdownMethod;

  public static native void collectTimeOffset();

  public static native long getTimeOffset();

  /**
   ** Our very private constructor.  Start our thread running.
   */
  private Heartbeat() {
    create(Boolean.getBoolean("mika.detect.deadlocks"));
    try {
      theRuntime = Runtime.getRuntime();
      shutdownMethod = Runtime.class.getDeclaredMethod("runShutdownHooks", new Class[0]);
      shutdownMethod.setAccessible(true);
    }
    catch (Throwable t) {
      t.printStackTrace();
    }

    thread = new Thread(this, "Heartbeat");
    thread.setPriority(1);
    thread.setDaemon(true);
    setThread(thread);
    thread.start();
  }

  private native void create(boolean detectDeadlocks);

  /**
   ** Return the solitary instance of Heartbeat.
   */
  public static synchronized Heartbeat getInstance() {
    if (theHeartbeat == null) {
      try {
        theHeartbeat = new Heartbeat();
      }
      catch (Throwable t) {
        t.printStackTrace();
      }
      finally {
        if (theHeartbeat == null) {
          theRuntime.exit(1);
        }
      }
    }

    return theHeartbeat;
  }

  /**
   ** The run() method
   */
  public void run() {
    boolean shutdown = false;
    int rc = 0;

    while(!shutdown) {
      nativesleep(PERIOD);

      if(isKilled()) {
        if (DEBUG) {
          System.out.println("Heartbeat: fatal signal received, invoking shutdown");
        }
        rc = 2;
        shutdown = true;
      }
      else if (numberNonDaemonThreads() == 0) {
        if (DEBUG) {
          System.out.println("Heartbeat: no non-daemon threads are running, invoking shutdown");
        }
        shutdown = true;
      }
    }

    try {
      // For an orderly shutdown we give finalizers a chance to run.
      // (Not mandated by spec, but not forbidden either).
      if (rc == 0) {
        theRuntime.runFinalization();
      }
      shutdownMethod.invoke(theRuntime, new Object[0]);
      theRuntime.exit(rc);
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
    finally {
      theRuntime.exit(1);
    }
  }

  private static native void setThread(Thread thread);
  
  private static native int numberNonDaemonThreads();
  
  private static native boolean isKilled();

  private static native void nativesleep(long period);
}

