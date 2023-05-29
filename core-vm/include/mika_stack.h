/**************************************************************************
* Copyright (c) 2023 by Chris Gray, KIFFER Ltd. All rights reserved.      *
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
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

#ifndef HAVE_MIKA_STACK_H
#define HAVE_MIKA_STACK_H

// If TRACE_CLASSLOADERS is defined, we keep track of the nearest enclosing
// user-defined class loader in each frame.
#if (defined(DEBUG) || defined(RESMON))
#ifndef TRACE_CLASSLOADERS
#define TRACE_CLASSLOADERS
#endif
#endif

/*
 * Each slot consists of two words: the slot contents (c) and the slot data type (s).
 */
typedef struct w_Slot {
  w_word c;
#ifndef _USE_OBJECT_HASHTABLE
  w_word s;
#endif
} w_Slot;

/**
 * Set the contents of a stack slot.
 * @param slotptr x_slot pointer to the w_Slot.
 * @param contents w_word the data to be stored in the slot.
*/
#define SET_SLOT_CONTENTS(slotptr,contents) (slotptr)->c = (contents)

/**
 * Set the type of a stack slot.
 * @param slotptr x_slot pointer to the w_Slot.
 * @param scan    w_word the data to be stored in the slot.
 * @param slot... if USE_OBJECT_HASHTABLE is set, not used and may be omitted (hence the ...).
 *                Otherwise, one of slot_noscan, slot_scan, or a pointer to a x_Monitor.
 */
#ifdef _USE_OBJECT_HASHTABLE
#define SET_SLOT_SCANNING(slotptr,scan...)
#else
#define SET_SLOT_SCANNING(slotptr,scan...) (slotptr)->s = (scan)
#endif

/**
 * Write an object reference into a w_Slot. 
 * If USE_OBJECT_HASHTABLE is set, the reference is simply written to the slot.
 * Otherwise, the slot type is set to slot_scan after the reference is written into the slot. 
 * @param slotptr x_slot pointer to the w_Slot.
 * @param contents w_word the data to be stored in the slot.
 */
#define SET_REFERENCE_SLOT(slotptr,contents) SET_SLOT_CONTENTS((slotptr),(contents));\
        SET_SLOT_SCANNING((slotptr),slot_trace)

/**
 * Write a value which is not an object reference into a w_Slot. 
 * If USE_OBJECT_HASHTABLE is set, the value is simply written to the slot.
 * Otherwise, the slot type is set to slot_noscan before the value is written into the slot.
 * @param slotptr x_slot pointer to the w_Slot.
 * @param contents w_word the data to be stored in the slot.
 */
#define SET_SCALAR_SLOT(slotptr,contents) SET_SLOT_SCANNING((slotptr),slot_notrace);\
        SET_SLOT_CONTENTS((slotptr),(contents))

/*
 * For the flags which a frame can have, see the WT__xxx symbols above.
 * The label of a frame is ASCII "frame", optionally followed by more chars.
 *
 * jstack_base points to the start of the Java stack for this method (often
 * local variable #0 = 'this'), while jstack_top points to the location where
 * the next push would occur.
 */
typedef struct w_Frame {
  volatile w_word flags;               // Flags
  char *label;
  w_slot jstack_base;                  // Array of slots for locals, stack, and return value
  volatile w_slot auxstack_top;        // push ==> *top->c = c; *top->s = s; top -= 1; pop ==> l->c = top[+1].c; l->s = top[+1].s; top += 1;
  w_slot auxstack_base;                  // Array of slots for locals, stack, and return value
  volatile w_slot jstack_top;          // push ==> *top->c = c; *top->s = s; top += 1; pop ==> l->c = top[-1].c; l->s = top[-1].s; top -= 1;
  w_frame         previous;            // points to caller or arguments stub frame
  volatile w_method method;            // points to method that this frame refers to, when NULL it's a stub frame
  w_thread        thread;              // points to current wonka thread
  volatile w_code current;             // The opcode pointer at method call or exception; pc = frame->current - frame->method_.exec.code
  volatile w_instance * map;           // A pointer to an array of references (stack map)
#ifdef TRACE_CLASSLOADERS
  w_instance      udcl;                // Nearest user-defined class loader or NULL
#endif
} w_Frame;

