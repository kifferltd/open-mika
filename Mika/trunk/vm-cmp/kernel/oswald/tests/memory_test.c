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
** $Id: memory_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <string.h>
#include <tests.h>
#include <oswald.h>

static x_sleep second;

/*
** The stack size for our memory tester threads.
*/

#define MEMT_STACK_SIZE ((1024 * 3) + MARGIN)

/*
** The declaration of a memory test block.
*/

typedef struct t_Block * t_block;

typedef struct t_Block {
  t_block next;
  t_block previous;
  x_ubyte pattern;
  x_ubyte padding;
  x_ushort tag;
  x_size size;
  x_ubyte data[0];
} t_Block;

#ifdef WITH_LOCKS
#define x_test_lock(m, t)   x_mutex_lock(m, t)
#define x_test_unlock(m)    x_mutex_unlock(m)
#else
#define x_test_lock(m, t)   (xs_success)
#define x_test_unlock(m)    (xs_success)
#endif

/*
** Round up a certain 'value' to the given 'rounding' factor.
*/

inline static x_size round_up(x_size value, x_size rounding) {
  return (value + (rounding - 1)) & ~(rounding - 1);
}

/*
** When global_force_release is true, all threads are forced to do releases untill
** the controller thread relieves the condition. Is triggered by an out of memory error
** so that the memory implementation is replenished. The global_in_use variable tracks
** how many bytes all threads have allocated in their blocks. The global_max keeps track
** of the maximum amount that was even in use, total_blocks keeps track of the total 
** number of blocks in use by all threads and test_mutex is used to guard manipulations to
** most of these variables.
*/

static int global_force_release = 0;
static int global_in_use = 0;
static int global_max = 0;
static x_boolean take_naps = true;
static int force_released = 0;
static int total_blocks = 0;
static x_size allocations = 0;
static x_size reallocations = 0;
static x_size shrinked = 0;
static x_size growed = 0;
static x_size releases = 0;
static x_int force_next_is_allocation = 0;
static x_int force_next_is_release = 0;
static x_int total_discarded = 0;
static x_int heap_used = 0;
static x_int heap_total = 0; // The total heap we had to start with in megabytes

static x_mutex test_mutex;

/*
** How many checker threads are created.
*/

static x_int num_checkers = 140;

/*
** For the entry with value 0, we randomly allocate a number of bytes. This variable controls
** the maximum number we can allocate at such time. This is primeraly done for checking for
** fragmentation.
*/

static const x_size max_random_bytes = (1024 * 80);

/*
** The array with test sizes in bytes to allocate. Make sure at least one entry is 0. 
** This entry is used to randomly pick a size. Always make sizes word aligned (for t_Block.data)
** but not all double word aligned so that we check our rounding function too.
*/

static x_size size_selector[] = {
       0,     20,     24,     40,     64,     72,     92,    120,    512,    788,   1024,
    1500,   2000,   2200,   2500,   3000,   4000,   5000,   6000,   6500,   7000,   7200,
    8000,   9000,  11000,  14000,  17000,  23000,  27000,  60000,  70000,  90000, 130000,
};

static const int num_sizes = (sizeof(size_selector) / sizeof(x_size));

/*
** How many blocks should a test thread have in use maximum. It is a calculated
** value that is dependant on the number of bytes we start with. Calculated in
** the 'control' thread. The maximum block size is also extracted from the size_selector
** array in the controller thread.
*/

static x_size max_blocks_in_use;
static x_size max_block_size;

