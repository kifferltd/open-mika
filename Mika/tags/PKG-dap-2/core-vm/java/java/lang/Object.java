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
