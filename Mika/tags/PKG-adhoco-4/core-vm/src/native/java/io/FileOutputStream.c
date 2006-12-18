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
/* Header for class FileOutputStream */

#ifndef _Included_FileOutputStream
#define _Included_FileOutputStream
#ifdef __cplusplus
extern "C" {
#endif

static jclass fileoutput_clazz;
static jclass filedesc_clazz;
static jclass ioe_clazz;
static jclass aioobe_clazz;
static jclass npe_clazz;
static jmethodID ioe_constructor;
static jmethodID aioobe_constructor;
static jmethodID npe_constructor;
static jmethodID filedesc_constructor;
static jfieldID fd_field;
static jfieldID fd;
static jfieldID name;
static jfieldID filedesc_validFD;

static void FileOutputStreamInit(JNIEnv *env, jobject thisObj) {
  fileoutput_clazz = (*env)->GetObjectClass(env, thisObj);
  filedesc_clazz = (*env)->FindClass(env, "java/io/FileDescriptor");
  ioe_clazz = (*env)->FindClass(env, "java/io/IOException");
  aioobe_clazz = (*env)->FindClass(env, "java/lang/ArrayIndexOutOfBoundsException");
  npe_clazz = (*env)->FindClass(env, "java/lang/NullPointerException");
  ioe_constructor = (*env)->GetMethodID(env, ioe_clazz, "<init>", "()V");
  aioobe_constructor = (*env)->GetMethodID(env, aioobe_clazz, "<init>", "()V");
  npe_constructor = (*env)->GetMethodID(env, npe_clazz, "<init>", "()V");
  filedesc_constructor = (*env)->GetMethodID(env, filedesc_clazz, "<init>", "()V");
  fd_field = (*env)->GetFieldID(env, fileoutput_clazz, "fd", "Ljava/io/FileDescriptor;");
  fd = (*env)->GetFieldID(env, filedesc_clazz, "fd", "I");
  name = (*env)->GetFieldID(env, filedesc_clazz, "fileName", "Ljava/lang/String;");
  filedesc_validFD = (*env)->GetFieldID(env, filedesc_clazz, "validFD", "Z");
}

static void throwIOException(JNIEnv *env) {
  jthrowable throwObj = (jthrowable)(*env)->NewObject(env, ioe_clazz, ioe_constructor);
  (*env)->Throw(env, throwObj);
}

static inline void throwArrayIndexOutOfBoundsException(JNIEnv *env) {
  jthrowable throwObj = (jthrowable)(*env)->NewObject(env, aioobe_clazz, aioobe_constructor);
  (*env)->Throw(env, throwObj);
}

static inline void throwNullPointerException(JNIEnv *env) {
  jthrowable throwObj = (jthrowable)(*env)->NewObject(env, npe_clazz, npe_constructor);
  (*env)->Throw(env, throwObj);
}

/*
 * Class:     FileOutputStream
 * Method:    createFromString
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT jint JNICALL Java_FileOutputStream_createFromString
  (JNIEnv *env, jobject thisObj, jstring path, jboolean append){

  const char  *pathname;
  jboolean    isCopy;
  vfs_FILE    *file;
  jobject     obj;

  if(!name) FileOutputStreamInit(env, thisObj);

  pathname = (*env)->GetStringUTFChars(env, path, &isCopy);	  

  if(append == JNI_FALSE) {
    file = vfs_fopen(pathname, "w+");
  } else {
    file = vfs_fopen(pathname, "a+");
  }

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
 * Class:     FileOutputStream
 * Method:    createFromFileDescriptor
 * Signature: (Ljava/io/FileDescriptor;)V
 */
JNIEXPORT void JNICALL Java_FileOutputStream_createFromFileDescriptor
  (JNIEnv *env, jobject thisObj, jobject filedesc) {

  if(!name) FileOutputStreamInit(env, thisObj);
  
  (*env)->SetObjectField(env, thisObj, fd_field, filedesc);
	  
}

/*
 * Class:     FileOutputStream
 * Method:    write
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_FileOutputStream_write
  (JNIEnv *env, jobject thisObj, jint oneByte) {

  jobject     obj;
  vfs_FILE    *file;

  obj = (*env)->GetObjectField(env, thisObj, fd_field);

  if(!obj) {
    throwIOException(env);
    return;
  }
    
  file = getWotsitField(obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwIOException(env);
  } else {
    vfs_fputc(oneByte, file);
  }
}

/*
 * Class:     FileOutputStream
 * Method:    writeFromBuffer
 * Signature: ([BII)V
 */
JNIEXPORT void JNICALL Java_FileOutputStream_writeFromBuffer
  (JNIEnv *env, jobject thisObj, jbyteArray buffer, jint offset, jint length) {

  jobject     obj;
  vfs_FILE    *file;
  jboolean    isCopy;
  jbyte       *bytes;
  jbyte       *data;

  if(!buffer) {
    throwNullPointerException(env);
    return;
  }

  bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);
  
  if(offset < 0 || length < 0 || offset > (*env)->GetArrayLength(env, buffer) - length) {
    throwArrayIndexOutOfBoundsException(env);
    return;
  }

  if(length == 0) {
    return;
  }

  obj = (*env)->GetObjectField(env, thisObj, fd_field);

  if(!obj) {
    throwIOException(env);
  }
  else {
  
    file = getWotsitField(obj, F_FileDescriptor_fd);

    if(file == NULL) {
      throwIOException(env);
    } else {
      data = bytes + offset;
      vfs_fwrite(data, 1, (w_word)length, file);
    }

  }

  if(isCopy == JNI_TRUE) (*env)->ReleaseByteArrayElements(env, buffer, bytes, 0);

}

/*
 * Class:     FileOutputStream
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_FileOutputStream_close
  (JNIEnv *env, jobject thisObj) {

  jobject     obj;
  vfs_FILE    *file;

  obj = (*env)->GetObjectField(env, thisObj, fd_field);

  woempa(9, "Closing file\n");

  if(obj != NULL) {

    woempa(9, "Still have a filedescriptor\n");
    
    file = getWotsitField(obj, F_FileDescriptor_fd);

    if(file == NULL) {
      woempa(9, "Filedescriptor is empty\n");
    } else {
      woempa(9, "Calling vfs_fclose\n");
      vfs_fclose(file);
      (*env)->SetObjectField(env, thisObj, fd_field, (jobject)NULL);
    }
  }
}

/*
 * Class:     FileOutputStream
 * Method:    flush
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_FileOutputStream_flush
  (JNIEnv *env, jobject thisObj) {

  jobject     obj;
  vfs_FILE    *file;

  obj = (*env)->GetObjectField(env, thisObj, fd_field);

  if (obj) {
    file = getWotsitField(obj, F_FileDescriptor_fd);
  
    if(file) {
      vfs_fflush(file);
    }
  } 
}

#ifdef __cplusplus
}
#endif
#endif
