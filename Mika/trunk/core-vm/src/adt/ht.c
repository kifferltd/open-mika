/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2004, 2008, 2009 by Chris Gray, /k/ Embedded Java   *
* Solutions.  All rights reserved.                                        *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#include <string.h>

#include "fifo.h"
#include "hashtable.h"  
#include "list.h"
#include "ts-mem.h"

/* 
** We implement "open addressing with linear probing", as described in
** Algorithms L (p.518) and R (p. 527) of Knuth's _Art of Computer
** Programming_, Chapter 6.4.
**
** In his discussion Knuth assumes that the hashing function h(K) acts
** directly on the key K.  In the case of java/lang/Hashtable we are
** given a function w_word (*hash)(w_word) which generates a 32-bit signed 
** integer result, but that isn't really what we want: h(K) should
** deliver a result in the range 0 to M-1, where M is the size of the
** hashtable.  So in our implementation,
**  1) if CACHE_HASCODES is defined, we store the value of (*hash)(K) in
**     the hashtable together with the key K and the associated information.
**  2) our function h(K) is equal to (*hash)(K) modulo M.
*/

/*
** ht_hash calculates the value of (hashcode MOD hashtable->currentsize),
** where MOD is unsigned integer modulus.  This is where the fun begins:
** C soesn't have such an operator, so we have to fake it.  We start by
** observing that if a/b evaluates as q and a%b evaluates as r, then C
** guarantees that a = b*q + r.  It follows that a - b*q = r, so we first
** divide b into a (discarding the remainder), then multiply the resulting
** quotient by b, and subtract the result from a to get r.  What a mess.
*/
inline static w_int ht_hash(w_hashtable hashtable, w_int hashcode) {
  w_int modulus = hashtable->currentsize;
  w_int result;

  result = hashcode - (hashcode/modulus)*modulus;

  return result;
}


/*
** Then the probe sequence is h(K), h(K)-1,...,0,M-1,M-2,...h(K)+1 and is obtained
** by calling ht_probe successively with sequence = 0, 1, 2, ...
*/

w_int ht_probe(w_hashtable hashtable, w_word hashcode, w_int sequence) {
  w_int result = ht_hash(hashtable,hashcode-sequence);

  while (result<0) {
    result += hashtable->currentsize;
  }

  return result;
}

/*
** For deletion, we need to examine the 'next' slot.
** ht_reprobe(hashtable,current) gives the value which follows 'current' 
** in the probe sequence
*/

static inline w_int ht_reprobe(w_hashtable hashtable, w_int current) {
  return current ? current-1 : hashtable->currentsize-1;
}

/*
** Also for deletion, we need to be able to measure the "distance" from
** one slot to another in the probing sequence: ht_distance gives the
** number of times we would have to call 'from = ht_reprobe(hashtable,from)'
** to make 'from' into 'to' ...
** [CG 20081127] Made into a macro because gcc refuses to inline function.
static inline w_int ht_distance(w_hashtable hashtable, w_int from, w_int to) {
  return from>=to ? from-to : hashtable->currentsize + from-to;
}
*/

#define ht_distance(ht,from,to) (((from) - (to)) + ((from) >= (to) ? 0 : (ht)->currentsize))

/*
** Then the question "does r lie cyclically between i and j" becomes
**   ht_distance(hashtable,i,r) < ht_distance(hashtable,i,j).
*/

/****************************************************************************/

/*
**
** recalculateThresholds(w_hashtable) calculates a new lowthreshold
** and highthreshold from the current table size;  these thresholds 
** are used by ht_check_size to decide whether a resizing is required. 
** Currently the thresholds are hard-coded to 25% and 75% of currentsize.
*/

void
recalculateThresholds(w_hashtable h) {
	w_int maxhighthreshold = h->currentsize - h->currentsize/16;
	if(maxhighthreshold>=h->currentsize) maxhighthreshold = h->currentsize-1;

	h->lowthreshold = h->currentsize/4;

	if(h->lowthreshold==0) h->lowthreshold = 1;

	h->highthreshold = h->currentsize - h->currentsize/4;

	if(h->highthreshold>maxhighthreshold) h->highthreshold = maxhighthreshold;
}


/*
 ************************************************************************
 */

w_word
dummy_hash (w_word a) {

	return a;

}

w_boolean
dummy_equal (w_word a, w_word b) {

	return a==b;

}

/*
 * [CG 20081127] Made into a macro as gcc refuses to inline
 static inline w_boolean
 ht_match(w_hashtable hashtable, int idx, w_word h, w_word k) {

 return 
#ifdef CACHE_HASHCODES
(!hashtable->hashcodes || hashtable->hashcodes[idx]==h) &&
#endif 
(*hashtable->equal)(hashtable->keys[idx],k);

}
 */
#ifdef CACHE_HASHCODES
#define ht_match(ht,i,h,k) ((!(ht)->hashcodes || (ht)->hashcodes[i]==h) && (*(ht)->equal)((ht)->keys[i],k))
#else
#define ht_match(ht,i,h,k) (*(ht)->equal)((ht)->keys[i],k)
#endif

static inline w_boolean
ht_nullkey(w_hashtable hashtable, w_word key) {

	return key==hashtable->nullkey;

}

w_boolean
ht_occupied(w_hashtable hashtable, w_int idx) {

	return !ht_nullkey(hashtable, hashtable->keys[idx]);

}

/*
 ************************************************************************
 */

