/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2008 by Chris Gray,         *
* /k/ Embedded Java Solutions. All rights reserved.                       *
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

#include "arrays.h"
#include "checks.h"
#include "clazz.h"
#include "constant.h"
#include "core-classes.h"
#include "exception.h"
#include "fields.h"
#include "fifo.h"
#include "heap.h"
#include "interpreter.h"
#include "loading.h"
#include "methods.h"
#include "wstrings.h"

#define PUBLIC   0 //this is the value of Member.PUBLIC
#define DECLARED 1 //this is the value of Member.DECLARED

/*
** Destructor called from GC just before releasing the memory occupied by
** an instance of java.lang.Class . Used to clean up the w_Clazz structure.
*/
/*
** getName() returns the name held in clazz->dotified.
*/
w_instance Class_getName(JNIEnv *env, w_instance Class) {
  w_instance Name = newStringInstance(Class2clazz(Class)->dotified);
  
  return Name;

}

/*
** isPrimitive() relies on the CLAZZ_IS_PRIMITIVE flag.
*/
w_boolean Class_isPrimitive(JNIEnv *env, w_instance Class) {

  return isSet(Class2clazz(Class)->flags, CLAZZ_IS_PRIMITIVE);

}

/*
** A class is an array class if it has 1 or more dimensions.
*/
w_boolean Class_isArray(JNIEnv *env, w_instance Class) {

  return Class2clazz(Class)->dims != 0;

}

w_boolean Class_isInterface(JNIEnv *env, w_instance Class) {

  return isSet(Class2clazz(Class)->flags, ACC_INTERFACE);

}

/*
** isInstance(foo) returns true iff foo is non-null and is assignable to a
** variable of this class.
*/
w_boolean Class_isInstance(JNIEnv *env, w_instance thisClass, w_instance theObject) {

  if (theObject) {
    w_clazz this_clazz = Class2clazz(thisClass);
    w_clazz that_clazz = instance2clazz(theObject);

    return isAssignmentCompatible(that_clazz, this_clazz); 

  }


  return WONKA_FALSE;
}

/*
** isAssignableFrom(c) returns true iff an instance of class c would be
** assignable to a variable of this class using only identity conversion
** or reference widening conversion.
*/
w_boolean Class_isAssignableFrom(JNIEnv *env, w_instance thisClass, w_instance thatClass) {

  w_thread thread = JNIEnv2w_thread(env);

  if (thatClass) {
    w_clazz this_clazz = Class2clazz(thisClass);
    w_clazz that_clazz = Class2clazz(thatClass);

    return isAssignmentCompatible(that_clazz, this_clazz); 
  }
  
  throwException(thread, clazzNullPointerException, NULL);

  return WONKA_FALSE;
}

/*
** getSuperclass() returns NULL if this is an interface, a primitive class,
** or java.lang.Object. If this is an array class, getSuperclass() returns
** java.lang.Object.  Otherwise, getSuper() is used to find the immediate
** superclass.
*/
w_instance Class_getSuperclass(JNIEnv *env, w_instance Class) {

  w_instance Super = NULL;
  w_clazz clazz = Class2clazz(Class);
  w_clazz super;

  if (! clazz) {

    return NULL;

  }

  if (isSet(clazz->flags, ACC_INTERFACE)) {
    woempa(1, "--> is interface; returning null instance.\n");
  }
  else if (clazz == clazzObject) {
    woempa(1, "--> is Object; returning null instance.\n");
  }
  else if (isSet(clazz->flags, CLAZZ_IS_PRIMITIVE)) {
    woempa(1, "--> is primitive; returning null instance.\n");
  }
  else if (clazz->dims) {
    woempa(1, "--> is array; returning Object Class.\n");
    Super = clazzObject->Class;
  }
  else {      
    woempa(1, "--> super class is '%k'.\n", clazz->supers[0]);
    super = getSuper(clazz);
    Super = super ? clazz2Class(super) : NULL;
  }
  
  return Super;
}

/*
** getInterfaces() returns the interfaces which are directly implemented
** by this class or extended by this interface.  Interfaces which this
** class indirectly implements or this interface indirectly extends are
** not included. The order should be the same as in the class file.
*/
w_instance Class_getInterfaces(JNIEnv *env, w_instance this) {
  w_thread thread = JNIEnv2w_thread(env);
  w_int   i;
  w_instance Array;
  w_instance exception;
  w_clazz clazz = Class2clazz(this);
  w_clazz interfaze;
  w_int   length;

  exception = NULL;
  threadMustBeSafe(thread);
  if (mustBeReferenced(clazz) == CLASS_LOADING_FAILED) {

    return NULL;

  }

  woempa(7, "getInterfaces of instance of %k, %d interfaces.\n", clazz, clazz->numDirectInterfaces);

  length = clazz->numDirectInterfaces;
  enterUnsafeRegion(thread);
  Array = allocArrayInstance_1d(thread, clazzArrayOf_Class, length);
  enterSafeRegion(thread);

  if (Array) {
    for (i = 0; i < (w_int)clazz->numDirectInterfaces; i++) {
      interfaze = clazz->interfaces[i];
      clazz2Class(interfaze);
      woempa(7, "--> interface %d = %k.\n", i, interfaze);
      setArrayReferenceField(Array, clazz2Class(interfaze), i);
    }
  }

  return Array;

}

