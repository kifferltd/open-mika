#ifndef _OSWALD_H
#define _OSWALD_H

/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2010 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#include "FreeRTOS.h"
#include "xmisc.h"

#define NUM_PRIORITIES     64

#ifdef __BIT_TYPES_DEFINED__
typedef int32_t      x_boolean;
typedef u_int8_t     x_ubyte;
typedef u_int32_t    x_time;
typedef u_int32_t    x_size;
typedef u_int32_t    x_word;
typedef u_int32_t    x_uword;
typedef int32_t      x_int;
typedef int64_t      x_long;
#else 
typedef int           x_boolean;
typedef unsigned char x_ubyte;
typedef unsigned int  x_time;
typedef unsigned int  x_size;
typedef unsigned int  x_word;
typedef unsigned int  x_uword;
typedef signed int    x_int;
typedef signed long long x_long;
#endif
typedef x_word                  x_flags;
typedef struct x_Queue *        x_queue;
typedef struct x_Monitor *      x_monitor;
typedef struct x_Thread *       x_thread;
typedef struct x_Sem *          x_sem;
typedef struct x_Mutex *        x_mutex;

typedef TickType_t   x_sleep;

typedef const char * (*x_report)(x_thread thread);  /* report generator */

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
// O4p specials
        xt_newborn      = 16,
	xt_dummy	    = 99,
} x_state;

#define LAST_XT_STATE xt_newborn

/* XXX NAH Move to kernel/common */
const char * x_state2char(x_thread thread);

/*
** When getting a sem or locking a mutex, these are two extreme values for the
** timeout parameter: x_no_wait means always return immediately (perhaps with
** a return code of xs_no_instance), x_eternal means hang on in there for
** however long it takes.
*/
#define x_no_wait 0
#define x_eternal portMAX_DELAY

#include "o4f.h"
#include "vsprintf.h"

/*
** Possible values for the `flags' argument of x_thread_create
*/
#define TF_START              0x00000000 /* start immediately */
#define TF_SUSPENDED          0x00000001 /* initially thread is suspended */
/*
 * Other possible values of x_thread flags word
 */
#define TF_TIMEOUT            0x00010000 /* most recent operation timed out */
#define TF_COMPETING          0x00100000 /* waiting for a monitor to become free */
#define TF_WAIT_INT           0x00200000 /* Thread was waiting on monitor, but interrupted by another thread. */
#define TF_RECEIVING          0x00400000 /* Thread blocked receiving on a queue */
#define TF_SENDING            0x00800000 /* Thread blocked sending on a queue */

/*
 * Flags used when recycling kernel threads within the Java VM */
#define TF_LIFETIME_LSB       0x01000000 /* Least-significant bit of lifetime counter */
#define TF_LIFETIME_MASK      0xff000000 /* Mask for lifetime counter */
#define TF_LIFETIME_SHIFT     24         /* Shift for lifetime counter */
#define setLifetime(x, n)     ((x->flags) |= ((n) << TF_LIFETIME_SHIFT))
#define getLifetime(x)        (((x->flags) & TF_LIFETIME_MASK) >> TF_LIFETIME_SHIFT)
#define useLifetime(x)        ((x->flags) -= TF_LIFETIME_LSB)

#define isSet(x, flag)            ((x) & (flag))
#define isNotSet(x, flag)         (!isSet((x), (flag)))
#define setFlag(x, flag)          ((x) |= (flag))
#define unsetFlag(x, flag)        ((x) &= ~(flag))
#define maskFlags(m, f)           ((m) & (f))

void x_scheduler_disable(void);
void x_scheduler_enable(void);

/*
** x_Monitor and the x_monitor_... primitives.
*/

x_status x_monitor_create(x_monitor);
x_status x_monitor_delete(x_monitor);
x_status x_monitor_enter(x_monitor, x_sleep);
#define  x_monitor_eternal(m) x_monitor_enter((m), x_eternal)
x_status x_monitor_exit(x_monitor);
x_status x_monitor_wait(x_monitor, x_sleep);
x_status x_monitor_notify(x_monitor);
x_status x_monitor_notify_all(x_monitor);
x_status x_monitor_stop_waiting(x_monitor monitor, x_thread thread);

