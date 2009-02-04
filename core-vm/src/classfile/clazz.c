/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 by Chris Gray,   *
* /k/ Embedded Java Solutions.  All rights reserved.                      *
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
#include "arrays.h"
#include "clazz.h"
#include "constant.h"
#include "dispatcher.h"
#include "exception.h"
#include "interpreter.h"
#include "loading.h"
#include "locks.h"
#include "oswald.h"
#include "hashtable.h"
#include "heap.h"
#include "ts-mem.h"
#include "methods.h"
#include "wstrings.h"
#include "threads.h"
#include "list.h"
#include "wonka.h"
#include "bar.h"

extern w_object object_sentinel;

/*
 * If NO_FORMAT_CHECKS is defined, no code to check class file format will
 * be generated (leading to a ~4K smaller binary, but less security).
 */
//#define NO_FORMAT_CHECKS

/*
** If DEBUG, JDWP, or METHOD_DEBUG is defined line number and local variable
** tables will always be built. Otherwise it depends on mika.debug.foo.
*/

#if defined(DEBUG) || defined(JDWP) || defined(METHOD_DEBUG) || defined(STORE_METHOD_DEBUG_INFO)
w_boolean use_method_debug_info = TRUE;
#else
w_boolean use_method_debug_info = FALSE;
#endif

/*
** Each classloader has associated with it a hashtable of loaded classes
** and one of unloaded classes, plus a hashtable of packages it has loaded.
** For the "primordial" class loader these are static items.
*/

w_hashtable system_loaded_class_hashtable;
w_hashtable system_unloaded_class_hashtable;
w_hashtable system_package_hashtable;

/*
** The clazz we clone arrays from. See also comments in wonka.h
*/

w_clazz clazz_Array;


w_clazz clazz_boolean;
w_clazz clazz_char;
w_clazz clazz_float;
w_clazz clazz_double;
w_clazz clazz_byte;
w_clazz clazz_short;
w_clazz clazz_int;
w_clazz clazz_long;
w_clazz clazz_void;

/*
** Three often used clazzes.
*/

w_clazz clazzArrayOf_Object;
w_clazz clazzArrayOf_Class;
w_clazz clazzArrayOf_String;

static void parseMethodCodeAttributes(w_method, w_bar);

void deallocClazz(w_clazz clazz);

/*
** Function used by x_snprintf to print out the name of a clazz (format %k).
*/

