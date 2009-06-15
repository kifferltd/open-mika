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


package java.security.cert;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.PublicKey;
import java.security.Principal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

/**
 *
 * @version $Id: X509CRL.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
 *
 */

public abstract class X509CRL extends CRL implements X509Extension {

  protected X509CRL(){
        super("X.509");
  }

  public boolean equals(Object other){
        if (!(other instanceof X509CRL)){
  	        return false;
        }
        X509CRL x509 = (X509CRL) other;
        try {
                return Arrays.equals(x509.getEncoded(), getEncoded());
        } catch(CRLException e){ return false; }
  }

  public int hashCode(){
        try {
                return getEncoded().hashCode();
        } catch(CRLException e){ return 0; }
  }

  public abstract byte[] getEncoded() throws CRLException;
  public abstract Principal getIssuerDN();
  public abstract Date getNextUpdate();
  public abstract X509CRLEntry getRevokedCertificate(BigInteger serialNumber);
  public abstract Set getRevokedCertificates();
  public abstract String getSigAlgName();
  public abstract String getSigAlgOID();
  public abstract byte[] getSigAlgParams();
  public abstract byte[] getSignature();
  public abstract byte[] getTBSCertList() throws CRLException;
  public abstract Date getThisUpdate();
  public abstract int getVersion();

  public abstract void verify(PublicKey key) throws CRLException,
        NoSuchAlgorithmException, InvalidKeyException,
        NoSuchProviderException, SignatureException;

  public abstract void verify(PublicKey key, String sigProvider)  throws CRLException,
        NoSuchAlgorithmException, InvalidKeyException,
        NoSuchProviderException, SignatureException;
  
  /**
   * default implementation throws UnsupportedOPerationException
   * @since 1.4
   */
  public X500Principal getIssuerX500Principal() {
    throw new UnsupportedOperationException();
  }
}
