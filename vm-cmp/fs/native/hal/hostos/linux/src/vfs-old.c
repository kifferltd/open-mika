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

#include "vfs.h"

char *current_working_dir;
char *current_root_dir;

void init_vfs(void) {
  #ifdef FS_NON_BLOCKING
    woempa(9, "init_vfs  -> Using native filesystem (NON-BLOCKING)\n"); 
  #else
    woempa(9, "init_vfs  -> Using native filesystem (BLOCKING)\n"); 
  #endif
  cwdbuffer = allocClearedMem(MAX_CWD_SIZE);
  current_working_dir = getcwd(cwdbuffer, MAX_CWD_SIZE);
  if (!current_working_dir) {
    woempa(9, "getcwd returned NULL, errono = %d\n", errno);
  }
  current_working_dir = reallocMem(cwdbuffer, strlen(cwdbuffer) + 1);
  current_root_dir = fsroot;
  woempa(9, "current dir  : %s\n", current_working_dir);
  woempa(9, "current root : %s\n", current_root_dir);
}

#ifndef VFS_FILEOPS_DEFINED

int vfs_open(const char *filename, const w_int flags, const w_word mode) {
  return open(filename, flags | O_NONBLOCK, mode);
}

int vfs_read(const int file_desc, w_void *buffer, const w_word count) {
  int retval = read(file_desc, buffer, count);
  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    retval = read(file_desc, buffer, count);
  }
  return retval;  
}

int vfs_write(const int file_desc, const w_void *buffer, const w_word count) {
  int retval = write(file_desc, buffer, count);
  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    x_thread_sleep(50);
    retval = write(file_desc, buffer, count);
  }
  return retval;  
}

#endif

