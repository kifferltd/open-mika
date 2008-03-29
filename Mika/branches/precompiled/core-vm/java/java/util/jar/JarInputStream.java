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
