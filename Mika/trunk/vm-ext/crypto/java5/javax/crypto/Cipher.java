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
import java.security.InvalidKeyException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public class Cipher {

  static final byte[] EMPTY = new byte[0];

  //                UNINITIALIZED= 0;
  public static final int ENCRYPT_MODE = 1;
  public static final int DECRYPT_MODE = 2;
  public static final int WRAP_MODE    = 3;
  public static final int UNWRAP_MODE  = 4;

  public static final int PUBLIC_KEY   = 1;
  public static final int PRIVATE_KEY  = 2;
  public static final int SECRET_KEY   = 3;


  public final static Cipher getInstance(String algorithm)throws NoSuchAlgorithmException{
    Provider[] p = Security.getProviders();
    for (int i=0 ; i < p.length ; i++){
      try {
        return getInstance(algorithm, p[i]);
       }catch(NoSuchAlgorithmException e){}
    }
    throw new NoSuchAlgorithmException("couldn't find "+algorithm);
  }

  public final static Cipher getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
    int first = algorithm.indexOf('/');
    if(first == -1){
      SecurityAction action = new SecurityAction(algorithm, provider, "Cipher.");
      return new Cipher((CipherSpi) action.spi, action.provider, algorithm);
    }

    try {
      SecurityAction action = new SecurityAction(algorithm, provider, "Cipher.");
      return new Cipher((CipherSpi) action.spi, action.provider, algorithm);
    }catch(NoSuchAlgorithmException nae){}

    int last = algorithm.lastIndexOf('/');

    String padding = algorithm.substring(last+1);

    try {
      SecurityAction action = new SecurityAction(algorithm.substring(0,last), provider, "Cipher.");
      CipherSpi spi = (CipherSpi)action.spi;
      spi.engineSetPadding(padding);
      return new Cipher(spi, action.provider, algorithm);
    }catch(NoSuchPaddingException nspe){}

    //TODO -- check if mode is allowed to be empty ...
    String mode = (first == last ? "" : algorithm.substring(first+1,last));
    String base = algorithm.substring(0,first);

    try {
      SecurityAction action = new SecurityAction(base+"//"+padding, provider, "Cipher.");
      CipherSpi spi = (CipherSpi)action.spi;
      spi.engineSetMode(mode);
      return new Cipher(spi, action.provider, algorithm);
    }catch(NoSuchAlgorithmException nae){}

    SecurityAction action = new SecurityAction(base, provider, "Cipher.");
    CipherSpi spi = (CipherSpi)action.spi;
    spi.engineSetMode(mode);
    try {
      spi.engineSetPadding(padding);
    } catch (NoSuchPaddingException e) {
      throw new NoSuchAlgorithmException();
    }
    return new Cipher(spi, action.provider, algorithm);
  }

  public final static Cipher getInstance(String algorithm, String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {

    Provider p = Security.getProvider(provider);
    if (p == null){
      throw new NoSuchProviderException("couldn't find "+provider);
    }
    return getInstance(algorithm, p);
  }

  private Provider provider;
  private CipherSpi cipherSpi;
  private String algorithm;
  private int state;

  Cipher(CipherSpi cipherSpi, String algorithm){
    this.cipherSpi = cipherSpi;
    this.algorithm = algorithm;
  }

  protected Cipher(CipherSpi cipherSpi, Provider provider, String algorithm){
    if (cipherSpi == null || provider == null || algorithm == null){
            throw new NullPointerException();
    }
    this.cipherSpi = cipherSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  public final String getAlgorithm(){
    return algorithm;
  }

  public final Provider getProvider(){
    return provider;
  }

  public final byte[] doFinal() throws IllegalStateException, IllegalBlockSizeException, BadPaddingException {
    if((state != DECRYPT_MODE && state != ENCRYPT_MODE)){
      throw new IllegalStateException("Cipher is not initialized correctly");
    }
    return cipherSpi.engineDoFinal(EMPTY, 0, 0);
  }

  public final int doFinal(byte[] bytes, int offset)
    throws IllegalStateException, IllegalBlockSizeException, ShortBufferException, BadPaddingException {

    if((state != DECRYPT_MODE && state != ENCRYPT_MODE)){
      throw new IllegalStateException("Cipher is not initialized correctly");
    }
    if((bytes.length - offset - cipherSpi.engineGetOutputSize(0)) < 0){
      throw new ShortBufferException();
    }
    return cipherSpi.engineDoFinal(EMPTY, 0, 0, bytes, offset);
  }

  public final byte[] doFinal(byte[] in) throws IllegalStateException, IllegalBlockSizeException, BadPaddingException {
    return doFinal(in, 0, in.length);
  }


  public final byte[] doFinal(byte[] in, int offset, int length)
    throws IllegalStateException, IllegalBlockSizeException, BadPaddingException {
    if((state != DECRYPT_MODE && state != ENCRYPT_MODE)){
      throw new IllegalStateException("Cipher is not initialized correctly");
    }
    return cipherSpi.engineDoFinal(in, offset, length);
  }

  public final int doFinal(byte[] in, int offset, int length, byte[] bytes)
    throws IllegalStateException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {

    if((bytes.length - cipherSpi.engineGetOutputSize(length)) < 0){
      throw new ShortBufferException();
    }
    return cipherSpi.engineDoFinal(in, offset, length, bytes, 0);
  }

  public final int doFinal(byte[] in, int offset, int length, byte[] bytes, int offbytes)
    throws IllegalStateException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {

    if((bytes.length - offbytes - cipherSpi.engineGetOutputSize(length)) < 0){
      throw new ShortBufferException();
    }
    return cipherSpi.engineDoFinal(in, offset, length, bytes, offbytes);
  }

  public final int getBlockSize(){
    return cipherSpi.engineGetBlockSize();
  }

  public final ExemptionMechanism getExemptionMechanism(){
    return null;
  }

  public final int getOutputSize(int length) throws IllegalStateException {
    if((state != DECRYPT_MODE && state != ENCRYPT_MODE)){
      throw new IllegalStateException("Cipher is not initialized correctly");
    }
    return cipherSpi.engineGetOutputSize(length);
  }

  public final byte[] getIV(){
    return cipherSpi.engineGetIV();
  }

  public final AlgorithmParameters getParameters(){
    return cipherSpi.engineGetParameters();
  }

  public final void init(int mode, Key key) throws InvalidKeyException {
    cipherSpi.engineInit(mode, key, new SecureRandom());
    state = mode;
  }

  public final void init(int mode, Key key, SecureRandom srandom) throws InvalidKeyException {
    cipherSpi.engineInit(mode, key, srandom);
    state = mode;
  }

  public final void init(int mode, Key key, AlgorithmParameterSpec params)
    throws InvalidKeyException, InvalidAlgorithmParameterException {

    cipherSpi.engineInit(mode, key, params, new SecureRandom());
    state = mode;
  }

  public final void init(int mode, Key key, AlgorithmParameterSpec params, SecureRandom srandom)
    throws InvalidKeyException, InvalidAlgorithmParameterException {

    cipherSpi.engineInit(mode, key, params, srandom);
    state = mode;
  }

  public final void init(int mode, Key key, AlgorithmParameters params)
    throws InvalidKeyException, InvalidAlgorithmParameterException {

    cipherSpi.engineInit(mode, key, params, new SecureRandom());
    state = mode;
  }

  public final void init(int mode, Key key, AlgorithmParameters params, SecureRandom srandom)
    throws InvalidKeyException, InvalidAlgorithmParameterException {

    cipherSpi.engineInit(mode, key, params, srandom);
    state = mode;
  }

  public final void init(int mode, java.security.cert.Certificate cert) throws InvalidKeyException {
    cipherSpi.engineInit(mode, cert.getPublicKey(), new SecureRandom());
    state = mode;
  }

  public final void init(int mode, java.security.cert.Certificate cert, SecureRandom srandom) throws InvalidKeyException {
    cipherSpi.engineInit(mode, cert.getPublicKey(), srandom);
    state = mode;
  }

  public final byte[] update(byte[] in) throws IllegalStateException {
    return update(in, 0, in.length);
  }

  public final byte[] update(byte[] in, int offset, int length) throws IllegalStateException {
    if((state != DECRYPT_MODE && state != ENCRYPT_MODE)){
      throw new IllegalStateException("Cipher is not initialized correctly");
    }
    return cipherSpi.engineUpdate(in, offset, length);
  }

  public final int update(byte[] in, int offset, int length, byte[] bytes)
    throws IllegalStateException, ShortBufferException {

    return update(in, offset, length, bytes, 0);
  }

  public final int update(byte[] in, int offset, int length, byte[] bytes, int offbytes)
    throws IllegalStateException, ShortBufferException {

    if((state != DECRYPT_MODE && state != ENCRYPT_MODE)){
      throw new IllegalStateException("Cipher is not initialized correctly");
    }
    return cipherSpi.engineUpdate(in, offset, length, bytes, offbytes);
  }

  public final Key unwrap(byte[] bytes, String keyAlgorithm, int type)
    throws IllegalStateException, InvalidKeyException, NoSuchAlgorithmException {
    if(state != UNWRAP_MODE){
      throw new IllegalStateException("Cipher is not initialized correctly");
    }
    return cipherSpi.engineUnwrap(bytes, keyAlgorithm, type);
  }

  public final byte[] wrap(Key key) throws IllegalStateException, IllegalBlockSizeException, InvalidKeyException {
    if(state != WRAP_MODE){
      throw new IllegalStateException("Cipher is not initialized correctly");
    }
    return cipherSpi.engineWrap(key);
  }
}
