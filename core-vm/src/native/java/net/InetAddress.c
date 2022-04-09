/**************************************************************************
* Copyright (c) 2020, 2021 by KIFFER Ltd. All rights reserved.            *
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

// TODO - rewrite to work without JNI

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

w_boolean InetAddress_static_lookupName(w_thread thread, w_instance thisClass, w_instance InetAddress) {

  w_boolean result = FALSE;
#if NETWORKING == native
  w_instance addrCache;
  w_string name;
// TODO make a w_hostent
// struct hostent * host = NULL;
  char * hostname;
  int af = 0;

  if (InetAddress == NULL) {
    return FALSE;
  }

  addrCache = getReferenceField(InetAddress, F_InetAddress_ipAddressString);

  if(addrCache == NULL) {
    w_dump("InetAddress_lookupName: Avoiding Segfault\n");
    return FALSE;
  }

  name = String2string(addrCache);

  if(name == NULL) {
    w_dump("w_string of String %p is NULL\n",addrCache);
  }

  hostname = allocMem(string_length(name) + 1);

  if (!hostname) {
    return FALSE;
  }

  x_snprintf(hostname, string_length(name) + 1, "%w", name);
/* TODO re-write me!
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
    w_string hname_string = cstring2String(host->h_name, strlen(host->h_name));
    setReferenceField(InetAddress, newStringInstance(hname_string), F_InetAddress_hostName);
    deregisterString(hname_string);
    result = TRUE;
  }
  else {
    woempa(7, "DNS lookup for '%s' failed! (Shouldn't we throw an exception?)\n", hostname);
    // The exception should include hstrerror(h_errno)
  }
*/
  releaseMem(hostname);
#endif

  return result;

}

/**
** hostName will be set to name ...
*/

void InetAddress_createInetAddress (w_thread thread, w_instance InetAddress, w_instance Name) {

#if NETWORKING == native
  long ipnumber;
  w_string name = String2string(Name);
  w_string hname = NULL;
  char * hostname = allocMem(string_length(name) + 1);
// TODO make a w_hostent
//  struct hostent * host = NULL;
  int af = 0;

  if (!hostname) {
    return;
  }

/* TODO : re-write me
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
  //w_printf("looking up %s (%d)\n", hostname, af);
  host = w_gethostbyname2(hostname, af);
  //w_printf("result = %p\n", host);

  if (! host) {
    woempa(9, "unable to find DNS name for '%s' due to %s", hostname,strerror(errno));
#ifdef UNC20
    // No hstrerror()???
    throwException(thread, clazzUnknownHostException, "unable to find DNS name for '%s'", hostname);
#else
    // The exception should include hstrerror(h_errno)
    throwException(thread, clazzUnknownHostException, "unable to find DNS name for '%s'", hostname);
#endif
    releaseMem(hostname);
    return;
  }

  woempa(1, "displaying DNS information... name = %s (%p)\n", host->h_name, hostname);
  woempa(1, "displaying DNS information... length = %d (%p)\n", host->h_length, hostname);
*/

  /*
  ** Creating and copying the hostname to private 'hostName' field of InetAddress Object
  ** TODO : re-write me

  //w_printf("canonical name = %s (%d)\n", hname, host->h_addrtype);
  w_string hname_string = cstring2String(host->h_name, strlen(host->h_name));
  setReferenceField(InetAddress, getStringInstance(hname_string), F_InetAddress_hostName);
  deregisterString(hname_string);
  //setReferenceField(InetAddress, Name, F_InetAddress_hostName);
  */

  /*
  ** Copying the 'type' into the 'type' private field of InetAddress Object
  ** Loopback	-> type = 1
  ** IPv4 	-> type = 2
  */

/* TODO : re-write me
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
*/

  /*
  ** 1.Retrieving the dotted decimal representation of the ip number.
  ** 2.Copying the octet stuff into the 'address' private field of InetAddress Object
  */

/* TODO : re-write me
  ipnumber = *((w_word*)host->h_addr_list[0]);
*/

  releaseMem(hostname);
  setIntegerField(InetAddress, F_InetAddress_address, FreeRTOS_ntohl(ipnumber));
  woempa(7, "IP address is %d.%d.%d.%d\n", ((char*)&ipnumber)[0], ((char*)&ipnumber)[1], ((char*)&ipnumber)[2], ((char*)&ipnumber)[3]);
  //w_printf("IP address is %d.%d.%d.%d\n", ((char*)&ipnumber)[0], ((char*)&ipnumber)[1], ((char*)&ipnumber)[2], ((char*)&ipnumber)[3]);
#endif
}

w_instance InetAddress_getLocalName(w_thread thread, w_instance clazz) {

  w_instance Name = NULL;

  if (w_gethostname(ownhostname, 255) == 0) {
    woempa(7, "Own host name is '%s'\n", ownhostname);
    w_string ownhostname_string = cstring2String(ownhostname, strlen(ownhostname));
    setReferenceField(Name, getStringInstance(ownhostname_string), F_InetAddress_hostName);
    deregisterString(ownhostname_string);
  }

  return Name;

}
