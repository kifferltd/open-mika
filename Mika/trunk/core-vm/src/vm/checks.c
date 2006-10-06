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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: checks.c,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
** 
**
** This file contains all sorts of routines to check for casts and assignment
** compatabilities. It is mostly used by the interpreter and the reflection
** methods, or JNI support functions.
*/

#include "checks.h"

#include "arrays.h"
#include "clazz.h"
#include "core-classes.h"
#include "fields.h"
#include "methods.h"
#include "wstrings.h"
#include "loading.h"

/*
** Check whether 'thisclazz' implements 'interface'.
*/

w_boolean implementsInterface (w_clazz thisclazz, w_clazz interfaze) {

  w_size i;
  w_clazz superclazz = thisclazz;
  w_clazz superinterface;

  if (mustBeReferenced(thisclazz) != CLASS_LOADING_FAILED) {
    while (superclazz) {
      woempa(1, "Does '%k' implement '%k' ...\n", superclazz, interfaze);
      for(i = 0; i < superclazz->numInterfaces; ++i) {
        superinterface = superclazz->interfaces[i];
        woempa(1, "... YEP, '%k' implements or extends '%k'.\n", superclazz, superinterface);
        if (superinterface == interfaze || implementsInterface(superinterface, interfaze)) {
          return WONKA_TRUE;
        }
      }

      superclazz = getSuper(superclazz);
    }
  }

  return WONKA_FALSE;

}

/*
** Check if 'S_clazz' can be cast to 'T_clazz'.
** This according to the rules described for the 'checkcast' opcode.
*/

w_boolean isAssignmentCompatible(w_clazz S_clazz, w_clazz T_clazz) {

  w_int castOK = WONKA_FALSE;
//  w_clazz current;
  w_clazz T_component;
  w_clazz S_component;
  
  woempa(1,"S is %k T is %k\n", S_clazz, T_clazz);

  /*
  ** Quick check - if same clazz pointers then they must be assignment compatible.
  */
  
  if (S_clazz == T_clazz) {
    woempa(4, "(QUICK check: same clazz pointer %p => clazz %k is compatible with itself.\n", S_clazz, S_clazz);
    return WONKA_TRUE;
  }

#ifdef RUNTIME_CHECKS
  if (getClazzState(T_clazz) < CLAZZ_STATE_LOADED) {
    wabort(ABORT_WONKA, "Target class %k of isAssignmentCompatible is not yet loaded!\n", T_clazz);
  }
#endif

  mustBeSupersLoaded(S_clazz);

  if (isStrictSuperClass(T_clazz, S_clazz)) {
    woempa(1, "From class %k to (indirect) superclass %k -> O.K.\n", S_clazz, T_clazz);

    return WONKA_TRUE;

  }

  if (S_clazz->dims) {
    if (T_clazz == clazzObject) {
      woempa(1, "From array %k to java.lang.Object -> O.K.\n", S_clazz);

      return WONKA_TRUE;

    }
    if (isSet(T_clazz->flags, ACC_INTERFACE)) {

      return implementsInterface (S_clazz , T_clazz);

    }
    if (T_clazz->dims) {
      /*
      ** If the element type is a primitive type, they should be the same, if they are reference types,
      ** they should be assignment compatible according to the 'isAssignmentCompatible' rules; we call
      ** isAssignmentCompatible recursively then, with the component clazzes.
      */
      if (clazzIsPrimitive(S_clazz->previousDimension)) {
        if (S_clazz->previousDimension == T_clazz->previousDimension) {
          castOK = WONKA_TRUE;
        }
      }
      else {
           T_component = T_clazz->previousDimension;
           wassert(T_component);
           S_component = S_clazz->previousDimension;
           wassert(S_component);
           castOK = isAssignmentCompatible(S_component, T_component);
      }
    }
  }
  else {
    if (isSet(T_clazz->flags, ACC_INTERFACE)) {
      castOK = implementsInterface(S_clazz, T_clazz);
    }
  }

  woempa(4, "Clazz %k is%s compatible with clazz %k.\n", S_clazz, castOK ? "" : " NOT",  T_clazz);
  
  return castOK;

}

/*
** Check whether the 'caller' clazz is allowed to call 'method'.
*/