char *print_clazz_short(char *buffer, int *remain, void *c, w_int width, w_int precision, w_flags flags) {

  w_clazz clazz = c;
  w_int    nbytes;
  char    *temp;

  if (*remain < 1) {

    return buffer;

  }

  temp = buffer;

  if (c == NULL) {
    strncpy(temp, (char*)"<NULL>", *remain);
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

  nbytes = x_snprintf(temp, *remain, "%w", clazz->dotified);
  *remain -= nbytes;

  return temp + nbytes;
}

static const char * clazzState2name[] = {
  "unloaded",
  "loading",
  "loaded",
  "supers loading",
  "supers loaded",
  "referencing",
  "referenced",
  "preparing",
  "prepared",
  "9",
  "10",
  "initializing",
  "initialized",
  "13",
  "14",
  "broken"
};

/*
** Function used by x_snprintf to print out the name of a clazz (format %K).
*/

char *print_clazz_long(char *buffer, int *remain, void *c, int w, int p, unsigned int f) {

  w_clazz clazz = c;
  w_int clazz_state;
  w_int    nbytes;
  char    *temp;

  if (*remain < 1) {

    return buffer;

  }

  temp = buffer;

  if (c == NULL) {
    strncpy(temp, (char*)"<NULL>", *remain);
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

  clazz_state = getClazzState(clazz);
  if (clazz_state == CLAZZ_STATE_INITIALIZED) {
    nbytes = x_snprintf(temp, *remain, "class ");
  }
  else {
    nbytes = x_snprintf(temp, *remain, "%s %s ", clazzState2name[getClazzState(clazz)], isSet(clazz->flags, ACC_INTERFACE) ? "interface" : "class");
  }
  temp += nbytes;
  *remain -= nbytes;

  if (clazz->loader  && clazz->loader != systemClassLoader) {
    nbytes = x_snprintf(temp, *remain, "{%j}", clazz->loader);
    temp += nbytes;
    *remain -= nbytes;
  }

  nbytes = x_snprintf(temp, *remain, "%w", clazz->dotified);
  *remain -= nbytes;

  return temp + nbytes;
}

static void barread(w_bar bar, w_ubyte *dst, w_int length) {

  w_int duffs = (length + 31) / 32;
  w_ubyte * src = bar->buffer + bar->current;

  switch (length & 0x1f) {
    case  0: do { *dst++ = *src++;
    case 31:      *dst++ = *src++;
    case 30:      *dst++ = *src++;
    case 29:      *dst++ = *src++;
    case 28:      *dst++ = *src++;
    case 27:      *dst++ = *src++;
    case 26:      *dst++ = *src++;
    case 25:      *dst++ = *src++;
    case 24:      *dst++ = *src++;
    case 23:      *dst++ = *src++;
    case 22:      *dst++ = *src++;
    case 21:      *dst++ = *src++;
    case 20:      *dst++ = *src++;
    case 19:      *dst++ = *src++;
    case 18:      *dst++ = *src++;
    case 17:      *dst++ = *src++;
    case 16:      *dst++ = *src++;
    case 15:      *dst++ = *src++;
    case 14:      *dst++ = *src++;
    case 13:      *dst++ = *src++;
    case 12:      *dst++ = *src++;
    case 11:      *dst++ = *src++;
    case 10:      *dst++ = *src++;
    case  9:      *dst++ = *src++;
    case  8:      *dst++ = *src++;
    case  7:      *dst++ = *src++;
    case  6:      *dst++ = *src++;
    case  5:      *dst++ = *src++;
    case  4:      *dst++ = *src++;
    case  3:      *dst++ = *src++;
    case  2:      *dst++ = *src++;
    case  1:      *dst++ = *src++;
            } while (--duffs > 0);
  }

  bar->current += length;

}

inline static u1 get_u1(w_bar bar) {

  return *(bar->buffer + bar->current++);

}

inline static u2 get_u2(w_bar s) {

  w_ubyte b0, b1;

  b0 = *(s->buffer + s->current++);
  b1 = *(s->buffer + s->current++);

  return ((b0 << 8) | b1);

}

inline static u4 get_u4(w_bar s) {

  w_ubyte b0, b1, b2, b3;

  b0 = *(s->buffer + s->current++);
  b1 = *(s->buffer + s->current++);
  b2 = *(s->buffer + s->current++);
  b3 = *(s->buffer + s->current++);

  return ((b0 << 24) | (b1 << 16) | (b2 << 8) | b3);

}

static void parseMember(w_clazz clazz, w_bar s, w_int idx)
{
  w_int class_index = get_u2(s);
  w_int name_and_type_index = get_u2(s);
  w_word member = Member_pack(class_index, name_and_type_index);

  clazz->values[idx] = member;
}

static void parseConstant(w_clazz clazz, w_bar s, w_size *idx) {
  u1    tag;
  w_int length;

  tag = get_u1(s);
  clazz->tags[*idx] = tag;

  switch (tag) {
    case CONSTANT_UTF8: 
      length = get_u2(s);

      if (length > 0) {
        void *buffer = allocMem(length);
        if (!buffer) {
          wabort(ABORT_WONKA, "No space for buffer\n");
        }
        barread(s, (w_ubyte*)buffer, length);
        clazz->values[*idx] = (w_ConstantValue)utf2String((char*)buffer, length);
        releaseMem(buffer);
      }
      else {
        clazz->values[*idx] = (w_ConstantValue)utf2String("", 0);
        woempa(1, "Utf8 constant with length %d!\n", length);
      }
      woempa(1, "Constant[%d] = UTF8 `%w'\n", *idx, clazz->values[*idx]);
      if (!clazz->values[*idx]) {
        wabort(ABORT_WONKA, "Unable to allocate space for UTF8 constant\n");
      }
      
      *idx += 1;
      break;
      
    case CONSTANT_INTEGER:
    case CONSTANT_FLOAT:
      clazz->values[*idx] = get_u4(s);
      woempa(1, "Constant[%d] = %s 0x%08x\n", *idx, tag == CONSTANT_INTEGER ? "int" : "float", clazz->values[*idx]);
      *idx += 1;
      break;
      
    case CONSTANT_LONG:
    case CONSTANT_DOUBLE:
      {
        w_long val = get_u4(s);

        val = (val << 32) | get_u4(s);
	memcpy((w_word*)clazz->values + *idx, &val, 8);
        clazz->tags[*idx + 1] = NO_VALID_ENTRY;
        woempa(1, "Constant[%d] = %s 0x%08x%08x\n", *idx, tag == CONSTANT_LONG ? "long" : "double", clazz->values[*idx], clazz->values[*idx + 1]);
        *idx += 2;
      }
      break;
      
    case CONSTANT_CLASS:
      clazz->values[*idx] = get_u2(s);
      woempa(1, "Constant[%d] = Class[%d]\n", *idx, clazz->values[*idx]);
#ifdef DEBUG
      if (clazz->values[*idx] < *idx) {
        woempa(1, "             = %w\n", clazz->values[clazz->values[*idx]]);
      }
#endif
      *idx += 1;
      break;
      
    case CONSTANT_STRING:
      clazz->values[*idx] = get_u2(s);
      woempa(1, "Constant[%d] = String[%d]\n", *idx, clazz->values[*idx]);
#ifdef DEBUG
      if (clazz->values[*idx] < *idx) {
        woempa(1, "             = %w\n", clazz->values[clazz->values[*idx]]);
      }
#endif
      *idx += 1;
      break;
      
    case CONSTANT_FIELD:
    case CONSTANT_METHOD:
    case CONSTANT_IMETHOD:
      parseMember(clazz, s, *idx);
      *idx += 1;
      break;
      
    case CONSTANT_NAME_AND_TYPE:
      {
        u2 name_index = get_u2(s);
        u2 descriptor_index = get_u2(s);
	w_word nat = Name_and_Type_pack(name_index, descriptor_index);
        clazz->values[*idx] = *(w_ConstantValue*)&nat;
        woempa(1, "Constant[%d] = NAT with Name[%d], Type[%d] => '0x%08x'\n", *idx, name_index, descriptor_index, nat);
#ifdef DEBUG
        if (descriptor_index < *idx) {
          woempa(1, "        type = %w\n", clazz->values[descriptor_index]);
        }
        if (name_index < *idx) {
          woempa(1, "        name = %w\n", clazz->values[name_index]);
        }
#endif
        *idx += 1;
      }
      break;
  }
}

#ifndef NO_FORMAT_CHECKS
/**
 ** Check cafebabe and spec version.
 ** 'bar' should be set to the start of the classfile, and will be rewound
 ** to there on exit.
 ** Returns TRUE for success, FALSE for failure.
 */ 
static w_boolean pre_check_header(w_clazz clazz, w_bar bar) {
  u4 cafebabe;

  if (bar_avail(bar) < 24) {
    woempa(9, "Class file too short, only %d bytes\n", bar_avail(bar));

    return FALSE;
  }

  cafebabe = get_u4(bar);

  if (cafebabe != 0xcafebabe) {
    woempa(9, "Uh oh, cafebabe is called `%08x' today\n", cafebabe);

    return FALSE;
  }

  clazz->cminor = get_u2(bar);
  clazz->cmajor = get_u2(bar);
  // TODO: check these ...

  bar_seek(bar, 0);

  return TRUE;
}


/**
 ** Check file is big enough to contain constant pool.
 ** 'bar' should be set to byte 8 of the classfile, and will be rewound
 ** to there on exit.
 ** Returns TRUE if all checks pass. If any check fails, throws a 
 ** ClassFormatError and returns FALSE.
 */ 
static w_boolean pre_check_constant_pool(w_clazz clazz, w_bar bar, w_thread thread) {
  w_size n;
  u1 tag;
  u2 val;
  w_int length;
  w_size i;
  char *tags;
  w_boolean ok = TRUE;

  n = get_u2(bar);
  tags = allocMem(n);

  // We do this in two passes, because in theory forward references are possible.
  for (i = 1; ok && i < n; ) {
    if (bar_avail(bar) < 3) {
      throwException(thread, clazzClassFormatError, "Less than 3 bytes remaining at start of constant[%d]", i);
      ok = FALSE;
    }
    tag = get_u1(bar);
    tags[i] = tag;
    switch (tag) {
      case CONSTANT_UTF8: 
        length = get_u2(bar);
        if (length) {
          if (bar_avail(bar) < length) {
            throwException(thread, clazzClassFormatError, "Insufficient bytes remaining for UTF8 constant[%d]", i);
            ok = FALSE;
          }
          bar_skip(bar, length);
        }
        ++i;
        break;

      case CONSTANT_CLASS:
      case CONSTANT_STRING:
        if (bar_avail(bar) < 2) {
          throwException(thread, clazzClassFormatError, "Insufficient bytes remaining for CLASS/STRING constant[%d]", i);
          ok = FALSE;
        }
        bar_skip(bar, 2);
        ++i;
        break;

      case CONSTANT_INTEGER:
      case CONSTANT_FLOAT:
      case CONSTANT_FIELD:
      case CONSTANT_METHOD:
      case CONSTANT_IMETHOD:
      case CONSTANT_NAME_AND_TYPE:
        if (bar_avail(bar) < 4) {
          throwException(thread, clazzClassFormatError, "Insufficient bytes remaining for constant[%d]", i);
          ok = FALSE;
        }
        bar_skip(bar, 4);
        ++i;
        break;

      case CONSTANT_LONG:
      case CONSTANT_DOUBLE:
        if (bar_avail(bar) < 8) {
          throwException(thread, clazzClassFormatError, "Insufficient bytes remaining for UTF8 constant[%d]", i);
          ok = FALSE;
        }
        bar_skip(bar, 8);
        i += 2;
        break;

      default:
        throwException(thread, clazzClassFormatError, "Illegal constant type tag %d", tag);
        ok = FALSE;
    }
  }

  bar_seek(bar, 10);

  for (i = 1; ok && i < n; ) {
    tag = get_u1(bar);
    switch (tag) {
      case CONSTANT_UTF8: 
        length = get_u2(bar);
        if (length) {
          bar_skip(bar, length);
        }
        ++i;
        break;

      case CONSTANT_CLASS:
      case CONSTANT_STRING:
        val = get_u2(bar);
        if (val == 0 || val >= n || tags[val] != CONSTANT_UTF8) {
          throwException(thread, clazzClassFormatError, "Class constant[%d] references non-utf8 constant[%d]", i, val);
          ok = FALSE;
        }
        ++i;
        break;

      case CONSTANT_INTEGER:
      case CONSTANT_FLOAT:
        bar_skip(bar, 4);
        ++i;
        break;

      case CONSTANT_FIELD:
      case CONSTANT_METHOD:
      case CONSTANT_IMETHOD:
        val = get_u2(bar);
        if (val == 0 || val >= n || tags[val] != CONSTANT_CLASS) {
          throwException(thread, clazzClassFormatError, "Member constant[%d] references non-class constant[%d]", i, val);
          ok = FALSE;
        }
        val = get_u2(bar);
        if (val == 0 || val >= n || tags[val] != CONSTANT_NAME_AND_TYPE) {
          throwException(thread, clazzClassFormatError, "Member constant[%d] references non-name & type constant[%d]", i, val);
          ok = FALSE;
        }
        ++i;
        break;

      case CONSTANT_NAME_AND_TYPE:
        val = get_u2(bar);
        if (val == 0 || val >= n || tags[val] != CONSTANT_UTF8) {
          throwException(thread, clazzClassFormatError, "Name & type constant[%d] references non-utf8 constant[%d]", i, val);
          ok = FALSE;
        }
        val = get_u2(bar);
        if (val == 0 || val >= n || tags[val] != CONSTANT_UTF8) {
          throwException(thread, clazzClassFormatError, "Name & type constant[%d] references non-utf8 constant[%d]", i, val);
          ok = FALSE;
        }
        ++i;
        break;

      case CONSTANT_LONG:
      case CONSTANT_DOUBLE:
        bar_skip(bar, 8);
        i += 2;
        break;
    }
  }

  releaseMem(tags);

  bar_seek(bar, 8);

  return ok;
}

/**
 ** Check a set of attributes beginning with the attribute count at the 
 ** current position of 'bar'. Afterwards 'bar' points past the last attribute.
 ** Returns TRUE if all checks pass. If any check fails, throws a 
 ** ClassFormatError and returns FALSE.
 */
static w_boolean pre_check_attributes(w_clazz clazz, w_bar bar, char *type, w_thread thread) {
  w_size n;
  w_size i;
  w_word val;
  w_int length;

  if (bar_avail(bar) < 2) {
    throwException(thread, clazzClassFormatError, "Class file too short for %s attribute count", type);

    return FALSE;
  }

  n = get_u2(bar);
  woempa(1, "%d attributes\n", n);

  for (i = 0; i < n; ++i) {
    if (bar_avail(bar) < 6) {
      throwException(thread, clazzClassFormatError, "Less than 6 bytes remaining at start of %s attribute[%d]", type, i);

      return FALSE;
    }

    val = get_u2(bar);
    if (clazz->tags[val] != CONSTANT_UTF8) {
      throwException(thread, clazzClassFormatError, "%s attribute[%d] references non-utf8 constant[%d]", type, i, val);

      return FALSE;
    }
    length = get_u4(bar);
    woempa(1, "Attribute[%d] length = %d\n", i, length);
    if (bar_avail(bar) < length) {
      throwException(thread, clazzClassFormatError, "Less than <attribute length> bytes remaining");

      return FALSE;
    }
    bar_skip(bar, length);
  }

  return TRUE;
}

/**
 ** Check file is big enough to contain fields, methods, attributes.
 ** 'bar' should be set to byte following constant pool, and will be rewound
 ** to there on exit.
 ** Returns TRUE if all checks pass. If any check fails, throws a 
 ** ClassFormatError and returns FALSE.
 */ 
static w_boolean pre_check_remainder(w_clazz clazz, w_bar bar, w_thread thread) {
  w_int offset = bar->current;
  w_size n;
  w_word val;
  w_size i;
  w_int length;
  w_boolean result;

  if (bar_avail(bar) < 8) {
    throwException(thread, clazzClassFormatError, "Class file too short for class flags / this class / super class / num interfaces");

    return FALSE;
  }

  bar_skip(bar, 6);
  n = get_u2(bar);

  if (bar_avail(bar) < (signed)(n * 2 + 2)) {
    throwException(thread, clazzClassFormatError, "Class file too short for interfaces / field count");

    return FALSE;
  }

  for (i = 0; i < n; ++i) {
    val = get_u2(bar);
    if (clazz->tags[val] != CONSTANT_CLASS) {
      throwException(thread, clazzClassFormatError, "Interface[%d] references non-class constant[%d]", i, val);

      return FALSE;
    }
  }

  n = get_u2(bar);
  woempa(1, "%d fields\n", n);

  for (i = 0; i < n; ++i) {
    if (bar_avail(bar) < 8) {
      throwException(thread, clazzClassFormatError, "Less than 8 bytes remaining at start of field");

      return FALSE;
    }

    bar_skip(bar, 2);
    val = get_u2(bar);
    if (clazz->tags[val] != CONSTANT_UTF8) {
      throwException(thread, clazzClassFormatError, "Field[%d] references non-utf8 constant[%d]", i, val);

      return FALSE;
    }
    val = get_u2(bar);
    if (clazz->tags[val] != CONSTANT_UTF8) {
      throwException(thread, clazzClassFormatError, "Field[%d] references non-utf8 constant[%d]", i, val);

      return FALSE;
    }
    if (!pre_check_attributes(clazz, bar, "field", thread)) {

      return FALSE;
    }
  }

  if (bar_avail(bar) < 2) {
    throwException(thread, clazzClassFormatError, "Class file too short for method count");

    return FALSE;
  }

  n = get_u2(bar);
  woempa(1, "%d methods\n", n);

  for (i = 0; i < n; ++i) {
    if (bar_avail(bar) < 8) {
      throwException(thread, clazzClassFormatError, "Less than 8 bytes remaining at start of method");

      return FALSE;
    }

    bar_skip(bar, 2); // flags
    val = get_u2(bar);
    if (clazz->tags[val] != CONSTANT_UTF8) {
      throwException(thread, clazzClassFormatError, "Method[%d] references non-utf8 constant[%d]", i, val);

      return FALSE;
    }
    val = get_u2(bar);
    if (clazz->tags[val] != CONSTANT_UTF8) {
      throwException(thread, clazzClassFormatError, "Method[%d] references non-utf8 constant[%d]", i, val);

      return FALSE;
    }
    if (!pre_check_attributes(clazz, bar, "method", thread)) {

      return FALSE;
    }
  }

  result =  pre_check_attributes(clazz, bar, "class", thread);

  length = bar_avail(bar);
  if (length) {
    throwException(thread, clazzClassFormatError, "%d bytes left at end of class file", bar_avail(bar));

    result = FALSE;
  }

  bar_seek(bar, offset);

  return result;
}

/*
** Check that the constant indexed by clazz->temp.this_index is a valid class
** constant and that its name matches 'name' (if the latter is non-NULL).
 ** Returns TRUE if all checks pass. If any check fails, throws a 
 ** ClassFormatError and returns FALSE.
*/
inline static w_boolean check_classname(w_clazz clazz, w_string name, w_thread thread) {
  w_int classConstantIndex;
  w_int classNameIndex;
  w_string slashed;
  w_string dotified;

  classConstantIndex = clazz->temp.this_index;
  if (clazz->tags[classConstantIndex] != CONSTANT_CLASS) {
    throwException(thread, clazzClassFormatError, "Own class index is not a CONSTANT_CLASS");

    return FALSE;
  }
  classNameIndex = clazz->values[classConstantIndex];
  if (clazz->tags[classNameIndex] != CONSTANT_UTF8) {
    throwException(thread, clazzClassFormatError, "Own class name is not a CONSTANT_UTF8");

    return FALSE;
  }
  slashed = (w_string)clazz->values[classNameIndex];
  woempa(7, "Slashed class name: %w\n", slashed);
  dotified = slashes2dots(slashed);
  if (!dotified) {
    wabort(ABORT_WONKA, "Unable to dotify name\n");
  }
  if (name && dotified != name) {
    throwException(thread, clazzClassFormatError, "Impostor! The class which should be known as `%w' is really `%w'.",name,dotified);

    return FALSE;
  }

  return TRUE;
}

static w_boolean check_field(w_clazz clazz, w_field f) {
  w_word flags = f->flags;

  if (isSet(clazz->flags, ACC_INTERFACE)) {
    // Interface fields must be public static final and nowt else.
    if ((flags & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC | ACC_FINAL | ACC_VOLATILE | ACC_TRANSIENT)) != (ACC_PUBLIC | ACC_STATIC | ACC_FINAL)) {
      return FALSE;
    }
  }
  else {
    // Class fields may have at most one of the following flags set.
    switch(flags & (ACC_PRIVATE | ACC_PROTECTED | ACC_PUBLIC)) {
    case 0:
    case ACC_PRIVATE:
    case ACC_PROTECTED:
    case ACC_PUBLIC:
      break;

    default:
      return FALSE;
    }

    // Similarly, at most one of ACC_VOLATILE/ACC_FINAL may be set
    if ((flags & (ACC_FINAL | ACC_VOLATILE)) == (ACC_FINAL | ACC_VOLATILE)) {
      return FALSE;
    }
  }

  // Spec says to silently ignore ConstantValue if field not static
  if (f->initval && isSet(flags, ACC_STATIC)) {
    w_ushort initval = f->initval;
    w_int inittag = clazz->tags[initval];
    if ((initval >= clazz->numConstants)) {
      return FALSE;
    }
    if (string_is_latin1(f->desc)) {
      if (string_length(f->desc) == 1) {
        switch(f->desc->contents.bytes[0]) {
        case 'B':
        case 'C':
        case 'I':
        case 'S':
        case 'Z':
          if (inittag != CONSTANT_INTEGER) {
            return FALSE;
          }
          break;

        case 'D':
          if (inittag != CONSTANT_DOUBLE) {
            return FALSE;
          }
          break;

        case 'F':
          if (inittag != CONSTANT_FLOAT) {
            return FALSE;
          }
          break;

          case 'J':
            if (inittag != CONSTANT_LONG) {
              return FALSE;
            }
            break;

          default:
            return FALSE;
          }
        }
        else if (f->desc == string_L_java_lang_String) {
        if ((inittag != CONSTANT_STRING) && (inittag != RESOLVED_STRING)) {
          return FALSE;
        }
      }
      else {
        return FALSE;
      }
    }
  }

  return TRUE;
}

static w_boolean check_method(w_clazz clazz, w_method m) {
  w_word flags = m->flags;

  if (isSet(clazz->flags, ACC_INTERFACE)) {
    // Interface methods must be public static final and nowt else.
    if (m->spec.name != string_angle_brackets_clinit && (flags & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC | ACC_FINAL | ACC_SYNCHRONIZED | ACC_NATIVE | ACC_ABSTRACT | ACC_STRICT)) != (ACC_PUBLIC | ACC_ABSTRACT)) {
      return FALSE;
    }
  }
  else {
    // Class methods may have at most one of the following flags set.
    switch(flags & (ACC_PRIVATE | ACC_PROTECTED | ACC_PUBLIC)) {
    case 0:
    case ACC_PRIVATE:
    case ACC_PROTECTED:
    case ACC_PUBLIC:
      break;

    default:
      return FALSE;
    }

    // If ACC_ABSTRACT is set then a whole bunch of flags are disallowed.
    if (isSet(flags, ACC_ABSTRACT) && isSet(flags, ACC_FINAL | ACC_NATIVE | ACC_PRIVATE | ACC_STATIC | ACC_SYNCHRONIZED | ACC_STRICT)) {
      return FALSE;
    }
  }

  // Code must be present iff neither ACC_ABSTRACT or ACC_NATIVE is set.
  if (isSet(flags, ACC_ABSTRACT | ACC_NATIVE) == (m->exec.code || m->throws || m->exec.debug_info)) {
    return FALSE;
  }

  // TODO: <clinit> must be static, <init> not. Both have return type void.
  // And <init> should not have ACC_FINAL set.

  return TRUE;
}

