/**************************************************************************
* Copyright (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.          *
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
* 3. Neither the name of Chris Gray, /k/ Embedded Java Solutions nor the  *
*    names of other contributors may be used to endorse or promote        *
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL CHRIS GRAY OR OTHER CONTRIBUTORS BE LIABLE            *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

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

