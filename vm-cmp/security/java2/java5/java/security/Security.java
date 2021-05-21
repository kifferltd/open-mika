/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2007, 2008, 2009 by Chris Gray, /k/ Embedded Java   *
* Solutions. All rights reserved.                                         *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package java.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.harmony.security.fortress.Engine;
import org.apache.harmony.security.fortress.SecurityAccess;
import org.apache.harmony.security.fortress.Services;

public final class Security {

  private Security(){}

  static void permissionCheck(String permission) {
    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkSecurityAccess(permission);
      }
    }
  }

  public static int addProvider(Provider provider) {
    permissionCheck("insertProvider."+provider.getName());
    ArrayList providers = Providers.providers;
    synchronized (providers){
      if (providers.contains(provider)){
        return -1;
      }
      providers.add(provider);
      return providers.size();
    }
  }

/**
** @deprecated
*/
  public static String  getAlgorithmProperty(String algName, String propName){
    ArrayList providers = Providers.providers;
    synchronized(providers){
      int size = providers.size();
      String name = "Alg."+propName+"."+algName;
      for(int i = 0 ; i < size ; i++){
        Provider p = (Provider)providers.get(i);
        if (p != null) {
          String prop = p.getProperty(name);
          if(prop != null){
            return prop;
          }
        }
      }
      return null;
    }
  }

  public static String getProperty(String key){
    permissionCheck("getProperty."+key);
    return SecurityProperties.getProperty(key);
  }

  public static void setProperty(String key, String datum){
    permissionCheck("setProperty."+key);
    SecurityProperties.setProperty(key,datum);
 }


  public static Provider getProvider(String name){
    ArrayList providers = Providers.providers;
    synchronized (providers){
      for (int i=0 ; i < providers.size() ; i++){
        Provider p = (Provider)providers.get(i);
        if ((p != null) && name.equals(p.getName())){
          return p;
        }
      }
    }
    return null;
  }

  public static Provider[] getProviders(){
    ArrayList providers = Providers.providers;
    ArrayList copy;
    synchronized (providers){
      copy = new ArrayList(providers.size());
      for (int i = 0 ; i < providers.size() ; i++){
        Provider p = (Provider)providers.get(i);
        if (p != null) {
          copy.add(p);
        }
      }
    }
    return (Provider[]) copy.toArray(new Provider[copy.size()]);
  }

  public static Provider[] getProviders(Map map){
    Iterator it = map.entrySet().iterator();
    ArrayList providers = Providers.providers;
    ArrayList providers_clone = new ArrayList(providers.size());
    synchronized (providers){
      for (int i = 0 ; i < providers.size() ; i++){
        Provider p = (Provider)providers.get(i);
        if (p != null) {
          providers_clone.add(p);
        }
      }
    }
    try {
      do {
        Map.Entry me = (Map.Entry)it.next();
        String attrib = (String) me.getValue();
        String name = (String) me.getKey();
        Iterator itp = providers_clone.iterator();
        if(attrib == null){
          while(itp.hasNext()){
            Provider p = (Provider)itp.next();
            if(p.get(name) == null){
              it.remove();
            }
          }
        }
        else {
          while(itp.hasNext()){
            Provider p = (Provider)itp.next();
            if(p.get(name) == null){
              it.remove();
            }
            else{
              //TODO check for attribute !
            }
          }
        }
      } while(true);
    }
    catch(java.util.NoSuchElementException nsee){}
    int size = providers_clone.size();
    if(size == 0){
      return null;
    }
    return (Provider[]) providers_clone.toArray(new Provider[size]);
  }

  public static Set getAlgorithms(String serviceName) {
    HashSet set = new HashSet(7);
    ArrayList providers = Providers.providers;
    serviceName = serviceName + '.';
    int idx = serviceName.length();
    int len = providers.size();
    for(int i=0 ; i < len ; i++) {
      Provider p = (Provider)providers.get(i);
      if (p != null) {
        Enumeration enumeration = ((Provider) providers.get(i)).keys();
        while (enumeration.hasMoreElements()) {
          String key = (String) enumeration.nextElement();
          if(key.startsWith(serviceName) && !key.endsWith(" ImplementedIn")) {
            set.add(key.substring(idx));
          }
        }        
      }            
    }    
    return Collections.unmodifiableSet(set);
  }
  
  public static Provider[] getProviders(String name){
    ArrayList providers = Providers.providers;
    int index = name.indexOf(' ');
    ArrayList providers_clone = new ArrayList(providers.size());
    synchronized (providers){
      for (int i = 0 ; i < providers.size() ; i++){
        Provider p = (Provider)providers.get(i);
        if (p != null) {
          providers_clone.add(p);
        }
      }
    }
    Iterator it = providers_clone.iterator();
    if(index != -1){
      //String attribute = name.substring(index).trim();
      name = name.substring(0,index).trim();
      while(it.hasNext()){
        Provider p = (Provider)it.next();
        if(p.get(name) == null){
          it.remove();
        }
        else {
          //TODO check for attribute !
        }
      }
    }
    else {
      while(it.hasNext()){
        Provider p = (Provider)it.next();
        if(p.get(name) == null){
          it.remove();
        }
      }
    }

    int size = providers_clone.size();
    if(size == 0){
      return null;
    }
    return (Provider[]) providers_clone.toArray(new Provider[size]);
  }

  public static int insertProviderAt(Provider provider, int position){
    permissionCheck("insertProvider."+provider.getName());

    ArrayList providers = Providers.providers;
    synchronized (providers){
      if (providers.contains(provider)){
        return -1;
      }
      providers.add(position, provider);
      renumProviders();
      return position;
    }
  }

  public static void removeProvider(String name){
    permissionCheck("removeProvider."+name);
    ArrayList providers = Providers.providers;
    Provider p = getProvider(name);
    if (p != null){
      providers.remove(p);
    }
    renumProviders();
    p.setProviderNumber(-1);
  }

  static void reloadProviders() {
    Providers.loadProviders();
  }

  private static void renumProviders() {
    Provider[] p = Services.getProviders();
    for (int i = 0; i < p.length; i++) {
      p[i].setProviderNumber(i + 1);
    }
  }

  private static class SecurityProperties {
    private final static Properties securityProps;

    static {
      securityProps = new Properties();
      try {
        securityProps.load(ClassLoader.getSystemResourceAsStream("wonka.security"));
      }
      catch(Exception e){}
      Engine.door = new SecurityDoor();
    }

    static int getSize() {
      return securityProps.size();
    }

    static String getProperty(String k) {
      return securityProps.getProperty(k);
    }

    static String setProperty(String k, String v) {
      return (String)securityProps.setProperty(k, v);
    }
  }

  private static class Providers {
    static final ArrayList providers;
    private static int size;

    static {
      size = SecurityProperties.getSize();
      providers = new ArrayList(size);
      loadProviders();
    }

    static void loadProviders() {
      ClassLoader cl = ClassLoader.getSystemClassLoader();
      for (int i = 0; i < size; ++i) {
        String s = "security.provider." + (i + 1);
        s = SecurityProperties.getProperty(s);
        if (s == null){
          size = i;
          break;
        } else {
          if (providers.size() <= i) {
            providers.add(null);
          }
          if (providers.get(i) == null) {
            try {          
              Provider p = (Provider) Class.forName(s, true, cl).newInstance();          
              providers.set(i, p);
            } catch(Exception e) {
            }
          }
        }
      }
    }
  }

  private static class SecurityDoor implements SecurityAccess {
    // Access to Security.renumProviders()
    public void renumProviders() {
      Security.renumProviders();
    }

    //  Access to Security.getAliases()
    public Iterator getAliases(Provider.Service s) {
      return s.getAliases();
    }
                                                 
    // Access to Provider.getService()
    public Provider.Service getService(Provider p, String type) {
      return p.getService(type);
    }
  }

}
