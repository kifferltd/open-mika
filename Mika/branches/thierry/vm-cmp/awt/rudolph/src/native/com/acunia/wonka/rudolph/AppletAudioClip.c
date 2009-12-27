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

#include <jni.h>

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