static x_boolean check_block(t_block b, x_boolean stop) {

  x_word tag;
  x_ubyte * data;
  x_ubyte pattern;
  x_size i;
  x_size j;
  x_int difference;

  if (b->size > max_block_size) {
    oempa("Size wrong %d (0x%08x).\n", b->size, b->size);
    exit(0);
  }

  /*
  ** The maximum difference between a size from x_mem_size and the own recorded size can be 16. A chunk
  ** is not pinched of when the leftover would be smaller than the minimum size of 16 bytes. 
  ** The difference should never become negative.
  */

  difference = x_mem_size(b) - b->size - sizeof(t_Block);
  if (difference > 16) {
    oempa("Wrong set or recorded size: diff = %d, set = %d bytes, recorded = %d bytes\n", difference, b->size, x_mem_size(b));
    exit(0);
  }

  if (difference < 0) {
    oempa("Negative difference! diff = -%d, set = %d bytes, recorded = %d bytes\n", -difference, b->size, x_mem_size(b));
    exit(0);
  }

  tag = x_mem_tag_get(b);
  
  if (tag != (x_word)b->tag) {
    oempa("Tag is wrong: tag = 0x%08x, b->tag = 0x%04x\n", tag, b->tag);
    exit(0);
  }
  
  data = b->data;
  pattern = b->pattern;
  for (i = 0; i < b->size; i++) {
    if (*data++ != pattern) {
      if (stop) {
        oempa("problem with block 0x%08x size %d at index %d (ref 0x%02x != 0x%02x).\n", b, b->size, i, pattern, data[-1]);
        for (j = i - 10 ; j < i + 10; j++) {
          oempa("%5d -> 0x%02x\n", j, b->data[j]);
        }
        exit(0);
      }
      return 0;
    }
  }

  return 1;
  
}

static void increment_count(x_size size) {

  x_status status;
  
  status = x_test_lock(test_mutex, x_eternal);
  if (status != xs_success) {
    oempa("Bad status '%s'\n", x_status2char(status));
    exit(0);
  }
  
  total_blocks += 1;
  global_in_use += size;
  if (global_in_use > global_max) {
    global_max = global_in_use;
  }

  status = x_test_unlock(test_mutex);
  if (status != xs_success) {
    oempa("Bad status '%s'\n", x_status2char(status));
    exit(0);
  }

}

static void decrement_count(x_size size) {

  x_status status;
  
  status = x_test_lock(test_mutex, x_eternal);
  if (status != xs_success) {
    oempa("Bad status '%s'\n", x_status2char(status));
    exit(0);
  }

  total_blocks -= 1;
  global_in_use -= size;

  status = x_test_unlock(test_mutex);
  if (status != xs_success) {
    oempa("Bad status '%s'\n", x_status2char(status));
    exit(0);
  }
  
}

inline static void set_data(x_ubyte * dest, x_size num, x_word wp) {

  int duffs;
  x_ubyte pattern = wp;

  duffs = (num + 15) / 16;
  switch (num % 16) {
    case  0: do { *dest++ = pattern;
    case 15:      *dest++ = pattern;
    case 14:      *dest++ = pattern;
    case 13:      *dest++ = pattern;
    case 12:      *dest++ = pattern;
    case 11:      *dest++ = pattern;
    case 10:      *dest++ = pattern;
    case  9:      *dest++ = pattern;
    case  8:      *dest++ = pattern;
    case  7:      *dest++ = pattern;
    case  6:      *dest++ = pattern;
    case  5:      *dest++ = pattern;
    case  4:      *dest++ = pattern;
    case  3:      *dest++ = pattern;
    case  2:      *dest++ = pattern;
    case  1:      *dest++ = pattern;
               } while (--duffs > 0); 
  }

}

static t_block reallocate_block(t_block list, t_block block, x_size new_size) {

  t_block new_block;
  x_size old_size = block->size;
  x_ubyte pattern = block->pattern;
  x_ubyte * data;

  decrement_count(old_size);
  x_list_remove(block);
  new_block = x_mem_realloc(block, sizeof(t_Block) + new_size);
  
  if (new_block == NULL) {
  
    /*
    ** Failed. We re-insert the old block into the list
    ** and update our count.
    */
    
    x_list_insert(list, block);
    increment_count(old_size);
    return NULL;
  }
  
  x_list_insert(list, new_block);
  increment_count(new_size);

  /*
  ** If the block has grown, fill the new room with the pattern data.
  */

  if (old_size < new_size) {
    data = & new_block->data[old_size];
    set_data(data, new_size - old_size, pattern);
    growed += 1;
  }
  else {
    shrinked += 1;
  }
  
  new_block->size = new_size;

  return new_block;
      
}

