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

import java.io.OutputStream;
import java.io.IOException;

class SocketOutputStream extends OutputStream {

  private PlainSocketImpl impl;

  protected SocketOutputStream (PlainSocketImpl i) {
    this.impl = i;
  }

  public void write (int b) throws IOException {
    byte [] bytes = new byte[1];
    bytes[0] = (byte)b;
    impl.write (bytes,0,1);
  }

  public void write (byte [] b) throws IOException {
    impl.write(b,0,b.length);
  }

  public void write (byte [] b, int start, int length) throws IOException {
    impl.write(b,start,length);
  }

  public void close() throws IOException {
    impl.close();
  }

}