/*
** newInstance0() creates a new instance of this class, using the default
** initializer.  Throws InstantiationException if this class is an
** interface, abstract class, array class, primitive class, or void, or
** if this class has no default initializer. Throws IllegalAccessException
** if the calling method is not allowed to invoke the default initializer.
** Throws ExceptionInInitializerError if an exception is thrown by the 
** default initializer.
*/
w_instance Class_newInstance0(JNIEnv *env, w_instance this) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz clazz = Class2clazz(this);
  w_clazz calling_clazz;
  w_instance newInstance = NULL;
  w_frame frame;

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }

  if (isSet(clazz->flags, ACC_ABSTRACT | ACC_INTERFACE | CLAZZ_IS_PRIMITIVE) || clazz->dims > 0) {
    throwException(thread, clazzInstantiationException, "%k cannot be instantiated", clazz);
    return NULL;
  }

  if (clazz->defaultInit == NULL) {
    throwException(thread, clazzInstantiationException, "%k has no default intializer", clazz);
    return NULL;
  }

  calling_clazz = getCallingClazz(thread);
  
  if (!isAllowedToCall(calling_clazz,clazz->defaultInit, clazz)) {
    throwException(thread, clazzIllegalAccessException, "%K is not allowed to call %M", calling_clazz, clazz->defaultInit);
  }

  if (! exceptionThrown(thread) && mustBeInitialized(clazzFileDescriptor) != CLASS_LOADING_FAILED) {
    enterUnsafeRegion(thread);
    newInstance = allocInstance(thread, clazz);
    enterSafeRegion(thread);
  }

  if (newInstance) {
    frame = activateFrame(thread, clazz->defaultInit, FRAME_REFLECTION, 1, newInstance, stack_trace);
    if (exceptionThrown(thread)) {
      newInstance = NULL;
    }
    deactivateFrame(frame, newInstance);
  }

  return newInstance;

}

/*
** forName_S() finds a class with the given name, using the classloader which
** defined the calling class. The class found will be initialized.
*/
w_instance Class_forName_S(JNIEnv *env, w_instance thisClass, w_instance Classname) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz clazz;
  w_string classname;
  w_clazz  calling_clazz;
  w_instance loader;
  w_instance exception;

  if (! Classname) {
    throwException(thread, clazzNullPointerException, NULL);
    return NULL;
  }

  classname = String2string(Classname);
  calling_clazz = getCallingClazz(thread);
  
  loader = clazz2loader(calling_clazz);

  clazz = namedClassMustBeLoaded(loader, classname);
  exception = exceptionThrown(thread);

  if (exception || mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }

  return clazz2Class(clazz);

}

/*
** forName_SZCL() finds a class with the given name, using the given classloader.
** The class found will be initialized iff 'initialize' is true.
*/
w_instance Class_forName_SZCL(JNIEnv *env, w_instance thisClass, w_instance Classname, w_boolean initialize, w_instance Classloader) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz clazz;
  w_string classname;
  w_instance exception;

  if (! Classname) {
    throwException(thread, clazzNullPointerException, NULL);
    return NULL;
  }

  classname = String2string(Classname);
  woempa(1, "called with string %w initialize = %s, classloader %j.\n", classname,initialize?"true":"false", Classloader);

  clazz = namedClassMustBeLoaded(Classloader, classname);
  exception = exceptionThrown(thread);

  if (! clazz && !exception) {
    throwException(thread, clazzClassNotFoundException, "%w", classname);
    exception = exceptionThrown(thread);
  } 

  if (exception || !clazz || (initialize && mustBeInitialized(clazz) == CLASS_LOADING_FAILED) ){
    return NULL;
  }

  return clazz2Class(clazz);

}

