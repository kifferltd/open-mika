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

#include "clazz.h"
#include "core-classes.h"
#include "descriptor.h"
#include "exception.h"
#include "fifo.h"
#include "grobag.h"
#include "hashtable.h"
#include "jdwp.h"
#include "jdwp_events.h"
#include "jdwp-protocol.h"
#include "methods.h"
#include "oswald.h"
#include "threads.h"
#include "wonka.h"
#include "wstrings.h"

extern void jdwp_internal_suspend_all(void);
extern void jdwp_internal_suspend_one(w_thread);

jdwp_event jdwp_events_by_kind[255]; /* One entry for every event kind */
w_hashtable jdwp_event_hashtable;     /* To look for eventID's */
w_hashtable jdwp_breakpoint_hashtable; /* To look for breakpointID's */

w_method jdwp_Thread_run_method;
w_boolean jdwp_holding_events;

/*
** Add a new event. This happens in 2 big steps. First the event is
** added to a cicular linked list of events of the same kind. Then 
** the event is also added to a hashtable with it's event ID as key
** so it can be recovered fast and easy (Keeping in mind that the 
** debugger refers to it by it's ID). 
*/

w_void jdwp_event_add(jdwp_event event) {
  if(jdwp_events_by_kind[event->event_kind] == NULL) {      /* First one in the list */
    jdwp_events_by_kind[event->event_kind] = event;
    event->next = event;
    event->prev = event;
  } else {                                          /* Put it on the end of the list */
    event->next = jdwp_events_by_kind[event->event_kind];
    event->prev = jdwp_events_by_kind[event->event_kind]->prev;
    event->prev->next = event;
    jdwp_events_by_kind[event->event_kind]->prev = event;
  }

  woempa(7, "Adding ID 0x%08x -> event %p to event hashtable\n", (unsigned)event->eventID, event);
  ht_write(jdwp_event_hashtable, (w_word)event->eventID, (w_word)event);
}


/*
** Throw away an event. Events have a unique ID, so we look for the event with this 
** specific ID. Removing an event also takes place in 2 steps. First it gets taken 
** out of the linked list of events of the same kind. Then it gets deleted in the 
** hashtable.
*/

w_void jdwp_event_remove(w_int ID) {
  jdwp_event event = jdwp_event_get_ID(ID);

  if(event == jdwp_events_by_kind[event->event_kind]) {     /* First one on the list */
    if(event == event->next) {                      /* And also the last one */
      jdwp_events_by_kind[event->event_kind] = NULL;
    } else {
      jdwp_events_by_kind[event->event_kind] = event->next;
    }
  }
  
  event->next->prev = event->prev;
  event->prev->next = event->next;

  woempa(7, "deleting ID 0x%08x from event hashtable\n", (unsigned)ID);
  ht_erase(jdwp_event_hashtable, event->eventID);
}


/*
** Lookup an event. Every event has a unique ID and we already have a hashtable 
** with all events and hashed with there ID, so this is fairly straight forward.
*/

jdwp_event jdwp_event_get_ID(w_int ID) {
  jdwp_event result = (jdwp_event)ht_read(jdwp_event_hashtable, ID);
  woempa(7, "ID 0x%08x -> event %p\n", (unsigned)ID, result);

  return result;
}


/*
** Add an event modifier to a given event. These modifiers are stored in a 
** circular linked list in the event. Adding elements is easy.
*/

w_void jdwp_event_add_modifier(jdwp_event event, jdwp_event_modifier modifier) {
  
  if(event->modifiers == NULL) {                    /* First one in the list */
    event->modifiers = modifier;
    event->modifiers->next = event->modifiers;
    event->modifiers->prev = event->modifiers;
  } else {                                          /* Put it on the end of the list */
    modifier->next = event->modifiers;
    modifier->prev = event->modifiers->prev;
    modifier->prev->next = modifier;
    event->modifiers->prev = modifier;
  }
}


/*
** The current event ID. Every event has it's own unique ID, so whenever a new event is 
** created, this counter will be incremented.
*/

