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
