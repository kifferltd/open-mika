/**************************************************************************
* Copyright  (c) 2002 by Acunia N.V. All rights reserved.                 *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: StandardInputStream.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package wonka.io;

import java.io.*;

/**
 * StandardInputStream implements an InputStream over standard input (stdin).
 */
public class StandardInputStream extends InputStream {

  /**
   ** A one-byte buffer used to implement read() 
   ** as a special case of read(array,offset,length).
   */
  private byte [] microbuffer = new byte[1];

  /**
   ** Set true when the InputStream is created, and false when
   ** close() is called.
   */
  private boolean open;

  /**
   */
  public StandardInputStream() {
    // TODO: security check?

    this.open = true;
  }
  
  /** Method read() (single-character read) is implemented as a
   ** special case of read(array,offset,length).  This could be
   ** done more efficiently: feel free if you have the time.
   */
  public synchronized int read()
    throws IOException
  {
    int readlen = read(microbuffer,0,1);

    if (readlen>0) {

      return microbuffer[0] & 0xff;

    }
    else return readlen;

  }
  
  /**
   ** Method read(array) is a convenience method, a shorthand
   ** for read(array, 0, b.length).
   */
  public synchronized int read(byte[] b)
    throws IOException, NullPointerException 
  {
    return read(b, 0, b.length);
  }
  
  /**
   */
  public native int read(byte[] b, int off, int len)
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException 
  ;
  
  /**
   */
  public native long skip(long n) throws IOException;

  /**
   ** Method available() returns the maximum number of bytes which could
   ** now be read from the input stream without blocking.
   */
  public native synchronized int available()
    throws IOException;
  
  /**
   ** Method close() sets "open" false.
   */
  public void close()
    throws IOException
  {
    open = false;
  }
  
}
