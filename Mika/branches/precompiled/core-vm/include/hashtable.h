/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2005 by Chris Gray, /k/ Embedded Java Solutions.    *
*  All rights reserved.                                                   *
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

#ifndef _HASHTABLE_H
#define _HASHTABLE_H

#include "fifo.h"
#include "wonka.h"
#include "oswald.h"

/*
** If CACHE_HASHCODES is defined, whenever a user-supplied has function
** is used we cache each item's hashcode along with its key and value.
** Not recommended, because it wastes a lot of space: if the hashcode
** is expensive to calculate, it should be cached in the item itself.
*/
//#define CACHE_HASHCODES

/**@name Hashtables: general description
**
** A Hashtable consists of:
**
**  label          a brief description in the form of a 'C' string
**                 (a Wonka string would have been nice, but the Wonka
**                 string system itself needs a hashtable, so...)
**
**  currentsize    the current number of slots allocated
**
**  occupancy      the number of slots currently occupied (key!=nullkey)
**
**  lowthreshold   occupancy level at which a call to ht_check_size will
**                 cause the table to be shrunk
**
**  highthreshold  occupancy level at which a call to ht_check_size will
**                 cause the table to be expanded
**
**                 lowthreshold and highthreshold are calculated when
**                 ht_create or ht_resize is called.
**
**  monitor        a x_Monitor used by the high-level operations and
**                 by ht_lock and ht_unlock
**
**  hash           hash function, mapping w_word -> w_word.   
**
**  equal          function used to test two keys for equality
**                 (*equal)(X,Y) should return WONKA_TRUE iff X and Y
**                 are to be treated as equal.  (*equal)(X,nullkey)
**                 should only return WONKA_TRUE if X==nullkey. (!!!)
**
**                 Two keys which compare as equal must yield the
**                 same hashcode, i.e.
**                   (*equal)(X,Y) ==> (*hash)(X)==(*hash)(Y) .
**                 The converse is not necessarily true.
**
**  nullkey        a value which the user guarantees will never be used
**                 as a 'key', and which therefore can safely be used   
**                 internally to mark a vacant slot.  (Also used by
**                 ht_register and ht_deregister as a return code).
**                 entry was found matIn return the
**
**  nullvalue      the value to be returned by ht_read or ht_write if
**                 there was no entry in the hashtable corresponding to
**                 the given key
**
**  keys[]         an array of keys, indexed by slot number
**
**  values[]       an array of values, indexed by slot number
**                 (created only when an entry is added for which
**                 value != key).
**
**  hashcodes[]    an array of hashcodes, indexed by slot number
**                 (present only if CACHE_HASHCODES is set and
**                 hash function is non-null).
**
** Each slot in a hashtable holds a key, a value, and a hashcode which
** was somehow derived from the key by a user-defined function.  As
** far as we are concerned these are just arbitrary 32-bit values, except
** that:
**   - 'key' is guaranteed not to hold the user-defined value 'nullkey'
**     (this is vital!!!)
**   - 'hashcode' is a true function of 'key', meaning that any given
**     value of 'key' will always be associated with the same 'hashcode'.
**   - any given value of 'key' will always be associated with the same
**     'hashcode'.  The reverse does not necessarily apply: for example
**     all entries could have the same hashcode and the algorithms would
**     still work, albeit inefficiently.  ("The worst case performance of
**     these algorithms is almost unthinkably bad", says Knuth).  There
**     are no reserved or forbidden values for the hashcode.
**
** Although to us the key, value and hashcode are just w_word's, the user's
** code will generally cast them to something else; for example, for the
** implementation of java/lang/Hashtable the key and value are w_instance's
** of class Object and hashcode is a w_int, while the routines to handle
** registration (interning) of strings uses the address of the UTF8 string
** as key and the refcount as value.
*/


typedef struct w_Hashtable {
  void    *dummy;
  char    *label; 
  w_hashtable next;
  w_hashtable previous;
  w_int    currentsize;   
  w_int    occupancy;     
  w_int    lowthreshold;  
  w_int    highthreshold; 
  x_Monitor monitor;        
  w_word (*hash) (w_word);
  w_boolean (*equal)(w_word,w_word);
  w_word   nullkey;
  w_word   nullvalue;
  w_word  *keys;
  w_word  *values;
#ifdef CACHE_HASHCODES
  w_word  *hashcodes;
#endif
} w_Hashtable;

