/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2008, 2009, 2010 by Chris   *
* Gray, /k/ Embedded Java Solutions.  All rights reserved.                *
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
#include <stdio.h>

#include "checks.h"
#include "clazz.h"
#include "constant.h"
#include "core-classes.h"
#include "descriptor.h"
#include "exception.h"
#include "fields.h"
#include "hashtable.h"
#include "loading.h"
#include "methods.h"
#include "wstrings.h"
#include "ts-mem.h"
#include "heap.h"
#include "wordset.h"

#ifdef RUNTIME_CHECKS
static void check_constant_index(w_clazz clazz, w_size i) {
  if (i == 0 || i > clazz->numConstants) {
    wabort(ABORT_WONKA, "Nom d'un chien! Trying to resolve constant[%d] of %k, which has %d constants\n", i, clazz, clazz->numConstants);
  }
}
#else
#define check_constant_index(clazz,i)
#endif

/*
 * Get the value of a CLASS constant, resolving it if need be.
 * The calling thread must be GC safe!
 */
w_clazz getClassConstant(w_clazz clazz, w_int i, w_thread thread) {
  int tag;
  threadMustBeSafe(thread);
  check_constant_index(clazz, i);

  while ((tag = clazz->tags[i]) < RESOLVED_CONSTANT) {
    if (tag == COULD_NOT_RESOLVE) {
      throwExceptionInstance(thread, (w_instance)clazz->values[i]);
      return NULL;
    }
    
    resolveClassConstant(clazz, i);
  }

  return (w_clazz)clazz->values[i];
}

/*
 * Get the value of a FIELD constant, resolving it if need be.
 * The calling thread must GC safe!
 */
w_field getFieldConstant(w_clazz clazz, w_int i) {
#ifdef RUNTIME_CHECKS
  w_thread thread = currentWonkaThread;
#endif
  int tag;
  threadMustBeSafe(thread);
  check_constant_index(clazz, i);

  while ((tag = clazz->tags[i]) < RESOLVED_CONSTANT) {
    if (tag == COULD_NOT_RESOLVE) {
      throwExceptionInstance(currentWonkaThread, (w_instance)clazz->values[i]);

      return NULL;
    }
    
    resolveFieldConstant(clazz, i);
  }

  return (w_field)clazz->values[i];
}

/*
 * Get the value of a METHOD constant, resolving it if need be.
 * The calling thread must GC safe!
 */
w_method getMethodConstant(w_clazz clazz, w_int i) {
#ifdef RUNTIME_CHECKS
  w_thread thread = currentWonkaThread;
#endif
  int tag;
  threadMustBeSafe(thread);
  check_constant_index(clazz, i);

  while ((tag = clazz->tags[i]) < RESOLVED_CONSTANT) {
    if (tag == COULD_NOT_RESOLVE) {
      throwExceptionInstance(currentWonkaThread, (w_instance)clazz->values[i]);

      return NULL;
    }
    
    resolveMethodConstant(clazz, i);
  }

  return (w_method)clazz->values[i];
}

/*
 * Get the value of an IMETHOD constant, resolving it if need be.
 * The calling thread must GC safe!
 */
w_method getIMethodConstant(w_clazz clazz, w_int i) {
#ifdef RUNTIME_CHECKS
  w_thread thread = currentWonkaThread;
#endif
  int tag;
  threadMustBeSafe(thread);
  check_constant_index(clazz, i);

  while ((tag = clazz->tags[i]) < RESOLVED_CONSTANT) {
    if (tag == COULD_NOT_RESOLVE) {
      throwExceptionInstance(currentWonkaThread, (w_instance)clazz->values[i]);

      return NULL;
    }
    
    resolveIMethodConstant(clazz, i);
    tag = clazz->tags[i];
  }

  return (w_method)clazz->values[i];
}

/*
** Get the Utf8 string from the constant pool array.
*/

w_string resolveUtf8Constant(w_clazz clazz, w_int i) {
  check_constant_index(clazz, i);
  return registerString((w_string)clazz->values[i]);
}

/*
** Add one more slot to the tag and values arrays. TODO: make this work
** more efficiently if called repeatedly. Result is old size = new size - 1.
** (This slot is guaranteed to be free, in fact it is uninitialised and
** the caller must set the tag immediately).
*/
static w_size expandConstantPool(w_clazz clazz) {
  w_size i = clazz->numConstants++;

  clazz->tags = reallocMem((void*)clazz->tags, clazz->numConstants);
  clazz->values = reallocMem((void*)clazz->values, clazz->numConstants * sizeof(void*));
  if (!clazz->tags || !clazz->values) {
    wabort(ABORT_WONKA, "Unable to realloc tags & values\n");
  }
  
  return i;
}

/*
** Add a new UTF8 constant to the pool, unless it already exists. Returns
** the index of the new or existing constant.
*/
w_int addUTF8ConstantToPool(w_clazz clazz, w_string string) {
  w_size i;

  for (i = 1; i < clazz->numConstants; ++i) {
    if (clazz->tags[i] == CONSTANT_UTF8 && clazz->values[i] == (w_word)string) {
      woempa(1, "Already existed as constant[%d]\n", i);
      return i;
    }
  }
    
/* [CG 20071014] Constants are no longer being deleted
  for (i = 1; i < clazz->numConstants; ++i) {
    if (clazz->tags[i] == CONSTANT_DELETED) {
      woempa(1, "Slot[%d] is free, recycling it\n", i);
      clazz->tags[i] = CONSTANT_UTF8;
      clazz->values[i] = (w_word)registerString(string);
      return i;
    }
  }
*/

  i = expandConstantPool(clazz);
  clazz->tags[i] = CONSTANT_UTF8;
  clazz->values[i] = (w_word)registerString(string);
  return i;
}

