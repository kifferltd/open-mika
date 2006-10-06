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
** $Id: Throwable.c,v 1.11 2006/10/04 14:24:16 cvsroot Exp $
*/

#include <string.h>

//#include "Throwable.h"
#include "core-classes.h"
#include "exception.h"
#include "fields.h"
#include "ts-mem.h"
#include "wstrings.h"
#include "heap.h"
#include "jni.h"
#include "loading.h"
#include "descriptor.h"
#include "methods.h"

/*
** Clean up any stack trace when a Throwable is garbage-collected.
*/
void Throwable_destructor(w_instance thisThrowable) {

  w_Exr * records = getWotsitField(thisThrowable, F_Throwable_records);
  
  if (records) {
    clearWotsitField(thisThrowable, F_Throwable_records);
    woempa(5, "Destroying trace record for %j\n", thisThrowable);
    releaseMem(records);
  }

}

static const w_size bufsize = 511;

/*
** Format the first line of a stack trace (Throwable class and message),
** returning the result as a java.lang.String .
*/
w_int Throwable_getStackTraceLength(JNIEnv *env, w_instance thisThrowable) {
  w_Exr *records = getWotsitField(thisThrowable, F_Throwable_records);
  setWotsitField(thisThrowable, F_Throwable_frame, records);
  if (records) {
   return records[0].position;
  }
  return 0;
}

/* 
** Get the next line of a stack trace (returning it as a java.lang.String),
** and advance the `frame' field of the Throwable to the next record.
*/
void Throwable_nextStackTrace(JNIEnv *env, w_instance thisThrowable, w_instance stack) {
  w_exr current = getWotsitField(thisThrowable, F_Throwable_frame);
  w_exr next = current + 1;
  
  if(stack == NULL) {
   return;
  }

  if (current) {
    w_method method = current->method;
    w_clazz clazz = method->spec.declaring_clazz;
    w_int native = isSet(method->flags, ACC_NATIVE);
    w_int pc = current->pc;
    w_int line = code2line(method, method->exec.code+pc);

    setReferenceField(stack, getStringInstance(clazz->dotified), F_StackTraceElement_declaringClass);
    setReferenceField(stack, getStringInstance(method->spec.name), F_StackTraceElement_methodName);
    setBooleanField(stack, F_StackTraceElement_nativeM, native);
    setIntegerField(stack, F_StackTraceElement_lineNumber, line);
    if (clazz->filename) {
      setReferenceField(stack, getStringInstance(clazz->filename), F_StackTraceElement_fileName);
    }

    if (current->position > 0) {
      setWotsitField(thisThrowable, F_Throwable_frame, next);
    }
    else {
      clearWotsitField(thisThrowable, F_Throwable_frame);
    }
  }
}

/*
** Strip off the top `n' levels of a stack trace (e.g., in order to remove
** the frame created by fillInStackTrace() itself ...)
*/
static void stripStackTraceLevels(w_instance thisThrowable, w_int n) {
  w_Exr * current = getWotsitField(thisThrowable, F_Throwable_records);

  while (1) {
    current->method = (current+n)->method;
    current->pc = (current+n)->pc;
    current->position = (current+n)->position;
    if (current->position == 0) {
      break;
    }
    ++current;
  }
}

/*
** Delete the current contents of a Throwable and replace it by a stack trace
** reflecting the current stack (but excluding the call to fillInStackTrace()).
*/
w_instance Throwable_fillInStackTrace(JNIEnv *env, w_instance thisThrowable) {
  w_Exr * records = getWotsitField(thisThrowable, F_Throwable_records);
  
  if (records) {
    clearWotsitField(thisThrowable, F_Throwable_records);
    woempa(1, "Destroying existing trace record for %j\n", thisThrowable);
    releaseMem(records);
  }
  fillThrowable(JNIEnv2w_thread(env), thisThrowable);
  stripStackTraceLevels(thisThrowable, 1);

  return thisThrowable;
}

