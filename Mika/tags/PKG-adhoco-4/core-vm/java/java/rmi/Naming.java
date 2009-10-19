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

