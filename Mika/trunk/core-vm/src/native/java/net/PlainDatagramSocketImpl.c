/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

#include <string.h>
#include <errno.h>

#include "arrays.h"
#include "core-classes.h"
#include "fields.h"
#include "exception.h"
#include "network.h"

/**
** PlainDatagramSocketImpl use calls to functions defined by network.h. All these calls work with a descriptor
** the descriptor is stored in the wotsit of SocketImpl ! We don't use the FileDescritor Object,
** it will be left null ... . This means other implementions should use this wotsit value to make calls to
** the network.  Note: this is not the way to do it according to java API.  Although using a FileDescriptor
** Almost implies using native code. By leaving that descriptor null we prevent it from being misused ...
*/

w_int PlainDatagramSocketImpl_clear(w_instance ThisImpl) {

  w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainDatagramSocketImpl_wotsit);
  w_int res = 0;
  w_int minus1 = -1;

  if (getBooleanField(ThisImpl, F_PlainDatagramSocketImpl_open)) {
    res = w_socketclose(sock);
    if (res == -1) {
      //woempa(9, "ERROR while closing socket %p (desc = %i) %s\n", ThisImpl, sock, w_strerror((int)w_errno(sock)));
    }
    setWotsitField(ThisImpl, F_PlainDatagramSocketImpl_wotsit, minus1);
    setBooleanField(ThisImpl, F_PlainDatagramSocketImpl_open, WONKA_FALSE);
  }
  else {
    woempa(7, "socket %p was already closed !\n", ThisImpl);
  }

  return res;

}

w_int PlainDatagramSocketImpl_getSocket(JNIEnv* env , w_instance ThisImpl) {

  w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainDatagramSocketImpl_wotsit);

  if (!getBooleanField(ThisImpl, F_PlainDatagramSocketImpl_open)) {
    woempa(9, "ERROR socket %p is closed !\n", ThisImpl);
    throwException(JNIEnv2w_thread(env), clazzSocketException, "socket is already closed");
  }

  return sock;

}

void PlainDatagramSocketImpl_close(JNIEnv* env , w_instance ThisImpl) {

  if (PlainDatagramSocketImpl_clear(ThisImpl) == -1) {
    throwException(JNIEnv2w_thread(env), clazzIOException, "close failed");
  }

}

void PlainDatagramSocketImpl_finalize(JNIEnv* env , w_instance ThisImpl) {
  PlainDatagramSocketImpl_clear(ThisImpl);
}

void PlainDatagramSocketImpl_nativeCreate(JNIEnv* env , w_instance ThisImpl) {

  w_int sock;

  if(getBooleanField(ThisImpl, F_PlainDatagramSocketImpl_open)){
    throwException(JNIEnv2w_thread(env), clazzIOException, "socket is already created");
  }
  else {
    sock = w_socket (PF_INET, SOCK_DGRAM, 0);

    setWotsitField(ThisImpl, F_PlainDatagramSocketImpl_wotsit, sock);	
    if (sock == -1) {
      throwException(JNIEnv2w_thread(env), clazzIOException, "failed to create a socket: %s",strerror(errno));
      woempa(9, "Error Not able to create a socket for %p\n", ThisImpl);
    }
    else {
      setBooleanField(ThisImpl, F_PlainDatagramSocketImpl_open, WONKA_TRUE);
    }
  }
}

