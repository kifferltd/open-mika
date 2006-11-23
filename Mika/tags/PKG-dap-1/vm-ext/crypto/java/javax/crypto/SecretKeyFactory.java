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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.spec.KeySpec;

public class SecretKeyFactory {

  public final static SecretKeyFactory getInstance(String algorithm)throws NoSuchAlgorithmException{
    SecurityAction action = new SecurityAction(algorithm, "SecretKeyFactory.");
    return new SecretKeyFactory((SecretKeyFactorySpi) action.spi, action.provider, algorithm);
  }

  public final static SecretKeyFactory getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException{
    SecurityAction action = new SecurityAction(algorithm, provider, "SecretKeyFactory.");
    return new SecretKeyFactory((SecretKeyFactorySpi) action.spi, action.provider, algorithm);
  }

  public final static SecretKeyFactory getInstance(String algorithm, String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {

    SecurityAction action = new SecurityAction(algorithm, provider, "SecretKeyFactory.");
    return new SecretKeyFactory((SecretKeyFactorySpi) action.spi, action.provider, algorithm);
  }


  private Provider provider;
  private SecretKeyFactorySpi keyFacSpi;
  private String algorithm;

  protected SecretKeyFactory(SecretKeyFactorySpi keyFacSpi, Provider provider, String algorithm){
    if (keyFacSpi == null || provider == null || algorithm == null){
            throw new NullPointerException();
    }
    this.keyFacSpi = keyFacSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  public final String getAlgorithm(){
    return algorithm;
  }

  public final Provider getProvider(){
    return provider;
  }

  public final SecretKey generateSecret(KeySpec keySpec)throws InvalidKeyException {
    return keyFacSpi.engineGenerateSecret(keySpec);
  }

  public final KeySpec getKeySpec(SecretKey key, Class keySpec)throws InvalidKeyException {
    return keyFacSpi.engineGetKeySpec(key, keySpec);
  }

  public final SecretKey translateKey(SecretKey key)throws InvalidKeyException {
    return keyFacSpi.engineTranslateKey(key);
  }

}
