/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public class KeyGenerator {

  public final static KeyGenerator getInstance(String algorithm)throws NoSuchAlgorithmException{
    SecurityAction action = new SecurityAction(algorithm, "KeyGenerator.");
    return new KeyGenerator((KeyGeneratorSpi) action.spi, action.provider, algorithm);
  }

  public final static KeyGenerator getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException{
    SecurityAction action = new SecurityAction(algorithm, provider, "KeyGenerator.");
    return new KeyGenerator((KeyGeneratorSpi) action.spi, action.provider, algorithm);
  }

  public final static KeyGenerator getInstance(String algorithm, String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {

    SecurityAction action = new SecurityAction(algorithm, provider, "KeyGenerator.");
    return new KeyGenerator((KeyGeneratorSpi) action.spi, action.provider, algorithm);
  }


  private Provider provider;
  private KeyGeneratorSpi keyGenSpi;
  private String algorithm;

  protected KeyGenerator(KeyGeneratorSpi keyGenSpi, Provider provider, String algorithm){
    if (keyGenSpi == null || provider == null || algorithm == null){
            throw new NullPointerException();
    }
    this.keyGenSpi = keyGenSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  public final String getAlgorithm(){
    return algorithm;
  }

  public final Provider getProvider(){
    return provider;
  }

  public final SecretKey generateKey(){
    return keyGenSpi.engineGenerateKey();
  }

  public final void init(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
    keyGenSpi.engineInit(params, new SecureRandom());
  }

  public final void init(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
    keyGenSpi.engineInit(params, random);
  }

  public final void init(int keysize){
    keyGenSpi.engineInit(keysize, new SecureRandom());
  }

  public final void init(int keysize, SecureRandom random){
    keyGenSpi.engineInit(keysize, random);
  }

  public final void init(SecureRandom random){
    keyGenSpi.engineInit(random);
  }
}
