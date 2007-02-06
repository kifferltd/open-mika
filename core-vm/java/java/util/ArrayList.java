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
  private static final int defaultCapacity = 7;

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
