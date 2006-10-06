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


/* Cccode for the native class access functions */
#include <jni.h>
//#include "gnu_testlet_wonka_jni_JNIClassAccessTest.h"


/****************************************************************************************************************
 * inplementations for the class acccess functions for classs JNIClassAccessTest                                *
 ****************************************************************************************************************/


/****************************************************************************************************************
* Access the variables from a given SimpleContainer
* the variables to access...
*    int                number;         I
*    java.lang.String   name;           Ljava/lang/String;
*    boolean            preferences[];  [Z
*    static int         common          I
****************************************************************************************************************/

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    getClassInteger
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_getClassInteger__Lgnu_testlet_wonka_jni_SimpleContainer_2
  (JNIEnv *env, jobject obj, jobject container)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jfieldID field = (*env)->GetFieldID(env, cls, "number", "I");
  jint number;
  if (field != NULL)
    number = (*env)->GetIntField(env, container, field);
  else
    number = -1;
  return number;
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    setClassInteger
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;I)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_setClassInteger__Lgnu_testlet_wonka_jni_SimpleContainer_2I
  (JNIEnv *env, jobject obj, jobject container, jint number)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jfieldID field = (*env)->GetFieldID(env, cls, "number", "I");
  if (field != NULL)
    (*env)->SetIntField(env, container, field, number);

  return;
}
/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    getClassString
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_getClassString__Lgnu_testlet_wonka_jni_SimpleContainer_2
  (JNIEnv *env, jobject obj, jobject container)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jfieldID field = (*env)->GetFieldID(env, cls, "name", "Ljava/lang/String;");
  jstring name;
  if (field != NULL)
    name = (jstring)(*env)->GetObjectField(env, container, field);
  else
    name = NULL;
  return name;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    setClassString
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_setClassString__Lgnu_testlet_wonka_jni_SimpleContainer_2Ljava_lang_String_2
  (JNIEnv *env, jobject obj, jobject container, jstring name)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jfieldID field = (*env)->GetFieldID(env, cls, "name", "Ljava/lang/String;");
  if (field != NULL)
    (*env)->SetObjectField(env, container, field, name);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    getClassArray
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;)[Z
 */
JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_getClassArray__Lgnu_testlet_wonka_jni_SimpleContainer_2
  (JNIEnv *env, jobject obj, jobject container)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jfieldID field = (*env)->GetFieldID(env, cls, "preferences", "[Z");
  jbooleanArray preferences;
  if (field != NULL)
    preferences = (jbooleanArray)(*env)->GetObjectField(env, container, field);
  else
    preferences = (*env)->NewBooleanArray(env, 3);
  return preferences;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    setClassArray
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;[Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_setClassArray__Lgnu_testlet_wonka_jni_SimpleContainer_2_3Z
  (JNIEnv *env, jobject obj, jobject container, jbooleanArray preferences)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jfieldID field = (*env)->GetFieldID(env, cls, "preferences", "[Z");
  if (field != NULL)
    (*env)->SetObjectField(env, container, field, preferences);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    setClassArray
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;ZZZ)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_setClassArray__Lgnu_testlet_wonka_jni_SimpleContainer_2ZZZ
  (JNIEnv *env, jobject obj, jobject container, jboolean b0, jboolean b1, jboolean b2)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jfieldID field = (*env)->GetFieldID(env, cls, "preferences", "[Z");
  if (field != NULL)
  {
    jboolean iscopy;
    jbooleanArray newarray = (*env)->NewBooleanArray(env, 3);
    jboolean *arrayelements = (*env)->GetBooleanArrayElements(env, newarray, &iscopy);
    // fill the array with data
    arrayelements[0] =b0;
    arrayelements[1] =b1;
    arrayelements[2] =b2;

    // release data into array
    if (iscopy)
      (*env)->ReleaseBooleanArrayElements(env, newarray, arrayelements, 0);

    // array to class member
    (*env)->SetObjectField(env, container, field, newarray);
  }
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    getClassStatic
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_getClassStatic__Lgnu_testlet_wonka_jni_SimpleContainer_2
  (JNIEnv *env, jobject obj, jobject container)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jfieldID field = (*env)->GetStaticFieldID(env, cls, "common", "I");
  jint common;
  if (field != NULL)
    common = (*env)->GetStaticIntField(env, cls, field);
  else
    common = -1;

  return common;
}

 /*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    setClassStatic
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;I)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_setClassStatic__Lgnu_testlet_wonka_jni_SimpleContainer_2I
  (JNIEnv *env, jobject obj, jobject container, jint common)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jfieldID field = (*env)->GetStaticFieldID(env, cls, "common", "I");
  if (field != NULL)
    (*env)->SetStaticIntField(env, cls, field, common);
}


/****************************************************************************************************************
* Access the functions from a given FunctionContainer
* the functions to call...
*    int                                      getNumber;      ()I
*    void                                     setNumber       (I)V
*    java.lang.String                         getName         ()Ljava/lang/String;
*    void                                     setName         (Ljava/lang/String)V
*    boolean[]                                getPreferences  ()[Z
*    void                                     setPreferences  ([z)V
*    Lgnu/testlet/WonkaTest/SimpleContainer;  getInternal     ()Lgnu/testlet/WonkaTest/SimpleContainer;
*    void                                     setInternal     (Lgnu/testlet/WonkaTest/SimpleContainer;)V
****************************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    getClassInteger
 * Signature: (Lgnu/testlet/wonka/jni/FunctionContainer;)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_getClassInteger__Lgnu_testlet_wonka_jni_FunctionContainer_2
  (JNIEnv *env, jobject obj, jobject container)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jmethodID method = (*env)->GetMethodID(env, cls, "getNumber", "()I");
  jint number;
  if (method != NULL)
    number = (*env)->CallIntMethod(env, container, method);
  else
    number = -1;
  return number;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    setClassInteger
 * Signature: (Lgnu/testlet/wonka/jni/FunctionContainer;I)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_setClassInteger__Lgnu_testlet_wonka_jni_FunctionContainer_2I
  (JNIEnv *env, jobject obj, jobject container, jint number)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jmethodID method = (*env)->GetMethodID(env, cls, "setNumber", "(I)V");
  if (method != NULL)
    (*env)->CallVoidMethod(env, container, method, number);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    getClassString
 * Signature: (Lgnu/testlet/wonka/jni/FunctionContainer;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_getClassString__Lgnu_testlet_wonka_jni_FunctionContainer_2
  (JNIEnv *env, jobject obj, jobject container)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jmethodID method = (*env)->GetMethodID(env, cls, "getName", "()Ljava/lang/String;");
  jstring name;

  if (method != NULL)
    name = (jstring)(*env)->CallObjectMethod(env, container, method);
  else
    name = NULL;
  return name;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    setClassString
 * Signature: (Lgnu/testlet/wonka/jni/FunctionContainer;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_setClassString__Lgnu_testlet_wonka_jni_FunctionContainer_2Ljava_lang_String_2
  (JNIEnv *env, jobject obj, jobject container, jstring name)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jmethodID method = (*env)->GetMethodID(env, cls, "setName", "(Ljava/lang/String;)V");
  if (method != NULL)
    (*env)->CallVoidMethod(env, container, method, name);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    getClassArray
 * Signature: (Lgnu/testlet/wonka/jni/FunctionContainer;)[Z
 */
JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_getClassArray__Lgnu_testlet_wonka_jni_FunctionContainer_2
  (JNIEnv *env, jobject obj, jobject container)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jmethodID method = (*env)->GetMethodID(env, cls, "getPreferences", "()[Z");
  jbooleanArray preferences;

  if (method != NULL)
    preferences = (jbooleanArray)(*env)->CallObjectMethod(env, container, method);
  else
    preferences = (*env)->NewBooleanArray(env,3);
  return preferences;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    setClassArray
 * Signature: (Lgnu/testlet/wonka/jni/FunctionContainer;[Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_setClassArray__Lgnu_testlet_wonka_jni_FunctionContainer_2_3Z
  (JNIEnv *env, jobject obj, jobject container, jbooleanArray preferences)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jmethodID method = (*env)->GetMethodID(env, cls, "setPreferences", "([Z)V");
  if (method != NULL)
    (*env)->CallVoidMethod(env, container, method, preferences);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    setClassArray
 * Signature: (Lgnu/testlet/wonka/jni/FunctionContainer;ZZZ)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_setClassArray__Lgnu_testlet_wonka_jni_FunctionContainer_2ZZZ
  (JNIEnv *env, jobject obj, jobject container, jboolean p0, jboolean p1, jboolean p2)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jmethodID method = (*env)->GetMethodID(env, cls, "setPreferences", "(ZZZ)V");
  if (method != NULL)
    (*env)->CallVoidMethod(env, container, method, p0, p1, p2);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    getClassContainer
 * Signature: (Lgnu/testlet/wonka/jni/FunctionContainer;)Lgnu/testlet/wonka/jni/SimpleContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_getClassContainer
  (JNIEnv *env, jobject obj, jobject container)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jmethodID method = (*env)->GetMethodID(env, cls, "getInternal", "()Lgnu/testlet/wonka/jni/SimpleContainer;");
  jobject internal;

  if (method != NULL)
    internal = (*env)->CallObjectMethod(env, container, method);
  else
    internal = NULL;
  return internal;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    setClassContainer
 * Signature: (Lgnu/testlet/wonka/jni/FunctionContainer;Lgnu/testlet/wonka/jni/SimpleContainer;)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_setClassContainer
  (JNIEnv *env, jobject obj, jobject container, jobject internal)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jmethodID method = (*env)->GetMethodID(env, cls, "setInternal", "(Lgnu/testlet/wonka/jni/SimpleContainer;)V");
  if (method != NULL)
    (*env)->CallVoidMethod(env, container, method, internal);
}

/****************************************************************************************************************
* Access the static member from a SimpleContainer member from a given FunctionContainer
*    int                                      SimpleContainer.common;      I
*    Lgnu/testlet/WonkaTest/SimpleContainer;  getInternal     ()Lgnu/testlet/WonkaTest/SimpleContainer;
*    void                                     setInternal     (Lgnu/testlet/WonkaTest/SimpleContainer;)V
****************************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    getClassStatic
 * Signature: (Lgnu/testlet/wonka/jni/FunctionContainer;)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_getClassStatic__Lgnu_testlet_wonka_jni_FunctionContainer_2
  (JNIEnv *env, jobject obj, jobject functioncontainer)
{
  return Java_gnu_testlet_wonka_jni_JNIClassAccessTest_getClassStatic__Lgnu_testlet_wonka_jni_SimpleContainer_2
  (
    env,
    obj,
    Java_gnu_testlet_wonka_jni_JNIClassAccessTest_getClassContainer(env, obj, functioncontainer)
   );
}
/*
{
  jclass functionclass = (*env)->GetObjectClass(env, functioncontainer);
  jmethodID method = (*env)->GetMethodID(env, functionclass, "getInternal", "()Lgnu/testlet/wonka/jni/SimpleContainer;");
  jint common = -1;

  if (method != NULL)
  {
    jobject simplecontainer = (*env)->CallObjectMethod(env, functioncontainer, method);
    jclass simpleclass = (*env)->GetObjectClass(env, simplecontainer);
    jfieldID field = (*env)->GetStaticFieldID(env, simpleclass, "common", "I");

    if (field != NULL)
      common = (*env)->GetStaticIntField(env, simpleclass, field);
  }
  return common;
}
*/

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    setClassStatic
 * Signature: (Lgnu/testlet/wonka/jni/FunctionContainer;I)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_setClassStatic__Lgnu_testlet_wonka_jni_FunctionContainer_2I
  (JNIEnv *env, jobject obj, jobject functioncontainer, jint common)
{
  Java_gnu_testlet_wonka_jni_JNIClassAccessTest_setClassStatic__Lgnu_testlet_wonka_jni_SimpleContainer_2I
  (
    env,
    obj,
    Java_gnu_testlet_wonka_jni_JNIClassAccessTest_getClassContainer(env, obj, functioncontainer),
    common
  );
}
/*{
  jclass functionclass = (*env)->GetObjectClass(env, functioncontainer);
  jmethodID method = (*env)->GetMethodID(env, functionclass, "getInternal", "()Lgnu/testlet/wonka/jni/SimpleContainer;");

  if (method != NULL)
  {
    jobject simplecontainer = (*env)->CallObjectMethod(env, functioncontainer, method);
    jclass simpleclass = (*env)->GetObjectClass(env, simplecontainer);
    jfieldID field = (*env)->GetStaticFieldID(env, simpleclass, "common", "I");
    if (field != NULL)
      (*env)->SetStaticIntField(env, simpleclass, field, common);
  }
}
*/

/****************************************************************************************************************
* call the static member function from a given MultiFunctionContainer
*    Lgnu/testlet/wonka/jni/SimpleContainer;  buildContainer     (ILjava/lang/String;ZZZ)Lgnu/testlet/wonka/jni/SimpleContainer;
****************************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    buildSimpleContainerStatic
 * Signature: (Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;ZZZ)Lgnu/testlet/wonka/jni/SimpleContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_buildSimpleContainerStatic
  (JNIEnv *env, jobject obj, jobject container, jint i, jstring s, jboolean b0, jboolean b1, jboolean b2)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jmethodID method = (*env)->GetStaticMethodID(env, cls, "simpleContainerStatic", "(ILjava/lang/String;ZZZ)Lgnu/testlet/wonka/jni/SimpleContainer;");
  jobject internal;

  if (method != NULL) 
    internal = (*env)->CallStaticObjectMethod(env, cls, method, i, s, b0, b1, b2);
  else 
    internal = NULL;
  
  return internal;

}

/****************************************************************************************************************
* call the nonvirtual base class function from the ConstructionContainer base class of a given MultiFunctioncontainer
*    void                                     setVariablesPartial     (ILjava/lang/String;ZZZ)V
*    void                                     setVariablesPartial     (ILjava/lang/String;ZZZ)V
****************************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    buildSimpleContainer
 * Signature: (Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;ZZZ)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_buildSimpleContainer
  (JNIEnv *env, jobject obj, jobject container, jint i, jstring s, jboolean b0, jboolean b1, jboolean b2)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jmethodID method = (*env)->GetMethodID(env, cls, "setVariables", "(ILjava/lang/String;ZZZ)V");

  if (method != NULL)
    (*env)->CallVoidMethod(env, container, method, i, s, b0, b1, b2);
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIClassAccessTest
 * Method:    buildSimpleContainerNonvirtual
 * Signature: (Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;ZZZ)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassAccessTest_buildSimpleContainerNonvirtual
  (JNIEnv *env, jobject obj, jobject container, jint i, jstring s, jboolean b0, jboolean b1, jboolean b2)
{
  jclass cls = (*env)->GetObjectClass(env, container);
  jclass supercls = (*env)->GetSuperclass(env, cls);
  jmethodID method = (*env)->GetMethodID(env, supercls, "setVariables", "(ILjava/lang/String;ZZZ)V");

  if (method != NULL)
    (*env)->CallNonvirtualVoidMethod(env, container, supercls, method, i, s, b0, b1, b2);
}



/*****************************************************************************************************************/



