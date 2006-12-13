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

#include <stdio.h>
#include <jni.h>
#include <ieee754.h>
//#include "gnu_testlet_wonka_jni_JNIArrayTest.h"


/*********************************************************************************************************************************
*
*   ByteArray tests
*
**********************************************************************************************************************************/
/*********************************************************************************************************************************
*  build using NewByteArray()
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    buildArray
 * Signature: (IBB)[B
 */
JNIEXPORT jbyteArray JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_buildArray__IBB
  (JNIEnv *env, jobject obj, jint size, jbyte first, jbyte increment)
{
  // build a new jbyte arrat of desired size: and get a pointer to its contents
  jboolean iscopy;
  jbyteArray newarray = (*env)->NewByteArray(env, size);
  jbyte*arrayelements = (*env)->GetByteArrayElements(env, newarray, &iscopy);

  jbyte current = first;
  int i;
  // fill the array with data
  for (i=0; i<size; i++)
  {
    arrayelements[i] = current;
    current = (jbyte)(current+increment);
  }
  // release data into array and return array
  if (iscopy)
    (*env)->ReleaseByteArrayElements(env, newarray, arrayelements, 0);

  return newarray;
}

/*********************************************************************************************************************************
* get length using GetArrayLength
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    getArrayLength
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_getArrayLength___3B
  (JNIEnv *env, jobject obj, jbyteArray testarray)
{ return (*env)->GetArrayLength(env, testarray);}
/*********************************************************************************************************************************
* get contents of array using GetByteArrayElements, GetPrimitiveArrayCritical and GetByteArrayregion
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayElements
 * Signature: ([BB)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayElements___3BB
  (JNIEnv *env, jobject obj, jbyteArray arraytoscan, jbyte maximum)
{
  // Get a pointer to the elements to scan
  jboolean iscopy;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jbyte* scanelements = (*env)->GetByteArrayElements(env, arraytoscan, &iscopy);

  int count =0;
  int i;
  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }
  // release data into array and return result
  if (iscopy)
    (*env)->ReleaseByteArrayElements(env, arraytoscan, scanelements, 0);

  return count;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayCritical
 * Signature: ([BB)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayCritical___3BB
  (JNIEnv *env, jobject obj, jbyteArray arraytoscan, jbyte maximum)
{
  // Get a pointer to the elements to scan
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
//jbyte* primitivebuffer = (*env)->GetPrimitiveArrayCritical(env, arraytoscan,0);
  jbyte* scanelements = (*env)->GetPrimitiveArrayCritical(env, arraytoscan, 0); // = primitivebuffer;


  int count =0;
  int i;
  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }
  // release data into array and return result
  (*env)->ReleasePrimitiveArrayCritical(env, arraytoscan, scanelements, 0); //(env, arraytoscan, primitivebuffer, 0);

  return count;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayRegionComplete
 * Signature: ([BB)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayRegionComplete___3BB
  (JNIEnv *env, jobject obj, jbyteArray arraytoscan, jbyte maximum)
{
  int count =0;
  int i;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  // Get a buffer for the elements to scan and copy the array into that buffer
  jbyte* scanelements = (jbyte*)malloc(size*sizeof(jbyte));
  (*env)->GetByteArrayRegion(env, arraytoscan, 0, size, scanelements);

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }

  // release data into array and return result
  free(scanelements);
  return count;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayRegionOneByOne
 * Signature: ([BB)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayRegionOneByOne___3BB
  (JNIEnv *env, jobject obj, jbyteArray arraytoscan, jbyte maximum)
{
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jbyte elementtoscan;
  int count =0;
  int i;

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    (*env)->GetByteArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    if (elementtoscan > maximum)
      count++;
  }
  //nothing more...
  return count;
}



/*********************************************************************************************************************************
* change contents of array using GetByteArrayElements, GetPrimitiveArrayCritical and GetByteArrayregion
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayElements
 * Signature: ([BBB)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayElements___3BBB
  (JNIEnv *env, jobject obj, jbyteArray arraytoscan, jbyte toreplace, jbyte newvalue)
{
  // Get a pointer to the elements to scan
  jboolean iscopy;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jbyte* scanelements = (*env)->GetByteArrayElements(env, arraytoscan, &iscopy);

  // scan and replace when necessary
  int i;
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] = newvalue;
  }
  // release data into array and return array
  if (iscopy)
    (*env)->ReleaseByteArrayElements(env, arraytoscan, scanelements, 0);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayCritical
 * Signature: ([BBB)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayCritical___3BBB
  (JNIEnv *env, jobject obj, jbyteArray arraytoscan, jbyte toreplace, jbyte newvalue)
{
  // Get a pointer to the elements to scan
//jbyte* primitivebuffer = (*env)->GetPrimitiveArrayCritical(env, arraytoscan, 0);
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jbyte* scanelements = (*env)->GetPrimitiveArrayCritical(env, arraytoscan, 0); // = primitivebuffer;

  // scan for values bigger then the desired maximum
  int i;
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] =  newvalue;
  }
  // release data into array and return result
  (*env)->ReleasePrimitiveArrayCritical(env, arraytoscan, scanelements, 0); //(env, arraytoscan, primitivebuffer, 0);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayRegionComplete
 * Signature: ([BBB)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayRegionComplete___3BBB
  (JNIEnv *env, jobject obj, jbyteArray arraytoscan, jbyte toreplace, jbyte newvalue)
{
  int i;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  // Get a buffer for the elements to scan and copy the array into that buffer
  jbyte* scanelements = (jbyte*)malloc(size*sizeof(jbyte));
  (*env)->GetByteArrayRegion(env, arraytoscan, 0, size, scanelements);

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] =  newvalue;
  }

  // release data into array and return result
  (*env)->SetByteArrayRegion(env, arraytoscan, 0, size, scanelements);
  free(scanelements);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayRegionOneByOne
 * Signature: ([BBB)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayRegionOneByOne___3BBB
  (JNIEnv *env, jobject obj, jbyteArray arraytoscan, jbyte toreplace, jbyte newvalue)
{
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jbyte elementtoscan;
  int i;

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    (*env)->GetByteArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    if (elementtoscan == toreplace)
    {
      elementtoscan = newvalue;
      (*env)->SetByteArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    }
  }
}



/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************
*
*   shortArray tests
*
**********************************************************************************************************************************/
/*********************************************************************************************************************************
*  build using NewByteArray()
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    buildArray
 * Signature: (ISS)[S
 */