w_boolean isAllowedToCall(w_clazz caller, w_method method, w_boolean is_this) {

  if (method->spec.declaring_clazz == caller) {
    woempa(1,"This Class (%K) is same as calling Class, so any method can be called.\n",method->spec.declaring_clazz);

    return WONKA_TRUE;

  }
  else if (sameRuntimePackage(caller, method->spec.declaring_clazz)) {
    woempa(1,"This Class (%K) is in the same package as calling Class (%K), so any non-private method can be called.\n",method->spec.declaring_clazz,caller);

    return isNotSet(method->spec.declaring_clazz->flags,ACC_PRIVATE) && isNotSet(method->flags,ACC_PRIVATE);

  }
  else if (isSuperClass(method->spec.declaring_clazz, caller) && is_this) {
    woempa(1,"This Class (%K) is a superclass of calling Class (%K), and the instance being accessed is `this', so protected methods can be called.\n",method->spec.declaring_clazz,caller);
    return isSet(method->spec.declaring_clazz->flags,ACC_PUBLIC) && (isSet(method->flags,ACC_PUBLIC) ||
              (isSet(method->flags,ACC_PROTECTED) && isNotSet(method->flags,ACC_STATIC)));

  }
  else {
    woempa(1,"This Class (%K) is in a different package to calling Class (%K), so only public methods can be called.\n",method->spec.declaring_clazz,caller);

    return isSet(method->spec.declaring_clazz->flags,ACC_PUBLIC) && isSet(method->flags,ACC_PUBLIC);

  }
}

/*
** Check whether the 'caller' clazz is allowed to access 'field'.
*/

w_boolean isAllowedToAccess(w_clazz caller, w_field field, w_boolean is_this) {

  if (field->declaring_clazz == caller) {
    woempa(1,"This Class (%K) is same as calling Class, so any field can be accessed.\n",field->declaring_clazz);

    return WONKA_TRUE;

  }
  else if (sameRuntimePackage(caller, field->declaring_clazz)) {
    woempa(1,"This Class (%K) is in the same package as calling Class (%K), so any non-private field can be accessed.\n",field->declaring_clazz,caller);

    return isNotSet(field->declaring_clazz->flags,ACC_PRIVATE) && isNotSet(field->flags,ACC_PRIVATE);

  }
  else if (is_this && isSuperClass(field->declaring_clazz, caller)) {
    woempa(1,"This Class (%K) is a superclass of calling Class (%K), and the instance being accessed is `this', so protected fields can be accessed.\n",field->declaring_clazz,caller);

    return isSet(field->declaring_clazz->flags,ACC_PUBLIC) && (isSet(field->flags,ACC_PUBLIC) ||
              (isSet(field->flags,ACC_PROTECTED) && isNotSet(field->flags,ACC_STATIC)));

  }
  else {
    woempa(1,"This Class (%K) is in a different package to calling Class (%K), so only public fields can be accessed.\n",field->declaring_clazz,caller);

    return isSet(field->declaring_clazz->flags,ACC_PUBLIC) && isSet(field->flags,ACC_PUBLIC);

  }
}

/** Two classes are said to belong to the same ``runtime package''
 ** if they have the same class loader and the same package name.
 ** Note that ``subpackages'' have no significance at runtime, e.g.
 ** java.lang and java.lang.reflect are just different packages
 ** which happen to have part of their name in common.
 */
w_boolean sameRuntimePackage(w_clazz clazz1, w_clazz clazz2) {
  w_string name1;
  w_string name2;
  w_size length1;
  w_size length2;
  w_instance loader1;
  w_instance loader2;

  woempa(1,"%K vs. %K.\n",clazz1,clazz2);
  if (clazz1 == clazz2) {
    woempa(1,"  - same class, so same runtime package(!)\n");

    return WONKA_TRUE;

  }

  loader1 = clazz1->loader;
  loader2 = clazz2->loader;

  if (loader1 == NULL) {
    loader1 = systemClassLoader;
  }
  if (loader2 == NULL) {
    loader2 = systemClassLoader;
  }

  name1 = clazz1->dotified;
  name2 = clazz2->dotified;
  length1 = string_length(name1);
  length2 = string_length(name2);

  woempa(1,"%K vs. %K.\n",clazz1, clazz2);
  if (loader1 != loader2) {
    woempa(1,"  - different classloaders, so they cannot be the same runtime package\n");

    return WONKA_FALSE;

  }

  while (length1 > 0 && string_char(name1, --length1) != '.');
  while (length2 > 0 && string_char(name2, --length2) != '.');
  woempa(1,"Package name lengths are %d and %d\n",length1, length2);
  if (length1 != length2) {
    woempa(1,"  - different lengths, so different packages.\n");

    return WONKA_FALSE;

  }
  for (; length1 > 0 && string_char(name1, length1-1) == string_char(name2, length1-1); --length1);
  if (length1) {
    woempa(1,"  - differ at position %d, so different packages.\n", length1-1);

    return WONKA_FALSE;

  }

  woempa(1,"  - same runtime package\n");

  return WONKA_TRUE;
}
  
