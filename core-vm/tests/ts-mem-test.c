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
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: ts-mem-test.c,v 1.4 2004/11/18 23:51:52 cvs Exp $
**
** The routines to test the memory allocation routines of Wonka
** contained in the ts-mem.h and ts-mem.c source files.
*/

#include "tests.h"
#include "oswald.h"

static x_sleep second;

#ifdef DEBUG
//#undef DEBUG  // Comment this line out for tests with DEBUG type chunk, enable for normal type chunk tests.
#endif

#include "ts-mem.h"
#include "mika_threads.h"

/*
** The stack size for our memory tester threads.
*/

#define MEMT_STACK_SIZE (1024 + MARGIN)

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

static int global_force_release = 0;
static int global_in_use = 0;
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

/*
** How many checker threads are created.
*/

static x_int num_checkers = 100;

/*
** How many blocks should a test thread have in use maximum, and what
** is the maximum size of a block.
*/

static x_size max_blocks_in_use = 100;
static x_size max_block_size = 1024 * 8;

/*
** The mutex to use during accounting.
*/

static x_mutex test_mutex;

/*
** A memory checker thread.
*/

typedef struct t_Mchecker {
  x_thread thread;
  x_ubyte * stack;
} t_Mchecker;

typedef struct t_Mchecker * t_mchecker;

static x_boolean check_block(t_block b, x_boolean stop) {

  x_ubyte * data;
  x_ubyte pattern;
  x_size i;

  data = b->data;
  pattern = b->pattern;
  for (i = 0; i < b->size; i++) {
    if (*data++ != pattern) {
      if (stop) {
        oempa("problem with block 0x%08x size %d at index %d (ref 0x%02x != 0x%02x).\n", b, b->size, i, pattern, data[-1]);
        exit(0);
      }
      return 0;
    }
  }

  return 1;
  
}

static void increment_count(t_block block, x_size size) {

  w_chunk chunk = block2chunk(block);
  
  total_blocks += 1;
  global_in_use += size;
}

static void decrement_count(t_block block, x_size size) {

  w_chunk chunk = block2chunk(block);

  total_blocks -= 1;
  global_in_use -= size;
}

