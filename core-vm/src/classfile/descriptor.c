/**************************************************************************
* Parts copyright (c) 2001, 2002 by Punch Telematix. All rights reserved. *
* Parts copyright (c) 2004, 2021 by Chris Gray, Kiffer Ltd.               *
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
* 3. Neither the name of Punch Telematix or of Kiffer Ltd  nor the names  *
*    of other contributors may be used to endorse or promote products     *
*    derived from this software without specific prior written            *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, KIFFER LTD OR OTHER  CONTRIBUTORS    *
* BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,     *
* OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT    *
* OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR      *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,       *
* EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                      *
**************************************************************************/

#include <stdio.h>
#include <string.h>

#include "clazz.h"
#include "descriptor.h"
#include "loading.h"
#include "wonka.h"


const char primitive2char[] = {  
  '*', 
  '*', 
  '*', 
  '*', 
  'Z', 
  'C', 
  'F', 
  'D', 
  'B', 
  'S', 
  'I', 
  'J', 
  'V'
};

/*
** An array that maps primitive descriptors to a C string. It corresponds with
** the P_Type enum declaration in wonka.h
*/

const char *primitive2name[] = {  
  "<invalid primitive>", 
  "<invalid primitive>", 
  "<invalid primitive>", 
  "<invalid primitive>", 
  "boolean", 
  "char", 
  "float", 
  "double", 
  "byte", 
  "short", 
  "int", 
  "long", 
  "void"
};

/*
** An array that maps descriptors to their size in bits. It corresponds with
** the P_Type enum declaration in wonka.h
*/

const unsigned char primitive2bits[] = {
   0,
   0,
   0,
   0,
   1,  /*  4: P_Type: boolean */
  16,  /*  5: P_Type: char    */
  32,  /*  6: P_Type: float   */
  64,  /*  7: P_Type: double  */
   8,  /*  8: P_Type: byte    */
  16,  /*  9: P_Type: short   */
  32,  /* 10: P_Type: int     */
  64,  /* 11: P_Type: long    */
   0,  /* 12: P_Type: void    */
  32,  /* 13: P_Type: Object  */
  32,  /* 14: D_Type: Class   */
  32,  /* 15: D_Type: Array   */
   0
};

/*
** An array that maps descriptors to their size in words (32 bits in a word). It corresponds with
** the P_Type enum declaration in wonka.h
*/

const unsigned char primitive2words[] = {
   0,
   0,
   0,
   0,
   1,  /*  4: P_Type: boolean */
   1,  /*  5: P_Type: char    */
   1,  /*  6: P_Type: float   */
   2,  /*  7: P_Type: double  */
   1,  /*  8: P_Type: byte    */
   1,  /*  9: P_Type: short   */
   1,  /* 10: P_Type: int     */
   2,  /* 11: P_Type: long    */
   0,  /* 12: P_Type: void    */
   1,  /* 13: P_Type: Object  */
   1,  /* 14: D_Type: Class   */
   1,  /* 15: D_Type: Array   */
   0
};

w_instance primitive2classInstance[13];
w_clazz primitive2clazz[13];
w_clazz primitive2wrapper[13];
w_size primitive2wrapperSlot[13];

w_clazz parseDescriptor(w_string descriptor, w_size *start, w_size end, w_instance loader) {
  w_clazz result = NULL;
  w_char  *desc_chars;
  w_string desc_string;
  w_string temp_string;
  w_string dotified;
  w_size   dims = 0;
  w_size   k;

    desc_chars = allocMem(string_length(descriptor) * sizeof(w_char));
    w_string2chars(descriptor, desc_chars);
    dims = 0;
    while (string_char(descriptor, *start) == '[') {
      ++dims;
      ++*start;
      if (*start >= end) {
        woempa(9, "descriptor %w ends with '[' !\n", descriptor);
        break;
      }
    }
    if (dims) {
      switch (string_char(descriptor, (*start)++)) {
      case 'Z':
      case 'C':
      case 'F':
      case 'D':
      case 'B':
      case 'S':
      case 'I':
      case 'J':
        temp_string = unicode2String(&desc_chars[*start - dims - 1], dims + 1);
        if (!temp_string) {
          wabort(ABORT_WONKA, "Unable to create %s\n", "temp_string");
        }
        result = identifyClazz(temp_string, NULL);
        break;
  
      case 'L':
        k = *start;
        while (k < end) {
          if (string_char(descriptor, k) == ';') {
            break;
          }
          ++k;
        }
        if (k == end) {
          woempa(9, "descriptor %w lacks final ';' !\n", descriptor);
          break;
        }
        desc_string = unicode2String(&desc_chars[*start - dims - 1], k - *start + dims + 2);
        if (!desc_string) {
          wabort(ABORT_WONKA, "Unable to create %s\n", "desc_string");
        }
        *start = k + 1;
        temp_string = slashes2dots(desc_string);
        if (!temp_string) {
          wabort(ABORT_WONKA, "Unable to dotify %s\n", "desc");
        }
        result = identifyClazz(temp_string, loader);
        deregisterString(desc_string);
        break;
  
      default:
        woempa(9, "Illegal char %c\n", string_char(descriptor, *start-1));
      }
      deregisterString(temp_string);
    }
    else {
      switch (string_char(descriptor, (*start)++)) {
      case 'Z':
        result = clazz_boolean;
        break;
  
      case 'C':
        result = clazz_char;
        break;
  
      case 'F':
        result = clazz_float;
        break;

      case 'D':
        result = clazz_double;
        break;

      case 'B':
        result = clazz_byte;
        break;

      case 'S':
        result = clazz_short;
        break;
  
      case 'I':
        result = clazz_int;
        break;
  
      case 'J':
        result = clazz_long;
        break;

      case 'V':
        result = clazz_void;
        break;

      case 'L':
        k = *start;
        while (k < end) {
          if (string_char(descriptor, k) == ';') {
            break;
           }
          ++k;
        }
        if (k == end) {
          woempa(9, "descriptor %w lacks final ';' !\n", descriptor);
          break;
        }

        temp_string = unicode2String(&desc_chars[*start], k - *start);
        if (!temp_string) {
          wabort(ABORT_WONKA, "Unable to create %s\n", "temp_string");
        }
        dotified = slashes2dots(temp_string);
        if (!dotified) {
          wabort(ABORT_WONKA, "Unable to dotify %s\n", "desc");
        }
        result = identifyClazz(dotified, loader);
        deregisterString(dotified);
        deregisterString(temp_string);
        *start = k + 1;
        break;

      default:
        woempa(9, "Illegal char %c\n", string_char(descriptor, *start-1));
      }
    }
    releaseMem(desc_chars);

  if (result && getClazzState(result) == CLAZZ_STATE_UNLOADED) {
    result = registerUnloadedClazz(result);
  }

  woempa(1, "Returning %K\n", result);

  return result;
}

