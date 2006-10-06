#ifndef _NETWORK_H
#define _NETWORK_H

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
**************************************************************************/

/*
** $Id: network.h,v 1.3 2005/11/29 10:02:36 cvs Exp $
*/

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/ioctl.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <netdb.h> 
#include <unistd.h>
#include <errno.h>
#include <string.h>

#ifdef UNC20
#undef errno
#endif

/*
**
** ---- Network Abstraction Layer ----
**
** This file represents the network interface of Wonka.
**
** ---- NetBSD Version, using standard NetBSD TCP/IP ----
** ---- BLOCKING VERSION ...     ----
#define w_socket(x,y,z)		socket(x,y,z)
#define w_connect(s,a,l)	connect(s,a,l) 
#define w_socketclose(s)   	close((int)s)
#define w_send(s,b,l,f)    	send(s,b,l,f)
#define w_recv(s,b,l,f, timeout)    	recv(s,b,l,f)
#define w_accept(s,a,l,timeout)    	accept(s,a,l)
#define w_bind(s,a,l)      	bind(s,a,l)
#define w_listen(s,i)      	listen(s,i)
#define w_strerror(s)		strerror((int)s)
#define w_getpeername(s,a)	getpeername((int)s,a,sizeof(*a))
#define w_getsockname(s,a,l)	getsockname(s,a,l)
#define w_gethostbyname(n)	gethostbyname(n)
#define w_gethostbyname2(n,a)	gethostbyname2(n,a)
#define w_gethostname(n,l)	gethostname(n,l)
#define w_recvfrom(s, b, blen, f, sa, size_sa, timeout) recvfrom(s, b, blen, f, sa, size_sa)
#define w_sendto(s, b, blen, f, sa, size_sa) sendto(s, b, blen, f, sa, size_sa)
#define w_getsockopt(s,lev,n,v,l) getsockopt((s),(lev),(n),(v),(l))
#define w_setsockopt(s,lev,n,v,l) setsockopt((s),(lev),(n),(v),(l))
#define w_getsockname(s,n,l) getsockname((s),(n),(l))

static inline int w_errno(int s) {
  return errno;
}
*/

/**
** NON-BLOCKING version needed by oswald ...
*/

#include "wonka.h"
#include <fcntl.h>
#include "oswald.h"
#include "locks.h"

#define DMSEC 100 // # of msec waited by datagram operations
#define AMSEC 250 // # of msec waited by the w_accept call
#define SMSEC  50 // # of msec waited by stream socket operations

static inline int w_socket(int x,int y,int z)	{	
  int sock = socket(x,y,z);
  if (sock != -1){
    woempa(7, "Setting socket %d non-blocking\n", sock);
    if (fcntl(sock, F_SETFL, O_NONBLOCK)){
      woempa(9,"Error occured while making socket non blocking '%i'\n",errno);
      close(sock);
      sock = -1;
    }
    else if (y == SOCK_DGRAM){
      int i = 1;
      woempa(7, "Setting socket %d broadcast\n", sock);
      if(setsockopt(sock, SOL_SOCKET, SO_BROADCAST, &i, sizeof(int))){
        woempa(9,"Error occured while making datagram socket able to send broadcast messages '%i'\n",errno);
        close(sock);
        sock = -1;
      }
    }
  }
  else {
    woempa(9,"Error occured while creating socket '%i'\n",errno);
  }
  return sock;
}

static inline int w_connect(int s, const struct sockaddr *a, socklen_t l){
  int retval = connect(s,a,l);
  if (retval == -1) {
    woempa(1, "connect(%d, %p, %d) returned -1, errno is %d ('%s')\n", s, a, l, errno, strerror(errno));
    if (errno == EINPROGRESS){
    woempa(1,"connecting in progress\n");
    retval = connect(s,a,l);
    while (retval == -1 && errno == EALREADY){
      x_thread_sleep(x_usecs2ticks(SMSEC * 1000));
      woempa(7,"still connecting ...\n");
      retval = connect(s,a,l);
    }
    woempa(6,"connection established %d (%d)\n",retval, errno);
    if (retval == -1 && errno == EISCONN){
      retval = 0;
    }
    }
    else {
      woempa(7,"connecting failed, errno = '%s'\n", strerror(errno));
    }
  }
  return retval;
}

static inline int w_recv(w_instance This, int s, void *b, size_t l, int f, int* timeoutp){
  int retval = recv(s,b,l,f);
  int waited = 0;
  int timeout = *timeoutp;

  //enterMonitor(This);

  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    if(timeout && waited > timeout){
      *timeoutp = 0;
      break;
    }
    waited += SMSEC;

    //waitMonitor(This, x_usecs2ticks(SMSEC * 1000));
    x_thread_sleep(x_usecs2ticks(SMSEC * 1000));

    woempa(6,"polling for new bytes ...\n");
    retval = recv(s,b,l,f);
  }

  //exitMonitor(This);

  return retval;  	
}
static inline int w_send(int s, const void *b, size_t l,int f){
  int retval = 1;
  // one problems --> 1) not ready to send
  while ((unsigned int)retval != 0){
    retval = send(s,b,l,f);
    while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
      woempa(7, "ERRNO = %d (%s), wait %d msec and try again\n", errno, strerror(errno), SMSEC);
      x_thread_sleep(x_usecs2ticks(SMSEC * 1000));
      retval = send(s,b,l,f);
    }
    if(retval == -1){
      woempa(7, "ERRNO = %d (%s), returning -1\n", errno, strerror(errno));
       return -1;
    }
    l -= retval;
    b = (void *)((char*)b + retval);
  }
  return retval;
}

