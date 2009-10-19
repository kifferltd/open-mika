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
** $Id: DataOutputStream.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

import wonka.vm.ArrayUtil;

public class DataOutputStream extends FilterOutputStream implements DataOutput {
  
  protected int written;
  private byte[] buffer = new byte[8];

  public DataOutputStream(OutputStream out){
    super(out);
  }

  public synchronized void write(int b) throws IOException {
    out.write(b);
    written += 1;
    if(written < 0){
      written = Integer.MAX_VALUE;
    }
  }

  public synchronized void write(byte b[], int off, int len) throws IOException {
    out.write(b, off, len);
    written += len;
    if(written < 0){
      written = Integer.MAX_VALUE;
    }
  }

  public void flush() throws IOException {
    out.flush();
  }

  public final void writeBoolean(boolean bool) throws IOException {
    out.write(bool ? 1:0);
    written += 1;
    if(written < 0){
      written = Integer.MAX_VALUE;
    }
 }

  public final void writeByte(int v) throws IOException {
    out.write(v);
    written += 1;
    if(written < 0){
      written = Integer.MAX_VALUE;
    }
  }

  public final void writeShort(int sh) throws IOException {
    out.write((sh>>8) & 0xFF);
    out.write(sh & 0xFF);
    written += 2;
    if(written < 0){
      written = Integer.MAX_VALUE;
    }
  }

  public final void writeChar(int ch) throws IOException {
    out.write((ch>>8));
    out.write(ch);

    written += 2;
    if(written < 0){
      written = Integer.MAX_VALUE;
    }
  }

  public final void writeInt(int i) throws IOException {
    ArrayUtil.iInBArray(i, buffer,0);
    out.write(this.buffer, 0, 4);
    written += 4;
    if(written < 0){
      written = Integer.MAX_VALUE;
    }
  }

  public final void writeLong(long l) throws IOException {
    ArrayUtil.lInBArray(l, buffer, 0);
    out.write(this.buffer, 0, 8);
    written += 8;
    if(written < 0){
      written = Integer.MAX_VALUE;
    }
  }

  public final void writeFloat(float f) throws IOException {
    ArrayUtil.fInBArray(f, buffer,0);
    out.write(this.buffer, 0, 4);
    written += 4;
    if(written < 0){
      written = Integer.MAX_VALUE;
    }
  }

  public final void writeDouble(double f) throws IOException {
    ArrayUtil.dInBArray(f, buffer, 0);
    out.write(this.buffer, 0, 8);
    written += 8;
    if(written < 0){
      written = Integer.MAX_VALUE;
    }
  }

  public final void writeBytes(String s) throws IOException {
    byte[] b = s.getBytes("latin1");
    int len = b.length;
    out.write(b,0,len);
    written += len;
    if(written < 0){
      written = Integer.MAX_VALUE;
    }
  }

  public final void writeChars(String s) throws IOException {
    char[] ca = s.toCharArray();	
    int len = ca.length;
    for(int i=0; i < len; ++i) {
      out.write(ca[i]>> 8);
      out.write(ca[i]);	
    }
    written += (2 * len);
    if(written < 0){
      written = Integer.MAX_VALUE;
    }
  }

  public final void writeUTF(String s) throws IOException {
    byte []b = s.getBytes("UTF8");
    writeShort(b.length);
    write(b, 0, b.length);
  }

  public final int size() {
    return written;
  }
}
