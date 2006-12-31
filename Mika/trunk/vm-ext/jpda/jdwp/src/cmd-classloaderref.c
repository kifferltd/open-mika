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

#include "clazz.h"
#include "jdwp.h"
#include "jdwp-protocol.h"
#include "loading.h"
#include "wonka.h"

void add_class_to_reply(w_word key, w_word value, void *pointer1, void *pointer2) {
  w_clazz clazz = (w_clazz)value;

  jdwp_put_u1(&reply_grobag, clazz->dims ? jdwp_tt_array : clazz->vmlt ? jdwp_tt_class : jdwp_tt_interface);
  jdwp_put_clazz(&reply_grobag, clazz);
}

/*
** Returns all reference types which have the given class loader as *initiating* class loader.
*/

w_void jdwp_classloaderref_visible_classes(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_instance loader;
  w_hashtable ht;

  loader = jdwp_get_objectref(cmd->data, &offset);
  if (loader) {
    if (isSet(instance2clazz(loader)->flags, CLAZZ_IS_CLASSLOADER)) {

      ht = loader2loaded_classes(loader);
      if (ht) { 
        jdwp_put_u4(&reply_grobag, ht->occupancy);
        ht_iterate(ht, add_class_to_reply, NULL, NULL);
      }
      else {
        jdwp_put_u4(&reply_grobag, 0);
      }

      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_class_loader(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}

w_void dispatch_classloaderref(jdwp_command_packet cmd) {

  switch((jdwp_classloaderref_cmd)cmd->command) {
    case jdwp_classloaderref_visibleClasses:
      jdwp_classloaderref_visible_classes(cmd);
      break;
    default:
      jdwp_send_not_implemented(cmd->id);
  }
  
}

