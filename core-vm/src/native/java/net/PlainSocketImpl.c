/**************************************************************************
* Copyright (c) 2004, 2007, 2008, 2009, 2015 by Chris Gray, KIFFER Ltd.   *
* All rights reserved.                                                    *
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
  w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);
  w_int res;	

  if (getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    w_int minus1 = -1;
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: closing %d\n", (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit));
    }
    res = w_socketclose(sock);

    setBooleanField(ThisImpl, F_PlainSocketImpl_open, WONKA_FALSE);
    setWotsitField(ThisImpl, F_PlainSocketImpl_wotsit, minus1);
  }
  else {
    woempa(1, "socket %p was already closed !\n", ThisImpl);
    res = 0;
  }

  return res;

}

void PlainSocketImpl_close(JNIEnv* env , w_instance ThisImpl) {
  if (PlainSocketImpl_clear(ThisImpl) == -1) {
    throwException(JNIEnv2w_thread(env), clazzIOException, "closing socket failed with %d: '%s'", errno, strerror(errno));
  }
}

void PlainSocketImpl_finalize(JNIEnv* env , w_instance ThisImpl) {
  PlainSocketImpl_clear(ThisImpl);
}

w_int PlainSocketImpl_getSocket(JNIEnv* env , w_instance ThisImpl){

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    throwException(JNIEnv2w_thread(env), clazzSocketException, "socket is not open or uninitialized");
  }

  return (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);

}

void PlainSocketImpl_nativeCreate(JNIEnv* env , w_instance ThisImpl) {
  w_int sock;
#ifdef PF_INET6
  w_int pf = (getBooleanField(ThisImpl, F_PlainSocketImpl_ipv6) ? PF_INET6 : PF_INET);
#else
  w_int pf = PF_INET;
#endif

  if(getBooleanField(ThisImpl, F_PlainSocketImpl_open)){
    throwException(JNIEnv2w_thread(env), clazzIOException, "socket is already open");
  }
  else {
    sock = w_socket (pf, SOCK_STREAM, 0);
    setWotsitField(ThisImpl, F_PlainSocketImpl_wotsit, sock);	
    woempa(1, "%j : socket is %d\n", ThisImpl, sock);

    if (sock == -1) {
      throwException(JNIEnv2w_thread(env), clazzIOException, "socket errno %d '%s'", errno, strerror(errno));
      woempa(9, "Not able to create a socket for %p\n", ThisImpl);
    }
    else {
      setBooleanField(ThisImpl, F_PlainSocketImpl_open, WONKA_TRUE);
    }
  }
}

void PlainSocketImpl_connect(JNIEnv* env , w_instance ThisImpl, w_int timeout) {

  w_instance address = getReferenceField(ThisImpl, F_SocketImpl_address); 
  w_int port = getIntegerField(ThisImpl, F_SocketImpl_port);

  if (!address) {
    throwException(JNIEnv2w_thread(env), clazzConnectException, "no IP address");
  }
  else {
    struct sockaddr * sa = NULL;
    int sa_size = 0;
    
    struct sockaddr_in  sa4;
#ifdef PF_INET6
    struct sockaddr_in6 sa6;
#endif

    w_int res;
    w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);

    if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
      woempa(9, "socket %i was closed\n", sock);
      throwException(JNIEnv2w_thread(env), clazzIOException, "socket was closed or uninitialized");
      return;
    }  	  	

#ifdef PF_INET6
    if (!(getBooleanField(ThisImpl, F_PlainSocketImpl_ipv6))) { 
      memset(&sa4, 0, sizeof(sa4));
      sa4.sin_addr.s_addr = htonl(getIntegerField(address, F_InetAddress_address));
      sa4.sin_family = AF_INET;
      //sa4.sin_port = w_switchPortBytes(port);
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
      sa6.sin6_port = w_switchPortBytes(port);

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
    sa4.sin_port = w_switchPortBytes(port);

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
        throwException(JNIEnv2w_thread(env), clazzSocketTimeoutException, NULL);
      }
      else {
        throwException(JNIEnv2w_thread(env), clazzConnectException, "socket connect errno %d '%s'", errno, strerror(errno));
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
        throwException(JNIEnv2w_thread(env), clazzConnectException, "getsockname errno %d '%s'", errno, strerror(errno));
      }
      else {
        setIntegerField(ThisImpl, F_SocketImpl_localport, w_switchPortBytes(sa4.sin_port));
      }
    }
    else {
      socklen_t namelen = sizeof(sa6);
      res = w_getsockname(sock , (struct sockaddr *)&sa6 , &namelen);

      if (res == -1) {
        woempa(9,"ERROR in connect/getsockname = %s\n", strerror(errno)); 	
        throwException(JNIEnv2w_thread(env), clazzConnectException, "getsockname errno %d '%s'", errno, strerror(errno));
      }
      else {	
        setIntegerField(ThisImpl, F_SocketImpl_localport, w_switchPortBytes(sa6.sin6_port));
      }
    }
#else
    socklen_t namelen = sizeof(sa4);
    res = w_getsockname(sock , (struct sockaddr *)&sa4 , &namelen);
    if (res == -1) {
      //woempa(9,"ERROR in connect = %s\n", w_strerror((int)w_errno(sock)));
      throwException(JNIEnv2w_thread(env), clazzConnectException, "getsockname errno %d '%s'", errno, strerror(errno));
    }
    else {  
      setIntegerField(ThisImpl, F_SocketImpl_localport, w_switchPortBytes(sa4.sin_port));
    }
#endif
  }
  
}

// Our signal handler doesn't actually do anything, it's just there so that
// SIGUSR1 has the desired effect of interrupting accept() or read().
static sigusr_handler(int sig) {
  woempa(1, "Received signal %d\n", sig);
}

w_int PlainSocketImpl_read(JNIEnv* env , w_instance ThisImpl, w_instance byteArray, w_int off, w_int length) {

  w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);
  w_int res;	
  w_int arrayLength;
  w_int timeout;
  w_int i;
  unsigned char c;

  struct sigaction action, savedaction;
  
  woempa(1, "reading %i bytes from SocketImpl %p (desp %i)\n", length, ThisImpl, sock);

  if (! byteArray) {
    throwException(JNIEnv2w_thread(env), clazzConnectException, "no IP address");
    return -1;
  }
  else {
    arrayLength = instance2Array_length(byteArray);
    if ((off < 0) || (length < 0) || (off > arrayLength - length)) {
      throwException(JNIEnv2w_thread(env), clazzArrayIndexOutOfBoundsException, "out of bounds; size = %d, offset = %d, length = %d", arrayLength, off, length);
      return -1;
    }
  }

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    throwException(JNIEnv2w_thread(env), clazzIOException, "socket closed or uninitialized");
    return -1;  	
  }

  if (length == 0) {
    return 0;
  }

  /*
  ** All seems OK, let's read...
  */
  action.sa_handler = sigusr_handler;
  sigemptyset (&action.sa_mask);
  action.sa_flags = 0;
  sigaction(SIGUSR1, &action, &savedaction);
  timeout = getIntegerField(ThisImpl, F_PlainSocketImpl_timeout);
  woempa(2, "Calling w_recv(%d,%j[%d],%d,0,%d)\n", sock, byteArray, off, length, 0, timeout);
