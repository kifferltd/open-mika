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

