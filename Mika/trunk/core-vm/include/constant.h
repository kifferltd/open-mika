#ifndef _CONSTANT_H
#define _CONSTANT_H

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
**************************************************************************/

/*
** $Id: constant.h,v 1.9 2006/06/16 19:22:54 cvs Exp $
*/

#include "exception.h"
#include "wonka.h"

/*
** For more info on the structure of the constant pool, refer
** to any book on the classfile structure.
*/

#define CONSTANT_UTF8               0x01
#define CONSTANT_UNICODE            0x02
#define CONSTANT_INTEGER            0x03
#define CONSTANT_FLOAT              0x04
#define CONSTANT_LONG               0x05
#define CONSTANT_DOUBLE             0x06
#define CONSTANT_CLASS              0x07
#define CONSTANT_STRING             0x08
#define CONSTANT_FIELD              0x09
#define CONSTANT_METHOD             0x0A
#define CONSTANT_IMETHOD            0x0B
#define CONSTANT_NAME_AND_TYPE      0x0C
#define NO_VALID_ENTRY              0x0E
#define COULD_NOT_RESOLVE           0x0F

#define UNRESOLVED_CONSTANT         0x00
#define RESOLVING_CONSTANT          0x10
#define RESOLVED_CONSTANT           0x20
#define CONSTANT_STATE_MASK         0xf0

#define CONSTANT_STATE(c)           ((c) & CONSTANT_STATE_MASK)
#define RESOLVING_CLASS             (CONSTANT_CLASS+RESOLVING_CONSTANT)
#define RESOLVING_STRING            (CONSTANT_STRING+RESOLVING_CONSTANT)
#define RESOLVING_FIELD             (CONSTANT_FIELD+RESOLVING_CONSTANT)
#define RESOLVING_METHOD            (CONSTANT_METHOD+RESOLVING_CONSTANT)
#define RESOLVING_IMETHOD           (CONSTANT_IMETHOD+RESOLVING_CONSTANT)

#define RESOLVED_CLASS              (CONSTANT_CLASS+RESOLVED_CONSTANT)
#define RESOLVED_STRING             (CONSTANT_STRING+RESOLVED_CONSTANT)
#define RESOLVED_FIELD              (CONSTANT_FIELD+RESOLVED_CONSTANT)
#define RESOLVED_METHOD             (CONSTANT_METHOD+RESOLVED_CONSTANT)
#define RESOLVED_IMETHOD            (CONSTANT_IMETHOD+RESOLVED_CONSTANT)

#define DIRECT_POINTER              0x2d

#define CONSTANT_DELETED            0xff

#define isUtf8Constant(c,i) (((c)->tags[i] & 0x0f) == CONSTANT_UTF8)
#define isUnicodeConstant(c,i) (((c)->tags[i] & 0x0f) == CONSTANT_UNICODE)
#define isIntegerConstant(c,i) (((c)->tags[i] & 0x0f) == CONSTANT_INTEGER)
#define isFloatConstant(c,i) (((c)->tags[i] & 0x0f) == CONSTANT_FLOAT)
#define isLongConstant(c,i) (((c)->tags[i] & 0x0f) == CONSTANT_LONG)
#define isDoubleConstant(c,i) (((c)->tags[i] & 0x0f) == CONSTANT_DOUBLE)
#define isClassConstant(c,i) (((c)->tags[i] & 0x0f) == CONSTANT_CLASS)
#define isStringConstant(c,i) (((c)->tags[i] & 0x0f) == CONSTANT_STRING)
#define isFieldConstant(c,i) (((c)->tags[i] & 0x0f) == CONSTANT_FIELD)
#define isMethodConstant(c,i) (((c)->tags[i] & 0x0f) == CONSTANT_METHOD)
#define isIMethodConstant(c,i) (((c)->tags[i] & 0x0f) == CONSTANT_IMETHOD)
#define isNatConstant(c,i) (((c)->tags[i] & 0x0f) == CONSTANT_NAME_AND_TYPE)
#define is2ndHalfConstant(c,i) (((c)->tags[i] & 0x0f) == NO_VALID_ENTRY)

/*
 * A Member (Field/Method/IMethod) constant consists of two 16-bit unsigned 
 * integers packed into a 32-bit word:
 * 1) the index of a class constant, which resolves to the class where the
 *    member should be defimed (may or not be the current class);
 * 2) the index of a name-and-type constant, see below.
 * Regardless of endianness, we store the class index in the less significant
 * half of the word and the name-and-type index in the upper half.
*/