/*
** get_constructors gets the constructors (PUBLIC or DECLARED, depending 
** on mtype) of this class. Note that constructors are not inherited.
*/
w_instance
Class_get_constructors
( JNIEnv *env, w_instance thisClass, w_int mtype
) {
  w_thread thread = JNIEnv2w_thread(env);
  w_clazz  clazz = Class2clazz(thisClass);
  w_size   i;
  w_size   numMethods = 0;
  w_int    numRelevantConstructors;
  w_method method;
  w_instance Constructor;
  w_instance Array;
  w_instance exception = NULL;
  w_clazz   clazzArrayOf_Constructor = getNextDimension(clazzConstructor, NULL);

  threadMustBeSafe(thread);
  mustBeInitialized(clazzConstructor);
  mustBeInitialized(clazzArrayOf_Constructor);

  if (clazz) {
    numMethods = clazz->numInheritableMethods;
    if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
      return NULL;
    }
  }

  numRelevantConstructors = 0;
  for (i = 0; i < clazz->numDeclaredMethods; ++i) {
    method = &clazz->own_methods[i];
    if (method->spec.name == string_angle_brackets_init
      && (mtype==DECLARED || isSet(method->flags, ACC_PUBLIC))
       ) {
      numRelevantConstructors += 1;
    }
  }

  enterUnsafeRegion(thread);
  Array = allocArrayInstance_1d(thread, clazzArrayOf_Constructor, numRelevantConstructors);

  if (Array) {
    numRelevantConstructors = 0;
    for (i = 0; i < clazz->numDeclaredMethods; ++i) {
      method = &clazz->own_methods[i];
      if (method->spec.name == string_angle_brackets_init
          && (mtype==DECLARED || isSet(method->flags, ACC_PUBLIC))
         ) {
        Constructor = allocInstance(JNIEnv2w_thread(env), clazzConstructor);
        if (!Constructor) {
          woempa(9, "Unable to allocate Constructor\n");
          break;
        }
        setWotsitField(Constructor, F_Constructor_wotsit, method);
        setArrayReferenceField(Array, Constructor, numRelevantConstructors++);
      }
    }
  }
  enterSafeRegion(thread);

  return Array;

}

static w_int addFieldsToFifo(w_clazz current_clazz, w_fifo fields_fifo, w_int mtype) {
  w_field field;
  w_int i;
  w_int relevant;
  for (i = 0; i < current_clazz->numFields; i++) {
    field = &current_clazz->own_fields[i];
    woempa(3, "%02d: field %w is %s, declared in %k\n",i,NM(field),
              isSet(field->flags, ACC_PUBLIC)?"public":"non-public", field->declaring_clazz);
    relevant = (mtype==PUBLIC && isSet(field->flags, ACC_PUBLIC))
             || (mtype==DECLARED && current_clazz->own_fields[i].declaring_clazz == current_clazz);

    woempa(3,"field[%d] %w is %srelevant\n",i,NM(field),relevant?"":"ir");
    if(relevant) {
      if(putFifo(field, fields_fifo) < 0) {
        releaseFifo(fields_fifo);
        return -1;
      }
    }
  }
  return 0;
}

/*
** get_fields gets the fields (PUBLIC or DECLARED, depending ** on mtype)
** of this class, and of its superclasses if mtype is PUBLIC.
*/
w_instance Class_get_fields ( JNIEnv *env, w_instance thisClass, w_int mtype) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz  clazz = Class2clazz(thisClass);
  w_int numRelevantFields;
  w_fifo fields;
  w_instance Array;
  w_instance exception = NULL;
  w_clazz   clazzArrayOf_Field = getNextDimension(clazzField, NULL);

  threadMustBeSafe(thread);
  mustBeInitialized(clazzField);
  mustBeInitialized(clazzArrayOf_Field);

  /*
  ** Find the number of appropriate fields first.  
  */

  woempa(1, "looking for %s fields of %K\n", mtype==PUBLIC ? "public" : "declared", clazz);

  fields = allocFifo((w_size)clazz->numFields);
  if(fields == NULL) {
    return NULL;
  }

  if(addFieldsToFifo(clazz, fields, mtype)) {
    return NULL;
  }

  if(mtype == PUBLIC) {
    if (isSet(clazz->flags,ACC_INTERFACE)) {
      int j;
      for (j = 0; j < clazz->numInterfaces; ++j) {
        if(addFieldsToFifo(clazz->interfaces[j],fields, PUBLIC)) {
          return NULL;
        }  
      }
    } else {
      w_clazz current_clazz = getSuper(clazz);
      while (current_clazz) {
        if(addFieldsToFifo(current_clazz,fields, PUBLIC)) {
          return NULL;
        }  
        current_clazz = getSuper(current_clazz);
      }
    }
  }
  
  numRelevantFields = fields->numElements;

  enterUnsafeRegion(thread);
  Array = allocArrayInstance_1d(thread, clazzArrayOf_Field, numRelevantFields);

  if (Array) {
    int i;

    for(i = 0; i < numRelevantFields ; i++) {
      w_field field = (w_field) getFifo(fields);
      w_instance Field;

      Field = allocInstance(JNIEnv2w_thread(env), clazzField);

      if (Field==NULL) {
        woempa(9, "Unable to allocate Field\n");
        break;
      }
      setWotsitField(Field, F_Field_wotsit, field);
      setArrayReferenceField(Array, Field, i);
    }
  }
  enterSafeRegion(thread);

  releaseFifo(fields);

  return Array;

}