w_hashtable 
_ht_create (const char *f, int l,
		char *   label,
		w_size  initialsize,
		w_word (*hash)(w_word),             
		w_boolean(*equal)(w_word,w_word), 
		w_word nullkey, w_word nullvalue
	   ) {
	w_hashtable hashtable;
	w_size newindex;

	hashtable = allocClearedMem(sizeof(w_Hashtable));
	if (!hashtable) {

		return NULL;

	}

	woempa(1,"creating %s at %p, size = %d\n",label,hashtable,initialsize);

	hashtable->label  = label;
	hashtable->currentsize = initialsize;
	hashtable->occupancy = 0;
	x_monitor_create(&hashtable->monitor);

	if (equal) {
		woempa(1,"user-defined equality function at %p\n",equal);
		hashtable->equal = equal;
	}
	else {
		hashtable->equal = dummy_equal;
	}

	hashtable->hash = hash ? hash : dummy_hash;
#ifdef CACHE_HASHCODES
	if (hash) {
		hashtable->hashcodes = allocClearedMem(initialsize * sizeof(w_word));
		if (!hashtable->hashcodes) {
			releaseMem(hashtable);

			return NULL;

		}
	}
	else {
		hashtable->hashcodes = NULL;
	}
#endif

	hashtable->nullkey   = nullkey;
	hashtable->nullvalue = nullvalue;

	hashtable->keys = allocClearedMem(initialsize * sizeof(w_word));
	if (!hashtable->keys) {
#ifdef CACHE_HASHCODES
		if (hashtable->hashcodes) {
			releaseMem(hashtable->hashcodes);
		}
#endif
		releaseMem(hashtable);

		return NULL;

	}
	woempa(1, "keys @ %p\n", hashtable->keys);

	for(newindex=0;newindex<initialsize;++newindex) {
		hashtable->keys[newindex] = hashtable->nullkey;
	}

	recalculateThresholds(hashtable);

	return hashtable;
}

void
ht_destroy(w_hashtable theHashtable) {

	x_monitor_delete(&theHashtable->monitor);
	if (theHashtable->keys) {
		releaseMem(theHashtable->keys);
	}
	if (theHashtable->values) {
		releaseMem(theHashtable->values);
	}
#ifdef CACHE_HASHCODES
	if (theHashtable->hashcodes) {
		releaseMem(theHashtable->hashcodes);
	}
#endif
	releaseMem(theHashtable);
}

void ht_check_size(w_hashtable hashtable) {
  w_int  oldsize;
  w_int  newsize;
  w_word *oldkeys;
  w_word *newkeys = NULL;
  w_word *oldvalues = hashtable->values;
  w_word *newvalues = NULL;
#ifdef CACHE_HASHCODES
  w_word *oldhashcodes = hashtable->hashcodes;
  w_word *newhashcodes = NULL;
#endif
  w_size  sequence;
  w_size  oldindex;
  w_size  newindex;
  w_word  key;

  woempa(1,"hashtable %p size now is %d, occupancy %d, thresholds %d %d\n",hashtable,hashtable->currentsize,hashtable->occupancy,hashtable->lowthreshold,hashtable->highthreshold);

  oldsize = hashtable->currentsize;
  newsize = oldsize;

  if ((hashtable->occupancy < hashtable->highthreshold)) {
    return;
  }

  newsize = hashtable->occupancy * 2;

  if (newsize < 7) {
    newsize = 7;
  }
  if ((newsize % 1) == 0) {
    ++newsize;
  }
  if ((newsize % 3) == 0) {
    newsize += 2;
  }
  if ((newsize % 5) == 0) {
    newsize += 4;
  }

  if(newsize == oldsize) {
    return;
  }

  oldkeys = hashtable->keys;

  woempa(1,"(re)allocating hashtable %s @ %p, new size %d\n",hashtable->label,hashtable,newsize);

  newkeys = allocClearedMem(newsize * sizeof(w_word));
  if (!newkeys) {
    woempa(9, "No memory available for newkeys of %s\n", hashtable->label);

    return;

  }

  if (oldvalues) {
    newvalues = allocClearedMem(newsize * sizeof(w_word));
    if (!newvalues) {
      woempa(9, "No memory available for newvalues of %s\n", hashtable->label);
      releaseMem(newkeys);

      return;

    }

  }
#ifdef CACHE_HASHCODES
  if (oldhashcodes) {
    newhashcodes = allocClearedMem(newsize * sizeof(w_word));
    if (!newhashcodes) {
      woempa(9, "No memory available for newhashcodes of %s\n", hashtable->label);
      releaseMem(newkeys);
      releaseMem(newvalues);

      return;

    }
  }
#endif

  hashtable->currentsize = newsize;
  hashtable->keys = newkeys;
  hashtable->values = newvalues;
#ifdef CACHE_HASHCODES
  hashtable->hashcodes = newhashcodes;
#endif

  for(newindex=0;newindex<newsize;++newindex) {
    newkeys[newindex] = hashtable->nullkey;
  }

  if(oldsize && newsize) {
  /*
  ** Rehash old contents into new arrays.
  ** Looks long-winded, but we don't want to have the tests for
  ** the existence of oldvalues and oldhashcodes in the main loop.
  */

#ifdef CACHE_HASHCODES
    if (oldvalues && oldhashcodes) {
      for(oldindex=0;oldindex<oldsize;++oldindex) {
        key = oldkeys[oldindex];
        if (key != hashtable->nullkey) { 
          w_word value = oldvalues[oldindex];
          w_word hc    = oldhashcodes[oldindex];

          for (sequence=0; sequence<newsize; ++sequence) {
            newindex = ht_probe(hashtable, hc, (signed)sequence);
            if (!ht_occupied(hashtable, (signed)newindex)) {
              newkeys[newindex]      = key;
              newvalues[newindex]    = value;
              newhashcodes[newindex] = hc;
              break;
            }
          }
        }
      }
    }
    else 
#endif
    if (oldvalues) {
      for (oldindex=0;oldindex<oldsize;++oldindex) {
        key = oldkeys[oldindex];
        if (key != hashtable->nullkey) { 
          w_word value = oldvalues[oldindex];
          w_word hc    = hashtable->hash(key);

          for (sequence=0; sequence<newsize; ++sequence) {
            newindex = ht_probe(hashtable, hc, (signed)sequence);
            if (!ht_occupied(hashtable, (signed)newindex)) {
              newkeys[newindex]      = key;
              newvalues[newindex]    = value;
              break;
            }
          }
        }
      }
    }
#ifdef CACHE_HASHCODES
    else if (oldhashcodes) {
      for (oldindex=0; oldindex<oldsize; ++oldindex) {
        key = oldkeys[oldindex];
        if (key != hashtable->nullkey) { 
          w_word hc = oldhashcodes[oldindex];

          for (sequence=0; sequence<newsize; ++sequence) {
            newindex = ht_probe(hashtable, hc, (signed)sequence);
            if (!ht_occupied(hashtable, (signed)newindex)) {
              newkeys[newindex]      = key;
              newhashcodes[newindex] = hc;
              break;
            }
          }
        }
      }
    }
#endif
    else {
      for (oldindex=0; oldindex<oldsize; ++oldindex) {
        key = oldkeys[oldindex];
        if (key != hashtable->nullkey) { 
          for (sequence=0; sequence<newsize; ++sequence) {
            w_word hc    = hashtable->hash(key);
            newindex = ht_probe(hashtable, hc, (signed)sequence);
            if (!ht_occupied(hashtable, (signed)newindex)) {
              newkeys[newindex]      = key;
              break;
            }
          }
        }
      }
    }
  }

  if (oldkeys) {
    releaseMem(oldkeys);
  }
  if (oldvalues) {
    releaseMem(oldvalues);
  }
#ifdef CACHE_HASHCODES
  if (oldhashcodes) {
    releaseMem(oldhashcodes);
  }
#endif

  recalculateThresholds(hashtable);

  woempa(1,"hashtable keys at %p\n",hashtable->keys);
  woempa(1,"hashtable values at %p\n",hashtable->values);
#ifdef CACHE_HASHCODES
  woempa(1,"hashtable hashcodes at %p\n",hashtable->hashcodes);
#endif

}

