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

#include "checks.h"
#include "core-classes.h"
#include "fields.h"
#include "loading.h"
#include "wstrings.h"
#include "descriptor.h"
#include "clazz.h"
#include "arrays.h"
#include "threads.h"
#include "heap.h"
#include "ts-mem.h"
#include "reflection.h"
#include "methods.h"
#include "exception.h"

w_instance Field_getDeclaringClass(JNIEnv *env, w_instance Field) {

  return clazz2Class(Field2field(Field)->declaring_clazz);
  
}

w_instance Field_getName(JNIEnv *env, w_instance Field) {

  return newStringInstance(Field2field(Field)->name);

}

w_int Field_getModifiers(JNIEnv *env, w_instance Field) {

  return Field2field(Field)->flags & ACC_FLAGS;

}

w_instance Field_getType(JNIEnv *env, w_instance Field) {

  w_field  field = Field2field(Field);
  w_clazz  clazz;
  w_instance Class = NULL;

  woempa(1, "Field '%w' of %k has type %k\n", field->name, field->declaring_clazz, field->value_clazz);
  if ((mustBeReferenced(field->declaring_clazz) == CLASS_LOADING_FAILED) || (mustBeLoaded(&field->value_clazz) == CLASS_LOADING_FAILED) || (mustBeReferenced(field->value_clazz) == CLASS_LOADING_FAILED)) {

    return NULL;

  }

  clazz = field->value_clazz;
  Class = clazz2Class(clazz);
  woempa(1, "Returning class instance %j of '%k'.\n", Class, clazz);
  
  return Class;

}

w_boolean Field_equals(JNIEnv *env, w_instance Field, w_instance Object) {

  w_field field_1;
  w_field field_2;
  w_clazz objectClazz = instance2clazz(Object);

  /*
  ** 1) Check if Object is indeed a Field instance.
  ** 2) Check that they have the same declaring class.
  ** 3) Check that they have the same name.
  */
  
  if (objectClazz == clazzField) {
    field_1 = Field2field(Field);
    field_2 = Field2field(Object);
    if (field_1->declaring_clazz == field_2->declaring_clazz) {
      if (field_1->name == field_2->name) {
        return WONKA_TRUE;
      }
    }
  }

  return WONKA_FALSE;
 
}

/*
** Utility function to get data from an 'Object' instance or from the static Class data, trough
** a field ('Field' instance) and return it's possibly widened value in the T_data array as type
** T_desc. If the widening doesn't work, we throw an IllegalArgumentException.
*/

void get_convert_and_assign(JNIEnv *env, w_instance thisField, w_instance theObject, w_clazz T_clazz, void *T_data) {

  w_thread thread = JNIEnv2w_thread(env);
  w_field field = Field2field(thisField);
  void *from;
  w_clazz calling_clazz;
  w_instance calling_instance;

  if (isSet(field->flags, ACC_STATIC)) {
    if (mustBeInitialized(field->declaring_clazz) == CLASS_LOADING_FAILED) {
      woempa(7, "Static initializer of %k threw %k\n", field->declaring_clazz, instance2clazz(exceptionThrown(thread)));
      return;
    }
    from = field->declaring_clazz->staticFields + field->size_and_slot;
  }
  else {
    if (!theObject) {
      woempa(7, "Object == null for non-static field %w of %k\n",NM(field), field->declaring_clazz);
      throwException(thread, clazzNullPointerException, NULL);
      return;
    }

    if (mustBeReferenced(field->declaring_clazz) == CLASS_LOADING_FAILED) {

      return;

    }
    if (!isSuperClass(field->declaring_clazz,instance2clazz(theObject))) {
      woempa(7, "%j is not a subclass of %k, does not have field %w\n", theObject, field->declaring_clazz,NM(field));
      throwException(thread, clazzIllegalArgumentException, "not field of this class");
      return;
    }

    if (field->size_and_slot < 0) {
      woempa(7, "Field %w is a reference field\n", field->name);
      from = theObject + instance2clazz(theObject)->instanceSize + field->size_and_slot;
    }
#ifdef PACK_BYTE_FIELDS
    else if ((field->size_and_slot & FIELD_SIZE_MASK) <= FIELD_SIZE_8_BITS) {
      woempa(7, "Field %w is a byte field\n", field->name);
      from = byteFieldPointer(theObject, field->size_and_slot & FIELD_OFFSET_MASK);
    }
    else {
      woempa(7, "Field %w is a word or doubleword field\n", field->name);
      from = wordFieldPointer(theObject, field->size_and_slot & FIELD_OFFSET_MASK);
    }
#else
    else {
      woempa(7, "Field %w is a primitive field\n", field->name);
      from = wordFieldPointer(theObject, field->size_and_slot);
    }
#endif
  }

  calling_clazz = getCurrentClazz(thread);
  calling_instance = getCurrentInstance(thread);
  if (!getBooleanField(thisField, F_AccessibleObject_accessible)
      && !isAllowedToAccess(calling_clazz, field,
      theObject ? instance2clazz(theObject) : NULL )) {
    throwException(thread, clazzIllegalAccessException, NULL);
  }
  else if (!widen(field->value_clazz, from, T_clazz, T_data)) {
    throwException(thread, clazzIllegalArgumentException, "widening not possible");
  }

}  

