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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004, 2006 by Chris Gray, /k/ Embedded Java *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: initialize.c,v 1.13 2006/10/04 14:24:16 cvsroot Exp $
*/

#include <string.h>

#include "checks.h"
#include "clazz.h"
#include "constant.h"
#include "exception.h"
#include "fields.h"
#include "interpreter.h"
#include "loading.h"
#include "methods.h"
#include "reflection.h"
#include "verifier.h"
#include "wonka.h"

void initializeStaticFields(w_thread thread, w_clazz clazz) {

  w_size  i;
  w_field field;
  w_ConstantType *c;

  woempa(1, "Setting the static fields of '%k' to default values.\n", clazz);

  for (i = 0; i < clazz->numStaticFields; i++) {
    field = &clazz->own_fields[i];
    if (field->initval) {
      c = &clazz->tags[field->initval];
      switch (*c) {
        case CONSTANT_STRING:
        case RESOLVED_STRING:
          {
            w_instance instance = getStringConstant(clazz, field->initval);

            if (thread) {
              enterUnsafeRegion(thread);
            }
            clazz->staticFields[field->size_and_slot] = (w_word) instance;
            if (thread) {
              enterSafeRegion(thread);
            }
          }
          woempa(1, "Initialized static field '%w' (type %k) of '%k' to 0x%08x.\n", NM(field), field->value_clazz, clazz, clazz->staticFields[field->size_and_slot]);
          break;
      
        case CONSTANT_INTEGER:
          clazz->staticFields[field->size_and_slot] = getIntegerConstant(clazz, field->initval);
          woempa(1, "Initialized static field '%w' (type %k) of '%k' to 0x%08x.\n", NM(field), field->value_clazz, clazz, clazz->staticFields[field->size_and_slot]);
          break;

        case CONSTANT_FLOAT:
          clazz->staticFields[field->size_and_slot] = getFloatConstant(clazz, field->initval);
          woempa(1, "Initialized static field '%w' (type %k) of '%k' to 0x%08x.\n", NM(field), field->value_clazz, clazz, clazz->staticFields[field->size_and_slot]);
          break;

        case CONSTANT_LONG:
          {
            w_u64 u64 = getLongConstant(clazz, field->initval);

            clazz->staticFields[field->size_and_slot] = u64.words[0];
            clazz->staticFields [ field->size_and_slot + 1] = u64.words[1];
            woempa(1, "Initialized static field '%w' (type %k) of '%k' to 0x%08x%08x.\n", NM(field), field->value_clazz, clazz,clazz->staticFields[field->size_and_slot+WORD_MSW],clazz->staticFields[field->size_and_slot+WORD_LSW]);
          }
          break;

        case CONSTANT_DOUBLE:
          {
            w_u64 u64 = getDoubleConstant(clazz, field->initval);

/* CG 20040101 WAS:
            clazz->staticFields[field->size_and_slot] = u64.words[WORD_LSW];
            clazz->staticFields [ field->size_and_slot + 1] = u64.words[WORD_MSW];
*/
            clazz->staticFields[field->size_and_slot] = u64.words[0];
            clazz->staticFields [ field->size_and_slot + 1] = u64.words[1];
            woempa(1, "Initialized static field '%w' (type %k) of '%k' to 0x%08x%08x.\n", NM(field), field->value_clazz, clazz, clazz->staticFields[field->size_and_slot+WORD_MSW], clazz->staticFields[field->size_and_slot+WORD_LSW]);
          }
          break;

        default:
          wabort(ABORT_WONKA, "Static field '%w' of %k has initializer with cnt_tag %d.\n", field->name, clazz, *c);
      }
    }
  }
}

