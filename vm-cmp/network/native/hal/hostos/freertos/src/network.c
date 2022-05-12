/**************************************************************************
* Copyright (c) 2020, 2022 by KIFFER Ltd. All rights reserved.            *
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

void startNetwork(void) {
  // nothing to do???
}

void vApplicationIPNetworkEventHook(eIPCallbackEvent_t eNetworkEvent) {
  printf("Network is %s\n", eNetworkEvent == eNetworkUp ? "UP" : "DOWN");
  tcp_mac_t mac;
  tcp_return_code_t rc = tcp_get_mac(&mac);
  if (rc == TCP_SUCCESS) {
    printf("MAC address = %02x %02x %02x %02x %02x %02x\n", mac.puMAC[0], mac.puMAC[1], mac.puMAC[2], mac.puMAC[3], mac.puMAC[4], mac.puMAC[5]);
  }
  else {
    printf("MAC address not available\n");
  }
}

void tcpPingSendHook(uint32_t address) {
}

void  vApplicationPingReplyHook(ePingReplyStatus_t eStatus, uint16_t usIdentifier) {
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

    return xorshift32(&xor32state);
}

