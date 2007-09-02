#ifndef _JNI_H
#define _JNI_H

/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

/*
** $Id: jni.h,v 1.3 2006/10/04 14:24:14 cvsroot Exp $
*/

#include "wmath.h"
#include "wonka.h"

#define JNI_FALSE         WONKA_FALSE
#define JNI_TRUE          WONKA_TRUE

#define JNI_COMMIT        1
#define JNI_ABORT         2

#define JNI_OK            0
#define JNI_ERR           (-1)
#define JNI_EDETACHED     (-2)              /* thread detached from the VM */
#define JNI_EVERSION      (-3)              /* JNI version error           */
/*
** Mapping between the Wonka types and their JNI counterparts.
*/

typedef w_boolean         jboolean;
typedef w_sbyte           jbyte;
typedef w_char            jchar;
typedef w_short           jshort;
typedef w_int             jint;
typedef w_long            jlong;
typedef w_float           jfloat;
typedef w_double          jdouble;
// [DV 2000-10-03]
typedef w_instance        jobject;
typedef w_instance        jweak;

typedef jint              jsize;

/*
** The different reference types for JNI.
*/

typedef jobject           jclass;
typedef jobject           jstring;
typedef jobject           jarray;
typedef jobject           jobjectArray;
typedef jobject           jbooleanArray;
typedef jobject           jbyteArray;
typedef jobject           jcharArray;
typedef jobject           jshortArray;
typedef jobject           jintArray;
typedef jobject           jlongArray;
typedef jobject           jfloatArray;
typedef jobject           jdoubleArray;
typedef jobject           jthrowable;

/*
** Our opaque field and method ID's.
*/

typedef w_method          jmethodID;
typedef w_field           jfieldID;

/*
** The JNI value type.
*/

typedef union jvalue {
  jboolean z;
  jbyte    b;
  jchar    c;
  jshort   s;
  jint     i;
  jlong    j;
  jfloat   f;
  jdouble  d;
  jobject  l;
} jvalue; 

/*
** A type for discriminating 32bit and 64 bit variables
*/

typedef union jvar {
  w_word var_32;
  w_long var_64;
} jvar;

typedef struct JNINativeMethod {
  char *name;
  char *signature;
  void *fnPtr;
} JNINativeMethod;

#define	JNIEXPORT extern
#define	JNICALL

typedef const struct JNINativeInterface *JNIEnv;

typedef const struct JNIInvokeInterface *JavaVM;

