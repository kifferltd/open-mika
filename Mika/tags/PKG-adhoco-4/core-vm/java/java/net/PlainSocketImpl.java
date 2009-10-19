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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Package-local implementation of SocketImpl for "plain vanilla" sockets.
 */
class PlainSocketImpl extends SocketImpl {

  /**
   * Set of all sockets for which listen(), accept(), or connect() has been
   * called but not yet close(). We store these as weak references to avoid
   * accidentally keeping sockets from getting garbage-collected. During
   * VM shutdown we try to close() any sockets which are found in this set.
   */
  private static HashSet opensockets;

  /**
   * Number of references which have been added to <code<opensockets</code>
   * since the last purge of stqle references.
   */
  private static int regcount;

  static {
    opensockets = new HashSet();

    /*
     * We add a shutdown hook which attempts to close() every socket
     * found in <code>opensockets</code>.
     */
    Runtime.getRuntime().addShutdownHook(new Thread("CloseOpenSockets") {
      public void run() {
        Iterator iter = opensockets.iterator();
        ArrayList threadlist = new ArrayList();
        while(iter.hasNext()) {
          threadlist.add(iter.next());
        }
        iter = threadlist.iterator();
        while(iter.hasNext()) {
          WeakReference wr = (WeakReference)iter.next();
          PlainSocketImpl psi = (PlainSocketImpl)wr.get();
          if (psi != null) {
            try {
              psi.close();
            }
            catch (IOException ioe) {
            }
          }
        }
      }
    });
   }

  private InputStream in;
  private OutputStream out;
  private int timeout;
  private boolean oob;
  private boolean keepalive;
  private boolean nodelay;
  private int linger = -1;
  private boolean open;
  private WeakReference wr;

  InetAddress localAddress;
  private int         localPort;
  private InetAddress remoteAddress;
  private int         remotePort;
  private boolean     ipv6;
  
  public PlainSocketImpl() { }

  public synchronized Object getOption(int opt) throws SocketException {
    switch (opt) {
    case SO_TIMEOUT:
      return new Integer(timeout);

    case SO_RCVBUF:
      return new Integer(getRcvBuf());

    case SO_SNDBUF:
      return new Integer(getSndBuf());

    case SO_KEEPALIVE:
      return new Boolean(keepalive);

    case SO_LINGER:
      return new Integer(linger);

    case SO_OOBINLINE:
      return new Boolean(oob);

    case TCP_NODELAY:
      return new Boolean(nodelay);

    case SO_BINDADDR:
      return remoteAddress;

    case IP_TOS:
      return new Integer(getIpTos());

    default:
      return PlainDatagramSocketImpl.options(opt, null, getSocket());
    }
  }

  public synchronized void setOption(int opt, Object value) throws SocketException {
    if(value == null){
      throw new SocketException("a non 'null' option value is required");
    }

    switch (opt) {
    case SO_TIMEOUT:
      if(value instanceof Integer){
        Integer iv = (Integer)value;
        timeout = iv.intValue();
        setSoTimeout(timeout);
      }
      break;

    case SO_RCVBUF:
      if(value instanceof Integer){
        setRcvBuf(((Integer)value).intValue());
      }
      break;

    case SO_SNDBUF:
      if(value instanceof Integer){
        setSndBuf(((Integer)value).intValue());
      }
      break;

    case SO_KEEPALIVE:
      keepalive = value instanceof Boolean ? ((Boolean)value).booleanValue() : true;
      setKeepAlive(keepalive);
      break;

    case SO_LINGER:
       if(value instanceof Boolean && !((Boolean)value).booleanValue()) {
         linger = -1;
         setLinger(-1);
       }
       else if(value instanceof Integer){
         linger = ((Integer)value).intValue();
         setLinger(linger);
       }
      break;

    case SO_OOBINLINE:
      oob = value instanceof Boolean ? ((Boolean)value).booleanValue() : true;
      setOOBInline(oob);
      break;

    case TCP_NODELAY:
      nodelay = value instanceof Boolean ? ((Boolean)value).booleanValue() : true;
      setNoDelay(nodelay);
      break;

    case IP_TOS:
      if(value instanceof Integer){
        setIpTos(((Integer)value).intValue());
      }
      break;

    default:
      PlainDatagramSocketImpl.options(opt, value, getSocket());
    }
  }