void PlainDatagramSocketImpl_bind(JNIEnv* env , w_instance ThisImpl, w_int port, w_instance address) {
  if (!address) {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, "bind failed: %s",strerror(errno));
  }
  else {
    struct sockaddr_in sa;
    w_int res = getIntegerField(address, F_InetAddress_family);
    w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainDatagramSocketImpl_wotsit);
  	
    if (!getBooleanField(ThisImpl, F_PlainDatagramSocketImpl_open)) {
      woempa(9, "Error socket %i is closed\n",sock);
      throwException(JNIEnv2w_thread(env), clazzIOException, "socket is already closed");
      return;
    }  	  	  	

    memset(&sa,0,sizeof(sa));
    sa.sin_addr.s_addr = htonl(getIntegerField(address, F_InetAddress_address)); 	
    sa.sin_family = (w_short)getIntegerField(address, F_InetAddress_family);
    sa.sin_port = w_switchPortBytes(port);
    res = w_bind (sock, (struct sockaddr*)&sa, sizeof (struct sockaddr_in));

    if (res == -1) {
      //woempa(9,"ERROR = %s\n", w_strerror((int)w_errno(sock)));
      throwException(JNIEnv2w_thread(env), clazzBindException, "bind failed: %s",strerror(errno));
      return;
    }

    if (port == 0){
      socklen_t namelen = sizeof(sa);
      res = w_getsockname(sock , (struct sockaddr*)&sa , &namelen);
      if (res == -1){
        //woempa(9,"ERROR in bind (getsockname) = %s\n", w_strerror((int)w_errno(sock))); 	
        throwException(JNIEnv2w_thread(env), clazzBindException, "bind failed: %s",strerror(errno));
      }
      else {
        port = w_switchPortBytes(sa.sin_port);    	
      }
    }

    setIntegerField(ThisImpl, F_DatagramSocketImpl_localPort, port);
  }

}

w_int PlainDatagramSocketImpl_peek(JNIEnv* env , w_instance ThisImpl, w_instance address) {

  w_int port = 0;

  if (!address) {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
  }
  else {
    struct sockaddr_in sa;
    w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainDatagramSocketImpl_wotsit);
    socklen_t len = sizeof (struct sockaddr_in);

    if (!getBooleanField(ThisImpl, F_PlainDatagramSocketImpl_open)) {
      woempa(9, "Error socket %i is closed\n",sock);
      throwException(JNIEnv2w_thread(env), clazzIOException, "socket is already closed");
      return port;
    }  	  	  	

    memset(&sa,0,sizeof(sa));
    port = w_recvfrom(sock, NULL, 0 , MSG_PEEK, (struct sockaddr*)&sa, &len,getIntegerField(ThisImpl, F_PlainDatagramSocketImpl_timeout),ThisImpl);
    woempa (6, "socketfd = %d, peek result = %d\n", sock, port);

    if (port == -1) {
      //woempa(9,"ERROR = %s\n", w_strerror((int)w_errno(sock)));
      throwException(JNIEnv2w_thread(env), clazzConnectException, "connect failed: %s",strerror(errno));
      return port;
    }

    setIntegerField(address, F_InetAddress_family, sa.sin_family);
    port = w_switchPortBytes(sa.sin_port);
    setIntegerField(address, F_InetAddress_address, ntohl(sa.sin_addr.s_addr));

  }

  return port;

}

int PlainDatagramSocketImpl_receive(JNIEnv* env , w_instance ThisImpl, w_instance packet) {

  if (packet == NULL) {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
  }
  else {
    w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainDatagramSocketImpl_wotsit);
  	
    if (!getBooleanField(ThisImpl, F_PlainDatagramSocketImpl_open)) {
      woempa(9, "Error socket %i is closed\n", sock);
      throwException(JNIEnv2w_thread(env), clazzIOException, "socket is already closed");
    }  	  	  	
    else {
      struct sockaddr_in sa;
      w_instance Buffer = getReferenceField(packet, F_DatagramPacket_bytes);
      w_sbyte *buffer = instance2Array_byte(Buffer);
      w_int result = instance2Array_length(Buffer);
      socklen_t len = sizeof (sa);
      w_int offset = getIntegerField(packet, F_DatagramPacket_offset);
      w_int length = getIntegerField(packet, F_DatagramPacket_length);

      if (offset < 0 || length < 0 || (offset + length) > result) {
        woempa(9, "Error socket OutOfBoundsException (off %i, len %i, length %i)\n",offset,length,result);
        throwException(JNIEnv2w_thread(env), clazzIOException, "receive failed: %s",strerror(errno));
        return 0;
      }
     	
      memset(&sa,0,sizeof(sa));
      buffer += offset;
      result = w_recvfrom(sock, buffer, (w_word)length, MSG_NOSIGNAL, (struct sockaddr*)&sa, &len, getIntegerField(ThisImpl, F_PlainDatagramSocketImpl_timeout),ThisImpl);
      woempa (6, "socketfd = %d, receive result = %d\n", sock, result);
     	
      if (result == -1) {
        if (len) { 		     		
          //woempa(9, "ERROR in receive = %s\n", w_strerror((int)w_errno(sock)));
          throwException(JNIEnv2w_thread(env), clazzIOException, NULL);
        }
     	else {
          woempa(9,"ERROR receive timed out \n");
          throwException(JNIEnv2w_thread(env), clazzInterruptedIOException, NULL);	     	
        }
      }
      else {
        setIntegerField(packet, F_DatagramPacket_length, result);
        setIntegerField(packet, F_DatagramPacket_port, w_switchPortBytes(sa.sin_port));
        return ntohl(sa.sin_addr.s_addr);
      }
    }
  }

  return 0;

}

