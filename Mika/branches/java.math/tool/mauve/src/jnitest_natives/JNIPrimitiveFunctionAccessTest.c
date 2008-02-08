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
//#include "gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest.h"


/***********************************************************************************************************
*
*  Declaring the assigning functions: get boolean, byte, short, int, long, float, double... setting functions
* for the call to normal, static and base-class functions, using the -A and -V options
***********************************************************************************************************/

jbyte GetByteValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args);
jbyte GetByteValueV(JNIEnv *env, jobject obj, const char* functionsig, ...);
jbyte GetStaticByteValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args);
jbyte GetStaticByteValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...);
jbyte GetNonvirtualByteValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args);
jbyte GetNonvirtualByteValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...);

jchar GetCharValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args);
jchar GetCharValueV(JNIEnv *env, jobject obj, const char* functionsig, ...);
jchar GetStaticCharValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args);
jchar GetStaticCharValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...);
jchar GetNonvirtualCharValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args);
jchar GetNonvirtualCharValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...);

jshort GetShortValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args);
jshort GetShortValueV(JNIEnv *env, jobject obj, const char* functionsig, ...);
jshort GetStaticShortValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args);
jshort GetStaticShortValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...);
jshort GetNonvirtualShortValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args);
jshort GetNonvirtualShortValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...);

jint GetIntValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args);
jint GetIntValueV(JNIEnv *env, jobject obj, const char* functionsig, ...);
jint GetStaticIntValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args);
jint GetStaticIntValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...);
jint GetNonvirtualIntValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args);
jint GetNonvirtualIntValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...);

jlong GetLongValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args);
jlong GetLongValueV(JNIEnv *env, jobject obj, const char* functionsig, ...);
jlong GetStaticLongValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args);
jlong GetStaticLongValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...);
jlong GetNonvirtualLongValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args);
jlong GetNonvirtualLongValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...);

jfloat GetFloatValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args);
jfloat GetFloatValueV(JNIEnv *env, jobject obj, const char* functionsig, ...);
jfloat GetStaticFloatValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args);
jfloat GetStaticFloatValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...);
jfloat GetNonvirtualFloatValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args);
jfloat GetNonvirtualFloatValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...);

jdouble GetDoubleValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args);
jdouble GetDoubleValueV(JNIEnv *env, jobject obj, const char* functionsig, ...);
jdouble GetStaticDoubleValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args);
jdouble GetStaticDoubleValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...);
jdouble GetNonvirtualDoubleValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args);
jdouble GetNonvirtualDoubleValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...);

jboolean GetBooleanValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args);
jboolean GetBooleanValueV(JNIEnv *env, jobject obj, const char* functionsig, ...);
jboolean GetStaticBooleanValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args);
jboolean GetStaticBooleanValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...);
jboolean GetNonvirtualBooleanValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args);
jboolean GetNonvirtualBooleanValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...);

/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/

/***********************************************************************************************************
*
* return an array of bytes obtained by calling the CallByteMethodA and CallByteMethodV functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    byteArray
 * Signature: (BBB)[B
 */
