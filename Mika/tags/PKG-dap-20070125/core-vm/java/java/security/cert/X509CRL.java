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
