/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2009, 2010, 2011, 2012,     *
* 2013 by Chris Gray, /k/ Embedded Java Solutions. All rights reserved.   *
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

#include "heap.h"
#ifdef RESMON
#include "core-classes.h"
#include "fields.h"
#include "hashtable.h"
#endif
#include "ts-mem.h"
#include "threads.h"
#include "oswald.h"
#include "exception.h"

/**
 ** alloc_barrier acts to slow down a thread which is allocating memory while
 ** another thread is sweeping the heap.
 */
static void alloc_barrier(w_thread thread) {
  if (gc_monitor && !marking_thread && sweeping_thread && sweeping_thread != thread) {
    int p = thread ? thread->jpriority : 5;
    if (p < 10) {
      woempa(7, "sleeping for (%d + %d + 1) * %d = %d ticks\n", *gc_kicks_pointer, memory_load_factor, 5 - p / 2, (*gc_kicks_pointer + memory_load_factor + 1) * (5 - p / 2));
      x_thread_sleep((*gc_kicks_pointer + memory_load_factor + 1) * (5 - p / 2));
    }
  }
}

#ifdef RESMON
extern w_hashtable resmon_memory_hashtable;
extern w_boolean pre_alloc_check(w_thread thread, w_size nbytes);
extern w_boolean pre_realloc_check(w_thread thread, w_size newbytes, w_size oldbytes);
extern void post_alloc(w_thread thread, void *address);
extern void pre_dealloc(w_thread thread, void *address, w_size nbytes);
#else
#define pre_alloc_check(t,n) TRUE
#define pre_realloc_check(t,n,o) TRUE
#define post_alloc(t,a)
#define pre_dealloc(t,a,n)
#endif

/*
** Allocate cleared (zeroed) memory.
*/
void * _allocClearedMem(w_size rsize) {

  w_chunk chunk;
  w_thread thread = currentWonkaThread;

  gc_reclaim(rsize, NULL);

  if (pre_alloc_check(thread, rsize)) {
    alloc_barrier(thread);
    chunk = x_mem_calloc(sizeof(w_Chunk) + rsize);
  }
  else {
    chunk = NULL;
  }

  if (chunk) {
    post_alloc(thread, chunk);
  }
  else {
    w_printf("Failed to allocate %d bytes\n", rsize);

    if (thread && thread->Thread && isNotSet(thread->flags, WT_THREAD_THROWING_OOME)) {
      throwOutOfMemoryError(thread, rsize);
    }

    return NULL;
  }

  return chunk->data;

}

/*
** Allocate memory. The contents are not cleared, and may contain "junk".
*/
void * _allocMem(w_size rsize) {

  w_chunk chunk;
  w_thread thread = currentWonkaThread;

  gc_reclaim(rsize, NULL);

  if (pre_alloc_check(thread, rsize)) {
    alloc_barrier(thread);
    chunk = x_mem_alloc(sizeof(w_Chunk) + rsize);
  }
  else {
    chunk = NULL;
  }

  if (chunk) {
    post_alloc(thread, chunk);
  }
  else {
    w_printf("Failed to allocate %d bytes\n", rsize);

    if (thread && thread->Thread && isNotSet(thread->flags, WT_THREAD_THROWING_OOME)) {
      throwOutOfMemoryError(thread, rsize);
    }

    return NULL;
  }

  return chunk->data;

}

void * _reallocMem(void * block, w_size newsize) {
  w_chunk oldchunk;
  w_size  oldsize;
  w_chunk newchunk;
  w_thread thread = currentWonkaThread;

  oldchunk = block2chunk(block);
  oldsize = x_mem_size(oldchunk);
  gc_reclaim(newsize - oldsize, NULL);

  if (pre_realloc_check(thread, newsize, oldsize)) {
    alloc_barrier(thread);
    newchunk = x_mem_realloc(oldchunk, sizeof(w_Chunk) + newsize);
  }
  else {
    newchunk = NULL;
  }
  
  if (newchunk) {
    post_alloc(thread, newchunk);
  }
  else {
    w_printf("Failed to reallocate %d bytes\n", newsize);

    if (thread && thread->Thread && isNotSet(thread->flags, WT_THREAD_THROWING_OOME)) {
      throwOutOfMemoryError(thread, newsize);
    }

    return NULL;
  }

  return newchunk->data;

}