/*
** Add a new unresolved Class constant to the pool, unless it already exists. 
** Parameter 'classname_index' is the index of an existing UTF8 constant which
** holds the name of the class (this may previously have been added using
** addUTF8Constant()).
** No attempt will be made to resolve the constant (and hence load the class).
** Returns the index of the new or existing constant.
*/
w_int addUnresolvedClassConstantToPool(w_clazz clazz, w_size classname_index) {
  w_size i;

  woempa(1, "addUnresolvedClassConstantToPool(%k, %d)\n", clazz, classname_index);
  woempa(1, " -> class name is %w\n", clazz->values[classname_index]);
  for (i = 1; i < clazz->numConstants; ++i) {
    if (clazz->tags[i] == CONSTANT_CLASS && clazz->values[i] == classname_index) {
      woempa(1, "Already existed as constant[%d]\n", i);
      return i;
    }

    if (clazz->tags[i] == RESOLVED_CLASS && ((w_clazz)clazz->values[i])->dotified == (w_string)clazz->values[classname_index]) {
      woempa(1, "Already existed as constant[%d]\n", i);
      return i;
    }
  }
    
/* [CG 20071014] Constants are no longer being deleted
  for (i = 1; i < clazz->numConstants; ++i) {
    switch (clazz->tags[i]) {
    case CONSTANT_DELETED:
      woempa(1, "    constant[%d] has been deleted, recycling it\n", i);
      clazz->tags[i] = CONSTANT_CLASS;
      clazz->values[i] = classname_index;
      return i;
    }
  }
*/

  i = expandConstantPool(clazz);
  clazz->tags[i] = CONSTANT_CLASS;
  clazz->values[i] = classname_index;
  return i;
}

/*
** Add a new Name&Type constant to the pool, unless it already exists.
** Returns the index of the new or existing constant.
w_int addNatConstantToPool(w_clazz clazz, w_string name, w_string type) {
  w_int name_index = addUTF8ConstantToPool(clazz, name);
  w_int descriptor_index = addUTF8ConstantToPool(clazz, type);
  w_size i;
  w_Name_and_Type *natptr;

  woempa(1, "Adding the Name & Type `%w %w' to constant pool of %k.\n", name, type, clazz);

  for (i = 1; i < clazz->numConstants; ++i) {
    switch (clazz->tags[i]) {
    case CONSTANT_NAME_AND_TYPE:
      natptr = (w_Name_and_Type*)&clazz->values[i];
      if (natptr->name_index == name_index && natptr->descriptor_index == descriptor_index) {
        woempa(1, "Already existed as constant[%d]\n", i);
        return i;
      }
    }
  }
    
/. [CG 20071014] Constants are no longer being deleted
  for (i = 1; i < clazz->numConstants; ++i) {
    switch (clazz->tags[i]) {
    case CONSTANT_DELETED:
      natptr = (w_Name_and_Type*)&clazz->values[i];
      woempa(1, "Slot[%d] is free, recycling it\n", i);
      clazz->tags[i] = CONSTANT_NAME_AND_TYPE;
      natptr->name_index = name_index;
      natptr->descriptor_index = descriptor_index;
      return i;
    }
  }
./

  i = clazz->numConstants++;
  clazz->tags = reallocMem((void*)clazz->tags, clazz->numConstants);
  clazz->values = reallocMem((void*)clazz->values, clazz->numConstants * sizeof(void*));
  if (!clazz->tags || !clazz->values) {
    wabort(ABORT_WONKA, "Unable to realloc tags & values\n");
  }
  natptr = (w_Name_and_Type*)&clazz->values[i];
  woempa(1, "Adding as slot[%d]\n", i);
  clazz->tags[i] = CONSTANT_NAME_AND_TYPE;
  natptr->name_index = name_index;
  natptr->descriptor_index = descriptor_index;
  return i;
}
*/

/*
** Add a new resolved Field constant to the pool, unless it already exists. 
** Returns the index of the new or existing constant.
*/
w_int addResolvedFieldConstantToPool(w_clazz clazz, w_field field) {
  w_size i;

  for (i = 1; i < clazz->numConstants; ++i) {
    if (clazz->tags[i] == RESOLVED_FIELD && clazz->values[i] == (w_word)field) {
      woempa(1, "Already existed as constant[%d]\n", i);
      return i;
    }
  }
    
/* [CG 20071014] Constants are no longer being deleted
  for (i = 1; i < clazz->numConstants; ++i) {
    if (clazz->tags[i] == CONSTANT_DELETED) {
      woempa(1, "Slot[%d] is free, recycling it\n", i);
      clazz->tags[i] = RESOLVED_FIELD;
      clazz->values[i] = (w_word)field;
      return i;
    }
  }
*/

  i = expandConstantPool(clazz);
  clazz->tags[i] = RESOLVED_FIELD;
  clazz->values[i] = (w_word)field;
  return i;
}

/*
** Add a direct pointer constant to the pool, unless it already exists. 
** Returns the index of the new or existing constant.
*/
w_int addPointerConstantToPool(w_clazz clazz, void *ptr) {
  w_size i;

  for (i = 1; i < clazz->numConstants; ++i) {
    if (clazz->tags[i] == DIRECT_POINTER && clazz->values[i] == (w_word)ptr) {
      woempa(1, "Already existed as constant[%d]\n", i);
      return i;
    }
  }
    
/* [CG 20071014] Constants are no longer being deleted
  for (i = 1; i < clazz->numConstants; ++i) {
    if (clazz->tags[i] == CONSTANT_DELETED) {
      woempa(1, "Slot[%d] is free, recycling it\n", i);
      clazz->tags[i] = DIRECT_POINTER;
      clazz->values[i] = (w_word)ptr;
      return i;
    }
  }
*/

  i = expandConstantPool(clazz);
  clazz->tags[i] = DIRECT_POINTER;
  clazz->values[i] = (w_word)ptr;
  return i;
}

/*
** If ref_clazz is not already referenced by this_clazz (e.g. via 
** clazz->supers or clazz->interfaces, or of course via clazz->references), 
** add it to clazz->references. The calling thread must own 
** clazz->resolution_monitor.
*/
static void addClassReference(w_clazz this_clazz, w_clazz ref_clazz) {
  w_int i;
  w_thread thread;

  if (isSystemClassLoader(ref_clazz->loader)) {
    woempa(1, "%K is a system class, doing nowt\n", ref_clazz);

    return;

  }

  if (isInWordset(&this_clazz->references, (w_word)ref_clazz)) {
    woempa(1, "%K is already referenced by %K, doing nowt\n", ref_clazz, this_clazz);
  }

  if (ref_clazz == this_clazz) {
    woempa(1, "%K is same as %K, doing nowt\n", ref_clazz, this_clazz);

    return;

  }

  for (i = 0; i < this_clazz->numSuperClasses; ++i) {
    if (ref_clazz == this_clazz->supers[i]) {
      woempa(1, "%K is superclass of as %K, doing nowt\n", ref_clazz, this_clazz);

      return;

    }
  }

  for (i = 0; i < this_clazz->numInterfaces; ++i) {
    if (ref_clazz == this_clazz->interfaces[i]) {
      woempa(1, "%K is superinterface of as %K, doing nowt\n", ref_clazz, this_clazz);

      return;

    }
  }

  thread = currentWonkaThread;
  threadMustBeSafe(thread);
  enterUnsafeRegion(thread);
  woempa(1, "%K references %K\n", ref_clazz, this_clazz);
  if (!addToWordset(&this_clazz->references, (w_word)ref_clazz)) {
    wabort(ABORT_WONKA, "Could not add entry to clazz->references\n");
  }
  enterSafeRegion(thread);
}

