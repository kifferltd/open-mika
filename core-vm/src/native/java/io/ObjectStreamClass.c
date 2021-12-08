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
#include "checks.h"
#include "clazz.h"
#include "arrays.h"
#include "exception.h"
#include "fields.h"
#include "heap.h"
#include "loading.h"
#include "mika_threads.h"
#include "methods.h"
#include "wstrings.h"

#define IO_LEVEL 6

// TODO move this code to methods.c?
w_method findInheritedMethod(const w_clazz clazz, const char *method_name, const char *method_spec);

w_method findInheritedMethod(const w_clazz clazz, const char *method_name, const char *method_spec) {
      w_MethodSpec *spec;
      w_method candidate;

      w_string method_name_string = utf2String(method_name, strlen(method_name));
      w_string method_desc_string = utf2String(method_spec, strlen(method_spec));
      if (createMethodSpecUsingDescriptor(clazz, method_name_string, method_desc_string, &spec) == CLASS_LOADING_FAILED) {
        wabort(ABORT_WONKA,"Uh oh: failed to build method spec using clazz %k, name %w, desc %w.\n",clazz, method_name_string, method_desc_string);
      }

      w_method the_method = NULL;
      w_clazz search_clazz = clazz;
      w_int i, j;
      for (j = 0; the_method == NULL && j < clazz->numSuperClasses;) {
        for (i = 0; i < clazz->numDeclaredMethods; ++i) {
          woempa(1, "Seek %w in %K\n", spec->name, search_clazz);
          candidate = &search_clazz->own_methods[i];
          woempa(1, "Checking %M\n", candidate);

          if (candidate->spec.name == method_name_string && candidate->desc == method_desc_string) {
            the_method = candidate;
            woempa(1, "Found %s.%s at %p\n", method_name_string, method_desc_string, the_method);
            break;
          }
        }
        search_clazz = search_clazz->supers[j++];
      }

      releaseMethodSpec(spec);
      releaseMem(spec);
      deregisterString(method_name_string);
      deregisterString(method_desc_string);
}


w_instance ObjectStreamClass_createObjectStreamClass(w_thread thread, w_instance ObjectStreamClass, w_instance Class) {
  w_clazz clazz;
  w_instance newOSC = NULL;

  if (mustBeInitialized(clazzMethod) == CLASS_LOADING_FAILED) {
    return NULL;
  }

  if(Class == NULL){
    throwNullPointerException(thread);
    return NULL;
  }

  clazz = Class2clazz(Class);
  if(isAssignmentCompatible(clazz, clazzSerializable)){
    w_int flags;
    enterUnsafeRegion(thread);
    newOSC = allocInstance(thread, clazzObjectStreamClass);
    enterSafeRegion(thread);

    if(newOSC == NULL){
      return NULL;
    }

    if(isAssignmentCompatible(clazz, clazzExternalizable)){
      flags = 0x0c;
    }
    else {
      w_method writeObject_method = findInheritedMethod(clazz, "writeObject", "(Ljava/io/ObjectOutputStream;)V");

      flags = 0x02;

      if(writeObject_method != NULL){
        if(writeObject_method->spec.declaring_clazz == clazz) {
          w_instance Method;

          enterUnsafeRegion(thread);
          Method = allocInstance(thread, clazzMethod);
          enterSafeRegion(thread);

          if(Method){
            w_int    acc_flags =  writeObject_method->flags & 0x0000ffff;

            if(acc_flags & ACC_PRIVATE){
              setReferenceField(newOSC, Method, F_ObjectStreamClass_writeObject);
              setWotsitField(Method, F_Method_wotsit, writeObject_method);
              setBooleanField(Method, F_AccessibleObject_accessible, WONKA_TRUE);
              flags |= 0x01;
            }
          }
          else {
            return NULL;
          }
        }
      }
      else {
        clearException(thread);
      }
    }

    //Setup suid ...
    {
      w_long* suid = (w_long*)wordFieldPointer(newOSC, F_ObjectStreamClass_suid);
      w_string name = cstring2String("serialVersionUID", 16);
      w_string desc = cstring2String("J", 16);
      w_field suid_field = searchClazzOnlyForField(clazz, name, desc, 1, -1);
      deregisterString(desc);
      deregisterString(name);

      if(suid_field && isSet(suid_field->flags, ACC_PRIVATE | ACC_FINAL | ACC_STATIC)){
        woempa(IO_LEVEL, "hardCoded suid found for %k\n",clazz);
	*suid = *(w_long*)&clazz->staticFields[FIELD_OFFSET(suid_field->size_and_slot)];
      }
      else {
        woempa(IO_LEVEL, "no hardCoded suid found for %k\n",clazz);
        *suid = Wonka_suid(thread, NULL, Class);
      }
    }

    {
      w_method writeReplace_method = findInheritedMethod(clazz, "writeReplace", "()Ljava/lang/Object;");
      if(writeReplace_method != NULL) {
        jobject method; 

        enterUnsafeRegion(thread);
        method = allocInstance(thread, clazzMethod);
        enterSafeRegion(thread);
        if(method){
          setWotsitField(method, F_Method_wotsit, writeReplace_method);
          setReferenceField(newOSC, method, F_ObjectStreamClass_writeReplace);
          setBooleanField(method, F_AccessibleObject_accessible, WONKA_TRUE);
        }
        else {
          return NULL;
        }
      }
      else {
        clearException(thread);
      }
    }

    //this allows us to check if the object is an array !!!
    if(clazz->dims){
      flags |= 0x01000000; //make sure this is the same value as java.io.ObjectStreamClass IS_ARRAY
    }

    setIntegerField(newOSC, F_ObjectStreamClass_flags, flags);
    setReferenceField(newOSC, Class, F_ObjectStreamClass_clazz);

  }
  return newOSC;
}

