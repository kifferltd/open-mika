/**************************************************************************
* Copyright (c) 2007, 2009, 2021, 2022, 2023 by KIFFER Ltd.               *
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
e                                                                         *
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

#include "core-classes.h"
#include "mika_threads.h"
#include "vfs.h"
#include "wonka.h"
#include "wstrings.h"

/*
** Open a file and return the resulting fd. The path string must be an absolute path and 'modenum' 
** must be one of the following:
** 0 -> Java MODE_READ   -> open(2) flags VFS_O_RDONLY
** 1 -> Java MODE_WRITE  -> open(2) flags VFS_O_WRONLY
** 2 -> Java MODE_APPEND -> open(2) flags VFS_O_WRONLY|VFS_O_APPEND
*/
w_int FileDescriptor_createFromPath(w_thread thread, w_instance thisFileDescriptor, w_instance pathString, w_int modenum) {
  w_string path_string = String2string(pathString);
  w_int path_length;
  w_ubyte *path = w_string2UTF8(path_string, &path_length);
  w_word flags = modenum == 0 ? VFS_O_RDONLY : modenum == 1 ? VFS_O_WRONLY : modenum == 2 ? (VFS_O_WRONLY|VFS_O_APPEND) : 0xffffffff;
  w_int fd;

  woempa(7, "flags = %d\n", flags);
  if (flags == 0xffffffff) {
    throwException(thread, clazzInternalError, NULL);
  }

  fd = vfs_open(path, flags, 0);
  woempa(7, "fd = %d\n", fd);
  if (fd < 0) {
    throwException(thread, clazzFileNotFoundException, "could not open file '%s' in mode %d using flags '%x': %s\n", path, modenum, flags, strerror(errno));
  }
  releaseMem(path);

  return fd;
}

void FileDescriptor_sync
  (w_thread thread, jobject theClass) {

  // TODO
  woempa(9, "sync !");
	  
}

