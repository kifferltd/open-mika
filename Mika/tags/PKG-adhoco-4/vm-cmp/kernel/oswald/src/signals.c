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
** $Id: signals.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <oswald.h>

x_status x_signals_create(x_signals signals) {

  signals->flags = 0x00000000;
  return x_event_init(&signals->Event, xe_signals);

}

x_status x_signals_delete(x_signals signals) {

  x_status status;
  
  x_preemption_disable;
  status = xi_event_destroy(&signals->Event);
  x_preemption_enable;
  
  return status;

}

/*
** Return true when the option is xo_and or xo_and_clear. We make use of the fact that the
** 'xo_and' and 'xo_and_clear' options both have their LSB bit set and the 'or' options
** dont have this bit set.
*/

inline static x_boolean x_option_is_and(x_option option) {
  return (option & 0x00000001);
}

/*
** Return true when the option is xo_and_clear or xo_or_clear. We make use of the fact that the
** clear options have the second bit set
*/

inline static x_boolean x_option_is_clear(x_option option) {
  return (option & 0x00000002);
}

inline static x_status x_signals_try_get(x_signals signals, x_flags concerned, x_option option, x_flags * actual, const x_boolean decrement_competing) {

  if (x_event_is_deleted(signals)) {
    if (decrement_competing) {
      signals->Event.n_competing--;
    }
    return xs_deleted;
  }

  if (x_event_type_bad(signals, xe_signals)) {
    return xs_bad_element;
  }

  if (x_option_is_and(option)) {
    if ((signals->flags & concerned) == concerned) {
      *actual = signals->flags;
      if (option == xo_and_clear) {
        signals->flags &= ~concerned;
      }
      return xs_success;
    }
    else {
      return xs_no_instance;
    }
  }
  else {
    if (signals->flags & concerned) {
      *actual = signals->flags;
      if (option == xo_or_clear) {
        signals->flags &= ~concerned;
      }
      return xs_success;
    }
    else {
      return xs_no_instance;
    }
  }

}

x_status x_signals_get(x_signals signals, x_flags concerned, x_option option, x_flags * actual, x_sleep timeout) {

  x_status status;
  x_thread thread = thread_current;

  if (option >= xo_unknown) {
    return xs_bad_option;
  }

  if (x_in_context_critical(timeout)) {
    return xs_bad_context;
  }

  x_preemption_disable;
    
  status = x_signals_try_get(signals, concerned, option, actual, false);
  if (status != xs_success) {
    if (timeout) {
      while (status == xs_no_instance && timeout) {
        timeout = x_event_compete_for(thread, &signals->Event, timeout);
        status = x_signals_try_get(signals, concerned, option, actual, true);
      }
    }
  }

  /*
  ** If the operation was successfull and if the option was one that required clearing 
  ** of some flags, we need to signal everybody on the competing list.
  */

  if (status == xs_success && x_option_is_clear(option)) {
    x_event_signal_all(&signals->Event);
  }

  x_preemption_enable;
  
  return status;

}

x_status x_signals_set(x_signals signals, x_flags concerned, x_option option) {

  x_status status;
  
  if (option > xo_and) {
    return xs_bad_option;
  }

  x_preemption_disable;

  if (x_event_is_deleted(signals)) {
    status = xs_deleted;
  }
  else if (x_event_type_bad(signals, xe_signals)) {
    status = xs_bad_element;
  }
  else {
    if (option == xo_or) {
      signals->flags |= concerned;
    }
    else {
      signals->flags &= concerned;
    }

    x_event_signal_all(&signals->Event);
    status = xs_success;
  }

  x_preemption_enable;

  return status;

}