w_string clazz2desc(w_clazz clazz) {
  woempa(1, "Class %k\n", clazz);
  if (isSet(clazz->flags, CLAZZ_IS_PRIMITIVE)) {
    if (clazz == clazz_boolean) {
      woempa(1, "Descriptor is '%c'\n", 'Z');
      return cstring2String("Z", 1);
    }
    else if (clazz == clazz_char) {
      woempa(1, "Descriptor is '%c'\n", 'C');
      return cstring2String("C", 1);
    }
    else if (clazz == clazz_float) {
      woempa(1, "Descriptor is '%c'\n", 'F');
      return cstring2String("F", 1);
    }
    else if (clazz == clazz_double) {
      woempa(1, "Descriptor is '%c'\n", 'D');
      return cstring2String("D", 1);
    }
    else if (clazz == clazz_byte) {
      woempa(1, "Descriptor is '%c'\n", 'B');
      return cstring2String("B", 1);
    }
    else if (clazz == clazz_short) {
      woempa(1, "Descriptor is '%c'\n", 'S');
      return cstring2String("S", 1);
    }
    else if (clazz == clazz_int) {
      woempa(1, "Descriptor is '%c'\n", 'I');
      return cstring2String("I", 1);
    }
    else if (clazz == clazz_long) {
      woempa(1, "Descriptor is '%c'\n", 'J');
      return cstring2String("J", 1);
    }
    else {
      woempa(1, "Descriptor is '%c'\n", 'V');
      return cstring2String("V", 1);
    }
  }
  else if (string_char(clazz->dotified, 0) == '[') {
    w_string result = dots2slashes(clazz->dotified);

    return result;

  }
  else {
    w_size l = string_length(clazz->dotified);
    w_char *buffer = allocMem((l + 2) * sizeof(w_char));
    w_size i;
    w_string s;

    if (!buffer) {
      wabort(ABORT_WONKA, "Unable to allocate %s\n", "buffer");
    }
    buffer[0] = 'L';
    for (i = 0; i < l; ++i) {
      w_char c =  string_char(clazz->dotified, i);
      buffer[i + 1] = c == '.' ? '/' : c;
    }
    buffer[i + 1] = ';';
    s = unicode2String(buffer, l + 2);
    if (!s) {
      wabort(ABORT_WONKA, "Unable to create %s\n", "s");
    }
    releaseMem(buffer);

    return s;
  }
}

char * print_descriptor(char * buffer, int * remain, void * data, int w, int p, unsigned int f) {
  w_clazz clazz = data;
  w_int   nbytes;
  char   *temp;
  w_int   i;

  if (*remain < 1) {

    return buffer;

  }

  temp = buffer;

  if (clazz == NULL) {
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

  if (isSet(clazz->flags, CLAZZ_IS_PRIMITIVE)) {
    if (clazz == clazz_boolean) {
      *temp = 'Z';
    }
    else if (clazz == clazz_char) {
      *temp = 'C';
    }
    else if (clazz == clazz_float) {
      *temp = 'F';
    }
    else if (clazz == clazz_double) {
      *temp = 'D';
    }
    else if (clazz == clazz_byte) {
      *temp = 'B';
    }
    else if (clazz == clazz_short) {
      *temp = 'S';
    }
    else if (clazz == clazz_int) {
      *temp = 'I';
    }
    else if (clazz == clazz_long) {
      *temp = 'J';
    }
    else {
      *temp = 'V';
    }
    ++temp;
    --*remain;
  }
  else if (string_char(clazz->dotified, 0) == '[') {
    nbytes = x_snprintf(temp, *remain, "%w", clazz->dotified);
    for (i = 0; i < nbytes; ++i) {
      if (temp[i] == '.') {
        temp[i] = '/';
      }
    }
    *remain -= nbytes;
    temp += nbytes;
  }
  else {
    nbytes = x_snprintf(temp, *remain, "L%w;", clazz->dotified);
    for (i = 0; i < nbytes; ++i) {
      if (temp[i] == '.') {
        temp[i] = '/';
      }
    }
    *remain -= nbytes;
    temp += nbytes;
  }

  return temp;
}

