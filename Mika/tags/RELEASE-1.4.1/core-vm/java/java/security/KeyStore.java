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
** $Id: KeyStore.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
*/

package java.security;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class KeyStore {

  public static String getDefaultType(){
      // if keystore.type not present in properties, return jks
      String keystoreProp = Security.getProperty("keystore.type");
      if (keystoreProp == null)
          return "jks";
      else
          return keystoreProp;
  }

  public static KeyStore getInstance(String type) throws KeyStoreException {
    try {
      SecurityAction action = new SecurityAction(type, "KeyStore.");
      return new KeyStore((KeyStoreSpi)action.spi, action.provider, type);
    } catch(NoSuchAlgorithmException nsae) {
      KeyStoreException kse = new KeyStoreException(nsae.getMessage());
      kse.initCause(nsae);
      throw kse;
    }
  }

  public static KeyStore getInstance(String type, String provider)
    throws KeyStoreException, NoSuchProviderException {

    try {
      SecurityAction action = new SecurityAction(type, provider, "KeyStore.");
      return new KeyStore((KeyStoreSpi)action.spi, action.provider, type);
    } catch(NoSuchAlgorithmException nsae) {
      KeyStoreException kse = new KeyStoreException(nsae.getMessage());
      kse.initCause(nsae);
      throw kse;
    }
  }

  public static KeyStore getInstance(String type, Provider provider)
    throws KeyStoreException {

    try {
      SecurityAction action = new SecurityAction(type, provider, "KeyStore.");
      return new KeyStore((KeyStoreSpi)action.spi, action.provider, type);
    } catch(NoSuchAlgorithmException nsae) {
      KeyStoreException kse = new KeyStoreException(nsae.getMessage());
      kse.initCause(nsae);
      throw kse;
    }
  }
  
  private KeyStoreSpi keySpi;
  private Provider provider;
  private String type;

  protected KeyStore(KeyStoreSpi keyStoreSpi, Provider provider, String type){
    this.keySpi = keyStoreSpi;
    this.provider = provider;
    this.type = type;
  }

  public final Enumeration aliases() throws KeyStoreException {
    return keySpi.engineAliases();
  }

  public final boolean containsAlias(String alias) throws KeyStoreException {
    return keySpi.engineContainsAlias(alias);
  }

  public final void deleteEntry(String alias) throws KeyStoreException {
    keySpi.engineDeleteEntry(alias);
  }

  public final Certificate getCertificate(String alias) throws KeyStoreException {
    return keySpi.engineGetCertificate(alias);
  }

  public final String getCertificateAlias(Certificate cert) throws KeyStoreException {
    return keySpi.engineGetCertificateAlias(cert);
  }

  public final Certificate[] getCertificateChain(String alias) throws KeyStoreException {
    return keySpi.engineGetCertificateChain(alias);
  }

  public final Date getCreationDate(String alias) throws KeyStoreException {
    return keySpi.engineGetCreationDate(alias);
  }

  public final Key getKey(String alias, char[] password)
    throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
    return keySpi.engineGetKey(alias, password);
  }

  public final Provider getProvider(){
    return provider;
  }

  public final String getType(){
    return type;
  }

  public final boolean isCertificateEntry(String alias) throws KeyStoreException {
    return keySpi.engineIsCertificateEntry(alias);
  }

  public final boolean isKeyEntry(String alias)  throws KeyStoreException {
    return keySpi.engineIsKeyEntry(alias);
  }

  public final void load(InputStream stream, char[] password)
    throws IOException, NoSuchAlgorithmException, CertificateException{
    keySpi.engineLoad(stream, password);
  }

  public final void setCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
    keySpi.engineSetCertificateEntry(alias, cert);
  }

  public final void setKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
    keySpi.engineSetKeyEntry(alias, key, chain);
  }

  public final void setKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
    keySpi.engineSetKeyEntry(alias, key, password, chain);
  }

  public final int size() throws KeyStoreException {
    return keySpi.engineSize();
  }

  public final void store(OutputStream stream, char[] password)
    throws IOException, NoSuchAlgorithmException, 
           CertificateException, KeyStoreException  {

    keySpi.engineStore(stream, password);
  }
}
