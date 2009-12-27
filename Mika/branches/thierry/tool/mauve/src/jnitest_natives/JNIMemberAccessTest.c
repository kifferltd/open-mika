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


/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
//#include "gnu_testlet_wonka_jni_JNIMemberAccessTest.h"



/****************************************************************************************************************
 * inplementations for the native data acccess functions for classs JNIMemberAccessTest                                *
 ****************************************************************************************************************/


/****************************************************************************************************************
* Access the variables from <this> class JNIMemberAccessTest
* the variables to access...
*    int                                    ourInt;             I
*    java.lang.String                       ourString;          Ljava/lang/String;
*    boolean                                ourArray[];         [Z
*    gnu.testlet.WonkaTest.SimpleContainer  ourSimpleContainer;  Lgnu/testlet/wonka/jni/SimpleContainer;
****************************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIMemberAccessTest
 * Method:    getMemberInteger
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_getMemberInteger
  (JNIEnv *env, jobject obj)
{
  jint oldint = 0;
  jclass cls = (*env)->GetObjectClass(env, obj);
  jfieldID field = (*env)->GetFieldID(env, cls, "ourInt", "I");
  if (field != NULL)
    oldint = (*env)->GetIntField(env, obj, field);

  return oldint;
}

/*
 * Class:     JNIMemberAccessTest
 * Method:    setMemberInteger
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_setMemberInteger
  (JNIEnv *env, jobject obj, jint newint)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jfieldID field = (*env)->GetFieldID(env, cls, "ourInt", "I");
  if (field != NULL)
    (*env)->SetIntField(env, obj, field, newint);

  return;
}

/*
 * Class:     JNIMemberAccessTest
 * Method:    getMemberString
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_getMemberString
  (JNIEnv *env, jobject obj)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jfieldID field = (*env)->GetFieldID(env, cls, "ourString", "Ljava/lang/String;");
  jstring oldstring;
  if (field != NULL)
    oldstring = (jstring)((*env)->GetObjectField(env, obj, field));
  else
    oldstring = (*env)->NewStringUTF(env,"field not found");
  return oldstring;
}

/*
 * Class:     JNIMemberAccessTest
 * Method:    setMemberString
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_setMemberString
  (JNIEnv *env, jobject obj, jstring newstring)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jfieldID field = (*env)->GetFieldID(env, cls, "ourString", "Ljava/lang/String;");
  if (field != NULL)
    (*env)->SetObjectField(env, obj, field, newstring);

  return;
}


/*
 * Class:     JNIMemberAccessTest
 * Method:    getMemberArray
 * Signature: ()[Z
 */
JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_getMemberArray
  (JNIEnv *env, jobject obj)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jfieldID field = (*env)->GetFieldID(env, cls, "ourArray", "[Z");
  jbooleanArray oldarray;
  if (field != NULL)
    oldarray = (jbooleanArray)((*env)->GetObjectField(env, obj, field));      ////??????
  else
    oldarray = (*env)->NewBooleanArray(env, 3);
  return oldarray;
}