static inline u2 Member_get_class_index(u4 nt) {
  return (u2)(nt >> 16);
}

static inline u2 Member_get_nat_index(u4 nt) {
  return (u2)(nt & 0xffff);
}

static inline u4 Member_pack(u2 class_index, u2 nat_index) {
  return ((class_index << 16) | nat_index);
}

/*
 * A Name-and-Type constant consists of two 16-bit unsigned integers packed
 * into a 32-bit word:
 * 1) the index of a UTF8 constant, which resolves to the member's name;
 * 2) the index of a UTF8 constant, which resolves to the member's type descriptor.
 * Regardless of endianness, we store the name index in the more significant
 * half of the word and the type index in the lower half.
*/

static inline u2 Name_and_Type_get_name_index(u4 nt) {
  return (u2)(nt >> 16);
}

static inline u2 Name_and_Type_get_type_index(u4 nt) {
  return (u2)(nt & 0xffff);
}

static inline u4 Name_and_Type_pack(u2 name_index, u2 type_index) {
  return ((name_index << 16) | type_index);
}

/*
** A constant's 'tag' indicates the type of the constant: one of the
** CONSTANT_xxxx values defined above, or NO_VALID_ENTRY for the index
** value which is skipped after a CONSTANT_LONG or CONTANT_DOUBLE, 
** or COULD_NOT_RESOLVE for a constant which we already tried unsuccessfully
** to resolve (so we should not try again).
**
** The value of the tag determines the type of data stored in the corresponding
** entry of clazz->values:
**     CONSTANT_UTF8                     w_string
**     CONSTANT_UNICODE                  (not used)
**     CONSTANT_INTEGER                  w_int value
**     CONSTANT_FLOAT                    w_float value
**     CONSTANT_LONG                     w_long value
**     CONSTANT_DOUBLE                   w_double value
**     CONSTANT_CLASS                    index of a CONSTANT_UTF8
**     RESOLVED_CLASS                    w_clazz                  
**     CONSTANT_STRING                   index of a CONSTANT_UTF8
**     RESOLVED_STRING                   w_instance of java.lang.String
**     CONSTANT_FIELD                    indices of a CONSTANT_CLASS (16 bits),
**                                       CONSTANT_NAME_AND_TYPE (16 bits).
**     RESOLVED_FIELD                    w_field
**     CONSTANT_METHOD                   indices of a CONSTANT_CLASS (16 bits),
**                                       CONSTANT_NAME_AND_TYPE (16 bits).
**     RESOLVED_METHOD                   w_method
**     CONSTANT_IMETHOD                  indices of a CONSTANT_CLASS (16 bits),
**                                       CONSTANT_NAME_AND_TYPE (16 bits).
**     RESOLVED_IMETHOD                  w_method
**     CONSTANT_NAME_AND_TYPE            indices of two CONSTANT_UTF8's.
**     NO_VALID_ENTRY                    (second word of w_long/w_double)
**     COULD_NOT_RESOLVE                 w_instance of a subclass of Throwable.
**     DIRECT_POINTER                    Direct pointer to a static variable.
**   
** [CG 20031116]
** With the exception of CONSTANT_FIELD, CONSTANT_METHOD, CONSTANT_IMETHOD,
** and NAME_AND_TYPE (see explanation of Member_xxxx and Name_and_Type_xxxx
** above, the data in clazz->values[] is always stored in machine order, which 
** differs from the order in the class file when the cpu is little-endian.
*/

/*
** Function resolveUtf8Constant returns the result of resolving the i'th
** constant of clazz, which must be a UTF* constant.
*/
w_string resolveUtf8Constant(w_clazz, w_int idx);

/*
** Function resolveStringConstant resolves the i'th constant of clazz (which
** must be a String constant).
*/
void resolveStringConstant(w_clazz clazz, w_int i);

/*
** Function resolveClassConstant resolves the i'th constant of clazz (which
** must be a Class constant).
*/
void resolveClassConstant(w_clazz clazz, w_int i);

/*
** Function resolveFieldConstant resolves the i'th constant of clazz (which
** must be a Field constant).
*/
void resolveFieldConstant(w_clazz clazz, w_int i);

/*
** Function resolveMethodConstant resolves the i'th constant of clazz (which
** must be a Method constant).
*/
void resolveMethodConstant(w_clazz clazz, w_int i);

