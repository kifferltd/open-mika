/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2007, 2008 by Chris Gray, /k/ Embedded Java         *
* Solutions. All rights reserved.                                         *
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
#include "loading.h"
#include "vfs.h"
#include "wstrings.h"

/*
 * Class:     RandomAccessFile
 * Method:    createFromString
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
w_int Java_RandomAccessFile_createFromString (JNIEnv *env, w_instance thisRAF, w_instance path, int mode) {
  w_thread thread = JNIEnv2w_thread(env);
  w_string pathname_string;
  char *pathname;
  const char  *fmode = "r+";
  int         openmode;
  int         fdesc;
  vfs_FILE    *file;
  w_instance  fd_obj;
  struct vfs_STAT statbuf;
  int rc;

  if (mustBeInitialized(clazzFileDescriptor) == CLASS_LOADING_FAILED) {
    return 0;
  }

  pathname_string = String2string(path);
  pathname = (char*)string2UTF8(pathname_string, NULL) + 2;	  
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

  switch(mode) {
    case 0:
      if (rc != 0 || !VFS_S_ISREG(statbuf.st_mode) || (((statbuf.st_mode & VFS_S_IRWXU) & VFS_S_IRUSR) != VFS_S_IRUSR)) {
        return 1;
      }
      break;

    default:
      if (rc == 0 && (!VFS_S_ISREG(statbuf.st_mode) || (((statbuf.st_mode & VFS_S_IRWXU) & VFS_S_IWUSR) != VFS_S_IWUSR))) {
        return 1;
      }
  }

  fdesc = vfs_open(pathname, openmode, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);

  releaseMem(pathname - 2);

  if(fdesc == -1) {
    return 1;
  } 

  file = vfs_fdopen(fdesc, fmode);

  if (file == NULL) {
    return 1;
  } else {
    enterUnsafeRegion(thread);
    fd_obj = allocInstance(thread, clazzFileDescriptor);
    enterSafeRegion(thread);
    if(fd_obj != NULL) {
      setReferenceField(thisRAF, fd_obj, F_RandomAccessFile_fd);
      setBooleanField(fd_obj, F_FileDescriptor_validFD, 1);
      setWotsitField(fd_obj, F_FileDescriptor_fd, file);
      setReferenceField(fd_obj, path, F_FileDescriptor_fileName);
    }
  }
  return 0;
}

/*
 * Class:     RandomAccessFile
 * Method:    read
 * Signature: ()I
 */
w_int Java_RandomAccessFile_read (JNIEnv *env, w_instance thisRAF) {

  w_instance  fd_obj;
  vfs_FILE    *file;
  w_int       result = -1;

  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwNullPointerException(JNIEnv2w_thread(env));
    result = 0;
  } else {
    result = vfs_fgetc(file);
    //if(!vfs_feof(file)) result = vfs_fgetc(file);
  }

  return result;
}

/*
 * Class:     RandomAccessFile
 * Method:    readIntoBuffer
 * Signature: ([BII)I
 */
w_int Java_RandomAccessFile_readIntoBuffer (JNIEnv *env, w_instance thisRAF, w_instance buffer, w_int offset, w_int length) {

  w_instance  fd_obj;
  vfs_FILE    *file;
  w_int       result;
  w_sbyte     *bytes;
  w_sbyte     *data;

  if ((offset < 0) || (offset > instance2Array_length(buffer) - length)) {
    throwArrayIndexOutOfBoundsException(JNIEnv2w_thread(env));

    return -1;
  }

  bytes = instance2Array_byte(buffer);
  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwNullPointerException(JNIEnv2w_thread(env));
    result = 0;
  } else {
    data = bytes + offset;
    result = vfs_fread(data, 1, (w_word)length, file);
    if(result == 0) result = -1;
  }

  return result;
}

/*
 * Class:     RandomAccessFile
 * Method:    skipBytes
 * Signature: (I)I
 */
w_int Java_RandomAccessFile_skipBytes (JNIEnv *env, w_instance thisRAF, w_int n) {

  w_instance  fd_obj;
  vfs_FILE    *file;
  w_long       result = 0;
  w_long       prev_pos;

  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwNullPointerException(JNIEnv2w_thread(env));
  } else {
    w_string pathname_string;
    const char *pathname;
    w_instance path;
    w_int pathlen;
    struct vfs_STAT statbuf;
    w_long size = 0;

    path = getReferenceField(fd_obj, F_FileDescriptor_fileName);
    pathname_string = String2string(path);
    pathname = (char*)string2UTF8(pathname_string, &pathlen) + 2;

    if(vfs_stat(pathname, &statbuf) != -1) {
      size = statbuf.st_size;
    }

    releaseMem(pathname - 2);


    prev_pos = vfs_ftell(file);

    if(n > (size - prev_pos)) {
      n = size - prev_pos;
    }
     
    result = vfs_fseek(file, (long)n, SEEK_CUR);

    if(result == 0) {
      result = vfs_ftell(file) - prev_pos;
    } else {
      throwIOException(JNIEnv2w_thread(env));
    }
  }

  return result;
}

