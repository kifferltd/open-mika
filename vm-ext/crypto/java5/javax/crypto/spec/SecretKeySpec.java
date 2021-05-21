/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2007, 2009 by Chris Gray, /k/ Embedded Java         *
* Solutions.  All rights reserved.                                        *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package javax.crypto.spec;

import java.security.spec.KeySpec;
import javax.crypto.SecretKey;

public class SecretKeySpec implements KeySpec,SecretKey {

  private byte[] key;
  private String algorithm;
  private int hashcode;

  public SecretKeySpec(byte[] key, int offset, int len, String algorithm){
    if (algorithm == null) {
      throw new IllegalArgumentException();
    }
    this.key = new byte[len];
    try {
      System.arraycopy(key, offset, this.key, 0, len);
    }
    catch (Exception e) {
      // E.g. key is null or offset/length is wrong
      throw new IllegalArgumentException();
    }
    this.algorithm = algorithm;
  }

  public SecretKeySpec(byte[] key, String algorithm) {
    this(key, 0, key.length, algorithm);
  }

  public boolean equals(Object obj){
    try {
      SecretKeySpec that = (SecretKeySpec)obj;
      boolean result = this.algorithm.equalsIgnoreCase(that.algorithm) && this.key.length == that.key.length;

      for (int i = 0; result && i < key.length; ++i) {
        result &= (this.key[i] == that.key[i]);
      }

      return result;
    }
    catch (ClassCastException cce) {
      return false;
    }
  }

  public String getAlgorithm(){
    return algorithm;
  }

  public byte[]getEncoded() {
    byte[] encoded = new byte[key.length];
    System.arraycopy(key, 0, encoded, 0, key.length);

    return encoded;
  }

  public String getFormat(){
    return "RAW";
  }

  public int hashCode(){
    if (hashcode == 0) {
      StringBuffer hashbuf = new StringBuffer(algorithm.toUpperCase().toLowerCase());
      for (int i = 0; i < key.length; ++i) {
        hashbuf.append((char)key[i]);
      }
      hashcode = hashbuf.toString().hashCode();
    }

    return hashcode;
  }
}
