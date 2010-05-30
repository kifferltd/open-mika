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
      if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
        w_printf("JDWP: loader = %j\n", loader);
      }

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