/*
** Resolve a String constant.
*/
void resolveStringConstant(w_clazz clazz, w_int i) {
  w_thread  thread = currentWonkaThread;

  threadMustBeSafe(thread);
  x_monitor_eternal(clazz->resolution_monitor);
  if (clazz->tags[i] == CONSTANT_STRING) {
    w_ConstantType *c = &clazz->tags[i];
    w_ConstantValue *v = &clazz->values[i];
    w_int   utf8index = (w_int)*v;
    w_string s = resolveUtf8Constant(clazz, utf8index);
    w_instance theString = getStringInstance(s);

    if (!theString) {
      wabort(ABORT_WONKA, "Unable to get String instance for String constant\n");
    }
    woempa(1, "Resolved String constant[%d] of %k to `%w'\n", i, clazz, s);
    *v = (w_word)theString;
    *c = RESOLVED_STRING;
    removeLocalReference(thread, theString);
  }
  x_monitor_exit(clazz->resolution_monitor);
}

/*
** Determine whether the named class is a member of a wonka.* package;
** such classes may only be resolved by a system class. We check for
** 'wonka.*', '[[...wonka.*', 'Lwonka.*;', and '[[...Lwonka.*;'.
*/
static w_boolean isInternalClass(w_string name) {
  w_size len = string_length(name);
  w_size skip = 0;

  while (skip < len && string_char(name, skip) == '[') {
    ++skip;
    --len;
  }

  if (skip) {
    if (string_char(name, skip) == 'L') {
      ++skip;
      len -= 2;
    }
  }

  if (len > 6 &&
      string_char(name, skip + 0) == 'w' && 
      string_char(name, skip + 1) == 'o' &&
      string_char(name, skip + 2) == 'n' && 
      string_char(name, skip + 3) == 'k' &&
      string_char(name, skip + 4) == 'a' && 
      string_char(name, skip + 5) == '.')
  {

    return TRUE;

  }

  return FALSE;
}

/*
** Resolve a Class constant.
*/

static void reallyResolveClassConstant(w_clazz clazz, w_ConstantType *c, w_ConstantValue *v) {

  w_thread   thread = currentWonkaThread;
  w_clazz    target_clazz = NULL;
  w_instance loader = clazz2loader(clazz);
  w_int      utf8index = (w_int)*v;
  w_string   slashed = resolveUtf8Constant(clazz, utf8index);
  w_string   dotified = slashes2dots(slashed);

  if (thread) threadMustBeSafe(thread);

  if (!dotified) {
    wabort(ABORT_WONKA, "Unable to dotify name\n");
  }

  if (!isSystemClassLoader(loader) && isInternalClass(dotified)) {
    x_monitor_exit(clazz->resolution_monitor);
    throwException(thread, clazzLinkageError, "%w may only be loaded by a system class", dotified);
    
    *v = (w_word)exceptionThrown(thread);
    *c = COULD_NOT_RESOLVE;

    deregisterString(dotified);
    deregisterString(slashed);
    x_monitor_eternal(clazz->resolution_monitor);

    return;

  }

  *c = RESOLVING_CLASS;
  x_monitor_exit(clazz->resolution_monitor);
  woempa(1, "Utf8 index = %d --> class name is `%w'\n", *v, dotified);

  target_clazz = namedClassMustBeLoaded(loader, dotified);

  deregisterString(slashed);

  if (target_clazz) {
    mustBeSupersLoaded(target_clazz);

    addClassReference(clazz, target_clazz);
    x_monitor_eternal(clazz->resolution_monitor);
    *v = (w_word)target_clazz;
    *c = RESOLVED_CLASS;
    x_monitor_notify_all(clazz->resolution_monitor);

    return;

  }

  woempa(9, "Got a %k\n", instance2clazz(exceptionThrown(thread)));

  x_monitor_eternal(clazz->resolution_monitor);
  *v = (w_word)exceptionThrown(thread);
  *c = COULD_NOT_RESOLVE;
  x_monitor_notify_all(clazz->resolution_monitor);

}

/*
** Wait for another thread to finish resolving a class constant
*/
void waitForClassConstant(w_clazz clazz, w_ConstantType *c, w_ConstantValue *v) {
  w_thread   thread = currentWonkaThread;

  while (*c == RESOLVING_CLASS) {
    x_monitor_wait(clazz->resolution_monitor, 2);
  }

  if (*c == COULD_NOT_RESOLVE) {
    x_monitor_exit(clazz->resolution_monitor);
    throwExceptionInstance(thread, (w_instance)*v);
    x_monitor_eternal(clazz->resolution_monitor);
  }
}

/*
*/
void resolveClassConstant(w_clazz clazz, w_int i) {

  w_ConstantType *c = &clazz->tags[i];
  w_ConstantValue *v = &clazz->values[i];
  w_thread   thread = currentWonkaThread;
  threadMustBeSafe(thread);

  x_monitor_eternal(clazz->resolution_monitor);

  if (*c == CONSTANT_CLASS) {
    woempa(1, "Resolving Class constant[%d] of %k\n", i, clazz);
    reallyResolveClassConstant(clazz, c, v);
  }
  else if (*c == RESOLVING_CLASS) {
    waitForClassConstant(clazz, c, v);
  }
  else if (*c == RESOLVED_CLASS) {
    /* do nothing */
  }
  else if (*c == COULD_NOT_RESOLVE) {
    x_monitor_exit(clazz->resolution_monitor);
    throwExceptionInstance(thread, (w_instance)*v);
    x_monitor_eternal(clazz->resolution_monitor);
  }
  else {
    woempa(9, "Wrong tag %d for a Class constant\n", *c);
  }

  x_monitor_exit(clazz->resolution_monitor);
}

/*
** Check that a member is accessible, given its flags and the fact thay we 
** are or are not in the same class, in the same package, or a subclass
** of the class where the member is declared.
*/
static w_boolean checkAccess(w_flags flags, w_boolean same_class, w_boolean same_package, w_boolean ancestor) {
  w_boolean allowed = same_class || isSet(flags, ACC_PUBLIC);

  if (!allowed && isNotSet(flags, ACC_PRIVATE)) {
    allowed = same_package || (ancestor && isSet(flags, ACC_PROTECTED)); 
  }

  return allowed;
}

