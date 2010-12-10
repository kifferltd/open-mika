/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
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

#ifndef _METHODS_H
#define _METHODS_H

#ifdef USE_LIBFFI
#include <ffi.h>
#endif

#include "clazz.h"
#include "hashtable.h"
#include "wonka.h"
#include "dispatcher.h"
#include "heap.h"

typedef struct w_LineNum {
  w_ushort start_pc;
  w_ushort line_number;
} w_LineNum;

typedef struct w_LocalVar {
  w_ushort start_pc;       /* This var is valid from start_pc (inclusive) */
  w_ushort length;         /* to start_pc+length (exclusive)              */
  w_string name;           /* Name of this variable                       */
  w_string desc;           /* Descriptor of this variable                 */
  w_ushort slot;           /* Slot number of this var within frame        */
  w_ushort dummy;
} w_LocalVar;

typedef struct w_MethodDebugInfo {
  w_short numLocalVars;    /* Number of local variable records            */
  w_short numLineNums;     /* Number of line number records               */
  w_LocalVar *localVars;   /* Local variable record array                 */
  w_LineNum *lineNums;     /* Line number record array                    */
} w_MethodDebugInfo;

typedef struct w_MethodDebugInfo *w_methodDebugInfo;

/*
** Define F_Method_wotsit. This way we don't need to include the huge core-classes.h file 
** and as a result speed up compilation a lot since almost every .c file includes this file.
*/

extern w_int F_Method_wotsit;

/*
** The slot number for a method that still isn't processed fully.
*/

#define SLOT_NOT_ALLOCATED                     -1

/*
** Description of an exception handler.
*/
typedef struct w_Exception {
  w_ushort start_pc;   /* Start of code covered by this handler (inclusive) */
  w_ushort end_pc;     /* End of code covered by this handler (exclusive)   */
  w_ushort handler_pc; /* Entry point of the handler                        */
  w_ushort type_index; /* Type of exception covered (0 -> all exceptions)   */
} w_Exception;

/*
** The information needed to uniquely identify a method.
*/
typedef struct w_MethodSpec {
  w_clazz  declaring_clazz; /* Class in which the method is declared       */
  w_string name;            /* Method name                                 */
  w_clazz *arg_types;       /* Types of arguments, null-terminated         */
  w_clazz  return_type;     /* Return type                                 */
} w_MethodSpec;

typedef struct w_MethodExec {
  volatile w_callfun dispatcher;
  w_size       arg_i;      /* The number of argument words. !! DONT move !! Assembly depends on it.    */
  w_function   function;   /* if native, the function pointer. !! DONT move !! Assembly depends on it. */
  w_ushort return_i;       /* Number of words returned as result (0, 1, or 2) */
  w_ushort local_i;        /* Number local vars, from class file */
  w_ushort stack_i;        /* Number of temporary locations used on stack */
  w_ushort nargs;          /* Number of arguments (of whatever size)      */
#ifdef USE_LIBFFI
  ffi_cif *cif;            /* Call InterFace used by libffi               */
#endif
  w_int    code_length;    /* Length of code - can be > 65535 (!)         */
  w_code   code;           /* Bytecode array (if not native method)       */
  w_short numExceptions;   /* Number of exception handlers                */
  w_exception exceptions;  /* Exception handler array                     */
  w_methodDebugInfo debug_info; /* Method debug info if available         */
#ifdef JAVA_PROFILE
  w_long bytecodes;
  w_word runs;
  w_long totaltime;
  w_long runtime;
   w_ubyte checked;
   void *callData;
   void *instanceData;
#endif
} w_MethodExec;

/*
** Complete data for a method.
*/
typedef struct w_Method {
  w_MethodSpec spec;         /* Method spec, see above                     */
  w_int        slot;         /* Offset in vmlt (-1 for abstract methods)   */
  w_flags      flags;
  w_string     desc;         /* descriptor                                 */
  w_method     parent;       /* method which we override or implement      */
  w_ushort     dummy;
  w_short      numThrows;    /* number of classes in the `throws' clause   */
  w_ushort    *throws;       /* the classes themselves                     */

  w_MethodExec exec;

  #ifdef JSPOT
    /*
    ** The return address of a J-Spot compiled method.  
    **   FIXME: as soon I figured out what new fields are required to 
    **          support J-Spot compiled methods, I'll probably bundle
    **          them in a seperate struct.
    */
  
    w_word return_address;
    int counter;
  #endif
    
} w_Method;

/*
** The lower 16 bits of the "flags" field are the ACC_... flags defined
** in the Java class file definition. The upper 16 bits are used as follows:
*/
#define METHOD_IS_CLINIT       0x00010000 /* static initializer (<clinit>)  */
#define METHOD_IS_CONSTRUCTOR  0x00020000 /* constructor (<init>)           */
#define METHOD_NO_OVERRIDE     0x00040000 /* private/final/constructor      */
                                          /* Note: a constructor has both   */
                                          /* CONSTRUCTOR and NO_OVERRIDE set*/
#define METHOD_IS_INTERFACE    0x00080000 /* method of an interface         */
#define METHOD_IS_COMPILED     0x00100000 /* JITed method                   */
#define METHOD_UNSAFE_DISPATCH 0x00200000 /* Can be dispatched without      */
#define METHOD_TRIVIAL_CASES   0x0f000000 /* Trivial cases, see below       */
                                          /* leaving unsafeRegion           */
