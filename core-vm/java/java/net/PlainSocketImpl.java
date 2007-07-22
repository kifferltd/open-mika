/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package java.net;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

class PlainSocketImpl extends SocketImpl {

  private InputStream in;
  private OutputStream out;
  private int timeout = 0;
  private boolean open;

  InetAddress localAddress;
  private int         localPort;
  private InetAddress remoteAddress;
  private int         remotePort;
  private boolean     ipv6;
  
  public PlainSocketImpl() { }

  public synchronized Object getOption(int opt) throws SocketException {
    if (opt == SO_TIMEOUT){
      return new Integer(timeout);
    }
    else if(opt == SO_KEEPALIVE) {
      return new Boolean(getKeepAlive());
    }
    else if(opt == TCP_NODELAY) {
      return new Boolean(getNoDelay());
    }
    return PlainDatagramSocketImpl.Options(opt, null, getSocket());
  }

  public synchronized void setOption(int opt, Object value) throws SocketException {
    if(value == null){
      throw new SocketException("a non 'null' option value is required");
    }
    if(opt == SO_TIMEOUT){
      if(value instanceof Integer){
        Integer iv = (Integer)value;
        timeout = iv.intValue();
        setSoTimeout(iv);
      }
    }
    else if(opt == SO_KEEPALIVE){
       if(value instanceof Boolean){
         setKeepAlive((Boolean)value);
       }
    }
    else if(opt == TCP_NODELAY) {
       if(value instanceof Boolean){
         setNoDelay((Boolean)value);
       }
    }
    else {
      PlainDatagramSocketImpl.Options(opt, value, getSocket());
    }
  }

  protected void connect(String host, int port) throws IOException{
    connect(InetAddress.getByName(host),port);
  }

  protected synchronized InputStream getInputStream() throws IOException {
    if (!open) {
       throw new IOException("SocketImpl has been closed");
    }
     if (in == null) {
         in = new SocketInputStream(this);
     }
     return in;
  }

  protected synchronized OutputStream getOutputStream() throws IOException {
    if (!open) {
       throw new IOException("SocketImpl has been closed");
    }
    if (out == null) {
        out = new SocketOutputStream(this);  
    }
    return out;
  }

  protected synchronized void create(boolean stream) throws IOException {
     if (stream == false) {
        throw new IOException("datagram services not supported");
     }
     fd = new FileDescriptor();
  }

  protected synchronized void accept(SocketImpl s) throws IOException {
     int ip = nativeAccept(s);
    s.address = InetAddress.createInetAddress(ip);
  }

  protected synchronized void shutdownInput() throws IOException {
    shutdown(true);
  }

  protected synchronized void shutdownOutput() throws IOException {
    shutdown(false);
  }

  protected synchronized void connect(InetAddress address, int port) throws IOException {
    remoteAddress = address;
    remotePort = port;
    if(remoteAddress instanceof Inet6Address) ipv6 = true;
    nativeCreate();
    nativeBind();
    nativeConnect();
  }
  
  protected synchronized void connect(SocketAddress sa, int timeout) throws IOException {
    try {
      InetSocketAddress isa = (InetSocketAddress)sa;
      remoteAddress = isa.addr;
      remotePort = isa.port;
      if(remoteAddress instanceof Inet6Address) ipv6 = true;
      this.timeout = timeout;
      nativeCreate();
      nativeBind();
      nativeConnect();
    }
    catch (ClassCastException cce) {
      throw new SocketException();
    }
  }
  
  protected synchronized void bind(InetAddress host, int port) throws IOException {
    localAddress = host;
    localPort = port;
    if(localAddress instanceof Inet6Address) ipv6 = true;
  }
  
  protected synchronized void listen(int backlog) throws IOException {
    nativeCreate();
    nativeBind();
    nativeListen(backlog);
  }
  
  //package private methods for communication with SocketStreams ...

  native int read(byte [] bytes, int off, int length) throws IOException;
  native void write(byte [] bytes, int off, int length) throws IOException;

  protected native void finalize();

  //private native methods

  private native int getSocket() throws SocketException;
  private native void nativeCreate();
  private native int nativeAccept(SocketImpl s) throws IOException;
  private native void shutdown(boolean in) throws IOException;
  private native boolean getKeepAlive() throws SocketException;
  private native void setKeepAlive(Boolean b) throws SocketException;
  private native boolean getNoDelay() throws SocketException;
  private native void setNoDelay(Boolean b) throws SocketException;
  private native void setSoTimeout(Integer t) throws SocketException;
  protected synchronized native void nativeConnect() throws IOException;
  protected synchronized native void nativeBind() throws IOException;
  protected synchronized native void nativeListen(int backlog) throws IOException;

  //straight calls to native code ...

  protected synchronized native int available() throws IOException;
  protected synchronized native void close() throws IOException;

  /*
  TCP_NODELAY = 0x0001;    --> Boolean
  SO_LINGER = 0x0080;      --> Boolean
  SO_BINDADDR = 0x000F;    --> InetAddress
  SO_REUSEADDR = 0x04;     --> Integer
  IP_MULTICAST_IF = 0x10;  --> InetAddress
  SO_TIMEOUT = 0x1006;     --> Integer
  SO_SNDBUF =0x1001;       --> Integer
  SO_RCVBUF = 0x1002;      --> Integer
*/



}
