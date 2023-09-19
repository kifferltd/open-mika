/**************************************************************************
* Copyright (c) 2006, 2007, 2010, 2012, 2022, 2023  by KIFFER Ltd.        *
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


#ifndef _EXCEPTION_H
#define _EXCEPTION_H

#include <errno.h>
#include <string.h>

#include "mika_threads.h"
#include "wonka.h"

/**
  An exception record used to build a stack trace.
*/
typedef struct w_Exr {
  w_method   method;
  w_int      pc;
  w_int      position;
} w_Exr;

/**
  This file contains prototypes for functions which are used to throw
  exceptions from within a Java thread.
*/

/*
** Location where we store exceptions during the bootstrap phase, when there
** is no w_Thread structure available.
*/
extern w_instance bootstrap_exception;

/** Throw an exception: `format' is allowed to be NULL (there will then be no detailMessage).
*/

void throwException(w_thread thread, w_clazz clazz, char * format, ...);

/** Throw an OutOfMemoryError
*/
#ifdef DEBUG
#define throwOutOfMemoryError(t,n) _throwOutOfMemoryError(t, n, __FILE__, __FUNCTION__, __LINE__)
void _throwOutOfMemoryError(w_thread, w_int size, const char *file, const char *function, const int line);
#define throwOutOfMemoryError_unsafe(t,n) _throwOutOfMemoryError_unsafe(t, n, __FILE__, __FUNCTION__, __LINE__)
void _throwOutOfMemoryError_unsafe(w_thread, w_int size, const char *file, const char *function, const int line);
#else
#define throwOutOfMemoryError(t,n) _throwOutOfMemoryError(t, n)
void _throwOutOfMemoryError(w_thread, w_int);
#define throwOutOfMemoryError_unsafe(t,n) _throwOutOfMemoryError_unsafe(t, n)
void _throwOutOfMemoryError_unsafe(w_thread, w_int);
#endif

void throwLocalException(w_thread sender, w_clazz exception, const char *file, const char *function, int line, const char *fmt, ...);
w_instance createExceptionInstance(w_thread thread, w_clazz clazz, w_string message);
void setLocalException(w_thread thread, w_instance Exception);
void fillInStackTrace(w_instance thisThrowable);

#ifndef __LINT__
#define ThrowLocalException(t, e, m, a...) throwLocalException((t), (e), __FILE__, __FUNCTION__, __LINE__, m, ##a)
#endif

#define exceptionThrown(thread) ( (thread) ? (thread)->exception : bootstrap_exception )

char * print_exception(char * buffer, int * remain, void * data, int w, int p, unsigned int f);

void throwExceptionInstance(w_thread thread, w_instance exception);
w_instance clearException(w_thread thread);

void fillThrowable(w_thread thread, w_instance Throwable);

extern w_clazz clazzArrayIndexOutOfBoundsException;
extern w_clazz clazzIOException;
extern w_clazz clazzNullPointerException;

static inline void throwArrayIndexOutOfBoundsException(w_thread thread) {
  throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
}

static inline void throwIOException(w_thread thread) {
  throwException(thread, clazzIOException, "%s", strerror(errno));
}

static inline void throwNullPointerException(w_thread thread) {
  throwException(thread, clazzNullPointerException, NULL);
}

w_instance createRuntimeException(const char *classname);

#endif /* _EXCEPTION_H */
