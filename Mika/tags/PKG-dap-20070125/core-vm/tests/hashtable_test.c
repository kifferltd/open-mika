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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: hashtable_test.c,v 1.2 2004/11/18 23:51:52 cvs Exp $
*/

#include <stdio.h>
#include <string.h>
#include "tests.h"
#include "oswald.h"
#include "wonka.h"
#include "oswald.h"
#include "fifo.h"
#include "hashtable.h"

#define HTT_STACK_SIZE ((1024 * 1) + MARGIN)

/*
** ------------------------------------------------------------------------------------------------
** The single key hashtable.
** ------------------------------------------------------------------------------------------------
*/

typedef struct t_Hashtable {
  w_int     currentsize;
  w_int     occupancy;
  x_Monitor monitor;        
  w_word    (*hash) (w_word);
  w_boolean (*equal)(w_word,w_word);
  w_word    nullkey;
  w_word    nullvalue;
  w_word    *keys;
  w_word    *values;
} t_Hashtable;

typedef t_Hashtable    *t_hashtable;


w_word test_dummy_hash (w_word a) {
  return a;
}


w_boolean test_dummy_equal (w_word a, w_word b) {
  return a == b;
}

/*
** Prototypes.
*/

w_word test_ht_write_no_lock(t_hashtable ht, w_word key, w_word newvalue);

/*
** Implementation.
*/

t_hashtable test_ht_create(w_size initialsize, w_word(*hash)(w_word), w_boolean(*equal)(w_word,w_word), w_word nullkey, w_word nullvalue) {
  w_word      i;
  t_hashtable ht = x_mem_get(sizeof(t_Hashtable));
  
  if(!equal) equal = test_dummy_equal;
  if(!hash) hash = test_dummy_hash;
  
  ht->currentsize = initialsize;
  ht->occupancy = 0;
  ht->hash = hash;
  ht->equal = equal;
  ht->nullkey = nullkey;
  ht->nullvalue = nullvalue;
  ht->keys = x_mem_get(ht->currentsize * sizeof(w_word));
  ht->values = x_mem_get(ht->currentsize * sizeof(w_word));
  
  x_monitor_create(&ht->monitor);

  for(i=0; i<initialsize; i++) ht->keys[i] = nullkey;
  for(i=0; i<initialsize; i++) ht->values[i] = nullvalue;

  return ht;
}


t_hashtable test_ht_rehash(t_hashtable hashtable) {

  /*
  ** No rehashing, just return the current table.
  */
  
  return hashtable;
}


w_void test_ht_destroy(t_hashtable ht) {
  x_mem_free(ht->values);
  x_mem_free(ht->keys);
  x_monitor_delete(&ht->monitor);
  x_mem_free(ht);
}


w_int test_ht_probe(t_hashtable ht, w_word hashcode, w_int sequence) {
  w_int slot = (hashcode % ht->currentsize) + sequence;
  if(slot >= ht->currentsize) slot -= ht->currentsize;
  return slot;
}


w_boolean test_ht_occupied(t_hashtable ht, w_int slot) {
  if(slot >= 0 && slot < ht->currentsize) {
    return (ht->keys[slot] != ht->nullkey);
  }
  else {
    return 0;
  }
}


w_void test_ht_insert(t_hashtable ht, w_int slot, w_word key, w_word value) {

  if(key == ht->nullkey) {
    oempa("(hashtable@%p) Attempted to insert the null key\n", ht); 
    return;
  }

  if(ht->occupancy == ht->currentsize) {
    oempa("(hashtable@%p) Attempted to insert an element in a full hashtable\n", ht, slot);
    return;
  }
  
  if(test_ht_occupied(ht, slot)) {
    oempa("(hashtable@%p) Attempted to overwrite an in use slot (%d)\n", ht, slot);
    return;
  }
    
  ht->keys[slot] = key;
  ht->values[slot] = value;
  ht->occupancy++;
}


w_void test_ht_delete(t_hashtable ht, w_int slot) {

  /*
  ** When a slot gets deleted, the neighbours which also might belong
  ** in that slot will not be touched. There's no rehashing whatsoever.
  ** This means that we have to traverse the whole table to find a key
  ** starting at the slot where one would expect it, but that we cannot
  ** stop searching if we've hit a nullkey. If a given key is not in
  ** the table, all keys in the table will be checked. 
  **
  ** But then again, this doesn't need to be a blazing fast hashtable...
  */
  
  ht->keys[slot] = ht->nullkey;
  ht->values[slot] = ht->nullvalue;
  ht->occupancy--;
}


