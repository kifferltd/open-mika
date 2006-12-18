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
** $Id: SignatureSpiWrapper.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.security;

import java.security.spec.AlgorithmParameterSpec;

class SignatureSpiWrapper extends Signature {

  private SignatureSpi core;

  public SignatureSpiWrapper(SignatureSpi core, String algorithm){
    super(algorithm);
    this.core = core;
  }

  protected Object engineGetParameter(String param) throws InvalidParameterException{
     return core.engineGetParameter(param);
  }

  protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException{
    core.engineInitSign(privateKey);
  }

  protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException{
    core.engineInitVerify(publicKey);
  }

  protected void engineSetParameter(String param, Object value) throws InvalidParameterException{
    core.engineSetParameter(param, value);
  }

  protected byte[] engineSign() throws SignatureException{
    return core.engineSign();
  }

  protected void engineUpdate(byte b) throws SignatureException{
    core.engineUpdate(b);
  }

  protected void engineUpdate(byte[] b, int off, int len) throws SignatureException{
    core.engineUpdate(b, off, len);
  }

  protected boolean engineVerify(byte[] sigBytes) throws SignatureException{
    return core.engineVerify(sigBytes);
  }

  protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
    core.engineInitSign(privateKey, random);
  }

  protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException{
    core.engineSetParameter(params);
  }

  protected int engineSign(byte[] outbuf, int offset, int len) throws SignatureException{
    return core.engineSign(outbuf, offset, len);
  }
}
