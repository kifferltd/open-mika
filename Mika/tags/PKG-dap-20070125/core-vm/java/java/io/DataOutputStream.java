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