/*
** -------------------------------------------------------------------------
** A w_hashtable2k is basically the same, but the single key[] array is
** replaced by two arrays, keys1[] and keys2[].  We also omit the hash()
** and equal() functions, together with nullkey and nullvalue: for 2-key
** hashtables we only support the default hash and equality functions,
** and zero as the null key or value.
*/

typedef struct w_Hashtable2k {
  void    *dummy;
  char    *label; 
  w_hashtable next;
  w_hashtable previous;
  w_int    currentsize;   
  w_int    occupancy;     
  w_int    lowthreshold;  
  w_int    highthreshold; 
  x_Monitor monitor;        
  w_word  *keys1;
  w_word  *keys2;
  w_word  *values;
#ifdef CACHE_HASHCODES
  w_word  *hashcodes;
#endif
} w_Hashtable2k;
  
/****************************************************************************/
/*                                                                          */
/* The following operations are used to create, destroy and maintain        */
/* hashtables.                                                              */
/*                                                                          */
/* ht_create   allocates a hashtable with a given                           */
/*             label, size, target load factor, hashing function,           */
/*             function to be used to compare two keys for equivalence,     */
/*             the null value for 'key' (used to mark a slot as empty),     */
/*             and the value to be returned when an ht_read or ht_write     */
/*             operation does not find an entry matching the `key'.         */
/*             These last four parameters are only used by the higher-      */
/*             level operations; if only the low-level primitives are       */
/*             used then they may be set to NULL/zero.                      */
/*                                                                          */
/* ht_rehash   yields a new hashtable with exactly the same size as the     */
/*             old, but with the contents inserted in an order which is     */
/*             in some sense "optimal".                                     */
/*                                                                          */
/*             Note: in the current implementation this operation is a      */
/*             no-op (the result returned is identical to the input value). */
/*                                                                          */
/* ht_destroy  deallocates a hashtable.                                     */
/*                                                                          */
/****************************************************************************/

w_hashtable _ht_create(const char *f, int l, char *label, w_size initialsize, w_word(*hash)(w_word), w_boolean(*equal)(w_word,w_word), w_word nullkey, w_word nullvalue);
#define ht_create(label, initialsize, hash, equal, nullkey, nullvalue) \
_ht_create(__FUNCTION__, __LINE__, label, initialsize, hash, equal, nullkey, nullvalue)

w_hashtable
ht_rehash(w_hashtable);

void
ht_destroy(w_hashtable hashtable);

