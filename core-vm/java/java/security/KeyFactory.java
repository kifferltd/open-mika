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

import java.security.spec.KeySpec;
import java.security.spec.InvalidKeySpecException;

/**
 *
 * @version	$Id: KeyFactory.java,v 1.3 2006/04/18 11:35:28 cvs Exp $
 *
 */
public class KeyFactory {

  public static KeyFactory getInstance(String algorithm) throws NoSuchAlgorithmException {
    SecurityAction action = new SecurityAction(algorithm, "KeyFactory.");
    return new KeyFactory((KeyFactorySpi) action.spi, action.provider, algorithm);
  }

  public static KeyFactory getInstance(String algorithm, String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {

    SecurityAction action = new SecurityAction(algorithm, provider,"KeyFactory.");
    return new KeyFactory((KeyFactorySpi) action.spi, action.provider, algorithm);
  }

  public static KeyFactory getInstance(String algorithm, Provider provider)
    throws NoSuchAlgorithmException {

    SecurityAction action = new SecurityAction(algorithm, provider,"KeyFactory.");
    return new KeyFactory((KeyFactorySpi) action.spi, provider, algorithm);
  }

  private KeyFactorySpi keyFacSpi;
  private Provider provider;
  private String algorithm;

  protected  KeyFactory(KeyFactorySpi keyFacSpi, Provider provider, String algorithm){
    this.keyFacSpi = keyFacSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }


  public final PrivateKey generatePrivate(KeySpec keySpec) throws InvalidKeySpecException {
    return keyFacSpi.engineGeneratePrivate(keySpec);
  }
  public final PublicKey generatePublic(KeySpec keySpec) throws InvalidKeySpecException {
    return keyFacSpi.engineGeneratePublic(keySpec);
  }

  public final String getAlgorithm(){
    return algorithm;
  }

  public final KeySpec getKeySpec(Key key, Class keySpec) throws InvalidKeySpecException {
    return keyFacSpi.engineGetKeySpec(key, keySpec);
  }

  public final Provider getProvider(){
    return provider;
  }

  public final Key translateKey(Key key) throws InvalidKeyException {
    return keyFacSpi.engineTranslateKey(key);
  }
}
