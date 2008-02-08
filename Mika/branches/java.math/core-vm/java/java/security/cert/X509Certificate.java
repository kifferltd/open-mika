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

import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.math.BigInteger;

import javax.security.auth.x500.X500Principal;

/**
 *
 * @version $Id: X509Certificate.java,v 1.3 2006/04/18 11:35:28 cvs Exp $
 *
 */

public abstract class X509Certificate extends Certificate implements X509Extension {

  private static final long serialVersionUID = -6439739127092946520L;

  protected X509Certificate(){
        super("X.509");
  }

  public abstract void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException;
  public abstract void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException;
  public abstract int getBasicConstraints();
  public abstract Principal getIssuerDN();
  public abstract boolean[] getIssuerUniqueID();
  public abstract boolean[] getKeyUsage();
  public abstract Date getNotAfter();
  public abstract Date getNotBefore();
  public abstract BigInteger getSerialNumber();
  public abstract String getSigAlgName();
  public abstract String getSigAlgOID();
  public abstract byte[] getSigAlgParams();
  public abstract byte[] getSignature();
  public abstract Principal getSubjectDN();
  public abstract boolean[] getSubjectUniqueID();
  public abstract byte[] getTBSCertificate() throws CertificateEncodingException;
  public abstract int getVersion();

  /**
   * @since 1.4 
   */
  public X500Principal getSubjectX500Principal() {
    throw new UnsupportedOperationException("unsuported 1.4 Api");
  }

  /**
   * @since 1.4 
   */
  public X500Principal getIssuerX500Principal() {
    throw new UnsupportedOperationException("unsuported 1.4 Api");
  }

  /**
   * @since 1.4 
   */
  public Collection getSubjectAlternativeNames()
    throws CertificateParsingException {
    
    return null;
  }
  
  /**
   * @since 1.4 
   */
  public Collection getIssuerAlternativeNames()
    throws CertificateParsingException {

    return null;
  }
  
  /**
   * @since 1.4 
   */
  public List getExtendedKeyUsage() throws CertificateParsingException {
    return null;
  }
}
