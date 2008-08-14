/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2004 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
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

public final class Finalizer implements Runnable {

  private static Finalizer theFinalizer;

  private static final int IDLE_WAIT_MILLIS = 1000;

  private Object[] empty;
  private java.lang.reflect.Method finalizer_method;

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

      if (idle) {
        finalized_count = 0;
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
   ** Poll the finalize() fifo.
   */
  private native Object nextFinalizee();

  /**
   ** Tell GC that object has been finalized.
   */
  private native void finalized(Object o);

}

