/**************************************************************************
* Copyright (c) 2006, 2007, 2008, 2010, 2011, 2012, 2020, 2021, 2022      *
* by KIFFER Ltd. All rights reserved.                                     *
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

#ifndef _CONSTANT_H
#define _CONSTANT_H

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

#define CONSTANT_TYPE_MASK          0x1F

#define UNRESOLVED_CONSTANT         0x00
#define RESOLVING_CONSTANT          0x20
#define RESOLVED_CONSTANT           0x40
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

#define DIRECT_POINTER              0x2D
#define COULD_NOT_RESOLVE           0x1F

/* [CG 20071014] Constants are no longer being deleted
//#define CONSTANT_DELETED            0xff
*/

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

/**
 * Get the value of any 32-bit constant, resolving it if need be.
 * This can be used for both scalar 32-bit types and reference types.
 * The calling thread must be GC safe!
 * @param clazz the clazz whose contant pool is to be used
 * @param i    the index into the constant pool
 * @param type the expected type of the constant (0 means any type is OK)
 * @param thread the current thread
 * @return the resolved constant value, as a u4 (u_int32_t).
 */
u4 get32BitConstant(w_clazz clazz, w_int i, w_int type, w_thread thread);

/**
 * Get the value of any 64-bit constant, resolving it if need be.
 * The calling thread must be GC safe!
 * @param clazz the clazz whose contant pool is to be used
 * @param i    the index into the constant pool
 * @param type the expected type of the constant (0 means any type is OK)
 * @param thread the current thread
 * @return the resolved constant value, as a w_u64 (u_int64_t).
 */
w_u64 get64BitConstant(w_clazz clazz, w_int i, w_int type, w_thread thread);

/*
** The functions getXXXXConstant return the value of the i'th constant of `clazz'.
*/
#define getIntegerConstant(clazz,i) ( (w_int)(clazz)->values[i] )
#define getFloatConstant(clazz,i) ( (w_float)(clazz)->values[i] )

/**
 * Get the value of an LONG constant.
 * @param clazz the clazz whose contant pool is to be used
 * @param i    the index into the constant pool
 * @return the resolved constant value, as a w_u64 (u_int64_t).
 */
static inline w_u64 getLongConstant(w_clazz clazz, w_int i) {
  w_u64 l;

  memcpy(&l, (u4*)&clazz->values[i], 8);

  return l;
}

/**
 * Get the value of an DOUBLE constant.
 * @param clazz the clazz whose contant pool is to be used
 * @param i    the index into the constant pool
 * @return the resolved constant value, as a w_u64 (u_int64_t).
 */
static inline w_u64 getDoubleConstant(w_clazz clazz, w_int i) {
  w_u64 d;

  memcpy(&d, (u4*)&clazz->values[i], 8);

  return d;
}

/**
** Get the value of a STRING constant, resolving it if necessary.
 * @param clazz the clazz whose contant pool is to be used
 * @param i    the index into the constant pool
 * @return the resolved constant value, as a w_u64 (u_int64_t).
 */
static inline w_instance getStringConstant(w_clazz clazz, w_int i) {
  if (clazz->tags[i] < RESOLVED_CONSTANT) {
    resolveStringConstant(clazz, i);
  }

  return (w_instance)clazz->values[i];
}

/**
** Get the value of a STRING constant which is known to already be resolved.
 * @param clazz the clazz whose contant pool is to be used
 * @param i    the index into the constant pool
 * @return the constant value.
 */
#define getResolvedStringConstant(clazz,i) ( (w_instance)(clazz)->values[i] )

/**
 * Get the value of a CLASS constant, resolving it if need be.
 * The calling thread must GC safe!
 * @param clazz the clazz whose contant pool is to be used
 * @param i    the index into the constant pool
 * @return the constant value.
 */
w_clazz getClassConstant(w_clazz clazz, w_int idx, w_thread thread);

/**
** Get the value of a CLASS constant which is known to already be resolved.
 * @param clazz the clazz whose contant pool is to be used
 * @param i    the index into the constant pool
 * @return the constant value.
*/
#define getResolvedClassConstant(clazz,i) ( (w_clazz)(clazz)->values[i] )

/**
 * Get the value of a FIELD constant, resolving it if need be.
 * The calling thread must GC safe!
 * @param clazz the clazz whose contant pool is to be used
 * @param i    the index into the constant pool
 * @return the constant value.
 */
w_field getFieldConstant(w_clazz clazz, w_int idx);

/**
** Get the value of a FIELD constant which is known to already be resolved.
 * @param clazz the clazz whose contant pool is to be used
 * @param i    the index into the constant pool
 * @return the constant value.
*/
#define getResolvedFieldConstant(clazz,i) ( (w_field)(clazz)->values[i] )

/**
 * Get the value of a METHOD constant, resolving it if need be.
 * The calling thread must GC safe!
  * @param clazz the clazz whose contant pool is to be used
 * @param i    the index into the constant pool
 * @return the constant value.
*/
w_method getMethodConstant(w_clazz clazz, w_int idx);

/**
** Get the value of a METHOD constant which is known to already be resolved.
 * @param clazz the clazz whose contant pool is to be used
 * @param i    the index into the constant pool
 * @return the constant value.
*/
#define getResolvedMethodConstant(clazz,i) ( (w_method)(clazz)->values[i] )

/**
 * Get the value of an IMETHOD constant, resolving it if need be.
 * The calling thread must GC safe!
 * @param clazz the clazz whose contant pool is to be used
 * @param i    the index into the constant pool
 * @return the constant value.
 */
w_method getIMethodConstant(w_clazz clazz, w_int idx);

/**
** Get the value of an IMETHOD constant which is known to already be resolved.
 * @param clazz the clazz whose contant pool is to be used
 * @param i    the index into the constant pool
 * @return the constant value.
*/
#define getResolvedIMethodConstant(clazz,i) ( (w_method)(clazz)->values[i] )

void dissolveConstant(w_clazz, int idx);

void dumpPools(int fd, w_clazz clazz);

/**
 ** Add a UTF8 constant to the pool (or find an identical existing
 ** one), returning the index of the result.
 * @param clazz the clazz to whose contant pool the constant is to be added.
 * @param the contents of the constant
 * @return the index of the new entry in the constant pool.
 */
w_int addUTF8ConstantToPool(w_clazz, w_string);

/**
** Add a new unresolved Class constant to the pool, unless it already exists. 
** No attempt will be made to resolve the constant (and hence load the class).
** @param clazz the clazz to whose contant pool the constant is to be added.
** @param classname_index the index of an existing UTF8 constant which
** holds the name of the class (this may previously have been added using
** addUTF8Constant()).
** @return the index of the new or existing constant.
*/
w_int addUnresolvedClassConstantToPool(w_clazz clazz, w_size classname_index);

/**
 ** Add a name & type constant to the pool (or find an identical existing
 ** one), returning theindex of the result.
 */
//w_int addNatConstantToPool(w_clazz, w_string name, w_string type);

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

/*
** Get the declaring class name, name, and/or descriptor from a Field, Method, 
** or IMethod constant, without resolving the constant if it is not already 
** resolved. Only the strings for which the corresponding w_string* parameter
** is non-null will be extracted. Returns TRUE if the operation succeeded,
** FALSE if it failed e.g. because the constant is in state COULD_NOT_RESOLVE.
** The resulting w_string's are registered, so remember to deregister
** them afterwards.
*/
w_boolean getMemberConstantStrings(w_clazz clazz, w_int idx, w_string *declaring_clazz_ptr, w_string *member_name_ptr, w_string *member_type_ptr);

#endif /* _CONSTANT_H */
