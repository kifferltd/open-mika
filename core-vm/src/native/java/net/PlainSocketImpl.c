/**************************************************************************
* Copyright (c) 2020, 2021, 2022 by KIFFER Ltd. All rights reserved.      *
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

#include <string.h>
#include <errno.h>
#include <unistd.h>
#include <sys/time.h>

#include "arrays.h"
#include "core-classes.h"
#include "exception.h"
#include "fields.h"
#include "network.h"

/**
** SocketImpl use calls to functions defined by network.h. All these calls work with a descriptor
** the descriptor is stored in the wotsit of SocketImpl ! We don't use the FileDescritor Object,
** it will be left null ... . This means other implementions should use this wotsit value to make calls to
** the network.  Note: this is not the way to do it according to java API.  Although using a FileDescriptor
** Almost implies using native code. By leaving that descriptor null we prevent it from being misused ...
*/

w_int PlainSocketImpl_clear(w_instance ThisImpl) {
  w_sock sock = (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);
  w_int res = 0;	

  if (getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    w_int minus1 = -1;
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: closing %d\n", (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit));
    }
    res = w_socketclose(sock);

    setBooleanField(ThisImpl, F_PlainSocketImpl_open, WONKA_FALSE);
    setWotsitField(ThisImpl, F_PlainSocketImpl_wotsit, minus1);
  }
  else {
    woempa(1, "socket %p was already closed !\n", ThisImpl);
  }

  return res;
}

void PlainSocketImpl_close(w_thread thread , w_instance ThisImpl) {
  if (PlainSocketImpl_clear(ThisImpl) == -1) {
    throwException(thread, clazzIOException, "closing socket failed with %d: '%s'", errno, strerror(errno));
  }
}

void PlainSocketImpl_finalize(w_thread thread , w_instance ThisImpl) {
  PlainSocketImpl_clear(ThisImpl);
}

w_int PlainSocketImpl_getSocket(w_thread thread , w_instance ThisImpl){

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    throwException(thread, clazzSocketException, "socket is not open or uninitialized");
  }

  return (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);
}

void PlainSocketImpl_nativeCreate(w_thread thread , w_instance ThisImpl) {
  w_sock sock;
#ifdef PF_INET6
  w_int pf = (getBooleanField(ThisImpl, F_PlainSocketImpl_ipv6) ? PF_INET6 : PF_INET);
#else
  w_int pf = PF_INET;
#endif

  if(getBooleanField(ThisImpl, F_PlainSocketImpl_open)){
    throwException(thread, clazzIOException, "socket is already open");
  }
  else {
    sock = w_socket (pf, SOCK_STREAM, IPPROT_TCP);
    setWotsitField(ThisImpl, F_PlainSocketImpl_wotsit, sock);	
    woempa(1, "%j : socket is %d\n", ThisImpl, sock);

    if (sock == -1) {
      throwException(thread, clazzIOException, "socket errno %d '%s'", errno, strerror(errno));
      woempa(9, "Not able to create a socket for %p\n", ThisImpl);
    }
    else {
      setBooleanField(ThisImpl, F_PlainSocketImpl_open, WONKA_TRUE);
    }
  }
}