#ifdef UCLINUX
  res = w_recv(ThisImpl, sock, instance2Array_byte(byteArray) + off, (w_word)length, 0, &timeout);
#else
  res = w_recv(sock, instance2Array_byte(byteArray) + off, (w_word)length, 0, &timeout);
#endif
  sigaction(SIGUSR1, &savedaction, NULL);
  if (res == -1) {
    if( timeout == -1) {
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d read timed out\n", sock);
      }
      throwException(JNIEnv2w_thread(env), clazzSocketTimeoutException, "");
    }
    else {
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d read failed: %s\n", sock, strerror(errno));
      }
      throwException(JNIEnv2w_thread(env), clazzIOException, "recv errno %d '%s'", errno, strerror(errno));
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

  woempa(1, "reading %i bytes from SocketImpl %p --> got %i\n", length, ThisImpl, res);
  if (res == 0) { 
    res--; 
  }

  return res;

}

void PlainSocketImpl_write(JNIEnv * env, w_instance ThisImpl, w_instance byteArray, w_int off, w_int length) {

  w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);
  w_int res;	
  w_int arrayLength;
  w_int i;
  unsigned char c;

  woempa(1, "writing %i bytes to SocketImpl %p\n", length, ThisImpl);

  if (! byteArray) {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, "buffer is NULL");
    return;
  }
  else {
    arrayLength = instance2Array_length(byteArray);
    if ((off < 0) || (length < 0) || (off > arrayLength - length)) {
      throwException(JNIEnv2w_thread(env), clazzArrayIndexOutOfBoundsException, "out of bounds; size = %d, offset = %d, length = %d", arrayLength, off, length);
      return;
    }
  }

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    throwException(JNIEnv2w_thread(env), clazzIOException, "socket closed or uninitialized");
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
    woempa(9,"got error while sending from SocketImpl %p --> error = %i\n", ThisImpl, w_errno(sock));
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: id = %d write failed: %s\n", sock, strerror(errno));
    }
    throwException(JNIEnv2w_thread(env), clazzIOException, "send errno %d '%s'", errno, strerror(errno));
  }

}