JNIEXPORT jshortArray JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_buildArray__ISS
  (JNIEnv *env, jobject obj, jint size, jshort first, jshort increment)
{
  // build a new jbyte arrat of desired size: and get a pointer to its contents
  jboolean iscopy;
  jshortArray newarray = (*env)->NewShortArray(env, size);
  jshort *arrayelements = (*env)->GetShortArrayElements(env, newarray, &iscopy);

  jshort current = first;
  int i;
  // fill the array with data
  for (i=0; i<size; i++)
  {
    arrayelements[i] = current;
    current = (jshort)(current+increment);
  }
  // release data into array and return array
  if (iscopy)
    (*env)->ReleaseShortArrayElements(env, newarray, arrayelements, 0);

  return newarray;
}

/*********************************************************************************************************************************
* get length using GetArrayLength
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    getArrayLength
 * Signature: ([S)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_getArrayLength___3S
  (JNIEnv *env, jobject obj, jshortArray testarray)
{ return (*env)->GetArrayLength(env, testarray);}

/*********************************************************************************************************************************
* get contents of array using GetByteArrayElements, GetPrimitiveArrayCritical and GetByteArrayregion
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayElements
 * Signature: ([SS)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayElements___3SS
  (JNIEnv *env, jobject obj, jshortArray arraytoscan, jshort maximum)
{
  // Get a pointer to the elements to scan
  jboolean iscopy;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jshort* scanelements = (*env)->GetShortArrayElements(env, arraytoscan, &iscopy);

  int count =0;
  int i;
  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }
  // release data into array and return result
  if (iscopy)
    (*env)->ReleaseShortArrayElements(env, arraytoscan, scanelements, 0);

  return count;
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayCritical
 * Signature: ([SS)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayCritical___3SS
  (JNIEnv *env, jobject obj, jshortArray arraytoscan, jshort maximum)
{
  // Get a pointer to the elements to scan
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jbyte* primitivebuffer = (*env)->GetPrimitiveArrayCritical(env, arraytoscan,0);
  jshort* scanelements = (jshort*)primitivebuffer;


  int count =0;
  int i;
  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }
  // release data into array and return result
  (*env)->ReleasePrimitiveArrayCritical(env, arraytoscan, primitivebuffer, 0);

  return count;
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayRegionComplete
 * Signature: ([SS)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayRegionComplete___3SS
  (JNIEnv *env, jobject obj, jshortArray arraytoscan, jshort maximum)
{
  int count =0;
  int i;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  // Get a buffer for the elements to scan and copy the array into that buffer
  jshort* scanelements = (jshort*)malloc(size*sizeof(jshort));
  (*env)->GetShortArrayRegion(env, arraytoscan, 0, size, scanelements);

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }

  // release data into array and return result
  free(scanelements);
  return count;
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayRegionOneByOne
 * Signature: ([SS)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayRegionOneByOne___3SS
  (JNIEnv *env, jobject obj, jshortArray arraytoscan, jshort maximum)
{
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jshort elementtoscan;
  int count =0;
  int i;

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    (*env)->GetShortArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    if (elementtoscan > maximum)
      count++;
  }
  //nothing more...
  return count;
}



/*********************************************************************************************************************************
* change contents of array using GetByteArrayElements, GetPrimitiveArrayCritical and GetByteArrayregion
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayElements
 * Signature: ([SSS)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayElements___3SSS
  (JNIEnv *env, jobject obj, jshortArray arraytoscan, jshort toreplace, jshort newvalue)
{
  // Get a pointer to the elements to scan
  jboolean iscopy;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jshort* scanelements = (*env)->GetShortArrayElements(env, arraytoscan, &iscopy);

  // scan and replace when necessary
  int i;
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] = newvalue;
  }
  // release data into array and return array
  if (iscopy)
    (*env)->ReleaseShortArrayElements(env, arraytoscan, scanelements, 0);
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayCritical
 * Signature: ([SSS)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayCritical___3SSS
  (JNIEnv *env, jobject obj, jshortArray arraytoscan, jshort toreplace, jshort newvalue)
{
  // Get a pointer to the elements to scan
  jbyte* primitivebuffer = (*env)->GetPrimitiveArrayCritical(env, arraytoscan, 0);
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jshort* scanelements = (jshort*) primitivebuffer;

  // scan for values bigger then the desired maximum
  int i;
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] =  newvalue;
  }
  // release data into array and return result
  (*env)->ReleasePrimitiveArrayCritical(env, arraytoscan, scanelements, 0); //(env, arraytoscan, primitivebuffer, 0);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayRegionComplete
 * Signature: ([SSS)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayRegionComplete___3SSS
  (JNIEnv *env, jobject obj, jshortArray arraytoscan, jshort toreplace, jshort newvalue)
{
  int i;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  // Get a buffer for the elements to scan and copy the array into that buffer
  jshort* scanelements = (jshort*)malloc(size*sizeof(jshort));
  (*env)->GetShortArrayRegion(env, arraytoscan, 0, size, scanelements);

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] =  newvalue;
  }

  // release data into array and return result
  (*env)->SetShortArrayRegion(env, arraytoscan, 0, size, scanelements);
  free(scanelements);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayRegionOneByOne
 * Signature: ([SSS)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayRegionOneByOne___3SSS
  (JNIEnv *env, jobject obj, jshortArray arraytoscan, jshort toreplace, jshort newvalue)
{
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jshort elementtoscan;
  int i;

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    (*env)->GetShortArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    if (elementtoscan == toreplace)
    {
      elementtoscan = newvalue;
      (*env)->SetShortArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    }
  }
}




/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
 /*********************************************************************************************************************************
*
*   intArray tests
*
**********************************************************************************************************************************/
/*********************************************************************************************************************************
*  build using NewByteArray()
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    buildArray
 * Signature: (III)[I
 */
