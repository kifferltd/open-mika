/**************************************************************************
* Copyright (c) 2004, 2006, 2009 by KIFFER Ltd. All rights reserved.      *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#ifndef _JDWP_EVENTS_H
#define _JDWP_EVENTS_H
#ifdef JDWP

#include "hashtable.h"
#include "wonka.h"

/*
** Pointers to the structures.
*/

typedef struct jdwp_Event           *jdwp_event;
typedef struct jdwp_Location        *jdwp_location;
typedef struct jdwp_Breakpoint      *jdwp_breakpoint;
typedef struct jdwp_Step            *jdwp_step;
typedef struct jdwp_Event_Modifier  *jdwp_event_modifier;

typedef struct jdwp_Location {
  w_method method;
  w_int pc;
} jdwp_Location;

/*
** The breakpoint structure. When the debugger sets a breakpoint on a certain location,
** the opcode at that location will be overwritten with a breakpoint opcode. This 
** structure holds the original opcode, the location of the breakpoint and a reference to
** the event that caused this breakpoint to be set.
*/

typedef struct jdwp_Breakpoint {
  w_ubyte          *code;
  w_ubyte          original;
  jdwp_event       event;
  jdwp_Location    location;
} jdwp_Breakpoint;

/*
** The step structure. The thread, event, size, depth, and frame are set up
**  when the step event is created; if the step depth is OVER or OUT then 
** the frame is set to the current top frame of the thread. For step OVER,
** the an event will only be triggered for bytecodes executed in the given
** frame; for step OUT, no events will be triggered until the given frame
** is left, at which moment the frame is set to NULL and the depth is 
** changed to INTO.
** The location is filled in when triggering the event itself, so 
** that it can be included in the JDWP reply.
*/

typedef struct jdwp_Step {
  w_thread thread;        /* Thread inside which the event will take place */
  jdwp_event event;       /* jdwp_event which describes the step */
  w_int size;             /* Size = MIN(0) / LINE(1) */
  w_int depth;            /* Depth = INTO(0) / OVER (1) / OUT(2) */
  w_frame frame;          /* Frame in which event will occur if OVER or OUT */
  jdwp_Location location; /* Location where event was triggered */
} jdwp_Step;

/* This is the modifier structure for events (see below). 
** Which value we need from the union depends on the value of mod_kind.
*/

typedef struct jdwp_Event_Modifier {
  w_ubyte mod_kind;
  
  union {
    w_int     count;
    w_int     exprID;
    w_instance threadID;
    w_clazz   clazz;
    w_string  match_pattern;
    w_string  exclude_pattern;
    jdwp_Location location;
    struct {
      w_clazz exception;
      w_byte  caught;
      w_byte  uncaught;
    } ex;
    struct {
      w_clazz declaring;
      w_field ID;
    } field;
    struct {
      w_thread thread;
      w_int   size;
      w_int   depth;
    } step;
    w_instance instance;
  } condition;

  jdwp_event_modifier next;
  jdwp_event_modifier prev;

} jdwp_Event_Modifier;

/*
** The event structure. Every event has a certain ID that uniquely defines it and an indicator
** of what kind event it is. Then there's also a suspend policy which tells us what to do if 
** this event occurs (e.g supend the thread, suspend all threads or don't suspend anything).
** Last but not least is the list with modifiers. These are extra conditions the event has to
** meet before it's passed on to the debugger.
*/

typedef struct jdwp_Event {
  w_int         eventID;
  w_ubyte       event_kind;
  w_ubyte       suspend_policy;
  w_ushort      dummy;
  jdwp_event    next;
  jdwp_event    prev;
  jdwp_event_modifier  modifiers;
  union {
    jdwp_breakpoint break_point;
    jdwp_step       step_point;
  } point;
} jdwp_Event;

/* 
** All the events we have to watch out for are kept in 2 structures. 
** The first one is a array of circular linked lists which groups all the events of a certain 
** type in one list (e.g. All the 'class prepared' events).
** The other one is a hashtable which maps eventID to event.
** For breakpoints we also have a table mapping bytecode address to breakpoint.
*/

#define JDWP_EVENT_HASHTABLE_SIZE 97
#define JDWP_BREAKPOINT_HASHTABLE_SIZE 97

extern jdwp_event jdwp_events_by_kind[255]; /* One entry for every event kind */
extern w_hashtable jdwp_event_hashtable;     /* To look for eventID's */
extern w_hashtable jdwp_breakpoint_hashtable; /* To look for breakpoints */

/*
** A few functions to add, remove and find stored events.
*/

w_void      jdwp_event_add(jdwp_event event);
w_void      jdwp_event_remove(w_int ID);
jdwp_event  jdwp_event_get_ID(w_int ID);
w_void      jdwp_event_add_modifier(jdwp_event event, jdwp_event_modifier modifier);
jdwp_event  jdwp_event_alloc(w_int event_kind, w_int suspend_policy);
w_void      jdwp_clear_all_events(w_void);

/*
** Breakpoints.
*/

w_void jdwp_breakpoint_set(jdwp_event event);
w_void jdwp_breakpoint_clear(jdwp_event event);
w_void jdwp_breakpoint_clear_all(void);

/*
** Single-step events.
*/
w_void jdwp_step_set(jdwp_event event);
w_void jdwp_step_clear(jdwp_event event);

#endif /* JDWP */

/*
** These are the actual event handlers. Wonka calls these functions to let us know what's
** going on. We then have to look for a matching event request to find out if the debugger asked 
** for these kind of events.
** 
** They are defined outside the '#ifdef JDWP' to get rid of compiler warnings.
*/

w_void  jdwp_event_class_prepare(w_clazz clazz);
w_void  jdwp_event_thread_start(w_thread thread);
w_void  jdwp_event_thread_end(w_thread thread);

extern w_boolean jdwp_holding_events;

void jdwp_single_step_set(jdwp_event event);
void jdwp_single_step_clear(jdwp_event event);

void jdwp_dealloc_event(jdwp_event event);

#endif /* _JDWP_EVENTS_H */

