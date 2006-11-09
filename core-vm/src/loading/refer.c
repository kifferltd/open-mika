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
* Modifications copyright (c) 2004, 2006 by Chris Gray, /k/ Embedded Java *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: refer.c,v 1.11 2006/10/04 14:24:16 cvsroot Exp $
*/

#include <string.h>

#include "clazz.h"
#include "checks.h"
#include "constant.h"
#include "descriptor.h"
#include "exception.h"
#include "fields.h"
#include "hashtable.h"
#include "loading.h"
#include "methods.h"
#include "reflection.h"
#include "threads.h"

/*
** The ``referenced'' state is defined by Staerk, Schmid & Boerger as follows:
**  - all superclasses and superinterfaces are in at least state ``referenced''
**  - all classes reachable via a ``direct'' reference are in at least state
**    ``supers loaded''
**  - all classes reachable via an ``indirect'' reference are in at least
**    state ``supers loaded''.
** By ``direct reference'' is meant (apparently): class constants, the
** value type of our own fields, and the parameter and return types of
** our own methods. ``Indirect references'' are the value types of fields
** of other classes and parameter and return types of methods of other
** classes which are referred to by the constant pool of this class.
**
** We actually defer some of this work: we load all classes referenced by
** class constants, but for the others we just establish the initiating
** class loader and set up a w_UnloadedClass structure.
*/

/*
** Parse a method descriptor and fill in method->spec.arg_types, 
** method->spec.return_type, and method->exec.args .
*/
static w_int parseMethodDescriptor(w_method method, w_string desc_string) {

  w_thread thread = currentWonkaThread;
  w_clazz  declaring_clazz;
  w_clazz  item_clazz;
  w_size   j;
  w_size   rparen;
  w_clazz *args = allocMem(256 * sizeof(w_clazz));
  w_size   nargs = 0;
  w_int    result = CLASS_LOADING_DID_NOTHING;

  if (!args) {
    return CLASS_LOADING_FAILED; /* OutOfMemoryError was already thrown ... */
  }

  declaring_clazz = method->spec.declaring_clazz;
  if (string_char(desc_string, 0) != '(') {
    woempa(9, "Missing '(' in %w\n", desc_string);
    throwException(thread, clazzClassFormatError, "%w", desc_string);
    return CLASS_LOADING_FAILED;
  }

  /*
  ** Skip the leading `(' and start parsing the parameter list.
  */
  j = 1;
  rparen = string_length(desc_string);

  while(--rparen && string_char(desc_string, rparen) != ')');

  if (!rparen) {
    throwException(thread, clazzClassFormatError, "%w", desc_string);
    return CLASS_LOADING_FAILED;
  }

  while (j < rparen && (result != CLASS_LOADING_FAILED)) {
    item_clazz = parseDescriptor(desc_string, &j, rparen, declaring_clazz->loader);
    if (item_clazz) {
      if (item_clazz != clazz_void) {
        woempa(1, "Method %k/%w%w arg[%d] has type %K\n", declaring_clazz, method->spec.name, desc_string, nargs, item_clazz);
        args[nargs++] = item_clazz;
      }
      else {
        woempa(9, "Method %k/%w%w arg[%d] has type %k\n", declaring_clazz, method->spec.name, desc_string, nargs, clazz_void);
        throwException(thread, clazzClassFormatError, "%w", desc_string);
        return CLASS_LOADING_FAILED;
      }
    }
    else {
      if (! exceptionThrown(thread)) {
        throwException(thread, clazzNoClassDefFoundError, "parsing failed: %w", desc_string);
      }
      return CLASS_LOADING_FAILED;
    }
  }
  
  if (result != CLASS_LOADING_FAILED) {
    if (nargs) {
      method->spec.arg_types = reallocMem(args, (nargs + 1) * sizeof(w_clazz));
      method->spec.arg_types[nargs] = NULL;
    }
    else {
      releaseMem(args);
      method->spec.arg_types = NULL;
    }
  
    j = rparen + 1;
    item_clazz = parseDescriptor(desc_string, &j, string_length(desc_string), declaring_clazz->loader);

    if (item_clazz) {
      woempa(1, "Method %k/%w%w return type is %K\n", declaring_clazz, method->spec.name, desc_string, item_clazz);
      method->spec.return_type = item_clazz;
    }
    else {
      if (! exceptionThrown(thread)) {
        throwException(thread, clazzClassFormatError, "%w", desc_string);
      }
      return CLASS_LOADING_FAILED;
    }
  
    method->exec.nargs = nargs;
  }

  return result;
}

