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
** $Id: BufferedOutputStream.java,v 1.2 2006/10/04 14:24:14 cvsroot Exp $
*/

package java.io;

public class BufferedOutputStream extends FilterOutputStream {

  protected byte[] buf;
  protected int count;

  public BufferedOutputStream(OutputStream out) {
    this(out, 512);
  }

  public BufferedOutputStream(OutputStream out, int size) {
    super(out);
    if (size < 1 ) throw new IllegalArgumentException("size should be bigger than 0");
    buf = new byte[size];
    count = 0;
  }

  public void write(int b) throws IOException, NullPointerException {
    if (count == buf.length) {
     	out.write(buf,0,count);
    	count=0;
    }
    buf[count++] = (byte)b;

  }

  public void write(byte[] b, int off, int len) throws IOException, NullPointerException {
    if (off > b.length - len || off < 0 || len < 0) {
      throw new IndexOutOfBoundsException("offset = " + off + ", length = " + len + ", length of " + b + " is " + b.length);
    }
    if(len > 0){
      if (len+count > buf.length) {
      	out.write(buf,0,count);
      	count=0;
      }
      if(len >= buf.length){
        out.write(b,off,len);
      }
      else {
      	System.arraycopy(b, off, buf, count, len);
      	count += len;
      }
    }
  }

  public void flush() throws IOException {   
    if(count > 0){
      out.write(buf, 0, count);
      count = 0;
    }
    out.flush();
  }

}

