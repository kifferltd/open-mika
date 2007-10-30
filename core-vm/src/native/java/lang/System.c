/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
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
#include <unistd.h>

#include "arrays.h"
#include "clazz.h"
#include "core-classes.h"
#include "descriptor.h"
#include "exception.h"
#include "environment.h"
#include "fastcall.h"
#include "heap.h"
#include "loading.h"
#include "checks.h"
#include "ts-mem.h"
#include "oswald.h"
#include "wstrings.h"
#include "threads.h"
#include "wonkatime.h"

w_long System_static_currentTimeMillis(JNIEnv *env, w_instance classSystem) {
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
        String = allocInstance_initialized(thread, clazzString);
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

