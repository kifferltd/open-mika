/**************************************************************************
* Copyright  (c) 2001, 2002 by Acunia N.V. All rights reserved.           *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: RMIConnection.c,v 1.2 2004/11/18 22:56:54 cvs Exp $
*/

#include "wonka.h"
#include "core-classes.h"
#include "ts-mem.h"
#include "sha.h"
#include "exception.h"
#include "wstrings.h"

w_long RMIConnection_getStringHash(JNIEnv *env, w_instance ThisClass, w_instance TheString) {
  w_long suid = 0;
  w_thread thread = JNIEnv2w_thread(env);

  if(TheString == NULL){
    throwException(thread,clazzNullPointerException,NULL);
  }
  else {
    w_word length;
    w_byte * utf8 = string2UTF8(String2string(TheString), &length);

    woempa(10, "'%w' with length %i (=?= %d)\n",String2string(TheString),length,(w_int)*(utf8+1));

    if(utf8) {
      w_sha sha = allocSha();
      processSha(sha, utf8, length);
      finishSha(sha);
      releaseMem(utf8);

      suid = sha->signature.B[4];
      suid <<= 8;  suid |= sha->signature.B[5];
      suid <<= 8;  suid |= sha->signature.B[6];
      suid <<= 8;  suid |= sha->signature.B[7];
      suid <<= 8;  suid |= sha->signature.B[0];
      suid <<= 8;  suid |= sha->signature.B[1];
      suid <<= 8;  suid |= sha->signature.B[2];
      suid <<= 8;  suid |= sha->signature.B[3];
    }
  }
  return suid;

}