static w_int current_eventID = 0;


/*
** Allocate a new event structure, give it an ID, store the eventkind and suspendpolicy.
*/

jdwp_event jdwp_event_alloc(w_int event_kind, w_int suspend_policy) {
  jdwp_event event = allocMem(sizeof(jdwp_Event));
  
  event->eventID = (event_kind == jdwp_evt_vm_start) ? 0 : ++current_eventID;
  event->event_kind = event_kind;
  event->suspend_policy = suspend_policy;
  event->modifiers = NULL;
  
  return event;
}


/*
** Clear all events.
*/

w_void jdwp_clear_all_events() {
  w_int i;
  jdwp_event event;
  jdwp_event temp;

  for(i=0; i<255; i++) {
    if(jdwp_events_by_kind[i]) {
      event = jdwp_events_by_kind[i];
      event->prev->next = NULL;
      
      while(event) {
        temp = event->next;

        if(event->event_kind == jdwp_evt_breakpoint) {
          jdwp_breakpoint_clear(event);
        }

        /* TODO: modifiers ! */
        ht_erase(jdwp_event_hashtable, event->eventID);
        
        releaseMem(event);
        
        event = temp;
      }
    
      jdwp_events_by_kind[i] = NULL;
    }
  }
}

/*
** Clear all breakpoint events. Probably broken. TODO: replace with a regular hashtable.
*/

w_void jdwp_breakpoint_clear_all(void) {
  w_int i;
  jdwp_event event;
  jdwp_event temp;

  // TODO: surely we only need to do this for i == BREAKPOINT?
  for(i=0; i<255; i++) {
    if(jdwp_events_by_kind[i]) {
      event = jdwp_events_by_kind[i];
      while(event) {
        temp = event->next;
        if(event->event_kind == jdwp_evt_breakpoint) {
          event->prev->next = temp;
          jdwp_breakpoint_clear(event);

        /* TODO: modifiers ! */
          ht_erase(jdwp_event_hashtable, event->eventID);
        
          releaseMem(event);
        }
        
        event = temp;
      }
    
      jdwp_events_by_kind[i] = NULL;
    }
  }
}


/*
** Add a breakpoint to the breakpoint hashtable.
*/

w_void jdwp_breakpoint_add(jdwp_breakpoint breakpoint) {
  woempa(7, "Adding breakpoint (event %p, original bytecode %02x, location %d:%k:%m:%d) at %p\n", breakpoint->event, breakpoint->original, breakpoint->location.tag, breakpoint->location.clazz, breakpoint->location.method, breakpoint->location.pc, breakpoint->code);
  ht_write(jdwp_breakpoint_hashtable, (w_word)breakpoint->code, (w_word)breakpoint);
}


/*
** Delete a breakpoint from the breakpoint hashtable.
*/

w_void jdwp_breakpoint_remove(jdwp_breakpoint breakpoint) {
  woempa(7, "Removing breakpoint (event %p, original bytecode %02x, location %d:%k:%m:%d) at %p\n", breakpoint->event, breakpoint->original, breakpoint->location.tag, breakpoint->location.clazz, breakpoint->location.method, breakpoint->location.pc, breakpoint->code);
  ht_erase(jdwp_breakpoint_hashtable, (w_word)breakpoint->code);
}


/*
** Search for a breakpoint in the breakpoint hashtable.
*/

jdwp_breakpoint jdwp_breakpoint_get(w_code code) {
  jdwp_breakpoint breakpoint =  (jdwp_breakpoint)ht_read(jdwp_breakpoint_hashtable, (w_word)code);
  woempa(7, "Getting breakpoint (event %p, original bytecode %02x, location %d:%k:%m:%d) at %p\n", breakpoint->event, breakpoint->original, breakpoint->location.tag, breakpoint->location.clazz, breakpoint->location.method, breakpoint->location.pc, breakpoint->code);
    
  return breakpoint;
}


/*
** Send an event to the debugger.
*/

