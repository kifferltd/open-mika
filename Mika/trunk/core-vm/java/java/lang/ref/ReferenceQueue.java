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
** $Id: ReferenceQueue.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.lang.ref;

import java.util.LinkedList;

/**
 ** Straightforward implementation using a LinkedList.
 ** We supply a package-visible method `append' which is used by a Reference
 ** to add itself to the queue.
 */
public class ReferenceQueue {

  /**
   ** The reference queue.
   */
  private LinkedList q;

  /**
   ** Default constructor: create an empty queue.
   */
  public ReferenceQueue() {
    q = new LinkedList();
  }
 
  /**
   */
  public synchronized Reference poll() {
    if (q.size() > 0) {
      Reference ref = (Reference)q.removeFirst();
      ref.queued = false;
      return ref;
    }

    return null;
  }

  public synchronized Reference remove(long timeout) throws IllegalArgumentException, InterruptedException {
    long now = System.currentTimeMillis();
    long deadline = now + timeout;
    long remaining = timeout;

    while (remaining > 0) {
      if (q.size() > 0) {
        Reference ref = (Reference)q.removeFirst();
        ref.queued = false;
        return ref;
      }
      this.wait(remaining);
      remaining = deadline - System.currentTimeMillis();
    }

    return null;
  }
  
  public synchronized Reference remove() throws IllegalArgumentException, InterruptedException {
    while (true) {
      if (q.size() > 0) {
        Reference ref = (Reference)q.removeFirst();
        ref.queued = false;
        return ref;
      }
      this.wait();
    }
  }
  
  synchronized void append(Reference item) {
    q.addLast(item);
    this.notify();
  }
  
}
