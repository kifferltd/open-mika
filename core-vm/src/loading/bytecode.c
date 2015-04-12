/**************************************************************************
* Copyright (c) 2004, 2005, 2006, 2007, 2008, 2011, 2015 by Chris Gray,   *
* /k/ Embedded Java Solutions and KIFFER Ltd. All rights reserved.        *
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
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
*  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,    *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

#include "bytecode.h"
#include "clazz.h"
#include "constant.h"
#include "core-classes.h"
#include "fastcall.h"
#include "interpreter.h"
#ifdef JDWP
#include "jdwp.h"
#endif
#include "methods.h"
#include "opcodes.h" 
#include "wstrings.h"

/*
** Lengths of the various instructions, indexed by opcode.
*/
const w_ushort instruction_length_and_features[256] = {
                            1, // nop             0x00
                            1, // aconst_null     0x01
                            1, // iconst_m1       0x02
                            1, // iconst_0        0x03
                            1, // iconst_1        0x04
                            1, // iconst_2        0x05
                            1, // iconst_3        0x06
                            1, // iconst_4        0x07
                            1, // iconst_5        0x08
                            1, // lconst_0        0x09
                            1, // lconst_1        0x0a
                            1, // fconst_0        0x0b
                            1, // fconst_1        0x0c
                            1, // fconst_2        0x0d
                            1, // dconst_0        0x0e
                            1, // dconst_1        0x0f
                            2, // bipush          0x10
                            3, // sipush          0x11
                            2, // ldc             0x12
                            3, // ldc_w           0x13
                            3, // ldc2_w          0x14
  INSTRUCTION_CAN_BE_WIDE + 2, // iload           0x15
  INSTRUCTION_CAN_BE_WIDE + 2, // lload           0x16
  INSTRUCTION_CAN_BE_WIDE + 2, // fload           0x17
  INSTRUCTION_CAN_BE_WIDE + 2, // dload           0x18
  INSTRUCTION_CAN_BE_WIDE + 2, // aload           0x19
  INSTRUCTION_USES_VAR_0  + 1, // iload_0         0x1a
  INSTRUCTION_USES_VAR_1  + 1, // iload_1         0x1b
  INSTRUCTION_USES_VAR_2  + 1, // iload_2         0x1c
  INSTRUCTION_USES_VAR_3  + 1, // iload_3         0x1d
  INSTRUCTION_USES_VAR_1  + 1, // lload_0         0x1e
  INSTRUCTION_USES_VAR_2  + 1, // lload_1         0x1f
  INSTRUCTION_USES_VAR_3  + 1, // lload_2         0x20
  INSTRUCTION_USES_VAR_4  + 1, // lload_3         0x21
  INSTRUCTION_USES_VAR_0  + 1, // fload_0         0x22
  INSTRUCTION_USES_VAR_1  + 1, // fload_1         0x23
  INSTRUCTION_USES_VAR_2  + 1, // fload_2         0x24
  INSTRUCTION_USES_VAR_3  + 1, // fload_3         0x25
  INSTRUCTION_USES_VAR_1  + 1, // dload_0         0x26
  INSTRUCTION_USES_VAR_2  + 1, // dload_1         0x27
  INSTRUCTION_USES_VAR_3  + 1, // dload_2         0x28
  INSTRUCTION_USES_VAR_4  + 1, // dload_3         0x29
  INSTRUCTION_USES_VAR_0  + 1, // aload_0         0x2a
  INSTRUCTION_USES_VAR_1  + 1, // aload_1         0x2b
  INSTRUCTION_USES_VAR_2  + 1, // aload_2         0x2c
  INSTRUCTION_USES_VAR_3  + 1, // aload_3         0x2d
                            1, // iaload          0x2e
                            1, // laload          0x2f
                            1, // faload          0x30
                            1, // daload          0x31
                            1, // aaload          0x32
                            1, // baload          0x33
                            1, // caload          0x34
                            1, // saload          0x35
  INSTRUCTION_CAN_BE_WIDE + 2, // istore          0x36
  INSTRUCTION_CAN_BE_WIDE + 2, // lstore          0x37
  INSTRUCTION_CAN_BE_WIDE + 2, // fstore          0x38
  INSTRUCTION_CAN_BE_WIDE + 2, // dstore          0x39
  INSTRUCTION_CAN_BE_WIDE + 2, // astore          0x3a
  INSTRUCTION_USES_VAR_0  + 1, // istore_0        0x3b
  INSTRUCTION_USES_VAR_1  + 1, // istore_1        0x3c
  INSTRUCTION_USES_VAR_2  + 1, // istore_2        0x3d
  INSTRUCTION_USES_VAR_3  + 1, // istore_3        0x3e
  INSTRUCTION_USES_VAR_1  + 1, // lstore_0        0x3f
  INSTRUCTION_USES_VAR_2  + 1, // lstore_1        0x40
  INSTRUCTION_USES_VAR_3  + 1, // lstore_2        0x41
  INSTRUCTION_USES_VAR_4  + 1, // lstore_3        0x42
  INSTRUCTION_USES_VAR_0  + 1, // fstore_0        0x43
  INSTRUCTION_USES_VAR_1  + 1, // fstore_1        0x44
  INSTRUCTION_USES_VAR_2  + 1, // fstore_2        0x45
  INSTRUCTION_USES_VAR_3  + 1, // fstore_3        0x46
  INSTRUCTION_USES_VAR_1  + 1, // dstore_0        0x47
  INSTRUCTION_USES_VAR_2  + 1, // dstore_1        0x48
  INSTRUCTION_USES_VAR_3  + 1, // dstore_2        0x49
  INSTRUCTION_USES_VAR_4  + 1, // dstore_3        0x4a
  INSTRUCTION_USES_VAR_0  + 1, // astore_0        0x4b
  INSTRUCTION_USES_VAR_1  + 1, // astore_1        0x4c
  INSTRUCTION_USES_VAR_2  + 1, // astore_2        0x4d
  INSTRUCTION_USES_VAR_3  + 1, // astore_3        0x4e
                            1, // iastore         0x4f
                            1, // lastore         0x50
                            1, // fastore         0x51
                            1, // dastore         0x52
                            1, // aastore         0x53
                            1, // bastore         0x54
                            1, // castore         0x55
                            1, // sastore         0x56
                            1, // pop             0x57
                            1, // pop2            0x58
                            1, // dup             0x59
                            1, // dup_x1          0x5a
                            1, // dup_x2          0x5b
                            1, // dup2            0x5c
                            1, // dup2_x1         0x5d
                            1, // dup2_x2         0x5e
                            1, // swap            0x5f
                            1, // iadd            0x60
                            1, // ladd            0x61
                            1, // fadd            0x62
                            1, // dadd            0x63
                            1, // isub            0x64
                            1, // lsub            0x65
                            1, // fsub            0x66
                            1, // dsub            0x67
                            1, // imul            0x68
                            1, // lmul            0x69
                            1, // fmul            0x6a
                            1, // dmul            0x6b
                            1, // idiv            0x6c
                            1, // ldiv            0x6d
                            1, // fdiv            0x6e
                            1, // ddiv            0x6f
                            1, // irem            0x70
                            1, // lrem            0x71
                            1, // frem            0x72
                            1, // drem            0x73
                            1, // ineg            0x74
                            1, // lneg            0x75
                            1, // fneg            0x76
                            1, // dneg            0x77
                            1, // ishl            0x78
                            1, // lshl            0x79
                            1, // ishr            0x7a
                            1, // lshr            0x7b
                            1, // iushr           0x7c
                            1, // lushr           0x7d
                            1, // iand            0x7e
                            1, // land            0x7f
                            1, // ior             0x80
                            1, // lor             0x81
                            1, // ixor            0x82
                            1, // lxor            0x83
  INSTRUCTION_CAN_BE_WIDE + 3, // iinc            0x84
                            1, // i2l             0x85
                            1, // i2f             0x86
                            1, // i2d             0x87
                            1, // l2i             0x88
                            1, // l2f             0x89
                            1, // l2d             0x8a
                            1, // f2i             0x8b
                            1, // f2l             0x8c
                            1, // f2d             0x8d
                            1, // d2i             0x8e
                            1, // d2l             0x8f
                            1, // d2f             0x90
                            1, // i2b             0x91
                            1, // i2c             0x92
                            1, // i2s             0x93
                            1, // lcm             0x94
                            1, // fcmpl           0x95
                            1, // fcmpg           0x96
                            1, // dcmpl           0x97
                            1, // dcmpg           0x98
                            3, // ifeq            0x99
                            3, // ifne            0x9a
                            3, // iflt            0x9b
                            3, // ifge            0x9c
                            3, // ifgt            0x9d
                            3, // ifle            0x9e
                            3, // if_icmpeq       0x9f
                            3, // if_icmpne       0xa0
                            3, // if_icmplt       0xa1
                            3, // if_icmpge       0xa2
                            3, // if_icmpgt       0xa3
                            3, // if_icmple       0xa4
                            3, // if_acmpeq       0xa5
                            3, // if_acmpne       0xa6
                            3, // j_goto          0xa7
                            3, // jsr             0xa8
  INSTRUCTION_CAN_BE_WIDE + 2, // ret             0xa9
                            0, // tableswitch     0xaa
                            0, // lookupswitch    0xab
                            1, // ireturn         0xac
                            1, // lreturn         0xad
                            1, // freturn         0xae
                            1, // dreturn         0xaf
                            1, // areturn         0xb0
                            1, // vreturn         0xb1
                            3, // getstatic       0xb2
                            3, // putstatic       0xb3
                            3, // getfield        0xb4
                            3, // putfield        0xb5
                            3, // invokevirtual   0xb6
                            3, // invokespecial   0xb7
                            3, // invokestatic    0xb8
                            5, // invokeinterface 0xb9
                            3, // invokedynamic   0xba (not supported)
                            3, // new             0xbb
                            2, // newarray        0xbc
                            3, // anewarray       0xbd
                            1, // arraylength     0xbe
                            1, // athrow          0xbf
                            3, // checkcast       0xc0
                            3, // instanceof      0xc1
                            1, // monitorenter    0xc2
                            1, // monitorexit     0xc3
                            1, // wide	          0xc4
                            4, // multianewarray  0xc5
                            3, // ifnull          0xc6
                            3, // ifnonnull       0xc7
                            5, // goto_w          0xc8
                            5, // jsr_w	          0xc9
                            1, // breakpoint      0xca
  INSTRUCTION_UNDEFINED +   1, //                 0xcb
  INSTRUCTION_UNDEFINED +   1, //                 0xcc
  INSTRUCTION_UNDEFINED +   1, //                 0xcd
  INSTRUCTION_UNDEFINED +   1, //                 0xce
  INSTRUCTION_UNDEFINED +   1, //                 0xcf
  INSTRUCTION_UNDEFINED +   1, //                 0xd0
  INSTRUCTION_UNDEFINED +   1, //                 0xd1
  INSTRUCTION_UNDEFINED +   1, //                 0xd2
  INSTRUCTION_UNDEFINED +   1, //                 0xd3
  INSTRUCTION_UNDEFINED +   1, //                 0xd4
  INSTRUCTION_UNDEFINED +   1, //                 0xd5
  INSTRUCTION_UNDEFINED +   1, //                 0xd6
  INSTRUCTION_UNDEFINED +   1, //                 0xd7
  INSTRUCTION_UNDEFINED +   1, //                 0xd8
  INSTRUCTION_UNDEFINED +   1, //                 0xd9
  INSTRUCTION_UNDEFINED +   1, //                 0xda
  INSTRUCTION_UNDEFINED +   1, //                 0xdb
  INSTRUCTION_UNDEFINED +   1, //                 0xdc
  INSTRUCTION_UNDEFINED +   1, //                 0xdd
  INSTRUCTION_UNDEFINED +   1, //                 0xde
  INSTRUCTION_UNDEFINED +   1, //                 0xdf
  INSTRUCTION_UNDEFINED +   1, //                 0xe0
  INSTRUCTION_UNDEFINED +   1, //                 0xe1
  INSTRUCTION_UNDEFINED +   1, //                 0xe2
  INSTRUCTION_UNDEFINED +   1, //                 0xe3
  INSTRUCTION_UNDEFINED +   1, //                 0xe4
  INSTRUCTION_UNDEFINED +   1, //                 0xe5
  INSTRUCTION_UNDEFINED +   1, //                 0xe6
  INSTRUCTION_UNDEFINED +   1, //                 0xe7
  INSTRUCTION_UNDEFINED +   1, //                 0xe8
  INSTRUCTION_UNDEFINED +   1, //                 0xe9
  INSTRUCTION_UNDEFINED +   1, //                 0xea
  INSTRUCTION_UNDEFINED +   1, //                 0xeb
  INSTRUCTION_UNDEFINED +   1, //                 0xec
  INSTRUCTION_UNDEFINED +   1, //                 0xed
  INSTRUCTION_UNDEFINED +   1, //                 0xee
  INSTRUCTION_UNDEFINED +   1, //                 0xef
  INSTRUCTION_UNDEFINED +   1, //                 0xf0
  INSTRUCTION_UNDEFINED +   1, //                 0xf1
  INSTRUCTION_UNDEFINED +   1, //                 0xf2
  INSTRUCTION_UNDEFINED +   1, //                 0xf3
  INSTRUCTION_UNDEFINED +   1, //                 0xf4
  INSTRUCTION_UNDEFINED +   1, //                 0xf5
  INSTRUCTION_UNDEFINED +   1, //                 0xf6
  INSTRUCTION_UNDEFINED +   1, //                 0xf7
  INSTRUCTION_UNDEFINED +   1, //                 0xf8
  INSTRUCTION_UNDEFINED +   1, //                 0xf9
  INSTRUCTION_UNDEFINED +   1, //                 0xfa
  INSTRUCTION_UNDEFINED +   1, //                 0xfb
  INSTRUCTION_UNDEFINED +   1, //                 0xfc
  INSTRUCTION_UNDEFINED +   1, //                 0xfd
  INSTRUCTION_UNDEFINED +   1, //                 0xfe
  INSTRUCTION_UNDEFINED +   1, //                 0xff
};

