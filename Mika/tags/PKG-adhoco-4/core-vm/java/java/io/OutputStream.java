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
** $Id: OutputStream.java,v 1.3 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;

public abstract class OutputStream {

  public OutputStream(){}

  public abstract void write(int b) throws IOException;

  public void write(byte[] b) throws IOException, NullPointerException {
    write(b, 0, b.length);
  }

  public void write(byte[] b, int off, int len) 
      throws IOException,
      NullPointerException,
      ArrayIndexOutOfBoundsException
  {
    if (off < 0 || len < 0 || off > b.length - len) {
      throw new ArrayIndexOutOfBoundsException();
    }
    for (int pos = off; pos < off + len; pos++) {
      write(b[pos]);
    }
  }

  public void flush() throws IOException {
  }

  public void close() throws IOException {
  }

}
