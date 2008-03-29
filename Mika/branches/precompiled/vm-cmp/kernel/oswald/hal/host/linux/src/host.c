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
*                                                                         *
* Modifications copyright (c) 2003 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: host.c,v 1.6 2006/09/11 13:21:38 cvsroot Exp $
*/

#include <stdio.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <signal.h>
#include <unistd.h>
#include <time.h>
#include <sys/mman.h>

#include <oswald.h>

#define HOST_TIMER_SIGNAL  SIGALRM 
#define HOST_TIMER_TIMER   ITIMER_REAL 

/* 
** The default number of microseconds per timer interrupt. For linux, don't make
** it too small since the kernel would not be able to service it. Note that the
** following value was experimentally found to be a correct number for most systems. Your mileage
** may vary...
*/

static x_size usecs_per_tick = 100000;

static struct sigaction tick;
static struct sigaction oldtick;
static struct itimerval ticker;
static struct itimerval oldtimer;
static sigset_t unblock_timersig;
static unsigned int pagesize;

sigset_t unblocked_signals;

static x_ubyte * host_memory = NULL;

// x_ubyte * host_memory_end;

x_size min_heap_bytes = 0;
x_size max_heap_bytes = 0;

volatile x_word preemptive_switch = 0;

void start_host_timer(void);

static void host_timer_tick(int sig) {

  /*
  ** Avoid timer tick when we are booting or too excessive irq depth.
  */
  
  if (irq_depth > 10) {
    return;
  }
  
  irq_depth++;
  (*irq_handlers[IRQ_TICK]->top)(irq_handlers[IRQ_TICK]);
  irq_depth--;

  /*
  ** An interrupt handler can change thread_next, so we call xi_thread_reschedule 
  ** that will do the correct thing. We reset preemptive switch in case that no thread 
  ** switch took place so that the next non preempted switch will not do go through the
  ** if clause in x_host_post.
  */

  if (critical_status == 0) {
    preemptive_switch = 1;
    xi_thread_reschedule();
    preemptive_switch = 0;
  }

}
 
void x_host_post(void) {

  if (preemptive_switch) {

    /*
    ** Unblock the timer signal! At entry of the above handler, the kernel has disabled the fired signal
    ** and will only unblock it when we leave the handler. Since we leave through a thread
    ** switch, we would block the next delivery of the timer signal. The thread switch routine calls this
    ** post handler to unblock the signal again.
    */

    sigprocmask(SIG_UNBLOCK, &unblock_timersig, NULL); 
    //sigprocmask(SIG_SETMASK, &unblocked_signals, NULL);
    preemptive_switch = 0;

  }

  if (runtime_checks && thread_current->trigger && *thread_current->trigger != 0xaaaaaaaa) {
    loempa(9, "Trigger value is 0x%0x for thread %d.\n", *thread_current->trigger, thread_current->id);
    abort();
  }

//  x_thread_switched(thread_current);

}

void start_host_timer () {

  x_int result;

  ticker.it_interval.tv_sec = usecs_per_tick / 1000000;
  ticker.it_interval.tv_usec = usecs_per_tick % 1000000;
  ticker.it_value.tv_sec = usecs_per_tick / 1000000;
  ticker.it_value.tv_usec = usecs_per_tick % 1000000;
  result = setitimer(HOST_TIMER_TIMER, &ticker, &oldtimer);
  if (result == -1) {
    loempa(9, "%s %d: setitimer failed.\n", __FILE__, __LINE__);
    abort();
  }

}

inline static unsigned int round_up(unsigned int value, unsigned int rounding) {
  return (value + (rounding - 1)) & ~(rounding - 1);
}

inline static unsigned int round_down(unsigned int value, unsigned int rounding) {  
  return (value) & (unsigned int)(0 - rounding);
}

static x_irq irq_handler_space[IRQ_MAX];

void x_irqs_setup(void) {

  x_size i;
  
  irq_handlers = irq_handler_space;

  for (i = 0; i < IRQ_MAX; i++) {
    irq_handlers[i] = irq_default;
  }
  
  irq_handlers[IRQ_TICK] = irq_tick;
}

void x_host_setup(void) {

  int result;

  /*
  ** Set up our timer tick interval timer and signal handler.
  */

  tick.sa_handler = host_timer_tick;
  sigemptyset(&tick.sa_mask);
  tick.sa_flags = SA_RESTART;

  result = sigaction(HOST_TIMER_SIGNAL, &tick, &oldtick);
  if (result == -1) {
    loempa(9, "%s %d: sigaction failed.\n", __FILE__, __LINE__);
    abort();
  }

  start_host_timer();

  /*
  ** Get currently unblocked signals to unblock them again in timer handler...
  */
    
  sigemptyset(&unblock_timersig);
  sigaddset(&unblock_timersig, HOST_TIMER_SIGNAL);

  sigprocmask(SIG_UNBLOCK, NULL, &unblocked_signals);
}