// TODO stop this ifdef madness!!!
#ifdef FREERTOS
void PlainSocketImpl_connect(w_thread thread , w_instance ThisImpl, w_int timeout) {

  w_instance address = getReferenceField(ThisImpl, F_SocketImpl_address); 
  w_int port = getIntegerField(ThisImpl, F_SocketImpl_port);

  if (!address) {
    throwException(thread, clazzConnectException, "no IP address");

    return;
  }

  struct freertos_sockaddr sa;
  w_int res;
  w_sock sock = (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    woempa(9, "socket %i was closed\n", sock);
    throwException(thread, clazzIOException, "socket was closed or uninitialized");
    return;
  }  	  	

  memset(&sa, 0, sizeof(sa));
  sa.sin_family = PF_INET;
  sa.sin_addr = FreeRTOS_htonl(getIntegerField(address, F_InetAddress_address));
  sa.sin_port = FreeRTOS_htons(port);

  woempa (7,"INET: connect, port %d addr %lx\n", sa.sin_port, sa.sin_addr);
  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    unsigned char *a = (unsigned char *)&sa.sin_addr;
    printf("Socket: connecting to %d.%d.%d.%d:%d\n", a[0], a[1], a[2], a[3], sa.sin_port);
  }

  res = w_connect (sock, &sa, sizeof(struct freertos_sockaddr), timeout);
  woempa (6,"socketfd = %d, connect result = %d\n",sock, res);

  if (res < 0) {
    woempa(9,"ERROR in connect = %d\n", res);
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: connection failed: %s\n", strerror(errno));
    }
    // TODO distiguish betewwen clazzSocketTimeoutException and clazzConnectException cases
    throwException(thread, clazzConnectException, "socket connect error %d", res);

    return;
  }

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: connected, id = %p\n", sock);
  }
  setIntegerField(ThisImpl, F_SocketImpl_port, port);
  setReferenceField(ThisImpl, address, F_SocketImpl_address);

}
#else
void PlainSocketImpl_connect(w_thread thread , w_instance ThisImpl, w_int timeout) {

  w_instance address = getReferenceField(ThisImpl, F_SocketImpl_address); 
  w_int port = getIntegerField(ThisImpl, F_SocketImpl_port);

  if (!address) {
    throwException(thread, clazzConnectException, "no IP address");

    return;
  }

  struct sockaddr * sa = NULL;
  w_size sa_size = 0;
    
  struct sockaddr_in  sa4;
#ifdef PF_INET6
  struct sockaddr_in6 sa6;
#endif

  w_int res;
  w_sock sock = (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    woempa(9, "socket %i was closed\n", sock);
    throwException(thread, clazzIOException, "socket was closed or uninitialized");
    return;
  }  	  	

#ifdef PF_INET6
  if (!(getBooleanField(ThisImpl, F_PlainSocketImpl_ipv6))) { 
    memset(&sa4, 0, sizeof(sa4));
    sa4.sin_addr.s_addr = htonl(getIntegerField(address, F_InetAddress_address));
    sa4.sin_family = AF_INET;
    sa4.sin_port = htons(port);

    woempa (7,"INET: connect, port %d addr %lx\n", sa4.sin_port, sa4.sin_addr.s_addr);
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      unsigned char *a = (unsigned char *)&sa4.sin_addr.s_addr;
      printf("Socket: connecting to %d.%d.%d.%d:%d\n", a[0], a[1], a[2], a[3], port);
    }

    sa = (struct sockaddr *)&sa4;
    sa_size = sizeof(struct sockaddr_in);
  }
  else {
    memset(&sa6, 0, sizeof(sa6));
    memcpy(&sa6.sin6_addr, instance2Array_byte(getReferenceField(address, F_Inet6Address_ipaddress)), 16);
    sa6.sin6_family = AF_INET6;
    sa6.sin6_port = w_htons(port);

    woempa (7,"INET6: connect, port %d addr %p\n", sa6.sin6_port, sa6.sin6_addr);
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: connecting (IPv6)\n");
    }

    sa = (struct sockaddr *)&sa6;
    sa_size = sizeof(struct sockaddr_in6);
  }
#else
  memset(&sa4, 0, sizeof(sa4));
  sa4.sin_addr.s_addr = htonl(getIntegerField(address, F_InetAddress_address));
  sa4.sin_family = AF_INET;
  sa4.sin_port = w_htons(port);

  woempa (7,"INET: connect, port %d addr %lx\n", sa4.sin_port, sa4.sin_addr.s_addr);
  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    unsigned char *a = (unsigned char *)&sa4.sin_addr.s_addr;
    printf("Socket: connecting to %d.%d.%d.%d:%d\n", a[0], a[1], a[2], a[3], sa4.sin_port);
  }

  sa = (struct sockaddr *)&sa4;
  sa_size = sizeof(struct sockaddr_in);
#endif

  res = w_connect (sock, sa, sa_size, timeout);
  woempa (6,"socketfd = %d, connect result = %d\n",sock, res);

  if (res == -1) {
    woempa(9,"ERROR in connect = %s\n", strerror(errno));
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: connection failed: %s\n", strerror(errno));
    }
    if (errno == ETIMEDOUT) {
      throwException(thread, clazzSocketTimeoutException, NULL);
    }
    else {
      throwException(thread, clazzConnectException, "socket connect errno %d '%s'", errno, strerror(errno));
    }
    return;
  }

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: connected, id = %d\n", sock);
  }
  setIntegerField(ThisImpl, F_SocketImpl_port, port);
  setReferenceField(ThisImpl, address, F_SocketImpl_address);

#ifdef PF_INET6
  if (!(getBooleanField(ThisImpl, F_PlainSocketImpl_ipv6))) { 
    socklen_t namelen = sizeof(sa4);
    res = w_getsockname(sock , (struct sockaddr *)&sa4 , &namelen);
    if (res == -1) {
      //woempa(9,"ERROR in connect = %s\n", w_strerror((int)w_errno(sock)));
      throwException(thread, clazzConnectException, "getsockname errno %d '%s'", errno, strerror(errno));
    }
    else {
      setIntegerField(ThisImpl, F_SocketImpl_localport, w_ntohs(sa4.sin_port));
    }
  }
  else {
    socklen_t namelen = sizeof(sa6);
    res = w_getsockname(sock , (struct sockaddr *)&sa6 , &namelen);

    if (res == -1) {
      woempa(9,"ERROR in connect/getsockname = %s\n", strerror(errno)); 	
      throwException(thread, clazzConnectException, "getsockname errno %d '%s'", errno, strerror(errno));
    }
    else {	
      setIntegerField(ThisImpl, F_SocketImpl_localport, w_ntohs(sa6.sin6_port));
    }
  }
