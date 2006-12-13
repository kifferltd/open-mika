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
** $Id: StringReader.java,v 1.3 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;

public class StringReader extends Reader {

  private String string;
  private int len;
  private int pos;
  private int markpos;

  public StringReader(String str){
    super();
    len = str.length();
    string = str;
  }

  public void close(){
    synchronized(lock){
      string = null;
    }
  }

  public void mark(int ignored) throws IOException {
    synchronized(lock){
      if(string == null){
        throw new IOException("Reader is closed");
      }
      pos = markpos;
    }
  }

  public boolean markSupported(){
    return true;
  }

  public int read() throws IOException {
    synchronized(lock){
      if(string == null){
        throw new IOException("Reader is closed");
      }
      if(pos >= len){
        return -1;
      }
      return string.charAt(pos++);
    }
  }

  public int read(char[] chars, int off, int len) throws IOException {
    synchronized(lock){
      if(string == null){
        throw new IOException("Reader is closed");
      }
      if(len < 0 || off < 0 || chars.length  - len < off){
        throw new ArrayIndexOutOfBoundsException();
      }
      int rd = this.len - pos;
      if (rd <= 0){
        return -1;
      }
      rd = (rd > len ? len : rd);
      string.getChars(pos, pos + rd, chars, off);
      pos += rd;
      return rd;
    }
  }

  public boolean ready() throws IOException {
    synchronized(lock){
      return (string != null);
    }
  }

  public void reset() throws IOException {
    synchronized(lock){
      if(string == null){
        throw new IOException("Reader is closed");
      }
      markpos = pos;
    }
  }

  public long skip(long skip) throws IOException {
    synchronized(lock){
      if(string == null){
        throw new IOException("Reader is closed");
      }
      if(skip <= 0){
         return 0;
      }
      int rd = this.len -pos;
      rd = (skip > rd ? rd : (int)skip);
      pos += rd;
      return rd;
    }
  }
}
