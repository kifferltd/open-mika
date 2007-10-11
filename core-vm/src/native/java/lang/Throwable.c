/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2006 by Chris Gray, /k/ Embedded Java         *
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

