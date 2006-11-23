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
