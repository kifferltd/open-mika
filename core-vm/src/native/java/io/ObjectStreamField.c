/**************************************************************************
* Copyright (c) 2004, 2005, 2006, 2007, 2021 by KIFFER Ltd.               *
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

#include "core-classes.h"
#include "clazz.h"
#include "exception.h"
#include "fields.h"
#include "heap.h"
#include "loading.h"
#include "wstrings.h"
#include "mika_threads.h"
#include "ts-mem.h"
#include "descriptor.h"

void ObjectStreamField_create(w_thread thread, w_instance this, w_instance String, w_instance Type) {
  w_clazz type;
  w_string typestring;

  if (!Type) {
    throwNullPointerException(thread);
    return;
  }

  type = Class2clazz(Type);

  typestring = clazz2desc(type);

  if(!typestring){
    return;
  }

  setReferenceField(this, getStringInstance(typestring), F_ObjectStreamField_typeString);
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


