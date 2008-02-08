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