/*
 * Class:     RandomAccessFile
 * Method:    write
 * Signature: (I)V
 */
void Java_RandomAccessFile_write (JNIEnv *env, w_instance thisRAF, w_int oneByte) {

  w_instance  fd_obj;
  vfs_FILE    *file;

  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwNullPointerException(JNIEnv2w_thread(env));
  } else {
    vfs_fputc(oneByte, file);
  }
}

/*
 * Class:     RandomAccessFile
 * Method:    writeFromBuffer
 * Signature: ([BII)V
 */
void Java_RandomAccessFile_writeFromBuffer (JNIEnv *env, w_instance thisRAF, w_instance buffer, w_int offset, w_int length) {

  w_instance  fd_obj;
  vfs_FILE    *file;
  w_sbyte     *bytes;
  w_sbyte     *data;

  if ((offset < 0) || (offset > instance2Array_length(buffer) - length)) {
    throwArrayIndexOutOfBoundsException(JNIEnv2w_thread(env));

    return;
  }

  bytes = instance2Array_byte(buffer);
  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwNullPointerException(JNIEnv2w_thread(env));
  } else {
    data = bytes + offset;
    vfs_fwrite(data, 1, (w_word)length, file);
    vfs_fflush(file);
  }
}

/*
 * Class:     RandomAccessFile
 * Method:    getFilePointer
 * Signature: ()J
 */
w_long Java_RandomAccessFile_getFilePointer (JNIEnv *env, w_instance thisRAF) {

  w_instance  fd_obj;
  vfs_FILE    *file;
  w_long       result = -1;

  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwNullPointerException(JNIEnv2w_thread(env));
    result = 0;
  } else {
    result = vfs_ftell(file);
  }

  return result;
}

/*
 * Class:     RandomAccessFile
 * Method:    seek
 * Signature: (J)V
 */
void Java_RandomAccessFile_seek (JNIEnv *env, w_instance thisRAF, w_long pos) {

  w_instance  fd_obj;
  vfs_FILE    *file;

  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwNullPointerException(JNIEnv2w_thread(env));
  } else {
    if(vfs_fseek(file, (long)pos, SEEK_SET) == -1) {
      throwIOException(JNIEnv2w_thread(env));
    }
  }
}

/*
 * Class:     RandomAccessFile
 * Method:    length
 * Signature: ()J
 */
w_long Java_RandomAccessFile_length (JNIEnv *env, w_instance thisRAF) {

  w_instance fd_obj;
  w_string pathname_string;
  const char *pathname;
  w_instance path;
  w_int pathlen;
  struct vfs_STAT statbuf;
  w_long result = 0;
  
  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  path = getReferenceField(fd_obj, F_FileDescriptor_fileName);
  pathname_string = String2string(path);
  pathname = (char*)string2UTF8(pathname_string, &pathlen) + 2;	  

  if (vfs_stat(pathname, &statbuf) == -1) {
    throwIOException(JNIEnv2w_thread(env));
  }
  result = statbuf.st_size;
  woempa(7, "vfs_stat(%s, %p) returned length %d\n", pathname, &statbuf, result);
  releaseMem(pathname - 2);

  return result;
}

/*
 * Class:     RandomAccessFile
 * Method:    setLength
 * Signature: (J)V
 */
void Java_RandomAccessFile_setLength (JNIEnv *env, w_instance thisRAF, w_long newlen) {

  w_instance  fd_obj;
  vfs_FILE    *file;

  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwNullPointerException(JNIEnv2w_thread(env));
  } else {
    w_string pathname_string;
    const char *pathname;
    w_instance path;
    w_int pathlen;
    w_long oldptr;

    path = getReferenceField(fd_obj, F_FileDescriptor_fileName);
    pathname_string = String2string(path);
    pathname = (char*)string2UTF8(pathname_string, &pathlen) + 2;

    oldptr = vfs_ftell(file);

    if(vfs_truncate(pathname, newlen) == -1) {
      throwIOException(JNIEnv2w_thread(env));
    }

    releaseMem(pathname - 2);

    if(newlen < oldptr && vfs_fseek(file, newlen, SEEK_SET) == -1) {
      throwIOException(JNIEnv2w_thread(env));
    }
  }
}

/*
 * Class:     RandomAccessFile
 * Method:    close
 * Signature: ()V
 */
void Java_RandomAccessFile_close (JNIEnv *env, w_instance thisRAF) { 
  w_instance  fd_obj;
  vfs_FILE    *file;

  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);

  if(fd_obj == NULL) {
    throwNullPointerException(JNIEnv2w_thread(env));
  } else {
    
    file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
    if(file) {  
      vfs_fclose(file);
      clearWotsitField(fd_obj, F_FileDescriptor_fd);
    }
    
  }
  
}
