/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2011 by Chris Gray, /k/ Embedded Java Solutions.    *
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

#ifndef _FIELD_H
#define _FIELD_H

#include "wmath.h"
#include "wonka.h"

/*
** Enable this in order to have boolean and byte fields packed bytewise
** instead of wordwise (to save heap space).
*/
//#define PACK_BYTE_FIELDS

/*
** Define F_Field_wotsit. This way we don't need to include the huge 
** core-classes.h file and as a result speed up compilation a lot since 
** almost every .c file includes this file.
*/

extern w_int F_Field_wotsit;

/*
** Fields of w_Field:
**  declaring_clazz  the declaring clazz
**  label           the C string "field"
**  name            the field's name
**  value_clazz     the type of data it holds
**  flags           low 16 bits: ACC_xxx flags from class file
**                  high 16 bits: Wonka FIELD_xxx flags, see below
**  size_and_slot   high 4 bits: if all '1', then this field is a reference
**                  field, otherwise it is a primitive field and these bits
**                  hold log2 of the field's logical size: e.g. 0000 for a
**                  boolean, 0101 for an int.
**                  low 28 bits: offset of the field within an instance.
**                  currently repesented as follows:
**                    boolean, byte: offset in bytes
**                    other primitives: offset in words
**                    reference: negative offset from the end of the instance,
**                    e.g. the last field has -1 (all '1's).
**                  if this negative then the field is a reference field and
**                  `slot' needs to be subtracted from the instanceSize of
**                  the actual clazz ro arrive at an offset in words.
**                  64 bit values live at 'slot' + WORD_LSW and 'slot' + WORD_MSW.
**  desc            this field's descriptor
**  initval         index in the constant pool of the constant initialiser (0 if none)
*/
typedef struct w_Field {
  w_clazz  declaring_clazz;
  char    *label;
  w_string name;
  w_clazz  value_clazz;
  w_flags  flags;
  w_int    size_and_slot;
  w_string desc;
  w_ushort initval;
  w_ushort dummy; /* bring size up to 32 bytes */
} w_Field;

#ifdef PACK_BYTE_FIELDS
#define FIELD_SIZE_MASK    0xf0000000
#define FIELD_OFFSET_MASK  0x0000ffff

#define FIELD_SIZE_1_BIT   0x00000000
#define FIELD_SIZE_8_BITS  0x30000000
#define FIELD_SIZE_16_BITS 0x40000000
#define FIELD_SIZE_32_BITS 0x50000000
#define FIELD_SIZE_64_BITS 0x60000000

#define FIELD_SIZE(f)   ((((f) & FIELD_SIZE_MASK) >> 28) & 0xf)
#define FIELD_OFFSET(f) ((f) & FIELD_OFFSET_MASK)

#else

#define FIELD_OFFSET(f) (f)
// FIELD_SIZE deliberately left undefined, should never be asked for
#endif

/*
** Wonka flags.  The following combinations are possible:
**  WOTSIT  ARRAY  REFERENCE  LONG  meaning
**     0       0       0       1    primitive field, long or double
**     0       0       0       0    primitive field, other
**     0       0       1       0    reference field, not an array
**     0       1       1       0    array reference field
**     1       0       0       0    wotsit
*/
#define FIELD_IS_LONG       0x10000
#define FIELD_IS_REFERENCE  0x20000
#define FIELD_IS_ARRAY      0x40000
#define FIELD_IS_WOTSIT     0x80000

/*
** fieldSize(f) returns 2 if f->flags has FIELD_IS_LONG set, else 1.
*/
#define fieldSize(f) (1 + !!isSet((f)->flags,  FIELD_IS_LONG))

/*
** Matching criteria
*/

#define MATCH_STATIC_FIELD                         1
#define MATCH_INSTANCE_FIELD                      -1
// #define MATCH_STATIC_OR_INSTANCE                   0

#define MATCH_PRIVATE_FIELD                        1
#define MATCH_PUBLIC_FIELD                        -1
// #define MATCH_PUBLIC_OR_PRIVATE                    0

// #define MATCH_ANY_DESCRIPTOR                       NULL

#define MATCH_ANY                                  0

w_field searchClazzHierarchyForField(w_clazz clazz, w_string name, w_string desc_string, w_int isStatic, w_int isPrivate);
w_field searchClazzAndInterfacesForField(w_clazz clazz, w_string name, w_string desc_string, w_int isStatic, w_int isPrivate);
w_size findFieldOffset(w_clazz clazz, const char * utf8name);

void fieldTableDump(w_clazz clazz);

