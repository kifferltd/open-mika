/**************************************************************************
* Copyright (c) 2021 by KIFFER Ltd. All rights reserved.                  *
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

#ifdef USE_LIBFFI
#include <ffi.h>
#endif

#include "core-classes.h"
#include "exception.h"
#include "heap.h"
#include "jni.h"
#include "methods.h"
#include "mika_threads.h"
#include "wonka.h"

typedef w_long (w_fun)(w_thread, w_instance, ...);

#ifdef USE_LIBFFI
void *getactual(w_slot *slotloc, w_clazz c, jvalue **jvalptrloc) {
  w_slot slot = *slotloc;
  jvalue *jvalptr = *jvalptrloc;

  if (clazzIsPrimitive(c)) {
    switch (c->type & 0x0f) {
      case VM_TYPE_SHORT:
      case VM_TYPE_INT:
      case VM_TYPE_BYTE:
      case VM_TYPE_CHAR:
      case VM_TYPE_FLOAT:
      case VM_TYPE_BOOLEAN:
        {
          jvalptr->i = slot->c;
          woempa(1, "  word parameter: %08x\n", jvalptr->i);
          *slotloc = slot + 1;
          *jvalptrloc = jvalptr + 1;
          return jvalptr;
        }

      case VM_TYPE_LONG:
        {
          memcpy(&jvalptr->j, (const void*)&slot->c, 4);
          ++slot;
          memcpy((char*)&jvalptr->j + 4, (const void*)&slot->c, 4);
          woempa(1, "  long parameter: %0%08x\n", jvalptr->j >> 32, jvalptr->j & 0x0ffffffffULL);
          *slotloc = slot + 1;
          *jvalptrloc = jvalptr + 1;
          return jvalptr;
        }

      case VM_TYPE_DOUBLE:
        {
          memcpy(&jvalptr->d, (const void*)&slot->c, 4);
          ++slot;
          memcpy((char*)&jvalptr->d + 4, (const void*)&slot->c, 4);
          woempa(1, "  double parameter: %0%08x\n", jvalptr->d >> 32, jvalptr->d & 0x0ffffffffULL);
          *slotloc = slot + 1;
          *jvalptrloc = jvalptr + 1;
          return jvalptr;
        }

      default:
        wabort(ABORT_WONKA, "Coddling catfish! Arg of unknown VM_TYPE %02x", c->type); 
        return NULL;
    }
  }
  else {
    jvalptr->l = (w_instance)slot->c;
    woempa(1, "  instance parameter: %p %j\n", jvalptr->l, jvalptr->l);
    *slotloc = slot + 1;
          *jvalptrloc = jvalptr + 1;
          return jvalptr;
  }
}
#endif

w_long _call_static(w_thread thread, w_instance theClass, w_slot top, w_method m) {
#ifdef USE_LIBFFI
  ffi_cif *cifptr;
  void *actuals[m->exec.nargs + 2];
  jvalue jvalues[m->exec.nargs + 2];
  w_long retval;
  w_slot nextparm;
  jvalue *nextjvalue;
  int i;

  cifptr = m->exec.cif;
  nextparm = top - m->exec.arg_i;
  nextjvalue = jvalues;

  actuals[0] = &thread;
  actuals[1] = &theClass;
  for (i = 0; i < m->exec.nargs; ++i) {
    w_clazz c = m->spec.arg_types[i];
    actuals[i + 2] = getactual(&nextparm, c, &nextjvalue);
  }

//printf("fun = %p retval ptr = %p acuals = %p -> [%p, %p, ...]\n", m->exec.function.void_fun, &retval, actuals, actuals[0], actuals[1]);
  ffi_call(cifptr, m->exec.function.void_fun, &retval, actuals);

  return retval;

#else
  w_fun *f = (w_fun*)m->exec.function.long_fun;

  switch (m->exec.arg_i) {
  case 0: 
      return f(thread, theClass);

  case 1: 
      return f(thread, theClass, top[-1].c);

  case 2: 
      return f(thread, theClass, top[-2].c, top[-1].c);

  case 3: 
      return f(thread, theClass, top[-3].c, top[-2].c, top[-1].c);

  case 4: 
      return f(thread, theClass, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 5: 
      return f(thread, theClass, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 6: 
      return f(thread, theClass, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 7: 
      return f(thread, theClass, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 8: 
      return f(thread, theClass, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 9: 
      return f(thread, theClass, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 10: 
      return f(thread, theClass, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 11: 
      return f(thread, theClass, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 12: 
      return f(thread, theClass, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 13: 
      return f(thread, theClass, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 14: 
      return f(thread, theClass, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 15: 
      return f(thread, theClass, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 16: 
      return f(thread, theClass, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 17: 
      return f(thread, theClass, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 18: 
      return f(thread, theClass, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 19: 
      return f(thread, theClass, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 20: 
      return f(thread, theClass, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 21: 
      return f(thread, theClass, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 22: 
      return f(thread, theClass, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 23: 
      return f(thread, theClass, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 24: 
      return f(thread, theClass, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 25: 
      return f(thread, theClass, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 26: 
      return f(thread, theClass, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 27: 
      return f(thread, theClass, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 28: 
      return f(thread, theClass, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 29: 
      return f(thread, theClass, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 30: 
      return f(thread, theClass, top[-30].c, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 31: 
      return f(thread, theClass, top[-31].c, top[-30].c, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  default: 
    {
      w_long dummy;

      throwException(thread, clazzVirtualMachineError, "Too many parameters in call to native static method (max. 31 in this build)");
      dummy = 0LL;

      return dummy;
    }
  }
#endif
}

w_long _call_instance(w_thread thread, w_slot top, w_method m) {
#ifdef USE_LIBFFI
  ffi_cif *cifptr;
  void *actuals[m->exec.nargs + 2];
  jvalue jvalues[m->exec.nargs + 2];
  w_long retval;
  w_slot nextparm;
  jvalue *nextjvalue;
  int i;

  cifptr = m->exec.cif;
  nextparm = top - m->exec.arg_i;
  nextjvalue = jvalues;

  woempa(1, "instance method %m has %d parameters\n", m, m->exec.nargs);
  actuals[0] = &thread;
  woempa(1, "calling thread = %t\n", thread);
  actuals[1] = &nextparm->c;
  woempa(1, "instance = %j\n", nextparm->c);
  nextparm++;
  for (i = 0; i < m->exec.nargs; ++i) {
    w_clazz c = m->spec.arg_types[i];
    actuals[i + 2] = getactual(&nextparm, c, &nextjvalue);
  }

//printf("fun = %p retval ptr = %p actuals = %p -> [%p, %p, ...]\n", m->exec.function.void_fun, &retval, actuals, actuals[0], actuals[1]);
  ffi_call(cifptr, m->exec.function.void_fun, &retval, actuals);

  return retval;

#else
  w_fun *f = (w_fun*)m->exec.function.long_fun;

  switch (m->exec.arg_i) {
  case 1: 
      return f(thread, (w_instance)top[-1].c);

  case 2: 
      return f(thread, (w_instance)top[-2].c, top[-1].c);

  case 3: 
      return f(thread, (w_instance)top[-3].c, top[-2].c, top[-1].c);

  case 4: 
      return f(thread, (w_instance)top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 5: 
      return f(thread, (w_instance)top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 6: 
      return f(thread, (w_instance)top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 7: 
      return f(thread, (w_instance)top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 8: 
      return f(thread, (w_instance)top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 9: 
      return f(thread, (w_instance)top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 10: 
      return f(thread, (w_instance)top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 11: 
      return f(thread, (w_instance)top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 12: 
      return f(thread, (w_instance)top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 13: 
      return f(thread, (w_instance)top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 14: 
      return f(thread, (w_instance)top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 15: 
      return f(thread, (w_instance)top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 16: 
      return f(thread, (w_instance)top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 17: 
      return f(thread, (w_instance)top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 18: 
      return f(thread, (w_instance)top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 19: 
      return f(thread, (w_instance)top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 20: 
      return f(thread, (w_instance)top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 21: 
      return f(thread, (w_instance)top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 22: 
      return f(thread, (w_instance)top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 23: 
      return f(thread, (w_instance)top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 24: 
      return f(thread, (w_instance)top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 25: 
      return f(thread, (w_instance)top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 26: 
      return f(thread, (w_instance)top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 27: 
      return f(thread, (w_instance)top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 28: 
      return f(thread, (w_instance)top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 29: 
      return f(thread, (w_instance)top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 30: 
      return f(thread, (w_instance)top[-30].c, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  case 31: 
      return f(thread, (w_instance)top[-31].c, top[-30].c, top[-29].c, top[-28].c, top[-27].c, top[-26].c, top[-25].c, top[-24].c, top[-23].c, top[-22].c, top[-21].c, top[-20].c, top[-19].c, top[-18].c, top[-17].c, top[-16].c, top[-15].c, top[-14].c, top[-13].c, top[-12].c, top[-11].c, top[-10].c, top[-9].c, top[-8].c, top[-7].c, top[-6].c, top[-5].c, top[-4].c, top[-3].c, top[-2].c, top[-1].c);

  default: 
    {
      w_long dummy;

      throwException(thread, clazzVirtualMachineError, "Too many parameters in call to native instance method (max. 30 in this build)");
      dummy = 0LL;

      return dummy;

    }
  }
#endif
}


