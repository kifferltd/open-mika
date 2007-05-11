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
** $Id: 
*/

package java.lang;

import java.util.WeakHashMap;

/**
 ** A ThreadLocal has a different value in each Thread which refers to it.
 ** In our implementation the values are actually stored in a Hashtable in
 ** the Thread itself.
 */
public class ThreadLocal {

  WeakHashMap threads = new WeakHashMap();

  public Object get() {
    Thread current_thread = Thread.currentThread();

    if (!threads.containsKey(current_thread)) {
      threads.put(current_thread, null);
      Object val = initialValue();
      current_thread.setThreadLocal(this,val);

      return val;

    }
    return current_thread.getThreadLocal(this);
  }

  public void set(Object val) {
    Thread current_thread = Thread.currentThread();

    if (!threads.containsKey(current_thread)) {
      threads.put(current_thread, null);
    }
    current_thread.setThreadLocal(this,val);
  }

  protected Object initialValue() {
    return null;
  }
}