#define MIN_TRY (1024 * 1024 * 4)
#define TRIM_FACTOR (6)

#ifdef SHARED_HEAP
  void * __libc_malloc(size_t size);
  void   __libc_free(void * ptr);
  void * __libc_realloc(void * ptr, size_t size);
  #define  ext_malloc  __libc_malloc
  #define  ext_free    __libc_free
  #define  ext_realloc __libc_realloc
#else
  #define  ext_malloc  malloc
  #define  ext_free    free
  #define  ext_realloc realloc
#endif

#define STATIC_SIZE 200000

x_status x_oswald_init(x_size max_heap, x_size millis) {

//  host_memory = malloc(STATIC_SIZE);
//  host_memory_end = host_memory + STATIC_SIZE - 1;
  max_heap_bytes = max_heap;

  pagesize = getpagesize();

  x_kernel_setup(host_memory);

  loempa(9, "Should not get here!\n");

  return xs_bad_argument;
  
}

/*
** Functions to convert seconds, microseconds to ticks and vice versa.
*/

static const x_size ticks_compensation = 2;

/*
** Return a compensated number of ticks for a number of seconds.
*/

x_size x_seconds2ticks(x_size seconds) {
  return (x_size) ((seconds * 1000) / (x_size) (usecs_per_tick / 1000) - ticks_compensation);
}

x_size x_millis2ticks(x_size millis) {

  x_size ticks = (millis / (x_size) (usecs_per_tick / 1000));
  
  return ticks ? ticks : 1;
  
}

x_size x_ticks2usecs(x_size ticks) {
  return (usecs_per_tick * ticks);
}

x_size x_usecs2ticks(x_size usecs) {
  return (usecs <= usecs_per_tick) ? 1 : ((x_size)((usecs-1) / usecs_per_tick)+1);
}

int my_error;

int * __errno_location(void) {
  if (thread_current){
//    loempa(9, "current OSwald thread is %p, errno at %p (was %d)\n", thread_current, &thread_current->tsd_errno, thread_current->tsd_errno);
    return (int *) & thread_current->tsd_errno;
  }
  else {
    return (int *) & my_error;
  }
}

void showhandler(void) {
  struct sigaction sf;
  sigaction(SIGSEGV, NULL, &sf);
  loempa(9, "handler = %p\n", sf.sa_sigaction);
}

#define MAX_ZONES 64
#ifdef X86
static const x_size max_zone_size = 8192000; // bogus, for emulation on "real" Linux
static const x_size min_zone_size = 1024000; // idem ditto
#else
static const x_size max_zone_size = 1024 * 1024; // seems to be max for uClinux
static const x_size min_zone_size = 256 * 1024;
#endif
static x_size zone_size;
static x_size total_allocated;

static struct {
  char * base;
  x_size size;
} zone[MAX_ZONES];
static int zones_allocated;
static int fake_break_zone;
static char* fake_base;
static char* fake_break;

