/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2004, 2006 by Chris Gray, /k/ Embedded Java         *
* Solutions. All rights reserved.                                         *
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

#include "oswald.h"  /* x_thread_... */
#include "wonka.h"   /* the w_ types */
#include "jdwp.h"
#include "jdwp_events.h"
#include "jdwp-debug.h"
#include "jdwp-protocol.h"

#ifdef DEBUG
static const char* evtreq_command_names[] = {
  /*  0 */ NULL,
  /*  1 */ "Set",
  /*  2 */ "Clear",
  /*  3 */ "ClearAllBreakpoints",
};

#define EVTREQ_MAX_COMMAND 3

#endif

extern w_thread jdwp_check_thread_reference(w_instance);

/*
** Set : Process the event request sent by the debugger. When events occur and they match
**       one of the previous received event request, an event will be send to the debugger.
*/

w_void jdwp_evtreq_set(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_ubyte event_kind = jdwp_get_u1(cmd->data, &offset);
  w_ubyte suspend_policy = jdwp_get_u1(cmd->data, &offset);
  w_int modifiers = jdwp_get_u4(cmd->data, &offset);
  w_instance instance;
  w_byte mod_kind;
  w_int i;
  w_int error_code = 0;
  jdwp_event event;
  jdwp_event_modifier mod;

  event = jdwp_event_alloc(event_kind, suspend_policy);
  
  woempa(7, "  eventKind:     %4d (%s)\n", 
          event->event_kind, jdwp_event_kind_str(event->event_kind));
  woempa(7, "  suspendPolicy: %4d (%s)\n", 
          event->suspend_policy, jdwp_suspend_policy_str(event->suspend_policy));
  woempa(7, "  modifiers (%d):\n", modifiers);
  /*
  ** Next we go over all the modifiers in the block and store them in the event.
  */
  
  for(i = 0; i < modifiers; i++) {
    
    mod_kind = jdwp_get_u1(cmd->data, &offset); /* Get the kind of modifier */

    mod = allocClearedMem(sizeof(jdwp_Event_Modifier));
    mod->mod_kind = mod_kind;
    
    woempa(7, "    %2d %s\n", mod_kind, jdwp_mod_kind_str(mod_kind));

    switch(mod_kind) {
      
      /* 
      ** Count : Suppress the first (count - 1) occurrences of the event. Each occurrence will 
      **         decrement count. When it reaches 0, the other modifiers are checked to see if 
      **         the event should be blocked. If none of the modifiers suppress the event, it will 
      **         be reported. This modifier works on all event kinds.
      **
      **         Once count has reached 0, no further events are 
      **         reported. This means that a count of 1 will report 
      **         the next occurrence and then completely ignore further occurrences.
      */

      case  1: 
        mod->condition.count = jdwp_get_u4(cmd->data, &offset);
        woempa(7, "       -> %d\n", mod->condition.count);
        break;
               
      /* 
      ** Conditional : This modifier is not used but reserved for future additions.
      */
      
      case  2:
        mod->condition.exprID = jdwp_get_u4(cmd->data, &offset);
        break;
               
      /* 
      ** Thread only : Only report the event if it happened in the given thread. This works on all 
      **               event kinds except on the 'Class unload' event.
      */
      
      case  3: 
        instance = jdwp_get_objectref(cmd->data, &offset);
        if (instance) {
          if (jdwp_check_thread_reference(instance)) {
            mod->condition.threadID = instance;
          }
          else {
            error_code = jdwp_err_invalid_thread;
          }
        } 
        else {
          error_code = jdwp_err_invalid_object;
        }
        woempa(7, "       -> %j\n", mod->condition.threadID);
        break;
               
      /* 
      ** Class only : Restrict the 'Class Prepare' events to classes of the the given reference type 
      **              or subtype. For other events, restrict the events to classes whose location is 
      **              in the given reference type of subtype. If the class can be safely casted to 
      **              the given reference type, then an event will be passed on. This modifier 
      **              doesnt't work on 'Class unload', 'Thread start' and 'Thread end' events.
      */
      
      case  4: 
        mod->condition.clazz = jdwp_get_clazz(cmd->data, &offset);
        woempa(7, "       -> %k\n", mod->condition.clazz);
        break;
               
      /* 
      ** Class match : Block events from classes whose name doesn't match the given expression. 
      **               For 'Class load' and 'Class unload' events, the class name is checked. For
      **               all the other events, the name of the class where the event occurred is 
      **               checked. This modifier can not be used with 'Thread start' and 'Thread end' 
      **               events.
      **               The expression is limited to exact matches and patterns that have an '*' at
      **               the beginning or the end. e.g. 'java.awt.Button', '*.Button' & 'java.awt.*'
      */

      case  5: 
        mod->condition.match_pattern = jdwp_get_string(cmd->data, &offset);

        woempa(7, "       -> %w\n", mod->condition.match_pattern);
     
        break;
               
      /* 
      ** Class exclude : This modifier has the opposite effects as the 'Class match' modifier. When
      **                 a certain class name is blocked with 'Class match', it will be reported with
      **                 'Class exclude'. When a certain class name gets through with 'Class match', 
      **                 it will be blocked by 'Class exclude'. The same rules for the patterns and
      **                 events on which it can be used applies here aswell.
      */
      
      case  6:
        mod->condition.exclude_pattern = jdwp_get_string(cmd->data, &offset);
        woempa(7, "       -> %w\n", mod->condition.exclude_pattern);
     
        break;

      /* 
      ** Location only : Only report events if they occurred at the given location. This modifier
      **                 only works with 'Breakpoint', 'Field access', 'Field modification', 'Step'
      **                 and exception events.
      */

      case  7: 
        jdwp_get_location_here(cmd->data, &offset, &mod->condition.location);
        woempa(7, "       -> method = %M, pc = %d\n", mod->condition.location.method, mod->condition.location.pc);
        break;
               
      /* 
      ** Exception only : This modifier works on 'Exception' events only and restricts exception to
      **                  the given type (or all types if the given type is null) and if they are 
      **                  caught or uncaught (depending on the 2 booleans that are given).
      **
      **                  NOTE: Sometimes it's not possible to determine if an exception is caught
      **                        or not at the time it's thrown...
      */

      case  8:
        mod->condition.ex.exception = jdwp_get_clazz(cmd->data, &offset);
        woempa(7, "          exception: %k\n", mod->condition.ex.exception);
        mod->condition.ex.caught = jdwp_get_u1(cmd->data, &offset);
        woempa(7, "          caught:    %d\n", mod->condition.ex.caught);
        mod->condition.ex.uncaught = jdwp_get_u1(cmd->data, &offset);
        woempa(7, "          uncaught:  %d\n", mod->condition.ex.uncaught);
        break;
               
      /* 
      ** Field only : This modifier can only be used with 'Field access' and 'Field modification'
      **              events. Only events that occurred for a given field are reported.
      */
      
      case  9:
        mod->condition.field.declaring = jdwp_get_clazz(cmd->data, &offset);
        mod->condition.field.ID = jdwp_get_field(cmd->data, &offset);
        woempa(7, "          field: %v\n", mod->condition.field.ID);
        break;
               
      /* 
      ** Step : This modifier can only be used with 'Step' events. Only events that match the given
      **        depth and size are reported.
      */
     
      case 10:
        instance = jdwp_get_objectref(cmd->data, &offset);
        if (instance) {
          w_thread thread = jdwp_check_thread_reference(instance);
          if (thread) {
            mod->condition.step.thread = thread;
            mod->condition.step.size = jdwp_get_u4(cmd->data, &offset);
            mod->condition.step.depth = jdwp_get_u4(cmd->data, &offset);
          }
          else {
            error_code = jdwp_err_invalid_thread;
          }
        } 
        else {
          error_code = jdwp_err_invalid_object;
        }
        woempa(7, "          step: %t size %d depth %d\n", mod->condition.step.thread, mod->condition.step.size, mod->condition.step.depth);
        break;
               
      /* 
      ** Instance only : Only report events in the given instance. This doens't work with 'Class 
      **                 prepare', 'Class unload', 'Thread start' and 'Thread end' events.
      **                 (introduced in JDWP 1.4)
      */

      case 11:
        instance = jdwp_get_objectref(cmd->data, &offset);
        if (instance) {
          mod->condition.instance = jdwp_get_objectref(cmd->data, &offset);
        } 
        else {
          error_code = jdwp_err_invalid_object;
        }
        break;
               
      /* 
      ** What's this ?  We don't know this kind of modifier...
      ** Let the debugger know about this little incident.
      */
      
      default:
        // Hm, actually it's the mod kind which is invalid ...
        error_code = jdwp_err_invalid_event_type;
    }
woempa(7, "error code = %d\n", error_code);

    if (error_code) {
      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
      // TODO: clean up modifiers etc.

      return;
    }

    /*
    ** Add the modifier to the event.
    */

    jdwp_event_add_modifier(event, mod);
  }

  /*
  ** If the event is a breakpoint or a step, we need to do some more stuff.
  */

  if(event->event_kind == jdwp_evt_breakpoint) {
    jdwp_breakpoint_set(event);
  }
  else if(event->event_kind == jdwp_evt_single_step) {
    jdwp_single_step_set(event);
  }

  /*
  ** When we're done parsing, add this event to the list.
  */  

  jdwp_event_add(event);

  /*
  ** Let the debugger know that everything worked out fine.
  */

  jdwp_put_u4(&reply_grobag, event->eventID);

  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}