JNIEXPORT jintArray JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_buildArray__III
  (JNIEnv *env, jobject obj, jint size, jint first, jint increment)
{
  // build a new jbyte arrat of desired size: and get a pointer to its contents
  jboolean iscopy;
  jintArray newarray = (*env)->NewIntArray(env, size);
  jint *arrayelements = (*env)->GetIntArrayElements(env, newarray, &iscopy);

  jint current = first;
  int i;
  // fill the array with data
  for (i=0; i<size; i++)
  {
    arrayelements[i] = current;
    current = (jint)(current+increment);
  }
  // release data into array and return array
  if (iscopy)
    (*env)->ReleaseIntArrayElements(env, newarray, arrayelements, 0);

  return newarray;
}

/*********************************************************************************************************************************
* get length using GetArrayLength
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    getArrayLength
* Signature: ([I)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_getArrayLength___3I
  (JNIEnv *env, jobject obj, jintArray testarray)
{ return (*env)->GetArrayLength(env, testarray);}

/*********************************************************************************************************************************
* get contents of array using GetByteArrayElements, GetPrimitiveArrayCritical and GetByteArrayregion
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayElements
 * Signature: ([II)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayElements___3II
  (JNIEnv *env, jobject obj, jintArray arraytoscan, jint maximum)
{
  // Get a pointer to the elements to scan
  jboolean iscopy;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jint* scanelements = (*env)->GetIntArrayElements(env, arraytoscan, &iscopy);

  int count =0;
  int i;
  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }
  // release data into array and return result
  if (iscopy)
    (*env)->ReleaseIntArrayElements(env, arraytoscan, scanelements, 0);

  return count;
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayCritical
 * Signature: ([II)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayCritical___3II
  (JNIEnv *env, jobject obj, jintArray arraytoscan, jint maximum)
{
  // Get a pointer to the elements to scan
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jbyte* primitivebuffer = (*env)->GetPrimitiveArrayCritical(env, arraytoscan,0);
  jint* scanelements = (jint*)primitivebuffer;


  int count =0;
  int i;
  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }
  // release data into array and return result
  (*env)->ReleasePrimitiveArrayCritical(env, arraytoscan, primitivebuffer, 0);

  return count;
}



/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayRegionComplete
 * Signature: ([II)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayRegionComplete___3II
  (JNIEnv *env, jobject obj, jintArray arraytoscan, jint maximum)
{
  int count =0;
  int i;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  // Get a buffer for the elements to scan and copy the array into that buffer
  jint* scanelements = (jint*)malloc(size*sizeof(jint));
  (*env)->GetIntArrayRegion(env, arraytoscan, 0, size, scanelements);

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }

  // release data into array and return result
  free(scanelements);
  return count;
}
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayRegionOneByOne
 * Signature: ([II)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayRegionOneByOne___3II
  (JNIEnv *env, jobject obj, jintArray arraytoscan, jint maximum)
{
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jint elementtoscan;
  int count =0;
  int i;

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    (*env)->GetIntArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    if (elementtoscan > maximum)
      count++;
  }
  //nothing more...
  return count;
}



/*********************************************************************************************************************************
* change contents of array using GetByteArrayElements, GetPrimitiveArrayCritical and GetByteArrayregion
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayElements
 * Signature: ([III)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayElements___3III
  (JNIEnv *env, jobject obj, jintArray arraytoscan, jint toreplace, jint newvalue)
{
  // Get a pointer to the elements to scan
  jboolean iscopy;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jint* scanelements = (*env)->GetIntArrayElements(env, arraytoscan, &iscopy);

  // scan and replace when necessary
  int i;
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] = newvalue;
  }
  // release data into array and return array
  if (iscopy)
    (*env)->ReleaseIntArrayElements(env, arraytoscan, scanelements, 0);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayCritical
 * Signature: ([III)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayCritical___3III
  (JNIEnv *env, jobject obj, jintArray arraytoscan, jint toreplace, jint newvalue)
{
  // Get a pointer to the elements to scan
  jbyte* primitivebuffer = (*env)->GetPrimitiveArrayCritical(env, arraytoscan, 0);
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jint* scanelements = (jint*) primitivebuffer;

  // scan for values bigger then the desired maximum
  int i;
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] =  newvalue;
  }
  // release data into array and return result
  (*env)->ReleasePrimitiveArrayCritical(env, arraytoscan, scanelements, 0); //(env, arraytoscan, primitivebuffer, 0);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayRegionComplete
 * Signature: ([III)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayRegionComplete___3III
  (JNIEnv *env, jobject obj, jintArray arraytoscan, jint toreplace, jint newvalue)
{
  int i;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  // Get a buffer for the elements to scan and copy the array into that buffer
  jint* scanelements = (jint*)malloc(size*sizeof(jint));
  (*env)->GetIntArrayRegion(env, arraytoscan, 0, size, scanelements);

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] =  newvalue;
  }

  // release data into array and return result
  (*env)->SetIntArrayRegion(env, arraytoscan, 0, size, scanelements);
  free(scanelements);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayRegionOneByOne
 * Signature: ([III)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayRegionOneByOne___3III
  (JNIEnv *env, jobject obj, jintArray arraytoscan, jint toreplace, jint newvalue)
{
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jint elementtoscan;
  int i;

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    (*env)->GetIntArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    if (elementtoscan == toreplace)
    {
      elementtoscan = newvalue;
      (*env)->SetIntArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    }
  }
}





/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
 /*********************************************************************************************************************************
*
*   longArray tests
*
**********************************************************************************************************************************/
/*********************************************************************************************************************************
*  build using NewByteArray()
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    buildArray
 * Signature: (IJJ)[J
 */
