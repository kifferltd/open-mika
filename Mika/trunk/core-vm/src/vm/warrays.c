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
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: warrays.c,v 1.1 2005/08/20 09:19:54 cvs Exp $
** 
*/

#include <string.h>

#include "wonka.h"
#include "arrays.h"
#include "clazz.h"
#include "constant.h"
#include "loading.h"
#include "hashtable.h"
#include "ts-mem.h"
#include "methods.h"
#include "wstrings.h"
#include "exception.h"

/*
** These global variables hold the offset in the array instance of the length of the
** array and the offset to the first word of data in the array instance.
*/

w_int                         F_Array_length;
w_int                         F_Array_data;

/*
** The array handling functions...
** Create the clazz_Array handle that is the mother of all other array clazzes.
*/

static w_Method Array_clone_method;
static w_clazz  Array_interfaces[2];
static w_clazz  Array_supers[1];

w_clazz createClazzArray(void) {
  w_string string_clone = cstring2String("clone",5);
  w_clazz clazz = allocClazz();
  w_method arrayclone = &Array_clone_method;
  w_size   i;

  for (i = 0; i < clazzObject->numDeclaredMethods; i++) {
    if (clazzObject->own_methods[i].spec.name == string_clone) {
      woempa(7, "Found clone method of clazzObject in slot [%d], using it as basis of array clone\n", i);
      w_memcpy(arrayclone, &clazzObject->own_methods[i], sizeof(w_Method));
      arrayclone->exec.function = (w_function)(w_void_fun)Array_clone;
      unsetFlag(arrayclone->flags, ACC_PROTECTED);
      setFlag(arrayclone->flags, ACC_PUBLIC);
    }
  }

  w_memcpy(clazz, clazzObject, sizeof(w_Clazz));
  clazz->resolution_monitor = allocMem(sizeof(x_Monitor));
  if (!clazz->resolution_monitor) {
    wabort(ABORT_WONKA, "Unable to allocate clazz->resolution_monitor\n");
  }
  x_monitor_create(clazz->resolution_monitor);
  clazz->dotified = cstring2String("array prototype", 15);
  clazz->tags = allocMem(clazzObject->numConstants * sizeof(w_ConstantType));
  if (!clazz->tags) {
    wabort(ABORT_WONKA, "Unable to allocate clazz->tags\n");
  }
  memcpy((char*)clazz->tags, (char*)clazzObject->tags, clazzObject->numConstants * sizeof(w_ConstantType));
  clazz->values = allocMem(clazzObject->numConstants * sizeof(w_word));
  if (!clazz->values) {
    wabort(ABORT_WONKA, "Unable to allocate clazz->values\n");
  }
  memcpy((char*)clazz->values, (char*)clazzObject->values, clazzObject->numConstants * sizeof(w_word));
  clazz->numDeclaredMethods = 1;
  clazz->own_methods = arrayclone;

  clazz->numInterfaces = 2;
  clazz->numDirectInterfaces = 2;
  clazz->interfaces = Array_interfaces;
  clazz->interfaces[0] = clazzCloneable;
  clazz->interfaces[1] = clazzSerializable;
  clazz->numSuperClasses = 1;
  clazz->supers = Array_supers;
  clazz->supers[0] = clazzObject;
  clazz->numConstants = 0;
  F_Array_length = 0; 
  F_Array_data = 1; 

  return clazz;

}

/*
** To clone an array, we first create an array instance of one dimension,
** with the same clazz and length as the original.  We then copy the length
** and data (i.e., (size in words + 1) words) from the original instance to
** the cloned instance.
*/

w_instance Array_clone(JNIEnv *env, w_instance originalArrayInstance) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz clazz = instance2clazz(originalArrayInstance);
  w_int   length;
  w_int   bitsPerWord = sizeof(w_word)*8;
  w_int   sizeInBeets;
  w_int   sizeInWoids;
  w_int   cloneSize;
  w_instance clonedArrayInstance;

  length = instance2Array_length(originalArrayInstance);
  sizeInBeets = (clazz->previousDimension->bits)*length;
  sizeInWoids = (sizeInBeets+bitsPerWord-1)/bitsPerWord;
  cloneSize   = (sizeInWoids+1)*sizeof(w_word);
  clonedArrayInstance = allocArrayInstance_1d(thread, clazz, length);

  if (clonedArrayInstance) {
    woempa(1, "length %d.\n", length);
    w_memcpy(clonedArrayInstance, originalArrayInstance, (w_size)cloneSize);
  }

  return clonedArrayInstance;
}

w_instance cloneArray(w_thread t, w_instance originalArrayInstance) {
  return Array_clone(w_thread2JNIEnv(t), originalArrayInstance);
}