void _releaseMem(void * block) {
  w_chunk chunk;
  if (!block) {
    wabort(ABORT_WONKA, "Bah. releaseMem(NULL)?");
  }

  chunk = block2chunk(block);
  pre_dealloc(currentWonkaThread, chunk, x_mem_size(chunk));
  x_mem_free(chunk);
}

void _discardMem(void * block) {

  w_chunk chunk;

  if (block) {
    chunk = block2chunk(block);
    pre_dealloc(currentWonkaThread, chunk, x_mem_size(chunk));
    x_mem_discard(chunk);
  }

}

#ifdef TRACE_MEM_ALLOC // --------------------------------------------------------------

#include <stdlib.h>
#include <string.h>

#define CHUNK_MADGIC                0xbbbbbbbb
#define FRONT_FENCE                 0xcccccccc
#define BACK_BYTE                   0xdd
#define FREED_BYTE                  0xee
#define FREED_DATA                  0xaa

// const 
int check_limit = 10000;        // Every check_limit releases/reallocs, a complete memory check is done

/*
** Our memory walker argument structure.
*/

typedef struct w_Wa * w_wa;

typedef struct w_Wa {
  w_int count;
  w_int bytes;
  w_int errors;
  w_int all_count;
  w_int all_bytes;
  const char * function;
  w_int line;
} w_Wa;

/*
** The memory walker callback to check for faulty chunks.
*/

static w_chunk previous;

static x_boolean checkWalk(void * mem, void * arg) {

  w_wa wa = arg;
  w_chunk chunk = mem;
  w_int bad;

  if (chunk->madgic != CHUNK_MADGIC) {
    woempa(1, "chunk %p madgic at %p holds 0x%08x instead of 0x%08x, it's probably not one of ours.\n", chunk, &chunk->madgic, chunk->madgic, CHUNK_MADGIC);
    return WONKA_TRUE;
  }    

  woempa(1, "%d %p %s:%d size %d back %d\n", wa->all_count, chunk, chunk->file, chunk->line, chunk->size, chunk->back);
  wa->all_count += 1;

  bad = 0;
  if (chunk->front != FRONT_FENCE) {
    woempa(9, "chunk %p front fence at %p holds 0x%08x instead of 0x%08x.\n", chunk, &chunk->front, chunk->front, FRONT_FENCE);
    bad += 1;
  }

  if ((x_size)chunk->back != chunk->size) {
    woempa(9, "chunk back index %d != chunk size %d.\n", chunk->back, chunk->size);
    bad += 100;
  }
    
  /*
  ** Only check the back byte if the previous tests turned out OK.
  */

  if (bad == 0 && ! (chunk->data[chunk->back] == BACK_BYTE || chunk->data[chunk->back] == FREED_BYTE)) {
    woempa(9, "bad back = 0x%08x\n", chunk->data[chunk->back]);
    bad += 1000;
  }

  if (bad) {
    wa->errors += 1;
    woempa(9, "CHUNK ERROR: number %d, bad chunk 0x%08x, status = %d\n", wa->count, chunk, bad);
    woempa(9, "  alloced in %s:%d size = %d\n", chunk->file, chunk->line, chunk->size);
    woempa(9, "  instance of %k\n", ((w_clazz*)chunk->data)[0]);
    if (previous) {
      woempa(9, "  previous chunk 0x%08x, alloced in %s:%d, size = %d\n", previous, previous->file, previous->line, previous->size);
    }
  }
  else {
//    wa->all_bytes += chunk->size + sizeof(w_Chunk);
    wa->all_bytes += x_mem_size(chunk);
//      wa->bytes += chunk->size + sizeof(w_Chunk);
    wa->bytes += x_mem_size(chunk);
    wa->count += 1;
  }
  previous = chunk;

  return WONKA_TRUE;
  
}

