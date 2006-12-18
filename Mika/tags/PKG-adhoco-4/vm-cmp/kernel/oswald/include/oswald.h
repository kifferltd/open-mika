#ifndef _OSWALD_H
#define _OSWALD_H

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

#include <stdlib.h>
#include <types.h>
#include <stdarg.h>

/*
** Oswald's boolean enumeration type.
*/

typedef enum {
  false = 0x00000000,
  true  = 0x00000001,
} x_boolean;

#include "host.h"
#include "cpu.h"
#include "vsprintf.h"
#include "atomic.h"
#include "xmisc.h"

#define NUM_PRIORITIES            64
#define NUM_HARD_PRIORITIES       62
#define MAX_PRIORITY              128
#define MIN_PRIORITY              1
#define NUM_SOFT_PRIORITIES       (MAX_PRIORITY - NUM_HARD_PRIORITIES)
#define MIN_SOFT_PRIORITY         (NUM_HARD_PRIORITIES + 1)
#define NUM_GROUPS                (NUM_PRIORITIES / 8)        /* 8 bits per bytes */

typedef struct x_Ident *          x_ident;
typedef struct x_Symbol *         x_symbol;
typedef struct x_Module *         x_module;
typedef struct x_Symtab *         x_symtab;
typedef struct x_Elf *            x_elf;
typedef struct x_Umap *           x_umap;
typedef struct x_Map *            x_map;
typedef struct x_Block *          x_block;
typedef struct x_Timer *          x_timer;
typedef struct x_Monitor *        x_monitor;
typedef struct x_Event *          x_event;
typedef struct x_Mutex *          x_mutex;
typedef struct x_Queue *          x_queue;
typedef struct x_Sem *            x_sem;
typedef struct x_Signals *        x_signals;
typedef struct x_Mailbox *        x_mailbox;
typedef struct x_Pcb *            x_pcb;
typedef struct x_Thread *         x_thread;
typedef struct x_Stack *          x_stack;
typedef struct x_Exception *      x_exception;
typedef struct x_Irq *            x_irq;
typedef x_size                    x_sleep;
typedef x_word                    x_flags;

typedef void (*x_mod_special)(void); // The function pointer declaration for special module functions. */
typedef void * (*x_malloc)(x_size size);
typedef void (*x_free)(void * mem);
typedef void (*x_entry)(void *argument);
typedef void (*x_action)(x_thread thread);
typedef void (*x_fire)(x_event event);
typedef void (*x_handler)(x_thread thread, x_word state, void * arg);
typedef const char * (*x_report)(x_thread thread);
typedef void (*x_exception_cb)(void * arg);

/*
** The timeout window enumeration.
*/

typedef enum {
  x_no_wait = 0x00000000,
  x_eternal = 0xffffffff,
} x_window;

void * xi_alloc_static_mem(x_ubyte ** memory, x_size size);
#define x_alloc_static_mem(m, s)       xi_alloc_static_mem(&(m), s)

x_ubyte * x_os_main(int argc, char** argv, x_ubyte *memory);
void x_kernel_setup(x_ubyte *memory);
void x_init_entry(void *memory);
x_status x_oswald_init(x_size max_heap, x_size millis_per_tick);

/*
** The number of time ticks passed since last reset.
*/

extern volatile unsigned int system_ticks;
typedef unsigned int x_time;

inline static x_time x_time_get(void) {
  return system_ticks;
}

inline static void x_time_set(x_time ticks) {
  system_ticks = ticks;
}

x_word x_random(void);
void x_init_random(x_thread thread);

extern int command_line_argument_count;
extern char **command_line_arguments; 

typedef void (*x_do_irq)(x_irq);

typedef struct x_Irq {
  x_do_irq top;
  void *cargo;
} x_Irq;

extern volatile x_size irq_depth;
extern const x_size nr_irq;
extern x_irq *irq_handlers;

extern x_irq irq_default;
extern x_irq irq_tick;

x_ubyte *setup_irqs(x_ubyte *memory);

typedef void (*x_revolver)(x_pcb);

typedef struct x_Pcb {
  x_ubyte x_pos;
  x_ubyte y_pos;
  x_ubyte x_bit;
  x_ubyte y_bit;
  volatile x_thread t_ready;      /* pointer to next thread to run in this priority block */
  volatile x_thread t_pending;    /* all threads pending in this priority block */
  x_revolver revolver;            /* a revolver function for this priority block */
} x_Pcb;

x_pcb x_prio2pcb(x_size prio);

x_status x_thread_resume(x_thread thread);
void xi_thread_add_pcb(x_thread thread);

void xi_thread_becomes_pending(x_thread, x_action action, x_sleep timeout, x_word state);
void xi_thread_remove_pending(x_thread thread);

