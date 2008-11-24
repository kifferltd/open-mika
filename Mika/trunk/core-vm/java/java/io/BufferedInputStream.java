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
** $Id: BufferedInputStream.java,v 1.3 2006/10/04 14:24:14 cvsroot Exp $
*/

package java.io;

public class BufferedInputStream extends FilterInputStream {

  protected byte [] buf;
  protected int count = 0;
  protected int pos = 0;
  protected int marklimit = 0;
  protected int markpos = -1;

  private int bufsize;

  public BufferedInputStream(InputStream in){
    super(in);
    bufsize = 2048;
    buf = new byte[2048];
  }

  public BufferedInputStream(InputStream in, int size) {
    super(in);
    if(size <= 0){
      throw new IllegalArgumentException("size should be > 0");
    }
    bufsize = size;
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
      byte[] newbuf = new byte[marklimit];
      System.arraycopy(buf,0, newbuf, 0, count);
      int cp = in.read(newbuf,count, marklimit - count);
      buf = newbuf;
      if(cp > 0){
        count += cp;
      }
      else if (cp < 0) {
        newbuf = new byte[count];
        System.arraycopy(buf,0, newbuf, 0, count);
        buf = newbuf;
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
        buf = new byte[bufsize];
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
        byte[] newbuf = new byte[count];
        System.arraycopy(buf,0, newbuf, 0, count);
        buf = newbuf;
        return false;
      }
      count += cp;
      return true;
    }
  }
}