w_void jdwp_send_event(jdwp_event event, w_grobag *data) {
  /*
  ** Normally JDWP uses composite events. This means that certain event types will be 
  ** grouped together and send to the debugger in one packet. Since this behaviour is 
  ** rather hard, we send events one by one.
  **
  ** The overall event packet looks like this :
  **
  **   byte   suspend_policy   The suspend policy used for this event.
  **   int    events           The number of events in the packet. In our case this is
                               always 1.
  **   byte   event_kind       The event kind of the event.
  **   int    eventID          The ID of the event.
  **   ...    ...              Event specific data.
  */
  woempa(7, "Sending composite event: suspend policy = %d, 1 event, kind %d, requestID %d\n", event->suspend_policy, event->event_kind, event->eventID);

  // suspend policy
  jdwp_put_u1(&command_grobag, event->suspend_policy);
  // number events
  jdwp_put_u4(&command_grobag, 1);
  // event kind
  jdwp_put_u1(&command_grobag, event->event_kind);
  // event ID
  jdwp_put_u4(&command_grobag, (w_word)event->eventID);
  // event specific data
  if (data && (*data) && (*data)->occupancy) {
    jdwp_put_bytes(&command_grobag, (*data)->contents, (w_size)(*data)->occupancy);
    (*data)->occupancy = 0;
  }
  jdwp_send_command(&command_grobag, jdwp_cmdset_event, jdwp_event_composite);
}


/*
** Suspend all threads
*/

void jdwp_suspend_all_threads(void) {
  jdwp_internal_suspend_all();
}

/*
** Suspend one thread
*/

void jdwp_suspend_one_thread(w_thread thread) {
  jdwp_internal_suspend_one(thread);
}


/*
** Check the suspend policy and act accordingly.
** There are 3 different suspend policies :
**   1. none           No threads will be suspended when the event occurs.
**   2. event_thread   Only the thread in which the event occured will be suspended.
**   3. all            All threads will be suspended.
*/

w_void jdwp_check_suspend(jdwp_event event) {

  switch(event->suspend_policy) {
    case jdwp_sp_none:
      break;

    case jdwp_sp_event_thread:
      jdwp_internal_suspend_one(currentWonkaThread);
      break;

    case jdwp_sp_all:
      jdwp_suspend_all_threads();
  }

  return;
}


/*
** Set a breakpoint.
*/

w_void jdwp_breakpoint_set(jdwp_event event) {
  jdwp_event_modifier  modifier = event->modifiers;
  w_long               pc;
  w_method             method;
  w_clazz              clazz;
  jdwp_breakpoint      point;
  
  woempa(9, "--== Setting a breakpoint ==--\n");

  /*
  ** Loop through the modifiers of the event to find the location_only modifier.
  ** This modifiers contains the clazz, method and pc where the breakpoint should be set.
  */
  
  while(modifier->mod_kind != 7) modifier = modifier->next;

  /*
  ** Get clazz, method and pc out of the location modifier.
  */
 
  clazz = modifier->condition.location.clazz;
  method = modifier->condition.location.method;
  pc = modifier->condition.location.pc;

  woempa(9, "    clazz: %w,  method: %w,  pc: %Ld,  original opcode: 0x%x\n", clazz->dotified, method->spec.name, pc, method->exec.code[pc]);

  /*
  ** Allocate memory for a breakpoint structure.
  */

  point = allocMem(sizeof(jdwp_Breakpoint));

  /*
  ** Fill in the breakpoint structure.
  */

  point->code = &method->exec.code[pc];       /* The address of the replaced opcode */
  point->original = method->exec.code[pc];    /* The original opcode that was replaced by a breakpoint */
  point->event = event;                  /* The event that requested this breakpoint to be set */
  memcpy(&point->location, &modifier->condition.location, sizeof(jdwp_Location));

  /*
  ** Store the breakpoint in the event as well.
  */
  
  event->break_point = point;

  /*
  ** Replace the original opcode with a breakpoint opcode (0xca).
  */

  method->exec.code[pc] = 0xca;

  /*
  ** Add this breakpoint structure to the hashtable of breakpoints.
  */

  jdwp_breakpoint_add(point);

}