/*
** get_methods gets the methods (PUBLIC or DECLARED, depending ** on mtype)
** of this class, and of its superclasses if mtype is PUBLIC. Methods of a
** superclass are not included if they are overridden. Constructors and
** static intializers (<init> and <clinit>) are excluded.
**
** If this class is an interface then the methods returned for mtype PUBLIC
** are those of this interface and all of its superinterfaces (direct or
** indirect). No arrempt is made to remove duplicates. Whether this is the
** intended behaviour I have no idea.
*/
w_instance Class_get_methods(JNIEnv *env, w_instance thisClass, w_int mtype) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz  clazz = Class2clazz(thisClass);
  w_clazz  super;
  w_int  i;
  w_size j;
  w_int  numMethods = 0;
  w_int  numRelevantMethods;
  w_method method;
  w_instance Method;
  w_instance Array;
  w_fifo relevantMethods;
  w_clazz   clazzArrayOf_Method = getNextDimension(clazzMethod, NULL);

  threadMustBeSafe(thread);
  mustBeInitialized(clazzMethod);
  mustBeInitialized(clazzArrayOf_Method);

  if (clazz) {
    numMethods = clazz->numInheritableMethods;
    if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
      return NULL;
    }
  }

  /*
  ** Find the number of appropriate methods first. Leave out the 
  ** <clinit> and <init> methods for the class.
  */

  woempa(1, "Class %k type %s\n", clazz, mtype == PUBLIC ? "public" : "declared");
  relevantMethods = allocFifo((w_size)numMethods);
  if (! relevantMethods) {
    return NULL;
  }
  
  numRelevantMethods = 0;
  if (mtype == DECLARED) {
    woempa(1, "Type DECLARED: Scanning %K only\n", clazz);
    for (i = 0; i < (w_int)clazz->numDeclaredMethods; ++i) {
      method = &clazz->own_methods[i];
      if (string_char(method->spec.name, 0) != '<' && isNotSet(method->flags, METHOD_IS_MIRANDA | METHOD_IS_PROXY | METHOD_IS_SYNTHETIC)) {
        numRelevantMethods += 1;
        woempa(1, "  Method %d: %m\n",numRelevantMethods,method);
        if (putFifo(method,relevantMethods) < 0) {
          wprintf("No space to store relevant methods for Class/getMethods()\n");
          throwOutOfMemoryError(thread);
          return NULL;
        }
      }
    }
  }
  else if (mtype == PUBLIC) {
    if (isSet(clazz->flags, ACC_INTERFACE)) {
      woempa(1, "Type PUBLIC: Scanning %K and superinterfaces\n", clazz);
      for (i = 0; i < (w_int)clazz->numDeclaredMethods; ++i) {
        method = &clazz->own_methods[i];
        if (string_char(method->spec.name, 0) != '<' && isSet(method->flags, ACC_PUBLIC)) {
          numRelevantMethods += 1;
          woempa(1, "  Method %d: %m\n",numRelevantMethods,method);
          if (putFifo(method,relevantMethods) < 0) {
            wprintf("No space to store relevant methods for Class/getMethods()\n");
            throwOutOfMemoryError(thread);
            return NULL;
          }
        }
      }
      for (j = 0; j < clazz->numInterfaces; ++j) {
        super = clazz->interfaces[j];
        woempa(1, "               Scanning %K\n", super);
        for (i = 0; i < (w_int)super->numDeclaredMethods; ++i) {
          method = &super->own_methods[i];
          if (string_char(method->spec.name, 0) != '<' && isSet(method->flags, ACC_PUBLIC)) {
            numRelevantMethods += 1;
            woempa(1, "  Method %d: %m\n",numRelevantMethods,method);
            if (putFifo(method,relevantMethods) < 0) {
              wprintf("No space to store relevant methods for Class/getMethods()\n");
              throwOutOfMemoryError(thread);
              return NULL;
            }
          }
        }
      }
    }
    else {
      woempa(1, "Type PUBLIC: Scanning %K and superclasses\n", clazz);
      for (super = clazz; super; super = getSuper(super)) {
        woempa(1, "               Scanning %K\n", super);
        for (i = 0; i < (w_int)super->numDeclaredMethods; ++i) {
          method = &super->own_methods[i];
          if (string_char(method->spec.name, 0) != '<' && isSet(method->flags, ACC_PUBLIC) && isNotSet(method->flags, METHOD_IS_MIRANDA) && virtualLookup(method, clazz) == method) {
            numRelevantMethods += 1;
            woempa(1, "  Method %d: %m\n",numRelevantMethods,method);
            if (putFifo(method,relevantMethods) < 0) {
              wprintf("No space to store relevant methods for Class/getMethods()\n");
              throwOutOfMemoryError(thread);
              return NULL;
            }
          }
        }
      }
    }
  }

  woempa(1,"Class %k has %d relevant methods\n", clazz,numRelevantMethods);
  enterUnsafeRegion(thread);
  Array = allocArrayInstance_1d(thread, clazzArrayOf_Method, numRelevantMethods);

  if (Array) {
    for (i = 0; i < numRelevantMethods; i++) {
      method = getFifo(relevantMethods);
      Method = allocInstance(thread, clazzMethod);
      if (Method == NULL) {
        woempa(9, "Unable to allocate Method\n");
        break;
      }
      woempa(1, "Method %d: %m\n", i + 1, method);
      setWotsitField(Method, F_Method_wotsit, method);
      setArrayReferenceField(Array, Method, i);
    }
  }
  enterSafeRegion(thread);

  releaseFifo(relevantMethods);

  return Array;

}

