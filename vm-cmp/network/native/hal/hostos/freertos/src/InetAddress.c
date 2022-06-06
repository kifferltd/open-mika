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

#include <network.h>
#include <errno.h>

#include "arrays.h"
#include "core-classes.h"
#include "exception.h"
#include "fields.h"
#include "wstrings.h"
#include "FreeRTOS_IP.h"
#define w_ntohl FreeRTOS_ntohl


// CG I don't think this is needed on FreeRTOS
w_boolean InetAddress_static_lookupName(w_thread thread, w_instance thisClass, w_instance InetAddress) {
  return TRUE;
}

/**
** hostName will be set to name ...
*/

void InetAddress_createInetAddress (w_thread thread, w_instance InetAddress, w_instance Name) {
printf("Calling InetAddress_createInetAddress(%p, %p, %p)\n", thread, InetAddress, Name);

  long ipnumber;
  w_string name = String2string(Name);
  char * hostname = allocMem(string_length(name) + 1);
  w_word host;

  if (!hostname) {
    return;
  }

  x_snprintf(hostname, string_length(name) + 1, "%w", name);
  // Check for special case where we are creating an InetAddress for our own IP interface
  char ownhostname[32];
  w_gethostname(ownhostname, 32);
  if (strcmp(hostname, ownhostname) == 0) {
    w_printf("looking up our own hostname %s\n", hostname);
    host = FreeRTOS_IPAddress;
  }
  else {
    // Assuming that only IPv4 is supported
    w_printf("looking up %s\n", hostname);
    host = w_gethostbyname(hostname);
    w_printf("result = %08x\n", host);
  }

  if (! host) {
    woempa(9, "unable to find DNS name for '%s' due to %s", hostname,strerror(errno));
    // The exception should include hstrerror(h_errno)
    throwException(thread, clazzUnknownHostException, "unable to find DNS name for '%s'", hostname);
    releaseMem(hostname);
    return;
  }

  releaseMem(hostname);

  setReferenceField(InetAddress, Name, F_InetAddress_hostName);

  /*
  ** Copying the 'type' into the 'type' private field of InetAddress Object
  ** Loopback	-> type = 1
  ** IPv4 	-> type = 2
  */

  // HACK HACK HACK
  if ((host & 0xff000000) == 0x7f000000) {
    setIntegerField(InetAddress, F_InetAddress_family, 1);
  }
  else {
    setIntegerField(InetAddress, F_InetAddress_family, 2);
  }

  /*
  ** 1.Retrieving the dotted decimal representation of the ip number.
  ** 2.Copying the octet stuff into the 'address' private field of InetAddress Object
  */

  setIntegerField(InetAddress, F_InetAddress_address, w_ntohl(host));
  woempa(7, "IP address is %d.%d.%d.%d\n", ((char*)&ipnumber)[0], ((char*)&ipnumber)[1], ((char*)&ipnumber)[2], ((char*)&ipnumber)[3]);
printf("InetAddress_createInetAddress(%p, %p, %p) -> %08x\n", thread, InetAddress, Name, host);
}

w_instance InetAddress_getLocalName(w_thread thread, w_instance theClass) {
printf("Calling InetAddress_getLocalName(%p, %p)\n", thread, theClass);
  char ownhostname[32];
  w_string ownhostname_string;

  if (w_gethostname(ownhostname, 32) == 0) {
    ownhostname_string = cstring2String(ownhostname, strlen(ownhostname));
    woempa(7, "Own host name is '%w'\n", ownhostname_string);
  }
  else {
    ownhostname_string = cstring2String("IM4000", 6);
  }
  
printf("InetAddress_getLocalName(%p, %p) returned %w\n", thread, theClass, ownhostname_string);
  w_instance theName = getStringInstance(ownhostname_string);
  deregisterString(ownhostname_string);

  return theName;

}