/*
** Here come the specific get methods.
*/

w_boolean Field_getBoolean(JNIEnv *env, w_instance Field, w_instance Object) {

  w_field  field = Field2field(Field);
  w_word to[1];

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        get_convert_and_assign(env, Field, Object, clazz_boolean, to);
      }
    }
  }

  return to[0];

}

w_sbyte Field_getByte(JNIEnv *env, w_instance Field, w_instance Object) {

  w_field  field = Field2field(Field);
  w_word to[1];

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        get_convert_and_assign(env, Field, Object, clazz_byte, to);
      }
    }
  }

  return to[0];

}

w_char Field_getChar(JNIEnv *env, w_instance Field, w_instance Object) {

  w_field  field = Field2field(Field);
  w_word to[1];

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        get_convert_and_assign(env, Field, Object, clazz_char, to);
      }
    }
  }

  return to[0];
  
}

w_short Field_getShort(JNIEnv *env, w_instance Field, w_instance Object) {

  w_field  field = Field2field(Field);
  w_word to[1];

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        get_convert_and_assign(env, Field, Object, clazz_short, to);
      }
    }
  }

  return to[0];

}

w_int Field_getInt(JNIEnv *env, w_instance Field, w_instance Object) {

  w_field  field = Field2field(Field);
  w_word to[1];

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        get_convert_and_assign(env, Field, Object, clazz_int, to);
      }
    }
  }

  return to[0];

}

w_long Field_getLong(JNIEnv *env, w_instance Field, w_instance Object) {

  w_field  field = Field2field(Field);
  w_long   to;

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        get_convert_and_assign(env, Field, Object, clazz_long, (w_word*)&to);
      }
    }
  }

  return to;

}

w_float Field_getFloat(JNIEnv *env, w_instance Field, w_instance Object) {

  w_field  field = Field2field(Field);
  w_word to[1];

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        get_convert_and_assign(env, Field, Object, clazz_float, to);
      }
    }
  }

  return to[0];

}

w_double Field_getDouble(JNIEnv *env, w_instance Field, w_instance Object) {

  w_field  field = Field2field(Field);
  w_double to;

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        get_convert_and_assign(env, Field, Object, clazz_double, (w_word*)&to);
      }
    }
  }

  return to;

}

w_instance Field_get(JNIEnv *env, w_instance Field, w_instance Object) {

  w_thread thread = JNIEnv2w_thread(env);
  w_field field = Field2field(Field);
  w_instance result = NULL;
  void *to;

  /*
  ** Get the underlying value of the field.
  */

  woempa(7, "Getting value of field '%w', class '%k' of instance '%j'\n", field->name, field->value_clazz, Object);

  if (Object == NULL && isNotSet(field->flags, ACC_STATIC)) {
    throwException(thread, clazzNullPointerException, NULL);

    return NULL;

  }

  if (isNotSet(field->flags, ACC_STATIC) &&  ! isFieldFromClazz(instance2clazz(Object), field)) {
    throwException(thread, clazzIllegalArgumentException, "not field of this class");

    return NULL;

  }

/*
** TODO: Access checks! (private/protected/public ...)
*/
  if ((mustBeReferenced(field->declaring_clazz) == CLASS_LOADING_FAILED) || (mustBeLoaded(&field->value_clazz) == CLASS_LOADING_FAILED) || (mustBeReferenced(field->value_clazz) == CLASS_LOADING_FAILED)) {

    return NULL;

  }
  if (isSet(field->value_clazz->flags, CLAZZ_IS_PRIMITIVE)) {
    w_int slot;
    result = createWrapperInstance(thread, field->value_clazz, &slot);
    if (result) {
#ifdef PACK_BYTE_FIELDS
      if ((slot & FIELD_SIZE_MASK) <= FIELD_SIZE_8_BITS) {
        to = byteFieldPointer(result, slot);
      }
      else {
        to = wordFieldPointer(result, slot);
      }
#else
      to = wordFieldPointer(result, slot);
#endif
      get_convert_and_assign(env, Field, Object, field->value_clazz, to);
    }
  }
  else {
    if (isSet(field->flags, ACC_STATIC)) {
      if (mustBeInitialized(field->declaring_clazz) == CLASS_LOADING_FAILED) {
        result = NULL;
      }
      else {
        result = (w_instance)field->declaring_clazz->staticFields[field->size_and_slot];
      }
    }
    else {
      result = (w_instance)Object[instance2clazz(Object)->instanceSize + field->size_and_slot];
    }
  }

  return result;
      
}

