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
** $Id: memory.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include "oswald.h"
#include <stdlib.h>
#include <malloc.h>

x_monitor		memory_monitor;
static x_Memory_Chunk	Memory_Sentinel;
static x_memory_chunk	memory_sentinel;

const char *magic = "This memory is valid.";

x_int		heap_claims;
x_size		heap_size; 
x_size		heap_remaining;
x_mutex		heap_mutex;
x_thread	heap_owner;

static void *chunk2mem(x_memory_chunk chunk) {
	return ((char*)chunk) + sizeof(x_Memory_Chunk);
}

static x_memory_chunk mem2chunk(void *mem) {
	return (x_memory_chunk)(((char*)mem) - sizeof(x_Memory_Chunk));
}

x_status x_mem_lock(x_sleep timeout) {
	return x_monitor_enter(memory_monitor, timeout);
}

x_status x_mem_unlock() {
	return x_monitor_exit(memory_monitor);
}

void x_mem_init(x_ubyte *start) {
	// We were given some memory to play with, but we won't use it ...
	free(start);
	memory_sentinel = &Memory_Sentinel;
	list_init(memory_sentinel);
	memory_sentinel->id = 0x31337;
	memory_sentinel->file = __FILE__;
	memory_sentinel->line = __LINE__;
	memory_sentinel->size = 0;
	memory_sentinel->check = (char*)magic;

	memory_monitor = calloc(1, sizeof(x_Monitor));
	x_monitor_create(memory_monitor);
}

void *_x_mem_alloc(x_size size, const char *file, x_int line) {
	
	void * newMem = NULL;

	loempa(7,"Allocating some memory\n");
	if (size > MAX_SINGLE_ALLOC) {
		loempa(9,"  +->Attempt to allocate %d bytes, maximum is %d!\n", size, MAX_SINGLE_ALLOC);
		return NULL;
	}
	else if (size > heap_remaining) {
		loempa(9,"  +->Attempt to allocate %d bytes, available space is %d!\n", size, heap_remaining);
		return NULL;
	}
	else {
		newMem = calloc((size + 7 + sizeof(x_Memory_Chunk)) / 8, 8);
	}

	if (newMem) {
		x_memory_chunk chunk = (x_memory_chunk) newMem;

		chunk->file = (char*)file;
		chunk->line = line;
		chunk->size = size;
		chunk->check = (char*)magic;
		loempa(5,"  +->Allocated %d bytes at %p\n", size, newMem);
		
		x_mem_lock(x_eternal);
		list_insert(memory_sentinel, chunk);
		heap_remaining -= size + sizeof(x_memory_chunk);
		loempa(5,"  +->Heap remaining: %d bytes\n", heap_remaining);
		x_mem_unlock();

		return chunk2mem(chunk);
	}
	else {
		loempa(9,"  +->Out of memory!  heap_remaining was %d, setting it to 0\n", heap_remaining);
		heap_remaining = 0;
	}

	return NULL;
}

void *_x_mem_calloc(x_size size, const char *file, int line) {
	void *newCalloc = NULL;

	loempa(7,"Clearing and allocating some memory\n");
	if (size > MAX_SINGLE_ALLOC) {
		loempa(9,"  +->Attempt to allocate %d bytes, maximum is %d!\n", size, MAX_SINGLE_ALLOC);
		return NULL;
	}
	else if (size > heap_remaining) {
		loempa(9,"  +->Attempt to allocate %d bytes, available space is %d!\n", size, heap_remaining);
		return NULL;
	}
	else {
		x_size calloc_size = (size + sizeof(x_Memory_Chunk) + 7) / 8;
		newCalloc = calloc(calloc_size, 8);
	}

	if (newCalloc) {
		x_memory_chunk chunk = (x_memory_chunk)newCalloc;

		chunk->file = (char*)file;
		chunk->line = line;
		chunk->size = size;
		chunk->check = (char*)magic;

		loempa(5,"  +->Allocated %d bytes at %p\n", size, newCalloc);

		x_mem_lock(x_eternal);
		list_insert(memory_sentinel, chunk);
		heap_remaining -= size + sizeof(x_Memory_Chunk);
		loempa(5,"  +->Heap remaining: %d bytes\n", heap_remaining);
		x_mem_unlock();

		return chunk2mem(chunk);
	}
	else {
		loempa(9,"  +->Out of memory!  heap_remaining was %d, setting it to 0\n", heap_remaining);
		heap_remaining = 0;
	}

	return NULL;
}

