#ifndef _NETWORK_H
#define _NETWORK_H

/**************************************************************************
* Copyright (c) 2015 by Chris Gray, KIFFER Ltd. All rights reserved.      *
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
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

#include <netinet/tcp.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/ioctl.h>
#include <netinet/in.h>
#include <netdb.h> 
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>

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

#ifndef OSWALD
#include "wonka.h"

/* ---- BLOCKING VERSION ...     ---- */

#define w_socket(x,y,z)		socket(x,y,z)
#define w_socketclose(s)   	close((int)s)
#define w_send(s,b,l,f)    	send(s,b,l,f)
/* [CG 20040329] Replaced by inline function (v.i.)
#define w_recv(T,s,b,l,f, timeout)    	recv(s,b,l,f)
*/

/*
 * If a timeout is specified then we have to do a lot of fancy stuff
 * instead of just forwarding to connect(s,a,l)
 */
static inline int w_connect(int s, void *a, size_t l, int t) {
    if (t == 0) {
        return connect(s, a, l);
    }

    // Get and save the current socket flags
    int flags = fcntl(s, F_GETFL, 0);
    if (flags < 0) {
      return flags;
    }

    // Temporarily set the socket non-blocking
    if(fcntl(s, F_SETFL, flags | O_NONBLOCK) < 0) {
        return -1;
    }

    // Now connect to it
    int rc = connect(s, (struct sockaddr *)a, l);
    if (rc < 0 && errno != EINPROGRESS) {
      return -1;
    }

    // If rc == 0 we connected immediately (e.g. loopback), skip the next bit
    if (rc) {
      fd_set rset, wset;
      struct timeval tv;

      FD_ZERO(&rset);
      FD_SET(s, &rset);
      wset = rset;
      tv.tv_sec = t / 1000;
      tv.tv_usec = (t % 1000) * 1000;

      // Now we perform a select with timeout to see if we can read or write.
      // The call will only return once the connect has succeeded or failed.
      rc = select(s + 1, &rset, &wset, NULL, &tv);

      // Failure -> connection failed
      if (rc < 0) {
          return -1;
      }

      // Zero -> timeout
      if (rc == 0) {
          errno = ETIMEDOUT;
          return -1;
      }

      
      // If we are really connected then read or write should be possible
      if (FD_ISSET(s, &rset) || FD_ISSET(s, &wset)) {
        // Check errno to be sure, because select is like that
        int error = 0;
        int errlen = sizeof(error);
        if (getsockopt(s, SOL_SOCKET, SO_ERROR, &error, &errlen) < 0) {
          return -1;
        }
        if (error) {
          errno = error;
          return -1;
        }
      } else {
        return -1;
      }

    }

    // Restore the current socket flags
    if (fcntl(s, F_SETFL, flags) < 0) {
      return -1;
    }

    return 0;
}

/*
 * If recv is interrupted before any data read, restart it.
 * If fails with EAGAIN, this normally means SO_RCVTIMEO was used and the
 * timeout has expired. So set timeout to -1 as a clunky way to signal to
 * PlainSocketImpl_read() that a timeout occurred.
 */
static inline int w_recv(int s, void *b, size_t l, int f, int *timeoutp) {
  int retval = recv(s,b,l,f);

  while (retval == -1 && errno == EINTR) {
    retval = recv(s,b,l,f);
  }

  if (retval == -1 && errno == EAGAIN) {
    *timeoutp = -1;
  }

  return retval;
}

static inline int w_accept(int s, struct sockaddr *a, socklen_t *l, int timeout) {
  int retval = accept(s,a,l);

  while (retval == -1 && errno == EINTR) {
    retval = accept(s,a,l);
  }

  if (retval == -1 && errno == EAGAIN) {
    *l = 0;
  }
  return retval;
}


//#define w_recvfrom(s, b, blen, f, sa, size_sa, timeout, T) recvfrom(s, b, blen, f, sa, size_sa)
static inline w_int w_recvfrom(int s, void *b, size_t blen, int f, struct sockaddr *sa, socklen_t *size_sa, int timeout, w_instance This) {

  int retval =  recvfrom(s, b, blen, f, sa, size_sa);

  while (retval == -1 && errno == EINTR) {
    retval =  recvfrom(s, b, blen, f, sa, size_sa);
  }

  if (retval == -1 && errno == EAGAIN) {
    *size_sa = 0;
  }

  return retval;
}


