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

/**
 * @version     $Id: JarInputStream.java,v 1.4 2006/04/05 09:20:06 cvs Exp $
 */

package java.util.jar;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The JarInputStream is not able to verify signatures ...
 *
 * this option must be added !
 */
public class JarInputStream extends ZipInputStream {

  private Manifest manifest;

  public JarInputStream (InputStream child) throws IOException {
    this(child, false);
  }
  public JarInputStream (InputStream child, boolean verify) throws IOException {
    super (child);
  }

  protected ZipEntry createZipEntry(String ename){
   	return new JarEntry(ename);   	   	
  }

  public Manifest getManifest() {
   	return manifest;
  }

  private void readManifest() throws IOException {
      manifest = new Manifest(this);	
  }

  public ZipEntry getNextEntry() throws IOException {
    JarEntry je = (JarEntry)super.getNextEntry();

    if (je != null) {
      if (je.getName().endsWith(JarFile.MANIFEST_NAME)) {
        readManifest();
        return getNextEntry();
      }
      if (manifest !=  null) {
  	 je.setAttributes(manifest.getAttributes(je.getName()));
      }
    }

    return je;
  }

  public JarEntry getNextJarEntry() throws IOException {
   	return (JarEntry)getNextEntry();
  }

  public int read(byte [] buf, int offset, int length) throws IOException {
	  return super.read(buf, offset, length);	
  }




}