void *x_mem_realloc(void *old, x_size size) {
	x_memory_chunk oldchunk = mem2chunk(old);
	x_memory_chunk newchunk;
	void *newReAlloc;
	
	loempa(7, "ReAllocating some memory\n");

	if (oldchunk->check != magic) {
		loempa(9,"  +->Memory block %p is not valid!\n", old);
		return NULL;
	}

	if (size <= oldchunk->size) {
		x_mem_lock(x_eternal);
		heap_remaining += oldchunk->size - size;
		oldchunk->size = size;
		x_mem_unlock();
		return old;
	}

	newReAlloc = x_mem_alloc(size);
	newchunk = mem2chunk(newReAlloc);
	loempa(5, "  +->New chunk is at %p, copying %d bytes from %p to %p\n", newchunk, oldchunk->size, old, newReAlloc); 
	memcpy(newReAlloc, old, oldchunk->size);
	loempa(5, "  +->Marking chunk %p with id 0x%x\n", newReAlloc, oldchunk->id);
	x_mem_tag_set(newReAlloc, oldchunk->id);
	x_mem_free(old);

	return newReAlloc;
}

void x_mem_free(void *block) {
	x_memory_chunk chunk = mem2chunk(block);
	
	loempa(7, "Freeing some memory\n");
	if (chunk->check != magic) {
		loempa(9, "  +->Memory block %p is not valid!\n", block);
	}

	loempa(5,"  +->Returning %d bytes at %p allocated\n", chunk->size, block);
	x_mem_lock(x_eternal);
	list_remove(chunk);
	heap_remaining += chunk->size + sizeof(x_Memory_Chunk);
	loempa(5,"  +->Heap remaining: %d bytes\n", heap_remaining);
	x_mem_unlock();

	free(chunk);
}

x_size x_mem_avail(void) {
	return heap_remaining;
}

x_status x_mem_walk(x_sleep timeout, void (*callback)(void * mem, void * arg), void * arg) {
	x_status status = xs_success;
	x_memory_chunk cursor;
	x_memory_chunk next;
  
	loempa(7, "Walking over some memory\n");

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
		next = cursor->next;
		callback(chunk2mem(cursor), arg);
	}

	status = x_mem_unlock();

	return status;
}

x_status x_mem_tag_set(void * mem, x_word tag) {
	x_memory_chunk chunk = mem2chunk(mem);
	
	loempa(7, "Setting a tag\n");
	if (chunk->check != magic) {
		loempa(9,"  +->Memory block %p is not valid!\n", mem);
		return xs_unknown;
	}

	loempa(5, "  +->Marking chunk %p with id 0x%x\n", mem, tag);
	chunk->id = tag;

	return xs_success;
}

x_word x_mem_tag_get(void * mem) {
	x_memory_chunk chunk = mem2chunk(mem);
	
	loempa(7, "Getting a tag\n");
	if (chunk->check != magic) {
		loempa(9,"Memory block %p is not valid!\n", mem);
		return 0;
	}
	loempa(5,"  +->Chunk %p has id 0x%x\n", mem, chunk->id);
	return chunk->id;
}

x_size x_mem_size(void * mem) {
	x_memory_chunk chunk = mem2chunk(mem);
	
	loempa(7, "Getting the memory size\n");
	if (chunk->check != magic) {
		loempa(9,"Memory block %p is not valid!\n", mem);
		return 0;
	}

	loempa(5,"  +->Chunk %p has size %d\n", mem, chunk->size);
	return chunk->size;
}

x_boolean x_mem_is_block(void * mem) {
	x_memory_chunk chunk = mem2chunk(mem);
	return chunk->check == magic;
}

static void discard_callback(void * mem, void * arg) {
	x_memory_chunk chunk = mem2chunk(mem);

	// Really the struct collect_result should be passed as `arg', I'm just being lazy tonight.
	if (isSet(chunk->id, GARBAGE_TAG)) {
		collect_result.collect_bytes += chunk->size;
		collect_result.collect_count += 1;
		x_mem_free(mem);
	}
}

void x_mem_discard(void * block) {
	x_memory_chunk chunk = mem2chunk(block);
	setFlag(chunk->id, GARBAGE_TAG);
}

x_status x_mem_collect(x_size * bytes, x_size * num) {
	x_status status = x_mem_walk(x_eternal, discard_callback, &collect_result);
	if (status == xs_success && *bytes) {
		*bytes = collect_result.collect_bytes;
	}
	if (status == xs_success && *num) {
		*num = collect_result.collect_count;
	}
	return status;
}

x_size x_mem_total(void) { 
	return heap_size; 
}

void * x_alloc_static_mem(void * memory, x_size size) {
	return calloc((size+7)/8, 8);
}
