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

