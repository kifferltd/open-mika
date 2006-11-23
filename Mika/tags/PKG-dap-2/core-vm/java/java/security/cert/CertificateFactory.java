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
