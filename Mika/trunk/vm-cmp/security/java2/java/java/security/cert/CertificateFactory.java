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
 * @version $Id: CertificateFactory.java,v 1.3 2006/04/18 11:35:28 cvs Exp $
 *
 */

public class CertificateFactory {

  public static CertificateFactory getInstance(String type) throws CertificateException {
    CertAction action = new CertAction(type, "CertificateFactory.");
    return new CertificateFactory((CertificateFactorySpi)action.spi, action.provider, type);
  }

  public static CertificateFactory getInstance(String type, String provider)
  throws CertificateException, NoSuchProviderException {

  try {
    CertAction action = new CertAction(type, provider, "CertificateFactory.");
    return new CertificateFactory((CertificateFactorySpi)action.spi, action.provider, type);
  } catch (NoSuchAlgorithmException nsae) {
    CertificateException ce = new CertificateException(nsae.getMessage());
    ce.initCause(nsae);
    throw ce;
  }
}

  public static CertificateFactory getInstance(String type, Provider provider)
    throws CertificateException {

    CertAction action = new CertAction(type, provider, "CertificateFactory.");
    return new CertificateFactory((CertificateFactorySpi)action.spi, action.provider, type);
  }

  private CertificateFactorySpi certFacSpi;
  private Provider provider;
  private String type;

  protected CertificateFactory(CertificateFactorySpi certFacSpi, Provider provider, String type) {
    this.type = type;
    this.provider = provider;
    this.certFacSpi = certFacSpi;
  }

  public final Provider getProvider(){
    return provider;
  }

  public final String getType(){
    return type;
  }

  public final Certificate generateCertificate(InputStream in) throws CertificateException {
    return certFacSpi.engineGenerateCertificate(in);
  }

  public final Collection generateCertificates(InputStream in) throws CertificateException {
    return certFacSpi.engineGenerateCertificates(in);
  }

  public final CertPath generateCertPath(InputStream inStream,
      String encoding) throws CertificateException {
    return certFacSpi.engineGenerateCertPath(inStream, encoding);
  }
  
  public final CertPath generateCertPath(List certificates) throws CertificateException {
    return certFacSpi.engineGenerateCertPath(certificates);
  }
  
  public final CRL generateCRL(InputStream in) throws CRLException {
    return certFacSpi.engineGenerateCRL(in);
  }

  public final Collection generateCRLs(InputStream in) throws CRLException {
    return certFacSpi.engineGenerateCRLs(in);
  }

  public final Iterator getCertPathEncodings() {
    return certFacSpi.engineGetCertPathEncodings();
  }
  
  public final CertPath generateCertPath(InputStream stream) throws CertificateException {
    return certFacSpi.engineGenerateCertPath(stream);
  }

}
