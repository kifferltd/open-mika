/**************************************************************************
* Copyright (c) 2020, 2021 by KIFFER Ltd. All rights reserved.            *
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

#ifdef FREERTOS
#include <ff_stdio.h>
#endif

#include "core-classes.h"
#include "exception.h"
#include "fields.h"
#include "loading.h"
#include "vfs.h"
#include "wstrings.h"

static w_int RAF2FD(w_instance thisRAF) {
  w_instance fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  return getWotsitField(fd_obj, F_FileDescriptor_fd);
}

w_int RandomAccessFile_createFromString (w_thread thread, w_instance thisRAF, w_instance path, w_int mode) {
  w_string pathname_string;
  char *pathname;
  const char  *fmode = "r+";
  int         openmode;
  int         fdesc;
  w_int fd;
  w_instance  fd_obj;
  struct vfs_stat_t statbuf;
  int rc;

  if (mustBeInitialized(clazzFileDescriptor) == CLASS_LOADING_FAILED) {
    return 0;
  }

  pathname_string = String2string(path);
  pathname = (char*)w_string2UTF8(pathname_string, NULL);	  
  switch(mode) {
    case 0:
      openmode = VFS_O_RDONLY;
      fmode = "r";
      break;
    case 1:
      openmode = VFS_O_RDWR | VFS_O_CREAT;
      break;
    case 2:
      openmode = VFS_O_RDWR | VFS_O_CREAT | VFS_O_DIRECT;
      break;
    case 3:
      openmode = VFS_O_RDWR | VFS_O_CREAT | VFS_O_SYNC;
      break;
    default:
     openmode = 0;
  }

  rc = vfs_stat(pathname, &statbuf);

  if (rc) {
    woempa(9, "vfs_stat(%s, %p) returned %d\n", pathname, &statbuf, rc);
    return 1;
  }

  if (!VFS_S_ISREG(statbuf.st_mode)) {
    woempa(9, "vfs_stat(%s, %p) did not report a regular file\n", pathname, &statbuf);
    return 1;
  }

  fd = vfs_open(pathname, openmode, VFS_S_IRUSR | VFS_S_IWUSR | VFS_S_IRGRP | VFS_S_IROTH);

  releaseMem(pathname);

  if(fd == -1) {
    return 1;
  } 

  if (fd < 0) {
    return 1;
  } else {
    enterUnsafeRegion(thread);
    fd_obj = allocInstance(thread, clazzFileDescriptor);
    enterSafeRegion(thread);
    if(fd_obj != NULL) {
      setReferenceField(thisRAF, fd_obj, F_RandomAccessFile_fd);
      setIntegerField(fd_obj, F_FileDescriptor_fd, fd);
    }
  }
  return 0;
}

w_int RandomAccessFile_read (w_thread thread, w_instance thisRAF) {
  w_int fd;;
  w_int       result = -1;
  w_sbyte     minibuf;

  fd = RAF2FD(thisRAF);
  
  if(fd < 0) {
    throwNullPointerException(thread);
  } else {
    if (vfs_read(fd, &minibuf, 1) > 0) {
      result = minibuf;
    }
  }

  return result;
}

w_int RandomAccessFile_readIntoBuffer (w_thread thread, w_instance thisRAF, w_instance buffer, w_int offset, w_int length) {

  w_int fd;;
  w_int       result;
  w_sbyte     *bytes;
  w_sbyte     *data;

  if ((offset < 0) || (offset > instance2Array_length(buffer) - length)) {
    throwArrayIndexOutOfBoundsException(thread);

    return -1;
  }

  bytes = instance2Array_byte(buffer);
  fd = RAF2FD(thisRAF);
  
  if(fd < 0) {
    throwNullPointerException(thread);
    result = 0;
  } else {
    data = bytes + offset;
    result = vfs_read(fd, data, length);
    if(result == 0) result = -1;
  }

  return result;
}

w_int RandomAccessFile_skipBytes (w_thread thread, w_instance thisRAF, w_int n) {
  w_instance  fd_obj;
  w_int fd;
  w_long       result = 0;
  w_long       prev_pos;

  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  fd = getIntegerField(fd_obj, F_FileDescriptor_fd);
  
  if (fd < 0) {
    throwIOException(thread);
  } else {
    struct vfs_stat_t statbuf;
    w_long size = (w_long)vfs_get_length(fd);

    prev_pos = vfs_ftell(fd);
    if (prev_pos < 0) {
      throwIOException(thread);
      return -1;
    }

    if(n > (size - prev_pos)) {
      n = size - prev_pos;
    }
     
    result = vfs_lseek(fd, (long)n, SEEK_CUR);
    if (result < 0) {
      throwIOException(thread);
      return -1;
    }

    result = vfs_ftell(fd);
    if (result < 0) {
      throwIOException(thread);
    }
  }

  return result;
}

void RandomAccessFile_write (w_thread thread, w_instance thisRAF, w_int oneByte) {
  w_int fd;
  w_sbyte     minibuf = oneByte;

  fd = RAF2FD(thisRAF);
  
  if(fd < 0) {
    throwNullPointerException(thread);
  } else {
     vfs_lseek(fd, vfs_ftell(fd), SEEK_SET);
    vfs_write(fd, &minibuf, 1);
    vfs_flush(fd);
  }
}

void RandomAccessFile_writeFromBuffer (w_thread thread, w_instance thisRAF, w_instance buffer, w_int offset, w_int length) {
  w_int fd;
  w_sbyte     *bytes;
  w_sbyte     *data;

  if ((offset < 0) || (offset > instance2Array_length(buffer) - length)) {
    throwArrayIndexOutOfBoundsException(thread);

    return;
  }

  bytes = instance2Array_byte(buffer);
  fd = RAF2FD(thisRAF);
  
  if(fd < 0) {
    throwNullPointerException(thread);
  } else {
    data = bytes + offset;
    vfs_write(fd, data, length);
    // TODO vfs_flush(fd);
  }
}

w_long RandomAccessFile_getFilePointer (w_thread thread, w_instance thisRAF) {
  w_int fd;
  w_long       result = -1;

  fd = RAF2FD(thisRAF);
  
  if(fd < 0) {
    throwNullPointerException(thread);
    result = 0;
  } else {
    result = vfs_ftell(fd);
  }

  return result;
}

void RandomAccessFile_seek (w_thread thread, w_instance thisRAF, w_long pos) {
  w_int fd;

  fd = RAF2FD(thisRAF);
  
  if(fd < 0) {
    throwNullPointerException(thread);
  } else {
    if(vfs_lseek(fd, (long)pos, SEEK_SET) == -1) {
      throwIOException(thread);
    }
  }
}

w_long RandomAccessFile_length (w_thread thread, w_instance thisRAF) {
  struct vfs_stat_t statbuf;
  w_long result = 0;

    // FIXME this should use the vfs_ abstraction
  w_int fd = RAF2FD(thisRAF);
  result = (w_long)vfs_get_length(fd);

  return result;
}

void RandomAccessFile_setLength (w_thread thread, w_instance thisRAF, w_long newlen) {

  w_instance  fd_obj;
  w_int fd;

  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  fd = getIntegerField(fd_obj, F_FileDescriptor_fd);
  
  if(fd == NULL) {
    throwNullPointerException(thread);
  } else {
    char *pathname;
    w_long oldptr;

    oldptr = vfs_ftell(fd);

// URGENT
// TODO    if(vfs_ftruncate(pathname, newlen) == -1) {
// TODO      throwIOException(thread);
// TODO    }

    if(newlen < oldptr && vfs_lseek(fd, newlen, SEEK_SET) == -1) {
      throwIOException(thread);
    }
  }
}

void RandomAccessFile_close (w_thread thread, w_instance thisRAF) { 
  w_instance  fd_obj;
  w_int fd;

  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);

  if(fd < 0LL) {
    throwNullPointerException(thread);
  } else {
    
    fd = getIntegerField(fd_obj, F_FileDescriptor_fd);
  
    if(fd) {  
      vfs_close(fd);
      setIntegerField(fd_obj, F_FileDescriptor_fd, -1);
    }
    
  }
  
}
