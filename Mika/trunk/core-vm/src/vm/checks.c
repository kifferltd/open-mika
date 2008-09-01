/**************************************************************************
* Parts copyright (c) 2001, 2002 by Punch Telematix. All rights reserved. *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2008 by Chris Gray, /k/     *
* Embedded Java Solutions. All rights reserved.                           *
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

/*
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

w_boolean isAllowedToCall(w_clazz caller, w_method method, w_clazz objClazz) {
  if (method->spec.declaring_clazz == caller) {
    woempa(1,"This Class (%K) is same as calling Class, so any method can be called.\n",method->spec.declaring_clazz);

    return WONKA_TRUE;

  }
  else if (sameRuntimePackage(caller, method->spec.declaring_clazz)) {
    woempa(1,"This Class (%K) is in the same package as calling Class (%K), so any non-private method can be called.\n",method->spec.declaring_clazz,caller);

    return isNotSet(method->spec.declaring_clazz->flags,ACC_PRIVATE) && isNotSet(method->flags,ACC_PRIVATE);

  }
  else if (isSuperClass(method->spec.declaring_clazz, caller)) {
    woempa(1,"This Class (%K) is a superclass of calling Class (%K), and the instance being accessed is `this', so protected methods can be called.\n",method->spec.declaring_clazz,caller);
    return isSet(method->spec.declaring_clazz->flags,ACC_PUBLIC) && (isSet(method->flags,ACC_PUBLIC) ||
           (isSet(method->flags,ACC_PROTECTED) && (objClazz && isSuperClass(caller,objClazz))));

  }
  else {
    woempa(1,"This Class (%K) is in a different package to calling Class (%K), so only public methods can be called.\n",method->spec.declaring_clazz,caller);

    return isSet(method->spec.declaring_clazz->flags,ACC_PUBLIC) && isSet(method->flags,ACC_PUBLIC);

  }
}

/*
** Check whether the 'caller' clazz is allowed to access 'field'.
*/

w_boolean isAllowedToAccess(w_clazz caller, w_field field, w_clazz objClazz) {

  if (field->declaring_clazz == caller) {
    woempa(1,"This Class (%K) is same as calling Class, so any field can be accessed.\n",field->declaring_clazz);

    return WONKA_TRUE;

  }
  else if (sameRuntimePackage(caller, field->declaring_clazz)) {
    woempa(1,"This Class (%K) is in the same package as calling Class (%K), so any non-private field can be accessed.\n",field->declaring_clazz,caller);

    return isNotSet(field->declaring_clazz->flags,ACC_PRIVATE) && isNotSet(field->flags,ACC_PRIVATE);

  }
  else if (isSuperClass(field->declaring_clazz, caller)) {
    woempa(1,"This Class (%K) is a superclass of calling Class (%K), and the instance being accessed is `this', so protected fields can be accessed.\n",field->declaring_clazz,caller);

    return isSet(field->declaring_clazz->flags,ACC_PUBLIC) && (isSet(field->flags,ACC_PUBLIC) ||
          (isSet(field->flags,ACC_PROTECTED) && (objClazz && isSuperClass(caller, objClazz))));

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
  return clazz1->package == clazz2->package;
/*
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
*/
}
  
