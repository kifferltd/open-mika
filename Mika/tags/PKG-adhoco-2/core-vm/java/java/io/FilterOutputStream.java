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
** $Id: FilterOutputStream.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;

public class FilterOutputStream extends OutputStream {

  protected OutputStream out;

  public FilterOutputStream(OutputStream out) {
    this.out = out;
  }

  public void write(int b) throws IOException {
    out.write(b);
  }

  public void write(byte b[]) throws IOException, NullPointerException {
    // if b=null then b.length will trigger the NullPointerException
    write(b, 0, b.length);
    // instead of writing directly to out we use a write defined within this class
    // this has the advantage that a class subclasses this one it will call the write from
    // that class. Example writing to a BufferedOutputStream ...
  }

  public void write(byte b[], int off, int len) 
      throws IOException, 
      NullPointerException, 
      ArrayIndexOutOfBoundsException 
  {
    // if b=null then b.length will trigger the NullPointerException
    if (off < 0 || len < 0 || off > b.length - len) {
      throw new ArrayIndexOutOfBoundsException();
    }

    out.write(b, off, len);
  }

  public void flush() throws IOException {
    out.flush();
  }

  public void close() throws IOException {
    flush();
    out.close();
  }
  
}
