/**************************************************************************
* Copyright (c) 2020 by KIFFER Ltd. All rights reserved.                  *
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
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>

#include "jdwp.h"
#include "jdwp-protocol.h"
#include "network.h"
#include "oswald.h"
#include "wonka.h"
#include "wstrings.h"

#define JDWP_THREAD_STACK_SIZE 65536
#define JDWP_THREAD_PRIORITY 10

extern void jdwp_event_vm_start(w_instance);

extern w_int jdwp_global_suspend_count;

w_thread jdwp_thread;

jdwp_state_enum jdwp_state;

JNIEnv *jdwp_JNIEnv;

char *jdwp_base_directory;

/*
** Command line arguments from argument.c
*/

extern char  *jdwp_args;
extern int   jdwp_enabled;

/*
** Internal configuration parameters.
*/

static char   *jdwp_address_host  = "127.0.0.1";
static char   *jdwp_address_port  = "5555";
static w_int  jdwp_config_server  = 0; 
w_int  jdwp_config_suspend = 1; 


/*
** The mutex used for locking.
*/

x_mutex  jdwp_mutex;

/*
** When jdwp_events_enabled equals 0, sending events will be supressed.
*/

w_int    jdwp_events_enabled = 0;

/*
** Command set names
*/
extern const char *command_set_names[];

/*
** Command names
*/
extern const char **command_names[];

/*
** Highest-numbered command in each command set
*/
const int command_set_max_command[] = {
  0, 16, 11, 4, 1, 0, 3, 0, 0, 9, 1, 12, 3, 3, 1, 3, 3, 1
  };

/*
** Show a little explanation about the JDWP parameters.
*/

void print_jdwp_help(void) {
  
  printf("  Usage of -Xjdwp:   -Xjdwp:<option>=<value>, ...\n");
  printf("\n");
  printf("Options :\n");
  printf("  suspend=y|n         Suspend the vm after startup (default: n) (not yet implemeted)\n");
  printf("  transport=<name>    Transport type. dt_socket is the only supported transport.\n");
  printf("                      (default: dt_socket)\n");
  printf("  address=<address>   Address to connect/listen to. (default: 127.0.0.1:5555)\n");
  printf("  server=y|n          Listen for the debugger or connect to the debugger. \n");
  printf("                      (default: n -> connect to the debugger)\n");
  printf("\n");
  exit(1);

}


static w_int current_cmdID = 0;

/*
** Send a command to the debugger. 
*/

void jdwp_send_command(w_grobag* gb, w_int cmd_set, w_int cmd) {
  jdwp_command_packet command;

  /*
  ** Allocate memory for the packet.
  */

  command = allocMem((w_word)(offsetof(jdwp_Command_Packet, data) + (*gb)->occupancy));

  /*
  ** Fill in the header of the packet.
  */

  command->length = swap_int((offsetof(jdwp_Reply_Packet, data) + (*gb)->occupancy));
  command->id = current_cmdID++;
  command->flags = 0;
  command->command_set = cmd_set;
  command->command = cmd;

  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    char *contents = bytes2hex((w_ubyte*) (*gb)->contents, (*gb)->occupancy);

    w_printf("JDWP: Sending command id %d, command set: %d (%s),  command: %d (%s),  length: %d, contents: %s\n", command->id, command->command_set, cmd_set == 64 ? "Event" : command_set_names[cmd_set], cmd, cmd_set == 64 ? "Composite" : command_names[cmd_set][cmd], swap_int(command->length), contents);
    releaseMem(contents);
  }

  /*
  ** If there's data to be added to the packet, do so.
  */
  
  if ((*gb) && (*gb)->occupancy) {
    memcpy(command->data, (*gb)->contents, (*gb)->occupancy);
    (*gb)->occupancy = 0;
  }

  /*
  ** Send the packet to the debugger.
  */

  jdwp_send_packet_dt_socket(command);

  /*
  ** Clean up.
  */

  releaseMem(command);
}


