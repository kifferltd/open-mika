/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2004 by Chris Gray, /k/ Embedded Java               *
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

#include "oswald.h"  /* x_thread_... */
#include "wonka.h"   /* the w_ types */
#include "jdwp.h"
#include "jdwp-debug.h"

char *jdwp_event_kind_str(w_int eventKind) {
  switch(eventKind) {
    case jdwp_evt_vm_disconnected:
      return "VM disconnected";

    case jdwp_evt_single_step:
      return "Single step";

    case jdwp_evt_breakpoint:
      return "Breakpoint";

    case jdwp_evt_frame_pop:
      return "Frame pop";

    case jdwp_evt_exception:
      return "Exception";

    case jdwp_evt_user_defined:
      return "User defined";

    case jdwp_evt_thread_start:
      return "Thread start";

    case jdwp_evt_thread_end:
      return "Thread end";

    case jdwp_evt_class_prepare:
      return "Class prepare";

    case jdwp_evt_class_unload:
      return "Class unload";

    case jdwp_evt_class_load:
      return "Class load";

    case jdwp_evt_field_access:
      return "Field access";

    case jdwp_evt_field_modification:
      return "Field modification";

    case jdwp_evt_exception_catch:
      return "Exception catch";

    case jdwp_evt_method_entry:
      return "Method entry";

    case jdwp_evt_method_exit:
      return "Method exit";

    case jdwp_evt_vm_init:
      return "VM init";

    case jdwp_evt_vm_death:
      return "VM death";

    default:
      return "unknown";
  }
}

char *jdwp_suspend_policy_str(w_int suspendPolicy) {
  switch(suspendPolicy) {
    case jdwp_sp_none:
      return "Suspend no threads";
    case jdwp_sp_event_thread: 
      return "Suspend the event thread";
    case jdwp_sp_all:
      return "Suspend all threads";
    default:
      return "unknown";
  }
}
  
char *jdwp_mod_kind_str(w_int mod_kind) {
  switch(mod_kind) {
    case  1:
      return "Count";
    case  2:
      return "Conditional";
    case  3:
      return "ThreadOnly";
    case  4:
      return "ClassOnly";
    case  5:
      return "ClassMatch";
    case  6:
      return "ClassExclude";
    case  7:
      return "LocationOnly";
    case  8:
      return "ExceptionOnly";
    case  9:
      return "FieldOnly";
    case 10:
      return "Step";
    case 11:
      return "InstanceOnly";
    default:
      return "unknown";
  }
}

