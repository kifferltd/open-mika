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
** $Id: Etc.c,v 1.2 2004/11/30 10:27:39 cvs Exp $
*/

#include "clazz.h"
#include "core-classes.h"
#include "wstrings.h"
#include "wonka.h"
#include "mika_threads.h"

w_instance
Etc_getPlatform (JNIEnv *env, w_instance classEtc) {

  w_instance result;
  w_string   string;

  result = allocStringInstance(JNIEnv2w_thread(env));
  if (result) {
    string = cstring2String("UNKNOWN", 7);
    setWotsitField(result, F_String_wotsit, string);
  }

  return result;

}

void 
Etc_static_setTriggerLevel ( JNIEnv *env, w_instance classSystem, w_instance filenameString, w_int triggerLevel) {
}

void Etc_static_setAllTriggerLevel ( JNIEnv *env, w_instance classSystem, w_int triggerLevel) {
  setAllTriggerLevel(triggerLevel);
}

void Etc_static_woempa ( JNIEnv *env, w_instance classSystem, w_int triggerLevel, w_instance theString) {
  woempa(triggerLevel,"%w\n", getWotsitField(theString, F_String_wotsit));
}


