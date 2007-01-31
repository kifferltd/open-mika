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


/* $Id: Toolkit.c,v 1.2 2005/06/14 09:46:04 cvs Exp $ */

#include "arrays.h"        // instance2Array_*
#include "hashtable.h"     // ht_register
#include "wstrings.h"      // cstring2String()
#include "threads.h"       // currentWonkaThread

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

