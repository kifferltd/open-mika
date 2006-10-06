#ifndef _JDWP_EVENTS_H
#define _JDWP_EVENTS_H
#ifdef JDWP

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
* Modifications copyright (c) 2004, 2006 by Chris Gray, /k/ Embedded Java *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

#include "hashtable.h"
#include "wonka.h"

/*
** Pointers to the structures.
*/

typedef struct jdwp_Event           *jdwp_event;
typedef struct jdwp_Location        *jdwp_location;
typedef struct jdwp_Breakpoint      *jdwp_breakpoint;
typedef struct jdwp_Event_Modifier  *jdwp_event_modifier;

typedef struct jdwp_Location {
  w_int tag;
  w_clazz clazz;
  w_method method;
  w_int pc;
} jdwp_Location;

/*
** The breakpoint structure. When the debugger sets a breakpoint on a certain location,
** the opcode at that location will be overwritten with a breakpoint opcode. This 
** structure holds the original opcode, the location of the breakpoint and a reference to
** the event that caused this breakpoint to be set.
** These breakpoints are stored in a hashtable.
*/

typedef struct jdwp_Breakpoint {
  w_ubyte          *code;
  w_ubyte          original;
  jdwp_event       event;
  jdwp_Location    location;
  jdwp_breakpoint  next;
  jdwp_breakpoint  prev;
} jdwp_Breakpoint;

/*
** This is the modifier structure for events (see below). 
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
      w_instance thread;
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
  w_int                eventID;
  w_ubyte              event_kind;
  w_ubyte              suspend_policy;
  jdwp_event_modifier  modifiers;
  jdwp_breakpoint      break_point;

  jdwp_event    next;
  jdwp_event    prev;
  jdwp_event    hash_next;
  jdwp_event    hash_prev;
} jdwp_Event;

/* 
** All the events we have to watch out for are kept in 2 structures. 
** The first one is a array of circular linked lists which groups all the events of a certain 
** type in one list (e.g. All the 'class prepared' events).
** The other one is a hashtable of circular linked lists in which all the elements have the
** same hashvalue. This is used to find an event with a certain ID at reasonable speed.
*/

#define JDWP_EVENT_HASHTABLE_SIZE 97
#define JDWP_BREAKPOINT_HASHTABLE_SIZE 97

extern jdwp_event jdwp_events_by_kind[255]; /* One entry for every event kind */
extern w_hashtable jdwp_event_hashtable;     /* To look for eventID's */
extern w_hashtable jdwp_breakpoint_hashtable; /* To look for breakpointID's */

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
w_ubyte jdwp_event_breakpoint(w_ubyte *code);

w_boolean jdwp_holding_events;

#endif /* _JDWP_EVENTS_H */

