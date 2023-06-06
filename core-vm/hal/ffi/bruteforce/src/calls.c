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

#include "core-classes.h"
#include "exception.h"
#include "heap.h"
#include "jni.h"
#include "methods.h"
#include "mika_threads.h"
#include "wonka.h"

w_word _call_static_32bits(w_thread thread, w_instance theClass, w_slot top, w_method m) {
  switch (m->exec.arg_i) {
  case 0: 
   {
     typedef w_word (w_sfun_0)(w_thread, w_instance);
     w_sfun_0 *sf0 = (w_sfun_0*)m->exec.function.word_fun;
     return sf0(thread, theClass);
   }

  case 1: 
   {
     typedef w_word (w_sfun_1)(w_thread, w_instance , w_word);
     w_sfun_1 *sf1 = (w_sfun_1*)m->exec.function.word_fun;
     return sf1(thread, theClass, GET_SLOT_CONTENTS(top - 1));
   }

  case 2: 
   {
     typedef w_word (w_sfun_2)(w_thread, w_instance , w_word, w_word);
     w_sfun_2 *sf2 = (w_sfun_2*)m->exec.function.word_fun;
     return sf2(thread, theClass, GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 3: 
   {
     typedef w_word (w_sfun_3)(w_thread, w_instance , w_word, w_word, w_word);
     w_sfun_3 *sf3 = (w_sfun_3*)m->exec.function.word_fun;
     return sf3(thread, theClass, GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 4: 
   {
     typedef w_word (w_sfun_4)(w_thread, w_instance , w_word, w_word, w_word, w_word);
     w_sfun_4 *sf4 = (w_sfun_4*)m->exec.function.word_fun;
     return sf4(thread, theClass, GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 5: 
   {
     typedef w_word (w_sfun_5)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word);
     w_sfun_5 *sf5 = (w_sfun_5*)m->exec.function.word_fun;
     return sf5(thread, theClass, GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 6: 
   {
     typedef w_word (w_sfun_6)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_6 *sf6 = (w_sfun_6*)m->exec.function.word_fun;
     return sf6(thread, theClass, GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 7: 
   {
     typedef w_word (w_sfun_7)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_7 *sf7 = (w_sfun_7*)m->exec.function.word_fun;
     return sf7(thread, theClass, GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 8: 
   {
     typedef w_word (w_sfun_8)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_8 *sf8 = (w_sfun_8*)m->exec.function.word_fun;
     return sf8(thread, theClass, GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 9: 
   {
     typedef w_word (w_sfun_9)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_9 *sf9 = (w_sfun_9*)m->exec.function.word_fun;
     return sf9(thread, theClass, GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 10: 
   {
     typedef w_word (w_sfun_10)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_10 *sf10 = (w_sfun_10*)m->exec.function.word_fun;
     return sf10(thread, theClass, GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 11: 
   {
     typedef w_word (w_sfun_11)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_11 *sf11 = (w_sfun_11*)m->exec.function.word_fun;
     return sf11(thread, theClass, GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 12: 
   {
     typedef w_word (w_sfun_12)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_12 *sf12 = (w_sfun_12*)m->exec.function.word_fun;
     return sf12(thread, theClass, GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 13: 
   {
     typedef w_word (w_sfun_13)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_13 *sf13 = (w_sfun_13*)m->exec.function.word_fun;
     return sf13(thread, theClass, GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 14: 
   {
     typedef w_word (w_sfun_14)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_14 *sf14 = (w_sfun_14*)m->exec.function.word_fun;
     return sf14(thread, theClass, GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 15: 
   {
     typedef w_word (w_sfun_15)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_15 *sf15 = (w_sfun_15*)m->exec.function.word_fun;
     return sf15(thread, theClass, GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 16: 
   {
     typedef w_word (w_sfun_16)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_16 *sf16 = (w_sfun_16*)m->exec.function.word_fun;
     return sf16(thread, theClass, GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 17: 
   {
     typedef w_word (w_sfun_17)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_17 *sf17 = (w_sfun_17*)m->exec.function.word_fun;
     return sf17(thread, theClass, GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 18: 
   {
     typedef w_word (w_sfun_18)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_18 *sf18 = (w_sfun_18*)m->exec.function.word_fun;
     return sf18(thread, theClass, GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 19: 
   {
     typedef w_word (w_sfun_19)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_19 *sf19 = (w_sfun_19*)m->exec.function.word_fun;
     return sf19(thread, theClass, GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 20: 
   {
     typedef w_word (w_sfun_20)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_20 *sf20 = (w_sfun_20*)m->exec.function.word_fun;
     return sf20(thread, theClass, GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 21: 
   {
     typedef w_word (w_sfun_21)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_21 *sf21 = (w_sfun_21*)m->exec.function.word_fun;
     return sf21(thread, theClass, GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 22: 
   {
     typedef w_word (w_sfun_22)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_22 *sf22 = (w_sfun_22*)m->exec.function.word_fun;
     return sf22(thread, theClass, GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 23: 
   {
     typedef w_word (w_sfun_23)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_23 *sf23 = (w_sfun_23*)m->exec.function.word_fun;
     return sf23(thread, theClass, GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 24: 
   {
     typedef w_word (w_sfun_24)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_24 *sf24 = (w_sfun_24*)m->exec.function.word_fun;
     return sf24(thread, theClass, GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 25: 
   {
     typedef w_word (w_sfun_25)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_25 *sf25 = (w_sfun_25*)m->exec.function.word_fun;
     return sf25(thread, theClass, GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 26: 
   {
     typedef w_word (w_sfun_26)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_instance, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_26 *sf26 = (w_sfun_26*)m->exec.function.word_fun;
     return sf26(thread, theClass, GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 27: 
   {
     typedef w_word (w_sfun_27)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_27 *sf27 = (w_sfun_27*)m->exec.function.word_fun;
     return sf27(thread, theClass, GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 28: 
   {
     typedef w_word (w_sfun_28)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_28 *sf28 = (w_sfun_28*)m->exec.function.word_fun;
     return sf28(thread, theClass, GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 29: 
   {
     typedef w_word (w_sfun_29)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_29 *sf29 = (w_sfun_29*)m->exec.function.word_fun;
     return sf29(thread, theClass, GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 30: 
   {
     typedef w_word (w_sfun_30)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_30 *sf30 = (w_sfun_30*)m->exec.function.word_fun;
     return sf30(thread, theClass, GET_SLOT_CONTENTS(top - 30), GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 31: 
   {
     typedef w_word (w_sfun_31)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_31 *sf31 = (w_sfun_31*)m->exec.function.word_fun;
     return sf31(thread, theClass, GET_SLOT_CONTENTS(top - 31), GET_SLOT_CONTENTS(top - 30), GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  default: 
    {
      w_word dummy;

      throwException(thread, clazzVirtualMachineError, "Too many parameters in call to native static method (max. 31 in this build)");
      dummy = 0L;

      return dummy;
    }
  }
}

w_long _call_static_64bits(w_thread thread, w_instance theClass, w_slot top, w_method m) {
  switch (m->exec.arg_i) {
  case 0: 
   {
     typedef w_long (w_sfun_0)(w_thread, w_instance);
     w_sfun_0 *sf0 = (w_sfun_0*)m->exec.function.long_fun;
     return sf0(thread, theClass);
   }

  case 1: 
   {
     typedef w_long (w_sfun_1)(w_thread, w_instance , w_word);
     w_sfun_1 *sf1 = (w_sfun_1*)m->exec.function.long_fun;
     return sf1(thread, theClass, GET_SLOT_CONTENTS(top - 1));
   }

  case 2: 
   {
     typedef w_long (w_sfun_2)(w_thread, w_instance , w_word, w_word);
     w_sfun_2 *sf2 = (w_sfun_2*)m->exec.function.long_fun;
     return sf2(thread, theClass, GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 3: 
   {
     typedef w_long (w_sfun_3)(w_thread, w_instance , w_word, w_word, w_word);
     w_sfun_3 *sf3 = (w_sfun_3*)m->exec.function.long_fun;
     return sf3(thread, theClass, GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 4: 
   {
     typedef w_long (w_sfun_4)(w_thread, w_instance , w_word, w_word, w_word, w_word);
     w_sfun_4 *sf4 = (w_sfun_4*)m->exec.function.long_fun;
     return sf4(thread, theClass, GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 5: 
   {
     typedef w_long (w_sfun_5)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word);
     w_sfun_5 *sf5 = (w_sfun_5*)m->exec.function.long_fun;
     return sf5(thread, theClass, GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 6: 
   {
     typedef w_long (w_sfun_6)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_6 *sf6 = (w_sfun_6*)m->exec.function.long_fun;
     return sf6(thread, theClass, GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 7: 
   {
     typedef w_long (w_sfun_7)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_7 *sf7 = (w_sfun_7*)m->exec.function.long_fun;
     return sf7(thread, theClass, GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 8: 
   {
     typedef w_long (w_sfun_8)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_8 *sf8 = (w_sfun_8*)m->exec.function.long_fun;
     return sf8(thread, theClass, GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 9: 
   {
     typedef w_long (w_sfun_9)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_9 *sf9 = (w_sfun_9*)m->exec.function.long_fun;
     return sf9(thread, theClass, GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 10: 
   {
     typedef w_long (w_sfun_10)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_10 *sf10 = (w_sfun_10*)m->exec.function.long_fun;
     return sf10(thread, theClass, GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 11: 
   {
     typedef w_long (w_sfun_11)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_11 *sf11 = (w_sfun_11*)m->exec.function.long_fun;
     return sf11(thread, theClass, GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 12: 
   {
     typedef w_long (w_sfun_12)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_12 *sf12 = (w_sfun_12*)m->exec.function.long_fun;
     return sf12(thread, theClass, GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 13: 
   {
     typedef w_long (w_sfun_13)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_13 *sf13 = (w_sfun_13*)m->exec.function.long_fun;
     return sf13(thread, theClass, GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 14: 
   {
     typedef w_long (w_sfun_14)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_14 *sf14 = (w_sfun_14*)m->exec.function.long_fun;
     return sf14(thread, theClass, GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 15: 
   {
     typedef w_long (w_sfun_15)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_15 *sf15 = (w_sfun_15*)m->exec.function.long_fun;
     return sf15(thread, theClass, GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 16: 
   {
     typedef w_long (w_sfun_16)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_16 *sf16 = (w_sfun_16*)m->exec.function.long_fun;
     return sf16(thread, theClass, GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 17: 
   {
     typedef w_long (w_sfun_17)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_17 *sf17 = (w_sfun_17*)m->exec.function.long_fun;
     return sf17(thread, theClass, GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 18: 
   {
     typedef w_long (w_sfun_18)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_18 *sf18 = (w_sfun_18*)m->exec.function.long_fun;
     return sf18(thread, theClass, GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 19: 
   {
     typedef w_long (w_sfun_19)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_19 *sf19 = (w_sfun_19*)m->exec.function.long_fun;
     return sf19(thread, theClass, GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 20: 
   {
     typedef w_long (w_sfun_20)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_20 *sf20 = (w_sfun_20*)m->exec.function.long_fun;
     return sf20(thread, theClass, GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 21: 
   {
     typedef w_long (w_sfun_21)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_21 *sf21 = (w_sfun_21*)m->exec.function.long_fun;
     return sf21(thread, theClass, GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 22: 
   {
     typedef w_long (w_sfun_22)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_22 *sf22 = (w_sfun_22*)m->exec.function.long_fun;
     return sf22(thread, theClass, GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 23: 
   {
     typedef w_long (w_sfun_23)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_23 *sf23 = (w_sfun_23*)m->exec.function.long_fun;
     return sf23(thread, theClass, GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 24: 
   {
     typedef w_long (w_sfun_24)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_24 *sf24 = (w_sfun_24*)m->exec.function.long_fun;
     return sf24(thread, theClass, GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 25: 
   {
     typedef w_long (w_sfun_25)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_25 *sf25 = (w_sfun_25*)m->exec.function.long_fun;
     return sf25(thread, theClass, GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 26: 
   {
     typedef w_long (w_sfun_26)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_instance, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_26 *sf26 = (w_sfun_26*)m->exec.function.long_fun;
     return sf26(thread, theClass, GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 27: 
   {
     typedef w_long (w_sfun_27)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_27 *sf27 = (w_sfun_27*)m->exec.function.long_fun;
     return sf27(thread, theClass, GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 28: 
   {
     typedef w_long (w_sfun_28)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_28 *sf28 = (w_sfun_28*)m->exec.function.long_fun;
     return sf28(thread, theClass, GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 29: 
   {
     typedef w_long (w_sfun_29)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_29 *sf29 = (w_sfun_29*)m->exec.function.long_fun;
     return sf29(thread, theClass, GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 30: 
   {
     typedef w_long (w_sfun_30)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_30 *sf30 = (w_sfun_30*)m->exec.function.long_fun;
     return sf30(thread, theClass, GET_SLOT_CONTENTS(top - 30), GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 31: 
   {
     typedef w_long (w_sfun_31)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_31 *sf31 = (w_sfun_31*)m->exec.function.long_fun;
     return sf31(thread, theClass, GET_SLOT_CONTENTS(top - 31), GET_SLOT_CONTENTS(top - 30), GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  default: 
    {
      w_long dummy;

      throwException(thread, clazzVirtualMachineError, "Too many parameters in call to native static method (max. 31 in this build)");
      dummy = 0LL;

      return dummy;
    }
  }
}

w_instance _call_static_reference(w_thread thread, w_instance theClass, w_slot top, w_method m) {
  switch (m->exec.arg_i) {
  case 0: 
   {
     typedef w_instance (w_sfun_0)(w_thread, w_instance);
     w_sfun_0 *sf0 = (w_sfun_0*)m->exec.function.ref_fun;
     return sf0(thread, theClass);
   }

  case 1: 
   {
     typedef w_instance (w_sfun_1)(w_thread, w_instance , w_word);
     w_sfun_1 *sf1 = (w_sfun_1*)m->exec.function.ref_fun;
     return sf1(thread, theClass, GET_SLOT_CONTENTS(top - 1));
   }

  case 2: 
   {
     typedef w_instance (w_sfun_2)(w_thread, w_instance , w_word, w_word);
     w_sfun_2 *sf2 = (w_sfun_2*)m->exec.function.ref_fun;
     return sf2(thread, theClass, GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 3: 
   {
     typedef w_instance (w_sfun_3)(w_thread, w_instance , w_word, w_word, w_word);
     w_sfun_3 *sf3 = (w_sfun_3*)m->exec.function.ref_fun;
     return sf3(thread, theClass, GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 4: 
   {
     typedef w_instance (w_sfun_4)(w_thread, w_instance , w_word, w_word, w_word, w_word);
     w_sfun_4 *sf4 = (w_sfun_4*)m->exec.function.ref_fun;
     return sf4(thread, theClass, GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 5: 
   {
     typedef w_instance (w_sfun_5)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word);
     w_sfun_5 *sf5 = (w_sfun_5*)m->exec.function.ref_fun;
     return sf5(thread, theClass, GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 6: 
   {
     typedef w_instance (w_sfun_6)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_6 *sf6 = (w_sfun_6*)m->exec.function.ref_fun;
     return sf6(thread, theClass, GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 7: 
   {
     typedef w_instance (w_sfun_7)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_7 *sf7 = (w_sfun_7*)m->exec.function.ref_fun;
     return sf7(thread, theClass, GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 8: 
   {
     typedef w_instance (w_sfun_8)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_8 *sf8 = (w_sfun_8*)m->exec.function.ref_fun;
     return sf8(thread, theClass, GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 9: 
   {
     typedef w_instance (w_sfun_9)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_9 *sf9 = (w_sfun_9*)m->exec.function.ref_fun;
     return sf9(thread, theClass, GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 10: 
   {
     typedef w_instance (w_sfun_10)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_10 *sf10 = (w_sfun_10*)m->exec.function.ref_fun;
     return sf10(thread, theClass, GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 11: 
   {
     typedef w_instance (w_sfun_11)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_11 *sf11 = (w_sfun_11*)m->exec.function.ref_fun;
     return sf11(thread, theClass, GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 12: 
   {
     typedef w_instance (w_sfun_12)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_12 *sf12 = (w_sfun_12*)m->exec.function.ref_fun;
     return sf12(thread, theClass, GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 13: 
   {
     typedef w_instance (w_sfun_13)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_13 *sf13 = (w_sfun_13*)m->exec.function.ref_fun;
     return sf13(thread, theClass, GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 14: 
   {
     typedef w_instance (w_sfun_14)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_14 *sf14 = (w_sfun_14*)m->exec.function.ref_fun;
     return sf14(thread, theClass, GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 15: 
   {
     typedef w_instance (w_sfun_15)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_15 *sf15 = (w_sfun_15*)m->exec.function.ref_fun;
     return sf15(thread, theClass, GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 16: 
   {
     typedef w_instance (w_sfun_16)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_16 *sf16 = (w_sfun_16*)m->exec.function.ref_fun;
     return sf16(thread, theClass, GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 17: 
   {
     typedef w_instance (w_sfun_17)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_17 *sf17 = (w_sfun_17*)m->exec.function.ref_fun;
     return sf17(thread, theClass, GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 18: 
   {
     typedef w_instance (w_sfun_18)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_18 *sf18 = (w_sfun_18*)m->exec.function.ref_fun;
     return sf18(thread, theClass, GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 19: 
   {
     typedef w_instance (w_sfun_19)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_19 *sf19 = (w_sfun_19*)m->exec.function.ref_fun;
     return sf19(thread, theClass, GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 20: 
   {
     typedef w_instance (w_sfun_20)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_20 *sf20 = (w_sfun_20*)m->exec.function.ref_fun;
     return sf20(thread, theClass, GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 21: 
   {
     typedef w_instance (w_sfun_21)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_21 *sf21 = (w_sfun_21*)m->exec.function.ref_fun;
     return sf21(thread, theClass, GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 22: 
   {
     typedef w_instance (w_sfun_22)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_22 *sf22 = (w_sfun_22*)m->exec.function.ref_fun;
     return sf22(thread, theClass, GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 23: 
   {
     typedef w_instance (w_sfun_23)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_23 *sf23 = (w_sfun_23*)m->exec.function.ref_fun;
     return sf23(thread, theClass, GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 24: 
   {
     typedef w_instance (w_sfun_24)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_24 *sf24 = (w_sfun_24*)m->exec.function.ref_fun;
     return sf24(thread, theClass, GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 25: 
   {
     typedef w_instance (w_sfun_25)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_25 *sf25 = (w_sfun_25*)m->exec.function.ref_fun;
     return sf25(thread, theClass, GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 26: 
   {
     typedef w_instance (w_sfun_26)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_instance, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_26 *sf26 = (w_sfun_26*)m->exec.function.ref_fun;
     return sf26(thread, theClass, GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 27: 
   {
     typedef w_instance (w_sfun_27)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_27 *sf27 = (w_sfun_27*)m->exec.function.ref_fun;
     return sf27(thread, theClass, GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 28: 
   {
     typedef w_instance (w_sfun_28)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_28 *sf28 = (w_sfun_28*)m->exec.function.ref_fun;
     return sf28(thread, theClass, GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 29: 
   {
     typedef w_instance (w_sfun_29)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_29 *sf29 = (w_sfun_29*)m->exec.function.ref_fun;
     return sf29(thread, theClass, GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 30: 
   {
     typedef w_instance (w_sfun_30)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_30 *sf30 = (w_sfun_30*)m->exec.function.ref_fun;
     return sf30(thread, theClass, GET_SLOT_CONTENTS(top - 30), GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 31: 
   {
     typedef w_instance (w_sfun_31)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_sfun_31 *sf31 = (w_sfun_31*)m->exec.function.ref_fun;
     return sf31(thread, theClass, GET_SLOT_CONTENTS(top - 31), GET_SLOT_CONTENTS(top - 30), GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  default: 
    {
      w_instance dummy;

      throwException(thread, clazzVirtualMachineError, "Too many parameters in call to native static method (max. 31 in this build)");
      dummy = NULL;

      return dummy;
    }
  }
}

w_word _call_instance_32bits(w_thread thread, w_slot top, w_method m) {
  switch (m->exec.arg_i) {
  case 1: 
   {
     typedef w_word (w_ifun_1)(w_thread, w_instance);
     w_ifun_1 *if1 = (w_ifun_1*)m->exec.function.word_fun;
     return if1(thread, (w_instance) GET_SLOT_CONTENTS(top - 1));
   }

  case 2: 
   {
     typedef w_word (w_ifun_2)(w_thread, w_instance , w_word);
     w_ifun_2 *if2 = (w_ifun_2*)m->exec.function.word_fun;
     return if2(thread, (w_instance) GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 3: 
   {
     typedef w_word (w_ifun_3)(w_thread, w_instance , w_word, w_word);
     w_ifun_3 *if3 = (w_ifun_3*)m->exec.function.word_fun;
     return if3(thread, (w_instance) GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 4: 
   {
     typedef w_word (w_ifun_4)(w_thread, w_instance , w_word, w_word, w_word);
     w_ifun_4 *if4 = (w_ifun_4*)m->exec.function.word_fun;
     return if4(thread, (w_instance) GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 5: 
   {
     typedef w_word (w_ifun_5)(w_thread, w_instance , w_word, w_word, w_word, w_word);
     w_ifun_5 *if5 = (w_ifun_5*)m->exec.function.word_fun;
     return if5(thread, (w_instance) GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 6: 
   {
     typedef w_word (w_ifun_6)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word);
     w_ifun_6 *if6 = (w_ifun_6*)m->exec.function.word_fun;
     return if6(thread, (w_instance) GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 7: 
   {
     typedef w_word (w_ifun_7)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_7 *if7 = (w_ifun_7*)m->exec.function.word_fun;
     return if7(thread, (w_instance) GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 8: 
   {
     typedef w_word (w_ifun_8)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_8 *if8 = (w_ifun_8*)m->exec.function.word_fun;
     return if8(thread, (w_instance) GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 9: 
   {
     typedef w_word (w_ifun_9)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_9 *if9 = (w_ifun_9*)m->exec.function.word_fun;
     return if9(thread, (w_instance) GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 10: 
   {
     typedef w_word (w_ifun_10)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_10 *if10 = (w_ifun_10*)m->exec.function.word_fun;
     return if10(thread, (w_instance) GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 11: 
   {
     typedef w_word (w_ifun_11)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_11 *if11 = (w_ifun_11*)m->exec.function.word_fun;
     return if11(thread, (w_instance) GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 12: 
   {
     typedef w_word (w_ifun_12)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_12 *if12 = (w_ifun_12*)m->exec.function.word_fun;
     return if12(thread, (w_instance) GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 13: 
   {
     typedef w_word (w_ifun_13)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_13 *if13 = (w_ifun_13*)m->exec.function.word_fun;
     return if13(thread, (w_instance) GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 14: 
   {
     typedef w_word (w_ifun_14)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_14 *if14 = (w_ifun_14*)m->exec.function.word_fun;
     return if14(thread, (w_instance) GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 15: 
   {
     typedef w_word (w_ifun_15)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_15 *if15 = (w_ifun_15*)m->exec.function.word_fun;
     return if15(thread, (w_instance) GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 16: 
   {
     typedef w_word (w_ifun_16)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_16 *if16 = (w_ifun_16*)m->exec.function.word_fun;
     return if16(thread, (w_instance) GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 17: 
   {
     typedef w_word (w_ifun_17)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_17 *if17 = (w_ifun_17*)m->exec.function.word_fun;
     return if17(thread, (w_instance) GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 18: 
   {
     typedef w_word (w_ifun_18)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_18 *if18 = (w_ifun_18*)m->exec.function.word_fun;
     return if18(thread, (w_instance) GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 19: 
   {
     typedef w_word (w_ifun_19)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_19 *if19 = (w_ifun_19*)m->exec.function.word_fun;
     return if19(thread, (w_instance) GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 20: 
   {
     typedef w_word (w_ifun_20)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_20 *if20 = (w_ifun_20*)m->exec.function.word_fun;
     return if20(thread, (w_instance) GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 21: 
   {
     typedef w_word (w_ifun_21)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_21 *if21 = (w_ifun_21*)m->exec.function.word_fun;
     return if21(thread, (w_instance) GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 22: 
   {
     typedef w_word (w_ifun_22)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_22 *if22 = (w_ifun_22*)m->exec.function.word_fun;
     return if22(thread, (w_instance) GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 23: 
   {
     typedef w_word (w_ifun_23)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_23 *if23 = (w_ifun_23*)m->exec.function.word_fun;
     return if23(thread, (w_instance) GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 24: 
   {
     typedef w_word (w_ifun_24)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_24 *if24 = (w_ifun_24*)m->exec.function.word_fun;
     return if24(thread, (w_instance) GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 25: 
   {
     typedef w_word (w_ifun_25)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_25 *if25 = (w_ifun_25*)m->exec.function.word_fun;
     return if25(thread, (w_instance) GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 26: 
   {
     typedef w_word (w_ifun_26)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_26 *if26 = (w_ifun_26*)m->exec.function.word_fun;
     return if26(thread, (w_instance) GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 27: 
   {
     typedef w_word (w_ifun_27)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_27 *if27 = (w_ifun_27*)m->exec.function.word_fun;
     return if27(thread, (w_instance) GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 28: 
   {
     typedef w_word (w_ifun_28)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_28 *if28 = (w_ifun_28*)m->exec.function.word_fun;
     return if28(thread, (w_instance) GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 29: 
   {
     typedef w_word (w_ifun_29)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_29 *if29 = (w_ifun_29*)m->exec.function.word_fun;
     return if29(thread, (w_instance) GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 30: 
   {
     typedef w_word (w_ifun_30)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_30 *if30 = (w_ifun_30*)m->exec.function.word_fun;
     return if30(thread, (w_instance) GET_SLOT_CONTENTS(top - 30), GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 31: 
   {
     typedef w_word (w_ifun_31)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_31 *if31 = (w_ifun_31*)m->exec.function.word_fun;
     return if31(thread, (w_instance) GET_SLOT_CONTENTS(top - 31), GET_SLOT_CONTENTS(top - 30), GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  default: 
    {
      w_word dummy;

      throwException(thread, clazzVirtualMachineError, "Too many parameters in call to native instance method (max. 30 in this build)");
      dummy = 0L;

      return dummy;

    }
  }
}

w_long _call_instance_64bits(w_thread thread, w_slot top, w_method m) {
  switch (m->exec.arg_i) {
  case 1: 
   {
     typedef w_long (w_ifun_1)(w_thread, w_instance);
     w_ifun_1 *if1 = (w_ifun_1*)m->exec.function.long_fun;
     return if1(thread, (w_instance) GET_SLOT_CONTENTS(top - 1));
   }

  case 2: 
   {
     typedef w_long (w_ifun_2)(w_thread, w_instance , w_word);
     w_ifun_2 *if2 = (w_ifun_2*)m->exec.function.long_fun;
     return if2(thread, (w_instance) GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 3: 
   {
     typedef w_long (w_ifun_3)(w_thread, w_instance , w_word, w_word);
     w_ifun_3 *if3 = (w_ifun_3*)m->exec.function.long_fun;
     return if3(thread, (w_instance) GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 4: 
   {
     typedef w_long (w_ifun_4)(w_thread, w_instance , w_word, w_word, w_word);
     w_ifun_4 *if4 = (w_ifun_4*)m->exec.function.long_fun;
     return if4(thread, (w_instance) GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 5: 
   {
     typedef w_long (w_ifun_5)(w_thread, w_instance , w_word, w_word, w_word, w_word);
     w_ifun_5 *if5 = (w_ifun_5*)m->exec.function.long_fun;
     return if5(thread, (w_instance) GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 6: 
   {
     typedef w_long (w_ifun_6)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word);
     w_ifun_6 *if6 = (w_ifun_6*)m->exec.function.long_fun;
     return if6(thread, (w_instance) GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 7: 
   {
     typedef w_long (w_ifun_7)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_7 *if7 = (w_ifun_7*)m->exec.function.long_fun;
     return if7(thread, (w_instance) GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 8: 
   {
     typedef w_long (w_ifun_8)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_8 *if8 = (w_ifun_8*)m->exec.function.long_fun;
     return if8(thread, (w_instance) GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 9: 
   {
     typedef w_long (w_ifun_9)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_9 *if9 = (w_ifun_9*)m->exec.function.long_fun;
     return if9(thread, (w_instance) GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 10: 
   {
     typedef w_long (w_ifun_10)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_10 *if10 = (w_ifun_10*)m->exec.function.long_fun;
     return if10(thread, (w_instance) GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 11: 
   {
     typedef w_long (w_ifun_11)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_11 *if11 = (w_ifun_11*)m->exec.function.long_fun;
     return if11(thread, (w_instance) GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 12: 
   {
     typedef w_long (w_ifun_12)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_12 *if12 = (w_ifun_12*)m->exec.function.long_fun;
     return if12(thread, (w_instance) GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 13: 
   {
     typedef w_long (w_ifun_13)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_13 *if13 = (w_ifun_13*)m->exec.function.long_fun;
     return if13(thread, (w_instance) GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 14: 
   {
     typedef w_long (w_ifun_14)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_14 *if14 = (w_ifun_14*)m->exec.function.long_fun;
     return if14(thread, (w_instance) GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 15: 
   {
     typedef w_long (w_ifun_15)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_15 *if15 = (w_ifun_15*)m->exec.function.long_fun;
     return if15(thread, (w_instance) GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 16: 
   {
     typedef w_long (w_ifun_16)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_16 *if16 = (w_ifun_16*)m->exec.function.long_fun;
     return if16(thread, (w_instance) GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 17: 
   {
     typedef w_long (w_ifun_17)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_17 *if17 = (w_ifun_17*)m->exec.function.long_fun;
     return if17(thread, (w_instance) GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 18: 
   {
     typedef w_long (w_ifun_18)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_18 *if18 = (w_ifun_18*)m->exec.function.long_fun;
     return if18(thread, (w_instance) GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 19: 
   {
     typedef w_long (w_ifun_19)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_19 *if19 = (w_ifun_19*)m->exec.function.long_fun;
     return if19(thread, (w_instance) GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 20: 
   {
     typedef w_long (w_ifun_20)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_20 *if20 = (w_ifun_20*)m->exec.function.long_fun;
     return if20(thread, (w_instance) GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 21: 
   {
     typedef w_long (w_ifun_21)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_21 *if21 = (w_ifun_21*)m->exec.function.long_fun;
     return if21(thread, (w_instance) GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 22: 
   {
     typedef w_long (w_ifun_22)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_22 *if22 = (w_ifun_22*)m->exec.function.long_fun;
     return if22(thread, (w_instance) GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 23: 
   {
     typedef w_long (w_ifun_23)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_23 *if23 = (w_ifun_23*)m->exec.function.long_fun;
     return if23(thread, (w_instance) GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 24: 
   {
     typedef w_long (w_ifun_24)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_24 *if24 = (w_ifun_24*)m->exec.function.long_fun;
     return if24(thread, (w_instance) GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 25: 
   {
     typedef w_long (w_ifun_25)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_25 *if25 = (w_ifun_25*)m->exec.function.long_fun;
     return if25(thread, (w_instance) GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 26: 
   {
     typedef w_long (w_ifun_26)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_26 *if26 = (w_ifun_26*)m->exec.function.long_fun;
     return if26(thread, (w_instance) GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 27: 
   {
     typedef w_long (w_ifun_27)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_27 *if27 = (w_ifun_27*)m->exec.function.long_fun;
     return if27(thread, (w_instance) GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 28: 
   {
     typedef w_long (w_ifun_28)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_28 *if28 = (w_ifun_28*)m->exec.function.long_fun;
     return if28(thread, (w_instance) GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 29: 
   {
     typedef w_long (w_ifun_29)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_29 *if29 = (w_ifun_29*)m->exec.function.long_fun;
     return if29(thread, (w_instance) GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 30: 
   {
     typedef w_long (w_ifun_30)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_30 *if30 = (w_ifun_30*)m->exec.function.long_fun;
     return if30(thread, (w_instance) GET_SLOT_CONTENTS(top - 30), GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 31: 
   {
     typedef w_long (w_ifun_31)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_31 *if31 = (w_ifun_31*)m->exec.function.long_fun;
     return if31(thread, (w_instance) GET_SLOT_CONTENTS(top - 31), GET_SLOT_CONTENTS(top - 30), GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  default: 
    {
      w_long dummy;

      throwException(thread, clazzVirtualMachineError, "Too many parameters in call to native instance method (max. 30 in this build)");
      dummy = 0LL;

      return dummy;

    }
  }
}

w_instance _call_instance_reference(w_thread thread, w_slot top, w_method m) {
  switch (m->exec.arg_i) {
  case 1: 
   {
     typedef w_word (w_ifun_1)(w_thread, w_instance);
     w_ifun_1 *if1 = (w_ifun_1*)m->exec.function.ref_fun;
     return if1(thread, (w_instance) GET_SLOT_CONTENTS(top - 1));
   }

  case 2: 
   {
     typedef w_word (w_ifun_2)(w_thread, w_instance , w_word);
     w_ifun_2 *if2 = (w_ifun_2*)m->exec.function.ref_fun;
     return if2(thread, (w_instance) GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 3: 
   {
     typedef w_word (w_ifun_3)(w_thread, w_instance , w_word, w_word);
     w_ifun_3 *if3 = (w_ifun_3*)m->exec.function.ref_fun;
     return if3(thread, (w_instance) GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 4: 
   {
     typedef w_word (w_ifun_4)(w_thread, w_instance , w_word, w_word, w_word);
     w_ifun_4 *if4 = (w_ifun_4*)m->exec.function.ref_fun;
     return if4(thread, (w_instance) GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 5: 
   {
     typedef w_word (w_ifun_5)(w_thread, w_instance , w_word, w_word, w_word, w_word);
     w_ifun_5 *if5 = (w_ifun_5*)m->exec.function.ref_fun;
     return if5(thread, (w_instance) GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 6: 
   {
     typedef w_word (w_ifun_6)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word);
     w_ifun_6 *if6 = (w_ifun_6*)m->exec.function.ref_fun;
     return if6(thread, (w_instance) GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 7: 
   {
     typedef w_word (w_ifun_7)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_7 *if7 = (w_ifun_7*)m->exec.function.ref_fun;
     return if7(thread, (w_instance) GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 8: 
   {
     typedef w_word (w_ifun_8)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_8 *if8 = (w_ifun_8*)m->exec.function.ref_fun;
     return if8(thread, (w_instance) GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 9: 
   {
     typedef w_word (w_ifun_9)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_9 *if9 = (w_ifun_9*)m->exec.function.ref_fun;
     return if9(thread, (w_instance) GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 10: 
   {
     typedef w_word (w_ifun_10)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_10 *if10 = (w_ifun_10*)m->exec.function.ref_fun;
     return if10(thread, (w_instance) GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 11: 
   {
     typedef w_word (w_ifun_11)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_11 *if11 = (w_ifun_11*)m->exec.function.ref_fun;
     return if11(thread, (w_instance) GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 12: 
   {
     typedef w_word (w_ifun_12)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_12 *if12 = (w_ifun_12*)m->exec.function.ref_fun;
     return if12(thread, (w_instance) GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 13: 
   {
     typedef w_word (w_ifun_13)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_13 *if13 = (w_ifun_13*)m->exec.function.ref_fun;
     return if13(thread, (w_instance) GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 14: 
   {
     typedef w_word (w_ifun_14)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_14 *if14 = (w_ifun_14*)m->exec.function.ref_fun;
     return if14(thread, (w_instance) GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 15: 
   {
     typedef w_word (w_ifun_15)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_15 *if15 = (w_ifun_15*)m->exec.function.ref_fun;
     return if15(thread, (w_instance) GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 16: 
   {
     typedef w_word (w_ifun_16)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_16 *if16 = (w_ifun_16*)m->exec.function.ref_fun;
     return if16(thread, (w_instance) GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 17: 
   {
     typedef w_word (w_ifun_17)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_17 *if17 = (w_ifun_17*)m->exec.function.ref_fun;
     return if17(thread, (w_instance) GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 18: 
   {
     typedef w_word (w_ifun_18)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_18 *if18 = (w_ifun_18*)m->exec.function.ref_fun;
     return if18(thread, (w_instance) GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 19: 
   {
     typedef w_word (w_ifun_19)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_19 *if19 = (w_ifun_19*)m->exec.function.ref_fun;
     return if19(thread, (w_instance) GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 20: 
   {
     typedef w_word (w_ifun_20)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_20 *if20 = (w_ifun_20*)m->exec.function.ref_fun;
     return if20(thread, (w_instance) GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 21: 
   {
     typedef w_word (w_ifun_21)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_21 *if21 = (w_ifun_21*)m->exec.function.ref_fun;
     return if21(thread, (w_instance) GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 22: 
   {
     typedef w_word (w_ifun_22)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_22 *if22 = (w_ifun_22*)m->exec.function.ref_fun;
     return if22(thread, (w_instance) GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 23: 
   {
     typedef w_word (w_ifun_23)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_23 *if23 = (w_ifun_23*)m->exec.function.ref_fun;
     return if23(thread, (w_instance) GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 24: 
   {
     typedef w_word (w_ifun_24)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_24 *if24 = (w_ifun_24*)m->exec.function.ref_fun;
     return if24(thread, (w_instance) GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 25: 
   {
     typedef w_word (w_ifun_25)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_25 *if25 = (w_ifun_25*)m->exec.function.ref_fun;
     return if25(thread, (w_instance) GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 26: 
   {
     typedef w_word (w_ifun_26)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_26 *if26 = (w_ifun_26*)m->exec.function.ref_fun;
     return if26(thread, (w_instance) GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 27: 
   {
     typedef w_word (w_ifun_27)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_27 *if27 = (w_ifun_27*)m->exec.function.ref_fun;
     return if27(thread, (w_instance) GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 28: 
   {
     typedef w_word (w_ifun_28)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_28 *if28 = (w_ifun_28*)m->exec.function.ref_fun;
     return if28(thread, (w_instance) GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 29: 
   {
     typedef w_word (w_ifun_29)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_29 *if29 = (w_ifun_29*)m->exec.function.ref_fun;
     return if29(thread, (w_instance) GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 30: 
   {
     typedef w_word (w_ifun_30)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_30 *if30 = (w_ifun_30*)m->exec.function.ref_fun;
     return if30(thread, (w_instance) GET_SLOT_CONTENTS(top - 30), GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  case 31: 
   {
     typedef w_word (w_ifun_31)(w_thread, w_instance , w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word, w_word);
     w_ifun_31 *if31 = (w_ifun_31*)m->exec.function.ref_fun;
     return if31(thread, (w_instance) GET_SLOT_CONTENTS(top - 31), GET_SLOT_CONTENTS(top - 30), GET_SLOT_CONTENTS(top - 29), GET_SLOT_CONTENTS(top - 28), GET_SLOT_CONTENTS(top - 27), GET_SLOT_CONTENTS(top - 26), GET_SLOT_CONTENTS(top - 25), GET_SLOT_CONTENTS(top - 24), GET_SLOT_CONTENTS(top - 23), GET_SLOT_CONTENTS(top - 22), GET_SLOT_CONTENTS(top - 21), GET_SLOT_CONTENTS(top - 20), GET_SLOT_CONTENTS(top - 19), GET_SLOT_CONTENTS(top - 18), GET_SLOT_CONTENTS(top - 17), GET_SLOT_CONTENTS(top - 16), GET_SLOT_CONTENTS(top - 15), GET_SLOT_CONTENTS(top - 14), GET_SLOT_CONTENTS(top - 13), GET_SLOT_CONTENTS(top - 12), GET_SLOT_CONTENTS(top - 11), GET_SLOT_CONTENTS(top - 10), GET_SLOT_CONTENTS(top - 9), GET_SLOT_CONTENTS(top - 8), GET_SLOT_CONTENTS(top - 7), GET_SLOT_CONTENTS(top - 6), GET_SLOT_CONTENTS(top - 5), GET_SLOT_CONTENTS(top - 4), GET_SLOT_CONTENTS(top - 3), GET_SLOT_CONTENTS(top - 2), GET_SLOT_CONTENTS(top - 1));
   }

  default: 
    {
      w_long dummy;

      throwException(thread, clazzVirtualMachineError, "Too many parameters in call to native instance method (max. 30 in this build)");
      dummy = 0LL;

      return dummy;

    }
  }
}