void _heapCheck(const char * function, const int line) {

  static w_int walks = 0;
  w_Wa Wa;
  w_wa wa = &Wa;

  wa->count = 0;
  wa->bytes = 0;
  wa->errors = 0;
  wa->all_count = 0;
  wa->all_bytes = 0;
  wa->function = function;
  wa->line = line;
  previous = NULL;
  x_mem_walk(x_eternal, checkWalk, wa);
  walks += 1;
  woempa(9, "%s %d : %d anon %d tagged, %d MB anon %d MB tagged, %d errors, %d scans.\n", function, line, wa->all_count, wa->count, wa->all_bytes / (1024 * 1024), wa->bytes / (1024 * 1024), wa->errors, walks);
  w_printf("%s %d : %d anon %d tagged, %d MB anon %d MB tagged, %d errors, %d scans.\n", function, line, wa->all_count, wa->count, wa->all_bytes / (1024 * 1024), wa->bytes / (1024 * 1024), wa->errors, walks);
  
  if (wa->errors) {
    wabort(ABORT_WONKA, "x_mem_walk returned %d errors\n", wa->errors);
  }
}

/*
** Perform the usual checking on a chunk. When we hit the limit for full checking,
** we perform a complete memory scan for faulty chunks.
*/

static w_chunk checkChunk(void * block, w_int for_realloc, const char * file, const int line) {

  static w_int errors = 0;
  static w_int checks = 0;
  static w_int walks = 0;
  w_chunk chunk;
  w_Wa Wa;
  w_wa wa = &Wa;

  checks += 1;
  
  if (checks % check_limit == 0) {
    x_mem_lock(x_eternal);
    woempa(7, "Performing heap check for alloc/releaseMem at %s:%d, count = %d\n", file, line, checks);
    wa->count = 0;
    wa->bytes = 0;
    wa->errors = 0;
    wa->all_bytes = 0;
    wa->all_count = 0;
    previous = NULL;
    x_mem_walk(x_eternal, checkWalk, wa);
    walks += 1;
    errors += wa->errors;
    x_mem_unlock();
    woempa(9, "%s.%d (alloc/release %p): Walked %d anon %d tagged chunks, %d MB anon %d Mb tagged, %d chunks were in error. Made %d scans.\n", file, line, block2chunk(block), wa->all_count - wa->count, wa->count, (wa->all_bytes - wa->bytes) / (1024 * 1024), wa->bytes / (1024 * 1024), wa->errors, walks);
    if (wa->errors) {
      wabort(ABORT_WONKA, "Found bad chunk. Stopping...\n");
    }
  }

  if (block == NULL) {
    wabort(ABORT_WONKA, "Freeing NULL block at %s:%d.\n", file, line);
  }

  /*
  ** It looks like a valid block, just proceed...
  */

  chunk = block2chunk(block);

  if (chunk->data[chunk->back] == FREED_BYTE) {
    wabort(ABORT_WONKA, "Freeing chunk %p twice (allocated %s %d, released %s %d).\n", chunk, chunk->file, chunk->line, file, line);
    return NULL;
  }

  if (chunk->data[chunk->back] != BACK_BYTE) {
    wabort(ABORT_WONKA, "Back fence overwritten of chunk %p (allocated %s %d, released %s %d).\n", chunk, chunk->file, chunk->line, file, line);
    return NULL;
  }

  if (chunk->front != FRONT_FENCE) {
    wabort(ABORT_WONKA, "Front fence of %p overwritten (allocated %s %d, released %s %d).\n", chunk, chunk->file, chunk->line, file, line);
    return NULL;
  }

  if (chunk->madgic != CHUNK_MADGIC) {
    wabort(ABORT_WONKA, "Wrong madgic of %p (allocated %s %d, released %s %d).\n", chunk, chunk->file, chunk->line, file, line);
    return NULL;
  }

  /*
  ** If this check isn't done for realloc, we overwrite the data area to quickly evoke
  ** errors when freed memory would b re-used. Also the backfence will get a new color.
  */

  if (! for_realloc) {
    memset(chunk->data, FREED_DATA, chunk->size);
    chunk->data[chunk->back] = FREED_BYTE;
  }

  return chunk;
  
}

/*
** Attach debugging information to a chunk.
*/