#define w_bind(s,a,l)      	bind(s,a,l)
#define w_listen(s,i)      	listen(s,i)
#define w_strerror(s)		strerror((int)s)
#define w_getpeername(s,a)      getpeername((int)s,a,sizeof(*a))
#define w_gethostbyname(n)      gethostbyname(n)
#define w_gethostbyname2(n,a)   gethostbyname2(n,a)
#define w_gethostname(n,l)      gethostname(n,l)
#define w_sendto(s, b, blen, f, sa, size_sa, T) sendto(s, b, blen, f, sa, size_sa)
#define w_getsockopt(s,lev,n,v,l) getsockopt((s),(lev),(n),(v),(l))
#define w_setsockopt(s,lev,n,v,l) setsockopt((s),(lev),(n),(v),(l))
#define w_getsockname(s,n,l) getsockname((s),(n),(l))

static inline int w_errno(int s) {
  return errno;
}

#else 

/*
** NON-BLOCKING version needed by oswald ...
*/

#include "wonka.h"
#include <fcntl.h>
#include "oswald.h"
#include "locks.h"
#include "asyncio.h"

//#define DMSEC 100 // # of msec waited by datagram operations
//#define AMSEC 250 // # of msec waited by the w_accept call
//#define SMSEC  50 // # of msec waited by stream socket operations

/*
** Function pointers to the network functions.
*/

extern int (*x_socket)(int domain, int type, int protocol);
extern int (*x_connect)(int sockfd, const struct sockaddr *serv_addr, socklen_t addrlen);
extern int (*x_send)(int s, const void *msg, size_t len, int flags);
extern int (*x_sendto)(int s, const void *msg, size_t len, int flags, const struct sockaddr *to, socklen_t tolen);
extern int (*x_sendmsg)(int s, const struct msghdr *msg, int flags);
extern int (*x_recv)(int s, void *buf, size_t len, int flags);
extern int (*x_recvfrom)(int s, void *buf, size_t len, int flags, struct sockaddr *from, socklen_t *fromlen);
extern int (*x_recvmsg)(int s, struct msghdr *msg, int flags);
extern int (*x_accept)(int s, struct sockaddr *addr, socklen_t *addrlen);

/*
** Create a socket in the given domain (PF_UNIX, PF_INET, etc.) and of the
** given type (SOCK+STREAM, SOCK_DGRAM, ...) using the given protocol
** (domain-dependemt, but usually 0). We try to set the socket non-blocking,
** and for datagram-type sockets we also set SO_BROADCAST.
** If all goes well, we register the socket with the async io system.
*/
static inline int w_socket(int domain, int type, int protocol)	{	
  int sock = x_socket(domain, type, protocol);
  if (sock != -1){
    if(fcntl(sock, F_SETFL, O_NONBLOCK)){
      woempa(9,"Error occured while making socket non blocking '%i'\n",errno);
      close(sock);
      sock = -1;
    }
    else if(type == SOCK_DGRAM){
      int i = 1;
      if(setsockopt(sock,SOL_SOCKET, SO_BROADCAST, &i, sizeof(int))){
        woempa(9,"Error occured while making datagramsocket able to send broadcast messages '%i'\n",errno);
        close(sock);
        sock = -1;
      }
    }
  }
  else {
    woempa(9,"Error occured while creating socket '%i'\n",errno);
  }
  if(x_async_register(sock)) {
    woempa(9, "Error registering the socket for async io '%i'\n",errno);
    close(sock);
    sock = -1;
  }
  return sock;
}

/*
** Try to connect socket 's' to the remote host/port combination specified
** by 'a' (which has length 'l'). This is allowed to take "forever",
** TODO: see if it make sense within the java.net API to apply a timeout here.
*/
static inline int w_connect(int s, const struct sockaddr *a, socklen_t l, int timeout){
	int retval = x_connect(s,a,l);
  if(retval == -1 && errno == EINPROGRESS){
    woempa(7,"connecting in progress\n");
    x_async_block(s, x_eternal);
    retval = x_connect(s,a,l);
    while(retval == -1 && errno == EALREADY){
      x_async_block(s, x_eternal);
      woempa(7,"still connecting ...\n");
      retval = connect(s,a,l);
    }
    woempa(6,"connection established %d (%d)\n",retval, errno);
    if (retval == -1 && errno == EISCONN){
      retval = 0;
    }
	}
  return retval;
}

