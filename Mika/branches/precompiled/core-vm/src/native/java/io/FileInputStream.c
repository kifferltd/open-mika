/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2007 by Chris Gray, /k/ Embedded Java Solutions.    *
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

static w_instance fileinput_clazz;
static w_instance filedesc_clazz;
static w_method filedesc_constructor;
static w_field fd_field;
static w_field fd;
static w_field name;
static w_field filedesc_validFD;

static void FileInputStreamInit(JNIEnv *env, jobject thisObj) {
  fileinput_clazz = (*env)->GetObjectClass(env, thisObj);
  filedesc_clazz = (*env)->FindClass(env, "java/io/FileDescriptor");
  filedesc_constructor = (*env)->GetMethodID(env, filedesc_clazz, "<init>", "()V");
  fd_field = (*env)->GetFieldID(env, fileinput_clazz, "fd", "Ljava/io/FileDescriptor;");
  fd = (*env)->GetFieldID(env, filedesc_clazz, "fd", "I");
  name = (*env)->GetFieldID(env, filedesc_clazz, "fileName", "Ljava/lang/String;");
  filedesc_validFD = (*env)->GetFieldID(env, filedesc_clazz, "validFD", "Z");
}

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
    throwIOException(JNIEnv2w_thread(env));
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
    throwNullPointerException(JNIEnv2w_thread(env));
    return -1;
  }
  
  bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);
  
  if(offset < 0 || length < 0 || offset > (*env)->GetArrayLength(env, buffer) - length) {
    throwArrayIndexOutOfBoundsException(JNIEnv2w_thread(env));
    return -1;
  }

  if(length == 0) {
    return 0;
  }

  obj = (*env)->GetObjectField(env, thisObj, fd_field);
  file = getWotsitField(obj, F_FileDescriptor_fd);
  
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

