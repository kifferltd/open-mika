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
