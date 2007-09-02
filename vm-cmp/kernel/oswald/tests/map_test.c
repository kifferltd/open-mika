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
** $Id: map_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <string.h>

#include <tests.h>
#include <oswald.h>

static x_int state = 0;

typedef struct t_Map * t_map;

typedef struct t_Map {
  x_Thread Thread;
  x_thread thread;
  x_ubyte * stack;
  x_int set;
  x_int any;
  x_int probe;
  x_int reset;
  x_int success;
  x_int fail;
  x_int deleted;
  x_int glitch;
} t_Map;

typedef struct t_Entry * t_entry;

typedef struct t_Entry {
  x_thread thread;
  x_size position;
} t_Entry;

static t_Map * t_Maps;
static t_Entry * t_Entries;
static x_int num_threads;
static x_size map_elements;
static x_map map;
static x_word * table;

inline static x_size round_up(x_size value, x_size rounding) {
  return (value + (rounding - 1)) & ~(rounding - 1);
}

typedef enum m_operation {
  set   = 0,
  any   = 1,
  probe = 2,
  reset = 3,
} m_operation;

static const char * c_operation[] = {
  "set",
  "any",
  "probe",
  "reset",
};

static void do_map(void * t) {

  t_map tm = t;
  x_thread thread = tm->thread;
  x_status status = xs_success;
  x_window window;
  m_operation operation;
  x_size element;
  x_boolean bool;
  x_size i;

  while (1) {
    if (state == 2) {
      return;
    }
    
    if (state == 1) {
      operation = x_random() % 4;
      element = x_random() % map_elements;
      window = x_random() % 125;
      if (window > 100) {
        window = x_eternal;
      }
      if (operation == set) {

        /*
        ** See first if we don't own the position yet...
        */

        if (t_Entries[element].thread != thread) {
          tm->set += 1;
          status = x_map_set(map, element, window);
          if (status == xs_success) {
            t_Entries[element].thread = thread;
            tm->success += 1;
          }
          else if (status == xs_deleted) {
            if (state == 2) {
              tm->deleted += 1;
              return;
            }
            oempa("Bad state\n");
            exit(0);
          }
          else {
            tm->fail += 1;
          }
        }
      }
      else if (operation == probe) {
        tm->probe += 1;
        status = x_map_probe(map, element, &bool);
        
        if (status == xs_deleted) {
          if (state == 2) {
            tm->deleted += 1;
            return;
          }
          oempa("Bad state\n");
          exit(0);
        }
        else if (status == xs_success) {
          if (bool && t_Entries[element].thread == NULL) {
            tm->glitch += 1;
          }
          else {
            tm->success += 1;
          }
        }
        else {
          oempa("Bad state %s\n", x_status2char(status));
          exit(0);
        }
      }
      else if (operation == any) {
        tm->any += 1;
      }
      else {
        for (i = x_random() % map_elements; i < map_elements; i++) {
          if (t_Entries[i].thread == thread) {
            break;
          }
        }
        if (i < map_elements) {
          tm->reset += 1;
          status = x_map_reset(map, i);
          if (status == xs_deleted) {
            if (state == 2) {
              tm->deleted += 1;
              return;
            }
            oempa("Bad state\n");
            exit(0);
          }
          else if (status == xs_success) {
            /*
            ** Somebody could have beaten us to this entry since it is released allready...
            */
            if (t_Entries[i].thread != thread) {
              tm->glitch += 1;
            }
            else {
              t_Entries[i].thread = NULL;
              tm->success += 1;
            }
          }
          else {
            oempa("BAD Operation for %d = %s %d, status was %s\n", thread->id, c_operation[operation], element, x_status2char(status));
            oempa("Bad status %s\n", x_status2char(status));
            exit(0);
          }
        }
      }
//      oempa("Operation for %d = %s %d, status was %s\n", thread->id, c_operation[operation], element, x_status2char(status));
    }
    
    x_thread_sleep(5);
  }
  
}

static void map_control(void * t) {

  x_int i;
  x_int counter = 0;
  x_boolean still_living;
  x_int tset = 0;
  x_int tany = 0;
  x_int tprobe = 0;
  x_int treset = 0;
  x_int tsuccess = 0;
  x_int tfail = 0;
  x_int tdeleted = 0;
  x_int tglitch = 0;
  x_status status;

  while (irq_depth) {
    x_thread_sleep(10);
  }
   
  while (1) {
    counter += 1;
    if (state == 0) {
      num_threads = x_random() % 5 + 2;
//      num_threads = 1;
      map_elements = round_up(x_random() % 4096 + 256, 32);
      t_Entries = x_mem_get(map_elements * sizeof(t_Entry));
      memset(t_Entries, 0x00, map_elements * sizeof(t_Entry));
      table = x_mem_get(x_map_size(map_elements));
      map = x_mem_get(sizeof(x_Map));
      x_map_create(map, map_elements, table);
      t_Maps = x_mem_get(sizeof(t_Map) * num_threads);
      memset(t_Maps, 0x00, sizeof(t_Map) * num_threads);
      for (i = 0; i < num_threads; i++) {
        t_Maps[i].thread = &t_Maps[i].Thread;
        t_Maps[i].stack = x_mem_get(BASE_STACK_SIZE);
        status = x_thread_create(t_Maps[i].thread, do_map, &t_Maps[i], t_Maps[i].stack, BASE_STACK_SIZE, prio_offset + 4, 0);
        if (status != xs_success) {
          oempa("Status = '%s'\n", x_status2char(status));
          exit(0);
        }
      }
      state = 1;
    }

    tset = 0;
    tany = 0;
    tprobe = 0;
    treset = 0;
    tsuccess = 0;
    tfail = 0;
    tdeleted = 0;
    tglitch = 0;
    for (i = 0; i < num_threads; i++) {
      tset += t_Maps[i].set;
      tany += t_Maps[i].any;
      tprobe += t_Maps[i].probe;
      treset += t_Maps[i].reset;
      tsuccess += t_Maps[i].success;
      tfail += t_Maps[i].fail;
      tdeleted += t_Maps[i].deleted;
      tglitch += t_Maps[i].glitch;
    }
    oempa("s = %3d a = %3d p = %3d r = %3d | OK = %3d FAIL = %3d DEL = %3d GLITCH = %3d\n", tset, tany, tprobe, treset, tsuccess, tfail, tdeleted, tglitch);
    
    if (counter % 60 == 0) {
      oempa("Deleting...\n");
      state = 2;
      status = x_map_delete(map);
      still_living = true;
      while (still_living) {
        still_living = false;
        for (i = 0; i < num_threads; i++) {
          if (t_Maps[i].thread->state != xt_ended) {
            still_living = true;
            break;
          }
        }
        x_thread_sleep(50);
      }
      oempa("Done...\n");
      
      x_mem_free(map);
      x_mem_free(table);
      for (i = 0; i < num_threads; i++) {
        x_mem_free(t_Maps[i].stack);
      }
      x_mem_free(t_Entries);
      
//      x_thread_sleep(125 * 2);
      state = 0;
      
    }

    x_thread_sleep(125);

  }
   
}

static x_thread map_thread;

x_ubyte * map_test(x_ubyte * memory) {

  x_status status;

  return memory; // CURRENTLY NO MAP TESTS !!
    
  map_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(map_thread, map_control, map_thread, x_alloc_static_mem(memory, BASE_STACK_SIZE), BASE_STACK_SIZE, prio_offset + 3, 0);
  if (status != xs_success) {
    oempa("Status = '%s'\n", x_status2char(status));
    exit(0);
  }

  return memory;
  
}
