/**************************************************************************
* Copyright (c) 2020 by KIFFER Ltd. All rights reserved.                  *
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

/* TODO : re-write me
int (*x_socket)(int domain, int type, int protocol);
int (*x_connect)(int sockfd, const struct sockaddr *serv_addr, socklen_t addrlen);
int (*x_send)(int s, const void *msg, size_t len, int flags);
int (*x_sendto)(int s, const void *msg, size_t len, int flags, const struct sockaddr *to, socklen_t tolen);
int (*x_sendmsg)(int s, const struct msghdr *msg, int flags);
int (*x_recv)(int s, void *buf, size_t len, int flags);
int (*x_recvfrom)(int s, void *buf, size_t len, int flags, struct sockaddr *from, socklen_t *fromlen);
int (*x_recvmsg)(int s, struct msghdr *msg, int flags);
int (*x_accept)(int s, struct sockaddr *addr, socklen_t *addrlen);
ssize_t (*x_read)(int fd, void *buf, size_t count);
ssize_t (*x_write)(int fd, const void *buf, size_t count);
*/

void startNetwork(void) {
/* TODO : re-write me
  x_socket = socket;
  x_connect = connect;
  x_send = send;
  x_sendto = sendto;
  x_sendmsg = sendmsg;
  x_recv = recv;
  x_recvfrom = recvfrom;
  x_recvmsg = recvmsg;
  x_accept = accept;
  x_read = read;
  x_write = write;
*/
}

