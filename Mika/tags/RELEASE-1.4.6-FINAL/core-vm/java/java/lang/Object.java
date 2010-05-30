/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
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


/*
** $Id: Object.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.lang;

/**
 ** The mother of all classes.
 **
 ** Note: this class is initialized "by hand" before the VM is fully
 ** initialized.  Consequently it must not have a static initializer.
 ** (It can have static variables, and even constant initial values
 ** for those variables, but nothing fancier and certainly no static{}
 ** clause.)
 */
public class Object {
  
  /**
   ** Get the class of which this object is an instance.
   */
  public final native Class getClass();

  /**
   ** Default String representation: overriden by many classes.
   */
  public String toString() {
    return this.getClass().getName()+"@"+Integer.toHexString(this.hashCode());
  }

  /**
   ** The (default) object hashcode: for transient objects this is simply the 
   ** address of the instance in memory, for persistent objects it is the
   ** address at which the instance was first created (maybe in a previous
   ** activation of the VM).
   */
  public native int hashCode();

  /**
   ** Default object equality (is identity).
   */
  public native boolean equals(Object obj);

  /**
   ** Default clone() method performs a "shallow" cloning of the instance.
   */
  protected native Object clone() 
    throws CloneNotSupportedException;

  /**
   ** Wait forever.
   */
  public final void wait() throws IllegalMonitorStateException, InterruptedException {
    wait(0, 0);
  }

  /**
   ** Wait for x ms, 0 ns.
   */
  public final void wait(long millis) throws IllegalMonitorStateException, InterruptedException {
    wait(millis, 0);
  }

  /**
   ** Wait on a monitor.
   */
  public final native void wait(long millis, int nanos) 
    throws IllegalMonitorStateException, InterruptedException;

  /**
   ** Notify (at most) one thread that is waiting on a monitor.
   */
  public final native void notify()
    throws IllegalMonitorStateException;

  /**
   ** Notify all threads (if any) that are waiting on a monitor.
   */
  public final native void notifyAll()
    throws IllegalMonitorStateException;

  /**
   ** The default finalizer does nothing.
   */
  protected void finalize()
    throws Throwable {}

  /**
   ** Private method which is called by the garbage collector (from native
   ** code) to invoke the finalizer of an object.
   */
  private static void do_finalize(Object o) {
    try {
      o.finalize();
    }
    catch (Throwable t) {
    }
  }

}  