typedef struct JNINativeInterface {

  void          *reserved0;
  void          *reserved1;
  void          *reserved2;
  void          *reserved3;

  jint          (*GetVersion)                      (JNIEnv *env);

  jclass        (*DefineClass)                     (JNIEnv *env, const char *name, jobject loader, const jbyte *buf, jsize buflen);
  jclass        (*FindClass)                       (JNIEnv *env, const char *name);

  jmethodID     (*FromReflectedMethod)             (JNIEnv *env, jobject method);
  jfieldID      (*FromReflectedField)              (JNIEnv *env, jobject field);
  jobject       (*ToReflectedMethod)               (JNIEnv *env, jclass class, jmethodID methodID, jboolean isStatic);

  jclass        (*GetSuperclass)                   (JNIEnv *env, jclass class);                                                     /*  10 */
  jboolean      (*IsAssignableFrom)                (JNIEnv *env, jclass class1, jclass class2);
  
  jobject       (*ToReflectedField)                (JNIEnv *env, jclass class, jfieldID fieldID, jboolean isStatic);
  
  jint          (*Throw)                           (JNIEnv *env, jthrowable obj);
  jint          (*ThrowNew)                        (JNIEnv *env, jclass class, const char *message);
  jthrowable    (*ExceptionOccurred)               (JNIEnv *env);
  void          (*ExceptionDescribe)               (JNIEnv *env);
  void          (*ExceptionClear)                  (JNIEnv *env);
  void          (*FatalError)                      (JNIEnv *env, const char *message);
  
  jint          (*PushLocalFrame)                  (JNIEnv *env, jint capacity);
  jobject       (*PopLocalFrame)                   (JNIEnv *env, jobject result);                                                   /*  20 */
  
  jobject       (*NewGlobalRef)                    (JNIEnv *env, jobject obj);
  void          (*DeleteGlobalRef)                 (JNIEnv *env, jobject gref);
  void          (*DeleteLocalRef)                  (JNIEnv *env, jobject lref);
  jboolean      (*IsSameObject)                    (JNIEnv *env, jobject ref1, jobject ref2);
  
  jobject       (*NewLocalRef)                     (JNIEnv *env, jobject ref);
  jint          (*EnsureLocalCapacity)             (JNIEnv *env, jint capacity);
  
  jobject       (*AllocObject)                     (JNIEnv *env, jclass class);
  jobject       (*NewObject)                       (JNIEnv *env, jclass class, jmethodID methodID, ...);  
  jobject       (*NewObjectV)                      (JNIEnv *env, jclass class, jmethodID methodID, va_list args);

  jobject       (*NewObjectA)                      (JNIEnv *env, jclass class, jmethodID methodID, jvalue *args);                   /*  30 */
  
  jclass        (*GetObjectClass)                  (JNIEnv *env, jobject obj);
  jboolean      (*IsInstanceOf)                    (JNIEnv *env, jobject obj, jclass class);
  
  jmethodID     (*GetMethodID)                     (JNIEnv *env, jclass class, const char *name, const char *sig);
  
  jobject       (*CallObjectMethod)                (JNIEnv *env, jobject obj, jmethodID methodID, ...);
  jobject       (*CallObjectMethodV)               (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
  jobject       (*CallObjectMethodA)               (JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args);
  jboolean      (*CallBooleanMethod)               (JNIEnv *env, jobject obj, jmethodID methodID, ...);
  jboolean      (*CallBooleanMethodV)              (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
  jboolean      (*CallBooleanMethodA)              (JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args);

  jbyte         (*CallByteMethod)                  (JNIEnv *env, jobject obj, jmethodID methodID, ...);                             /*  40 */
  jbyte         (*CallByteMethodV)                 (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
  jbyte         (*CallByteMethodA)                 (JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args);
  jchar         (*CallCharMethod)                  (JNIEnv *env, jobject obj, jmethodID methodID, ...);
  jchar         (*CallCharMethodV)                 (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
  jchar         (*CallCharMethodA)                 (JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args);
  jshort        (*CallShortMethod)                 (JNIEnv *env, jobject obj, jmethodID methodID, ...);
  jshort        (*CallShortMethodV)                (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
  jshort        (*CallShortMethodA)                (JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args);
  jint          (*CallIntMethod)                   (JNIEnv *env, jobject obj, jmethodID methodID, ...);

  jint          (*CallIntMethodV)                  (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);                    /*  50 */
  jint          (*CallIntMethodA)                  (JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args);
  jlong         (*CallLongMethod)                  (JNIEnv *env, jobject obj, jmethodID methodID, ...);
  jlong         (*CallLongMethodV)                 (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
  jlong         (*CallLongMethodA)                 (JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args);
  jfloat        (*CallFloatMethod)                 (JNIEnv *env, jobject obj, jmethodID methodID, ...);
  jfloat        (*CallFloatMethodV)                (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
  jfloat        (*CallFloatMethodA)                (JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args);
  jdouble       (*CallDoubleMethod)                (JNIEnv *env, jobject obj, jmethodID methodID, ...);
  jdouble       (*CallDoubleMethodV)               (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);

  jdouble       (*CallDoubleMethodA)               (JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args);                    /*  60 */
  void          (*CallVoidMethod)                  (JNIEnv *env, jobject obj, jmethodID methodID, ...);
  void          (*CallVoidMethodV)                 (JNIEnv *env, jobject obj, jmethodID methodID, va_list args);
  void          (*CallVoidMethodA)                 (JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args);

  jobject       (*CallNonvirtualObjectMethod)      (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...);
  jobject       (*CallNonvirtualObjectMethodV)     (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args);
  jobject       (*CallNonvirtualObjectMethodA)     (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args);
  jboolean      (*CallNonvirtualBooleanMethod)     (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...);
  jboolean      (*CallNonvirtualBooleanMethodV)    (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args);
  jboolean      (*CallNonvirtualBooleanMethodA)    (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args);
  jbyte         (*CallNonvirtualByteMethod)        (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...);               /*  70 */
  jbyte         (*CallNonvirtualByteMethodV)       (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args);
  jbyte         (*CallNonvirtualByteMethodA)       (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args);
  jchar         (*CallNonvirtualCharMethod)        (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...);
  jchar         (*CallNonvirtualCharMethodV)       (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args);
  jchar         (*CallNonvirtualCharMethodA)       (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args);
  jshort        (*CallNonvirtualShortMethod)       (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...);
  jshort        (*CallNonvirtualShortMethodV)      (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args);
  jshort        (*CallNonvirtualShortMethodA)      (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args);
  jint          (*CallNonvirtualIntMethod)         (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...);
  jint          (*CallNonvirtualIntMethodV)        (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args);      /*  80 */
  jint          (*CallNonvirtualIntMethodA)        (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args);
  jlong         (*CallNonvirtualLongMethod)        (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...);
  jlong         (*CallNonvirtualLongMethodV)       (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args);
  jlong         (*CallNonvirtualLongMethodA)       (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args);
  jfloat        (*CallNonvirtualFloatMethod)       (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...);
  jfloat        (*CallNonvirtualFloatMethodV)      (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args);
  jfloat        (*CallNonvirtualFloatMethodA)      (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args);
  jdouble       (*CallNonvirtualDoubleMethod)      (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...);
  jdouble       (*CallNonvirtualDoubleMethodV)     (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args);
  jdouble       (*CallNonvirtualDoubleMethodA)     (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args);      /*  90 */
  void          (*CallNonvirtualVoidMethod)        (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...);
  void          (*CallNonvirtualVoidMethodV)       (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args);
  void          (*CallNonvirtualVoidMethodA)       (JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args);

  jfieldID      (*GetFieldID)                      (JNIEnv *env, jclass class, const char *name, const char *sig);
  
  jobject       (*GetObjectField)                  (JNIEnv *env, jobject obj, jfieldID fieldID);
  jboolean      (*GetBooleanField)                 (JNIEnv *env, jobject obj, jfieldID fieldID);
  jbyte         (*GetByteField)                    (JNIEnv *env, jobject obj, jfieldID fieldID);
  jchar         (*GetCharField)                    (JNIEnv *env, jobject obj, jfieldID fieldID);
  jshort        (*GetShortField)                   (JNIEnv *env, jobject obj, jfieldID fieldID);
  jint          (*GetIntField)                     (JNIEnv *env, jobject obj, jfieldID fieldID);                                    /* 100 */
  jlong         (*GetLongField)                    (JNIEnv *env, jobject obj, jfieldID fieldID);
  jfloat        (*GetFloatField)                   (JNIEnv *env, jobject obj, jfieldID fieldID);
  jdouble       (*GetDoubleField)                  (JNIEnv *env, jobject obj, jfieldID fieldID);
  void          (*SetObjectField)                  (JNIEnv *env, jobject obj, jfieldID fieldID, jobject value);
  void          (*SetBooleanField)                 (JNIEnv *env, jobject obj, jfieldID fieldID, jboolean value);
  void          (*SetByteField)                    (JNIEnv *env, jobject obj, jfieldID fieldID, jbyte value);
  void          (*SetCharField)                    (JNIEnv *env, jobject obj, jfieldID fieldID, jchar value);
  void          (*SetShortField)                   (JNIEnv *env, jobject obj, jfieldID fieldID, jshort value);
  void          (*SetIntField)                     (JNIEnv *env, jobject obj, jfieldID fieldID, jint value);
  void          (*SetLongField)                    (JNIEnv *env, jobject obj, jfieldID fieldID, jlong value);                      /* 110 */
  void          (*SetFloatField)                   (JNIEnv *env, jobject obj, jfieldID fieldID, jfloat value);
  void          (*SetDoubleField)                  (JNIEnv *env, jobject obj, jfieldID fieldID, jdouble value);

  jmethodID     (*GetStaticMethodID)               (JNIEnv *env, jclass class, const char *name, const char *sig);
  
  jobject       (*CallStaticObjectMethod)          (JNIEnv *env, jclass class, jmethodID methodID, ...);
  jobject       (*CallStaticObjectMethodV)         (JNIEnv *env, jclass class, jmethodID methodID, va_list args);
  jobject       (*CallStaticObjectMethodA)         (JNIEnv *env, jclass class, jmethodID methodID, jvalue *args);
  jboolean      (*CallStaticBooleanMethod)         (JNIEnv *env, jclass class, jmethodID methodID, ...);
  jboolean      (*CallStaticBooleanMethodV)        (JNIEnv *env, jclass class, jmethodID methodID, va_list args);
  jboolean      (*CallStaticBooleanMethodA)        (JNIEnv *env, jclass class, jmethodID methodID, jvalue *args);
  jbyte         (*CallStaticByteMethod)            (JNIEnv *env, jclass class, jmethodID methodID, ...);                           /* 120 */
  jbyte         (*CallStaticByteMethodV)           (JNIEnv *env, jclass class, jmethodID methodID, va_list args);
  jbyte         (*CallStaticByteMethodA)           (JNIEnv *env, jclass class, jmethodID methodID, jvalue *args);
  jchar         (*CallStaticCharMethod)            (JNIEnv *env, jclass class, jmethodID methodID, ...);
  jchar         (*CallStaticCharMethodV)           (JNIEnv *env, jclass class, jmethodID methodID, va_list args);
  jchar         (*CallStaticCharMethodA)           (JNIEnv *env, jclass class, jmethodID methodID, jvalue *args);
  jshort        (*CallStaticShortMethod)           (JNIEnv *env, jclass class, jmethodID methodID, ...);
  jshort        (*CallStaticShortMethodV)          (JNIEnv *env, jclass class, jmethodID methodID, va_list args);
  jshort        (*CallStaticShortMethodA)          (JNIEnv *env, jclass class, jmethodID methodID, jvalue *args);
  jint          (*CallStaticIntMethod)             (JNIEnv *env, jclass class, jmethodID methodID, ...);
  jint          (*CallStaticIntMethodV)            (JNIEnv *env, jclass class, jmethodID methodID, va_list args);                  /* 130 */
  jint          (*CallStaticIntMethodA)            (JNIEnv *env, jclass class, jmethodID methodID, jvalue *args);
  jlong         (*CallStaticLongMethod)            (JNIEnv *env, jclass class, jmethodID methodID, ...);
  jlong         (*CallStaticLongMethodV)           (JNIEnv *env, jclass class, jmethodID methodID, va_list args);
  jlong         (*CallStaticLongMethodA)           (JNIEnv *env, jclass class, jmethodID methodID, jvalue *args);
  jfloat        (*CallStaticFloatMethod)           (JNIEnv *env, jclass class, jmethodID methodID, ...);
  jfloat        (*CallStaticFloatMethodV)          (JNIEnv *env, jclass class, jmethodID methodID, va_list args);
  jfloat        (*CallStaticFloatMethodA)          (JNIEnv *env, jclass class, jmethodID methodID, jvalue *args);
  jdouble       (*CallStaticDoubleMethod)          (JNIEnv *env, jclass class, jmethodID methodID, ...);
  jdouble       (*CallStaticDoubleMethodV)         (JNIEnv *env, jclass class, jmethodID methodID, va_list args);
  jdouble       (*CallStaticDoubleMethodA)         (JNIEnv *env, jclass class, jmethodID methodID, jvalue *args);                  /* 140 */
  void          (*CallStaticVoidMethod)            (JNIEnv *env, jclass class, jmethodID methodID, ...);
  void          (*CallStaticVoidMethodV)           (JNIEnv *env, jclass class, jmethodID methodID, va_list args);
  void          (*CallStaticVoidMethodA)           (JNIEnv *env, jclass class, jmethodID methodID, jvalue *args);
  
  jfieldID      (*GetStaticFieldID)                (JNIEnv *env, jclass class, const char *name, const char *sig);
  
  jobject       (*GetStaticObjectField)            (JNIEnv *env, jclass class, jfieldID fieldID);
  jboolean      (*GetStaticBooleanField)           (JNIEnv *env, jclass class, jfieldID fieldID);
  jbyte         (*GetStaticByteField)              (JNIEnv *env, jclass class, jfieldID fieldID);
  jchar         (*GetStaticCharField)              (JNIEnv *env, jclass class, jfieldID fieldID);
  jshort        (*GetStaticShortField)             (JNIEnv *env, jclass class, jfieldID fieldID);
  jint          (*GetStaticIntField)               (JNIEnv *env, jclass class, jfieldID fieldID);                                  /* 150 */
  jlong         (*GetStaticLongField)              (JNIEnv *env, jclass class, jfieldID fieldID);
  jfloat        (*GetStaticFloatField)             (JNIEnv *env, jclass class, jfieldID fieldID);
  jdouble       (*GetStaticDoubleField)            (JNIEnv *env, jclass class, jfieldID fieldID);
  void          (*SetStaticObjectField)            (JNIEnv *env, jclass class, jfieldID fieldID, jobject value);
  void          (*SetStaticBooleanField)           (JNIEnv *env, jclass class, jfieldID fieldID, jboolean value);
  void          (*SetStaticByteField)              (JNIEnv *env, jclass class, jfieldID fieldID, jbyte value);
  void          (*SetStaticCharField)              (JNIEnv *env, jclass class, jfieldID fieldID, jchar value);
  void          (*SetStaticShortField)             (JNIEnv *env, jclass class, jfieldID fieldID, jshort value);
  void          (*SetStaticIntField)               (JNIEnv *env, jclass class, jfieldID fieldID, jint value);
  void          (*SetStaticLongField)              (JNIEnv *env, jclass class, jfieldID fieldID, jlong value);                    /* 160 */
  void          (*SetStaticFloatField)             (JNIEnv *env, jclass class, jfieldID fieldID, jfloat value);
  void          (*SetStaticDoubleField)            (JNIEnv *env, jclass class, jfieldID fieldID, jdouble value);
  
  jstring       (*NewString)                       (JNIEnv *env, const jchar *uchars, jsize len);
  jsize         (*GetStringLength)                 (JNIEnv *env, jstring string);
  const jchar  *(*GetStringChars)                  (JNIEnv *env, jstring string, jboolean *isCopy);
  void          (*ReleaseStringChars)              (JNIEnv *env, jstring string, const jchar *chars);
  
  jstring       (*NewStringUTF)                    (JNIEnv *env, const char *bytes);
  jsize         (*GetStringUTFLength)              (JNIEnv *env, jstring string);
  const jbyte  *(*GetStringUTFChars)               (JNIEnv *env, jstring string, jboolean *isCopy);
  void          (*ReleaseStringUTFChars)           (JNIEnv *env, jstring string, const char *utf);                                /* 170 */
  
  jsize         (*GetArrayLength)                  (JNIEnv *env, jarray array);
  
  jarray        (*NewObjectArray)                  (JNIEnv *env, jsize length, jclass elementType, jobject initialElement);
  jobject       (*GetObjectArrayElement)           (JNIEnv *env, jobjectArray array, jsize aindex);
  void          (*SetObjectArrayElement)           (JNIEnv *env, jobjectArray array, jsize aindex, jobject value);
  
  jbooleanArray (*NewBooleanArray)                 (JNIEnv *env, jsize length);
  jbyteArray    (*NewByteArray)                    (JNIEnv *env, jsize length);
  jcharArray    (*NewCharArray)                    (JNIEnv *env, jsize length);
  jshortArray   (*NewShortArray)                   (JNIEnv *env, jsize length);
  jintArray     (*NewIntArray)                     (JNIEnv *env, jsize length);
  jlongArray    (*NewLongArray)                    (JNIEnv *env, jsize length);                                                   /* 180 */
  jfloatArray   (*NewFloatArray)                   (JNIEnv *env, jsize length);
  jdoubleArray  (*NewDoubleArray)                  (JNIEnv *env, jsize length);
  
  jboolean     *(*GetBooleanArrayElements)         (JNIEnv *env, jbooleanArray array, jboolean *isCopy);
  jbyte        *(*GetByteArrayElements)            (JNIEnv *env, jbyteArray array, jboolean *isCopy);
  jchar        *(*GetCharArrayElements)            (JNIEnv *env, jcharArray array, jboolean *isCopy);
  jshort       *(*GetShortArrayElements)           (JNIEnv *env, jshortArray array, jboolean *isCopy);
  jint         *(*GetIntArrayElements)             (JNIEnv *env, jintArray array, jboolean *isCopy);
  jlong        *(*GetLongArrayElements)            (JNIEnv *env, jlongArray array, jboolean *isCopy);
  jfloat       *(*GetFloatArrayElements)           (JNIEnv *env, jfloatArray array, jboolean *isCopy);
  jdouble      *(*GetDoubleArrayElements)          (JNIEnv *env, jdoubleArray array, jboolean *isCopy);                           /* 190 */

  void          (*ReleaseBooleanArrayElements)     (JNIEnv *env, jbooleanArray array, jboolean *elems, jint mode);
  void          (*ReleaseByteArrayElements)        (JNIEnv *env, jbyteArray array, jbyte *elems, jint mode);
  void          (*ReleaseCharArrayElements)        (JNIEnv *env, jcharArray array, jchar *elems, jint mode);
  void          (*ReleaseShortArrayElements)       (JNIEnv *env, jshortArray array, jshort *elems, jint mode);
  void          (*ReleaseIntArrayElements)         (JNIEnv *env, jintArray array, jint *elems, jint mode);
  void          (*ReleaseLongArrayElements)        (JNIEnv *env, jlongArray array, jlong *elems, jint mode);
  void          (*ReleaseFloatArrayElements)       (JNIEnv *env, jfloatArray array, jfloat *elems, jint mode);
  void          (*ReleaseDoubleArrayElements)      (JNIEnv *env, jdoubleArray array, jdouble *elems, jint mode);

  void          (*GetBooleanArrayRegion)           (JNIEnv *env, jbooleanArray array, jsize start, jsize len, jboolean *buf);
  void          (*GetByteArrayRegion)              (JNIEnv *env, jbyteArray array, jsize start, jsize len, jbyte *buf);             /* 200 */
  void          (*GetCharArrayRegion)              (JNIEnv *env, jcharArray array, jsize start, jsize len, jchar *buf);
  void          (*GetShortArrayRegion)             (JNIEnv *env, jshortArray array, jsize start, jsize len, jshort *buf);
  void          (*GetIntArrayRegion)               (JNIEnv *env, jintArray array, jsize start, jsize len, jint *buf);
  void          (*GetLongArrayRegion)              (JNIEnv *env, jlongArray array, jsize start, jsize len, jlong *buf);
  void          (*GetFloatArrayRegion)             (JNIEnv *env, jfloatArray array, jsize start, jsize len, jfloat *buf);
  void          (*GetDoubleArrayRegion)            (JNIEnv *env, jdoubleArray array, jsize start, jsize len, jdouble *buf);

  void          (*SetBooleanArrayRegion)           (JNIEnv *env, jbooleanArray array, jsize start, jsize len, jboolean *buf);
  void          (*SetByteArrayRegion)              (JNIEnv *env, jbyteArray array, jsize start, jsize len, jbyte *buf);
  void          (*SetCharArrayRegion)              (JNIEnv *env, jcharArray array, jsize start, jsize len, jchar *buf);
  void          (*SetShortArrayRegion)             (JNIEnv *env, jshortArray array, jsize start, jsize len, jshort *buf);           /* 210 */
  void          (*SetIntArrayRegion)               (JNIEnv *env, jintArray array, jsize start, jsize len, jint *buf);
  void          (*SetLongArrayRegion)              (JNIEnv *env, jlongArray array, jsize start, jsize len, jlong *buf);
  void          (*SetFloatArrayRegion)             (JNIEnv *env, jfloatArray array, jsize start, jsize len, jfloat *buf);
  void          (*SetDoubleArrayRegion)            (JNIEnv *env, jdoubleArray array, jsize start, jsize len, jdouble *buf);

  jint          (*RegisterNatives)                 (JNIEnv *env, jclass class, const JNINativeMethod *methods, jint nMethods);
  jint          (*UnregisterNatives)               (JNIEnv *env, jclass class);
  
  jint          (*MonitorEnter)                    (JNIEnv *env, jobject obj);
  jint          (*MonitorExit)                     (JNIEnv *env, jobject obj);

  jint          (*GetJavaVM)                       (JNIEnv *env, JavaVM** vm);
  void          (*GetStringRegion)                 (JNIEnv *env, jstring string, jsize start, jsize len, jchar *buf);               /* 220 */  
  void          (*GetStringUTFRegion)              (JNIEnv *env, jstring string, jsize start, jsize len, char *buf);
  void         *(*GetPrimitiveArrayCritical)       (JNIEnv *env, jarray array, jboolean *isCopy);
  void          (*ReleasePrimitiveArrayCritical)   (JNIEnv *env, jarray array, void *carray, jint mode);
  const jchar  *(*GetStringCritical)               (JNIEnv *env, jstring string, jboolean *isCopy);
  void          (*ReleaseStringCritical)           (JNIEnv *env, jstring string, const jchar *carray);
  jweak         (*NewWeakGlobalRef)                (JNIEnv *env, jobject obj);
  void          *reserved227;
  jboolean      (*ExceptionCheck)                  (JNIEnv *env);

} JNINativeInterface;

/*
** A couple of dummy structures used by the invocation interface.
*/

/*
** The Invocation API consists of three global and three per-JVM functions.
** Currently we only have one JVM.
*/


typedef struct JNIInvokeInterface {
// N.B. The first three entries are bogus: these are global functions ...
  jint (*JNI_GetDefaultJavaVMInitArgs)(void *vm_args);
  jint (*JNI_GetCreatedJavaVMs)  (JavaVM **vmBuf, jsize bufLen, jsize *nVMs);
  jint (*JNI_CreateJavaVM)       (JavaVM **p_VM, JNIEnv **p_env, void *vm_args);
  jint (*DestroyJavaVM)          (JavaVM *vm);
  jint (*AttachCurrentThread)    (JavaVM *vm, JNIEnv **p_env, void *thr_args);
  jint (*DetachCurrentThread)    (JavaVM *vm);
  jint (*GetEnv)                 (JavaVM *vm, void **env, jint version);
  jint (*AttachCurrentThreadAsDaemon)  (JavaVM* vm, void** penv, void* args);
} JNIInvokeInterface;


jint JNI_GetDefaultJavaVMInitArgs(void *vm_args);
jint JNI_GetCreatedJavaVMs(JavaVM **vmBuf, jsize bufLen, jsize *nVMs);
jint JNI_CreateJavaVM(JavaVM **p_VM, JNIEnv **p_env, void *vm_args);
jint DestroyJavaVM(JavaVM *vm);
jint AttachCurrentThread(JavaVM *vm, JNIEnv **p_env, void *thr_args);
jint DetachCurrentThread(JavaVM *vm);

extern const struct JNINativeInterface w_JNINativeInterface; 
extern const struct JNIInvokeInterface w_JNIInvokeInterface; 

typedef struct JavaVMInitArgs {
/// Java VM version
  jint version;
/// System properties
  char **properties;
/// Whether to check for source file newer than class file
  jint checkSource;
/// Max native stack size for Java-created threads
  jint nativeStackSize;
/// Max Java stack size
  jint javaStackSize;
/// Starting heap size
  jint minHeapSize;
/// Max heap size
  jint maxHeapSize;
/// Verify: 0=none, 1=non-local, 2=all
  jint verifyMode;
/// local dir path for classloading
  const char *classpath;
/// Callback for VM messages
// N.B.: the void* should really be a FILE*, FIXME
  jint (*vfprint)(void *fp, const char *format, va_list args);
/// VM exit callback
  void (*exit)(jint code);
/// VM abort callback
  void (*abort)(void);
/// Enable GC of classes
  jint enableClassGC;
/// Make GC loquacious
  jint enableVerboseGC;
/// Turn off asynchronous GC
  jint disableAsyncGC;
/// TBD
  jint reserved0;
  jint reserved1;
  jint reserved2;
} Wonka_InitArgs;

typedef struct Wonka_AttachArgs {
  char *name;
} Wonka_AttachArgs;

#endif /* _JNI_H */

