/**************************************************************************
* Copyright (c) 2007, 2009, 2021, 2023 by KIFFER Ltd. All rights reserved.*
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

#include "core-classes.h"
#include "exception.h"
#include "fields.h"
#include "vfs.h"
#include "vfs_fcntl.h"
#include "wstrings.h"

w_int FileInputStream_read
  (w_thread thread, w_instance thisFileInputStream) {
  w_instance     fdObj;
  w_int fd;
  w_int        result = -1;

  fdObj = getReferenceField(thisFileInputStream, F_FileInputStream_fd);
  fd = getIntegerField(fdObj, F_FileDescriptor_fd);
  
  if (fd < 0) {
    throwIOException(thread);
// TODO  } else if (vfs_eof(fd)) {
// TODO    return -1;
  } else {
    // TODO detect EOF
    w_ubyte minibuf[1];
    vfs_read(fd, minibuf, 1);
    result = minibuf[0];
  }

  return result;
}

w_int FileInputStream_readIntoBuffer
  (w_thread thread, w_instance thisFileInputStream, w_instance buffer, w_int offset, w_int length) {
  w_instance     fdObj;
  w_int    fd;
  w_int        result = -1;
  w_byte       *bytes;
  w_byte       *data;

  if(!buffer) {
    throwNullPointerException(thread);
    return -1;
  }
  
  if(offset < 0 || length < 0 || offset > instance2Array_length(buffer) - length) {
    throwArrayIndexOutOfBoundsException(thread);
    return -1;
  }

  if(length == 0) {
    return 0;
  }

  bytes = instance2Array_byte(buffer);
  fdObj = getReferenceField(thisFileInputStream, F_FileInputStream_fd);
  fd = getIntegerField(fdObj, F_FileDescriptor_fd);
  
  if(fd < 0) {
    throwIOException(thread);
// TODO  } else if (vfs_eof(fd)) {
// TODO    return -1;
  } else {
    data = bytes + offset;
    result = vfs_read(fd, data, length);
    woempa(1, "vfs_read(%d, %p, %d) returned %d\n", fd, data, length, result);
  }

  return result;
}

w_long FileInputStream_skip
  (w_thread thread, w_instance thisFileInputStream, w_long n) {

  w_instance fdObj;
  w_int    fd;
  w_long       result = n;
  w_long       prev_pos;

  fdObj = getReferenceField(thisFileInputStream, F_FileInputStream_fd);
  fd = getIntegerField(fdObj, F_FileDescriptor_fd);
  
  if(fd < 0) {
    throwIOException(thread);
    result = 0;
  } else {
    prev_pos = vfs_ftell(fd);
    result = vfs_lseek(fd, (long)n, SEEK_CUR);

    if(result != (long)-1) {
      result = vfs_ftell(fd) - prev_pos;
    } else {
      throwIOException(thread);
      result = 0; 
    }
  }

  return result;
}

w_int FileInputStream_available
  (w_thread thread, w_instance thisFileInputStream) {

  w_instance         fdObj;
  w_int fd;
  w_int            result;
  const char      *filename;
  fdObj = getReferenceField(thisFileInputStream, F_FileInputStream_fd);
  fd = getIntegerField(fdObj, F_FileDescriptor_fd);
  woempa(1, "fd = %d\n", fd);
  
  if(fd < 0) {
    woempa(1, "fd < 0, gettin outa here\n");
    throwIOException(thread);
    result = 0;
  } else {
    size_t flen = vfs_get_length(fd);
    woempa(1, "file length = %d errno = %d\n", flen, errno);
    if (flen > 0 || errno == 0) {
      result = flen;
    } else {
      throwIOException(thread);
      result = 0;
    }
  }

  return result;  
}

void FileInputStream_close
  (w_thread thread, w_instance thisFileInputStream) {

  w_instance     fdObj;
  w_int fd;

  fdObj = getReferenceField(thisFileInputStream, F_FileInputStream_fd);

  if (fdObj) {
    fd = getIntegerField(fdObj, F_FileDescriptor_fd);
  
    if(fd >= 0) {
      woempa(9, "Calling vfs_close\n");
      vfs_close(fd);
      setIntegerField(fdObj, F_FileDescriptor_fd, -1);
    }
    setReferenceField(thisFileInputStream, NULL, F_FileInputStream_fd);
  }
}

