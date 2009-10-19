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


package java.security;

import java.util.Enumeration;
import java.util.Random;

public class SecureRandom extends Random {

  private static final long serialVersionUID = 4940670005562187L;

  public static SecureRandom getInstance(String algorithm) throws NoSuchAlgorithmException {
    SecurityAction action = new SecurityAction(algorithm, "SecureRandom.");
    return new SecureRandom((SecureRandomSpi)action.spi, action.provider);
  }

  public static SecureRandom getInstance(String algorithm, String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {

    SecurityAction action = new SecurityAction(algorithm, provider, "SecureRandom.");
    return new SecureRandom((SecureRandomSpi)action.spi, action.provider);
  }

  public static SecureRandom getInstance(String algorithm, Provider provider)
    throws NoSuchAlgorithmException {

    SecurityAction action = new SecurityAction(algorithm, provider, "SecureRandom.");
    return new SecureRandom((SecureRandomSpi)action.spi, provider);
  }

  public static byte[] getSeed(int numBytes){
    return new SecureRandom().generateSeed(numBytes);
  }

  /**
  ** dictated by the Serialized From ...
  */
  private long counter;
  private MessageDigest digest;
  private Provider provider;
  private byte[] randomBytes;
  private int randomBytesUsed;
  private SecureRandomSpi secureRandomSpi;
  private byte[] state;

  public SecureRandom(){
    Provider[] p = Security.getProviders();
    for (int i=0 ; i < p.length ; i++){
      Provider prov = p[i];
      Enumeration e = prov.propertyNames();
      while(e.hasMoreElements()){
        String key = (String)e.nextElement();
        if(key.startsWith("SecureRandom.")){
          String classname = prov.getProperty(key);
          try {
            secureRandomSpi = (SecureRandomSpi)Class.forName(classname, true, SecurityAction.getClassLoader(prov)).newInstance();
            provider = prov;
            i = p.length;
            break;
          }catch(Exception ex){
            ex.printStackTrace();
          }
        }
      }
    }
    if(secureRandomSpi == null){
      throw new RuntimeException("couldn't find a SecureRandom");
    }
  }

  public SecureRandom(byte[] seed){
    this();
    setSeed(seed);
  }

  protected SecureRandom(SecureRandomSpi secureRandomSpi, Provider provider){
    this.secureRandomSpi = secureRandomSpi;
    this.provider = provider;
  }

  public byte[] generateSeed(int numBytes){
    return secureRandomSpi.engineGenerateSeed(numBytes);
  }

  public final Provider getProvider(){
    return provider;
  }

  protected final int next(int numBits){
    if (numBits <= 0 || numBits > 32){
      throw new IllegalArgumentException();
    }
    int bts = (numBits-1)/8+1;
    byte [] bytes = new byte[bts];
    nextBytes(bytes);
    if (bts > 0){
      int mod = (0x0ff >> (8 - ( numBits % 8 )));
      if(mod > 0) {
        bytes[bts-1] = (byte)((0xff & (char) bytes[bts-1]) & mod);
      }
    }
    int next = 0;
    for (int i=0 ; i < bts ; i++){
      next =  (next << 8) + (0x0ff & (char)bytes[i]);
    }
    return next;
  }

  public void nextBytes(byte[] bytes){
    secureRandomSpi.engineNextBytes(bytes);
  }

  public void setSeed(byte[] seed){
    secureRandomSpi.engineSetSeed(seed);
  }

  public void setSeed(long seed){
    //if we come into the super() this method gets called
    if(secureRandomSpi != null){
      byte[] bytes = new byte[8];
      for (int i=0 ; i < 8 ; i++){
        bytes[i] = (byte)seed;
        seed = (seed >> 8);
      }
      setSeed(bytes);
    }
  }
}
