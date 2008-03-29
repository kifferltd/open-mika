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


/**
 * $Id: SecurityAction.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
 */
package java.security;

import com.acunia.wonka.security.DefaultProvider;

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

  SecurityAction(String algorithm, Provider provider, String type) throws NoSuchAlgorithmException {
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
    throw new NoSuchAlgorithmException("couldn't find "+algorithm+" of type "+type);
  }

  public Object run(){
    return provider.getClass().getClassLoader();
  }

  private String getAliasName(String aliasName, String type){
    String alias =  provider.getProperty(aliasName);
    String classname = null;
    if(alias != null){
      classname = defaultProvider.getProperty(type+alias);
    }
    if(classname == null){
      classname = provider.getProperty(type+alias);
    }
    return classname;
  }
}