static void doSuperConstructorHack(w_clazz clazz, w_method m) {
  w_size j;
  w_method super;

  if (isSet(m->flags, METHOD_IS_CONSTRUCTOR) && m->exec.code && (m->exec.code_length > 4) && (m->exec.code[0] == 0x2a) && (m->exec.code[1] == 0xb7)) {
    woempa(7, "First byte of %M is 0x2a (aload_0), second is 0xb7 (invokespecial)\n", m);
    j = (unsigned char)m->exec.code[2];
    j = (j << 8) | (unsigned char)m->exec.code[3];
    super = getMethodConstant(clazz, j);
    if (!super) {
      woempa(9, "Couldn't resolve method called using invokespecial at start of %M !\n", m);

      return;

    }
    woempa(7, "Method being called is %M with code length %d\n", super, super->exec.code_length);
    if (super->exec.code_length) {
      j = 0;
      if (super->exec.code[0] == 0xa7) {
        j = (super->exec.code[1] << 8) | super->exec.code[2];
        woempa(7, "  begins with a jump to pc %d, opcode = 0x%02x\n", j, super->exec.code[j]);
      }
      if (super->exec.code[j] == 0xb1) {
        woempa(7, "Removing call from %M to trivial constructor %M :)\n", m, super);
        m->exec.code[0] = 0xa7; // j_goto
        m->exec.code[1] = 0;
        m->exec.code[2] = 4;
        m->exec.code[3] = 0;    // nop
      }
    }
  }
}

/*
**   - if the class is not abstract, it is not allowed to contain any
**     abstract methods.
**   - identify the static initializer, default initializer <init>()V, and
**     finalizer() (if any) and record their addresses.
*/

static void scanMethods(w_clazz clazz) {
  w_method m;
  w_size   i;

  for (i = 0; i < clazz->numDeclaredMethods; i++) {
    m = &clazz->own_methods[i];

    if (m->spec.name == string_angle_brackets_clinit && m->spec.arg_types == NULL) {
      woempa(1, "Class %k has static initializer %m\n", clazz, m);
      clazz->clinit = m;
    }
    else if (m->spec.name == string_angle_brackets_init && m->spec.arg_types == NULL) {
      woempa(1, "Class %k has default constructor %m\n", clazz, m);
      clazz->defaultInit = m;
    }
    else if (m->spec.name == string_run && m->spec.arg_types == NULL) {
      woempa(1, "Class %k has run method %m\n", clazz, m);
      clazz->runner = m;
    }
    doSuperConstructorHack(clazz, m);
  }

  if (isNotSet(clazz->flags, ACC_INTERFACE)) {
    m = virtualLookup(finalize_method, clazz);
    // We only record a finalizer if it is not the one declared in
    // java.lang.Object and is not a bytecode method consisting of "vreturn".
    if (m->spec.declaring_clazz != clazzObject && !(m->exec.code && m->exec.code_length == 1)) {
      woempa(1, "Class %k has finalizer %m\n", clazz, m);
      setFlag(clazz->flags, CLAZZ_HAS_FINALIZER);
    }
  }

  if (pedantic) {
    if (isNotSet(clazz->flags, ACC_ABSTRACT)) {
      for (i = 0; i < clazz->numInheritableMethods; i++) {
        m = clazz->vmlt[i];

        if (isSet(m->flags, ACC_ABSTRACT)) {
          woempa(1,"AbstractMethodError detected: non-abstract class %k fails to implement %M\n",clazz, m);
          throwException(currentWonkaThread, clazzAbstractMethodError, "%w", clazz->dotified);

          return;

        }
      }
    }
  }

  if (!clazz->runner && clazz->supers) {
    clazz->runner = clazz->supers[0]->runner;
  }
}

/*
** After executing a class's <clinit> method, throw away the code etc..
*/
static void cleanUpClinit(w_method clinit) {
  woempa(1, "Cleaning up %M\n", clinit);
  // TODO: set the dispatcher/function to some kind of trap
  if (clinit->exec.code_length) {
    woempa(1, "  Throwing away code array %p\n", clinit->exec.code);
    releaseMem(clinit->exec.code - 4);
    clinit->exec.code_length = 0;
    clinit->exec.code = NULL;
  }
  if (clinit->exec.exceptions) {
    woempa(1, "  Throwing away exception table %p\n", clinit->exec.exceptions);
    releaseMem(clinit->exec.exceptions);
    clinit->exec.numExceptions = 0;
    clinit->exec.exceptions = NULL;
  }
  if (clinit->exec.debug_info) {
    if (clinit->exec.debug_info->localVars) {
      woempa(1, "  Throwing away local variable table %p\n", clinit->exec.debug_info->localVars);
      releaseMem(clinit->exec.debug_info->localVars);
      clinit->exec.debug_info->localVars = NULL;
    }
  }
}


