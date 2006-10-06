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
** $Id: Etc.c,v 1.5 2006/03/14 14:17:21 cvs Exp $
*/

#include <stdlib.h>
#include <string.h>
#include "clazz.h"
#include "core-classes.h"
#include "wstrings.h"
#include "threads.h"
#include "wonka.h"
#include "exception.h"

void 
Etc_static_setTriggerLevel
( JNIEnv *env, w_instance classSystem,
  w_instance filenameString, w_int triggerLevel
) {
  woempa(9, "NOT FUNCTIONAL\n");
}

void 
Etc_static_setAllTriggerLevel
( JNIEnv *env, w_instance classSystem,
  w_int triggerLevel
) {
  setAllTriggerLevel(triggerLevel);
}

void
Etc_static_woempa
( JNIEnv *env, w_instance classSystem,
  w_int triggerLevel,
  w_instance theString
) {
  woempa(triggerLevel, "%w\n", String2string ( theString));
}

void Etc_static_heapCheck(JNIEnv *env, w_instance classSystem){
#ifdef DEBUG
  woempa(9, "calling heapCheck for Etc\n");
  heapCheck;
#endif //DEBUG

}

