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
 * $Id: WeakHashMap.java,v 1.3 2006/04/18 11:35:28 cvs Exp $
 */

package java.util;

import java.lang.ref.*;

public class WeakHashMap extends AbstractMap implements Map {

  final static Object nullKey = new Object();

  private final static float DEFAULT_LOADFACTOR = (float).75;
  private final static int DEFAULT_CAPACITY = 101;

  WeakHashReference[] entries;
  private ReferenceQueue queue;

  private int threshold;
  private int capacity;
  private float loadFactor;

  int occupancy;
  int modCount=0;

  public WeakHashMap() {
    this(DEFAULT_CAPACITY, DEFAULT_LOADFACTOR);
  }

  public WeakHashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOADFACTOR);
  }

  public WeakHashMap(int initialCapacity, float loadFactor) {
    if ( initialCapacity < 0 || loadFactor <= 0.0f ) {
    	throw new IllegalArgumentException("HashMap needs positive numbers");
    }
    this.capacity = (initialCapacity < 5 ? 5 :initialCapacity);
    entries = new WeakHashReference[capacity];
    this.loadFactor = (loadFactor > 1.0f ? DEFAULT_LOADFACTOR : loadFactor );
    this.queue = new ReferenceQueue();
  }

  public WeakHashMap(Map map) {
    int initialCapacity = ((map.size() * 4) / 3) + 5;
    this.capacity = initialCapacity;
    entries = new WeakHashReference[capacity];
    this.loadFactor = DEFAULT_LOADFACTOR;
    this.queue = new ReferenceQueue();

    Iterator it = map.entrySet().iterator();
    try {
      do {
        Map.Entry me = (Map.Entry)it.next();
        put(me.getKey(), me.getValue());
      } while(true);
    }
    catch(NoSuchElementException nsee){}
  }

  public void clear() {
    if (occupancy > 0) {
      entries = new WeakHashReference[capacity];
      occupancy = 0;
      modCount++;
    }
  }

  public boolean containsKey(Object key) {
    checkQueue();
    if(key == null) {
      key = nullKey;
    }

    int cap = capacity;;
    int hash = key.hashCode() % cap;

    do {
      if(hash < 0){
        hash += cap;
      }
      WeakHashReference entry = entries[hash];
      if(entry == null) {
        return false;
      } else if(key.equals(entry.get())) {
        return true;
      } else hash--;
    } while(true);
  }

  public Set entrySet(){
    checkQueue();
    return new WeakHashMapSet();
  }

  public Object get(Object key)  {
    checkQueue();

    if(key==null) {
      key = nullKey;
    }

    int cap = capacity;;
    int hash = key.hashCode() % cap;

    do {
      if(hash < 0){
        hash += cap;
      }
      WeakHashReference entry = entries[hash];
      if(entry == null) {
        return null;
      } else if(key.equals(entry.get())) {
        return entry.value;
      } else hash--;
    } while(true);
  }

  public boolean isEmpty() {
    checkQueue();
    return (this.occupancy == 0);
  }

  public Object put(Object key, Object newvalue)  {
    if(key==null) {
      key = nullKey;
    }

    checkQueue();

    int cap = capacity;;
    int hash = key.hashCode() % cap;

    do {
      if(hash < 0){
        hash += cap;
      }

      WeakHashReference entry = entries[hash];
      if(entry == null) {
        entries[hash] = new WeakHashReference(key, newvalue, queue);
        int occ = this.occupancy;
        this.occupancy = ++occ;
        if(threshold <= occ){
          resize();
        }
        modCount++;
        return null;
      }
      else if(key.equals(entry.get())) {
        Object oldvalue = entry.value;
        entry.value = newvalue;
        modCount++;
        return oldvalue;
      }
      hash--;
    } while(true);
  }

  public Object remove(Object key)  {
    checkQueue();

    if(key==null) {
      key = nullKey;
    }

    int cap = capacity;;
    int hash = key.hashCode() % cap;

    do {
      if(hash < 0){
        hash += cap;
      }

      WeakHashReference entry = entries[hash];
      if(entry == null) {
        return null;
      } else {
        if(key.equals(entry.get())) {
          Object oldvalue = entry.value;
          deleteSlot(hash);
          modCount++;
          return oldvalue;
        }
      }
      hash--;
    } while(true);
  }

  public int size() {
    checkQueue();
    return this.occupancy;
  }

  void checkQueue(){
    WeakHashReference ref = (WeakHashReference) queue.poll();
    int cap = capacity;
    while(ref != null){
      int place = ref.hashValue % cap;
      if(place < 0){
        place += cap;
      }
      while(ref != entries[place]){
        if(--place < 0){
          place += cap;
        }
      }
      deleteSlot(place);
      ref = (WeakHashReference) queue.poll();
    }
  }

  int firstBusySlot(int i) {
    int j;
    if (i<0 || i>=capacity) {
      return -1;
    }
    for(j=i;j<capacity;++j)
      if (entries[j] != null)  {
        return j;
      }
    return -1;
  } 

  void deleteSlot(int slotIndex) {
    int vacant, current, home, distance1, distance2;
    boolean happy;

    --occupancy;
    current = slotIndex;

    while(true) {
      // R1
      entries[current] = null;
      vacant = current;

      // R2
      happy = true;
      while(happy) {
        current = current==0 ? capacity-1 : current-1;

        // R3
        if (entries[current] == null) {

          return; // normal termination

        }
        home = entries[current].hashValue % capacity;
        if(home < 0){
          home += capacity;
        }
        distance1 = current<=home ? home - current : capacity + home - current;
        distance2 = current<=vacant ? vacant - current : capacity + vacant - current;

        happy = distance1<distance2;
        // back to R2
      }

      // R4
      entries[vacant] = entries[current];

      // repeat from R1 ...
    }
  }

  private void resize() {
    int oldsize = this.capacity;
    int newsize = oldsize * 2 + 1;
    WeakHashReference[] oldkeys = entries;
    WeakHashReference[] entries = new WeakHashReference[newsize];

    this.capacity = newsize;
    this.threshold = (int)(newsize*loadFactor);

    for (int oldindex=0; oldindex < oldsize; ++oldindex) {
      WeakHashReference key = oldkeys[oldindex];
      if (key != null){
        int place = key.hashValue % newsize;
        if(place < 0){
          place += newsize;
        }
        while(entries[place] != null){
          if(--place < 0){
            place += newsize;
          }
        }
        entries[place] = key;
      }
    }
    this.entries = entries;
  }

//inner classes ...

  class WeakHashMapSet extends AbstractSet {
    public Iterator iterator() {
      return new WeakHashMapIterator();
    }

    public int size() {
      return WeakHashMap.this.size();
    }
  }

  class WeakHashMapIterator implements Iterator {
  	int pos=firstBusySlot(0); //points to the next free slot
  	int mc=modCount;
  	Object refKey;

    public  boolean hasNext() {
   		return pos !=-1;
    }

    public Object next() {
      if (modCount != mc) {
        throw new ConcurrentModificationException("don't change the set please (this.modCount == "+mc+", outer.modCount == "+modCount+")");
      }
      if (pos == -1) {
        throw new NoSuchElementException("no elements left");
      }
  		WeakHashReference entry = entries[pos];
      refKey = entry.get();
      pos = firstBusySlot(pos+1);
  		return entry;
    }

    public void remove() {
     	if (refKey == null ) {
     	  throw new IllegalStateException("do a next() operation before remove()");
     	}
    	if (modCount != mc) {
    	  throw new ConcurrentModificationException("don't change the set please");
    	}
    	WeakHashMap.this.remove(refKey);
    	mc++;
    	refKey = null;
    }
  }
}
