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
** $Id: ByteArrayInputStream.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

public class ByteArrayInputStream extends InputStream {

  protected byte[] buf;
  protected int pos;
  protected int count;
  protected int mark ;

  public ByteArrayInputStream(byte[] b) {
    this(b, 0, b.length);
  }

  public ByteArrayInputStream(byte[] b, int off, int len) {
    if (off < 0 || len < 0 || off > b.length){
      throw new ArrayIndexOutOfBoundsException();
    }
    buf   = b;
    count = (off+len > b.length) ? b.length - off : off+len;
    pos   = off;
    mark  = off;
  }
 
  public synchronized int read(){
    if(pos >= count){
      return -1;
    }else {
      return (0xff & ((char) buf[pos++]));
    }
  }

  public void close() throws IOException {}

  public synchronized int read(byte[] b, int off, int len){
    if(pos >= count){
      return -1;
    }
    int k = count-pos;
    k = (len > k ? k : len);
    System.arraycopy(buf, pos, b, off, k);
    pos += k;
    return k;
  }

  public synchronized long skip(long n){
    if (n <= 0){
      return 0;
    }
    int k = count-pos;
    k = (n > k) ? k :(int) n;
    pos += k;
    return k;
  }

  public synchronized int available(){
    return count-pos;
  }

   public synchronized void mark(int readAheadLimit){
     mark = pos;
    }

  public synchronized void reset(){
    pos = mark;
  }

  public boolean markSupported() {
    return true;
  }
}
