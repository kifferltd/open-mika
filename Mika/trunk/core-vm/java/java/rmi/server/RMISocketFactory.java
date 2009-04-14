/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2009 by Chris Gray, /k/ Embedded Java Solutions.    *
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
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package java.rmi.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public abstract class RMISocketFactory implements RMIClientSocketFactory, RMIServerSocketFactory {

  static final RMISocketFactory theDefaultSocketFactory = new wonka.rmi.DefaultRMISocketFactory();
  static RMISocketFactory currentSocketFactory = null;
  static RMIFailureHandler theFailureHandler = null;

  public RMISocketFactory() {}

  public abstract ServerSocket createServerSocket(int port) throws IOException;
  
  public abstract Socket createSocket(String host, int port) throws IOException;
  
  public static RMISocketFactory getDefaultSocketFactory() {
    return theDefaultSocketFactory;
  }
  
  public static RMIFailureHandler getFailureHandler() {
    return theFailureHandler;
  }
  
  public static RMISocketFactory getSocketFactory() {
    return currentSocketFactory;
  }
  
  public static void setFailureHandler(RMIFailureHandler fh) {
    theFailureHandler = fh;
  }
  
  public static void setSocketFactory(RMISocketFactory fac) throws IOException {
    if(currentSocketFactory != null){
      throw new IOException("Factory is already set");
    }

    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkSetFactory();
      }
    }

    currentSocketFactory = fac;
  }

  static RMISocketFactory getRMISocketFactory(){
    return currentSocketFactory == null ? theDefaultSocketFactory : currentSocketFactory;
  }

}

