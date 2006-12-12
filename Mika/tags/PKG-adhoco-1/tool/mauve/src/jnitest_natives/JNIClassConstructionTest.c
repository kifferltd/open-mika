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

#include <stdio.h>
#include <jni.h>

/****************************************************************************************/
/****************************************************************************************
  building new instances using default initialisers
****************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    newDefaultSimpleContainer
 * Signature: ()Lgnu/testlet/wonka/jni/SimpleContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_newDefaultSimpleContainer
  (JNIEnv *env, jobject obj)
{
  // getting the desired simplecontainer's class and default initialisator
  jclass simplecontainerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/SimpleContainer");
  jmethodID defaultconstructor = (*env)->GetMethodID(env, simplecontainerclass,"<init>","()V");
  // building and returning the new container
  jobject newsimplecontainer = (*env)->NewObject(env,simplecontainerclass, defaultconstructor);
  return newsimplecontainer;
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    newDefaultFunctionContainer
 * Signature: ()Lgnu/testlet/wonka/jni/FunctionContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_newDefaultFunctionContainer
  (JNIEnv *env, jobject obj)
{
  // getting the desired functioncontainer's class and default initialisator
  jclass simplecontainerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/FunctionContainer");
  jmethodID defaultconstructor = (*env)->GetMethodID(env, simplecontainerclass,"<init>","()V");
  // building and returning the new container
  jobject newfunctioncontainer = (*env)->NewObject(env,simplecontainerclass, defaultconstructor);
  return newfunctioncontainer;
}
/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    newClonedSimpleContainer
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;)Lgnu/testlet/wonka/jni/SimpleContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_newClonedSimpleContainer
  (JNIEnv *env, jobject obj, jobject sourcesimplecontainer)
{
  // getting the container's class and default initialisator
  jclass simplecontainerclass = (*env)->GetObjectClass(env, sourcesimplecontainer);
  jmethodID defaultconstructor = (*env)->GetMethodID(env, simplecontainerclass,"<init>","()V");
  // building and returning the new container
  jobject newsimplecontainer = (*env)->NewObject(env,simplecontainerclass, defaultconstructor);
  return newsimplecontainer;
}
/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    newClonedFunctionContainer
 * Signature: (Lgnu/testlet/wonka/jni/FunctionContainer;)Lgnu/testlet/wonka/jni/FunctionContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_newClonedFunctionContainer
  (JNIEnv *env, jobject obj, jobject sourcefunctioncontainer)
{
return JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_newClonedSimpleContainer(env, obj, sourcefunctioncontainer);
/*
  // getting the container's class and default initialisator
  jclass functioncontainerclass = (*env)->GetObjectClass(env, sourcefunctioncontainer);
  jmethodID defaultconstructor = (*env)->GetMethodID(env, functioncontainerclass,"<init>","()V");
  // building and returning the new container
  jobject newfunctincontainer = (*env)->NewObject(env,functioncontainerclass, defaultconstructor);
  return newfunctionContainer;
*/
}