static void init_zones(void) {
  int i;
  int j;

  zone_size = max_zone_size;
  for (zones_allocated = 0; zones_allocated < MAX_ZONES;) {
    if (total_allocated + (min_zone_size / 2) > max_heap_bytes) {
      // printf("have allocated %d bytes in %d zones\n", total_allocated, zones_allocated);
      break;
    }
    while (total_allocated  + zone_size > max_heap_bytes + (min_zone_size / 2) && zone_size > min_zone_size) {
      zone_size /= 2;
      // printf("have allocated %d bytes in %d zones, reducing zone size to %d so as not to overrun max\n", total_allocated, zones_allocated, zone_size);
    }
    zone[zones_allocated].base = malloc(zone_size);
    if (zone[zones_allocated].base == NULL) {
      if (zone_size > min_zone_size) {
        zone_size /= 2;
        // printf("reducing zone_size to %d because alloc failed\n", zone_size);
        zone[zones_allocated].base = malloc(zone_size);
        if (zone[zones_allocated].base == NULL) {
          // printf("unable to allocate memory zone[%d]\n", zones_allocated);
          break;
        }
      }
      else {
        // printf("unable to allocate memory zone[%d]\n", zones_allocated);
        break;
      }
    }
    else {
      zone[zones_allocated++].size = zone_size;
      total_allocated += zone_size;
    }
  }

  // printf("Successfully allocated %d zones totalling %d bytes (%d requested)\n", zones_allocated, total_allocated, max_heap_bytes);
  max_heap_bytes = total_allocated;

  for (i = 0; i < zones_allocated; ++i) {
    for (j = 1; j < zones_allocated - i; ++j) {
      if (zone[j - 1].base - zone[j].base > 0) {
        char * tempbase;
        x_size tempsize;

        // printf("Swapping zone[%d] with zone [%d]\n", j - 1, j);
        tempbase = zone[j - 1].base;
        zone[j - 1].base = zone[j].base;
        zone[j].base = tempbase;
        tempsize = zone[j - 1].size;
        zone[j - 1].size = zone[j].size;
        zone[j].size = tempsize;
      }
    }
  }

  // printf("\nZones allocated (before merging): %d\n", zones_allocated);
  for (i = 0; i < zones_allocated; ++i) {
    // printf("  Zone[%d] : from %p to %p, size = %d bytes\n", i, zone[i].base, zone[i].base + zone[i].size - 1, zone[i].size);
  }

  for (i = zones_allocated - 1; i > 0; --i) {
    if (zone[i].base == zone[i-1].base + zone[i-1].size) {
      // printf("Merging zone[%d] with zone[%d]\n", i, i - 1);
      zone[i - 1].size += zone[i].size;
      for (j = i; j + 1 < zones_allocated; ++j) {
        zone[j].base = zone[j + 1].base;
        zone[j].size = zone[j + 1].size;
      }
      --zones_allocated;
    }
  }

  // printf("\nZones allocated (after merging): %d\n", zones_allocated);
  for (i = 0; i < zones_allocated; ++i) {
    // printf("  Zone[%d] : from %p to %p, size = %d bytes\n", i, zone[i].base, zone[i].base + zone[i].size - 1, zone[i].size);
  }

  fake_break_zone = 0;
  fake_base = zone[0].base;
  fake_break = fake_base;
}

x_ubyte * x_host_sbrk(x_int bytes) {
  char *old_fake_break = fake_break;

  if (!fake_base) {
    init_zones();
  }

  if (fake_break + bytes < fake_base) {
    x_int less = fake_base - fake_break - bytes;

    // printf("Request for %d bytes takes us under start of zone[%d] (%p)\n", bytes, fake_break_zone, fake_base);
    while (fake_break_zone) {
      --fake_break_zone;
      fake_base = zone[fake_break_zone].base;
      // printf("Need to lose %d bytes : moving to zone[%d] (%p)\n", less, fake_break_zone, fake_base);
      fake_break = fake_base + zone[fake_break_zone].size - less;
      less -= zone[fake_break_zone + 1].size;
      if (less <= 0) {
        // printf("OK, new fake break is %p\n", fake_break);

        return old_fake_break;

      }
    }
      
    if (less > 0) {
      loempa(1, "request for %d bytes heap when %d allocated, returning NULL\n", bytes, fake_break - fake_base);

      return NULL;

    }
  }

  if (fake_break + bytes >= fake_base + zone[fake_break_zone].size) {
    // printf("Request for %d bytes takes us over the end of zone[%d] (%p + 0x%08x = %p > %p)\n", bytes, fake_break_zone, fake_break, bytes, fake_break + bytes, fake_base + zone[fake_break_zone].size - 1);
    if (fake_break_zone + 1 >= zones_allocated) {
      // printf("no more zones, game over.\n");

      return NULL;

    }

    if ((x_size)bytes >= zone[fake_break_zone + 1].size - 32 /* ?? */) {
      // printf("expanding into next zone won't help, sorry.\n");

      return NULL;

    }

    ++fake_break_zone;
    fake_base = zone[fake_break_zone].base;
    // printf("Moving to zone[%d] (%p)\n", fake_break_zone, fake_base);
    fake_break = fake_base + bytes;

    return fake_base;

  }

  // loempa(1, "request for %d bytes heap when %d allocated (max. %d), returning %p\n", bytes, fake_break - fake_base + total_allocated, max_heap_bytes, fake_break + bytes);
  fake_break += bytes;

  return old_fake_break;
}

void x_host_break(x_ubyte * memory) {

/*
  if (memory != host_memory) {
    printf( "Reallocing host static memory down to %d bytes (%p - %p).\n", (int)(memory - host_memory), memory, host_memory);
    ext_realloc(host_memory, memory - host_memory);
  }
  else {
    printf( "Freeing host static memory at %p.\n", host_memory);
    ext_free(host_memory);
  }
*/
}

