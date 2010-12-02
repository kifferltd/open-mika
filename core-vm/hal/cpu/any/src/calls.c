/**************************************************************************
* Copyright (c) 2004 by Chris Gray, /k/ Embedded Java Solutions.          *
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

#ifdef USE_LIBFFI
#include <ffi.h>
#endif

#include "core-classes.h"
#include "exception.h"
#include "heap.h"
#include "jni.h"
#include "methods.h"
#include "threads.h"
#include "wonka.h"

typedef w_long (w_fun)(JNIEnv*, w_instance, ...);

#ifdef USE_LIBFFI
void *getactual(w_slot *slotloc, w_clazz c) {
  woempa(7, "  parameter type = %k\n", c);
  w_slot slot = *slotloc;
  woempa(7, "  slot = %p\n", slot);

  if (clazzIsPrimitive(c)) {
    switch (c->type & 0x0f) {
      case VM_TYPE_SHORT:
      case VM_TYPE_INT:
      case VM_TYPE_BYTE:
      case VM_TYPE_CHAR:
      case VM_TYPE_FLOAT:
      case VM_TYPE_BOOLEAN:
        {
          w_word *wordptr = allocMem(sizeof(w_word));
          *wordptr = slot->c;
          woempa(7, "  word parameter: %08x\n", *wordptr);
          *slotloc = slot + 1;
          return wordptr;
        }

      case VM_TYPE_LONG:
      case VM_TYPE_DOUBLE:
        {
          void *dwordptr = allocMem(sizeof(w_dword));
          memcpy(dwordptr, &slot->c, 4);
          ++slot;
          memcpy((char*)dwordptr + 4, &slot->c, 4);
          //woempa(7, "  dword parameter: %08x%08x\n", u.w[WORD_MSW], u.w[WORD_LSW]);
          woempa(7, "  dword parameter: %0%08x\n", (*(w_dword*)dwordptr) >> 32, (*(w_dword*)dwordptr) & 0x0ffffffffULL);
          *slotloc = slot + 1;
          return dwordptr;
        }

      default:
        wabort(ABORT_WONKA, "Coddling catfish! Arg of unknown VM_TYPE %02x", c->type); 
        return NULL;
    }
  }
  else {
    w_instance *instanceptr = allocMem(sizeof(w_instance));
    *instanceptr = (w_instance)slot->c;
    woempa(7, "  instance parameter: %p %j\n", *instanceptr, *instanceptr);
    *slotloc = slot + 1;
    return instanceptr;
  }
}
#endif

w_long _call_static(JNIEnv* env, w_instance theClass, w_slot top, w_method m) {
#ifdef USE_LIBFFI
  ffi_cif *cifptr;
  void *actuals[m->exec.nargs + 2];
  w_long retval;
  w_slot nextparm;
  int i;

  cifptr = m->exec.cif;
  nextparm = top - m->exec.arg_i;
//printf("parms start at %p\n", nextparm);

  actuals[0] = &env;
  actuals[1] = &theClass;
  for (i = 0; i < m->exec.nargs; ++i) {
    w_clazz c = m->spec.arg_types[i];
    actuals[i + 2] = getactual(&nextparm, c);
  }

//printf("fun = %p retval ptr = %p acuals = %p -> [%p, %p, ...]\n", m->exec.function.long_fun, &retval, actuals, actuals[0], actuals[1]);
  ffi_call(cifptr, m->exec.function.long_fun, &retval, actuals);

  return retval;

#else
  w_fun *f = (w_fun*)m->exec.function.long_fun;

  switch (m->exec.arg_i) {
  case 0: 
      return f(env, theClass);

  case 1: 
      return f(env, theClass, top[-1].c);

  case 2: 
      return f(env, theClass, top[-2].c, top[-1].c);

  case 3: 
      return f(env, theClass, top[-3].c, top[-2].c, top[-1].c);

  case 4: 
      return f(env, theClass, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 5: 
      return f(env, theClass, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 6: 
      return f(env, theClass, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 7: 
      return f(env, theClass, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 8: 
      return f(env, theClass, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 9: 
      return f(env, theClass, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 10: 
      return f(env, theClass, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 11: 
      return f(env, theClass, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 12: 
      return f(env, theClass, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 13: 
      return f(env, theClass, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 14: 
      return f(env, theClass, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 15: 
      return f(env, theClass, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 16: 
      return f(env, theClass, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 17: 
      return f(env, theClass, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 18: 
      return f(env, theClass, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 19: 
      return f(env, theClass, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 20: 
      return f(env, theClass, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 21: 
      return f(env, theClass, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 22: 
      return f(env, theClass, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 23: 
      return f(env, theClass, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 24: 
      return f(env, theClass, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 25: 
      return f(env, theClass, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 26: 
      return f(env, theClass, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 27: 
      return f(env, theClass, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 28: 
      return f(env, theClass, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 29: 
      return f(env, theClass, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 30: 
      return f(env, theClass, top[-30].c, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 31: 
      return f(env, theClass, top[-31].c, top[-30].c, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  default: 
    {
      w_long dummy;

      throwException(JNIEnv2w_thread(env), clazzVirtualMachineError, "Too many parameters in call to native static method (max. 31 in this build)");
      dummy = 0LL;

      return dummy;
    }
  }
#endif
}

w_long _call_instance(JNIEnv* env, w_slot top, w_method m) {
#ifdef USE_LIBFFI
  ffi_cif *cifptr;
  void *actuals[m->exec.nargs + 2];
  w_long retval;
  w_slot nextparm;
  int i;

  cifptr = m->exec.cif;
  nextparm = top - m->exec.arg_i;

  woempa(7, "instance method %m has %d parameters\n", m, m->exec.nargs);
  actuals[0] = &env;
  woempa(7, "calling thread = %t\n", env);
  actuals[1] = &nextparm->c;
  woempa(7, "instance = %j\n", nextparm->c);
  nextparm++;
  for (i = 0; i < m->exec.nargs; ++i) {
    w_clazz c = m->spec.arg_types[i];
    actuals[i + 2] = getactual(&nextparm, c);
  }

//printf("fun = %p retval ptr = %p actuals = %p -> [%p, %p, ...]\n", m->exec.function.long_fun, &retval, actuals, actuals[0], actuals[1]);
  ffi_call(cifptr, m->exec.function.long_fun, &retval, actuals);

  return retval;

#else
  w_fun *f = (w_fun*)m->exec.function.long_fun;

  switch (m->exec.arg_i) {
  case 1: 
      return f(env, (w_instance)top[-1].c);

  case 2: 
      return f(env, (w_instance)top[-2].c, top[-1].c);

  case 3: 
      return f(env, (w_instance)top[-3].c, top[-2].c, top[-1].c);

  case 4: 
      return f(env, (w_instance)top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 5: 
      return f(env, (w_instance)top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 6: 
      return f(env, (w_instance)top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 7: 
      return f(env, (w_instance)top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 8: 
      return f(env, (w_instance)top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 9: 
      return f(env, (w_instance)top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 10: 
      return f(env, (w_instance)top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 11: 
      return f(env, (w_instance)top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 12: 
      return f(env, (w_instance)top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 13: 
      return f(env, (w_instance)top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 14: 
      return f(env, (w_instance)top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 15: 
      return f(env, (w_instance)top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 16: 
      return f(env, (w_instance)top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 17: 
      return f(env, (w_instance)top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 18: 
      return f(env, (w_instance)top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 19: 
      return f(env, (w_instance)top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 20: 
      return f(env, (w_instance)top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 21: 
      return f(env, (w_instance)top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 22: 
      return f(env, (w_instance)top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 23: 
      return f(env, (w_instance)top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 24: 
      return f(env, (w_instance)top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 25: 
      return f(env, (w_instance)top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 26: 
      return f(env, (w_instance)top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 27: 
      return f(env, (w_instance)top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 28: 
      return f(env, (w_instance)top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 29: 
      return f(env, (w_instance)top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 30: 
      return f(env, (w_instance)top[-30].c, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 31: 
      return f(env, (w_instance)top[-31].c, top[-30].c, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  default: 
    {
      w_long dummy;

      throwException(JNIEnv2w_thread(env), clazzVirtualMachineError, "Too many parameters in call to native instance method (max. 30 in this build)");
      dummy = 0LL;

      return dummy;

    }
  }
#endif
}