/*
 * Class:     JNIMemberAccessTest
 * Method:    setMemberArray
 * Signature: ([Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_setMemberArray
  (JNIEnv *env, jobject obj, jbooleanArray newarray)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jfieldID field = (*env)->GetFieldID(env, cls, "ourArray", "[Z");
  if (field != NULL)
    (*env)->SetObjectField(env, obj, field, newarray);   ////??????

  return;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIMemberAccessTest
 * Method:    getMemberSimpleContainer
 * Signature: ()Lgnu/testlet/wonka/jni/SimpleContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_getMemberSimpleContainer
  (JNIEnv *env, jobject obj)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jfieldID field = (*env)->GetFieldID(env, cls, "ourSimpleContainer", "Lgnu/testlet/wonka/jni/SimpleContainer;");
  jobject oldmemberobject;
  if (field != NULL)
    oldmemberobject = (*env)->GetObjectField(env, obj, field);
  else
    oldmemberobject = NULL;
  return oldmemberobject;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIMemberAccessTest
 * Method:    setMemberSimpleContainer
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_setMemberSimpleContainer
  (JNIEnv *env, jobject obj, jobject newmemberobject)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jfieldID field = (*env)->GetFieldID(env, cls, "ourSimpleContainer", "Lgnu/testlet/wonka/jni/SimpleContainer;");
  if (field != NULL)
    (*env)->SetObjectField(env, obj, field, newmemberobject);

  return;
}

/****************************************************************************************************************
* Change the variables from <this> class JNIMemberAccessTest by means of calls to the classes' functions from the native c-routines
* the functions to access...
*    private int getIntegerInverse();                                          ()I
*    private java.lang.String getStringInverse();                              ()Ljava/lang/String;
*    private boolean getArrayInverse()[];                                      ()[Z
*    private gnu.testlet.WonkaTest.SimpleContainer getContainerInverse();      ()Lgnu/testlet/wonka/jni/SimpleContainer;
*
*    private void setIntegerInverse(int);                                      (I)V
*    private void setStringInverse(java.lang.String);                          (Ljava/lang/String;)V
*    private void setArrayInverse(boolean[]);                                  ([Z)V
*    private void setContainerInverse(gnu.testlet.WonkaTest.SimpleContainer);  (Lgnu/testlet/wonka/jni/SimpleContainer;)V
****************************************************************************************************************/
/*
 * Class:     JNIMemberAccessTest
 * Method:    getInverseInteger
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_getInverseInteger
  (JNIEnv *env, jobject obj)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jmethodID method = (*env)->GetMethodID(env, cls, "getIntegerInverse", "()I");
  jint i;
  if (method == 0)
    i = -1;
  else
    i = (*env)->CallIntMethod(env, obj, method);

  return i;
}


/*
 * Class:     JNIMemberAccessTest
 * Method:    setInverseInteger
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_setInverseInteger
  (JNIEnv *env, jobject obj, jint i)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jmethodID method = (*env)->GetMethodID(env, cls, "setIntegerInverse", "(I)V");

  if (method != NULL)
    (*env)->CallVoidMethod(env, obj, method, i);

  return;
}

/*
 * Class:     JNIMemberAccessTest
 * Method:    getInverseString
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_getInverseString
  (JNIEnv *env, jobject obj)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jmethodID method = (*env)->GetMethodID(env, cls, "getStringInverse", "()Ljava/lang/String;");
  jstring s;

  if (method != NULL)
    s = (jstring)((*env)->CallObjectMethod(env, obj, method));
  else
    s = (*env)->NewStringUTF(env,"function not found");
  return s;
}

/*
 * Class:     JNIMemberAccessTest
 * Method:    setInverseString
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_setInverseString
  (JNIEnv *env, jobject obj, jstring s)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jmethodID method = (*env)->GetMethodID(env, cls, "setStringInverse", "(Ljava/lang/String;)V");

  if (method != NULL)
    (*env)->CallVoidMethod(env, obj, method, s);

  return ;
}

/*
 * Class:     JNIMemberAccessTest
 * Method:    getInverseArray
 * Signature: ()[Z
 */
JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_getInverseArray
  (JNIEnv *env, jobject obj)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jmethodID method = (*env)->GetMethodID(env, cls, "getArrayInverse", "()[Z");
  jbooleanArray bx = (*env)->NewBooleanArray(env, 3);

  if (method != NULL)
    bx = (jbooleanArray)((*env)->CallObjectMethod(env, obj, method, bx));

  return bx;
}