static t_block allocate_block(t_block list, x_size size) {

  x_ubyte * data;
  x_ubyte pattern;
  t_block b;
  x_size i;
  x_word tag;

  /*
  ** Make sure that our tag doesn't equal the ts-mem debug tag
  ** as we would upset the ts-mem routines.
  */
  
  do {
    tag = x_random() & 0x000000ff;  
  } while (tag > 31 || tag == 0);

  b = x_mem_calloc(sizeof(t_Block) + size);
  if (b == NULL) {
    return NULL;
  }

  x_mem_tag_set(b, tag);
  
  /*
  ** Check block is cleared correctly.
  */
  
  data = (x_ubyte *)b;
  for (i = 0; i < sizeof(t_Block) + size; i++) {
    if (*data++ != 0x00) {
      oempa("Block of size %d not cleared properly. data[%d] == 0x%02x\n", sizeof(t_Block) + size, i, data[i]);
      exit(0);
    }
  }

  b->size = size;
  pattern = (x_ubyte) x_random();

  /*
  ** Assure the highest 2 bits are set in the pattern so that 
  ** PREVIOUS_IN_USE & CURRENT_IN_USE trouble is found quickly
  */

  pattern |= 0xc0;
  b->pattern = pattern;
  b->tag = tag;

  /*
  ** Check the x_mem_is_block function. We also do some checks that should return false.
  ** One to check the alignment return, the other one is for searching the chunks.
  ** We also want to stress test on these routines with totally crazy pointers.
  */

  if (! x_mem_is_block(b)) {
    oempa("Block is not seen as allocated memory!\n");
    exit(0);
  }

  if (x_mem_is_block((unsigned char *)b + 1)) {
    oempa("Block is seen as allocated memory!\n");
    exit(0);
  }

  if (x_mem_is_block((unsigned char *)b + 16)) {
    oempa("Block is seen as allocated memory!\n");
    exit(0);
  }

  if (x_mem_is_block((unsigned char *)0xcafebabe)) {
    oempa("Cafebabe is seen as allocated memory!\n");
    exit(0);
  }

  /*
  ** Write the pattern in the block.
  */
  
  data = b->data;
  set_data(data, size, pattern);

  /*
  ** Write the tag and increment the count only afterwards to not upset our
  ** counting in the memory walker.
  */

  x_list_insert(list, b);

  increment_count(size);

  return b;
    
}

static x_boolean release_block(t_block b, x_boolean stop, x_boolean discard) {

  x_boolean result;

  result = check_block(b, stop);
  
  x_list_remove(b);
  decrement_count(b->size);

  if (discard) {
    total_discarded += 1;
    if (total_discarded % 100 == 0) {
      oempa("Discarding memory block %d...\n", total_discarded);
    }
    x_mem_discard(b);
  }
  else {
    x_mem_free(b);
  }

  return result;
  
}

/*
** Return a random size that is larger than the size of a t_Block and is
** rounded up to a word (4) size.
*/

static x_size random_size(x_size max_bytes) {

  x_int i;
  x_size size;

  i = x_random() % num_sizes;
  size = size_selector[i];
  if (size == 0) {
    size = x_random() % max_bytes;
    size += sizeof(t_Block);
    size = round_up(size, 4);
  }

  return size;
  
}

/*
** A function that returns a victim block that will be released back to the free memory
** pool. Depending on the 'which' argument, we select the best cache behaviour (blocks allocated
** most recently are returned), the worst cache behaviour (the oldest block is returned) or
** a random behaviour with a probability of 0.5.
**
** which = 0 -> worst cache behaviour
** which = 1 -> best cache behaviour
** which = 2 -> random return
*/

#define WORST_CACHE           0
#define BEST_CACHE            1
#define RANDOM_CACHE          2

static t_block victim(t_block initial, int which) {

  /*
  ** Force random when global_force_release is set.
  */
  
  if (global_force_release) {
    which = RANDOM_CACHE;
  }

  switch (which) {
    case WORST_CACHE: {
      return initial->next;
    }

    case BEST_CACHE: {
      return initial->previous;
    }
    
    case RANDOM_CACHE: {
      if (x_random() & 0x00000001) {
        return initial->next;
      }
      else {
        return initial->previous;
      }
    }
    
    default: return initial->next;
    
  }

}

