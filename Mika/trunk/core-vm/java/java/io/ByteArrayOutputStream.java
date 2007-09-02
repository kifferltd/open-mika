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
** $Id: ByteArrayOutputStream.java,v 1.2 2006/10/04 14:24:14 cvsroot Exp $
*/

package java.io;

public class ByteArrayOutputStream extends OutputStream {

  protected byte[] buf;
  protected int count;

  public ByteArrayOutputStream(){
    this(32);
  }

  public ByteArrayOutputStream(int size) {
    if (size < 0){
      throw new IllegalArgumentException("negative size "+size);
    }
    buf = new byte[size];
  }
 
  public synchronized void write(int b) {
    if(buf.length <= count){
      byte[] bytes = new byte[(count+1)*2];
      System.arraycopy(buf,0,bytes,0,count);
      buf = bytes;
    }
    buf[count++] = (byte)b;
  }

  public synchronized void write(byte[] b, int off, int len) {
    if(off < 0 || len < 0 || off > b.length - len){
      throw new IndexOutOfBoundsException();
    }
    if(count+len > buf.length){
      int c = (count+1)*2;
      byte[] bytes = new byte[c > count+len ? c : count+len];
      System.arraycopy(buf,0,bytes,0,count);
      buf = bytes;
    }
    System.arraycopy(b,off,buf,count,len);
    count += len;
  }

  public int size() {
    return count;
  }

  public synchronized void reset() {
    count = 0;
  }

  public synchronized byte[] toByteArray() {
    byte[] result = new byte[count];
    System.arraycopy(buf,0,result,0,count);
    return result;
  }

  public String toString() {
    // uses default encoding ...
    return new String(buf, 0, count);
  }

/**
** @deprecated
*/
  public String toString(int hibyte) {
    return new String(buf, hibyte, 0, count);
  }

  public String toString(String enc) throws UnsupportedEncodingException {
    return new String(buf, 0, count, enc);
  }

  public synchronized void writeTo(OutputStream out) throws IOException {
    out.write(buf, 0, count);
  }

  public void close() throws IOException {}
	
}
