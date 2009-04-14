/**************************************************************************
* Copyright (c) 2007, 2009 by Chris Gray, /k/ Embedded Java Solutions.    *
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
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written            *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS     *
* BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,     *
* OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT    *
* OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR      *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,       *
* EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                      *
**************************************************************************/

package java.net;

import wonka.vm.SecurityConfiguration;
import java.security.AccessController;
import java.security.Security;

/*
** Package-visible class used to get security properties with full privileges.
*/

class GetSecurityProperty implements java.security.PrivilegedAction {

  static final int INETADDRESS_CACHE_TTL;

  private String key;
  private Object value;

  /**
   ** Our static initialiser fetches the security property which
   ** is needed by InetAddress.
   */
  static {
    int l;

    if (SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      GetSecurityProperty gsp = new GetSecurityProperty("inetaddress.cache.ttl");
      AccessController.doPrivileged(gsp);
      Integer l0 = (Integer)gsp.get();
      if (l0 == null) {
        gsp = new GetSecurityProperty("mika.inetaddress.cache.ttl");
        AccessController.doPrivileged(gsp);
        l0 = (Integer)gsp.get();
      }
      l = (l0 == null) ? 86400 : l0.intValue();
    }
    else {
      String prop = Security.getProperty("inetaddress.cache.ttl");
      if (prop == null) {
        prop = Security.getProperty("mika.inetaddress.cache.ttl");
      }
      try {
        l = Integer.parseInt(prop);
      }
      catch (Throwable t) {
        l = 86400;
      }
    }

    INETADDRESS_CACHE_TTL = l;
  }

  /**
   ** Constructor which just takes a key.
   */
  public GetSecurityProperty(String key) {
    this.key = key;
  }

  /**
   ** This gets called by doPrivileged().
   */
  public Object run() {
    try {
      value = new Integer(Integer.parseInt(Security.getProperty(key)));
    }
    catch (Throwable t) {
      // t.printStackTrace();
    }

    return null;
  }

  /**
   ** Return the value to the caller.
   */
  public Object get() {
    return value;
  }

}

