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


#include "wonka.h"
#include "ts-mem.h"
#include "threads.h"
#include "oswald.h"

#include <stdlib.h>
#include <stdio.h>
#include "buffercache.h"
#include "vfs.h"

/* ------------------------------------------------------------------------------------------------------- */

fs_buffer fs_hash[FS_HASH_SIZE];

/* ------------------------------------------------------------------------------------------------------- */

fs_buffer fs_head_free = NULL;
fs_buffer fs_tail_free = NULL;

/* ------------------------------------------------------------------------------------------------------- */

w_word fs_buffer_count = 0;

/* ------------------------------------------------------------------------------------------------------- */

/* fs_search_hash : Search for a buffer on the hash queue, returns NULL if it's not in the hash queue */

fs_buffer fs_search_hash(w_word block, w_word device_id) {

  fs_buffer iter;
  fs_buffer result = NULL;

  iter = fs_hash[block % FS_HASH_SIZE];

  while((iter != NULL) && (result == NULL)) {
    if((iter->device_id == device_id) && (block == iter->block)) result = iter;
    iter = iter->next_hash;
  }
  return result;

}

/* ------------------------------------------------------------------------------------------------------- */

w_void fs_add_to_hash(fs_buffer buffer) {

  buffer->next_hash = fs_hash[buffer->block % FS_HASH_SIZE];
  if(fs_hash[buffer->block % FS_HASH_SIZE] != NULL) fs_hash[buffer->block % FS_HASH_SIZE]->prev_hash = buffer;
  buffer->prev_hash = NULL;
  
  fs_hash[buffer->block % FS_HASH_SIZE] = buffer;
  
}

/* ------------------------------------------------------------------------------------------------------- */

w_void fs_remove_from_hash(fs_buffer buffer) {

  if((buffer->prev_hash != NULL) && (buffer->next_hash != NULL)) {
    buffer->prev_hash->next_hash = buffer->next_hash;
    buffer->next_hash->prev_hash = buffer->prev_hash;
  } else { 
    if(buffer->prev_hash == NULL) { /* First on the list */
      fs_hash[buffer->block % FS_HASH_SIZE] = buffer->next_hash;
      fs_hash[buffer->block % FS_HASH_SIZE]->prev_hash = NULL;
    } else {                        /* Last on the list */
      buffer->prev_hash->next_hash = NULL;
    }
  }
}

/* ------------------------------------------------------------------------------------------------------- */

/* fs_allocate_buffer : Allocates a buffer. First it looks in the hash table. If the buffer is not there 
 *                      and we have not reached FS_MAX_BUFFERS, a new buffer will be created. If we can't
 *                      create a new one, the first buffer from the free list will be the lucky one.
 *                      If the buffer is in use, we have to wait for it to become available. If the buffer 
 *                      from the free list is dirty, it will be written to the device before it becomes 
 *                      available. The buffer will be locked. After allocating, the buffer will move to
 *                      the last entry of the free list.
 */

fs_buffer fs_allocate_buffer(w_word block, w_word size, w_device disk_device) {

  fs_buffer result;
  w_word    device_id = 0;
  w_ubyte   i;
  
  for(i=0; i < strlen(disk_device->name); i++) {
    device_id += disk_device->name[i];
  }

  result = fs_search_hash(block, device_id);

  if(result != NULL) { 
    // x_sem_get(&result->lock, x_eternal);                   /* Lock the buffer */
  } else {
    if(fs_buffer_count < FS_MAX_BUFFERS) {      /* Still room, make a new one */
      
      result = fs_create_buffer(size);          /* Create the buffer */
      // x_sem_get(&result->lock, x_eternal);                 /* Lock it */
      
    } else {                                    /* No room, take one from the free list */
      
      result = fs_head_free;                    /* Take the first of the free list */
      // x_sem_get(&result->lock, x_eternal);                 /* Lock it */
      
      fs_head_free = fs_head_free->next_free;   /* Remove the first entry of the free list */
      fs_head_free->prev_free = NULL;

      result->prev_free = fs_tail_free;         /* And add it to the tail */
      fs_tail_free->next_free = result;
      result->next_free = NULL;
      fs_tail_free = result;

      if((result->status & FS_BUFFER_DIRTY) == FS_BUFFER_DIRTY)
        deviceBRWrite(result->disk_device, result->data, (w_int)(result->block * (result->size / 512)), (w_int)result->size, x_eternal);
                                                /* If the buffer is dirty, write it */
      fs_remove_from_hash(result);
      if(size != result->size) wabort(ABORT_WONKA, "Buffer sizes differ -> BIIIG PROBLEM\n");
    }

    result->block =  block;
    result->device_id = device_id;
    result->disk_device = disk_device;
    result->status = 0;
    fs_add_to_hash(result);

    deviceBRRead(result->disk_device, result->data, (w_int)(result->block * (result->size / 512)), (w_int)result->size, x_eternal);
  }

  return result;

}

/* ------------------------------------------------------------------------------------------------------- */

w_void fs_write_all_buffers() {
  fs_buffer iter = fs_head_free;
  int count = 0;
  woempa(6, "Writing all buffers to device\n");
  while(iter != NULL) {
    if((iter->status & FS_BUFFER_DIRTY) == FS_BUFFER_DIRTY) {
      deviceBRWrite(iter->disk_device, iter->data, (w_int)(iter->block * (iter->size / 512)), (w_int)iter->size, x_eternal);

      iter->status = 0;
      count++;
    }
    iter = iter->next_free;
  }
  woempa(7, "Done writing buffers to device (%d/%d)\n", count, fs_buffer_count);
}
                                    
/* ------------------------------------------------------------------------------------------------------- */

/* fs_release_buffer : Release a buffer -> Remove the lock, put on the last entry of the list */

w_void fs_release_buffer(fs_buffer buffer, w_word status) {

  buffer->status |= status;

  /* OLA: Move it to the tail of the free buffers list 
  
  buffer->prev_free->next_free = buffer->next_free;
  buffer->next_free->prev_free = buffer->prev_free;
  fs_tail_free->next_free = buffer;
  buffer->prev_free = fs_tail_free;
  buffer->next_free = NULL;

  fs_tail_free = buffer;

  */
  
  // x_sem_put(&buffer->lock);
  
}

/* ------------------------------------------------------------------------------------------------------- */

/* fs_create_buffer : Allocates memory for a new buffer and puts it on the tail of the free list queue */

fs_buffer fs_create_buffer(w_word size) {

  fs_buffer new_buffer;

  new_buffer = allocMem(sizeof(fs_Buffer));
  if (new_buffer) {
    memset(new_buffer, 0, sizeof(fs_Buffer));
    new_buffer->data = allocMem(size);

    new_buffer->prev_free = fs_tail_free;

    if(fs_tail_free != NULL) fs_tail_free->next_free = new_buffer;
  
    fs_tail_free = new_buffer;

    if(fs_head_free == NULL) fs_head_free = new_buffer;

    new_buffer->size = size;

  // x_sem_create(&new_buffer->lock, 1);

    fs_buffer_count++;
    if((fs_buffer_count % 100) == 0) woempa(6, "buffers = %d\n", fs_buffer_count);
  }

  return new_buffer;

}

