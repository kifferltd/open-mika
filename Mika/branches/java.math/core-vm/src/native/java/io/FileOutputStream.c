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

static w_instance fileoutput_clazz;
static w_instance filedesc_clazz;
static w_method filedesc_constructor;
static w_field fd_field;
static w_field fd;
static w_field name;
static w_field filedesc_validFD;

static void FileOutputStreamInit(JNIEnv *env, jobject thisObj) {
  fileoutput_clazz = (*env)->GetObjectClass(env, thisObj);
  filedesc_clazz = (*env)->FindClass(env, "java/io/FileDescriptor");
  filedesc_constructor = (*env)->GetMethodID(env, filedesc_clazz, "<init>", "()V");
  fd_field = (*env)->GetFieldID(env, fileoutput_clazz, "fd", "Ljava/io/FileDescriptor;");
  fd = (*env)->GetFieldID(env, filedesc_clazz, "fd", "I");
  name = (*env)->GetFieldID(env, filedesc_clazz, "fileName", "Ljava/lang/String;");
  filedesc_validFD = (*env)->GetFieldID(env, filedesc_clazz, "validFD", "Z");
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
    throwIOException(JNIEnv2w_thread(env));
    return;
  }
    
  file = getWotsitField(obj, F_FileDescriptor_fd);
  
  if(file == NULL) {
    throwIOException(JNIEnv2w_thread(env));
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
    throwNullPointerException(JNIEnv2w_thread(env));
    return;
  }

  bytes = (*env)->GetByteArrayElements(env, buffer, &isCopy);
  
  if(offset < 0 || length < 0 || offset > (*env)->GetArrayLength(env, buffer) - length) {
    throwArrayIndexOutOfBoundsException(JNIEnv2w_thread(env));
    return;
  }

  if(length == 0) {
    return;
  }

  obj = (*env)->GetObjectField(env, thisObj, fd_field);

  if(!obj) {
    throwIOException(JNIEnv2w_thread(env));
  }
  else {
  
    file = getWotsitField(obj, F_FileDescriptor_fd);

    if(file == NULL) {
      throwIOException(JNIEnv2w_thread(env));
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

