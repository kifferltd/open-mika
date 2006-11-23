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
** $Id: BufferedReader.java,v 1.2 2006/10/04 14:24:14 cvsroot Exp $
*/

package java.io;

public class BufferedReader extends Reader {

  private Reader in;

  /*
  ** these 5 fields have the same meaning as in the BufferedInputStream...
  */

  private char buf[];
  private int pos;
  private int count;
  private int markpos;
  private int marklimit;

  public BufferedReader(Reader in) {
    this(in, 8192);
  }
  public BufferedReader(Reader in, int size) {
    super(in);
    if (size <= 0){
     throw new IllegalArgumentException("negative buffer size");
    }
    this.in = in;
    buf = new char[size];
    markpos = -1;
  }

  public int read() throws IOException {
    synchronized (lock) {
      if(buf == null){
        throw new IOException("Reader is closed");
      }
      if(count <= pos && !updateBuffer()){
        return -1;
      }
      return buf[pos++];
    }
  }

  public int read(char b[], int off, int len) throws IOException {
    synchronized (lock) {
      if(buf == null){
        throw new IOException("Reader is closed");
      }
      if(off < 0 || len < 0 || off > b.length - len){
        throw new IndexOutOfBoundsException();
      }
      int rd = 0;
      while(len > 0) {
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
            break;
          }
        }
        //we need more bytes ...
        if(len > buf.length && (markpos == -1 || pos > (markpos+marklimit))){
          len = in.read(b, off, len);
          if(len == -1 && rd > 0){
            len = 0;
          }
          break;
        }else if (!updateBuffer()){
          len = (rd == 0 ? -1 : 0);
          break;
        }
      }
      return rd + len;
    }
  }

  private native int locateEnd(char[] chars, int startPos, int endPos);

  public String readLine() throws IOException {
    synchronized (lock) {
      if(buf == null){
        throw new IOException("Reader is closed");
      }
      StringBuffer line = new StringBuffer(64);
      int end;
      do {
        end = locateEnd(buf,pos,count);
        if(end != -1){
          line.append(buf,pos, end - pos);
          pos = end;
          if(buf[pos++] == '\r'){
            if ((count > pos || updateBuffer()) &&  buf[pos] == '\n'){
              pos++;
            }
          }
          return line.toString();
        }
        else {
          line.append(buf,pos, count - pos);
          pos = count;
        }
      } while(updateBuffer());
      if (line.length() == 0 && end == -1){
        return null;
      }
      return line.toString();
    }
  }

  public long skip(long n) throws IOException {
    synchronized (lock) {
      if(buf == null){
        throw new IOException("Reader is closed");
      }
      if(n < 0){
        throw new IllegalArgumentException();
      }
      long skipped = 0;
      while(n > 0){
        if((n - count + pos) > 0){
          //not enough bytes in the buffer
          int skip = count - pos;
          pos = count;
          if((n - skip) > buf.length && (markpos == -1 || pos > (markpos+marklimit))){
            return in.skip(n-skip)+skip;
          }else if(!updateBuffer()){
            break;
          }
        }else{
          //take bytes from the buffer
          pos += (int)n;
          skipped += n;
          break;
        }
      }
      return skipped;
    }
  }

  public boolean ready() throws IOException {
    synchronized (lock) {
      if(buf == null){
        throw new IOException("Reader is closed");
      }
      return (count > pos || in.ready());
    }
  }

  public boolean markSupported() {
    return true;
  }

  public void mark(int readLimit) throws IOException {
    synchronized (lock) {
      if(buf == null){
        throw new IOException("Reader is closed");
      }
      if(readLimit < 0){
        throw new IllegalArgumentException();
      }
      marklimit = readLimit;
      markpos = pos;
    }
  }

  public void reset() throws IOException {
    synchronized (lock) {
      if(buf == null || markpos == -1 || (markpos+marklimit < pos)){
        throw new IOException();
      }
      pos = markpos;
    }
  }

  public void close() throws IOException {
    synchronized (lock) {
      if(buf != null){
        buf = null;
        in.close();
        in = null;
      }
    }
  }

  private boolean updateBuffer() throws IOException{
    if(markpos == -1 || pos >= (markpos + marklimit)){
      int rd = in.read(buf, 0, buf.length);
      pos = 0;
      markpos = -1;
      if (rd == -1){
        count = 0;
        return false;
      }
      else if (rd < 1) {
        new Exception("OOPS - read on "+in+" returned "+rd).printStackTrace();
      }
      count = rd;
      return true;
    }else{
      //a position is marked and still valid
      char[] chars = buf;
      if (marklimit > buf.length){
         //let the buffer grow
         chars = new char[marklimit];
      }
      int cp = count - markpos;
      System.arraycopy(buf, markpos, chars, 0 ,cp);
      int rd = in.read(chars, cp, chars.length - cp);
      pos = cp;
      markpos = 0;
      buf = chars;
      if (rd == -1){
        count = cp;
        return false;
      }
      else if (rd < 1) {
        new Exception("OOPS - read on "+in+" returned "+rd).printStackTrace();
      }
      count = cp + rd;
      return true;
    }
  }
}
