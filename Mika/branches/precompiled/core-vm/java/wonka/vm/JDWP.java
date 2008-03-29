/**************************************************************************
* Copyright (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                                    *
**************************************************************************/


/*
** $Id: JDWP.java,v 1.1 2006/10/04 14:24:15 cvsroot Exp $
*/

package wonka.vm;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Vector;

public final class JDWP extends Thread {

  private static JDWP theJDWP;

  private static native boolean isEnabled();

  private static native boolean isRunning();

  /** A Vector of WeakReference's to all existing instances of ClassLoader.
   */
  private static Vector refsToClassLoaders;

  /** A reference queue used to clean up dead WeakReferences.
   */
  private static ReferenceQueue refQ;

  /** Remove from refsToClassLoaders all references which have been cleared.
   */
  private static void purgeClassLoaders() {
    synchronized(refsToClassLoaders) {
      WeakReference wr = (WeakReference)refQ.poll();
      while (wr != null) {
        refsToClassLoaders.remove(wr);
        wr = (WeakReference)refQ.poll();
      }
    }
  }

  /** Add a WeakReference to a ClassLoader to refsToClassLoaders.
   */
  public static synchronized void registerClassLoader(ClassLoader cl) {
    if (!isEnabled()) {
      return;
    }

    if (refQ == null) {
      refQ = new ReferenceQueue();
      refsToClassLoaders = new Vector();
    }
    else {
      purgeClassLoaders();
    }
    refsToClassLoaders.add(new WeakReference(cl, refQ));
  }

  public static synchronized JDWP getInstance() {
    if (theJDWP == null) {
      theJDWP = new JDWP();
    }

    while (!isRunning()) {
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException ie) {
      }
    }

    return theJDWP;
  }

  private JDWP() {
    super("JDWP");
    setPriority(10);
    setDaemon(true);
    start();
  }

  public native void run();

}

