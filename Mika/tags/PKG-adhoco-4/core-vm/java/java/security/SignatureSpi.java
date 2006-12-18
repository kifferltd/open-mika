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
** $Id: SignatureSpi.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
*/

package java.security;

import java.security.spec.AlgorithmParameterSpec;

public abstract class SignatureSpi {

  protected SecureRandom appRandom;

  public SignatureSpi(){}

  public Object clone() throws CloneNotSupportedException{
        return super.clone();
  }

/**
** @deprecated
*/
  protected abstract Object engineGetParameter(String param) throws InvalidParameterException;
  protected abstract void engineInitSign(PrivateKey privateKey) throws InvalidKeyException;
  protected abstract void engineInitVerify(PublicKey publicKey) throws InvalidKeyException;
/**
**   @deprecated Replaced by engineSetParameter.
*/
  protected abstract void engineSetParameter(String param, Object value) throws InvalidParameterException;
  protected abstract byte[] engineSign() throws SignatureException;
  protected abstract void engineUpdate(byte b) throws SignatureException;
  protected abstract void engineUpdate(byte[] b, int off, int len) throws SignatureException;
  protected abstract boolean engineVerify(byte[] sigBytes) throws SignatureException;


/**
** method was added in 1.2.  It should be abstract, but it cannot be for backworth compatibility.
** The implementation does nothing.
**
** @remark should be overridden
*/
  protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {}
/**
** method was added in 1.2.  It should be abstract, but it cannot be for backworth compatibility.
** The implementation does nothing.
**
** @remark should be overridden
*/
  protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException{

  }
/**
** method was added in 1.2.  It should be abstract, but it cannot be for backworth compatibility.
** The implementation does nothing. It simply returns 0.
**
** @remark should be overridden
*/
  protected int engineSign(byte[] outbuf, int offset, int len) throws SignatureException{
        return 0;
  }

  /**
   * The default implementation throws a UnsupportOperationException
   * @return the parameters used with this signature engine, 
   *   or null not using any parameters
   * @since 1.4
   */
  protected AlgorithmParameters engineGetParameters() {
    throw new UnsupportedOperationException();
  }
  
  /**
   * @since 1.4
   * @throws SignatureException by default...
   */
  protected boolean engineVerify(byte[] sigBytes, int offset, int length)
    throws SignatureException {
    
    throw new SecurityException("Unsupported method !");    
  }
}