/*
** Search a class for a field with the correct name and descriptor.
** If none found, returns NULL. thread->exception may have been set if a
** class loading error occurred as a side-effect.
*/
static w_field seekFieldInClass(w_string name, w_string descriptor, w_clazz value_clazz, w_clazz search_clazz, w_clazz accessible_from) {
  w_boolean same_class = (search_clazz == accessible_from);
  w_boolean ancestor = isSuperClass(search_clazz, accessible_from);
  w_boolean same_package = sameRuntimePackage(accessible_from, search_clazz);
  w_int      i;

  for (i = 0; i < search_clazz->numFields; ++i) {
    w_field f = &search_clazz->own_fields[i];

    if (f->name == name) {
      if(checkAccess(f->flags, same_class, same_package, ancestor)) {
        if (mustBeLoaded(&f->value_clazz) == CLASS_LOADING_FAILED) {
          woempa(9, "  failed to load %K!\n", value_clazz);
        }
        else {
          woempa(1,"  matching field %w has value_clazz %K (%p), looking for %K (%p)\n", name, f->value_clazz, f->value_clazz, value_clazz, value_clazz);
          if (f->value_clazz == value_clazz) {
#ifdef PACK_BYTE_FIELDS
            woempa(1, "  found it in %K, size %d slot %d.\n", search_clazz, FIELD_SIZE(f->size_and_slot), FIELD_OFFSET(f->size_and_slot));
#else
            woempa(1, "  found it in %K, slot %d.\n", search_clazz, f->size_and_slot);
#endif

            return f;
          }
        }
      }
      else {
        throwException(currentWonkaThread, clazzIllegalAccessError, "Field %w in %k is not accessable from %k",
                       name,search_clazz,accessible_from);
        return NULL;
      } 
    }
  }

  return NULL;
}

/*
** Resolve a Field constant. Can result in the thread being temporarily
** declared GC-safe, so the stack must be in order.
*/
static void reallyResolveFieldConstant(w_clazz clazz, w_ConstantType *c, w_ConstantValue *v) {

  w_thread   thread = currentWonkaThread;
  w_field    field = NULL;
  w_clazz    search_clazz;
  w_clazz    super;
  w_string   desc_string;
  w_string   name;
  w_word     member;
  w_int      nat_index;
  w_word     nat;
  w_int      i;
  w_size     start;
  w_size     end;
  w_clazz    value_clazz;
  w_boolean  class_loading_result;

  *c = RESOLVING_FIELD;
  x_monitor_exit(clazz->resolution_monitor);
  member = *v;
  woempa(1, "Resolving field constant [%d] '0x%08x' from %k\n", v - clazz->values, member, clazz);
  search_clazz = getClassConstant(clazz, Member_get_class_index(member), thread);
  x_monitor_eternal(clazz->resolution_monitor);
  if (!search_clazz) {
    woempa(9, "  failed to load the class referenced by field constant[%d] of %K!\n", v - clazz->values, clazz);
    *v = (w_word)exceptionThrown(thread);
    *c = COULD_NOT_RESOLVE;
    return;
  }

  nat_index = Member_get_nat_index(member);
  nat = clazz->values[nat_index];
  woempa(1, "Name & Type = '0x%08x'\n", nat);
  name = resolveUtf8Constant(clazz, Name_and_Type_get_name_index(nat));
  desc_string = resolveUtf8Constant(clazz, Name_and_Type_get_type_index(nat));
  x_monitor_exit(clazz->resolution_monitor);
  woempa(1, "Searching for %w %w\n", desc_string, name);
  start = 0;
  end = string_length(desc_string);
  value_clazz = parseDescriptor(desc_string, &start, end, clazz->loader);

  class_loading_result = mustBeLoaded(&value_clazz);
  if (class_loading_result == CLASS_LOADING_FAILED) {
    woempa(9, "  failed to load %K, the type of field %w of %K!\n", value_clazz, name, search_clazz);
    x_monitor_eternal(clazz->resolution_monitor);
    *v = (w_word)exceptionThrown(thread);
    *c = COULD_NOT_RESOLVE;
    return;

  }

  if (search_clazz && !(thread && exceptionThrown(thread))) {
    mustBeReferenced(search_clazz);
    woempa(1, "Field '%w' (type %w) should be member of %K\n", name, desc_string, search_clazz);

    super = search_clazz;
    while (super && !field && !(thread && exceptionThrown(thread))) {
      woempa(1, "  searching %K.\n", super);
      field = seekFieldInClass(name, desc_string, value_clazz, super, clazz);

      if (field || (thread && exceptionThrown(thread))) {
        break;
      }

      woempa(1, "  not found in %K.\n", super);
      super = getSuper(super);
    }

    for (i = 0; i < search_clazz->numInterfaces && !field && !(thread && exceptionThrown(thread)); ++i) {
      super = search_clazz->interfaces[i];
      woempa(1, "  searching %K.\n", super);
      field = seekFieldInClass(name, desc_string, value_clazz, super, clazz);

      if (field || (thread && exceptionThrown(thread))) {
        break;
      }

      woempa(1, "  not found in %K.\n", super);
    }

    if (!field && thread && !exceptionThrown(thread)) {
      throwException(thread, clazzNoSuchFieldError, "%k: no field '%w' with type %w found in %K or its superclasses", clazz, name, desc_string, search_clazz);
    }

    deregisterString(desc_string);

    if (!thread || ! exceptionThrown(thread)) {
      addClassReference(clazz, field->value_clazz);
      x_monitor_eternal(clazz->resolution_monitor);
      *v = (w_word)field;
      *c = RESOLVED_FIELD;
      x_monitor_notify_all(clazz->resolution_monitor);

      return;

    }
  }
  
  x_monitor_eternal(clazz->resolution_monitor);
  *v = (w_word)exceptionThrown(thread);
  *c = COULD_NOT_RESOLVE;
  x_monitor_notify_all(clazz->resolution_monitor);

}

/*
** Wait for another thread to finish resolving a field constant
*/
void waitForFieldConstant(w_clazz clazz, w_ConstantType *c, w_ConstantValue *v) {
  w_thread   thread = currentWonkaThread;

  while (*c == RESOLVING_FIELD) {
    x_monitor_wait(clazz->resolution_monitor, 2);
  }

  if (*c == COULD_NOT_RESOLVE) {
    x_monitor_exit(clazz->resolution_monitor);
    throwExceptionInstance(thread, (w_instance)*v);
    x_monitor_eternal(clazz->resolution_monitor);
  }
}

