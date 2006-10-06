/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: Finalizer.java,v 1.5 2005/09/03 12:11:33 cvs Exp $
*/

package wonka.vm;

public final class Finalizer implements Runnable {

  private static Finalizer theFinalizer;

  private static final int IDLE_WAIT_MILLIS = 1000;

  private Object[] empty;
  private java.lang.reflect.Method finalizer_method;
  private java.lang.reflect.Method enqueue_method;

  /**
   ** Our very private constructor.  We look up the methodID's for the
   ** methods Reference/enqueue() and Object/finalize() and start our
   ** thread running.
   */
  private Finalizer() {

    try {
      Class java_lang_Object = Class.forName("java.lang.Object");
      finalizer_method = java_lang_Object.getDeclaredMethod("finalize", new Class[0]);
      finalizer_method.setAccessible(true);

      Class java_lang_ref_Reference = Class.forName("java.lang.ref.Reference");
      enqueue_method = java_lang_ref_Reference.getDeclaredMethod("enqueue", new Class[0]);
      enqueue_method.setAccessible(true);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    empty = new Object[0];
    Thread thread = new Thread(this, "Confessor");
    thread.setPriority(10);
    thread.setDaemon(true);
    thread.start();

  }

  /**
   ** Return the solitary instance of Finalizer.
   */
  public static synchronized Finalizer getInstance() {
    if (theFinalizer == null) {
      theFinalizer = new Finalizer();
    }

    return theFinalizer;
  }

  /**
   ** The run() method polls the two internal fifo's and calls enqueue()
   ** or finalize() as appropriate.
   */
  public void run() {
    boolean idle;
    boolean verbose;
    int finalized_count = 0;
    int enqueued_count = 0;
    Object o;

    // Give GC thread time to start up, otherwise nextFinalizee() crashes (kludge).
    try {
      synchronized(this) {
        wait(IDLE_WAIT_MILLIS);
      }
    }
    catch (InterruptedException ie) {
    }

    verbose = (System.getProperty("mika.verbose", "").indexOf("gc") >= 0);

    while(true) {
      idle = true;
      synchronized(this) {
        o = nextFinalizee();
      }

      if (o == null) {
        synchronized(this) {
          notifyAll();
        }
      }
      else {
        idle = false;
        if (verbose) {
          System.err.println("GC: finalizing " + o);
        }
        try {
          finalizer_method.invoke(o, empty);
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
        finalized(o);
        ++finalized_count;
      }

      synchronized(this) {
        o = nextEnqueueee();
      }

      if (o == null) {
        synchronized(this) {
          notifyAll();
        }
      }
      else {
        idle = false;
        if (verbose) {
          System.err.println("GC: enqueuing " + o);
        }
        try {
          enqueue_method.invoke(o, empty);
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
        enqueued(o);
        ++enqueued_count;
      }

      if (idle) {
        finalized_count = 0;
        enqueued_count = 0;
        synchronized(this) {
          try {
            wait(IDLE_WAIT_MILLIS);
          }
          catch (Throwable t) {
            t.printStackTrace();
          }
        }
      }
    }
  }

  /**
   ** Poll the enqueue() fifo.
   */
  private native Object nextEnqueueee();

  /**
   ** Poll the finalize() fifo.
   */
  private native Object nextFinalizee();

  /**
   ** Tell GC that object has been finalized.
   */
  private native void finalized(Object o);

  /**
   ** Tell GC that object has been enqueued.
   */
  private native void enqueued(Object o);
}

