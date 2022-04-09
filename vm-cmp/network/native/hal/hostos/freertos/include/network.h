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

#include <sys/types.h>
#include <sys/time.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include "FreeRTOS_sockets.h"

#ifdef DEBUG
#define DEBUG_NETWORK
#endif

//#define DEBUG_NETWORK

#ifdef DEFINE_NETWORK
#include <stdio.h>
#endif

/*
** ---- Network Abstraction Layer ----
**
** This file represents the network interface of Wonka.
**
** ---- LINUX Version, using standard Linux TCP/IP ----
*/

#if __BYTE_ORDER == __LITTLE_ENDIAN
static inline int w_switchPortBytes(int port){
  return ((port & 0x00ff) << 8) | ((port & 0xff00) >> 8);
}
#else
#define w_switchPortBytes(p) (p)
#endif

#include "wonka.h"

#define w_socket(x,y,z)		FreeRTOS_socket(x,y,z)
// TODO maybe we need this:
//  	{FreeRTOS_shutdown( xSocket, FREERTOS_SHUT_RDWR ); while( FreeRTOS_recv( xSocket, pcBufferToTransmit, xTotalLengthToSend, 0 ) >= 0 ) vTaskDelay( pdTICKS_TO_MS( 250 ) ); FreeRTOS_closesocket( xSocket );}
#define w_socketclose(s)	FreeRTOS_closesocket(s)
#define w_send(s,b,l,f)    	FreeRTOS_send(s,b,l,f)

/*
 * TODO
 * If a timeout is specified then we have to do a lot of fancy stuff
 * instead of just forwarding to connect(s,a,l)
 */
static inline int w_connect(int s, void *a, size_t l, int t) {
//    if (t == 0) {
        return FreeRTOS_connect(s, a, l);
//    }
// otherwise ... TODO
}

/*
 * If recv is interrupted before any data read, restart it.
 * If fails with EAGAIN, this normally means SO_RCVTIMEO was used and the
 * timeout has expired. So set timeout to -1 as a clunky way to signal to
 * PlainSocketImpl_read() that a timeout occurred.
 */
static inline int w_recv(int s, void *b, size_t l, int f, int *timeoutp) {
  int retval = FreeRTOS_recv(s,b,l,f);

  while (retval == -1 && errno == EINTR) {
    retval = FreeRTOS_recv(s,b,l,f);
  }

  if (retval == -1 && errno == EAGAIN) {
    *timeoutp = -1;
  }

  return retval;
}

// TODO no socklen_t for parameter l?
static inline int w_accept(int s, struct sockaddr *a, unsigned *l, int timeout) {
  int retval = FreeRTOS_accept(s,a,l);

  while (retval == -1 && errno == EINTR) {
    retval = FreeRTOS_accept(s,a,l);
  }

  if (retval == -1 && errno == EAGAIN) {
    *l = 0;
  }
  return retval;
}


// TODO no socklen_t for parameter l?
//#define w_recvfrom(s, b, blen, f, sa, size_sa, timeout, T) recvfrom(s, b, blen, f, sa, size_sa)
static inline w_int w_recvfrom(int s, void *b, size_t blen, int f, struct sockaddr *sa, unsigned *size_sa, int timeout, w_instance This) {

  int retval =  FreeRTOS_recvfrom(s, b, blen, f, sa, size_sa);

  while (retval == -1 && errno == EINTR) {
    retval =  FreeRTOS_recvfrom(s, b, blen, f, sa, size_sa);
  }

  if (retval == -1 && errno == EAGAIN) {
    *size_sa = 0;
  }

  return retval;
}


#define w_bind(s,a,l)      	FreeRTOS_bind(s,a,l)
#define w_listen(s,i)      	FreeRTOS_listen(s,i)
#define w_strerror(s)		FreeRTOS_strerror((int)s)
#define w_getpeername(s,a)      FreeRTOS_getpeername((int)s,a,sizeof(*a))
#define w_gethostbyname(n)      FreeRTOS_gethostbyname(n)
#define w_gethostbyname2(n,a)   FreeRTOS_gethostbyname2(n,a)
// TODO find out if the board has a "hostname" somewhere
#define w_gethostname(n,l)      ({strncpy("im4000",n,l); 0;})
#define w_sendto(s, b, blen, f, sa, size_sa, T) FreeRTOS_sendto(s, b, blen, f, sa, size_sa)
#define w_getsockopt(s,lev,n,v,l) FreeRTOS_getsockopt((s),(lev),(n),(v),(l))
#define w_setsockopt(s,lev,n,v,l) FreeRTOS_setsockopt((s),(lev),(n),(v),(l))
#define w_getsockname(s,n,l) FreeRTOS_getsockname((s),(n),(l))

// TODO FreeRTOS+TCP doesn't have an errno ...
/*
static inline int w_errno(int s) {
  return FreeRTOS_errno;
}
*/

void startNetwork(void);

// TODO no socklen_t for parameter l?
extern int (*x_socket)(int domain, int type, int protocol);
extern int (*x_connect)(int sockfd, const struct sockaddr *serv_addr, unsigned addrlen);
extern int (*x_send)(int s, const void *msg, size_t len, int flags);
extern int (*x_sendto)(int s, const void *msg, size_t len, int flags, const struct sockaddr *to, unsigned tolen);
extern int (*x_sendmsg)(int s, const struct msghdr *msg, int flags);
extern int (*x_recv)(int s, void *buf, size_t len, int flags);
extern int (*x_recvfrom)(int s, void *buf, size_t len, int flags, struct sockaddr *from, unsigned *fromlen);
extern int (*x_recvmsg)(int s, struct msghdr *msg, int flags);
extern int (*x_accept)(int s, struct sockaddr *addr, unsigned *addrlen);
extern ssize_t (*x_read)(int fd, void *buf, size_t count);
extern ssize_t (*x_write)(int fd, const void *buf, size_t count);

#endif /* _NETWORK_H */

