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
** $Id: debug.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <oswald.h>
#include <string.h>
#include <stdio.h>

/*
** An array we use to quickly store the thread pointers.
*/

static x_thread all_threads[2048];

void x_pending_dump(void) {

  x_thread thread;
  x_sleep running;

  x_preemption_disable;

  loempa(9, "Sleeping threads...\n");
  running = 0;
  for (thread = sleepers; thread; thread = thread->snext) {
    running += thread->sticks;
    loempa(9, "id = %4d  %10s prio = %2d  acc = %10u  sticks = %10u.\n", thread->id, x_state2char(thread), thread->c_prio, running, thread->sticks);
  }

  loempa(9, "Eternally sleeping threads...\n");
  for (thread = eternals; thread; thread = thread->snext) {
    loempa(9, "id = %4d  %10s prio = %2d  acc = -----------  sticks = %10u.\n", thread->id, x_state2char(thread), thread->c_prio, thread->sticks);
  }
  
  x_preemption_enable;
  
}

void x_thread_dump(x_thread t) {

  x_size size;
  x_size used;
  x_size left;
  x_int percentage;
  x_size waiting = 0;
  x_size oid = 0; // Owner id
  x_int type;
  x_event event;
  x_monitor monitor;
  x_mutex mutex;
  
  if (t->waiting_for) {
    waiting = t->waiting_for->n_waiting;
  }
 
  x_stack_info(t, &size, &used, &left);
  percentage = (size * 100 - left * 100) / size;

  if (t->competing_for) {
    event = (x_event)t->competing_for;
    type = x_event_type_get(event);
    if (type == xe_monitor) {
      monitor = (x_monitor)event;
      if (monitor->owner) {
        oid = monitor->owner->id;
      }
    }
    else if (type == xe_mutex) {
      mutex = (x_mutex)event;
      if (mutex->owner) {
        oid = mutex->owner->id;
      }
    }
  }
    
  loempa(9, "| %4d    %3d      %3d     %-11s %2d %5d   0x%08x %2d  0x%08x  %3d   %5d %5d %2d%% |\n", t->id, t->c_prio, t->a_prio, x_state2char(t), t->c_quantums, t->num_switches, t->waiting_for, waiting, t->competing_for, oid, size, used, percentage);

  if (t->report) {
    loempa(9, "| = %-93s |\n", t->report(t));
  }

}

extern void reportMemStat(int);

void x_pcbs_dump(void) {

  x_size i;
  x_pcb pcb;
  x_thread t;
  x_size num_threads = 0;
  x_size cursor = 0;
  static x_size max = 0;

  for (i = 0; i < NUM_PRIORITIES - 1; i++) {
    pcb = x_prio2pcb(i);
    if (pcb->t_ready || pcb->t_pending) {

      /*
      ** Ready threads
      */

      t = pcb->t_ready;
      if (t) {
        do {
          num_threads += 1;
          all_threads[cursor++] = t;
          t = t->next;
        } while (t != pcb->t_ready);
      }
      
      /*
      ** Pending threads
      */

      t = pcb->t_pending;
      if (t) {
        do {
          num_threads += 1;
          all_threads[cursor++] = t;
          t = t->next;
        } while (t != pcb->t_pending);
      }

    }
  }

  loempa(9, "+---------- Dumping all threads in priority order. scheduler lock = %d ----------------------+\n", critical_status);  
  loempa(9, "| thread current assigned  thread  cur    thread    +- monitor    competing  owned  ---- stack ---- |\n");
  loempa(9, "|   id    prio     prio    state  quant  switches   |  # waiting  for event  by id  size   used   %% |\n");
  loempa(9, "+---------------------------------------------------V----------V------------------------------------+\n");  

  if (num_threads > max) {
    max = cursor;
  }
  
  for (i = 0; i < cursor; i++) {
    x_thread_dump(all_threads[i]);
  }

  loempa(9, "+------ %3d threads dumped -- (max %3d) ----------- %8d bytes in use ------------------------+\n", num_threads, max, x_mem_total() - x_mem_avail());

}

/*
** A function that prints a one line list of sleeping threads.
*/

#define BUFFER_SIZE 512

void x_sleeping_list(char *caption, x_thread thread) {

  char buffer[BUFFER_SIZE];
  x_int i = 0;
  x_thread current;
  
  for (i = 0; i < BUFFER_SIZE; i++) {
    buffer[i] = 0x00;
  }
  
  x_snprintf(buffer, BUFFER_SIZE, "%10s -> ", caption);
  for (current = thread; current; current = current->snext) {
    x_snprintf(buffer + strlen(buffer), (x_word)(BUFFER_SIZE - strlen(buffer)), "[%2d %9s] ", current->id, x_state2char(current));
  }
  loempa(9, "%s\n", buffer);
  
}

