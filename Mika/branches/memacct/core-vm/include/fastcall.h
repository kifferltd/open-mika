/**************************************************************************
* Copyright (c) 2003 by Chris Gray, /k/ Embedded Java Solutions.          *
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
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE  *
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR     *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/
/****************************************************************************
* Copyright (c) 2003 by Chris Gray, trading as /k/ Embedded Java Solutions. *
* All rights reserved.  The contents of this file may not be copied or      *
* distributed in any form without express written consent of the author.    *
****************************************************************************/

#ifndef _FASTCALL_H

/*
** If USE_FAST_CALLS is defined, certain often-used methods will be replaced
** by a "fast call" the first time they are executed.  In a "fast call" the
** method is simply passed a pointer to the calling frame, and is responsible
** for finding its parameters and writing its return value to the stack.
*/

#define USE_FAST_CALLS

/*
** The "fast" methods defined to date. The indices here need to correspond to
** the fast call table in interpreter.c !!!
*/
#define FAST_STRINGBUFFER_APPEND         0
#define FAST_STRINGBUFFER_TOSTRING       1
#define FAST_STRING_CREATE_EMPTY         2
#define FAST_STRING_CREATE_BYTE          3
#define FAST_STRING_CREATE_CHAR          4
#define FAST_STRING_EQUALS               5
#define FAST_STRING_HASHCODE             6
#define FAST_STRING_LENGTH               7
#define FAST_STRING_SUBSTRING            8
#define FAST_STRING_INDEXOF_CHAR         9
#define FAST_STRING_CHARAT              10
#define FAST_STRING_TOSTRING            11
#define FAST_STRING_STARTSWITH          12
#define FAST_CHARACTER_ISDIGIT_CHAR     13
#define FAST_CHARACTER_FORDIGIT_INT_INT 14
#define FAST_CHARACTER_DIGIT_CHAR_INT   15
#define FAST_SYSTEM_CURRENTTIMEMILLIS   16
#define FAST_MATH_SQRT                  17
#define FAST_MATH_SIN                   18
#define FAST_MATH_COS                   19
#define FAST_MATH_TAN                   20
#define FAST_MATH_ASIN                  21
#define FAST_MATH_ATAN                  22
#define FAST_MATH_LOG                   23
#define FAST_MATH_EXP                   24

typedef struct w_FastCall {
  w_string method_name;
  w_string method_sig;
  w_int index;
} w_FastCall;

typedef w_FastCall* w_fastcall;

typedef struct w_FastClass {
  w_string class_name;
  w_fastcall* calls;
} w_FastClass;

typedef w_FastClass* w_fastclass;

#ifdef NATIVE_MATH
#define FAST_STATIC_CLASSES 3
#else
#define FAST_STATIC_CLASSES 2
#endif
#define FAST_VIRTUAL_CLASSES 2
#define FAST_SPECIAL_CLASSES 1

void fastcall_check_invoke_static(w_clazz clazz, unsigned char * bytecodes);
void fastcall_check_invoke_virtual(w_clazz clazz, unsigned char * bytecodes);
void fastcall_check_invoke_special(w_clazz clazz, unsigned char * bytecodes);
void fastcall_init_tables(void);

#define _FASTCALL_H
#endif /* _FASTCALL_H */
