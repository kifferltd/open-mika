/**************************************************************************
* Copyright  (c) 2001, 2002 by Acunia N.V. All rights reserved.           *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: verify.c,v 1.3 2004/11/30 10:08:10 cvs Exp $
*/

#include "clazz.h"
#include "checks.h"
#include "constant.h"
#include "exception.h"
#include "loading.h"
#include "methods.h"
#include "opcodes.h"
#include "threads.h"
#include "ts-mem.h"
#include "wonka.h"
#include <string.h>  /* for strlen */

/*
** References of the form [J+JVM ...] are to the book _Java and the Java
** Virtual Machine_ by Robert Staerk, Joachim Schmid, and Egon Boerger.
*/

/*
** Generate an instance of VerifyError using the name of the failing class
** and two strings, one C (ISO 8859-1) and one Java.
*/

void createVerifyError(w_clazz failing_clazz, char *msg, w_string str) {

  char    *cptr;
  w_char  *buffer;
  w_size   length;
  w_string message;
  w_thread thread = currentWonkaThread;

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
    wabort(ABORT_WONKA, "Unable to allocate buffer\n");
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
  if (!message) {
    wabort(ABORT_WONKA, "Unable to create message\n");
  }
  releaseMem(buffer);
  woempa(9,"%w: VerifyError: %w\n", failing_clazz->dotified, message);
  throwException(thread, clazzVerifyError, "%w", message);
  failing_clazz->failure_message = message;

}

/*
** So far we do not perform any verification ...
** TODO: go yak-trekking in Mongolia
*/

void verifyClass(w_clazz clazz) {


}

/*
 * Based on the Kaffe verifier by Rob Gonzalez <rob@kaffe.org>.
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

struct v_UninitializedType;

typedef struct v_Type
{
	w_int tinfo;
	
	union {
	        /* different ways to refer to an object reference */
		w_string name;
		w_string sig;
		w_clazz clazz;
		
	        /* uninitialized object reference */
		struct v_UninitializedType* uninit;
		
	        /* list of supertypes in the event of multiple inheritence of interfaces. */
		w_clazz* supertypes;
		
	        /* return address for TINFO_ADDR */
		w_int addr;
	} data;
} v_Type;

/* status flags for opstack/local info arrays
 *
 *   TINFO_SYSTEM       internal type, such as UNSTABLE or VOID
 *   TINFO_ADDR         return address type
 *   TINFO_PRIMITIVE    v_Type.data.class is some primitive class, like clazz_int
 *   TINFO_CLASS        v_Type.data.class
 *   TINFO_NAME         v_Type.data.name represents the class' fully qualified name
 *   TINFO_SIG          v_Type.data.sig  represents the class' fully qualified type signature
 *   TINFO_UNINIT       is a class instance created by NEW that has yet to be initialized.
 *                      the type is really an (v_UninitializedType*), so that dups, moves, etc. ensure that whatever
 *                      copies of the type are around are all initialized when the <init>() is called.
 *   TINFO_UNINIT_SUPER reserved for the self-reference in a constructor method.  when the receiver of a call to <init>()
 *                      is of type TINFO_UNINIT_SUPER, then the <init>() referenced may be in the current class of in its
 *                      superclass.
 *   TINFO_SUPERLIST    a list of supertypes.  used when merging two types that have multiple common supertypes.
 *                      this can occur with the multiple inheritence of interfaces.
 *                      the zeroth element is always a common superclass, the rest are common superinterfaces.
 */
#define TINFO_SYSTEM       0
#define TINFO_ADDR         1
#define TINFO_PRIMITIVE    2
#define TINFO_SIG          4
#define TINFO_NAME         8
#define TINFO_CLASS        16
#define TINFO_UNINIT       32
#define TINFO_UNINIT_SUPER 96
#define TINFO_SUPERLIST    128

#define IS_ADDRESS(_TINFO) ((_TINFO)->tinfo & TINFO_ADDR)
#define IS_PRIMITIVE_TYPE(_TINFO) ((_TINFO)->tinfo & TINFO_PRIMITIVE)


/*
 * holds the list of uninitialized items.  that way, if we DUP some uninitialized
 * reference, put it into a local variable, dup it again, etc, all will point to
 * one item in this list, so when we <init> any of those we can init them all! :)
 *
 * doubly linked list to allow for easy removal of types
 */
typedef struct v_UninitializedType
{
	struct v_Type type;
	
	struct v_UninitializedType* prev;
	struct v_UninitializedType* next;
} v_UninitializedType;



/*
 * basic block header information
 */
typedef struct v_BasicBlock {
        /* address of start of block */
	w_word startAddr;
        w_word lastAddr;  /* whether it be the address of a GOTO, etc. */
	
        /* status of block...
	 * changed (needs to be re-evaluated), visited, etc. 
	 */
	w_word status;
	
        /* array of local variables */
	v_Type*  locals;
	
        /* simulated operand stack */
	w_size stacksz;
	v_Type*  opstack;
} v_BasicBlock;

/* status flags for a basic block.
 * these also pertain to the status[] array for the entire instruction array
 */
#define CHANGED            1
#define VISITED            2
#define IS_INSTRUCTION     4

/* if the instruction is preceeded by WIDE */
#define WIDE_MODDED        8

/* used at the instruction status level to find basic blocks */
#define START_BLOCK       16
#define END_BLOCK         32

#define EXCEPTION_HANDLER 64

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

/*
** Lengths of the various instructions, indexed by opcode.
*/
static const w_ushort instruction_length_and_features[256] = {
                            1, // nop         0x00
                            1, // aconst_null 0x01
                            1, // iconst_m1   0x02
                            1, // iconst_0    0x03
                            1, // iconst_1    0x04
                            1, // iconst_2    0x05
                            1, // iconst_3    0x06
                            1, // iconst_4    0x07
                            1, // iconst_5    0x08
                            1, // lconst_0    0x09
                            1, // lconst_1    0x0a
                            1, // fconst_0    0x0b
                            1, // fconst_1    0x0c
                            1, // fconst_2    0x0d
                            1, // dconst_0    0x0e
                            1, // dconst_1    0x0f
                            2, // bipush      0x10
                            3, // sipush      0x11
                            2, // ldc         0x12
                            3, // ldc_w       0x13
                            3, // ldc2_w      0x14
  INSTRUCTION_CAN_BE_WIDE + 2, // iload       0x15
  INSTRUCTION_CAN_BE_WIDE + 2, // lload       0x16
  INSTRUCTION_CAN_BE_WIDE + 2, // fload       0x17
  INSTRUCTION_CAN_BE_WIDE + 2, // dload       0x18
  INSTRUCTION_CAN_BE_WIDE + 2, // aload       0x19
  INSTRUCTION_USES_VAR_0  + 1, // iload_0     0x1a
  INSTRUCTION_USES_VAR_1  + 1, // iload_1     0x1b
  INSTRUCTION_USES_VAR_2  + 1, // iload_2     0x1c
  INSTRUCTION_USES_VAR_3  + 1, // iload_3     0x1d
  INSTRUCTION_USES_VAR_1  + 1, // lload_0     0x1e
  INSTRUCTION_USES_VAR_2  + 1, // lload_1     0x1f
  INSTRUCTION_USES_VAR_3  + 1, // lload_2     0x20
  INSTRUCTION_USES_VAR_4  + 1, // lload_3     0x21
  INSTRUCTION_USES_VAR_0  + 1, // fload_0     0x22
  INSTRUCTION_USES_VAR_1  + 1, // fload_1     0x23
  INSTRUCTION_USES_VAR_2  + 1, // fload_2     0x24
  INSTRUCTION_USES_VAR_3  + 1, // fload_3     0x25
  INSTRUCTION_USES_VAR_1  + 1, // dload_0     0x26
  INSTRUCTION_USES_VAR_2  + 1, // dload_1     0x27
  INSTRUCTION_USES_VAR_3  + 1, // dload_2     0x28
  INSTRUCTION_USES_VAR_4  + 1, // dload_3     0x29
  INSTRUCTION_USES_VAR_0  + 1, // aload_0     0x2a
  INSTRUCTION_USES_VAR_1  + 1, // aload_1     0x2b
  INSTRUCTION_USES_VAR_2  + 1, // aload_2     0x2c
  INSTRUCTION_USES_VAR_3  + 1, // aload_3     0x2d
                            1, // iaload      0x2e
                            1, // laload      0x2f
                            1, // faload      0x30
                            1, // daload      0x31
                            1, // aaload      0x32
                            1, // baload      0x33
                            1, // caload      0x34
                            1, // saload      0x35
  INSTRUCTION_CAN_BE_WIDE + 2, // istore      0x36
  INSTRUCTION_CAN_BE_WIDE + 2, // lstore      0x37
  INSTRUCTION_CAN_BE_WIDE + 2, // fstore      0x38
  INSTRUCTION_CAN_BE_WIDE + 2, // dstore      0x39
  INSTRUCTION_CAN_BE_WIDE + 2, // astore      0x3a
  INSTRUCTION_USES_VAR_0  + 1, // istore_0    0x3b
  INSTRUCTION_USES_VAR_1  + 1, // istore_1    0x3c
  INSTRUCTION_USES_VAR_2  + 1, // istore_2    0x3d
  INSTRUCTION_USES_VAR_3  + 1, // istore_3    0x3e
  INSTRUCTION_USES_VAR_1  + 1, // lstore_0    0x3f
  INSTRUCTION_USES_VAR_2  + 1, // lstore_1    0x40
  INSTRUCTION_USES_VAR_3  + 1, // lstore_2    0x41
  INSTRUCTION_USES_VAR_4  + 1, // lstore_3    0x42
  INSTRUCTION_USES_VAR_0  + 1, // fstore_0    0x43
  INSTRUCTION_USES_VAR_1  + 1, // fstore_1    0x44
  INSTRUCTION_USES_VAR_2  + 1, // fstore_2    0x45
  INSTRUCTION_USES_VAR_3  + 1, // fstore_3    0x46
  INSTRUCTION_USES_VAR_1  + 1, // dstore_0    0x47
  INSTRUCTION_USES_VAR_2  + 1, // dstore_1    0x48
  INSTRUCTION_USES_VAR_3  + 1, // dstore_2    0x49
  INSTRUCTION_USES_VAR_4  + 1, // dstore_3    0x4a
  INSTRUCTION_USES_VAR_0  + 1, // astore_0    0x4b
  INSTRUCTION_USES_VAR_1  + 1, // astore_1    0x4c
  INSTRUCTION_USES_VAR_2  + 1, // astore_2    0x4d
  INSTRUCTION_USES_VAR_3  + 1, // astore_3    0x4e
                            1, // iastore     0x4f
                            1, // lastore     0x50
                            1, // fastore     0x51
                            1, // dastore     0x52
                            1, // aastore     0x53
                            1, // bastore     0x54
                            1, // castore     0x55
                            1, // sastore     0x56
                            1, // pop         0x57
                            1, // pop2        0x58
                            1, // dup         0x59
                            1, // dup_x1      0x5a
                            1, // dup_x2      0x5b
                            1, // dup2        0x5c
                            1, // dup2_x1     0x5d
                            1, // dup2_x2     0x5e
                            1, // swap        0x5f
                            1, // iadd        0x60
                            1, // ladd        0x61
                            1, // fadd        0x62
                            1, // dadd        0x63
                            1, // isub        0x64
                            1, // lsub        0x65
                            1, // fsub        0x66
                            1, // dsub        0x67
                            1, // imul        0x68
                            1, // lmul        0x69
                            1, // fmul        0x6a
                            1, // dmul        0x6b
                            1, // idiv        0x6c
                            1, // ldiv        0x6d
                            1, // fdiv        0x6e
                            1, // ddiv        0x6f
                            1, // irem        0x70
                            1, // lrem        0x71
                            1, // frem        0x72
                            1, // drem        0x73
                            1, // ineg        0x74
                            1, // lneg        0x75
                            1, // fneg        0x76
                            1, // dneg        0x77
                            1, // ishl        0x78
                            1, // lshl        0x79
                            1, // ishr        0x7a
                            1, // lshr        0x7b
                            1, // iushr       0x7c
                            1, // lushr       0x7d
                            1, // iand        0x7e
                            1, // land        0x7f
                            1, // ior         0x80
                            1, // lor         0x81
                            1, // ixor        0x82
                            1, // lxor        0x83
  INSTRUCTION_CAN_BE_WIDE + 3, // iinc        0x84
                            1, // i2l         0x85
                            1, // i2f         0x86
                            1, // i2d         0x87
                            1, // l2i         0x88
                            1, // l2f         0x89
                            1, // l2d         0x8a
                            1, // f2i         0x8b
                            1, // f2l         0x8c
                            1, // f2d         0x8d
                            1, // d2i         0x8e
                            1, // d2l         0x8f
                            1, // d2f         0x90
                            1, // i2b         0x91
                            1, // i2c         0x92
                            1, // i2s         0x93
                            1, // lcm         0x94
                            1, // fcmpl    0x95
                            1, // fcmpg    0x96
                            1, // dcmpl    0x97
                            1, // dcmpg    0x98
                            3, // ifeq    0x99
                            3, // ifne    0x9a
                            3, // iflt    0x9b
                            3, // ifge    0x9c
                            3, // ifgt    0x9d
                            3, // ifle    0x9e
                            3, // if_icmpeq			0x9f
                            3, // if_icmpne			0xa0
                            3, // if_icmplt			0xa1
                            3, // if_icmpge			0xa2
                            3, // if_icmpgt			0xa3
                            3, // if_icmple			0xa4
                            3, // if_acmpeq			0xa5
                            3, // if_acmpne			0xa6
                            3, // j_goto				0xa7
                            3, // jsr				0xa8
  INSTRUCTION_CAN_BE_WIDE + 2, // ret				0xa9
                            0, // tableswitch			0xaa
                            0, // lookupswitch			0xab
                            1, // ireturn				0xac
                            1, // lreturn				0xad
                            1, // freturn				0xae
                            1, // dreturn				0xaf
                            1, // areturn				0xb0
                            1, // vreturn				0xb1
                            3, // getstatic			0xb2
                            3, // putstatic			0xb3
                            3, // getfield			0xb4
                            3, // putfield			0xb5
                            3, // invokevirtual			0xb6
                            3, // invokespecial			0xb7
                            3, // invokestatic			0xb8
                            5, // invokeinterface			0xb9
                            1, // in_new	 			0xba
                            3, // new				0xbb
                            2, // newarray			0xbc
                            3, // anewarray			0xbd
                            1, // arraylength			0xbe
                            1, // athrow				0xbf
                            3, // checkcast			0xc0
                            3, // instanceof			0xc1
                            1, // monitorenter			0xc2
                            1, // monitorexit			0xc3
                            1, // wide				0xc4
                            4, // multianewarray			0xc5
                            3, // ifnull				0xc6
                            3, // ifnonnull			0xc7
                            5, // goto_w				0xc8
                            5, // jsr_w				0xc9
                            1, // breakpoint			0xca
  INSTRUCTION_UNDEFINED +   1, // 0xcb
  INSTRUCTION_UNDEFINED +   1, // 0xcc
  INSTRUCTION_UNDEFINED +   1, // 0xcd
  INSTRUCTION_UNDEFINED +   1, // 0xce
  INSTRUCTION_UNDEFINED +   1, // 0xcf
  INSTRUCTION_UNDEFINED +   1, // 0xd0
  INSTRUCTION_UNDEFINED +   1, // 0xd1
  INSTRUCTION_UNDEFINED +   1, // 0xd2
  INSTRUCTION_UNDEFINED +   1, // 0xd3
  INSTRUCTION_UNDEFINED +   1, // 0xd4
  INSTRUCTION_UNDEFINED +   1, // 0xd5
  INSTRUCTION_UNDEFINED +   1, // 0xd6
  INSTRUCTION_UNDEFINED +   1, // 0xd7
  INSTRUCTION_UNDEFINED +   1, // 0xd8
  INSTRUCTION_UNDEFINED +   1, // 0xd9
  INSTRUCTION_UNDEFINED +   1, // 0xda
  INSTRUCTION_UNDEFINED +   1, // 0xdb
  INSTRUCTION_UNDEFINED +   1, // 0xdc
  INSTRUCTION_UNDEFINED +   1, // 0xdd
  INSTRUCTION_UNDEFINED +   1, // 0xde
  INSTRUCTION_UNDEFINED +   1, // 0xdf
  INSTRUCTION_UNDEFINED +   1, // 0xe0
  INSTRUCTION_UNDEFINED +   1, // 0xe1
  INSTRUCTION_UNDEFINED +   1, // 0xe2
  INSTRUCTION_UNDEFINED +   1, // 0xe3
  INSTRUCTION_UNDEFINED +   1, // 0xe4
  INSTRUCTION_UNDEFINED +   1, // 0xe5
  INSTRUCTION_UNDEFINED +   1, // 0xe6
  INSTRUCTION_UNDEFINED +   1, // 0xe7
  INSTRUCTION_UNDEFINED +   1, // 0xe8
  INSTRUCTION_UNDEFINED +   1, // 0xe9
  INSTRUCTION_UNDEFINED +   1, // 0xea
  INSTRUCTION_UNDEFINED +   1, // 0xeb
  INSTRUCTION_UNDEFINED +   1, // 0xec
  INSTRUCTION_UNDEFINED +   1, // 0xed
  INSTRUCTION_UNDEFINED +   1, // 0xee
  INSTRUCTION_UNDEFINED +   1, // 0xef
  INSTRUCTION_UNDEFINED +   1, // 0xf0
  INSTRUCTION_UNDEFINED +   1, // 0xf1
  INSTRUCTION_UNDEFINED +   1, // 0xf2
  INSTRUCTION_UNDEFINED +   1, // 0xf3
  INSTRUCTION_UNDEFINED +   1, // 0xf4
  INSTRUCTION_UNDEFINED +   1, // 0xf5
  INSTRUCTION_UNDEFINED +   1, // 0xf6
  INSTRUCTION_UNDEFINED +   1, // 0xf7
  INSTRUCTION_UNDEFINED +   1, // 0xf8
  INSTRUCTION_UNDEFINED +   1, // 0xf9
  INSTRUCTION_UNDEFINED +   1, // 0xfa
  INSTRUCTION_UNDEFINED +   1, // 0xfb
  INSTRUCTION_UNDEFINED +   1, // 0xfc
  INSTRUCTION_UNDEFINED +   1, // 0xfd
  INSTRUCTION_UNDEFINED +   1, // 0xfe
  INSTRUCTION_UNDEFINED +   1, // 0xff
};