/*
** Dump a set of flags into a character buffer.
*/

void x_flags_as_bits(unsigned char buffer[], x_flags flags) {

  x_word mask = (unsigned)(1 << 31);
  x_size i;

  for (i = 0; i < (sizeof(x_flags) * 8) + 1; i++) {
    buffer[i] = 0x00;
  }
  i = 0;
  while (mask) {
    if ((x_word)flags & mask) {
      buffer[i++] = '1';
    }
    else {
      buffer[i++] = '0';
    }
    mask >>= 1;
  }

}


void x_monitor_dump(const char *caption, x_monitor monitor) {

  char list[BUFFER_SIZE];
  x_thread current;

  loempa(9, "Dumping monitor '%s'\n", caption);
  if (monitor->owner) {
    loempa(9, "Monitor owner = %d, count = %d, waiting = %d, competing = %d\n", monitor->owner->id, monitor->count, monitor->n_waiting, monitor->Event.n_competing);
  }
  else {
    loempa(9, "Monitor free, count = %d\n", monitor->count);
  }
 
  memset(list, 0x00, BUFFER_SIZE);   

  for (current = monitor->l_waiting; current; current = current->l_waiting) {
    x_snprintf(list + strlen(list), (x_word)(BUFFER_SIZE - strlen(list)), "[%d %s] ", current->id, x_state2char(current));
  }  
  
  if (strlen(list)) {
    loempa(9, "  Waiting: %s\n", list);
  }
  else {
    loempa(9, "  Waiting: <none>\n");
  }

  memset(list, 0x00, BUFFER_SIZE);   

  for (current = monitor->Event.l_competing; current; current = current->l_competing) {
    x_snprintf(list + strlen(list), (x_word)(BUFFER_SIZE - strlen(list)), "[%d %s] ", current->id, x_state2char(current));
  }  
  
  if (strlen(list)) {
    loempa(9, "Competing: %s\n", list);
  }
  else {
    loempa(9, "Competing: <none>\n");
  }
  
}

/*
** Dump a simple one line list of the threads competing for an event.
*/

void x_competing_list(char *caption, x_event event) {

  char buffer[BUFFER_SIZE];
  x_thread current;

  memset(buffer, 0x00, BUFFER_SIZE);

  x_snprintf(buffer, BUFFER_SIZE, "%10s : %10s, %d competitors", caption, x_event2char(event), event->n_competing);

  for (current = event->l_competing; current; current = current->l_competing) {
    x_snprintf(buffer + strlen(buffer), (x_word)(BUFFER_SIZE - strlen(buffer)), "[%2d] ", current->id);
  }

  loempa(9, "%s\n", buffer);

}

/*
** Dump a complete list of threads and other info competing for an event.
*/

void x_event_dump_list(x_event event) {

  x_boolean header = true;
  x_thread thread;

  loempa(9, "Waiting for event...\n");  
  for (thread = event->l_competing; thread; thread = thread->l_competing) {
    //x_dump_thread(header, thread);
    header = false;
  }
  
}

/*
** Dump the memory blocks that are still free in a block pool.
*/

void x_blocks_dump(x_block block) {

  x_boll boll;

  for (boll = block->bolls; boll; boll = boll->header.next) {
    loempa(9, "boll %p (%d), bytes %p, next = %p (%d)\n", boll, boll, boll->bytes, boll->header.next,boll->header.next);
  }

}

/*
** Table and function to translate event types to readable strings.
*/

static const char * _type2char[] = {
  "(unused)",    /*  0 */
  "mutex",       /*  1 */
  "queue",       /*  2 */
  "mailbox",     /*  3 */
  "semaphore",   /*  4 */
  "signals",     /*  5 */
  "monitor",     /*  6 */
  "block",       /*  7 */
  "map",         /*  8 */
  "(deleted)",   /*  9 */
  "(unknown)",   /* 10 */
};

const char * x_type2char(x_type type) {
  return _type2char[(type > xe_unknown) ? xe_unknown : type];
}

const char * x_event2char(x_event event) {
  return x_type2char(x_event_type_get(event));
}

static const char * _option2char[] = {
  "OR",          /* 0 */
  "AND",         /* 1 */
  "OR (clear)",  /* 2 */
  "AND (clear)", /* 3 */
  "(unknown)",   /* 4 */
};

const char * option2char(x_option option) {
  return _option2char[(option > xo_unknown) ? xo_unknown : option];
}
