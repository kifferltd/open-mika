/**************************************************************************
* Parts copyright (c) 2001, 2002 by Punch Telematix. All rights reserved. *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2008, 2011 by Chris Gray,   *
* /k/ Embedded Java Solutions. All rights reserved.                       *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#include "clazz.h"
#include "constant.h"
#include "interpreter.h"
#include "methods.h"
#include "opcodes.h" 

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