/*
 * Error messages
 */
char* array_too_few_dims = (char*)"array type has too few dimensions";
char* array_not_correct = (char*)"not an array of appropriate type";
char* arg_not_correct = (char*)"argument of wrong type";
char* bad_angle_bracket = (char*)"invoking a method with name starting with '<' (and not invokespecial of <init>)";
char* bad_branch_dest = (char*)"branch out of method code";
char* bad_constant_index = (char*)"constant index out of bounds";
char* bad_invoke_constructor = (char*)"<init> method not invoked with invokespecial";
char* bad_local_var = (char*)"attempting to access a local variable beyond local array";
char *bad_return_address = (char*)"return address corrupted";
char* branch_dest_not_instr = (char*)"branch destination is not on an instruction boundary";
char* catch_not_throwable = (char*)"exception handler catches non-Throwable type";
char* count_not_integer = (char*)"count is not integer";
char* exception_pc_beyond_end  = (char*)"exception start/end/handler_pc is beyond end of method code";
char* exception_pc_not_instr = (char*)"exception start/end/handler_pc is not on an exception boundary";
char* fall_off_end = (char*)"control flow falls off end of method code";
char* illegal_opcode = (char*)"illegal opcode";
char* illegal_wide_opcode = (char*)"illegal opcode following wide prefix";
char* index_not_integer = (char*)"array index is not integer";
char* invokeinterface_byte4_zero = (char*)"fourth byte of invokeinterface is zero";
char* invokeinterface_byte5_nonzero = (char*)"fifth byte of invokeinterface is nonzero";
char* invokestatic_not_static = (char*)"invokestatic on non-static method";
char* instruction_truncated = (char*)"instruction truncated";
char* ldc_bad_constant = (char*)"ldc/ldc_w/ldc2_w on constant pool entry of wrong type";
char* local_not_double = (char*)"local var is not double";
char* local_not_float = (char*)"local var is not float";
char* local_not_integer = (char*)"local var is not integer";
char* local_not_long = (char*)"local var is not long";
char* local_not_reference = (char*)"local var is not a reference type";
char* lookupswitch_bad_operand = (char*)"lookupswitch with npairs < 0";
char* lookupswitch_out_of_order = (char*)"lookupswitch match values out of order";
char* member_not_static = (char*)"member is not static";
char* member_is_static = (char*)"member is static";
char* member_not_interface = (char*)"member is not interface";
char* member_is_interface = (char*)"member is interface";
char* multianewarray_dims_too_big = (char*)"multianewarray has dimensions greater than array class";
char* multianewarray_dims_zero = (char*)"multianewarray has zero dimensions";
char* newarray_bad_operand = (char*)"newarray operand not in the range [4,11]";
char* new_creates_array = (char*)"new instruction used to create an array";
char* not_class_constant = (char*)"non-Class constant in anewarray/checkclass/instanceof/new/multianewarray";
char* not_doubleword = (char*)"not a long/double";
char* not_fieldref = (char*)"non-Fieldref constant in [get/put][field/static]";
char* not_imethodref = (char*)"non-IMethodref constant in invokeinterface";
char* not_methodref = (char*)"non-Methodref constant in invoke[static/virtual/special]";
char* objectref_wrong_type = (char*)"objectref has wrong type";
char* parameter_type_mismatch = (char*)"actual parameter cannot be assigned to formal parameter";
char* return_not_match_type = (char*)"return bytecode does not match return type of method";
char *splitting_value = (char*)"attempt to split two-word value";
char* stack_overflow = (char*)"stack overflow";
char* stack_underflow = (char*)"stack underflow";
char* tableswitch_bad_high_low = (char*)"tableswitch with high < low";
char* too_few_locals = (char*)"non-static method has no local vars";
char* value_not_array = (char*)"value is not an array type";
char* value_not_correct = (char*)"value to be stored has wrong type";
char* value_not_reference = (char*)"value is not a reference type";