void PlainDatagramSocketImpl_send(JNIEnv* env , w_instance ThisImpl, w_instance packet) {

  if (packet == NULL) {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
  }
  else {
    w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainDatagramSocketImpl_wotsit);
  	
    if (!getBooleanField(ThisImpl, F_PlainDatagramSocketImpl_open)) {
      woempa(9, "Error socket %i is closed\n", sock);
      throwException(JNIEnv2w_thread(env), clazzIOException, "socket is already closed");
    }  	  	  	
    else {
      struct sockaddr_in sa;
      w_sbyte * buffer = instance2Array_byte(getReferenceField(packet, F_DatagramPacket_bytes));
      w_int result = getIntegerField(packet, F_DatagramPacket_port);
      w_instance address = getReferenceField(packet, F_DatagramPacket_address);

      buffer += getIntegerField(packet, F_DatagramPacket_offset);
      memset(&sa,0,sizeof(sa));
      sa.sin_addr.s_addr = htonl(getIntegerField(address, F_InetAddress_address));
      sa.sin_family = AF_INET;
      sa.sin_port = w_switchPortBytes(result);

      result = w_sendto(sock, buffer, getIntegerField(packet, F_DatagramPacket_length), MSG_NOSIGNAL, (struct sockaddr*)&sa, sizeof (struct sockaddr_in),ThisImpl);
      if (result == -1){
        //woempa(9,"ERROR in sendto = %s\n", w_strerror((int)w_errno(sock)));
        throwException(JNIEnv2w_thread(env), clazzIOException, "send failed: %s",strerror(errno));
      }
    }
  }

}

void PlainDatagramSocketImpl_setTimeToLive(JNIEnv* env, w_instance ThisImpl, w_int value) {

  w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainDatagramSocketImpl_wotsit);
      	
  if (!getBooleanField(ThisImpl, F_PlainDatagramSocketImpl_open)) {
    woempa(9, "Error socket %i is closed\n", sock);
    throwException(JNIEnv2w_thread(env), clazzIOException, "socket is already closed");
  }
  else if (value > 255 || value < 0) {
    woempa(9, "Error invalid argument passed %d\n", value);
    throwException(JNIEnv2w_thread(env), clazzIllegalArgumentException, "invalid TTL value %i",value);
  }  	  	  	
  else {
    unsigned char val = (unsigned char) value;
    if  (w_setsockopt(sock, IPPROTO_IP, IP_MULTICAST_TTL, &val, sizeof(val))) {
      //woempa(9,"ERROR in setTimeToLive = %s\n", w_strerror((int)w_errno(sock)));		
      throwException(JNIEnv2w_thread(env), clazzSocketException, "setsocket option failed: %s",strerror(errno));
    }
  } 	
}

w_int PlainDatagramSocketImpl_getTimeToLive(JNIEnv* env, w_instance ThisImpl) {

  w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainDatagramSocketImpl_wotsit);

  if (!getBooleanField(ThisImpl, F_PlainDatagramSocketImpl_open)) {
    woempa(9, "Error socket %i is closed\n", sock);
    throwException(JNIEnv2w_thread(env), clazzSocketException, "socket is already closed");
    return sock;
  }  	  	  	
  else {
    unsigned char val;
    socklen_t len = sizeof(val);

    if (w_getsockopt(sock, IPPROTO_IP, IP_MULTICAST_TTL, &val, &len)) {
      //woempa(9,"ERROR in getTimeToLive = %s\n", w_strerror((int)w_errno(sock)));			
      throwException(JNIEnv2w_thread(env), clazzIOException, "getsocket option failed: $s",strerror(errno));
    }
    else {
      return (w_int) val;
    }

    return sock; 	
  }
  
}

