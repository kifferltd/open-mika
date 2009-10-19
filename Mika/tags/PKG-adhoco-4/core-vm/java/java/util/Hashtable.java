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
** $Id: Hashtable.java,v 1.4 2006/04/18 11:35:28 cvs Exp $
*/

package java.util;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

public class Hashtable extends Dictionary implements Cloneable, Serializable, Map {

  private static final long serialVersionUID = 1421746759512286392L;

  transient Object[] keys;
  transient Object[] values;
  private float loadFactor;
  private int threshold;
  private transient int capacity;
  transient int occupancy;
  private final static int DEFAULT_CAPACITY = 11;

  transient int modCount;

  public Hashtable(int initialCapacity, float loadFactor) throws IllegalArgumentException {
    if (initialCapacity < 0 || loadFactor <= 0.0f) throw new IllegalArgumentException();
    if (loadFactor > 1.0f) loadFactor = 0.75f;
    this.capacity = (initialCapacity < 5 ? 5 :initialCapacity);
    this.keys = new Object[capacity];
    this.values = new Object[capacity];
    this.loadFactor = loadFactor;
    this.threshold = (int)(capacity*loadFactor);
  }

  public Hashtable(int initialCapacity) throws IllegalArgumentException {
    this(initialCapacity,0.75f);
  }

  public Hashtable() {
    this(DEFAULT_CAPACITY,0.75f);
  }

  public Hashtable (Map t) {
    this((t.size()*4)/3+2);

    synchronized(t) {
      int n = t.size();
      Object[] mapkeys = t.keySet().toArray();

      for (int i = 0; i < n; i++) {
        this.put(mapkeys[i],t.get(mapkeys[i]));
      }
    }
  }


  native int firstBusySlot(int i);

  public synchronized void putAll(Map m) throws NullPointerException {
  	Iterator it = m.entrySet().iterator();
        java.util.Map.Entry me;
        while (it.hasNext()) {
        	me = (java.util.Map.Entry) it.next();
        	put(me.getKey() , me.getValue());
        }
  }	


  public String toString() {
    String result = "{";
    int i = 0; 
    boolean exhausted = false;
    while(!exhausted) {
      synchronized(this) {
        i = firstBusySlot(i);
        if(i<0) {
          exhausted = true;
        } else {
          if(result.length()>1) result = result + ", ";
          result = result + this.keys[i] + "=" + this.values[i];
        }
      }
      ++i;
    }

    result = result + "}";

    return result;

  }
  
  public synchronized Object clone() {
    Object that = null;

    try {
      that = super.clone();
      Hashtable that_hashtable = (Hashtable)that;

      that_hashtable.keys = new Object[capacity];
      System.arraycopy(this.keys,0,that_hashtable.keys,0,capacity);
      that_hashtable.values = new Object[capacity];
      System.arraycopy(this.values,0,that_hashtable.values,0,capacity);
    }
    catch (CloneNotSupportedException cnse) {
    }

    return that;

  }
  
  protected void rehash() {
    int newsize = this.capacity*2+1;
    int oldsize = this.capacity;
    Object[] oldkeys = this.keys;
    Object[] oldvalues = this.values;
    Object[] newkeys = new Object[newsize];
    Object[] newvalues = new Object[newsize];

    this.capacity = newsize;
    this.threshold = (int)(capacity * loadFactor);
    this.keys = newkeys;
    this.values = newvalues;

    for (int oldindex = 0; oldindex < oldsize; ++oldindex) {
      Object key = oldkeys[oldindex];
      if (key != null) {
        int hash = key.hashCode() % newsize;
        do {
          if(hash < 0) hash += newsize;
          if(newkeys[hash] == null) {
            newkeys[hash] = key;
            newvalues[hash] = oldvalues[oldindex];
            break;
          }
          hash--;
        } while(true);
      }
    }
  }
  
  public int size() {

     return this.occupancy;

  }
  
