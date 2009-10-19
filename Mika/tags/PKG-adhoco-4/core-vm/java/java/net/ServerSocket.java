/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2007 by Chris Gray, /k/ Embedded Java Solutions.    *
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

package java.net;

import java.io.*;

public class ServerSocket {

  private static SocketImplFactory factory;

  private SocketImpl socket;
  private InetAddress local;

  public ServerSocket() throws IOException {
    socket = (factory == null ? new PlainSocketImpl() : factory.createSocketImpl());
    //TODO Need more...
  }
  
  public ServerSocket (int port) throws IOException {
    this(port, 50, null);
  }

  public ServerSocket (int port, int backlog) throws IOException {
    this(port, backlog, null);
  }

  public ServerSocket (int port, int backlog, InetAddress local) throws IOException {
    if(port < 0 || port > 65535){
      throw new IllegalArgumentException();
    }
    
    InetAddress.listenCheck(port);
    
    if (local == null) {
          local = InetAddress.allZeroAddress;
    }
    this.local = local;
    socket = (factory == null ? new PlainSocketImpl() : factory.createSocketImpl());  
    socket.create(true);
    socket.bind(local,port);  
    socket.listen(backlog);
  }

  public Socket accept() throws IOException {
     Socket s = new Socket();
     implAccept(s);
     InetAddress.acceptCheck(s.getInetAddress().getHostAddress(), s.getPort());
     return s;
  }

  protected final void implAccept(Socket s) throws IOException {
     socket.accept(s.socket);
     s.bound = true;
  }

  public void close() throws IOException {
     socket.close();
  }

  public InetAddress getInetAddress() {
     return local;
  }

  public int getLocalPort () {
    return socket.localport;
  }

  public synchronized int getSoTimeout() throws IOException {
     return ((Integer)socket.getOption(SocketOptions.SO_TIMEOUT)).intValue();
  }

  public synchronized void setSoTimeout(int timeout) throws SocketException {
     if ( timeout < 0) {
        throw new IllegalArgumentException();
     }
     socket.setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
  }


  public synchronized static void setSocketFactory(SocketImplFactory fact) throws IOException {
     if (factory != null) {
       throw new SocketException("factory already set");
     }
     
     InetAddress.factoryCheck();
     
     factory = fact;
     
  }

  public String toString() {
     return "ServerSocket[address = "+local+"at port = "+socket.localport+"]";
  }
}
