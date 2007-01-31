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
** $Id: Signature.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
*/

package java.security;

import java.security.spec.AlgorithmParameterSpec;

public abstract class Signature extends SignatureSpi {

  protected static final int SIGN = 2;
  protected static final int UNINITIALIZED = 0;
  protected static final int VERIFY = 3;

  private static final String SIGNATURE_DOT = "Signature.";

  public static Signature getInstance(String algorithm) throws NoSuchAlgorithmException{
    SecurityAction action = new SecurityAction(algorithm, SIGNATURE_DOT);
    Signature sign = (action.spi instanceof Signature) ? (Signature)action.spi :
     new SignatureSpiWrapper((SignatureSpi)action.spi,algorithm);
    sign.provider = action.provider;
    return sign;
  }

  public static Signature getInstance(String algorithm, String provider)
    throws NoSuchAlgorithmException, NoSuchProviderException {

    SecurityAction action = new SecurityAction(algorithm, provider, SIGNATURE_DOT);
    Signature sign = (action.spi instanceof Signature) ? (Signature)action.spi :
      new SignatureSpiWrapper((SignatureSpi)action.spi,algorithm);
    sign.provider = action.provider;
    return sign;
  }

  public static Signature getInstance(String algorithm, Provider provider)
    throws NoSuchAlgorithmException {

    SecurityAction action = new SecurityAction(algorithm, provider, SIGNATURE_DOT);
    Signature sign = (action.spi instanceof Signature) ? (Signature)action.spi :
      new SignatureSpiWrapper((SignatureSpi)action.spi,algorithm);
    sign.provider = provider;
    return sign;
  }

  private String algorithm;
  private Provider provider;

  protected int state = UNINITIALIZED;

  protected Signature(String algorithm){
    if (algorithm == null){
      throw new NullPointerException();
    }
    this.algorithm = algorithm;
  }

  public Object clone() throws CloneNotSupportedException{
    return super.clone();
  }

  public final AlgorithmParameters getParameters() {
    return this.engineGetParameters();
  }
  
  public final boolean verify(byte[] signature, int offset, int length) 
    throws SignatureException {
    if(signature == null || offset < 0 || length < 0 ||
        length + offset > signature.length) {
      throw new IllegalArgumentException();
    }
    return this.engineVerify(signature, offset, length);
  }
  
  public final String getAlgorithm(){
    return algorithm;
  }
/**
** @deprecated
*/
  public final Object getParameter(String param){
    //System.out.println("java.security.Signature: getParameter("+param+") is called");
    return engineGetParameter(param);
  }

  public final Provider getProvider(){
    return provider;
  }

  public final void initSign(PrivateKey prtKey) throws InvalidKeyException{
    //System.out.println("java.security.Signature: initSign("+prtKey+") is called");
    engineInitSign(prtKey);
    state = SIGN;
  }

  public final void initSign(PrivateKey prtKey, SecureRandom rnd) throws InvalidKeyException{
    //System.out.println("java.security.Signature: initSign("+prtKey+", "+rnd+") is called");
    engineInitSign(prtKey, rnd);
    state = SIGN;
  }

  public final void initVerify(PublicKey publicKey) throws InvalidKeyException{
    //System.out.println("java.security.Signature: initVerify("+publicKey+") is called");
    engineInitVerify(publicKey);
    state = VERIFY;
  }

  public final void initVerify(java.security.cert.Certificate certificate) throws InvalidKeyException {
    engineInitVerify(certificate.getPublicKey());
    state = VERIFY;
  }

  public final void setParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException{
    //System.out.println("java.security.Signature: setParameter("+params+") is called");
    engineSetParameter(params);
  }

/**
** @deprecated. Use setParameter.
*/
  public final void setParameter(String param, Object value){
    //System.out.println("java.security.Signature: setParameter("+param+", "+value+") is called");
    engineSetParameter(param, value);
  }

  public final byte[] sign() throws SignatureException{
    //System.out.println("java.security.Signature: sign() is called");
    if(state != SIGN){
      throw new SignatureException("the Signature has an invalid state");
    }
    return engineSign();
  }

  public final int sign(byte[] buf, int offset, int len) throws SignatureException{
    //System.out.println("java.security.Signature: sign("+buf+", "+offset+", "+len+") is called");
    if(state != SIGN){
      throw new SignatureException("the Signature has an invalid state");
    }
    return engineSign(buf, offset, len);
  }

  public String toString(){
    String s = null;
    switch(state){
      case SIGN:
        s = "SIGN";
        break;
      case VERIFY:
        s = "VERIFY";
        break;
      default:
        s = "UNINITIALIZED";
    }
    return "Signature state = "+s+" using "+algorithm;
  }

  public final void update(byte b) throws SignatureException{
    //System.out.println("java.security.Signature: update("+b+") is called");
    if(state == UNINITIALIZED){
      throw new SignatureException("the Signature has not initialized");
    }
    engineUpdate(b);
  }

  public final void update(byte[] data) throws SignatureException{
    //System.out.println("java.security.Signature: update("+data+") is called");
    if(state == UNINITIALIZED){
      throw new SignatureException("the Signature has not initialized");
    }
    engineUpdate(data, 0, data.length);
  }

  public final void update(byte[] data, int off, int len) throws SignatureException{
    //System.out.println("java.security.Signature: update("+data+", "+off+", "+len+") is called");
    if(state == UNINITIALIZED){
      throw new SignatureException("the Signature has not initialized");
    }
    engineUpdate(data, off, len);
  }

  public final boolean verify(byte[] signature) throws SignatureException{
    //System.out.println("java.security.Signature: verify("+signature+") is called");
    if(state != VERIFY){
      throw new SignatureException("the Signature has an invalid state");
    }
    return engineVerify(signature);
  }

}