/*
** Copy method->spec.arg_types, method->spec.return_type, and method->exec.nargs
** from the `parent' method which this method overrides or implements.
*/
static w_int inheritMethodDescriptor(w_method method, w_method parent) {

  w_size   i;
  w_size   nargs = 0;
  w_int    result = CLASS_LOADING_DID_NOTHING;

  woempa(1, "%M: inheriting method descriptor from %M\n", method, parent);
  nargs = parent->exec.nargs; 
  if (nargs) {
    method->spec.arg_types = allocMem((nargs + 1) * sizeof(w_clazz));
    if (!method->spec.arg_types) {

      return CLASS_LOADING_FAILED;

    }
    for (i = 0; i < nargs; ++i) {
      method->spec.arg_types[i] = parent->spec.arg_types[i];
      if (getClazzState(parent->spec.arg_types[i]) == CLAZZ_STATE_UNLOADED) {
        registerUnloadedClazz(parent->spec.arg_types[i]);
      }
      woempa(1, "%M arg[%d]: %K\n", method, i, method->spec.arg_types[i]);
    }
    method->spec.arg_types[nargs] = NULL;
  }
  method->exec.nargs = nargs;

  method->spec.return_type = parent->spec.return_type;
  if (getClazzState(parent->spec.return_type) == CLAZZ_STATE_UNLOADED) {
    registerUnloadedClazz(parent->spec.return_type);
  }
  woempa(1, "%M returns %K\n", method, method->spec.return_type);

  return result;
}

/*
*/
w_int referenceField(w_field field) {
  w_int result = CLASS_LOADING_DID_NOTHING;
  w_thread thread = currentWonkaThread;
  w_size   j = 0;

  woempa(1, "Referencing field '%w %w' of %k\n", field->desc, field->name, field->declaring_clazz);
  field->value_clazz = parseDescriptor(field->desc, &j, string_length(field->desc), field->declaring_clazz->loader);

  if (exceptionThrown(thread)) {

    return  CLASS_LOADING_FAILED;

  }

  woempa(1, "Referenced field '%k %w'of %k: result is %d\n", field->value_clazz, field->name, field->declaring_clazz, result);

  return result;
}

// TODO: replace this with some 'standard' thing
static int clazz2words(w_clazz clazz) {
  return (clazz == clazz_void) ? 0 : (clazz == clazz_long || clazz == clazz_double) ? 2 : 1;
}

/*
** Reference a method
*/
w_int referenceMethod(w_method method) {
  w_int result = CLASS_LOADING_DID_NOTHING;
  w_method parent = method->parent;
  w_size j;

  woempa(1, "Method %m declared in %K, parent is %p\n", method, method->spec.declaring_clazz, parent);
  if (parent) {
    woempa(1, "Parent %m declared in %K\n", parent, parent->spec.declaring_clazz);
  }

  if (parent) {
    result = inheritMethodDescriptor(method, parent);
  }
  else {
    result = parseMethodDescriptor(method, method->desc);
  }

  if (result == CLASS_LOADING_FAILED) {

    return result;

  }

  woempa(1,"Setting up stack and local variable information for %M\n", method);
  method->exec.arg_i = isSet(method->flags, ACC_STATIC) ? 0 : 1;
  if (method->spec.arg_types) {
    for (j = 0; method->spec.arg_types[j]; j++) {
      method->exec.arg_i +=  clazz2words(method->spec.arg_types[j]);
    }
  }
  method->exec.return_i = clazz2words(method->spec.return_type);
  woempa(1, "%M: arg_i = %d, return_i = %d\n", method, method->exec.arg_i, method->exec.return_i);
  method->exec.dispatcher = initialize_dispatcher;

  if (isSet(method->flags, ACC_ABSTRACT)) {

    return result;

  }

  /*
  ** If the method is native we use the stack_i variable
  ** to store the number of words the native stack should grow.
  ** For non static functions, it is 2 (env and status pointers)
  ** for static functions, it needs third extra spot for the Class
  ** instance that is passed.
  */

  if (isSet(method->flags, ACC_NATIVE)) {
    if (isSet(method->flags, ACC_STATIC)) {
      method->exec.stack_i = 3;
    }
    else {
      method->exec.stack_i = 2;
    }
  }

  return result;
}

