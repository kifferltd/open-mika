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
** $Id: KeyPairGenerator.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
*/

package java.security;

import java.security.spec.AlgorithmParameterSpec;

public abstract class KeyPairGenerator extends KeyPairGeneratorSpi {

 public static KeyPairGenerator getInstance(String algorithm) throws NoSuchAlgorithmException{
    SecurityAction action = new SecurityAction(algorithm, "KeyPairGenerator.");
    KeyPairGenerator kpg = (action.spi instanceof KeyPairGenerator) ? (KeyPairGenerator)action.spi :
      new KeyPairGeneratorSpiWrapper((KeyPairGeneratorSpi)action.spi, algorithm);
    kpg.provider = action.provider;
    return kpg;
  }

 public static KeyPairGenerator getInstance(String algorithm, String provider)
 throws NoSuchAlgorithmException, NoSuchProviderException {
 SecurityAction action = new SecurityAction(algorithm, provider, "KeyPairGenerator.");
 KeyPairGenerator kpg = (action.spi instanceof KeyPairGenerator) ? (KeyPairGenerator)action.spi :
   new KeyPairGeneratorSpiWrapper((KeyPairGeneratorSpi)action.spi, algorithm);
 kpg.provider = action.provider;
 return kpg;
}

 public static KeyPairGenerator getInstance(String algorithm, Provider provider)
   throws NoSuchAlgorithmException {
   SecurityAction action = new SecurityAction(algorithm, provider, "KeyPairGenerator.");
   KeyPairGenerator kpg = (action.spi instanceof KeyPairGenerator) ? (KeyPairGenerator)action.spi :
     new KeyPairGeneratorSpiWrapper((KeyPairGeneratorSpi)action.spi, algorithm);
   kpg.provider = provider;
   return kpg;
  }

  private Provider provider;
  private String algorithm;

  protected KeyPairGenerator(String algorithm){
    this.algorithm = algorithm;
  }

  public final KeyPair genKeyPair(){
    return generateKeyPair();
  }

  public KeyPair generateKeyPair(){
    //it is unclear what the default behaviour should be.This method should be overriden to do something usefull
    throw new UnsupportedOperationException("KeyPairGenerator generateKeyPair "+this.getClass());
  }

  public String getAlgorithm(){
    return algorithm;
  }

  public final Provider getProvider(){
    return provider;
  }

  public void initialize(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
    throw new UnsupportedOperationException("KeyPairGenerator initialize(AlgorithmParameterSpec) "+this.getClass());
  }

  public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
    throw new UnsupportedOperationException("KeyPairGenerator initialize(AlgorithmParameterSpec,SecureRandom) "+this.getClass());
  }

  public void initialize(int keysize){
    initialize(keysize, new SecureRandom());
  }

  public void initialize(int keysize, SecureRandom random){
    throw new UnsupportedOperationException("KeyPairGenerator initialize(int,SecureRandom) "+this.getClass());
  }
}

