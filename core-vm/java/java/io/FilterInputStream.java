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
** $Id: FilterInputStream.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

public class FilterInputStream extends InputStream {

  protected InputStream in;

  protected FilterInputStream(InputStream input) {
    in = input;
  }

  public int read() throws IOException {
    return in.read();
  }
  
  public int read(byte[] b) throws IOException, NullPointerException {
    // if b=null then b.length will trigger the NullPointerException
    return read(b, 0, b.length);
    // subclasses only overwrite the read(byte[],int,int) --> by calling it
    // we will use the read defined by the subclass
    // this important for the BufferedInputStream, since otherwise you would read immediately
    // from the underlying stream while still unread bytes are in the buffer from the stream
  }
  
  public int read(byte[] b, int off, int len)
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException {

    //no argument check are done, this should be handled by 'in'
    return in.read(b, off, len);
  }
  
  public long skip(long n) throws IOException {
    return in.skip(n);
  }

  public int available() throws IOException {
     return in.available();
  }

  public void close() throws IOException {
    in.close();
  }

  public void mark(int readLimit) {
    in.mark(readLimit);
  }

  public void reset()
    throws IOException
  {
    in.reset();
  }

  public boolean markSupported() {
    return in.markSupported();
  }

}
