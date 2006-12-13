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

#include <stdio.h>
#include <sys/socket.h>
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
** The sockets which will be used for transport.
*/

static w_int sock;
static w_int serversock;


/*
** The mutex used for locking.
*/

x_mutex  jdwp_mutex;

/*
** When jdwp_events_enabled equals 0, sending events will be supressed.
*/

w_int    jdwp_events_enabled = 0;


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

/*
** Connect to the debugger. Only TCP/IP socket connections are possible for now. In the future, 
** this will most likely be extended to connections over serial ports and others.
** PS: These functions should eventually move to a seperate file/layer, which makes it easier
**     to implement different transports for JDWP.
*/

void jdwp_connect() {
  
  int rc = 1;
  struct sockaddr_in sa;
  char *buffer;
  int  count = 0;
  struct hostent *host;
  int port_nr;
  int timeout = 0;

  /*
  ** Get the host address and port.
  */

  woempa(9, "JDWP Host : %s\n", jdwp_address_host);
  woempa(9, "JDWP Port : %s\n", jdwp_address_port);
  
  host = w_gethostbyname(jdwp_address_host);
  sscanf(jdwp_address_port, "%d", &port_nr);

  /*
  ** Get a socket and fill in the address structure.
  */

  sock = w_socket(PF_INET, SOCK_STREAM, 0);
  memset(&sa,0,sizeof(sa)); 
  sa.sin_addr.s_addr = *((unsigned long*)host->h_addr_list[0]);
  sa.sin_family = host->h_addrtype;
  sa.sin_port = swap_short(port_nr);
 
  if(jdwp_config_server) {

    /*
    ** We should behave as a server and wait for jdwp connections.
    */
   
    w_size socksize = sizeof(struct sockaddr_in);

    if(serversock == 0) {
    
      serversock = sock;
      sa.sin_addr.s_addr = INADDR_ANY;
      rc = w_bind(serversock, (struct sockaddr*)&sa, sizeof(struct sockaddr_in));
      rc = w_listen(serversock, 1);  
    }

    woempa(7, "Calling accept() on socket %d\n", serversock);
    sock = w_accept(serversock, (struct sockaddr *)&sa, &socksize, 0);
    woempa(7, "accepted() returned %d\n", sock);
    rc = (sock < 0) ? -1 : 0;
  } else {

    /*
    ** We should behave as a client and connect to the debugger at startup.
    */
    
    woempa(7, "Connecting socket %d\n", sock);
    rc = w_connect(sock, (struct sockaddr*)&sa, sizeof(struct sockaddr_in));
    woempa(7, "connect() return code = %d\n", rc);
    
  }

  if(rc != 0) {
    woempa(9, "-------------------------------------------------------------------------------------------------\n");
    woempa(9, "         J D W P :     C O U L D    N O T    C O N N E C T    TO    T H E    D E B U G G E R\n");
    woempa(9, "-------------------------------------------------------------------------------------------------\n");
    x_thread_sleep(x_eternal);
  }

  /*
  ** Wait for the handshake. Keep receiving bytes until we have received the full 14 bytes.
  */

  buffer = allocClearedMem(15);
  
  while(count < 14) {
    rc = w_recv(sock, (buffer + count), (w_word)(14 - count), 0, (w_int *)&timeout);
    if (rc > 0) count += rc;
  }
  woempa(7, "Received handshake: '%s'\n", buffer);
  
  if(strcmp(buffer, (char*)"JDWP-Handshake") != 0) {
    wabort(ABORT_WONKA, "Incredible bad handshake: %s\n", buffer);
  }
  
  releaseMem(buffer);

  /*
  ** Respond to the handshake.
  */

  w_send(sock, "JDWP-Handshake", 14, 0);

  woempa(9, "JDWP connection established.\n");

  jdwp_state = jdwp_state_connected;

  /*
  ** From now on, it's safe to send events.
  */ 

  jdwp_events_enabled = 1;
  
  return;
}


/*
** Disconnect the debugger.
*/

void jdwp_disconnect() {
  if(sock) w_socketclose(sock);
  jdwp_state = jdwp_state_initialised;
  return;
}


/*
** Receive a packet from the debugger. This function will block the thread that called it if
** there aren't any packets available.
*/

void *jdwp_recv_packet() {
  
  jdwp_command_packet cmd;
  w_int peek;
  w_int count = 0;
  w_int timeout = 0;
  w_int rc = 0;
  
  /*
  ** First peek to get the length of this packet
  */
    
  while (rc < 4) {
    rc = w_recv(sock, &peek, sizeof(w_int), MSG_PEEK, (w_int *)&timeout);
  }
  peek = swap_int(peek);
  woempa(1, "Peek result = %d, peek = 0x%08x\n", rc, peek);

  /*
  ** We now know the length of the entire command so we can allocate a
  ** buffer and retrieve the whole packet.
  */
    
  cmd = allocMem((w_word)peek);
  while(count < peek) {
    rc = w_recv(sock, ((w_ubyte *)cmd + count), (w_word)(peek - count), 0, (w_int *)&timeout);
    woempa(1, "Recv result = %d\n", rc);
    if (rc > 0) count += rc;
    woempa(1, "      count = %d\n", count);
  }
  
  return cmd;
}


/*
** Send a packet to the debugger.
*/

void jdwp_send_packet(void *packet) {
  w_send(sock, packet, swap_int(((jdwp_reply_packet)packet)->length), 0);
  return;
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

  jdwp_send_packet(command);

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
      if(jdwp_address_port != NULL) {
        jdwp_address_port[0] = '\0';
        jdwp_address_port++;
      } else {
        jdwp_address_port = value;
        jdwp_address_host = "127.0.0.1";
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

void jdwp_dispatcher() {
  jdwp_command_packet cmd;
  
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

  while(1) {

    /*
    ** Try to connect to the debugger.
    */

    jdwp_connect();

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

      cmd = jdwp_recv_packet();

      if(cmd->flags == 0) {

        /*
        ** It's a command packet.
        */

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

    jdwp_disconnect();

  }

  releaseMem(cmd);
  
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
  w_int    length = strlen(string);
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

