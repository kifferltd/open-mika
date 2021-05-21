/**************************************************************************
* Copyright (c) 2001, 2002 by Punch Telematix. All rights reserved.       *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

#ifndef _CHECKS_H
#define _CHECKS_H

#include "wonka.h"

/*
** isAssignmentCompatible(S_clazz, T_clazz) checks that an instance of
** S_Clazz can be assigned to a variable of type T_clazz. Both S_clazz 
** and T_class must already be loaded. Can result in S_clazz becoming
** supersLoaded as a side-effect.
*/
w_boolean isAssignmentCompatible(w_clazz S_clazz, w_clazz T_clazz);

w_boolean implementsInterface (w_clazz thisclass, w_clazz interfaze);

w_boolean sameRuntimePackage(w_clazz clazz1, w_clazz clazz2);

w_boolean isAllowedToCall(w_clazz caller, w_method method, w_clazz objClazz);
w_boolean isAllowedToAccess(w_clazz caller, w_field field, w_clazz objClazz);

#endif /* _CHECKS_H */
