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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/* $Id: AppletAudioClip.c,v 1.2 2006/05/02 16:28:48 cvs Exp $ */

#include <jni.h>
//#include "sound.h"


int callback_function(JNIEnv *env, jobject thisObj) {
  jclass clazz = (*env)->GetObjectClass(env, thisObj);
  jfieldID playID = (*env)->GetFieldID(env,clazz, "play","Z");
  jfieldID restartID = (*env)->GetFieldID(env,clazz, "restart","Z");
  int result = !(*env)->GetBooleanField(env, thisObj, playID);
  result |= (*env)->GetBooleanField(env, thisObj, restartID);
  return result;
}


void AppletAudioClip_play(JNIEnv *env, jobject thisObj, jbyteArray byteArray) {
  jbyte *file_data;
  jboolean iscopy;
  file_data = (*env)->GetByteArrayElements(env, byteArray, &iscopy);

  play(file_data, 0, &callback_function, env, thisObj);
  
  if(iscopy == JNI_TRUE) (*env)->ReleaseByteArrayElements(env, byteArray, file_data, 0);
}

jobject AppletAudioClip_decode(JNIEnv *env, jobject thisObj, jbyteArray byteArray) {
  return byteArray;
}

