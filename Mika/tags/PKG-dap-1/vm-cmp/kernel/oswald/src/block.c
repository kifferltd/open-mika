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
** $Id: block.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <oswald.h>

inline static x_size round_up(x_size value, x_size rounding) {
  return (value + (rounding - 1)) & ~(rounding - 1);
}

/*
** Note that we round up block sizes to a word boundary since some processors (ARM) work only optimal
** or some not at all with non word aligned pointers.
*/

x_status x_block_create(x_block block, x_size size, void * space, x_size space_size) {

  x_ubyte * cursor;
  x_ubyte * edge;
  x_boll previous;
  x_boll current;

  size = round_up(size, 4);

  if (size > 0x0000ffff) {
    return xs_bad_argument;
  }
    
  if (size + sizeof(void *) > space_size) {
    return xs_insufficient_memory;
  }

  block->boll_size = size;
  block->space_size = space_size;
  block->space = space;

  edge = (x_ubyte *)space + space_size;
  block->bolls = space;
  block->bolls->header.next = NULL;
  block->bolls_left = 1;
  previous = block->bolls;
  cursor = (x_ubyte *)space + size + sizeof(void *);
  while (cursor <= edge - size - sizeof(void *)) {
    current = (x_boll)cursor;
    previous->header.next = current;
    current->header.next = NULL;
    block->bolls_left += 1;
    cursor += size + sizeof(void *);
    previous = current;
  }

  block->bolls_max = block->bolls_left;

  return x_event_init(&block->Event, xe_block);

}

inline static x_status xi_block_try_allocate(x_block block, const x_boolean decrement_competing, void ** bytes) {

  x_boll boll;

  x_assert(critical_status);

  if (x_event_is_deleted(block)) {
    if (decrement_competing) {
      block->Event.n_competing--;
    }
    return xs_deleted;
  }

  if (x_event_type_bad(block, xe_block)) {
    return xs_bad_element;
  }
  
  if (block->bolls) {
    boll = block->bolls;
    block->bolls = boll->header.next;
    boll->header.block = block;
    *bytes = boll->bytes;
    block->bolls_left -= 1;
    return xs_success;
  }

  return xs_no_instance;
    
}

/*
** Allocate a block of memory of a fixed size. When timeout == 0, this guarantees that the allocation
** happens in fixed time (and fast), when a block is available...
*/

x_status x_block_allocate(x_block block, void ** bytes, x_sleep timeout) {

  x_status status;
  x_thread thread = thread_current;
  
  if (x_in_context_critical(timeout)) { 
    return xs_bad_context;
  }

  x_preemption_disable;

  status = xi_block_try_allocate(block, false, bytes);
  if (status == xs_no_instance) {
    if (timeout) {
      while (timeout && status == xs_no_instance) {
        timeout = x_event_compete_for(thread, &block->Event, timeout);
        status = xi_block_try_allocate(block, true, bytes);
      }
    }
  }
  
  x_preemption_enable;
  
  return status;

}

x_status x_block_release(void * bytes) {

  x_status status;
  x_boll boll;
  x_block block;

  x_preemption_disable;

  boll = (x_boll)((x_ubyte *)bytes - sizeof(void *));
  block = boll->header.block;
  
  if (x_event_is_deleted(block)) {
    status = xs_deleted;
  }
  else if (x_event_type_bad(block, xe_block)) {
    status = xs_bad_element;
  }
  else {
    boll->header.next = block->bolls;
    block->bolls = boll;
    block->bolls_left += 1;
    x_event_signal(&block->Event);
    status = xs_success;
  }

  x_preemption_enable;
  
  return status;

}

x_status x_block_delete(x_block block) {

  x_status status;
  
  x_preemption_disable;
  status = xi_event_destroy(&block->Event);
  x_preemption_enable;

  return status;

}

/*
** Calculate the amount of memory we need to have a certain number of blocks of a certain size.
** Note that a block size will always be rounded up to a word size (4 bytes).
*/

x_size x_block_calc(x_size block_size, x_size num_blocks) {
  return (round_up(block_size, 4) + sizeof(void *)) * num_blocks;
}