/*
** Parse jdwp arguments.
*/

void jdwp_parse_arguments(char *args) {
  char *argument;
  char *key;
  char *value;

  /*
  ** Go over the arguments one by one. They are seperated with comma's.
  */

  while((argument = strsep((char **)&args, ",")) != NULL) {

    /*
    ** Every parameter has a key and value, they are seperated with ='s.
    */
    
    key = argument;
    value = index(argument, '=');
    if(value != NULL) {
      value[0] = '\0';
      value++;
    }

    /*
    ** Check the key/value pairs.
    */

    /*
    ** server: if server is set to 'y', we have to listen for connections,
    **         if it's set to 'n', we have to connect to the debugger.
    */

    if(strcmp(key, "server") == 0) {
      if(strcmp(value, "y") == 0) {
        woempa(7, "Setting jdwp_config_server to 1\n");
        jdwp_config_server = 1;
      }
      else if(strcmp(value, "n") == 0) {
        woempa(7, "Setting jdwp_config_server to 0\n");
        jdwp_config_server = 0;
      } 
      else {
        printf("jdwp error: server should be set to 'y' or 'n', but not to '%s' !\n", value);
        exit(1);
      }
    } 

    /*
    ** suspend: if suspend is set to 'y', the VM should start up in a suspended 
    **          state, if it's set to 'n', everything stays the way it was.
    */
    
    else if(strcmp(key, "suspend") == 0) {
      if(strcmp(value, "y") == 0) {
        woempa(7, "Setting jdwp_config_suspend to 1\n");
        jdwp_config_suspend = 1;
      } 
      else if(strcmp(value, "n") == 0) {
        woempa(7, "Setting jdwp_config_suspend to 0\n");
        jdwp_config_suspend = 0;
      }
      else {
        printf("jdwp error: suspend should be set to 'y' or 'n', but not to '%s' !\n", value);
        exit(1);
      }
    } 

    /*
    ** transport: the method of transport for jdwp. Only dt_socket is supported for 
    **            the moment, but support for serial connections is pending...
    */
    
    else if(strcmp(key, "transport") == 0) {
      if(strcmp(value, "dt_socket") != 0) {
        printf("jdwp error: transport can only be set to 'dt_socket', so '%s' is out of the question !\n", value);
        exit(1);
      }
    } 

    /*
    ** address: the address and/or port to listen/connect to.
    */
    
    else if(strcmp(key, "address") == 0) {
      jdwp_address_host = value;
      jdwp_address_port = strchr(value, ':');
      if(jdwp_address_port == value) {
        // hostname part is empty, port is present
        jdwp_address_host = "127.0.0.1";
        jdwp_address_port++;
        if (!*jdwp_address_port) {
          jdwp_address_port = "5555";
        }
      }
      else if(jdwp_address_port) {
        // both hostname and port are present, insert a null char between them
        *jdwp_address_port++ = 0;
        if (!*jdwp_address_port) {
          jdwp_address_port = "5555";
        }
      } else {
        // no colon found, use default port
        jdwp_address_port = "5555";
        if (!*value) {
          // no address either (empty string)
          jdwp_address_host = "127.0.0.1";
        }
      }
      woempa(7, "Setting jdwp_address_host to %s, jdwp_address_port to %s\n", jdwp_address_host, jdwp_address_port);
    }
  }
  
  return;
}

/*
** This is the main loop of JDWP and it's running in its own thread. First it will try to connect 
** to the debugger, then it goes in to a loop where it waits for commands from the debugger. 
** When a command packet is received, it will be parsed and dispatched to the different commandset
** dispatchers. This will go on and on forever.
*/

static w_boolean jdwp_have_sent_vm_start;

static w_int connection_attempt_count;
#define MAX_CONNECTION_ATTEMPTS 12
#define CONNECTION_REATTEMPT_MILLIS 2500
extern int wonka_killed;

