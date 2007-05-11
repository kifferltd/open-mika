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
**************************************************************************/


/*
** $Id: mem.c,v 1.1 2006/09/11 13:21:38 cvsroot Exp $
*/    
 

#include "wonka.h"
#include "oswald.h"

/** Memory chunk header
    +----------+
    ! reserved ! ) Just to bring the total up to 8 words.
    +----------+  
    !    id    ! User-supplied object type identifier.  Values 0..31 reserved.
    +----------+
    !   next   ! )
    +----------+ ) The linked list used to emulate Oswald's heap-walking stuff
    ! previous ! )
    +----------+
    !   file   ! )
    +----------+ ) Program location where allocated
    !   line   ! )
    +----------+
    !   size   ! Requested size in bytes
    +----------+
    !   check  ! Must point to `magic'
    +----------+
    ! contents !
    :          :
*/

typedef struct o4p_Memory_Chunk *o4p_memory_chunk;

typedef struct o4p_Memory_Chunk {
  w_word            reserved0;
  w_word            id;
  o4p_memory_chunk  next;
  o4p_memory_chunk  previous;
  char             *file;
  w_size            line;
  w_size            size;
  char             *check;
} o4p_Memory_Chunk;

/*
** We only use bits 27..18 of the `id' word: in the `real' oswald this shares
** space with the chunk size and a couple of other flags.
*/
#define TAG_MASK               0x1ff00000 // The tag can hold a 9 bit information NUMBER, numbers 0 - 31 are reserved.
#define GARBAGE_TAG            0x20000000 // Piece of memory is garbage, can be reclaimed by our OWN garbage collector

/// The maximum amount of memory we allow ourselves to allocate in a single chunk.
/// (Oswald has such a limit).
#ifndef MAX_SINGLE_ALLOC
#define MAX_SINGLE_ALLOC  8*1024*1024
#endif

x_size heap_size; 
x_size heap_remaining;

x_monitor memory_monitor;

x_status x_mem_lock(x_sleep timeout) {
  return x_monitor_enter(memory_monitor, timeout);
}

x_status x_mem_unlock() {
  return x_monitor_exit(memory_monitor);
}


const char *magic = "This memory is valid.";

static o4p_Memory_Chunk Memory_Sentinel;
static o4p_memory_chunk memory_sentinel;

static void *chunk2mem(o4p_memory_chunk chunk) {
  return ((char*)chunk) + sizeof(o4p_Memory_Chunk);
}

static o4p_memory_chunk mem2chunk(void *mem) {
  return (o4p_memory_chunk)(((char*)mem) - sizeof(o4p_Memory_Chunk));
}

x_ubyte *x_mem_init(x_ubyte *start) {
// We were given some memory to play with, but we won't use it ...
  free(start);
  memory_sentinel = &Memory_Sentinel;
  x_list_init(memory_sentinel);
  memory_sentinel->id = 0;
  memory_sentinel->file = __FILE__;
  memory_sentinel->line = __LINE__;
  memory_sentinel->size = 0;
  memory_sentinel->check = (char*)magic;

  memory_monitor = calloc(1, sizeof(x_Monitor));
  x_monitor_create(memory_monitor);

  return NULL;
}

void *_x_mem_alloc(w_size size, const char *file, int line) {
  void *new = NULL;

  if (size > MAX_SINGLE_ALLOC) {
    loempa(9,"%s:d Attempt to allocate %d bytes, maximum is %d!\n",file,line,size,MAX_SINGLE_ALLOC);

    return NULL;
  }
  else if (size > heap_remaining) {
    loempa(9,"%s:d Attempt to allocate %d bytes, available space is %d!\n",file,line,size, heap_remaining);

    return NULL;
  }
  else {
    new = calloc((size + 7 + sizeof(o4p_Memory_Chunk)) / 8, 8);
  }

  if (new) {
    o4p_memory_chunk chunk = (o4p_memory_chunk)new;

    chunk->file = (char*)file;
    chunk->line = line;
    chunk->size = size;
    chunk->check = (char*)magic;
    loempa(1,"%s:%d Allocated %d bytes at %p\n", chunk->file, chunk->line, size, new);
    x_mem_lock(x_eternal);
    x_list_insert(memory_sentinel, chunk);
    heap_remaining -= size + sizeof(o4p_Memory_Chunk);
    loempa(1,"Heap remaining: %d bytes\n", heap_remaining);
    x_mem_unlock();

    return chunk2mem(chunk);
  }
  else {
    loempa(9,"%s:%d Out of memory!  heap_remaining was %d, setting it to 0\n", file, line, heap_remaining);
    heap_remaining = 0;
  }

  return NULL;
}

