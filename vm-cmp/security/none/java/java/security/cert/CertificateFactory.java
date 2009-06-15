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

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * Dummy version for vm-cmp/security/none CG 20090615
 *
 */

public class CertificateFactory {

  public static CertificateFactory getInstance(String type) throws CertificateException {
    return null;
  }

  public static CertificateFactory getInstance(String type, String provider)
  throws CertificateException, NoSuchProviderException {
    return null;
  }

  public static CertificateFactory getInstance(String type, Provider provider)
    throws CertificateException {
    return null;
  }

  private Provider provider;
  private String type;

  protected CertificateFactory(CertificateFactorySpi certFacSpi, Provider provider, String type) {
    this.type = type;
    this.provider = provider;
  }

  public final Provider getProvider(){
    return provider;
  }

  public final String getType(){
    return type;
  }

  public final Certificate generateCertificate(InputStream in) throws CertificateException {
    return null;
  }

  public final Collection generateCertificates(InputStream in) throws CertificateException {
    return null;
  }

  public final CertPath generateCertPath(InputStream inStream,
      String encoding) throws CertificateException {
    return null;
  }
  
  public final CertPath generateCertPath(List certificates) throws CertificateException {
    return null;
  }
  
  public final CRL generateCRL(InputStream in) throws CRLException {
    return null;
  }

  public final Collection generateCRLs(InputStream in) throws CRLException {
    return null;
  }

  public final Iterator getCertPathEncodings() {
    return null;
  }
  
  public final CertPath generateCertPath(InputStream stream) throws CertificateException {
    return null;
  }

}
