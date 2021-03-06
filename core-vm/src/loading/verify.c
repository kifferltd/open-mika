/**************************************************************************
* Copyright (c) 2004, 2005, 2006, 2007, 2010, 2011, 2015 by Chris Gray,   *
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
#include "checks.h"
#include "constant.h"
#include "exception.h"
#include "fifo.h"
#include "loading.h"
#include "methods.h"
#include "opcodes.h"
#include "threads.h"
#include "ts-mem.h"
#include "wonka.h"
#include "wordset.h"

/**************************************************************************
* Java bytecode verifier                                                  *
* ======================                                                  *
*                                                                         *
* Written by Chris Gray, mainly in a winter holiday hackathon from 22 to  *
* 31 December 2006.                                                       *
*                                                                         *
* This is a fairly straightforward implementation of Sun's spec, except   *
* that rather than unifing data types up towards java.lang.Object we      *
* build a union of possible types for the slot (whether classes or        *
* interfaces). Verification tends to provoke "eager" class loading.       *
*                                                                         *
* In writing this code I was able to refer to the Kaffe verifier by Rob   *
* Gonzalez <rob@kaffe.org>. Almost none of the code is directly derived   *
* from Rob's, but nonetheless his contribution is hereby acknowledged;    *
* if nothing else, it gave me the courage to go forward. See Rob's        *
* original copyright notice below.                                        *
**************************************************************************/
/*
 *
 * copyright (c) 2003, Rob Gonzalez (rob@kaffe.org)
 *
 * Modifications copyright (c) 2006, Chris Gray, /k/ Embedded Java Solutions.
 *
 * (the following is the MIT/X11 licence)
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

w_word verify_flags; // select classes to be verified, see verifier.h

w_boolean verifyMethod(w_method method, w_thread thread);

/*
** Generate an instance of VerifyError using the name of the failing class
** and two strings, one C (ISO 8859-1) and one Java.
*/
void createVerifyError(w_clazz failing_clazz, char *msg, w_string str) {
  char    *cptr;
  w_char  *buffer;
  w_size   length;
  w_string message;

  length = string_length(failing_clazz->dotified);
  if (msg) {
    length += 2 + strlen(msg);
  }
  if (str) {
    length += 2 + string_length(str);
  }

  buffer = allocMem(length * sizeof(w_char));
  woempa(1, "buffer: %p, length %d\n", buffer, length);
  if (!buffer) {
    // An OutOfMemoryError has been thrown, so that will have to do

    return;

  }

  length = w_string2chars(failing_clazz->dotified, buffer);
  woempa(1, "Wrote `%w', length now %d chars\n", failing_clazz->dotified, length);
  if (msg) {
    buffer[length++] = ':';
    buffer[length++] = ' ';
    for (cptr = msg; *cptr;) {
      buffer[length++] = *cptr++;
    }
    woempa(1, "Wrote `%s', length now %d chars\n", msg, length);
  }
  if (str) {
    buffer[length++] = ':';
    buffer[length++] = ' ';
    length += w_string2chars(str, buffer + length);
    woempa(1, "Wrote `%w', length now %d chars\n", str, length);
  }

  message = unicode2String(buffer, length);
  releaseMem(buffer);
  woempa(9,"%w: VerifyError: %w\n", failing_clazz->dotified, message);
  throwException(currentWonkaThread, clazzVerifyError, "%w", message);
  failing_clazz->failure_message = message;
}

/*
** Load the class corresponding to the type of a field's value.
** The result returned is CLASS_LOADING_xxxxx.
static w_int loadValueType(w_field f) {
  woempa(7, "Value type of %v = %K\n", f, f->value_clazz);
  return mustBeLoaded(&f->value_clazz);
}
*/

/*
** Load the class corresponding to the type of a method's parameters and return value.
** The result returned is CLASS_LOADING_xxxxx.
*/
static w_int loadDescriptorTypes(w_method m) {
  w_int result = CLASS_LOADING_DID_NOTHING;
  w_int j;

  if (m->spec.arg_types) {
    for (j = 0; result != CLASS_LOADING_FAILED && m->spec.arg_types[j]; ++j) {
      woempa(7, "Parameter[%d] of %m = %K\n", j, m, m->spec.arg_types[j]);
      result |= mustBeLoaded(&m->spec.arg_types[j]);
    }
  }
  if (result != CLASS_LOADING_FAILED && m->spec.return_type != clazz_void) {
    woempa(7, "Return type of %m = %K\n", m, m->spec.return_type);
    result |= mustBeLoaded(&m->spec.return_type);
  }

  return result;
}

/*
** Called from initializeClazz() to perform bytecode verification.
** Returns CLASS_LOADING_SUCCEEDED or CLASS_LOADING_FAILED.
*/
w_int verifyClazz(w_clazz clazz) {
  w_thread thread = currentWonkaThread;
  w_size i;
  w_method m;
  w_int result = CLASS_LOADING_DID_NOTHING;

  if (verbose_flags & VERBOSE_FLAG_INIT) {
    w_printf("Verify %k: loading all classes referenced by %k\n", clazz, clazz);
  }

  for (i = 0; result != CLASS_LOADING_FAILED && !exceptionThrown(thread) && i < clazz->numDeclaredMethods; ++i) {
    m = &clazz->own_methods[i];
    result |= loadDescriptorTypes(m);
    if (verbose_flags & VERBOSE_FLAG_INIT) {
      w_printf("Verify %k: verifying method %m\n", clazz, m);
    }
    result |= verifyMethod(m, thread) ? CLASS_LOADING_SUCCEEDED : CLASS_LOADING_FAILED;
  }

  return result;
}

/*
 * Representation of a class union. Class unions are immutable; if we want to
 * extend one we create a new one and add more classes. All unions are stored
 * in the mv->unions wordset, and when we release the mv we release all the
 * memory used by the unions.
 */

typedef struct v_Union {
  struct v_MethodVerifier *mv;
  w_size  size;
  w_word  hashcode;
  w_clazz members[0];
} v_Union;

/* status flags for opstack/local info arrays
 *
 *   TINFO_UNDEFINED    no defined value or unstable
 *   TINFO_ADDR         return address type
 *   TINFO_PRIMITIVE    primitive class, like clazz_int
 *   TINFO_CLASS        reference class (including array classes)
 *   TINFO_UNINIT       is a class instance created by NEW that has yet to be initialized.
 *   TINFO_UNINIT_SUPER the self-reference in a constructor method.
 *   TINFO_UNION        a list of types.
 */
#define TINFO_UNDEFINED    0
#define TINFO_ADDR         1
#define TINFO_PRIMITIVE    2
#define TINFO_CLASS        4
#define TINFO_UNLOADED_CLASS 8
#define TINFO_UNINIT       16
#define TINFO_UNINIT_SUPER 48
#define TINFO_UNION        64
#define TINFO_SECOND_HALF  128

#define UNDEF_VIRGIN 0    // never assigned
#define UNDEF_SOMETIMES 1 // was virgin in one path, defined in another
#define UNDEF_CONFLICT 2  // conflicting types, e.g. class / primitive

#define IS_ADDRESS(_TINFO) ((_TINFO)->tinfo & TINFO_ADDR)
#define IS_PRIMITIVE_TYPE(_TINFO) ((_TINFO)->tinfo & TINFO_PRIMITIVE)

/*
 * Verifier super-structure which holds everything you need to know about
 * this method and its state of verification.
 */
typedef struct v_MethodVerifier {
  /* The method being verified */
  w_method method;
  /* The length of its bytecode */
  w_size code_length;
  /* Number of basic blocks */
  w_size numBlocks;
  /* Class unions - each entry in the wordset is a v_Union* */
  w_wordset unions;
  /* Count of jsr/jsr_w instructions */
  w_size jsr_count;
} v_MethodVerifier;

/*
** Macro to retrieve the i'th basic block from mv->method->exec.basicBlocks
*/
#define getBasicBlock(mv,i) ((v_BasicBlock*)elementOfWordset(&((mv)->method->exec.basicBlocks),(i)))

/*
** Macro to retrieve the i'th union from mv->unions
*/
#define getUnion(mv,i) ((v_Union*)elementOfWordset(&((mv)->unions),(i)))

#ifdef DEBUG
#define DUMP_TYPE_LEVEL 7
static void w_dump_type(const char *s, struct v_MethodVerifier *mv, v_Type t) {
  switch(t.tinfo) {
  case TINFO_UNDEFINED:
    woempa(DUMP_TYPE_LEVEL, "%s undefined type (%s)\n", s, t.data.undef_kind == UNDEF_VIRGIN ? "never assigned" : t.data.undef_kind == UNDEF_SOMETIMES ? "not defined in all paths" : "conflicting definitions");
    break;

  case TINFO_ADDR:
    woempa(DUMP_TYPE_LEVEL, "%s return address %d\n", s, t.data.retaddr);
    break;

  case TINFO_PRIMITIVE:
    woempa(DUMP_TYPE_LEVEL, "%s primitive %k\n", s, t.data.clazz);
    break;

  case TINFO_CLASS:
    woempa(DUMP_TYPE_LEVEL, "%s class %k\n", s, t.data.clazz);
    break;

  case TINFO_UNLOADED_CLASS:
    woempa(DUMP_TYPE_LEVEL, "%s unloaded class %w\n", s, t.data.dotified);
    break;

  case TINFO_UNINIT:
    woempa(DUMP_TYPE_LEVEL, "%s uninitialized type #%d, class %k\n", s, t.uninitpc, t.data.clazz);
    break;

  case TINFO_UNINIT_SUPER:
    woempa(DUMP_TYPE_LEVEL, "%s uninitialized type #%d (super), class %k\n", s, t.uninitpc, t.data.clazz);
    break;

  case TINFO_UNION:
    woempa(DUMP_TYPE_LEVEL, "%s union of types #%d\n", s, t.data.union_index);
    { v_Union *uni = getUnion(mv, t.data.union_index);
      w_size i;

      for (i = 0; i < uni->size; ++i) {
        woempa(DUMP_TYPE_LEVEL, "  member[%d]: %k\n", i, uni->members[i]);
      }
    }
    break;

  case TINFO_SECOND_HALF:
    woempa(DUMP_TYPE_LEVEL, "%s second half of double-length type\n", s);
    break;

  default:
    woempa(DUMP_TYPE_LEVEL, "%s bad tinfo %d\n", t.tinfo, s);
    break;

  }
}
#else
#define w_dump_type(s,mv,t)
#endif

/*
 * Special variables representing the VM internal types.
 */
static v_Type v_type_int;
static v_Type v_type_long;
static v_Type v_type_float;
static v_Type v_type_double;
static v_Type v_type_null; // reference type which is subclass of all reference types
static v_Type v_type_Object; // reference type which is superclass of all reference types
static v_Type v_type_String;
static v_Type v_type_Class;
static v_Type v_type_conflict;

/*
 * Ensure the index given for a local variable is within the method's locals
 */
#define CHECK_LOCAL_INDEX(_N) V_ASSERT((unsigned)(_N) < method->exec.local_i, bad_local_var)

/*
 * Ensure the index given for a constant is within the constant pool
 */
#define CHECK_POOL_INDEX(_N) V_ASSERT((unsigned)(_N) < declaring_clazz->numConstants, bad_constant_index)

/*
 * Initialise the "special" type opjects such as v_type_null, v_type_int, ...
 */
void initVerifier(void) {
  v_type_int.tinfo = TINFO_PRIMITIVE;
  v_type_int.data.clazz = clazz_int;
  v_type_long.tinfo = TINFO_PRIMITIVE;
  v_type_long.data.clazz = clazz_long;
  v_type_float.tinfo = TINFO_PRIMITIVE;
  v_type_float.data.clazz = clazz_float;
  v_type_double.tinfo = TINFO_PRIMITIVE;
  v_type_double.data.clazz = clazz_double;
  v_type_null.tinfo = TINFO_CLASS;
  v_type_null.uninitpc = 0;
  v_type_null.data.clazz = clazz_void;
  v_type_Object.tinfo = TINFO_CLASS;
  v_type_Object.uninitpc = 0;
  v_type_Object.data.clazz = clazzObject;
  v_type_String.tinfo = TINFO_CLASS;
  v_type_String.uninitpc = 0;
  v_type_String.data.clazz = clazzString;
  v_type_Class.tinfo = TINFO_CLASS;
  v_type_Class.uninitpc = 0;
  v_type_Class.data.clazz = clazzClass;
  v_type_conflict.tinfo = TINFO_UNDEFINED;
  v_type_conflict.uninitpc = 0;
  v_type_conflict.data.undef_kind = UNDEF_CONFLICT;
}

/*
** Release the memory used by a method verifier and by all its adjuncts and
** appertinances.
*/
void releaseMethodVerifier(v_MethodVerifier *mv) {
  if (mv->method->exec.basicBlocks) {
    while (sizeOfWordset(&mv->method->exec.basicBlocks)) {
      releaseMem((void*)takeLastFromWordset(&mv->method->exec.basicBlocks));
    }
    releaseWordset(&mv->method->exec.basicBlocks);
  }
  if (mv->unions) {
    while (sizeOfWordset(&mv->unions)) {
      releaseMem((void*)takeLastFromWordset(&mv->unions));
    }
    releaseWordset(&mv->unions);
  }
}

/*
 * Allocate memory for a block info and fill in with default values
 */
static v_BasicBlock* createBlock(v_MethodVerifier *mv) {
  w_size bytes_needed_for_locals = mv->method->exec.local_i * sizeof(v_Type);
  w_size bytes_needed_for_stack = mv->method->exec.stack_i * sizeof(v_Type);
  w_size bytes_needed_for_successors = mv->numBlocks * sizeof(w_ubyte);
  void *memptr;
  v_BasicBlock* binfo;

  woempa(7, "Need %d bytes for v_BasicBlock struct, %d for locals, %d for stack, %d for successors\n", sizeof(v_BasicBlock), bytes_needed_for_locals, bytes_needed_for_stack, bytes_needed_for_successors);
  if (mv->jsr_count) {
    bytes_needed_for_successors *= mv->jsr_count;
    woempa(7, "Found %d jsr/jsr_w's, increasing size of successor array to %d bytes\n", mv->jsr_count, bytes_needed_for_successors);
  }

  memptr = allocClearedMem(sizeof(v_BasicBlock) + bytes_needed_for_locals + bytes_needed_for_stack + bytes_needed_for_successors);
  binfo = memptr;

  if (!binfo) {
    return NULL;
  }

  memptr = binfo + 1;
  binfo->method = mv->method;
  binfo->localsz = mv->method->exec.local_i;

  /* memory for locals */
  if (mv->method->exec.local_i > 0) {
    binfo->locals = memptr;
    memptr = (char*)memptr + bytes_needed_for_locals;
  } else {
    binfo->locals = NULL;
  }

  /* memory for operand stack */
  binfo->stacksz = 0;
  if (mv->method->exec.stack_i > 0) {
    binfo->opstack = memptr;
    memptr = (char*)memptr + bytes_needed_for_stack;
  } else {
    binfo->opstack = NULL;
  }

  /* memory for successors array */
  binfo->max_successors = bytes_needed_for_successors;
  binfo->successors = memptr;

  return binfo;
}

