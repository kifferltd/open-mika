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

package java.rmi;

import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class Naming {

  private Naming(){}

  public static void bind(String name, Remote obj) throws AlreadyBoundException, MalformedURLException, RemoteException {
    StringBuffer buf = new StringBuffer(name.length());
    Registry reg = getRegistry(name, buf);
    reg.bind(buf.toString(), obj);
  }
  
  public static String[] list(String name) throws MalformedURLException, RemoteException {
    StringBuffer buf = new StringBuffer(name.length());
    Registry reg = getRegistry(name, buf);
    return reg.list();
  }
  
  public static Remote lookup(String name)  throws NotBoundException, MalformedURLException, RemoteException {
    StringBuffer buf = new StringBuffer(name.length());
    Registry reg = getRegistry(name, buf);
    return reg.lookup(buf.toString());
  }
  
  public static void rebind(String name, Remote obj) throws MalformedURLException, RemoteException {
    StringBuffer buf = new StringBuffer(name.length());
    Registry reg = getRegistry(name, buf);
    reg.rebind(buf.toString(), obj);
  }
  
  public static void unbind(String name)  throws NotBoundException, MalformedURLException, RemoteException {
    StringBuffer buf = new StringBuffer(name.length());
    Registry reg = getRegistry(name, buf);
    reg.unbind(buf.toString());
  }

  private static Registry getRegistry(String url, StringBuffer name) throws MalformedURLException, RemoteException {
    int port = Registry.REGISTRY_PORT;
    String host = "localhost";
    if(url.startsWith("rmi:")){
      url = url.substring(4);
    }
    if(url.startsWith("//")){
      int index = url.indexOf("/",2);
      if(index == -1){
        throw new MalformedURLException("no name specified in: "+url);
      }
      host = url.substring(2,index);
      url = url.substring(index+1);
      index = host.indexOf(":");
      if(index != -1){
        try {
          port = Integer.parseInt(host.substring(index+1));
        }
        catch(NumberFormatException nfe){
          throw new MalformedURLException("invalid port");
        }
        host = host.substring(0,index);
      }
    }
    name.append(url);
    if(host.length() == 0){
      host = "localhost";
    }

    return LocateRegistry.getRegistry(host,port);
  }
}