JNIEXPORT jlongArray JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_buildArray__IJJ
  (JNIEnv *env, jobject obj, jint size, jlong first, jlong increment)
{
  // build a new jbyte arrat of desired size: and get a pointer to its contents
  jboolean iscopy;
  jlongArray newarray = (*env)->NewLongArray(env, size);
  jlong *arrayelements = (*env)->GetLongArrayElements(env, newarray, &iscopy);

  jlong current = first;
  int i;
  // fill the array with data
  for (i=0; i<size; i++)
  {
    arrayelements[i] = current;
    current = (jlong)(current+increment);
  }
  // release data into array and return array
  if (iscopy)
    (*env)->ReleaseLongArrayElements(env, newarray, arrayelements, 0);

  return newarray;
}

/*********************************************************************************************************************************
* get length using GetArrayLength
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    getArrayLength
 * Signature: ([J)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_getArrayLength___3J
  (JNIEnv *env, jobject obj, jlongArray testarray)
{ return (*env)->GetArrayLength(env, testarray);}

/*********************************************************************************************************************************
* get contents of array using GetByteArrayElements, GetPrimitiveArrayCritical and GetByteArrayregion
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayElements
 * Signature: ([JJ)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayElements___3JJ
  (JNIEnv *env, jobject obj, jlongArray arraytoscan, jlong maximum)
{
  // Get a pointer to the elements to scan
  jboolean iscopy;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jlong* scanelements = (*env)->GetLongArrayElements(env, arraytoscan, &iscopy);

  int count =0;
  int i;
  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }
  // release data into array and return result
  if (iscopy)
    (*env)->ReleaseLongArrayElements(env, arraytoscan, scanelements, 0);

  return count;
}



/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayCritical
 * Signature: ([JJ)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayCritical___3JJ
  (JNIEnv *env, jobject obj, jlongArray arraytoscan, jlong maximum)
{
  // Get a pointer to the elements to scan
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jbyte* primitivebuffer = (*env)->GetPrimitiveArrayCritical(env, arraytoscan,0);
  jlong* scanelements = (jlong*)primitivebuffer;


  int count =0;
  int i;
  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }
  // release data into array and return result
  (*env)->ReleasePrimitiveArrayCritical(env, arraytoscan, primitivebuffer, 0);

  return count;
}



/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayRegionComplete
 * Signature: ([JJ)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayRegionComplete___3JJ
  (JNIEnv *env, jobject obj, jlongArray arraytoscan, jlong maximum)
{
  int count =0;
  int i;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  // Get a buffer for the elements to scan and copy the array into that buffer
  jlong* scanelements = (jlong*)malloc(size*sizeof(jlong));
  (*env)->GetLongArrayRegion(env, arraytoscan, 0, size, scanelements);

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }

  // release data into array and return result
  free(scanelements);
  return count;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayRegionOneByOne
 * Signature: ([JJ)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayRegionOneByOne___3JJ
  (JNIEnv *env, jobject obj, jlongArray arraytoscan, jlong maximum)
{
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jlong elementtoscan;
  int count =0;
  int i;

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    (*env)->GetLongArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    if (elementtoscan > maximum)
      count++;
  }
  //nothing more...
  return count;
}



/*********************************************************************************************************************************
* change contents of array using GetByteArrayElements, GetPrimitiveArrayCritical and GetByteArrayregion
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayElements
 * Signature: ([JJJ)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayElements___3JJJ
  (JNIEnv *env, jobject obj, jlongArray arraytoscan, jlong toreplace, jlong newvalue)
{
  // Get a pointer to the elements to scan
  jboolean iscopy;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jlong* scanelements = (*env)->GetLongArrayElements(env, arraytoscan, &iscopy);

  // scan and replace when necessary
  int i;
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] = newvalue;
  }
  // release data into array and return array
  if (iscopy)
    (*env)->ReleaseLongArrayElements(env, arraytoscan, scanelements, 0);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayCritical
 * Signature: ([JJJ)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayCritical___3JJJ
  (JNIEnv *env, jobject obj, jlongArray arraytoscan, jlong toreplace, jlong newvalue)
{
  // Get a pointer to the elements to scan
  jbyte* primitivebuffer = (*env)->GetPrimitiveArrayCritical(env, arraytoscan, 0);
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jlong* scanelements = (jlong*) primitivebuffer;

  // scan for values bigger then the desired maximum
  int i;
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] =  newvalue;
  }
  // release data into array and return result
  (*env)->ReleasePrimitiveArrayCritical(env, arraytoscan, scanelements, 0); //(env, arraytoscan, primitivebuffer, 0);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayRegionComplete
 * Signature: ([JJJ)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayRegionComplete___3JJJ
  (JNIEnv *env, jobject obj, jlongArray arraytoscan, jlong toreplace, jlong newvalue)
{
  int i;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  // Get a buffer for the elements to scan and copy the array into that buffer
  jlong* scanelements = (jlong*)malloc(size*sizeof(jlong));
  (*env)->GetLongArrayRegion(env, arraytoscan, 0, size, scanelements);

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] =  newvalue;
  }

  // release data into array and return result
  (*env)->SetLongArrayRegion(env, arraytoscan, 0, size, scanelements);
  free(scanelements);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayRegionOneByOne
 * Signature: ([JJJ)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayRegionOneByOne___3JJJ
  (JNIEnv *env, jobject obj, jlongArray arraytoscan, jlong toreplace, jlong newvalue)
{
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jlong elementtoscan;
  int i;

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    (*env)->GetLongArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    if (elementtoscan == toreplace)
    {
      elementtoscan = newvalue;
      (*env)->SetLongArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    }
  }
}





/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
 /*********************************************************************************************************************************
*
*   floatArray tests
*
**********************************************************************************************************************************/
/*********************************************************************************************************************************
*  build using NewByteArray()
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    buildArray
 * Signature: (IFF)[F
 */