/*
 * Class:     JNIMemberAccessTest
 * Method:    setInverseArray
 * Signature: ([Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_setInverseArray
  (JNIEnv *env, jobject obj, jbooleanArray bx)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jmethodID method = (*env)->GetMethodID(env, cls, "setArrayInverse", "([Z)V");

  if (method != NULL)
    (*env)->CallVoidMethod(env, obj, method, bx);

  return;
}


/*
 * Class:     JNIMemberAccessTest
 * Method:    getInverseSimpleContainer
 * Signature: ()Lgnu/testlet/wonka/jni/SimpleContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_getInverseSimpleContainer
  (JNIEnv *env, jobject obj)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jmethodID method = (*env)->GetMethodID(env, cls, "getContainerInverse", "()Lgnu/testlet/wonka/jni/SimpleContainer;");
  jobject container;

  if (method != NULL)
    container= (*env)->CallObjectMethod(env, obj, method);
  else
    container = NULL;

  return container;
}

/*
 * Class:     JNIMemberAccessTest
 * Method:    setInverseSimpleContainer
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_setInverseSimpleContainer
  (JNIEnv *env, jobject obj, jobject container)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jmethodID method = (*env)->GetMethodID(env, cls, "setContainerInverse", "(Lgnu/testlet/wonka/jni/SimpleContainer;)V");

  if (method != NULL)
    (*env)->CallVoidMethod(env, obj, method, container);

  return;
}

/****************************************************************************************************************
* mirror a call to the classes' static functions : access the static functins from within the C-code and return the outcome
*    static int             inverse(int);              (I)I
*    static String          inverse(String);           (Ljava/lang/String;)Ljava/lang/String;
*    static boolean[]       inverse(boolean[]);        ([Z)[Z
*    static SimpleContainer inverse(SimpleContainer);  (Lgnu/testlet/wonka/jni/SimpleContainer;)Lgnu/testlet/wonka/jni/SimpleContainer;
****************************************************************************************************************/
/*
 * Class:     JNIMemberAccessTest
 * Method:    getInverse
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_getInverse__I
  (JNIEnv *env, jobject obj, jint i)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jmethodID method = (*env)->GetStaticMethodID(env, cls, "inverse", "(I)I");
  jint j;

  if (method != NULL)
    j= (*env)->CallStaticIntMethod(env, cls, method, i);
  else
    j=i;
  return j;

}

/*
 * Class:     gnu_testlet_wonka_jni_JNIMemberAccessTest
 * Method:    getInverse
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_getInverse__Ljava_lang_String_2
  (JNIEnv *env, jobject obj, jstring s)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jmethodID method = (*env)->GetStaticMethodID(env, cls, "inverse", "(Ljava/lang/String;)Ljava/lang/String;");
  jstring inverse;

  if (method != NULL)
    inverse = (jstring)((*env)->CallStaticObjectMethod(env, cls, method,s));
  else
    inverse = s;
  return inverse;
}


/*
 * Class:     JNIMemberAccessTest
 * Method:    getInverse
 * Signature: ([Z)[Z
 */
JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_getInverse___3Z
  (JNIEnv *env, jobject obj, jbooleanArray bx)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jmethodID method = (*env)->GetStaticMethodID(env, cls, "inverse", "([Z)[Z");
  jbooleanArray inverse;

  if (method != NULL)
    inverse = (jbooleanArray)((*env)->CallStaticObjectMethod(env, cls, method, bx));
  else
    inverse = bx;

  return inverse;

}


/*
 * Class:     JNIMemberAccessTest
 * Method:    getInverse
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;)Lgnu/testlet/wonka/jni/SimpleContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_getInverse__Lgnu_testlet_wonka_jni_SimpleContainer_2
  (JNIEnv *env, jobject obj, jobject container)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jmethodID method = (*env)->GetStaticMethodID(env, cls, "inverse", "(Lgnu/testlet/wonka/jni/SimpleContainer;)Lgnu/testlet/wonka/jni/SimpleContainer;");
  jobject inverse;

  if (method != NULL)
    inverse = (*env)->CallStaticObjectMethod(env, cls, method, container);
  else
    inverse = container;
  return inverse;

}



/****************************************************************************************************************
* Call the functions 'byte getVersion()' function. THis function is defined in the base class ContainerFunctions
* and overwritten in the JNIMemberAccessTest, in such a way that it returns '1' in the base class and '2' in the derived
* we'll call both the base  and the derived function and pass the results back to the java class to compare
****************************************************************************************************************/
/*
 * Class:     JNIMemberAccessTest
 * Method:    getCurrentVersion
 * Signature: ()B
 */
JNIEXPORT jbyte JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_getCurrentVersion
  (JNIEnv *env, jobject obj)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jmethodID method = (*env)->GetMethodID(env, cls, "getVersion", "()B");
  jbyte version = 0;

  if (method != NULL)
    version = (*env)->CallByteMethod(env, obj, method);

  return version;
}


/*
 * Class:     JNIMemberAccessTest
 * Method:    getBaseVersion
 * Signature: ()B
 */
JNIEXPORT jbyte JNICALL Java_gnu_testlet_wonka_jni_JNIMemberAccessTest_getBaseVersion
  (JNIEnv *env, jobject obj)
{
  jclass cls = (*env)->GetObjectClass(env, obj);
  jclass supercls = (*env)->GetSuperclass(env, cls);
  jmethodID method = (*env)->GetMethodID(env, supercls, "getVersion", "()B");
  jbyte version = 0;

  if (method != NULL)
    version = (*env)->CallNonvirtualByteMethod(env, obj, supercls, method);

  return version;
}




