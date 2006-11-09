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

typedef struct w_FastCall {
  w_string method_name;
  w_string method_sig;
  w_int index;
} w_FastCall;

typedef w_FastCall* w_fastcall;

typedef struct w_FastClass {
  w_string class_name;
  w_clazz initialize;
  w_fastcall* calls;
} w_FastClass;

typedef w_FastClass* w_fastclass;

#define FAST_STATIC_CLASSES 2
#define FAST_VIRTUAL_CLASSES 2
void fastcall_check_invoke_static(w_clazz clazz, unsigned char * bytecodes);
void fastcall_check_invoke_virtual(w_clazz clazz, unsigned char * bytecodes);
void fastcall_init_tables(void);

#define _FASTCALL_H
#endif /* _FASTCALL_H */
