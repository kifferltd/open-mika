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
