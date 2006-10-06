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
** $Id: BufferedInputStream.java,v 1.3 2006/10/04 14:24:14 cvsroot Exp $
*/

package java.io;

public class BufferedInputStream extends FilterInputStream {

  protected byte [] buf;
  protected int count = 0;
  protected int pos = 0;
  protected int marklimit = 0;
  protected int markpos = -1;

  public BufferedInputStream(InputStream in){
    super(in);
    buf = new byte[2048];
  }

  public BufferedInputStream(InputStream in, int size) {
    super(in);
    if(size <= 0){
      throw new IllegalArgumentException("size should be > 0");
    }
    buf = new byte[size];
  }

  public int read() throws IOException {
    if(buf == null) {
      throw new IOException("Stream is closed");
    }
    if(count <= pos){
      if(!updateBuffer(1)){
        return -1;
     }
    }
    pos++;
    if(buf == null) {
      throw new IOException("Stream is closed");
    }
    return 0xff & ((char)buf[pos-1]);
  }

  public int read(byte[] b, int off, int len) throws IOException {
    if(buf == null){
      throw new IOException("Stream is closed");
    }
    if(off < 0 || len < 0 || off > b.length - len){
      throw new IndexOutOfBoundsException("offset = " + off + ", length = " + len + ", length of " + b + " is " + b.length);
    }
    if(len == 0){
      return 0;
    }

    checkSize(len);

    int rd = 0;
    //first take the bytes out of buf
    if(count > pos){
      int cp = count - pos;
      if(len > cp){
        System.arraycopy(buf, pos, b, off, cp);
        off += cp;
        len -= cp;
        pos += cp;
        rd +=cp;
      }else{
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
      }
    }
    //we need more bytes ...
    if(len > buf.length && (markpos == -1 || pos+len > (markpos+marklimit))){
      int rd2 = in.read(b, off, len);
      if (rd2 >= 0) {
        rd += rd2;
      }
      markpos = -1;
    }else if (updateBuffer(len)){
      len = len > count - pos ? count - pos : len;

      if(buf == null){
        throw new IOException("Stream is closed");
      }

      System.arraycopy(buf, pos, b, off, len);
      pos += len;
      rd += len;
    }
    return rd == 0 ? -1 : rd;
  }

  public long skip(long n) throws IOException {
    if(buf == null){
      throw new IOException("Stream is closed");
    }
    if(n <= 0){
      return 0;
    }

    checkSize(n);

    if((n - count + pos) > 0){
      //not enough bytes in the buffer
      if(count == pos){
        //everything is read/skipped ...
        if(n > buf.length){
          markpos = -1;
          return in.skip(n);
        }
        if(updateBuffer((int)n)){
          if((n - count + pos) > 0){
            int ret = count - pos;
            pos = count;
            return ret;
          }
        }
        else {
          return 0;
        }
      }
      else {
        int ret = count - pos;
        pos = count;
        return ret;
      }
    }
    //take bytes from the buffer
    pos += (int)n;
    return n;
  }

  public boolean markSupported() {
     return true;
  }

  public void mark(int markLimit){
     this.marklimit = markLimit;
     markpos = pos;
  }

  public void reset() throws IOException {
    if(buf == null || markpos == -1 || pos > markpos+marklimit){
      throw new IOException();
    }
    pos = markpos;
  }

  public void close() throws IOException {
    if(buf != null){
      markpos = -1;
      marklimit = 0;
      in.close();
      buf = null;
      in = null;
    }
  }

  public int available() throws IOException {
    if(buf == null){
      throw new IOException("Stream is closed");
    }
    return (count - pos) + in.available();
  }

  private void checkSize(long need) throws IOException {
    if(markpos == 0 && marklimit > buf.length && pos+need > buf.length){
      byte[] bytes = new byte[marklimit];
      System.arraycopy(buf,0, bytes, 0, count);
      int cp = in.read(bytes,count, marklimit - count);
      buf = bytes;
      if(cp > 0){
        count += cp;
      }
    }
  }

  /**
  ** this method will will read bytes from in and place them in the buffer
  ** it will update count and pos and should take care of mark/reset stuff
  */
  private boolean updateBuffer(int want) throws IOException {
    if(markpos == -1 || (pos + want)> (markpos + marklimit)){
      if(markpos != -1 && marklimit > buf.length){
        buf = new byte[marklimit];
      }
      count = in.read(buf, 0, buf.length);
      pos = 0;
      markpos = -1;
      if(count == -1){
        count = 0;
        return false;
      }
      return true;
    }else{
      //a position is marked and still valid
      byte[] bytes = buf;
      int cp = count - markpos;

      if (marklimit > buf.length && cp >= buf.length){
         //let the buffer grow
         bytes = new byte[marklimit];
      }

      //int cp = count - markpos;
      System.arraycopy(buf, markpos, bytes, 0 ,cp);
      count = in.read(bytes, cp, bytes.length - cp);
      pos = cp;
      markpos = 0;
      buf = bytes;
      if(count == -1){
        count = cp;
        return false;
      }
      count += cp;
      return true;
    }
  }
}
