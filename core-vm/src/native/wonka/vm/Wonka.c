/**************************************************************************
* Copyright (c) 2008, 2015, 2021 by KIFFER Ltd. All rights reserved.      *
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

#include "clazz.h"
#include "descriptor.h"
#include "exception.h"
#include "fields.h"
#include "loading.h"
#include "methods.h"
#include "sha.h"
#include "wstrings.h"
#include "wonka.h"

#ifdef JDWP
extern void jdwp_init(void);
#endif

#ifdef MODULES 
extern void loadExtensions(void);
#else
extern void init_extensions(void);
#endif

w_int sort_interfaces(const void * v_i1, const void * v_i2) {

  w_clazz i1 = *(w_clazz *)v_i1;
  w_clazz i2 = *(w_clazz *)v_i2;
 
  return compareStrings(i1->dotified, i2->dotified);
  
}

w_int sort_methods(const void * v_m1, const void * v_m2) {

  w_method m1 = *(w_method *)v_m1;
  w_method m2 = *(w_method *)v_m2;
  w_size   l1;
  w_size   l2;
  w_size   i;
  w_string s1;
  w_string s2;
  w_char *b1;
  w_char *b2;
  w_int result;
  
  l1 = string_length(m1->spec.name) + 2;
  if (m1->spec.arg_types) {
    for (i = 0; m1->spec.arg_types[i]; ++i) {
      s1 = clazz2desc(m1->spec.arg_types[i]);
      if (!s1) {
        wabort(ABORT_WONKA, "Unable to create s1\n");
      }
      l1 += string_length(s1);
      deregisterString(s1);
    }
  }
  s1 = clazz2desc(m1->spec.return_type);
  if (!s1) {
    wabort(ABORT_WONKA, "Unable to create s1\n");
  }
  l1 += string_length(s1);
  deregisterString(s1);

  l2 = string_length(m2->spec.name) + 2;
  if (m2->spec.arg_types) {
    for (i = 0; m2->spec.arg_types[i]; ++i) {
      s2 = clazz2desc(m2->spec.arg_types[i]);
      if (!s2) {
        wabort(ABORT_WONKA, "Unable to create s2\n");
      }
      l2 += string_length(s2);
      deregisterString(s2);
    }
  }
  s2 = clazz2desc(m2->spec.return_type);
  if (!s2) {
    wabort(ABORT_WONKA, "Unable to create s2\n");
  }
  l2 += string_length(s2);
  deregisterString(s2);

  b1 = allocMem(l1 * sizeof(w_char));
  if (!b1) {
    wabort(ABORT_WONKA, "Unable to allocate b1\n");
  }
  stringAdd(b1, m1->spec.name);
  l1 = string_length(m1->spec.name);
  b1[l1++] = '(';
  if (m1->spec.arg_types) {
    for (i = 0; m1->spec.arg_types[i]; ++i) {
      s1 = clazz2desc(m1->spec.arg_types[i]);
      if (!s1) {
        wabort(ABORT_WONKA, "Unable to create s1\n");
      }
      stringAdd(b1 + l1, s1);
      l1 += string_length(s1);
      deregisterString(s1);
    }
  }
  b1[l1++] = ')';
  s1 = clazz2desc(m1->spec.return_type);
  if (!s1) {
    wabort(ABORT_WONKA, "Unable to create s1\n");
  }
  stringAdd(b1 + l1, s1);
  l1 += string_length(s1);
  deregisterString(s1);
  s1 = unicode2String(b1, l1);
  if (!s1) {
    wabort(ABORT_WONKA, "Unable to create s1\n");
  }
  releaseMem(b1);

  b2 = allocMem(l2 * sizeof(w_char));
  if (!b2) {
    wabort(ABORT_WONKA, "Unable to allocate b2\n");
  }
  stringAdd(b2, m2->spec.name);
  l2 = string_length(m2->spec.name);
  b2[l2++] = '(';
  if (m2->spec.arg_types) {
    for (i = 0; m2->spec.arg_types[i]; ++i) {
      s2 = clazz2desc(m2->spec.arg_types[i]);
      if (!s2) {
        wabort(ABORT_WONKA, "Unable to create s2\n");
      }
      stringAdd(b2 + l2, s2);
      l2 += string_length(s2);
      deregisterString(s2);
    }
  }
  b2[l2++] = ')';
  s2 = clazz2desc(m2->spec.return_type);
  if (!s2) {
    wabort(ABORT_WONKA, "Unable to create s2\n");
  }
  stringAdd(b2 + l2, s2);
  l2 += string_length(s2);
  deregisterString(s2);
  s2 = unicode2String(b2, l2);
  if (!s2) {
    wabort(ABORT_WONKA, "Unable to create s2\n");
  }
  releaseMem(b2);

  woempa(1, "SERIALISE: %w <=> %w\n", s1, s2);
  result = compareStrings(s1, s2);
  
  deregisterString(s1);
  deregisterString(s2);

  return result;
    
}

w_int sort_fields(const void * v_f1, const void * v_f2) {

  w_field f1 = *(w_field *)v_f1;
  w_field f2 = *(w_field *)v_f2;

  return compareStrings(f1->name, f2->name);

}

typedef union  {
  w_word W;
  w_byte B[4];
} word2bytes;

static w_int addModifiers(w_byte *buffer, w_word modifiers) {

  word2bytes u;
  
  u.W = (modifiers & ACC_FLAGS);
#if __BYTE_ORDER == __LITTLE_ENDIAN
  buffer[0] = u.B[3];
  buffer[1] = u.B[2];
  buffer[2] = u.B[1];
  buffer[3] = u.B[0];
#else
  buffer[0] = u.B[0];
  buffer[1] = u.B[1];
  buffer[2] = u.B[2];
  buffer[3] = u.B[3];
#endif  

  return 4;

}    

w_word bytes2word(w_byte *bytes) {

  word2bytes u;
  
#if __BYTE_ORDER == __LITTLE_ENDIAN
  u.B[3] = bytes[0];
  u.B[2] = bytes[1];
  u.B[1] = bytes[2];
  u.B[0] = bytes[3];
#else
  u.B[0] = bytes[0];
  u.B[1] = bytes[1];
  u.B[2] = bytes[2];
  u.B[3] = bytes[3];
#endif  

  return u.W;

}    

#include <ctype.h>
                                        
w_long Wonka_suid(w_thread thread, w_instance Wonka, w_instance Class) {
  w_clazz clazz = Class2clazz(Class);
  w_long suid = 0;
  w_clazz *interfaces = NULL;
  w_size numInterfaces = 0;
  w_field *fields = NULL;
  w_size numFields = 0;
  w_method *constructors = NULL;
  w_size numConstructors = 0;
  w_method *methods = NULL;
  w_size numMethods = 0;
  w_size i;
  w_size j;
  w_size k;
  w_size chars;
  w_byte *buffer;
  w_byte *utf8;
  w_int length;
  w_sha sha;
  w_string zig;
  w_string tmp;
  w_size   sigsize;
  w_char  *sig;
  w_flags  flags;
  w_method m;

  woempa(1, "SERIALISE: Calculating suid of clazz %k\n", clazz);

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    return 0;
  }

  /*
  ** See if we have a cached version of the SUID in our sleeves...
  */

  if (clazz->suid != 0) {
    woempa(1, "SERIALISE: Returning cached suid 0x%x%x\n", MSW_PART(clazz->suid), LSW_PART(clazz->suid));
    return clazz->suid;
  }


  if (!clazz->dims) {
    /*
    ** First create the arrays so that they can be sorted, some arrays are overallocated
    ** but they are released soon enough so that is not a problem.
    */

  numInterfaces = clazz->numDirectInterfaces;
  if (numInterfaces) {
    interfaces = allocMem(numInterfaces * sizeof(w_clazz));
    if (!interfaces) {
      wabort(ABORT_WONKA, "Unable to allocate interfaces\n");
    }
    w_memcpy(interfaces, clazz->interfaces, numInterfaces * sizeof(w_clazz));
    qsort(interfaces, numInterfaces, sizeof(w_clazz), sort_interfaces);
  }

  if (clazz->numFields) {
    fields = allocMem(clazz->numFields * sizeof(w_field));
    if (!fields) {
      wabort(ABORT_WONKA, "Unable to allocate fields\n");
    }
    for (i = 0; i < clazz->numFields; i++) {
      if (clazz->own_fields[i].declaring_clazz == clazz) {
        flags = clazz->own_fields[i].flags;
        if (! (isSet(flags, ACC_PRIVATE) && isSet(flags, ACC_STATIC)) && ! (isSet(flags, ACC_PRIVATE) && isSet(flags, ACC_TRANSIENT)) ) {
          fields[numFields++] = &clazz->own_fields[i];
        }
      }
    }
    qsort(fields, numFields, sizeof(w_field), sort_fields);
  }

  /*
  ** We can not directly rule out that constructors or methods do not apply to this algorithm. So
  ** we have to allocate memory, but still can end up with numConstructors and numMethods being 0...
  */

  constructors = allocMem(clazz->numDeclaredMethods * sizeof(w_method));
  if (!constructors) {
    wabort(ABORT_WONKA, "Unable to allocate constructors\n");
  }
  for (i = 0; i < clazz->numDeclaredMethods; ++i) {
    m = &clazz->own_methods[i];
    if ((m->spec.name == string_angle_brackets_init) && isNotSet(m->flags, ACC_PRIVATE)) {
      constructors[numConstructors++] = m;
    }
  }
  if (numConstructors) {
    qsort(constructors, numConstructors, sizeof(w_method), sort_methods);
  }

  methods = allocMem(clazz->numDeclaredMethods * sizeof(w_method));
  if (!methods) {
    wabort(ABORT_WONKA, "Unable to allocate methods\n");
  }
  for (i = 0; i < clazz->numDeclaredMethods; ++i) {
    m = &clazz->own_methods[i];
    if (m->spec.name != string_angle_brackets_init && m->spec.name != string_angle_brackets_clinit && isNotSet(m->flags, ACC_PRIVATE)) {
      methods[numMethods++] = m;
    }
  }
  if (numMethods) {
    qsort(methods, numMethods, sizeof(w_method), sort_methods);
  }

  /*
  ** Count the number of chars we need to convert back to UTF8, we need this count so we can allocate a suitably size
  ** buffer to convert the strings to UTF8, add the 32 bit modifier integers and calculate the SHA signature from.
  ** Note that we add 2 to each length to accomodate the 2 bytes for the length in the UTF8 byte sequence, and
  ** we add sizeof(w_word) for accomodating the modifiers.
  */
  
  chars = string_length(clazz->dotified) + 2;
  chars += sizeof(w_word);
  
  for (i = 0; i < numInterfaces; i++) {
    chars += string_length(interfaces[i]->dotified) + 2;
    woempa(1, "  INTERFACE %2d: %w\n", i, interfaces[i]->dotified);
  }

  if (clazz->clinit) {
    chars += string_length(clazz->clinit->spec.name);
    if (clazz->clinit->spec.arg_types) {
      for (j = 0; clazz->clinit->spec.arg_types[j]; ++j) {
        tmp = clazz2desc(clazz->clinit->spec.arg_types[j]);
        if (!tmp) {
          wabort(ABORT_WONKA, "Unable to create tmp\n");
        }
        chars += string_length(tmp);
        deregisterString(tmp);
      }
    }
    tmp = clazz2desc(clazz->clinit->spec.return_type);
    if (!tmp) {
      wabort(ABORT_WONKA, "Unable to create tmp\n");
    }
    chars += string_length(tmp);
    deregisterString(tmp);
    chars += sizeof(w_word) + 2 + 2;
  }
  
  for (i = 0; i < numFields; i++) {
    chars += string_length(fields[i]->name);
    tmp = clazz2desc(fields[i]->value_clazz);
    if (!tmp) {
      wabort(ABORT_WONKA, "Unable to create tmp\n");
    }
    chars += string_length(tmp);
    deregisterString(tmp);
    chars += sizeof(w_word) + 2 + 2;
    woempa(1, "      FIELD %2d: %w\n", i, fields[i]->name);
  }

  for (i = 0; i < numConstructors; i++) {
    chars += string_length(constructors[i]->spec.name);
    if (constructors[i]->spec.arg_types) {
      for (j = 0; constructors[i]->spec.arg_types[j]; ++j) {
        tmp = clazz2desc(constructors[i]->spec.arg_types[j]);
        if (!tmp) {
          wabort(ABORT_WONKA, "Unable to create tmp\n");
        }
        chars += string_length(tmp);
        deregisterString(tmp);
      }
    }
    tmp = clazz2desc(constructors[i]->spec.return_type);
    if (!tmp) {
      wabort(ABORT_WONKA, "Unable to create tmp\n");
    }
    chars += string_length(tmp);
    deregisterString(tmp);
    chars += sizeof(w_word) + 2 + 2;
    woempa(1, "CONSTRUCTOR %2d: %m\n", i, constructors[i]);
  }

  for (i = 0; i < numMethods; i++) {
    chars += string_length(methods[i]->spec.name);
    if (methods[i]->spec.arg_types) {
      for (j = 0; methods[i]->spec.arg_types[j]; ++j) {
        tmp = clazz2desc(methods[i]->spec.arg_types[j]);
        if (!tmp) {
          wabort(ABORT_WONKA, "Unable to create tmp\n");
        }
        chars += string_length(tmp);
        deregisterString(tmp);
      }
    }
    tmp = clazz2desc(methods[i]->spec.return_type);
    if (!tmp) {
      wabort(ABORT_WONKA, "Unable to create tmp\n");
    }
    chars += string_length(tmp);
    deregisterString(tmp);
    chars += sizeof(w_word) + 2 + 2;
    woempa(1, "     METHOD %2d: %m\n", i, methods[i]);
  }

  /*
  ** Now allocate a working buffer and convert strings back to UTF8, add the modifiers at the same time, for
  ** each w_char we allocate 3 bytes for the worst case UTF8 encoding.
  */
  
  buffer = allocMem(chars * (3 * sizeof(w_byte)));
  if (!buffer) {
    wabort(ABORT_WONKA, "Unable to allocate buffer\n");
  }
  chars = 0;

  utf8 = string2UTF8(clazz->dotified, &length);
  woempa(1, "SERIALISE: utf8 = %d:'%s'\n", length-2, utf8+2);
  w_memcpy(buffer, utf8, length);
  releaseMem(utf8);
  chars += length;

  /*
  ** It seems that this ACC_SUPER flag, which is always set by the compiler, needs to be unset
  ** before calculating the SUID. Try to find this when it's not in the SUN specs for serialization...
  */
  
  flags = clazz->flags;
  unsetFlag(flags, ACC_SUPER);
  chars += addModifiers(buffer + chars, flags);

  for (i = 0; i < numInterfaces; i++) {
    utf8 = string2UTF8(interfaces[i]->dotified, &length);
    woempa(1, "SERIALISE: utf8 = %d:'%s'\n", length-2, utf8+2);
    w_memcpy(buffer + chars, utf8, length);
    releaseMem(utf8);
    chars += length;
  }

  for (i = 0; i < numFields; i++) {
    utf8 = string2UTF8(fields[i]->name, &length);
    woempa(1, "SERIALISE: utf8 = %d:'%s'\n", length-2, utf8+2);
    w_memcpy(buffer + chars, utf8, length);
    releaseMem(utf8);
    chars += length;
    chars += addModifiers(buffer + chars, fields[i]->flags);
    woempa(1, "SERIALISE: buffer length = %d\n", chars);
    tmp = clazz2desc(fields[i]->value_clazz);
    if (!tmp) {
      wabort(ABORT_WONKA, "Unable to create tmp\n");
    }
    utf8 = string2UTF8(tmp, &length);
    deregisterString(tmp);
    woempa(1, "SERIALISE: utf8 = %d:'%s'\n", length-2, utf8+2);
    w_memcpy(buffer + chars, utf8, length);
    releaseMem(utf8);
    chars += length;
  }
  
  if (clazz->clinit) {
    utf8 = string2UTF8(clazz->clinit->spec.name, &length);
    woempa(1, "SERIALISE: utf8 = %d:'%s'\n", length-2, utf8+2);
    w_memcpy(buffer + chars, utf8, length);
    releaseMem(utf8);
    chars += length;
    chars += addModifiers(buffer + chars, ACC_STATIC);
    woempa(1, "SERIALISE: buffer length = %d\n", chars);
    w_memcpy(buffer + chars, "\000\003()V", 5);
    chars += 5;
  }
  
  for (i = 0; i < numConstructors; i++) {
    utf8 = string2UTF8(constructors[i]->spec.name, &length);
    woempa(1, "SERIALISE: utf8 = %d:'%s'\n", length-2, utf8+2);
    w_memcpy(buffer + chars, utf8, length);
    releaseMem(utf8);
    chars += length;
    chars += addModifiers(buffer + chars, constructors[i]->flags);
    sigsize = 2;
    if (constructors[i]->spec.arg_types) {
      for (j = 0; constructors[i]->spec.arg_types[j]; ++j) {
        tmp = clazz2desc(constructors[i]->spec.arg_types[j]);
        if (!tmp) {
          wabort(ABORT_WONKA, "Unable to create tmp\n");
        }
        sigsize += string_length(tmp);
        deregisterString(tmp);
      }
    }
    tmp = clazz2desc(constructors[i]->spec.return_type);
    if (!tmp) {
      wabort(ABORT_WONKA, "Unable to create tmp\n");
    }
    sigsize += string_length(tmp);
    deregisterString(tmp);
    sig = allocMem(sigsize * sizeof(w_char));
    if (!sig) {
      wabort(ABORT_WONKA, "Unable to allocate sig\n");
    }
    sig[0] = '(';
    sigsize = 1;
    if (constructors[i]->spec.arg_types) {
      for (j = 0; constructors[i]->spec.arg_types[j]; ++j) {
        tmp = clazz2desc(constructors[i]->spec.arg_types[j]);
        if (!tmp) {
          wabort(ABORT_WONKA, "Unable to create tmp\n");
        }
        for (k = 0; k < string_length(tmp); ++k) {
          sig[k + sigsize] = string_char(tmp, k);
        }
        sigsize += string_length(tmp);
        deregisterString(tmp);
      }
    }
    sig[sigsize++] = ')';
    tmp = clazz2desc(constructors[i]->spec.return_type);
    if (!tmp) {
      wabort(ABORT_WONKA, "Unable to create tmp\n");
    }
    for (k = 0; k < string_length(tmp); ++k) {
      sig[k + sigsize] = string_char(tmp, k);
    }
    sigsize += string_length(tmp);
    deregisterString(tmp);
    zig = unicode2String(sig, sigsize);
    if (!zig) {
      wabort(ABORT_WONKA, "Unable to create zig\n");
    }
    releaseMem(sig);
    tmp = slashes2dots(zig);
    if (!tmp) {
      wabort(ABORT_WONKA, "Unable to dotify zig\n");
    }
    woempa(1, "SERIALISE: %w descriptor = '%w', dotified = '%w'\n", constructors[i]->spec.name, zig, tmp);
    deregisterString(zig);
    utf8 = string2UTF8(tmp, &length);
    woempa(1, "SERIALISE: utf8 = %d:'%s'\n", length-2, utf8+2);
    deregisterString(tmp);
    w_memcpy(buffer + chars, utf8, length);
    releaseMem(utf8);
    chars += length;
  }
  
  for (i = 0; i < numMethods; i++) {
    utf8 = string2UTF8(methods[i]->spec.name, &length);
    woempa(1, "SERIALISE: utf8 = %d:'%s'\n", length-2, utf8+2);
    w_memcpy(buffer + chars, utf8, length);
    releaseMem(utf8);
    chars += length;
    chars += addModifiers(buffer + chars, methods[i]->flags);
    sigsize = 2;
    if (methods[i]->spec.arg_types) {
      for (j = 0; methods[i]->spec.arg_types[j]; ++j) {
        tmp = clazz2desc(methods[i]->spec.arg_types[j]);
        if (!tmp) {
          wabort(ABORT_WONKA, "Unable to create tmp\n");
        }
        sigsize += string_length(tmp);
        deregisterString(tmp);
      }
    }
    tmp = clazz2desc(methods[i]->spec.return_type);
    if (!tmp) {
      wabort(ABORT_WONKA, "Unable to create tmp\n");
    }
    sigsize += string_length(tmp);
    deregisterString(tmp);
    sig = allocMem(sigsize * sizeof(w_char));
    if (!sig) {
      wabort(ABORT_WONKA, "Unable to allocate sig\n");
    }
    woempa(1, "sigsize %d\n", sigsize);
    sig[0] = '(';
    sigsize = 1;
    if (methods[i]->spec.arg_types) {
      for (j = 0; methods[i]->spec.arg_types[j]; ++j) {
        tmp = clazz2desc(methods[i]->spec.arg_types[j]);
        if (!tmp) {
          wabort(ABORT_WONKA, "Unable to create tmp\n");
        }
        for (k = 0; k < string_length(tmp); ++k) {
          sig[k + sigsize] = string_char(tmp, k);
        }
        sigsize += string_length(tmp);
        deregisterString(tmp);
      }
    }
    sig[sigsize++] = ')';
    tmp = clazz2desc(methods[i]->spec.return_type);
    if (!tmp) {
      wabort(ABORT_WONKA, "Unable to create tmp\n");
    }
    for (k = 0; k < string_length(tmp); ++k) {
      sig[k + sigsize] = string_char(tmp, k);
    }
    sigsize += string_length(tmp);
    deregisterString(tmp);
    woempa(1, "sigsize %d\n", sigsize);
    zig = unicode2String(sig, sigsize);
    if (!zig) {
      wabort(ABORT_WONKA, "Unable to create zig\n");
    }
    releaseMem(sig);
    tmp = slashes2dots(zig);
    if (!tmp) {
      wabort(ABORT_WONKA, "Unable to dotify zig\n");
    }
    woempa(1, "SERIALISE: %w descriptor = '%w', dotified = '%w'\n", methods[i]->spec.name, zig, tmp);
    deregisterString(zig);
    utf8 = string2UTF8(tmp, &length);
    woempa(1, "SERIALISE: utf8 = %d:'%s'\n", length-2, utf8+2);
    deregisterString(tmp);
    w_memcpy(buffer + chars, utf8, length);
    releaseMem(utf8);
    chars += length;
  }
  }
  else {

    /*
    ** It is an array clazz, just take name and modifiers and process along...
    */

    buffer = allocMem(2 + string_length(clazz->dotified) * (3 * sizeof(w_byte)));
    if (!buffer) {
      wabort(ABORT_WONKA, "Unable to allocate buffer\n");
    }
    chars = 0;
    utf8 = string2UTF8(clazz->dotified, &length);
    woempa(1, "SERIALISE: utf8 = %d:'%s'\n", length-2, utf8+2);
    w_memcpy(buffer + chars, utf8, length);
    releaseMem(utf8);
    chars += length;
    flags = clazz->flags;
    unsetFlag(flags, ACC_SUPER);
    chars += addModifiers(buffer + chars, flags);

  }

    woempa(1, "SERIALISE: buffer length = %d\n", chars);
  sha = allocSha();
  processSha(sha, buffer, chars);
  finishSha(sha);
  
  suid = sha->signature.W[1] & 0xff;
  suid = (suid << 8) | ((sha->signature.W[1] >> 8) & 0xff);
  suid = (suid << 8) | ((sha->signature.W[1] >> 16) & 0xff);
  suid = (suid << 8) | ((sha->signature.W[1] >> 24) & 0xff);
  suid = (suid << 8) | ((sha->signature.W[0]) & 0xff);
  suid = (suid << 8) | ((sha->signature.W[0] >> 8) & 0xff);
  suid = (suid << 8) | ((sha->signature.W[0] >> 16) & 0xff);
  suid = (suid << 8) | ((sha->signature.W[0] >> 24) & 0xff);

  woempa(1,"SERIALISE: suid = %02x %02x %02x %02x %02x %02x %02x %02x\n",
    sha->signature.B[7], sha->signature.B[6], sha->signature.B[5], sha->signature.B[4],
    sha->signature.B[3], sha->signature.B[2], sha->signature.B[1], sha->signature.B[0]);

  if (interfaces) {
    releaseMem(interfaces);
  }

  if (fields) {
    releaseMem(fields);
  }

  if (constructors) {
    releaseMem(constructors);
  }

  if (methods) {
    releaseMem(methods);
  }

  releaseMem(buffer);

  releaseSha(sha);

  /*
  ** Cache the SUID in the clazz structure for the next time around...
  */
  
  clazz->suid = suid;
  
  return suid;

}