void ObjectStreamClass_verifyInput(w_thread thread, w_instance thisOSC) {
  w_instance Class = getReferenceField(thisOSC, F_ObjectStreamClass_clazz);
  w_int flags = getIntegerField(thisOSC, F_ObjectStreamClass_flags);
  w_clazz clazz;

  if (mustBeInitialized(clazzField) == CLASS_LOADING_FAILED
   || mustBeInitialized(clazzMethod) == CLASS_LOADING_FAILED
    ) {
    return;
  }

  if(Class == NULL){
    throwException(thread, clazzClassNotFoundException, NULL);
    return;
  }

  clazz = Class2clazz(Class);

  //check if this class is serializable
  woempa(IO_LEVEL, "Class %k is serializable ? %d\n",clazz,isAssignmentCompatible(clazz, clazzSerializable));
  if(!isAssignmentCompatible(clazz, clazzSerializable)){
    //this is allowed to happen if it is the clazzDescripter of when a non serializable class gets serialized
    w_long* suid = (w_long*)wordFieldPointer(thisOSC, F_ObjectStreamClass_suid);

    if(*suid != 0x0 || flags != 0){
      flags |= (0x00800000 | 0x00400000); //make sure this is the same value as java.io.ObjectStreamClass IS_BAD and IS_NOT_SER
      woempa(IO_LEVEL, "Class %k is not serializable\n",clazz);
      throwException(thread, clazzInvalidClassException, "Class %k is not serializable",clazz);
    }
    return;
  }
  else{
    woempa(IO_LEVEL, "Class %k is serializable\n",clazz);
  }
  //check serialVersionUID ...
  {
    w_long* suid = (w_long*)wordFieldPointer(thisOSC, F_ObjectStreamClass_suid);
    w_long realsuid;
    w_word i;
    w_string name = cstring2String("serialVersionUID", 16);
    w_string desc = cstring2String("J", 1);
    w_field realsuid_field = searchClazzOnlyForField(clazz, name, desc, 1, -1);
    deregisterString(desc);
    deregisterString(name);

    if(realsuid_field && isSet(realsuid_field->flags, ACC_PRIVATE | ACC_FINAL | ACC_STATIC)){
      woempa(IO_LEVEL, "hardCoded suid found for %k\n",clazz);
      realsuid = *(w_long*)&clazz->staticFields[FIELD_OFFSET(realsuid_field->size_and_slot)];
    }
    else {
      realsuid = Wonka_suid(thread, NULL, Class);
      woempa(IO_LEVEL, "no hardCoded suid found for %k calculated %lld\n",clazz,realsuid);
    }

    if(realsuid != *suid){
      flags |= (0x00800000 | 0x00200000); //make sure this is the same value as java.io.ObjectStreamClass IS_BAD and WRONG_SUID
      woempa(IO_LEVEL, "local Class %k has different SUID %lld (%llx) than SUID on stream %lld (%llx)\n",clazz, realsuid, realsuid, *suid, *suid);
      throwException(thread, clazzInvalidClassException,
        "local Class %k has different SUID %lld (0x%llx) then SUID on stream %lld(0x%llx) ", clazz, realsuid, realsuid, *suid, *suid);
      return;
    }
    else {
      woempa(IO_LEVEL, "local Class %k has same SUID %llx then SUID on stream %llx\n",clazz, realsuid, *suid);
    }
  }


  {
    //setup readResolve
    w_method readResolve_method = findInheritedMethod(clazz, "readResolve", "()Ljava/lang/Object;");
    if(readResolve_method != NULL) {
      jobject method; 

      enterUnsafeRegion(thread);
      method = allocInstance(thread, clazzMethod);
      enterSafeRegion(thread);
      if(method){
        setWotsitField(method, F_Method_wotsit, readResolve_method);
        setReferenceField(thisOSC, method, F_ObjectStreamClass_readResolve);
        setBooleanField(method, F_AccessibleObject_accessible, WONKA_TRUE);
      }
      else {
        return;
      }
    }
    else {
      clearException(thread);
    }
  }

  {
    //setup readObject
    w_method readObject_method = find_method(clazz, "readObject", "(Ljava/io/ObjectInputStream;)V");

    if(readObject_method != NULL){
        w_instance Method; 

        enterUnsafeRegion(thread);
        Method = allocInstance(thread, clazzMethod);
        enterSafeRegion(thread);

        if(Method){
          w_method method =  (w_method) readObject_method;
          w_int    acc_flags =  method->flags & 0x0000ffff;

          if(acc_flags & ACC_PRIVATE){
            setReferenceField(thisOSC, Method, F_ObjectStreamClass_readObject);
            setWotsitField(Method, F_Method_wotsit, readObject_method);
            setBooleanField(Method, F_AccessibleObject_accessible, WONKA_TRUE);
          }
        }
        else {
          return;
        }
    }
    else {
      clearException(thread);
    }
  }

  {
    //walk through the ObjectStreamFields and create the Fields where needed ...
    w_instance OsFields =  getReferenceField(thisOSC, F_ObjectStreamClass_osFields);
    w_word numFields = clazz->numFields;
    w_field flds = clazz->own_fields;
    w_word start = clazz->numStaticFields;
    w_instance * osFields;
    w_int i;
    w_int length;

    if(OsFields == NULL){
      return;
    }

    osFields = instance2Array_instance(OsFields);
    length = instance2Array_length(OsFields);

    for(i = 0 ; i < length ; i++){
      w_instance OsField = osFields[i];
      w_instance Name;
      w_string name;
      w_instance Type;
      w_string type;
      w_word j;

      if(!OsField){
        throwNullPointerException(thread);
        woempa(IO_LEVEL, "Throwing NullPointerException ObjectStreamField[%d] is NULL\n",i);
        return;
      }

      Name = getReferenceField(OsField, F_ObjectStreamField_name);

      if(!Name){
        throwNullPointerException(thread);
        woempa(IO_LEVEL, "Throwing NullPointerException ObjectStreamField[%d].name is NULL\n",i);
        return;
      }

      name = String2string(Name);

      Type = getReferenceField(OsField, F_ObjectStreamField_typeString);

      if(!Type){
        throwNullPointerException(thread);
        woempa(IO_LEVEL, "Throwing NullPointerException ObjectStreamField[%d].type is NULL\n",i);
        return;
      }

      type = String2string(Type);

      for(j = start ; j < numFields ; j++){
        w_field osfield = flds+j;

        if(osfield->name == name && osfield->value_clazz->dotified == type){
          w_instance newField;

          enterUnsafeRegion(thread);
          newField = allocInstance(thread, clazzField);
          enterSafeRegion(thread);

          if(newField){
            setWotsitField(newField, F_Field_wotsit, osfield);
            setBooleanField(newField, F_AccessibleObject_accessible, WONKA_TRUE);

            setReferenceField(OsField, newField, F_ObjectStreamField_field);
            break;
          }
          else {
            return;
          }
        }
      }
    }
  }
  //TODO: what about serialPersitantFields ???  --> we use the fields found on the stream !
}

