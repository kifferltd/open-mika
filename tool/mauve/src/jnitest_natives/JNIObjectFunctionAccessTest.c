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
#include <stdio.h>
#include <jni.h>
//#include "gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest.h"

/***********************************************************************************************************
*
*  Declaring the assigning functions
***********************************************************************************************************/
void BuildContainerA(JNIEnv *env, jobject obj, jclass functionclass, const char* functionsig, const char* targetname, jvalue* args);
void BuildContainerV(JNIEnv *env, jobject obj, jclass functionclass, const char* functionsig, const char* targetname, ...);

void SetContainerA(JNIEnv *env, jobject obj, const char* functionsig, const char* targetname, jvalue* args);
void SetContainerV(JNIEnv *env, jobject obj, const char* functionsig, const char* targetname, ...);
void SetStaticContainerA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args);
void SetStaticContainerV(JNIEnv *env, jobject obj, const char* functionsig, ...);
void SetNonvirtualContainerA(JNIEnv *env, jobject obj, const char* functionsig, const char* targetname, jvalue* args);
void SetNonvirtualContainerV(JNIEnv *env, jobject obj, const char* functionsig, const char* targetname, ...);

void BuildSimpleContainerA(JNIEnv *env, jobject obj, const char* functionsig, const char* sourcename, const char* targetname, jvalue* args);
void BuildSimpleContainerV(JNIEnv *env, jobject obj, const char* functionsig, const char* sourcename, const char* targetname, ...);
void BuildSimpleStaticContainerA(JNIEnv *env, jobject obj, const char* functionsig, const char* targetname, jvalue* args);
void BuildSimpleStaticContainerV(JNIEnv *env, jobject obj, const char* functionsig, const char* targetname, ...);
void BuildSimpleNonvirtualContainerA(JNIEnv *env, jobject obj, const char* functionsig,
                                                                              const char* sourcename, const char* targetname, jvalue* args);
void BuildSimpleNonvirtualContainerV(JNIEnv *env, jobject obj, const char* functionsig, const char* sourcename, const char* targetname, ...);

//return an object representing the demanded cm1-cm5, cs1-cs5 variable of the main JNIObjectFunctionAccess class
jobject getVariableObject(JNIEnv *env, jobject obj, const char* variablename)
{
  //this class
  jclass thisclass= (*env)->GetObjectClass(env, obj);//FindClass(env, "gnu/testlet/wonka/jni/JNIObjectFunctionAccess");
  jfieldID variablefield= (*env)->GetFieldID(env, thisclass, variablename, "Lgnu/testlet/wonka/jni/MultiFunctionContainer;");// field
  jobject variableobject = NULL; // crash safety
  if(variablefield)
    variableobject = (*env)->GetObjectField(env, obj, variablefield);  // the object
  return variableobject;
}

void setVariableObject(JNIEnv *env, jobject obj, const char* variablename, jobject newvalue)
{
  jclass thiscls = (*env)->GetObjectClass(env, obj);
  jfieldID targetfield = (*env)->GetFieldID(env, thiscls, variablename, "Lgnu/testlet/wonka/jni/SimpleContainer;");
  // Set value to field
  if(targetfield)
    (*env)->SetObjectField(env, obj, targetfield, newvalue);
}
/***********************************************************************************************************
*
*  Build five containers and paste them into the classes variable
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest
 * Method:    buildFiveContainersA
 * Signature: (ILjava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest_buildFiveContainersA
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b)
{
  jclass containerclass = (*env)->FindClass(env, "gnu/testlet/wonka/jni/MultiFunctionContainer");
  jvalue args[5];

  // build the data array
  args[0].i = i;
  args[1].l = s;
  args[2].z = b;
  args[3].z = b;
  args[4].z = b;

  // call the different NewObjectA constructor functions tor the different ca-x containers
  BuildContainerA(env, obj, containerclass, "(I)V",                  "cm1", args);
  BuildContainerA(env, obj, containerclass, "(ILjava/lang/String;)V", "cm2", args);
  BuildContainerA(env, obj, containerclass, "(ILjava/lang/String;Z)V", "cm3", args);
  BuildContainerA(env, obj, containerclass, "(ILjava/lang/String;ZZ)V", "cm4", args);
  BuildContainerA(env, obj, containerclass, "(ILjava/lang/String;ZZZ)V", "cm5", args);

/*  // build a container using the given data
  containerConstructor = (*env)->GetMethodId(env, containerclass, "<init>", "(ILjava/lang/String;ZZZ)V");
  containerobject = (*env)->NewObjectA(containerclass, containerconstructor, args);
  // assign the container to the ca-x variables
  testcontainerfield = (*env)->getFieldID(objectaccesstest, "ca1","lgnu/testlet/wonka/jni/MultiFunctionContainer;");
  (*env)->SetObjectField(env, testobject, testcontainerfield, containerobject);
*/

}

