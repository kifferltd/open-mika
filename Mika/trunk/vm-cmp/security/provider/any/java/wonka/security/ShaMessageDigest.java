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

public class ShaMessageDigest extends MessageDigestSpi implements Cloneable {

  private static final byte[] oneByte = new byte[1];

  public ShaMessageDigest(){}

  /** set-up of native structures */
  protected synchronized native void engineReset();

  protected synchronized void engineUpdate(byte input){
    oneByte[0] = input;
    engineUpdate(oneByte,0,1);
  }

  /**
  ** Add bytes to the message from the specified array
  ** @param input the array of bytes
  ** @param offset the starting point in the specified array
  ** @param len the number of bytes to add
  */
  protected synchronized  native void engineUpdate(byte[] input, int offset, int len);

  /**
  ** Calculate the digest
  ** @return the byte array containing the digest
  */
  protected byte[] engineDigest() {
    byte[] bytes = new byte[20];
    nativeDigest(bytes,0);
    return bytes;
  }

  /**
  ** Calculate the digest for the message and place it in the specified buffer
  ** @param buf the buffer in which to store the digest
  ** @param offset offset to start from in the buffer
  ** @param len number of bytes in buffer allocated for digest. This must be at least equal to the length of the digest
  ** @return the length of the digest stored in the buffer
  ** @exception DigestException not enough space has been allocated in the output buffer for the digest
  */
  protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
    if(len < 20){
      throw new DigestException();
    }
    nativeDigest(buf,offset);
    return 20;
  }

  /**
  ** Return length of digest in bytes
  ** @return the length of the digest in bytes
  */
  protected int engineGetDigestLength(){
    return 20;
  }

  /** clean-up of native part */
  protected synchronized native void finalize();

  /** fills up the array with the digest data */
  private synchronized native void nativeDigest(byte[] bytes, int off);

   public Object clone() throws CloneNotSupportedException {
      ShaMessageDigest clone = (ShaMessageDigest)  super.clone();
      // copy the native sha fields to the clone
      this.shaClone(clone);
      return clone;
  }

  private native void shaClone(ShaMessageDigest clone);

}