/*
** ht_rehash is a no-op: see Theorem P on p. 530 of Knuth, op. cit..
*/

w_hashtable 
ht_rehash(w_hashtable oldtable) {

  return oldtable;

}

static inline void set_value(w_hashtable hashtable, w_int idx, w_word key, w_word value) {
  if (hashtable->values) {
    hashtable->values[idx] = value;
  }
  else if (value != key) {
    hashtable->values = allocClearedMem(hashtable->currentsize * sizeof(w_word));
    if (!hashtable->values) {
      wabort(ABORT_WONKA, "Unable to allocate hashtable->values[]\n");
    }
    hashtable->values[idx] = value;
  }
}


void
ht_insert(w_hashtable hashtable, w_int idx, w_word key, w_word value)
{
  woempa(1, "hashtable %p index %d key %p value %p\n", hashtable, idx, (char *)key, (char *)value);
  /*
  ** Protect ourselves against barbarous practices of the user
  */
  if(ht_nullkey(hashtable, key) 
     || ht_occupied(hashtable, idx) 
     || hashtable->currentsize-hashtable->occupancy<=1) {

    woempa(9,"attempt to corrupt hashtable thwarted!\n");
    return;
  }

  hashtable->keys[idx] = key;
  set_value(hashtable, idx, key, value);
#ifdef CACHE_HASHCODES
  if (hashtable->hashcodes) {
    hashtable->hashcodes[idx] = (*hashtable->hash)(key);
  }
#endif
  hashtable->occupancy += 1;
}

void
ht_delete(w_hashtable hashtable, w_int idx) {
  w_int     vacant;
  w_int     current;
  w_int     home;
  w_boolean happy;

  woempa(1, "hashtable %s @ %p: delete slot %d\n",hashtable->label,hashtable,idx);
  /*
  ** The following is a summary of Knuth's Algorithm R.
  **
  ** Step R1 : Mark TABLE[i] empty, and set j equal to i.
  ** Step R2 : decrease i, modulo M
  ** Step R3 : if TABLE[i] is empty, the algorithm terminates.
  **           Otherwise set r equal to h(KEY[i]), the original hash 
  **           address of KEY[i]).  If i<=r<j (modulo M), go back to R2.
  ** Step R4 : Set TABLE[j] equal to TABLE[i], and return to step R1.
  **
  ** In Knuth's therminology, TABLE[j] is our vacant and TABLE[i] is
  ** our current; TABLE[r] corresponds to home, the "home" slot
  ** of the entry at current. 
  */

  hashtable->occupancy -= 1;
  current = idx;

  while(1) {
  /*
  ** R1
  */
    woempa(1,"mark slot %d as empty\n",current);
    hashtable->keys[current] = hashtable->nullkey;
    vacant = current;
  /*
  ** R2
  */
    happy = WONKA_TRUE;

    while(happy) {
      current = ht_reprobe(hashtable,current);
      woempa(1,"Next slot is %d\n",current);
  /*
  ** R3
  */
      if(ht_nullkey(hashtable, hashtable->keys[current])) {
        woempa(1,"slot %d is empty, algorithm terminated normally\n",current);

        return;

      }

#ifdef CACHE_HASHCODES
      if (hashtable->hashcodes) {
        home = ht_probe(hashtable,hashtable->hashcodes[current],0); 
      }
      else {
        home = ht_probe(hashtable,hashtable->keys[current],0); 
      }
#else
      home = ht_probe(hashtable,hashtable->hash(hashtable->keys[current]),0); 
#endif
      woempa(1,"  whereas in slot %d it would be %d slots from home\n",vacant,ht_distance(hashtable,home,vacant));
      if( ht_distance(hashtable,home,current) 
        < ht_distance(hashtable,home,vacant)) {
        woempa(1,"so it can stay where it is\n");
      } else {
        woempa(1,"so it needs to move ...\n");
        happy = WONKA_FALSE;
      }
    }
  /*
  ** R4
  */
    woempa(1,"copying from slot %d to slot %d\n",current,vacant);
    hashtable->keys[vacant]      = hashtable->keys[current];
    if (hashtable->values) {
      hashtable->values[vacant]    = hashtable->values[current];
    }
#ifdef CACHE_HASHCODES
    if (hashtable->hashcodes) {
      hashtable->hashcodes[vacant] = hashtable->hashcodes[current];
    }
#endif
  }
}

/*
***************************************************************************
*/

