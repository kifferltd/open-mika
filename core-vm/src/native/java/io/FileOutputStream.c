/**************************************************************************
* Copyright (c) 2007, 2009, 2021 by KIFFER Ltd. All rights reserved.      *
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

void FileOutputStream_write
  (w_thread thread, w_instance thisFileOutputStream, w_int oneByte) {

  w_instance     fdObj;
  w_int fd;

  fdObj = getReferenceField(thisFileOutputStream, F_FileOutputStream_fd);

  if(!fdObj) {
    throwIOException(thread);
    return;
  }
    
  fd = getIntegerField(fdObj, F_FileDescriptor_fd);
  
  if(fd < 0) {
    throwIOException(thread);
  } else {
    w_ubyte minibuf[1];
    minibuf[0] = (w_ubyte)oneByte;
    vfs_write(fd, minibuf, 1);
  }
}

void FileOutputStream_writeFromBuffer
  (w_thread thread, w_instance thisFileOutputStream, w_instance buffer, w_int offset, w_int length) {

  w_instance     fdObj;
  w_int fd;
  w_byte       *bytes;
  w_byte       *data;

  if(!buffer) {
    throwNullPointerException(thread);
    return;
  }

  if(offset < 0 || length < 0 || offset > instance2Array_length(buffer) - length) {
    throwArrayIndexOutOfBoundsException(thread);
    return;
  }

  if(length == 0) {
    return;
  }

  bytes = instance2Array_byte(buffer);
  fdObj = getReferenceField(thisFileOutputStream, F_FileOutputStream_fd);

  if(!fdObj) {
    throwIOException(thread);
  }
  else {
  
    fd = getIntegerField(fdObj, F_FileDescriptor_fd);

    if(fd == NULL) {
      throwIOException(thread);
    } else {
      data = bytes + offset;
      vfs_write(fd, data, length);
    }

  }

}

void FileOutputStream_close
  (w_thread thread, w_instance thisFileOutputStream) {

  w_instance     fdObj;
  vfs_FILE    *file;

  fdObj = getReferenceField(thisFileOutputStream, F_FileOutputStream_fd);

  woempa(1, "Closing file\n");

  if(fdObj != NULL) {

    woempa(1, "Still have a filedescriptor\n");
    
    file = getWotsitField(fdObj, F_FileDescriptor_fd);

    if(file == NULL) {
      woempa(1, "Filedescriptor is empty\n");
    } else {
      woempa(1, "Calling vfs_close\n");
      vfs_close(file);
      setReferenceField(thisFileOutputStream, NULL, F_FileOutputStream_fd);
    }
  }
}

void FileOutputStream_flush
  (w_thread thread, w_instance thisFileOutputStream) {

  w_instance     fdObj;
  vfs_FILE    *file;

  fdObj = getReferenceField(thisFileOutputStream, F_FileOutputStream_fd);

  if (fdObj) {
    file = getWotsitField(fdObj, F_FileDescriptor_fd);
  
    if(file) {
      vfs_fflush(file);
    }
  } 
}

