/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2010 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
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

#include "oswald.h"

x_size heap_size; 
x_size heap_remaining = 4096;

x_size min_heap_bytes;
x_size max_heap_bytes;

x_monitor memory_monitor;

x_status x_mem_lock(x_sleep timeout) {
  return x_monitor_enter(memory_monitor, timeout);
}

x_status x_mem_unlock() {
  return x_monitor_exit(memory_monitor);
}

// ---------------------- USE NATIVE MALLOC ---------------------------
#ifdef USE_NATIVE_MALLOC

const char *magic = "This memory is valid.";

static o4p_Memory_Chunk Memory_Sentinel;
static o4p_memory_chunk memory_sentinel;

static void *chunk2mem(o4p_memory_chunk chunk) {
  return ((char*)chunk) + sizeof(o4p_Memory_Chunk);
}

static o4p_memory_chunk mem2chunk(void *mem) {
  return (o4p_memory_chunk)(((char*)mem) - sizeof(o4p_Memory_Chunk));
}

void x_mem_init(void) {
  memory_sentinel = &Memory_Sentinel;
  x_list_init(memory_sentinel);
  memory_sentinel->id = 0;
  memory_sentinel->size = 0;
#ifdef DEBUG
  memory_sentinel->file = __FILE__;
  memory_sentinel->line = __LINE__;
  memory_sentinel->check = (char*)magic;
#endif

  memory_monitor = calloc(1, sizeof(x_Monitor));
  x_monitor_create(memory_monitor);
}

