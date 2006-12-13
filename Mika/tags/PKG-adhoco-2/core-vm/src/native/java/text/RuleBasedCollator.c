/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: RuleBasedCollator.c,v 1.2 2004/11/30 10:08:10 cvs Exp $
*/

#include "core-classes.h"
#include "hashtable.h"
#include "wstrings.h"
#include "unicode.h"

void RuleBasedCollator_createHashtables(JNIEnv *env, w_instance thisClass) {
  createDecompositionTables();
}

w_int RuleBasedCollator_getCombiningClass(JNIEnv *env, w_instance thisClass, w_char c) {
  return charToCombiningClass(c);
}

w_instance RuleBasedCollator_getDecomposition(JNIEnv *env, w_instance thisClass, w_char c) {
  w_char *decomposition;
  w_string   s;
  w_instance result;

  decomposition = charToDecomposition(c);
  if (decomposition) {

    s = unicode2String(decomposition+1, *decomposition);
    if (!s) {
      wabort(ABORT_WONKA, "Unable to create s\n");
    }
    result = newStringInstance(s);
    deregisterString(s);

    return result;
  }
  else {

    return NULL;

  }
}

w_instance RuleBasedCollator_getCompatibility(JNIEnv *env, w_instance thisClass, w_char c) {
  w_string compatibility;

  compatibility = charToCompatibilityType(c);
  if (compatibility) {

    return newStringInstance(compatibility);

  }
  else {

    return NULL;

  }
}