/*
** Derive a method `declared' in this class from a method of a superinterface.
*/
static w_method cloneMethod(w_clazz clazz, w_method original) {
  w_method copy;
  volatile w_clazz original_clazz;
  int i;

  ++clazz->numDeclaredMethods;
  if (clazz->own_methods) {
    clazz->own_methods = reallocMem(clazz->own_methods, (clazz->numDeclaredMethods) * sizeof(w_Method));
  }
  else {
    clazz->own_methods = allocMem((clazz->numDeclaredMethods) * sizeof(w_Method));
  }

  copy = &clazz->own_methods[clazz->numDeclaredMethods - 1];
  memcpy(copy, original, sizeof(w_Method));
  copy->spec.declaring_clazz = clazz;
  registerString(copy->spec.name);
  copy->desc = registerString(original->desc);
  copy->spec.arg_types = NULL;
  copy->spec.return_type = NULL;
  copy->parent = original;
  if (original->throws) {
    copy->throws = allocMem(original->numThrows * sizeof(w_ushort));
    if (!copy->throws) {
      return NULL;
    }
    memcpy(copy->throws, original->throws, original->numThrows * sizeof(w_ushort));
    for (i = 0; i < original->numThrows; ++i) {
      int original_index = original->throws[i];
      int classname_index;
      int class_index;

      original_clazz = original->spec.declaring_clazz;
      x_monitor_eternal(original_clazz->resolution_monitor);
      while (original_clazz->tags[original_index] == RESOLVING_CLASS) {
        x_monitor_wait(original_clazz->resolution_monitor, 3);
      }
      switch(original_clazz->tags[original_index]) {
      case CONSTANT_CLASS:
        classname_index = addUTF8ConstantToPool(clazz, resolveUtf8Constant(original_clazz, original_clazz->values[original_index]));
        class_index = addUnresolvedClassConstantToPool(clazz, classname_index);
	break;

      case RESOLVED_CLASS:
        classname_index = addUTF8ConstantToPool(clazz, ((w_clazz)original_clazz->values[original_index])->dotified);
        class_index = addUnresolvedClassConstantToPool(clazz, classname_index);
	break;

      default:
	class_index = -1; // to suppress a warning
	wabort(ABORT_WONKA, "Miljaar! Class constant in illegal state 0x%02x while cloning method exceptions\n", original_clazz->tags[original_index]);
      }
      x_monitor_exit(original_clazz->resolution_monitor);

      copy->throws[i] = class_index;
    }
  }
  if (copy->exec.debug_info) {
    // TODO: we could also clone the array(s).
    copy->exec.debug_info->numLocalVars = 0;
    copy->exec.debug_info->numLineNums = 0;
    copy->exec.debug_info->localVars = NULL;
    copy->exec.debug_info->lineNums = NULL;
  }

  return copy;
}