w_void test_ht_resize(t_hashtable ht, w_size newsize) {
  t_hashtable ht_new = test_ht_create(newsize, ht->hash, ht->equal, ht->nullkey, ht->nullvalue);
  w_int       i;

  for(i=0; i<ht->currentsize; i++) {
    if(ht->keys[i] != ht->nullkey) {
      test_ht_write_no_lock(ht_new, ht->keys[i], ht->values[i]);
    }
  }
 
  ht->currentsize = ht_new->currentsize;
  ht->occupancy = ht_new->occupancy;

  x_mem_free(ht->keys);
  x_mem_free(ht->values);

  ht->keys = ht_new->keys;
  ht->values = ht_new->values;
  
  x_monitor_delete(&ht_new->monitor);
  x_mem_free(ht_new);
}


w_size test_ht_check_size(t_hashtable ht, w_int direction) {
  w_int size = ht->currentsize;             // Current size
  w_int low  = ht->currentsize * 25 / 100;  // Low water mark
  w_int high = ht->currentsize * 75 / 100;  // High water mark

  /*
  ** The preferred action :
  **  -1 = shrink
  **   0 = remain the same
  **   1 = grow
  */
  
  w_int action = 0;                         

  /*
  ** Determine the preferred action.
  */
  
  if(size < low) action = -1;
  else if(low <= size && size >= high) action = 0;
  else if(high < size) action = 1;

  /*
  ** Check what is allowed by the caller and act accordingly.
  */

  if(direction < 0 && action == -1) {
    return ht->currentsize / 2;
  }
  else if(direction > 0 && action == 1) {
    return high * 2;
  }
  else if(direction == 0) {
    switch(action) {
      case -1: return ht->currentsize / 2;
      case 0:  return 0;
      case 1:  return high * 2;
    }
  }

  return 0;
}


#define test_ht_lock(ht)   x_monitor_enter(&(ht)->monitor, x_eternal);
#define test_ht_unlock(ht) x_monitor_exit(&(ht)->monitor);


w_word  test_ht_write_no_lock(t_hashtable ht, w_word key, w_word newvalue) {
  w_int  i;
  w_int  slot;
  w_int  result = ht->nullvalue;
  w_word newsize;
  w_word hashcode = ht->hash(key);

  for(i=0; i<ht->currentsize; i++) {
    slot = test_ht_probe(ht, hashcode, i);
    if(test_ht_occupied(ht, slot)) {
      if(ht->equal(ht->keys[slot], key)) {
        oempa("(hashtable@%p) Collision with match at slot %d, key %d\n", ht, slot, key);
        result = ht->values[slot];
        ht->values[slot] = newvalue;
        break;
      }
    }
    else {
      test_ht_insert(ht, slot, hashcode, newvalue);
      newsize = test_ht_check_size(ht, 1);
      if(newsize) test_ht_resize(ht, newsize);
      break;
    }
  }
  return result;
}


w_word  test_ht_read_no_lock(t_hashtable ht, w_word key) {
  w_int  i;
  w_int  slot;
  w_int  result = ht->nullvalue;
  w_word hashcode = ht->hash(key);

  for(i=0; i<ht->currentsize; i++) {
    slot = test_ht_probe(ht, hashcode, i);
    if(test_ht_occupied(ht, slot)) {
      if(ht->equal(ht->keys[slot], key)) {
        result = ht->values[slot];
        break;
      }
    }
  }
  return result;
}


w_word  test_ht_findkey_no_lock(t_hashtable ht, w_word key) {
  w_int  i;
  w_int  slot;
  w_int  result = ht->nullkey;
  w_word hashcode = ht->hash(key);

  for(i=0; i<ht->currentsize; i++) {
    slot = test_ht_probe(ht, hashcode, i);
    if(test_ht_occupied(ht, slot)) {
      if(ht->equal(ht->keys[slot], key)) {
        result = ht->keys[slot];
        break;
      }
    }
  }
  return result;
}