// TODO: test if this actually works ...
void PlainSocketImpl_sendUrgentData(JNIEnv* env , w_instance thisImpl, w_int udata) {
  w_int sock = (w_int)getWotsitField(thisImpl, F_PlainSocketImpl_wotsit);
  w_byte bdata = (w_byte)udata;
  w_int res;

  res = w_send(sock, &bdata, 1, MSG_NOSIGNAL | MSG_OOB);

  if (isSet(verbose_flags, VERBOSE_FLAG_TRAFFIC)) {
    printf("Traffic: write OOB on id %d: %d\n", sock, bdata);
  }

  if (res == -1) {
    woempa(9,"got error while sending OOB from SocketImpl %p --> error = %i\n", thisImpl, w_errno(sock));
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: id = %d OOB write failed: %s\n", sock, strerror(errno));
    }
    throwException(JNIEnv2w_thread(env), clazzIOException, "send OOB errno %d '%s'", errno, strerror(errno));
  }
}

// TODO rewrite this using getaddrinfo(), see example at
// https://beej.us/guide/bgnet/output/html/multipage/bindman.html

void PlainSocketImpl_bind(JNIEnv* env , w_instance ThisImpl) {

  w_instance address = getReferenceField(ThisImpl, F_PlainSocketImpl_localAddress); 
  w_int port = getIntegerField(ThisImpl, F_SocketImpl_localport);
  w_thread thread = JNIEnv2w_thread(env);
  
  if (! address) {
    throwException(JNIEnv2w_thread(env), clazzConnectException, "no IP address");
  }
  else {
    struct sockaddr * sa = NULL;
    int sa_size = 0;
    
    struct sockaddr_in  sa4;
#ifdef PF_INET6
    struct sockaddr_in6 sa6;
#endif

    w_int res;
    w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);
  	
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
      sa4.sin_port = w_switchPortBytes(port);
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
      sa6.sin6_port = w_switchPortBytes(port);
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
    sa4.sin_port = w_switchPortBytes(port);
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
          port = w_switchPortBytes(sa4.sin_port);
        }
      }
      else {
        socklen_t namelen = sizeof(sa6);
        res = w_getsockname(sock , (struct sockaddr*)&sa6 , &namelen);
        if (res == -1) {
          throwException(thread, clazzBindException, "getsockname errno %d '%s'", errno, strerror(errno));
        }
        else {
          port = w_switchPortBytes(sa6.sin6_port);
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
        port = w_switchPortBytes(sa4.sin_port);
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

void PlainSocketImpl_listen(JNIEnv* env , w_instance ThisImpl, w_int backlog) {

  w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    woempa(9, "socket %i is already closed\n",sock);
    throwException(JNIEnv2w_thread(env), clazzIOException, "socket closed or uninitialized");
  }
  else {
    w_int res = w_listen(sock,(int)backlog);
    woempa(6, "listen called on socket %p (desc %i)\n", ThisImpl, sock);
    if (res == -1) {
      woempa(9,"ERROR occured while listen was called --> %s\n", strerror(errno));
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d listen failed: %s\n", sock, strerror(errno));
      }
      throwException(JNIEnv2w_thread(env), clazzIOException, "listen errno %d '%s'", errno, strerror(errno));
    }
    else {
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d listen succeeded\n", sock);
      }
    }
  }

}

