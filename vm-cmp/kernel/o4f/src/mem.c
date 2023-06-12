/**************************************************************************
* Copyright (c) 2010, 2018, 2021, 2023 by KIFFER Ltd.                     *
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

static w_int heap_remaining_check_count;
#define HEAP_REMAINING_CHECK_DIVISOR 10000
#define HEAP_OVERHEAD (sizeof(o4f_Memory_Chunk) + 16)

static inline void check_heap_remaining() {
  if ((++heap_remaining_check_count % HEAP_REMAINING_CHECK_DIVISOR) == 0) {
    w_int estimated_heap_remaining = heap_remaining;
    heap_remaining = FreeRTOS_heap_remaining;
    woempa(7, "estimated heap_remaining = %d, actual = %d\n", estimated_heap_remaining, heap_remaining);
  }
}

static inline void reduce_heap_remaining(w_int consumed) {
  heap_remaining -= HEAP_OVERHEAD + consumed;
  check_heap_remaining();
}

static inline void increase_heap_remaining(w_int released) {
  heap_remaining += HEAP_OVERHEAD + released;
  check_heap_remaining();
}

static inline void adjust_heap_remaining(w_int diff) {
  heap_remaining += diff;
  check_heap_remaining();
}

SemaphoreHandle_t memoryMutex;
static StaticSemaphore_t memoryMutex_storage;

#ifdef DEBUG
int32_t x_mem_lock_depth = 0;
#endif

inline x_status x_mem_lock(x_sleep timeout) {
  if (o4fe->status == O4F_ENV_STATUS_NORMAL) {
    switch(xSemaphoreTakeRecursive(memoryMutex, timeout == x_eternal ? portMAX_DELAY : timeout)) {
      case pdPASS:
#ifdef DEBUG
        ++x_mem_lock_depth;
#endif
        return xs_success;
      default :
        o4f_abort(O4F_ABORT_THREAD, "x_mem_unlock() failed!\n", 0);
        return xs_unknown;
    }
  }
}

inline x_status x_mem_unlock() {
#ifdef DEBUG
  if (--x_mem_lock_depth < 0) {
    o4f_abort(O4F_ABORT_THREAD, "Too many x_mem_unlock()!\n", x_mem_lock_depth);
  }
  if (xSemaphoreGetMutexHolder(memoryMutex) != xTaskGetCurrentTaskHandle()) {
    o4f_abort(O4F_ABORT_THREAD, "Calling x_mem_unlock() when not mutex owner!\n", 0);
  }
#endif
  if (o4fe->status == O4F_ENV_STATUS_NORMAL) {
    int status = xSemaphoreGiveRecursive(memoryMutex);
    switch (status) {
      case pdPASS:
        return xs_success;
      case pdFAIL:
#ifdef DEBUG
        o4f_abort(O4F_ABORT_THREAD, "x_mem_unlock() when not lock owner\n", status);
#endif
        return xs_not_owner;
      default :   
#ifdef DEBUG
        o4f_abort(O4F_ABORT_THREAD, "x_mem_unlock() when not lock owner\n", status);
#endif
        return xs_unknown;
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

  memoryMutex = xSemaphoreCreateRecursiveMutexStatic(&memoryMutex_storage);
}

#ifdef DEBUG
void *_x_mem_alloc(w_size size, const char *file, int line) {
  o4f_memory_chunk newchunk;

  if (!memory_sentinel) {
    loempa(9, "%s:%d Attempt to allocate memory before x_mem_init() has been called!\n", file, line);
return NULL;
  }
    
  x_mem_lock(x_eternal);
  newchunk = pvPortMalloc(sizeof(o4f_Memory_Chunk) + size);

  if (!newchunk) {
    x_mem_unlock();
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
  x_list_insert(memory_sentinel, newchunk);
  loempa(1,"heap_remaining was %d\n", heap_remaining);
  reduce_heap_remaining(size);
  x_mem_unlock();
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

void *_x_mem_calloc(w_size size, const char *file, int line) {
  o4f_memory_chunk newchunk;

  if (!memory_sentinel) {
    loempa(9, "%s:%d Attempt to allocate memory before x_mem_init() has been called!\n", file, line);

    return NULL;
  }
    
  x_mem_lock(x_eternal);
  newchunk = pvPortMalloc(sizeof(o4f_Memory_Chunk) + size);

  if (!newchunk) {
    x_mem_unlock();
    loempa(9,"%s:%d Out of memory!  heap_remaining was %d, setting it to 0\n", file, line, heap_remaining);
    heap_remaining = 0;

    return NULL;

  }

  memset(newchunk, 0, sizeof(o4f_Memory_Chunk) + size);
  newchunk->file = (char*)file;
  newchunk->line = line;
  newchunk->size = size;
  newchunk->check = (char*)magic;
  loempa(1,"%s:%d Allocated %d bytes at %p\n", newchunk->file, newchunk->line, size, newchunk);
  x_list_insert(memory_sentinel, newchunk);
  reduce_heap_remaining(size);
  x_mem_unlock();
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

void *_x_mem_realloc(void *old, w_size size, const char *file, int line) {
  o4f_memory_chunk oldchunk = mem2chunk(old);
  o4f_memory_chunk newchunk;
  w_size oldsize;
  w_size newsize;

  if (!memory_sentinel) {
    loempa(9, "%s:%d Attempt to allocate memory before x_mem_init() has been called!\n", file, line);

    return NULL;
  }
    
  if (oldchunk->check != magic) {
    loempa(9,"Memory block %p is not valid!\n", old);

    return NULL;

  }

  x_mem_lock(x_eternal);
  x_list_remove(oldchunk);
  oldsize = oldchunk->size + sizeof(o4f_Memory_Chunk);
  newsize = size + sizeof(o4f_Memory_Chunk);
  newchunk = pvPortMalloc(newsize);
  if (!newchunk) {
    x_mem_unlock();
    vPortFree(oldchunk);
    heap_remaining = 0;

    return NULL;
  }

  memcpy(newchunk, oldchunk, newsize);
  newchunk->file = (char*)file;
  newchunk->line = line;
  newchunk->size = size;
  x_list_insert(memory_sentinel, newchunk);
  adjust_heap_remaining(oldsize - newsize);
  x_mem_unlock();
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

#else

void *_x_mem_alloc(w_size size) {
  o4f_memory_chunk newchunk;

  x_mem_lock(x_eternal);
  newchunk = pvPortMalloc(sizeof(o4f_Memory_Chunk) + size);

  if (!newchunk) {
    x_mem_unlock();
    heap_remaining = 0;

    return NULL;

  }

  newchunk->id = 0;
  newchunk->size = size;
  x_list_insert(memory_sentinel, newchunk);

  reduce_heap_remaining(size);
  x_mem_unlock();
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

void *_x_mem_calloc(w_size size) {
  o4f_memory_chunk newchunk;

  x_mem_lock(x_eternal);
  newchunk = pvPortMalloc(sizeof(o4f_Memory_Chunk) + size);

  if (!newchunk) {
    x_mem_unlock();
    heap_remaining = 0;

    return NULL;

  }

  memset(newchunk, 0, sizeof(o4f_Memory_Chunk) + size);
  newchunk->size = size;
  x_list_insert(memory_sentinel, newchunk);

  reduce_heap_remaining(size);
  x_mem_unlock();
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  return chunk2mem(newchunk);
}

void *_x_mem_realloc(void *old, w_size size) {
  o4f_memory_chunk oldchunk = mem2chunk(old);
  o4f_memory_chunk newchunk;
  w_size oldsize;
  w_size newsize;

  x_mem_lock(x_eternal);
  x_list_remove(oldchunk);
  oldsize = oldchunk->size + sizeof(o4f_Memory_Chunk);
  newsize = size + sizeof(o4f_Memory_Chunk);
  newchunk = pvPortMalloc(newsize);
  if (!newchunk) {
    heap_remaining = 0;
    vPortFree(oldchunk);
    x_mem_unlock();

    return NULL;

  }

  memcpy(newchunk, oldchunk, newsize);
  newchunk->size = size;
  x_list_insert(memory_sentinel, newchunk);

  adjust_heap_remaining(oldsize - newsize);
  x_mem_unlock();
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
  increase_heap_remaining(chunk->size);
  loempa(1,"Heap remaining: %d bytes\n", heap_remaining);

  vPortFree(chunk);
  x_mem_unlock();
}

w_size x_mem_avail(void) {
  x_mem_lock(x_eternal);
  w_size result = heap_remaining;
  x_mem_unlock();
  return result;
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
w_size x_mem_heap_start() {
  return _sys_mem_staticSize();
}

w_size x_mem_heap_end() {
  return _sys_mem_totalSize();
}