void *_x_mem_calloc(w_size size, const char *file, int line) {
  void *new = NULL;

  if (size > MAX_SINGLE_ALLOC) {
    loempa(9,"%s:d Attempt to allocate %d bytes, maximum is %d!\n",file,line,size,MAX_SINGLE_ALLOC);

    return NULL;
  }
  else if (size > heap_remaining) {
    loempa(9,"%s:d Attempt to allocate %d bytes, available space is %d!\n",file,line,size, heap_remaining);

    return NULL;
  }
  else {
    w_size calloc_size = (size + sizeof(o4p_Memory_Chunk) + 7) / 8;
    new = calloc(calloc_size, 8);
  }

  if (new) {
    o4p_memory_chunk chunk = (o4p_memory_chunk)new;

    chunk->file = (char*)file;
    chunk->line = line;
    chunk->size = size;
    chunk->check = (char*)magic;
    loempa(1,"%s:%d Allocated %d bytes at %p\n", chunk->file, chunk->line, size, new);
    x_mem_lock(x_eternal);
    x_list_insert(memory_sentinel, chunk);
    heap_remaining -= size + sizeof(o4p_Memory_Chunk);
    loempa(1,"Heap remaining: %d bytes\n", heap_remaining);
    x_mem_unlock();

    return chunk2mem(chunk);
  }
  else {
    loempa(9,"%s:%d Out of memory!  heap_remaining was %d, setting it to 0\n", file, line, heap_remaining);
    heap_remaining = 0;
  }

  return NULL;
}

void *x_mem_realloc(void *old, w_size size) {
  o4p_memory_chunk oldchunk = mem2chunk(old);
  o4p_memory_chunk newchunk;
  void *new;

  
  if (oldchunk->check != magic) {
    loempa(9,"Memory block %p is not valid!\n", old);

    return NULL;

  }

  if (size <= oldchunk->size) {
    x_mem_lock(x_eternal);
    heap_remaining += oldchunk->size - size;
    oldchunk->size = size;
    x_mem_unlock();
  
    return old;

  }

  new = x_mem_alloc(size);
  newchunk = mem2chunk(new);
  loempa(1, "New chunk is at %p, copying %d bytes from %p to %p\n", newchunk, oldchunk->size, old, new); 
  memcpy(new, old, oldchunk->size);
  loempa(1,"Marking chunk %p with id 0x%x\n", new, oldchunk->id);
  x_mem_tag_set(new, oldchunk->id);
  x_mem_free(old);

  return new;
}

void x_mem_free(void *block) {
  o4p_memory_chunk chunk = mem2chunk(block);
  
  if (chunk->check != magic) {
    loempa(9,"Memory block %p is not valid!\n", block);
  }

  loempa(1,"Returning %d bytes at %p allocated at %s:%d\n", chunk->size, block, chunk->file, chunk->line);
  x_mem_lock(x_eternal);
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
  
  if (chunk->check != magic) {
    loempa(9,"Memory block %p is not valid!\n", mem);

    return xs_unknown;
  }

  loempa(1,"Marking chunk %p (allocated at %s:%d) with id 0x%x\n", mem, chunk->file, chunk->line, tag);
  chunk->id |= tag;

  return xs_success;
}

w_word x_mem_tag_get(void * mem) {
  o4p_memory_chunk chunk = mem2chunk(mem);
  
  if (chunk->check != magic) {
    loempa(9,"Memory block %p is not valid!\n", mem);

    return 0;

  }

  loempa(1,"Chunk %p (allocated at %s:%d) has id 0x%x\n", mem, chunk->file, chunk->line, chunk->id);

  return chunk->id;
}

w_size x_mem_size(void * mem) {
  o4p_memory_chunk chunk = mem2chunk(mem);
  
  if (chunk->check != magic) {
    loempa(9,"Memory block %p is not valid!\n", mem);

    return 0;

  }

  loempa(1,"Chunk %p (allocated at %s:%d) has size %d\n", mem, chunk->file, chunk->line, chunk->size);

  return chunk->size;
}

x_boolean x_mem_is_block(void * mem) {
  o4p_memory_chunk chunk = mem2chunk(mem);
  
  return chunk->check == magic;
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
  return true;
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

