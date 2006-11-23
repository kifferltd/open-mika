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



#ifndef _BUFFERCACHE_H
#define _BUFFERCACHE_H

#include "driver_blockrandom.h"

#define FS_HASH_SIZE    64
#define FS_MAX_BUFFERS  1024

#define FS_BUFFER_DIRTY  0x01

/* ------------------------------------------------------------------------------------------------------- */

typedef struct fs_Buffer {

  w_ubyte           *data;
  w_word            size;

  w_word            block;
  w_word            device_id;

  w_device        disk_device;

  x_Mutex           lock;

  w_word            status;

  struct fs_Buffer  *prev_hash;
  struct fs_Buffer  *next_hash;
  
  struct fs_Buffer  *prev_free;
  struct fs_Buffer  *next_free;

} fs_Buffer;

typedef fs_Buffer  *fs_buffer;

/* ------------------------------------------------------------------------------------------------------- */

extern fs_buffer fs_search_hash(w_word block, w_word device);
extern w_void fs_add_to_hash(fs_buffer buffer);
extern w_void fs_remove_from_hash(fs_buffer buffer);
extern fs_buffer fs_allocate_buffer(w_word block, w_word size, w_device disk_device);
extern w_void fs_release_buffer(fs_buffer buffer, w_word status);
extern fs_buffer fs_create_buffer(w_word size);
extern w_void fs_write_all_buffers(void);

#endif
