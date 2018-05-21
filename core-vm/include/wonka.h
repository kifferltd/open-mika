/**************************************************************************
* Copyright (c) 2004, 2005, 2006, 2007, 2009, 2011, 2016 by Chris Gray,   *
* KIFFER Ltd. All rights reserved.                                        *
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
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR        *
* ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL  *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN     *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

#ifndef _WONKA_H
#define _WONKA_H

#ifndef FALSE
#define FALSE 0
#endif

#ifndef TRUE
#define TRUE 1
#endif

#ifndef NULL
#define NULL ((void*)0)
#endif

#define NM(e) ((e)->name)

#define isSet(x, flag)              ((x) & (flag))
#define isNotSet(x, flag)           (!isSet((x), (flag)))
#define setFlag(x, flag)            ((x) |= (flag))
#define unsetFlag(x, flag)          ((x) &= ~(flag))
#define maskFlags(m, f)             ((m) & (f))

#include <sys/types.h>
#include <inttypes.h>
/*
** Include processor dependent stuff from 
** the ./processor/<arch> directory.
*/

#include "processor.h"

#ifndef __BIT_TYPES_DEFINED__
#error Your compilation environment does not define __BIT_TYPES_DEFINED__!
#error Your only remedy is to go into the wonka/hal/cpu/<foo>/include
#error directory for your architecture, and edit the processor.h file to
#error add typedefs which have the same effect as the standard definitions 
#error in wonka/include/wonka.h.
#else
typedef u_int8_t                    u1;
typedef u_int16_t                   u2;
typedef u_int32_t                   u4;

typedef void                        w_void;
typedef u_int8_t                    w_byte;
typedef u_int8_t                    w_ubyte;
typedef int8_t                      w_sbyte;
typedef int16_t                     w_short;
typedef u_int16_t                   w_ushort;
typedef u_int32_t                   w_word;
typedef int32_t                     w_boolean;
typedef u_int32_t                   w_size;
typedef int32_t                     w_int;
typedef int64_t                     w_dword;
typedef int64_t                     w_long;
typedef u_int64_t                   w_ulong;
typedef u_int16_t                   w_char;
#endif

typedef volatile u1 w_ConstantType;
typedef volatile u4 w_ConstantValue;


#define LSW_PART(l)                  (w_word)(l)
#define MSW_PART(l)                  (w_word)((w_ulong)(l)>>32)
#define DWORD_TO_WORDS(c, cm, cl)    (cm) = MSW_PART(c); (cl) = LSW_PART(c)

#if (defined(DEBUG) || defined(LOWMEM))
#define INLINE
#else
#define INLINE inline
#endif

/*
** An object instance is just an array of words that holds field variables.
** It is the trailing part of a w_object.
*/

typedef struct w_Object            *w_object;
typedef w_word                     *w_instance;

// TODO: get rid of this silliness
#define WONKA_TRUE   1
#define WONKA_FALSE  0

typedef struct w_Method *             w_method;
typedef struct w_MethodExec *         w_methodExec;
typedef struct w_MethodSpec *         w_methodSpec;
typedef struct w_Field *              w_field;
typedef struct w_Frame *              w_frame;
typedef struct w_Exr *                w_exr;
typedef volatile struct w_Slot *      w_slot;
typedef struct w_Desc *               w_desc;
typedef struct w_BAR *                w_bar;
typedef struct w_Type *               w_type;
typedef struct w_Fld *                w_fld;
typedef struct w_Mtd *                w_mtd;
typedef struct w_Mei *                w_mei;
typedef struct w_Handler *            w_handler;
typedef struct w_Local *              w_local;
typedef struct w_Lve *                w_lve;
typedef struct w_Lne *                w_lne;
typedef struct w_Mti *                w_mti;
typedef int                           w_color;
typedef const struct JNINativeInterface *   w_env;
typedef unsigned int                  w_pc; /* the program counter */
typedef unsigned char *               w_code; /* an opcode */
typedef unsigned char                 w_opcode;
typedef struct w_String *             w_string;
typedef struct w_Clazz *              w_clazz;
typedef struct w_Class *              w_class;
typedef struct w_Constant *           w_constant;
typedef struct w_Loader *             w_loader;
typedef struct w_Exception *          w_exception;
typedef struct w_Triple *             w_triple;
typedef struct w_Dispatcher *         w_dispatcher;
typedef struct w_Jstack *             w_jstack;
typedef struct w_Root *               w_root;
typedef struct w_Hashtable *          w_hashtable;
typedef struct w_Hashtable2k *        w_hashtable2k;
typedef struct w_Space *              w_space;
typedef struct w_StreamDriver *       w_streamDriver;
typedef struct w_ByteArrayStream *    w_byteArrayStream;
typedef struct w_BufferedStream *     w_bufferedStream;
typedef struct w_Jnode *              w_jnode;
typedef struct w_Jnode_free *         w_jnode_free;
typedef struct w_Jbuffer    *         w_jbuffer;
typedef struct w_Pobject *            w_pobject;
typedef struct w_Pss *                w_pss;
typedef struct w_Stream *             w_stream;
typedef struct w_Fifo *               w_fifo;
typedef struct w_Message *            w_message;
typedef struct w_Pipe *               w_pipe;
typedef struct w_ClassData *          w_classData;
typedef struct w_Repository *         w_repository;
typedef struct w_Rentry *             w_rentry;
typedef struct w_SHA *                w_sha;
typedef struct w_Grobag *             w_grobag;

