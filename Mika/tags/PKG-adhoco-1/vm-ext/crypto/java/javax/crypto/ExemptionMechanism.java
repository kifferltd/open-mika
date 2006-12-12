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

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;

public class ExemptionMechanism {

  public final static ExemptionMechanism getInstance(String algorithm)throws NoSuchAlgorithmException{
    SecurityAction action = new SecurityAction(algorithm, "ExemptionMechanism.");
    return new ExemptionMechanism((ExemptionMechanismSpi) action.spi, action.provider, algorithm);
  }

  public final static ExemptionMechanism getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException{
    SecurityAction action = new SecurityAction(algorithm, provider, "ExemptionMechanism.");
    return new ExemptionMechanism((ExemptionMechanismSpi) action.spi, provider, algorithm);
  }

  public final static ExemptionMechanism getInstance(String algorithm, String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {

    SecurityAction action = new SecurityAction(algorithm, provider, "ExemptionMechanism.");
    return new ExemptionMechanism((ExemptionMechanismSpi) action.spi, action.provider, algorithm);
  }

  private Provider provider;
  private ExemptionMechanismSpi exMechSpi;
  private String algorithm;
  private Key myKey;
  private boolean blob;

  protected ExemptionMechanism(ExemptionMechanismSpi exMechSpi, Provider provider, String algorithm){
    if (exMechSpi == null || provider == null || algorithm == null){
            throw new NullPointerException();
    }
    this.exMechSpi = exMechSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  public final String getName(){
    return algorithm;
  }

  public final Provider getProvider(){
    return provider;
  }

  protected void finalize(){
    myKey = null;
  }

  public final byte[] genExemptionBlob() throws IllegalStateException, ExemptionMechanismException {
    if(myKey == null){
      throw new IllegalStateException("ExemptionMechanism is not initialized yet");
    }
    byte[] blobs = exMechSpi.engineGenExemptionBlob();
    blob = true;
    return blobs;
  }

  public final int genExemptionBlob(byte[] bytes)
    throws IllegalStateException, ShortBufferException, ExemptionMechanismException {

    return genExemptionBlob(bytes, 0);
  }

  public final int genExemptionBlob(byte[] bytes, int offset)
    throws IllegalStateException, ShortBufferException, ExemptionMechanismException {

    if(myKey == null){
      throw new IllegalStateException("ExemptionMechanism is not initializad yet");
    }
    int result = exMechSpi.engineGenExemptionBlob(bytes, offset);
    blob = true;
    return result;
  }

  public final int getOutputSize(int length) throws IllegalStateException {
    if(myKey == null){
      throw new IllegalStateException("ExemptionMechanism is not initializad yet");
    }
    return exMechSpi.engineGetOutputSize(length);
  }

  public final void init(Key key) throws InvalidKeyException, ExemptionMechanismException {
    exMechSpi.engineInit(key);
    myKey = key;
    blob = false;
  }

  public final void init(Key key, AlgorithmParameterSpec params)
    throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException {

    exMechSpi.engineInit(key, params);
    myKey = key;
    blob = false;
  }

  public final void init(Key key, AlgorithmParameters params)
    throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException {

    exMechSpi.engineInit(key, params);
    myKey = key;
    blob = false;
  }

  public final boolean isCryptoAllowed(Key key) throws ExemptionMechanismException {
    if(myKey != key){
      throw new ExemptionMechanismException("different key specified");
    }
    return blob;
  }
}