/*
 * Copy from one block info to another; the internal pointers 'locals',
 * 'opstack', and 'successors' are adjusted to point to the same offset
 * within the new block. Parameter 'maxSuccessors' should be either at least
 * one greater than the number of successors in fromBlock, or zero; in the 
 * latter case the successors will not be copied and toBlock->successors is 
 * set to NULL.
 */
static void copyBlock(v_BasicBlock *toBlock, v_BasicBlock *fromBlock, v_MethodVerifier *mv) {
  void *fromptr = fromBlock;
  void *toptr = toBlock;
  void *memptr;
  w_size bytes_needed_for_locals = mv->method->exec.local_i * sizeof(v_Type);
  w_size bytes_needed_for_stack = mv->method->exec.stack_i * sizeof(v_Type);
  w_size bytes_needed_for_successors = fromBlock->max_successors * sizeof(w_ubyte);

  memcpy(toBlock, fromBlock, sizeof(v_BasicBlock) + bytes_needed_for_locals + bytes_needed_for_stack + bytes_needed_for_successors);
  memptr = fromBlock->locals;
  if (memptr) {
    memptr = (char*)toptr + ((char*)memptr - (char*)fromptr);
    toBlock->locals = memptr;
  }
  memptr = fromBlock->opstack;
  if (memptr) {
    memptr = (char*)toptr + ((char*)memptr - (char*)fromptr);
    toBlock->opstack = memptr;
  }
  memptr = fromBlock->successors;
  if (memptr) {
    memptr = (char*)toptr + ((char*)memptr - (char*)fromptr);
    toBlock->successors = memptr;
  }
}

/*
** Macro to append a basic block to the mv->method->exec.basicBlocks wordset
*/
#define appendBlock(bb,mv) bb->own_index = sizeOfWordset(&(mv)->method->exec.basicBlocks); \
    addToWordset(&(mv)->method->exec.basicBlocks, (w_word)bb);

/*
** Within a method verifier, find the index of the basic block which starts at 
** the (exact) given PC. Returns the index of the block or -1 if none found.
** TODO: use a binary search here.
*/
static w_int pc2BasicBlock(v_MethodVerifier *mv, w_size pc) {
  v_BasicBlock* bb;
  w_int i;

  woempa(1, "looking for basic block with start_pc = %d\n", pc);
  for (i = 0; i < (w_int)mv->numBlocks; ++i) {
    bb = getBasicBlock(mv, i);
    woempa(1, "blocks[%d] has start_pc %d\n", i, bb->start_pc);
    if (bb->start_pc == pc) {
      woempa(1, "OK, found it at offset %d\n", i);

      return i;

    }
    woempa(1, "Not found at offset[%d]\n", i);
  }

  wabort(ABORT_WONKA, "Unable to find basic block with start_pc = %d in verifier for %M", pc, mv->method);

  return -1;
}

/*
** For instructions which have a constant pool index as operand, check that
** the constant has the correct type and (for multi/a/new/array) the correct
** dimensionality.
** Returns TRUE if all tests pass, else FALSE.
*/
static w_boolean checkOpcodeArgs(w_method method, w_ubyte* status_array, w_thread thread) {
  w_size codelen = method->exec.code_length;
  w_code code = method->exec.code;
  w_boolean is_wide;
  w_size pc = 0;
  w_size idx = 0;
  w_size n = 0;
  w_int i;
  w_ubyte opcode;
  w_ubyte ndims;
  w_int low;
  w_int high;
  w_size inslen;
  w_clazz declaring_clazz = method->spec.declaring_clazz;
  char *error_cstring;
#ifdef DEBUG
  int lineno;
#endif
  w_string method_name;

  woempa(7, "Checking operand types in %M, codelen = %d\n", method, codelen);
  is_wide = FALSE;
  pc = 0;
  while (pc < codelen) {
    opcode = code[pc];
    inslen = instruction_length(opcode);
    if (is_wide) {
      inslen += (opcode == iinc ? 2 : 1);
    }
    woempa(7, "instruction at pc[%d] is %s%s\n", pc, is_wide ? "wide " : "", opc2name(opcode));
  
    if (instruction_uses_local_var(opcode)) {
      if ((instruction_uses_local_var_0(opcode) && method->exec.local_i == 0)
       || (instruction_uses_local_var_1(opcode) && method->exec.local_i <= 1)
       || (instruction_uses_local_var_2(opcode) && method->exec.local_i <= 2)
       || (instruction_uses_local_var_3(opcode) && method->exec.local_i <= 3)
       || (instruction_uses_local_var_4(opcode) && method->exec.local_i <= 4)
      ) {
        VERIFY_ERROR("local variable index out of range");
      }
    }

    switch(opcode) {
    case ldc:
      idx = code[pc + 1];
      CHECK_POOL_INDEX(idx);
      break;

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
      idx = (code[pc + 1] << 8) | code[pc + 2];
      CHECK_POOL_INDEX(idx);
      break;

    case invokeinterface:
      idx = (code[pc + 1] << 8) | code[pc + 2];
      CHECK_POOL_INDEX(idx);
      V_ASSERT(code[pc + 3] != 0, invokeinterface_byte4_zero);
      // TODO: check == num params expected
      V_ASSERT(code[pc + 4] == 0, invokeinterface_byte5_nonzero);
      break;

    case newarray:
      n = code[pc + 1];
      V_ASSERT(n >= 4 && n <= 11, newarray_bad_operand);
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
        n = (code[pc + 1] << 8) | code[pc + 2];
        pc++;
 
      }
      else {
        n = code[pc + 1];
      }
      CHECK_LOCAL_INDEX(n);
      break;

    case lload:
    case lstore:
    case dload:
    case dstore:
      if (is_wide) {
        is_wide = FALSE;
        n = (code[pc + 1] << 8) | code[pc + 2];
        pc++;

      }
      else {
        n = code[pc + 1];
      }
      CHECK_LOCAL_INDEX(n + 1); /* long/double occupies 2 slots */
      break;

    case iinc:
      if (is_wide) {
        is_wide = FALSE;
        pc += 2;
      }
      break;
 
    /********************************************************************
    * BRANCHING INSTRUCTIONS
    ********************************************************************/
    case ret:
      if (is_wide) {
        is_wide = FALSE;
        idx = (code[pc + 1] << 8) | code[pc + 2];
        ++pc;
      }
      else {
        idx = code[pc + 1];
      }
      CHECK_LOCAL_INDEX(idx);
      pc = pc + instruction_length(opcode);
      // 'continue' rather than 'break' in order to skip normal incrementing of pc
      continue;

    case lookupswitch:
      {
        w_int npairs;

        /* default branch...between 0 and 3 bytes of padding are added so that the
         * default branch is at an address that is divisible by 4
         */
        n = (pc + 4) & -4;
        n += 4;
        npairs = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
  
        n += 4;
        pc = n + 8 * npairs;
      }

      // 'continue' rather than 'break' in order to skip normal incrementing of pc
      continue;

    case tableswitch:
      n = (pc + 4) & -4;
      woempa(1, "tableswitch default branch offset is at pc[%d]\n", n);
      n += 4;
      low = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
      n += 4;
      high = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];

      pc = n + 4 * (high - low + 2);
      // 'continue' rather than 'break' in order to skip normal incrementing of pc
      continue;
 
    default:
      ;
    }

    /*
    ** For instructions which access a constant, check its type.
    */
    switch(opcode) {
    case ldc:
    case ldc_w:
      V_ASSERT(isIntegerConstant(declaring_clazz, idx) || isFloatConstant(declaring_clazz, idx) || isStringConstant(declaring_clazz, idx) || isClassConstant(declaring_clazz, idx), ldc_bad_constant);
      break;
      
    case ldc2_w:
      V_ASSERT(isLongConstant(declaring_clazz, idx) || isDoubleConstant(declaring_clazz, idx), ldc_bad_constant);
      break;
      
    case getfield:
    case putfield:
    case getstatic: 
    case putstatic:
      V_ASSERT(isFieldConstant(declaring_clazz, idx), not_fieldref);
      break;
      
    case invokevirtual:
    case invokestatic:
    case invokespecial:
      V_ASSERT(isMethodConstant(declaring_clazz, idx), not_methodref);
      method_name = NULL;
      getMemberConstantStrings(declaring_clazz, idx, NULL, &method_name, NULL);
      woempa(1, "Call to method %w\n", method_name);
      break;
      
    case invokeinterface:
      V_ASSERT(isIMethodConstant(declaring_clazz, idx), not_imethodref);
      method_name = NULL;
      getMemberConstantStrings(declaring_clazz, idx, NULL, &method_name, NULL);
      woempa(1, "Call to interface method %w\n", method_name);
      break;

    case instanceof:
    case checkcast:
      V_ASSERT(isClassConstant(declaring_clazz, idx), not_class_constant);
      break;
      
    case new:
      V_ASSERT(isClassConstant(declaring_clazz, idx), not_class_constant);

      n = getClassConstantDims(declaring_clazz, idx, thread);
      CHECK_FOR_EXCEPTION(thread);

      V_ASSERT(!n, new_creates_array);
      // NOTE: Sun's spec imlies that we should check that the class is not
      // abstract, but we defer this check until runtime (see Coglio 2003).
      break;
 
    case anewarray:
      V_ASSERT(isClassConstant(declaring_clazz, idx), not_class_constant);
      break;
       
    case multianewarray:
      V_ASSERT(isClassConstant(declaring_clazz, idx), not_class_constant);

      n = getClassConstantDims(declaring_clazz, idx, thread);
      ndims = code[pc + 3];
      V_ASSERT(ndims, multianewarray_dims_zero);
      V_ASSERT(n >= ndims, multianewarray_dims_too_big);
      break;
    }
      
    /*
    ** For invoke instructions, check the name of method being called.
    */
    switch(opcode) {
    case invokevirtual:
    case invokestatic:
      {
        V_ASSERT(method_name != string_angle_brackets_init, bad_invoke_constructor);
        V_ASSERT(string_char(method_name, 0) != '<', bad_angle_bracket);
      }
      deregisterString(method_name);
      break;

    case invokespecial:
      {
        V_ASSERT(string_char(method_name, 0) != '<' || method_name == string_angle_brackets_init, bad_angle_bracket);
      }
      deregisterString(method_name);
      break;

    case invokeinterface:
      {
        V_ASSERT(string_char(method_name, 0) != '<', bad_angle_bracket);
        // TODO: must we check that it is an interface method?
        // And then what about other invoke instructions (must be non-interface?)
        // (Probably better to do this at execution time)
      }
      deregisterString(method_name);
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

      if (ex->type_index) {
        w_clazz throwable_clazz = getClassConstant(declaring_clazz, ex->type_index, thread);
        CHECK_FOR_EXCEPTION(thread);
        V_ASSERT(isSuperClass(clazzThrowable, throwable_clazz), catch_not_throwable);
      }

    }
  }

  return TRUE;

verify_error:
#ifdef DEBUG
  throwException(currentWonkaThread, clazzVerifyError, "identifyBoundaries() line %d: %k.%m pc %d (%s): %s", lineno, declaring_clazz, method, pc, opc2name(method->exec.code[pc]), error_cstring);
#else
  throwException(currentWonkaThread, clazzVerifyError, "%k.%m pc %d (%s): %s", declaring_clazz, method, pc, opc2name(method->exec.code[pc]), error_cstring);
#endif

error:
  return FALSE;
}

/*
** Add the basic block with index succ_index as a successor of kind succ_kind
** of the basic block with index block.
*/
static void addSuccessor(v_MethodVerifier *mv, w_size block_index, w_size succ_index, w_ubyte succ_kind) {
  v_BasicBlock* bb = getBasicBlock(mv, block_index);

  woempa(7, "Adding block[%d] to successors of block[%d] of kind %d\n", succ_index, block_index, succ_kind);
  bb->successors[succ_index] = succ_kind;
}
 
/*
** Copy the subroutine consisting of block subr_index, its successors, etc.
** into a new set of basic blocks appended to the end of mv->method->exec.basicBlocks
*/
static w_size cloneSubroutine(v_MethodVerifier *mv, w_size subr_index) {
  w_size clone_index = mv->numBlocks;
  w_size result = mv->numBlocks;
  w_size limit = mv->numBlocks;
  w_size succ_index;
  v_BasicBlock *origBlock;
  v_BasicBlock *succBlock;
  v_BasicBlock *cloneBlock;
  w_fifo clone_fifo = allocFifo(30);
  w_ubyte *mapping = allocClearedMem(mv->numBlocks);
#ifdef DEBUG
  w_ubyte last_opcode;
  w_size orig_index;
#endif

  woempa(7, "Subroutine begins with block[%d], enqueueing that block\n", subr_index);
  origBlock = getBasicBlock(mv, subr_index);
  putFifo(origBlock, clone_fifo);
  while ((origBlock = getFifo(clone_fifo))) {
#ifdef DEBUG
    orig_index = origBlock->own_index;
    woempa(7, "Dequeued block[%d]\n", orig_index);
#endif
    cloneBlock = createBlock(mv);
    if (!cloneBlock) {
      wabort(ABORT_WONKA, "No space to clone block\n");
    }
    copyBlock(cloneBlock, origBlock, mv);
    appendBlock(cloneBlock, mv);
    ++clone_index;
    woempa(7, "Copied block[%d] to become block[%d], incremented numBlocks to %d\n", orig_index, clone_index - 1, clone_index);
#ifdef DEBUG
    last_opcode = mv->method->exec.code[origBlock->last_pc];
    woempa(7, "Last opcode is %s\n", opc2name(last_opcode));
    woempa(7, "Checking successors of block[%d]\n", orig_index);
#endif
    for (succ_index = 0; succ_index < result; ++succ_index) {
      w_ubyte succ_kind = origBlock->successors[succ_index];

      switch (succ_kind) {
      case SUCC_NORMAL:
      case SUCC_EXCEPTION:
        if (mapping[succ_index]) {
          woempa(7, "Successor %d of kind %d, already mapped to %d\n", succ_index, succ_kind, mapping[succ_index]);
        }
        else {
          mapping[succ_index] = clone_index + occupancyOfFifo(clone_fifo);
          woempa(7, "Successor %d of kind %d, new index is %d\n", succ_index, succ_kind, mapping[succ_index]);
          succBlock = getBasicBlock(mv, succ_index);
          putFifo(succBlock, clone_fifo);
        }
        cloneBlock->successors[succ_index] = 0;
        cloneBlock->successors[mapping[succ_index]] = succ_kind;
        break;

      case SUCC_RET:
        woempa(7, "Cancelling old RET successor %d\n", succ_index);
        cloneBlock->successors[succ_index] = 0;
        break;

      case SUCC_JSR:
        woempa(7, "Not following JSR successor, will be unrolled later\n");

      default: // SUCC_NONE
        ;
      }
    }

    // This is just to prevent us from looping indefinitely.
    // TODO - don't abort, force a VerifyError instead
    if (limit-- == 0) {
      wabort(ABORT_WONKA, "Infinite loop?");
    }
  }

  woempa(7, "No more successors, finished. Now have %d basic blocks\n", clone_index);
  mv->numBlocks = clone_index;
  releaseFifo(clone_fifo);
  releaseMem(mapping);

  return result;
}

