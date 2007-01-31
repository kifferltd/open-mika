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
** $Id: network.c,v 1.2 2005/07/02 12:49:54 cvs Exp $
*/

#include <oswald.h>
#include <network.h>
#include <wonka.h>

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

void startNetwork(void) {
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
}