x_size x_thread_priority_get(x_thread thread);

x_status x_thread_priority_set(x_thread thread, x_size newprio);
void xi_thread_priority_set(x_thread thread, x_size newprio);

void xi_pending_tick(void);
void xi_thread_reschedule(void);
void xi_sleep_timeout_action(x_thread thread);
void xi_event_timeout_action(x_thread thread);
void xi_wait_timeout_action(x_thread thread);

void x_pcbs_setup(void);

x_word x_cpsr(void);
x_word x_spsr(void);
void disable_interrupts(void);
void enable_interrupts(void);

void report_thread(x_thread thread);

typedef struct x_Thread {
  x_cpu cpu;                      /* CPU structure holding information for thread switches and context saves/restores.                   */
  x_ubyte * s_sp;                 /* the saved stackpointer of a thread waiting for a context switch                                     */
  x_ubyte * b_stack;              /* begin of user supplied stack (lower in memory)                                                      */
  x_ubyte * e_stack;              /* end of user supplied stack and starting point for pushes (higher in memory)                         */
  x_word * trigger;               /* Points to a word on the stack, used for checking stack use.                                         */
  x_stack stack;
  x_entry entry;
  void * argument;
  x_ushort id;
  x_ubyte a_prio;                 /* assigned priority.                                                                                  */
  volatile x_ubyte c_prio;        /* current priority, used against priority inversion.                                                  */
  x_ubyte c_quantums;             /* How many quantums does this thread has left.                                                        */
  x_ubyte a_quantums;             /* Assigned number of quantums                                                                         */
  volatile x_ubyte state;         /* The state a thread is in; check out the x_type enum below. Numbers correspond with event types.     */
  x_size num_switches;            /* The number of times that the thread as switched context.                                            */
  x_report report;                /* a report function for this thread.                                                                  */
  void * xref;                    /* A pointer for user defined purposes.                                                                */
  volatile x_flags flags;         /* Thread flags and suspend counter in upper 16 bits.                                                  */
  volatile x_thread next;         /* The next on the list of threads waiting in the pending or ready list.                               */
  volatile x_thread previous;     /* The previous on the list of threads waiting in the pending or ready list.                           */

  // related to sleeps and timeouts

  volatile x_sleep sticks;        /* Number of ticks to sleep OR remaining at the time we were removed from the pending list.            */
  volatile x_sleep wakeup;        /* the ticker at time of wakeup to be used in timeout recalculations                                   */
  volatile x_thread snext;        /* The next thread that is sleeping or NULL if last thread sleeping.                                   */
  x_action action;

  // related to an event the thread is competing for or the list of events he owns...

  volatile x_thread l_waiting;    /* Threads list 'waiting' on a monitor; it starts at the monitor->l_waiting field.                     */
  volatile x_monitor waiting_for; /* The monitor the thread is waiting on. The thread can only be waiting on a single monitor at a time. */
  volatile x_thread l_competing;  /* The threads link in the list of threads competing for the event in the next field.                  */
  volatile x_event competing_for; /* The event this thread is competing for to happen (signal)                                           */
  volatile x_event l_owned;       /* The list of events that this thread owns, links further through x_event->l_owned.                   */
  volatile x_size m_count;        /* The saved x_monitor->count when a thread is waiting on a monitor.                                   */
  volatile x_thread l_joining_with;/* The list of threads that is trying to join the 'joining_with' thread.                              */
  void * join_result;             /* The result argument of an x_thread_join(x_thread thread, void ** result) call.                      */
  volatile x_thread joining_with; /* The thread we are trying to join with.                                                              */
  volatile x_thread l_joining_us; /* List of threads that is trying to join with us.                                                     */

  x_exception l_exception;        /* List of outstanding exception Try/Catch blocks                                                      */
  volatile void * catapulted;     /* An exception that another thread threw upon us.                                                     */
  x_word * fptr;                  /* State variable for the per thread random number generator.                                          */
  x_word * rptr;                  /* State variable for the per thread random number generator.                                          */
  x_int tsd_errno;                /* The per thread variable holding the last errno of an operation this thread performed in libc.       */

#ifdef JAVA_PROFILE
  x_long time_delta;
  x_long time_last;
#endif
} x_Thread;


#ifdef JAVA_PROFILE

#include <sys/time.h>

static inline x_long x_systime_get(void) {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return tv.tv_sec * 1000000 + tv.tv_usec;
}
#endif


/*
** A virtual stack for Oswald. Note that the implementation is very OS specific. See the
** hal/host/src/host.c file for implementation. CURRENTLY UNIMPLEMENTED
*/

