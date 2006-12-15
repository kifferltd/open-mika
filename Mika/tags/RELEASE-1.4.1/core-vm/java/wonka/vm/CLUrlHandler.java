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
** $Id: CLUrlHandler.java,v 1.2 2006/05/16 08:24:41 cvs Exp $
*/

package wonka.vm;

import java.io.*;
import java.net.URL;

public class CLUrlHandler extends ClassLoaderURLHandler {

  protected URL url;

  CLUrlHandler(URL file){
    if(file.getFile().endsWith(".jar")){
      try {
        url = new URL("jar:"+file+"!/");
      }
      catch(Exception e){}
    }
    else {
      url = file;
    }
  }

  public byte[] getByteArray(String resource){
    try {
      URL res = new URL(url, resource);
      InputStream in = res.openStream();
      byte[] bytes = new byte[1024];
      ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
      int j = in.read(bytes,0,1024);
      while(j != -1){
        out.write(bytes,0,j);
        j = in.read(bytes,0,1024);
      }
      return out.toByteArray();
    } catch(Exception e){}

    return null;
  }

  public InputStream getInputStream(String resource){
    try {
      URL res = new URL(url, resource);
      return res.openStream();
    } catch(Exception e){}

    return null;
  }

  public URL getURL(String resource){
    try {
      URL res = new URL(url, resource);
      res.openStream();
      return res;
    } catch(Exception e){}

    return null;
  }

  public String toString() {
    return "CLUrlHandler for URL " + url;
  }
}