/*
** Tag symbols for the main and auxiliary stack.
** Note that when a tag is greater than 'stack_trace', the symbol contains
** the address of the monitor that is used to lock the object!
** Use the isMonitoredSlot function to check whether a slot contains
** a monitored object.
*/

static const w_word stack_notrace   = 0; // The stack item does not refer to an object that needs GC tracing; must be 0!
static const w_word stack_trace     = 1; // Refers to an object that needs GC tracing, main stack and auxillary stack.

#ifdef _USE_OBJECT_HASHTABLE
TODO - look up in locks hashtable?
#else
#define isMonitoredSlot(slot) ((slot)->s > stack_trace)
#endif

/*
 * Pointer to the last slot (auxstack_base of the root frame).
 */
#define last_slot(t) ((t)->slots + (SLOTS_PER_THREAD) - 1)

void addLocalReference(w_thread thread, w_instance instance);
void pushLocalReference(w_frame frame, w_instance instance);
void popLocalReference(w_frame frame);
void pushMonitoredReference(w_frame frame, w_instance instance, x_monitor monitor);
void removeLocalReference(w_thread thread, w_instance instance);

/*
** Copy a double value from two adjacent stack slots to a variable in memory.
** The order of the two slots in memory will determine the order of the two
** 32-bit halves of the variable as it is stored.
*/ 
INLINE static w_double slots2w_double(const w_slot first) {
  union{w_double d; w_word w[2];} two_words;
  two_words.w[0] = first[0].c;
  two_words.w[1] = first[1].c;
  return two_words.d;
}

/*
** Copy a long value from two adjacent stack slots to a variable in memory.
** The order of the two slots in memory will determine the order of the two
** 32-bit halves of the variable as it is stored.
*/ 
INLINE static w_long slots2w_long(const w_slot first) {
  union{w_long j; w_word w[2];} two_words;
  two_words.w[0] = first[0].c;
  two_words.w[1] = first[1].c;
  return two_words.j;
}

/*
** Copy a double value from a variable in memory to two adjacent stack slots.
** The order of the two slots in memory will be the same as the two 32-bit 
** halves of the variable as it was stored.
*/ 
INLINE static void w_double2slots(const w_double value, const w_slot first) {
    union{w_double d; w_word w[2];} two_words;
    two_words.d = value;
    SET_SLOT_SCANNING(first, stack_notrace);
    SET_SLOT_CONTENTS(first, two_words.w[0]);
    SET_SLOT_SCANNING(first+1, stack_notrace);
    SET_SLOT_CONTENTS(first+1, two_words.w[1]);
}

/*
** Copy a long value from a variable in memory to two adjacent stack slots.
** The order of the two slots in memory will be the same as the two 32-bit 
** halves of the variable as it was stored.
*/ 
INLINE static void w_long2slots(const w_long value, const w_slot first) {
    union{w_long j; w_word w[2];} two_words;
    two_words.j = value;
    SET_SLOT_SCANNING(first, stack_notrace);
    SET_SLOT_CONTENTS(first, two_words.w[0]);
    SET_SLOT_SCANNING(first+1, stack_notrace);
    SET_SLOT_CONTENTS(first+1, two_words.w[1]);
}

/*
* Stack frame flags
*/

#define FRAME_NATIVE        0x00000001   /* Frame is a host frame for a native method */
#define FRAME_JNI           0x00000002   /* Frame is used in a JNI call */
#define FRAME_LOADING       0x00000004   /* Frame built to invoke classloader */
#define FRAME_CLINIT        0x00000008   /* Frame is used to run a <clinit> method  */
#define FRAME_REFLECTION    0x00000010   /* Frame is used in reflection invocation */
#define FRAME_ROOT          0x00000020   /* Frame is the root frame of a thread */
#define FRAME_PRIVILEGED    0x00000040   /* Frame was built using doPrivileged */
#define FRAME_STACKMAP      0x00000080   /* Frame has stack map  */

void callMethod(w_frame arguments, w_method method);

/**
** Get the security domain associated with a frame.
** Note that for the time being we simply ignore native code.
** Steven, you may need to adapt this.
*/
#define frame2domain(f) getReferenceField(clazz2Class((f)->method->clazz), F_Class_domain))


#endif // HAVE_MIKA_STACK_H
