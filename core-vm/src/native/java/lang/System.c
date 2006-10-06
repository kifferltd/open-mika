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
*   Philips Site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004, 2005, 2006 by Chris Gray,             *
* /k/ Embedded Java Solutions. All rights reserved.                       *
*                                                                         *
**************************************************************************/

/*
** $Id: System.c,v 1.10 2006/10/04 14:24:16 cvsroot Exp $
*/

#include <string.h>
#include <unistd.h>

#include "arrays.h"
#include "clazz.h"
#include "core-classes.h"
#include "descriptor.h"
#include "exception.h"
#include "environment.h"
#include "fastcall.h"
#include "hashtable.h"
#include "heap.h"
#include "loading.h"
#include "checks.h"
#include "ts-mem.h"
#include "oswald.h"
#include "wstrings.h"
#include "threads.h"
#include "wonkatime.h"

w_hashtable prop_hashtable;

w_long System_static_currentTimeMillis(JNIEnv *env, w_instance classSystem) {
  replaceByFastCall(env, FAST_SYSTEM_CURRENTTIMEMILLIS);
  return (w_long)getNativeSystemTime();
}

void fast_System_static_currentTimeMillis(w_frame frame) {
  // Volatile 'coz gcc might screw up otherwise
  volatile union {w_long l; w_word w[2];} millis;

  millis.l = getNativeSystemTime();
  frame->jstack_top[0].s = 0;
  frame->jstack_top[0].c = millis.w[0];
  frame->jstack_top[1].s = 0;
  frame->jstack_top[1].c = millis.w[1];
  frame->jstack_top += 2;
  woempa(1, "returning 0x%08x 0x%08x\n", frame->jstack_top[-2].c, frame->jstack_top[-1].c);
}

void System_static_initNativeProperties(JNIEnv *env, w_instance classSystem) {
  char *utf8;
  w_string s;

  prop_hashtable = ht_create("hashtable:native system properties", 17, ht_stringHash, ht_stringCompare, 0, 0);
  woempa(1, "Created prop_hashtable\n");
  s = cstring2String(UNICODE_SUBSETS, strlen(UNICODE_SUBSETS));
  ht_write(prop_hashtable, (w_word)cstring2String("mika.unicode.subsets", 20), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.unicode.subsets", s);

  utf8 = getInstallationDir();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("java.home", 9), (w_word)s);
  woempa(1, "Set %s -> %w\n", "java.home", s);

  utf8 = getExtensionDir();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("java.ext.dirs", 13), (w_word)s);
  woempa(1, "Set %s -> %w\n", "java.ext.dirs", s);

  utf8 = getLibraryPath();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("java.library.path", 13), (w_word)s);
  woempa(1, "Set %s -> %w\n", "java.library.path", s);

  s = utf2String(VERSION_STRING, strlen(VERSION_STRING));
  ht_write(prop_hashtable, (w_word)utf2String("mika.version", 12), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.version", s);

  s = utf2String(WONKA_INFO, strlen(WONKA_INFO));
  ht_write(prop_hashtable, (w_word)utf2String("mika.vm.options", 15), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.vm.options", s);

  s = utf2String(DEFAULT_HEAP_SIZE, strlen(DEFAULT_HEAP_SIZE));
  ht_write(prop_hashtable, (w_word)utf2String("mika.default.heap.size", 22), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.default.heap.size", s);

  s = utf2String(AWT_INFO, strlen(AWT_INFO));
  ht_write(prop_hashtable, (w_word)utf2String("mika.awt.options", 16), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.awt.options", s);

#ifdef O4P
  s = utf2String(O4P_INFO, strlen(O4P_INFO));
  ht_write(prop_hashtable, (w_word)utf2String("mika.o4p.options", 16), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.o4p.options", s);
#endif

#ifdef OSWALD
  s = utf2String(OSWALD_INFO, strlen(OSWALD_INFO));
  ht_write(prop_hashtable, (w_word)utf2String("mika.oswald.options", 19), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.oswald.options", s);
#endif

  s = utf2String(BUILD_HOST, strlen(BUILD_HOST));
  ht_write(prop_hashtable, (w_word)utf2String("mika.build.host", 15), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.build.host", s);

  utf8 = __DATE__ " " __TIME__;
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("mika.build.time", 15), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.build.time", s);

  utf8 = getOSName();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("os.name", 7), (w_word)s);
  woempa(1, "Set %s -> %w\n", "os.name", s);

  utf8 = getOSVersion();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("os.version", 10), (w_word)s);
  woempa(1, "Set %s -> %w\n", "os.version", s);

  utf8 = getOSArch();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("os.arch", 7), (w_word)s);
  woempa(1, "Set %s -> %w\n", "os.arch", s);

  utf8 = getUserName();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("user.name", 9), (w_word)s);
  woempa(1, "Set %s -> %w\n", "user.name", s);

  utf8 = getUserHome();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("user.home", 9), (w_word)s);
  woempa(1, "Set %s -> %w\n", "user.home", s);

  utf8 = getUserDir();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("user.dir", 8), (w_word)s);
  woempa(1, "Set %s -> %w\n", "user.dir", s);

  utf8 = getUserLanguage();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("user.language", 8), (w_word)s);
  woempa(1, "Set %s -> %w\n", "user.language", s);
}

