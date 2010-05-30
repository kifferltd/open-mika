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
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;

public class Mac implements Cloneable {

  public final static Mac getInstance(String algorithm)throws NoSuchAlgorithmException{
    SecurityAction action = new SecurityAction(algorithm, "Mac.");
    return new Mac((MacSpi) action.spi, action.provider, algorithm);
  }

  public final static Mac getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException{
    SecurityAction action = new SecurityAction(algorithm, provider, "Mac.");
    return new Mac((MacSpi) action.spi, action.provider, algorithm);
  }

  public final static Mac getInstance(String algorithm, String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {

    SecurityAction action = new SecurityAction(algorithm, provider, "Mac.");
    return new Mac((MacSpi) action.spi, action.provider, algorithm);
  }

  private Provider provider;
  private MacSpi macSpi;
  private String algorithm;
  private boolean invalidState = true;

  protected Mac(MacSpi macSpi, Provider provider, String algorithm){
    if (macSpi == null || provider == null || algorithm == null){
      throw new NullPointerException();
    }
    this.macSpi = macSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  public final String getAlgorithm(){
    return algorithm;
  }

  public final Provider getProvider(){
    return provider;
  }

  public final Object clone() throws CloneNotSupportedException {
    Mac clone = (Mac) super.clone();
    clone.macSpi = (MacSpi) macSpi.clone();
    return clone;
  }

  public final byte[] doFinal() throws IllegalStateException {
    if(invalidState){
      throw new IllegalStateException("Mac was not initialized");
    }
    return macSpi.engineDoFinal();
  }

  public final void doFinal(byte[] bytes, int offset) throws ShortBufferException, IllegalStateException {
    if(invalidState){
      throw new IllegalStateException("Mac was not initialized");
    }
    int len = macSpi.engineGetMacLength();
    if((bytes.length - offset - len) < 0){
      throw new ShortBufferException();
    }
    System.arraycopy(macSpi.engineDoFinal(),0, bytes, offset, len);
  }

  public final byte[] doFinal(byte[] bytes) throws IllegalStateException {
    if(invalidState){
      throw new IllegalStateException("Mac was not initialized");
    }
    macSpi.engineUpdate(bytes, 0, bytes.length);
    return macSpi.engineDoFinal();
  }


  public final int getMacLength(){
    return macSpi.engineGetMacLength();
  }

  public final void init(Key key) throws InvalidKeyException {
    try {
      macSpi.engineInit(key, null);
      invalidState = false;
    }
    catch(InvalidAlgorithmParameterException iape){
      //TODO ... is this the right thing to do ?
      throw new InvalidKeyException();
    }
  }

  public final void init(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
    macSpi.engineInit(key, params);
    invalidState = false;
  }

  public final void reset(){
    macSpi.engineReset();
  }

  public final void update(byte b) throws IllegalStateException {
    if(invalidState){
      throw new IllegalStateException("Mac was not initialized");
    }
    macSpi.engineUpdate(b);
  }

  public final void update(byte[] bytes) throws IllegalStateException {
    if(invalidState){
      throw new IllegalStateException("Mac was not initialized");
    }
    macSpi.engineUpdate(bytes, 0, bytes.length);
  }

  public final void update(byte[] bytes, int offset, int len) throws IllegalStateException {
    if(invalidState){
      throw new IllegalStateException("Mac was not initialized");
    }
    macSpi.engineUpdate(bytes, offset, len);
  }
}
