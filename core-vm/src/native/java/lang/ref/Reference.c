/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2005, 2006 by Chris Gray, /k/ Embedded        *
* Java Solutions. All rights reserved.                                    *
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
*                                                                         *
* Modifications copyright (c) 2004, 2005, 2006 by Chris Gray,             *
* /k/ Embedded Java Solutions. All rights reserved.                       *
*                                                                         *
**************************************************************************/


/*
** $Id: Reference.c,v 1.7 2006/10/04 14:24:16 cvsroot Exp $
*/

#include "clazz.h"
#include "core-classes.h"
#include "heap.h"

void Reference_set(JNIEnv *env, w_instance this, w_instance referent) {
  w_thread thread = JNIEnv2w_thread(env);
  w_boolean unsafe = enterUnsafeRegion(thread);
  woempa(1, "Setting reference from %j to %k at %p\n", this, instance2clazz(referent), (w_instance)referent);
  if(referent) {
    volatile w_word *flagsptr = instance2flagsptr(this);
    setFlag(*flagsptr, O_ENQUEUEABLE);
    setWotsitField(this, F_Reference_referent, referent);
  }
  if (!unsafe) {
    enterSafeRegion(thread);
  }
}

w_instance Reference_get(JNIEnv *env, w_instance this) {
  w_thread thread = JNIEnv2w_thread(env);
  w_instance referent;

  w_boolean unsafe = enterUnsafeRegion(thread);

  referent = getWotsitField(this, F_Reference_referent);
  woempa(1, "Getting reference from %j to %j\n", this, referent);

  if (referent) {
/*
    if(isNotSet(instance2object(this)->flags,O_ENQUEUEABLE)) {
      w_dump("PANIC ! Reference %j is not Enqueueable but stil has referent %p :0\n",this,referent);
    }
*/
    addLocalReference(thread, referent);
  }
  if (!unsafe) {
    enterSafeRegion(thread);
  }

  return referent;
}

void Reference_clear(JNIEnv *env, w_instance this) {
  w_thread thread = JNIEnv2w_thread(env);
  w_boolean unsafe = enterUnsafeRegion(thread);
  woempa(1, "User cleared reference from %j to %j\n", this, getWotsitField(this, F_Reference_referent));
  clearWotsitField(this, F_Reference_referent);
  if (!unsafe) {
    enterSafeRegion(thread);
  }
}