w_word /* previous value or hashtable->nullvalue */
ht_write(w_hashtable hashtable, w_word key, 
w_word newvalue
) {
  w_int  seq;
  w_int  maxseq = hashtable->currentsize;
  w_word result = hashtable->nullvalue;
  w_word hashcode = (*hashtable->hash)(key);
  w_int idx;

  ht_lock(hashtable);
  for(seq=0;seq<maxseq;++seq) {                                                    
    idx=ht_probe(hashtable,hashcode,seq);                            
    if(ht_occupied(hashtable,idx)) {                                        
      if(ht_match(hashtable, idx, hashcode, key)) {                            
#ifdef HASHTEST
      woempa(1,"%p: MATCH with %d collisions\n",hashtable,seq);
#endif
        if (hashtable->values) {
          result = hashtable->values[idx];
        }
        else {
          result = key;
        }
        set_value(hashtable, idx, key, newvalue);
        break;                                              
      }                                                   
    }                                                    
    else {                                              
#ifdef HASHTEST
      woempa(1,"INSERT with %d collisions\n",seq);
#endif
      hashtable->keys[idx] = key;
      set_value(hashtable, idx, key, newvalue);
#ifdef CACHE_HASHCODES
      if (hashtable->hashcodes) {
        hashtable->hashcodes[idx] = hashcode;
      }
#endif
      hashtable->occupancy += 1;
      ht_check_size(hashtable);
      break;                                                            
    }                                                                  
  }                                                                   
  ht_unlock(hashtable);

  return result;

}


w_word /* previous value or hashtable->nullvalue */
ht_write_no_lock(w_hashtable hashtable, w_word key, 
w_word newvalue
) {
  w_int  seq;
  w_int  maxseq = hashtable->currentsize;
  w_word result = hashtable->nullvalue;
  w_word hashcode = (*hashtable->hash)(key);
  w_int idx;

  for(seq=0;seq<maxseq;++seq) {                                                    
    idx=ht_probe(hashtable,hashcode,seq);                            
    if(ht_occupied(hashtable,idx)) {                                        
      if(ht_match(hashtable, idx, hashcode, key)) {                            
#ifdef HASHTEST
      woempa(1,"%p: MATCH with %d collisions\n",hashtable,seq);
#endif
        if (hashtable->values) {
          result = hashtable->values[idx];
        }
        else {
          result = key;
        }
        set_value(hashtable, idx, key, newvalue);
        break;                                              
      }                                                   
    }                                                    
    else {                                              
#ifdef HASHTEST
      woempa(1,"INSERT with %d collisions\n",seq);
#endif
      hashtable->keys[idx] = key;
      set_value(hashtable, idx, key, newvalue);
#ifdef CACHE_HASHCODES
      if (hashtable->hashcodes) {
        hashtable->hashcodes[idx] = hashcode;
      }
#endif
      hashtable->occupancy += 1;
      ht_check_size(hashtable);
      break;                                                            
    }                                                                  
  }                                                                   

  return result;

}


w_word /* value found or hashtable->nullvalue */
ht_read_no_lock(w_hashtable hashtable, w_word key) {
  w_int  seq;
  w_int  maxseq;
  w_word result;
  w_word hashcode;
  w_int  idx;

  maxseq = hashtable->currentsize;
  result = hashtable->nullvalue;
#ifdef CACHE_HASHCODES
  hashcode = hashtable->hashcodes ? (*hashtable->hash)(key) : key;
#else
  hashcode = (*hashtable->hash)(key);
#endif

  for(seq=0;seq<maxseq;++seq) {

    idx=ht_probe(hashtable,hashcode,seq);
    if(ht_occupied(hashtable,idx)) {
      if(ht_match(hashtable, idx, hashcode, key)) {
#ifdef HASHTEST
      woempa(1,"%p: MATCH with %d collisions\n",hashtable,seq);
#endif
        result = hashtable->values ? hashtable->values[idx] : hashtable->keys[idx];
        break;
      }
    }
    else {
      break;
    }
  }
 
  return result;

}

w_word /* key found or hashtable->nullkey */
ht_findkey_no_lock(w_hashtable hashtable, w_word key) {
  w_int  seq;
  w_int  maxseq = hashtable->currentsize;
  w_word result = hashtable->nullkey;
  w_word hashcode = (*hashtable->hash)(key);
  w_int  idx;

  for(seq=0;seq<maxseq;++seq) {
    idx=ht_probe(hashtable,hashcode,seq);
    if(ht_occupied(hashtable,idx)) {
      if(ht_match(hashtable, idx, hashcode, key)) {
#ifdef HASHTEST
      woempa(1,"%p: MATCH with %d collisions\n",hashtable,seq);
#endif
        result = hashtable->keys[idx];
        break;
      }
    }
    else {
      break;
    }
  }
 
  return result;

}

w_word /* value found or NULL */
ht_erase_no_lock(w_hashtable hashtable, w_word key) {
  w_int  seq;
  w_int  maxseq = hashtable->currentsize;
  w_word result = hashtable->nullvalue;
  w_word hashcode = (*hashtable->hash)(key);
  w_int  idx;

  for(seq=0;seq<maxseq;++seq) {

    idx=ht_probe(hashtable,hashcode,seq);
    if(ht_occupied(hashtable,idx)) {
      if(ht_match(hashtable, idx, hashcode, key)) {
        result = hashtable->values ? hashtable->values[idx] : key;
        ht_delete(hashtable,idx);
        break;
      }
    }
    else {
      break;
    }
  }

  return result;
 

}


w_int
ht_every(w_hashtable hashtable, void (*fun)(w_word key,w_word value)) {
  w_int count = 0;
  w_int i;

  ht_lock(hashtable);

  if (hashtable->values) {
    for(i=0;i<hashtable->currentsize;++i) {
      if(ht_occupied(hashtable,i)) {
        fun(hashtable->keys[i], hashtable->values[i]);
        ++count;
      }
    }
  }
  else {
    for(i=0;i<hashtable->currentsize;++i) {
      if(ht_occupied(hashtable,i)) {
        fun(hashtable->keys[i], hashtable->keys[i]);
        ++count;
      }
    }
  }

  ht_unlock(hashtable);

  return count;
}

