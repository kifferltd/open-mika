/**************************************************************************
* Copyright (c) 2011 by Chris Gray, /k/ Embedded Java Solutions.          *
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

#ifndef _BYTECODE_H
#define _BYTECODE_H

#include "methods.h"

/* status flags for an instruction */
#define IS_INSTRUCTION     1

/* if the instruction is preceeded by WIDE */
#define WIDE_MODDED        2

/* used at the instruction status level to find basic blocks */
#define START_BLOCK        4
#define END_BLOCK          8

#define instruction_length(opc) (instruction_length_and_features[opc] & 0xf)
#define instruction_can_be_wide(opc) (instruction_length_and_features[opc] & INSTRUCTION_CAN_BE_WIDE)
#define instruction_uses_local_var(opc) (instruction_length_and_features[opc] & INSTRUCTION_USES_VAR)
#define instruction_uses_local_var_0(opc) ((instruction_length_and_features[opc] & INSTRUCTION_USES_VAR) == INSTRUCTION_USES_VAR_0)
#define instruction_uses_local_var_1(opc) ((instruction_length_and_features[opc] & INSTRUCTION_USES_VAR) == INSTRUCTION_USES_VAR_1)
#define instruction_uses_local_var_2(opc) ((instruction_length_and_features[opc] & INSTRUCTION_USES_VAR) == INSTRUCTION_USES_VAR_2)
#define instruction_uses_local_var_3(opc) ((instruction_length_and_features[opc] & INSTRUCTION_USES_VAR) == INSTRUCTION_USES_VAR_3)
#define instruction_uses_local_var_4(opc) ((instruction_length_and_features[opc] & INSTRUCTION_USES_VAR) == INSTRUCTION_USES_VAR_4)
#define instruction_undefined(opc) (instruction_length_and_features[opc] & INSTRUCTION_UNDEFINED)

extern char* array_too_few_dims;
extern char* array_not_correct;
extern char* arg_not_correct;
extern char* bad_angle_bracket;
extern char* bad_branch_dest;
extern char* bad_constant_index;
extern char* bad_invoke_constructor;
extern char* bad_local_var;
extern char *bad_return_address;
extern char* branch_dest_not_instr;
extern char* catch_not_throwable;
extern char* count_not_integer;
extern char* exception_pc_beyond_end;
extern char* exception_pc_not_instr;
extern char* fall_off_end;
extern char* illegal_opcode;
extern char* illegal_wide_opcode;
extern char* index_not_integer;
extern char* invokeinterface_byte4_zero;
extern char* invokeinterface_byte5_nonzero;
extern char* invokestatic_not_static;
extern char* instruction_truncated;
extern char* ldc_bad_constant;
extern char* local_not_double;
extern char* local_not_float;
extern char* local_not_integer;
extern char* local_not_long;
extern char* local_not_reference;
extern char* lookupswitch_bad_operand;
extern char* lookupswitch_out_of_order;
extern char* member_not_static;
extern char* member_is_static;
extern char* member_not_interface;
extern char* member_is_interface;
extern char* multianewarray_dims_too_big;
extern char* multianewarray_dims_zero;
extern char* newarray_bad_operand;
extern char* new_creates_array;
extern char* not_class_constant;
extern char* not_doubleword;
extern char* not_fieldref;
extern char* not_imethodref;
extern char* not_methodref;
extern char* objectref_wrong_type;
extern char* parameter_type_mismatch;
extern char* return_not_match_type;
extern char *splitting_value;
extern char* stack_overflow;
extern char* stack_underflow;
extern char* tableswitch_bad_high_low;
extern char* too_few_locals;
extern char* value_not_array;
extern char* value_not_correct;
extern char* value_not_reference;

/*
 * Throw a VerifyError and exit.
 */