/** Final checks on class file format.
 ** Returns TRUE if all checks pass. If any check fails, throws a 
 ** ClassFormatError and returns FALSE.
*/
static w_boolean post_checks(w_clazz clazz, w_string name, w_thread thread) {
  w_size i;
  w_boolean result = TRUE;

  for (i = 0; result && i < clazz->numFields; ++i) {
    result = check_field(clazz, &clazz->own_fields[i]);
  }
  if (!result) {
    throwException(thread, clazzClassFormatError, "bad flags in field[%d]", i);

    return FALSE;
  }
  for (i = 0; result && i < clazz->numDeclaredMethods; ++i) {
    result = check_method(clazz, &clazz->own_methods[i]);
  }
  if (!result) {
    throwException(thread, clazzClassFormatError, "bad flags in method[%d]", i);

    return FALSE;
  }

  return check_classname(clazz, name, thread);
}
#endif

/*
** Extract information from the classfile header.
*/
inline static void get_header(w_clazz clazz, w_bar bar) {
  bar_skip(bar, 4);
  clazz->cminor = get_u2(bar);
  clazz->cmajor = get_u2(bar);
}

/*
** Scan through the constant pool and copy the data to clazz->tags[].
*/

inline static void get_constantpool(w_clazz clazz, w_bar bar) {
  w_size i;

  clazz->numConstants = get_u2(bar);

  woempa(1, "Number of constant pool entries is %d, using %d bytes each\n", clazz->numConstants - 1, sizeof(w_ConstantType));
  clazz->tags = allocMem((clazz->numConstants + 1) * sizeof(w_ConstantType));
  if (!clazz->tags) {
    wabort(ABORT_WONKA, "No space for clazz->tags\n");
  }
  clazz->values = allocMem((clazz->numConstants + 1) * sizeof(w_word));
  if (!clazz->values) {
    wabort(ABORT_WONKA, "No space for clazz->values\n");
  }

  for (i = 1; i < clazz->numConstants; ) {
    woempa(1, "parsing constant pool entry %d of %d\n", i, clazz->numConstants - 1);
    parseConstant(clazz, bar, &i);
  }
}

