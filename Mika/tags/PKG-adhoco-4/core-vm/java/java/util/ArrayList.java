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


/**
 * $Id: ArrayList.java,v 1.3 2006/04/18 11:35:28 cvs Exp $
 */

package java.util;

import java.io.*;

public class ArrayList extends AbstractList 
  implements List, Cloneable, Serializable, RandomAccess {

  private static final long serialVersionUID = 8683452581122892189L;
  private transient Object[] elements;
  private int size;
  private static final int defaultCapacity = 10;

  private void writeObject (ObjectOutputStream s) throws IOException {
    s.defaultWriteObject();
    s.writeInt(elements.length);
    for (int i=0;i<size;i++) {
      s.writeObject(elements[i]);
    }              
  }

  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    int local_oc;
    s.defaultReadObject();
    local_oc = s.readInt();
    this.elements = new Object[local_oc];
    for (int i=0;i<size;i++) {
      this.elements[i] = s.readObject();
    }
  }    

  public ArrayList() {
    this(defaultCapacity);
  }

  public ArrayList(int initialCapacity) {
    this.elements = new Object[initialCapacity];
    this.size = 0;
  }

  public ArrayList(Collection c) {
    this((int)(c.size()*1.1f));
    Iterator it = c.iterator();
    while (it.hasNext()) {
      elements[this.size] = it.next();
      this.size++;
    }
  }

  public void trimToSize() {
    if (this.size < this.elements.length) {
      Object[] oldElements = this.elements;
//      modCount++;
      this.elements = new Object[this.size];
      System.arraycopy(oldElements, 0, this.elements, 0, this.size);
    }
  }

  public void ensureCapacity(int minCapacity) {
    if (this.elements.length < minCapacity) {
      Object[] oldElements = this.elements;
      int oldlength = this.size * 2;
      if (minCapacity < (oldlength)) {
        minCapacity = oldlength;
      }
      this.elements = new Object[minCapacity];
      // JV: the new size might me more than minCapacity
      System.arraycopy(oldElements, 0, this.elements, 0, oldlength>>>1);
    }
  }

  public int size() {
    return this.size;
  }

  public boolean isEmpty() {
    return (size == 0);
  }

  public boolean contains (Object elem) {
    int i=0;
    if (elem==null) {
	for (; i < size ; i++) {	
	   if (elements[i]==null) return true;
      	}
    }
    else {
	for (; i < size ; i++) {	
 	   if (elem.equals(elements[i])) return true;
      	}
    }
    return false;
  }

  public int indexOf (Object elem) {
    int i=0;
    if (elem==null) {
	for (; i < size ; i++) {	
	   if (elements[i]==null) return i;
      	}
    }
    else {
	for (; i < size ; i++) {	
 	   if (elem.equals(elements[i])) return i;
      	}
    }
    return -1;
  }

  public int lastIndexOf (Object elem) {
    int i=size-1;
    if (elem==null) {
	for (; i >= 0 ; i--) {	
	   if (elements[i]==null) return i;
      	}
    }
    else {
	for (; i >= 0 ; i--) {	
 	   if (elem.equals(elements[i])) return i;
      	}
    }
    return -1;
  }

  public Object clone() {
    ArrayList al = null;
    try {
     	al = (ArrayList) super.clone();
    }
    catch(CloneNotSupportedException cnse) {}
    al.elements = new Object[this.elements.length];
    System.arraycopy(this.elements, 0, al.elements, 0, this.size); 	
    return al;
  }

  public Object[] toArray() {
    Object[] answer = new Object[this.size];
    System.arraycopy(this.elements, 0, answer, 0, this.size);
    return answer;
  }

  public Object[] toArray(Object[] a) {
    if (a.length < this.size) {
      Class ctype = a.getClass().getComponentType();
      a = (Object[]) java.lang.reflect.Array.newInstance(ctype,this.size);
    }
    System.arraycopy(this.elements, 0, a, 0, this.size);
    if (a.length > this.size) a[size] = null;
    return a;
  }

  public Object get(int index) {
    if (index >= size) throw new IndexOutOfBoundsException();
    // if index < 0 interpreter.c will throw the exception
    return this.elements[index];
  }

  public Object set (int index, Object element) {
    if (index >= size) throw new IndexOutOfBoundsException();
    // if index < 0 interpreter.c will throw the exception
    Object answer = this.elements[index];
    this.elements[index] = element;
    return answer;
  }

  public boolean add (Object o) {
    ensureCapacity(this.size+1);
    int position = this.size;
    this.elements[position] = o;
    this.size++;
    modCount++;
    return true;
  }

  public void add (int index, Object element) {
    if ((index <0) || (index > size)) {
      throw new IndexOutOfBoundsException("Asked index "+index+" in array of size "+size);
    }
    int todo = this.size-index;
    ensureCapacity(this.size+1);
    System.arraycopy(this.elements, index, this.elements, index+1, todo);
    this.elements[index] = element;
    this.size++;
    modCount++;
  }

  public Object remove (int index) {
    Object answer;
    if ((index <0) || (index > size)) {
      throw new IndexOutOfBoundsException("Asked index "+index+" in array of size "+size);
    }
    answer = this.elements[index];
    int todo = size-index-1;
    System.arraycopy(this.elements, index+1, this.elements, index, todo);
    modCount++;
    size--;
    return answer;
  }

  public void clear() {
    modCount++;
    int oldsize = this.size;
    this.size = 0;
    for (int i=0; i<oldsize; i++) {
      elements[i] = null;
    }
  }

  public boolean addAll (Collection c) {
    if (c.isEmpty()) return false;	
    ensureCapacity(this.size+c.size());
// Is eigenlijk niet nodig maar anders wordt rij 1 per 1 uitgebreid, tenzij
// er een betere implementatie van ensureCapcity komt -- JV
    Iterator it = c.iterator();
    while (it.hasNext()) {
      add(it.next());
    }
    return true;
  }

  public boolean addAll (int index, Collection c) {
    if ((index <0) || (index > size)) {
      throw new IndexOutOfBoundsException("Asked index "+index+" in array of size "+size);
    }
    if (c.isEmpty()) return false;	
    Iterator it = c.iterator();
    int todo = this.size-index;
    int extra = c.size();
    ensureCapacity(this.size+extra);
    System.arraycopy(this.elements, index, this.elements, index+extra, todo);
    for (int i=0; i<extra; i++) { 
      this.elements[index+i] = it.next();
    }
    modCount++;
    return true;
  }

/**
**  removeRange doesn't have to be overwritten ...
**  (but is allowed !)
*/
  protected void removeRange (int fromIndex, int toIndex) {
    if (fromIndex > toIndex) throw new IndexOutOfBoundsException();
    if(toIndex != size) {
    System.arraycopy(this.elements, toIndex, this.elements, fromIndex, size-toIndex);
    }
    for (int i= fromIndex+size-toIndex; i< size;i++) {
      this.elements[i] = null;
    }
    modCount++;
    size -= (toIndex - fromIndex);

  }
}
