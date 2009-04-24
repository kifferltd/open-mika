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

void FileOutputStream_write
  (JNIEnv *env, w_instance thisFileOutputStream, w_int oneByte) {

  w_instance     fdObj;
  vfs_FILE    *file;

  fdObj = getReferenceField(thisFileOutputStream, F_FileOutputStream_fd);

  if(!fdObj) {
    throwIOException(JNIEnv2w_thread(env));
    return;
  }
    
  file = getWotsitField(fdObj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwIOException(JNIEnv2w_thread(env));
  } else {
    vfs_fputc(oneByte, file);
  }
}

void FileOutputStream_writeFromBuffer
  (JNIEnv *env, w_instance thisFileOutputStream, w_instance buffer, w_int offset, w_int length) {

  w_instance     fdObj;
  vfs_FILE    *file;
  w_byte       *bytes;
  w_byte       *data;

  if(!buffer) {
    throwNullPointerException(JNIEnv2w_thread(env));
    return;
  }

  if(offset < 0 || length < 0 || offset > instance2Array_length(buffer) - length) {
    throwArrayIndexOutOfBoundsException(JNIEnv2w_thread(env));
    return;
  }

  if(length == 0) {
    return;
  }

  bytes = instance2Array_byte(buffer);
  fdObj = getReferenceField(thisFileOutputStream, F_FileOutputStream_fd);

  if(!fdObj) {
    throwIOException(JNIEnv2w_thread(env));
  }
  else {
  
    file = getWotsitField(fdObj, F_FileDescriptor_fd);

    if(file == NULL) {
      throwIOException(JNIEnv2w_thread(env));
    } else {
      data = bytes + offset;
      vfs_fwrite(data, 1, (w_word)length, file);
    }

  }

}

void FileOutputStream_close
  (JNIEnv *env, w_instance thisFileOutputStream) {

  w_instance     fdObj;
  vfs_FILE    *file;

  fdObj = getReferenceField(thisFileOutputStream, F_FileOutputStream_fd);

  woempa(9, "Closing file\n");

  if(fdObj != NULL) {

    woempa(9, "Still have a filedescriptor\n");
    
    file = getWotsitField(fdObj, F_FileDescriptor_fd);

    if(file == NULL) {
      woempa(9, "Filedescriptor is empty\n");
    } else {
      woempa(9, "Calling vfs_fclose\n");
      vfs_fclose(file);
      setReferenceField(thisFileOutputStream, NULL, F_FileOutputStream_fd);
    }
  }
}

void FileOutputStream_flush
  (JNIEnv *env, w_instance thisFileOutputStream) {

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

