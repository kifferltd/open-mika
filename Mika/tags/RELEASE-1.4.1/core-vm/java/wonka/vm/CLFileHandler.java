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
** $Id: CLFileHandler.java,v 1.3 2006/05/16 08:24:41 cvs Exp $
*/

package wonka.vm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

public class CLFileHandler extends ClassLoaderURLHandler {

  private File directory;

  CLFileHandler(File file){
    directory = file;
  }

  public byte[] getByteArray(String resource){
    try {
      InputStream in =  new FileInputStream(new File(directory, resource));
      int len_avail = in.available();
      int len_read = 0;
      byte[] bytes = new byte[len_avail];
      while (len_read < len_avail) {
        int l = in.read(bytes,0,len_avail-len_read);
	if (l < 0) {
          return null;
	}
	len_read += l;
      }

      /**
      ** we might need to check if we got all data ...
      */

      return bytes;
    } catch(Exception e){}

    return null;
  }

  public InputStream getInputStream(String resource){
    try {
      return new FileInputStream(new File(directory, resource));
    } catch(Exception e){}

    return null;
  }

  public URL getURL(String resource){
    try {
      File f = new File(directory, resource);
      if(f.isFile()){
        return f.toURL();
      }
    } catch(Exception e){}

    return null;
  }

  public String toString() {
    return "CLFileHandler for directory " + directory;
  }
}