/*
** Mark block subr_index and its successors as having the given return address
*/
static void propagateReturnAddress(v_MethodVerifier *mv, w_size subr_index, w_size return_address) {
  w_size limit = mv->numBlocks;
  w_size succ_index;
  v_BasicBlock *origBlock;
  v_BasicBlock *succBlock;
  w_fifo clone_fifo = allocFifo(30);
#ifdef DEBUG
  w_size orig_index;
#endif

  woempa(7, "Subroutine begins with block[%d], enqueueing that block\n", subr_index);
  origBlock = getBasicBlock(mv, subr_index);
  putFifo(origBlock, clone_fifo);
  while ((origBlock = getFifo(clone_fifo))) {
#ifdef DEBUG
    orig_index = origBlock->own_index;
    woempa(7, "Dequeued block[%d]\n", orig_index);
    woempa(7, "Set return address of block[%d] to %d\n", orig_index, return_address);
#endif
    origBlock->return_address = return_address;
    woempa(7, "Checking successors of block[%d]\n", orig_index);

    for (succ_index = 0; succ_index < mv->numBlocks; ++succ_index) {
      w_ubyte succ_kind = origBlock->successors[succ_index];

      if (succ_kind == SUCC_NORMAL) {
        woempa(7, "NORMAL successor %d\n", succ_index);
        succBlock = getBasicBlock(mv, succ_index);
        if (succBlock->return_address != return_address) {
          woempa(7, "enqueueing block[%d]\n", succ_index);
          putFifo(succBlock, clone_fifo);
        }
      }
    }

    // This is just to prevent us from looping indefinitely.
    // TODO - don't abort, force a VerifyError instead
    if (limit-- == 0) {
      wabort(ABORT_WONKA, "Infinite loop?");
    }
  }

  woempa(7, "No more successors, finished.\n");
  releaseFifo(clone_fifo);
}

/*
** Given a method, identifyBasicBlocks() builds a list of v_BasicBlock elements.
** The array is terminated by a NULL pointer, and the number of basic blocks
** found is also recorded in mv->numBlocks.
*/
static void identifyBasicBlocks(v_MethodVerifier *mv) {
  w_method method = mv->method;
  w_ubyte *status_array = method->exec.status_array;
  w_code code = method->exec.code;
  w_size codelen = method->exec.code_length;
  w_boolean in_a_block;
  w_size pc;
  w_size previous_pc;
  w_size block_count;
  w_size jsr_count;
  v_BasicBlock *bb = NULL;
  char *error_cstring;
#ifdef DEBUG
  int lineno;
#endif
  w_ubyte opcode;
  w_int branchoffset;
  w_size block_index;
  w_size succ_index;
  w_int exception_index;

  if (!status_array) {
    status_array = identifyBoundaries(method, currentWonkaThread);
    method->exec.status_array = status_array;
  }
  if (!status_array || !checkOpcodeArgs(method, status_array, currentWonkaThread)) {
    goto error;
  }

  block_count = 0;
  jsr_count = 0;
  pc = 0;
  previous_pc = -1;
  in_a_block = FALSE;

  /*
  ** First pass; check consistency and count the basic blocks. Count jrs/jsr_w's.
  */
  woempa(1, "%M pass 1\n", method);
  for (pc = 0; pc < codelen; ++pc) {
#ifdef DEBUG
    if (isSet(status_array[pc], IS_INSTRUCTION)) {
      woempa(1, "pc %d is an instruction, in_a_block = %s\n", pc, in_a_block ? "true" : "false");
    }
    else {
      woempa(1, "pc %d is not an instruction\n", pc);
    }
#endif

    opcode = code[pc];
    if (opcode == jsr || opcode == jsr_w) {
      ++jsr_count;
    }

    if (isNotSet(status_array[pc], IS_INSTRUCTION)) {
      V_ASSERT(isNotSet(status_array[pc], START_BLOCK | END_BLOCK), branch_dest_not_instr);

      continue;

    }

    if (in_a_block) {
      if (isSet(status_array[pc], START_BLOCK)) {
        woempa(1, "pc %d is start of block: already in block, but start a new one anyhow\n", pc);
        woempa(1, "=> pc %d was end of block\n", previous_pc);
        if (previous_pc >= 0) {
          setFlag(status_array[previous_pc], END_BLOCK);
        }
        ++block_count;
      }
    }
    else {
      if (isSet(status_array[pc], START_BLOCK)) {
        woempa(1, "pc [%d]: Start of block\n", pc);
        ++block_count;
        in_a_block = TRUE;
      }
    }

    if (in_a_block) {
      if (isSet(status_array[pc], END_BLOCK)) {
        woempa(1, "pc %d is end of block\n", pc);
        in_a_block = FALSE;
      }
    }
    else {
      if (isSet(status_array[pc], END_BLOCK)) {
        woempa(1, "pc %d is end of block, but we weren't in one anyway?\n", pc);
      }
    }

    previous_pc = pc;
  }

  V_ASSERT(!in_a_block, fall_off_end);

  mv->numBlocks = block_count;
  mv->jsr_count = jsr_count;
  woempa(7, "Found %d basic blocks and %d jsr/jsr_w's\n", mv->numBlocks, mv->jsr_count);

  /*
  ** Second pass; create v_BasicBlock structures, fill in the start_pc and last_pc.
  */
  woempa(7, "%M pass 2\n", method);

  in_a_block = FALSE;
  block_count = 0;

  for (pc = 0; pc < codelen; ++pc) {
    if (isNotSet(status_array[pc], IS_INSTRUCTION)) {
      continue;
    }

    if (isSet(status_array[pc], START_BLOCK)) {
      bb = createBlock(mv);
      if (!bb) {
        wabort(ABORT_WONKA, "No space to create block\n");
      }
      bb->start_pc = pc;
      in_a_block = TRUE;
      woempa(7, "Basic block [%d] at %p, starts at pc %d\n", block_count, bb, pc);
    }
    if (in_a_block && isSet(status_array[pc], END_BLOCK)) {
      woempa(7, "Basic block [%d] ends at pc %d (%s)\n", block_count, pc, opc2name(code[pc]));
      bb->last_pc = pc;
      appendBlock(bb, mv);
      block_count++;
      in_a_block = FALSE;
    }
  }

#ifdef RUNTIME_CHECKS
  if (block_count != mv->numBlocks) {
    wabort(ABORT_WONKA, "Darn, first I had %d basic blocks and now I have %d", mv->numBlocks, block_count);
  }
#endif


  /*
  ** Third pass: add the list of successor blocks to each block.
  */
  woempa(7, "%M pass 3\n", method);
  for (block_index = 0; block_index < block_count; ++block_index) {
    bb = getBasicBlock(mv, block_index);
    pc = bb->last_pc;
    opcode = code[pc];
    woempa(7, "Block[%d] ends with opcode %s\n", block_index, opc2name(opcode));
    switch (opcode) {
    case j_goto:
    case jsr:
      branchoffset = (code[pc + 1] << 8) | code[pc + 2];
      if (branchoffset & 0x8000) {
        branchoffset |= 0xffff0000;
      }

      succ_index = pc2BasicBlock(mv, pc + branchoffset);
      if (opcode == jsr) {
        goto jsr_common;
      }
      goto branch_common;

    case goto_w:
    case jsr_w:
      branchoffset = (((((code[pc + 1] << 8) | code[pc + 2]) << 8) | code[pc + 3]) << 8) | code[pc + 4];
      succ_index = pc2BasicBlock(mv, pc + branchoffset);
      if (opcode == jsr_w) {
        goto jsr_common;
      }
      goto branch_common;

    jsr_common:
      addSuccessor(mv, block_index, succ_index, SUCC_JSR);
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
      branchoffset = (code[pc + 1] << 8) | code[pc + 2];
      if (branchoffset & 0x8000) {
        branchoffset |= 0xffff0000;
      }
      succ_index = pc2BasicBlock(mv, pc + instruction_length(opcode));
      addSuccessor(mv, block_index, succ_index, SUCC_NORMAL);
      succ_index = pc2BasicBlock(mv, pc + branchoffset);

    branch_common:
      addSuccessor(mv, block_index, succ_index, SUCC_NORMAL);
      break;

    case lookupswitch:
      { w_int n = (pc + 4) & -4;
        w_int npairs;
        w_int j;

        branchoffset = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
        succ_index = pc2BasicBlock(mv, pc + branchoffset);
        addSuccessor(mv, block_index, succ_index, SUCC_NORMAL);
        n += 4;
        npairs = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
        n += 4;
        for (j = 0; j < npairs; ++j) {
          n += 4;
          branchoffset = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
          succ_index = pc2BasicBlock(mv, pc + branchoffset);
          addSuccessor(mv, block_index, succ_index, SUCC_NORMAL);
          n += 4;
        }
      }
      break;

    case tableswitch:
      { w_int n = (pc + 4) & -4;
        w_int low;
        w_int high;
        w_int j;

        branchoffset = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
        succ_index = pc2BasicBlock(mv, pc + branchoffset);
        addSuccessor(mv, block_index, succ_index, SUCC_NORMAL);
        n += 4;
        low = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
        n += 4;
        high = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
        n += 4;
        for (j = low; j <= high; ++j) {
          branchoffset = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
          succ_index = pc2BasicBlock(mv, pc + branchoffset);
          addSuccessor(mv, block_index, succ_index, SUCC_NORMAL);
          n += 4;
        }
      }
      break;

    case ret:
    case athrow:
      woempa(7, "last instruction of block[%d] is %s, cannot determine successors\n", block_index, opc2name(opcode));
      break;


    case ireturn:
    case lreturn:
    case freturn:
    case dreturn:
    case areturn:
    case vreturn:
      // No successors in this method
      woempa(7, "last instruction of block[%d] is %s, no successors\n", block_index, opc2name(opcode));
      break;

    default: // not a transfer of control, sole successor is next block
      if (block_index < block_count - 1) {
        woempa(7, "last instruction of block[%d] is %s, sole successor is block[%d]\n", block_index, opc2name(opcode), block_index + 1);
        bb->successors[block_index + 1] = SUCC_NORMAL;
      }
      else {
        woempa(7, "last block[%d] and last instruction is %s, no successor\n", block_index, opc2name(opcode));
      }
    }

    if (method->exec.numExceptions) {
      w_exception ex = method->exec.exceptions;
      v_BasicBlock *exbb;

      woempa(1, "Parsing exception table\n");
      for (exception_index = 0; exception_index < method->exec.numExceptions; ++exception_index, ++ex) {
        woempa(7, "Exception[%d]; start_pc = %d, end_pc = %d, handler_pc = %d\n", exception_index, ex->start_pc, ex->end_pc, ex->handler_pc);
        if (ex->start_pc <= bb->start_pc && ex->end_pc > bb->last_pc) {
          succ_index = pc2BasicBlock(mv, ex->handler_pc);
          addSuccessor(mv, block_index, succ_index, SUCC_EXCEPTION);
          exbb = getBasicBlock(mv, succ_index);
          setFlag(exbb->flags, EXCEPTION);
          if (ex->type_index) {
            w_thread thread = currentWonkaThread;
            V_ASSERT((unsigned)(ex->type_index) < method->spec.declaring_clazz->numConstants, bad_constant_index)
            exbb->exception_type = getClassConstant(mv->method->spec.declaring_clazz, ex->type_index, thread);
            CHECK_FOR_EXCEPTION(thread);
            woempa(7, "block[%d] exception_type = %k\n", succ_index, exbb->exception_type);
          }
          else {
            exbb->exception_type = clazzThrowable;
            woempa(7, "block[%d] exception_type = %k\n", succ_index, clazzThrowable);
          }

        }
      }
    }
  }

  /*
  ** Fourth pass: clone each subroutine so that we have one copy for every jsr
  ** which calls it, and mark each basic block with its return address.
  */
  woempa(7, "%M pass 4\n", method);
  for (block_index = 0; block_index < block_count; ++block_index) {
    bb = getBasicBlock(mv, block_index);
    pc = bb->last_pc;
    opcode = code[pc];
    woempa(7, "Block[%d] ends with opcode %s\n", block_index, opc2name(opcode));
    switch (opcode) {
    case jsr:
    case jsr_w:
      for (succ_index = 0; succ_index < block_count; ++succ_index) {
        if (bb->successors[succ_index] == SUCC_JSR) {
          if (getBasicBlock(mv, succ_index)->return_address) {
            woempa(7, "Successor block[%d] already has return address %d, need to clone it\n", succ_index, getBasicBlock(mv, succ_index)->return_address);
            bb->successors[succ_index] = SUCC_NONE;
            succ_index = cloneSubroutine(mv, succ_index);
            woempa(7, "Continuing with successor block[%d]\n", succ_index);
            bb->successors[succ_index] = SUCC_JSR;
            block_count = mv->numBlocks;
          }
          propagateReturnAddress(mv, succ_index, pc + (opcode == jsr ? 3 : 5));

          break;
        }
      }
      break;

    case ret:
      succ_index = pc2BasicBlock(mv, bb->return_address);
      woempa(7, "last instruction of block[%d] is %s, return address is %d in block[%d]\n", block_index, opc2name(opcode), bb->return_address, succ_index);
      addSuccessor(mv, block_index, succ_index, SUCC_RET);
      break;

    default: 
      ;
    }
  }

  return;

verify_error:
#ifdef DEBUG
  throwException(currentWonkaThread, clazzVerifyError, "identifyBasicBlocks() line %d: %k.%m pc %d (%s): %s", lineno, method->spec.declaring_clazz, method, pc, opc2name(method->exec.code[pc]), error_cstring);
#else
  throwException(currentWonkaThread, clazzVerifyError, "%k.%m pc %d (%s): %s", method->spec.declaring_clazz, method, pc, opc2name(method->exec.code[pc]), error_cstring);
#endif
error:;
}