/*
** Receive data on socket 's' into buffer 'b' (length 'l'), applying flags 'f'
** (out-of-band, peek mode, wait-all, etc.), subject to timeout '*timeoutp'.
** If timeout occurs, '*timeoutp' will be set to -1: this is an Ugly Hack(tm),
** and one which gcc is fully capable of ^&*%ing up, so it should be reviewed.
** Caveat: this API is used in various contexts (OSwald/o4p, hostsos...).
** BTW: 'This' is not used, get rid?
*/
static inline int w_recv(w_instance This, int s, void *b, size_t l, int f, int* timeoutp){
  int retval = x_recv(s,b,l,f);
  x_time timeout = *timeoutp;
  x_time now = x_time_get();
  x_time expire = timeout ? now + x_usecs2ticks(timeout * 1000) : x_eternal;

  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    x_async_block(s, timeout ? expire - now : x_eternal);
    now = x_time_get();
    if (now >= expire) {
      *timeoutp = -1;
      break;
    }

    woempa(6,"polling for new bytes ...\n");
    retval = x_recv(s,b,l,f);
  }

  return retval;  	
}

/*
** Send data on socket 's' from buffer 'b' (length 'l'), applying flags 'f'
** (out-of-band, don't wait, etc.).
*/
static inline int w_send(int s, const void *b, size_t l,int f){
  int retval = x_send(s,b,l,f);
  
  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    x_async_block(s, x_eternal);
    retval = x_send(s,b,l,f);
  }

  return retval;  	
}

/*
static inline int w_send(int s, const void *b, size_t l,int f){
  int retval = 1;
  int total = 0;
  // one problems --> 1) not ready to send
  while ((unsigned int)l != 0){
    retval = x_send(s,b,l,f);
    while (retval == -1 && (errno == eagain || errno == eintr)){
      x_async_block(s, x_eternal);
      woempa(6,"polling to send more bytes ...\n");
      retval = x_send(s,b,l,f);
    }
    if(retval == -1){
       return (total == 0 ? -1 : total);
    }
    l -= retval;
    total += retval;
    b = (void *)((char*)b + retval);
  }
  errno = 0;
  return retval;
}
*/


/*
** Accept a connection on socket 's' fromhe remote host/port combination 
** specified by 'a' (which has length 'l'). 
** TODO: see whether the timeout here (and the yucky method used to report
** timeout back to the caller, namely *l=0) really works.
*/
static inline int w_accept(int s, struct sockaddr *a, socklen_t *l, int timeout){
  int retval = x_accept(s,a,l);

  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    
    if(x_async_block(s, timeout ? x_usecs2ticks(timeout * 1000) : x_eternal)) {
      *l = 0;
      break;
    }
    
    woempa(6,"polling for incoming connection ...\n");
    retval = x_accept(s,a,l);
  }
  if (retval != -1) {
    if(x_async_register(retval)) {
      close(retval);
      return -1;
    }
  }

  return retval;  	
}


/*
** Receive from socket 'sock' into buffer 'b' (length 'blen'), applying flags 
** 'f' (out-of-band, peek mode, wait-all, etc.), from the address specified by
** 'a' (length '*l'), subject to timeout 'timeout'.
** TODO: see whether the timeout here (and the yucky method used to report
** timeout back to the caller, namely *l=0) really works.
** Caveat: this API is used in various contexts (OSwald/o4p, hostsos...).
** BTW: 'This' is not used, get rid?
*/
static inline w_int w_recvfrom(int sock, void *b, size_t blen, int f, struct sockaddr *a, socklen_t *l, int timeout, w_instance This) {
  int retval;
  x_time now = x_time_get();
  x_time expire = timeout ? now + x_usecs2ticks(timeout * 1000) : x_eternal;

  retval = x_recvfrom(sock,b,blen,f,a,l);

  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    x_async_block(sock, timeout ? expire - now : x_eternal);
    now = x_time_get();
    if (now >= expire) {
      *l = 0;
      break;
    }

    woempa(6,"waiting for datagram packet ...\n");
    retval = x_recvfrom(sock,b,blen,f,a,l);
  }

  return retval;  	
}