typedef struct x_Stack {
  x_stack next;
  x_stack previous;
  x_thread thread;                /* The thread we are associated with. */
  x_uword * top;                  /* Address of the first word of the stack that is useable. */
  x_ubyte * first;                /* Address of first page of the stack. */
  x_ubyte * guard;                /* Current guard page that is write/read protected. */
  x_ubyte * final;                /* Final protection page; only used for checking overruns. */
  x_ushort used;                  /* Number of pages allocated. */
  x_ushort capacity;              /* Maximum number of pages allocatable for stack use. */
} x_Stack;

x_status x_stack_create(x_stack stack, x_size size, x_thread thread);
x_status x_stack_delete(x_stack stack);

#define STACK_EXHAUSTED (void *)(-1)

/*
** If you change this enum, also change _state2char in debug.c and the x_type enum 
** in this file. Some thread states correspond to an event type!
*/

typedef enum {
  xt_ready        =  0,
  xt_mutex        =  1,
  xt_queue        =  2,
  xt_mailbox      =  3,
  xt_semaphore    =  4,
  xt_signals      =  5,
  xt_monitor      =  6,
  xt_block        =  7,
  xt_map          =  8,
  xt_joining      =  9,
  xt_waiting      = 10,
  xt_suspended    = 11,
  xt_sleeping     = 12,
  xt_rescheduled  = 13,
  xt_ended        = 14,
  xt_unknown      = 15,
} x_state;

#define TF_START              0x00000100 /* Special value to pass to x_thread_create to immediately start a thread. */
#define TF_ETERNAL            0x00000200 /* Thread is eternally sleeping */
#define TF_SUSPENDED          0x00000400 /* Thread is currently suspended */
#define TF_TIMER              0x00000800 /* Special flag for the timer handler thread. */
#define TF_JOIN_ENDED         0x00001000 /* Thread we joined with has ended normally. */
#define TF_JOIN_EXIT          0x00002000 /* Thread we joined with has called x_thread_exit. */
#define TF_JOIN_DELETED       0x00004000 /* Thread we joined with has been suspended and then deleted. */
#define TF_VIRTUAL_STACK      0x00008000 /* Thread has a virtual x_stack structure as stack. */
#define TF_STACK_FINAL        0x00010000 /* Thread is running on last page of stack! */
#define TF_WAIT_INT           0x00020000 /* Thread was waiting on monitor, but interrupted by another thread. */
#define TF_SPARE_3            0x00040000
#define TF_SPARE_4            0x00080000
#define TF_STATE_MASK         0x000000ff /* Saved thread status in suspend, one of the above xt_ states. */
#define TF_COUNTER_MASK       0xfff00000 /* Mask to keep count of number of nested suspends/resumes. */
#define TF_COUNTER_SHIFT      (20)       /* Number of bits to shift down to achieve the nested count. */
#define TF_COUNT_ONE          0x00100000 /* Nested count is set to 1. */

#define MIN_STACK_SIZE        (1024 * 3)

/*
** Function to get the state of a thread in char format.
*/

const char * x_state2char(x_thread thread);

/*
** Functions to save and retrieve the saved state in the thread flags.
*/

inline static void x_set_saved_state(x_thread thread, x_state state) {
  thread->flags &= ~ TF_STATE_MASK;
  thread->flags |= (TF_STATE_MASK & state);
}

inline static x_state x_get_saved_state(x_thread thread) {
  return (x_state)(thread->flags & TF_STATE_MASK);
}

inline static x_state x_thread_state(x_thread thread) {
  return thread->state;
}

/*
** Any state that is not 'xt_ready' is a pending state. We make an exception for the xt_ended
** state since this is not really a pending state. 
*/

inline static x_boolean isPending(x_thread thread) {
  return ! (thread->state == xt_ready || thread->state == xt_ended);
}

/*
** Only the state 'xt_ready' state makes a thread ready to run.
*/

inline static x_boolean isReady(x_thread thread) {
  return (thread->state == xt_ready);
}

extern volatile x_thread thread_current;
extern volatile x_thread thread_next;

inline static x_thread x_thread_current(void) {
  return thread_current;
}

inline static x_ushort x_thread_id(x_thread thread) {
  return thread->id;
}

extern x_thread thread_init;
extern x_thread thread_idle;
extern x_thread thread_timer;
extern x_int thread_count;

/*
** The lists for pending threads; one list is for threads that are eternally pending
** for an event to happen, the other list is for bounded waits.
*/

extern volatile x_thread sleepers;
extern volatile x_thread eternals;

void x_stack_init(x_thread thread);
x_status x_thread_create(x_thread thread, x_entry entry, void *argument, x_ubyte *stack, x_size size, x_size prio, x_flags flags);