static void prepareChunk(w_chunk chunk, w_size size, const char * file, const int line) {

  woempa(1,"Preparing chunk %p, size %d, allocated at %s:%d\n", chunk, size, file, line);
  chunk->file = file;
  chunk->line = line;
  chunk->size = size;
  chunk->madgic = CHUNK_MADGIC;
  chunk->front = FRONT_FENCE;
  chunk->back = size;
  woempa(1,"Chunk %p data starts at %p, back fence at %p\n", chunk, chunk->data, &chunk->data[size]);
  chunk->data[size] = BACK_BYTE;
}

void * _d_allocClearedMem(w_size rsize, const char * file, const int line) {

  w_chunk chunk;
  w_thread thread = currentWonkaThread;

  woempa(1,"%s.%d: Requested %d bytes, allocating %d bytes\n", file, line, rsize, sizeof(w_Chunk) + rsize);
  if (pre_alloc_check(thread, rsize)) {
    alloc_barrier(thread);
    x_mem_lock(x_eternal);
    chunk = _x_mem_calloc(sizeof(w_Chunk) + rsize, file, line);
  }
  else {
    chunk = NULL;
  }

  if (!chunk) {
    x_mem_unlock();
    woempa(9, "Failed to allocate %d bytes\n", rsize);

    if (thread && thread->Thread && isNotSet(thread->flags, WT_THREAD_THROWING_OOME)) {
      throwOutOfMemoryError(thread, rsize);
    }

    return NULL;
  }

  post_alloc(thread, chunk);
  woempa(1,"%s.%d: Allocated %d bytes at %p\n", file, line, rsize, chunk);
  prepareChunk(chunk, rsize, file, line);  
  x_mem_unlock();
  checkChunk(chunk->data, 1, file, line);

  return chunk->data;

}

void * _d_allocMem(w_size rsize, const char * file, const int line) {

  w_chunk chunk;
  w_thread thread = currentWonkaThread;

  woempa(1,"%s.%d: Requested %d bytes, allocating %d bytes\n", file, line, rsize, sizeof(w_Chunk) + rsize);
  gc_reclaim(rsize, NULL);

  if (pre_alloc_check(thread, rsize)) {
    alloc_barrier(thread);
    x_mem_lock(x_eternal);
    chunk = _x_mem_alloc(sizeof(w_Chunk) + rsize, file, line);
  }
  else {
    chunk = NULL;
  }

  if (chunk) {
    post_alloc(thread, chunk);
  }
  else {
    x_mem_unlock();
    woempa(9, "Failed to allocate %d bytes for %s:%d\n", rsize, file, line);

    if (thread && thread->Thread && isNotSet(thread->flags, WT_THREAD_THROWING_OOME)) {
      throwOutOfMemoryError(thread, rsize);
    }

    return NULL;
  }

  woempa(1,"%s.%d: Allocated %d bytes at %p\n", file, line, rsize, chunk);
  prepareChunk(chunk, rsize, file, line);  
  x_mem_unlock();
  checkChunk(chunk->data, 1, file, line);
  
  return chunk->data;

}

void * _d_reallocMem(void * block, w_size newsize, const char * file, const int line) {
  w_chunk oldchunk;
  w_chunk newchunk;
  w_size  oldsize;
  w_thread thread = currentWonkaThread;

  woempa(1,"%s.%d: Reallocating block %p, new size = %d\n", file, line, block, newsize);
  oldchunk = checkChunk(block, 1, file, line);
  if (!oldchunk) {
    woempa(9, "Failed to reallocate %d bytes for %s:%d\n", newsize, file, line);

    return NULL;

  }

  pre_dealloc(thread, oldchunk, 0);
  oldsize = x_mem_size(oldchunk);
  woempa(1,"%s.%d: Requested %d bytes, allocating %d bytes\n", file, line, newsize, sizeof(w_Chunk) + newsize);
  gc_reclaim(newsize - oldsize, NULL);

  if (pre_realloc_check(thread, newsize, oldsize)) {
    alloc_barrier(thread);
    x_mem_lock(x_eternal);
    newchunk = _x_mem_realloc(oldchunk, sizeof(w_Chunk) + newsize, file, line);
  }
  else {
    newchunk = NULL;
  }

  if (newchunk) {
    post_alloc(thread, newchunk);
  }
  else {
    x_mem_unlock();
    printf("Failed to reallocate %d bytes for %s:%d\n", newsize, file, line);

    if (thread && thread->Thread && isNotSet(thread->flags, WT_THREAD_THROWING_OOME)) {
      throwOutOfMemoryError(thread, newsize);
    }

    return NULL;

  }

  woempa(1,"%s.%d: Reallocated %d bytes at %p\n", file, line, newsize, newchunk);
  prepareChunk(newchunk, newsize, file, line);  
  x_mem_unlock();
  checkChunk(newchunk->data, 1, file, line);

  return newchunk->data;
}