int PlainSocketImpl_accept(JNIEnv* env , w_instance ThisImpl, w_instance newImpl) {

  w_thread thread = JNIEnv2w_thread(env);
  
  //newImpl will not be null (this function is a private method)
  w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);
  woempa(1, "sock = %d timeout = %d\n", sock, getIntegerField(ThisImpl, F_PlainSocketImpl_timeout));

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open) || !newImpl) {
    woempa(9, "socket %i is already closed\n",sock);
    throwException(thread, clazzIOException, "socket error: accept() on closed socket");

    return -1;

  }
  else { // accept is done in two steps step step 1. get a socket from the system
    struct sockaddr_in sa;
    w_int newsocket;
    socklen_t bytelen = sizeof (struct sockaddr_in);
    struct sigaction action, savedaction;
  		
    action.sa_handler = sigusr_handler;
    sigemptyset (&action.sa_mask);
    action.sa_flags = 0;
    sigaction(SIGUSR1, &action, &savedaction);
    memset(&sa, 0, sizeof(sa)); //lets play safe
#ifdef UCLINUX
    newsocket = w_accept(ThisImpl,sock, (struct sockaddr *) &sa , &bytelen, getIntegerField(ThisImpl, F_PlainSocketImpl_timeout));
#else
    newsocket = w_accept(sock, (struct sockaddr *) &sa , &bytelen, getIntegerField(ThisImpl, F_PlainSocketImpl_timeout));
#endif
    sigaction(SIGUSR1, &savedaction, NULL);
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
      printf("Socket: id = %d accept succeeded, port = %d id = %d\n", sock, w_switchPortBytes(sa.sin_port), newsocket);
    }
    // step 2. set the fields of newImpl.
    setBooleanField(newImpl, F_PlainSocketImpl_open, WONKA_TRUE);
    setIntegerField(newImpl, F_SocketImpl_port, w_switchPortBytes(sa.sin_port));
    bytelen = sizeof(sa);
    newsocket = w_getsockname(newsocket , (struct sockaddr *) &sa , &bytelen);
    if (newsocket == -1) {
      woempa(9,"ERROR in accept (getsockname) = %s\n", strerror(errno));      
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d accept failed in getsockname(): %s\n", sock, strerror(errno));
      }
      throwException(thread, clazzIOException, "error in getsockname, errno %d '%s', originating socket %d", errno, strerror(errno), sock);
    }
    else {
      setIntegerField(newImpl, F_SocketImpl_localport, w_switchPortBytes(sa.sin_port));
    }

    // TODO This only works for IPv4 !!!
    return ntohl(sa.sin_addr.s_addr);  	
  }
}

w_int PlainSocketImpl_available(JNIEnv* env , w_instance ThisImpl) {

  w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    woempa(9, "socket %i is already closed\n", sock);
    throwException(JNIEnv2w_thread(env), clazzIOException, "socket closed or uninitialized");
  }
  else { 	
    w_int arg = 0;
    w_int res = ioctl(sock, FIONREAD, &arg);

    if (res == -1) {
      //woempa(9, "Error in Available 'ioctl' failed: %s\n", w_strerror((int)w_errno(sock)));
      throwException(JNIEnv2w_thread(env), clazzIOException, "ioctl errno %d '%s'", errno, strerror(errno));
    }

    woempa(6, "Available bytes %x, %x\n", res, arg);
    return arg;  		
  }

  return sock;

}