/*
** Clear a breakpoint.
*/

w_void jdwp_breakpoint_clear(jdwp_event event) {
  w_method             method;
  w_clazz              clazz;
  
  woempa(9, "--== Clearing a breakpoint ==--\n");

  /*
  ** Get the clazz and method out of the event->breakpoint.
  */

  clazz = event->break_point->location.clazz;
  method = event->break_point->location.method;

  woempa(9, "    clazz: %w,  method: %w\n", clazz->dotified, method->spec.name);

  /*
  ** Put the original opcode back where it belongs.
  */

  *event->break_point->code = event->break_point->original;

  /*
  ** Remove the breakpoint from the hashtable.
  */
  
  jdwp_breakpoint_remove(event->break_point);
  
  /* 
  ** Clean up.
  */
  
  releaseMem(event->break_point);
}


/*
** From this point on are the event handlers. Wonka calls these to let us know 
** something happened. We then need to look in our tables for a matching event 
** request. If one is found, the event gets through to the debuggger. If there 
** aren't any matching event requests stored, this particular event will be 
** ignored.
*/

extern w_boolean matchClassname(w_clazz clazz, w_string match_pattern);

/*
** A Throwable has been thrown, and caught.
*/

w_void jdwp_event_exception(w_instance throwable, w_method catch_method, w_int catch_pc) {
  jdwp_event events = jdwp_events_by_kind[jdwp_evt_exception];
  jdwp_event_modifier  modifier;
  w_clazz clazz = instance2clazz(throwable);
  w_Exr * records = getWotsitField(throwable, F_Throwable_records);
  w_method throw_method = records->method;
  w_int throw_pc = records->pc;
  // TODO: is it possible that records == NULL?
  w_int go_ahead;
  w_thread thread = currentWonkaThread;
  w_grobag gb = NULL;
  jdwp_Location location;

  /*
  ** Only go through with this if the events are enabled.
  */

  if(!jdwp_events_enabled) return;

  while (isSet(throw_method->flags, ACC_NATIVE)) {
    ++records;
    throw_method = records->method;
    throw_pc = records->pc;
  }

  /*
  ** Do some locking for sanity.
  */

  x_mutex_lock(jdwp_mutex, x_eternal);
  
  woempa(7, "%e was thrown at %M:%d and caught at %M:%d\n", throwable, throw_method, throw_pc, catch_method, catch_pc);
  /*
  ** First check if there are any events requested for this event kind. 
  ** If there are any events, go through them one by one.
  */

  if(events) {
    
    do {

      go_ahead = 1;

      /*
      ** Check the modifiers.
      */

      modifier = events->modifiers;

      if(modifier) {

        do {
          switch (modifier->mod_kind) {
          case 1:
            go_ahead &= (--modifier->condition.count == 0);
            woempa(7, "Modifier of kind 1 specified count %d, will%s send event\n", modifier->condition.count, go_ahead ? "" : " not");
            break;

          case 4:
            go_ahead &= isSuperClass(modifier->condition.clazz, clazz);
            woempa(7, "Modifier of kind 3 specified referenceTypeID %k, will%s send event\n", modifier->condition.clazz, go_ahead ? "" : " not");
            break;

          case 5:
            go_ahead &= matchClassname(clazz, modifier->condition.match_pattern);
            woempa(7, "Modifier of kind 5 specified match_pattern %w, will%s send event\n", modifier->condition.match_pattern, go_ahead ? "" : " not");
            break;

          case 6:
            go_ahead &= !matchClassname(clazz, modifier->condition.match_pattern);
            woempa(7, "Modifier of kind 6 specified match_pattern %w, will%s send event\n", modifier->condition.match_pattern, go_ahead ? "" : " not");
            break;

          case 8:
            if (modifier->condition.ex.exception) {
              go_ahead &= (modifier->condition.ex.exception == NULL) || isSuperClass(modifier->condition.ex.exception, clazz);
              // HACK: if caught by Thread._run(), treat as uncaught
              go_ahead &= (catch_method == jdwp_Thread_run_method) ? modifier->condition.ex.uncaught : modifier->condition.ex.caught;
            }
            woempa(7, "Modifier of kind 8 specified throwable type %k, caught = %d uncaught = %d, exception%d caught, will%s send event\n", modifier->condition.ex.exception, modifier->condition.ex.uncaught, modifier->condition.ex.caught, catch_method == jdwp_Thread_run_method ? " not" : "", go_ahead ? "" : " not");
          }
        
          modifier = modifier->next;

        } while(modifier != events->modifiers && go_ahead);
        
      }

      if(go_ahead) {

        /*
        ** The modifiers didn't block us from sending this event, so we go ahead
        ** and send it to the debugger.
        */

        // Thread in which thrown
        jdwp_put_objectref(&gb, thread->Thread);
        // Location where thrown
        location.tag = isSet(throw_method->spec.declaring_clazz->flags, ACC_INTERFACE) ? jdwp_tt_interface : jdwp_tt_class;
        location.clazz = throw_method->spec.declaring_clazz;
        location.method = throw_method;
        location.pc = throw_pc;
        jdwp_put_location(&gb, &location);
        // Tagged objectID of exception
        jdwp_put_u1(&gb, jdwp_tag_object);
        jdwp_put_objectref(&gb, throwable);
        // Location where thrown
        location.tag = isSet(catch_method->spec.declaring_clazz->flags, ACC_INTERFACE) ? jdwp_tt_interface : jdwp_tt_class;
        location.clazz = catch_method->spec.declaring_clazz;
        location.method = catch_method;
        location.pc = catch_pc;
        jdwp_put_location(&gb, &location);
        /*
        ** Send the event to the debugger.
        */
        
        jdwp_send_event(events, &gb);

        /*
        ** Clean up.
        */

        releaseGrobag(&gb);

        /*
        ** Check the suspend status of the event and act accordingly.
        */

        jdwp_check_suspend(events);
        
      }

      /*
      ** Go to the next event in the list.
      */
      
      events = events->next;
      
    } while(events != jdwp_events_by_kind[jdwp_evt_exception]);
    
  }

  /*
  ** Unlock.
  */
  
  x_mutex_unlock(jdwp_mutex);
  
  return;
}

