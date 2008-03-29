/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
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

#include "arrays.h"
#include "core-classes.h"
#include "fields.h"
#include "wordset.h"

/** Convert an array of ProtectionDomain's into a w_Wordset of
 ** distinct PermissionCollection's.
 */
w_wordset pdarray2context(w_instance thisAccessControlContext, w_instance arrayOfProtectionDomains) {
  w_wordset context = NULL;
  w_instance *domain = instance2Array_instance(arrayOfProtectionDomains);
  w_int n = instance2Array_length(arrayOfProtectionDomains);
  w_int i;

  if (n) {
    if (!addToWordset(&context, (w_word)getReferenceField((*domain), F_ProtectionDomain_permissions))) {
      wabort(ABORT_WONKA, "Unable to add permission to context\n");
    }
    ++domain;
  }
  for (i = 1; i < n; ++i) {
    w_word permissions = (w_word)getReferenceField((*domain), F_ProtectionDomain_permissions);
    if (!isInWordset(&context,permissions)) {
      if (!addToWordset(&context,permissions)) {
        wabort(ABORT_WONKA, "Unable to add permission to context\n");
      }
    }
    ++domain;
  }
  if (n) {
    sortWordset(&context);
  }
  return context;
}

