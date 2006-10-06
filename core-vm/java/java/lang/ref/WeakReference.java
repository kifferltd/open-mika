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
** $Id: WeakReference.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.lang.ref;

public class WeakReference extends Reference {

  public WeakReference(Object referent) {
    if(referent == null){
      throw new NullPointerException();
    }
    set(referent);
  }

  public WeakReference(Object referent, ReferenceQueue queue) {
    if(referent == null || queue == null){
      throw new NullPointerException();
    }
    set(referent);
    this.ref_queue = queue;
  }
  
  private native void set(Object referent);

/*  public boolean enqueue() {
    boolean result = super.enqueue();
    clear();
    return result;
  }*/
}