void groupRequest(JNIEnv* env, w_instance ThisImpl, w_instance address, w_int action) {

  if (!address) {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
  }
  else {
    w_int sock = (w_int)getWotsitField(ThisImpl, F_PlainDatagramSocketImpl_wotsit);
      	
    if (!getBooleanField(ThisImpl, F_PlainDatagramSocketImpl_open)) {
      woempa(9, "Error socket %i is closed\n", sock);
      throwException(JNIEnv2w_thread(env), clazzSocketException, "socket is already closed");
    }  	  	  	
    else {
      struct ip_mreq groupaddr;
      groupaddr.imr_multiaddr.s_addr = htonl(getIntegerField(address, F_InetAddress_address));
      groupaddr.imr_interface.s_addr = htonl(INADDR_ANY);

     if (w_setsockopt(sock, IPPROTO_IP, action, &groupaddr, sizeof(groupaddr)) == -1) {
       //woempa(9,"ERROR in join/leave = %s\n", w_strerror((int)w_errno(sock)));
       throwException(JNIEnv2w_thread(env), clazzSocketException, "setsocket option failed: %s", strerror(errno));
     }
   } 	
  }

}

void PlainDatagramSocketImpl_join(JNIEnv* env, w_instance ThisImpl, w_instance address) {
  groupRequest(env, ThisImpl, address, IP_ADD_MEMBERSHIP);
}

void PlainDatagramSocketImpl_leave(JNIEnv* env, w_instance ThisImpl, w_instance address) {
  groupRequest(env, ThisImpl, address, IP_DROP_MEMBERSHIP);
}

w_int PlainDatagramSocketImpl_optBindAddress(JNIEnv* env , w_instance ThisImpl, w_int sock) {

  struct sockaddr_in sa;
  socklen_t namelen = sizeof(sa);
  w_int res = w_getsockname(sock, (struct sockaddr *)&sa, &namelen);	
 	
  if (res == -1) {
    //woempa(9,"ERROR in optBindAddress = %s\n", w_strerror((int)w_errno(sock))); 	
    throwException(JNIEnv2w_thread(env), clazzSocketException, "get socket name failed: %s",strerror(errno));
  }
  else {
    res = ntohl(sa.sin_addr.s_addr);
  }

  return res;
  
}

w_int PlainDatagramSocketImpl_optLinger(JNIEnv* env , w_instance ThisImpl, w_int sock, w_int value) {

  struct linger lg;
  socklen_t len = sizeof(lg);

  if (value == -1) { //get
    if (w_getsockopt(sock, SOL_SOCKET, SO_LINGER, &lg, &len)) {
      //woempa(9,"ERROR in optLinger = %s\n", w_strerror((int)w_errno(sock))); 	
      throwException(JNIEnv2w_thread(env), clazzSocketException, "getsocket option linger failed: %s",strerror(errno));
      return 0;
    }
    if (lg.l_onoff) {
      return lg.l_linger;
    }
  }
  else {  //set
    if (value) {
      lg.l_linger = value;
    }
    lg.l_onoff = value;

    if (w_setsockopt(sock, SOL_SOCKET, SO_LINGER, &lg, len)) {
      //woempa(9,"ERROR in optLinger = %s\n", w_strerror((int)w_errno(sock))); 	
      throwException(JNIEnv2w_thread(env), clazzSocketException, "setsocket option linger failed: %s",strerror(errno));
    }
  }

  return 0;

}

w_boolean PlainDatagramSocketImpl_optNoDelay(JNIEnv* env , w_instance ThisImpl, w_int sock, w_boolean value, w_boolean get) {

  int val;     	
  socklen_t len = sizeof(int);

  if (get == WONKA_TRUE) { //get
    if (w_getsockopt(sock, IPPROTO_TCP, TCP_NODELAY, &val, &len)) {
      //woempa(9,"ERROR in optNoDelay = %s\n", w_strerror((int)w_errno(sock))); 	
      throwException(JNIEnv2w_thread(env), clazzSocketException, "getsocket option failed: %s",strerror(errno));
    }
    else {
      return (val == 0 ? WONKA_FALSE : WONKA_TRUE);
    }
  }
  else { //set
    val = (value == WONKA_TRUE ? 1 : 0);
    if (w_setsockopt(sock, IPPROTO_TCP, TCP_NODELAY, &val, len)) {
      //woempa(9,"ERROR in optNoDelay = %s(set fails ...)\n", w_strerror((int)w_errno(sock))); 	
      throwException(JNIEnv2w_thread(env), clazzSocketException, "setsocket option failed: %s",strerror(errno));
    }
  }

  return WONKA_TRUE;

}