/*
 * Create a new uninitialised type. It is assigned the sequence number
 * mv->uninit_count, which is then incremented.
 */
static w_size pushUninit(v_MethodVerifier *mv, w_size pc)
{
  return pc;
}

/*
 * popUninit()
 *     Pops an uninitialized type off of the operand stack
 */
static void popUninit(v_MethodVerifier *mv, w_size uninit, v_BasicBlock* binfo)
{
  w_size i;
  
  woempa(1, "Popping element[%d]\n", uninit);
  for (i = 0; i < binfo->localsz ; i++) {
    if (isSet(binfo->locals[i].tinfo, TINFO_UNINIT) && binfo->locals[i].uninitpc == uninit) {
      woempa(1, "Updating locals[%d]\n", i);
      binfo->locals[i].tinfo = TINFO_CLASS;
      binfo->locals[i].uninitpc = 0;
    }
  }
  
  for (i = 0; i < binfo->stacksz; i++) {
    if (isSet(binfo->opstack[i].tinfo, TINFO_UNINIT) && binfo->opstack[i].uninitpc == uninit) {
      woempa(1, "Updating stack[%d]\n", i);
      binfo->opstack[i].tinfo = TINFO_CLASS;
      binfo->opstack[i].uninitpc = 0;
    }
  }
}

#define CLAZZ_IS_INTEGER(c) ((c) == clazz_int || (c) == clazz_char || (c) == clazz_short || (c) == clazz_byte || (c) == clazz_boolean)
#define CLAZZ2TYPE(c,t) if (clazzIsPrimitive(c)) { t.tinfo = TINFO_PRIMITIVE; t.uninitpc = 0; t.data.clazz = CLAZZ_IS_INTEGER(c) ? clazz_int : (c); } else { t.tinfo = TINFO_CLASS; t.uninitpc = 0; t.data.clazz = (c); }

/*
 * Initialize the local variable array.
 */
static w_boolean loadInitialArgs(v_MethodVerifier *mv) {
  w_method method = mv->method;
  v_BasicBlock *block = getBasicBlock(mv, 0);
  w_word paramCount = 0;
  w_int i = 0;
  char *error_cstring;
#ifdef DEBUG
  int lineno;
#endif
  w_clazz arg_clazz;
  v_Type t;
  v_Type* locals = block->locals;

  woempa(1, "initialising basic block[0] for %M\n", mv->method);
  if (isNotSet(method->flags, ACC_STATIC)) {
    V_ASSERT(method->exec.local_i > 0, too_few_locals);
    woempa(1, "'this' has type %K\n", method->spec.declaring_clazz);
    locals[0].tinfo = TINFO_CLASS;
    locals[0].uninitpc = 0;
    locals[0].data.clazz = method->spec.declaring_clazz;
    if (method->spec.name == string_angle_brackets_init) {
      locals[0].tinfo = TINFO_UNINIT_SUPER;
      locals[0].uninitpc = pushUninit(mv, 0);
      locals[0].data.clazz = method->spec.declaring_clazz;
    }
    paramCount = 1;
  }

  arg_clazz = method->spec.arg_types? method->spec.arg_types[0] : NULL;
  while (arg_clazz) {
    woempa(1, "arg[%d] has type %K\n", i, arg_clazz);
    CLAZZ2TYPE(arg_clazz, t);
    locals[paramCount] = t;
    woempa(1, "tinfo = %d, data.clazz = %k\n", t.tinfo, t.data.clazz);
    woempa(1, "tinfo = %d, data.clazz = %k\n", locals[paramCount].tinfo, locals[paramCount].data.clazz);
    ++paramCount;
    if (arg_clazz == clazz_long || arg_clazz == clazz_double) {
      locals[paramCount].tinfo = TINFO_SECOND_HALF;
      ++paramCount;
    }
    arg_clazz = method->spec.arg_types[++i];
  }
  
  return TRUE;

verify_error:
#ifdef DEBUG
  throwException(currentWonkaThread, clazzVerifyError, "loadInitialArgs() line %d: %k.%m: %s", lineno, method->spec.declaring_clazz, method, error_cstring);
#else
  throwException(currentWonkaThread, clazzVerifyError, "%k.%m: %s", method->spec.declaring_clazz, method, error_cstring);
#endif

  return FALSE;
}
#undef LOCAL_OVERFLOW_ERROR

/*
** Print out the contents of a basic block.
*/
void listBasicBlock(v_BasicBlock *block) {
  w_size i, n;

  woempa(7, "  start_pc = %d\n", block->start_pc);
  woempa(7, "  last_pc  = %d\n", block->last_pc);
  woempa(7, "  flags  = %02x\n", block->flags);
  woempa(7, "  stacksz = %02x\n", block->stacksz);
  n = sizeOfWordset(&block->method->exec.basicBlocks);
  for (i = 0; i < n; ++i) {
    switch (block->successors[i]) {
    case SUCC_NONE:
      continue;

    case SUCC_NORMAL:
      woempa(7, "  normal successor: %d\n", i);

    case SUCC_EXCEPTION:
      woempa(7, "  exception successor: %d\n", i);

    case SUCC_JSR:
      woempa(7, "  jsr successor: %d\n", i);

    case SUCC_RET:
      woempa(7, "  ret successor: %d\n", i);

    default:
      woempa(7, "  subroutine successor: %d (color %d)\n", i, block->successors[i]);
    }
  }
}

/*
** Print out the contents of a basic block list.
*/
void listBasicBlocks(v_BasicBlock** blocks) {
  v_BasicBlock** bbb = blocks;
  v_BasicBlock* bb = *bbb;

  while (bb) {
    woempa(1, "Block[%d]:\n", bbb - blocks);
    listBasicBlock(bb);
    ++bbb;
    bb = *bbb;
  }
}

#define LOAD_TYPE(t,l) {\
      w_clazz tclazz = namedClassMustBeLoaded(mv->method->spec.declaring_clazz->loader, (t)->data.dotified);\
      if (!tclazz) goto l;\
      (t)->tinfo = TINFO_CLASS;\
      (t)->data.clazz = tclazz;\
      /* TODO: probably deregisterString((t)->data.dotified); */\
    }

/*
** Test whether the type rhs can be assigned to a field or parameter of type
** lhs, given as a w_clazz. Returns TRUE if assignment is possible, FALSE otherwise.
*/
static w_boolean v_assignable2clazz(w_clazz lhs, v_MethodVerifier *mv, v_Type *rhs) {
  //woempa(7, "lhs: %k\n", lhs);
  //w_dump_type("rhs:", mv, *rhs);
  switch(rhs->tinfo) {
  case TINFO_PRIMITIVE:
    return lhs == rhs->data.clazz || ((lhs == clazz_boolean || lhs == clazz_byte || lhs == clazz_short || lhs == clazz_char) && rhs->data.clazz == clazz_int);

  case TINFO_UNLOADED_CLASS:
    LOAD_TYPE(rhs, fail)
    // fall through
 
  case TINFO_CLASS:
    if (rhs->data.clazz == clazz_void) {

      return TRUE;

    }

    return isAssignmentCompatible(rhs->data.clazz, lhs);

  case TINFO_UNION:
    { v_Union *uni = getUnion(mv, rhs->data.union_index);
      w_size i;

      for (i = 0; i < uni->size; ++i) {
        w_clazz c = uni->members[i];

        woempa(1, "members[%d] is %k, is this assignable to %k?\n", i, c, lhs);
        if (!isAssignmentCompatible(c, lhs)) {
          woempa(1, "--> NO\n");

          return FALSE;
        }
        woempa(1, "--> yes\n");
      }

      return TRUE;
    }
  }

  fail:
    return FALSE;
}

/*
** Test whether the type rhs can be assigned to a field or parameter of type
** lhs, given as a v_Type. Returns TRUE if assignme,t is possible, FALSE otherwise.
*/
static w_boolean v_assignable2type(v_Type *lhs, v_MethodVerifier *mv, v_Type *rhs) {
  v_Union *lhs_uni;
  v_Union *rhs_uni;
  w_size i;
  w_size j;
  w_size n;
  w_clazz lhs_clazz;
  w_clazz rhs_clazz;

  w_dump_type("lhs:", mv, *lhs);
  w_dump_type("rhs:", mv, *rhs);

  if (lhs->tinfo == TINFO_CLASS) {

    return v_assignable2clazz(lhs->data.clazz, mv, rhs);

  }

  lhs_uni = getUnion(mv, lhs->data.union_index);
  n = lhs_uni->size;
  switch(rhs->tinfo) {
  case TINFO_UNLOADED_CLASS:
    LOAD_TYPE(rhs, fail)
    // fall through
 
  case TINFO_CLASS:
    if (rhs->data.clazz == clazz_void) {
      woempa(1, "rhs is clazz_void, that's assignable to anything\n");

      return TRUE;

    }

    for (i = 0; i < n; ++i) {
      lhs_clazz = lhs_uni->members[i];
      woempa(1, "lhs members[%d] is %k, is %k assignable to it?\n", i, lhs_clazz, rhs->data.clazz);
      if (!isAssignmentCompatible(rhs->data.clazz, lhs_clazz)) {
        woempa(1, "--> NO\n");

        return FALSE;

      }
      woempa(1, "--> yes\n");
    }

    return TRUE;

  case TINFO_UNION:
    rhs_uni = getUnion(mv, rhs->data.union_index);
    for (i = 0; i < n; ++i) {
      lhs_clazz = lhs_uni->members[i];
      woempa(1, "lhs members[%d] is %k\n", i, lhs_clazz);
      for (j = 0; j < rhs_uni->size; ++j) {
        rhs_clazz = rhs_uni->members[j];
        woempa(1, "rhs members[%d] is %k, is it assignable to lhs?\n", j, rhs_clazz);
        if (!isAssignmentCompatible(rhs_clazz, lhs_clazz)) {
          woempa(1, "--> NO\n");

          return FALSE;
        }
        woempa(1, "--> yes\n");
      }
    }

    return TRUE;
  }

  fail:
    return FALSE;
}

/*
** Calculate the hash code of a union. We first sort its members into ascending
** order of w_clazz address.
*/
static void calculateUnionHashcode(v_Union *uni) {
  w_size i;
  w_size j;
  w_size n;
  w_clazz c;
  w_word hashcode;

  woempa(7, "Calculating hashcode for union %p, size is %d\n", uni, uni->size);
  n = uni->size;
  while (n--) {
    for (i = 0; i < n; ++i) {
      woempa(7, "members[%d] = %p\n", i, uni->members[i]);
      for (j = i + 1; j < uni->size; ++j) {
        woempa(7, "members[%d] = %p\n", j, uni->members[j]);
        if (uni->members[j] < uni->members[i]) {
          woempa(7, "swapping [%d] with [%d]\n", i, j);
          c = uni->members[i];
          uni->members[i] = uni->members[j];
          uni->members[j] = c;
        }
      }
    }
  }

  hashcode = 0;
  for (i = 0; i < uni->size; ++i) {
    woempa(7, "merging %p into the hashcode\n", uni->members[i], uni->members[i]);
    hashcode = hashcode * 41 + (w_word)uni->members[i];
  }

  uni->hashcode = hashcode;
}

/*
** Get the hash code of a union, calculating it first if necessary.
*/
static w_word getUnionHashcode(v_Union *uni) {
  if (!uni->hashcode) {
    calculateUnionHashcode(uni);
  }

  return uni->hashcode;
}

/*
** Compare two unions for equality. Note that this can have a side effect;
** if the unions are the same size and one or other has no hash code set,
** that union (or both unions) will get sorted into ascensing order of
** w_clazz address and its hash code will be calculated.
*/
static w_boolean unionsEqual(v_Union *uni1, v_Union *uni2) {
  w_word hashcode1;
  w_word hashcode2;
  w_size i;

  woempa(7, "Comparing unions %p and %p\n", uni1, uni2);
  if (uni1 == uni2) {
    woempa(9, "Funny, we were called with two identical pointers. That works, but we didn't expect it.\n");
  }

  if (uni1->size != uni2->size) {
    woempa(7, "Different sizes, cannot be equal\n");

    return FALSE;
  }

  hashcode1 = getUnionHashcode(uni1);
  hashcode2 = getUnionHashcode(uni2);

  if (hashcode1 != hashcode2) {
    woempa(7, "Different hashcodes, cannot be equal\n");

    return FALSE;

  }

  for (i = 0; i < uni1->size; ++i) {
    if (uni1->members[i] != uni2->members[i]) {

      return FALSE;

    }
  }

  return TRUE;
}


/*
** Look for an existing copy of a union in mv->unions; if it is found, release
** the memory used for the union and return the index of the existing union
** of which it is a duplicate. If not found, add it to mv->unions and return
** its index there.
*/
static w_size seekUnion(v_MethodVerifier *mv, v_Union *uni) {
  w_size i;
  w_size n = sizeOfWordset(&mv->unions);

  woempa(7, "Looking for a match for union %p, MV for %m has %d unions\n", uni, mv->method, n);
  for (i = 0; i < n; ++i) {
    if (unionsEqual(getUnion(mv, i), uni)) {
      releaseMem(uni);
      woempa(7, "Found at position %d\n", i);

      return i;
    }
  }

  addToWordset(&mv->unions, (w_word)uni);
  woempa(7, "Not found, adding at position %d\n", n);

  return n;
}

