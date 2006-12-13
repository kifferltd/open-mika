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
** $Id: network.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

/*
**
** ---- Network Abstraction Layer ----
**
** This file represents the network interface of Wonka.
**
** ---- None Version, returning -1 (implying an error occured)
*/

#define AF_LOCAL 0
#define AF_INET  0
#define AF_INET6 0

#define PF_INET 0
#define INADDR_ANY 0

#define MSG_NOSIGNAL 0
#define MSG_PEEK 0

#define FIONREAD 0
#define IPPROTO_IP 0
#define IP_MULTICAST_TTL   0
#define IP_ADD_MEMBERSHIP   0
#define IP_DROP_MEMBERSHIP   0
#define SOL_SOCKET   0
#define SO_LINGER   0
#define IPPROTO_TCP   0
#define TCP_NODELAY   0
#define IP_MULTICAST_IF   0
#define SO_REUSEADDR   0
#define SO_RCVBUF   0
#define SO_SNDBUF   0

#define SOCK_STREAM 0
#define SOCK_DGRAM  0
#define SO_KEEPALIVE  0

typedef unsigned int socklen_t;
typedef unsigned int nsize_t;
struct timeval{
  int tv_sec;
  int tv_usec;
};

struct linger{
  int l_onoff;
  int l_linger;
};

typedef struct sockaddr{
   unsigned short sa_family;
   char sa_data[14];
}sockaddr;

typedef struct hostent {
  char*  h_name;		  //--> name of host
  char** h_aliases; 	//--> list of host aliases
  int    h_addrtype;	//--> type of host address (AF_INET for IPv4)
  int    h_length;		//--> address length
  char** h_addr_list;	//--> list of addresses
}hostent;

struct in_addr {
  unsigned long s_addr;
};

struct ip_mreq {
  struct in_addr imr_multiaddr;
  struct in_addr imr_interface;
};

struct sockaddr_in {
   unsigned short sin_family; /*Internet protocol(AF_INET)*/
   long sin_port;             /*address port (32 bit)*/
   struct in_addr sin_addr;   /*Ip address (32 bit)*/
   char sin_zero[6];          /*not used*/
};

static inline int ioctl(int s, int req, int* arg){
  return -1;
}

static inline int w_switchPortBytes(int port){
  return port;
}

static inline int w_gethostname(char* name, nsize_t size) {
  return -1;
}

static inline int w_errno(int s){
  return -1;
}

static inline char* w_strerror(int errno) {
  return "NO NETWORK AVAILABLE";
}

static inline int w_socket(int x,int y,int z)	{
  return -1;
}

static inline int w_connect(int s, void *a, socklen_t l) {
  return -1;
}
	
static inline int w_setsockopt(int s, int level, int name, void *a, socklen_t l)	{
  return -1;
}

static inline int w_getsockopt(int s, int level, int name, void *a, socklen_t *l)	{
  return -1;
}

static inline unsigned long int htonl(long int l){
  return l;
}
static inline long int ntohl(unsigned long int l){
  return l;
}

static inline int w_socketclose(int s) {  	
  return -1;
}

static inline int w_send(int s,char *b,socklen_t l,int f) {   	
  return -1;
}

static inline int w_recv(int* This, int s, void *b,socklen_t l, int f, int* t) {    	
  return -1;
}

static inline int w_accept(int* This, int s, void* a,socklen_t *l, int t) {    	
  return -1;
}

static inline int w_bind(int s, struct sockaddr *a,socklen_t l) {      	
  return -1;
}

static inline int w_listen(int s, socklen_t i) {      	
  return -1;
}

static inline int w_getsockname(int  s , void* name , socklen_t * namelen){
  return -1;
}

static inline int w_getpeername(int s, struct sockaddr_in a) {	
  return -1;
}

static inline hostent* w_gethostbyname(const char *n) {	
  return 0;
}

static inline int w_recvfrom(int s, void *buf, nsize_t len, int flags, void *from, socklen_t *fromlen, int t, int* This){
  return -1;
}

static inline int w_sendto(int s, const void *msg, nsize_t len, int flags, const void* to, socklen_t tolen, int* This){
  return -1;
}

static inline int shutdown(int s,int h){
  return -1;
}

extern int errno;

static inline short ntohs(short s){
  return (short)(s<<8) | (s>>8);
}

static inline short htons(short s){
  return (short)(s<<8) | (s>>8);
}

#endif /* _NETWORK_H */