void jdwp_dispatcher() {
  jdwp_command_packet cmd = NULL;
  
  if(!jdwp_enabled) {
    jdwp_state = jdwp_state_terminated;
    
    return;

  }

  if(jdwp_args) {
    jdwp_parse_arguments(jdwp_args);
  }

  jdwp_mutex = allocMem(sizeof(x_Mutex));
  x_mutex_create(jdwp_mutex);

  woempa(7, "Setting jdwp_thread = %t\n", currentWonkaThread);
  jdwp_thread = currentWonkaThread;

  /*
  ** Note the current working directory, it will be e.g. the base for relative classpaths.
  */
  {
    int m;

    m = 32;
    jdwp_base_directory = allocMem(m);
    while (!getcwd(jdwp_base_directory, m - 1)) {
      m *= 2;
      jdwp_base_directory = reallocMem(jdwp_base_directory, m);
      memset(jdwp_base_directory, 0, m);
    }
    woempa(7, "jdwp_base_directory is '%s'\n", jdwp_base_directory);

  }

  jdwp_event_hashtable = ht_create("hashtable:jdwp-events", JDWP_EVENT_HASHTABLE_SIZE, NULL, NULL, 0, 0);
  jdwp_breakpoint_hashtable = ht_create("hashtable:jdwp-breakpoints", JDWP_BREAKPOINT_HASHTABLE_SIZE, NULL, NULL, 0, 0);

  if (jdwp_config_suspend) {
    jdwp_internal_suspend_all();
  }

  woempa(7, "Setting jdwp_state to jdwp_state_initialised\n");
  jdwp_state = jdwp_state_initialised;

  while(jdwp_state != jdwp_state_terminated) {
    if (!jdwp_connect_dt_socket(jdwp_address_host, jdwp_address_port, jdwp_config_server)) {
      if (!wonka_killed && connection_attempt_count++ < MAX_CONNECTION_ATTEMPTS) {
        x_thread_sleep(x_millis2ticks(CONNECTION_REATTEMPT_MILLIS));
        if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
          w_printf("JDWP: Unable to establish connection, retrying in %d ms", CONNECTION_REATTEMPT_MILLIS);
        }
      }
      else {
        while (jdwp_global_suspend_count) {
          jdwp_internal_resume_all();
        }
        jdwp_state = jdwp_state_terminated;
        if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
          w_printf("JDWP: Unable to establish connection after %d attempts, giving up", MAX_CONNECTION_ATTEMPTS);
        }
      }
      continue;
    }

    if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
      w_printf("JDWP: Connection established\n");
    }
    /*
    ** If VM_START was not yet sent, send it now.
    */

    if (!jdwp_have_sent_vm_start) {
      jdwp_event_vm_start(I_Thread_sysInit);
      jdwp_have_sent_vm_start = TRUE;
    }

    /*
    ** Now we are connected.
    ** Receive packets and respond to them.
    */

    while(jdwp_state == jdwp_state_connected) {

      cmd = jdwp_recv_packet_dt_socket();

      if (cmd->flags == 0) {
        /*
        ** It's a command packet.
        */

        if (cmd->command_set > 0 || cmd->command_set <= 17) {
          if (cmd->command <= 0 || cmd->command > command_set_max_command[cmd->command_set]) {
            if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
              w_printf("JDWP: Received command id %d, command set: %d (%s),  command: %d (unknown!),  length: %d - ignoring\n", swap_int(cmd->id), cmd->command_set, command_set_names[cmd->command_set], cmd->command, swap_int(cmd->length));
            }
            continue;
          }
          else {
            if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
              char *contents = bytes2hex((w_ubyte*) cmd->data, swap_int(cmd->length) - 11);

              w_printf("JDWP: Received command id %d, command set: %d (%s),  command: %d (%s),  length: %d, contents: %s\n", swap_int(cmd->id), cmd->command_set, command_set_names[cmd->command_set], cmd->command, command_names[cmd->command_set][cmd->command], swap_int(cmd->length), contents);
              releaseMem(contents);
            }
          }
        }
        else if (cmd->command_set == 64) {
          if (cmd->command != 100) {
            if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
              w_printf("JDWP: Received command id %d, command set: %d (Event),  command: %d (unknown!),  length: %d - ignoring\n", swap_int(cmd->id), cmd->command, swap_int(cmd->length));
            }
            continue;
          }
          else {
            if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
              char *contents = bytes2hex((w_ubyte*) cmd->data, swap_int(cmd->length) - 11);

              w_printf("JDWP: Received command id %d, command set: 64 (Event),  command: 100 (Composite),  length: %d, contents: %s\n", swap_int(cmd->id), swap_int(cmd->length), contents);
              releaseMem(contents);
            }
          }
        }
        else {
          if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
            w_printf("JDWP: Received command id %d, command set: %d (unknown!)  length: %d - ignoring\n", swap_int(cmd->id), cmd->command_set, swap_int(cmd->length));
          }
          continue;
        }

        woempa(9, "Command id %d, command set: %d,  command: %d,  length: %d\n", swap_int(cmd->id), cmd->command_set, cmd->command, swap_int(cmd->length));

        x_mutex_lock(jdwp_mutex, x_eternal);
        switch((jdwp_cmdset)cmd->command_set) {
          case jdwp_cmdset_virtualMachine:        dispatch_vm(cmd);              break;
          case jdwp_cmdset_referenceType:         dispatch_reftype(cmd);         break;
          case jdwp_cmdset_classType:             dispatch_classtype(cmd);       break;
          case jdwp_cmdset_arrayType:             dispatch_arraytype(cmd);       break;
          case jdwp_cmdset_interfaceType:         dispatch_inttype(cmd);         break;
          case jdwp_cmdset_method:                dispatch_method(cmd);          break;
          case jdwp_cmdset_field:                 dispatch_field(cmd);           break;
          case jdwp_cmdset_objectReference:       dispatch_objref(cmd);          break;
          case jdwp_cmdset_stringReference:       dispatch_strref(cmd);          break;
          case jdwp_cmdset_threadReference:       dispatch_threadref(cmd);       break;
          case jdwp_cmdset_threadGroupReference:  dispatch_threadgrpref(cmd);    break;
          case jdwp_cmdset_arrayReference:        dispatch_arrayref(cmd);        break;
          case jdwp_cmdset_classLoaderReference:  dispatch_classloaderref(cmd);  break;
          case jdwp_cmdset_eventRequest:          dispatch_eventreq(cmd);        break;
          case jdwp_cmdset_stackFrame:            dispatch_stack(cmd);           break;
          case jdwp_cmdset_classObjectReference:  dispatch_classobjref(cmd);     break;
          case jdwp_cmdset_event:                 dispatch_event(cmd);           break;
          case jdwp_cmdset_wonka:                 dispatch_wonka(cmd);           break;
          default:

            /*
            ** None of the above -> Seems there's a command set we don't know about 
            ** so send the 'not implemented' error.
            */

            jdwp_send_not_implemented(cmd->id);
        }
        x_mutex_unlock(jdwp_mutex);

      } else {

        /*
        ** It's a reply packet.
        */

        woempa(9, "Reply packet received, but we don't know how to handle this.. (so we just forget about it)\n");

      }

    }

    if (jdwp_config_suspend) {
      while (jdwp_global_suspend_count) {
        jdwp_internal_resume_all();
      }
    }

    jdwp_disconnect_dt_socket();

  }

  if (cmd) {
    releaseMem(cmd);
  }
  
  return;
}