/*
 * Ensure the destination of a branch instruction is within the bytecode of the method
 */
#define BRANCH_IN_BOUNDS(_N) V_ASSERT((unsigned)(_N) < codelen, bad_branch_dest)

/*
** Get the dimensionality of a class constant. Returns 0 if the class is scalar,
** or the number of dimensions if it is an array class.
** Note: #dims can be > 255, the caller should check this.
*/
w_int getClassConstantDims(w_clazz declaring_clazz, w_size idx, w_thread thread) {
  w_string clazzname = NULL;
  w_int dims = 0;
  threadMustBeSafe(currentWonkaThread);

  x_monitor_eternal(declaring_clazz->resolution_monitor);
  while (declaring_clazz->tags[idx] == RESOLVING_CLASS) {
    x_monitor_wait(declaring_clazz->resolution_monitor, 2);
  }

  if (declaring_clazz->tags[idx] == CONSTANT_CLASS) {
    w_int name_index = declaring_clazz->values[idx];
    clazzname = resolveUtf8Constant(declaring_clazz, name_index);
  }
  else if (CONSTANT_STATE(declaring_clazz->tags[idx]) == RESOLVED_CONSTANT) {
    w_clazz c = (w_clazz)declaring_clazz->values[idx];

    clazzname = c->dotified;

  }
  else {
    return -1;
  }
  x_monitor_exit(declaring_clazz->resolution_monitor);

  while (string_char(clazzname, dims) == '[') {
    ++dims;
  }

  return dims;
 }