#define instruction_length(opc) (instruction_length_and_features[opc] & 0xf)
#define instruction_can_be_wide(opc) (instruction_length_and_features[opc] & INSTRUCTION_CAN_BE_WIDE)
#define instruction_uses_local_var(opc) (instruction_length_and_features[opc] & INSTRUCTION_USES_VAR)
#define instruction_uses_local_var_0(opc) ((instruction_length_and_features[opc] & INSTRUCTION_USES_VAR) == INSTRUCTION_USES_VAR_0)
#define instruction_uses_local_var_1(opc) ((instruction_length_and_features[opc] & INSTRUCTION_USES_VAR) == INSTRUCTION_USES_VAR_1)
#define instruction_uses_local_var_2(opc) ((instruction_length_and_features[opc] & INSTRUCTION_USES_VAR) == INSTRUCTION_USES_VAR_2)
#define instruction_uses_local_var_3(opc) ((instruction_length_and_features[opc] & INSTRUCTION_USES_VAR) == INSTRUCTION_USES_VAR_3)
#define instruction_uses_local_var_4(opc) ((instruction_length_and_features[opc] & INSTRUCTION_USES_VAR) == INSTRUCTION_USES_VAR_4)
#define instruction_undefined(opc) (instruction_length_and_features[opc] & INSTRUCTION_UNDEFINED)

/*
 * Free the memory of a basic block.
 */
void
freeBlock(v_BasicBlock* binfo)
{
  if (binfo == NULL) return;

  if (binfo->locals != NULL) {
    releaseMem(binfo->locals);
  }
  if (binfo->opstack != NULL) {
    releaseMem(binfo->opstack);
  }
	
  releaseMem(binfo);
}

/*
 * Throw a VerifyError and exit.
 */
#define VERIFY_ERROR(cstr) error_cstring = (cstr); goto verify_error

/*
 * Ensure the index given for a local variable is within the method's locals
 */
#define CHECK_LOCAL_INDEX(_N) \
  woempa(1, "  local variable slot == %d\n", _N); \
  if ((_N) >= method->exec.local_i) { \
    woempa(9, "Method %m has %d local variable slots, instruction adresses slot[%d]!\n", method, method->exec.local_i, (_N)); \
    VERIFY_ERROR("attempting to access a local variable beyond local array");  \
  }

/*
 * Ensure the index given for a constant is within the constant pool
 */
#define CHECK_POOL_IDX(_N) \
  woempa(1, "  constant pool index == %d\n", _N); \
  if ((_N) >= declaring_clazz->numConstants) { \
    woempa(9, "Class %k has %d constant slots, instruction addresses slot[%d]!\n", declaring_clazz, declaring_clazz->numConstants, (_N)); \
    VERIFY_ERROR("constant index out of bounds");  \
  }

/*
 * Ensure the tination of a branch instruction is within the bytecode of the method
 */
#define BRANCH_IN_BOUNDS(_N) \
  woempa(1, "  branch destination == %d\n", _N); \
  if (_N < 0 || _N >= codelen) { \
    woempa(9, "Method %m has code length of %d, instruction addresses pc %d!\n", method, method->exec.code_length, (_N)); \
    VERIFY_ERROR("branch out of method code"); \
  }

/*
** Get the dimensionality of a class constant; the constant may or may not
** be unresolved, but it must be a class constant. Returns:
**   -1   if an error occurred resolving the constant
**    0   if the constant identifies a non-array class
** #dims  if the constant identifies an array class
** Note: #dims can be > 255, the caller should check this.
*/
w_int getClassConstantDims(w_clazz clazz, w_int idx) {
  w_int n;
  x_status status;
  w_string sig;

// TODO: do we already hold this monitor? would be nice ...
  x_monitor_eternal(clazz->resolution_monitor);
  while (clazz->tags[idx] == RESOLVING_CLASS) {
    status = x_monitor_wait(clazz->resolution_monitor, 2);
    if (status == xs_interrupted) {
      x_monitor_eternal(clazz->resolution_monitor);
    }
  }

  switch (clazz->tags[idx]) {
  case CONSTANT_CLASS:
    sig = resolveUtf8Constant(clazz, clazz->values[idx]);
    for(n = 0; n <= 255 && string_char(sig, n) == '['; n++);
    deregisterString(sig);
    break;

  case RESOLVED_CLASS:
    n = getResolvedClassConstant(clazz, idx)->dims;
    break;

  case COULD_NOT_RESOLVE:
  default:
    n = -1;
  }
  x_monitor_exit(clazz->resolution_monitor);

  return n;
}

/*
 * Allocate memory for a block info and fill in with default values
 */
static v_BasicBlock* createBlock(w_method method) {
  w_int i;
  v_BasicBlock* binfo = allocMem(sizeof(v_BasicBlock));

  if (!binfo) {
    wabort(ABORT_WONKA, "Unable to allocate memory for Basic Block");
  }

  binfo->startAddr = 0;
  binfo->status = IS_INSTRUCTION | START_BLOCK;  /* not VISITED or CHANGED */

  /* allocate memory for locals */
  // TODO - merge with main allocation?
  if (method->exec.local_i > 0) {
    binfo->locals = allocMem(method->exec.local_i * sizeof(v_Type));
    if (!binfo->locals) {
      wabort(ABORT_WONKA, "Unable to allocate memory for local vars");
    }

    for (i = 0; i < method->exec.local_i; i++) {
      // TODO: binfo->locals[i] = *TUNSTABLE;
    }
  } else {
    binfo->locals = NULL;
  }

  /* allocate memory for operand stack */
  // TODO - merge with main allocation?
  binfo->stacksz = 0;
  if (method->exec.stack_i > 0) {
    binfo->opstack = allocMem(method->exec.stack_i * sizeof(v_Type));
    if (!binfo->opstack) {
      wabort(ABORT_WONKA, "Unable to allocate memory for local vars");
    }

    for (i = 0; i < method->exec.stack_i; i++) {
      // TODO: binfo->opstack[i] = *TUNSTABLE;
    }
  } else {
    binfo->opstack = NULL;
  }

  return binfo;
}

/*
** Get the method name from a Method constant, without resolving the constant
** if it is not already resolved. The resulting w_string is registered, so 
** remember to deregister it afterwards.
*/
static w_string getMethodConstantName(w_clazz clazz, w_int idx) {
  w_string result = NULL;

  if (clazz->tags[idx] == RESOLVED_METHOD) {
    result = getMethodConstant(clazz, idx)->spec.name;
  }
  else {
    x_monitor_eternal(clazz->resolution_monitor);
    while (clazz->tags[idx] == RESOLVING_METHOD) {
      if (x_monitor_wait(clazz->resolution_monitor, 2) == xs_interrupted) {
        x_monitor_eternal(clazz->resolution_monitor);
      }
    }

    if (clazz->tags[idx] == CONSTANT_METHOD) {
      w_word member = clazz->values[idx];
      w_int nat = clazz->values[Member_get_nat_index(member)];

      result = resolveUtf8Constant(clazz, Name_and_Type_get_name_index(nat));
    }
    else if (clazz->tags[idx] == RESOLVED_METHOD) {
      result = registerString(getMethodConstant(clazz, idx)->spec.name);
    }
    // else we return NULL (e.g. COULD_NOT_RESOLVE)
    x_monitor_exit(clazz->resolution_monitor);
  }

  return result;
}

/*
** Get the method name from an IMethod constant, without resolving the constant
** if it is not already resolved. The resulting w_string is registered, so 
** remember to deregister it afterwards.
*/
static w_string getIMethodConstantName(w_clazz clazz, w_int idx) {
  w_string result = NULL;

  if (clazz->tags[idx] == RESOLVED_IMETHOD) {
    result = getMethodConstant(clazz, idx)->spec.name;
  }
  else {
    x_monitor_eternal(clazz->resolution_monitor);
    while (clazz->tags[idx] == RESOLVING_IMETHOD) {
      if (x_monitor_wait(clazz->resolution_monitor, 2) == xs_interrupted) {
        x_monitor_eternal(clazz->resolution_monitor);
      }
    }

    if (clazz->tags[idx] == CONSTANT_IMETHOD) {
      w_word member = clazz->values[idx];
      w_int nat = clazz->values[Member_get_nat_index(member)];

      result = resolveUtf8Constant(clazz, Name_and_Type_get_name_index(nat));
    }
    else if (clazz->tags[idx] == RESOLVED_IMETHOD) {
      result = registerString(getMethodConstant(clazz, idx)->spec.name);
    }
    // else we return NULL (e.g. COULD_NOT_RESOLVE)
    x_monitor_exit(clazz->resolution_monitor);
  }

  return result;
}