/****************************************************************************/
/*                                                                          */
/* The following are the primitive operations on hashtables.                */
/* They are very low-level: most often you will want to use the higher-     */
/* level operations ht_read, ht_write ht_every, and ht_list_keys/values.    */
/*                                                                          */
/* ht_lock     lock the monitor                                             */
/*                                                                          */
/* ht_try_lock try to lock the monitor - returns immediately with TRUE if   */
/*             the lock was obtained, FALSE if it was in use.               */
/*                                                                          */
/* ht_unlock   unlock the monitor                                           */
/*                                                                          */
/* ht_lock and ht_unlock should be used to protect any sequence of probes   */
/* followed by a getkey and/or setvalue, in order to prevent mutation of    */
/* the hashtable by another thread.  The user is responsible for ensuring   */
/* that deadlocks do not occur!                                             */
/*                                                                          */
/* ht_probe    called with a given hashcode and sequence 0, 1, 2, ... this  */
/*             yields the sequence of hashslots in which an entry with that */
/*             hashcode might be stored.                                    */
/*                                                                          */
/* ht_occupied returns true iff the slot is currently in use.               */
/*                                                                          */
/* ht_getkey   yields the key currently stored in a slot.                   */
/*                                                                          */
/* ht_getvalue yields the value currently stored in a slot.                 */
/*                                                                          */
/* ht_insert   sets the key and value for a given slot (which must          */
/*             currently be unoccupied!!!)  Do not attempt to insert an     */
/*             entry with a key equal to the null value that would be ;     */
/*             naughty; similarly, do not try to fill the table completely. */
/*             In both cases your request will be silently ignored ...      */
/*                                                                          */
/* ht_setvalue modifies the value currently stored in a slot.  A value      */
/*             of nullInstance is allowed, if that's what floats your boat. */
/*                                                                          */
/* ht_delete   erases a slot (sets it unoccupied).                          */
/*                                                                          */
/* ht_resize   re-allocates new arrays of keys, values, and hashcodes,      */
/*             and re-hashes the dfata accordingly.                         */
/*                                                                          */
/* ht_check_size  if the current occupancy deviates sufficiently from the   */
/*             a suggested new size for the hashtable is returned: the      */
/*             caller should then use ht_resize to resize the table.        */
/*             This routine must be called regularly when adding to a       */
/*             table, as horrible things will happen if the table becomes   */
/*             full.  The parameter 'direction' specifies which kind of     */
/*             adjustments are acceptable:                                  */
/*               direction<0 : may contract but not expand                  */
/*               direction==0 : both are acceptable                         */
/*               direction>0 : may expand but not contract                  */
/*                                                                          */
/*             The result returned is either zero (meaning no resizing is   */
/*             required) or the new recommended size.                       */
/*                                                                          */
/*             Note: the thresholds for expanding or contracting the table  */
/*             three-quarters and one-quarter of the capacity respectively. */
/*             The new hashtable will be sized to have an  occupancy close  */
/*             to 50%.                                                      */
/*                                                                          */
/* Sample code:                                                             */
/*                                                                          */
/*   w_hashtable mytable;                                                   */
/*   w_int       myslot;                                                    */
/*   w_word      akey;                                                      */
/*   w_word      avalue;                                                    */
/*                                                                          */
/*   mytable = ht_create(0.75,NULL,NULL,0,0);                               */
/*   ht_alloc(mytable,10,NULL,NULL);                                        */
/*   ...                                                                    */
/*       initial population: here it is assumed that (1) no other thread    */
/*       will manipulate the table, (2) it cannot become full (e.g.         */
/*       because we allocated space for 10 elements and we only add 8),     */
/*       (3) the keys are all distinct (although the hashcodes may not be). */
/*   ...                                                                    */
/*   w_int seq;                                                             */
/*   for(seq=0;;++seq) {                                                    */
/*     slot = ht_probe(mytable, hashcode1, seq);                            */
/*     if(!ht_occupied(mytable,slot)) {                                     */
/*       ht_insert(mytable,slot,key1,value1);                               */
/*       break;                                                             */
/*     }                                                                    */
/*   }                                                                      */
/*   for(seq=0;;++seq) {                                                    */
/*     slot = ht_probe(mytable, hashcode2, seq);                            */
/*     if(!ht_occupied(mytable,slot)) {                                     */
/*       ht_insert(mytable,slot,key2,value2);                               */
/*       break;                                                             */
/*     }                                                                    */
/*   }                                                                      */
/*   ...                                                                    */
/*       search for an entry with a given key.                              */
/*   ...                                                                    */
/*   w_int seq;                                                             */
/*   w_boolean found;                                                       */
/*   w_word     value;                                                      */
/*                                                                          */
/*   ht_lock(mytable);                                                      */
/*   for(seq=0;;++seq) {                                                    */
/*     slot=ht_probe(mytable,hashcode,seq);                                 */
/*     if(ht_occupied(mytable,slot)) {                                      */
/*       if(ht_getkey(slot)==desired) {                                     */
/*         found = WONKA_TRUE;                                              */
/*         value = ht_getvalue(slot);                                       */
/*         break;                                                           */
/*       }                                                                  */
/*     }                                                                    */
/*     else {                                                               */
/*       found = WONKA_FALSE;                                               */
/*       break;                                                             */
/*     }                                                                    */
/*   ht_unlock(mytable);                                                    */
/*   ...                                                                    */
/*       search for an entry with a given key and either update its value   */
/*       or create a new entry with the key-value pair.                     */
/*   ...                                                                    */
/*                                                                          */
/*   ht_lock(mytable);                                                      */
/*   for(seq=0;;++seq) {                                                    */
/*     slot=ht_probe(mytable,hashcode,seq);                                 */
/*     if(ht_occupied(mytable,slot)) {                                      */
/*       if(ht_getkey(slot)==desired) {                                     */
/*         ht_setvalue(slot,newvalue);                                      */
/*         break;                                                           */
/*       }                                                                  */
/*     }                                                                    */
/*     else {                                                               */
/*       ht_insert(mytable,slot,desired,newvalue);                          */
/*       break;                                                             */
/*     }                                                                    */
/*   }                                                                      */
/*   ht_unlock(mytable);                                                    */
/*                                                                          */
/*                                                                          */
/*   ...                                                                    */
/*       search for an entry with a given key and delete its entry from     */
/*       the table.                                                         */
/*   ...                                                                    */
/*                                                                          */
/*   ht_lock(mytable);                                                      */
/*   for(seq=0;;++seq) {                                                    */
/*     slot=ht_probe(mytable,hashcode,seq);                                 */
/*     if(ht_occupied(mytable,slot)) {                                      */
/*       if(ht_getkey(slot)==desired) {                                     */
/*         ht_delete(mytable,slot);                                         */
/*         break;                                                           */
/*       }                                                                  */
/*     }                                                                    */
/*   }                                                                      */
/*   ht_unlock(mytable);                                                    */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/*                                                                          */
/****************************************************************************/

