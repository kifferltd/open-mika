/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
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