  public boolean isEmpty() {

     return this.occupancy == 0;

  }

// See Algorithm R on p.527 of Knuth Vol.3

  private void deleteSlot(int slotIndex) {
    int vacant, current, home, distance1, distance2;
    boolean happy;

    --occupancy;
    current = slotIndex;

    while(true) {
    // R1
      keys[current] = null;
      vacant = current;

    // R2
      happy = true;
      while(happy) {
        current = current==0 ? capacity-1 : current-1;

    // R3
        if (keys[current] == null) {

          return; // normal termination

        }

/*      original use with substract in probe
        home = probe(keys[current].hashCode(),0);
        distance1 = current>=home ? current-home : current+capacity-home;
        distance2 = current>=vacant ? current-vacant : current+capacity-vacant;
        happy = distance1<distance2;
*/
        home = keys[current].hashCode() % this.capacity;
        if(home < 0) home = home + this.capacity;
        
        distance1 = current<=home ? home - current : capacity + home - current;
        distance2 = current<=vacant ? vacant - current : capacity + vacant - current;

        happy = distance1<distance2;


      // back to R2    
      }

    // R4
      keys[vacant] = keys[current];
      values[vacant] = values[current];

    // repeat from R1 ...
    }
  }

  public synchronized Object get(Object key) {
    int   cap = capacity;
    int   hash = key.hashCode() % cap;
    Object k[] = keys;
    Object k2;

    do {
      if(hash < 0) hash += cap;

      k2 = k[hash];
      
      if(k2 == null) {
        return null;
      } else if(key.equals(k2)) {
        return this.values[hash];
      }

      hash--;
    } while(true);
  }

  public synchronized Object put(Object key, Object newvalue) throws NullPointerException {
    if(newvalue == null)
      throw new NullPointerException();

    int cap = capacity;
    int hash = key.hashCode() % cap;
    Object k[] = keys;
    Object k2;

    do {
      if(hash < 0) hash += cap;

      k2 = k[hash];

      if(k2 == null) {
        k[hash] = key;
        this.values[hash] = newvalue;

        occupancy++;

        if (occupancy >= threshold) {
          rehash();
        }

        return null;

      } else if(key.equals(k2)) {
        Object oldvalue = this.values[hash];

        this.values[hash] = newvalue;

        return oldvalue;

      }

      hash--;
    } while(true);
  } 

  public synchronized Object remove(Object key) {
    int   cap = capacity;
    int   hashcode = key.hashCode() % cap;
    while(true) {
      if(hashcode < 0) hashcode += cap;

      if(keys[hashcode] == null) {

        return null;

      } else {
        if(key.equals(this.keys[hashcode])) {
          Object oldvalue = this.values[hashcode];

          deleteSlot(hashcode);
          return oldvalue;

        }

      }

      hashcode--;
    }
  }

  public Set keySet() {
    return new HashtableKeySet();
  }

  public Set entrySet() {
//     	System.out.println("DEBUG: entering entrySet of HT");
        return new HashtableSet();
  }

  public Collection values() {
  	return new HashtableValues();
  }

  

  class HashtableKeyEnum implements Enumeration, Iterator {

    private int nextSlot = 0;

    public boolean hasMoreElements() {
      return(firstBusySlot(nextSlot)>=0);
    }
/*
    public native boolean hasMoreElements();
*/

    public boolean hasNext() {
      return(firstBusySlot(nextSlot)>=0);
    }

    public synchronized Object nextElement() throws NoSuchElementException {
      int thisSlot = firstBusySlot(nextSlot);

      if(thisSlot<0) {
        throw new NoSuchElementException();
      }
      nextSlot = firstBusySlot(thisSlot+1);
      return keys[thisSlot];
    }

