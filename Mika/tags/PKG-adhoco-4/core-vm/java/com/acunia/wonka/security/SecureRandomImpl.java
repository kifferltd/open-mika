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



/**
 * $Id: SecureRandomImpl.java,v 1.2 2005/03/19 13:42:30 cvs Exp $
 */

package com.acunia.wonka.security;

import java.security.SecureRandomSpi;
import java.util.Random;

/**
** This class is not very Secure since we use a the java.util.Random to get the next values.
** But at least it is a start and it will make sure that the SecureRandom class will work
** (but not so secure as wanted ...).
*/
public final class SecureRandomImpl extends SecureRandomSpi {

  private Random random;

  public SecureRandomImpl(){
    long seed = System.currentTimeMillis();
    int sign = (short)seed;
    seed = seed ^ ((long)sign)<<32;
    Runtime rt = Runtime.getRuntime();
    seed ^= rt.freeMemory()<<47;
    seed ^= (rt.totalMemory())<<33;
    seed ^= (((long)hashCode()) << 32 ) | rt.hashCode();
    random = new Random(seed);
  }

  protected byte[] engineGenerateSeed(int numOfBytes) {
    byte[] seed = new byte[numOfBytes];
    engineNextBytes(seed);
    return seed;
  }

  protected void engineNextBytes(byte[] bytes) {
    random.nextBytes(bytes);
    int size = bytes.length;
    for (int i = 0 ; i < size ; i++){
      int pos = random.nextInt(size);
      byte b = bytes[i];
      bytes[i] = bytes[pos];
      bytes[pos] = b;
    }
  }

  protected void engineSetSeed(byte[] seedBytes) {
    int stop = seedBytes.length;
    if(stop > 8){
      stop = 8;
    }
    long seed = 0;
    for(int i = 0 ; i < stop ; i++){
      seed = seed<<8 | (0xff & (char)seedBytes[i]);
    }
    random.setSeed(seed);
  }
}

