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
** $Id: Provider.java,v 1.3 2006/04/18 11:35:28 cvs Exp $
*/

package java.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class Provider extends Properties{

  private static final long serialVersionUID = -4298000515446427739L;

  /**
  ** Fields dictated by the Serialized Form ...
  */
  private String info;
  private String name;
  private double version;

  private static void permissionCheck(String permission) {
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

  protected Provider(String name, double version, String info){
        this.info = info;
        this.name = name;
        this.version = version;
  }

  public String getInfo(){
        return info;
  }

  public String getName(){
        return name;
  }

  public double getVersion(){
        return version;
  }

  public String toString()  {
        return name+" version: "+version;
  }

  public Set entrySet(){
        return Collections.unmodifiableSet(super.entrySet());
  }

  public Set keySet(){
        return Collections.unmodifiableSet(super.keySet());
  }

  public Collection values(){
        return Collections.unmodifiableCollection(super.values());
  }

  public void clear() {
    permissionCheck("clearProviderProperties."+name);
    super.clear();
  }

  public void load(InputStream in) throws IOException {
    super.load(in);
  }

  public Object put(Object key, Object value) {
    permissionCheck("putProviderProperty."+name);
    return super.put(key,value);
  }

  public void putAll(Map t){
    permissionCheck("putProviderProperty."+name);
    Iterator it = t.entrySet().iterator();
    while (it.hasNext()){
      Map.Entry me = (Map.Entry)it.next();
      super.put(me.getKey(),me.getValue());
    }
  }

  /**
  ** REMOVE THIS METHOD DEBUG ONLY  ...
  */
  public String getProperty(String nm){
    //System.out.println("\n\n\n-------\nLOOKING FOR PROPERTY '"+nm+"' in :\n"+super.toString()+"\n\n\n\n---------\n");
    return super.getProperty(nm);
  }


  public Object remove(Object key) {
    permissionCheck("removeProviderProperty."+name);
    return super.remove(key);
  }


}
