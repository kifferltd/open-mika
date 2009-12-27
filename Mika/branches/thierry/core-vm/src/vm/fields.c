/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2005, 2006 by Chris Gray, /k/ Embedded Java   *
* Solutions. All rights reserved.                                         *
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

#include "fields.h"
#include "loading.h"
#include "wstrings.h"
#include "clazz.h"
#include "descriptor.h"
#include "ts-mem.h"
#include "threads.h"
#include "wonka.h"

char * print_field_short(char * buffer, int * remain, void * data, int w, int p, unsigned int f) {

  w_field  field = data;
  w_int    nbytes;
  char    *temp;
  w_int    i;

  if (*remain < 1) {

    return buffer;

  }

  temp = buffer;

  if (field == NULL) {
    strncpy(temp, (char *)"<NULL>", *remain);
    if (*remain < 6) {
      temp += *remain;
      *remain = 0;
    }
    else {
      temp += 6;
      *remain -= 6;
    }

    return temp;

  }

  if (field->value_clazz) {
    nbytes = x_snprintf(temp, *remain, "%k", field->value_clazz);
  }
  else if (field->desc) {
    nbytes = x_snprintf(temp, *remain, "%w", field->desc);
  }
  else {
    nbytes = x_snprintf(temp, *remain, "???");
  }
  *remain -= nbytes;
  temp += nbytes;

  nbytes = x_snprintf(temp, *remain, " %w", field->name);
  *remain -= nbytes;

  return temp + nbytes;
}

char * print_field_long(char * buffer, int * remain, void * data, int w, int p, unsigned int f) {

  w_field field = data;
  w_int    nbytes;
  char    *temp;
  w_int    i;

  if (*remain < 1) {

    return buffer;

  }

  temp = buffer;

  if (field == NULL) {
    strncpy(temp, (char *)"<NULL>", *remain);
    if (*remain < 6) {
      temp += *remain;
      *remain = 0;
    }
    else {
      temp += 6;
      *remain -= 6;
    }

    return temp;

  }
  
  if (isSet(field->flags, ACC_PUBLIC)) {
    nbytes = x_snprintf(temp, *remain, "public ");
    *remain -= nbytes;
    temp += nbytes;
  }
  else if (isSet(field->flags, ACC_PROTECTED)) {
    nbytes = x_snprintf(temp, *remain, "protected ");
    *remain -= nbytes;
    temp += nbytes;
  }
  else if (isSet(field->flags, ACC_PRIVATE)) {
    nbytes = x_snprintf(temp, *remain, "private ");
    *remain -= nbytes;
    temp += nbytes;
  }

  if (isSet(field->flags, ACC_STATIC)) {
    nbytes = x_snprintf(temp, *remain, "static ");
    *remain -= nbytes;
    temp += nbytes;
  }

  if (isSet(field->flags, ACC_FINAL)) {
    nbytes = x_snprintf(temp, *remain, "final ");
    *remain -= nbytes;
    temp += nbytes;
  }

  if (field->value_clazz) {
    nbytes = x_snprintf(temp, *remain, "%k", field->value_clazz);
  }
  else if (field->desc) {
    nbytes = x_snprintf(temp, *remain, "%w", field->desc);
  }
  else {
    nbytes = x_snprintf(temp, *remain, "???");
  }
  *remain -= nbytes;
  temp += nbytes;

  nbytes = x_snprintf(temp, *remain, " %w", field->name);
  *remain -= nbytes;
  temp += nbytes;

  nbytes = x_snprintf(temp, *remain, " of %K", field->declaring_clazz);
  *remain -= nbytes;

  return temp + nbytes;
}

/*
** See if a given field structure matches with a name, descriptor, access mode and the
** static mode given in the argument list.
**
**        field : the field structure to check
**         desc : the descriptor to match against, when NULL it always matches
**  matchStatic : 1 should match 'static', -1 should match non static, 0 don't care.
** matchPrivate : 1 should match 'private', -1 should match 'public', 0 don't care.
**
**      Returns : 1 when there is a match, 0 when no match.
*/

