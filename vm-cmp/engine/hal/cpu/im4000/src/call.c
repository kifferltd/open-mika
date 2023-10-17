/**************************************************************************
* Copyright (c) 2023 by KIFFER Ltd. All rights reserved.                  *
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
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#include "methods.h"
#include "mika_stack.h"
#include "wonka.h"

void callMethod(w_frame caller, w_method method) {

  woempa(1, "CALLING %M, dispatcher is %p\n", method, method->exec.dispatcher);
  // TODO check for stack overflow?

  // TODO we need to distinguish between native and Java, synchronized and not, etc.
  // method->exec.dispatcher(caller, method);

  w_thread thread = currentWonkaThread;
  w_u64 result;
  w_word *args = allocMem(method->exec.arg_i * sizeof(w_word));
  w_Slot *tos = (w_Slot*)caller->jstack_top;
  for (w_size i = 0; i < method->exec.arg_i; ++i) {
    args[i] = GET_SLOT_CONTENTS(--tos);
  }
  activate_frame(method, method->exec.arg_i, args, &result);
  releaseMem(args);

  caller->jstack_top -= method->exec.arg_i;
  // TODO assuming that we do not come here if an exception was thrown
  int n = 0;
  switch(method->exec.return_i) {
    case 2:
      SET_SLOT_CONTENTS(caller->jstack_top++, result.words[n++]);
      // fall through

    case 1:
      SET_SLOT_CONTENTS(caller->jstack_top++, result.words[n++]);
      // fall through

    case 0:
      caller->jstack_top = tos + n;
      break;

    default:
      wabort(ABORT_WONKA, "Impossible exec.return_i value : %d\n", method->exec.return_i);
  }
  thread->top = caller;

  woempa(1, "RETURNED from %M\n", method);
}

// referenced by loading.c:1909 (/home/chris/Imsys/env-isal/sw-open-mika/core-vm/src/vm/loading.c:1909)
// referenced by loading.c:2062 (/home/chris/Imsys/env-isal/sw-open-mika/core-vm/src/vm/loading.c:2062)
// referenced by reflection.c:553 (/home/chris/Imsys/env-isal/sw-open-mika/core-vm/src/vm/reflection.c:553)
// referenced 9 more times
w_frame pushFrame(w_thread thread, w_method method) {

  w_frame frame;

  /*
  ** Add at least 2 slots for an eventual 64 bit return value.
  */

  frame = allocClearedMem(sizeof(w_Frame));
//  frame->jstack_base = allocClearedMem(sizeof(w_Slot) * (method->exec.arg_i + method->exec.stack_i + 2));

  woempa(1,"Calling %M from %t using frame at %p (previous at %p)\n", method, thread, frame, thread->top);

  if (frame) {
    frame->label = "frame";
    frame->jstack_base = thread->top->jstack_top; 
    frame->jstack_top = frame->jstack_base;
    frame->auxstack_base = thread->top->auxstack_top; 
    frame->auxstack_top = frame->auxstack_base;
    frame->previous = thread->top;
    frame->thread = thread;
    frame->method = method;
    frame->current = method->exec.code;
    frame->flags = isSet(method->flags, ACC_NATIVE) ? FRAME_NATIVE : 0;
#ifdef TRACE_CLASSLOADERS
  { 
   // N.B. If method is virtual then the caller should overwrite the static udcl
   // with the appropriate runtime one ...
    w_instance loader = method->spec.declaring_clazz->loader;
    if (loader && !getBooleanField(loader, F_ClassLoader_systemDefined)) {
      frame->udcl = loader;
    }
    else {
      frame->udcl = thread->top->udcl;
    }
  }
#endif

    thread->top = frame;
    woempa(1, "%t jstack_base = jstack_top = %p, auxstack_base = auxstack_top = %p\n", frame->jstack_top, frame->auxstack_top);
  }

  return frame;

}

// referenced by loading.c:1798 (/home/chris/Imsys/env-isal/sw-open-mika/core-vm/src/vm/loading.c:1798)
// referenced by Class.c:246 (/home/chris/Imsys/env-isal/sw-open-mika/core-vm/src/native/java/lang/Class.c:246)
// referenced by BufferedReader.c:92 (/home/chris/Imsys/env-isal/sw-open-mika/core-vm/src/native/java/io/BufferedReader.c:92)
// referenced 2 more times
w_frame activateFrame(w_thread thread, w_method method, w_word flags, w_int nargs, ...) {

  va_list args;
  w_frame frame = pushFrame(thread, method);
  w_int i = 0;

  threadMustBeSafe(thread);
  if (frame) {
    va_start(args, nargs);
    while (i < nargs) {
      // [CG 20230531] Doing it this way so that the slot-type arguments will be consumed even if they are not used.
      w_word contents = va_arg(args, w_word);
      w_word scanning = va_arg(args, w_word);
      //
      SET_SLOT_CONTENTS(frame->jstack_top, contents);
      SET_SLOT_SCANNING(frame->jstack_top, scanning);
      frame->jstack_top += 1;
      i += 1;
    }
    va_end(args);

    frame->flags |= flags;
    callMethod(frame, method);
  }
  
  return frame;

}

// referenced by loading.c:1803 (/home/chris/Imsys/env-isal/sw-open-mika/core-vm/src/vm/loading.c:1803)
// referenced by loading.c:1919 (/home/chris/Imsys/env-isal/sw-open-mika/core-vm/src/vm/loading.c:1919)
// referenced by loading.c:2072 (/home/chris/Imsys/env-isal/sw-open-mika/core-vm/src/vm/loading.c:2072)
// referenced 15 more times
void deactivateFrame(w_frame frame, w_instance protect) {

  w_frame newtop = frame->previous;
  w_thread thread = frame->thread;

  thread->protected = protect;
  thread->top = newtop;

//  releaseMem(frame->jstack_base);
  releaseMem(frame);
}


// dummies to keep the linker happy
// TODO deal with these properly

// referenced by debug.c:434 (/home/chris/Imsys/env-isal/sw-open-mika/core-vm/src/misc/debug.c:434)
volatile w_thread jitting_thread = NULL;

// referenced by dispatcher.c:223 (/home/chris/Imsys/env-isal/sw-open-mika/core-vm/src/vm/dispatcher.c:223)
// referenced by dispatcher.c:251 (/home/chris/Imsys/env-isal/sw-open-mika/core-vm/src/vm/dispatcher.c:251)
// referenced by dispatcher.c
void interpret(w_frame caller, w_method method) {
}