#ifdef DEBUG
void *_x_mem_alloc(w_size size, const char *file, int line) {
  o4p_memory_chunk newchunk;

  if (size > heap_remaining) {
    loempa(9,"%s:d Attempt to allocate %d bytes, available space is %d!\n",file,line,size, heap_remaining);

    return NULL;
  }
  else {
    newchunk = malloc(sizeof(o4p_Memory_Chunk) + size);
  }

  if (!newchunk) {
    loempa(9,"%s:%d Out of memory!  heap_remaining was %d, setting it to 0\n", file, line, heap_remaining);
    heap_remaining = 0;

    return NULL;

  }


  newchunk->id = 0;
  newchunk->file = (char*)file;
  newchunk->line = line;
  newchunk->size = size;
  newchunk->check = (char*)magic;
  loempa(1,"%s:%d Allocated %d bytes at %p\n", newchunk->file, newchunk->line, size, newchunk);
  x_mem_lock(x_eternal);
  x_list_insert(memory_sentinel, newchunk);
  x_mem_unlock();
  heap_remaining -= size + sizeof(o4p_Memory_Chunk);
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

void *_x_mem_calloc(w_size size, const char *file, int line) {
  o4p_memory_chunk newchunk;

  if (size > heap_remaining) {
    loempa(9,"%s:d Attempt to allocate %d bytes, available space is %d!\n",file,line,size, heap_remaining);

    return NULL;
  }

  newchunk = calloc(sizeof(o4p_Memory_Chunk) + size, 1);

  if (!newchunk) {
    loempa(9,"%s:%d Out of memory!  heap_remaining was %d, setting it to 0\n", file, line, heap_remaining);
    heap_remaining = 0;

    return NULL;

  }

  newchunk->file = (char*)file;
  newchunk->line = line;
  newchunk->size = size;
  newchunk->check = (char*)magic;
  loempa(1,"%s:%d Allocated %d bytes at %p\n", newchunk->file, newchunk->line, size, newchunk);
  x_mem_lock(x_eternal);
  x_list_insert(memory_sentinel, newchunk);
  x_mem_unlock();
  heap_remaining -= size + sizeof(o4p_Memory_Chunk);
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

void *_x_mem_realloc(void *old, w_size size, const char *file, int line) {
  o4p_memory_chunk oldchunk = mem2chunk(old);
  o4p_memory_chunk newchunk;
  w_size oldsize;
  w_size newsize;

  if (oldchunk->check != magic) {
    loempa(9,"Memory block %p is not valid!\n", old);

    return NULL;

  }

  if (size - oldchunk->size > heap_remaining) {
    loempa(9,"%s:d Attempt to allocate %d bytes, available space is %d!\n",file,line,size - oldchunk->size, heap_remaining);

    return NULL;
  }

  x_mem_lock(x_eternal);
  oldsize = oldchunk->size + sizeof(o4p_Memory_Chunk);
  newsize = size + sizeof(o4p_Memory_Chunk);
  newchunk = realloc(oldchunk, newsize);
  if (newchunk) {
    newchunk->file = (char*)file;
    newchunk->line = line;
    newchunk->size = size;
    if (newchunk != oldchunk) {
      x_list_remove(oldchunk);
      x_list_insert(memory_sentinel, newchunk);
    }
    x_mem_unlock();
    heap_remaining -= newsize - oldsize;
    loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

    return chunk2mem(newchunk);
  }
  else {
    x_mem_unlock();
    x_mem_free(old);
    heap_remaining = 0;
  }

  return NULL;
}

#else

void *_x_mem_alloc(w_size size) {
  o4p_memory_chunk newchunk;

  if (size > heap_remaining) {

    return NULL;
  }

  newchunk = malloc(sizeof(o4p_Memory_Chunk) + size);

  if (!newchunk) {
    heap_remaining = 0;

    return NULL;

  }

  newchunk->id = 0;
  newchunk->size = size;
  x_mem_lock(x_eternal);
  x_list_insert(memory_sentinel, newchunk);
  x_mem_unlock();
  heap_remaining -= size + sizeof(o4p_Memory_Chunk);
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

void *_x_mem_calloc(w_size size) {
  o4p_memory_chunk newchunk;

  if (size > heap_remaining) {

    return NULL;
  }

    newchunk = calloc(sizeof(o4p_Memory_Chunk) + size, 1);

  if (!newchunk) {
    heap_remaining = 0;

    return NULL;

  }

  newchunk->size = size;
  x_mem_lock(x_eternal);
  x_list_insert(memory_sentinel, newchunk);
  x_mem_unlock();
  heap_remaining -= size + sizeof(o4p_Memory_Chunk);
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

void *_x_mem_realloc(void *old, w_size size) {
  o4p_memory_chunk oldchunk = mem2chunk(old);
  o4p_memory_chunk newchunk;
  w_size oldsize;
  w_size newsize;

  if (size - oldchunk->size > heap_remaining) {
    loempa(9,"%s:d Attempt to allocate %d bytes, available space is %d!\n",file,line,size - oldchunk->size, heap_remaining);

    return NULL;
  }

  x_mem_lock(x_eternal);
  oldsize = oldchunk->size + sizeof(o4p_Memory_Chunk);
  newsize = size + sizeof(o4p_Memory_Chunk);
  newchunk = realloc(oldchunk, newsize);
  if (!newchunk) {
    heap_remaining = 0;
    x_mem_free(old);
    x_mem_unlock();

    return NULL;

  }

  newchunk->size = size;
  if (newchunk != oldchunk) {
    x_list_remove(oldchunk);
    x_list_insert(memory_sentinel, newchunk);
  }
  x_mem_unlock();
  heap_remaining -= newsize - oldsize;
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

#endif

void x_mem_free(void *block) {
  o4p_memory_chunk chunk = mem2chunk(block);
  
#ifdef DEBUG
  if (chunk->check != magic) {
    loempa(9,"Memory block %p is not valid!\n", block);
  }
#endif

  loempa(1,"Returning %d bytes at %p allocated at %s:%d\n", chunk->size, block, chunk->file, chunk->line);
  x_mem_lock(x_eternal);
#ifdef DEBUG
  chunk->check = NULL;
#endif
  x_list_remove(chunk);
  heap_remaining += chunk->size + sizeof(o4p_Memory_Chunk);
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);
  x_mem_unlock();

  free(chunk);
}

w_size x_mem_avail(void) {
  return heap_remaining;
}

x_status x_mem_walk(x_sleep timeout, x_boolean (*callback)(void * mem, void * arg), void * arg) {
  x_status status = xs_success;
  o4p_memory_chunk cursor;
  o4p_memory_chunk next;
  
  status = x_monitor_enter(memory_monitor, timeout);
  if (status != xs_success) {
    return status;
  }

  /*
  ** We loop over the chunks. We precalculate the next chunk before we call
  ** the callback for safety reasons.  (It is allowed for the callback function
  ** to release the chunk, for instance).
  */
  
  for (cursor = memory_sentinel->next; cursor != memory_sentinel; cursor = next) {
    next = cursor->next;
    if (!callback(chunk2mem(cursor), arg)) {
      break;
    }
  }

  status = x_monitor_exit(memory_monitor);

  return status;
}

x_status x_mem_scan(x_sleep timeout, x_word tag, x_boolean (*callback)(void * mem, void * arg), void * arg) {
  x_status status = xs_success;
  o4p_memory_chunk cursor;
  o4p_memory_chunk next;
  
  status = x_monitor_enter(memory_monitor, timeout);
  if (status != xs_success) {
    return status;
  }

  /*
  ** We loop over the chunks. We precalculate the next chunk before we call
  ** the callback for safety reasons.  (It is allowed for the callback function
  ** to release the chunk, for instance).
  */
  
  for (cursor = memory_sentinel->next; cursor != memory_sentinel; cursor = next) {
    next = cursor->next;

    if (cursor->id & tag) {
      if (!callback(chunk2mem(cursor), arg)) {
        break;
      }
    }
  }

  status = x_monitor_exit(memory_monitor);

  return status;
}

x_status x_mem_tag_set(void * mem, w_word tag) {
  o4p_memory_chunk chunk = mem2chunk(mem);
  
#ifdef DEBUG
  if (chunk->check != magic) {
    loempa(9,"Memory block %p is not valid!\n", mem);

    return xs_unknown;
  }
#endif

  loempa(1,"Marking chunk %p (allocated at %s:%d) with id 0x%x\n", mem, chunk->file, chunk->line, tag);
  chunk->id |= tag;

  return xs_success;
}

w_word x_mem_tag_get(void * mem) {
  o4p_memory_chunk chunk = mem2chunk(mem);
  
#ifdef DEBUG
  if (chunk->check != magic) {
    loempa(9,"Memory block %p is not valid!\n", mem);

    return 0;

  }
#endif

  loempa(1,"Chunk %p (allocated at %s:%d) has id 0x%x\n", mem, chunk->file, chunk->line, chunk->id);

  return chunk->id;
}

w_size x_mem_size(void * mem) {
  o4p_memory_chunk chunk = mem2chunk(mem);
  
#ifdef DEBUG
  if (chunk->check != magic) {
    loempa(9,"Memory block %p is not valid!\n", mem);

    return 0;

  }
#endif

  loempa(1,"Chunk %p (allocated at %s:%d) has size %d\n", mem, chunk->file, chunk->line, chunk->size);

  return chunk->size;
}

w_boolean x_mem_is_block(void * mem) {
#ifdef DEBUG
  o4p_memory_chunk chunk = mem2chunk(mem);
  
  return chunk->check == magic;
#else
  return 1;
#endif
}

struct collect_result {
  w_size collect_bytes;
  w_size collect_count;
} collect_result;

static x_boolean discard_callback(void * mem, void * arg) {
  o4p_memory_chunk chunk = mem2chunk(mem);

  // Really the struct collect_result should be passed as `arg', I'm just being lazy tonight.
  if (isSet(chunk->id, GARBAGE_TAG)) {
    collect_result.collect_bytes += chunk->size;
    collect_result.collect_count += 1;
    x_mem_free(mem);
  }
  return TRUE;
}

void x_mem_discard(void * block) {
  o4p_memory_chunk chunk = mem2chunk(block);

  setFlag(chunk->id, GARBAGE_TAG);
}

x_status x_mem_collect(w_size * bytes, w_size * num) {
  x_status status = x_mem_walk(x_eternal, discard_callback, &collect_result);
  if (status == xs_success && *bytes) {
    *bytes = collect_result.collect_bytes;
  }
  if (status == xs_success && *num) {
    *num = collect_result.collect_count;
  }

  return status;
}


w_size x_mem_total() { 
  return heap_size; 
}

#else

x_size min_heap_bytes;
x_size max_heap_bytes;

/*
** We use a monitor to lock access to shared resources so that the same thread can
** lock more than once, since we use the free routines also in realloc
** and we would otherwise deadlock.
*/

x_monitor memory_monitor;

/*
** We define 2 types for use in this package. The 'x_units' type
** indicates the number of double words for a size and the 'x_bytes'
** type indicates the number of bytes for a size.
*/

typedef signed int x_units;
typedef signed int x_bytes;

/*
** To keep track of the number of units used.
*/

static volatile x_units free_units;

typedef struct x_Chunk * x_chunk;

typedef struct x_Chunk {
  x_units prev_size;
  x_units this_size;
  x_chunk next;
  x_chunk previous;
} x_Chunk;

/*
** Conversion from a user pointer to a chunk pointer.
*/

inline static x_chunk mem2chunk(void * mem) {
  return (x_chunk)((unsigned char *)mem - 2 * sizeof(x_word));
}

/*
** The masks to extract information out of the 'this_size' field of a chunk.
*/

#define PREVIOUS_IN_USE        0x80000000 // Single bit that indicates if the previous chunk is in use
#define CURRENT_IN_USE         0x40000000 // Single bit that indicates that THIS chunk is in use
#define GARBAGE_TAG            0x20000000 // Piece of memory is garbage, can be reclaimed by our OWN garbage collector
#define BIG_CHUNK              0x10000000 // Foreign block or block > 8 Mbytes, no tag information
#define TAG_MASK               0x0ff00000 // The tag can hold an 8 bit information NUMBER, numbers 0 - 31 are reserved.
#define USED_UNITS_MASK        0x000fffff // The number of double words units size of a used chunk (20 bits)
#define FREE_UNITS_MASK        0x0fffffff // The mask for the units size of a free chunk (29 bits)
#define TAG_SHIFT                    (20) // The number of bits we have to shift a tag word up to get it into position

/*
NEW BINS INFO (TODO)
*/

/*
** Note that the bin structure is internal to this file and looks exactly like a x_Chunk structure from
** a memory layout point of view. This is *ESSENTIAL* so if you change something, make it such that it looks
** and smells like an x_Chunk, only to the taste I'm indifferent...
*/

typedef struct x_Bin * x_bin;

typedef struct x_Bin {
  x_word dummy1;
  x_word binlock;
  x_chunk next;
  x_chunk previous;
} x_Bin;

/*
** The special top chunk that will always hold the chunk associated with
** the latest sbrk.
*/

static x_chunk top;

/*
** The number of available bins.
*/

#define NUMBER_OF_BINS 128

/*
** The array of all bins.
*/

static x_Bin bins[NUMBER_OF_BINS];

/*
** The bin that will hold the last remainder of a split chunk. It is the bin at index 1.
*/

static x_chunk last_remainder;

/*
** The chunk that has been allocated initially.
*/

static x_chunk initial;

/*
** The invalid value of a chunk in an index slot.
*/

#define cleared_slot (x_chunk)0xffffffff

/*
** Return the bin associated with the index.
*/

inline static x_chunk bin_at(x_size indexx) {
  return (x_chunk)&bins[indexx];
}

#define last(x)                   ((x)->previous)
#define first(x)                  ((x)->next)
#define bin_empty(bin)            ((bin)->previous == (bin))

/*
** Return the bin, next to this one.
*/

inline static x_chunk next_bin(x_chunk chunk) {
  return (x_chunk)((unsigned char *)chunk + sizeof(x_Bin));
}

inline static void tag_set(x_chunk chunk, x_word tag) {
  chunk->this_size = ((chunk->this_size & (x_size)~TAG_MASK) | ((tag << TAG_SHIFT) & TAG_MASK));
}

#ifdef USE_HOST_SBRK
#else
#define MAX_ZONES 64
#ifdef __uClinux__
static const x_size max_zone_size = 1024 * 1024; // seems to be max for uClinux
static const x_size min_zone_size = 256 * 1024;
#else
#define max_zone_size max_heap_bytes             // bogus, for emulation on "real" Linux
#define min_zone_size min_heap_bytes             // likewise
#endif
static x_size zone_size;
static x_size total_allocated;

static struct {
  char * base;
  x_size size;
} zone[MAX_ZONES];
static int zones_allocated;
static int fake_break_zone;
static char* fake_base;
static char* fake_break;

static void init_zones(void) {
  int i;
  int j;

  zone_size = max_zone_size;
  for (zones_allocated = 0; zones_allocated < MAX_ZONES;) {
    if (total_allocated + (min_zone_size / 2) > max_heap_bytes) {
      // printf("have allocated %d bytes in %d zones\n", total_allocated, zones_allocated);
      break;
    }
    while (total_allocated  + zone_size > max_heap_bytes + (min_zone_size / 2) && zone_size > min_zone_size) {
      zone_size /= 2;
      // printf("have allocated %d bytes in %d zones, reducing zone size to %d so as not to overrun max\n", total_allocated, zones_allocated, zone_size);
    }
    zone[zones_allocated].base = malloc(zone_size);
    if (zone[zones_allocated].base == NULL) {
      if (zone_size > min_zone_size) {
        zone_size /= 2;
        // printf("reducing zone_size to %d because alloc failed\n", zone_size);
        zone[zones_allocated].base = malloc(zone_size);
        if (zone[zones_allocated].base == NULL) {
          // printf("unable to allocate memory zone[%d]\n", zones_allocated);
          break;
        }
      }
      else {
        // printf("unable to allocate memory zone[%d]\n", zones_allocated);
        break;
      }
    }
    else {
      zone[zones_allocated++].size = zone_size;
      total_allocated += zone_size;
    }
  }

  // printf("Successfully allocated %d zones totalling %d bytes (%d requested)\n", zones_allocated, total_allocated, max_heap_bytes);
  max_heap_bytes = total_allocated;

  for (i = 0; i < zones_allocated; ++i) {
    for (j = 1; j < zones_allocated - i; ++j) {
      if (zone[j - 1].base - zone[j].base > 0) {
        char * tempbase;
        x_size tempsize;

        // printf("Swapping zone[%d] with zone [%d]\n", j - 1, j);
        tempbase = zone[j - 1].base;
        zone[j - 1].base = zone[j].base;
        zone[j].base = tempbase;
        tempsize = zone[j - 1].size;
        zone[j - 1].size = zone[j].size;
        zone[j].size = tempsize;
      }
    }
  }

  // printf("\nZones allocated (before merging): %d\n", zones_allocated);
  for (i = 0; i < zones_allocated; ++i) {
    // printf("  Zone[%d] : from %p to %p, size = %d bytes\n", i, zone[i].base, zone[i].base + zone[i].size - 1, zone[i].size);
  }

  for (i = zones_allocated - 1; i > 0; --i) {
    if (zone[i].base == zone[i-1].base + zone[i-1].size) {
      // printf("Merging zone[%d] with zone[%d]\n", i, i - 1);
      zone[i - 1].size += zone[i].size;
      for (j = i; j + 1 < zones_allocated; ++j) {
        zone[j].base = zone[j + 1].base;
        zone[j].size = zone[j + 1].size;
      }
      --zones_allocated;
    }
  }

  // printf("\nZones allocated (after merging): %d\n", zones_allocated);
  for (i = 0; i < zones_allocated; ++i) {
    // printf("  Zone[%d] : from %p to %p, size = %d bytes\n", i, zone[i].base, zone[i].base + zone[i].size - 1, zone[i].size);
  }

  fake_break_zone = 0;
  fake_base = zone[0].base;
  fake_break = fake_base;
}

x_ubyte * x_host_sbrk(x_int bytes) {
  char *old_fake_break = fake_break;

  if (!fake_base) {
    init_zones();
  }

  if (fake_break + bytes < fake_base) {
    x_int less = fake_base - fake_break - bytes;

    // printf("Request for %d bytes takes us under start of zone[%d] (%p)\n", bytes, fake_break_zone, fake_base);
    while (fake_break_zone) {
      --fake_break_zone;
      fake_base = zone[fake_break_zone].base;
      // printf("Need to lose %d bytes : moving to zone[%d] (%p)\n", less, fake_break_zone, fake_base);
      fake_break = fake_base + zone[fake_break_zone].size - less;
      less -= zone[fake_break_zone + 1].size;
      if (less <= 0) {
        // printf("OK, new fake break is %p\n", fake_break);

        return old_fake_break;

      }
    }
      
    if (less > 0) {
      loempa(1, "request for %d bytes heap when %d allocated, returning NULL\n", bytes, fake_break - fake_base);

      return NULL;

    }
  }

  if (fake_break + bytes >= fake_base + zone[fake_break_zone].size) {
    // printf("Request for %d bytes takes us over the end of zone[%d] (%p + 0x%08x = %p > %p)\n", bytes, fake_break_zone, fake_break, bytes, fake_break + bytes, fake_base + zone[fake_break_zone].size - 1);
    if (fake_break_zone + 1 >= zones_allocated) {
      // printf("no more zones, game over.\n");

      return NULL;

    }

    if ((x_size)bytes >= zone[fake_break_zone + 1].size - 32 /* ?? */) {
      // printf("expanding into next zone won't help, sorry.\n");

      return NULL;

    }

    ++fake_break_zone;
    fake_base = zone[fake_break_zone].base;
    // printf("Moving to zone[%d] (%p)\n", fake_break_zone, fake_base);
    fake_break = fake_base + bytes;

    return fake_base;

  }

  // loempa(1, "request for %d bytes heap when %d allocated (max. %d), returning %p\n", bytes, fake_break - fake_base + total_allocated, max_heap_bytes, fake_break + bytes);
  fake_break += bytes;
  // printf("fake_break now %p, was %p\n", fake_break, old_fake_break);

  return old_fake_break;
}

void x_host_break(x_ubyte * memory) {
}
#endif

#ifndef NOMMU

/*
** Function to set the 9 bits of the tag information. We need to use a lock for this
** since we touch internally used stuff, i.e. the this_size field.
*/

x_status x_mem_tag_set(void * mem, x_word tag) {

  x_chunk chunk = mem2chunk(mem);
  x_status status;
  
  status = x_monitor_eternal(memory_monitor);
  if (status == xs_success) {
    tag_set(chunk, tag);
    status = x_monitor_exit(memory_monitor);
  }
  
  return status;
  
}

/*
** Function to get the 9 bits of the tag information as a word. If the chunk is
** not in use, because it is being reallocated or freed, it returns tag 0.
*/

x_word x_mem_tag_get(void * mem) {

  x_word tag = 0x00000000;
  x_chunk chunk = mem2chunk(mem);

  if (chunk->this_size & CURRENT_IN_USE) {
    tag = (chunk->this_size & TAG_MASK) >> TAG_SHIFT;
  }
  
  return tag;
  
}

/*
** Return the recorded size in bytes of a memory chunk. This returns the number
** of bytes that are available in the chunk, based on the fact that chunk sizes
** are round up to units of 8 bytes. So a call of 'b = x_mem_alloc(22)' will yield a
** 'x_mem_size(b)' result of 28. An allocated chunk carries an overhead of 1 word so we substract
** 4 from the internally carried size, converted from units to bytes.
*/

x_size x_mem_size(void * mem) {
  return ((mem2chunk(mem)->this_size & USED_UNITS_MASK) << 3) - 4;
}

#endif

static const x_bytes page_size = OSWALD_PAGE_SIZE;

/*
** When we trim the top, how many >>bytes<< do we keep in the new top.
*/

static const x_bytes trim_keep = OSWALD_PAGE_SIZE * 10;

/*
** The decision to trim the top is taken by comparing its size in units
** with the trim_threshold size in >>units<<.
*/

static const x_units trim_threshold = (1024 * 512) >> 3;

/*
** Get memory from the system. Returns NULL when no more memory is
** available in the heap. When the argument is 0, it returns the current
** heap page pointer.
*/

static void * x_sbrk(x_bytes bytes) {

  void * result;
  static x_size allocated = 0;
  
  if (bytes < 0) {
    loempa(9, "Reducing pages in use; returning %d bytes.\n", - bytes);
  }

  if (bytes > 0 && allocated > max_heap_bytes) {
    loempa(9, "No more memory: %d bytes used, max_heap_bytes = %d.\n", allocated, max_heap_bytes);
    return NULL;
  }  
  
  result = x_host_sbrk(bytes);

  if (result) {
    allocated += bytes;
  }
  
  return result;

}

static x_ubyte * sbrk_base  = 0;
static x_int sbrked_mem = 0;

/*
** We always return chunks aligned on double word boundaries.
*/

static const x_size alignment = 8;

/*
** The minimum size that can be allocated is the size of a x_Chunk structure. 
** For unit sizes, we need to divide by 8.
*/

static const x_units minimum_size = (sizeof(x_Chunk) >> 3);

/*
** The maximum size of a chunk in units that a user can request for. 
** The range of the USED_UNITS_MASK. Note that there is always an overhead
** of a word so the real size the user can request for is 1 word less.
*/

static const x_units maximum_size = USED_UNITS_MASK;

/*
** Round up a certain 'value' to the given 'rounding' factor.
*/ 

inline static x_size round_up(x_size value, x_size rounding) {  
  return (value + (rounding - 1)) & ~(rounding - 1);
}

/*
** Round down a certain 'value' to the given 'rounding' factor.
** (not used for the moment)
*/ 

inline static x_size round_down(x_size value, x_size rounding) {  
  return (value) & (x_size)(0 - rounding);
}

/*
** Conversion from a chunk pointer to a user pointer.
*/

inline static void * chunk2mem(x_chunk chunk) {
  return ((unsigned char *)chunk + 2 * sizeof(x_word));
}


/*
** Treat the free space in the current chunk at offset as a new chunk.
*/

inline static x_chunk treat_as_chunk(x_chunk chunk, x_units offset) {
  return (x_chunk)((unsigned char *)chunk + (offset << 3));
}

/*
** Return the in use bit of a chunk at a certain offset. Note that this information is to be 
** searched for in the chunk following this chunk.
*/

inline static x_boolean is_inuse_at_offset(x_chunk chunk, x_units offset) {
  return treat_as_chunk(chunk, offset)->this_size & PREVIOUS_IN_USE;
}

/*
** Return true if a chunk is garbage.
*/

inline static x_boolean is_garbage(x_chunk chunk) {
  return (chunk->this_size & GARBAGE_TAG);
}

/*
** See if the preceeding chunk is in use.
*/

inline static x_boolean previous_is_inuse(x_chunk chunk) {
  return (x_word)(chunk->this_size & PREVIOUS_IN_USE);
}

/*
** Set the in use bit of a chunk at a certain offset.
*/

inline static void set_inuse_at_offset(x_chunk chunk, x_units offset) {
  treat_as_chunk(chunk, offset)->this_size |= PREVIOUS_IN_USE;
}

/*
** Clear the in use bit, the garbage tag and tag contents of a chunk, so that the
** size calculations can be done on free chunks and can use the tag bits space.
*/

inline static void clear_ciu_tag(x_chunk chunk) {
  chunk->this_size &= ~ (CURRENT_IN_USE | GARBAGE_TAG | TAG_MASK);
}

/*
** For a given size, return the index in the array of bins.
*/

inline static x_int bin_index(x_units size) {

  unsigned int divisor = size >> 6;

  if (divisor == 0) {
    return size;
  }
  else {
    if (divisor <= 4) {
      return 56 + (size >> 3);
    }
    else {
      if (divisor <= 20) {
        return 91 + (size >> 6);
      }
      else {
        if (divisor <= 84) {
          return 110 + (size >> 9);
        }
        else {
          if (divisor <= 340) {
            return 119 + (size >> 12);
          }
          else {
            if (divisor <= 1364) {
              return 124 + (size >> 15);
            } 
            else {
              return 126;
            }
          }
        }
      }
    }
  }
  
}

inline static void clear_last_remainder(void) {

  last_remainder->next = last_remainder;
  last_remainder->previous = last_remainder;

}

/*
** Return the index of a bin for a small size request (request < MAX_SMALLBIN_SIZE).
*/

inline static x_size smallbin_index(x_units size) {
  return size;
}

#define MAX_SMALLBIN         63    // We have this number of small bins
#define MAX_SMALLBIN_SIZE    64    // Any request smaller than this, in UNITS, is considered small
#define SMALLBIN_WIDTH        1    // Small bins are sized 8 bytes or 1 UNIT apart

/*
** A request is considered a 'small request' if the corresponding bin and the next bin are small.
*/

inline static x_int is_small_request(x_units size) {
  return (size < MAX_SMALLBIN_SIZE - SMALLBIN_WIDTH);
}

/*
** How a user size is transformed into a normalized size of UNITS. It is
** bumped up with the overhead of a single word and then rounded up to 8 bytes or a unit.
*/

inline static x_units request2units(x_size request) {

  if (request + sizeof(x_word) < (x_size)(minimum_size << 3)) {
    return minimum_size;
  }
  else {
    return (round_up(request + sizeof(x_word), alignment)) >> 3;
  }

}

#ifdef DEBUG /* ---- DEBUG is defined. We have extensive consistency checking ---------------------------- */

/*
** The debugging routines. When DEBUG is not defined, there are inlined
** alternatives that are optimized away. Note that any comment is provided in the debugging
** versions of the routines. The non debug versions don't contain any comment.
*/

#define check_monitor(c) _check_monitor(__LINE__, c)
static void _check_monitor(int l, x_size count) {
}

inline static void memory_lock(x_monitor m) {

  x_status status;
  
  status = x_monitor_eternal(m);
  if (status != xs_success) {
    o4p_abort(O4P_ABORT_BAD_STATUS, "x_monitor_eternal()", status);
  }

}

inline static void memory_unlock(x_monitor m) {

  x_status status;
  
  status = x_monitor_exit(m);
  if (status != xs_success) {
    o4p_abort(O4P_ABORT_BAD_STATUS, "x_monitor_eternal()", status);
  }

}

/*
** From a certain chunk, give the next one...
*/

#define next_chunk(c) _next_chunk(__LINE__, c)
inline static x_chunk _next_chunk(int l, x_chunk chunk) {

  if (chunk->this_size & CURRENT_IN_USE) {
    wabort(ABORT_WONKA, "%d: Chunk 0x%08x - 0x%08x is in use and next chunk is taken !!\n", l, chunk, chunk->this_size);
  }

  return (x_chunk)((unsigned char *)chunk + ((chunk->this_size & FREE_UNITS_MASK) << 3));

}


/*
** Return the size of the previous chunk in units, when the previous chunk is free. Otherwise,
** the field chunk->prev_size is part of the data space given to the caller of x_mem_alloc.
*/

#define previous_size(c) _previous_size(__LINE__, c)
inline static x_units _previous_size(int l, x_chunk chunk) {

  if (chunk->this_size & PREVIOUS_IN_USE) {
    wabort(ABORT_WONKA, "%d: previous size field taken for a chunk that is in use.!\n", l);
  }

  return chunk->prev_size;

}

/*
** From a certain chunk, give the previous one... (this only works if the previous is free !!)
*/

#define previous_chunk(c) _previous_chunk(__LINE__, c)
inline static x_chunk _previous_chunk(int l, x_chunk chunk) {

  if (chunk->this_size & PREVIOUS_IN_USE) {
    wabort(ABORT_WONKA, "%d: should not happen\n", l);
  }

  return treat_as_chunk(chunk, - previous_size(chunk));

}

/*
** Return the in use bit of a chunk. Note that this information is to be 
** searched for in the chunk following this chunk.
*/

#define is_inuse(c) _is_inuse(__LINE__, c)
inline static x_word _is_inuse(int l, x_chunk chunk) {

   if (! (chunk->this_size & CURRENT_IN_USE) && chunk != top) {
     if (treat_as_chunk(chunk, chunk->this_size & FREE_UNITS_MASK)->this_size & PREVIOUS_IN_USE) {
       wabort(ABORT_WONKA, "%d: Wrong 0x%08x. Memory is corrupt!\n", l, chunk);
     }
   }
   if (chunk->this_size & CURRENT_IN_USE && chunk != top) {
     if (! (treat_as_chunk(chunk, chunk->this_size & USED_UNITS_MASK)->this_size & PREVIOUS_IN_USE)) {
       wabort(ABORT_WONKA, "%d: Wrong 0x%08x. Memory is corrupt!\n", l, chunk);
     }
   }

  return chunk->this_size & CURRENT_IN_USE;

}

/*
** Get the size of a chunk
*/

#define chunk_size(c) _chunk_size(__LINE__, c)
inline static x_units _chunk_size(int l, x_chunk chunk) {

  if (chunk->this_size & CURRENT_IN_USE && ! (chunk->this_size & BIG_CHUNK)) {
    wabort(ABORT_WONKA, "%d: getting size of in use chunk !\n", l);
  }

  return chunk->this_size & FREE_UNITS_MASK;

}

/*
** Set the current in use bit of a chunk and set the tag. Note that the
** tag word should have its contents allready shifted into the right position.
*/

#define set_ciu_tag(c, t) _set_ciu_tag(__LINE__, c, t)
inline static void _set_ciu_tag(int l, x_chunk chunk, x_word tag) {

  chunk->this_size &= ~ (GARBAGE_TAG | TAG_MASK);
  chunk->this_size |= (CURRENT_IN_USE | tag);

  /*
  ** We check if the previous in use bit in the next chunk has been set properly since
  ** it pertains to this chunk that is now 'in use'.
  */
  
  if (! treat_as_chunk(chunk, chunk->this_size & USED_UNITS_MASK)->this_size & PREVIOUS_IN_USE) {
    wabort(ABORT_WONKA, "Problem at line %d.\n", l);
  }

}

/*
** Set the size of a chunk and set the previous in use bit. This is only
** done on chunks that aren't in use, so we don't bother about the current in
** use bit or the tag bits. In fact, they will be cleared...
*/

#define set_size_set_piu(c, s) _set_size_set_piu(__LINE__, c, s)
inline static void _set_size_set_piu(int l, x_chunk chunk, x_units size) {

  /*
  ** If the chunk is in use and we would set the size beyond the size of an
  ** in use chunk. We complain.
  */
  
  if ((chunk->this_size & CURRENT_IN_USE) && (size & ~USED_UNITS_MASK) != 0) {
    wabort(ABORT_WONKA, "%d: size = 0x%08x (%d Mb)\n", l, size, (size << 3) / (1024 * 1024));
  }

  if (size < 0) {
    wabort(ABORT_WONKA, "Size is negative!\n");
  }

  if (chunk->this_size & CURRENT_IN_USE) {
    wabort(ABORT_WONKA, "%d: chunk is in use\n", l);
  }
  
  chunk->this_size = (PREVIOUS_IN_USE | size);

}

/*
** Set the size of a chunk and leave the previous in use bit and the tag bits.
** Used only in realloc.
*/

#define set_size_leave_piu(c, s) _set_size_leave_piu(__LINE__, c, s)
inline static void _set_size_leave_piu(int l, x_chunk chunk, x_units size) {

  /*
  ** If the chunk is in use and we would set the size beyond the size of an
  ** in use chunk. We complain.
  */
  
  if ((chunk->this_size & CURRENT_IN_USE) && (size & ~USED_UNITS_MASK) != 0) {
    wabort(ABORT_WONKA, "%d: size = 0x%08x (%d Mb)\n", l, size, (size << 3) / (1024 * 1024));
  }

  if (size < 0) {
    wabort(ABORT_WONKA, "Size is negative!\n");
  }

  if (chunk->this_size & CURRENT_IN_USE) {
    wabort(ABORT_WONKA, "%d: chunk is in use\n", l);
  }
  
  chunk->this_size = (chunk->this_size & ~FREE_UNITS_MASK) | size;

}

/*
** Set the size at the footer of a chunk that is not in use (free).
*/

#define set_foot(c, s) _set_foot(__LINE__, c, s)
inline static void _set_foot(int l, x_chunk chunk, x_units size) {

  if (chunk->this_size & CURRENT_IN_USE) {
    wabort(ABORT_WONKA, "Should not happen\n");
  }

  if (size < 0) {
    wabort(ABORT_WONKA, "Size is negative|\n");
  }

  treat_as_chunk(chunk, size)->prev_size = size;

}

/*
** Link the last remainder.
*/

#define link_last_remainder(c, s) _link_last_remainder(__LINE__, c, s)
inline static void _link_last_remainder(int l, x_chunk remainder, x_units remainder_size) {

  clear_ciu_tag(remainder);
  set_size_set_piu(remainder, remainder_size);
  set_foot(remainder, remainder_size);

  last_remainder->next = remainder;
  last_remainder->previous = remainder;
  remainder->next = last_remainder;
  remainder->previous = last_remainder;

}

static x_boolean aligned_OK(void * address) {
  return (round_up((x_size) address, alignment) == (x_size)address);
}

#define check_top(t) _check_top(__LINE__, t)
static void _check_top(int l, x_chunk chunk) { 

  if (is_inuse(chunk)) {
    wabort(ABORT_WONKA, "%d: top isn't supposed to be in use!\n", l);
  }
  
  if (chunk_size(chunk) < minimum_size) {
    wabort(ABORT_WONKA, "%d: top size smaller then minimum %d < %d!\n", l, chunk_size(chunk), minimum_size);
  }

  if (! aligned_OK(chunk)) {
    wabort(ABORT_WONKA, "%d: top chunk doesn not align properly.\n", l);
  }

}

static void _check_chunk(int l, x_chunk chunk) { 

  x_units size;

  size = chunk->this_size & FREE_UNITS_MASK;
  if (chunk->this_size & CURRENT_IN_USE) {
    size &= USED_UNITS_MASK;
  }

  /*
  ** Check if addresses are legal.
  */
    
  x_assert((x_ubyte *)chunk >= sbrk_base);
  if (chunk != top) {
    if (! ((x_ubyte *)chunk + (size << 3) <= (x_ubyte *)top)) {
      loempa(9, "%d: wrong 1.\n", l);
    }
  }
  else {
    if (! ((x_ubyte *)next_chunk(top) <= sbrk_base + sbrked_mem)) {
      loempa(9, "%d: wrong 2.\n", l);
    }
  }

}

static void _check_free_chunk(int l, x_chunk chunk) { 

  x_units size = chunk_size(chunk);
  x_chunk next = treat_as_chunk(chunk, size);

  _check_chunk(l, chunk);

  /*
  ** Check wether it really claims to be free.
  */
  
  x_assert(! is_inuse(chunk));

  if (size >= minimum_size) {

    /*
    ** Check wether size aligns OK.
    */
  
    x_assert(((size << 3) & (alignment - 1)) == 0);

    /*
    ** Check footer field.
    */
  
    x_assert(next->prev_size == size);
  
    /*
    ** Check if consolidated correctly.
    */

    x_assert(previous_is_inuse(chunk));
    x_assert(next == top || is_inuse(next));
  
    /*
    ** Minimally check sane links.
    */
  
    x_assert(chunk->next->previous == chunk);
    x_assert(chunk->previous->next == chunk);
  }
  
}

#define check_inuse_chunk(chunk) _check_inuse_chunk(__LINE__, chunk)
static void _check_inuse_chunk(int l, x_chunk chunk) { 

  x_units size;
  x_chunk next;
  x_chunk previous;

  _check_chunk(l, chunk);

  if (! chunk->this_size & CURRENT_IN_USE) {
    wabort(ABORT_WONKA, "Chunk should be in use!\n");
  }
  
  size = chunk->this_size & USED_UNITS_MASK;

  next = treat_as_chunk(chunk, size);
    
  /*
  ** Check wether it is surrounded by OK chunks. Free surrounding chunks
  ** can be checked more thoroughly.
  */

  if (! previous_is_inuse(chunk)) {
    previous = previous_chunk(chunk);
    if (_next_chunk(l, previous) != chunk) {
      loempa(9, "%d: Pointers are wrong, top = 0x%08x, lr = 0x%08x, initial = 0x%08x\n", l, top, last_remainder, initial);
      loempa(9, "previous = 0x%08x - 0x%08x, chunk = 0x%08x - 0x%08x - prev 0x%08x\n", previous, previous->this_size, chunk, chunk->this_size, chunk->prev_size);
      wabort(ABORT_WONKA, "previous->next = 0x%08x\n", previous->next);
    }
    _check_free_chunk(l, previous);
  }

  if (next == top) {
    x_assert(previous_is_inuse(next));
    x_assert(chunk_size(next) >= minimum_size);
  }
  else if (! _is_inuse(l, next)) {
    _check_free_chunk(l, next);
  }

}

#define check_alloced_chunk(chunk, requested) _check_alloced_chunk(__LINE__, chunk, requested)
static void _check_alloced_chunk(int l, x_chunk chunk, x_units requested) { 

  x_units size = chunk->this_size & USED_UNITS_MASK;
  x_units room = size - requested;

  if (chunk->this_size & TAG_MASK) {
    wabort(ABORT_WONKA, "%d: tag bits are not 0.\n", l);
  }

  if (chunk->this_size & GARBAGE_TAG) {
    wabort(ABORT_WONKA, "%d: garbage bit is not 0.\n", l);
  }
  
  _check_inuse_chunk(l, chunk);

  /*
  ** Check if sizes are legal.
  */
  
  x_assert(size >= minimum_size);
  x_assert(((size << 3) & (alignment - 1)) == 0);
  x_assert(room >= 0);
  x_assert(room < minimum_size);

  /*
  ** Check wether it was allocated at the front of an available chunk.
  */
  
  x_assert(previous_is_inuse(chunk));
  
}

#else /* ---- DEBUG is not defined ---------------------------------------------------------------- */

inline static x_chunk next_chunk(x_chunk chunk) {
  return (x_chunk)((unsigned char *)chunk + ((chunk->this_size & FREE_UNITS_MASK) << 3));
}

inline static x_units previous_size(x_chunk chunk) {
  return chunk->prev_size;
}

inline static x_chunk previous_chunk(x_chunk chunk) {
  return treat_as_chunk(chunk, - previous_size(chunk));
}

inline static x_word is_inuse(x_chunk chunk) {
  return chunk->this_size & CURRENT_IN_USE;
}

inline static x_units chunk_size(x_chunk chunk) {
  return chunk->this_size & FREE_UNITS_MASK;
}

inline static void set_ciu_tag(x_chunk chunk, x_word tag) {
  chunk->this_size &= ~ (GARBAGE_TAG | TAG_MASK);
  chunk->this_size |= (CURRENT_IN_USE | tag);
}

inline static void set_size_set_piu(x_chunk chunk, x_units size) {
  chunk->this_size = (PREVIOUS_IN_USE | size);
}

inline static void set_size_leave_piu(x_chunk chunk, x_units size) {
  chunk->this_size = (chunk->this_size & ~FREE_UNITS_MASK) | size;
}

inline static void set_foot(x_chunk chunk, x_units size) {
  treat_as_chunk(chunk, size)->prev_size = size;
}

inline static void link_last_remainder(x_chunk remainder, x_units remainder_size) {

  clear_ciu_tag(remainder);
  set_size_set_piu(remainder, remainder_size);
  set_foot(remainder, remainder_size);

  last_remainder->next = remainder;
  last_remainder->previous = remainder;
  remainder->next = last_remainder;
  remainder->previous = last_remainder;

}

#define memory_lock(m) x_monitor_eternal(m)
#define memory_unlock(m) x_monitor_exit(m)
#define check_monitor(c)
#define check_top(t)
#define check_free_chunk(chunk)
#define check_inuse_chunk(chunk)
#define check_alloced_chunk(chunk, size)

#endif /* ---- DEBUG is not defined ---------------------------------------------------------------- */

#ifndef NOMMU

static x_Monitor Memory_monitor;

void x_mem_init() {

  x_size i;
  x_chunk chunk;
  x_ubyte *current;
  x_ubyte *aligned;

  memory_monitor = &Memory_monitor;
  // TODO, see if we could get the bins into the remainder of page aligning the memory
  // with not to much code...
  last_remainder = bin_at(1);
  clear_last_remainder();
  
  for (i = 0; i < NUMBER_OF_BINS; i++) {
    chunk = bin_at(i);
    chunk->previous = chunk;
    chunk->next = chunk;
  }

  /*
  ** Page align sbrk_base and store for internal use.
  */

  current = x_sbrk(0);
  aligned = (x_ubyte *)round_up((x_size)current, (x_size)page_size);
  loempa(9, "Aligned memory would begin at 0x%08x\n", aligned);
  current = x_sbrk(aligned - current);
  loempa(9, "Currently aligned x_sbrk at 0x%08x\n", x_sbrk(0));
  current = x_sbrk(0);
  free_units = max_heap_bytes >> 3;
  loempa(9, "Correction = %d bytes, current = 0x%08x\n", aligned - current, x_sbrk(0));
  sbrk_base = x_sbrk(0);

  /*
  ** Create the initial and top chunk.
  */

  top = x_sbrk(page_size);
  sbrked_mem = page_size;
  clear_ciu_tag(top);
  set_size_set_piu(top, page_size >> 3);
  initial = top;
  loempa(9, "Top chunk at 0x%08x, size %d bytes, total of %d bytes free.\n", top, chunk_size(top) << 3, free_units << 3);
  check_top(top);

  x_monitor_create(memory_monitor);
}

#endif

/*
** Put 'chunk' in the appropriate bin, in size order, and put it ahead of other
** free chunks of the same size.
*/

inline static void frontlink(x_chunk chunk, x_units size) {

  x_size idx;
  x_chunk bin;
  x_chunk current;

  if (size < MAX_SMALLBIN_SIZE) {
    idx = smallbin_index(size);
    bin = bin_at(idx);
    x_list_insert(bin, chunk);
  }
  else {
    idx = bin_index(size);
    bin = bin_at(idx);
    current = first(bin);
    if (! bin_empty(bin)) {
      while (current != bin && size < chunk_size(current)) {
        current = current->next;
      }
    }
    x_list_insert(current, chunk);
  }

}

/*
** When we had an intervening or foreign break, we need to get the 'old'
** top chunk linked into the list of free chunks. We also have to bridge
** the gap between the old top and the new top and set it to foreign
** territory. Since this gap can be large, we set the BIG_CHUNK flag on it.
*/

static void link_old_top(x_chunk chunk, x_units size) {

  x_units prev_size;
  x_boolean islr;
  x_chunk foreign;

  free_units += size;

  if (size > minimum_size) {
    size -= 1;
    foreign = treat_as_chunk(chunk, size);
    foreign->this_size = CURRENT_IN_USE | BIG_CHUNK | (((x_ubyte *)top - (x_ubyte *)foreign) >> 3);

    loempa(9, "Freeing old top 0x%08x, size %d bytes.\n", chunk, size << 3);
    loempa(9, "Foreign land starts at %p, size %d bytes.\n", foreign, (foreign->this_size & USED_UNITS_MASK) << 3);
    loempa(9, "Size set in foreign chunk for this chunk is %d bytes.\n", foreign->prev_size << 3);
  }

  islr = FALSE;
  
  if (! previous_is_inuse(chunk)) {
    loempa(9, "CONSOLIDATING\n");
    prev_size = previous_size(chunk);
    chunk = treat_as_chunk(chunk, - prev_size);
    size += prev_size;
    if (chunk->next == last_remainder) {
      loempa(9, "LAST REMAINDER\n");
      islr = TRUE;
    }
    else {
      x_list_remove(chunk);
    }
  }
  
  set_size_set_piu(chunk, size);
  set_foot(chunk, size);


  if (! islr) {
    loempa(9, "Frontlinking...\n");
    frontlink(chunk, size);
  }

}

inline static void extend_top(x_units size) {

  x_ubyte * brked;                        // the return value of sbrk
  x_units top_size;                       // the new size of the top chunk
  x_chunk old_top = top;                  // the previous top chunk
  x_units old_top_size = chunk_size(top);
  x_ubyte * old_end = (x_ubyte *)(treat_as_chunk(old_top, old_top_size));
  x_bytes sbrk_size;
  x_ubyte * new_sbrk;
  x_bytes misalignment;
  x_bytes correction;

  sbrk_size = (size + minimum_size) << 3;
  
  /*
  ** Round to page boundary and get memory from the system.
  */
  
  sbrk_size = round_up((x_size) sbrk_size, (x_size) page_size);
  // loempa(2, "Requested %d units, issuing sbrk for %d bytes\n", size, sbrk_size);
  brked = x_sbrk(sbrk_size);
  
  /*
  ** See if we had an intervening (foreign) x_sbrk call that killed the consistency of
  ** our own x_sbrk pages...
  */
  
  if (brked == NULL || (brked < old_end && old_top != initial)) {
    loempa(9, "sbrk killed %p ?\n", brked);
    return;
  }
  
  sbrked_mem += sbrk_size;

  /*
  ** See if we can just grow our current top further...
  */
    
  if (brked == old_end) {
    top_size = (sbrk_size >> 3) + old_top_size;
    set_size_set_piu(top, top_size);
  }
  else {
    loempa(9, "Foreign sbrk happened; correcting...\n");
    sbrked_mem += brked - old_end;
    
    /*
    ** We must guarantee alignment of a chunk that will be carved from this new
    ** space.
    */
    
    correction = 0;
    misalignment = (x_size) chunk2mem((x_chunk) brked) & (alignment - 1);
    if (misalignment > 0) {
      correction = alignment - misalignment;
      brked += correction;
    }

    loempa(9, "Foreign sbrk happened; front correction = %d bytes.\n", correction);

    correction += page_size - (((unsigned long) brked + sbrk_size) & (page_size - 1));

    loempa(9, "Foreign sbrk happened; total correction = %d bytes.\n", correction);

    new_sbrk = x_sbrk(correction);
    if (new_sbrk == NULL) {
      return;
    }
    
    sbrked_mem += correction;
    top = (x_chunk) brked;
    top_size = (new_sbrk - brked + correction) >> 3;
    set_size_set_piu(top, top_size);
    check_top(top);
    
    if (old_top != initial) {
      if (old_top_size < minimum_size) {
        loempa(9, "Serious problem! allocation will return NULL...\n");
        set_size_set_piu(top, old_top_size); // Will force a NULL return from memory allocation...
        return;
      }
      link_old_top(old_top, old_top_size);
    }

  }
  
}

/*
** The internal allocation function. This function does no locking and works in unit sizes.
*/

static x_chunk chunk_alloc(x_units size) {

  x_size idx;
  x_chunk bin;
  x_chunk victim;
  x_units victim_size;
  x_chunk remainder;
  x_units remainder_size;      // This can become negative !

  if (is_small_request(size)) {
    idx = smallbin_index(size);
    bin = bin_at(idx);
    victim = last(bin);

    /*
    ** If the bin is empty, we can check the next one too, since the remainder would be smaller
    ** than minimum_size; remember that the small bins are spaced 8 bytes apart. So the remainder is
    ** only 8 bytes.
    */
        
    if (bin_empty(bin)) {
      bin = next_bin(bin);
      victim = last(bin);
    }

    if (! bin_empty(bin)) {
      victim_size = chunk_size(victim);
      x_list_remove(victim);
      set_inuse_at_offset(victim, victim_size);
      set_ciu_tag(victim, 0);
      check_alloced_chunk(victim, size);

      /*
      ** Since the chunk is 'oversized', we need to substract its real size in stead
      ** of the size requested by the user.
      */
      
      free_units -= victim_size;
      return victim;
    }

    idx += 2;
    
  }
  else {
    idx = bin_index(size);
    bin = bin_at(idx);
    
    for (victim = last(bin); victim != bin; victim = victim->previous) {
      victim_size = chunk_size(victim);
      remainder_size = victim_size - size;

      if (remainder_size >= minimum_size) {
        idx -= 1;
        break;
      }
      else if (remainder_size >= 0) {
        x_list_remove(victim);
        set_inuse_at_offset(victim, victim_size);
        set_ciu_tag(victim, 0);
        check_alloced_chunk(victim, size);
        
        /*
        ** Again, an 'oversized' chunk...
        */
        
        free_units -= victim_size;
        return victim;
      }
    }
    
    idx += 1;
    
  }

  /*
  ** Try to use the last split off remainder.
  */
  
  victim = last_remainder->next;
  if (victim != last_remainder) {
    victim_size = chunk_size(victim);
    remainder_size = victim_size - size;
    
    if (remainder_size >= minimum_size) {
      remainder = treat_as_chunk(victim, size);
      set_size_set_piu(victim, size);
      set_ciu_tag(victim, 0);
      link_last_remainder(remainder, remainder_size);
      check_alloced_chunk(victim, size);
      free_units -= size;
      return victim;
    }

    clear_last_remainder();
    
    if (remainder_size >= 0) {
      set_inuse_at_offset(victim, victim_size);
      set_ciu_tag(victim, 0);
      check_alloced_chunk(victim, size);
      
      /*
      ** Again an 'oversized' chunk.
      */
      
      free_units -= victim_size;
      return victim;
    }

    // place in bin    

    frontlink(victim, victim_size);
    
  }

  /*
  ** Try searching in other bins, we search back from the last chunk in a bin to the front
  ** since the largest chunks are accumulated at the bottom of a bin.
  */

  while (idx < NUMBER_OF_BINS - 1) {
    bin = bin_at(idx);
    for (victim = last(bin); victim != bin; victim = victim->previous) {
      victim_size = chunk_size(victim);
      remainder_size = victim_size - size;
      if (remainder_size >= minimum_size) {
        remainder = treat_as_chunk(victim, size);
        set_size_set_piu(victim, size);
        set_ciu_tag(victim, 0);
        x_list_remove(victim);
        link_last_remainder(remainder, remainder_size);
        check_alloced_chunk(victim, size);
        free_units -= size;
        return victim;
      }
      else if (remainder_size >= 0) {
        set_inuse_at_offset(victim, victim_size);
        set_ciu_tag(victim, 0);
        x_list_remove(victim);
        check_alloced_chunk(victim, victim_size);
        free_units -= victim_size;
        return victim;
      }
    }
    idx += 1;
  }

  /*
  ** Try to extend top...
  */

  if ((remainder_size = chunk_size(top) - size) < minimum_size) {
    extend_top(size);
    if ((remainder_size = chunk_size(top) - size) < minimum_size) {
      loempa(9, "Returning NULL; remainder_size = %d\n", remainder_size);
      return NULL;
    }
  }

  victim = top;
  set_size_set_piu(victim, size);
  top = treat_as_chunk(victim, size);
  clear_ciu_tag(top);
  set_size_set_piu(top, remainder_size);
  check_top(top);
  set_ciu_tag(victim, 0);
  check_alloced_chunk(victim, size);
  free_units -= size;
  return victim;
  
}

/*
** The plain vanilla memory allocation function, works just the same as malloc but
** works on a multithreaded system...
*/

#ifdef DEBUG
void * _x_mem_alloc(x_size request, const char * file, int line) {
#else
void * _x_mem_alloc(x_size request) {
#endif

  x_units size = request2units(request);
  x_chunk chunk;
  void * mem = NULL;

  if (size > maximum_size) {
    return NULL;
  }

  memory_lock(memory_monitor);
  chunk = chunk_alloc(size);
  if (chunk) {
    mem = chunk2mem(chunk);
  }
  memory_unlock(memory_monitor);
  
  return mem;
  
}

/*
** ... and the memory allocation function for cleared memory ...
*/

#ifdef DEBUG
void * _x_mem_calloc(x_size request, const char * file, int line) {
#else
void * _x_mem_calloc(x_size request) {
#endif

  x_word * block = x_mem_alloc(request);
  x_word * cursor;
  int duffs;

  if (block) {

    cursor = block;
    request = mem2chunk(block)->this_size & USED_UNITS_MASK; // size in units
    request *= 2;                                            // Convert to words
    request -= 1;                                            // Reduce with chunk overhead of 1 word
    duffs = (request + 31) / 32;                             // How many switch rounds we have to do, note 31 == 0x1f

    switch (request & 0x1f) {
      default:
      case  0: do { *cursor++ = 0;
      case 31:      *cursor++ = 0;
      case 30:      *cursor++ = 0;
      case 29:      *cursor++ = 0;
      case 28:      *cursor++ = 0;
      case 27:      *cursor++ = 0;
      case 26:      *cursor++ = 0;
      case 25:      *cursor++ = 0;
      case 24:      *cursor++ = 0;
      case 23:      *cursor++ = 0;
      case 22:      *cursor++ = 0;
      case 21:      *cursor++ = 0;
      case 20:      *cursor++ = 0;
      case 19:      *cursor++ = 0;
      case 18:      *cursor++ = 0;
      case 17:      *cursor++ = 0;
      case 16:      *cursor++ = 0;
      case 15:      *cursor++ = 0;
      case 14:      *cursor++ = 0;
      case 13:      *cursor++ = 0;
      case 12:      *cursor++ = 0;
      case 11:      *cursor++ = 0;
      case 10:      *cursor++ = 0;
      case  9:      *cursor++ = 0;
      case  8:      *cursor++ = 0;
      case  7:      *cursor++ = 0;
      case  6:      *cursor++ = 0;
      case  5:      *cursor++ = 0;
      case  4:      *cursor++ = 0;
      case  3:      *cursor++ = 0;
      case  2:      *cursor++ = 0;
      case  1:      *cursor++ = 0;
              } while (--duffs > 0);
    }

  }

  return block;

}

/*
** Make the chunk the new top with the given size. When the size is too large, we return the
** memory to the system.
*/

inline static void update_top(x_chunk new_top, x_units new_units) {

  x_bytes to_return;
  x_ubyte * current_sbrk;
  x_ubyte * new_sbrk;
  x_bytes new_bytes;

  /*
  ** We first set the new top and then see if we should trim it...
  */
  
  top = new_top;
  set_size_set_piu(top, new_units);

  new_bytes = new_units << 3;

  if (new_units > trim_threshold) {
  
    /*
    ** See if somebody else called x_sbrk...
    */
    
    current_sbrk = x_sbrk(0);
    if (current_sbrk != (x_ubyte *)top + new_bytes) {
      loempa(9, "Sbrk called by somebody else; current = 0x%08x, required = 0x%08x\n", current_sbrk, (char *)top + new_bytes);
      return;
    }
    else {
      to_return = round_up(new_bytes - trim_keep, (x_size)page_size);
      loempa(9, "Trimming top with size %d bytes downto %d bytes.\n", new_bytes, new_bytes - to_return);  
      new_sbrk = x_sbrk(- to_return);
      if (new_sbrk == NULL) {
        current_sbrk = x_sbrk(0);
        new_bytes = current_sbrk - (x_ubyte *) top;
        if (new_bytes >= minimum_size << 3) {
          sbrked_mem = current_sbrk - sbrk_base;
          set_size_set_piu(top, new_bytes >> 3);
        }
        check_top(top);
        return;
      }
      else {
        new_units -= (to_return >> 3);
        sbrked_mem -= to_return;
        set_size_set_piu(top, new_units);
      }
    }
  }
  
  check_top(top);

}

/*
** ... and the internal chunk freeing function. Does no locking...
*/

static void chunk_free(x_chunk chunk) {

  x_chunk next;
  x_units size;
  x_units next_size;
  x_units prev_size;
  x_boolean islr;

  size = chunk_size(chunk);

  free_units += size;

  next = treat_as_chunk(chunk, size);
  
  if (next == top) {
    size += chunk_size(top);
    
    if (! previous_is_inuse(chunk)) {
      prev_size = previous_size(chunk);
      chunk = treat_as_chunk(chunk, - prev_size);
      size += prev_size;
      x_list_remove(chunk);
    }

    update_top(chunk, size);
    return;
  }

  /*
  ** We determine the size of the next chunk. We are carefull since when it is in use,
  ** we need to chop of the tag bits, if not, we keep the full size of the chunk.
  */

  next_size = next->this_size & FREE_UNITS_MASK; // == chunk_size(next) but would trigger an error when runtime checks are enabled
  if (is_inuse(next)) {
    next_size &= USED_UNITS_MASK;
  }

  /*
  ** Clear the previous in use bit in the next chunk since we'll merge with the previous chunk.
  ** It means that we mark the chunk we want to set free as not being in use anymore.
  */
  
  next->this_size &= ~PREVIOUS_IN_USE;
  
  islr = FALSE;
  
  if (! previous_is_inuse(chunk)) {
    prev_size = previous_size(chunk);
    chunk = treat_as_chunk(chunk, - prev_size);
    size += prev_size;
    if (chunk->next == last_remainder) {
      islr = TRUE;
    }
    else {
      x_list_remove(chunk);
    }
  }
  
  if (! (is_inuse_at_offset(next, next_size))) {
    size += next_size;
    if (! islr && next->next == last_remainder) {
      islr = TRUE;
      link_last_remainder(chunk, size);
    }
    else {
      x_list_remove(next);
    }
  }

  set_size_set_piu(chunk, size);
  set_foot(chunk, size);

  if (! islr) {
    frontlink(chunk, size);
  }

}

#ifndef NOMMU

/*
** ... the user visible memory release function ...
*/

void x_mem_free(void * mem) {

  x_chunk chunk;
  
  if (mem == NULL) {
    return;
  }
  
  chunk = mem2chunk(mem);

  memory_lock(memory_monitor);
  check_inuse_chunk(chunk);
  clear_ciu_tag(chunk);
  chunk_free(chunk);
  memory_unlock(memory_monitor);

}

#endif

/*
** A copy that is used internally to copy over contents from the source chunk to the
** destination chunk; used in realloc.
*/

inline static void realloc_copy(x_chunk destination, x_chunk source) {

  x_word * from = chunk2mem(source);
  x_word * to = chunk2mem(destination);
  x_size words = (chunk_size(source) * 2) - 1;
  int duffs;

  if (words) {

    duffs = (words + 31) / 32;

    switch (words & 0x1f) {
      default:
      case  0: do { *to++ = *from++;
      case 31:      *to++ = *from++;
      case 30:      *to++ = *from++;
      case 29:      *to++ = *from++;
      case 28:      *to++ = *from++;
      case 27:      *to++ = *from++;
      case 26:      *to++ = *from++;
      case 25:      *to++ = *from++;
      case 24:      *to++ = *from++;
      case 23:      *to++ = *from++;
      case 22:      *to++ = *from++;
      case 21:      *to++ = *from++;
      case 20:      *to++ = *from++;
      case 19:      *to++ = *from++;
      case 18:      *to++ = *from++;
      case 17:      *to++ = *from++;
      case 16:      *to++ = *from++;
      case 15:      *to++ = *from++;
      case 14:      *to++ = *from++;
      case 13:      *to++ = *from++;
      case 12:      *to++ = *from++;
      case 11:      *to++ = *from++;
      case 10:      *to++ = *from++;
      case  9:      *to++ = *from++;
      case  8:      *to++ = *from++;
      case  7:      *to++ = *from++;
      case  6:      *to++ = *from++;
      case  5:      *to++ = *from++;
      case  4:      *to++ = *from++;
      case  3:      *to++ = *from++;
      case  2:      *to++ = *from++;
      case  1:      *to++ = *from++;
              } while (--duffs > 0);
    }
    
  }

}

#ifndef NOMMU

/*
** ... the memory reallocator ...
*/

void * x_mem_realloc(void * oldmem, x_size requested) {

  x_units req_size;
  x_units old_size;
  x_chunk old;
  x_units new_size;
  x_chunk new;
  x_units prev_size;
  x_chunk prev;
  x_units next_size;
  x_chunk next;
  x_units rem_size;
  x_chunk rem = NULL;
  x_word tag;
  
  /*
  ** If old was NULL, behave as x_mem_alloc.
  */
  
  if (oldmem == NULL) {
    return x_mem_alloc(requested);
  }
  
  req_size = request2units(requested);

  if (req_size > maximum_size) {
    return NULL;
  }
  
  new = old = mem2chunk(oldmem);
  
  memory_lock(memory_monitor);

  check_inuse_chunk(old);

  tag = old->this_size & TAG_MASK;

  clear_ciu_tag(old);
  new_size = old_size = chunk_size(old);

  /*
  ** Check if we are expanding or shrinking and act accordingly. The next clause handles the
  ** expanding case...
  */
  
  if (old_size < req_size) {

    /*
    ** Try expanding forward.
    */
    
    next = treat_as_chunk(old, old_size);
    
    if (next == top || ! is_inuse(next)) {
      next_size = chunk_size(next);

      /*
      ** Try forward into the top if possible (there must be a remainder left after extending 
      ** top, so available top size must be requested size + minimum_size) or otherwise try 
      ** to grow forward into the next chunk that isn't the top chunk.
      */
        
      if (next == top) {
        if (next_size + new_size >= req_size + minimum_size) {
          new_size += next_size;
          top = treat_as_chunk(old, req_size);
          clear_ciu_tag(top);
          set_size_set_piu(top, new_size - req_size);
          set_size_leave_piu(old, req_size);
          set_ciu_tag(old, tag);
          free_units += old_size;
          free_units -= req_size;
          x_monitor_exit(memory_monitor);
          return chunk2mem(old);
        }
      }
      else if (next_size + new_size >= req_size) {
        x_list_remove(next);
        new_size += next_size;
        free_units -= new_size;
        free_units += old_size;
        goto split;
      }
    }
    else {
      next = NULL;
      next_size = 0;
    }

    /*
    ** Try backwards to see if we can join with preceeding free space and possibly next chunk free space.
    */
    
    if (! previous_is_inuse(old)) {
      prev = previous_chunk(old);
      prev_size = chunk_size(prev);
      
      /*
      ** Try forward and backward first, to save a later consolidation. Again, we
      ** check that the top chunk, if involved, has remainder space left after the
      ** operation.
      */
      
      if (next != NULL) {
        if (next == top) {
          if (next_size + prev_size + new_size >= req_size + minimum_size) {
            x_list_remove(prev);
            new = prev;
            realloc_copy(new, old);
            new_size += prev_size + next_size;
            top = treat_as_chunk(new, req_size);
            clear_ciu_tag(top);
            set_size_set_piu(top, new_size - req_size);
            set_size_leave_piu(new, req_size);
            set_ciu_tag(new, tag);
            free_units += old_size;
            free_units -= req_size;
            x_monitor_exit(memory_monitor);
            return chunk2mem(new);
          }
        }
        else if (next_size + prev_size + new_size >= req_size) {
          x_list_remove(next);
          x_list_remove(prev);
          new = prev;
          new_size += next_size + prev_size;
          realloc_copy(new, old);
          free_units += old_size;
          free_units -= new_size;
          goto split;
        }
      }
        
      /*
      ** Backward only.
      */
        
      if (prev_size + new_size >= req_size) {
        x_list_remove(prev);
        new = prev;
        realloc_copy(new, old);
        new_size += prev_size;
        free_units += old_size;
        free_units -= new_size;
        goto split;
      }
    }

    /*
    ** No avail, we ask x_mem_alloc to help out. If that even returns NULL, we make
    ** sure we bring the old chunk back in pristine order, as we got it.
    */
      
    new = chunk_alloc(req_size);
    check_monitor(1);
    if (new == NULL) {
      set_ciu_tag(old, tag);
      memory_unlock(memory_monitor);
      return NULL;
    }

    /*
    ** Check if the new memory comes from a chunk that follows the old memory.
    ** This could only happen when the preceeding x_mem_alloc call extended the top.
    ** If this is the case, we avoid a copy.
    */

    clear_ciu_tag(new);
    if (new == next_chunk(old)) {
      new_size += chunk_size(new);
      new = old;
      goto split;
    }
      
    /*
    ** ... else copy and free the old stuff and return.
    */
      
    realloc_copy(new, old);
    set_ciu_tag(new, tag);
    chunk_free(old);
    check_monitor(1);
    memory_unlock(memory_monitor);
    return chunk2mem(new);
      
  }

  /*
  ** Split of the extra room in the old chunk (shrinked) or in the expanded chunk (growth).
  */
  
  split:

  if (new_size - req_size >= minimum_size) {
    rem = treat_as_chunk(new, req_size);
    rem_size = new_size - req_size;
    clear_ciu_tag(rem);
    set_size_leave_piu(new, req_size);
    set_size_set_piu(rem, rem_size);
    set_inuse_at_offset(rem, rem_size);
    set_ciu_tag(new, tag);
    chunk_free(rem);
    check_monitor(1);
  }
  else {
    set_size_leave_piu(new, new_size);
    set_inuse_at_offset(new, new_size);
    set_ciu_tag(new, tag);
    check_inuse_chunk(new);
  }

  memory_unlock(memory_monitor);

  return chunk2mem(new);
    
}

/*
** Walk over all the memory blocks.
*/

x_status x_mem_walk(x_sleep timeout, x_boolean (*callback)(void * mem, void * arg), void * arg) {

  x_chunk cursor;
  x_chunk next;
  x_status status;
  x_units size;
  x_boolean proceed;
  
  status = x_monitor_enter(memory_monitor, timeout);
  if (status != xs_success) {
    return status;
  }

  /*
  ** We loop over the chunks. We precalculate the next chunk before we call
  ** the callback for safety reasons.
  */
  
  for (cursor = initial; cursor < top; cursor = next) {
    size = cursor->this_size & FREE_UNITS_MASK;
    if (cursor->this_size & BIG_CHUNK) {
      next = treat_as_chunk(cursor, size);
    }
    else if (is_inuse(cursor)) {
      size &= USED_UNITS_MASK;
      next = treat_as_chunk(cursor, size);
      if (! is_garbage(cursor)) {
        proceed = callback(chunk2mem(cursor), arg);
        if (! proceed) {
          break;
        }
      }
    }
    else {
      next = treat_as_chunk(cursor, size);
    }

    if (runtime_checks && size == 0) {
      wabort(ABORT_WONKA, "Size is 0; cursor = 0x%08x\n", cursor);
    }
  }

  status = x_monitor_exit(memory_monitor);

  return status;
  
}

/*
** A special walker function that also passes the garbage blocks, so that the memory check
** routines can account for garbage blocks too.
*/

x_status x_mem_walkall(x_sleep timeout, x_boolean (*callback)(void * mem, void * arg), void * arg) {

  x_chunk cursor;
  x_chunk next;
  x_status status;
  x_units size;
  x_boolean proceed;
  
  status = x_monitor_enter(memory_monitor, timeout);
  if (status != xs_success) {
    return status;
  }

  /*
  ** We loop over the chunks. We precalculate the next chunk before we call
  ** the callback for safety reasons.
  */
  
  for (cursor = initial; cursor < top; cursor = next) {
    size = cursor->this_size & FREE_UNITS_MASK;
    if (cursor->this_size & BIG_CHUNK) {
      next = treat_as_chunk(cursor, size);
    }
    else if (is_inuse(cursor)) {
      size &= USED_UNITS_MASK;
      next = treat_as_chunk(cursor, size);
      proceed = callback(chunk2mem(cursor), arg);
      if (! proceed) {
        break;
      }
    }
    else {
      next = treat_as_chunk(cursor, size);
    }
  }

  status = x_monitor_exit(memory_monitor);

  return status;
  
}

/*
** Scan all the memory blocks with a certain tag.
*/

x_status x_mem_scan(x_sleep timeout, x_word tag, x_boolean (*callback)(void * mem, void * arg), void * arg) {

  x_chunk cursor;
  x_chunk next;
  x_status status;
  x_units size;
  x_boolean proceed;
  x_word thistag;
  
  status = x_monitor_enter(memory_monitor, timeout);
  if (status != xs_success) {
    return status;
  }

  /*
  ** We loop over the chunks. We precalculate the next chunk before we call
  ** the callback for safety reasons. We also precalculate the shifted tag.
  */

  tag = (tag << TAG_SHIFT) & TAG_MASK;
  
  for (cursor = initial; cursor < top; cursor = next) {
    size = cursor->this_size & FREE_UNITS_MASK;
    if (cursor->this_size & BIG_CHUNK) {
      next = treat_as_chunk(cursor, size);
    }
    else if (is_inuse(cursor)) {
      size &= USED_UNITS_MASK;
      next = treat_as_chunk(cursor, size);
      if (! is_garbage(cursor)) {
        thistag = cursor->this_size & TAG_MASK;
        if (thistag & tag) {
          proceed = callback(chunk2mem(cursor), arg);
          if (! proceed) {
            break;
          }
        }
      }
    }
    else {
      next = treat_as_chunk(cursor, size);
    }

    if (runtime_checks && size == 0) {
      wabort(ABORT_WONKA, "Size is 0!\n");
    }
  }

  status = x_monitor_exit(memory_monitor);

  return status;
  
}

/*
** Return true when a  block of memory is a block allocated through the memory
** allocation functions, false otherwise.
*/

w_boolean x_mem_is_block(void * mem) {

  x_chunk cursor;
  x_chunk check;
  x_boolean is_block = FALSE;
  x_units size;
  
  static x_size calls = 0;
  static x_size walked = 0;

  if (mem == NULL) {
    return FALSE;
  }
  
  check = mem2chunk(mem);

  /*
  ** Check if the alignment is OK. It that is not the case, we should
  ** not even bother looking further ...
  */

  if ((x_word)mem & 0x00000007) {
    return FALSE;
  }

  /*
  ** Check if it has the correct bounds.
  **
  ** Note that 'top' is being checked while the memory allocator is not locked. 
  ** It can change but it's value will not be changed such that we get error results.
  */

  if ((check >= top) || (check < (x_chunk)sbrk_base)) {
    return FALSE;
  }

  /*
  ** ... now check if the chunk is in use ...
  */
  
  if (! check->this_size & CURRENT_IN_USE) {
    return FALSE;
  }

  /*
  ** ... now check if it isn't garbage ...
  */
  
  if (is_garbage(check)) {
    return FALSE;
  }

  /*
  ** ... OK, so the alignment, in use bit, garbage status and bounds are allright, let's dig deeper ...
  */

  memory_lock(memory_monitor);

  calls += 1;

  /*
  ** Try to find an appropriate chunk 'leader' from the index. If we don't
  ** find a valid one, we go one slot up and set a flag that we should cache
  ** the chunk leaders during the walk. In the allocation and free routines,
  ** invalid chunk leaders will get cleared and this routine will heat up the
  ** index array.
  */
      
  /*
  ** and now Johnny, walk the walk...
  */

  for (cursor = initial; cursor < top; cursor = treat_as_chunk(cursor, size)) {

    walked += 1;

    if (cursor == check) {
      is_block = TRUE;
      break;
    }
    
    if (cursor > check) {
      break;
    }
    
    size = cursor->this_size & FREE_UNITS_MASK;
    if (cursor->this_size & CURRENT_IN_USE && ! (cursor->this_size & BIG_CHUNK)) {
      size &= USED_UNITS_MASK;
    }
    
    if (runtime_checks && size == 0) {
      wabort(ABORT_WONKA, "Size is 0! cursor = %p\n", cursor);
    }

  }

  if (calls % 50000 == 0) {
//    printf("FINDEX cursor = %p, %u calls, %u walked, %f ratio\n", cursor, calls, walked, (double)walked / (double)calls);
  }

  memory_unlock(memory_monitor);

  return is_block;

}

/*
** The two routines to lock and unlock memory operations.

x_status x_mem_lock(x_sleep timeout) {
  return x_monitor_enter(memory_monitor, timeout);
}

x_status x_mem_unlock(void) {
  return x_monitor_exit(memory_monitor);
}
*/

/*
** Get total and free memory. We don't lock before reading out the values
** since these routines don't change anything internally and the reported
** numbers should not be interpreted as the exact truth... Note also that
** the number of used bytes = x_mem_total - x_mem_avail.
*/

x_size x_mem_total(void) {
  return max_heap_bytes;
}

x_size x_mem_avail(void) {
  return (free_units << 3);
}

/*
** The 'fast free' routine and the corresponding garbage collector function. It's
** fast since it doesn't lock the memory functions and only sets a flag. Don't get
** too high on using it though, since the memory is not re-used untill it's garbage
** collected by x_mem_collect.
*/

void x_mem_discard(void * mem) {

  x_chunk chunk;

  if (mem) {
    memory_lock(memory_monitor);
    chunk = mem2chunk(mem);
    chunk->this_size |= GARBAGE_TAG;
    memory_unlock(memory_monitor);
  }
  
}

x_status x_mem_collect(x_size * bytesr, x_size * numr) {

  x_chunk cursor;
  x_chunk next;
  x_status status;
  x_units size;
  x_size bytes = 0;
  x_size num = 0;
  
  status = x_monitor_eternal(memory_monitor);
  if (status != xs_success) {
    return status;
  }

  for (cursor = initial; cursor < top; cursor = next) {
    size = cursor->this_size & FREE_UNITS_MASK;

    if (is_garbage(cursor)) {
      size &= USED_UNITS_MASK;
      num += 1;

      /*
      ** Coalesce as much garbage blocks as you can find after the current cursor...
      */      
      
      next = treat_as_chunk(cursor, size);
      while (is_garbage(next) && (next < top)) {
        size += next->this_size & USED_UNITS_MASK;
        num += 1;
        next = treat_as_chunk(next, next->this_size & USED_UNITS_MASK);
      }

      /*
      ** Clear the current in use bit, the garbage bit and tag bits, since we are giving this
      ** chunk a new size and the current in use bit needs to be reset before we free the chunk.
      */
      
      cursor->this_size = (cursor->this_size & ~(CURRENT_IN_USE | GARBAGE_TAG | FREE_UNITS_MASK)) | size;
      bytes += (size << 3);

      /*
      ** When the next chunk is free and isn't top, we skip over it for the next time
      ** around since the chunk we set free now, will be coalesced with it.
      */
      
      next = treat_as_chunk(cursor, size);
      if (next < top) {
        if (! (next->this_size & CURRENT_IN_USE)) {
          size += next->this_size & FREE_UNITS_MASK;
          next = treat_as_chunk(cursor, size);
        }
      }
      
      chunk_free(cursor);
    }
    else if (cursor->this_size & CURRENT_IN_USE) {
      size &= USED_UNITS_MASK;
      next = treat_as_chunk(cursor, size);
    }
    else {
      next = treat_as_chunk(cursor, size);
    }
    
  }

  status = x_monitor_exit(memory_monitor);

  if (bytesr) {
    *bytesr = bytes;
  }
  
  if (numr) {
    *numr = num;
  }

  return status;
  
}

#endif

#ifdef SHARED_HEAP 

void *calloc(size_t nmemb, size_t size) {
  return x_mem_calloc(nmemb * size);
}

void *malloc(size_t size) {
  return x_mem_alloc(size);
}

void free(void *ptr) {
  x_mem_free(ptr);
}

void *realloc(void *ptr, size_t size) {
  return x_mem_realloc(ptr, size);
}

#endif

#endif


/*
** Fake implementation of bogus function
*/
void * x_alloc_static_mem(void * memory, w_size size) {
  return calloc((size+7)/8, 8);
}