/*
** Function resolveIMethodConstant resolves the i'th constant of clazz (which
** must be a IMethod constant).
*/
void resolveIMethodConstant(w_clazz clazz, w_int i);

/*
** The functions getXXXXConstant return the value of the i'th constant of `clazz'.
*/

static inline w_int getIntegerConstant(w_clazz clazz, w_int i) {
  return (w_int)clazz->values[i];
}

static inline w_float getFloatConstant(w_clazz clazz, w_int i) {
  return (w_float)clazz->values[i];
}

static inline w_u64 getLongConstant(w_clazz clazz, w_int i) {
  w_u64 l;

  memcpy(&l, (u4*)&clazz->values[i], 8);

  return l;
}

static inline w_u64 getDoubleConstant(w_clazz clazz, w_int i) {
  w_u64 d;

  memcpy(&d, (u4*)&clazz->values[i], 8);

  return d;
}

/*
** Get the value of a STRING constant, resolving it if necessary.
*/
static inline w_instance getStringConstant(w_clazz clazz, w_int i) {
  if (clazz->tags[i] < RESOLVED_CONSTANT) {
    resolveStringConstant(clazz, i);
  }

  return (w_instance)clazz->values[i];
}

/*
** Get the value of a STRING constant which is known to already be resolved.
*/
static inline w_instance getResolvedStringConstant(w_clazz clazz, w_int i) {
  return (w_instance)clazz->values[i];
}

/*
 * Get the value of a CLASS constant, resolving it if need be.
 * The calling thread must GC safe!
 */
w_clazz getClassConstant(w_clazz clazz, w_int idx);

/**
 * Same as getClassConstant, but the calling thread is GC-unsafe on entry and exit.
 * Since this function may call enterSafeRegion, the thread's GC affairs must
 * already be in order before calling this function.
 */
w_clazz getClassConstant_unsafe(w_clazz clazz, w_int idx, w_thread thread);

/*
** Get the value of a CLASS constant which is known to already be resolved.
*/
static inline w_clazz getResolvedClassConstant(w_clazz clazz, w_int i) {
  return (w_clazz)clazz->values[i];
}

/*
 * Get the value of a FIELD constant, resolving it if need be.
 * The calling thread must GC safe!
 */
w_field getFieldConstant(w_clazz clazz, w_int idx);

/*
** Get the value of a FIELD constant which is known to already be resolved.
*/
static inline w_field getResolvedFieldConstant(w_clazz clazz, w_int i) {
  return (w_field)clazz->values[i];
}

/*
 * Get the value of a METHOD constant, resolving it if need be.
 * The calling thread must GC safe!
 */
w_method getMethodConstant(w_clazz clazz, w_int idx);

/*
** Get the value of a METHOD constant which is known to already be resolved.
*/
static inline w_method getResolvedMethodConstant(w_clazz clazz, w_int i) {
  return (w_method)clazz->values[i];
}

/*
 * Get the value of an IMETHOD constant, resolving it if need be.
 * The calling thread must GC safe!
 */
w_method getIMethodConstant(w_clazz clazz, w_int idx);

/*
** Get the value of an IMETHOD constant which is known to already be resolved.
*/
static inline w_method getResolvedIMethodConstant(w_clazz clazz, w_int i) {
  return (w_method)clazz->values[i];
}

void dissolveConstant(w_clazz, int idx);

void dumpPools(int fd, w_clazz clazz);

void checkUTF8References(w_clazz clazz, w_int idx);

/**
 ** Add a UTF8 constant to the pool (or find an identical existing
 ** one), returning theindex of the result.
 */
w_int addUTF8ConstantToPool(w_clazz, w_string);

/**
 ** Add an unresolved class constant to the pool (or find an identical existing
 ** one), returning theindex of the result.
 */
w_int addUnresolvedClassConstantToPool(w_clazz, w_size);

/**
 ** Add a name & type constant to the pool (or find an identical existing
 ** one), returning theindex of the result.
 */
w_int addNatConstantToPool(w_clazz, w_string name, w_string type);

/**
 ** Add a resolved field constant to the pool (or find an identical existing
 ** one), returning theindex of the result.
 */
w_int addResolvedFieldConstantToPool(w_clazz, w_field field);

/**
 ** Add a direct pointer constant to the pool (or find an identical existing
 ** one), returning theindex of the result.
 */
w_int addPointerConstantToPool(w_clazz, void *ptr);

#endif /* _CONSTANT_H */
