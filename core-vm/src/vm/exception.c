/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
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
#include <stdarg.h>
#include <stdio.h>

#include "clazz.h"
#include "constant.h"
#include "exception.h"
#include "fifo.h"
#include "heap.h"
#include "interpreter.h"
#include "loading.h"
#include "oswald.h"
#include "Throwable.h"
#include "wstrings.h"
#include "threads.h"
#include "fields.h"
#include "ts-mem.h"
#include "methods.h"
#include "descriptor.h"
#include "core-classes.h"
#include "checks.h"

#define dumpStackTrace(thrown)

w_instance bootstrap_exception;

char * print_exception(char * buffer, int * remain, void * data, int w, int p, unsigned int f) {
  w_instance exception = data;
  w_instance message;
  w_int    nbytes;
  char    *temp;
  
  if (*remain < 1) {

    return buffer;

  }

  temp = buffer;

  if (exception == NULL) {
    strncpy(temp, (char *)"<NULL>", *remain);
    if (*remain < 6) {
      temp += *remain;
      *remain = 0;
    }
    else {
      temp += 6;
      *remain -= 6;
    }

    return temp;

  }

  message = getReferenceField(exception, F_Throwable_detailMessage);
  if (message) {
    nbytes = x_snprintf(temp, *remain, "%k : %w", instance2clazz(exception), String2string(message));
  }
  else {
    nbytes = x_snprintf(temp, *remain, "%k (no message)", instance2clazz(exception));
  }

  *remain -= nbytes;

  return temp + nbytes;
}

#ifdef DEBUG
void _throwOutOfMemoryError(w_thread thread, const char *file, const char *function, const int line) {
  w_instance oome;

  if (thread) {
    threadMustBeSafe(thread);
  }

  woempa(9,"FREE MEMORY %i TOTAL MEMORY %i\n",x_mem_avail(),  x_mem_total());
  if (instance2clazz(exceptionThrown(thread)) == clazzOutOfMemoryError) {
    woempa(9, "Second or subsequent OutOfMemoryError thrown in %t at line %d in %s (%s)\n", thread, line, function, file);
  }
  else if(!exceptionThrown(thread)) {
    woempa(9, "First OutOfMemoryError thrown in %t at line %d in %s (%s)\n", thread, line, function, file);
    enterUnsafeRegion(thread);
    oome = allocThrowableInstance(thread, clazzOutOfMemoryError);
    if (!oome) {
      wabort(ABORT_WONKA, "Could not allocate memory for OutOfMemoryError!");
    }
    if (thread) {
      thread->exception = oome;
      removeLocalReference(thread, oome);
      if (thread->Thread) {
        setReferenceField(thread->Thread, oome, F_Thread_thrown);
      }
    }
    else {
      bootstrap_exception = oome;
    }
    enterSafeRegion(thread);
  }
  else {
    woempa(9, "OutOfMemoryError thrown when %e already pending - ignoring OutOfMemoryError at line %d in %s (%s)\n", exceptionThrown(thread), line, function, file);
  }
}
#else
void _throwOutOfMemoryError(w_thread thread) {
  w_instance oome;

  if (thread) {
    threadMustBeSafe(thread);
  }

  if (instance2clazz(exceptionThrown(thread)) != clazzOutOfMemoryError && !exceptionThrown(thread)) {
    enterUnsafeRegion(thread);
    oome = allocThrowableInstance(thread, clazzOutOfMemoryError);
    if (!oome) {
      wabort(ABORT_WONKA, "Could not allocate memory for OutOfMemoryError!");
    }
    if (thread) {
      thread->exception = oome;
      removeLocalReference(thread, oome);
      if (thread->Thread) {
        setReferenceField(thread->Thread, oome, F_Thread_thrown);
      }
    }
    else {
      bootstrap_exception = oome;
    }
    enterSafeRegion(thread);
  }
  // else ignore, exception already pending
}
#endif

