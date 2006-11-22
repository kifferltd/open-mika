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

  private ReferenceQueue ref_queue;

  /**
  ** true if the Reference is in a queue, false otherwise ...
  ** NOTE: this will be set via native code !!!
  */
  private boolean queued;

  /**
   ** In the constructor we check that the programmer has used one of the
   ** "official" subclasses [Soft|Weak|Phantom]Reference. Our GC depends on
   ** this.
   * @param referent 
   */
  Reference(Object referent) {
    set(referent);
  }

  Reference(Object referent, ReferenceQueue queue) {
    set(referent);
    ref_queue = referent == null ? null : queue;
  }

  public native Object get();
  
  public native void clear();
  
  public boolean isEnqueued() {
    return queued;
  }

  public boolean enqueue() {
    if (ref_queue == null) {
      return false;
    }
    return ref_queue.append(this);
  }  
  
  private native void set(Object referent);

}
