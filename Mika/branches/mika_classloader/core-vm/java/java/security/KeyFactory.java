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