/*
** Get a pointer to a word-sized field of an instance.
** (Currently also used for half- and double-word fields; this may change).
*/
#ifdef PACK_BYTE_FIELDS
#define wordFieldPointer(parent,slot) ((parent) + ((slot) & FIELD_OFFSET_MASK))
#else
#define wordFieldPointer(parent,slot) ((parent) + (slot))
#endif

/*
** Get a pointer to a byte-sized field of an instance.
** (Currently also used for boolean fields; this may change).
*/
#ifdef PACK_BYTE_FIELDS
#define byteFieldPointer(parent,slot) (((char*)(parent)) + ((slot) & FIELD_OFFSET_MASK))
#else
#define byteFieldPointer wordFieldPointer
#endif

/*
** "Getters" for the various types of primitive field.
*/

#define getBooleanField(parent,slot) !!*wordFieldPointer((parent), (slot))
#define getByteField(parent,slot) (w_sbyte)*byteFieldPointer((parent), (slot))

#define getCharacterField(parent,slot) (w_char)*wordFieldPointer((parent), (slot))
#define getShortField(parent,slot) (w_short)*wordFieldPointer((parent), (slot))

#define getIntegerField(parent,slot) (w_int)*wordFieldPointer((parent), (slot))

#define getFloatField(parent,slot) (w_float)*wordFieldPointer((parent), (slot))

w_long getLongField(w_instance parent, w_int slot);

#define getWotsitField(parent,slot) (void*)*wordFieldPointer((parent), (slot))

/*
** "Setters" for the various types of primitive field.
*/
#define setBooleanField(parent,slot,value) *(byteFieldPointer((parent), (slot))) = !!(value)

static inline void setByteField(w_instance parent, w_int slot, w_sbyte value) {
  *(byteFieldPointer(parent, slot)) = value;
}

static inline void setShortField(w_instance parent, w_int slot, w_short value) {
  *(wordFieldPointer(parent, slot)) = value;
}

static inline void setCharacterField(w_instance parent, w_int slot, w_char value) {
  *(wordFieldPointer(parent, slot)) = value;
}

static inline void setIntegerField(w_instance parent, w_int slot, w_int value) {
  *((w_int*)wordFieldPointer(parent, slot)) = value;
}

static inline void setFloatField(w_instance parent, w_int slot, w_int value) {
  *((w_float*)wordFieldPointer(parent, slot)) = value;
}

static inline void setLongField(w_instance parent, w_int slot, w_long value) {
  (wordFieldPointer(parent, slot))[WORD_LSW] = value & 0x00000000ffffffffUL;
  (wordFieldPointer(parent, slot))[WORD_MSW] = value >> 32;
}

static inline void setDoubleField(w_instance parent, w_int slot, w_double value) {
  w_memcpy(wordFieldPointer(parent, slot), &value, 8);
}

#define setWotsitField(parent,slot,value) memcpy(wordFieldPointer((parent), (slot)), &(value), sizeof(void*))

#define clearWotsitField(parent,slot) memset(wordFieldPointer((parent), (slot)), 0, sizeof(void*))

w_clazz instance2clazz(w_instance ins);
#include "clazz.h"
#include "heap.h"

/*
** To get a reference field of any object. Note that 'slot' is a negative
** value relative to the end of the instance.
*/
#define getReferenceField(parent,slot) ((w_instance)(parent)[instance2clazz((parent))->instanceSize + (slot)])

/*
** To set a reference field of any object, always use this function, which 
** acts as a ``write barrier''. Note that `slot' is a negative value relative
** to the end of the instance.
*/
void setReferenceField(w_instance parent, w_instance child, w_int slot);

/*
** Use this variant if you know that the calling thread is already marked unsafe
** (e.g. from within J-spot or the interpreter).  In case of doubt use setReferenceField above!
*/
void setReferenceField_unsafe(w_instance parent, w_instance child, w_int slot);

/*
** Get the w_field pointer corresponding to an instance of java.lang.reflect.Field.
*/
static inline w_field Field2field(w_instance Field) {
  return getWotsitField(Field, F_Field_wotsit);
}

/*
** Return WONKA_TRUE if field is declared in clazz or one of its superclasses,
** WONKA_FALSE otherwise.
*/
static inline w_boolean isFieldFromClazz(w_clazz clazz, w_field field) {
  return isSuperClass(field->declaring_clazz, clazz);
}

/*
** Formatter functions to print a field name only, or the full Wonka.
*/

char * print_field_short(char * buffer, int * remain, void * data, int w, int p, unsigned int f);
char * print_field_long(char * buffer, int * remain, void * data, int w, int p, unsigned int f);

#endif /* _FIELD_H */