/*
** Parse a field attribute (a name/value pair).
*/

static void parseFieldAttribute(w_field field, w_bar s) {

  u2 attribute_name_index = 0;
  u4 attribute_length = 0;
  w_string attributeName;

  attribute_name_index = get_u2(s);
  attribute_length = get_u4(s);

  attributeName = resolveUtf8Constant(field->declaring_clazz, attribute_name_index);
  woempa(1, "Parsing attribute %w for field %w of %k.\n", attributeName, NM(field), field->declaring_clazz);

  /*
  ** See which attribute we have here.
  */

  if (attributeName == string_ConstantValue) {
    field->initval = get_u2(s);
  }
  else {
    /*
    ** Unknown attributes are silently ignored.
    */
    woempa(1, "Unknown/ignored attribute %w\n", attributeName);
    bar_skip(s, attribute_length);
  }
}

/*
** Parse a field entry in the classfile.  The entry consists of a
** flags set (u2), name index (u2), descriptor index (u2), number of
** attributes (u2), followed by the attributes (see previous function).
**
** We store the field entries sorted as follows:
**   first, all static fields;
**   then all instance fields which store references;
**   lastly, instance fields which store primitives.
** This ordering will simplify later processing (class preparation, etc.).
*/

static void parseField(w_clazz clazz, w_size idx, w_bar s) {

  u2 tmp_u2;
  int attributeNumber;
  w_field  field;
  w_flags  flags;
  w_string name;
  w_string desc;
  w_boolean lng = WONKA_FALSE;
  w_boolean ref = WONKA_FALSE;
  w_boolean arr = WONKA_FALSE;

  flags = get_u2(s);
  tmp_u2 = get_u2(s);
  name = resolveUtf8Constant(clazz, tmp_u2);
  
  tmp_u2 = get_u2(s);
  desc = resolveUtf8Constant(clazz, tmp_u2);
  switch(string_char(desc, 0)) {
    case 'D':
    case 'J':
      woempa(1, "Field %w of %k has descriptor %w, setting FIELD_IS_LONG\n", name, clazz, desc);
      lng = WONKA_TRUE;
      break;

    case 'L':
      woempa(1, "Field %w of %k has descriptor %w, setting FIELD_IS_REFERENCE\n", name, clazz, desc);
      ref = WONKA_TRUE;
      break;

    case '[':
      woempa(1, "Field %w of %k has descriptor %w, setting FIELD_IS_REFERENCE and FIELD_IS_ARRAY\n", name, clazz, desc);
      ref = WONKA_TRUE;
      arr = WONKA_TRUE;
      break;

    default:
      woempa(1, "Field %w of %k has descriptor %w, setting no flags\n", name, clazz, desc);
  }

  deregisterString(desc);

  field = &clazz->own_fields[idx];
  field->desc = desc;
  field->declaring_clazz = clazz;
  field->label = (char *) "field";
  field->flags = flags;
  if (isSet(field->flags, ACC_STATIC)) {
    ++clazz->numStaticFields;
  }
  field->name  = name;
  field->initval = 0;
  if (lng) {
    setFlag(field->flags, FIELD_IS_LONG);
  }
  else if (ref) {
    setFlag(field->flags, FIELD_IS_REFERENCE);
  }
  if (arr) {
    setFlag(field->flags, FIELD_IS_ARRAY);
  }

  /*
  ** Parse its attributes.
  */

  tmp_u2 = get_u2(s);
  if (tmp_u2) {
    for (attributeNumber = 0; attributeNumber < tmp_u2; attributeNumber++) {
      parseFieldAttribute(field, s);
    }
  }
}

static void parseMethodCodeExceptionTable(w_method method, w_bar s) {

  w_exception exception;
  w_int n;
  w_int i;

  n = get_u2(s);
  method->exec.numExceptions = n;
  if (n) {
    method->exec.exceptions = allocMem(n * sizeof(w_Exception));
    if (!method->exec.exceptions) {
      wabort(ABORT_WONKA, "No space for exceptions\n");
    }
    for (i = 0; i < n; i++) {
      exception = &method->exec.exceptions[i];
      exception->start_pc = get_u2(s);
      exception->end_pc = get_u2(s);
      exception->handler_pc = get_u2(s);
      exception->type_index = get_u2(s);
    }
  }
}

static void parseMethodCode(w_method method, w_bar s) {

  unsigned char * code;

  method->exec.stack_i = get_u2(s);
  method->exec.local_i = get_u2(s);
  method->exec.code_length = get_u4(s);
  if (method->exec.code_length > 0) {
    code = allocMem((w_size)method->exec.code_length + 4);
    if (!code) {
      wabort(ABORT_WONKA, "No space for method bytecode\n");
    }
    method->exec.code = &code[4];
    method->exec.code[-1] = 254; // handle exception
    method->exec.code[-2] = 255; // no handler found, return
    barread(s, method->exec.code, (signed)method->exec.code_length);
  }
}

static w_boolean parseLocalVars(w_method method, w_bar s) {
  w_methodDebugInfo debug_info = method->exec.debug_info;
  w_int attribute_count;
  w_int i;

  if (!debug_info) {
    debug_info = allocClearedMem(sizeof(w_MethodDebugInfo));
    method->exec.debug_info = debug_info;
    if (!debug_info) {
      printf("No space for method debug info!n");

      return FALSE;

    }
  }

  attribute_count = get_u2(s);
  debug_info->numLocalVars = attribute_count;
  if (attribute_count > 0) {
    debug_info->localVars = allocMem(attribute_count * sizeof(w_LocalVar));
    if (!debug_info->localVars) {
      wabort(ABORT_WONKA, "No space for local vars\n");
    }
    for (i = 0; i < attribute_count; i++) {
      debug_info->localVars[i].start_pc = get_u2(s);
      debug_info->localVars[i].length = get_u2(s);
      debug_info->localVars[i].name = resolveUtf8Constant(method->spec.declaring_clazz, get_u2(s));
      debug_info->localVars[i].desc = resolveUtf8Constant(method->spec.declaring_clazz, get_u2(s));
      debug_info->localVars[i].slot = get_u2(s);
    }
  }

  return TRUE;
}