void System_static_termNativeProperties(JNIEnv *env, w_instance classSystem) {
  ht_destroy(prop_hashtable);
  prop_hashtable = NULL;
}

w_instance System_static_getNativeProperty(JNIEnv *env, w_instance classSystem, w_instance nameString) {
  w_string name = String2string(nameString);
  w_string result;

  result = (w_string)ht_read(prop_hashtable, (w_word)name);

  return result ? getStringInstance(result) : NULL;
}

static void do_boolean_arraycopy(w_instance Src, w_int srco, w_instance Dst, w_int dsto, w_int length) {

  w_byte * src;
  w_int    srcMask;
  w_byte * dst;
  w_int    dstMask;
  w_int    i;

  if (srco > dsto) {
    src = ((w_byte *)(Src + F_Array_data)) + srco / 8;
    dst = ((w_byte *)(Dst + F_Array_data)) + dsto / 8;
    srcMask = 1 << (srco % 8);
    dstMask = 1 << (dsto % 8);

    for (i = 0; i < length; i++) {
      if (*src & srcMask) {
        *dst |= dstMask;
      }
      else {
        *dst &= 0xffffffff ^ dstMask;
      }

      srcMask <<= 1;
      if (srcMask == 0x100) {
        src++;
        srcMask = 1;
      }

      dstMask <<= 1;
      if (dstMask == 0x100) {
        dst++;
        dstMask = 1;
      }
    }
  }
  else {
    length -= 1;
    src = ((w_byte *)(Src + F_Array_data)) + (srco + length) / 8;
    dst = ((w_byte *)(Dst + F_Array_data)) + (dsto + length) / 8;
    srcMask = 1 << ((srco + length) % 8);
    dstMask = 1 << ((dsto + length) % 8);

    for (i = 0; i <= length; i++) {
      if (*src & srcMask) {
        *dst |= dstMask;
      }
      else {
        *dst &= 0xffffffff ^ dstMask;
      }

      srcMask >>= 1;
      if (srcMask == 0 ) {
        src--;
        srcMask = 0x80;
      }

      dstMask >>= 1;
      if (dstMask == 0) {
        dst--;
        dstMask = 0x80;
      }
    }
  }

}