void _d_releaseMem(void * block, const char * file, const int line) {

  w_chunk chunk;

  woempa(1,"Releasing block %p\n", block);
  chunk = checkChunk(block, 0, file, line);
  if (!chunk) {

    return;

  }
  // Remove the madgic marker - otherwise if x_mem_alloc later returns the
  // same memory it will look "one of ours" even if it isn't.
  chunk->madgic = 0;

  pre_dealloc(currentWonkaThread, chunk, x_mem_size(chunk));
  x_mem_free(chunk);

}

void _d_discardMem(void * block, const char * file, const int line) {

  w_chunk chunk;

  woempa(1,"Discarding block %p\n", block);
  chunk = checkChunk(block, 0, file, line);
  if (!chunk) {

    return;

  }
  // Remove the madgic marker - otherwise if x_mem_alloc later returns the
  // same memory it will look "one of ours" even if it isn't.
  chunk->madgic = 0;

  pre_dealloc(currentWonkaThread, chunk, x_mem_size(chunk));
  x_mem_discard(chunk);

}

/*
** A chunk reporting structure.
*/

typedef struct w_Cr * w_cr;

typedef struct w_Cr {
  w_cr next;
  w_word hash;
  const char * file;
  w_int line;
  w_int hits;
  w_int total;
} w_Cr;

/*
** Our own small custom hashtable, the number of buckets it has and the total count
** of report structures that are in the hashtable.
*/

#define NUMBER_OF_BUCKETS 64
#define NUMBER_OF_ENTRIES 256

static w_cr ht[NUMBER_OF_BUCKETS];
static w_Cr ht_entries[NUMBER_OF_ENTRIES];
static w_int num_cr;

static w_int cr_compare(w_cr cr1, w_cr cr2) {

  woempa(1, "comparing {%s:%d} with {%s:%d}\n", cr1->file, cr1->line, cr2->file, cr2->line);
  if ((cr1->file == cr2->file) && (cr1->line == cr2->line)) {
    return 1;
  }
  else {
    return 0;
  }

}

/*
** See if a cr structure is allready in our hashtable. Returns the found one or NULL
** if not found.
*/

static w_cr cr_find(w_cr new) {

  w_size i;
  w_cr ex;
  
  i = new->hash % NUMBER_OF_BUCKETS;
  woempa(1, "hash is %08x, start search at [%d]\n", new->hash, i);
  for (ex = ht[i]; ex; ex = ex->next) {
    woempa(1, "  trying %p\n", ex);
    if (cr_compare(ex, new)) {
      return ex;
    }
  }
  woempa(1, "no match found\n");

  return NULL;

}

/*
** Add a new cr to the hashtable.
*/

static void cr_add(w_cr new) {

  w_size i = new->hash % NUMBER_OF_BUCKETS;
  w_cr ex;

  new->next = NULL;

  for (ex = ht[i]; ex; ex = ex->next) {
    if (ex->next == NULL) {
      ex->next = new;
      woempa(1, "Bucket %d: chained in %p after %p\n", i, new, ex);
      break;
    }
  }

  /*
  ** There was no leader yet in the hashtable, make this one the leader.
  */
  
  if (ex == NULL) {
    ht[i] = new;
    woempa(1, "Bucket %d: started new chain with %p\n", i, new);
  }
  
}

/*
** Our memory walker callback to walk over the chunks and add reporting structures to our
** hastable.
*/

