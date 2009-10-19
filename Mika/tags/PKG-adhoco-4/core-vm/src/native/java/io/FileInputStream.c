/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2007, 2009 by Chris Gray, /k/ Embedded Java         *
* Solutions.  All rights reserved.                                        *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#include "core-classes.h"
#include "exception.h"
#include "fields.h"
#include "vfs.h"
#include "vfs_fcntl.h"
#include "wstrings.h"

/*
** Get the file descriptor's name as a C-style UTF8 string. After you've
** finished with the name, call freeFileDescriptorName() on the result to
** free up the memory.
*/
char *getFISFileName(w_instance thisFileInputStream) {
  w_instance fd;
  w_instance pathString;
  w_string path;

  fd = getReferenceField(thisFileInputStream, F_FileInputStream_fd);
  pathString = getReferenceField(fd, F_FileDescriptor_fileName);
  path = String2string(pathString);

  return (char*)string2UTF8(path, NULL) + 2;
}

#define freeFileName(n) releaseMem((n)-2)

w_int FileInputStream_read
  (JNIEnv *env, w_instance thisFileInputStream) {
  w_instance     fdObj;
  vfs_FILE    *file;
  w_int        result = -1;

  fdObj = getReferenceField(thisFileInputStream, F_FileInputStream_fd);
  file = getWotsitField(fdObj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwIOException(JNIEnv2w_thread(env));
    result = 0;
  } else {
    if(!vfs_feof(file)) {
      result = vfs_fgetc(file);
    }
  }

  return result;
}

w_int FileInputStream_readIntoBuffer
  (JNIEnv *env, w_instance thisFileInputStream, w_instance buffer, w_int offset, w_int length) {
  w_instance     fdObj;
  vfs_FILE    *file;
  w_int        result;
  w_byte       *bytes;
  w_byte       *data;

  if(!buffer) {
    throwNullPointerException(JNIEnv2w_thread(env));
    return -1;
  }
  
  if(offset < 0 || length < 0 || offset > instance2Array_length(buffer) - length) {
    throwArrayIndexOutOfBoundsException(JNIEnv2w_thread(env));
    return -1;
  }

  if(length == 0) {
    return 0;
  }

  bytes = instance2Array_byte(buffer);
  fdObj = getReferenceField(thisFileInputStream, F_FileInputStream_fd);
  file = getWotsitField(fdObj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwIOException(JNIEnv2w_thread(env));
    result = 0;
  } else {
    data = bytes + offset;
    result = vfs_fread(data, 1, (w_word)length, file);
    if(result == 0 && vfs_feof(file)) {
      result = -1;
    }
  }

  return result;
}

w_long FileInputStream_skip
  (JNIEnv *env, w_instance thisFileInputStream, w_long n) {

  w_instance fdObj;
  vfs_FILE    *file;
  w_long       result = n;
  w_long       prev_pos;

  fdObj = getReferenceField(thisFileInputStream, F_FileInputStream_fd);
  file = getWotsitField(fdObj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwIOException(JNIEnv2w_thread(env));
    result = 0;
  } else {
    prev_pos = vfs_ftell(file);
    result = vfs_fseek(file, (long)n, SEEK_CUR);

    if(result != (long)-1) {
      result = vfs_ftell(file) - prev_pos;
    } else {
      throwIOException(JNIEnv2w_thread(env));
      result = 0; 
    }
  }

  return result;
}

w_int FileInputStream_available
  (JNIEnv *env, w_instance thisFileInputStream) {

  w_instance         fdObj;
  vfs_FILE        *file;
  w_int            result;
  const char      *filename;
  struct vfs_STAT statbuf;

  fdObj = getReferenceField(thisFileInputStream, F_FileInputStream_fd);
  file = getWotsitField(fdObj, F_FileDescriptor_fd);

  filename = getFISFileName(thisFileInputStream);
  
  if(file == NULL) {
    throwIOException(JNIEnv2w_thread(env));
    result = 0;
  } else {
    if(vfs_stat((w_ubyte *)filename, &statbuf) != -1) {
      result = (statbuf.st_size - vfs_ftell(file));
    } else {
      throwIOException(JNIEnv2w_thread(env));
      result = 0;
    }
  }

  return result;  
}

void FileInputStream_close
  (JNIEnv *env, w_instance thisFileInputStream) {

  w_instance     fdObj;
  vfs_FILE    *file;

  fdObj = getReferenceField(thisFileInputStream, F_FileInputStream_fd);

  if (fdObj) {
    file = getWotsitField(fdObj, F_FileDescriptor_fd);
  
    if(file) {
      vfs_fclose(file);
      clearWotsitField(fdObj, F_FileDescriptor_fd);
    }
  }
}