/**
 ** Resolve a Field constant.
 */
void resolveFieldConstant(w_clazz clazz, w_int i) {
  w_ConstantType *c;
  w_ConstantValue *v;
  w_thread   thread = currentWonkaThread;
  threadMustBeSafe(thread);

  x_monitor_eternal(clazz->resolution_monitor);
  c = &clazz->tags[i];
  v = &clazz->values[i];

  if (*c == CONSTANT_FIELD) {
    woempa(1, "Resolving Field constant[%d] of %k\n", i, clazz);
    reallyResolveFieldConstant(clazz, c, v);
  }
  else if (*c == RESOLVING_FIELD) {
    waitForFieldConstant(clazz, c, v);
  }
  else if (*c == RESOLVED_FIELD) {
    /* do nothing */
  }
  else if (*c == COULD_NOT_RESOLVE) {
    x_monitor_exit(clazz->resolution_monitor);
    throwExceptionInstance(thread, (w_instance)*v);
    x_monitor_eternal(clazz->resolution_monitor);
  }
  else {
    woempa(9, "Wrong tag %d for a Field constant\n", *c);
  }
  x_monitor_exit(clazz->resolution_monitor);
}

/*
** Search a class for a method with the correct name and descriptor.
** If none found, returns NULL. thread->exception may have been set if a
** class loading error occurred as a side-effect.
*/
static w_method seekMethodInClass(w_string name, w_string desc_string, w_MethodSpec *spec, w_clazz search_clazz, w_clazz accessible_from) {
  w_boolean same_class = (search_clazz == accessible_from);
  w_boolean ancestor = isSuperClass(search_clazz, accessible_from);
  w_boolean same_package = sameRuntimePackage(accessible_from, search_clazz);
  w_method m;
  w_size   i;

  for (i = 0; i < search_clazz->numDeclaredMethods; ++i) {
    m = &search_clazz->own_methods[i];

    woempa(1, "Candidate: %w%w\n", m->spec.name, m->desc);
    woempa(1, "Is %spublic, %sprivate, %sprotected, %ssame package, %sancestor\n", isSet(m->flags, ACC_PUBLIC) ? "" : "not ", isSet(m->flags, ACC_PRIVATE) ? "" : "not ", isSet(m->flags, ACC_PROTECTED) ? "" : "not ", same_package ? " " : "not ", ancestor ? " " : "not ");
    if (m->spec.name == name && m->desc == desc_string) {
      woempa(1, "=> name matches, descriptor matches ...\n");
      if (methodMatchesSpec(m, spec)) {

        woempa(1, "=> parameter types match ...\n");
        if (checkAccess(m->flags, same_class, same_package, ancestor)) {
          woempa(1, "=> access allowed, match succeeds\n");

          return m;

        }
        woempa(1, "=> access forbidden, match fails\n");
        throwException(currentWonkaThread, clazzIllegalAccessError, "Method %w in %k is not accessable from %k",
                       name,search_clazz,accessible_from);
        return NULL;
      }
    }
  }

  return NULL;
}

/*
** Resolve a Method constant.
*/

static void reallyResolveMethodConstant(w_clazz clazz, w_ConstantType *c, w_ConstantValue *v) {

  w_thread   thread = currentWonkaThread;
  w_method   method = NULL;
  w_clazz    search_clazz;
  w_clazz    super;
  w_string name;
  w_string desc_string;
  w_size   j;
  w_word   member = *v;
  w_word   nat;
  w_MethodSpec *spec = NULL;

  *c += RESOLVING_CONSTANT;
  x_monitor_exit(clazz->resolution_monitor);
  woempa(1, "Resolving method constant [%d] '0x%08x' from %k\n", v - clazz->values, member, clazz);
  search_clazz = getClassConstant(clazz, Member_get_class_index(member), thread);

  if (search_clazz) {
    woempa(1, "Method was declared in class %k.\n", search_clazz);
    /*
    ** Look in the class for a method with the correct name
    ** and descriptor.
    */
    nat = clazz->values[Member_get_nat_index(member)];
    woempa(1, "Name & Type = '0x%08x'\n", nat);
    name = resolveUtf8Constant(clazz, Name_and_Type_get_name_index(nat));
    desc_string = resolveUtf8Constant(clazz, Name_and_Type_get_type_index(nat));
    woempa(1,"  -> name is `%w', descriptor is `%w'\n",name,desc_string);

    if ((mustBeReferenced(search_clazz) != CLASS_LOADING_FAILED) && (createMethodSpecUsingDescriptor(search_clazz, name, desc_string, &spec) != CLASS_LOADING_FAILED)) {
      woempa(1,"Seeking method %w%w in %K\n", name, desc_string, search_clazz);
      method = seekMethodInClass(name, desc_string, spec, search_clazz, clazz);
 
      for (j = 0; !method && j < search_clazz->numSuperClasses; ++j) {
        super = search_clazz->supers[j];
        woempa(1,"Seeking method %w%w in %K\n", name, desc_string, super);
        method = seekMethodInClass(name, desc_string, spec, super, clazz);

      }

      if (spec) {
        releaseMethodSpec(spec);
        releaseMem(spec);
      }
    }


    if (!method && thread && !exceptionThrown(thread)) {
        throwException(thread, clazzNoSuchMethodError, "%k: no method %w with signature %w found in %K or its superclasses", clazz, name, desc_string, search_clazz);
    }

    if (!thread || !exceptionThrown(thread)) {
      woempa(1, "found method %w%w in class %k.\n", name, desc_string, search_clazz);

      if (!thread || !exceptionThrown(thread)) {
        if (method->spec.arg_types) {
          for (j = 0; method->spec.arg_types[j]; ++j) {
            woempa(1, "%M argument[%d] type %K must be loaded\n", method, j, method->spec.arg_types[j]);
            addClassReference(clazz, method->spec.arg_types[j]);
          }
        }

        if (!thread || !exceptionThrown(thread)) {
          woempa(1, "%M return type %K must be loaded\n", method, method->spec.return_type);
          addClassReference(clazz, method->spec.return_type);
        }

        x_monitor_eternal(clazz->resolution_monitor);

        *c += RESOLVED_CONSTANT - RESOLVING_CONSTANT;
        *v = (w_word)method;
        x_monitor_notify_all(clazz->resolution_monitor);
        return;
      }
    }

  }

  x_monitor_eternal(clazz->resolution_monitor);
  *v = (w_word)exceptionThrown(thread);
  *c = COULD_NOT_RESOLVE;
  x_monitor_notify_all(clazz->resolution_monitor);

}

