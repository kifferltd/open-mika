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