/*
** Deal with inheritance of methods from the superclass and superinterfaces.
*/
static w_int inheritMethods(w_clazz clazz) {
  w_hashtable2k temp;
  w_int    i;
  w_int    j;
  w_int    n;
  w_clazz  super;
  w_method m1;
  w_method m2;
  w_boolean same_package;

/*
** Put all our declared methods into the temp hashtable, so that we can
** easily see when inherited methods are overridden.
*/
  temp = ht2k_create((char *)"hashtable: (temporary)", 101);
  if (!temp) {
    wabort(ABORT_WONKA, "Unable to create temp hashtable\n");
  }
  n = clazz->numDeclaredMethods;
  for (i = 0; i < n; ++i) {
    m1 = &clazz->own_methods[i];
    if (isNotSet(m1->flags, ACC_STATIC)) {
      woempa(1, "%k declares a method %m\n", clazz, m1);
      ht2k_write_no_lock(temp, (w_word)m1->spec.name, m1->desc, (w_word)m1);
    }
  }

/*
** Inherit methods from our superclass(es). If the method is overridden by
** the current class, then the overriding method gets the same slot number
** as the overridden method: otherwise we just add the inherited method to
** the temp hashtable.
*/
  if (clazz->numSuperClasses) {
    super = clazz->supers[0];
    same_package = sameRuntimePackage(clazz, super);
    for (j = 0; j < (w_int)super->numInheritableMethods; ++j) {
      m1 = super->vmlt[j];
      if (same_package || isSet(m1->flags, ACC_PROTECTED | ACC_PUBLIC)) {
        woempa(1, "%k inherits %m from superclass %k\n", clazz, m1, m1->spec.declaring_clazz);
        if (isSet(m1->flags, ACC_FINAL)) {
          woempa(1, "%m of %k is final, no override is possible\n", m1, m1->spec.declaring_clazz);
          m2 = (w_method)ht2k_read_no_lock(temp, (w_word)m1->spec.name, (w_word)m1->desc);
          if (m2) {
            woempa(1, "Interestingly enough, %k does declare a method %m, which is rather naughty of it. But we are not deceived ...\n", clazz, m2);
          }
          else {
            woempa(1, "Method %M is not overridden by %k, slot is %d\n", m1, clazz, m1->slot);
            ht2k_write_no_lock(temp, (w_word)m1->spec.name, (w_word)m1->desc, (w_word)m1);
          }
        }
        else {
          m2 = (w_method)ht2k_read_no_lock(temp, (w_word)m1->spec.name, (w_word)m1->desc);
          if (m2) {
            m2->slot = m1->slot;
            m2->parent = m1;
            woempa(1, "Method %M overrides %M, slot is %d\n", m2, m1, m1->slot);
          }
          else {
            woempa(1, "Method %M is not overridden by %k, slot is %d\n", m1, clazz, m1->slot);
            ht2k_write_no_lock(temp, (w_word)m1->spec.name, (w_word)m1->desc, (w_word)m1);
          }
        }
      }
    }
  }

/*
** The temp hashtable now contains all methods which are declared by the
** current class or inherited from its superclass(es). If the current class
** is a non-interface class, we now check the (non-static) methods declared
** in all superinterfaces (direct and indirect): if we find that one is not
** implemented in the current class then we fabricate the declaration of an
** abstract implementation. (Such a fictitious method is known as a "Miranda"
** method: early Java compilers used to supply such methods automatically,
** but with Java2 this became the responsibility of the VM).
*/
  if (isNotSet(clazz->flags, ACC_INTERFACE)) {
    n = clazz->numInterfaces;
    for (i = n - 1; i >= 0; --i) {
      super = clazz->interfaces[i];
      same_package = sameRuntimePackage(clazz, super);
      for (j = 0; j < (w_int)super->numDeclaredMethods; ++j) {
        m1 = &super->own_methods[j];
        if (isNotSet(m1->flags, ACC_STATIC) && (same_package || isSet(m1->flags, ACC_PROTECTED | ACC_PUBLIC))) {
          woempa(1, "Inheriting %M from %k\n", m1, super);
          m2 = (w_method)ht2k_read_no_lock(temp, (w_word)m1->spec.name, (w_word)m1->desc);
          if (m2) {
            woempa(1, "Method %M implements %M\n", m2, m1);
          }
          else if (isSet(clazz->flags, CLAZZ_IS_PROXY)) {
            woempa(7, "Method %M is not implemented by %k, generating a Proxy method\n", m1, clazz);
            m2 = cloneMethod(clazz, m1);
	    if (!m2) {

              return CLASS_LOADING_FAILED;

	    }
            setFlag(m2->flags, METHOD_IS_PROXY | ACC_NATIVE);
            unsetFlag(m2->flags, ACC_ABSTRACT | METHOD_IS_INTERFACE);
            woempa(7, "Original flags were 0x%08x, proxy flags are 0x%08x\n", m1->flags, m2->flags);
            switch (m1->exec.return_i) {
            case 1:
              m2->exec.function.word_fun = (w_word_fun)singleProxyMethodCode;
              break;

            case 2:
              m2->exec.function.long_fun = (w_long_fun)doubleProxyMethodCode;
              break;

            default:
              m2->exec.function.void_fun = (w_void_fun)voidProxyMethodCode;
            }
            ht2k_write_no_lock(temp, (w_word)m2->spec.name, (w_word)m2->desc, (w_word)m2);
            woempa(7, "%k: increased numDeclaredMethods to %d\n", clazz, clazz->numDeclaredMethods);
          }
          else {
            woempa(1, "Method %M is not implemented by %k, generating a Miranda method\n", m1, clazz);
            m2 = cloneMethod(clazz, m1);
	    if (!m2) {

              return CLASS_LOADING_FAILED;

	    }
            setFlag(m2->flags, METHOD_IS_MIRANDA);
            setFlag(m2->flags, ACC_PUBLIC | ACC_ABSTRACT); // just in case
            unsetFlag(m2->flags, METHOD_IS_INTERFACE);
            ht2k_write_no_lock(temp, (w_word)m2->spec.name, (w_word)m2->desc, (w_word)m2);
            woempa(1, "%k: increased numDeclaredMethods to %d\n", clazz, clazz->numDeclaredMethods);
          }
        }
      }
    }
  }
  ht2k_destroy(temp);

  return CLASS_LOADING_SUCCEEDED;
}

