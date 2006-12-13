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

  private final static Vector providers = new Vector(11);
  final static Properties securityProps = new Properties();

  static {
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
          //e.printStackTrace();
        }
      }
    }
  }

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
    return (Provider[]) providers.toArray(new Provider[providers.size()]);
  }

  public static Provider[] getProviders(Map map){
    Iterator it = map.entrySet().iterator();
    Vector providers_clone = (Vector) Security.providers.clone();
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
    int index = name.indexOf(' ');
    Vector providers_clone = (Vector) Security.providers.clone();
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
    Provider p = getProvider(name);
    if (p != null){
      providers.remove(p);
    }
  }

}
