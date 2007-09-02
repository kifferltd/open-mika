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
