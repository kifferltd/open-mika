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

package com.acunia.wonka.security;

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