static inline void do_byte_arraycopy(w_instance Src, w_int srco, w_instance Dst, w_int dsto, w_int length) {

  w_ubyte * src = ((w_byte *)(Src + F_Array_data)) + srco;
  w_ubyte * dst = ((w_byte *)(Dst + F_Array_data)) + dsto;
  /* [CG 20050620] Dunno about all this Duff stuff, what say we just do a memcpy or a memmove?
  w_int duffs = (length + 15) / 16;

  if (src < dst && dst < src + length) {
    dst += length;
    src += length;
    switch (length & 0x0f) {
      case  0: do { *--dst = *--src;
      case 15:      *--dst = *--src;
      case 14:      *--dst = *--src;
      case 13:      *--dst = *--src;
      case 12:      *--dst = *--src;
      case 11:      *--dst = *--src;
      case 10:      *--dst = *--src;
      case  9:      *--dst = *--src;
      case  8:      *--dst = *--src;
      case  7:      *--dst = *--src;
      case  6:      *--dst = *--src;
      case  5:      *--dst = *--src;
      case  4:      *--dst = *--src;
      case  3:      *--dst = *--src;
      case  2:      *--dst = *--src;
      case  1:      *--dst = *--src;
              } while (--duffs > 0);
    }
  }
  else {
    switch (length & 0x0f) {
      case  0: do { *dst++ = *src++;
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
  }
  */
  if (Src == Dst) {
    memmove(dst, src, length);
  }
  else {
    w_memcpy(dst, src, length);
  }
}

