#ifndef _OPCODES_H
#define _OPCODES_H

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
** $Id: opcodes.h,v 1.6 2006/04/19 21:24:54 cvs Exp $
*/

#include "mika_threads.h"
#include "wonka.h"

#define nop				0x00
#define aconst_null			0x01
#define iconst_m1			0x02
#define iconst_0			0x03
#define iconst_1			0x04
#define iconst_2			0x05
#define iconst_3			0x06
#define iconst_4			0x07
#define iconst_5			0x08
#define lconst_0			0x09
#define lconst_1			0x0a /* 10 */
#define fconst_0			0x0b
#define fconst_1			0x0c
#define fconst_2			0x0d
#define dconst_0			0x0e
#define dconst_1			0x0f
#define bipush				0x10
#define sipush				0x11
#define ldc				0x12
#define ldc_w				0x13
#define ldc2_w				0x14 /* 20 */
#define iload				0x15
#define lload				0x16
#define fload				0x17
#define dload				0x18
#define aload				0x19
#define iload_0				0x1a
#define iload_1				0x1b
#define iload_2				0x1c
#define iload_3				0x1d
#define lload_0				0x1e /* 30 */
#define lload_1				0x1f
#define lload_2				0x20
#define lload_3				0x21
#define fload_0				0x22
#define fload_1				0x23
#define fload_2				0x24
#define fload_3				0x25
#define dload_0				0x26
#define dload_1				0x27
#define dload_2				0x28 /* 40 */
#define dload_3				0x29
#define aload_0				0x2a
#define aload_1				0x2b
#define aload_2				0x2c
#define aload_3				0x2d
#define iaload				0x2e
#define laload				0x2f
#define faload				0x30
#define daload				0x31
#define aaload				0x32 /* 50 */
#define baload				0x33
#define caload				0x34
#define saload				0x35
#define istore				0x36
#define lstore				0x37
#define fstore				0x38
#define dstore				0x39
#define astore				0x3a
#define istore_0			0x3b
#define istore_1			0x3c /* 60 */
#define istore_2			0x3d
#define istore_3			0x3e
#define lstore_0			0x3f
#define lstore_1			0x40
#define lstore_2			0x41
#define lstore_3			0x42
#define fstore_0			0x43
#define fstore_1			0x44
#define fstore_2			0x45
#define fstore_3			0x46 /* 70 */
#define dstore_0			0x47
#define dstore_1			0x48
#define dstore_2			0x49
#define dstore_3			0x4a
#define astore_0			0x4b
#define astore_1			0x4c
#define astore_2			0x4d
#define astore_3			0x4e
#define iastore				0x4f
#define lastore				0x50 /* 80 */
#define fastore				0x51
#define dastore				0x52
#define aastore				0x53
#define bastore				0x54
#define castore				0x55
#define sastore				0x56
#define pop				0x57
#define pop2				0x58
#define dup				0x59
#define dup_x1				0x5a /* 90 */
#define dup_x2				0x5b
#define dup2				0x5c
#define dup2_x1				0x5d
#define dup2_x2				0x5e
#define swap				0x5f
#define iadd				0x60
#define ladd				0x61
#define fadd				0x62
#define dadd				0x63
#define isub				0x64 /* 100 */
#define lsub				0x65
#define fsub				0x66
#define dsub				0x67
#define imul				0x68
#define lmul				0x69
#define fmul				0x6a
#define dmul				0x6b
#define idiv				0x6c
#define ldiv				0x6d
#define fdiv				0x6e /* 110 */
#define ddiv				0x6f
#define irem				0x70
#define lrem				0x71
#define frem				0x72
#define drem				0x73
#define ineg				0x74
#define lneg				0x75
#define fneg				0x76
#define dneg				0x77
#define ishl				0x78 /* 120 */
#define lshl				0x79
#define ishr				0x7a
#define lshr				0x7b
#define iushr				0x7c
#define lushr				0x7d
#define iand				0x7e
#define land				0x7f
#define ior				0x80
#define lor				0x81
#define ixor				0x82 /* 130 */
#define lxor				0x83
#define iinc				0x84
#define i2l				0x85
#define i2f				0x86
#define i2d				0x87
#define l2i				0x88
#define l2f				0x89
#define l2d				0x8a
#define f2i				0x8b
#define f2l				0x8c /* 140 */
#define f2d				0x8d
#define d2i				0x8e
#define d2l				0x8f
#define d2f				0x90
#define i2b				0x91
#define i2c				0x92
#define i2s				0x93
#define lcmp				0x94
#define fcmpl				0x95
#define fcmpg				0x96 /* 150 */
#define dcmpl				0x97
#define dcmpg				0x98
#define ifeq				0x99
#define ifne				0x9a
#define iflt				0x9b
#define ifge				0x9c
#define ifgt				0x9d
#define ifle				0x9e
#define if_icmpeq			0x9f
#define if_icmpne			0xa0 /* 160 */
#define if_icmplt			0xa1
#define if_icmpge			0xa2
#define if_icmpgt			0xa3
#define if_icmple			0xa4
#define if_acmpeq			0xa5
#define if_acmpne			0xa6
#define j_goto				0xa7
#define jsr				0xa8
#define ret				0xa9
#define tableswitch			0xaa /* 170 */
#define lookupswitch			0xab
#define ireturn				0xac
#define lreturn				0xad
#define freturn				0xae
#define dreturn				0xaf
#define areturn				0xb0
#define vreturn				0xb1 /* 'return' is a reserved word */
#define getstatic			0xb2
#define putstatic			0xb3
#define getfield			0xb4 /* 180 */
#define putfield			0xb5
#define invokevirtual			0xb6
#define invokespecial			0xb7
#define invokestatic			0xb8
#define invokeinterface			0xb9
#define in_new	 			0xba
#define new				0xbb
#define newarray			0xbc
#define anewarray			0xbd
#define arraylength			0xbe /* 190 */
#define athrow				0xbf
#define checkcast			0xc0
#define instanceof			0xc1
#define monitorenter			0xc2
#define monitorexit			0xc3
#define wide				0xc4
#define multianewarray			0xc5
#define ifnull				0xc6
#define ifnonnull			0xc7
#define goto_w				0xc8 /* 200 */
#define jsr_w				0xc9
#define breakpoint			0xca
#define in_getfield_byte		0xcb
#define in_getfield_single		0xcc
#define in_getfield_double		0xcd
#define in_getfield_ref			0xce
#define in_invokestatic                 0xcf
#define in_invokenonvirtual             0xd0
#define in_invokesuper                  0xd1
#define in_invokevirtual                0xd2 /* 210 */
#define in_invokefast                   0xd3
#define in_invokeinterface              0xd4
#define in_getstatic_ref		0xd5
#define in_getstatic_double		0xd6
#define in_getstatic_single		0xd7
#define in_putstatic_ref		0xd8
#define in_putstatic_double		0xd9
#define in_putstatic_single		0xda
// spare				0xdb
#define in_putfield_byte		0xdc /* 220 */
#define in_putfield_single		0xdd
#define in_putfield_double		0xde
#define in_putfield_ref			0xdf
#define in_ldc_class			0xe0
#define in_ldc_string			0xe1
#define in_ldc_scalar			0xe2
#define in_ldc_w_class			0xe3
#define in_ldc_w_string			0xe4
#define in_ldc_w_scalar			0xe5

