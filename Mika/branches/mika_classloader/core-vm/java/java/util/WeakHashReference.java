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
** $Id: WeakHashReference.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.util;

import java.lang.ref.*;

class WeakHashReference extends java.lang.ref.WeakReference implements Map.Entry {

  int hashValue;
  Object value;

  WeakHashReference(Object key, Object value, ReferenceQueue queue){
    super(key, queue);
    this.value = value;
    hashValue = key.hashCode();
  }

  public synchronized boolean enqueue() {
    value = null;
    return super.enqueue();
  }

  public boolean equals (Object o) {
    boolean answer = false;
    if (o instanceof WeakHashReference) {
      Object key = get();
      if(key != null){
        WeakHashReference e2 = (WeakHashReference)o;
        answer = key.equals(e2.get()) && (value == null ? e2.value == null : value.equals(e2.value));
      }
    }
    return answer;
  }


  public int hashCode() {
    return hashValue ^ (value==null ? 0 : value.hashCode());
  }

  public Object getKey() {
    Object key = get();

    return (key == WeakHashMap.nullKey ? null : key);
  }

  public Object getValue() {
    return this.value;
  }

  public Object setValue(Object o) {
    Object oldval = value;
    value = o;
    return oldval;
  }
}
