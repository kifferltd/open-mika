/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: acc.c,v 1.2 2005/05/24 10:06:30 cvs Exp $
*/

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

