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
** $Id: ByteArrayInputStream.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

public class ByteArrayInputStream extends InputStream {

  protected byte[] buf;
  protected int pos;
  protected int count;
  protected int mark ;

  public ByteArrayInputStream(byte[] b) {
    this(b, 0, b.length);
  }

  public ByteArrayInputStream(byte[] b, int off, int len) {
    if (off < 0 || len < 0 || off > b.length){
      throw new ArrayIndexOutOfBoundsException();
    }
    buf   = b;
    count = (off+len > b.length) ? b.length - off : off+len;
    pos   = off;
    mark  = off;
  }
 
  public synchronized int read(){
    if(pos >= count){
      return -1;
    }else {
      return (0xff & ((char) buf[pos++]));
    }
  }

  public void close() throws IOException {}

  public synchronized int read(byte[] b, int off, int len){
    if(pos >= count){
      return -1;
    }
    int k = count-pos;
    k = (len > k ? k : len);
    System.arraycopy(buf, pos, b, off, k);
    pos += k;
    return k;
  }

  public synchronized long skip(long n){
    if (n <= 0){
      return 0;
    }
    int k = count-pos;
    k = (n > k) ? k :(int) n;
    pos += k;
    return k;
  }

  public synchronized int available(){
    return count-pos;
  }

   public synchronized void mark(int readAheadLimit){
     mark = pos;
    }

  public synchronized void reset(){
    pos = mark;
  }

  public boolean markSupported() {
    return true;
  }
}