static w_int createVirtualMethodLookupTable(w_clazz clazz) {
  w_int    i;
  w_int    n;
  w_size   next_free_slot;
  w_size   vmlt_size;
  w_method m;

  next_free_slot = clazz->supers ? clazz->supers[0]->numInheritableMethods : 0;

  n = clazz->numDeclaredMethods;

  for (i = 0; i < n; i++) {
    m = &clazz->own_methods[i];
    if (m->spec.name == string_angle_brackets_init) {
      woempa(1, "Method %M is a constructor, does not belong in vmlt\n", m);
      setFlag(m->flags, METHOD_IS_CONSTRUCTOR | METHOD_NO_OVERRIDE);
    }
    else if (isSet(m->flags, ACC_PRIVATE)) {
      woempa(1, "Method %M is private, does not belong in vmlt\n", m);
      setFlag(m->flags, METHOD_NO_OVERRIDE);
    }
    else if (isSet(m->flags, ACC_STATIC)) {
      woempa(1, "Method %M is static, does not belong in vmlt\n", m);
      if (m->spec.name == string_angle_brackets_clinit) {
        woempa(1, "Method %M is a clinit.\n", m);
        setFlag(m->flags, METHOD_IS_CLINIT);
      }
    }
    else {
      if (isSet(m->flags, ACC_FINAL) || isSet(clazz->flags, ACC_FINAL)) {
        woempa(1,"  Method %m is inheritable, but final\n", m);
        setFlag(m->flags, METHOD_NO_OVERRIDE);
      }
      if (m->slot == SLOT_NOT_ALLOCATED) {
        woempa(1, "%sethod %M does not override anything, giving it slot %d\n", isSet(m->flags, ACC_ABSTRACT) ? "Abstract m" : "M", m, next_free_slot);
        m->slot = next_free_slot++;
      }
      else {
        woempa(1, "%sethod %M overrides %M, slot already set to %d\n", isSet(m->flags, ACC_ABSTRACT) ? "Abstract m" : "M", m, m->parent, m->slot);
      }
    }
  }

  vmlt_size = next_free_slot;
  woempa(1, "%k: virtual method lookup table has %d slots\n", clazz, vmlt_size);

  clazz->numInheritableMethods = vmlt_size;
  clazz->vmlt = allocMem(vmlt_size * sizeof(w_method));

  if (clazz->numSuperClasses) {
    n = clazz->supers[0]->numInheritableMethods;
    woempa(1, "superclass %k has %d inheritable methods\n", clazz->supers[0], n);
    for (i = 0; i < n; ++i) {
      woempa(1, "%k: Inheriting %m of %k in slot[%d]\n", clazz, clazz->supers[0]->vmlt[i], clazz->supers[0]->vmlt[i]->spec.declaring_clazz, i);
      clazz->vmlt[i] = clazz->supers[0]->vmlt[i];
    }
  }

  n = clazz->numDeclaredMethods;
  for (i = 0; i < n; i++) {
    m = &clazz->own_methods[i];
    if (m->slot >= 0) {
      woempa(1, "%k: Own %smethod %m  goes in slot[%d]\n", clazz, isSet(m->flags, ACC_ABSTRACT) ? "abstract " : "", m, m->slot);
      clazz->vmlt[m->slot] = m;
    }
  }

  return CLASS_LOADING_DID_NOTHING;
}

