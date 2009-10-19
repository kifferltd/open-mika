/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
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

/*
** $Id: exception.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include "oswald.h"

void x_exception_push(x_exception exception) {

  x_thread thread = thread_current;

  /*
  ** Initialize the exception itself.
  */
  
  exception->thrown = NULL;
  exception->fired = false;
  exception->callbacks = &exception->Callbacks;
  x_list_init(exception->callbacks);  

  /*
  ** Link it in the thread control block.
  */
  
  x_preemption_disable;

  exception->previous = thread->l_exception;
  thread->l_exception = exception;

  x_preemption_enable;

}

volatile void * x_exception_pop(void) {

  x_thread thread = thread_current;
  x_exception exception;
  x_xcb xcb;
  x_xcb victim;

//  loempa(9, "Popping exception 0x%08x, thrown = 0x%08x, fired = %s\n", exception, exception->thrown, exception->fired ? "!!!!! YES !!!!!" : "NO");

  x_preemption_disable;
  
  exception = thread->l_exception;
  thread->l_exception = exception->previous;

  if (exception->callbacks->previous != exception->callbacks) {
    xcb = exception->callbacks->previous;
    while (xcb != exception->callbacks) {
      if (exception->fired) {
        xcb->cb(xcb->arg);
      }
      victim = xcb;
      xcb = xcb->previous;
      x_list_remove(victim);
      x_mem_free(victim);
    }
  }

  x_preemption_enable;
    
  return exception->thrown;
  
}

x_status x_Throw(void * thrown) {

  x_exception exception;

  x_preemption_disable;

  exception = thread_current->l_exception;
  
  /*
  ** Note that we don't return xs_success as this function will not
  ** really return. When there is an execption in the current thread, we
  ** will leave through the x_context_restore, if not, we signal that there
  ** is no exception x_try/x_Catch block in scope, by returning xs_no_instance.
  */
    
  if (exception) {
    exception->thrown = (volatile void *)thrown;
    x_preemption_enable;
    x_context_restore(exception, 1);
  }

  x_preemption_enable;
  
  return xs_no_instance;
  
}

/*
** Throw an exception to another thread. When the other thread doesn't have
** an exception handler active, this function returns xs_no_instance, otherwise
** it returns xs_success. The target thread will re-throw the catapulted
** exception as soon as we have threadswitched to it...
*/

x_status x_Catapult(x_thread target, void * thrown) {

  x_exception exception;
  x_status status = xs_no_instance;
  
  x_preemption_disable;

  exception = target->l_exception;

  if (exception) {
    if (! target->catapulted) {
      target->catapulted = thrown;
      status = xs_success;
    }
  }
  
  x_preemption_enable;

  return status;

}

x_status x_exception_callback(x_exception_cb cb, void * arg) {

  x_status status = xs_no_instance;
  x_exception exception;
  x_xcb xcb;

  x_preemption_disable;

  exception = thread_current->l_exception;
  
  if (exception) {
    xcb = x_mem_alloc(sizeof(x_Xcb));
    if (xcb) {
      xcb->cb = cb;
      xcb->arg = arg;
      x_list_insert(exception->callbacks, xcb)
      status = xs_success;
    }
  }

  x_preemption_enable;

  return status;
  
}