/*
** A class has been prepared
*/

w_void jdwp_event_class_prepare(w_clazz clazz) {
  jdwp_event events = jdwp_events_by_kind[jdwp_evt_class_prepare];
  jdwp_event_modifier  modifier;
  w_int go_ahead;
  w_thread thread = currentWonkaThread;
  w_grobag gb = NULL;
  w_string desc_string;

  /*
  ** Only go through with this if the events are enabled.
  */

  if(!jdwp_events_enabled) return;

  /*
  ** Do some locking for sanity.
  */

  x_mutex_lock(jdwp_mutex, x_eternal);
  
  woempa(7, "%K has been prepared\n", clazz);
  /*
  ** First check if there are any events requested for this event kind. 
  ** If there are any events, go through them one by one.
  */

  if(events) {
    
    do {

      go_ahead = 1;

      /*
      ** Check the modifiers.
      */

      modifier = events->modifiers;

      if(modifier) {

        do {
          switch (modifier->mod_kind) {
          case 1:
            go_ahead &= (--modifier->condition.count == 0);
            woempa(7, "Modifier of kind 1 specified count %d, will%s send event\n", modifier->condition.count, go_ahead ? "" : " not");
            break;

          case 4:
            go_ahead &= isSuperClass(modifier->condition.clazz, clazz);
            woempa(7, "Modifier of kind 3 specified referenceTypeID %k, will%s send event\n", modifier->condition.clazz, go_ahead ? "" : " not");
            break;

          case 5:
            go_ahead &= matchClassname(clazz, modifier->condition.match_pattern);
            woempa(7, "Modifier of kind 5 specified match_pattern %w, will%s send event\n", modifier->condition.match_pattern, go_ahead ? "" : " not");
            break;

          case 6:
            go_ahead &= !matchClassname(clazz, modifier->condition.match_pattern);
            woempa(7, "Modifier of kind 6 specified match_pattern %w, will%s send event\n", modifier->condition.match_pattern, go_ahead ? "" : " not");
          }
        
          modifier = modifier->next;

        } while(modifier != events->modifiers && go_ahead);
        
      }

      if(go_ahead) {

        /*
        ** The modifiers didn't block us from sending this event, so we go ahead
        ** and send it to the debugger.
        */

        // TODO: if thread is jdwp_thread, write null here and if suspend policy
        // is to suspend thread change it to suspend all.
        jdwp_put_objectref(&gb, thread->Thread);
        jdwp_put_u1(&gb, isSet(clazz->flags, ACC_INTERFACE) ? jdwp_tt_interface : jdwp_tt_class);
        jdwp_put_clazz(&gb, clazz);
        desc_string = clazz2desc(clazz);
        jdwp_put_string(&gb, desc_string);
        deregisterString(desc_string);
        // TODO: clazz2whatever
        jdwp_put_u4(&gb, jdwp_cs_prepared);
        /*
        ** Send the event to the debugger.
        */
        
        jdwp_send_event(events, &gb);

        /*
        ** Clean up.
        */

        releaseGrobag(&gb);

        /*
        ** Check the suspend status of the event and act accordingly.
        */

        jdwp_check_suspend(events);
        
      }

      /*
      ** Go to the next event in the list.
      */
      
      events = events->next;
      
    } while(events != jdwp_events_by_kind[jdwp_evt_class_prepare]);
    
  }

  /*
  ** Unlock.
  */
  
  x_mutex_unlock(jdwp_mutex);
  
  return;
}