/*
** Merge two existing unions to obtain a new one (or possibly one of the old
** ones back, if one is a superset of the other). Returns the index of the
** new or existing union in mv->unions.
*/
static w_size mergeUnions(v_MethodVerifier *mv, w_size union_index1, w_size union_index2) {
  v_Union *uni1;
  v_Union *uni2;
  v_Union *merged;
  w_size size1;
  w_size size2;
  w_size size3;
  w_clazz *members1;
  w_clazz *members2;
  w_clazz *members3;
  w_size i;
  w_size j;
  w_clazz clazz;
  w_boolean found;

  if (union_index1 == union_index2) {

    return union_index1;

  }

  uni1 = getUnion(mv, union_index1);
  uni2 = getUnion(mv, union_index2);
  size1 = uni1->size;
  size2 = uni2->size;
  members1 = uni1->members;
  members2 = uni2->members;
  merged = allocMem(sizeof(v_Union) + (size1 + size2) * sizeof(w_clazz));
  merged->mv = mv;
  members3 = merged->members;
  memcpy(members3, members1, size1 * sizeof(w_clazz));
  size3 = size1;
  for (i = 0; i < size2; ++i) {
    clazz = members2[i];
    found = FALSE;
    for (j = 0; j < size3; ++j) {
      if (isAssignmentCompatible(clazz, members3[j])) {
        found = TRUE;
        break;
      }
      else if (isAssignmentCompatible(members3[j], clazz)) {
        members3[j] = clazz;
        found = TRUE;
        break;
      }
    } 
    if (!found) {
      members3[size3++] = clazz;
    }
  }
  merged->size = size3;
  merged->hashcode = 0;

  return seekUnion(mv, merged);
}

/*
** Create a union, starting with two classes. The caller should ensure that
** neither class is a superclass of the other, otherwise redundancy will result.
** Returns the index of the new or existing union in mv->unions.
*/
static w_size createUnion2(v_MethodVerifier *mv, w_clazz clazz1, w_clazz clazz2) {
  v_Union *merged;

  woempa(7, "Creating a union of %k and %k\n", clazz1, clazz2);
  merged = allocMem(sizeof(v_Union) + 2 * sizeof(w_clazz));
  merged->mv = mv;
  merged->members[0] = clazz1;
  merged->members[1] = clazz2;
  merged->size = 2;
  merged->hashcode = 0;

  return seekUnion(mv, merged);
}

/*
** Merge an existing union with a class to obtain a new or existing union.
** Returns the index of the new or existing union in mv->unions.
*/
static w_size mergeClassIntoUnion(v_MethodVerifier *mv, w_clazz clazz, w_size union_index) {
  v_Union *uni;
  v_Union *merged;
  w_size size;
  w_clazz *members;
  w_size j;
  w_boolean found;

  if (clazz == clazz_void) {

    return union_index;

  }

  uni = getUnion(mv, union_index);
  size = uni->size;
  members = uni->members;
  merged = allocMem(sizeof(v_Union) + (size + 1) * sizeof(w_clazz));
  memcpy(merged->members, members, size * sizeof(w_clazz));
  found = FALSE;
  for (j = 0; j < size; ++j) {
    if (isSuperClass(merged->members[j], clazz)) {
      found = TRUE;
      break;
    }
    else if (isStrictSuperClass(clazz, merged->members[j])) {
      merged->members[j] = clazz;
      found = TRUE;
      break;
    }
  } 

  if (!found) {
    merged->members[size++] = clazz;
  }
  merged->size = size;
  merged->hashcode = 0;

  return seekUnion(mv, merged);
}

/*
** Given an array type atype, get the type of its elements. If atype is a
** TINFO_CLASS, returns a TINFO_CLASS holding the element type; if atype is
** a TINFO_UNION, returns a TINFO_UNION holding the element types of the
** member classes. If atype is or includes a non-array type, returns a
** TINFO_UNDEFINED.
*/
v_Type getElementType(v_MethodVerifier *mv, v_Type type) {
  v_Type result = v_type_conflict;
  w_clazz clazz;
  v_Union *uni;
  w_size i;
  w_size n;
  v_Union *merged;

  w_dump_type("getElementType() array_type:", mv, type);
  switch(type.tinfo) {
  case TINFO_UNLOADED_CLASS:
    LOAD_TYPE(&type, fail)
    // fall through
 
  case TINFO_CLASS:
    clazz = type.data.clazz;
    if (clazz->previousDimension) {
      woempa(7, "Previous dimension is %k, returning that\n", clazz->previousDimension);
      result.tinfo = TINFO_CLASS;
      result.uninitpc = 0;
      result.data.clazz = clazz->previousDimension;
    }
    break;

  case TINFO_UNION:
    uni = getUnion(mv, type.data.union_index);
    n = uni->size;
    merged = allocMem(sizeof(v_Union) + n * sizeof(w_clazz));
    merged->mv = mv;
    for (i = 0; i < n; ++i) {
      clazz = uni->members[i];
      if (clazz->previousDimension) {
        woempa(7, "Union member %d revious dimension is %k, returning that\n", i, clazz->previousDimension);
        merged->members[i] = clazz->previousDimension;
      }
      else {
        releaseMem(merged);
        woempa(7, "Union member %d is not array, returning UNDEFINED\n");

        return result;
      }
    }

    result.tinfo = TINFO_UNION;
    result.uninitpc = 0;
    result.data.union_index = seekUnion(mv, merged);
    break;

  default:
     ;
  }

  w_dump_type("getElementType() result:", mv, result);

  return result;

fail:
  return v_type_conflict;
}

#define CHECK_STACK_CAPACITY(n) V_ASSERT(block->stacksz + (n) <= method->exec.stack_i, stack_overflow)
#define CHECK_STACK_SIZE(n) V_ASSERT(block->stacksz >= (n), stack_underflow)
#define GET_INCREMENT (isSet(status_array[pc], WIDE_MODDED) ? (code[pc + 3] << 8) | code[pc + 4] : code[pc + 2])
#define GET_INDEX (isSet(status_array[pc], WIDE_MODDED) ? GET_INDEX2 : GET_INDEX1)
#define GET_INDEX1 (code[pc + 1])
#define GET_INDEX2 ((code[pc + 1] << 8) | code[pc + 2])
#define IS_REFERENCE(t) (t.tinfo == TINFO_UNLOADED_CLASS || t.tinfo == TINFO_CLASS || t.tinfo == TINFO_UNION)
#define LOCAL_IS_DOUBLE(idx) (block->locals[idx].tinfo == TINFO_PRIMITIVE && block->locals[idx].data.clazz == clazz_double)
#define LOCAL_IS_FLOAT(idx) (block->locals[idx].tinfo == TINFO_PRIMITIVE && block->locals[idx].data.clazz == clazz_float)
#define LOCAL_IS_INTEGER(idx) (block->locals[idx].tinfo == TINFO_PRIMITIVE && block->locals[idx].data.clazz == clazz_int)
#define LOCAL_IS_LONG(idx) (block->locals[idx].tinfo == TINFO_PRIMITIVE && block->locals[idx].data.clazz == clazz_long)
#define LOCAL_IS_REFERENCE(idx) ((block->locals[idx].tinfo == TINFO_UNLOADED_CLASS || block->locals[idx].tinfo == TINFO_CLASS ) || (block->locals[idx].tinfo == TINFO_UNINIT ) || (block->locals[idx].tinfo == TINFO_UNINIT_SUPER ) || (block->locals[idx].tinfo == TINFO_UNION))
#define LOCAL_IS_RETURN_ADDRESS(idx,addr) (block->locals[idx].tinfo == TINFO_ADDR && block->locals[idx].data.retaddr == addr)
#define PEEK block->opstack[block->stacksz - 1]
#define PEEK2 block->opstack[block->stacksz - 2]
#define PEEK_IS_DOUBLE (PEEK.tinfo == TINFO_SECOND_HALF && PEEK2.tinfo == TINFO_PRIMITIVE && PEEK2.data.clazz == clazz_double)
#define PEEK_IS_FLOAT (PEEK.tinfo == TINFO_PRIMITIVE && PEEK.data.clazz == clazz_float)
#define PEEK_IS_INTEGER TYPE_IS_INTEGER(PEEK)
#define PEEK_IS_LONG (PEEK.tinfo == TINFO_SECOND_HALF && PEEK2.tinfo == TINFO_PRIMITIVE && PEEK2.data.clazz == clazz_long)
#define PEEK_MUST_BE_LOADED {if (PEEK.tinfo == TINFO_UNLOADED_CLASS) LOAD_TYPE(&PEEK, error)}
#define POP block->opstack[block->stacksz-- - 1]
#define POP_IS_CLASS(c) (t = POP, t.tinfo == TINFO_CLASS && t.data.clazz == (c))
#define POP_IS_DOUBLE ((void)POP, t = POP, t.tinfo == TINFO_PRIMITIVE && t.data.clazz == clazz_double)
#define POP_IS_FLOAT (t = POP, t.tinfo == TINFO_PRIMITIVE && t.data.clazz == clazz_float)
#define POP_IS_INTEGER (t = POP, TYPE_IS_INTEGER(t))
#define POP_IS_LONG ((void)POP, t = POP, t.tinfo == TINFO_PRIMITIVE && t.data.clazz == clazz_long)
#define POP_IS_PRIMITIVE(c) (((c) == clazz_double || (c) == clazz_long ? (void)POP : (void)0), t = POP, t.tinfo == TINFO_PRIMITIVE && t.data.clazz == (c))
#define POP_IS_REFERENCE (t = POP, IS_REFERENCE(t))
#define POP_PRIMITIVE_ARRAY(p) {PEEK_MUST_BE_LOADED V_ASSERT(POP_IS_CLASS(atype2clazz[(p)]), array_not_correct)}
#define PUSH(t) if (TYPE_IS_INTEGER(t)) { PUSH1(v_type_int); } else if ((t).tinfo == TINFO_PRIMITIVE) { if ((t).data.clazz == clazz_float) { PUSH1(t); } else { PUSH2(t); } } else { PUSH1(t); }
#define PUSH1(t) CHECK_STACK_CAPACITY(1); block->opstack[block->stacksz++] = (t)
#define PUSH2(t) CHECK_STACK_CAPACITY(2); block->opstack[block->stacksz++] = (t); block->opstack[block->stacksz++].tinfo = TINFO_SECOND_HALF
#define STORE(t,i) if (TYPE_IS_INTEGER(t)) { STORE1(v_type_int, (i)); } else if ((t).tinfo == TINFO_PRIMITIVE) { if ((t).data.clazz == clazz_float) { STORE1((t), (i)); } else { STORE2((t), (i)); } } else { STORE1((t), (i)); }
#define STORE1(t,i) block->locals[i] = (t)
#define STORE2(t,i) block->locals[i] = (t); block->locals[i + 1].tinfo = TINFO_SECOND_HALF
#define TYPE_IS_INTEGER(t) ((t).tinfo == TINFO_PRIMITIVE) && CLAZZ_IS_INTEGER((t).data.clazz)