static w_boolean parseLineNumbers(w_method method, w_bar s) {
  w_methodDebugInfo debug_info = method->exec.debug_info;
  w_int attribute_count;
  w_int i;

  if (!debug_info) {
    debug_info = allocClearedMem(sizeof(w_MethodDebugInfo));
    method->exec.debug_info = debug_info;
    if (!debug_info) {
      printf("No space for method debug info!n");

      return FALSE;

    }
  }

  attribute_count = get_u2(s);
  debug_info->numLineNums = attribute_count;
  if (attribute_count > 0) {
    debug_info->lineNums = allocMem(attribute_count * sizeof(w_LineNum));
    if (!debug_info->lineNums) {
      wabort(ABORT_WONKA, "No space for method lineNums\n");
    }
    for (i = 0; i < attribute_count; i++) {
      debug_info->lineNums[i].start_pc = get_u2(s);
      debug_info->lineNums[i].line_number = get_u2(s);
    }
  }

  return TRUE;
}

static void parseMethodAttribute(w_method method, w_bar s) {

  u2 attribute_name_index;
  u4 attribute_length;
  w_int i;
  w_string attributeName;
  u2 attribute_count;
  
  attribute_name_index = get_u2(s);
  attribute_length = get_u4(s);
  woempa(1, "Parsing attribute[%d] of %k\n", attribute_name_index, method->spec.declaring_clazz);
  attributeName = resolveUtf8Constant(method->spec.declaring_clazz, attribute_name_index);

  woempa(1, "Attribute `%w' length %d\n", attributeName, attribute_length);

  /*
  ** See which attribute we have here.
  */

  if (attributeName == string_Code) {
    parseMethodCode(method, s);
    parseMethodCodeExceptionTable(method, s);
    parseMethodCodeAttributes(method, s);

    woempa(4, "Read Code attribute; %d bytes, %d exception handlers\n", method->exec.code_length, method->exec.numExceptions);
  }
  else if (attributeName == string_Exceptions) {
    method->numThrows = get_u2(s);
    method->throws = allocMem(method->numThrows * sizeof(w_ushort));
    if (!method->throws) {
      wabort(ABORT_WONKA, "No space for method throws\n");
    }
    for (i = 0; i < method->numThrows; i++) {
      method->throws[i] = get_u2(s);
    }
    woempa(4, "Read Exceptions attribute; throws %d exceptions\n", method->numThrows);
  }
  else if (attributeName == string_Synthetic) {
    setFlag(method->flags, METHOD_IS_SYNTHETIC);
  }
  else if (attributeName == string_LocalVariableTable) {
    if (!use_method_debug_info || !parseLocalVars(method, s)) {
      attribute_count = get_u2(s);
      for (i = 0; i < attribute_count; i++) {
        get_u2(s);
        get_u2(s);
        get_u2(s);
        get_u2(s);
        get_u2(s);
      }
    }
  }
  else if (attributeName == string_LineNumberTable) {

    if (!use_method_debug_info || !parseLineNumbers(method, s)) {
      attribute_count = get_u2(s);
      for (i = 0; i < attribute_count; i++) {
        get_u2(s);
        get_u2(s);
      }
    }
  }
  else {
    woempa(1, "Unknown/ignored attribute '%w'\n", attributeName);
    for (i=0;(u4)i<attribute_length;++i) {
      get_u1(s);
    }
  }

}

/*
** This function can not be made inline. It's called by parseMethodAttribute and this function calls
** this parseMethodAttribute function recursively...
*/

static void parseMethodCodeAttributes(w_method method, w_bar s) {

  w_int i;
  w_int attr_count;

  attr_count = get_u2(s);
  if (attr_count > 0) {
    for (i = 0; i < attr_count; i++) {
      parseMethodAttribute(method, s);
    }
  }

}

static void parseMethod(w_clazz clazz, w_size idx, w_bar s) {
  u2 access_flags;
  u2 numAttributes;
  w_string name;
  w_size i;
  
  access_flags = get_u2(s);
  name = resolveUtf8Constant(clazz,get_u2(s));
  if (name == string_angle_brackets_clinit) {
    access_flags &= ACC_STRICT;
    access_flags |= ACC_STATIC;
  }
  clazz->own_methods[idx].flags = access_flags;
  clazz->own_methods[idx].spec.declaring_clazz = clazz;
  clazz->own_methods[idx].spec.name = name;
  clazz->own_methods[idx].desc = resolveUtf8Constant(clazz, get_u2(s));
  clazz->own_methods[idx].slot = SLOT_NOT_ALLOCATED;
  clazz->own_methods[idx].numThrows = 0;

  clazz->own_methods[idx].exec.code = NULL;
  clazz->own_methods[idx].exec.function.void_fun = NULL;
  clazz->own_methods[idx].exec.numExceptions = 0;
  clazz->own_methods[idx].exec.debug_info = NULL;

  /*
  ** Parse the attributes of this method.
  */

  numAttributes = get_u2(s);
  if (numAttributes > 0) {
    for (i = 0; i < numAttributes; i++) {
      parseMethodAttribute(&clazz->own_methods[idx], s);
    }
  }
}

/*
** Determine whether a name begins with one of the reserved prefixes "java.",
** "wonka.".  TODO: define and use a function in strings.h.
*/
w_boolean isReservedName(w_string name) {
  w_size l = string_length(name);
  w_size n = 0;

  while (n < l && string_char(name, n) == '[') {
    ++n;
    --l;
  }

  if (n) {
    if (string_char(name, n) == 'L') {
      ++n;
      l -= 2;
    }
    else {
      return WONKA_TRUE;
    }
  }

  if (l > 5 &&
      string_char(name, n + 0) == 'j' && string_char(name, n + 1) == 'a' &&
      string_char(name, n + 2) == 'v' && string_char(name, n + 3) == 'a' &&
      string_char(name, n + 4) == '.')
  {
    return WONKA_TRUE;
  }

  if (l > 6 &&
      string_char(name, n + 0) == 'w' && string_char(name, n + 1) == 'o' &&
      string_char(name, n + 2) == 'n' && string_char(name, n + 3) == 'k' &&
      string_char(name, n + 4) == 'a' && string_char(name, n + 5) == '.')
  {
    return WONKA_TRUE;
  }

  return WONKA_FALSE;
}


/*
** Register a w_Clazz structure in a class hashtable.  
** See remarks in clazz.n.
*/

w_clazz registerClazz(w_thread thread, w_clazz clazz, w_instance loader) {
  w_clazz  existing;
  w_clazz result = clazz;
  w_hashtable hashtable = loader2loaded_classes(loader);

  woempa(7, "Registering %K in %s.\n", clazz, hashtable->label);

  threadMustBeSafe(thread);

  ht_lock(hashtable);
  existing = (w_clazz)ht_write_no_lock(hashtable, (w_word)clazz->dotified, (w_word)clazz);

  if (existing) {
    if (existing == clazz) {
      woempa(7, "Class %k (%p) was already present in %s (which is OK).\n", clazz, clazz, hashtable->label);
    }
    else {
#ifdef RUNTIME_CHECKS
      if (getClazzState(existing) == CLAZZ_STATE_UNLOADED) {
        wabort(ABORT_WONKA, "Woah now, what is an unloaded class doing in the loaded_classes hashtable?");
      }
      else
#endif
      if (getClazzState(existing) == CLAZZ_STATE_LOADING) {
        // Existing entry is a placeholder, if 'clazz' is a loaded class then
        // we copy the contents into 'existing' and adjust a few pointers.
        if (getClazzState(clazz) >= CLAZZ_STATE_LOADED) {
          x_monitor save_monitor;
          w_instance Class = clazz2Class(clazz);
          w_int i;

          woempa(7, "Placeholder %K (%p) found for class %K (%p) in %s, copying %d bytes from %p to %p.\n", existing, existing, clazz, clazz, hashtable->label, sizeof(w_Clazz), clazz, existing);
          
          // Avoid overwriting resolution_monitor of 'existing'. 
          // This is very ugly, sorry about that.
          save_monitor = clazz->resolution_monitor;
          clazz->resolution_monitor = existing->resolution_monitor;
          w_memcpy(existing, clazz, sizeof(w_Clazz));
          clazz->resolution_monitor = save_monitor;

          for (i = 0; i < clazz->numFields; ++i) {
            existing->own_fields[i].declaring_clazz = existing;
          }
          for (i = 0; i < clazz->numDeclaredMethods; ++i) {
            existing->own_methods[i].spec.declaring_clazz = existing;
          }
          if (!clazz->dims) {
            setReferenceField_unsafe(Class, existing->loader, F_Class_loader);
          }
          setWotsitField(Class, F_Class_wotsit, existing);
        }
        // otherwise 'clazz' is also a placeholder, we just throw it away.
      }
      else {
        // Existing entry is for real, Houston we have a problem.
        if (thread) {
          throwException(thread, clazzSecurityException, "Class already defined: %w", clazz->dotified);
        }
      }
      // Put 'existing' back into the hashtable and trash 'clazz'.
      ht_write_no_lock(hashtable, (w_word)clazz->dotified, (w_word)existing);
      deallocClazz(clazz);
      result = existing;
    }
  }
  ht_unlock(hashtable);
  woempa(7, "Returning %K (%p)\n", result);
  if (loader) {
    notifyMonitor(loader, TRUE);
  }

  return result;
}

