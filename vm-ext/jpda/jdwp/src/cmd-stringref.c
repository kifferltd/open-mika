/**************************************************************************
* Copyright (c) 2004, 2006, 2016, 2023 by Chris Gray, KIFFER Ltd.         *
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
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,            *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,       *
* EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                      *
**************************************************************************/

#include "jdwp.h"
#include "jdwp-protocol.h"
#include "oswald.h"  /* x_thread_... */
#include "wonka.h"   /* the w_ types */
#include "wstrings.h"

extern w_clazz clazzString;

#ifdef DEBUG
static const char* string_reference_command_names[] = {
  /*  0 */ NULL,
  /*  1 */ "Value",
};

#define STRING_REFERENCE_MAX_COMMAND 12

#endif

/*
** Get the value of a String instance
*/

w_void jdwp_string_value(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_instance instance;
  w_string string;
  w_ubyte *utf8;
  w_int length;

  instance = jdwp_get_objectref(cmd->data, &offset);
  woempa(7, "%j\n", instance);

  if (instance) {
    if (instance2clazz(instance) == clazzString) {
      string = String2string(instance);
      utf8 = w_string2UTF8(string, &length);
      jdwp_put_cstring(&reply_grobag, (char*)utf8, length);
      releaseMem(utf8);
      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_string(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}


/*
** The dispatcher for the 'String reference' command set.
*/

w_void dispatch_strref(jdwp_command_packet cmd) {

  woempa(7, "String Reference Command = %s\n", cmd->command > 0 && cmd->command <= STRING_REFERENCE_MAX_COMMAND ? string_reference_command_names[cmd->command] : "unknown");
  switch((jdwp_strref_cmd)cmd->command) {
    case jdwp_strref_value:   jdwp_string_value(cmd);   break;
    default:
      jdwp_send_not_implemented(cmd->id);
  }

}

