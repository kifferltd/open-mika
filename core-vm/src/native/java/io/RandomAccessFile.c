/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


#include "core-classes.h"
#include "exception.h"
#include "fields.h"
#include "vfs.h"
#include "wstrings.h"

static void throwArrayIndexOutOfBoundsException(JNIEnv *env) {
  throwException(JNIEnv2w_thread(env), clazzArrayIndexOutOfBoundsException, NULL);
}

static void throwIOException(JNIEnv *env) {
  throwException(JNIEnv2w_thread(env), clazzIOException, "%s", strerror(errno));
}

static void throwNullPointerException(JNIEnv *env) {
  throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
}

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
  fdesc = vfs_open(pathname, openmode, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);

  releaseMem(pathname - 2);

  if(fdesc == -1) {
    return 1;
  } 

  file = vfs_fdopen(fdesc, fmode);

  if (file == NULL) {
    return 1;
  } else {
    fd_obj = allocInstance(thread, clazzFileDescriptor);
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
    throwNullPointerException(env);
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
    throwArrayIndexOutOfBoundsException(env);

    return -1;
  }

  bytes = instance2Array_byte(buffer);
  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwNullPointerException(env);
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
  jlong       result = n;
  jlong       prev_pos;

  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwNullPointerException(env);
    result = 0;
  } else {
    prev_pos = vfs_ftell(file);
    result = vfs_fseek(file, (long)n, SEEK_SET);

    if(result == 0) {
      result = vfs_ftell(file) - prev_pos;
    } else {
      throwIOException(env);
      result = 0; 
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
    throwNullPointerException(env);
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
    throwArrayIndexOutOfBoundsException(env);

    return;
  }

  bytes = instance2Array_byte(buffer);
  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwNullPointerException(env);
  } else {
    data = bytes + offset;
    vfs_fwrite(data, 1, (w_word)length, file);
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
  jlong       result = -1;

  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwNullPointerException(env);
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
void Java_RandomAccessFile_seek (JNIEnv *env, w_instance thisRAF, jlong pos) {

  w_instance  fd_obj;
  vfs_FILE    *file;

  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwNullPointerException(env);
  } else {
    if(vfs_fseek(file, (long)pos, SEEK_SET) == -1) {
      throwIOException(env);
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
  w_int len;
  struct vfs_STAT statbuf;
  jlong result = 0;
  
  fd_obj = getReferenceField(thisRAF, F_RandomAccessFile_fd);
  path = getReferenceField(fd_obj, F_FileDescriptor_fileName);
  pathname_string = String2string(path);
  pathname = (char*)string2UTF8(pathname_string, &len) + 2;	  

  if(vfs_stat(pathname, &statbuf) != -1) {
    result = statbuf.st_size;
  }
  
  releaseMem(pathname - 2);

  return result;
}

/*
 * Class:     RandomAccessFile
 * Method:    setLength
 * Signature: (J)V
 */
void Java_RandomAccessFile_setLength (JNIEnv *env, w_instance thisRAF, jlong len) {

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
    throwNullPointerException(env);
  } else {
    
    file = getWotsitField(fd_obj, F_FileDescriptor_fd);
  
    if(file) {  
      vfs_fclose(file);
      clearWotsitField(fd_obj, F_FileDescriptor_fd);
    }
    
  }
  
}
