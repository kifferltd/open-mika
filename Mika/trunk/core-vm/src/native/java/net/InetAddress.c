/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: InetAddress.c,v 1.8 2006/10/04 14:24:16 cvsroot Exp $
*/

#include <network.h>
#include <errno.h>

#include "arrays.h"
#include "core-classes.h"
#include "exception.h"
#include "fields.h"
#include "network.h"
#include "wstrings.h"

/*
** CG HACK to ensure we can always look up own name ...
*/
static char ownhostname[256];

w_boolean InetAddress_lookupName(JNIEnv *env, w_instance InetAddress) {

  w_boolean result = FALSE;
  w_string name = String2string(getReferenceField(InetAddress, F_InetAddress_addressCache));
  struct hostent * host = NULL;
  char * hostname = allocMem(string_length(name) + 1);
  int af = 0;

  if (!hostname) {
    return FALSE;
  }

  x_snprintf(hostname, string_length(name) + 1, "%w", name);
#ifdef AF_INET6
  if(instance2clazz(InetAddress) == clazzInet6Address) {
    af = AF_INET6;
  }
  else {
    af = AF_INET;
  }
#else
  af = AF_INET;
#endif
  host = w_gethostbyname(hostname);
  if (host) {
    woempa(7, "canonical name = '%s'\n", host->h_name);
    setReferenceField(InetAddress, (*env)->NewStringUTF(env, host->h_name), F_InetAddress_hostName);
    result = TRUE;
  }
  else {
    woempa(7, "DNS lookup for '%s' failed! (Shouldn't we throw an exception?)\n", hostname);
    // The exception should include hstrerror(h_errno)
  }
  releaseMem(hostname);

  return result;

}

/**
** hostName will be set to name ...
*/

void InetAddress_createInetAddress (JNIEnv *env, w_instance InetAddress, w_instance Name) {

  w_thread thread = JNIEnv2w_thread(env);
  long ipnumber;
  w_string name = String2string(Name);
  w_string h_name = NULL;
  char * hostname = allocMem(string_length(name) + 1);
  struct hostent * host = NULL;
  int af = 0;

  if (!hostname) {
    return;
  }

  x_snprintf(hostname, string_length(name) + 1, "%w", name);
#ifdef AF_INET6
  if(instance2clazz(InetAddress) == clazzInet6Address) {
    af = AF_INET6;
  }
  else {
    af = AF_INET;
  }
#else
  af = AF_INET;
#endif
  //wprintf("looking up %s (%d)\n", hostname, af);
  host = w_gethostbyname2(hostname, af);
  //wprintf("result = %p\n", host);

  if (! host) {
    woempa(9, "unable to find DNS name for '%s' due to %s", hostname,strerror(errno));
#ifdef UNC20
    // No hstrerror()???
    throwException(thread, clazzUnknownHostException, "unable to find DNS name for '%s'", hostname);
#else
    throwException(thread, clazzUnknownHostException, "unable to find DNS name for '%s': %s", hostname,hstrerror(h_errno));
#endif
    releaseMem(hostname);
    return;
  }

  woempa(1, "displaying DNS information... name = %s (%p)\n", host->h_name, hostname);
  woempa(1, "displaying DNS information... length = %d (%p)\n", host->h_length, hostname);

  /*
  ** Creating and copying the hostname to private 'hostName' field of InetAddress Object
  */

  //wprintf("canonical name = %s (%d)\n", h_name, host->h_addrtype);
  h_name = cstring2String(host->h_name, strlen(host->h_name));
  setReferenceField(InetAddress, newStringInstance(h_name), F_InetAddress_hostName);
  deregisterString(h_name);
  //setReferenceField(InetAddress, Name, F_InetAddress_hostName);

  /*
  ** Copying the 'type' into the 'type' private field of InetAddress Object
  ** Loopback	-> type = 1
  ** IPv4 	-> type = 2
  */

  switch (host->h_addrtype) {
  case (AF_LOCAL) :
    setIntegerField(InetAddress, F_InetAddress_family, 1);
    break;

  case (AF_INET) :
    setIntegerField(InetAddress, F_InetAddress_family, 2);
    break;

  case (AF_INET6) :
    setIntegerField(InetAddress, F_InetAddress_family, 6);
  }

  /*
  ** 1.Retrieving the dotted decimal representation of the ip number.
  ** 2.Copying the octet stuff into the 'address' private field of InetAddress Object
  */

  ipnumber = *((w_word*)host->h_addr_list[0]);

  releaseMem(hostname);
  setIntegerField(InetAddress, F_InetAddress_address, ntohl(ipnumber));
  woempa(7, "IP address is %d.%d.%d.%d\n", ((char*)&ipnumber)[0], ((char*)&ipnumber)[1], ((char*)&ipnumber)[2], ((char*)&ipnumber)[3]);
  //wprintf("IP address is %d.%d.%d.%d\n", ((char*)&ipnumber)[0], ((char*)&ipnumber)[1], ((char*)&ipnumber)[2], ((char*)&ipnumber)[3]);
}

w_instance InetAddress_getLocalName(JNIEnv *env, w_instance clazz) {

//  static const w_size length = 255;
  w_instance Name = NULL;
/* CG WAS:
  char * name = allocMem(W_Thread_system, length * sizeof(char));

  if (name && w_gethostname(name, length) == 0) {
    woempa(7, "w_gethostname => '%s'\n", name);
    Name = (*env)->NewStringUTF(env, name);
  }

  if (name) {
    releaseMem(name);
  }
*/
  if (w_gethostname(ownhostname, 255) == 0) {
    woempa(7, "Own host name is '%s'\n", ownhostname);
    Name = (*env)->NewStringUTF(env, ownhostname);
  } 

  return Name;

}