w_int PlainDatagramSocketImpl_optMulticastIF(JNIEnv* env , w_instance ThisImpl, w_int sock, w_instance address, w_boolean get) {

  struct sockaddr_in sa;
  socklen_t len = sizeof(sa);
 	
  if (get == WONKA_TRUE) { //get
    if (w_getsockopt(sock, IPPROTO_IP, IP_MULTICAST_IF, &sa, &len)) {
      //woempa(9,"ERROR in optMulticastIF = %s\n", w_strerror((int)w_errno(sock))); 	
      throwException(JNIEnv2w_thread(env), clazzSocketException, "getsocket option failed: %s",strerror(errno));
    }
    else {
      return ntohl(sa.sin_addr.s_addr);
    }
  }
  else { //set
    sa.sin_family = (w_short)getIntegerField(address, F_InetAddress_family);
    sa.sin_addr.s_addr = htonl(getIntegerField(address, F_InetAddress_address));

    if  (w_setsockopt(sock, IPPROTO_IP, IP_MULTICAST_IF, &sa, len)) {
      //woempa(9,"ERROR in optMulticastIF = %s\n", w_strerror((int)w_errno(sock))); 	
      throwException(JNIEnv2w_thread(env), clazzSocketException, "setsocket option failed: %s",strerror(errno));
    }
  }

  return 0;

}

w_int PlainDatagramSocketImpl_optIntOptions(JNIEnv* env, w_instance ThisImpl, w_int sock, w_int value, w_int opt) {

  socklen_t len = sizeof(w_int);

  switch(opt) {
    case 0:
      opt = SO_REUSEADDR;
      break;
    case 1:
      opt = SO_RCVBUF;
      break;
    case 2:
      opt = SO_SNDBUF;
      break;
    default:
      woempa(9, "ERROR in optIntOptions: unknown options %d\n", opt);
      throwException(JNIEnv2w_thread(env), clazzSocketException, "invalid socket option");
      return 0;
  }

  if (value == -1) { // get
    if (w_getsockopt(sock, SOL_SOCKET, opt, &value, &len)) {
      //woempa(9,"ERROR (get) in optIntOptions = %s (%d,%d)\n", w_strerror((int)w_errno(sock)), opt, sock); 	
      throwException(JNIEnv2w_thread(env), clazzSocketException, "getsocket option failed: %s",strerror(errno));
    }
    else {
      return value;
    }
  }
  else { // set
    if (w_setsockopt(sock, SOL_SOCKET, opt, &value, len)) {
      //woempa(9,"ERROR (set) in optIntOptions = %s(%d,%d)\n", w_strerror((int)w_errno(sock)), opt, sock); 	
      throwException(JNIEnv2w_thread(env), clazzSocketException, "setsocket option failed: %s",strerror(errno));
    }
  }

  return 0;

}

void PlainDatagramSocketImpl_setSoTimeout(JNIEnv* env , w_instance ThisImpl, w_int sock, w_int value) {
  struct timeval tv;

  tv.tv_sec = value / 1000;
  tv.tv_usec = (value % 1000) * 1000;
  if (isSet(verbose_flags, VERBOSE_FLAG_SOCKET)) {
    printf("Socket: id = %d setting timeout to %d secs %d usecs\n", sock, (int)tv.tv_sec, (int)tv.tv_usec);
  }
    if (w_setsockopt(sock, SOL_SOCKET, SO_RCVTIMEO, &tv, sizeof(struct timeval))) {
      //woempa(9,"ERROR (set) in optIntOptions = %s(%d,%d)\n", w_strerror((int)w_errno(sock)), opt, sock); 	
      throwException(JNIEnv2w_thread(env), clazzSocketException, "setsocket option failed: %s",strerror(errno));
    }
}
