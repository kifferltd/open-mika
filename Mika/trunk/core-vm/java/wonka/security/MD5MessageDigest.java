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

package wonka.security;

import java.security.MessageDigestSpi;
import java.security.DigestException;

public class MD5MessageDigest extends MessageDigestSpi implements Cloneable {

  private static final byte[] OneByte = new byte[1];

  public MD5MessageDigest() {}

  protected synchronized native void engineReset();

  protected synchronized void engineUpdate(byte input){
    OneByte[0] = input;
    engineUpdate(OneByte,0,1);
  }

  protected synchronized native void engineUpdate(byte[] input, int offset, int len);

  protected byte[] engineDigest(){
    byte[] bytes = new byte[16];
    nativeDigest(bytes,0);
    return bytes;
  }

  protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
    if(len < 16){
      throw new DigestException();
    }
    nativeDigest(buf,offset);
    return 16;
  }

  protected int engineGetDigestLength(){
	  return 16;
  }

  protected synchronized native void finalize();

  private synchronized native void nativeDigest(byte[] bytes, int off);

  public Object clone() throws CloneNotSupportedException {
      MD5MessageDigest clone = (MD5MessageDigest)  super.clone();
      // copy the native md5 fields to the clone
      this.md5Clone(clone);
      return clone;
  }

  private native void md5Clone(MD5MessageDigest clone);

}