w_fifo
ht_list_keys_no_lock(w_hashtable hashtable) {
  w_fifo result;
  w_size leafsize;
  w_int i;

  leafsize = 62;
  while (leafsize < (w_size)hashtable->occupancy) {
    leafsize = leafsize * 2 + 2;
  }
  result = allocFifo(leafsize);

  if (result == NULL) {

    return NULL;

  }

  ht_lock(hashtable);

  for(i=0;i<hashtable->currentsize;++i)
  {
    if(ht_occupied(hashtable,i)) {
      if (putFifo((void*)hashtable->keys[i],result) < 0) {
        wabort(ABORT_WONKA, "I don't believe it!  We sized fifo %p to have a leafsize of %d, and after %d putFifo's it overflows!\n", result, leafsize, i);
      }
    }
  }

  ht_unlock(hashtable);

  return result;
}

w_fifo
ht_list_values_no_lock(w_hashtable hashtable) {
  w_fifo result;
  w_size leafsize;
  w_int i;

  if (!hashtable->values) {

    return ht_list_keys(hashtable);

  }

  leafsize = 62;
  while (leafsize < (w_size)hashtable->occupancy) {
    leafsize = leafsize * 2 + 2;
  }
  result = allocFifo(leafsize);
  if (result == NULL) {

    return NULL;

  }


  ht_lock(hashtable);

  for(i=0;i<hashtable->currentsize;++i)
  {
    if(ht_occupied(hashtable,i)) {
      if (putFifo((void*)hashtable->values[i],result) < 0) {
        wabort(ABORT_WONKA, "I don't believe it!  We sized fifo %p to have a leafsize of %d, and after %d putFifo's it overflows!\n", result, leafsize, i);
      }
    }
  }

  ht_unlock(hashtable);

  return result;
}

w_int
ht_iterate_no_lock(w_hashtable hashtable, void (*fun)(w_word key,w_word value, void *arg1, void *arg2), void *arg1, void *arg2) {
  w_int count = 0;
  w_int i;

  if (hashtable->values) {
    for(i=0;i<hashtable->currentsize;++i) {
      if(ht_occupied(hashtable,i)) {
        fun(hashtable->keys[i], hashtable->values[i], arg1, arg2);
        ++count;
      }
    }
  }
  else {
    for(i=0;i<hashtable->currentsize;++i) {
      if(ht_occupied(hashtable,i)) {
        fun(hashtable->keys[i], hashtable->keys[i], arg1, arg2);
        ++count;
      }
    }
  }

  return count;

}


w_word /* old value of key or hashtable->nullkey */
ht_register(w_hashtable hashtable, w_word key) 
{
  w_int seq;
  w_int maxseq = hashtable->currentsize;
  w_word result = hashtable->nullkey;
  w_word hashcode = (*hashtable->hash)(key);
  w_int  idx;
  w_int  counter;

  ht_lock(hashtable);
  for(seq=0;seq<maxseq;++seq) { 
    idx=ht_probe(hashtable,hashcode,seq);                            
    if(ht_occupied(hashtable,idx)) {                                        
      if(ht_match(hashtable, idx, hashcode, key)) {                            
#ifdef HASHTEST
      woempa(1,"%p: MATCH with %d collisions\n",hashtable,seq);
#endif
        result = hashtable->keys[idx];
        counter = (w_int)(hashtable->values[idx]);
        if(counter<HT_BIGNUM)
          hashtable->values[idx] = (w_word)(counter+1);

        break;                                              
      }                                                   
    }                                                    
    else {                                              
#ifdef HASHTEST
      woempa(1,"INSERT with %d collisions\n",seq);
#endif
      hashtable->keys[idx] = key;
      if (!hashtable->values) {
        hashtable->values = allocClearedMem(hashtable->currentsize * sizeof(w_word));
        if (!hashtable->values) {
          wabort(ABORT_WONKA, "Unable to allocate hashtable->values[]\n");
        }
      }
      hashtable->values[idx] = 1;
#ifdef CACHE_HASHCODES
      if (hashtable->hashcodes) {
        hashtable->hashcodes[idx] = hashcode;
      }
#endif
      hashtable->occupancy += 1;
      ht_check_size(hashtable);
      break;                                                            
    }                                                                  
  }                                                                   
  ht_unlock(hashtable);                                                   

  return result;

}

w_word /* value of matched key or hashtable->nullkey */
ht_deregister(w_hashtable hashtable, w_word key) 
{
  w_int seq;
  w_int maxseq = hashtable->currentsize;
  w_word result = hashtable->nullkey;
  w_word hashcode = (*hashtable->hash)(key);
  w_int  idx;
  w_int  counter;

  ht_lock(hashtable);
  for(seq=0;seq<maxseq;++seq) {
    idx=ht_probe(hashtable,hashcode,seq);                            
    if(ht_occupied(hashtable,idx)) {                                        
      if(ht_match(hashtable, idx, hashcode, key)) { 
        counter = hashtable->values[idx];
        if(counter<HT_BIGNUM) {
          counter -= 1;
          if(counter==0) {
            result = hashtable->keys[idx];
            ht_delete(hashtable,idx);
          }
          else {
            hashtable->values[idx] = (w_word)counter; 
          }
          break;                                              
        }
      }                                                   
    }  
    else { 
      wabort(ABORT_WONKA,"Manure occured: did not find key 0x%08x\n",key);
    }
  }                                                                   
  ht_unlock(hashtable);                                                   

  return result;

}

 
w_word
ht_unique(w_hashtable hashtable, w_word try, w_word value, w_word min, w_word max) {
  w_boolean  found = WONKA_FALSE;
  w_word      result;

  result = try;
  if(result < min) {
    result=max;
  }
  if(result > max || result == 0) {
    result=min;
  }

  ht_lock(hashtable);

  while(found==WONKA_FALSE) {
    if (ht_read_no_lock(hashtable, result) == 0) {
      ht_write_no_lock(hashtable, result, value);
      found = WONKA_TRUE;
    }
    else {
      ++result;
      if(result > max || result == 0) {
        result = min;
      }
    }
  }

  ht_unlock(hashtable);

  return result;

}

/*
** Implementation of functions needed for cstring hashtables.
*/

w_word cstring_hash (w_word a) {
  char *bytes = (char*)a;
  int n = strlen(bytes);
  int m = 1;
  w_word hash = 0x5a6978b4;

  if (n > 32) {
    m = (n / 16);
    n = 16;
  }

  while (n--) {
    hash = hash * 253 + *bytes;
    bytes += m;
  }
  
  return hash;
}