void BuildContainerA(JNIEnv *env, jobject obj, jclass functionclass, const char* functionsig, const char* targetname, jvalue* args)
{
  // Get desired constructor and construct object
  jmethodID containerconstructor = (*env)->GetMethodID(env, functionclass, "<init>", functionsig);
  jobject containerobject = (*env)->NewObjectA(env, functionclass, containerconstructor, args);

  // assign the container to the desired variables
  jclass targetclass = (*env)->GetObjectClass(env, obj);
  jfieldID targetfield= (*env)->GetFieldID(env, targetclass, targetname,"Lgnu/testlet/wonka/jni/MultiFunctionContainer;");
  (*env)->SetObjectField(env, obj, targetfield, containerobject);
}



/*
 * Class:     gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest
 * Method:    buildFiveContainersV
 * Signature: (ILjava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest_buildFiveContainersV
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b)
{
  jclass containerclass;
  containerclass = (*env)->FindClass(env, "gnu/testlet/wonka/jni/MultiFunctionContainer");

  // call the different NewObjectV constructors for the different cv-x container variables
  BuildContainerV(env, obj, containerclass, "(I)V",                  "cm1", i);
  BuildContainerV(env, obj, containerclass, "(ILjava/lang/String;)V", "cm2", i, s);
  BuildContainerV(env, obj, containerclass, "(ILjava/lang/String;Z)V", "cm3", i, s, b);
  BuildContainerV(env, obj, containerclass, "(ILjava/lang/String;ZZ)V", "cm4", i, s, b, b);
  BuildContainerV(env, obj, containerclass, "(ILjava/lang/String;ZZZ)V", "cm5", i, s, b, b, b);

}

void BuildContainerV(JNIEnv *env, jobject obj, jclass functionclass, const char* functionsig, const char* targetname, ...)
{
  // Get desired method and field id
  jmethodID containerconstructor = (*env)->GetMethodID(env, functionclass, "<init>", functionsig);
  jclass targetclass = (*env)->GetObjectClass(env, obj);
  jfieldID targetfield = (*env)->GetFieldID(env, targetclass, targetname,"Lgnu/testlet/wonka/jni/MultiFunctionContainer;");

  //get the argument list
  jobject containerobject;
  va_list arglist;
  va_start(arglist, targetname);
  containerobject = (*env)->NewObjectV(env, functionclass, containerconstructor, arglist);
  va_end(arglist);

  // assign the container to the desired variables
  (*env)->SetObjectField(env, obj, targetfield, containerobject);
}

/***********************************************************************************************************
*
*  Perform a void <variable-set> functions on all five MultiFunctionContainers
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest
 * Method:    SetVoidFiveContainersA
 * Signature: (ILjava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest_SetVoidFiveContainersA
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b)
{
  jvalue args[5];

  // build the data array
  args[0].i = i;
  args[1].l = s;
  args[2].z = b;
  args[3].z = b;
  args[4].z = b;

  // call the different CallObjectV setting functions for the different cv-x container variables
  SetContainerA(env, obj, "(I)V",                  "cm1", args);
  SetContainerA(env, obj, "(ILjava/lang/String;)V", "cm2", args);
  SetContainerA(env, obj, "(ILjava/lang/String;Z)V", "cm3", args);
  SetContainerA(env, obj, "(ILjava/lang/String;ZZ)V", "cm4", args);
  SetContainerA(env, obj, "(ILjava/lang/String;ZZZ)V", "cm5", args);
}

void SetContainerA(JNIEnv *env, jobject obj, const char* functionsig, const char* targetname, jvalue* args)
{
  // test environment MultiFunctionContainer object to perform function on
  jobject targetcontainer = getVariableObject(env, obj, targetname);

  // On this object, perform "setVariablesPartial",  using CallVoidMethodA
  jclass targetclass = (*env)->GetObjectClass(env, targetcontainer); //object class = Lgnu/testlet/wonka/jni/MultiFunctionContainer;
  jmethodID containerfunction = (*env)->GetMethodID(env, targetclass, "buildContainer", functionsig); // function to perform
  // preform function on object
  (*env)->CallVoidMethodA(env, targetcontainer, containerfunction, args);
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest
 * Method:    SetVoidFiveContainersV
 * Signature: (ILjava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest_SetVoidFiveContainersV
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b)
{
  // call the different CallObjectV setting functions for the different cv-x container variables
  SetContainerV(env, obj, "(I)V",                  "cm1", i);
  SetContainerV(env, obj, "(ILjava/lang/String;)V", "cm2", i, s);
  SetContainerV(env, obj, "(ILjava/lang/String;Z)V", "cm3", i, s, b);
  SetContainerV(env, obj, "(ILjava/lang/String;ZZ)V", "cm4", i, s, b, b);
  SetContainerV(env, obj, "(ILjava/lang/String;ZZZ)V", "cm5", i, s, b, b, b);
}

void SetContainerV(JNIEnv *env, jobject obj, const char* functionsig, const char* targetname, ...)
{
  // test environment MultiFunctionContainer object to perform function on
  jobject targetcontainer = getVariableObject(env, obj, targetname);

  jclass targetclass = (*env)->GetObjectClass(env, targetcontainer); //;
  jmethodID containerfunction= (*env)->GetMethodID(env, targetclass, "buildContainer", functionsig);

  // call desired method using Call<>MethodA
  va_list arglist;
  va_start(arglist, targetname);
  (*env)->CallVoidMethodV(env, targetcontainer, containerfunction, arglist);
  va_end(arglist);
}


/***********************************************************************************************************
*
*  Perform a static void <variable-set> functions on all five MultiFunctionContainers
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest
 * Method:    SetStaticVoidFiveContainersA
 * Signature: (ILjava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest_SetStaticVoidFiveContainersA
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b)
{
  jvalue args[6];

  // build the data array
  args[1].i = i;
  args[2].l = s;
  args[3].z = b;
  args[4].z = b;
  args[5].z = b;

  // call the different CallObjectV setting functions for the different cv-x container variables

printf("getting target object <cm1> \n");
  args[0].l = getVariableObject(env, obj,"cm1");
printf("setting static CallStaticObjectMethodA \n");
  SetStaticContainerA(env, obj, "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;I)V",                  args);
printf("getting target object <cm2> \n");
  args[0].l = getVariableObject(env, obj,"cm2");
  SetStaticContainerA(env, obj, "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;)V", args);
  args[0].l = getVariableObject(env, obj,"cm3");
  SetStaticContainerA(env, obj, "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;Z)V", args);
  args[0].l = getVariableObject(env, obj,"cm4");
  SetStaticContainerA(env, obj, "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;ZZ)V", args);
  args[0].l = getVariableObject(env, obj,"cm5");
  SetStaticContainerA(env, obj, "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;ZZZ)V", args);
}

void SetStaticContainerA(JNIEnv *env, jobject obj, const char* functionsig, jvalue* args)
{
  jclass staticclass;
  jmethodID staticfunction;
  // static Multifunctioncontainer class and buildContaierStatic function
printf("finding static class <gnu/testlet/wonka/jni/MultiFunctionContainer> \n");
   staticclass = (*env)->FindClass(env, "gnu/testlet/wonka/jni/MultiFunctionContainer");
printf("finding static method <buildContainerStatic - %s> \n", functionsig);
   staticfunction = (*env)->GetStaticMethodID(env, staticclass, "buildContainerStatic", functionsig);

printf("calling  CallStaticObjectMethodA : %p\n", staticfunction);
  // perform the static function
  (*env)->CallStaticVoidMethodA(env, staticclass, staticfunction, args);
printf("done\n");
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest
 * Method:    SetStaticVoidFiveContainersV
 * Signature: (ILjava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest_SetStaticVoidFiveContainersV
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b)
{
  jobject container;
  // call the different CallObjectV setting functions for the different cv-x container variables
  container = getVariableObject(env, obj, "cm1");
  SetStaticContainerV(env, obj, "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;I)V",                  container, i);
  container = getVariableObject(env, obj, "cm2");
  SetStaticContainerV(env, obj, "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;)V", container, i, s);
  container = getVariableObject(env, obj, "cm3");
  SetStaticContainerV(env, obj, "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;Z)V", container, i, s, b);
  container = getVariableObject(env, obj, "cm4");
  SetStaticContainerV(env, obj, "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;ZZ)V", container, i, s, b, b);
  container = getVariableObject(env, obj, "cm5");
  SetStaticContainerV(env, obj, "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;ZZZ)V", container, i, s, b, b, b);
}

void SetStaticContainerV(JNIEnv *env, jobject obj, const char* functionsig, ...)
{
  // static class MultiFunctionContainer and function buildContainerStatic
  jclass staticclass = (*env)->FindClass(env, "gnu/testlet/wonka/jni/MultiFunctionContainer");
  // the static <buildContainer>
  jmethodID staticfunction = (*env)->GetStaticMethodID(env, staticclass, "buildContainerStatic", functionsig);

  //get the argument list
  va_list arglist;
  va_start(arglist, functionsig);
  (*env)->CallStaticVoidMethodV(env, staticclass, staticfunction, arglist);
  va_end(arglist);
}
/***********************************************************************************************************
*
*  Perform a nonvirtual void <variable-set> functions on all five MultiFunctionContainers
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest
 * Method:    SetNonvirtualVoidFiveContainersA
 * Signature: (ILjava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest_SetNonvirtualVoidFiveContainersA
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b)
{
  jvalue args[5];

  // build the data array
  args[0].i = i;
  args[1].l = s;
  args[2].z = b;
  args[3].z = b;
  args[4].z = b;

  // call the different CallObjectV setting functions for the different cv-x container variables
  SetNonvirtualContainerA(env, obj, "(I)V",                  "cm1", args);
  SetNonvirtualContainerA(env, obj, "(ILjava/lang/String;)V", "cm2", args);
  SetNonvirtualContainerA(env, obj, "(ILjava/lang/String;Z)V", "cm3", args);
  SetNonvirtualContainerA(env, obj, "(ILjava/lang/String;ZZ)V", "cm4", args);
  SetNonvirtualContainerA(env, obj, "(ILjava/lang/String;ZZZ)V", "cm5", args);
}

void SetNonvirtualContainerA(JNIEnv *env, jobject obj, const char* functionsig, const char* targetname, jvalue* args)
{
  // test environment MultiFunctionContainer object to perform function on
  jobject targetcontainer = getVariableObject(env, obj, targetname);

  //test class and super class
  jclass derivedclass = (*env)->GetObjectClass(env, targetcontainer);
  jclass baseclass = (*env)->GetSuperclass(env, derivedclass); //(*env)->FindClass(env, "gnu/testlet/wonka/jni/ConstructionContainer");  //
  // base class method
  jmethodID basefunction = (*env)->GetMethodID(env, baseclass, "buildContainer", functionsig);
if(basefunction == NULL) printf("ERROR: NO BASE CLASS METHOD \n");

  // call desired method using CallNonvirtual<>MethodA
  (*env)->CallNonvirtualVoidMethodA(env, targetcontainer, baseclass, basefunction, args);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest
 * Method:    SetNonvirtualVoidFiveContainersV
 * Signature: (ILjava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest_SetNonvirtualVoidFiveContainersV
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b)
{
  // call the different CallObjectV setting functions for the different cv-x container variables
  SetNonvirtualContainerV(env, obj, "(I)V",                  "cm1", i);
  SetNonvirtualContainerV(env, obj, "(ILjava/lang/String;)V", "cm2", i, s);
  SetNonvirtualContainerV(env, obj, "(ILjava/lang/String;Z)V", "cm3", i, s, b);
  SetNonvirtualContainerV(env, obj, "(ILjava/lang/String;ZZ)V", "cm4", i, s, b, b);
  SetNonvirtualContainerV(env, obj, "(ILjava/lang/String;ZZZ)V", "cm5", i, s, b, b, b);
}

void SetNonvirtualContainerV(JNIEnv *env, jobject obj, const char* functionsig, const char* targetname, ...)
{
  // test environment MultiFunctionContainer object to perform function on
  jobject targetcontainer = getVariableObject(env, obj, targetname);

  //test class and super class
  jclass multifunctionclass = (*env)->GetObjectClass(env, targetcontainer);//FindClass(env, "gnu/testlet/wonka/jni/MultiFunctionContainer");
  jclass constructionclass = (*env)->GetSuperclass(env, multifunctionclass);//FindClass(env, "gnu/testlet/wonka/jni/ConstructionContainer");
  jmethodID constructionsetpartial = (*env)->GetMethodID(env, constructionclass, "buildContainer", functionsig);

  // call desired method using CallNonvirtual<>Methodv
  va_list arglist;
  va_start(arglist, targetname);
  (*env)->CallNonvirtualVoidMethodV(env, targetcontainer, constructionclass, constructionsetpartial, arglist);
  va_end(arglist);
}

/***********************************************************************************************************
*
*  Perform a series of <getBuiltBase> object functions and paste the SimpleContainers retrieved into the
*  given SimpleContainers
***********************************************************************************************************/
/* Inaccessible static: harness */
/*
 * Class:     gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest
 * Method:    SetFiveSimpleContainersA
 * Signature: (ILjava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest_SetFiveSimpleContainersA
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b)
{
  // build the data array
  jvalue args[5];
  args[0].i = i;
  args[1].l = s;
  args[2].z = b;
  args[3].z = b;
  args[4].z = b;

  // call the different CallObjectV setting functions for the different cv-x container variables
  BuildSimpleContainerA(env, obj, "(I)Lgnu/testlet/wonka/jni/SimpleContainer;",                  "cm1","cs1", args);
  BuildSimpleContainerA(env, obj, "(ILjava/lang/String;)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm2","cs2", args);
  BuildSimpleContainerA(env, obj, "(ILjava/lang/String;Z)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm3","cs3", args);
  BuildSimpleContainerA(env, obj, "(ILjava/lang/String;ZZ)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm4","cs4", args);
  BuildSimpleContainerA(env, obj, "(ILjava/lang/String;ZZZ)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm5","cs5", args);
}

void BuildSimpleContainerA(JNIEnv *env, jobject obj, const char* functionsig, const char* sourcename, const char* targetname, jvalue* args)
{
  //  perform "SimpleContainer = MultiFunctincontainer.GetBuiltContainerbase(args)"on given container
  jobject basecontainer = getVariableObject(env, obj, sourcename);
  jclass baseclass = (*env)->GetObjectClass(env, basecontainer); //FindClass(env, "Lgnu/testlet/wonka/jni/MultiFunctionContainer");
  jmethodID containerfunction = (*env)->GetMethodID(env, baseclass, "getBuiltContainerBase", functionsig); // function to perform

  // preform function on object
  jobject simplecontainer = (*env)->CallObjectMethodA(env, basecontainer, containerfunction, args);

  // assign simplecontainer value to cm variable
  setVariableObject(env, obj, targetname, simplecontainer);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest
 * Method:    SetFiveSimpleContainersV
 * Signature: (ILjava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest_SetFiveSimpleContainersV
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b)
{

  // call the different CallObjectV setting functions for the different cv-x container variables
  BuildSimpleContainerV(env, obj, "(I)Lgnu/testlet/wonka/jni/SimpleContainer;",                  "cm1","cs1", i);
  BuildSimpleContainerV(env, obj, "(ILjava/lang/String;)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm2","cs2", i, s);
  BuildSimpleContainerV(env, obj, "(ILjava/lang/String;Z)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm3","cs3", i, s, b);
  BuildSimpleContainerV(env, obj, "(ILjava/lang/String;ZZ)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm4","cs4", i, s, b, b);
  BuildSimpleContainerV(env, obj, "(ILjava/lang/String;ZZZ)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm5","cs5", i, s, b, b, b);
}

void BuildSimpleContainerV(JNIEnv *env, jobject obj, const char* functionsig, const char* sourcename, const char* targetname,...)
{
  //  perform "SimpleContainer = MultiFunctincontainer.GetBuiltContainerbase(args)"on given container
  jobject basecontainer = getVariableObject(env, obj, sourcename);
  jclass baseclass = (*env)->GetObjectClass(env, basecontainer); //FindClass(env, "Lgnu/testlet/wonka/jni/MultiFunctionContainer");
  jmethodID containerfunction = (*env)->GetMethodID(env, baseclass, "getBuiltContainerBase", functionsig); // function to perform

  // preform function on object
  jobject simplecontainer;
  va_list arglist;
  va_start(arglist, targetname);
  simplecontainer = (*env)->CallObjectMethodV(env, basecontainer, containerfunction, arglist);
  va_end(arglist);

  // assign simplecontainer value to cm variable
  setVariableObject(env, obj, targetname, simplecontainer);
}

/***********************************************************************************************************
*
*  Perform a series of static <getBuiltBase> object functions and paste the SimpleContainers retrieved into the
*  given SimpleContainers
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest
 * Method:    SetStaticFiveSimpleContainersA
 * Signature: (ILjava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest_SetStaticFiveSimpleContainersA
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b)
{
  // build the data array
  jvalue args[6];
  args[1].i = i;
  args[2].l = s;
  args[3].z = b;
  args[4].z = b;
  args[5].z = b;

  // call the different CallObjectV setting functions for the different cv-x container variables
  args[0].l = getVariableObject(env, obj, "cm1");
  BuildSimpleStaticContainerA(env, obj,
    "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;I)Lgnu/testlet/wonka/jni/SimpleContainer;",                  "cs1", args);
  args[0].l = getVariableObject(env, obj, "cm2");
  BuildSimpleStaticContainerA(env, obj,
    "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;)Lgnu/testlet/wonka/jni/SimpleContainer;", "cs2", args);
  args[0].l = getVariableObject(env, obj, "cm3");
  BuildSimpleStaticContainerA(env, obj,
    "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;Z)Lgnu/testlet/wonka/jni/SimpleContainer;", "cs3", args);
  args[0].l = getVariableObject(env, obj, "cm4");
  BuildSimpleStaticContainerA(env, obj,
    "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;ZZ)Lgnu/testlet/wonka/jni/SimpleContainer;", "cs4", args);
  args[0].l = getVariableObject(env, obj, "cm5");
  BuildSimpleStaticContainerA(env, obj,
    "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;ZZZ)Lgnu/testlet/wonka/jni/SimpleContainer;", "cs5", args);
}

void BuildSimpleStaticContainerA(JNIEnv *env, jobject obj, const char* functionsig, const char* targetname, jvalue* args)
{
  // get the Multifunctioncontainer upon which to execute the function:
  jclass mfcontainerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/MultiFunctionContainer");
  jmethodID buildcontainerfunction = (*env)->GetStaticMethodID(env, mfcontainerclass, "getBuiltContainerBaseStatic", functionsig);

  // static function SimpleContainer = static Multifunctincontainer(var...)
  jobject simplecontainer = (*env)->CallStaticObjectMethodA(env, mfcontainerclass, buildcontainerfunction, args);

  // assign simplecontainer value to cm variable
  setVariableObject(env, obj, targetname, simplecontainer);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest
 * Method:    SetStaticFiveSimpleContainersV
 * Signature: (ILjava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest_SetStaticFiveSimpleContainersV
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b)
{
  jobject cm;
  // call the different CallObjectV setting functions for the different cv-x container variables
  cm = getVariableObject(env, obj, "cm1");
  BuildSimpleStaticContainerV(env, obj,
      "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;I)Lgnu/testlet/wonka/jni/SimpleContainer;",                  "cs1", cm,i);
  cm = getVariableObject(env, obj, "cm2");
  BuildSimpleStaticContainerV(env, obj,
      "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;)Lgnu/testlet/wonka/jni/SimpleContainer;", "cs2", cm,i,s);
  cm = getVariableObject(env, obj, "cm3");
  BuildSimpleStaticContainerV(env, obj,
      "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;Z)Lgnu/testlet/wonka/jni/SimpleContainer;", "cs3", cm,i,s,b);
  cm = getVariableObject(env, obj, "cm4");
  BuildSimpleStaticContainerV(env, obj,
      "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;ZZ)Lgnu/testlet/wonka/jni/SimpleContainer;", "cs4", cm,i,s,b,b);
  cm = getVariableObject(env, obj, "cm5");
  BuildSimpleStaticContainerV(env, obj,
      "(Lgnu/testlet/wonka/jni/MultiFunctionContainer;ILjava/lang/String;ZZZ)Lgnu/testlet/wonka/jni/SimpleContainer;", "cs5", cm,i,s,b,b,b);

}

void BuildSimpleStaticContainerV(JNIEnv *env, jobject obj, const char* functionsig, const char* targetname, ...)
{
  // get the Multifunctioncontainer upon which to execute the function:
  jclass mfcontainerclass = (*env)->FindClass(env,"gnu/testlet/wonka/jni/MultiFunctionContainer");
  jmethodID buildcontainerfunction = (*env)->GetStaticMethodID(env, mfcontainerclass, "getBuiltContainerBaseStatic", functionsig);

  // static function SimpleContainer = static Multifunctincontainer(var...)
  jobject simplecontainer;
  va_list arglist;
  va_start(arglist, targetname);
  simplecontainer = (*env)->CallStaticObjectMethodV(env, mfcontainerclass, buildcontainerfunction, arglist);
  va_end(arglist);

  // assign simplecontainer value to cm variable
  setVariableObject(env, obj, targetname, simplecontainer);
}

/***********************************************************************************************************
*
*  Perform a series of nonvirtual <getBuiltBase> object functions and paste the SimpleContainers retrieved into the
*  given SimpleContainers
***********************************************************************************************************/
/*
 * Class:     gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest
 * Method:    SetNonvirtualFiveSimpleContainersA
 * Signature: (ILjava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest_SetNonvirtualFiveSimpleContainersA
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b)
{
  // build the data array
  jvalue args[5];
  args[0].i = i;
  args[1].l = s;
  args[2].z = b;
  args[3].z = b;
  args[4].z = b;

  // call the different CallObjectV setting functions for the different cv-x container variables
  BuildSimpleNonvirtualContainerA(env, obj, "(I)Lgnu/testlet/wonka/jni/SimpleContainer;",                  "cm1","cs1", args);
  BuildSimpleNonvirtualContainerA(env, obj, "(ILjava/lang/String;)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm2","cs2", args);
  BuildSimpleNonvirtualContainerA(env, obj, "(ILjava/lang/String;Z)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm3","cs3", args);
  BuildSimpleNonvirtualContainerA(env, obj, "(ILjava/lang/String;ZZ)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm4","cs4", args);
  BuildSimpleNonvirtualContainerA(env, obj, "(ILjava/lang/String;ZZZ)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm5","cs5", args);
}


void BuildSimpleNonvirtualContainerA
      (JNIEnv *env, jobject obj, const char* functionsig, const char* sourcename, const char* targetname, jvalue* args)
{
   //test class object, class and super class
  jobject derivedinstance = getVariableObject(env, obj, sourcename);
  jclass derivedclass = (*env)->GetObjectClass(env, derivedinstance);
  jclass baseclass = (*env)->GetSuperclass(env, derivedclass); //FindClass(env, "gnu/testlet/wonka/jni/ConstructionContainer");
  // base class method
  jmethodID getbase = (*env)->GetMethodID(env, baseclass, "getBuiltContainerBase", functionsig);
  // preform function on object
  jobject simplecontainer = (*env)->CallNonvirtualObjectMethodA(env, derivedinstance, baseclass, getbase, args);

  // assign simplecontainer value to cm variable
  setVariableObject(env, obj, targetname, simplecontainer);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest
 * Method:    SetNonvirtualFiveSimpleContainersV
 * Signature: (ILjava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIObjectFunctionAccessTest_SetNonvirtualFiveSimpleContainersV
  (JNIEnv *env, jobject obj, jint i, jstring s, jboolean b)
{
  // call the different CallObjectV setting functions for the different cv-x container variables
  BuildSimpleNonvirtualContainerV(env, obj, "(I)Lgnu/testlet/wonka/jni/SimpleContainer;",                  "cm1","cs1", i);
  BuildSimpleNonvirtualContainerV(env, obj, "(ILjava/lang/String;)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm2","cs2", i, s);
  BuildSimpleNonvirtualContainerV(env, obj, "(ILjava/lang/String;Z)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm3","cs3", i, s, b);
  BuildSimpleNonvirtualContainerV(env, obj, "(ILjava/lang/String;ZZ)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm4","cs4", i, s, b, b);
  BuildSimpleNonvirtualContainerV(env, obj, "(ILjava/lang/String;ZZZ)Lgnu/testlet/wonka/jni/SimpleContainer;", "cm5","cs5", i, s, b, b, b);
}

void BuildSimpleNonvirtualContainerV(JNIEnv *env, jobject obj, const char* functionsig, const char* sourcename, const char* targetname,...)
{
  //test class object, class and super class
  jobject derivedinstance = getVariableObject(env, obj, sourcename);
  jclass derivedclass = (*env)->GetObjectClass(env, derivedinstance);
  jclass baseclass = (*env)->GetSuperclass(env, derivedclass); //FindClass(env, "gnu/testlet/wonka/jni/ConstructionContainer");
  // base class method
  jmethodID getbase = (*env)->GetMethodID(env, baseclass, "getBuiltContainerBase", functionsig);

  // preform function on object
  jobject simplecontainer;
  va_list arglist;
  va_start(arglist, targetname);
  simplecontainer = (*env)->CallNonvirtualObjectMethodV(env, derivedinstance, baseclass, getbase, arglist);
  va_end(arglist);

  // assign simplecontainer value to cm variable
  setVariableObject(env, obj, targetname, simplecontainer);
}