w_instance ObjectStreamClass_createFields(w_thread thread, w_instance thisOSC) {
  w_instance Class = getReferenceField(thisOSC, F_ObjectStreamClass_clazz);
  w_instance OSFIELDS = NULL;
  w_clazz clazz;

  threadMustBeSafe(thread);
  if (mustBeInitialized(clazzField) == CLASS_LOADING_FAILED
   || mustBeInitialized(clazzObjectStreamField) == CLASS_LOADING_FAILED
    ) {
    return NULL;
  }

  if(Class == NULL){
    throwNullPointerException(thread);
    woempa(IO_LEVEL, "Throwing NullPointerException Class is NULL\n");
    return NULL;
  }

  clazz = Class2clazz(Class);


  if(isAssignmentCompatible(clazz, clazzSerializable)){
    w_field flds = clazz->own_fields;
    w_word stop = clazz->numStaticFields;
    w_word i;
    w_field spf_field = NULL;
    w_string name = cstring2String("serialPersistentFields", strlen("serialPersistentFields"));
    w_string desc = cstring2String("[Ljava/io/ObjectStreamClass", 16);
    w_field suid_field = searchClazzOnlyForField(clazz, name, desc, 1, -1);
    deregisterString(desc);
    deregisterString(name);

    if(spf_field && isSet(spf_field->flags, ACC_PRIVATE | ACC_FINAL | ACC_STATIC)){
      w_instance Field;

      enterUnsafeRegion(thread);
      Field = allocInstance(thread, clazzField);
      enterSafeRegion(thread);

      if(Field){
        setWotsitField(Field, F_Field_wotsit, spf_field);

        if(isSet(spf_field->flags, ACC_FINAL | ACC_PRIVATE )){
          w_instance OsFields = *(w_instance*)&clazz->staticFields[FIELD_OFFSET(spf_field->size_and_slot)];

          if(OsFields && (OsFields = cloneArray(thread,OsFields))){
            w_int i = 0;
            w_int length = instance2Array_length(OsFields);
            w_instance * osFields = instance2Array_instance(OsFields);
            w_word numFields = clazz->numFields;
            w_field flds = clazz->own_fields;
            w_word start = clazz->numStaticFields;

            for( ; i < length ; i++){
              w_instance OsField = osFields[i];
              w_instance Name;
              w_string name;
              w_instance Type;
              w_clazz type;
              w_word j;

              if(!OsField){
                throwNullPointerException(thread);
                woempa(IO_LEVEL, "Throwing NullPointerException ObjectStreamField[%d] is NULL\n",i);
                return NULL;
              }

              Name = getReferenceField(OsField, F_ObjectStreamField_name);

              if(!Name){
                throwNullPointerException(thread);
                woempa(IO_LEVEL, "Throwing NullPointerException ObjectStreamField[%d].name is NULL\n",i);
                return NULL;
              }

              name = String2string(Name);

              Type = getReferenceField(OsField, F_ObjectStreamField_type);

              if(!Type){
                throwNullPointerException(thread);
                woempa(IO_LEVEL, "Throwing NullPointerException ObjectStreamField[%d].type is NULL\n",i);
                return NULL;
              }

              type = Class2clazz(Type);

              for(j = start ; j < numFields ; j++){
                w_field osfield = flds+j;

                if(osfield->name == name && osfield->value_clazz == type){
                  w_instance newField;

                  enterUnsafeRegion(thread);
                  newField = allocInstance(thread, clazzField);
                  enterSafeRegion(thread);

                  if(newField){
                    setWotsitField(newField, F_Field_wotsit, osfield);
                    setBooleanField(newField, F_AccessibleObject_accessible, WONKA_TRUE);

                    setReferenceField(OsField, newField, F_ObjectStreamField_field);
                    break;
                  }
                  else {
                    return NULL;
                  }
                }
              }
              /** if we don't find the w_field we don't create a Field object
              **  it's up to the ObjectOutputStream to complain about it !!!
              */
            }
          }
          return OsFields;
        }
        woempa(9, "found static field serialPersistentFields in %k, but it was not private and final\n",clazz);
      }
      else {
        woempa(IO_LEVEL, "returning Null: failed to create Field\n");
        return NULL;
      }
    }
    else {
      w_int numField = clazz->numFields;
      w_field flds = clazz->own_fields;
      w_int i = clazz->numStaticFields;
      w_int count=0;

      w_field* prf;

      clearException(thread);

      prf = allocMem((numField - i) * sizeof(w_field));

      if(prf == NULL){
        woempa(IO_LEVEL, "returning Null: allocMem failed.\n");
        return NULL;
      }

      for( ; i < numField ; i++){
        if(isNotSet(flds[i].flags, ACC_TRANSIENT | ACC_STATIC)){
          prf[count++] = flds+i;
        }
      }

      woempa(IO_LEVEL, "found %d fields to be serialized in %k\n",count,clazz);

      if(count){
        w_clazz array_osfclazz = getNextDimension(clazzObjectStreamField, NULL);
        w_clazz array_fclazz = getNextDimension(clazzField, NULL);

        if(array_fclazz && array_osfclazz && mustBeInitialized(array_fclazz) != CLASS_LOADING_FAILED && mustBeInitialized(array_osfclazz) != CLASS_LOADING_FAILED) {
          enterUnsafeRegion(thread);
          OSFIELDS = allocArrayInstance_1d(thread, array_osfclazz, count);
          enterSafeRegion(thread);

          if(OSFIELDS){
            for(i = 0 ; i < count ; i++){
              w_instance OSField;
              w_instance newField;
              w_field field = prf[i];
              w_instance String;
              w_instance FldClass;

              enterUnsafeRegion(thread);
              OSField = allocInstance(thread, clazzObjectStreamField);
              newField = allocInstance(thread, clazzField);
              enterSafeRegion(thread);
              String = getStringInstance(field->name);

              if (!OSField || !newField || !String) {
                woempa(9, "Unable to allocate OSField and newField\n");
                return NULL;
              }

              if(mustBeLoaded(&field->value_clazz) == CLASS_LOADING_FAILED){
                break;
              }

              FldClass = clazz2Class(field->value_clazz);

              woempa(IO_LEVEL, "field %i in %k: %w %k \n",i,clazz,field->name,field->value_clazz);

              if(!OSField && newField){
                break;
              }

              setWotsitField(newField, F_Field_wotsit, field);
              setBooleanField(newField, F_AccessibleObject_accessible, WONKA_TRUE);

              ObjectStreamField_create(thread, OSField, String, FldClass);

              setArrayReferenceField(OSFIELDS, OSField, (w_int) i);
              setReferenceField(OSField, newField, F_ObjectStreamField_field);

            }
          }
        }
      }
      releaseMem(prf);
    }
  }

  return OSFIELDS;
}