static void memory_check(void * arg) {

  x_thread thread = thread_current;
  x_int i;
  x_size size;
  x_size old_size;
  x_size blocks_in_use = 0;
  t_Block Initial;
  t_block initial = &Initial;
  t_block block;
  x_int command;
  x_int threshold = 0;
  x_int operations = 0;

  /*
  ** Initialize our own list.
  */

  initial->size = 1024 * 1024 * 30;
  x_list_init(initial);

  /*
  ** ... and check dynamic allocation and deallocation forever ...
  */

  while (1) {
    x_assert(critical_status == 0);  

    /*
    ** Select the operation: 0 and 1 = allocation, 2 = reallocation and 3 = free or discard, 4 = check is_block
    */
    
    command = x_random() % 5;
    block = NULL;
    operations += 1;

    if (force_next_is_release || global_force_release) {
      command = 3;
      force_next_is_release = 0;
    }
    else if (force_next_is_allocation) {
      command = 0;
    }

    /*
    ** If we have released all our blocks due to a global force release command and
    ** we have no blocks left, we sleep so that other threads can start dumping their
    ** blocks.
    */

    if (global_force_release && blocks_in_use == 0) {
      x_thread_sleep(second * 1);
    }

    /*
    ** Only do allocations when we have not all blocks in use.
    */

    if ((command == 1 || command == 0) && (blocks_in_use < max_blocks_in_use)) { 
      i = x_random() % num_sizes;
      size = size_selector[i];
      if (size == 0) {
        size = random_size(max_random_bytes);
      }

      block = allocate_block(initial, size);
      if (block == NULL) {
        force_next_is_release = 1;
        global_force_release = 1;
        oempa("No more memory for %d bytes. Global force releasing enabled, %d bytes in use.\n", size, global_in_use);
        continue;
      }
      
      blocks_in_use += 1;
      allocations += 1;

      if (force_next_is_allocation) {
        force_next_is_allocation = 0;
      }

    }
    else if (command == 2) {
      block = victim(initial, WORST_CACHE);
      if (block != initial) {
        i = x_random() % num_sizes;
        size = size_selector[i];
        if (size == 0) {
          size = random_size(max_random_bytes);
        }

        old_size = block->size;
        block = reallocate_block(initial, block, size);
        if (block == NULL) {
          force_next_is_release = 1;
          global_force_release = 1;
          oempa("x_mem_realloc failed. Old size %d bytes, new size %d bytes.\n", old_size, size);
          continue;
        }
      
        reallocations += 1;

      }
    }
    else if (command == 3) {
      block = victim(initial, RANDOM_CACHE);
      if (block != initial) {
        release_block(block, true, (x_random() % 5 == 0));
        blocks_in_use -= 1;
        releases += 1;
      }
    }
    else {
      threshold += 1;
      if (threshold > 2) {
        block = initial->next;
        while (block != initial) {
          if (! x_mem_is_block(block)) {
            oempa("Block test wrong %p %p...\n", block, initial);
            exit(0);
          }
          block = block->next;
        }
        threshold = 0;
      }
    }

    if (allocations % 500 == 0 && command == 0) {
      heap_used = x_mem_total() - x_mem_avail();
      oempa("Thread %3d p %d: A %d Rg %d Rs %d, used %d (%d of %d Mb)\n", thread->id, thread->a_prio, allocations, growed, shrinked, heap_used, heap_used / (1024 * 1024), heap_total);
      force_next_is_allocation = 1;
    }

    /*
    ** Introduce a little nap when we are running with other test threads,
    ** so that these guys get a chance to run too, especially for the soft
    ** test threads...
    */
    
    if (operations % 1000 == 0 && take_naps) {
//XX      x_thread_sleep((x_random() % 400) + second);
      x_thread_sleep(second);
    }

  }
  
}

/*
** A memory checker thread.
*/

typedef struct t_Mchecker {
  x_thread thread;
  x_ubyte * stack;
} t_Mchecker;

static t_Mchecker * Mcheckers;

static x_thread tc;

static x_int state = 0;

/*
** The argument structure for our memory walker.
*/