    public Object next() throws NoSuchElementException {
      int thisSlot = firstBusySlot(nextSlot);

      if(thisSlot<0) {
        throw new NoSuchElementException();
      }
      nextSlot = firstBusySlot(thisSlot+1);
      return keys[thisSlot];
    }
    // this function is not specified by the JLS 1.2 (not even in jls 1.3 on the web !)
    public void remove() throws UnsupportedOperationException, IllegalStateException {
      throw new RuntimeException ("not yet supported");
    }
  }

  public Enumeration keys() {

    return new HashtableKeyEnum();

  }


  // This could probably be done with an anonymous class, but for now:

  class HashtableElementEnum implements Enumeration {
    private int nextSlot = firstBusySlot(0);

    public boolean hasMoreElements() {

      return(firstBusySlot(nextSlot)>=0);

    }
/*
    public native boolean hasMoreElements();
*/

    public synchronized Object nextElement()
      throws NoSuchElementException
      {
        int thisSlot = firstBusySlot(nextSlot);

        if(thisSlot<0)
          throw new NoSuchElementException();

        nextSlot = firstBusySlot(thisSlot+1);

        return values[thisSlot];

      }

  }

  public Enumeration elements() {

    return new HashtableElementEnum();

  }


  public boolean containsValue(Object value) {
    return contains(value);
  }

  public boolean contains(Object value) {
    int nextSlot = firstBusySlot(0);
    Object v[] = values;

    while(nextSlot >= 0) {
      if(value.equals(v[nextSlot])) {

        return true;

      }
      nextSlot = firstBusySlot(nextSlot + 1);
    }

    return false;
  }

  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  public void clear() {
    keys = new Object[capacity];
    values = new Object[capacity];
    this.occupancy = 0;
  }

  private void writeObject(ObjectOutputStream s) throws IOException {
    s.defaultWriteObject();
    s.writeInt(capacity);
    s.writeInt(occupancy);
    int nextSlot=firstBusySlot(0);
    for (int i=0;i<occupancy;i++) {
      s.writeObject(keys[nextSlot]);
      s.writeObject(values[nextSlot]);
      nextSlot=firstBusySlot(nextSlot+1);
    }
  }

  private void readObject(ObjectInputStream s) throws IOException, 
  ClassNotFoundException {

    int local_oc;
    s.defaultReadObject();
    this.capacity=s.readInt();
    local_oc=s.readInt();
    this.keys = new Object[this.capacity];
    this.values = new Object[this.capacity];
    Object key,value;
    for (int i=0;i<local_oc;i++) {
      key=s.readObject();
      value=s.readObject();
      this.put(key,value);
    }
  }

  /**
   ** inner class which implements Map.Entry
   **
   */
  private class HashtableMapEntry implements java.util.Map.Entry {

    private Object key;
    private Object value;

    public HashtableMapEntry(Object k, Object v) {
      key = k;
      value = v;
    }

    public Object getKey() {
      return key;
    }

    public Object getValue() {
      return value;
    }

    public Object setValue(Object nv) {
      Object ov = value;
      value = nv;
      return ov;
    }

    public boolean equals(Object o) {

      if (!(o instanceof java.util.Map.Entry))return false;
      java.util.Map.Entry e = (java.util.Map.Entry)o;
      if (  e == null ) return false;
      return ( (key == null ? e.getKey()==null : key.equals(e.getKey())) &&
          (value == null ? e.getValue()==null : value.equals(e.getValue())));
    }

    public int hashCode() {
      int kc = key == null ? 0 : key.hashCode();
      int vc = value == null ? 0 : value.hashCode();
      return kc ^ vc;
    }

  }

  private class HashtableSet extends AbstractSet {


    public HashtableSet(){}

    public Iterator iterator() {
      return new EntrySetIterator();
    }
    public int size() {
      return occupancy;
    }
  }	

  private class EntrySetIterator implements Iterator {
    int pos=firstBusySlot(0);
    int oldpos=-1;
    int mc=modCount;
    int status=0;

    public EntrySetIterator() {}

