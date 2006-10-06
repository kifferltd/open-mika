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
** $Id: PipedOutputStream.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

public class PipedOutputStream extends OutputStream {

  /** The PipedInputStream to which we are connected.
  */

  PipedInputStream dst;

  public PipedOutputStream(PipedInputStream dst) throws IOException {
    this.connect(dst);
  }

  public PipedOutputStream() {
  }

  public void connect(PipedInputStream dst) throws IOException {

    if (this.dst == null && dst.src == null) {
      this.dst = dst;
      dst.src = this;
    }
    else {
      throw new IOException("allready connected");
    }
                  
  }

  public synchronized void flush() throws IOException {

    PipedInputStream dst = this.dst;

    if (dst != null) {
      synchronized (dst) {
        dst.readable = dst.buffer.length - dst.storable;
        dst.notifyAll();
      }
    }

  }

  public void close() throws IOException {
    if(dst != null && !dst.producerclosed){
      this.flush();
      dst.producerclosed = true;
    }
  }

  /*
  ** Note that in the following 2 methods, we don't do any checks. The appropriate exceptions
  ** will be thrown by called methods, so we don't have to check this. E.g. NullPointerException
  ** will be thrown by the interpreter when 'dst' would be null or the pipe is not yet connected;
  ** ArrayIndexOutOfBoundsException will be thrown by the arraycopy in dst.receive.
  */
  
  public void write(byte[] buffer, int offset, int count) throws IOException {
    if(dst == null){
      throw new IOException("unconnected");
    }
    if(count < 0){
      throw new ArrayIndexOutOfBoundsException();
    }

    this.dst.receive(buffer, offset, count);
  }

  public void write(int oneByte) throws IOException {
    if(dst == null){
      throw new IOException("unconnected");
    }
    this.dst.receive(oneByte);
  }

}
