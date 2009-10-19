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

package java.net;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public abstract class SocketImpl implements SocketOptions {

  protected InetAddress address;
  protected FileDescriptor 	fd;
  protected int localport;
  protected int	port;

  public SocketImpl() {}

  protected abstract void accept(SocketImpl s) throws IOException;
  
  protected abstract int available() throws IOException;

  protected abstract void bind(InetAddress host, int port) throws IOException;

  protected abstract void close() throws IOException;

  protected abstract void connect(InetAddress address, int port) throws IOException;

  protected abstract void connect(SocketAddress address, int timeout) throws IOException;

  protected abstract void connect(String host, int port) throws IOException;

  protected abstract void create(boolean stream) throws IOException;

  protected FileDescriptor getFileDescriptor() {
    return fd;
  }

  protected InetAddress getInetAddress() {
    return address;
  }

  protected abstract InputStream getInputStream() throws IOException;

  protected abstract OutputStream getOutputStream() throws IOException;

  protected int getLocalPort() {
    return localport;
  }

  protected int getPort() {
    return port;
  }

  protected abstract void listen(int backlog) throws IOException;

  protected abstract void sendUrgentData(int udata) throws IOException;

  protected boolean supportsUrgentData() {
    return false;
  }

  public String toString() {
    return address+":"+port+" on localport "+localport;
  }

  protected void shutdownInput() throws IOException {
    throw new IOException("shutdownInput(): unsupported Feature in "+getClass());
  }

  protected void shutdownOutput() throws IOException {
    throw new IOException("shutdownOutput(): unsupported Feature in "+getClass());
  }
}