static void createFieldTable(w_clazz clazz) {
  w_int bytesOffset = 0;
  w_int wordsOffset = 0;
  w_int referencesOffset = 0;
  w_int staticOffset = 0;
  w_size i;
  w_field field;

  woempa(1,"creating field table for class %k\n", clazz);
  /*
  ** Calculate the size in words of an instance of this class
  ** and fill in the correct offsets for each field of this
  ** class.
  */

  if (clazz->supers) {
    referencesOffset = 0 - clazz->supers[0]->numReferenceFields;
    wordsOffset = clazz->supers[0]->instanceSize + referencesOffset;
    bytesOffset = clazz->supers[0]->nextByteSlot;
    woempa(1,"superclass %k has %d primitive bytes %d primitive words %d reference words and %d words of static fields\n", clazz->supers[0], bytesOffset, wordsOffset, -referencesOffset, staticOffset);
  }
  else {
    referencesOffset = 0;
    bytesOffset = 0;
    wordsOffset = 0;
  }
  staticOffset = 0;

  for (i = 0; i < clazz->numStaticFields; i++) {
    field = &clazz->own_fields[i];
    woempa(1,"static field '%w' at slot[%d]\n",NM(field),staticOffset);
    field->size_and_slot = staticOffset;
    staticOffset +=  fieldSize(field);
  }
  for (;i < clazz->numFields; i++) {
    field = &clazz->own_fields[i];
    if (isSet(field->flags, FIELD_IS_REFERENCE)) {
      referencesOffset -=  1;
      field->size_and_slot = referencesOffset;
      woempa(1,"reference field '%w' at slot[%d]\n", NM(field), referencesOffset);
    }
    else {
      switch (field->value_clazz->type & 0x0f) {
        case VM_TYPE_BOOLEAN:
#ifdef PACK_BYTE_FIELDS
          woempa(1,"boolean field '%w' at byte slot[%d]\n", field->name, bytesOffset);
          field->size_and_slot = FIELD_SIZE_1_BIT + bytesOffset;
          if (bytesOffset == (wordsOffset * 4)) {
            wordsOffset++;
          }
          bytesOffset += 1;
          if ((bytesOffset % 4) == 0) {
            bytesOffset = wordsOffset * 4;
          }
#else
          woempa(1,"boolean field '%w' at word slot[%d]\n", field->name, wordsOffset);
          field->size_and_slot = wordsOffset;
          wordsOffset +=  1;
#endif
          break;

        case VM_TYPE_BYTE:
#ifdef PACK_BYTE_FIELDS
          woempa(1,"byte field '%w' at byte slot[%d]\n",NM(field),bytesOffset);
          field->size_and_slot = FIELD_SIZE_8_BITS + bytesOffset;
          if (bytesOffset == (wordsOffset * 4)) {
            wordsOffset++;
          }
          bytesOffset += 1;
          if ((bytesOffset % 4) == 0) {
            bytesOffset = wordsOffset * 4;
          }
#else
          woempa(1,"byte field '%w' at word slot[%d]\n",NM(field),wordsOffset);
          field->size_and_slot = wordsOffset;
          wordsOffset +=  1;
#endif
          break;

        case VM_TYPE_SHORT:
        case VM_TYPE_CHAR:
          woempa(1,"short/char field '%w' at word slot[%d]\n", field->name, wordsOffset);
#ifdef PACK_BYTE_FIELDS
          field->size_and_slot = FIELD_SIZE_16_BITS + wordsOffset;
#else
          field->size_and_slot = wordsOffset;
#endif
          wordsOffset += 1;
          break;

        case VM_TYPE_INT:
        case VM_TYPE_FLOAT:
          woempa(1,"int/float field '%w' at word slot[%d]\n", field->name, wordsOffset);
#ifdef PACK_BYTE_FIELDS
          field->size_and_slot = FIELD_SIZE_32_BITS + wordsOffset;
#else
          field->size_and_slot = wordsOffset;
#endif
          wordsOffset += 1;
          break;

        case VM_TYPE_LONG:
        case VM_TYPE_DOUBLE:
          woempa(1,"long/double field '%w' at word slot[%d]\n", field->name, wordsOffset);
#ifdef PACK_BYTE_FIELDS
          field->size_and_slot = FIELD_SIZE_64_BITS + wordsOffset;
#else
          field->size_and_slot = wordsOffset;
#endif
          wordsOffset += 2;
          break;

        default:
          wabort(ABORT_WONKA, "field '%w' has VM_TYPE %d, should be 1..8\n", field->name, field->value_clazz->type);
      }
    }
  }

  woempa(1,"class %k has %d primitive bytes %d primitive words %d reference words and %d words of static fields\n", clazz, bytesOffset, wordsOffset, -referencesOffset, staticOffset);
  clazz->instanceSize = wordsOffset - referencesOffset;
  clazz->nextByteSlot = bytesOffset;
  clazz->numReferenceFields = 0 - referencesOffset;
  clazz->numStaticWords = staticOffset;
}

