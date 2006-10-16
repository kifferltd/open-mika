/**************************************************************************
* Copyright  (c) 2002 by Acunia N.V. All rights reserved.                 *
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


/*
** $Id: Heartbeat.java,v 1.2 2006/10/13 13:40:33 cvs Exp $
*/

package wonka.vm;

import java.lang.reflect.Method;

public final class Heartbeat implements Runnable {

  private static final boolean DEBUG = false;

  private static Heartbeat theHeartbeat;

  private static final long PERIOD = 60000 / 72;

  private static Runtime theRuntime;

  private Thread thread;

  private Method  shutdownMethod;

  /**
   ** Our very private constructor.  Start our thread running.
   */
  private Heartbeat() {
    create();
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

  private native void create();

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
    while(true) {
      try {
        Thread.sleep(PERIOD);
      }
      catch (InterruptedException ie) {
      }

      if(isKilled()) {
        try {
          shutdownMethod.invoke(theRuntime, new Object[0]);
          theRuntime.exit(-1);
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
      }

      if (numberNonDaemonThreads() == 0) {
        try {
          if (DEBUG) {
            System.out.println("Heartbeat: no non-daemon threads are running, invoking shutdown");
          }
          shutdownMethod.invoke(theRuntime, new Object[0]);
          theRuntime.exit(0);
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
        finally {
          theRuntime.exit(1);
        }
      }
    }
  }

  private static native void setThread(Thread thread);
  
  private static native int numberNonDaemonThreads();
  
  private static native boolean isKilled();
}