w_word test_ht_erase_no_lock(t_hashtable ht, w_word key) {
  w_int  slot;
  w_word result = ht->nullvalue;
  w_word hashcode = (*ht->hash)(key);
  w_int  i;

  for(i=0; i<ht->currentsize; i++) {
    slot = test_ht_probe(ht, hashcode, i);
    if(test_ht_occupied(ht, slot)) {
      if(ht->equal(ht->keys[slot], hashcode)) {
        result = ht->values[slot];
        test_ht_delete(ht, slot);
        break;
      }
    }
  }

  return result;
}


w_int test_ht_iterate_no_lock(t_hashtable ht, void (*fun)(w_word key, w_word value, void * arg1, void * arg2), void *arg1, void *arg2) {
  w_int count = 0;
  w_int i;

  for(i=0; i<ht->currentsize; i++) {
    if(test_ht_occupied(ht, i)) {
      fun(ht->keys[i], ht->values[i], arg1, arg2);
      count++;
    }
  }

  return count;
}


w_int test_ht_every(t_hashtable ht, void (*fun)(w_word key,w_word value)) {
  w_int count = 0;
  w_int i;

  test_ht_lock(ht);

  for(i=0; i<ht->currentsize; i++) {
    if(test_ht_occupied(ht, i)) {
      fun(ht->keys[i], ht->values[i]);
      count++;
    }
  }

  test_ht_unlock(ht);

  return count;
}


w_fifo test_ht_list_keys(t_hashtable ht) {
  w_fifo  result;
  w_size  leafsize;
  w_int   i;

  leafsize = 63;
  while (leafsize < (w_size)ht->occupancy) {
    leafsize = leafsize * 2 - 1;
  }
  result = allocFifo(leafsize);

  if (result == NULL) {
    return NULL;
  }

  test_ht_lock(ht);

  for(i=0; i<ht->currentsize; i++) {
    if(test_ht_occupied(ht, i)) {
      if (putFifo((void*)ht->keys[i], result) < 0) {
        oempa("Fifo should not get full !!\n");
        exit(0);
      }
    }
  }

  test_ht_unlock(ht);

  return result;
}


w_fifo test_ht_list_values(t_hashtable ht) {
  w_fifo  result;
  w_size  leafsize;
  w_int   i;

  leafsize = 63;
  while (leafsize < (w_size)ht->occupancy) {
    leafsize = leafsize * 2 - 1;
  }
  result = allocFifo(leafsize);

  if (result == NULL) {
    return NULL;
  }

  test_ht_lock(ht);

  for(i=0; i<ht->currentsize; i++) {
    if(test_ht_occupied(ht, i)) {
      if (putFifo((void*)ht->values[i], result) < 0) {
        oempa("Fifo should not get full !!\n");
        exit(0);
      }
    }
  }

  test_ht_unlock(ht);

  return result;
}


w_word test_ht_unique(t_hashtable ht, w_word try, w_word value, w_word min, w_word max) {
  w_int   found = 0;
  w_word  result;

  result = try;
  if(result < min) {
    result = max;
  }
  if(result > max || result == 0) {
    result = min;
  }

  test_ht_lock(ht);

  while(!found) {
    if (test_ht_read_no_lock(ht, result) == 0) {
      test_ht_write_no_lock(ht, result, value);
      found = 1;
    }
    else {
      result++;
      if(result > max || result == 0) {
        result = min;
      }
    }
  }

  test_ht_unlock(ht);

  return result;
}


w_word test_ht_write(t_hashtable hashtable, w_word key, w_word newvalue) {
  w_word previous;
  test_ht_lock(hashtable);
  previous = test_ht_write_no_lock(hashtable, key, newvalue);
  test_ht_unlock(hashtable);
  return previous;
}


w_word test_ht_read(t_hashtable hashtable, w_word key) {
  w_word value;
  test_ht_lock(hashtable);
  value = test_ht_read_no_lock(hashtable, key);
  test_ht_unlock(hashtable);
  return value;
}


w_word test_ht_findkey(t_hashtable hashtable, w_word key) {
  w_word foundkey;
  test_ht_lock(hashtable);
  foundkey = test_ht_read_no_lock(hashtable, key);
  test_ht_unlock(hashtable);
  return foundkey;
}


