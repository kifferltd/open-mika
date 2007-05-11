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


package javax.crypto;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;

public class CipherOutputStream extends FilterOutputStream {

private Cipher cipher;

  public CipherOutputStream(OutputStream out, Cipher c){
    super(out);
    cipher = c;
  }

  protected CipherOutputStream(OutputStream out){
    super(out);
    cipher = new NullCipher();
  }

  public void close() throws IOException {
    if(cipher != null){
      try {
        out.write(cipher.doFinal());
      }
      catch(Exception e){
        throw new IOException("WRAPPING EXCEPTION "+e);
      }
      out.close();
      cipher = null;
    }
  }

  public void flush() throws IOException {
    if(cipher == null){
      throw new IOException("STREAM IS CLOSED");
    }
  }

  public void write(int b) throws IOException {
    byte[] theByte = new byte[1];
    theByte[0] = (byte)b;
    write(theByte, 0 , 1);
  }

  public void write(byte[] bytes) throws IOException {
    write(bytes, 0, bytes.length);
  }

  public void write(byte[] bytes, int offset, int length) throws IOException {
    if(cipher == null){
      throw new IOException("STREAM IS CLOSED");
    }
    out.write(cipher.update(bytes, offset, length));
  }
}