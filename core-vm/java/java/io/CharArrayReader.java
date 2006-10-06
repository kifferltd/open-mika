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
** $Id: CharArrayReader.java,v 1.2 2006/10/04 14:24:14 cvsroot Exp $
*/

package java.io;

public class CharArrayReader extends Reader {

  protected char[] buf;
  protected int count;
  protected int markedPos;
  protected int pos;

  public CharArrayReader(char[] chars) {
    this(chars, 0, chars.length);
  }

  public CharArrayReader(char[] chars, int off, int len) {
    super();
    if(off < 0 || len < 0 || off > chars.length){
      throw new IllegalArgumentException();
    }	
    buf = chars;
    count = chars.length <(len + off) ? chars.length: len+off;
    pos = off;
    markedPos = off;
  }

  public int read() throws IOException {
    synchronized(lock){
      if(buf == null){
    	  throw new IOException("cannot read from a closed Stream");
  	  }
      if(pos >= count){
    	  return -1;
  	  }
      return buf[pos++];
    }
  }

  public int read(char[] chars, int off, int len) throws IOException {
    if(off < 0 || len < 0 || off > chars.length - len){
      throw new ArrayIndexOutOfBoundsException();
    }	
    synchronized(lock){
      if(buf == null){
  	    throw new IOException("cannot read from a closed Stream");
      }
      if(pos >= count){
  	    return -1;
      }	
      int rd = count-pos;
      rd = rd > len ? len : rd;
      System.arraycopy(buf, pos, chars, off, rd);
      pos += rd;
      return rd;
    }
  }

  public long skip(long n) throws IOException {
    if(n <= 0){
      return 0;
    }	
    synchronized(lock){
      if(buf == null){
  	    throw new IOException("cannot read from a closed Stream");
      }	
      if(pos >= count){
  	    return 0;
      }	
      int skip = count-pos;
      skip = skip > n ? (int)n : skip;
      pos += skip; 	
      return skip;
    }
  }

  public boolean ready() throws IOException {
    synchronized(lock){
      if(buf == null){
  	    throw new IOException("cannot read from a closed Stream");
      }
      return pos < count;
    }
  }

  public boolean markSupported() {
    return true;
  }

  public void mark(int readAheadLimit) throws IOException {
    synchronized(lock){
      if(buf == null){
  	    throw new IOException("cannot read from a closed Stream");
      }
      markedPos = pos;
    }
  }

  public void reset() throws IOException {
    synchronized(lock) {
      if(buf == null){
  	    throw new IOException("cannot read from a closed Stream");
      }	
      pos = markedPos;
    }
  }

  public void close(){
    synchronized(lock){
      buf = null;
    }
  }
}
