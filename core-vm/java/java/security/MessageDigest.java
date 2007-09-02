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
** $Id: MessageDigest.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
*/

package java.security;

import java.util.Arrays;

public abstract class MessageDigest extends MessageDigestSpi {

  public static boolean isEqual(byte[] first, byte[] second){
    return Arrays.equals(first,second);
  }

  public static MessageDigest getInstance(String algorithm) throws NoSuchAlgorithmException {
    SecurityAction action = new SecurityAction(algorithm,"MessageDigest.");
    MessageDigest msgD = (action.spi instanceof MessageDigest) ?
      (MessageDigest)action.spi : new MessageDigestSpiWrapper((MessageDigestSpi)action.spi, algorithm);
    msgD.provider = action.provider;
    return msgD;
  }

  public static MessageDigest getInstance(String algorithm,String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {

    SecurityAction action = new SecurityAction(algorithm, provider, "MessageDigest.");
    MessageDigest msgD = (action.spi instanceof MessageDigest) ?
      (MessageDigest)action.spi : new MessageDigestSpiWrapper((MessageDigestSpi)action.spi, algorithm);
    msgD.provider = action.provider;
    return msgD;
  }

  public static MessageDigest getInstance(String algorithm,Provider provider)
  throws NoSuchAlgorithmException {

    SecurityAction action = new SecurityAction(algorithm, provider, "MessageDigest.");
    MessageDigest msgD = (action.spi instanceof MessageDigest) ?
        (MessageDigest)action.spi : 
        new MessageDigestSpiWrapper((MessageDigestSpi)action.spi, algorithm);
        
    msgD.provider = provider;
    return msgD;
  }
  
  private Provider provider;
  private String algorithm;

  protected MessageDigest(String algorithm) {
    this.algorithm = algorithm;
  }


  public final Provider getProvider(){
    return provider;
  }

  public void update(byte b){
    engineUpdate(b);
  }

  public void update(byte[] bytes, int offset, int len){
    engineUpdate(bytes, offset, len);
  }

  public void update(byte[] bytes){
    engineUpdate(bytes, 0, bytes.length);
  }

  public byte[] digest(){
    return engineDigest();
  }

  public int digest(byte[] buf, int offset, int len) throws DigestException{
    return engineDigest(buf, offset, len);
  }

  public byte[] digest(byte[] bytes) {
    engineUpdate(bytes, 0, bytes.length);
        return engineDigest();
  }

  public String toString(){
    return "Message Digest based on "+algorithm+" and provided by "+provider;
  }

  public void reset(){
    engineReset();
  }

  public final String getAlgorithm(){
    return algorithm;
  }

  public final int getDigestLength() {
    try {
      return engineGetDigestLength();
    }catch (Exception e){
      return 0;
    }
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

}