/*
** x_queue_... primitives.
*/
x_status x_queue_create(x_queue queue, void *queue_start, x_size queue_size);
x_status x_queue_delete(x_queue queue);
x_status x_queue_receive(x_queue queue, void **dest_msg, x_sleep waittime);
x_status x_queue_send(x_queue queue, void *src_msg, x_sleep waittime);
x_status x_queue_flush(x_queue queue, void(*do_this)(void *data));

/*
** x_mutex_... primitives.
*/
x_status x_mutex_create(x_mutex mutex);
x_status x_mutex_delete(x_mutex mutex);
x_status x_mutex_lock(x_mutex mutex, x_sleep timeout);
x_status x_mutex_unlock(x_mutex mutex);

/*
** x_thread_... primitives.
*/

typedef void(*x_entry)(void* arg);

x_status x_thread_create(x_thread thread, x_entry entry_function, void* entry_input, void *stack_start, x_size stack_size, x_size priority, x_word flags);
x_status x_thread_delete(x_thread thread);
x_size x_thread_priority_get(x_thread thread);
x_size x_thread_priority_set(x_thread thread, x_size new_priority);
x_status x_thread_resume(x_thread thread);
x_status x_thread_sleep(x_sleep timer_ticks);
x_status x_thread_suspend(x_thread thread);
x_status x_thread_join(x_thread thread, void **result, x_sleep timeout);
x_status x_thread_stop_waiting(x_thread);
x_status x_thread_wakeup(x_thread);
x_status x_thread_attach_current(x_thread);
x_status x_thread_detach(x_thread);
x_status x_thread_signal(x_thread, x_int signum);

//#define x_signal_1 SIGUSR1

inline static x_state x_thread_state(x_thread thread) {
  return thread->state;
}


#define x_thread_current() ((x_thread) pvTaskGetThreadLocalStoragePointer(NULL, O4F_LOCAL_STORAGE_OFFSET_X_THREAD))

void x_thread_yield(void);

/*
** The Oswald time API (part of).
*/
x_sleep x_time_get(void);

x_size x_millis2ticks(x_size millis);
x_long x_ticks2millis(x_long millis);
x_size x_seconds2ticks(x_size seconds);
x_long x_ticks2usecs(x_size ticks);
x_size x_usecs2ticks(x_size usecs);


/*
** The Oswald memory API.
*/

void x_mem_init(void);
#ifdef DEBUG
void *_x_mem_alloc(x_size bytes, const char *file, int line);
void *_x_mem_calloc(x_size bytes, const char *file, int line);
void *_x_mem_realloc(void *old, x_size size, const char *file, int line);
#define x_mem_alloc(bytes) _x_mem_alloc((bytes), __FILE__,__LINE__)
#define x_mem_calloc(bytes) _x_mem_calloc((bytes), __FILE__,__LINE__)
#define x_mem_realloc(old,bytes) _x_mem_realloc((old), (bytes), __FILE__,__LINE__)
#else
void *_x_mem_alloc(x_size bytes);
void *_x_mem_calloc(x_size bytes);
void *_x_mem_realloc(void *old, x_size size);
#define x_mem_alloc(bytes) _x_mem_alloc((bytes))
#define x_mem_calloc(bytes) _x_mem_calloc((bytes))
#define x_mem_realloc(old,bytes) _x_mem_realloc((old), (bytes))
#endif
void x_mem_free(void*);
x_status x_mem_lock(x_sleep timeout);
x_status x_mem_unlock(void);

// only exposed so that dump.c can say who owns it - do not use directly
// TODO create a function to get the owner as an x_thread
extern SemaphoreHandle_t memoryMutex;

/*
** Unconditionally walk all blocks in memory, calling a callback on each one.
*/
x_status x_mem_walk(x_sleep timeout, x_boolean (*callback)(void * mem, void * arg), void *arg);