/*
** Send to socket 'sock' into buffer 'b' (length 'blen'), applying flags 
** 'f' (out-of-band, don't wait, etc.), from the address specified by
** 'a' (length '*l').
** BTW: 'This' is not used, get rid?
*/
static inline w_int w_sendto(int sock, const void *b, size_t blen, int f, const struct sockaddr *a, socklen_t l, w_instance This) {
  int retval;

  retval = x_sendto(sock,b,blen,f,a,l);

  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    x_async_block(sock, x_eternal);
    woempa(6,"waiting to send datagram packet ...\n");
    retval = x_sendto(sock,b,blen,f,a,l);
  }

  return retval;  	
}

/*
** Close a socket, and deregister it form the async io system.
*/
static inline w_int w_socketclose(int s) {
  int result = close(s);
  x_async_unregister(s);
  return result;
}

#include <signal.h>

// BLOCK_SIGALRM_DURING_LOOKUP = mask out SIGALRM during calls to gethostbyname[2].
// USE_REENTRANT_LOOKUP = use gethostbyname[2]_r instead of gethostbyname[2].
// From experimentation with 2.6 kernel on HirePort :
//   Linux version 2.6.11-mm2-cps (lothar@ipc1) (gcc version 3.4.3) #129 Fri Sep 16 00:40:21 CEST 2005            
// it appears that BLOCK_SIGALRM_DURING_LOOKUP does the trick, and 
// USE_REENTRANT_LOOKUP is not needed (the ..._r functions are also not 
// standard libc). TODO: rewrite all this stuff, probably using getaddrinfo().

#define BLOCK_SIGALRM_DURING_LOOKUP
//#define USE_REENTRANT_LOOKUP

static inline struct hostent *w_gethostbyname(const char *name) {
  struct hostent *h;
  int rc;
#ifdef USE_REENTRANT_LOOKUP
  struct hostent he;
  int herr;
  char *buffer = x_mem_alloc(4096); // <shrug>
#endif
#ifdef BLOCK_SIGALRM_DURING_LOOKUP
  sigset_t block_these;
  sigset_t blocked_already;
#endif

#ifdef BLOCK_SIGALRM_DURING_LOOKUP
  sigemptyset(&block_these);
  rc = sigaddset(&block_these, SIGALRM);
#ifdef DEBUG_NETWORK
  if (rc) {
    printf("sigaddset(%p, %d) returned %d\n", &block_these, SIGALRM, rc);
  }
#endif
  rc = sigprocmask(SIG_BLOCK, &block_these, &blocked_already);
#ifdef DEBUG_NETWORK
  if (rc) {
    printf("sigprocmask(SIG_BLOCK, {SIGALRM}, %p) returned %d\n", &blocked_already, rc);
  }
#endif
#endif

#ifdef USE_REENTRANT_LOOKUP
  rc = gethostbyname_r(name, &he, buffer, 4096, &h, &herr);
  if (rc) {
#ifdef DEBUG_NETWORK
    printf("gethostbyname(%s, %p, %p, 4096, ...) returned %d, h = %p, herr = %d\n", name, &he, buffer, rc, h, herr);
#endif
    h_errno = herr; // HACK
  }
  x_mem_free(buffer);
#else
  h = gethostbyname(name);
#endif

#ifdef BLOCK_SIGALRM_DURING_LOOKUP
  if (!sigismember(&blocked_already, SIGALRM)) {
    rc = sigprocmask(SIG_UNBLOCK, &block_these, NULL);
#ifdef DEBUG_NETWORK
    if (rc) {
      printf("sigprocmask(SIG_UNBLOCK, {SIGALRM}, NULL) returned %d\n", rc);
    }
#endif
  }
#endif

  return h;
}

