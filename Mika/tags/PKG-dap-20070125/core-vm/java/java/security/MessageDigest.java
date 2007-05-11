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
