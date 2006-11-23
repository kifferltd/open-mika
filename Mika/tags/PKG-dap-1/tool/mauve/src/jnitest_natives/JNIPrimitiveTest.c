/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


#include <jni.h>
#include <ieee754.h>
//#include "gnu_testlet_wonka_jni_JNIPrimitiveTest.h"

/* Inaccessible static: harness */
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveTest
 * Method:    nativeHalve
 * Signature: (B)B
 */
JNIEXPORT jbyte JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveTest_nativeHalve__B
  (JNIEnv *env , jobject obj, jbyte b)
{return ((jbyte)(b/2));}
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveTest
 * Method:    nativeHalve
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveTest_nativeHalve__I
  (JNIEnv *env , jobject obj, jint i)
{return ((jint)(i/2));}

/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveTest
 * Method:    nativeHalve
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveTest_nativeHalve__J
  (JNIEnv *env , jobject obj, jlong l)
{return ((jlong)(l/2L));}

/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveTest
 * Method:    nativeHalve
 * Signature: (S)S
 */
JNIEXPORT jshort JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveTest_nativeHalve__S
  (JNIEnv *env , jobject obj, jshort s)
{return ((jshort)(s/2));}

/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveTest
 * Method:    nativeInverse
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveTest_nativeInverse
  (JNIEnv *env , jobject obj, jboolean b )
{return ((b==JNI_TRUE)?JNI_FALSE:JNI_TRUE);}

/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveTest
 * Method:    nativePart
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveTest_nativePart__D
  (JNIEnv *env , jobject obj, jdouble d)
{ return ((jdouble) float64_div(D_ONE , d)); }
//{return ((jdouble)(1.0/d));}


/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveTest
 * Method:    nativePart
 * Signature: (F)F
 */
JNIEXPORT jfloat JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveTest_nativePart__F
  (JNIEnv *env , jobject obj, jfloat f)
{ return ((jfloat) float32_div(F_ONE , f)); }
//{return ((jfloat)(1.0f/f));}