/*
** Identify instruction and basic block boundaries; also check that 'wide'
** always prefixes a valid instruction. For instruction which have a constant
** pool index as operand, we also check that the constant has the correct
** type and (for multi/a/new/array) the correct dimensionality.
**
** The status_array supplied by the caller must contain one word for every
** byte of code in the method; it must be preset to all 0's.
** If no errors are found, TRUE is returned, and each word of status_array
** is set accordingly: see the definitions of START_BLOCK, END_BLOCK,
** IS_INSTRUCTION, WIDE_MODDED.
** If errors are found, an exception is raised and FALSE is returned.
*/
static w_boolean identifyBoundaries(w_method method, w_word *status_array) {
  w_size codelen = method->exec.code_length;
  w_code code = method->exec.code;
  w_boolean is_wide;
  w_size pc = 0;
  w_size newpc = 0;
  w_size idx = 0;
  w_size n = 0;
  w_int branchoffset;
  w_ubyte opcode;
  w_ubyte ndims;
  w_int low;
  w_int high;
  w_thread thread = currentWonkaThread;
  w_clazz declaring_clazz = method->spec.declaring_clazz;
  char *error_cstring;
  w_string method_name;

  woempa(7, "Identifying instruction and basic block boundaries in %M, codelen = %d\n", method, codelen);
  status_array[0] |= START_BLOCK;
  is_wide = FALSE;
  pc = 0;
  while (pc < codelen) {
    status_array[pc] |= IS_INSTRUCTION;
    opcode = code[pc];
    woempa(1, "instruction at pc[%d] is %s%s\n", pc, is_wide ? "wide " : "", opc2name(opcode));
  	
    if (codelen - pc < instruction_length(opcode)) {
      VERIFY_ERROR("instruction truncated");
    }
  	
    if (instruction_undefined(opcode)) {
      VERIFY_ERROR("illegal instruction");
    }

    if (is_wide && !instruction_can_be_wide(opcode)) {
      VERIFY_ERROR("illegal instruction following wide instruction");
    }

    if (instruction_uses_local_var(opcode)) {
#ifdef DEBUG
      if (instruction_uses_local_var_0(opcode)) {
        woempa(1, "opcode uses local var 0, method has %d local vars\n", method->exec.local_i);
      }
      if (instruction_uses_local_var_1(opcode)) {
        woempa(1, "opcode uses local var 1, method has %d local vars\n", method->exec.local_i);
      }
      if (instruction_uses_local_var_2(opcode)) {
        woempa(1, "opcode uses local var 3, method has %d local vars\n", method->exec.local_i);
      }
      if (instruction_uses_local_var_3(opcode)) {
        woempa(1, "opcode uses local var 3, method has %d local vars\n", method->exec.local_i);
      }
      if (instruction_uses_local_var_4(opcode)) {
        woempa(1, "opcode uses local var 4, method has %d local vars\n", method->exec.local_i);
      }
#endif

      if ((instruction_uses_local_var_0(opcode) && method->exec.local_i == 0)
       || (instruction_uses_local_var_1(opcode) && method->exec.local_i <= 1)
       || (instruction_uses_local_var_2(opcode) && method->exec.local_i <= 2)
       || (instruction_uses_local_var_3(opcode) && method->exec.local_i <= 3)
       || (instruction_uses_local_var_4(opcode) && method->exec.local_i <= 4)
      ) {
        VERIFY_ERROR("local variable index out of range");
      }
    }

    /*
    ** Check instruction length and wideness, and that operand is within range.
    */
    switch(opcode) {
    case ldc:
      idx = code[pc + 1];
      CHECK_POOL_IDX(idx);
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
      CHECK_POOL_IDX(idx);
      break;

    case invokeinterface:
      idx = (code[pc + 1] << 8) | code[pc + 2];
      CHECK_POOL_IDX(idx);
      if (code[pc + 3] == 0) {
        VERIFY_ERROR("fourth byte of invokeinterface is zero");
        // TODO: check == num params expected
      }
      else if (code[pc + 4] != 0) {
        VERIFY_ERROR("fifth byte of invokeinterface is not zero");
      }
      break;

    case newarray:
      n = code[pc + 1];
      woempa(1, "  type = %d\n", n);
      if (n < 4 || n > 11) {
        VERIFY_ERROR("newarray operand must be in the range [4,11]");
      }
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
        /* the WIDE is considered the beginning of the instruction */
        status_array[pc] ^= IS_INSTRUCTION;
        status_array[pc] |= WIDE_MODDED;
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
      /* the WIDE is considered the beginning of the instruction */
        status_array[pc] ^= IS_INSTRUCTION;
        status_array[pc] |= WIDE_MODDED;
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
        idx = (code[pc + 1] << 8) | code[pc + 2];

        status_array[pc] ^= IS_INSTRUCTION;
        status_array[pc] |= WIDE_MODDED;

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
        w_int lastmatch = 0x80000000;
        w_int npairs;
        w_size nextpc;

        status_array[pc] |= END_BLOCK;
		
        /* default branch...between 0 and 3 bytes of padding are added so that the
         * default branch is at an address that is divisible by 4
         */
        n = (pc + 1) % 4;
        n = n ? pc + 5 - n : pc + 1;
        branchoffset = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
        newpc = pc + branchoffset;
        BRANCH_IN_BOUNDS(newpc);
        status_array[newpc] |= START_BLOCK;
  
        n += 4;
        npairs = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
        if (npairs < 0) {
          VERIFY_ERROR("lookupswitch with npairs < 0");
        }
  
        n += 4;
        nextpc = n + 8 * npairs;
        BRANCH_IN_BOUNDS(nextpc);
        status_array[newpc] |= START_BLOCK;

        /* make sure match values are sorted and all targets are in bounds */
        while (n < nextpc) {
          w_int match = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];

          if (match <= lastmatch) {
            VERIFY_ERROR("lookupswitch match values out of order");
          }
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
      n = (pc + 1) % 4;
      n = n ? pc + 5 - n : pc + 1;
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
      if (high < low) {
        VERIFY_ERROR("tableswitch with high < low");
      }

      while (low <= high) {
        n += 4;
        branchoffset = (((((code[n] << 8) | code[n + 1]) << 8) | code[n + 2]) << 8) | code[n + 3];
        newpc = pc + branchoffset;
        woempa(1, "tableswitch: branch offset [%d] = %d, target = %d\n", low, branchoffset, newpc);
        BRANCH_IN_BOUNDS(newpc);
        status_array[newpc] |= START_BLOCK;
        ++low;
      }
      pc = n;
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

    /*
    ** For instructions which access a constant, check its type.
    */
    switch(opcode) {
    case ldc:
    case ldc_w:
      if (!isIntegerConstant(declaring_clazz, idx)
       && !isFloatConstant(declaring_clazz, idx)
       && !isStringConstant(declaring_clazz, idx)
       && !isClassConstant(declaring_clazz, idx)) {
        VERIFY_ERROR("ldc* on constant pool entry other than int/float/class/string");
      }
      break;
      
    case ldc2_w:
      if (!isLongConstant(declaring_clazz, idx)
       && !isDoubleConstant(declaring_clazz, idx)) {
        VERIFY_ERROR("ldc2_w on constant pool entry other than long or double");
      }
      break;
      
    case getfield:
    case putfield:
    case getstatic: 
    case putstatic:
      if (!isFieldConstant(declaring_clazz, idx)) {
        VERIFY_ERROR("[get/put][field/static] accesses something in the constant pool that is not a CONSTANT_Fieldref");
      }
      break;
      
    case invokevirtual:
    case invokestatic:
    case invokespecial:
      if (!isMethodConstant(declaring_clazz, idx)) {
        VERIFY_ERROR("invoke* accesses something in the constant pool that is not a CONSTANT_Methodref");
      }
      method_name = NULL;
      getMemberConstantStrings(declaring_clazz, idx, NULL, &method_name, NULL);
      woempa(1, "Call to method %w\n", method_name);
      break;
      
    case invokeinterface:
      if (!isIMethodConstant(declaring_clazz, idx)) {
        VERIFY_ERROR("invokeinterface accesses something in the constant pool that is not a CONSTANT_InterfaceMethodref");
      }
      method_name = NULL;
      getMemberConstantStrings(declaring_clazz, idx, NULL, &method_name, NULL);
      woempa(1, "Call to interface method %w\n", method_name);
      break;

    case instanceof:
    case checkcast:
      if (!isClassConstant(declaring_clazz, idx)) {
        VERIFY_ERROR("instanceof/checkcast indexes a constant pool entry that is not type CONSTANT_Class or CONSTANT_ResolvedClass");
      }
      break;
      
    case new:
      if (!isClassConstant(declaring_clazz, idx)) {
        VERIFY_ERROR("new indexes a constant pool entry that is not type CONSTANT_Class or CONSTANT_ResolvedClass");
      }

      n = getClassConstantDims(declaring_clazz, idx);
      if (n < 0) {
        // TODO: throw something else for COULD_NOT_RESOLVE
        VERIFY_ERROR("new indexes a bad constant pool entry");
      }
      else if (n) {
        VERIFY_ERROR("new instruction used to create an array");
      }
      break;
 
    case anewarray:
      if (!isClassConstant(declaring_clazz, idx)) {
        VERIFY_ERROR("anewarray indexes a constant pool entry that is not type CONSTANT_Class or CONSTANT_ResolvedClass");
      }
      break;
       
    case multianewarray:
      if (!isClassConstant(declaring_clazz, idx)) {
        VERIFY_ERROR("multianewarray indexes a constant pool entry that is not type CONSTANT_Class or CONSTANT_ResolvedClass");
      }

      /* number of dimensions must be <= num dimensions of array type being created */
      n = getClassConstantDims(declaring_clazz, idx);
      if (n < 0) {
        // TODO: throw something else for COULD_NOT_RESOLVE
        VERIFY_ERROR("multianewarray indexes a bad constant pool entry");
      }

      ndims = code[pc + 3];
      if (ndims == 0) {
        VERIFY_ERROR("dimensions operand of multianewarray must be non-zero");
      }
      if (n < ndims) {
        VERIFY_ERROR("dimensions operand of multianewarray is > the number of dimensions in array being created");
      }
      break;
    }
      
    /*
    ** For invoke instructions, check the name of method being called.
    */
    switch(opcode) {
    case invokevirtual:
    case invokestatic:
      {
        if (!method_name) {
          // We tried to resolve this constant and failed.
          // Let it pass for now; the invoke<foo> will not succeed anyway.
          break;
        }
        if (method_name == string_angle_brackets_init) {
          VERIFY_ERROR("only invokespecial can be used to execute <init> methods");
        }
        else if (string_char(method_name, 0) == '<') {
          VERIFY_ERROR("no method with a name whose first character is '<' may be called by an invoke instruction");
        }
      }
      deregisterString(method_name);
      break;

    case invokespecial:
      {
        if (!method_name) {
          // We tried to resolve this constant and failed.
          // Let it pass for now; the invokespecial will not succeed anyway.
          break;
        }
        if (string_char(method_name, 0) == '<' && method_name != string_angle_brackets_init) {
          VERIFY_ERROR("no method (other than <init>) with a name whose first character is '<' may be called by an invokespecial instruction");
        }
      }
      deregisterString(method_name);
      break;

    case invokeinterface:
      {
        if (!method_name) {
          // We tried to resolve this constant and failed.
          // Let it pass for now; the invokeinterface will not succeed anyway.
          break;
        }
        if (string_char(method_name, 0) == '<') {
          VERIFY_ERROR("invokeinterface cannot be used to invoke any method with a name starting with '<'");
        }
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

    woempa(7, "Parsing exception table\n");
    for (n = 0; n < method->exec.numExceptions; ++n, ++ex) {
      woempa(7, "Exception[%d]: start_pc = %d, end_pc = %d, handler_pc = %d, type_index = %d\n", n, ex->start_pc, ex->end_pc, ex->handler_pc, ex->type_index);
      pc = ex->handler_pc;
      if (pc >= codelen) {
        VERIFY_ERROR("exception handler is beyond bound of method code");
      }
      else if (!(status_array[pc] & IS_INSTRUCTION)) {
        VERIFY_ERROR("exception handler starts in the middle of an instruction");
      }
			
      status_array[pc] |= (EXCEPTION_HANDLER | START_BLOCK);

      /*
       * if ex->type_index == 0, it's a finally clause - else check is Throwable
    ----  check postponed to avoid deadlocks when resolving class constant ---
      if (ex->type_index) {
        w_clazz catch_clazz = getClassConstant(declaring_clazz, ex->type_index);

        woempa(7, "Catches class %K\n", catch_clazz);
        if (!catch_clazz) {
          VERIFY_ERROR("unresolvable catch type");
        }
        if (!isSuperClass(clazzThrowable, catch_clazz)) {
          VERIFY_ERROR("Exception to be handled by exception handler is not a subclass of Java/Lang/Throwable");
        }
      }
      else {
        woempa(7, "Catches everything\n");
      }
       */
    }
  }

  return TRUE;

verify_error:
  throwException(thread, clazzVerifyError, "%k.%m.%d: %s", declaring_clazz, method, pc, error_cstring);

  return FALSE;
}
#undef CHECK_LOCAL_INDEX	
#undef CHECK_POOL_IDX
#undef BRANCH_IN_BOUNDS

/*
** Given a method and the status_array produced by identifyBoundaries(),
** identifyBasicBlocks() builds a list of v_BasicBlock elements. The array
** is terminated by a NULL pointer, and the number of basic blocks found is 
** also recorded in numBlocks*.
*/
static v_BasicBlock** identifyBasicBlocks(w_method method, w_word *status_array, w_int *numBlocks) {
  w_size codelen = method->exec.code_length;
  w_boolean in_a_block;
  w_size pc;
  w_size previous_pc;
  w_int block_count;
  v_BasicBlock** blocks;
  char *error_cstring;

  block_count = 0;
  pc = 0;
  previous_pc = -1;
  in_a_block = FALSE;

  /*
  ** First pass; check consistency and count the basic blocks.
  */
  woempa(7, "%M pass 1\n", method);
  for (pc = 0; pc < codelen; ++pc) {
#ifdef DEBUG
    if (isSet(status_array[pc], IS_INSTRUCTION)) {
      woempa(1, "pc %d is an instruction, in_a_block = %s\n", pc, in_a_block ? "true" : "false");
    }
    else {
      woempa(1, "pc %d is not an instruction\n", pc);
    }
#endif

    if (isNotSet(status_array[pc], IS_INSTRUCTION)) {
      if (isSet(status_array[pc], START_BLOCK | END_BLOCK)) {
        VERIFY_ERROR("branch into middle of instruction");
      }
      else {

        continue;

      }
    }

    if (in_a_block) {
      if (isSet(status_array[pc], START_BLOCK)) {
        woempa(1, "pc %d is start of block: already in block, but start a new one anyhow\n", pc);
        woempa(1, "=> pc %d was end of block\n", previous_pc);
        setFlag(status_array[previous_pc], END_BLOCK);
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
        VERIFY_ERROR("End of block, but we weren't in one anyway?\n");
      }
    }

    previous_pc = pc;
  }

  *numBlocks = block_count;
  woempa(7, "Found %d basic blocks\n", *numBlocks);

  /*
  ** Second pass; allocate v_BasicBlock elements and fill them in.
  */
  woempa(7, "%M pass 2\n", method);
  blocks = allocMem((*numBlocks + 1) * sizeof(v_BasicBlock*));
  in_a_block = FALSE;
  block_count = 0;

  for (pc = 0; pc < codelen; ++pc) {
    if (isNotSet(status_array[pc], IS_INSTRUCTION)) {
      continue;
    }

    if (isSet(status_array[pc], START_BLOCK)) {
      woempa(1, "Basic block [%d] starts at pc %d\n", block_count, pc);
      blocks[block_count] = createBlock(method);
      blocks[block_count]->startAddr = pc;
    }
    if (isSet(status_array[pc], END_BLOCK)) {
      woempa(1, "Basic block [%d] ends at pc %d\n", block_count, pc);
      blocks[block_count]->lastAddr = pc;
      ++block_count;
    }
  }
  blocks[block_count] = NULL;

#ifdef RUNTIME_CHECKS
  if (block_count != *numBlocks) {
    wabort(ABORT_WONKA, "Darn, first I had %d basic blocks and now I have %d", *numBlocks, block_count);
  }
#endif

  return blocks;

verify_error:
  throwException(currentWonkaThread, clazzVerifyError, "%k.%m.%d: %s", method->spec.declaring_clazz, method, pc, error_cstring);

  return NULL;
}

/*
** TEST CODE
** Currently just calls identifyBoundaries() and identifyBasicBlocks() and prints out the result.
*/
w_boolean verifyMethod(w_method method) {
  w_word *status_array = allocClearedMem(method->exec.code_length * sizeof(w_word));
  w_int numBlocks;
  w_boolean result = identifyBoundaries(method, status_array);

  if (result) {
    v_BasicBlock** blocks = identifyBasicBlocks(method, status_array, &numBlocks);
  }
  releaseMem(status_array);

  return result;
}

//#define GONZALEZ

#ifdef GONZALEZ

/*
 * verifyMethod3a()
 *     check static constraints.  section 4.8.1 of JVML Spec 2.
 *
 * NOTE: we don't check whether execution can fall off the end of method code here as
 *       that would require us to know whether the last statements are reachable.
 *       Sun's verifier, for instance, rejects code with an unreachable NOP at the end!
 *       Thus we check whether execution can fall off the end during the data flow analysis
 *       of pass 3b, structural constraint checking.
 */
static v_BasicBlock** verifyMethod3a(w_method method, w_word* status_array, w_size* numBlocks)
{

  w_string sig;
  w_int codelen  = method->exec.code_length;
  w_code code = method->exec.code;
  w_size pc = 0;
  w_size newpc = 0;
  w_size n = 0;
  w_size idx = 0;
  w_int branchoffset;
  w_int low;
  w_int high;
  w_boolean is_wide;
  w_word blockCount  = 0;
  v_BasicBlock** blocks = NULL;
	
  woempa(7, "    Verifier Pass 3a: checking static constraints and finding basic blocks...\n");
	
	
  if (!identifyBoundaries(method, status_array, numBlocks)) {
    // TODO: scream and shout

    return NULL;
  }

  woempa(7, "    Verifier Pass 3a: second pass to locate illegal branches and count blocks...\n");
	
  blocks = allocMem((*numBlocks) * sizeof(v_BasicBlock*));
  if (!blocks) {
    // TODO: scream and shout

    return NULL;
  }

  for (inABlock = true, n = 0, pc = 0; pc < codelen; pc++) {
		if (status_array[pc] & START_BLOCK) {
			blocks[n] = createBlock(method);
			blocks[n]->startAddr = pc;
			n++;
			
			inABlock = true;
			
			
		if (inABlock && (status_array[pc] & END_BLOCK)) {
			blocks[n-1]->lastAddr = pc;
			
			inABlock = false;
			
			
	}
	
	
	
	*numBlocks = blockCount;
	return blocks;
	
	
#undef VERIFY_ERROR
}
/*
 * Controls the verification of a single method.  It allocates most of the memory needed for
 * verification (when encountering JSRs, more memory will need to be allocated later),
 * loads the initial arguments, calls pass3a, then calls pass3b and cleans up.
 */
static w_boolean verifyMethod(w_method method) {
  w_int codelen  = method->exec.code_length;
/* the status of each instruction...changed, visited, etc.
 * used primarily to help find the basic blocks initially
 */
  w_word *status_array = NULL; 
/* Rob uses this to remember what memory he allocated and needs to clean up.
  SigStack* sigs = NULL;
*/

  v_UninitializedType* uninits = NULL;

  w_size numBlocks = 0;
  v_BasicBlock** blocks    = NULL;


  /****************************************************************************
   * Memory Management Macros
   ****************************************************************************/
  /* to make sure we don't forget to unalloc anything...
   * should be called during ANY EXIT FROM THIS METHOD
   */
#define CLEANUP \
  woempa(7, "    cleaning up..."); \
  releaseMem(status_array); \
  if (blocks) { \
    while (numBlocks > 0) { \
      freeBlock(blocks[--numBlocks]); \
    } \
    releaseMem(blocks); \
  } \
  freeSigStack(sigs); \
  freeUninits(uninits); \
  woempa(7, " done\n");
	
#define FAIL \
  woempa(7, "    Verify Method 3b: %k.%m: FAILED\n", method->declaring_class, method); \
  if (!exceptionThrown(currentWonkaThread)) { \
    woempa(9, "      should have raised an exception\n"); \
    throwException(currentWonkaThread, clazzVerifyError, NULL);
  } \
  CLEANUP; \
  return(false)
	
	
  /***************************************************************************
   * Memory Allocation
   ***************************************************************************/
  woempa(7, "        allocating memory for verification (codelen = %d)...\n", codelen);

  status_array = checkPtr((char*)gc_malloc(codelen * sizeof(w_word), GC_ALLOC_VERIFIER));
  if (!status_array) {
    FAIL;
  }
	
  /* find basic blocks and allocate memory for them */
  blocks = verifyMethod3a(einfo, method, status_array, &numBlocks);
  if (!blocks) {
    woempa(9, "Failed to find the basic blocks in pass 3a\n");

    /* propagate error */
    FAIL;
  }
	
  woempa(7, "        done allocating memory\n"); );
  /***************************************************************************
   * Prepare for data-flow analysis
   ***************************************************************************/
	
  /* load initial arguments into local variable array */
  woempa(7, "    about to load initial args...\n");
  if (!loadInitialArgs(method, einfo, blocks[0], &sigs, &uninits)) {
     /* propagate error */
    FAIL;
  }
#ifdef DEBUG
  /* print out the local arguments */
  {
    w_int n;
    for(n = 0; n < method->localsz; n++) {
      woempa(7, "        local %d: \n", n);
      printType(&blocks[0]->locals[n]);
    }
  }
#endif
	
  if (!verifyMethod3b(einfo, method, status_array, blocks, numBlocks, &sigs, &uninits)) {
    FAIL;
  }

  CLEANUP;
  woempa(7, "    Verify Method 3b: done\n"); );

  return TRUE;

#undef FAIL
#undef CLEANUP
}




/*
 * verifyMethod3b()
 *    The Data-flow Analyzer
 *
 * The data-flow algorithm is taken from the JVM 2 spec, which describes it more or less as follows:
 *
 *  0  data-flow analyzer is initialised
 *       - for the first instruction of the method, the local variables that represent parameters
 *         initially contain values of the types indicated by the method's type descriptor.
 *       - the operand stack is empty.
 *       - all local variables contain an illegal value.
 *       - for the other instructions, which have not been examined yet, no information is available
 *         regarding the operand stack or local variables.
 *       - the "changed" bit is only set for the first instruction.
 *
 *  1  select a VM instruction whose "changed" bit is set
 *
 *       - if no such instruction remains, the method has successfully been verified.
 *       - otherwise, turn off the "changed" bit of the selected instruction.
 *
 *  2  model the effect of the instruction on the operand stack and local variable array by:
 *
 *       - if the instruction uses values from the operand stack, ensure that there are a
 *         sufficient number of values on the stack and that the top values on the stack are
 *         of an appropriate type.
 *       - if the instruction uses a local variable, ensure that the specified local variable
 *         contains a value of the appropriate type.
 *       - if the instruction pushes values onto the operand stack, ensure that there is sufficient
 *         room on the operand stack for the new values.  add the indicated types to the type of the
 *         modeled operand stack.
 *       - if the instruction modifies a local variable, record that the local variable now contains
 *         a type.
 *
 *  3  determine the instructions that can follow the current instruction.  successor instructions
 *     can be one of the following:
 *
 *       - the next instruction, if the current instruction is not an unconditional control tranfer
 *         instruction (ie - goto, return, or athrow).  basically check to make sure you don't
 *         "fall off" the last instruction of the method.
 *       - the target of a conditional or unconditional branch or switch.
 *       - any exception handlers for this instruction.
 *
 *  4  merge the state of the operand stack and local variable array at the end of the execution of the
 *     current instruction into each of the successor instructions.
 *
 *     (see merge function below)
 *
 *  5  continue at step 1.
 */
static
w_boolean
verifyMethod3b(errorInfo* einfo, const Method* method,
	       const w_word* status_array,
	       v_BasicBlock** blocks, const w_word numBlocks,
	       SigStack** sigs,
	       v_UninitializedType** uninits)
{
	const w_word codelen      = METHOD_BYTECODE_LEN(method);
	const unsigned char* code = METHOD_BYTECODE_CODE(method);
	
	w_word curIndex;
	v_BasicBlock* curBlock;
	v_BasicBlock* nextBlock;
	
#define VERIFY_ERROR(_MSG) \
        releaseMem(curBlock); \
        if (einfo->type == 0) { \
        	postExceptionMessage(einfo, JAVA_LANG(VerifyError), \
				     "in method \"%s.%s\": %s", \
				     CLASS_CNAME(method->class), METHOD_NAMED(method), _MSG); \
	} \
	return(false)
	
	
	w_word pc = 0, newpc = 0, n = 0;
	w_int high = 0, low = 0;  /* for the switching instructions */
	
	
	
	curBlock = createBlock(method);
	
	
	blocks[0]->status |= CHANGED;
	curIndex = 0;
	while(curIndex < numBlocks) {
		
		if (!(blocks[curIndex]->status & CHANGED)) {
			curIndex++;
			continue;
		}
		
		blocks[curIndex]->status ^= CHANGED; /* unset CHANGED bit */
		blocks[curIndex]->status |= VISITED; /* make sure we've visited it...important for merging */
		copyBlockData(method, blocks[curIndex], curBlock);
		
		if (curBlock->status & EXCEPTION_HANDLER && curBlock->stacksz > 0) {
			VERIFY_ERROR("it's possible to reach an exception handler with a nonempty stack");
		}
		
		
		if (!verifyBasicBlock(einfo, method, curBlock, sigs, uninits)) {
			VERIFY_ERROR("failure to verify basic block");
		}
		
		
		
		
		/*
		 * merge this block's information into the next block
		 */
		pc = curBlock->lastAddr;
		if (code[pc] == WIDE && code[pc + instruction_length(code[pc])] == RET)
			pc += instruction_length(code[pc]);
		switch(code[pc])
			{
			case GOTO:
				newpc = pc + 1;
				newpc = pc + WORD(code, newpc);
				nextBlock = inWhichBlock(newpc, blocks, numBlocks);
				
				if (!merge(einfo, method, curBlock, nextBlock)) {
					VERIFY_ERROR("error merging operand stacks");
				}
				break;
				
			case GOTO_W:
				newpc = pc + 1;
				newpc = pc + DWORD(code, newpc);
				nextBlock = inWhichBlock(newpc, blocks, numBlocks);
				
				if (!merge(einfo, method, curBlock, nextBlock)) {
					VERIFY_ERROR("error merging operand stacks");
				}
				break;
					
			case JSR:
				newpc = pc + 1;
				newpc = pc + WORD(code, newpc);
				goto JSR_common;
			case JSR_W:
				newpc = pc + 1;
				newpc = pc + DWORD(code, newpc);
			JSR_common:
				nextBlock = inWhichBlock(newpc, blocks, numBlocks);
				
				if (!merge(einfo, method, curBlock, nextBlock)) {
					VERIFY_ERROR("jsr: error merging operand stacks");
				}
	
				/*
				 * args, we need to verify the RET block first ...
				 */
				for (;curIndex<numBlocks && blocks[curIndex]!=nextBlock; curIndex++);
				assert (curIndex < numBlocks);
				continue;
				
			case RET:
				if (status_array[pc] & WIDE_MODDED) {
					n = pc + 1;
					n = WORD(code, n);
				} else {
					n = code[pc + 1];
				}
				
				if (!IS_ADDRESS(&curBlock->locals[n])) {
					VERIFY_ERROR("ret instruction does not refer to a variable with type returnAddress");
				}
				
				newpc = curBlock->locals[n].tinfo;
				
				/* each instance of return address can only be used once */
				curBlock->locals[n] = *TUNSTABLE;
				
				nextBlock = inWhichBlock(newpc, blocks, numBlocks);
				if (!merge(einfo, method, curBlock, nextBlock)) {
					VERIFY_ERROR("error merging opstacks when returning from a subroutine");
				}

				/* 
				 * unmark this block as visited, so that the next
				 * entry is treated as a first time merge.
				 */
				blocks[curIndex]->status ^= VISITED;
				break;	
				
			case IF_ACMPEQ:  case IFNONNULL:
			case IF_ACMPNE:  case IFNULL:
			case IF_ICMPEQ:  case IFEQ:
			case IF_ICMPNE:	 case IFNE:
			case IF_ICMPGT:	 case IFGT:
			case IF_ICMPGE:	 case IFGE:
			case IF_ICMPLT:	 case IFLT:
			case IF_ICMPLE:	 case IFLE:
				newpc     = pc + 1;
				newpc     = pc + WORD(code, newpc);
				nextBlock = inWhichBlock(newpc, blocks, numBlocks);
				
				if (!merge(einfo, method, curBlock, nextBlock)) {
					VERIFY_ERROR("error merging operand stacks");
				}
				
				/* if the condition is false, then the next block is the one that will be executed */
				curIndex++;
				if (curIndex >= numBlocks) {
					VERIFY_ERROR("execution falls off the end of a basic block");
				}
				else if (!merge(einfo, method, curBlock, blocks[curIndex])) {
					VERIFY_ERROR("error merging operand stacks");
				}
				break;
				
				
			case LOOKUPSWITCH:
			        /* default branch...between 0 and 3 bytes of padding are added so that the
				 * default branch is at an address that is divisible by 4
				 */
				n = (pc + 1) % 4;
				if (n) n = pc + 5 - n;
				else   n = pc + 1;
				newpc = pc + DWORD(code, n);
				nextBlock = inWhichBlock(newpc, blocks, numBlocks);
				if (!merge(einfo, method, curBlock, nextBlock)) {
					VERIFY_ERROR("error merging into the default branch of a lookupswitch instruction");
				}
				
				/* get number of key/target pairs */
				n += 4;
				low = DWORD(code, n);
				
				/* branch into all targets */
				for (n += 4, high = n + 8*low; n < high; n += 8) {
					newpc = pc + DWORD(code, n+4);
					nextBlock = inWhichBlock(newpc, blocks, numBlocks);
					if (!merge(einfo, method, curBlock, nextBlock)) {
						VERIFY_ERROR("error merging into a branch of a lookupswitch instruction");
					}
				}
				
				break;
				
			case TABLESWITCH:
			        /* default branch...between 0 and 3 bytes of padding are added so that the
				 * default branch is at an address that is divisible by 4
				 */
				n = (pc + 1) % 4;
				if (n) n = pc + 5 - n;
				else   n = pc + 1;
				newpc = pc + DWORD(code, n);
				
				/* get the high and low values of the table */
				low  = DWORD(code, n + 4);
				high = DWORD(code, n + 8);
				
				n += 12;
				
				/* high and low are used as temps in this loop that checks
				 * the validity of all the branches in the table
				 */
				for (high = n + 4*(high - low + 1); n < high; n += 4) {
					newpc = pc + DWORD(code, n);
					nextBlock = inWhichBlock(newpc, blocks, numBlocks);
					if (!merge(einfo, method, curBlock, nextBlock)) {
						VERIFY_ERROR("error merging into a branch of a tableswitch instruction");
					}
				}
				break;
				
				
				/* the rest of the ways to end a block */
			case RETURN:
			case ARETURN:
			case IRETURN:
			case FRETURN:
			case LRETURN:
			case DRETURN:
			case ATHROW:
				curIndex++;
				continue;
				
			default:
				for (n = pc + 1; n < codelen; n++) {
					if (status_array[n] & IS_INSTRUCTION) break;
				}
				if (n == codelen) {
					VERIFY_ERROR("execution falls off the end of a code block");
				}
				else if (!merge(einfo, method, curBlock, blocks[curIndex+1])) {
					VERIFY_ERROR("error merging operand stacks");
				}
			}
		
		
		for (curIndex = 0; curIndex < numBlocks; curIndex++) {
			if (blocks[curIndex]->status & CHANGED)
				break;
		}
	}
	
	
	releaseMem(curBlock);
	return(true);
	
#undef VERIFY_ERROR
#undef RETURN_3B
}


/*
 * merges two operand stacks.  just to repeat what the JVML 2 spec says about this:
 *   Merge the state of the operand stack and local variable array at the end of the
 *   execution of the current instruction into each of the successor instructions.  In
 *   the special case of control transfer to an exception handler, the operand stack is
 *   set to contain a single object of the exception type indicated by the exception
 *   handler information.
 *     - if this if the first time the successor instruction has been visited, record
 *       that the operand stack and local variable values calculated in steps 2 and 3
 *       are the state of the operand stack and local variable array prior to executing
 *       the successor instruction.  Set the "changed" bit for the successor instruction.
 *     - if the successor instruction has been seen before, merge the operand stack and
 *       local variable values calculated in steps 2 and 3 into the values already there.
 *       set the "changed" bit if there is any modification to the values.
 *
 *   to merge two operand stacks, the number of values on each stack must be identical.
 *   the types of values on the stacks must also be identical, except that differently
 *   typed reference values may appear at corresponding places on the two stacks.  in this
 *   case, the merged operand stack contains a reference to an instance of the first common
 *   superclass of the two types.  such a reference type always exists because the type Object
 *   is a superclass of all class and interface types.  if the operand stacks cannot be merged,
 *   verification of the method fails.
 *
 *   to merge two local variable array states, corresponding pairs of local variables are
 *   compared.  if the two types are not identical, then unless both contain reference values,
 *   the verification records that the local variable contains an unusable value.  if both of
 *   the pair of local variables contain reference values, the merged state contains a reference
 *   to an instance of the first common superclass of the two types.
 */
static
w_boolean
merge(errorInfo* einfo,
      const Method* method,
      v_BasicBlock* fromBlock,
      v_BasicBlock* toBlock)
{
#define VERIFY_ERROR(_MSG) \
        if (einfo->type == 0) { \
        	postExceptionMessage(einfo, JAVA_LANG(VerifyError), \
				     "in method \"%s.%s\": %s", \
				     CLASS_CNAME(method->class), METHOD_NAMED(method), _MSG); \
	} \
	return(false)
	
	
	w_size n;
	
	
	/* Ensure that no uninitiazed object instances are in the local variable array
	 * or on the operand stack during a backwards branch
	 */
	if (toBlock->startAddr < fromBlock->startAddr) {
		for (n = 0; n < method->localsz; n++) {
			if (fromBlock->locals[n].tinfo & TINFO_UNINIT) {
				VERIFY_ERROR("uninitialized object reference in a local variable during a backwards branch");
			}
		}
		for (n = 0; n < fromBlock->stacksz; n++) {
			if (fromBlock->opstack[n].tinfo & TINFO_UNINIT) {
				VERIFY_ERROR("uninitialized object reference on operand stack during a backwards branch");
			}
		}
	}
	
	if (!(toBlock->status & VISITED)) {
		
		copyBlockState(method, fromBlock, toBlock);
		toBlock->status |= CHANGED;
		return(true);
	}
	
	
	if (fromBlock->stacksz != toBlock->stacksz) {
		postExceptionMessage(einfo, JAVA_LANG(VerifyError),
				     "in method %s.%s: merging two operand stacks of unequal size",
				     METHOD_NAMED(method), CLASS_CNAME(method->class));
		return(false);
	}
	
	
	/* merge the local variable arrays */
	for (n = 0; n < method->localsz; n++) {
		if (mergeTypes(einfo, method->class,
			       &fromBlock->locals[n], &toBlock->locals[n])) {
			toBlock->status |= CHANGED;
		}
	}
	
	/* merge the operand stacks */
	for (n = 0; n < fromBlock->stacksz; n++) {
	        /* if we get unstable here, not really a big deal until we try to use it.
		 * i mean, we could get an unstable value and then immediately pop it off the stack,
		 * for instance.
		 */
		if (mergeTypes(einfo, method->class,
			       &fromBlock->opstack[n], &toBlock->opstack[n])) {
			toBlock->status |= CHANGED;
		}
	}
	
	
#ifdef DEBUG
	    printBlock(method, toBlock, indent2);
#endif
	
	
	return(true);
#undef VERIFY_ERROR
}




/*
 * verifyBasicBlock()
 *   Simulates execution of a basic block by modifying its simulated operand stack and local variable array.
 */
static
w_boolean
verifyBasicBlock(errorInfo* einfo,
		 const Method* method,
		 v_BasicBlock* block,
		 SigStack** sigs,
		 v_UninitializedType** uninits)
{
	/**************************************************************************************************
	 * VARIABLES
	 **************************************************************************************************/
	w_word            pc   = 0;
	unsigned char*    code = METHOD_BYTECODE_CODE(method);
	Hjava_lang_Class* this = method->class;
	
	w_boolean is_wide = false;       /* was the previous opcode a WIDE instruction? */
	
	w_word n = 0;            /* used as a general temporary variable, often as a temporary pc */
	
	v_Type* type = NULL;
	v_Type* arrayType = NULL;
	Hjava_lang_Class* class; /* for when we need a pointer to an actual class */
	
	/* for the rare occasions when we actually need a Type */
	v_Type  tt;
	v_Type* t = &tt;
	
	int tag;                 /* used for constant tag stuff */
	
	w_word     idx;          /* index into constant pool */
	constants* pool = CLASS_CONSTANTS(method->class);
	
	const char* sig;
	
	
	/**************************************************************************************************
	 * HANDY MACROS USED ONLY IN THIS METHOD
	 *    most of these belong to one of two categories:
	 *         - those dealing with locals variables
	 *         - those dealing with the operand stack
	 **************************************************************************************************/
#define VERIFY_ERROR(_MSG) \
	if (einfo->type == 0) { \
		postExceptionMessage(einfo, JAVA_LANG(VerifyError), \
				     "in method \"%s.%s\": %s", \
				     CLASS_CNAME(this), METHOD_NAMED(method), _MSG); \
	} \
	return(false)

#define GET_IDX \
	idx = code[pc + 1]
	
#define GET_WIDX \
	idx = pc + 1; idx = WORD(code, idx)
	
	
	/* checks whether the specified local variable is of the specified type. */
#define ENSURE_LOCAL_TYPE(_N, _TINFO) \
	if (!typecheck(einfo, this, (_TINFO), &block->locals[_N])) { \
		if (block->locals[_N].data.class == TUNSTABLE->data.class) { \
			VERIFY_ERROR("attempt to access an unstable local variable"); \
		} else { \
			VERIFY_ERROR("attempt to access a local variable not of the correct type"); \
		} \
	} 
	
	/* only use with TLONG and TDOUBLE */
#define ENSURE_LOCAL_WTYPE(_N, _TINFO) \
	if (block->locals[_N].data.class != (_TINFO)->data.class) { \
		VERIFY_ERROR("local variable not of correct type"); \
	} \
	else if (block->locals[_N + 1].data.class != TWIDE->data.class) { \
		VERIFY_ERROR("accessing a long or double in a local where the following local has been corrupted"); \
	}

	
#define ENSURE_OPSTACK_SIZE(_N) \
	if (block->stacksz < (_N)) { \
		VERIFY_ERROR("not enough items on stack for operation"); \
	}

#define CHECK_STACK_OVERFLOW(_N) \
	if (block->stacksz + _N > method->stacksz) { \
		VERIFY_ERROR("stack overflow"); \
	}
	
	
	/* the nth item on the operand stack from the top */
#define OPSTACK_ITEM(_N) \
	(&block->opstack[block->stacksz - _N])
	
#define OPSTACK_TOP  OPSTACK_ITEM(1)
#define OPSTACK_WTOP OPSTACK_ITEM(2)

#define OPSTACK_INFO(_N) \
        (block->opstack[block->stacksz - _N].tinfo)

#define LOCALS_INFO(_N) \
	(block->locals[_N].tinfo)
	
	
	
#define OPSTACK_PUSH_BLIND(_TINFO) \
	block->opstack[block->stacksz++] = *(_TINFO)
	
#define OPSTACK_PUSH(_TINFO) \
	CHECK_STACK_OVERFLOW(1); \
	OPSTACK_PUSH_BLIND(_TINFO)
	
	
	/* only use for LONGs and DOUBLEs */
#define OPSTACK_WPUSH_BLIND(_TINFO) \
	OPSTACK_PUSH_BLIND(_TINFO); \
	OPSTACK_PUSH_BLIND(TWIDE)
	
#define OPSTACK_WPUSH(_T) \
	CHECK_STACK_OVERFLOW(2); \
        OPSTACK_WPUSH_BLIND(_T)
	
	
	
	/* ensure that the top item on the stack is of type _T	*/
#define OPSTACK_PEEK_T_BLIND(_TINFO) \
	if (!typecheck(einfo, this, _TINFO, OPSTACK_TOP)) { \
		VERIFY_ERROR("top of opstack does not have desired type"); \
	}
	
#define OPSTACK_PEEK_T(_TINFO) \
        ENSURE_OPSTACK_SIZE(1); \
	OPSTACK_PEEK_T_BLIND(_TINFO)
	
	/* ensure that the top item on the stack is of wide type _T
	 * this only works with doubles and longs
	 */
#define OPSTACK_WPEEK_T_BLIND(_TINFO) \
	if (OPSTACK_TOP->data.class != TWIDE->data.class) { \
		VERIFY_ERROR("trying to pop a wide value off operand stack where there is none"); \
	} else if (OPSTACK_WTOP->data.class != (_TINFO)->data.class) { \
		VERIFY_ERROR("mismatched stack types"); \
	}
	
#define OPSTACK_WPEEK_T(_TINFO) \
	ENSURE_OPSTACK_SIZE(2); \
	OPSTACK_WPEEK_T_BLIND(_TINFO)
	
	
	
#define OPSTACK_POP_BLIND \
	block->stacksz--; \
	block->opstack[block->stacksz] = *TUNSTABLE
	
#define OPSTACK_POP \
        ENSURE_OPSTACK_SIZE(1); \
	OPSTACK_POP_BLIND

	/* pop a type off the stack and typecheck it */
#define OPSTACK_POP_T_BLIND(_TINFO) \
	OPSTACK_PEEK_T_BLIND(_TINFO); \
	OPSTACK_POP_BLIND

#define OPSTACK_POP_T(_TINFO) \
	OPSTACK_PEEK_T(_TINFO); \
        OPSTACK_POP_BLIND



#define OPSTACK_WPOP_BLIND \
	OPSTACK_POP_BLIND; \
	OPSTACK_POP_BLIND

#define OPSTACK_WPOP \
	ENSURE_OPSTACK_SIZE(2); \
	OPSTACK_WPOP_BLIND

	/* pop a wide type off the stack and typecheck it */
#define OPSTACK_WPOP_T_BLIND(_TINFO) \
	OPSTACK_WPEEK_T_BLIND(_TINFO); \
	OPSTACK_WPOP_BLIND

#define OPSTACK_WPOP_T(_TINFO) \
        OPSTACK_WPEEK_T(_TINFO); \
	OPSTACK_WPOP_BLIND
        

	
	/* pop _N things off the stack off the stack */
#define OPSTACK_POP_N_BLIND(_N) \
	for (n = 0; n < _N; n++) { \
		OPSTACK_POP_BLIND; \
	}
	
#define OPSTACK_POP_N(_N) \
        ENSURE_OPSTACK_SIZE(_N); \
	OPSTACK_POP_N_BLIND(_N)
	
	
	
	/**************************************************************************************************
	 * BLOCK-LEVEL DATA FLOW ANALYASIS
	 *    this is actually pretty easy, since there are never any branches.  basically, it just
	 *    manipulates the working stack after every instruction as if it were actually running the
	 *    code so that, after verifying the block, the working block can be used to merge this block
	 *    with its successors.
	 **************************************************************************************************/
	
	pc = block->startAddr;
	while (pc <= block->lastAddr) {
		
		switch(code[pc]) {
			/**************************************************************
			 * INSTRUCTIONS FOR PUSHING CONSTANTS ONTO THE STACK
			 **************************************************************/
			/* pushes NULL onto the stack, which matches any object */
		case ACONST_NULL:
			OPSTACK_PUSH(TNULL);
			break;
			
			/* iconst_<n> pushes n onto the stack */
		case ICONST_0: case ICONST_1: case ICONST_2:
		case ICONST_3: case ICONST_4: case ICONST_5:
			
		case ICONST_M1: /* pushes -1 onto the stack */
		case BIPUSH:    /* sign extends an 8-bit int to 32-bits and pushes it onto stack */
		case SIPUSH:    /* sign extends a 16-bit int to 32-bits and pushes it onto stack */
			OPSTACK_PUSH(TINT);
			break;
			
		case FCONST_0:
		case FCONST_1:
		case FCONST_2:
			OPSTACK_PUSH(TFLOAT);
			break;
			
		case LCONST_0:
		case LCONST_1:
			OPSTACK_WPUSH(TLONG);
			break;
			
		case DCONST_0:
		case DCONST_1:
			OPSTACK_WPUSH(TDOUBLE);
			break;
			
			
		case LDC1:
			GET_IDX;
			goto LDC_common;
		case LDC2:
			GET_WIDX;
		LDC_common:
			tag = CONST_TAG(idx, pool);
			switch(tag) {
			case CONSTANT_Integer: OPSTACK_PUSH(TINT);    break;
			case CONSTANT_Float:   OPSTACK_PUSH(TFLOAT);  break;
			case CONSTANT_ResolvedString:
			case CONSTANT_String:
			        /* we do this because we might be loading a class before
				 * loading String
				 */
				OPSTACK_PUSH(TSTRING);
				break;
			}
			break;
			
		case LDC2W:
			GET_WIDX;
			tag = CONST_TAG(idx, pool);
			if (tag == CONSTANT_Long) {
				OPSTACK_WPUSH(TLONG);
			} else {
				OPSTACK_WPUSH(TDOUBLE);
			}
			break;
			
			
			/**************************************************************
			 * INSTRUCTIONS DEALING WITH THE LOCALS AND STACK
			 **************************************************************/
		case POP:
			OPSTACK_POP;
			break;
		case POP2:
			OPSTACK_WPOP;
			break;
			
			
#define GET_CONST_INDEX \
			if (is_wide == true) { GET_WIDX; } \
			else              { GET_IDX;  }
			
			
			/* aload_<n> takes the object reference in location <n> and pushes it onto the stack */
		case aload_0: idx = 0; goto aload_common;
		case aload_1: idx = 1; goto aload_common;
		case aload_2: idx = 2; goto aload_common;
		case aload_3: idx = 3; goto aload_common;
		case aload:
			GET_CONST_INDEX;
		aload_common:
			if (!isReference(&block->locals[idx])) {
				VERIFY_ERROR("aload<_n> where local variable does not contain an object reference");
			}
			
			OPSTACK_PUSH(&block->locals[idx]);
			break;
			
			
			/* stores whatever's on the top of the stack in local <n> */
		case astore_0: idx = 0; goto astore_common;
		case astore_1: idx = 1; goto astore_common;
		case astore_2: idx = 2; goto astore_common;
		case astore_3: idx = 3; goto astore_common;
		case astore:
			GET_CONST_INDEX;
		astore_common:
			ENSURE_OPSTACK_SIZE(1);
			type = OPSTACK_TOP;
			
			if (!IS_ADDRESS(type) && !isReference(type)) {
				VERIFY_ERROR("astore: top of stack is not a return address or reference type");
			}
			
			block->locals[idx] = *type;
			OPSTACK_POP_BLIND;
			break;
			
			
			
			/* iload_<n> takes the variable in location <n> and pushes it onto the stack */
		case iload_0: idx = 0; goto iload_common;
		case iload_1: idx = 1; goto iload_common;
		case iload_2: idx = 2; goto iload_common;
		case iload_3: idx = 3; goto iload_common;
		case iload:
			GET_CONST_INDEX;
		iload_common:
			ENSURE_LOCAL_TYPE(idx, TINT);
			OPSTACK_PUSH(TINT);
			break;
			
			
		case istore_0: idx =0; goto istore_common;
		case istore_1: idx =1; goto istore_common;
		case istore_2: idx =2; goto istore_common;
		case istore_3: idx =3; goto istore_common;
		case istore:
			GET_CONST_INDEX;
		istore_common:
			OPSTACK_POP_T(TINT);
			block->locals[idx] = *TINT;
			break;
			
			
			/* fload_<n> takes the variable at location <n> and pushes it onto the stack */
		case fload_0: idx =0; goto fload_common;
		case fload_1: idx =1; goto fload_common;
		case fload_2: idx =2; goto fload_common;
		case fload_3: idx = 3; goto fload_common;
		case fload:
			GET_CONST_INDEX;
		fload_common:
			ENSURE_LOCAL_TYPE(idx, TFLOAT);
			OPSTACK_PUSH(TFLOAT);
			break;
			
			
			/* stores a float from top of stack into local <n> */
		case fstore_0: idx = 0; goto fstore_common;
		case fstore_1: idx = 1; goto fstore_common;
		case fstore_2: idx = 2; goto fstore_common;
		case fstore_3: idx = 3; goto fstore_common;
		case fstore:
			GET_CONST_INDEX;
		fstore_common:
			OPSTACK_POP_T(TFLOAT);
			block->locals[idx] = *TFLOAT;
			break;
			
			
			/* lload_<n> takes the variable at location <n> and pushes it onto the stack */
		case lload_0: idx = 0; goto lload_common;
		case lload_1: idx = 1; goto lload_common;
		case lload_2: idx = 2; goto lload_common;
		case lload_3: idx = 3; goto lload_common;
		case lload:
			GET_CONST_INDEX;
		lload_common:
			ENSURE_LOCAL_WTYPE(idx, TLONG);
			OPSTACK_WPUSH(TLONG);
			break;
			
			
			/* lstore_<n> stores a long from top of stack into local <n> */
		case lstore_0: idx = 0; goto lstore_common;
		case lstore_1: idx = 1; goto lstore_common;
		case lstore_2: idx = 2; goto lstore_common;
		case lstore_3: idx = 3; goto lstore_common;
		case lstore:
			GET_CONST_INDEX;
		lstore_common:
			OPSTACK_WPOP_T(TLONG);
			block->locals[idx] = *TLONG;
			block->locals[idx + 1] = *TWIDE;
			break;
			
			
			/* dload_<n> takes the double at local <n> and pushes it onto the stack */
		case dload_0: idx = 0; goto dload_common;
		case dload_1: idx = 1; goto dload_common;
		case dload_2: idx = 2; goto dload_common;
		case dload_3: idx = 3; goto dload_common;
		case dload:
			GET_CONST_INDEX;
		dload_common:
			ENSURE_LOCAL_WTYPE(idx, TDOUBLE);
			OPSTACK_WPUSH(TDOUBLE);
			break;
			
			
			/* dstore stores a double from the top of stack into a local variable */
		case dstore_0: idx = 0; goto dstore_common;
		case dstore_1: idx = 1; goto dstore_common;
		case dstore_2: idx = 2; goto dstore_common;
		case dstore_3: idx = 3; goto dstore_common;
		case dstore:
			GET_CONST_INDEX;
		dstore_common:
			OPSTACK_WPOP_T(TDOUBLE);
			block->locals[idx] = *TDOUBLE;
			block->locals[idx + 1] = *TWIDE;
			break;
			
			
#undef GET_CONST_INDEX
			/**************************************************************
			 * ARRAY INSTRUCTIONS!
			 **************************************************************/
			/* i put ANEWARRAY code by NEW instead of in the array instructions
			 * section because of similarities with NEW
			
			 * for creating a primitive array
			 */
		case NEWARRAY:
		        OPSTACK_POP_T(TINT);   /* array size */
			
			switch(code[pc + 1]) {
			case TYPE_Boolean: OPSTACK_PUSH(TBOOLARR);   break;
			case TYPE_Char:    OPSTACK_PUSH(TCHARARR);   break;
			case TYPE_Float:   OPSTACK_PUSH(TFLOATARR);  break;
			case TYPE_Double:  OPSTACK_PUSH(TDOUBLEARR); break;
			case TYPE_Byte:    OPSTACK_PUSH(TBYTEARR);   break;
			case TYPE_Short:   OPSTACK_PUSH(TSHORTARR);  break;
			case TYPE_Int:     OPSTACK_PUSH(TINTARR);    break;
			case TYPE_Long:    OPSTACK_PUSH(TLONGARR);   break;
			default: VERIFY_ERROR("newarray of unknown type");
			}
			break;
			
		case ARRAYLENGTH:
			ENSURE_OPSTACK_SIZE(1);
			
			type = OPSTACK_TOP;
			if (!isArray(type)) {
				VERIFY_ERROR("arraylength: top of operand stack is not an array");
			}
			
			*type = *TINT;
			break;
			
			
#define ARRAY_LOAD(_T, _ARRT) \
                                OPSTACK_POP_T(TINT); \
                                OPSTACK_POP_T(_ARRT); \
				OPSTACK_PUSH(_T);

#define ARRAY_WLOAD(_T, _ARRT) \
                                OPSTACK_POP_T(TINT); \
                                OPSTACK_POP_T(_ARRT); \
				OPSTACK_WPUSH(_T);
			
			
		case AALOAD:
			ENSURE_OPSTACK_SIZE(2);
			
			if (OPSTACK_TOP->data.class != TINT->data.class) {
				VERIFY_ERROR("aaload: item on top of stack is not an integer");
			}
			OPSTACK_POP_BLIND;
			
			type = OPSTACK_TOP;
			if (!isArray(type)) {
				VERIFY_ERROR("aaload: top of operand stack is not an array");
			}
			
			if (type->tinfo & TINFO_NAME || type->tinfo & TINFO_SIG) {
				type->tinfo = TINFO_SIG;
				(type->data.sig)++;
			}
			else if (type->data.class != TNULL->data.class) {
				type->tinfo = TINFO_SIG;
				type->data.sig = CLASS_CNAME(type->data.class) + 1;
			}
			break;
			
		case IALOAD: ARRAY_LOAD(TINT,   TINTARR);   break;
		case FALOAD: ARRAY_LOAD(TFLOAT, TFLOATARR); break;
		case CALOAD: ARRAY_LOAD(TINT,   TCHARARR);  break;
		case SALOAD: ARRAY_LOAD(TINT,   TSHORTARR); break;
			
		case LALOAD: ARRAY_WLOAD(TLONG,   TLONGARR);   break;
		case DALOAD: ARRAY_WLOAD(TDOUBLE, TDOUBLEARR); break;
#undef ARRAY_LOAD
#undef ARRAY_WLOAD

		case BALOAD:
			/* BALOAD can be used for bytes or booleans .... */
			OPSTACK_POP_T(TINT);

			if (!typecheck (einfo, this, TBYTEARR, OPSTACK_TOP) &&
			    !typecheck (einfo, this, TBOOLARR, OPSTACK_TOP)) {
                                VERIFY_ERROR("top of opstack does not have desired type");
			}

			OPSTACK_POP_BLIND;
			OPSTACK_PUSH(TINT);
			break;


		case AASTORE:
		        /* the runtime value of the type on the top of the stack must be
			 * assignment compatible with the type of the array
			 */
			ENSURE_OPSTACK_SIZE(3);
			
			if (OPSTACK_ITEM(2)->data.class != TINT->data.class) {
				VERIFY_ERROR("aastore: array index is not an integer");
			}
			
			type      = OPSTACK_ITEM(1);
			arrayType = OPSTACK_ITEM(3);
			
			if (!isArray(arrayType)) {
				VERIFY_ERROR("aastore: top of operand stack is not an array");
			}
			
			if (arrayType->tinfo & TINFO_NAME || arrayType->tinfo & TINFO_SIG) {
				arrayType->tinfo = TINFO_SIG;
				(arrayType->data.sig)++;
			}
			else {
				if (arrayType->data.class == TOBJARR->data.class) {
					*arrayType = *TOBJ;
				} else if (arrayType->data.class != TNULL->data.class) {
					arrayType->tinfo = TINFO_SIG;
					arrayType->data.sig = CLASS_CNAME(arrayType->data.class) + 1;
				}
			}
			
			if (!typecheck(einfo, this, arrayType, type)) {
				VERIFY_ERROR("attempting to store incompatible type in array");
			}
			
			OPSTACK_POP_N_BLIND(3);
			break;

#define ARRAY_STORE(_T, _ARRT) \
				OPSTACK_POP_T(_T); \
				OPSTACK_POP_T(TINT); \
				OPSTACK_POP_T(_ARRT);
			
#define ARRAY_WSTORE(_T, _ARRT) \
				OPSTACK_WPOP_T(_T); \
				OPSTACK_POP_T(TINT); \
				OPSTACK_POP_T(_ARRT);
			
			
			
			
		case IASTORE: ARRAY_STORE(TINT,   TINTARR);   break;
		case FASTORE: ARRAY_STORE(TFLOAT, TFLOATARR); break;
		case CASTORE: ARRAY_STORE(TINT,   TCHARARR);  break;
		case SASTORE: ARRAY_STORE(TINT,   TSHORTARR); break;
			
		case LASTORE: ARRAY_WSTORE(TLONG,   TLONGARR);   break;
		case DASTORE: ARRAY_WSTORE(TDOUBLE, TDOUBLEARR); break;
#undef ARRAY_STORE
#undef ARRAY_WSTORE

		case BASTORE: 
			/* BASTORE can store either bytes or booleans .... */
			OPSTACK_POP_T(TINT);
			OPSTACK_POP_T(TINT);

			if ( !typecheck(einfo, this, TBYTEARR, OPSTACK_TOP) &&
			     !typecheck(einfo, this, TBOOLARR, OPSTACK_TOP)) {
				VERIFY_ERROR("top of opstack does not have desired type");
			}
			OPSTACK_POP_BLIND;
			break;			
			
			
			/**************************************************************
			 * ARITHMETIC INSTRUCTIONS
			 **************************************************************/
		case IAND: case IOR:  case IXOR:
		case IADD: case ISUB: case IMUL: case IDIV: case IREM:
		case ISHL: case ISHR: case IUSHR:
			OPSTACK_POP_T(TINT);
			break;
		case INEG:
			OPSTACK_PEEK_T(TINT);
			break;
			
			
		case LAND: case LOR:  case LXOR:
		case LADD: case LSUB: case LMUL: case LDIV: case LREM:
			OPSTACK_WPOP_T(TLONG);
			break;
		case LNEG:
			OPSTACK_WPEEK_T(TLONG);
			break;
			
		case LSHL: case LSHR: case LUSHR:
			OPSTACK_POP_T(TINT);
			OPSTACK_WPEEK_T(TLONG);
			break;
			
			
		case FADD: case FSUB: case FMUL: case FDIV: case FREM:
			OPSTACK_POP_T(TFLOAT);
			break;
		case FNEG:
			OPSTACK_PEEK_T(TFLOAT);
			break;
			
			
		case DADD: case DSUB: case DDIV: case DMUL: case DREM:
			OPSTACK_WPOP_T(TDOUBLE);
			break;
		case DNEG:
			OPSTACK_WPEEK_T(TDOUBLE);
			break;
			
			
		case LCMP:
			OPSTACK_WPOP_T(TLONG);
			OPSTACK_WPOP_T(TLONG);
			OPSTACK_PUSH_BLIND(TINT);
			break;
			
		case FCMPG:
		case FCMPL:
			OPSTACK_POP_T(TFLOAT);
			OPSTACK_POP_T(TFLOAT);
			OPSTACK_PUSH_BLIND(TINT);
			break;
				
		case DCMPG:
		case DCMPL:
			OPSTACK_WPOP_T(TDOUBLE);
			OPSTACK_WPOP_T(TDOUBLE);
			OPSTACK_PUSH_BLIND(TINT);
			break;
			
			
		case IINC:
			if (is_wide == true) { GET_WIDX; }
			else              { GET_IDX; }
			
			ENSURE_LOCAL_TYPE(idx, TINT);
			
			pc += instruction_length(code[pc]);
			if (is_wide == true) {
				pc += 2;
				is_wide = false;
			}
			continue;
			
			
			/**************************************************************
			 * PRIMITIVE CONVERSION STUFF
			 **************************************************************/
		case INT2BYTE:
		case INT2CHAR:
		case INT2SHORT:
			OPSTACK_PEEK_T(TINT);
			break;
			
		case I2F:
			OPSTACK_POP_T(TINT);
			OPSTACK_PUSH_BLIND(TFLOAT);
			break;
		case I2L:
			OPSTACK_POP_T(TINT);
			CHECK_STACK_OVERFLOW(2);
			OPSTACK_WPUSH_BLIND(TLONG);
			break;
		case I2D:
			OPSTACK_POP_T(TINT);
			CHECK_STACK_OVERFLOW(2);
			OPSTACK_WPUSH_BLIND(TDOUBLE);
			break;
			
		case F2I:
			OPSTACK_POP_T(TFLOAT);
			OPSTACK_PUSH_BLIND(TINT);
			break;
		case F2L:
			OPSTACK_POP_T(TFLOAT);
			OPSTACK_WPUSH(TLONG);
			break;
		case F2D:
			OPSTACK_POP_T(TFLOAT);
			OPSTACK_WPUSH(TDOUBLE);
			break;
			
		case L2I:
			OPSTACK_WPOP_T(TLONG);
			OPSTACK_PUSH_BLIND(TINT);
			break;
		case L2F:
			OPSTACK_WPOP_T(TLONG);
			OPSTACK_PUSH_BLIND(TFLOAT);
			break;
		case L2D:
			OPSTACK_WPOP_T(TLONG);
			OPSTACK_WPUSH_BLIND(TDOUBLE);
			break;
			
		case D2I:
			OPSTACK_WPOP_T(TDOUBLE);
			OPSTACK_PUSH_BLIND(TINT);
			break;
		case D2F:
			OPSTACK_WPOP_T(TDOUBLE);
			OPSTACK_PUSH_BLIND(TFLOAT);
			break;
		case D2L:
			OPSTACK_WPOP_T(TDOUBLE);
			OPSTACK_WPUSH_BLIND(TLONG);
			break;
			
			
			
			/**************************************************************
			 * OBJECT CREATION/TYPE CHECKING
			 **************************************************************/
		case INSTANCEOF:
			ENSURE_OPSTACK_SIZE(1);
			if (!isReference(OPSTACK_ITEM(1))) {
				VERIFY_ERROR("instanceof: top of stack is not a reference type");
			}
			*OPSTACK_TOP = *TINT;
			break;
			
		case CHECKCAST:
			ENSURE_OPSTACK_SIZE(1);
			OPSTACK_POP_BLIND;
			goto NEW_COMMON;
			
		case MULTIANEWARRAY:
			n = code[pc + 3];
			ENSURE_OPSTACK_SIZE(n);
			while (n > 0) {
				if (OPSTACK_TOP->data.class != TINT->data.class) {
					VERIFY_ERROR("multinewarray: first <n> things on opstack must be integers");
				}
				OPSTACK_POP_BLIND;
				n--;
			}
			goto NEW_COMMON;
			
		NEW_COMMON:
			GET_WIDX;
			
			CHECK_STACK_OVERFLOW(1);
			block->stacksz++;
			type = OPSTACK_TOP;
			
			if (method->declaring_clazz->tags[idx] == CONSTANT_ResolvedClass) {
				type->tinfo = TINFO_CLASS;
				type->data.class = CLASS_CLASS(idx, pool);
			} else {
				const char* namestr;
				
				namestr = CLASS_NAMED(idx, pool);
				
				if (*namestr == '[') {
					type->tinfo = TINFO_SIG;
					type->data.sig = namestr;
				} else {
					type->tinfo = TINFO_NAME;
					type->data.sig = namestr;
				}
			}
			
			break;
			
		case NEW:
			GET_WIDX;
			
			CHECK_STACK_OVERFLOW(1);
			block->stacksz++;
			type = OPSTACK_TOP;
			if (method->declaring_clazz->tags[idx] == CONSTANT_ResolvedClass) {
				type->tinfo = TINFO_CLASS;
				type->data.class = CLASS_CLASS(idx, pool);
			} else {
				const char* namestr = CLASS_NAMED(idx, pool);
				
				if (*namestr == '[') {
					VERIFY_ERROR("new: used to create an array");
				}
				
				type->tinfo = TINFO_NAME;				
				type->data.name = namestr;
			}
			
			*uninits = pushUninit(*uninits, type);
			type->tinfo = TINFO_UNINIT;
			type->data.uninit  = *uninits;
			
			break;
			
			
		case ANEWARRAY:
			GET_WIDX;
			OPSTACK_PEEK_T(TINT);
			
			type = OPSTACK_TOP;
			if (method->declaring_clazz->tags[idx] == CONSTANT_ResolvedClass) {
				class = CLASS_CLASS(idx, pool);
				type->tinfo = TINFO_CLASS;
				type->data.class  = lookupArray(class, einfo);
				
				if (type->data.class == NULL) {
					VERIFY_ERROR("anewarray: error creating array type");
				}
			} else {
				char* namestr;
				
				sig = CLASS_NAMED(idx, pool);
				if (*sig == '[') {
					namestr = checkPtr(gc_malloc(sizeof(char) * (strlen(sig) + 2), GC_ALLOC_VERIFIER));
					*sigs = pushSig(*sigs, namestr);
					sprintf(namestr, "[%s", sig);
				} else {
					namestr = checkPtr(gc_malloc(sizeof(char) * (strlen(sig) + 4), GC_ALLOC_VERIFIER));
					*sigs = pushSig(*sigs, namestr);
					sprintf(namestr, "[L%s;", sig);
				}
				
				type->tinfo = TINFO_SIG;
				type->data.sig  = namestr;
			}
			
			break;
			
			
		case GETFIELD:
			ENSURE_OPSTACK_SIZE(1);
			if (!checkUninit(this, OPSTACK_TOP)) {
				VERIFY_ERROR("getfield: uninitialized type on top of operand stack");
			}
			
			GET_WIDX;
			n = FIELDREF_CLASS(idx, pool);
			
			if (method->declaring_clazz->tags[n] == CONSTANT_ResolvedClass) {
				t->tinfo = TINFO_CLASS;
				t->data.class = CLASS_CLASS(n, pool);
			} else {
				t->tinfo = TINFO_NAME;
				t->data.name = CLASS_NAMED(n, pool);
			}
			
			OPSTACK_POP_T_BLIND(t);
			goto GET_COMMON;
			
		case GETSTATIC:
			GET_WIDX;
			CHECK_STACK_OVERFLOW(1);
		GET_COMMON:
			sig = FIELDREF_SIGD(idx, pool);
			
			
			/* TODO: we should just have a function that returns a type based on a signature */
			switch (*sig) {
			case 'I': case 'Z': case 'S': case 'B': case 'C':
				OPSTACK_PUSH_BLIND(TINT);
				break;
				
			case 'F': OPSTACK_PUSH_BLIND(TFLOAT); break;
			case 'J': OPSTACK_WPUSH(TLONG); break;
			case 'D': OPSTACK_WPUSH(TDOUBLE); break;
				
			case '[':
			case 'L':
				CHECK_STACK_OVERFLOW(1);
				block->stacksz++;
				type = OPSTACK_TOP;
				type->tinfo = TINFO_SIG;
				type->data.name = sig;
				break;
				
			default:
				VERIFY_ERROR("get{field/static}: unrecognized type signature");
				break;
			}
			break;
			
			
		case PUTFIELD:
			if (IS_WIDE(OPSTACK_TOP)) n = 3;
			else                      n = 2;
			ENSURE_OPSTACK_SIZE(n);
			
			if (!checkUninit(this, OPSTACK_TOP)) {
				VERIFY_ERROR("putfield: uninitialized type on top of operand stack");
			}
			
			GET_WIDX;
			sig = FIELDREF_SIGD(idx, pool);
			
			switch (*sig) {
			case 'I': case 'Z': case 'S': case 'B': case 'C':
				OPSTACK_POP_T_BLIND(TINT);
				break;
				
			case 'F': OPSTACK_POP_T_BLIND(TFLOAT);   break;
			case 'J': OPSTACK_WPOP_T_BLIND(TLONG);   break;
			case 'D': OPSTACK_WPOP_T_BLIND(TDOUBLE); break;
				
			case '[':
			case 'L':
				t->tinfo = TINFO_SIG;
				t->data.sig = sig;
				OPSTACK_POP_T_BLIND(t);
				break;
				
			default:
				VERIFY_ERROR("put{field/static}: unrecognized type signature");
				break;
			}
			
			
			n = FIELDREF_CLASS(idx, pool);
			if (method->declaring_clazz->tags[n] == CONSTANT_ResolvedClass) {
				t->tinfo = TINFO_CLASS;
				t->data.class = CLASS_CLASS(n, pool);
			} else {
				t->tinfo = TINFO_NAME;
				t->data.name = CLASS_NAMED(n, pool);
			}
			
			OPSTACK_POP_T_BLIND(t);
			break;
			
			
		case PUTSTATIC:
			if (OPSTACK_TOP == TWIDE) n = 2;
			else                      n = 1;
			ENSURE_OPSTACK_SIZE(n);
			
			GET_WIDX;
			sig = FIELDREF_SIGD(idx, pool);
			
			
			switch (*sig) {
			case 'I': case 'Z': case 'S': case 'B': case 'C':
				OPSTACK_POP_T_BLIND(TINT);
				break;
				
			case 'F': OPSTACK_POP_T_BLIND(TFLOAT);   break;
			case 'J': OPSTACK_WPOP_T_BLIND(TLONG);   break;
			case 'D': OPSTACK_WPOP_T_BLIND(TDOUBLE); break;
				
			case '[':
			case 'L':
				t->tinfo = TINFO_SIG;
				t->data.sig = sig;
				OPSTACK_POP_T_BLIND(t);
				break;
				
			default:
				VERIFY_ERROR("put{field/static}: unrecognized type signature");
				break;
			}
			break;
			
			
			/**************************************************************
			 * BRANCHING INSTRUCTIONS...END OF BASIC BLOCKS
			 **************************************************************/
		case GOTO:
		case GOTO_W:
			break;
			
		case JSR_W:
		case JSR:
			CHECK_STACK_OVERFLOW(1);
			block->stacksz++;
			type = OPSTACK_TOP;
			type->tinfo = TINFO_ADDR;
			type->data.addr = pc + instruction_length(code[pc]);
			break;

		case RET:
		        /* type checking done during merging stuff... */
			break;
			
		case IF_ACMPEQ:
		case IF_ACMPNE:
			ENSURE_OPSTACK_SIZE(2);
			if (!isReference(OPSTACK_TOP) ||
			    !isReference(OPSTACK_WTOP)) {
				VERIFY_ERROR("if_acmp* when item on top of stack is not a reference type");
			}
			OPSTACK_POP_BLIND;
			OPSTACK_POP_BLIND;
			break;
			
		case IF_ICMPEQ:
		case IF_ICMPNE:
		case IF_ICMPGT:
		case IF_ICMPGE:
		case IF_ICMPLT:
		case IF_ICMPLE:
			OPSTACK_POP_T(TINT);
		case IFEQ:
		case IFNE:
		case IFGT:
		case IFGE:
		case IFLT:
		case IFLE:
			OPSTACK_POP_T(TINT);
			break;
			
		case IFNONNULL:
		case IFNULL:
			ENSURE_OPSTACK_SIZE(1);
			if (!isReference(OPSTACK_ITEM(1))) {
				VERIFY_ERROR("if[non]null: thing on top of stack is not a reference");
			}
			OPSTACK_POP_BLIND;
			break;
			
		case LOOKUPSWITCH:
		case TABLESWITCH:
			OPSTACK_POP_T(TINT);
			return(true);
			
			
			/**************************************************************
			 * METHOD CALLING/RETURNING
			 **************************************************************/
		case INVOKEVIRTUAL:
		case INVOKESPECIAL:
		case INVOKEINTERFACE:
			
		case INVOKESTATIC:
			if (!checkMethodCall(einfo, method, block, pc, sigs, uninits)) {
				/* propagate error */
				VERIFY_ERROR("invoke* error");
			}
			break;
			
			
		case IRETURN:
			OPSTACK_PEEK_T(TINT);
			sig = getReturnSig(method);
			if (strlen(sig) != 1 || (*sig != 'I' && *sig != 'Z' && *sig != 'S' && *sig != 'B' && *sig != 'C')) {
				VERIFY_ERROR("ireturn: method doesn't return an integer");
			}
			break;
		case FRETURN:
			OPSTACK_PEEK_T(TFLOAT);
			sig = getReturnSig(method);
			if (strcmp(sig, "F")) {
				VERIFY_ERROR("freturn: method doesn't return an float");
			}
			break;
		case LRETURN:
			OPSTACK_WPEEK_T(TLONG);
			sig = getReturnSig(method);
			if (strcmp(sig, "J")) {
				VERIFY_ERROR("lreturn: method doesn't return a long");
			}
			break;
		case DRETURN:
			OPSTACK_WPEEK_T(TDOUBLE);
			sig = getReturnSig(method);
			if (strcmp(sig, "D")) {
				VERIFY_ERROR("dreturn: method doesn't return a double");
			}
			break;
		case RETURN:
			sig = getReturnSig(method);
			if (strcmp(sig, "V")) {
				VERIFY_ERROR("return: must return something in a non-void function");
			}
			break;
		case ARETURN:
			ENSURE_OPSTACK_SIZE(1);
			t->tinfo = TINFO_SIG;
			t->data.sig  = getReturnSig(method);
			if (!typecheck(einfo, this, t, OPSTACK_TOP)) {
				VERIFY_ERROR("areturn: top of stack is not type compatible with method return type");
			}
			break;
			
		case ATHROW:
			ENSURE_OPSTACK_SIZE(1);
			if (!javaLangThrowable) {
			        /* TODO: this is here for now, but perhaps we should have a TTHROWABLE that initialized as
				 *       a signature, like we do for String and Object
				 */
				loadStaticClass(&javaLangThrowable, "java/lang/Throwable");
			}
			t->tinfo = TINFO_CLASS;
			t->data.class = javaLangThrowable;
			if (!typecheck(einfo, this, t, OPSTACK_TOP)) {
				VERIFY_ERROR("athrow: object on top of stack is not a subclass of throwable");
			}
			
			for (n = 0; n < method->localsz; n++) {
				if (block->locals[n].tinfo & TINFO_UNINIT) {
					VERIFY_ERROR("athrow: uninitialized class instance in a local variable");
				}
			}
			break;
			
			
			/**************************************************************
			 * MISC
			 **************************************************************/
		case NOP:
			break;
			
			
		case BREAKPOINT:
		        /* for internal use only: cannot appear in a class file */
			VERIFY_ERROR("breakpoint instruction cannot appear in classfile");
			break;
			
			
		case MONITORENTER:
		case MONITOREXIT:
			ENSURE_OPSTACK_SIZE(1);
			if(!isReference(OPSTACK_TOP)) {
				VERIFY_ERROR("monitor*: top of stack is not an object reference");
			}
			OPSTACK_POP_BLIND;
			break;
			
			
		case DUP:
			ENSURE_OPSTACK_SIZE(1);
			if (IS_WIDE(OPSTACK_TOP)) {
				VERIFY_ERROR("dup: on a long or double");
			}
			
			OPSTACK_PUSH(OPSTACK_TOP);
			break;
			
		case DUP_X1:
			ENSURE_OPSTACK_SIZE(2);
			if (IS_WIDE(OPSTACK_TOP) || IS_WIDE(OPSTACK_WTOP)) {
				VERIFY_ERROR("dup_x1: splits up a double or long");
			}
			
			OPSTACK_PUSH(OPSTACK_TOP);
			
			*OPSTACK_ITEM(2) = *OPSTACK_ITEM(3);
			*OPSTACK_ITEM(3) = *OPSTACK_ITEM(1);
			break;
			
		case DUP_X2:
			ENSURE_OPSTACK_SIZE(3);
			if (IS_WIDE(OPSTACK_TOP)) {
				VERIFY_ERROR("cannot dup_x2 when top item on operand stack is a two byte item");
			}
			
			OPSTACK_PUSH(OPSTACK_TOP);
			
			*OPSTACK_ITEM(2) = *OPSTACK_ITEM(3);
			*OPSTACK_ITEM(3) = *OPSTACK_ITEM(4);
			*OPSTACK_ITEM(4) = *OPSTACK_ITEM(1);
			break;
			
		case DUP2:
			ENSURE_OPSTACK_SIZE(2);
			
			OPSTACK_PUSH(OPSTACK_WTOP);
			OPSTACK_PUSH(OPSTACK_WTOP);
			break;
			
		case DUP2_X1:
			ENSURE_OPSTACK_SIZE(2);
			if (IS_WIDE(OPSTACK_ITEM(2))) {
				VERIFY_ERROR("dup_x1 requires top 2 bytes on operand stack to be single bytes items");
			}
			CHECK_STACK_OVERFLOW(2);
			
			OPSTACK_PUSH_BLIND(OPSTACK_ITEM(2));
			OPSTACK_PUSH_BLIND(OPSTACK_ITEM(2));
			
			*OPSTACK_ITEM(3) = *OPSTACK_ITEM(5);
			*OPSTACK_ITEM(4) = *OPSTACK_ITEM(1);
			*OPSTACK_ITEM(5) = *OPSTACK_ITEM(2);
			break;
			
		case DUP2_X2:
			ENSURE_OPSTACK_SIZE(4);
			if (IS_WIDE(OPSTACK_ITEM(2)) || IS_WIDE(OPSTACK_ITEM(4))) {
				VERIFY_ERROR("dup2_x2 where either 2nd or 4th byte is 2nd half of a 2 byte item");
			}
			CHECK_STACK_OVERFLOW(2);
			
			OPSTACK_PUSH_BLIND(OPSTACK_ITEM(2));
			OPSTACK_PUSH_BLIND(OPSTACK_ITEM(2));
			
			*OPSTACK_ITEM(3) = *OPSTACK_ITEM(5);
			*OPSTACK_ITEM(4) = *OPSTACK_ITEM(6);
			*OPSTACK_ITEM(5) = *OPSTACK_ITEM(1);
			*OPSTACK_ITEM(6) = *OPSTACK_ITEM(2);
			break;
			
			
		case SWAP:
			ENSURE_OPSTACK_SIZE(2);
			if (IS_WIDE(OPSTACK_TOP) || IS_WIDE(OPSTACK_WTOP)) {
				VERIFY_ERROR("cannot swap 2 bytes of a long or double");
			}
			
			*type         = *OPSTACK_TOP;
			*OPSTACK_TOP  = *OPSTACK_WTOP;
			*OPSTACK_WTOP = *type;
			break;
			
			
		case WIDE:
			is_wide = true;
			pc += instruction_length(code[pc]);
			continue;
			
		default:
		        /* should never get here because of preprocessing in defineBasicBlocks() */
			VERIFY_ERROR("unknown opcode encountered");
		}
		
		
		pc += instruction_length(code[pc]);
		if (is_wide == true) {
			is_wide = false;
			pc++;
		}
	}
		
	
	/* SUCCESS! */
	return(true);


	
	/* take care of the namespace */
#undef OPSTACK_POP_N
#undef OPSTACK_POP_N_BLIND

#undef OPSTACK_WPOP_T
#undef OPSTACK_WPOP_T_BLIND
#undef OPSTACK_WPOP
#undef OPSTACK_WPOP_BLIND

#undef OPSTACK_POP_T
#undef OPSTACK_POP_T_BLIND
#undef OPSTACK_POP
#undef OPSTACK_POP_BLIND

#undef OPSTACK_WPEEK_T
#undef OPSTACK_WPEEK_T_BLIND
#undef OPSTACK_PEEK_T
#undef OPSTACK_PEEK_T_BLIND

#undef OPSTACK_WPUSH
#undef OPSTACK_WPUSH_BLIND
#undef OPSTACK_PUSH
#undef OPSTACK_PUSH_BLIND

#undef LOCALS_INFO
#undef OPSTACK_INFO

#undef OPSTACK_WTOP
#undef OPSTACK_TOP
#undef OPSTACK_ITEM

#undef CHECK_STACK_OVERFLOW
#undef ENSURE_OPSTACK_SIZE

#undef ENSURE_LOCAL_WTYPE
#undef ENSURE_LOCAL_TYPE

#undef GET_WIDX
#undef GET_IDX

#undef VERIFY_ERROR
}


/* 
 * parses the next argument from sig into buf, returning pointer beyond arg.
 */
static
const char*
getNextArg(const char* sig, char* buf)
{
	const char* afterSig;
	
	if (*sig == ')') {
		buf[0] = ')';
		buf[1] = '\0';
		return sig;
	}
	/* parseFieldTypeDescriptor doesn't deal with void signatures */
	else if (*sig == 'V') {
		buf[0] = 'V';
		buf[1] = '\0';
		sig++;
		return sig;
	}
	
	for (afterSig = parseFieldTypeDescriptor(sig);
	     sig < afterSig;
	     sig++, buf++) {
		*buf = *sig;
	}
	
	*buf = '\0';
	
	return afterSig;
}


/*
 * countSizeOfArgsInSignature()
 *    Longs & Double count for 2, all else counts for one.
 */
static
w_word
countSizeOfArgsInSignature(const char* sig)
{
	w_word count = 0;
	
	for (sig++; *sig != ')'; sig = parseFieldTypeDescriptor(sig)) {
		if (*sig == 'J' || *sig == 'D')
			count += 2;
		else
			count++;
	}
	
	return count;
}


/* 
 * checkMethodCall()
 *    verify an invoke instruction.  this includes making sure that the types
 *    on the operand stack are type compatible with those expected by the method
 *    being called.
 *
 *    note: we don't check to make sure that the class being referenced by the
 *          method call actually has the method, or that we have permission to
 *          access it, as those checks are deferred until pass 4.
 *
 * returns whether the method's arguments type check correctly.
 * it also pushes the return type onto binfo's operand stack.
 */
static
w_boolean
checkMethodCall(errorInfo* einfo, const Method* method,
		v_BasicBlock* binfo, w_word pc,
		SigStack** sigs, v_UninitializedType** uninits)
{
#define VERIFY_ERROR(_MSG) \
	releaseMem(argbuf); \
	if (einfo->type == 0) { \
		postExceptionMessage(einfo, JAVA_LANG(VerifyError), \
				     "in method \"%s.%s\": %s", \
				     CLASS_CNAME(method->class), METHOD_NAMED(method), _MSG); \
	} \
	return(false)
	
#define TYPE_ERROR VERIFY_ERROR("parameters fail type checking in method invocation")
	
	const unsigned char* code        = METHOD_BYTECODE_CODE(method);
	const w_word opcode              = code[pc];
	
	const constants* pool            = CLASS_CONSTANTS(method->class);
	const w_size idx                 = WORD(code, pc + 1);
				   				 
	const w_size classIdx            = METHODREF_CLASS(idx, pool);
	v_Type  mrc;
	v_Type* methodRefClass             = &mrc;
	v_Type* t                          = &mrc; /* for shorthand :> */
	v_Type* receiver                   = NULL;
	
	const char* methSig              = METHODREF_SIGD(idx, pool);
	const char* sig                  = methSig;
	w_size nargs                     = countSizeOfArgsInSignature(sig);
	
	w_size paramIndex                = 0;
	char* argbuf                     = checkPtr(gc_malloc(strlen(sig) * sizeof(char), GC_ALLOC_VERIFIER));
	
	
	
	
	if (nargs > binfo->stacksz) {
		VERIFY_ERROR("not enough stuff on opstack for method invocation");
	}
	
	
	/* make sure that the receiver is type compatible with the class being invoked */
	if (opcode != INVOKESTATIC) {
		if (nargs == binfo->stacksz) {
			VERIFY_ERROR("not enough stuff on opstack for method invocation");
		}
		
		
		receiver = &binfo->opstack[binfo->stacksz - (nargs + 1)];
		if (!(receiver->tinfo & TINFO_UNINIT) && !isReference(receiver)) {
			VERIFY_ERROR("invoking a method on something that is not a reference");
		}
		
		if (method->declaring_clazz->tags[classIdx] == CONSTANT_Class) {
			methodRefClass->tinfo = TINFO_NAME;
			methodRefClass->data.name = UNRESOLVED_CLASS_NAMED(classIdx, pool);
		} else {
			methodRefClass->tinfo = TINFO_CLASS;
			methodRefClass->data.class = CLASS_CLASS(classIdx, pool);
		}
		
		
		if (!strcmp(METHODREF_NAMED(idx,pool), constructor_name->data)) {
			if (receiver->tinfo & TINFO_UNINIT) {
				v_UninitializedType* uninit = receiver->data.uninit;
				
				if (receiver->tinfo == TINFO_UNINIT_SUPER) {
					v_Type t;
					t.tinfo = TINFO_CLASS;
					t.data.class = uninit->type.data.class->superclass;
					
					if (!sameType(methodRefClass, &uninit->type) &&
					    uninit->type.data.class != TOBJ->data.class &&
					    !sameType(methodRefClass, &t)) {
						VERIFY_ERROR("incompatible receiving type for superclass constructor call");
					}
				} else if (!sameType(methodRefClass, &uninit->type)) {
					VERIFY_ERROR("incompatible receiving type for constructor call");
				}
				
				/* fix front of list, if necessary */
				if (uninit == *uninits) {
					*uninits = (*uninits)->next;
					if (*uninits) {
						(*uninits)->prev = NULL;
					}
					uninit->next = NULL;
				}
				
				popUninit(method, uninit, binfo);
			}
			else if (!sameType(methodRefClass, receiver)) {
				VERIFY_ERROR("incompatible receiving type for constructor call");
			}
		}
		else if (!typecheck(einfo, method->class, methodRefClass, receiver)) {
			if (receiver->tinfo & TINFO_UNINIT) {
				VERIFY_ERROR("invoking a method on an uninitialized object reference");
			}
			
			VERIFY_ERROR("expected method receiver does not typecheck with object on operand stack");
		}
	}
	
	
	/* here we use paramIndex to represent which parameter we're currently considering.
	 * remember, when we call a method, the first parameter is deepest in the stack,
	 * so when we traverse the parameter list in the method signature we have to look
	 * from the bottom up.
	 */
	paramIndex = binfo->stacksz - nargs;
	for (sig = getNextArg(sig + 1, argbuf); *argbuf != ')'; sig = getNextArg(sig, argbuf)) {
		
		if (paramIndex >= binfo->stacksz) {
			releaseMem(argbuf);
			VERIFY_ERROR("error: not enough parameters on stack for method invocation");
		}
		
		
		switch (*argbuf) {
		case '[':
		case 'L':
			t->tinfo = TINFO_SIG;
			t->data.sig = argbuf;
			
			if (!typecheck(einfo, method->class, t, &binfo->opstack[paramIndex])) {
				TYPE_ERROR;
			}
			
			binfo->opstack[paramIndex] = *TUNSTABLE;
			paramIndex++;
			break;
			
		case 'Z': case 'S': case 'B': case 'C':
		case 'I':
			if (binfo->opstack[paramIndex].data.class != TINT->data.class) {
				TYPE_ERROR;
			}
			
			binfo->opstack[paramIndex] = *TUNSTABLE;
			paramIndex++;
			break;
			
		case 'F':
			if (binfo->opstack[paramIndex].data.class != TFLOAT->data.class) {
				TYPE_ERROR;
			}
			
			binfo->opstack[paramIndex] = *TUNSTABLE;
			paramIndex++;
			break;
			
		case 'J':
			if (binfo->opstack[paramIndex].data.class != TLONG->data.class ||
			    !IS_WIDE(&binfo->opstack[paramIndex + 1])) {
				TYPE_ERROR;
			}
			
			binfo->opstack[paramIndex]    = *TUNSTABLE;
			binfo->opstack[paramIndex+ 1] = *TUNSTABLE;
			paramIndex += 2;
			break;
			
		case 'D':
			if (binfo->opstack[paramIndex].data.class != TDOUBLE->data.class ||
			    !IS_WIDE(&binfo->opstack[paramIndex + 1])) {
				TYPE_ERROR;
			}
			
			binfo->opstack[paramIndex]     = *TUNSTABLE;
			binfo->opstack[paramIndex + 1] = *TUNSTABLE;
			paramIndex += 2;
			break;
			
		default:
			TYPE_ERROR;
		}
	}
	binfo->stacksz -= nargs;
	
	
	if (opcode != INVOKESTATIC) {
	        /* pop object reference off the stack */
		binfo->stacksz--;
		binfo->opstack[binfo->stacksz] = *TUNSTABLE;
	}
	
	
	/**************************************************************
	 * Process Return Type
	 **************************************************************/
	sig++;
	sig = getNextArg(sig, argbuf);
	
	if (*argbuf == 'J' || *argbuf == 'D') {
		if (method->stacksz < binfo->stacksz + 2) {
			VERIFY_ERROR("not enough room on operand stack for method call's return value");
		}
	}
	else if (*argbuf != 'V') {
		if (method->stacksz < binfo->stacksz + 1) {
			VERIFY_ERROR("not enough room on operand stack for method call's return value");
		}
	}
	
	switch (*argbuf) {
	case 'Z': case 'S': case 'B': case 'C':
	case 'I':
		binfo->opstack[binfo->stacksz++] = *TINT;
		break;
		
	case 'F':
		binfo->opstack[binfo->stacksz++] = *TFLOAT;
		break;
		
	case 'J':
		binfo->opstack[binfo->stacksz]     = *TLONG;
		binfo->opstack[binfo->stacksz + 1] = *TWIDE;
		binfo->stacksz += 2;
		break;
		
	case 'D':
		binfo->opstack[binfo->stacksz]     = *TDOUBLE;
		binfo->opstack[binfo->stacksz + 1] = *TWIDE;
		binfo->stacksz += 2;
		break;
		
	case 'V':
		break;
		
	case '[':
	case 'L':
		*sigs = pushSig(*sigs, argbuf);
		
		binfo->opstack[binfo->stacksz].data.class = (Hjava_lang_Class*)argbuf;
		binfo->opstack[binfo->stacksz].tinfo = TINFO_SIG;
		binfo->stacksz++;
		
		/* no freeing of the argbuf here... */
		return(true);
		
	default:
	        /* shouldn't get here because of parsing during pass 2... */
		releaseMem(argbuf);
		postExceptionMessage(einfo, JAVA_LANG(InternalError),
				     "unrecognized return type signature");
		return(false);
	}
	
	releaseMem(argbuf);
	return(true);
#undef TYPE_ERROR
#undef VERIFY_ERROR
}




/*
 * pushes the initial method arguments into local variable array
 */
static
w_boolean
loadInitialArgs(const Method* method, errorInfo* einfo,
		v_BasicBlock* block,
		SigStack** sigs, v_UninitializedType** uninits)
{
#define VERIFY_ERROR(_MSG) \
	postExceptionMessage(einfo, JAVA_LANG(VerifyError), \
			     "method %s.%s: %s", \
			     CLASS_CNAME(method->class), METHOD_NAMED(method), _MSG); \
	releaseMem(argbuf); \
	return(false)

#define LOCAL_OVERFLOW_ERROR \
	VERIFY_ERROR("method arguments cannot fit into local variables")
	
	
	w_word paramCount = 0;
	
	/* the +1 skips the initial '(' */
	const char* sig = METHOD_SIGD(method) + 1;
	char* argbuf    = checkPtr(gc_malloc((strlen(sig)+1) * sizeof(char), GC_ALLOC_VERIFIER));
	char* newsig    = NULL;
	
	v_Type* locals = block->locals;
	
	/* must have at least 1 local variable for the object reference	*/
	if (!METHOD_IS_STATIC(method)) {
		if (method->localsz <= 0) {
			VERIFY_ERROR("number of locals in non-static method must be > 0");
		}
		
		/* the first local variable in every method is the class to which it belongs */
		locals[0].tinfo = TINFO_CLASS;
		locals[0].data.class = method->class;
		paramCount++;
		if (!strcmp(METHOD_NAMED(method), constructor_name->data)) {
		        /* the local reference in a constructor is uninitialized */
			*uninits = pushUninit(*uninits, &locals[0]);
			locals[0].tinfo = TINFO_UNINIT_SUPER;
			locals[0].data.uninit = *uninits;
		}
	}
	
	for (sig = getNextArg(sig, argbuf); *argbuf != ')'; sig = getNextArg(sig, argbuf)) {
		if (paramCount > method->localsz) {
			LOCAL_OVERFLOW_ERROR;
		}
		
		switch (*argbuf) {
		case 'Z': case 'S': case 'B': case 'C':
		case 'I': locals[paramCount++] = *TINT; break;
		case 'F': locals[paramCount++] = *TFLOAT; break;
			
		case 'J':
			if (paramCount + 1 > method->localsz) {
				LOCAL_OVERFLOW_ERROR;
			}
			locals[paramCount] = *TLONG;
			locals[paramCount+1] = *TWIDE;
			paramCount += 2;
			break;
			
		case 'D':
			if (paramCount + 1 > method->localsz) {
				LOCAL_OVERFLOW_ERROR;
			}
			locals[paramCount] = *TDOUBLE;
			locals[paramCount+1] = *TWIDE;
			paramCount += 2;
			break;
			
		case '[':
		case 'L':
			newsig = checkPtr(gc_malloc((strlen(argbuf) + 1) * sizeof(char), GC_ALLOC_VERIFIER));
			*sigs = pushSig(*sigs, newsig);
			sprintf(newsig, "%s", argbuf);
			locals[paramCount].tinfo = TINFO_SIG;
			locals[paramCount].data.sig = newsig;
			paramCount++;
			break;
			
		default:
			
			VERIFY_ERROR("unrecognized first character in parameter type descriptor");
			break;
		}
	}
	
	
	/* success! */
	releaseMem(argbuf);
	return(true);

#undef LOCAL_OVERFLOW_ERROR
#undef VERIFY_ERROR
}


/*
 * getReturnSig()
 */
static
const char*
getReturnSig(const Method* method)
{
	const char* sig = METHOD_SIGD(method);
	
	/* skip the type parameters */
	for (sig++; *sig != ')'; sig = parseFieldTypeDescriptor(sig));
	sig++;
	
	return sig;
}


/*
 * resolveType()
 *     Ensures that the type is a pointer to an instance of Hjava_lang_Class.
 */
static
void
resolveType(errorInfo* einfo, Hjava_lang_Class* this, v_Type *type)
{
	const char* sig;
	char* tmp = NULL;

	if (type->tinfo & TINFO_NAME) {
		sig = type->data.sig;
		
		if (*sig != '[') {
			tmp = checkPtr(gc_malloc((strlen(sig) + 3) * sizeof(char), GC_ALLOC_VERIFIER));
			sprintf(tmp, "L%s;", sig);
			sig = tmp;
		}
		
		type->tinfo = TINFO_CLASS;
		type->data.class = getClassFromSignature(sig, this->loader, einfo);
		
		if (tmp) {
			releaseMem(tmp);
		}
	}
	else if (type->tinfo & TINFO_SIG) {
		type->tinfo = TINFO_CLASS;
		type->data.class = getClassFromSignature(type->data.sig, this->loader, einfo);
	}
}


/*
 * mergeTypes()
 *     merges two types, t1 and t2, into t2.  this result could
 *     be a common superclass, a common class that both types implement, or,
 *     in the event that the types are not compatible, TUNSTABLE.
 *
 * returns whether an actual merger was made (i.e. they weren't the same type)
 *
 * note: the precedence of merged types goes (from highest to lowest):
 *     actual pointer to Hjava_lang_Class*
 *     TINFO_SIG
 *     TINFO_NAME
 *
 * TODO: right now the priority is to be a common superclass, as stated in
 *       the JVML2 specs.  a better verification technique might check this first,
 *       and then check interfaces that both classes implement.  of course, depending
 *       on the complexity of the inheritance hirearchy, this could take a lot of time.
 *       
 *       the ideal solution is to remember *all* possible highest resolution types,
 *       which, of course, would require allocating more memory on the fly, etc., so,
 *       at least for now, we're not really even considering it.
 */
static
w_boolean
mergeTypes(errorInfo* einfo, Hjava_lang_Class* this,
	   v_Type* t1, v_Type* t2)
{
	if (IS_ADDRESS(t1) || IS_ADDRESS(t2)) {
	        /* if one of the types is TADDR, the other one must also be TADDR */
		if (t1->tinfo != t2->tinfo) {
			return false;
		}
		
		/* TODO: should this be an error if they don't agree? */
		t2->tinfo = t1->tinfo;
		return true;
	}
	else if (t2->data.class == TUNSTABLE->data.class || sameType(t1, t2)) {
		return false;
	}
	else if (t1->tinfo & TINFO_UNINIT || t2->tinfo & TINFO_UNINIT ||
		 !isReference(t1) || !isReference(t2)) {
		
		*t2 = *TUNSTABLE;
		return true;
	}
	/* references only from here on out */
	else if (t1->data.class == TOBJ->data.class) {
		*t2 = *t1;
		return true;
	}
	
	
	/* not equivalent, must resolve them */
	resolveType(einfo, this, t1);
	if (t1->data.class == NULL) {
		return false;
	}

	resolveType(einfo, this, t2);
	if (t2->data.class == NULL) {
		return false;
	}
	
	if (CLASS_IS_INTERFACE(t1->data.class) &&
	    instanceof_interface(t1->data.class, t2->data.class)) {
	
		/* t1 is an interface and t2 implements it,
		 * so the interface is the merged type.
		 */

		*t2 = *t1;
		
		return true;
	
	} else if (CLASS_IS_INTERFACE(t2->data.class) &&
		   instanceof_interface(t2->data.class, t1->data.class)) {
		
		/* same as above, but we don't need to merge, since
		 * t2 already is the merged type
		 */

		return false;
	} else {
		/*
		 * neither of the types is an interface, so we have to
		 * check for common superclasses. Only merge iff t2 is
		 * not the common superclass.
		 */
		Hjava_lang_Class *tmp = t2->data.class;
		
		t2->data.class = getCommonSuperclass(t1->data.class, t2->data.class);
		
		return tmp != t2->data.class;
	} 
}


/*
 * returns the first (highest) common superclass of classes A and B.
 *
 * precondition: neither type is an array type
 *               nor is either a primitive type
 */
static
Hjava_lang_Class*
getCommonSuperclass(Hjava_lang_Class* t1, Hjava_lang_Class* t2)
{
	Hjava_lang_Class* A;
	Hjava_lang_Class* B;
	
	for (A = t1; A != NULL; A = A->superclass) {
		for (B = t2; B != NULL; B = B->superclass) {
			if (A == B) return A;
		}
	}
	
	/* error of some kind...at the very least, we shoulda gotten to Object
	 * when traversing the class hirearchy
	 */
	return TUNSTABLE->data.class;
}


/*
 * isReference()
 *    returns whether the type is a reference type
 */
static
w_boolean
isReference(const v_Type* type)
{
	return (type->tinfo & TINFO_NAME ||
		type->tinfo & TINFO_SIG ||
		type->tinfo & TINFO_CLASS ||
		type->tinfo & TINFO_UNINIT);
}

/*
 * isArray()
 *     returns whether the Type is an array Type
 */
static
w_boolean
isArray(const v_Type* type)
{
	if (!isReference(type)) {
		return false;
	}
	else if (type->tinfo & TINFO_NAME || type->tinfo & TINFO_SIG) {
		return (*(type->data.sig) == '[');
	}
	else if (type->tinfo != TINFO_CLASS) {
		return false;
	}
	else {
		return (*(CLASS_CNAME(type->data.class)) == '[');
	}
}


/*
 * sameType()
 *     returns whether two Types are effectively equivalent.
 */
static
w_boolean
sameType(v_Type* t1, v_Type* t2)
{
	switch (t1->tinfo) {
	case TINFO_SYSTEM:
		return (t2->tinfo == TINFO_SYSTEM &&
			t1->data.class == t2->data.class);
		
	case TINFO_ADDR:
		return (t2->tinfo == TINFO_ADDR &&
			t1->data.addr == t2->data.addr);
		
	case TINFO_PRIMITIVE:
		return (t2->tinfo == TINFO_PRIMITIVE &&
			t1->data.class == t2->data.class);
		
	case TINFO_UNINIT:
	case TINFO_UNINIT_SUPER:
		return (t2->tinfo & TINFO_UNINIT &&
			(t1->data.uninit == t2->data.uninit ||
			 sameRefType(&(t1->data.uninit->type),
				     &(t2->data.uninit->type))));
		
	default:
		return false;
		
	case TINFO_SIG:
	case TINFO_NAME:
	case TINFO_CLASS:
		return ((t2->tinfo == TINFO_SIG ||
			 t2->tinfo == TINFO_NAME || 
			 t2->tinfo == TINFO_CLASS) &&
			sameRefType(t1,t2));
	}
}

/*
 * sameRefType()
 *     returns whether two Types are effectively equivalent.
 *
 *     pre: t1 and t2 are both reference types
 */
static
w_boolean
sameRefType(v_Type* t1, v_Type* t2)
{
	const char* sig1 = NULL;
	const char* sig2 = NULL;
	w_word len1, len2;
	
	if (IS_NULL(t1) || IS_NULL(t2)) {
		return true;
	}
	
	if (t1->tinfo & TINFO_NAME) {
		sig1 = t1->data.name;
		
		if (t2->tinfo & TINFO_NAME) {
			return (!strcmp(sig1, t2->data.name));
		}
		else if (t2->tinfo & TINFO_SIG) {
			sig2 = t2->data.sig;
			
			len1 = strlen(sig1);
			len2 = strlen(sig2);
			
			sig2++;
			if ((len1 + 2 != len2) || strncmp(sig1, sig2, len1))
				return false;
		}
		else {
			if (strcmp(sig1, CLASS_CNAME(t2->data.class)))
				return false;
		}
		
		*t1 = *t2;
		return true;
	}
	else if (t1->tinfo & TINFO_SIG) {
		sig1 = t1->data.sig;
		
		if (t2->tinfo & TINFO_SIG) {
			return (!strcmp(sig1, t2->data.sig));
		}
		else if (t2->tinfo & TINFO_NAME) {
			sig2 = t2->data.name;
			
			len1 = strlen(sig1);
			len2 = strlen(sig2);
			sig1++;
			
			if ((len1 != len2 + 2) || strncmp(sig1, sig2, len2))
				return false;
			
			*t2 = *t1;
			return true;
		}
		else {
			sig2 = CLASS_CNAME(t2->data.class);
			
			len1 = strlen(sig1);
			len2 = strlen(sig2);
			sig1++;
			
			if ((len1 != len2 + 2) || strncmp(sig1, sig2, len2))
				return false;
			
			*t1 = *t2;
			return true;
		}
	}
	else {
		sig1 = CLASS_CNAME(t1->data.class);
		
		if (t2->tinfo & TINFO_SIG) {
			sig2 = t2->data.sig;
			
			len1 = strlen(sig1);
			len2 = strlen(sig2);
			sig2++;
			if ((len1 + 2 != len2) || strncmp(sig1, sig2, len1))
				return false;
			
			*t2 = *t1;
			return true;
		}
		else if (t2->tinfo & TINFO_NAME) {
			sig2 = t2->data.name;
			
			if (strcmp(sig1, sig2))
				return false;
			
			*t2 = *t1;
			return true;
		}
		else {
		        /* we should never get here */
			sig2 = CLASS_CNAME(t2->data.class);
			return (!strcmp(sig1, sig2));
		}
	}
}


/*
 * returns whether t2 can be a t1
 */
static
w_boolean
typecheck(errorInfo* einfo, Hjava_lang_Class* this, v_Type* t1, v_Type* t2)
{
	
	if (sameType(t1, t2)) {
		return true;
	}
	else if (t1->tinfo & TINFO_UNINIT || t2->tinfo & TINFO_UNINIT) {
		return false;
	}
	else if (!isReference(t1) || !isReference(t2)) {
		return false;
	}
	else if (sameType(t1, TOBJ)) {
		return true;
	}

	resolveType(einfo, this, t1);
	if (t1->data.class == NULL) {
		return false;
	}

	resolveType(einfo, this, t2);
	if (t2->data.class == NULL) {
		return false;
	}

	return instanceof(t1->data.class, t2->data.class);
}

/*
 * allocate memory for a block info and fill in with default values
 */
v_BasicBlock*
createBlock(const Method* method)
{
	int i;
	
	v_BasicBlock* binfo = checkPtr((v_BasicBlock*)gc_malloc(sizeof(v_BasicBlock), GC_ALLOC_VERIFIER));
	
	binfo->startAddr   = 0;
	binfo->status      = IS_INSTRUCTION | START_BLOCK;  /* not VISITED or CHANGED */
	
	/* allocate memory for locals */
	if (method->localsz > 0) {
		binfo->locals = checkPtr(gc_malloc(method->localsz * sizeof(v_Type), GC_ALLOC_VERIFIER));
		
		for (i = 0; i < method->localsz; i++) {
			binfo->locals[i] = *TUNSTABLE;
		}
	} else {
		binfo->locals = NULL;
	}
	
	
	/* allocate memory for operand stack */
	binfo->stacksz = 0;
	if (method->stacksz > 0) {
		binfo->opstack = checkPtr(gc_malloc(method->stacksz * sizeof(v_Type), GC_ALLOC_VERIFIER));
		
		for (i = 0; i < method->stacksz; i++) {
			binfo->opstack[i] = *TUNSTABLE;
		}
	} else {
		binfo->opstack = NULL;
	}
	
	return binfo;
}

/*
 * copies information from one stack of basic blocks to another
 */
void
copyBlockData(const Method* method, v_BasicBlock* fromBlock, v_BasicBlock* toBlock)
{
	toBlock->startAddr = fromBlock->startAddr;
	toBlock->lastAddr  = fromBlock->lastAddr;
	
	copyBlockState(method, fromBlock, toBlock);
}

/*
 * copies the local variables, operand stack, status, and context
 * from one block to another.
 */
void
copyBlockState(const Method* method, v_BasicBlock* fromBlock, v_BasicBlock* toBlock)
{
	w_word n;
	
	toBlock->status  = fromBlock->status;
	
	for (n = 0; n < method->localsz; n++) {
		toBlock->locals[n] = fromBlock->locals[n];
	}
	
	toBlock->stacksz = fromBlock->stacksz;
	for (n = 0; n < method->stacksz; n++) {
		toBlock->opstack[n] = fromBlock->opstack[n];
	}
}

/*
 * returns which block the given pc is in
 */
static
v_BasicBlock*
inWhichBlock(w_word pc, v_BasicBlock** blocks, w_word numBlocks)
{
	w_word i;
	for (i = 0; i < numBlocks; i++) {
		if (pc < blocks[i]->startAddr) continue;
		if (pc <= blocks[i]->lastAddr) return blocks[i];
	}
	
	/* shouldn't ever get here unless the specified PC is messed up */
	
	return NULL;
}



/*
 * pushSig()
 *     Pushes a new signature on the Stack
 */
static
SigStack*
pushSig(SigStack* sigs, const char* sig)
{
	SigStack* new_sig = checkPtr(gc_malloc(sizeof(SigStack), GC_ALLOC_VERIFIER));
	new_sig->sig = sig;
	new_sig->next = sigs;
	return new_sig;
}


/*
 * freeSigStack()
 *     Frees the memory consumed by a stack of names and signatures.
 */
static
void
freeSigStack(SigStack* sigs)
{
	SigStack* tmp;
	while(sigs != NULL) {
		tmp = sigs->next;
		releaseMem(sigs);
		sigs = tmp;
	}
}


/*
 * checkUninit()
 *     To be called when dealing with (get/put)field access.  Makes sure that get/putfield and
 *     invoke* instructions have access to the instance fields of the object in question.
 */
static
w_boolean
checkUninit(Hjava_lang_Class* this, v_Type* type)
{
	if (type->tinfo & TINFO_UNINIT) {
		if (type->tinfo & TINFO_UNINIT_SUPER) {
			v_UninitializedType* uninit = type->data.uninit;
			v_Type t;
			t.tinfo = TINFO_CLASS;
			t.data.class = this;
			
			if (!sameType(&uninit->type, &t)) {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	return true;
}

/*
 * pushUninit()
 *    Adds an uninitialized type to the list of uninitialized types.
 *
 *    uninits is the front of the list to be added onto.
 */
static
v_UninitializedType*
pushUninit(v_UninitializedType* uninits, const v_Type* type)
{
	v_UninitializedType* uninit = checkPtr(gc_malloc(sizeof(v_UninitializedType), GC_ALLOC_VERIFIER));
	uninit->type = *type;
	uninit->prev = NULL;
	
	if (!uninits) {
		uninit->next = NULL;
		return uninit;
	}
	
	uninit->prev = NULL;
	uninit->next = uninits;
	uninits->prev = uninit;
	return uninit;
}

/*
 * popUninit()
 *     Pops an uninitialized type off of the operand stack
 */
static
void
popUninit(const Method* method, v_UninitializedType* uninit, v_BasicBlock* binfo)
{
	w_word n;
	
	for (n = 0; n < method->localsz; n++) {
		if (binfo->locals[n].tinfo & TINFO_UNINIT &&
		    ((v_UninitializedType*)binfo->locals[n].data.class) == uninit) {
			binfo->locals[n] = uninit->type;
		}
	}
	
	for (n = 0; n < binfo->stacksz; n++) {
		if (binfo->opstack[n].tinfo & TINFO_UNINIT &&
		    ((v_UninitializedType*)binfo->opstack[n].data.class) == uninit) {
			binfo->opstack[n] = uninit->type;
		}
	}
	
	if (uninit->prev) {
		uninit->prev->next = uninit->next;
	}
	if (uninit->next) {
		uninit->next->prev = uninit->prev;
	}
	
	releaseMem(uninit);
}

/*
 * freeUninits
 *    frees a list of uninitialized types
 */
static
void
freeUninits(v_UninitializedType* uninits)
{
	v_UninitializedType* tmp;
	while (uninits) {
		tmp = uninits->next;
		releaseMem(uninits);
		uninits = tmp;
	}
}



/* for debugging */
#if !(defined(NDEBUG) || !defined(KAFFE_VMDEBUG))

}

static
void
printType(const v_Type* t)
{
	const Hjava_lang_Class* type = t->data.class;
	
	dprintf("(%d)", t->tinfo);
	switch(t->tinfo) {
	case TINFO_SYSTEM:
		if (type == TUNSTABLE->data.class) {
			dprintf("TUNSTABLE");
		}
		else if (IS_WIDE(t)) {
			dprintf("TWIDE");
		}
		else {
			dprintf("UNKNOWN SYSTEM TYPE");
		}
		break;
		
	case TINFO_ADDR:
		dprintf("TADDR: %d", t->data.addr);
		break;
		
	case TINFO_PRIMITIVE:
		if (type == TINT->data.class) {
			dprintf("TINT");
		}
		else if (type == TLONG->data.class) {
			dprintf("TLONG");
		}
		else if (type == TFLOAT->data.class) {
			dprintf("TFLOAT");
		}
		else if (type == TDOUBLE->data.class) {
			dprintf("TDOUBLE");
		}
		else {
			dprintf("UKNOWN PRIMITIVE TYPE");
		}
		break;
		
	case TINFO_SIG:
		dprintf("%s", t->data.sig);
		break;
		
	case TINFO_NAME:
		dprintf("%s", t->data.name);
		break;
		
	case TINFO_CLASS:
		if (type == NULL) {
			dprintf("NULL");
		}
		else if (IS_NULL(t)) {
			dprintf("TNULL");
		}
		
		else if (type == TCHARARR->data.class) {
			dprintf("TCHARARR");
		}
		else if (type == TBOOLARR->data.class) {
			dprintf("TBOOLARR");
		}
		else if (type == TBYTEARR->data.class) {
			dprintf("TBYTEARR");
		}
		else if (type == TSHORTARR->data.class) {
			dprintf("TSHORTARR");
		}
		else if (type == TINTARR->data.class) {
			dprintf("TINTARR");
		}
		else if (type == TLONGARR->data.class) {
			dprintf("TLONGARR");
		}
		else if (type == TFLOATARR->data.class) {
			dprintf("TFLOATARR");
		}
		else if (type == TDOUBLEARR->data.class) {
			dprintf("TDOUBLEARR");
		}
		else if (type == TOBJARR->data.class) {
			dprintf("TOBJARR");
		}
		else {
			if (type->name == NULL || CLASS_CNAME(type) == NULL) {
				dprintf("<NULL NAME>");
			} else {
				dprintf("%s", CLASS_CNAME(type));
			}
		}
		break;
		
	case TINFO_UNINIT:
	case TINFO_UNINIT_SUPER:
		printType(&(t->data.uninit->type));
		break;
		
	default:
		dprintf("UNRECOGNIZED TINFO");
		break;
	}
}


/*
 * printBlock()
 *    For debugging.  Prints out a basic block.
 */
static
void
printBlock(const Method* method, const v_BasicBlock* binfo, const char* indent)
{
	w_word n;
	
	dprintf("%slocals:\n", indent);
	for (n = 0; n < method->localsz; n++) {
		dprintf("%s    %d: ", indent, n);
		printType(&binfo->locals[n]);
		dprintf("\n");
	}
	dprintf("%sopstack (%d):\n", indent, binfo->stacksz);
	for (n = 0; n < method->stacksz; n++) {
		dprintf("%s    %d: ", indent, n);
		printType(&binfo->opstack[n]);
		dprintf("\n");
	}
}

#endif
#endif