/*
** This one is also defined in core-classes.h, but to eliminate the
** need to include core-classes.h in every file that uses
** it, we define it here also.
*/

#ifndef WS_SUCCESS
#define WS_SUCCESS 0
#endif

/*
** The access flags for methods, fields and classes and that type that
** will hold them. Note that w_flags is of size int, while in the
** JVM standard book it is only 'short' (u2) in size. We do this so that
** all structures are on word boundaries and we use the extra flag space
** for our own flags.
*/

typedef unsigned int                w_flags;

/*
** The official flags...
*/

#define ACC_PUBLIC                  0x00000001
#define ACC_PRIVATE                 0x00000002
#define ACC_PROTECTED               0x00000004
#define ACC_STATIC                  0x00000008
#define ACC_FINAL                   0x00000010
#define ACC_SYNCHRONIZED            0x00000020
#define ACC_SUPER                   0x00000020
#define ACC_VOLATILE                0x00000040
#define ACC_TRANSIENT               0x00000080
#define ACC_NATIVE                  0x00000100
#define ACC_INTERFACE               0x00000200
#define ACC_ABSTRACT                0x00000400
#define ACC_STRICT                  0x00000800
#define ACC_SYNTHETIC               0x00001000
// for later maybe ...
#ifdef JAVA6
#define ACC_ANNOTATION              0x00002000
#endif
#ifdef JAVA5
#define ACC_ENUM                    0x00004000
#endif
#define ACC_FLAGS                   0x00007fff

/*
** An entry in our constant pool array.
*/
typedef struct w_Cnt               *w_cnt;

/*
** A table with mapping of class (static) names and the fixup function
** for this class; each mapping ends with a { NULL, NULL }
*/

typedef void (*w_fixup)(w_clazz);

/*
** The function pointer declarations for native methods.
**
**   w_word = w_word_fun(void) a pointer to a function that returns a w_word;
**                             we declare that it doesn't take any arguments 
**                             since we will build the stack ourselves.
**   w_long = w_long_fun(void) a pointer to a function that returns a w_long;
**            w_void_fun(void) a pointer to a function that returns void; 
**                             again, with this function, we will build the 
**                             stack of arguments ourselves.
** w_fun_dec(w_word this, ...) the function pointer declaration that is 
**                             used in the registerMethod macro defined in 
**                             method.h. We defy the checking mechanism of 
**                             the C compiler, with this hack, but we know 
**                             what we're doing, right? right...
**
** These types are combined in the union w_Function that will be the type
** of a 'generic' native function pointer.
**
** Functions that return a value will have this result automatically copied
** to the callers stack top, as with normal JVM semantics.
*/

typedef w_word  (*w_word_fun)(void);
typedef w_long  (*w_long_fun)(void);
typedef void    (*w_void_fun)(void);
typedef void    (*w_fun_dec)(w_word this, ...);

union w_Function {
  w_word_fun word_fun;
  w_long_fun long_fun;
  w_void_fun void_fun;
};

typedef union w_Function            w_function;


/*
** Wonka RTOS related stuff.
*/

typedef struct w_Thread            *w_thread;
typedef unsigned long               w_time;

/*
** The Primitive type tags. Note that some descriptors are set to a number
** that corresponds with the 'newarray' bytecode.
*/

typedef enum {
  P_boolean     =  4,
  P_char        =  5,
  P_float       =  6,
  P_double      =  7,
  P_byte        =  8,
  P_short       =  9,
  P_int         = 10,
  P_long        = 11,
  P_void        = 12,
  P_Object      = 13
} P_Type;

