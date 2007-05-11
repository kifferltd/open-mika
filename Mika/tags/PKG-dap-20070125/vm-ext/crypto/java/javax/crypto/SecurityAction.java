/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


/**
 * $Id: SecurityAction.java,v 1.1.1.1 2004/07/12 14:07:48 cvs Exp $
 */
package javax.crypto;

import com.acunia.wonka.security.DefaultProvider;
import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.PrivilegedAction;
import java.security.Security;

class SecurityAction implements PrivilegedAction {

  private final static Provider defaultProvider = DefaultProvider.getInstance();

  static ClassLoader getClassLoader(Provider prov){
    return (ClassLoader)AccessController.doPrivileged(new SecurityAction(prov));
  }

  Provider provider;
  Object spi;

  private SecurityAction(Provider prov){
    provider = prov;
  }

  SecurityAction(String algorithm, String type) throws NoSuchAlgorithmException {
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
    throw new NoSuchAlgorithmException("couldn't find "+algorithm+" of type "+type);
  }

  SecurityAction(String algorithm, String providerName, String type) throws NoSuchAlgorithmException, NoSuchProviderException {
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

  SecurityAction(String algorithm, Provider prov, String type) throws NoSuchAlgorithmException {
    provider = prov;
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