/*
** Walk memory and for each block which has a given tag, invoke the callback
** and if it returns flase (0) abort the walk.
*/
x_status x_mem_scan(x_sleep timeout, x_word tag, x_boolean (*callback)(void * mem, void * arg), void * arg);

x_size x_mem_total(void);
#define x_mem_gettotal x_mem_total

x_size x_mem_avail(void);

x_status x_mem_tag_set(void * mem, x_word tag);
x_word x_mem_tag_get(void * mem);
x_size x_mem_size(void * mem);
void x_mem_discard(void * block);
x_status x_mem_collect(x_size * bytes, x_size * num);
x_boolean x_mem_is_block(void * mem);

/*
** The OSwald debugging API.
*/
void x_debug_write(const void *buf, size_t count);

inline static void x_debug_putc(const char c) {
  x_debug_write(&c, 1);
}

inline static void x_debug_puts(const char *s) {
  x_debug_write(s, strlen(s));
}

x_long x_time_now_millis(void);

/*
** Don't use the memory monitor directly: always call x_memory_lock/unlock.
** We make the monitor visible here for debugging purposes only.
*/
extern x_monitor memory_monitor;

/*
** This is the entry point which the OS will call when it has completed
** its own internal initialisation.
*/
void x_os_main(int argc, char** argv);

/*
** Call this from main() to start up oswald.
** Parameters: max heap in bytes, tick interval in milliseconds.
*/
x_status x_oswald_init(x_size max_heap, x_size millis);

#define x_preemption_disable x_scheduler_disable()
#define x_preemption_enable  x_scheduler_enable()

/*
** Debugging related stuff...
*/

void x_dump_threads(void);
void x_dump_mutex(char *, x_mutex);
void x_dump_monitor(char *, x_monitor);
void x_dump_monitor_if_locked(char *, x_monitor);

#ifdef DEBUG

#ifndef DEBUG_LEVEL
#define DEBUG_LEVEL 7
#endif

void _loempa(const char *file, const char *function, const int line, const int level, const char *fmt, ...);

static const int loempa_trigger = DEBUG_LEVEL;

#define loempa(level, format, ...) {                          \
  if (level >= loempa_trigger) {                               \
    _loempa(__FILE_NAME__, __func__, __LINE__, level, format, __VA_ARGS__);       \
  }                                                            \
}

static const x_boolean debug = true;

#else /* No DEBUG */

#define loempa(level, format, ...)
static const x_boolean debug = false;

#endif /* DEBUG */

/*
** Runtime checks stuff...
*/

#ifdef RUNTIME_CHECKS

static const x_boolean runtime_checks = TRUE;

/*
** Return true when the event type doesn't match.

inline static x_boolean x_event_type_bad(void * event, x_type type) {
  return ((((x_event)event)->flags_type & EVENT_TYPE_MASK) != (x_ushort)type);
}
*/

void _assert(const char * message);

#define x_assert_string(x) x_assert_val(x)
#define x_assert_val(x)    #x
static inline void _x_fail(const char *function, const char *file, int line, const char *message) {
  printf("Failed assertion in %s at %s:%d : %s\n", function, file, line, message);
}

#define x_assert(test) ((test) ? (void) 0 : _x_fail(__FUNCTION__, __FILE__, __LINE__, x_assert_string(test)))

/*
** Return true when we are in a context sensitive environment, i.e. interrupt handler
** or timer handler. When runtime checks is not defined, we return false so that the
** compiler optimizes away the checks.
*/

inline static x_boolean x_in_context_critical(x_sleep timeout) {
  return FALSE;
}

#else /* No RUNTIME_CHECKS */

static const x_boolean runtime_checks = FALSE;

/*
inline static x_boolean x_event_type_bad(void * event, x_type type) {
  return FALSE;
}
*/

#define x_assert(test)

inline static x_boolean x_in_context_critical(x_sleep timeout) {
  return FALSE;
}

#endif /* RUNTIME_CHECKS */

#endif /* _OSWALD_H */

