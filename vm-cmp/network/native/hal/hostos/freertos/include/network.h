#ifndef _NETWORK_H
#define _NETWORK_H

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

#define  BOARD_USE_TCP   1
#include "board_init.h"
#include "FreeRTOS.h"
#include "FreeRTOS_Sockets.h"
#include "FreeRTOS_IP.h"
#include <sys/types.h>
#include <sys/time.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>

#ifdef DEBUG
#define DEBUG_NETWORK
#endif

#ifdef DEFINE_NETWORK
#include <stdio.h>
#endif

/*
** ---- Network Abstraction Layer ----
**
** This file represents the network interface of Wonka.
**
** ---- FreeRTOS version, using FreeRTOS+TCP ----
*/

#include "wonka.h"

// Type used to select a socket
// (in POSIX this is an int, but in FreeRTOS it is a pointer)
#define w_sock      Socket_t

#define w_htons                 FreeRTOS_htons
#define w_ntohs                 FreeRTOS_ntohs
#define w_htonl                 FreeRTOS_htonl
#define w_ntohl                 FreeRTOS_ntohl
#define w_socket(x,y,z)		FreeRTOS_socket(x,y,z)
#define w_shutdown(x,y)         FreeRTOS_shutdown(x,y)
#define w_socketclose(s)	FreeRTOS_closesocket(s)
#define w_send(s,b,l,f)    	FreeRTOS_send(s,b,l,f)
#define w_sockaddr              freertos_sockaddr

#define AF_INET     FREERTOS_AF_INET
#define PF_INET     FREERTOS_AF_INET
#define SOCK_STREAM FREERTOS_SOCK_STREAM
#define IPPROT_TCP  FREERTOS_IPPROTO_TCP
#define SOL_SOCKET  FREERTOS_SOL_SOCKET
#define SO_RCVBUF   FREERTOS_SO_RCVBUF
#define SO_SNDBUF   FREERTOS_SO_SNDBUF
// the following are not used by FreeRTOS+TCP
#define IPPROTO_IP  0
#define MSG_NOSIGNAL 0
#define MSG_OOB 0
#define SOL_SOCKET 0

w_int w_connect(w_sock s, void *a, w_size l, w_int t);
w_int w_recv(w_sock s, void *b, size_t l, w_int f, w_int *timeoutp);
w_sock w_accept(w_sock s, struct w_sockaddr *a, unsigned *l, w_int timeout);
w_int w_recvfrom(w_sock s, void *b, size_t blen, w_int f, struct w_sockaddr *sa, unsigned *size_sa, w_int timeout, w_instance This);


#define w_bind(s,a,l)      	FreeRTOS_bind(s,a,l)
#define w_listen(s,i)      	FreeRTOS_listen(s,i)
#define w_strerror(s)		FreeRTOS_strerror((int)s)
#define w_getpeername(s,a)      FreeRTOS_getpeername((int)s,a,sizeof(*a))
#define w_gethostbyname(n)      FreeRTOS_gethostbyname(n)
#define w_gethostbyname2(n,a)   FreeRTOS_gethostbyname2(n,a)

int w_gethostname(char *name, size_t len);
int w_sethostname(const char *name, size_t len);

#define w_sendto(s, b, blen, f, sa, size_sa, T) FreeRTOS_sendto(s, b, blen, f, sa, size_sa)
#define w_getsockopt(s,lev,n,v,l) FreeRTOS_getsockopt((s),(lev),(n),(v),(l))
#define w_setsockopt(s,lev,n,v,l) FreeRTOS_setsockopt((s),(lev),(n),(v),(l))
#define w_getsockname(s,n,l) FreeRTOS_getsockname((s),(n),(l))

// TODO FreeRTOS+TCP doesn't have an errno ...
/*
static inline w_int w_errno(int s) {
  return FreeRTOS_errno;
}
*/

extern w_int FreeRTOS_IPAddress;
extern w_word FreeRTOS_NetMask;
extern w_word FreeRTOS_GatewayAddress;
extern w_word FreeRTOS_DNSServerAddress;

void startNetwork(void);

// TODO no socklen_t for parameter l?
extern w_int (*x_socket)(w_int domain, w_int type, w_int protocol);
extern w_int (*x_connect)(w_int sockfd, const struct w_sockaddr *serv_addr, unsigned addrlen);
extern w_int (*x_send)(w_int s, const void *msg, size_t len, w_int flags);
extern w_int (*x_sendto)(w_int s, const void *msg, size_t len, w_int flags, const struct w_sockaddr *to, unsigned tolen);
extern w_int (*x_sendmsg)(w_int s, const struct msghdr *msg, w_int flags);
extern w_int (*x_recv)(w_int s, void *buf, size_t len, w_int flags);
extern w_int (*x_recvfrom)(w_int s, void *buf, size_t len, w_int flags, struct w_sockaddr *from, unsigned *fromlen);
extern w_int (*x_recvmsg)(w_int s, struct msghdr *msg, w_int flags);
extern w_int (*x_accept)(w_int s, struct w_sockaddr *addr, unsigned *addrlen);
extern ssize_t (*x_read)(w_int fd, void *buf, size_t count);
extern ssize_t (*x_write)(w_int fd, const void *buf, size_t count);

// Functions we need to supply for FreeRTOS+TCP
extern void vApplicationIPNetworkEventHook(eIPCallbackEvent_t eNetworkEvent);
extern void tcpPingSendHook(uint32_t address);
extern void  vApplicationPingReplyHook(ePingReplyStatus_t eStatus, uint16_t usIdentifier);
extern BaseType_t xApplicationGetRandomNumber(uint32_t *pulNumber);
extern uint32_t ulApplicationGetNextSequenceNumber(uint32_t ulSourceAddress, uint16_t usSourcePort, uint32_t ulDestinationAddress, uint16_t usDestinationPort);

#endif /* _NETWORK_H */

