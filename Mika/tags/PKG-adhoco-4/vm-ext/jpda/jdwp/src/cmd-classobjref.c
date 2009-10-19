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

#include "clazz.h"
#include "jdwp.h"
#include "jdwp-protocol.h"
#include "wonka.h"

#ifdef DEBUG
static const char* class_reference_command_names[] = {
  /*  0 */ NULL,
  /*  1 */ "ReflectedType",
};

#define CLASS_REFERENCE_MAX_COMMAND 1
#endif

extern w_clazz clazzClass;

/*
** Returns the reference type associated with a given class object.
*/

w_void jdwp_classobjref_refltype(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_instance instance;
  w_clazz clazz;

  instance = jdwp_get_objectref(cmd->data, &offset);
  if (instance) {
    if (instance2clazz(instance) == clazzClass) {
      clazz = Class2clazz(instance);
      if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
        wprintf("JDWP: clazz = %k\n", clazz);
      }
      jdwp_put_u1(&reply_grobag, clazz->dims ? jdwp_tt_array : clazz->vmlt ? jdwp_tt_class : jdwp_tt_interface);
      jdwp_put_clazz(&reply_grobag, clazz);

      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_class(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}


w_void dispatch_classobjref(jdwp_command_packet cmd) {

  woempa(7, "ClassReference Command = %s\n", cmd->command > 0 && cmd->command <= CLASS_REFERENCE_MAX_COMMAND ? class_reference_command_names[cmd->command] : "unknown");
  switch((jdwp_classobjref_cmd)cmd->command) {
    case jdwp_classobjref_reflectedType:
      jdwp_classobjref_refltype(cmd);
      break;
    default:
      jdwp_send_not_implemented(cmd->id);
  }
}