w_boolean fieldMatch(w_field field, w_string name, w_string desc_string, w_int matchStatic, w_int matchPrivate) {

  woempa(1, "Comparing %w (%s, %s) against %w (%s, %s).\n", 
    NM(field), isSet(field->flags, ACC_STATIC) ? "static" : "instance", isSet(field->flags, ACC_PUBLIC) ? "public" : "private",
    name, 
    (matchStatic == MATCH_ANY)  ? "static or instance" : (matchStatic == MATCH_STATIC_FIELD)   ? "static" : "instance", 
    (matchPrivate == MATCH_ANY) ? "private or public"  : (matchPrivate == MATCH_PRIVATE_FIELD) ? "private" : "public");

  if (field->name == name) {
    woempa(1, "--> names match\n");
    if ((matchStatic == MATCH_ANY) 
      || ((matchStatic == MATCH_STATIC_FIELD) && isSet(field->flags, ACC_STATIC))
      || ((matchStatic == MATCH_INSTANCE_FIELD) && isNotSet(field->flags, ACC_STATIC))
      ) 
    {
      woempa(1, "--> static or instance match.\n");
      if ((matchPrivate == MATCH_ANY) 
        || ((matchPrivate == MATCH_PRIVATE_FIELD) && isSet(field->flags, ACC_PRIVATE))
        || ((matchPrivate == MATCH_PUBLIC_FIELD) && isSet(field->flags, ACC_PUBLIC))
        ) 
      {
        woempa(1, "--> public or private match.\n");
        if (desc_string) {
          w_string field_desc = clazz2desc(field->value_clazz);
          w_boolean result;
          if (!field_desc) {
            wabort(ABORT_WONKA, "Unable to create field_desc\n");
          }
          result = (desc_string == field_desc);
          woempa(1, "--> descriptor is %w, looking for %w\n", field_desc, desc_string);

          deregisterString(field_desc);

          return result;

        }
        return WONKA_TRUE;
      }
    }
  }

  return 0;
  
}

/*
** The following function performs the steps to find a given field with name,
** descriptor, private or public nature and static or instance field nature.
** The function should search for the field at the current level of hierarchy
** of the class and in all its superinterfaces.
**
**        clazz : the clazz to find the field in.
**       string : the name of the field.
**         desc : the descriptor for the field, when NULL it always matches.
**  matchStatic : 1 should match 'static', -1 should match with instance field, 0 don't care.
** matchPrivate : 1 should match 'private', -1 should match 'public', 0 don't care.
**
**      Returns : the field structure if found, NULL when not found.
**
*/

w_field searchClazzAndInterfacesForField(w_clazz clazz, w_string name, w_string desc_string, w_int matchStatic, w_int matchPrivate) {

  w_field field;
  w_int k;
  w_size i;
  w_size j;
  w_clazz current;

  if (mustBeReferenced(clazz) == CLASS_LOADING_FAILED) {

    return NULL;

  }

  /*
  ** Let's see first if we declare a field in clazz
  ** that has the simple name of 'name' and that satisfies the 'instance' and
  ** 'public' flags.
  */

  for (k = clazz->numFields - 1; k >= 0; k--) {
    field = &clazz->own_fields[k];
    if (fieldMatch(field, name, desc_string, matchStatic, matchPrivate)) {
      return field;
    }
  }

  /*
  ** If we didn't find such a field, we recursively go over all our direct superinterfaces 
  ** in the order that they were declared.
  */

  for (i = 0; i < clazz->numInterfaces; i++) {
    current = clazz->interfaces[i];
    for (j = 0; j < current->numFields; j++) {
      field = &current->own_fields[j];
      if (fieldMatch(field, name, desc_string, matchStatic, matchPrivate)) {
        return field;
      }
    }
  }

  return NULL;
    
}

