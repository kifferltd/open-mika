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
** $Id: mailbox.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <oswald.h>

x_status x_mailbox_create(x_mailbox mailbox) {

  return x_event_init(&mailbox->Event, xe_mailbox);
  
}

inline static x_status x_mailbox_try_send(x_mailbox mailbox, void * message, const x_boolean decrement_competing) {

  if (x_event_is_deleted(mailbox)) {
    if (decrement_competing) {
      mailbox->Event.n_competing--;
    }
    return xs_deleted;
  }

  if (x_event_type_bad(mailbox, xe_mailbox)) {
    return xs_bad_element;
  }

  if (mailbox->message == NULL) {
    mailbox->message = message;
    x_event_signal_all(&mailbox->Event);
    return xs_success;
  }

  return xs_no_instance;
    
}

x_status x_mailbox_send(x_mailbox mailbox, void * message, x_sleep timeout) {

  x_status status;
  x_thread thread = thread_current;

  if (x_in_context_critical(timeout)) {
    return xs_bad_context;
  }
  
  x_preemption_disable;

  /*
  ** See if the message is NULL so that we can put our new message in. If that is the
  ** case, we signal the event to indicate that there is a message waiting...
  **
  ** If the mailbox is occupied, we can only compete untill we find the message slot to be
  ** NULL or timeout...
  */

  status = x_mailbox_try_send(mailbox, message, false);
  if (status == xs_no_instance) {
    if (timeout) {
      while (timeout && status == xs_no_instance) {
        timeout = x_event_compete_for(thread, &mailbox->Event, timeout);
        status = x_mailbox_try_send(mailbox, message, true);
      }
    }
  }

  x_preemption_enable;

  return status;
    
}

inline static x_status x_mailbox_try_receive(x_mailbox mailbox, void ** message, const x_boolean decrement_competing) {

  if (x_event_is_deleted(mailbox)) {
    if (decrement_competing) {
      mailbox->Event.n_competing--;
    }
    return xs_deleted;
  }

  if (x_event_type_bad(mailbox, xe_mailbox)) {
    return xs_bad_element;
  }

  if (mailbox->message != NULL) {
    *message = (void *)mailbox->message;
    mailbox->message = NULL;
    x_event_signal_all(&mailbox->Event);
    return xs_success;
  }

  return xs_no_instance;
  
}

x_status x_mailbox_receive(x_mailbox mailbox, void ** message, x_sleep timeout) {

  x_status status;
  x_thread thread = thread_current;

  if (x_in_context_critical(timeout)) {
    return xs_bad_context;
  }
  
  x_preemption_disable;

  status = x_mailbox_try_receive(mailbox, message, false);
  if (status == xs_no_instance) {
    if (timeout) {
      x_event_compete_for(thread, &mailbox->Event, timeout);
      status = x_mailbox_try_receive(mailbox, message, true);
    }
  }

  x_preemption_enable;
  
  return status;
  
}

x_status x_mailbox_delete(x_mailbox mailbox) {

  x_status status;
  
  x_preemption_disable;
  status = xi_event_destroy(&mailbox->Event);
  x_preemption_enable;
  
  return status;
  
}
