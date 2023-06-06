/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
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

#include <string.h>

#include "checks.h" 
#include "clazz.h" 
#include "core-classes.h"
#include "descriptor.h" 
#include "jdwp.h"
#include "jdwp-protocol.h"
#include "methods.h" 
#include "oswald.h"
#include "mika_threads.h"
#include "wonka.h"

#ifdef DEBUG
static const char* stack_frame_command_names[] = {
  /*  0 */ NULL,
  /*  1 */ "GetValues",
  /*  2 */ "SetValues",
  /*  3 */ "ThisObject",
  /*  4 */ "PopFrames",
};

#define STACK_FRAME_MAX_COMMAND 4
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

        switch(tag) {
          case 'V':
            jdwp_put_u1(&reply_grobag, tag);
            break;
          case 'B':
          case 'Z':
            jdwp_put_u1(&reply_grobag, tag);
            jdwp_put_u1(&reply_grobag, (w_ubyte)GET_SLOT_CONTENTS(frame->jstack_base + slot));
            break;  
          case 'C':
          case 'S':
            jdwp_put_u1(&reply_grobag, tag);
            jdwp_put_u2(&reply_grobag, (w_ushort)GET_SLOT_CONTENTS(frame->jstack_base + slot));
            break;
          case 'F':
          case 'I':
            jdwp_put_u1(&reply_grobag, tag);
            jdwp_put_u4(&reply_grobag, GET_SLOT_CONTENTS(frame->jstack_base + slot));
            break;
          case 'J':
          case 'D':
            jdwp_put_u1(&reply_grobag, tag);
            jdwp_put_u4(&reply_grobag, GET_SLOT_CONTENTS(frame->jstack_base + slot + WORD_MSW));
            jdwp_put_u4(&reply_grobag, GET_SLOT_CONTENTS(frame->jstack_base + slot + WORD_LSW));
            break;
          default:
            jdwp_put_u1(&reply_grobag, clazz2sigbyte(instance2clazz((w_instance)GET_SLOT_CONTENTS(frame->jstack_base + slot))));
            jdwp_put_objectref(&reply_grobag, (w_instance)GET_SLOT_CONTENTS(frame->jstack_base + slot));
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
        tag = jdwp_get_u1(cmd->data, &offset);

        switch(tag) {
          case 'V':
            break;
          case 'B':
          case 'Z':
            SET_SLOT_CONTENTS(frame->jstack_base + slot, jdwp_get_u1(cmd->data, &offset));
            break;  
          case 'C':
          case 'S':
            SET_SLOT_CONTENTS(frame->jstack_base + slot, jdwp_get_u2(cmd->data, &offset));
            break;
          case 'F':
          case 'I':
            SET_SLOT_CONTENTS(frame->jstack_base + slot, jdwp_get_u4(cmd->data, &offset));
            break;
          case 'J':
          case 'D':
            SET_SLOT_CONTENTS(frame->jstack_base + slot + WORD_MSW, jdwp_get_u4(cmd->data, &offset));
            SET_SLOT_CONTENTS(frame->jstack_base + slot + WORD_LSW, jdwp_get_u4(cmd->data, &offset));
            break;
        default:
          SET_SLOT_CONTENTS(frame->jstack_base + slot, (w_word)jdwp_get_objectref(cmd->data, &offset));
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
        instance = (w_instance)GET_SLOT_CONTENTS(frame->jstack_base);
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

  woempa(7, "StackReference Command = %s\n", cmd->command > 0 && cmd->command <= STACK_FRAME_MAX_COMMAND ? stack_frame_command_names[cmd->command] : "unknown");
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