/*
** Deregister a w_Clazz structure from a class hashtable.  
*/

void deregisterClazz(w_clazz clazz, w_instance loader) {
  w_clazz  existing;
  w_hashtable hashtable = loader2loaded_classes(loader);

  woempa(8, "Deregistering clazz %k from %s.\n", clazz, hashtable->label);

  existing = (w_clazz)ht_erase(hashtable, (w_word)clazz->dotified);

  if (!existing) {
    woempa(9, "Fascinating. When we wanted to deregister class %k, we couldn't find it in %s\n", clazz, hashtable->label);
  }

}

inline static void set_classname(w_clazz clazz) {
  w_string slashed = (w_string)clazz->values[clazz->values[clazz->temp.this_index]];

  clazz->dotified = slashes2dots(slashed);
}

inline static void get_interfaces(w_clazz clazz, w_bar s) {

  w_size i;

  clazz->temp.interface_index_count = get_u2(s);
  woempa(1, "%K has %d interfaces\n", clazz, clazz->temp.interface_index_count);
  if (clazz->temp.interface_index_count > 0) {
    clazz->temp.interface_index = allocClearedMem(clazz->temp.interface_index_count * sizeof(w_ushort));
    if (!clazz->temp.interface_index) {
      wabort(ABORT_WONKA, "Unable to allocate clazz->temp.interface_index\n");
    }
    for (i = 0; i < clazz->temp.interface_index_count; i++) {
      clazz->temp.interface_index[i] = get_u2(s);
      woempa(1, "%K has interface %d index %d\n", clazz, i, clazz->temp.interface_index[i]);
    }
  }
}

static void swapFields(w_clazz clazz, w_int i, w_int j) {
  w_Field f;

  woempa(1, "Swapping fields [%d] (`%w') and [%d] (`%w') of %k\n", i, clazz->own_fields[i].name, j, clazz->own_fields[j].name, clazz);
  w_memcpy(&f, &clazz->own_fields[i], sizeof(w_Field));
  w_memcpy(&clazz->own_fields[i], &clazz->own_fields[j], sizeof(w_Field));
  w_memcpy(&clazz->own_fields[j], &f, sizeof(w_Field));
}

 void sortFields(w_clazz clazz) {
  w_int i;
  w_int j;

  woempa(1, "Sorting fields of %k\n", clazz);
  i = 0;
  j = clazz->numFields - 1;
  while (i < j) {
    if (isSet(clazz->own_fields[i].flags, ACC_STATIC)) {
      woempa(1, "Field [%d] (`%w') is static, leaving it be\n",  i, clazz->own_fields[i].name);
      ++i;
    }
    else if (isNotSet(clazz->own_fields[j].flags, ACC_STATIC)) {
      woempa(1, "Field [%d] (`%w') is non-static, leaving it be\n",  j, clazz->own_fields[j].name);
      --j;
    }
    else {
      swapFields(clazz, i, j);
      ++i;
      --j;
    }
  }

  woempa(1, "Sorting static fields of %k\n", clazz);
  i = 0;
  j = clazz->numStaticFields - 1;
  while (i < j) {
    if (isSet(clazz->own_fields[i].flags, FIELD_IS_REFERENCE)) {
      woempa(1, "Static field [%d] (`%w') is  reference, leaving it be\n",  i, clazz->own_fields[i].name);
      ++i;
    }
    else if (isNotSet(clazz->own_fields[j].flags, FIELD_IS_REFERENCE)) {
      woempa(1, "Static field [%d] (`%w') is non-reference, leaving it be\n",  j, clazz->own_fields[j].name);
      --j;
    }
    else {
      swapFields(clazz, i, j);
    }
  }

  woempa(1, "Sorting non-static fields of %k\n", clazz);
  i = clazz->numStaticFields;
  j = clazz->numFields - 1;
  while (i < j) {
    if (isSet(clazz->own_fields[i].flags, FIELD_IS_REFERENCE)) {
      woempa(1, "Static field [%d] (`%w') is  reference, leaving it be\n",  i, clazz->own_fields[i].name);
      ++i;
    }
    else if (isNotSet(clazz->own_fields[j].flags, FIELD_IS_REFERENCE)) {
      woempa(1, "Static field [%d] (`%w') is non-reference, leaving it be\n",  j, clazz->own_fields[j].name);
      --j;
    }
    else {
      swapFields(clazz, i, j);
    }
  }

}

void get_fields(w_clazz clazz, w_bar s) {

  w_size i;
  w_size n;

  clazz->staticFields = NULL;
  clazz->numStaticFields = 0;
  clazz->numStaticWords = 0;
  n = get_u2(s);
  clazz->numFields = n;
  if (n > 0) {
    woempa(1, "Allocating %d w_Field's of %d bytes each\n", clazz->numFields, sizeof(w_Field));
    clazz->own_fields = allocClearedMem(clazz->numFields * sizeof(w_Field));
    if (!clazz->own_fields) {
      wabort(ABORT_WONKA, "Unable to allocate clazz->own_fields\n");
    }
    for (i = 0; i < n; i++) {
      parseField(clazz, i, s);
    }
  }

  if (n > 1) {
    sortFields(clazz);
  }

}

void get_methods(w_thread thread, w_clazz clazz, w_bar s) {

  w_size i;

  clazz->numDeclaredMethods = get_u2(s);
  clazz->numInheritableMethods = 0;
  if (clazz->numDeclaredMethods > 0) {
    clazz->own_methods = allocClearedMem(clazz->numDeclaredMethods * sizeof(w_Method));
    if (!clazz->own_methods) {
      wabort(ABORT_WONKA, "Unable to allocate clazz->own_methods\n");
    }
    for (i = 0; i < clazz->numDeclaredMethods; i++) {
      parseMethod(clazz, i, s); 
      if (exceptionThrown(thread)) {
        return;
      }
    }
  }

}

static void parseClassAttribute(w_thread thread, w_clazz clazz, w_bar s) {
  u2 attribute_name_index;
  u4 attribute_length;
  w_string attributeName;
  w_size  i;
  
  attribute_name_index = get_u2(s);
  attribute_length = get_u4(s);
  attributeName = resolveUtf8Constant(clazz, attribute_name_index);
  
  if (attributeName == string_SourceFile) {
    clazz->filename = resolveUtf8Constant(clazz, get_u2(s));
    woempa(1, "Source file = %w\n", clazz->filename);
  }
  else if (attributeName == string_InnerClasses) {
    w_size n = get_u2(s);
    if (n * 8 + 2 != attribute_length) {
      if (thread) {
        throwException(thread, clazzClassFormatError, "InnerClasses attribute has wrong length");
      }

      return;
    }

    clazz->temp.inner_class_info = allocMem(n * sizeof(w_InnerClassInfo));
    if (!clazz->temp.inner_class_info) {
      return;
    }

    clazz->temp.inner_class_info_count = n;
    for (i = 0; i < n; ++i) {
      clazz->temp.inner_class_info[i].inner_class_info_index = get_u2(s);	     
      clazz->temp.inner_class_info[i].outer_class_info_index = get_u2(s);	     
      clazz->temp.inner_class_info[i].inner_name_index = get_u2(s);	     
      clazz->temp.inner_class_info[i].inner_class_access_flags = get_u2(s);

    }
  }
#ifdef SUPPORT_BYTECODE_SCRAMBLING
  else if (attributeName == string_be_kiffer_Scrambled) {
    clazz->flags |= CLAZZ_IS_SCRAMBLED;
  }
#endif
  else {
    woempa(1, "Unknown attribute '%w'\n", attributeName);
    for (i = 0; (u4)i < attribute_length; i++) {
      get_u1(s);
    }
  }

}

static void get_attributes(w_thread thread, w_clazz clazz, w_bar s) {

  u2 numberOfAttributes;

  numberOfAttributes = get_u2(s);
  while (numberOfAttributes-- > 0) {
    parseClassAttribute(thread, clazz, s);
  }

}

w_clazz allocClazz() {

  w_clazz clazz = allocClearedMem(sizeof(w_Clazz));
  if (!clazz) {
    wabort(ABORT_WONKA, "Unable to allocate clazz\n");
  }
  clazz->label = (char *) "clazz";
  clazz->resolution_monitor = allocMem(sizeof(x_Monitor));
  if (!clazz->resolution_monitor) {
    wabort(ABORT_WONKA, "No space for clazz resolution monitor\n");
  }
  x_monitor_create(clazz->resolution_monitor);
  
  return clazz; 

}

