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

#include <string.h>
#include <errno.h>
//#include <net/if.h>

#include "core-classes.h"
#include "exception.h"
#include "network.h"
#include "jni.h"
#include "wstrings.h"

w_int NetworkInterface_getAddressDevice(JNIEnv* env , w_instance ThisImpl, w_instance string) {
/* TODO : re-write me
  struct ifreq ifr;
  struct sockaddr_in* addr;
  w_string name;
  int ret;
  int fd;
  if(string == NULL) {
    w_thread thread = JNIEnv2w_thread(env);
    throwException(thread, clazzNullPointerException, NULL);
    return -1;
  }

  name = String2string(string);
  if((string_length(name) + 1) > sizeof(ifr.ifr_name)) {
    return -1;
  }

  x_snprintf(ifr.ifr_name, string_length(name) + 1, "%w", name);
  fd = socket(PF_INET, SOCK_STREAM, 0);
  ret = ioctl(fd, SIOCGIFADDR, &ifr);
  close(fd);
  if(ret < 0) {
    woempa(9, "NetworkInterface: iotclt failed %s (%d)\n",strerror(errno),errno);
    return -1;
  }

  addr = (struct sockaddr_in*)&ifr.ifr_addr;
  if(addr != NULL) {
    woempa(6,"NetworkInterface: addr is %x\n", ntohl(addr->sin_addr.s_addr));
    return ntohl(addr->sin_addr.s_addr);
  } 
*/
  return -1;
}

w_void NetworkInterface_getInterfaces(JNIEnv* env , w_instance ThisClass, w_instance list) {
/* TODO : re-write me
  jmethodID mid = (*env)->GetStaticMethodID(env, ThisClass, "addToList","(Ljava/util/Vector;ILjava/lang/String;)V");
  struct ifreq *ifr;
  struct ifconf ifc;
  int fd;
  int n;

  if (mid == NULL) {
    wabort(ABORT_WONKA, "NetworkInterface.getNetworkInterfaces: couldn't find addToList method");
  }
  memset(&ifc, 0, sizeof(struct ifconf));
  ifc.ifc_ifcu.ifcu_req = NULL;
  ifc.ifc_len = 0;

  fd = socket(AF_INET, SOCK_DGRAM, 0);
  if (fd < 0) {
    wabort(ABORT_WONKA, "NetworkInterface.getNetworkInterfaces: unable to open socket");
  }

  if (ioctl(fd, SIOCGIFCONF, &ifc) < 0 || ifc.ifc_len <= 0) {
    wabort(ABORT_WONKA, "NetworkInterface.getNetworkInterfaces: 1st SIOCGIFCONF failed");
  }

  // allocate double just in case someone created more interfaces already
  ifr = allocMem(ifc.ifc_len * 2);
  if (ifr == NULL) {
    woempa(9, "Unable to allocMem for SIOCGIFCONF\n");
    return;
  }

  ifc.ifc_ifcu.ifcu_req = ifr;
  if (ioctl(fd, SIOCGIFCONF, &ifc) < 0) {
    wabort(ABORT_WONKA, "NetworkInterface.getNetworkInterfaces: 2nd SIOCGIFCONF failed");
  }
  close(fd);

  struct ifreq *cursor = ifr;
  n = ifc.ifc_len / sizeof(struct ifreq);
  while (n--) {
    struct sockaddr_in *addr = (struct sockaddr_in *)&cursor->ifr_addr;
    (*env)->CallStaticVoidMethod(env, ThisClass, mid, list,  ntohl(addr->sin_addr.s_addr),
                                      (*env)->NewStringUTF(env, cursor->ifr_name));
    cursor++;
   }

  releaseMem(ifr);
*/
}
