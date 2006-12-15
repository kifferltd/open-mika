/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: SocketImpl.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $ 
*/

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