  protected void connect(String host, int port) throws IOException {
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
    try {
      ((PlainSocketImpl)s).register();
    }
    catch (ClassCastException cce) {
    }
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
    if (remoteAddress instanceof Inet6Address) {
      ipv6 = true;
    }
    try {
      SocketUsers.put(this, Thread.currentThread());
      nativeCreate();
      nativeBind();
      nativeConnect();
      register();
    }
    finally {
      SocketUsers.remove(this);
    }
  }
  
  protected synchronized void connect(SocketAddress sa, int timeout) throws IOException {
    try {
      SocketUsers.put(this, Thread.currentThread());
      InetSocketAddress isa = (InetSocketAddress)sa;
      remoteAddress = isa.addr;
      remotePort = isa.port;
      if (remoteAddress instanceof Inet6Address) {
        ipv6 = true;
      }
      this.timeout = timeout;
      nativeCreate();
      nativeBind();
      nativeConnect();
    }
    catch (ClassCastException cce) {
      throw new SocketException();
    }
    finally {
      SocketUsers.remove(this);
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
    register();
  }
  
  protected void close() throws IOException {
    Thread t = SocketUsers.get(this);
    if (t != null) {
      signal(t);
    }
    _close();
  }

  private void register() {
    if (wr == null) {
      wr = new WeakReference(this);
      synchronized (opensockets) {
        opensockets.add(wr);
        if (++regcount >= 100) {
          // Clean up stale references
          regcount = 0;
          Iterator iter = opensockets.iterator();
          while (iter.hasNext()) {
            WeakReference wr0 = (WeakReference)iter.next();
            if (wr0.get() == null) {
              iter.remove();
            }
          }
        }
      }
    }
  }

  private void deregister() {
    if (wr != null) {
      synchronized (opensockets) {
        opensockets.remove(wr);
      }
    }
  }

  //package private methods for communication with SocketStreams ...

  int read(byte [] bytes, int off, int length) throws IOException {
    try {
      SocketUsers.put(this, Thread.currentThread());
      return _read(bytes, off, length);
    }
    finally {
      SocketUsers.remove(this);
    }
  }

  native int _read(byte [] bytes, int off, int length) throws IOException;
  native void write(byte [] bytes, int off, int length) throws IOException;

  protected native void finalize();

  //private native methods

  private native void signal(Thread t);
  private native int getSocket() throws SocketException;
  private native void nativeCreate();
  private native int nativeAccept(SocketImpl s) throws IOException;
  private native void shutdown(boolean in) throws IOException;
  private native void setKeepAlive(boolean on) throws SocketException;
  private native void setNoDelay(boolean on) throws SocketException;
  private native void setSoTimeout(int millis) throws SocketException;
  private native int getRcvBuf() throws SocketException;
  private native void setRcvBuf(int size) throws SocketException;
  private native int getSndBuf() throws SocketException;
  private native void setSndBuf(int size) throws SocketException;
  private native void setLinger(int secs) throws SocketException;
  private native void setOOBInline(boolean on) throws SocketException;
  private native int getIpTos() throws SocketException;
  private native void setIpTos(int tos) throws SocketException;
  protected synchronized native void nativeConnect() throws IOException;
  protected synchronized native void nativeBind() throws IOException;
  protected synchronized native void nativeListen(int backlog) throws IOException;

  //straight calls to native code ...

  protected native void sendUrgentData(int udata) throws IOException;
  protected synchronized native int available() throws IOException;
  protected native void _close() throws IOException;

  /*
  TCP_NODELAY = 0x0001;    --> Boolean
  IP_TOS = 0x0003;         --> Integer
  SO_LINGER = 0x0080;      --> Boolean
  SO_BINDADDR = 0x000F;    --> InetAddress
  SO_REUSEADDR = 0x04;     --> Integer
  SO_BROADCAST = 0x0020;   --> Boolean
  IP_MULTICAST_IF = 0x10;  --> InetAddress
  IP_MULTICAST_IF2 = 0x1F; --> InetAddress
  IP_MULTICAST_LOOP = 0x0012;> Boolean
  SO_TIMEOUT = 0x1006;     --> Integer
  SO_SNDBUF =0x1001;       --> Integer
  SO_RCVBUF = 0x1002;      --> Integer
  SO_OOBINLINE = 0x1003;   --> Boolean
  SO_KEEPALIVE = 0x0008;   --> Boolean
*/



}