#ifdef DEBUG
#define VERIFY_ERROR(cstr) error_cstring = (char*)(cstr); lineno = __LINE__; goto verify_error
#else
#define VERIFY_ERROR(cstr) error_cstring = (cstr); goto verify_error
#endif
#define CHECK_FOR_EXCEPTION(t) if (exceptionThrown(t)) { goto error; }

#define V_ASSERT(cond,msg) if (!(cond)) { VERIFY_ERROR(msg); }

/*
 * Flags for instruction_length_and_features array below:
 */
#define INSTRUCTION_UNDEFINED   0x8000
#define INSTRUCTION_CAN_BE_WIDE 0x1000
#define INSTRUCTION_USES_VAR    0x0f00
#define INSTRUCTION_USES_VAR_0  0x0800
#define INSTRUCTION_USES_VAR_1  0x0100
#define INSTRUCTION_USES_VAR_2  0x0200
#define INSTRUCTION_USES_VAR_3  0x0300
#define INSTRUCTION_USES_VAR_4  0x0400

extern const w_ushort instruction_length_and_features[256];

/*
 * Internal representation of a data type; may be undefined, a class (possibly
 * uninitialised or a primitive class), a union of classes, or a return address.
 */

typedef struct v_Type
{
  /* Discriminator for the union below */
  w_ushort tinfo;

  /* sequence number for tinfo = TINFO_UNINIT, TINFO_UNINIT_SUPER */
  w_ushort uninitpc;
  
  union {
    /* more precision for TINFO_UNDEFINED, see UNDEF_... below */
    w_size undef_kind;

    /* clazz for TINFO_PRIMITIVE, TINFO_CLASS */
    w_clazz clazz;
    
    /* dotified class name for TINFO_UNLOADED_CLASS */
    w_string dotified;

    /* for TINFO_UNION we store an index onto mv->unions */
    w_size union_index;
  
    /* return address for TINFO_ADDR */
    w_size retaddr;
  } data;
} v_Type;

/*
 * Basic block information
 */
typedef struct v_BasicBlock {
  /* The method to which this basic block belongs */
  w_method method;
  /* The index of this block within method->exec.basicBlocks */
  w_size own_index;
  /* Address (pc) of first instruction of block */
  w_ushort start_pc;
  /* Address (pc) of last instruction of block */
  w_ushort last_pc;

  /* Block flags: CHANGED (needs to be re-evaluated), VISITED, etc.  */
  w_word flags;
  
  /* Type of exception handled, if this is the first block of an exception handler */
  w_clazz exception_type;

  /* Return address, if this is the first block of a subroutine */
  w_size return_address;

  /* array of local variables */
  v_Type*  locals;
  w_size localsz;
  
  /* simulated operand stack */
  v_Type*  opstack;
  w_size stacksz;

  /* The size of the successors[] array */
  w_size max_successors;
  /* Per basic block, an indication of whether the basic block is a successor */
  /* of this block (see SUCC_... below). */
  w_ubyte *successors;
} v_BasicBlock;

/* Flags for a basic block.  */
/* The block needs to be (re-evaluated) */
#define CHANGED     16
/* The block has already been evaluated at least once */
#define VISITED     32
/* The block is reachable as part of the normal control flow */
#define NORMAL      64
/* The block is reachable via an exception handler */
#define EXCEPTION  128
/* The block is reachable via a jsr/jsr_w */
#define SUBROUTINE 256

/*
** Kinds of successor; normal transfer of control, exception handler, or
** subroutine.
*/
#define SUCC_NONE 0
#define SUCC_NORMAL 1
#define SUCC_EXCEPTION 2
#define SUCC_JSR 3
#define SUCC_RET 4

/*
 ** Get the dimensionality of a class constant. Returns 0 if the class is scalar,
 ** or the number of dimensions if it is an array class.
 ** Note: #dims can be > 255, the caller should check this.
 */
extern w_int getClassConstantDims(w_clazz declaring_clazz, w_size idx, w_thread thread);

w_ubyte *identifyBoundaries(w_method method, w_thread thread);

#endif /* _BYTECODE_H */

