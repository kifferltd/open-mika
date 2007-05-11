#ifndef _EXCEPTION_H
#define _EXCEPTION_H

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
* Modifications copyright (c) 2006 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: exception.h,v 1.4 2006/10/04 14:24:14 cvsroot Exp $
*/

#include "threads.h"
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
#define throwOutOfMemoryError(t) _throwOutOfMemoryError(t, __FILE__, __FUNCTION__, __LINE__)
void _throwOutOfMemoryError(w_thread, const char *file, const char *function, const int line);
#else
#define throwOutOfMemoryError(t) _throwOutOfMemoryError(t)
void _throwOutOfMemoryError(w_thread);
#endif

void throwLocalException(w_thread sender, w_clazz exception, const char *file, const char *function, int line, const char *fmt, ...);
w_instance createExceptionInstance(w_thread thread, w_clazz clazz, w_string message);
void setLocalException(w_thread thread, w_instance Exception);
void fillInStackTrace(w_instance thisThrowable);

#ifndef __LINT__
#define ThrowLocalException(t, e, m, a...) throwLocalException((t), (e), __FILE__, __FUNCTION__, __LINE__, m, ##a)
#endif

inline static w_instance exceptionThrown(w_thread thread) {
  return thread ? thread->exception : bootstrap_exception;
}

char * print_exception(char * buffer, int * remain, void * data, int w, int p, unsigned int f);

void throwExceptionInstance(w_thread thread, w_instance exception);
w_instance clearException(w_thread thread);

void fillThrowable(w_thread thread, w_instance Throwable);

#endif /* _EXCEPTION_H */