    public  boolean hasNext() {
      if (modCount != mc) throw new ConcurrentModificationException("don't change the set please");
      return  firstBusySlot(pos)!=-1;
    }

    public Object next() {
      if (modCount != mc) throw new ConcurrentModificationException("don't change the set please");
      status = 1;
      if (firstBusySlot(pos)==-1) throw new NoSuchElementException("no elements left");
      oldpos = pos;
      pos = firstBusySlot(pos+1);			
      return new HashtableMapEntry(keys[oldpos] ,values[oldpos]);                 	
    }

    public void remove() {
      if (modCount != mc) throw new ConcurrentModificationException("don't change the set please");
      if (status != 1 ) throw new IllegalStateException("do a next() operation before remove()");
      Hashtable.this.remove(keys[oldpos]);
      status=-1;
    }
  }


  private class KeySetIterator implements Iterator {

    public KeySetIterator(){}

    int pos=firstBusySlot(0);
    int oldpos=-1;
    int mc=modCount;
    int status=0;

    public  boolean hasNext() {
      return  firstBusySlot(pos)!=-1;
    }

    public Object next() {
      if (modCount != mc) throw new ConcurrentModificationException("don't change the set please");
      status = 1;
      if (firstBusySlot(pos)==-1) throw new NoSuchElementException("no elements left");
      oldpos = pos;
      pos = firstBusySlot(pos+1);			
      return keys[oldpos];                 	
    }

    public void remove() {
      if (modCount != mc) throw new ConcurrentModificationException("don't change the set please");
      if (status != 1 ) throw new IllegalStateException("do a next() operation before remove()");
      Hashtable.this.remove(keys[oldpos]);
      status=-1;
      mc++;
    }


  }

  private class HashtableKeySet extends AbstractSet {
    public HashtableKeySet(){}

    public Iterator iterator() {
      return new KeySetIterator();
    }
    public int size() {
      return occupancy;
    }
  }
  private class ValuesIterator implements Iterator {

    public ValuesIterator(){}

    int pos=firstBusySlot(0);
    int oldpos=-1;
    int mc=modCount;
    int status=0;

    public  boolean hasNext() {
      return  firstBusySlot(pos)!=-1;
    }

    public Object next() {
      if (modCount != mc) throw new ConcurrentModificationException("don't change the set please");
      status = 1;
      if (firstBusySlot(pos)==-1) throw new NoSuchElementException("no elements left");
      oldpos = pos;
      pos = firstBusySlot(pos+1);			
      return values[oldpos];                 	
    }

    public void remove() {
      if (modCount != mc) throw new ConcurrentModificationException("don't change the set please");
      if (status != 1 ) throw new IllegalStateException("do a next() operation before remove()");
      Hashtable.this.remove(keys[oldpos]);
      status=-1;
      mc++;
    }


  }

  private class HashtableValues extends AbstractCollection {
    public HashtableValues(){}

    public Iterator iterator() {
      return new ValuesIterator();
    }
    public int size() {
      return occupancy;
    }
  }


  //adding methods overwritten from object ...

  public synchronized boolean equals(Object c) {
    if (c == this ) return true;
    if (!(c instanceof Map)) return false;
    Map target = (Map)c;
    Object value;
    if (!this.keySet().equals(target.keySet()))
      return false;
    Iterator it = target.entrySet().iterator();
    java.util.Map.Entry me;
    while (it.hasNext()) {
      me = (java.util.Map.Entry) it.next();
      value = me.getValue();
      if (!(value==null ? get(me.getKey())==null : value.equals(get(me.getKey()))))
        return false;
    }
    return true;
  }

  public synchronized int hashCode() {
    int hash=0;
    int pos=firstBusySlot(0);
    while (pos != -1) {
      hash += (keys[pos].hashCode() ^ values[pos].hashCode());
      pos = firstBusySlot(pos+1);		
    }
    return hash;
  }	
}
