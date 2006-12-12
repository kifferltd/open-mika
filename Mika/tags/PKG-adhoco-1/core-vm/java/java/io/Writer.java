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

package java.io;

public abstract class Writer {

  protected Object lock;


  protected Writer(){
    lock = this;
  }

  protected Writer(Object lock){
    if (lock == null){
      throw new NullPointerException("lock should be non null");
    }
    this.lock = lock;
  }

  public abstract void close() throws IOException;
  public abstract void flush() throws IOException;

  /**
  ** Note: an implementation of this method should use the 'lock' object
  ** to synchronize with other methods ...
  */
  public abstract void write(char[] cbuf, int off, int len) throws IOException;

  public void write(char[] buf) throws IOException {
     write(buf, 0, buf.length);
  }

  public void write(int c) throws IOException {
     char[] buf = new char[1];
     buf[0] = (char) c;
     write(buf,0,1);
  }

  public void write(String string) throws IOException {
    write(string, 0, string.length());
  }

  public void write(String string, int off, int len) throws IOException {
     if (len < 0) {
       throw new IndexOutOfBoundsException();
     }
     char[] buf = new char[len];
     string.getChars(off,off+len, buf, 0);
     write(buf, 0, len);
  }

}
