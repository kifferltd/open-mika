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

#include "core-classes.h"
#include "oswald.h"
#include "wonka.h"         /* the w_ types */
#include "jdwp.h"          /* all the jdwp stuff */
#include "jdwp-protocol.h"
#include "core-classes.h"  /* F_... stuff */
#include "arrays.h"        /* instance2arrayof */
#include "wstrings.h"      /* string2UTF8 */
#include <string.h>        /* strncmp */

#ifdef DEBUG
static const char* threadgroup_reference_command_names[] = {
  /*  0 */ NULL,
  /*  1 */ "Name",
  /*  2 */ "Parent",
  /*  3 */ "Children",
};

#define THREADGROUP_REFERENCE_MAX_COMMAND 3
#endif

/*
** threadgrp_name : Returns the name of the thread with given ID.
*/

w_void jdwp_threadgrp_name(jdwp_command_packet cmd) {
  w_instance threadgroup;
  w_size offset = 0;
  w_string name_string;
  
  threadgroup = jdwp_get_objectref(cmd->data, &offset);
  woempa(7, "%j\n", threadgroup);
  if (threadgroup) {
    if (isSuperClass(clazzThreadGroup, instance2clazz(threadgroup))) {

      name_string = String2string(getReferenceField(threadgroup, F_ThreadGroup_name));
      jdwp_put_string(&reply_grobag, name_string);
      woempa(7, "%j Name = %w\n", threadgroup, name_string);
  
      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_thread_group(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}


/*
** threadgrp_parent: Returns the parent of the threadgroup with given ID.
*/

w_void jdwp_threadgrp_parent(jdwp_command_packet cmd) {
  w_instance threadgroup;
  w_size offset = 0;
  w_instance parent;
  
  threadgroup = jdwp_get_objectref(cmd->data, &offset);
  woempa(7, "ThreadGroup = %j\n", threadgroup);
  if (threadgroup) {
    if (isSuperClass(clazzThreadGroup, instance2clazz(threadgroup))) {
      parent = getReferenceField(threadgroup, F_ThreadGroup_parent);

      woempa(7, "Parent = %j\n", parent);
      jdwp_put_objectref(&reply_grobag, parent);
  
      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_thread_group(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}

/*
** threadgrp_children: Returns the direct children of this threadgroup. 'Children' are the 
**                     different threads and threadgroups that have this threadgroup as their
**                     parent. Threads/threadgroups in child threadgroups are not reported.
*/

w_void jdwp_threadgrp_children(jdwp_command_packet cmd) {
  w_instance  threadgroup;
  w_size offset = 0;
  w_instance  threads_vector;
  w_instance  groups_vector;
  w_instance  threads_array;
  w_instance  groups_array;
  w_instance  *threads_data = NULL;
  w_instance  *groups_data = NULL;
  w_int       threads_count = 0;
  w_int       groups_count = 0;
  w_int       i;
  
  threadgroup = jdwp_get_objectref(cmd->data, &offset);
  woempa(7, "ThreadGroup = %j\n", threadgroup);
  if (threadgroup) {
    if (isSuperClass(clazzThreadGroup, instance2clazz(threadgroup))) {
      threads_vector = getReferenceField(threadgroup, F_ThreadGroup_flock);
      groups_vector =  getReferenceField(threadgroup, F_ThreadGroup_children);

      /*
      ** Get the number of elements in the 2 vectors.
      */

      if(threads_vector) {
        threads_count = getIntegerField(threads_vector, F_Vector_elementCount);
        threads_array = getReferenceField(threads_vector, F_Vector_elementData);
        threads_data = instance2Array_instance(threads_array);
      } 

      if(groups_vector) {
        groups_count =  getIntegerField(groups_vector, F_Vector_elementCount);
        groups_array = getReferenceField(groups_vector, F_Vector_elementData);
        groups_data = instance2Array_instance(groups_array);
      }

      // childThreads
      woempa(7, "Child threads: %d\n", threads_count);
      // HACK - if this is the system thread group, ignore the JDWP thread
      jdwp_put_u4(&reply_grobag, (w_word)(threads_count - (threadgroup == I_ThreadGroup_system)));
      for(i = 0; i < threads_count; i++) {
        if (getWotsitField(threads_data[i], F_Thread_wotsit) != jdwp_thread) {
          woempa(7, "  %j\n", threads_data[i]);
          jdwp_put_objectref(&reply_grobag, threads_data[i]);
        }
        else {
          woempa(7, "  skipping %j\n", threads_data[i]);
        }
      }

      // childGroups
      woempa(7, "Child groups: %d\n", groups_count);
      jdwp_put_u4(&reply_grobag, (w_word)groups_count);
      for(i = 0; i < groups_count; i++) {
        woempa(7, "  %j\n", groups_data[i]);
        jdwp_put_objectref(&reply_grobag, groups_data[i]);
      }

      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_thread_group(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}


/*
** The dispatcher for the 'ThreadGroup reference' command set.
*/

w_void dispatch_threadgrpref(jdwp_command_packet cmd) {

  woempa(7, "ThreadGroupReference Command = %s\n", cmd->command > 0 && cmd->command <= THREADGROUP_REFERENCE_MAX_COMMAND ? threadgroup_reference_command_names[cmd->command] : "unknown");
  switch((jdwp_threadgrpref_cmd)cmd->command) {
    case jdwp_threadgrpref_name:
      jdwp_threadgrp_name(cmd);
      break;
    case jdwp_threadgrpref_children:
      jdwp_threadgrp_children(cmd);
      break;
    case jdwp_threadgrpref_parent:
      jdwp_threadgrp_parent(cmd);
      break;
    default:
      jdwp_send_not_implemented(cmd->id);
  }
  
}

