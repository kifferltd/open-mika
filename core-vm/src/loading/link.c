/**************************************************************************
* Copyright (c) 2010, 2015 by Chris Gray, KIFFER Ltd. All rights reserved.*
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
*  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,    *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

#include "chars.h"

#include "clazz.h"
#include "constant.h"
#include "exception.h"
#include "loading.h"
#include "wonka.h"

/*
** The ``linked'' state is the last state before class initialisation.
** Currently not much work is done here: however this is probably the
** appropriate moment to invoke a bytecode verifier.
*/

static w_int linkClazz(w_clazz clazz) {
  w_int    result = CLASS_LOADING_DID_NOTHING;

  // nothing to do ...
  result = CLASS_LOADING_SUCCEEDED;

  return result;
}

w_int mustBeLinked(w_clazz clazz) {
  w_thread thread = currentWonkaThread;
  w_int    state = getClazzState(clazz);
  w_int    result = CLASS_LOADING_DID_NOTHING;
  x_status monitor_status;

#ifdef RUNTIME_CHECKS
  threadMustBeSafe(thread);

  switch (state) {
  case CLAZZ_STATE_UNLOADED:
  case CLAZZ_STATE_LOADING:
    wabort(ABORT_WONKA, "%K must be loaded before it can be linked\n", clazz);

  case CLAZZ_STATE_VERIFYING:
  case CLAZZ_STATE_VERIFIED:
    wabort(ABORT_WONKA, "Class state VERIFYING/VERIFIED doesn't exist yet!");

  default:
    ;
  }

  if (exceptionThrown(thread)) {
    woempa(9, "Eh? Exception '%e' already pending in mustBeLinked(%K)\n", exceptionThrown(thread), clazz);
  }
#endif

  if (state == CLAZZ_STATE_BROKEN) {
  // TODO - is the right thing to throw?
    throwException(thread, clazzNoClassDefFoundError, "%k : %w", clazz, clazz->failure_message);

    return CLASS_LOADING_FAILED;

  }

  if (state >= CLAZZ_STATE_LINKED) {

    return CLASS_LOADING_DID_NOTHING;

  }

  result = mustBeReferenced(clazz);

  if (result == CLASS_LOADING_FAILED) {

    return CLASS_LOADING_FAILED;

  }

  x_monitor_eternal(clazz->resolution_monitor);
  state = getClazzState(clazz);

#ifdef RUNTIME_CHECKS
  if(state < CLAZZ_STATE_REFERENCED) {
    wabort(ABORT_WONKA,"INVALID CLAZZ STATE %d for %K",state,clazz);
  }
#endif

  while(state == CLAZZ_STATE_LINKING) {
    if(clazz->resolution_thread == thread) {
      if (isSet(verbose_flags, VERBOSE_FLAG_INIT)) {
        w_printf("Link %w: failed\n", clazz->dotified);
      }
      setClazzState(clazz, CLAZZ_STATE_BROKEN);
      throwException(thread, clazzLinkageError, "Linking of %k failed", clazz);
      saveFailureMessage(thread, clazz);
      x_monitor_notify_all(clazz->resolution_monitor);
      x_monitor_exit(clazz->resolution_monitor);

      return CLASS_LOADING_FAILED;
    }
    monitor_status = x_monitor_wait(clazz->resolution_monitor, CLASS_STATE_WAIT_TICKS);
    state = getClazzState(clazz);
  }

  if (state == CLAZZ_STATE_REFERENCED) {
    woempa(1, "Linking %K\n", clazz);
    setClazzState(clazz, CLAZZ_STATE_LINKING);
#ifdef RUNTIME_CHECKS
    if (clazz->resolution_thread) {
      wabort(ABORT_WONKA, "clazz %k resolution_thread should be NULL\n", clazz);
    }
#endif
    clazz->resolution_thread = thread;
    x_monitor_exit(clazz->resolution_monitor);

    result = linkClazz(clazz);

    x_monitor_eternal(clazz->resolution_monitor);
#ifdef RUNTIME_CHECKS
    if (clazz->resolution_thread != thread) {
      wabort(ABORT_WONKA, "clazz %k resolution_thread should be %p\n", clazz, thread);
    }
#endif
    clazz->resolution_thread = NULL;
    if (result == CLASS_LOADING_FAILED) {
      if (isSet(verbose_flags, VERBOSE_FLAG_INIT)) {
        w_printf("Link %w: linkClazz returned CLASS_LOADING_FAILED\n", clazz->dotified);
      }
      setClazzState(clazz, CLAZZ_STATE_BROKEN);
      saveFailureMessage(thread, clazz);
      x_monitor_notify_all(clazz->resolution_monitor);
      x_monitor_exit(clazz->resolution_monitor);

      return result;

    }

    if(exceptionThrown(thread)) {
      if (isSet(verbose_flags, VERBOSE_FLAG_INIT)) {
        w_printf("Link %w: linkClazz threw %e\n", clazz->dotified, exceptionThrown(thread));
      }
      setClazzState(clazz, CLAZZ_STATE_BROKEN);
      saveFailureMessage(thread, clazz);
      result = CLASS_LOADING_FAILED;
    }
    else {
      setClazzState(clazz, CLAZZ_STATE_LINKED);
    }
    x_monitor_notify_all(clazz->resolution_monitor);
  }
  else if (state == CLAZZ_STATE_BROKEN) {
    x_monitor_exit(clazz->resolution_monitor);

    return CLASS_LOADING_FAILED;

  }

  x_monitor_exit(clazz->resolution_monitor);

  return result;
}