w_boolean cstring_equal (w_word a, w_word b) {
  return (strcmp((char *)a, (char *)b) == 0);
}

/*
** -----------------------------------------------------------------------
** ht2k_ functions: these differ from the ht_ functions in that the key
** consists of two w_words, not one.  For example the key could be a 
** w_long or w_double, or (more probably) it could be two independent
** w_word's, so that ht2k becomes an implementation of a sparse 2d matrix.
** -----------------------------------------------------------------------
*/

/*
** ht2k_hash calculates the value of (hashcode MOD hashtable->currentsize),
** where MOD is unsigned integer modulus.  See ht_hash().
*/
inline static w_int ht2k_hash(w_hashtable2k hashtable, w_int hashcode) {
  w_int modulus = hashtable->currentsize;
  w_int result;

  result = hashcode - (hashcode/modulus)*modulus;

  return result;
}


/*
** Then the probe sequence is h(K), h(K)-1,...,0,M-1,M-2,...h(K)+1 and is obtained
** by calling ht_2kprobe successively with sequence = 0, 1, 2, ...
*/

w_int ht2k_probe(w_hashtable2k hashtable, w_word hashcode, w_int sequence) {
  w_int result = ht2k_hash(hashtable,hashcode-sequence);

  while (result<0) result += hashtable->currentsize;

#ifdef HASHTEST
  woempa(3, "hashtable %p hashcode %08x modulus %d sequence %d : result =%d\n", hashtable, hashcode, hashtable->currentsize, sequence, result);
#endif

  return result;
}

/*
** For deletion, we need to examine the 'next' slot.
** ht2k_reprobe(hashtable,current) gives the value which follows 'current' 
** in the probe sequence
*/

static inline w_int ht2k_reprobe(w_hashtable2k hashtable, w_int current) {
  return current ? current-1 : hashtable->currentsize-1;
}

/*
** Also for deletion, we need to be able to measure the "distance" from
** one slot to another in the probing sequence: ht2k_distance gives the
** number of times we would have to call 'from = ht2k_reprobe(hashtable,from)'
** to make 'from' into 'to' ...
*/

static inline w_int ht2k_distance(w_hashtable2k hashtable, w_int from, w_int to) {
  return from>=to ? from-to : hashtable->currentsize + from-to;
}

/*
** Then the question "does r lie cyclically between i and j" becomes
**   ht2k_distance(hashtable,i,r) < ht2k_distance(hashtable,i,j).
*/

/****************************************************************************/

/*
**
** recalculateThresholds(w_hashtable2k) calculates a new lowthreshold
** and highthreshold from the current table size;  these thresholds 
** are used by ht2k_check_size to decide whether a resizing is required. 
*/

void
recalculateThresholds2k(w_hashtable2k h) {
  w_int maxhighthreshold = h->currentsize - h->currentsize/16;
  if(maxhighthreshold>=h->currentsize) maxhighthreshold = h->currentsize-1;

  h->lowthreshold = h->currentsize/4;

  if(h->lowthreshold==0) h->lowthreshold = 1;

  h->highthreshold = h->currentsize - h->currentsize/4;

  if(h->highthreshold>maxhighthreshold) h->highthreshold = maxhighthreshold;
}


/*
************************************************************************
*/

static inline w_word hash2k(w_word key1, w_word key2) {
/*
  w_long temp = key1;

  temp = (temp << 32) | key2;

  return (w_word)(temp / 1234567);
*/
  return key1 ^ key2;
}

static inline w_boolean
ht2k_match(w_hashtable2k hashtable, int idx, w_word h, w_word k1, w_word k2) {
  woempa(1, "%s: index %d holds 0x%08x/0x%08x, looking for  0x%08x/0x%08x\n", hashtable->label, idx, hashtable->keys1[idx], hashtable->keys2[idx], k1, k2);

  return (hashtable->keys1[idx] == k1) && (hashtable->keys2[idx] == k2);

}

static inline w_boolean
ht2k_nullkey(w_word key1, w_word key2) {

  return key1 == 0 && key2 == 0;

}

w_boolean
ht2k_occupied(w_hashtable2k hashtable, w_int idx) {

  return !ht2k_nullkey(hashtable->keys1[idx], hashtable->keys2[idx]);

}

/*
************************************************************************
*/

w_hashtable2k 
_ht2k_create (const char *f, int l,
  char *   label,
  w_size  initialsize
) {
  w_hashtable2k hashtable;

  hashtable = allocClearedMem(sizeof(w_Hashtable2k));
  if (!hashtable) {

    return NULL;

  }

  woempa(1,"creating %s at %p, size = %d\n",label,hashtable,initialsize);

  hashtable->label  = label;
  hashtable->currentsize = initialsize;
  hashtable->occupancy = 0;
  x_monitor_create(&hashtable->monitor);

  hashtable->keys1 = allocClearedMem(initialsize * sizeof(w_word));
  hashtable->keys2 = allocClearedMem(initialsize * sizeof(w_word));
  hashtable->values = allocClearedMem(initialsize * sizeof(w_word));
  if (!hashtable->keys1 || !hashtable->keys2 || !hashtable->values) {
    if (hashtable->keys1) {
      releaseMem(hashtable->keys1);
    }
    if (hashtable->keys2) {
      releaseMem(hashtable->keys2);
    }
    if (hashtable->values) {
      releaseMem(hashtable->values);
    }
    releaseMem(hashtable);

    return NULL;

  }

  recalculateThresholds2k(hashtable);

  return hashtable;
}