#else
  socklen_t namelen = sizeof(sa4);
  res = w_getsockname(sock , (struct sockaddr *)&sa4 , &namelen);
  if (res == -1) {
    //woempa(9,"ERROR in connect = %s\n", w_strerror((int)w_errno(sock)));
    throwException(thread, clazzConnectException, "getsockname errno %d '%s'", errno, strerror(errno));
  }
  else {  
    setIntegerField(ThisImpl, F_SocketImpl_localport, w_ntohs(sa4.sin_port));
  }
#endif
}
#endif
  
#ifndef FREERTOS
// Our signal handler doesn't actually do anything, it's just there so that
// SIGUSR1 has the desired effect of interrupting accept() or read().
static sigusr_handler(int sig) {
  woempa(1, "Received signal %d\n", sig);
}
#endif

w_int PlainSocketImpl_read(w_thread thread , w_instance ThisImpl, w_instance byteArray, w_int off, w_int length) {

  w_sock sock = (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);
  w_int res;	
  w_int arrayLength;
  w_int timeout;
  w_int i;
  unsigned char c;

#ifndef FREERTOS
  struct sigaction action, savedaction;
#endif
  
  woempa(7, "reading %i bytes from SocketImpl %p (desp %i)\n", length, ThisImpl, sock);

  if (! byteArray) {
    throwException(thread, clazzConnectException, "no IP address");
    return -1;
  }
  else {
    arrayLength = instance2Array_length(byteArray);
    if ((off < 0) || (length < 0) || (off > arrayLength - length)) {
      throwException(thread, clazzArrayIndexOutOfBoundsException, "out of bounds; size = %d, offset = %d, length = %d", arrayLength, off, length);
      return -1;
    }
  }

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    throwException(thread, clazzIOException, "socket closed or uninitialized");
    return -1;  	
  }

  if (length == 0) {
    return 0;
  }

  /*
  ** All seems OK, let's read...
  */
#ifndef FREERTOS
  action.sa_handler = sigusr_handler;
  sigemptyset (&action.sa_mask);
  action.sa_flags = 0;
  sigaction(SIGUSR1, &action, &savedaction);
#endif
  timeout = getIntegerField(ThisImpl, F_PlainSocketImpl_timeout);
  woempa(7, "Calling w_recv(%d,%j[%d],%d,0,%d)\n", sock, byteArray, off, length, 0, timeout);
  res = w_recv(sock, instance2Array_byte(byteArray) + off, (w_word)length, 0, &timeout);
#ifndef FREERTOS
  sigaction(SIGUSR1, &savedaction, NULL);
#endif
  if (res < 0) {
    if( timeout == -1) {
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d read timed out\n", sock);
      }
      throwException(thread, clazzSocketTimeoutException, "");
    }
    else {
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d read failed: %s\n", sock, strerror(errno));
      }
#ifdef FREERTOS
      throwException(thread, clazzIOException, "recv error %d", res);
#else
      throwException(thread, clazzIOException, "recv errno %d '%s'", errno, strerror(errno));
#endif
    }
  }

  if (isSet(verbose_flags, VERBOSE_FLAG_TRAFFIC)) {
    printf("Traffic: read on id %d: length %d\n", sock, res);
    for (i = 0; i < res; ++i) {
      c = instance2Array_byte(byteArray)[off + i];
      printf("%c", c >= 32 && c < 255 && (c < 127 || c >= 160) ? c : '.');
    }
    printf("\n");
  }

  woempa(7, "reading %i bytes from SocketImpl %p --> got %i\n", length, ThisImpl, res);
  if (res == 0) { 
    res--; 
  }

  return res;
}