/*
** A thread has started.
*/

w_void jdwp_event_thread_start(w_thread thread) {
  jdwp_event           events = jdwp_events_by_kind[jdwp_evt_thread_start];
  jdwp_event_modifier  modifier;
  w_int                go_ahead;
  w_grobag             gb = NULL;
  
  /*
  ** Only go through with this if the events are enabled.
  */

  if(!jdwp_events_enabled) return;

  /*
  ** Do some locking for sanity.
  */

  x_mutex_lock(jdwp_mutex, x_eternal);
  
  /* 
  ** Check if there are any events of this type requested. If there are, go over them
  ** one by one and check if we need to send an event.
  */

  woempa(7, "Thread '%t' has started\n", thread);
  if(events) {
    
    do {

      go_ahead = 1;

      /*
      ** Get the modifiers and go over them one by one to check if we need to send
      ** this event.
      */
      
      modifier = events->modifiers;

      if(modifier) {

        do {
          switch (modifier->mod_kind) {
          case 1:
            go_ahead &= (--modifier->condition.count == 0);
            woempa(7, "Modifier of kind 1 specified count %d, will%s send event\n", modifier->condition.count, go_ahead ? "" : " not");
            break;

          case 3:
            go_ahead &= (modifier->condition.threadID == thread->Thread);
            woempa(7, "Modifier of kind 3 specified threadID %j, will%s send event\n", modifier->condition.threadID, go_ahead ? "" : " not");
          }
        
          modifier = modifier->next;

        } while(modifier != events->modifiers);
        
      }

      if(go_ahead) {

        /*
        ** None of the modifiers blocked this event from being sent, so we can go ahead 
        ** and send an event to the debugger.
        */
        
        jdwp_put_objectref(&gb, thread->Thread);
        jdwp_send_event(events, &gb);

        /*
        ** Check the suspend status and act accordingly.
        */
        
        jdwp_check_suspend(events);
        
      }

      /*
      ** Go to the next event in the list.
      */

      events = events->next;

    } while(events != jdwp_events_by_kind[jdwp_evt_thread_start]);

  }

  /*
  ** Unlock.
  */

  x_mutex_unlock(jdwp_mutex);
  
  return;
}