/*
** Initialize a class.
** The result returned is CLASS_LOADING_xxxxx.
*/

w_int initializeClazz(w_clazz clazz) {

  w_thread   thread = currentWonkaThread;
  w_frame    frame;
  w_size     i;
  w_clazz    parent;
  w_fixup    fixup;
  w_int      result = CLASS_LOADING_DID_NOTHING;

// These two classes can be needed if things go wrong later, so just make
// sure that they are already initialized. We trust them not to throw an
// exception during initialization.
  if (clazz->clinit) {
    mustBeInitialized(clazzExceptionInInitializerError);
    mustBeInitialized(clazzAbstractMethodError);
  }

  parent = clazz;
  for (i = clazz->dims; i && !classIsInitialized(parent->previousDimension); --i) {
    parent = parent->previousDimension;
  }
  for (; i < clazz->dims; ++i) {
    result |= mustBeInitialized(parent);
    if (result == CLASS_LOADING_FAILED) {

      return CLASS_LOADING_FAILED;

    }
    parent = parent->nextDimension;
  }

#ifdef USE_BYTECODE_VERIFIER
  /*
  ** If we are going to run this class through a bytecode verifier, now is a 
  ** good time to resolve all the Class, Field, and Method constants. Otherwise
  ** we'll have to do so in the middle of verification, which sucks.
  ** (And which locks dead, to the point more).
  */
  if (clazzShouldBeVerified(clazz)) {
    result = verifyClazz(clazz);
  }

  if (result == CLASS_LOADING_FAILED || exceptionThrown(thread)) {

      return CLASS_LOADING_FAILED;

  }
#endif

  woempa(1, "Thread %t: initializing class %k.\n", thread, clazz);
  setClazzState(clazz, CLAZZ_STATE_INITIALIZING);

  if ((isNotSet(clazz->flags, ACC_INTERFACE)) && (clazz->numSuperClasses > 1)) {
    for (i = clazz->numSuperClasses - 1; i > 0; --i) {
      woempa(1, "Initializing superclass[%d] (%k) of %k\n", i - 1, clazz->supers[i - 1], clazz);
      result |= mustBeInitialized(clazz->supers[i - 1]);
      if (result == CLASS_LOADING_FAILED) {

        return CLASS_LOADING_FAILED;

      }
    }
  }

  /*
  ** Fixup the clazz for WNI calls.
  ** No need to lock fixup2_hashtable, as all writes preceded all reads
  */
  fixup = (w_fixup)ht_read_no_lock(fixup2_hashtable,(w_word) clazz->dotified);
  if (fixup) {
    woempa(7,"fixup2 for %k = %p\n", clazz,fixup);
    fixup(clazz);
  }

  /*
  ** Scan the method pool to check that all abstract classes are implemented
  ** and to identify special methods such as <init>()V, run()V, finalize()V.
  */
  scanMethods(clazz);


  if (!exceptionThrown(thread)) {
    if (clazz->numStaticWords > 0) {
      woempa(1, "Initializing static fields of %k : %d words\n", clazz, clazz->numStaticWords);
      clazz->staticFields = allocClearedMem(clazz->numStaticWords * sizeof(w_word));
      if (!clazz->staticFields) {
        wabort(ABORT_WONKA, "Unable to allocate clazz->staticFields\n");
      }
    }

    if (!clazz->dims) {
      initializeStaticFields(thread, clazz);
      if (clazz->clinit) {
        woempa(1, "Class %k has <clinit> method %m, so let's run it.\n", clazz, clazz->clinit);
        frame = activateFrame(thread, clazz->clinit, FRAME_CLINIT, 0);
        deactivateFrame(frame, NULL);
        cleanUpClinit(clazz->clinit);
        //clazz->clinit = NULL;
      }
    }
  }

  if (exceptionThrown(thread)) {
    w_instance exception = exceptionThrown(thread);

    if(isAssignmentCompatible(instance2object(exception)->clazz, clazzException)) {
      wrapException(thread,clazzExceptionInInitializerError, F_Throwable_cause);
    }

    woempa(9, "<clinit> for %k did NOT terminate normally. Spreading the rumour...\n",  clazz);

    return CLASS_LOADING_FAILED;
  }

  woempa(1, "Initialisation of %k terminated normally. Shouting it out...\n",  clazz);

  return CLASS_LOADING_SUCCEEDED;
}

