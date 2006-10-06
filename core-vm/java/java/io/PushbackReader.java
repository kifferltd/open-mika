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
** $Id: PushbackReader.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;

public class PushbackReader extends FilterReader {

  private int pos;
  private char[] chars;

  public PushbackReader(Reader in){
    super(in);
    chars = new char[1];
    pos = 1;
  }

  public PushbackReader(Reader in, int size){
    super(in);
    if(size < 1){
      throw new IllegalArgumentException("buffer size should 1 or greater");
    }
    chars = new char[size];
    pos = size;
  }

  public void close() throws IOException {
    synchronized(lock){
       if(chars != null){
         in.close();
       }
    }
  }

  public void mark(int ignored) throws IOException {
    throw new IOException("mark/reset not supported");
  }

  public boolean markSupported() {
    return false;
  }

  public int read()throws IOException {
    synchronized(lock){
      if(chars == null){
        throw new IOException("Reader is closed");
      }
      if(pos < chars.length){
        return chars[pos++];
      }
      return in.read();
    }
  }

  public int read(char[] buf, int off, int len)throws IOException {
    synchronized(lock){
      if(chars == null){
        throw new IOException("Reader is closed");
      }
      if(buf.length - len < off || off < 0 || len < 0){
        throw new ArrayIndexOutOfBoundsException();
      }
      int rd = chars.length - pos;
      if(rd > 0){
        if (len <= rd){
          System.arraycopy(chars, pos, buf, off, rd);
          pos += len;
          return len;
        }
        System.arraycopy(chars, pos, buf, off, rd);
        off += rd;
        pos += rd;
        len -= rd;
      }
      len = in.read(buf, off, len);
      return (len == -1 ? (rd == 0 ? len : rd) : len + rd);
    }
  }

  public boolean ready() throws IOException{
    synchronized(lock){
      if(chars == null){
        throw new IOException("Reader is closed");
      }
      return (pos < chars.length || in.ready());
    }
  }

  public void reset() throws IOException {
    throw new IOException("mark/reset not supported");
  }

  public void unread(int ch) throws IOException {
    synchronized(lock){
      if(chars == null || pos == 0){
        throw new IOException();
      }
      chars[--pos] = (char)ch;
    }
  }

  public void unread(char[] buf) throws IOException {
     unread(buf, 0, buf.length);
  }

  public void unread(char[] buf, int off, int len) throws IOException {
    synchronized(lock){
      if(buf.length - len < off || off < 0 || len < 0){
        throw new ArrayIndexOutOfBoundsException();
      }
      if(chars == null) {
        throw new IOException("char array is null");
      }
      if((pos - len) < 0) {
        throw new IOException("PushbackBuffer is full");
      }
      pos -= len;
      System.arraycopy(buf, off, chars, pos, len);
    }
  }
}