w_word test_ht_erase(t_hashtable hashtable, w_word key) {
  w_word erased;
  test_ht_lock(hashtable);
  erased = test_ht_erase_no_lock(hashtable, key);
  test_ht_unlock(hashtable);
  return erased;
}


w_int test_ht_iterate(t_hashtable hashtable,void (*fun)(w_word key,w_word value, void * arg1, void * arg2), void *arg1, void *arg2) {
  w_int count;
  test_ht_lock(hashtable);
  count = test_ht_iterate_no_lock(hashtable, fun, arg1, arg2);
  test_ht_unlock(hashtable);
  return count;
}


/*
** ------------------------------------------------------------------------------------------------
** The 2 key hashtable.
** ------------------------------------------------------------------------------------------------
*/

typedef struct t_Hashtable2k {
  w_int     currentsize;   
  w_int     occupancy;
  x_Monitor monitor;        
  w_word    *keys1;
  w_word    *keys2;
  w_word    *values;
} t_Hashtable2k;

typedef t_Hashtable2k  *t_hashtable2k;


t_hashtable2k test_ht2k_create(w_size initialsize) {
  /* TODO */
  return NULL;
}


t_hashtable2k test_ht2k_rehash(t_hashtable2k ht) {
  /* TODO */
  return NULL;
}


w_void test_ht2k_destroy(t_hashtable2k ht) {
  /* TODO */
}


#define test_ht2k_lock(ht)   x_monitor_enter(&(ht)->monitor, x_eternal);
#define test_ht2k_unlock(ht) x_monitor_exit(&(ht)->monitor);


w_int test_ht2k_probe(t_hashtable2k ht, w_word hashcode, w_int sequence) {
  /* TODO */
  return 0;
}


w_boolean test_ht2k_occupied(t_hashtable2k ht, w_int slot) {
  /* TODO */
  return 0;
}


w_void test_ht2k_insert(t_hashtable2k ht, w_int slot, w_word key1, w_word key2, w_word value) {
  /* TODO */
}


w_void test_ht2k_delete(t_hashtable2k ht, w_int slot) {
  /* TODO */
}


w_void test_ht2k_resize(t_hashtable2k ht, w_size newsize) {
  /* TODO */
}


w_size test_ht2k_check_size(t_hashtable2k ht, w_int direction) {
  /* TODO */
  return 0;
}


w_int test_ht2k_every(t_hashtable2k ht, void (*fun)(w_word key1, w_word key2, w_word value)) {
  /* TODO */
  return 0;
}


w_fifo test_ht2k_list_values(t_hashtable2k ht) {
  /* TODO */
  return NULL;
}


w_boolean test_ht2k_write_no_lock(t_hashtable2k ht, w_word key1, w_word key2, w_word newvalue) {
  /* TODO */
  return 0;
}


w_word test_ht2k_read_no_lock(t_hashtable2k ht, w_word key1, w_word key2) {
  /* TODO */
  return 0;
}


w_boolean test_ht2k_erase_no_lock(t_hashtable2k ht, w_word key1, w_word key2) {
  /* TODO */
  return 0;
}


w_int test_ht2k_iterate_no_lock(t_hashtable2k ht, void (*fun)(w_word key1, w_word key2, w_word value, void * arg1, void * arg2), void *arg1, void *arg2) {
  /* TODO */
  return 0;
}


w_boolean test_ht2k_write(t_hashtable2k hashtable, w_word key1, w_word key2, w_word newvalue) {
  w_boolean result;
  test_ht2k_lock(hashtable);
  result = test_ht2k_write_no_lock(hashtable, key1, key2, newvalue);
  test_ht2k_unlock(hashtable);
  return result;
}


w_word test_ht2k_read(t_hashtable2k hashtable, w_word key1, w_word key2) {
  w_word value;
  test_ht2k_lock(hashtable);
  value = test_ht2k_read_no_lock(hashtable, key1, key2);
  test_ht2k_unlock(hashtable);
  return value;
}


w_boolean test_ht2k_erase(t_hashtable2k hashtable, w_word key1, w_word key2) {
  w_boolean erased;
  test_ht2k_lock(hashtable);
  erased = test_ht2k_erase_no_lock(hashtable, key1, key2);
  test_ht2k_unlock(hashtable);
  return erased;
}