void PlainSocketImpl_write(w_thread thread, w_instance ThisImpl, w_instance byteArray, w_int off, w_int length) {

  w_sock sock = (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);
  w_int res;	
  w_int arrayLength;
  w_int i;
  unsigned char c;

  woempa(1, "writing %i bytes to SocketImpl %p\n", length, ThisImpl);

  if (! byteArray) {
    throwException(thread, clazzNullPointerException, "buffer is NULL");
    return;
  }
  else {
    arrayLength = instance2Array_length(byteArray);
    if ((off < 0) || (length < 0) || (off > arrayLength - length)) {
      throwException(thread, clazzArrayIndexOutOfBoundsException, "out of bounds; size = %d, offset = %d, length = %d", arrayLength, off, length);
      return;
    }
  }

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    throwException(thread, clazzIOException, "socket closed or uninitialized");
    return;  	
  }

  if (length == 0) {
    return;
  }

  res = w_send(sock, instance2Array_byte(byteArray) + off, (w_word)length, MSG_NOSIGNAL);

  if (isSet(verbose_flags, VERBOSE_FLAG_TRAFFIC)) {
    printf("Traffic: write on id %d: length %d\n", sock, res);
    for (i = 0; i < res; ++i) {
      c = instance2Array_byte(byteArray)[off + i];
      printf("%c", c >= 32 && c < 255 && (c < 127 || c >= 160) ? c : '.');
    }
    printf("\n");
  }

  if (res == -1) {
#ifndef FREERTOS
    woempa(9,"got error while sending from SocketImpl %p --> error = %i\n", ThisImpl, w_errno(sock));
#endif
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: id = %d write failed: %s\n", sock, strerror(errno));
    }
    throwException(thread, clazzIOException, "send errno %d '%s'", errno, strerror(errno));
  }

}

// Note: this doesn't actually send the data OOB (so pretty useless)
void PlainSocketImpl_sendUrgentData(w_thread thread , w_instance thisImpl, w_int udata) {
  w_sock sock = (w_sock)getWotsitField(thisImpl, F_PlainSocketImpl_wotsit);
  w_byte bdata = (w_byte)udata;
  w_int res;

  res = w_send(sock, &bdata, 1, MSG_NOSIGNAL | MSG_OOB);

  if (isSet(verbose_flags, VERBOSE_FLAG_TRAFFIC)) {
    printf("Traffic: write OOB on id %d: %d\n", sock, bdata);
  }

  if (res == -1) {
#ifndef FREERTOS
    woempa(9,"got error while sending OOB from SocketImpl %p --> error = %i\n", thisImpl, w_errno(sock));
#endif
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: id = %d OOB write failed: %s\n", sock, strerror(errno));
    }
    throwException(thread, clazzIOException, "send OOB errno %d '%s'", errno, strerror(errno));
  }
}

// TODO rewrite this using getaddrinfo(), see example at
// https://beej.us/guide/bgnet/output/html/multipage/bindman.html