/*
** Identify instruction and basic block boundaries.
**
** Returns a status_array which has one byte for every byte of the original 
** bytecode, contatining flags: see the definitions of START_BLOCK, END_BLOCK,
** IS_INSTRUCTION, WIDE_MODDED.
** If errors are found, an exception is raised and NULL is returned.
*/
w_ubyte *identifyBoundaries(w_method method, w_thread thread) {
  w_ubyte *status_array;
  w_size codelen = method->exec.code_length;
  w_code code = method->exec.code;
  w_boolean is_wide;
  w_size pc = 0;
  w_size newpc = 0;
  w_size n = 0;
  w_int i;
  w_int branchoffset;
  w_ubyte opcode;
  w_int low;
  w_int high;
  w_size inslen;
  w_clazz declaring_clazz = method->spec.declaring_clazz;
  char *error_cstring;
#ifdef DEBUG
  int lineno;
#endif

  status_array = allocClearedMem(method->exec.code_length);
  woempa(1, "Identifying instruction and basic block boundaries in %M, codelen = %d\n", method, codelen);
  status_array[0] |= START_BLOCK;
  is_wide = FALSE;
  pc = 0;
  while (pc < codelen) {
    status_array[pc] |= IS_INSTRUCTION;
    opcode = code[pc];
    inslen = instruction_length(opcode);
    if (is_wide) {
      inslen += (opcode == iinc ? 2 : 1);
    }
    woempa(7, "instruction at pc[%d] is %s%s\n", pc, is_wide ? "wide " : "", opc2name(opcode));
  
    V_ASSERT(codelen - pc >= inslen, instruction_truncated);
    V_ASSERT(!instruction_undefined(opcode), illegal_opcode);
    V_ASSERT(!is_wide || instruction_can_be_wide(opcode), illegal_wide_opcode);

    switch(opcode) {
    case ldc:
    case ldc_w:
    case ldc2_w:
    case getfield:
    case putfield:
    case getstatic: 
    case putstatic:
    case instanceof:
    case checkcast:
    case multianewarray:
    case new:
    case anewarray:
    case invokevirtual:
    case invokestatic:
    case invokespecial:
    case invokeinterface:
    case newarray:
      break;
 
    /***********************************************************
    * Instructions that can be modified by WIDE
    ***********************************************************/
    case wide:
      is_wide = TRUE;
      break;
 
    case aload:
    case astore:
    case iload:
    case istore:
    case fload:
    case fstore:
      if (is_wide) {
        is_wide = FALSE;
        /* the WIDE is considered the beginning of the instruction */
        status_array[pc] ^= IS_INSTRUCTION;
        status_array[pc] |= WIDE_MODDED;
        pc++;
      }
      break;

    case lload:
    case lstore:
    case dload:
    case dstore:
      if (is_wide) {
        is_wide = FALSE;
      /* the WIDE is considered the beginning of the instruction */
        status_array[pc] ^= IS_INSTRUCTION;
        status_array[pc] |= WIDE_MODDED;
        pc++;
      }
      break;

    case iinc:
      if (is_wide) {
        is_wide = FALSE;
      /* the WIDE is considered the beginning of the instruction */
        status_array[pc] ^= IS_INSTRUCTION;
        status_array[pc] |= WIDE_MODDED;
        pc += 2;
      }
      break;
 
    /********************************************************************
    * BRANCHING INSTRUCTIONS
    ********************************************************************/
    case j_goto:
      status_array[pc] |= END_BLOCK;

      branchoffset = (code[pc + 1] << 8) | code[pc + 2];
      if (branchoffset & 0x8000) {
        branchoffset |= 0xffff0000;
      }
      goto goto_common;

    case goto_w:
      status_array[pc] |= END_BLOCK;

      branchoffset = (((((code[pc + 1] << 8) | code[pc + 2]) << 8) | code[pc + 3]) << 8) | code[pc + 4];

    goto_common:
      newpc = pc + branchoffset;
      BRANCH_IN_BOUNDS(newpc);
      status_array[newpc] |= START_BLOCK;
      break;

    case ifnonnull:
    case ifnull:
    case ifeq:
    case ifne:
    case ifgt:
    case ifge:
    case iflt:
    case ifle:
    case if_acmpeq:
    case if_acmpne:
    case if_icmpeq:
    case if_icmpne:
    case if_icmpgt:
    case if_icmpge:
    case if_icmplt:
    case if_icmple:
      status_array[pc] |= END_BLOCK;
 
      newpc = pc + instruction_length(code[pc]);
      BRANCH_IN_BOUNDS(newpc);
      status_array[newpc] |= START_BLOCK;
 
      branchoffset = (code[pc + 1] << 8) | code[pc + 2];
      if (branchoffset & 0x8000) {
        branchoffset |= 0xffff0000;
      }
      newpc = pc + branchoffset;
      BRANCH_IN_BOUNDS(newpc);
      status_array[newpc] |= START_BLOCK;
      break;
 
    case jsr:
      woempa(1, "0x%02x 0x%02x 0x%02x\n", code[pc], code [pc + 1], code[pc + 2]);
      branchoffset = (code[pc + 1] << 8) | code[pc + 2];
      if (branchoffset & 0x8000) {
        branchoffset |= 0xffff0000;
      }
      goto jsr_common;

    case jsr_w:
      branchoffset = (((((code[pc + 1] << 8) | code[pc + 2]) << 8) | code[pc + 3]) << 8) | code[pc + 4];

    jsr_common:
      newpc = pc + branchoffset;
      status_array[pc] |= END_BLOCK;

      woempa(1, "jsr target pc = %d\n", newpc);
      BRANCH_IN_BOUNDS(newpc);
      status_array[newpc] |= START_BLOCK;

      /* the next instruction is a target for branching via RET */
      pc = pc + instruction_length(code[pc]);
      BRANCH_IN_BOUNDS(pc);
      status_array[pc] |= START_BLOCK;
      // 'continue' rather than 'break' in order to skip normal incrementing of pc
      continue;

    case ret:
      status_array[pc] |= END_BLOCK;
      if (is_wide) {
        is_wide = FALSE;

        status_array[pc] ^= IS_INSTRUCTION;
        status_array[pc] |= WIDE_MODDED;

        ++pc;
      }
      pc = pc + instruction_length(opcode);
      // 'continue' rather than 'break' in order to skip normal incrementing of pc
      continue;

    case lookupswitch:
      {
        w_int npairs;
        w_size nextpc;

        status_array[pc] |= END_BLOCK;

        /* default branch...between 0 and 3 bytes of padding are added so that the
         * default branch is at an address that is divisible by 4
         */
        n = (pc + 4) & -4;
        woempa(1, "lookupswitch default branch offset is at pc[%d]\n", n);
        branchoffset = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
        newpc = pc + branchoffset;
        BRANCH_IN_BOUNDS(newpc);
        status_array[newpc] |= START_BLOCK;
  
        n += 4;
        npairs = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
        V_ASSERT(npairs >= 0, lookupswitch_bad_operand);
  
        n += 4;
        nextpc = n + 8 * npairs;
        BRANCH_IN_BOUNDS(nextpc);
        status_array[newpc] |= START_BLOCK;

        /* TODO: make sure match values are sorted */
        /* make sure all targets are in bounds */
        while (n < nextpc) {
          n += 4;

          branchoffset = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
          newpc = pc + branchoffset;
          BRANCH_IN_BOUNDS(newpc);
          status_array[newpc] |= START_BLOCK;
          n += 4;
        }
        pc = nextpc;
      }

      // 'continue' rather than 'break' in order to skip normal incrementing of pc
      continue;

    case tableswitch:
      status_array[pc] |= END_BLOCK;

      /* From 0 to 3 bytes of padding follow the opcode so that all operands
       * begin on a word boundary. We set 'n' to the first byte after the
       * padding (being the start of the default branch offset).
       */
      n = (pc + 4) & -4;
      woempa(1, "tableswitch default branch offset is at pc[%d]\n", n);
      branchoffset = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
      newpc = pc + branchoffset;
      woempa(1, "tableswitch: default branch offset = %d, target = %d\n", branchoffset, newpc);
      BRANCH_IN_BOUNDS(newpc);
      status_array[newpc] |= START_BLOCK;

      /* get the high and low values of the table */
      n += 4;
      low = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
      n += 4;
      high = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
      woempa(1, "tableswitch: low = %d, high = %d\n", low, high);

      while (low <= high) {
        n += 4;
        branchoffset = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
        newpc = pc + branchoffset;
        woempa(1, "tableswitch: branch offset [%d] = %d, target = %d\n", low, branchoffset, newpc);
        BRANCH_IN_BOUNDS(newpc);
        status_array[newpc] |= START_BLOCK;
        ++low;
      }
      pc = n + 4;
      // 'continue' rather than 'break' in order to skip normal incrementing of pc
      continue;
 
 
    /* the rest of the ways to end a block */
    case vreturn:
    case areturn:
    case ireturn:
    case freturn:
    case lreturn:
    case dreturn:
    case athrow:
      status_array[pc] |= END_BLOCK;
      break;
      
    default:
      ;
    }

    pc = pc + instruction_length(code[pc]);
  }

  if (method->exec.numExceptions) {
    w_exception ex = method->exec.exceptions;

    woempa(1, "Parsing exception table\n");
    for (i = 0; i < method->exec.numExceptions; ++i, ++ex) {
      woempa(1, "Exception[%d]: start_pc = %d, end_pc = %d, handler_pc = %d, type_index = %d\n", i, ex->start_pc, ex->end_pc, ex->handler_pc, ex->type_index);
      pc = ex->start_pc;
      V_ASSERT(pc < codelen, exception_pc_beyond_end);
      V_ASSERT(status_array[pc] & IS_INSTRUCTION, exception_pc_not_instr);
      status_array[pc] |= START_BLOCK;
			
      pc = ex->end_pc;
      V_ASSERT(pc <= codelen, exception_pc_beyond_end);
      if (pc < codelen) {
        V_ASSERT(status_array[pc] & IS_INSTRUCTION, exception_pc_not_instr);
        status_array[pc] |= START_BLOCK;
      }
			
      pc = ex->handler_pc;
      V_ASSERT(pc < codelen, exception_pc_beyond_end);
      V_ASSERT(status_array[pc] & IS_INSTRUCTION, exception_pc_not_instr);
      status_array[pc] |= START_BLOCK;

    }
  }

  return status_array;

verify_error:
#ifdef DEBUG
  throwException(currentWonkaThread, clazzVerifyError, "identifyBoundaries() line %d: %k.%m pc %d (%s): %s", lineno, declaring_clazz, method, pc, opc2name(method->exec.code[pc]), error_cstring);
#else
  throwException(currentWonkaThread, clazzVerifyError, "%k.%m pc %d (%s): %s", declaring_clazz, method, pc, opc2name(method->exec.code[pc]), error_cstring);
#endif

  releaseMem(status_array);

  return NULL;
}