#ifdef JDWP
extern void jdwp_event_class_prepare(w_clazz);
#endif

static w_int referenceClazz(w_clazz clazz) {
  w_int    result = CLASS_LOADING_DID_NOTHING;
  w_thread thread = currentWonkaThread;
  w_field  f;
  w_method m;
  w_int    i;
  w_int    j;
  w_int    n;
  w_fixup  fixup;

  n = clazz->numFields;
  /*
  ** Check for duplicate fields
  */
  if (isNotSet(clazz->flags, CLAZZ_IS_TRUSTED)) {
    for (i = 0; i < n; i++) {
      for (j = i + 1; j < n; j++) {
        if (clazz->own_fields[i].name == clazz->own_fields[j].name && clazz->own_fields[i].desc == clazz->own_fields[j].desc) {
          throwException(thread, clazzClassFormatError, "duplicate field %w %w", clazz->own_fields[i].desc, clazz->own_fields[i].name);

          return CLASS_LOADING_FAILED;
        }
      }
    }
  }

  /*
  ** Reference all classes which are value types of our fields
  */
  for (i = 0; i < n; i++) {
    f = &clazz->own_fields[i];
    if (!f->value_clazz) {
      result |= referenceField(f);
      if (result == CLASS_LOADING_FAILED) {

        return CLASS_LOADING_FAILED;

      }
    }
  }

  result |= inheritMethods(clazz);
  if (result == CLASS_LOADING_FAILED) {

    return CLASS_LOADING_FAILED;

  }

  if (isNotSet(clazz->flags, ACC_INTERFACE)) {
    result |= createVirtualMethodLookupTable(clazz);
    if (result == CLASS_LOADING_FAILED) {

      return CLASS_LOADING_FAILED;

    }
  }
  else {
    n = clazz->numDeclaredMethods;
    for (i = 0; i < n; i++) {
      m = &clazz->own_methods[i];
      if (isNotSet(m->flags, ACC_STATIC)) {
        woempa(1, "%k: method %M is an interface method.\n", clazz, m);
        m->flags |= METHOD_IS_INTERFACE;
      }
    }
  }

  n = clazz->numDeclaredMethods;
  if (isNotSet(clazz->flags, CLAZZ_IS_TRUSTED)) {
    for (i = 0; i < n; i++) {
      for (j = i + 1; j < n; j++) {
        if (clazz->own_methods[i].spec.name == clazz->own_methods[j].spec.name && clazz->own_methods[i].desc== clazz->own_methods[j].desc) {
          throwException(thread, clazzClassFormatError, "duplicate method %m", &clazz->own_methods[i]);

          return CLASS_LOADING_FAILED;
        }
      }
    }
  }

  for (i = 0; i < n; i++) {
    m = &clazz->own_methods[i];
    if (!m->spec.return_type) {
      result |= referenceMethod(m);
      if (result == CLASS_LOADING_FAILED) {

        return CLASS_LOADING_FAILED;

      }
    }
  }

  createFieldTable(clazz);

  /*
  ** Fixup the clazz for wotsit fields etc..
  ** No need to lock fixup1_hashtable, as all writes preceded all reads
  */

  fixup = (w_fixup)ht_read_no_lock(fixup1_hashtable,(w_word) clazz->dotified);
  woempa(1,"fixup1 for %k = %p\n", clazz,fixup);
  if (fixup) {
    fixup(clazz);
    if (exceptionThrown(thread)) {

      return CLASS_LOADING_FAILED;

    }
  }

  clazz->bytes_needed = (((clazz->instanceSize + 1) & ~1) * sizeof(w_word)) + sizeof(w_Object);
  woempa(1,"class %k has instance size %d words and %d words of static fields, each instance requires %d bytes\n", clazz, clazz->instanceSize, clazz->numStaticWords, clazz->bytes_needed);

  x_monitor_eternal(clazz->resolution_monitor);
  for (i = 1; i < (w_int)clazz->numConstants; i++) {
    if (clazz->tags[i] == CONSTANT_UTF8) {
      checkUTF8References(clazz, i);
    }
  }
  x_monitor_exit(clazz->resolution_monitor);

#ifdef JDWP
  jdwp_event_class_prepare(clazz);
#endif

  return result;
}

