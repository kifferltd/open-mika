/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
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

