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
        w_string desc = method->exec.debug_info->localVars[i].desc_or_sig;
        w_ubyte *string;
        w_int length;

        woempa(7, "    %w %w codeIndex %d length %d, slot %d\n", name, desc, method->exec.debug_info->localVars[i].start_pc, method->exec.debug_info->localVars[i].length, method->exec.debug_info->localVars[i].slot);
        jdwp_put_u4(&reply_grobag, 0);                        // lineCodeIndex (MSW)
        jdwp_put_u4(&reply_grobag, method->exec.debug_info->localVars[i].start_pc);
        string = w_string2UTF8(name, &length);
        jdwp_put_cstring(&reply_grobag, (char*)string, length);   // name
        deregisterString(name);
        releaseMem(string);
        string = w_string2UTF8(desc, &length);
        jdwp_put_cstring(&reply_grobag, (char*)string, length);   // signature
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