/*
** Search a clazz and all it's superclasses and superinterfaces for a matching field.
**
**        clazz : the clazz to find the field in.
**         name : the name of the field.
**         desc : the descriptor for the field, when NULL it always matches.
**  matchStatic : 1 should match 'static', -1 should match with instance field, 0 don't care.
** matchPrivate : 1 should match 'private', -1 should match 'public', 0 don't care.
**
**      Returns : the field structure if found, NULL when not found.
*/

w_field searchClazzHierarchyForField(w_clazz clazz, w_string name, w_string desc_string, w_int matchStatic, w_int matchPrivate) {

  w_clazz current = clazz;
  w_field field;
 
  while (current) {
    woempa(1, "clazz %k, field %w.\n", current, name);
    field = searchClazzAndInterfacesForField(current, name, desc_string, matchStatic, matchPrivate);
    if (field) {
      return field;
    }
    current = getSuper(current);
  }

  return NULL;
                                  
}                                 

/*
** locate the offset of a field given its name as a UTF-8 string
*/
 
w_size findFieldOffset(w_clazz clazz, const char * utf8name) {

  w_size   slot = 0;
  w_string name = utf2String(utf8name, strlen(utf8name));
  w_field  field;

  if (!name) {
    wabort(ABORT_WONKA, "Unable to create string from name\n");
  }

  field = getField(clazz, name);
  if (field) {
    slot = field->size_and_slot;
  }
  else {
    wabort(ABORT_WONKA, "Field %w not found in class %k.\n", name, clazz);
  }

  return slot;
 
}
 
/*
** To set a reference field of any object, always use this function, which 
** acts as a ``write barrier''. Note that `slot' is a negative value relative
** to the end of the instance.
*/
void setReferenceField(w_instance parent, w_instance child, w_int slot) {
  w_thread  thread = currentWonkaThread;
  w_boolean unsafe = (parent[slot] && thread) ? enterUnsafeRegion(thread) : TRUE;
  w_int offset = instance2clazz(parent)->instanceSize + slot;

  parent[offset] = (w_word)(child);
  if (child) {
    setFlag(instance2flags(child), O_BLACK);
  }

  if (!unsafe) {
    enterSafeRegion(thread);
  }
}

/*
** Use this variant if you know that the calling thread is already marked unsafe
** (e.g. from within J-spot or the interpreter).  In case of doubt use setReferenceField above!
*/
void setReferenceField_unsafe(w_instance parent, w_instance child, w_int slot) {
  w_int offset = instance2clazz(parent)->instanceSize + slot;

  parent[offset] = (w_word)(child);
  if (child) {
    setFlag(instance2flags(child), O_BLACK);
  }
}

w_long getLongField(w_instance parent, w_int slot) {
  w_long value;

  value = (wordFieldPointer(parent, slot))[WORD_MSW];
  value <<= 32;
  value |= (wordFieldPointer(parent, slot))[WORD_LSW];

  return value;
}

#if defined (DEBUG)

void fieldTableDumplet(w_clazz clazz) {

  w_size i;
  w_field field;
  w_field previous = NULL;

  if (clazz->supers) {
    woempa(9,"from superclass \"%k\":\n", clazz->supers[0]);
    fieldTableDumplet(clazz->supers[0]);
  }

  woempa(9,"from class \"%k\":\n", clazz);

  for (i = 0; i < clazz->numFields; i++) {
    field = &clazz->own_fields[i]; 
    woempa(9, "-> %-8s slot %08x (%d w) %w\n", isSet(field->flags, ACC_STATIC) ? "Static" : "Instance", field->size_and_slot, fieldSize(previous), NM(field));
    previous = field;
  }
}

void fieldTableDump(w_clazz clazz) {

  woempa(9, "=== Field Table Dump for %k ===\n", clazz);
  woempa(9, "  %d own fields, instance size is %d words.\n\n", clazz->numFields, clazz->instanceSize);

  fieldTableDumplet(clazz);

  woempa(9, "=== End Field Table Dump for %k ===\n", clazz);

}

#endif /* DEBUG */