static void change_count(x_size orsize, x_size nrsize, t_block block, x_size osize, x_size nsize) {

  global_in_use   -= osize;
  global_in_use   += nsize;

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

static t_block reallocate_block(t_mchecker mc, t_block list, t_block block, x_size new_size) {

  t_block new_block;
  x_size old_size = block->size;
  x_ubyte pattern = block->pattern;
  x_ubyte * data;
  x_size which = x_random() % 5;
  x_size orsize;
  x_size nrsize;
  w_chunk chunk;

  x_mutex_lock(test_mutex, x_eternal);

  x_list_remove(block);

  chunk = block2chunk(block);
  orsize = x_mem_size(chunk);

  /*
  ** Use 'which' to bring some variation in the __LINE__ number.
  */
  
  if (which == 0) {
    new_block = reallocMem(block, sizeof(t_Block) + new_size);
  }
  else if (which == 1) {
    new_block = reallocMem(block, sizeof(t_Block) + new_size);
  }
  else if (which == 2) {
    new_block = reallocMem(block, sizeof(t_Block) + new_size);
  }
  else if (which == 3) {
    new_block = reallocMem(block, sizeof(t_Block) + new_size);
  }
  else {
    new_block = reallocMem(block, sizeof(t_Block) + new_size);
  }
  
  if (new_block == NULL) {
    x_list_insert(list, block);
    x_mutex_unlock(test_mutex);
    return NULL;
  }
  else {
    chunk = block2chunk(new_block);
    nrsize = x_mem_size(chunk);
    new_block->size = new_size;
    change_count(orsize, nrsize, new_block, old_size, new_size);
  }
  
  x_list_insert(list, new_block);

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

  x_mutex_unlock(test_mutex);

  return new_block;

}

static t_block allocate_block(t_mchecker mc, t_block list, x_size size, x_boolean cleared) {

  x_ubyte * data;
  x_ubyte pattern;
  t_block b;
  x_size i;
  x_word tag;
  x_size which = x_random() % 5;

  tag = ID_anon;

  x_mutex_lock(test_mutex, x_eternal);

  /*
  ** We use the which parameter to bring some variation in the __LINE__ number
  ** for checking the memory dumper.
  */
  
  if (cleared) {
    if (which == 0) {
      b = allocClearedMem(sizeof(t_Block) + size);
    }
    else if (which == 1) {
      b = allocClearedMem(sizeof(t_Block) + size);
    }
    else if (which == 2) {
      b = allocClearedMem(sizeof(t_Block) + size);
    }
    else if (which == 3) {
      b = allocClearedMem(sizeof(t_Block) + size);
    }
    else {
      b = allocClearedMem(sizeof(t_Block) + size);
    }
  }
  else {
    if (which == 0) {
      b = allocMem(sizeof(t_Block) + size);
    }
    else if (which == 1) {
      b = allocMem(sizeof(t_Block) + size);
    }
    else if (which == 2) {
      b = allocMem(sizeof(t_Block) + size);
    }
    else if (which == 3) {
      b = allocMem(sizeof(t_Block) + size);
    }
    else {
      b = allocMem(sizeof(t_Block) + size);
    }
  }

  if (b == NULL) {
    x_mutex_unlock(test_mutex);
    return NULL;
  }
  
  /*
  ** Check block is cleared correctly.
  */
  
  if (cleared) {
    data = (x_ubyte *)b;
    for (i = 0; i < sizeof(t_Block) + size; i++) {
      if (*data++ != 0x00) {
        oempa("Block of size %d not cleared properly. data[%d] == 0x%02x\n", sizeof(t_Block) + size, i, data[i]);
        exit(0);
      }
    }
  }

  b->size = size;
  pattern = (x_ubyte) x_random();
  b->pattern = pattern;

  /*
  ** Write the pattern in the block.
  */
  
  data = b->data;
  set_data(data, size, pattern);

  x_list_insert(list, b);

  increment_count(b, size);

  setMemTag(b, 33);
  
  x_mutex_unlock(test_mutex);

  return b;
    
}

static x_boolean release_block(t_mchecker mc, t_block b, x_boolean stop, x_boolean discard) {

  x_boolean result;

  result = check_block(b, stop);
  
  x_mutex_lock(test_mutex, x_eternal);

  x_list_remove(b);
  decrement_count(b, b->size);

  if (discard) {
    total_discarded += 1;
    if (total_discarded % 1000 == 0) {
      oempa("Discarding Wonka Chunk %d...\n", total_discarded);
    }
    discardMem(b);
  }
  else {
    releaseMem(b);
  }

  x_mutex_unlock(test_mutex);

  return result;
  
}

/*
** Round up a certain 'value' to the given 'rounding' factor.
*/

inline static x_size round_up(x_size value, x_size rounding) {
  return (value + (rounding - 1)) & ~(rounding - 1);
}

/*
** Return a random size. Note that the size of a block will be
** added in the allocation routines themselves.
*/

static x_size random_size(void) {

  x_size size = 1;

  size += x_random() % max_block_size;

  return size;
  
}

static void ts_mem_check(void * arg) {

  t_mchecker mc = arg;
  x_thread thread = mc->thread;
  x_size size;
  x_size old_size;
  x_size blocks_in_use = 0;
  t_Block Initial;
  t_block initial = &Initial;
  t_block block;
  x_int command;
  x_size discards = 0;
  x_size frees = 0;
  x_size allocs = 0;
  x_size reallocs_g = 0;
  x_size reallocs_s = 0;

  /*
  ** Initialize our own list.
  */

  initial->size = 1024 * 1024 * 30;
  x_list_init(initial);

  /*
  ** ... and check dynamic allocation and deallocation forever ...
  */

  while (1) {
  
    /*
    ** Select the operation: 0 and 1 = allocation, 2 = reallocation, 3 = change owner and 4 = free or discard
    */
    
    command = x_random() % 5;
    block = NULL;

    if (force_next_is_release || global_force_release) {
      command = 4;
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
      x_thread_sleep(2);
    }

    /*
    ** Only do allocations when we have not all blocks in use.
    */

    if ((command == 1 || command == 0) && (blocks_in_use < max_blocks_in_use)) { 
      size = random_size();

      block = allocate_block(mc, initial, size, x_random() & 0x00000001);
      allocs += 1;
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
      block = initial->next;
      if (block != initial) {
        size = random_size();
        old_size = block->size;
        block = reallocate_block(mc, initial, block, size);
        if (old_size < size) {
          reallocs_g += 1;
        }
        else {
          reallocs_s += 1;
        }
        allocs += 1;
        if (block == NULL) {
          force_next_is_release = 1;
          global_force_release = 1;
          continue;
        }
      
        reallocations += 1;

      }
    }
    else if (command == 3) {
      block = initial->next;
    }
    else {
      block = initial->next;
      if (block != initial) {
        if (x_random() % 5 == 0) {
          release_block(mc, block, true, 1);
          discards += 1;
        }
        else {
          release_block(mc, block, true, 0);
          frees += 1;
        }
        blocks_in_use -= 1;
        releases += 1;
      }
    }

    if (allocs > 0 && allocs % 4000 == 0) {
      oempa("Thread %p: A %d Rg %d Rs %d D %d F %d\n", thread, allocs, reallocs_g, reallocs_s, discards, frees);
      force_next_is_allocation = 1;
    }

    if (allocs > 0 && allocs % 2000 == 0) {
      x_thread_sleep((x_random() % 10) + 10);
    }

  }
  
}

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

/*
** The chunk madgic word used by the Wonka memory routines.
*/

#define CHUNK_MADGIC  (0xbbbbbbbb)

static x_boolean callback(void * mem, void * a) {
  
  x_word tag;
  warg arg = a;
  t_block block;
  w_chunk chunk;
  
  arg->number += 1;
  
  /*
  ** See if the block has as tag that is a Wonka tag.
  */
  
  tag = x_mem_tag_get(mem);
  if (tag > 31) {
    block = (t_block)mem;
    arg->testblocks += 1;
    chunk = (w_chunk)block->data;
#ifdef DEBUG
    if (chunk->madgic == CHUNK_MADGIC) {
      // DEBUG defined
    }
    else {
      // DEBUG not defined
    }
#else
#endif
  }

  /*
  ** Add all memory in use
  */

  arg->bytes += x_mem_size(mem);
  
  return true;
}

static void ts_mem_control(void * t) {

//  x_size avail = heap_end - heap_start;
  x_int i;
  x_status status;
  x_int loops = 0;
  Warg argument;
  x_int thread_releases = 0;
  x_int discards = 0;
  x_int bytes_discarded = 0;
  x_int blocks_discarded = 0;
  x_int from_tests;
  x_int total_check;

  test_mutex = x_mem_calloc(sizeof(x_Mutex));
  x_mutex_create(test_mutex);

  while (1) {

    if (state == 0) {
      Mcheckers = x_mem_alloc(sizeof(t_Mchecker) * num_checkers);

      for (i = 0; i < num_checkers; i++) {
        Mcheckers[i].thread = x_mem_alloc(sizeof(x_Thread));
        Mcheckers[i].stack = x_mem_alloc(MEMT_STACK_SIZE);
        status = x_thread_create(Mcheckers[i].thread, ts_mem_check, &Mcheckers[i], Mcheckers[i].stack, MEMT_STACK_SIZE, 5, TF_START);
        if (status != xs_success) {
          oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }
      
      state = 1;
    }

    if (global_force_release) {
      force_released += 1;
      x_thread_sleep((x_size)(num_checkers / 4));
      oempa("Going to re-enable allocation, %d forced releases, current %d bytes in use.\n", force_released);
      global_force_release = 0;
      x_thread_sleep(2);
    }

    oempa("%d bytes in use, A = %d, R = %d, Rs = %d Rg = %d\n", global_in_use, allocations, reallocations, shrinked, growed);
    oempa("%d checker threads, %d free bytes.\n", num_checkers, x_mem_avail());

    argument.number = 0;
    argument.testblocks = 0;
    argument.bytes = 0;
    status = x_mem_walk(x_eternal, callback, &argument);
    if (status != xs_success) {
      oempa("Status = '%s'\n", x_status2char(status));
      exit(0);
    }
    
    oempa("WALKER: chunks used = %d, %d (%d ref) blocks, %d Mb walked.\n", argument.number, argument.testblocks, total_blocks, argument.bytes / (1024 * 1024));

#if defined(DEBUG)
    heapCheck;
#endif

    if (loops > 1 && loops % 30 == 0) {
      thread_releases += 1;
      oempa("************* Forcing all threads to release their blocks ***************\n");
      global_force_release = 1;
      x_thread_sleep((x_sleep)(num_checkers / 4));
      oempa("Heap used after total release %d bytes, %d releases, %d forced\n", thread_releases, force_released);
      global_force_release = 0;
    }

    if (loops > 1) {
      x_mutex_lock(test_mutex, x_eternal);
      x_mem_lock(x_eternal);
      discards += 1;
      oempa("WONKA COLLECT: starting collection cycle %d, free = %d bytes.\n", discards, x_mem_avail());
      x_mem_collect(&bytes_discarded, &blocks_discarded);
      oempa("WONKA COLLECT: collection %d, collected %d Kb from %d chunks (%d reported), free = %d bytes.\n", discards, (bytes_discarded / 1024), blocks_discarded, total_discarded, x_mem_avail());
      total_discarded = 0;
#ifdef DEBUG
      if (loops % 10 == 0) {
        reportMemStat((loops % 2 == 0) ? 1 : 2);
      }
#endif
      total_check = 0;
      oempa("CHECK %d == %d ? %s\n", global_in_use, total_check, (global_in_use == total_check) ? "YES" : "NO");
      if (global_in_use != total_check) {
        exit(0);
      }
      x_mem_unlock();
      x_mutex_unlock(test_mutex);
    }
    
    x_thread_sleep(second * 3);
    loops += 1;
    
  }
  
}

x_ubyte * ts_mem_test(x_ubyte * memory) {

  x_status status;
  
  second = x_seconds2ticks(1);
  
  tc = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(tc, ts_mem_control, tc, x_alloc_static_mem(memory, MEMT_STACK_SIZE), MEMT_STACK_SIZE, 2, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Thread id %p.\n", tc);
  }

  return memory;
  
}

