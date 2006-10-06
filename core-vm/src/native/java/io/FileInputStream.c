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
#include "fields.h"
#include "vfs.h"
#include "vfs_fcntl.h"
/* Header for class FileInputStream */

#ifndef _Included_FileInputStream
#define _Included_FileInputStream
#ifdef __cplusplus
extern "C" {
#endif
#undef FileInputStream_SKIP_BUFFER_SIZE
#define FileInputStream_SKIP_BUFFER_SIZE 2048L

static jclass   fileinput_clazz;
static jclass   filedesc_clazz;
static jclass   exception_clazz;
static jclass   aioob_clazz;
static jclass   npe_clazz;
static jmethodID exception_constructor;
static jmethodID aioob_constructor;
static jmethodID npe_constructor;
static jmethodID filedesc_constructor;
static jfieldID fd_field;
static jfieldID fd;
static jfieldID name;
static jfieldID filedesc_validFD;

static void FileInputStreamInit(JNIEnv *env, jobject thisObj) {
  fileinput_clazz = (*env)->GetObjectClass(env, thisObj);
  filedesc_clazz = (*env)->FindClass(env, "java/io/FileDescriptor");
  exception_clazz = (*env)->FindClass(env, "java/io/IOException");
  aioob_clazz = (*env)->FindClass(env, "java/lang/ArrayIndexOutOfBoundsException");
  npe_clazz = (*env)->FindClass(env, "java/lang/NullPointerException");
  exception_constructor = (*env)->GetMethodID(env, exception_clazz, "<init>", "()V");
  aioob_constructor = (*env)->GetMethodID(env, aioob_clazz, "<init>", "()V");
  npe_constructor = (*env)->GetMethodID(env, npe_clazz, "<init>", "()V");
  filedesc_constructor = (*env)->GetMethodID(env, filedesc_clazz, "<init>", "()V");
  fd_field = (*env)->GetFieldID(env, fileinput_clazz, "fd", "Ljava/io/FileDescriptor;");
  fd = (*env)->GetFieldID(env, filedesc_clazz, "fd", "I");
  name = (*env)->GetFieldID(env, filedesc_clazz, "fileName", "Ljava/lang/String;");
  filedesc_validFD = (*env)->GetFieldID(env, filedesc_clazz, "validFD", "Z");
}

static void throwIOException(JNIEnv *env) {
  jthrowable throwObj = (jthrowable)(*env)->NewObject(env, exception_clazz, exception_constructor);
  (*env)->Throw(env, throwObj);
}

static inline void throwArrayIndexOutOfBoundsException(JNIEnv *env) {
	jthrowable throwObj = (jthrowable)(*env)->NewObject(env, aioob_clazz, aioob_constructor);
	(*env)->Throw(env, throwObj);
}

static inline void throwNullPointerException(JNIEnv *env) {
	jthrowable throwObj = (jthrowable)(*env)->NewObject(env, npe_clazz, npe_constructor);
	(*env)->Throw(env, throwObj);
}

/* Inaccessible static: skipBuffer */
/*
 * Class:     FileInputStream
 * Method:    createFromString
 * Signature: (Ljava/lang/String;)V
 */

JNIEXPORT jint JNICALL Java_FileInputStream_createFromString
(JNIEnv *env, jobject thisObj, jstring path) {

	const char  *pathname;
	jboolean    isCopy;
	vfs_FILE    *file;
	jobject     obj;

	if(!name) FileInputStreamInit(env, thisObj);

	pathname = (*env)->GetStringUTFChars(env, path, &isCopy);	  

	file = vfs_fopen(pathname, "r");

	if(isCopy == JNI_TRUE) (*env)->ReleaseStringUTFChars(env, path, pathname);

	if(file == NULL) {

		return 1;

	} else {
		obj = (*env)->NewObject(env, filedesc_clazz, filedesc_constructor);
		(*env)->SetObjectField(env, thisObj, fd_field, obj);

		(*env)->SetBooleanField(env, obj, filedesc_validFD, 1);
		setWotsitField(obj, F_FileDescriptor_fd, file);
		(*env)->SetObjectField(env, obj, name, path);
	}

	return 0;

}

/*
 * Class:     FileInputStream
 * Method:    createFromFileDescriptor
 * Signature: (Ljava/io/FileDescriptor;)V
 */
JNIEXPORT void JNICALL Java_FileInputStream_createFromFileDescriptor
  (JNIEnv *env, jobject thisObj, jobject filedesc) {

  if(!name) FileInputStreamInit(env, thisObj);
  
  (*env)->SetObjectField(env, thisObj, fd_field, filedesc);
	  
}

/*
 * Class:     FileInputStream
 * Method:    read
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_FileInputStream_read
  (JNIEnv *env, jobject thisObj) {

  jobject     obj;
  vfs_FILE    *file;
  jint        result = -1;

  obj = (*env)->GetObjectField(env, thisObj, fd_field);
  file = getWotsitField(obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwIOException(env);
    result = 0;
  } else {
    if(!vfs_feof(file)) result = vfs_fgetc(file);
  }

  return result;
}

/*
 * Class:     FileInputStream
 * Method:    readIntoBuffer
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_FileInputStream_readIntoBuffer
  (JNIEnv *env, jobject thisObj, jbyteArray buffer, jint offset, jint length) {

  jobject     obj;
  vfs_FILE    *file;
  jint        result;
  jboolean    isCopy;
  jbyte       *bytes;
  jbyte       *data;

  if(!buffer) {
    throwNullPointerException(env);
    return -1;
  }
  
  bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);
  
  if(offset < 0 || length < 0 || offset > (*env)->GetArrayLength(env, buffer) - length) {
    throwArrayIndexOutOfBoundsException(env);
    return -1;
  }

  if(length == 0) {
    return 0;
  }

  obj = (*env)->GetObjectField(env, thisObj, fd_field);
  file = getWotsitField(obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwIOException(env);
    result = 0;
  } else {
    data = bytes + offset;
    result = vfs_fread(data, 1, (w_word)length, file);
    if(result == 0 && vfs_feof(file)) {
      result = -1;
    }
  }

  if(isCopy == JNI_TRUE) (*env)->ReleaseByteArrayElements(env, buffer, bytes, 0);

  return result;
}

/*
 * Class:     FileInputStream
 * Method:    skip
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_FileInputStream_skip
  (JNIEnv *env, jobject thisObj, jlong n) {

  jobject     obj;
  vfs_FILE    *file;
  jlong       result = n;
  jlong       prev_pos;

  obj = (*env)->GetObjectField(env, thisObj, fd_field);
  file = getWotsitField(obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwIOException(env);
    result = 0;
  } else {
    prev_pos = vfs_ftell(file);
    result = vfs_fseek(file, (long)n, SEEK_CUR);

    if(result != (long)-1) {
      result = vfs_ftell(file) - prev_pos;
    } else {
      throwIOException(env);
      result = 0; 
    }
  }

  return result;
}

/*
 * Class:     FileInputStream
 * Method:    available
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_FileInputStream_available
  (JNIEnv *env, jobject thisObj) {

  jobject         obj;
  vfs_FILE        *file;
  jint            result;
  jboolean        isCopy;
  jstring         path;
  const char      *filename;
  struct vfs_STAT statbuf;

  obj = (*env)->GetObjectField(env, thisObj, fd_field);
  file = getWotsitField(obj, F_FileDescriptor_fd);

  path = (jstring)((*env)->GetObjectField(env, obj, name));
  filename = (*env)->GetStringUTFChars(env, path, &isCopy);	 
  
  if(file == NULL) {
    throwIOException(env);
    result = 0;
  } else {
    if(vfs_stat((w_ubyte *)filename, &statbuf) != -1) {
      result = (statbuf.st_size - vfs_ftell(file));
    } else {
      throwIOException(env);
      result = 0;
    }
  }

  if(isCopy == JNI_TRUE) (*env)->ReleaseStringUTFChars(env, path, filename);

  return result;  
}

/*
 * Class:     FileInputStream
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_FileInputStream_close
  (JNIEnv *env, jobject thisObj) {

  jobject     obj;
  vfs_FILE    *file;

  obj = (*env)->GetObjectField(env, thisObj, fd_field);

  if (obj) {
    file = getWotsitField(obj, F_FileDescriptor_fd);
  
    if(file) {
      vfs_fclose(file);
      clearWotsitField(obj, F_FileDescriptor_fd);
    }
  }
}

#ifdef __cplusplus
}
#endif
#endif
