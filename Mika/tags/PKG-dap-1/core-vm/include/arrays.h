#ifndef _ARRAYS_H
#define _ARRAYS_H

/**************************************************************************
* Copyright  (c) 2001, 2002 by Acunia N.V. All rights reserved.           *
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

/*
** $Id: arrays.h,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

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
