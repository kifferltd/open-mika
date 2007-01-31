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
** $Id: CLJarHandler.java,v 1.2 2006/05/16 08:24:41 cvs Exp $
*/

package wonka.vm;

import java.io.InputStream;
import java.net.URL;
import java.util.jar.*;

public class CLJarHandler extends ClassLoaderURLHandler {

  private JarFile jf;
  private Manifest man;
  private URL url;

  CLJarHandler(JarFile file, URL url){
    this.url = url;
    jf = file;
    try {
      man = jf.getManifest();
    }
    catch (java.io.IOException ioe){
      man = super.getManifest();
    }
  }

  public byte[] getByteArray(String resource){
    try {
      java.util.zip.ZipEntry ze = jf.getEntry(resource);
      if(ze != null){
        InputStream in = jf.getInputStream(ze);
        int len = in.available();
        byte[] bytes = new byte[len];

        /**
        ** since these bytes come from a ByteArrayInputStream we can thrust the read
        ** to get all bytes in one run!
        */

        in.read(bytes,0,len);
        return bytes;
      }
    } catch(Exception e){
      e.printStackTrace();
    }

    return null;
  }

  public InputStream getInputStream(String resource){
    try {
      java.util.zip.ZipEntry ze = jf.getEntry(resource);
      if(ze != null){
        return jf.getInputStream(ze);
      }
    } catch(Exception e){}

    return null;
  }

  public Manifest getManifest(){
    return man;
  }

  public URL getURL(String resource) {
    try {
      if (jf.getEntry(resource) != null) {
        return new URL(url, resource);
      } else {
        String directory = resource + "/";
        if (jf.getEntry(directory) != null) {
          return new URL(url, directory);
        }
      }
    } catch (Exception e) { }
    return null;
  }

  public String toString() {
    return "CLJarHandler for jarfile " + jf + ", URL " + url;
  }
}