JNIEXPORT jbyteArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_byteArray
  (JNIEnv *env, jobject obj, jbyte x0, jbyte x1, jbyte x2)
{
  jbyteArray resultlist= (*env)->NewByteArray(env, 3);
  jbyte *results = (*env)->GetByteArrayElements(env, resultlist, 0);
  jvalue args[3];
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;


  // build the data array
  args[0].b = x0;
  args[1].b = x1;
  args[2].b = x2;
  // set the return value array
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(B)B");
  results[0] = (*env)->CallByteMethod(env, obj, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(BB)B");
  results[1] = (*env)->CallByteMethod(env, obj, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(BBB)B");
  results[2] = (*env)->CallByteMethod(env, obj, testfunction, x0, x1, x2);

  (*env)->ReleaseByteArrayElements(env, resultlist, results, 0);
  return resultlist;
}


JNIEXPORT jbyteArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_byteArrayA
  (JNIEnv *env, jobject obj, jbyte x0, jbyte x1, jbyte x2)
{
  jbyteArray resultlist= (*env)->NewByteArray(env, 3);
  jbyte *results = (*env)->GetByteArrayElements(env, resultlist, 0);
  jvalue args[3];

  // build the data array
  args[0].b = x0;
  args[1].b = x1;
  args[2].b = x2;
  // set the return value array
  results[0] = GetByteValueA(env, obj, "(B)B" ,args);
  results[1] = GetByteValueA(env, obj, "(BB)B" ,args);
  results[2] = GetByteValueA(env, obj, "(BBB)B" ,args);

  (*env)->ReleaseByteArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jbyteArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_byteArrayV
  (JNIEnv *env, jobject obj, jbyte x0, jbyte x1, jbyte x2)
{
  jbyteArray resultlist= (*env)->NewByteArray(env, 3);
  jbyte *results = (*env)->GetByteArrayElements(env, resultlist, 0);

  // set the return value array
  results[0] = GetByteValueV(env, obj, "(B)B" ,x0);
  results[1] = GetByteValueV(env, obj, "(BB)B" ,x0,x1);
  results[2] = GetByteValueV(env, obj, "(BBB)B" ,x0,x1,x2);

  (*env)->ReleaseByteArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jbyte GetByteValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args)
{
  jbyte result = (jbyte)0;
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallByteMethodA(env, obj, testfunction, args);
  return result;
}

jbyte GetByteValueV(JNIEnv *env, jobject obj, const char* functionsig, ...)
{
  jbyte result = (jbyte)0;
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallByteMethodV(env, obj, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}


/***********************************************************************************************************
*
* Idem for static class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    staticByteArray
 * Signature: (BBBB)[B
 */
JNIEXPORT jbyteArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticByteArray
  (JNIEnv *env, jobject obj, jbyte x0, jbyte x1, jbyte x2, jbyte x3)
{
  jbyteArray resultlist= (*env)->NewByteArray(env, 4);
  jbyte *results = (*env)->GetByteArrayElements(env, resultlist, 0);
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;
  // test static function calling

  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(BB)B");
  results[0] = (*env)->CallStaticByteMethod(env, testclass, testfunction, x0, x1);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(BBB)B");
  results[1] = (*env)->CallStaticByteMethod(env, testclass, testfunction, x0, x1, x2);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(BBBB)B");
  results[2] = (*env)->CallStaticByteMethod(env, testclass, testfunction, x0, x1, x2, x3);
  (*env)->ReleaseByteArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jbyteArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticByteArrayA
  (JNIEnv *env, jobject obj, jbyte x0, jbyte x1, jbyte x2, jbyte x3)
{
  jbyteArray resultlist= (*env)->NewByteArray(env, 3);
  jbyte *results = (*env)->GetByteArrayElements(env, resultlist, 0);
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jvalue args[4];

  // build the data array
  args[0].b = x0;
  args[1].b = x1;
  args[2].b = x2;
  args[3].b = x3;
  // set the return value array
  results[0] = GetStaticByteValueA(env, testclass, "(BB)B" ,args);
  results[1] = GetStaticByteValueA(env, testclass, "(BBB)B" ,args);
  results[2] = GetStaticByteValueA(env, testclass, "(BBBB)B" ,args);

  (*env)->ReleaseByteArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jbyteArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticByteArrayV
  (JNIEnv *env, jobject obj, jbyte x0, jbyte x1, jbyte x2, jbyte x3)
{
  jbyteArray resultlist= (*env)->NewByteArray(env, 4);
  jbyte *results = (*env)->GetByteArrayElements(env, resultlist, 0);
  jclass testclass = (*env)->GetObjectClass(env, obj);
  // test static function calling

  // set the return value array
  results[0] = GetStaticByteValueV(env, testclass, "(BB)B" ,x0,x1);
  results[1] = GetStaticByteValueV(env, testclass, "(BBB)B" ,x0,x1,x2);
  results[2] = GetStaticByteValueV(env, testclass, "(BBBB)B" ,x0,x1,x2,x3);

  (*env)->ReleaseByteArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jbyte GetStaticByteValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args)
{
  jbyte result = (jbyte)0;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallStaticByteMethodA(env, testclass, testfunction, args);
  return result;
}

jbyte GetStaticByteValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...)
{
  jbyte result = (jbyte)0;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallStaticByteMethodV(env, testclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}


/***********************************************************************************************************
*
* Idem for nonvirtual base class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    nonvirtualByteArray
 * Signature: (BBB)[B
 */
JNIEXPORT jbyteArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualByteArray
  (JNIEnv *env, jobject obj, jbyte x0, jbyte x1, jbyte x2)
{
  jbyteArray resultlist= (*env)->NewByteArray(env, 3);
  jbyte *results = (*env)->GetByteArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jmethodID testfunction;

  // set the return value array
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(B)B");
  results[0] = (*env)->CallNonvirtualByteMethod(env, obj, superclass, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(BB)B");
  results[1] = (*env)->CallNonvirtualByteMethod(env, obj, superclass, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(BBB)B");
  results[2] = (*env)->CallNonvirtualByteMethod(env, obj, superclass, testfunction, x0, x1, x2);

  (*env)->ReleaseByteArrayElements(env, resultlist, results, 0);
  return resultlist;
}


JNIEXPORT jbyteArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualByteArrayA
  (JNIEnv *env, jobject obj, jbyte x0, jbyte x1, jbyte x2)
{
  jbyteArray resultlist= (*env)->NewByteArray(env, 3);
  jbyte *results = (*env)->GetByteArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jvalue args[3];

  // build the data array
  args[0].b = x0;
  args[1].b = x1;
  args[2].b = x2;
  // set the return value array
  results[0] = GetNonvirtualByteValueA(env, obj, superclass, "(B)B" ,args);
  results[1] = GetNonvirtualByteValueA(env, obj, superclass, "(BB)B" ,args);
  results[2] = GetNonvirtualByteValueA(env, obj, superclass, "(BBB)B" ,args);

  (*env)->ReleaseByteArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jbyteArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualByteArrayV
  (JNIEnv *env, jobject obj, jbyte x0, jbyte x1, jbyte x2)
{
  jbyteArray resultlist= (*env)->NewByteArray(env, 3);
  jbyte *results = (*env)->GetByteArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);

  // set the return value array
  results[0] = GetNonvirtualByteValueV(env, obj, superclass, "(B)B" ,x0);
  results[1] = GetNonvirtualByteValueV(env, obj, superclass, "(BB)B" ,x0,x1);
  results[2] = GetNonvirtualByteValueV(env, obj, superclass, "(BBB)B" ,x0,x1,x2);

  (*env)->ReleaseByteArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jbyte GetNonvirtualByteValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args)
{
  jbyte result = (jbyte)0;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallNonvirtualByteMethodA(env, obj, superclass, testfunction, args);
  return result;
}

jbyte GetNonvirtualByteValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...)
{
  jbyte result = (jbyte)0;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallNonvirtualByteMethodV(env, obj, superclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}

/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/

/***********************************************************************************************************
*
* return an array of chars obtained by calling the CallCharMethodA and CallCharMethodV functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    CharArray
 * Signature: (CCC)[C
 */

JNIEXPORT jcharArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_charArray
  (JNIEnv *env, jobject obj, jchar x0, jchar x1, jchar x2)
{
  jcharArray resultlist= (*env)->NewCharArray(env, 3);
  jchar *results = (*env)->GetCharArrayElements(env, resultlist, 0);
  jvalue args[3];
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;


  // build the data array
  args[0].b = x0;
  args[1].b = x1;
  args[2].b = x2;
  // set the return value array
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(C)C");
  results[0] = (*env)->CallCharMethod(env, obj, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(CC)C");
  results[1] = (*env)->CallCharMethod(env, obj, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(CCC)C");
  results[2] = (*env)->CallCharMethod(env, obj, testfunction, x0, x1, x2);

  (*env)->ReleaseCharArrayElements(env, resultlist, results, 0);
  return resultlist;
}



JNIEXPORT jcharArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_charArrayA
  (JNIEnv *env, jobject obj, jchar x0, jchar x1, jchar x2)
{
  jcharArray resultlist= (*env)->NewCharArray(env, 3);
  jchar *results = (*env)->GetCharArrayElements(env, resultlist, 0);
  jvalue args[3];

  // build the data array
  args[0].s = x0;
  args[1].s = x1;
  args[2].s = x2;
  // set the return value array
  results[0] = GetCharValueA(env, obj, "(C)C" ,args);
  results[1] = GetCharValueA(env, obj, "(CC)C" ,args);
  results[2] = GetCharValueA(env, obj, "(CCC)C" ,args);

  (*env)->ReleaseCharArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jcharArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_charArrayV
  (JNIEnv *env, jobject obj, jchar x0, jchar x1, jchar x2)
{
  jcharArray resultlist = (*env)->NewCharArray(env, 3);
  jchar *results = (*env)->GetCharArrayElements(env, resultlist, 0);

  // set the return value array
  results[0] = GetCharValueV(env, obj, "(C)C" ,x0);
  results[1] = GetCharValueV(env, obj, "(CC)C" ,x0,x1);
  results[2] = GetCharValueV(env, obj, "(CCC)C" ,x0,x1,x2);

  (*env)->ReleaseCharArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jchar GetCharValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args)
{
  jchar result = (jchar)0;
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallCharMethodA(env, obj, testfunction, args);
  return result;
}

jchar GetCharValueV(JNIEnv *env, jobject obj, const char* functionsig, ...)
{
  jchar result = (jchar)0;
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallCharMethodV(env, obj, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}



/***********************************************************************************************************
*
* Idem for static class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    staticCharArray
 * Cignature: (CCCC)[C
 */
JNIEXPORT jcharArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticCharArray
  (JNIEnv *env, jobject obj, jchar x0, jchar x1, jchar x2, jchar x3)
{
  jcharArray resultlist= (*env)->NewCharArray(env, 4);
  jchar *results = (*env)->GetCharArrayElements(env, resultlist, 0);
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;
  // test static function calling

  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(CC)C");
  results[0] = (*env)->CallStaticCharMethod(env, testclass, testfunction, x0, x1);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(CCC)C");
  results[1] = (*env)->CallStaticCharMethod(env, testclass, testfunction, x0, x1, x2);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(CCCC)C");
  results[2] = (*env)->CallStaticCharMethod(env, testclass, testfunction, x0, x1, x2, x3);
  (*env)->ReleaseCharArrayElements(env, resultlist, results, 0);
  return resultlist;
}



JNIEXPORT jcharArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticCharArrayA
  (JNIEnv *env, jobject obj, jchar x0, jchar x1, jchar x2, jchar x3)
{
  jcharArray resultlist= (*env)->NewCharArray(env, 3);
  jchar *results = (*env)->GetCharArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jvalue args[4];

  // build the data array
  args[0].s = x0;
  args[1].s = x1;
  args[2].s = x2;
  args[3].s = x3;
  // set the return value array
  results[0] = GetStaticCharValueA(env, testclass, "(CC)C" ,args);
  results[1] = GetStaticCharValueA(env, testclass, "(CCC)C" ,args);
  results[2] = GetStaticCharValueA(env, testclass, "(CCCC)C" ,args);

  (*env)->ReleaseCharArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jcharArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticCharArrayV
  (JNIEnv *env, jobject obj, jchar x0, jchar x1, jchar x2, jchar x3)
{
  jcharArray resultlist= (*env)->NewCharArray(env, 3);
  jchar *results = (*env)->GetCharArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  results[0] = GetStaticCharValueV(env, testclass, "(CC)C" ,x0,x1);
  results[1] = GetStaticCharValueV(env, testclass, "(CCC)C" ,x0,x1,x2);
  results[2] = GetStaticCharValueV(env, testclass, "(CCCC)C" ,x0,x1,x2,x3);

  (*env)->ReleaseCharArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jchar GetStaticCharValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args)
{
  jchar result = (jchar)0;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallStaticCharMethodA(env, testclass, testfunction, args);
  return result;
}

jchar GetStaticCharValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...)
{
  jchar result = (jchar)0;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallStaticCharMethodV(env, testclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}

/***********************************************************************************************************
*
* Idem for nonvirtual base class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    nonvirtualCharArray
 * Cignature: (CCC)[C
 */
JNIEXPORT jcharArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualCharArray
  (JNIEnv *env, jobject obj, jchar x0, jchar x1, jchar x2)
{
  jcharArray resultlist= (*env)->NewCharArray(env, 3);
  jchar *results = (*env)->GetCharArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jmethodID testfunction;

  // set the return value array
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(C)C");
  results[0] = (*env)->CallNonvirtualCharMethod(env, obj, superclass, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(CC)C");
  results[1] = (*env)->CallNonvirtualCharMethod(env, obj, superclass, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(CCC)C");
  results[2] = (*env)->CallNonvirtualCharMethod(env, obj, superclass, testfunction, x0, x1, x2);

  (*env)->ReleaseCharArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jcharArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualCharArrayA
  (JNIEnv *env, jobject obj, jchar x0, jchar x1, jchar x2)
{
  jcharArray resultlist= (*env)->NewCharArray(env, 3);
  jchar *results = (*env)->GetCharArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jvalue args[3];

  // build the data array
  args[0].s = x0;
  args[1].s = x1;
  args[2].s = x2;
  // set the return value array
  results[0] = GetNonvirtualCharValueA(env, obj, superclass, "(C)C" ,args);
  results[1] = GetNonvirtualCharValueA(env, obj, superclass, "(CC)C" ,args);
  results[2] = GetNonvirtualCharValueA(env, obj, superclass, "(CCC)C" ,args);

  (*env)->ReleaseCharArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jcharArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualCharArrayV
  (JNIEnv *env, jobject obj, jchar x0, jchar x1, jchar x2)
{
  jcharArray resultlist= (*env)->NewCharArray(env, 3);
  jchar *results = (*env)->GetCharArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);

  results[0] = GetNonvirtualCharValueV(env, obj, superclass, "(C)C" ,x0);
  results[1] = GetNonvirtualCharValueV(env, obj, superclass, "(CC)C" ,x0,x1);
  results[2] = GetNonvirtualCharValueV(env, obj, superclass, "(CCC)C" ,x0,x1,x2);

  (*env)->ReleaseCharArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jchar GetNonvirtualCharValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args)
{
  jchar result = (jchar)0;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallNonvirtualCharMethodA(env, obj, superclass, testfunction, args);
  return result;
}

jchar GetNonvirtualCharValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...)
{
  jchar result = (jchar)0;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallNonvirtualCharMethodV(env, obj, superclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}

/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/

/***********************************************************************************************************
*
* return an array of shorts obtained by calling the CallShortMethodA and CallShortMethodV functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    shortArray
 * Signature: (SSS)[S
 */
JNIEXPORT jshortArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_shortArray
  (JNIEnv *env, jobject obj, jshort x0, jshort x1, jshort x2)
{
  jshortArray resultlist= (*env)->NewShortArray(env, 3);
  jshort *results = (*env)->GetShortArrayElements(env, resultlist, 0);
  jvalue args[3];
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;


  // build the data array
  args[0].b = x0;
  args[1].b = x1;
  args[2].b = x2;
  // set the return value array
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(S)S");
  results[0] = (*env)->CallShortMethod(env, obj, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(SS)S");
  results[1] = (*env)->CallShortMethod(env, obj, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(SSS)S");
  results[2] = (*env)->CallShortMethod(env, obj, testfunction, x0, x1, x2);

  (*env)->ReleaseShortArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jshortArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_shortArrayA
  (JNIEnv *env, jobject obj, jshort x0, jshort x1, jshort x2)
{
  jshortArray resultlist= (*env)->NewShortArray(env, 3);
  jshort *results = (*env)->GetShortArrayElements(env, resultlist, 0);
  jvalue args[3];

  // build the data array
  args[0].s = x0;
  args[1].s = x1;
  args[2].s = x2;
  // set the return value array
  results[0] = GetShortValueA(env, obj, "(S)S" ,args);
  results[1] = GetShortValueA(env, obj, "(SS)S" ,args);
  results[2] = GetShortValueA(env, obj, "(SSS)S" ,args);

  (*env)->ReleaseShortArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jshortArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_shortArrayV
  (JNIEnv *env, jobject obj, jshort x0, jshort x1, jshort x2)
{
  jshortArray resultlist = (*env)->NewShortArray(env, 3);
  jshort *results = (*env)->GetShortArrayElements(env, resultlist, 0);

  // set the return value array
  results[0] = GetShortValueV(env, obj, "(S)S" ,x0);
  results[1] = GetShortValueV(env, obj, "(SS)S" ,x0,x1);
  results[2] = GetShortValueV(env, obj, "(SSS)S" ,x0,x1,x2);

  (*env)->ReleaseShortArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jshort GetShortValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args)
{
  jshort result = (jshort)0;
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallShortMethodA(env, obj, testfunction, args);
  return result;
}

jshort GetShortValueV(JNIEnv *env, jobject obj, const char* functionsig, ...)
{
  jshort result = (jshort)0;
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallShortMethodV(env, obj, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}



/***********************************************************************************************************
*
* Idem for static class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    staticShortArray
 * Signature: (SSSS)[S
 */

JNIEXPORT jshortArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticShortArray
  (JNIEnv *env, jobject obj, jshort x0, jshort x1, jshort x2, jshort x3)
{
  jshortArray resultlist= (*env)->NewShortArray(env, 4);
  jshort *results = (*env)->GetShortArrayElements(env, resultlist, 0);
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;
  // test static function calling

  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(SS)S");
  results[0] = (*env)->CallStaticShortMethod(env, testclass, testfunction, x0, x1);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(SSS)S");
  results[1] = (*env)->CallStaticShortMethod(env, testclass, testfunction, x0, x1, x2);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(SSSS)S");
  results[2] = (*env)->CallStaticShortMethod(env, testclass, testfunction, x0, x1, x2, x3);
  (*env)->ReleaseShortArrayElements(env, resultlist, results, 0);
  return resultlist;
}


JNIEXPORT jshortArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticShortArrayA
  (JNIEnv *env, jobject obj, jshort x0, jshort x1, jshort x2, jshort x3)
{
  jshortArray resultlist= (*env)->NewShortArray(env, 3);
  jshort *results = (*env)->GetShortArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jvalue args[4];

  // build the data array
  args[0].s = x0;
  args[1].s = x1;
  args[2].s = x2;
  args[3].s = x3;
  // set the return value array
  results[0] = GetStaticShortValueA(env, testclass, "(SS)S" ,args);
  results[1] = GetStaticShortValueA(env, testclass, "(SSS)S" ,args);
  results[2] = GetStaticShortValueA(env, testclass, "(SSSS)S" ,args);

  (*env)->ReleaseShortArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jshortArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticShortArrayV
  (JNIEnv *env, jobject obj, jshort x0, jshort x1, jshort x2, jshort x3)
{
  jshortArray resultlist= (*env)->NewShortArray(env, 3);
  jshort *results = (*env)->GetShortArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  results[0] = GetStaticShortValueV(env, testclass, "(SS)S" ,x0,x1);
  results[1] = GetStaticShortValueV(env, testclass, "(SSS)S" ,x0,x1,x2);
  results[2] = GetStaticShortValueV(env, testclass, "(SSSS)S" ,x0,x1,x2,x3);

  (*env)->ReleaseShortArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jshort GetStaticShortValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args)
{
  jshort result = (jshort)0;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallStaticShortMethodA(env, testclass, testfunction, args);
  return result;
}

jshort GetStaticShortValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...)
{
  jshort result = (jshort)0;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallStaticShortMethodV(env, testclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}

/***********************************************************************************************************
*
* Idem for nonvirtual base class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    nonvirtualShortArray
 * Signature: (SSS)[S
 */
JNIEXPORT jshortArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualShortArray
  (JNIEnv *env, jobject obj, jshort x0, jshort x1, jshort x2)
{
  jshortArray resultlist= (*env)->NewShortArray(env, 3);
  jshort *results = (*env)->GetShortArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jmethodID testfunction;

  // set the return value array
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(S)S");
  results[0] = (*env)->CallNonvirtualShortMethod(env, obj, superclass, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(SS)S");
  results[1] = (*env)->CallNonvirtualShortMethod(env, obj, superclass, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(SSS)S");
  results[2] = (*env)->CallNonvirtualShortMethod(env, obj, superclass, testfunction, x0, x1, x2);

  (*env)->ReleaseShortArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jshortArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualShortArrayA
  (JNIEnv *env, jobject obj, jshort x0, jshort x1, jshort x2)
{
  jshortArray resultlist= (*env)->NewShortArray(env, 3);
  jshort *results = (*env)->GetShortArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jvalue args[3];

  // build the data array
  args[0].s = x0;
  args[1].s = x1;
  args[2].s = x2;
  // set the return value array
  results[0] = GetNonvirtualShortValueA(env, obj, superclass, "(S)S" ,args);
  results[1] = GetNonvirtualShortValueA(env, obj, superclass, "(SS)S" ,args);
  results[2] = GetNonvirtualShortValueA(env, obj, superclass, "(SSS)S" ,args);

  (*env)->ReleaseShortArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jshortArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualShortArrayV
  (JNIEnv *env, jobject obj, jshort x0, jshort x1, jshort x2)
{
  jshortArray resultlist= (*env)->NewShortArray(env, 3);
  jshort *results = (*env)->GetShortArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);

  results[0] = GetNonvirtualShortValueV(env, obj, superclass, "(S)S" ,x0);
  results[1] = GetNonvirtualShortValueV(env, obj, superclass, "(SS)S" ,x0,x1);
  results[2] = GetNonvirtualShortValueV(env, obj, superclass, "(SSS)S" ,x0,x1,x2);

  (*env)->ReleaseShortArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jshort GetNonvirtualShortValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args)
{
  jshort result = (jshort)0;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallNonvirtualShortMethodA(env, obj, superclass, testfunction, args);
  return result;
}

jshort GetNonvirtualShortValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...)
{
  jshort result = (jshort)0;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallNonvirtualShortMethodV(env, obj, superclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}

/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/

/***********************************************************************************************************
*
* return an array of integers obtained by calling the CallIntMethodA and CallIntMethodV functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    intArray
 * Signature: (III)[I
 */
JNIEXPORT jintArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_intArray
  (JNIEnv *env, jobject obj, jint x0, jint x1, jint x2)
{
  jintArray resultlist= (*env)->NewIntArray(env, 3);
  jint *results = (*env)->GetIntArrayElements(env, resultlist, 0);
  jvalue args[3];
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;


  // build the data array
  args[0].b = x0;
  args[1].b = x1;
  args[2].b = x2;
  // set the return value array
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(I)I");
  results[0] = (*env)->CallIntMethod(env, obj, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(II)I");
  results[1] = (*env)->CallIntMethod(env, obj, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(III)I");
  results[2] = (*env)->CallIntMethod(env, obj, testfunction, x0, x1, x2);

  (*env)->ReleaseIntArrayElements(env, resultlist, results, 0);
  return resultlist;
}


JNIEXPORT jintArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_intArrayA
  (JNIEnv *env, jobject obj, jint x0, jint x1, jint x2)
{
  jintArray resultlist= (*env)->NewIntArray(env, 3);
  jint *results = (*env)->GetIntArrayElements(env, resultlist, 0);
  jvalue args[3];

  // build the data array
  args[0].i = x0;
  args[1].i = x1;
  args[2].i = x2;
  // set the return value array
  results[0] = GetIntValueA(env, obj, "(I)I" ,args);
  results[1] = GetIntValueA(env, obj, "(II)I" ,args);
  results[2] = GetIntValueA(env, obj, "(III)I" ,args);

  (*env)->ReleaseIntArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jintArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_intArrayV
  (JNIEnv *env, jobject obj, jint x0, jint x1, jint x2)
{
  jintArray resultlist= (*env)->NewIntArray(env, 3);
  jint *results = (*env)->GetIntArrayElements(env, resultlist, 0);

  results[0] = GetIntValueV(env, obj, "(I)I" ,x0);
  results[1] = GetIntValueV(env, obj, "(II)I" ,x0,x1);
  results[2] = GetIntValueV(env, obj, "(III)I" ,x0,x1,x2);

  (*env)->ReleaseIntArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jint GetIntValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args)
{
  jint result = 0;
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallIntMethodA(env, obj, testfunction, args);
  return result;
}

jint GetIntValueV(JNIEnv *env, jobject obj, const char* functionsig, ...)
{
  jint result = 0;
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallIntMethodV(env, obj, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}



/***********************************************************************************************************
*
* Idem for static class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    staticIntArray
 * Signature: (IIII)[I
 */

JNIEXPORT jintArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticIntArray
  (JNIEnv *env, jobject obj, jint x0, jint x1, jint x2, jint x3)
{
  jintArray resultlist= (*env)->NewIntArray(env, 4);
  jint *results = (*env)->GetIntArrayElements(env, resultlist, 0);
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;
  // test static function calling

  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(II)I");
  results[0] = (*env)->CallStaticIntMethod(env, testclass, testfunction, x0, x1);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(III)I");
  results[1] = (*env)->CallStaticIntMethod(env, testclass, testfunction, x0, x1, x2);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(III)I");
  results[2] = (*env)->CallStaticIntMethod(env, testclass, testfunction, x0, x1, x2, x3);
  (*env)->ReleaseIntArrayElements(env, resultlist, results, 0);
  return resultlist;
}


JNIEXPORT jintArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticIntArrayA
  (JNIEnv *env, jobject obj, jint x0, jint x1, jint x2, jint x3)
{
  jintArray resultlist= (*env)->NewIntArray(env, 3);
  jint *results = (*env)->GetIntArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jvalue args[4];

  // build the data array
  args[0].i = x0;
  args[1].i = x1;
  args[2].i = x2;
  args[3].i = x3;
  // set the return value array
  results[0] = GetStaticIntValueA(env, testclass, "(II)I" ,args);
  results[1] = GetStaticIntValueA(env, testclass, "(III)I" ,args);
  results[2] = GetStaticIntValueA(env, testclass, "(IIII)I" ,args);

  (*env)->ReleaseIntArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jintArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticIntArrayV
  (JNIEnv *env, jobject obj, jint x0, jint x1, jint x2, jint x3)
{
  jintArray resultlist= (*env)->NewIntArray(env, 3);
  jint *results = (*env)->GetIntArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);

  results[0] = GetStaticIntValueV(env, testclass, "(II)I" ,x0,x1);
  results[1] = GetStaticIntValueV(env, testclass, "(III)I" ,x0,x1,x2);
  results[2] = GetStaticIntValueV(env, testclass, "(IIII)I" ,x0,x1,x2,x3);

  (*env)->ReleaseIntArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jint GetStaticIntValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args)
{
  jint result = 0;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallStaticIntMethodA(env, testclass, testfunction, args);
  return result;
}

jint GetStaticIntValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...)
{
  jint result = 0;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallStaticIntMethodV(env, testclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}



/***********************************************************************************************************
*
* Idem for nonvirtual base class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    nonvirtualIntArray
 * Signature: (III)[I
 */
JNIEXPORT jintArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualIntArray
  (JNIEnv *env, jobject obj, jint x0, jint x1, jint x2)
{
  jintArray resultlist= (*env)->NewIntArray(env, 3);
  jint *results = (*env)->GetIntArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jmethodID testfunction;

  // set the return value array
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(I)I");
  results[0] = (*env)->CallNonvirtualIntMethod(env, obj, superclass, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(II)I");
  results[1] = (*env)->CallNonvirtualIntMethod(env, obj, superclass, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(III)I");
  results[2] = (*env)->CallNonvirtualIntMethod(env, obj, superclass, testfunction, x0, x1, x2);

  (*env)->ReleaseIntArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jintArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualIntArrayA
  (JNIEnv *env, jobject obj, jint x0, jint x1, jint x2)
{
  jintArray resultlist= (*env)->NewIntArray(env, 3);
  jint *results = (*env)->GetIntArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jvalue args[3];

  // build the data array
  args[0].i = x0;
  args[1].i = x1;
  args[2].i = x2;
  // set the return value array
  results[0] = GetNonvirtualIntValueA(env, obj, superclass, "(I)I" ,args);
  results[1] = GetNonvirtualIntValueA(env, obj, superclass, "(II)I" ,args);
  results[2] = GetNonvirtualIntValueA(env, obj, superclass, "(III)I" ,args);

  (*env)->ReleaseIntArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jintArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualIntArrayV
  (JNIEnv *env, jobject obj, jint x0, jint x1, jint x2)
{
  jintArray resultlist= (*env)->NewIntArray(env, 3);
  jint *results = (*env)->GetIntArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);

  results[0] = GetNonvirtualIntValueV(env, obj, superclass, "(I)I" ,x0);
  results[1] = GetNonvirtualIntValueV(env, obj, superclass, "(II)I" ,x0,x1);
  results[2] = GetNonvirtualIntValueV(env, obj, superclass, "(III)I" ,x0,x1,x2);

  (*env)->ReleaseIntArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jint GetNonvirtualIntValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args)
{
  jint result = 0;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallNonvirtualIntMethodA(env, obj, superclass, testfunction, args);
  return result;
}

jint GetNonvirtualIntValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...)
{
  jint result = 0;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallNonvirtualIntMethodV(env, obj, superclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}


/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/

/***********************************************************************************************************
*
* return an array of long integers obtained by calling the CallLongMethodA and CallLongMethodV functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    longArray
 * Signature: (JJJ)[J
 */
JNIEXPORT jlongArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_longArray
  (JNIEnv *env, jobject obj, jlong x0, jlong x1, jlong x2)
{
  jlongArray resultlist= (*env)->NewLongArray(env, 3);
  jlong *results = (*env)->GetLongArrayElements(env, resultlist, 0);
  jvalue args[3];
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;


  // build the data array
  args[0].b = x0;
  args[1].b = x1;
  args[2].b = x2;
  // set the return value array
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(J)J");
  results[0] = (*env)->CallLongMethod(env, obj, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(JJ)J");
  results[1] = (*env)->CallLongMethod(env, obj, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(JJJ)J");
  results[2] = (*env)->CallLongMethod(env, obj, testfunction, x0, x1, x2);

  (*env)->ReleaseLongArrayElements(env, resultlist, results, 0);
  return resultlist;
}


JNIEXPORT jlongArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_longArrayA
  (JNIEnv *env, jobject obj, jlong x0, jlong x1, jlong x2)
{
  jlongArray resultlist= (*env)->NewLongArray(env, 3);
  jlong *results = (*env)->GetLongArrayElements(env, resultlist, 0);
  jvalue args[3];

  // build the data array
  args[0].j = x0;
  args[1].j = x1;
  args[2].j = x2;
  // set the return value array
  results[0] = GetLongValueA(env, obj, "(J)J" ,args);
  results[1] = GetLongValueA(env, obj, "(JJ)J" ,args);
  results[2] = GetLongValueA(env, obj, "(JJJ)J" ,args);

  (*env)->ReleaseLongArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jlongArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_longArrayV
  (JNIEnv *env, jobject obj, jlong x0, jlong x1, jlong x2)
{
  jlongArray resultlist= (*env)->NewLongArray(env, 3);
  jlong *results = (*env)->GetLongArrayElements(env, resultlist, 0);

  results[0] = GetLongValueV(env, obj, "(J)J" ,x0);
  results[1] = GetLongValueV(env, obj, "(JJ)J" ,x0,x1);
  results[2] = GetLongValueV(env, obj, "(JJJ)J" ,x0,x1,x2);

  (*env)->ReleaseLongArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jlong GetLongValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args)
{
  jlong result = 0L;
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallLongMethodA(env, obj, testfunction, args);
  return result;
}

jlong GetLongValueV(JNIEnv *env, jobject obj,const char* functionsig, ...)
{
  jlong result = 0L;
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallLongMethodV(env, obj, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}



/***********************************************************************************************************
*
* Idem for static class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    staticLongArray
 * Signature: (JJJJ)[J
 */
JNIEXPORT jlongArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticLongArray
  (JNIEnv *env, jobject obj, jlong x0, jlong x1, jlong x2, jlong x3)
{
  jlongArray resultlist= (*env)->NewLongArray(env, 4);
  jlong *results = (*env)->GetLongArrayElements(env, resultlist, 0);
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;
  // test static function calling

  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(JJ)J");
  results[0] = (*env)->CallStaticLongMethod(env, testclass, testfunction, x0, x1);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(JJJ)J");
  results[1] = (*env)->CallStaticLongMethod(env, testclass, testfunction, x0, x1, x2);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(JJJJ)J");
  results[2] = (*env)->CallStaticLongMethod(env, testclass, testfunction, x0, x1, x2, x3);
  (*env)->ReleaseLongArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jlongArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticLongArrayA
   (JNIEnv *env, jobject obj, jlong x0, jlong x1, jlong x2, jlong x3)
{
  jlongArray resultlist= (*env)->NewLongArray(env, 3);
  jlong *results = (*env)->GetLongArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jvalue args[4];

  // build the data array
  args[0].j = x0;
  args[1].j = x1;
  args[2].j = x2;
  args[3].j = x3;
  // set the return value array
  results[0] = GetStaticLongValueA(env, testclass, "(JJ)J" ,args);
  results[1] = GetStaticLongValueA(env, testclass, "(JJJ)J" ,args);
  results[2] = GetStaticLongValueA(env, testclass, "(JJJJ)J" ,args);

  (*env)->ReleaseLongArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jlongArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticLongArrayV
   (JNIEnv *env, jobject obj, jlong x0, jlong x1, jlong x2, jlong x3)
{
  jlongArray resultlist= (*env)->NewLongArray(env, 3);
  jlong *results = (*env)->GetLongArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);

  // set the return value array
  results[0] = GetStaticLongValueV(env, testclass, "(JJ)J" ,x0,x1);
  results[1] = GetStaticLongValueV(env, testclass, "(JJJ)J" ,x0,x1,x2);
  results[2] = GetStaticLongValueV(env, testclass, "(JJJJ)J" ,x0,x1,x2,x3);

  (*env)->ReleaseLongArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jlong GetStaticLongValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args)
{
  jlong result = 0L;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallStaticLongMethodA(env, testclass, testfunction, args);
  return result;
}

jlong GetStaticLongValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...)
{
  jlong result = 0L;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallStaticLongMethodV(env, testclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}



/***********************************************************************************************************
*
* Idem for nonvirtual base class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    nonvirtualLongArray
 * Signature: (JJJ)[J
 */

JNIEXPORT jlongArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualLongArray
  (JNIEnv *env, jobject obj, jlong x0, jlong x1, jlong x2)
{
  jlongArray resultlist= (*env)->NewLongArray(env, 3);
  jlong *results = (*env)->GetLongArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jmethodID testfunction;

  // set the return value array
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(J)J");
  results[0] = (*env)->CallNonvirtualLongMethod(env, obj, superclass, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(JJ)J");
  results[1] = (*env)->CallNonvirtualLongMethod(env, obj, superclass, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(JJJ)J");
  results[2] = (*env)->CallNonvirtualLongMethod(env, obj, superclass, testfunction, x0, x1, x2);

  (*env)->ReleaseLongArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jlongArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualLongArrayA
  (JNIEnv *env, jobject obj, jlong x0, jlong x1, jlong x2)
{
  jlongArray resultlist= (*env)->NewLongArray(env, 3);
  jlong *results = (*env)->GetLongArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jvalue args[3];

  // build the data array
  args[0].j = x0;
  args[1].j = x1;
  args[2].j = x2;
  // set the return value array
  results[0] = GetNonvirtualLongValueA(env, obj, superclass, "(J)J" ,args);
  results[1] = GetNonvirtualLongValueA(env, obj, superclass, "(JJ)J" ,args);
  results[2] = GetNonvirtualLongValueA(env, obj, superclass, "(JJJ)J" ,args);

  (*env)->ReleaseLongArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jlongArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualLongArrayV
  (JNIEnv *env, jobject obj, jlong x0, jlong x1, jlong x2)
{
  jlongArray resultlist= (*env)->NewLongArray(env, 3);
  jlong *results = (*env)->GetLongArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);

  results[0] = GetNonvirtualLongValueV(env, obj, superclass, "(J)J" ,x0);
  results[1] = GetNonvirtualLongValueV(env, obj, superclass, "(JJ)J" ,x0,x1);
  results[2] = GetNonvirtualLongValueV(env, obj, superclass, "(JJJ)J" ,x0,x1,x2);

  (*env)->ReleaseLongArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jlong GetNonvirtualLongValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args)
{
  jlong result = 0L;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallNonvirtualLongMethodA(env, obj, superclass, testfunction, args);
  return result;
}

jlong GetNonvirtualLongValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...)
{
  jlong result = 0L;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallNonvirtualLongMethodV(env, obj, superclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}



/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/

/***********************************************************************************************************
*
* return an array of floats obtained by calling the CallFloatMethodA and CallFloatMethodV functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    floatArray
 * Signature: (FFF)[F
 */
JNIEXPORT jfloatArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_floatArray
  (JNIEnv *env, jobject obj, jfloat x0, jfloat x1, jfloat x2)
{
  jfloatArray resultlist= (*env)->NewFloatArray(env, 3);
  jfloat *results = (*env)->GetFloatArrayElements(env, resultlist, 0);
  jvalue args[3];
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;


  // build the data array
  args[0].b = x0;
  args[1].b = x1;
  args[2].b = x2;
  // set the return value array
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(F)F");
  results[0] = (*env)->CallFloatMethod(env, obj, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(FF)F");
  results[1] = (*env)->CallFloatMethod(env, obj, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(FFF)F");
  results[2] = (*env)->CallFloatMethod(env, obj, testfunction, x0, x1, x2);

  (*env)->ReleaseFloatArrayElements(env, resultlist, results, 0);
  return resultlist;
}


JNIEXPORT jfloatArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_floatArrayA
  (JNIEnv *env , jobject obj, jfloat x0, jfloat x1, jfloat x2)
{
  jfloatArray resultlist= (*env)->NewFloatArray(env, 3);
  jfloat *results = (*env)->GetFloatArrayElements(env, resultlist, 0);
  jvalue args[3];

  // build the data array
  args[0].f = x0;
  args[1].f = x1;
  args[2].f = x2;
  // set the return value array
  results[0] = GetFloatValueA(env, obj, "(F)F" ,args);
  results[1] = GetFloatValueA(env, obj, "(FF)F" ,args);
  results[2] = GetFloatValueA(env, obj, "(FFF)F" ,args);

  (*env)->ReleaseFloatArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jfloatArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_floatArrayV
  (JNIEnv *env , jobject obj, jfloat x0, jfloat x1, jfloat x2)
{
  jfloatArray resultlist= (*env)->NewFloatArray(env, 3);
  jfloat *results = (*env)->GetFloatArrayElements(env, resultlist, 0);

  results[0] = GetFloatValueV(env, obj, "(F)F" ,x0);
  results[1] = GetFloatValueV(env, obj, "(FF)F" ,x0,x1);
  results[2] = GetFloatValueV(env, obj, "(FFF)F" ,x0,x1,x2);

  (*env)->ReleaseFloatArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jfloat GetFloatValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args)
{
  jfloat result = 0.0f;
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallFloatMethodA(env, obj, testfunction, args);
  return result;
}

jfloat GetFloatValueV(JNIEnv *env, jobject obj, const char* functionsig, ...)
{
  jfloat result = 0.0f;
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallFloatMethodV(env, obj, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}

/***********************************************************************************************************
*
* Idem for static class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    staticFloatArray
 * Signature: (FFFF)[F
 */

JNIEXPORT jfloatArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticFloatArray
  (JNIEnv *env, jobject obj, jfloat x0, jfloat x1, jfloat x2, jfloat x3)
{
  jfloatArray resultlist= (*env)->NewFloatArray(env, 4);
  jfloat *results = (*env)->GetFloatArrayElements(env, resultlist, 0);
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;
  // test static function calling

  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(FF)F");
  results[0] = (*env)->CallStaticFloatMethod(env, testclass, testfunction, x0, x1);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(FFF)F");
  results[1] = (*env)->CallStaticFloatMethod(env, testclass, testfunction, x0, x1, x2);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(FFFF)F");
  results[2] = (*env)->CallStaticFloatMethod(env, testclass, testfunction, x0, x1, x2, x3);
  (*env)->ReleaseFloatArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jfloatArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticFloatArrayA
  (JNIEnv *env, jobject obj, jfloat x0, jfloat x1, jfloat x2, jfloat x3)
{
  jfloatArray resultlist= (*env)->NewFloatArray(env, 3);
  jfloat *results = (*env)->GetFloatArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jvalue args[4];

  // build the data array
  args[0].f = x0;
  args[1].f = x1;
  args[2].f = x2;
  args[3].f = x3;
  // set the return value array
  results[0] = GetStaticFloatValueA(env, testclass, "(FF)F" ,args);
  results[1] = GetStaticFloatValueA(env, testclass, "(FFF)F" ,args);
  results[2] = GetStaticFloatValueA(env, testclass, "(FFFF)F" ,args);

  (*env)->ReleaseFloatArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jfloatArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticFloatArrayV
  (JNIEnv *env, jobject obj, jfloat x0, jfloat x1, jfloat x2, jfloat x3)
{
  jfloatArray resultlist= (*env)->NewFloatArray(env, 3);
  jfloat *results = (*env)->GetFloatArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);

  results[0] = GetStaticFloatValueV(env, testclass, "(FF)F" ,x0,x1);
  results[1] = GetStaticFloatValueV(env, testclass, "(FFF)F" ,x0,x1,x2);
  results[2] = GetStaticFloatValueV(env, testclass, "(FFFF)F" ,x0,x1,x2,x3);

  (*env)->ReleaseFloatArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jfloat GetStaticFloatValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args)
{
  jfloat result = 0.0f;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallStaticFloatMethodA(env, testclass, testfunction, args);
  return result;
}

jfloat GetStaticFloatValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...)
{
  jfloat result = 0.0f;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallStaticFloatMethodV(env, testclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}



/***********************************************************************************************************
*
* Idem for nonvirtual base class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    nonvirtualFloatArray
 * Signature: (FFF)[F
 */

JNIEXPORT jfloatArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualFloatArray
  (JNIEnv *env, jobject obj, jfloat x0, jfloat x1, jfloat x2)
{
  jfloatArray resultlist= (*env)->NewFloatArray(env, 3);
  jfloat *results = (*env)->GetFloatArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jmethodID testfunction;

  // set the return value array
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(F)F");
  results[0] = (*env)->CallNonvirtualFloatMethod(env, obj, superclass, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(FF)F");
  results[1] = (*env)->CallNonvirtualFloatMethod(env, obj, superclass, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(FFF)F");
  results[2] = (*env)->CallNonvirtualFloatMethod(env, obj, superclass, testfunction, x0, x1, x2);

  (*env)->ReleaseFloatArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jfloatArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualFloatArrayA
  (JNIEnv *env, jobject obj, jfloat x0, jfloat x1, jfloat x2)
{
  jfloatArray resultlist= (*env)->NewFloatArray(env, 3);
  jfloat *results = (*env)->GetFloatArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jvalue args[3];

  // build the data array
  args[0].f = x0;
  args[1].f = x1;
  args[2].f = x2;
  // set the return value array
  results[0] = GetNonvirtualFloatValueA(env, obj, superclass, "(F)F" ,args);
  results[1] = GetNonvirtualFloatValueA(env, obj, superclass, "(FF)F" ,args);
  results[2] = GetNonvirtualFloatValueA(env, obj, superclass, "(FFF)F" ,args);

  (*env)->ReleaseFloatArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jfloatArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualFloatArrayV
  (JNIEnv *env, jobject obj, jfloat x0, jfloat x1, jfloat x2)
{
  jfloatArray resultlist= (*env)->NewFloatArray(env, 3);
  jfloat *results = (*env)->GetFloatArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);

  results[0] = GetNonvirtualFloatValueV(env, obj, superclass, "(F)F" ,x0);
  results[1] = GetNonvirtualFloatValueV(env, obj, superclass, "(FF)F" ,x0,x1);
  results[2] = GetNonvirtualFloatValueV(env, obj, superclass, "(FFF)F" ,x0,x1,x2);

  (*env)->ReleaseFloatArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jfloat GetNonvirtualFloatValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args)
{
  jfloat result = 0.0f;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallNonvirtualFloatMethodA(env, obj, superclass, testfunction, args);
  return result;
}

jfloat GetNonvirtualFloatValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...)
{
  jfloat result = 0.0f;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallNonvirtualFloatMethodV(env, obj, superclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}


/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/

/***********************************************************************************************************
*
* return an array of doubles obtained by calling the CallDoubleMethodA and CallDoubleMethodV functions
***********************************************************************************************************/

/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    doubleArray
 * Signature: (DDD)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_doubleArray
  (JNIEnv *env, jobject obj, jdouble x0, jdouble x1, jdouble x2)
{
  jdoubleArray resultlist= (*env)->NewDoubleArray(env, 3);
  jdouble *results = (*env)->GetDoubleArrayElements(env, resultlist, 0);
  jvalue args[3];
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;


  // build the data array
  args[0].b = x0;
  args[1].b = x1;
  args[2].b = x2;
  // set the return value array
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(D)D");
  results[0] = (*env)->CallDoubleMethod(env, obj, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(DD)D");
  results[1] = (*env)->CallDoubleMethod(env, obj, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", "(DDD)D");
  results[2] = (*env)->CallDoubleMethod(env, obj, testfunction, x0, x1, x2);

  (*env)->ReleaseDoubleArrayElements(env, resultlist, results, 0);
  return resultlist;
}



JNIEXPORT jdoubleArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_doubleArrayA
  (JNIEnv *env, jobject obj, jdouble x0, jdouble x1, jdouble x2)
{
  jdoubleArray resultlist= (*env)->NewDoubleArray(env, 3);
  jdouble *results = (*env)->GetDoubleArrayElements(env, resultlist, 0);
  jvalue args[3];

  // build the data array
  args[0].d = x0;
  args[1].d = x1;
  args[2].d = x2;
  // set the return value array
  results[0] = GetDoubleValueA(env, obj, "(D)D" ,args);
  results[1] = GetDoubleValueA(env, obj, "(DD)D" ,args);
  results[2] = GetDoubleValueA(env, obj, "(DDD)D" ,args);

  (*env)->ReleaseDoubleArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jdoubleArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_doubleArrayV
  (JNIEnv *env, jobject obj, jdouble x0, jdouble x1, jdouble x2)
{
  jdoubleArray resultlist= (*env)->NewDoubleArray(env, 3);
  jdouble *results = (*env)->GetDoubleArrayElements(env, resultlist, 0);

  results[0] = GetDoubleValueV(env, obj, "(D)D" ,x0);
  results[1] = GetDoubleValueV(env, obj, "(DD)D" ,x0,x1);
  results[2] = GetDoubleValueV(env, obj, "(DDD)D" ,x0,x1,x2);

  (*env)->ReleaseDoubleArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jdouble GetDoubleValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args)
{
  jdouble result = 0.0;
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallDoubleMethodA(env, obj, testfunction, args);
  return result;
}

jdouble GetDoubleValueV(JNIEnv *env, jobject obj, const char* functionsig, ...)
{
  jdouble result = 0.0;
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "getMaximum", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallDoubleMethodV(env, obj, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}



/***********************************************************************************************************
*
* Idem for static class functions
***********************************************************************************************************/

/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    staticDoubleArray
 * Signature: (DDDD)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticDoubleArray
  (JNIEnv *env, jobject obj, jdouble x0, jdouble x1, jdouble x2, jdouble x3)
{
  jdoubleArray resultlist= (*env)->NewDoubleArray(env, 4);
  jdouble *results = (*env)->GetDoubleArrayElements(env, resultlist, 0);
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;
  // test static function calling

  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(DD)D");
  results[0] = (*env)->CallStaticDoubleMethod(env, testclass, testfunction, x0, x1);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(DDD)D");
  results[1] = (*env)->CallStaticDoubleMethod(env, testclass, testfunction, x0, x1, x2);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", "(DDDD)D");
  results[2] = (*env)->CallStaticDoubleMethod(env, testclass, testfunction, x0, x1, x2, x3);
  (*env)->ReleaseDoubleArrayElements(env, resultlist, results, 0);
  return resultlist;
}


JNIEXPORT jdoubleArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticDoubleArrayA
  (JNIEnv *env, jobject obj, jdouble x0, jdouble x1, jdouble x2, jdouble x3)
 {
  jdoubleArray resultlist= (*env)->NewDoubleArray(env, 3);
  jdouble *results = (*env)->GetDoubleArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jvalue args[4];

  // build the data array
  args[0].d = x0;
  args[1].d = x1;
  args[2].d = x2;
  args[3].d = x3;
  // set the return value array
  results[0] = GetStaticDoubleValueA(env, testclass, "(DD)D" ,args);
  results[1] = GetStaticDoubleValueA(env, testclass, "(DDD)D" ,args);
  results[2] = GetStaticDoubleValueA(env, testclass, "(DDDD)D" ,args);

  (*env)->ReleaseDoubleArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jdoubleArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticDoubleArrayV
  (JNIEnv *env, jobject obj, jdouble x0, jdouble x1, jdouble x2, jdouble x3)
 {
  jdoubleArray resultlist= (*env)->NewDoubleArray(env, 3);
  jdouble *results = (*env)->GetDoubleArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);

  results[0] = GetStaticDoubleValueV(env, testclass, "(DD)D" ,x0,x1);
  results[1] = GetStaticDoubleValueV(env, testclass, "(DDD)D" ,x0,x1,x2);
  results[2] = GetStaticDoubleValueV(env, testclass, "(DDDD)D" ,x0,x1,x2,x3);

  (*env)->ReleaseDoubleArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jdouble GetStaticDoubleValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args)
{
  jdouble result = 0.0;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallStaticDoubleMethodA(env, testclass, testfunction, args);
  return result;
}

jdouble GetStaticDoubleValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...)
{
  jdouble result = 0.0;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "absmax", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallStaticDoubleMethodV(env, testclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}


/***********************************************************************************************************
*
* Idem for nonvirtual base class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    nonvirtualDoubleArray
 * Signature: (DDD)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualDoubleArray
  (JNIEnv *env, jobject obj, jdouble x0, jdouble x1, jdouble x2)
{
  jdoubleArray resultlist= (*env)->NewDoubleArray(env, 3);
  jdouble *results = (*env)->GetDoubleArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jmethodID testfunction;

  // set the return value array
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(D)D");
  results[0] = (*env)->CallNonvirtualDoubleMethod(env, obj, superclass, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(DD)D");
  results[1] = (*env)->CallNonvirtualDoubleMethod(env, obj, superclass, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", "(DDD)D");
  results[2] = (*env)->CallNonvirtualDoubleMethod(env, obj, superclass, testfunction, x0, x1, x2);

  (*env)->ReleaseDoubleArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jdoubleArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualDoubleArrayA
  (JNIEnv *env, jobject obj, jdouble x0, jdouble x1, jdouble x2)
{
  jdoubleArray resultlist= (*env)->NewDoubleArray(env, 3);
  jdouble *results = (*env)->GetDoubleArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jvalue args[3];

  // build the data array
  args[0].d = x0;
  args[1].d = x1;
  args[2].d = x2;
  // set the return value array
  results[0] = GetNonvirtualDoubleValueA(env, obj, superclass, "(D)D" ,args);
  results[1] = GetNonvirtualDoubleValueA(env, obj, superclass, "(DD)D" ,args);
  results[2] = GetNonvirtualDoubleValueA(env, obj, superclass, "(DDD)D" ,args);

  (*env)->ReleaseDoubleArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jdoubleArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualDoubleArrayV
  (JNIEnv *env, jobject obj, jdouble x0, jdouble x1, jdouble x2)
{
  jdoubleArray resultlist= (*env)->NewDoubleArray(env, 3);
  jdouble *results = (*env)->GetDoubleArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);

  results[0] = GetNonvirtualDoubleValueV(env, obj, superclass, "(D)D" ,x0);
  results[1] = GetNonvirtualDoubleValueV(env, obj, superclass, "(DD)D" ,x0,x1);
  results[2] = GetNonvirtualDoubleValueV(env, obj, superclass, "(DDD)D" ,x0,x1,x2);

  (*env)->ReleaseDoubleArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jdouble GetNonvirtualDoubleValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args)
{
  jdouble result = 0.0;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallNonvirtualDoubleMethodA(env, obj, superclass, testfunction, args);
  return result;
}

jdouble GetNonvirtualDoubleValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...)
{
  jdouble result = 0.0;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "getMaximum", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallNonvirtualDoubleMethodV(env, obj, superclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}


/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************/
/***********************************************************************************************************
*
* return an array of booleans obtained by calling the CallBooleanMethodA and CallBooleanMethodV functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    booleanArray
 * Signature: (ZZZ)[Z
 */
JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_booleanArray
  (JNIEnv *env, jobject obj, jboolean x0, jboolean x1, jboolean x2)
{
  jbooleanArray resultlist= (*env)->NewBooleanArray(env, 3);
  jboolean *results = (*env)->GetBooleanArrayElements(env, resultlist, 0);
  jvalue args[3];
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;


  // build the data array
  args[0].b = x0;
  args[1].b = x1;
  args[2].b = x2;
  // set the return value array
  testfunction = (*env)->GetMethodID(env, testclass, "countBinary", "(Z)Z");
  results[0] = (*env)->CallBooleanMethod(env, obj, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, testclass, "countBinary", "(ZZ)Z");
  results[1] = (*env)->CallBooleanMethod(env, obj, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, testclass, "countBinary", "(ZZZ)Z");
  results[2] = (*env)->CallBooleanMethod(env, obj, testfunction, x0, x1, x2);

  (*env)->ReleaseBooleanArrayElements(env, resultlist, results, 0);
  return resultlist;
}


JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_booleanArrayA
  (JNIEnv *env, jobject obj, jboolean x0, jboolean x1, jboolean x2)
{
  jbooleanArray resultlist= (*env)->NewBooleanArray(env, 3);
  jboolean *results = (*env)->GetBooleanArrayElements(env, resultlist, 0);
  jvalue args[3];

  // build the data array
  args[0].z = x0;
  args[1].z = x1;
  args[2].z = x2;
  // set the return value array
  results[0] = GetBooleanValueA(env, obj, "(Z)Z" ,args);
  results[1] = GetBooleanValueA(env, obj, "(ZZ)Z" ,args);
  results[2] = GetBooleanValueA(env, obj, "(ZZZ)Z" ,args);

  (*env)->ReleaseBooleanArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_booleanArrayV
  (JNIEnv *env, jobject obj, jboolean x0, jboolean x1, jboolean x2)
{
  jbooleanArray resultlist= (*env)->NewBooleanArray(env, 3);
  jboolean *results = (*env)->GetBooleanArrayElements(env, resultlist, 0);

  // set the return value array
  results[0] = GetBooleanValueV(env, obj, "(Z)Z" ,x0);
  results[1] = GetBooleanValueV(env, obj, "(ZZ)Z" ,x0,x1);
  results[2] = GetBooleanValueV(env, obj, "(ZZZ)Z" ,x0,x1,x2);

  (*env)->ReleaseBooleanArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jboolean GetBooleanValueA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args)
{
  jboolean result = JNI_FALSE;
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "countBinary", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallBooleanMethodA(env, obj, testfunction, args);
  return result;
}

jboolean GetBooleanValueV(JNIEnv *env, jobject obj, const char* functionsig, ...)
{
  jboolean result = JNI_FALSE;
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction = (*env)->GetMethodID(env, testclass, "countBinary", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallBooleanMethodV(env, obj, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}


/***********************************************************************************************************
*
* Idem for static class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    staticBooleanArray
 * Signature: (ZZZZ)[Z
 */
JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticBooleanArray
  (JNIEnv *env, jobject obj, jboolean x0, jboolean x1, jboolean x2, jboolean x3)
{
  jbooleanArray resultlist= (*env)->NewBooleanArray(env, 4);
  jboolean *results = (*env)->GetBooleanArrayElements(env, resultlist, 0);
  jclass testclass = (*env)->GetObjectClass(env, obj);
  jmethodID testfunction;
  // test static function calling

  testfunction = (*env)->GetStaticMethodID(env, testclass, "binary", "(ZZ)Z");
  results[0] = (*env)->CallStaticBooleanMethod(env, testclass, testfunction, x0, x1);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "binary", "(ZZZ)Z");
  results[1] = (*env)->CallStaticBooleanMethod(env, testclass, testfunction, x0, x1, x2);
  testfunction = (*env)->GetStaticMethodID(env, testclass, "binary", "(ZZZZ)Z");
  results[2] = (*env)->CallStaticBooleanMethod(env, testclass, testfunction, x0, x1, x2, x3);
  (*env)->ReleaseBooleanArrayElements(env, resultlist, results, 0);
  return resultlist;
}


JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticBooleanArrayA
  (JNIEnv *env, jobject obj, jboolean x0, jboolean x1, jboolean x2, jboolean x3)
{
  jbooleanArray resultlist= (*env)->NewBooleanArray(env, 3);
  jboolean *results = (*env)->GetBooleanArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jvalue args[4];

  // build the data array
  args[0].z = x0;
  args[1].z = x1;
  args[2].z = x2;
  args[3].z = x3;
  // set the return value array
  results[0] = GetStaticBooleanValueA(env, testclass, "(ZZ)Z" ,args);
  results[1] = GetStaticBooleanValueA(env, testclass, "(ZZZ)Z" ,args);
  results[2] = GetStaticBooleanValueA(env, testclass, "(ZZZZ)Z" ,args);

  (*env)->ReleaseBooleanArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_staticBooleanArrayV
  (JNIEnv *env, jobject obj, jboolean x0, jboolean x1, jboolean x2, jboolean x3)
{
  jbooleanArray resultlist= (*env)->NewBooleanArray(env, 3);
  jboolean *results = (*env)->GetBooleanArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);

  // set the return value array
  results[0] = GetStaticBooleanValueV(env, testclass, "(ZZ)Z" ,x0,x1);
  results[1] = GetStaticBooleanValueV(env, testclass, "(ZZZ)Z" ,x0,x1,x2);
  results[2] = GetStaticBooleanValueV(env, testclass, "(ZZZZ)Z" ,x0,x1,x2,x3);

  (*env)->ReleaseBooleanArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jboolean GetStaticBooleanValueA(JNIEnv *env, jclass testclass, const char* functionsig, jvalue* args)
{
  jboolean result = JNI_FALSE;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "binary", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallStaticBooleanMethodA(env, testclass, testfunction, args);
  return result;
}

jboolean GetStaticBooleanValueV(JNIEnv *env, jclass testclass, const char* functionsig, ...)
{
  jboolean result = JNI_FALSE;
  jmethodID testfunction = (*env)->GetStaticMethodID(env, testclass, "binary", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallStaticBooleanMethodV(env, testclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}


/***********************************************************************************************************
*
* Idem for nonvirtual base class functions
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest
 * Method:    nonvirtualBooleanArray
 * Signature: (ZZZ)[Z
 */
JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualBooleanArray
  (JNIEnv *env, jobject obj, jboolean x0, jboolean x1, jboolean x2)
{
  jbooleanArray resultlist= (*env)->NewBooleanArray(env, 3);
  jboolean *results = (*env)->GetBooleanArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jmethodID testfunction;

  // set the return value array
  testfunction = (*env)->GetMethodID(env, superclass, "countBinary", "(Z)Z");
  results[0] = (*env)->CallNonvirtualBooleanMethod(env, obj, superclass, testfunction, x0);
  testfunction = (*env)->GetMethodID(env, superclass, "countBinary", "(ZZ)Z");
  results[1] = (*env)->CallNonvirtualBooleanMethod(env, obj, superclass, testfunction, x0, x1);
  testfunction = (*env)->GetMethodID(env, superclass, "countBinary", "(ZZZ)Z");
  results[2] = (*env)->CallNonvirtualBooleanMethod(env, obj, superclass, testfunction, x0, x1, x2);

  (*env)->ReleaseBooleanArrayElements(env, resultlist, results, 0);
  return resultlist;
}


JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualBooleanArrayA
  (JNIEnv *env, jobject obj, jboolean x0, jboolean x1, jboolean x2)
{
  jbooleanArray resultlist= (*env)->NewBooleanArray(env, 3);
  jboolean *results = (*env)->GetBooleanArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);
  jvalue args[3];

  // build the data array
  args[0].z = x0;
  args[1].z = x1;
  args[2].z = x2;
  // set the return value array
  results[0] = GetNonvirtualBooleanValueA(env, obj, superclass, "(Z)Z" ,args);
  results[1] = GetNonvirtualBooleanValueA(env, obj, superclass, "(ZZ)Z" ,args);
  results[2] = GetNonvirtualBooleanValueA(env, obj, superclass, "(ZZZ)Z" ,args);

  (*env)->ReleaseBooleanArrayElements(env, resultlist, results, 0);
  return resultlist;
}

JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIPrimitiveFunctionAccessTest_nonvirtualBooleanArrayV
  (JNIEnv *env, jobject obj, jboolean x0, jboolean x1, jboolean x2)
{
  jbooleanArray resultlist= (*env)->NewBooleanArray(env, 3);
  jboolean *results = (*env)->GetBooleanArrayElements(env, resultlist, 0);
  jclass testclass= (*env)->GetObjectClass(env, obj);
  jclass superclass = (*env)->GetSuperclass(env,testclass);

  // set the return value array
  results[0] = GetNonvirtualBooleanValueV(env, obj, superclass, "(Z)Z" ,x0);
  results[1] = GetNonvirtualBooleanValueV(env, obj, superclass, "(ZZ)Z" ,x0,x1);
  results[2] = GetNonvirtualBooleanValueV(env, obj, superclass, "(ZZZ)Z" ,x0,x1,x2);

  (*env)->ReleaseBooleanArrayElements(env, resultlist, results, 0);
  return resultlist;
}

jboolean GetNonvirtualBooleanValueA(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, jvalue* args)
{
  jboolean result = JNI_FALSE;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "countBinary", functionsig);
  if (testfunction != NULL)
    result = (*env)->CallNonvirtualBooleanMethodA(env, obj, superclass, testfunction, args);
  return result;
}

jboolean GetNonvirtualBooleanValueV(JNIEnv *env, jobject obj, jclass superclass, const char* functionsig, ...)
{
  jboolean result = JNI_FALSE;
  jmethodID testfunction = (*env)->GetMethodID(env, superclass, "countBinary", functionsig);
  va_list arglist;
  if (testfunction != NULL)
  {
    va_start(arglist, functionsig);
    result = (*env)->CallNonvirtualBooleanMethodV(env, obj, superclass, testfunction, arglist);
    va_end(arglist);
  }
  return result;
}


