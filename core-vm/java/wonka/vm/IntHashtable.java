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
 * $Id: IntHashtable.java,v 1.2 2006/04/18 13:00:29 cvs Exp $
 */

package wonka.vm;

public class IntHashtable {

  private final static float DEFAULT_LOADFACTOR = 0.75f;
  private final static int DEFAULT_CAPACITY = 101;

  private int capacity;
  private float loadFactor;
  private int occupancy;
  private int threshold;

  private int[] keys;
  private Object[] value;

  public IntHashtable() {
    this(DEFAULT_CAPACITY, DEFAULT_LOADFACTOR);
  }

  public IntHashtable(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOADFACTOR);
  }

  public IntHashtable(int initialCapacity, float loadFactor) {
    if ( initialCapacity < 0 || loadFactor <= 0.0f )
    	throw new IllegalArgumentException("IntHashtable needs positive numbers");
    this.capacity = (initialCapacity < 5 ? 5 :initialCapacity);
    this.keys = new int[capacity];
    this.value = new Object[capacity];
    this.loadFactor = (loadFactor > 1.0f ? DEFAULT_LOADFACTOR : loadFactor );
    threshold = (int)(this.loadFactor * this.capacity);
  }

  public int size() {
    return this.occupancy;
  }

  public Object get(int key)  {
    int cap = this.capacity;
    int i = key % cap;
    int[] k = keys;
    do {
      if(i < 0){
        i += cap;
      }
      int ckey = k[i];
      if(ckey == 0) {
        return null;
      } else if(key == ckey) {
        return this.value[i];
      } else i--;
    } while(true);
  }          

  public Object put(int key, Object newvalue)  {
    int cap = this.capacity;
    int i = key % cap;
    int[] k = keys;
    do {
      if(i < 0){
        i += cap;
      }
      int ckey = k[i];
      if(ckey == 0) {
        k[i] = key;
        this.value[i] = newvalue;
        if(++this.occupancy >= threshold){
          resize();
        }
        return null;
      } 
      else if(key == ckey) {
        Object oldvalue = this.value[i];
        this.value[i] = newvalue;
        return oldvalue;
      }
      i--;
    } while(true);
  }

  public Object remove(int key)  {
    int cap = this.capacity;
    int i = key % cap;
    int[] k = keys;
    do {
      if(i < 0){
        i += cap;
      }
      int ckey = k[i];
      if(ckey == 0) {
        return null; 
      } else {
        if(key == ckey) {
          Object oldvalue = this.value[i];
          deleteSlot(i);
          return oldvalue;
        }
      }
      --i;
    } while(true);

  }

  public void clear() {
     if (occupancy > 0) {
     keys = new int[capacity];
     value = new Object[capacity];
     occupancy = 0;
     }
  }

  private void resize() {
    int oldsize = this.capacity;
    int newsize = oldsize * 2 + 1;
    int[] oldkeys = this.keys;
    Object[] oldvalues = this.value;
    int[] newkeys = new int[newsize];
    Object[] newvalues = new Object[newsize];

    this.capacity = newsize;
    this.occupancy = 0;
    this.keys = newkeys;
    this.value = newvalues;
    this.threshold = (int) (loadFactor * newsize);

    for (int oldindex=0;oldindex<oldsize;++oldindex) {
      int key = oldkeys[oldindex];
      if (key != 0)
        this.put(key,oldvalues[oldindex]);
    }
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
        home = keys[current] % capacity;
        if(home < 0){
          home += capacity;
        }
        distance1 = current<=home ? home - current : capacity + home - current;
        distance2 = current<=vacant ? vacant - current : capacity + vacant - current;

        happy = distance1<distance2;
      // back to R2
      }

    // R4
      keys[vacant] = keys[current];
      value[vacant] = value[current];

    // repeat from R1 ...
    }
  }
}