// TODO stop this ifdef madness!!!
#ifdef FREERTOS
void PlainSocketImpl_bind(w_thread thread , w_instance ThisImpl) {

  w_instance address = getReferenceField(ThisImpl, F_PlainSocketImpl_localAddress); 
  w_int port = getIntegerField(ThisImpl, F_SocketImpl_localport);
  
  if (! address) {
    throwException(thread, clazzConnectException, "no IP address");

    return;
  }

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: tried to bind closed socket\n");
    }
    throwException(thread, clazzIOException, "socket closed or uninitialized");

    return;
  }  	  	  	

  struct freertos_sockaddr sa;
  w_int res;
  w_sock sock = (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);
  	
  memset(&sa, 0, sizeof(sa));
  sa.sin_family = PF_INET;
  sa.sin_addr = FreeRTOS_htonl(getIntegerField(address, F_InetAddress_address));
  sa.sin_port = FreeRTOS_htons(port);
  woempa(7, "INET: bind, port %d addr %x (%x)\n", sa.sin_port, sa.sin_addr, getIntegerField(address, F_InetAddress_address));
  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    unsigned char *a = (unsigned char *)&sa.sin_addr;
    printf("Socket: binding to %d.%d.%d.%d:%d\n", a[0], a[1], a[2], a[3], sa.sin_port);
  }

  res = w_bind (sock, &sa, sizeof(sa));
  woempa(6, "socketfd = %d, bindresult = %d\n", sock, res);

  if (res) {
    char *errmsg = (res == pdFREERTOS_ERRNO_EINVAL ? "could not bind to port" : res == pdFREERTOS_ERRNO_ECANCELED ? "cancelled" : "unknown");
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: id = %d bind failed: %s\n", sock, errmsg);
    }
    throwException(thread, clazzBindException, errmsg, "");

    return;
  }

  if (port == 0) {
    FreeRTOS_GetLocalAddress(sock, &sa);
    port = FreeRTOS_ntohs(sa.sin_port);
  } 

  setIntegerField(ThisImpl, F_SocketImpl_localport, port);
  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d bind succeeded, port = %d\n", sock, port);
  }
}
#else
void PlainSocketImpl_bind(w_thread thread , w_instance ThisImpl) {

  w_instance address = getReferenceField(ThisImpl, F_PlainSocketImpl_localAddress); 
  w_int port = getIntegerField(ThisImpl, F_SocketImpl_localport);
  
  if (! address) {
    throwException(thread, clazzConnectException, "no IP address");
  }
  else {
    struct sockaddr * sa = NULL;
    
    struct sockaddr_in  sa4;
#ifdef PF_INET6
    struct sockaddr_in6 sa6;
#endif

    w_int res;
    w_sock sock = (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);
  	
    if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
      woempa(9, "socket %i was closed\n",sock);
      throwException(thread, clazzIOException, "socket closed or uninitialized");
      return;
    }  	  	  	

#ifdef PF_INET6
    if (!(getBooleanField(ThisImpl, F_PlainSocketImpl_ipv6))) { 
      memset(&sa4, 0, sizeof(sa4));
      sa4.sin_family = AF_INET;
      sa4.sin_addr.s_addr = htonl(getIntegerField(address, F_InetAddress_address));
      sa4.sin_port = w_htons(port);
      woempa(1, "INET: bind, port %d addr %x (%x)\n", sa4.sin_port, sa4.sin_addr.s_addr, getIntegerField(address, F_InetAddress_address));
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        unsigned char *a = (unsigned char *)&sa4.sin_addr.s_addr;
        printf("Socket: binding to %d.%d.%d.%d:%d\n", a[0], a[1], a[2], a[3], sa4.sin_port);
      }

      sa = (struct sockaddr *)&sa4;
      sa_size = sizeof(struct sockaddr_in);
    }
    else {
      memset(&sa6, 0, sizeof(sa6));
      sa6.sin6_family = AF_INET6;
      sa6.sin6_port = w_htons(port);
      // TODO - test this!!!
      w_instance ipaddressInstance = getReferenceField(address, F_Inet6Address_ipaddress);
      w_byte *ipaddressBytes = instance2Array_byte(ipaddressInstance);
      memcpy(&sa6.sin6_addr, ipaddressBytes, 16);
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: binding (IPv6)\n");
      }

      sa = (struct sockaddr *)&sa6;
      sa_size = sizeof(struct sockaddr_in6);
    }
#else
    memset(&sa4, 0, sizeof(sa4));
    sa4.sin_family = AF_INET;
    sa4.sin_addr.s_addr = htonl(getIntegerField(address, F_InetAddress_address));
    sa4.sin_port = w_htons(port);
    //w_comparePorts(sa.sin_port,port);     
    woempa(1, "INET: bind, port %d addr %x (%x)\n", sa4.sin_port, sa4.sin_addr.s_addr, getIntegerField(address, F_InetAddress_address));
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      unsigned char *a = (unsigned char *)&sa4.sin_addr.s_addr;
      printf("Socket: binding to %d.%d.%d.%d:%d\n", a[0], a[1], a[2], a[3], sa4.sin_port);
    }

    sa = (struct sockaddr *)&sa4;
    sa_size = sizeof(struct sockaddr_in);
#endif

    res = w_bind (sock, sa, sa_size);
    woempa(6, "socketfd = %d, bindresult = %d\n", sock, res);

    if (res == -1) {
      woempa(9,"ERROR = %s\n", strerror(errno));
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d bind failed: %s\n", sock, strerror(errno));
      }
      throwException(thread, clazzBindException, "bind errno %d '%s'", errno, strerror(errno));
      return;
    }

#ifdef PF_INET6
    // TODO is this port == 0 check really necessary?
    if (port == 0) {
      if (!(getBooleanField(ThisImpl, F_PlainSocketImpl_ipv6))) { 
        socklen_t namelen = sizeof(sa4);
        res = w_getsockname(sock , (struct sockaddr*)&sa4 , &namelen);
        if (res == -1) {
          //woempa(9,"ERROR in bind (getsockname) = %s\n", w_strerror((int)w_errno(sock)));
          throwException(thread, clazzBindException, "getsockname errno %d '%s'", errno, strerror(errno));
        }
        else {
          port = w_ntohs(sa4.sin_port);
        }
      }
      else {
        socklen_t namelen = sizeof(sa6);
        res = w_getsockname(sock , (struct sockaddr*)&sa6 , &namelen);
        if (res == -1) {
          throwException(thread, clazzBindException, "getsockname errno %d '%s'", errno, strerror(errno));
        }
        else {
          port = w_ntohs(sa6.sin6_port);
        }
      }
    }  	