JNIEXPORT jfloatArray JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_buildArray__IFF
  (JNIEnv *env, jobject obj, jint size, jfloat first, jfloat increment)
{
  // build a new jbyte arrat of desired size: and get a pointer to its contents
  jboolean iscopy;
  jfloatArray newarray = (*env)->NewFloatArray(env, size);
  jfloat *arrayelements = (*env)->GetFloatArrayElements(env, newarray, &iscopy);

  jfloat current = first;
  int i;
  // fill the array with data
  for (i=0; i<size; i++)
  {
    arrayelements[i] = current;
    current = (jfloat)float32_add(current, (jfloat)increment);
  }
  // release data into array and return array
  if (iscopy)
    (*env)->ReleaseFloatArrayElements(env, newarray, arrayelements, 0);

  return newarray;
}

/*********************************************************************************************************************************
* get length using GetArrayLength
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    getArrayLength
 * Signature: ([F)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_getArrayLength___3F
  (JNIEnv *env, jobject obj, jfloatArray testarray)
{ return (*env)->GetArrayLength(env, testarray);}

/*********************************************************************************************************************************
* get contents of array using GetByteArrayElements, GetPrimitiveArrayCritical and GetByteArrayregion
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayElements
 * Signature: ([FF)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayElements___3FF
  (JNIEnv *env, jobject obj, jfloatArray arraytoscan, jfloat maximum)
{
  // Get a pointer to the elements to scan
  jboolean iscopy;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jfloat* scanelements = (*env)->GetFloatArrayElements(env, arraytoscan, &iscopy);

  int count =0;
  int i;
  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }
  // release data into array and return result
  if (iscopy)
    (*env)->ReleaseFloatArrayElements(env, arraytoscan, scanelements, 0);

  return count;
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayCritical
 * Signature: ([FF)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayCritical___3FF
  (JNIEnv *env, jobject obj, jfloatArray arraytoscan, jfloat maximum)
{
  // Get a pointer to the elements to scan
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jbyte* primitivebuffer = (*env)->GetPrimitiveArrayCritical(env, arraytoscan,0);
  jfloat* scanelements = (jfloat*)primitivebuffer;


  int count =0;
  int i;
  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }
  // release data into array and return result
  (*env)->ReleasePrimitiveArrayCritical(env, arraytoscan, primitivebuffer, 0);

  return count;
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayRegionComplete
 * Signature: ([FF)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayRegionComplete___3FF
  (JNIEnv *env, jobject obj, jfloatArray arraytoscan, jfloat maximum)
{
  int count =0;
  int i;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  // Get a buffer for the elements to scan and copy the array into that buffer
  jfloat* scanelements = (jfloat*)malloc(size*sizeof(jfloat));
  (*env)->GetFloatArrayRegion(env, arraytoscan, 0, size, scanelements);

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }

  // release data into array and return result
  free(scanelements);
  return count;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayRegionOneByOne
 * Signature: ([FF)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayRegionOneByOne___3FF
  (JNIEnv *env, jobject obj, jfloatArray arraytoscan, jfloat maximum)
{
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jfloat elementtoscan;
  int count =0;
  int i;

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    (*env)->GetFloatArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    if (elementtoscan > maximum)
      count++;
  }
  //nothing more...
  return count;
}


/*********************************************************************************************************************************
* change contents of array using GetByteArrayElements, GetPrimitiveArrayCritical and GetByteArrayregion
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayElements
 * Signature: ([FFF)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayElements___3FFF
  (JNIEnv *env, jobject obj, jfloatArray arraytoscan, jfloat toreplace, jfloat newvalue)
{
  // Get a pointer to the elements to scan
  jboolean iscopy;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jfloat* scanelements = (*env)->GetFloatArrayElements(env, arraytoscan, &iscopy);

  // scan and replace when necessary
  int i;
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] = newvalue;
  }
  // release data into array and return array
  if (iscopy)
    (*env)->ReleaseFloatArrayElements(env, arraytoscan, scanelements, 0);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayCritical
 * Signature: ([FFF)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayCritical___3FFF
  (JNIEnv *env, jobject obj, jfloatArray arraytoscan, jfloat toreplace, jfloat newvalue)
{
  // Get a pointer to the elements to scan
  jbyte* primitivebuffer = (*env)->GetPrimitiveArrayCritical(env, arraytoscan, 0);
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jfloat* scanelements = (jfloat*) primitivebuffer;

  // scan for values bigger then the desired maximum
  int i;
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] =  newvalue;
  }
  // release data into array and return result
  (*env)->ReleasePrimitiveArrayCritical(env, arraytoscan, scanelements, 0); //(env, arraytoscan, primitivebuffer, 0);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayRegionComplete
 * Signature: ([FFF)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayRegionComplete___3FFF
  (JNIEnv *env, jobject obj, jfloatArray arraytoscan, jfloat toreplace, jfloat newvalue)
{
  int i;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  // Get a buffer for the elements to scan and copy the array into that buffer
  jfloat* scanelements = (jfloat*)malloc(size*sizeof(jfloat));
  (*env)->GetFloatArrayRegion(env, arraytoscan, 0, size, scanelements);

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] =  newvalue;
  }

  // release data into array and return result
  (*env)->SetFloatArrayRegion(env, arraytoscan, 0, size, scanelements);
  free(scanelements);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayRegionOneByOne
 * Signature: ([FFF)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayRegionOneByOne___3FFF
  (JNIEnv *env, jobject obj, jfloatArray arraytoscan, jfloat toreplace, jfloat newvalue)
{
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jfloat elementtoscan;
  int i;

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    (*env)->GetFloatArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    if (elementtoscan == toreplace)
    {
      elementtoscan = newvalue;
      (*env)->SetFloatArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    }
  }
}




/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
 /*********************************************************************************************************************************
*
*   doubleArray tests
*
**********************************************************************************************************************************/
/*********************************************************************************************************************************
*  build using NewByteArray()
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    buildArray
 * Signature: (IDD)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_buildArray__IDD
  (JNIEnv *env, jobject obj, jint size, jdouble first, jdouble increment)
{
  // build a new jbyte arrat of desired size: and get a pointer to its contents
  jboolean iscopy;
  jdoubleArray newarray = (*env)->NewDoubleArray(env, size);
  jdouble *arrayelements = (*env)->GetDoubleArrayElements(env, newarray, &iscopy);

  jdouble current = first;
  int i;
  // fill the array with data
  for (i=0; i<size; i++)
  {
    arrayelements[i] = current;
    current = (jdouble)float64_add(current, (jdouble)increment);
  }
  // release data into array and return array
  if (iscopy)
    (*env)->ReleaseDoubleArrayElements(env, newarray, arrayelements, 0);

  return newarray;
}

/*********************************************************************************************************************************
* get length using GetArrayLength
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    getArrayLength
 * Signature: ([D)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_getArrayLength___3D
  (JNIEnv *env, jobject obj, jdoubleArray testarray)
{ return (*env)->GetArrayLength(env, testarray);}
/*********************************************************************************************************************************
* get contents of array using GetByteArrayElements, GetPrimitiveArrayCritical and GetByteArrayregion
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayElements
 * Signature: ([DD)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayElements___3DD
  (JNIEnv *env, jobject obj, jdoubleArray arraytoscan, jdouble maximum)
{
  // Get a pointer to the elements to scan
  jboolean iscopy;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jdouble* scanelements = (*env)->GetDoubleArrayElements(env, arraytoscan, &iscopy);

  int count =0;
  int i;
  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }
  // release data into array and return result
  if (iscopy)
    (*env)->ReleaseDoubleArrayElements(env, arraytoscan, scanelements, 0);

  return count;
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayCritical
 * Signature: ([DD)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayCritical___3DD
  (JNIEnv *env, jobject obj, jdoubleArray arraytoscan, jdouble maximum)
{
  // Get a pointer to the elements to scan
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jbyte* primitivebuffer = (*env)->GetPrimitiveArrayCritical(env, arraytoscan,0);
  jdouble* scanelements = (jdouble*)primitivebuffer;


  int count =0;
  int i;
  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }
  // release data into array and return result
  (*env)->ReleasePrimitiveArrayCritical(env, arraytoscan, primitivebuffer, 0);

  return count;
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayRegionComplete
 * Signature: ([DD)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayRegionComplete___3DD
  (JNIEnv *env, jobject obj, jdoubleArray arraytoscan, jdouble maximum)
{
  int count =0;
  int i;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  // Get a buffer for the elements to scan and copy the array into that buffer
  jdouble* scanelements = (jdouble*)malloc(size*sizeof(jdouble));
  (*env)->GetDoubleArrayRegion(env, arraytoscan, 0, size, scanelements);

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] > maximum)
      count++;
  }

  // release data into array and return result
  free(scanelements);
  return count;
}
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayRegionOneByOne
 * Signature: ([BB)I
 * Signature: ([DD)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayRegionOneByOne___3DD
  (JNIEnv *env, jobject obj, jdoubleArray arraytoscan, jdouble maximum)
{
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jdouble elementtoscan;
  int count =0;
  int i;

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    (*env)->GetDoubleArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    if (elementtoscan > maximum)
      count++;
  }
  //nothing more...
  return count;
}

/*********************************************************************************************************************************
* change contents of array using GetByteArrayElements, GetPrimitiveArrayCritical and GetByteArrayregion
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayElements
 * Signature: ([DDD)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayElements___3DDD
  (JNIEnv *env, jobject obj, jdoubleArray arraytoscan, jdouble toreplace, jdouble newvalue)
{
  // Get a pointer to the elements to scan
  jboolean iscopy;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jdouble* scanelements = (*env)->GetDoubleArrayElements(env, arraytoscan, &iscopy);

  // scan and replace when necessary
  int i;
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] = newvalue;
  }
  // release data into array and return array
  if (iscopy)
    (*env)->ReleaseDoubleArrayElements(env, arraytoscan, scanelements, 0);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayCritical
 * Signature: ([DDD)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayCritical___3DDD
  (JNIEnv *env, jobject obj, jdoubleArray arraytoscan, jdouble toreplace, jdouble newvalue)
{
  // Get a pointer to the elements to scan
  jbyte* primitivebuffer = (*env)->GetPrimitiveArrayCritical(env, arraytoscan, 0);
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jdouble* scanelements = (jdouble*) primitivebuffer;

  // scan for values bigger then the desired maximum
  int i;
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] =  newvalue;
  }
  // release data into array and return result
  (*env)->ReleasePrimitiveArrayCritical(env, arraytoscan, scanelements, 0); //(env, arraytoscan, primitivebuffer, 0);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayRegionComplete
 * Signature: ([DDD)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayRegionComplete___3DDD
  (JNIEnv *env, jobject obj, jdoubleArray arraytoscan, jdouble toreplace, jdouble newvalue)
{
  int i;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  // Get a buffer for the elements to scan and copy the array into that buffer
  jdouble* scanelements = (jdouble*)malloc(size*sizeof(jdouble));
  (*env)->GetDoubleArrayRegion(env, arraytoscan, 0, size, scanelements);

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == toreplace)
      scanelements[i] =  newvalue;
  }

  // release data into array and return result
  (*env)->SetDoubleArrayRegion(env, arraytoscan, 0, size, scanelements);
  free(scanelements);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayRegionOneByOne
 * Signature: ([DDD)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayRegionOneByOne___3DDD
  (JNIEnv *env, jobject obj, jdoubleArray arraytoscan, jdouble toreplace, jdouble newvalue)
{
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jdouble elementtoscan;
  int i;

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    (*env)->GetDoubleArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    if (elementtoscan == toreplace)
    {
      elementtoscan = newvalue;
      (*env)->SetDoubleArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    }
  }
}



/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************
*
*  booleanArray tests
*
**********************************************************************************************************************************/
/*********************************************************************************************************************************
*  build a new array consisting out of <trues> true values followed by <falses> false values, using NewByteArray()
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    buildArray
 * Signature: (II)[Z
 */