void deallocClazz(w_clazz clazz) {
  woempa(7,"Releasing w_clazz at %p\n", clazz);
  releaseMem(clazz);
}


/*
** Create the w_Clazz structure corresponding to a class, reading the class
** file from byte-array reader'bar'.  The class will be registered with 
** 'loader' as its defining class loader.
*/

w_clazz createClazz(w_thread thread, w_string name, w_bar bar, w_instance loader, w_boolean trusted) {
  w_clazz clazz;

  if (loader && systemClassLoader && loader != systemClassLoader && namedClassIsSystemClass(name)) {
    throwException(thread, clazzSecurityException, "not allowed to define system class %w", name);

    return NULL;
  }

  clazz = allocClazz();
  // We initialise instanceSize to an impossible value, so any attempt
  // to allocate an instance of an unresolved clazz will fail.
  clazz->instanceSize = 32767;  // mugtrap
  clazz->numReferenceFields = 0;
  clazz->loader = loader;
  clazz->type = VM_TYPE_REF + VM_TYPE_OBJECT;
  clazz->bits = 32;
  clazz->resolution_thread = thread;
  setClazzState(clazz, CLAZZ_STATE_LOADING);

#ifndef NO_FORMAT_CHECKS
  if (!trusted && !pre_check_header(clazz, bar)) {
    throwException(thread, clazzClassFormatError, "error in class file header");
    destroyClazz(clazz);

    return NULL;
  }
#endif

  get_header(clazz, bar);

#ifndef NO_FORMAT_CHECKS
  if (!trusted && !pre_check_constant_pool(clazz, bar, thread)) {
    destroyClazz(clazz);

    return NULL;
  }
#endif

  get_constantpool(clazz, bar);

#ifndef NO_FORMAT_CHECKS
  if (!trusted && !pre_check_remainder(clazz, bar, thread)) {
    destroyClazz(clazz);

    return NULL;
  }
#endif

  // 'Or in' the access flags, so as not to destroy the clazz state
  clazz->flags |= get_u2(bar) & (ACC_PUBLIC | ACC_FINAL | ACC_SUPER | ACC_INTERFACE | ACC_ABSTRACT);
  if(isSet(clazz->flags, ACC_INTERFACE) && isNotSet(clazz->flags, ACC_ABSTRACT)){
    woempa(9,"How rude, clazz %w is has ACC_INTERFACE set but not ACC_ABSTRACT\n",name);
    woempa(9,"Fixing this ...\n");
    setFlag(clazz->flags, ACC_ABSTRACT);
  } 

  clazz->temp.this_index = get_u2(bar);
  clazz->temp.super_index = get_u2(bar);
  get_interfaces(clazz, bar);
  get_fields(clazz, bar);
  get_methods(thread, clazz, bar);
  get_attributes(thread, clazz, bar);

  if (exceptionThrown(thread)
#ifndef NO_FORMAT_CHECKS
    || (!trusted && !post_checks(clazz, name, thread))
#endif
  ) {
    destroyClazz(clazz);

    return NULL;
  }

  if (trusted) {
    setFlag(clazz->flags, CLAZZ_IS_TRUSTED);
  }

  set_classname(clazz);
  getPackageForClazz(clazz, loader);

  clazz->resolution_thread = NULL;
  setClazzState(clazz, CLAZZ_STATE_LOADED);
  // Don't try to attach a Class instance during initial bootstrap phase.
  // At the end of this phase all loaded classes get a Class instance attached,
  // from that point on it's done here at creation time.
  if (clazzClass && clazzClass->Class) {
    attachClassInstance(clazz, thread);
  }

  return registerClazz(thread, clazz, loader);  
}

/*
** Attach an instance of java.lang.Class to this clazz.
*/

w_instance attachClassInstance(w_clazz clazz, w_thread thread) {
  w_instance Class;
  w_int      i;
  w_boolean unsafe;

#ifdef RUNTIME_CHECKS
  if (clazzClass == NULL) {
    woempa(9, "clazzClass not defined\n");
    wabort(ABORT_WONKA, "Cannot create instance of Class for %k, as clazzClass not yet defined\n", clazz);
    return NULL;
  }

  if (getClazzState(clazz) < CLAZZ_STATE_LOADED) {
    wabort(ABORT_WONKA, "Cannot create instance of Class for unloaded class: %K\n", clazz);
    return NULL;
  }
#endif

  unsafe = thread && enterUnsafeRegion(thread);
  Class = allocInstance(thread, clazzClass);

  if (Class) {
    clazz->Class = Class;
    woempa(1, "Attached instance of Class (%p) to %k.\n", Class, clazz);
    if (clazz->dims) {
      w_instance base_Class;

      base_Class = clazz2Class(clazz->previousDimension);
      for (i = 0; i < (w_int)FIELD_OFFSET(F_Class_wotsit); ++i) {
        woempa(1,"Copying field[%d] (0x%08x) from Class %k to %k\n",i,base_Class[i], clazz->previousDimension, clazz);
        Class[i] = base_Class[i];
      }
    }
    else {
      setReferenceField_unsafe(Class, clazz->loader, F_Class_loader);
    }
    setWotsitField(Class, F_Class_wotsit, clazz);
  }

  if (thread && !unsafe) {
    enterSafeRegion(thread);
  }

  return Class;
}

/*
** Destroy a w_Method structure.
*/
static void destroyMethod(w_method method) {
  woempa(7, "  Destroying method %w%w\n", method->spec.name, method->desc);

  releaseMethodSpec(&method->spec);

  if (method->desc) {
    deregisterString(method->desc);
  }
  if (method->throws) {
    releaseMem(method->throws);
  }
  if (method->exec.code) {
    releaseMem(method->exec.code - 4);
  }
  if (method->exec.exceptions) {
    releaseMem(method->exec.exceptions);
  }
  if (method->exec.debug_info) {
    if (method->exec.debug_info->localVars) {
      releaseMem(method->exec.debug_info->localVars);
    }
    if (method->exec.debug_info->lineNums) {
      releaseMem(method->exec.debug_info->lineNums);
    }
    releaseMem(method->exec.debug_info);
  }
}

/*
** Destroy a w_Field structure.
*/
static inline void destroyField(w_field field) {
  woempa(7, "  Destroying field %w\n", field->name);
  deregisterString(field->name);
}

/*
** Examine an entry in interface_hashtable to see if it involves clazz c;
** if it does, put the imethod onto fifo f.
*/
static void interface_fifo_iterator(w_word clazz_word, w_word imethod_word, w_word method_word, void *c, void *f) {
  w_clazz found_clazz = (w_clazz) clazz_word;
  w_method imethod = (w_method) imethod_word;
  w_clazz target_clazz = c;

  if (found_clazz == target_clazz) {
    woempa(7, "Looking for %k: found it, %M implements %M\n", target_clazz, method_word, imethod);
    putFifo(imethod, f);
  }
}

/*
** Remove all the entries in the interface_hashtable referring to this class.
*/
static void destroyImplementations(w_clazz clazz) {
  w_method imethod;
  w_fifo fifo = allocFifo(255);

  ht2k_iterate(interface_hashtable, interface_fifo_iterator, clazz, fifo);
  while ((imethod = getFifo(fifo))) {
    woempa(7, "Erasing %k x %m from interface_hashtable\n", clazz, imethod);
    ht2k_erase(interface_hashtable, (w_word)clazz, (w_word)imethod);
  }
  releaseFifo(fifo);
}
  
/*
** Destroy the w_Clazz structure corresponding to a class.
*/

