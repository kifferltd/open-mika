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