JNIEXPORT jbooleanArray JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_buildArray__II
  (JNIEnv *env, jobject obj, jint trues, jint falses)
{
  // build a new jbyte arrat of desired size: and get a pointer to its contents
  jint i;
  jboolean iscopy;
  jint size = falses + trues;
  jbooleanArray newarray = (*env)->NewBooleanArray(env, size);
  jboolean *arrayelements = (*env)->GetBooleanArrayElements(env, newarray, &iscopy);
  // fill the array with data
  for (i=0; i<size; i++)
    arrayelements[i] =(i<trues)? JNI_TRUE:JNI_FALSE;

  // release data into array and return array
  if (iscopy)
    (*env)->ReleaseBooleanArrayElements(env, newarray, arrayelements, 0);

  return newarray;
}

/*********************************************************************************************************************************
* get length using GetArrayLength
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    getArrayLength
 * Signature: ([Z)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_getArrayLength___3Z
  (JNIEnv *env, jobject obj, jbooleanArray testarray)
{ return (*env)->GetArrayLength(env, testarray);}

/*********************************************************************************************************************************
* get contents of array using GetByteArrayElements, GetPrimitiveArrayCritical and GetByteArrayregion
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayElements
 * Signature: ([ZZ)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayElements___3ZZ
  (JNIEnv *env, jobject obj, jbooleanArray arraytoscan, jboolean valuetoscan)
{
  // Get a pointer to the elements to scan
  jboolean iscopy;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jboolean* scanelements = (*env)->GetBooleanArrayElements(env, arraytoscan, &iscopy);

  int count =0;
  int i;
  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == valuetoscan)
      count++;
  }
  // release data into array and return result
  if (iscopy)
    (*env)->ReleaseBooleanArrayElements(env, arraytoscan, scanelements, 0);

  return count;
}


/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayCritical
 * Signature: ([ZZ)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayCritical___3ZZ
  (JNIEnv *env, jobject obj, jbooleanArray arraytoscan, jboolean valuetoscan)
{
  // Get a pointer to the elements to scan
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jbyte* primitivebuffer = (*env)->GetPrimitiveArrayCritical(env, arraytoscan,0);
  jboolean* scanelements = (jboolean*)primitivebuffer;

  int count =0;
  int i;
  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    if (scanelements[i] == valuetoscan)
      count++;
  }
  // release data into array and return result
  (*env)->ReleasePrimitiveArrayCritical(env, arraytoscan, primitivebuffer, 0);

  return count;
}



/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayRegionComplete
 * Signature: ([ZZ)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayRegionComplete___3ZZ
  (JNIEnv *env, jobject obj, jbooleanArray arraytoscan, jboolean valuetoscan)
{
  jint count =0;
  jint i;
  jint size = (*env)->GetArrayLength(env, arraytoscan);
  // Get a buffer for the elements to scan and copy the array into that buffer
  jboolean* scanelements = (jboolean*)malloc( size * sizeof(jboolean));
  (*env)->GetBooleanArrayRegion(env, arraytoscan, 0, size, scanelements);

  // scan for values bigger then the desired maximum
  for (i=0; i< size; i++)
  {
    if (scanelements[i] == valuetoscan)
      count++;
  }

  // release data into array and return result
  free(scanelements);
  return count;
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayRegionOneByOne
 * Signature: ([ZZ)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayRegionOneByOne___3ZZ
  (JNIEnv *env, jobject obj, jbooleanArray arraytoscan, jboolean valuetoscan)
{
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jboolean elementtoscan;
  int count =0;
  int i;

  // scan for values bigger then the desired maximum
  for (i=0; i<size; i++)
  {
    (*env)->GetBooleanArrayRegion(env, arraytoscan, i, 1, &elementtoscan);
    if (elementtoscan == valuetoscan)
      count++;
  }
  //nothing more...
  return count;
}



/*********************************************************************************************************************************
* change contents of array using GetByteArrayElements, GetPrimitiveArrayCritical and GetByteArrayregion
* the algorithm to follow : if element[x] has same value as element [x-1], replace element[x-1] by its opposite
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayElements
 * Signature: ([ZZ)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayElements___3ZZ
  (JNIEnv *env, jobject obj, jbooleanArray arraytoscan, jboolean rightelement)
{
  // Get a pointer to the elements to scan
  jboolean iscopy;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jboolean* scanelements = (*env)->GetBooleanArrayElements(env, arraytoscan, &iscopy);

  // scan and replace when necessary
  int i;
  for (i=1; i<size; i++)
  {
      scanelements[i-1] = scanelements[i];
  }
  scanelements[size-1] = rightelement;
  // release data into array and return array
  if (iscopy)
    (*env)->ReleaseBooleanArrayElements(env, arraytoscan, scanelements, 0);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayCritical
 * Signature: ([ZZ)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayCritical___3ZZ
  (JNIEnv *env, jobject obj, jbooleanArray arraytoscan, jboolean rightelement)
{
  // Get a pointer to the elements to scan
  jbyte* primitivebuffer = (*env)->GetPrimitiveArrayCritical(env, arraytoscan, 0);
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jboolean* scanelements = (jboolean*) primitivebuffer;

  // scan for values bigger then the desired maximum
  int i;
  for (i=1; i<size; i++)
  {
      scanelements[i-1] = scanelements[i];
  }
  scanelements[size-1] = rightelement;
  // release data into array and return result
  (*env)->ReleasePrimitiveArrayCritical(env, arraytoscan, scanelements, 0); //(env, arraytoscan, primitivebuffer, 0);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayRegionComplete
 * Signature: ([ZZ)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayRegionComplete___3ZZ
  (JNIEnv *env, jobject obj, jbooleanArray arraytoscan, jboolean rightelement)
{
  int i;
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  // Get a buffer for the elements to scan and copy the array into that buffer
  jboolean* scanelements = (jboolean*)malloc(size*sizeof(jboolean));
  (*env)->GetBooleanArrayRegion(env, arraytoscan, 0, size, scanelements);

  // scan for values bigger then the desired maximum
  for (i=1; i<size; i++)
  {
      scanelements[i-1] = scanelements[i];
  }
  scanelements[size-1] = rightelement;

  // release data into array and return result
  (*env)->SetBooleanArrayRegion(env, arraytoscan, 0, size, scanelements);
  free(scanelements);
}

/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayRegionOneByOne
 * Signature: ([ZZ)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayRegionOneByOne___3ZZ
  (JNIEnv *env, jobject obj, jbooleanArray arraytoscan, jboolean rightelement)
{
  jsize size = (*env)->GetArrayLength(env, arraytoscan);
  jboolean current;
  int i;

  for (i=1; i<size; i++)
  {
    // copy element to previous
    (*env)->GetBooleanArrayRegion(env, arraytoscan, i, 1, &current);
    (*env)->SetBooleanArrayRegion(env, arraytoscan, i-1, 1, &current);
  }
  current = rightelement;
  (*env)->SetBooleanArrayRegion(env, arraytoscan, size-1, 1, &current);
}




/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/

 /*********************************************************************************************************************************
*
*   objectArray tests
*
**********************************************************************************************************************************/
/*********************************************************************************************************************************
* get length using GetArrayLength
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    getArrayLength
 * Signature: ([Lgnu/testlet/wonka/jni/SimpleContainer;)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_getArrayLength___3Lgnu_testlet_wonka_jni_SimpleContainer_2
  (JNIEnv *env, jobject obj, jobjectArray testarray)
{ return ((jsize)((*env)->GetArrayLength(env, testarray)));}
/*********************************************************************************************************************************
* get contents of array using GetObjectArrayElement
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    scanArrayElements
 * Signature: ([Lgnu/testlet/wonka/jni/SimpleContainer;I)I
 */
