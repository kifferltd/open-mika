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


package java.util.jar;

import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.OutputStream;
import java.io.IOException;

/**
 *
 * @version     $Id: JarOutputStream.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
 *
 */
public class JarOutputStream extends ZipOutputStream {

  public JarOutputStream (OutputStream child) throws IOException {
    	super (child);
    	Manifest man = new Manifest();
    	Attributes main = man.getMainAttributes();
    	main.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    	writeManifest(man);
  }

  public JarOutputStream(OutputStream out, Manifest man) throws IOException {
   	super(out);  	   	   	
    	writeManifest(man);

  }

  public void putNextEntry(ZipEntry ze) throws IOException {
   	super.putNextEntry(ze);
  }

  private void writeManifest(Manifest man) throws IOException {
        JarEntry jman = new JarEntry(JarFile.MANIFEST_NAME);
        super.putNextEntry(jman);
        man.write(this);
        closeEntry();
  }
}
