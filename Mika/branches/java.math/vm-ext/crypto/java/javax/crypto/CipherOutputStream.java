/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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