x_status x_thread_suspend(x_thread thread);
x_status x_thread_suspend_cb(x_thread thread, x_boolean (*cb)(x_event event, void * arg), void * arg);
x_status x_thread_delete(x_thread thread);
x_status x_thread_join(x_thread thread, void ** result, x_window window);
void x_thread_exit(void * result);

x_status x_thread_quantums_set(x_thread thread, x_size quantums);

x_status x_thread_wakeup(x_thread thread);

/*
** The critical status is a variable that controls wether threads
** can preemptively interrupted or not. Use with much caution...
*/

extern volatile x_word critical_status;

#define x_preemption_disable     (critical_status++)
#define x_preemption_enable      (critical_status--)

void x_thread_sleep(x_sleep ticks);
void x_thread_start(x_thread thread);
void x_start_kernel(x_ubyte *memory);
void x_stack_info(x_thread thread, x_size * size, x_size * used, x_size * left);

void x_thread_switch(x_thread t_current, x_thread t_next);
void x_thread_switched(x_thread thread);
x_status x_thread_yield(void);

x_boolean x_enter_atomic(void * address, void * contents);
x_boolean x_exit_atomic(void * address);

/*
** The different event types. Note that the values for the types correspond
** to the different threads states declared in scheduler.h. This way, we can use
** the event types to set threads states and vice versa. Keep this enum thus
** ALWAYS synchronised with the thread states enum.
*/

typedef enum {
  xe_unused    =  0,              /*                                                                                                       */
  xe_mutex     =  1,              /* Synchronized with x_state event type.                                                                 */
  xe_queue     =  2,              /* Synchronized with x_state event type.                                                                 */
  xe_mailbox   =  3,              /* Synchronized with x_state event type.                                                                 */
  xe_semaphore =  4,              /* Synchronized with x_state event type.                                                                 */
  xe_signals   =  5,              /* Synchronized with x_state event type.                                                                 */
  xe_monitor   =  6,              /* Synchronized with x_state event type.                                                                 */
  xe_block     =  7,              /* Synchronized with x_state event type.                                                                 */
  xe_map       =  8,              /* Synchronized with x_state event type.                                                                 */
  xe_deleted   =  9,              /*                                                                                                       */
  xe_unknown   = 10,              /*                                                                                                       */
} x_type;

typedef struct x_Event {
  volatile x_ushort flags_type;   /* The event flags and the event type, compressed together in a 16 bit field. Keep as first field...     */
  volatile x_ushort n_competing;  /* The number of threads competing for this event; keep it after the flags_type field for good packing.  */
  volatile x_thread l_competing;  /* The sorted (by c_prio) list of threads competing, links further through x_thread->l_competing.        */
  volatile x_event l_owned;       /* List of events that are owned by a certain thread, starts from x_thread->l_owned field.               */
} x_Event;

/*
** The different event flags that can be set in the event->flags_type field.
*/

#define EVENT_FLAG_DELETED   0x8000 /* Event is deleted flag. */
#define EVENT_FLAG_SUSPENDED 0x4000 /* When set, the event is suspended and no read/write operations can be performed. */

#define EVENT_FLAG_MASK      0xff00 /* The mask to extract event flags. */
#define EVENT_TYPE_MASK      0x00ff /* The mask to extract event types. */

/*
** The different type and flags getting and setting methods.
**
** We start with the function to retrieve the event type.
*/

inline static x_ushort x_event_type_get(x_event event) {
  return (event->flags_type & EVENT_TYPE_MASK);
}

/*
** Set the event type. Note that the type is passed as a 32 bit word to avoid alignment warnings.
** First clear out the lower portion (the old type value) of the field and then melt in the new
** type number.
*/

inline static void x_event_type_set(x_event event, x_word type) {
  event->flags_type &= ~EVENT_TYPE_MASK;
  event->flags_type |= (x_ushort)type & EVENT_TYPE_MASK;
}

/*
** See if the EVENT_FLAG_DELETED is set. We don't mask since we only need a single bit
** of the event->flags_type field. Again, we pass the 'flag' argument as a word to 
** remove word alignment and casting warnings.
*/

inline static x_boolean x_event_flag_is_set(x_event event, x_word fl) {
  return (event->flags_type & (x_ushort)(fl & EVENT_FLAG_MASK));
}

inline static x_boolean x_event_flag_is_not_set(x_event event, x_word fl) {
  return ! (event->flags_type & (x_ushort)(fl & EVENT_FLAG_MASK));
}

inline static void x_event_flag_set(x_event event, x_word fl) { 
  event->flags_type |= fl & EVENT_FLAG_MASK;
}

inline static void x_event_flag_unset(x_event event, x_word fl) { 
  event->flags_type &= ~(fl & EVENT_FLAG_MASK);
}

x_ubyte * x_events_setup(x_ubyte * memory);

