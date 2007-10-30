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

#include "oswald.h"
#include "wonka.h"
#include "jdwp.h"
#include "jdwp-protocol.h"
#include "clazz.h"

#ifdef DEBUG
static const char* class_type_command_names[] = {
  /*  0 */ NULL,
  /*  1 */ "Superclass",
  /*  2 */ "SetValues",
  /*  3 */ "InvokeMethod",
  /*  4 */ "NewInstance",
};

#define CLASS_TYPE_MAX_COMMAND 4

#endif

/*
** Get the superclass of the given class.
*/

w_void jdwp_class_superclass(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_clazz clazz;
  w_clazz super;

  clazz = jdwp_get_clazz(cmd->data, &offset);
  if (jdwp_check_clazz(clazz) & isNotSet(clazz->flags, ACC_INTERFACE)) {
    super = getSuper(clazz);
    woempa(7, "class = %k, super = %k\n", clazz, super);
    jdwp_put_clazz(&reply_grobag, super);
    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}

/*
** Set static values of the given class.
*/

w_void jdwp_class_set_values(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_clazz clazz;
  w_field field;
  w_int count;
  w_int i;
  w_ubyte sigbyte;

  clazz = jdwp_get_clazz(cmd->data, &offset);
  if (jdwp_check_clazz(clazz) & isNotSet(clazz->flags, ACC_INTERFACE)) {
    count = jdwp_get_u4(cmd->data, &offset);
    for (i = 0; i < count; ++i) {
      field = jdwp_get_field(cmd->data, &offset);
      if (jdwp_check_field(field)) {
        sigbyte = clazz2sigbyte(clazz);
        woempa(9, "  %v: sigbyte = '%c'\n", field, sigbyte);
        switch (sigbyte) {
          case jdwp_tag_boolean:
            field->declaring_clazz->staticFields[field->size_and_slot] = jdwp_get_u1(cmd->data, &offset);
            woempa(7, "  boolean: %d\n", field->declaring_clazz->staticFields[field->size_and_slot]);
            break;
          case jdwp_tag_char:
            field->declaring_clazz->staticFields[field->size_and_slot] = jdwp_get_u2(cmd->data, &offset);
            woempa(7, "  char: %d\n", field->declaring_clazz->staticFields[field->size_and_slot]);
            break;
          case jdwp_tag_float:
            field->declaring_clazz->staticFields[field->size_and_slot] = jdwp_get_u4(cmd->data, &offset);
            woempa(7, "  float: 0x%08x\n", field->declaring_clazz->staticFields[field->size_and_slot]);
            break;
          case jdwp_tag_double:
            field->declaring_clazz->staticFields[field->size_and_slot + WORD_MSW] = jdwp_get_u4(cmd->data, &offset);
            field->declaring_clazz->staticFields[field->size_and_slot + WORD_LSW] = jdwp_get_u4(cmd->data, &offset);
            woempa(7, "  double: 0x%08x%08x\n", field->declaring_clazz->staticFields[field->size_and_slot + WORD_MSW], field->declaring_clazz->staticFields[field->size_and_slot + WORD_LSW]);
            break;
          case jdwp_tag_byte:
            field->declaring_clazz->staticFields[field->size_and_slot] = jdwp_get_u1(cmd->data, &offset);
            woempa(7, "  byte: %d\n", field->declaring_clazz->staticFields[field->size_and_slot]);
            break;
          case jdwp_tag_short:
            field->declaring_clazz->staticFields[field->size_and_slot] = jdwp_get_u2(cmd->data, &offset);
            woempa(7, "  short: %d\n", field->declaring_clazz->staticFields[field->size_and_slot]);
            break;
          case jdwp_tag_int:
            field->declaring_clazz->staticFields[field->size_and_slot] = jdwp_get_u4(cmd->data, &offset);
            woempa(7, "  int: %d\n", field->declaring_clazz->staticFields[field->size_and_slot]);
            break;
          case jdwp_tag_long:
            field->declaring_clazz->staticFields[field->size_and_slot + WORD_MSW] = jdwp_get_u4(cmd->data, &offset);
            field->declaring_clazz->staticFields[field->size_and_slot + WORD_LSW] = jdwp_get_u4(cmd->data, &offset);
            woempa(7, "  long: 0x%08x%08x\n", field->declaring_clazz->staticFields[field->size_and_slot + WORD_MSW], field->declaring_clazz->staticFields[field->size_and_slot + WORD_LSW]);
            break;
          default:
            field->declaring_clazz->staticFields[field->size_and_slot] = jdwp_get_u4(cmd->data, &offset);
            woempa(7, "  reference: %j\n", field->declaring_clazz->staticFields[field->size_and_slot]);
        }
      }
      else {
        jdwp_send_invalid_fieldid(cmd->id);
      }
    }
    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}


/*
** Create an instance of the given class.

w_void jdwp_class_new_instance(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_clazz clazz;
  w_thread thread;
  w_method method;
  w_int count;
  w_instance instance;
  w_int i;

  clazz = jdwp_get_clazz(cmd->data, &offset);
  if (jdwp_check_clazz(clazz) & isNotSet(clazz->flags, ACC_INTERFACE)) {
    thread = jdwp_get_thread(cmd->data, &offset);
    if (jdwp_check_thread(thread)) {
      if (thread->WT_THREAD_SUSPEND_COUNT_MASK) {
        method = jdwp_get_method((cmd->data, &offset);
        if (jdwp_check_method(method)) {
          count = jdwp_get_u4(cmd->data, &offset);
          for (i = 0; i < count; ++i) {
          }
          // TODO: must be initialized
          instance = allocInstance(clazz);
    woempa(7, "class = %k, instance = %k\n", clazz, instance);
    jdwp_put_objectref(&reply_grobag, instance);
    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
        }
        else {
          jdwp_send_invalid_methodid(cmd->id);
        }
      }
      else {
        jdwp_send_thread_not_suspended(cmd->id);
      }
    }
    else {
      jdwp_send_invalid_thread(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}
*/

/*
** The dispatcher for the 'Class type' command set.
*/ 

w_void dispatch_classtype(jdwp_command_packet cmd) {

  woempa(7, "Class Type Command = %s\n", cmd->command > 0 && cmd->command <= CLASS_TYPE_MAX_COMMAND ? class_type_command_names[cmd->command] : "unknown");
  switch((jdwp_classtype_cmd)cmd->command) {
    case jdwp_classtype_superclass:
      jdwp_class_superclass(cmd);
      break;

    case jdwp_classtype_setValues:
      jdwp_class_set_values(cmd);
      break;

    case jdwp_classtype_newInstance:
      // FIXME jdwp_class_new_instance(cmd);
      // break;

// FIXME
    case jdwp_classtype_invokeMethod:
// FIXME
    default:
      jdwp_send_not_implemented(cmd->id);
  }
  
}