/*
** A thread has ended.
*/

w_void jdwp_event_thread_end(w_thread thread) {
  jdwp_event           events = jdwp_events_by_kind[jdwp_evt_thread_end];
  jdwp_event_modifier  modifier;
  w_int                go_ahead;
  w_grobag             gb = NULL;
  
  /*
  ** Only go through with this if the events are enabled.
  */

  if(!jdwp_events_enabled) return;

  /*
  ** Do some locking for sanity.
  */

  x_mutex_lock(jdwp_mutex, x_eternal);
  
  /* 
  ** Check if there are any events of this type requested. If there are, go over them
  ** one by one and check if we need to send an event.
  */

  woempa(7, "Thread '%t' has ended\n", thread);
  if(events) {
    
    do {

      go_ahead = 1;

      /*
      ** Get the modifiers and go over them one by one to check if we need to send
      ** this event.
      */
      
      modifier = events->modifiers;

      if(modifier) {

        do {
          switch (modifier->mod_kind) {
          case 1:
            go_ahead &= (--modifier->condition.count == 0);
            woempa(7, "Modifier of kind 1 specified count %d, will%s send event\n", modifier->condition.count, go_ahead ? "" : " not");
            break;

          case 3:
            go_ahead &= (modifier->condition.threadID == thread->Thread);
            woempa(7, "Modifier of kind 3 specified threadID %j, will%s send event\n", modifier->condition.threadID, go_ahead ? "" : " not");
          }
        
          modifier = modifier->next;

        } while(modifier != events->modifiers);
        
      }

      if(go_ahead) {

        /*
        ** None of the modifiers blocked this event from being sent, so we can go ahead 
        ** and send an event to the debugger.
        */
        
        jdwp_put_objectref(&gb, thread->Thread);
        jdwp_send_event(events, &gb);

        /*
        ** Check the suspend status and act accordingly.
        */
        
        jdwp_check_suspend(events);
        
      }

      /*
      ** Go to the next event in the list.
      */

      events = events->next;

    } while(events != jdwp_events_by_kind[jdwp_evt_thread_end]);

    if (gb) releaseGrobag(&gb);

  }

  /*
  ** Unlock.
  */

  x_mutex_unlock(jdwp_mutex);
  
  return;
}

extern w_int jdwp_config_suspend; 
extern w_int jdwp_connected; 

/*
** Send the VM_START event.
*/
void jdwp_event_vm_start(w_instance threadID) {
  w_grobag gb = NULL;

  jdwp_event event = jdwp_event_alloc(jdwp_evt_vm_start, jdwp_sp_none);
  jdwp_put_objectref(&gb, threadID);
  jdwp_send_event(event, &gb);
  jdwp_check_suspend(event);
  releaseGrobag(&gb);
}

/*
** We've hit a breakpoint !
*/

w_ubyte jdwp_event_breakpoint(w_ubyte *code) {
  jdwp_event           event;
  jdwp_breakpoint      point;
  w_instance           instance = currentWonkaThread->Thread;
  w_grobag             gb = NULL;
  
  /*
  ** Only go through with this if the events are enabled.
  */

  if(!jdwp_events_enabled) {
    return jdwp_breakpoint_get(code)->original;
  }

  /*
  ** Do some locking for sanity.
  */

  x_mutex_lock(jdwp_mutex, x_eternal);
  
  woempa(9, " -= EVENT =-    B R E A K P O I N T\n");

  /*
  ** Lookup the breakpoint in the hashtable and also get the event.
  */
 
  point = jdwp_breakpoint_get(code);
  event = point->event;
  woempa(7, "Hit breakpoint (event %p, original bytecode %02x, location %d:%k:%m:%d) at %p\n", point->event, point->original, point->location.tag, point->location.clazz, point->location.method, point->location.pc, point->code);

  /*
  ** Store the thread ID.
  */

  jdwp_put_objectref(&gb, instance);
  jdwp_put_location(&gb, &point->location);
  jdwp_send_event(event, &gb);

  /*
  ** Check the suspend policy and act accordingly.
  */
        
  jdwp_check_suspend(event);

  /*
  ** Unlock.
  */

  x_mutex_unlock(jdwp_mutex);
  releaseGrobag(&gb);

  /*
  ** Return the original opcode. This is used in the interpreter loop where the breakpoint
  ** opcode will be overwritten with the original opcode. Then the pc is turned back by one
  ** so the interpreter will go over the original opcode. Then the breakpoint will be written
  ** over the original opcode once again.
  */

  return point->original;
}