/*
** Clear : Clear an event request. Events that match the event in question will no longer be send
**         to the debugger.
*/

w_void jdwp_evtreq_clear(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_ubyte event_kind = jdwp_get_u1(cmd->data, &offset);
  w_int eventID = jdwp_get_u4(cmd->data, &offset);
  jdwp_event          event;
  jdwp_event_modifier mod;
  jdwp_event_modifier tmp;

  woempa(7, "  eventKind: %4d (%s)\n", event_kind, jdwp_event_kind_str(event_kind));
  woempa(7, "  eventID:   %4d\n", eventID); 

  event = jdwp_event_get_ID(eventID);

  if(event) {

    if(event->event_kind == jdwp_evt_breakpoint) {
      jdwp_breakpoint_clear(event);
    }
    else if(event->event_kind == jdwp_evt_single_step) {
      jdwp_single_step_clear(event);
    }

    /*
    ** Clear the modifiers and release memory.
    */

    mod = event->modifiers;
    if(mod != NULL) mod->prev->next = NULL;

    while(mod != NULL) {
      tmp = mod;
      mod = mod->next;

      /* TODO: Some modifiers have extra allocated memory. Deal with it. */
      
      releaseMem(tmp);
    }

    /* 
    ** Clear the event and release memory.
    */

    jdwp_event_remove(event->eventID);

    releaseMem(event);

  }
  
  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}
    
/*
** Clear breakpoints : Clear all breakpoint event requests. All the breakpoints will be deleted and
**                     no more breakpoint events will be send.
*/

w_void jdwp_evtreq_clear_all(jdwp_command_packet cmd) {

  woempa(9, "Event Request - Clear all breakpoints\n");

  /*
  ** TODO: Everything....
  */
  
  //jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_not_implemented);
  jdwp_send_reply(cmd->id, NULL, jdwp_err_none);
}

/*
** The dispatcher for the 'Event request' command set.
*/

w_void dispatch_eventreq(jdwp_command_packet cmd) {

  woempa(7, "Event Request Command = %s\n", cmd->command > 0 && cmd->command <= EVTREQ_MAX_COMMAND ? evtreq_command_names[cmd->command] : "unknown");
  switch((jdwp_eventreq_cmd)cmd->command) {
    case jdwp_eventreq_set:
      jdwp_evtreq_set(cmd);
      break;
    case jdwp_eventreq_clear:
      jdwp_evtreq_clear(cmd);
      break;
    case jdwp_eventreq_clearAllBreakpoints:
      jdwp_evtreq_clear_all(cmd);
      break;
    default:  
      jdwp_send_not_implemented(cmd->id);
  }
}


