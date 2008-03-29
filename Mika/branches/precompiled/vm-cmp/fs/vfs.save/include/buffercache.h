/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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