/*
** The different types of descriptors. Note that the numbers follow the numbers of the 
** P_Type table so that we can easily make a LUTS out of it for sizeInBits and sizeInWords.
** See the file descriptor.h for the macros...
*/

typedef enum {
  D_Primitive   =  0,
  D_Class       = 14,
  D_Array       = 15,
  D_Method      = 16
} D_Type;

/*
** General utility functions
*/

void startNatives(void);
void startPlatform(void);
void install_term_handler(void);

#ifdef MODULE
void loadExternals(void);
#else
void initExternals(void);
#endif
void registerExternals(void);

extern w_int bytesInUse;

void startWonka(void*);

/*
** The systemClassLoader is the unique instance of com.acunia.wonka.SystemClassLoader.
*/

extern w_instance                    SystemClassLoader;

/*
** A special clazz that we use to clone array clazzes from. It inherits all clazzObject
** stuff and replaces the 'clone' method slot and adds 2 interfaces: Cloneable and java.io.Serializable
*/

extern w_clazz                       clazz_Array;

void reportMem(void);

/*
** PutString outputs a null-terminated string to the debug device.
** PutHex and PutDec output a number (in hex or decimal respectively),
** to the given number of places.
*/

void PutString(char *s);
void PutHex (unsigned int Value, unsigned int Width);
void PutDec (unsigned int Value, unsigned int Width);

void setAllTriggerLevel(int trigger);
void _setTriggerLevel(const char *file, int trigger);

/*
** Logging & debugging stuff.
*/

#include "debug.h"
#include <stdarg.h>

w_ushort CCITT_16(w_ubyte *data, w_size length);
w_word CCITT_32(w_ubyte *data, w_size length);
w_word update_ISO3309_CRC(w_word crc, w_ubyte *buf, w_size len);
w_void make_ISO3309_CRC_table(w_void);
w_int wonka_vsnprintf(char * buf, w_int bufsize, const char *fmt, va_list args);
w_int wonka_snprintf(char * buf, w_int bufsize, const char *fmt, ...);

char *threadDescription(w_thread t);

/*
** Some utility structures to handle 64 bit values in a somewhat CPU independent way.
**
** We represent the bytes, shorts and words in a 64 or 32 bit variable as follows
**
**        +-----------------------------------+-----------------------------------+
** bit #  | 63 ............................32 | 31............................. 0 |
** bits   | MSB ............................. | ............................. LSB |
**        +-----------------------------------+-----------------------------------+
** words  |              WORD_MSW             |             WORD_LSW              |
**        +-----------------+-----------------+-----------------+-----------------+
** shorts |     SHORT_3     |     SHORT_2     |     SHORT_1     |    SHORT_0      |
**        +--------+--------+--------+--------+--------+--------+--------+--------+
** bytes  | BYTE_7 | BYTE_6 | BYTE_5 | BYTE_4 | BYTE_3 | BYTE_2 | BYTE_1 | BYTE_0 |
**        +--------+--------+--------+--------+--------+--------+--------+--------+
*/

typedef union w_u64 {
  w_ulong u64;
  w_long  s64;
  w_ubyte ubytes[8];
  w_ushort ushorts[4];
  w_word words[2];
} w_u64;

typedef union w_u32 {
  w_word u32;
  w_int  s32;
  w_ubyte ubytes[4];
  w_ubyte u08[4];
  w_ushort ushorts[2];
} w_u32;

typedef union w_u16 {
  w_ushort u16;
  w_short  s16;
  w_ubyte u8[2];
} w_u16;

extern w_boolean memoryAddressIsValid(w_size addr);

/*
** Convert a number of bits to a number of w_words, rounding up to the next
** whole number.
*/
inline static w_size roundBitsToWords(w_int bits) {
  return ((bits + 31) & ~31) >> 5;
}

/*
** w_memcpy is intended to be more efficient when the return value of memcpy(3)
** is not required. It should work on all CPU types and OSes, but you may need
** to disable it if it is causing memory corruption or is slower than memcpy(3).
*/
#define USE_W_MEMCPY

#ifdef USE_W_MEMCPY
void w_memcpy(void * adst, const void * asrc, w_size length);
#else
#define w_memcpy(d,s,l) memcpy(d,s,l)
#endif

/*
** See core-vm/src/heap/collector.c for the use of PIGS_MIGHT_FLY.
*/
//#define PIGS_MIGHT_FLY

/*
** If 'pedantic' is TRUE, Wonka will detect errors such as AbstractMethodError
** even if the offending method is never called at runtime.
*/
extern w_boolean pedantic;

// #define RESMON

#endif /* _WONKA_H */