w_int destroyClazz(w_clazz clazz) {
  w_int i;
  w_int freed = sizeof(w_Clazz);
  
  woempa(7,"Destroying class %k\n",clazz);

  if (!clazz->dims) {
#ifdef RUNTIME_CHECKS
    if (getClazzState(clazz) >= CLAZZ_STATE_LOADED && (!clazz->loader || clazz->loader == systemClassLoader)) {
      wabort(ABORT_WONKA, "'S wounds! Attempt to destroy system class %k", clazz);
    }
#endif

    woempa(7, "Removing implementations from interface_hashtable \n");
    if (clazz->interfaces) {
      destroyImplementations(clazz);
      releaseMem(clazz->interfaces);
    }

    woempa(7, "Releasing class members\n");
    if (clazz->own_methods) {
      for (i = 0; i < (w_int)clazz->numDeclaredMethods; i++) {
        destroyMethod(&clazz->own_methods[i]);
      }
      releaseMem(clazz->own_methods);
    }

    if (clazz->own_fields) {
      for (i = 0; i < (w_int)clazz->numFields; i++) {
        destroyField(&clazz->own_fields[i]);
      }
      releaseMem(clazz->own_fields);
    }

    if (clazz->vmlt) {
      woempa(7, "Releasing vmlt and references\n");
      releaseMem(clazz->vmlt);
    }

    if (clazz->references) {
      releaseWordset(&clazz->references);
    }
    woempa(7, "Releasing supers and interfaces\n");
    if (clazz->supers) {
      releaseMem(clazz->supers);
    }
    if (clazz->staticFields) {
      releaseMem(clazz->staticFields);
    }

    if (clazz->filename) {
      deregisterString(clazz->filename);
    }

    if (clazz->temp.interface_index) {
      woempa(7, "Releasing temp.interface_index\n");
      releaseMem(clazz->temp.interface_index);
    }

    if (clazz->temp.inner_class_info) {
      woempa(7, "Releasing temp.inner_class_info\n");
      releaseMem(clazz->temp.inner_class_info);
    }

    if (clazz->resolution_monitor) {
      woempa(7, "Releasing resolution_monitor\n");
      x_monitor_delete(clazz->resolution_monitor);
      releaseMem(clazz->resolution_monitor);
    }
  }

  woempa(7,"Deregistering %k\n", clazz);
  deregisterClazz(clazz, clazz->loader);  

  if (clazz->failure_message) {
    woempa(7,"Deregistering failure message '%w'\n", clazz->failure_message);
    deregisterString(clazz->failure_message);
  }

  if (!clazz->dims) {
    woempa(7, "Releasing constant pool\n");
    for (i = 1; i < (w_int)clazz->numConstants; i++) {
      dissolveConstant(clazz, i);
    }
    if (clazz->tags){
      releaseMem((void*)clazz->tags);
    }
    if (clazz->values){
      releaseMem((void*)clazz->values);
    }
  }

  woempa(7, "Deregistering [dotified] class name\n");
  if (clazz->dotified) {
    deregisterString(clazz->dotified);
  }

  deallocClazz(clazz);

  return freed;
}

/*
** To find a field, we have to search recursively over the superclass
** and all superinterfaces.
*/
w_field getField(w_clazz clazz, w_string name) {
  w_clazz current_clazz = clazz;
  w_field field;
  w_size  i;
  w_size  j;

  woempa(1, "Looking for field `%w' in %k\n", name, clazz);
  while (current_clazz) {
    field = current_clazz->own_fields;
    for (i = 0; i < current_clazz->numFields; ++i) {
      woempa(1, "Trying `%w'\n", field->name);
      if (field->name == name) {
        woempa(1, "Success!  Found field `%w' in %k\n", name, current_clazz);

        return field;

      }
      ++field;
    }
    for (j = 0; j < current_clazz->numInterfaces; ++j) {
      woempa(1, "Looking in superinterface %w of %w\n", current_clazz->interfaces[j], current_clazz);
      field = getField(current_clazz->interfaces[j], name);
      if (field) {

        return field;

      }
    }
    current_clazz = getSuper(current_clazz);
    woempa(1, "Looking in superclass %k\n", current_clazz);
  }

  return NULL;
}

/*
** clazz2Class returns the w_instance of Class associated with this w_clazz.
** It calls attachClassInstance if necessary (which shouldn't be very often,
** as after the bootstrap phase every clazz gets a Class instance attached
** as soon as it is created.
*/
#ifdef RUNTIME_CHECKS
w_instance clazz2Class(w_clazz clazz) {
  w_instance Class;

  if (!clazz) {
    wabort(ABORT_WONKA, "clazz2Class(NULL)???");
  }

  Class = clazz->Class;
  if (Class == NULL) {
    wabort(ABORT_WONKA, "Gadzooks! %k->Class = NULL", clazz);
  }

  return Class;
}
#endif

/*
** Get a copy of a reference field of a class
*/
w_instance getStaticReferenceField(w_clazz clazz, w_int slot) {
  mustBeInitialized(clazz);
  return (w_instance)clazz->staticFields[slot];
}

/*
** Set a reference field of a class.
*/
void setStaticReferenceField(w_clazz clazz, w_int slot, w_instance child, w_thread thread) {
  w_boolean unsafe;

  mustBeInitialized(clazz);

  unsafe = thread && enterUnsafeRegion(thread);

  clazz->staticFields[slot] = (w_word)child;
  if (child) {
    setFlag(instance2flags(child), O_BLACK);
  }

  if (thread && !unsafe) {
    enterSafeRegion(thread);
  }
}

/*
** Set a reference field of a class, when the context is known to be 'unsafe'.
*/
void setStaticReferenceField_unsafe(w_clazz clazz, w_int slot, w_instance child, w_thread thread) {
  threadMustBeUnsafe(thread);

  mustBeInitialized(clazz);

  clazz->staticFields[slot] = (w_word)child;
  if (child) {
    setFlag(instance2flags(child), O_BLACK);
  }

}

const char * instruction2char[] = {
  "nop", "aconst_null", "iconst_m1", "iconst_0", "iconst_1", "iconst_2", "iconst_3", "iconst_4", "iconst_5",
  "lconst_0", "lconst_1", "fconst_0", "fconst_1", "fconst_2", "dconst_0", "dconst_1", "bipush", "sipush",
  "ldc", "ldc_w", "ldc2_w", "iload", "lload", "fload", "dload", "aload", "iload_0", "iload_1", "iload_2",
  "iload_3", "lload_0", "lload_1", "lload_2", "lload_3", "fload_0", "fload_1", "fload_2", "fload_3", "dload_0",
  "dload_1", "dload_2", "dload_3", "aload_0", "aload_1", "aload_2", "aload_3", "iaload", "laload", "faload",
  "daload", "aaload", "baload", "caload", "saload", "istore", "lstore", "fstore", "dstore", "astore", "istore_0",
  "istore_1", "istore_2", "istore_3", "lstore_0", "lstore_1", "lstore_2", "lstore_3", "fstore_0", "fstore_1",
  "fstore_2", "fstore_3", "dstore_0", "dstore_1", "dstore_2", "dstore_3", "astore_0", "astore_1", "astore_2",
  "astore_3", "iastore", "lastore", "fastore", "dastore", "aastore", "bastore", "castore", "sastore", "pop",
  "pop2", "dup", "dup_x1", "dup_x2", "dup2", "dup2_x1", "dup2_x2", "swap", "iadd", "ladd", "fadd", "dadd",
  "isub", "lsub", "fsub", "dsub", "imul", "lmul", "fmul", "dmul", "idiv", "ldiv", "fdiv", "ddiv", "irem",
  "lrem", "frem", "drem", "ineg", "lneg", "fneg", "dneg", "ishl", "lshl", "ishr", "lshr", "iushr", "lushr",
  "iand", "land", "ior", "lor", "ixor", "lxor", "iinc", "i2l", "i2f", "i2d", "l2i", "l2f", "l2d", "f2i",
  "f2l", "f2d", "d2i", "d2l", "d2f", "i2b", "i2c", "i2s", "lcmp", "fcmpl", "fcmpg", "dcmpl", "dcmpg", "ifeq",
  "ifne", "iflt", "ifge", "ifgt", "ifle", "if_icmpeq", "if_icmpne", "if_icmplt", "if_icmpge", "if_icmpgt",
  "if_icmple", "if_acmpeq", "if_acmpne", "goto", "jsr", "ret", "tableswitch", "lookupswitch", "ireturn",
  "lreturn", "freturn", "dreturn", "areturn", "return", "getstatic", "putstatic", "getfield", "putfield",
  "invokevirtual", "invokespecial", "invokestatic", "invokeinterface", "in_new", "new", "newarray",
  "anewarray", "arraylength", "athrow", "checkcast", "instanceof", "monitorenter", "monitorexit", "wide",
  "multianewarray", "ifnull", "ifnonnull", "goto_w", "jsr_w", "breakpoint", "in_getstatic", "in_getfield",
  "in_putfield", "getfield_quick", "putfield_quick", "getfield2_quick", "putfield2_quick", "getstatic_quick",
  "putstatic_quick", "getstatic2_quick", "putstatic2_quick", "invokevirtual_quick", "invokenonvirtual_quick",
  "invokesuper_quick", "invokestatic_quick", "invokeinterface_quick", "invokevirtualobject_quick",
  "new_quick", "anewarray_quick", "multianewarray_quick", "checkcast_quick", "instanceof_quick", "invokevirtual_quick_w",
  "getfield_quick_w", "putfield_quick_w", "no_code01", "no_code02", "no_code03", "no_code04", "no_code05",
  "no_code06", "no_code07", "no_code08", "no_code09", "no_code10", "no_code11", "no_code12", "no_code13",
  "no_code14", "no_code15", "no_code16", "no_code17", "no_code18", "no_code19", "no_code20", "no_code21",
  "no_code22", "no_code23", "no_code24", "no_code25", "impdep1", "impdep2",
};