typedef struct Warg {
  x_int number;        // The total number of blocks walked
  x_int testblocks;    // The total number of test blocks we encountered
  x_int bytes;         // The total number of bytes in the walked over blocks
} Warg;

typedef struct Warg * warg;

static x_boolean callback(void * mem, void * arg) {
  
  x_word tag;
  warg argument = arg;
  t_block block;
  
  argument->number += 1;
  
  /*
  ** See if the block has as tag that is not 0 and if it equals the tag
  ** of the block.
  */
  
  tag = x_mem_tag_get(mem);
  if (tag) {
    block = (t_block)mem;
    if (block->tag == tag) {
      argument->testblocks += 1;
    }
  }

  /*
  ** Add all memory in use
  */

  argument->bytes += x_mem_size(mem);

  return true;
    
}

/*
** This thread is constantly trying to allocate the maximum size block
** and if it succeeds, it releases the block immeditiately. We have a variable
** max_keep that if set, we will keep the largest allocated block and only will
** free it the next time we wake up. It is set to true in the memcheck_control
** thread.
*/

static volatile x_boolean max_keep = false;

static void max_entry(void * t) {

  void * max_block = NULL;
  x_size maximum_size = (0x000fffff << 3) - sizeof(x_word);
  x_int succeeded = 0;
  x_int failed = 0;
  x_boolean last;

  while (1) {
    x_assert(critical_status == 0);  

    if (max_keep && max_block != NULL) {
      oempa("Delayed the release of the maximum block.\n");
      x_mem_free(max_block);
      max_block = NULL;
      max_keep = false;
      x_thread_sleep(20);
    }

    max_block = x_mem_alloc(maximum_size);
    if (max_block != NULL) {
      succeeded += 1;
      if (! max_keep) {
        x_mem_free(max_block);
        max_block = NULL;
      }
      last = true;
    }
    else {
      failed += 1;
      last = false;
    }
    oempa("Allocated maximum size block %d times, failed %d times (last was %s)\n", succeeded, failed, last ? "success" : "failure");
    x_thread_sleep(second * 10);
  }
  
}

/*
** Check the number of used bytes by walking over the chunks and compare it against what
** is reported through x_mem_total and x_mem_avail. We use the special walker function
** that also reports back garbage chunks.
*/

typedef struct t_Count {
  x_int bytes_used;
  x_int mem_avail;
  x_int mem_total;
} t_Count;

typedef struct t_Count * t_count;

static x_boolean count_callback(void * mem, void * arg) {

  t_count count = arg;
    
  count->bytes_used += x_mem_size(mem);
  count->bytes_used += 4; // The header word of the chunk
  
  return true;
  
}

inline static x_int absolute(x_int a, x_int b) {

  if (a > b) {
    return a - b;
  }
  else {
    return b - a;
  }

}

x_int check_count(void) {

  static x_int loop = 0;
  t_Count Count;
  t_count count = &Count;
  x_status status;

  /*
  ** We need to lock since the check after the walking over the blocks uses the
  ** x_mem_avail and x_mem_total to check against our own count, and if we allow
  ** other threads to fiddle with the memory, the checks could fail. So we lock,
  ** and unlock only after the check has happened.
  */
  
  status = x_mem_lock(x_eternal);
  if (status != xs_success) {
    oempa("Status = '%s'\n", x_status2char(status));
    exit(0);
  }

  count->bytes_used = 0;
  count->mem_avail = 0;
  count->mem_total = 0;
  status = x_mem_walkall(x_eternal, count_callback, count);
  if (status != xs_success) {
    oempa("Status = '%s'\n", x_status2char(status));
    exit(0);
  }
  count->mem_avail = x_mem_avail();
  count->mem_total = x_mem_total();

  loop += 1;
  oempa("1            callback count = %8d bytes.\n", count->bytes_used);
  oempa("2 x_mem_total - x_mem_avail = %8d bytes.\n", count->mem_total - count->mem_avail);
  oempa("3               x_mem_total = %8d bytes.\n", count->mem_total);
  oempa("4               x_mem_avail = %8d bytes.\n", count->mem_avail);
  if (absolute(count->bytes_used, count->mem_total - count->mem_avail) != 0) {
    oempa("At loop %d. avail count wrong: 1 != 2 \n", loop);
    exit(0);
  }

  status = x_mem_unlock();
  if (status != xs_success) {
    oempa("Status = '%s'\n", x_status2char(status));
    exit(0);
  }
  
  return count->bytes_used;
  
}

