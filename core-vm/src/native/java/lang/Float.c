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

#ifdef NATIVE_FP
#include <math.h>
#endif
#include "clazz.h"
#include "descriptor.h"
#include "mikaMath.h"

w_int Float_static_floatToRawIntBits(w_thread thread, w_instance class, w_float f) {
  union {w_float f; w_int i;} foo;

  foo.f = f;

  return foo.i;
}

w_float Float_static_intToFloatBits(w_thread thread, w_instance class, w_int i) {
  union {w_float f; w_int i;} foo;

  foo.i = i;

  return foo.f;
}

w_boolean Float_static_isInfinite(w_thread thread, w_instance class, w_float f) {
  return wfp_float32_is_Infinite(f);
}

w_boolean Float_static_isNaN(w_thread thread, w_instance class, w_float f) {
  return wfp_float32_is_NaN(f);
}

w_instance Float_getWrappedClass(w_thread thread, w_instance thisClass) {
  return clazz2Class(clazz_float);
}