#else
    if (port == 0) {
      socklen_t namelen = sizeof(sa4);
      res = w_getsockname(sock , (struct sockaddr*)&sa4 , &namelen);
      if (res == -1) {
        //woempa(9,"ERROR in bind (getsockname) = %s\n", w_strerror((int)w_errno(sock)));
        throwException(thread, clazzBindException, "getsockname errno %d '%s'", errno, strerror(errno));
      }
      else {
        port = w_ntohs(sa4.sin_port);
        //w_comparePorts(sa4.sin_port,port);        
      }
    }  	
#endif
    setIntegerField(ThisImpl, F_SocketImpl_localport, port);
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: id = %d bind succeeded, port = %d\n", sock, port);
    }
  }

}
#endif

void PlainSocketImpl_listen(w_thread thread , w_instance ThisImpl, w_int backlog) {

  w_sock sock = (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    woempa(9, "socket %i is already closed\n",sock);
    throwException(thread, clazzIOException, "socket closed or uninitialized");
  }
  else {
    w_int res = w_listen(sock,(int)backlog);
    woempa(6, "listen called on socket %p (desc %i)\n", ThisImpl, sock);
    if (res == -1) {
      woempa(9,"ERROR occured while listen was called --> %s\n", strerror(errno));
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d listen failed: %s\n", sock, strerror(errno));
      }
      throwException(thread, clazzIOException, "listen errno %d '%s'", errno, strerror(errno));
    }
    else {
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d listen succeeded\n", sock);
      }
    }
  }

}

w_int PlainSocketImpl_accept(w_thread thread , w_instance ThisImpl, w_instance newImpl) {

  
  //newImpl will not be null (this function is a private method)
  w_sock sock = (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);
  woempa(1, "sock = %d timeout = %d\n", sock, getIntegerField(ThisImpl, F_PlainSocketImpl_timeout));

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open) || !newImpl) {
    woempa(9, "socket %i is already closed\n",sock);
    throwException(thread, clazzIOException, "socket error: accept() on closed socket");

    return -1;

  }
  else { // accept is done in two steps step step 1. get a socket from the system
#ifdef FREERTOS
    struct freertos_sockaddr sa;
    socklen_t bytelen = sizeof (struct freertos_sockaddr);
#else
    struct sockaddr_in sa;
    socklen_t bytelen = sizeof (struct sockaddr_in);
    struct sigaction action, savedaction;
  		
    action.sa_handler = sigusr_handler;
    sigemptyset (&action.sa_mask);
    action.sa_flags = 0;
    sigaction(SIGUSR1, &action, &savedaction);
#endif
    memset(&sa, 0, sizeof(sa)); //lets play safe
    w_sock newsocket = w_accept(sock, (struct sockaddr *) &sa , &bytelen, getIntegerField(ThisImpl, F_PlainSocketImpl_timeout));
#ifndef FREERTOS
    sigaction(SIGUSR1, &savedaction, NULL);
#endif
    woempa(1, "newsocket = %d\n", newsocket);
  	
    //set the wotsit value to -1 or the valid socket descriptor
    setWotsitField(newImpl, F_PlainSocketImpl_wotsit, newsocket);
  	
    if (newsocket == -1) {
      if (bytelen == 0) {
        woempa(9, "ERROR accept timed out\n");
        if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
          printf("Socket: id = %d accept failed (timeout)\n", sock);
        }
        throwException(thread, clazzInterruptedIOException, "socket timed out");
      }
      else {
        woempa(9,"ERROR occured while accept was called --> %s\n", strerror(errno));
        if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
          printf("Socket: id = %d accept failed: %s\n", sock, strerror(errno));
        }
        throwException(thread, clazzIOException, "error in accept, errno %d '%s', originating socket %d", errno, strerror(errno), sock);
      }

      return -1;

    }

    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: id = %d accept succeeded, port = %d id = %d\n", sock, w_ntohs(sa.sin_port), newsocket);
    }
    // step 2. set the fields of newImpl.
    setBooleanField(newImpl, F_PlainSocketImpl_open, WONKA_TRUE);
    setIntegerField(newImpl, F_SocketImpl_port, w_ntohs(sa.sin_port));
    bytelen = sizeof(sa);
#ifdef FREERTOS
    FreeRTOS_GetLocalAddress(sock, &sa);
    setIntegerField(newImpl, F_SocketImpl_localport, w_ntohs(sa.sin_port));
    return w_ntohl(sa.sin_addr);  	
#else
    newsocket = w_getsockname(newsocket , (struct sockaddr *) &sa , &bytelen);
    if (newsocket == -1) {
      woempa(9,"ERROR in accept (getsockname) = %s\n", strerror(errno));      
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d accept failed in getsockname(): %s\n", sock, strerror(errno));
      }
      throwException(thread, clazzIOException, "error in getsockname, errno %d '%s', originating socket %d", errno, strerror(errno), sock);
    }
    else {
      setIntegerField(newImpl, F_SocketImpl_localport, w_ntohs(sa.sin_port));
    }

    // TODO This only works for IPv4 !!!
    return w_ntohl(sa.sin_addr.s_addr);  	
