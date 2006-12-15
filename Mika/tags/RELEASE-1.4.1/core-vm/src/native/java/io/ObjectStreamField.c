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
** $Id: ObjectStreamField.c,v 1.4 2006/05/23 14:54:14 cvs Exp $
*/

#include <string.h>

#include "core-classes.h"
#include "clazz.h"
#include "exception.h"
#include "fields.h"
#include "heap.h"
#include "loading.h"
#include "wstrings.h"
#include "threads.h"
#include "ts-mem.h"
#include "descriptor.h"

void ObjectStreamField_create(JNIEnv *env, w_instance this, w_instance String, w_instance Type) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz type;
  w_string typestring;

  if (!Type) {
    throwException(thread, clazzNullPointerException, NULL);
    return;
  }

  type = Class2clazz(Type);

  typestring = clazz2desc(type);

  if(!typestring){
    return;
  }

  setReferenceField(this, newStringInstance(typestring), F_ObjectStreamField_typeString);
  deregisterString(typestring);


  setBooleanField(this, F_ObjectStreamField_isPrimitive, WONKA_TRUE);

  if (type->dims) {
    setBooleanField(this, F_ObjectStreamField_isPrimitive, WONKA_FALSE);
    setCharacterField(this, F_ObjectStreamField_code, '[');
  }
  else if (type == clazz_byte) {
    setCharacterField(this, F_ObjectStreamField_code, 'B');
  }
  else if (type == clazz_char) {
    setCharacterField(this, F_ObjectStreamField_code, 'C');
  }
  else if (type == clazz_double) {
    setCharacterField(this, F_ObjectStreamField_code, 'D');
  }
  else if (type == clazz_float) {
    setCharacterField(this, F_ObjectStreamField_code, 'F');
  }
  else if (type == clazz_int) {
    setCharacterField(this, F_ObjectStreamField_code, 'I');
  }
  else if (type == clazz_long) {
    setCharacterField(this, F_ObjectStreamField_code, 'J');
  }
  else if (type == clazz_short) {
    setCharacterField(this, F_ObjectStreamField_code, 'S');
  }
  else if (type == clazz_boolean) {
    setCharacterField(this, F_ObjectStreamField_code, 'Z');
  }
  else {
    /*
    ** Must be another class or Interface type.
    */
    setBooleanField(this, F_ObjectStreamField_isPrimitive, WONKA_FALSE);
    setCharacterField(this, F_ObjectStreamField_code, 'L');
  }

  woempa(6, "fieldname is '%w', type is Class '%k', type code set at '%c'.\n", String2string(String), type, getCharacterField(this, F_ObjectStreamField_code));

  if (mustBeInitialized(clazzField) == CLASS_LOADING_FAILED) {
    return;
  }

  setReferenceField(this, Type, F_ObjectStreamField_type);
  setReferenceField(this, String, F_ObjectStreamField_name);
}