#define METHOD_IS_MIRANDA      0x10000000 /* abstract method inherited from */
                                          /* a superinterface               */
#define METHOD_IS_PROXY        0x20000000 /* Automatically-generated method */
                                          /*  a dynamic Proxy class         */
#define METHOD_IS_SYNTHETIC    0x40000000 /* method was not in source file  */
                                          /* (only if neither PROXY nor     */
                                          /* MIRANDA is set)                */

/* We recognise some trivial cases in prepareBytecode() */
#define METHOD_IS_VRETURN      0x01000000 /* Method consists of 'vreturn' */
#define METHOD_IS_RETURN_THIS  0x02000000 /* Method just returns 'this' */
#define METHOD_IS_RETURN_NULL  0x03000000 /* Method just returns null */
#define METHOD_IS_RETURN_ICONST 0x04000000 /* Method just returns an in constant */
#define METHOD_CALLS_SUPER     0x05000000 /* Method just calls another non-virtual method */

/*
 * methodIsInterface() returns true iff the method is an interface method.
 */
static inline w_boolean methodIsInterface(w_method m) {
  return isSet(m->flags, METHOD_IS_INTERFACE);
}

/*
** Convert a w_methodExec pointer to a w_method, by subtracting the offset
** of the `exec' field within w_Method.
*/
#define exec2method(x) ((w_method)(((char*)(x)) - (size_t)&((w_method)0)->exec))

/*
** registerNative() registers `fp' as the native code for the native method
** with the given name and signature of the given clazz.
*/
void _registerNativeMethod(w_clazz clazz, w_fun_dec fp, const char *name, const char *sig);
#define registerNativeMethod(clazz, function, name, sig) _registerNativeMethod(clazz, (w_fun_dec)function, name, sig) 

/*
** code2pc translates a pointer into the code of a method into a PC (offset
** from the start of the method).  Special case: if the method is native,
** always returns -1.
*/
w_int code2pc(w_method method, w_code current);

/*
** code2line translates a pointer into the code of a method into a line number
** within the source file.  Special cases: 
** - if not debug info is stored, always returns -2.
** - if the method is native, always returns -1.
** - if the code pointer is out of range, returns the last line of the method.
*/
w_int code2line(w_method method, w_code current);

/*
** Formatter functions to print a method name only, or the full Wonka.
*/

char * print_method_short(char * buffer, int * remain, void * data, int w, int p, unsigned int f);
char * print_method_long(char * buffer, int * remain, void * data, int w, int p, unsigned int f);

#if defined(DEBUG)
/*
** Print a report detailing all methods of a class.
*/
void methodTableDump(w_clazz clazz);
#endif

/*
** Convert an instance of java.lang.reflect.Method to a w_method pointer.
*/
static inline w_method Method2method(w_instance Method) {

  if (Method == NULL) {
    return NULL;
  }

  return getWotsitField(Method, F_Method_wotsit);

}

/*
** Create a w_MethodStack given the declaring class and the descriptor string.
** Returns CLASS_LOADING_SUCCEEDED or CLASS_LOADING_FAILED.
*/
w_int createMethodSpecUsingDescriptor(w_clazz declaring_clazz, w_string name, w_string desc_string, w_MethodSpec **specptr);

/*
** Release a method spec. Does not release the memory of the w_MethodSpec
** itself (this could be part of a w_Method structure), but it does
** deregister all the strings, unloaded classes etc. which it references.
*/
void releaseMethodSpec(w_MethodSpec *spec);

/*
** Register a set of native methods (e.g. all those of a particular class).
*/
void registerNatives(w_clazz clazz, const JNINativeMethod *methods, w_int mcount);

/*
** Look up the implementation of a given (non-interface) method in a subclass.
** If clazz does not inherit method, result is undefined(!).  May be called if
** clazz ==  method->declaring_clazz.
*/
static inline w_method virtualLookup(w_method method, w_clazz clazz) {
#ifdef RUNTIME_CHECKS
  if (isSet(method->flags, METHOD_IS_INTERFACE)) {
    wabort(ABORT_WONKA, "%M is an interface method, must use interfaceLookup instead\n", method);
  }
#endif

  if (isSet(method->flags, METHOD_NO_OVERRIDE)) {
    woempa(1, "%M cannot be overridden, so let's not do that\n", method);

    return method;

  }

  woempa(1, "Looking up %M in %K: result is %M\n", method, clazz, clazz->vmlt[method->slot]);

  return clazz->vmlt[method->slot];
}

/*
** Look up the implementation of a given interface method in an implementing class.
** If clazz does not implement this method, will return NULL.  
*/
w_method interfaceLookup(w_method method, w_clazz clazz);

/*
** Test whether a method matches a given MethodSpec. The method must have
** the correct declaring_clazz, name, and arg_types: the return_type must
** also match, unless it is given in the spec as 'null', in which case any
** return_type is acceptable. 
*/
w_boolean methodMatchesSpec(w_method method, w_MethodSpec *spec);

/**
** If true, line number and local variable tables will be stored and can be
** used for debugging.
*/
extern w_boolean use_method_debug_info;

#endif /* _METHODS_H */
