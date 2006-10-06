/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004, 2006 by Chris Gray, /k/ Embedded Java *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

#include <string.h>

#include "checks.h" 
#include "clazz.h" 
#include "core-classes.h"
#include "descriptor.h" 
#include "jdwp.h"
#include "jdwp-protocol.h"
#include "methods.h" 
#include "oswald.h"
#include "threads.h"
#include "wonka.h"

#ifdef DEBUG
static const char* stack_reference_command_names[] = {
  /*  0 */ NULL,
  /*  1 */ "GetValues",
  /*  2 */ "SetValues",
  /*  3 */ "ThisObject",
  /*  4 */ "PopFrames",
};

#define STACK_REFERENCE_MAX_COMMAND 4
#endif

extern w_thread jdwp_check_thread_reference(w_instance);

/*
** Get the values of local variables in a given stackframe. 
*/

w_void jdwp_stack_get_values(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_instance instance;
  w_frame frame;
  w_int slots;
  w_int slot;
  w_int i;
  w_ubyte tag;

  instance = jdwp_get_objectref(cmd->data, &offset);
  if (instance) {
    if (jdwp_check_thread_reference(instance)) {
      frame = jdwp_get_frame(cmd->data, &offset);
      slots = jdwp_get_u4(cmd->data, &offset);
  
      jdwp_put_u4(&reply_grobag, slots);
      for(i = 0; i < slots; i++) {
        slot = jdwp_get_u4(cmd->data, &offset);
        tag = jdwp_get_u1(cmd->data, &offset);
        jdwp_put_u1(&reply_grobag, tag);

        switch(tag) {
          case 'V':
            break;
          case 'B':
          case 'Z':
            jdwp_put_u1(&reply_grobag, (w_ubyte)frame->jstack_base[slot].c);
            break;  
          case 'C':
          case 'S':
           jdwp_put_u2(&reply_grobag, (w_ushort)frame->jstack_base[slot].c);
            break;
          case 'F':
          case 'I':
            jdwp_put_u4(&reply_grobag, frame->jstack_base[slot].c);
            break;
          case 'J':
          case 'D':
            jdwp_put_u4(&reply_grobag, frame->jstack_base[slot + WORD_MSW].c);
            jdwp_put_u4(&reply_grobag, frame->jstack_base[slot + WORD_LSW].c);
            break;
          default:
           jdwp_put_objectref(&reply_grobag, (w_instance)frame->jstack_base[slot].c);
        }
      }

      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_thread(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}


/*
** Set the values of local variables in a given stackframe. 
*/

w_void jdwp_stack_set_values(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_instance instance;
  w_frame frame;
  w_int slots;
  w_int slot;
  w_int i;
  w_ubyte tag;

  instance = jdwp_get_objectref(cmd->data, &offset);
  if (instance) {
    if (jdwp_check_thread_reference(instance)) {
      frame = jdwp_get_frame(cmd->data, &offset);
      slots = jdwp_get_u4(cmd->data, &offset);
  
      for(i = 0; i < slots; i++) {
        slot = jdwp_get_u4(cmd->data, &offset);
        tag = jdwp_get_u4(cmd->data, &offset);

        switch(tag) {
          case 'V':
            break;
          case 'B':
          case 'Z':
            frame->jstack_base[slot].c = jdwp_get_u1(cmd->data, &offset);
            break;  
          case 'C':
          case 'S':
            frame->jstack_base[slot].c = jdwp_get_u2(cmd->data, &offset);
            break;
          case 'F':
          case 'I':
            frame->jstack_base[slot].c = jdwp_get_u4(cmd->data, &offset);
            break;
          case 'J':
          case 'D':
            frame->jstack_base[slot + WORD_MSW].c = jdwp_get_u4(cmd->data, &offset);
            frame->jstack_base[slot + WORD_LSW].c = jdwp_get_u4(cmd->data, &offset);
            break;
        default:
          frame->jstack_base[slot].c = (w_word)jdwp_get_objectref(cmd->data, &offset);
        }
      }

      jdwp_send_reply(cmd->id, NULL, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_thread(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}

/*
** Get the 'this' reference for a given stackframe. If the frame's method is native or static, 
** the reply will contain the null reference.
*/

w_void jdwp_stack_this(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_thread thread;
  w_frame frame;
  w_instance instance;
  w_clazz clazz;
  w_ubyte tag;
  
  instance = jdwp_get_objectref(cmd->data, &offset);
  if (instance) {
    frame = jdwp_get_frame(cmd->data, &offset);
    thread = jdwp_check_thread_reference(instance);
  
    if (thread) {
      if (!isSet(frame->flags, FRAME_NATIVE) && !isSet(frame->method->flags, ACC_STATIC)) {
        instance = (w_instance)frame->jstack_base[0].c;
        clazz = instance2clazz(instance);
        if(clazz->dims) {
          tag = jdwp_tag_array;
        }
        else {
          if(isAssignmentCompatible(clazz, clazzThread)) {
            tag = jdwp_tag_thread;
          }
          else if(isAssignmentCompatible(clazz, clazzThreadGroup)) {
            tag = jdwp_tag_thread_group;
          }
          else if(isAssignmentCompatible(clazz, clazzClassLoader)) {
            tag = jdwp_tag_class_loader;
          }
          else if(isAssignmentCompatible(clazz, clazzString)) {
            tag = jdwp_tag_string;
          }
          else {
            tag = jdwp_tag_object;
          }
        }
        jdwp_put_u1(&reply_grobag, tag);
        jdwp_put_objectref(&reply_grobag, instance);
      }
      else {
        jdwp_put_u1(&reply_grobag, jdwp_tag_object);
        jdwp_put_objectref(&reply_grobag, NULL);
      }

      /*
      ** Send the reply to the debugger.
      */

      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    } else {
      jdwp_send_invalid_thread(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}

/*
** The dispatcher for the 'Stack frame' command set.
*/

w_void dispatch_stack(jdwp_command_packet cmd) {

  woempa(7, "StackReference Command = %s\n", cmd->command > 0 && cmd->command <= STACK_REFERENCE_MAX_COMMAND ? stack_reference_command_names[cmd->command] : "unknown");
  switch((jdwp_stack_cmd)cmd->command) {
    case jdwp_stack_getValues:
      jdwp_stack_get_values(cmd);
      break;
    case jdwp_stack_setValues:
      jdwp_stack_set_values(cmd);
      break;
    case jdwp_stack_thisObject:
      jdwp_stack_this(cmd);
      break;
    case jdwp_stack_popFrames:
    default:
      jdwp_send_not_implemented(cmd->id);
  }

}