static inline struct hostent *w_gethostbyname2(const char *name, int af) {
  struct hostent *h;
  int rc;
#ifdef USE_REENTRANT_LOOKUP
  struct hostent he;
  int herr;
  char *buffer = x_mem_alloc(4096); // <shrug>
#endif
#ifdef BLOCK_SIGALRM_DURING_LOOKUP
  sigset_t block_these;
  sigset_t blocked_already;
#endif

#ifdef BLOCK_SIGALRM_DURING_LOOKUP
  sigemptyset(&block_these);
  rc = sigaddset(&block_these, SIGALRM);
#ifdef DEBUG_NETWORK
  if (rc) {
    printf("sigaddset(%p, %d) returned %d\n", &block_these, SIGALRM, rc);
  }
#endif
  rc = sigprocmask(SIG_BLOCK, &block_these, &blocked_already);
#ifdef DEBUG_NETWORK
  if (rc) {
    printf("sigprocmask(SIG_BLOCK, {SIGALRM}, %p) returned %d\n", &blocked_already, rc);
  }
#endif
#endif

#ifdef USE_REENTRANT_LOOKUP
  rc = gethostbyname_r(name, &he, buffer, 4096, &h, &herr);
  if (rc) {
#ifdef DEBUG_NETWORK
    printf("gethostbyname(%s, %p, %p, 4096, ...) returned %d, h = %p, herr = %d\n", name, &he, buffer, rc, h, herr);
#endif
    h_errno = herr; // HACK
  }
  x_mem_free(buffer);
#else
  h = gethostbyname2(name, af);
#endif

#ifdef BLOCK_SIGALRM_DURING_LOOKUP
  if (!sigismember(&blocked_already, SIGALRM)) {
    rc = sigprocmask(SIG_UNBLOCK, &block_these, NULL);
#ifdef DEBUG_NETWORK
    if (rc) {
      printf("sigprocmask(SIG_UNBLOCK, {SIGALRM}, NULL) returned %d\n", rc);
    }
#endif
  }
#endif

  return h;
}

// non blocking calls ???
#define w_listen(s,i)      	listen(s,i)
#define w_bind(s,a,l)      	bind(s,a,l)

/**
** getpeername is currently unused
*/
//#define w_getpeername(s,a)	getpeername((int)s,a,sizeof(*a))
#define w_gethostname(n,l)	gethostname(n,l)
#define w_getsockopt(s,lev,n,v,l) getsockopt((s),(lev),(n),(v),(l))
#define w_setsockopt(s,lev,n,v,l) setsockopt((s),(lev),(n),(v),(l))
#define w_getsockname(s,n,l) getsockname((s),(n),(l))

static inline int w_errno(int s) {
  return errno;
}

/**
** for debug use only ...
*/
static inline void w_printIntAsIpString(unsigned int ip){
#ifdef DEBUG
  unsigned int * p = &ip;
  char * octet = (char*)p;
  woempa(9,"DEBUGGING %x = %x.%x.%x.%x\n",ip,octet[0],octet[1],octet[2],octet[3]);
#endif /*DEBUG */
}

static inline void w_comparePorts(unsigned int p, int port){
#ifdef DEBUG
  unsigned int * np = &p;
  char * noctet = (char*)np;
  int * hp = &port;
  char * hoctet = (char*)hp;
  woempa(9,"DEBUGGING Ports: %x = %x%x%x%x <--> %x = %x%x%x%x\n",p,noctet[0],noctet[1],noctet[2],noctet[3],
    port,hoctet[0],hoctet[1],hoctet[2],hoctet[3]);
#endif /*DEBUG */
}

#endif /* OSWALD */

void startNetwork(void);

extern int (*x_socket)(int domain, int type, int protocol);
extern int (*x_connect)(int sockfd, const struct sockaddr *serv_addr, socklen_t addrlen);
extern int (*x_send)(int s, const void *msg, size_t len, int flags);
extern int (*x_sendto)(int s, const void *msg, size_t len, int flags, const struct sockaddr *to, socklen_t tolen);
extern int (*x_sendmsg)(int s, const struct msghdr *msg, int flags);
extern int (*x_recv)(int s, void *buf, size_t len, int flags);
extern int (*x_recvfrom)(int s, void *buf, size_t len, int flags, struct sockaddr *from, socklen_t *fromlen);
extern int (*x_recvmsg)(int s, struct msghdr *msg, int flags);
extern int (*x_accept)(int s, struct sockaddr *addr, socklen_t *addrlen);
extern ssize_t (*x_read)(int fd, void *buf, size_t count);
extern ssize_t (*x_write)(int fd, const void *buf, size_t count);

#endif /* _NETWORK_H */