void fillThrowable(w_thread thread, w_instance Throwable) {

  w_frame frame;
  w_int n;
  w_int i;
  w_Exr * records = getWotsitField(Throwable, F_Throwable_records);

  /*
  ** No need to fill the same throwable twice; it would result in a memory
  ** leak. Been there, done that...
  */
  
  if (records == NULL) {
    for (n = 0, frame = thread->top; frame; frame = frame->previous) {
      if (frame->method) {
        n += 1;
      }
    }

    records = allocClearedMem(sizeof(w_Exr) * n);
    if (records) {
      setWotsitField(Throwable, F_Throwable_records,  records);
      for (i = 0, frame = thread->top; frame; frame = frame->previous) {
        if (frame->method) {
          records[i].method = frame->method;
          records[i].pc = frame->current - frame->method->exec.code;
          records[i].position = --n;
          i += 1;
        }
      }
    }
  }

}

w_instance clearException(w_thread thread) {

  w_instance exception = exceptionThrown(thread);

// We should protect the cleared exception somehow...
//  thread->top->auxs[0].c = exception;
//  thread->top->auxs[0].s = stack_exception;
//  thread->top->auxs += 1;
  if (thread) {
    thread->exception = NULL; // but leave the thrown field unchanged for protection from GC
  }
  else {
    bootstrap_exception = NULL;
  }

  return exception;

}

void throwExceptionInstance(w_thread thread, w_instance Throwable) {
  if (exceptionThrown(thread) == Throwable) {
  // Specially 'coz interpreter.c uses throwExceptionInstance(thread, thread->exception)
    woempa(7, "Throwing %e (%j) in %t\n", Throwable, Throwable, thread);
    setReferenceField(thread->Thread, Throwable, F_Thread_thrown);
  }
  else if(!exceptionThrown(thread)) {
    woempa(7, "Throwing %e (%j) in %t\n", Throwable, Throwable, thread);
    if (thread) {
      thread->exception = Throwable;
      if (thread->Thread) {
        setReferenceField(thread->Thread, Throwable, F_Thread_thrown);
      }
    }
    else {
      bootstrap_exception = NULL;
    }
  }
  else {
    woempa(7, "Exception %e (%j) is already pending\n", exceptionThrown(thread), exceptionThrown(thread));
    woempa(7, "--> ignoring %e \n", Throwable);
  }
}

static const w_int bufsize = 1024;

void throwException(w_thread thread, w_clazz exception, char * format, ...) {

  va_list ap;
  char * buffer;
  w_int length;
  w_string message = NULL;
  w_instance theMessage = NULL;
  w_instance theThrowable;

  threadMustBeSafe(thread);

  if (exceptionThrown(thread)) {
    woempa(7, "Not throwing %k in %t : exception %j is already there\n", exception, thread, exceptionThrown(thread));

    return;

  }

  if (mustBeInitialized(exception) == CLASS_LOADING_FAILED) {
    woempa(7, "Not throwing %k in %t : cannot initialise %k\n", exception, thread, exception);

    return;

  }

  if (format) {
    buffer = allocMem(bufsize + 1);
    if (buffer) {  
      va_start (ap, format);
      length = x_vsnprintf(buffer, bufsize, format , ap);
      va_end (ap);
      message = cstring2String(buffer, length);
      if (!message) {
        wabort(ABORT_WONKA, "Unable to create message\n");
      }
      releaseMem(buffer);
    }
  }
  
  woempa(7, "Throwing %k with message `%w' in %t\n", exception, message, thread);
  enterUnsafeRegion(thread);

  theThrowable = allocThrowableInstance(thread, exception);

  if (theThrowable) {
    if (message) {
      theMessage = newStringInstance(message);
      if (theMessage) {
        setReferenceField(theThrowable, theMessage, F_Throwable_detailMessage);
        removeLocalReference(thread, theMessage);
      }
      deregisterString(message);
    }

    if (thread) {
      thread->exception = theThrowable;

      if (thread->Thread) {
        setReferenceField(thread->Thread, theThrowable, F_Thread_thrown);
      }
      removeLocalReference(thread, theThrowable);
    }
    else {
      bootstrap_exception = theThrowable;
    }
  }
  else {
    woempa(9, "Unable to allocate instance of %k\n", exception);
    throwOutOfMemoryError(thread);
  }
  enterSafeRegion(thread);
}