#endif
  }
}

w_int PlainSocketImpl_available(w_thread thread , w_instance ThisImpl) {

  w_sock sock = (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    woempa(9, "socket %i is already closed\n", sock);
    throwException(thread, clazzIOException, "socket closed or uninitialized");
  }
  else { 	
    w_int arg = 0;
#ifdef FREERTOS
    w_int res = FreeRTOS_rx_size(sock);
#else
    w_int res = ioctl(sock, FIONREAD, &arg);
#endif

    if (res == -1) {
      //woempa(9, "Error in Available 'ioctl' failed: %s\n", w_strerror((int)w_errno(sock)));
      throwException(thread, clazzIOException, "ioctl errno %d '%s'", errno, strerror(errno));
    }

    woempa(6, "Available bytes %x, %x\n", res, arg);
    return arg;  		
  }

  return sock;

}

void PlainSocketImpl_shutdown(w_thread thread , w_instance ThisImpl, w_boolean in) {
  w_sock sock = (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    woempa(9, "socket %i is already closed\n", sock);
    throwException(thread, clazzIOException, "socket closed or uninitialized");
  }
  else { 	
    if(w_shutdown(sock, (in == WONKA_TRUE ? 0 : 1)) == -1){
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d shutdown(%d) failed: %s\n", sock, in, strerror(errno));
      }
      throwException(thread, clazzIOException, "shutdown failed: errno %d '%s'", errno, strerror(errno));
    }
    else {
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d shutdown succeeded\n", sock);
      }
    }

  }
}

static w_int getOption(w_thread thread, w_instance this, int level, int option) {

  w_sock sock = (w_sock)getWotsitField(this, F_PlainSocketImpl_wotsit);
  socklen_t size = sizeof(int);
  w_int value = 0;

  if (!getBooleanField(this, F_PlainSocketImpl_open)) {
    throwException(thread, clazzSocketException, "socket closed or uninitialized");
  } else
// FreeRTOS has no getsockopt
#ifndef FREERTOS
  if(w_getsockopt(sock, level, option, &value, &size) == -1){
    throwException(thread, clazzSocketException, "getting option %d failed: errno %d '%s'", option, errno, strerror(errno));
  } else
#endif
  return value;

}

static void setOption(w_thread thread, w_instance this, w_int level, w_int option, void * valptr, w_int valsize) {
  w_sock sock = (w_sock)getWotsitField(this, F_PlainSocketImpl_wotsit);

  if (!getBooleanField(this, F_PlainSocketImpl_open)) {
    throwException(thread, clazzSocketException, "socket closed or uninitialized");
    return;
  }

  if (w_setsockopt(sock, level, option, valptr, valsize) == -1) {
    throwException(thread, clazzSocketException, "setsockopt failed: %s", strerror(errno));
  }
}

w_int PlainSocketImpl_getRcvBuf(w_thread thread , w_instance thisImpl) {
  return getOption(thread, thisImpl, SOL_SOCKET, SO_RCVBUF);
}

void PlainSocketImpl_setRcvBuf(w_thread thread , w_instance thisImpl, w_int size) {

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting rcvbuf size to %d\n", (w_sock)getWotsitField(thisImpl, F_PlainSocketImpl_wotsit), size);
  }

  setOption(thread, thisImpl, SOL_SOCKET, SO_RCVBUF, &size, sizeof(int));
}

w_int PlainSocketImpl_getSndBuf(w_thread thread , w_instance thisImpl) {
  return getOption(thread, thisImpl, SOL_SOCKET, SO_SNDBUF);
}

void PlainSocketImpl_setSndBuf(w_thread thread , w_instance thisImpl, w_int size) {

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting sndbuf size to %d\n", (w_sock)getWotsitField(thisImpl, F_PlainSocketImpl_wotsit), size);
  }

  setOption(thread, thisImpl, SOL_SOCKET, SO_SNDBUF, &size, sizeof(int));
}

w_int PlainSocketImpl_getIpTos(w_thread thread , w_instance thisImpl) {
#ifdef FREERTOS
  woempa(7, "FreeRTOS+TCP doesn't give a TOS\n");
  return 0;
#else
  return getOption(thread, thisImpl, IPPROTO_IP, IP_TOS);
#endif
}

