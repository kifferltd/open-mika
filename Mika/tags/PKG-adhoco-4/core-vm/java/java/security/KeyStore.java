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