JNIEXPORT jint JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_scanArrayElements___3Lgnu_testlet_wonka_jni_SimpleContainer_2I
  (JNIEnv *env, jobject obj, jobjectArray arraytoscan, jint maximum)
{
  jint i, numbertoscan ;
  jint count =0;
  // container, class and 'number' field
  jobject testcontainer = (*env)->GetObjectArrayElement(env, arraytoscan, 0);
  jclass cls = (*env)->GetObjectClass(env, testcontainer);
  jfieldID field = (*env)->GetFieldID(env, cls, "number", "I");

  // one by one get the array containers and scan their numbers
  jint size = (*env)->GetArrayLength(env, arraytoscan);
  for(i=0; i<size; i++)
  {
    // get current container, get its number variable and check if bigger then maximum
    testcontainer = (*env)->GetObjectArrayElement(env, arraytoscan, i);
    numbertoscan = (*env)->GetIntField(env, testcontainer, field);
    if(numbertoscan>maximum)
      count++;
  }
  return count;
}

/*********************************************************************************************************************************
* change contents of array using Get0bjectArrayElement/Set0bjectArrayElement
*/
/*
 * Class:     gnu_testlet_wonka_jni_JNIArrayTest
 * Method:    changeArrayElements
 * Signature: ([Lgnu/testlet/wonka/jni/SimpleContainer;ILgnu/testlet/wonka/jni/SimpleContainer;)V
 */
JNIEXPORT void JNICALL Java_gnu_testlet_wonka_jni_JNIArrayTest_changeArrayElements___3Lgnu_testlet_wonka_jni_SimpleContainer_2ILgnu_testlet_wonka_jni_SimpleContainer_2
  (JNIEnv *env, jobject obj, jobjectArray arraytoscan, jint valuetoscan, jobject newvalue)
{
  // get the array elements one by one
  jint i, numbertoscan ;
  // container, class and 'number' field
  jobject testcontainer = (*env)->GetObjectArrayElement(env, arraytoscan, 0);
  jclass cls = (*env)->GetObjectClass(env, testcontainer);
  jfieldID field = (*env)->GetFieldID(env, cls, "number", "I");

  jint size = (*env)->GetArrayLength(env, arraytoscan);
  for(i=0; i<size; i++)
  {
    // get current container
    testcontainer = (*env)->GetObjectArrayElement(env, arraytoscan, i);
    // get number and check if bigger then maximum
    numbertoscan = (*env)->GetIntField(env, testcontainer, field);
    if(numbertoscan == valuetoscan)
      (*env)->SetObjectArrayElement(env, arraytoscan, i, newvalue);
  }
}
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
/*********************************************************************************************************************************/
