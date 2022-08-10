#ifndef _XMISC_H
#define _XMISC_H

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

typedef enum {
  xs_success             =  0,    /* Guess...                                                                        */
  xs_no_instance         =  1,    /* Could not get resource within timeout specified.                                */
  xs_not_owner           =  2,    /* Trying to unlock a mutex which this thread doesn't own currently not used.      */
  xs_deadlock            =  3,    /* Trying to lock a mutex which is allready locked by current thread.              */
  xs_bad_context         =  4,    /* Call with timeout within IRQ context.                                           */
  xs_bad_option          =  5,    /* A bad option was passed to a signals function.                                  */
  xs_deleted             =  6,    /* The element was deleted when a call to it was tried.                            */
  xs_bad_element         =  7,    /* The call was tried on a element that was invalid.                               */
  xs_competing           =  8,    /* The call could not be completed because threads were competing for the element. */
  xs_incomplete          =  9,    /* The call did not complete fully (only with _delete calls and waiters)           */
  xs_tick_error          = 10,    /* Invalid tick value supplied.                                                    */
  xs_timer_error         = 11,    /* Timer error                                                                     */
  xs_activate_error      = 12,    /* Timer activation error.                                                         */
  xs_bad_state           = 13,    /* Element is in bad state for this operation.                                     */
  xs_insufficient_memory = 14,    /* Insufficient memory to perform operation.                                       */
  xs_bad_argument        = 15,    /* One of the passed arguments is bad or out of range.                             */
  xs_owner               = 16,    /* Result of suspend call when the thread is still owning mutexes or monitors.     */
  xs_not_elf             = 17,    /* (M) The passed data is not ELF compatible.                                      */
  xs_mod_error           = 18,    /* (M) The module is in error. The module->flags can give more information.        */
  xs_seq_error           = 19,    /* (M) An operation has been attempted for which a previous step must be performed.*/
  xs_no_mem              = 20,    /* (M) No more memory to perform operation.                                        */  
  xs_rel_error           = 21,    /* (M) Error happened with relocation. Unhandled type.                             */  
  xs_sym_defined         = 22,    /* (M) Symbol multiply defined.                                                    */
  xs_unresolved          = 23,    /* (M) Unresolved symbols remain in module after x_module_resolve attempt.         */
  xs_deferred            = 24,    /* Operation has been deferred (for nestor).                                       */
  xs_interrupted         = 25,    /* Thread was interrupted during call.                                             */
  xs_unknown             = 26,    /* Catch all for unknown situations.                                               */
} x_status;

const char * x_status2char(x_status status);

#define isSet(x, flag)            ((x) & (flag))
#define isNotSet(x, flag)         (!isSet((x), (flag)))
#define setFlag(x, flag)          ((x) |= (flag))
#define unsetFlag(x, flag)        ((x) &= ~(flag))
#define maskFlags(m, f)           ((m) & (f))

/*
** Macros to manipulate doubly linked lists. These macro's require an
** immutable first sentinel entry that is setup by means of the list_init
** macro.
**
** In the following macro's, 'f' refers to this first sentinel entry and 'x'
** refers to the to be manipulated list entry.
**
** These macros will only compile if the 'f' and 'l' types passed are pointers 
** to structures that contain a literal 'next' and 'previous' structure element.
*/

#define x_list_remove(x) {                             \
  (x)->previous->next = (x)->next;                     \
  (x)->next->previous = (x)->previous;                 \
}

#define x_list_prune_from(f, x) {                      \
  (x)->next = (f);                                     \
  (f)->previous = (x);                                 \
}

/*
** Note that in the following macro, order is important. We run over
** lists via the next pointer so we want the next to be setup
** correctly before we insert the item.
*/

#define x_list_insert(f, x) {                          \
  (x)->next = (f);                                     \
  (x)->previous = (f)->previous;                       \
  (f)->previous->next = (x);                           \
  (f)->previous = (x);                                 \
}

#define x_list_insert_begin(f, x) {                    \
  (x)->next = (f)->next;                               \
  (x)->previous = (f);                                 \
  (f)->next->previous = (x);                           \
  (f)->next = (x);                                     \
}

#define x_list_init(f) {                               \
  (f)->next = (f);                                     \
  (f)->previous = (f);                                 \
}

#define x_list_is_empty(f) ((f)->next == (f))

// If f is empty, x_list_peek(f) returns null.
// Otherwise, x_list_peek(f) returns the first element in f without removing it.
#define x_list_peek(f) (x_list_is_empty(f) ? 0 : (f)->next )

#define x_list_rotate(f) 

#endif /* _XMISC_H */
