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
** $Id: PushbackInputStream.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;

public class PushbackInputStream extends FilterInputStream {

  protected byte[] buf;
  protected int pos;
  
  public PushbackInputStream(InputStream in) {
    this(in, 1);
  }

  public PushbackInputStream(InputStream in, int size) {
    super(in);
    pos = size;
    buf = new byte[size];
  }

  public void close() throws IOException {
    if(buf != null){
      in.close();
      buf = null;
    }
  }

  public int read() throws IOException {
    if(buf == null){
      throw new IOException("STREAM IS CLOSED");
    }

    if(pos < buf.length) {
      return (0xff & (char)buf[pos++]);
    } else {
      return in.read();
    }
  }
  
  public int read(byte[] b, int off, int len)
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException {
      
    if(buf == null){
      throw new IOException("STREAM IS CLOSED");
    }

    if(off < 0 || len < 0 || off > b.length - len) throw new ArrayIndexOutOfBoundsException();

    if (len==0) return 0;
     
    int bufsize = buf.length - pos;
    int result = 0;

    if(bufsize > len) {
      System.arraycopy(buf, pos, b, off, len);
      pos += len;
      result = len;
    }
    else {
      System.arraycopy(buf, pos, b, off, bufsize);
      pos = buf.length;
      result = bufsize;
      try {
        int rd = in.read(b, off + bufsize, len - bufsize);
        if(result == 0 || rd != -1){
          result += rd;
        }
      } 
      catch(IOException e) {}
    }
    return result;
  }      
  
  public void unread(int b) throws IOException {
    if(buf == null){
      throw new IOException("STREAM IS CLOSED");
    }
    if (pos == 0) throw new IOException("buffer full");
    buf[--pos] = (byte)b;
  }

  public void unread(byte[] b) throws IOException {
    unread(b, 0, b.length);
  }
  
  public void unread(byte[] b, int off, int len) throws IOException {
    if(buf == null){
      throw new IOException("STREAM IS CLOSED");
    }

    if(b.length - len < off || off < 0 || len < 0){
      throw new ArrayIndexOutOfBoundsException();
    }
    if(pos < len) {
      throw new IOException("buffer full");
    }

    for(int i = len - 1; i >= 0; i--) buf[--pos] = b[off + i];
  }

  public int available() throws IOException {
    if(buf == null){
      throw new IOException("STREAM IS CLOSED");
    }

    return in.available() + (buf.length - pos);
  }

  public long skip(long n) throws IOException {
    if(buf == null){
      throw new IOException("STREAM IS CLOSED");
    }
    long bufsize = buf.length - pos;
    if(bufsize > n) {
      pos += n;
      return n;
    }
    else {
      pos = buf.length;
      return bufsize + in.skip(n - bufsize);
    }
  }
  
  public boolean markSupported() {
    return false;
  }

}
