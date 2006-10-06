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
**************************************************************************/

/**
 * $Id: NetworkInterface.c,v 1.1 2006/10/04 14:25:22 cvsroot Exp $
 */

#include <string.h>
#include <errno.h>
#include <net/if.h>

#include "core-classes.h"
#include "exception.h"
#include "network.h"
#include "jni.h"
#include "wstrings.h"

w_int NetworkInterface_getAddressDevice(JNIEnv* env , w_instance ThisImpl, w_instance string) {
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
  return -1;
}

w_void NetworkInterface_getInterfaces(JNIEnv* env , w_instance ThisClass, w_instance list) {
  struct ifconf lc;
  int fd;

  memset(&lc, 0, sizeof(struct ifconf));
  fd = socket(PF_INET, SOCK_STREAM, 0);
  if(ioctl(fd, SIOCGIFCONF, &lc) !=-1 && lc.ifc_len > 0) {
    lc.ifc_req = allocMem(lc.ifc_len * sizeof(struct ifreq));
    if(lc.ifc_req != NULL) {
      if(ioctl(fd, SIOCGIFCONF, &lc) !=-1) {
        int i;
        jmethodID mid = (*env)->GetStaticMethodID(env, ThisClass, "addToList","(Ljava/util/Vector;ILjava/lang/String;)V");
        if(mid == NULL) {
          woempa(9,"Didn't find addToList method");
        }
        for(i=0 ; (i < lc.ifc_len) &&  strlen(lc.ifc_req[i].ifr_name); i++) {
          struct sockaddr_in* addr = (struct sockaddr_in*) &lc.ifc_req[i].ifr_addr;
          (*env)->CallStaticVoidMethod(env, ThisClass, mid, list,  ntohl(addr->sin_addr.s_addr),
                                      (*env)->NewStringUTF(env, lc.ifc_req[i].ifr_name));
        }
      }
    }
  }
  close(fd);
}