x_status x_event_init(x_event event, x_type type);
x_status xi_event_destroy(x_event event);

x_sleep x_event_compete_for(x_thread thread, x_event event, x_sleep timeout);
void x_event_signal(x_event event);
void x_event_signal_all(x_event event);
x_status x_event_abandon(void * e);

void xi_remove_owned_event(x_thread thread, x_event event);
x_size xi_find_safe_priority(x_thread thread);

x_status x_event_join(void * e, x_time ticks);

/*
** Return true when 0 < timeout <= 0x7fffffff. In other words, return true
** when the timeout is not eternal.
*/

inline static x_boolean x_real_timeout(x_sleep timeout) {
  return (x_int)timeout > 0;
}

/*
** Return true when the event is in the deleted state.
*/

inline static x_boolean x_event_is_deleted(void * event) {
  return ((x_event)event)->flags_type & EVENT_FLAG_DELETED;
}


/*
** Attach an event to the list of owned events of a thread. Attach the event
** at the beginning of the list.
*/

inline static void xi_add_owned_event(x_thread thread, x_event event) {
  event->l_owned = thread->l_owned;
  thread->l_owned = event;
}

/*
** An x_Boll is a helper structure for block pools. What is passed to the caller
** of the allocate function is the 'bytes' field of an x_boll, while it's header
** contains a pointer to the block pool. When an x_boll is free (not allocated) the
** header contains a reference to the next free x_boll.
*/

typedef struct x_Boll * x_boll;

typedef struct x_Boll {
  union {
    x_block block;
    x_boll next;
  } header;
  x_ubyte bytes[1];
} x_Boll;

typedef struct x_Block {
  x_Event Event;                  /* Always have this Event structure as the first element for x_event_join to work. */
  x_ushort boll_size;             /* 16 bit value for block size; for packing reasons, keep with next field. */
  volatile x_ushort bolls_left;   /* 16 bits value of how many balls do we have left, ouch. */
  x_size bolls_max;               /* */
  x_size space_size;              /* */
  void * space;
  volatile x_boll bolls;          /* The linked list of remaining bolls. */
} x_Block;

/*
** Block pool API functions
*/

x_status x_block_create(x_block block, x_size size, void * space, x_size space_size);
x_status x_block_allocate(x_block block, void ** ablock, x_sleep timeout);
x_status x_block_release(void * block);
x_status x_block_delete(x_block block);
x_size x_block_calc(x_size block_size, x_size num_blocks);

void x_block_check(x_block block);

/*
** Mailbox specific stuff. A mailbox is like a queue with a single slot message
** space. There can only be a single message in the mailbox at any time.
*/

typedef struct x_Mailbox {
  x_Event Event;                  /* Always have this Event structure as the first element for x_event_join to work. */
  volatile void * message;        /* The message slot of the mailbox. */
} x_Mailbox;

x_status x_mailbox_create(x_mailbox mailbox);
x_status x_mailbox_delete(x_mailbox mailbox);
x_status x_mailbox_send(x_mailbox mailbox, void * message, x_sleep timeout);
x_status x_mailbox_receive(x_mailbox mailbox, void ** message, x_sleep timeout);

/*
** Exception handling stuff. This works much like setjmp/longjmp. Note that the number
** of registers that need saving, and for which space must be allocated in the structure
** is defined by NUM_CALLEE_SAVED. This is defined in the header file for the respective CPU.
** Also the context save and context restore functions are defined in the CPU section.
*/

typedef struct x_Xcb * x_xcb;

typedef struct x_Xcb {
  x_xcb previous;
  x_xcb next;
  x_exception_cb cb;
  void * arg;
} x_Xcb;

typedef struct x_Exception {
  void * pc;
  void * sp;
  unsigned int registers[NUM_CALLEE_SAVED];
  volatile void * thrown;
  x_exception previous;
  x_boolean fired;
  x_xcb callbacks;
  x_Xcb Callbacks;
} x_Exception;

int x_context_save(x_exception exception);
void x_context_restore(x_exception exception, int r);

/*
** These are defined in src/exception.c Note that these functions should NEVER be
** called directly by an application programmer, but always through the macro's defined
** below. We make x_exception_pop return a volatile void * so that the compiler catches
** misdefined pointers; it should give a warning.
*/

void x_exception_push(x_exception exception);
volatile void * x_exception_pop(void);

/*
** Function to register an exception callback that is run in the reverse
** order that they were pushed, only when an exception has been thrown.
** The callbacks will be executed just before the x_Catch clause will be
** executed.
*/

x_status x_exception_callback(x_exception_cb cb, void * arg);

/*
** Note that the following is really stretching the compiler and the pre-processor...
** This pre-processor trickery is inspired by the work done by Adam M. Costello and
** Cosmin Truta on the header file "cexcept.h".
*/

