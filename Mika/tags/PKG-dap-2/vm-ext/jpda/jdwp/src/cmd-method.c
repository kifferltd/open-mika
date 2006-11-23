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
#include "constant.h" 
#include "jdwp.h"
#include "jdwp-protocol.h"
#include "methods.h"
#include "wstrings.h"

#ifdef DEBUG
static const char* method_command_names[] = {
  /*  0 */ NULL,
  /*  1 */ "LineTable",
  /*  2 */ "VariableTable",
  /*  3 */ "Bytecodes",
  /*  4 */ "isObsolete",
};

#define METHOD_MAX_COMMAND 4

#endif

/*
** Returns the line table of a given method in a given reference.
*/

w_void jdwp_method_lines(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_clazz clazz;
  w_method method;

  /*
  ** Get the clazz and method ID out of the packet.
  ** In our case, a methodID is enough because it's already quite unique and 
  ** the method holds a pointer to the class. The specification however says 
  ** that methodID's don't have to be unique if they are in different classes.
  */

  clazz = jdwp_get_clazz(cmd->data, &offset);
  woempa(7, "clazz = %p\n", clazz);

  if (jdwp_check_clazz(clazz)) {
    method = jdwp_get_method(cmd->data, &offset);
    woempa(7, "clazz = %k, method = %m\n", clazz, method);
    jdwp_put_u4(&reply_grobag, 0);                            // start (MSW)
    jdwp_put_u4(&reply_grobag, 0);                            // start (LSW)
    jdwp_put_u4(&reply_grobag, 0);                            // end   (MSW)
    jdwp_put_u4(&reply_grobag, method->exec.code_length - 1); // end   (LSW)
    woempa(7, "  Valid pc from 0 to %d\n", method->exec.code_length - 1);

    if (method->exec.debug_info && method->exec.debug_info->lineNums) {
      w_int i;
      w_int n = method->exec.debug_info->numLineNums;

      jdwp_put_u4(&reply_grobag, n);                          // lines
      woempa(7, "  %d lines\n", n);

      for (i = 0; i < n; i++) {
        woempa(7, "    lineCodeIndex %d = lineNumber %d\n", method->exec.debug_info->lineNums[i].start_pc, method->exec.debug_info->lineNums[i].line_number);

        jdwp_put_u4(&reply_grobag, 0);                        // lineCodeIndex (MSW)
                                                              // lineCodeIndex (LSW)
        jdwp_put_u4(&reply_grobag, method->exec.debug_info->lineNums[i].start_pc);
                                                              // lineNumber
        jdwp_put_u4(&reply_grobag, method->exec.debug_info->lineNums[i].line_number);
      }
    }
    else {
      woempa(7, "  no line number info available\n");
      jdwp_put_u4(&reply_grobag, 0);                        // lines
    }
    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}


/*
** Get the variable table for the given method. This table includes the arguments to
** the method as well as all the locals used during execution of the method. In case 
** the given class is an instance, 'this' is also included.
*/

w_void jdwp_method_var_table(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_clazz clazz;
  w_method method;

  clazz = jdwp_get_clazz(cmd->data, &offset);
  woempa(7, "clazz = %p\n", clazz);

  if (jdwp_check_clazz(clazz)) {
    method = jdwp_get_method(cmd->data, &offset);
    woempa(7, "clazz = %k, method = %m\n", clazz, method);
    jdwp_put_u4(&reply_grobag, method->exec.arg_i);   // argCnt
    woempa(7, "  argCnt = %d\n", method->exec.arg_i);

    if (method->exec.debug_info && method->exec.debug_info->localVars) {
      w_int i;
      w_int n = method->exec.debug_info->numLocalVars;

      jdwp_put_u4(&reply_grobag, n);                  // slots
      woempa(7, "  %d slots\n", n);

      for (i = 0; i < n; i++) {
        w_string name = method->exec.debug_info->localVars[i].name;
        w_string desc = method->exec.debug_info->localVars[i].desc;
        w_ubyte *string;
        w_int length;

        woempa(7, "    %w %w codeIndex %d length %d, slot %d\n", name, desc, method->exec.debug_info->localVars[i].start_pc, method->exec.debug_info->localVars[i].length, method->exec.debug_info->localVars[i].slot);
        jdwp_put_u4(&reply_grobag, 0);                        // lineCodeIndex (MSW)
        jdwp_put_u4(&reply_grobag, method->exec.debug_info->localVars[i].start_pc);
        string = jdwp_string2UTF8(name, &length);
        jdwp_put_cstring(&reply_grobag, (char*)string + 4, length);   // name
        deregisterString(name);
        releaseMem(string);
        string = jdwp_string2UTF8(desc, &length);
        jdwp_put_cstring(&reply_grobag, (char*)string + 4, length);   // signature
        deregisterString(desc);
        releaseMem(string);
                                                              // length
        jdwp_put_u4(&reply_grobag, method->exec.debug_info->localVars[i].length);
                                                              // slot
        jdwp_put_u4(&reply_grobag, method->exec.debug_info->localVars[i].slot);
      }
    }
    else {
      woempa(7, "  no local var info available\n");
      jdwp_put_u4(&reply_grobag, 0);                        // slots
    }
    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}


/*
** Returns the bytecode of a given method in a given reference.
*/

w_void jdwp_method_bytecode(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_clazz clazz;
  w_method method;
  w_int n;

  clazz = jdwp_get_clazz(cmd->data, &offset);
  woempa(7, "clazz = %p\n", clazz);

  if (jdwp_check_clazz(clazz)) {
    method = jdwp_get_method(cmd->data, &offset);
    n = method->exec.code_length;
    woempa(7, "clazz = %k, method = %m, bytcode length = %d\n", clazz, method, n);
    jdwp_put_u4(&reply_grobag, n);
    jdwp_put_bytes(&reply_grobag, method->exec.code, n);
    
    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}


/*
** The dispatcher for the 'Method' command set.
*/

w_void dispatch_method(jdwp_command_packet cmd) {

  woempa(7, "Method Command = %s\n", cmd->command > 0 && cmd->command <= METHOD_MAX_COMMAND ? method_command_names[cmd->command] : "unknown");
  switch((jdwp_method_cmd)cmd->command) {
    case jdwp_method_lineTable:
      jdwp_method_lines(cmd);
      break;
    case jdwp_method_variableTable:
      jdwp_method_var_table(cmd);
      break;
    case jdwp_method_bytecodes: 
      jdwp_method_bytecode(cmd);
      break;

    default:
      jdwp_send_not_implemented(cmd->id);
  }
  
}