w_int mustBeInitialized(w_clazz clazz) {
  w_thread thread = currentWonkaThread;
  w_int    i;
  w_int    n;
  w_int    state = getClazzState(clazz);
  w_int    result = CLASS_LOADING_DID_NOTHING;
  x_status monitor_status;

  threadMustBeSafe(thread);

  if (exceptionThrown(thread)) {
    woempa(9, "Eh? Exception '%e' already pending in mustBeInitialized(%K)\n", exceptionThrown(thread), clazz);

    return CLASS_LOADING_FAILED;

  }

  switch (state) {
  case CLAZZ_STATE_UNLOADED:
  case CLAZZ_STATE_LOADING:
    wabort(ABORT_WONKA, "%K must be loaded before it can be Initialized\n", clazz);

  case CLAZZ_STATE_LOADED:
  case CLAZZ_STATE_SUPERS_LOADING:
  case CLAZZ_STATE_SUPERS_LOADED:
  case CLAZZ_STATE_REFERENCING:
  case CLAZZ_STATE_REFERENCED:
  case CLAZZ_STATE_LINKING:
    result = mustBeLinked(clazz);
    if (result == CLASS_LOADING_FAILED) {
      return CLASS_LOADING_FAILED;
    }
    break;

  case CLAZZ_STATE_VERIFYING:
  case CLAZZ_STATE_VERIFIED:
    wabort(ABORT_WONKA, "Class state VERIFYING/VERIFIED doesn't exist yet!");

  case CLAZZ_STATE_LINKED:
    break;

  case CLAZZ_STATE_INITIALIZING:
    if (clazz->resolution_thread == thread) {

      return CLASS_LOADING_DID_NOTHING;

    }
    break;

  case CLAZZ_STATE_INITIALIZED:

    return CLASS_LOADING_DID_NOTHING;

  default: // broken/garbage class
  // TODO - is the right thing to throw?
    throwException(thread, clazzNoClassDefFoundError, "%k : %w", clazz, clazz->failure_message);

    return CLASS_LOADING_FAILED;

  }

  x_monitor_eternal(clazz->resolution_monitor);
  state = getClazzState(clazz);

  while(state == CLAZZ_STATE_INITIALIZING) {
    monitor_status = x_monitor_wait(clazz->resolution_monitor, CLASS_STATE_WAIT_TICKS);
    if (monitor_status == xs_interrupted) {
      x_monitor_eternal(clazz->resolution_monitor);
    }
    state = getClazzState(clazz);
  }

  if (state == CLAZZ_STATE_LINKED) {
    woempa(7, "Initializing %K\n", clazz);
    clazz->resolution_thread = thread;
    setClazzState(clazz, CLAZZ_STATE_INITIALIZING);
    x_monitor_exit(clazz->resolution_monitor);

    n = clazz->numSuperClasses;
    woempa(1, "%K has %d superclasses\n", clazz, n);
    for (i = n - 1; i >= 0; --i) {
      result = mustBeInitialized(clazz->supers[i]);
      if (result != CLASS_LOADING_SUCCEEDED) {

        break;

      }
    }

    if (result != CLASS_LOADING_FAILED) {
      result = initializeClazz(clazz);
    }

    x_monitor_eternal(clazz->resolution_monitor);
    if (result == CLASS_LOADING_FAILED) {
      setClazzState(clazz, CLAZZ_STATE_BROKEN);
      x_monitor_notify_all(clazz->resolution_monitor);
      x_monitor_exit(clazz->resolution_monitor);

      return result;

    }

    if(exceptionThrown(thread)) {
      setClazzState(clazz, CLAZZ_STATE_BROKEN);
      result = CLASS_LOADING_FAILED;
    }
    else {
      setClazzState(clazz, CLAZZ_STATE_INITIALIZED);
    }
    x_monitor_notify_all(clazz->resolution_monitor);
  }
  else if (state == CLAZZ_STATE_BROKEN) {
    x_monitor_exit(clazz->resolution_monitor);

    return CLASS_LOADING_FAILED;

  }

  x_monitor_exit(clazz->resolution_monitor);

  return result;
}

