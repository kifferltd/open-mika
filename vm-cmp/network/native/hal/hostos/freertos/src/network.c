/**************************************************************************
* Copyright (c) 2020, 2022, 2023 by KIFFER Ltd. All rights reserved.      *
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

#include <oswald.h>
#include <network.h>
#include <wonka.h>

w_int FreeRTOS_IPAddress;
w_word FreeRTOS_NetMask;
w_word FreeRTOS_GatewayAddress;
w_word FreeRTOS_DNSServerAddress;

void startNetwork(void) {
}

static char hostname[32] = {'I', 'm', 's', 'y', 's', 0};

w_int w_gethostname(char *name, size_t len) {
  strncpy(name, hostname, len);
  return 0;
}

w_int w_sethostname(const char *name, size_t len) {
  strncpy(hostname, name, len < 32 ? len: 31);
  return 0;
}

/*
 * TODO
 * If a timeout is specified then we have to do a lot of fancy stuff
 * instead of just forwarding to connect(s,a,l)
 */
w_int w_connect(w_sock s, void *a, w_size l, w_int t) {
//    if (t == 0) {
        return FreeRTOS_connect(s, a, l);
//    }
// otherwise ... TODO
}

/*
 * If recv is interrupted before any data read, restart it.
 * If it returns 0 then probably the timeout has expired, 
 * so set timeout to -1 as a clunky way to signal to
 * PlainSocketImpl_read() that a timeout occurred.
 */
w_int w_recv(w_sock s, void *b, size_t l, w_int f, w_int *timeoutp) {
  BaseType_t retval = FreeRTOS_recv(s,b,l,f);

  while (retval == -pdFREERTOS_ERRNO_EINTR) {
    retval = FreeRTOS_recv(s,b,l,f);
  }

  if (retval == 0) {
    *timeoutp = -1;
  }

  return (w_int)retval;
}

// TODO no socklen_t for parameter l?
w_sock w_accept(w_sock s, struct w_sockaddr *a, unsigned *l, w_int timeout) {
  w_sock retval = FreeRTOS_accept(s,a,l);

  if (retval < 0) {
    *l = 0;
  }
  return retval;
}

// TODO no socklen_t for parameter l?
w_int w_recvfrom(w_sock s, void *b, size_t blen, w_int f, struct w_sockaddr *sa, unsigned *size_sa, w_int timeout, w_instance This) {

  w_int retval =  FreeRTOS_recvfrom(s, b, blen, f, sa, size_sa);

  while (retval == -1 && errno == EINTR) {
    retval =  FreeRTOS_recvfrom(s, b, blen, f, sa, size_sa);
  }

  if (retval == -1 && errno == EAGAIN) {
    *size_sa = 0;
  }

  return retval;
}

/*
 * We don't want to do any dynamic memory allocation here, because this code gets called very early on.
 * So instead we use a ring-buffer of 16 buffers, each of which is big enough to contain a numeric
 * ipv4 address (xxx.xxx.xxx.xxx) plus a trailing null byte. In practice we should never need more 
 * than four buffers at a time (for vApplicationIPNetworkEventHook).
 */
static char ipabuf[16][16];
static int ipabufidx = 0;

static char *ipaddress_word2cstring(int ipa) {
  char *result = &ipabuf[ipabufidx];
  snprintf(result, 16, "%d.%d.%d.%d", (ipa >> 24) & 0xff, (ipa >> 16) & 0xff, (ipa >> 8) & 0xff, ipa & 0xff);
  ipabufidx = (ipabufidx + 1) & 0xf;

  return result;
}

static TaskHandle_t _xCliServerHandle = NULL;

void vApplicationIPNetworkEventHook(eIPCallbackEvent_t eNetworkEvent) {
  tcp_mac_t mac;
  tcp_return_code_t rc = tcp_get_mac(&mac);
  snprintf(hostname, 32, "Imsys_%02x%02x%02x", mac.puMAC[3], mac.puMAC[4], mac.puMAC[5]);
//#ifdef DEBUG
  printf("Network is %s\n", eNetworkEvent == eNetworkUp ? "UP" : "DOWN");
  if (rc == TCP_SUCCESS) {
    printf("MAC address = %02x:%02x:%02x:%02x:%02x:%02x\n", mac.puMAC[0], mac.puMAC[1], mac.puMAC[2], mac.puMAC[3], mac.puMAC[4], mac.puMAC[5]);
  }
  else {
    printf("MAC address not available\n");
  }

  FreeRTOS_GetAddressConfiguration(&FreeRTOS_IPAddress, &FreeRTOS_NetMask, &FreeRTOS_GatewayAddress, &FreeRTOS_DNSServerAddress );
  printf("IPAdress = %s Netmask = %s GatewayAddress = %s DNSServerAddress = %s\n", ipaddress_word2cstring(FreeRTOS_IPAddress), ipaddress_word2cstring(FreeRTOS_NetMask), ipaddress_word2cstring(FreeRTOS_GatewayAddress), ipaddress_word2cstring(FreeRTOS_DNSServerAddress));
//#endif
#ifdef FREERTOS_CLI
  if (rc == TCP_SUCCESS) {
    if(_xCliServerHandle == NULL) {
      _xCliServerHandle =  startTelnetConsole();
    }
  }
  else {
    stopTelnetConsole(_xCliServerHandle);
    _xCliServerHandle = NULL;
  }
#endif
}

void tcpPingSendHook(uint32_t address) {
#ifdef DEBUG
  printf("Pinging %s\n", ipaddress_word2cstring(address));
#endif
}

void  vApplicationPingReplyHook(ePingReplyStatus_t eStatus, uint16_t usIdentifier) {
#ifdef DEBUG
  const char *status;
  switch (eStatus) {
    case eSuccess : status = "Success"; break;
    case eInvalidChecksum : status = "InvalidChecksum"; break;
    case eInvalidData : status = "InvalidData"; break;
    default : status = "?????"; 
  }
  printf("Ping #%d received reply '%s'\n", usIdentifier, status);
#endif
}

/*
** XOR-shift PRNG which generates a 32-bit unsigned int on each call.
** See: https://en.wikipedia.org/wiki/Xorshift
** TODO Maybe this should be part of OSwald???
*/

/* The state word must be initialized to non-zero */
static uint32_t xor32state = 0x3569ac;

x_word xorshift32(x_word state)
{
        /* Algorithm "xor" from p. 4 of Marsaglia, "Xorshift RNGs" */
        uint32_t x = xor32state;
        x ^= x << 13;
        x ^= x >> 17;
        x ^= x << 5;
        return xor32state = x;
}

BaseType_t xApplicationGetRandomNumber(uint32_t *pulNumber) {
  *pulNumber = xorshift32(&xor32state);

  return pdTRUE;
}

uint32_t ulApplicationGetNextSequenceNumber(uint32_t ulSourceAddress, uint16_t usSourcePort, uint32_t ulDestinationAddress, uint16_t usDestinationPort) {
    ( void ) ulSourceAddress;
    ( void ) usSourcePort;
    ( void ) ulDestinationAddress;
    ( void ) usDestinationPort;

#ifdef DEBUG
  printf("Generating random sequence number for src %s:%d dst %s:%d\n", ipaddress_word2cstring(ulSourceAddress), usSourcePort, ipaddress_word2cstring(ulDestinationAddress), usDestinationPort);
#endif
    return xorshift32(&xor32state);
}

