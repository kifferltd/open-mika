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

/*
** $Id: DigestInputStream.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.security;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

public class DigestInputStream extends FilterInputStream {

  protected MessageDigest digest;
  private boolean digesting = false;

  public DigestInputStream(InputStream stream, MessageDigest digest) {
    super(stream);
    this.digest = digest;
  }

  public MessageDigest getMessageDigest() {
    return digest;
  }

  public void setMessageDigest(MessageDigest d) {
    digest = d;
  }

  public int read() throws IOException {
    int rd = in.read();
    if (digesting && rd != -1) {
      digest.update((byte)rd);
    }
    return rd;
  }

  public int read(byte[] b, int off, int len) throws IOException {
    int rd = in.read(b,off,len);
    if (digesting && rd != -1) {
      digest.update(b,off,len);
    }

    return rd;
  }

  public void on (boolean foo) {
    digesting = foo;
  }

  public String toString() {
    return "DigestInputStream on inputstream "+in+" and digest "+digest;
  }
  
}