static void set_convert_and_assign(JNIEnv *env, w_instance thisField, w_instance theObject, w_clazz F_clazz, w_word F_data[]) {

  w_thread thread = JNIEnv2w_thread(env);
  w_field field = Field2field(thisField);
  w_clazz calling_clazz;
  w_instance calling_instance;
  void *to;
  
  if (isSet(field->flags, ACC_STATIC)) {
    if (mustBeInitialized(field->declaring_clazz) == CLASS_LOADING_FAILED) {

      return;

    }
    to = field->declaring_clazz->staticFields + field->size_and_slot;
  }
  else {
    if (field->size_and_slot < 0) {
      woempa(7, "Field %w is a reference field\n", field->name);
      to = theObject + instance2clazz(theObject)->instanceSize + field->size_and_slot;
    }
#ifdef PACK_BYTE_FIELDS
    else if ((field->size_and_slot & FIELD_SIZE_MASK) <= FIELD_SIZE_8_BITS) {
      woempa(7, "Field %w is a byte field\n", field->name);
      to = byteFieldPointer(theObject, field->size_and_slot & FIELD_OFFSET_MASK);
    }
    else {
      woempa(7, "Field %w is a word or doubleword field\n", field->name);
      to = wordFieldPointer(theObject, field->size_and_slot & FIELD_OFFSET_MASK);
    }
#else
    else {
      woempa(7, "Field %w is a primitive field\n", field->name);
      to = wordFieldPointer(theObject, field->size_and_slot);
    }
#endif
  }

  calling_clazz = getCurrentClazz(thread);
  calling_instance = getCurrentInstance(thread);
  if (!getBooleanField(thisField, F_AccessibleObject_accessible)
      && !isAllowedToAccess(calling_clazz, field, theObject ? instance2clazz(theObject) : NULL)) {

    throwException(thread, clazzIllegalAccessException, NULL);
  }
  else if (!widen(F_clazz, F_data, field->value_clazz, to)) {
    throwException(thread, clazzIllegalArgumentException, "widening not possible");
  }

}  

void Field_setBoolean(JNIEnv *env, w_instance Field, w_instance Object, w_boolean z) {

  w_field  field = Field2field(Field);
  w_word from[1];

  from[0] = z;

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        set_convert_and_assign(env, Field, Object, clazz_boolean, from);
      }
    }
  }

}

void Field_setByte(JNIEnv *env, w_instance Field, w_instance Object, w_sbyte b) {

  w_field  field = Field2field(Field);
  w_word from[1];

  from[0] = b;

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        set_convert_and_assign(env, Field, Object, clazz_byte, from);
      }
    }
  }

}

void Field_setChar(JNIEnv *env, w_instance Field, w_instance Object, w_char c) {

  w_field  field = Field2field(Field);
  w_word from[1];

  from[0] = c;

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        set_convert_and_assign(env, Field, Object, clazz_char, from);
      }
    }
  }

}

void Field_setShort(JNIEnv *env, w_instance Field, w_instance Object, w_short s) {

  w_field  field = Field2field(Field);
  w_word from[1];

  from[0] = s;

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        set_convert_and_assign(env, Field, Object, clazz_short, from);
      }
    }
  }

}

void Field_setInt(JNIEnv *env, w_instance Field, w_instance Object, w_int i) {

  w_field  field = Field2field(Field);
  w_word from[1];

  from[0] = i;

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        set_convert_and_assign(env, Field, Object, clazz_int, from);
      }
    }
  }

}

