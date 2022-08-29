/**************************************************************************
* Copyright (c) 2010, 2018, 2021  by KIFFER Ltd.  All rights reserved.    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#include "oswald.h"

x_size heap_size; 
x_size heap_remaining;

x_size min_heap_bytes;
x_size max_heap_bytes;

#define FreeRTOS_heap_remaining (sysconf(_SC_AVPHYS_PAGES) * sysconf(_SC_PAGESIZE))

SemaphoreHandle_t memoryMutex;

inline x_status x_mem_lock(x_sleep timeout) {
  if (o4fe->status == O4F_ENV_STATUS_NORMAL) {
    switch(xSemaphoreTakeRecursive(memoryMutex, timeout == x_eternal ? portMAX_DELAY : timeout)) {
      case pdPASS:  return xs_success;
      case pdFAIL:  return xs_no_instance;
      default :     return xs_unknown;
    }
  }
}

inline x_status x_mem_unlock() {
  if (o4fe->status == O4F_ENV_STATUS_NORMAL) {
    switch (xSemaphoreGiveRecursive(memoryMutex)) {
      case pdPASS: return xs_success;
      case pdFAIL: return xs_not_owner;
      default :    return xs_unknown;
    }
  }
}

const char *magic = "This memory is valid.";

static o4f_Memory_Chunk Memory_Sentinel;
static o4f_memory_chunk memory_sentinel;

#define chunk2mem(chunk) (((char*)chunk) + sizeof(o4f_Memory_Chunk))

#define mem2chunk(mem) ((o4f_memory_chunk)(((char*)mem) - sizeof(o4f_Memory_Chunk)))

typedef enum {
  chunk_ok,
  chunk_err_chunk_null,
  chunk_err_chunk_addr_invalid,
  chunk_err_next_null,
  chunk_err_next_addr_invalid,
  chunk_err_next_loop,
  chunk_err_previous_null,
  chunk_err_previous_addr_invalid,
  chunk_err_previous_loop,
  chunk_err_next_previous_not_same,
  chunk_err_previous_next_not_same,
} x_chunk_status;

static char *chunk_status_text [] = {
  "chunk_ok",
  "chunk_err_chunk_null",
  "chunk_err_chunk_addr_invalid",
  "chunk_err_next_null",
  "chunk_err_next_addr_invalid",
  "chunk_err_next_loop",
  "chunk_err_previous_null",
  "chunk_err_previous_addr_invalid",
  "chunk_err_previous_loop",
  "chunk_err_next_previous_not_same",
  "chunk_err_previous_next_not_same",
  NULL
};

static x_chunk_status chunk_sanity_check(o4f_memory_chunk chunk) {
  if (!chunk) {
    return chunk_err_chunk_null;
  }
  if (chunk<0x100000) {
    return chunk_err_chunk_addr_invalid;
  }
  if (!chunk) {
    return chunk_err_next_null;
  }
  if (chunk->next<0x100000 && chunk->next != memory_sentinel) {
    return chunk_err_next_addr_invalid;
  }
  if (chunk->next == chunk) {
    return chunk_err_next_loop;
  }
  if (chunk->previous<0x100000 && chunk->previous != memory_sentinel) {
    return chunk_err_previous_addr_invalid;
  }
  if (chunk->previous == chunk) {
    return chunk_err_previous_loop;
  }
  if (chunk->next->previous != chunk) {
    return chunk_err_next_previous_not_same;
  }
  if (chunk->previous->next != chunk) {
    return chunk_err_previous_next_not_same;
  }
  return chunk_ok;
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

  memoryMutex = xSemaphoreCreateRecursiveMutex();
}

#ifdef DEBUG
void *_x_mem_alloc(w_size size, const char *file, int line) {
  o4f_memory_chunk newchunk;

  if (size > FreeRTOS_heap_remaining) {
    loempa(9,"%s:%d Attempt to allocate %d bytes, available space is %d!\n",file,line,size, FreeRTOS_heap_remaining);

    return NULL;
  }
  else {
    newchunk = malloc(sizeof(o4f_Memory_Chunk) + size);
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
  loempa(1,"heap_remaining was %d\n", heap_remaining);
  x_mem_unlock();
  heap_remaining = FreeRTOS_heap_remaining;
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

void *_x_mem_calloc(w_size size, const char *file, int line) {
  o4f_memory_chunk newchunk;

  if (size > FreeRTOS_heap_remaining) {
    loempa(9,"%s:%d Attempt to allocate %d bytes, available space is %d!\n",file,line,size, FreeRTOS_heap_remaining);

    return NULL;
  }

  newchunk = calloc(sizeof(o4f_Memory_Chunk) + size, 1);

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
  heap_remaining = FreeRTOS_heap_remaining;
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

void *_x_mem_realloc(void *old, w_size size, const char *file, int line) {
  o4f_memory_chunk oldchunk = mem2chunk(old);
  o4f_memory_chunk newchunk;
  w_size oldsize;
  w_size newsize;

  if (oldchunk->check != magic) {
    loempa(9,"Memory block %p is not valid!\n", old);

    return NULL;

  }

  if (size > oldchunk->size && size - oldchunk->size > FreeRTOS_heap_remaining) {
    loempa(9,"%s:%d Attempt to allocate %d bytes, available space is %d!\n",file,line,size - oldchunk->size, FreeRTOS_heap_remaining);

    return NULL;
  }

  x_mem_lock(x_eternal);
  x_list_remove(oldchunk);
  oldsize = oldchunk->size + sizeof(o4f_Memory_Chunk);
  newsize = size + sizeof(o4f_Memory_Chunk);
  newchunk = realloc(oldchunk, newsize);
  if (!newchunk) {
    x_mem_unlock();
    free(oldchunk);
    heap_remaining = 0;

    return NULL;
  }

  newchunk->file = (char*)file;
  newchunk->line = line;
  newchunk->size = size;
  x_list_insert(memory_sentinel, newchunk);
  x_mem_unlock();
  heap_remaining = FreeRTOS_heap_remaining;
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

#else

void *_x_mem_alloc(w_size size) {
  o4f_memory_chunk newchunk;

  if (size > FreeRTOS_heap_remaining) {

    return NULL;
  }

  newchunk = malloc(sizeof(o4f_Memory_Chunk) + size);

  if (!newchunk) {
    heap_remaining = 0;

    return NULL;

  }

  newchunk->id = 0;
  newchunk->size = size;
  x_mem_lock(x_eternal);
  x_list_insert(memory_sentinel, newchunk);
  x_mem_unlock();

  heap_remaining = FreeRTOS_heap_remaining;
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

void *_x_mem_calloc(w_size size) {
  o4f_memory_chunk newchunk;

  if (size > FreeRTOS_heap_remaining) {

    return NULL;
  }

  newchunk = calloc(sizeof(o4f_Memory_Chunk) + size, 1);

  if (!newchunk) {
    heap_remaining = 0;

    return NULL;

  }

  newchunk->size = size;
  x_mem_lock(x_eternal);
  x_list_insert(memory_sentinel, newchunk);
  x_mem_unlock();

  heap_remaining = FreeRTOS_heap_remaining;
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

void *_x_mem_realloc(void *old, w_size size) {
  o4f_memory_chunk oldchunk = mem2chunk(old);
  o4f_memory_chunk newchunk;
  w_size oldsize;
  w_size newsize;

  if (size > oldchunk->size && size - oldchunk->size > FreeRTOS_heap_remaining) {
    loempa(9,"%s:d Attempt to allocate %d bytes, available space is %d!\n",file,line,size - oldchunk->size, FreeRTOS_heap_remaining);

    return NULL;
  }

  x_mem_lock(x_eternal);
  x_list_remove(oldchunk);
  oldsize = oldchunk->size + sizeof(o4f_Memory_Chunk);
  newsize = size + sizeof(o4f_Memory_Chunk);
  newchunk = realloc(oldchunk, newsize);
  if (!newchunk) {
    heap_remaining = 0;
    free(oldchunk);
    x_mem_unlock();

    return NULL;

  }

  newchunk->size = size;
  x_list_insert(memory_sentinel, newchunk);
  x_mem_unlock();

  heap_remaining = FreeRTOS_heap_remaining;
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

#endif

void x_mem_free(void *block) {
  o4f_memory_chunk chunk = mem2chunk(block);
  
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
  x_mem_unlock();
  heap_remaining = FreeRTOS_heap_remaining;
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  free(chunk);
}

w_size x_mem_avail(void) {
  return heap_remaining;
}

x_status x_mem_walk(x_sleep timeout, x_boolean (*callback)(void * mem, void * arg), void * arg) {
  x_status status = xs_success;
  volatile o4f_memory_chunk cursor;
  volatile o4f_memory_chunk next;
  
  status = x_mem_lock(timeout);
  if (status != xs_success) {
    return status;
  }

  /*
  ** We loop over the chunks. We precalculate the next chunk before we call
  ** the callback for safety reasons.  (It is allowed for the callback function
  ** to release the chunk, for instance).
  */
  
  for (cursor = memory_sentinel->next; cursor != memory_sentinel; cursor = next) {
    x_chunk_status chunk_status = chunk_sanity_check(cursor);
    if (chunk_status) {
      o4f_abort(O4F_ABORT_MEMCHUNK, chunk_status_text[chunk_status], chunk_status);
    }

    next = cursor->next;
    if (!callback(chunk2mem(cursor), arg)) {
      break;
    }
  }

  status = x_mem_unlock();

  return status;
}