static x_boolean cr_walk(void * block, void * arg) {

  w_wa wa = arg;
  w_cr cr;
  w_cr ex;
  w_chunk chunk;
  
  if (num_cr >= NUMBER_OF_ENTRIES) {
    woempa(9, "Too many entries - aborting scan\n");

    return FALSE;

  }

  chunk = block;
  woempa(7, "Scanning block %08p\n", block);
  
  wa->count += 1;
  wa->bytes += chunk->size;

  cr = &ht_entries[num_cr];
  cr->file = chunk->file;
  cr->line = chunk->line;
  cr->next = NULL;
  cr->hits = 1;
  cr->total = chunk->size;
  cr->hash = (w_word)cr->file + cr->line;

  ex = cr_find(cr);
  if (ex) {
    woempa(7, "found match at %p (%s:%d)\n", ex, cr->file, cr->line);
    ex->hits += 1;
    ex->total += chunk->size;
  }
  else {
    woempa(7, "found no match, adding new entry for %s:%d\n", cr->file, cr->line);
    cr_add(cr);
    num_cr += 1;

    woempa(9, "num_cr = %d\n", num_cr);
  }

  return TRUE;
  
}

static const char * fno(const char * file) {

  char * location;
  
  location = strrchr(file, '/');
  if (location == NULL) {
    location = (char *)file;
  }
  else {
    location += 1;
  }
  
  return location;
  
}

w_cr array[NUMBER_OF_ENTRIES];
static w_Wa memStatWa;

void reportMemStat(w_int type) {

  w_cr cr;
  w_int indx;
  w_int i;
  w_int overhead_mb;
  w_int requested_mb;
  w_int percent;
  w_int total_percent = 0;
  w_int was_printed = 0;
  w_wa wa = &memStatWa;
 
  x_mem_lock(x_eternal);
  
  num_cr = 0;

  /*
  ** Walk over all blocks.
  */
  
  wa->count = 0;
  wa->bytes = 0;
  wa->errors = 0;
  wa->all_count = 0;
  wa->all_bytes = 0;
  for (i = 0; i < NUMBER_OF_BUCKETS; i++) {
    ht[i] = NULL;
  }
  w_printf("ht = %p wa = %p: taking a walk now\n", ht, wa);
  x_mem_walk(x_eternal, cr_walk, wa);
  w_printf("OK, enjoyed our stroll ...\n");

  /*
  ** Iterate over the hashtable and put all reporting structure references
  ** in an array.
  */

  indx = 0;
  for (i = 0; i < NUMBER_OF_BUCKETS; i++) {
    if (ht[i]) {
      for (cr = ht[i]; cr && indx < NUMBER_OF_ENTRIES; cr = cr->next) {
        array[indx++] = cr;
      }
    }
  }

  /*
  ** Do the reporting...
  */

  overhead_mb = (wa->count * sizeof(w_Chunk)) / (1024 * 1024);
  requested_mb = wa->bytes / (1024 * 1024);

  if (indx) {
    w_printf("+---------------------------+--------+---------------+--------------+-------------+\n");
    w_printf("|                file: line |  hits  | bytes req.  %% | bytes overh. | bytes total |\n");
    w_printf("+---------------------------+--------+---------------+--------------+-------------+\n");
  }
  for (i = 0; i < indx; i++) {
    cr = array[i];
    percent = ((w_int)(cr->total * 100)) / wa->bytes;
    total_percent += percent;
    w_printf("|%20s:%5d | %6d | %7d  %3d%% |   %8d   | %9d   |\n", 
      fno(cr->file), cr->line, cr->hits, cr->total, percent, (w_int)(cr->hits * sizeof(w_Chunk)), cr->total + (w_int)(cr->hits * sizeof(w_Chunk)));
    was_printed = 1;
  }
  if (indx) {
    w_printf("+---------------------------+--------+---------------+--------------+-------------+\n");
    w_printf("Total requested bytes is %d (%d Mb) %d%% in %d chunks, total overhead bytes is %d (%d Mb)\n", 
      wa->bytes, requested_mb, total_percent, wa->count, (w_int)(wa->count * sizeof(w_Chunk)), overhead_mb);
  }

  x_mem_unlock();
}

#endif // --- TRACE_MEM_ALLOC ----------------------------------------------------------