/*
** Verify a basic block.
*/
w_boolean verifyBasicBlock(v_BasicBlock *block, v_MethodVerifier *mv, w_thread thread) {
  w_method method = mv->method;
  w_ubyte *status_array = method->exec.status_array;
  w_code code = method->exec.code;
  w_clazz declaring_clazz = mv->method->spec.declaring_clazz;
  w_ubyte opcode;
  w_size pc = block->start_pc;
  w_int idx;
  w_size n;
  v_Type t;
  v_Type aType;
  v_Type eType;
  w_clazz clazz;
  w_clazz aclazz;
  w_field f;
  w_method m;
  w_clazz *arglist;
  char *error_cstring;
#ifdef DEBUG
  int lineno;
#endif

  while (pc <= block->last_pc) {
    opcode = code[pc];
#ifdef DEBUG
    woempa(7, "stack size = %d (max is %d)\n", block->stacksz, method->exec.stack_i);
    for (n = block->stacksz; n > 0;) {
      w_dump_type("   ", mv, block->opstack[--n]);
    }
    woempa(7, "%m pc %d (%s)\n", method, pc, opc2name(opcode));
#endif
    switch(opcode) {
    case nop:
      break;

    case aconst_null:
      PUSH1(v_type_null);
      break;

    case iconst_m1:
    case iconst_0:
    case iconst_1:
    case iconst_2:
    case iconst_3:
    case iconst_4:
    case iconst_5:
    case bipush:
    case sipush:
    push_integer:
      PUSH1(v_type_int);
      break;

    case lconst_0:
    case lconst_1:
    push_long:
      PUSH2(v_type_long);
      break;

    case fconst_0:
    case fconst_1:
    case fconst_2:
    push_float:
      PUSH1(v_type_float);
      break;

    case dconst_0:
    case dconst_1:
    push_double:
      PUSH2(v_type_double);
      break;

    case ldc:
      idx = code[pc + 1];
      goto ldc_common;

    case ldc_w:
      idx = (code[pc + 1] << 8) | code[pc + 2];
    ldc_common:
      if (isIntegerConstant(declaring_clazz, idx)) {
        goto push_integer;
      }
      if (isFloatConstant(declaring_clazz, idx)) {
        goto push_float;
      }
      if (isStringConstant(declaring_clazz, idx)) {
        PUSH1(v_type_String);
      }
      else if (isClassConstant(declaring_clazz, idx)) {
        PUSH1(v_type_Class);
      }
      break;
      
    case ldc2_w:
      idx = (code[pc + 1] << 8) | code[pc + 2];
      if (isLongConstant(declaring_clazz, idx)) {
        goto push_long;
      }
      if (isDoubleConstant(declaring_clazz, idx)) {
        goto push_double;
      }
      break;
      
    case iload:
      idx = GET_INDEX;
      goto xload_common;

    case iload_0:
    case iload_1:
    case iload_2:
    case iload_3:
      idx = opcode - iload_0;

    xload_common:
      V_ASSERT(LOCAL_IS_INTEGER(idx), local_not_integer);
      goto push_integer;

    case lload:
      idx = GET_INDEX;
      goto lload_common;

    case lload_0:
    case lload_1:
    case lload_2:
    case lload_3:
      idx = opcode - lload_0;

    lload_common:
      V_ASSERT(LOCAL_IS_LONG(idx), local_not_long);
      goto push_long;

    case fload:
      idx = GET_INDEX;
      goto fload_common;

    case fload_0:
    case fload_1:
    case fload_2:
    case fload_3:
      idx = opcode - fload_0;

    fload_common:
      V_ASSERT(LOCAL_IS_FLOAT(idx), local_not_float);
      goto push_float;

    case dload:
      idx = GET_INDEX;
      goto dload_common;

    case dload_0:
    case dload_1:
    case dload_2:
    case dload_3:
      idx = opcode - dload_0;

    dload_common:
      V_ASSERT(LOCAL_IS_DOUBLE(idx), local_not_double);
      goto push_double;

    case aload:
      idx = GET_INDEX;
      goto aload_common;

    case aload_0:
    case aload_1:
    case aload_2:
    case aload_3:
      idx = opcode - aload_0;

    aload_common:
      V_ASSERT(LOCAL_IS_REFERENCE(idx), local_not_reference);
      t = block->locals[idx];
    push_reference:
      w_dump_type("pushing", mv, t);
      PUSH1(t);
      break;

    case iaload:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_INTEGER, index_not_integer);
      POP_PRIMITIVE_ARRAY(P_int);
      goto push_integer;

    case laload:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_INTEGER, index_not_integer);
      POP_PRIMITIVE_ARRAY(P_long);
      goto push_long;

    case faload:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_INTEGER, index_not_integer);
      POP_PRIMITIVE_ARRAY(P_float);
      goto push_float;

    case daload:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_INTEGER, index_not_integer);
      POP_PRIMITIVE_ARRAY(P_double);
      goto push_double;

    case aaload:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_INTEGER, index_not_integer);
      PEEK_MUST_BE_LOADED
      t = POP;
      V_ASSERT(IS_REFERENCE(t), array_not_correct);
      aType = getElementType(mv, t);
      V_ASSERT(aType.tinfo != TINFO_UNDEFINED, array_not_correct);
      V_ASSERT(IS_REFERENCE(aType), array_not_correct);
      PUSH1(aType);
      break;

    case baload:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_INTEGER, index_not_integer);
      PEEK_MUST_BE_LOADED
      t = POP;
      V_ASSERT(t.tinfo == TINFO_CLASS && (t.data.clazz == atype2clazz[P_boolean] || t.data.clazz == atype2clazz[P_byte]), array_not_correct);
      goto push_integer;

    case caload:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_INTEGER, index_not_integer);
      POP_PRIMITIVE_ARRAY(P_char);
      goto push_integer;

    case saload:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_INTEGER, index_not_integer);
      POP_PRIMITIVE_ARRAY(P_short);
      goto push_integer;

    case istore:
      idx = GET_INDEX;
      goto xstore_common;

    case lstore:
      idx = GET_INDEX;
      goto lstore_common;

    case fstore:
      idx = GET_INDEX;
      goto fstore_common;

    case dstore:
      idx = GET_INDEX;
      goto dstore_common;

    case astore:
      idx = GET_INDEX;
      goto astore_common;

    case istore_0:
    case istore_1:
    case istore_2:
    case istore_3:
      idx = opcode - istore_0;

    xstore_common:
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_INTEGER, value_not_correct);
      STORE(v_type_int, idx);
      break;

    case lstore_0:
    case lstore_1:
    case lstore_2:
    case lstore_3:
      idx = opcode - lstore_0;

    lstore_common:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_LONG, value_not_correct);
      STORE(v_type_long, idx);
      break;

    case fstore_0:
    case fstore_1:
    case fstore_2:
    case fstore_3:
      idx = opcode - fstore_0;

    fstore_common:
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_FLOAT, value_not_correct);
      STORE(v_type_float, idx);
      break;

    case dstore_0:
    case dstore_1:
    case dstore_2:
    case dstore_3:
      idx = opcode - dstore_0;

    dstore_common:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_DOUBLE, value_not_correct);
      STORE(v_type_double, idx);
      break;

    case astore_0:
    case astore_1:
    case astore_2:
    case astore_3:
      idx = opcode - astore_0;

    astore_common:
      CHECK_STACK_SIZE(1);
      t = POP;
      w_dump_type("astore: popped", mv, t);
      V_ASSERT(t.tinfo == TINFO_UNLOADED_CLASS || t.tinfo == TINFO_CLASS || t.tinfo == TINFO_UNINIT || t.tinfo == TINFO_UNINIT_SUPER || t.tinfo == TINFO_UNION || t.tinfo == TINFO_ADDR, value_not_correct);
      STORE(t, idx);
      break;

    case iastore:
      CHECK_STACK_SIZE(3);
      V_ASSERT(POP_IS_INTEGER, value_not_correct);
      aclazz = atype2clazz[P_int];
      goto xastore_common;

    case lastore:
      CHECK_STACK_SIZE(4);
      V_ASSERT(POP_IS_LONG, value_not_correct);
      aclazz = atype2clazz[P_long];
      goto xastore_common;

    case fastore:
      CHECK_STACK_SIZE(3);
      V_ASSERT(POP_IS_FLOAT, value_not_correct);
      aclazz = atype2clazz[P_float];
      goto xastore_common;

    case dastore:
      CHECK_STACK_SIZE(4);
      V_ASSERT(POP_IS_DOUBLE, value_not_correct);
      aclazz = atype2clazz[P_double];
      goto xastore_common;

    case aastore:
      CHECK_STACK_SIZE(3);
      eType = POP;
      w_dump_type("aastore value", mv, eType);
      V_ASSERT(IS_REFERENCE(eType), value_not_correct);
      V_ASSERT(POP_IS_INTEGER, index_not_integer);
      PEEK_MUST_BE_LOADED
      t = POP;
      w_dump_type("aastore array", mv, t);
      V_ASSERT(IS_REFERENCE(t), array_not_correct);
      aType = getElementType(mv, t);
      V_ASSERT(aType.tinfo != TINFO_UNDEFINED, array_not_correct);
/*
      if ((aType.tinfo == TINFO_CLASS && isSet(aType.data.clazz->flags, ACC_INTERFACE) || aType.tinfo == TINFO_UNION) && eType.tinfo == TINFO_CLASS && eType.data.clazz == clazzObject) {
          break;
      }
*/
      V_ASSERT(v_assignable2type(&aType, mv, &eType), array_not_correct);
      break;

    case bastore:
      CHECK_STACK_SIZE(3);
      V_ASSERT(POP_IS_INTEGER, value_not_correct);
      V_ASSERT(POP_IS_INTEGER, index_not_integer);
      PEEK_MUST_BE_LOADED
      t = POP;
      V_ASSERT(t.tinfo == TINFO_CLASS && (t.data.clazz == atype2clazz[P_boolean] || t.data.clazz == atype2clazz[P_byte]), array_not_correct);
      break;

    case castore:
      CHECK_STACK_SIZE(3);
      V_ASSERT(POP_IS_INTEGER, value_not_correct);
      aclazz = atype2clazz[P_char];
      goto xastore_common;

    case sastore:
      CHECK_STACK_SIZE(3);
      V_ASSERT(POP_IS_INTEGER, value_not_correct);
      aclazz = atype2clazz[P_short];

    xastore_common:
      V_ASSERT(POP_IS_INTEGER, index_not_integer);
      PEEK_MUST_BE_LOADED
      V_ASSERT(POP_IS_CLASS(aclazz), array_not_correct);
      break;

    case pop:
      CHECK_STACK_SIZE(1);
      t = POP;
      V_ASSERT(t.tinfo != TINFO_SECOND_HALF, splitting_value)
      break;

    case pop2:
      CHECK_STACK_SIZE(2);
      t = PEEK2;
      V_ASSERT(t.tinfo != TINFO_SECOND_HALF, splitting_value)
      block->stacksz -= 2;
      break;

    case dup:
      CHECK_STACK_SIZE(1);
      t = PEEK;
      V_ASSERT(t.tinfo != TINFO_SECOND_HALF, splitting_value)
      PUSH1(t);
      break;

    case dup_x1:
      CHECK_STACK_SIZE(2);
      t = PEEK;
      V_ASSERT(t.tinfo != TINFO_SECOND_HALF, splitting_value)
      PUSH1(t);
      block->opstack[block->stacksz - 2] = block->opstack[block->stacksz - 3];
      block->opstack[block->stacksz - 3] = t;
      break;

    case dup_x2:
      CHECK_STACK_SIZE(3);
      t = block->opstack[block->stacksz - 3];
      V_ASSERT(t.tinfo != TINFO_SECOND_HALF, splitting_value)
      t = PEEK;
      V_ASSERT(t.tinfo != TINFO_SECOND_HALF, splitting_value)
      PUSH1(t);
      block->opstack[block->stacksz - 2] = block->opstack[block->stacksz - 3];
      block->opstack[block->stacksz - 3] = block->opstack[block->stacksz - 4];
      block->opstack[block->stacksz - 4] = t;
      break;

    case dup2:
      CHECK_STACK_SIZE(2);
      t = PEEK2;
      V_ASSERT(t.tinfo != TINFO_SECOND_HALF, splitting_value)
      PUSH1(block->opstack[block->stacksz - 2]);
      PUSH1(block->opstack[block->stacksz - 2]);
      break;

    case dup2_x1:
      CHECK_STACK_SIZE(3);
      t = PEEK2;
      V_ASSERT(t.tinfo != TINFO_SECOND_HALF, splitting_value)
      t = block->opstack[block->stacksz - 3];
      V_ASSERT(t.tinfo != TINFO_SECOND_HALF, splitting_value)
      PUSH1(block->opstack[block->stacksz - 2]);
      PUSH1(block->opstack[block->stacksz - 2]);
      block->opstack[block->stacksz - 3] = block->opstack[block->stacksz - 5];
      block->opstack[block->stacksz - 4] = block->opstack[block->stacksz - 1];
      block->opstack[block->stacksz - 5] = block->opstack[block->stacksz - 2];
      break;

    case dup2_x2:
      CHECK_STACK_SIZE(4);
      t = PEEK2;
      V_ASSERT(t.tinfo != TINFO_SECOND_HALF, splitting_value)
      t = block->opstack[block->stacksz - 4];
      V_ASSERT(t.tinfo != TINFO_SECOND_HALF, splitting_value)
      PUSH1(block->opstack[block->stacksz - 2]);
      PUSH1(block->opstack[block->stacksz - 2]);
      block->opstack[block->stacksz - 3] = block->opstack[block->stacksz - 5];
      block->opstack[block->stacksz - 4] = block->opstack[block->stacksz - 6];
      block->opstack[block->stacksz - 5] = block->opstack[block->stacksz - 1];
      block->opstack[block->stacksz - 6] = block->opstack[block->stacksz - 2];
      break;

    case swap:
      CHECK_STACK_SIZE(2);
      t = PEEK2;
      V_ASSERT(t.tinfo != TINFO_SECOND_HALF, splitting_value)
      t = PEEK;
      V_ASSERT(t.tinfo != TINFO_SECOND_HALF, splitting_value)
      block->opstack[block->stacksz - 1] = block->opstack[block->stacksz - 2];
      block->opstack[block->stacksz - 2] = t;
      break;

    case iadd:
    case isub:
    case imul:
    case idiv:
    case irem:
    case ishl:
    case ishr:
    case iushr:
    case iand:
    case ior:
    case ixor:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_INTEGER, arg_not_correct);
      V_ASSERT(PEEK_IS_INTEGER, arg_not_correct);
      break;

    case ladd:
    case lsub:
    case lmul:
    case ldiv:
    case lrem:
    case land:
    case lor:
    case lxor:
      CHECK_STACK_SIZE(4);
      V_ASSERT(POP_IS_LONG, arg_not_correct);
      V_ASSERT(PEEK_IS_LONG, arg_not_correct);
      break;

    case fadd:
    case fsub:
    case fmul:
    case fdiv:
    case frem:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_FLOAT, arg_not_correct);
      V_ASSERT(PEEK_IS_FLOAT, arg_not_correct);
      break;

    case dadd:
    case dsub:
    case dmul:
    case ddiv:
    case drem:
      CHECK_STACK_SIZE(4);
      V_ASSERT(POP_IS_DOUBLE, arg_not_correct);
      V_ASSERT(PEEK_IS_DOUBLE, arg_not_correct);
      break;

    case ineg:
      CHECK_STACK_SIZE(1);
      V_ASSERT(PEEK_IS_INTEGER, arg_not_correct);
      break;

    case lneg:
      CHECK_STACK_SIZE(2);
      V_ASSERT(PEEK_IS_LONG, arg_not_correct);
      break;

    case fneg:
      CHECK_STACK_SIZE(1);
      V_ASSERT(PEEK_IS_FLOAT, arg_not_correct);
      break;

    case dneg:
      CHECK_STACK_SIZE(2);
      V_ASSERT(PEEK_IS_DOUBLE, arg_not_correct);
      break;

    case lshl:
    case lshr:
    case lushr:
      CHECK_STACK_SIZE(3);
      V_ASSERT(POP_IS_INTEGER, arg_not_correct);
      V_ASSERT(POP_IS_LONG, arg_not_correct);
      goto push_long;

    case iinc:
      idx = GET_INDEX;
      V_ASSERT(LOCAL_IS_INTEGER(idx), local_not_integer);
      break;

    case i2l:
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_INTEGER, arg_not_correct);
      goto push_long;

    case i2f:
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_INTEGER, arg_not_correct);
      goto push_float;

    case i2d:
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_INTEGER, arg_not_correct);
      goto push_double;

    case l2i:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_LONG, arg_not_correct);
      goto push_integer;

    case l2f:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_LONG, arg_not_correct);
      goto push_float;

    case l2d:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_LONG, arg_not_correct);
      goto push_double;

    case f2i:
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_FLOAT, arg_not_correct);
      goto push_integer;

    case f2l:
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_FLOAT, arg_not_correct);
      goto push_long;

    case f2d:
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_FLOAT, arg_not_correct);
      goto push_double;

    case d2i:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_DOUBLE, arg_not_correct);
      goto push_integer;

    case d2l:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_DOUBLE, arg_not_correct);
      goto push_long;

    case d2f:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_DOUBLE, arg_not_correct);
      goto push_float;

    case i2b:
    case i2c:
    case i2s:
      CHECK_STACK_SIZE(1);
      V_ASSERT(PEEK_IS_INTEGER, arg_not_correct);
      break;

    case lcmp:
      CHECK_STACK_SIZE(4);
      V_ASSERT(POP_IS_LONG, arg_not_correct);
      V_ASSERT(POP_IS_LONG, arg_not_correct);
      goto push_integer;

    case fcmpl:
    case fcmpg:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_FLOAT, arg_not_correct);
      V_ASSERT(POP_IS_FLOAT, arg_not_correct);
      goto push_integer;

    case dcmpl:
    case dcmpg:
      CHECK_STACK_SIZE(4);
      V_ASSERT(POP_IS_DOUBLE, arg_not_correct);
      V_ASSERT(POP_IS_DOUBLE, arg_not_correct);
      goto push_integer;

    case if_icmpeq:
    case if_icmpne:
    case if_icmplt:
    case if_icmpge:
    case if_icmpgt:
    case if_icmple:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_INTEGER, arg_not_correct);
      V_ASSERT(POP_IS_INTEGER, arg_not_correct);
      break;

    case if_acmpeq:
    case if_acmpne:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_REFERENCE, arg_not_correct);
      V_ASSERT(POP_IS_REFERENCE, arg_not_correct);
      break;

    case ifeq:
    case ifne:
    case iflt:
    case ifge:
    case ifgt:
    case ifle:
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_INTEGER, arg_not_correct);
      break;

    case tableswitch:
      pc = (pc + 4) & -4;
      n = ((((((code[pc + 8] << 8) | code[pc + 9]) << 8) | code[pc + 10]) << 8) | code[pc + 11]) - ((((((code[pc + 4] << 8) | code[pc + 5]) << 8) | code[pc + 6]) << 8) | code[pc + 7]) + 1;
      pc += 12 + (n * 4);
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_INTEGER, arg_not_correct);
      break;

    case lookupswitch:
      pc = (pc + 4) & -4;
      n = (((((code[pc + 4] << 8) | code[pc + 4]) << 8) | code[pc + 6]) << 8) | code[pc + 7];
      pc += 8 + (n * 8);
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_INTEGER, arg_not_correct);
      break;

    case j_goto:
      break;

    case jsr:
    case jsr_w:
      t.tinfo = TINFO_ADDR;
      t.data.retaddr = pc + instruction_length(opcode);
      w_dump_type("pushing", mv, t);
      PUSH1(t);
      break;

    case ret:
      idx = GET_INDEX;
      V_ASSERT(LOCAL_IS_RETURN_ADDRESS(idx, block->return_address), bad_return_address);
      break;

    case ireturn:
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_INTEGER, arg_not_correct);
      V_ASSERT(CLAZZ_IS_INTEGER(method->spec.return_type), return_not_match_type)
      break;

    case lreturn:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_LONG, arg_not_correct);
      V_ASSERT(method->spec.return_type == clazz_long, return_not_match_type)
      break;

    case freturn:
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_FLOAT, arg_not_correct);
      V_ASSERT(method->spec.return_type == clazz_float, return_not_match_type)
      break;

    case dreturn:
      CHECK_STACK_SIZE(2);
      V_ASSERT(POP_IS_DOUBLE, arg_not_correct);
      V_ASSERT(method->spec.return_type == clazz_double, return_not_match_type)
      break;

    case areturn:
      CHECK_STACK_SIZE(1);
      V_ASSERT(!clazzIsPrimitive(method->spec.return_type), return_not_match_type);
      t = POP;
      switch(t.tinfo) {
      case TINFO_UNLOADED_CLASS:
      case TINFO_CLASS:
      case TINFO_UNION:
        V_ASSERT(v_assignable2clazz(method->spec.return_type, mv, &t), value_not_correct);
        break;

      default:
        VERIFY_ERROR(value_not_correct);
      }
      break;

    case vreturn:
      V_ASSERT(method->spec.return_type == clazz_void, return_not_match_type)
      break;

    case getstatic:
woempa(7, "getstatic");
      idx = GET_INDEX2;
woempa(7, "idx %d type %02x\n", idx, declaring_clazz->tags[idx]);
      f = getFieldConstant(declaring_clazz, idx);
woempa(7, "field %V\n", f);
      CHECK_FOR_EXCEPTION(thread);
      V_ASSERT(isSet(f->flags, ACC_STATIC), member_not_static);
      // TODO: check access?
      CLAZZ2TYPE(f->value_clazz, t);
      PUSH(t);
      break;

    case putstatic:
      idx = GET_INDEX2;
      f = getFieldConstant(declaring_clazz, idx);
      CHECK_FOR_EXCEPTION(thread);
      V_ASSERT(isSet(f->flags, ACC_STATIC), member_not_static);
      // TODO: check access?
      if (f->value_clazz == clazz_long || f->value_clazz == clazz_double) {
        t = POP;
        V_ASSERT(t.tinfo = TINFO_SECOND_HALF, not_doubleword);
      }
      t = POP;
      woempa(1, "popped %d %p, field %v expects %k\n", t.tinfo, t.data.clazz, f, f->value_clazz);
      V_ASSERT((t.tinfo == TINFO_PRIMITIVE || t.tinfo == TINFO_UNLOADED_CLASS || t.tinfo == TINFO_CLASS || t.tinfo == TINFO_UNION) && v_assignable2clazz(f->value_clazz, mv, &t), value_not_correct);
      break;

    case getfield:
      CHECK_STACK_SIZE(1);
      idx = GET_INDEX2;
      f = getFieldConstant(declaring_clazz, idx);
      CHECK_FOR_EXCEPTION(thread);
      V_ASSERT(isNotSet(f->flags, ACC_STATIC), member_is_static);
      t = POP;
      V_ASSERT(IS_REFERENCE(t) && v_assignable2clazz(f->declaring_clazz, mv, &t), objectref_wrong_type);
      // TODO: check access?
      CLAZZ2TYPE(f->value_clazz, t);
      w_dump_type("pushing", mv, t);
      PUSH(t);
      break;

    case putfield:
      CHECK_STACK_SIZE(2);
      idx = GET_INDEX2;
      f = getFieldConstant(declaring_clazz, idx);
      CHECK_FOR_EXCEPTION(thread);
      V_ASSERT(isNotSet(f->flags, ACC_STATIC), member_is_static);
      // TODO: check access?
      if (f->value_clazz == clazz_long || f->value_clazz == clazz_double) {
        t = POP;
        V_ASSERT(t.tinfo = TINFO_SECOND_HALF, not_doubleword);
      }
      t = POP;
      w_dump_type("putfield: value =", mv, t);
      V_ASSERT((t.tinfo == TINFO_PRIMITIVE || t.tinfo == TINFO_UNLOADED_CLASS || t.tinfo == TINFO_CLASS || t.tinfo == TINFO_UNION) && v_assignable2clazz(f->value_clazz, mv, &t), value_not_correct);
      t = POP;
      w_dump_type("putfield: objectref =", mv, t);
      V_ASSERT(((t.tinfo == TINFO_UNLOADED_CLASS || t.tinfo == TINFO_CLASS || t.tinfo == TINFO_UNION) && v_assignable2clazz(f->declaring_clazz, mv, &t)) || (t.tinfo == TINFO_UNINIT_SUPER && isSuperClass(f->declaring_clazz, t.data.clazz)) , objectref_wrong_type);
      break;

    case invokevirtual:
    case invokespecial:
      idx = GET_INDEX2;
      m = getMethodConstant(declaring_clazz, idx);
      V_ASSERT(isNotSet(m->spec.declaring_clazz->flags, ACC_INTERFACE), member_is_interface);
      goto invoke_instance;

    case invokeinterface:
      idx = GET_INDEX2;
      m = getIMethodConstant(declaring_clazz, idx);
      V_ASSERT(isSet(m->spec.declaring_clazz->flags, ACC_INTERFACE), member_not_interface);

    invoke_instance:
      CHECK_FOR_EXCEPTION(thread);
      V_ASSERT(isNotSet(m->flags, ACC_STATIC), member_is_static);
      // TODO: check access?
      woempa(7, "Method %m expects %d elements on stack\n", m, m->exec.arg_i);
      n = m->exec.arg_i;
      CHECK_STACK_SIZE(n);
      --n;
      arglist = m->spec.arg_types;
      while (n) {
        w_clazz arg_type = *arglist++;

        woempa(7, "argument[%d] is of type %k\n", arglist - m->spec.arg_types, arg_type);
        t = block->opstack[block->stacksz - n];
        w_dump_type("actual argument =", mv, t);
        V_ASSERT(v_assignable2clazz(arg_type, mv, &t), parameter_type_mismatch);
        --n;
        if (arg_type == clazz_long || arg_type == clazz_double) {
          w_dump_type("skipped word with", mv, block->opstack[block->stacksz - n]);
          --n;
        }
      }

      t = block->opstack[block->stacksz - m->exec.arg_i];
      if (opcode == invokespecial && m->spec.name == string_angle_brackets_init) {
        if (m->spec.declaring_clazz == declaring_clazz) {
          woempa(7, "invokespecial on own constructor\n");
          w_dump_type("objectref =", mv, t);
          if (t.tinfo == TINFO_UNINIT && t.data.clazz == declaring_clazz) {
            popUninit(mv, t.uninitpc, block);
          }
          else if (t.tinfo == TINFO_UNINIT_SUPER) {
            popUninit(mv, t.uninitpc, block);
          }
          else {
            VERIFY_ERROR(objectref_wrong_type);
          }
        }
        else if (m->spec.declaring_clazz == getSuper(declaring_clazz)) {
          woempa(7, "invokespecial on constructor of superclass %k\n", m->spec.declaring_clazz);
          w_dump_type("objectref =", mv, t);
          // Note: it could be a call to super(), but it could just be a call
          // to a constructor of the superclass, e.g. new Object();
          if (t.tinfo == TINFO_UNINIT_SUPER) {
            popUninit(mv, t.uninitpc, block);
          }
          else if (t.tinfo == TINFO_UNINIT) {
            popUninit(mv, t.uninitpc, block);
          }
          else {
            VERIFY_ERROR(objectref_wrong_type);
          }
        }
        else { // someone else's constructor
          woempa(7, "invokespecial on constructor of non-superclass %k\n", m->spec.declaring_clazz);
          w_dump_type("objectref =", mv, t);

          if (t.tinfo == TINFO_UNINIT) {
            V_ASSERT(t.data.clazz == clazz_void || isAssignmentCompatible(t.data.clazz, m->spec.declaring_clazz), objectref_wrong_type);
            popUninit(mv, t.uninitpc, block);
          }
          else {
            VERIFY_ERROR(objectref_wrong_type);
          }
        }
      }
      else {
        woempa(7, "normal invocation of method %m of %k\n", m, m->spec.declaring_clazz);
        w_dump_type("objectref =", mv, t);
        V_ASSERT((t.tinfo == TINFO_UNLOADED_CLASS || t.tinfo == TINFO_CLASS || t.tinfo == TINFO_UNION) && v_assignable2clazz(m->spec.declaring_clazz, mv, &t), objectref_wrong_type);
      }
      goto invoke_common;

    case invokestatic:
      idx = GET_INDEX2;
      m = getMethodConstant(declaring_clazz, idx);
      CHECK_FOR_EXCEPTION(thread);
      V_ASSERT(isSet(m->flags, ACC_STATIC), invokestatic_not_static);
      // TODO: check access?
      woempa(1, "Method %m expects %d elements on stack\n", m, m->exec.arg_i);
      n = m->exec.arg_i;
      CHECK_STACK_SIZE(n);
      arglist = m->spec.arg_types;
      while (n) {
        w_clazz arg_type = *arglist++;

        woempa(1, "argument[%d] is of type %k\n", arglist - m->spec.arg_types, arg_type);
        t = block->opstack[block->stacksz - n];
        woempa(1, "actual argument = %d %k\n", t.tinfo, t.data.clazz);
        V_ASSERT(v_assignable2clazz(arg_type, mv, &t), parameter_type_mismatch);
        --n;
        if (arg_type == clazz_long || arg_type == clazz_double) {
          woempa(1, "skipped word with tinfo = %d\n", block->opstack[block->stacksz - n].tinfo);
          --n;
        }
      }

    invoke_common:
      block->stacksz -= m->exec.arg_i;
      if (m->spec.return_type != clazz_void) {
        CLAZZ2TYPE(m->spec.return_type, t);
        PUSH(t);
      }
      break;

    case new:
      idx = GET_INDEX2;
      clazz = getClassConstant(declaring_clazz, idx, thread);
      CHECK_FOR_EXCEPTION(thread);
      t.data.clazz = clazz;
      t.tinfo = TINFO_UNINIT;
      t.uninitpc = pushUninit(mv, pc);
      goto push_reference;

    case newarray:
      idx = GET_INDEX1;
      V_ASSERT(POP_IS_INTEGER, count_not_integer);
      CLAZZ2TYPE(atype2clazz[idx], t);
      goto push_reference;

    case anewarray:
      idx = GET_INDEX2;
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_INTEGER, count_not_integer);
      aclazz = getClassConstant(declaring_clazz, idx, thread);
      t.tinfo = TINFO_CLASS;
      t.uninitpc = 0;
      t.data.clazz = getNextDimension(aclazz, aclazz->loader);
      goto push_reference;

    case arraylength:
      CHECK_STACK_SIZE(1);
      t = POP;
      V_ASSERT(t.tinfo == TINFO_CLASS && (t.data.clazz == clazz_void || t.data.clazz->dims) || (t.tinfo == TINFO_UNLOADED_CLASS && string_char(t.data.dotified, 0) == '['), value_not_array);
      goto push_integer;

    case athrow:
      CHECK_STACK_SIZE(1);
      V_ASSERT(IS_REFERENCE(PEEK),value_not_reference);
      if (PEEK.tinfo == TINFO_UNLOADED_CLASS) {
        LOAD_TYPE(&PEEK, error)
      }
      if (PEEK.data.clazz != clazz_void) {
        V_ASSERT(isSet(PEEK.data.clazz->flags, CLAZZ_IS_THROWABLE),"not throwable");
      }
      // don't worry about the rest, the merging logic will take care of it
      break;

    case checkcast:
      idx = GET_INDEX2;
      CHECK_POOL_INDEX(idx);
      CHECK_STACK_SIZE(1);
      t = POP;
      V_ASSERT(IS_REFERENCE(t), value_not_reference);
      if ((declaring_clazz->tags[idx] & CONSTANT_STATE_MASK) == UNRESOLVED_CONSTANT) {
        w_int utf8_index = declaring_clazz->values[idx];
        CHECK_POOL_INDEX(utf8_index);
        if ((declaring_clazz->tags[utf8_index] & 0x0f) == CONSTANT_UTF8) {
          w_string name = resolveUtf8Constant(declaring_clazz, utf8_index);
          w_string dotified = slashes2dots(name);
        // TODO: deregisterString(name);
          t.tinfo = TINFO_UNLOADED_CLASS;
          t.uninitpc = 0;
          t.data.dotified = dotified;
        } else {
          goto verify_error;
        }
      }
      else { 
        w_clazz target_clazz = getClassConstant(declaring_clazz, idx, thread);
        CHECK_FOR_EXCEPTION(thread);
        CLAZZ2TYPE(target_clazz, t);
        woempa(1, "checkcast to %k\n", t.data.clazz);
      }
      goto push_reference;

    case instanceof:
      CHECK_STACK_SIZE(1);
      V_ASSERT(POP_IS_REFERENCE, value_not_reference);
      goto push_integer;

    case monitorenter:
    case monitorexit:
    case ifnull:
    case ifnonnull:
      V_ASSERT(POP_IS_REFERENCE, value_not_reference);
      break;

    case wide:
      break;

    case multianewarray:
      idx = GET_INDEX2;
      CHECK_POOL_INDEX(idx);
