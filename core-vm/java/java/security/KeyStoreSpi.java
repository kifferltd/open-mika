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

/*
** $Id: KeyStoreSpi.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.security;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public abstract class KeyStoreSpi {

  public KeyStoreSpi(){}

  public abstract Enumeration engineAliases();
  public abstract boolean engineContainsAlias(String alias);
  public abstract void engineDeleteEntry(String alias) throws KeyStoreException;
  public abstract Certificate engineGetCertificate(String alias);
  public abstract String engineGetCertificateAlias(Certificate cert);
  public abstract Certificate[] engineGetCertificateChain(String alias);
  public abstract Date engineGetCreationDate(String alias);
  public abstract Key engineGetKey(String alias, char[] password)
        throws NoSuchAlgorithmException, UnrecoverableKeyException;

  public abstract boolean engineIsCertificateEntry(String alias);
  public abstract boolean engineIsKeyEntry(String alias);
  public abstract void engineLoad(InputStream stream, char[] password)
         throws IOException, NoSuchAlgorithmException, CertificateException;

  public abstract void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException;
  public abstract void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException;
  public abstract void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException;
  public abstract int engineSize();
  public abstract void engineStore(OutputStream stream, char[] password)
         throws IOException, NoSuchAlgorithmException, CertificateException;

}