/*
** Wait for another thread to finish resolving a Method constant
*/
void waitForMethodConstant(w_clazz clazz, w_ConstantType *c, w_ConstantValue *v) {
  w_thread   thread = currentWonkaThread;

  while (*c == RESOLVING_METHOD) {
    x_monitor_wait(clazz->resolution_monitor, 2);
  }

  if (*c == COULD_NOT_RESOLVE) {
    x_monitor_exit(clazz->resolution_monitor);
    throwExceptionInstance(thread, (w_instance)*v);
    x_monitor_eternal(clazz->resolution_monitor);
  }
}

void resolveMethodConstant(w_clazz clazz, w_int i) {
  w_ConstantType *c;
  w_ConstantValue *v;
  w_thread   thread = currentWonkaThread;
  threadMustBeSafe(thread);

  x_monitor_eternal(clazz->resolution_monitor);
  c = &clazz->tags[i];
  v = &clazz->values[i];

  if (clazz->tags[i] < RESOLVED_CONSTANT) {

    if (*c == CONSTANT_METHOD) {
      woempa(1, "Resolving Method constant [%d] of %k\n", i, clazz);
      reallyResolveMethodConstant(clazz, c, v);
    }
    else if (*c == RESOLVING_METHOD) {
      waitForMethodConstant(clazz, c, v);
    }
    else if (*c == COULD_NOT_RESOLVE) {
      x_monitor_exit(clazz->resolution_monitor);
      throwExceptionInstance(thread, (w_instance)*v);
      x_monitor_eternal(clazz->resolution_monitor);
    }
    else {
      woempa(9, "Wrong tag %d for a Method constant\n", *c);
    }
  }
  x_monitor_exit(clazz->resolution_monitor);
}

/*
** Resolve an IMethod constant.
** We search the interface specified in the IMethod constant and all its
** superinterfaces for a method which matches the name and descriptor;
** if we find a match then we call methodMatchesSpec to check that the
** classes of the parameters and return value really do match. We create
** the spec which we use for comparison using the class loader of the 
** interface specified in the IMethod constant, and force the classes of
** its parameters and return type to be loaded; so if a superinterface was
** loaded by a different class loader then methodMatchesSpec may also need
** to force loading of the parameter/return types. 
*/

static void reallyResolveIMethodConstant(w_clazz clazz, w_ConstantType *c, w_ConstantValue *v) {

  w_thread   thread = currentWonkaThread;
  w_method   method = NULL;
  w_method   candidate;
  w_clazz    search_clazz;
  w_clazz    current_clazz;
  w_string name;
  w_string desc_string;
  w_size   i;
  w_size   j;
  w_word   member = *v;
  w_word   nat;

  *c += RESOLVING_CONSTANT;
  x_monitor_exit(clazz->resolution_monitor);
  woempa(1, "Resolving imethod constant [%d] '0x%08x' from %k\n", v - clazz->values, member, clazz);
  search_clazz = getClassConstant(clazz, Member_get_class_index(member), thread);
  nat = clazz->values[Member_get_nat_index(member)];
  woempa(1, "Name & Type = '0x%08x'\n", nat);
  name = resolveUtf8Constant(clazz, Name_and_Type_get_name_index(nat));
  desc_string = resolveUtf8Constant(clazz, Name_and_Type_get_type_index(nat));
  woempa(1,"  -> name is `%w', descriptor is `%w'\n",name,desc_string);

  if (search_clazz) {
    w_MethodSpec *spec = NULL;
    woempa(1, "IMethod %w%w should be declared in class %K.\n", name, desc_string, search_clazz);

    if ((mustBeReferenced(search_clazz) != CLASS_LOADING_FAILED) && (createMethodSpecUsingDescriptor(search_clazz, name, desc_string, &spec) != CLASS_LOADING_FAILED)) {
      current_clazz = search_clazz;
      woempa(1, "  searching %K\n", current_clazz);
      for (i = 0; i < current_clazz->numDeclaredMethods; ++i) {
        candidate = &current_clazz->own_methods[i];
        if (candidate->spec.name == name && candidate->desc == desc_string) {
          method = candidate;
          break;
        }
      }

      if (method && !methodMatchesSpec(method, spec)) {
        woempa(1, "found a method %w%w in class %k, but on closer inspection the parameters / return type do not match.\n", name, desc_string, current_clazz);
        method = NULL;
      }
      else if (!method) {
        woempa(1, "did not find interface method %w%w in class %k.\n", name, desc_string, current_clazz);
        for (j = 0; j < search_clazz->numInterfaces && !method; ++j) {
          if (thread && exceptionThrown(thread)) {
            break;
	  }
          current_clazz = search_clazz->interfaces[j];
          woempa(1, "  searching %K\n", current_clazz);
          spec->declaring_clazz = current_clazz;
          for (i = 0; i < current_clazz->numDeclaredMethods; ++i) {
             candidate = &current_clazz->own_methods[i];
            if (candidate->spec.name == name && candidate->desc == desc_string) {
              method = candidate;
              break;
            }
          }
          if (!method) {
            woempa(1, "did not find method %w%w in class %k.\n", name, desc_string, current_clazz);
          }
          else if (!methodMatchesSpec(method, spec)) {
            woempa(1, "found a method %w%w in class %k, but on closer inspection the parameters / return type do not match.\n", name, desc_string, current_clazz);
            method = NULL;
            break;
          }
        }
      }

      if (spec) {
        releaseMethodSpec(spec);
        releaseMem(spec);
      }
    }

    if (!method && (!thread || !exceptionThrown(thread))) {
        throwException(thread, clazzLinkageError, "%k: no interface method %w with signature %w found in %K or its superinterfaces", clazz, name, desc_string, search_clazz);
    }

    if (!thread || !exceptionThrown(thread)) {
      woempa(1, "found interface method %w%w in class %k.\n", name, desc_string, search_clazz);

      if (!thread || !exceptionThrown(thread)) {
        if (method->spec.arg_types) {
          for (j = 0; method->spec.arg_types[j]; ++j) {
            addClassReference(clazz, method->spec.arg_types[j]);
          }
        }

        if (!thread || !exceptionThrown(thread)) {
          addClassReference(clazz, method->spec.return_type);
        }

        x_monitor_eternal(clazz->resolution_monitor);

        *c += RESOLVED_CONSTANT - RESOLVING_CONSTANT;
        *v = (w_word)method;
        x_monitor_notify_all(clazz->resolution_monitor);
        return;
      }
    }

  }

  x_monitor_eternal(clazz->resolution_monitor);
  *v = (w_word)exceptionThrown(thread);
  *c = COULD_NOT_RESOLVE;
  x_monitor_notify_all(clazz->resolution_monitor);

}

