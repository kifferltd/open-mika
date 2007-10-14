/**************************************************************************
* Parts copyright (c) 2001, 2002 by Punch Telematix. All rights reserved. *
* Parts copyright (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.    *
*  All rights reserved.                                                  *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/
#ifndef _DISPATCHER_H_
#define _DISPATCHER_H_

void initialize_native_dispatcher(w_frame caller, w_method method);

void native_instance_synchronized_reference(w_frame caller, w_method method);
void native_instance_synchronized_32bits(w_frame caller, w_method method);
void native_instance_synchronized_64bits(w_frame caller, w_method method);
void native_instance_synchronized_void(w_frame caller, w_method method);

void native_instance_unsynchronized_reference(w_frame caller, w_method method);
void native_instance_unsynchronized_32bits(w_frame caller, w_method method);
void native_instance_unsynchronized_64bits(w_frame caller, w_method method);
void native_instance_unsynchronized_void(w_frame caller, w_method method);

void native_static_synchronized_reference(w_frame caller, w_method method);
void native_static_synchronized_32bits(w_frame caller, w_method method);
void native_static_synchronized_64bits(w_frame caller, w_method method);
void native_static_synchronized_void(w_frame caller, w_method method);

void native_static_unsynchronized_reference(w_frame caller, w_method method);
void native_static_unsynchronized_32bits(w_frame caller, w_method method);
void native_static_unsynchronized_64bits(w_frame caller, w_method method);
void native_static_unsynchronized_void(w_frame caller, w_method method);

typedef void (*w_callfun)(w_frame frame, w_method method);

void initialize_dispatcher(w_frame caller, w_method method);

extern w_callfun dispatchers[];

/*
** If USE_SPECIAL_CASE_DISPATCHERS is defined, special cases such as
** trivial methods and getters or setters will use special dispatchers.
*/
#define USE_SPECIAL_CASE_DISPATCHERS

/*
** If BACKPATCH_SPECIAL_CASES is defined, special-case dispatchers will
** patch their call sites whenever they are called with invokenonvirtual 
** or invokestatic. Not enabled by default, as it seems to offer little
** gain in return for the increased VM code size and complexity.
** Only operative if USE_SPECIAL_CASE_DISPATCHERS is defined.
*/
//#define BACKPATCH_SPECIAL_CASES

#endif /* _DISPATCHER_H_ */