/*
** get_one_constructor looks for a constructor with the arguments specified
** in AParameters, either in this class (mtype DECLARED) or in this class
** and all its superclasses (mtype PUBLIC).
*/
w_instance Class_get_one_constructor(JNIEnv *env, w_instance thisClass, w_instance AParameters, w_int mtype) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz      clazz = Class2clazz(thisClass);
  w_instance   Constructor;
  w_method     constructor = NULL;
  w_MethodSpec spec;
  w_size       i;
  w_size       nargs;
  w_instance  *arg_Classes;

  mustBeInitialized(clazzConstructor);
  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }

#ifdef RUNTIME_CHECKS
  if (!AParameters) {
    wabort(ABORT_WONKA, "This can't happen: java.lang.Class should replace a null argument by a zero-length array\n");
  }
#endif

  spec.declaring_clazz = Class2clazz(thisClass);
  arg_Classes = instance2Array_instance(AParameters);
  spec.name = string_angle_brackets_init;
  spec.return_type = clazz_void;

  if(AParameters == NULL || instance2Array_length(AParameters) == 0){
    woempa(1, "Constructor should have name %w and 0 parameters\n", spec.name);
    spec.arg_types = NULL;
  }
  else {
    nargs = instance2Array_length(AParameters);
    woempa(1, "Constructor should have name %w and %d parameters\n", spec.name, nargs);
    spec.arg_types = allocMem((nargs + 1) * sizeof(w_clazz));
    if (!spec.arg_types) {
      wabort(ABORT_WONKA, "Unable to allocate spec.arg_types\n");
    }
    for (i = 0; i < nargs; ++i) {
      spec.arg_types[i] = Class2clazz(arg_Classes[i]);
      woempa(1, "  Arg[%d] should have type %k\n", i, spec.arg_types[i]);
    }
    spec.arg_types[nargs] = NULL;
  }

  for (i = 0; i < clazz->numDeclaredMethods; ++i) {
    w_method m = &clazz->own_methods[i];

    woempa(1, "Candidate: %w\n", m->spec.name);
    if (methodMatchesSpec(m, &spec) && (mtype == DECLARED || isSet(m->flags, ACC_PUBLIC))) {
      constructor = m;
      break;
    }
  }

  if (spec.arg_types) {
    releaseMem(spec.arg_types);
  }

  if (constructor) {
    woempa(1,"Found %m\n",constructor);
    if (mtype==PUBLIC && isNotSet(constructor->flags, ACC_PUBLIC)) {
      woempa(9,"Aw shucks: constructor is not public\n");
      constructor = NULL;
    }
    else if (mtype==DECLARED && constructor->spec.declaring_clazz!=clazz) {
      woempa(1,"Aw shucks: constructor is declared in %k not %k\n", constructor->spec.declaring_clazz, clazz);
      constructor = NULL;
    }
  }

  if (constructor) {
    enterUnsafeRegion(thread);
    Constructor = allocInstance(JNIEnv2w_thread(env), clazzConstructor);
    enterSafeRegion(thread);
    if (Constructor == NULL) {
      woempa(9, "Unable to allocate Constructor\n");
      return NULL;
    }
    setWotsitField(Constructor, F_Constructor_wotsit, constructor);
    return Constructor;
  }
  else {
    woempa(9, "Did not find correct constructor!\n");
    throwException(thread, clazzNoSuchMethodException, NULL);
    return NULL;
  }

}

