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
** $Id: BufferedOutputStream.java,v 1.2 2006/10/04 14:24:14 cvsroot Exp $
*/

package java.io;

public class BufferedOutputStream extends FilterOutputStream {

  protected byte[] buf;
  protected int count;

  public BufferedOutputStream(OutputStream out) {
    this(out, 512);
  }

  public BufferedOutputStream(OutputStream out, int size) {
    super(out);
    if (size < 1 ) throw new IllegalArgumentException("size should be bigger than 0");
    buf = new byte[size];
    count = 0;
  }

  public void write(int b) throws IOException, NullPointerException {
    if (count == buf.length) {
     	out.write(buf,0,count);
    	count=0;
    }
    buf[count++] = (byte)b;

  }

  public void write(byte[] b, int off, int len) throws IOException, NullPointerException {
    if (off > b.length - len || off < 0 || len < 0) {
      throw new IndexOutOfBoundsException("offset = " + off + ", length = " + len + ", length of " + b + " is " + b.length);
    }
    if(len > 0){
      if (len+count > buf.length) {
      	out.write(buf,0,count);
      	count=0;
      }
      if(len >= buf.length){
        out.write(b,off,len);
      }
      else {
      	System.arraycopy(b, off, buf, count, len);
      	count += len;
      }
    }
  }

  public void flush() throws IOException {   
    if(count > 0){
      out.write(buf, 0, count);
      count = 0;
    }
    out.flush();
  }

}

