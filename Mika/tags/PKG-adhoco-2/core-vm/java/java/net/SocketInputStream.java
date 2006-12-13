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


package java.net;

import java.io.InputStream;
import java.io.IOException;

class SocketInputStream extends InputStream {

  private PlainSocketImpl impl;;

  public SocketInputStream (PlainSocketImpl s) {
    this.impl = s;
  }

  public int read() throws IOException {
     byte [] thebyte = new byte[1];
     if (impl.read(thebyte,0,1) != 1) {
    	return -1;        	
     }
     int i= (char)thebyte[0] & 0x0ff;
     return i;
  }

  public void close() throws IOException {
    impl.close();
  }

  public int read (byte[] b) throws IOException {
    return impl.read (b, 0, b.length);
  }

  public int read (byte[] b, int off, int len) throws IOException {
    return impl.read (b, off, len);
  }

  public int available() throws IOException {
    return impl.available(); 	
  }
}