static x_thread foreigner;
static x_ubyte * foreigner_stack;

static void foreigner_entry(void * t) {

  void * block = NULL;
  x_size size = 0;

  while (1) {
    if (block == NULL) {
      size = (1024 * 1024) * ((x_random() % 30) + 1);
      size += x_random() % 4096;
      block = malloc(size);
      if (block) {
        memset(block, 0xaa, size);
      }
      oempa("Malloced %d Mb (%d bytes), block = 0x%08x\n", size / (1024 * 1024), size, block);
    }
    else {
      oempa("Freeing foreign block of %d Mb (%d bytes), block = 0x%08x\n", size / (1024 * 1024), size, block);
      free(block);
      block = NULL;
      size = 0;
    }
    x_thread_sleep(second * 5);
  }
  
}

static void memcheck_control(void * t) {

  x_size avail = x_mem_avail();
  x_int i;
  x_int ready;
  x_int waiting;
  x_int max_waiting = 0;
  x_status status;
  x_boolean zero_seen = false;
  x_int loops = 0;
  x_size prio;
  x_ubyte * block;
  x_ubyte * block2;
  void ** blocks;
  const int num_blocks = 50000;
  x_int failed_at = num_blocks;
  x_size maximum_size = (0x000fffff << 3) - sizeof(x_word);
  x_size max_memory;
  Warg argument;
  x_ubyte * max_stack;
  x_thread max_thread = NULL;
  x_int thread_releases = 0;
  const x_int max_blocks_per_thread = 48;
  char * with_locks;
  x_int discards = 0;
  x_int bytes_discarded = 0;
  x_int blocks_discarded = 0;
  x_int bytes_used;

  /*
  ** If we don't have 8Mb of heap, no bother in trying to allocate it...
  */
  
  if (avail < maximum_size) {
    oempa("Avail = %d, reducing max_size block.\n", avail);
    maximum_size = avail / 2;
    oempa("Max size block now %d bytes...\n", maximum_size);
  }

#ifdef WITH_LOCKS
  with_locks = "test locks enabled";
#else
  with_locks = "no test locks";
#endif

  /*
  ** We record the number of available bytes at this point and the number of bytes that have
  ** been used at this point, to be used as offset later, when we check that x_mem_avail
  ** returns the correct number.
  */
  
//  block = x_mem_alloc(20);
//  block = x_mem_alloc(10);

//#ifdef bar
    bytes_used = 0;
    status = x_mem_walk(x_eternal, count_callback, &bytes_used);
    if (status != xs_success) {
      oempa("Status = '%s'\n", x_status2char(status));
      exit(0);
    }

    oempa("                    callback count = %8d bytes. --+\n", bytes_used);
    oempa("         x_mem_total - x_mem_avail = %8d bytes. --+\n", x_mem_total() - x_mem_avail());
    oempa("                       x_mem_total = %8d bytes.\n", x_mem_total());
    oempa("                       x_mem_avail = %8d bytes.\n", x_mem_avail());
    if ((bytes_used != (x_int)(x_mem_total() - x_mem_avail()))) {
      oempa("Avail count wrong with respect to our own calculations!\n");
      exit(0);
    }
//  exit(0);
//#endif

  /*
  ** Check for the large block behaviour first, if we have enough memory.
  */

  if (avail >= (1024 * 1024 * 16)) {
    block = x_mem_alloc(maximum_size);
    if (block == NULL) {
      oempa("Problem allocating the largest block of %d bytes.\n", maximum_size);
      exit(0);
    }
    block2 = x_mem_alloc(maximum_size);
    if (block2 == NULL) {
      oempa("Problem allocating the largest block of %d bytes.\n", maximum_size);
      exit(0);
    }
    x_mem_free(block);
    x_mem_free(block2);
  }
  
  /*
  ** Do some simple test first by allocating all the memory in random sizes
  ** and returning it in the order that we allocated it. This is the worst case
  ** scenario that could happen for a memory allocator since the top chunk is
  ** never trimmed until we release the last block.
  */

  blocks = x_mem_alloc(num_blocks * sizeof(void *));  

  for (i = 0; i < num_blocks; i++) {
    blocks[i] = x_mem_alloc(x_random() % 4096 + 10);
    if (blocks[i] == NULL) {
      oempa("No more memory at index %d\n", i);
      failed_at = i;
      break;
    }
  }

  for (i = 0; i < failed_at; i++) {
    x_mem_free(blocks[i]);
  }

  x_mem_free(blocks);

  oempa("Checked allocating all memory with %d blocks.\n", failed_at - 1);

  /*
  ** Now see if we can allocate a block of the largest size.
  */
  
  block = x_mem_alloc(maximum_size);
  if (block == NULL) {
    oempa("(2) Problem allocating the largest block of %d bytes.\n", maximum_size);
    exit(0);
  }
  x_mem_free(block);

  /*
  ** Add a single unit (8 bytes) to the maximum to trigger a NULL return.
  */

// removed because of smaller heap values < 8 MB
//  block = x_mem_alloc(maximum_size + 8);
//  if (block != NULL) {
//    oempa("Problem allocating too large a block of %d bytes.\n", maximum_size + 8);
//    exit(0);
//  }

  for (i = 14; i < 40; i += 1) {
    block = x_mem_alloc((x_size) i);
    x_mem_free(block);
  }

  /*
  ** Check and set parameters for running test.
  */

  max_block_size = max_random_bytes;
  for (i = 0; i < num_sizes; i++) {
    if (size_selector[i] > max_block_size) {
      max_block_size = size_selector[i];
    }
    if (size_selector[i] == 0) {
      zero_seen = true;
    }
  }

  if (! zero_seen) {
    oempa("Please included a 0 entry in the size_selector array for testing fragmentation.\n");
    exit(0);
  }
  
  /*
  ** Calculate the max number of blocks in use per thread, for a certain amount of memory
  ** we have. We roughly calculate that for each megabyte we have, we allow max_blocks_per_thread
  ** blocks per thread. Since 'max_blocks_in_use' is on a per thread basis, we divide by the
  ** number of checker threads we have, but we top of the max_blocks_in_use to the point were the
  ** largest block, if used for all blocks, doesn't take up all memory.
  */

  max_memory = x_mem_total();
  max_blocks_in_use = max_memory / (1024 * 1024);
  max_blocks_in_use *= max_blocks_per_thread;
  max_blocks_in_use /= num_checkers;

  if (max_blocks_in_use * max_block_size > max_memory) {
    max_blocks_in_use = max_memory / (num_checkers * max_block_size);
  }

  heap_total = x_mem_total() / (1024 * 1024);

  oempa("Maximum test block size %d bytes, %d threads, %d blocks max per thread.\n", max_block_size, num_checkers, max_blocks_in_use);

  test_mutex = x_mem_alloc(sizeof(x_Mutex));
  x_mutex_create(test_mutex);

  /*
  ** Only create this thread when there is enough memory.
  */

  if (avail > (1024 * 1024 * 12)) {
    max_thread = x_mem_alloc(sizeof(x_Thread));
    max_stack = x_mem_alloc(MEMT_STACK_SIZE);
    status = x_thread_create(max_thread, max_entry, max_thread, max_stack, MEMT_STACK_SIZE, prio_offset + 4, TF_START);
    if (status != xs_success) {
      oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
      exit(0);
    }
  }

  foreigner = x_mem_alloc(sizeof(x_Thread));
  foreigner_stack = x_mem_alloc(MEMT_STACK_SIZE);
  status = x_thread_create(foreigner, foreigner_entry, foreigner, foreigner_stack, MEMT_STACK_SIZE, prio_offset + 2, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
    
  while (1) {
    x_assert(critical_status == 0);  

    if (state == 0) {
      Mcheckers = x_mem_alloc(sizeof(t_Mchecker) * num_checkers);

      for (i = 0; i < num_checkers; i++) {
        Mcheckers[i].thread = x_mem_alloc(sizeof(x_Thread));
        Mcheckers[i].stack = x_mem_alloc(MEMT_STACK_SIZE);
        prio = 5;
        if (take_naps) {
          prio = x_random() % 5 + 5;
        }
        status = x_thread_create(Mcheckers[i].thread, memory_check, Mcheckers[i].thread, Mcheckers[i].stack, MEMT_STACK_SIZE, prio_offset + prio, TF_START);
        if (status != xs_success) {
          oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }
      
      state = 1;
    }

    /*
    ** If we are running with other test threads in other modules, we take regular naps, otherwise
    ** not. If there are more than 10 other threads running, we take naps...
    */
     
    if (thread_count > num_checkers + 10) {
      take_naps = true;
    }
    else {
      take_naps = false;
    }
    
    if (global_force_release) {
      force_released += 1;
      x_thread_sleep((x_size)(num_checkers / 4));
      heap_used = x_mem_total() - x_mem_avail();
      oempa("Going to re-enable allocation, %d forced releases, current %d bytes in use.\n", force_released, heap_used);
      global_force_release = 0;
      max_keep = true;
      if (avail > (1024 * 1024 * 12)) {
        x_thread_wakeup(max_thread);
      }
      max_waiting = 0;
      global_in_use = 0;
      x_thread_sleep(50);
    }

    ready = 0;
    waiting = 0;
    for (i = 0; i < num_checkers; i++) {
      if (isReady(Mcheckers[i].thread)) {
        ready += 1;
      }
      else {
        waiting += 1;
      }
    }
    
    if (waiting > max_waiting) {
      max_waiting = waiting;
    }

    oempa("%d Kb, %d Kb max, %d waiting (%d max), %d ready, rel. f %d n %d, %s naps.\n", global_in_use / 1024, global_max / 1024, waiting, max_waiting, ready, force_released, thread_releases, take_naps ? "" : "no");

    argument.number = 0;
    argument.testblocks = 0;
    argument.bytes = 0;
    status = x_mem_walk(x_eternal, callback, &argument);
    if (status != xs_success) {
      oempa("Status = '%s'\n", x_status2char(status));
      exit(0);
    }

    oempa("WALKER: chunks used = %d, %d (%d ref) blocks, %d Mb walked.\n", argument.number, argument.testblocks, total_blocks, argument.bytes / (1024 * 1024));

    /*
    ** Check that the avail count is correct.
    */

    bytes_used = check_count();
    oempa("Checked free bytes against x_mem_avail and x_mem_total. Used = %d bytes (%d Mb).\n", bytes_used, bytes_used / (1024 * 1024));
    
    if (loops > 1 && loops % 30 == 0) {
      thread_releases += 1;
      oempa("************* Forcing all threads to release their blocks ***************\n");
      global_force_release = 1;
      x_thread_sleep((x_sleep)(num_checkers / 4));
      heap_used = x_mem_total() - x_mem_avail();
      oempa("Heap used after total release %d bytes, %d releases, %d forced, %s.\n", heap_used, thread_releases, force_released, with_locks);
      if (avail > (1024 * 1024 * 12)) {
        x_thread_wakeup(max_thread);
      }
      global_force_release = 0;
    }

    if (loops > 1) {
      discards += 1;
      oempa("COLLECT: starting collection cycle %d.\n", discards);
      x_mem_collect(&bytes_discarded, &blocks_discarded);
      oempa("COLLECT: collection %d, collected %d Kb from %d chunks (%d reported).\n", discards, (bytes_discarded / 1024), blocks_discarded, total_discarded);
      total_discarded = 0;
    }
    
    x_thread_sleep(second * 5);
    oempa("Loop %d\n", loops);
    loops += 1;
    
  }
  
}

x_ubyte * memory_test(x_ubyte * memory) {

  x_status status;

  second = x_seconds2ticks(1);
    
  tc = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(tc, memcheck_control, tc, x_alloc_static_mem(memory, MEMT_STACK_SIZE), MEMT_STACK_SIZE, prio_offset + 1, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Thread id %d.\n", tc->id);
  }

  return memory;
  
}