w_int mustBeReferenced(w_clazz clazz) {
  w_thread thread = currentWonkaThread;
  w_int    i;
  w_int    n;
  w_int    state = getClazzState(clazz);
  w_int    result = CLASS_LOADING_DID_NOTHING;
  x_status monitor_status;

#ifdef RUNTIME_CHECKS
  threadMustBeSafe(thread);

  if (state < CLAZZ_STATE_LOADED) {
    wabort(ABORT_WONKA, "%K must be loaded before it can be Referenced\n", clazz);
  }

  if (exceptionThrown(thread)) {
    woempa(9, "Eh? Exception '%e' already pending in mustBeReferenced(%K)\n", exceptionThrown(thread), clazz);
  }
#endif

  if (state == CLAZZ_STATE_BROKEN) {
  // TODO - is the right thing to throw?
    throwException(thread, clazzNoClassDefFoundError, "%k : %w", clazz, clazz->failure_message);

    return CLASS_LOADING_FAILED;

  }

  if (state >= CLAZZ_STATE_REFERENCED) {

    return CLASS_LOADING_DID_NOTHING;

  }

  result = mustBeSupersLoaded(clazz);

  if (result == CLASS_LOADING_FAILED) {

    return CLASS_LOADING_FAILED;

  }

  x_monitor_eternal(clazz->resolution_monitor);
  //clazz->resolution_thread = thread;
  state = getClazzState(clazz);

  while(state == CLAZZ_STATE_REFERENCING) {
    monitor_status = x_monitor_wait(clazz->resolution_monitor, CLASS_STATE_WAIT_TICKS);
    if (monitor_status == xs_interrupted) {
      x_monitor_eternal(clazz->resolution_monitor);
    }
    state = getClazzState(clazz);
  }

  if (state == CLAZZ_STATE_SUPERS_LOADED) {
    woempa(1, "Referencing %K\n", clazz);
    setClazzState(clazz, CLAZZ_STATE_REFERENCING);
    x_monitor_exit(clazz->resolution_monitor);

    n = clazz->numSuperClasses;
    woempa(1, "%K has %d superclasses\n", clazz, n);
    for (i = n - 1; (result != CLASS_LOADING_FAILED) && (i >= 0); --i) {
      result |= mustBeReferenced(clazz->supers[i]);
    }

    n = clazz->numInterfaces;
    woempa(1, "%K has %d superinterfaces\n", clazz, n);
    for (i = n - 1; (result != CLASS_LOADING_FAILED) && (i >= 0); --i) {
      result |= mustBeReferenced(clazz->interfaces[i]);
    }

    if (result != CLASS_LOADING_FAILED) {
      result |= referenceClazz(clazz);
    }

    if (result == CLASS_LOADING_FAILED) {
      x_monitor_eternal(clazz->resolution_monitor);
      setClazzState(clazz, CLAZZ_STATE_BROKEN);
      x_monitor_notify_all(clazz->resolution_monitor);
      x_monitor_exit(clazz->resolution_monitor);

      return result;

    }

    x_monitor_eternal(clazz->resolution_monitor);
    if(exceptionThrown(thread)) {
      setClazzState(clazz, CLAZZ_STATE_BROKEN);
      result = CLASS_LOADING_FAILED;
    }
    else {
      setClazzState(clazz, CLAZZ_STATE_REFERENCED);
    }
    x_monitor_notify_all(clazz->resolution_monitor);
  }
  else if (state == CLAZZ_STATE_BROKEN) {
    x_monitor_exit(clazz->resolution_monitor);

    return CLASS_LOADING_FAILED;

  }

  x_monitor_exit(clazz->resolution_monitor);

  return result;
}