#define ht_lock(ht)   x_monitor_enter(&(ht)->monitor, x_eternal);

#define ht_try_lock(ht) (x_monitor_enter(&(ht)->monitor, x_no_wait) == xs_success)

#define ht_unlock(ht) x_monitor_exit(&(ht)->monitor);

w_int
ht_probe(w_hashtable, w_word hashcode, w_int sequence);

w_boolean
ht_occupied(w_hashtable,w_int);

void
ht_insert(w_hashtable, w_int, w_word key, w_word value);

void
ht_delete(w_hashtable, w_int);

void
ht_resize(w_hashtable, w_size newsize);

w_size
ht_check_size(w_hashtable, w_int direction);

/****************************************************************************/
/*                                                                          */
/* That was for the masochists, now come the functions for normal users.    */
/* In general these lock the hashtable for the duration of the operation,   */
/* although some also have _no_lock variants which can be used when safety  */
/* is already guaranteed, e.g. because the data structure is only accessed  */
/* by a single thread.                                                      */
/*                                                                          */
/* ht_write    searches the hashtable for the given key, and either         */
/*             - if the key is found, updates the associated value and      */
/*               returns the old value; or                                  */
/*             - if the key is not found, creates a new entry with the      */
/*               given key and value and returns nullValue.                 */
/*                                                                          */
/* ht_read     searches the hashtable for the given key, and either         */
/*             - if the key is found, returns the corresponding value; or   */
/*             - if the key is not found, returns nullValue.                */
/*                                                                          */
/* ht_findkey  same as ht_read, but instead of the value it returns the     */
/*             matching *key* that was found, or nullkey if there was no    */
/*             match.  Can be used to test whether an entry exists.         */
/*             (Note that they key found need not be identical to the       */
/*             one that was given -  it may be a 'synonym').                */
/*                                                                          */
/* ht_erase    erases the entry (if any) corresponding to the given key.    */
/*             Returns the value formerly associated with the key, or NULL. */
/*                                                                          */
/* ht_every    invokes a function on every key/value pair in the hashtable. */
/*             The hashtable is locked for the duration.                    */
/*                                                                          */
/* ht_list_keys                                                             */
/*             builds a fifo containing all the keys which occur in the     */
/*             hashtable. (The caller is reponsible for releasing the fifo  */
/*             after use)                                                   */
/*                                                                          */
/* ht_list_values                                                           */
/*             builds a fifo containing all the values which occur in the   */
/*             hashtable (including repeated values). (The caller is        */
/*             reponsible for releasing the fifo after use)                 */
/*                                                                          */
/* As far as ht_write/read/erase are concerned, the key and value are just  */
/* a w_word.  Very often they will be pointers to structures: in this case  */
/* the user is responsible for releasing the memory associated with a key   */
/* or value which is overwritten or erased.                                 */
/*                                                                          */
/* ht_register and ht_deregister are provided mainly for the benefit of     */
/* the string_hashtable.  They both assume that the value associated with   */
/* the key is some kind of occurrence counter:                              */
/*                                                                          */
/*   ht_register  searches the hashtable for the given key, and either      */
/*                - if the key is found, increments the associated value    */
/*                  returns the old key; or                                 */
/*                - if the key is not found, creates a new entry with the   */
/*                  given key and a value of 1, and returns nullkey.        */
/*                                                                          */
/*   ht_deregister  searches the hashtable for the given key, which should  */
/*                  exist in the hashtable (else the result is undefined).  */
/*                  The associated value is then decremented, and either    */
/*                  - if the result is zero then the key is deleted and     */
/*                    the deleted key is returned as a result; or           */
/*                  - the result returned is 'nullkey'.                     */
/*                                                                          */
/* The return code is intended to help the user software with its key- and  */
/* resource management.  If ht_register returns a value other than the      */
/* key it was given or nullkey, then the user will probably want to adopt   */
/* the result returned by ht_register as the "canonical" key, and free any  */
/* resources associated with the other key.  If ht_deregister returns a     */
/* result other than nullkey then the user may wish to return resources     */
/* associated with the now-defunct key.                                     */
/*                                                                          */
/* An associated value of HT_BIGNUM is treated as "saturated", and will be  */
/* be neither incremented nor decremented.                                  */
/*                                                                          */
/* All of the above come in two versions: the normal version which locks    */
/* the hashtable for the duration of the operation, and a version with the  */
/* suffix _no_lock which does not do so.  Use the _no_lock version iff you  */
/* are certain that no other thread could modify the hashtable during the   */
/* operation (e.g. because the hashtable is read-only, or because your      */
/* thread already holds some other lock which protects the hashtable).      */
/* Similarly, a series of _no_lock operations can be enclosed in            */
/* ht_lock ... ht_unlock, e.g. when adding a series of entries.             */
/*                                                                          */
/****************************************************************************/