void
ht2k_resize(w_hashtable2k hashtable, w_size newsize) 
{
  w_size  oldsize = hashtable->currentsize;
  w_word *oldkeys1 = hashtable->keys1;
  w_word *oldkeys2 = hashtable->keys2;
  w_word *oldvalues = hashtable->values;
  w_word *newkeys1;
  w_word *newkeys2;
  w_word *newvalues;
  w_size  sequence;
  w_size  oldindex;
  w_size  newindex;
  w_word  key1;
  w_word  key2;

  woempa(1,"(re)allocating hashtable %s @ %p, new size %d\n",hashtable->label,hashtable,newsize);

  newkeys1 = allocClearedMem(newsize * sizeof(w_word));
  if (!newkeys1) {
    woempa(9, "No memory available for newkeys1 of %s\n", hashtable->label);

    return;

  }

  newkeys2 = allocClearedMem(newsize * sizeof(w_word));
  if (!newkeys2) {
    woempa(9, "No memory available for newkeys2 of %s\n", hashtable->label);
    releaseMem(newkeys1);

    return;
  }

  newvalues = allocClearedMem(newsize * sizeof(w_word));
  if (!newvalues) {
    woempa(9, "No memory available for newvalues of %s\n", hashtable->label);
    releaseMem(newkeys2);
    releaseMem(newkeys1);

    return;
  }

  hashtable->currentsize = newsize;
  hashtable->keys1 = newkeys1;
  hashtable->keys2 = newkeys2;
  hashtable->values = newvalues;

  for(newindex=0;newindex<newsize;++newindex) {
    newkeys1[newindex] = 0;
    newkeys2[newindex] = 0;
  }

  if(oldsize && newsize) {
  /*
  ** rehash old contents into new arrays
  */

    for(oldindex=0;oldindex<oldsize;++oldindex) {
      key1 = oldkeys1[oldindex];
      key2 = oldkeys2[oldindex];
      if(!ht2k_nullkey(key1, key2)) { 
        w_word value = oldvalues[oldindex];
        w_word hc    = hash2k(key1, key2);

        for(sequence=0;sequence<newsize;++sequence) {
          newindex = ht2k_probe(hashtable,hc, (signed)sequence);
          if(!ht2k_occupied(hashtable, (signed)newindex)) {
            newkeys1[newindex]     = key1;
            newkeys2[newindex]     = key2;
            newvalues[newindex]    = value;
            break;
          }
        }
      }
    }
  }

  if (oldkeys1) {
    releaseMem(oldkeys1);
  }
  if (oldkeys2) {
    releaseMem(oldkeys2);
  }
  if (oldvalues) {
    releaseMem(oldvalues);
  }

  recalculateThresholds2k(hashtable);

  woempa(1,"hashtable keys1 at %p\n",hashtable->keys1);
  woempa(1,"hashtable keys2 at %p\n",hashtable->keys2);
  woempa(1,"hashtable values at %p\n",hashtable->values);
#ifdef CACHE_HASHCODES
  woempa(1,"hashtable hashcodes at %p\n",hashtable->hashcodes);
#endif

}

void
ht2k_destroy(w_hashtable2k theHashtable) {

  x_monitor_delete(&theHashtable->monitor);
  if (theHashtable->keys1) {
    releaseMem(theHashtable->keys1);
  }
  if (theHashtable->keys2) {
    releaseMem(theHashtable->keys2);
  }
  if (theHashtable->values) {
    releaseMem(theHashtable->values);
  }
  releaseMem(theHashtable);
}

w_size
ht2k_check_size(w_hashtable2k hashtable, w_int direction) {
  w_int       oldsize;
  w_int       newsize;

  woempa(1,"hashtable %p size now is %d, occupancy %d, thresholds %d %d\n",hashtable,hashtable->currentsize,hashtable->occupancy,hashtable->lowthreshold,hashtable->highthreshold);
  oldsize = hashtable->currentsize;
  newsize = oldsize;

  if( (hashtable->occupancy>=hashtable->highthreshold && direction>=0)
   || (hashtable->occupancy<=hashtable->lowthreshold  && direction<=0)
    ) {
    newsize = hashtable->occupancy * 2;

    if(newsize<hashtable->occupancy+2) newsize=hashtable->occupancy+2;
    if((newsize&1)==0) ++newsize;
    if((newsize%3)==0) newsize += 2;
    if((newsize%5)==0) newsize += 4;

    if(newsize==oldsize) newsize = 0;
    else {
      woempa(1,"recommend resizing to %d\n",newsize);
    }

    return newsize;

  }
  else return 0;

}


/*
** ht2k_rehash is a no-op: see Theorem P on p. 530 of Knuth, op. cit..
*/

w_hashtable2k 
ht2k_rehash(w_hashtable2k oldtable) {

  return oldtable;

}


void
ht2k_insert(w_hashtable2k hashtable, w_int idx, w_word key1, w_word key2, w_word value)
{
  woempa(1, "hashtable %p index %d key %p %p value %p\n", hashtable, idx, (char *)key1, (char *)key2, (char *)value);
  /*
  ** Protect ourselves against barbarous practices of the user
  */
  if(ht2k_nullkey(key1, key2) 
     || ht2k_occupied(hashtable, idx) 
     || hashtable->currentsize-hashtable->occupancy<=1) {

    woempa(1,"attempt to corrupt hashtable thwarted!\n");
    return;
  }

  hashtable->keys1[idx] = key1;
  hashtable->keys2[idx] = key2;
  hashtable->values[idx] = value;
  hashtable->occupancy += 1;
}