#define x_Try                                  \
  {                                            \
    x_Exception Exception;                     \
    x_exception_push(&Exception);              \
    if (x_context_save(&Exception) == 0) {     \
      if (1 == 1)

#define x_Catch(e)                             \
      Exception.fired = false;                 \
    }                                          \
    else {                                     \
      Exception.fired = true;                  \
    }                                          \
    e = x_exception_pop();                     \
  }                                            \
  if (e)

x_status x_Throw(void * thrown);

/*
** Mutex related stuff.
*/

typedef struct x_Mutex {
  x_Event Event;           /* Always have this Event structure as the first element for x_event_join to work. */
  volatile x_thread owner;
} x_Mutex;

x_status x_mutex_create(x_mutex mutex);
x_status x_mutex_delete(x_mutex mutex);
x_status x_mutex_lock(x_mutex mutex, x_sleep timeout);
x_status x_mutex_unlock(x_mutex mutex);
x_status x_mutex_release(x_mutex mutex);
x_thread x_mutex_owner(x_mutex mutex);

void x_mutex_check(x_mutex mutex);

/*
** Atomic swap based mutexes. Note that these mutexes DO NOT implement
** anything to prevent priority inversion. Use them only when you're absolutely
** sure that priority inversion is not a problem... There is also no timeout
** option available. Pattern of use
**
** static x_thread mutex; // The variable holding the thread that has the mutex or the 'looking value' 1.
**
** x_atomic_enter(&mutex);
** ... critical region ...
** x_atomic_exit(&mutex);
*/

inline static void x_atomic_enter(x_thread * mutex) {
  while(! xi_enter_atomic(mutex, thread_current)) {
    x_thread_sleep(1);
  }
}

inline static void x_atomic_exit(x_thread * mutex) {
  while(! xi_exit_atomic(mutex)) {
    x_thread_sleep(1);
  }
}

/*
** Memory and heap related stuff.
*/

/*
** The page size we will use for this allocator.
*/

#define OSWALD_PAGE_SIZE  (4096)

x_status x_mem_tag_set(void * mem, x_word tag);
x_word x_mem_tag_get(void * mem);
x_size x_mem_size(void * mem);
void * x_mem_alloc(x_size bytes);
void * x_mem_calloc(x_size bytes);
void * x_mem_realloc(void * old, x_size newsize);
void x_mem_free(void * block);
void x_mem_discard(void * block);
x_status x_mem_collect(x_size * bytes, x_size * num);
void x_mem_init(void);
x_boolean x_mem_is_block(void * mem);
x_status x_mem_walk(x_sleep timeout, x_boolean (*callback)(void * mem, void * arg), void * arg);
x_status x_mem_walkall(x_sleep timeout, x_boolean (*callback)(void * mem, void * arg), void * arg);
x_status x_mem_scan(x_sleep timeout, x_word tag, x_boolean (*callback)(void * mem, void * arg), void * arg);
x_status x_mem_lock(x_sleep timeout);
x_status x_mem_unlock(void);
x_size x_mem_total(void);
x_size x_mem_avail(void);


/*
** Don't use the memory monitor directly: always call x_memory_lock/unlock.
** We make the monitor visible here for debugging purposes only.
*/
extern x_monitor memory_monitor;

/*
** Monitor related stuff...
*/

typedef struct x_Monitor {
  x_Event Event;                  /* Always have this Event structure as the first element for x_event_join to work.                 */
  volatile x_thread owner;        /* The current owner of the monitor or NULL, when it is not owned by a thread.                     */
  volatile x_ushort count;        /* The number of times the current owner thread has locked (entered) this monitor.                 */
  volatile x_ushort n_waiting;    /* Number of threads waiting on this monitor; keep it after the previous field for packing.        */
  volatile x_thread l_waiting;    /* The start of the list of threads waiting on this monitor. Proceeds through x_thread->l_waiting. */
} x_Monitor;

x_status x_monitor_create(x_monitor monitor);
x_status x_monitor_delete(x_monitor monitor);
x_status x_monitor_enter(x_monitor monitor, x_sleep timeout);
x_status x_monitor_eternal(x_monitor monitor);
x_status x_monitor_exit(x_monitor monitor);
x_status x_monitor_release(x_monitor monitor);
x_status x_monitor_wait(x_monitor monitor, x_sleep timeout);
x_status x_monitor_notify(x_monitor monitor);
x_status x_monitor_notify_all(x_monitor monitor);
x_status x_thread_stop_waiting(x_thread thread);
x_status x_monitor_kick_all(x_monitor monitor);