w_field seekField (w_clazz clazz, w_string name, int mtype) {
  w_field  candidate;
  w_field  result = NULL;
  w_size    i;

  if (mustBeReferenced(clazz) == CLASS_LOADING_FAILED) {

    return NULL;

  }

/*
** 1. Seek field declared in class itself
*/
  woempa(1,"looking for %s field %w in class %k\n",mtype==DECLARED?"declared":"public",name, clazz);

  for (i = 0; i < clazz->numFields; i++) {
    candidate = &clazz->own_fields[i];
    woempa(1,"looking at field %w: %s declared in class %k\n",NM(candidate),isSet(candidate->flags, ACC_PUBLIC)?"public":"", clazz);
    if (candidate->name == name
        && (mtype == DECLARED || isSet(candidate->flags, ACC_PUBLIC))
        && (mtype == PUBLIC || candidate->declaring_clazz == clazz)
      ) {
      result = candidate;
      break;
    }
  }

  if (result || mtype == DECLARED) {

    return result;

  }

/*
** 2. Seek field declared in the superinterfaces
*/

  for (i=0; !result && i<clazz->numInterfaces; i++) {
    woempa(1, " --> trying superinterface %x\n", clazz->interfaces[i]);
    result = seekField(clazz->interfaces[i],name,mtype);
  }

/*
** 3. Seek field declared in the superclass
*/

  if (!result && clazz->supers) {
    w_size j;
    for (j = 0; !result && j < clazz->numSuperClasses; ++j) {
      woempa(1, " --> trying superclass[%d] = %k\n", j, clazz->supers[j]);
      result = seekField(clazz->supers[j],name,mtype);
    }
  }

  return result;
}

