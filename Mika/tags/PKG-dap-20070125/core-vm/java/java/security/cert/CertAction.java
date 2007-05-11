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


/**
 * $Id: CertAction.java,v 1.3 2006/04/18 11:35:28 cvs Exp $
 */
package java.security.cert;

import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;

import com.acunia.wonka.security.DefaultProvider;

class CertAction implements java.security.PrivilegedAction {

  private final static Provider defaultProvider = DefaultProvider.getInstance();

  Provider provider;
  Object spi;

  CertAction(String algorithm, String type) throws CertificateException {
    Provider[] p = Security.getProviders();
    String propName = type+algorithm;
    String aliasName = "Alg.Alias." + propName;
    for (int i=0 ; i < p.length ; i++){
      provider = p[i];
      String classname = provider.getProperty(propName);
      if (classname != null){
        try {
          spi = Class.forName(classname, true, (ClassLoader)AccessController.doPrivileged(this)).newInstance();
          return;
        }catch(Exception e){}
      }
      classname =  getAliasName(aliasName, type);
      if (classname != null){
        try {
          spi =  Class.forName(classname, true, (ClassLoader)AccessController.doPrivileged(this)).newInstance();
          return;
        }catch(Exception e){}
      }
    }
    throw new CertificateException("couldn't find "+algorithm+" of type "+type);
  }

  CertAction(String algorithm, String providerName, String type) throws NoSuchAlgorithmException, NoSuchProviderException {
    provider = Security.getProvider(providerName);
    if (provider == null){
      throw new NoSuchProviderException("couldn't find "+provider);
    }
    String propName = type+algorithm;
    String classname = provider.getProperty(propName);
    if (classname != null){
      try {
        spi = Class.forName(classname, true, (ClassLoader)AccessController.doPrivileged(this)).newInstance();
        return;
      }catch(Exception e){}
    }
    classname = getAliasName("Alg.Alias." + propName, type);
    if (classname != null){
      try {
        spi = Class.forName(classname, true, (ClassLoader)AccessController.doPrivileged(this)).newInstance();
        return;
      }catch(Exception e){}
    }
    throw new NoSuchAlgorithmException("couldn't find "+algorithm+" of type "+type);
  }

  CertAction(String algorithm, Provider provider, String type) throws CertificateException {
    this.provider = provider;
    String propName = type+algorithm;
    String classname = provider.getProperty(propName);
    if (classname != null){
      try {
        spi = Class.forName(classname, true, (ClassLoader)AccessController.doPrivileged(this)).newInstance();
        return;
      }catch(Exception e){}
    }
    classname = getAliasName("Alg.Alias." + propName, type);
    if (classname != null){
      try {
        spi = Class.forName(classname, true, (ClassLoader)AccessController.doPrivileged(this)).newInstance();
        return;
      }catch(Exception e){}
    }
    throw new CertificateException("couldn't find "+algorithm+" of type "+type);
  }

  public Object run(){
    return provider.getClass().getClassLoader();
  }

  private String getAliasName(String aliasName, String type){
    String alias =  provider.getProperty(aliasName);
    if(alias != null){
      alias = defaultProvider.getProperty(aliasName);
    }
    if(alias != null){
      return provider.getProperty(type+alias);
    }
    return null;
  }
}