void xi_remove_waiting_thread(x_monitor monitor, x_thread thread);
void x_monitor_check(x_monitor monitor);

/*
** Bit map related stuff...
*/

/*
** An unsynchronised map, i.e. a bitmap that isn't an event. We use such maps in Oswald for unique
** thread and event ids. The synchronised map x_Map defined below, is a map that is also an event,
** such that multiple threads can compete for a bit in the map.
*/

typedef struct x_Umap {
  x_size entries;                 /* The maximum number of entries, indexes range from 0 to (entries - 1). */
  x_word mask;                    /* The mask used for clearing indexes. The lower bits are always 1, the higher bits 0. */
  volatile x_size cache_0;        /* The first index in table, where a 0 bit is found in a word. */
  x_word * table;
} x_Umap;

x_size x_umap_create(x_umap umap, x_size entries, x_word * table);
x_boolean x_umap_set(x_umap umap, x_size entry);
x_status x_umap_any(x_umap umap, x_size * entry);
x_status x_umap_reset(x_umap umap, x_size entry);
x_boolean x_umap_probe(x_umap umap, x_size entry);

/*
** The event version of a map, so that multiple threads can compete for bits.
*/

typedef struct x_Map {
  x_Event Event;                  /* Keep this first! */
  x_Umap Umap;                    /* The real bitmap, as defined above. */
} x_Map;

x_size x_map_size(x_size entries);
x_status x_map_create(x_map map, x_size entries, x_word * table);
x_status x_map_set(x_map map, x_size entry, x_window window);
x_status x_map_any(x_map map, x_size * entry, x_window window);
x_status x_map_reset(x_map map, x_size entry);
x_status x_map_probe(x_map map, x_size entry, x_boolean * bool);
x_status x_map_delete(x_map map);

inline static void x_id_set(x_flags * flags, x_word max_ids, x_word id) {
  *flags |= ((max_ids - 1) & id);
}

inline static x_word x_id_get(x_flags flags, x_word max_ids) {
  return (flags & max_ids);
}

/*
** Queue related stuff...
*/

typedef struct x_Queue {
  x_Event Event;                  /* Always have this Event structure as the first element for x_event_join to work.               */
  x_word * messages;              /* The memory area in which the messages will reside.                                            */
  volatile x_word * write;        /* The write pointer position, points to the next slot to write in.                              */
  volatile x_word * read;         /* Points to the next message to read.                                                           */
  volatile x_ushort available;    /* The number of available messages in the queue, max = 0xffff                                   */
  x_ushort capacity;              /* The total number of messages that can be in the queue; for packing, keep with previous field. */
  x_word * limit;                 /* A pointer that points 1 message beyond the message area. To check for wrap arounds.           */
} x_Queue;

x_status x_queue_create(x_queue queue, void * messages, x_size capacity);
x_status x_queue_delete(x_queue queue);
x_status x_queue_receive(x_queue queue, void ** data, x_sleep timeout);
x_status x_queue_send(x_queue queue, void * data, x_sleep timeout);
x_status x_queue_flush(x_queue queue, void (*fcb)(void * data));
x_status x_queue_suspend(x_queue queue);
x_status x_queue_resume(x_queue queue);

/*
** Semamphore related stuff...
*/

typedef struct x_Sem {
  x_Event Event;                  /* Always have this Event structure as the first element for x_event_join to work. */
  volatile x_size current;        /* The current count of the semaphore. */
} x_Sem;

x_status x_sem_create(x_sem sem, x_size initial);
x_status x_sem_delete(x_sem sem);
x_status x_sem_get(x_sem sem, x_sleep timeout);
x_status x_sem_put(x_sem sem);

/*
** Signals related stuff. Note that these are not the same as UNIX signals.
** Oswald signals are event flags on which threads can wait for a combination 
** of flags to appear.
*/

typedef struct x_Signals {
  x_Event Event;                  /* Always have this Event structure as the first element for x_event_join to work. */
  volatile x_flags flags;         /* the flags. */
} x_Signals;

/*
** Note that the order and value of the options should NOT be changed, we rely on the bits
** in the value to quickly decide for 'clear' or 'not clear' and 'or' or 'and' option.
** An option 'clears' when Bit 1 is set, an option 'ands' when Bit 0 is set.
*/

typedef enum x_option {     /*  Bit 1 | Bit 0  */
  xo_or             = 0,    /*    0   |   0    */   
  xo_and            = 1,    /*    0   |   1    */
  xo_or_clear       = 2,    /*    1   |   0    */
  xo_and_clear      = 3,    /*    1   |   1    */
  xo_unknown        = 4,    /*    0   |   0    */
} x_option;