void jdwp_internal_suspend_one(w_thread thread) {
  if ((thread->flags & WT_THREAD_SUSPEND_COUNT_MASK) != WT_THREAD_SUSPEND_COUNT_MASK) {
    thread->flags += 1 << WT_THREAD_SUSPEND_COUNT_SHIFT;
    woempa(7, "JDWP: suspending %t, count now %d\n", thread, (thread->flags & WT_THREAD_SUSPEND_COUNT_MASK) >> WT_THREAD_SUSPEND_COUNT_SHIFT);
  }
  else {
    woempa(7, "JDWP: not suspending %t, count is %d\n", thread, (thread->flags & WT_THREAD_SUSPEND_COUNT_MASK) >> WT_THREAD_SUSPEND_COUNT_SHIFT);
  }
}

void jdwp_internal_resume_one(w_thread thread) {
  if (thread->flags & WT_THREAD_SUSPEND_COUNT_MASK) {
    thread->flags -= 1 << WT_THREAD_SUSPEND_COUNT_SHIFT;
    woempa(7, "JDWP: resuming %t, count now %d\n", thread, (thread->flags & WT_THREAD_SUSPEND_COUNT_MASK) >> WT_THREAD_SUSPEND_COUNT_SHIFT);
  }
  else {
    woempa(7, "JDWP: not resuming %t, count is %d\n", thread, (thread->flags & WT_THREAD_SUSPEND_COUNT_MASK) >> WT_THREAD_SUSPEND_COUNT_SHIFT);
  }
}

w_size jdwp_global_suspend_count;

void jdwp_internal_suspend_all(void) {
  x_status status;

  if (jdwp_global_suspend_count++) {
    woempa(7, "JDWP: already suspended, now count = %d\n", jdwp_global_suspend_count);

    return;
  }

  woempa(7, "JDWP: start locking other threads\n");
  if (number_unsafe_threads < 0) {
    wabort(ABORT_WONKA, "number_unsafe_threads = %d!", number_unsafe_threads);
  }
  x_monitor_eternal(safe_points_monitor);
  while(isSet(blocking_all_threads, BLOCKED_BY_GC)) {
    woempa(7, "GC is blocking all threads, not possible to suspend VM yet.\n");
    status = x_monitor_wait(safe_points_monitor, GC_STATUS_WAIT_TICKS);
    if (status == xs_interrupted) {
      x_monitor_eternal(safe_points_monitor);
    }
  }
  woempa(2, "JDWP: setting blocking_all_threads to BLOCKED_BY_JDWP\n");
  blocking_all_threads = BLOCKED_BY_JDWP;

  x_monitor_notify_all(safe_points_monitor);
  x_monitor_exit(safe_points_monitor);
  woempa(7, "JDWP: finished locking other threads\n");
}

void jdwp_internal_resume_all(void) {
  if (--jdwp_global_suspend_count > 0) {
    woempa(7, "JDWP: still suspended, now count = %d\n", jdwp_global_suspend_count);
  }
  else {
    woempa(7, "JDWP: count = %d, start unlocking other threads\n", jdwp_global_suspend_count);
    x_monitor_eternal(safe_points_monitor);
    blocking_all_threads = 0;
    x_monitor_notify_all(safe_points_monitor);
    x_monitor_exit(safe_points_monitor);
    woempa(7, "JDWP: finished unlocking other threads\n");
  }
}