static inline int w_accept(w_instance This, int s, struct sockaddr *a, socklen_t *l, int timeout){
  int retval = accept(s,a,l);
  int waited = 0;

  enterMonitor(This);

  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    if(timeout && waited > timeout){
      *l = 0;
      break;
    }
    waited += AMSEC;

    waitMonitor(This, x_usecs2ticks(AMSEC * 1000));

    woempa(6,"polling for incoming connection ...\n");
    retval = accept(s,a,l);
  }
  if (retval != -1){
    if(fcntl(retval, F_SETFL, O_NONBLOCK)){
      //woempa(9,"Error occured while making socket non blocking in accecpt '%s'\n",strerror(errno));
      close(retval);
    }
  }

  exitMonitor(This);

  return retval;  	
}

static inline w_int w_recvfrom(int sock, void *b, size_t blen, int f, struct sockaddr *a, socklen_t *l, int timeout, w_instance This) {
  int waited = 0;
  int retval;

  enterMonitor(This);

  retval = recvfrom(sock,b,blen,f,a,l);

  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    if(timeout && waited > timeout){
      *l = 0;
      break;
    }
    waited += DMSEC;
    waitMonitor(This, x_usecs2ticks(DMSEC * 1000));
    woempa(6,"waiting for datagram packet ...\n");
    retval = recvfrom(sock,b,blen,f,a,l);
  }

  exitMonitor(This);
  return retval;  	
}

static inline w_int w_sendto(int sock, const void *b, size_t blen, int f, const struct sockaddr *a, socklen_t l, w_instance This) {
  int retval;

  enterMonitor(This);

  retval = sendto(sock,b,blen,f,a,l);

  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    waitMonitor(This, x_usecs2ticks(DMSEC * 1000));
    woempa(6,"waiting to send datagram packet ...\n");
    retval = sendto(sock,b,blen,f,a,l);
  }

  exitMonitor(This);

  return retval;  	
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
  w_word *firstaddr;
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
  rc = sigprocmask(SIG_BLOCK, &block_these, &blocked_already);
#endif

#ifdef USE_REENTRANT_LOOKUP
  rc = gethostbyname_r(name, &he, buffer, 4096, &h, &herr);
  if (rc) {
    h_errno = herr; // HACK
  }
  x_mem_free(buffer);
#else
  h = gethostbyname(name);
  firstaddr = (w_word*)h->h_addr_list[0];
#endif

#ifdef BLOCK_SIGALRM_DURING_LOOKUP
  if (!sigismember(&blocked_already, SIGALRM)) {
    rc = sigprocmask(SIG_UNBLOCK, &block_these, NULL);
  }
#endif

  return h;
}

static inline struct hostent *w_gethostbyname2(const char *name, int af) {
  struct hostent *h;
  w_word *firstaddr;
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
  rc = sigprocmask(SIG_BLOCK, &block_these, &blocked_already);
#endif

#ifdef USE_REENTRANT_LOOKUP
  rc = gethostbyname_r(name, &he, buffer, 4096, &h, &herr);
  if (rc) {
    h_errno = herr; // HACK
  }
  x_mem_free(buffer);
#else
  h = gethostbyname2(name, af);
  firstaddr = (w_word*)h->h_addr_list[0];
#endif

#ifdef BLOCK_SIGALRM_DURING_LOOKUP
  if (!sigismember(&blocked_already, SIGALRM)) {
    rc = sigprocmask(SIG_UNBLOCK, &block_these, NULL);
  }
#endif

  return h;
}


// non blocking calls ???
#define w_listen(s,i)      	listen(s,i)
#define w_socketclose(s)   	close((int)s)
#define w_bind(s,a,l)      	bind(s,a,l)

/**
** the pointer returned by strerror is only valid until the next call to strerror
** this is not thread safe and therefore should not be used ...
*/
//#define w_strerror(s)		    strerror((int)s)

/**
** getpeername is currently unused
*/
#define w_getpeername(s,a)	getpeername((int)s,a,sizeof(*a))
#define w_gethostname(n,l)	gethostname(n,l)
#define w_getsockopt(s,lev,n,v,l) getsockopt((s),(lev),(n),(v),(l))
#define w_setsockopt(s,lev,n,v,l) setsockopt((s),(lev),(n),(v),(l))
#define w_getsockname(s,n,l) getsockname((s),(n),(l))

static inline int w_switchPortBytes(int port){
  return ((port & 0x00ff) << 8) | ((port & 0xff00) >> 8);
}

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
  woempa(9,"DEBUGGING %x = %d.%d.%d.%d\n",ip,octet[0],octet[1],octet[2],octet[3]);
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

static inline void startNetwork(void) {
}

#endif /* _NETWORK_H */
