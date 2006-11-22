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


/**
 ** Straightforward implementation using a LinkedList.
 ** We supply a package-visible method `append' which is used by a Reference
 ** to add itself to the queue.
 */
public class ReferenceQueue {

  /**
   ** Default constructor: create an empty queue.
   */
  public ReferenceQueue() {
    create();
  }
 
  /**
   */
  public native Reference poll(); 

  public Reference remove(long timeout) throws IllegalArgumentException, InterruptedException {
    if(timeout < 1) {
      if(timeout == 0) {
        return remove();
      }
      throw new IllegalArgumentException();
    }
    return _remove(timeout);
  }
  

  public native Reference remove() throws InterruptedException; 
  
  native boolean append(Reference item);
  private native void create();
  private native Reference _remove(long timeout); 

}