/****************************************************************************************/
/****************************************************************************************
  building new instances using propper class constructors
****************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    newConstructedContainer
 * Signature: (ILjava/lang/String;ZZZ)Lgnu/testlet/wonka/jni/ConstructionContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_newConstructedContainer__ILjava_lang_String_2ZZZ
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b0, jboolean b1, jboolean b2)
{
  // container class
  jclass containerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/ConstructionContainer");
  // container data-setting function
  jmethodID constructor = (*env)->GetMethodID(env,containerclass, "<init>", "(ILjava/lang/String;ZZZ)V");
  // build new container by direct call to constructor and return'em
  jobject newcontainer =  (*env)->NewObject(env,containerclass, constructor, i, s, b0, b1, b2);
  return newcontainer;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    newConstructedContainer
 * Signature: (ILjava/lang/String;[Z)Lgnu/testlet/wonka/jni/ConstructionContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_newConstructedContainer__ILjava_lang_String_2_3Z
  (JNIEnv *env, jobject obj, jint i, jstring s, jbooleanArray bx)
{
  // container class
  jclass containerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/ConstructionContainer");
  // container data-setting function
  jmethodID constructor = (*env)->GetMethodID(env,containerclass, "<init>", "(ILjava/lang/String;[Z)V");
  // build new container by direct call to constructor and return'em
  jobject newcontainer =  (*env)->NewObject(env,containerclass, constructor, i, s, bx);
  return newcontainer;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    newConstructedContainer
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;)Lgnu/testlet/wonka/jni/ConstructionContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_newConstructedContainer__Lgnu_testlet_wonka_jni_SimpleContainer_2
  (JNIEnv *env, jobject obj, jobject sourcecontainer)
{
  // container class
  jclass containerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/ConstructionContainer");
  // container data-setting function
  jmethodID constructor = (*env)->GetMethodID(env, containerclass, "<init>", "(Lgnu/testlet/wonka/jni/SimpleContainer;)V");
  // build new container by direct call to constructor and return'em
  jobject newcontainer =  (*env)->NewObject(env ,containerclass, constructor, sourcecontainer);
  return newcontainer;
}

 /*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    newAllocatedContainer
 * Signature: (ILjava/lang/String;ZZZ)Lgnu/testlet/wonka/jni/ConstructionContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_newAllocatedContainer__ILjava_lang_String_2ZZZ
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b0, jboolean b1, jboolean b2)
{
  // get container class and allocate memory for new container
  jclass containerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/ConstructionContainer");
  jobject newcontainer =  (*env)->AllocObject(env, containerclass);

  // get container init function and perform on the new container
  jmethodID initialiser = (*env)->GetMethodID(env,containerclass, "<init>", "(ILjava/lang/String;ZZZ)V");
  (*env)->CallVoidMethod(env, newcontainer, initialiser, i, s, b0, b1, b2);

  return newcontainer;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    newAllocatedContainer
 * Signature: (ILjava/lang/String;[Z)Lgnu/testlet/wonka/jni/ConstructionContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_newAllocatedContainer__ILjava_lang_String_2_3Z
  (JNIEnv *env, jobject obj, jint i, jstring s, jbooleanArray bx)
{
  // get container class and allocate memory for new container
  jclass containerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/ConstructionContainer");
  jobject newcontainer =  (*env)->AllocObject(env, containerclass);

  // get container init function and perform on the new container
  jmethodID initialiser = (*env)->GetMethodID(env,containerclass, "<init>", "(ILjava/lang/String;[Z)V");
  (*env)->CallVoidMethod(env, newcontainer, initialiser, i, s, bx);

  return newcontainer;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    newAllocatedContainer
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;)Lgnu/testlet/wonka/jni/ConstructionContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_newAllocatedContainer__Lgnu_testlet_wonka_jni_SimpleContainer_2
  (JNIEnv *env, jobject obj, jobject sourcecontainer)
{
  // get container class and allocate memory for new container
  jclass containerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/ConstructionContainer");
  jobject newcontainer =  (*env)->AllocObject(env, containerclass);

  // get container init function and perform on the new container
  jmethodID initialiser = (*env)->GetMethodID(env,containerclass, "<init>", "(Lgnu/testlet/wonka/jni/SimpleContainer;)V");
  (*env)->CallVoidMethod(env, newcontainer, initialiser, sourcecontainer);

  return newcontainer;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    newInitialisedContainer
 * Signature: (ILjava/lang/String;ZZZ)Lgnu/testlet/wonka/jni/ConstructionContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_newInitialisedContainer__ILjava_lang_String_2ZZZ
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b0, jboolean b1, jboolean b2)
{
  // container class, void constructor and  void container
  jclass containerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/ConstructionContainer");
  jmethodID constructor = (*env)->GetMethodID(env, containerclass, "<init>", "()V");
  jobject newcontainer =  (*env)->NewObject(env ,containerclass, constructor);

  // get container init function and perform on the new container
  jmethodID initialiser = (*env)->GetMethodID(env,containerclass, "setVariables", "(ILjava/lang/String;ZZZ)V");
  (*env)->CallVoidMethod(env, newcontainer, initialiser, i, s, b0, b1, b2);

  return newcontainer;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    newInitialisedContainer
 * Signature: (ILjava/lang/String;[Z)Lgnu/testlet/wonka/jni/ConstructionContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_newInitialisedContainer__ILjava_lang_String_2_3Z
  (JNIEnv *env, jobject obj, jint i, jstring s, jbooleanArray bx)
{
  // container class, void constructor and  void container
  jclass containerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/ConstructionContainer");
  jmethodID constructor = (*env)->GetMethodID(env, containerclass, "<init>", "()V");
  jobject newcontainer =  (*env)->NewObject(env ,containerclass, constructor);

  // get container init function and perform on the new container
  jmethodID initialiser = (*env)->GetMethodID(env,containerclass, "setVariables", "(ILjava/lang/String;[Z)V");
  (*env)->CallVoidMethod(env, newcontainer, initialiser, i, s, bx);

  return newcontainer;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    newInitialisedContainer
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;)Lgnu/testlet/wonka/jni/ConstructionContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_newInitialisedContainer__Lgnu_testlet_wonka_jni_SimpleContainer_2
  (JNIEnv *env, jobject obj, jobject sourcecontainer)
{
  // container class, void constructor and  void container
  jclass containerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/ConstructionContainer");
  jmethodID constructor = (*env)->GetMethodID(env, containerclass, "<init>", "()V");
  jobject newcontainer =  (*env)->NewObject(env ,containerclass, constructor);

  // get container init function and perform on the new container
  jmethodID initialiser = (*env)->GetMethodID(env,containerclass, "setVariables", "(Lgnu/testlet/wonka/jni/SimpleContainer;)V");
  (*env)->CallVoidMethod(env, newcontainer, initialiser, sourcecontainer);

  return newcontainer;
}


/****************************************************************************************/
/****************************************************************************************
  building array of new instances
****************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    buildStaticArray
 * Signature: (Lgnu/testlet/wonka/jni/ConstructionContainer;I)[Lgnu/testlet/wonka/jni/ConstructionContainer;
 */
