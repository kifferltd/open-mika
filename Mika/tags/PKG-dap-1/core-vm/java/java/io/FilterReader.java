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
** $Id: FilterReader.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

public abstract class FilterReader extends Reader {

  protected Reader in;

  protected FilterReader(Reader in) {
    super(in);
    this.in = in;
  }

  public void close() throws IOException {
    synchronized(lock){
        in.close();
    }
  }

  public void mark(int readLimit) throws IOException {
    synchronized(lock){
       in.mark(readLimit);
    }
  }

  public boolean markSupported(){
    return in.markSupported();
  }

  public int read() throws IOException {
    synchronized(lock){
       return in.read();
    }
  }

  public int read(char[] chars, int off, int len) throws IOException {
    synchronized(lock){
       return in.read(chars, off, len);
    }
  }

  public boolean ready() throws IOException{
    synchronized(lock){
      return in.ready();
    }
  }

  public void reset() throws IOException{
    synchronized(lock){
       in.reset();
    }
  }

  public long skip(long n) throws IOException{
    synchronized(lock){
       return in.skip(n);
    }
  }
}
