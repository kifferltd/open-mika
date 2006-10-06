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
* Modifications copyright (c) 2005 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/


/*
** $Id: Reference.java,v 1.5 2006/03/29 09:27:14 cvs Exp $
*/

package java.lang.ref;

public abstract class Reference {

  ReferenceQueue ref_queue;

  /**
  ** true if the Reference is in a queue, false otherwise ...
  */
  boolean queued;
  /**
  ** a reference can only be queued once.
  ** Holds true if this reference has been put in a queue, false otherwise ...
  */
  boolean hasBeenQueued;

  /**
   ** In the constructor we check that the programmer has used one of the
   ** "official" subclasses [Soft|Weak|Phantom]Reference. Our GC depends on
   ** this.
   */
  Reference() {
    if (!(this instanceof SoftReference) && !(this instanceof WeakReference) && !(this instanceof PhantomReference)) {
      throw new SecurityException("Forbidden to directly subclass java.lang.ref.Reference");
    }
  }

  public native Object get();
  
  public native void clear();
  
  public synchronized boolean isEnqueued() {
    return queued;
  }

  public synchronized boolean enqueue() {
    if (hasBeenQueued || ref_queue == null) {
      return false;
    }
    hasBeenQueued = true;
    queued = true;
    ref_queue.append(this);
    return true;
  }

}
