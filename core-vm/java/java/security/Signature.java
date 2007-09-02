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