/*
** In the following function, we rewrite the jump codes in the native
** endianess format of the CPU of lookupswitch and tableswitch. Not other
** indexes since they are not guaranteed to be on properly aligned boundaries.
** We also resolve any string constants that are used, to save time later.
*/

void prepareBytecode(w_method method) {
  w_clazz cclazz = method->spec.declaring_clazz;
  w_int pc = 0;
#ifdef USE_SPECIAL_CASE_DISPATCHERS
  w_int first_real_opcode = 0;
#endif
  w_ConstantType *tag;
  w_int i;
  w_int * n;
  w_Mopair * mopair;
  unsigned char * bytecode;

  if (isSet(method->flags, ACC_ABSTRACT | ACC_NATIVE)) {
    return;
  }

  threadMustBeSafe(currentWonkaThread);

  bytecode = method->exec.code;

  while (pc < method->exec.code_length) {
    int bc = bytecode[pc];

#ifdef JDWP
    if (bc == breakpoint) {
      bc = jdwp_breakpoint_get_original(bytecode + pc);
    }
#endif

    switch (bc) {
      case aload: 
#ifdef USE_SPECIAL_CASE_DISPATCHERS
        if (pc == first_real_opcode && method->exec.arg_i == 1 && bytecode[pc + 1] == areturn) {
          woempa(7, "%M consists of 'aload_0; areturn'\n", method);
          setFlag(method->flags, METHOD_IS_RETURN_THIS);
        }
#endif
      case astore: case bipush: case dload: case dstore: case fload: case fstore:
      case iload: case istore: case lload: case lstore: case newarray: case ret: {
        pc += 1; 
        break;
      }

      case ldc: {
        i = method->exec.code[++pc];
        tag = &cclazz->tags[i];
        if (*tag == CONSTANT_STRING) {
          resolveStringConstant(cclazz, i);
        }
        break;
      }

      case ldc_w: {
        i = method->exec.code[++pc];
        i = (i << 8) + method->exec.code[++pc];
        tag = &cclazz->tags[i];
        if (*tag == CONSTANT_STRING) {
          resolveStringConstant(cclazz, i);
        }
        break;
      }

      case j_goto:
#ifdef USE_SPECIAL_CASE_DISPATCHERS
        if (pc == first_real_opcode) {
          first_real_opcode += (bytecode[pc + 1] << 8) + bytecode[pc + 2];
          woempa(7, "Bytecode[%d] is 'goto', setting first_real_opcode to %d\n", pc, first_real_opcode);
        }
#endif
        // fall through

      case if_acmpeq: case if_acmpne: case if_icmpeq: case if_icmpne: case if_icmplt: case if_icmpge: 
      case if_icmpgt: case if_icmple: case ifeq: case ifne: case iflt: case ifge: case ifgt: case ifle:
      case ifnonnull: case ifnull: case jsr: case sipush: case anewarray: case instanceof: 
      case checkcast: case ldc2_w: case new: case getfield: case getstatic: 
      case putfield: case putstatic: case iinc: {
        pc += 2;
        break;
      }

      case invokestatic: {
        fastcall_check_invoke_static(cclazz, bytecode+pc+1);
        pc += 2;
        break;
      }

      case invokespecial: {
        fastcall_check_invoke_special(cclazz, bytecode+pc+1);
        pc += 2;
        break;
      }

      case invokevirtual: {    
        fastcall_check_invoke_virtual(cclazz, bytecode+pc+1);
        pc += 2;
        break;
      }
 
      case multianewarray: {
        pc += 3;
        break;
      }
      
      case goto_w:
#ifdef USE_SPECIAL_CASE_DISPATCHERS
        if (pc == first_real_opcode) {
          first_real_opcode += (((((bytecode[pc + 1] << 8) + bytecode[pc + 2]) << 8) + bytecode[pc + 3]) << 8) + bytecode[pc + 4];
          woempa(7, "Bytecode[%d] is 'goto_w', setting first_real_opcode to %d\n", pc, first_real_opcode);
        }
#endif
        // fall through

      case invokeinterface: case jsr_w: {
        pc += 4;
        break;
      }
      
      case wide: {
        pc += 1;
        if (bytecode[pc] == iinc) {
          pc += 2;
        }
        pc += 2;
        break;
      }

      case tableswitch: {
        pc = (pc + 4) & ~3;
        n = (w_int *) & bytecode[pc];
        n[0] = (w_int) ((bytecode[pc + 0] << 24) | (bytecode[pc + 1] << 16) | (bytecode[pc +  2] << 8) | bytecode[pc +  3]);
        n[1] = (w_int) ((bytecode[pc + 4] << 24) | (bytecode[pc + 5] << 16) | (bytecode[pc +  6] << 8) | bytecode[pc +  7]);
        n[2] = (w_int) ((bytecode[pc + 8] << 24) | (bytecode[pc + 9] << 16) | (bytecode[pc + 10] << 8) | bytecode[pc + 11]);
        pc += 12;
        for (i = 3; i < n[2] - n[1] + 1 + 3; i++) {
          n[i] = (w_int) ((bytecode[pc + 0] << 24) | (bytecode[pc + 1] << 16) | (bytecode[pc +  2] << 8) | bytecode[pc +  3]);
          pc += 4;
        }
        pc -= 1;
        break;
      }

      case lookupswitch: {
        pc = (pc + 4) & ~3;
        n = (w_int *) & bytecode[pc];
        n[0] = ((bytecode[pc + 0] << 24) | (bytecode[pc + 1] << 16) | (bytecode[pc + 2] << 8) | bytecode[pc + 3]);
        n[1] = ((bytecode[pc + 4] << 24) | (bytecode[pc + 5] << 16) | (bytecode[pc + 6] << 8) | bytecode[pc + 7]);
        pc += 8;
        mopair = (w_Mopair *) (n + 2);
        for (i = 0; i < n[1]; i++) {
          mopair[i].m = ((bytecode[pc + 0] << 24) | (bytecode[pc + 1] << 16) | (bytecode[pc + 2] << 8) | bytecode[pc + 3]);
          mopair[i].o = ((bytecode[pc + 4] << 24) | (bytecode[pc + 5] << 16) | (bytecode[pc + 6] << 8) | bytecode[pc + 7]);
          pc += 8;
        }
        pc -= 1;
        break;
      }

#ifdef USE_SPECIAL_CASE_DISPATCHERS
      case nop:
        if (pc == first_real_opcode) {
          ++first_real_opcode;
          woempa(7, "Bytecode[%d] is 'nop', setting first_real_opcode to %d\n", pc, first_real_opcode);
        }
        break;

      case aconst_null:
        if (pc == first_real_opcode && bytecode[pc + 1] == areturn) {
          woempa(7, "%M consists of 'aconst_null; areturn'\n", method);
          setFlag(method->flags, METHOD_IS_RETURN_NULL);
        }
        break;

      case vreturn:
        if (pc == first_real_opcode) {
          woempa(7, "%M consists of 'vreturn'\n", method);
          setFlag(method->flags, METHOD_IS_VRETURN);
        }
        break;

      case iconst_m1:
      case iconst_0:
      case iconst_1:
      case iconst_2:
      case iconst_3:
      case iconst_4:
      case iconst_5:
        if (pc == first_real_opcode && bytecode[pc + 1] == ireturn) {
          woempa(7, "%M consists of 'iconst_%d; ireturn'\n", method, bc - iconst_0);
          setFlag(method->flags, METHOD_IS_RETURN_ICONST);
        }
#endif
    }
    
    pc += 1;
    
  }
}

