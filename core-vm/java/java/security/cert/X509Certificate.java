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