w_int test_ht2k_iterate(t_hashtable2k hashtable,void (*fun)(w_word key1, w_word key2, w_word value, void * arg1, void * arg2), void *arg1, void *arg2) {
  w_int count;
  test_ht2k_lock(hashtable);
  count = test_ht2k_iterate_no_lock(hashtable, fun, arg1, arg2);
  test_ht2k_unlock(hashtable);
  return count;
}

/*
** After the reference hashtable implementations, the tests themselves...
*/


/*
** Transform a counter into something more hashy...
** This is used to generate keys. A simple counter would not get too many
** collisions, and we really like collisions when testing ;)
*/

static w_word hash(w_word i) {
  return (w_int)(7 + i * i * 97);
}

w_void hashtable_test1(w_void *t) {
  w_hashtable wht;
  t_hashtable tht;
  w_word i, j = 0;
  w_word result;
  w_word amount = 9000;

  /*
  ** Initialize some Wonka structures.
  */

  wonka_init();

  /*
  ** Create 2 hashtables, one from Wonka and one reference hashtable.
  */

  wht = ht_create("hashtable", 9127, NULL, NULL, 9999, 9999); 
  tht = test_ht_create(9127, NULL, NULL, 9999, 9999); 

  while(1) {
    
    oempa("[hashtable] Pass %d\n", j++);

    /*
    ** Fill the hashtables.
    */
    
    for(i=0; i<amount; i++) {
      result = ht_write(wht, hash(i), i);
      if(result != 9999) {
        oempa("[hashtable] Value overwritten in hashtable != nullvalue (key %d, %d != %d)\n", hash(i), result, 9999);
        exit(0);
      }
      result = test_ht_write(tht, hash(i), i);
      if(result != 9999) {
        oempa("[hashtable] Value overwritten in ref hashtable != nullvalue (key %d, %d != %d)\n", hash(i), result, 9999);
        exit(0);
      }
    }

    x_thread_sleep(1);

    /*
    ** Check if everything we added can still be retrieved.
    */
    
    for(i=0; i<amount; i++) {
      result = ht_read(wht, hash(i));
      if(result != i) {
        oempa("[hashtable] Value written to hashtable != value read from hashtable (key %d, %d != %d)\n", hash(i), i, result);
        exit(0);
      }
      result = test_ht_read(tht, hash(i));    
      if(result != i) {
        oempa("[hashtable] Value written to ref hashtable != value read from ref hashtable (key %d, %d != %d)\n", hash(i), i, result);
        exit(0);
      }
    }

    x_thread_sleep(1);

    /*
    ** Delete everything in the hashtables and check if we can get the value back.
    */ 

    for(i=0; i<amount; i++) {
      result = ht_erase(wht, hash(i));
      if(result != i) {
        oempa("[hashtable] Value deleted from hashtable != value written to hashtable (%d != %d)\n", result, i);
        exit(0);
      }
      result = test_ht_erase(tht, hash(i));    
      if(result != i) {
        oempa("[hashtable] Value deleted from ref hashtable != value written to hashtable (%d != %d)\n", result, i);
        exit(0);
      }
    }

    x_thread_sleep(1);

    /*
    ** Read the hashtables again, everything should be filled with nullvalues.
    */

    for(i=0; i<amount; i++) {
      result = ht_read(wht, hash(i));
      if(result != 9999) {
        oempa("[hashtable] Value deleted from hashtable != null value (%d != %d)\n", result, 9999);
        exit(0);
      }
      result = test_ht_read(tht, hash(i));    
      if(result != 9999) {
        oempa("[hashtable] Value deleted from ref hashtable != null value (%d != %d\n", result, 9999);
        exit(0);
      }
    }
    
    x_thread_sleep(1);
  }
  
}

static x_thread ht1_thread;

x_ubyte * hashtable_test(x_ubyte * memory) {
  x_status status;

  oempa("Starting hashtable tests\n");
 
  ht1_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(ht1_thread, hashtable_test1, ht1_thread, x_alloc_static_mem(memory, HTT_STACK_SIZE), HTT_STACK_SIZE, 4, TF_START);

  if (status != xs_success) {
    oempa("Could not start hashtable test thread... Status is '%s'\n", x_status2char(status));
    exit(0);
  }
  
  return memory;
  
}

