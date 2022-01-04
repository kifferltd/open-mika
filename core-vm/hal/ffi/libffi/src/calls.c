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

#include <ffi.h>

#include "core-classes.h"
#include "exception.h"
#include "heap.h"
#include "jni.h"
#include "methods.h"
#include "mika_threads.h"
#include "wonka.h"

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

w_long _call_static(w_thread thread, w_instance theClass, w_slot top, w_method m) {
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
}

w_long _call_instance(w_thread thread, w_slot top, w_method m) {
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
}