/*
** Missing codes
*/

#define no_code02			0xe6
#define no_code03			0xe7
#define no_code04			0xe8
#define no_code05			0xe9
#define no_code06			0xea
#define no_code07			0xeb
#define no_code08			0xec
#define no_code09			0xed
#define no_code10			0xee
#define no_code11			0xef
#define no_code12			0xf0
#define no_code13			0xf1
#define no_code14			0xf2
#define no_code15			0xf3
#define no_code16			0xf4
#define no_code17			0xf5
#define no_code18			0xf6
#define no_code19			0xf7
#define no_code20			0xf8
#define no_code21			0xf9
#define no_code22			0xfa
#define no_code23			0xfb
#define no_code24			0xfc
#define no_code25			0xfd

/*
** Some more reserved codes
*/

#define impdep1				0xfe
#define impdep2				0xff

/*
** A structure that is usefull in debugging the interpreter loop
** and for building a opcode dumper...
*/

#define CF_FLAGS                0x00000000
#define CF_CAN_BE_WIDE          0x00000001
#define CF_VAR_ARGUMENTS        0x00000002
#define CF_VAR_STACK            0x00000004
#define CF_BASIC_BLOCK          0x00000008
#define CF_EXCEPTION_SRC        0x00000010
#define CF_END                  0x00000020
#define CF_INVOKE               0x00000040

#define CF_NULLP_EXC            0x00010000
#define CF_AINDX_EXC            0x00020000
#define CF_ASTOR_EXC            0x00040000
#define CF_ANEGS_EXC            0x00080000
#define CF_ILMON_EXC            0x00100000
#define CF_CLCST_EXC            0x00200000
#define CF_ERROR_EXC            0x00400000
#define CF_ARITH_EXC            0x00800000

typedef struct opcode_i {
  const char *name;
  unsigned char code;
  unsigned char arguments;  /* the number of argument bytes for this opcode in the code array */
  unsigned char c_stack;    /* the number of stack items consumed (popped) by this opcode */
  unsigned char p_stack;    /* the number of stack items produced (pushed) by this opcode */
  w_flags flags;
  w_size  called;           /* the number of times this opcode was called */
  w_size  total;            /* the total number of ticks these calls took */
} opcode_i;

extern opcode_i opcode_info[];

#define opc2name(opcode) ((unsigned char *)opcode_info[(opcode)].name)

//#ifdef DEBUG
extern const char *opcode_names[];
//#endif

#endif /* _OPCODES_H */
