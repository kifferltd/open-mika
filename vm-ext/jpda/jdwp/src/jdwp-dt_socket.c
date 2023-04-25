/**************************************************************************
* Copyright (c) 2020, 2023 by KIFFER Ltd. All rights reserved.            *
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

#include "jdwp.h"

#include "network.h"

/*
** The sockets which will be used for transport.
*/

static w_int sock;
static w_int serversock;

static void report_error(const char *host, const char *port, const char *mess) {
#ifdef FREERTOS
  (void) printf("\r\nFailed to bind JDWP socket to %s:%s due to %s\n", host, port, mess ? mess : "error");
#else
  (void) fprintf(stderr, "\r\nFailed to bind JDWP socket to %s:%s due to '%s'\n", host, port, mess ? mess : strerror(errno));
#endif
}

/*
** Connect to the debugger via a TCP/IP socket. 
*/

w_boolean jdwp_connect_dt_socket(const char *jdwp_address_host, const char *jdwp_address_port, const w_boolean jdwp_config_server) {
  
  int rc = 1;
  struct w_sockaddr_in sa;
  char *buffer;
  int count = 0;
#ifdef FREERTOS
  w_word host;
#else
  struct hostent *host;
#endif
  int port_nr;
  int timeout = 0;

  /*
  ** Get the host address and port.
  */
  
  host = w_gethostbyname(jdwp_address_host);
  if(!host) {
    report_error(jdwp_address_host, jdwp_address_port, "DNS lookup failure");

    return FALSE;
  }

  rc = sscanf(jdwp_address_port, "%d", &port_nr);
  if (rc == 0 || rc == EOF) {
    report_error(jdwp_address_host, jdwp_address_port, "invalid port number");

    return FALSE;
  }


  /*
  ** Get a socket and fill in the address structure.
  */

  sock = w_socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);

  if(sock == -1) {
    report_error(jdwp_address_host, jdwp_address_port, NULL);

    return FALSE;
  }

  memset(&sa,0,sizeof(sa)); 
  sa.sin_port = w_htons(port_nr);
 
  if(jdwp_config_server) {

    /*
    ** We should behave as a server and wait for jdwp connections.
    */
   
    woempa(7, "JDWP Port : %s\n", jdwp_address_port);
    w_size socksize = sizeof(struct w_sockaddr_in);

    if(serversock == 0) {
    
      serversock = sock;
#ifdef FREERTOS
      sa.sin_family = AF_INET;
      sa.sin_addr = NULL;
#else
      sa.sin_addr.s_addr = INADDR_ANY;
      sa.sin_family = host->h_addrtype;
#endif
      rc = w_bind(serversock, (struct w_sockaddr*)&sa, sizeof(struct w_sockaddr_in));
      if(rc == -1) {
        report_error(jdwp_address_host, jdwp_address_port, NULL);
        w_socketclose (sock);
        sock = 0;
        serversock = 0;

        return FALSE;
      }
      rc = w_listen(serversock, 1);
      if(rc == -1) {
        report_error(jdwp_address_host, jdwp_address_port, NULL);
        w_socketclose (sock);
        sock = 0;
        serversock = 0;

        return FALSE;
      }
      printf("Listening for transport dt_socket at address: %d\n", port_nr);
    }

    woempa(7, "Calling accept() on socket %d\n", serversock);
    sock = w_accept(serversock, (struct w_sockaddr *)&sa, &socksize, 0);
    woempa(7, "accepted() returned %d\n", sock);
    rc = (sock < 0) ? -1 : 0;
  } else {

    /*
    ** We should behave as a client and connect to the debugger at startup.
    */
    woempa(7, "JDWP Host : %s\n", jdwp_address_host);
    woempa(7, "JDWP Port : %s\n", jdwp_address_port);
    
#ifdef FREERTOS
    sa.sin_addr = host;
    sa.sin_family = AF_INET;
#else
    sa.sin_addr.s_addr = *((unsigned long*)host->h_addr_list[0]);
    sa.sin_family = host->h_addrtype;
#endif
    woempa(7, "Connecting socket %d\n", sock);
    rc = w_connect(sock, (struct w_sockaddr*)&sa, sizeof(struct w_sockaddr_in), 0);
    woempa(7, "connect() return code = %d\n", rc);
    
  }

  if(rc != 0) {
    woempa(9, "-------------------------------------------------------------------------------------------------\n");
    woempa(9, "         J D W P :     C O U L D    N O T    C O N N E C T    TO    T H E    D E B U G G E R\n");
    woempa(9, "-------------------------------------------------------------------------------------------------\n");
    report_error(jdwp_address_host, jdwp_address_port, NULL);

    return FALSE;
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
  
  return TRUE;
}

/*
** Receive a packet from the debugger. This function will block the thread that called it if
** there aren't any packets available.
*/

void *jdwp_recv_packet_dt_socket(void) {
  
  jdwp_command_packet cmd;
  w_int peek;
  w_int count = 0;
  w_int timeout = 0;
  w_int rc = 0;
  
  while (rc < sizeof(w_int)) {
    // Note: not using MSG_PEEK because FreeRTOS does not support this.
    rc = w_recv(sock, &peek, sizeof(w_int), 0, &timeout);
  }
  peek = w_ntohl(peek);
  woempa(7, "Peek result = %d, peek = 0x%08x\n", rc, peek);

  /*
  ** We now know the length of the entire command so we can allocate a
  ** buffer and retrieve the whole packet.
  */
    
  cmd = allocMem((w_size)peek);
  cmd->length = peek;
  count = sizeof(w_int);
  while(count < peek) {
    rc = w_recv(sock, ((w_ubyte *)cmd + count), (w_size)(peek - count), 0, &timeout);
    woempa(1, "Recv result = %d\n", rc);
    if (rc > 0) count += rc;
    woempa(1, "      count = %d\n", count);
  }
  
  return cmd;
}


/*
** Send a packet to the debugger.
*/

void jdwp_send_packet_dt_socket(void *packet) {
  w_send(sock, packet, ((jdwp_reply_packet)packet)->length, 0);
  return;
}

/*
** Disconnect the debugger.
*/

void jdwp_disconnect_dt_socket(void) {
  if(sock)  {
    w_socketclose(sock);
    sock = 0;
  }
  jdwp_state = jdwp_state_initialised;
  return;
}
