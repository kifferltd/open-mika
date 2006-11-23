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
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

//#include "core-classes.h"
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
      utf8 = jdwp_string2UTF8(string, &length);
      jdwp_put_cstring(&reply_grobag, (char*)utf8 + 4, length);
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