void PlainSocketImpl_shutdown(JNIEnv* env , w_instance ThisImpl, w_boolean in) {
  w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit);

  if (!getBooleanField(ThisImpl, F_PlainSocketImpl_open)) {
    woempa(9, "socket %i is already closed\n", sock);
    throwException(JNIEnv2w_thread(env), clazzIOException, "socket closed or uninitialized");
  }
  else { 	
    if(shutdown(sock, (in == WONKA_TRUE ? 0 : 1)) == -1){
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d shutdown(%d) failed: %s\n", sock, in, strerror(errno));
      }
      throwException(JNIEnv2w_thread(env), clazzIOException, "shutdown failed: errno %d '%s'", errno, strerror(errno));
    }
    else {
      if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
        printf("Socket: id = %d shutdown succeeded\n", sock);
      }
    }

  }
}

static w_int getOption(w_thread thread, w_instance this, int level, int option) {
  w_int sock = (w_int)getWotsitField(this, F_PlainSocketImpl_wotsit);
  socklen_t size = sizeof(int);
  int value;

  if (!getBooleanField(this, F_PlainSocketImpl_open)) {
    throwException(thread, clazzSocketException, "socket closed or uninitialized");

    return 0;

  }

  if(w_getsockopt(sock, level, option, &value, &size) == -1){
    throwException(thread, clazzSocketException, "getting SO_KEEPALIVE failed: errno %d '%s'", errno, strerror(errno));

    return 0;

  }

  return value;

}

static void setOption(w_thread thread, w_instance this, int level, int option, void * valptr, int valsize) {
  w_int sock = (w_int)getWotsitField(this, F_PlainSocketImpl_wotsit);

  if (!getBooleanField(this, F_PlainSocketImpl_open)) {
    throwException(thread, clazzSocketException, "socket closed or uninitialized");
    return;
  }

  if (w_setsockopt(sock, level, option, valptr, valsize) == -1) {
    throwException(thread, clazzSocketException, "setsockopt failed: %s", strerror(errno));
  }
}

w_int PlainSocketImpl_getRcvBuf(JNIEnv* env , w_instance thisImpl) {
  return getOption(JNIEnv2w_thread(env), thisImpl, SOL_SOCKET, SO_RCVBUF);
}

void PlainSocketImpl_setRcvBuf(JNIEnv* env , w_instance thisImpl, w_int size) {
  w_thread thread = JNIEnv2w_thread(env);

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting rcvbuf size to %d\n", (w_int)getWotsitField(thisImpl, F_PlainSocketImpl_wotsit), size);
  }

  setOption(thread, thisImpl, SOL_SOCKET, SO_RCVBUF, &size, sizeof(int));
}

w_int PlainSocketImpl_getSndBuf(JNIEnv* env , w_instance thisImpl) {
  return getOption(JNIEnv2w_thread(env), thisImpl, SOL_SOCKET, SO_SNDBUF);
}

void PlainSocketImpl_setSndBuf(JNIEnv* env , w_instance thisImpl, w_int size) {
  w_thread thread = JNIEnv2w_thread(env);

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting sndbuf size to %d\n", (w_int)getWotsitField(thisImpl, F_PlainSocketImpl_wotsit), size);
  }

  setOption(thread, thisImpl, SOL_SOCKET, SO_SNDBUF, &size, sizeof(int));
}

w_int PlainSocketImpl_getIpTos(JNIEnv* env , w_instance thisImpl) {
  return getOption(JNIEnv2w_thread(env), thisImpl, IPPROTO_IP, IP_TOS);
}

void PlainSocketImpl_setIpTos(JNIEnv* env , w_instance thisImpl, w_int tos) {
  w_thread thread = JNIEnv2w_thread(env);

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting IP TOS to %d\n", (w_int)getWotsitField(thisImpl, F_PlainSocketImpl_wotsit), tos);
  }

  setOption(thread, thisImpl, IPPROTO_IP, IP_TOS, &tos, sizeof(int));
}

