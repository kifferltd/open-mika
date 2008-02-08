/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/
#ifndef _ARRAYS_H
#define _ARRAYS_H

#include "jni.h"
#include "wonka.h"

/** Wonka Arrays
** An array instance looks like an instance of an object with two fields:
** - the length, stored in one w_word (just like a normal field), and
** - the array elements, stored in as many w_word's as are needed (a
**   "fat" field).  Array elements are packed down to byte level.
** The information needed to determine the data type and size of each
** array element is stored in the in the w_clazz structure pointed to
** by instance2clazz(arrayinstance).
**
** F_Array_length and F_Array_data are initialised to the offsets of
** these two "fields". 
*/

extern w_int                  F_Array_length;
extern w_int                  F_Array_data;

/**
** The table 'atype2clazz' maps the primitives to their respective array clazz type.
*/

extern w_clazz atype2clazz[];

/// Index an array of byte's
inline static w_sbyte *instance2Array_byte(w_instance a) {
  return (w_sbyte *)(a + F_Array_data);
}

/// Index an array of char's
inline static w_char *instance2Array_char(w_instance a) {
  return (w_char *)(a + F_Array_data);
}

/// Index an array of char's
inline static w_short *instance2Array_short(w_instance a) {
  return (w_short *)(a + F_Array_data);
}

/// Index an array of int's
inline static w_int *instance2Array_int(w_instance a) {
  return (w_int *)(a + F_Array_data);
}

/// Index an array of instances
inline static w_instance *instance2Array_instance(w_instance a) {
  return (w_instance *)(a + F_Array_data);
}

/// Index an array of float's
inline static w_float *instance2Array_float(w_instance a) {
  return (w_float *)(a + F_Array_data);
}

/// Index an array of long's
inline static w_long *instance2Array_long(w_instance a) {
  return (w_long *)(a + F_Array_data);
}

/// Index an array of double's
inline static w_double *instance2Array_double(w_instance a) {
  return (w_double *)(a + F_Array_data);
}

/// Get the length of an array
inline static w_int instance2Array_length(w_instance a) {
  return (w_int)a[F_Array_length];
}

/** Create array classes
*/
/// Create the archetypal clazz_Array from which all others are derived.
w_clazz createClazzArray(void);

w_instance cloneArray(w_thread thread, w_instance);
void arrayDestructor(w_instance this);

w_instance Array_clone (JNIEnv *env, w_instance thisObject);

#endif /* _ARRAYS_H */