void Field_setLong(JNIEnv *env, w_instance Field, w_instance Object, w_long l) {

  w_field  field = Field2field(Field);
  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        set_convert_and_assign(env, Field, Object, clazz_long, (w_word*)&l);
      }
    }
  }

}

void Field_setFloat(JNIEnv *env, w_instance Field, w_instance Object, w_float f) {

  w_field  field = Field2field(Field);
  w_word from[1];

  from[0] = f;

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        set_convert_and_assign(env, Field, Object, clazz_float, from);
      }
    }
  }

}

void Field_setDouble(JNIEnv *env, w_instance Field, w_instance Object, w_double d) {

  w_field  field = Field2field(Field);

  if (mustBeReferenced(field->declaring_clazz) != CLASS_LOADING_FAILED) {
    if (mustBeLoaded(&field->value_clazz) != CLASS_LOADING_FAILED) {
      if (mustBeReferenced(field->value_clazz) != CLASS_LOADING_FAILED) {
        set_convert_and_assign(env, Field, Object, clazz_double, (w_word*)&d);
      }
    }
  }

}


void Field_set(JNIEnv *env, w_instance Field, w_instance Object, w_instance Value) {

  w_thread thread = JNIEnv2w_thread(env);
  w_field field = Field2field(Field);
  w_word *data = NULL;
  w_clazz clazz;

  woempa(1, "Setting value of field '%w', class '%k' of instance '%j'\n", NM(field), field->value_clazz,Object);

  if (!Object && isNotSet(field->flags, ACC_STATIC)) {
    throwException(thread, clazzNullPointerException, NULL);

    return;

  }

  if (isNotSet(field->flags, ACC_STATIC) &&  ! isFieldFromClazz(instance2clazz(Object), field)) {
    throwException(thread, clazzIllegalArgumentException, "not field of this class");

    return;

  }

/*
** TODO: Access checks! (private/protected/public ...)
*/
  if ((mustBeReferenced(field->declaring_clazz) == CLASS_LOADING_FAILED) || (mustBeLoaded(&field->value_clazz) == CLASS_LOADING_FAILED) || mustBeReferenced(field->value_clazz) == CLASS_LOADING_FAILED) {

    return;

  }

  if (isSet(field->value_clazz->flags, CLAZZ_IS_PRIMITIVE) && !Value) {
    throwException(thread, clazzNullPointerException, NULL);
  }
  else if (isNotSet(field->flags,ACC_STATIC) && !isSuperClass(field->declaring_clazz,instance2clazz(Object))) {
    throwException(thread, clazzIllegalArgumentException, "not field of this class");
  }
  else if (!getBooleanField(Field, F_AccessibleObject_accessible) && isSet(field->flags, ACC_FINAL)) {
      throwException(thread, clazzIllegalAccessException, "field is FINAL");
    /*
    ** Missing checks:
    **   not ACC_FINAL and caller is another package -> IllegalAccessException
    **   ACC_PRIVATE and caller is not exact same class -> idem ditto
    **   ACC_PROTECTED and caller is not same/subclass  -> idem ditto
    */
  }
  else if (isSet(field->value_clazz->flags, CLAZZ_IS_PRIMITIVE)) {

    /*
    ** It's a primitive field, get the appropriate descriptor and
    ** the unwrapped data and see if a widening assignment can happen.
    ** If the Value instance is not of a wrapper clazz, we get a NULL
    ** descriptor back...
    */

    clazz = getWrappedValue(Value, &data);
    if (clazz) {
      set_convert_and_assign(env, Field, Object, clazz, data);
    }
    else {
      throwException(thread, clazzIllegalArgumentException, NULL);
    }
  }
  else {

    /*
    ** It's a reference, see if the field is declared in the clazz of Object
    ** or in one of the super classes and this of course only when the field is not static...
    */

    if (!isSet(field->flags, ACC_STATIC) && ! isSuperClass(field->declaring_clazz, instance2clazz(Object))) {
      throwException(thread, clazzIllegalArgumentException, "not field of this class");
    }
    else if (isSet(field->flags, ACC_STATIC)) {
      if (mustBeInitialized(field->declaring_clazz) == CLASS_LOADING_FAILED) {
        setStaticReferenceField(field->declaring_clazz, (w_int)field->size_and_slot, Value);
      }
    }
    else {
      setReferenceField(Object, Value, field->size_and_slot);
    }
  }

}