/*
** Wait for another thread to finish resolving an IMethod constant
*/
void waitForIMethodConstant(w_clazz clazz, w_ConstantType *c, w_ConstantValue *v) {
  w_thread   thread = currentWonkaThread;

  while (*c == RESOLVING_IMETHOD) {
    x_monitor_wait(clazz->resolution_monitor, 2);
  }

  if (*c == COULD_NOT_RESOLVE) {
    x_monitor_exit(clazz->resolution_monitor);
    throwExceptionInstance(thread, (w_instance)*v);
    x_monitor_eternal(clazz->resolution_monitor);
  }
}

void resolveIMethodConstant(w_clazz clazz, w_int i) {
  w_ConstantType *c;
  w_ConstantValue *v;
  w_thread   thread = currentWonkaThread;
  threadMustBeSafe(thread);

  x_monitor_eternal(clazz->resolution_monitor);
  c = &clazz->tags[i];
  v = &clazz->values[i];

  if (clazz->tags[i] < RESOLVED_CONSTANT) {

    if (*c == CONSTANT_IMETHOD) {
      woempa(1, "Resolving IMethod constant [%d] of %k\n", i, clazz);
      reallyResolveIMethodConstant(clazz, c, v);
    }
    else if (*c == RESOLVING_IMETHOD) {
      waitForIMethodConstant(clazz, c, v);
    }
    else if (*c == COULD_NOT_RESOLVE) {
      x_monitor_exit(clazz->resolution_monitor);
      throwExceptionInstance(thread, (w_instance)*v);
      x_monitor_eternal(clazz->resolution_monitor);
    }
    else {
      woempa(9, "Wrong tag %d for an IMethod constant\n", *c);
    }
  }
  x_monitor_exit(clazz->resolution_monitor);
}

void dissolveConstant(w_clazz clazz, int idx) {
  w_ConstantType *c = &clazz->tags[idx];

  if (*c == CONSTANT_UTF8) {
    if (clazz->values[idx]) {
      deregisterString((w_string)clazz->values[idx]);
    }
  }
}

/*
** Get the class name from a Class constant, without resolving the constant if
** it is not already resolved. Returns the descriptor of the class if
** the operation succeeded, NULL if it failed e.g. because the constant is in
** state COULD_NOT_RESOLVE.
** The resulting w_string is registered, so remember to deregister it afterwards.
*/
static w_string getClassConstantName(w_clazz clazz, w_int idx) {
  w_string result = NULL;
  threadMustBeSafe(currentWonkaThread);

  x_monitor_eternal(clazz->resolution_monitor);
  while (clazz->tags[idx] == RESOLVING_CLASS) {
    x_monitor_wait(clazz->resolution_monitor, 2);
  }

  if (clazz->tags[idx] == CONSTANT_CLASS) {
    w_int name_index = clazz->values[idx];
    w_string name = resolveUtf8Constant(clazz, name_index);

    result = name;

  }
  else if (CONSTANT_STATE(clazz->tags[idx]) == RESOLVED_CONSTANT) {
    w_clazz c = (w_clazz)clazz->values[idx];

    result = dots2slashes(c->dotified);

  }
  // else we return NULL (e.g. COULD_NOT_RESOLVE)

  x_monitor_exit(clazz->resolution_monitor);

  return result;
}

/*
** Utility method used by getMemberConstantStrings() when the constant is
** already resolved.
*/
static w_boolean internal_getMemberConstantStrings(w_clazz clazz, w_int idx, w_string *declaring_clazz_ptr, w_string *member_name_ptr, w_string *member_type_ptr) {
  w_method m;
  w_field f;

  if (isFieldConstant(clazz, idx)) {
    f = (w_field)clazz->values[idx];
    woempa(1, "Resolved Field constant: %k %w %k\n", f->declaring_clazz, f->name, f->value_clazz);
    if (declaring_clazz_ptr) {
      *declaring_clazz_ptr = dots2slashes(f->declaring_clazz->dotified);
    }
    if (member_name_ptr) {
      *member_name_ptr = registerString(f->name);
    }
    if (member_type_ptr) {
      *member_type_ptr = registerString(f->desc);
    }

    return TRUE;
  }
  else if (isMethodConstant(clazz, idx) || isIMethodConstant(clazz, idx)) {
    m = (w_method)clazz->values[idx];
    woempa(1, "Resolved [I]Method constant: %k %w %w\n", m->spec.declaring_clazz, m->spec.name, m->desc);
    if (declaring_clazz_ptr) {
      *declaring_clazz_ptr = dots2slashes(m->spec.declaring_clazz->dotified);
    }
    if (member_name_ptr) {
      *member_name_ptr = registerString(m->spec.name);
    }
    if (member_type_ptr) {
      *member_type_ptr = registerString(m->desc);
    }

    return TRUE;
  }
  else {
    woempa(9, "Constant tag[%d] of %k is 0x%02x!\n", idx, clazz, clazz->tags[idx]);

    return FALSE;

  }
}