#define HT_BIGNUM 999999999

extern w_hashtable first_hashtable;

w_word /* previous key or NULL */
ht_write_no_lock(w_hashtable, w_word key, w_word newvalue);

inline static w_word /* previous key or NULL */
ht_write(w_hashtable hashtable, w_word key, w_word newvalue) {
  w_word previous;
  ht_lock(hashtable);
  previous = ht_write_no_lock(hashtable, key, newvalue);
  ht_unlock(hashtable);

  return previous;
}

w_word /* value found or NULL */
ht_read_no_lock(w_hashtable, w_word key);

inline static w_word /* value found or NULL */
ht_read(w_hashtable hashtable, w_word key) {
  w_word value;
  ht_lock(hashtable);
  value = ht_read_no_lock(hashtable, key);
  ht_unlock(hashtable);

  return value;
}

w_word /* key found or NULL */
ht_findkey_no_lock(w_hashtable, w_word key);

inline static w_word /* key found or NULL */
ht_findkey(w_hashtable hashtable, w_word key) {
  w_word foundkey;
  ht_lock(hashtable);
  foundkey = ht_read_no_lock(hashtable, key);
  ht_unlock(hashtable);

  return foundkey;
}

w_word /* value found or NULL */
ht_erase_no_lock(w_hashtable, w_word key);

inline static w_word /* value found or NULL */
ht_erase(w_hashtable hashtable, w_word key) {
  w_word erased;

  ht_lock(hashtable);
  erased = ht_erase_no_lock(hashtable, key);
  ht_unlock(hashtable);

  return erased;
}

w_int /* number of occupied slots */
ht_every(w_hashtable, void (*fun)(w_word key,w_word value));

w_fifo /* list of keys */
ht_list_keys_no_lock(w_hashtable);

inline static w_fifo /* list of keys */
ht_list_keys(w_hashtable hashtable) {
  w_fifo fifo;
  ht_lock(hashtable);
  fifo = ht_list_keys_no_lock(hashtable);
  ht_unlock(hashtable);

  return fifo;
}

w_fifo /* list of values */
ht_list_values_no_lock(w_hashtable);

inline static w_fifo /* list of keys */
ht_list_values(w_hashtable hashtable) {
  w_fifo fifo;
  ht_lock(hashtable);
  fifo = ht_list_values_no_lock(hashtable);
  ht_unlock(hashtable);

  return fifo;
}

w_int /* number of occupied slots */
ht_iterate_no_lock(w_hashtable, void (*fun)(w_word key,w_word value, void * arg1, void * arg2), void *arg1, void *arg2);

