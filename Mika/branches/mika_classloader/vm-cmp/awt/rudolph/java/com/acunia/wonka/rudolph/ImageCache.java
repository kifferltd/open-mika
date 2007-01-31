/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: ImageCache.java,v 1.1 2005/06/14 08:48:24 cvs Exp $
*/

package com.acunia.wonka.rudolph;

import java.lang.ref.*;

public class ImageCache {

  protected int[] keys;
  protected SoftReference[] values;
  private float loadFactor;
  private int threshold;
  private transient int capacity;
  private transient int occupancy;
  private final static int DEFAULT_CAPACITY = 101;
  private transient int modCount = 0;

  public ImageCache(int initialCapacity, float loadFactor) throws IllegalArgumentException {
    if (initialCapacity < 0 || loadFactor <= 0.0f) throw new IllegalArgumentException();
    if (loadFactor > 1.0f) loadFactor = (float)0.75;
    this.capacity = (initialCapacity < 5 ? 5 :initialCapacity);
    this.keys = new int[capacity];
    this.values = new SoftReference[capacity];
    this.loadFactor = loadFactor;
    this.threshold = (int)(capacity*loadFactor);
    this.occupancy = 0;
  }

  public ImageCache(int initialCapacity) throws IllegalArgumentException {
    this(initialCapacity,(float)0.75);
  }

  public ImageCache() {
    this(DEFAULT_CAPACITY,(float)0.75);
  }

  private void resize(int newsize) {
    int oldsize = this.capacity;
    int[] oldkeys = this.keys;
    SoftReference[] oldvalues = this.values;
    int[] newkeys = new int[newsize];
    SoftReference[] newvalues = new SoftReference[newsize];

    this.capacity = newsize;
    this.threshold = (int)(capacity*loadFactor);
    this.occupancy = 0;
    this.keys = newkeys;
    this.values = newvalues;

    int oldindex;//, newindex;

    for (oldindex=0;oldindex<oldsize;++oldindex) {
      int key = oldkeys[oldindex];
      if (key != 0)
        this.putref(key,oldvalues[oldindex]);
    }
  }

  protected int firstBusySlot(int i) {
    int j;
    if (i<0 || i>=capacity || this.keys == null) {
      return -1;
    }

    for(j=i;j<capacity;++j)
      if (this.keys[j] != 0) {
        return j;
      }

    return -1;
  }

  private int probe(int hashcode, int sequence) {
    int rehash = (hashcode-sequence) % this.capacity;

    if (rehash<0) rehash = rehash + this.capacity;
    return rehash;

  }

  private void deleteSlot(int slotIndex) {
    int vacant, current, home, distance1, distance2;
    boolean happy;

    --occupancy;
    current = slotIndex;
    while(true) {
      // R1
      keys[current] = 0;
      vacant = current;
      // R2
      happy = true;
      while(happy) {
        current = current==0 ? capacity-1 : current-1;
        // R3
        if (keys[current] == 0) {
          return; // normal termination
        }

        home = probe(keys[current],0);
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

  public Object get(int key) {
    if(key == 0) return null; 

    synchronized(this) {
      if (this.keys == null) {
        return null;
      }

      int   i;
      int   j = 0;
      int   hash = key;

      while(true) {
        i = probe(hash, j); 
        if(this.keys[i] == 0) {
          return null;
        } else if(key == this.keys[i]) {
          return this.values[i].get();
        } else ++j;
      }
    }
  }

  private int checkSize(int direction) {
    float targetsquared, current, currentsquared;

    targetsquared = loadFactor * loadFactor;
    if (capacity > 0) {
      current = (float)occupancy / (float)capacity;
    }
    else {
      current = 1;
    }
    currentsquared = current * current;

    if (direction>=0 && currentsquared<loadFactor
        || direction<=0 && targetsquared>current) {
      return 0;
    }

    int newsize = (int)((float)occupancy/loadFactor);
    if (newsize<occupancy+2) newsize = occupancy+2;
    if (direction>=0 && newsize<=capacity) newsize = 0;
    if (direction<=0 && newsize>=capacity) newsize = 0;

    return newsize;
  }

  public Object putref(int key, SoftReference newvalue)
    throws NullPointerException {
      
    if(key == 0 || newvalue ==null)
      throw new NullPointerException();

    int i;
    int j = 0;
    int hashcode = key;
    int newsize;

    synchronized(this) {

      if (this.keys == null) {
        this.keys = new int[DEFAULT_CAPACITY];
        this.values = new SoftReference[DEFAULT_CAPACITY];
        this.capacity = DEFAULT_CAPACITY;
        this.threshold = (int)(capacity*loadFactor);
      }

      while(true) {
        i = probe(hashcode, j);
        if(this.keys[i] == 0) {
          this.keys[i] = key;
          this.values[i] = newvalue;
          ++this.occupancy;
          newsize = this.checkSize(+1);
          if(newsize!=0) resize(newsize);
          return null;
        } else if(key == this.keys[i]) {
          SoftReference oldvalue = this.values[i];
          this.values[i] = newvalue;
          return oldvalue;
        }
        ++j;
      }
    }
  }

  public Object put(int key, Object newvalue)
    throws NullPointerException {

    return putref(key, new SoftReference(newvalue));
    
  }

  public Object remove(int key) {
    if(key == 0) return null;

    int   i;
    int   j = 0;
    int   hashcode = key;
    int   newsize;

    synchronized(this) {
      if (this.keys == null) {
        return null;
      }

      while(true) {
        i = probe(hashcode,j);
        if(this.keys[i] == 0) {
          return null;
        } else {
          if(key == this.keys[i]) {
            Object oldvalue = this.values[i].get();
            deleteSlot(i);
            newsize = this.checkSize(-1);
            if(newsize!=0) resize(newsize);
            return oldvalue;
          }
        }
        ++j;
      }
    }
  }

  public void clear() {
    int idx;
    int i, j;

    if(keys != null) {
      for(idx=0;idx<capacity;++idx) {
        if(this.keys[idx] != 0) {
          this.deleteSlot(idx);
        }
      }
      this.occupancy = 0;
    }
  }

}
