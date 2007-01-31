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
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/


/*
** $Id: descriptor.c,v 1.3 2004/11/30 10:08:10 cvs Exp $
*/
#include <stdio.h>
#include <string.h>

#include "clazz.h"
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

    desc_chars = alloca(string_length(descriptor) * sizeof(w_char));
    w_string2chars(descriptor, desc_chars);
    dims = 0;
    while (string_char(descriptor, *start) == '[') {
      ++dims;
      ++*start;
      if (*start >= end) {

        return NULL;

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
          wabort(ABORT_WONKA, "Unable to create temp_string\n");
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

          return NULL;

        }
        desc_string = unicode2String(&desc_chars[*start - dims - 1], k - *start + dims + 2);
        if (!desc_string) {
          wabort(ABORT_WONKA, "Unable to create desc_string\n");
        }
        *start = k + 1;
        temp_string = slashes2dots(desc_string);
        if (!temp_string) {
          wabort(ABORT_WONKA, "Unable to dotify desc\n");
        }
        result = identifyClazz(temp_string, loader);
        deregisterString(desc_string);
        break;
  
      default:
        temp_string = unicode2String(&desc_chars[*start - dims - 1], dims + 1);
        if (!temp_string) {
          wabort(ABORT_WONKA, "Unable to create temp_string\n");
        }

        return NULL;

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

          return NULL;

        }

        temp_string = unicode2String(&desc_chars[*start], k - *start);
        if (!temp_string) {
          wabort(ABORT_WONKA, "Unable to create temp_string\n");
        }
        dotified = slashes2dots(temp_string);
        if (!dotified) {
          wabort(ABORT_WONKA, "Unable to dotify desc\n");
        }
        result = identifyClazz(dotified, loader);
        deregisterString(dotified);
        deregisterString(temp_string);
        *start = k + 1;
        break;

      default:
        woempa(9, "Illegal char %c\n", string_char(descriptor, *start-1));

        return NULL;

      }
    }

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
      woempa(1, "Descriptor is 'Z'\n");
      return cstring2String("Z", 1);
    }
    else if (clazz == clazz_char) {
      woempa(1, "Descriptor is 'C'\n");
      return cstring2String("C", 1);
    }
    else if (clazz == clazz_float) {
      woempa(1, "Descriptor is 'F'\n");
      return cstring2String("F", 1);
    }
    else if (clazz == clazz_double) {
      woempa(1, "Descriptor is 'D'\n");
      return cstring2String("D", 1);
    }
    else if (clazz == clazz_byte) {
      woempa(1, "Descriptor is 'B'\n");
      return cstring2String("B", 1);
    }
    else if (clazz == clazz_short) {
      woempa(1, "Descriptor is 'D'\n");
      return cstring2String("S", 1);
    }
    else if (clazz == clazz_int) {
      woempa(1, "Descriptor is 'I'\n");
      return cstring2String("I", 1);
    }
    else if (clazz == clazz_long) {
      woempa(1, "Descriptor is 'J'\n");
      return cstring2String("J", 1);
    }
    else {
      woempa(1, "Descriptor is 'V'\n");
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
      wabort(ABORT_WONKA, "Unable to allocate buffer\n");
    }
    buffer[0] = 'L';
    for (i = 0; i < l; ++i) {
      w_char c =  string_char(clazz->dotified, i);
      buffer[i + 1] = c == '.' ? '/' : c;
    }
    buffer[i + 1] = ';';
    s = unicode2String(buffer, l + 2);
    if (!s) {
      wabort(ABORT_WONKA, "Unable to create s\n");
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

