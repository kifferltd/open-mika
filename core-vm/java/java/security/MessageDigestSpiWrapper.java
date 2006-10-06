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
** $Id: MessageDigestSpiWrapper.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.security;

class MessageDigestSpiWrapper extends MessageDigest implements Cloneable {

  private MessageDigestSpi spi;

  public MessageDigestSpiWrapper(MessageDigestSpi mdspi, String algorithm){
    super(algorithm);
    spi = mdspi;
  }

  protected byte[] engineDigest(){
    return spi.engineDigest();
  }
  protected void engineReset(){
    spi.engineReset();
  }

  protected void engineUpdate(byte input){
    spi.engineUpdate(input);
  }
  protected void engineUpdate(byte[] input, int offset, int len){
    spi.engineUpdate(input, offset, len);
  }
  protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
    return spi.engineDigest(buf, offset, len);
  }

  protected int engineGetDigestLength() {
    return spi.engineGetDigestLength();
  }

  public Object clone() throws CloneNotSupportedException {
      MessageDigestSpiWrapper clone = (MessageDigestSpiWrapper)  super.clone();
      clone.spi = (MessageDigestSpi) spi.clone();
      return clone;
  }
}