static void do_16bits_arraycopy(w_instance Src, w_int srco, w_instance Dst, w_int dsto, w_int length) {

  w_short * src = ((w_short *)(Src + F_Array_data)) + srco;
  w_short * dst = ((w_short *)(Dst + F_Array_data)) + dsto;
  w_int duffs = (length + 15) / 16;

  if (src < dst && dst < src + length) {
    dst += length;
    src += length;
    switch (length & 0x0f) {
      case  0: do { *--dst = *--src;
      case 15:      *--dst = *--src;
      case 14:      *--dst = *--src;
      case 13:      *--dst = *--src;
      case 12:      *--dst = *--src;
      case 11:      *--dst = *--src;
      case 10:      *--dst = *--src;
      case  9:      *--dst = *--src;
      case  8:      *--dst = *--src;
      case  7:      *--dst = *--src;
      case  6:      *--dst = *--src;
      case  5:      *--dst = *--src;
      case  4:      *--dst = *--src;
      case  3:      *--dst = *--src;
      case  2:      *--dst = *--src;
      case  1:      *--dst = *--src;
              } while (--duffs > 0);
    }
  }
  else {
    switch (length & 0x0f) {
      case  0: do { *dst++ = *src++;
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
  }

}

static void do_32bits_arraycopy(w_instance Src, w_int srco, w_instance Dst, w_int dsto, w_int length) {

  w_word * src = ((w_word *)(Src + F_Array_data)) + srco;
  w_word * dst = ((w_word *)(Dst + F_Array_data)) + dsto;
  w_int duffs = (length + 15) / 16;

  if (src < dst && dst < src + length) {
    dst += length;
    src += length;
    switch (length & 0x0f) {
      case  0: do { *--dst = *--src;
      case 15:      *--dst = *--src;
      case 14:      *--dst = *--src;
      case 13:      *--dst = *--src;
      case 12:      *--dst = *--src;
      case 11:      *--dst = *--src;
      case 10:      *--dst = *--src;
      case  9:      *--dst = *--src;
      case  8:      *--dst = *--src;
      case  7:      *--dst = *--src;
      case  6:      *--dst = *--src;
      case  5:      *--dst = *--src;
      case  4:      *--dst = *--src;
      case  3:      *--dst = *--src;
      case  2:      *--dst = *--src;
      case  1:      *--dst = *--src;
              } while (--duffs > 0);
    }
  }
  else {
    switch (length & 0x0f) {
      case  0: do { *dst++ = *src++;
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
  }

}

static void do_64bits_arraycopy(w_instance Src, w_int srco, w_instance Dst, w_int dsto, w_int length) {
  do_32bits_arraycopy(Src, srco, Dst, dsto, length * 2);
}

/*
** A checked assignment of references; return true if there was a problem...
*/

static inline w_boolean do_checked_reference_arraycopy(w_instance Src, w_int srco, w_instance Dst, w_int dsto, w_int length) {

  w_instance * src = ((w_instance *)(Src + F_Array_data)) + srco;
  w_instance * dst = ((w_instance *)(Dst + F_Array_data)) + dsto;
  w_clazz      dstComponent = instance2clazz(Dst)->previousDimension;
  w_int        i;

  if (src < dst && dst < src + length) {
    for (i = length - 1; i >= 0; i--) {
      if (src[i] == NULL || isAssignmentCompatible(instance2clazz(src[i]), dstComponent)) {
        setArrayReferenceField(Dst, src[i], dsto + i);
      }
      else {
        return TRUE;
      }
    }
  }
  else {
    for (i = 0; i < length; i++) {
      if (src[i] == NULL || isAssignmentCompatible(instance2clazz(src[i]), dstComponent)) {
        setArrayReferenceField(Dst, src[i], dsto + i);
      }
      else {
        return TRUE;
      }
    }
  }

  return FALSE;

}

/*
 * [ORIGINAL COMMENT]
** In the reference copying, we can safely ignore setArrayReferenceField since the references
** are protected allready by the source array.
** [CG 20050620] Not so, my friend. This code is also used when the source array
** and the destination array are one and the same, and in that case we have a
** real risk of a race condition (imagine that the array is being marked by
** another thread while we are busy shuffling data,
** Therefore I have switched to using a memcpy/memmove, wrapped in an "unsafe" 
** context.
*/

static inline void do_unchecked_reference_arraycopy(w_instance Src, w_int srco, w_instance Dst, w_int dsto, w_int length) {

  w_instance * src = ((w_instance *)(Src + F_Array_data)) + srco;
  w_instance * dst = ((w_instance *)(Dst + F_Array_data)) + dsto;
  /* WAS:
  w_int duffs = (length + 15) / 16;

  if (src < dst && dst < src + length) {
    dst += length;
    src += length;
    switch (length & 0x0f) {
      case  0: do { *--dst = *--src;
      case 15:      *--dst = *--src;
      case 14:      *--dst = *--src;
      case 13:      *--dst = *--src;
      case 12:      *--dst = *--src;
      case 11:      *--dst = *--src;
      case 10:      *--dst = *--src;
      case  9:      *--dst = *--src;
      case  8:      *--dst = *--src;
      case  7:      *--dst = *--src;
      case  6:      *--dst = *--src;
      case  5:      *--dst = *--src;
      case  4:      *--dst = *--src;
      case  3:      *--dst = *--src;
      case  2:      *--dst = *--src;
      case  1:      *--dst = *--src;
              } while (--duffs > 0);
    }
  }
  else {
    switch (length & 0x0f) {
      case  0: do { *dst++ = *src++;
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
  }
  */

  w_thread thread = currentWonkaThread;
  w_boolean unsafe = enterUnsafeRegion(thread);
  if (Src == Dst) {
    memmove(dst, src, length * sizeof(w_instance));
  }
  else {
    memcpy(dst, src, length * sizeof(w_instance));
  }
  if (!unsafe) {
    enterSafeRegion(thread);
  }
}

void System_static_arraycopy(JNIEnv *env, w_instance Class, w_instance Src, w_int srco, w_instance Dst, w_int dsto, w_int length) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz   srcClazz;
  w_clazz   dstClazz;
  w_clazz   srcCom;
  w_clazz   dstCom;
  w_int     srcLength;
  w_int     dstLength;
  w_boolean problem = FALSE;

  /*
  ** Do the required checks...
  */
  
  if (Src == NULL || Dst == NULL) {
    throwException(thread, clazzNullPointerException, NULL);
    return;
  }

  srcClazz = instance2clazz(Src);
  dstClazz = instance2clazz(Dst);

  if (!srcClazz->dims || !dstClazz->dims) {
    throwException(thread, clazzArrayStoreException, NULL);
    return;
  }

  srcCom = srcClazz->previousDimension;
  dstCom = dstClazz->previousDimension;

  if (clazzIsPrimitive(srcCom) && clazzIsPrimitive(dstCom) && srcCom != dstCom) {
    problem = TRUE;
  }
  else if (clazzIsPrimitive(srcCom) && !clazzIsPrimitive(dstCom)) {
    problem = TRUE;
  }
  else if (!clazzIsPrimitive(srcCom) && clazzIsPrimitive(dstCom)) {
    problem = TRUE;
  }

  if (problem) {
    throwException(thread, clazzArrayStoreException, NULL);
    return;
  }

  srcLength = instance2Array_length(Src);
  dstLength = instance2Array_length(Dst);

  if (srco < 0 || dsto < 0 || length < 0 || srco > srcLength - length || dsto > dstLength - length) {
    throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    return;
  }

  /*
  ** Eliminate trivial cases.
  */

  if (length == 0 || ((Src == Dst) && (srco == dsto))) {
    return;
  }

  /*
  ** Call the appropriate routine, depending on destination and source type. If any assignment
  ** conversion is required, it is done in special routines.
  */

  if (clazzIsPrimitive(srcCom)) {
    switch (srcCom->type & 0x0f) {
      case VM_TYPE_BOOLEAN: {
        do_boolean_arraycopy(Src, srco, Dst, dsto, length);
        break;
      }
      
      case VM_TYPE_CHAR: {
        do_16bits_arraycopy(Src, srco, Dst, dsto, length);
        break;
      }

      case VM_TYPE_SHORT: {
        do_16bits_arraycopy(Src, srco, Dst, dsto, length);
        break;
      }
      
      case VM_TYPE_FLOAT: {
        do_32bits_arraycopy(Src, srco, Dst, dsto, length);
        break;
      }
      
      case VM_TYPE_INT: {
        do_32bits_arraycopy(Src, srco, Dst, dsto, length);
        break;
      }
      
      case VM_TYPE_DOUBLE: {
        do_64bits_arraycopy(Src, srco, Dst, dsto, length);
        break;
      }
      
      case VM_TYPE_LONG: {
        do_64bits_arraycopy(Src, srco, Dst, dsto, length);
        break;
      }
      
      case VM_TYPE_BYTE: {
        do_byte_arraycopy(Src, srco, Dst, dsto, length);
        break;
      }
      
      default: break;
      
    }
  }
  else {
    if (dstClazz == srcClazz || dstClazz == clazzArrayOf_Object) {
      do_unchecked_reference_arraycopy(Src, srco, Dst, dsto, length);
    }
    else {
      if (do_checked_reference_arraycopy(Src, srco, Dst, dsto, length)) {
        throwException(thread, clazzArrayStoreException, NULL);
      }
    }
  }

}

w_int System_identityHashCode(JNIEnv *env, jclass clazz, w_instance obj) {

  if (obj == NULL) {
    return 0;
  }

  return Object_hashCode(env, obj);

}

extern Wonka_InitArgs *system_vm_args;

w_instance System_getCmdLineProperties(JNIEnv *env, w_instance this) {

  w_thread    thread = JNIEnv2w_thread(env);
  w_int       i;
  w_instance  Array = NULL;
  w_instance  String;
  w_string    string;
  w_int       length;

  for (length = 0; length < 100; length++) {
    if (system_vm_args->properties[length] == NULL) {
      break;
    }
  }

  if (length > 0) {

    Array = allocArrayInstance_1d(thread, clazzArrayOf_String, length);

    if (Array) {
      for (i = 0; i < length; i++) {
        String = allocInstance(thread, clazzString);
        if (String) {
          string = cstring2String(system_vm_args->properties[i], strlen(system_vm_args->properties[i]));
          setWotsitField(String, F_String_wotsit, string);
        } 
        setArrayReferenceField(Array, String, i);
      }
    }
  }

  return Array;

}

