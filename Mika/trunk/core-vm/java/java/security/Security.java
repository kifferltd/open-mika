/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2007 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

public final class Security {

  private Security(){}

  final static Properties securityProps = new Properties();

  static void permissionCheck(String permission) {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new SecurityPermission(permission));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkSecurityAccess(permission);
      }
    }
  }

  public static int addProvider(Provider provider) {
    permissionCheck("insertProvider."+provider.getName());
    Vector providers = Providers.providers;
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
    Vector providers = Providers.providers;
    synchronized(providers){
      int size = providers.size();
      String name = "Alg."+propName+"."+algName;
      for(int i = 0 ; i < size ; i++){
        Provider p = (Provider)providers.get(i);
        String prop = p.getProperty(name);
        if(prop != null){
          return prop;
        }
      }
      return null;
    }
  }

  public static String getProperty(String key){
    permissionCheck("getProperty."+key);
    return securityProps.getProperty(key);
  }

  public static void setProperty(String key, String datum){
    permissionCheck("setProperty."+key);
    securityProps.setProperty(key,datum);
 }


  public static Provider getProvider(String name){
    Vector providers = Providers.providers;
    synchronized (providers){
      for (int i=0 ; i < providers.size() ; i++){
        if (name.equals(((Provider)providers.get(i)).getName())){
          return (Provider)providers.get(i);
        }
      }
    }
    return null;
  }

  public static Provider[] getProviders(){
    Vector providers = Providers.providers;
    return (Provider[]) providers.toArray(new Provider[providers.size()]);
  }

  public static Provider[] getProviders(Map map){
    Iterator it = map.entrySet().iterator();
    Vector providers = Providers.providers;
    Vector providers_clone = (Vector)providers.clone();
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
    Vector providers = Providers.providers;
    serviceName = serviceName + '.';
    int idx = serviceName.length();
    int len = providers.size();
    for(int i=0 ; i < len ; i++) {
      Enumeration enumeration = ((Provider) providers.get(i)).keys();
      while (enumeration.hasMoreElements()) {
        String key = (String) enumeration.nextElement();
        if(key.startsWith(serviceName) && !key.endsWith(" ImplementedIn")) {
          set.add(key.substring(idx));
        }        
      }            
    }    
    return Collections.unmodifiableSet(set);
  }
  
  public static Provider[] getProviders(String name){
    Vector providers = Providers.providers;
    int index = name.indexOf(' ');
    Vector providers_clone = (Vector)providers.clone();
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

    Vector providers = Providers.providers;
    synchronized (providers){
      if (providers.contains(provider)){
        return -1;
      }
      providers.add(position, provider);
      return position;
    }
  }

  public static void removeProvider(String name){
    permissionCheck("removeProvider."+name);
    Vector providers = Providers.providers;
    Provider p = getProvider(name);
    if (p != null){
      providers.remove(p);
    }
  }

  private static class Providers {
    final static Vector providers;

    static {
      providers = new Vector(11);
      try {
        securityProps.load(ClassLoader.getSystemResourceAsStream("wonka.security"));
      }
      catch(Exception e){}
      int size = securityProps.size() + 1;
      for (int i=1 ; i < size ; i++){
        String s = "security.provider."+i;
        s = securityProps.getProperty(s);
        if (s == null){
          break;
        } else {
          try {          
            Provider p = (Provider) Class.forName(s,true,ClassLoader.getSystemClassLoader()).newInstance();          
            providers.add(p);
          } catch(Exception e){
            e.printStackTrace();
          }
        }
      }
    }
  }

}
