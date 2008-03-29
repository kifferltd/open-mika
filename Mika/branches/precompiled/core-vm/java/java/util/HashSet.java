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
 * $Id: HashSet.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
 */

package java.util;

import java.io.Serializable;

public class HashSet extends AbstractSet implements Set, Cloneable, Serializable {

  private static final long serialVersionUID = -5024744406713321676L;

  private static int defaultSize = 101;
  private static float defaultLoadFactor= 0.75f;
  final static Object nullKey = new Object();

  transient int modCount;

  transient float loadFactor;
  transient int occupancy;
  transient int capacity;
  transient int threshold;
  transient Object[] key;

  public HashSet() {
  	this(defaultSize,defaultLoadFactor);
  }

  public HashSet(Collection c){
  	this(1 + (c.size() * 2),defaultLoadFactor);
    addAll(c);
  }

  public HashSet(int initCap){
  	this(initCap,defaultLoadFactor);
  }

  public HashSet(int initCap, float loadFactor){
    if (initCap < 0 || loadFactor < 0.0f) {
      throw new IllegalArgumentException();
    }
    initCap = (initCap < 5 ? 5 : initCap);
    capacity = initCap;
    if(loadFactor > 1.0f){
      loadFactor = defaultLoadFactor;
    }
    this.loadFactor = loadFactor;
    key = new Object[initCap];
    threshold =  (int)(loadFactor * initCap);
  }

  public boolean add(Object key) {
    if(key == null) {
      key = nullKey;
    }

    int cap = this.capacity;
    int hashcode = key.hashCode() % cap;
    Object[] k = this.key;

    do {
      if(hashcode < 0){
        hashcode += cap;
      }
      Object o = k[hashcode];

      if(o == null) {
        modCount++;
        k[hashcode] = key;
        if(++this.occupancy >= threshold){
          resize();
        }
        return true;
      }
      else if(key.equals(o)) {
        return false;
      }
      hashcode--;

    } while(true);
  }

  public int size() {
    return occupancy;
  }

  public boolean isEmpty() {
   	return (occupancy == 0);
  }

  public boolean remove(Object rkey){

    if(rkey == null) {
      rkey = nullKey;
    }

    int cap = this.capacity;
    int hashcode = rkey.hashCode() % cap;
    Object[] k = this.key;

    do {
      if(hashcode < 0){
        hashcode += cap;
      }

      Object o = k[hashcode];

      if(o == null) {
        return false;
      }
      else if(rkey.equals(o)) {
        modCount++;
        deleteSlot(hashcode);
        return true;
      }
      hashcode--;
    } while(true);
  }

  public Iterator iterator() {
    return new HashSetIterator();
  }

  public void clear(){
    if(occupancy > 0){
      modCount++;
      occupancy=0;
      key = new Object[capacity];
    }
  }

  public Object clone(){
  	HashSet h = null;
  	try {
  	 	h = (HashSet) super.clone();
	   	h.key = (Object[])this.key.clone();
  	}
  	catch(CloneNotSupportedException cnse){}
  	return h;
  }

  public boolean contains(Object key) {
    int nextSlot = firstBusySlot(0);
    if (key == null){
      key = nullKey;
    }
    Object[] k = this.key;
    while(nextSlot>=0) {
      if(key.equals(k[nextSlot])) {
        return true;
      }
      nextSlot = firstBusySlot(nextSlot+1);
    }
    return false;
  }


  int firstBusySlot(int i) {
    Object[] key = this.key;
    int capacity = this.capacity;
    if (i<0) {
      return -1;
    }
    for( ; i < capacity; ++i) {
      if (key[i] != null){
        return i;
      }
    }
    return -1;
  }

  private void resize() {
    int oldsize = this.capacity;
    int newsize = oldsize * 2 + 1;
    Object[] oldkeys = this.key;
    this.key = new Object[newsize];
    this.capacity = newsize;
    this.occupancy = 0;
    this.threshold = (int)(loadFactor * newsize);

    for (int oldindex = 0; oldindex < oldsize; ++oldindex) {
      Object key = oldkeys[oldindex];
      if (key != null)
        this.add(key);
    }
  }

  void deleteSlot(int slotIndex) {
    int vacant, current, home, distance1, distance2;
    boolean happy;
    --occupancy;
    current = slotIndex;

    Object[] key = this.key;
    int capacity = this.capacity;

    while(true) {
    // R1
      key[current] = null;
      vacant = current;
    // R2
      happy = true;
      while(happy) {
        current = current==0 ? capacity-1 : current-1;
    // R3
        if (key[current] == null) {
          return; // normal termination
        }
        home = key[current].hashCode() % capacity;
        if(home < 0){
          home += capacity;
        }
        distance1 = current<=home ? home - current : capacity + home - current;
        distance2 = current<=vacant ? vacant - current : capacity + vacant - current;
        happy = distance1<distance2;
      // back to R2
      }
    // R4
      key[vacant] = key[current];
    // repeat from R1 ...
    }
  }

  private class HashSetIterator implements Iterator {

  	private int mc;
  	private int pos;
  	private int oldpos;
  	
  	public HashSetIterator(){
  		mc = modCount;
  		pos = firstBusySlot(0);
  		oldpos = pos;
  	}	
  	
  	public boolean hasNext() {
      return (pos != -1);
  	}
  	
  	public Object next() {
      if (pos == -1) {
        throw new NoSuchElementException();  	       	 	
      }
      if (mc != modCount) {
        throw new ConcurrentModificationException();
      }
      Object e = key[pos];
      oldpos = pos;
      pos = firstBusySlot(pos+1);
      return (nullKey == e ? null : e);		  	
  	} 	
  	
  	public void remove() {
      if (oldpos == pos) {
        throw new IllegalStateException();
      }
      if (mc != modCount) {
        throw new ConcurrentModificationException();
      }
      deleteSlot(oldpos);
      oldpos = pos; 	
  	}
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    int cap = s.readInt();
    capacity = cap;
    threshold = (int)(cap * loadFactor);
    loadFactor = s.readFloat();
    int occ = s.readInt();
    key = new Object[cap];
    while(occ-- > 0){
      add(s.readObject());
      occ--;
    }
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    s.writeInt(capacity);
    s.writeFloat(loadFactor);
    int occ = this.occupancy;
    s.writeInt(occ);
    int i = 0;
    Object[] k = key;
    while(occ > 0){
      while(k[i] == null){
        i++;
      }
      s.writeObject(k[i++]);
      occ--;
    }
  }
}
