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

