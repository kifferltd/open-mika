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

public abstract class Reader {

  protected Object lock;

  protected Reader(){
    this.lock = this;
  }

  protected Reader(Object lock){
    if (lock == null) {
      throw new NullPointerException();
    }
    this.lock = lock;
  }

  public abstract void close() throws IOException;

  /**
  ** real implementations should be synchronized ...
  */
  public abstract int read(char chars[], int off, int len) throws IOException;

  public void mark(int readLimit) throws IOException {
    throw new IOException("mark is not supported by "+this.getClass().getName());
  }

  public boolean markSupported(){
    return false;
  }

  public int read() throws IOException {
    char[] chars = new char[1];
    int rd = read(chars, 0, 1);
    if(rd < 1){
      return -1;
    }else{
      return chars[0];
    }
  }

  public int read(char[] chars) throws IOException {
    return read(chars, 0, chars.length);
  }

  public boolean ready() throws IOException{
    return false;
  }

  public void reset() throws IOException {
    throw new IOException("reset is not supported by "+this.getClass().getName());
  }

  public long skip(long skip) throws IOException {
    if (skip < 0){
      throw new IllegalArgumentException("cannot skip "+skip+" (value must be 0 or greater)");
    }
    synchronized(lock){
      char[] chars = new char[128];
      long remain=skip;
      do{
        int rd = read(chars,0 , (128 > remain ? (int) remain : 128));
        if (rd == -1){
          break;
        }
        remain -= rd;
      }while (remain > 0);
      return skip - remain;
    }
  }
}