x_status x_mem_scan(x_sleep timeout, x_word tag, x_boolean (*callback)(void * mem, void * arg), void * arg) {
  x_status status = xs_success;
  volatile o4f_memory_chunk cursor;
  volatile o4f_memory_chunk next;
  
  status = x_mem_lock(timeout);
  if (status != xs_success) {
    return status;
  }

  /*
  ** We loop over the chunks. We precalculate the next chunk before we call
  ** the callback for safety reasons.  (It is allowed for the callback function
  ** to release the chunk, for instance).
  */
  
  for (cursor = memory_sentinel->next; cursor != memory_sentinel; cursor = next) {
    x_chunk_status chunk_status = chunk_sanity_check(cursor);
    if (chunk_status) {
      o4f_abort(O4F_ABORT_MEMCHUNK, chunk_status_text[chunk_status], chunk_status);
    }

    next = cursor->next;
    if (cursor->id & tag) {
      if (!callback(chunk2mem(cursor), arg)) {
        break;
      }
    }
  }

  status = x_mem_unlock();

  return status;
}

x_status x_mem_tag_set(void * mem, w_word tag) {
  o4f_memory_chunk chunk = mem2chunk(mem);
  
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
  o4f_memory_chunk chunk = mem2chunk(mem);
  
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
  o4f_memory_chunk chunk = mem2chunk(mem);
  
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
  o4f_memory_chunk chunk = mem2chunk(mem);
  
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
  o4f_memory_chunk chunk = mem2chunk(mem);

  // Really the struct collect_result should be passed as `arg', I'm just being lazy tonight.
  if (isSet(chunk->id, GARBAGE_TAG)) {
    collect_result.collect_bytes += chunk->size;
    collect_result.collect_count += 1;
    x_mem_free(mem);
  }
  return TRUE;
}

void x_mem_discard(void * block) {
  o4f_memory_chunk chunk = mem2chunk(block);

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