inline static w_int /* number of occupied slots */
ht_iterate(w_hashtable hashtable,void (*fun)(w_word key,w_word value, void * arg1, void * arg2), void *arg1, void *arg2) {
  w_int count;

  ht_lock(hashtable);
  count = ht_iterate_no_lock(hashtable, fun, arg1, arg2);
  ht_unlock(hashtable);

  return count;
}

w_word /* old key or nullkey */
ht_register(w_hashtable, w_word key);

w_word /* old key or nullkey */
ht_deregister(w_hashtable, w_word key);

w_word /* unique key for value */
ht_unique(w_hashtable hashtable, w_word try, w_word value, w_word min, w_word max);


/****************************************************************************/
/*                                                                          */
/* The operations on a w_hashtable2k are essentially the same as for a      */
/* w_Hashtable.  For the time being we omit the `unique' and [de]register   */
/* functions, because these would have to return two keys instead of one ...*/
/****************************************************************************/

w_hashtable2k _ht2k_create(const char *f, int l, char *label, w_size initialsize);
#define ht2k_create(label, initialsize) \
_ht2k_create(__FUNCTION__, __LINE__, label, initialsize)

w_hashtable2k
ht2k_rehash(w_hashtable2k);

void
ht2k_destroy(w_hashtable2k hashtable);

#define ht2k_lock(ht)   x_monitor_enter(&(ht)->monitor, x_eternal);

#define ht2k_unlock(ht) x_monitor_exit(&(ht)->monitor);

w_int
ht2k_probe(w_hashtable2k, w_word hashcode, w_int sequence);

w_boolean
ht2k_occupied(w_hashtable2k,w_int);

void
ht2k_insert(w_hashtable2k, w_int, w_word key1, w_word key2, w_word value);

void
ht2k_delete(w_hashtable2k, w_int);

void
ht2k_resize(w_hashtable2k, w_size newsize);

w_size
ht2k_check_size(w_hashtable2k, w_int direction);

w_boolean
ht2k_write_no_lock(w_hashtable2k, w_word key1, w_word key2, w_word newvalue);

inline static w_boolean
ht2k_write(w_hashtable2k hashtable, w_word key1, w_word key2, w_word newvalue) {
  w_boolean result;
  ht2k_lock(hashtable);
  result = ht2k_write_no_lock(hashtable, key1, key2, newvalue);
  ht2k_unlock(hashtable);

  return result;
}

w_word /* value found or NULL */
ht2k_read_no_lock(w_hashtable2k, w_word key1, w_word key2);

inline static w_word /* value found or NULL */
ht2k_read(w_hashtable2k hashtable, w_word key1, w_word key2) {
  w_word value;
  ht2k_lock(hashtable);
  value = ht2k_read_no_lock(hashtable, key1, key2);
  ht2k_unlock(hashtable);

  return value;
}

w_boolean
ht2k_erase_no_lock(w_hashtable2k, w_word key1, w_word key2);

inline static w_boolean
ht2k_erase(w_hashtable2k hashtable, w_word key1, w_word key2) {
  w_boolean erased;

  ht2k_lock(hashtable);
  erased = ht2k_erase_no_lock(hashtable, key1, key2);
  ht2k_unlock(hashtable);

  return erased;
}

w_int /* number of occupied slots */
ht2k_every(w_hashtable2k, void (*fun)(w_word key1, w_word key2, w_word value));

w_fifo /* list of values */
ht2k_list_values(w_hashtable2k);

w_int /* number of occupied slots */
ht2k_iterate_no_lock(w_hashtable2k, void (*fun)(w_word key1, w_word key2, w_word value, void * arg1, void * arg2), void *arg1, void *arg2);

inline static w_int /* number of occupied slots */
ht2k_iterate(w_hashtable2k hashtable,void (*fun)(w_word key1, w_word key2, w_word value, void * arg1, void * arg2), void *arg1, void *arg2) {
  w_int count;

  ht2k_lock(hashtable);
  count = ht2k_iterate_no_lock(hashtable, fun, arg1, arg2);
  ht2k_unlock(hashtable);

  return count;
}

// cstring hashing
w_word cstring_hash (w_word a);
w_boolean cstring_equal (w_word a, w_word b);

#endif
