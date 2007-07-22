/**************************************************************************
* Copyright (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                                    *
**************************************************************************/

/*
** $Id: JDWP.c,v 1.1 2006/10/04 14:24:17 cvsroot Exp $
*/

#include "core-classes.h"

#ifdef JDWP
#include "jdwp.h"

w_boolean JDWP_static_isEnabled(JNIEnv *env, w_instance classJDWP) {
  return TRUE;
}

w_boolean JDWP_static_isRunning(JNIEnv *env, w_instance theJDWP) {
  return jdwp_state != jdwp_state_unstarted;
}

void JDWP_run(JNIEnv *env, w_instance theJDWP) {
  jdwp_dispatcher();
}

#else

w_boolean JDWP_static_isEnabled(JNIEnv *env, w_instance classJDWP) {
  return FALSE;
}

w_boolean JDWP_static_isRunning(JNIEnv *env, w_instance theJDWP) {
  // Don't wait before entering main()
  return TRUE;
}

void JDWP_run(JNIEnv *env, w_instance theJDWP) {
  // Just return
}

#endif