JNIEXPORT jobjectArray JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_buildStaticArray__Lgnu_testlet_wonka_jni_ConstructionContainer_2I
  (JNIEnv *env, jobject obj, jobject sourcecontainer, jint size)
{
  //get container class
  jclass containerclass = (*env)->GetObjectClass(env, sourcecontainer);
  //build container array from container class and default container
  jobjectArray containerarray = (*env)->NewObjectArray(env, size, containerclass, sourcecontainer);
  return containerarray;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    buildStaticArray
 * Signature: (ILjava/lang/String;ZZZI)[Lgnu/testlet/wonka/jni/ConstructionContainer;
 */
JNIEXPORT jobjectArray JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_buildStaticArray__ILjava_lang_String_2ZZZI
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b0, jboolean b1, jboolean b2, jint size)
{
  // container class and default constructor
  jclass containerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/ConstructionContainer");
  jmethodID constructor =  (*env)->GetMethodID(env,containerclass, "<init>", "(ILjava/lang/String;ZZZ)V");
  // build container out of constructor
  jobject defaultcontainer = (*env)->NewObject(env,containerclass, constructor, i, s, b0, b1, b2);
  // build abnd return array of containers
  jobjectArray containerarray = (*env)->NewObjectArray(env, size, containerclass, defaultcontainer);
  return containerarray;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    buildIncrementalArray
 * Signature: (Lgnu/testlet/wonka/jni/ConstructionContainer;II)[Lgnu/testlet/wonka/jni/ConstructionContainer;
 */
JNIEXPORT jobjectArray JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_buildIncrementalArray__Lgnu_testlet_wonka_jni_ConstructionContainer_2II
  (JNIEnv *env, jobject obj, jobject sourcecontainer, jint incrementingnumber, jint size)
{
  //build container array from container class and default container
  jclass containerclass = (*env)->GetObjectClass(env, sourcecontainer);
  jobjectArray containerarray = (*env)->NewObjectArray(env, size, containerclass, sourcecontainer);
  /** NOTE: in JNI-C aaas in Java, object arrays store a reference to an object rather then a real object.
  Therefore NewObjectArray() just sets you up with a nest of pointers to the same original object, and a change
  in that object will immediately result in a change in all members of the array.
  To avoid this, before setting the new 'number' member of an array's container, we first will have to replace the pointers
  by references to a freshly constructed instance.
  */
  jmethodID constructor = (*env)->GetMethodID(env,containerclass, "<init>", "(Lgnu/testlet/wonka/jni/ConstructionContainer;)V");
  jmethodID setnumber = (*env)->GetMethodID(env,containerclass, "setNumber", "(I)V");

  jobject currentcontainer;
  jint element;
  for(element=0; element<size; element++)
  {
    incrementingnumber++;
    // a completely new container
    currentcontainer = (*env)->NewObject(env, containerclass, constructor, sourcecontainer);
    // its number set to the desired value
    (*env)->CallVoidMethod(env, currentcontainer, setnumber, incrementingnumber );
    // is allocated to the desired position in the array
    (*env)->SetObjectArrayElement(env, containerarray, element, currentcontainer);
  }

  return containerarray;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    buildIncrementalArray
 * Signature: (ILjava/lang/String;ZZZI)[Lgnu/testlet/wonka/jni/ConstructionContainer;
 */
JNIEXPORT jobjectArray JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_buildIncrementalArray__ILjava_lang_String_2ZZZI
  (JNIEnv *env, jobject obj, jint incrementingnumber, jstring s, jboolean b0, jboolean b1, jboolean b2, jint size)
{
  //allocate memory for a ConstructionContainer and build container array from that'stub'
  jclass containerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/ConstructionContainer");
  jobject stub = (*env)->AllocObject(env,containerclass);
  jobjectArray containerarray = (*env)->NewObjectArray(env, size, containerclass, stub);

  // call setting functions to replace the stubs one by one by newly constructed containers
  jint element;
  jobject currentcontainer;
  jmethodID constructor = (*env)->GetMethodID(env,containerclass, "<init>", "(ILjava/lang/String;ZZZ)V");
  for(element=0; element<size; element++)
  {
    incrementingnumber++;
    currentcontainer = (*env)->NewObject(env, containerclass, constructor, incrementingnumber, s, b0, b1, b2);
    (*env)->SetObjectArrayElement(env, containerarray, element, currentcontainer);
  }

  return containerarray;
}



/****************************************************************************************/
/****************************************************************************************
  building and throwing an exception
****************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    throwsNullPointerException
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_throwsNullPointerException
  (JNIEnv *env, jobject obj, jstring errormsg)
{
  jclass exclass;
  jboolean needsrelease;
  const char *str = (*env)->GetStringUTFChars(env, errormsg, &needsrelease);

  exclass = (*env)->FindClass(env, "java/lang/NullPointerException");
  if(exclass != 0)
    (*env)->ThrowNew(env, exclass, str);
  if (needsrelease == JNI_TRUE)
    (*env)->ReleaseStringUTFChars(env, errormsg, str);
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    throwsConstructedContainerException
 * Signature: (ILjava/lang/String;[ZLgnu/testlet/wonka/jni/SimpleContainer;)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_throwsConstructedContainerException
  (JNIEnv *env, jobject obj, jint i, jstring s, jbooleanArray bx, jobject internalcontainer)
{
  jclass exclass;
  jmethodID constructor;
  jthrowable exobject;
  exclass = (*env)->FindClass(env, "gnu/testlet/wonka/jni/ContainerException");
  constructor = (*env)->GetMethodID(env, exclass,"<init>","(ILjava/lang/String;[ZLgnu/testlet/wonka/jni/SimpleContainer;)V");
  exobject=(jthrowable)(*env)->NewObject(env, exclass, constructor, i, s, bx, internalcontainer);
  if(exobject) {
    (*env)->Throw(env, exobject);
  }
}



/****************************************************************************************/
/****************************************************************************************
  handling a received exception
****************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    handlesException
 * Signature: ()Lgnu/testlet/wonka/jni/FunctionContainer;
 */
JNIEXPORT jobject JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_handlesException
  (JNIEnv *env, jobject obj)
{
  jclass currentclass;
  jmethodID failfunction;
  jthrowable exceptionobj;
  jobject reason = NULL;
  jclass exclass;
  jmethodID getfunction;


  currentclass = (*env)->GetObjectClass(env, obj);
  //exception class
  exclass = (*env)->FindClass(env, "gnu/testlet/wonka/jni/ContainerException");
  if( exclass == NULL)
  {
    printf("class <gnu/testlet/wonka/jni/ContainerException> could not be found \n");
    return NULL;
  }
  // exception getContainer function
  getfunction = (*env)->GetMethodID(env, exclass, "getFunctionContainer", "()Lgnu/testlet/wonka/jni/FunctionContainer;");
  if(getfunction == NULL)
  {
printf("no GetFunctionContainer call found \n");
    return NULL;
  }

  failfunction = (*env)->GetMethodID(env, currentclass, "throwsError", "()V");
  // call exception-throwing function
  if(failfunction)
  {
    (*env)->CallVoidMethod(env, obj, failfunction);
  }
  else
  {
printf("no function throwsError found \n");
    return NULL;
  }

  //detect  exception
  exceptionobj = (*env)->ExceptionOccurred(env);
  if (exceptionobj)
  {
    if((*env)->IsInstanceOf(env, exceptionobj, exclass))
    (*env)->ExceptionClear(env);
    {
        reason = (*env)->CallObjectMethod(env, exceptionobj, getfunction);
    }
  }

  return reason;
}


/****************************************************************************************/
/****************************************************************************************
  tests if same, instance-of or assignable
****************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    testAssignable
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_testAssignable
  (JNIEnv *env, jobject obj, jstring test, jstring reference)
{
  jboolean result = JNI_FALSE;
  const char *teststr;
  const char *refstr;
  jclass testclass;
  jclass refclass;
  jclass exclass;

  // convert the strings to jclass
  jboolean  testcopied, refcopied;
  teststr = (*env)->GetStringUTFChars(env, test, &testcopied);
  refstr =  (*env)->GetStringUTFChars(env, reference, &refcopied);
  testclass = (*env)->FindClass(env, teststr);
  refclass = (*env)->FindClass(env, refstr);

  //safety:
  exclass = (*env)->FindClass(env, "java/lang/NullPointerException");
  if(testclass == NULL)
     return JNI_FALSE;

  if(refclass == NULL)
     return JNI_FALSE;

  //do the test
  result = (*env)->IsAssignableFrom(env,testclass, refclass);
  //release memory
  if(testcopied == JNI_TRUE)
    (*env)->ReleaseStringUTFChars(env, test, teststr);
  if(refcopied == JNI_TRUE)
    (*env)->ReleaseStringUTFChars(env, reference, refstr);
  //return result
  return result;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    testInstanceOf
 * Signature: (Lgnu/testlet/wonka/jni/ContainerException;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_testInstanceOf
  (JNIEnv *env, jobject obj, jthrowable testobject, jstring reference)
{
  jboolean result = JNI_FALSE;

  // convert string to  a jclass
  jclass exclass;
  jboolean refcopied;
  const char *refstr =  (*env)->GetStringUTFChars(env, reference, &refcopied);
  jclass refclass = (*env)->FindClass(env, refstr);
  //safety:
  exclass = (*env)->FindClass(env, "java/lang/NullPointerException");
  if(refclass == NULL)
     return JNI_FALSE;
  //do the test
  if((*env)->IsInstanceOf(env, testobject, refclass) )
    result = JNI_TRUE;
  //release memory
  if(refcopied == JNI_TRUE)
    (*env)->ReleaseStringUTFChars(env, reference, refstr);
  //return result
  return result;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIClassConstructionTest
 * Method:    testSame
 * Signature: (Lgnu/testlet/wonka/jni/SimpleContainer;Lgnu/testlet/wonka/jni/SimpleContainer;)Z
 */
JNIEXPORT jboolean JNICALL Java_gnu_testlet_wonka_jni_JNIClassConstructionTest_testSame
  (JNIEnv *env, jobject obj, jobject left, jobject right)
{
  jboolean result = JNI_FALSE;
  if((*env)->IsSameObject(env, left, right) )
    result = JNI_TRUE;
  return result;
}