/*
** Rewrite the bytecode as Mika wordcode.
*/

void prepareWordcode(w_method method) {
  w_clazz cclazz = method->spec.declaring_clazz;
  w_int pc = 0;
#ifdef USE_SPECIAL_CASE_DISPATCHERS
  w_int first_real_opcode = 0;
#endif
  w_ConstantType *tag;
  w_int i;
  w_int * n;
  w_Mopair * mopair;
  unsigned char * bytecode;

  if (isSet(method->flags, ACC_ABSTRACT | ACC_NATIVE)) {
    return;
  }

  threadMustBeSafe(currentWonkaThread);

  bytecode = method->exec.code;

  while (pc < method->exec.code_length) {
    int bc = bytecode[pc];

#ifdef JDWP
    if (bc == breakpoint) {
      bc = jdwp_breakpoint_get_original(bytecode + pc);
    }
#endif

    switch (bc) {
      case aload: 
#ifdef USE_SPECIAL_CASE_DISPATCHERS
        if (pc == first_real_opcode && method->exec.arg_i == 1 && bytecode[pc + 1] == areturn) {
          woempa(7, "%M consists of 'aload_0; areturn'\n", method);
          setFlag(method->flags, METHOD_IS_RETURN_THIS);
        }
#endif
      case astore: case bipush: case dload: case dstore: case fload: case fstore:
      case iload: case istore: case lload: case lstore: case newarray: case ret: {
        pc += 1; 
        break;
      }

      case ldc: {
        i = method->exec.code[++pc];
        tag = &cclazz->tags[i];
        if (*tag == CONSTANT_STRING) {
          resolveStringConstant(cclazz, i);
        }
        break;
      }

      case ldc_w: {
        i = method->exec.code[++pc];
        i = (i << 8) + method->exec.code[++pc];
        tag = &cclazz->tags[i];
        if (*tag == CONSTANT_STRING) {
          resolveStringConstant(cclazz, i);
        }
        break;
      }

      case j_goto:
#ifdef USE_SPECIAL_CASE_DISPATCHERS
        if (pc == first_real_opcode) {
          first_real_opcode += (bytecode[pc + 1] << 8) + bytecode[pc + 2];
          woempa(7, "Bytecode[%d] is 'goto', setting first_real_opcode to %d\n", pc, first_real_opcode);
        }
#endif
        // fall through

      case if_acmpeq: case if_acmpne: case if_icmpeq: case if_icmpne: case if_icmplt: case if_icmpge: 
      case if_icmpgt: case if_icmple: case ifeq: case ifne: case iflt: case ifge: case ifgt: case ifle:
      case ifnonnull: case ifnull: case jsr: case sipush: case anewarray: case instanceof: 
      case checkcast: case ldc2_w: case new: case getfield: case getstatic: 
      case putfield: case putstatic: case iinc: {
        pc += 2;
        break;
      }

      case invokestatic: {
        fastcall_check_invoke_static(cclazz, bytecode+pc+1);
        pc += 2;
        break;
      }

      case invokespecial: {
        fastcall_check_invoke_special(cclazz, bytecode+pc+1);
        pc += 2;
        break;
      }

      case invokevirtual: {    
        fastcall_check_invoke_virtual(cclazz, bytecode+pc+1);
        pc += 2;
        break;
      }
 
      case multianewarray: {
        pc += 3;
        break;
      }
      
      case goto_w:
#ifdef USE_SPECIAL_CASE_DISPATCHERS
        if (pc == first_real_opcode) {
          first_real_opcode += (((((bytecode[pc + 1] << 8) + bytecode[pc + 2]) << 8) + bytecode[pc + 3]) << 8) + bytecode[pc + 4];
          woempa(7, "Bytecode[%d] is 'goto_w', setting first_real_opcode to %d\n", pc, first_real_opcode);
        }
#endif
        // fall through

      case invokeinterface: case jsr_w: {
        pc += 4;
        break;
      }
      
      case wide: {
        pc += 1;
        if (bytecode[pc] == iinc) {
          pc += 2;
        }
        pc += 2;
        break;
      }

      case tableswitch: {
        pc = (pc + 4) & ~3;
        n = (w_int *) & bytecode[pc];
        n[0] = (w_int) ((bytecode[pc + 0] << 24) | (bytecode[pc + 1] << 16) | (bytecode[pc +  2] << 8) | bytecode[pc +  3]);
        n[1] = (w_int) ((bytecode[pc + 4] << 24) | (bytecode[pc + 5] << 16) | (bytecode[pc +  6] << 8) | bytecode[pc +  7]);
        n[2] = (w_int) ((bytecode[pc + 8] << 24) | (bytecode[pc + 9] << 16) | (bytecode[pc + 10] << 8) | bytecode[pc + 11]);
        pc += 12;
        for (i = 3; i < n[2] - n[1] + 1 + 3; i++) {
          n[i] = (w_int) ((bytecode[pc + 0] << 24) | (bytecode[pc + 1] << 16) | (bytecode[pc +  2] << 8) | bytecode[pc +  3]);
          pc += 4;
        }
        pc -= 1;
        break;
      }

      case lookupswitch: {
        pc = (pc + 4) & ~3;
        n = (w_int *) & bytecode[pc];
        n[0] = ((bytecode[pc + 0] << 24) | (bytecode[pc + 1] << 16) | (bytecode[pc + 2] << 8) | bytecode[pc + 3]);
        n[1] = ((bytecode[pc + 4] << 24) | (bytecode[pc + 5] << 16) | (bytecode[pc + 6] << 8) | bytecode[pc + 7]);
        pc += 8;
        mopair = (w_Mopair *) (n + 2);
        for (i = 0; i < n[1]; i++) {
          mopair[i].m = ((bytecode[pc + 0] << 24) | (bytecode[pc + 1] << 16) | (bytecode[pc + 2] << 8) | bytecode[pc + 3]);
          mopair[i].o = ((bytecode[pc + 4] << 24) | (bytecode[pc + 5] << 16) | (bytecode[pc + 6] << 8) | bytecode[pc + 7]);
          pc += 8;
        }
        pc -= 1;
        break;
      }

#ifdef USE_SPECIAL_CASE_DISPATCHERS
      case nop:
        if (pc == first_real_opcode) {
          ++first_real_opcode;
          woempa(7, "Bytecode[%d] is 'nop', setting first_real_opcode to %d\n", pc, first_real_opcode);
        }
        break;

      case aconst_null:
        if (pc == first_real_opcode && bytecode[pc + 1] == areturn) {
          woempa(7, "%M consists of 'aconst_null; areturn'\n", method);
          setFlag(method->flags, METHOD_IS_RETURN_NULL);
        }
        break;

      case vreturn:
        if (pc == first_real_opcode) {
          woempa(7, "%M consists of 'vreturn'\n", method);
          setFlag(method->flags, METHOD_IS_VRETURN);
        }
        break;

      case iconst_m1:
      case iconst_0:
      case iconst_1:
      case iconst_2:
      case iconst_3:
      case iconst_4:
      case iconst_5:
        if (pc == first_real_opcode && bytecode[pc + 1] == ireturn) {
          woempa(7, "%M consists of 'iconst_%d; ireturn'\n", method, bc - iconst_0);
          setFlag(method->flags, METHOD_IS_RETURN_ICONST);
        }
#endif
    }
    
    pc += 1;
    
  }
}