void Wonka_static_setWonkaVerbose(w_thread thread, w_instance theClass, w_instance verboseString) {
  w_string verbose_string = String2string(verboseString);

  if (verbose_string && !verbose_cstring) {
    w_size l = string_length(verbose_string);

    verbose_cstring = allocMem(l + 1);
    x_snprintf(verbose_cstring, l + 1, "%w", verbose_string);
    // Don't think the next line is needed, but here it is anyway
    verbose_cstring[l] = 0;
    if (l == 1 && verbose_cstring[0] == '?') {
      w_printf("mika.verbose should be a colon(:)-separated selection from the following list\n");
      w_printf("  startup:  VM startup\n");
      w_printf("  shutdown: VM shutdown\n");
      w_printf("  gc:       garbage collection\n");
      w_printf("  threads:  thread creation/completion\n");
      w_printf("  loading:  loading and unloading of classes\n");
#ifdef DEBUG_STACKS
      w_printf("  stack:    stack depth etc. at each method invocation\n");
#endif
      w_printf("  throw:    exception handling\n");
      w_printf("  socket:   socket activity\n");
      w_printf("  traffic:  bytes sent/received over sockets\n");
      w_printf("  http:     traffic interpreted as HTTP\n");
      w_printf("  exec:     Runtime.exec()\n");
#ifdef JDWP
      w_printf("  jdwp:     JDWP\n");
#endif
    }
    else if (l) {
      w_printf("mika.verbose=%s\n", verbose_cstring);
    }
    if (strstr(verbose_cstring, "start")) {
      verbose_flags |= VERBOSE_FLAG_STARTUP;
    }
    if (strstr(verbose_cstring, "shut")) {
      verbose_flags |= VERBOSE_FLAG_SHUTDOWN;
    }
    if (strstr(verbose_cstring, "thread")) {
      verbose_flags |= VERBOSE_FLAG_THREAD;
    }
    if (strstr(verbose_cstring, "gc")) {
      verbose_flags |= VERBOSE_FLAG_GC;
    }
    if (strstr(verbose_cstring, "init")) {
      verbose_flags |= VERBOSE_FLAG_INIT;
    }
    if (strstr(verbose_cstring, "load")) {
      verbose_flags |= VERBOSE_FLAG_LOAD;
    }
    if (strstr(verbose_cstring, "stack")) {
      verbose_flags |= VERBOSE_FLAG_STACK;
    }
    if (strstr(verbose_cstring, "socket")) {
      verbose_flags |= VERBOSE_FLAG_SOCKET;
    }
    if (strstr(verbose_cstring, "traffic")) {
      verbose_flags |= VERBOSE_FLAG_TRAFFIC;
    }
    if (strstr(verbose_cstring, "throw")) {
      verbose_flags |= VERBOSE_FLAG_THROW;
    }
    if (strstr(verbose_cstring, "url")) {
      verbose_flags |= VERBOSE_FLAG_URL;
    }
    if (strstr(verbose_cstring, "http")) {
      verbose_flags |= VERBOSE_FLAG_HTTP;
    }
    if (strstr(verbose_cstring, "exec")) {
      verbose_flags |= VERBOSE_FLAG_EXEC;
    }
    if (strstr(verbose_cstring, "jdwp")) {
      verbose_flags |= VERBOSE_FLAG_JDWP;
    }
  }
}

void Wonka_static_setMethodDebugInfo(w_thread thread, w_instance theClass, w_boolean enable) {
  use_method_debug_info = enable;
}

void Wonka_static_loadExtensions(w_thread thread, w_instance theClass) {
#ifdef MODULES 
  loadExtensions();
#else 
  init_extensions();
#endif
}