w_instance Class_get_one_field(JNIEnv *env, w_instance thisClass, w_instance fieldNameString, w_int mtype) {

  w_thread   thread = JNIEnv2w_thread(env);
  w_clazz    clazz = Class2clazz(thisClass);
  w_string   fieldName;
  w_field    field;
  w_instance Field;

  mustBeInitialized(clazzField);

  if (fieldNameString == NULL){
    throwException(thread, clazzNoSuchFieldException, NULL);
    return NULL;
  }

  fieldName = String2string(fieldNameString);
  if (mustBeReferenced(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }

  woempa(1, "Looking for %w in %k\n", fieldName, clazz);
  field = seekField(clazz, fieldName, mtype);

  if (field) {
    enterUnsafeRegion(thread);
    Field = allocInstance(JNIEnv2w_thread(env), clazzField);
    enterSafeRegion(thread);
    if (!Field) {
      woempa(9, "Unable to allocate Constructor\n");
    }
    else {
      setWotsitField(Field, F_Field_wotsit, field);
    }
    return Field;
  }
  
  woempa(9,"Couldn't find field %w in class %k\n",fieldName, clazz);
  throwException(thread, clazzNoSuchFieldException, "%w", fieldName);

  return NULL;

}

w_instance Class_get_one_method(JNIEnv *env, w_instance thisClass, w_instance methodNameString, w_instance AParameters, w_int mtype) {

  w_thread   thread = JNIEnv2w_thread(env);
  w_clazz    clazz = Class2clazz(thisClass);
  w_clazz    super;
  w_string   method_name;
  w_method   method = NULL;
  w_instance Method;
  w_MethodSpec spec;
  w_size     i;
  w_size     j;
  w_size     nargs;
  w_instance *arg_Classes;

  mustBeInitialized(clazzMethod);

#ifdef RUNTIME_CHECKS
  if (!AParameters) {
    wabort(ABORT_WONKA, "This can't happen: java.lang.Class should replace a null argument by a zero-length array\n");
  }
#endif


  if(methodNameString == NULL){
    throwException(thread, clazzNoSuchFieldException,"null");
    return NULL;
  }

  method_name = String2string(methodNameString);

  if(method_name == string_angle_brackets_init || method_name == string_angle_brackets_clinit) {
    woempa(1,"NoSuchMethod '%w' in %k\n",method_name,clazz);
    throwException(thread, clazzNoSuchMethodException, "cannot use getMethod/getDeclaredMethod to access %w", method_name);
    return NULL;
  }

  if(isSet(clazz->flags, CLAZZ_IS_PRIMITIVE) || clazz->dims>0){
    throwException(thread, clazzNoSuchMethodException, "cannot use getMethod/getDeclaredMethod to search an array or primitive class such as %k", clazz);
    return NULL;
  }

  if (mustBeReferenced(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }

  spec.declaring_clazz = Class2clazz(thisClass);
  arg_Classes = instance2Array_instance(AParameters);
  spec.name = method_name;
  spec.return_type = NULL;

  if(AParameters == NULL || instance2Array_length(AParameters) == 0){
    woempa(1, "Method should have name %w and 0 parameters\n", spec.name);
    spec.arg_types = NULL;
  }
  else {
    nargs = instance2Array_length(AParameters);
    woempa(1, "Method should have name %w and %d parameters\n", spec.name, nargs);
    spec.arg_types = allocMem((nargs + 1) * sizeof(w_clazz));
    if (!spec.arg_types) {
      wabort(ABORT_WONKA, "Unable to allocate spec.arg_types\n");
    }
    for (i = 0; i < nargs; ++i) {
      spec.arg_types[i] = Class2clazz(arg_Classes[i]);
      woempa(1, "  Arg[%d] should have type %k\n", i, spec.arg_types[i]);
    }
    spec.arg_types[nargs] = NULL;
  }

  woempa(1,"Seeking  method %w in %K\n", method_name, spec.declaring_clazz);
 
  for (i = 0; i < clazz->numDeclaredMethods; ++i) {
    w_method m = &clazz->own_methods[i];

    woempa(1, "Candidate: %w\n", m->spec.name);
    if (methodMatchesSpec(m, &spec) && (mtype == DECLARED || isSet(m->flags, ACC_PUBLIC))) {
      woempa(1, "Parameters match, %s\n", method ? "see if return type is more specific" : "first match found");
      if (!method || (isNotSet(method->flags, METHOD_IS_MIRANDA) && isAssignmentCompatible(m->spec.return_type, method->spec.return_type))) {
        method = m;
      }
    }
  }

  if (mtype == PUBLIC) {
    if (!method) {
      for (j = 0; j < clazz->numSuperClasses; ++j) {
        super = clazz->supers[j];
        woempa(1, "Seek %w in %K\n", method_name, super);
        for (i = 0; i < super->numDeclaredMethods; ++i) {
          w_method m = &super->own_methods[i];

          woempa(1, "Candidate: %w\n", m->spec.name);
          if (methodMatchesSpec(m, &spec) && isSet(m->flags, ACC_PUBLIC)) {
            woempa(1, "Parameters match, %s\n", method ? "see if return type is more specific" : "first match found");
           if (!method || (isNotSet(method->flags, METHOD_IS_MIRANDA) && isAssignmentCompatible(m->spec.return_type, method->spec.return_type))) {
              method = m;
            }
          }
        }
      }
    }

    if (!method) {
      for (j = 0; j < clazz->numInterfaces; ++j) {
        super = clazz->interfaces[j];
        woempa(1, "Seek %w in %K\n", method_name, super);
        for (i = 0; i < super->numDeclaredMethods; ++i) {
          w_method m = &super->own_methods[i];

          woempa(1, "Candidate: %w\n", m->spec.name);
          if (methodMatchesSpec(m, &spec) && isSet(m->flags, ACC_PUBLIC)) {
            woempa(1, "Parameters match, %s\n", method ? "see if return type is more specific" : "first match found");
           if (!method || (isNotSet(method->flags, METHOD_IS_MIRANDA) && isAssignmentCompatible(m->spec.return_type, method->spec.return_type))) {
              method = m;
            }
          }
        }
      }
    }
  }

  if (spec.arg_types) {
    releaseMem(spec.arg_types);
  }

  if (method) {
    woempa(1,"Found %m\n",method);
    if (mtype==PUBLIC && isNotSet(method->flags, ACC_PUBLIC)) {
      woempa(9,"Aw shucks: method is not public\n");
      method = NULL;
    }
    else if (mtype==DECLARED && method->spec.declaring_clazz!=clazz) {
      woempa(9,"Aw shucks: method is declared in %K not %K\n", method->spec.declaring_clazz, clazz);
      method = NULL;
    }
  }

  if (method) {
    enterUnsafeRegion(thread);
    Method = allocInstance(JNIEnv2w_thread(env), clazzMethod);
    enterSafeRegion(thread);
    if (Method == NULL) {
      woempa(9, "Unable to allocate Method\n");
      return NULL;
    }
    setWotsitField(Method, F_Method_wotsit, method);
    return Method;
  }
  else {
    woempa(1, "Did not find method %w in %K!\n", method_name, clazz);
    throwException(thread, clazzNoSuchMethodException, "%w", method_name);
    return NULL;
  }

}

/*
** getComponentType() uses clazz->previousDimension.
*/
w_instance Class_getComponentType(JNIEnv *env, w_instance thisClass) {
  w_clazz clazz = Class2clazz(thisClass);
  w_instance result = NULL;

  if (clazz && clazz->dims) {
    result = clazz2Class(clazz->previousDimension);
  }

  return result;
    
}

/*
** getModifiers() just masks clazz->flags to remove our Wonka flags and the
** dreaded ambiguous ACC_SYNCwhatever flag.
*/
w_int Class_getModifiers(JNIEnv *env, w_instance Class) {
  w_clazz clazz = Class2clazz(Class);
  w_word flags;
  int i;

  if (mustBeReferenced(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }
  for (i = 0; i < clazz->temp.inner_class_info_count; ++i) {
    if (clazz->temp.inner_class_info[i].inner_class_info_index == clazz->temp.this_index) {
      return clazz->temp.inner_class_info[i].inner_class_access_flags;
    }
  }

  flags = Class2clazz(Class)->flags;

  woempa(1, "Class %k has modifiers %s %s %s %s %s %s (0x%08x).\n", Class2clazz(Class), isSet(flags,ACC_PUBLIC)?"public":"",isSet(flags,ACC_PRIVATE)?"private":"",isSet(flags,ACC_PROTECTED)?"protected":"",isSet(flags,ACC_ABSTRACT)?"abstract":"",isSet(flags,ACC_FINAL)?"final":"",isSet(flags,ACC_INTERFACE)?"interface":"",flags & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED | ACC_ABSTRACT | ACC_FINAL));

  return (w_int)(flags & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED | ACC_ABSTRACT | ACC_FINAL | ACC_INTERFACE));
  
}

w_instance Class_getDeclaringClass(JNIEnv *env, w_instance Class) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_clazz clazz = Class2clazz(Class);
  int i;

  if (mustBeReferenced(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }
  for (i = 0; i < clazz->temp.inner_class_info_count; ++i) {
    if (clazz->temp.inner_class_info[i].inner_class_info_index == clazz->temp.this_index) {
      int j = clazz->temp.inner_class_info[i].outer_class_info_index;
      w_clazz outer_clazz = getClassConstant(clazz, j, thread);
      if (exceptionThrown(thread)) {

        return NULL;

      }

      return clazz2Class(outer_clazz);
    }
  }

  return NULL;
}
w_instance Class_getDeclaredClasses0(JNIEnv *env, w_instance Class) {
  w_thread thread = JNIEnv2w_thread(env);
  w_clazz clazz = Class2clazz(Class);
  w_clazz *inner_clazz = allocMem(clazz->temp.inner_class_info_count * sizeof(w_clazz));
  w_instance Array;
  int i;
  int n = 0;

  threadMustBeSafe(thread);
  if (mustBeReferenced(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }
  for (i = 0; i < clazz->temp.inner_class_info_count; ++i) {
    if (clazz->temp.inner_class_info[i].outer_class_info_index == clazz->temp.this_index) {
      int j = clazz->temp.inner_class_info[i].inner_class_info_index;

      inner_clazz[n++] = getClassConstant(clazz, j, thread);
      if (exceptionThrown(thread)) {

        return NULL;

      }
    }
  }

  enterUnsafeRegion(thread);
  Array = allocArrayInstance_1d(thread, clazzArrayOf_Class, n);
  if (Array) {
    for (i = 0; i < n; ++i) {
      setArrayReferenceField(Array, clazz2Class(inner_clazz[i]), i);
    }
  }
  enterSafeRegion(thread);

  releaseMem(inner_clazz);

  return Array;
}

w_instance Class_getClasses0(JNIEnv *env, w_instance Class) {
  w_thread thread = JNIEnv2w_thread(env);
  w_clazz clazz = Class2clazz(Class);
  w_clazz super = clazz;
  w_fifo inner_clazz_fifo = allocFifo(511);
  w_instance Array;
  int i;
  int j = 0;
  int n = 0;

  threadMustBeSafe(thread);
  if (mustBeReferenced(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }


  while (super) {
    for (i = 0; i < super->temp.inner_class_info_count; ++i) {
      if (super->temp.inner_class_info[i].outer_class_info_index == super->temp.this_index) {
        w_clazz inner_clazz = getClassConstant(super, super->temp.inner_class_info[i].inner_class_info_index, thread);

        if (exceptionThrown(thread)) {
          releaseFifo(inner_clazz_fifo);

          return NULL;

        }

        if (isSet(super->temp.inner_class_info[i].inner_class_access_flags, ACC_PUBLIC)) {
          if (putFifo(inner_clazz, inner_clazz_fifo) < 0) {
            wprintf("No space to store inner class for Class/getClasses()\n");
            throwOutOfMemoryError(thread);
            releaseFifo(inner_clazz_fifo);

            return NULL;

          }
          ++n;
        }
      }
    }

    if (isSet(clazz->flags, ACC_INTERFACE)) {
      super = (j < clazz->numInterfaces ? clazz->interfaces[j] : NULL);
      ++j;
    }
    else {
      super = getSuper(super);
    }
  }

  enterUnsafeRegion(thread);
  Array = allocArrayInstance_1d(thread, clazzArrayOf_Class, n);
  if (Array) {
    for (i = 0; i < n; ++i) {
      setArrayReferenceField(Array, clazz2Class((w_clazz)getFifo(inner_clazz_fifo)), i);
    }
  }
  enterSafeRegion(thread);

  releaseFifo(inner_clazz_fifo);

  return Array;
}