x_status x_signals_create(x_signals signals);
x_status x_signals_delete(x_signals signals);
x_status x_signals_get(x_signals signals, x_flags concerned, x_option option, x_flags * actual, x_sleep timeout);
x_status x_signals_set(x_signals signals, x_flags concerned, x_option option);

/*
** Timer related stuff...
*/

typedef void (*x_timerfire)(x_timer timer);

typedef struct x_Timer {
  volatile x_timer previous;
  volatile x_timer next;
  volatile x_timer list;   // the link to other timers on the active or inactive list
  x_size id;               // the id for this timer, from 0 to 0xffffffff
  x_timerfire timerfire;   // the timeout function for this timer.
  void * argument;         // the argument that can associate user data with the timer, passed as argument to timeout function
  x_sleep initial;         // the initial number of ticks for this timer, when 0, we reload the timer with 'repeat'.
  x_sleep repeat;
  x_sleep delta;           // The timeout w.r.t. the previous timer in the activer_timers list.
  x_size fired;            // The number of times that the timer has fired, reset with each x_timer_change
  x_flags flags;
} x_Timer;

/*
** The different flags for x_timer_create
*/

#define TIMER_DONT_START            0x00000000   // Don't start the timer, wait for a x_timer_activate call
#define TIMER_AUTO_START            0x00000001   // Start the timer immidiately, when not set, deferred start
#define TIMER_LINKED                0x00000002   // Timer is linked in, when not set, the timer is deleted.
#define TIMER_ARMED                 0x00000004   // The timer is armed
#define TIMER_FIRED                 0x00000008   // The timer has fired, when set and TIMER_ARMED is not set, it should not fire
#define TIMER_USER_FLAGS            0xffff0000   // A user can define flags, but only in this area

x_status x_timer_create(x_timer timer, x_timerfire timerfire, x_sleep initial, x_sleep repeat, x_flags flags);
x_status x_timer_delete(x_timer timer);
x_status x_timer_activate(x_timer timer);
x_status x_timer_deactivate(x_timer timer);
x_status x_timer_change(x_timer timer, x_sleep initial, x_sleep repeat);

static void inline x_timer_set_flags(x_timer timer, x_flags flags) {
  setFlag(timer->flags, flags & TIMER_USER_FLAGS);
}

void xi_timers_tick(void);
void x_init_timers(void);

/*
** Debugging related stuff...
*/

void x_dump_threads(void);
void x_dump_mutex(char *, x_mutex);
void x_dump_monitor(char *, x_monitor);
void x_dump_monitor_if_locked(char *, x_monitor);

#ifdef DEBUG

void _loempa(const char *function, const int line, const int level, const char *fmt, ...);
int register_customer(x_int specifier, char * (*cf)(char * buf, int * remain, void * arg));

static const int loempa_trigger = 1;

#define loempa(level, format, a...) {                          \
  if (level >= loempa_trigger) {                               \
    _loempa(__FUNCTION__, __LINE__, level, format, ##a);       \
  }                                                            \
}

x_int find_thread_in_pcbs(x_thread thread);

const char * x_type2char(x_type type);
const char * x_event2char(x_event event);
const char * x_option2char(x_option option);
void x_flags_as_bits(unsigned char buffer[], x_flags flags);

void x_pcbs_dump(void);
void x_pending_dump(void);
void x_blocks_dump(x_block block);

static const x_boolean debug = true;

#else /* No DEBUG */

#define loempa(level, format, a...)
static const x_boolean debug = false;

#endif /* DEBUG */

/*
** Runtime checks stuff...
*/

#ifdef RUNTIME_CHECKS

static const x_boolean runtime_checks = true;

/*
** Return true when the event type doesn't match.
*/

inline static x_boolean x_event_type_bad(void * event, x_type type) {
  return ((((x_event)event)->flags_type & EVENT_TYPE_MASK) != (x_ushort)type);
}

void _assert(const char * message);

#define x_assert_string(x) x_assert_val(x)
#define x_assert_val(x)    #x
#define x_assert(test) ((test) ? (void) 0 : _assert(__FUNCTION__ ":" x_assert_string(__LINE__) " " #test))

/*
** Return true when we are in a context sensitive environment, i.e. interrupt handler
** or timer handler. When runtime checks is not defined, we return false so that the
** compiler optimizes away the checks.
*/

inline static x_boolean x_in_context_critical(x_sleep timeout) {
  return timeout && (irq_depth || thread_current == thread_timer);
}

#else /* No RUNTIME_CHECKS */

static const x_boolean runtime_checks = false;

inline static x_boolean x_event_type_bad(void * event, x_type type) {
  return false;
}

#define x_assert(test)

inline static x_boolean x_in_context_critical(x_sleep timeout) {
  return false;
}

#endif /* RUNTIME_CHECKS */

#endif /* _OSWALD_H */