/*
** Convert a Wonka String to a JDWP UTF8 string. This UTF8 is different from those used in
** Wonka because the length at the beginning of the string is 4 bytes instead of 2 bytes.
** The memory used to hold the string is allocated using allocMem(), and the 
** caller should release it using releaseMem().
*/

w_ubyte *jdwp_string2UTF8(w_string string, w_int *length) {
  w_ubyte *UTF8string;
  w_ubyte *result;
  
  UTF8string = string2UTF8(string, length);
  result = allocMem((w_word)(*length + 2));
  memcpy(&result[2], UTF8string, (w_word)*length);
  *length = swap_int((*length - 2));
  memcpy(&result[0], length, 4);
  *length = swap_int(*length);
  releaseMem(UTF8string);

  return result;
}


/*
** Convert a cstring to a JDWP UTF8 string.
** The memory used to hold the string is allocated using allocMem(), and the 
** caller should release it using releaseMem().
*/

w_ubyte *jdwp_cstring2UTF8(char *string, w_int *length) {
  w_ubyte *result;
  
  *length = strlen(string);
  result = allocClearedMem((w_word)(*length + 4 + 1));
  memcpy(&result[4], string, (w_word)*length);
  *length = swap_int(*length);
  memcpy(&result[0], length, 4);
  *length = swap_int(*length);

  return result;
}