void
ht2k_delete(w_hashtable2k hashtable, w_int idx) {
  w_int     vacant;
  w_int     current;
  w_int     home;
  w_boolean happy;

  woempa(1, "hashtable %s @ %p: delete slot %d\n",hashtable->label,hashtable,idx);
  /*
  ** The following is a summary of Knuth's Algorithm R.
  **
  ** Step R1 : Mark TABLE[i] empty, and set j equal to i.
  ** Step R2 : decrease i, modulo M
  ** Step R3 : if TABLE[i] is empty, the algorithm terminates.
  **           Otherwise set r equal to h(KEY[i]), the original hash 
  **           address of KEY[i]).  If i<=r<j (modulo M), go back to R2.
  ** Step R4 : Set TABLE[j] equal to TABLE[i], and return to step R1.
  **
  ** In Knuth's therminology, TABLE[j] is our vacant and TABLE[i] is
  ** our current; TABLE[r] corresponds to home, the "home" slot
  ** of the entry at current. 
  */

  hashtable->occupancy -= 1;
  current = idx;

  while(1) {
  /*
  ** R1
  */
    woempa(1,"mark slot %d as empty\n",current);
    hashtable->keys1[current] = 0;
    hashtable->keys2[current] = 0;
    vacant = current;
  /*
  ** R2
  */
    happy = WONKA_TRUE;

    while(happy) {
      current = ht2k_reprobe(hashtable,current);
      woempa(1,"Next slot is %d\n",current);
  /*
  ** R3
  */
      if(ht2k_nullkey(hashtable->keys1[current], hashtable->keys2[current])) {
        woempa(1,"slot %d is empty, algorithm terminated normally\n",current);

        return;

      }

      home = ht2k_probe(hashtable,hash2k(hashtable->keys1[current], hashtable->keys2[current]), 0); 
      woempa(1,"  whereas in slot %d it would be %d slots from home\n",vacant,ht2k_distance(hashtable,home,vacant));
      if( ht2k_distance(hashtable,home,current) 
        < ht2k_distance(hashtable,home,vacant)) {
        woempa(1,"so it can stay where it is\n");
      } else {
        woempa(1,"so it needs to move ...\n");
        happy = WONKA_FALSE;
      }
    }
  /*
  ** R4
  */
    woempa(1,"copying from slot %d to slot %d\n",current,vacant);
    hashtable->keys1[vacant]      = hashtable->keys1[current];
    hashtable->keys2[vacant]      = hashtable->keys2[current];
    hashtable->values[vacant]    = hashtable->values[current];
  }
}

/*
***************************************************************************
*/

w_boolean
ht2k_write_no_lock(w_hashtable2k hashtable, w_word key1, w_word key2, w_word newvalue) {
  w_int  seq;
  w_int  maxseq = hashtable->currentsize;
  w_word result = 0;
  w_word hashcode = hash2k(key1, key2);
  w_size newsize;
  w_int idx;

  for(seq=0;seq<maxseq;++seq) {                                                    
    idx=ht2k_probe(hashtable,hashcode,seq);                            
    if(ht2k_occupied(hashtable,idx)) {                                        
      if(ht2k_match(hashtable, idx, hashcode, key1, key2)) {                            
      woempa(1,"%s: REPLACE slot %d with %d collisions\n",hashtable->label,idx,seq);
        result = WONKA_TRUE;
        hashtable->values[idx] = newvalue;                           
        break;                                              
      }                                                   
    }                                                    
    else {                                              
      woempa(1,"%s: INSERT slot %d with %d collisions\n",hashtable->label,idx,seq);
      hashtable->keys1[idx] = key1;
      hashtable->keys2[idx] = key2;
      hashtable->values[idx] = newvalue;
      hashtable->occupancy += 1;
      newsize = ht2k_check_size(hashtable,1);
      if (newsize) {
        ht2k_resize(hashtable, newsize);
      }
      break;                                                            
    }                                                                  
  }                                                                   

  return result;

}


w_word /* value found or 0 */
ht2k_read_no_lock(w_hashtable2k hashtable, w_word key1, w_word key2) {
  w_int  seq;
  w_int  maxseq;
  w_word result;
  w_word hashcode;
  w_int  idx;

  maxseq = hashtable->currentsize;
  result = 0;
  hashcode = hash2k(key1, key2);

  for(seq=0;seq<maxseq;++seq) {

    idx=ht2k_probe(hashtable,hashcode,seq);
    if(ht2k_occupied(hashtable,idx)) {
      if(ht2k_match(hashtable, idx, hashcode, key1, key2)) {
        woempa(1,"%s: MATCH slot %d with %d collisions\n",hashtable->label,idx,seq);
        result = hashtable->values[idx];
        break;
      }
    }
    else {
      break;
    }
  }
 
  return result;

}

w_boolean
ht2k_erase_no_lock(w_hashtable2k hashtable, w_word key1, w_word key2) {
  w_int  seq;
  w_int  maxseq = hashtable->currentsize;
  w_boolean result = WONKA_FALSE;
  w_word hashcode = hash2k(key1, key2);
  w_int  idx;

  for(seq=0;seq<maxseq;++seq) {

    idx=ht2k_probe(hashtable,hashcode,seq);
    if(ht2k_occupied(hashtable,idx)) {
      if(ht2k_match(hashtable, idx, hashcode, key1, key2)) {
        result = WONKA_TRUE;
        ht2k_delete(hashtable,idx);
        break;
      }
    }
    else {
      break;
    }
  }

  return result;
 

}


w_int
ht2k_every(w_hashtable2k hashtable, void (*fun)(w_word key1, w_word key2, w_word value)) {
  w_int count = 0;
  w_int i;

ht2k_lock(hashtable);

  for(i=0;i<hashtable->currentsize;++i)
  {
    if(ht2k_occupied(hashtable,i)) {
      fun(hashtable->keys1[i], hashtable->keys2[i], hashtable->values[i]);
      ++count;
    }
  }

ht2k_unlock(hashtable);

  return count;
}

w_fifo
ht2k_list_values(w_hashtable2k hashtable) {
  w_fifo result;
  w_size leafsize;
  w_int i;

  leafsize = 62;
  while (leafsize < (w_size)hashtable->occupancy) {
    leafsize = leafsize * 2 + 2;
  }
  result = allocFifo(leafsize);
  if (result == NULL) {

    return NULL;

  }


  ht2k_lock(hashtable);

  for(i=0;i<hashtable->currentsize;++i)
  {
    if(ht2k_occupied(hashtable,i)) {
      if (putFifo((void*)hashtable->values[i],result) <0) {
        wabort(ABORT_WONKA, "I don't believe it!  We sized fifo %p to have a leafsize of %d, and after %d putFifo's it overflows!\n", result, leafsize, i);
      }
    }
  }

  ht2k_unlock(hashtable);

  return result;
}

w_int
ht2k_iterate_no_lock(w_hashtable2k hashtable, void (*fun)(w_word key1, w_word key2, w_word value, void *arg1, void *arg2), void *arg1, void *arg2) {
  w_int count = 0;
  w_int i;

  for(i=0;i<hashtable->currentsize;++i)
  {
    if(ht2k_occupied(hashtable,i)) {
      fun(hashtable->keys1[i], hashtable->keys2[i], hashtable->values[i], arg1, arg2);
      ++count;
    }
  }

  return count;

}


 
