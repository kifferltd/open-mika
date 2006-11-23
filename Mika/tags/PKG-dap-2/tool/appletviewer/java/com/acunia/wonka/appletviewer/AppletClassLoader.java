/**************************************************************************
* Copyright  (c) 2002 by Acunia N.V. All rights reserved.                 *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


package com.acunia.wonka.appletviewer;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.security.*;
import java.util.jar.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.HashMap;

public class AppletClassLoader extends URLClassLoader{

  static AppletClassLoader createAppletClassLoader(URL codebase, URL documentbase, String archives) throws java.io.IOException {
    Vector urls = new Vector(16);
    if(archives != null){
      StringTokenizer stk = new StringTokenizer(archives,",");
      while(stk.hasMoreTokens()){
        try {
          //this url will point to a jar/zip file, but it is not a Jar URL
          URL url = new URL(documentbase,stk.nextToken());
          urls.add(new URL("jar:"+url.toString()+"!/"));
        }
        catch(MalformedURLException murle){}
      }
    }
    if(codebase != documentbase){
      urls.add(codebase);
    }
    urls.add(documentbase);

    System.out.println(urls);

    URL[] bases = new URL[urls.size()];
    urls.toArray(bases);
    return new AppletClassLoader(bases);
  }

  private HashMap permissions;

  private AppletClassLoader(URL[] bases) throws java.io.IOException {
    super(bases);
    permissions = new HashMap(bases.length);
    for(int i = 0 ; i < bases.length ; i++){
      Permissions p = new Permissions();
      p.add(bases[i].openConnection().getPermission());
      permissions.put(new CodeSource(bases[i],null),p);
    }
  }

  protected PermissionCollection getPermissions(CodeSource cs){
     Object o = permissions.get(cs);
     if(o != null){
        return (PermissionCollection) o;
     }

    throw new SecurityException("AppletClassLoader cannot give permissions to unknown CodeSource "+cs);
  }
}














