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
** $Id: InputStream.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;

public abstract class InputStream {

  public InputStream(){}

  public abstract int read() throws IOException;
  

  public int read(byte[] b) 
    throws IOException 
  {
    return read(b, 0, b.length);
  }

  
  public int read(byte [] b, int off, int len) 
    throws IOException 
  {
    if(off<0 || len<0 || off > b.length - len)
	throw new ArrayIndexOutOfBoundsException();

    int i=0;
    int next;
    for(i=0 ; i<len ; i++) {
      next = read();
      if (next<0) 
        break;

      b[i+off] = (byte)next;
    }

    return ((i==0) ? -1 : i);
  }

  
  public long skip(long n)
    throws IOException
  {
    if(n < 0 || n > Integer.MAX_VALUE){
      return 0;
    }

    int m = (int)n;
    long j;
    int next;

    for(j=0;j<m;++j) {
      next = read();
      if (next<0) break;
    }

    return j;
  }


  public int available()
    throws IOException
  {
    return 0;
  }

  public void close() throws IOException {}

  public void mark(int readLimit) {}

  public void reset() throws IOException {
    throw new IOException();
  }

  public boolean markSupported() {
    return false;
  }

}