/*
      if ((declaring_clazz->tags[idx] & CONSTANT_STATE_MASK) == UNRESOLVED_CONSTANT) {
        w_int utf8_index = declaring_clazz->values[idx];
        CHECK_POOL_INDEX(utf8_index);
        if ((declaring_clazz->tags[utf8_index] & 0x0f) == CONSTANT_UTF8) {
          w_string name = resolveUtf8Constant(declaring_clazz, utf8_index);
          w_string dotified = slashes2dots(name);
        // TODO: deregisterString(name);
          t.tinfo = TINFO_CLASS;
          t.uninitpc = 0;
          t.data.dotified = dotified;
        } else {
          goto verify_error;
        }
      }
      else { 
*/
        aclazz = getClassConstant(declaring_clazz, idx, thread);
        CHECK_FOR_EXCEPTION(thread);
        t.tinfo = TINFO_CLASS;
        t.uninitpc = 0;
        t.data.clazz = aclazz;
//      }
      n = method->exec.code[pc + 3];
      CHECK_STACK_SIZE(n);
      V_ASSERT(aclazz->dims >= n, array_too_few_dims);
      block->stacksz -= n;
      goto push_reference;

    case goto_w:
    case breakpoint:
      break;

    default:
      wabort(ABORT_WONKA, "illegal opcode!");
    }
    pc += instruction_length(opcode);
  }

  return TRUE;

verify_error:
#ifdef DEBUG
  throwException(currentWonkaThread, clazzVerifyError, "verifyBasicBlock() line %d: %k.%m pc %d (%s): %s", lineno, method->spec.declaring_clazz, method, pc, opc2name(method->exec.code[pc]), error_cstring);
#else
  throwException(currentWonkaThread, clazzVerifyError, "%k.%m pc %d (%s): %s", method->spec.declaring_clazz, method, pc, opc2name(method->exec.code[pc]), error_cstring);
#endif

error:
  return FALSE;
}

/*
** Possible return codes from mergeTypes/mergeBlocks
*/
#define MERGE_DID_NOTHING 0
#define MERGE_SUCCEEDED 1
#define MERGE_FAILED -1

/*
** Merge two types. If successful, either old_type is updated to hold the 
** merged type and MERGE_SUCCEEDED is returned, or in trivial cases old_type
** is unchanged and MERGE_DID_NOTHING is returned. If unsuccessful, returns
** MERGE_FAILED.
*/
w_int mergeTypes(v_MethodVerifier *mv, v_Type *old_type, v_Type *new_type) {

  if (memcmp(old_type, new_type, sizeof(v_Type)) == 0) {

    return MERGE_DID_NOTHING;

  }

  if (new_type->tinfo == TINFO_UNDEFINED) {
    *old_type = *new_type;
    old_type->data.undef_kind = UNDEF_SOMETIMES;

    return MERGE_SUCCEEDED;
  }

  switch (old_type->tinfo) {
  case TINFO_UNDEFINED:
    *old_type = *new_type;
    return MERGE_SUCCEEDED;

  case TINFO_ADDR:
    if (new_type->tinfo == TINFO_ADDR && new_type->data.retaddr == old_type->data.retaddr) {
      return MERGE_DID_NOTHING;
    }
    break;

  case TINFO_PRIMITIVE:
    if (new_type->tinfo == TINFO_PRIMITIVE && new_type->data.clazz == old_type->data.clazz) {
      return MERGE_DID_NOTHING;
    }
    break;

  case TINFO_UNLOADED_CLASS:
    LOAD_TYPE(old_type, fail);
    // fall through
 
  case TINFO_CLASS:
    if (new_type->tinfo == TINFO_UNLOADED_CLASS) {
      LOAD_TYPE(new_type, fail);
    }
    if (new_type->tinfo == TINFO_CLASS) {
      woempa(7, "Merging classes...\n");
      if (new_type->data.clazz == clazz_void) {
        woempa(7, "new class is void, leaving as is\n");
        return MERGE_DID_NOTHING;
      }
      if (old_type->data.clazz == clazz_void) {
        woempa(7, "old class is void, replacing by new\n");
        *old_type = *new_type;
        return MERGE_SUCCEEDED;
      }
      if (new_type->data.clazz == old_type->data.clazz) {
        woempa(7, "both are type %k, leaving as is\n", new_type->data.clazz);
        return MERGE_DID_NOTHING;
      }

      if (isAssignmentCompatible(new_type->data.clazz, old_type->data.clazz)) {
        woempa(7, "new type %k is subclass of old type %k, leaving as is\n", new_type->data.clazz, old_type->data.clazz);
        return MERGE_DID_NOTHING;
      }
      if (isAssignmentCompatible(old_type->data.clazz, new_type->data.clazz)) {
        woempa(7, "old type %k is subclass of new type %k, replacing old by new\n", old_type->data.clazz, new_type->data.clazz);
        *old_type = *new_type;
        return MERGE_SUCCEEDED;
      }

      old_type->tinfo = TINFO_UNION;
      old_type->data.union_index = createUnion2(mv, old_type->data.clazz, new_type->data.clazz);
      return MERGE_SUCCEEDED;
      /* WAS:
      super = getSuper(new_type->data.clazz);
      while (super) {
        woempa(7, "Searching for common superclass, trying %k\n", super);
        if (isAssignmentCompatible(old_type->data.clazz, super)) {
          woempa(7, "old type %k can be assigned to %k, replacing it\n", old_type->data.clazz, super);
          old_type->data.clazz = super;
          return MERGE_SUCCEEDED;
        }
        super = getSuper(super);
      }
      */
    }
    else if (new_type->tinfo == TINFO_UNION) {
      old_type->tinfo = TINFO_UNION;
      old_type->data.union_index = mergeClassIntoUnion(mv, old_type->data.clazz, new_type->data.union_index);
      return MERGE_SUCCEEDED;
    }
    break;

  case TINFO_UNINIT:
  case TINFO_UNINIT_SUPER:
    if (new_type->tinfo == old_type->tinfo && new_type->uninitpc == old_type->uninitpc && new_type->data.clazz == old_type->data.clazz) {
      return MERGE_DID_NOTHING;
    }
    break;

  case TINFO_UNION:
    {
      w_size old_index = old_type->data.union_index;
      w_size new_index;

      if (old_type->tinfo == TINFO_UNLOADED_CLASS) {
        LOAD_TYPE(old_type, fail);
      }

      if (new_type->tinfo == TINFO_CLASS) {
        new_index = mergeClassIntoUnion(mv, new_type->data.clazz, old_index);
      }
      else if (new_type->tinfo == TINFO_UNION) {
        new_index = mergeUnions(mv, new_type->data.union_index, old_index);
      }
      else {

        // [CG 20070126] Don't fail yet, maybe the slot is never used
        // return MERGE_FAILED;
        break;
      }

      return new_index == old_index ? MERGE_DID_NOTHING : MERGE_SUCCEEDED;
    }

  case TINFO_SECOND_HALF:
    if (new_type->tinfo == TINFO_SECOND_HALF) {
      return MERGE_DID_NOTHING;
    }
    // [CG 20070126] Don't fail yet, maybe the slot is never used
    // return MERGE_FAILED;

  }

  woempa(7, "Merged conflicting types:\n");
  w_dump_type("   old:", mv, *old_type);
  w_dump_type("   new:", mv, *new_type);
  old_type->tinfo = TINFO_UNDEFINED;
  old_type->data.undef_kind = UNDEF_CONFLICT;

  return MERGE_SUCCEEDED;

fail:
  return MERGE_FAILED;
}

/*
*/
w_int mergeBlocks(v_MethodVerifier *mv, v_BasicBlock *old_block, v_BasicBlock *new_block) {
  w_size i;
  w_int result = MERGE_DID_NOTHING;

  if (isNotSet(old_block->flags, VISITED)) {
    woempa(7, "block  %p has not yet been visited, copying state from block %p\n", old_block, new_block);
    old_block->stacksz = new_block->stacksz;
/*
    memcpy(old_block->locals, new_block->locals, mv->method->exec.local_i * sizeof(v_Type));
    memcpy(old_block->opstack, new_block->opstack, mv->method->exec.stack_i * sizeof(v_Type));
*/
    for (i = 0; i < old_block->localsz; ++i) {
      woempa(7, "Filling in old locals[%d]\n", i);
      w_dump_type("   ", mv, new_block->locals[i]);
      old_block->locals[i] = new_block->locals[i];
    }

    for (i = 0; i < old_block->stacksz; ++i) {
      woempa(7, "Filling in old opstack[%d]\n", i);
      w_dump_type("   ", mv, new_block->opstack[i]);
      old_block->opstack[i] = new_block->opstack[i];
    }

    setFlag(old_block->flags, CHANGED | VISITED);

    return MERGE_SUCCEEDED;
  }
  else {
    woempa(7, "block  %p has already been visited, merging state from block %p\n", old_block, new_block);

#ifdef RUNTIME_CHECKS
    if (old_block->localsz != new_block->localsz) {
      wabort(ABORT_WONKA, "Crikey. Basic blocks to be merged had different numbers of local vars");
    }
#endif

    if (old_block->stacksz != new_block->stacksz) {
      woempa(7, "old_block->stacksz = %d, new_block->stacksz = %d\n", old_block->stacksz, new_block->stacksz);
      throwException(currentWonkaThread, clazzVerifyError, "stack size mismatch when merging");

      return MERGE_FAILED;
    }

    for (i = 0; i < old_block->localsz; ++i) {
      woempa(7, "Merging locals[%d]\n", i);
      w_dump_type("  old:", mv, old_block->locals[i]);
      w_dump_type("  new:", mv, new_block->locals[i]);
      result |= mergeTypes(mv, &old_block->locals[i], &new_block->locals[i]);
      if (result == MERGE_FAILED) {

        return MERGE_FAILED;

      }
    }

    for (i = 0; i < old_block->stacksz; ++i) {
      woempa(7, "Merging opstack[%d]\n", i);
      w_dump_type("  old:", mv, old_block->opstack[i]);
      w_dump_type("  new:", mv, new_block->opstack[i]);
      result |= mergeTypes(mv, &old_block->opstack[i], &new_block->opstack[i]);
      if (result == MERGE_FAILED) {

        return MERGE_FAILED;

      }
    }

    if (result == MERGE_SUCCEEDED) {
      setFlag(old_block->flags, CHANGED);
    }
  }

  return result;
}

/*
*/
w_boolean verifyMethod(w_method method, w_thread thread) {
  v_MethodVerifier mv;
  v_BasicBlock* thisBlock = NULL;
  v_BasicBlock* currentBlock = NULL;
  v_BasicBlock* successorBlock = NULL;
  w_size block_index;
  w_size successor_index;
  w_ubyte succ_kind;
  w_boolean result;

  if (!v_type_int.tinfo) initVerifier();

  mv.method = method;
  mv.code_length = method->exec.code_length;
  mv.numBlocks = 0;
  mv.method->exec.basicBlocks = NULL;
  mv.unions = NULL;

    identifyBasicBlocks(&mv);

    if (mv.method->exec.basicBlocks) {
      // listBasicBlocks(mv.method->exec.basicBlocks);
      result = loadInitialArgs(&mv);
      if (result) {
        thisBlock = getBasicBlock(&mv, 0);
        setFlag(thisBlock->flags, CHANGED);
        block_index = 0;
        currentBlock = createBlock(&mv);
        if (!currentBlock) {
          // An OutOfMemoryError has been thrown

          goto exit_verify;

        }
        while (block_index < mv.numBlocks) {
          woempa(7, "basic block[%d] has changed, re-evaluating it\n", block_index);
          thisBlock = getBasicBlock(&mv, block_index);
          unsetFlag(thisBlock->flags, CHANGED);
          setFlag(thisBlock->flags, VISITED);
          copyBlock(currentBlock, thisBlock, &mv);

          result = verifyBasicBlock(currentBlock, &mv, thread);
          if (!result) {

             goto exit_verify;

          }

          /* Scan the "normal" successors */
          for (successor_index = 0; successor_index < mv.numBlocks; ++successor_index) {
            succ_kind = thisBlock->successors[successor_index];
            if (succ_kind && succ_kind != SUCC_EXCEPTION) {
              successorBlock = getBasicBlock(&mv, successor_index);
              woempa(7, "successor block [%d] = %p\n", successor_index, successorBlock);
              // TODO: check we don't create illegal combinations of flags
              // TODO: find out what combinations are illegal ...
              switch(succ_kind) {
                case SUCC_RET:
                case SUCC_NORMAL:
                  setFlag(successorBlock->flags, NORMAL);
                  break;

                case SUCC_JSR:
                  setFlag(successorBlock->flags, SUBROUTINE);
                  break;

                default:
                  wabort(ABORT_WONKA, "illegal succ_kind %d", succ_kind);
              }
              //listBasicBlock(successorBlock);
              if (mergeBlocks(&mv, successorBlock, currentBlock) == MERGE_FAILED) {
                result = FALSE;

                goto exit_verify;

              }
            }
          }

          /* Now clear the stack and handle the "exception" successors */
          currentBlock->stacksz = 1;
          for (successor_index = 0; successor_index < mv.numBlocks; ++successor_index) {
            if (thisBlock->successors[successor_index] == SUCC_EXCEPTION) {
              successorBlock = getBasicBlock(&mv, successor_index);
              CLAZZ2TYPE(successorBlock->exception_type, currentBlock->opstack[0]);
              woempa(7, "exception successor block [%d] = %p\n", successor_index, successorBlock);
              //listBasicBlock(successorBlock);
              if (mergeBlocks(&mv, successorBlock, currentBlock) == MERGE_FAILED) {
                result = FALSE;

                goto exit_verify;

              }
            }
          }

          // Find another CHANGED block. If we don't find one, verification is
          // complete and the outer loop (block_index < numBlocks) will exit.
          for (block_index = 0; block_index < mv.numBlocks; ++block_index) {
            thisBlock = getBasicBlock(&mv, block_index);
            if (isSet(thisBlock->flags, CHANGED)) {
              break;
            }
          }
        }
      }
    }

exit_verify:
  if (currentBlock) {
    releaseMem(currentBlock);
  }
  releaseMethodVerifier(&mv);

  return result;
}