void PlainSocketImpl_setIpTos(w_thread thread , w_instance thisImpl, w_int tos) {

#ifdef FREERTOS
  woempa(7, "FreeRTOS+TCP doesn't give a TOS\n");
#else
  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting IP TOS to %d\n", (w_sock)getWotsitField(thisImpl, F_PlainSocketImpl_wotsit), tos);
  }

  setOption(thread, thisImpl, IPPROTO_IP, IP_TOS, &tos, sizeof(int));
#endif
}

void PlainSocketImpl_setLinger(w_thread thread , w_instance thisImpl, w_int ling) {
/* TODO : re-write me
  struct linger longer;

  if (ling < 0) {
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: id = %d disabling linger\n", (w_sock)getWotsitField(thisImpl, F_PlainSocketImpl_wotsit));
    }

    longer.l_onoff = 0;
    longer.l_linger = 0;
    setOption(thread, thisImpl, SOL_SOCKET, SO_LINGER, &longer, sizeof(longer));
  }
  else {
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: id = %d setting linger to %d seconds\n", (w_sock)getWotsitField(thisImpl, F_PlainSocketImpl_wotsit), ling);
    }

    longer.l_onoff = 1;
    longer.l_linger = ling;
    setOption(thread, thisImpl, SOL_SOCKET, SO_LINGER, &longer, sizeof(struct linger));
  }
*/
}

void PlainSocketImpl_setKeepAlive(w_thread thread , w_instance ThisImpl, w_boolean on) {
/* TODO : re-write me

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting keepalive to %d\n", (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit), on);
  }

  setOption(thread, ThisImpl, SOL_SOCKET, SO_KEEPALIVE, &on, sizeof(int));
*/
}

void PlainSocketImpl_setNoDelay(w_thread thread , w_instance ThisImpl, w_boolean on) {
/* TODO : re-write me

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting nodelay to %d\n", (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit), on);
  }

  setOption(thread, ThisImpl, IPPROTO_TCP, TCP_NODELAY, &on, sizeof(int));
*/
}

void PlainSocketImpl_setOOBInline(w_thread thread , w_instance ThisImpl, w_boolean on) {
/* TODO : re-write me

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting OOBInline to %d\n", (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit), on);
  }

  setOption(thread, ThisImpl, SOL_SOCKET, SO_OOBINLINE, &on, sizeof(int));
*/
}

void PlainSocketImpl_setSoTimeout(w_thread thread , w_instance ThisImpl, w_int millis) {
/* TODO : re-write me
  struct timeval tv;

  tv.tv_sec = millis / 1000;
  tv.tv_usec = (millis % 1000) * 1000;
  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting timeout to %d secs %d usecs\n", (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit), (int)tv.tv_sec, (int)tv.tv_usec);
  }

  setOption(thread, ThisImpl, SOL_SOCKET, SO_RCVTIMEO, &tv, sizeof(struct timeval));
*/
}

void PlainSocketImpl_setReuseAddr(w_thread thread , w_instance ThisImpl, w_boolean on) {
/* TODO : re-write me

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting ReuseAddr to %d\n", (w_sock)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit), on);
  }

  setOption(thread, ThisImpl, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(int));
*/
}

void PlainSocketImpl_signal(w_thread thread, w_instance thisPlainSocketImpl, w_instance aThread) {
/* TODO : re-write me
  w_thread wt = w_threadFromThreadInstance(aThread);
  x_thread xt = wt->kthread;
  if (xt) {
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      w_printf("Socket: signalling %t\n", wt);
    }
    x_status status = x_thread_signal(xt, x_signal_1);
    if (status != xs_success) {
      w_printf("WARNING: x_thread_signal returned %d\n", status);
    }
  }
*/
}

w_int PlainSocketImpl_getLocal4Address(w_thread thread, w_instance thisPlainSocketImpl) {
/* TODO : re-write me
    w_sock sock = (w_sock)getWotsitField(thisPlainSocketImpl, F_PlainSocketImpl_wotsit);
    struct sockaddr_in sa4;
    socklen_t addressLength = sizeof(sa4);
    getsockname(sock, (struct sockaddr*)&sa4, &addressLength);
    return ntohl(sa4.sin_addr.s_addr);
*/
  return 0;
}

void PlainSocketImpl_getLocal6Address(w_thread thread, w_instance thisPlainSocketImpl, w_instance outputByteArray) {
/* TODO : re-write me
    w_sock sock = (w_sock)getWotsitField(thisPlainSocketImpl, F_PlainSocketImpl_wotsit);
    struct sockaddr_in6 sa6;
    socklen_t addressLength = sizeof(sa6);
    getsockname(sock, (struct sockaddr*)&sa6, &addressLength);
    memcpy(instance2Array_byte(outputByteArray), &sa6.sin6_addr, 16);
*/
}

