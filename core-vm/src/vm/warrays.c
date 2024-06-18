/**************************************************************************
* Copyright (c) 2021, 2023 by KIFFER Ltd. All rights reserved.            *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

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
  w_string string_clone = ascii2String("clone",5);
  w_clazz array_clazz = allocClazz(clazzObject->numConstants);
  w_method array_clone_method = &Array_clone_method;
  w_size   i;

  w_method object_clone = NULL;
  for (i = 0; i < clazzObject->numDeclaredMethods; i++) {
    if (clazzObject->own_methods[i].spec.name == string_clone) {
      woempa(7, "Found clone() method of clazzObject in slot [%d], using it as basis of array clone()\n", i);
      object_clone = &clazzObject->own_methods[i];
      break;
    }
  }

  if (!object_clone) {
    wabort(ABORT_WONKA, "Was ist das fuer schweinerei - es gibt kein clone() in java.lang.Object!");
  }

  w_memcpy(array_clone_method, object_clone, sizeof(w_Method));
  array_clone_method->exec.function = (w_function)(w_void_fun)Array_clone;
  unsetFlag(array_clone_method->flags, ACC_PROTECTED);
  setFlag(array_clone_method->flags, ACC_PUBLIC);

  w_memcpy(array_clazz, clazzObject, sizeof(w_Clazz));
  // [CG 20230806] Note that the above memcpy leaves array_clazz->tags and array_clazz->values pointing into clazzObject
  // - but that's alright :-)
  memset(&array_clazz->resolutionMonitor, 0, sizeof(x_Monitor));
  x_monitor_create(&array_clazz->resolutionMonitor);
  array_clazz->dotified = ascii2String("array prototype", 15);
  array_clazz->numDeclaredMethods = 1;
  array_clazz->own_methods = array_clone_method;

  array_clazz->numInterfaces = 2;
  array_clazz->numDirectInterfaces = 2;
  array_clazz->interfaces = Array_interfaces;
  array_clazz->interfaces[0] = clazzCloneable;
  array_clazz->interfaces[1] = clazzSerializable;
  array_clazz->numSuperClasses = 1;
  array_clazz->supers = Array_supers;
  array_clazz->supers[0] = clazzObject;
  array_clazz->numConstants = 0;
  F_Array_length = 0; 
  F_Array_data = 1; 

  return array_clazz;

}

/*
** To clone an array, we first create an array instance of one dimension,
** with the same clazz and length as the original.  We then copy the length
** and data (i.e., (size in words + 1) words) from the original instance to
** the cloned instance.
*/

w_instance Array_clone(w_thread thread, w_instance originalArrayInstance) {

  w_clazz clazz = instance2clazz(originalArrayInstance);
  w_int   length;
  w_int   bitsPerWord = sizeof(w_word)*8;
  w_int   sizeInBeets;
  w_int   sizeInWoids;
  w_int   cloneSize;
  w_instance clonedArrayInstance;

  threadMustBeSafe(thread);
  length = instance2Array_length(originalArrayInstance);
  sizeInBeets = (clazz->previousDimension->bits)*length;
  sizeInWoids = (sizeInBeets+bitsPerWord-1)/bitsPerWord;
  cloneSize   = (sizeInWoids+1)*sizeof(w_word);
  enterUnsafeRegion(thread);
  clonedArrayInstance = allocArrayInstance_1d(thread, clazz, length);
  enterSafeRegion(thread);

  if (clonedArrayInstance) {
    woempa(1, "length %d.\n", length);
    w_memcpy(clonedArrayInstance, originalArrayInstance, (w_size)cloneSize);
  }

  return clonedArrayInstance;
}

w_instance cloneArray(w_thread t, w_instance originalArrayInstance) {
  return Array_clone(t, originalArrayInstance);
}

