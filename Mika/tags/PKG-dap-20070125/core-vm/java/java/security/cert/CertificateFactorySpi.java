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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @version $Id: CertificateFactorySpi.java,v 1.3 2006/04/18 11:35:28 cvs Exp $
 *
 */

public abstract class CertificateFactorySpi {

  public CertificateFactorySpi(){}

  public abstract Certificate engineGenerateCertificate(InputStream inStream) throws CertificateException;
  public abstract Collection engineGenerateCertificates(InputStream inStream) throws CertificateException;
  public abstract CRL engineGenerateCRL(InputStream inStream) throws CRLException;
  public abstract Collection engineGenerateCRLs(InputStream inStream) throws CRLException;

  public Iterator engineGetCertPathEncodings() {
    throw new UnsupportedOperationException("new in 1.4 Api");        
  }

  public CertPath engineGenerateCertPath(InputStream inStream,
      String encoding) throws CertificateException {
    
    throw new UnsupportedOperationException("new in 1.4 Api");            
  }

  public CertPath engineGenerateCertPath(InputStream stream) 
      throws CertificateException {
    
    throw new UnsupportedOperationException("new in 1.4 Api");          
  }

  public CertPath engineGenerateCertPath(List certificates)
      throws CertificateException {
    
    throw new UnsupportedOperationException("new in 1.4 Api");          
  }
}
