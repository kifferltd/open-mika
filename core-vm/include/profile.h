/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.    *
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

#ifndef _PROFILE_H
#define _PROFILE_H

#ifdef JAVA_PROFILE 

#include "oswald.h"
#include "wonka.h"
#include "heap.h"
#include "methods.h"


#define METHOD_ENTER 0x01
#define METHOD_EXIT  0x02

extern w_long prev_timestamp;

#define METHOD_CALL_TABLE_SIZE 15

typedef struct w_ProfileEvent {
  w_short   type;
  w_short   thread;
  w_short   timestamp;
} w_ProfileEvent;

typedef w_ProfileEvent * w_profileEvent;

typedef struct w_ProfileMethodCallData {
  w_method child;
  w_int count;
  struct w_ProfileMethodCallData *next;
  struct w_ProfileMethodCallData *prev;
} w_ProfileMethodCallData;

typedef w_ProfileMethodCallData *w_profileMethodCallData;

/*
** Get an entry from the hashtable.
*/

static inline w_profileMethodCallData w_profileMethodCallGet(w_method parent, w_method child) {
  w_profileMethodCallData iter;

  iter = ((w_profileMethodCallData *)parent->exec.callData)[((w_word)child >> 3) % METHOD_CALL_TABLE_SIZE];

  while(iter) {
    if(iter->child == child) {
      x_preemption_enable;
      return iter;
    }
    iter = iter->next;
  }

  return NULL;
}

/*
** Add an entry to the hashtable.
*/

static inline void w_profileMethodCallAdd(w_method parent, w_method child) {
  w_profileMethodCallData data;
 
  if(!parent) return;

  if(!parent->exec.callData) {
    parent->exec.callData = x_mem_calloc(METHOD_CALL_TABLE_SIZE * 4);
  }
  
  data = w_profileMethodCallGet(parent, child);

  if(!data) {
    data = x_mem_alloc(sizeof(w_ProfileMethodCallData));
    data->child = child;
    data->count = 1;

    data->next = ((w_profileMethodCallData *)parent->exec.callData)[((w_word)child >> 3) % METHOD_CALL_TABLE_SIZE];
    if(data->next) {
      data->next->prev = data;
    }
    ((w_profileMethodCallData *)parent->exec.callData)[((w_word)child >> 3) % METHOD_CALL_TABLE_SIZE] = data;
  }
  else {
    data->count++;
  }
}

#define METHOD_INSTANCE_TABLE_SIZE 15

typedef struct w_ProfileMethodInstanceData {
  w_clazz clazz;
  w_int count;
  struct w_ProfileMethodInstanceData *next;
  struct w_ProfileMethodInstanceData *prev;
} w_ProfileMethodInstanceData;

typedef w_ProfileMethodInstanceData *w_profileMethodInstanceData;

/*
** Get an entry from the hashtable.
*/

static inline w_profileMethodInstanceData w_profileMethodInstanceGet(w_method parent, w_clazz clazz) {
  w_profileMethodInstanceData iter;

  iter = ((w_profileMethodInstanceData *)parent->exec.instanceData)[((w_word)clazz >> 3) % METHOD_CALL_TABLE_SIZE];

  while(iter) {
    if(iter->clazz == clazz) {
      x_preemption_enable;
      return iter;
    }
    iter = iter->next;
  }

  return NULL;
}

/*
** Add an entry to the hashtable.
*/

static inline void w_profileMethodInstanceAdd(w_method parent, w_clazz clazz) {
  w_profileMethodInstanceData data;
 
  if(!parent) return;

  if(!parent->exec.instanceData) {
    parent->exec.instanceData = x_mem_calloc(METHOD_CALL_TABLE_SIZE * 4);
  }
  
  data = w_profileMethodInstanceGet(parent, clazz);

  if(!data) {
    data = x_mem_alloc(sizeof(w_ProfileMethodInstanceData));
    data->clazz = clazz;
    data->count = 1;

    data->next = ((w_profileMethodInstanceData *)parent->exec.instanceData)[((w_word)clazz >> 3) % METHOD_INSTANCE_TABLE_SIZE];
    if(data->next) {
      data->next->prev = data;
    }
    ((w_profileMethodInstanceData *)parent->exec.instanceData)[((w_word)clazz >> 3) % METHOD_INSTANCE_TABLE_SIZE] = data;
  }
  else {
    data->count++;
  }
}

inline static void updateProfileBytecodes(w_frame frame) {
  frame->method->exec.bytecodes++; 
}

inline static void updateProfileCalls(w_method parent, w_method child) {
  child->exec.runs++;
  w_profileMethodCallAdd(parent, child);
}

inline static void profileMethodEnter(w_method method) {
}

inline static void profileMethodExit(w_method method) {
}

inline static void profileAllocInstance(w_thread thread, w_clazz clazz) {
  clazz->instances++;
  clazz->total_instances++;
  if(clazz->instances > clazz->max_instances) {
    clazz->max_instances = clazz->instances;
  }
  w_profileMethodInstanceAdd(thread->top->method, clazz);
}

#else /* JAVA_PROFILE */

inline static void updateProfileBytecodes(w_frame frame) {}
inline static void updateProfileCalls(w_method parent, w_method child) {}
inline static void profileMethodEnter(w_method method) {}
inline static void profileMethodExit(w_method method) {}
inline static void profileAllocInstance(void) {}

#endif /* JAVA_PROFILE */

#endif /* _PROFILE_H */
