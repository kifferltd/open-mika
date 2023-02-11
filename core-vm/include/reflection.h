/**************************************************************************
* Copyright (c) 2007, 2023 by Chris Gray, KIFFER Ltd.                     *
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

#ifndef _REFLECTION_H
#define _REFLECTION_H

#include "jni.h"
#include "wonka.h"

w_boolean widen(w_clazz F_clazz, w_word F_data[], w_clazz T_clazz, void *T_data);
w_instance createWrapperInstance(w_thread thread, w_clazz clazz, w_int *slot);

w_frame invoke(JNIEnv *env, w_method method, w_instance This, w_instance Arguments);
w_clazz getWrappedValue(w_instance value, w_word **data);

void voidProxyMethodCode(w_thread thread, w_instance thisProxy, ...);
w_word singleProxyMethodCode(w_thread thread, w_instance thisProxy, ...);
w_long doubleProxyMethodCode(w_thread thread, w_instance thisProxy, ...);

void wrapException(w_thread thread, w_clazz wrapper_clazz, w_size field_offset);
#endif/* _REFLECTION_H */
