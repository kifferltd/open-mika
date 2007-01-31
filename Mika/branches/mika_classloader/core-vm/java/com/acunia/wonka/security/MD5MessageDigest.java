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