void PlainSocketImpl_setLinger(JNIEnv* env , w_instance thisImpl, w_int ling) {
  w_thread thread = JNIEnv2w_thread(env);
  struct linger longer;

  if (ling < 0) {
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: id = %d disabling linger\n", (w_int)getWotsitField(thisImpl, F_PlainSocketImpl_wotsit));
    }

    longer.l_onoff = 0;
    longer.l_linger = 0;
    setOption(thread, thisImpl, SOL_SOCKET, SO_LINGER, &longer, sizeof(longer));
  }
  else {
    if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
      printf("Socket: id = %d setting linger to %d seconds\n", (w_int)getWotsitField(thisImpl, F_PlainSocketImpl_wotsit), ling);
    }

    longer.l_onoff = 1;
    longer.l_linger = ling;
    setOption(thread, thisImpl, SOL_SOCKET, SO_LINGER, &longer, sizeof(struct linger));
  }
}

void PlainSocketImpl_setKeepAlive(JNIEnv* env , w_instance ThisImpl, w_boolean on) {
  w_thread thread = JNIEnv2w_thread(env);

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting keepalive to %d\n", (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit), on);
  }

  setOption(thread, ThisImpl, SOL_SOCKET, SO_KEEPALIVE, &on, sizeof(int));
}

void PlainSocketImpl_setNoDelay(JNIEnv* env , w_instance ThisImpl, w_boolean on) {
  w_thread thread = JNIEnv2w_thread(env);

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting nodelay to %d\n", (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit), on);
  }

  setOption(thread, ThisImpl, IPPROTO_TCP, TCP_NODELAY, &on, sizeof(int));
}

void PlainSocketImpl_setOOBInline(JNIEnv* env , w_instance ThisImpl, w_boolean on) {
  w_thread thread = JNIEnv2w_thread(env);

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting OOBInline to %d\n", (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit), on);
  }

  setOption(thread, ThisImpl, SOL_SOCKET, SO_OOBINLINE, &on, sizeof(int));
}

void PlainSocketImpl_setSoTimeout(JNIEnv* env , w_instance ThisImpl, w_int millis) {
  w_thread thread = JNIEnv2w_thread(env);
  struct timeval tv;

  tv.tv_sec = millis / 1000;
  tv.tv_usec = (millis % 1000) * 1000;
  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting timeout to %d secs %d usecs\n", (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit), (int)tv.tv_sec, (int)tv.tv_usec);
  }

  setOption(thread, ThisImpl, SOL_SOCKET, SO_RCVTIMEO, &tv, sizeof(struct timeval));
}

void PlainSocketImpl_setReuseAddr(JNIEnv* env , w_instance ThisImpl, w_boolean on) {
  w_thread thread = JNIEnv2w_thread(env);

  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting ReuseAddr to %d\n", (w_int)getWotsitField(ThisImpl, F_PlainSocketImpl_wotsit), on);
  }

  setOption(thread, ThisImpl, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(int));
}

void PlainSocketImpl_signal(JNIEnv *env, w_instance thisPlainSocketImpl, w_instance aThread) {
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
}

int PlainSocketImpl_getLocal4Address(JNIEnv *env, w_instance thisPlainSocketImpl) {
    w_int sock = (w_int)getWotsitField(thisPlainSocketImpl, F_PlainSocketImpl_wotsit);
    struct sockaddr_in sa4;
    socklen_t addressLength = sizeof(sa4);
    getsockname(sock, (struct sockaddr*)&sa4, &addressLength);
    return ntohl(sa4.sin_addr.s_addr);
}

void PlainSocketImpl_getLocal6Address(JNIEnv *env, w_instance thisPlainSocketImpl, w_instance outputByteArray) {
    w_int sock = (w_int)getWotsitField(thisPlainSocketImpl, F_PlainSocketImpl_wotsit);
    struct sockaddr_in6 sa6;
    socklen_t addressLength = sizeof(sa6);
    getsockname(sock, (struct sockaddr*)&sa6, &addressLength);
    memcpy(instance2Array_byte(outputByteArray), &sa6.sin6_addr, 16);
}

