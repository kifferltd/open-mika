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

#include "arrays.h"        // instance2Array_*
#include "hashtable.h"     // ht_register
#include "wstrings.h"      // ascii2String()
#include "mika_threads.h"       // currentWonkaThread

#include "awt-classes.h"
#include "rudolph.h"       // defaultToolkit
#include "platform.h"      // defaultToolkit
#include "registry.h"

#include "Image.h"
#include "canvas.h"

w_instance defaultEventQueue;

jobject Toolkit_getSystemEventQueue(JNIEnv *env, jobject thisToolkit) {
  return defaultEventQueue;
}

jint Toolkit_getScreenWidth(JNIEnv *env, jobject thisToolkit) {
  return screen->width;
}

jint Toolkit_getScreenHeight(JNIEnv *env, jobject thisToolkit) {
  return screen->height;
}

static int loop = 0;

void Toolkit_sync(JNIEnv *env, jobject thisToolkit) {
  return;
  if(loop) return;
  loop = 1;
  rudolph_lock();
  canvas_drawCanvas(rootCanvas, 0);  
  rudolph_unlock();
  loop = 0;
}