/*
** Convert a JDWP UTF8 string to a cstring. The memory used to hold the
** string is allocated using allocMem(), and the caller should release it
** using releaseMem().
*/

w_ubyte *jdwp_UTF82cstring(w_ubyte *string, w_int *length) {
  w_ubyte *result;
  
  memcpy(length, &string[0], 4);
  *length = swap_int(*length);
  
  result = allocClearedMem((w_word)(*length + 1));
  memcpy(result, &string[4], (w_word)*length);

  return result;
}


/*
** Convert a Wonka string to a JNI mangled string.
** The memory used to hold the string is allocated using allocMem(), and the 
** caller should release it using releaseMem().

w_ubyte *jdwp_string2JNI(w_string string) {
  w_int    length = string_length(string);
  char *jni = allocClearedMem((w_word)(length + 50));
  char *iter = jni;
  w_int i;
  w_char c;
  char hex[5];

  for(i = 0; i < length; i++) {
    c = string_char(string, i);
    if(c & 0xFF80) {
      sprintf(hex, "%04x", c);
      *(iter++) = '_';
      *(iter++) = '0';
      strcpy(iter, hex);
      iter += 4;
    } else {
      switch(c & 0x00FF) {
        case '/' : *(iter++) = '_'; break ;
        case '[' : *(iter++) = '_'; *(iter++) = '3'; break;
        case ';' : *(iter++) = '_'; *(iter++) = '2'; break;
        case '_' : *(iter++) = '_'; *(iter++) = '1'; break;
        case '(' : *(iter++) = '_'; *(iter++) = '_'; break;
        case ')' : break;
        default  : *(iter++) = c & 0x00FF;
      }
    }
  }
  
  return jni;
}
*/


/*
** Convert a cstring to a JNI mangled string.
** The memory used to hold the string is allocated using allocMem(), and the 
** caller should release it using releaseMem().
*/

w_ubyte *jdwp_cstring2JNI(w_ubyte *string) {
  w_int    length = strlen((char*) string);
  w_ubyte  *jni = allocClearedMem((w_word)(length + 50));
  w_ubyte  *iter = jni;
  w_int    i;

  for(i = 0; i < length; i++) {
    switch(string[i]) {
      case '/' : *(iter++) = '_'; break ;
      case '[' : *(iter++) = '_'; *(iter++) = '3'; break;
      case ';' : *(iter++) = '_'; *(iter++) = '2'; break;
      case '_' : *(iter++) = '_'; *(iter++) = '1'; break;
      case '(' : *(iter++) = '_'; *(iter++) = '_'; break;
      case ')' : break;
      default  : *(iter++) = string[i];
    }
  }
  
  return jni;
}