/*
** Get the declaring class name, name, and/or descriptor from a Field, Method, 
** or IMethod constant, without resolving the constant if it is not already 
** resolved. Only the strings for which the corresponding w_string* parameter
** is non-null will be extracted. Returns TRUE if the operation succeeded,
** FALSE if it failed e.g. because the constant is in state COULD_NOT_RESOLVE.
** The resulting w_string's are registered, so remember to deregister
** them afterwards.
*/
w_boolean getMemberConstantStrings(w_clazz clazz, w_int idx, w_string *declaring_clazz_ptr, w_string *member_name_ptr, w_string *member_type_ptr) {
  w_boolean result = FALSE;

  threadMustBeSafe(currentWonkaThread);

  x_monitor_eternal(clazz->resolution_monitor);

  while (CONSTANT_STATE(clazz->tags[idx]) == RESOLVING_CONSTANT) {
    x_monitor_wait(clazz->resolution_monitor, 2);
  }

  if (CONSTANT_STATE(clazz->tags[idx]) == UNRESOLVED_CONSTANT && clazz->tags[idx] != COULD_NOT_RESOLVE) {
    w_word member = clazz->values[idx];

    woempa(1, "Unresolved constant\n");
    if (declaring_clazz_ptr) {
      w_int cls_idx = Member_get_class_index(member);
      w_string declaring_clazz_name = getClassConstantName(clazz, cls_idx);

      if (declaring_clazz_name) {
        woempa(1, "  declaring class index = %d, name = %w\n", cls_idx, *declaring_clazz_ptr);
        *declaring_clazz_ptr = declaring_clazz_name;
      }
      else {
        x_monitor_exit(clazz->resolution_monitor);

        return FALSE;
      }
    }
    if (member_name_ptr || member_type_ptr) {
      w_int nat_idx = Member_get_nat_index(member);
      w_word nat = clazz->values[nat_idx];
      w_int name_idx = Name_and_Type_get_name_index(nat);
      w_int type_idx = Name_and_Type_get_type_index(nat);

      woempa(1, "  nat index = %d, nat = 0x%08x\n", nat_idx, nat);
      if (member_name_ptr) {
        *member_name_ptr = resolveUtf8Constant(clazz, name_idx);
        woempa(1, "  name index = %d, name = %w\n", name_idx, *member_name_ptr);
      }
      if (member_type_ptr) {
        *member_type_ptr = resolveUtf8Constant(clazz, type_idx);
        woempa(1, "  type index = %d, type = %w\n", type_idx, *member_type_ptr);
      }
    }
    result = TRUE;
  }
  else if (CONSTANT_STATE(clazz->tags[idx]) == RESOLVED_CONSTANT) {

    result = internal_getMemberConstantStrings(clazz, idx, declaring_clazz_ptr, member_name_ptr, member_type_ptr);

  }
  // else we return FALSE (e.g. COULD_NOT_RESOLVE)

  x_monitor_exit(clazz->resolution_monitor);

  return result;
}

#ifdef DEBUG
/*
** Print out all contant pool entries of a class.
*/
void dumpPools(int fd, w_clazz clazz) {

  w_size i;
  w_ConstantType *c;

  woempa(1,"=== Constant Pool Dump for %k (fd = %d) ===\n", clazz, fd);
  for (i = 1; i < clazz->numConstants; i++) {
    if (clazz->tags) {
      c = &clazz->tags[i];
      switch (*c) {
      case CONSTANT_CLASS:
        woempa(1, "%4d Class:     utf8 index %d\n", i, clazz->values[i]);
        break;

      case RESOLVING_CLASS:
        woempa(1, "%4d Class (resolving)\n", i);
        break;

      case RESOLVED_CLASS:
        woempa(1, "%4d Class (resolved): %k\n", i, clazz->values[i]);
        break;

      case CONSTANT_UTF8:
        woempa(1, "%4d UTF8:      '%w'\n", i, clazz->values[i]);
        break;

      case CONSTANT_INTEGER:
        woempa(1, "%4d Integer    0x%x\n", i, clazz->values[i]);
        break;

      case CONSTANT_FLOAT:
        woempa(1, "%4d Float:     0x%x\n", i, clazz->values[i]);
        break;

      case CONSTANT_LONG:
        woempa(1, "%4d Long:      0x%x%x\n", i, clazz->values[i + WORD_MSW], clazz->values[i + WORD_LSW]);
        break;

      case CONSTANT_DOUBLE:
        woempa(1, "%4d Double:    0x%x%x\n", i, clazz->values[i + WORD_MSW], clazz->values[i + WORD_LSW]);
        break;

      case CONSTANT_STRING:
        woempa(1, "%4d String (unresolved):    index %d\n", i, clazz->values[i]);
        break;

      case RESOLVED_STRING:
        woempa(1, "%4d String (resolved): `%w'\n", i, String2string((w_instance)clazz->values[i]));
        break;

      case CONSTANT_FIELD:
        {
          u4 member = clazz->values[i];
          woempa(1, "%4d Field (unresolved):     class index %d name&type %d\n", i, Member_get_class_index(member), Member_get_nat_index(member));
        }
        break;

      case RESOLVING_FIELD:
        woempa(1, "%4d Field (resolving)\n, i");
        break;

      case RESOLVED_FIELD:
        {
          w_field field = (w_field)clazz->values[i];
          woempa(1, "%4d Field (resolved): %k %w of %k\n", i, field->value_clazz, field->name, field->declaring_clazz);
        }
        break;

      case CONSTANT_METHOD:
        {
          u4 member = clazz->values[i];
          woempa(1, "%4d Method (unresolved):    class index %d name&type %d\n", i, Member_get_class_index(member), Member_get_nat_index(member));
        }
        break;

      case RESOLVING_METHOD:
        woempa(1, "%4d Method (resolving)\n", i);
        break;

      case RESOLVED_METHOD:
        {
          w_method method = (w_method)clazz->values[i];
          woempa(1, "%4d Method (resolved): %M\n", i, method);
        }
        break;

      case CONSTANT_IMETHOD:
        {
          u4 member = clazz->values[i];
          woempa(1, "%4d IMethod (unresolved):   class index %d name&type %d\n", i, Member_get_class_index(member), Member_get_nat_index(member));
        }
        break;

      case RESOLVING_IMETHOD:
        woempa(1, "%4d IMethod (resolving)\n", i);
        break;

      case RESOLVED_IMETHOD:
        {
          w_method method = (w_method)clazz->values[i];
          woempa(1, "%4d IMethod (resolved): %M\n", i, method);
        }
        break;

      case CONSTANT_NAME_AND_TYPE:
        {
          u4 nat = clazz->values[i];
        woempa(1, "%4d Name&type: name index %d descriptor index %d\n", i, Name_and_Type_get_name_index(nat), Name_and_Type_get_type_index(nat)); 
        }
        break;

/* [CG 20071014] Constants are no longer being deleted
      case CONSTANT_DELETED:
        woempa(1,"%4d Constant was no longer needed, has been cleaned up\n", i);
        break;
*/

      case COULD_NOT_RESOLVE:
        woempa(1,"%4d Failed to resolve constant: threw %k\n", i, instance2clazz((w_instance)clazz->values[i]));
        break;

      case NO_VALID_ENTRY:
        woempa(1,"%4d Skipping second word of double-size constant\n", i);
        break;

      default:
        woempa(9,"clazz %k index %d *c = %d\n", clazz, i, *c);
        wabort(ABORT_WONKA, "Should not get here...\n");
      }
    }
  }
  woempa(1,"=== End Constant Pool Dump for %k ===\n", clazz);

}
#endif


