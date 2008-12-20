#ifndef _DEBUG_H
#define _DEBUG_H

/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

/*
** $Id: debug.h,v 1.3 2005/07/17 15:40:34 cvs Exp $
*/

/*
** --- IMPORTANT NOTE ---
**
** This file is NOT meant to be included by any other file than "wonka.h".
*/

#define ABORT_INFO                    0
#define ABORT_THREAD                  1
#define ABORT_GROUP                   2
#define ABORT_ROOT_GROUP              3
#define ABORT_WONKA                   4

#ifndef DEBUG_LEVEL
#define DEBUG_LEVEL 1
#endif

#if defined (__LINT__)

#define woempa                      lint_woempa
#define wabort                      lint_wabort
#define wassert(test)               ((test) ? (void)0 : lint_wabort(ABORT_ROOT_GROUP, "Assertion failed: '%s'\n", #test))
#define ThrowLocalException         lint_ThrowLocalException 
#define inline
#define DEBUG_STATEMENT(x)          (x)

void lint_woempa(int level, const char *format, ...);
void lint_wabort(int scope, const char *format, ...);
void lint_ThrowLocalException(w_thread ad, w_clazz exception, const char *fmt, ...);

#elif defined (DEBUG)

#include <stdio.h>

#define D(n) fprintf(stderr, "DEBUG %d\n", n); fflush(stderr);
#define setTriggerLevel(t)          _setTriggerLevel(__FILE__, t)
#if defined(__STDC_VERSION__) && (__STDC_VERSION__ >= 199901L)
#define woempa(level, ...) if(level>=DEBUG_LEVEL)_woempa(__FILE__, __FUNCTION__, __LINE__, level, __VA_ARGS__)
#else
#define woempa(level, format, a...) if(level>=DEBUG_LEVEL)_woempa(__FILE__, __FUNCTION__, __LINE__, level, format, ##a)
#endif

#define wassert(test)               ((test) ? (void)0 : _wabort(__FUNCTION__, __LINE__, ABORT_ROOT_GROUP, "Assertion failed: '%s'\n", #test))
#define DEBUG_STATEMENT(x)          (x)
void _wabort(const char *function, int line, int scope, const char *format, ...);

#if defined(__STDC_VERSION__) && (__STDC_VERSION__ >= 199901L)
#define wabort(scope, ...)   _wabort(__FUNCTION__, __LINE__, scope, __VA_ARGS__)
#else
#define wabort(scope, format, a...)   _wabort(__FUNCTION__, __LINE__, scope, format, ##a)
#endif

//void _woempa(const char *file, const char *function, int line, int level, const char *format, ...) __attribute__ ((format (printf, 5, 6)));
void _woempa(const char *file, const char *function, int line, int level, const char *format, ...);
void _setTriggerLevel(const char *file, int trigger);

#else /* DEBUG nor __LINT__ is defined */

#define D(x)
#define setTriggerLevel(t) 
#define woempa(level, format, a...)  
#define wassert(test) 
#define DEBUG_STATEMENT(x)
//void _wabort(const char *function, int line, int scope, const char *format, ...) __attribute__ ((format (printf, 4, 5)));
void _wabort(const char *function, int line, int scope, const char *format, ...);

#define wabort(scope, format, a...)   _wabort(__FUNCTION__, __LINE__, scope, format, ##a)

#endif /* DEBUG */

#ifdef __INSURE__
//#define inline
#endif

/*
** Define the format of a woempa.  If any of the following symbols is not
** #defined (e.g. the definition is comment out) then the corresponding
** information will not be printed.
**   WOEMPA_JTHREAD_FORMAT       prints the address of the current w_thread.
**   WOEMPA_LEVEL_FORMAT         prints the woempa level.
**   WOEMPA_BYTECODECOUNT_FORMAT prints the count of opcodes executed
**                               (summed over all threads).
**   WOEMPA_METHOD_FORMAT        prints the current method's declaring class
**                               and name, and the current PC.
**   WOEMPA_FUNCTION_FORMAT      prints the current C function name and line
**                               number.
*/
#define WOEMPA_JTHREAD_FORMAT       "%t"
// #define WOEMPA_LEVEL_FORMAT         "(%d)"
// #define WOEMPA_BYTECODECOUNT_FORMAT "%d"
#define WOEMPA_METHOD_FORMAT        "[%k/%w:%d]"
#define WOEMPA_FUNCTION_FORMAT      "[%s:%d]"

void w_dump(const char *fmt, ... );
  
/*
** Location where an ASCII version of the wonka.verbose property is stored,
** as a null-terminated C string
*/
extern char *verbose_cstring;

/*
 * Bitmap representation of flags recognised from the wonka.verbose string
 */
extern w_flags verbose_flags;

/*
 * Some flags and the corresponding strings. Note that the string will be
 * recognised if it occurs in the string at all, not only if it is strictly
 * delimited by :'s. Thus for example "throw" also matched "throws", etc..
 */
#define VERBOSE_FLAG_STARTUP  0x00000001 // "start"
#define VERBOSE_FLAG_SHUTDOWN 0x00000002 // "shut"
#define VERBOSE_FLAG_THREAD   0x00000004 // "thread"
#define VERBOSE_FLAG_GC       0x00000008 // "gc"
#define VERBOSE_FLAG_LOAD     0x00000010 // "load"
#define VERBOSE_FLAG_SOCKET   0x00000020 // "socket"
#define VERBOSE_FLAG_STACK    0x00000040 // "stack"
#define VERBOSE_FLAG_TRAFFIC  0x00000080 // "traffic"
#define VERBOSE_FLAG_THROW    0x00000100 // "throw"
#define VERBOSE_FLAG_URL      0x00000200 // "url"
#define VERBOSE_FLAG_HTTP     0x00000400 // "http"
#define VERBOSE_FLAG_EXEC     0x00000800 // "exec"

/*
** Function which behaves like printf, except that it recognizes wonka-specific
** formats such as %w, %j, etc..
*/
void wprintf(const char *format, ...);

#define DUMP_CLASSES
#define DUMP_CLASSLOADERS

#endif /* _DEBUG_H */
